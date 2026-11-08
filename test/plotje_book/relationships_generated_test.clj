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
 v21_l131
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth
   {:stat :linear-model, :confidence-band true, :level 0.8})))


(deftest
 t22_l136
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v21_l131)))


(def
 v23_l138
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth
   {:stat :linear-model, :confidence-band true, :level 0.99})))


(deftest
 t24_l143
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
     (> (median-width 0.99) (median-width 0.95) (median-width 0.8))))
   v23_l138)))


(def
 v26_l168
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :smoker})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t27_l173
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 244 (:points s)) (= 2 (:lines s)))))
   v26_l168)))


(def
 v29_l181
 (->
  (let
   [r (rng/rng :jdk 42) xs (vec (range 50))]
   {:x xs,
    :y
    (mapv
     (fn*
      [p1__87430#]
      (+
       (Math/sin (* p1__87430# 0.2))
       (* 0.3 (- (rng/drandom r) 0.5))))
     xs)})
  (pj/lay-point :x :y)
  (pj/lay-smooth {:bandwidth 0.2})))


(deftest
 t30_l190
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v29_l181)))


(def
 v32_l198
 (->
  (rdatasets/datasets-iris)
  (pj/lay-tile :sepal-length :sepal-width)))


(deftest
 t33_l201
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:visible-tiles s)))))
   v32_l198)))


(def
 v35_l211
 (def
  grid-data
  (let
   [r (rng/rng :jdk 99)]
   {:x (for [i (range 5) _j (range 5)] i),
    :y (for [_i (range 5) j (range 5)] j),
    :value (repeatedly 25 (fn* [] (rng/irandom r 100)))})))


(def v36_l217 (-> grid-data (pj/lay-tile :x :y {:fill :value})))


(deftest
 t37_l220
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      [lo hi]
      (:x-domain (first (:panels (pj/plan v))))]
     (and
      (= 1 (:panels s))
      (pos? (:visible-tiles s))
      (<= lo -0.5)
      (>= hi 4.5))))
   v36_l217)))


(def
 v39_l232
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)))


(deftest
 t40_l235
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:visible-tiles s)))))
   v39_l232)))


(def
 v42_l243
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density-2d :sepal-length :sepal-width)
  (pj/lay-point {:alpha 0.5})))


(deftest
 t43_l247
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:visible-tiles s)))))
   v42_l243)))


(def
 v45_l255
 (->
  (rdatasets/datasets-iris)
  (pj/lay-contour :sepal-length :sepal-width)))


(deftest
 t46_l258
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:lines s)))))
   v45_l255)))


(def
 v48_l266
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.3})
  (pj/lay-contour {:levels 8})))


(deftest
 t49_l270
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v48_l266)))


(def
 v51_l282
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t52_l286
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v51_l282)))


(def
 v54_l300
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right :histogram)))


(deftest
 t55_l304
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__87431#] (-> p1__87431# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= 150 (:points (pj/svg-summary v)))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v54_l300)))


(def
 v57_l323
 (pj/matrix
  (rdatasets/datasets-iris)
  [:sepal-length nil]
  [nil :sepal-width]
  {:col-weights [3 1], :row-weights [1 3]}))


(deftest
 t58_l328
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      subs
      (:sub-plots (pj/plan v))
      marks
      (into
       {}
       (map
        (juxt
         :path
         (fn*
          [p1__87432#]
          (mapv :mark (-> p1__87432# :plan :panels first :layers)))))
       subs)
      w
      (into
       {}
       (map
        (juxt
         :path
         (fn* [p1__87433#] (get-in p1__87433# [:plan :panel-width]))))
       subs)
      h
      (into
       {}
       (map
        (juxt
         :path
         (fn* [p1__87434#] (get-in p1__87434# [:plan :panel-height]))))
       subs)]
     (and
      (= 3 (:panels s))
      (= 150 (:points s))
      (= {[0 0] [:bar], [0 1] [], [1 0] [:point], [1 1] [:bar]} marks)
      (<= (Math/abs (- (w [1 0]) (* 3 (w [1 1])))) 3.0)
      (<= (Math/abs (- (h [1 0]) (* 3 (h [0 0])))) 3.0))))
   v57_l323)))


(def
 v60_l351
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t61_l357
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      labelled
      (->>
       (pj/plan v)
       :sub-plots
       (mapcat (comp :panels :plan))
       (keep :col-label))]
     (and
      (= 6 (:panels s))
      (= 150 (:points s))
      (= ["setosa" "versicolor" "virginica"] (vec labelled)))))
   v60_l351)))


(def v63_l385 (def small-cols [:sepal-length :petal-length]))


(def
 v64_l387
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix small-cols)))


(deftest
 t65_l391
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
   v64_l387)))


(deftest
 t67_l411
 (is
  ((fn
    [v]
    (=
     v
     (->
      (rdatasets/datasets-iris)
      (pj/pose {:color :species})
      (pj/facet {:col {:x small-cols}, :row {:y small-cols}}))))
   v64_l387)))


(def
 v69_l419
 (def cols [:sepal-length :sepal-width :petal-length :petal-width]))


(def
 v70_l421
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix cols)))


(deftest
 t71_l425
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 16 (:panels s))
      (= (* 12 150) (:points s))
      (pos? (:polygons s))
      (->>
       (:sub-plots (pj/plan v))
       (every?
        (fn
         [{:keys [path plan]}]
         (let
          [[r c] path mark (-> plan :panels first :layers first :mark)]
          (= mark (if (= r c) :bar :point)))))))))
   v70_l421)))


(def
 v73_l452
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix small-cols)
  pj/lay-point))


(deftest
 t74_l457
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
   v73_l452)))


(def
 v76_l473
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix small-cols)
  pj/lay-point
  pj/lay-density))


(deftest
 t77_l479
 (is
  ((fn
    [v]
    (->>
     (:sub-plots (pj/plan v))
     (every?
      (fn
       [{:keys [path plan]}]
       (let
        [[r c]
         path
         marks
         (set (map :mark (-> plan :panels first :layers)))]
        (= marks (if (= r c) #{:area} #{:point})))))))
   v76_l473)))


(def
 v79_l493
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix small-cols)
  pj/lay-point
  pj/lay-density
  (pj/lay-smooth
   (first small-cols)
   (second small-cols)
   {:color "black"})))


(deftest
 t80_l500
 (is
  ((fn
    [v]
    (let
     [with-line
      (->>
       (:sub-plots (pj/plan v))
       (filter
        (fn
         [{:keys [plan]}]
         (some
          #{:line}
          (map :mark (-> plan :panels first :layers))))))]
     (and
      (= 1 (count with-line))
      (=
       #{:line :point}
       (set
        (map
         :mark
         (-> with-line first :plan :panels first :layers)))))))
   v79_l493)))
