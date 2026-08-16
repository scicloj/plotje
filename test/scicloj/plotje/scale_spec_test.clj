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

(deftest an-axis-takes-its-scale-from-the-pose-test
  ;; One panel has one x axis, so its layers cannot each have their own.
  (doseq [k [:x :y]]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"an axis takes its scale from the pose"
                          (pj/lay-point measured :when :level
                                        {k {:column :level :scale :log}}))
        (str "a scale type on " k))))

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
