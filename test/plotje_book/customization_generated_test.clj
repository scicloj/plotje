(ns
 plotje-book.customization-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure2d.color :as c2d]
  [clojure.test :refer [deftest is]]))


(def
 v3_l42
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 800, :height 400})))


(deftest
 t4_l46
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 800 (:width s)) (= 400 (:height s)))))
   v3_l42)))


(def
 v6_l54
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 800, :height 200})))


(deftest
 t7_l58
 (is
  ((fn
    [v]
    (let
     [taller
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width {:color :species})
       (pj/options {:width 800, :height 400}))
      font-sizes
      (fn [fr] (set (re-seq #":font-size [0-9.]+" (str (pj/plot fr)))))
      s
      (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 800 (:width s))
      (= 200 (:height s))
      (= (:sizes s) (:sizes (pj/svg-summary taller)))
      (= (font-sizes v) (font-sizes taller))
      (<
       (:panel-height (pj/plan v))
       (:panel-height (pj/plan taller))))))
   v6_l54)))


(def
 v9_l76
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Sepal Measurements",
    :x-label "Length (cm)",
    :y-label "Width (cm)"})))


(deftest
 t10_l82
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Sepal Measurements"} (:texts s)))))
   v9_l76)))


(def
 v12_l88
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements",
    :subtitle "Sepal dimensions across three species",
    :caption "Source: Fisher's Iris dataset (1936)"})))


(deftest
 t13_l94
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Measurements"} (:texts s))
      (some (fn [t] (.contains t "Sepal dimensions")) (:texts s)))))
   v12_l88)))


(def
 v15_l106
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-label "Species (override)"})))


(deftest
 t16_l110
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Species (override)"} (:texts s)))))
   v15_l106)))


(def
 v18_l116
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:size :petal-length})
  (pj/options {:size-label "Petal length (override)"})))


(deftest
 t19_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v18_l116)))


(def
 v21_l126
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha :petal-length})
  (pj/options {:alpha-label "Petal length (override)"})))


(deftest
 t22_l130
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v21_l126)))


(def
 v24_l138
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})
  (pj/options {:shape-label "Marker (override)"})))


(deftest
 t25_l142
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (+ (:points s) (:polygons s)))
      (some #{"Marker (override)"} (:texts s)))))
   v24_l138)))


(def
 v27_l157
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})))


(deftest
 t28_l160
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (texts "z") (not (texts "fill")))))
   v27_l157)))


(def
 v30_l166
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:fill-label "Score"})))


(deftest
 t31_l170
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (some #{"Score"} (:texts s)) (pos? (:visible-tiles s)))))
   v30_l166)))


(def
 v33_l179
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:color-label "Score"})))


(deftest
 t34_l183
 (is ((fn [v] (some #{"Score"} (:texts (pj/svg-summary v)))) v33_l179)))


(def
 v36_l193
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:color :species})))


(deftest
 t37_l196
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 3 (:polygons s)) (= 3 (count fills)))))
   v36_l193)))


(def
 v39_l212
 (->
  {:product
   (map (fn* [p1__83638#] (str "Product " p1__83638#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t40_l217
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-45")))
   v39_l212)))


(def
 v42_l226
 (->
  {:product
   (map (fn* [p1__83639#] (str "Product " p1__83639#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45, :x-tick-label-pad 90})))


(deftest
 t43_l232
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v42_l226)))


(def
 v45_l244
 (->
  {:team ["Operations" "Research" "Customer support"],
   :headcount [42 17 29]}
  (pj/lay-bar :team :headcount)
  (pj/coord :flip)
  (pj/options {:y-tick-angle -90})))


(deftest
 t46_l250
 (is
  ((fn
    [v]
    (and
     (= 3 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-90")))
   v45_l244)))


(def
 v48_l265
 (->
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"],
   :tickets [462389 181444 163294]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator ","})))


(deftest
 t49_l271
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "462,389") (contains? texts "100,000"))))
   v48_l265)))


(def
 v51_l281
 (->
  {:violation ["Meter Expired" "Over Time Limit"],
   :tickets [462389 181444]}
  (pj/lay-bar :tickets :violation)
  (pj/lay-label :tickets :violation {:text :tickets, :align-x :right})
  (pj/options {:thousands-separator "."})))


(deftest
 t52_l287
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "462.389"))
   v51_l281)))


(def
 v54_l295
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
 t55_l304
 (is ((fn [m] (< (:grouped m) (:ungrouped m))) v54_l295)))


(def
 v57_l309
 (->
  {:x [1], :y [1], :amount [1234.56]}
  (pj/lay-label :x :y {:text :amount})
  (pj/options {:thousands-separator ","})))


(deftest
 t58_l313
 (is
  ((fn [v] (contains? (set (:texts (pj/svg-summary v))) "1,234.56"))
   v57_l309)))


(def
 v60_l325
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
 t61_l334
 (is ((fn [labels] (= "2,020" (first labels))) v60_l325)))


(def
 v63_l340
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
 t64_l349
 (is
  ((fn [labels] (= ["2020" "2021" "2022" "2023"] (vec labels)))
   v63_l340)))


(def
 v66_l355
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
  (filter (fn* [p1__83640#] (re-find #"," p1__83640#)))
  distinct
  sort))


(deftest
 t67_l365
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
   v66_l355)))


(def
 v69_l378
 (->
  {:region ["North" "South" "East"], :profit [1234.5 1500.25 2680.75]}
  (pj/lay-bar :profit :region)
  (pj/lay-label :profit :region {:text :profit, :align-x :right})
  (pj/options {:thousands-separator ".", :decimal-separator ","})))


(deftest
 t71_l388
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "1.234,5")
      (contains? texts "2.680,75")
      (some (fn [t] (re-matches #"\d\.\d00" t)) texts))))
   v69_l378)))


(def
 v73_l407
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
 t74_l415
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v73_l407)))


(def
 v76_l430
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t77_l433
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
   v76_l430)))


(def
 v79_l441
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t80_l444
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v79_l441)))


(def
 v82_l452
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dashed})))


(deftest
 t83_l455
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v82_l452)))


(def
 v85_l461
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dotted})))


(deftest
 t86_l464
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "1.00 3.00"))))
   v85_l461)))


(def
 v88_l470
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :solid})))


(deftest
 t89_l473
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (= 0 (:dashed-lines s)))))
   v88_l470)))


(def
 v91_l480
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash [12 4]})))


(deftest
 t92_l483
 (is
  ((fn
    [v]
    (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))
   v91_l480)))


(def
 v94_l487
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t95_l490
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v94_l487)))


(def
 v97_l519
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
 t98_l527
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:bold-texts s))
      (= 0 (:italic-texts s))
      (every? (set (:texts s)) ["steady" "dip" "peak"]))))
   v97_l519)))


(def
 v100_l538
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
 t101_l544
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:italic-texts s)) (= 0 (:bold-texts s)))))
   v100_l538)))


(def
 v103_l560
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag, :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(def
 v104_l563
 (->
  {:x [1], :y [1]}
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :box true,
    :data {:x [1], :y [1], :tag ["a boxed label"]}})))


(deftest
 t105_l567
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
   v104_l563)))


(def
 v107_l588
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
 t108_l597
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
        (filter (fn* [p1__83641#] (= :text (:mark p1__83641#))))
        (mapv
         (fn*
          [p1__83642#]
          (-> p1__83642# :style :box :corner-radius))))))))
   v107_l588)))


(def
 v110_l610
 (->
  {:x [1], :y [1]}
  (pj/lay-label
   :x
   :y
   {:text :tag,
    :box false,
    :data {:x [1], :y [1], :tag ["bare text"]}})))


(deftest
 t111_l614
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
   v110_l610)))


(def
 v113_l637
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.4})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#cc3311"})
  (pj/lay-rule-h {:y-intercept 3.0, :color "#4477aa", :offset-y -25})))


(deftest
 t114_l642
 (is
  ((fn
    [fr]
    (=
     [nil nil -25]
     (mapv :offset-y (:layers (first (:panels (pj/plan fr)))))))
   v113_l637)))


(def v116_l651 (:band-opacity (pj/config)))


(deftest t117_l653 (is ((fn [v] (= 0.15 v)) v116_l651)))


(def
 v119_l657
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t120_l662
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v119_l657)))


(def v122_l668 (:band-color (pj/config)))


(deftest t123_l670 (is ((fn [v] (= "#7F7F7F" v)) v122_l668)))


(def
 v125_l675
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/options {:band-color "#2a9d8f"})))


(deftest
 t126_l680
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
       (filter (fn* [p1__83643#] (= :band-v (:mark p1__83643#))))
       first)]
     (= [(/ 42 255.0) (/ 157 255.0) (/ 143 255.0) 1.0] (:color band))))
   v125_l675)))


(def
 v128_l703
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-v
   {:x-intercept 6.0, :color "gray", :stroke-dash :dashed})))


(deftest
 t129_l707
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v128_l703)))


(def
 v131_l718
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-rule-h
   {:y-intercept 3.0, :size 8, :alpha 0.3, :color "#cc3311"})
  (pj/lay-point {:color :species})))


(deftest
 t132_l723
 (is
  ((fn
    [fr]
    (let
     [layers (:layers (first (:panels (pj/plan fr))))]
     (and
      (= [:rule-h :point] (mapv :mark layers))
      (= 8 (:stroke-width (:style (first layers))))
      (= 0.3 (:opacity (:style (first layers)))))))
   v131_l718)))


(def v134_l745 (c2d/find-palette #"budapest"))


(deftest
 t135_l747
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v134_l745)))


(def v137_l751 (c2d/find-palette #"^:set"))


(deftest
 t138_l753
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v137_l751)))


(def v140_l757 (c2d/find-gradient #"viridis"))


(deftest
 t141_l759
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v140_l757)))


(def v143_l764 (c2d/palette :grand-budapest-1))


(deftest
 t144_l766
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v143_l764)))


(def
 v146_l778
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-values :khroma/okabeito})))


(deftest
 t147_l782
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v146_l778)))


(def
 v149_l788
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t150_l793
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v149_l788)))


(def
 v152_l801
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t153_l805
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v152_l801)))


(def
 v155_l811
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t156_l815
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v155_l811)))


(def
 v158_l820
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:legend-position :bottom})))


(deftest
 t159_l824
 (is
  ((fn
    [v]
    (let
     [layout (:layout (pj/plan v))]
     (and (pos? (:legend-h layout)) (zero? (:legend-w layout)))))
   v158_l820)))


(def
 v161_l836
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t162_l840
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
   v161_l836)))


(def
 v164_l857
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-font-size 14})))


(deftest
 t165_l861
 (is
  ((fn
    [v]
    (let
     [legend-w (fn [pose] (get-in (pj/plan pose) [:layout :legend-w]))]
     (and
      (= 150 (:points (pj/svg-summary v)))
      (>
       (legend-w v)
       (legend-w (pj/options v {:legend-font-size 10}))))))
   v164_l857)))


(def v167_l872 (:legend-font-size (pj/config)))


(deftest t168_l874 (is ((fn [n] (= 10 n)) v167_l872)))
