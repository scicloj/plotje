(ns scicloj.plotje.impl.aesthetics-test
  "Tests for the two decisions that specifying an aesthetic makes.

   The table these check against is the one ggplot2 already has,
   measured on 4.0.0: `aes(size=5)` scales a written value and gives it
   a legend, `geom_point(size=5)` draws it, `aes(size=b)` scales a
   column, `scale_*_identity()` draws one. All four cells, for every
   aesthetic that has them."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.impl.aesthetics :as aes]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.pose-schema :as pose-schema]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.api :as pj]))

(def cols
  "A layer's column names. `\"blue\"` is here on purpose: a string can
   name a column whose name is also a color, and the data decides."
  #{:height :weight :species "blue" 0 1})

(deftest source-is-decided-by-the-data-test
  (testing "a name the data carries is a column, whatever its type"
    (doseq [v [:height :weight "blue" 0 1]]
      (is (= :column (aes/source v cols)) (pr-str v))))

  (testing "anything else is a written value"
    (doseq [v [:sepal-length "red" "#FF0000" 5 7.5 :circle nil]]
      (is (= :value (aes/source v cols)) (pr-str v))))

  (testing "a column named blue wins over the color of that name"
    ;; The whole reason source is answered before scale.
    (is (= :column (aes/source "blue" cols)))
    (is (= :value (aes/source "red" cols))))

  (testing "matching is strict, as everywhere else in Plotje"
    (is (= :value (aes/source :blue cols)) "keyword does not find the string")
    (is (= :value (aes/source "height" cols)) "string does not find the keyword")))

(deftest every-by-value-aesthetic-can-say-what-it-draws-test
  ;; A `:by-value` entry with no drawn-value schema would silently
  ;; answer "not drawable" for everything and so scale everything --
  ;; the class of defect that let `:shape :circle` pass every check and
  ;; draw nothing.
  (doseq [[k {:keys [scale-default]}] defaults/aesthetic-registry]
    (when (= :by-value scale-default)
      (is (contains? pose-schema/drawn-value-schemas k)
          (str k " decides by value but says nothing about what it draws")))))

(deftest drawable-reads-the-drawn-value-schema-test
  (testing "colors on the two color-valued aesthetics"
    (doseq [k [:color :fill]]
      (is (aes/drawable? k "red") (str k))
      (is (aes/drawable? k "#FF0000") (str k))
      (is (aes/drawable? k :steelblue) (str k))
      (is (not (aes/drawable? k "setosa")) (str k))
      (is (not (aes/drawable? k "fff")) (str k " -- a likely column typo"))))

  (testing "symbols on shape"
    (is (aes/drawable? :shape :circle))
    (is (aes/drawable? :shape :cross))
    (is (not (aes/drawable? :shape :sphere)))
    (is (not (aes/drawable? :shape "circle")) "the symbols are keywords"))

  (testing "the magnitudes, which are drawable but decide by source"
    ;; Both of these are drawable and a valid datum alike, which is
    ;; exactly why no value predicate could decide their scaling.
    (is (aes/drawable? :size 7))
    (is (not (aes/drawable? :size -1)) "a radius is positive")
    (is (aes/drawable? :alpha 0.3))
    (is (not (aes/drawable? :alpha 1.5)) "an opacity is within 0 and 1"))

  (testing "a mistyped column name is not drawable, which is what reports it"
    ;; The guard the source rule needs: `:size :wieght` names no column
    ;; and is no radius either, so neither reading fits and it is an
    ;; error rather than a bogus drawn value.
    (is (not (aes/drawable? :size :wieght)))
    (is (not (aes/drawable? :alpha :opacty)))
    (is (not (aes/drawable? :x "red"))))

  (testing "an aesthetic that draws nothing of its own draws nothing"
    (doseq [v [5 "red" :circle]]
      (is (not (aes/drawable? :group v)) (pr-str v)))))

(deftest x-and-y-are-always-scaled-test
  (testing "both sources, and both shapes of value"
    (doseq [k [:x :y :x-end :y-min :y-max]]
      (is (aes/scaled? k {:source :column :value :height}) (str k " column"))
      (is (aes/scaled? k {:source :value :value 6.5}) (str k " value"))))

  (testing "a number on x is a datum, not a coordinate"
    ;; This is what `pj/lay-text {:x 6.5 :y 3.5}` relies on, and what
    ;; makes the value count toward the domain as annotate() does.
    (is (aes/scaled? :x {:source :value :value 6.5}))))

(deftest size-and-alpha-decide-by-source-test
  (doseq [k [:size :alpha]]
    (testing (str k " scales a column")
      (is (aes/scaled? k {:source :column :value :weight})))
    (testing (str k " draws a written value")
      (is (not (aes/scaled? k {:source :value :value 7}))))))

(deftest color-decides-by-source-test
  (testing "a written value is drawn, whatever it is"
    ;; The vocabulary is not consulted here. It decides at the gate
    ;; whether the value can be used at all, so `"red"` is drawn and
    ;; `"Model A"` -- neither a column nor a color -- is reported
    ;; rather than quietly becoming a one-entry legend.
    (is (not (aes/scaled? :color {:source :value :value "red"})))
    (is (not (aes/scaled? :color {:source :value :value :steelblue})))
    (is (not (aes/scaled? :color {:source :value :value "Model A"}))))

  (testing "and reading one as data is asked for, not inferred"
    (is (aes/scaled? :color {:source :value :value "Model A" :scale true})))

  (testing "a column is scaled whatever it holds"
    ;; The vocabulary is asked of a value and never of a column. The
    ;; reading that asked it of a column produced three defects: the
    ;; same column behaved differently for "red" than for "Red";
    ;; `:shape` answered "drawn" while `collect-shapes` scaled it
    ;; anyway; and `:fill` held the same disagreement unnoticed.
    (is (aes/scaled? :color {:source :column :value :species}))
    (is (aes/scaled? :color {:source :column :value :hexes}))))

(deftest a-column-does-not-depend-on-the-rows-it-holds-test
  ;; What a column means cannot turn on which rows happen to be in it.
  ;; `scaled?` takes no column values at all now, so there is nothing
  ;; for the contents to change.
  (doseq [k [:color :shape :fill :size :alpha :x :y]]
    (is (aes/scaled? k {:source :column :value :c}) (str k " column scales")))

  (testing "including the two that used to escape"
    ;; A column of colors, and a column of shape symbols.
    (is (aes/scaled? :color {:source :column :value :hexes}))
    (is (aes/scaled? :shape {:source :column :value :symbols})))

  (testing "except where there is no scale to pass through"
    (is (not (aes/scaled? :text {:source :column :value :label})))
    (is (not (aes/scaled? :group {:source :column :value :country}))))

  (testing "and `:scale false` is the one way off"
    (is (not (aes/scaled? :color {:source :column :value :hexes :scale false})))))

(deftest an-explicit-scale-overrides-the-convention-test
  (testing "false draws what the convention would scale"
    (is (not (aes/scaled? :x {:source :column :value :height :scale false})))
    (is (not (aes/scaled? :color {:source :column :value :species
                                  :column-values ["setosa"] :scale false}))))

  (testing "true scales what the convention would draw"
    ;; ggplot2's aes(size=5) against geom_point(size=5).
    (is (aes/scaled? :size {:source :value :value 7 :scale true}))
    (is (aes/scaled? :color {:source :value :value "red" :scale true})))

  (testing "a scale type scales"
    (is (aes/scaled? :size {:source :value :value 7 :scale :log})))

  (testing "absent and nil both leave it to the convention"
    (is (not (aes/scaled? :size {:source :value :value 7})))
    (is (not (aes/scaled? :size {:source :value :value 7 :scale nil})))))

(deftest a-color-column-drawn-end-to-end-test
  ;; What the decision is for: the plan a pose actually produces.
  (let [colors-of (fn [pose] (->> (pj/plan pose) :panels first :layers first
                                  :groups (mapv :color)))
        legend-of (fn [pose] (:legend (pj/plan pose)))]
    (testing "a column of hex codes draws itself when asked, with no legend"
      ;; `scale_colour_identity()`, and asking is the only way to it.
      (let [p (-> {:x [1 2 3] :y [4 5 6] :c ["#FF0000" "#00FF00" "#0000FF"]}
                  (pj/pose :x :y)
                  (pj/lay-point {:color {:column :c :scale false}}))]
        (is (= [[1.0 0.0 0.0 1.0] [0.0 1.0 0.0 1.0] [0.0 0.0 1.0 1.0]] (colors-of p)))
        (is (nil? (legend-of p))
            "a legend explains a choice, and here there was none to make")))

    (testing "CSS names count too"
      (is (= [[1.0 0.0 0.0 1.0] [0.0 0.0 1.0 1.0]]
             (colors-of (-> {:x [1 2] :y [3 4] :c ["red" "blue"]}
                            (pj/pose :x :y)
                            (pj/lay-point {:color {:column :c :scale false}}))))))

    (testing "unasked, the same column is three categories"
      ;; The rows it holds do not decide what it means.
      (let [p (-> {:x [1 2 3] :y [4 5 6] :c ["#FF0000" "#00FF00" "#0000FF"]}
                  (pj/pose :x :y)
                  (pj/lay-point {:color :c}))]
        (is (not= [1.0 0.0 0.0 1.0] (first (colors-of p))))
        (is (= 3 (count (:entries (legend-of p)))) "with a legend, as any column has")))

    (testing "and capitalization cannot change the answer"
      ;; It did: `["red" "green" "blue"]` drew itself while
      ;; `["Red" "Green" "Blue"]` took the palette.
      (let [plan-for (fn [vs] (-> {:x [1 2 3] :y [4 5 6] :c vs}
                                  (pj/pose :x :y)
                                  (pj/lay-point {:color :c})))]
        (is (= (colors-of (plan-for ["red" "green" "blue"]))
               (colors-of (plan-for ["Red" "Green" "Blue"]))))))

    (testing "an ordinary category column is untouched"
      (let [p (-> {:x [1 2 3] :y [4 5 6] :s ["a" "b" "a"]}
                  (pj/pose :x :y)
                  (pj/lay-point {:color :s}))]
        (is (= 2 (count (colors-of p))))
        (is (= 2 (count (:entries (legend-of p)))))))))

(deftest the-explicit-form-overrides-the-convention-end-to-end-test
  (let [produce {:x [1 2 3] :y [4 5 6] :variety ["olive" "plum" "tomato"]}
        cols (fn [p] (mapv :color (-> (pj/plan p) :panels first :layers first :groups)))
        olive [(/ 128.0 255) (/ 128.0 255) 0.0 1.0]]

    (testing "the convention scales a column, colour-named values and all"
      ;; Olive, plum and tomato are varieties here, not colours, and
      ;; the convention no longer has to guess which.
      (let [p (-> produce (pj/pose :x :y) (pj/lay-point {:color :variety}))]
        (is (not= olive (first (cols p))))
        (is (= 3 (count (:entries (:legend (pj/plan p))))))))

    (testing ":scale false is what asks for the values themselves"
      (is (= olive (first (cols (-> produce (pj/pose :x :y)
                                    (pj/lay-point {:color {:column :variety
                                                           :scale false}})))))))

    (testing ":scale false draws a column the convention would scale"
      (is (= [[1.0 0.0 0.0 1.0] [0.0 0.0 1.0 1.0]]
             (cols (-> {:x [1 2] :y [3 4] :s ["red" "blue"]}
                       (pj/pose :x :y)
                       (pj/lay-point {:color {:column :s :scale false}}))))))

    (testing ":scale false on values the aesthetic cannot draw is reported"
      ;; Unchecked this drew near-identical greys and said nothing,
      ;; because clojure2d reads a bare `a` as the hex `#aaaaaa`.
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"given :scale false.*is not one :color can draw"
           (pj/plan (-> {:x [1 2] :y [3 4] :s ["a" "b"]}
                        (pj/pose :x :y)
                        (pj/lay-point {:color {:column :s :scale false}}))))))

    (testing ":value insists on the value where a column of that name exists"
      (is (= [[0.0 0.0 1.0 1.0]]
             (cols (-> {:x [1 2] :y [3 4] "blue" ["p" "q"]}
                       (pj/pose :x :y)
                       (pj/lay-point {:color {:value "blue"}}))))))

    (testing ":column insists on the column"
      (is (= 2 (count (cols (-> {:x [1 2] :y [3 4] "blue" ["p" "q"]}
                                (pj/pose :x :y)
                                (pj/lay-point {:color {:column "blue"}})))))))

    (testing "the axes take the same form"
      (let [x-domain #(-> (pj/plan %) :panels first :x-domain)]
        (is (= (x-domain (-> produce (pj/lay-point :x :y)))
               (x-domain (-> produce (pj/lay-point {:x {:column :x}
                                                    :y :y})))))))

    (testing "and it settles a number that is also a column name"
      ;; The bare form is refused at the pose for exactly this ambiguity,
      ;; so the explicit one is the only way to say either.
      (let [ds {0 [1 2 3] 1 [4 5 6]}
            x-domain #(-> (pj/plan %) :panels first :x-domain)]
        (is (= [0.9 3.1] (x-domain (-> ds (pj/lay-point {:x {:column 0} :y 1}))))
            "the column named 0")
        (is (= [-1.0 1.0] (x-domain (-> ds (pj/lay-point {:x {:value 0} :y 1}))))
            "the value 0")))

    (testing ":scale false on an axis places in drawing units"
      ;; An unscaled x or y is a distance across the panel rather than a
      ;; value on the axis, so it informs no domain -- and the axes are
      ;; asked separately, so one can be scaled while the other is not.
      (let [p (pj/plan (-> {:a [0 1 2 3] :b [0 1 2 3]}
                           (pj/lay-point {:x {:column :a}
                                          :y {:column :b :scale false}})))
            panel (-> p :panels first)]
        (is (= [0 1] (:y-domain panel))
            "y contributed nothing, so the domain fell back")
        (is (< (first (:x-domain panel)) 0)
            "x still went through its scale")
        (is (true? (-> panel :layers first :y-drawn?)))
        (is (nil? (-> panel :layers first :x-drawn?)))))

    (testing "a written value asked to scale becomes a datum"
      ;; ggplot2's constant inside aes(): one value repeated over every
      ;; row is a column of one distinct value, so the scales and the
      ;; legend read it as they read any column.
      (let [p (pj/plan (-> produce (pj/lay-point {:x :x :y :y
                                                  :color {:value "Model A"
                                                          :scale true}})))]
        (is (= ["Model A"] (mapv :label (:entries (:legend p)))))
        (is (= 1 (count (-> p :panels first :layers first :groups)))))
      (let [p (pj/plan (-> produce (pj/lay-point {:x :x :y :y
                                                  :size {:value 7 :scale true}})))]
        (is (= [7 7 7] (-> p :panels first :layers first :groups first :sizes vec)))
        (is (some? (:size-legend p)))))

    (testing "a size column told not to scale holds radii already"
      ;; ggplot2's scale_size_identity(). Unreachable by convention,
      ;; since every number is a valid radius and a valid measurement.
      (let [p (pj/plan (-> {:a [1 2 3] :b [1 2 3] :r [4 8 12]}
                           (pj/lay-point :a :b {:size {:column :r :scale false}})))]
        (is (true? (-> p :panels first :layers first :size-drawn?)))
        (is (nil? (:size-legend p)) "no scale, so nothing to explain")))

    (testing "naming both sources, or an unknown key, is reported"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"names both :column and :value"
           (pj/plan (-> produce (pj/pose :x :y)
                        (pj/lay-point {:color {:column :variety :value "red"}})))))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"unexpected key"
           (pj/plan (-> produce (pj/pose :x :y)
                        (pj/lay-point {:color {:column :variety :in :data}}))))))))

(deftest an-aesthetic-with-no-scale-is-never-scaled-test
  (testing "text has a reading but no scale"
    (is (not (aes/scaled? :text {:source :column :value :label})))
    (is (not (aes/scaled? :text {:source :value :value "n = 150"}))))

  (testing "group has no scale at all, and cannot be given one"
    ;; `pj/scale` refuses the channel for the same reason: grouping
    ;; splits the data and draws nothing of its own. Asking for one
    ;; here is reported where the mapping is built, not honored.
    (is (not (aes/scaled? :group {:source :column :value :country})))
    (is (not (aes/scaled? :group {:source :column :value :country :scale true})))))

(def measured
  "A layer's data for the end-to-end checks below. `:hex` holds colors
   and `:variety` holds categories, so one column is drawn as it stands
   and the other is scaled."
  {:when [1 2 3 4]
   :level [1 4 2 3]
   :radius [4 8 12 16]
   :hex ["#FF0000" "#00FF00" "#0000FF" "#FF00FF"]
   :variety ["olive" "plum" "olive" "plum"]})

(deftest an-explicit-column-is-looked-up-like-any-other-test
  ;; Honoring `{:column ...}` settles which of the two readings applies.
  ;; It is not a promise that the column is there, and taking it for one
  ;; dropped the aesthetic without a word: a mistyped `:color` drew the
  ;; default grey, and a mistyped `:group` resolved to zero groups, so
  ;; the layer left the plot with nothing said.
  (doseq [k [:color :size :alpha :shape :group]]
    (testing (str "a missing column named by " k " is reported")
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Column :nosuch .* not found in dataset"
           (pj/plan (-> measured (pj/lay-point :when :level {k {:column :nosuch}})))))))

  (testing "and by :text, on a layer type that draws one"
    ;; Not on `lay-point`: the point mark draws no label, so `:text`
    ;; is off its accept-list and the option is stripped with a
    ;; warning naming the layer types that do -- which is the more
    ;; useful answer than a missing column would be.
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Column :nosuch .* not found in dataset"
         (pj/plan (-> measured (pj/lay-text :when :level {:text {:column :nosuch}}))))))

  (testing "the positional aesthetics too"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Column :nosuch \(from :x\) not found in dataset"
         (pj/plan (-> measured (pj/pose {:x {:column :nosuch} :y :level}) pj/lay-point)))))

  (testing "and the drawn reading is not offered as the fix"
    ;; The writer has said which reading they meant, so \"it is not a
    ;; color either\" answers a question they did not ask.
    (is (not (re-find #"not a color either"
                      (try (pj/plan (-> measured (pj/lay-point :when :level
                                                               {:color {:column :nosuch}})))
                           ""
                           (catch clojure.lang.ExceptionInfo e (.getMessage e)))))))

  (testing "a column that is there still passes"
    (is (pj/plan (-> measured (pj/lay-point :when :level {:color {:column :variety}}))))))

(deftest an-explicit-value-reports-what-it-is-not-test
  ;; `{:value :sphere}` says the value is not a column reference, so
  ;; listing the columns available reads as a contradiction -- the
  ;; message named a column that was right there in the list. Shown on
  ;; `:shape`, whose vocabulary is the seven symbols: a value outside a
  ;; closed vocabulary is a mistake, where a value outside `:color`'s
  ;; open one is read as data.
  (let [msg (try (pj/plan (-> measured (pj/lay-point :when :level
                                                     {:shape {:value :sphere}})))
                 ""
                 (catch clojure.lang.ExceptionInfo e (.getMessage e)))]
    (is (re-find #":shape \{:value :sphere\} is not a symbol" msg))
    (is (not (re-find #"Available" msg))
        "no column list, since the column reading was declined")))

(deftest a-scale-on-an-aesthetic-with-none-is-reported-test
  ;; `impl.aesthetics/scaled?` defers the report here rather than
  ;; ignoring the key, which would leave a writer believing they had
  ;; changed something.
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #":group .* has no scale to set"
       (pj/plan (-> measured (pj/lay-point :when :level
                                           {:group {:column :variety :scale false}})))))
  (testing "the same mapping without a :scale passes"
    (is (pj/plan (-> measured (pj/lay-point :when :level
                                            {:group {:column :variety}}))))))

(defn domains
  "The panel's two domains, for the layers a pose draws."
  [pose]
  (-> pose pj/plan :panels first (select-keys [:x-domain :y-domain])))

(deftest an-unscaled-text-mark-informs-no-domain-test
  ;; Two places leave a drawing-space layer out of the domains: the
  ;; collection in `resolve-panel-domains` and the widening that makes
  ;; room for text. The per-axis form reached only the first, so a label
  ;; at 50 drawing units stretched the axis to 54 data units while the
  ;; whole-layer `:in :drawing-area` beside it did not.
  (let [scatter (-> measured (pj/lay-point :when :level))
        plain   (domains scatter)]
    (is (= plain (domains (-> scatter (pj/lay-text {:x 50 :y 20 :text "note"
                                                    :in :drawing-area}))))
        "the whole-layer form, for comparison")
    (is (= plain (domains (-> scatter (pj/lay-text {:x {:value 50 :scale false}
                                                    :y {:value 20 :scale false}
                                                    :text "note"})))))
    (testing "and one axis at a time"
      ;; x still names a column here, so its domain widens to fit the
      ;; label as it always has. Only y is out.
      (let [one-axis (domains (-> scatter (pj/lay-text {:x :when
                                                        :y {:value 20 :scale false}
                                                        :text "note"})))]
        (is (= (:y-domain plain) (:y-domain one-axis)))
        (is (<= (second (:x-domain plain)) (second (:x-domain one-axis)))))))

  (testing "a text mark in the data still widens the domain to fit"
    (let [scatter (-> measured (pj/lay-point :when :level))]
      (is (< (second (:x-domain (domains scatter)))
             (second (:x-domain (domains (-> scatter (pj/lay-text
                                                      {:x 4 :y 4
                                                       :text "a very long label indeed"}))))))))))

(deftest an-unscaled-axis-needs-a-numeric-column-test
  ;; Drawing units are numbers. A category column told not to scale
  ;; reached the renderer and died on `String cannot be cast to
  ;; Number`, which is the error this work replaced for `:size` and
  ;; `:alpha`.
  (doseq [k [:x :y]]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"was given :scale false.*holds categorical values"
         (pj/plan (-> measured (pj/lay-point :when :level
                                             {k {:column :variety :scale false}}))))))
  (testing "a numeric column is placed in drawing units as asked"
    (is (pj/plan (-> measured (pj/lay-point :when :level
                                            {:y {:column :level :scale false}})))))
  (testing "and a category column through its scale is untouched"
    (is (pj/plan (-> measured (pj/lay-point :variety :level))))))

(deftest an-unscaled-axis-refuses-a-coord-that-moves-it-test
  ;; An unscaled axis is a distance from the panel background's top
  ;; left, which names one screen direction. `:flip` swaps which one the
  ;; mapping's `:y` reaches and `:polar` gives it none, and the renderer
  ;; drew every mark into one corner rather than saying so. Refusing
  ;; leaves open what it should mean instead.
  (doseq [coord [:flip :polar]]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"cannot be combined"
         (pj/plan (-> measured
                      (pj/lay-point :when :level {:y {:column :level :scale false}})
                      (pj/coord coord))))))

  (testing "the coord alone, and an unscaled axis alone, both still pass"
    (is (pj/plan (-> measured (pj/lay-point :when :level) (pj/coord :flip))))
    (is (pj/plan (-> measured (pj/lay-point :when :level
                                            {:y {:column :level :scale false}})))))

  (testing "and the whole-layer form is unaffected by the coord"
    ;; Both of its coordinates are drawing units from the same corner,
    ;; so there is no axis to follow through the swap.
    (is (pj/plan (-> measured
                     (pj/lay-point :when :level)
                     (pj/lay-text {:x 50 :y 20 :text "note" :in :drawing-area})
                     (pj/coord :flip))))))

(deftest a-size-domain-of-one-value-maps-to-the-middle-test
  ;; With nothing to compare a value against, the midpoint of the output
  ;; range is the only unprejudiced answer -- ggplot2's
  ;; `scales::rescale` answers a zero range the same way. Collapsing to
  ;; the low end drew the datum cell at radius 2.0, smaller than the
  ;; default 3.0, beside a legend reading 7.
  (let [radius-of (fn [pose]
                    (let [p (pj/plan pose)]
                      {:marks (-> p :panels first :layers first :groups first :sizes vec)
                       :legend (mapv :magnitude (:entries (:size-legend p)))}))
        default-radius (-> (pj/plan (-> measured (pj/lay-point :when :level)))
                           :panels first :layers first :style :radius)]
    (testing "a written value sent through the scale"
      (is (= {:marks [7 7 7 7] :legend [5.0]}
             (radius-of (-> measured (pj/lay-point :when :level
                                                   {:size {:value 7 :scale true}})))))
      (is (< default-radius 5.0)
          "and larger than the default, not smaller"))

    (testing "a column whose values happen to be equal, for the same reason"
      (is (= {:marks [5 5 5 5] :legend [5.0]}
             (radius-of (-> (assoc measured :flat [5 5 5 5])
                            (pj/lay-point :when :level {:size :flat}))))))

    (testing "a column with spread is untouched"
      ;; The marks and the legend read the same function, so the mark
      ;; drawn beside a value in the legend is the size a mark of that
      ;; value is drawn at on the panel.
      (let [{:keys [legend]} (radius-of (-> measured (pj/lay-point :when :level
                                                                   {:size :radius})))]
        (is (= [2.0 8.0] [(first legend) (last legend)]))
        (is (apply < legend))))))

(deftest a-degenerate-domain-is-decided-relatively-test
  ;; The midpoint rule needs a relative test, as `scales::zero_range`
  ;; has. With an absolute `(max 1e-6 span)` floor beneath an exact
  ;; equality test, a span of 1e-7 fell between the two and drew every
  ;; mark between radius 2.0 and 2.6 -- below the default 3.0, which is
  ;; the picture the midpoint rule was added to prevent.
  (let [radii (fn [span]
                (let [f (scale/channel-mapper {:type :linear} 5.0 (+ 5.0 span) [2.0 8.0] 2)]
                  [(double (f 5.0)) (double (f (+ 5.0 span)))]))]
    (testing "a span too small to be real takes the midpoint"
      (is (= [5.0 5.0] (radii 0.0)))
      (is (= [5.0 5.0] (radii 1e-13))))

    (testing "a span that is real takes the whole range, with no band beneath it"
      (doseq [span [1e-7 1e-6 1e-3 1.0 1000.0]]
        (is (= [2.0 8.0] (radii span)) (str "span " span))))))

(deftest a-size-legend-with-no-entries-still-renders-test
  ;; `log-ticks` finds no 1-2-5 tick in a constant log domain and
  ;; returns none, and the legend renderer reduced `max` over the empty
  ;; list. Newly reachable: the datum cell puts a constant column where
  ;; v0.8.1's schema refused one.
  (is (instance? java.awt.image.BufferedImage
                 (pj/plot (-> {:a [1 2 3] :b [1 2 3]}
                              (pj/lay-point :a :b {:size {:value 7 :scale true}})
                              (pj/scale :size {:type :log}))
                          {:format :bufimg}))))

(deftest a-column-argument-reads-the-explicit-form-test
  ;; `pj/facet` names a column and has no second reading, so the form
  ;; has no work to do there -- but a writer who has learned it for
  ;; mappings reaches for it, and the map used to be stashed whole:
  ;; one unfaceted panel, in silence, while a missing column reported
  ;; correctly.
  (let [d {:when [1 2 3 4] :level [1 2 3 4] :variety ["p" "q" "p" "q"]}
        panels (fn [pose] (count (:panels (pj/plan pose))))]
    (is (= 2 (panels (-> d (pj/lay-point :when :level) (pj/facet :variety)))))
    (is (= 2 (panels (-> d (pj/lay-point :when :level) (pj/facet {:column :variety})))))

    (testing "and a map with no column reading is reported"
      (doseq [m [{:value :variety} {:column :variety :scale false}]]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo #"takes a column of the data"
             (-> d (pj/lay-point :when :level) (pj/facet m))))))))

(deftest a-datum-broadcasts-on-a-layer-of-values-too-test
  ;; A layer whose x and y are both written values gets a one-row
  ;; dataset synthesized for it. Building that row from the positional
  ;; values alone left the scaled ones out, so the cell that works
  ;; beside a column reported a column missing from a dataset the writer
  ;; never wrote.
  (let [scatter (-> measured (pj/lay-point :when :level))]
    (testing "a written color asked to scale earns its legend entry"
      (let [layer (-> scatter
                      (pj/lay-text {:x 2 :y 3 :text "here" :color {:value "Model A" :scale true}})
                      pj/plan :panels first :layers last)]
        (is (= ["Model A"] (mapv :label (:groups layer))))))

    (testing "a written size asked to scale reaches the size buffer"
      (let [layer (-> scatter
                      (pj/lay-point {:x 2 :y 3 :size {:value 7 :scale true}})
                      pj/plan :panels first :layers last)]
        (is (= [[7]] (mapv (comp vec :sizes) (:groups layer)))
            "a column of one distinct value, not a fixed radius")))))

(deftest an-aesthetic-that-takes-no-value-still-takes-none-test
  ;; `drawn-value-schemas` states what a value on `:fill` *would* mean;
  ;; the registry's `:value?` says whether one is accepted. Asking only
  ;; the first let `{:fill "red"}` past, and the tile drew the default
  ;; blue in silence -- the case the design note said not to open, and
  ;; reached without anyone flipping `:value?`. Only a value that
  ;; happens to name a color was affected.
  (let [tiles {:when [1 2 1 2] :level [1 1 2 2] :heat [1 2 3 4]}]
    (doseq [v ["red" :red]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"not found in dataset"
           (pj/plan (-> tiles (pj/lay-tile :when :level {:fill v}))))
          (str "a written " (pr-str v) " on :fill")))

    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":fill .* has no reading for a value"
         (pj/plan (-> tiles (pj/lay-tile :when :level {:fill {:value "red"}})))))

    (testing "and the column it does take is unaffected"
      (is (= 4 (count (distinct (map :color (-> (pj/plan (-> tiles (pj/lay-tile :when :level {:fill :heat})))
                                                :panels first :layers first :tiles)))))))))

(deftest a-drawn-column-holds-what-the-aesthetic-can-draw-test
  ;; `:size` and `:alpha` decide by source, and the check for what a
  ;; `:scale false` column may hold covered `:always` and `:by-value`
  ;; only -- so the cell this release opens skipped it. A radius column
  ;; holding a negative drew nothing for that row: the plan counted the
  ;; mark, `svg-summary` counted it, and the picture was short.
  ;; `{:size -4}` was refused all along.
  (let [d (assoc measured :negative [-4 8 -12 16] :over [5 10 2 8])]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"was given :scale false.*is not one :size can draw"
         (pj/plan (-> d (pj/lay-point :when :level {:size {:column :negative :scale false}})))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"was given :scale false.*is not one :alpha can draw"
         (pj/plan (-> d (pj/lay-point :when :level {:alpha {:column :over :scale false}})))))
    (testing "a column it can draw still passes"
      (is (pj/plan (-> d (pj/lay-point :when :level {:size {:column :radius :scale false}})))))))

(deftest the-scale-key-names-a-scale-test
  ;; `:scale` says which side of the scale a value passes -- true or
  ;; false -- and may say which scale, as a type or a whole spec.
  (testing "a value that is neither is refused"
    (doseq [v ["false" 0]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"sets :scale to .*A mapping's :scale is true or false"
           (pj/plan (-> measured (pj/lay-point :when :level
                                               {:size {:column :radius :scale v}}))))
          (str "a :scale of " (pr-str v)))))

  (testing "and so is a type the channel has no scale for"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"sets :scale type :bogus, and :size has no such scale"
         (pj/plan (-> measured (pj/lay-point :when :level
                                             {:size {:column :radius :scale :bogus}}))))))

  (testing "true, false, absent, a type and a spec are the vocabulary"
    (doseq [m [{:size {:column :radius :scale true}}
               {:size {:column :radius :scale false}}
               {:size {:column :radius :scale nil}}
               {:size {:column :radius :scale :log}}
               {:size {:column :radius :scale {:type :log :range [1 12]}}}
               {:size {:column :radius}}]]
      (is (pj/plan (-> measured (pj/lay-point :when :level m))))))

  (testing "a type in the mapping reads that one mapping through it"
    ;; The reading the writer of it expects, and the one that used to be
    ;; refused because nothing carried it out.
    (let [radii (fn [m] (->> (pj/plan (-> measured (pj/lay-point :when :level m)))
                             :size-legend :entries (mapv :magnitude)))]
      (is (not= (radii {:size {:column :radius :scale :log}})
                (radii {:size {:column :radius :scale :linear}})))))

  (testing "and a mapping's :scale true is not an opinion about which scale"
    ;; It says which side of the scale the value passes, which is a
    ;; different question. So the pose's type still decides.
    (let [radii (fn [pose] (->> (pj/plan pose) :size-legend :entries (mapv :magnitude)))]
      (is (= (radii (-> measured
                        (pj/lay-point :when :level {:size {:column :radius :scale true}})
                        (pj/scale :size :log)))
             (radii (-> measured
                        (pj/lay-point :when :level {:size :radius})
                        (pj/scale :size :log))))))))

(deftest a-source-named-as-nil-is-refused-test
  ;; Both slipped past the checks below, which skip a nil: a nil
  ;; `:value` broadcast a column of nils and drew an empty panel
  ;; reading "no data", and a nil `:column` reached `pj/plan` and died
  ;; on a schema error.
  (doseq [m [{:x {:value nil} :y :level} {:x {:column nil} :y :level}]]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"names :(value|column) nil"
         (pj/plan (-> measured (pj/lay-point m))))))

  (testing "a bare nil still cancels an inherited mapping"
    (is (pj/plan (-> measured (pj/pose {:x :when :y :level :color :variety})
                     (pj/lay-point {:color nil}))))))

(deftest a-datum-does-not-overwrite-a-column-of-its-own-name-test
  ;; The synthesized constant takes the aesthetic's own name, which is
  ;; what titles the axis and the legend. Where the data already
  ;; carries that name it replaced the data:
  ;; `(pj/lay-point :when :size {:size {:value 7 :scale true}})` drew
  ;; every mark at y=7 on an axis still labelled `size`. Mapping a
  ;; coordinate to a column named after an aesthetic is ordinary.
  (let [collides {:when [1 2 3] :size [9 20 31] :color ["p" "q" "p"]}
        y-domain (fn [pose] (-> pose pj/plan :panels first :y-domain))]
    (is (= (y-domain (-> collides (pj/lay-point :when :size)))
           (y-domain (-> collides (pj/lay-point :when :size
                                                {:size {:value 7 :scale true}}))))
        "the data column still decides the y domain")
    (is (= (y-domain (-> collides (pj/lay-point :when :color)))
           (y-domain (-> collides (pj/lay-point :when :color
                                                {:color {:value "Model A" :scale true}}))))))

  (testing "and the datum still reaches its own aesthetic"
    (is (= ["Model A"]
           (->> (pj/plan (-> {:when [1 2 3] :color ["p" "q" "p"]}
                             (pj/lay-point :when :color
                                           {:color {:value "Model A" :scale true}})))
                :panels first :layers first :groups (mapv :label))))))

(deftest a-column-drawn-as-it-stands-needs-a-reading-that-exists-test
  ;; `:scale-default` says what a reading would mean; `:drawn-column?`
  ;; says whether it is written. `:shape` carries `:by-value` and no
  ;; reading, so `scaled?` answered `false` for a column of symbols
  ;; while `collect-shapes` assigned symbols by category order anyway:
  ;; a column holding `:cross` drew a circle under a legend labelling
  ;; that circle "cross". Backlogged rather than implemented, so the
  ;; request is refused meanwhile.
  (let [symbols {:when [1 2 3] :level [1 2 3] :sym [:cross :square :circle]}]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":shape .* not a reading Plotje has yet"
         (pj/plan (-> symbols (pj/lay-point :when :level
                                            {:shape {:column :sym :scale false}})))))
    (testing "the column still scales, which is what it did all along"
      (is (pj/plan (-> symbols (pj/lay-point :when :level {:shape :sym}))))))

  (testing "the aesthetics whose reading is written are unaffected"
    (is (pj/plan (-> (assoc measured :hexes ["#FF0000" "#00FF00" "#0000FF" "#FF00FF"])
                     (pj/lay-point :when :level {:color {:column :hexes :scale false}}))))
    (is (pj/plan (-> measured (pj/lay-point :when :level
                                            {:size {:column :radius :scale false}}))))
    (is (pj/plan (-> measured (pj/lay-point :when :level
                                            {:y {:column :level :scale false}}))))))

(deftest an-aesthetic-with-no-scale-refuses-one-test
  ;; `:group` has no scale at all and `:text` has a reading but none,
  ;; and the mapping gate refused only the first. So `{:text {:value
  ;; "hi" :scale true}}` was accepted and the key dropped -- a writer
  ;; told nothing, having changed nothing, which is the case the
  ;; refusal exists for. `scaled?` answered `true` for it meanwhile, an
  ;; internal claim nothing downstream could act on.
  (let [msg #"has no scale to set"]
    (testing ":text refuses a scale, by either spelling and either value"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/lay-text measured :when :level
                                         {:text {:value "hi" :scale true}})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/lay-text measured :when :level
                                         {:text {:column :variety :scale false}}))))
    (testing ":group refuses one under the same reasoning"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/lay-point measured :when :level
                                          {:group {:column :variety :scale true}}))))
    (testing "the mapping without a :scale is untouched"
      (is (= ["olive" "plum" "olive" "plum"]
             (->> (pj/plan (pj/lay-text measured :when :level {:text :variety}))
                  :panels first :layers first :groups first :labels vec)))
      (is (= ["hi" "hi" "hi" "hi"]
             (->> (pj/plan (pj/lay-text measured :when :level {:text "hi"}))
                  :panels first :layers first :groups first :labels vec))))
    (testing "and scaled? says so, whatever the mapping asks for"
      (is (not (aes/scaled? :text {:source :value :scale true})))
      (is (not (aes/scaled? :text {:source :column :scale true})))
      (is (not (aes/scaled? :group {:source :column :scale true}))))))

(deftest an-explicit-value-wins-over-a-column-of-that-name-test
  ;; Settling this collision is what the explicit form is for. Every
  ;; reading has to consult the writer's choice for that to hold, and
  ;; `:shape` asked the data alone -- so `{:value :circle}` on data
  ;; carrying a column called `:circle` drew one symbol per category.
  (let [collides {:when [1 2 3 4] :level [1 4 2 3] :circle ["p" "q" "p" "q"]}
        layer (fn [m] (-> collides (pj/lay-point :when :level m)
                          pj/plan :panels first :layers first))]
    (is (= :circle (:shape (:style (layer {:shape {:value :circle}}))))
        "one symbol for the whole layer")
    (is (nil? (:shape-map (layer {:shape {:value :circle}}))))
    (is (some? (:shape-map (layer {:shape {:column :circle}})))
        "and the column reading is still reachable")))

(deftest a-drawn-color-column-takes-no-palette-slot-test
  ;; A drawn column holds colors, not categories. Counted among the
  ;; categories it took palette slots from the scaled layer beside it,
  ;; so that layer drew its groups in the wrong colors, and it earned
  ;; legend rows pairing a hex code with a palette color that drew
  ;; nothing.
  (let [scaled-alone (-> measured (pj/lay-line :when :level {:color :variety}) pj/plan)
        mixed (-> measured
                  (pj/lay-point :when :level {:color {:column :hex :scale false}})
                  (pj/lay-line {:color :variety})
                  pj/plan)]
    (is (= (:legend scaled-alone) (:legend mixed))
        "the legend explains the scaled column and only it")
    (is (= (mapv :color (-> scaled-alone :panels first :layers first :groups))
           (mapv :color (-> mixed :panels first :layers second :groups)))
        "and the palette it draws from is unshifted")
    (is (= [[1.0 0.0 0.0 1.0] [0.0 1.0 0.0 1.0] [0.0 0.0 1.0 1.0] [1.0 0.0 1.0 1.0]]
           (mapv :color (-> mixed :panels first :layers first :groups)))
        "while the drawn column keeps drawing its own colors")))

(deftest the-form-is-checked-where-it-is-written-test
  ;; What the map says is decided by the map, so a malformed one is
  ;; visible at the call. Left to `pj/draft`, the pose was built,
  ;; threaded and composed before anything said the mapping was wrong,
  ;; and the same mistake surfaced at two different moments depending
  ;; on how it was spelled.
  (let [built (fn [m] (pj/lay-point measured :when :level m))]
    (testing "a key the form does not have"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unexpected key"
                            (built {:color {:column :variety :typo 1}})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unexpected key"
                            (pj/pose measured {:x :when :y :level
                                               :color {:column :variety :typo 1}}))))
    (testing "both sources, a scale type the channel lacks, a source named nil"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names both :column and :value"
                            (built {:color {:column :variety :value "red"}})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"has no such scale"
                            (built {:size {:column :radius :scale :categorical}})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names :column nil"
                            (built {:color {:column nil}}))))
    (testing "a map naming no source at all"
      ;; `{:scale false}` is not the form -- it says which side of the
      ;; scale without saying of what -- so it used to reach the column
      ;; lookup whole and be reported as a column called `{:scale false}`.
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names no source"
                            (built {:color {:scale false}})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names no source"
                            (built {:color {}}))))
    (testing "a symbol inside the form gets the same help as one outside"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"did you mean :variety"
                            (built {:color 'variety})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"did you mean :variety"
                            (built {:color {:column 'variety}}))))
    (testing "and a well-formed one is still built"
      (is (some? (pj/plan (built {:color {:column :variety}})))))))

(deftest a-mapping-cannot-be-written-in-a-map-slot-test
  ;; A mapping written in full names one aesthetic's reading, so it
  ;; belongs under an aesthetic key. Both map slots hold the aesthetics
  ;; themselves: a `lay-*` call reads a map in its last positional
  ;; argument as the options map, whichever axis that argument stands
  ;; in. So `(pj/lay-point data :a {:column :b})` did not say `:y`: the
  ;; map was the options, `:column` was warned about and stripped, and
  ;; a one-dimensional plot came back from a call that reads as a
  ;; two-dimensional one.
  (let [two {:when [1 2 3] :level [4 5 6]}
        msg #"so there is no aesthetic for it to name"]
    (testing "a lay-* call's last positional argument"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/lay-point two :when {:column :level})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/lay-point two {:column :when})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/lay-text (pj/pose two :when :level) {:value "hi"}))))
    (testing "and pj/pose's mapping map, at every arity that takes one"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/pose two {:column :when})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/pose two :when {:column :level})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/pose two :when :level {:column :level}))))
    (testing "the slots that do take it are unaffected"
      (is (= {:x {:column :when} :y :level}
             (:mapping (pj/lay-point two {:column :when} :level))))
      (is (= {:x :when :y {:column :level}}
             (:mapping (pj/lay-point two :when {:column :level} {:color :when}))))
      (is (= {:x {:column :when} :y :level}
             (:mapping (pj/pose two {:x {:column :when} :y :level})))))
    (testing "and an ordinary options map is untouched"
      (is (= {:x :when :y :level} (:mapping (pj/lay-point two :when :level {:alpha 0.5}))))
      (is (= {:color :when :x :when :y :level}
             (:mapping (pj/pose two :when :level {:color :when})))))))

(deftest a-band-bound-takes-the-written-form-test
  ;; `:x-min` and `:x-max` carry `:value? true` and `:column? false`, so
  ;; a written value is the only reading they have -- and it was the one
  ;; the notation could not reach. `{:value 1.5}` fell through to the
  ;; finite-number check, which answered that it was not a number.
  (let [base (pj/lay-point {:when [1 2 3] :level [10 20 30]} :when :level)
        layers (fn [p] (count (:layers (first (:panels (pj/plan p))))))]
    (is (= (layers (pj/lay-band-h base {:y-min 12 :y-max 18}))
           (layers (pj/lay-band-h base {:y-min {:value 12} :y-max {:value 18}}))))
    (is (= (layers (pj/lay-band-v base {:x-min 1.5 :x-max 2.5}))
           (layers (pj/lay-band-v base {:x-min {:value 1.5} :x-max {:value 2.5}}))))
    (is (= (layers (pj/lay-rule-v base {:x-intercept 2}))
           (layers (pj/lay-rule-v base {:x-intercept {:value 2}}))))
    (testing "a column, or a scale, is refused by name rather than as a non-number"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no column for \{:column \.\.\.\} to name"
                            (pj/lay-band-h base {:y-min {:column :lo} :y-max 18})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no scale for :scale to choose"
                            (pj/lay-band-h base {:y-min {:value 12 :scale false} :y-max 18}))))
    (testing "while the same keys on an errorbar still read a column"
      (is (some? (pj/plan (pj/lay-errorbar {:when [1 2] :level [2 3] :lo [1 2] :hi [3 4]}
                                           :when :level
                                           {:y-min {:column :lo} :y-max {:column :hi}})))))))

(deftest with-data-checks-the-explicit-column-test
  ;; `{:column :typo}` is a column reference written more explicitly
  ;; than the plain `:typo`, and the attach-time check read only the
  ;; plain one -- so the template attached and failed later, at
  ;; `pj/plan`, with a different message.
  (let [d {:when [1 2 3] :level [4 5 6]}]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Cannot attach data"
                          (-> (pj/pose) (pj/pose :when :typo) pj/lay-point (pj/with-data d))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Cannot attach data"
                          (-> (pj/pose) (pj/pose {:x {:column :when} :y {:column :typo}})
                              pj/lay-point (pj/with-data d))))
    (testing "a written value names no column, so it is not looked for"
      (is (some? (-> (pj/pose) (pj/pose {:x {:column :when} :y {:value 5}})
                     pj/lay-point (pj/with-data d) pj/plan))))))

(def per-row
  "A layer's data for the per-row-channel checks. `:rad` holds valid
   radii and `:op` valid opacities, so nothing below turns on the
   values being undrawable."
  {:when [1 2 3 4] :level [1 4 2 3] :rad [3 8 14 20]
   :op [0.2 0.4 0.6 0.9] :variety ["olive" "plum" "olive" "plum"]})

(deftest a-channel-the-mark-draws-once-refuses-a-drawn-column-test
  ;; `{:size {:column :r :scale false}}` asks for the column's own
  ;; values as radii. A mark that draws one radius for the layer has
  ;; none to give, and answered by drawing exactly what it drew before:
  ;; `pj/save` of a lollipop with and without the option produced
  ;; byte-identical PNGs. The appearance twin of the per-axis refusal.
  (let [msg #"draws one (size|alpha) for the whole layer"]
    (doseq [[lay-fn nm] [[pj/lay-line "line"] [pj/lay-step "step"]
                         [pj/lay-boxplot "boxplot"] [pj/lay-violin "violin"]
                         [pj/lay-lollipop "lollipop"] [pj/lay-summary "summary"]]]
      (testing (str nm " refuses a drawn :size column")
        (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                              (pj/plan (lay-fn per-row :variety :level
                                               {:size {:column :rad :scale false}})))))
      (testing (str nm " refuses a drawn :alpha column")
        (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                              (pj/plan (lay-fn per-row :variety :level
                                               {:alpha {:column :op :scale false}}))))))

    (testing "the message names the mark that can"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"The marks that can: :point"
                            (pj/plan (pj/lay-line per-row :when :level
                                                  {:size {:column :rad :scale false}})))))

    (testing "and :point, which reads them, is untouched"
      (let [layer (-> (pj/lay-point per-row :when :level
                                    {:size {:column :rad :scale false}
                                     :alpha {:column :op :scale false}})
                      pj/plan :panels first :layers first)]
        (is (true? (:size-drawn? layer)))
        (is (true? (:alpha-drawn? layer)))
        (is (= [3 8 14 20] (vec (:sizes (first (:groups layer))))))
        (is (= [0.2 0.4 0.6 0.9] (vec (:alphas (first (:groups layer))))))))

    (testing "a written value over the whole layer is what those marks take"
      (is (= 5 (:stroke-width (:style (-> (pj/lay-line per-row :when :level {:size 5})
                                          pj/plan :panels first :layers first))))))))

(deftest no-legend-for-a-channel-no-mark-varies-test
  ;; The scaled spelling has been accepted since before 0.8.1, so it is
  ;; warned rather than refused -- but it must not stay silent: the
  ;; layer ignored the column and the plot grew a legend for it, so the
  ;; picture advertised an encoding it did not contain.
  (testing "a lone line layer earns no size or alpha legend"
    (is (nil? (:size-legend (pj/plan (pj/lay-line per-row :when :level {:size :rad})))))
    (is (nil? (:alpha-legend (pj/plan (pj/lay-line per-row :when :level {:alpha :op}))))))

  (testing "a point layer still earns one"
    (is (some? (:size-legend (pj/plan (pj/lay-point per-row :when :level {:size :rad})))))
    (is (some? (:alpha-legend (pj/plan (pj/lay-point per-row :when :level {:alpha :op}))))))

  (testing "and a point layer beside a line layer keeps it -- the case the channel exists for"
    (is (some? (:size-legend (-> (pj/pose per-row :when :level {:size :rad})
                                 pj/lay-point pj/lay-line pj/plan)))))

  (testing ":text on a point layer is reported by location, as :shape always was"
    ;; The point mark draws no label, so `:text` is off its accept-list
    ;; and the universal warning names the layer types that draw one.
    (is (nil? (:text (:mapping (first (:layers (pj/lay-point per-row :when :level
                                                             {:text "note"})))))))
    (is (= ["note" "note" "note" "note"]
           (vec (:labels (first (:groups (-> (pj/lay-text per-row :when :level {:text "note"})
                                             pj/plan :panels first :layers first)))))))))

(deftest an-unscaled-channel-refuses-a-scale-option-test
  ;; A column drawn as it stands passes through no scale, so an option
  ;; configuring that scale has nothing to configure. Both spellings
  ;; rendered identically to the option being absent.
  (let [hexes (assoc per-row :hex ["#FF0000" "#00FF00" "#0000FF" "#FF00FF"])
        msg #"configures? the scale it just left"]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                          (pj/plan (pj/lay-point hexes :when :level
                                                 {:color {:column :hex :scale false}
                                                  :color-type :categorical}))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                          (pj/plan (-> (pj/lay-point hexes :when :level
                                                     {:color {:column :hex :scale false}})
                                       (pj/scale :color {:domain ["#FF0000"]})))))
    (testing "every channel with a scale, not only :color"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/plan (-> (pj/lay-point per-row {:x :when
                                                                :y {:column :level :scale false}})
                                         (pj/scale :y {:type :log})))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo msg
                            (pj/plan (-> (pj/lay-point per-row :when :level
                                                       {:size {:column :rad :scale false}})
                                         (pj/scale :size {:domain [0 20]}))))))
    (testing "the unscaled column on its own is untouched"
      (is (= [[1.0 0.0 0.0 1.0] [0.0 1.0 0.0 1.0] [0.0 0.0 1.0 1.0] [1.0 0.0 1.0 1.0]]
             (mapv :color (-> (pj/lay-point hexes :when :level
                                            {:color {:column :hex :scale false}})
                              pj/plan :panels first :layers first :groups)))))
    (testing "and so is the scaled reading, which is what the option is for"
      (is (some? (pj/plan (pj/lay-point hexes :when :level
                                        {:color {:column :hex :scale true}
                                         :color-type :categorical})))))))

(deftest an-axis-nothing-informs-draws-no-scale-test
  ;; The `[0 1]` fallback is a coordinate function, not a meaning.
  ;; Ticks drawn off it named values no mark carries: y values of 10,
  ;; 50 and 90 drawing units under an axis reading 0.0 to 1.0 labelled
  ;; `b`, beside a real x axis, with nothing about the picture looking
  ;; wrong.
  (let [d {:a [1 2 3] :b [10 50 90]}
        ticks (fn [p axis] (count (:labels (get (first (:panels p)) axis))))]
    (testing "a per-axis drawn y loses its ticks and its label, and x keeps both"
      (let [p (pj/plan (pj/lay-point d {:x :a :y {:column :b :scale false}}))]
        (is (zero? (ticks p :y-ticks)))
        (is (nil? (:y-label p)))
        (is (pos? (ticks p :x-ticks)))
        (is (= "a" (:x-label p)))))

    (testing "a whole layer in the drawing area loses both"
      (let [p (pj/plan (pj/lay-point d :a :b {:in :drawing-area}))]
        (is (zero? (ticks p :x-ticks)))
        (is (zero? (ticks p :y-ticks)))
        (is (nil? (:x-label p)))
        (is (nil? (:y-label p)))))

    (testing "a layer placed in the data gives the axes their meaning back"
      ;; The usual shape: a note on the panel beside a scatter.
      (let [p (-> (pj/lay-point d :a :b)
                  (pj/lay-text {:x 10 :y 10 :text "n" :in :drawing-area})
                  pj/plan)]
        (is (pos? (ticks p :x-ticks)))
        (is (pos? (ticks p :y-ticks)))
        (is (= "a" (:x-label p)))))

    (testing "and a domain the writer set counts as a meaning"
      (let [p (-> (pj/lay-point d :a :b {:in :drawing-area})
                  (pj/scale :x {:domain [0 100]})
                  pj/plan)]
        (is (pos? (ticks p :x-ticks)))
        (is (= "a" (:x-label p)))))

    (testing "an ordinary plot is untouched"
      (let [p (pj/plan (pj/lay-point d :a :b))]
        (is (pos? (ticks p :x-ticks)))
        (is (pos? (ticks p :y-ticks)))
        (is (= ["a" "b"] [(:x-label p) (:y-label p)]))))))
