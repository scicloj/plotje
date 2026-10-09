(ns
 pr61-examples-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l17
 (def
  iris3
  (tc/select-columns
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :species])))


(def v4_l25 (def inferred (pj/pose iris3)))


(def v5_l27 inferred)


(def v6_l29 (kind/pprint inferred))


(deftest
 t7_l31
 (is
  ((fn
    [v]
    (and
     (nil? (:mapping v))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping (pj/infer-mapping v)))))
   v6_l29)))


(def
 v9_l37
 (def one-column (pj/pose (rdatasets/datasets-iris) :sepal-length)))


(def v10_l39 one-column)


(def v11_l41 (kind/pprint one-column))


(deftest
 t12_l43
 (is ((fn [v] (= {:x :sepal-length} (:mapping v))) v11_l41)))


(def
 v14_l47
 (def
  one-column-mapped
  (pj/pose (rdatasets/datasets-iris) :sepal-length {:color :species})))


(def v15_l49 one-column-mapped)


(def v16_l51 (kind/pprint one-column-mapped))


(deftest
 t17_l53
 (is
  ((fn [v] (= {:x :sepal-length, :color :species} (:mapping v)))
   v16_l51)))


(def
 v19_l57
 (def
  two-columns
  (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)))


(def v20_l59 two-columns)


(def v21_l61 (kind/pprint two-columns))


(deftest
 t22_l63
 (is
  ((fn [v] (= {:x :sepal-length, :y :sepal-width} (:mapping v)))
   v21_l61)))


(def
 v24_l67
 (def
  two-columns-mapped
  (pj/pose
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:color :species})))


(def v25_l70 two-columns-mapped)


(def v26_l72 (kind/pprint two-columns-mapped))


(deftest
 t27_l74
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v26_l72)))


(def
 v29_l79
 (def
  mapping-only
  (pj/pose
   (rdatasets/datasets-iris)
   {:x :sepal-length, :y :sepal-width, :color :species})))


(def v30_l82 mapping-only)


(def v31_l84 (kind/pprint mapping-only))


(deftest
 t32_l86
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v31_l84)))


(def
 v34_l93
 (try
  (pj/plot
   (pj/pose (rdatasets/datasets-iris) [:sepal-length :sepal-width]))
  (catch Exception e (ex-message e))))


(deftest
 t35_l97
 (is ((fn [s] (and (string? s) (re-find #"as a series" s))) v34_l93)))


(def
 v37_l107
 (def
  matrix-3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v38_l110 matrix-3)


(def v39_l112 (kind/pprint matrix-3))


(deftest
 t40_l114
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v39_l112)))


(def
 v42_l119
 (def
  matrix-mapped
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v43_l122 matrix-mapped)


(def v44_l124 (kind/pprint matrix-mapped))


(deftest
 t45_l126
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= #{:y :x} (get-in v [:opts :share-scales]))
     (=
      ["sepal-length" "sepal-width"]
      (get-in v [:grid-strip-labels :col-labels]))))
   v44_l124)))


(def
 v47_l133
 (def
  matrix-2x2
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width])))


(def v48_l138 matrix-2x2)


(def v49_l140 (kind/pprint matrix-2x2))


(deftest
 t50_l142
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v49_l140)))


(def
 v52_l148
 (def
  matrix-2x3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width :sepal-length]
   {:color :species})))


(def v53_l154 matrix-2x3)


(def v54_l156 (kind/pprint matrix-2x3))


(deftest
 t55_l158
 (is ((fn [v] (= 6 (:panels (pj/svg-summary v)))) v54_l156)))


(def
 v57_l162
 (def
  matrix-all
  (->
   (rdatasets/datasets-iris)
   (tc/select-columns [:sepal-length :sepal-width :petal-length])
   pj/cross-matrix)))


(def v58_l167 matrix-all)


(def v59_l169 (kind/pprint matrix-all))


(deftest
 t60_l171
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v59_l169)))


(def
 v62_l175
 (def
  matrix-points
  (->
   (rdatasets/datasets-iris)
   (pj/cross-matrix
    [:sepal-length :sepal-width]
    [:petal-length :petal-width])
   pj/lay-point)))


(def v63_l180 matrix-points)


(def v64_l182 (kind/pprint matrix-points))


(deftest
 t65_l184
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= [{:layer-type :point}] (:layers v))))
   v64_l182)))


(def
 v67_l191
 (def
  arranged-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v68_l194 arranged-columns)


(def v69_l196 (kind/pprint arranged-columns))


(deftest
 t70_l198
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v69_l196)))


(def
 v72_l202
 (def
  arranged-pairs
  (pj/arrange
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])))


(def v73_l206 arranged-pairs)


(def v74_l208 (kind/pprint arranged-pairs))


(deftest
 t75_l210
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v74_l208)))


(def
 v77_l214
 (def
  arranged-two-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length :petal-width]
   {:cols 2})))


(def v78_l219 arranged-two-columns)


(def v79_l221 (kind/pprint arranged-two-columns))


(deftest
 t80_l223
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v79_l221)))


(def
 v82_l227
 (def
  arranged-densities
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [:sepal-length :sepal-width :petal-length])
   (pj/lay-density {:color :species}))))


(def v83_l232 arranged-densities)


(def v84_l234 (kind/pprint arranged-densities))


(deftest
 t85_l236
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v84_l234)))


(def
 v87_l242
 (def
  arranged-from-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]]))))


(def v88_l248 arranged-from-pose)


(def v89_l250 (kind/pprint arranged-from-pose))


(deftest
 t90_l252
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 2, :points 300}
      (select-keys (pj/svg-summary v) [:panels :points]))
     (= {:color :species} (:mapping v))))
   v89_l250)))


(def
 v92_l258
 (def
  arranged-twice
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]])
   pj/lay-point)))


(def v93_l264 arranged-twice)


(def v94_l266 (kind/pprint arranged-twice))


(deftest
 t95_l268
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v94_l266)))


(def
 v97_l272
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]] {:cols 1}))
  (catch Exception e (ex-message e))))


(deftest
 t98_l278
 (is ((fn [s] (and (string? s) (re-find #"not used" s))) v97_l272)))


(def
 v100_l285
 (def
  sepal-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point)))


(def
 v101_l290
 (def
  mixed
  (pj/arrange
   (rdatasets/datasets-iris)
   [[sepal-scatter :sepal-length :sepal-width]
    [:petal-length :petal-width]
    [[:sepal-length :petal-length]]])))


(def v102_l296 mixed)


(def v103_l298 (kind/pprint mixed))


(deftest
 t104_l300
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v103_l298)))


(def
 v106_l304
 (def
  two-poses
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
    (pj/lay-histogram (rdatasets/datasets-iris) :petal-length)])))


(def v107_l308 two-poses)


(def v108_l310 (kind/pprint two-poses))


(deftest
 t109_l312
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v108_l310)))


(def
 v111_l319
 (def
  density-series
  (pj/lay-density
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v112_l322 density-series)


(def v113_l324 (kind/pprint density-series))


(deftest
 t114_l326
 (is
  ((fn
    [v]
    (and
     (= 1 (:panels (pj/svg-summary v)))
     (= {:x :value, :color :series} (:mapping v))
     (= 450 (tc/row-count (:data v)))))
   v113_l324)))


(def
 v116_l332
 (def
  density-series-colored
  (pj/lay-density
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length]
   {:color :species})))


(def v117_l337 density-series-colored)


(def v118_l339 (kind/pprint density-series-colored))


(deftest
 t119_l341
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 1, :polygons 3}
      (select-keys (pj/svg-summary v) [:panels :polygons]))
     (= :species (-> v pj/plan :legend :title))))
   v118_l339)))


(def
 v121_l357
 (def
  where-layer-things-go
  (pj/lay-histogram
   (rdatasets/datasets-iris)
   :sepal-length
   {:bins 8, :alpha 0.5, :color :species, :position :stack})))


(def v122_l362 where-layer-things-go)


(def v123_l364 (kind/pprint where-layer-things-go))


(deftest
 t124_l366
 (is
  ((fn
    [v]
    (and
     (nil? (:mapping v))
     (=
      {:x :sepal-length, :bins 8, :alpha 0.5, :color :species}
      (get-in v [:layers 0 :mapping]))
     (= :stack (get-in v [:layers 0 :position]))
     (=
      (pj/plot v)
      (pj/plot
       (pj/lay-histogram
        (rdatasets/datasets-iris)
        :sepal-length
        {:bins 8, :alpha 0.5, :color :species})))))
   v123_l364)))


(def
 v125_l376
 (def
  where-stat-goes
  (pj/lay-smooth
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:stat :linear-model})))


(def v126_l379 where-stat-goes)


(def v127_l381 (kind/pprint where-stat-goes))


(deftest
 t128_l383
 (is
  ((fn [v] (= :linear-model (get-in v [:layers 0 :stat]))) v127_l381)))


(def
 v130_l387
 (def
  where-type-goes
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :species, :y :sepal-width, :x-type :categorical})
   pj/lay-boxplot)))


(def v131_l392 where-type-goes)


(def v132_l394 (kind/pprint where-type-goes))


(deftest
 t133_l396
 (is
  ((fn [v] (= :categorical (get-in v [:mapping :x-type]))) v132_l394)))


(def
 v135_l402
 (def
  where-scale-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/scale :x {:type :log}))))


(def v136_l407 where-scale-goes)


(def v137_l409 (kind/pprint where-scale-goes))


(deftest
 t138_l411
 (is
  ((fn
    [v]
    (and
     (= {:scale {:type :log}} (get-in v [:mapping :x]))
     (= :sepal-length (get-in v [:layers 0 :mapping :x]))
     (= :log (-> v pj/plan :panels first :x-scale :type))))
   v137_l409)))


(def
 v140_l417
 (def
  where-facet-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species))))


(def v141_l422 where-facet-goes)


(def v142_l424 (kind/pprint where-facet-goes))


(deftest
 t143_l426
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v142_l424)))


(def
 v145_l430
 (def
  where-options-go
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options
    {:title "Iris", :x-label "sepal length (cm)", :width 400})
   (pj/coord :flip))))


(def v146_l436 where-options-go)


(def v147_l438 (kind/pprint where-options-go))


(deftest
 t148_l440
 (is
  ((fn
    [v]
    (=
     {:title "Iris",
      :x-label "sepal length (cm)",
      :width 400,
      :coord :flip}
     (:opts v)))
   v147_l438)))


(def
 v150_l446
 (def
  where-composite-things-go
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   (pj/pose {:color :species})
   pj/lay-point)))


(def v151_l452 where-composite-things-go)


(def v152_l454 (kind/pprint where-composite-things-go))


(deftest
 t153_l456
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v152_l454)))


(def
 v155_l464
 (def
  bins-on-the-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :sepal-length, :bins 5})
   pj/lay-histogram)))


(def v156_l469 bins-on-the-pose)


(def v157_l471 (kind/pprint bins-on-the-pose))


(deftest
 t158_l473
 (is
  ((fn
    [v]
    (and
     (not (contains? (:mapping v) :bins))
     (= 9 (:polygons (pj/svg-summary v)))))
   v157_l471)))


(def
 v160_l478
 (def
  label-on-a-layer
  (pj/lay-point
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:x-label "D"})))


(def v161_l481 label-on-a-layer)


(def v162_l483 (kind/pprint label-on-a-layer))


(deftest
 t163_l485
 (is ((fn [v] (not (contains? (:opts v) :x-label))) v162_l483)))


(def
 v165_l489
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   {:x {:column :sepal-length, :label "SL"}, :y :sepal-width})
  (catch Exception e (ex-message e))))


(deftest
 t166_l494
 (is
  ((fn [s] (and (string? s) (re-find #"unexpected key" s))) v165_l489)))


(def
 v168_l505
 (comment
  {:data iris,
   :mapping
   {:x {:column :sepal-length, :label "sepal length (cm)"}, :bins 8},
   :layers
   [{:layer-type :histogram,
     :mapping {:color :species, :position :stack}}],
   :opts {:title "Iris", :width 400}}))
