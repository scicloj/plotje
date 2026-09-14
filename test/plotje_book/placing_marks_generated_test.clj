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
      (filter (fn* [p1__75642#] (= :text (:mark p1__75642#))))
      (mapv (fn* [p1__75643#] (-> p1__75643# :style :align-x))))))
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
       (filter (fn* [p1__75644#] (= :text (:mark p1__75644#))))
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
        (filter (fn* [p1__75645#] (= :text (:mark p1__75645#))))
        first
        :style
        ((fn*
          [p1__75646#]
          (select-keys p1__75646# [:align-x :align-y])))))]
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
      (fn* [p1__75647#] (first (pj/to-drawing panel p1__75647# 20.0)))]
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
      (fn* [p1__75648#] (first (pj/to-drawing panel p1__75648# 20.0)))]
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
 v23_l227
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score)
  (pj/lay-text {:text :score, :align-x :center, :offset-y -6})))


(deftest
 t24_l231
 (is
  ((fn
    [fr]
    (=
     [nil -6]
     (->> fr pj/plan :panels first :layers (mapv :offset-y))))
   v23_l227)))


(def
 v26_l242
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score)
  (pj/lay-text {:text :score, :align-x :center, :nudge-x 0.5})))


(deftest
 t27_l246
 (is
  ((fn
    [fr]
    (=
     [nil 0.5]
     (->> fr pj/plan :panels first :layers (mapv :nudge-x))))
   v26_l242)))


(def
 v29_l276
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 7.5, :y 4.2, :text "outliers up here"})))


(deftest
 t30_l280
 (is
  ((fn
    [fr]
    (some
     #{"outliers up here"}
     (:texts (pj/svg-summary (pj/plot fr)))))
   v29_l276)))


(def
 v32_l295
 (->
  cars
  (pj/lay-point :wt :mpg)
  (pj/lay-text {:x 5.6, :y :mpg, :text :rownames, :offset-x 6})))


(deftest
 t33_l299
 (is
  ((fn
    [fr]
    (every?
     (set (:texts (pj/svg-summary (pj/plot fr))))
     ["Mazda RX4" "Valiant" "Merc 280C"]))
   v32_l295)))


(def
 v35_l311
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:in :drawing-area, :x 12, :y 12, :text "n = 150"})))


(deftest
 t36_l315
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
   v35_l311)))


(def
 v38_l331
 (let
  [base
   (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
   in-data
   (pj/lay-text base {:x 12, :y 12, :text "x"})
   in-drawing
   (pj/lay-text base {:in :drawing-area, :x 12, :y 12, :text "x"})
   x-domain
   (fn*
    [p1__75649#]
    (:x-domain (first (:panels (pj/plan p1__75649#)))))]
  {:no-note (x-domain base),
   :note-in-data (x-domain in-data),
   :note-in-drawing (x-domain in-drawing)}))


(deftest
 t39_l340
 (is
  ((fn
    [m]
    (and
     (= (:no-note m) (:note-in-drawing m))
     (not= (:no-note m) (:note-in-data m))))
   v38_l331)))


(def
 v41_l355
 (def
  scatter
  (->
   cars
   (pj/lay-point :wt :mpg)
   (pj/options {:width 620, :height 380}))))


(def v42_l360 scatter)


(def v43_l362 (-> scatter pj/frames kind/pprint))


(def
 v45_l372
 (let
  [panel (-> scatter pj/frames :panels first)]
  {:mazda-rx4-at (pj/to-drawing panel 2.62 21.0),
   :under-the-pointer (pj/to-data panel 300 200),
   :round-trip
   (->> (pj/to-drawing panel 2.62 21.0) (apply pj/to-data panel))}))


(deftest
 t46_l378
 (is
  ((fn
    [m]
    (every?
     true?
     (map
      (fn*
       [p1__75650# p2__75651#]
       (< (abs (- p1__75650# p2__75651#)) 1.0E-9))
      (:round-trip m)
      [2.62 21.0])))
   v45_l372)))


(def
 v48_l394
 (pj/to-drawing
  (-> scatter pj/frames :panels first)
  {:x [2.62 3.44 5.25], :y [21.0 18.1 10.4]}))


(deftest
 t49_l397
 (is ((fn [ds] (= [:x :y] (vec (tc/column-names ds)))) v48_l394)))


(def
 v51_l408
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
      (fn* [p1__75652#] (first (pj/to-drawing panel p1__75652# 20.0)))]
     (- (at (+ lo 0.08)) (at lo))))]
  {:on-weight (shift :wt), :on-displacement (shift :disp)}))


(deftest
 t52_l420
 (is
  ((fn
    [m]
    (and (< 10.0 (:on-weight m) 13.0) (< (:on-displacement m) 0.2)))
   v51_l408)))


(def
 v54_l440
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
 t55_l453
 (is
  ((fn
    [m]
    (and
     (= [200000.0 "Over Time Limit"] (:read-back m))
     (nil? (second (:outside-a-band m)))
     (re-find #"Double Parked" (:not-a-category m))
     (re-find #"Meter Expired" (:not-a-category m))))
   v54_l440)))


(def
 v57_l465
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
 t58_l477
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
   v57_l465)))


(def
 v60_l492
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
 t61_l501
 (is
  ((fn [fr] (= 8 (:points (pj/svg-summary (pj/plot fr))))) v60_l492)))


(def
 v63_l514
 (let
  [drawing-area
   (fn*
    [p1__75653#]
    (-> p1__75653# pj/frames :panels first :frames :drawing-area))]
  {:untitled (drawing-area scatter),
   :titled
   (drawing-area (pj/options scatter {:title "Motor Trend Cars"}))}))


(deftest
 t64_l518
 (is ((fn [m] (< (last (:titled m)) (last (:untitled m)))) v63_l514)))


(def
 v66_l527
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
 t67_l531
 (is
  ((fn
    [fr]
    (let
     [drawing-area
      (fn*
       [p1__75654#]
       (-> p1__75654# pj/frames :panels first :frames :drawing-area))]
     (and
      (= 6 (tc/row-count cars))
      (some #{"n = 6"} (:texts (pj/svg-summary (pj/plot fr))))
      (= (drawing-area scatter) (drawing-area fr)))))
   v66_l527)))


(def
 v69_l554
 (def
  tickets-by-violation
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}))


(def
 v70_l558
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})))


(def
 v72_l566
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/options {:fit-text-domain false})))


(def
 v74_l573
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
 t75_l586
 (is ((fn [m] (> (:fitted m) (:unfitted m))) v74_l573)))


(def
 v77_l592
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
 t78_l604
 (is ((fn [m] (= (:fitted m) (:unfitted m))) v77_l592)))


(def
 v80_l612
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/scale :x {:domain [0 500000]})
  pj/plan
  :panels
  first
  :x-domain))


(deftest t81_l621 (is ((fn [d] (= [0 500000] d)) v80_l612)))


(def
 v83_l632
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
 t84_l643
 (is
  ((fn
    [fr]
    (and
     (some
      #{"heaviest car in the set"}
      (:texts (pj/svg-summary (pj/plot fr))))
     (= 5.424 (apply max ((rdatasets/datasets-mtcars) :wt)))))
   v83_l632)))
