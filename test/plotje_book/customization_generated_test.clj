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
 v27_l138
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})))


(deftest
 t28_l141
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (texts "z") (not (texts "fill")))))
   v27_l138)))


(def
 v30_l147
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:fill-label "Score"})))


(deftest
 t31_l151
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (some #{"Score"} (:texts s)) (pos? (:visible-tiles s)))))
   v30_l147)))


(def
 v33_l160
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:color-label "Score"})))


(deftest
 t34_l164
 (is ((fn [v] (some #{"Score"} (:texts (pj/svg-summary v)))) v33_l160)))


(def
 v36_l174
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:color :species})))


(deftest
 t37_l177
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 3 (:polygons s)) (= 3 (count fills)))))
   v36_l174)))


(def
 v39_l193
 (->
  {:product
   (map (fn* [p1__11193#] (str "Product " p1__11193#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t40_l198
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-45")))
   v39_l193)))


(def
 v42_l207
 (->
  {:product
   (map (fn* [p1__11194#] (str "Product " p1__11194#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45, :x-tick-label-pad 90})))


(deftest
 t43_l213
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v42_l207)))


(def
 v45_l225
 (->
  {:team ["Operations" "Research" "Customer support"],
   :headcount [42 17 29]}
  (pj/lay-bar :team :headcount)
  (pj/coord :flip)
  (pj/options {:y-tick-angle -90})))


(deftest
 t46_l231
 (is
  ((fn
    [v]
    (and
     (= 3 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-90")))
   v45_l225)))


(def
 v48_l246
 (->
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator ","})))


(deftest
 t49_l252
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "462,389") (contains? texts "100,000"))))
   v48_l246)))


(def
 v51_l262
 (->
  {:violation ["Meter Expired" "Over Time Limit"],
   :tickets [462389 181444]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator "."})))


(deftest
 t52_l268
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "462.389"))
   v51_l262)))


(def
 v54_l276
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
 t55_l285
 (is ((fn [m] (< (:grouped m) (:ungrouped m))) v54_l276)))


(def
 v57_l290
 (->
  {:x [1], :y [1], :amount [1234.56]}
  (pj/lay-label :x :y {:text :amount})
  (pj/options {:thousands-separator ","})))


(deftest
 t58_l294
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "1,234.56"))
   v57_l290)))


(def
 v60_l306
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
 t61_l315
 (is ((fn [labels] (= "2,020" (first labels))) v60_l306)))


(def
 v63_l321
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
 t64_l330
 (is
  ((fn [labels] (= ["2020" "2021" "2022" "2023"] (vec labels)))
   v63_l321)))


(def
 v66_l336
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
 t67_l346
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
   v66_l336)))


(def
 v69_l359
 (->
  {:region ["North" "South" "East"], :profit [1234.5 1500.25 2680.75]}
  (pj/lay-bar :profit :region)
  (pj/lay-label :profit :region {:text :profit, :align-x :right})
  (pj/options {:thousands-separator ".", :decimal-separator ","})))


(deftest
 t71_l369
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "1.234,5")
      (contains? texts "2.680,75")
      (some (fn [t] (re-matches #"\d\.\d00" t)) texts))))
   v69_l359)))


(def
 v73_l388
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
 t74_l396
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v73_l388)))


(def
 v76_l411
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t77_l414
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
   v76_l411)))


(def
 v79_l422
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t80_l425
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v79_l422)))


(def
 v82_l433
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dashed})))


(deftest
 t83_l436
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v82_l433)))


(def
 v85_l442
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dotted})))


(deftest
 t86_l445
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "1.00 3.00"))))
   v85_l442)))


(def
 v88_l451
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :solid})))


(deftest
 t89_l454
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (= 0 (:dashed-lines s)))))
   v88_l451)))


(def
 v91_l461
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash [12 4]})))


(deftest
 t92_l464
 (is
  ((fn
    [v]
    (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))
   v91_l461)))


(def
 v94_l468
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t95_l471
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v94_l468)))


(def
 v97_l500
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
 t98_l508
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:bold-texts s))
      (= 0 (:italic-texts s))
      (every? (set (:texts s)) ["steady" "dip" "peak"]))))
   v97_l500)))


(def
 v100_l519
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
 t101_l525
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:italic-texts s)) (= 0 (:bold-texts s)))))
   v100_l519)))


(def
 v103_l541
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag, :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(def
 v104_l544
 (->
  {:x [1], :y [1]}
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :box true,
    :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(deftest
 t105_l548
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
   v104_l544)))


(def
 v107_l569
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
 t108_l578
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
   v107_l569)))


(def
 v110_l591
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box false,
    :data {:x [1], :y [1], :tag ["bare text"]}})))


(deftest
 t111_l595
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
   v110_l591)))


(def
 v113_l618
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.4})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#cc3311"})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#4477aa", :offset-y -25})))


(deftest
 t114_l623
 (is
  ((fn
    [fr]
    (=
     [nil nil -25]
     (mapv :offset-y (:layers (first (:panels (pj/plan fr)))))))
   v113_l618)))


(def v116_l632 (:band-opacity (pj/config)))


(deftest t117_l634 (is ((fn [v] (= 0.15 v)) v116_l632)))


(def
 v119_l638
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t120_l643
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v119_l638)))


(def v122_l649 (:band-color (pj/config)))


(deftest t123_l651 (is ((fn [v] (= "#7F7F7F" v)) v122_l649)))


(def
 v125_l656
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/options {:band-color "#2a9d8f"})))


(deftest
 t126_l661
 (is
  ((fn
    [v]
    (let
     [band
      (->>
       (pj/plan v)
       :panels
       first
       :layers
       (filter (fn* [p1__11198#] (= :band-v (:mark p1__11198#))))
       first)]
     (= [(/ 42 255.0) (/ 157 255.0) (/ 143 255.0) 1.0] (:color band))))
   v125_l656)))


(def
 v128_l684
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-v
   {:x-intercept 6.0, :color "gray", :stroke-dash :dashed})))


(deftest
 t129_l688
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v128_l684)))


(def
 v131_l700
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-rule-h
   {:y-intercept 3.0, :size 8, :alpha 0.3, :color "#cc3311"})
  (pj/lay-point {:color :species})))


(deftest
 t132_l705
 (is
  ((fn
    [fr]
    (let
     [layers (:layers (first (:panels (pj/plan fr))))]
     (and
      (= [:rule-h :point] (mapv :mark layers))
      (= 8 (:stroke-width (:style (first layers))))
      (= 0.3 (:opacity (:style (first layers)))))))
   v131_l700)))


(def v134_l727 (c2d/find-palette #"budapest"))


(deftest
 t135_l729
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v134_l727)))


(def v137_l733 (c2d/find-palette #"^:set"))


(deftest
 t138_l735
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v137_l733)))


(def v140_l739 (c2d/find-gradient #"viridis"))


(deftest
 t141_l741
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v140_l739)))


(def v143_l746 (c2d/palette :grand-budapest-1))


(deftest
 t144_l748
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v143_l746)))


(def
 v146_l760
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-values :khroma/okabeito})))


(deftest
 t147_l764
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v146_l760)))


(def
 v149_l770
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t150_l775
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v149_l770)))


(def
 v152_l783
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t153_l787
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v152_l783)))


(def
 v155_l793
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t156_l797
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v155_l793)))


(def
 v158_l802
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:legend-position :bottom})))


(deftest
 t159_l806
 (is
  ((fn
    [v]
    (let
     [layout (:layout (pj/plan v))]
     (and (pos? (:legend-h layout)) (zero? (:legend-w layout)))))
   v158_l802)))


(def
 v161_l818
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t162_l822
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
   v161_l818)))
