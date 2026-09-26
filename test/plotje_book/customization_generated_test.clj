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
 v30_l150
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:color-label "Score"})))


(deftest
 t31_l154
 (is ((fn [v] (some #{"Score"} (:texts (pj/svg-summary v)))) v30_l150)))


(def
 v33_l163
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:color :species})))


(deftest
 t34_l166
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 3 (:polygons s)) (= 3 (count fills)))))
   v33_l163)))


(def
 v36_l182
 (->
  {:product
   (map (fn* [p1__11193#] (str "Product " p1__11193#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t37_l187
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-45")))
   v36_l182)))


(def
 v39_l196
 (->
  {:product
   (map (fn* [p1__11194#] (str "Product " p1__11194#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45, :x-tick-label-pad 90})))


(deftest
 t40_l202
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v39_l196)))


(def
 v42_l214
 (->
  {:team ["Operations" "Research" "Customer support"],
   :headcount [42 17 29]}
  (pj/lay-bar :team :headcount)
  (pj/coord :flip)
  (pj/options {:y-tick-angle -90})))


(deftest
 t43_l220
 (is
  ((fn
    [v]
    (and
     (= 3 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-90")))
   v42_l214)))


(def
 v45_l235
 (->
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator ","})))


(deftest
 t46_l241
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "462,389") (contains? texts "100,000"))))
   v45_l235)))


(def
 v48_l251
 (->
  {:violation ["Meter Expired" "Over Time Limit"],
   :tickets [462389 181444]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator "."})))


(deftest
 t49_l257
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "462.389"))
   v48_l251)))


(def
 v51_l265
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
 t52_l274
 (is ((fn [m] (< (:grouped m) (:ungrouped m))) v51_l265)))


(def
 v54_l279
 (->
  {:x [1], :y [1], :amount [1234.56]}
  (pj/lay-label :x :y {:text :amount})
  (pj/options {:thousands-separator ","})))


(deftest
 t55_l283
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "1,234.56"))
   v54_l279)))


(def
 v57_l295
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
 t58_l304
 (is ((fn [labels] (= "2,020" (first labels))) v57_l295)))


(def
 v60_l310
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
 t61_l319
 (is
  ((fn [labels] (= ["2020" "2021" "2022" "2023"] (vec labels)))
   v60_l310)))


(def
 v63_l325
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
  (filter (fn* [p1__11195#] (re-find #"," p1__11195#)))
  distinct
  sort))


(deftest
 t64_l335
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
   v63_l325)))


(def
 v66_l348
 (->
  {:region ["North" "South" "East"], :profit [1234.5 1500.25 2680.75]}
  (pj/lay-bar :profit :region)
  (pj/lay-label :profit :region {:text :profit, :align-x :right})
  (pj/options {:thousands-separator ".", :decimal-separator ","})))


(deftest
 t68_l358
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "1.234,5")
      (contains? texts "2.680,75")
      (some (fn [t] (re-matches #"\d\.\d00" t)) texts))))
   v66_l348)))


(def
 v70_l377
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
 t71_l385
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v70_l377)))


(def
 v73_l400
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t74_l403
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
   v73_l400)))


(def
 v76_l411
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t77_l414
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v76_l411)))


(def
 v79_l422
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dashed})))


(deftest
 t80_l425
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v79_l422)))


(def
 v82_l431
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dotted})))


(deftest
 t83_l434
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "1.00 3.00"))))
   v82_l431)))


(def
 v85_l440
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :solid})))


(deftest
 t86_l443
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (= 0 (:dashed-lines s)))))
   v85_l440)))


(def
 v88_l450
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash [12 4]})))


(deftest
 t89_l453
 (is
  ((fn
    [v]
    (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))
   v88_l450)))


(def
 v91_l457
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t92_l460
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v91_l457)))


(def
 v94_l489
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
 t95_l497
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:bold-texts s))
      (= 0 (:italic-texts s))
      (every? (set (:texts s)) ["steady" "dip" "peak"]))))
   v94_l489)))


(def
 v97_l508
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
 t98_l514
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:italic-texts s)) (= 0 (:bold-texts s)))))
   v97_l508)))


(def
 v100_l530
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag, :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(def
 v101_l533
 (->
  {:x [1], :y [1]}
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :box true,
    :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(deftest
 t102_l537
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
   v101_l533)))


(def
 v104_l558
 (->
  {:x [1 1 1], :y [3 2 1]}
  (pj/lay-point :x :y {:size 5, :color "#888888"})
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box {:corner-radius 8},
    :dx 0.05,
    :data {:x [1], :y [3], :tag ["corner-radius 8"]}})
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :dx 0.05,
    :data {:x [1], :y [2], :tag ["the default, 3"]}})
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box {:corner-radius 0},
    :dx 0.05,
    :data {:x [1], :y [1], :tag ["corner-radius 0"]}})))


(deftest
 t105_l567
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
        (filter (fn* [p1__11196#] (= :text (:mark p1__11196#))))
        (mapv
         (fn*
          [p1__11197#]
          (-> p1__11197# :style :box :corner-radius))))))))
   v104_l558)))


(def
 v107_l580
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box false,
    :data {:x [1], :y [1], :tag ["bare text"]}})))


(deftest
 t108_l584
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
   v107_l580)))


(def
 v110_l607
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.4})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#cc3311"})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#4477aa", :offset-y -25})))


(deftest
 t111_l612
 (is
  ((fn
    [fr]
    (=
     [nil nil -25]
     (mapv :offset-y (:layers (first (:panels (pj/plan fr)))))))
   v110_l607)))


(def v113_l621 (:band-opacity (pj/config)))


(deftest t114_l623 (is ((fn [v] (= 0.15 v)) v113_l621)))


(def
 v116_l627
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t117_l632
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v116_l627)))


(def
 v119_l652
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-v
   {:x-intercept 6.0, :color "gray", :stroke-dash :dashed})))


(deftest
 t120_l656
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v119_l652)))


(def
 v122_l668
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-rule-h
   {:y-intercept 3.0, :size 8, :alpha 0.3, :color "#cc3311"})
  (pj/lay-point {:color :species})))


(deftest
 t123_l673
 (is
  ((fn
    [fr]
    (let
     [layers (:layers (first (:panels (pj/plan fr))))]
     (and
      (= [:rule-h :point] (mapv :mark layers))
      (= 8 (:stroke-width (:style (first layers))))
      (= 0.3 (:opacity (:style (first layers)))))))
   v122_l668)))


(def v125_l695 (c2d/find-palette #"budapest"))


(deftest
 t126_l697
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v125_l695)))


(def v128_l701 (c2d/find-palette #"^:set"))


(deftest
 t129_l703
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v128_l701)))


(def v131_l707 (c2d/find-gradient #"viridis"))


(deftest
 t132_l709
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v131_l707)))


(def v134_l714 (c2d/palette :grand-budapest-1))


(deftest
 t135_l716
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v134_l714)))


(def
 v137_l728
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-values :khroma/okabeito})))


(deftest
 t138_l732
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v137_l728)))


(def
 v140_l738
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t141_l743
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v140_l738)))


(def
 v143_l751
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t144_l755
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v143_l751)))


(def
 v146_l761
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t147_l765
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v146_l761)))


(def
 v149_l771
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t150_l775
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
   v149_l771)))
