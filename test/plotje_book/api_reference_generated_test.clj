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
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
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
  (pj/cross-matrix [:sepal-length :petal-length] {:color :species})))


(deftest
 t33_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 300 (:points s)))))
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
   (map (fn* [p1__76294#] (Math/sin (* p1__76294# 0.3))) (range 30))}))


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
 v51_l192
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  {:x [:sepal-length :sepal-width]}))


(deftest
 t52_l194
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v51_l192)))


(def v53_l198 (kind/doc #'pj/lay-bar))


(def v54_l200 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t55_l203
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v54_l200)))


(def
 v57_l208
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t58_l211
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l208)))


(def
 v60_l216
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t61_l219
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v60_l216)))


(def v63_l225 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t64_l228
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v63_l225)))


(def v66_l234 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t67_l237
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v66_l234)))


(def
 v69_l244
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t70_l247
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v69_l244)))


(def v72_l254 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t73_l257
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v72_l254)))


(def
 v75_l269
 (-> sales (pj/lay-bar :product :revenue {:bar-width 0.4})))


(deftest
 t76_l272
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v75_l269)))


(def
 v78_l279
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t79_l282
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v78_l279)))


(def
 v81_l289
 (->
  {:x [1 2 3 4 5], :y [10 20 15 30 25]}
  (pj/lay-bar :x :y {:bar-width 0.3})))


(deftest
 t82_l292
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v81_l289)))


(def
 v84_l297
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t85_l301
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v84_l297)))


(def v86_l304 (kind/doc #'pj/lay-smooth))


(def
 v88_l308
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t89_l312
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v88_l308)))


(def
 v90_l316
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__76295#]
      (+
       (Math/sin (* p1__76295# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t91_l325
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v90_l316)))


(def v92_l329 (kind/doc #'pj/lay-density))


(def
 v93_l331
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t94_l334
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v93_l331)))


(def v95_l337 (kind/doc #'pj/lay-area))


(def v96_l339 (-> wave (pj/lay-area :x :y)))


(deftest
 t97_l342
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v96_l339)))


(def
 v99_l347
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
 t100_l354
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v99_l347)))


(def v101_l357 (kind/doc #'pj/lay-text))


(def
 v102_l359
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t103_l362
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v102_l359)))


(def v104_l365 (kind/doc #'pj/lay-label))


(def
 v105_l367
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t106_l371
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v105_l367)))


(def v107_l375 (kind/doc #'pj/lay-boxplot))


(def
 v108_l377
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t109_l380
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v108_l377)))


(def v110_l384 (kind/doc #'pj/lay-violin))


(def
 v111_l386
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t112_l389
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v111_l386)))


(def v113_l392 (kind/doc #'pj/lay-errorbar))


(def
 v114_l394
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t115_l398
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v114_l394)))


(def v116_l402 (kind/doc #'pj/lay-lollipop))


(def v117_l404 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t118_l407
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v117_l404)))


(def v119_l411 (kind/doc #'pj/lay-tile))


(def
 v120_l413
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t121_l416
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v120_l413)))


(def v122_l419 (kind/doc #'pj/lay-density-2d))


(def
 v123_l421
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t124_l424
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v123_l421)))


(def v125_l427 (kind/doc #'pj/lay-contour))


(def
 v126_l429
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t127_l432
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v126_l429)))


(def v128_l435 (kind/doc #'pj/lay-ridgeline))


(def
 v129_l437
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t130_l440
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v129_l437)))


(def v131_l443 (kind/doc #'pj/lay-rug))


(def
 v132_l445
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t133_l449
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v132_l445)))


(def v134_l452 (kind/doc #'pj/lay-step))


(def v135_l454 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t136_l458
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v135_l454)))


(def v137_l462 (kind/doc #'pj/lay-summary))


(def
 v138_l464
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t139_l467
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v138_l464)))


(def v140_l471 (kind/doc #'pj/lay-interval-h))


(def
 v141_l473
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
 t142_l478
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v141_l473)))


(def v143_l481 (kind/doc #'pj/lay-segment))


(def
 v144_l483
 (->
  {:x0 [1 2 4], :y0 [1 3 2], :x1 [3 3 5], :y1 [2 1 4]}
  (pj/lay-segment :x0 :y0 {:x-end :x1, :y-end :y1, :arrow :end})))


(deftest
 t145_l486
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:lines s)) (= 3 (:polygons s)))))
   v144_l483)))


(def
 v147_l545
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t148_l551
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v147_l545)))


(def
 v150_l559
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t151_l565
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v150_l559)))


(def v152_l569 (kind/doc #'pj/lay-rule-v))


(def
 v153_l571
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t154_l575
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v153_l571)))


(def
 v156_l582
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
 t157_l588
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v156_l582)))


(def v158_l592 (kind/doc #'pj/lay-rule-h))


(def
 v159_l594
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t160_l598
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v159_l594)))


(def v161_l602 (kind/doc #'pj/lay-band-v))


(def
 v162_l604
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t163_l608
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v162_l604)))


(def v164_l611 (kind/doc #'pj/lay-band-h))


(def
 v165_l613
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t166_l617
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v165_l613)))


(def v168_l622 (kind/doc #'pj/coord))


(def
 v170_l626
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t171_l629
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v170_l626)))


(def
 v173_l634
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t174_l637
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v173_l634)))


(def v175_l640 (kind/doc #'pj/scale))


(def
 v177_l644
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t178_l647
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v177_l644)))


(def
 v180_l652
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t181_l655
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v180_l652)))


(def
 v183_l661
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t184_l665
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v183_l661)))


(def
 v186_l671
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t187_l675
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
   v186_l671)))


(def
 v189_l682
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t190_l687
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v189_l682)))


(def v191_l693 (kind/doc #'pj/shape-symbols))


(def v192_l695 (pj/shape-symbols))


(deftest
 t193_l697
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v192_l695)))


(def
 v195_l702
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t196_l708
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v195_l702)))


(def v198_l714 (kind/doc #'pj/facet))


(def
 v199_l716
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t200_l720
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v199_l716)))


(def v201_l724 (kind/doc #'pj/facet-grid))


(def
 v202_l726
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t203_l730
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v202_l726)))


(def v205_l736 (kind/doc #'pj/arrange))


(def
 v206_l738
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


(deftest t207_l746 (is ((fn [v] (pj/pose? v)) v206_l738)))


(def v208_l748 (kind/doc #'pj/overlay))


(def
 v210_l753
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t211_l760 (is ((fn [n] (= 1 n)) v210_l753)))


(def v212_l762 (kind/doc #'pj/marginal))


(def
 v213_l764
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t214_l768
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v213_l764)))


(def
 v216_l775
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t217_l779
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v216_l775)))


(def
 v219_l787
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t220_l791
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (mapv
       (fn* [p1__76296#] (-> p1__76296# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v219_l787)))


(def v222_l803 (kind/doc #'pj/plot))


(def v224_l808 (-> tiny (pj/lay-point :x :y)))


(deftest
 t225_l811
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v224_l808)))


(def
 v227_l818
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t228_l821
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v227_l818)))


(def v229_l825 (kind/doc #'pj/options))


(def
 v231_l829
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t232_l833
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v231_l829)))


(def v234_l839 (kind/doc #'pj/pose?))


(def v236_l843 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t237_l845 (is (true? v236_l843)))


(def v238_l847 (kind/doc #'pj/plan?))


(def v240_l851 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t241_l853 (is (true? v240_l851)))


(def v242_l855 (kind/doc #'pj/leaf-plan?))


(def v244_l860 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t245_l862 (is (true? v244_l860)))


(def v246_l864 (kind/doc #'pj/composite-plan?))


(def
 v248_l869
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t249_l873 (is (true? v248_l869)))


(def v250_l875 (kind/doc #'pj/draft?))


(def v252_l880 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t253_l882 (is (true? v252_l880)))


(def v254_l884 (kind/doc #'pj/leaf-draft?))


(def v256_l889 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t257_l891 (is (true? v256_l889)))


(def v258_l893 (kind/doc #'pj/composite-draft?))


(def
 v260_l898
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t261_l902 (is (true? v260_l898)))


(def v262_l904 (kind/doc #'pj/plan-layer?))


(def
 v264_l908
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t265_l910 (is (true? v264_l908)))


(def v266_l912 (kind/doc #'pj/layer-type?))


(def v268_l916 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t269_l918 (is (true? v268_l916)))


(def v270_l920 (kind/doc #'pj/membrane?))


(def v272_l925 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t273_l927 (is (true? v272_l925)))


(def v275_l931 (kind/doc #'pj/draft))


(def
 v277_l938
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t278_l944
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v277_l938)))


(def v279_l948 (kind/doc #'pj/plan))


(def v281_l952 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v282_l956 plan1)


(deftest
 t283_l958
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v282_l956)))


(def v284_l961 (kind/doc #'pj/frames))


(def v286_l967 (-> plan1 pj/frames kind/pprint))


(deftest
 t287_l969
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v286_l967)))


(def
 v289_l982
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
    (fn* [p1__76297#] (-> p1__76297# :frames :panel-box))
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
    (fn* [p1__76298#] (vec (keys (:frames p1__76298#))))
    (:panels f))}))


(deftest
 t290_l994
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__76299#] (= [:panel-box :drawing-area] p1__76299#))
      (:panel-rectangle-keys m))))
   v289_l982)))


(def v292_l1008 (kind/doc #'pj/to-drawing))


(def v293_l1010 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t294_l1012 (is ((fn [v] (= 2 (count v))) v293_l1010)))


(def v295_l1014 (kind/doc #'pj/to-data))


(def
 v297_l1018
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t298_l1021
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v297_l1018)))


(def
 v300_l1026
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__76300#] (Math/round (double p1__76300#)))))))


(deftest t301_l1031 (is ((fn [v] (= [2 5] v)) v300_l1026)))


(def
 v303_l1038
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
 t304_l1047
 (is ((fn [m] (= "versicolor" (:back m))) v303_l1038)))


(def v305_l1049 (kind/doc #'pj/svg-summary))


(def
 v306_l1051
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t307_l1054
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v306_l1051)))


(def v308_l1057 (kind/doc #'pj/valid-pose?))


(def v309_l1059 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t310_l1061 (is (true? v309_l1059)))


(def v311_l1063 (kind/doc #'pj/explain-pose))


(def v312_l1065 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t313_l1067 (is (nil? v312_l1065)))


(def v314_l1069 (kind/doc #'pj/valid-plan?))


(def v315_l1071 (pj/valid-plan? plan1))


(deftest t316_l1073 (is (true? v315_l1071)))


(def v317_l1075 (kind/doc #'pj/explain-plan))


(def v318_l1077 (pj/explain-plan plan1))


(deftest t319_l1079 (is (nil? v318_l1077)))


(def v321_l1096 (kind/doc #'pj/membrane))


(def
 v323_l1106
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t324_l1112
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v323_l1106)))


(def v325_l1118 (kind/doc #'pj/->pose))


(def v327_l1125 (pj/pose? (pj/->pose tiny)))


(deftest t328_l1127 (is (true? v327_l1125)))


(def v329_l1129 (kind/doc #'pj/infer-mapping))


(def
 v331_l1135
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t332_l1140
 (is ((fn [m] (= {:x :height, :y :weight} m)) v331_l1135)))


(def
 v334_l1145
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t335_l1149 (is (true? v334_l1145)))


(def v336_l1151 (kind/doc #'pj/pose->draft))


(def
 v338_l1157
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t339_l1160 (is (true? v338_l1157)))


(def v340_l1162 (kind/doc #'pj/plan->membrane))


(def v341_l1164 (def m1 (pj/plan->membrane plan1)))


(def v342_l1166 (pj/membrane? m1))


(deftest t343_l1168 (is (true? v342_l1166)))


(def v344_l1170 (kind/doc #'pj/valid-membrane?))


(def v345_l1172 (pj/valid-membrane? m1))


(deftest t346_l1174 (is (true? v345_l1172)))


(def v347_l1176 (kind/doc #'pj/explain-membrane))


(def v348_l1178 (pj/explain-membrane m1))


(deftest t349_l1180 (is (nil? v348_l1178)))


(def v350_l1182 (kind/doc #'pj/membrane->plot))


(def v351_l1184 (first (pj/membrane->plot m1 :svg {})))


(deftest t352_l1186 (is ((fn [v] (= :svg v)) v351_l1184)))


(def v353_l1188 (kind/doc #'pj/plan->plot))


(def v354_l1190 (first (pj/plan->plot plan1 :svg {})))


(deftest t355_l1192 (is ((fn [v] (= :svg v)) v354_l1190)))


(def v357_l1199 (kind/doc #'pj/draft->plan))


(def v358_l1201 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v359_l1203 (pj/plan? (pj/draft->plan draft1)))


(deftest t360_l1205 (is (true? v359_l1203)))


(def v361_l1207 (kind/doc #'pj/draft->membrane))


(def v362_l1209 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t363_l1211 (is (true? v362_l1209)))


(def v364_l1213 (kind/doc #'pj/draft->plot))


(def v365_l1215 (first (pj/draft->plot draft1 :svg {})))


(deftest t366_l1217 (is ((fn [v] (= :svg v)) v365_l1215)))


(def v368_l1221 (kind/doc #'pj/config))


(def v369_l1223 (pj/config))


(deftest t370_l1225 (is ((fn [m] (map? m)) v369_l1223)))


(def v371_l1227 (kind/doc #'pj/set-config!))


(def v372_l1229 (kind/doc #'pj/with-config))


(def
 v373_l1231
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t374_l1234 (is ((fn [p] (= :pastel1 p)) v373_l1231)))


(def v376_l1240 (kind/doc #'pj/config-key-docs))


(def v377_l1242 (count pj/config-key-docs))


(deftest t378_l1244 (is ((fn [n] (= 45 n)) v377_l1242)))


(def v379_l1246 (kind/doc #'pj/plot-option-docs))


(def v380_l1248 (count pj/plot-option-docs))


(deftest t381_l1250 (is ((fn [n] (= 15 n)) v380_l1248)))


(def v382_l1252 (kind/doc #'pj/layer-option-docs))


(def v383_l1254 (count pj/layer-option-docs))


(deftest t384_l1256 (is ((fn [n] (= 57 n)) v383_l1254)))


(def v386_l1260 (kind/doc #'pj/layer-type-lookup))


(def v387_l1262 (pj/layer-type-lookup :smooth))


(deftest
 t388_l1264
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v387_l1262)))


(def v389_l1267 (kind/doc #'pj/registered-layer-types))


(def v390_l1269 (count (pj/registered-layer-types)))


(deftest t391_l1271 (is ((fn [n] (= 26 n)) v390_l1269)))


(def v392_l1273 (first (pj/registered-layer-types)))


(deftest
 t393_l1275
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v392_l1273)))


(def v395_l1283 (kind/doc #'pj/stat-doc))


(def v396_l1285 (pj/stat-doc :linear-model))


(deftest t397_l1287 (is ((fn [s] (string? s)) v396_l1285)))


(def v398_l1289 (kind/doc #'pj/mark-doc))


(def v399_l1291 (pj/mark-doc :point))


(deftest t400_l1293 (is ((fn [s] (string? s)) v399_l1291)))


(def v401_l1295 (kind/doc #'pj/position-doc))


(def v402_l1297 (pj/position-doc :dodge))


(deftest t403_l1299 (is ((fn [s] (string? s)) v402_l1297)))


(def v404_l1301 (kind/doc #'pj/scale-doc))


(def v405_l1303 (pj/scale-doc :linear))


(deftest t406_l1305 (is ((fn [s] (string? s)) v405_l1303)))


(def v407_l1307 (kind/doc #'pj/coord-doc))


(def v408_l1309 (pj/coord-doc :cartesian))


(deftest t409_l1311 (is ((fn [s] (string? s)) v408_l1309)))


(def v410_l1313 (kind/doc #'pj/membrane-mark-doc))


(def v411_l1315 (pj/membrane-mark-doc :point))


(deftest t412_l1317 (is ((fn [s] (string? s)) v411_l1315)))


(def v414_l1321 (kind/doc #'pj/save))


(def
 v416_l1325
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t417_l1331 (is (true? v416_l1325)))


(def
 v419_l1336
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
    (mapv (fn* [p1__76301#] (bit-and p1__76301# 255)) (vec bs))))))


(deftest
 t420_l1345
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v419_l1336)))


(def
 v422_l1350
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
    (mapv (fn* [p1__76302#] (bit-and p1__76302# 255)) (vec bs))))))


(deftest t423_l1359 (is ((fn [bs] (= [137 80 78 71] bs)) v422_l1350)))
