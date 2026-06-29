(ns
 plotje-book.pose-model-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l46
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t4_l49
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v3_l46)))


(def
 v6_l54
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  kind/pprint))


(deftest
 t7_l58
 (is
  ((fn [v] (and (seq (:data v)) (= :sepal-length (:x (:mapping v)))))
   v6_l54)))


(def
 v9_l72
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})))


(deftest
 t10_l75
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v9_l72)))


(def
 v12_l79
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  kind/pprint))


(deftest
 t13_l83
 (is ((fn [v] (= :species (:color (:mapping v)))) v12_l79)))


(def
 v15_l101
 (def
  multi-layer
  (pj/pose
   {:mapping {:x :sepal-length, :y :sepal-width, :color :species},
    :layers
    [{:layer-type :point} {:layer-type :smooth, :stat :linear-model}],
    :data (rdatasets/datasets-iris)})))


(def v16_l108 multi-layer)


(deftest
 t17_l110
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v16_l108)))


(def v19_l116 (kind/pprint multi-layer))


(deftest
 t20_l118
 (is
  ((fn
    [v]
    (and (= 2 (count (:layers v))) (= :species (:color (:mapping v)))))
   v19_l116)))


(def
 v22_l124
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t23_l129
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v22_l124)))


(def
 v25_l137
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t26_l143
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:layers v)))
     (= :species (get-in v [:mapping :color]))))
   v25_l137)))


(def
 v28_l156
 (->
  {:x [1], :y [1]}
  (pj/lay-point :x :y {:size 30, :color "#a6cee3"})
  (pj/lay-point :x :y {:size 12, :color "#1f78b4"})))


(deftest
 t29_l160
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
   v28_l156)))


(def
 v31_l174
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t32_l180
 (is
  ((fn
    [fr]
    (=
     [:rect :text]
     (->> fr pj/plan :panels first :layers (mapv :mark))))
   v31_l174)))


(deftest
 t34_l188
 (is
  ((fn
    [_]
    (let
     [marks
      (fn [pose] (->> pose pj/plan :panels first :layers (mapv :mark)))
      data
      {:species ["setosa" "versicolor" "virginica"],
       :pct [33.3 33.3 33.3]}]
     (and
      (=
       [:rect :text]
       (marks
        (->
         data
         (pj/lay-bar :species :pct)
         (pj/lay-text :species :pct {:text :pct}))))
      (=
       [:text :rect]
       (marks
        (->
         data
         (pj/lay-text :species :pct {:text :pct})
         (pj/lay-bar :species :pct)))))))
   v31_l174)))


(def v36_l208 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t37_l211
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v36_l208)))


(def
 v39_l217
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length) kind/pprint))


(deftest t40_l221 (is ((fn [v] (empty? (:layers v))) v39_l217)))


(def
 v42_l245
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


(def v43_l254 two-panel)


(deftest
 t44_l256
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v43_l254)))


(def v46_l262 (kind/pprint two-panel))


(deftest
 t47_l264
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:poses v)))
     (= :horizontal (get-in v [:layout :direction]))))
   v46_l262)))


(def
 v49_l271
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
 t50_l279
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v49_l271)))


(def
 v52_l287
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
 t53_l296
 (is
  ((fn
    [v]
    (and
     (= :vertical (get-in v [:layout :direction]))
     (= 1 (count (:poses v)))
     (= 2 (count (:poses (first (:poses v)))))
     (= :horizontal (get-in v [:poses 0 :layout :direction]))))
   v52_l287)))
