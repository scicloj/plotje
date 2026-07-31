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
   (map (fn* [p1__75246#] (Math/sin (* p1__75246# 0.3))) (range 30))}))


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
      [p1__75247#]
      (+
       (Math/sin (* p1__75247# 0.2))
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
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t173_l589
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v172_l583)))


(def v175_l595 (kind/doc #'pj/facet))


(def
 v176_l597
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t177_l601
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v176_l597)))


(def v178_l605 (kind/doc #'pj/facet-grid))


(def
 v179_l607
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t180_l611
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v179_l607)))


(def v182_l617 (kind/doc #'pj/arrange))


(def
 v183_l619
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


(deftest t184_l627 (is ((fn [v] (pj/pose? v)) v183_l619)))


(def v186_l631 (kind/doc #'pj/plot))


(def v188_l636 (-> tiny (pj/lay-point :x :y)))


(deftest
 t189_l639
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v188_l636)))


(def
 v191_l646
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t192_l649
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v191_l646)))


(def v193_l653 (kind/doc #'pj/options))


(def
 v195_l657
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t196_l661
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v195_l657)))


(def v198_l667 (kind/doc #'pj/pose?))


(def v200_l671 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t201_l673 (is (true? v200_l671)))


(def v202_l675 (kind/doc #'pj/plan?))


(def v204_l679 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t205_l681 (is (true? v204_l679)))


(def v206_l683 (kind/doc #'pj/leaf-plan?))


(def v208_l688 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t209_l690 (is (true? v208_l688)))


(def v210_l692 (kind/doc #'pj/composite-plan?))


(def
 v212_l697
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t213_l701 (is (true? v212_l697)))


(def v214_l703 (kind/doc #'pj/draft?))


(def v216_l708 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t217_l710 (is (true? v216_l708)))


(def v218_l712 (kind/doc #'pj/leaf-draft?))


(def v220_l717 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t221_l719 (is (true? v220_l717)))


(def v222_l721 (kind/doc #'pj/composite-draft?))


(def
 v224_l726
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t225_l730 (is (true? v224_l726)))


(def v226_l732 (kind/doc #'pj/plan-layer?))


(def
 v228_l736
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t229_l738 (is (true? v228_l736)))


(def v230_l740 (kind/doc #'pj/layer-type?))


(def v232_l744 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t233_l746 (is (true? v232_l744)))


(def v234_l748 (kind/doc #'pj/membrane?))


(def v236_l753 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t237_l755 (is (true? v236_l753)))


(def v239_l759 (kind/doc #'pj/draft))


(def
 v241_l766
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t242_l772
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v241_l766)))


(def v243_l776 (kind/doc #'pj/plan))


(def v245_l780 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v246_l784 plan1)


(deftest
 t247_l786
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v246_l784)))


(def v248_l789 (kind/doc #'pj/svg-summary))


(def
 v249_l791
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t250_l794
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v249_l791)))


(def v251_l797 (kind/doc #'pj/valid-plan?))


(def v252_l799 (pj/valid-plan? plan1))


(deftest t253_l801 (is (true? v252_l799)))


(def v254_l803 (kind/doc #'pj/explain-plan))


(def v255_l805 (pj/explain-plan plan1))


(deftest t256_l807 (is (nil? v255_l805)))


(def v258_l824 (kind/doc #'pj/membrane))


(def
 v260_l834
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t261_l840
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v260_l834)))


(def v262_l846 (kind/doc #'pj/->pose))


(def v264_l853 (pj/pose? (pj/->pose tiny)))


(deftest t265_l855 (is (true? v264_l853)))


(def v266_l857 (kind/doc #'pj/infer-mapping))


(def
 v268_l863
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t269_l868
 (is ((fn [m] (= {:x :height, :y :weight} m)) v268_l863)))


(def
 v271_l873
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t272_l877 (is (true? v271_l873)))


(def v273_l879 (kind/doc #'pj/pose->draft))


(def
 v275_l885
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t276_l888 (is (true? v275_l885)))


(def v277_l890 (kind/doc #'pj/plan->membrane))


(def v278_l892 (def m1 (pj/plan->membrane plan1)))


(def v279_l894 (pj/membrane? m1))


(deftest t280_l896 (is (true? v279_l894)))


(def v281_l898 (kind/doc #'pj/valid-membrane?))


(def v282_l900 (pj/valid-membrane? m1))


(deftest t283_l902 (is (true? v282_l900)))


(def v284_l904 (kind/doc #'pj/explain-membrane))


(def v285_l906 (pj/explain-membrane m1))


(deftest t286_l908 (is (nil? v285_l906)))


(def v287_l910 (kind/doc #'pj/membrane->plot))


(def v288_l912 (first (pj/membrane->plot m1 :svg {})))


(deftest t289_l914 (is ((fn [v] (= :svg v)) v288_l912)))


(def v290_l916 (kind/doc #'pj/plan->plot))


(def v291_l918 (first (pj/plan->plot plan1 :svg {})))


(deftest t292_l920 (is ((fn [v] (= :svg v)) v291_l918)))


(def v294_l927 (kind/doc #'pj/draft->plan))


(def v295_l929 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v296_l931 (pj/plan? (pj/draft->plan draft1)))


(deftest t297_l933 (is (true? v296_l931)))


(def v298_l935 (kind/doc #'pj/draft->membrane))


(def v299_l937 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t300_l939 (is (true? v299_l937)))


(def v301_l941 (kind/doc #'pj/draft->plot))


(def v302_l943 (first (pj/draft->plot draft1 :svg {})))


(deftest t303_l945 (is ((fn [v] (= :svg v)) v302_l943)))


(def v305_l949 (kind/doc #'pj/config))


(def v306_l951 (pj/config))


(deftest t307_l953 (is ((fn [m] (map? m)) v306_l951)))


(def v308_l955 (kind/doc #'pj/set-config!))


(def v309_l957 (kind/doc #'pj/with-config))


(def
 v310_l959
 (pj/with-config {:palette :pastel1} (:palette (pj/config))))


(deftest t311_l962 (is ((fn [p] (= :pastel1 p)) v310_l959)))


(def v313_l968 (kind/doc #'pj/config-key-docs))


(def v314_l970 (count pj/config-key-docs))


(deftest t315_l972 (is ((fn [n] (= 40 n)) v314_l970)))


(def v316_l974 (kind/doc #'pj/plot-option-docs))


(def v317_l976 (count pj/plot-option-docs))


(deftest t318_l978 (is ((fn [n] (= 14 n)) v317_l976)))


(def v319_l980 (kind/doc #'pj/layer-option-docs))


(def v320_l982 (count pj/layer-option-docs))


(deftest t321_l984 (is ((fn [n] (pos? n)) v320_l982)))


(def v323_l988 (kind/doc #'pj/layer-type-lookup))


(def v324_l990 (pj/layer-type-lookup :smooth))


(deftest
 t325_l992
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v324_l990)))


(def v326_l995 (kind/doc #'pj/registered-layer-types))


(def v327_l997 (count (pj/registered-layer-types)))


(deftest t328_l999 (is ((fn [n] (= 25 n)) v327_l997)))


(def v329_l1001 (first (pj/registered-layer-types)))


(deftest
 t330_l1003
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v329_l1001)))


(def v332_l1011 (kind/doc #'pj/stat-doc))


(def v333_l1013 (pj/stat-doc :linear-model))


(deftest t334_l1015 (is ((fn [s] (string? s)) v333_l1013)))


(def v335_l1017 (kind/doc #'pj/mark-doc))


(def v336_l1019 (pj/mark-doc :point))


(deftest t337_l1021 (is ((fn [s] (string? s)) v336_l1019)))


(def v338_l1023 (kind/doc #'pj/position-doc))


(def v339_l1025 (pj/position-doc :dodge))


(deftest t340_l1027 (is ((fn [s] (string? s)) v339_l1025)))


(def v341_l1029 (kind/doc #'pj/scale-doc))


(def v342_l1031 (pj/scale-doc :linear))


(deftest t343_l1033 (is ((fn [s] (string? s)) v342_l1031)))


(def v344_l1035 (kind/doc #'pj/coord-doc))


(def v345_l1037 (pj/coord-doc :cartesian))


(deftest t346_l1039 (is ((fn [s] (string? s)) v345_l1037)))


(def v347_l1041 (kind/doc #'pj/membrane-mark-doc))


(def v348_l1043 (pj/membrane-mark-doc :point))


(deftest t349_l1045 (is ((fn [s] (string? s)) v348_l1043)))


(def v351_l1049 (kind/doc #'pj/save))


(def
 v353_l1053
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t354_l1059 (is (true? v353_l1053)))


(def
 v356_l1064
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
    (mapv (fn* [p1__75248#] (bit-and p1__75248# 255)) (vec bs))))))


(deftest
 t357_l1073
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v356_l1064)))


(def
 v359_l1078
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
    (mapv (fn* [p1__75249#] (bit-and p1__75249# 255)) (vec bs))))))


(deftest t360_l1087 (is ((fn [bs] (= [137 80 78 71] bs)) v359_l1078)))
