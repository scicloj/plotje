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


(def v21_l141 (def limit 8.5))


(def
 v22_l143
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__73348#] (= "setosa" (:species p1__73348#))))
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-v {:x-intercept limit, :color "firebrick"}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__73349#] (= "virginica" (:species p1__73349#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t23_l153
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__73350#] (:panels (:plan p1__73350#)))
       (:sub-plots (pj/plan v)))
      domains
      (mapv
       (fn* [p1__73351#] (mapv double (:x-domain p1__73351#)))
       panels)]
     (and
      (= 2 (count domains))
      (apply = domains)
      (< limit (second (first domains))))))
   v22_l143)))


(def
 v25_l173
 (def
  readings
  {:t [1 2 3 4 5],
   :rate [1.0 2.0 3.0 2.0 4.0],
   :total [1200000.0 2400000.0 1800000.0 3100000.0 2600000.0]}))


(def
 v26_l178
 (pj/arrange
  [(-> readings (pj/lay-line :t :rate))
   (-> readings (pj/lay-line :t :total))]
  {:cols 1, :share-scales #{:x}, :align-panels true}))


(deftest
 t27_l183
 (is
  ((fn
    [v]
    (let
     [pads-of
      (fn
       [pose]
       (mapv
        (fn*
         [p1__73352#]
         (get-in p1__73352# [:plan :layout :y-label-pad]))
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
   v26_l178)))


(def
 v29_l213
 (def
  marginal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/marginal :top))))


(def v30_l218 marginal)


(deftest
 t31_l220
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal)))
      panels
      (mapv (fn* [p1__73353#] (-> p1__73353# :panels first)) plans)
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
        (fn* [p1__73354#] (get-in p1__73354# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__73355#] (get-in p1__73355# [:layout :legend-w]))
        plans)))))
   v30_l218)))


(def
 v33_l258
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.35})))


(deftest
 t34_l262
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v33_l258)))


(def
 v36_l274
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/marginal :right)))


(deftest
 t37_l278
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan v)))
      panels
      (mapv (fn* [p1__73356#] (-> p1__73356# :panels first)) plans)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels)))
      (= [] (:values (:y-ticks (second panels))))
      (nil? (:y-label (second plans)))
      (apply
       ==
       (map
        (fn* [p1__73357#] (get-in p1__73357# [:layout :x-label-pad]))
        plans)))))
   v36_l274)))


(def
 v39_l312
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


(def v40_l323 marginal-by-hand)


(deftest
 t41_l325
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
      panels
      (mapv (fn* [p1__73358#] (-> p1__73358# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= d-x s-x)
      (=
       [0 102]
       (mapv
        (fn* [p1__73359#] (get-in p1__73359# [:layout :legend-w]))
        plans)))))
   v40_l323)))


(def v43_l354 (assoc-in marginal-by-hand [:opts :align-panels] true))


(deftest
 t44_l356
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
        (fn* [p1__73360#] (get-in p1__73360# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__73361#] (get-in p1__73361# [:layout :legend-w]))
        plans)))))
   v43_l354)))


(def
 v46_l381
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


(def v47_l388 dashboard)


(deftest
 t48_l390
 (is
  ((fn
    [v]
    (let
     [chrome (-> dashboard pj/plan :chrome)]
     (and
      (= 4 (:panels (pj/svg-summary v)))
      (= #{} (:shared-aesthetics chrome)))))
   v47_l388)))


(def v50_l427 (def overlay-base {:fitted [1 2 3], :residual [1 2 3]}))


(def
 v51_l431
 (def overlay-other (tc/dataset {:x [0.5 1.5 2.5], :y [1.5 2.5 3.5]})))


(def
 v52_l435
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
 t53_l442
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:points s)))))
   v52_l435)))


(def
 v55_l460
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  pj/overlay
  (pj/lay-point :x :y {:color "#e6550d", :data overlay-other})))


(deftest
 t56_l465
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
   v55_l460)))


(def
 v58_l501
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point :x :y {:color "#e6550d", :data overlay-other})))


(deftest
 t59_l505
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 6 (:points s))
      (= [[0 0] [1 1]] (mapv (juxt :row :col) (:panels (pj/plan v))))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v58_l501)))


(def
 v61_l535
 (def
  bounded
  (->
   {:x [1 2 3 4 5], :y [10 20 15 25 18]}
   (pj/lay-point :x :y)
   (pj/lay-line {:data {:x [1 5], :y [-200 300]}})
   (pj/scale :y {:type :linear, :domain [0 30]}))))


(def v62_l541 (pj/arrange [bounded bounded] {:cols 1}))


(deftest
 t63_l543
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:clips s)))))
   v62_l541)))


(def
 v65_l595
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]))


(deftest
 t66_l601
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
   v65_l595)))


(def
 v68_l616
 (pj/arrange
  [(pj/arrange
    [(->
      (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width))
     (->
      (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width))]
    {:cols 1})
   (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))]))


(deftest
 t69_l626
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v68_l616)))
