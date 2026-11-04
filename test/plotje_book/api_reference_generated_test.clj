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
   (map (fn* [p1__11199#] (Math/sin (* p1__11199# 0.3))) (range 30))}))


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
      [p1__11200#]
      (+
       (Math/sin (* p1__11200# 0.2))
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


(def v200_l731 (kind/doc #'pj/matrix))


(def
 v201_l733
 (pj/matrix
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]))


(deftest
 t202_l735
 (is
  ((fn
    [v]
    (and
     (= 9 (:panels (pj/svg-summary v)))
     (=
      v
      (pj/facet
       (rdatasets/datasets-iris)
       {:col {:x [:sepal-length :sepal-width :petal-length]},
        :row {:y [:sepal-length :sepal-width :petal-length]}}))))
   v201_l733)))


(def v204_l742 (kind/doc #'pj/arrange))


(def
 v205_l744
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 2, :height 250}))


(deftest
 t206_l750
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (= 2 (:panels (pj/svg-summary v)))
     (= [0.0 0.0 600.0 250.0] (:canvas (pj/frames v)))))
   v205_l744)))


(def
 v208_l758
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point {:color :species})))


(deftest
 t209_l763
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v208_l758)))


(def v210_l767 (kind/doc #'pj/marginal))


(def
 v211_l769
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t212_l773
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v211_l769)))


(def
 v214_l780
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t215_l784
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v214_l780)))


(def
 v217_l792
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t218_l796
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (mapv
       (fn* [p1__11201#] (-> p1__11201# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v217_l792)))


(def v220_l808 (kind/doc #'pj/plot))


(def v222_l813 (-> tiny (pj/lay-point :x :y)))


(deftest
 t223_l816
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v222_l813)))


(def
 v225_l823
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t226_l826
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v225_l823)))


(def v227_l830 (kind/doc #'pj/options))


(def
 v229_l834
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t230_l838
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v229_l834)))


(def v232_l845 (kind/doc #'pj/pose?))


(def v234_l849 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t235_l851 (is (true? v234_l849)))


(def v236_l853 (kind/doc #'pj/plan?))


(def v238_l857 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t239_l859 (is (true? v238_l857)))


(def v240_l861 (kind/doc #'pj/leaf-plan?))


(def v242_l866 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t243_l868 (is (true? v242_l866)))


(def v244_l870 (kind/doc #'pj/composite-plan?))


(def
 v246_l875
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t247_l879 (is (true? v246_l875)))


(def v248_l881 (kind/doc #'pj/draft?))


(def v250_l886 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t251_l888 (is (true? v250_l886)))


(def v252_l890 (kind/doc #'pj/leaf-draft?))


(def v254_l895 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t255_l897 (is (true? v254_l895)))


(def v256_l899 (kind/doc #'pj/composite-draft?))


(def
 v258_l904
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t259_l908 (is (true? v258_l904)))


(def v260_l910 (kind/doc #'pj/plan-layer?))


(def
 v262_l914
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t263_l916 (is (true? v262_l914)))


(def v264_l918 (kind/doc #'pj/layer-type?))


(def v266_l922 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t267_l924 (is (true? v266_l922)))


(def v268_l926 (kind/doc #'pj/membrane?))


(def v270_l931 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t271_l933 (is (true? v270_l931)))


(def v272_l935 (kind/doc #'pj/series-mapping?))


(def
 v274_l940
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t275_l943 (is ((fn [v] (= [true false] v)) v274_l940)))


(def v276_l945 (kind/doc #'pj/series-mapping))


(def
 v278_l950
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t279_l952
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v278_l950)))


(def v281_l956 (kind/doc #'pj/draft))


(def
 v283_l963
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t284_l969
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v283_l963)))


(def v285_l973 (kind/doc #'pj/plan))


(def v287_l977 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v288_l981 plan1)


(deftest
 t289_l983
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v288_l981)))


(def v290_l986 (kind/doc #'pj/frames))


(def v292_l992 (-> plan1 pj/frames kind/pprint))


(deftest
 t293_l994
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v292_l992)))


(def
 v295_l1007
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
    (fn* [p1__11202#] (-> p1__11202# :frames :panel-box))
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
    (fn* [p1__11203#] (vec (keys (:frames p1__11203#))))
    (:panels f))}))


(deftest
 t296_l1019
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__11204#] (= [:panel-box :drawing-area] p1__11204#))
      (:panel-rectangle-keys m))))
   v295_l1007)))


(def v298_l1033 (kind/doc #'pj/to-drawing))


(def v299_l1035 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t300_l1037 (is ((fn [v] (= 2 (count v))) v299_l1035)))


(def v301_l1039 (kind/doc #'pj/to-data))


(def
 v303_l1043
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t304_l1046
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v303_l1043)))


(def
 v306_l1051
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11205#] (Math/round (double p1__11205#)))))))


(deftest t307_l1056 (is ((fn [v] (= [2 5] v)) v306_l1051)))


(def
 v309_l1063
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
 t310_l1072
 (is ((fn [m] (= "versicolor" (:back m))) v309_l1063)))


(def v311_l1074 (kind/doc #'pj/svg-summary))


(def
 v312_l1076
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t313_l1079
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v312_l1076)))


(def v314_l1082 (kind/doc #'pj/valid-pose?))


(def v315_l1084 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t316_l1086 (is (true? v315_l1084)))


(def v317_l1088 (kind/doc #'pj/explain-pose))


(def v318_l1090 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t319_l1092 (is (nil? v318_l1090)))


(def v320_l1094 (kind/doc #'pj/valid-plan?))


(def v321_l1096 (pj/valid-plan? plan1))


(deftest t322_l1098 (is (true? v321_l1096)))


(def v323_l1100 (kind/doc #'pj/explain-plan))


(def v324_l1102 (pj/explain-plan plan1))


(deftest t325_l1104 (is (nil? v324_l1102)))


(def v327_l1121 (kind/doc #'pj/membrane))


(def
 v329_l1131
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t330_l1137
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v329_l1131)))


(def v331_l1143 (kind/doc #'pj/->pose))


(def v333_l1150 (pj/pose? (pj/->pose tiny)))


(deftest t334_l1152 (is (true? v333_l1150)))


(def v335_l1154 (kind/doc #'pj/infer-mapping))


(def
 v337_l1160
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t338_l1165
 (is ((fn [m] (= {:x :height, :y :weight} m)) v337_l1160)))


(def
 v340_l1172
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t341_l1176 (is ((fn [v] (= [nil nil] v)) v340_l1172)))


(def
 v343_l1181
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t344_l1186
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v343_l1181)))


(def v345_l1188 (kind/doc #'pj/pose->draft))


(def
 v347_l1194
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t348_l1197 (is (true? v347_l1194)))


(def v349_l1199 (kind/doc #'pj/plan->membrane))


(def v350_l1201 (def m1 (pj/plan->membrane plan1)))


(def v351_l1203 (pj/membrane? m1))


(deftest t352_l1205 (is (true? v351_l1203)))


(def v353_l1207 (kind/doc #'pj/valid-membrane?))


(def v354_l1209 (pj/valid-membrane? m1))


(deftest t355_l1211 (is (true? v354_l1209)))


(def v356_l1213 (kind/doc #'pj/explain-membrane))


(def v357_l1215 (pj/explain-membrane m1))


(deftest t358_l1217 (is (nil? v357_l1215)))


(def v359_l1219 (kind/doc #'pj/membrane->plot))


(def v360_l1221 (first (pj/membrane->plot m1 :svg {})))


(deftest t361_l1223 (is ((fn [v] (= :svg v)) v360_l1221)))


(def v362_l1225 (kind/doc #'pj/plan->plot))


(def v363_l1227 (first (pj/plan->plot plan1 :svg {})))


(deftest t364_l1229 (is ((fn [v] (= :svg v)) v363_l1227)))


(def v366_l1236 (kind/doc #'pj/draft->plan))


(def v367_l1238 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v368_l1240 (pj/plan? (pj/draft->plan draft1)))


(deftest t369_l1242 (is (true? v368_l1240)))


(def v370_l1244 (kind/doc #'pj/draft->membrane))


(def v371_l1246 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t372_l1248 (is (true? v371_l1246)))


(def v373_l1250 (kind/doc #'pj/draft->plot))


(def v374_l1252 (first (pj/draft->plot draft1 :svg {})))


(deftest t375_l1254 (is ((fn [v] (= :svg v)) v374_l1252)))


(def v377_l1258 (kind/doc #'pj/config))


(def v378_l1260 (pj/config))


(deftest t379_l1262 (is ((fn [m] (map? m)) v378_l1260)))


(def v380_l1264 (kind/doc #'pj/set-config!))


(def v381_l1266 (kind/doc #'pj/with-config))


(def
 v382_l1268
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t383_l1271 (is ((fn [p] (= :pastel1 p)) v382_l1268)))


(def v385_l1277 (kind/doc #'pj/config-key-docs))


(def v386_l1279 (count pj/config-key-docs))


(deftest t387_l1281 (is ((fn [n] (= 48 n)) v386_l1279)))


(def v388_l1283 (kind/doc #'pj/plot-option-docs))


(def v389_l1285 (count pj/plot-option-docs))


(deftest t390_l1287 (is ((fn [n] (= 16 n)) v389_l1285)))


(def v391_l1289 (kind/doc #'pj/layer-mapping-docs))


(def v392_l1291 (count pj/layer-mapping-docs))


(deftest t393_l1293 (is ((fn [n] (= 57 n)) v392_l1291)))


(def v395_l1299 (kind/doc #'pj/panel-aesthetic-docs))


(def v396_l1301 (keys pj/panel-aesthetic-docs))


(deftest
 t397_l1303
 (is ((fn [ks] (= #{:col :row} (set ks))) v396_l1301)))


(def v399_l1307 (kind/doc #'pj/aesthetic-roles))


(def
 v400_l1309
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t401_l1311
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v400_l1309)))


(def v403_l1319 (kind/doc #'pj/aesthetic-scales))


(def v404_l1321 (first pj/aesthetic-scales))


(deftest
 t405_l1323
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v404_l1321)))


(def v407_l1329 (kind/doc #'pj/compound-key-aesthetics))


(def v408_l1331 (pj/compound-key-aesthetics))


(deftest
 t409_l1333
 (is ((fn [s] (= #{:group :col :row} s)) v408_l1331)))


(def v411_l1338 (kind/doc #'pj/shape-palette))


(def v412_l1340 (pj/shape-palette))


(deftest
 t413_l1342
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v412_l1340)))


(def v415_l1347 (kind/doc #'pj/layer-type-lookup))


(def v416_l1349 (pj/layer-type-lookup :smooth))


(deftest
 t417_l1351
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v416_l1349)))


(def v418_l1354 (kind/doc #'pj/registered-layer-types))


(def v419_l1356 (count (pj/registered-layer-types)))


(deftest t420_l1358 (is ((fn [n] (= 26 n)) v419_l1356)))


(def v421_l1360 (first (pj/registered-layer-types)))


(deftest
 t422_l1362
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v421_l1360)))


(def v424_l1370 (kind/doc #'pj/stat-doc))


(def v425_l1372 (pj/stat-doc :linear-model))


(deftest t426_l1374 (is ((fn [s] (string? s)) v425_l1372)))


(def v427_l1376 (kind/doc #'pj/mark-doc))


(def v428_l1378 (pj/mark-doc :point))


(deftest t429_l1380 (is ((fn [s] (string? s)) v428_l1378)))


(def v430_l1382 (kind/doc #'pj/position-doc))


(def v431_l1384 (pj/position-doc :dodge))


(deftest t432_l1386 (is ((fn [s] (string? s)) v431_l1384)))


(def v433_l1388 (kind/doc #'pj/scale-doc))


(def v434_l1390 (pj/scale-doc :linear))


(deftest t435_l1392 (is ((fn [s] (string? s)) v434_l1390)))


(def v436_l1394 (kind/doc #'pj/coord-doc))


(def v437_l1396 (pj/coord-doc :cartesian))


(deftest t438_l1398 (is ((fn [s] (string? s)) v437_l1396)))


(def v439_l1400 (kind/doc #'pj/membrane-mark-doc))


(def v440_l1402 (pj/membrane-mark-doc :point))


(deftest t441_l1404 (is ((fn [s] (string? s)) v440_l1402)))


(def v443_l1408 (kind/doc #'pj/save))


(def
 v445_l1412
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t446_l1418 (is (true? v445_l1412)))


(def
 v448_l1423
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
    (mapv (fn* [p1__11206#] (bit-and p1__11206# 255)) (vec bs))))))


(deftest
 t449_l1432
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v448_l1423)))


(def
 v451_l1437
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
    (mapv (fn* [p1__11207#] (bit-and p1__11207# 255)) (vec bs))))))


(deftest t452_l1446 (is ((fn [bs] (= [137 80 78 71] bs)) v451_l1437)))


(def v454_l1456 (kind/doc #'pj/overlay))


(def
 v456_l1461
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t457_l1468 (is ((fn [n] (= 1 n)) v456_l1461)))


(def v458_l1470 (kind/doc #'pj/facet-grid))


(def
 v460_l1475
 (let
  [tips-pose (pj/lay-point (rdatasets/reshape2-tips) :total-bill :tip)]
  (=
   (pj/facet-grid tips-pose :smoker :sex)
   (pj/facet tips-pose {:col :smoker, :row :sex}))))


(deftest t461_l1479 (is (true? v460_l1475)))
