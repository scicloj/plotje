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
 v8_l82
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)))


(deftest
 t9_l86
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v8_l82)))


(def
 v11_l92
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v12_l97
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v13_l102 (pj/arrange [linear-cell log-cell]))


(deftest
 t14_l104
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
   v13_l102)))


(def
 v16_l119
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t17_l123
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v16_l119)))


(def
 v19_l133
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest
 t20_l140
 (is ((fn [spec] (= {:range [3 16], :type :log} spec)) v19_l133)))


(def
 v22_l146
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest
 t23_l154
 (is ((fn [spec] (= {:type :log, :range [3 16]} spec)) v22_l146)))


(def
 v25_l166
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale false}})
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest t26_l173 (is ((fn [spec] (nil? spec)) v25_l166)))


(def
 v28_l188
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t29_l194 (is ((fn [spec] (= {:type :log} spec)) v28_l188)))


(def
 v30_l196
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t31_l202 (is ((fn [spec] (= {:type :log} spec)) v30_l196)))


(def
 v33_l206
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t34_l211 (is ((fn [spec] (= {:type :log} spec)) v33_l206)))


(def
 v35_l213
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t36_l218 (is ((fn [spec] (= {:type :log} spec)) v35_l213)))


(def
 v38_l224
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t39_l231
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v38_l224)))


(def
 v41_l239
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


(deftest t42_l245 (is ((fn [spec] (= {:type :log} spec)) v41_l239)))


(def
 v44_l249
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
 t45_l256
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
   v44_l249)))


(def
 v47_l276
 (->
  gapminder-2007
  (pj/lay-point
   {:x :gdp-percap,
    :y
    {:column :life-exp,
     :scale {:domain [35 85], :breaks [40 60 80]}}})))


(deftest
 t48_l281
 (is
  ((fn
    [fr]
    (=
     [40.0 60.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v47_l276)))


(def
 v50_l295
 (->
  gapminder-2007
  (pj/pose
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/lay-point))


(deftest
 t51_l299
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v50_l295)))


(def
 v53_l334
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t54_l341
 (is ((fn [m] (re-find #":x reads no :range" m)) v53_l334)))


(def
 v56_l367
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v57_l370
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
 v59_l384
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
 v60_l394
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
 t61_l404
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
   v60_l394)))


(def
 v63_l419
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t64_l423
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
   v63_l419)))


(def
 v66_l437
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t67_l441
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
   v66_l437)))


(def v69_l466 (:varies (layer-type/lookup :point)))


(deftest
 t70_l468
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v69_l466)))


(def
 v72_l491
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t73_l500
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v72_l491)))


(def
 v75_l509
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:domain [35 85], :breaks [40 50 60 70 80]})))


(deftest
 t76_l513
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v75_l509)))
