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
 v34_l94
 (pj/pose (rdatasets/datasets-iris) [:sepal-length :sepal-width]))


(deftest
 t35_l96
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v34_l94)))


(def
 v37_l106
 (def
  matrix-3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v38_l109 matrix-3)


(def v39_l111 (kind/pprint matrix-3))


(deftest
 t40_l113
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v39_l111)))


(def
 v42_l118
 (def
  matrix-mapped
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v43_l121 matrix-mapped)


(def v44_l123 (kind/pprint matrix-mapped))


(deftest
 t45_l125
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= #{:y :x} (get-in v [:opts :share-scales]))
     (=
      ["sepal-length" "sepal-width"]
      (get-in v [:grid-strip-labels :col-labels]))))
   v44_l123)))


(def
 v47_l132
 (def
  matrix-2x2
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width])))


(def v48_l137 matrix-2x2)


(def v49_l139 (kind/pprint matrix-2x2))


(deftest
 t50_l141
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v49_l139)))


(def
 v52_l147
 (def
  matrix-2x3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width :sepal-length]
   {:color :species})))


(def v53_l153 matrix-2x3)


(def v54_l155 (kind/pprint matrix-2x3))


(deftest
 t55_l157
 (is ((fn [v] (= 6 (:panels (pj/svg-summary v)))) v54_l155)))


(def
 v57_l161
 (def
  matrix-all
  (->
   (rdatasets/datasets-iris)
   (tc/select-columns [:sepal-length :sepal-width :petal-length])
   pj/cross-matrix)))


(def v58_l166 matrix-all)


(def v59_l168 (kind/pprint matrix-all))


(deftest
 t60_l170
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v59_l168)))


(def
 v62_l174
 (def
  matrix-points
  (->
   (rdatasets/datasets-iris)
   (pj/cross-matrix
    [:sepal-length :sepal-width]
    [:petal-length :petal-width])
   pj/lay-point)))


(def v63_l179 matrix-points)


(def v64_l181 (kind/pprint matrix-points))


(deftest
 t65_l183
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= [{:layer-type :point}] (:layers v))))
   v64_l181)))


(def
 v67_l190
 (def
  arranged-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v68_l193 arranged-columns)


(def v69_l195 (kind/pprint arranged-columns))


(deftest
 t70_l197
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v69_l195)))


(def
 v72_l201
 (def
  arranged-pairs
  (pj/arrange
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])))


(def v73_l205 arranged-pairs)


(def v74_l207 (kind/pprint arranged-pairs))


(deftest
 t75_l209
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v74_l207)))


(def
 v77_l213
 (def
  arranged-two-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length :petal-width]
   {:cols 2})))


(def v78_l218 arranged-two-columns)


(def v79_l220 (kind/pprint arranged-two-columns))


(deftest
 t80_l222
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v79_l220)))


(def
 v82_l226
 (def
  arranged-densities
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [:sepal-length :sepal-width :petal-length])
   (pj/lay-density {:color :species}))))


(def v83_l231 arranged-densities)


(def v84_l233 (kind/pprint arranged-densities))


(deftest
 t85_l235
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v84_l233)))


(def
 v87_l241
 (def
  arranged-from-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]]))))


(def v88_l247 arranged-from-pose)


(def v89_l249 (kind/pprint arranged-from-pose))


(deftest
 t90_l251
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 2, :points 300}
      (select-keys (pj/svg-summary v) [:panels :points]))
     (= {:color :species} (:mapping v))))
   v89_l249)))


(def
 v92_l257
 (def
  arranged-twice
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]])
   pj/lay-point)))


(def v93_l263 arranged-twice)


(def v94_l265 (kind/pprint arranged-twice))


(deftest
 t95_l267
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v94_l265)))


(def
 v97_l271
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]] {:cols 1}))
  (catch Exception e (ex-message e))))


(deftest
 t98_l277
 (is ((fn [s] (and (string? s) (re-find #"not used" s))) v97_l271)))


(def
 v100_l284
 (def
  sepal-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point)))


(def
 v101_l289
 (def
  mixed
  (pj/arrange
   (rdatasets/datasets-iris)
   [[sepal-scatter :sepal-length :sepal-width]
    [:petal-length :petal-width]
    [[:sepal-length :petal-length]]])))


(def v102_l295 mixed)


(def v103_l297 (kind/pprint mixed))


(deftest
 t104_l299
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v103_l297)))


(def
 v106_l303
 (def
  two-poses
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
    (pj/lay-histogram (rdatasets/datasets-iris) :petal-length)])))


(def v107_l307 two-poses)


(def v108_l309 (kind/pprint two-poses))


(deftest
 t109_l311
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v108_l309)))


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
     (= {:x [:sepal-length :sepal-width :petal-length]} (:mapping v))
     (let
      [l (-> v pj/draft :layers first)]
      (and
       (= [:value :series] [(:x l) (:color l)])
       (= 450 (tc/row-count (:data l)))))))
   v113_l324)))


(def
 v116_l335
 (def
  density-series-colored
  (pj/lay-density
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length]
   {:color :species})))


(def v117_l340 density-series-colored)


(def v118_l342 (kind/pprint density-series-colored))


(deftest
 t119_l344
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 1, :polygons 3}
      (select-keys (pj/svg-summary v) [:panels :polygons]))
     (= :species (-> v pj/plan :legend :title))))
   v118_l342)))


(def
 v121_l360
 (def
  where-layer-things-go
  (pj/lay-histogram
   (rdatasets/datasets-iris)
   :sepal-length
   {:bins 8, :alpha 0.5, :color :species, :position :stack})))


(def v122_l365 where-layer-things-go)


(def v123_l367 (kind/pprint where-layer-things-go))


(deftest
 t124_l369
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
   v123_l367)))


(def
 v125_l379
 (def
  where-stat-goes
  (pj/lay-smooth
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:stat :linear-model})))


(def v126_l382 where-stat-goes)


(def v127_l384 (kind/pprint where-stat-goes))


(deftest
 t128_l386
 (is
  ((fn [v] (= :linear-model (get-in v [:layers 0 :stat]))) v127_l384)))


(def
 v130_l390
 (def
  where-type-goes
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :species, :y :sepal-width, :x-type :categorical})
   pj/lay-boxplot)))


(def v131_l395 where-type-goes)


(def v132_l397 (kind/pprint where-type-goes))


(deftest
 t133_l399
 (is
  ((fn [v] (= :categorical (get-in v [:mapping :x-type]))) v132_l397)))


(def
 v135_l405
 (def
  where-scale-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/scale :x {:type :log}))))


(def v136_l410 where-scale-goes)


(def v137_l412 (kind/pprint where-scale-goes))


(deftest
 t138_l414
 (is
  ((fn
    [v]
    (and
     (= {:scale {:type :log}} (get-in v [:mapping :x]))
     (= :sepal-length (get-in v [:layers 0 :mapping :x]))
     (= :log (-> v pj/plan :panels first :x-scale :type))))
   v137_l412)))


(def
 v140_l420
 (def
  where-facet-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species))))


(def v141_l425 where-facet-goes)


(def v142_l427 (kind/pprint where-facet-goes))


(deftest
 t143_l429
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v142_l427)))


(def
 v145_l433
 (def
  where-options-go
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options
    {:title "Iris", :x-label "sepal length (cm)", :width 400})
   (pj/coord :flip))))


(def v146_l439 where-options-go)


(def v147_l441 (kind/pprint where-options-go))


(deftest
 t148_l443
 (is
  ((fn
    [v]
    (=
     {:title "Iris",
      :x-label "sepal length (cm)",
      :width 400,
      :coord :flip}
     (:opts v)))
   v147_l441)))


(def
 v150_l449
 (def
  where-composite-things-go
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   (pj/pose {:color :species})
   pj/lay-point)))


(def v151_l455 where-composite-things-go)


(def v152_l457 (kind/pprint where-composite-things-go))


(deftest
 t153_l459
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v152_l457)))


(def
 v155_l467
 (def
  bins-on-the-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :sepal-length, :bins 5})
   pj/lay-histogram)))


(def v156_l472 bins-on-the-pose)


(def v157_l474 (kind/pprint bins-on-the-pose))


(deftest
 t158_l476
 (is
  ((fn
    [v]
    (and
     (not (contains? (:mapping v) :bins))
     (= 9 (:polygons (pj/svg-summary v)))))
   v157_l474)))


(def
 v160_l481
 (def
  label-on-a-layer
  (pj/lay-point
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:x-label "D"})))


(def v161_l484 label-on-a-layer)


(def v162_l486 (kind/pprint label-on-a-layer))


(deftest
 t163_l488
 (is ((fn [v] (not (contains? (:opts v) :x-label))) v162_l486)))


(def
 v165_l492
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   {:x {:column :sepal-length, :label "SL"}, :y :sepal-width})
  (catch Exception e (ex-message e))))


(deftest
 t166_l497
 (is
  ((fn [s] (and (string? s) (re-find #"unexpected key" s))) v165_l492)))


(def
 v168_l508
 (comment
  {:data iris,
   :mapping
   {:x {:column :sepal-length, :label "sepal length (cm)"}, :bins 8},
   :layers
   [{:layer-type :histogram,
     :mapping {:color :species, :position :stack}}],
   :opts {:title "Iris", :width 400}}))
