(ns
 plotje-book.customization-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure2d.color :as c2d]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 800, :height 250})))


(deftest
 t4_l39
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:width s) 800))))
   v3_l35)))


(def
 v6_l45
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 300, :height 500})))


(deftest
 t7_l49
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:width s) 300))))
   v6_l45)))


(def
 v9_l57
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Sepal Measurements",
    :x-label "Length (cm)",
    :y-label "Width (cm)"})))


(deftest
 t10_l63
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Sepal Measurements"} (:texts s)))))
   v9_l57)))


(def
 v12_l69
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements",
    :subtitle "Sepal dimensions across three species",
    :caption "Source: Fisher's Iris dataset (1936)"})))


(deftest
 t13_l75
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Measurements"} (:texts s))
      (some (fn [t] (.contains t "Sepal dimensions")) (:texts s)))))
   v12_l69)))


(def
 v15_l87
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-label "Species (override)"})))


(deftest
 t16_l91
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Species (override)"} (:texts s)))))
   v15_l87)))


(def
 v18_l97
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:size :petal-length})
  (pj/options {:size-label "Petal length (override)"})))


(deftest
 t19_l101
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v18_l97)))


(def
 v21_l107
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha :petal-length})
  (pj/options {:alpha-label "Petal length (override)"})))


(deftest
 t22_l111
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v21_l107)))


(def
 v24_l119
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})
  (pj/options {:shape-label "Marker (override)"})))


(deftest
 t25_l123
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (+ (:points s) (:polygons s)))
      (some #{"Marker (override)"} (:texts s)))))
   v24_l119)))


(def
 v27_l137
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:fill-label "Score"})))


(deftest
 t28_l141
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (some #{"Score"} (:texts s)) (pos? (:visible-tiles s)))))
   v27_l137)))


(def
 v30_l152
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:color :species})))


(deftest
 t31_l155
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 3 (:polygons s)) (= 3 (count fills)))))
   v30_l152)))


(def
 v33_l169
 (->
  {:product
   (map (fn* [p1__71988#] (str "Product " p1__71988#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t34_l174
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-45")))
   v33_l169)))


(def
 v36_l182
 (->
  {:product
   (map (fn* [p1__71989#] (str "Product " p1__71989#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45, :x-tick-label-pad 90})))


(deftest
 t37_l188
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v36_l182)))


(def
 v39_l207
 (->
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator ","})))


(deftest
 t40_l213
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "462,389") (contains? texts "100,000"))))
   v39_l207)))


(def
 v42_l223
 (->
  {:violation ["Meter Expired" "Over Time Limit"],
   :tickets [462389 181444]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator "."})))


(deftest
 t43_l229
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "462.389"))
   v42_l223)))


(def
 v45_l237
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
 t46_l246
 (is ((fn [m] (< (:grouped m) (:ungrouped m))) v45_l237)))


(def
 v48_l251
 (->
  {:x [1], :y [1], :amount [1234.56]}
  (pj/lay-label :x :y {:text :amount})
  (pj/options {:thousands-separator ","})))


(deftest
 t49_l255
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "1,234.56"))
   v48_l251)))


(def
 v51_l267
 (->
  (for [y (range 2020 2031)] {:year y, :revenue (* 1000 (- y 2019))})
  (pj/lay-point :year :revenue)
  (pj/options {:thousands-separator ","})
  pj/plan
  :panels
  first
  :x-ticks
  :labels))


(deftest
 t52_l276
 (is ((fn [labels] (= "2,020" (first labels))) v51_l267)))


(def
 v54_l282
 (->
  (for [y (range 2020 2024)] {:year y, :revenue (* 1000 (- y 2019))})
  (pj/lay-bar :year :revenue {:x-type :categorical})
  (pj/options {:thousands-separator ","})
  pj/plan
  :panels
  first
  :x-ticks
  :labels))


(deftest
 t55_l291
 (is
  ((fn [labels] (= ["2020" "2021" "2022" "2023"] (vec labels)))
   v54_l282)))


(def
 v57_l297
 (->>
  (->
   (for
    [i (range 8)]
    {:xx (double i),
     :yy (double i),
     :volume (* 100000 (inc i)),
     :region (str "region " i)})
   (pj/lay-point :xx :yy {:size :volume, :color :region})
   (pj/options {:thousands-separator ","})
   pj/svg-summary
   :texts)
  (filter (fn* [p1__71990#] (re-find #"," p1__71990#)))
  distinct
  sort))


(deftest
 t58_l307
 (is
  ((fn
    [texts]
    (=
     ["100,000"
      "200,000"
      "300,000"
      "400,000"
      "500,000"
      "600,000"
      "700,000"
      "800,000"]
     (vec texts)))
   v57_l297)))


(def
 v60_l320
 (->
  {:region ["North" "South" "East"], :profit [1234.5 1500.25 2680.75]}
  (pj/lay-bar :profit :region)
  (pj/lay-label :profit :region {:text :profit, :align-x :right})
  (pj/options {:thousands-separator ".", :decimal-separator ","})))


(deftest
 t62_l330
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "1.234,5")
      (contains? texts "2.680,75")
      (some (fn [t] (re-matches #"\d\.\d00" t)) texts))))
   v60_l320)))


(def
 v64_l349
 (->
  (for
   [day (range 1 8) hour (range 0 24)]
   {:day day,
    :hour hour,
    :load (+ (* 0.3 (Math/sin (* 0.5 hour))) (* 0.2 (mod day 3)))})
  (pj/lay-tile :day :hour {:fill :load})
  (pj/scale
   :x
   {:breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})
  (pj/options {:title "Weekly Load by Hour"})))


(deftest
 t65_l357
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v64_l349)))


(def
 v67_l372
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t68_l375
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
   v67_l372)))


(def
 v70_l383
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t71_l386
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v70_l383)))


(def
 v73_l394
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dashed})))


(deftest
 t74_l397
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v73_l394)))


(def
 v76_l403
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dotted})))


(deftest
 t77_l406
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "1.00 3.00"))))
   v76_l403)))


(def
 v79_l412
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :solid})))


(deftest
 t80_l415
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (= 0 (:dashed-lines s)))))
   v79_l412)))


(def
 v82_l422
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash [12 4]})))


(deftest
 t83_l425
 (is
  ((fn
    [v]
    (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))
   v82_l422)))


(def
 v85_l429
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t86_l432
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v85_l429)))


(def
 v88_l461
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
 t89_l469
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:bold-texts s))
      (= 0 (:italic-texts s))
      (every? (set (:texts s)) ["steady" "dip" "peak"]))))
   v88_l461)))


(def
 v91_l480
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
 t92_l486
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:italic-texts s)) (= 0 (:bold-texts s)))))
   v91_l480)))


(def
 v94_l502
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag, :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(def
 v95_l505
 (->
  {:x [1], :y [1]}
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :box true,
    :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(deftest
 t96_l509
 (is
  ((fn
    [v]
    (and
     (= 1 (:label-boxes (pj/svg-summary v)))
     (=
      (str (pj/plot v))
      (str
       (pj/plot
        (->
         {:x [1], :y [1]}
         (pj/lay-label
          :x
          :y
          {:text :tag,
           :data {:x [1], :y [1], :tag ["a boxed label"]}})))))))
   v95_l505)))


(def
 v98_l530
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
 t99_l539
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
        (filter (fn* [p1__71991#] (= :text (:mark p1__71991#))))
        (mapv
         (fn*
          [p1__71992#]
          (-> p1__71992# :style :box :corner-radius))))))))
   v98_l530)))


(def
 v101_l552
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box false,
    :data {:x [1], :y [1], :tag ["bare text"]}})))


(deftest
 t102_l556
 (is
  ((fn
    [v]
    (and
     (zero? (:label-boxes (pj/svg-summary v)))
     (=
      (str (pj/plot v))
      (str
       (pj/plot
        (->
         {:x [1], :y [1]}
         (pj/lay-text
          :x
          :y
          {:text :tag,
           :data {:x [1], :y [1], :tag ["bare text"]}})))))))
   v101_l552)))


(def
 v104_l579
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.4})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#cc3311"})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#4477aa", :offset-y -25})))


(deftest
 t105_l584
 (is
  ((fn
    [fr]
    (=
     [nil -25]
     (mapv :offset-y (:annotations (first (:panels (pj/plan fr)))))))
   v104_l579)))


(def v107_l591 (:band-opacity (pj/config)))


(deftest t108_l593 (is ((fn [v] (= 0.15 v)) v107_l591)))


(def
 v110_l597
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t111_l602
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v110_l597)))


(def
 v113_l622
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-v
   {:x-intercept 6.0, :color "gray", :stroke-dash :dashed})))


(deftest
 t114_l626
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v113_l622)))


(def v116_l646 (c2d/find-palette #"budapest"))


(deftest
 t117_l648
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v116_l646)))


(def v119_l652 (c2d/find-palette #"^:set"))


(deftest
 t120_l654
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v119_l652)))


(def v122_l658 (c2d/find-gradient #"viridis"))


(deftest
 t123_l660
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v122_l658)))


(def v125_l665 (c2d/palette :grand-budapest-1))


(deftest
 t126_l667
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v125_l665)))


(def
 v128_l679
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-values :khroma/okabeito})))


(deftest
 t129_l683
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v128_l679)))


(def
 v131_l689
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t132_l694
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v131_l689)))


(def
 v134_l702
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t135_l706
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v134_l702)))


(def
 v137_l712
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t138_l716
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v137_l712)))


(def
 v140_l722
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t141_l726
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
   v140_l722)))
