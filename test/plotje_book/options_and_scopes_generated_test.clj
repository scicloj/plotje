(ns
 plotje-book.options-and-scopes-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l44
 (defn
  strip-data
  [pose]
  (cond->
   (dissoc pose :data)
   (:layers pose)
   (update
    :layers
    (partial mapv (fn* [p1__86151#] (dissoc p1__86151# :data))))
   (:poses pose)
   (update :poses (partial mapv strip-data)))))


(def
 v4_l49
 (defn
  pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose))))


(def
 v6_l86
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :loess, :bandwidth 0.5})
  pose-summary))


(deftest
 t7_l91
 (is
  ((fn
    [m]
    (and
     (= :loess (get-in m [:layers 0 :stat]))
     (= {:bandwidth 0.5} (get-in m [:layers 0 :mapping]))
     (let
      [layer
       (->
        (rdatasets/datasets-iris)
        (pj/pose :sepal-length :sepal-width)
        (pj/lay-point {:data (rdatasets/datasets-iris)})
        :layers
        first)]
      (and
       (contains? layer :data)
       (not (contains? (:mapping layer) :data))))))
   v6_l86)))


(def
 v9_l107
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})))


(deftest
 t10_l111
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v9_l107)))


(def
 v12_l115
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  pose-summary))


(deftest
 t13_l120
 (is
  ((fn [m] (= :species (get-in m [:layers 0 :mapping :color])))
   v12_l115)))


(def
 v15_l201
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})
  (pj/coord :flip)))


(deftest
 t16_l207
 (is ((fn [v] (some #{"Iris"} (:texts (pj/svg-summary v)))) v15_l201)))


(def
 v18_l211
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})
  (pj/coord :flip)
  pose-summary))


(deftest
 t19_l218
 (is
  ((fn
    [m]
    (and
     (= "Iris" (get-in m [:opts :title]))
     (= :flip (get-in m [:opts :coord]))))
   v18_l211)))


(def v21_l290 (select-keys (pj/config) [:width :height :margin]))


(deftest
 t22_l292
 (is
  ((fn
    [m]
    (and
     (number? (:width m))
     (number? (:height m))
     (number? (:margin m))))
   v21_l290)))


(def
 v24_l303
 (def
  demo
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:color :species})
   (pj/options {:title "Iris measurements"})
   (pj/coord :flip))))


(def v26_l314 demo)


(deftest
 t27_l316
 (is
  ((fn [v] (some #{"Iris measurements"} (:texts (pj/svg-summary v))))
   v26_l314)))


(def v29_l320 (pose-summary demo))


(deftest
 t30_l322
 (is
  ((fn
    [m]
    (and
     (= :species (get-in m [:layers 0 :mapping :color]))
     (= "Iris measurements" (get-in m [:opts :title]))
     (= :flip (get-in m [:opts :coord]))))
   v29_l320)))
