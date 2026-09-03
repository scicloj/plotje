(ns
 plotje-book.api-reference-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [clojure.test :refer [deftest is]]))


(def
 v3_l28
 (def tiny {:x [1 2 3 4 5], :y [2 4 1 5 3], :group [:a :a :b :b :b]}))


(def
 v4_l32
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def
 v5_l35
 (def
  measurements
  {:treatment ["A" "B" "C" "D"],
   :mean [10.0 15.0 12.0 18.0],
   :ci-lo [8.0 12.0 9.5 15.5],
   :ci-hi [12.0 18.0 14.5 20.5]}))


(def v7_l50 (kind/doc #'pj/pose))


(def
 v9_l54
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t10_l58
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v9_l54)))


(def
 v12_l64
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t13_l69
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v12_l64)))


(def v15_l77 (pj/pose [1 4 1 5 6 2 3 3 3 2 4 5 1 2 3 4]))


(deftest
 t16_l79
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v15_l77)))


(def v17_l83 (kind/doc #'pj/with-data))


(def
 v19_l89
 (def
  scatter-template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def v20_l93 (-> scatter-template (pj/with-data tiny)))


(deftest
 t21_l96
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v20_l93)))


(def
 v23_l101
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point {:color :species})))


(deftest
 t24_l106
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v23_l101)))


(def
 v26_l112
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length, :y :sepal-width})
  pj/lay-point))


(deftest
 t27_l116
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v26_l112)))


(def v28_l120 (kind/doc #'pj/cross))


(def v29_l122 (pj/cross [:a :b] [1 2 3]))


(deftest
 t30_l124
 (is
  ((fn [v] (= [[:a 1] [:a 2] [:a 3] [:b 1] [:b 2] [:b 3]] v))
   v29_l122)))


(def
 v32_l128
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   (pj/cross [:sepal-length :petal-length] [:sepal-width :petal-width])
   {:color :species})))


(deftest
 t33_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v32_l128)))


(def v35_l139 (kind/doc #'pj/lay))


(def
 v37_l147
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t38_l151
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v37_l147)))


(def v40_l162 (kind/doc #'pj/lay-point))


(def
 v41_l164
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t42_l167
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v41_l164)))


(def v43_l170 (kind/doc #'pj/lay-line))


(def
 v44_l172
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__11193#] (Math/sin (* p1__11193# 0.3))) (range 30))}))


(def v45_l175 (-> wave (pj/lay-line :x :y)))


(deftest
 t46_l178
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v45_l175)))


(def v47_l181 (kind/doc #'pj/lay-histogram))


(def
 v48_l183
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t49_l186
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v48_l183)))


(def
 v51_l191
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width]))


(deftest
 t52_l193
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (pos? (:polygons s)))))
   v51_l191)))


(def v53_l197 (kind/doc #'pj/lay-bar))


(def v54_l199 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t55_l202
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v54_l199)))


(def
 v57_l207
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t58_l210
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l207)))


(def
 v60_l215
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t61_l218
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v60_l215)))


(def v63_l224 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t64_l227
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v63_l224)))


(def v66_l233 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t67_l236
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v66_l233)))


(def
 v69_l243
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t70_l246
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v69_l243)))


(def v72_l253 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t73_l256
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v72_l253)))


(def
 v75_l269
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t76_l272
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v75_l269)))


(def
 v78_l277
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t79_l281
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v78_l277)))


(def v80_l284 (kind/doc #'pj/lay-smooth))


(def
 v82_l288
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t83_l292
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v82_l288)))


(def
 v84_l296
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__11194#]
      (+
       (Math/sin (* p1__11194# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t85_l305
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v84_l296)))


(def v86_l309 (kind/doc #'pj/lay-density))


(def
 v87_l311
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t88_l314
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v87_l311)))


(def v89_l317 (kind/doc #'pj/lay-area))


(def v90_l319 (-> wave (pj/lay-area :x :y)))


(deftest
 t91_l322
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v90_l319)))


(def
 v93_l327
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
 t94_l334
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v93_l327)))


(def v95_l337 (kind/doc #'pj/lay-text))


(def
 v96_l339
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t97_l342
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v96_l339)))


(def v98_l345 (kind/doc #'pj/lay-label))


(def
 v99_l347
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t100_l351
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v99_l347)))


(def v101_l355 (kind/doc #'pj/lay-boxplot))


(def
 v102_l357
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t103_l360
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v102_l357)))


(def v104_l364 (kind/doc #'pj/lay-violin))


(def
 v105_l366
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t106_l369
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v105_l366)))


(def v107_l372 (kind/doc #'pj/lay-errorbar))


(def
 v108_l374
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t109_l378
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v108_l374)))


(def v110_l382 (kind/doc #'pj/lay-lollipop))


(def v111_l384 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t112_l387
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v111_l384)))


(def v113_l391 (kind/doc #'pj/lay-tile))


(def
 v114_l393
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t115_l396
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v114_l393)))


(def v116_l399 (kind/doc #'pj/lay-density-2d))


(def
 v117_l401
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t118_l404
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v117_l401)))


(def v119_l407 (kind/doc #'pj/lay-contour))


(def
 v120_l409
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t121_l412
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v120_l409)))


(def v122_l415 (kind/doc #'pj/lay-ridgeline))


(def
 v123_l417
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t124_l420
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v123_l417)))


(def v125_l423 (kind/doc #'pj/lay-rug))


(def
 v126_l425
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t127_l429
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v126_l425)))


(def v128_l432 (kind/doc #'pj/lay-step))


(def v129_l434 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t130_l438
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v129_l434)))


(def v131_l442 (kind/doc #'pj/lay-summary))


(def
 v132_l444
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t133_l447
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v132_l444)))


(def v134_l451 (kind/doc #'pj/lay-interval-h))


(def
 v135_l453
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
 t136_l458
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v135_l453)))


(def v138_l489 (kind/doc #'pj/lay-rule-v))


(def
 v139_l491
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t140_l495
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v139_l491)))


(def
 v142_l502
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
 t143_l508
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v142_l502)))


(def v144_l512 (kind/doc #'pj/lay-rule-h))


(def
 v145_l514
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t146_l518
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v145_l514)))


(def v147_l522 (kind/doc #'pj/lay-band-v))


(def
 v148_l524
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t149_l528
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v148_l524)))


(def v150_l531 (kind/doc #'pj/lay-band-h))


(def
 v151_l533
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t152_l537
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v151_l533)))


(def v154_l542 (kind/doc #'pj/coord))


(def
 v156_l546
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t157_l549
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v156_l546)))


(def
 v159_l554
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t160_l557
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v159_l554)))


(def v161_l560 (kind/doc #'pj/scale))


(def
 v163_l564
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t164_l567
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v163_l564)))


(def
 v166_l572
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t167_l575
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v166_l572)))


(def
 v169_l581
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t170_l585
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v169_l581)))


(def
 v172_l591
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t173_l595
 (is
  ((fn
    [v]
    (=
     16.0
     (->>
      v
      pj/plan
      :size-legend
      :entries
      (map :magnitude)
      (apply max))))
   v172_l591)))


(def
 v175_l602
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t176_l607
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v175_l602)))


(def v177_l613 (kind/doc #'pj/shape-symbols))


(def v178_l615 pj/shape-symbols)


(deftest
 t179_l617
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v178_l615)))


(def
 v181_l622
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t182_l628
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v181_l622)))


(def v184_l634 (kind/doc #'pj/facet))


(def
 v185_l636
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t186_l640
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v185_l636)))


(def v187_l644 (kind/doc #'pj/facet-grid))


(def
 v188_l646
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t189_l650
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v188_l646)))


(def v191_l656 (kind/doc #'pj/arrange))


(def
 v192_l658
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


(deftest t193_l666 (is ((fn [v] (pj/pose? v)) v192_l658)))


(def v195_l670 (kind/doc #'pj/plot))


(def v197_l675 (-> tiny (pj/lay-point :x :y)))


(deftest
 t198_l678
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v197_l675)))


(def
 v200_l685
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t201_l688
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v200_l685)))


(def v202_l692 (kind/doc #'pj/options))


(def
 v204_l696
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t205_l700
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v204_l696)))


(def v207_l706 (kind/doc #'pj/pose?))


(def v209_l710 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t210_l712 (is (true? v209_l710)))


(def v211_l714 (kind/doc #'pj/plan?))


(def v213_l718 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t214_l720 (is (true? v213_l718)))


(def v215_l722 (kind/doc #'pj/leaf-plan?))


(def v217_l727 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t218_l729 (is (true? v217_l727)))


(def v219_l731 (kind/doc #'pj/composite-plan?))


(def
 v221_l736
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t222_l740 (is (true? v221_l736)))


(def v223_l742 (kind/doc #'pj/draft?))


(def v225_l747 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t226_l749 (is (true? v225_l747)))


(def v227_l751 (kind/doc #'pj/leaf-draft?))


(def v229_l756 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t230_l758 (is (true? v229_l756)))


(def v231_l760 (kind/doc #'pj/composite-draft?))


(def
 v233_l765
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t234_l769 (is (true? v233_l765)))


(def v235_l771 (kind/doc #'pj/plan-layer?))


(def
 v237_l775
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t238_l777 (is (true? v237_l775)))


(def v239_l779 (kind/doc #'pj/layer-type?))


(def v241_l783 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t242_l785 (is (true? v241_l783)))


(def v243_l787 (kind/doc #'pj/membrane?))


(def v245_l792 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t246_l794 (is (true? v245_l792)))


(def v248_l798 (kind/doc #'pj/draft))


(def
 v250_l805
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t251_l811
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v250_l805)))


(def v252_l815 (kind/doc #'pj/plan))


(def v254_l819 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v255_l823 plan1)


(deftest
 t256_l825
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v255_l823)))


(def v257_l828 (kind/doc #'pj/frames))


(def v259_l834 (-> plan1 pj/frames kind/pprint))


(deftest
 t260_l836
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v259_l834)))


(def
 v262_l849
 (let
  [f
   (pj/frames
    (pj/arrange
     [(pj/lay-point tiny :x :y) (pj/lay-line tiny :x :y)]
     {:width 700, :height 300}))
   [_ _ cw ch]
   (:canvas f)
   boxes
   (mapv
    (fn* [p1__11195#] (-> p1__11195# :frames :panel-box))
    (:panels f))
   inside?
   (fn
    [[x y w h]]
    (and (>= x 0) (>= y 0) (<= (+ x w) cw) (<= (+ y h) ch)))]
  {:canvas (:canvas f),
   :panel-boxes boxes,
   :every-box-inside-the-canvas (every? inside? boxes),
   :panel-rectangle-keys
   (mapv
    (fn* [p1__11196#] (vec (keys (:frames p1__11196#))))
    (:panels f))}))


(deftest
 t263_l861
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__11197#] (= [:panel-box :drawing-area] p1__11197#))
      (:panel-rectangle-keys m))))
   v262_l849)))


(def v265_l875 (kind/doc #'pj/to-drawing))


(def v266_l877 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t267_l879 (is ((fn [v] (= 2 (count v))) v266_l877)))


(def v268_l881 (kind/doc #'pj/to-data))


(def
 v270_l885
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t271_l888
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v270_l885)))


(def
 v273_l893
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11198#] (Math/round (double p1__11198#)))))))


(deftest t274_l898 (is ((fn [v] (= [2 5] v)) v273_l893)))


(def v275_l900 (kind/doc #'pj/svg-summary))


(def
 v276_l902
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t277_l905
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v276_l902)))


(def v278_l908 (kind/doc #'pj/valid-pose?))


(def v279_l910 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t280_l912 (is (true? v279_l910)))


(def v281_l914 (kind/doc #'pj/explain-pose))


(def v282_l916 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t283_l918 (is (nil? v282_l916)))


(def v284_l920 (kind/doc #'pj/valid-plan?))


(def v285_l922 (pj/valid-plan? plan1))


(deftest t286_l924 (is (true? v285_l922)))


(def v287_l926 (kind/doc #'pj/explain-plan))


(def v288_l928 (pj/explain-plan plan1))


(deftest t289_l930 (is (nil? v288_l928)))


(def v291_l947 (kind/doc #'pj/membrane))


(def
 v293_l957
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t294_l963
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v293_l957)))


(def v295_l969 (kind/doc #'pj/->pose))


(def v297_l976 (pj/pose? (pj/->pose tiny)))


(deftest t298_l978 (is (true? v297_l976)))


(def v299_l980 (kind/doc #'pj/infer-mapping))


(def
 v301_l986
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t302_l991
 (is ((fn [m] (= {:x :height, :y :weight} m)) v301_l986)))


(def
 v304_l996
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t305_l1000 (is (true? v304_l996)))


(def v306_l1002 (kind/doc #'pj/pose->draft))


(def
 v308_l1008
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t309_l1011 (is (true? v308_l1008)))


(def v310_l1013 (kind/doc #'pj/plan->membrane))


(def v311_l1015 (def m1 (pj/plan->membrane plan1)))


(def v312_l1017 (pj/membrane? m1))


(deftest t313_l1019 (is (true? v312_l1017)))


(def v314_l1021 (kind/doc #'pj/valid-membrane?))


(def v315_l1023 (pj/valid-membrane? m1))


(deftest t316_l1025 (is (true? v315_l1023)))


(def v317_l1027 (kind/doc #'pj/explain-membrane))


(def v318_l1029 (pj/explain-membrane m1))


(deftest t319_l1031 (is (nil? v318_l1029)))


(def v320_l1033 (kind/doc #'pj/membrane->plot))


(def v321_l1035 (first (pj/membrane->plot m1 :svg {})))


(deftest t322_l1037 (is ((fn [v] (= :svg v)) v321_l1035)))


(def v323_l1039 (kind/doc #'pj/plan->plot))


(def v324_l1041 (first (pj/plan->plot plan1 :svg {})))


(deftest t325_l1043 (is ((fn [v] (= :svg v)) v324_l1041)))


(def v327_l1050 (kind/doc #'pj/draft->plan))


(def v328_l1052 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v329_l1054 (pj/plan? (pj/draft->plan draft1)))


(deftest t330_l1056 (is (true? v329_l1054)))


(def v331_l1058 (kind/doc #'pj/draft->membrane))


(def v332_l1060 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t333_l1062 (is (true? v332_l1060)))


(def v334_l1064 (kind/doc #'pj/draft->plot))


(def v335_l1066 (first (pj/draft->plot draft1 :svg {})))


(deftest t336_l1068 (is ((fn [v] (= :svg v)) v335_l1066)))


(def v338_l1072 (kind/doc #'pj/config))


(def v339_l1074 (pj/config))


(deftest t340_l1076 (is ((fn [m] (map? m)) v339_l1074)))


(def v341_l1078 (kind/doc #'pj/set-config!))


(def v342_l1080 (kind/doc #'pj/with-config))


(def
 v343_l1082
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t344_l1085 (is ((fn [p] (= :pastel1 p)) v343_l1082)))


(def v346_l1091 (kind/doc #'pj/config-key-docs))


(def v347_l1093 (count pj/config-key-docs))


(deftest t348_l1095 (is ((fn [n] (= 44 n)) v347_l1093)))


(def v349_l1097 (kind/doc #'pj/plot-option-docs))


(def v350_l1099 (count pj/plot-option-docs))


(deftest t351_l1101 (is ((fn [n] (= 15 n)) v350_l1099)))


(def v352_l1103 (kind/doc #'pj/layer-option-docs))


(def v353_l1105 (count pj/layer-option-docs))


(deftest t354_l1107 (is ((fn [n] (= 55 n)) v353_l1105)))


(def v356_l1111 (kind/doc #'pj/layer-type-lookup))


(def v357_l1113 (pj/layer-type-lookup :smooth))


(deftest
 t358_l1115
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v357_l1113)))


(def v359_l1118 (kind/doc #'pj/registered-layer-types))


(def v360_l1120 (count (pj/registered-layer-types)))


(deftest t361_l1122 (is ((fn [n] (= 25 n)) v360_l1120)))


(def v362_l1124 (first (pj/registered-layer-types)))


(deftest
 t363_l1126
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v362_l1124)))


(def v365_l1134 (kind/doc #'pj/stat-doc))


(def v366_l1136 (pj/stat-doc :linear-model))


(deftest t367_l1138 (is ((fn [s] (string? s)) v366_l1136)))


(def v368_l1140 (kind/doc #'pj/mark-doc))


(def v369_l1142 (pj/mark-doc :point))


(deftest t370_l1144 (is ((fn [s] (string? s)) v369_l1142)))


(def v371_l1146 (kind/doc #'pj/position-doc))


(def v372_l1148 (pj/position-doc :dodge))


(deftest t373_l1150 (is ((fn [s] (string? s)) v372_l1148)))


(def v374_l1152 (kind/doc #'pj/scale-doc))


(def v375_l1154 (pj/scale-doc :linear))


(deftest t376_l1156 (is ((fn [s] (string? s)) v375_l1154)))


(def v377_l1158 (kind/doc #'pj/coord-doc))


(def v378_l1160 (pj/coord-doc :cartesian))


(deftest t379_l1162 (is ((fn [s] (string? s)) v378_l1160)))


(def v380_l1164 (kind/doc #'pj/membrane-mark-doc))


(def v381_l1166 (pj/membrane-mark-doc :point))


(deftest t382_l1168 (is ((fn [s] (string? s)) v381_l1166)))


(def v384_l1172 (kind/doc #'pj/save))


(def
 v386_l1176
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t387_l1182 (is (true? v386_l1176)))


(def
 v389_l1187
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
    (mapv (fn* [p1__11199#] (bit-and p1__11199# 255)) (vec bs))))))


(deftest
 t390_l1196
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v389_l1187)))


(def
 v392_l1201
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
    (mapv (fn* [p1__11200#] (bit-and p1__11200# 255)) (vec bs))))))


(deftest t393_l1210 (is ((fn [bs] (= [137 80 78 71] bs)) v392_l1201)))
