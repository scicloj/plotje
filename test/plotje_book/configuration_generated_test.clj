(ns
 plotje-book.configuration-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l29
 (defn
  base-plot
  []
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v5_l38 (pj/config))


(deftest
 t6_l40
 (is
  ((fn
    [cfg]
    (and
     (map? cfg)
     (= 600 (:width cfg))
     (= 400 (:height cfg))
     (= 10 (:margin cfg))
     (map? (:theme cfg))))
   v5_l38)))


(def
 v8_l52
 (def
  category-order
  ["Layout"
   "Theme"
   "Typography"
   "Points"
   "Bars & Lines"
   "Rules & Bands"
   "Ticks"
   "Statistics"
   "Labels"
   "Behavior"
   "Color"
   "Interaction"
   "Output"]))


(def
 v9_l57
 (kind/table
  {:column-names ["Key" "Default" "Category" "Description"],
   :row-maps
   (let
    [cfg (pj/config)]
    (->>
     pj/config-key-docs
     (sort-by
      (fn [[k [cat]]] [(.indexOf category-order cat) (name k)]))
     (mapv
      (fn
       [[k [cat desc]]]
       {"Key" (kind/code (pr-str k)),
        "Default" (kind/code (pr-str (get cfg k))),
        "Category" cat,
        "Description" desc}))))}))


(deftest t10_l70 (is ((fn [t] (= 45 (count (:row-maps t)))) v9_l57)))


(def
 v12_l80
 (kind/table
  {:column-names ["Key" "Category" "Description"],
   :row-maps
   (->>
    pj/plot-option-docs
    (sort-by (fn [[k [cat]]] [cat (name k)]))
    (mapv
     (fn
      [[k [cat desc]]]
      {"Key" (kind/code (pr-str k)),
       "Category" cat,
       "Description" desc})))}))


(deftest t13_l90 (is ((fn [t] (= 15 (count (:row-maps t)))) v12_l80)))


(def v15_l100 (select-keys (pj/config) [:width :height]))


(deftest
 t16_l102
 (is ((fn [m] (= {:width 600, :height 400} m)) v15_l100)))


(def v17_l104 (-> (base-plot) (pj/options {:width 900, :height 250})))


(deftest
 t18_l107
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (> (:width s) 800))))
   v17_l104)))


(def v20_l116 (-> (base-plot) (pj/options {:theme {:bg "#FFFFFF"}})))


(deftest
 t21_l119
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v20_l116)))


(def v23_l125 (-> (base-plot) (pj/options {:color-values :dark2})))


(deftest
 t24_l128
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v23_l125)))


(def v26_l140 (pj/set-config! {:width 800}))


(def v27_l142 (select-keys (pj/config) [:width :height]))


(deftest
 t28_l144
 (is ((fn [m] (= {:width 800, :height 400} m)) v27_l142)))


(def v29_l146 (-> (base-plot)))


(def
 v31_l151
 (->
  (rdatasets/datasets-iris)
  (pj/cross-matrix [:sepal-length :sepal-width])))


(def
 v33_l156
 (->
  (rdatasets/datasets-iris)
  (pj/cross-matrix [:sepal-length :sepal-width])
  pj/svg-summary
  :width))


(deftest t34_l161 (is ((fn [w] (= 800 w)) v33_l156)))


(def v36_l165 (pj/set-config! nil))


(def v37_l167 (select-keys (pj/config) [:width :height]))


(deftest
 t38_l169
 (is ((fn [m] (= {:width 600, :height 400} m)) v37_l167)))


(def
 v40_l180
 (pj/with-config
  {:theme {:bg "#1a1a2e", :grid "#16213e", :font-size 8}}
  (-> (base-plot) (pj/options {:title "Dark Theme via with-config"}))))


(deftest
 t41_l184
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v40_l180)))


(def
 v43_l191
 (pj/with-config
  {:theme {:bg "#F5F5DC"}}
  (-> (base-plot) (pj/options {:title "Partial Theme Override"}))))


(deftest
 t44_l195
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v43_l191)))


(def v46_l201 (select-keys (pj/config) [:width :height]))


(deftest
 t47_l203
 (is ((fn [m] (= {:width 600, :height 400} m)) v46_l201)))


(def v49_l219 (:point-radius (pj/config)))


(deftest t50_l221 (is ((fn [v] (= 3.0 v)) v49_l219)))


(def
 v52_l225
 (pj/set-config! {:width 800, :height 350, :point-radius 5.0}))


(def
 v54_l231
 (def
  precedence-plot
  (pj/with-config
   {:width 1200, :height 500}
   (pj/plot (-> (base-plot) (pj/options {:width 900}))))))


(def v55_l238 precedence-plot)


(deftest
 t56_l240
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 900.0 (double (:width s)))
      (= 500.0 (double (:height s))))))
   v55_l238)))


(def
 v58_l254
 (def
  precedence-point-radius
  (pj/with-config
   {:width 1200, :height 500}
   (:point-radius (pj/config)))))


(def v59_l258 precedence-point-radius)


(deftest t60_l260 (is ((fn [v] (= 5.0 v)) v59_l258)))


(def v62_l264 (pj/set-config! nil))


(def v63_l266 (select-keys (pj/config) [:width :height :point-radius]))


(deftest
 t64_l268
 (is
  ((fn [m] (= {:width 600, :height 400, :point-radius 3.0} m))
   v63_l266)))


(def v66_l306 (set (keys (:theme (pj/config)))))


(deftest
 t67_l308
 (is ((fn [s] (= #{:font-size :grid :bg} s)) v66_l306)))


(def
 v69_l317
 (-> (base-plot) (pj/options {:theme {:bg "#F5F5DC"}}) pj/plot))


(deftest
 t70_l321
 (is
  ((fn
    [v]
    (let
     [s (str v)]
     (and
      (clojure.string/includes? s "rgb(245,245,220)")
      (clojure.string/includes? s "rgb(245,245,245)"))))
   v69_l317)))


(def
 v72_l331
 (->
  (base-plot)
  (pj/options
   {:title "Full Dark Theme",
    :theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})
  pj/plot))


(deftest
 t73_l336
 (is
  ((fn
    [v]
    (let [s (str v)] (clojure.string/includes? s "rgb(45,45,45)")))
   v72_l331)))


(def
 v75_l346
 (pj/arrange
  [(->
    (base-plot)
    (pj/options
     {:title "Light",
      :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 8},
      :width 350,
      :height 250}))
   (->
    (base-plot)
    (pj/options
     {:title "Dark",
      :theme {:bg "#2d2d2d", :grid "#444444", :font-size 8},
      :width 350,
      :height 250}))]))


(deftest
 t76_l356
 (is
  ((fn
    [v]
    (and (pj/pose? v) (= 2 (count (:poses (first (:poses v)))))))
   v75_l346)))


(def v78_l384 (-> (base-plot) (pj/options {:color-values :tableau-10})))


(deftest
 t79_l387
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v78_l384)))


(def
 v81_l392
 (->
  (base-plot)
  (pj/options {:color-values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t82_l395
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v81_l392)))


(def
 v84_l400
 (->
  (base-plot)
  (pj/options
   {:color-values
    {"setosa" "#FF6B6B",
     "versicolor" "#4ECDC4",
     "virginica" "#45B7D1"}})))


(deftest
 t85_l405
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v84_l400)))


(def v87_l410 (pj/set-config! {:color-values :pastel1}))


(def v88_l412 (-> (base-plot)))


(deftest
 t89_l414
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v88_l412)))


(def v90_l417 (pj/set-config! nil))


(def v92_l421 (pj/with-config {:color-values :accent} (-> (base-plot))))


(deftest
 t93_l424
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v92_l421)))


(def
 v95_l436
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})))


(deftest
 t96_l439
 (is ((fn [v] (= 50 (:points (pj/svg-summary v)))) v95_l436)))


(def
 v98_l445
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/options {:color-range :inferno})))


(deftest
 t99_l449
 (is
  ((fn
    [v]
    (let
     [leg
      (:legend
       (pj/plan
        (->
         {:x (range 50), :y (range 50), :c (range 50)}
         (pj/lay-point :x :y {:color :c})
         (pj/options {:color-range :inferno}))))]
     (and
      (= 50 (:points (pj/svg-summary v)))
      (= :inferno (:color-range leg))
      (= :continuous (:type leg)))))
   v98_l445)))


(def
 v101_l460
 (pj/with-config
  {:color-range :plasma}
  (->
   {:x (range 50), :y (range 50), :c (range 50)}
   (pj/lay-point :x :y {:color :c}))))


(deftest
 t102_l464
 (is ((fn [v] (= 50 (:points (pj/svg-summary v)))) v101_l460)))


(def v104_l486 (pj/plan (base-plot)))


(deftest
 t105_l488
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v104_l486)))


(def v107_l495 (-> (base-plot)))


(deftest
 t108_l497
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v107_l495)))


(def v110_l506 (def good-plan (pj/plan (base-plot) {:validate false})))


(def v111_l508 (pj/valid-plan? good-plan))


(deftest t112_l510 (is ((fn [v] (true? v)) v111_l508)))


(def v114_l515 (def bad-plan (assoc good-plan :width "not-a-number")))


(def v115_l517 (pj/valid-plan? bad-plan))


(deftest t116_l519 (is ((fn [v] (false? v)) v115_l517)))


(def
 v118_l526
 (->
  (pj/explain-plan bad-plan)
  :errors
  first
  (select-keys [:in :value])))


(deftest
 t119_l531
 (is
  ((fn [m] (and (= [:width] (:in m)) (= "not-a-number" (:value m))))
   v118_l526)))


(def
 v121_l539
 (try
  (let
   [plan
    (pj/plan (base-plot) {:validate false})
    bad
    (assoc plan :width "not-a-number")]
   (when-let
    [explanation (pj/explain-plan bad)]
    (throw
     (ex-info
      "Plan does not conform to schema"
      {:explanation explanation})))
   :no-error)
  (catch Exception e {:caught true, :message (.getMessage e)})))


(deftest
 t122_l550
 (is
  ((fn
    [m]
    (and
     (:caught m)
     (= "Plan does not conform to schema" (:message m))))
   v121_l539)))


(def v124_l559 (pj/plan (base-plot) {:validate false}))


(deftest
 t125_l561
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v124_l559)))


(def
 v127_l585
 (pj/with-config
  {:strict false}
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options {:nonsense-key 42}))))


(deftest
 t128_l590
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v127_l585)))


(def
 v130_l594
 (pj/with-config
  {:strict true}
  (try
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/options {:nonsense-key 42})
    pj/plot)
   (catch Exception e (.getMessage e)))))


(deftest
 t131_l602
 (is
  ((fn [msg] (and (string? msg) (re-find #"does not recognize" msg)))
   v130_l594)))
