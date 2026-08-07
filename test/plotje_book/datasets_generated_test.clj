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
 v18_l120
 (->
  {:sepal-length [5.1 4.9 4.7 5.0], :sepal_width [3.5 3.0 3.2 3.6]}
  (pj/lay-point :sepal-length :sepal_width)))


(deftest
 t19_l124
 (is
  ((fn
    [v]
    (=
     ["sepal length" "sepal width"]
     ((juxt :x-label :y-label) (pj/plan v))))
   v18_l120)))


(def
 v21_l131
 (->
  {"sepal_length" [5.1 4.9 4.7 5.0],
   "Cost-Benefit Ratio" [3.5 3.0 3.2 3.6]}
  (pj/lay-point "sepal_length" "Cost-Benefit Ratio")))


(deftest
 t22_l135
 (is
  ((fn
    [v]
    (=
     ["sepal_length" "Cost-Benefit Ratio"]
     ((juxt :x-label :y-label) (pj/plan v))))
   v21_l131)))


(def v24_l144 (tc/dataset [[1 2] [3 4] [5 7]]))


(deftest
 t25_l146
 (is ((fn [ds] (= [0 1] (tc/column-names ds))) v24_l144)))


(def v27_l151 (-> (tc/dataset [[1 2] [3 4] [5 7]]) pj/pose))


(deftest
 t28_l154
 (is
  ((fn [v] (= ["0" "1"] ((juxt :x-label :y-label) (pj/plan v))))
   v27_l151)))


(def
 v30_l161
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t31_l165
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v30_l161)))


(def v33_l186 (rdatasets/datasets-iris))


(deftest
 t34_l188
 (is
  ((fn [ds] (and (tc/dataset? ds) (= 150 (tc/row-count ds))))
   v33_l186)))


(def
 v36_l196
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
   (fn* [p1__82168#] (-> p1__82168# meta :name)))
  (tc/map-columns :dataset :var (fn* [p1__82169#] (p1__82169#)))
  (tc/map-columns :rows :dataset tc/row-count)
  (tc/map-columns
   :description
   :var
   (fn*
    [p1__82170#]
    (->
     p1__82170#
     meta
     :doc-link
     slurp
     str/split-lines
     first
     (str/replace "<!DOCTYPE html><html><head><title>R: " "")
     (str/replace "</title>" ""))))
  (tc/select-columns [:function :rows :description])))


(def v38_l228 (tc/head (rdatasets/datasets-iris) 3))


(deftest t39_l230 (is ((fn [ds] (= 3 (tc/row-count ds))) v38_l228)))


(def
 v41_l234
 (->
  (rdatasets/datasets-iris)
  (tc/select-rows
   (fn* [p1__82171#] (= "setosa" (:species p1__82171#))))))


(deftest t42_l237 (is ((fn [ds] (= 50 (tc/row-count ds))) v41_l234)))


(def
 v44_l241
 (->
  (rdatasets/datasets-iris)
  (tc/group-by [:species])
  (tc/aggregate
   {:mean-sl
    (fn [ds] (/ (reduce + (ds :sepal-length)) (tc/row-count ds)))})))


(deftest t45_l246 (is ((fn [ds] (= 3 (tc/row-count ds))) v44_l241)))


(def
 v47_l250
 (->
  (rdatasets/datasets-mtcars)
  (tc/order-by [:mpg] :desc)
  (tc/head 3)))


(deftest t48_l254 (is ((fn [ds] (= 3 (tc/row-count ds))) v47_l250)))


(def v50_l258 (tc/column-names (rdatasets/datasets-iris)))


(deftest t51_l260 (is ((fn [cols] (= 6 (count cols))) v50_l258)))


(def v53_l264 (tc/row-count (rdatasets/ggplot2-diamonds)))


(deftest t54_l266 (is ((fn [n] (= 53940 n)) v53_l264)))


(def v56_l276 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y)))


(deftest
 t57_l279
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v56_l276)))


(def
 v59_l283
 (-> (tc/dataset {:x [1 2 3], :y [4 5 6]}) (pj/lay-point :x :y)))


(deftest
 t60_l286
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v59_l283)))


(def
 v62_l298
 (def
  temps-wide
  (tc/dataset
   {:month ["Jan" "Feb" "Mar"],
    :tokyo [3 5 9],
    :paris [4 6 11],
    :nairobi [22 23 24]})))


(def v63_l305 temps-wide)


(deftest
 t64_l307
 (is ((fn [ds] (= 4 (count (tc/column-names ds)))) v63_l305)))


(def
 v66_l313
 (def
  temps-long
  (tc/pivot->longer
   temps-wide
   [:tokyo :paris :nairobi]
   {:target-columns :city, :value-column-name :temperature})))


(def v67_l318 temps-long)


(deftest
 t68_l320
 (is
  ((fn
    [ds]
    (and (= 3 (count (tc/column-names ds))) (= 9 (tc/row-count ds))))
   v67_l318)))


(def
 v70_l325
 (-> temps-long (pj/lay-line :month :temperature {:color :city})))


(deftest
 t71_l329
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:lines s)))))
   v70_l325)))
