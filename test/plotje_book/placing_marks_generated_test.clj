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
      (filter (fn* [p1__89532#] (= :text (:mark p1__89532#))))
      (mapv (fn* [p1__89533#] (-> p1__89533# :style :align-x))))))
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
       (filter (fn* [p1__89534#] (= :text (:mark p1__89534#))))
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
        (filter (fn* [p1__89535#] (= :text (:mark p1__89535#))))
        first
        :style
        ((fn*
          [p1__89536#]
          (select-keys p1__89536# [:align-x :align-y])))))]
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
 v10_l160
 (def
  cars
  (->
   (rdatasets/datasets-mtcars)
   (tc/select-rows (range 0 30 5))
   (tc/select-columns [:rownames :wt :mpg :disp]))))


(def v11_l165 cars)


(deftest
 t12_l167
 (is
  ((fn
    [ds]
    (and
     (= 6 (tc/row-count ds))
     (= [1.935 5.424] [(apply min (ds :wt)) (apply max (ds :wt))])
     (= [79.0 460.0] [(apply min (ds :disp)) (apply max (ds :disp))])))
   v11_l165)))


(def
 v14_l176
 (->
  cars
  (pj/lay-point :wt :mpg {:size 5})
  (pj/lay-text {:text :rownames, :nudge-x 0.08})))


(deftest
 t15_l180
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn* [p1__89537#] (first (pj/to-drawing panel p1__89537# 20.0)))]
     (< 8.0 (- (at 2.08) (at 2.0)) 11.0)))
   v14_l176)))


(def
 v17_l193
 (->
  cars
  (pj/lay-point :disp :mpg {:size 5})
  (pj/lay-text {:text :rownames, :nudge-x 0.08})))


(deftest
 t18_l197
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn* [p1__89538#] (first (pj/to-drawing panel p1__89538# 20.0)))]
     (and
      (=
       [79.0 460.0]
       [(reduce min (cars :disp)) (reduce max (cars :disp))])
      (=
       [1.935 5.424]
       [(reduce min (cars :wt)) (reduce max (cars :wt))])
      (< (- (at 79.08) (at 79.0)) 0.2))))
   v17_l193)))


(def
 v20_l212
 (->
  cars
  (pj/lay-point :disp :mpg {:size 5})
  (pj/lay-text {:text :rownames, :offset-x 10})))


(deftest
 t21_l216
 (is
  ((fn
    [fr]
    (=
     [nil 10]
     (->> fr pj/plan :panels first :layers (mapv :offset-x))))
   v20_l212)))


(def
 v23_l228
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score)
  (pj/lay-text {:text :score, :align-x :center, :offset-y -6})))


(deftest
 t24_l232
 (is
  ((fn
    [fr]
    (and
     (=
      [nil -6]
      (->> fr pj/plan :panels first :layers (mapv :offset-y)))
     (try
      (->
       {:team ["red" "green" "blue"], :score [3 5 4]}
       (pj/lay-bar :team :score)
       (pj/lay-text {:text :score, :nudge-x 0.2})
       pj/plot)
      false
      (catch
       Exception
       e
       (boolean
        (re-find #":nudge-x is a data-space shift" (ex-message e)))))))
   v23_l228)))


(def
 v26_l271
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 7.5, :y 4.2, :text "outliers up here"})))


(deftest
 t27_l275
 (is
  ((fn
    [fr]
    (some
     #{"outliers up here"}
     (:texts (pj/svg-summary (pj/plot fr)))))
   v26_l271)))


(def
 v29_l290
 (->
  cars
  (pj/lay-point :wt :mpg)
  (pj/lay-text {:x 5.6, :y :mpg, :text :rownames, :offset-x 6})))


(deftest
 t30_l294
 (is
  ((fn
    [fr]
    (every?
     (set (:texts (pj/svg-summary (pj/plot fr))))
     ["Mazda RX4" "Valiant" "Merc 280C"]))
   v29_l290)))


(def
 v32_l306
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:in :drawing-area, :x 12, :y 12, :text "n = 150"})))


(deftest
 t33_l310
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
   v32_l306)))


(def
 v35_l326
 (let
  [base
   (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
   in-data
   (pj/lay-text base {:x 12, :y 12, :text "x"})
   in-drawing
   (pj/lay-text base {:in :drawing-area, :x 12, :y 12, :text "x"})
   x-domain
   (fn*
    [p1__89539#]
    (:x-domain (first (:panels (pj/plan p1__89539#)))))]
  {:no-note (x-domain base),
   :note-in-data (x-domain in-data),
   :note-in-drawing (x-domain in-drawing)}))


(deftest
 t36_l335
 (is
  ((fn
    [m]
    (and
     (= (:no-note m) (:note-in-drawing m))
     (not= (:no-note m) (:note-in-data m))))
   v35_l326)))


(def
 v38_l350
 (def
  scatter
  (->
   cars
   (pj/lay-point :wt :mpg)
   (pj/options {:width 620, :height 380}))))


(def v39_l355 scatter)


(def v40_l357 (-> scatter pj/frames kind/pprint))


(def
 v42_l367
 (let
  [panel (-> scatter pj/frames :panels first)]
  {:mazda-rx4-at (pj/to-drawing panel 2.62 21.0),
   :under-the-pointer (pj/to-data panel 300 200),
   :round-trip
   (->> (pj/to-drawing panel 2.62 21.0) (apply pj/to-data panel))}))


(deftest
 t43_l373
 (is
  ((fn
    [m]
    (every?
     true?
     (map
      (fn*
       [p1__89540# p2__89541#]
       (< (abs (- p1__89540# p2__89541#)) 1.0E-9))
      (:round-trip m)
      [2.62 21.0])))
   v42_l367)))


(def
 v45_l389
 (pj/to-drawing
  (-> scatter pj/frames :panels first)
  {:x [2.62 3.44 5.25], :y [21.0 18.1 10.4]}))


(deftest
 t46_l392
 (is ((fn [ds] (= [:x :y] (vec (tc/column-names ds)))) v45_l389)))


(def
 v48_l403
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
      (fn* [p1__89542#] (first (pj/to-drawing panel p1__89542# 20.0)))]
     (- (at (+ lo 0.08)) (at lo))))]
  {:on-weight (shift :wt), :on-displacement (shift :disp)}))


(deftest
 t49_l415
 (is
  ((fn
    [m]
    (and (< 10.0 (:on-weight m) 13.0) (< (:on-displacement m) 0.2)))
   v48_l403)))


(def
 v51_l435
 (let
  [panel
   (->
    {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
     :tickets [462389 181444 163294]}
    (pj/lay-bar :tickets :violation)
    pj/frames
    :panels
    first)]
  {:band-middle (pj/to-drawing panel 200000 "Over Time Limit"),
   :read-back
   (->>
    (pj/to-drawing panel 200000 "Over Time Limit")
    (apply pj/to-data panel)),
   :outside-a-band (pj/to-data panel 325.0 5.0),
   :not-a-category
   (try
    (pj/to-drawing panel 200000 "Double Parked")
    (catch Exception e (ex-message e)))}))


(deftest
 t52_l448
 (is
  ((fn
    [m]
    (and
     (= [200000.0 "Over Time Limit"] (:read-back m))
     (nil? (second (:outside-a-band m)))
     (re-find #"Double Parked" (:not-a-category m))
     (re-find #"Meter Expired" (:not-a-category m))))
   v51_l435)))


(def
 v54_l460
 (let
  [panel
   (->
    {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
     :tickets [462389 181444 163294]}
    (pj/lay-bar :violation :tickets)
    (pj/coord :flip)
    pj/frames
    :panels
    first)]
  {:x-domain (:x-domain panel),
   :y-domain (:y-domain panel),
   :read-back
   (pj/to-data
    panel
    (pj/to-drawing panel {:x ["Over Time Limit"], :y [200000]}))}))


(deftest
 t55_l472
 (is
  ((fn
    [m]
    (and
     (=
      ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
      (:y-domain m))
     (number? (first (:x-domain m)))
     (= ["Over Time Limit"] (vec ((:read-back m) :x)))
     (< (abs (- 200000.0 (first ((:read-back m) :y)))) 1.0E-6)))
   v54_l460)))


(def
 v57_l487
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
 t58_l496
 (is
  ((fn [fr] (= 8 (:points (pj/svg-summary (pj/plot fr))))) v57_l487)))


(def
 v60_l509
 (let
  [drawing-area
   (fn*
    [p1__89543#]
    (-> p1__89543# pj/frames :panels first :frames :drawing-area))]
  {:untitled (drawing-area scatter),
   :titled
   (drawing-area (pj/options scatter {:title "Motor Trend Cars"}))}))


(deftest
 t61_l513
 (is ((fn [m] (< (last (:titled m)) (last (:untitled m)))) v60_l509)))


(def
 v63_l522
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
 t64_l526
 (is
  ((fn
    [fr]
    (let
     [drawing-area
      (fn*
       [p1__89544#]
       (-> p1__89544# pj/frames :panels first :frames :drawing-area))]
     (and
      (= 6 (tc/row-count cars))
      (some #{"n = 6"} (:texts (pj/svg-summary (pj/plot fr))))
      (= (drawing-area scatter) (drawing-area fr)))))
   v63_l522)))


(def
 v66_l549
 (def
  tickets-by-violation
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}))


(def
 v67_l553
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})))


(def
 v69_l561
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/options {:fit-text-domain false})))


(def
 v71_l568
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
 t72_l581
 (is ((fn [m] (> (:fitted m) (:unfitted m))) v71_l568)))


(def
 v74_l587
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
 t75_l599
 (is ((fn [m] (= (:fitted m) (:unfitted m))) v74_l587)))


(def
 v77_l607
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/scale :x {:domain [0 500000]})
  pj/plan
  :panels
  first
  :x-domain))


(deftest t78_l616 (is ((fn [d] (= [0 500000] d)) v77_l607)))


(def
 v80_l627
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :wt :mpg {:color "#bbbbbb"})
  (pj/lay-point
   {:data {:wt [5.424], :mpg [10.4]},
    :x :wt,
    :y :mpg,
    :color "#cc3311",
    :size 6})
  (pj/lay-line
   {:data {:wt [4.3 5.32], :mpg [13.5 10.8]},
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
 t81_l638
 (is
  ((fn
    [fr]
    (and
     (some
      #{"heaviest car in the set"}
      (:texts (pj/svg-summary (pj/plot fr))))
     (= 5.424 (apply max ((rdatasets/datasets-mtcars) :wt)))))
   v80_l627)))
