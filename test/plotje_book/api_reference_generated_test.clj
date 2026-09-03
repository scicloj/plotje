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
   (map (fn* [p1__72237#] (Math/sin (* p1__72237# 0.3))) (range 30))}))


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
      [p1__72238#]
      (+
       (Math/sin (* p1__72238# 0.2))
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
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  pj/overlay
  (pj/lay-bar :tax :cohort {:color "#e6550d"})
  (pj/overlay false)
  (pj/lay-bar :spend :cohort {:color "#4daf4a"})))


(deftest
 t206_l714
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


(def v207_l729 (kind/doc #'pj/marginal))


(def
 v208_l731
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t209_l735
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v208_l731)))


(def
 v211_l742
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t212_l746
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v211_l742)))


(def v214_l753 (kind/doc #'pj/plot))


(def v216_l758 (-> tiny (pj/lay-point :x :y)))


(deftest
 t217_l761
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v216_l758)))


(def
 v219_l768
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t220_l771
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v219_l768)))


(def v221_l775 (kind/doc #'pj/options))


(def
 v223_l779
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t224_l783
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v223_l779)))


(def v226_l789 (kind/doc #'pj/pose?))


(def v228_l793 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t229_l795 (is (true? v228_l793)))


(def v230_l797 (kind/doc #'pj/plan?))


(def v232_l801 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t233_l803 (is (true? v232_l801)))


(def v234_l805 (kind/doc #'pj/leaf-plan?))


(def v236_l810 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t237_l812 (is (true? v236_l810)))


(def v238_l814 (kind/doc #'pj/composite-plan?))


(def
 v240_l819
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t241_l823 (is (true? v240_l819)))


(def v242_l825 (kind/doc #'pj/draft?))


(def v244_l830 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t245_l832 (is (true? v244_l830)))


(def v246_l834 (kind/doc #'pj/leaf-draft?))


(def v248_l839 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t249_l841 (is (true? v248_l839)))


(def v250_l843 (kind/doc #'pj/composite-draft?))


(def
 v252_l848
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t253_l852 (is (true? v252_l848)))


(def v254_l854 (kind/doc #'pj/plan-layer?))


(def
 v256_l858
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t257_l860 (is (true? v256_l858)))


(def v258_l862 (kind/doc #'pj/layer-type?))


(def v260_l866 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t261_l868 (is (true? v260_l866)))


(def v262_l870 (kind/doc #'pj/membrane?))


(def v264_l875 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t265_l877 (is (true? v264_l875)))


(def v267_l881 (kind/doc #'pj/draft))


(def
 v269_l888
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t270_l894
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v269_l888)))


(def v271_l898 (kind/doc #'pj/plan))


(def v273_l902 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v274_l906 plan1)


(deftest
 t275_l908
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v274_l906)))


(def v276_l911 (kind/doc #'pj/frames))


(def v278_l917 (-> plan1 pj/frames kind/pprint))


(deftest
 t279_l919
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v278_l917)))


(def
 v281_l932
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
    (fn* [p1__72239#] (-> p1__72239# :frames :panel-box))
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
    (fn* [p1__72240#] (vec (keys (:frames p1__72240#))))
    (:panels f))}))


(deftest
 t282_l944
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__72241#] (= [:panel-box :drawing-area] p1__72241#))
      (:panel-rectangle-keys m))))
   v281_l932)))


(def v284_l958 (kind/doc #'pj/to-drawing))


(def v285_l960 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t286_l962 (is ((fn [v] (= 2 (count v))) v285_l960)))


(def v287_l964 (kind/doc #'pj/to-data))


(def
 v289_l968
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t290_l971
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v289_l968)))


(def
 v292_l976
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__72242#] (Math/round (double p1__72242#)))))))


(deftest t293_l981 (is ((fn [v] (= [2 5] v)) v292_l976)))


(def v294_l983 (kind/doc #'pj/svg-summary))


(def
 v295_l985
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t296_l988
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v295_l985)))


(def v297_l991 (kind/doc #'pj/valid-pose?))


(def v298_l993 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t299_l995 (is (true? v298_l993)))


(def v300_l997 (kind/doc #'pj/explain-pose))


(def v301_l999 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t302_l1001 (is (nil? v301_l999)))


(def v303_l1003 (kind/doc #'pj/valid-plan?))


(def v304_l1005 (pj/valid-plan? plan1))


(deftest t305_l1007 (is (true? v304_l1005)))


(def v306_l1009 (kind/doc #'pj/explain-plan))


(def v307_l1011 (pj/explain-plan plan1))


(deftest t308_l1013 (is (nil? v307_l1011)))


(def v310_l1030 (kind/doc #'pj/membrane))


(def
 v312_l1040
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t313_l1046
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v312_l1040)))


(def v314_l1052 (kind/doc #'pj/->pose))


(def v316_l1059 (pj/pose? (pj/->pose tiny)))


(deftest t317_l1061 (is (true? v316_l1059)))


(def v318_l1063 (kind/doc #'pj/infer-mapping))


(def
 v320_l1069
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t321_l1074
 (is ((fn [m] (= {:x :height, :y :weight} m)) v320_l1069)))


(def
 v323_l1079
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t324_l1083 (is (true? v323_l1079)))


(def v325_l1085 (kind/doc #'pj/pose->draft))


(def
 v327_l1091
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t328_l1094 (is (true? v327_l1091)))


(def v329_l1096 (kind/doc #'pj/plan->membrane))


(def v330_l1098 (def m1 (pj/plan->membrane plan1)))


(def v331_l1100 (pj/membrane? m1))


(deftest t332_l1102 (is (true? v331_l1100)))


(def v333_l1104 (kind/doc #'pj/valid-membrane?))


(def v334_l1106 (pj/valid-membrane? m1))


(deftest t335_l1108 (is (true? v334_l1106)))


(def v336_l1110 (kind/doc #'pj/explain-membrane))


(def v337_l1112 (pj/explain-membrane m1))


(deftest t338_l1114 (is (nil? v337_l1112)))


(def v339_l1116 (kind/doc #'pj/membrane->plot))


(def v340_l1118 (first (pj/membrane->plot m1 :svg {})))


(deftest t341_l1120 (is ((fn [v] (= :svg v)) v340_l1118)))


(def v342_l1122 (kind/doc #'pj/plan->plot))


(def v343_l1124 (first (pj/plan->plot plan1 :svg {})))


(deftest t344_l1126 (is ((fn [v] (= :svg v)) v343_l1124)))


(def v346_l1133 (kind/doc #'pj/draft->plan))


(def v347_l1135 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v348_l1137 (pj/plan? (pj/draft->plan draft1)))


(deftest t349_l1139 (is (true? v348_l1137)))


(def v350_l1141 (kind/doc #'pj/draft->membrane))


(def v351_l1143 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t352_l1145 (is (true? v351_l1143)))


(def v353_l1147 (kind/doc #'pj/draft->plot))


(def v354_l1149 (first (pj/draft->plot draft1 :svg {})))


(deftest t355_l1151 (is ((fn [v] (= :svg v)) v354_l1149)))


(def v357_l1155 (kind/doc #'pj/config))


(def v358_l1157 (pj/config))


(deftest t359_l1159 (is ((fn [m] (map? m)) v358_l1157)))


(def v360_l1161 (kind/doc #'pj/set-config!))


(def v361_l1163 (kind/doc #'pj/with-config))


(def
 v362_l1165
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t363_l1168 (is ((fn [p] (= :pastel1 p)) v362_l1165)))


(def v365_l1174 (kind/doc #'pj/config-key-docs))


(def v366_l1176 (count pj/config-key-docs))


(deftest t367_l1178 (is ((fn [n] (= 44 n)) v366_l1176)))


(def v368_l1180 (kind/doc #'pj/plot-option-docs))


(def v369_l1182 (count pj/plot-option-docs))


(deftest t370_l1184 (is ((fn [n] (= 15 n)) v369_l1182)))


(def v371_l1186 (kind/doc #'pj/layer-option-docs))


(def v372_l1188 (count pj/layer-option-docs))


(deftest t373_l1190 (is ((fn [n] (= 55 n)) v372_l1188)))


(def v375_l1194 (kind/doc #'pj/layer-type-lookup))


(def v376_l1196 (pj/layer-type-lookup :smooth))


(deftest
 t377_l1198
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v376_l1196)))


(def v378_l1201 (kind/doc #'pj/registered-layer-types))


(def v379_l1203 (count (pj/registered-layer-types)))


(deftest t380_l1205 (is ((fn [n] (= 25 n)) v379_l1203)))


(def v381_l1207 (first (pj/registered-layer-types)))


(deftest
 t382_l1209
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v381_l1207)))


(def v384_l1217 (kind/doc #'pj/stat-doc))


(def v385_l1219 (pj/stat-doc :linear-model))


(deftest t386_l1221 (is ((fn [s] (string? s)) v385_l1219)))


(def v387_l1223 (kind/doc #'pj/mark-doc))


(def v388_l1225 (pj/mark-doc :point))


(deftest t389_l1227 (is ((fn [s] (string? s)) v388_l1225)))


(def v390_l1229 (kind/doc #'pj/position-doc))


(def v391_l1231 (pj/position-doc :dodge))


(deftest t392_l1233 (is ((fn [s] (string? s)) v391_l1231)))


(def v393_l1235 (kind/doc #'pj/scale-doc))


(def v394_l1237 (pj/scale-doc :linear))


(deftest t395_l1239 (is ((fn [s] (string? s)) v394_l1237)))


(def v396_l1241 (kind/doc #'pj/coord-doc))


(def v397_l1243 (pj/coord-doc :cartesian))


(deftest t398_l1245 (is ((fn [s] (string? s)) v397_l1243)))


(def v399_l1247 (kind/doc #'pj/membrane-mark-doc))


(def v400_l1249 (pj/membrane-mark-doc :point))


(deftest t401_l1251 (is ((fn [s] (string? s)) v400_l1249)))


(def v403_l1255 (kind/doc #'pj/save))


(def
 v405_l1259
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t406_l1265 (is (true? v405_l1259)))


(def
 v408_l1270
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
    (mapv (fn* [p1__72243#] (bit-and p1__72243# 255)) (vec bs))))))


(deftest
 t409_l1279
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v408_l1270)))


(def
 v411_l1284
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
    (mapv (fn* [p1__72244#] (bit-and p1__72244# 255)) (vec bs))))))


(deftest t412_l1293 (is ((fn [bs] (= [137 80 78 71] bs)) v411_l1284)))
