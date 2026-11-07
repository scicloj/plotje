(ns
 plotje-book.gallery-generated-test
 (:require
  [scicloj.plotje.api :as pj]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [tech.v3.datatype.functional :as dfn]
  [fastmath.stats :as fstats]
  [fastmath.random :as rng]
  [clojure.test :refer [deftest is]]))


(def
 v3_l30
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy {:color :class})
  pj/lay-point
  pj/lay-smooth
  (pj/options
   {:title "Fuel Efficiency by Engine Size",
    :x-label "Engine Displacement (L)",
    :y-label "Highway MPG"})))


(deftest
 t4_l38
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v3_l30)))


(def
 v6_l50
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/random 500 {:seed 1, :repeat? false})
  (pj/pose :carat :price {:color :cut, :size :depth})
  pj/lay-point
  (pj/options
   {:title "Diamond Price vs Carat (bubble)",
    :x-label "Carat",
    :y-label "Price (USD)"})))


(deftest
 t7_l58
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 500 (:points s)))) v6_l50)))


(def
 v9_l66
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :sex})
  pj/lay-point
  (pj/options
   {:title "Total Bill by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t10_l73
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 244 (:points s)))) v9_l66)))


(def
 v12_l83
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :price)
  pj/lay-histogram
  (pj/options
   {:title "Distribution of Diamond Prices",
    :x-label "Price (USD)",
    :y-label "Count"})))


(deftest
 t13_l90
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s)))) v12_l83)))


(def
 v15_l95
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :price {:color :cut})
  pj/lay-histogram
  (pj/options
   {:title "Diamond Prices by Cut",
    :x-label "Price (USD)",
    :y-label "Count"})))


(deftest
 t16_l102
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (< 1 (:polygons s)))) v15_l95)))


(def
 v18_l110
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :carat {:color :cut})
  pj/lay-density
  (pj/options
   {:title "Carat Distribution by Cut",
    :x-label "Carat",
    :y-label "Density"})))


(deftest
 t19_l117
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v18_l110)))


(def
 v21_l122
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/random 500 {:seed 1, :repeat? false})
  (pj/pose :carat)
  pj/lay-density
  pj/lay-rug
  (pj/options
   {:title "Carat Distribution with Rug",
    :x-label "Carat",
    :y-label "Density"})))


(deftest
 t22_l131
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v21_l122)))


(def
 v24_l140
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-boxplot
  (pj/options
   {:title "Total Bill by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t25_l147
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v24_l140)))


(def
 v27_l152
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-boxplot
  pj/lay-point
  (pj/options
   {:title "Total Bill by Day (box + points)",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t28_l160
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v27_l152)))


(def
 v30_l169
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-violin
  (pj/options
   {:title "Total Bill by Day (violin)",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t31_l176
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v30_l169)))


(def
 v33_l181
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-violin
  pj/lay-boxplot
  (pj/options
   {:title "Total Bill Distribution by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t34_l189
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v33_l181)))


(def
 v36_l197
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut :price)
  pj/lay-ridgeline
  (pj/options
   {:title "Price Distribution by Cut (ridgeline)",
    :x-label "Cut",
    :y-label "Price (USD)"})))


(deftest
 t37_l204
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v36_l197)))


(def
 v39_l214
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/options
   {:title "Diamond Count by Cut", :x-label "Cut", :y-label "Count"})))


(deftest
 t40_l221
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v39_l214)))


(def
 v42_l231
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/coord :flip)
  (pj/options
   {:title "Diamond Count by Cut (horizontal)",
    :x-label "Cut",
    :y-label "Count"})))


(deftest
 t43_l239
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v42_l231)))


(def
 v45_l247
 (def
  mpg-mfr-counts
  (->
   (rdatasets/ggplot2-mpg)
   (tc/group-by [:manufacturer])
   (tc/aggregate {:count tc/row-count})
   (tc/order-by [:count] :desc)
   (tc/select-rows (range 8)))))


(def
 v46_l254
 (->
  mpg-mfr-counts
  (pj/pose :manufacturer :count)
  pj/lay-lollipop
  (pj/options
   {:title "Top Manufacturers by Model Count",
    :x-label "Manufacturer",
    :y-label "Count"})))


(deftest
 t47_l261
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v46_l254)))


(def
 v49_l267
 (->
  mpg-mfr-counts
  (pj/pose :manufacturer :count)
  pj/lay-lollipop
  (pj/coord :flip)
  (pj/options
   {:title "Top Manufacturers (horizontal lollipop)",
    :x-label "Manufacturer",
    :y-label "Count"})))


(deftest
 t50_l275
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v49_l267)))


(def
 v52_l286
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-line
  (pj/options
   {:title "US Unemployment Over Time",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t53_l293
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v52_l286)))


(def
 v55_l298
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn*
    [p1__88912#]
    (#{"Australia" "Brazil" "Japan" "Nigeria" "Germany"}
     (:country p1__88912#))))
  (pj/pose :year :life-exp {:color :country})
  pj/lay-line
  pj/lay-point
  (pj/options
   {:title "Life Expectancy Over Time",
    :x-label "Year",
    :y-label "Life Expectancy"})))


(deftest
 t56_l308
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v55_l298)))


(def
 v58_l317
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-area
  (pj/options
   {:title "US Unemployment Over Time (area)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t59_l324
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v58_l317)))


(def
 v61_l334
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/random 2000 {:seed 1, :repeat? false})
  (pj/pose :carat :price)
  pj/lay-density-2d
  (pj/options
   {:title "Diamond Carat vs Price (density)",
    :x-label "Carat",
    :y-label "Price (USD)"})))


(deftest
 t62_l342
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v61_l334)))


(def
 v64_l350
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options
   {:title "Tip vs Total Bill (with regression)",
    :x-label "Total Bill (USD)",
    :y-label "Tip (USD)"})))


(deftest
 t65_l358
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v64_l350)))


(def
 v67_l368
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  pj/lay-contour
  (pj/options
   {:title "Iris Sepal Dimensions (contour)",
    :x-label "Sepal Length",
    :y-label "Sepal Width"})))


(deftest
 t68_l376
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v67_l368)))


(def
 v70_l387
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy {:color :class})
  pj/lay-point
  (pj/facet :drv)
  (pj/options
   {:title "Highway MPG by Engine Size, faceted by Drive",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t71_l395
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v70_l387)))


(def
 v73_l400
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :hwy)
  pj/lay-histogram
  (pj/facet :drv)
  (pj/options
   {:title "Highway MPG by Drive Type",
    :x-label "Highway MPG",
    :y-label "Count"})))


(deftest
 t74_l408
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v73_l400)))


(def
 v76_l420
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix [:sepal-length :sepal-width :petal-length :petal-width])
  (pj/options {:title "Iris SPLOM"})))


(deftest
 t77_l425
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 16 (:panels s))
      (= (* 12 150) (:points s))
      (pos? (:polygons s)))))
   v76_l420)))


(def
 v79_l437
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day {:color :sex})
  (pj/lay-bar {:position :stack})
  (pj/options
   {:title "Tips by Day and Sex (stacked bar)",
    :x-label "Day",
    :y-label "Count"})))


(deftest
 t80_l444
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v79_l437)))


(def
 v82_l450
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day {:color :sex})
  (pj/lay-bar {:position :fill})
  (pj/options
   {:title "Proportion by Day and Sex",
    :x-label "Day",
    :y-label "Proportion"})))


(deftest
 t83_l457
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v82_l450)))


(def
 v85_l466
 (->
  (rdatasets/gapminder-gapminder)
  (tc/group-by [:year :continent])
  (tc/aggregate {:pop (fn [ds] (dfn/sum (ds :pop)))})
  (tc/order-by [:year :continent])
  (pj/pose :year :pop {:color :continent})
  (pj/lay-area {:position :stack})
  (pj/options
   {:title "World Population by Continent",
    :x-label "Year",
    :y-label "Population"})))


(deftest
 t86_l476
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v85_l466)))


(def
 v88_l486
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Diamond Cut (rose chart)"})))


(deftest
 t89_l492
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v88_l486)))


(def
 v91_l498
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Tips Count by Day (Rose)"})))


(deftest
 t92_l504
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v91_l498)))


(def
 v94_l509
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Chick Count by Feed (Rose)"})))


(deftest
 t95_l515
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v94_l509)))


(def
 v97_l520
 (->
  {:day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"],
   :hours [8 7 6 9 5 3 4]}
  (pj/lay-bar :day :hours)
  (pj/coord :polar)
  (pj/options {:title "Weekly Working Hours (Polar)"})))


(deftest
 t98_l526
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v97_l520)))


(def
 v100_l534
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose :wt :mpg)
  pj/lay-point
  (pj/lay-text {:text :rownames})
  (pj/options
   {:title "Motor Trend Cars",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t101_l542
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (count (:texts s))))))
   v100_l534)))


(def
 v103_l549
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose :wt :mpg)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})
  (pj/options
   {:title "Weight vs MPG with Linear Fit",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t104_l557
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v103_l549)))


(def
 v106_l566
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options {:title "Tips by Day and Gender"})))


(deftest
 t107_l570
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v106_l566)))


(def
 v109_l580
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)
  (pj/options
   {:title "Diamond Price by Carat (Log Scale)",
    :x-label "Carat",
    :y-label "Price (US dollars)",
    :format :bufimg})))


(deftest
 t110_l588
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v109_l580)))


(def
 v112_l593
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-summary :day :total-bill {:color :sex})
  (pj/options {:title "Average Bill with Standard Error"})))


(deftest
 t113_l597
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:points s)))) v112_l593)))


(def
 v115_l603
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows (fn* [p1__88913#] (= 2007 (:year p1__88913#))))
  (pj/lay-point :gdp-percap :life-exp {:color :continent, :size :pop})
  (pj/scale :x :log)
  (pj/options
   {:title "Gapminder 2007: Life Expectancy vs GDP",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t116_l611
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v115_l603)))


(def
 v118_l616
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn*
    [p1__88914#]
    (#{"Brazil" "United States" "Japan" "China" "India"}
     (:country p1__88914#))))
  (pj/lay-line :year :life-exp {:color :country})
  (pj/options
   {:title "Life Expectancy Over Time",
    :x-label "Year",
    :y-label "Life Expectancy"})))


(deftest
 t119_l623
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v118_l616)))


(def
 v121_l628
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-step :date :unemploy)
  (pj/options
   {:title "US Unemployment (Step)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t122_l634
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v121_l628)))


(def
 v124_l639
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  pj/lay-density
  pj/lay-rug
  (pj/options {:title "Iris Sepal Length: Density + Rug"})))


(deftest
 t125_l645
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v124_l639)))


(def
 v127_l650
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :smoker})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options
   {:title "Tips: Bill vs Tip by Smoking Status",
    :x-label "Total Bill ($)",
    :y-label "Tip ($)"})))


(deftest
 t128_l658
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v127_l650)))


(def
 v130_l665
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-histogram :hwy {:color :drv})
  (pj/facet :drv)
  (pj/options {:title "Highway MPG by Drive Type"})))


(deftest
 t131_l670
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v130_l665)))


(def
 v133_l676
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-rule-v {:x-intercept 6.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  (pj/options {:title "Iris with Reference Lines and Band"})))


(deftest
 t134_l683
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v133_l676)))


(def
 v136_l690
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-violin
  pj/lay-boxplot
  (pj/options {:title "Tips Distribution by Day"})))


(deftest
 t137_l696
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v136_l690)))


(def
 v139_l705
 (->
  (rdatasets/datasets-mtcars)
  (tc/order-by :mpg)
  (pj/lay-lollipop :rownames :mpg)
  (pj/coord :flip)
  (pj/options {:title "Cars Ranked by MPG"})))


(deftest
 t140_l711
 (is
  ((fn
    [v]
    (let
     [mtcars
      (rdatasets/datasets-mtcars)
      by-name
      (zipmap (:rownames mtcars) (:mpg mtcars))
      cars
      (-> v pj/plan :panels first :y-domain)]
     (and
      (= 32 (:points (pj/svg-summary v)))
      (= 32 (count cars))
      (apply <= (map by-name cars)))))
   v139_l705)))


(def
 v142_l723
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :fill, :color :sex})
  (pj/options {:title "Gender Proportion by Day (100% stacked)"})))


(deftest
 t143_l727
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v142_l723)))


(def
 v145_l736
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-histogram {:normalize :density})
  (pj/lay-density {:color "steelblue", :alpha 0.4})
  (pj/options {:title "Sepal Length: Histogram + Density Curve"})))


(deftest
 t146_l742
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v145_l736)))


(def
 v148_l749
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :fixed)
  (pj/options {:title "Iris Sepals (Equal Aspect Ratio)"})))


(deftest
 t149_l754
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v148_l749)))


(def
 v151_l759
 (->
  {:category ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"],
   :value [120 200 150 80 70 110 130]}
  (pj/lay-bar :category :value)
  (pj/options {:title "Weekly Sales"})))


(deftest
 t152_l764
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v151_l759)))


(def
 v154_l769
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})
  (pj/options
   {:title "Sepal Length by Species", :x-label "Sepal Length (cm)"})))


(deftest
 t155_l774
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v154_l769)))


(def
 v157_l779
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options {:title "Iris: Scatter + LOESS by Species"})))


(deftest
 t158_l785
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v157_l779)))


(def
 v160_l792
 (->
  (rdatasets/datasets-iris)
  (pj/lay-summary :species :sepal-length)
  (pj/options {:title "Mean Sepal Length +/- SE by Species"})))


(deftest
 t161_l796
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v160_l792)))


(def
 v163_l801
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/lay-rug
  (pj/options {:title "Iris: Scatter with Rug Marks"})))


(deftest
 t164_l807
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v163_l801)))


(def
 v166_l818
 (->
  (rdatasets/ggplot2-economics)
  (as-> econ (tc/select-rows econ (range 0 (tc/row-count econ) 12)))
  (pj/pose :unemploy :pce)
  pj/lay-line
  pj/lay-point
  (pj/options
   {:title "US Economy: Unemployment vs Personal Consumption",
    :x-label "Unemployed (thousands)",
    :y-label "Personal Consumption Expenditures"})))


(deftest
 t167_l827
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v166_l818)))


(def
 v169_l837
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-step
  pj/lay-area
  (pj/options
   {:title "US Unemployment (Step Area)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t170_l845
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:polygons s)))))
   v169_l837)))


(def
 v172_l854
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :psavert)
  pj/lay-area
  pj/lay-line
  (pj/options
   {:title "US Personal Savings Rate",
    :x-label "Date",
    :y-label "Savings Rate (%)"})))


(deftest
 t173_l862
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v172_l854)))


(def
 v175_l869
 (->
  (rdatasets/ggplot2-txhousing)
  (tc/select-rows
   (fn*
    [p1__88915#]
    (#{"Houston" "Dallas" "San Antonio" "Austin"} (:city p1__88915#))))
  (pj/pose :date :median {:color :city})
  pj/lay-line
  (pj/options
   {:title "Texas Median Home Prices",
    :x-label "Date",
    :y-label "Median Price ($)"})))


(deftest
 t176_l877
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v175_l869)))


(def
 v178_l884
 (->
  (rdatasets/lme4-sleepstudy)
  (pj/pose :days :reaction {:color :subject, :color-type :categorical})
  pj/lay-line
  pj/lay-point
  (pj/options
   {:title "Sleep Deprivation: Reaction Time by Subject",
    :x-label "Days of Sleep Deprivation",
    :y-label "Reaction Time (ms)"})))


(deftest
 t179_l892
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (= 180 (:points s)))))
   v178_l884)))


(def
 v181_l899
 (->
  (rdatasets/lme4-sleepstudy)
  (tc/select-rows
   (fn* [p1__88916#] (= "308" (str (:subject p1__88916#)))))
  (pj/pose :days :reaction)
  pj/lay-step
  pj/lay-point
  (pj/options
   {:title "Subject 308: Reaction Time (Step)",
    :x-label "Days",
    :y-label "Reaction Time (ms)"})))


(deftest
 t182_l908
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v181_l899)))


(def
 v184_l918
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-point
  (pj/options
   {:title "Old Faithful Geyser",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t185_l925
 (is ((fn [v] (= 272 (:points (pj/svg-summary v)))) v184_l918)))


(def
 v187_l930
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-point
  pj/lay-smooth
  (pj/options
   {:title "Old Faithful with LOESS",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t188_l938
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:lines s)))))
   v187_l930)))


(def
 v190_l949
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.05})
  (pj/options
   {:title "Diamond Price vs Carat (alpha = 0.05)",
    :x-label "Carat",
    :y-label "Price (US dollars)",
    :format :bufimg})))


(deftest
 t191_l956
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v190_l949)))


(def
 v193_l963
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :wt :mpg {:color :hp})
  (pj/options
   {:title "Cars: Color by Horsepower",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t194_l969
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v193_l963)))


(def
 v196_l974
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :hp :mpg {:color :cyl, :size :disp})
  (pj/options
   {:title "Cars: Color by Cylinders, Size by Displacement",
    :x-label "Horsepower",
    :y-label "Miles per Gallon"})))


(deftest
 t197_l980
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v196_l974)))


(def
 v199_l985
 (->
  (tc/select-rows
   (rdatasets/gapminder-gapminder)
   (fn* [p1__88917#] (= 2007 (:year p1__88917#))))
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:color :continent, :size :pop, :alpha 0.6})
  (pj/scale :x :log)
  (pj/options
   {:title "Gapminder 2007",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t200_l992
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v199_l985)))


(def
 v202_l997
 (->
  (rdatasets/ggplot2-midwest)
  (pj/lay-point
   :percollege
   :percbelowpoverty
   {:color :state, :size :poptotal, :alpha 0.5})
  (pj/options
   {:title "Midwest: College Education vs Poverty",
    :x-label "Percent College Educated",
    :y-label "Percent Below Poverty"})))


(deftest
 t203_l1003
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v202_l997)))


(def
 v205_l1008
 (def
  msleep
  (tc/drop-missing
   (rdatasets/ggplot2-msleep)
   [:sleep-total :bodywt :brainwt :vore])))


(def
 v206_l1011
 (->
  msleep
  (pj/lay-point :bodywt :brainwt {:color :vore})
  (pj/scale :x :log)
  (pj/scale :y :log)
  (pj/options
   {:title "Mammal Body vs Brain Weight (log-log)",
    :x-label "Body Weight (kg, log)",
    :y-label "Brain Weight (kg, log)"})))


(deftest
 t207_l1019
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v206_l1011)))


(def
 v209_l1024
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :petal-length {:color :species})
  pj/lay-point
  (pj/coord :fixed)
  (pj/options
   {:title "Iris: Sepal vs Petal Length (1:1 Aspect)",
    :x-label "Sepal Length",
    :y-label "Petal Length"})))


(deftest
 t210_l1032
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v209_l1024)))


(def
 v212_l1037
 (->
  (rdatasets/datasets-mtcars)
  (tc/order-by [:mpg] :desc)
  (tc/select-rows (range 5))
  (pj/pose :wt :mpg)
  pj/lay-point
  (pj/lay-label {:text :rownames})
  (pj/options
   {:title "Top 5 Most Fuel Efficient Cars",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t213_l1047
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (pos? (count (:texts s))))))
   v212_l1037)))


(def
 v215_l1054
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/options
   {:title "Iris Petals with Linear Fit per Species",
    :x-label "Petal Length",
    :y-label "Petal Width"})))


(deftest
 t216_l1062
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v215_l1054)))


(def
 v218_l1069
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose :wt :mpg)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model, :confidence-band true})
  (pj/options
   {:title "Weight vs MPG with 95% Confidence Band",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t219_l1077
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 32 (:points s)) (pos? (:lines s)) (pos? (:polygons s)))))
   v218_l1069)))


(def
 v221_l1087
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose :wt :mpg)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  pj/lay-smooth
  (pj/options
   {:title "Cars: Linear Model and LOESS Smoothers",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t222_l1096
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 32 (:points s)) (>= (:lines s) 2))))
   v221_l1087)))


(def
 v224_l1106
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions)
  (pj/lay-histogram {:normalize :density, :binwidth 0.25})
  (pj/lay-density {:color "steelblue", :alpha 0.4})
  (pj/options
   {:title "Old Faithful: Histogram + Density",
    :x-label "Eruption Duration (min)",
    :y-label "Density"})))


(deftest
 t225_l1114
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v224_l1106)))


(def
 v227_l1119
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions)
  pj/lay-density
  pj/lay-rug
  (pj/options
   {:title "Old Faithful: Density with Rug",
    :x-label "Eruption Duration (min)",
    :y-label "Density"})))


(deftest
 t228_l1127
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v227_l1119)))


(def
 v230_l1134
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :depth)
  pj/lay-density
  (pj/options
   {:title "Distribution of Diamond Depth",
    :x-label "Depth (%)",
    :y-label "Density"})))


(deftest
 t231_l1141
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v230_l1134)))


(def
 v233_l1146
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :depth)
  (pj/lay-histogram {:normalize :density})
  (pj/lay-density {:color "steelblue", :alpha 0.4})
  (pj/options
   {:title "Diamond Depth: Histogram + Density",
    :x-label "Depth (%)",
    :y-label "Density"})))


(deftest
 t234_l1154
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v233_l1146)))


(def
 v236_l1159
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :petal-width {:color :species})
  (pj/options
   {:title "Iris Petal Width by Species",
    :x-label "Petal Width (cm)",
    :y-label "Density"})))


(deftest
 t237_l1165
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v236_l1159)))


(def
 v239_l1170
 (->
  msleep
  (pj/lay-density :sleep-total {:color :vore})
  (pj/options
   {:title "Sleep Duration by Diet Type",
    :x-label "Total Sleep (hours)",
    :y-label "Density"})))


(deftest
 t240_l1176
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v239_l1170)))


(def
 v242_l1181
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :waiting)
  (pj/lay-histogram {:bins 15})
  (pj/options
   {:title "Waiting Time Between Eruptions (15 bins)",
    :x-label "Waiting Time (min)",
    :y-label "Count"})))


(deftest
 t243_l1188
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v242_l1181)))


(def
 v245_l1193
 (->
  (let
   [r (rng/rng :jdk 7)]
   {:value
    (repeatedly
     500
     (fn*
      []
      (+
       (* 2.0 (rng/drandom r))
       (* 2.0 (rng/drandom r))
       (* 2.0 (rng/drandom r))
       -3.0)))})
  (pj/pose :value)
  (pj/lay-histogram {:bins 30, :normalize :density})
  (pj/lay-density {:color "steelblue", :alpha 0.4})
  (pj/options
   {:title "Simulated Distribution: Histogram + Density",
    :x-label "Value",
    :y-label "Density"})))


(deftest
 t246_l1205
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v245_l1193)))


(def
 v248_l1210
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed :weight {:color :feed})
  pj/lay-boxplot
  (pj/options
   {:title "Chick Weight by Feed Type",
    :x-label "Feed",
    :y-label "Weight (g)"})))


(deftest
 t249_l1217
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v248_l1210)))


(def
 v251_l1222
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :sepal-length {:color :species})
  pj/lay-boxplot
  (pj/coord :flip)
  (pj/options
   {:title "Iris Sepal Length (Horizontal Box)",
    :x-label "Species",
    :y-label "Sepal Length (cm)"})))


(deftest
 t252_l1230
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v251_l1222)))


(def
 v254_l1237
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :sex})
  pj/lay-boxplot
  (pj/options
   {:title "Tips by Day and Gender (Grouped Boxplot)",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t255_l1244
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v254_l1237)))


(def
 v257_l1249
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :sepal-width {:color :species})
  pj/lay-violin
  (pj/options
   {:title "Iris Sepal Width (Violin)",
    :x-label "Species",
    :y-label "Sepal Width (cm)"})))


(deftest
 t258_l1256
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v257_l1249)))


(def
 v260_l1261
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :petal-width {:color :species})
  pj/lay-violin
  (pj/coord :flip)
  (pj/options
   {:title "Iris Petal Width (Horizontal Violin)",
    :x-label "Species",
    :y-label "Petal Width (cm)"})))


(deftest
 t261_l1269
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v260_l1261)))


(def
 v263_l1277
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-violin
  pj/lay-point
  (pj/options
   {:title "Tips: Violin with Individual Points",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t264_l1285
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 244 (:points s)))))
   v263_l1277)))


(def
 v266_l1294
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-violin
  pj/lay-boxplot
  pj/lay-point
  (pj/options
   {:title "Tips: Violin + Boxplot + Points",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t267_l1303
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v266_l1294)))


(def
 v269_l1310
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :smoker :total-bill {:color :smoker})
  pj/lay-violin
  (pj/options
   {:title "Total Bill by Smoking Status",
    :x-label "Smoker",
    :y-label "Total Bill ($)"})))


(deftest
 t270_l1317
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v269_l1310)))


(def
 v272_l1322
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :petal-length)
  pj/lay-ridgeline
  (pj/options
   {:title "Iris Petal Length by Species (Ridgeline)",
    :x-label "Species",
    :y-label "Petal Length (cm)"})))


(deftest
 t273_l1329
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v272_l1322)))


(def
 v275_l1334
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :color :price)
  pj/lay-ridgeline
  (pj/options
   {:title "Diamond Price by Color Grade (Ridgeline)",
    :x-label "Color",
    :y-label "Price ($)"})))


(deftest
 t276_l1341
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v275_l1334)))


(def
 v278_l1346
 (def
  airquality
  (->
   (rdatasets/datasets-airquality)
   (tc/drop-missing :ozone)
   (tc/add-column
    :month-name
    (fn
     [ds]
     (map
      (fn*
       [p1__88918#]
       (get {5 "May", 6 "Jun", 7 "Jul", 8 "Aug", 9 "Sep"} p1__88918#))
      (ds :month)))))))


(def
 v279_l1353
 (->
  airquality
  (pj/pose :month-name :ozone {:color :month-name})
  pj/lay-boxplot
  (pj/options
   {:title "New York Ozone by Month",
    :x-label "Month",
    :y-label "Ozone (ppb)"})))


(deftest
 t280_l1360
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v279_l1353)))


(def
 v282_l1368
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :class)
  pj/lay-bar
  (pj/options
   {:title "Vehicle Count by Class",
    :x-label "Class",
    :y-label "Count"})))


(deftest
 t283_l1375
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v282_l1368)))


(def
 v285_l1382
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options
   {:title "Tips Count by Day and Gender",
    :x-label "Day",
    :y-label "Count"})))


(deftest
 t286_l1388
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v285_l1382)))


(def
 v288_l1393
 (->
  {:country ["US" "China" "Japan" "Germany" "UK" "India" "France"],
   :gdp [21.4 14.7 5.1 3.8 2.8 2.7 2.6]}
  (pj/lay-bar :gdp :country)
  (pj/options
   {:title "GDP by Country (2019)",
    :x-label "GDP (Trillion $)",
    :y-label "Country"})))


(deftest
 t289_l1400
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v288_l1393)))


(def
 v291_l1407
 (->
  {:metric
   ["Quality"
    "Speed"
    "Usability"
    "Reliability"
    "Support"
    "Price"
    "Design"
    "Docs"],
   :score [-30 -20 -10 5 15 25 35 45]}
  (pj/lay-bar :score :metric)
  (pj/lay-rule-v {:x-intercept 0})
  (pj/options
   {:title "Customer Satisfaction Scores",
    :x-label "Net Score",
    :y-label "Metric"})))


(deftest
 t292_l1415
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:polygons s)) (pos? (:lines s)))))
   v291_l1407)))


(def
 v294_l1422
 (->
  (rdatasets/datasets-chickwts)
  (tc/group-by [:feed])
  (tc/aggregate {:mean-weight (fn [ds] (dfn/mean (ds :weight)))})
  (pj/lay-lollipop :feed :mean-weight)
  (pj/coord :flip)
  (pj/options
   {:title "Mean Chick Weight by Feed Type",
    :x-label "Feed",
    :y-label "Mean Weight (g)"})))


(deftest
 t295_l1431
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v294_l1422)))


(def
 v297_l1438
 (->
  (rdatasets/datasets-iris)
  (tc/group-by [:species])
  (tc/aggregate {:mean-sl (fn [ds] (fstats/mean (ds :sepal-length)))})
  (pj/lay-lollipop :species :mean-sl)
  (pj/coord :flip)
  (pj/options
   {:title "Mean Sepal Length by Species",
    :x-label "Species",
    :y-label "Mean Sepal Length (cm)"})))


(deftest
 t298_l1447
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v297_l1438)))


(def
 v300_l1457
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-density-2d
  (pj/options
   {:title "Old Faithful: 2D Density",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t301_l1464
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v300_l1457)))


(def
 v303_l1469
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-point
  pj/lay-density-2d
  (pj/options
   {:title "Old Faithful: Scatter + Density",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t304_l1477
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:visible-tiles s)))))
   v303_l1469)))


(def
 v306_l1484
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-point
  pj/lay-contour
  (pj/options
   {:title "Old Faithful: Scatter + Contour",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t307_l1492
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:lines s)))))
   v306_l1484)))


(def
 v309_l1499
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :petal-length)
  pj/lay-contour
  (pj/options
   {:title "Iris: Sepal vs Petal Length Contour",
    :x-label "Sepal Length",
    :y-label "Petal Length"})))


(deftest
 t310_l1506
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v309_l1499)))


(def
 v312_l1513
 (->
  (rdatasets/ggplot2-faithfuld)
  (pj/pose :eruptions :waiting {:fill :density})
  pj/lay-tile
  (pj/options
   {:title "Old Faithful: Pre-computed Density Heatmap",
    :x-label "Eruption Duration",
    :y-label "Waiting Time"})))


(deftest
 t313_l1520
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v312_l1513)))


(def
 v315_l1525
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/random 3000 {:seed 1, :repeat? false})
  (pj/pose :carat :price)
  pj/lay-point
  pj/lay-density-2d
  (pj/options
   {:title "Diamonds: Scatter + 2D Density",
    :x-label "Carat",
    :y-label "Price ($)"})))


(deftest
 t316_l1534
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:visible-tiles s)))))
   v315_l1525)))


(def
 v318_l1541
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-density-2d
  (pj/options
   {:title "MPG: Displacement vs Highway (Density)",
    :x-label "Displacement (L)",
    :y-label "Highway MPG"})))


(deftest
 t319_l1548
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v318_l1541)))


(def
 v321_l1553
 (->
  {:row (mapcat (fn* [p1__88919#] (repeat 6 p1__88919#)) (range 6)),
   :col (flatten (repeat 6 (range 6))),
   :value
   (map (fn* [p1__88920#] (Math/sin (* p1__88920# 0.5))) (range 36))}
  (pj/pose :col :row {:fill :value})
  pj/lay-tile
  (pj/options
   {:title "Synthetic Heatmap (sin wave)",
    :x-label "Column",
    :y-label "Row"})))


(deftest
 t322_l1562
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v321_l1553)))


(def
 v324_l1570
 (->
  (rdatasets/datasets-iris)
  (tc/group-by [:species])
  (tc/aggregate
   {:mean (fn [ds] (fstats/mean (ds :sepal-length))),
    :y-min
    (fn
     [ds]
     (-
      (fstats/mean (ds :sepal-length))
      (fstats/stddev (ds :sepal-length)))),
    :y-max
    (fn
     [ds]
     (+
      (fstats/mean (ds :sepal-length))
      (fstats/stddev (ds :sepal-length))))})
  (pj/lay-errorbar :species :mean {:y-min :y-min, :y-max :y-max})
  (pj/lay-point :species :mean)
  (pj/options
   {:title "Mean Sepal Length +/- SD by Species",
    :x-label "Species",
    :y-label "Sepal Length (cm)"})))


(deftest
 t325_l1583
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v324_l1570)))


(def
 v327_l1590
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-summary :day :tip {:color :sex})
  (pj/options
   {:title "Mean Tip +/- SE by Day and Gender",
    :x-label "Day",
    :y-label "Tip ($)"})))


(deftest
 t328_l1596
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v327_l1590)))


(def
 v330_l1608
 (->
  (rdatasets/ggplot2-economics)
  (pj/arrange [{:x :date, :y :unemploy} {:x :date, :y :uempmed}])
  pj/lay-line
  (pj/options {:title "Unemployment: Total vs Median Duration"})))


(deftest
 t331_l1614
 (is ((fn [v] (>= (:lines (pj/svg-summary v)) 2)) v330_l1608)))


(def
 v333_l1619
 (->
  (rdatasets/ggplot2-economics)
  (pj/arrange
   [{:x :date, :y :unemploy}
    {:x :date, :y :uempmed}
    {:x :date, :y :psavert}])
  pj/lay-line
  (pj/options {:title "US Economic Indicators"})))


(deftest
 t334_l1626
 (is ((fn [v] (>= (:lines (pj/svg-summary v)) 3)) v333_l1619)))


(def
 v336_l1636
 (pj/arrange
  [(->
    (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/options {:title "Highway"}))
   (->
    (rdatasets/ggplot2-mpg)
    (pj/lay-line :displ :cty)
    (pj/options {:title "City"}))]))


(deftest
 t337_l1644
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v336_l1636)))


(def
 v339_l1654
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-rule-h {:y-intercept 4.0})
  (pj/lay-rule-v {:x-intercept 5.0})
  (pj/lay-rule-v {:x-intercept 7.0})
  (pj/options
   {:title "Iris: Scatter with Grid Lines",
    :x-label "Sepal Length",
    :y-label "Sepal Width"})))


(deftest
 t340_l1665
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:lines s) 4))))
   v339_l1654)))


(def
 v342_l1672
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose :wt :mpg)
  pj/lay-point
  (pj/lay-band-h {:y-min 20, :y-max 30})
  (pj/lay-band-v {:x-min 2.5, :x-max 3.5})
  (pj/options
   {:title "Cars: Scatter with Highlight Bands",
    :x-label "Weight (1000 lbs)",
    :y-label "MPG"})))


(deftest
 t343_l1681
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v342_l1672)))


(def
 v345_l1686
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-area
  (pj/lay-rule-h {:y-intercept 8000})
  (pj/options
   {:title "US Unemployment with 8000 Threshold",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t346_l1694
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v345_l1686)))


(def
 v348_l1701
 (->
  airquality
  (pj/lay-line :rownames :ozone)
  (pj/lay-rule-h {:y-intercept 60})
  (pj/options
   {:title "NYC Ozone with Threshold at 60 ppb",
    :x-label "Observation",
    :y-label "Ozone (ppb)"})))


(deftest
 t349_l1708
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (and (pos? (:lines s)))))
   v348_l1701)))


(def
 v351_l1714
 (->
  airquality
  (pj/pose :wind :ozone)
  pj/lay-point
  (pj/lay-band-h {:y-min 0, :y-max 40})
  (pj/options
   {:title "Ozone vs Wind: Safe Zone Highlighted",
    :x-label "Wind Speed (mph)",
    :y-label "Ozone (ppb)"})))


(deftest
 t352_l1722
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v351_l1714)))


(def
 v354_l1730
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet :class)
  (pj/options
   {:title "MPG: Faceted by Vehicle Class",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t355_l1738
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v354_l1730)))


(def
 v357_l1743
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet {:col :drv, :row :year})
  (pj/options
   {:title "MPG: Drive Type x Model Year",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t358_l1751
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 6 (:panels (pj/svg-summary v)))
      (= ["f" "4" "r"] (distinct (map :col-label panels)))
      (= ["1999" "2008"] (distinct (map :row-label panels))))))
   v357_l1743)))


(def
 v360_l1764
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet {:col :drv, :row :class})
  (pj/options
   {:title "MPG: Drive x Class",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t361_l1772
 (is ((fn [v] (pos? (:panels (pj/svg-summary v)))) v360_l1764)))


(def
 v363_l1777
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet {:row :drv})
  (pj/options
   {:title "MPG: Row Facets by Drive Type",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t364_l1785
 (is
  ((fn
    [v]
    (and
     (= 3 (:panels (pj/svg-summary v)))
     (= #{0} (set (map :col (:panels (pj/plan v)))))))
   v363_l1777)))


(def
 v366_l1792
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length)
  pj/lay-density
  (pj/facet :species)
  (pj/options
   {:title "Petal Length Density by Species",
    :x-label "Petal Length (cm)",
    :y-label "Density"})))


(deftest
 t367_l1800
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v366_l1792)))


(def
 v369_l1805
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-boxplot
  (pj/facet :sex)
  (pj/options
   {:title "Total Bill by Day, Faceted by Gender",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t370_l1813
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 2 (:panels s)))))
   v369_l1805)))


(def
 v372_l1820
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-violin
  (pj/facet :sex)
  (pj/options
   {:title "Total Bill Violin by Day, Faceted by Gender",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t373_l1828
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 2 (:panels s)))))
   v372_l1820)))


(def
 v375_l1835
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :class)
  pj/lay-bar
  (pj/facet :year)
  (pj/options
   {:title "Vehicle Class Count by Model Year",
    :x-label "Class",
    :y-label "Count"})))


(deftest
 t376_l1843
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v375_l1835)))


(def
 v378_l1848
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet :species)
  (pj/options
   {:title "Iris Petals: Faceted Regression",
    :x-label "Petal Length",
    :y-label "Petal Width"})))


(deftest
 t379_l1857
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (= 3 (:lines s)) (= 3 (:panels s)))))
   v378_l1848)))


(def
 v381_l1865
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-boxplot
  (pj/facet {:col :time, :row :smoker})
  (pj/options
   {:title "Tips: Day x Time x Smoker",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t382_l1873
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v381_l1865)))


(def
 v384_l1878
 (->
  (tc/select-rows
   (rdatasets/gapminder-gapminder)
   (fn* [p1__88921#] (= 2007 (:year p1__88921#))))
  (pj/pose :gdp-percap :life-exp)
  pj/lay-point
  (pj/scale :x :log)
  (pj/facet :continent)
  (pj/options
   {:title "Gapminder 2007 by Continent",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t385_l1887
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v384_l1878)))


(def
 v387_l1892
 (->
  (rdatasets/lme4-sleepstudy)
  (pj/pose :days :reaction)
  pj/lay-line
  pj/lay-point
  (pj/facet :subject)
  (pj/options
   {:title "Sleep Study: Each Subject",
    :x-label "Days",
    :y-label "Reaction Time (ms)"})))


(deftest
 t388_l1901
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (= 180 (:points s)))))
   v387_l1892)))


(def
 v390_l1908
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  pj/lay-smooth
  (pj/facet :cyl)
  (pj/options
   {:title "MPG: Scatter + LOESS by Cylinder Count",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t391_l1917
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v390_l1908)))


(def
 v393_l1927
 (->
  (rdatasets/datasets-mtcars)
  (pj/matrix [:mpg :hp :wt])
  (pj/options {:title "Motor Trend Cars: 3x3 SPLOM"})))


(deftest
 t394_l1931
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v393_l1927)))


(def
 v396_l1936
 (->
  (rdatasets/datasets-mtcars)
  (pj/matrix [:mpg :wt])
  (pj/options {:title "MPG vs Weight: 2x2 SPLOM"})))


(deftest
 t397_l1940
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v396_l1936)))


(def
 v399_l1948
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/random 2000 {:seed 1, :repeat? false})
  (pj/lay-point :carat :price {:alpha 0.15})
  (pj/scale :y :log)
  (pj/options
   {:title "Diamond Price (Log Scale)",
    :x-label "Carat",
    :y-label "Price ($, log)"})))


(deftest
 t400_l1956
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v399_l1948)))


(def
 v402_l1961
 (->
  msleep
  (pj/lay-point :bodywt :sleep-total {:color :vore})
  (pj/scale :x :log)
  (pj/options
   {:title "Body Weight vs Sleep (log x-axis)",
    :x-label "Body Weight (kg, log)",
    :y-label "Total Sleep (hours)"})))


(deftest
 t403_l1968
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v402_l1961)))


(def
 v405_l1976
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut {:color :color})
  (pj/lay-bar {:position :stack})
  (pj/options
   {:title "Diamonds by Cut and Color (Stacked)",
    :x-label "Cut",
    :y-label "Count"})))


(deftest
 t406_l1983
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v405_l1976)))


(def
 v408_l1992
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  (pj/lay-bar
   {:data
    (->
     (rdatasets/reshape2-tips)
     (tc/group-by [:day])
     (tc/aggregate
      {:total-bill
       (fn*
        [p1__88922#]
        (/
         (reduce + (:total-bill p1__88922#))
         (tc/row-count p1__88922#)))})),
    :alpha 0.4})
  (pj/lay-point {:jitter true})
  (pj/options
   {:title "Tips: Mean Total Bill by Day, and Each Bill",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t409_l2004
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) n (tc/row-count (rdatasets/reshape2-tips))]
     (and (= 4 (:polygons s)) (= n (:points s)))))
   v408_l1992)))


(def
 v411_l2019
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-density-2d
  (pj/options
   {:title "Iris: 2D Density",
    :x-label "Sepal Length",
    :y-label "Sepal Width"})))


(deftest
 t412_l2026
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v411_l2019)))


(def
 v414_l2031
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/random 1000 {:seed 1, :repeat? false})
  (pj/pose :carat :price)
  pj/lay-contour
  pj/lay-point
  (pj/options
   {:title "Diamonds: Contour + Scatter",
    :x-label "Carat",
    :y-label "Price ($)"})))


(deftest
 t415_l2040
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v414_l2031)))


(def
 v417_l2052
 (->
  {:task ["Design" "Build" "Integration" "Testing" "Launch"],
   :start
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-15T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"],
   :end
   [#inst "2024-02-15T00:00:00.000-00:00"
    #inst "2024-04-15T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"
    #inst "2024-06-15T00:00:00.000-00:00"],
   :team ["A" "B" "B" "C" "A"]}
  (pj/lay-interval-h :start :task {:x-end :end, :color :team})
  (pj/options {:title "Project Schedule by Team"})))


(deftest
 t418_l2063
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v417_l2052)))
