(ns
 pr61-examples-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l14
 (def
  iris3
  (tc/select-columns
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :species])))


(def v4_l19 (def inferred (pj/pose iris3)))


(def v5_l21 inferred)


(def v6_l23 (kind/pprint inferred))


(deftest
 t7_l25
 (is
  ((fn
    [v]
    (and
     (nil? (:mapping v))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping (pj/infer-mapping v)))))
   v6_l23)))


(def
 v8_l29
 (def one-column (pj/pose (rdatasets/datasets-iris) :sepal-length)))


(def v9_l31 one-column)


(def v10_l33 (kind/pprint one-column))


(deftest
 t11_l35
 (is ((fn [v] (= {:x :sepal-length} (:mapping v))) v10_l33)))


(def
 v12_l37
 (def
  one-column-mapped
  (pj/pose (rdatasets/datasets-iris) :sepal-length {:color :species})))


(def v13_l39 one-column-mapped)


(def v14_l41 (kind/pprint one-column-mapped))


(deftest
 t15_l43
 (is
  ((fn [v] (= {:x :sepal-length, :color :species} (:mapping v)))
   v14_l41)))


(def
 v16_l45
 (def
  two-columns
  (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)))


(def v17_l47 two-columns)


(def v18_l49 (kind/pprint two-columns))


(deftest
 t19_l51
 (is
  ((fn [v] (= {:x :sepal-length, :y :sepal-width} (:mapping v)))
   v18_l49)))


(def
 v20_l53
 (def
  two-columns-mapped
  (pj/pose
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:color :species})))


(def v21_l56 two-columns-mapped)


(def v22_l58 (kind/pprint two-columns-mapped))


(deftest
 t23_l60
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v22_l58)))


(def
 v24_l63
 (def
  mapping-only
  (pj/pose
   (rdatasets/datasets-iris)
   {:x :sepal-length, :y :sepal-width, :color :species})))


(def v25_l66 mapping-only)


(def v26_l68 (kind/pprint mapping-only))


(deftest
 t27_l70
 (is
  ((fn
    [v]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping v)))
   v26_l68)))


(def
 v29_l75
 (pj/pose (rdatasets/datasets-iris) [:sepal-length :sepal-width]))


(deftest
 t30_l77
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v29_l75)))


(def
 v32_l83
 (def
  matrix-3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v33_l86 matrix-3)


(def v34_l88 (kind/pprint matrix-3))


(deftest
 t35_l90
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v34_l88)))


(def
 v36_l92
 (def
  matrix-mapped
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v37_l95 matrix-mapped)


(def v38_l97 (kind/pprint matrix-mapped))


(deftest
 t39_l99
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= #{:y :x} (get-in v [:opts :share-scales]))
     (=
      ["sepal-length" "sepal-width"]
      (get-in v [:grid-strip-labels :col-labels]))))
   v38_l97)))


(def
 v40_l104
 (def
  matrix-2x2
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width])))


(def v41_l109 matrix-2x2)


(def v42_l111 (kind/pprint matrix-2x2))


(deftest
 t43_l113
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v42_l111)))


(def
 v44_l115
 (def
  matrix-2x3
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   [:petal-length :petal-width :sepal-length]
   {:color :species})))


(def v45_l121 matrix-2x3)


(def v46_l123 (kind/pprint matrix-2x3))


(deftest
 t47_l125
 (is ((fn [v] (= 6 (:panels (pj/svg-summary v)))) v46_l123)))


(def
 v48_l127
 (def
  matrix-all
  (->
   (rdatasets/datasets-iris)
   (tc/select-columns [:sepal-length :sepal-width :petal-length])
   pj/cross-matrix)))


(def v49_l132 matrix-all)


(def v50_l134 (kind/pprint matrix-all))


(deftest
 t51_l136
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v50_l134)))


(def
 v52_l138
 (def
  matrix-points
  (->
   (rdatasets/datasets-iris)
   (pj/cross-matrix
    [:sepal-length :sepal-width]
    [:petal-length :petal-width])
   pj/lay-point)))


(def v53_l143 matrix-points)


(def v54_l145 (kind/pprint matrix-points))


(deftest
 t55_l147
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= [{:layer-type :point}] (:layers v))))
   v54_l145)))


(def
 v57_l152
 (def
  arranged-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v58_l155 arranged-columns)


(def v59_l157 (kind/pprint arranged-columns))


(deftest
 t60_l159
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v59_l157)))


(def
 v61_l161
 (def
  arranged-pairs
  (pj/arrange
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])))


(def v62_l165 arranged-pairs)


(def v63_l167 (kind/pprint arranged-pairs))


(deftest
 t64_l169
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v63_l167)))


(def
 v65_l171
 (def
  arranged-two-columns
  (pj/arrange
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length :petal-width]
   {:cols 2})))


(def v66_l176 arranged-two-columns)


(def v67_l178 (kind/pprint arranged-two-columns))


(deftest
 t68_l180
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v67_l178)))


(def
 v69_l182
 (def
  arranged-densities
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [:sepal-length :sepal-width :petal-length])
   (pj/lay-density {:color :species}))))


(def v70_l187 arranged-densities)


(def v71_l189 (kind/pprint arranged-densities))


(deftest
 t72_l191
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v71_l189)))


(def
 v74_l195
 (def
  arranged-from-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]]))))


(def v75_l201 arranged-from-pose)


(def v76_l203 (kind/pprint arranged-from-pose))


(deftest
 t77_l205
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 2, :points 300}
      (select-keys (pj/svg-summary v) [:panels :points]))
     (= {:color :species} (:mapping v))))
   v76_l203)))


(def
 v78_l209
 (def
  arranged-twice
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]])
   pj/lay-point)))


(def v79_l215 arranged-twice)


(def v80_l217 (kind/pprint arranged-twice))


(deftest
 t81_l219
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v80_l217)))


(def
 v82_l221
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/arrange [[:sepal-length :sepal-width]])
   (pj/arrange [[:petal-length :petal-width]] {:cols 1}))
  (catch Exception e (ex-message e))))


(deftest
 t83_l227
 (is ((fn [s] (and (string? s) (re-find #"not used" s))) v82_l221)))


(def
 v85_l231
 (def
  sepal-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:color :species})
   pj/lay-point)))


(def
 v86_l236
 (def
  mixed
  (pj/arrange
   (rdatasets/datasets-iris)
   [[sepal-scatter :sepal-length :sepal-width]
    [:petal-length :petal-width]
    [[:sepal-length :petal-length]]])))


(def v87_l242 mixed)


(def v88_l244 (kind/pprint mixed))


(deftest
 t89_l246
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v88_l244)))


(def
 v90_l248
 (def
  two-poses
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
    (pj/lay-histogram (rdatasets/datasets-iris) :petal-length)])))


(def v91_l252 two-poses)


(def v92_l254 (kind/pprint two-poses))


(deftest
 t93_l256
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v92_l254)))


(def
 v95_l260
 (def
  density-panels
  (pj/lay-density
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width :petal-length])))


(def v96_l263 density-panels)


(deftest
 t97_l265
 (is
  ((fn
    [v]
    (and
     (= 3 (:panels (pj/svg-summary v)))
     (re-find
      #"next release"
      (with-out-str
       (pj/lay-density
        (rdatasets/datasets-iris)
        [:sepal-length :sepal-width :petal-length])))))
   v96_l263)))


(def
 v98_l271
 (def
  density-series
  (pj/lay-density
   (rdatasets/datasets-iris)
   {:x [:sepal-length :sepal-width :petal-length]})))


(def v99_l274 density-series)


(def v100_l276 (kind/pprint density-series))


(deftest
 t101_l278
 (is
  ((fn
    [v]
    (and
     (= 1 (:panels (pj/svg-summary v)))
     (=
      [:sepal-length :sepal-width :petal-length]
      (-> v :layers first :mapping :x))
     (let
      [l (-> v pj/draft :layers first)]
      (and
       (= [:value :series] [(:x l) (:color l)])
       (= 450 (tc/row-count (:data l)))))))
   v100_l276)))


(def
 v102_l285
 (def
  density-series-colored
  (pj/lay-density
   (rdatasets/datasets-iris)
   {:x [:sepal-length :sepal-width :petal-length], :color :species})))


(def v103_l290 density-series-colored)


(def v104_l292 (kind/pprint density-series-colored))


(deftest
 t105_l294
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 1, :polygons 9}
      (select-keys (pj/svg-summary v) [:panels :polygons]))
     (= :species (-> v pj/plan :legend :title))))
   v104_l292)))


(def
 v107_l300
 (def
  where-layer-things-go
  (pj/lay-histogram
   (rdatasets/datasets-iris)
   :sepal-length
   {:bins 8, :alpha 0.5, :color :species, :position :stack})))


(def v108_l305 where-layer-things-go)


(def v109_l307 (kind/pprint where-layer-things-go))


(deftest
 t110_l309
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
   v109_l307)))


(def
 v111_l319
 (def
  where-stat-goes
  (pj/lay-smooth
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:stat :linear-model})))


(def v112_l322 where-stat-goes)


(def v113_l324 (kind/pprint where-stat-goes))


(deftest
 t114_l326
 (is
  ((fn [v] (= :linear-model (get-in v [:layers 0 :stat]))) v113_l324)))


(def
 v115_l328
 (def
  where-type-goes
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :species, :y :sepal-width, :x-type :categorical})
   pj/lay-boxplot)))


(def v116_l333 where-type-goes)


(def v117_l335 (kind/pprint where-type-goes))


(deftest
 t118_l337
 (is
  ((fn [v] (= :categorical (get-in v [:mapping :x-type]))) v117_l335)))


(def
 v119_l339
 (def
  where-scale-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/scale :x {:type :log}))))


(def v120_l344 where-scale-goes)


(def v121_l346 (kind/pprint where-scale-goes))


(deftest
 t122_l348
 (is
  ((fn
    [v]
    (and
     (= {:scale {:type :log}} (get-in v [:mapping :x]))
     (= :sepal-length (get-in v [:layers 0 :mapping :x]))
     (= :log (-> v pj/plan :panels first :x-scale :type))))
   v121_l346)))


(def
 v123_l352
 (def
  where-facet-goes
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species))))


(def v124_l357 where-facet-goes)


(def v125_l359 (kind/pprint where-facet-goes))


(deftest
 t126_l361
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v125_l359)))


(def
 v127_l363
 (def
  where-options-go
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/options
    {:title "Iris", :x-label "sepal length (cm)", :width 400})
   (pj/coord :flip))))


(def v128_l369 where-options-go)


(def v129_l371 (kind/pprint where-options-go))


(deftest
 t130_l373
 (is
  ((fn
    [v]
    (=
     {:title "Iris",
      :x-label "sepal length (cm)",
      :width 400,
      :coord :flip}
     (:opts v)))
   v129_l371)))


(def
 v131_l376
 (def
  where-composite-things-go
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   (pj/pose {:color :species})
   pj/lay-point)))


(def v132_l382 where-composite-things-go)


(def v133_l384 (kind/pprint where-composite-things-go))


(deftest
 t134_l386
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v133_l384)))


(def
 v136_l391
 (def
  bins-on-the-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose {:x :sepal-length, :bins 5})
   pj/lay-histogram)))


(def v137_l396 bins-on-the-pose)


(def v138_l398 (kind/pprint bins-on-the-pose))


(deftest
 t139_l400
 (is
  ((fn
    [v]
    (and
     (not (contains? (:mapping v) :bins))
     (= 9 (:polygons (pj/svg-summary v)))))
   v138_l398)))


(def
 v140_l403
 (def
  label-on-a-layer
  (pj/lay-point
   (rdatasets/datasets-iris)
   :sepal-length
   :sepal-width
   {:x-label "D"})))


(def v141_l406 label-on-a-layer)


(def v142_l408 (kind/pprint label-on-a-layer))


(deftest
 t143_l410
 (is ((fn [v] (not (contains? (:opts v) :x-label))) v142_l408)))


(def
 v144_l412
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   {:x {:column :sepal-length, :label "SL"}, :y :sepal-width})
  (catch Exception e (ex-message e))))


(deftest
 t145_l417
 (is
  ((fn [s] (and (string? s) (re-find #"unexpected key" s))) v144_l412)))


(def
 v147_l423
 (comment
  {:data iris,
   :mapping
   {:x {:column :sepal-length, :label "sepal length (cm)"}, :bins 8},
   :layers
   [{:layer-type :histogram,
     :mapping {:color :species, :position :stack}}],
   :opts {:title "Iris", :width 400}}))
