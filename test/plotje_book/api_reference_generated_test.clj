(ns
 plotje-book.api-reference-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [clojure.test :refer [deftest is]]))


(def
 v3_l26
 (def tiny {:x [1 2 3 4 5], :y [2 4 1 5 3], :group [:a :a :b :b :b]}))


(def
 v4_l30
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def
 v5_l33
 (def
  measurements
  {:treatment ["A" "B" "C" "D"],
   :mean [10.0 15.0 12.0 18.0],
   :ci-lo [8.0 12.0 9.5 15.5],
   :ci-hi [12.0 18.0 14.5 20.5]}))


(def v7_l47 (kind/doc #'pj/pose))


(def
 v9_l51
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t10_l55
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v9_l51)))


(def
 v12_l61
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t13_l66
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v12_l61)))


(def v14_l70 (kind/doc #'pj/with-data))


(def
 v16_l76
 (def
  scatter-template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def v17_l80 (-> scatter-template (pj/with-data tiny)))


(deftest
 t18_l83
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v17_l80)))


(def
 v20_l88
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point {:color :species})))


(deftest
 t21_l93
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v20_l88)))


(def
 v23_l99
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length, :y :sepal-width})
  pj/lay-point))


(deftest
 t24_l103
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v23_l99)))


(def v25_l107 (kind/doc #'pj/cross))


(def v26_l109 (pj/cross [:a :b] [1 2 3]))


(deftest
 t27_l111
 (is
  ((fn [v] (= [[:a 1] [:a 2] [:a 3] [:b 1] [:b 2] [:b 3]] v))
   v26_l109)))


(def
 v29_l115
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   (pj/cross [:sepal-length :petal-length] [:sepal-width :petal-width])
   {:color :species})))


(deftest
 t30_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 600 (:points s)))))
   v29_l115)))


(def
 v32_l126
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width]))


(deftest
 t33_l128
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (pos? (:polygons s)))))
   v32_l126)))


(def v35_l134 (kind/doc #'pj/lay))


(def
 v37_l142
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t38_l146
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v37_l142)))


(def v40_l157 (kind/doc #'pj/lay-point))


(def
 v41_l159
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t42_l162
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v41_l159)))


(def v43_l165 (kind/doc #'pj/lay-line))


(def
 v44_l167
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__93426#] (Math/sin (* p1__93426# 0.3))) (range 30))}))


(def v45_l170 (-> wave (pj/lay-line :x :y)))


(deftest
 t46_l173
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v45_l170)))


(def v47_l176 (kind/doc #'pj/lay-histogram))


(def
 v48_l178
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t49_l181
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v48_l178)))


(def v50_l184 (kind/doc #'pj/lay-bar))


(def v51_l186 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t52_l189
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v51_l186)))


(def
 v54_l194
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t55_l197
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v54_l194)))


(def
 v57_l202
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t58_l205
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v57_l202)))


(def v59_l208 (kind/doc #'pj/lay-value-bar))


(def v60_l210 (-> sales (pj/lay-value-bar :product :revenue)))


(deftest
 t61_l213
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v60_l210)))


(def
 v63_l218
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t64_l222
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v63_l218)))


(def v65_l226 (kind/doc #'pj/lay-smooth))


(def
 v66_l228
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__93427#]
      (+
       (Math/sin (* p1__93427# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t67_l237
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v66_l228)))


(def v68_l241 (kind/doc #'pj/lay-density))


(def
 v69_l243
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t70_l246
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v69_l243)))


(def v71_l249 (kind/doc #'pj/lay-area))


(def v72_l251 (-> wave (pj/lay-area :x :y)))


(deftest
 t73_l254
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v72_l251)))


(def
 v75_l259
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
 t76_l266
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v75_l259)))


(def v77_l269 (kind/doc #'pj/lay-text))


(def
 v78_l271
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t79_l274
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v78_l271)))


(def v80_l277 (kind/doc #'pj/lay-label))


(def
 v81_l279
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t82_l283
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v81_l279)))


(def v83_l287 (kind/doc #'pj/lay-boxplot))


(def
 v84_l289
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t85_l292
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v84_l289)))


(def v86_l296 (kind/doc #'pj/lay-violin))


(def
 v87_l298
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t88_l301
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v87_l298)))


(def v89_l304 (kind/doc #'pj/lay-errorbar))


(def
 v90_l306
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t91_l310
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v90_l306)))


(def v92_l314 (kind/doc #'pj/lay-lollipop))


(def v93_l316 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t94_l319
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v93_l316)))


(def v95_l323 (kind/doc #'pj/lay-tile))


(def
 v96_l325
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t97_l328
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v96_l325)))


(def v98_l331 (kind/doc #'pj/lay-density-2d))


(def
 v99_l333
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t100_l336
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v99_l333)))


(def v101_l339 (kind/doc #'pj/lay-contour))


(def
 v102_l341
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t103_l344
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v102_l341)))


(def v104_l347 (kind/doc #'pj/lay-ridgeline))


(def
 v105_l349
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t106_l352
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v105_l349)))


(def v107_l355 (kind/doc #'pj/lay-rug))


(def
 v108_l357
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t109_l361
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v108_l357)))


(def v110_l364 (kind/doc #'pj/lay-step))


(def v111_l366 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t112_l370
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v111_l366)))


(def v113_l374 (kind/doc #'pj/lay-summary))


(def
 v114_l376
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t115_l379
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v114_l376)))


(def v116_l383 (kind/doc #'pj/lay-interval-h))


(def
 v117_l385
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
 t118_l390
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v117_l385)))


(def v120_l418 (kind/doc #'pj/lay-rule-v))


(def
 v121_l420
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t122_l424
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v121_l420)))


(def
 v124_l431
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
 t125_l437
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v124_l431)))


(def v126_l441 (kind/doc #'pj/lay-rule-h))


(def
 v127_l443
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t128_l447
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v127_l443)))


(def v129_l451 (kind/doc #'pj/lay-band-v))


(def
 v130_l453
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t131_l457
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v130_l453)))


(def v132_l460 (kind/doc #'pj/lay-band-h))


(def
 v133_l462
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t134_l466
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v133_l462)))


(def v136_l471 (kind/doc #'pj/coord))


(def
 v138_l475
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t139_l478
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v138_l475)))


(def
 v141_l483
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t142_l486
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v141_l483)))


(def v143_l489 (kind/doc #'pj/scale))


(def
 v145_l493
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t146_l496
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v145_l493)))


(def
 v148_l501
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t149_l504
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v148_l501)))


(def
 v151_l510
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t152_l514
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v151_l510)))


(def
 v154_l519
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t155_l525
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v154_l519)))


(def v157_l531 (kind/doc #'pj/facet))


(def
 v158_l533
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t159_l537
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v158_l533)))


(def v160_l541 (kind/doc #'pj/facet-grid))


(def
 v161_l543
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t162_l547
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v161_l543)))


(def v164_l553 (kind/doc #'pj/arrange))


(def
 v165_l555
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


(deftest t166_l563 (is ((fn [v] (pj/pose? v)) v165_l555)))


(def v168_l567 (kind/doc #'pj/plot))


(def v170_l572 (-> tiny (pj/lay-point :x :y)))


(deftest
 t171_l575
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v170_l572)))


(def v172_l578 (kind/doc #'pj/options))


(def
 v174_l582
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t175_l586
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v174_l582)))


(def v177_l592 (kind/doc #'pj/pose?))


(def v179_l596 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t180_l598 (is (true? v179_l596)))


(def v181_l600 (kind/doc #'pj/plan?))


(def v183_l604 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t184_l606 (is (true? v183_l604)))


(def v185_l608 (kind/doc #'pj/leaf-plan?))


(def v187_l613 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t188_l615 (is (true? v187_l613)))


(def v189_l617 (kind/doc #'pj/composite-plan?))


(def
 v191_l622
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t192_l626 (is (true? v191_l622)))


(def v193_l628 (kind/doc #'pj/draft?))


(def v195_l633 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t196_l635 (is (true? v195_l633)))


(def v197_l637 (kind/doc #'pj/leaf-draft?))


(def v199_l642 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t200_l644 (is (true? v199_l642)))


(def v201_l646 (kind/doc #'pj/composite-draft?))


(def
 v203_l651
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t204_l655 (is (true? v203_l651)))


(def v205_l657 (kind/doc #'pj/plan-layer?))


(def
 v207_l661
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t208_l663 (is (true? v207_l661)))


(def v209_l665 (kind/doc #'pj/layer-type?))


(def v211_l669 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t212_l671 (is (true? v211_l669)))


(def v213_l673 (kind/doc #'pj/membrane?))


(def v215_l678 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t216_l680 (is (true? v215_l678)))


(def v218_l684 (kind/doc #'pj/draft))


(def
 v220_l691
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t221_l697
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v220_l691)))


(def v222_l701 (kind/doc #'pj/plan))


(def v224_l705 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v225_l709 plan1)


(deftest
 t226_l711
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v225_l709)))


(def v227_l714 (kind/doc #'pj/svg-summary))


(def
 v228_l716
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t229_l719
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v228_l716)))


(def v230_l722 (kind/doc #'pj/valid-plan?))


(def v231_l724 (pj/valid-plan? plan1))


(deftest t232_l726 (is (true? v231_l724)))


(def v233_l728 (kind/doc #'pj/explain-plan))


(def v234_l730 (pj/explain-plan plan1))


(deftest t235_l732 (is (nil? v234_l730)))


(def v237_l744 (kind/doc #'pj/membrane))


(def
 v239_l754
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t240_l760
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v239_l754)))


(def v241_l766 (kind/doc #'pj/->pose))


(def v243_l773 (pj/pose? (pj/->pose tiny)))


(deftest t244_l775 (is (true? v243_l773)))


(def v245_l777 (kind/doc #'pj/pose->draft))


(def
 v247_l783
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t248_l786 (is (true? v247_l783)))


(def v249_l788 (kind/doc #'pj/plan->membrane))


(def v250_l790 (def m1 (pj/plan->membrane plan1)))


(def v251_l792 (pj/membrane? m1))


(deftest t252_l794 (is (true? v251_l792)))


(def v253_l796 (kind/doc #'pj/membrane->plot))


(def v254_l798 (first (pj/membrane->plot m1 :svg {})))


(deftest t255_l800 (is ((fn [v] (= :svg v)) v254_l798)))


(def v256_l802 (kind/doc #'pj/plan->plot))


(def v257_l804 (first (pj/plan->plot plan1 :svg {})))


(deftest t258_l806 (is ((fn [v] (= :svg v)) v257_l804)))


(def v260_l813 (kind/doc #'pj/draft->plan))


(def v261_l815 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v262_l817 (pj/plan? (pj/draft->plan draft1)))


(deftest t263_l819 (is (true? v262_l817)))


(def v264_l821 (kind/doc #'pj/draft->membrane))


(def v265_l823 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t266_l825 (is (true? v265_l823)))


(def v267_l827 (kind/doc #'pj/draft->plot))


(def v268_l829 (first (pj/draft->plot draft1 :svg {})))


(deftest t269_l831 (is ((fn [v] (= :svg v)) v268_l829)))


(def v271_l835 (kind/doc #'pj/config))


(def v272_l837 (pj/config))


(deftest t273_l839 (is ((fn [m] (map? m)) v272_l837)))


(def v274_l841 (kind/doc #'pj/set-config!))


(def v275_l843 (kind/doc #'pj/with-config))


(def
 v276_l845
 (pj/with-config {:palette :pastel1} (:palette (pj/config))))


(deftest t277_l848 (is ((fn [p] (= :pastel1 p)) v276_l845)))


(def v279_l854 (kind/doc #'pj/config-key-docs))


(def v280_l856 (count pj/config-key-docs))


(deftest t281_l858 (is ((fn [n] (= 37 n)) v280_l856)))


(def v282_l860 (kind/doc #'pj/plot-option-docs))


(def v283_l862 (count pj/plot-option-docs))


(deftest t284_l864 (is ((fn [n] (= 14 n)) v283_l862)))


(def v285_l866 (kind/doc #'pj/layer-option-docs))


(def v286_l868 (count pj/layer-option-docs))


(deftest t287_l870 (is ((fn [n] (pos? n)) v286_l868)))


(def v289_l874 (kind/doc #'pj/layer-type-lookup))


(def v290_l876 (pj/layer-type-lookup :smooth))


(deftest
 t291_l878
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v290_l876)))


(def v292_l881 (kind/doc #'pj/registered-layer-types))


(def v293_l883 (count (pj/registered-layer-types)))


(deftest t294_l885 (is ((fn [n] (= 26 n)) v293_l883)))


(def v295_l887 (first (pj/registered-layer-types)))


(deftest
 t296_l889
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v295_l887)))


(def v298_l897 (kind/doc #'pj/stat-doc))


(def v299_l899 (pj/stat-doc :linear-model))


(deftest t300_l901 (is ((fn [s] (string? s)) v299_l899)))


(def v301_l903 (kind/doc #'pj/mark-doc))


(def v302_l905 (pj/mark-doc :point))


(deftest t303_l907 (is ((fn [s] (string? s)) v302_l905)))


(def v304_l909 (kind/doc #'pj/position-doc))


(def v305_l911 (pj/position-doc :dodge))


(deftest t306_l913 (is ((fn [s] (string? s)) v305_l911)))


(def v307_l915 (kind/doc #'pj/scale-doc))


(def v308_l917 (pj/scale-doc :linear))


(deftest t309_l919 (is ((fn [s] (string? s)) v308_l917)))


(def v310_l921 (kind/doc #'pj/coord-doc))


(def v311_l923 (pj/coord-doc :cartesian))


(deftest t312_l925 (is ((fn [s] (string? s)) v311_l923)))


(def v313_l927 (kind/doc #'pj/membrane-mark-doc))


(def v314_l929 (pj/membrane-mark-doc :point))


(deftest t315_l931 (is ((fn [s] (string? s)) v314_l929)))


(def v317_l935 (kind/doc #'pj/save))


(def
 v319_l939
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t320_l945 (is (true? v319_l939)))


(def
 v322_l950
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
    (mapv (fn* [p1__93428#] (bit-and p1__93428# 255)) (vec bs))))))


(deftest
 t323_l959
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v322_l950)))


(def
 v325_l964
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
    (mapv (fn* [p1__93429#] (bit-and p1__93429# 255)) (vec bs))))))


(deftest t326_l973 (is ((fn [bs] (= [137 80 78 71] bs)) v325_l964)))
