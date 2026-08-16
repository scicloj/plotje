(ns
 plotje-book.scales-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (def
  gapminder-2007
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows (fn* [p1__11193#] (= 2007 (:year p1__11193#)))))))


(def v4_l39 gapminder-2007)


(def
 v6_l62
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:size :pop, :color :continent})))


(def
 v8_l87
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t9_l93 (is ((fn [spec] (= {:type :log} spec)) v8_l87)))


(def
 v10_l95
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t11_l101 (is ((fn [spec] (= {:type :log} spec)) v10_l95)))


(def
 v13_l110
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v14_l115
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v15_l120 (pj/arrange [linear-cell log-cell]))


(deftest
 t16_l122
 (is
  ((fn
    [fr]
    (=
     [:linear :log]
     (->>
      (pj/plan fr)
      :sub-plots
      (mapv
       (fn*
        [p1__11194#]
        (-> p1__11194# :plan :panels first :x-scale :type))))))
   v15_l120)))


(def
 v18_l137
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t19_l141
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v18_l137)))


(def
 v21_l148
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size {:column :pop, :scale :log}})
  (pj/lay-point {:size {:column :pop, :scale :linear}})
  (pj/scale :x :log)))


(deftest
 t22_l153
 (is
  ((fn [fr] (= :linear (-> fr pj/plan :size-legend :scale-type)))
   v21_l148)))


(def
 v24_l165
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t25_l171
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v24_l165)))


(def
 v27_l185
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size {:column :pop, :scale :log}})
  pj/lay-point
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest t28_l191 (is ((fn [spec] (= {:type :log} spec)) v27_l185)))


(def
 v30_l195
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size :pop})
  pj/lay-point
  (pj/scale :size :log)
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest
 t31_l202
 (is
  ((fn
    [spec]
    (=
     spec
     (->
      gapminder-2007
      (pj/pose
       :gdp-percap
       :life-exp
       {:size {:column :pop, :scale :log}})
      pj/lay-point
      pj/plan
      :panels
      first
      :layers
      first
      :size-scale)))
   v30_l195)))


(def
 v33_l222
 (->
  gapminder-2007
  (pj/lay-point
   {:x :gdp-percap,
    :y
    {:column :life-exp,
     :scale {:domain [35 85], :breaks [40 60 80]}}})))


(deftest
 t34_l227
 (is
  ((fn
    [fr]
    (=
     [40.0 60.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v33_l222)))


(def
 v36_l241
 (->
  gapminder-2007
  (pj/pose
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/lay-point))


(deftest
 t37_l245
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v36_l241)))


(def
 v39_l280
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t40_l287
 (is ((fn [m] (re-find #":x reads no :range" m)) v39_l280)))


(def
 v42_l313
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v43_l316
 (->
  (pj/arrange
   [(->
     squares
     (pj/lay-point :x :y {:size :n})
     (pj/options {:title ":sqrt (default)"}))
    (->
     squares
     (pj/lay-point :x :y {:size :n})
     (pj/scale :size {:by :linear})
     (pj/options {:title ":linear"}))
    (->
     squares
     (pj/lay-point :x :y {:size :n})
     (pj/scale :size {:by :area})
     (pj/options {:title ":area"}))])
  (pj/options {:width 900, :height 340})))


(def
 v45_l330
 (defn
  legend-magnitudes
  [spec]
  (->>
   (->
    squares
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size spec)
    pj/plan
    :size-legend
    :entries)
   (mapv :magnitude))))


(def
 v46_l340
 (kind/table
  {:column-names
   ["by" "smallest labelled" "middle" "largest labelled"],
   :row-maps
   (for
    [by
     [:linear :area :sqrt]
     :let
     [ms
      (legend-magnitudes {:by by})
      mid
      (nth ms (quot (count ms) 2))]]
    {"by" (kind/code (pr-str by)),
     "smallest labelled" (format "%.2f" (first ms)),
     "middle" (format "%.2f" mid),
     "largest labelled" (format "%.2f" (last ms))})}))


(deftest
 t47_l350
 (is
  ((fn
    [_]
    (every?
     (fn [[l a s]] (< l a s))
     (map
      vector
      (legend-magnitudes {:by :linear})
      (legend-magnitudes {:by :area})
      (legend-magnitudes {:by :sqrt}))))
   v46_l340)))


(def
 v49_l365
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t50_l369
 (is
  ((fn
    [fr]
    (let
     [widest
      (->>
       fr
       pj/plan
       :size-legend
       :entries
       (map :magnitude)
       (apply max)
       double)]
     (< 8.0 widest 20.0)))
   v49_l365)))


(def
 v52_l383
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t53_l387
 (is
  ((fn
    [fr]
    (let
     [{:keys [entries]}
      (-> fr pj/plan :size-legend)
      area
      (fn [e] (Math/pow (:magnitude e) 2))
      by-value
      (into {} (map (juxt :value area) entries))]
     (every?
      (fn
       [[v a]]
       (if-let
        [half (by-value (/ v 2))]
        (< (Math/abs (- (/ a half) 2.0)) 1.0E-6)
        true))
      by-value)))
   v52_l383)))


(def v55_l412 (:varies (layer-type/lookup :point)))


(deftest
 t56_l414
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v55_l412)))


(def
 v58_l437
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t59_l446
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v58_l437)))


(def
 v61_l455
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:domain [35 85], :breaks [40 50 60 70 80]})))


(deftest
 t62_l459
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v61_l455)))
