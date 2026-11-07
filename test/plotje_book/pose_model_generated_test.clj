(ns
 plotje-book.pose-model-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l45
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t4_l48
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v3_l45)))


(def
 v6_l53
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  kind/pprint))


(deftest
 t7_l57
 (is
  ((fn [v] (and (seq (:data v)) (= :sepal-length (:x (:mapping v)))))
   v6_l53)))


(def
 v9_l73
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})))


(deftest
 t10_l76
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v9_l73)))


(def
 v12_l80
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  kind/pprint))


(deftest
 t13_l84
 (is ((fn [v] (= :species (:color (:mapping v)))) v12_l80)))


(def
 v15_l105
 (def
  multi-layer
  (pj/pose
   {:mapping {:x :sepal-length, :y :sepal-width, :color :species},
    :layers
    [{:layer-type :point} {:layer-type :smooth, :stat :linear-model}],
    :data (rdatasets/datasets-iris)})))


(def v16_l112 multi-layer)


(deftest
 t17_l114
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v16_l112)))


(def v19_l120 (kind/pprint multi-layer))


(deftest
 t20_l122
 (is
  ((fn
    [v]
    (and (= 2 (count (:layers v))) (= :species (:color (:mapping v)))))
   v19_l120)))


(def
 v22_l128
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t23_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v22_l128)))


(def
 v25_l141
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t26_l147
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:layers v)))
     (= :species (get-in v [:mapping :color]))))
   v25_l141)))


(def
 v28_l160
 (->
  {:height [1], :weight [1]}
  (pj/lay-point :height :weight {:size 30, :color "#a6cee3"})
  (pj/lay-point :height :weight {:size 12, :color "#1f78b4"})))


(deftest
 t29_l164
 (is
  ((fn
    [fr]
    (=
     [30 12]
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      (mapv (comp :radius :style)))))
   v28_l160)))


(def
 v31_l179
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t32_l185
 (is
  ((fn
    [fr]
    (=
     [:rect :text]
     (->> fr pj/plan :panels first :layers (mapv :mark))))
   v31_l179)))


(def
 v34_l194
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/coord :flip)))


(deftest
 t35_l200
 (is
  ((fn
    [fr]
    (=
     [:text :rect]
     (->> fr pj/plan :panels first :layers (mapv :mark))))
   v34_l194)))


(def v37_l210 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t38_l213
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v37_l210)))


(def
 v40_l219
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length) kind/pprint))


(deftest t41_l223 (is ((fn [v] (empty? (:layers v))) v40_l219)))


(def
 v43_l249
 (def
  two-panel
  (pj/pose
   {:layout {:direction :horizontal},
    :poses
    [{:mapping {:x :sepal-length, :y :sepal-width, :color :species},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data (rdatasets/datasets-iris)})))


(def v44_l258 two-panel)


(deftest
 t45_l260
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v44_l258)))


(def v47_l266 (kind/pprint two-panel))


(deftest
 t48_l268
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:poses v)))
     (= :horizontal (get-in v [:layout :direction]))))
   v47_l266)))


(def
 v50_l275
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point)
   (->
    (rdatasets/datasets-iris)
    (pj/pose :petal-length :petal-width {:color :species})
    pj/lay-point)]))


(deftest
 t51_l283
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v50_l275)))


(def
 v53_l292
 (->
  (pj/arrange
   [(->
     (rdatasets/datasets-iris)
     (pj/pose :sepal-length :sepal-width {:color :species})
     pj/lay-point)
    (->
     (rdatasets/datasets-iris)
     (pj/pose :petal-length :petal-width {:color :species})
     pj/lay-point)])
  kind/pprint))


(deftest
 t54_l301
 (is
  ((fn
    [v]
    (and
     (= :vertical (get-in v [:layout :direction]))
     (= 1 (count (:poses v)))
     (= 2 (count (:poses (first (:poses v)))))
     (= :horizontal (get-in v [:poses 0 :layout :direction]))
     (nil? (:data v))
     (every? (comp some? :data) (:poses (first (:poses v))))))
   v53_l292)))
