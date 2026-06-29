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
   (map (fn* [p1__86549#] (Math/sin (* p1__86549# 0.3))) (range 30))}))


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


(def
 v69_l237
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t70_l241
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v69_l237)))


(def v71_l245 (kind/doc #'pj/lay-smooth))


(def
 v72_l247
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__86550#]
      (+
       (Math/sin (* p1__86550# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t73_l256
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v72_l247)))


(def v74_l260 (kind/doc #'pj/lay-density))


(def
 v75_l262
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t76_l265
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v75_l262)))


(def v77_l268 (kind/doc #'pj/lay-area))


(def v78_l270 (-> wave (pj/lay-area :x :y)))


(deftest
 t79_l273
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v78_l270)))


(def
 v81_l278
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
 t82_l285
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s)))) v81_l278)))


(def v83_l288 (kind/doc #'pj/lay-text))


(def
 v84_l290
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-text :x :y {:text :name})))


(deftest
 t85_l293
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (every? (set (:texts s)) ["A" "B" "C" "D"])))
   v84_l290)))


(def v86_l296 (kind/doc #'pj/lay-label))


(def
 v87_l298
 (->
  {:x [1 2 3 4], :y [4 7 5 8], :name ["A" "B" "C" "D"]}
  (pj/lay-point :x :y {:size 5})
  (pj/lay-label {:text :name})))


(deftest
 t88_l302
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (every? (set (:texts s)) ["A" "B" "C" "D"]))))
   v87_l298)))


(def v89_l306 (kind/doc #'pj/lay-boxplot))


(def
 v90_l308
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t91_l311
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (pos? (:lines s)))))
   v90_l308)))


(def v92_l315 (kind/doc #'pj/lay-violin))


(def
 v93_l317
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t94_l320
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:polygons s)))) v93_l317)))


(def v95_l323 (kind/doc #'pj/lay-errorbar))


(def
 v96_l325
 (->
  measurements
  (pj/lay-point :treatment :mean)
  (pj/lay-errorbar {:y-min :ci-lo, :y-max :ci-hi})))


(deftest
 t97_l329
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v96_l325)))


(def v98_l333 (kind/doc #'pj/lay-lollipop))


(def v99_l335 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t100_l338
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v99_l335)))


(def v101_l342 (kind/doc #'pj/lay-tile))


(def
 v102_l344
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t103_l347
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v102_l344)))


(def v104_l350 (kind/doc #'pj/lay-density-2d))


(def
 v105_l352
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t106_l355
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v105_l352)))


(def v107_l358 (kind/doc #'pj/lay-contour))


(def
 v108_l360
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t109_l363
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v108_l360)))


(def v110_l366 (kind/doc #'pj/lay-ridgeline))


(def
 v111_l368
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t112_l371
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v111_l368)))


(def v113_l374 (kind/doc #'pj/lay-rug))


(def
 v114_l376
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:side :both})))


(deftest
 t115_l380
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 300 (:lines s)))) v114_l376)))


(def v116_l383 (kind/doc #'pj/lay-step))


(def v117_l385 (-> tiny (pj/lay-step :x :y) pj/lay-point))


(deftest
 t118_l389
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v117_l385)))


(def v119_l393 (kind/doc #'pj/lay-summary))


(def
 v120_l395
 (-> (rdatasets/datasets-iris) (pj/lay-summary :species :sepal-length)))


(deftest
 t121_l398
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 3 (:lines s)))))
   v120_l395)))


(def v122_l402 (kind/doc #'pj/lay-interval-h))


(def
 v123_l404
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
 t124_l409
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v123_l404)))


(def v126_l437 (kind/doc #'pj/lay-rule-v))


(def
 v127_l439
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-v {:x-intercept 6.0})))


(deftest
 t128_l443
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v127_l439)))


(def
 v130_l450
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
 t131_l456
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v130_l450)))


(def v132_l460 (kind/doc #'pj/lay-rule-h))


(def
 v133_l462
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t134_l466
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v133_l462)))


(def v135_l470 (kind/doc #'pj/lay-band-v))


(def
 v136_l472
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})))


(deftest
 t137_l476
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v136_l472)))


(def v138_l479 (kind/doc #'pj/lay-band-h))


(def
 v139_l481
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-h {:y-min 2.5, :y-max 3.5})))


(deftest
 t140_l485
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v139_l481)))


(def v142_l490 (kind/doc #'pj/coord))


(def
 v144_l494
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t145_l497
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:polygons s))))
   v144_l494)))


(def
 v147_l502
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :polar)))


(deftest
 t148_l505
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v147_l502)))


(def v149_l508 (kind/doc #'pj/scale))


(def
 v151_l512
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x :log)))


(deftest
 t152_l515
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v151_l512)))


(def
 v154_l520
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :x {:domain [3 9]})))


(deftest
 t155_l523
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v154_l520)))


(def
 v157_l529
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t158_l533
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v157_l529)))


(def
 v160_l538
 (->
  (for [d (range 1 8)] {:day d, :v (mod d 3)})
  (pj/lay-point :day :v)
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t161_l544
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v160_l538)))


(def v163_l550 (kind/doc #'pj/facet))


(def
 v164_l552
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t165_l556
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v164_l552)))


(def v166_l560 (kind/doc #'pj/facet-grid))


(def
 v167_l562
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t168_l566
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v167_l562)))


(def v170_l572 (kind/doc #'pj/arrange))


(def
 v171_l574
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


(deftest t172_l582 (is ((fn [v] (pj/pose? v)) v171_l574)))


(def v174_l586 (kind/doc #'pj/plot))


(def v176_l591 (-> tiny (pj/lay-point :x :y)))


(deftest
 t177_l594
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:points s)))) v176_l591)))


(def v178_l597 (kind/doc #'pj/options))


(def
 v180_l601
 (->
  tiny
  (pj/lay-point :x :y)
  (pj/options {:width 400, :height 200, :title "Small Plot"})))


(deftest
 t181_l605
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (< (:width s) 500) (some #{"Small Plot"} (:texts s)))))
   v180_l601)))


(def v183_l611 (kind/doc #'pj/pose?))


(def v185_l615 (pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point)))


(deftest t186_l617 (is (true? v185_l615)))


(def v187_l619 (kind/doc #'pj/plan?))


(def v189_l623 (pj/plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t190_l625 (is (true? v189_l623)))


(def v191_l627 (kind/doc #'pj/leaf-plan?))


(def v193_l632 (pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y))))


(deftest t194_l634 (is (true? v193_l632)))


(def v195_l636 (kind/doc #'pj/composite-plan?))


(def
 v197_l641
 (pj/composite-plan?
  (pj/plan
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t198_l645 (is (true? v197_l641)))


(def v199_l647 (kind/doc #'pj/draft?))


(def v201_l652 (pj/draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t202_l654 (is (true? v201_l652)))


(def v203_l656 (kind/doc #'pj/leaf-draft?))


(def v205_l661 (pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y))))


(deftest t206_l663 (is (true? v205_l661)))


(def v207_l665 (kind/doc #'pj/composite-draft?))


(def
 v209_l670
 (pj/composite-draft?
  (pj/draft
   (pj/arrange [(pj/lay-point tiny :x :y) (pj/lay-point tiny :x :y)]))))


(deftest t210_l674 (is (true? v209_l670)))


(def v211_l676 (kind/doc #'pj/plan-layer?))


(def
 v213_l680
 (pj/plan-layer?
  (first
   (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y))))))))


(deftest t214_l682 (is (true? v213_l680)))


(def v215_l684 (kind/doc #'pj/layer-type?))


(def v217_l688 (pj/layer-type? (pj/layer-type-lookup :point)))


(deftest t218_l690 (is (true? v217_l688)))


(def v219_l692 (kind/doc #'pj/membrane?))


(def v221_l697 (pj/membrane? (pj/membrane (pj/lay-point tiny :x :y))))


(deftest t222_l699 (is (true? v221_l697)))


(def v224_l703 (kind/doc #'pj/draft))


(def
 v226_l710
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/draft
  kind/pprint))


(deftest
 t227_l716
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v226_l710)))


(def v228_l720 (kind/doc #'pj/plan))


(def v230_l724 (def plan1 (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v231_l728 plan1)


(deftest
 t232_l730
 (is
  ((fn [m] (and (= 600 (:width m)) (= "x" (:x-label m)))) v231_l728)))


(def v233_l733 (kind/doc #'pj/svg-summary))


(def
 v234_l735
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  pj/svg-summary))


(deftest
 t235_l738
 (is ((fn [m] (and (= 1 (:panels m)) (= 150 (:points m)))) v234_l735)))


(def v236_l741 (kind/doc #'pj/valid-plan?))


(def v237_l743 (pj/valid-plan? plan1))


(deftest t238_l745 (is (true? v237_l743)))


(def v239_l747 (kind/doc #'pj/explain-plan))


(def v240_l749 (pj/explain-plan plan1))


(deftest t241_l751 (is (nil? v240_l749)))


(def v243_l763 (kind/doc #'pj/membrane))


(def
 v245_l773
 (let
  [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane? (pj/membrane? m),
   :width (membrane.ui/width m),
   :height (membrane.ui/height m),
   :record-keys (sort (filter keyword? (keys m)))}))


(deftest
 t246_l779
 (is
  ((fn
    [info]
    (and
     (:membrane? info)
     (= 600 (:width info))
     (= 400 (:height info))
     (= [:drawables :height :width] (:record-keys info))))
   v245_l773)))


(def v247_l785 (kind/doc #'pj/->pose))


(def v249_l792 (pj/pose? (pj/->pose tiny)))


(deftest t250_l794 (is (true? v249_l792)))


(def v251_l796 (kind/doc #'pj/pose->draft))


(def
 v253_l802
 (pj/leaf-draft? (pj/pose->draft (pj/lay-point tiny :x :y))))


(deftest t254_l805 (is (true? v253_l802)))


(def v255_l807 (kind/doc #'pj/plan->membrane))


(def v256_l809 (def m1 (pj/plan->membrane plan1)))


(def v257_l811 (pj/membrane? m1))


(deftest t258_l813 (is (true? v257_l811)))


(def v259_l815 (kind/doc #'pj/valid-membrane?))


(def v260_l817 (pj/valid-membrane? m1))


(deftest t261_l819 (is (true? v260_l817)))


(def v262_l821 (kind/doc #'pj/explain-membrane))


(def v263_l823 (pj/explain-membrane m1))


(deftest t264_l825 (is (nil? v263_l823)))


(def v265_l827 (kind/doc #'pj/membrane->plot))


(def v266_l829 (first (pj/membrane->plot m1 :svg {})))


(deftest t267_l831 (is ((fn [v] (= :svg v)) v266_l829)))


(def v268_l833 (kind/doc #'pj/plan->plot))


(def v269_l835 (first (pj/plan->plot plan1 :svg {})))


(deftest t270_l837 (is ((fn [v] (= :svg v)) v269_l835)))


(def v272_l844 (kind/doc #'pj/draft->plan))


(def v273_l846 (def draft1 (pj/draft (pj/lay-point tiny :x :y))))


(def v274_l848 (pj/plan? (pj/draft->plan draft1)))


(deftest t275_l850 (is (true? v274_l848)))


(def v276_l852 (kind/doc #'pj/draft->membrane))


(def v277_l854 (pj/membrane? (pj/draft->membrane draft1)))


(deftest t278_l856 (is (true? v277_l854)))


(def v279_l858 (kind/doc #'pj/draft->plot))


(def v280_l860 (first (pj/draft->plot draft1 :svg {})))


(deftest t281_l862 (is ((fn [v] (= :svg v)) v280_l860)))


(def v283_l866 (kind/doc #'pj/config))


(def v284_l868 (pj/config))


(deftest t285_l870 (is ((fn [m] (map? m)) v284_l868)))


(def v286_l872 (kind/doc #'pj/set-config!))


(def v287_l874 (kind/doc #'pj/with-config))


(def
 v288_l876
 (pj/with-config {:palette :pastel1} (:palette (pj/config))))


(deftest t289_l879 (is ((fn [p] (= :pastel1 p)) v288_l876)))


(def v291_l885 (kind/doc #'pj/config-key-docs))


(def v292_l887 (count pj/config-key-docs))


(deftest t293_l889 (is ((fn [n] (= 37 n)) v292_l887)))


(def v294_l891 (kind/doc #'pj/plot-option-docs))


(def v295_l893 (count pj/plot-option-docs))


(deftest t296_l895 (is ((fn [n] (= 14 n)) v295_l893)))


(def v297_l897 (kind/doc #'pj/layer-option-docs))


(def v298_l899 (count pj/layer-option-docs))


(deftest t299_l901 (is ((fn [n] (pos? n)) v298_l899)))


(def v301_l905 (kind/doc #'pj/layer-type-lookup))


(def v302_l907 (pj/layer-type-lookup :smooth))


(deftest
 t303_l909
 (is
  ((fn [m] (and (= :line (:mark m)) (= :loess (:stat m)))) v302_l907)))


(def v304_l912 (kind/doc #'pj/registered-layer-types))


(def v305_l914 (count (pj/registered-layer-types)))


(deftest t306_l916 (is ((fn [n] (= 25 n)) v305_l914)))


(def v307_l918 (first (pj/registered-layer-types)))


(deftest
 t308_l920
 (is
  ((fn [[k m]] (and (keyword? k) (some? (:mark m)) (some? (:stat m))))
   v307_l918)))


(def v310_l928 (kind/doc #'pj/stat-doc))


(def v311_l930 (pj/stat-doc :linear-model))


(deftest t312_l932 (is ((fn [s] (string? s)) v311_l930)))


(def v313_l934 (kind/doc #'pj/mark-doc))


(def v314_l936 (pj/mark-doc :point))


(deftest t315_l938 (is ((fn [s] (string? s)) v314_l936)))


(def v316_l940 (kind/doc #'pj/position-doc))


(def v317_l942 (pj/position-doc :dodge))


(deftest t318_l944 (is ((fn [s] (string? s)) v317_l942)))


(def v319_l946 (kind/doc #'pj/scale-doc))


(def v320_l948 (pj/scale-doc :linear))


(deftest t321_l950 (is ((fn [s] (string? s)) v320_l948)))


(def v322_l952 (kind/doc #'pj/coord-doc))


(def v323_l954 (pj/coord-doc :cartesian))


(deftest t324_l956 (is ((fn [s] (string? s)) v323_l954)))


(def v325_l958 (kind/doc #'pj/membrane-mark-doc))


(def v326_l960 (pj/membrane-mark-doc :point))


(deftest t327_l962 (is ((fn [s] (string? s)) v326_l960)))


(def v329_l966 (kind/doc #'pj/save))


(def
 v331_l970
 (let
  [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg")))


(deftest t332_l976 (is (true? v331_l970)))


(def
 v334_l981
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
    (mapv (fn* [p1__86551#] (bit-and p1__86551# 255)) (vec bs))))))


(deftest
 t335_l990
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v334_l981)))


(def
 v337_l995
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
    (mapv (fn* [p1__86552#] (bit-and p1__86552# 255)) (vec bs))))))


(deftest t338_l1004 (is ((fn [bs] (= [137 80 78 71] bs)) v337_l995)))
