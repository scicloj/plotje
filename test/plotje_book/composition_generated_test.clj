(ns
 plotje-book.composition-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l33
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]))


(deftest
 t4_l37
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v3_l33)))


(def
 v6_l44
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 1}))


(deftest
 t7_l49
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v6_l44)))


(def
 v9_l67
 (def
  weighted
  (pj/pose
   {:layout {:direction :horizontal, :weights [2 1]},
    :poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width},
      :layers [{:layer-type :point}]}],
    :data (rdatasets/datasets-iris)})))


(def v11_l79 weighted)


(deftest
 t12_l81
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v11_l79)))


(def v14_l89 (kind/pprint weighted))


(deftest
 t15_l91
 (is
  ((fn
    [pose]
    (and
     (= [2 1] (get-in pose [:layout :weights]))
     (= 2 (count (:poses pose)))))
   v14_l89)))


(def
 v17_l108
 (def
  shared-x
  (pj/pose
   {:share-scales #{:x},
    :layout {:direction :horizontal, :weights [1 1]},
    :poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :sepal-length, :y :petal-length},
      :layers [{:layer-type :point}]}],
    :data (rdatasets/datasets-iris)})))


(def v18_l118 shared-x)


(deftest
 t19_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v18_l118)))


(def
 v21_l143
 (def
  readings
  {:t [1 2 3 4 5],
   :rate [1.0 2.0 3.0 2.0 4.0],
   :total [1200000.0 2400000.0 1800000.0 3100000.0 2600000.0]}))


(def
 v22_l148
 (pj/arrange
  [(-> readings (pj/lay-line :t :rate))
   (-> readings (pj/lay-line :t :total))]
  {:cols 1, :share-scales #{:x}, :align-panels true}))


(deftest
 t23_l153
 (is
  ((fn
    [v]
    (let
     [pads-of
      (fn
       [pose]
       (mapv
        (fn*
         [p1__83207#]
         (get-in p1__83207# [:plan :layout :y-label-pad]))
        (:sub-plots (pj/plan pose))))
      plain
      (pads-of
       (pj/arrange
        [(-> readings (pj/lay-line :t :rate))
         (-> readings (pj/lay-line :t :total))]
        {:cols 1, :share-scales #{:x}}))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (apply == (pads-of v))
      (not (apply == plain)))))
   v22_l148)))


(def
 v25_l183
 (def
  marginal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/marginal :top))))


(def v26_l188 marginal)


(deftest
 t27_l190
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal)))
      panels
      (mapv (fn* [p1__83208#] (-> p1__83208# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)
      [d-y s-y]
      (mapv :y-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (pos? (:polygons s))
      (= d-x s-x)
      (not= d-y s-y)
      (= [] (:values (:x-ticks (first panels))))
      (nil? (:x-label (first plans)))
      (apply
       ==
       (map
        (fn* [p1__83209#] (get-in p1__83209# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__83210#] (get-in p1__83210# [:layout :legend-w]))
        plans)))))
   v26_l188)))


(def
 v29_l228
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.35})))


(deftest
 t30_l232
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v29_l228)))


(def
 v32_l244
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/marginal :right)))


(deftest
 t33_l248
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan v)))
      panels
      (mapv (fn* [p1__83211#] (-> p1__83211# :panels first)) plans)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels)))
      (= [] (:values (:y-ticks (second panels))))
      (nil? (:y-label (second plans)))
      (apply
       ==
       (map
        (fn* [p1__83212#] (get-in p1__83212# [:layout :x-label-pad]))
        plans)))))
   v32_l244)))


(def
 v35_l282
 (def
  marginal-by-hand
  (pj/pose
   {:share-scales #{:x},
    :layout {:direction :vertical, :weights [1 3]},
    :poses
    [{:mapping {:x :sepal-length},
      :opts {:suppress-x-ticks true, :suppress-x-label true},
      :layers [{:layer-type :density}]}
     {:mapping {:x :sepal-length, :y :sepal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data (rdatasets/datasets-iris)})))


(def v36_l293 marginal-by-hand)


(deftest
 t37_l295
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
      panels
      (mapv (fn* [p1__83213#] (-> p1__83213# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= d-x s-x)
      (=
       [0 102]
       (mapv
        (fn* [p1__83214#] (get-in p1__83214# [:layout :legend-w]))
        plans)))))
   v36_l293)))


(def v39_l324 (assoc-in marginal-by-hand [:opts :align-panels] true))


(deftest
 t40_l326
 (is
  ((fn
    [v]
    (let
     [plans (mapv :plan (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (apply
       ==
       (map
        (fn* [p1__83215#] (get-in p1__83215# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__83216#] (get-in p1__83216# [:layout :legend-w]))
        plans)))))
   v39_l324)))


(def
 v42_l351
 (def
  dashboard
  (pj/arrange
   [[(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))
     (->
      (rdatasets/datasets-iris)
      (pj/lay-boxplot :species :sepal-width {:color :species}))]
    [(->
      (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width {:color :species}))
     (->
      (rdatasets/datasets-iris)
      (pj/lay-density :petal-length {:color :species}))]])))


(def v43_l358 dashboard)


(deftest
 t44_l360
 (is
  ((fn
    [v]
    (let
     [chrome (-> dashboard pj/plan :chrome)]
     (and
      (= 4 (:panels (pj/svg-summary v)))
      (= #{} (:shared-aesthetics chrome)))))
   v43_l358)))


(def v46_l397 (def overlay-base {:fitted [1 2 3], :residual [1 2 3]}))


(def
 v47_l401
 (def overlay-other (tc/dataset {:x [0.5 1.5 2.5], :y [1.5 2.5 3.5]})))


(def
 v48_l405
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point
   :fitted
   :residual
   {:color "#e6550d",
    :data
    (tc/rename-columns overlay-other {:x :fitted, :y :residual})})))


(deftest
 t49_l412
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:points s)))))
   v48_l405)))


(def
 v51_l430
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  pj/overlay
  (pj/lay-point :x :y {:color "#e6550d", :data overlay-other})))


(deftest
 t52_l435
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      renamed
      (->
       overlay-base
       (pj/lay-point :fitted :residual {:color "#377eb8"})
       (pj/lay-point
        :fitted
        :residual
        {:color "#e6550d",
         :data
         (tc/rename-columns
          overlay-other
          {:x :fitted, :y :residual})}))]
     (and
      (= 1 (:panels s))
      (= 6 (:points s))
      (= (pj/plot renamed) (pj/plot v)))))
   v51_l430)))


(def
 v54_l470
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point :x :y {:color "#e6550d", :data overlay-other})))


(deftest
 t55_l474
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 6 (:points s))
      (= [2 2] ((juxt :n-rows :n-cols) (:chrome (pj/plan v))))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v54_l470)))


(def
 v57_l503
 (def
  bounded
  (->
   {:x [1 2 3 4 5], :y [10 20 15 25 18]}
   (pj/lay-point :x :y)
   (pj/lay-line {:data {:x [1 5], :y [-200 300]}})
   (pj/scale :y {:type :linear, :domain [0 30]}))))


(def v58_l509 (pj/arrange [bounded bounded] {:cols 1}))


(deftest
 t59_l511
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:clips s)))))
   v58_l509)))


(def
 v61_l562
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]))


(deftest
 t62_l568
 (is
  ((fn
    [v]
    (and
     (= #{:color} (-> v pj/plan :chrome :shared-aesthetics))
     (=
      #{}
      (->
       (pj/arrange
        [(->
          (rdatasets/datasets-iris)
          (pj/lay-histogram :sepal-length))
         (->
          (rdatasets/datasets-iris)
          (pj/lay-point
           :petal-length
           :petal-width
           {:color :species}))])
       pj/plan
       :chrome
       :shared-aesthetics))))
   v61_l562)))
