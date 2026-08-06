(ns
 plotje-book.placing-marks-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [tech.v3.datatype.functional :as dfn]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l79
 (->
  {:hour [2 2 2], :level [3 2 1]}
  (pj/lay-point :hour :level {:size 6, :color "#888888"})
  (pj/lay-text
   :hour
   :level
   {:text :tag,
    :align-x :left,
    :data {:hour [2], :level [3], :tag ["align-x :left"]}})
  (pj/lay-text
   :hour
   :level
   {:text :tag,
    :align-x :center,
    :data {:hour [2], :level [2], :tag ["align-x :center"]}})
  (pj/lay-text
   :hour
   :level
   {:text :tag,
    :align-x :right,
    :data {:hour [2], :level [1], :tag ["align-x :right"]}})))


(deftest
 t4_l91
 (is
  ((fn
    [fr]
    (=
     [:left :center :right]
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      (filter (fn* [p1__82792#] (= :text (:mark p1__82792#))))
      (mapv (fn* [p1__82793#] (-> p1__82793# :style :align-x))))))
   v3_l79)))


(def
 v6_l102
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :align-y :bottom})))


(deftest
 t8_l111
 (is
  ((fn
    [fr]
    (let
     [style-of
      (->>
       fr
       pj/plan
       :panels
       first
       :layers
       (filter (fn* [p1__82794#] (= :text (:mark p1__82794#))))
       first
       :style)
      text-style
      (fn
       [layer-fn opts]
       (->>
        (->
         {:x [1], :y [1], :t ["a"]}
         (layer-fn :x :y (merge {:text :t} opts)))
        pj/plan
        :panels
        first
        :layers
        (filter (fn* [p1__82795#] (= :text (:mark p1__82795#))))
        first
        :style
        ((fn*
          [p1__82796#]
          (select-keys p1__82796# [:align-x :align-y])))))]
     (and
      (= :center (:align-x style-of))
      (= :bottom (:align-y style-of))
      (=
       {:align-x :left, :align-y :center}
       (text-style pj/lay-text {}))
      (= :left (:align-x (text-style pj/lay-text {:align-x :left})))
      (=
       :center
       (:align-x (text-style pj/lay-text {:align-x :center})))
      (= :right (:align-x (text-style pj/lay-text {:align-x :right})))
      (= :top (:align-y (text-style pj/lay-text {:align-y :top})))
      (=
       :center
       (:align-y (text-style pj/lay-text {:align-y :center})))
      (=
       :bottom
       (:align-y (text-style pj/lay-text {:align-y :bottom})))
      (=
       {:align-x :right, :align-y :top}
       (text-style pj/lay-label {:align-x :right, :align-y :top}))
      (try
       (text-style pj/lay-text {:align-x :middle})
       false
       (catch Exception _ true)))))
   v6_l102)))


(def
 v10_l158
 (def
  cars
  (->
   (rdatasets/datasets-mtcars)
   (tc/select-rows (range 0 30 5))
   (tc/select-columns [:rownames :wt :mpg :disp]))))


(def v11_l163 cars)


(deftest
 t12_l165
 (is
  ((fn
    [ds]
    (and
     (= 6 (tc/row-count ds))
     (= [1.935 5.424] [(apply min (ds :wt)) (apply max (ds :wt))])
     (= [79.0 460.0] [(apply min (ds :disp)) (apply max (ds :disp))])))
   v11_l163)))


(def
 v13_l170
 (->
  cars
  (pj/lay-point :wt :mpg {:size 5})
  (pj/lay-text {:text :rownames, :nudge-x 0.08})))


(deftest
 t14_l174
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn* [p1__82797#] (first (pj/to-drawing panel p1__82797# 20.0)))]
     (< 8.0 (- (at 2.08) (at 2.0)) 11.0)))
   v13_l170)))


(def
 v16_l187
 (->
  cars
  (pj/lay-point :disp :mpg {:size 5})
  (pj/lay-text {:text :rownames, :nudge-x 0.08})))


(deftest
 t17_l191
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn* [p1__82798#] (first (pj/to-drawing panel p1__82798# 20.0)))]
     (< (- (at 79.08) (at 79.0)) 0.2)))
   v16_l187)))


(def
 v19_l202
 (->
  cars
  (pj/lay-point :disp :mpg {:size 5})
  (pj/lay-text {:text :rownames, :offset-x 10})))


(deftest
 t20_l206
 (is
  ((fn
    [fr]
    (=
     [nil 10]
     (->> fr pj/plan :panels first :layers (mapv :offset-x))))
   v19_l202)))


(def
 v22_l215
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score)
  (pj/lay-text {:text :score, :align-x :center, :offset-y -6})))


(deftest
 t23_l219
 (is
  ((fn
    [fr]
    (=
     [nil -6]
     (->> fr pj/plan :panels first :layers (mapv :offset-y))))
   v22_l215)))


(def
 v25_l248
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 7.5, :y 4.2, :text "outliers up here"})))


(deftest
 t26_l252
 (is
  ((fn
    [fr]
    (some
     #{"outliers up here"}
     (:texts (pj/svg-summary (pj/plot fr)))))
   v25_l248)))


(def
 v28_l267
 (->
  cars
  (pj/lay-point :wt :mpg)
  (pj/lay-text {:x 5.6, :y :mpg, :text :rownames, :offset-x 6})))


(deftest
 t29_l271
 (is
  ((fn
    [fr]
    (every?
     (set (:texts (pj/svg-summary (pj/plot fr))))
     ["Mazda RX4" "Valiant" "Merc 280C"]))
   v28_l267)))


(def
 v31_l283
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:in :drawing-area, :x 12, :y 12, :text "n = 150"})))


(deftest
 t32_l287
 (is
  ((fn
    [fr]
    (let
     [dom (fn [p] (:x-domain (first (:panels (pj/plan p)))))]
     (and
      (= 150 (tc/row-count (rdatasets/datasets-iris)))
      (=
       (dom
        (pj/lay-point
         (rdatasets/datasets-iris)
         :sepal-length
         :sepal-width))
       (dom fr)))))
   v31_l283)))


(def
 v34_l302
 (let
  [base
   (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
   in-data
   (pj/lay-text base {:x 12, :y 12, :text "x"})
   in-drawing
   (pj/lay-text base {:in :drawing-area, :x 12, :y 12, :text "x"})
   x-domain
   (fn*
    [p1__82799#]
    (:x-domain (first (:panels (pj/plan p1__82799#)))))]
  {:no-note (x-domain base),
   :note-in-data (x-domain in-data),
   :note-in-drawing (x-domain in-drawing)}))


(deftest
 t35_l311
 (is
  ((fn
    [m]
    (and
     (= (:no-note m) (:note-in-drawing m))
     (not= (:no-note m) (:note-in-data m))))
   v34_l302)))


(def
 v37_l326
 (def
  scatter
  (->
   cars
   (pj/lay-point :wt :mpg)
   (pj/options {:width 620, :height 380}))))


(def v38_l331 (-> scatter pj/frames kind/pprint))


(def
 v40_l341
 (let
  [panel (-> scatter pj/frames :panels first)]
  {:mazda-rx4-at (pj/to-drawing panel 2.62 21.0),
   :under-the-pointer (pj/to-data panel 300 200),
   :round-trip
   (->> (pj/to-drawing panel 2.62 21.0) (apply pj/to-data panel))}))


(deftest
 t41_l347
 (is
  ((fn
    [m]
    (every?
     true?
     (map
      (fn*
       [p1__82800# p2__82801#]
       (< (abs (- p1__82800# p2__82801#)) 1.0E-9))
      (:round-trip m)
      [2.62 21.0])))
   v40_l341)))


(def
 v43_l363
 (pj/to-drawing
  (-> scatter pj/frames :panels first)
  {:x [2.62 3.44 5.25], :y [21.0 18.1 10.4]}))


(deftest
 t44_l366
 (is ((fn [ds] (= [:x :y] (vec (tc/column-names ds)))) v43_l363)))


(def
 v46_l377
 (let
  [shift
   (fn
    [column]
    (let
     [panel
      (-> cars (pj/lay-point column :mpg) pj/frames :panels first)
      lo
      (apply min (cars column))
      at
      (fn* [p1__82802#] (first (pj/to-drawing panel p1__82802# 20.0)))]
     (- (at (+ lo 0.08)) (at lo))))]
  {:on-weight (shift :wt), :on-displacement (shift :disp)}))


(deftest
 t47_l389
 (is
  ((fn
    [m]
    (and (< 10.0 (:on-weight m) 13.0) (< (:on-displacement m) 0.2)))
   v46_l377)))


(def
 v49_l404
 (let
  [panel
   (-> scatter pj/frames :panels first)
   [dax day]
   (-> panel :frames :drawing-area)
   canvas
   (pj/to-drawing panel {:x [2.62 5.424], :y [21.0 10.4]})]
  (pj/lay-point
   scatter
   {:in :drawing-area,
    :data {:x (dfn/- (canvas :x) dax), :y (dfn/- (canvas :y) day)},
    :x :x,
    :y :y,
    :color "#cc3311",
    :size 7})))


(deftest
 t50_l413
 (is
  ((fn [fr] (= 8 (:points (pj/svg-summary (pj/plot fr))))) v49_l404)))


(def
 v52_l426
 (let
  [drawing-area
   (fn*
    [p1__82803#]
    (-> p1__82803# pj/frames :panels first :frames :drawing-area))]
  {:untitled (drawing-area scatter),
   :titled
   (drawing-area (pj/options scatter {:title "Motor Trend Cars"}))}))


(deftest
 t53_l430
 (is ((fn [m] (< (last (:titled m)) (last (:untitled m)))) v52_l426)))


(def
 v55_l439
 (let
  [[_ _ _ h]
   (-> scatter pj/frames :panels first :frames :drawing-area)]
  (pj/lay-text
   scatter
   {:in :drawing-area,
    :x 12,
    :y (- h 16),
    :text "n = 6",
    :align-x :left,
    :color "#555555"})))


(deftest
 t56_l443
 (is
  ((fn
    [fr]
    (let
     [drawing-area
      (fn*
       [p1__82804#]
       (-> p1__82804# pj/frames :panels first :frames :drawing-area))]
     (and
      (= 6 (tc/row-count cars))
      (some #{"n = 6"} (:texts (pj/svg-summary (pj/plot fr))))
      (= (drawing-area scatter) (drawing-area fr)))))
   v55_l439)))


(def
 v58_l466
 (def
  tickets-by-violation
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}))


(def
 v59_l470
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})))


(def
 v61_l478
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/options {:fit-text-domain false})))


(def
 v63_l485
 (let
  [top-end
   (fn
    [opts]
    (->
     tickets-by-violation
     (pj/lay-bar :tickets :violation)
     (pj/lay-label :tickets :violation {:text :tickets})
     (pj/options opts)
     pj/plan
     :panels
     first
     :x-domain
     second))]
  {:fitted (top-end {}), :unfitted (top-end {:fit-text-domain false})}))


(deftest
 t64_l498
 (is ((fn [m] (> (:fitted m) (:unfitted m))) v63_l485)))


(def
 v66_l504
 (let
  [top-end
   (fn
    [opts]
    (->
     tickets-by-violation
     (pj/lay-bar :tickets :violation)
     (pj/options opts)
     pj/plan
     :panels
     first
     :x-domain
     second))]
  {:fitted (top-end {}), :unfitted (top-end {:fit-text-domain false})}))


(deftest
 t67_l516
 (is ((fn [m] (= (:fitted m) (:unfitted m))) v66_l504)))


(def
 v69_l524
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/scale :x {:domain [0 500000]})
  pj/plan
  :panels
  first
  :x-domain))


(deftest t70_l533 (is ((fn [d] (= [0 500000] d)) v69_l524)))


(def
 v72_l544
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :wt :mpg {:color "#bbbbbb"})
  (pj/lay-point
   {:data {:wt [5.25], :mpg [10.4]},
    :x :wt,
    :y :mpg,
    :color "#cc3311",
    :size 6})
  (pj/lay-line
   {:data {:wt [4.3 5.15], :mpg [13.5 10.8]},
    :x :wt,
    :y :mpg,
    :color "#777777",
    :stroke-dash :dotted})
  (pj/lay-text
   {:x 4.25,
    :y 13.7,
    :align-x :right,
    :offset-x -4,
    :color "#333333",
    :text "heaviest car in the set"})))


(deftest
 t73_l555
 (is
  ((fn
    [fr]
    (some
     #{"heaviest car in the set"}
     (:texts (pj/svg-summary (pj/plot fr)))))
   v72_l544)))
