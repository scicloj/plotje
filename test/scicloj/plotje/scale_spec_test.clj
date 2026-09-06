(ns scicloj.plotje.scale-spec-test
  "A scale spec says which scale an aesthetic is read through and how a
   value spreads across what the mark draws it as: `:type`, `:domain`,
   `:range`, `:by` and `:from-zero`. The same spec can be written on
   the pose with `pj/scale` or in the mapping itself."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.layer-type :as layer-type]
            [scicloj.plotje.impl.extract :as extract]
            [scicloj.plotje.render.mark :as mark]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

(def measured
  {:when [1 2 3 4]
   :level [10 20 30 40]
   :radius [4 8 12 16]})

(defn magnitudes
  "The sizes the legend pairs with its values -- the same function the
   marks are drawn from, so what the legend says is what the panel
   does."
  [pose]
  (->> (pj/plan pose) :size-legend :entries (mapv :magnitude)))

(defn round3 [x] (/ (Math/round (* 1000.0 (double x))) 1000.0))

;; ---- The three ways a value can spread ----

(deftest by-methods-reproduce-their-definitions-test
  ;; Measured against ggplot2 4.0.0 with values 10, 15, 20 and a range
  ;; of 1 to 6: `scale_size` gives 1, 4.536, 6; `scale_size_area` gives
  ;; 4.243, 5.196, 6; `scale_radius` gives 1, 3.5, 6.
  (let [at (fn [spec] (mapv #(round3 ((scale/channel-mapper spec 10 20 [1 6] 2) %))
                            [10 15 20]))]
    (testing ":sqrt is ggplot2's scale_size, and the default"
      (is (= [1.0 4.536 6.0] (at {:by :sqrt})))
      (is (= [1.0 4.536 6.0] (at {}))))

    (testing ":linear is ggplot2's scale_radius -- the radius carries the value"
      (is (= [1.0 3.5 6.0] (at {:by :linear}))))

    (testing ":area spreads the ink evenly, which no ggplot2 scale does"
      ;; Equal steps in value are equal steps in area: 1, 18.5, 36.
      (let [radii (at {:by :area})]
        (is (= [1.0 4.301 6.0] radii))
        (is (< (Math/abs (- (- (Math/pow (second radii) 2)
                               (Math/pow (first radii) 2))
                            (- (Math/pow (last radii) 2)
                               (Math/pow (second radii) 2))))
               0.01))))

    (testing "and :from-zero with :area is ggplot2's scale_size_area"
      (is (= [4.243 5.196 6.0] (at {:by :area :from-zero true}))))

    (testing "anchored at zero, :sqrt and :area are one function"
      ;; Which is why ggplot2's default is area-proportional exactly
      ;; when its range starts at zero, and not otherwise.
      (is (= (at {:by :sqrt :from-zero true}) (at {:by :area :from-zero true}))))))

(deftest a-quantity-whose-ink-grows-linearly-has-one-method-test
  ;; A stroke's width, an opacity: there is no area to correct for, so
  ;; the three methods are the same function.
  (let [at (fn [spec] (mapv #(round3 ((scale/channel-mapper spec 10 20 [1 6] 1) %))
                            [10 15 20]))]
    (is (= (at {:by :linear}) (at {:by :sqrt}) (at {:by :area})))))

;; ---- The spec end to end ----

(deftest range-sets-what-the-channel-spans-test
  (testing "a size range is a radius in drawing units"
    (is (= [4.0 16.0]
           (let [ms (magnitudes (-> measured (pj/lay-point :when :level {:size :radius})
                                    (pj/scale :size {:range [4 16]})))]
             [(first ms) (last ms)]))))

  (testing "doubling the range doubles every mark"
    (is (= (magnitudes (-> measured (pj/lay-point :when :level {:size :radius})
                           (pj/scale :size {:range [4 16]})))
           (mapv #(* 2 %)
                 (magnitudes (-> measured (pj/lay-point :when :level {:size :radius})))))))

  (testing "an alpha range is an opacity"
    (is (= [0.5 1.0]
           (let [es (->> (-> measured (pj/lay-point :when :level {:alpha :radius})
                             (pj/scale :alpha {:range [0.5 1.0]})
                             pj/plan :alpha-legend :entries)
                         (mapv :alpha))]
             [(first es) (last es)])))))

(deftest a-domain-clamps-rather-than-drops-test
  ;; A `:domain` narrower than the data says what the reader should
  ;; compare, not which rows to draw -- and a dropped row leaves no
  ;; trace on the panel. Before this, the option validated and changed
  ;; nothing at all (issue #39).
  (let [f (scale/channel-mapper {:domain [8 12]} 4 16 [2 8] 2)]
    ;; 10 is halfway across the domain, so it takes the default
    ;; method's halfway radius; 4 and 16 are outside and take the ends.
    (is (= [2.0 2.0 6.243 8.0 8.0] (mapv #(round3 (f %)) [4 8 10 12 16])))))

(deftest from-zero-makes-the-size-a-proportion-test
  ;; Twice the value, twice the ink -- which needs both ends anchored
  ;; at zero, the domain's and the range's.
  (let [f (scale/channel-mapper {:by :area :from-zero true} 0 20 [2 8] 2)
        ink #(Math/pow (f %) 2)]
    (is (< (Math/abs (- (/ (ink 20) (ink 10)) 2.0)) 1e-9))
    (is (< (Math/abs (- (/ (ink 10) (ink 5)) 2.0)) 1e-9))))

;; ---- Where a spec can be written ----

(deftest a-mapping-scale-wins-over-the-poses-key-by-key-test
  (testing "a type in the mapping reads that mapping through it"
    (is (not= (magnitudes (-> measured (pj/lay-point :when :level
                                                     {:size {:column :radius :scale :log}})))
              (magnitudes (-> measured (pj/lay-point :when :level
                                                     {:size {:column :radius :scale :linear}}))))))

  (testing "and takes only the keys it names, leaving the rest of the pose's"
    ;; The pose sets a range, the mapping a type: the plot keeps both.
    ;; A range twice the default draws every mark twice as wide, and it
    ;; is still the mapping's log scale deciding where each one lands.
    (is (= (magnitudes (-> measured
                           (pj/lay-point :when :level
                                         {:size {:column :radius :scale :log}})
                           (pj/scale :size {:range [4 16]})))
           (mapv #(* 2 %)
                 (magnitudes (-> measured
                                 (pj/lay-point :when :level
                                               {:size {:column :radius :scale :log}})))))))

  (testing "true is not an opinion about which scale, so the pose still decides"
    (is (= (magnitudes (-> measured (pj/lay-point :when :level
                                                  {:size {:column :radius :scale true}})
                           (pj/scale :size :log)))
           (magnitudes (-> measured (pj/lay-point :when :level {:size :radius})
                           (pj/scale :size :log)))))))

;; ---- What is refused ----

(deftest an-option-the-channel-does-not-read-is-refused-test
  (testing "a range on an axis, which the plot's size decides"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":x reads no :range"
                          (-> measured (pj/lay-point :when :level)
                              (pj/scale :x {:range [1 2]})))))

  (testing "a spread method on an opacity, which has no quantity to spread"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":alpha reads no :by"
                          (-> measured (pj/lay-point :when :level)
                              (pj/scale :alpha {:by :area}))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":alpha reads no :by"
                          (pj/lay-point measured :when :level
                                        {:alpha {:column :radius :scale {:by :area}}}))))

  (testing "a method that is not one"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"not a method a value can spread by"
                          (-> measured (pj/lay-point :when :level)
                              (pj/scale :size {:by :cube})))))

  (testing "zero beside a log scale, which has no reading for it"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":from-zero beside a log scale"
                          (-> measured (pj/lay-point :when :level)
                              (pj/scale :size {:type :log :from-zero true}))))))

(deftest an-axis-takes-a-scale-in-its-mapping-test
  ;; `pj/scale` and a mapping's `:scale` speak one language, on every
  ;; channel that has a scale. The axes were refused a type at first,
  ;; on the reasoning that a panel has one of each -- but that is an
  ;; argument about layers disagreeing, not about the form.
  (let [wide {:when [1 10 100 1000] :level [1 2 3 4]}
        x-scale #(-> % pj/plan :panels first :x-scale)]
    (testing "a type in the mapping reaches the axis"
      (is (= {:type :log}
             (x-scale (pj/lay-point wide {:x {:column :when :scale :log}
                                          :y :level})))))

    (testing "and so does a whole spec, from the pose's mapping"
      (is (= {:type :log}
             (x-scale (-> wide
                          (pj/pose {:x {:column :when :scale {:type :log}}
                                    :y :level})
                          pj/lay-point)))))

    (testing "a spec naming no type still carries one, as the schema asks"
      (is (= {:type :linear :domain [1 2000]}
             (x-scale (pj/lay-point wide {:x {:column :when
                                              :scale {:domain [1 2000]}}
                                          :y :level})))))

    (testing "the axis takes the spec from whichever layer names it"
      ;; Not from the first layer, which may name none.
      (is (= {:type :log}
             (x-scale (-> wide
                          (pj/pose {:x :when :y :level})
                          pj/lay-point
                          (pj/lay-line {:x {:column :when :scale :log}}))))))

    (testing "and `:label` titles the axis from either spelling"
      (is (= "Custom" (-> wide (pj/lay-point :when :level)
                          (pj/scale :x {:label "Custom"})
                          pj/plan :x-label)))
      (is (= "Mapped" (-> wide (pj/lay-point {:x {:column :when
                                                  :scale {:label "Mapped"}}
                                              :y :level})
                          pj/plan :x-label))))

    (testing "`:label` and `:x-label` are one setting, and the innermost wins"
      ;; An axis label is the one place where a plot option and a scale
      ;; spec say the same thing. It resolves like every other scale
      ;; setting: the spec is written further in, so it wins, however
      ;; far out the option sits.
      (is (= "spec" (-> wide
                        (pj/pose {:x :when :y :level})
                        (pj/options {:x-label "option"})
                        (pj/lay-point {:x {:column :when
                                           :scale {:label "spec"}}})
                        pj/plan :x-label)))
      (is (= "spec y" (-> wide (pj/lay-point :when :level)
                          (pj/scale :y {:label "spec y"})
                          (pj/options {:y-label "option y"})
                          pj/plan :y-label)))
      ;; With no spec the option still titles the axis, and with
      ;; neither the column name is inferred.
      (is (= ["option" "level"]
             (-> wide (pj/lay-point :when :level)
                 (pj/options {:x-label "option"})
                 pj/plan
                 ((juxt :x-label :y-label))))))))

(deftest layers-that-name-different-axis-scales-are-refused-test
  ;; A panel has one x axis and every layer is drawn against it, so it
  ;; can carry one spec. A layer naming none is no disagreement -- that
  ;; is where axes differ from the appearance channels, where each
  ;; layer scales its own values.
  (let [wide {:when [1 10 100 1000] :level [1 2 3 4]}]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Layers name different scales for the :x axis"
         (pj/plan (-> wide
                      (pj/pose {:x :when :y :level})
                      (pj/lay-point {:x {:column :when :scale :log}})
                      (pj/lay-line {:x {:column :when :scale :linear}})))))

    (testing "a layer that names none goes along with the one that does"
      (is (pj/plan (-> wide
                       (pj/pose {:x :when :y :level})
                       pj/lay-point
                       (pj/lay-line {:x {:column :when :scale :log}})))))))

(deftest the-secondary-positional-aesthetics-have-no-scale-test
  ;; They are drawn through the panel's own axis. A `:scale` on one was
  ;; accepted and read by nothing.
  (doseq [k [:y-min :y-max]]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"has no scale to set"
                          (pj/lay-errorbar {:a [1 2] :b [3 4] :lo [1 2]}
                                           :a :b {k {:column :lo :scale false}}))
        (str "a :scale on " k))))

(deftest a-key-the-spec-does-not-have-is-refused-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unexpected key"
                        (pj/lay-point measured :when :level
                                      {:size {:column :radius :scale {:rnge [1 2]}}}))))

(deftest layers-that-disagree-about-a-channel-are-refused-test
  ;; One legend explains one scale, so a plot cannot carry two readings
  ;; of one channel and label both.
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"read :size through different scales"
       (pj/plan (-> measured
                    (pj/pose :when :level)
                    (pj/lay-point {:size {:column :radius :scale :log}})
                    (pj/lay-point {:size {:column :radius :scale :linear}}))))))

;; ---- Scale settings accumulate down the scope chain ----

(deftest scales-accumulate-wherever-they-are-written-test
  ;; A mapping states one source, and two sources cannot combine, so a
  ;; mapping is replaced by the one below it. Its `:scale` is a set of
  ;; independent settings -- the same one `pj/scale` writes -- so it
  ;; accumulates instead. Before this, a range set on the pose was lost
  ;; the moment a layer named a type.
  (let [d {:x [1 2 3] :y [1 2 3] :n [1 5 9]}
        spec #(-> % pj/plan :panels first :layers first :size-scale)]
    (testing "a pose's mapping and a layer's mapping"
      (is (= {:range [3 16] :type :log}
             (spec (-> d
                       (pj/pose :x :y {:size {:column :n :scale {:range [3 16]}}})
                       (pj/lay-point {:size {:column :n :scale :log}}))))))

    (testing "a pose's mapping and a layer that names no scale"
      ;; The type is the fallback, filled in once the scopes have
      ;; accumulated -- the mapping itself named none.
      (is (= {:range [3 16] :type :linear}
             (spec (-> d
                       (pj/pose :x :y {:size {:column :n :scale {:range [3 16]}}})
                       pj/lay-point)))))

    (testing "`pj/scale` and a mapping, in either order"
      (is (= {:type :log :range [3 16]}
             (spec (-> d (pj/pose :x :y)
                       (pj/lay-point {:size {:column :n :scale :log}})
                       (pj/scale :size {:range [3 16]})))))
      (is (= {:type :log :range [3 16]}
             (spec (-> d (pj/pose :x :y)
                       (pj/scale :size {:range [3 16]})
                       (pj/lay-point {:size {:column :n :scale :log}}))))))

    (testing "`pj/scale` before a `lay-*` that names its positions"
      ;; Both arms above pass only an options map, so the position
      ;; mapping is nil and the branch that folds one in is never
      ;; reached. That branch merged the position over the mapping
      ;; wholesale, discarding a scale already written there.
      (let [x-scale #(-> % pj/plan :panels first :x-scale)]
        (is (= {:type :log}
               (x-scale (-> d (pj/scale :x :log) (pj/lay-point :x :y)))))
        (is (= {:type :log}
               (x-scale (-> d (pj/lay-point :x :y) (pj/scale :x :log)))))))

    (testing "an outer pose of a composite and the cell inside it"
      (is (= {:range [3 16] :type :linear}
             (-> (pj/arrange [(-> d
                                  (pj/pose :x :y {:size {:column :n :scale :linear}})
                                  pj/lay-point)])
                 (pj/pose {:size {:column :n :scale {:range [3 16]}}})
                 pj/plan
                 :sub-plots first :plan :panels first :layers first :size-scale))))

    (testing "the innermost wins for each key it names"
      (is (= {:range [1 4] :type :log}
             (spec (-> d
                       (pj/pose :x :y {:size {:column :n :scale {:range [3 16]
                                                                 :type :log}}})
                       (pj/lay-point {:size {:column :n :scale {:range [1 4]}}}))))))

    (testing "and `:scale false` replaces rather than accumulating"
      ;; It says the value passes through no scale at all, so there is
      ;; nothing for an outer setting to add to.
      (is (nil? (spec (-> d
                          (pj/pose :x :y {:size {:column :n
                                                 :scale {:range [3 16]}}})
                          (pj/lay-point {:size {:column :n :scale false}}))))))))

(deftest from-names-the-source-the-way-the-plain-form-does-test
  ;; `:from` is what lets a mapping that leaves the source to the data
  ;; still carry a scale. A plain `:size :weight` has nowhere to put
  ;; one, which is why combining a pose's scale with a plain layer
  ;; mapping rewrites it in the full form.
  (let [d {:x [1 2 3] :y [1 2 3] :n [1 5 9]}]
    (testing "it reads a column where the data has one"
      (is (= (-> d (pj/lay-point :x :y {:size :n}) pj/plan
                 :panels first :layers first :groups first :sizes vec)
             (-> d (pj/lay-point :x :y {:size {:from :n}}) pj/plan
                 :panels first :layers first :groups first :sizes vec))))

    (testing "and a written value where it does not"
      (is (= [1.0 0.0 0.0 1.0]
             (-> d (pj/lay-point :x :y {:color {:from "red"}}) pj/plan
                 :panels first :layers first :groups first :color))))

    (testing "unlike :column and :value, it names no source of its own"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names 2 sources"
                            (pj/lay-point d :x :y {:size {:column :n :from :n}}))))

    (testing "a map naming only a scale names how to read, not what"
      ;; It is the form `pj/scale` writes. With no source named
      ;; anywhere, nothing is drawn for the aesthetic and the scale is
      ;; inert -- the same as setting a scale for an unmapped one.
      (is (nil? (-> d (pj/lay-point :x :y {:size {:scale :log}})
                    pj/plan :panels first :layers first :size)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names no source"
                            (pj/lay-point d :x :y {:size {}}))))))

;; ---- A gradient and a scale are separate things ----

(deftest a-gradient-and-a-colour-scale-can-both-be-asked-for-test
  ;; The gradient and the scale type are two settings. They shared one
  ;; key once, so whichever was written second discarded the other in
  ;; silence; the gradient is now the scale's own `:range`.
  (let [d {:x [1 2 3 4] :y [1 2 3 4] :n [1 10 100 1000]}
        stops #(->> % pj/plan :legend :stops (mapv :color))
        colours #(-> % pj/plan :panels first :layers first :groups first :colors vec)
        plain (-> d (pj/lay-point :x :y {:color :n}))
        viridis (-> plain (pj/options {:color-range :viridis}))
        logged (-> plain (pj/scale :color :log))
        both (-> plain (pj/options {:color-range :viridis}) (pj/scale :color :log))
        both-reversed (-> plain (pj/scale :color :log)
                          (pj/options {:color-range :viridis}))
        both-in-one-spec (-> plain (pj/scale :color {:type :log :range :viridis}))]
    (testing "the order they are written in does not decide which survives"
      (is (= (stops both) (stops both-reversed)))
      (is (= (colours both) (colours both-reversed))))

    (testing "the gradient is the one asked for"
      (is (= (stops viridis) (stops both)))
      (is (not= (stops plain) (stops both))))

    (testing "and the log scale still spaces the marks"
      (is (not= (colours viridis) (colours both)))
      (is (not= (colours plain) (colours logged))))

    (testing "one spec can carry both, and says the same as the two spellings"
      (is (= (stops both) (stops both-in-one-spec)))
      (is (= (colours both) (colours both-in-one-spec))))))

(deftest a-log-colour-legend-labels-its-decades-test
  ;; The marks were log-spaced already; the gradient bar carried only
  ;; its two end labels, so nothing said the scale was logarithmic.
  (let [d {:x [1 2 3 4] :y [1 2 3 4] :n [1 10 100 1000]}
        legend #(-> % pj/plan :legend)]
    (is (empty? (:ticks (legend (-> d (pj/lay-point :x :y {:color :n}))))))
    (let [lg (legend (-> d (pj/lay-point :x :y {:color :n}) (pj/scale :color :log)))]
      (is (= :log (:scale-type lg)))
      (is (= [1.0 10.0 100.0 1000.0] (mapv :value (:ticks lg)))))))

(deftest a-categorical-scale-on-a-numeric-column-says-what-to-do-test
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #":categorical scale places categories.*:x-type or :y-type"
       (-> {:hour [9 9 10 10] :v [1 2 3 4]}
           (pj/lay-point :hour :v)
           (pj/scale :x :categorical)
           pj/plan))))

;; ---- The registry declaration ----

(deftest a-mark-declares-what-it-varies-test
  (testing "the built-in point declares both channels it varies per row"
    (is (= :radius (layer-type/mark-varies :point :size)))
    (is (= :opacity (layer-type/mark-varies :point :alpha))))

  (testing "and a mark that draws one value for the whole layer declares neither"
    (is (nil? (layer-type/mark-varies :line :size)))
    (is (nil? (layer-type/mark-varies :boxplot :alpha))))

  (testing "the ink exponent comes from the quantity, not from the mark"
    (is (= 2 (layer-type/ink-exponent :point :size)))
    (is (= 1 (layer-type/ink-exponent :point :alpha))))

  (testing "a quantity Plotje does not draw is refused at registration"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"not a quantity Plotje draws"
                          (layer-type/register! ::bogus-quantity
                                                {:mark ::bogus :stat :identity
                                                 :varies {:size :loudness}}))))

  (testing "and two layer types sharing a mark cannot disagree about it"
    (try
      (layer-type/register! ::widths {:mark ::shared :stat :identity
                                      :varies {:size :width}})
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"has to agree about what that mark varies"
                            (layer-type/register! ::radii {:mark ::shared :stat :identity
                                                           :varies {:size :radius}})))
      (finally
        (swap! @(resolve 'scicloj.plotje.layer-type/registry*) dissoc ::widths ::radii))))

  (testing "and a mark whose other layer types vary nothing cannot be claimed"
    ;; Saying nothing is saying \"one value for the whole layer\", so a
    ;; declaration against a silent sibling would earn every layer type
    ;; drawing that mark a legend its renderer does not honour. `:line`
    ;; is drawn by `lay-line` and `lay-smooth`, neither of which varies
    ;; a size.
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"without varying :size at all"
                          (layer-type/register! ::hijack {:mark :line :stat :identity
                                                          :varies {:size :width}})))
    (is (nil? (layer-type/mark-varies :line :size))
        "and the built-in mark is left as it was")))

(deftest an-extension-that-varies-size-earns-its-legend-test
  ;; The capability was a closed table in `impl.plan` until the marks
  ;; declared it, and a mark it had never heard of answered no: an
  ;; extension varying size per row was warned about and denied its
  ;; legend while drawing correctly.
  (try
    (defmethod extract/extract-layer ::bubble [dl stat colors cfg]
      ((get-method extract/extract-layer :point) dl stat colors cfg))
    (defmethod mark/layer->membrane ::bubble [layer ctx]
      ((get-method mark/layer->membrane :point) layer ctx))
    (layer-type/register! ::bubble {:mark ::bubble :stat :identity
                                    :varies {:size :radius}
                                    :accepts [:size]})
    (let [warned (atom nil)
          plan (binding [*out* (java.io.StringWriter.)]
                 (let [w (java.io.StringWriter.)
                       p (binding [*out* w]
                           (pj/plan (-> measured
                                        (pj/pose :when :level)
                                        (pj/lay (merge (layer-type/lookup ::bubble)
                                                       {:size :radius})))))]
                   (reset! warned (str w))
                   p))]
      (is (= "" @warned) "no warning about a channel no mark varies")
      (is (some? (:size-legend plan)) "and the legend the encoding earns")
      (is (= :radius (:quantity (:size-legend plan)))))
    (finally
      (swap! @(resolve 'scicloj.plotje.layer-type/registry*) dissoc ::bubble)
      (remove-method extract/extract-layer ::bubble)
      (remove-method mark/layer->membrane ::bubble))))

;; ---- The legend has to fit the canvas ----

(deftest a-wide-range-takes-fewer-legend-rows-test
  ;; A row of swatches is as tall as the mark it draws, so a range the
  ;; writer widens makes the rows taller. Rendered at the tick
  ;; generator's seven rows, `{:range [3 20]}` ran off the bottom of
  ;; the canvas -- the rows overlapped and the last two were outside
  ;; the image.
  (let [spread {:x [1 2 3 4 5 6] :y [1 2 3 4 5 6] :n [1 4 9 16 25 36]}
        values (fn [pose] (->> (pj/plan pose) :size-legend :entries (mapv :value)))]
    (testing "the default range is untouched -- its rows were always eighteen tall"
      (is (= [5.0 10.0 15.0 20.0 25.0 30.0 35.0]
             (values (-> spread (pj/lay-point :x :y {:size :n})
                         (pj/options {:height 300}))))))

    (testing "a wide range keeps the ends and drops rows from between them"
      (let [vs (values (-> spread (pj/lay-point :x :y {:size :n})
                           (pj/scale :size {:range [3 20]})
                           (pj/options {:height 300})))]
        (is (= [5.0 35.0] [(first vs) (last vs)]))
        (is (< (count vs) 7))))

    (testing "and a taller canvas has room for more of them"
      (is (< (count (values (-> spread (pj/lay-point :x :y {:size :n})
                                (pj/scale :size {:range [3 20]})
                                (pj/options {:height 300}))))
             (count (values (-> spread (pj/lay-point :x :y {:size :n})
                                (pj/scale :size {:range [3 20]})
                                (pj/options {:height 900})))))))))

(deftest a-swatch-with-no-size-is-not-a-legend-row-test
  ;; `:from-zero` starts the domain at zero, and zero draws nothing --
  ;; so the legend read "0" beside an empty space.
  (let [spread {:x [1 2 3] :y [1 2 3] :n [10 20 30]}
        entries (-> spread
                    (pj/lay-point :x :y {:size :n})
                    (pj/scale :size {:by :area :from-zero true})
                    pj/plan
                    :size-legend
                    :entries)]
    (is (every? #(pos? (:magnitude %)) entries))
    (is (not-any? #(zero? (:value %)) entries))))

;; ---- The legend explains what the panel draws ----

(deftest the-legend-swatch-follows-the-quantity-test
  (testing "a radius is explained by circles"
    (let [sl (-> measured (pj/lay-point :when :level {:size :radius}) pj/plan :size-legend)]
      (is (= :radius (:quantity sl)))
      (is (= :circle (:swatch sl)))))

  (testing "and a width by strokes of that thickness"
    ;; Reached by an extension declaring `{:size :width}`; the built-in
    ;; marks all draw a radius. The renderer is asked directly, because
    ;; what is under test is the swatch it chooses.
    (is (= :segment (:swatch (layer-type/quantities :width))))))

;; ---- `:domain` on a colour or a fill ----
;;
;; The key carries two readings and the domain decides which: two
;; numbers are a range, anything else is a list of categories. Both
;; used to validate and change nothing.

(deftest color-domain-orders-categories-test
  (let [d [{:g "z" :v 1} {:g "y" :v 2} {:g "x" :v 3}]
        labels (fn [fr] (->> fr pj/plan :legend :entries (mapv :label)))
        swatches (fn [fr] (->> fr pj/plan :legend :entries (mapv :color)))
        plain (-> d (pj/lay-point :v :v {:color :g}))
        ordered (-> plain (pj/scale :color {:domain ["x" "y" "z"]}))]

    (testing "the data decides the order when no domain says otherwise"
      (is (= ["z" "y" "x"] (labels plain))))

    (testing "a categorical domain reorders the legend"
      (is (= ["x" "y" "z"] (labels ordered))))

    (testing "and the palette follows it, so which color a category gets changes"
      ;; The palette is handed out in order either way, so the sequence
      ;; of swatches is the same; what moves is which category each one
      ;; belongs to. "x" is last in the data and first in the domain, so
      ;; it goes from the palette's last color to its first.
      (is (= (swatches plain) (swatches ordered)))
      (let [paired (fn [fr] (->> fr pj/plan :legend :entries
                                 (map (juxt :label :color)) (into {})))]
        (is (= (get (paired plain) "z") (get (paired ordered) "x")))
        (is (= (get (paired plain) "x") (get (paired ordered) "z")))))

    (testing "and the marks are drawn in the color their legend row shows"
      ;; The drawn groups keep the data's order -- that is drawing
      ;; order, not legend order -- but each carries its new color.
      (let [group-colors (fn [fr] (->> fr pj/plan :panels first :layers first
                                       :groups (map (juxt :label :color)) (into {})))
            legend-colors (fn [fr] (->> fr pj/plan :legend :entries
                                        (map (juxt :label :color)) (into {})))]
        (is (= (group-colors ordered) (legend-colors ordered)))
        (is (not= (group-colors plain) (group-colors ordered)))))

    (testing "a category the domain omits is still drawn, after the listed ones"
      (let [partial-domain (-> plain (pj/scale :color {:domain ["x"]}))]
        (is (= ["x" "z" "y"] (labels partial-domain)))
        (is (re-find #":domain omits"
                     (with-out-str (pj/plan partial-domain))))))))

(deftest color-domain-sets-gradient-ends-test
  (let [d (mapv (fn [i] {:a i :b i :n (* 10 i)}) (range 1 10))
        plain (-> d (pj/lay-point :a :b {:color :n}))
        bounds (fn [fr] ((juxt :min :max) (:legend (pj/plan fr))))
        mark-colors (fn [fr] (->> fr pj/plan :panels first :layers first
                                  :groups first :colors vec))]

    (testing "the data decides the ends when no domain says otherwise"
      (is (= [10.0 90.0] (bounds plain))))

    (testing "a numeric domain replaces them, in the legend"
      (is (= [0.0 200.0] (bounds (-> plain (pj/scale :color {:domain [0 200]}))))))

    (testing "and in the marks, which is what the legend explains"
      (is (not= (mark-colors plain)
                (mark-colors (-> plain (pj/scale :color {:domain [0 200]}))))))

    (testing "a value outside the domain is drawn at the nearer end, not dropped"
      (let [narrow (-> plain (pj/scale :color {:domain [40 60]}))
            cs (mark-colors narrow)]
        ;; every row still draws
        (is (= 9 (count cs)))
        ;; 10, 20, 30 and 40 all clamp to the low end; 60 upward to the high
        (is (apply = (take 4 cs)))
        (is (apply = (drop 5 cs)))
        (is (not= (first cs) (last cs)))))))

(deftest fill-domain-sets-gradient-ends-test
  (testing "a fill reads its domain as a colour does"
    (let [tiles (vec (for [x (range 1 5) y (range 1 5)] {:x x :y y :v (* x y)}))
          tile-colors (fn [fr] (->> fr pj/plan :panels first :layers first
                                    :tiles (mapv :color)))
          plain (-> tiles (pj/lay-tile :x :y {:fill :v}))]
      (is (not= (tile-colors plain)
                (tile-colors (-> plain (pj/scale :fill {:domain [0 40]}))))))))

;; ---- The published table and the validators agree ----

(deftest aesthetic-scales-matches-what-pj-scale-accepts-test
  ;; `pj/aesthetic-scales` is what the book's reference table is built
  ;; from. It is derived from the same tables the validators read, so it
  ;; cannot drift from them -- but that says nothing about whether those
  ;; tables describe the API. These assertions cross that gap.
  (let [d {:x [1 2 3] :y [1 2 3]}
        accepts? (fn [aesthetic spec]
                   (try
                     (pj/scale (pj/lay-point d :x :y) aesthetic spec)
                     true
                     (catch clojure.lang.ExceptionInfo _ false)))
        sample {:range [1 2] :by :linear :from-zero true :midpoint 0
                :include 0
                :breaks [1 2] :tick-labels ["a" "b"] :n-ticks 3
                :label "t" :values [(first pj/shape-symbols)]}
        ;; Some keys carry a constraint the capability table does not
        ;; describe, and a value has to respect it to test acceptance
        ;; at all: `:tick-labels` is meaningless without the `:breaks`
        ;; it pairs with, an opacity range has to lie inside 0 to 1,
        ;; and the two colour aesthetics read `:range` as a gradient
        ;; and `:values` as colours rather than as symbols.
        colour? #{:color :fill}
        spec-for (fn [aesthetic a-type k]
                   (merge {:type a-type}
                          (cond
                            (= k :tick-labels) {:breaks [1 2] :tick-labels ["a" "b"]}
                            (and (= k :range) (= aesthetic :alpha)) {:range [0.1 1.0]}
                            (and (= k :range) (colour? aesthetic)) {:range :viridis}
                            (and (= k :values) (colour? aesthetic)) {:values ["#e41a1c" "#377eb8"]}
                            :else {k (get sample k)})))]

    (testing "every type listed is accepted, and one left out is refused"
      (doseq [entry pj/aesthetic-scales
              :let [aesthetic (:aesthetic entry)
                    types (:types entry)
                    absent (first (remove (set types) [:linear :log :categorical]))]]
        (doseq [t types]
          (is (accepts? aesthetic t) (str aesthetic " should accept " t)))
        (when absent
          (is (not (accepts? aesthetic absent))
              (str aesthetic " should refuse " absent)))))

    (testing "every spec key listed is accepted"
      (doseq [entry pj/aesthetic-scales
              :let [aesthetic (:aesthetic entry)
                    a-type (first (:types entry))]
              k (:keys entry)]
        (is (accepts? aesthetic (spec-for aesthetic a-type k))
            (str aesthetic " should accept " k))))

    (testing "a drawn-range key an aesthetic does not read is refused"
      (doseq [entry pj/aesthetic-scales
              :let [aesthetic (:aesthetic entry)
                    a-type (first (:types entry))
                    absent (first (remove (set (:keys entry))
                                          [:range :by :from-zero]))]
              :when absent]
        (is (not (accepts? aesthetic (spec-for aesthetic a-type absent)))
            (str aesthetic " should refuse " absent))))))

;; ============================================================
;; The colour path: :fill and :color answer one question
;; ============================================================

(def heat
  "A tile grid whose fill runs 0 to 361, wide enough that a domain of
   [0 100] clamps most of it."
  (let [cells (for [a (range 5) b (range 4)] [a b])]
    {:a (mapv first cells)
     :b (mapv second cells)
     :n (mapv (fn [[a b]] (* 19 (+ (* 4 a) b))) cells)}))

(defn fill-legend
  [pose]
  (-> pose pj/plan :legend))

(defn tile-colours
  [pose]
  (->> pose pj/plan :panels first :layers first :tiles (mapv :color)))

(deftest a-fill-domain-moves-the-legend-with-the-marks-test
  ;; The bar a reader matches a tile against has to span what the tiles
  ;; were drawn against, so the domain that clamps them bounds it too.
  (let [plain (-> heat (pj/lay-tile :a :b {:fill :n}))
        bounded (-> plain (pj/scale :fill {:domain [0 100]}))]
    (testing "the legend spans the domain, not the data"
      (is (= [0.0 361.0] [(:min (fill-legend plain)) (:max (fill-legend plain))]))
      (is (= [0.0 100.0] [(:min (fill-legend bounded)) (:max (fill-legend bounded))])))

    (testing "and the tiles are clamped to it, as :color's marks are"
      (is (= 20 (count (distinct (tile-colours plain)))))
      (is (= 7 (count (distinct (tile-colours bounded))))))

    (testing "a domain written on :color reaches a tile the same way"
      (is (= [0.0 100.0]
             (let [lg (fill-legend (-> plain (pj/scale :color {:domain [0 100]})))]
               [(:min lg) (:max lg)]))))))

(deftest a-fill-legends-provenance-is-the-resolver-that-drew-the-marks-test
  ;; :range-from-spec? decides whether render-time configuration
  ;; repaints the bar. Read off :fill-scale alone it answered false for
  ;; a range a :color spec had decided, and the bar was repainted to a
  ;; gradient the tiles were never drawn with.
  (let [plain (-> heat (pj/lay-tile :a :b {:fill :n}))]
    (testing "a range from a :fill spec is marked as coming from a spec"
      (is (true? (:range-from-spec?
                  (fill-legend (-> plain (pj/scale :fill {:range :inferno})))))))

    (testing "and so is one from a :color spec, which a tile reads too"
      (let [lg (fill-legend (-> plain (pj/scale :color {:range :inferno})))]
        (is (= :inferno (:color-range lg)))
        (is (true? (:range-from-spec? lg)))))

    (testing "a range from a plot option is not"
      (let [lg (fill-legend (-> plain (pj/options {:color-range :inferno})))]
        (is (= :inferno (:color-range lg)))
        (is (false? (:range-from-spec? lg)))))))

(deftest a-fill-scale-spec-wins-over-the-matching-plot-option-test
  ;; A spec is written on a mapping, a layer or a pose; a plot option is
  ;; written outside all three. Both specs are read before either
  ;; option, so a :fill-range option does not overrule a :color spec.
  (let [plain (-> heat (pj/lay-tile :a :b {:fill :n}))
        inferno (tile-colours (-> plain (pj/scale :fill {:range :inferno})))
        viridis (tile-colours (-> plain (pj/scale :fill {:range :viridis})))]
    (is (not= inferno viridis) "the two gradients differ, so the test can fail")

    (testing "a :color spec beats a :fill plot option"
      (is (= inferno
             (tile-colours (-> plain
                               (pj/scale :color {:range :inferno})
                               (pj/options {:fill-range :viridis}))))))

    (testing "and the legend says the same"
      (is (= :inferno
             (:color-range (fill-legend (-> plain
                                            (pj/scale :color {:range :inferno})
                                            (pj/options {:fill-range :viridis})))))))

    (testing "between the two options, :fill wins -- the aesthetic the mark draws"
      (is (= inferno
             (tile-colours (-> plain (pj/options {:fill-range :inferno
                                                  :color-range :viridis}))))))

    (testing "and between the two specs, :fill wins for the same reason"
      (is (= inferno
             (tile-colours (-> plain
                               (pj/scale :fill {:range :inferno})
                               (pj/scale :color {:range :viridis}))))))))

(deftest every-fill-setting-reads-the-same-lattice-test
  ;; One resolver answers every fill setting, so :midpoint is ordered
  ;; the way :range is rather than keeping a rule of its own.
  (let [plain (-> heat (pj/lay-tile :a :b {:fill :n}))
        spec-only (tile-colours (-> plain (pj/scale :color {:midpoint 100})))
        option-only (tile-colours (-> plain (pj/options {:fill-midpoint 200})))]
    (is (not= spec-only option-only)
        "the two midpoints differ, so the test can fail")

    (testing "a :color spec beats a :fill plot option"
      (is (= spec-only
             (tile-colours (-> plain
                               (pj/scale :color {:midpoint 100})
                               (pj/options {:fill-midpoint 200}))))))

    (testing "and a :fill spec beats a :color plot option"
      (is (= (tile-colours (-> plain (pj/scale :fill {:midpoint 100})))
             (tile-colours (-> plain
                               (pj/scale :fill {:midpoint 100})
                               (pj/options {:color-midpoint 200}))))))))

(deftest a-fill-spec-answers-key-by-key-test
  ;; A :fill spec naming only a gradient leaves the domain to :color,
  ;; rather than shadowing the whole :color spec.
  (let [lg (fill-legend (-> heat
                            (pj/lay-tile :a :b {:fill :n})
                            (pj/scale :fill {:range :inferno})
                            (pj/scale :color {:domain [0 100]})))]
    (is (= :inferno (:color-range lg)))
    (is (= [0.0 100.0] [(:min lg) (:max lg)]))))

(deftest a-contour-reads-its-legend-from-the-colour-scale-test
  ;; A contour draws lines, and only a tile paints an interior through
  ;; :fill. The bar is built through the resolver the levels are, so a
  ;; :fill scale changes neither.
  (let [rnd (java.util.Random. 42)
        d {:x (vec (repeatedly 300 #(.nextGaussian rnd)))
           :y (vec (repeatedly 300 #(.nextGaussian rnd)))}
        levels #(->> % pj/plan :panels first :layers first :levels (mapv :color))
        plain (-> d (pj/lay-contour :x :y))]
    (testing "a :color scale moves the levels and the bar together"
      (let [coloured (-> plain (pj/scale :color {:range :inferno}))]
        (is (not= (levels plain) (levels coloured)))
        (is (= :inferno (:color-range (fill-legend coloured))))
        (is (true? (:range-from-spec? (fill-legend coloured))))))

    (testing "a :fill scale moves neither"
      ;; Two warnings say :color is the key a contour reads, and they
      ;; are held back here; what matters is that the bar does not
      ;; honour :fill on its own.
      (binding [*out* (java.io.StringWriter.)]
        (let [filled (-> plain (pj/scale :fill {:range :inferno}))]
          (is (= (levels plain) (levels filled)))
          (is (nil? (:color-range (fill-legend filled)))))))))

(deftest a-mark-that-draws-one-colour-per-group-says-so-test
  ;; :point is the only mark carrying a colour per row. Every other mark
  ;; draws its group's one colour, so a numeric :color column it cannot
  ;; read as a gradient is reported rather than passed over.
  (let [d {:cat ["u" "u" "u" "v" "v" "v"]
           :b [1.0 4.0 2.0 3.0 5.0 2.0]
           :a [1.0 2.0 3.0 4.0 5.0 6.0]
           :lo [0.5 3.0 1.0 2.0 4.0 1.0]
           :hi [1.5 5.0 3.0 4.0 6.0 3.0]
           :lab ["p" "q" "r" "s" "t" "u"]
           :n [10.0 20.0 30.0 40.0 50.0 60.0]}
        warned? (fn [pose]
                  (let [out (java.io.StringWriter.)]
                    (binding [*out* out] (pj/plan pose))
                    (boolean (re-find #"do not read the column as a gradient"
                                      (str out)))))]
    (testing "a mark drawn once for many rows reports a numeric colour"
      ;; Each of these draws one path, one box or one bin from several
      ;; rows, so there is no row whose colour it could wear.
      (doseq [[what pose]
              [[:line (-> d (pj/lay-line :a :b {:color :n}))]
               [:step (-> d (pj/lay-step :a :b {:color :n}))]
               [:area (-> d (pj/lay-area :a :b {:color :n}))]
               [:smooth (-> d (pj/lay-smooth :a :b {:color :n}))]
               [:counted-bar (-> d (pj/lay-bar :cat {:color :n}))]
               [:histogram (-> d (pj/lay-histogram :a {:color :n}))]
               [:boxplot (-> d (pj/lay-boxplot :cat :b {:color :n}))]
               [:violin (-> d (pj/lay-violin :cat :b {:color :n}))]
               [:ridgeline (-> d (pj/lay-ridgeline :cat :b {:color :n}))]
               [:density (-> d (pj/lay-density :a {:color :n}))]
               [:summary (-> d (pj/lay-summary :cat :b {:color :n}))]]]
        (is (warned? pose) (str what " should report a numeric :color"))))

    (testing "and a mark drawn once per row reads the column instead"
      ;; The gradient is painted on the marks, so the legend beside them
      ;; explains what they show. Silence is the assertion here.
      (doseq [[what pose]
              [[:point (-> d (pj/lay-point :a :b {:color :n}))]
               [:tile (-> d (pj/lay-tile :a :b {:fill :n}))]
               [:value-bar (-> d (pj/lay-bar :cat :b {:color :n}))]
               [:numeric-position-bar (-> d (pj/lay-bar :a :b {:color :n}))]
               [:lollipop (-> d (pj/lay-lollipop :cat :b {:color :n}))]
               [:rug (-> d (pj/lay-rug :a {:color :n}))]
               [:text (-> d (pj/lay-text :a :b {:text :lab :color :n}))]
               [:errorbar (-> d (pj/lay-errorbar :a :b {:y-min :lo :y-max :hi
                                                        :color :n}))]]]
        (is (not (warned? pose)) (str what " should read a numeric :color"))))

    (testing "a bar answers by what it stands for, not by its layer type"
      ;; One `lay-bar` call, two answers: a bar per row wears its row's
      ;; colour, a bar counting a category has no row to read.
      (is (not (warned? (-> d (pj/lay-bar :cat :b {:color :n})))))
      (is (warned? (-> d (pj/lay-bar :cat {:color :n})))))

    (testing "a categorical :color is what those marks do read"
      (is (not (warned? (-> d (pj/lay-line :a :b {:color :cat}))))))))

(deftest a-fill-scale-is-reported-unused-only-where-nothing-reads-it-test
  ;; A tile reads :fill whether a column was mapped to it or the :bin2d
  ;; stat computed it, and the scale decides the gradient and the domain
  ;; either way. The warning belongs to the marks that paint with
  ;; :color, which a :fill scale leaves alone.
  (let [warned? (fn [pose]
                  (let [out (java.io.StringWriter.)]
                    (binding [*out* out] (pj/plan pose))
                    (boolean (re-find #"did you mean :color" (str out)))))
        binned (-> (rdatasets/ggplot2-mpg) (pj/lay-tile :displ :hwy))
        mapped (-> {:a [1.0 2.0 3.0] :b [1.0 2.0 3.0] :n [1.0 2.0 3.0]}
                   (pj/lay-tile :a :b {:fill :n}))]
    (testing "a tile reads the scale, so nothing is reported"
      (is (not (warned? (-> binned (pj/scale :fill {:range :inferno})))))
      (is (not (warned? (-> mapped (pj/scale :fill {:range :inferno}))))))

    (testing "and the scale it reads bounds the bar"
      (let [lg (-> binned (pj/scale :fill {:domain [0 60]}) pj/plan :legend)]
        (is (= [0.0 60.0] [(:min lg) (:max lg)]))))

    (testing "a mark that paints with :color is told which key it reads"
      (is (warned? (-> {:a [1.0 2.0 3.0] :b [1.0 2.0 3.0]}
                       (pj/lay-point :a :b)
                       (pj/scale :fill {:range :inferno})))))))

(def diverging
  {:region ["n" "s" "e" "w" "c"]
   :year [1 2 3 4 5]
   :change [-40 -10 5 30 60]})

(deftest a-midpoint-centres-one-window-on-itself-test
  ;; ggplot2's rescale_mid: the window is symmetric about the midpoint
  ;; and as wide as the further of the two domain ends reaches, so equal
  ;; deviations draw at equal saturation. Stretching each half to its
  ;; own extent instead drew -40 as saturated as 60.
  (testing "the further end takes the extreme and the nearer stops short"
    (is (== 1.0 (defaults/normalize-continuous :linear 60 -40 60 0)))
    (is (< 0.16666 (defaults/normalize-continuous :linear -40 -40 60 0) 0.16667)))

  (testing "the midpoint itself is the middle of the gradient"
    (is (== 0.5 (defaults/normalize-continuous :linear 0 -40 60 0))))

  (testing "equal deviations from the midpoint draw equally far from it"
    (let [t (fn [v] (defaults/normalize-continuous :linear v -40 60 0))]
      (is (< (Math/abs (- (- 0.5 (t -30)) (- (t 30) 0.5))) 1e-12))))

  (testing "a midpoint at an end of the domain still answers 0.5 there"
    ;; The clamp used to fire first, so zero on a column of zero and up
    ;; was drawn at the most saturated below-midpoint colour.
    (is (== 0.5 (defaults/normalize-continuous :linear 0 0 100 0))))

  (testing "a domain of one value answers the middle"
    (is (== 0.5 (defaults/normalize-continuous :linear 7 7 7 7))))

  (testing "a value outside the domain is drawn at the nearer end"
    (is (== 0.0 (defaults/normalize-continuous :linear -200 -40 60 0)))
    (is (== 1.0 (defaults/normalize-continuous :linear 200 -40 60 0))))

  (testing "no midpoint, and log, are untouched"
    (is (== 0.0 (defaults/normalize-continuous :linear 0 0 10 nil)))
    (is (== 0.5 (defaults/normalize-continuous :linear 5 0 10 nil)))
    (is (< 0.333 (defaults/normalize-continuous :log 10 1 1000 nil) 0.334))))

(deftest a-legend-bar-is-drawn-through-the-midpoint-its-marks-are-test
  ;; The bar's stops used to be sampled at evenly spaced places in the
  ;; gradient while the marks reached only part of it, so the colour at
  ;; the end labelled -40 was one no mark on the panel could take.
  (let [plan (-> diverging
                 (pj/lay-point :year :change {:color :change})
                 (pj/scale :color {:range :diverging :midpoint 0})
                 pj/plan)
        stops (-> plan :legend :stops)
        lowest-mark-color (-> plan :panels first :layers first
                              :groups first :colors first vec)]
    (testing "the bar spans the data, not the whole gradient"
      (is (= [-40.0 60.0] [(-> plan :legend :min) (-> plan :legend :max)]))
      (is (== 0.0 (:t (first stops))))
      (is (< 0.16666 (:gradient-t (first stops)) 0.16667))
      (is (== 1.0 (:gradient-t (last stops)))))

    (testing "so its low end is the colour its lowest mark is drawn in"
      (is (= (mapv double lowest-mark-color)
             (mapv double (:color (first stops))))))))

(deftest a-bar-without-a-midpoint-reads-place-for-place-test
  ;; Where the domain covers the whole gradient -- every scale without a
  ;; midpoint, :log among them -- a stop's place on the bar and its
  ;; place in the gradient are the same number.
  (let [agree? (fn [plan]
                 (every? #(== (:t %) (:gradient-t %)) (-> plan :legend :stops)))]
    (testing "a linear colour bar"
      (is (agree? (-> diverging
                      (pj/lay-point :year :change {:color :change})
                      (pj/scale :color {:range :diverging})
                      pj/plan))))

    (testing "a log fill bar"
      (is (agree? (-> {:x [1 2 3 4] :y [1 1 1 1] :v [1 10 100 1000]}
                      (pj/lay-tile :x :y {:fill :v})
                      (pj/scale :fill :log)
                      pj/plan))))))

(def narrow-sizes
  {:a [1.0 2.0 3.0] :b [1.0 2.0 3.0] :s [6.0 7.0 9.0]})

(deftest a-legend-labels-only-values-the-data-reaches-test
  ;; A legend entry pairs a value with the quantity a mark of that value
  ;; is drawn at. The log tick generator is allowed to reach past the
  ;; domain -- on an axis a bounding power of ten just outside the data
  ;; is an ordinary tick -- and on a narrow domain every tick it picks
  ;; can land outside: 6 to 9 gave the single tick 10. There the domain
  ;; is ticked across itself instead, by `scale/log-ticks-drawn`, which
  ;; is the same function the axis reads.
  (testing "a narrow log domain is ticked across itself"
    (let [entries (-> narrow-sizes
                      (pj/lay-point :a :b {:size :s})
                      (pj/scale :size :log)
                      pj/plan :size-legend :entries)]
      (is (= [6.0 7.0 8.0 9.0] (mapv :value entries)))
      (is (every? (fn [e] (<= 6.0 (:value e) 9.0)) entries))))

  (testing "the same on :alpha"
    (let [entries (-> narrow-sizes
                      (pj/lay-point :a :b {:alpha :s})
                      (pj/scale :alpha :log)
                      pj/plan :alpha-legend :entries)]
      (is (= [6.0 7.0 8.0 9.0] (mapv :value entries)))))

  (testing "and the axis beside it reads the same values"
    ;; The claim the release makes: one column gives a legend and the
    ;; axis beside it the same labels.
    (is (= ["6" "7" "8" "9"]
           (-> narrow-sizes
               (pj/lay-point :a :s)
               (pj/scale :y :log)
               pj/plan :panels first :y-ticks :labels))))

  (testing "a wide log domain is untouched -- its decades are all inside"
    (let [entries (-> {:a [1.0 2.0 3.0 4.0] :b [1.0 2.0 3.0 4.0]
                       :s [1.0 10.0 100.0 1000.0]}
                      (pj/lay-point :a :b {:size :s})
                      (pj/scale :size :log)
                      pj/plan :size-legend :entries)]
      (is (= [1.0 10.0 100.0 1000.0] (mapv :value entries)))))

  (testing "and every labelled value lies within a linear domain too"
    (let [entries (-> {:a [1.0 2.0 3.0 4.0] :b [1.0 4.0 2.0 3.0]
                       :s [3.0 8.0 14.0 20.0]}
                      (pj/lay-point :a :b {:size :s})
                      pj/plan :size-legend :entries)]
      (is (every? (fn [e] (<= 3.0 (:value e) 20.0)) entries)))))

(deftest a-layer-naming-no-scale-has-no-opinion-test
  ;; The rule an axis has always followed, now on every aesthetic. The
  ;; default used to materialize into a second opinion, so a `:log`
  ;; written on one layer disagreed with the silence of the next.
  (let [d {:a [1.0 2.0 3.0] :b [1.0 2.0 3.0] :p [1.0 10.0 100.0]}
        two-layers (fn [aesthetic]
                     (-> d
                         (pj/pose :a :b {aesthetic :p})
                         (pj/lay-point {aesthetic {:column :p :scale :log}})
                         (pj/lay-point {})
                         pj/plan))]
    (testing "the plot is drawn, on the scale the one naming layer gave"
      (doseq [[aesthetic scale-key] [[:size :size-scale] [:alpha :alpha-scale]]]
        (let [layers (-> (two-layers aesthetic) :panels first :layers)]
          (is (= 2 (count layers)) (str aesthetic " draws both layers"))
          ;; Settled onto both, so the silent layer is drawn through the
          ;; scale the legend explains rather than through the default.
          (is (= [{:type :log} {:type :log}] (mapv scale-key layers))
              (str aesthetic " settles onto both layers")))))

    (testing ":color settles the same way"
      (is (= :log (-> (two-layers :color) :legend :scale-type))))

    (testing "and the axis rule it now matches is unchanged"
      (is (= {:type :log}
             (-> d
                 (pj/pose :a :b)
                 (pj/lay-point {:x {:column :a :scale :log}})
                 (pj/lay-point {})
                 pj/plan :panels first :x-scale))))

    (testing "two layers that each name a different scale are still refused"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"read :size through different scales"
           (-> d
               (pj/pose :a :b {:size :p})
               (pj/lay-point {:size {:column :p :scale :log}})
               (pj/lay-point {:size {:column :p :scale :linear}})
               pj/plan))))))
