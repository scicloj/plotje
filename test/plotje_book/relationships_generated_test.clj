(ns
 plotje-book.relationships-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [clojure.test :refer [deftest is]]))


(def
 v3_l28
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t4_l31
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (zero? (:lines s)))))
   v3_l28)))


(def
 v6_l40
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t7_l43
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (zero? (:lines s)))))
   v6_l40)))


(def
 v9_l53
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t10_l56
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (zero? (:lines s)))))
   v9_l53)))


(def
 v12_l65
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t13_l69
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v12_l65)))


(def
 v15_l77
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t16_l82
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v15_l77)))


(def
 v18_l90
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})))


(deftest
 t19_l95
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      base
      (->
       (rdatasets/datasets-iris)
       (pj/pose :sepal-length :sepal-width {:color :species})
       pj/lay-point)
      default-band
      (->
       base
       (pj/lay-smooth {:stat :linear-model, :confidence-band true})
       pj/plan
       :panels
       first
       :layers
       last
       :ribbons)
      explicit-95
      (->
       base
       (pj/lay-smooth
        {:stat :linear-model, :confidence-band true, :level 0.95})
       pj/plan
       :panels
       first
       :layers
       last
       :ribbons)]
     (and
      (= 150 (:points s))
      (= 3 (:lines s))
      (= 3 (:polygons s))
      (= default-band explicit-95))))
   v18_l90)))


(def
 v21_l123
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth
   {:stat :linear-model, :confidence-band true, :level 0.8})))


(deftest
 t22_l128
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v21_l123)))


(def
 v23_l130
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth
   {:stat :linear-model, :confidence-band true, :level 0.99})))


(deftest
 t24_l135
 (is
  ((fn
    [v]
    (let
     [iris
      (rdatasets/datasets-iris)
      median-width
      (fn
       [level]
       (let
        [r
         (->
          iris
          (pj/pose :sepal-length :sepal-width)
          (pj/lay-smooth
           {:stat :linear-model, :confidence-band true, :level level})
          pj/plan
          :panels
          first
          :layers
          first
          :ribbons
          first)
         widths
         (map - (:ymaxs r) (:ymins r))]
        (nth (sort widths) (quot (count widths) 2))))]
     (> (median-width 0.99) (median-width 0.8))))
   v23_l130)))


(def
 v26_l155
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :smoker})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t27_l160
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 244 (:points s)) (= 2 (:lines s)))))
   v26_l155)))


(def
 v29_l168
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__84961#]
      (+
       (Math/sin (* p1__84961# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t30_l177
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v29_l168)))


(def
 v32_l185
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t33_l188
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:visible-tiles s)))))
   v32_l185)))


(def
 v35_l196
 (def
  grid-data
  (let
   [r (rng/rng :jdk 99)]
   {:x (for [i (range 5) _j (range 5)] i),
    :y (for [_i (range 5) j (range 5)] j),
    :value (repeatedly 25 (fn* [] (rng/irandom r 100)))})))


(def v36_l202 (-> grid-data (pj/lay-tile :x :y {:fill :value})))


(deftest
 t37_l205
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:visible-tiles s)))))
   v36_l202)))


(def
 v39_l213
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t40_l216
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:visible-tiles s)))))
   v39_l213)))


(def
 v42_l224
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)
  (pj/lay-point {:alpha 0.5})))


(deftest
 t43_l228
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:visible-tiles s)))))
   v42_l224)))


(def
 v45_l236
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t46_l239
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:lines s)))))
   v45_l236)))


(def
 v48_l247
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.3})
  (pj/lay-contour {:levels 8})))


(deftest
 t49_l251
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v48_l247)))


(def v51_l267 (def small-cols [:sepal-length :petal-length]))


(def
 v52_l269
 (->
  (rdatasets/datasets-iris)
  (pj/pose (pj/cross small-cols small-cols) {:color :species})))


(deftest
 t53_l272
 (is
  ((fn
    [v]
    (let
     [marks
      (->>
       (:sub-plots (pj/plan v))
       (mapv
        (fn
         [{:keys [path plan]}]
         (let
          [[r c] path m (-> plan :panels first :layers first :mark)]
          [r c m]))))]
     (and
      (= 4 (:panels (pj/svg-summary v)))
      (every? (fn [[r c m]] (= m (if (= r c) :bar :point))) marks))))
   v52_l269)))


(def
 v55_l285
 (def cols [:sepal-length :sepal-width :petal-length :petal-width]))


(def
 v56_l287
 (->
  (rdatasets/datasets-iris)
  (pj/pose (pj/cross cols cols) {:color :species})))


(deftest
 t57_l290
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 16 (:panels s))
      (= (* 12 150) (:points s))
      (pos? (:polygons s)))))
   v56_l287)))


(deftest
 t58_l295
 (is
  ((fn
    [v]
    (->>
     (:sub-plots (pj/plan v))
     (every?
      (fn
       [{:keys [path plan]}]
       (let
        [[r c] path mark (-> plan :panels first :layers first :mark)]
        (= mark (if (= r c) :bar :point)))))))
   v56_l287)))
