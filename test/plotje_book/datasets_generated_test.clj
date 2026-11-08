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


(def
 v6_l77
 (tc/dataset {:height [1 2 3 4 5], :weight [10 20 15 30 25]}))


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


(def v30_l161 (-> (tc/dataset [[1 2] [3 4] [5 7]]) (pj/lay-point 0 1)))


(deftest
 t31_l164
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v30_l161)))


(def
 v33_l175
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:height :weight])
  (pj/lay-point :height :weight)))


(deftest
 t34_l179
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v33_l175)))


(def v36_l200 (rdatasets/datasets-iris))


(deftest
 t37_l202
 (is
  ((fn [ds] (and (tc/dataset? ds) (= 150 (tc/row-count ds))))
   v36_l200)))


(def
 v39_l210
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
   (fn* [p1__82240#] (-> p1__82240# meta :name)))
  (tc/map-columns :dataset :var (fn* [p1__82241#] (p1__82241#)))
  (tc/map-columns :rows :dataset tc/row-count)
  (tc/map-columns
   :description
   :var
   (fn*
    [p1__82242#]
    (->
     p1__82242#
     meta
     :doc-link
     slurp
     str/split-lines
     first
     (str/replace "<!DOCTYPE html><html><head><title>R: " "")
     (str/replace "</title>" ""))))
  (tc/select-columns [:function :rows :description])))


(def v41_l244 (tc/head (rdatasets/datasets-iris) 3))


(deftest t42_l246 (is ((fn [ds] (= 3 (tc/row-count ds))) v41_l244)))


(def
 v44_l250
 (->
  (rdatasets/datasets-iris)
  (tc/select-rows
   (fn* [p1__82243#] (= "setosa" (:species p1__82243#))))))


(deftest t45_l253 (is ((fn [ds] (= 50 (tc/row-count ds))) v44_l250)))


(def
 v47_l257
 (->
  (rdatasets/datasets-iris)
  (tc/group-by [:species])
  (tc/aggregate
   {:mean-sl
    (fn [ds] (/ (reduce + (ds :sepal-length)) (tc/row-count ds)))})))


(deftest t48_l262 (is ((fn [ds] (= 3 (tc/row-count ds))) v47_l257)))


(def
 v50_l266
 (->
  (rdatasets/datasets-mtcars)
  (tc/order-by [:mpg] :desc)
  (tc/head 3)))


(deftest t51_l270 (is ((fn [ds] (= 3 (tc/row-count ds))) v50_l266)))


(def v53_l274 (tc/column-names (rdatasets/datasets-iris)))


(deftest t54_l276 (is ((fn [cols] (= 6 (count cols))) v53_l274)))


(def v56_l280 (tc/row-count (rdatasets/ggplot2-diamonds)))


(deftest t57_l282 (is ((fn [n] (= 53940 n)) v56_l280)))


(def
 v59_l292
 (-> {:height [1 2 3], :weight [4 5 6]} (pj/lay-point :height :weight)))


(deftest
 t60_l295
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v59_l292)))


(def
 v62_l299
 (->
  (tc/dataset {:height [1 2 3], :weight [4 5 6]})
  (pj/lay-point :height :weight)))


(deftest
 t63_l302
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v62_l299)))


(def
 v65_l314
 (def
  temps-wide
  (tc/dataset
   {:month ["Jan" "Feb" "Mar"],
    :tokyo [3 5 9],
    :paris [4 6 11],
    :nairobi [22 23 24]})))


(def v66_l321 temps-wide)


(deftest
 t67_l323
 (is ((fn [ds] (= 4 (count (tc/column-names ds)))) v66_l321)))


(def
 v69_l329
 (def
  temps-long
  (tc/pivot->longer
   temps-wide
   [:tokyo :paris :nairobi]
   {:target-columns :city, :value-column-name :temperature})))


(def v70_l334 temps-long)


(deftest
 t71_l336
 (is
  ((fn
    [ds]
    (and (= 3 (count (tc/column-names ds))) (= 9 (tc/row-count ds))))
   v70_l334)))


(def
 v73_l341
 (-> temps-long (pj/lay-line :month :temperature {:color :city})))


(deftest
 t74_l345
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:lines s)))))
   v73_l341)))


(def
 v76_l355
 (-> temps-wide (pj/lay-line :month [:tokyo :paris :nairobi])))


(deftest
 t77_l358
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 1, :lines 3}
      (select-keys (pj/svg-summary v) [:panels :lines]))
     (=
      ["tokyo" "paris" "nairobi"]
      (mapv :label (:entries (:legend (pj/plan v)))))))
   v76_l355)))
