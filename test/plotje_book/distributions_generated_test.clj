(ns
 plotje-book.distributions-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l19
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t4_l22
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v3_l19)))


(def
 v6_l31
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})))


(deftest
 t7_l34
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v6_l31)))


(def
 v9_l43
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :petal-width)))


(deftest
 t10_l46
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v9_l43)))


(def
 v12_l53
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-histogram :total-bill)
  (pj/options
   {:title "Distribution of Total Bill", :x-label "Amount ($)"})))


(deftest
 t13_l58
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (pos? (:polygons s))
      (some
       (fn* [p1__94083#] (= "Distribution of Total Bill" p1__94083#))
       (:texts s)))))
   v12_l53)))


(def
 v15_l70
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:normalize :density, :alpha 0.5})
  pj/lay-density))


(deftest
 t16_l74
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v15_l70)))


(def
 v18_l91
 (->
  {:x (mapcat (fn [i] (repeat (long (Math/pow 2 i)) i)) (range 10))}
  (pj/lay-histogram {:bins 10})
  (pj/scale :y :log)
  (pj/options {:title "Log Y on Histogram"})))


(deftest
 t19_l96
 (is
  ((fn
    [v]
    (let
     [panel (-> v pj/plan :panels first) [lo hi] (:y-domain panel)]
     (and
      (= :log (:type (:y-scale panel)))
      (pos? lo)
      (< lo 1.0)
      (< 500.0 hi 2000.0))))
   v18_l91)))


(def
 v21_l110
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t22_l113
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v21_l110)))


(def
 v24_l122
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t25_l125
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v24_l122)))


(def
 v27_l134
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:bandwidth 0.3})))


(deftest
 t28_l137
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v27_l134)))


(def
 v30_l148
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length)
  pj/lay-rug))


(deftest
 t31_l152
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)) (= 150 (:lines s)))))
   v30_l148)))


(def
 v33_l164
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter true})))


(deftest
 t34_l167
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v33_l164)))


(def
 v36_l173
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter 10, :alpha 0.5})))


(deftest
 t37_l176
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v36_l173)))


(def
 v39_l184
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t40_l187
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v39_l184)))


(deftest
 t42_l197
 (is
  ((fn
    [_]
    (let
     [plan
      (->
       (rdatasets/datasets-iris)
       (pj/lay-boxplot :species :sepal-width)
       pj/plan)
      box-layer
      (first
       (filter
        (fn* [p1__94084#] (= :boxplot (:mark p1__94084#)))
        (:layers (first (:panels plan)))))
      results
      (mapv
       (fn
        [{:keys [q1 q3 whisker-lo whisker-hi outliers]}]
        (let
         [iqr
          (- q3 q1)
          lo-fence
          (- q1 (* 1.5 iqr))
          hi-fence
          (+ q3 (* 1.5 iqr))]
         {:whisker-lo-in-fence (>= whisker-lo lo-fence),
          :whisker-hi-in-fence (<= whisker-hi hi-fence),
          :outliers-outside-fence
          (every?
           (fn [o] (or (< o lo-fence) (> o hi-fence)))
           outliers)}))
       (:boxes box-layer))]
     (and
      (= 3 (count results))
      (every?
       (fn
        [r]
        (and
         (:whisker-lo-in-fence r)
         (:whisker-hi-in-fence r)
         (:outliers-outside-fence r)))
       results))))
   v39_l184)))


(def
 v44_l224
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-boxplot :day :total-bill {:color :smoker})))


(deftest
 t46_l230
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/reshape2-tips)
        (pj/lay-boxplot :day :total-bill {:color :smoker})))
      box-layer
      (first
       (filter
        (fn* [p1__94085#] (= :boxplot (:mark p1__94085#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (pos? (:lines s))
      (= 2 (count (:color-categories box-layer))))))
   v44_l224)))


(def
 v48_l246
 (->
  (rdatasets/datasets-iris)
  (pj/lay-boxplot :species :sepal-width)
  (pj/coord :flip)))


(deftest
 t49_l250
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v48_l246)))


(def
 v51_l261
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t52_l264
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v51_l261)))


(def
 v54_l273
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-violin :day :total-bill {:color :smoker})))


(deftest
 t56_l279
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/reshape2-tips)
        (pj/lay-violin :day :total-bill {:color :smoker})))
      viol-layer
      (first
       (filter
        (fn* [p1__94086#] (= :violin (:mark p1__94086#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (= 2 (count (:color-categories viol-layer))))))
   v54_l273)))


(def
 v58_l292
 (->
  (rdatasets/datasets-iris)
  (pj/lay-violin :species :petal-length)
  (pj/coord :flip)))


(deftest
 t59_l296
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v58_l292)))


(def
 v61_l306
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t62_l309
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v61_l306)))


(def
 v64_l318
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length {:color :species})))


(deftest
 t65_l321
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v64_l318)))


(def
 v67_l332
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]))


(deftest
 t68_l334
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v67_l332)))


(def
 v70_l341
 (pj/lay-density
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]
  {:color :species}))


(deftest
 t71_l343
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v70_l341)))
