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


(def v7_l47 (kind/doc #'pj/pose))


(def
 v9_l51
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t10_l55
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v9_l51)))


(def
 v12_l61
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t13_l66
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v12_l61)))


(def v14_l70 (kind/doc #'pj/with-data))


(def
 v16_l76
 (def
  scatter-template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def v17_l80 (-> scatter-template (pj/with-data tiny)))


(deftest
 t18_l83
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v17_l80)))


(def
 v20_l88
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point {:color :species})))


(deftest
 t21_l93
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v20_l88)))


(def
 v23_l99
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length, :y :sepal-width})
  pj/lay-point))


(deftest
 t24_l103
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v23_l99)))


(def v25_l107 (kind/doc #'pj/cross))


(def v26_l109 (pj/cross [:a :b] [1 2 3]))


(deftest
 t27_l111
 (is
  ((fn [v] (= [[:a 1] [:a 2] [:a 3] [:b 1] [:b 2] [:b 3]] v))
   v26_l109)))


(def
 v29_l115
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   (pj/cross [:sepal-length :petal-length] [:sepal-width :petal-width])
   {:color :species})))


(deftest
 t30_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v29_l115)))


(def v32_l126 (kind/doc #'pj/lay))


(def
 v34_l134
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t35_l138
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v34_l134)))


(def v37_l149 (kind/doc #'pj/lay-point))


(def
 v38_l151
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l154
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v38_l151)))


(def v40_l157 (kind/doc #'pj/lay-line))


(def
 v41_l159
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__89103#] (Math/sin (* p1__89103# 0.3))) (range 30))}))


(def v42_l162 (-> wave (pj/lay-line :x :y)))


(deftest
 t43_l165
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v42_l162)))


(def v44_l168 (kind/doc #'pj/lay-histogram))


(def
 v45_l170
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t46_l173
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v45_l170)))


(def
 v48_l178
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width]))


(deftest
 t49_l180
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (pos? (:polygons s)))))
   v48_l178)))


(def v50_l184 (kind/doc #'pj/lay-bar))


(def v51_l186 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t52_l189
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v51_l186)))


(def
 v54_l194
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t55_l197
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v54_l194)))


(def
 v57_l202
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t58_l205
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l202)))


(def v60_l211 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t61_l214
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v60_l211)))


(def v63_l220 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t64_l223
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v63_l220)))


(def
 v66_l230
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t67_l233
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v66_l230)))


(def v69_l240 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t70_l243
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v69_l240)))


(def
 v72_l254
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t73_l257
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v72_l254)))


(def
 v75_l262
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t76_l266
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v75_l262)))


(def v77_l269 (kind/doc #'pj/lay-smooth))


(def
 v79_l273
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t80_l277
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v79_l273)))


(def
 v81_l281
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__89104#]
      (+
       (Math/sin (* p1__89104# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t82_l290
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v81_l281)))


(def v83_l294 (kind/doc #'pj/lay-density))


(def
 v84_l296
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t85_l299
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v84_l296)))


(def v86_l302 (kind/doc #'pj/lay-area))


(def v87_l304 (-> wave (pj/lay-area :x :y)))


(deftest
 t88_l307
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v87_l304)))


(def
 v90_l312
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
 t91_l319
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v90_l312)))


(def v92_l322 (kind/doc #'pj/lay-text))


(def
 v93_l324
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t94_l327
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v93_l324)))


(def v95_l330 (kind/doc #'pj/lay-label))


(def
 v96_l332
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t97_l336
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v96_l332)))


(def v98_l340 (kind/doc #'pj/lay-boxplot))


(def
 v99_l342
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t100_l345
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v99_l342)))


(def v101_l349 (kind/doc #'pj/lay-violin))


(def
 v102_l351
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t103_l354
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v102_l351)))


(def v104_l357 (kind/doc #'pj/lay-errorbar))


(def
 v105_l359
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t106_l363
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v105_l359)))


(def v107_l367 (kind/doc #'pj/lay-lollipop))


(def v108_l369 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t109_l372
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v108_l369)))


(def v110_l376 (kind/doc #'pj/lay-tile))


(def
 v111_l378
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t112_l381
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v111_l378)))


(def v113_l384 (kind/doc #'pj/lay-density-2d))


(def
 v114_l386
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t115_l389
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v114_l386)))


(def v116_l392 (kind/doc #'pj/lay-contour))


(def
 v117_l394
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t118_l397
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v117_l394)))


(def v119_l400 (kind/doc #'pj/lay-ridgeline))


(def
 v120_l402
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t121_l405
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v120_l402)))


(def v122_l408 (kind/doc #'pj/lay-rug))


(def
 v123_l410
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t124_l414
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v123_l410)))


(def v125_l417 (kind/doc #'pj/lay-step))


(def v126_l419 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t127_l423
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v126_l419)))


(def v128_l427 (kind/doc #'pj/lay-summary))


(def
 v129_l429
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t130_l432
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v129_l429)))


(def v131_l436 (kind/doc #'pj/lay-interval-h))


(def
 v132_l438
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
 t133_l443
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v132_l438)))


(def v135_l471 (kind/doc #'pj/lay-rule-v))


(def
 v136_l473
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t137_l477
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v136_l473)))


(def
 v139_l484
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
 t140_l490
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v139_l484)))


(def v141_l494 (kind/doc #'pj/lay-rule-h))


(def
 v142_l496
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t143_l500
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v142_l496)))


(def v144_l504 (kind/doc #'pj/lay-band-v))


(def
 v145_l506
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t146_l510
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v145_l506)))


(def v147_l513 (kind/doc #'pj/lay-band-h))


(def
 v148_l515
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t149_l519
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v148_l515)))


(def v151_l524 (kind/doc #'pj/coord))


(def
 v153_l528
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t154_l531
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v153_l528)))


(def
 v156_l536
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t157_l539
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v156_l536)))


(def v158_l542 (kind/doc #'pj/scale))


(def
 v160_l546
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t161_l549
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v160_l546)))


(def
 v163_l554
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t164_l557
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v163_l554)))


(def
 v166_l563
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t167_l567
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v166_l563)))


(def
 v169_l572
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t170_l578
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v169_l572)))


(def v172_l584 (kind/doc #'pj/facet))


(def
 v173_l586
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t174_l590
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v173_l586)))


(def v175_l594 (kind/doc #'pj/facet-grid))


(def
 v176_l596
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t177_l600
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v176_l596)))


(def v179_l606 (kind/doc #'pj/arrange))


(def
 v180_l608
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


(deftest t181_l616 (is ((fn [v] (pj/pose? v)) v180_l608)))


(def v183_l620 (kind/doc #'pj/plot))


(def v185_l625 (-> tiny (pj/lay-point :x :y)))


(deftest
 t186_l628
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v185_l625)))


(def v187_l631 (kind/doc #'pj/options))


(def
 v189_l635
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t190_l639
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v189_l635)))


(def v192_l645 (kind/doc #'pj/pose?))


(def v194_l649 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t195_l651 (is (true? v194_l649)))


(def v196_l653 (kind/doc #'pj/plan?))


(def v198_l657 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t199_l659 (is (true? v198_l657)))


(def v200_l661 (kind/doc #'pj/leaf-plan?))


(def v202_l666 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t203_l668 (is (true? v202_l666)))


(def v204_l670 (kind/doc #'pj/composite-plan?))


(def
 v206_l675
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t207_l679 (is (true? v206_l675)))


(def v208_l681 (kind/doc #'pj/draft?))


(def v210_l686 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t211_l688 (is (true? v210_l686)))


(def v212_l690 (kind/doc #'pj/leaf-draft?))


(def v214_l695 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t215_l697 (is (true? v214_l695)))


(def v216_l699 (kind/doc #'pj/composite-draft?))


(def
 v218_l704
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t219_l708 (is (true? v218_l704)))


(def v220_l710 (kind/doc #'pj/plan-layer?))


(def
 v222_l714
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t223_l716 (is (true? v222_l714)))


(def v224_l718 (kind/doc #'pj/layer-type?))


(def v226_l722 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t227_l724 (is (true? v226_l722)))


(def v228_l726 (kind/doc #'pj/membrane?))


(def v230_l731 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t231_l733 (is (true? v230_l731)))


(def v233_l737 (kind/doc #'pj/draft))


(def
 v235_l744
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t236_l750
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v235_l744)))


(def v237_l754 (kind/doc #'pj/plan))


(def v239_l758 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v240_l762 plan1)


(deftest
 t241_l764
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v240_l762)))


(def v242_l767 (kind/doc #'pj/svg-summary))


(def
 v243_l769
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t244_l772
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v243_l769)))


(def v245_l775 (kind/doc #'pj/valid-plan?))


(def v246_l777 (pj/valid-plan? plan1))


(deftest t247_l779 (is (true? v246_l777)))


(def v248_l781 (kind/doc #'pj/explain-plan))


(def v249_l783 (pj/explain-plan plan1))


(deftest t250_l785 (is (nil? v249_l783)))


(def v252_l797 (kind/doc #'pj/membrane))


(def
 v254_l807
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t255_l813
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v254_l807)))


(def v256_l819 (kind/doc #'pj/->pose))


(def v258_l826 (pj/pose? (pj/->pose tiny)))


(deftest t259_l828 (is (true? v258_l826)))


(def v260_l830 (kind/doc #'pj/pose->draft))


(def
 v262_l836
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t263_l839 (is (true? v262_l836)))


(def v264_l841 (kind/doc #'pj/plan->membrane))


(def v265_l843 (def m1 (pj/plan->membrane plan1)))


(def v266_l845 (pj/membrane? m1))


(deftest t267_l847 (is (true? v266_l845)))


(def v268_l849 (kind/doc #'pj/valid-membrane?))


(def v269_l851 (pj/valid-membrane? m1))


(deftest t270_l853 (is (true? v269_l851)))


(def v271_l855 (kind/doc #'pj/explain-membrane))


(def v272_l857 (pj/explain-membrane m1))


(deftest t273_l859 (is (nil? v272_l857)))


(def v274_l861 (kind/doc #'pj/membrane->plot))


(def v275_l863 (first (pj/membrane->plot m1 :svg {})))


(deftest t276_l865 (is ((fn [v] (= :svg v)) v275_l863)))


(def v277_l867 (kind/doc #'pj/plan->plot))


(def v278_l869 (first (pj/plan->plot plan1 :svg {})))


(deftest t279_l871 (is ((fn [v] (= :svg v)) v278_l869)))


(def v281_l878 (kind/doc #'pj/draft->plan))


(def v282_l880 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v283_l882 (pj/plan? (pj/draft->plan draft1)))


(deftest t284_l884 (is (true? v283_l882)))


(def v285_l886 (kind/doc #'pj/draft->membrane))


(def v286_l888 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t287_l890 (is (true? v286_l888)))


(def v288_l892 (kind/doc #'pj/draft->plot))


(def v289_l894 (first (pj/draft->plot draft1 :svg {})))


(deftest t290_l896 (is ((fn [v] (= :svg v)) v289_l894)))


(def v292_l900 (kind/doc #'pj/config))


(def v293_l902 (pj/config))


(deftest t294_l904 (is ((fn [m] (map? m)) v293_l902)))


(def v295_l906 (kind/doc #'pj/set-config!))


(def v296_l908 (kind/doc #'pj/with-config))


(def
 v297_l910
 (pj/with-config {:palette :pastel1} (:palette (pj/config))))


(deftest t298_l913 (is ((fn [p] (= :pastel1 p)) v297_l910)))


(def v300_l919 (kind/doc #'pj/config-key-docs))


(def v301_l921 (count pj/config-key-docs))


(deftest t302_l923 (is ((fn [n] (= 39 n)) v301_l921)))


(def v303_l925 (kind/doc #'pj/plot-option-docs))


(def v304_l927 (count pj/plot-option-docs))


(deftest t305_l929 (is ((fn [n] (= 14 n)) v304_l927)))


(def v306_l931 (kind/doc #'pj/layer-option-docs))


(def v307_l933 (count pj/layer-option-docs))


(deftest t308_l935 (is ((fn [n] (pos? n)) v307_l933)))


(def v310_l939 (kind/doc #'pj/layer-type-lookup))


(def v311_l941 (pj/layer-type-lookup :smooth))


(deftest
 t312_l943
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v311_l941)))


(def v313_l946 (kind/doc #'pj/registered-layer-types))


(def v314_l948 (count (pj/registered-layer-types)))


(deftest t315_l950 (is ((fn [n] (= 25 n)) v314_l948)))


(def v316_l952 (first (pj/registered-layer-types)))


(deftest
 t317_l954
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v316_l952)))


(def v319_l962 (kind/doc #'pj/stat-doc))


(def v320_l964 (pj/stat-doc :linear-model))


(deftest t321_l966 (is ((fn [s] (string? s)) v320_l964)))


(def v322_l968 (kind/doc #'pj/mark-doc))


(def v323_l970 (pj/mark-doc :point))


(deftest t324_l972 (is ((fn [s] (string? s)) v323_l970)))


(def v325_l974 (kind/doc #'pj/position-doc))


(def v326_l976 (pj/position-doc :dodge))


(deftest t327_l978 (is ((fn [s] (string? s)) v326_l976)))


(def v328_l980 (kind/doc #'pj/scale-doc))


(def v329_l982 (pj/scale-doc :linear))


(deftest t330_l984 (is ((fn [s] (string? s)) v329_l982)))


(def v331_l986 (kind/doc #'pj/coord-doc))


(def v332_l988 (pj/coord-doc :cartesian))


(deftest t333_l990 (is ((fn [s] (string? s)) v332_l988)))


(def v334_l992 (kind/doc #'pj/membrane-mark-doc))


(def v335_l994 (pj/membrane-mark-doc :point))


(deftest t336_l996 (is ((fn [s] (string? s)) v335_l994)))


(def v338_l1000 (kind/doc #'pj/save))


(def
 v340_l1004
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t341_l1010 (is (true? v340_l1004)))


(def
 v343_l1015
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
    (mapv (fn* [p1__89105#] (bit-and p1__89105# 255)) (vec bs))))))


(deftest
 t344_l1024
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v343_l1015)))


(def
 v346_l1029
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
    (mapv (fn* [p1__89106#] (bit-and p1__89106# 255)) (vec bs))))))


(deftest t347_l1038 (is ((fn [bs] (= [137 80 78 71] bs)) v346_l1029)))
