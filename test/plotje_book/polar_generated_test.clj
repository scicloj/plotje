(ns
 plotje-book.polar-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l26
 (def
  wind
  {:direction ["N" "NE" "E" "SE" "S" "SW" "W" "NW"],
   :speed [12 8 15 10 7 13 9 11]}))


(def
 v4_l37
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :polar)))


(deftest
 t5_l41
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
   v4_l37)))


(def
 v7_l55
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t8_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v7_l55)))


(def v10_l68 (-> wind (pj/lay-bar :direction :speed) (pj/coord :polar)))


(deftest
 t11_l72
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v10_l68)))


(def
 v13_l83
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})
  (pj/coord :polar)))


(deftest
 t14_l87
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v13_l83)))


(def
 v16_l96
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length)
  (pj/coord :polar)))


(deftest
 t17_l100
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v16_l96)))


(def
 v19_l110
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :polar)
  (pj/options
   {:title "Iris in Polar Space",
    :x-label "Sepal length (angle)",
    :y-label "Sepal width (radius)"})))


(deftest
 t20_l117
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (every?
       (set (:texts s))
       ["Iris in Polar Space"
        "Sepal length (angle)"
        "Sepal width (radius)"]))))
   v19_l110)))
