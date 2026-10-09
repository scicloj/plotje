(ns
 pr61-unify-examples-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l18
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v4_l24
 (def
  moved
  (->
   (trend-plot (rdatasets/datasets-iris))
   (pj/pose :petal-length :petal-width))))


(def v5_l28 moved)


(def v6_l30 (kind/pprint moved))


(deftest
 t7_l32
 (is
  ((fn
    [v]
    (and
     (not (contains? v :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping v))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers v))))
   v6_l30)))


(def
 v8_l36
 (def
  own-columns
  (->
   (rdatasets/datasets-iris)
   pj/pose
   (pj/lay-point {:x :sepal-length, :y :sepal-width})
   (pj/pose :petal-length :petal-width))))


(def v9_l42 own-columns)


(def v10_l44 (kind/pprint own-columns))


(deftest
 t11_l46
 (is
  ((fn
    [v]
    (and
     (=
      {:x :sepal-length, :y :sepal-width}
      (get-in v [:layers 0 :mapping]))
     (= 2 (:panels (pj/svg-summary v)))))
   v10_l44)))


(def
 v12_l50
 (def
  side-by-side
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   pj/lay-point)))


(def v13_l55 side-by-side)


(def v14_l57 (kind/pprint side-by-side))


(deftest
 t15_l59
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v14_l57)))


(def
 v17_l63
 (def
  scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point)))


(def v18_l68 scatter)


(def v19_l70 (kind/pprint scatter))


(def v20_l72 (= scatter (pj/pose scatter :sepal-length :sepal-width)))


(deftest t21_l74 (is (true? v20_l72)))


(def v23_l78 (def colored-scatter (pj/pose scatter {:color :species})))


(def v24_l81 colored-scatter)


(def v25_l83 (kind/pprint colored-scatter))


(deftest
 t26_l85
 (is
  ((fn
    [v]
    (and
     (not (contains? v :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v25_l83)))


(def
 v27_l90
 (=
  colored-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width {:color :species})
   pj/lay-point)))


(deftest t28_l95 (is (true? v27_l90)))


(def
 v30_l99
 (=
  colored-scatter
  (pj/pose colored-scatter)
  (pj/pose colored-scatter {})))


(deftest t31_l103 (is (true? v30_l99)))


(def
 v32_l105
 (=
  (pj/pose scatter {:color :species})
  (pj/pose (pj/pose scatter {:color :species}) {:color :species})))


(deftest t33_l108 (is (true? v32_l105)))


(def
 v35_l112
 (def colored-side-by-side (pj/pose side-by-side {:color :species})))


(def v36_l115 colored-side-by-side)


(def v37_l117 (kind/pprint colored-side-by-side))


(deftest
 t38_l119
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= 2 (:panels (pj/svg-summary v)))))
   v37_l117)))


(def
 v39_l122
 (def
  unread-position
  (pj/pose side-by-side :sepal-length :petal-length)))


(def v40_l125 unread-position)


(def v41_l127 (kind/pprint unread-position))


(deftest
 t42_l129
 (is
  ((fn
    [v]
    (and
     (= {:x :sepal-length, :y :petal-length} (:mapping v))
     (re-find #"not drawn" (with-out-str (pj/plan v)))))
   v41_l127)))


(def
 v44_l134
 (def
  matrix
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v45_l137 matrix)


(def v46_l139 (kind/pprint matrix))


(deftest
 t47_l141
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= {:color :species} (:mapping v))))
   v46_l139)))


(def
 v48_l144
 (def
  matrix-row
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length]
   [:sepal-width :petal-length :petal-width])))


(def v49_l147 matrix-row)


(def v50_l149 (kind/pprint matrix-row))


(deftest
 t51_l151
 (is
  ((fn
    [v]
    (and (= 3 (:panels (pj/svg-summary v))) (= 3 (count (:poses v)))))
   v50_l149)))


(def
 v53_l158
 (def
  two-layers
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width))))


(def v54_l163 two-layers)


(def v55_l165 (kind/pprint two-layers))


(deftest
 t56_l167
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
   v55_l165)))


(def v58_l175 (def smooth-per-panel (-> two-layers pj/lay-smooth)))


(def v59_l179 smooth-per-panel)


(def v60_l181 (kind/pprint smooth-per-panel))


(deftest
 t61_l183
 (is
  ((fn
    [v]
    (=
     [[0 :sepal-length :sepal-width] [1 :petal-length :petal-width]]
     (->>
      (pj/draft v)
      :layers
      (filter (fn* [p1__73966#] (= :smooth (:layer-type p1__73966#))))
      (mapv (juxt :__panel-idx :x :y)))))
   v60_l181)))


(def
 v62_l189
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  pj/lay-smooth
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t63_l194
 (is
  ((fn
    [v]
    (=
     [[0 :sepal-length :sepal-width] [1 :petal-length :petal-width]]
     (->>
      (pj/draft v)
      :layers
      (filter (fn* [p1__73967#] (= :smooth (:layer-type p1__73967#))))
      (mapv (juxt :__panel-idx :x :y)))))
   v62_l189)))


(def
 v65_l202
 (def
  pose-after-lay
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/pose {:x :petal-length}))))


(def v66_l207 pose-after-lay)


(def v67_l209 (kind/pprint pose-after-lay))


(deftest
 t68_l211
 (is
  ((fn
    [v]
    (and
     (= {:x :petal-length} (:mapping v))
     (= 2 (:panels (pj/svg-summary v)))))
   v67_l209)))


(def
 v70_l216
 (def
  raw-scatter
  (pj/lay-point {:height [170 180 165], :weight [70 80 65]})))


(def v71_l219 raw-scatter)


(def v72_l221 (kind/pprint raw-scatter))


(deftest
 t73_l223
 (is
  ((fn
    [v]
    (and
     (nil? (:mapping v))
     (= {:x :height, :y :weight} (:mapping (pj/infer-mapping v)))
     (= 3 (:points (pj/svg-summary v)))))
   v72_l221)))


(def
 v75_l229
 (def
  sales
  {:quarter ["Q1" "Q2" "Q3" "Q4"],
   :revenue [120 150 140 190],
   :cost [90 100 115 120]}))


(def
 v77_l236
 (def series-line (pj/lay-line sales :quarter [:revenue :cost])))


(def v78_l239 series-line)


(def v79_l241 (kind/pprint series-line))


(deftest
 t80_l243
 (is
  ((fn
    [v]
    (and
     (= [:revenue :cost] (-> v :layers first :mapping :y))
     (= [:quarter :revenue :cost] (vec (tc/column-names (:data v))))
     (=
      [:quarter :value :series]
      ((juxt :x :y :color) (-> v pj/draft :layers first)))))
   v79_l241)))


(def
 v82_l250
 (def
  line-and-points
  (->
   sales
   (pj/lay-line :quarter [:revenue :cost])
   (pj/lay-point :quarter [:revenue :cost]))))


(def v83_l255 line-and-points)


(deftest
 t84_l257
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:points s)) (= 2 (:lines s)))))
   v83_l255)))


(def
 v86_l262
 (def
  series-no-layer
  (pj/pose sales {:x :quarter, :y [:revenue :cost]})))


(def v87_l265 series-no-layer)


(deftest
 t88_l267
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v87_l265)))


(def
 v90_l273
 (def
  lone-vector
  (pj/lay-histogram
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width])))


(def v91_l276 lone-vector)


(deftest
 t92_l278
 (is
  ((fn
    [v]
    (and
     (= 2 (:panels (pj/svg-summary v)))
     (re-find
      #"\(pj/arrange \[:sepal-length :sepal-width\]\)"
      (with-out-str
       (pj/lay-histogram
        (rdatasets/datasets-iris)
        [:sepal-length :sepal-width])))))
   v91_l276)))


(def
 v94_l286
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (catch Exception e (ex-message e))))


(deftest
 t95_l291
 (is
  ((fn
    [msg]
    (and
     (re-find #"pj/lay-point was given pairs of columns" msg)
     (re-find #"pj/arrange" msg)
     (re-find #"pj/cross-matrix" msg)))
   v94_l286)))
