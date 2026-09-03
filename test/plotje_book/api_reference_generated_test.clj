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
   (map (fn* [p1__88674#] (Math/sin (* p1__88674# 0.3))) (range 30))}))


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
 v75_l268
 (-> sales (pj/lay-bar :product :revenue {:bar-width 0.4})))


(deftest
 t76_l271
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v75_l268)))


(def
 v78_l278
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t79_l281
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v78_l278)))


(def
 v81_l288
 (->
  {:x [1 2 3 4 5], :y [10 20 15 30 25]}
  (pj/lay-bar :x :y {:bar-width 0.3})))


(deftest
 t82_l291
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v81_l288)))


(def
 v84_l296
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t85_l300
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v84_l296)))


(def v86_l303 (kind/doc #'pj/lay-smooth))


(def
 v88_l307
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t89_l311
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v88_l307)))


(def
 v90_l315
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__88675#]
      (+
       (Math/sin (* p1__88675# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t91_l324
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v90_l315)))


(def v92_l328 (kind/doc #'pj/lay-density))


(def
 v93_l330
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t94_l333
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v93_l330)))


(def v95_l336 (kind/doc #'pj/lay-area))


(def v96_l338 (-> wave (pj/lay-area :x :y)))


(deftest
 t97_l341
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v96_l338)))


(def
 v99_l346
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
 t100_l353
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v99_l346)))


(def v101_l356 (kind/doc #'pj/lay-text))


(def
 v102_l358
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t103_l361
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v102_l358)))


(def v104_l364 (kind/doc #'pj/lay-label))


(def
 v105_l366
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t106_l370
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v105_l366)))


(def v107_l374 (kind/doc #'pj/lay-boxplot))


(def
 v108_l376
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t109_l379
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v108_l376)))


(def v110_l383 (kind/doc #'pj/lay-violin))


(def
 v111_l385
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t112_l388
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v111_l385)))


(def v113_l391 (kind/doc #'pj/lay-errorbar))


(def
 v114_l393
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t115_l397
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v114_l393)))


(def v116_l401 (kind/doc #'pj/lay-lollipop))


(def v117_l403 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t118_l406
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v117_l403)))


(def v119_l410 (kind/doc #'pj/lay-tile))


(def
 v120_l412
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t121_l415
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v120_l412)))


(def v122_l418 (kind/doc #'pj/lay-density-2d))


(def
 v123_l420
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t124_l423
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v123_l420)))


(def v125_l426 (kind/doc #'pj/lay-contour))


(def
 v126_l428
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t127_l431
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v126_l428)))


(def v128_l434 (kind/doc #'pj/lay-ridgeline))


(def
 v129_l436
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t130_l439
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v129_l436)))


(def v131_l442 (kind/doc #'pj/lay-rug))


(def
 v132_l444
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t133_l448
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v132_l444)))


(def v134_l451 (kind/doc #'pj/lay-step))


(def v135_l453 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t136_l457
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v135_l453)))


(def v137_l461 (kind/doc #'pj/lay-summary))


(def
 v138_l463
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t139_l466
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v138_l463)))


(def v140_l470 (kind/doc #'pj/lay-interval-h))


(def
 v141_l472
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
 t142_l477
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v141_l472)))


(def v144_l508 (kind/doc #'pj/lay-rule-v))


(def
 v145_l510
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t146_l514
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v145_l510)))


(def
 v148_l521
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
 t149_l527
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v148_l521)))


(def v150_l531 (kind/doc #'pj/lay-rule-h))


(def
 v151_l533
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t152_l537
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v151_l533)))


(def v153_l541 (kind/doc #'pj/lay-band-v))


(def
 v154_l543
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t155_l547
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v154_l543)))


(def v156_l550 (kind/doc #'pj/lay-band-h))


(def
 v157_l552
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t158_l556
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v157_l552)))


(def v160_l561 (kind/doc #'pj/coord))


(def
 v162_l565
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t163_l568
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v162_l565)))


(def
 v165_l573
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t166_l576
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v165_l573)))


(def v167_l579 (kind/doc #'pj/scale))


(def
 v169_l583
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t170_l586
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v169_l583)))


(def
 v172_l591
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t173_l594
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v172_l591)))


(def
 v175_l600
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t176_l604
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v175_l600)))


(def
 v178_l610
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t179_l614
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
   v178_l610)))


(def
 v181_l621
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t182_l626
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v181_l621)))


(def v183_l632 (kind/doc #'pj/shape-symbols))


(def v184_l634 pj/shape-symbols)


(deftest
 t185_l636
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v184_l634)))


(def
 v187_l641
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t188_l647
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v187_l641)))


(def v190_l653 (kind/doc #'pj/facet))


(def
 v191_l655
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t192_l659
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v191_l655)))


(def v193_l663 (kind/doc #'pj/facet-grid))


(def
 v194_l665
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t195_l669
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v194_l665)))


(def v197_l675 (kind/doc #'pj/arrange))


(def
 v198_l677
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


(deftest t199_l685 (is ((fn [v] (pj/pose? v)) v198_l677)))


(def v200_l687 (kind/doc #'pj/overlay))


(def
 v202_l693
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t203_l698
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v202_l693)))


(def
 v205_l707
 (->
  {:cohort [:a :b :c],
   :growth [12 19 15],
   :tax [3 5 4],
   :spend [7 9 6]}
  (pj/pose :growth :cohort)
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  pj/overlay
  (pj/lay-bar :tax :cohort {:color "#e6550d"})
  (pj/overlay false)
  (pj/lay-bar :spend :cohort {:color "#4daf4a"})))


(deftest
 t206_l715
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 9 (:polygons s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
       (disj (:colors s) "none"))
      (= [2 1] (mapv (comp count :layers) (:poses v))))))
   v205_l707)))


(def v207_l730 (kind/doc #'pj/marginal))


(def
 v208_l732
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t209_l736
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v208_l732)))


(def
 v211_l743
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t212_l747
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v211_l743)))


(def v214_l754 (kind/doc #'pj/plot))


(def v216_l759 (-> tiny (pj/lay-point :x :y)))


(deftest
 t217_l762
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v216_l759)))


(def
 v219_l769
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t220_l772
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v219_l769)))


(def v221_l776 (kind/doc #'pj/options))


(def
 v223_l780
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t224_l784
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v223_l780)))


(def v226_l790 (kind/doc #'pj/pose?))


(def v228_l794 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t229_l796 (is (true? v228_l794)))


(def v230_l798 (kind/doc #'pj/plan?))


(def v232_l802 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t233_l804 (is (true? v232_l802)))


(def v234_l806 (kind/doc #'pj/leaf-plan?))


(def v236_l811 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t237_l813 (is (true? v236_l811)))


(def v238_l815 (kind/doc #'pj/composite-plan?))


(def
 v240_l820
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t241_l824 (is (true? v240_l820)))


(def v242_l826 (kind/doc #'pj/draft?))


(def v244_l831 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t245_l833 (is (true? v244_l831)))


(def v246_l835 (kind/doc #'pj/leaf-draft?))


(def v248_l840 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t249_l842 (is (true? v248_l840)))


(def v250_l844 (kind/doc #'pj/composite-draft?))


(def
 v252_l849
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t253_l853 (is (true? v252_l849)))


(def v254_l855 (kind/doc #'pj/plan-layer?))


(def
 v256_l859
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t257_l861 (is (true? v256_l859)))


(def v258_l863 (kind/doc #'pj/layer-type?))


(def v260_l867 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t261_l869 (is (true? v260_l867)))


(def v262_l871 (kind/doc #'pj/membrane?))


(def v264_l876 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t265_l878 (is (true? v264_l876)))


(def v267_l882 (kind/doc #'pj/draft))


(def
 v269_l889
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t270_l895
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v269_l889)))


(def v271_l899 (kind/doc #'pj/plan))


(def v273_l903 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v274_l907 plan1)


(deftest
 t275_l909
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v274_l907)))


(def v276_l912 (kind/doc #'pj/frames))


(def v278_l918 (-> plan1 pj/frames kind/pprint))


(deftest
 t279_l920
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v278_l918)))


(def
 v281_l933
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
    (fn* [p1__88676#] (-> p1__88676# :frames :panel-box))
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
    (fn* [p1__88677#] (vec (keys (:frames p1__88677#))))
    (:panels f))}))


(deftest
 t282_l945
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__88678#] (= [:panel-box :drawing-area] p1__88678#))
      (:panel-rectangle-keys m))))
   v281_l933)))


(def v284_l959 (kind/doc #'pj/to-drawing))


(def v285_l961 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t286_l963 (is ((fn [v] (= 2 (count v))) v285_l961)))


(def v287_l965 (kind/doc #'pj/to-data))


(def
 v289_l969
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t290_l972
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v289_l969)))


(def
 v292_l977
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__88679#] (Math/round (double p1__88679#)))))))


(deftest t293_l982 (is ((fn [v] (= [2 5] v)) v292_l977)))


(def v294_l984 (kind/doc #'pj/svg-summary))


(def
 v295_l986
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t296_l989
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v295_l986)))


(def v297_l992 (kind/doc #'pj/valid-pose?))


(def v298_l994 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t299_l996 (is (true? v298_l994)))


(def v300_l998 (kind/doc #'pj/explain-pose))


(def v301_l1000 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t302_l1002 (is (nil? v301_l1000)))


(def v303_l1004 (kind/doc #'pj/valid-plan?))


(def v304_l1006 (pj/valid-plan? plan1))


(deftest t305_l1008 (is (true? v304_l1006)))


(def v306_l1010 (kind/doc #'pj/explain-plan))


(def v307_l1012 (pj/explain-plan plan1))


(deftest t308_l1014 (is (nil? v307_l1012)))


(def v310_l1031 (kind/doc #'pj/membrane))


(def
 v312_l1041
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t313_l1047
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v312_l1041)))


(def v314_l1053 (kind/doc #'pj/->pose))


(def v316_l1060 (pj/pose? (pj/->pose tiny)))


(deftest t317_l1062 (is (true? v316_l1060)))


(def v318_l1064 (kind/doc #'pj/infer-mapping))


(def
 v320_l1070
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t321_l1075
 (is ((fn [m] (= {:x :height, :y :weight} m)) v320_l1070)))


(def
 v323_l1080
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t324_l1084 (is (true? v323_l1080)))


(def v325_l1086 (kind/doc #'pj/pose->draft))


(def
 v327_l1092
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t328_l1095 (is (true? v327_l1092)))


(def v329_l1097 (kind/doc #'pj/plan->membrane))


(def v330_l1099 (def m1 (pj/plan->membrane plan1)))


(def v331_l1101 (pj/membrane? m1))


(deftest t332_l1103 (is (true? v331_l1101)))


(def v333_l1105 (kind/doc #'pj/valid-membrane?))


(def v334_l1107 (pj/valid-membrane? m1))


(deftest t335_l1109 (is (true? v334_l1107)))


(def v336_l1111 (kind/doc #'pj/explain-membrane))


(def v337_l1113 (pj/explain-membrane m1))


(deftest t338_l1115 (is (nil? v337_l1113)))


(def v339_l1117 (kind/doc #'pj/membrane->plot))


(def v340_l1119 (first (pj/membrane->plot m1 :svg {})))


(deftest t341_l1121 (is ((fn [v] (= :svg v)) v340_l1119)))


(def v342_l1123 (kind/doc #'pj/plan->plot))


(def v343_l1125 (first (pj/plan->plot plan1 :svg {})))


(deftest t344_l1127 (is ((fn [v] (= :svg v)) v343_l1125)))


(def v346_l1134 (kind/doc #'pj/draft->plan))


(def v347_l1136 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v348_l1138 (pj/plan? (pj/draft->plan draft1)))


(deftest t349_l1140 (is (true? v348_l1138)))


(def v350_l1142 (kind/doc #'pj/draft->membrane))


(def v351_l1144 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t352_l1146 (is (true? v351_l1144)))


(def v353_l1148 (kind/doc #'pj/draft->plot))


(def v354_l1150 (first (pj/draft->plot draft1 :svg {})))


(deftest t355_l1152 (is ((fn [v] (= :svg v)) v354_l1150)))


(def v357_l1156 (kind/doc #'pj/config))


(def v358_l1158 (pj/config))


(deftest t359_l1160 (is ((fn [m] (map? m)) v358_l1158)))


(def v360_l1162 (kind/doc #'pj/set-config!))


(def v361_l1164 (kind/doc #'pj/with-config))


(def
 v362_l1166
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t363_l1169 (is ((fn [p] (= :pastel1 p)) v362_l1166)))


(def v365_l1175 (kind/doc #'pj/config-key-docs))


(def v366_l1177 (count pj/config-key-docs))


(deftest t367_l1179 (is ((fn [n] (= 44 n)) v366_l1177)))


(def v368_l1181 (kind/doc #'pj/plot-option-docs))


(def v369_l1183 (count pj/plot-option-docs))


(deftest t370_l1185 (is ((fn [n] (= 15 n)) v369_l1183)))


(def v371_l1187 (kind/doc #'pj/layer-option-docs))


(def v372_l1189 (count pj/layer-option-docs))


(deftest t373_l1191 (is ((fn [n] (= 55 n)) v372_l1189)))


(def v375_l1195 (kind/doc #'pj/layer-type-lookup))


(def v376_l1197 (pj/layer-type-lookup :smooth))


(deftest
 t377_l1199
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v376_l1197)))


(def v378_l1202 (kind/doc #'pj/registered-layer-types))


(def v379_l1204 (count (pj/registered-layer-types)))


(deftest t380_l1206 (is ((fn [n] (= 25 n)) v379_l1204)))


(def v381_l1208 (first (pj/registered-layer-types)))


(deftest
 t382_l1210
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v381_l1208)))


(def v384_l1218 (kind/doc #'pj/stat-doc))


(def v385_l1220 (pj/stat-doc :linear-model))


(deftest t386_l1222 (is ((fn [s] (string? s)) v385_l1220)))


(def v387_l1224 (kind/doc #'pj/mark-doc))


(def v388_l1226 (pj/mark-doc :point))


(deftest t389_l1228 (is ((fn [s] (string? s)) v388_l1226)))


(def v390_l1230 (kind/doc #'pj/position-doc))


(def v391_l1232 (pj/position-doc :dodge))


(deftest t392_l1234 (is ((fn [s] (string? s)) v391_l1232)))


(def v393_l1236 (kind/doc #'pj/scale-doc))


(def v394_l1238 (pj/scale-doc :linear))


(deftest t395_l1240 (is ((fn [s] (string? s)) v394_l1238)))


(def v396_l1242 (kind/doc #'pj/coord-doc))


(def v397_l1244 (pj/coord-doc :cartesian))


(deftest t398_l1246 (is ((fn [s] (string? s)) v397_l1244)))


(def v399_l1248 (kind/doc #'pj/membrane-mark-doc))


(def v400_l1250 (pj/membrane-mark-doc :point))


(deftest t401_l1252 (is ((fn [s] (string? s)) v400_l1250)))


(def v403_l1256 (kind/doc #'pj/save))


(def
 v405_l1260
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t406_l1266 (is (true? v405_l1260)))


(def
 v408_l1271
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
    (mapv (fn* [p1__88680#] (bit-and p1__88680# 255)) (vec bs))))))


(deftest
 t409_l1280
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v408_l1271)))


(def
 v411_l1285
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
    (mapv (fn* [p1__88681#] (bit-and p1__88681# 255)) (vec bs))))))


(deftest t412_l1294 (is ((fn [bs] (= [137 80 78 71] bs)) v411_l1285)))
