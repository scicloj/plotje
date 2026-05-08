(ns
 plotje-book.quickstart-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l32
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t4_l35
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v3_l32)))


(def v6_l49 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-point :x :y)))


(deftest
 t7_l52
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v6_l49)))


(def v9_l57 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t10_l60
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v9_l57)))


(def
 v12_l71
 (-> {"x" [1 2 3 4 5], "y" [2 4 3 5 4]} (pj/lay-point "x" "y")))


(deftest
 t13_l74
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v12_l71)))


(def
 v15_l80
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t16_l83
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"setosa"} (:texts s))
      (some #{"sepal length"} (:texts s)))))
   v15_l80)))


(def
 v18_l94
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t19_l97
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)) (zero? (:points s)))))
   v18_l94)))


(def v21_l104 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t22_l107
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v21_l104)))


(def
 v24_l113
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t25_l117
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v24_l113)))


(def
 v27_l122
 (-> {:x [1 2 3 4 5 6 7 8], :y [3 5 4 7 6 8 7 9]} (pj/lay-line :x :y)))


(deftest
 t28_l126
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (zero? (:points s)))))
   v27_l122)))


(def
 v30_l132
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t31_l135
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:lines s)))))
   v30_l132)))


(def
 v33_l148
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t34_l151
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v33_l148)))


(def v36_l155 (-> (rdatasets/datasets-iris) (pj/pose :species)))


(deftest
 t37_l158
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v36_l155)))


(def v39_l162 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t40_l165
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v39_l162)))


(def
 v42_l176
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t43_l181
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v42_l176)))


(def
 v45_l189
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})
  (pj/options
   {:width 500,
    :height 350,
    :title "Iris Petals",
    :x-label "Petal Length (cm)",
    :y-label "Petal Width (cm)"})))


(deftest
 t46_l196
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Petals"} (:texts s))
      (some #{"Petal Length (cm)"} (:texts s)))))
   v45_l189)))


(def
 v48_l205
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


(deftest t49_l209 (is ((fn [v] (pj/pose? v)) v48_l205)))


(def
 v51_l222
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  pj/plot))


(deftest
 t52_l226
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v51_l222)))


(def
 v54_l234
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  pj/plot
  kind/pprint))


(deftest
 t55_l239
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v54_l234)))


(def
 v57_l245
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/save "/tmp/iris-scatter.svg")))


(deftest
 t58_l249
 (is ((fn [p] (and (string? p) (.endsWith p ".svg"))) v57_l245)))
