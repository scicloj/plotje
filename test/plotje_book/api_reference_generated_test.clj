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


(def v29_l119 (kind/doc #'pj/cross-matrix))


(def
 v31_l124
 (->
  (rdatasets/datasets-iris)
  (pj/cross-matrix [:sepal-length :petal-length] {:color :species})))


(deftest
 t32_l128
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 300 (:points s)))))
   v31_l124)))


(def v34_l134 (kind/doc #'pj/lay))


(def
 v36_l142
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t37_l146
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v36_l142)))


(def v39_l157 (kind/doc #'pj/lay-point))


(def
 v40_l159
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t41_l162
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v40_l159)))


(def v42_l165 (kind/doc #'pj/lay-line))


(def
 v43_l167
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__11193#] (Math/sin (* p1__11193# 0.3))) (range 30))}))


(def v44_l170 (-> wave (pj/lay-line :x :y)))


(deftest
 t45_l173
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v44_l170)))


(def v46_l176 (kind/doc #'pj/lay-histogram))


(def
 v47_l178
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t48_l181
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v47_l178)))


(def
 v50_l187
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  {:x [:sepal-length :sepal-width]}))


(deftest
 t51_l189
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v50_l187)))


(def v52_l193 (kind/doc #'pj/lay-bar))


(def v53_l195 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t54_l198
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v53_l195)))


(def
 v56_l203
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t57_l206
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v56_l203)))


(def
 v59_l211
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t60_l214
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v59_l211)))


(def v62_l220 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t63_l223
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v62_l220)))


(def v65_l229 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t66_l232
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v65_l229)))


(def
 v68_l239
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t69_l242
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v68_l239)))


(def v71_l249 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t72_l252
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v71_l249)))


(def
 v74_l264
 (-> sales (pj/lay-bar :product :revenue {:bar-width 0.4})))


(deftest
 t75_l267
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v74_l264)))


(def
 v77_l274
 (-> {:x [1 2 3 4 5], :y [10 20 15 30 25]} (pj/lay-bar :x :y)))


(deftest
 t78_l277
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v77_l274)))


(def
 v80_l284
 (->
  {:x [1 2 3 4 5], :y [10 20 15 30 25]}
  (pj/lay-bar :x :y {:bar-width 0.3})))


(deftest
 t81_l287
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v80_l284)))


(def
 v83_l292
 (->
  {:month
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"],
   :revenue [120 180 150]}
  (pj/lay-bar :month :revenue)))


(deftest
 t84_l296
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v83_l292)))


(def v85_l299 (kind/doc #'pj/lay-smooth))


(def
 v87_l303
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t88_l307
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v87_l303)))


(def
 v89_l311
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
 t90_l320
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v89_l311)))


(def v91_l324 (kind/doc #'pj/lay-density))


(def
 v92_l326
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t93_l329
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v92_l326)))


(def v94_l332 (kind/doc #'pj/lay-area))


(def v95_l334 (-> wave (pj/lay-area :x :y)))


(deftest
 t96_l337
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v95_l334)))


(def
 v98_l342
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
 t99_l349
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v98_l342)))


(def v100_l352 (kind/doc #'pj/lay-text))


(def
 v101_l354
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t102_l357
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v101_l354)))


(def v103_l360 (kind/doc #'pj/lay-label))


(def
 v104_l362
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t105_l366
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v104_l362)))


(def v106_l370 (kind/doc #'pj/lay-boxplot))


(def
 v107_l372
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t108_l375
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v107_l372)))


(def v109_l379 (kind/doc #'pj/lay-violin))


(def
 v110_l381
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t111_l384
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s))))
   v110_l381)))


(def v112_l387 (kind/doc #'pj/lay-errorbar))


(def
 v113_l389
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t114_l393
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v113_l389)))


(def v115_l397 (kind/doc #'pj/lay-lollipop))


(def v116_l399 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t117_l402
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v116_l399)))


(def v118_l406 (kind/doc #'pj/lay-tile))


(def
 v119_l408
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t120_l411
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v119_l408)))


(def v121_l414 (kind/doc #'pj/lay-density-2d))


(def
 v122_l416
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t123_l419
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v122_l416)))


(def v124_l422 (kind/doc #'pj/lay-contour))


(def
 v125_l424
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t126_l427
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v125_l424)))


(def v127_l430 (kind/doc #'pj/lay-ridgeline))


(def
 v128_l432
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t129_l435
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v128_l432)))


(def v130_l438 (kind/doc #'pj/lay-rug))


(def
 v131_l440
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t132_l444
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v131_l440)))


(def v133_l447 (kind/doc #'pj/lay-step))


(def v134_l449 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t135_l453
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v134_l449)))


(def v136_l457 (kind/doc #'pj/lay-summary))


(def
 v137_l459
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t138_l462
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v137_l459)))


(def v139_l466 (kind/doc #'pj/lay-interval-h))


(def
 v140_l468
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
 t141_l473
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v140_l468)))


(def v142_l476 (kind/doc #'pj/lay-segment))


(def
 v143_l478
 (->
  {:x0 [1 2 4], :y0 [1 3 2], :x1 [3 3 5], :y1 [2 1 4]}
  (pj/lay-segment :x0 :y0 {:x-end :x1, :y-end :y1, :arrow :end})))


(deftest
 t144_l481
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:lines s)) (= 3 (:polygons s)))))
   v143_l478)))


(def
 v146_l542
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min 0.5, :y-max 1.5})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t147_l548
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-errorbar :y-min 0.5 is not a column" msg)
     (re-find #"one bound per row" msg)
     (re-find #"pj/lay-band-h" msg)))
   v146_l542)))


(def
 v149_l556
 (try
  (->
   measurements
   (pj/lay-point :treatment :mean)
   (pj/lay-errorbar {:y-min :ci-lo})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t150_l562
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min and :y-max columns" msg)
     (re-find #"got :y-min alone" msg)))
   v149_l556)))


(def v151_l566 (kind/doc #'pj/lay-rule-v))


(def
 v152_l568
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t153_l572
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v152_l568)))


(def
 v155_l579
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
 t156_l585
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v155_l579)))


(def v157_l589 (kind/doc #'pj/lay-rule-h))


(def
 v158_l591
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t159_l595
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v158_l591)))


(def v160_l599 (kind/doc #'pj/lay-band-v))


(def
 v161_l601
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t162_l605
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v161_l601)))


(def v163_l608 (kind/doc #'pj/lay-band-h))


(def
 v164_l610
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t165_l614
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v164_l610)))


(def v167_l619 (kind/doc #'pj/coord))


(def
 v169_l623
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t170_l626
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v169_l623)))


(def
 v172_l631
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t173_l634
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v172_l631)))


(def v174_l637 (kind/doc #'pj/scale))


(def
 v176_l641
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t177_l644
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v176_l641)))


(def
 v179_l649
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t180_l652
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v179_l649)))


(def
 v182_l658
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t183_l662
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v182_l658)))


(def
 v185_l668
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size {:range [3 16], :by :area, :from-zero true})))


(deftest
 t186_l672
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
   v185_l668)))


(def
 v188_l679
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:domain ["virginica" "versicolor" "setosa"],
    :values [:cross :plus :diamond]})))


(deftest
 t189_l684
 (is
  ((fn
    [v]
    (=
     [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v188_l679)))


(def v190_l690 (kind/doc #'pj/shape-symbols))


(def v191_l692 (pj/shape-symbols))


(deftest
 t192_l694
 (is ((fn [syms] (and (seq syms) (every? keyword? syms))) v191_l692)))


(def
 v194_l699
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t195_l705
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v194_l699)))


(def v197_l711 (kind/doc #'pj/facet))


(def
 v198_l713
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t199_l717
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v198_l713)))


(def v200_l721 (kind/doc #'pj/facet-grid))


(def
 v201_l723
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t202_l727
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v201_l723)))


(def v204_l733 (kind/doc #'pj/arrange))


(def
 v205_l735
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 2, :height 250}))


(deftest
 t206_l741
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (= 2 (:panels (pj/svg-summary v)))
     (= [0.0 0.0 600.0 250.0] (:canvas (pj/frames v)))))
   v205_l735)))


(def
 v208_l748
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point {:color :species})))


(deftest
 t209_l753
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v208_l748)))


(def v210_l757 (kind/doc #'pj/marginal))


(def
 v211_l759
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t212_l763
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v211_l759)))


(def
 v214_l770
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.3})))


(deftest
 t215_l774
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)) (= 9 (:polygons s)))))
   v214_l770)))


(def
 v217_l782
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t218_l786
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
   v217_l782)))


(def v220_l798 (kind/doc #'pj/plot))


(def v222_l803 (-> tiny (pj/lay-point :x :y)))


(deftest
 t223_l806
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v222_l803)))


(def
 v225_l813
 (pj/plot {:height [150 160 170 175], :weight [50 60 72 78]}))


(deftest
 t226_l816
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:points s)))))
   v225_l813)))


(def v227_l820 (kind/doc #'pj/options))


(def
 v229_l824
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options
   {:width 400, :height 200, :title "Twice as wide as tall"})))


(deftest
 t230_l828
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (== 400 (:width s))
      (== 200 (:height s))
      (some #{"Twice as wide as tall"} (:texts s)))))
   v229_l824)))


(def v232_l835 (kind/doc #'pj/pose?))


(def v234_l839 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t235_l841 (is (true? v234_l839)))


(def v236_l843 (kind/doc #'pj/plan?))


(def v238_l847 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t239_l849 (is (true? v238_l847)))


(def v240_l851 (kind/doc #'pj/leaf-plan?))


(def v242_l856 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t243_l858 (is (true? v242_l856)))


(def v244_l860 (kind/doc #'pj/composite-plan?))


(def
 v246_l865
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t247_l869 (is (true? v246_l865)))


(def v248_l871 (kind/doc #'pj/draft?))


(def v250_l876 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t251_l878 (is (true? v250_l876)))


(def v252_l880 (kind/doc #'pj/leaf-draft?))


(def v254_l885 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t255_l887 (is (true? v254_l885)))


(def v256_l889 (kind/doc #'pj/composite-draft?))


(def
 v258_l894
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t259_l898 (is (true? v258_l894)))


(def v260_l900 (kind/doc #'pj/plan-layer?))


(def
 v262_l904
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t263_l906 (is (true? v262_l904)))


(def v264_l908 (kind/doc #'pj/layer-type?))


(def v266_l912 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t267_l914 (is (true? v266_l912)))


(def v268_l916 (kind/doc #'pj/membrane?))


(def v270_l921 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t271_l923 (is (true? v270_l921)))


(def v272_l925 (kind/doc #'pj/series-mapping?))


(def
 v274_l930
 [(pj/series-mapping? [:revenue :cost]) (pj/series-mapping? :revenue)])


(deftest t275_l933 (is ((fn [v] (= [true false] v)) v274_l930)))


(def v276_l935 (kind/doc #'pj/series-mapping))


(def
 v278_l940
 (pj/series-mapping {:series [:revenue :cost], :as :measure}))


(deftest
 t279_l942
 (is ((fn [m] (= {:cols [:revenue :cost], :as :measure} m)) v278_l940)))


(def v281_l946 (kind/doc #'pj/draft))


(def
 v283_l953
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t284_l959
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v283_l953)))


(def v285_l963 (kind/doc #'pj/plan))


(def v287_l967 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v288_l971 plan1)


(deftest
 t289_l973
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v288_l971)))


(def v290_l976 (kind/doc #'pj/frames))


(def v292_l982 (-> plan1 pj/frames kind/pprint))


(deftest
 t293_l984
 (is
  ((fn
    [m]
    (and
     (= [0.0 0.0 600.0 400.0] (:canvas m))
     (= 1 (count (:panels m)))
     (true? (-> m :panels first :invertible?))
     (= 4 (count (-> m :panels first :frames :drawing-area)))))
   v292_l982)))


(def
 v295_l997
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
 t296_l1009
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
   v295_l997)))


(def v298_l1023 (kind/doc #'pj/to-drawing))


(def v299_l1025 (pj/to-drawing (-> plan1 pj/frames :panels first) 2 5))


(deftest t300_l1027 (is ((fn [v] (= 2 (count v))) v299_l1025)))


(def v301_l1029 (kind/doc #'pj/to-data))


(def
 v303_l1033
 (pj/to-drawing
  (-> plan1 pj/frames :panels first)
  {:x [2 3], :y [5 6]}))


(deftest
 t304_l1036
 (is
  ((fn
    [ds]
    (and
     (= [:x :y] (vec (tc/column-names ds)))
     (= 2 (tc/row-count ds))))
   v303_l1033)))


(def
 v306_l1041
 (let
  [panel (-> plan1 pj/frames :panels first)]
  (->>
   (pj/to-drawing panel 2 5)
   (apply pj/to-data panel)
   (mapv (fn* [p1__11199#] (Math/round (double p1__11199#)))))))


(deftest t307_l1046 (is ((fn [v] (= [2 5] v)) v306_l1041)))


(def
 v309_l1053
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
 t310_l1062
 (is ((fn [m] (= "versicolor" (:back m))) v309_l1053)))


(def v311_l1064 (kind/doc #'pj/svg-summary))


(def
 v312_l1066
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t313_l1069
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v312_l1066)))


(def v314_l1072 (kind/doc #'pj/valid-pose?))


(def v315_l1074 (pj/valid-pose? (pj/lay-point tiny :x :y)))


(deftest t316_l1076 (is (true? v315_l1074)))


(def v317_l1078 (kind/doc #'pj/explain-pose))


(def v318_l1080 (pj/explain-pose (pj/lay-point tiny :x :y)))


(deftest t319_l1082 (is (nil? v318_l1080)))


(def v320_l1084 (kind/doc #'pj/valid-plan?))


(def v321_l1086 (pj/valid-plan? plan1))


(deftest t322_l1088 (is (true? v321_l1086)))


(def v323_l1090 (kind/doc #'pj/explain-plan))


(def v324_l1092 (pj/explain-plan plan1))


(deftest t325_l1094 (is (nil? v324_l1092)))


(def v327_l1111 (kind/doc #'pj/membrane))


(def
 v329_l1121
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t330_l1127
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v329_l1121)))


(def v331_l1133 (kind/doc #'pj/->pose))


(def v333_l1140 (pj/pose? (pj/->pose tiny)))


(deftest t334_l1142 (is (true? v333_l1140)))


(def v335_l1144 (kind/doc #'pj/infer-mapping))


(def
 v337_l1150
 (->
  {:height [150 160 170], :weight [50 60 72]}
  pj/->pose
  pj/infer-mapping
  :mapping))


(deftest
 t338_l1155
 (is ((fn [m] (= {:x :height, :y :weight} m)) v337_l1150)))


(def
 v340_l1162
 (let
  [built (pj/lay-point tiny :x :y)]
  [(:mapping built) (:mapping (pj/infer-mapping built))]))


(deftest t341_l1166 (is ((fn [v] (= [nil nil] v)) v340_l1162)))


(def
 v343_l1171
 (-> tiny (pj/pose {:color :group}) pj/infer-mapping :mapping))


(deftest
 t344_l1176
 (is ((fn [m] (= {:x :x, :y :y, :color :group} m)) v343_l1171)))


(def v345_l1178 (kind/doc #'pj/pose->draft))


(def
 v347_l1184
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t348_l1187 (is (true? v347_l1184)))


(def v349_l1189 (kind/doc #'pj/plan->membrane))


(def v350_l1191 (def m1 (pj/plan->membrane plan1)))


(def v351_l1193 (pj/membrane? m1))


(deftest t352_l1195 (is (true? v351_l1193)))


(def v353_l1197 (kind/doc #'pj/valid-membrane?))


(def v354_l1199 (pj/valid-membrane? m1))


(deftest t355_l1201 (is (true? v354_l1199)))


(def v356_l1203 (kind/doc #'pj/explain-membrane))


(def v357_l1205 (pj/explain-membrane m1))


(deftest t358_l1207 (is (nil? v357_l1205)))


(def v359_l1209 (kind/doc #'pj/membrane->plot))


(def v360_l1211 (first (pj/membrane->plot m1 :svg {})))


(deftest t361_l1213 (is ((fn [v] (= :svg v)) v360_l1211)))


(def v362_l1215 (kind/doc #'pj/plan->plot))


(def v363_l1217 (first (pj/plan->plot plan1 :svg {})))


(deftest t364_l1219 (is ((fn [v] (= :svg v)) v363_l1217)))


(def v366_l1226 (kind/doc #'pj/draft->plan))


(def v367_l1228 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v368_l1230 (pj/plan? (pj/draft->plan draft1)))


(deftest t369_l1232 (is (true? v368_l1230)))


(def v370_l1234 (kind/doc #'pj/draft->membrane))


(def v371_l1236 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t372_l1238 (is (true? v371_l1236)))


(def v373_l1240 (kind/doc #'pj/draft->plot))


(def v374_l1242 (first (pj/draft->plot draft1 :svg {})))


(deftest t375_l1244 (is ((fn [v] (= :svg v)) v374_l1242)))


(def v377_l1248 (kind/doc #'pj/config))


(def v378_l1250 (pj/config))


(deftest t379_l1252 (is ((fn [m] (map? m)) v378_l1250)))


(def v380_l1254 (kind/doc #'pj/set-config!))


(def v381_l1256 (kind/doc #'pj/with-config))


(def
 v382_l1258
 (pj/with-config {:color-values :pastel1} (:color-values (pj/config))))


(deftest t383_l1261 (is ((fn [p] (= :pastel1 p)) v382_l1258)))


(def v385_l1267 (kind/doc #'pj/config-key-docs))


(def v386_l1269 (count pj/config-key-docs))


(deftest t387_l1271 (is ((fn [n] (= 47 n)) v386_l1269)))


(def v388_l1273 (kind/doc #'pj/plot-option-docs))


(def v389_l1275 (count pj/plot-option-docs))


(deftest t390_l1277 (is ((fn [n] (= 14 n)) v389_l1275)))


(def v391_l1279 (kind/doc #'pj/layer-option-docs))


(def v392_l1281 (count pj/layer-option-docs))


(deftest t393_l1283 (is ((fn [n] (= 57 n)) v392_l1281)))


(def v395_l1289 (kind/doc #'pj/panel-aesthetic-docs))


(def v396_l1291 (keys pj/panel-aesthetic-docs))


(deftest
 t397_l1293
 (is ((fn [ks] (= #{:col :row} (set ks))) v396_l1291)))


(def v399_l1297 (kind/doc #'pj/aesthetic-roles))


(def
 v400_l1299
 (select-keys (pj/aesthetic-roles) [:x :color :group :col]))


(deftest
 t401_l1301
 (is
  ((fn
    [m]
    (=
     {:x :positional,
      :color :appearance,
      :group :grouping,
      :col :panel}
     m))
   v400_l1299)))


(def v403_l1309 (kind/doc #'pj/aesthetic-scales))


(def v404_l1311 (first pj/aesthetic-scales))


(deftest
 t405_l1313
 (is
  ((fn
    [m]
    (and
     (= :x (:aesthetic m))
     (= [:linear :log :categorical] (:types m))))
   v404_l1311)))


(def v407_l1319 (kind/doc #'pj/compound-key-aesthetics))


(def v408_l1321 (pj/compound-key-aesthetics))


(deftest
 t409_l1323
 (is ((fn [s] (= #{:group :col :row} s)) v408_l1321)))


(def v411_l1328 (kind/doc #'pj/shape-palette))


(def v412_l1330 (pj/shape-palette))


(deftest
 t413_l1332
 (is
  ((fn [syms] (and (seq syms) (every? (set (pj/shape-symbols)) syms)))
   v412_l1330)))


(def v415_l1337 (kind/doc #'pj/layer-type-lookup))


(def v416_l1339 (pj/layer-type-lookup :smooth))


(deftest
 t417_l1341
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v416_l1339)))


(def v418_l1344 (kind/doc #'pj/registered-layer-types))


(def v419_l1346 (count (pj/registered-layer-types)))


(deftest t420_l1348 (is ((fn [n] (= 26 n)) v419_l1346)))


(def v421_l1350 (first (pj/registered-layer-types)))


(deftest
 t422_l1352
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v421_l1350)))


(def v424_l1360 (kind/doc #'pj/stat-doc))


(def v425_l1362 (pj/stat-doc :linear-model))


(deftest t426_l1364 (is ((fn [s] (string? s)) v425_l1362)))


(def v427_l1366 (kind/doc #'pj/mark-doc))


(def v428_l1368 (pj/mark-doc :point))


(deftest t429_l1370 (is ((fn [s] (string? s)) v428_l1368)))


(def v430_l1372 (kind/doc #'pj/position-doc))


(def v431_l1374 (pj/position-doc :dodge))


(deftest t432_l1376 (is ((fn [s] (string? s)) v431_l1374)))


(def v433_l1378 (kind/doc #'pj/scale-doc))


(def v434_l1380 (pj/scale-doc :linear))


(deftest t435_l1382 (is ((fn [s] (string? s)) v434_l1380)))


(def v436_l1384 (kind/doc #'pj/coord-doc))


(def v437_l1386 (pj/coord-doc :cartesian))


(deftest t438_l1388 (is ((fn [s] (string? s)) v437_l1386)))


(def v439_l1390 (kind/doc #'pj/membrane-mark-doc))


(def v440_l1392 (pj/membrane-mark-doc :point))


(deftest t441_l1394 (is ((fn [s] (string? s)) v440_l1392)))


(def v443_l1398 (kind/doc #'pj/save))


(def
 v445_l1402
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t446_l1408 (is (true? v445_l1402)))


(def
 v448_l1413
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
 t449_l1422
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v448_l1413)))


(def
 v451_l1427
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


(deftest t452_l1436 (is ((fn [bs] (= [137 80 78 71] bs)) v451_l1427)))


(def v454_l1443 (kind/doc #'pj/overlay))


(def
 v456_l1448
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort)
  pj/overlay
  pj/svg-summary
  :panels))


(deftest t457_l1455 (is ((fn [n] (= 1 n)) v456_l1448)))
