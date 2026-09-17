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
      (filter (fn* [p1__70252#] (= :text (:mark p1__70252#))))
      (mapv (fn* [p1__70253#] (-> p1__70253# :style :align-x))))))
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
       (filter (fn* [p1__70254#] (= :text (:mark p1__70254#))))
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
        (filter (fn* [p1__70255#] (= :text (:mark p1__70255#))))
        first
        :style
        ((fn*
          [p1__70256#]
          (select-keys p1__70256# [:align-x :align-y])))))]
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
  (pj/lay-text {:text :rownames, :dx 0.08})))


(deftest
 t15_l180
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn* [p1__70257#] (first (pj/to-drawing panel p1__70257# 20.0)))]
     (< 8.0 (- (at 2.08) (at 2.0)) 11.0)))
   v14_l176)))


(def
 v17_l193
 (->
  cars
  (pj/lay-point :disp :mpg {:size 5})
  (pj/lay-text {:text :rownames, :dx 0.08})))


(deftest
 t18_l197
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn* [p1__70258#] (first (pj/to-drawing panel p1__70258# 20.0)))]
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
 v26_l247
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x {:value "red"},
    :y 3,
    :align-x :center,
    :dx 0.5,
    :offset-y -10,
    :text "half a band along"})))


(deftest
 t27_l252
 (is
  ((fn
    [fr]
    (and
     (= [nil 0.5] (->> fr pj/plan :panels first :layers (mapv :dx)))
     (=
      (pj/plot
       (->
        {:team ["red" "green" "blue"], :score [3 5 4]}
        (pj/lay-bar :team :score)
        (pj/lay-text
         {:x {:value "red"}, :dx 0.5, :y 4.5, :text "note"})))
      (pj/plot
       (->
        {:team ["red" "green" "blue"], :score [3 5 4]}
        (pj/lay-bar :team :score)
        (pj/lay-text {:x 1.5, :y 4.5, :text "note"}))))
     (=
      (pj/plot
       (->
        {:team ["red" "green" "blue"], :score [3 5 4]}
        (pj/lay-bar :team :score)
        (pj/lay-text
         {:x {:value "green"}, :dx 0.5, :y 4.5, :text "note"})))
      (pj/plot
       (->
        {:team ["red" "green" "blue"], :score [3 5 4]}
        (pj/lay-bar :team :score)
        (pj/lay-text {:x 2.5, :y 4.5, :text "note"}))))))
   v26_l247)))


(def
 v29_l305
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 7.5, :y 4.2, :text "outliers up here"})))


(deftest
 t30_l309
 (is
  ((fn
    [fr]
    (some
     #{"outliers up here"}
     (:texts (pj/svg-summary (pj/plot fr)))))
   v29_l305)))


(def
 v32_l324
 (->
  cars
  (pj/lay-point :wt :mpg)
  (pj/lay-text {:x 5.6, :y :mpg, :text :rownames, :offset-x 6})))


(deftest
 t33_l328
 (is
  ((fn
    [fr]
    (every?
     (set (:texts (pj/svg-summary (pj/plot fr))))
     ["Mazda RX4" "Valiant" "Merc 280C"]))
   v32_l324)))


(def
 v35_l339
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-point {:x 1.5, :y 4.5, :size 6, :color "#cc3311"})
  (pj/lay-text
   {:x 1.5,
    :y 4.5,
    :align-x :center,
    :offset-y -10,
    :text "between two teams"})))


(deftest
 t36_l345
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/plan :panels first)
      panel-entry
      (-> fr pj/frames :panels first)
      at
      (fn [v] (first (pj/to-drawing panel-entry v 4.5)))]
     (and
      (= ["red" "green" "blue"] (:x-domain panel))
      (= ["red" "green" "blue"] (:values (:x-ticks panel)))
      (= (at 1) (at "red"))
      (= (at 2) (at "green"))
      (< (abs (- (at 1.5) (/ (+ (at "red") (at "green")) 2.0))) 1.0E-9)
      (number? (at 0.5))
      (number? (at 3.5))
      (every?
       (fn
        [bad]
        (try
         (at bad)
         false
         (catch
          Exception
          e
          (boolean
           (re-find #"past the ends of this axis" (ex-message e))))))
       [0.4 3.6]))))
   v35_l339)))


(def
 v38_l377
 (->
  {:cohort [2020 2021 2022], :n [3 5 4]}
  (pj/lay-bar :cohort :n {:x-type :categorical})
  (pj/lay-text
   {:x {:value "2021"},
    :y 5.5,
    :align-x :center,
    :text "the 2021 cohort"})))


(deftest
 t39_l382
 (is
  ((fn
    [fr]
    (and
     (some #{"the 2021 cohort"} (:texts (pj/svg-summary (pj/plot fr))))
     (let
      [panel (-> fr pj/frames :panels first)]
      (=
       (first (pj/to-drawing panel "2021" 5.5))
       (first (pj/to-drawing panel 2 5.5))))
     (try
      (->
       {:cohort [2020 2021 2022], :n [3 5 4]}
       (pj/lay-bar :cohort :n {:x-type :categorical})
       (pj/lay-text {:x 2021, :y 5.5, :text "the 2021 cohort"})
       pj/plan)
      false
      (catch
       Exception
       e
       (boolean
        (re-find #"past the ends of this axis" (ex-message e)))))))
   v38_l377)))


(def
 v41_l407
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-band-v {:x-min 1, :x-max 2, :color "#a6cee3", :alpha 0.55})
  (pj/lay-bar :team :score)
  (pj/lay-rule-v {:x-intercept 2, :color "#cc3311", :size 2})))


(deftest
 t42_l412
 (is
  ((fn
    [fr]
    (let
     [panel
      (-> fr pj/frames :panels first)
      at
      (fn [v] (first (pj/to-drawing panel v 4.0)))]
     (and (= (at 1) (at "red")) (= (at 2) (at "green")))))
   v41_l407)))


(def
 v44_l424
 (try
  (->
   {:team ["red" "green" "blue"], :score [3 5 4]}
   (pj/lay-bar :team :score)
   (pj/lay-rule-v {:x-intercept 99})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t45_l430
 (is
  ((fn
    [msg]
    (and
     (re-find #"pj/plan got 99 for :x" msg)
     (re-find #"runs from 0.5 to 3.5" msg)))
   v44_l424)))


(def
 v47_l442
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:in :drawing-area, :x 12, :y 12, :text "n = 150"})))


(deftest
 t48_l446
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
   v47_l442)))


(def
 v50_l462
 (let
  [base
   (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
   in-data
   (pj/lay-text base {:x 12, :y 12, :text "x"})
   in-drawing
   (pj/lay-text base {:in :drawing-area, :x 12, :y 12, :text "x"})
   x-domain
   (fn*
    [p1__70259#]
    (:x-domain (first (:panels (pj/plan p1__70259#)))))]
  {:no-note (x-domain base),
   :note-in-data (x-domain in-data),
   :note-in-drawing (x-domain in-drawing)}))


(deftest
 t51_l471
 (is
  ((fn
    [m]
    (and
     (= (:no-note m) (:note-in-drawing m))
     (not= (:no-note m) (:note-in-data m))))
   v50_l462)))


(def
 v53_l486
 (def
  scatter
  (->
   cars
   (pj/lay-point :wt :mpg)
   (pj/options {:width 620, :height 380}))))


(def v54_l491 scatter)


(def v55_l493 (-> scatter pj/frames kind/pprint))


(def
 v57_l503
 (let
  [panel (-> scatter pj/frames :panels first)]
  {:mazda-rx4-at (pj/to-drawing panel 2.62 21.0),
   :under-the-pointer (pj/to-data panel 300 200),
   :round-trip
   (->> (pj/to-drawing panel 2.62 21.0) (apply pj/to-data panel))}))


(deftest
 t58_l509
 (is
  ((fn
    [m]
    (every?
     true?
     (map
      (fn*
       [p1__70260# p2__70261#]
       (< (abs (- p1__70260# p2__70261#)) 1.0E-9))
      (:round-trip m)
      [2.62 21.0])))
   v57_l503)))


(def
 v60_l525
 (pj/to-drawing
  (-> scatter pj/frames :panels first)
  {:x [2.62 3.44 5.25], :y [21.0 18.1 10.4]}))


(deftest
 t61_l528
 (is ((fn [ds] (= [:x :y] (vec (tc/column-names ds)))) v60_l525)))


(def
 v63_l539
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
      (fn* [p1__70262#] (first (pj/to-drawing panel p1__70262# 20.0)))]
     (- (at (+ lo 0.08)) (at lo))))]
  {:on-weight (shift :wt), :on-displacement (shift :disp)}))


(deftest
 t64_l551
 (is
  ((fn
    [m]
    (and (< 10.0 (:on-weight m) 13.0) (< (:on-displacement m) 0.2)))
   v63_l539)))


(def
 v66_l578
 (let
  [panel
   (->
    {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
     :tickets [462389 181444 163294]}
    (pj/lay-bar :tickets :violation)
    pj/frames
    :panels
    first)]
  {:past-the-ends
   (try
    (pj/to-drawing panel 200000 99)
    (catch Exception e (ex-message e))),
   :band-middle (pj/to-drawing panel 200000 "Over Time Limit"),
   :halfway-up (pj/to-drawing panel 200000 1.5),
   :last-end (pj/to-drawing panel 200000 3.5),
   :outside-a-band (pj/to-data panel 325.0 5.0),
   :first-end (pj/to-drawing panel 200000 0.5),
   :place-read-back
   (->> (pj/to-drawing panel 200000 1.5) (apply pj/to-data panel)),
   :read-back
   (->>
    (pj/to-drawing panel 200000 "Over Time Limit")
    (apply pj/to-data panel)),
   :not-a-category
   (try
    (pj/to-drawing panel 200000 "Double Parked")
    (catch Exception e (ex-message e)))}))


(deftest
 t67_l598
 (is
  ((fn
    [m]
    (and
     (= [200000.0 "Over Time Limit"] (:read-back m))
     (nil? (second (:outside-a-band m)))
     (re-find #"Double Parked" (:not-a-category m))
     (re-find #"Meter Expired" (:not-a-category m))
     (re-find #"past the ends of this axis" (:past-the-ends m))
     (every? number? (concat (:first-end m) (:last-end m)))
     (= "Meter Expired" (second (:place-read-back m)))
     (let
      [mid
       (fn
        [v]
        (second
         (pj/to-drawing
          (->
           {:violation
            ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
            :tickets [462389 181444 163294]}
           (pj/lay-bar :tickets :violation)
           pj/frames
           :panels
           first)
          200000
          v)))]
      (<
       (abs
        (-
         (second (:halfway-up m))
         (/ (+ (mid "Meter Expired") (mid "Over Time Limit")) 2.0)))
       1.0E-9))))
   v66_l578)))


(def
 v69_l634
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
 t70_l646
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
   v69_l634)))


(def
 v72_l661
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
 t73_l670
 (is
  ((fn [fr] (= 8 (:points (pj/svg-summary (pj/plot fr))))) v72_l661)))


(def
 v75_l683
 (let
  [drawing-area
   (fn*
    [p1__70263#]
    (-> p1__70263# pj/frames :panels first :frames :drawing-area))]
  {:untitled (drawing-area scatter),
   :titled
   (drawing-area (pj/options scatter {:title "Motor Trend Cars"}))}))


(deftest
 t76_l687
 (is ((fn [m] (< (last (:titled m)) (last (:untitled m)))) v75_l683)))


(def
 v78_l696
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
 t79_l700
 (is
  ((fn
    [fr]
    (let
     [drawing-area
      (fn*
       [p1__70264#]
       (-> p1__70264# pj/frames :panels first :frames :drawing-area))]
     (and
      (= 6 (tc/row-count cars))
      (some #{"n = 6"} (:texts (pj/svg-summary (pj/plot fr))))
      (= (drawing-area scatter) (drawing-area fr)))))
   v78_l696)))


(def
 v81_l723
 (def
  tickets-by-violation
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}))


(def
 v82_l727
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})))


(def
 v84_l735
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/options {:fit-text-domain false})))


(def
 v86_l742
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
 t87_l755
 (is ((fn [m] (> (:fitted m) (:unfitted m))) v86_l742)))


(def
 v89_l761
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
 t90_l773
 (is ((fn [m] (= (:fitted m) (:unfitted m))) v89_l761)))


(def
 v92_l781
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/scale :x {:domain [0 500000]})
  pj/plan
  :panels
  first
  :x-domain))


(deftest t93_l790 (is ((fn [d] (= [0 500000] d)) v92_l781)))


(def
 v95_l801
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
 t96_l812
 (is
  ((fn
    [fr]
    (and
     (some
      #{"heaviest car in the set"}
      (:texts (pj/svg-summary (pj/plot fr))))
     (= 5.424 (apply max ((rdatasets/datasets-mtcars) :wt)))))
   v95_l801)))
