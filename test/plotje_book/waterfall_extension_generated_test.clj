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
 v7_l72
 (stat/compute-stat
  {:stat :waterfall,
   :data (tc/dataset pnl-data),
   :x :category,
   :y :amount,
   :x-type :categorical}))


(deftest
 t8_l74
 (is
  ((fn
    [m]
    (and
     (= 6 (count (:waterfall-bars m)))
     (= 500.0 (:end (first (:waterfall-bars m))))
     (=
      ["Revenue" "COGS" "Gross Profit" "OpEx" "Tax" "Net Income"]
      (:categories m))))
   v7_l72)))


(def
 v10_l87
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
 v12_l113
 (defmethod
  mark/layer->membrane
  :waterfall
  [layer ctx]
  (let
   [{:keys [bars style]}
    layer
    {:keys [sx sy coord-fn]}
    ctx
    {:keys [opacity]}
    style
    sample-info
    (sx (-> bars first :category) true)
    bw
    (- (:rend sample-info) (:rstart sample-info))
    w
    (* 0.8 bw)]
   (vec
    (for
     [{:keys [category start end color]}
      bars
      :let
      [[cr cg cb ca]
       color
       band
       (sx category true)
       mid-x
       (:point band)
       py-start
       (double (sy start))
       py-end
       (double (sy end))
       top
       (min py-start py-end)
       bot
       (max py-start py-end)
       x0
       (- mid-x (/ w 2.0))
       x1
       (+ mid-x (/ w 2.0))]]
     (ui/with-color
      [cr cg cb (or opacity ca)]
      (ui/with-style
       :membrane.ui/style-fill
       (ui/path [x0 top] [x1 top] [x1 bot] [x0 bot]))))))))


(def
 v14_l142
 (layer-type/register!
  :waterfall
  {:mark :waterfall,
   :stat :waterfall,
   :doc "Waterfall -- running total with increase/decrease bars."}))


(def
 v16_l157
 (def
  waterfall-bars
  (->
   pnl-data
   (pj/pose :category :amount)
   (pj/lay (layer-type/lookup :waterfall))
   pj/plan
   :panels
   first
   :layers
   first
   :bars)))


(def
 v17_l164
 (->
  pnl-data
  (pj/pose :category :amount)
  (pj/lay (layer-type/lookup :waterfall))
  (pj/options
   {:title "Profit & Loss Waterfall", :width 500, :height 350})
  pj/plot))


(deftest
 t18_l171
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
      (mapv (fn* [p1__89659#] (nth waterfall-bars p1__89659#)) [0 2 5])
      negative-bars
      (mapv
       (fn* [p1__89660#] (nth waterfall-bars p1__89660#))
       [1 3 4])]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (every?
       (fn* [p1__89661#] (= green (:color p1__89661#)))
       positive-bars)
      (every?
       (fn* [p1__89662#] (= red (:color p1__89662#)))
       negative-bars))))
   v17_l164)))


(def
 v20_l192
 (defn
  lay-waterfall
  ([pose] (pj/lay pose (layer-type/lookup :waterfall)))
  ([data x y]
   (-> data (pj/pose x y) (pj/lay (layer-type/lookup :waterfall))))
  ([data x y opts]
   (->
    data
    (pj/pose x y)
    (pj/lay (merge (layer-type/lookup :waterfall) opts))))))


(def
 v22_l199
 (->
  pnl-data
  (lay-waterfall :category :amount)
  (pj/options {:title "Quarterly Cash Flow", :width 500})
  pj/plot))


(deftest
 t23_l204
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 6 (:polygons s)))) v22_l199)))


(def
 v25_l212
 (defmethod
  stat/compute-stat
  [:waterfall :doc]
  [_]
  "Compute running totals for waterfall bars"))


(def
 v26_l215
 (defmethod
  extract/extract-layer
  [:waterfall :doc]
  [_ _ _ _]
  "Colored bars (green/red) with start/end positions"))


(def
 v27_l218
 (defmethod
  mark/layer->membrane
  [:waterfall :doc]
  [_ _]
  "Filled rectangles positioned by running total"))


(def v28_l221 (pj/stat-doc :waterfall))


(deftest
 t29_l223
 (is
  ((fn [v] (= "Compute running totals for waterfall bars" v))
   v28_l221)))


(def v31_l229 (remove-method stat/compute-stat :waterfall))


(def v32_l230 (remove-method stat/compute-stat [:waterfall :doc]))


(def v33_l231 (remove-method extract/extract-layer :waterfall))


(def v34_l232 (remove-method extract/extract-layer [:waterfall :doc]))


(def v35_l233 (remove-method mark/layer->membrane :waterfall))


(def v36_l234 (remove-method mark/layer->membrane [:waterfall :doc]))


(def
 v37_l235
 (swap!
  @(resolve 'scicloj.plotje.layer-type/registry*)
  dissoc
  :waterfall))


(def v39_l239 (nil? (layer-type/lookup :waterfall)))


(deftest t40_l241 (is ((fn [v] (true? v)) v39_l239)))
