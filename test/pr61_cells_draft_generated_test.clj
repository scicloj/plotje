(ns
 pr61-cells-draft-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.pose :as pose]
  [clojure.test :refer [deftest is]]))


(def
 v3_l18
 (def
  scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width {:color :species})
   pj/lay-point)))


(def v5_l25 (def by-facet (pj/facet scatter :species)))


(def v6_l28 by-facet)


(def
 v8_l32
 (def
  by-columns
  (pj/arrange
   scatter
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])))


(def v9_l36 by-columns)


(def
 v11_l40
 (def
  by-hand
  (pj/arrange
   (vec
    (for
     [s ["setosa" "versicolor" "virginica"]]
     (->
      (rdatasets/datasets-iris)
      (tc/select-rows (fn* [p1__11193#] (= s (:species p1__11193#))))
      (pj/pose :sepal-length :sepal-width {:color :species})
      pj/lay-point
      (pj/options {:title s})))))))


(def v12_l49 by-hand)


(def
 v14_l53
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
 t15_l61
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
   v14_l53)))


(def v17_l74 (def expand pose/expand-variation))


(def v18_l76 (expand scatter :species))


(deftest
 t19_l78
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v18_l76)))


(def
 v20_l82
 (->
  (expand scatter :species)
  (update
   :poses
   (fn* [p1__11194#] (mapv (fn [c] (dissoc c :opts)) p1__11194#)))
  (dissoc :data :layers)
  kind/pprint))


(def
 v21_l87
 (expand scatter {:x [:sepal-length :petal-length :petal-width]}))


(deftest
 t22_l89
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 450 (:points s)))))
   v21_l87)))


(def
 v24_l95
 (def mpg-points (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)))


(def v25_l97 (pj/facet-grid mpg-points :drv :cyl))


(def v26_l99 (expand mpg-points {:col :drv, :row :cyl}))


(deftest
 t27_l101
 (is
  ((fn
    [v]
    (=
     (:points (pj/svg-summary v))
     (:points (pj/svg-summary (pj/facet-grid mpg-points :drv :cyl)))))
   v26_l99)))


(def
 v29_l107
 (def
  listed-with-filters
  (->
   scatter
   (assoc
    :layout
    {:direction :horizontal}
    :share-scales
    #{:y :x}
    :poses
    [{:where {:species "setosa"}}
     {:where {:species "versicolor"}}
     {:where {:species "virginica"}}]))))


(def v30_l115 listed-with-filters)


(deftest
 t31_l117
 (is
  ((fn
    [v]
    (let
     [titles
      (fn
       [p]
       (frequencies
        (filter
         #{"sepal length" "sepal width"}
         (:texts (pj/svg-summary p)))))]
     (and
      (=
       (select-keys (pj/svg-summary v) [:panels :points])
       (select-keys (pj/svg-summary by-facet) [:panels :points]))
      (= (titles by-facet) (titles v)))))
   v30_l115)))


(def
 v33_l126
 (->
  scatter
  (assoc
   :layout
   {:direction :horizontal}
   :poses
   [{:where {:species "setosa"}}
    {:where {:species "no such species"}}
    {:where {:species #{"versicolor" "virginica"}}}])))


(deftest
 t34_l132
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v33_l126)))
