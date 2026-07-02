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
       (fn* [p1__82920#] (= "Distribution of Total Bill" p1__82920#))
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
 v30_l150
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color "lightblue", :stroke "black"})))


(deftest
 t31_l153
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)) (= 1 (:lines s)))))
   v30_l150)))


(def
 v33_l165
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length)
  pj/lay-rug))


(deftest
 t34_l169
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)) (= 150 (:lines s)))))
   v33_l165)))


(def
 v36_l181
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter true})))


(deftest
 t37_l184
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v36_l181)))


(def
 v39_l190
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter 10, :alpha 0.5})))


(deftest
 t40_l193
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v39_l190)))


(def
 v42_l201
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t43_l204
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v42_l201)))


(deftest
 t45_l214
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
        (fn* [p1__82921#] (= :boxplot (:mark p1__82921#)))
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
   v42_l201)))


(def
 v47_l241
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-boxplot :day :total-bill {:color :smoker})))


(deftest
 t49_l247
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
        (fn* [p1__82922#] (= :boxplot (:mark p1__82922#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (pos? (:lines s))
      (= 2 (count (:color-categories box-layer))))))
   v47_l241)))


(def
 v51_l263
 (->
  (rdatasets/datasets-iris)
  (pj/lay-boxplot :species :sepal-width)
  (pj/coord :flip)))


(deftest
 t52_l267
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v51_l263)))


(def
 v54_l278
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t55_l281
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v54_l278)))


(def
 v57_l290
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-violin :day :total-bill {:color :smoker})))


(deftest
 t59_l296
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
        (fn* [p1__82923#] (= :violin (:mark p1__82923#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (= 2 (count (:color-categories viol-layer))))))
   v57_l290)))


(def
 v61_l309
 (->
  (rdatasets/datasets-iris)
  (pj/lay-violin :species :petal-length)
  (pj/coord :flip)))


(deftest
 t62_l313
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v61_l309)))


(def
 v64_l323
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t65_l326
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v64_l323)))


(def
 v67_l335
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length {:color :species})))


(deftest
 t68_l338
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v67_l335)))


(def
 v70_l349
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]))


(deftest
 t71_l351
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v70_l349)))


(def
 v73_l358
 (pj/lay-density
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]
  {:color :species}))


(deftest
 t74_l360
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v73_l358)))
