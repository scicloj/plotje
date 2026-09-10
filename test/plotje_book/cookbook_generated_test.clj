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
     [p1__74841#]
     (+ 100.0 (* 30.0 (Math/sin (* (double p1__74841#) 0.12)))))
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
 v38_l184
 (def
  experiment
  {:condition ["A" "B" "C" "D"],
   :mean [10.0 15.0 12.0 18.0],
   :ci_lo [8.0 12.0 9.5 15.5],
   :ci_hi [12.0 18.0 14.5 20.5]}))


(def
 v39_l190
 (->
  experiment
  (pj/lay-point :condition :mean {:size 5})
  (pj/lay-errorbar {:y-min :ci_lo, :y-max :ci_hi})))


(deftest
 t40_l194
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v39_l190)))


(def
 v42_l202
 (->
  experiment
  (pj/lay-lollipop :condition :mean)
  (pj/lay-errorbar {:y-min :ci_lo, :y-max :ci_hi})))


(deftest
 t43_l206
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 16 (:lines s)))))
   v42_l202)))


(def
 v45_l214
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:alpha 0.3, :jitter 5})
  (pj/lay-summary {:color :species})))


(deftest
 t46_l218
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 153 (:points s)) (= 3 (:lines s)))))
   v45_l214)))


(def
 v48_l226
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
 t49_l234
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (pos? (:points s))
      (= 2 (:lines s))
      (some #{"Tipping Behavior"} (:texts s)))))
   v48_l226)))


(def
 v51_l246
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:alpha 0.5})
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})
  (pj/options {:title "Sepal Regression with Confidence Bands"})))


(deftest
 t52_l252
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v51_l246)))


(def
 v54_l261
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options {:title "Dodged Bars (default)"})))


(deftest
 t55_l265
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v54_l261)))


(def
 v56_l267
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :sex})
  (pj/options {:title "Stacked Bars"})))


(deftest
 t57_l271
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v56_l267)))


(def
 v59_l278
 (def
  daily-temps
  {:day (range 1 15),
   :temp [12 14 14 16 18 17 15 13 14 16 19 21 20 18]}))


(def
 v60_l282
 (->
  daily-temps
  (pj/lay-step :day :temp {:color "#2196F3"})
  (pj/lay-point {:color "#2196F3", :size 3})
  (pj/options {:title "Daily Temperature (Step)"})))


(deftest
 t61_l287
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
   v60_l282)))


(def
 v63_l298
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.4})
  (pj/lay-contour {:levels 5})))


(deftest
 t64_l302
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v63_l298)))


(def
 v66_l310
 (def
  top5
  (->
   (rdatasets/datasets-iris)
   (tc/order-by :sepal-length :desc)
   (tc/head 5))))


(def
 v67_l315
 (->
  top5
  (pj/lay-point :sepal-length :sepal-width {:size 5})
  (pj/lay-label {:text :species, :nudge-y 0.15})))


(deftest
 t68_l319
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (pos? (:points s))
      (some
       (fn* [p1__74842#] (= "virginica" p1__74842#))
       (:texts s)))))
   v67_l315)))


(def
 v70_l330
 (def
  species-share
  {:species ["setosa" "versicolor" "virginica"],
   :percent [33.3 33.3 33.3]}))


(def
 v71_l334
 (->
  species-share
  (pj/lay-bar :species :percent {:color "#a6cee3"})
  (pj/lay-text :species :percent {:text :percent, :align-x :right})
  (pj/coord :flip)))


(deftest
 t72_l339
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
       (filter (fn* [p1__74843#] (= :text (:mark p1__74843#))))
       first)]
     (= :right (-> text-layer :style :align-x))))
   v71_l334)))


(def
 v74_l354
 (->
  (rdatasets/datasets-iris)
  (pj/lay-bar :species)
  (pj/lay-label {:stat :count, :align-x :center})))


(deftest
 t75_l358
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
      (filter (fn* [p1__74844#] (= :text (:mark p1__74844#))))
      first
      :groups
      first
      :labels)))
   v74_l354)))


(def
 v77_l373
 (->
  {:sex ["male" "male" "female" "female"],
   :species ["cat" "dog" "cat" "dog"],
   :percent [21 17 9 14]}
  (pj/pose :sex :percent)
  (pj/lay-bar {:color :species})
  (pj/lay-label {:text :percent, :group :species, :align-x :center})))


(deftest
 t78_l380
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
        (filter (fn* [p1__74845#] (= mark (:mark p1__74845#))))
        first
        :groups
        (mapv (juxt :label :dodge-idx))))]
     (= (groups :rect) (groups :text))))
   v77_l373)))


(def
 v80_l393
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:color-values
    {:setosa "#E91E63", :versicolor "#4CAF50", :virginica "#2196F3"},
    :title "Custom Palette Map"})))


(deftest
 t81_l400
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)))))
   v80_l393)))


(def
 v83_l409
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/coord :fixed)
  (pj/options {:title "Fixed Aspect Ratio"})))


(deftest
 t84_l416
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (= 3 (:lines s)))))
   v83_l409)))


(def
 v86_l425
 (->
  {:x (range 20),
   :y
   (map (fn* [p1__74846#] (Math/sin (/ p1__74846# 3.0))) (range 20)),
   :change (map (fn* [p1__74847#] (- p1__74847# 10)) (range 20))}
  (pj/lay-point :x :y {:color :change})
  (pj/options
   {:color-range :diverging,
    :color-midpoint 0,
    :title "Diverging Color Scale"})))


(deftest
 t87_l433
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 20 (:points s)))))
   v86_l425)))


(def
 v89_l441
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options {:title "LOESS with 95% CI"})))


(deftest
 t90_l447
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)) (= 3 (:polygons s)))))
   v89_l441)))


(def
 v92_l456
 (def
  iris-sepal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Sepal", :width 300, :height 250}))))


(def
 v93_l461
 (def
  iris-petal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width {:color :species})
   (pj/options {:title "Petal", :width 300, :height 250}))))


(def
 v94_l466
 (pj/arrange
  [iris-sepal iris-petal]
  {:title "Iris Dashboard", :cols 2}))


(deftest
 t95_l469
 (is
  ((fn [v] (and (pj/pose? v) (= "Iris Dashboard" (-> v :opts :title))))
   v94_l466)))


(def
 v97_l476
 (def
  top-cities
  {:city ["Tokyo" "Delhi" "Shanghai" "São Paulo" "Mumbai"],
   :population [37.4 32.9 29.2 22.4 21.7],
   :area [2194 1484 6341 1521 603]}))


(def
 v98_l481
 (->
  top-cities
  (pj/lay-point :area :population)
  (pj/lay-text {:text :city, :nudge-y 1.0})
  (pj/options {:title "Population vs Area"})))


(deftest
 t99_l486
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:points s))
      (every? (set (:texts s)) ["Tokyo" "Delhi"]))))
   v98_l481)))


(def
 v101_l520
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5, :alpha 0.3})))


(deftest
 t102_l525
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v101_l520)))


(def
 v104_l538
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length)
  (pj/lay-rule-v
   {:x-intercept
    (fstats/mean (:sepal-length (rdatasets/datasets-iris))),
    :color "firebrick",
    :size 2})))


(deftest
 t105_l544
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v104_l538)))


(def
 v107_l553
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length)
  (pj/lay-rule-h {:y-intercept 20, :color "firebrick"})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5, :alpha 0.2})))


(deftest
 t108_l558
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (pos? (:visible-tiles s)))))
   v107_l553)))


(def
 v110_l569
 (def
  life-tracks
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows
    (fn*
     [p1__74848#]
     (#{"Cambodia" "Botswana" "Japan" "Rwanda" "China"}
      (:country p1__74848#))))
   (tc/select-columns [:country :year :life-exp]))))


(def
 v111_l575
 (->
  life-tracks
  (pj/lay-line :year :life-exp {:color :country})
  (pj/options
   {:title "Life expectancy at birth", :width 620, :height 380})))


(deftest
 t112_l580
 (is ((fn [v] (= 5 (:lines (pj/svg-summary v)))) v111_l575)))


(def
 v114_l590
 (->
  life-tracks
  (pj/lay-line :year :life-exp {:color :country})
  (pj/lay-text
   {:data
    (tc/select-rows
     life-tracks
     (fn* [p1__74849#] (= 2007 (:year p1__74849#)))),
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
 t115_l599
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
   v114_l590)))


(def
 v117_l616
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn* [p1__74850#] (= "Rwanda" (:country p1__74850#))))
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
 t118_l632
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
   v117_l616)))


(def
 v120_l660
 (def
  life-history
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-columns [:country :year :life-exp]))))


(def
 v121_l664
 (def
  ends-highest
  (->
   life-history
   (tc/select-rows (fn* [p1__74851#] (= 2007 (:year p1__74851#))))
   (tc/order-by :life-exp :desc)
   (tc/rows :as-maps)
   first
   :country)))


(def
 v122_l671
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
 v124_l683
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


(def v125_l694 [ends-highest gained-most sharpest-fall])


(deftest
 t126_l696
 (is
  ((fn
    [[a b c]]
    (and
     (= "Japan" a)
     (= "Oman" b)
     (= "Rwanda" (:$group-name c))
     (< -21 (:fall c) -20)))
   v125_l694)))


(def
 v128_l706
 (let
  [named
   #{ends-highest gained-most (:$group-name sharpest-fall)}
   chosen
   (tc/select-rows
    life-history
    (fn* [p1__74852#] (named (:country p1__74852#))))]
  (->
   life-history
   (pj/lay-line :year :life-exp {:group :country, :color "#d0d0d0"})
   (pj/lay-line
    {:data chosen, :x :year, :y :life-exp, :color :country})
   (pj/lay-text
    {:data
     (tc/select-rows
      chosen
      (fn* [p1__74853#] (= 2007 (:year p1__74853#)))),
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
 t129_l728
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (every? (set (:texts s)) ["Japan" "Oman" "Rwanda"])
      (some
       (fn*
        [p1__74854#]
        (re-find #"^Rwanda, 1992: a fall of 20 years" p1__74854#))
       (:texts s))
      (some
       (fn* [p1__74855#] (= "142 countries, 1952-2007" p1__74855#))
       (:texts s)))))
   v128_l706)))


(def
 v131_l742
 (let
  [r
   (rng/rng :jdk 77)
   xs
   (range 0 10 0.5)
   ys
   (map
    (fn*
     [p1__74856#]
     (+ (* 3 p1__74856#) 5 (* 2 (- (rng/drandom r) 0.5))))
    xs)]
  (->
   {:x xs, :y ys}
   (pj/lay-point :x :y)
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Simulated: y = 3x + 5 + noise"}))))


(deftest
 t132_l753
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 20 (:points s))
      (= 1 (:lines s))
      (some #{"Simulated: y = 3x + 5 + noise"} (:texts s)))))
   v131_l742)))


(def
 v134_l764
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
  (pj/options {:title "Palmer Penguins: Bill Dimensions"})))


(deftest
 t135_l768
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 342 (:points s)))))
   v134_l764)))


(def
 v137_l774
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/pose :bill-length-mm :bill-depth-mm {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Bill Length vs Depth with Regression"})))


(deftest
 t138_l780
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 3 (:lines s)))))
   v137_l774)))


(def
 v140_l787
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
  (pj/lay-smooth {:stat :linear-model, :color nil})
  (pj/options
   {:title "Simpson's Paradox: Overall vs Per-Group Trend"})))


(deftest
 t141_l792
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 1 (:lines s)))))
   v140_l787)))


(def
 v143_l798
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})
  (pj/options {:title "Species by Island"})))


(deftest
 t144_l802
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v143_l798)))


(def
 v146_l808
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/pose :flipper-length-mm :body-mass-g {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Flipper Length vs Body Mass"})))


(deftest
 t147_l814
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 3 (:lines s)))))
   v146_l808)))


(def
 v149_l820
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-histogram :body-mass-g {:color :species})
  (pj/options {:title "Body Mass Distribution"})))


(deftest
 t150_l824
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v149_l820)))


(def
 v152_l832
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
 t153_l839
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 244 (:points s)) (= 2 (:lines s)))))
   v152_l832)))


(def
 v155_l845
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :time})
  (pj/options {:title "Visits by Day and Meal Time"})))


(deftest
 t156_l849
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v155_l845)))


(def
 v158_l855
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :time})
  (pj/options {:title "Visits by Day (Stacked)"})))


(deftest
 t159_l859
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v158_l855)))


(def
 v161_l865
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/coord :flip)
  (pj/options {:title "Day by Gender (Horizontal)"})))


(deftest
 t162_l870
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v161_l865)))


(def
 v164_l878
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy {:color :class})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Displacement vs Highway MPG by Class"})))


(deftest
 t165_l884
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 234 (:points s)) (pos? (:lines s)))))
   v164_l878)))


(def
 v167_l890
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :cty {:color :drv})
  (pj/options {:title "Engine Displacement vs City Fuel Efficiency"})))


(deftest
 t168_l894
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 234 (:points s)))))
   v167_l890)))


(def
 v170_l900
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-bar :drv)
  (pj/options {:title "Cars by Drive Type"})))


(deftest
 t171_l904
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v170_l900)))


(def
 v173_l925
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 500)
  (pj/lay-point :carat :price {:color :cut})
  (pj/options {:title "Diamonds (500 rows, SVG)"})))


(deftest
 t174_l930
 (is ((fn [v] (= 500 (:points (pj/svg-summary v)))) v173_l925)))


(def
 v176_l937
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:color :cut, :alpha 0.3})
  (pj/options
   {:title "Diamonds (53,940 rows, BufferedImage)", :format :bufimg})))


(deftest
 t177_l942
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v176_l937)))


(def
 v179_l955
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


(def v180_l963 quarterly-revenue)


(deftest
 t181_l965
 (is
  ((fn [v] (.contains (pr-str (pj/plot v)) "rotate(-45")) v180_l963)))


(def v183_l972 (pj/options quarterly-revenue {:format :bufimg}))


(deftest
 t184_l974
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v183_l972)))


(def
 v186_l982
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
    (mapv (fn* [p1__74857#] (bit-and p1__74857# 255)) (vec bs))))))


(deftest
 t187_l992
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v186_l982)))
