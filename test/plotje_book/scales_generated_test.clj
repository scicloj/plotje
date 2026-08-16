(ns
 plotje-book.scales-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l42
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:size :cyl, :color :class})))


(def
 v5_l56
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/scale :x :log)))


(deftest
 t6_l60
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v5_l56)))


(def
 v8_l66
 (def
  linear-cell
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/options {:title "linear"}))))


(def
 v9_l71
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v10_l76 (pj/arrange [linear-cell log-cell]))


(deftest
 t11_l78
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
        [p1__11193#]
        (-> p1__11193# :plan :panels first :x-scale :type))))))
   v10_l76)))


(def
 v13_l90
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:size {:column :cyl, :scale :log}})))


(deftest
 t14_l93
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v13_l90)))


(def
 v16_l120
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t17_l127
 (is ((fn [m] (re-find #":x reads no :range" m)) v16_l120)))


(def
 v19_l153
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v20_l156
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
 v22_l170
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
 v23_l180
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
 t24_l190
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
   v23_l180)))


(def
 v26_l206
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t27_l210
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
   v26_l206)))


(def
 v29_l224
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t30_l228
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
   v29_l224)))


(def v32_l253 (:varies (layer-type/lookup :point)))


(deftest
 t33_l255
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v32_l253)))


(def
 v35_l278
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/pose :displ :hwy)
   (pj/lay-point {:size {:column :cyl, :scale :log}})
   (pj/lay-point {:size {:column :cyl, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t36_l287
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v35_l278)))


(def
 v38_l296
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/scale :y {:domain [10 50], :breaks [10 20 30 40 50]})))


(deftest
 t39_l300
 (is
  ((fn
    [fr]
    (=
     [10.0 20.0 30.0 40.0 50.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v38_l296)))
