(ns
 plotje-book.waterfall-extension-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.stat :as stat]
  [scicloj.plotje.impl.extract :as extract]
  [scicloj.plotje.render.mark :as mark]
  [scicloj.plotje.layer-type :as layer-type]
  [membrane.ui :as ui]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (def
  pnl-data
  {:category
   ["Revenue" "COGS" "Gross Profit" "OpEx" "Tax" "Net Income"],
   :amount [500 -300 200 -120 -30 50]}))


(def
 v5_l49
 (defmethod
  stat/compute-stat
  :waterfall
  [{:keys [data x y x-type], :as draft-layer}]
  (let
   [clean
    (tc/drop-missing data [x y])
    categories
    (vec (distinct (clean x)))
    values
    (vec (clean y))
    ends
    (vec (reductions + values))
    starts
    (vec (cons 0 (butlast ends)))
    bars
    (mapv
     (fn
      [cat s e v]
      {:category cat,
       :start (double s),
       :end (double e),
       :value (double v)})
     categories
     starts
     ends
     values)
    y-min
    (min 0.0 (apply min (concat starts ends)))
    y-max
    (apply max (concat starts ends))]
   {:waterfall-bars bars,
    :categories categories,
    :x-domain categories,
    :y-domain [y-min y-max]})))


(def
 v7_l71
 (stat/compute-stat
  {:stat :waterfall,
   :data (tc/dataset pnl-data),
   :x :category,
   :y :amount,
   :x-type :categorical}))


(deftest
 t8_l73
 (is
  ((fn
    [m]
    (and
     (= 6 (count (:waterfall-bars m)))
     (= 500.0 (:end (first (:waterfall-bars m))))
     (=
      ["Revenue" "COGS" "Gross Profit" "OpEx" "Tax" "Net Income"]
      (:categories m))))
   v7_l71)))


(def
 v10_l86
 (defmethod
  extract/extract-layer
  :waterfall
  [draft-layer stat all-colors cfg]
  (let
   [bars
    (:waterfall-bars stat)
    green
    [0.2 0.7 0.3 1.0]
    red
    [0.85 0.25 0.25 1.0]]
   {:mark :waterfall,
    :style {:opacity 0.85},
    :categories (:categories stat),
    :bars
    (mapv
     (fn
      [{:keys [category start end value]}]
      {:category category,
       :start start,
       :end end,
       :color (if (>= value 0) green red)})
     bars)})))


(def
 v12_l118
 (defmethod
  mark/layer->membrane
  :waterfall
  [layer ctx]
  (let
   [{:keys [bars style]}
    layer
    {:keys [coord-fn]}
    ctx
    {:keys [opacity]}
    style]
   (vec
    (for
     [[i {:keys [start end color]}]
      (map-indexed vector bars)
      :let
      [[cr cg cb ca]
       color
       place
       (inc i)
       left
       (- place 0.4)
       right
       (+ place 0.4)]]
     (ui/with-color
      [cr cg cb (or opacity ca)]
      (ui/with-style
       :membrane.ui/style-fill
       (ui/path
        (coord-fn left start)
        (coord-fn right start)
        (coord-fn right end)
        (coord-fn left end)))))))))


(def
 v14_l139
 (layer-type/register!
  :waterfall
  {:mark :waterfall,
   :stat :waterfall,
   :doc "Waterfall -- running total with increase/decrease bars."}))


(def
 v16_l150
 (def
  waterfall-bars
  (->
   pnl-data
   (pj/pose :category :amount)
   (pj/lay :waterfall)
   pj/plan
   :panels
   first
   :layers
   first
   :bars)))


(def
 v17_l161
 (->
  pnl-data
  (pj/pose :category :amount)
  (pj/lay :waterfall)
  (pj/options
   {:title "Profit & Loss Waterfall", :width 500, :height 350})
  pj/plot))


(deftest
 t18_l168
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      green
      [0.2 0.7 0.3 1.0]
      red
      [0.85 0.25 0.25 1.0]
      positive-bars
      (mapv (fn* [p1__91151#] (nth waterfall-bars p1__91151#)) [0 2 5])
      negative-bars
      (mapv
       (fn* [p1__91152#] (nth waterfall-bars p1__91152#))
       [1 3 4])]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (every?
       (fn* [p1__91153#] (= green (:color p1__91153#)))
       positive-bars)
      (every?
       (fn* [p1__91154#] (= red (:color p1__91154#)))
       negative-bars))))
   v17_l161)))


(def
 v20_l187
 (->
  pnl-data
  (pj/pose :category :amount)
  (pj/lay :waterfall)
  (pj/coord :flip)
  (pj/options
   {:title "Profit & Loss Waterfall", :width 500, :height 350})
  pj/plot))


(deftest
 t21_l195
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 6 (:polygons s)))) v20_l187)))


(def
 v23_l203
 (defn
  lay-waterfall
  ([pose] (pj/lay pose :waterfall))
  ([data x y] (-> data (pj/pose x y) (pj/lay :waterfall)))
  ([data x y mapping]
   (-> data (pj/pose x y) (pj/lay :waterfall mapping)))))


(def
 v25_l217
 (->
  pnl-data
  (lay-waterfall :category :amount)
  (pj/options {:width 500})
  pj/plot))


(deftest
 t26_l222
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 6 (:polygons s)))) v25_l217)))


(def
 v28_l230
 (defmethod
  stat/compute-stat
  [:waterfall :doc]
  [_]
  "Compute running totals for waterfall bars"))


(def
 v29_l233
 (defmethod
  extract/extract-layer
  [:waterfall :doc]
  [_ _ _ _]
  "Colored bars (green/red) with start/end positions"))


(def
 v30_l236
 (defmethod
  mark/layer->membrane
  [:waterfall :doc]
  [_ _]
  "Filled rectangles positioned by running total"))


(def v31_l239 (pj/stat-doc :waterfall))


(deftest
 t32_l241
 (is
  ((fn [v] (= "Compute running totals for waterfall bars" v))
   v31_l239)))


(def v34_l247 (remove-method stat/compute-stat :waterfall))


(def v35_l248 (remove-method stat/compute-stat [:waterfall :doc]))


(def v36_l249 (remove-method extract/extract-layer :waterfall))


(def v37_l250 (remove-method extract/extract-layer [:waterfall :doc]))


(def v38_l251 (remove-method mark/layer->membrane :waterfall))


(def v39_l252 (remove-method mark/layer->membrane [:waterfall :doc]))


(def
 v40_l253
 (swap!
  @(resolve 'scicloj.plotje.layer-type/registry*)
  dissoc
  :waterfall))


(def v42_l257 (nil? (layer-type/lookup :waterfall)))


(deftest t43_l259 (is ((fn [v] (true? v)) v42_l257)))
