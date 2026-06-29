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


(def v32_l126 (kind/doc #'pj/lay))


(def
 v34_l134
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay :point)))


(deftest
 t35_l138
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v34_l134)))


(def v37_l149 (kind/doc #'pj/lay-point))


(def
 v38_l151
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l154
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v38_l151)))


(def v40_l157 (kind/doc #'pj/lay-line))


(def
 v41_l159
 (def
  wave
  {:x (range 30),
   :y
   (map
    (fn* [p1__100691#] (Math/sin (* p1__100691# 0.3)))
    (range 30))}))


(def v42_l162 (-> wave (pj/lay-line :x :y)))


(deftest
 t43_l165
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:lines s)))) v42_l162)))


(def v44_l168 (kind/doc #'pj/lay-histogram))


(def
 v45_l170
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t46_l173
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v45_l170)))


(def
 v48_l178
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width]))


(deftest
 t49_l180
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (pos? (:polygons s)))))
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


(def v60_l211 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t61_l214
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v60_l211)))


(def v63_l220 (-> sales (pj/lay-bar :product :revenue {:stat :count})))


(deftest
 t64_l223
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v63_l220)))


(def
 v66_l229
 (->
  {:hour [9 10 11], :sales [3 5 4]}
  (pj/lay-bar :hour :sales {:x-type :categorical})))


(deftest
 t67_l232
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v66_l229)))


(def v69_l239 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t70_l242
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v69_l239)))


(def
 v72_l251
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t73_l255
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v72_l251)))


(def v74_l259 (kind/doc #'pj/lay-smooth))


(def
 v75_l261
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__100692#]
      (+
       (Math/sin (* p1__100692# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t76_l270
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v75_l261)))


(def v77_l274 (kind/doc #'pj/lay-density))


(def
 v78_l276
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t79_l279
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v78_l276)))


(def v80_l282 (kind/doc #'pj/lay-area))


(def v81_l284 (-> wave (pj/lay-area :x :y)))


(deftest
 t82_l287
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v81_l284)))


(def
 v84_l292
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
 t85_l299
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v84_l292)))


(def v86_l302 (kind/doc #'pj/lay-text))


(def
 v87_l304
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t88_l307
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v87_l304)))


(def v89_l310 (kind/doc #'pj/lay-label))


(def
 v90_l312
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t91_l316
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v90_l312)))


(def v92_l320 (kind/doc #'pj/lay-boxplot))


(def
 v93_l322
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t94_l325
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v93_l322)))


(def v95_l329 (kind/doc #'pj/lay-violin))


(def
 v96_l331
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t97_l334
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v96_l331)))


(def v98_l337 (kind/doc #'pj/lay-errorbar))


(def
 v99_l339
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t100_l343
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v99_l339)))


(def v101_l347 (kind/doc #'pj/lay-lollipop))


(def v102_l349 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t103_l352
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v102_l349)))


(def v104_l356 (kind/doc #'pj/lay-tile))


(def
 v105_l358
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t106_l361
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v105_l358)))


(def v107_l364 (kind/doc #'pj/lay-density-2d))


(def
 v108_l366
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t109_l369
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v108_l366)))


(def v110_l372 (kind/doc #'pj/lay-contour))


(def
 v111_l374
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t112_l377
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v111_l374)))


(def v113_l380 (kind/doc #'pj/lay-ridgeline))


(def
 v114_l382
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t115_l385
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v114_l382)))


(def v116_l388 (kind/doc #'pj/lay-rug))


(def
 v117_l390
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t118_l394
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v117_l390)))


(def v119_l397 (kind/doc #'pj/lay-step))


(def v120_l399 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t121_l403
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v120_l399)))


(def v122_l407 (kind/doc #'pj/lay-summary))


(def
 v123_l409
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t124_l412
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v123_l409)))


(def v125_l416 (kind/doc #'pj/lay-interval-h))


(def
 v126_l418
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
 t127_l423
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v126_l418)))


(def v129_l451 (kind/doc #'pj/lay-rule-v))


(def
 v130_l453
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t131_l457
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v130_l453)))


(def
 v133_l464
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
 t134_l470
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v133_l464)))


(def v135_l474 (kind/doc #'pj/lay-rule-h))


(def
 v136_l476
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t137_l480
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v136_l476)))


(def v138_l484 (kind/doc #'pj/lay-band-v))


(def
 v139_l486
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t140_l490
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v139_l486)))


(def v141_l493 (kind/doc #'pj/lay-band-h))


(def
 v142_l495
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t143_l499
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v142_l495)))


(def v145_l504 (kind/doc #'pj/coord))


(def
 v147_l508
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t148_l511
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v147_l508)))


(def
 v150_l516
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t151_l519
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v150_l516)))


(def v152_l522 (kind/doc #'pj/scale))


(def
 v154_l526
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t155_l529
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v154_l526)))


(def
 v157_l534
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t158_l537
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v157_l534)))


(def
 v160_l543
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t161_l547
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v160_l543)))


(def
 v163_l552
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t164_l558
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v163_l552)))


(def v166_l564 (kind/doc #'pj/facet))


(def
 v167_l566
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t168_l570
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v167_l566)))


(def v169_l574 (kind/doc #'pj/facet-grid))


(def
 v170_l576
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t171_l580
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v170_l576)))


(def v173_l586 (kind/doc #'pj/arrange))


(def
 v174_l588
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


(deftest t175_l596 (is ((fn [v] (pj/pose? v)) v174_l588)))


(def v177_l600 (kind/doc #'pj/plot))


(def v179_l605 (-> tiny (pj/lay-point :x :y)))


(deftest
 t180_l608
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v179_l605)))


(def v181_l611 (kind/doc #'pj/options))


(def
 v183_l615
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t184_l619
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v183_l615)))


(def v186_l625 (kind/doc #'pj/pose?))


(def v188_l629 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t189_l631 (is (true? v188_l629)))


(def v190_l633 (kind/doc #'pj/plan?))


(def v192_l637 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t193_l639 (is (true? v192_l637)))


(def v194_l641 (kind/doc #'pj/leaf-plan?))


(def v196_l646 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t197_l648 (is (true? v196_l646)))


(def v198_l650 (kind/doc #'pj/composite-plan?))


(def
 v200_l655
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t201_l659 (is (true? v200_l655)))


(def v202_l661 (kind/doc #'pj/draft?))


(def v204_l666 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t205_l668 (is (true? v204_l666)))


(def v206_l670 (kind/doc #'pj/leaf-draft?))


(def v208_l675 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t209_l677 (is (true? v208_l675)))


(def v210_l679 (kind/doc #'pj/composite-draft?))


(def
 v212_l684
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t213_l688 (is (true? v212_l684)))


(def v214_l690 (kind/doc #'pj/plan-layer?))


(def
 v216_l694
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t217_l696 (is (true? v216_l694)))


(def v218_l698 (kind/doc #'pj/layer-type?))


(def v220_l702 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t221_l704 (is (true? v220_l702)))


(def v222_l706 (kind/doc #'pj/membrane?))


(def v224_l711 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t225_l713 (is (true? v224_l711)))


(def v227_l717 (kind/doc #'pj/draft))


(def
 v229_l724
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t230_l730
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v229_l724)))


(def v231_l734 (kind/doc #'pj/plan))


(def v233_l738 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v234_l742 plan1)


(deftest
 t235_l744
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v234_l742)))


(def v236_l747 (kind/doc #'pj/svg-summary))


(def
 v237_l749
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t238_l752
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v237_l749)))


(def v239_l755 (kind/doc #'pj/valid-plan?))


(def v240_l757 (pj/valid-plan? plan1))


(deftest t241_l759 (is (true? v240_l757)))


(def v242_l761 (kind/doc #'pj/explain-plan))


(def v243_l763 (pj/explain-plan plan1))


(deftest t244_l765 (is (nil? v243_l763)))


(def v246_l777 (kind/doc #'pj/membrane))


(def
 v248_l787
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t249_l793
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v248_l787)))


(def v250_l799 (kind/doc #'pj/->pose))


(def v252_l806 (pj/pose? (pj/->pose tiny)))


(deftest t253_l808 (is (true? v252_l806)))


(def v254_l810 (kind/doc #'pj/pose->draft))


(def
 v256_l816
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t257_l819 (is (true? v256_l816)))


(def v258_l821 (kind/doc #'pj/plan->membrane))


(def v259_l823 (def m1 (pj/plan->membrane plan1)))


(def v260_l825 (pj/membrane? m1))


(deftest t261_l827 (is (true? v260_l825)))


(def v262_l829 (kind/doc #'pj/valid-membrane?))


(def v263_l831 (pj/valid-membrane? m1))


(deftest t264_l833 (is (true? v263_l831)))


(def v265_l835 (kind/doc #'pj/explain-membrane))


(def v266_l837 (pj/explain-membrane m1))


(deftest t267_l839 (is (nil? v266_l837)))


(def v268_l841 (kind/doc #'pj/membrane->plot))


(def v269_l843 (first (pj/membrane->plot m1 :svg {})))


(deftest t270_l845 (is ((fn [v] (= :svg v)) v269_l843)))


(def v271_l847 (kind/doc #'pj/plan->plot))


(def v272_l849 (first (pj/plan->plot plan1 :svg {})))


(deftest t273_l851 (is ((fn [v] (= :svg v)) v272_l849)))


(def v275_l858 (kind/doc #'pj/draft->plan))


(def v276_l860 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v277_l862 (pj/plan? (pj/draft->plan draft1)))


(deftest t278_l864 (is (true? v277_l862)))


(def v279_l866 (kind/doc #'pj/draft->membrane))


(def v280_l868 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t281_l870 (is (true? v280_l868)))


(def v282_l872 (kind/doc #'pj/draft->plot))


(def v283_l874 (first (pj/draft->plot draft1 :svg {})))


(deftest t284_l876 (is ((fn [v] (= :svg v)) v283_l874)))


(def v286_l880 (kind/doc #'pj/config))


(def v287_l882 (pj/config))


(deftest t288_l884 (is ((fn [m] (map? m)) v287_l882)))


(def v289_l886 (kind/doc #'pj/set-config!))


(def v290_l888 (kind/doc #'pj/with-config))


(def
 v291_l890
 (pj/with-config {:palette :pastel1} (:palette (pj/config))))


(deftest t292_l893 (is ((fn [p] (= :pastel1 p)) v291_l890)))


(def v294_l899 (kind/doc #'pj/config-key-docs))


(def v295_l901 (count pj/config-key-docs))


(deftest t296_l903 (is ((fn [n] (= 37 n)) v295_l901)))


(def v297_l905 (kind/doc #'pj/plot-option-docs))


(def v298_l907 (count pj/plot-option-docs))


(deftest t299_l909 (is ((fn [n] (= 14 n)) v298_l907)))


(def v300_l911 (kind/doc #'pj/layer-option-docs))


(def v301_l913 (count pj/layer-option-docs))


(deftest t302_l915 (is ((fn [n] (pos? n)) v301_l913)))


(def v304_l919 (kind/doc #'pj/layer-type-lookup))


(def v305_l921 (pj/layer-type-lookup :smooth))


(deftest
 t306_l923
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v305_l921)))


(def v307_l926 (kind/doc #'pj/registered-layer-types))


(def v308_l928 (count (pj/registered-layer-types)))


(deftest t309_l930 (is ((fn [n] (= 25 n)) v308_l928)))


(def v310_l932 (first (pj/registered-layer-types)))


(deftest
 t311_l934
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v310_l932)))


(def v313_l942 (kind/doc #'pj/stat-doc))


(def v314_l944 (pj/stat-doc :linear-model))


(deftest t315_l946 (is ((fn [s] (string? s)) v314_l944)))


(def v316_l948 (kind/doc #'pj/mark-doc))


(def v317_l950 (pj/mark-doc :point))


(deftest t318_l952 (is ((fn [s] (string? s)) v317_l950)))


(def v319_l954 (kind/doc #'pj/position-doc))


(def v320_l956 (pj/position-doc :dodge))


(deftest t321_l958 (is ((fn [s] (string? s)) v320_l956)))


(def v322_l960 (kind/doc #'pj/scale-doc))


(def v323_l962 (pj/scale-doc :linear))


(deftest t324_l964 (is ((fn [s] (string? s)) v323_l962)))


(def v325_l966 (kind/doc #'pj/coord-doc))


(def v326_l968 (pj/coord-doc :cartesian))


(deftest t327_l970 (is ((fn [s] (string? s)) v326_l968)))


(def v328_l972 (kind/doc #'pj/membrane-mark-doc))


(def v329_l974 (pj/membrane-mark-doc :point))


(deftest t330_l976 (is ((fn [s] (string? s)) v329_l974)))


(def v332_l980 (kind/doc #'pj/save))


(def
 v334_l984
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t335_l990 (is (true? v334_l984)))


(def
 v337_l995
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
    (mapv (fn* [p1__100693#] (bit-and p1__100693# 255)) (vec bs))))))


(deftest
 t338_l1004
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v337_l995)))


(def
 v340_l1009
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
    (mapv (fn* [p1__100694#] (bit-and p1__100694# 255)) (vec bs))))))


(deftest t341_l1018 (is ((fn [bs] (= [137 80 78 71] bs)) v340_l1009)))
