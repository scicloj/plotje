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


(def v7_l51 (kind/doc #'pj/pose))


(def
 v9_l55
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t10_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v9_l55)))


(def
 v12_l65
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t13_l70
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v12_l65)))


(def v15_l78 (pj/pose [1 4 1 5 6 2 3 3 3 2 4 5 1 2 3 4]))


(deftest
 t16_l80
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v15_l78)))


(def v17_l84 (kind/doc #'pj/with-data))


(def
 v19_l90
 (def
  scatter-template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def v20_l94 (-> scatter-template (pj/with-data tiny)))


(deftest
 t21_l97
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v20_l94)))


(def
 v23_l101
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length, :y :sepal-width})
  pj/lay-point))


(deftest
 t24_l105
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v23_l101)))


(def v25_l109 (kind/doc #'pj/cross))


(def
 v27_l118
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :petal-length]},
    :row {:y [:sepal-length :petal-length]}})))


(deftest
 t28_l123
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 300 (:points s)))))
   v27_l118)))


(def v30_l129 (kind/doc #'pj/lay))


(def
 v32_l137
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t33_l141
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v32_l137)))


(def v35_l152 (kind/doc #'pj/lay-point))


(def
 v36_l154
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t37_l157
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v36_l154)))


(def v38_l160 (kind/doc #'pj/lay-line))


(def
 v39_l162
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__11193#] (Math/sin (* p1__11193# 0.3))) (range 30))}))


(def v40_l165 (-> wave (pj/lay-line :x :y)))


(deftest
 t41_l168
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v40_l165)))


(def v42_l171 (kind/doc #'pj/lay-histogram))


(def
 v43_l173
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t44_l176
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v43_l173)))


(def
 v46_l182
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  {:x [:sepal-length :sepal-width]}))


(deftest
 t47_l184
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v46_l182)))


(def v48_l188 (kind/doc #'pj/lay-bar))


(def v49_l190 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t50_l193
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v49_l190)))


(def
 v52_l198
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t53_l201
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v52_l198)))


(def
 v55_l206
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t56_l209
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v55_l206)))


(def v58_l215 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t59_l218
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v58_l215)))


(def v61_l224 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t62_l227
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v61_l224)))


(def
 v64_l234
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t65_l237
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v64_l234)))


(def v67_l244 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t68_l247
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v67_l244)))


(def
 v70_l259
 (-> sales (pj/lay-bar :product :revenue {:bar-width 0.4})))


(deftest
 t71_l262
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v70_l259)))


(def
 v73_l269
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t74_l272
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v73_l269)))


(def
 v76_l279
 (->
  {:x [1 2 3 4 5], :y [10 20 15 30 25]}
  (pj/lay-bar :x :y {:bar-width 0.3})))


(deftest
 t77_l282
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v76_l279)))


(def
 v79_l287
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t80_l291
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v79_l287)))


(def v81_l294 (kind/doc #'pj/lay-smooth))


(def
 v83_l298
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t84_l302
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v83_l298)))


(def
 v85_l306
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
 t86_l315
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v85_l306)))


(def v87_l319 (kind/doc #'pj/lay-density))


(def
 v88_l321
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t89_l324
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v88_l321)))


(def v90_l327 (kind/doc #'pj/lay-area))


(def v91_l329 (-> wave (pj/lay-area :x :y)))


(deftest
 t92_l332
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v91_l329)))


(def
 v94_l337
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
 t95_l344
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v94_l337)))


(def v96_l347 (kind/doc #'pj/lay-text))


(def
 v97_l349
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t98_l352
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v97_l349)))


(def v99_l355 (kind/doc #'pj/lay-label))


(def
 v100_l357
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t101_l361
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v100_l357)))


(def v102_l365 (kind/doc #'pj/lay-boxplot))


(def
 v103_l367
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t104_l370
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v103_l367)))


(def v105_l374 (kind/doc #'pj/lay-violin))


(def
 v106_l376
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t107_l379
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v106_l376)))


(def v108_l382 (kind/doc #'pj/lay-errorbar))


(def
 v109_l384
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t110_l388
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v109_l384)))


(def v111_l392 (kind/doc #'pj/lay-lollipop))


(def v112_l394 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t113_l397
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v112_l394)))


(def v114_l401 (kind/doc #'pj/lay-tile))


(def
 v115_l403
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t116_l406
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v115_l403)))


(def v117_l409 (kind/doc #'pj/lay-density-2d))


(def
 v118_l411
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t119_l414
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v118_l411)))


(def v120_l417 (kind/doc #'pj/lay-contour))


(def
 v121_l419
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t122_l422
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v121_l419)))


(def v123_l425 (kind/doc #'pj/lay-ridgeline))


(def
 v124_l427
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t125_l430
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v124_l427)))


(def v126_l433 (kind/doc #'pj/lay-rug))


(def
 v127_l435
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t128_l439
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v127_l435)))


(def v129_l442 (kind/doc #'pj/lay-step))


(def v130_l444 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t131_l448
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v130_l444)))


(def v132_l452 (kind/doc #'pj/lay-summary))


(def
 v133_l454
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t134_l457
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v133_l454)))


(def v135_l461 (kind/doc #'pj/lay-interval-h))


(def
 v136_l463
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
 t137_l468
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v136_l463)))


(def v138_l471 (kind/doc #'pj/lay-segment))


(def
 v139_l473
 (->
  {:x0 [1 2 4], :y0 [1 3 2], :x1 [3 3 5], :y1 [2 1 4]}
  (pj/lay-segment :x0 :y0 {:x-end :x1, :y-end :y1, :arrow :end})))


(deftest
 t140_l476
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:lines s)) (= 3 (:polygons s)))))
   v139_l473)))


(def
 v142_l539
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t143_l545
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v142_l539)))


(def
 v145_l553
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t146_l559
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v145_l553)))


(def v147_l563 (kind/doc #'pj/lay-rule-v))


(def
 v148_l565
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t149_l569
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v148_l565)))


(def
 v151_l576
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
 t152_l582
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v151_l576)))


(def v153_l586 (kind/doc #'pj/lay-rule-h))


(def
 v154_l588
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t155_l592
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v154_l588)))


(def v156_l596 (kind/doc #'pj/lay-band-v))


(def
 v157_l598
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t158_l602
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v157_l598)))


(def v159_l605 (kind/doc #'pj/lay-band-h))


(def
 v160_l607
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t161_l611
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v160_l607)))


(def v163_l616 (kind/doc #'pj/coord))


(def
 v165_l620
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t166_l623
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v165_l620)))


(def
 v168_l628
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t169_l631
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v168_l628)))


(def v170_l634 (kind/doc #'pj/scale))


(def
 v172_l638
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t173_l641
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v172_l638)))


(def
 v175_l646
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t176_l649
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v175_l646)))


(def
 v178_l655
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t179_l659
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v178_l655)))


(def
 v181_l665
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t182_l669
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
   v181_l665)))


(def
 v184_l676
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t185_l681
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v184_l676)))


(def v186_l687 (kind/doc #'pj/shape-symbols))


(def v187_l689 (pj/shape-symbols))


(deftest
 t188_l691
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v187_l689)))


(def
 v190_l696
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t191_l702
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v190_l696)))


(def v193_l708 (kind/doc #'pj/facet))


(def
 v194_l710
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t195_l714
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v194_l710)))


(def
 v197_l721
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t198_l725
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v197_l721)))


(def v200_l731 (kind/doc #'pj/arrange))


(def
 v201_l733
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 2, :height 250}))


(deftest
 t202_l739
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (= 2 (:panels (pj/svg-summary v)))
     (= [0.0 0.0 600.0 250.0] (:canvas (pj/frames v)))))
   v201_l733)))


(def
 v204_l747
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point {:color :species})))


(deftest
 t205_l752
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v204_l747)))


(def v206_l756 (kind/doc #'pj/marginal))


(def
 v207_l758
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t208_l762
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v207_l758)))


(def
 v210_l769
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t211_l773
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v210_l769)))


(def
 v213_l781
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t214_l785
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (mapv
       (fn* [p1__11195#] (-> p1__11195# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v213_l781)))


(def v216_l797 (kind/doc #'pj/plot))


(def v218_l802 (-> tiny (pj/lay-point :x :y)))


(deftest
 t219_l805
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v218_l802)))


(def
 v221_l812
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t222_l815
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v221_l812)))


(def v223_l819 (kind/doc #'pj/options))


(def
 v225_l823
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t226_l827
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v225_l823)))


(def v228_l834 (kind/doc #'pj/pose?))


(def v230_l838 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t231_l840 (is (true? v230_l838)))


(def v232_l842 (kind/doc #'pj/plan?))


(def v234_l846 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t235_l848 (is (true? v234_l846)))


(def v236_l850 (kind/doc #'pj/leaf-plan?))


(def v238_l855 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t239_l857 (is (true? v238_l855)))


(def v240_l859 (kind/doc #'pj/composite-plan?))


(def
 v242_l864
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t243_l868 (is (true? v242_l864)))


(def v244_l870 (kind/doc #'pj/draft?))


(def v246_l875 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t247_l877 (is (true? v246_l875)))


(def v248_l879 (kind/doc #'pj/leaf-draft?))


(def v250_l884 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t251_l886 (is (true? v250_l884)))


(def v252_l888 (kind/doc #'pj/composite-draft?))


(def
 v254_l893
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t255_l897 (is (true? v254_l893)))


(def v256_l899 (kind/doc #'pj/plan-layer?))


(def
 v258_l903
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t259_l905 (is (true? v258_l903)))


(def v260_l907 (kind/doc #'pj/layer-type?))


(def v262_l911 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t263_l913 (is (true? v262_l911)))


(def v264_l915 (kind/doc #'pj/membrane?))


(def v266_l920 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t267_l922 (is (true? v266_l920)))


(def v268_l924 (kind/doc #'pj/series-mapping?))


(def
 v270_l929
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t271_l932 (is ((fn [v] (= [true false] v)) v270_l929)))


(def v272_l934 (kind/doc #'pj/series-mapping))


(def
 v274_l939
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t275_l941
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v274_l939)))


(def v277_l945 (kind/doc #'pj/draft))


(def
 v279_l952
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t280_l958
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v279_l952)))


(def v281_l962 (kind/doc #'pj/plan))


(def v283_l966 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v284_l970 plan1)


(deftest
 t285_l972
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v284_l970)))


(def v286_l975 (kind/doc #'pj/frames))


(def v288_l981 (-> plan1 pj/frames kind/pprint))


(deftest
 t289_l983
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v288_l981)))


(def
 v291_l996
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
    (fn* [p1__11196#] (-> p1__11196# :frames :panel-box))
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
    (fn* [p1__11197#] (vec (keys (:frames p1__11197#))))
    (:panels f))}))


(deftest
 t292_l1008
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__11198#] (= [:panel-box :drawing-area] p1__11198#))
      (:panel-rectangle-keys m))))
   v291_l996)))


(def v294_l1022 (kind/doc #'pj/to-drawing))


(def v295_l1024 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t296_l1026 (is ((fn [v] (= 2 (count v))) v295_l1024)))


(def v297_l1028 (kind/doc #'pj/to-data))


(def
 v299_l1032
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t300_l1035
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v299_l1032)))


(def
 v302_l1040
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11199#] (Math/round (double p1__11199#)))))))


(deftest t303_l1045 (is ((fn [v] (= [2 5] v)) v302_l1040)))


(def
 v305_l1052
 (let
  [panel
   (->
    {:species ["setosa" "versicolor" "virginica"],
     :count [12.0 19.0 8.0]}
    (pj/lay-bar :species :count)
    pj/frames
    :panels
    first)]
  {:in 1.5,
   :drawing-x (first (pj/to-drawing panel 1.5 10.0)),
   :back
   (first
    (pj/to-data panel (first (pj/to-drawing panel 1.5 10.0)) 10.0))}))


(deftest
 t306_l1061
 (is ((fn [m] (= "versicolor" (:back m))) v305_l1052)))


(def v307_l1063 (kind/doc #'pj/svg-summary))


(def
 v308_l1065
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t309_l1068
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v308_l1065)))


(def v310_l1071 (kind/doc #'pj/valid-pose?))


(def v311_l1073 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t312_l1075 (is (true? v311_l1073)))


(def v313_l1077 (kind/doc #'pj/explain-pose))


(def v314_l1079 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t315_l1081 (is (nil? v314_l1079)))


(def v316_l1083 (kind/doc #'pj/valid-plan?))


(def v317_l1085 (pj/valid-plan? plan1))


(deftest t318_l1087 (is (true? v317_l1085)))


(def v319_l1089 (kind/doc #'pj/explain-plan))


(def v320_l1091 (pj/explain-plan plan1))


(deftest t321_l1093 (is (nil? v320_l1091)))


(def v323_l1110 (kind/doc #'pj/membrane))


(def
 v325_l1120
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t326_l1126
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v325_l1120)))


(def v327_l1132 (kind/doc #'pj/->pose))


(def v329_l1139 (pj/pose? (pj/->pose tiny)))


(deftest t330_l1141 (is (true? v329_l1139)))


(def v331_l1143 (kind/doc #'pj/infer-mapping))


(def
 v333_l1149
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t334_l1154
 (is ((fn [m] (= {:x :height, :y :weight} m)) v333_l1149)))


(def
 v336_l1161
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t337_l1165 (is ((fn [v] (= [nil nil] v)) v336_l1161)))


(def
 v339_l1170
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t340_l1175
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v339_l1170)))


(def v341_l1177 (kind/doc #'pj/pose->draft))


(def
 v343_l1183
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t344_l1186 (is (true? v343_l1183)))


(def v345_l1188 (kind/doc #'pj/plan->membrane))


(def v346_l1190 (def m1 (pj/plan->membrane plan1)))


(def v347_l1192 (pj/membrane? m1))


(deftest t348_l1194 (is (true? v347_l1192)))


(def v349_l1196 (kind/doc #'pj/valid-membrane?))


(def v350_l1198 (pj/valid-membrane? m1))


(deftest t351_l1200 (is (true? v350_l1198)))


(def v352_l1202 (kind/doc #'pj/explain-membrane))


(def v353_l1204 (pj/explain-membrane m1))


(deftest t354_l1206 (is (nil? v353_l1204)))


(def v355_l1208 (kind/doc #'pj/membrane->plot))


(def v356_l1210 (first (pj/membrane->plot m1 :svg {})))


(deftest t357_l1212 (is ((fn [v] (= :svg v)) v356_l1210)))


(def v358_l1214 (kind/doc #'pj/plan->plot))


(def v359_l1216 (first (pj/plan->plot plan1 :svg {})))


(deftest t360_l1218 (is ((fn [v] (= :svg v)) v359_l1216)))


(def v362_l1225 (kind/doc #'pj/draft->plan))


(def v363_l1227 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v364_l1229 (pj/plan? (pj/draft->plan draft1)))


(deftest t365_l1231 (is (true? v364_l1229)))


(def v366_l1233 (kind/doc #'pj/draft->membrane))


(def v367_l1235 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t368_l1237 (is (true? v367_l1235)))


(def v369_l1239 (kind/doc #'pj/draft->plot))


(def v370_l1241 (first (pj/draft->plot draft1 :svg {})))


(deftest t371_l1243 (is ((fn [v] (= :svg v)) v370_l1241)))


(def v373_l1247 (kind/doc #'pj/config))


(def v374_l1249 (pj/config))


(deftest t375_l1251 (is ((fn [m] (map? m)) v374_l1249)))


(def v376_l1253 (kind/doc #'pj/set-config!))


(def v377_l1255 (kind/doc #'pj/with-config))


(def
 v378_l1257
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t379_l1260 (is ((fn [p] (= :pastel1 p)) v378_l1257)))


(def v381_l1266 (kind/doc #'pj/config-key-docs))


(def v382_l1268 (count pj/config-key-docs))


(deftest t383_l1270 (is ((fn [n] (= 48 n)) v382_l1268)))


(def v384_l1272 (kind/doc #'pj/plot-option-docs))


(def v385_l1274 (count pj/plot-option-docs))


(deftest t386_l1276 (is ((fn [n] (= 14 n)) v385_l1274)))


(def v387_l1278 (kind/doc #'pj/layer-mapping-docs))


(def v388_l1280 (count pj/layer-mapping-docs))


(deftest t389_l1282 (is ((fn [n] (= 57 n)) v388_l1280)))


(def v391_l1288 (kind/doc #'pj/panel-aesthetic-docs))


(def v392_l1290 (keys pj/panel-aesthetic-docs))


(deftest
 t393_l1292
 (is ((fn [ks] (= #{:col :row} (set ks))) v392_l1290)))


(def v395_l1296 (kind/doc #'pj/aesthetic-roles))


(def
 v396_l1298
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t397_l1300
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v396_l1298)))


(def v399_l1308 (kind/doc #'pj/aesthetic-scales))


(def v400_l1310 (first pj/aesthetic-scales))


(deftest
 t401_l1312
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v400_l1310)))


(def v403_l1318 (kind/doc #'pj/compound-key-aesthetics))


(def v404_l1320 (pj/compound-key-aesthetics))


(deftest
 t405_l1322
 (is ((fn [s] (= #{:group :col :row} s)) v404_l1320)))


(def v407_l1327 (kind/doc #'pj/shape-palette))


(def v408_l1329 (pj/shape-palette))


(deftest
 t409_l1331
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v408_l1329)))


(def v411_l1336 (kind/doc #'pj/layer-type-lookup))


(def v412_l1338 (pj/layer-type-lookup :smooth))


(deftest
 t413_l1340
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v412_l1338)))


(def v414_l1343 (kind/doc #'pj/registered-layer-types))


(def v415_l1345 (count (pj/registered-layer-types)))


(deftest t416_l1347 (is ((fn [n] (= 26 n)) v415_l1345)))


(def v417_l1349 (first (pj/registered-layer-types)))


(deftest
 t418_l1351
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v417_l1349)))


(def v420_l1359 (kind/doc #'pj/stat-doc))


(def v421_l1361 (pj/stat-doc :linear-model))


(deftest t422_l1363 (is ((fn [s] (string? s)) v421_l1361)))


(def v423_l1365 (kind/doc #'pj/mark-doc))


(def v424_l1367 (pj/mark-doc :point))


(deftest t425_l1369 (is ((fn [s] (string? s)) v424_l1367)))


(def v426_l1371 (kind/doc #'pj/position-doc))


(def v427_l1373 (pj/position-doc :dodge))


(deftest t428_l1375 (is ((fn [s] (string? s)) v427_l1373)))


(def v429_l1377 (kind/doc #'pj/scale-doc))


(def v430_l1379 (pj/scale-doc :linear))


(deftest t431_l1381 (is ((fn [s] (string? s)) v430_l1379)))


(def v432_l1383 (kind/doc #'pj/coord-doc))


(def v433_l1385 (pj/coord-doc :cartesian))


(deftest t434_l1387 (is ((fn [s] (string? s)) v433_l1385)))


(def v435_l1389 (kind/doc #'pj/membrane-mark-doc))


(def v436_l1391 (pj/membrane-mark-doc :point))


(deftest t437_l1393 (is ((fn [s] (string? s)) v436_l1391)))


(def v439_l1397 (kind/doc #'pj/save))


(def
 v441_l1401
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t442_l1407 (is (true? v441_l1401)))


(def
 v444_l1412
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
    (mapv (fn* [p1__11200#] (bit-and p1__11200# 255)) (vec bs))))))


(deftest
 t445_l1421
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v444_l1412)))


(def
 v447_l1426
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
    (mapv (fn* [p1__11201#] (bit-and p1__11201# 255)) (vec bs))))))


(deftest t448_l1435 (is ((fn [bs] (= [137 80 78 71] bs)) v447_l1426)))


(def v450_l1445 (kind/doc #'pj/overlay))


(def
 v452_l1450
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t453_l1457 (is ((fn [n] (= 1 n)) v452_l1450)))


(def v454_l1459 (kind/doc #'pj/facet-grid))


(def
 v456_l1464
 (let
  [tips-pose (pj/lay-point (rdatasets/reshape2-tips) :total-bill :tip)]
  (=
   (pj/facet-grid tips-pose :smoker :sex)
   (pj/facet tips-pose {:col :smoker, :row :sex}))))


(deftest t457_l1468 (is (true? v456_l1464)))
