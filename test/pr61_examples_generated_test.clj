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


(def v4_l24 (def inferred (pj/pose iris3)))


(def v5_l26 inferred)


(def v6_l28 (kind/pprint inferred))


(deftest
 t7_l30
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v6_l28)))


(def
 v9_l35
 (def one-column (pj/pose (rdatasets/datasets-iris) :sepal-length)))


(def v10_l37 one-column)


(def v11_l39 (kind/pprint one-column))


(deftest
 t12_l41
 (is ((fn [v] (= {:x :sepal-length} (:mapping v))) v11_l39)))


(def
 v14_l45
 (def
  one-column-mapped
  (pj/pose (rdatasets/datasets-iris) :sepal-length {:color :species})))


(def v15_l47 one-column-mapped)


(def v16_l49 (kind/pprint one-column-mapped))


(deftest
 t17_l51
 (is
  ((fn [v] (= {:x :sepal-length, :color :species} (:mapping v)))
   v16_l49)))


(def
 v19_l55
 (def
  two-columns
  (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)))


(def v20_l57 two-columns)


(def v21_l59 (kind/pprint two-columns))


(deftest
 t22_l61
 (is
  ((fn [v] (= {:x :sepal-length, :y :sepal-width} (:mapping v)))
   v21_l59)))


(def
 v24_l65
 (def
  two-columns-mapped
  (pj/pose
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:color :species})))


(def v25_l68 two-columns-mapped)


(def v26_l70 (kind/pprint two-columns-mapped))


(deftest
 t27_l72
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v26_l70)))


(def
 v29_l77
 (def
  mapping-only
  (pj/pose
   (rdatasets/datasets-iris)
   {:x :sepal-length, :y :sepal-width, :color :species})))


(def v30_l80 mapping-only)


(def v31_l82 (kind/pprint mapping-only))


(deftest
 t32_l84
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v31_l82)))


(def
 v34_l91
 (try
  (pj/plot
   (pj/pose (rdatasets/datasets-iris) [:sepal-length :sepal-width]))
  (catch Exception e (ex-message e))))


(deftest
 t35_l95
 (is ((fn [s] (and (string? s) (re-find #"as a series" s))) v34_l91)))


(def
 v37_l105
 (def
  matrix-3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v38_l108 matrix-3)


(def v39_l110 (kind/pprint matrix-3))


(deftest
 t40_l112
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v39_l110)))


(def
 v42_l117
 (def
  matrix-mapped
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v43_l120 matrix-mapped)


(def v44_l122 (kind/pprint matrix-mapped))


(deftest
 t45_l124
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= #{:y :x} (get-in v [:opts :share-scales]))
     (=
      ["sepal-length" "sepal-width"]
      (get-in v [:grid-strip-labels :col-labels]))))
   v44_l122)))


(def
 v47_l131
 (def
  matrix-2x2
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width])))


(def v48_l136 matrix-2x2)


(def v49_l138 (kind/pprint matrix-2x2))


(deftest
 t50_l140
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v49_l138)))


(def
 v52_l146
 (def
  matrix-2x3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width :sepal-length]
   {:color :species})))


(def v53_l152 matrix-2x3)


(def v54_l154 (kind/pprint matrix-2x3))


(deftest
 t55_l156
 (is ((fn [v] (= 6 (:panels (pj/svg-summary v)))) v54_l154)))


(def
 v57_l160
 (def
  matrix-all
  (->
   (rdatasets/datasets-iris)
   (tc/select-columns [:sepal-length :sepal-width :petal-length])
   pj/cross-matrix)))


(def v58_l165 matrix-all)


(def v59_l167 (kind/pprint matrix-all))


(deftest
 t60_l169
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v59_l167)))


(def
 v62_l173
 (def
  matrix-points
  (->
   (rdatasets/datasets-iris)
   (pj/cross-matrix
    [:sepal-length :sepal-width]
    [:petal-length :petal-width])
   pj/lay-point)))


(def v63_l178 matrix-points)


(def v64_l180 (kind/pprint matrix-points))


(deftest
 t65_l182
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= [{:layer-type :point}] (:layers v))))
   v64_l180)))


(def
 v67_l189
 (def
  arranged-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v68_l192 arranged-columns)


(def v69_l194 (kind/pprint arranged-columns))


(deftest
 t70_l196
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v69_l194)))


(def
 v72_l200
 (def
  arranged-pairs
  (pj/arrange
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])))


(def v73_l204 arranged-pairs)


(def v74_l206 (kind/pprint arranged-pairs))


(deftest
 t75_l208
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v74_l206)))


(def
 v77_l212
 (def
  arranged-two-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length :petal-width]
   {:cols 2})))


(def v78_l217 arranged-two-columns)


(def v79_l219 (kind/pprint arranged-two-columns))


(deftest
 t80_l221
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v79_l219)))


(def
 v82_l225
 (def
  arranged-densities
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [:sepal-length :sepal-width :petal-length])
   (pj/lay-density {:color :species}))))


(def v83_l230 arranged-densities)


(def v84_l232 (kind/pprint arranged-densities))


(deftest
 t85_l234
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v84_l232)))


(def
 v87_l240
 (def
  arranged-from-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]]))))


(def v88_l246 arranged-from-pose)


(def v89_l248 (kind/pprint arranged-from-pose))


(deftest
 t90_l250
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 2, :points 300}
      (select-keys (pj/svg-summary v) [:panels :points]))
     (= {:color :species} (:mapping v))))
   v89_l248)))


(def
 v92_l256
 (def
  arranged-twice
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]])
   pj/lay-point)))


(def v93_l262 arranged-twice)


(def v94_l264 (kind/pprint arranged-twice))


(deftest
 t95_l266
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v94_l264)))


(def
 v97_l270
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]] {:cols 1}))
  (catch Exception e (ex-message e))))


(deftest
 t98_l276
 (is ((fn [s] (and (string? s) (re-find #"not used" s))) v97_l270)))


(def
 v100_l283
 (def
  sepal-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point)))


(def
 v101_l288
 (def
  mixed
  (pj/arrange
   (rdatasets/datasets-iris)
   [[sepal-scatter :sepal-length :sepal-width]
    [:petal-length :petal-width]
    [[:sepal-length :petal-length]]])))


(def v102_l294 mixed)


(def v103_l296 (kind/pprint mixed))


(deftest
 t104_l298
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v103_l296)))


(def
 v106_l302
 (def
  two-poses
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
    (pj/lay-histogram (rdatasets/datasets-iris) :petal-length)])))


(def v107_l306 two-poses)


(def v108_l308 (kind/pprint two-poses))


(deftest
 t109_l310
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v108_l308)))


(def
 v111_l317
 (def
  density-series
  (pj/lay-density
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v112_l320 density-series)


(def v113_l322 (kind/pprint density-series))


(deftest
 t114_l324
 (is
  ((fn
    [v]
    (and
     (= 1 (:panels (pj/svg-summary v)))
     (= {:x :value, :color :series} (:mapping v))
     (= 450 (tc/row-count (:data v)))))
   v113_l322)))


(def
 v116_l330
 (def
  density-series-colored
  (pj/lay-density
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length]
   {:color :species})))


(def v117_l335 density-series-colored)


(def v118_l337 (kind/pprint density-series-colored))


(deftest
 t119_l339
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 1, :polygons 3}
      (select-keys (pj/svg-summary v) [:panels :polygons]))
     (= :species (-> v pj/plan :legend :title))))
   v118_l337)))


(def
 v121_l354
 (def
  where-layer-things-go
  (pj/lay-histogram
   (rdatasets/datasets-iris)
   :sepal-length
   {:bins 8, :alpha 0.5, :color :species, :position :stack})))


(def v122_l359 where-layer-things-go)


(def v123_l361 (kind/pprint where-layer-things-go))


(deftest
 t124_l363
 (is
  ((fn
    [v]
    (and
     (= {:x :sepal-length} (:mapping v))
     (=
      {:bins 8, :alpha 0.5, :color :species}
      (get-in v [:layers 0 :mapping]))
     (= :stack (get-in v [:layers 0 :position]))
     (=
      (pj/plot v)
      (pj/plot
       (pj/lay-histogram
        (rdatasets/datasets-iris)
        :sepal-length
        {:bins 8, :alpha 0.5, :color :species})))))
   v123_l361)))


(def
 v125_l373
 (def
  where-stat-goes
  (pj/lay-smooth
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:stat :linear-model})))


(def v126_l376 where-stat-goes)


(def v127_l378 (kind/pprint where-stat-goes))


(deftest
 t128_l380
 (is
  ((fn [v] (= :linear-model (get-in v [:layers 0 :stat]))) v127_l378)))


(def
 v130_l384
 (def
  where-type-goes
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :species, :y :sepal-width, :x-type :categorical})
   pj/lay-boxplot)))


(def v131_l389 where-type-goes)


(def v132_l391 (kind/pprint where-type-goes))


(deftest
 t133_l393
 (is
  ((fn [v] (= :categorical (get-in v [:mapping :x-type]))) v132_l391)))


(def
 v135_l397
 (def
  where-scale-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/scale :x {:type :log}))))


(def v136_l402 where-scale-goes)


(def v137_l404 (kind/pprint where-scale-goes))


(deftest
 t138_l406
 (is
  ((fn
    [v]
    (=
     {:from :sepal-length, :scale {:type :log}}
     (get-in v [:mapping :x])))
   v137_l404)))


(def
 v140_l411
 (def
  where-facet-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species))))


(def v141_l416 where-facet-goes)


(def v142_l418 (kind/pprint where-facet-goes))


(deftest
 t143_l420
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v142_l418)))


(def
 v145_l424
 (def
  where-options-go
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options
    {:title "Iris", :x-label "sepal length (cm)", :width 400})
   (pj/coord :flip))))


(def v146_l430 where-options-go)


(def v147_l432 (kind/pprint where-options-go))


(deftest
 t148_l434
 (is
  ((fn
    [v]
    (=
     {:title "Iris",
      :x-label "sepal length (cm)",
      :width 400,
      :coord :flip}
     (:opts v)))
   v147_l432)))


(def
 v150_l440
 (def
  where-composite-things-go
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   (pj/pose {:color :species})
   pj/lay-point)))


(def v151_l446 where-composite-things-go)


(def v152_l448 (kind/pprint where-composite-things-go))


(deftest
 t153_l450
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v152_l448)))


(def
 v155_l458
 (def
  bins-on-the-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :sepal-length, :bins 5})
   pj/lay-histogram)))


(def v156_l463 bins-on-the-pose)


(def v157_l465 (kind/pprint bins-on-the-pose))


(deftest
 t158_l467
 (is
  ((fn
    [v]
    (and
     (not (contains? (:mapping v) :bins))
     (= 9 (:polygons (pj/svg-summary v)))))
   v157_l465)))


(def
 v160_l472
 (def
  label-on-a-layer
  (pj/lay-point
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:x-label "D"})))


(def v161_l475 label-on-a-layer)


(def v162_l477 (kind/pprint label-on-a-layer))


(deftest
 t163_l479
 (is ((fn [v] (not (contains? (:opts v) :x-label))) v162_l477)))


(def
 v165_l483
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   {:x {:column :sepal-length, :label "SL"}, :y :sepal-width})
  (catch Exception e (ex-message e))))


(deftest
 t166_l488
 (is
  ((fn [s] (and (string? s) (re-find #"unexpected key" s))) v165_l483)))


(def
 v168_l499
 (comment
  {:data iris,
   :mapping
   {:x {:column :sepal-length, :label "sepal length (cm)"}, :bins 8},
   :layers
   [{:layer-type :histogram,
     :mapping {:color :species, :position :stack}}],
   :opts {:title "Iris", :width 400}}))
