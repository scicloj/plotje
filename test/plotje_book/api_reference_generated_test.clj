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
   (map (fn* [p1__71895#] (Math/sin (* p1__71895# 0.3))) (range 30))}))


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
 v75_l267
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t76_l270
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v75_l267)))


(def
 v78_l275
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t79_l279
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v78_l275)))


(def v80_l282 (kind/doc #'pj/lay-smooth))


(def
 v82_l286
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t83_l290
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v82_l286)))


(def
 v84_l294
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__71896#]
      (+
       (Math/sin (* p1__71896# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t85_l303
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v84_l294)))


(def v86_l307 (kind/doc #'pj/lay-density))


(def
 v87_l309
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t88_l312
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v87_l309)))


(def v89_l315 (kind/doc #'pj/lay-area))


(def v90_l317 (-> wave (pj/lay-area :x :y)))


(deftest
 t91_l320
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v90_l317)))


(def
 v93_l325
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
 t94_l332
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v93_l325)))


(def v95_l335 (kind/doc #'pj/lay-text))


(def
 v96_l337
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t97_l340
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v96_l337)))


(def v98_l343 (kind/doc #'pj/lay-label))


(def
 v99_l345
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t100_l349
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v99_l345)))


(def v101_l353 (kind/doc #'pj/lay-boxplot))


(def
 v102_l355
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t103_l358
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v102_l355)))


(def v104_l362 (kind/doc #'pj/lay-violin))


(def
 v105_l364
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t106_l367
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v105_l364)))


(def v107_l370 (kind/doc #'pj/lay-errorbar))


(def
 v108_l372
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t109_l376
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v108_l372)))


(def v110_l380 (kind/doc #'pj/lay-lollipop))


(def v111_l382 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t112_l385
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v111_l382)))


(def v113_l389 (kind/doc #'pj/lay-tile))


(def
 v114_l391
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t115_l394
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v114_l391)))


(def v116_l397 (kind/doc #'pj/lay-density-2d))


(def
 v117_l399
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t118_l402
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v117_l399)))


(def v119_l405 (kind/doc #'pj/lay-contour))


(def
 v120_l407
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t121_l410
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v120_l407)))


(def v122_l413 (kind/doc #'pj/lay-ridgeline))


(def
 v123_l415
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t124_l418
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v123_l415)))


(def v125_l421 (kind/doc #'pj/lay-rug))


(def
 v126_l423
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t127_l427
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v126_l423)))


(def v128_l430 (kind/doc #'pj/lay-step))


(def v129_l432 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t130_l436
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v129_l432)))


(def v131_l440 (kind/doc #'pj/lay-summary))


(def
 v132_l442
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t133_l445
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v132_l442)))


(def v134_l449 (kind/doc #'pj/lay-interval-h))


(def
 v135_l451
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
 t136_l456
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v135_l451)))


(def v138_l487 (kind/doc #'pj/lay-rule-v))


(def
 v139_l489
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t140_l493
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v139_l489)))


(def
 v142_l500
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
 t143_l506
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v142_l500)))


(def v144_l510 (kind/doc #'pj/lay-rule-h))


(def
 v145_l512
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t146_l516
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v145_l512)))


(def v147_l520 (kind/doc #'pj/lay-band-v))


(def
 v148_l522
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t149_l526
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v148_l522)))


(def v150_l529 (kind/doc #'pj/lay-band-h))


(def
 v151_l531
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t152_l535
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v151_l531)))


(def v154_l540 (kind/doc #'pj/coord))


(def
 v156_l544
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t157_l547
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v156_l544)))


(def
 v159_l552
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t160_l555
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v159_l552)))


(def v161_l558 (kind/doc #'pj/scale))


(def
 v163_l562
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t164_l565
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v163_l562)))


(def
 v166_l570
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t167_l573
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v166_l570)))


(def
 v169_l579
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t170_l583
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v169_l579)))


(def
 v172_l589
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t173_l593
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
   v172_l589)))


(def
 v175_l600
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t176_l605
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v175_l600)))


(def v177_l611 (kind/doc #'pj/shape-symbols))


(def v178_l613 pj/shape-symbols)


(deftest
 t179_l615
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v178_l613)))


(def
 v181_l620
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t182_l626
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v181_l620)))


(def v184_l632 (kind/doc #'pj/facet))


(def
 v185_l634
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t186_l638
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v185_l634)))


(def v187_l642 (kind/doc #'pj/facet-grid))


(def
 v188_l644
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t189_l648
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v188_l644)))


(def v191_l654 (kind/doc #'pj/arrange))


(def
 v192_l656
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


(deftest t193_l664 (is ((fn [v] (pj/pose? v)) v192_l656)))


(def v195_l668 (kind/doc #'pj/plot))


(def v197_l673 (-> tiny (pj/lay-point :x :y)))


(deftest
 t198_l676
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v197_l673)))


(def
 v200_l683
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t201_l686
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v200_l683)))


(def v202_l690 (kind/doc #'pj/options))


(def
 v204_l694
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t205_l698
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v204_l694)))


(def v207_l704 (kind/doc #'pj/pose?))


(def v209_l708 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t210_l710 (is (true? v209_l708)))


(def v211_l712 (kind/doc #'pj/plan?))


(def v213_l716 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t214_l718 (is (true? v213_l716)))


(def v215_l720 (kind/doc #'pj/leaf-plan?))


(def v217_l725 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t218_l727 (is (true? v217_l725)))


(def v219_l729 (kind/doc #'pj/composite-plan?))


(def
 v221_l734
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t222_l738 (is (true? v221_l734)))


(def v223_l740 (kind/doc #'pj/draft?))


(def v225_l745 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t226_l747 (is (true? v225_l745)))


(def v227_l749 (kind/doc #'pj/leaf-draft?))


(def v229_l754 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t230_l756 (is (true? v229_l754)))


(def v231_l758 (kind/doc #'pj/composite-draft?))


(def
 v233_l763
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t234_l767 (is (true? v233_l763)))


(def v235_l769 (kind/doc #'pj/plan-layer?))


(def
 v237_l773
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t238_l775 (is (true? v237_l773)))


(def v239_l777 (kind/doc #'pj/layer-type?))


(def v241_l781 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t242_l783 (is (true? v241_l781)))


(def v243_l785 (kind/doc #'pj/membrane?))


(def v245_l790 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t246_l792 (is (true? v245_l790)))


(def v248_l796 (kind/doc #'pj/draft))


(def
 v250_l803
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t251_l809
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v250_l803)))


(def v252_l813 (kind/doc #'pj/plan))


(def v254_l817 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v255_l821 plan1)


(deftest
 t256_l823
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v255_l821)))


(def v257_l826 (kind/doc #'pj/frames))


(def v259_l832 (-> plan1 pj/frames kind/pprint))


(deftest
 t260_l834
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v259_l832)))


(def
 v262_l847
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
    (fn* [p1__71897#] (-> p1__71897# :frames :panel-box))
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
    (fn* [p1__71898#] (vec (keys (:frames p1__71898#))))
    (:panels f))}))


(deftest
 t263_l859
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__71899#] (= [:panel-box :drawing-area] p1__71899#))
      (:panel-rectangle-keys m))))
   v262_l847)))


(def v265_l873 (kind/doc #'pj/to-drawing))


(def v266_l875 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t267_l877 (is ((fn [v] (= 2 (count v))) v266_l875)))


(def v268_l879 (kind/doc #'pj/to-data))


(def
 v270_l883
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t271_l886
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v270_l883)))


(def
 v273_l891
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__71900#] (Math/round (double p1__71900#)))))))


(deftest t274_l896 (is ((fn [v] (= [2 5] v)) v273_l891)))


(def v275_l898 (kind/doc #'pj/svg-summary))


(def
 v276_l900
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t277_l903
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v276_l900)))


(def v278_l906 (kind/doc #'pj/valid-plan?))


(def v279_l908 (pj/valid-plan? plan1))


(deftest t280_l910 (is (true? v279_l908)))


(def v281_l912 (kind/doc #'pj/explain-plan))


(def v282_l914 (pj/explain-plan plan1))


(deftest t283_l916 (is (nil? v282_l914)))


(def v285_l933 (kind/doc #'pj/membrane))


(def
 v287_l943
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t288_l949
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v287_l943)))


(def v289_l955 (kind/doc #'pj/->pose))


(def v291_l962 (pj/pose? (pj/->pose tiny)))


(deftest t292_l964 (is (true? v291_l962)))


(def v293_l966 (kind/doc #'pj/infer-mapping))


(def
 v295_l972
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t296_l977
 (is ((fn [m] (= {:x :height, :y :weight} m)) v295_l972)))


(def
 v298_l982
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t299_l986 (is (true? v298_l982)))


(def v300_l988 (kind/doc #'pj/pose->draft))


(def
 v302_l994
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t303_l997 (is (true? v302_l994)))


(def v304_l999 (kind/doc #'pj/plan->membrane))


(def v305_l1001 (def m1 (pj/plan->membrane plan1)))


(def v306_l1003 (pj/membrane? m1))


(deftest t307_l1005 (is (true? v306_l1003)))


(def v308_l1007 (kind/doc #'pj/valid-membrane?))


(def v309_l1009 (pj/valid-membrane? m1))


(deftest t310_l1011 (is (true? v309_l1009)))


(def v311_l1013 (kind/doc #'pj/explain-membrane))


(def v312_l1015 (pj/explain-membrane m1))


(deftest t313_l1017 (is (nil? v312_l1015)))


(def v314_l1019 (kind/doc #'pj/membrane->plot))


(def v315_l1021 (first (pj/membrane->plot m1 :svg {})))


(deftest t316_l1023 (is ((fn [v] (= :svg v)) v315_l1021)))


(def v317_l1025 (kind/doc #'pj/plan->plot))


(def v318_l1027 (first (pj/plan->plot plan1 :svg {})))


(deftest t319_l1029 (is ((fn [v] (= :svg v)) v318_l1027)))


(def v321_l1036 (kind/doc #'pj/draft->plan))


(def v322_l1038 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v323_l1040 (pj/plan? (pj/draft->plan draft1)))


(deftest t324_l1042 (is (true? v323_l1040)))


(def v325_l1044 (kind/doc #'pj/draft->membrane))


(def v326_l1046 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t327_l1048 (is (true? v326_l1046)))


(def v328_l1050 (kind/doc #'pj/draft->plot))


(def v329_l1052 (first (pj/draft->plot draft1 :svg {})))


(deftest t330_l1054 (is ((fn [v] (= :svg v)) v329_l1052)))


(def v332_l1058 (kind/doc #'pj/config))


(def v333_l1060 (pj/config))


(deftest t334_l1062 (is ((fn [m] (map? m)) v333_l1060)))


(def v335_l1064 (kind/doc #'pj/set-config!))


(def v336_l1066 (kind/doc #'pj/with-config))


(def
 v337_l1068
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t338_l1071 (is ((fn [p] (= :pastel1 p)) v337_l1068)))


(def v340_l1077 (kind/doc #'pj/config-key-docs))


(def v341_l1079 (count pj/config-key-docs))


(deftest t342_l1081 (is ((fn [n] (= 44 n)) v341_l1079)))


(def v343_l1083 (kind/doc #'pj/plot-option-docs))


(def v344_l1085 (count pj/plot-option-docs))


(deftest t345_l1087 (is ((fn [n] (= 15 n)) v344_l1085)))


(def v346_l1089 (kind/doc #'pj/layer-option-docs))


(def v347_l1091 (count pj/layer-option-docs))


(deftest t348_l1093 (is ((fn [n] (= 54 n)) v347_l1091)))


(def v350_l1097 (kind/doc #'pj/layer-type-lookup))


(def v351_l1099 (pj/layer-type-lookup :smooth))


(deftest
 t352_l1101
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v351_l1099)))


(def v353_l1104 (kind/doc #'pj/registered-layer-types))


(def v354_l1106 (count (pj/registered-layer-types)))


(deftest t355_l1108 (is ((fn [n] (= 25 n)) v354_l1106)))


(def v356_l1110 (first (pj/registered-layer-types)))


(deftest
 t357_l1112
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v356_l1110)))


(def v359_l1120 (kind/doc #'pj/stat-doc))


(def v360_l1122 (pj/stat-doc :linear-model))


(deftest t361_l1124 (is ((fn [s] (string? s)) v360_l1122)))


(def v362_l1126 (kind/doc #'pj/mark-doc))


(def v363_l1128 (pj/mark-doc :point))


(deftest t364_l1130 (is ((fn [s] (string? s)) v363_l1128)))


(def v365_l1132 (kind/doc #'pj/position-doc))


(def v366_l1134 (pj/position-doc :dodge))


(deftest t367_l1136 (is ((fn [s] (string? s)) v366_l1134)))


(def v368_l1138 (kind/doc #'pj/scale-doc))


(def v369_l1140 (pj/scale-doc :linear))


(deftest t370_l1142 (is ((fn [s] (string? s)) v369_l1140)))


(def v371_l1144 (kind/doc #'pj/coord-doc))


(def v372_l1146 (pj/coord-doc :cartesian))


(deftest t373_l1148 (is ((fn [s] (string? s)) v372_l1146)))


(def v374_l1150 (kind/doc #'pj/membrane-mark-doc))


(def v375_l1152 (pj/membrane-mark-doc :point))


(deftest t376_l1154 (is ((fn [s] (string? s)) v375_l1152)))


(def v378_l1158 (kind/doc #'pj/save))


(def
 v380_l1162
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t381_l1168 (is (true? v380_l1162)))


(def
 v383_l1173
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
    (mapv (fn* [p1__71901#] (bit-and p1__71901# 255)) (vec bs))))))


(deftest
 t384_l1182
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v383_l1173)))


(def
 v386_l1187
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
    (mapv (fn* [p1__71902#] (bit-and p1__71902# 255)) (vec bs))))))


(deftest t387_l1196 (is ((fn [bs] (= [137 80 78 71] bs)) v386_l1187)))
