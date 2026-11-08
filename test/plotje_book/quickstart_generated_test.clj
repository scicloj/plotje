(ns
 plotje-book.quickstart-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l37
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t4_l40
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v3_l37)))


(def
 v6_l54
 (->
  {:height [1 2 3 4 5], :weight [2 4 3 5 4]}
  (pj/lay-point :height :weight)))


(deftest
 t7_l57
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v6_l54)))


(def
 v9_l63
 (-> {:weight [1 2 3 4 5], :height [2 4 3 5 4]} pj/lay-point))


(deftest
 t10_l66
 (is
  ((fn
    [v]
    (and
     (= 5 (:points (pj/svg-summary v)))
     (= "weight" (:x-label (pj/plan v)))))
   v9_l63)))


(def
 v12_l79
 (->
  {"height" [1 2 3 4 5], "weight" [2 4 3 5 4]}
  (pj/lay-point "height" "weight")))


(deftest
 t13_l82
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v12_l79)))


(def
 v15_l88
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t16_l91
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"setosa"} (:texts s))
      (some #{"sepal length"} (:texts s)))))
   v15_l88)))


(def
 v18_l102
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t19_l105
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)) (zero? (:points s)))))
   v18_l102)))


(def v21_l112 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t22_l115
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v21_l112)))


(def
 v24_l121
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t25_l125
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v24_l121)))


(def
 v27_l130
 (->
  {:day [1 2 3 4 5 6 7 8], :visits [3 5 4 7 6 8 7 9]}
  (pj/lay-line :day :visits)))


(deftest
 t28_l134
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (zero? (:points s)))))
   v27_l130)))


(def
 v30_l140
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t31_l143
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:lines s)))))
   v30_l140)))


(def
 v33_l156
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t34_l159
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v33_l156)))


(def v36_l163 (-> (rdatasets/datasets-iris) (pj/pose :species)))


(deftest
 t37_l166
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v36_l163)))


(def v39_l170 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t40_l173
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v39_l170)))


(def
 v42_l185
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t43_l190
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v42_l185)))


(def
 v45_l198
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})
  (pj/options
   {:title "Iris Petals",
    :x-label "Petal Length (cm)",
    :y-label "Petal Width (cm)"})))


(deftest
 t46_l204
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Petals"} (:texts s))
      (some #{"Petal Length (cm)"} (:texts s)))))
   v45_l198)))


(def
 v48_l214
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t49_l218
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v48_l214)))


(def
 v51_l224
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})))


(deftest
 t52_l229
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v51_l224)))


(def
 v54_l240
 (pj/arrange
  [(pj/lay-point
    (rdatasets/datasets-iris)
    :sepal-length
    :sepal-width
    {:color :species})
   (pj/lay-histogram
    (rdatasets/datasets-iris)
    :sepal-length
    {:color :species})]
  {:cols 2}))


(deftest
 t55_l244
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v54_l240)))


(def
 v57_l257
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  pj/plot))


(deftest
 t58_l261
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v57_l257)))


(def
 v60_l269
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  pj/plot
  kind/pprint))


(deftest
 t61_l274
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v60_l269)))


(def
 v63_l281
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/save "/tmp/iris-scatter.svg")))


(deftest
 t64_l285
 (is ((fn [p] (and (instance? java.io.File p) (.exists p))) v63_l281)))
