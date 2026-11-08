(ns
 plotje-book.configuration-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l31
 (defn
  base-plot
  []
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v5_l40 (pj/config))


(deftest
 t6_l42
 (is
  ((fn
    [cfg]
    (and
     (map? cfg)
     (= 600 (:width cfg))
     (= 400 (:height cfg))
     (= 10 (:margin cfg))
     (map? (:theme cfg))))
   v5_l40)))


(def
 v8_l54
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
 v9_l59
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


(deftest t10_l72 (is ((fn [t] (= 46 (count (:row-maps t)))) v9_l59)))


(deftest
 t12_l80
 (is
  ((fn
    [_]
    (let
     [printed
      (with-out-str
       (pj/plan (pj/options (base-plot) {:panel-size 200})))]
     (and
      (clojure.string/includes? printed ":panel-size")
      (clojure.string/includes? printed ":panel-width"))))
   v9_l59)))


(def
 v14_l95
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


(deftest t15_l105 (is ((fn [t] (= 16 (count (:row-maps t)))) v14_l95)))


(def v17_l115 (select-keys (pj/config) [:width :height]))


(deftest
 t18_l117
 (is ((fn [m] (= {:width 600, :height 400} m)) v17_l115)))


(def v19_l119 (-> (base-plot) (pj/options {:width 900, :height 250})))


(deftest
 t20_l122
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (> (:width s) 800))))
   v19_l119)))


(def v22_l132 (-> (base-plot) (pj/options {:theme {:bg "#FFFFFF"}})))


(deftest
 t23_l135
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v22_l132)))


(def v25_l141 (-> (base-plot) (pj/options {:color-values :dark2})))


(deftest
 t26_l144
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v25_l141)))


(def v28_l161 (pj/set-config! {:width 800}))


(def v29_l163 (select-keys (pj/config) [:width :height]))


(deftest
 t30_l165
 (is ((fn [m] (= {:width 800, :height 400} m)) v29_l163)))


(def v31_l167 (-> (base-plot) pj/plot))


(deftest
 t32_l170
 (is ((fn [v] (= 800 (:width (pj/svg-summary v)))) v31_l167)))


(def
 v34_l175
 (->
  (rdatasets/datasets-iris)
  (pj/matrix [:sepal-length :sepal-width])
  pj/plot))


(deftest
 t35_l179
 (is ((fn [v] (= 800 (:width (pj/svg-summary v)))) v34_l175)))


(def v37_l183 (pj/set-config! nil))


(def v38_l185 (select-keys (pj/config) [:width :height]))


(deftest
 t39_l187
 (is ((fn [m] (= {:width 600, :height 400} m)) v38_l185)))


(def
 v41_l203
 (pj/with-config
  {:theme {:bg "#1a1a2e", :grid "#16213e", :font-size 8}}
  (->
   (base-plot)
   (pj/options {:title "Dark Theme via with-config"})
   pj/plot)))


(deftest
 t42_l208
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (boolean (re-find #"rgb\(26,26,46\)" (str v)))))
   v41_l203)))


(def
 v44_l217
 (pj/with-config
  {:theme {:bg "#1a1a2e", :grid "#16213e", :font-size 8}}
  (->
   (base-plot)
   (pj/options {:title "Default theme: shown outside the body"}))))


(deftest
 t45_l221
 (is
  ((fn [v] (not (re-find #"rgb\(26,26,46\)" (str (pj/plot v)))))
   v44_l217)))


(def
 v47_l228
 (pj/with-config
  {:theme {:bg "#F5F5DC"}}
  (->
   (base-plot)
   (pj/options {:title "Partial Theme Override"})
   pj/plot)))


(deftest
 t48_l233
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (boolean (re-find #"rgb\(245,245,220\)" (str v)))))
   v47_l228)))


(def v50_l241 (:theme (pj/config)))


(deftest t51_l243 (is ((fn [t] (= "#E8E8E8" (:bg t))) v50_l241)))


(def v53_l259 (:point-radius (pj/config)))


(deftest t54_l261 (is ((fn [v] (= 3.0 v)) v53_l259)))


(def
 v56_l265
 (pj/set-config! {:width 800, :height 350, :point-radius 5.0}))


(def
 v58_l271
 (def
  precedence-plot
  (pj/with-config
   {:width 1200, :height 500}
   (pj/plot (-> (base-plot) (pj/options {:width 900}))))))


(def v59_l278 precedence-plot)


(deftest
 t60_l280
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 900.0 (double (:width s)))
      (= 500.0 (double (:height s))))))
   v59_l278)))


(def
 v62_l294
 (def
  precedence-point-radius
  (pj/with-config
   {:width 1200, :height 500}
   (:point-radius (pj/config)))))


(def v63_l298 precedence-point-radius)


(deftest t64_l300 (is ((fn [v] (= 5.0 v)) v63_l298)))


(def v66_l304 (pj/set-config! nil))


(def v67_l306 (select-keys (pj/config) [:width :height :point-radius]))


(deftest
 t68_l308
 (is
  ((fn [m] (= {:width 600, :height 400, :point-radius 3.0} m))
   v67_l306)))


(def v70_l346 (set (keys (:theme (pj/config)))))


(deftest
 t71_l348
 (is ((fn [s] (= #{:font-size :grid :bg} s)) v70_l346)))


(def
 v73_l357
 (-> (base-plot) (pj/options {:theme {:bg "#F5F5DC"}}) pj/plot))


(deftest
 t74_l361
 (is
  ((fn
    [v]
    (let
     [s (str v)]
     (and
      (clojure.string/includes? s "rgb(245,245,220)")
      (clojure.string/includes? s "rgb(245,245,245)"))))
   v73_l357)))


(def
 v76_l371
 (->
  (base-plot)
  (pj/options
   {:title "Full Dark Theme",
    :theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})
  pj/plot))


(deftest
 t77_l376
 (is
  ((fn
    [v]
    (let [s (str v)] (clojure.string/includes? s "rgb(45,45,45)")))
   v76_l371)))


(def
 v79_l386
 (pj/arrange
  [(->
    (base-plot)
    (pj/options
     {:title "Light",
      :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 8}}))
   (->
    (base-plot)
    (pj/options
     {:title "Dark",
      :theme {:bg "#2d2d2d", :grid "#444444", :font-size 8}}))]))


(deftest
 t80_l394
 (is
  ((fn
    [v]
    (and (pj/pose? v) (= 2 (count (:poses (first (:poses v)))))))
   v79_l386)))


(def v82_l422 (-> (base-plot) (pj/options {:color-values :tableau-10})))


(deftest
 t83_l425
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v82_l422)))


(def
 v85_l430
 (->
  (base-plot)
  (pj/options {:color-values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t86_l433
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v85_l430)))


(def
 v88_l438
 (->
  (base-plot)
  (pj/options
   {:color-values
    {"setosa" "#FF6B6B",
     "versicolor" "#4ECDC4",
     "virginica" "#45B7D1"}})))


(deftest
 t89_l443
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v88_l438)))


(def v91_l448 (pj/set-config! {:color-values :pastel1}))


(def v92_l450 (-> (base-plot) pj/plot))


(deftest
 t93_l453
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (contains? (:colors (pj/svg-summary v)) "rgb(251,180,174)")))
   v92_l450)))


(def v94_l459 (pj/set-config! nil))


(def
 v96_l463
 (pj/with-config {:color-values :accent} (-> (base-plot) pj/plot)))


(deftest
 t97_l467
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (not=
      (:colors (pj/svg-summary v))
      (:colors (pj/svg-summary (base-plot))))))
   v96_l463)))


(def
 v99_l483
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})))


(deftest
 t100_l486
 (is ((fn [v] (= 50 (:points (pj/svg-summary v)))) v99_l483)))


(def
 v102_l492
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/options {:color-range :inferno})))


(deftest
 t103_l496
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
   v102_l492)))


(def
 v105_l507
 (pj/with-config
  {:color-range :plasma}
  (->
   {:x (range 50), :y (range 50), :c (range 50)}
   (pj/lay-point :x :y {:color :c})
   pj/plot)))


(deftest
 t106_l512
 (is
  ((fn
    [v]
    (and
     (= 50 (:points (pj/svg-summary v)))
     (not=
      (:colors (pj/svg-summary v))
      (:colors
       (pj/svg-summary
        (->
         {:x (range 50), :y (range 50), :c (range 50)}
         (pj/lay-point :x :y {:color :c})))))))
   v105_l507)))


(def v108_l541 (pj/plan (base-plot)))


(deftest
 t109_l543
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v108_l541)))


(def v111_l550 (-> (base-plot)))


(deftest
 t112_l552
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v111_l550)))


(def v114_l561 (def good-plan (pj/plan (base-plot) {:validate false})))


(def v115_l563 (pj/valid-plan? good-plan))


(deftest t116_l565 (is ((fn [v] (true? v)) v115_l563)))


(def v118_l570 (def bad-plan (assoc good-plan :width "not-a-number")))


(def v119_l572 (pj/valid-plan? bad-plan))


(deftest t120_l574 (is ((fn [v] (false? v)) v119_l572)))


(def
 v122_l581
 (->
  (pj/explain-plan bad-plan)
  :errors
  first
  (select-keys [:in :value])))


(deftest
 t123_l586
 (is
  ((fn [m] (and (= [:width] (:in m)) (= "not-a-number" (:value m))))
   v122_l581)))


(def
 v125_l594
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
 t126_l605
 (is
  ((fn
    [m]
    (and
     (:caught m)
     (= "Plan does not conform to schema" (:message m))))
   v125_l594)))


(def v128_l614 (pj/plan (base-plot) {:validate false}))


(deftest
 t129_l616
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v128_l614)))


(def
 v131_l640
 (pj/with-config
  {:strict false}
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options {:nonsense-key 42})
   pj/plot)))


(deftest
 t132_l646
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v131_l640)))


(def
 v134_l650
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
 t135_l658
 (is
  ((fn [msg] (and (string? msg) (re-find #"does not recognize" msg)))
   v134_l650)))
