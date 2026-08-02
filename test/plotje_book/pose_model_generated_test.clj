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
 v9_l71
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})))


(deftest
 t10_l74
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v9_l71)))


(def
 v12_l78
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  kind/pprint))


(deftest
 t13_l82
 (is ((fn [v] (= :species (:color (:mapping v)))) v12_l78)))


(def
 v15_l100
 (def
  multi-layer
  (pj/pose
   {:mapping {:x :sepal-length, :y :sepal-width, :color :species},
    :layers
    [{:layer-type :point} {:layer-type :smooth, :stat :linear-model}],
    :data (rdatasets/datasets-iris)})))


(def v16_l107 multi-layer)


(deftest
 t17_l109
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v16_l107)))


(def v19_l115 (kind/pprint multi-layer))


(deftest
 t20_l117
 (is
  ((fn
    [v]
    (and (= 2 (count (:layers v))) (= :species (:color (:mapping v)))))
   v19_l115)))


(def
 v22_l123
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t23_l128
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v22_l123)))


(def
 v25_l136
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t26_l142
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:layers v)))
     (= :species (get-in v [:mapping :color]))))
   v25_l136)))


(def
 v28_l155
 (->
  {:x [1], :y [1]}
  (pj/lay-point :x :y {:size 30, :color "#a6cee3"})
  (pj/lay-point :x :y {:size 12, :color "#1f78b4"})))


(deftest
 t29_l159
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
   v28_l155)))


(def
 v31_l173
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t32_l179
 (is
  ((fn
    [fr]
    (=
     [:rect :text]
     (->> fr pj/plan :panels first :layers (mapv :mark))))
   v31_l173)))


(def
 v34_l189
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/coord :flip)))


(deftest
 t35_l195
 (is
  ((fn
    [fr]
    (=
     [:text :rect]
     (->> fr pj/plan :panels first :layers (mapv :mark))))
   v34_l189)))


(def v37_l205 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t38_l208
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v37_l205)))


(def
 v40_l214
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length) kind/pprint))


(deftest t41_l218 (is ((fn [v] (empty? (:layers v))) v40_l214)))


(def
 v43_l243
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


(def v44_l252 two-panel)


(deftest
 t45_l254
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v44_l252)))


(def v47_l260 (kind/pprint two-panel))


(deftest
 t48_l262
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:poses v)))
     (= :horizontal (get-in v [:layout :direction]))))
   v47_l260)))


(def
 v50_l269
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
 t51_l277
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v50_l269)))


(def
 v53_l285
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
 t54_l294
 (is
  ((fn
    [v]
    (and
     (= :vertical (get-in v [:layout :direction]))
     (= 1 (count (:poses v)))
     (= 2 (count (:poses (first (:poses v)))))
     (= :horizontal (get-in v [:poses 0 :layout :direction]))))
   v53_l285)))
