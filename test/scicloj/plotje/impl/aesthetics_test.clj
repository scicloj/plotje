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
            [scicloj.plotje.impl.pose-schema :as pose-schema]))

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

(deftest color-decides-by-value-test
  (testing "a written color is drawn, a written non-color is a datum"
    (is (not (aes/scaled? :color {:source :value :value "red"})))
    (is (not (aes/scaled? :color {:source :value :value :steelblue})))
    ;; ggplot2's aes(colour="Model A"): a labelled series, scaled, with
    ;; a legend entry of its own.
    (is (aes/scaled? :color {:source :value :value "Model A"})))

  (testing "a column of categories is scaled -- the palette"
    (is (aes/scaled? :color {:source :column :value :species
                             :column-values ["setosa" "virginica" "setosa"]})))

  (testing "a column of colors is drawn -- the identity mapping"
    ;; The measured surprise, fixed: these used to become three
    ;; categories and draw palette colors instead of themselves.
    (is (not (aes/scaled? :color {:source :column :value :hexes
                                  :column-values ["#FF0000" "#00FF00" "#0000FF"]})))))

(deftest the-column-scan-needs-every-value-test
  (testing "one non-color makes the whole column categorical"
    (is (aes/scaled? :color {:source :column :value :c
                             :column-values ["#FF0000" "setosa" "#0000FF"]})))

  (testing "missing values are skipped, not counted against it"
    (is (not (aes/scaled? :color {:source :column :value :c
                                  :column-values ["#FF0000" nil "#0000FF"]}))))

  (testing "a column with nothing to read is scaled"
    ;; Vacuous agreement is not evidence.
    (is (aes/scaled? :color {:source :column :value :c :column-values []}))
    (is (aes/scaled? :color {:source :column :value :c :column-values [nil nil]})))

  (testing "the residual risk, pinned rather than reasoned about"
    ;; A column whose values all happen to name colors stops being
    ;; categorical. `:scale true` is the escape.
    (let [produce ["olive" "plum" "tomato"]]
      (is (not (aes/scaled? :color {:source :column :value :variety
                                    :column-values produce})))
      (is (aes/scaled? :color {:source :column :value :variety
                               :column-values produce :scale true}))))

  (testing "the cases that do not trip it"
    (doseq [vs [["setosa" "virginica"]
                ["salmon" "tuna" "cod"]
                ["brown" "blue" "green" "hazel"]
                ["cat" "dog" "fox"]]]
      (is (aes/scaled? :color {:source :column :value :c :column-values vs})
          (pr-str vs)))))

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
