(ns
 plotje-book.polar-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l24
 (def
  wind
  {:direction ["N" "NE" "E" "SE" "S" "SW" "W" "NW"],
   :speed [12 8 15 10 7 13 9 11]}))


(def
 v4_l35
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :polar)))


(deftest
 t5_l39
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      lengths
      ((rdatasets/datasets-iris) :sepal-length)]
     (and
      (= 1 (:panels s))
      (= 150 (:points s))
      (= [4.3 7.9] [(reduce min lengths) (reduce max lengths)]))))
   v4_l35)))


(def
 v7_l53
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t8_l57
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v7_l53)))


(def v10_l66 (-> wind (pj/lay-bar :direction :speed) (pj/coord :polar)))


(deftest
 t11_l70
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v10_l66)))


(def
 v13_l81
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})
  (pj/coord :polar)))


(deftest
 t14_l85
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v13_l81)))


(def
 v16_l94
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length)
  (pj/coord :polar)))


(deftest
 t17_l98
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v16_l94)))


(def
 v19_l107
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :polar)
  (pj/options {:title "Iris in Polar Space"})))


(deftest
 t20_l112
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (some #{"Iris in Polar Space"} (:texts s)))))
   v19_l107)))
