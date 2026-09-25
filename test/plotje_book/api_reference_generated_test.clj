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
   (map (fn* [p1__73475#] (Math/sin (* p1__73475# 0.3))) (range 30))}))


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
      [p1__73476#]
      (+
       (Math/sin (* p1__73476# 0.2))
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


(def v143_l480 (kind/doc #'pj/lay-segment))


(def
 v144_l482
 (->
  {:x0 [1 2 4], :y0 [1 3 2], :x1 [3 3 5], :y1 [2 1 4]}
  (pj/lay-segment :x0 :y0 {:x-end :x1, :y-end :y1, :arrow :end})))


(deftest
 t145_l485
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:lines s)) (= 3 (:polygons s)))))
   v144_l482)))


(def
 v147_l544
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t148_l550
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v147_l544)))


(def
 v150_l558
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t151_l564
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v150_l558)))


(def v152_l568 (kind/doc #'pj/lay-rule-v))


(def
 v153_l570
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t154_l574
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v153_l570)))


(def
 v156_l581
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
 t157_l587
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v156_l581)))


(def v158_l591 (kind/doc #'pj/lay-rule-h))


(def
 v159_l593
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t160_l597
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v159_l593)))


(def v161_l601 (kind/doc #'pj/lay-band-v))


(def
 v162_l603
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t163_l607
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v162_l603)))


(def v164_l610 (kind/doc #'pj/lay-band-h))


(def
 v165_l612
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t166_l616
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v165_l612)))


(def v168_l621 (kind/doc #'pj/coord))


(def
 v170_l625
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t171_l628
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v170_l625)))


(def
 v173_l633
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t174_l636
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v173_l633)))


(def v175_l639 (kind/doc #'pj/scale))


(def
 v177_l643
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t178_l646
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v177_l643)))


(def
 v180_l651
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t181_l654
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v180_l651)))


(def
 v183_l660
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t184_l664
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v183_l660)))


(def
 v186_l670
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t187_l674
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
   v186_l670)))


(def
 v189_l681
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t190_l686
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v189_l681)))


(def v191_l692 (kind/doc #'pj/shape-symbols))


(def v192_l694 (pj/shape-symbols))


(deftest
 t193_l696
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v192_l694)))


(def
 v195_l701
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t196_l707
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v195_l701)))


(def v198_l713 (kind/doc #'pj/facet))


(def
 v199_l715
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t200_l719
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v199_l715)))


(def v201_l723 (kind/doc #'pj/facet-grid))


(def
 v202_l725
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t203_l729
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v202_l725)))


(def v205_l735 (kind/doc #'pj/arrange))


(def
 v206_l737
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


(deftest t207_l745 (is ((fn [v] (pj/pose? v)) v206_l737)))


(def v208_l747 (kind/doc #'pj/overlay))


(def
 v210_l753
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t211_l758
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v210_l753)))


(def
 v213_l767
 (->
  {:cohort [:a :b :c],
   :growth [12 19 15],
   :tax [3 5 4],
   :spend [7 9 6]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d", :overlay true})
  (pj/lay-bar :spend :cohort {:color "#4daf4a"})))


(deftest
 t214_l772
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
       (disj (:colors s) "none")))))
   v213_l767)))


(def
 v216_l787
 [(->
   {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
   pj/overlay
   (pj/lay-bar :growth :cohort)
   (pj/lay-bar :tax :cohort)
   pj/svg-summary
   :panels)
  (->
   {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
   (pj/lay-bar :growth :cohort)
   (pj/lay-bar :tax :cohort)
   pj/overlay
   pj/svg-summary
   :panels)])


(deftest t217_l800 (is ((fn [v] (= [1 1] v)) v216_l787)))


(def v218_l802 (kind/doc #'pj/marginal))


(def
 v219_l804
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t220_l808
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v219_l804)))


(def
 v222_l815
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t223_l819
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v222_l815)))


(def
 v225_l827
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t226_l831
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (mapv
       (fn* [p1__73477#] (-> p1__73477# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v225_l827)))


(def v228_l843 (kind/doc #'pj/plot))


(def v230_l848 (-> tiny (pj/lay-point :x :y)))


(deftest
 t231_l851
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v230_l848)))


(def
 v233_l858
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t234_l861
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v233_l858)))


(def v235_l865 (kind/doc #'pj/options))


(def
 v237_l869
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t238_l873
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v237_l869)))


(def v240_l879 (kind/doc #'pj/pose?))


(def v242_l883 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t243_l885 (is (true? v242_l883)))


(def v244_l887 (kind/doc #'pj/plan?))


(def v246_l891 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t247_l893 (is (true? v246_l891)))


(def v248_l895 (kind/doc #'pj/leaf-plan?))


(def v250_l900 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t251_l902 (is (true? v250_l900)))


(def v252_l904 (kind/doc #'pj/composite-plan?))


(def
 v254_l909
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t255_l913 (is (true? v254_l909)))


(def v256_l915 (kind/doc #'pj/draft?))


(def v258_l920 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t259_l922 (is (true? v258_l920)))


(def v260_l924 (kind/doc #'pj/leaf-draft?))


(def v262_l929 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t263_l931 (is (true? v262_l929)))


(def v264_l933 (kind/doc #'pj/composite-draft?))


(def
 v266_l938
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t267_l942 (is (true? v266_l938)))


(def v268_l944 (kind/doc #'pj/plan-layer?))


(def
 v270_l948
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t271_l950 (is (true? v270_l948)))


(def v272_l952 (kind/doc #'pj/layer-type?))


(def v274_l956 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t275_l958 (is (true? v274_l956)))


(def v276_l960 (kind/doc #'pj/membrane?))


(def v278_l965 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t279_l967 (is (true? v278_l965)))


(def v281_l971 (kind/doc #'pj/draft))


(def
 v283_l978
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t284_l984
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v283_l978)))


(def v285_l988 (kind/doc #'pj/plan))


(def v287_l992 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v288_l996 plan1)


(deftest
 t289_l998
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v288_l996)))


(def v290_l1001 (kind/doc #'pj/frames))


(def v292_l1007 (-> plan1 pj/frames kind/pprint))


(deftest
 t293_l1009
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v292_l1007)))


(def
 v295_l1022
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
    (fn* [p1__73478#] (-> p1__73478# :frames :panel-box))
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
    (fn* [p1__73479#] (vec (keys (:frames p1__73479#))))
    (:panels f))}))


(deftest
 t296_l1034
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__73480#] (= [:panel-box :drawing-area] p1__73480#))
      (:panel-rectangle-keys m))))
   v295_l1022)))


(def v298_l1048 (kind/doc #'pj/to-drawing))


(def v299_l1050 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t300_l1052 (is ((fn [v] (= 2 (count v))) v299_l1050)))


(def v301_l1054 (kind/doc #'pj/to-data))


(def
 v303_l1058
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t304_l1061
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v303_l1058)))


(def
 v306_l1066
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__73481#] (Math/round (double p1__73481#)))))))


(deftest t307_l1071 (is ((fn [v] (= [2 5] v)) v306_l1066)))


(def
 v309_l1078
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
 t310_l1087
 (is ((fn [m] (= "versicolor" (:back m))) v309_l1078)))


(def v311_l1089 (kind/doc #'pj/svg-summary))


(def
 v312_l1091
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t313_l1094
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v312_l1091)))


(def v314_l1097 (kind/doc #'pj/valid-pose?))


(def v315_l1099 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t316_l1101 (is (true? v315_l1099)))


(def v317_l1103 (kind/doc #'pj/explain-pose))


(def v318_l1105 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t319_l1107 (is (nil? v318_l1105)))


(def v320_l1109 (kind/doc #'pj/valid-plan?))


(def v321_l1111 (pj/valid-plan? plan1))


(deftest t322_l1113 (is (true? v321_l1111)))


(def v323_l1115 (kind/doc #'pj/explain-plan))


(def v324_l1117 (pj/explain-plan plan1))


(deftest t325_l1119 (is (nil? v324_l1117)))


(def v327_l1136 (kind/doc #'pj/membrane))


(def
 v329_l1146
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t330_l1152
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v329_l1146)))


(def v331_l1158 (kind/doc #'pj/->pose))


(def v333_l1165 (pj/pose? (pj/->pose tiny)))


(deftest t334_l1167 (is (true? v333_l1165)))


(def v335_l1169 (kind/doc #'pj/infer-mapping))


(def
 v337_l1175
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t338_l1180
 (is ((fn [m] (= {:x :height, :y :weight} m)) v337_l1175)))


(def
 v340_l1185
 (let
  [built (pj/lay-point tiny :x :y)]
  (= (:mapping built) (:mapping (pj/infer-mapping built)))))


(deftest t341_l1189 (is (true? v340_l1185)))


(def v342_l1191 (kind/doc #'pj/pose->draft))


(def
 v344_l1197
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t345_l1200 (is (true? v344_l1197)))


(def v346_l1202 (kind/doc #'pj/plan->membrane))


(def v347_l1204 (def m1 (pj/plan->membrane plan1)))


(def v348_l1206 (pj/membrane? m1))


(deftest t349_l1208 (is (true? v348_l1206)))


(def v350_l1210 (kind/doc #'pj/valid-membrane?))


(def v351_l1212 (pj/valid-membrane? m1))


(deftest t352_l1214 (is (true? v351_l1212)))


(def v353_l1216 (kind/doc #'pj/explain-membrane))


(def v354_l1218 (pj/explain-membrane m1))


(deftest t355_l1220 (is (nil? v354_l1218)))


(def v356_l1222 (kind/doc #'pj/membrane->plot))


(def v357_l1224 (first (pj/membrane->plot m1 :svg {})))


(deftest t358_l1226 (is ((fn [v] (= :svg v)) v357_l1224)))


(def v359_l1228 (kind/doc #'pj/plan->plot))


(def v360_l1230 (first (pj/plan->plot plan1 :svg {})))


(deftest t361_l1232 (is ((fn [v] (= :svg v)) v360_l1230)))


(def v363_l1239 (kind/doc #'pj/draft->plan))


(def v364_l1241 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v365_l1243 (pj/plan? (pj/draft->plan draft1)))


(deftest t366_l1245 (is (true? v365_l1243)))


(def v367_l1247 (kind/doc #'pj/draft->membrane))


(def v368_l1249 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t369_l1251 (is (true? v368_l1249)))


(def v370_l1253 (kind/doc #'pj/draft->plot))


(def v371_l1255 (first (pj/draft->plot draft1 :svg {})))


(deftest t372_l1257 (is ((fn [v] (= :svg v)) v371_l1255)))


(def v374_l1261 (kind/doc #'pj/config))


(def v375_l1263 (pj/config))


(deftest t376_l1265 (is ((fn [m] (map? m)) v375_l1263)))


(def v377_l1267 (kind/doc #'pj/set-config!))


(def v378_l1269 (kind/doc #'pj/with-config))


(def
 v379_l1271
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t380_l1274 (is ((fn [p] (= :pastel1 p)) v379_l1271)))


(def v382_l1280 (kind/doc #'pj/config-key-docs))


(def v383_l1282 (count pj/config-key-docs))


(deftest t384_l1284 (is ((fn [n] (= 44 n)) v383_l1282)))


(def v385_l1286 (kind/doc #'pj/plot-option-docs))


(def v386_l1288 (count pj/plot-option-docs))


(deftest t387_l1290 (is ((fn [n] (= 15 n)) v386_l1288)))


(def v388_l1292 (kind/doc #'pj/layer-option-docs))


(def v389_l1294 (count pj/layer-option-docs))


(deftest t390_l1296 (is ((fn [n] (= 58 n)) v389_l1294)))


(def v392_l1300 (kind/doc #'pj/layer-type-lookup))


(def v393_l1302 (pj/layer-type-lookup :smooth))


(deftest
 t394_l1304
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v393_l1302)))


(def v395_l1307 (kind/doc #'pj/registered-layer-types))


(def v396_l1309 (count (pj/registered-layer-types)))


(deftest t397_l1311 (is ((fn [n] (= 26 n)) v396_l1309)))


(def v398_l1313 (first (pj/registered-layer-types)))


(deftest
 t399_l1315
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v398_l1313)))


(def v401_l1323 (kind/doc #'pj/stat-doc))


(def v402_l1325 (pj/stat-doc :linear-model))


(deftest t403_l1327 (is ((fn [s] (string? s)) v402_l1325)))


(def v404_l1329 (kind/doc #'pj/mark-doc))


(def v405_l1331 (pj/mark-doc :point))


(deftest t406_l1333 (is ((fn [s] (string? s)) v405_l1331)))


(def v407_l1335 (kind/doc #'pj/position-doc))


(def v408_l1337 (pj/position-doc :dodge))


(deftest t409_l1339 (is ((fn [s] (string? s)) v408_l1337)))


(def v410_l1341 (kind/doc #'pj/scale-doc))


(def v411_l1343 (pj/scale-doc :linear))


(deftest t412_l1345 (is ((fn [s] (string? s)) v411_l1343)))


(def v413_l1347 (kind/doc #'pj/coord-doc))


(def v414_l1349 (pj/coord-doc :cartesian))


(deftest t415_l1351 (is ((fn [s] (string? s)) v414_l1349)))


(def v416_l1353 (kind/doc #'pj/membrane-mark-doc))


(def v417_l1355 (pj/membrane-mark-doc :point))


(deftest t418_l1357 (is ((fn [s] (string? s)) v417_l1355)))


(def v420_l1361 (kind/doc #'pj/save))


(def
 v422_l1365
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t423_l1371 (is (true? v422_l1365)))


(def
 v425_l1376
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
    (mapv (fn* [p1__73482#] (bit-and p1__73482# 255)) (vec bs))))))


(deftest
 t426_l1385
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v425_l1376)))


(def
 v428_l1390
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
    (mapv (fn* [p1__73483#] (bit-and p1__73483# 255)) (vec bs))))))


(deftest t429_l1399 (is ((fn [bs] (= [137 80 78 71] bs)) v428_l1390)))
