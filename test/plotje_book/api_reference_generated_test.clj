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
   (map (fn* [p1__87818#] (Math/sin (* p1__87818# 0.3))) (range 30))}))


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
      [p1__87819#]
      (+
       (Math/sin (* p1__87819# 0.2))
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
 v138_l521
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t139_l527
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v138_l521)))


(def
 v141_l535
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t142_l541
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v141_l535)))


(def v143_l545 (kind/doc #'pj/lay-rule-v))


(def
 v144_l547
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t145_l551
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v144_l547)))


(def
 v147_l558
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
 t148_l564
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v147_l558)))


(def v149_l568 (kind/doc #'pj/lay-rule-h))


(def
 v150_l570
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t151_l574
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v150_l570)))


(def v152_l578 (kind/doc #'pj/lay-band-v))


(def
 v153_l580
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t154_l584
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v153_l580)))


(def v155_l587 (kind/doc #'pj/lay-band-h))


(def
 v156_l589
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t157_l593
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v156_l589)))


(def v159_l598 (kind/doc #'pj/coord))


(def
 v161_l602
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t162_l606
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v161_l602)))


(def
 v164_l611
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t165_l615
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v164_l611)))


(def v166_l618 (kind/doc #'pj/scale))


(def
 v168_l622
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t169_l626
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v168_l622)))


(def
 v171_l631
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t172_l635
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v171_l631)))


(def
 v174_l641
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t175_l645
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v174_l641)))


(def
 v177_l651
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t178_l655
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
   v177_l651)))


(def
 v180_l662
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t181_l667
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v180_l662)))


(def v182_l673 (kind/doc #'pj/shape-symbols))


(def v183_l675 (pj/shape-symbols))


(deftest
 t184_l677
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v183_l675)))


(def
 v186_l682
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t187_l688
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v186_l682)))


(def v189_l694 (kind/doc #'pj/facet))


(def
 v190_l696
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t191_l700
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v190_l696)))


(def
 v193_l707
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t194_l711
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v193_l707)))


(def v196_l717 (kind/doc #'pj/matrix))


(def
 v197_l719
 (pj/matrix
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]))


(deftest
 t198_l721
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
   v197_l719)))


(def v200_l728 (kind/doc #'pj/arrange))


(def
 v201_l730
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 2, :height 250}))


(deftest
 t202_l736
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (= 2 (:panels (pj/svg-summary v)))
     (= [0.0 0.0 600.0 250.0] (:canvas (pj/frames v)))))
   v201_l730)))


(def
 v204_l744
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point {:color :species})))


(deftest
 t205_l749
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v204_l744)))


(def v206_l753 (kind/doc #'pj/marginal))


(def
 v207_l755
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t208_l759
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v207_l755)))


(def
 v210_l766
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t211_l770
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v210_l766)))


(def
 v213_l778
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t214_l782
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (mapv
       (fn* [p1__87820#] (-> p1__87820# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v213_l778)))


(def v216_l794 (kind/doc #'pj/plot))


(def v218_l799 (-> tiny (pj/lay-point :x :y)))


(deftest
 t219_l802
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v218_l799)))


(def
 v221_l809
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t222_l812
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v221_l809)))


(def v223_l816 (kind/doc #'pj/options))


(def
 v225_l820
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t226_l824
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v225_l820)))


(def v228_l831 (kind/doc #'pj/pose?))


(def v230_l835 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t231_l837 (is (true? v230_l835)))


(def v232_l839 (kind/doc #'pj/plan?))


(def v234_l843 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t235_l845 (is (true? v234_l843)))


(def v236_l847 (kind/doc #'pj/leaf-plan?))


(def v238_l852 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t239_l854 (is (true? v238_l852)))


(def v240_l856 (kind/doc #'pj/composite-plan?))


(def
 v242_l861
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t243_l865 (is (true? v242_l861)))


(def v244_l867 (kind/doc #'pj/draft?))


(def v246_l872 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t247_l874 (is (true? v246_l872)))


(def v248_l876 (kind/doc #'pj/leaf-draft?))


(def v250_l881 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t251_l883 (is (true? v250_l881)))


(def v252_l885 (kind/doc #'pj/composite-draft?))


(def
 v254_l890
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t255_l894 (is (true? v254_l890)))


(def v256_l896 (kind/doc #'pj/plan-layer?))


(def
 v258_l900
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t259_l902 (is (true? v258_l900)))


(def v260_l904 (kind/doc #'pj/layer-type?))


(def v262_l908 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t263_l910 (is (true? v262_l908)))


(def v264_l912 (kind/doc #'pj/membrane?))


(def v266_l917 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t267_l919 (is (true? v266_l917)))


(def v268_l921 (kind/doc #'pj/series-mapping?))


(def
 v270_l926
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t271_l929 (is ((fn [v] (= [true false] v)) v270_l926)))


(def v272_l931 (kind/doc #'pj/series-mapping))


(def
 v274_l936
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t275_l938
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v274_l936)))


(def v277_l942 (kind/doc #'pj/draft))


(def
 v279_l949
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t280_l955
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v279_l949)))


(def v281_l959 (kind/doc #'pj/plan))


(def v283_l963 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v284_l967 plan1)


(deftest
 t285_l969
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v284_l967)))


(def v286_l972 (kind/doc #'pj/frames))


(def v288_l978 (-> plan1 pj/frames kind/pprint))


(deftest
 t289_l980
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v288_l978)))


(def
 v291_l993
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
    (fn* [p1__87821#] (-> p1__87821# :frames :panel-box))
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
    (fn* [p1__87822#] (vec (keys (:frames p1__87822#))))
    (:panels f))}))


(deftest
 t292_l1005
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 700.0 300.0] (:canvas m))
     (= 2 (count (:panel-boxes m)))
     (apply not= (map first (:panel-boxes m)))
     (true? (:every-box-inside-the-canvas m))
     (every?
      (fn* [p1__87823#] (= [:panel-box :drawing-area] p1__87823#))
      (:panel-rectangle-keys m))))
   v291_l993)))


(def v294_l1019 (kind/doc #'pj/to-drawing))


(def v295_l1021 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t296_l1023 (is ((fn [v] (= 2 (count v))) v295_l1021)))


(def v297_l1025 (kind/doc #'pj/to-data))


(def
 v299_l1029
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t300_l1032
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v299_l1029)))


(def
 v302_l1037
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__87824#] (Math/round (double p1__87824#)))))))


(deftest t303_l1042 (is ((fn [v] (= [2 5] v)) v302_l1037)))


(def
 v305_l1049
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
 t306_l1058
 (is ((fn [m] (= "versicolor" (:back m))) v305_l1049)))


(def v307_l1060 (kind/doc #'pj/svg-summary))


(def
 v308_l1062
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t309_l1066
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v308_l1062)))


(def v310_l1069 (kind/doc #'pj/valid-pose?))


(def v311_l1071 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t312_l1073 (is (true? v311_l1071)))


(def v313_l1075 (kind/doc #'pj/explain-pose))


(def v314_l1077 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t315_l1079 (is (nil? v314_l1077)))


(def v316_l1081 (kind/doc #'pj/valid-plan?))


(def v317_l1083 (pj/valid-plan? plan1))


(deftest t318_l1085 (is (true? v317_l1083)))


(def v319_l1087 (kind/doc #'pj/explain-plan))


(def v320_l1089 (pj/explain-plan plan1))


(deftest t321_l1091 (is (nil? v320_l1089)))


(def v323_l1108 (kind/doc #'pj/membrane))


(def
 v325_l1118
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t326_l1124
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v325_l1118)))


(def v327_l1130 (kind/doc #'pj/->pose))


(def v329_l1137 (pj/pose? (pj/->pose tiny)))


(deftest t330_l1139 (is (true? v329_l1137)))


(def v331_l1141 (kind/doc #'pj/infer-mapping))


(def
 v333_l1147
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t334_l1152
 (is ((fn [m] (= {:x :height, :y :weight} m)) v333_l1147)))


(def
 v336_l1159
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t337_l1163 (is ((fn [v] (= [nil nil] v)) v336_l1159)))


(def
 v339_l1168
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t340_l1173
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v339_l1168)))


(def v341_l1175 (kind/doc #'pj/pose->draft))


(def
 v343_l1181
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t344_l1184 (is (true? v343_l1181)))


(def v345_l1186 (kind/doc #'pj/plan->membrane))


(def v346_l1188 (def m1 (pj/plan->membrane plan1)))


(def v347_l1190 (pj/membrane? m1))


(deftest t348_l1192 (is (true? v347_l1190)))


(def v349_l1194 (kind/doc #'pj/valid-membrane?))


(def v350_l1196 (pj/valid-membrane? m1))


(deftest t351_l1198 (is (true? v350_l1196)))


(def v352_l1200 (kind/doc #'pj/explain-membrane))


(def v353_l1202 (pj/explain-membrane m1))


(deftest t354_l1204 (is (nil? v353_l1202)))


(def v355_l1206 (kind/doc #'pj/membrane->plot))


(def v356_l1208 (first (pj/membrane->plot m1 :svg {})))


(deftest t357_l1210 (is ((fn [v] (= :svg v)) v356_l1208)))


(def v358_l1212 (kind/doc #'pj/plan->plot))


(def v359_l1214 (first (pj/plan->plot plan1 :svg {})))


(deftest t360_l1216 (is ((fn [v] (= :svg v)) v359_l1214)))


(def v362_l1223 (kind/doc #'pj/draft->plan))


(def v363_l1225 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v364_l1227 (pj/plan? (pj/draft->plan draft1)))


(deftest t365_l1229 (is (true? v364_l1227)))


(def v366_l1231 (kind/doc #'pj/draft->membrane))


(def v367_l1233 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t368_l1235 (is (true? v367_l1233)))


(def v369_l1237 (kind/doc #'pj/draft->plot))


(def v370_l1239 (first (pj/draft->plot draft1 :svg {})))


(deftest t371_l1241 (is ((fn [v] (= :svg v)) v370_l1239)))


(def v373_l1245 (kind/doc #'pj/config))


(def v374_l1247 (pj/config))


(deftest t375_l1249 (is ((fn [m] (map? m)) v374_l1247)))


(def v376_l1251 (kind/doc #'pj/set-config!))


(def v377_l1253 (kind/doc #'pj/with-config))


(def
 v378_l1255
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t379_l1258 (is ((fn [p] (= :pastel1 p)) v378_l1255)))


(def v381_l1265 (kind/doc #'pj/config-key-docs))


(def v382_l1267 (count pj/config-key-docs))


(deftest t383_l1269 (is ((fn [n] (= 48 n)) v382_l1267)))


(def v384_l1271 (kind/doc #'pj/plot-option-docs))


(def v385_l1273 (count pj/plot-option-docs))


(deftest t386_l1275 (is ((fn [n] (= 16 n)) v385_l1273)))


(def v387_l1277 (kind/doc #'pj/layer-mapping-docs))


(def v388_l1279 (count pj/layer-mapping-docs))


(deftest t389_l1281 (is ((fn [n] (= 57 n)) v388_l1279)))


(def v391_l1287 (kind/doc #'pj/panel-aesthetic-docs))


(def v392_l1289 (keys pj/panel-aesthetic-docs))


(deftest
 t393_l1291
 (is ((fn [ks] (= #{:col :row} (set ks))) v392_l1289)))


(def v395_l1295 (kind/doc #'pj/aesthetic-roles))


(def
 v396_l1297
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t397_l1299
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v396_l1297)))


(def v399_l1307 (kind/doc #'pj/aesthetic-scales))


(def v400_l1309 (first pj/aesthetic-scales))


(deftest
 t401_l1311
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v400_l1309)))


(def v403_l1317 (kind/doc #'pj/compound-key-aesthetics))


(def v404_l1319 (pj/compound-key-aesthetics))


(deftest
 t405_l1321
 (is ((fn [s] (= #{:group :col :row} s)) v404_l1319)))


(def v407_l1326 (kind/doc #'pj/shape-palette))


(def v408_l1328 (pj/shape-palette))


(deftest
 t409_l1330
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v408_l1328)))


(def v411_l1335 (kind/doc #'pj/layer-type-lookup))


(def v412_l1337 (pj/layer-type-lookup :smooth))


(deftest
 t413_l1339
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v412_l1337)))


(def v414_l1342 (kind/doc #'pj/registered-layer-types))


(def v415_l1344 (count (pj/registered-layer-types)))


(deftest t416_l1346 (is ((fn [n] (= 26 n)) v415_l1344)))


(def v417_l1348 (first (pj/registered-layer-types)))


(deftest
 t418_l1350
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v417_l1348)))


(def v420_l1358 (kind/doc #'pj/stat-doc))


(def v421_l1360 (pj/stat-doc :linear-model))


(deftest t422_l1362 (is ((fn [s] (string? s)) v421_l1360)))


(def v423_l1364 (kind/doc #'pj/mark-doc))


(def v424_l1366 (pj/mark-doc :point))


(deftest t425_l1368 (is ((fn [s] (string? s)) v424_l1366)))


(def v426_l1370 (kind/doc #'pj/position-doc))


(def v427_l1372 (pj/position-doc :dodge))


(deftest t428_l1374 (is ((fn [s] (string? s)) v427_l1372)))


(def v429_l1376 (kind/doc #'pj/scale-doc))


(def v430_l1378 (pj/scale-doc :linear))


(deftest t431_l1380 (is ((fn [s] (string? s)) v430_l1378)))


(def v432_l1382 (kind/doc #'pj/coord-doc))


(def v433_l1384 (pj/coord-doc :cartesian))


(deftest t434_l1386 (is ((fn [s] (string? s)) v433_l1384)))


(def v435_l1388 (kind/doc #'pj/membrane-mark-doc))


(def v436_l1390 (pj/membrane-mark-doc :point))


(deftest t437_l1392 (is ((fn [s] (string? s)) v436_l1390)))


(def v439_l1396 (kind/doc #'pj/save))


(def
 v441_l1400
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t442_l1406 (is (true? v441_l1400)))


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
    (mapv (fn* [p1__87825#] (bit-and p1__87825# 255)) (vec bs))))))


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
    (mapv (fn* [p1__87826#] (bit-and p1__87826# 255)) (vec bs))))))


(deftest t448_l1435 (is ((fn [bs] (= [137 80 78 71] bs)) v447_l1426)))


(def v450_l1448 (kind/doc #'pj/overlay))


(def
 v452_l1453
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t453_l1460 (is ((fn [n] (= 1 n)) v452_l1453)))


(def v454_l1462 (kind/doc #'pj/facet-grid))


(def
 v456_l1467
 (let
  [tips-pose (pj/lay-point (rdatasets/reshape2-tips) :total-bill :tip)]
  (=
   (pj/facet-grid tips-pose :smoker :sex)
   (pj/facet tips-pose {:col :smoker, :row :sex}))))


(deftest t457_l1471 (is (true? v456_l1467)))


(def v458_l1473 (kind/doc #'pj/cross))


(def
 v460_l1479
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
 t461_l1486
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
   v460_l1479)))


(def
 v463_l1496
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix
   [:sepal-length :petal-length]
   [:sepal-width :petal-width])))


(deftest
 t464_l1501
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v463_l1496)))


(def v465_l1505 (kind/doc #'pj/layer-option-docs))


(def v467_l1509 (identical? pj/layer-option-docs pj/layer-mapping-docs))


(deftest t468_l1511 (is (true? v467_l1509)))
