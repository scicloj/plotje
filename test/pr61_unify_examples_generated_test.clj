(ns
 pr61-unify-examples-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l23
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v4_l29
 (def
  moved
  (->
   (trend-plot (rdatasets/datasets-iris))
   (pj/pose :petal-length :petal-width))))


(def v5_l33 moved)


(def v6_l35 (kind/pprint moved))


(deftest
 t7_l37
 (is
  ((fn
    [v]
    (and
     (not (contains? v :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping v))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers v))))
   v6_l35)))


(def
 v9_l44
 (def
  own-columns
  (->
   (rdatasets/datasets-iris)
   pj/pose
   (pj/lay-point {:x :sepal-length, :y :sepal-width})
   (pj/pose :petal-length :petal-width))))


(def v10_l50 own-columns)


(def v11_l52 (kind/pprint own-columns))


(deftest
 t12_l54
 (is
  ((fn
    [v]
    (and
     (=
      {:x :sepal-length, :y :sepal-width}
      (get-in v [:layers 0 :mapping]))
     (= 2 (:panels (pj/svg-summary v)))))
   v11_l52)))


(def
 v14_l60
 (def
  side-by-side
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   pj/lay-point)))


(def v15_l65 side-by-side)


(def v16_l67 (kind/pprint side-by-side))


(deftest
 t17_l69
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v16_l67)))


(def
 v19_l73
 (def
  scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point)))


(def v20_l78 scatter)


(def v21_l80 (kind/pprint scatter))


(def v22_l82 (= scatter (pj/pose scatter :sepal-length :sepal-width)))


(deftest t23_l84 (is (true? v22_l82)))


(def v25_l88 (def colored-scatter (pj/pose scatter {:color :species})))


(def v26_l91 colored-scatter)


(def v27_l93 (kind/pprint colored-scatter))


(deftest
 t28_l95
 (is
  ((fn
    [v]
    (and
     (not (contains? v :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v27_l93)))


(def
 v30_l102
 (=
  colored-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width {:color :species})
   pj/lay-point)))


(deftest t31_l107 (is (true? v30_l102)))


(def
 v33_l113
 (=
  colored-scatter
  (pj/pose colored-scatter)
  (pj/pose colored-scatter {})))


(deftest t34_l117 (is (true? v33_l113)))


(def
 v35_l119
 (=
  (pj/pose scatter {:color :species})
  (pj/pose (pj/pose scatter {:color :species}) {:color :species})))


(deftest t36_l122 (is (true? v35_l119)))


(def
 v38_l128
 (def colored-side-by-side (pj/pose side-by-side {:color :species})))


(def v39_l131 colored-side-by-side)


(def v40_l133 (kind/pprint colored-side-by-side))


(deftest
 t41_l135
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= 2 (:panels (pj/svg-summary v)))))
   v40_l133)))


(def
 v43_l141
 (def
  unread-position
  (pj/pose side-by-side :sepal-length :petal-length)))


(def v44_l144 unread-position)


(def v45_l146 (kind/pprint unread-position))


(deftest
 t46_l148
 (is
  ((fn
    [v]
    (and
     (= {:x :sepal-length, :y :petal-length} (:mapping v))
     (re-find #"not drawn" (with-out-str (pj/plan v)))))
   v45_l146)))


(def
 v48_l155
 (def
  matrix
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v49_l158 matrix)


(def v50_l160 (kind/pprint matrix))


(deftest
 t51_l162
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= {:color :species} (:mapping v))))
   v50_l160)))


(def
 v53_l167
 (def
  matrix-row
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length]
   [:sepal-width :petal-length :petal-width])))


(def v54_l170 matrix-row)


(def v55_l172 (kind/pprint matrix-row))


(deftest
 t56_l174
 (is
  ((fn
    [v]
    (and (= 3 (:panels (pj/svg-summary v))) (= 3 (count (:poses v)))))
   v55_l172)))


(def
 v58_l184
 (def
  two-layers
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width))))


(def v59_l189 two-layers)


(def v60_l191 (kind/pprint two-layers))


(deftest
 t61_l193
 (is
  ((fn
    [v]
    (and
     (nil? (:mapping v))
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}]
      (mapv :mapping (:layers v)))
     (= 2 (:panels (pj/svg-summary v)))))
   v60_l191)))


(def v63_l204 (def smooth-per-panel (-> two-layers pj/lay-smooth)))


(def v64_l208 smooth-per-panel)


(def v65_l210 (kind/pprint smooth-per-panel))


(deftest
 t66_l212
 (is
  ((fn
    [v]
    (=
     [[0 :sepal-length :sepal-width] [1 :petal-length :petal-width]]
     (->>
      (pj/draft v)
      :layers
      (filter (fn* [p1__74943#] (= :smooth (:layer-type p1__74943#))))
      (mapv (juxt :__panel-idx :x :y)))))
   v65_l210)))


(def
 v68_l220
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  pj/lay-smooth
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t69_l225
 (is
  ((fn
    [v]
    (=
     [[0 :sepal-length :sepal-width] [1 :petal-length :petal-width]]
     (->>
      (pj/draft v)
      :layers
      (filter (fn* [p1__74944#] (= :smooth (:layer-type p1__74944#))))
      (mapv (juxt :__panel-idx :x :y)))))
   v68_l220)))


(def
 v71_l236
 (def
  pose-after-lay
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/pose {:x :petal-length}))))


(def v72_l241 pose-after-lay)


(def v73_l243 (kind/pprint pose-after-lay))


(deftest
 t74_l245
 (is
  ((fn
    [v]
    (and
     (= {:x :petal-length} (:mapping v))
     (= 2 (:panels (pj/svg-summary v)))))
   v73_l243)))


(def
 v76_l250
 (def
  raw-scatter
  (pj/lay-point {:height [170 180 165], :weight [70 80 65]})))


(def v77_l253 raw-scatter)


(def v78_l255 (kind/pprint raw-scatter))


(deftest
 t79_l257
 (is
  ((fn
    [v]
    (and
     (nil? (:mapping v))
     (= {:x :height, :y :weight} (:mapping (pj/infer-mapping v)))
     (= 3 (:points (pj/svg-summary v)))))
   v78_l255)))


(def
 v81_l263
 (def
  sales
  {:quarter ["Q1" "Q2" "Q3" "Q4"],
   :revenue [120 150 140 190],
   :cost [90 100 115 120]}))


(def
 v83_l272
 (def series-line (pj/lay-line sales :quarter [:revenue :cost])))


(def v84_l275 series-line)


(def v85_l277 (kind/pprint series-line))


(deftest
 t86_l279
 (is
  ((fn
    [v]
    (and
     (= [:revenue :cost] (-> v :layers first :mapping :y))
     (= [:quarter :revenue :cost] (vec (tc/column-names (:data v))))
     (=
      [:quarter :value :series]
      ((juxt :x :y :color) (-> v pj/draft :layers first)))))
   v85_l277)))


(def
 v88_l288
 (def
  line-and-points
  (->
   sales
   (pj/lay-line :quarter [:revenue :cost])
   (pj/lay-point :quarter [:revenue :cost]))))


(def v89_l293 line-and-points)


(deftest
 t90_l295
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:points s)) (= 2 (:lines s)))))
   v89_l293)))


(def
 v92_l302
 (def
  series-no-layer
  (pj/pose sales {:x :quarter, :y [:revenue :cost]})))


(def v93_l305 series-no-layer)


(deftest
 t94_l307
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v93_l305)))
