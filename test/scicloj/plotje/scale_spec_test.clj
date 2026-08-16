(ns scicloj.plotje.scale-spec-test
  "A scale spec says which scale a channel is read through and how a
   value spreads across what the mark draws it as: `:type`, `:domain`,
   `:range`, `:by` and `:from-zero`. The same spec can be written on
   the pose with `pj/scale` or in the mapping itself."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.layer-type :as layer-type]
            [scicloj.plotje.impl.extract :as extract]
            [scicloj.plotje.render.mark :as mark]))

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
                          pj/plan :x-label))))))

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
  ;; They shared the `:color-scale` key, so whichever was written
  ;; second discarded the other in silence.
  (let [d {:x [1 2 3 4] :y [1 2 3 4] :n [1 10 100 1000]}
        stops #(->> % pj/plan :legend :stops (mapv :color))
        colours #(-> % pj/plan :panels first :layers first :groups first :colors vec)
        plain (-> d (pj/lay-point :x :y {:color :n}))
        viridis (-> plain (pj/options {:color-scale :viridis}))
        logged (-> plain (pj/scale :color :log))
        both (-> plain (pj/options {:color-scale :viridis}) (pj/scale :color :log))
        both-reversed (-> plain (pj/scale :color :log)
                          (pj/options {:color-scale :viridis}))]
    (testing "the order they are written in does not decide which survives"
      (is (= (stops both) (stops both-reversed)))
      (is (= (colours both) (colours both-reversed))))

    (testing "the gradient is the one asked for"
      (is (= (stops viridis) (stops both)))
      (is (not= (stops plain) (stops both))))

    (testing "and the log scale still spaces the marks"
      (is (not= (colours viridis) (colours both)))
      (is (not= (colours plain) (colours logged))))))

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
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"A mark draws one quantity"
                            (layer-type/register! ::radii {:mark ::shared :stat :identity
                                                           :varies {:size :radius}})))
      (finally
        (swap! @(resolve 'scicloj.plotje.layer-type/registry*) dissoc ::widths ::radii)))))

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
        sample {:range [1 2] :by :linear :from-zero true
                :breaks [1 2] :labels ["a" "b"] :n-ticks 3
                :label "t" :values [(first pj/shape-symbols)]}
        ;; Two keys carry a constraint the capability table does not
        ;; describe, and a value has to respect it to test acceptance
        ;; at all: `:labels` is meaningless without the `:breaks` it
        ;; pairs with, and an opacity range has to lie inside 0 to 1.
        spec-for (fn [aesthetic a-type k]
                   (merge {:type a-type}
                          (cond
                            (= k :labels) {:breaks [1 2] :labels ["a" "b"]}
                            (and (= k :range) (= aesthetic :alpha)) {:range [0.1 1.0]}
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
