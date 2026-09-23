(ns
 plotje-book.faceting-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l29
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t4_l33
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (:panels
       (pj/plan
        (->
         (rdatasets/datasets-iris)
         (pj/lay-point :sepal-length :sepal-width {:color :species})
         (pj/facet :species))))
      x-doms
      (mapv :x-domain panels)
      y-doms
      (mapv :y-domain panels)]
     (and
      (= 3 (:panels s))
      (= 150 (:points s))
      (apply = x-doms)
      (apply = y-doms))))
   v3_l29)))


(def
 v6_l57
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species :row)))


(deftest
 t7_l61
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v6_l57)))


(def
 v9_l67
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t10_l71
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v9_l67)))


(def
 v12_l85
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t13_l90
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v12_l85)))


(def
 v15_l96
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v16_l100
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t17_l105
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v16_l100)))


(def
 v19_l111
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet-grid :drv :cyl)
  kind/pprint))


(deftest
 t20_l116
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v19_l111)))


(def
 v22_l123
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t23_l127
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
   v22_l123)))


(def
 v25_l144
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t26_l150
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v25_l144)))


(def
 v28_l163
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t29_l169
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__11193#] (count (:panels (:plan p1__11193#))))
       (:sub-plots (pj/plan fr))))))
   v28_l163)))


(def
 v31_l182
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t32_l188
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v31_l182)))


(def
 v34_l199
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t35_l205
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
   v34_l199)))


(def v37_l223 (pj/compound-key-aesthetics))


(deftest t38_l225 (is ((fn [s] (= #{:group :col :row} s)) v37_l223)))


(def
 v40_l230
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v41_l236 measures)


(def
 v43_l241
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t44_l247
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v43_l241)))


(def
 v46_l253
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t47_l257
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v46_l253)))


(def
 v49_l266
 (-> measures (pj/lay-point :t :v) (pj/facet-grid :part :dimension)))


(deftest
 t50_l270
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v49_l266)))


(def
 v52_l280
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:scales :shared})))


(deftest
 t53_l285
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      doms
      (mapv
       :x-domain
       (:panels
        (pj/plan
         (->
          (rdatasets/datasets-iris)
          (pj/lay-point :sepal-length :sepal-width)
          (pj/facet :species)))))]
     (and (= 3 (:panels s)) (= 150 (:points s)) (apply = doms))))
   v52_l280)))


(def
 v55_l299
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:scales :free-y})))


(deftest
 t56_l304
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      doms
      (mapv
       :y-domain
       (:panels
        (pj/plan
         (->
          (rdatasets/datasets-iris)
          (pj/lay-point :sepal-length :sepal-width)
          (pj/facet :species)
          (pj/options {:scales :free-y})))))]
     (and (= 3 (:panels s)) (= 3 (count (distinct doms))))))
   v55_l299)))


(def
 v58_l332
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v59_l338
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t60_l342
 (is
  ((fn
    [v]
    (let
     [layers (->> v pj/plan :panels (mapcat :layers))]
     (and
      (= 2 (count layers))
      (= [[1 10] [1 10]] (mapv :size-extent layers))
      (=
       [[1 2 3] [4 7 10]]
       (mapv (fn [l] (vec (mapcat :sizes (:groups l)))) layers)))))
   v59_l338)))


(def
 v62_l360
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t63_l365
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v62_l360)))


(def
 v65_l388
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t66_l394
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
   v65_l388)))


(def
 v68_l412
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t69_l419
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v68_l412)))


(def
 v71_l428
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t72_l432
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v71_l428)))


(def
 v74_l440
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet-grid :smoker :sex)))


(deftest
 t75_l446
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v74_l440)))


(def
 v77_l457
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)))


(deftest
 t78_l461
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v77_l457)))


(def
 v80_l469
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]
  {:color :species}))


(deftest
 t81_l471
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v80_l469)))


(def
 v83_l479
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t84_l485
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:panels s))
      (= 150 (:points s))
      (some #{"Iris by Species"} (:texts s))
      (some #{"Sepal Length (cm)"} (:texts s)))))
   v83_l479)))
