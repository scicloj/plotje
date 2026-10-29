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


(def v26_l111 (pj/cross [:a :b] [1 2 3]))


(deftest
 t27_l113
 (is
  ((fn [v] (= [[:a 1] [:a 2] [:a 3] [:b 1] [:b 2] [:b 3]] v))
   v26_l111)))


(def
 v29_l120
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :petal-length]},
    :row {:y [:sepal-length :petal-length]}})))


(deftest
 t30_l125
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 300 (:points s)))))
   v29_l120)))


(def v32_l131 (kind/doc #'pj/lay))


(def
 v34_l139
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t35_l143
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v34_l139)))


(def v37_l154 (kind/doc #'pj/lay-point))


(def
 v38_l156
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l159
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v38_l156)))


(def v40_l162 (kind/doc #'pj/lay-line))


(def
 v41_l164
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__11193#] (Math/sin (* p1__11193# 0.3))) (range 30))}))


(def v42_l167 (-> wave (pj/lay-line :x :y)))


(deftest
 t43_l170
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v42_l167)))


(def v44_l173 (kind/doc #'pj/lay-histogram))


(def
 v45_l175
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t46_l178
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v45_l175)))


(def
 v48_l184
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  {:x [:sepal-length :sepal-width]}))


(deftest
 t49_l186
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v48_l184)))


(def v50_l190 (kind/doc #'pj/lay-bar))


(def v51_l192 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t52_l195
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v51_l192)))


(def
 v54_l200
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t55_l203
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v54_l200)))


(def
 v57_l208
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t58_l211
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l208)))


(def v60_l217 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t61_l220
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v60_l217)))


(def v63_l226 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t64_l229
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v63_l226)))


(def
 v66_l236
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t67_l239
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v66_l236)))


(def v69_l246 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t70_l249
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v69_l246)))


(def
 v72_l261
 (-> sales (pj/lay-bar :product :revenue {:bar-width 0.4})))


(deftest
 t73_l264
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v72_l261)))


(def
 v75_l271
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t76_l274
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v75_l271)))


(def
 v78_l281
 (->
  {:x [1 2 3 4 5], :y [10 20 15 30 25]}
  (pj/lay-bar :x :y {:bar-width 0.3})))


(deftest
 t79_l284
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v78_l281)))


(def
 v81_l289
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t82_l293
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v81_l289)))


(def v83_l296 (kind/doc #'pj/lay-smooth))


(def
 v85_l300
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t86_l304
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v85_l300)))


(def
 v87_l308
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
 t88_l317
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v87_l308)))


(def v89_l321 (kind/doc #'pj/lay-density))


(def
 v90_l323
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t91_l326
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v90_l323)))


(def v92_l329 (kind/doc #'pj/lay-area))


(def v93_l331 (-> wave (pj/lay-area :x :y)))


(deftest
 t94_l334
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v93_l331)))


(def
 v96_l339
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
 t97_l346
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v96_l339)))


(def v98_l349 (kind/doc #'pj/lay-text))


(def
 v99_l351
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t100_l354
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v99_l351)))


(def v101_l357 (kind/doc #'pj/lay-label))


(def
 v102_l359
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t103_l363
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v102_l359)))


(def v104_l367 (kind/doc #'pj/lay-boxplot))


(def
 v105_l369
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t106_l372
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v105_l369)))


(def v107_l376 (kind/doc #'pj/lay-violin))


(def
 v108_l378
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t109_l381
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v108_l378)))


(def v110_l384 (kind/doc #'pj/lay-errorbar))


(def
 v111_l386
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t112_l390
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v111_l386)))


(def v113_l394 (kind/doc #'pj/lay-lollipop))


(def v114_l396 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t115_l399
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v114_l396)))


(def v116_l403 (kind/doc #'pj/lay-tile))


(def
 v117_l405
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t118_l408
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v117_l405)))


(def v119_l411 (kind/doc #'pj/lay-density-2d))


(def
 v120_l413
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t121_l416
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v120_l413)))


(def v122_l419 (kind/doc #'pj/lay-contour))


(def
 v123_l421
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t124_l424
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v123_l421)))


(def v125_l427 (kind/doc #'pj/lay-ridgeline))


(def
 v126_l429
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t127_l432
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v126_l429)))


(def v128_l435 (kind/doc #'pj/lay-rug))


(def
 v129_l437
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t130_l441
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v129_l437)))


(def v131_l444 (kind/doc #'pj/lay-step))


(def v132_l446 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t133_l450
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v132_l446)))


(def v134_l454 (kind/doc #'pj/lay-summary))


(def
 v135_l456
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t136_l459
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v135_l456)))


(def v137_l463 (kind/doc #'pj/lay-interval-h))


(def
 v138_l465
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
 t139_l470
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v138_l465)))


(def v140_l473 (kind/doc #'pj/lay-segment))


(def
 v141_l475
 (->
  {:x0 [1 2 4], :y0 [1 3 2], :x1 [3 3 5], :y1 [2 1 4]}
  (pj/lay-segment :x0 :y0 {:x-end :x1, :y-end :y1, :arrow :end})))


(deftest
 t142_l478
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:lines s)) (= 3 (:polygons s)))))
   v141_l475)))


(def
 v144_l541
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t145_l547
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v144_l541)))


(def
 v147_l555
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t148_l561
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v147_l555)))


(def v149_l565 (kind/doc #'pj/lay-rule-v))


(def
 v150_l567
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t151_l571
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v150_l567)))


(def
 v153_l578
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
 t154_l584
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v153_l578)))


(def v155_l588 (kind/doc #'pj/lay-rule-h))


(def
 v156_l590
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t157_l594
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v156_l590)))


(def v158_l598 (kind/doc #'pj/lay-band-v))


(def
 v159_l600
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t160_l604
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v159_l600)))


(def v161_l607 (kind/doc #'pj/lay-band-h))


(def
 v162_l609
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t163_l613
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v162_l609)))


(def v165_l618 (kind/doc #'pj/coord))


(def
 v167_l622
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t168_l625
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v167_l622)))


(def
 v170_l630
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t171_l633
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v170_l630)))


(def v172_l636 (kind/doc #'pj/scale))


(def
 v174_l640
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t175_l643
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v174_l640)))


(def
 v177_l648
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t178_l651
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v177_l648)))


(def
 v180_l657
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t181_l661
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v180_l657)))


(def
 v183_l667
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t184_l671
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
   v183_l667)))


(def
 v186_l678
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t187_l683
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v186_l678)))


(def v188_l689 (kind/doc #'pj/shape-symbols))


(def v189_l691 (pj/shape-symbols))


(deftest
 t190_l693
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v189_l691)))


(def
 v192_l698
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t193_l704
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v192_l698)))


(def v195_l710 (kind/doc #'pj/facet))


(def
 v196_l712
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t197_l716
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v196_l712)))


(def
 v199_l723
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t200_l727
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v199_l723)))


(def v202_l733 (kind/doc #'pj/arrange))


(def
 v203_l735
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 2, :height 250}))


(deftest
 t204_l741
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (= 2 (:panels (pj/svg-summary v)))
     (= [0.0 0.0 600.0 250.0] (:canvas (pj/frames v)))))
   v203_l735)))


(def
 v206_l748
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point {:color :species})))


(deftest
 t207_l753
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v206_l748)))


(def v208_l757 (kind/doc #'pj/marginal))


(def
 v209_l759
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t210_l763
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v209_l759)))


(def
 v212_l770
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t213_l774
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v212_l770)))


(def
 v215_l782
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t216_l786
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
   v215_l782)))


(def v218_l798 (kind/doc #'pj/plot))


(def v220_l803 (-> tiny (pj/lay-point :x :y)))


(deftest
 t221_l806
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v220_l803)))


(def
 v223_l813
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t224_l816
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v223_l813)))


(def v225_l820 (kind/doc #'pj/options))


(def
 v227_l824
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t228_l828
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v227_l824)))


(def v230_l835 (kind/doc #'pj/pose?))


(def v232_l839 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t233_l841 (is (true? v232_l839)))


(def v234_l843 (kind/doc #'pj/plan?))


(def v236_l847 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t237_l849 (is (true? v236_l847)))


(def v238_l851 (kind/doc #'pj/leaf-plan?))


(def v240_l856 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t241_l858 (is (true? v240_l856)))


(def v242_l860 (kind/doc #'pj/composite-plan?))


(def
 v244_l865
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t245_l869 (is (true? v244_l865)))


(def v246_l871 (kind/doc #'pj/draft?))


(def v248_l876 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t249_l878 (is (true? v248_l876)))


(def v250_l880 (kind/doc #'pj/leaf-draft?))


(def v252_l885 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t253_l887 (is (true? v252_l885)))


(def v254_l889 (kind/doc #'pj/composite-draft?))


(def
 v256_l894
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t257_l898 (is (true? v256_l894)))


(def v258_l900 (kind/doc #'pj/plan-layer?))


(def
 v260_l904
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t261_l906 (is (true? v260_l904)))


(def v262_l908 (kind/doc #'pj/layer-type?))


(def v264_l912 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t265_l914 (is (true? v264_l912)))


(def v266_l916 (kind/doc #'pj/membrane?))


(def v268_l921 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t269_l923 (is (true? v268_l921)))


(def v270_l925 (kind/doc #'pj/series-mapping?))


(def
 v272_l930
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t273_l933 (is ((fn [v] (= [true false] v)) v272_l930)))


(def v274_l935 (kind/doc #'pj/series-mapping))


(def
 v276_l940
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t277_l942
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v276_l940)))


(def v279_l946 (kind/doc #'pj/draft))


(def
 v281_l953
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t282_l959
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v281_l953)))


(def v283_l963 (kind/doc #'pj/plan))


(def v285_l967 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v286_l971 plan1)


(deftest
 t287_l973
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v286_l971)))


(def v288_l976 (kind/doc #'pj/frames))


(def v290_l982 (-> plan1 pj/frames kind/pprint))


(deftest
 t291_l984
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v290_l982)))


(def
 v293_l997
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
 t294_l1009
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
   v293_l997)))


(def v296_l1023 (kind/doc #'pj/to-drawing))


(def v297_l1025 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t298_l1027 (is ((fn [v] (= 2 (count v))) v297_l1025)))


(def v299_l1029 (kind/doc #'pj/to-data))


(def
 v301_l1033
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t302_l1036
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v301_l1033)))


(def
 v304_l1041
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11199#] (Math/round (double p1__11199#)))))))


(deftest t305_l1046 (is ((fn [v] (= [2 5] v)) v304_l1041)))


(def
 v307_l1053
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
 t308_l1062
 (is ((fn [m] (= "versicolor" (:back m))) v307_l1053)))


(def v309_l1064 (kind/doc #'pj/svg-summary))


(def
 v310_l1066
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t311_l1069
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v310_l1066)))


(def v312_l1072 (kind/doc #'pj/valid-pose?))


(def v313_l1074 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t314_l1076 (is (true? v313_l1074)))


(def v315_l1078 (kind/doc #'pj/explain-pose))


(def v316_l1080 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t317_l1082 (is (nil? v316_l1080)))


(def v318_l1084 (kind/doc #'pj/valid-plan?))


(def v319_l1086 (pj/valid-plan? plan1))


(deftest t320_l1088 (is (true? v319_l1086)))


(def v321_l1090 (kind/doc #'pj/explain-plan))


(def v322_l1092 (pj/explain-plan plan1))


(deftest t323_l1094 (is (nil? v322_l1092)))


(def v325_l1111 (kind/doc #'pj/membrane))


(def
 v327_l1121
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t328_l1127
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v327_l1121)))


(def v329_l1133 (kind/doc #'pj/->pose))


(def v331_l1140 (pj/pose? (pj/->pose tiny)))


(deftest t332_l1142 (is (true? v331_l1140)))


(def v333_l1144 (kind/doc #'pj/infer-mapping))


(def
 v335_l1150
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t336_l1155
 (is ((fn [m] (= {:x :height, :y :weight} m)) v335_l1150)))


(def
 v338_l1162
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t339_l1166 (is ((fn [v] (= [nil nil] v)) v338_l1162)))


(def
 v341_l1171
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t342_l1176
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v341_l1171)))


(def v343_l1178 (kind/doc #'pj/pose->draft))


(def
 v345_l1184
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t346_l1187 (is (true? v345_l1184)))


(def v347_l1189 (kind/doc #'pj/plan->membrane))


(def v348_l1191 (def m1 (pj/plan->membrane plan1)))


(def v349_l1193 (pj/membrane? m1))


(deftest t350_l1195 (is (true? v349_l1193)))


(def v351_l1197 (kind/doc #'pj/valid-membrane?))


(def v352_l1199 (pj/valid-membrane? m1))


(deftest t353_l1201 (is (true? v352_l1199)))


(def v354_l1203 (kind/doc #'pj/explain-membrane))


(def v355_l1205 (pj/explain-membrane m1))


(deftest t356_l1207 (is (nil? v355_l1205)))


(def v357_l1209 (kind/doc #'pj/membrane->plot))


(def v358_l1211 (first (pj/membrane->plot m1 :svg {})))


(deftest t359_l1213 (is ((fn [v] (= :svg v)) v358_l1211)))


(def v360_l1215 (kind/doc #'pj/plan->plot))


(def v361_l1217 (first (pj/plan->plot plan1 :svg {})))


(deftest t362_l1219 (is ((fn [v] (= :svg v)) v361_l1217)))


(def v364_l1226 (kind/doc #'pj/draft->plan))


(def v365_l1228 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v366_l1230 (pj/plan? (pj/draft->plan draft1)))


(deftest t367_l1232 (is (true? v366_l1230)))


(def v368_l1234 (kind/doc #'pj/draft->membrane))


(def v369_l1236 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t370_l1238 (is (true? v369_l1236)))


(def v371_l1240 (kind/doc #'pj/draft->plot))


(def v372_l1242 (first (pj/draft->plot draft1 :svg {})))


(deftest t373_l1244 (is ((fn [v] (= :svg v)) v372_l1242)))


(def v375_l1248 (kind/doc #'pj/config))


(def v376_l1250 (pj/config))


(deftest t377_l1252 (is ((fn [m] (map? m)) v376_l1250)))


(def v378_l1254 (kind/doc #'pj/set-config!))


(def v379_l1256 (kind/doc #'pj/with-config))


(def
 v380_l1258
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t381_l1261 (is ((fn [p] (= :pastel1 p)) v380_l1258)))


(def v383_l1267 (kind/doc #'pj/config-key-docs))


(def v384_l1269 (count pj/config-key-docs))


(deftest t385_l1271 (is ((fn [n] (= 48 n)) v384_l1269)))


(def v386_l1273 (kind/doc #'pj/plot-option-docs))


(def v387_l1275 (count pj/plot-option-docs))


(deftest t388_l1277 (is ((fn [n] (= 14 n)) v387_l1275)))


(def v389_l1279 (kind/doc #'pj/layer-mapping-docs))


(def v390_l1281 (count pj/layer-mapping-docs))


(deftest t391_l1283 (is ((fn [n] (= 57 n)) v390_l1281)))


(def v393_l1289 (kind/doc #'pj/panel-aesthetic-docs))


(def v394_l1291 (keys pj/panel-aesthetic-docs))


(deftest
 t395_l1293
 (is ((fn [ks] (= #{:col :row} (set ks))) v394_l1291)))


(def v397_l1297 (kind/doc #'pj/aesthetic-roles))


(def
 v398_l1299
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t399_l1301
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v398_l1299)))


(def v401_l1309 (kind/doc #'pj/aesthetic-scales))


(def v402_l1311 (first pj/aesthetic-scales))


(deftest
 t403_l1313
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v402_l1311)))


(def v405_l1319 (kind/doc #'pj/compound-key-aesthetics))


(def v406_l1321 (pj/compound-key-aesthetics))


(deftest
 t407_l1323
 (is ((fn [s] (= #{:group :col :row} s)) v406_l1321)))


(def v409_l1328 (kind/doc #'pj/shape-palette))


(def v410_l1330 (pj/shape-palette))


(deftest
 t411_l1332
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v410_l1330)))


(def v413_l1337 (kind/doc #'pj/layer-type-lookup))


(def v414_l1339 (pj/layer-type-lookup :smooth))


(deftest
 t415_l1341
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v414_l1339)))


(def v416_l1344 (kind/doc #'pj/registered-layer-types))


(def v417_l1346 (count (pj/registered-layer-types)))


(deftest t418_l1348 (is ((fn [n] (= 26 n)) v417_l1346)))


(def v419_l1350 (first (pj/registered-layer-types)))


(deftest
 t420_l1352
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v419_l1350)))


(def v422_l1360 (kind/doc #'pj/stat-doc))


(def v423_l1362 (pj/stat-doc :linear-model))


(deftest t424_l1364 (is ((fn [s] (string? s)) v423_l1362)))


(def v425_l1366 (kind/doc #'pj/mark-doc))


(def v426_l1368 (pj/mark-doc :point))


(deftest t427_l1370 (is ((fn [s] (string? s)) v426_l1368)))


(def v428_l1372 (kind/doc #'pj/position-doc))


(def v429_l1374 (pj/position-doc :dodge))


(deftest t430_l1376 (is ((fn [s] (string? s)) v429_l1374)))


(def v431_l1378 (kind/doc #'pj/scale-doc))


(def v432_l1380 (pj/scale-doc :linear))


(deftest t433_l1382 (is ((fn [s] (string? s)) v432_l1380)))


(def v434_l1384 (kind/doc #'pj/coord-doc))


(def v435_l1386 (pj/coord-doc :cartesian))


(deftest t436_l1388 (is ((fn [s] (string? s)) v435_l1386)))


(def v437_l1390 (kind/doc #'pj/membrane-mark-doc))


(def v438_l1392 (pj/membrane-mark-doc :point))


(deftest t439_l1394 (is ((fn [s] (string? s)) v438_l1392)))


(def v441_l1398 (kind/doc #'pj/save))


(def
 v443_l1402
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t444_l1408 (is (true? v443_l1402)))


(def
 v446_l1413
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
 t447_l1422
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v446_l1413)))


(def
 v449_l1427
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


(deftest t450_l1436 (is ((fn [bs] (= [137 80 78 71] bs)) v449_l1427)))


(def v452_l1446 (kind/doc #'pj/overlay))


(def
 v454_l1451
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t455_l1458 (is ((fn [n] (= 1 n)) v454_l1451)))


(def v456_l1460 (kind/doc #'pj/facet-grid))


(def
 v458_l1465
 (let
  [tips-pose (pj/lay-point (rdatasets/reshape2-tips) :total-bill :tip)]
  (=
   (pj/facet-grid tips-pose :smoker :sex)
   (pj/facet tips-pose {:col :smoker, :row :sex}))))


(deftest t459_l1469 (is (true? v458_l1465)))
