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


(def v26_l111 (kind/doc #'pj/lay))


(def
 v28_l119
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t29_l123
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v28_l119)))


(def v31_l134 (kind/doc #'pj/lay-point))


(def
 v32_l136
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t33_l139
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v32_l136)))


(def v34_l142 (kind/doc #'pj/lay-line))


(def
 v35_l144
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__11199#] (Math/sin (* p1__11199# 0.3))) (range 30))}))


(def v36_l147 (-> wave (pj/lay-line :x :y)))


(deftest
 t37_l150
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v36_l147)))


(def v38_l153 (kind/doc #'pj/lay-histogram))


(def
 v39_l155
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t40_l158
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v39_l155)))


(def
 v42_l164
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  {:x [:sepal-length :sepal-width]}))


(deftest
 t43_l166
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v42_l164)))


(def v44_l170 (kind/doc #'pj/lay-bar))


(def v45_l172 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t46_l175
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v45_l172)))


(def
 v48_l180
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t49_l183
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v48_l180)))


(def
 v51_l188
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t52_l191
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v51_l188)))


(def v54_l197 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t55_l200
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v54_l197)))


(def v57_l206 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t58_l209
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l206)))


(def
 v60_l216
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t61_l219
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v60_l216)))


(def v63_l226 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t64_l229
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v63_l226)))


(def
 v66_l241
 (-> sales (pj/lay-bar :product :revenue {:bar-width 0.4})))


(deftest
 t67_l244
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v66_l241)))


(def
 v69_l251
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t70_l254
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v69_l251)))


(def
 v72_l261
 (->
  {:x [1 2 3 4 5], :y [10 20 15 30 25]}
  (pj/lay-bar :x :y {:bar-width 0.3})))


(deftest
 t73_l264
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v72_l261)))


(def
 v75_l269
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t76_l273
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v75_l269)))


(def v77_l276 (kind/doc #'pj/lay-smooth))


(def
 v79_l280
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t80_l284
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v79_l280)))


(def
 v81_l288
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
 t82_l297
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v81_l288)))


(def v83_l301 (kind/doc #'pj/lay-density))


(def
 v84_l303
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t85_l306
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v84_l303)))


(def v86_l309 (kind/doc #'pj/lay-area))


(def v87_l311 (-> wave (pj/lay-area :x :y)))


(deftest
 t88_l314
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v87_l311)))


(def
 v90_l319
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
 t91_l326
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v90_l319)))


(def v92_l329 (kind/doc #'pj/lay-text))


(def
 v93_l331
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t94_l334
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v93_l331)))


(def v95_l337 (kind/doc #'pj/lay-label))


(def
 v96_l339
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t97_l343
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v96_l339)))


(def v98_l347 (kind/doc #'pj/lay-boxplot))


(def
 v99_l349
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t100_l352
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v99_l349)))


(def v101_l356 (kind/doc #'pj/lay-violin))


(def
 v102_l358
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t103_l361
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v102_l358)))


(def v104_l364 (kind/doc #'pj/lay-errorbar))


(def
 v105_l366
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t106_l370
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v105_l366)))


(def v107_l374 (kind/doc #'pj/lay-lollipop))


(def v108_l376 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t109_l379
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v108_l376)))


(def v110_l383 (kind/doc #'pj/lay-tile))


(def
 v111_l385
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t112_l388
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v111_l385)))


(def v113_l391 (kind/doc #'pj/lay-density-2d))


(def
 v114_l393
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t115_l396
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v114_l393)))


(def v116_l399 (kind/doc #'pj/lay-contour))


(def
 v117_l401
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t118_l404
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v117_l401)))


(def v119_l407 (kind/doc #'pj/lay-ridgeline))


(def
 v120_l409
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t121_l412
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v120_l409)))


(def v122_l415 (kind/doc #'pj/lay-rug))


(def
 v123_l417
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t124_l421
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v123_l417)))


(def v125_l424 (kind/doc #'pj/lay-step))


(def v126_l426 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t127_l430
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v126_l426)))


(def v128_l434 (kind/doc #'pj/lay-summary))


(def
 v129_l436
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t130_l439
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v129_l436)))


(def v131_l443 (kind/doc #'pj/lay-interval-h))


(def
 v132_l445
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
 t133_l450
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v132_l445)))


(def v134_l453 (kind/doc #'pj/lay-segment))


(def
 v135_l455
 (->
  {:x0 [1 2 4], :y0 [1 3 2], :x1 [3 3 5], :y1 [2 1 4]}
  (pj/lay-segment :x0 :y0 {:x-end :x1, :y-end :y1, :arrow :end})))


(deftest
 t136_l458
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:lines s)) (= 3 (:polygons s)))))
   v135_l455)))


(def
 v138_l523
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t139_l529
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v138_l523)))


(def
 v141_l537
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t142_l543
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v141_l537)))


(def v143_l547 (kind/doc #'pj/lay-rule-v))


(def
 v144_l549
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t145_l553
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v144_l549)))


(def
 v147_l560
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
 t148_l566
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v147_l560)))


(def v149_l570 (kind/doc #'pj/lay-rule-h))


(def
 v150_l572
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t151_l576
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v150_l572)))


(def v152_l580 (kind/doc #'pj/lay-band-v))


(def
 v153_l582
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t154_l586
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v153_l582)))


(def v155_l589 (kind/doc #'pj/lay-band-h))


(def
 v156_l591
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t157_l595
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v156_l591)))


(def v159_l600 (kind/doc #'pj/coord))


(def
 v161_l604
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t162_l608
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v161_l604)))


(def
 v164_l613
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t165_l617
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v164_l613)))


(def v166_l620 (kind/doc #'pj/scale))


(def
 v168_l624
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t169_l628
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v168_l624)))


(def
 v171_l633
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t172_l637
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v171_l633)))


(def
 v174_l643
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t175_l647
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v174_l643)))


(def
 v177_l653
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t178_l657
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
   v177_l653)))


(def
 v180_l668
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t181_l673
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v180_l668)))


(def v182_l679 (kind/doc #'pj/shape-symbols))


(def v183_l681 (pj/shape-symbols))


(deftest
 t184_l683
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v183_l681)))


(def
 v186_l688
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t187_l694
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v186_l688)))


(def v189_l700 (kind/doc #'pj/facet))


(def
 v190_l702
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t191_l706
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v190_l702)))


(def
 v193_l713
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :class {:cols 4})))


(deftest
 t194_l717
 (is
  ((fn
    [v]
    (and (= {:cols 4} (:layout v)) (= 7 (:panels (pj/svg-summary v)))))
   v193_l713)))


(def
 v196_l723
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t197_l727
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v196_l723)))


(def v199_l733 (kind/doc #'pj/matrix))


(def
 v200_l735
 (pj/matrix
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]))


(deftest
 t201_l737
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
   v200_l735)))


(def v203_l744 (kind/doc #'pj/arrange))


(def
 v204_l746
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 2, :height 250}))


(deftest
 t205_l752
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (= 2 (:panels (pj/svg-summary v)))
     (= [0.0 0.0 600.0 250.0] (:canvas (pj/frames v)))))
   v204_l746)))


(def
 v207_l766
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point {:color :species})
  (pj/lay-smooth :sepal-length :sepal-width)))


(deftest
 t208_l772
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)) (= 1 (:lines s)))))
   v207_l766)))


(def v209_l777 (kind/doc #'pj/marginal))


(def
 v210_l779
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t211_l783
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v210_l779)))


(def
 v213_l790
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t214_l794
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v213_l790)))


(def
 v216_l802
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t217_l806
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
   v216_l802)))


(def v219_l821 (kind/doc #'pj/plot))


(def v221_l826 (-> tiny (pj/lay-point :x :y)))


(deftest
 t222_l829
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v221_l826)))


(def
 v224_l836
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t225_l839
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v224_l836)))


(def v226_l843 (kind/doc #'pj/options))


(def
 v228_l847
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t229_l851
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v228_l847)))


(def v231_l858 (kind/doc #'pj/pose?))


(def v233_l862 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t234_l866 (is (true? v233_l862)))


(def v235_l868 (kind/doc #'pj/plan?))


(def v237_l872 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t238_l874 (is (true? v237_l872)))


(def v239_l876 (kind/doc #'pj/leaf-plan?))


(def v241_l881 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t242_l883 (is (true? v241_l881)))


(def v243_l885 (kind/doc #'pj/composite-plan?))


(def
 v245_l890
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t246_l894 (is (true? v245_l890)))


(def v247_l896 (kind/doc #'pj/draft?))


(def v249_l901 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t250_l903 (is (true? v249_l901)))


(def v251_l905 (kind/doc #'pj/leaf-draft?))


(def v253_l910 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t254_l912 (is (true? v253_l910)))


(def v255_l914 (kind/doc #'pj/composite-draft?))


(def
 v257_l919
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t258_l923 (is (true? v257_l919)))


(def v259_l925 (kind/doc #'pj/plan-layer?))


(def
 v261_l929
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t262_l931 (is (true? v261_l929)))


(def v263_l933 (kind/doc #'pj/layer-type?))


(def v265_l937 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t266_l939 (is (true? v265_l937)))


(def v267_l941 (kind/doc #'pj/membrane?))


(def v269_l946 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t270_l948 (is (true? v269_l946)))


(def v271_l950 (kind/doc #'pj/series-mapping?))


(def
 v273_l955
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t274_l958 (is ((fn [v] (= [true false] v)) v273_l955)))


(def v275_l960 (kind/doc #'pj/series-mapping))


(def
 v277_l965
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t278_l967
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v277_l965)))


(def v280_l971 (kind/doc #'pj/draft))


(def
 v282_l978
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t283_l984
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v282_l978)))


(def v284_l988 (kind/doc #'pj/plan))


(def v286_l992 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v287_l996 plan1)


(deftest
 t288_l998
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v287_l996)))


(def v289_l1001 (kind/doc #'pj/frames))


(def v291_l1007 (-> plan1 pj/frames kind/pprint))


(deftest
 t292_l1011
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v291_l1007)))


(def
 v294_l1031
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
 t295_l1046
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
   v294_l1031)))


(def v297_l1060 (kind/doc #'pj/to-drawing))


(def v298_l1062 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t299_l1068 (is ((fn [v] (= 2 (count v))) v298_l1062)))


(def v300_l1070 (kind/doc #'pj/to-data))


(def
 v302_l1074
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t303_l1080
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v302_l1074)))


(def
 v305_l1085
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11205#] (Math/round (double p1__11205#)))))))


(deftest t306_l1093 (is ((fn [v] (= [2 5] v)) v305_l1085)))


(def
 v308_l1100
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
 t309_l1109
 (is ((fn [m] (= "versicolor" (:back m))) v308_l1100)))


(def v310_l1111 (kind/doc #'pj/svg-summary))


(def
 v311_l1113
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t312_l1117
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v311_l1113)))


(def v313_l1120 (kind/doc #'pj/valid-pose?))


(def v314_l1122 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t315_l1124 (is (true? v314_l1122)))


(def v316_l1126 (kind/doc #'pj/explain-pose))


(def v317_l1128 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t318_l1130 (is (nil? v317_l1128)))


(def v319_l1132 (kind/doc #'pj/valid-plan?))


(def v320_l1134 (pj/valid-plan? plan1))


(deftest t321_l1136 (is (true? v320_l1134)))


(def v322_l1138 (kind/doc #'pj/explain-plan))


(def v323_l1140 (pj/explain-plan plan1))


(deftest t324_l1142 (is (nil? v323_l1140)))


(def v326_l1159 (kind/doc #'pj/membrane))


(def
 v328_l1169
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t329_l1175
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v328_l1169)))


(def v330_l1181 (kind/doc #'pj/->pose))


(def v332_l1188 (pj/pose? (pj/->pose tiny)))


(deftest t333_l1190 (is (true? v332_l1188)))


(def v334_l1192 (kind/doc #'pj/infer-mapping))


(def
 v336_l1198
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t337_l1203
 (is ((fn [m] (= {:x :height, :y :weight} m)) v336_l1198)))


(def
 v339_l1210
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t340_l1214 (is ((fn [v] (= [nil nil] v)) v339_l1210)))


(def
 v342_l1219
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t343_l1224
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v342_l1219)))


(def v344_l1226 (kind/doc #'pj/pose->draft))


(def
 v346_l1232
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t347_l1235 (is (true? v346_l1232)))


(def v348_l1237 (kind/doc #'pj/plan->membrane))


(def v349_l1239 (def m1 (pj/plan->membrane plan1)))


(def v350_l1241 (pj/membrane? m1))


(deftest t351_l1243 (is (true? v350_l1241)))


(def v352_l1245 (kind/doc #'pj/valid-membrane?))


(def v353_l1247 (pj/valid-membrane? m1))


(deftest t354_l1249 (is (true? v353_l1247)))


(def v355_l1251 (kind/doc #'pj/explain-membrane))


(def v356_l1253 (pj/explain-membrane m1))


(deftest t357_l1255 (is (nil? v356_l1253)))


(def v358_l1257 (kind/doc #'pj/membrane->plot))


(def v359_l1259 (first (pj/membrane->plot m1 :svg {})))


(deftest t360_l1261 (is ((fn [v] (= :svg v)) v359_l1259)))


(def v361_l1263 (kind/doc #'pj/plan->plot))


(def v362_l1265 (first (pj/plan->plot plan1 :svg {})))


(deftest t363_l1267 (is ((fn [v] (= :svg v)) v362_l1265)))


(def v365_l1274 (kind/doc #'pj/draft->plan))


(def v366_l1276 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v367_l1278 (pj/plan? (pj/draft->plan draft1)))


(deftest t368_l1280 (is (true? v367_l1278)))


(def v369_l1282 (kind/doc #'pj/draft->membrane))


(def v370_l1284 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t371_l1286 (is (true? v370_l1284)))


(def v372_l1288 (kind/doc #'pj/draft->plot))


(def v373_l1290 (first (pj/draft->plot draft1 :svg {})))


(deftest t374_l1292 (is ((fn [v] (= :svg v)) v373_l1290)))


(def v376_l1296 (kind/doc #'pj/config))


(def v377_l1298 (pj/config))


(deftest t378_l1300 (is ((fn [m] (map? m)) v377_l1298)))


(def v379_l1302 (kind/doc #'pj/set-config!))


(def v380_l1304 (kind/doc #'pj/with-config))


(def
 v381_l1306
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t382_l1309 (is ((fn [p] (= :pastel1 p)) v381_l1306)))


(def
 v384_l1316
 (pj/with-config
  {:theme {:bg "#1a1a2e"}}
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   pj/plot)))


(deftest
 t385_l1321
 (is
  ((fn [v] (boolean (re-find #"rgb\(26,26,46\)" (str v)))) v384_l1316)))


(def v387_l1331 (kind/doc #'pj/config-key-docs))


(def v388_l1333 (count pj/config-key-docs))


(deftest t389_l1335 (is ((fn [n] (= 46 n)) v388_l1333)))


(def v390_l1337 (kind/doc #'pj/plot-option-docs))


(def v391_l1339 (count pj/plot-option-docs))


(deftest t392_l1341 (is ((fn [n] (= 16 n)) v391_l1339)))


(def v393_l1343 (kind/doc #'pj/layer-mapping-docs))


(def v394_l1345 (count pj/layer-mapping-docs))


(deftest t395_l1347 (is ((fn [n] (= 57 n)) v394_l1345)))


(def v397_l1353 (kind/doc #'pj/panel-aesthetic-docs))


(def v398_l1355 (keys pj/panel-aesthetic-docs))


(deftest
 t399_l1357
 (is ((fn [ks] (= #{:col :row} (set ks))) v398_l1355)))


(def v401_l1361 (kind/doc #'pj/aesthetic-roles))


(def
 v402_l1363
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t403_l1365
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v402_l1363)))


(def v405_l1373 (kind/doc #'pj/aesthetic-scales))


(def v406_l1375 (first pj/aesthetic-scales))


(deftest
 t407_l1377
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v406_l1375)))


(def v409_l1383 (kind/doc #'pj/compound-key-aesthetics))


(def v410_l1385 (pj/compound-key-aesthetics))


(deftest
 t411_l1387
 (is ((fn [s] (= #{:group :col :row} s)) v410_l1385)))


(def v413_l1392 (kind/doc #'pj/shape-palette))


(def v414_l1394 (pj/shape-palette))


(deftest
 t415_l1396
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v414_l1394)))


(def v417_l1401 (kind/doc #'pj/layer-type-lookup))


(def v418_l1403 (pj/layer-type-lookup :smooth))


(deftest
 t419_l1405
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v418_l1403)))


(def v420_l1408 (kind/doc #'pj/registered-layer-types))


(def v421_l1410 (count (pj/registered-layer-types)))


(deftest t422_l1412 (is ((fn [n] (= 26 n)) v421_l1410)))


(def v423_l1414 (first (pj/registered-layer-types)))


(deftest
 t424_l1416
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v423_l1414)))


(def v426_l1424 (kind/doc #'pj/stat-doc))


(def v427_l1426 (pj/stat-doc :linear-model))


(deftest t428_l1428 (is ((fn [s] (string? s)) v427_l1426)))


(def v429_l1430 (kind/doc #'pj/mark-doc))


(def v430_l1432 (pj/mark-doc :point))


(deftest t431_l1434 (is ((fn [s] (string? s)) v430_l1432)))


(def v432_l1436 (kind/doc #'pj/position-doc))


(def v433_l1438 (pj/position-doc :dodge))


(deftest t434_l1440 (is ((fn [s] (string? s)) v433_l1438)))


(def v435_l1442 (kind/doc #'pj/scale-doc))


(def v436_l1444 (pj/scale-doc :linear))


(deftest t437_l1446 (is ((fn [s] (string? s)) v436_l1444)))


(def v438_l1448 (kind/doc #'pj/coord-doc))


(def v439_l1450 (pj/coord-doc :cartesian))


(deftest t440_l1452 (is ((fn [s] (string? s)) v439_l1450)))


(def v441_l1454 (kind/doc #'pj/membrane-mark-doc))


(def v442_l1456 (pj/membrane-mark-doc :point))


(deftest t443_l1458 (is ((fn [s] (string? s)) v442_l1456)))


(def v445_l1462 (kind/doc #'pj/save))


(def
 v447_l1466
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t448_l1472 (is (true? v447_l1466)))


(def
 v450_l1478
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
 t451_l1487
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v450_l1478)))


(def
 v453_l1492
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


(deftest t454_l1501 (is ((fn [bs] (= [137 80 78 71] bs)) v453_l1492)))


(def v456_l1514 (kind/doc #'pj/overlay))


(def
 v458_l1519
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t459_l1526 (is ((fn [n] (= 1 n)) v458_l1519)))


(def v460_l1528 (kind/doc #'pj/facet-grid))


(def
 v462_l1533
 (let
  [tips-pose (pj/lay-point (rdatasets/reshape2-tips) :total-bill :tip)]
  (=
   (pj/facet-grid tips-pose :smoker :sex)
   (pj/facet tips-pose {:col :smoker, :row :sex}))))


(deftest t463_l1537 (is (true? v462_l1533)))


(def v464_l1539 (kind/doc #'pj/cross))


(def
 v466_l1545
 (let
  [pairs
   (atom nil)
   printed
   (with-out-str
    (reset!
     pairs
     (pj/cross
      [:sepal-length :petal-length]
      [:sepal-width :petal-width])))]
  {:pairs (deref pairs),
   :warns-with-pj-matrix (some? (re-find #"pj/matrix" printed))}))


(deftest
 t467_l1552
 (is
  ((fn
    [m]
    (and
     (=
      [[:sepal-length :sepal-width]
       [:sepal-length :petal-width]
       [:petal-length :sepal-width]
       [:petal-length :petal-width]]
      (:pairs m))
     (:warns-with-pj-matrix m)))
   v466_l1545)))


(def
 v469_l1562
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix
   [:sepal-length :petal-length]
   [:sepal-width :petal-width])))


(deftest
 t470_l1567
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v469_l1562)))


(def v471_l1571 (kind/doc #'pj/layer-option-docs))


(def v473_l1575 (identical? pj/layer-option-docs pj/layer-mapping-docs))


(deftest t474_l1577 (is (true? v473_l1575)))
