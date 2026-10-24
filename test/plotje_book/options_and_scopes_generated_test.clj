(ns
 plotje-book.options-and-scopes-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l42
 (defn
  strip-data
  [pose]
  (cond->
   (dissoc pose :data)
   (:layers pose)
   (update
    :layers
    (partial mapv (fn* [p1__77657#] (dissoc p1__77657# :data))))
   (:poses pose)
   (update :poses (partial mapv strip-data)))))


(def
 v4_l47
 (defn
  pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose))))


(def
 v6_l80
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})))


(deftest
 t7_l84
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v6_l80)))


(def
 v9_l88
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  pose-summary))


(deftest
 t10_l93
 (is
  ((fn [m] (= :species (get-in m [:layers 0 :mapping :color])))
   v9_l88)))


(def
 v12_l168
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})
  (pj/coord :flip)))


(deftest
 t13_l174
 (is ((fn [v] (some #{"Iris"} (:texts (pj/svg-summary v)))) v12_l168)))


(def
 v15_l178
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})
  (pj/coord :flip)
  pose-summary))


(deftest
 t16_l185
 (is
  ((fn
    [m]
    (and
     (= "Iris" (get-in m [:opts :title]))
     (= :flip (get-in m [:opts :coord]))))
   v15_l178)))


(def v18_l257 (select-keys (pj/config) [:width :height :margin]))


(deftest
 t19_l259
 (is
  ((fn
    [m]
    (and
     (number? (:width m))
     (number? (:height m))
     (number? (:margin m))))
   v18_l257)))


(def
 v21_l270
 (def
  demo
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:color :species})
   (pj/options {:title "Iris measurements"})
   (pj/coord :flip))))


(def v23_l281 demo)


(deftest
 t24_l283
 (is
  ((fn [v] (some #{"Iris measurements"} (:texts (pj/svg-summary v))))
   v23_l281)))


(def v26_l287 (pose-summary demo))


(deftest
 t27_l289
 (is
  ((fn
    [m]
    (and
     (= :species (get-in m [:layers 0 :mapping :color]))
     (= "Iris measurements" (get-in m [:opts :title]))
     (= :flip (get-in m [:opts :coord]))))
   v26_l287)))
