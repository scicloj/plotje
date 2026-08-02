(ns
 plotje-book.customization-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure2d.color :as c2d]
  [clojure.test :refer [deftest is]]))


(def
 v3_l31
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 800, :height 250})))


(deftest
 t4_l35
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:width s) 800))))
   v3_l31)))


(def
 v6_l41
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 300, :height 500})))


(deftest
 t7_l45
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:width s) 300))))
   v6_l41)))


(def
 v9_l53
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Sepal Measurements",
    :x-label "Length (cm)",
    :y-label "Width (cm)"})))


(deftest
 t10_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Sepal Measurements"} (:texts s)))))
   v9_l53)))


(def
 v12_l65
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements",
    :subtitle "Sepal dimensions across three species",
    :caption "Source: Fisher's Iris dataset (1936)"})))


(deftest
 t13_l71
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Measurements"} (:texts s))
      (some (fn [t] (.contains t "Sepal dimensions")) (:texts s)))))
   v12_l65)))


(def
 v15_l79
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-label "Species (override)"})))


(deftest
 t16_l83
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Species (override)"} (:texts s)))))
   v15_l79)))


(def
 v18_l89
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:size :petal-length})
  (pj/options {:size-label "Petal length (override)"})))


(deftest
 t19_l93
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v18_l89)))


(def
 v21_l99
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha :petal-length})
  (pj/options {:alpha-label "Petal length (override)"})))


(deftest
 t22_l103
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v21_l99)))


(def
 v24_l111
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})
  (pj/options {:shape-label "Marker (override)"})))


(deftest
 t25_l115
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (+ (:points s) (:polygons s)))
      (some #{"Marker (override)"} (:texts s)))))
   v24_l111)))


(def
 v27_l129
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:fill-label "Score"})))


(deftest
 t28_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (some #{"Score"} (:texts s)) (pos? (:visible-tiles s)))))
   v27_l129)))


(def
 v30_l144
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:color :species})))


(deftest
 t31_l147
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 3 (:polygons s)) (= 3 (count fills)))))
   v30_l144)))


(def
 v33_l161
 (->
  {:product
   (map (fn* [p1__11157#] (str "Product " p1__11157#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t34_l166
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-45")))
   v33_l161)))


(def
 v36_l174
 (->
  {:product
   (map (fn* [p1__11158#] (str "Product " p1__11158#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45, :x-tick-label-pad 90})))


(deftest
 t37_l180
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v36_l174)))


(def
 v39_l199
 (->
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator ","})))


(deftest
 t40_l205
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "462,389") (contains? texts "100,000"))))
   v39_l199)))


(def
 v42_l215
 (->
  {:violation ["Meter Expired" "Over Time Limit"],
   :tickets [462389 181444]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator "."})))


(deftest
 t43_l221
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "462.389"))
   v42_l215)))


(def
 v45_l229
 (let
  [panel-width
   (fn
    [opts]
    (->
     {:x [1 2 3], :y [1000000 2000000 3000000]}
     (pj/lay-point :x :y)
     (pj/options opts)
     pj/plan
     :panel-width))]
  {:ungrouped (panel-width {}),
   :grouped (panel-width {:thousands-separator ","})}))


(deftest
 t46_l238
 (is ((fn [m] (< (:grouped m) (:ungrouped m))) v45_l229)))


(def
 v48_l243
 (->
  {:x [1], :y [1], :amount [1234.56]}
  (pj/lay-label :x :y {:text :amount})
  (pj/options {:thousands-separator ","})))


(deftest
 t49_l247
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "1,234.56"))
   v48_l243)))


(def
 v51_l260
 (def
  exponential-data
  {:x (range 1 50),
   :y
   (map
    (fn* [p1__11159#] (* 2 (Math/pow 1.1 p1__11159#)))
    (range 1 50))}))


(def
 v53_l266
 (->
  exponential-data
  (pj/lay-point :x :y)
  (pj/options {:title "Linear Scale"})))


(deftest
 t54_l270
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 49 (:points s)))))
   v53_l266)))


(def
 v56_l276
 (->
  exponential-data
  (pj/lay-point :x :y)
  (pj/scale :y :log)
  (pj/options {:title "Log Y Scale"})))


(deftest
 t57_l281
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 49 (:points s)))))
   v56_l276)))


(def
 v59_l287
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :domain [0 6]})
  (pj/options {:title "Fixed Y Domain [0, 6]"})))


(deftest
 t60_l292
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v59_l287)))


(def
 v62_l305
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :domain [3.0 3.5]})
  (pj/options {:title "Tight Y Domain [3.0, 3.5]"})))


(deftest
 t63_l310
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v62_l305)))


(def
 v65_l317
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :breaks [2.0 3.0 4.0]})))


(deftest
 t66_l321
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (every? (set (:texts s)) ["2" "3" "4"]))))
   v65_l317)))


(def
 v68_l331
 (->
  (for
   [day (range 1 8) hour (range 0 24)]
   {:day day,
    :hour hour,
    :load (+ (* 0.3 (Math/sin (* 0.5 hour))) (* 0.2 (mod day 3)))})
  (pj/lay-tile :day :hour {:fill :load})
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})
  (pj/options {:title "Weekly Load by Hour"})))


(deftest
 t69_l340
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v68_l331)))


(def
 v71_l348
 (->
  {:size ["medium" "small" "large"], :count [12 30 7]}
  (pj/lay-bar :size :count)
  (pj/scale
   :x
   {:type :categorical, :domain ["large" "medium" "small"]})))


(deftest
 t72_l353
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      labels
      (filter #{"small" "medium" "large"} (:texts s))]
     (= ["large" "medium" "small"] (vec labels))))
   v71_l348)))


(def
 v74_l364
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :labels ["First" "Fourth"]})))


(deftest
 t75_l369
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v74_l364)))


(def
 v77_l382
 (->
  {:bin (map (fn* [p1__11160#] (str "bin-" p1__11160#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t78_l387
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__11161#] (.startsWith p1__11161# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v77_l382)))


(def
 v80_l408
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t81_l411
 (is
  ((fn
    [v]
    (let
     [sizes (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (count sizes))
      (< (/ (second sizes) (first sizes)) 1.5)
      (> (/ (last sizes) (first sizes)) 3.0))))
   v80_l408)))


(def
 v83_l424
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t84_l428
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v83_l424)))


(def
 v86_l437
 (->
  (for
   [r (range 5) c (range 5)]
   {:r r, :c c, :v (Math/pow 10.0 (/ (+ r c) 2.0))})
  (pj/lay-tile :r :c {:fill :v})
  (pj/scale :fill :log)))


(deftest
 t87_l442
 (is ((fn [v] (>= (:visible-tiles (pj/svg-summary v)) 25)) v86_l437)))


(def v89_l463 pj/shape-symbols)


(deftest t90_l465 (is ((fn [syms] (= syms (distinct syms))) v89_l463)))


(def
 v92_l473
 (->
  {:model ["a" "b" "c" "d"],
   :score [3 1 4 2],
   :tier ["gold" "silver" "bronze" "gold"]}
  (pj/lay-point :model :score {:shape :tier})))


(deftest
 t93_l476
 (is
  ((fn
    [v]
    (=
     (take 3 pj/shape-symbols)
     (mapv :shape (:entries (:shape-legend (pj/plan v))))))
   v92_l473)))


(def
 v95_l483
 (->
  {:model ["a" "b" "c" "d"],
   :score [3 1 4 2],
   :tier ["gold" "silver" "bronze" "gold"]}
  (pj/lay-point :model :score {:shape :tier})
  (pj/scale :shape {:domain ["gold" "silver" "bronze"]})))


(deftest
 t96_l487
 (is
  ((fn
    [v]
    (=
     (mapv vector ["gold" "silver" "bronze"] pj/shape-symbols)
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v95_l483)))


(def
 v98_l496
 (->
  {:model ["a" "b" "c" "d"],
   :score [3 1 4 2],
   :tier ["gold" "silver" "bronze" "gold"]}
  (pj/lay-point :model :score {:shape :tier})
  (pj/scale
   :shape
   {:domain ["gold" "silver" "bronze"],
    :values [:diamond :cross :plus]})))


(deftest
 t99_l501
 (is
  ((fn
    [v]
    (=
     [["gold" :diamond] ["silver" :cross] ["bronze" :plus]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan v))))))
   v98_l496)))


(def
 v101_l511
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t102_l514
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 150 (:points s))
      (contains? (:alphas s) 0.5)
      (contains? (:sizes s) 5.0))))
   v101_l511)))


(def
 v104_l522
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t105_l525
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v104_l522)))


(def
 v107_l533
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dashed})))


(deftest
 t108_l536
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v107_l533)))


(def
 v110_l542
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dotted})))


(deftest
 t111_l545
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "1.00 3.00"))))
   v110_l542)))


(def
 v113_l551
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :solid})))


(deftest
 t114_l554
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (= 0 (:dashed-lines s)))))
   v113_l551)))


(def
 v116_l561
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash [12 4]})))


(deftest
 t117_l564
 (is
  ((fn
    [v]
    (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))
   v116_l561)))


(def
 v119_l568
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t120_l571
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v119_l568)))


(def
 v122_l594
 (->
  {:x [2 2 2], :y [3 2 1]}
  (pj/lay-point :x :y {:size 6, :color "#888888"})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :left,
    :data {:x [2], :y [3], :tag ["align-x :left"]}})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :center,
    :data {:x [2], :y [2], :tag ["align-x :center"]}})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :right,
    :data {:x [2], :y [1], :tag ["align-x :right"]}})))


(deftest
 t123_l603
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
      (filter (fn* [p1__11162#] (= :text (:mark p1__11162#))))
      (mapv (fn* [p1__11163#] (-> p1__11163# :style :align-x))))))
   v122_l594)))


(def
 v125_l614
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :align-y :bottom})))


(deftest
 t127_l623
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
       (filter (fn* [p1__11164#] (= :text (:mark p1__11164#))))
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
        (filter (fn* [p1__11165#] (= :text (:mark p1__11165#))))
        first
        :style
        ((fn*
          [p1__11166#]
          (select-keys p1__11166# [:align-x :align-y])))))]
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
   v125_l614)))


(def
 v129_l666
 (def
  tickets-by-violation
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}))


(def
 v130_l670
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})))


(def
 v132_l678
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/options {:fit-text-domain false})))


(def
 v134_l685
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
 t135_l698
 (is ((fn [m] (> (:fitted m) (:unfitted m))) v134_l685)))


(def
 v137_l704
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
 t138_l716
 (is ((fn [m] (= (:fitted m) (:unfitted m))) v137_l704)))


(def
 v140_l725
 (->
  tickets-by-violation
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets})
  (pj/scale :x {:domain [0 500000]})
  pj/plan
  :panels
  first
  :x-domain))


(deftest t141_l734 (is ((fn [d] (= [0 500000] d)) v140_l725)))


(def
 v143_l754
 (->
  {:x [1 2 3], :y [2 3 1]}
  (pj/lay-point :x :y {:size 5, :color "#888888"})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :center,
    :align-y :bottom,
    :data {:x [1 3], :y [2 1], :tag ["steady" "dip"]}})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :center,
    :align-y :bottom,
    :font-weight :bold,
    :data {:x [2], :y [3], :tag ["peak"]}})))


(deftest
 t144_l762
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:bold-texts s))
      (= 0 (:italic-texts s))
      (every? (set (:texts s)) ["steady" "dip" "peak"]))))
   v143_l754)))


(def
 v146_l773
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5})
  (pj/lay-label
   {:text :note,
    :font-style :italic,
    :data
    {:sepal-length [7.0],
     :sepal-width [4.2],
     :note ["setosa sits apart"]}})))


(deftest
 t147_l779
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:italic-texts s)) (= 0 (:bold-texts s)))))
   v146_l773)))


(def
 v149_l795
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag, :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(def
 v150_l798
 (->
  {:x [1], :y [1]}
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :box true,
    :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(deftest
 t151_l802
 (is ((fn [v] (= 1 (:label-boxes (pj/svg-summary v)))) v150_l798)))


(def
 v153_l813
 (->
  {:x [1 1 1], :y [3 2 1]}
  (pj/lay-point :x :y {:size 5, :color "#888888"})
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box {:corner-radius 8},
    :nudge-x 0.05,
    :data {:x [1], :y [3], :tag ["corner-radius 8"]}})
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :nudge-x 0.05,
    :data {:x [1], :y [2], :tag ["the default, 3"]}})
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box {:corner-radius 0},
    :nudge-x 0.05,
    :data {:x [1], :y [1], :tag ["corner-radius 0"]}})))


(deftest
 t154_l822
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:label-boxes s))
      (= 3 (:points s))
      (=
       [8.0 3.0 0.0]
       (->>
        (pj/plan v)
        :panels
        first
        :layers
        (filter (fn* [p1__11167#] (= :text (:mark p1__11167#))))
        (mapv
         (fn*
          [p1__11168#]
          (-> p1__11168# :style :box :corner-radius))))))))
   v153_l813)))


(def v156_l845 (:band-opacity (pj/config)))


(deftest t157_l847 (is ((fn [v] (= 0.15 v)) v156_l845)))


(def
 v159_l851
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t160_l856
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v159_l851)))


(def
 v162_l870
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-v
   {:x-intercept 6.0, :color "gray", :stroke-dash :dashed})))


(deftest
 t163_l874
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v162_l870)))


(def
 v165_l893
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t166_l897
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v165_l893)))


(def
 v168_l901
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :dark2})))


(deftest
 t169_l905
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v168_l901)))


(def v171_l917 (c2d/find-palette #"budapest"))


(deftest
 t172_l919
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v171_l917)))


(def v174_l923 (c2d/find-palette #"^:set"))


(deftest
 t175_l925
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v174_l923)))


(def v177_l929 (c2d/find-gradient #"viridis"))


(deftest
 t178_l931
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v177_l929)))


(def v180_l936 (c2d/palette :grand-budapest-1))


(deftest
 t181_l938
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v180_l936)))


(def
 v183_l950
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :khroma/okabeito})))


(deftest
 t184_l954
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v183_l950)))


(def
 v186_l960
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t187_l965
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v186_l960)))


(def
 v189_l973
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t190_l977
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v189_l973)))


(def
 v192_l983
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t193_l987
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v192_l983)))


(def
 v195_l993
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t196_l997
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width {:color :species})
        (pj/options {:legend-position :none})))]
     (and
      (= 150 (:points s))
      (zero? (get-in plan [:layout :legend-w])))))
   v195_l993)))
