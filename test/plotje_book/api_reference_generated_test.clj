(ns
 plotje-book.api-reference-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [clojure.test :refer [deftest is]]))


(def
 v3_l26
 (def tiny {:x [1 2 3 4 5], :y [2 4 1 5 3], :group [:a :a :b :b :b]}))


(def
 v4_l30
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def
 v5_l33
 (def
  measurements
  {:treatment ["A" "B" "C" "D"],
   :mean [10.0 15.0 12.0 18.0],
   :ci-lo [8.0 12.0 9.5 15.5],
   :ci-hi [12.0 18.0 14.5 20.5]}))


(def v7_l48 (kind/doc #'pj/pose))


(def
 v9_l52
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t10_l56
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v9_l52)))


(def
 v12_l62
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t13_l67
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v12_l62)))


(def v15_l75 (pj/pose [1 4 1 5 6 2 3 3 3 2 4 5 1 2 3 4]))


(deftest
 t16_l77
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v15_l75)))


(def v17_l81 (kind/doc #'pj/with-data))


(def
 v19_l87
 (def
  scatter-template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def v20_l91 (-> scatter-template (pj/with-data tiny)))


(deftest
 t21_l94
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v20_l91)))


(def
 v23_l99
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point {:color :species})))


(deftest
 t24_l104
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v23_l99)))


(def
 v26_l110
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length, :y :sepal-width})
  pj/lay-point))


(deftest
 t27_l114
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v26_l110)))


(def v28_l118 (kind/doc #'pj/cross))


(def v29_l120 (pj/cross [:a :b] [1 2 3]))


(deftest
 t30_l122
 (is
  ((fn [v] (= [[:a 1] [:a 2] [:a 3] [:b 1] [:b 2] [:b 3]] v))
   v29_l120)))


(def
 v32_l126
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   (pj/cross [:sepal-length :petal-length] [:sepal-width :petal-width])
   {:color :species})))


(deftest
 t33_l131
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v32_l126)))


(def v35_l137 (kind/doc #'pj/lay))


(def
 v37_l145
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t38_l149
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v37_l145)))


(def v40_l160 (kind/doc #'pj/lay-point))


(def
 v41_l162
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t42_l165
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v41_l162)))


(def v43_l168 (kind/doc #'pj/lay-line))


(def
 v44_l170
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__83721#] (Math/sin (* p1__83721# 0.3))) (range 30))}))


(def v45_l173 (-> wave (pj/lay-line :x :y)))


(deftest
 t46_l176
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v45_l173)))


(def v47_l179 (kind/doc #'pj/lay-histogram))


(def
 v48_l181
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t49_l184
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v48_l181)))


(def
 v51_l189
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width]))


(deftest
 t52_l191
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (pos? (:polygons s)))))
   v51_l189)))


(def v53_l195 (kind/doc #'pj/lay-bar))


(def v54_l197 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t55_l200
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v54_l197)))


(def
 v57_l205
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t58_l208
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l205)))


(def
 v60_l213
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t61_l216
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v60_l213)))


(def v63_l222 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t64_l225
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v63_l222)))


(def v66_l231 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t67_l234
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v66_l231)))


(def
 v69_l241
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t70_l244
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v69_l241)))


(def v72_l251 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t73_l254
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v72_l251)))


(def
 v75_l265
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t76_l268
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v75_l265)))


(def
 v78_l273
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t79_l277
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v78_l273)))


(def v80_l280 (kind/doc #'pj/lay-smooth))


(def
 v82_l284
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t83_l288
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v82_l284)))


(def
 v84_l292
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__83722#]
      (+
       (Math/sin (* p1__83722# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t85_l301
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v84_l292)))


(def v86_l305 (kind/doc #'pj/lay-density))


(def
 v87_l307
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t88_l310
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v87_l307)))


(def v89_l313 (kind/doc #'pj/lay-area))


(def v90_l315 (-> wave (pj/lay-area :x :y)))


(deftest
 t91_l318
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v90_l315)))


(def
 v93_l323
 (->
  {:x (concat (range 10) (range 10) (range 10)),
   :y
   (concat
    [1 2 3 4 5 4 3 2 1 0]
    [2 2 2 3 3 3 2 2 2 2]
    [1 1 1 1 2 2 2 1 1 1]),
   :group (concat (repeat 10 "A") (repeat 10 "B") (repeat 10 "C"))}
  (pj/lay-area :x :y {:position :stack, :color :group})))


(deftest
 t94_l330
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v93_l323)))


(def v95_l333 (kind/doc #'pj/lay-text))


(def
 v96_l335
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t97_l338
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v96_l335)))


(def v98_l341 (kind/doc #'pj/lay-label))


(def
 v99_l343
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t100_l347
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v99_l343)))


(def v101_l351 (kind/doc #'pj/lay-boxplot))


(def
 v102_l353
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t103_l356
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v102_l353)))


(def v104_l360 (kind/doc #'pj/lay-violin))


(def
 v105_l362
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t106_l365
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v105_l362)))


(def v107_l368 (kind/doc #'pj/lay-errorbar))


(def
 v108_l370
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t109_l374
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v108_l370)))


(def v110_l378 (kind/doc #'pj/lay-lollipop))


(def v111_l380 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t112_l383
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v111_l380)))


(def v113_l387 (kind/doc #'pj/lay-tile))


(def
 v114_l389
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t115_l392
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v114_l389)))


(def v116_l395 (kind/doc #'pj/lay-density-2d))


(def
 v117_l397
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t118_l400
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v117_l397)))


(def v119_l403 (kind/doc #'pj/lay-contour))


(def
 v120_l405
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t121_l408
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v120_l405)))


(def v122_l411 (kind/doc #'pj/lay-ridgeline))


(def
 v123_l413
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t124_l416
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v123_l413)))


(def v125_l419 (kind/doc #'pj/lay-rug))


(def
 v126_l421
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t127_l425
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v126_l421)))


(def v128_l428 (kind/doc #'pj/lay-step))


(def v129_l430 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t130_l434
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v129_l430)))


(def v131_l438 (kind/doc #'pj/lay-summary))


(def
 v132_l440
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t133_l443
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v132_l440)))


(def v134_l447 (kind/doc #'pj/lay-interval-h))


(def
 v135_l449
 (->
  {:start
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"],
   :end
   [#inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"
    #inst "2024-08-01T00:00:00.000-00:00"],
   :task ["Design" "Build" "Test"]}
  (pj/lay-interval-h :start :task {:x-end :end})))


(deftest
 t136_l454
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v135_l449)))


(def v138_l482 (kind/doc #'pj/lay-rule-v))


(def
 v139_l484
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t140_l488
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v139_l484)))


(def
 v142_l495
 (->
  {:date
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-08-01T00:00:00.000-00:00"],
   :value [3 5 9]}
  (pj/lay-line :date :value)
  (pj/lay-rule-v
   {:x-intercept (java.time.LocalDate/parse "2024-06-01"),
    :color "#c0392b"})))


(deftest
 t143_l501
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v142_l495)))


(def v144_l505 (kind/doc #'pj/lay-rule-h))


(def
 v145_l507
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t146_l511
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v145_l507)))


(def v147_l515 (kind/doc #'pj/lay-band-v))


(def
 v148_l517
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t149_l521
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v148_l517)))


(def v150_l524 (kind/doc #'pj/lay-band-h))


(def
 v151_l526
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t152_l530
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v151_l526)))


(def v154_l535 (kind/doc #'pj/coord))


(def
 v156_l539
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t157_l542
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v156_l539)))


(def
 v159_l547
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t160_l550
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v159_l547)))


(def v161_l553 (kind/doc #'pj/scale))


(def
 v163_l557
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t164_l560
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v163_l557)))


(def
 v166_l565
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t167_l568
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v166_l565)))


(def
 v169_l574
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t170_l578
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v169_l574)))


(def
 v172_l583
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t173_l588
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v172_l583)))


(def v174_l594 (kind/doc #'pj/shape-symbols))


(def v175_l596 pj/shape-symbols)


(deftest
 t176_l598
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v175_l596)))


(def
 v178_l603
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t179_l609
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v178_l603)))


(def v181_l615 (kind/doc #'pj/facet))


(def
 v182_l617
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t183_l621
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v182_l617)))


(def v184_l625 (kind/doc #'pj/facet-grid))


(def
 v185_l627
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t186_l631
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v185_l627)))


(def v188_l637 (kind/doc #'pj/arrange))


(def
 v189_l639
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:width 250, :height 200}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species})
    (pj/options {:width 250, :height 200}))]
  {:cols 2}))


(deftest t190_l647 (is ((fn [v] (pj/pose? v)) v189_l639)))


(def v192_l651 (kind/doc #'pj/plot))


(def v194_l656 (-> tiny (pj/lay-point :x :y)))


(deftest
 t195_l659
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v194_l656)))


(def
 v197_l666
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t198_l669
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v197_l666)))


(def v199_l673 (kind/doc #'pj/options))


(def
 v201_l677
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t202_l681
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v201_l677)))


(def v204_l687 (kind/doc #'pj/pose?))


(def v206_l691 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t207_l693 (is (true? v206_l691)))


(def v208_l695 (kind/doc #'pj/plan?))


(def v210_l699 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t211_l701 (is (true? v210_l699)))


(def v212_l703 (kind/doc #'pj/leaf-plan?))


(def v214_l708 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t215_l710 (is (true? v214_l708)))


(def v216_l712 (kind/doc #'pj/composite-plan?))


(def
 v218_l717
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t219_l721 (is (true? v218_l717)))


(def v220_l723 (kind/doc #'pj/draft?))


(def v222_l728 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t223_l730 (is (true? v222_l728)))


(def v224_l732 (kind/doc #'pj/leaf-draft?))


(def v226_l737 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t227_l739 (is (true? v226_l737)))


(def v228_l741 (kind/doc #'pj/composite-draft?))


(def
 v230_l746
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t231_l750 (is (true? v230_l746)))


(def v232_l752 (kind/doc #'pj/plan-layer?))


(def
 v234_l756
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t235_l758 (is (true? v234_l756)))


(def v236_l760 (kind/doc #'pj/layer-type?))


(def v238_l764 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t239_l766 (is (true? v238_l764)))


(def v240_l768 (kind/doc #'pj/membrane?))


(def v242_l773 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t243_l775 (is (true? v242_l773)))


(def v245_l779 (kind/doc #'pj/draft))


(def
 v247_l786
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t248_l792
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v247_l786)))


(def v249_l796 (kind/doc #'pj/plan))


(def v251_l800 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v252_l804 plan1)


(deftest
 t253_l806
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v252_l804)))


(def v254_l809 (kind/doc #'pj/svg-summary))


(def
 v255_l811
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t256_l814
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v255_l811)))


(def v257_l817 (kind/doc #'pj/valid-plan?))


(def v258_l819 (pj/valid-plan? plan1))


(deftest t259_l821 (is (true? v258_l819)))


(def v260_l823 (kind/doc #'pj/explain-plan))


(def v261_l825 (pj/explain-plan plan1))


(deftest t262_l827 (is (nil? v261_l825)))


(def v264_l844 (kind/doc #'pj/membrane))


(def
 v266_l854
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t267_l860
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v266_l854)))


(def v268_l866 (kind/doc #'pj/->pose))


(def v270_l873 (pj/pose? (pj/->pose tiny)))


(deftest t271_l875 (is (true? v270_l873)))


(def v272_l877 (kind/doc #'pj/infer-mapping))


(def
 v274_l883
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t275_l888
 (is ((fn [m] (= {:x :height, :y :weight} m)) v274_l883)))


(def
 v277_l893
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t278_l897 (is (true? v277_l893)))


(def v279_l899 (kind/doc #'pj/pose->draft))


(def
 v281_l905
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t282_l908 (is (true? v281_l905)))


(def v283_l910 (kind/doc #'pj/plan->membrane))


(def v284_l912 (def m1 (pj/plan->membrane plan1)))


(def v285_l914 (pj/membrane? m1))


(deftest t286_l916 (is (true? v285_l914)))


(def v287_l918 (kind/doc #'pj/valid-membrane?))


(def v288_l920 (pj/valid-membrane? m1))


(deftest t289_l922 (is (true? v288_l920)))


(def v290_l924 (kind/doc #'pj/explain-membrane))


(def v291_l926 (pj/explain-membrane m1))


(deftest t292_l928 (is (nil? v291_l926)))


(def v293_l930 (kind/doc #'pj/membrane->plot))


(def v294_l932 (first (pj/membrane->plot m1 :svg {})))


(deftest t295_l934 (is ((fn [v] (= :svg v)) v294_l932)))


(def v296_l936 (kind/doc #'pj/plan->plot))


(def v297_l938 (first (pj/plan->plot plan1 :svg {})))


(deftest t298_l940 (is ((fn [v] (= :svg v)) v297_l938)))


(def v300_l947 (kind/doc #'pj/draft->plan))


(def v301_l949 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v302_l951 (pj/plan? (pj/draft->plan draft1)))


(deftest t303_l953 (is (true? v302_l951)))


(def v304_l955 (kind/doc #'pj/draft->membrane))


(def v305_l957 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t306_l959 (is (true? v305_l957)))


(def v307_l961 (kind/doc #'pj/draft->plot))


(def v308_l963 (first (pj/draft->plot draft1 :svg {})))


(deftest t309_l965 (is ((fn [v] (= :svg v)) v308_l963)))


(def v311_l969 (kind/doc #'pj/config))


(def v312_l971 (pj/config))


(deftest t313_l973 (is ((fn [m] (map? m)) v312_l971)))


(def v314_l975 (kind/doc #'pj/set-config!))


(def v315_l977 (kind/doc #'pj/with-config))


(def
 v316_l979
 (pj/with-config {:palette :pastel1} (:palette (pj/config))))


(deftest t317_l982 (is ((fn [p] (= :pastel1 p)) v316_l979)))


(def v319_l988 (kind/doc #'pj/config-key-docs))


(def v320_l990 (count pj/config-key-docs))


(deftest t321_l992 (is ((fn [n] (= 41 n)) v320_l990)))


(def v322_l994 (kind/doc #'pj/plot-option-docs))


(def v323_l996 (count pj/plot-option-docs))


(deftest t324_l998 (is ((fn [n] (= 15 n)) v323_l996)))


(def v325_l1000 (kind/doc #'pj/layer-option-docs))


(def v326_l1002 (count pj/layer-option-docs))


(deftest t327_l1004 (is ((fn [n] (= 51 n)) v326_l1002)))


(def v329_l1008 (kind/doc #'pj/layer-type-lookup))


(def v330_l1010 (pj/layer-type-lookup :smooth))


(deftest
 t331_l1012
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v330_l1010)))


(def v332_l1015 (kind/doc #'pj/registered-layer-types))


(def v333_l1017 (count (pj/registered-layer-types)))


(deftest t334_l1019 (is ((fn [n] (= 25 n)) v333_l1017)))


(def v335_l1021 (first (pj/registered-layer-types)))


(deftest
 t336_l1023
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v335_l1021)))


(def v338_l1031 (kind/doc #'pj/stat-doc))


(def v339_l1033 (pj/stat-doc :linear-model))


(deftest t340_l1035 (is ((fn [s] (string? s)) v339_l1033)))


(def v341_l1037 (kind/doc #'pj/mark-doc))


(def v342_l1039 (pj/mark-doc :point))


(deftest t343_l1041 (is ((fn [s] (string? s)) v342_l1039)))


(def v344_l1043 (kind/doc #'pj/position-doc))


(def v345_l1045 (pj/position-doc :dodge))


(deftest t346_l1047 (is ((fn [s] (string? s)) v345_l1045)))


(def v347_l1049 (kind/doc #'pj/scale-doc))


(def v348_l1051 (pj/scale-doc :linear))


(deftest t349_l1053 (is ((fn [s] (string? s)) v348_l1051)))


(def v350_l1055 (kind/doc #'pj/coord-doc))


(def v351_l1057 (pj/coord-doc :cartesian))


(deftest t352_l1059 (is ((fn [s] (string? s)) v351_l1057)))


(def v353_l1061 (kind/doc #'pj/membrane-mark-doc))


(def v354_l1063 (pj/membrane-mark-doc :point))


(deftest t355_l1065 (is ((fn [s] (string? s)) v354_l1063)))


(def v357_l1069 (kind/doc #'pj/save))


(def
 v359_l1073
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t360_l1079 (is (true? v359_l1073)))


(def
 v362_l1084
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".png"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path))
  (with-open
   [in (java.io.FileInputStream. path)]
   (let
    [bs (byte-array 8)]
    (.read in bs)
    (mapv (fn* [p1__83723#] (bit-and p1__83723# 255)) (vec bs))))))


(deftest
 t363_l1093
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v362_l1084)))


(def
 v365_l1098
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".out"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:format :png}))
  (with-open
   [in (java.io.FileInputStream. path)]
   (let
    [bs (byte-array 4)]
    (.read in bs)
    (mapv (fn* [p1__83724#] (bit-and p1__83724# 255)) (vec bs))))))


(deftest t366_l1107 (is ((fn [bs] (= [137 80 78 71] bs)) v365_l1098)))
