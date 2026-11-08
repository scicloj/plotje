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
 v207_l762
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point {:color :species})
  (pj/lay-smooth :sepal-length :sepal-width)))


(deftest
 t208_l768
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)) (= 1 (:lines s)))))
   v207_l762)))


(def v209_l773 (kind/doc #'pj/marginal))


(def
 v210_l775
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t211_l779
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v210_l775)))


(def
 v213_l786
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t214_l790
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v213_l786)))


(def
 v216_l798
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t217_l802
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
   v216_l798)))


(def v219_l817 (kind/doc #'pj/plot))


(def v221_l822 (-> tiny (pj/lay-point :x :y)))


(deftest
 t222_l825
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v221_l822)))


(def
 v224_l832
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t225_l835
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v224_l832)))


(def v226_l839 (kind/doc #'pj/options))


(def
 v228_l843
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t229_l847
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v228_l843)))


(def v231_l854 (kind/doc #'pj/pose?))


(def v233_l858 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t234_l862 (is (true? v233_l858)))


(def v235_l864 (kind/doc #'pj/plan?))


(def v237_l868 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t238_l870 (is (true? v237_l868)))


(def v239_l872 (kind/doc #'pj/leaf-plan?))


(def v241_l877 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t242_l879 (is (true? v241_l877)))


(def v243_l881 (kind/doc #'pj/composite-plan?))


(def
 v245_l886
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t246_l890 (is (true? v245_l886)))


(def v247_l892 (kind/doc #'pj/draft?))


(def v249_l897 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t250_l899 (is (true? v249_l897)))


(def v251_l901 (kind/doc #'pj/leaf-draft?))


(def v253_l906 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t254_l908 (is (true? v253_l906)))


(def v255_l910 (kind/doc #'pj/composite-draft?))


(def
 v257_l915
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t258_l919 (is (true? v257_l915)))


(def v259_l921 (kind/doc #'pj/plan-layer?))


(def
 v261_l925
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t262_l927 (is (true? v261_l925)))


(def v263_l929 (kind/doc #'pj/layer-type?))


(def v265_l933 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t266_l935 (is (true? v265_l933)))


(def v267_l937 (kind/doc #'pj/membrane?))


(def v269_l942 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t270_l944 (is (true? v269_l942)))


(def v271_l946 (kind/doc #'pj/series-mapping?))


(def
 v273_l951
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t274_l954 (is ((fn [v] (= [true false] v)) v273_l951)))


(def v275_l956 (kind/doc #'pj/series-mapping))


(def
 v277_l961
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t278_l963
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v277_l961)))


(def v280_l967 (kind/doc #'pj/draft))


(def
 v282_l974
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t283_l980
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v282_l974)))


(def v284_l984 (kind/doc #'pj/plan))


(def v286_l988 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v287_l992 plan1)


(deftest
 t288_l994
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v287_l992)))


(def v289_l997 (kind/doc #'pj/frames))


(def v291_l1003 (-> plan1 pj/frames kind/pprint))


(deftest
 t292_l1007
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v291_l1003)))


(def
 v294_l1027
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
 t295_l1042
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
   v294_l1027)))


(def v297_l1056 (kind/doc #'pj/to-drawing))


(def v298_l1058 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t299_l1064 (is ((fn [v] (= 2 (count v))) v298_l1058)))


(def v300_l1066 (kind/doc #'pj/to-data))


(def
 v302_l1070
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t303_l1076
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v302_l1070)))


(def
 v305_l1081
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11205#] (Math/round (double p1__11205#)))))))


(deftest t306_l1089 (is ((fn [v] (= [2 5] v)) v305_l1081)))


(def
 v308_l1096
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
 t309_l1105
 (is ((fn [m] (= "versicolor" (:back m))) v308_l1096)))


(def v310_l1107 (kind/doc #'pj/svg-summary))


(def
 v311_l1109
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t312_l1113
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v311_l1109)))


(def v313_l1116 (kind/doc #'pj/valid-pose?))


(def v314_l1118 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t315_l1120 (is (true? v314_l1118)))


(def v316_l1122 (kind/doc #'pj/explain-pose))


(def v317_l1124 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t318_l1126 (is (nil? v317_l1124)))


(def v319_l1128 (kind/doc #'pj/valid-plan?))


(def v320_l1130 (pj/valid-plan? plan1))


(deftest t321_l1132 (is (true? v320_l1130)))


(def v322_l1134 (kind/doc #'pj/explain-plan))


(def v323_l1136 (pj/explain-plan plan1))


(deftest t324_l1138 (is (nil? v323_l1136)))


(def v326_l1155 (kind/doc #'pj/membrane))


(def
 v328_l1165
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t329_l1171
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v328_l1165)))


(def v330_l1177 (kind/doc #'pj/->pose))


(def v332_l1184 (pj/pose? (pj/->pose tiny)))


(deftest t333_l1186 (is (true? v332_l1184)))


(def v334_l1188 (kind/doc #'pj/infer-mapping))


(def
 v336_l1194
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t337_l1199
 (is ((fn [m] (= {:x :height, :y :weight} m)) v336_l1194)))


(def
 v339_l1206
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t340_l1210 (is ((fn [v] (= [nil nil] v)) v339_l1206)))


(def
 v342_l1215
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t343_l1220
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v342_l1215)))


(def v344_l1222 (kind/doc #'pj/pose->draft))


(def
 v346_l1228
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t347_l1231 (is (true? v346_l1228)))


(def v348_l1233 (kind/doc #'pj/plan->membrane))


(def v349_l1235 (def m1 (pj/plan->membrane plan1)))


(def v350_l1237 (pj/membrane? m1))


(deftest t351_l1239 (is (true? v350_l1237)))


(def v352_l1241 (kind/doc #'pj/valid-membrane?))


(def v353_l1243 (pj/valid-membrane? m1))


(deftest t354_l1245 (is (true? v353_l1243)))


(def v355_l1247 (kind/doc #'pj/explain-membrane))


(def v356_l1249 (pj/explain-membrane m1))


(deftest t357_l1251 (is (nil? v356_l1249)))


(def v358_l1253 (kind/doc #'pj/membrane->plot))


(def v359_l1255 (first (pj/membrane->plot m1 :svg {})))


(deftest t360_l1257 (is ((fn [v] (= :svg v)) v359_l1255)))


(def v361_l1259 (kind/doc #'pj/plan->plot))


(def v362_l1261 (first (pj/plan->plot plan1 :svg {})))


(deftest t363_l1263 (is ((fn [v] (= :svg v)) v362_l1261)))


(def v365_l1270 (kind/doc #'pj/draft->plan))


(def v366_l1272 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v367_l1274 (pj/plan? (pj/draft->plan draft1)))


(deftest t368_l1276 (is (true? v367_l1274)))


(def v369_l1278 (kind/doc #'pj/draft->membrane))


(def v370_l1280 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t371_l1282 (is (true? v370_l1280)))


(def v372_l1284 (kind/doc #'pj/draft->plot))


(def v373_l1286 (first (pj/draft->plot draft1 :svg {})))


(deftest t374_l1288 (is ((fn [v] (= :svg v)) v373_l1286)))


(def v376_l1292 (kind/doc #'pj/config))


(def v377_l1294 (pj/config))


(deftest t378_l1296 (is ((fn [m] (map? m)) v377_l1294)))


(def v379_l1298 (kind/doc #'pj/set-config!))


(def v380_l1300 (kind/doc #'pj/with-config))


(def
 v381_l1302
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t382_l1305 (is ((fn [p] (= :pastel1 p)) v381_l1302)))


(def v384_l1312 (kind/doc #'pj/config-key-docs))


(def v385_l1314 (count pj/config-key-docs))


(deftest t386_l1316 (is ((fn [n] (= 46 n)) v385_l1314)))


(def v387_l1318 (kind/doc #'pj/plot-option-docs))


(def v388_l1320 (count pj/plot-option-docs))


(deftest t389_l1322 (is ((fn [n] (= 16 n)) v388_l1320)))


(def v390_l1324 (kind/doc #'pj/layer-mapping-docs))


(def v391_l1326 (count pj/layer-mapping-docs))


(deftest t392_l1328 (is ((fn [n] (= 57 n)) v391_l1326)))


(def v394_l1334 (kind/doc #'pj/panel-aesthetic-docs))


(def v395_l1336 (keys pj/panel-aesthetic-docs))


(deftest
 t396_l1338
 (is ((fn [ks] (= #{:col :row} (set ks))) v395_l1336)))


(def v398_l1342 (kind/doc #'pj/aesthetic-roles))


(def
 v399_l1344
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t400_l1346
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v399_l1344)))


(def v402_l1354 (kind/doc #'pj/aesthetic-scales))


(def v403_l1356 (first pj/aesthetic-scales))


(deftest
 t404_l1358
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v403_l1356)))


(def v406_l1364 (kind/doc #'pj/compound-key-aesthetics))


(def v407_l1366 (pj/compound-key-aesthetics))


(deftest
 t408_l1368
 (is ((fn [s] (= #{:group :col :row} s)) v407_l1366)))


(def v410_l1373 (kind/doc #'pj/shape-palette))


(def v411_l1375 (pj/shape-palette))


(deftest
 t412_l1377
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v411_l1375)))


(def v414_l1382 (kind/doc #'pj/layer-type-lookup))


(def v415_l1384 (pj/layer-type-lookup :smooth))


(deftest
 t416_l1386
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v415_l1384)))


(def v417_l1389 (kind/doc #'pj/registered-layer-types))


(def v418_l1391 (count (pj/registered-layer-types)))


(deftest t419_l1393 (is ((fn [n] (= 26 n)) v418_l1391)))


(def v420_l1395 (first (pj/registered-layer-types)))


(deftest
 t421_l1397
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v420_l1395)))


(def v423_l1405 (kind/doc #'pj/stat-doc))


(def v424_l1407 (pj/stat-doc :linear-model))


(deftest t425_l1409 (is ((fn [s] (string? s)) v424_l1407)))


(def v426_l1411 (kind/doc #'pj/mark-doc))


(def v427_l1413 (pj/mark-doc :point))


(deftest t428_l1415 (is ((fn [s] (string? s)) v427_l1413)))


(def v429_l1417 (kind/doc #'pj/position-doc))


(def v430_l1419 (pj/position-doc :dodge))


(deftest t431_l1421 (is ((fn [s] (string? s)) v430_l1419)))


(def v432_l1423 (kind/doc #'pj/scale-doc))


(def v433_l1425 (pj/scale-doc :linear))


(deftest t434_l1427 (is ((fn [s] (string? s)) v433_l1425)))


(def v435_l1429 (kind/doc #'pj/coord-doc))


(def v436_l1431 (pj/coord-doc :cartesian))


(deftest t437_l1433 (is ((fn [s] (string? s)) v436_l1431)))


(def v438_l1435 (kind/doc #'pj/membrane-mark-doc))


(def v439_l1437 (pj/membrane-mark-doc :point))


(deftest t440_l1439 (is ((fn [s] (string? s)) v439_l1437)))


(def v442_l1443 (kind/doc #'pj/save))


(def
 v444_l1447
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t445_l1453 (is (true? v444_l1447)))


(def
 v447_l1459
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
 t448_l1468
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v447_l1459)))


(def
 v450_l1473
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


(deftest t451_l1482 (is ((fn [bs] (= [137 80 78 71] bs)) v450_l1473)))


(def v453_l1495 (kind/doc #'pj/overlay))


(def
 v455_l1500
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t456_l1507 (is ((fn [n] (= 1 n)) v455_l1500)))


(def v457_l1509 (kind/doc #'pj/facet-grid))


(def
 v459_l1514
 (let
  [tips-pose (pj/lay-point (rdatasets/reshape2-tips) :total-bill :tip)]
  (=
   (pj/facet-grid tips-pose :smoker :sex)
   (pj/facet tips-pose {:col :smoker, :row :sex}))))


(deftest t460_l1518 (is (true? v459_l1514)))


(def v461_l1520 (kind/doc #'pj/cross))


(def
 v463_l1526
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
 t464_l1533
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
   v463_l1526)))


(def
 v466_l1543
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix
   [:sepal-length :petal-length]
   [:sepal-width :petal-width])))


(deftest
 t467_l1548
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v466_l1543)))


(def v468_l1552 (kind/doc #'pj/layer-option-docs))


(def v470_l1556 (identical? pj/layer-option-docs pj/layer-mapping-docs))


(deftest t471_l1558 (is (true? v470_l1556)))
