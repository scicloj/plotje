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


(deftest t10_l70 (is ((fn [t] (= 46 (count (:row-maps t)))) v9_l57)))


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


(deftest t13_l90 (is ((fn [t] (= 16 (count (:row-maps t)))) v12_l80)))


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


(def v20_l117 (-> (base-plot) (pj/options {:theme {:bg "#FFFFFF"}})))


(deftest
 t21_l120
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v20_l117)))


(def v23_l126 (-> (base-plot) (pj/options {:color-values :dark2})))


(deftest
 t24_l129
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v23_l126)))


(def v26_l144 (pj/set-config! {:width 800}))


(def v27_l146 (select-keys (pj/config) [:width :height]))


(deftest
 t28_l148
 (is ((fn [m] (= {:width 800, :height 400} m)) v27_l146)))


(def v29_l150 (-> (base-plot)))


(def
 v31_l155
 (->
  (rdatasets/datasets-iris)
  (pj/matrix [:sepal-length :sepal-width])))


(def
 v33_l160
 (->
  (rdatasets/datasets-iris)
  (pj/matrix [:sepal-length :sepal-width])
  pj/svg-summary
  :width))


(deftest t34_l165 (is ((fn [w] (= 800 w)) v33_l160)))


(def v36_l169 (pj/set-config! nil))


(def v37_l171 (select-keys (pj/config) [:width :height]))


(deftest
 t38_l173
 (is ((fn [m] (= {:width 600, :height 400} m)) v37_l171)))


(def
 v40_l189
 (pj/with-config
  {:theme {:bg "#1a1a2e", :grid "#16213e", :font-size 8}}
  (->
   (base-plot)
   (pj/options {:title "Dark Theme via with-config"})
   pj/plot)))


(deftest
 t41_l194
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (boolean (re-find #"rgb\(26,26,46\)" (str v)))))
   v40_l189)))


(def
 v43_l203
 (pj/with-config
  {:theme {:bg "#1a1a2e", :grid "#16213e", :font-size 8}}
  (->
   (base-plot)
   (pj/options {:title "Default theme: shown outside the body"}))))


(deftest
 t44_l207
 (is
  ((fn [v] (not (re-find #"rgb\(26,26,46\)" (str (pj/plot v)))))
   v43_l203)))


(def
 v46_l214
 (pj/with-config
  {:theme {:bg "#F5F5DC"}}
  (->
   (base-plot)
   (pj/options {:title "Partial Theme Override"})
   pj/plot)))


(deftest
 t47_l219
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (boolean (re-find #"rgb\(245,245,220\)" (str v)))))
   v46_l214)))


(def v49_l227 (select-keys (pj/config) [:width :height]))


(deftest
 t50_l229
 (is ((fn [m] (= {:width 600, :height 400} m)) v49_l227)))


(def v52_l245 (:point-radius (pj/config)))


(deftest t53_l247 (is ((fn [v] (= 3.0 v)) v52_l245)))


(def
 v55_l251
 (pj/set-config! {:width 800, :height 350, :point-radius 5.0}))


(def
 v57_l257
 (def
  precedence-plot
  (pj/with-config
   {:width 1200, :height 500}
   (pj/plot (-> (base-plot) (pj/options {:width 900}))))))


(def v58_l264 precedence-plot)


(deftest
 t59_l266
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 900.0 (double (:width s)))
      (= 500.0 (double (:height s))))))
   v58_l264)))


(def
 v61_l280
 (def
  precedence-point-radius
  (pj/with-config
   {:width 1200, :height 500}
   (:point-radius (pj/config)))))


(def v62_l284 precedence-point-radius)


(deftest t63_l286 (is ((fn [v] (= 5.0 v)) v62_l284)))


(def v65_l290 (pj/set-config! nil))


(def v66_l292 (select-keys (pj/config) [:width :height :point-radius]))


(deftest
 t67_l294
 (is
  ((fn [m] (= {:width 600, :height 400, :point-radius 3.0} m))
   v66_l292)))


(def v69_l332 (set (keys (:theme (pj/config)))))


(deftest
 t70_l334
 (is ((fn [s] (= #{:font-size :grid :bg} s)) v69_l332)))


(def
 v72_l343
 (-> (base-plot) (pj/options {:theme {:bg "#F5F5DC"}}) pj/plot))


(deftest
 t73_l347
 (is
  ((fn
    [v]
    (let
     [s (str v)]
     (and
      (clojure.string/includes? s "rgb(245,245,220)")
      (clojure.string/includes? s "rgb(245,245,245)"))))
   v72_l343)))


(def
 v75_l357
 (->
  (base-plot)
  (pj/options
   {:title "Full Dark Theme",
    :theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})
  pj/plot))


(deftest
 t76_l362
 (is
  ((fn
    [v]
    (let [s (str v)] (clojure.string/includes? s "rgb(45,45,45)")))
   v75_l357)))


(def
 v78_l372
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
 t79_l382
 (is
  ((fn
    [v]
    (and (pj/pose? v) (= 2 (count (:poses (first (:poses v)))))))
   v78_l372)))


(def v81_l410 (-> (base-plot) (pj/options {:color-values :tableau-10})))


(deftest
 t82_l413
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v81_l410)))


(def
 v84_l418
 (->
  (base-plot)
  (pj/options {:color-values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t85_l421
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v84_l418)))


(def
 v87_l426
 (->
  (base-plot)
  (pj/options
   {:color-values
    {"setosa" "#FF6B6B",
     "versicolor" "#4ECDC4",
     "virginica" "#45B7D1"}})))


(deftest
 t88_l431
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v87_l426)))


(def v90_l436 (pj/set-config! {:color-values :pastel1}))


(def v91_l438 (-> (base-plot)))


(deftest
 t92_l440
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v91_l438)))


(def v93_l443 (pj/set-config! nil))


(def
 v95_l447
 (pj/with-config {:color-values :accent} (-> (base-plot) pj/plot)))


(deftest
 t96_l451
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (not=
      (:colors (pj/svg-summary v))
      (:colors (pj/svg-summary (base-plot))))))
   v95_l447)))


(def
 v98_l467
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})))


(deftest
 t99_l470
 (is ((fn [v] (= 50 (:points (pj/svg-summary v)))) v98_l467)))


(def
 v101_l476
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/options {:color-range :inferno})))


(deftest
 t102_l480
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
   v101_l476)))


(def
 v104_l491
 (pj/with-config
  {:color-range :plasma}
  (->
   {:x (range 50), :y (range 50), :c (range 50)}
   (pj/lay-point :x :y {:color :c})
   pj/plot)))


(deftest
 t105_l496
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
   v104_l491)))


(def v107_l525 (pj/plan (base-plot)))


(deftest
 t108_l527
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v107_l525)))


(def v110_l534 (-> (base-plot)))


(deftest
 t111_l536
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v110_l534)))


(def v113_l545 (def good-plan (pj/plan (base-plot) {:validate false})))


(def v114_l547 (pj/valid-plan? good-plan))


(deftest t115_l549 (is ((fn [v] (true? v)) v114_l547)))


(def v117_l554 (def bad-plan (assoc good-plan :width "not-a-number")))


(def v118_l556 (pj/valid-plan? bad-plan))


(deftest t119_l558 (is ((fn [v] (false? v)) v118_l556)))


(def
 v121_l565
 (->
  (pj/explain-plan bad-plan)
  :errors
  first
  (select-keys [:in :value])))


(deftest
 t122_l570
 (is
  ((fn [m] (and (= [:width] (:in m)) (= "not-a-number" (:value m))))
   v121_l565)))


(def
 v124_l578
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
 t125_l589
 (is
  ((fn
    [m]
    (and
     (:caught m)
     (= "Plan does not conform to schema" (:message m))))
   v124_l578)))


(def v127_l598 (pj/plan (base-plot) {:validate false}))


(deftest
 t128_l600
 (is ((fn [plan] (and (map? plan) (= 600 (:width plan)))) v127_l598)))


(def
 v130_l624
 (pj/with-config
  {:strict false}
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options {:nonsense-key 42})
   pj/plot)))


(deftest
 t131_l630
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v130_l624)))


(def
 v133_l634
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
 t134_l642
 (is
  ((fn [msg] (and (string? msg) (re-find #"does not recognize" msg)))
   v133_l634)))
