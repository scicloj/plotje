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
   "Annotations"
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


(deftest t10_l70 (is ((fn [t] (= 41 (count (:row-maps t)))) v9_l57)))


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


(def v23_l125 (-> (base-plot) (pj/options {:palette :dark2})))


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


(def v31_l150 (pj/set-config! nil))


(def v32_l152 (select-keys (pj/config) [:width :height]))


(deftest
 t33_l154
 (is ((fn [m] (= {:width 600, :height 400} m)) v32_l152)))


(def
 v35_l165
 (pj/with-config
  {:theme {:bg "#1a1a2e", :grid "#16213e", :font-size 8}}
  (-> (base-plot) (pj/options {:title "Dark Theme via with-config"}))))


(deftest
 t36_l169
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v35_l165)))


(def
 v38_l176
 (pj/with-config
  {:theme {:bg "#F5F5DC"}}
  (-> (base-plot) (pj/options {:title "Partial Theme Override"}))))


(deftest
 t39_l180
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v38_l176)))


(def v41_l186 (select-keys (pj/config) [:width :height]))


(deftest
 t42_l188
 (is ((fn [m] (= {:width 600, :height 400} m)) v41_l186)))


(def v44_l204 (:point-radius (pj/config)))


(deftest t45_l206 (is ((fn [v] (= 3.0 v)) v44_l204)))


(def
 v47_l210
 (pj/set-config! {:width 800, :height 350, :point-radius 5.0}))


(def
 v49_l216
 (def
  precedence-plot
  (pj/with-config
   {:width 1200, :height 500}
   (pj/plot (-> (base-plot) (pj/options {:width 900}))))))


(def v50_l223 precedence-plot)


(deftest
 t51_l225
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 900.0 (double (:width s)))
      (= 500.0 (double (:height s))))))
   v50_l223)))


(def
 v53_l239
 (def
  precedence-point-radius
  (pj/with-config
   {:width 1200, :height 500}
   (:point-radius (pj/config)))))


(def v54_l243 precedence-point-radius)


(deftest t55_l245 (is ((fn [v] (= 5.0 v)) v54_l243)))


(def v57_l249 (pj/set-config! nil))


(def v58_l251 (select-keys (pj/config) [:width :height :point-radius]))


(deftest
 t59_l253
 (is
  ((fn [m] (= {:width 600, :height 400, :point-radius 3.0} m))
   v58_l251)))


(def v61_l291 (set (keys (:theme (pj/config)))))


(deftest
 t62_l293
 (is ((fn [s] (= #{:font-size :grid :bg} s)) v61_l291)))


(def
 v64_l302
 (-> (base-plot) (pj/options {:theme {:bg "#F5F5DC"}}) pj/plot))


(deftest
 t65_l306
 (is
  ((fn
    [v]
    (let
     [s (str v)]
     (and
      (clojure.string/includes? s "rgb(245,245,220)")
      (clojure.string/includes? s "rgb(245,245,245)"))))
   v64_l302)))


(def
 v67_l316
 (->
  (base-plot)
  (pj/options
   {:title "Full Dark Theme",
    :theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})
  pj/plot))


(deftest
 t68_l321
 (is
  ((fn
    [v]
    (let [s (str v)] (clojure.string/includes? s "rgb(45,45,45)")))
   v67_l316)))


(def
 v70_l331
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
 t71_l341
 (is
  ((fn
    [v]
    (and (pj/pose? v) (= 2 (count (:poses (first (:poses v)))))))
   v70_l331)))


(def v73_l366 (-> (base-plot) (pj/options {:palette :tableau-10})))


(deftest
 t74_l369
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v73_l366)))


(def
 v76_l374
 (->
  (base-plot)
  (pj/options {:palette ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t77_l377
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v76_l374)))


(def
 v79_l382
 (->
  (base-plot)
  (pj/options
   {:palette
    {"setosa" "#FF6B6B",
     "versicolor" "#4ECDC4",
     "virginica" "#45B7D1"}})))


(deftest
 t80_l387
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v79_l382)))


(def v82_l392 (pj/set-config! {:palette :pastel1}))


(def v83_l394 (-> (base-plot)))


(deftest
 t84_l396
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v83_l394)))


(def v85_l399 (pj/set-config! nil))


(def v87_l403 (pj/with-config {:palette :accent} (-> (base-plot))))


(deftest
 t88_l406
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v87_l403)))


(def
 v90_l417
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})))


(deftest
 t91_l420
 (is ((fn [v] (= 50 (:points (pj/svg-summary v)))) v90_l417)))


(def
 v93_l426
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/options {:color-scale :inferno})))


(deftest
 t94_l430
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
         (pj/options {:color-scale :inferno}))))]
     (and
      (= 50 (:points (pj/svg-summary v)))
      (= :inferno (:color-scale leg))
      (= :continuous (:type leg)))))
   v93_l426)))


(def
 v96_l441
 (pj/with-config
  {:color-scale :plasma}
  (->
   {:x (range 50), :y (range 50), :c (range 50)}
   (pj/lay-point :x :y {:color :c}))))


(deftest
 t97_l445
 (is ((fn [v] (= 50 (:points (pj/svg-summary v)))) v96_l441)))


(def v99_l464 (pj/plan (base-plot)))


(deftest
 t100_l466
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v99_l464)))


(def v102_l473 (-> (base-plot)))


(deftest
 t103_l475
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v102_l473)))


(def v105_l484 (def good-plan (pj/plan (base-plot) {:validate false})))


(def v106_l486 (pj/valid-plan? good-plan))


(deftest t107_l488 (is ((fn [v] (true? v)) v106_l486)))


(def v109_l493 (def bad-plan (assoc good-plan :width "not-a-number")))


(def v110_l495 (pj/valid-plan? bad-plan))


(deftest t111_l497 (is ((fn [v] (false? v)) v110_l495)))


(def
 v113_l504
 (->
  (pj/explain-plan bad-plan)
  :errors
  first
  (select-keys [:in :value])))


(deftest
 t114_l509
 (is
  ((fn [m] (and (= [:width] (:in m)) (= "not-a-number" (:value m))))
   v113_l504)))


(def
 v116_l518
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
 t117_l529
 (is
  ((fn
    [m]
    (and
     (:caught m)
     (= "Plan does not conform to schema" (:message m))))
   v116_l518)))


(def v119_l538 (pj/plan (base-plot) {:validate false}))


(deftest
 t120_l540
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v119_l538)))


(def
 v122_l564
 (pj/with-config
  {:strict false}
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options {:nonsense-key 42}))))


(deftest
 t123_l569
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v122_l564)))


(def
 v125_l573
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
 t126_l581
 (is
  ((fn [msg] (and (string? msg) (re-find #"does not recognize" msg)))
   v125_l573)))
