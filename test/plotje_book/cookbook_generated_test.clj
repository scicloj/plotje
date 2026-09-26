(ns
 plotje-book.cookbook-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [fastmath.stats :as fstats]
  [java-time.api :as jt]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l29
 (->
  (rdatasets/datasets-iris)
  (pj/lay-boxplot :species :sepal-length)
  (pj/lay-point {:jitter true, :alpha 0.3})))


(deftest
 t4_l33
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (= 3 (:polygons s)))))
   v3_l29)))


(def
 v6_l42
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:normalize :density, :alpha 0.5})
  pj/lay-density))


(deftest
 t7_l46
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v6_l42)))


(def
 v9_l54
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:alpha 0.6})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t10_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v9_l54)))


(def
 v12_l67
 (->
  (rdatasets/datasets-iris)
  (pj/lay-violin :species :petal-width {:alpha 0.3})
  (pj/lay-point {:jitter true, :alpha 0.4})))


(deftest
 t13_l71
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:polygons s)))))
   v12_l67)))


(def
 v15_l80
 (def
  ts-dates
  (take 52 (jt/iterate jt/plus (jt/local-date 2020 1 6) (jt/weeks 1)))))


(def
 v16_l82
 (def
  ts-ds
  {:date ts-dates,
   :value
   (map
    (fn*
     [p1__73957#]
     (+ 100.0 (* 30.0 (Math/sin (* (double p1__73957#) 0.12)))))
    (range 52))}))


(def
 v17_l86
 (->
  ts-ds
  (pj/lay-area :date :value {:alpha 0.2})
  pj/lay-line
  (pj/lay-point {:alpha 0.5})))


(deftest
 t18_l91
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 52 (:points s)) (= 1 (:lines s)) (= 1 (:polygons s)))))
   v17_l86)))


(def
 v20_l100
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t21_l104
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v20_l100)))


(def
 v23_l115
 (->
  {:task ["Design" "Build" "Test" "Ship"],
   :start
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-15T00:00:00.000-00:00"
    #inst "2024-04-15T00:00:00.000-00:00"],
   :end
   [#inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-20T00:00:00.000-00:00"
    #inst "2024-04-15T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"]}
  (pj/lay-interval-h :start :task {:x-end :end})
  (pj/options {:title "Project schedule"})))


(deftest
 t24_l123
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v23_l115)))


(def
 v26_l132
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length {:color :species})))


(deftest
 t27_l135
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (= 3 (:lines s)))))
   v26_l132)))


(def
 v29_l143
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t30_l146
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v29_l143)))


(def
 v32_l156
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-smooth {:stat :linear-model, :color nil})))


(deftest
 t33_l160
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v32_l156)))


(def
 v35_l170
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha 0.3})
  (pj/lay-point
   {:data {:sepal-length [5.0 6.5], :sepal-width [3.5 3.0]},
    :x :sepal-length,
    :y :sepal-width,
    :color "red",
    :size 6})))


(deftest
 t36_l177
 (is ((fn [v] (= 152 (:points (pj/svg-summary v)))) v35_l170)))


(def
 v38_l188
 (->
  {:violation ["Meter" "Over time" "Parking" "Bus zone"],
   :tickets [462 181 92 30]}
  (pj/lay-bar :violation :tickets)
  (pj/coord :flip)
  (pj/lay-segment {:x 3.45, :y 300, :x-end 3, :y-end 100, :arrow :end})
  (pj/lay-text {:x 3.6, :y 300, :text "Parking ends a tier"})))


(deftest
 t39_l195
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:polygons s))
      (= 1 (:lines s))
      (contains? (set (:texts s)) "Parking ends a tier"))))
   v38_l188)))


(def
 v41_l206
 (def
  experiment
  {:condition ["A" "B" "C" "D"],
   :mean [10.0 15.0 12.0 18.0],
   :ci_lo [8.0 12.0 9.5 15.5],
   :ci_hi [12.0 18.0 14.5 20.5]}))


(def
 v42_l212
 (->
  experiment
  (pj/lay-point :condition :mean {:size 5})
  (pj/lay-errorbar {:y-min :ci_lo, :y-max :ci_hi})))


(deftest
 t43_l216
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v42_l212)))


(def
 v45_l224
 (->
  experiment
  (pj/lay-lollipop :condition :mean)
  (pj/lay-errorbar {:y-min :ci_lo, :y-max :ci_hi})))


(deftest
 t46_l228
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 16 (:lines s)))))
   v45_l224)))


(def
 v48_l236
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:alpha 0.3, :jitter 5})
  (pj/lay-summary {:color :species})))


(deftest
 t49_l240
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 153 (:points s)) (= 3 (:lines s)))))
   v48_l236)))


(def
 v51_l248
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :smoker})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options
   {:title "Tipping Behavior",
    :x-label "Total Bill ($)",
    :y-label "Tip ($)"})))


(deftest
 t52_l256
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (pos? (:points s))
      (= 2 (:lines s))
      (some #{"Tipping Behavior"} (:texts s)))))
   v51_l248)))


(def
 v54_l268
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:alpha 0.5})
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})
  (pj/options {:title "Sepal Regression with Confidence Bands"})))


(deftest
 t55_l274
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v54_l268)))


(def
 v57_l283
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options {:title "Dodged Bars (default)"})))


(deftest
 t58_l287
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v57_l283)))


(def
 v59_l289
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :sex})
  (pj/options {:title "Stacked Bars"})))


(deftest
 t60_l293
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v59_l289)))


(def
 v62_l300
 (def
  daily-temps
  {:day (range 1 15),
   :temp [12 14 14 16 18 17 15 13 14 16 19 21 20 18]}))


(def
 v63_l304
 (->
  daily-temps
  (pj/lay-step :day :temp {:color "#2196F3"})
  (pj/lay-point {:color "#2196F3", :size 3})
  (pj/options {:title "Daily Temperature (Step)"})))


(deftest
 t64_l309
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (pos? (:lines s))
      (pos? (:points s))
      (contains? (:colors s) "rgb(33,150,243)")
      (contains? (:sizes s) 3.0))))
   v63_l304)))


(def
 v66_l320
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.4})
  (pj/lay-contour {:levels 5})))


(deftest
 t67_l324
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v66_l320)))


(def
 v69_l338
 (def
  longest-per-species
  (->
   (rdatasets/datasets-iris)
   (tc/group-by :species)
   (tc/order-by :sepal-length :desc)
   (tc/head 1)
   tc/ungroup)))


(def v70_l345 longest-per-species)


(def
 v71_l347
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:size 3, :color "#bbbbbb"})
  (pj/lay-point
   {:data longest-per-species,
    :x :sepal-length,
    :y :sepal-width,
    :size 5,
    :color "#cc3311"})
  (pj/lay-label
   {:data longest-per-species,
    :x :sepal-length,
    :y :sepal-width,
    :text :species,
    :align-x :right,
    :offset-x -8})))


(deftest
 t72_l356
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 153 (:points s))
      (= 3 (:label-boxes s))
      (=
       #{"versicolor" "setosa" "virginica"}
       (set
        (filter #{"versicolor" "setosa" "virginica"} (:texts s)))))))
   v71_l347)))


(def
 v74_l374
 (def
  species-share
  {:species ["setosa" "versicolor" "virginica"],
   :percent [33.3 33.3 33.3]}))


(def
 v75_l378
 (->
  species-share
  (pj/lay-bar :species :percent {:color "#a6cee3"})
  (pj/lay-text :species :percent {:text :percent, :align-x :right})
  (pj/coord :flip)))


(deftest
 t76_l383
 (is
  ((fn
    [fr]
    (let
     [text-layer
      (->>
       fr
       pj/plan
       :panels
       first
       :layers
       (filter (fn* [p1__73958#] (= :text (:mark p1__73958#))))
       first)]
     (= :right (-> text-layer :style :align-x))))
   v75_l378)))


(def
 v78_l398
 (->
  (rdatasets/datasets-iris)
  (pj/lay-bar :species)
  (pj/lay-label {:stat :count, :align-x :center})))


(deftest
 t79_l402
 (is
  ((fn
    [fr]
    (=
     ["50" "50" "50"]
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      (filter (fn* [p1__73959#] (= :text (:mark p1__73959#))))
      first
      :groups
      first
      :labels)))
   v78_l398)))


(def
 v81_l417
 (->
  {:sex ["male" "male" "female" "female"],
   :species ["cat" "dog" "cat" "dog"],
   :percent [21 17 9 14]}
  (pj/pose :sex :percent)
  (pj/lay-bar {:color :species})
  (pj/lay-label {:text :percent, :group :species, :align-x :center})))


(deftest
 t82_l424
 (is
  ((fn
    [fr]
    (let
     [layers
      (->> fr pj/plan :panels first :layers)
      groups
      (fn
       [mark]
       (->>
        layers
        (filter (fn* [p1__73960#] (= mark (:mark p1__73960#))))
        first
        :groups
        (mapv (juxt :label :dodge-idx))))]
     (= (groups :rect) (groups :text))))
   v81_l417)))


(def
 v84_l437
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:color-values
    {:setosa "#E91E63", :versicolor "#4CAF50", :virginica "#2196F3"},
    :title "Custom Palette Map"})))


(deftest
 t85_l444
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)))))
   v84_l437)))


(def
 v87_l453
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/coord :fixed)
  (pj/options {:title "Fixed Aspect Ratio"})))


(deftest
 t88_l460
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (= 3 (:lines s)))))
   v87_l453)))


(def
 v90_l469
 (->
  {:x (range 20),
   :y
   (map (fn* [p1__73961#] (Math/sin (/ p1__73961# 3.0))) (range 20)),
   :change (map (fn* [p1__73962#] (- p1__73962# 10)) (range 20))}
  (pj/lay-point :x :y {:color :change})
  (pj/options
   {:color-range :diverging,
    :color-midpoint 0,
    :title "Diverging Color Scale"})))


(deftest
 t91_l477
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 20 (:points s)))))
   v90_l469)))


(def
 v93_l485
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options {:title "LOESS with 95% CI"})))


(deftest
 t94_l491
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)) (= 3 (:polygons s)))))
   v93_l485)))


(def
 v96_l500
 (def
  iris-sepal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Sepal", :width 300, :height 250}))))


(def
 v97_l505
 (def
  iris-petal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width {:color :species})
   (pj/options {:title "Petal", :width 300, :height 250}))))


(def
 v98_l510
 (pj/arrange
  [iris-sepal iris-petal]
  {:title "Iris Dashboard", :cols 2}))


(deftest
 t99_l513
 (is
  ((fn [v] (and (pj/pose? v) (= "Iris Dashboard" (-> v :opts :title))))
   v98_l510)))


(def
 v101_l524
 (def
  top-cities
  {:city ["Tokyo" "Delhi" "Shanghai" "São Paulo" "Mumbai"],
   :population [37.4 32.9 29.2 22.4 21.7],
   :area [2194 1484 6341 1521 603]}))


(def
 v102_l529
 (->
  top-cities
  (pj/lay-point :area :population)
  (pj/lay-text {:text :city, :align-x :center, :offset-y -10})
  (pj/options {:title "Population vs Area"})))


(deftest
 t103_l534
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:points s))
      (every? (set (:texts s)) ["Tokyo" "Delhi"]))))
   v102_l529)))


(def
 v105_l568
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5, :alpha 0.3})))


(deftest
 t106_l573
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v105_l568)))


(def
 v108_l586
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length)
  (pj/lay-rule-v
   {:x-intercept
    (fstats/mean (:sepal-length (rdatasets/datasets-iris))),
    :color "firebrick",
    :size 2})))


(deftest
 t109_l592
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v108_l586)))


(def
 v111_l601
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length)
  (pj/lay-rule-h {:y-intercept 20, :color "firebrick"})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5, :alpha 0.2})))


(deftest
 t112_l606
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (pos? (:visible-tiles s)))))
   v111_l601)))


(def
 v114_l617
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length)
  (pj/lay-rug :sepal-length)
  (pj/lay-rule-v
   {:x-intercept
    (fstats/mean (:sepal-length (rdatasets/datasets-iris))),
    :color "firebrick"})))


(deftest
 t115_l623
 (is
  ((fn
    [fr]
    (let
     [top
      (fn [p] (second (:y-domain (first (:panels (pj/plan p))))))
      bare
      (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length))]
     (and (< (top fr) 0.5) (= (top fr) (top bare)))))
   v114_l617)))


(def
 v117_l640
 (def
  life-tracks
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows
    (fn*
     [p1__73963#]
     (#{"Cambodia" "Botswana" "Japan" "Rwanda" "China"}
      (:country p1__73963#))))
   (tc/select-columns [:country :year :life-exp]))))


(def
 v118_l646
 (->
  life-tracks
  (pj/lay-line :year :life-exp {:color :country})
  (pj/options
   {:title "Life expectancy at birth", :width 620, :height 380})))


(deftest
 t119_l651
 (is ((fn [v] (= 5 (:lines (pj/svg-summary v)))) v118_l646)))


(def
 v121_l661
 (->
  life-tracks
  (pj/lay-line :year :life-exp {:color :country})
  (pj/lay-text
   {:data
    (tc/select-rows
     life-tracks
     (fn* [p1__73964#] (= 2007 (:year p1__73964#)))),
    :x :year,
    :y :life-exp,
    :text :country,
    :color :country,
    :offset-x 8})
  (pj/options
   {:title "Life expectancy at birth",
    :width 620,
    :height 380,
    :legend-position :none})))


(deftest
 t122_l670
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:lines s))
      (every?
       (set (:texts s))
       ["Rwanda" "Cambodia" "China" "Japan" "Botswana"]))))
   v121_l661)))


(def
 v124_l687
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn* [p1__73965#] (= "Rwanda" (:country p1__73965#))))
  (pj/lay-line :year :life-exp {:color "#4477aa"})
  (pj/lay-point
   {:data {:year [1992], :life-exp [23.599]},
    :x :year,
    :y :life-exp,
    :color "#cc3311",
    :size 6})
  (pj/lay-line
   {:data {:year [1972 1990], :life-exp [30 24.5]},
    :x :year,
    :y :life-exp,
    :color "#777777",
    :stroke-dash :dotted})
  (pj/lay-text
   {:x 1971,
    :y 30,
    :align-x :right,
    :offset-x -4,
    :color "#333333",
    :text "life expectancy fell to 23.6 years in 1992"})
  (pj/lay-text
   {:in :drawing-area,
    :x 10,
    :y 8,
    :color "#777777",
    :text "Rwanda, 1952-2007"})
  (pj/options
   {:width 640, :height 400, :y-label "life expectancy at birth"})))


(deftest
 t125_l703
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (every?
       (set (:texts s))
       ["life expectancy fell to 23.6 years in 1992"
        "Rwanda, 1952-2007"])
      (= 1 (:points s)))))
   v124_l687)))


(def
 v127_l731
 (def
  life-history
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-columns [:country :year :life-exp]))))


(def
 v128_l735
 (def
  ends-highest
  (->
   life-history
   (tc/select-rows (fn* [p1__73966#] (= 2007 (:year p1__73966#))))
   (tc/order-by :life-exp :desc)
   (tc/rows :as-maps)
   first
   :country)))


(def
 v129_l742
 (def
  gained-most
  (->
   life-history
   (tc/group-by :country)
   (tc/aggregate
    {:gain
     (fn
      [ds]
      (- (reduce max (:life-exp ds)) (reduce min (:life-exp ds))))})
   (tc/order-by :gain :desc)
   (tc/rows :as-maps)
   first
   :$group-name)))


(def
 v131_l754
 (def
  sharpest-fall
  (->
   life-history
   (tc/order-by [:country :year])
   (tc/group-by :country)
   (tc/aggregate
    {:fall
     (fn
      [ds]
      (let
       [ys (vec (:life-exp ds))]
       (reduce min 0 (map - (rest ys) ys))))})
   (tc/order-by :fall)
   (tc/rows :as-maps)
   first)))


(def v132_l765 [ends-highest gained-most sharpest-fall])


(deftest
 t133_l767
 (is
  ((fn
    [[a b c]]
    (and
     (= "Japan" a)
     (= "Oman" b)
     (= "Rwanda" (:$group-name c))
     (< -21 (:fall c) -20)))
   v132_l765)))


(def
 v135_l777
 (let
  [named
   #{ends-highest gained-most (:$group-name sharpest-fall)}
   chosen
   (tc/select-rows
    life-history
    (fn* [p1__73967#] (named (:country p1__73967#))))]
  (->
   life-history
   (pj/lay-line :year :life-exp {:group :country, :color "#d0d0d0"})
   (pj/lay-line
    {:data chosen, :x :year, :y :life-exp, :color :country})
   (pj/lay-text
    {:data
     (tc/select-rows
      chosen
      (fn* [p1__73968#] (= 2007 (:year p1__73968#)))),
     :x :year,
     :y :life-exp,
     :text :country,
     :color :country,
     :offset-x 8})
   (pj/lay-line
    {:data {:year [1972 1989], :life-exp [31 25]},
     :x :year,
     :y :life-exp,
     :color "#777777",
     :stroke-dash :dotted})
   (pj/lay-text
    {:x 1971,
     :y 31,
     :align-x :right,
     :offset-x -4,
     :color "#333333",
     :text
     (format
      "%s, 1992: a fall of %.0f years in one step"
      (:$group-name sharpest-fall)
      (- (:fall sharpest-fall)))})
   (pj/lay-text
    {:in :drawing-area,
     :x 10,
     :y 8,
     :color "#888888",
     :text
     (format
      "%d countries, 1952-2007"
      (count (distinct (:country life-history))))})
   (pj/options
    {:width 760,
     :height 430,
     :legend-position :none,
     :y-label "life expectancy at birth"}))))


(deftest
 t136_l799
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (every? (set (:texts s)) ["Japan" "Oman" "Rwanda"])
      (some
       (fn*
        [p1__73969#]
        (re-find #"^Rwanda, 1992: a fall of 20 years" p1__73969#))
       (:texts s))
      (some
       (fn* [p1__73970#] (= "142 countries, 1952-2007" p1__73970#))
       (:texts s)))))
   v135_l777)))


(def
 v138_l813
 (let
  [r
   (rng/rng :jdk 77)
   xs
   (range 0 10 0.5)
   ys
   (map
    (fn*
     [p1__73971#]
     (+ (* 3 p1__73971#) 5 (* 2 (- (rng/drandom r) 0.5))))
    xs)]
  (->
   {:x xs, :y ys}
   (pj/lay-point :x :y)
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Simulated: y = 3x + 5 + noise"}))))


(deftest
 t139_l824
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 20 (:points s))
      (= 1 (:lines s))
      (some #{"Simulated: y = 3x + 5 + noise"} (:texts s)))))
   v138_l813)))


(def
 v141_l835
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
  (pj/options {:title "Palmer Penguins: Bill Dimensions"})))


(deftest
 t142_l839
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 342 (:points s)))))
   v141_l835)))


(def
 v144_l845
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/pose :bill-length-mm :bill-depth-mm {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Bill Length vs Depth with Regression"})))


(deftest
 t145_l851
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 3 (:lines s)))))
   v144_l845)))


(def
 v147_l858
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
  (pj/lay-smooth {:stat :linear-model, :color nil})
  (pj/options
   {:title "Simpson's Paradox: Overall vs Per-Group Trend"})))


(deftest
 t148_l863
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 1 (:lines s)))))
   v147_l858)))


(def
 v150_l869
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})
  (pj/options {:title "Species by Island"})))


(deftest
 t151_l873
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v150_l869)))


(def
 v153_l879
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/pose :flipper-length-mm :body-mass-g {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Flipper Length vs Body Mass"})))


(deftest
 t154_l885
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 3 (:lines s)))))
   v153_l879)))


(def
 v156_l891
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-histogram :body-mass-g {:color :species})
  (pj/options {:title "Body Mass Distribution"})))


(deftest
 t157_l895
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v156_l891)))


(def
 v159_l903
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :smoker})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options
   {:title "Tipping: Smokers vs Non-Smokers",
    :x-label "Total Bill ($)",
    :y-label "Tip ($)"})))


(deftest
 t160_l910
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 244 (:points s)) (= 2 (:lines s)))))
   v159_l903)))


(def
 v162_l916
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :time})
  (pj/options {:title "Visits by Day and Meal Time"})))


(deftest
 t163_l920
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v162_l916)))


(def
 v165_l926
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :time})
  (pj/options {:title "Visits by Day (Stacked)"})))


(deftest
 t166_l930
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v165_l926)))


(def
 v168_l936
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/coord :flip)
  (pj/options {:title "Day by Gender (Horizontal)"})))


(deftest
 t169_l941
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v168_l936)))


(def
 v171_l949
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy {:color :class})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Displacement vs Highway MPG by Class"})))


(deftest
 t172_l955
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 234 (:points s)) (pos? (:lines s)))))
   v171_l949)))


(def
 v174_l961
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :cty {:color :drv})
  (pj/options {:title "Engine Displacement vs City Fuel Efficiency"})))


(deftest
 t175_l965
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 234 (:points s)))))
   v174_l961)))


(def
 v177_l971
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-bar :drv)
  (pj/options {:title "Cars by Drive Type"})))


(deftest
 t178_l975
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v177_l971)))


(def
 v180_l996
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 500)
  (pj/lay-point :carat :price {:color :cut})
  (pj/options {:title "Diamonds (500 rows, SVG)"})))


(deftest
 t181_l1001
 (is ((fn [v] (= 500 (:points (pj/svg-summary v)))) v180_l996)))


(def
 v183_l1008
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:color :cut, :alpha 0.3})
  (pj/options
   {:title "Diamonds (53,940 rows, BufferedImage)", :format :bufimg})))


(deftest
 t184_l1013
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v183_l1008)))


(def
 v186_l1026
 (def
  quarterly-revenue
  (->
   {:quarter ["Q1 2024" "Q2 2024" "Q3 2024" "Q4 2024"],
    :revenue [1250000 1480000 1310000 1720000]}
   (pj/lay-bar :quarter :revenue)
   (pj/options
    {:x-tick-angle -45,
     :y-label "revenue in US dollars",
     :thousands-separator ","}))))


(def v187_l1034 quarterly-revenue)


(deftest
 t188_l1036
 (is
  ((fn [v] (.contains (pr-str (pj/plot v)) "rotate(-45")) v187_l1034)))


(def v190_l1043 (pj/options quarterly-revenue {:format :bufimg}))


(deftest
 t191_l1045
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v190_l1043)))


(def
 v193_l1053
 (let
  [path (str (java.io.File/createTempFile "plotje-diamonds" ".png"))]
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:color :cut})
   (pj/save path))
  (with-open
   [in (java.io.FileInputStream. path)]
   (let
    [bs (byte-array 8)]
    (.read in bs)
    (mapv (fn* [p1__73972#] (bit-and p1__73972# 255)) (vec bs))))))


(deftest
 t194_l1063
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v193_l1053)))
