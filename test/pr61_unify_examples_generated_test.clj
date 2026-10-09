(ns
 pr61-unify-examples-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l21
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v4_l27
 (def
  moved
  (->
   (trend-plot (rdatasets/datasets-iris))
   (pj/pose :petal-length :petal-width))))


(def v5_l31 moved)


(def v6_l33 (kind/pprint moved))


(deftest
 t7_l35
 (is
  ((fn
    [v]
    (and
     (not (contains? v :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping v))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers v))))
   v6_l33)))


(def
 v9_l42
 (def
  own-columns
  (->
   (rdatasets/datasets-iris)
   pj/pose
   (pj/lay-point {:x :sepal-length, :y :sepal-width})
   (pj/pose :petal-length :petal-width))))


(def v10_l48 own-columns)


(def v11_l50 (kind/pprint own-columns))


(deftest
 t12_l52
 (is
  ((fn
    [v]
    (and
     (=
      {:x :sepal-length, :y :sepal-width}
      (get-in v [:layers 0 :mapping]))
     (= 2 (:panels (pj/svg-summary v)))))
   v11_l50)))


(def
 v14_l58
 (def
  side-by-side
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   pj/lay-point)))


(def v15_l63 side-by-side)


(def v16_l65 (kind/pprint side-by-side))


(deftest
 t17_l67
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v16_l65)))


(def
 v19_l71
 (def
  scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point)))


(def v20_l76 scatter)


(def v21_l78 (kind/pprint scatter))


(def v22_l80 (= scatter (pj/pose scatter :sepal-length :sepal-width)))


(deftest t23_l82 (is (true? v22_l80)))


(def v25_l86 (def colored-scatter (pj/pose scatter {:color :species})))


(def v26_l89 colored-scatter)


(def v27_l91 (kind/pprint colored-scatter))


(deftest
 t28_l93
 (is
  ((fn
    [v]
    (and
     (not (contains? v :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping v))
     (= [{:layer-type :point}] (:layers v))))
   v27_l91)))


(def
 v30_l100
 (=
  colored-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width {:color :species})
   pj/lay-point)))


(deftest t31_l105 (is (true? v30_l100)))


(def
 v33_l111
 (=
  colored-scatter
  (pj/pose colored-scatter)
  (pj/pose colored-scatter {})))


(deftest t34_l115 (is (true? v33_l111)))


(def
 v35_l117
 (=
  (pj/pose scatter {:color :species})
  (pj/pose (pj/pose scatter {:color :species}) {:color :species})))


(deftest t36_l120 (is (true? v35_l117)))


(def
 v38_l126
 (def colored-side-by-side (pj/pose side-by-side {:color :species})))


(def v39_l129 colored-side-by-side)


(def v40_l131 (kind/pprint colored-side-by-side))


(deftest
 t41_l133
 (is
  ((fn
    [v]
    (and
     (= {:color :species} (:mapping v))
     (= 2 (:panels (pj/svg-summary v)))))
   v40_l131)))


(def
 v43_l139
 (def
  unread-position
  (pj/pose side-by-side :sepal-length :petal-length)))


(def v44_l142 unread-position)


(def v45_l144 (kind/pprint unread-position))


(deftest
 t46_l146
 (is
  ((fn
    [v]
    (and
     (= {:x :sepal-length, :y :petal-length} (:mapping v))
     (re-find #"not drawn" (with-out-str (pj/plan v)))))
   v45_l144)))


(def
 v48_l153
 (def
  matrix
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length :sepal-width]
   {:color :species})))


(def v49_l156 matrix)


(def v50_l158 (kind/pprint matrix))


(deftest
 t51_l160
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (= {:color :species} (:mapping v))))
   v50_l158)))


(def
 v53_l165
 (def
  matrix-row
  (pj/cross-matrix
   (rdatasets/datasets-iris)
   [:sepal-length]
   [:sepal-width :petal-length :petal-width])))


(def v54_l168 matrix-row)


(def v55_l170 (kind/pprint matrix-row))


(deftest
 t56_l172
 (is
  ((fn
    [v]
    (and (= 3 (:panels (pj/svg-summary v))) (= 3 (count (:poses v)))))
   v55_l170)))
