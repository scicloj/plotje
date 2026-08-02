(ns
 plotje-book.datasets-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def
 v3_l49
 (->
  [{:month "Jan", :temperature 5}
   {:month "Feb", :temperature 7}
   {:month "Mar", :temperature 12}
   {:month "Apr", :temperature 16}]
  (pj/lay-line :month :temperature)
  pj/lay-point))


(deftest
 t4_l56
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v3_l49)))


(def v6_l77 (tc/dataset {:x [1 2 3 4 5], :y [10 20 15 30 25]}))


(deftest t7_l80 (is ((fn [ds] (= 5 (tc/row-count ds))) v6_l77)))


(def
 v9_l84
 (tc/dataset
  [{:name "Alice", :score 92}
   {:name "Bob", :score 85}
   {:name "Carol", :score 97}]))


(deftest t10_l88 (is ((fn [ds] (= 3 (tc/row-count ds))) v9_l84)))


(def
 v12_l92
 (tc/dataset
  [["Alice" 92] ["Bob" 85] ["Carol" 97]]
  {:column-names [:name :score]}))


(deftest t13_l97 (is ((fn [ds] (= 3 (tc/row-count ds))) v12_l92)))


(def
 v15_l101
 (tc/dataset
  "https://vincentarelbundock.github.io/Rdatasets/csv/datasets/iris.csv"
  {:key-fn keyword}))


(deftest t16_l104 (is ((fn [ds] (= 150 (tc/row-count ds))) v15_l101)))


(def
 v18_l119
 (->
  {"sepal_length" [5.1 4.9 4.7 5.0], "sepal_width" [3.5 3.0 3.2 3.6]}
  (pj/lay-point "sepal_length" "sepal_width")))


(deftest
 t19_l123
 (is
  ((fn
    [v]
    (=
     ["sepal length" "sepal width"]
     ((juxt :x-label :y-label) (pj/plan v))))
   v18_l119)))


(def v21_l129 (tc/dataset [[1 2] [3 4] [5 7]]))


(deftest
 t22_l131
 (is ((fn [ds] (= [0 1] (tc/column-names ds))) v21_l129)))


(def v24_l136 (-> (tc/dataset [[1 2] [3 4] [5 7]]) pj/pose))


(deftest
 t25_l139
 (is
  ((fn [v] (= ["0" "1"] ((juxt :x-label :y-label) (pj/plan v))))
   v24_l136)))


(def
 v27_l146
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t28_l150
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v27_l146)))


(def v30_l171 (rdatasets/datasets-iris))


(deftest
 t31_l173
 (is
  ((fn [ds] (and (tc/dataset? ds) (= 150 (tc/row-count ds))))
   v30_l171)))


(def
 v33_l181
 (->
  {:var
   [#'rdatasets/datasets-iris
    #'rdatasets/reshape2-tips
    #'rdatasets/ggplot2-mpg
    #'rdatasets/ggplot2-diamonds
    #'rdatasets/gapminder-gapminder
    #'rdatasets/datasets-mtcars]}
  tc/dataset
  (tc/map-columns
   :function
   :var
   (fn* [p1__85904#] (-> p1__85904# meta :name)))
  (tc/map-columns :dataset :var (fn* [p1__85905#] (p1__85905#)))
  (tc/map-columns :rows :dataset tc/row-count)
  (tc/map-columns
   :description
   :var
   (fn*
    [p1__85906#]
    (->
     p1__85906#
     meta
     :doc-link
     slurp
     str/split-lines
     first
     (str/replace "<!DOCTYPE html><html><head><title>R: " "")
     (str/replace "</title>" ""))))
  (tc/select-columns [:function :rows :description])))


(def v35_l213 (tc/head (rdatasets/datasets-iris) 3))


(deftest t36_l215 (is ((fn [ds] (= 3 (tc/row-count ds))) v35_l213)))


(def
 v38_l219
 (->
  (rdatasets/datasets-iris)
  (tc/select-rows
   (fn* [p1__85907#] (= "setosa" (:species p1__85907#))))))


(deftest t39_l222 (is ((fn [ds] (= 50 (tc/row-count ds))) v38_l219)))


(def
 v41_l226
 (->
  (rdatasets/datasets-iris)
  (tc/group-by [:species])
  (tc/aggregate
   {:mean-sl
    (fn [ds] (/ (reduce + (ds :sepal-length)) (tc/row-count ds)))})))


(deftest t42_l231 (is ((fn [ds] (= 3 (tc/row-count ds))) v41_l226)))


(def
 v44_l235
 (->
  (rdatasets/datasets-mtcars)
  (tc/order-by [:mpg] :desc)
  (tc/head 3)))


(deftest t45_l239 (is ((fn [ds] (= 3 (tc/row-count ds))) v44_l235)))


(def v47_l243 (tc/column-names (rdatasets/datasets-iris)))


(deftest t48_l245 (is ((fn [cols] (= 6 (count cols))) v47_l243)))


(def v50_l249 (tc/row-count (rdatasets/ggplot2-diamonds)))


(deftest t51_l251 (is ((fn [n] (= 53940 n)) v50_l249)))


(def v53_l261 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y)))


(deftest
 t54_l264
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v53_l261)))


(def
 v56_l268
 (-> (tc/dataset {:x [1 2 3], :y [4 5 6]}) (pj/lay-point :x :y)))


(deftest
 t57_l271
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v56_l268)))


(def
 v59_l283
 (def
  temps-wide
  (tc/dataset
   {:month ["Jan" "Feb" "Mar"],
    :tokyo [3 5 9],
    :paris [4 6 11],
    :nairobi [22 23 24]})))


(def v60_l290 temps-wide)


(deftest
 t61_l292
 (is ((fn [ds] (= 4 (count (tc/column-names ds)))) v60_l290)))


(def
 v63_l298
 (def
  temps-long
  (tc/pivot->longer
   temps-wide
   [:tokyo :paris :nairobi]
   {:target-columns :city, :value-column-name :temperature})))


(def v64_l303 temps-long)


(deftest
 t65_l305
 (is
  ((fn
    [ds]
    (and (= 3 (count (tc/column-names ds))) (= 9 (tc/row-count ds))))
   v64_l303)))


(def
 v67_l310
 (-> temps-long (pj/lay-line :month :temperature {:color :city})))


(deftest
 t68_l314
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:lines s)))))
   v67_l310)))
