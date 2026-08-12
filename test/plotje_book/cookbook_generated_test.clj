(ns
 plotje-book.cookbook-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [fastmath.random :as rng]
  [java-time.api :as jt]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l28
 (->
  (rdatasets/datasets-iris)
  (pj/lay-boxplot :species :sepal-length)
  (pj/lay-point {:jitter true, :alpha 0.3})))


(deftest
 t4_l32
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (= 3 (:polygons s)))))
   v3_l28)))


(def
 v6_l41
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:normalize :density, :alpha 0.5})
  pj/lay-density))


(deftest
 t7_l45
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v6_l41)))


(def
 v9_l53
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:alpha 0.6})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t10_l58
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v9_l53)))


(def
 v12_l66
 (->
  (rdatasets/datasets-iris)
  (pj/lay-violin :species :petal-width {:alpha 0.3})
  (pj/lay-point {:jitter true, :alpha 0.4})))


(deftest
 t13_l70
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:polygons s)))))
   v12_l66)))


(def
 v15_l79
 (def
  ts-dates
  (take 52 (jt/iterate jt/plus (jt/local-date 2020 1 6) (jt/weeks 1)))))


(def
 v16_l81
 (def
  ts-ds
  {:date ts-dates,
   :value
   (map
    (fn*
     [p1__73635#]
     (+ 100.0 (* 30.0 (Math/sin (* (double p1__73635#) 0.12)))))
    (range 52))}))


(def
 v17_l85
 (->
  ts-ds
  (pj/lay-area :date :value {:alpha 0.2})
  pj/lay-line
  (pj/lay-point {:alpha 0.5})))


(deftest
 t18_l90
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 52 (:points s)) (= 1 (:lines s)) (= 1 (:polygons s)))))
   v17_l85)))


(def
 v20_l99
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t21_l103
 (is ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v20_l99)))


(def
 v23_l114
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
 t24_l122
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v23_l114)))


(def
 v26_l131
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length {:color :species})))


(deftest
 t27_l134
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (= 3 (:lines s)))))
   v26_l131)))


(def
 v29_l142
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t30_l145
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v29_l142)))


(def
 v32_l155
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-smooth {:stat :linear-model, :color nil})))


(deftest
 t33_l159
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v32_l155)))


(def
 v35_l169
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
 t36_l176
 (is ((fn [v] (= 152 (:points (pj/svg-summary v)))) v35_l169)))


(def
 v38_l183
 (def
  experiment
  {:condition ["A" "B" "C" "D"],
   :mean [10.0 15.0 12.0 18.0],
   :ci_lo [8.0 12.0 9.5 15.5],
   :ci_hi [12.0 18.0 14.5 20.5]}))


(def
 v39_l189
 (->
  experiment
  (pj/lay-point :condition :mean {:size 5})
  (pj/lay-errorbar {:y-min :ci_lo, :y-max :ci_hi})))


(deftest
 t40_l193
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 12 (:lines s)))))
   v39_l189)))


(def
 v42_l201
 (->
  experiment
  (pj/lay-lollipop :condition :mean)
  (pj/lay-errorbar {:y-min :ci_lo, :y-max :ci_hi})))


(deftest
 t43_l205
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 16 (:lines s)))))
   v42_l201)))


(def
 v45_l213
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:alpha 0.3, :jitter 5})
  (pj/lay-summary {:color :species})))


(deftest
 t46_l217
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 153 (:points s)) (= 3 (:lines s)))))
   v45_l213)))


(def
 v48_l225
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
 t49_l233
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (pos? (:points s))
      (= 2 (:lines s))
      (some #{"Tipping Behavior"} (:texts s)))))
   v48_l225)))


(def
 v51_l245
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:alpha 0.5})
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})
  (pj/options {:title "Sepal Regression with Confidence Bands"})))


(deftest
 t52_l251
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v51_l245)))


(def
 v54_l260
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options {:title "Dodged Bars (default)"})))


(deftest
 t55_l264
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v54_l260)))


(def
 v56_l266
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :sex})
  (pj/options {:title "Stacked Bars"})))


(deftest
 t57_l270
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v56_l266)))


(def
 v59_l277
 (def
  daily-temps
  {:day (range 1 15),
   :temp [12 14 14 16 18 17 15 13 14 16 19 21 20 18]}))


(def
 v60_l281
 (->
  daily-temps
  (pj/lay-step :day :temp {:color "#2196F3"})
  (pj/lay-point {:color "#2196F3", :size 3})
  (pj/options {:title "Daily Temperature (Step)"})))


(deftest
 t61_l286
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
   v60_l281)))


(def
 v63_l297
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.4})
  (pj/lay-contour {:levels 5})))


(deftest
 t64_l301
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v63_l297)))


(def
 v66_l309
 (def
  top5
  (->
   (rdatasets/datasets-iris)
   (tc/order-by :sepal-length :desc)
   (tc/head 5))))


(def
 v67_l314
 (->
  top5
  (pj/lay-point :sepal-length :sepal-width {:size 5})
  (pj/lay-label {:text :species, :nudge-y 0.15})))


(deftest
 t68_l318
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (pos? (:points s))
      (some
       (fn* [p1__73636#] (= "virginica" p1__73636#))
       (:texts s)))))
   v67_l314)))


(def
 v70_l329
 (def
  species-share
  {:species ["setosa" "versicolor" "virginica"],
   :percent [33.3 33.3 33.3]}))


(def
 v71_l333
 (->
  species-share
  (pj/lay-bar :species :percent {:color "#a6cee3"})
  (pj/lay-text :species :percent {:text :percent, :align-x :right})
  (pj/coord :flip)))


(deftest
 t72_l338
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
       (filter (fn* [p1__73637#] (= :text (:mark p1__73637#))))
       first)]
     (= :right (-> text-layer :style :align-x))))
   v71_l333)))


(def
 v74_l353
 (->
  (rdatasets/datasets-iris)
  (pj/lay-bar :species)
  (pj/lay-label {:stat :count, :align-x :center})))


(deftest
 t75_l357
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
      (filter (fn* [p1__73638#] (= :text (:mark p1__73638#))))
      first
      :groups
      first
      :labels)))
   v74_l353)))


(def
 v77_l372
 (->
  {:sex ["male" "male" "female" "female"],
   :species ["cat" "dog" "cat" "dog"],
   :percent [21 17 9 14]}
  (pj/pose :sex :percent)
  (pj/lay-bar {:color :species})
  (pj/lay-label {:text :percent, :group :species, :align-x :center})))


(deftest
 t78_l379
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
        (filter (fn* [p1__73639#] (= mark (:mark p1__73639#))))
        first
        :groups
        (mapv (juxt :label :dodge-idx))))]
     (= (groups :rect) (groups :text))))
   v77_l372)))


(def
 v80_l392
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:palette
    {:setosa "#E91E63", :versicolor "#4CAF50", :virginica "#2196F3"},
    :title "Custom Palette Map"})))


(deftest
 t81_l399
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)))))
   v80_l392)))


(def
 v83_l408
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/coord :fixed)
  (pj/options {:title "Fixed Aspect Ratio"})))


(deftest
 t84_l415
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (= 3 (:lines s)))))
   v83_l408)))


(def
 v86_l424
 (->
  {:x (range 20),
   :y
   (map (fn* [p1__73640#] (Math/sin (/ p1__73640# 3.0))) (range 20)),
   :change (map (fn* [p1__73641#] (- p1__73641# 10)) (range 20))}
  (pj/lay-point :x :y {:color :change})
  (pj/options
   {:color-scale :diverging,
    :color-midpoint 0,
    :title "Diverging Color Scale"})))


(deftest
 t87_l432
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 20 (:points s)))))
   v86_l424)))


(def
 v89_l440
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options {:title "LOESS with 95% CI"})))


(deftest
 t90_l446
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)) (= 3 (:polygons s)))))
   v89_l440)))


(def
 v92_l455
 (def
  iris-sepal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Sepal", :width 300, :height 250}))))


(def
 v93_l460
 (def
  iris-petal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width {:color :species})
   (pj/options {:title "Petal", :width 300, :height 250}))))


(def
 v94_l465
 (pj/arrange
  [iris-sepal iris-petal]
  {:title "Iris Dashboard", :cols 2}))


(deftest
 t95_l468
 (is
  ((fn [v] (and (pj/pose? v) (= "Iris Dashboard" (-> v :opts :title))))
   v94_l465)))


(def
 v97_l475
 (def
  top-cities
  {:city ["Tokyo" "Delhi" "Shanghai" "São Paulo" "Mumbai"],
   :population [37.4 32.9 29.2 22.4 21.7],
   :area [2194 1484 6341 1521 603]}))


(def
 v98_l480
 (->
  top-cities
  (pj/lay-point :area :population)
  (pj/lay-text {:text :city, :nudge-y 1.0})
  (pj/options {:title "Population vs Area"})))


(deftest
 t99_l485
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:points s))
      (every? (set (:texts s)) ["Tokyo" "Delhi"]))))
   v98_l480)))


(def
 v101_l519
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5, :alpha 0.3})))


(deftest
 t102_l524
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v101_l519)))


(def
 v104_l534
 (def
  life-tracks
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows
    (fn*
     [p1__73642#]
     (#{"Cambodia" "Botswana" "Japan" "Rwanda" "China"}
      (:country p1__73642#))))
   (tc/select-columns [:country :year :life-exp]))))


(def
 v105_l540
 (->
  life-tracks
  (pj/lay-line :year :life-exp {:color :country})
  (pj/options
   {:title "Life expectancy at birth", :width 620, :height 380})))


(deftest
 t106_l545
 (is ((fn [v] (= 5 (:lines (pj/svg-summary v)))) v105_l540)))


(def
 v108_l555
 (->
  life-tracks
  (pj/lay-line :year :life-exp {:color :country})
  (pj/lay-text
   {:data
    (tc/select-rows
     life-tracks
     (fn* [p1__73643#] (= 2007 (:year p1__73643#)))),
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
 t109_l564
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
   v108_l555)))


(def
 v111_l581
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn* [p1__73644#] (= "Rwanda" (:country p1__73644#))))
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
 t112_l597
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
   v111_l581)))


(def
 v114_l625
 (def
  life-history
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-columns [:country :year :life-exp]))))


(def
 v115_l629
 (def
  ends-highest
  (->
   life-history
   (tc/select-rows (fn* [p1__73645#] (= 2007 (:year p1__73645#))))
   (tc/order-by :life-exp :desc)
   (tc/rows :as-maps)
   first
   :country)))


(def
 v116_l636
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
 v118_l648
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


(def v119_l659 [ends-highest gained-most sharpest-fall])


(deftest
 t120_l661
 (is
  ((fn
    [[a b c]]
    (and
     (= "Japan" a)
     (= "Oman" b)
     (= "Rwanda" (:$group-name c))
     (< -21 (:fall c) -20)))
   v119_l659)))


(def
 v122_l671
 (let
  [named
   #{ends-highest gained-most (:$group-name sharpest-fall)}
   chosen
   (tc/select-rows
    life-history
    (fn* [p1__73646#] (named (:country p1__73646#))))]
  (->
   life-history
   (pj/lay-line :year :life-exp {:group :country, :color "#d0d0d0"})
   (pj/lay-line
    {:data chosen, :x :year, :y :life-exp, :color :country})
   (pj/lay-text
    {:data
     (tc/select-rows
      chosen
      (fn* [p1__73647#] (= 2007 (:year p1__73647#)))),
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
 t123_l693
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (every? (set (:texts s)) ["Japan" "Oman" "Rwanda"])
      (some
       (fn*
        [p1__73648#]
        (re-find #"^Rwanda, 1992: a fall of 20 years" p1__73648#))
       (:texts s))
      (some
       (fn* [p1__73649#] (= "142 countries, 1952-2007" p1__73649#))
       (:texts s)))))
   v122_l671)))


(def
 v125_l707
 (let
  [r
   (rng/rng :jdk 77)
   xs
   (range 0 10 0.5)
   ys
   (map
    (fn*
     [p1__73650#]
     (+ (* 3 p1__73650#) 5 (* 2 (- (rng/drandom r) 0.5))))
    xs)]
  (->
   {:x xs, :y ys}
   (pj/lay-point :x :y)
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Simulated: y = 3x + 5 + noise"}))))


(deftest
 t126_l718
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 20 (:points s))
      (= 1 (:lines s))
      (some #{"Simulated: y = 3x + 5 + noise"} (:texts s)))))
   v125_l707)))


(def
 v128_l729
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
  (pj/options {:title "Palmer Penguins: Bill Dimensions"})))


(deftest
 t129_l733
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 342 (:points s)))))
   v128_l729)))


(def
 v131_l739
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/pose :bill-length-mm :bill-depth-mm {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Bill Length vs Depth with Regression"})))


(deftest
 t132_l745
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 3 (:lines s)))))
   v131_l739)))


(def
 v134_l752
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
  (pj/lay-smooth {:stat :linear-model, :color nil})
  (pj/options
   {:title "Simpson's Paradox: Overall vs Per-Group Trend"})))


(deftest
 t135_l757
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 1 (:lines s)))))
   v134_l752)))


(def
 v137_l763
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})
  (pj/options {:title "Species by Island"})))


(deftest
 t138_l767
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v137_l763)))


(def
 v140_l773
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/pose :flipper-length-mm :body-mass-g {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Flipper Length vs Body Mass"})))


(deftest
 t141_l779
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 342 (:points s)) (= 3 (:lines s)))))
   v140_l773)))


(def
 v143_l785
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-histogram :body-mass-g {:color :species})
  (pj/options {:title "Body Mass Distribution"})))


(deftest
 t144_l789
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v143_l785)))


(def
 v146_l797
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
 t147_l804
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 244 (:points s)) (= 2 (:lines s)))))
   v146_l797)))


(def
 v149_l810
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :time})
  (pj/options {:title "Visits by Day and Meal Time"})))


(deftest
 t150_l814
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v149_l810)))


(def
 v152_l820
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :time})
  (pj/options {:title "Visits by Day (Stacked)"})))


(deftest
 t153_l824
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v152_l820)))


(def
 v155_l830
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/coord :flip)
  (pj/options {:title "Day by Gender (Horizontal)"})))


(deftest
 t156_l835
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v155_l830)))


(def
 v158_l843
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy {:color :class})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options {:title "Displacement vs Highway MPG by Class"})))


(deftest
 t159_l849
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 234 (:points s)) (pos? (:lines s)))))
   v158_l843)))


(def
 v161_l855
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :cty {:color :drv})
  (pj/options {:title "Engine Displacement vs City Fuel Efficiency"})))


(deftest
 t162_l859
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 234 (:points s)))))
   v161_l855)))


(def
 v164_l865
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-bar :drv)
  (pj/options {:title "Cars by Drive Type"})))


(deftest
 t165_l869
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v164_l865)))


(def
 v167_l890
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 500)
  (pj/lay-point :carat :price {:color :cut})
  (pj/options {:title "Diamonds (500 rows, SVG)"})))


(deftest
 t168_l895
 (is ((fn [v] (= 500 (:points (pj/svg-summary v)))) v167_l890)))


(def
 v170_l902
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:color :cut, :alpha 0.3})
  (pj/options
   {:title "Diamonds (53,940 rows, BufferedImage)", :format :bufimg})))


(deftest
 t171_l907
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v170_l902)))


(def
 v173_l920
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


(def v174_l928 quarterly-revenue)


(deftest
 t175_l930
 (is
  ((fn [v] (.contains (pr-str (pj/plot v)) "rotate(-45")) v174_l928)))


(def v177_l937 (pj/options quarterly-revenue {:format :bufimg}))


(deftest
 t178_l939
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v177_l937)))


(def
 v180_l947
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
    (mapv (fn* [p1__73651#] (bit-and p1__73651# 255)) (vec bs))))))


(deftest
 t181_l957
 (is ((fn [bs] (= [137 80 78 71 13 10 26 10] bs)) v180_l947)))
