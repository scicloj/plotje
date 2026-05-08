(ns
 plotje-book.edge-cases-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [java-time.api :as jt]
  [tech.v3.datatype.datetime :as dt-dt]
  [tech.v3.datatype :as dtype]
  [clojure.test :refer [deftest is]]))


(def
 v3_l31
 (def with-missing {:x [1 2 nil 4 5 nil 7], :y [3 nil 5 6 nil 8 9]}))


(def v4_l35 (-> with-missing (pj/lay-point :x :y)))


(deftest
 t5_l38
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:points s)))))
   v4_l35)))


(def
 v7_l47
 (def
  with-infinity
  {:x [1 2 3 4 5],
   :y
   [10.0 Double/POSITIVE_INFINITY 30.0 Double/NEGATIVE_INFINITY 50.0]}))


(def v8_l51 (-> with-infinity (pj/lay-point :x :y)))


(deftest
 t9_l54
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 3 (:points s))
      (not (clojure.string/includes? (str v) "NaN")))))
   v8_l51)))


(def v11_l62 (-> {:x [3], :y [7]} (pj/lay-point :x :y)))


(deftest
 t12_l65
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:points s)))))
   v11_l62)))


(def
 v14_l74
 (->
  {:x [1 10], :y [5 50]}
  (pj/lay-point :x :y)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t15_l78
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:points s)) (zero? (:lines s)))))
   v14_l74)))


(def
 v17_l86
 (->
  {:x [1 5 10], :y [5 25 50]}
  (pj/lay-point :x :y)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t18_l90
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 1 (:lines s)))))
   v17_l86)))


(def v20_l98 (-> {:x [5 5 5 5 5], :y [1 2 3 4 5]} (pj/lay-point :x :y)))


(deftest
 t21_l101
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:points s)))))
   v20_l98)))


(def
 v23_l109
 (-> {:x [1 2 3 4 5], :y [3 3 3 3 3]} (pj/lay-point :x :y)))


(deftest
 t24_l112
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:points s)))))
   v23_l109)))


(def
 v26_l121
 (-> {:x [-5 -3 0 3 5], :y [-2 4 0 -4 2]} (pj/lay-point :x :y)))


(deftest
 t27_l124
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:points s)))))
   v26_l121)))


(def
 v29_l130
 (->
  {:x [1000000.0 2000000.0 3000000.0], :y [1.0E9 2.0E9 3.0E9]}
  (pj/lay-point :x :y)))


(deftest
 t30_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:points s)))))
   v29_l130)))


(def
 v32_l139
 (->
  {:x [0.001 0.002 0.003], :y [1.0E-4 2.0E-4 3.0E-4]}
  (pj/lay-point :x :y)))


(deftest
 t33_l142
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:points s)))))
   v32_l139)))


(def
 v35_l150
 (def
  large-data
  (let
   [r (rng/rng :jdk 42)]
   {:x (repeatedly 1000 (fn* [] (rng/drandom r))),
    :y (repeatedly 1000 (fn* [] (rng/drandom r))),
    :group (repeatedly 1000 (fn* [] ([:a :b :c] (rng/irandom r 3))))})))


(def v36_l156 (-> large-data (pj/lay-point :x :y {:color :group})))


(deftest
 t37_l159
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1000 (:points s)))))
   v36_l156)))


(def
 v39_l167
 (->
  (let
   [r (rng/rng :jdk 99)]
   {:category
    (map
     (fn* [p1__89794#] (keyword (str "cat-" p1__89794#)))
     (range 12)),
    :value (repeatedly 12 (fn* [] (+ 10 (rng/irandom r 90))))})
  (pj/lay-value-bar :category :value)))


(deftest
 t40_l172
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 12 (:polygons s)))))
   v39_l167)))


(def
 v42_l180
 (->
  (rdatasets/datasets-iris)
  (tc/map-columns :sepal-ratio [:sepal-length :sepal-width] /)
  (pj/lay-point :sepal-length :sepal-ratio {:color :species})
  (pj/options {:title "Sepal Length/Width Ratio"})))


(deftest
 t43_l185
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v42_l180)))


(def
 v45_l193
 (->
  (rdatasets/datasets-iris)
  (tc/select-rows
   (fn* [p1__89795#] (= "setosa" (p1__89795# :species))))
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Setosa Only"})))


(deftest
 t46_l199
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v45_l193)))


(def
 v48_l209
 (->
  {:category ["a" "b" "c"], :count [10 20 15]}
  (pj/lay-value-bar :category :count {:position :stack})))


(deftest
 t49_l213
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v48_l209)))


(def
 v51_l220
 (->
  {:x ["a" "b" "a"], :g ["g1" "g1" "g2"]}
  (pj/lay-bar :x {:color :g})))


(deftest
 t52_l224
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v51_l220)))


(def
 v54_l231
 (->
  {:x ["a" "a" "b" "b" "b"], :g ["g1" "g2" "g1" "g1" "g1"]}
  (pj/lay-bar :x {:position :fill, :color :g})))


(deftest
 t55_l235
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v54_l231)))


(def
 v57_l241
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:nudge-x 0.1, :nudge-y -0.05})))


(deftest
 t58_l244
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v57_l241)))


(def
 v60_l251
 (->
  {:x [1 2 3], :y [2 4 5]}
  (pj/lay-point :x :y)
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})))


(deftest
 t61_l255
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (= 1 (:lines s)))))
   v60_l251)))


(def
 v63_l263
 (->
  (let
   [r (rng/rng :jdk 55)]
   {:x (range 10), :y (repeatedly 10 (fn* [] (rng/irandom r 20)))})
  (pj/lay-area :x :y {:position :stack})))


(deftest
 t64_l268
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v63_l263)))


(def
 v66_l274
 (->
  {:x [1 10 100 1000 10000], :y [2 20 200 2000 20000]}
  (pj/lay-point :x :y)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t67_l280
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:panels s)))))
   v66_l274)))


(def
 v69_l286
 (->
  {:x [0.001 0.01 0.1 1 10 100], :y [1 2 3 4 5 6]}
  (pj/lay-point :x :y)
  (pj/scale :x :log)))


(deftest
 t70_l291
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v69_l286)))


(def
 v72_l298
 (->
  {:x [0 -1 1 10 100], :y [1 2 3 4 5]}
  (pj/lay-point :x :y)
  (pj/scale :x :log)))


(deftest
 t73_l302
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v72_l298)))


(def
 v75_l309
 (->
  {:x [1 2 3], :y [4 5 6], :c [5 5 5]}
  (pj/lay-point :x :y {:color :c})))


(deftest
 t76_l312
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v75_l309)))


(def
 v78_l316
 (->
  {:x (range 20),
   :y (map (fn* [p1__89796#] (- p1__89796# 10)) (range 20)),
   :val (map (fn* [p1__89797#] (- p1__89797# 10.0)) (range 20))}
  (pj/lay-point :x :y {:color :val})
  (pj/options {:color-scale :diverging, :color-midpoint 0})))


(deftest
 t79_l322
 (is ((fn [v] (= 20 (:points (pj/svg-summary v)))) v78_l316)))


(def
 v81_l326
 (->
  {:date [(jt/local-date 2025 1 1) (jt/local-date 2025 1 2)],
   :val [10 20]}
  (pj/lay-point :date :val)))


(deftest
 t82_l331
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v81_l326)))


(def
 v84_l338
 (->
  {:time
   (dt-dt/plus-temporal-amount
    (dtype/const-reader (jt/local-date-time 2025 3 15 8 0) 24)
    (map (fn* [p1__89798#] (* (long p1__89798#) 15)) (range 24))
    :minutes),
   :value
   (map
    (fn* [p1__89799#] (+ 18.0 (* 4.0 (Math/sin (* p1__89799# 0.3)))))
    (range 24))}
  (pj/lay-line :time :value)
  pj/lay-point))


(deftest
 t85_l345
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 24 (:points s)) (= 1 (:lines s)))))
   v84_l338)))


(def
 v87_l355
 (->
  {:time
   (dt-dt/plus-temporal-amount
    (dtype/const-reader (jt/instant 1750003200000) 12)
    (range 12)
    :hours),
   :temp
   (map
    (fn* [p1__89800#] (+ 20.0 (* 5.0 (Math/sin (* p1__89800# 0.5)))))
    (range 12))}
  (pj/lay-line :time :temp)
  pj/lay-point))


(deftest
 t88_l362
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 1 (:lines s))
      (some
       (fn* [p1__89801#] (re-find #":\d\d" p1__89801#))
       (:texts s)))))
   v87_l355)))


(def
 v90_l371
 (->
  {:date
   (dt-dt/plus-temporal-amount
    (dtype/const-reader (jt/local-date 2020 1 1) 20)
    (map (fn* [p1__89802#] (* (long p1__89802#) 120)) (range 20))
    :days),
   :value
   (map
    (fn* [p1__89803#] (+ 100 (* 50 (Math/sin (* p1__89803# 0.4)))))
    (range 20))}
  (pj/lay-line :date :value)
  pj/lay-point))


(deftest
 t91_l378
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 20 (:points s)) (= 1 (:lines s)))))
   v90_l371)))


(def
 v93_l384
 (->
  {:cat (map (fn* [p1__89804#] (str "cat-" p1__89804#)) (range 12)),
   :val (repeatedly 12 (fn* [] (rand-int 100)))}
  (pj/lay-value-bar :cat :val)
  (pj/coord :polar)))


(deftest
 t94_l389
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v93_l384)))


(def
 v96_l397
 (->
  {:x [1 10 100 1000], :y [2 4 8 16]}
  (pj/lay-point :x :y)
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t97_l402
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v) panel (first (:panels plan))]
     (and
      (= 4 (:points (pj/svg-summary v)))
      (= :flip (:coord panel))
      (= {:type :log} (:y-scale panel))
      (= {:type :linear} (:x-scale panel)))))
   v96_l397)))


(def
 v99_l415
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :y {:domain [0 6]})))


(deftest
 t100_l419
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v) panel (first (:panels plan))]
     (= [0 6] (:y-domain panel))))
   v99_l415)))


(def
 v102_l427
 (->
  {:x (range 100), :y (range 0 10 0.1)}
  (pj/lay-point :x :y)
  (pj/coord :fixed)))


(deftest
 t103_l431
 (is ((fn [v] (= 100 (:points (pj/svg-summary v)))) v102_l427)))


(def
 v105_l438
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   (pj/cross
    [:sepal-length :sepal-width :petal-length]
    [:sepal-length :sepal-width :petal-length])
   {:color :species})))


(deftest
 t106_l443
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      texts
      (:texts s)
      col-label?
      (fn* [p1__89805#] (re-find #"sepal|petal" p1__89805#))]
     (and (= 9 (:panels s)) (seq (filter col-label? texts)))))
   v105_l438)))


(def
 v108_l453
 (try
  (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :nonexistent :y) pj/plot)
  (catch Exception e (ex-message e))))


(deftest t109_l460 (is ((fn [m] (string? m)) v108_l453)))


(def
 v111_l464
 (try
  (->
   {:x [1 2 3], :y [4 5 6]}
   (pj/lay-point :x :y {:color :bogus})
   pj/plot)
  (catch Exception e (ex-message e))))


(deftest t112_l471 (is ((fn [m] (string? m)) v111_l464)))


(def
 v114_l475
 (try
  (->
   {:x [1 2 3], :y [4 5 6]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t115_l483
 (is ((fn [m] (re-find #"not supported with polar" m)) v114_l475)))


(def
 v117_l487
 (try
  (->
   {:x [1 2 3]}
   (pj/pose :x)
   (pj/lay {:mark :boxplot, :stat :bin})
   pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t118_l495
 (is ((fn [m] (re-find #"must contain :boxes" m)) v117_l487)))


(def
 v120_l504
 (try
  (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-histogram :x :y))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t121_l510
 (is
  ((fn [m] (re-find #"lay-histogram uses only the x column" m))
   v120_l504)))
