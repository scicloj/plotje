(ns
 plotje-book.panel-aesthetics-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l28
 (kind/table
  {:column-names [:aesthetic :role],
   :row-vectors
   (->>
    (pj/aesthetic-roles)
    (sort-by (comp str key))
    (mapv (fn [[k role]] [k role])))}))


(deftest
 t4_l34
 (is
  ((fn
    [_]
    (=
     #{:grouping :panel :positional :appearance}
     (set (vals (pj/aesthetic-roles)))))
   v3_l28)))


(def
 v6_l46
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t7_l51
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v6_l46)))


(def
 v9_l59
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v10_l63
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t11_l68
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v10_l63)))


(def
 v13_l74
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet-grid :drv :cyl)
  kind/pprint))


(deftest
 t14_l79
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v13_l74)))


(def
 v16_l94
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t17_l100
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v16_l94)))


(def
 v19_l113
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t20_l119
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__76033#] (count (:panels (:plan p1__76033#))))
       (:sub-plots (pj/plan fr))))))
   v19_l113)))


(def
 v22_l132
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t23_l138
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v22_l132)))


(def
 v25_l149
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t26_l155
 (is
  ((fn
    [fr]
    (let
     [s
      (pj/svg-summary fr)
      labelled
      (->>
       (pj/plan fr)
       :sub-plots
       (mapcat (comp :panels :plan))
       (keep :col-label))]
     (and
      (= 6 (:panels s))
      (= 150 (:points s))
      (= ["setosa" "versicolor" "virginica"] (vec labelled)))))
   v25_l149)))


(def v28_l173 (pj/compound-key-aesthetics))


(deftest t29_l175 (is ((fn [s] (= #{:group :col :row} s)) v28_l173)))


(def
 v31_l180
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v32_l186 measures)


(def
 v34_l191
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t35_l197
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v34_l191)))


(def
 v37_l203
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t38_l207
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v37_l203)))


(def
 v40_l216
 (-> measures (pj/lay-point :t :v) (pj/facet-grid :part :dimension)))


(deftest
 t41_l220
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v40_l216)))


(def
 v43_l228
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t44_l232
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (pj/svg-summary
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width)
       (pj/facet :species)))))
   v43_l228)))


(def
 v46_l246
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t47_l252
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"panel aesthetic" m)
     (re-find #"read from a pose's mapping" m)
     (re-find #"pj/facet pose" m)
     (re-find #"pj/facet-grid" m)
     (re-find #"in the pose's mapping" m)))
   v46_l246)))


(def
 v49_l270
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t50_l277
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v49_l270)))
