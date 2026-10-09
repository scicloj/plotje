(ns
 pr61-cells-draft-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l16
 (def
  scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width {:color :species})
   pj/lay-point)))


(def v5_l23 (def by-facet (pj/facet scatter :species)))


(def v6_l26 by-facet)


(def
 v8_l30
 (def
  by-columns
  (pj/arrange
   scatter
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])))


(def v9_l34 by-columns)


(def
 v11_l38
 (def
  by-hand
  (pj/arrange
   (vec
    (for
     [s ["setosa" "versicolor" "virginica"]]
     (->
      (rdatasets/datasets-iris)
      (tc/select-rows (fn* [p1__74132#] (= s (:species p1__74132#))))
      (pj/pose :sepal-length :sepal-width {:color :species})
      pj/lay-point
      (pj/options {:title s})))))))


(def v12_l47 by-hand)


(def
 v14_l51
 (kind/table
  (for
   [[label v]
    [["pj/facet" by-facet]
     ["listed by hand" by-hand]
     ["columns" by-columns]]]
   (let
    [s (pj/svg-summary v)]
    {:route label,
     :panels (:panels s),
     :points (:points s),
     :composite? (contains? v :poses)}))))


(deftest
 t15_l59
 (is
  ((fn
    [_]
    (let
     [f (pj/svg-summary by-facet) h (pj/svg-summary by-hand)]
     (and
      (= 3 (:panels f) (:panels h))
      (= 150 (:points f) (:points h))
      (= 1 (count (filter #{"sepal length"} (:texts f))))
      (= 3 (count (filter #{"sepal length"} (:texts h))))
      (not (contains? by-facet :poses))
      (contains? by-hand :poses))))
   v14_l51)))


(def
 v17_l72
 (defn
  expand
  [pose variation]
  (if
   (map? variation)
   (let
    [[k cols] (first variation)]
    (vec (for [c cols] (-> pose (pj/pose {k c})))))
   (vec
    (for
     [v (distinct ((:data pose) variation))]
     (->
      pose
      (update
       :data
       tc/select-rows
       (fn* [p1__74133#] (= v (get p1__74133# variation))))
      (pj/options {:title (str v)})))))))


(def v18_l84 (pj/arrange (expand scatter :species)))


(deftest
 t19_l86
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v18_l84)))


(def
 v20_l90
 (pj/arrange
  (expand scatter {:x [:sepal-length :petal-length :petal-width]})))


(deftest
 t21_l92
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 450 (:points s)))))
   v20_l90)))
