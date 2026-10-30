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
 v6_l48
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 500)
  (pj/pose :carat :price {:color :cut, :size :depth})
  pj/lay-point
  (pj/options
   {:title "Diamond Price vs Carat (bubble)",
    :x-label "Carat",
    :y-label "Price (USD)"})))


(deftest
 t7_l56
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 500 (:points s)))) v6_l48)))


(def
 v9_l64
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :sex})
  pj/lay-point
  (pj/options
   {:title "Total Bill by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t10_l71
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 244 (:points s)))) v9_l64)))


(def
 v12_l81
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :price)
  pj/lay-histogram
  (pj/options
   {:title "Distribution of Diamond Prices",
    :x-label "Price (USD)",
    :y-label "Count"})))


(deftest
 t13_l88
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s)))) v12_l81)))


(def
 v15_l93
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :price {:color :cut})
  pj/lay-histogram
  (pj/options
   {:title "Diamond Prices by Cut",
    :x-label "Price (USD)",
    :y-label "Count"})))


(deftest
 t16_l100
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (< 1 (:polygons s)))) v15_l93)))


(def
 v18_l108
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :carat {:color :cut})
  pj/lay-density
  (pj/options
   {:title "Carat Distribution by Cut",
    :x-label "Carat",
    :y-label "Density"})))


(deftest
 t19_l115
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v18_l108)))


(def
 v21_l120
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 500)
  (pj/pose :carat)
  pj/lay-density
  pj/lay-rug
  (pj/options
   {:title "Carat Distribution with Rug",
    :x-label "Carat",
    :y-label "Density"})))


(deftest
 t22_l129
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v21_l120)))


(def
 v24_l138
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-boxplot
  (pj/options
   {:title "Total Bill by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t25_l145
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v24_l138)))


(def
 v27_l150
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
 t28_l158
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v27_l150)))


(def
 v30_l167
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-violin
  (pj/options
   {:title "Total Bill by Day (violin)",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t31_l174
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v30_l167)))


(def
 v33_l179
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
 t34_l187
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v33_l179)))


(def
 v36_l195
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut :price)
  pj/lay-ridgeline
  (pj/options
   {:title "Price Distribution by Cut (ridgeline)",
    :x-label "Cut",
    :y-label "Price (USD)"})))


(deftest
 t37_l202
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v36_l195)))


(def
 v39_l212
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/options
   {:title "Diamond Count by Cut", :x-label "Cut", :y-label "Count"})))


(deftest
 t40_l219
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v39_l212)))


(def
 v42_l229
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
 t43_l237
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v42_l229)))


(def
 v45_l245
 (def
  mpg-mfr-counts
  (->
   (rdatasets/ggplot2-mpg)
   (tc/group-by [:manufacturer])
   (tc/aggregate {:count tc/row-count})
   (tc/order-by [:count] :desc)
   (tc/select-rows (range 8)))))


(def
 v46_l252
 (->
  mpg-mfr-counts
  (pj/pose :manufacturer :count)
  pj/lay-lollipop
  (pj/options
   {:title "Top Manufacturers by Model Count",
    :x-label "Manufacturer",
    :y-label "Count"})))


(deftest
 t47_l259
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v46_l252)))


(def
 v49_l265
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
 t50_l273
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v49_l265)))


(def
 v52_l284
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-line
  (pj/options
   {:title "US Unemployment Over Time",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t53_l291
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v52_l284)))


(def
 v55_l296
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn*
    [p1__81210#]
    (#{"Australia" "Brazil" "Japan" "Nigeria" "Germany"}
     (:country p1__81210#))))
  (pj/pose :year :life-exp {:color :country})
  pj/lay-line
  pj/lay-point
  (pj/options
   {:title "Life Expectancy Over Time",
    :x-label "Year",
    :y-label "Life Expectancy"})))


(deftest
 t56_l306
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v55_l296)))


(def
 v58_l315
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-area
  (pj/options
   {:title "US Unemployment Over Time (area)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t59_l322
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v58_l315)))


(def
 v61_l332
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 2000)
  (pj/pose :carat :price)
  pj/lay-density-2d
  (pj/options
   {:title "Diamond Carat vs Price (density)",
    :x-label "Carat",
    :y-label "Price (USD)"})))


(deftest
 t62_l340
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v61_l332)))


(def
 v64_l348
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
 t65_l356
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v64_l348)))


(def
 v67_l366
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
 t68_l374
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v67_l366)))


(def
 v70_l385
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
 t71_l393
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v70_l385)))


(def
 v73_l398
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
 t74_l406
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v73_l398)))


(def
 v76_l418
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width :petal-length :petal-width]},
    :row {:y [:sepal-length :sepal-width :petal-length :petal-width]}})
  (pj/options {:title "Iris SPLOM"})))


(deftest
 t77_l424
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 16 (:panels s))
      (= (* 12 150) (:points s))
      (pos? (:polygons s)))))
   v76_l418)))


(def
 v79_l436
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day {:color :sex})
  (pj/lay-bar {:position :stack})
  (pj/options
   {:title "Tips by Day and Sex (stacked bar)",
    :x-label "Day",
    :y-label "Count"})))


(deftest
 t80_l443
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v79_l436)))


(def
 v82_l449
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day {:color :sex})
  (pj/lay-bar {:position :fill})
  (pj/options
   {:title "Proportion by Day and Sex",
    :x-label "Day",
    :y-label "Proportion"})))


(deftest
 t83_l456
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v82_l449)))


(def
 v85_l465
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
 t86_l475
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v85_l465)))


(def
 v88_l485
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Diamond Cut (rose chart)"})))


(deftest
 t89_l491
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v88_l485)))


(def
 v91_l497
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Tips Count by Day (Rose)"})))


(deftest
 t92_l503
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v91_l497)))


(def
 v94_l508
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Chick Count by Feed (Rose)"})))


(deftest
 t95_l514
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v94_l508)))


(def
 v97_l519
 (->
  {:day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"],
   :hours [8 7 6 9 5 3 4]}
  (pj/lay-bar :day :hours)
  (pj/coord :polar)
  (pj/options {:title "Weekly Working Hours (Polar)"})))


(deftest
 t98_l525
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v97_l519)))


(def
 v100_l533
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
 t101_l541
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (count (:texts s))))))
   v100_l533)))


(def
 v103_l548
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
 t104_l556
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v103_l548)))


(def
 v106_l565
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options {:title "Tips by Day and Gender"})))


(deftest
 t107_l569
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v106_l565)))


(def
 v109_l579
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
 t110_l587
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v109_l579)))


(def
 v112_l592
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-summary :day :total-bill {:color :sex})
  (pj/options {:title "Average Bill with Standard Error"})))


(deftest
 t113_l596
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:points s)))) v112_l592)))


(def
 v115_l602
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows (fn* [p1__81211#] (= 2007 (:year p1__81211#))))
  (pj/lay-point :gdp-percap :life-exp {:color :continent, :size :pop})
  (pj/scale :x :log)
  (pj/options
   {:title "Gapminder 2007: Life Expectancy vs GDP",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t116_l610
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v115_l602)))


(def
 v118_l615
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn*
    [p1__81212#]
    (#{"Brazil" "United States" "Japan" "China" "India"}
     (:country p1__81212#))))
  (pj/lay-line :year :life-exp {:color :country})
  (pj/options
   {:title "Life Expectancy Over Time",
    :x-label "Year",
    :y-label "Life Expectancy"})))


(deftest
 t119_l622
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v118_l615)))


(def
 v121_l627
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-step :date :unemploy)
  (pj/options
   {:title "US Unemployment (Step)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t122_l633
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v121_l627)))


(def
 v124_l638
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  pj/lay-density
  pj/lay-rug
  (pj/options {:title "Iris Sepal Length: Density + Rug"})))


(deftest
 t125_l644
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v124_l638)))


(def
 v127_l649
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
 t128_l657
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v127_l649)))


(def
 v130_l664
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-histogram :hwy {:color :drv})
  (pj/facet :drv)
  (pj/options {:title "Highway MPG by Drive Type"})))


(deftest
 t131_l669
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v130_l664)))


(def
 v133_l675
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-rule-v {:x-intercept 6.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  (pj/options {:title "Iris with Reference Lines and Band"})))


(deftest
 t134_l682
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v133_l675)))


(def
 v136_l689
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-violin
  pj/lay-boxplot
  (pj/options {:title "Tips Distribution by Day"})))


(deftest
 t137_l695
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v136_l689)))


(def
 v139_l701
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-lollipop :rownames :mpg)
  (pj/coord :flip)
  (pj/options {:title "Cars Ranked by MPG"})))


(deftest
 t140_l706
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v139_l701)))


(def
 v142_l713
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :fill, :color :sex})
  (pj/options {:title "Gender Proportion by Day (100% stacked)"})))


(deftest
 t143_l717
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v142_l713)))


(def
 v145_l722
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-histogram {:normalize :density})
  pj/lay-density
  (pj/options {:title "Sepal Length: Histogram + Density Curve"})))


(deftest
 t146_l728
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v145_l722)))


(def
 v148_l734
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :fixed)
  (pj/options {:title "Iris Sepals (Equal Aspect Ratio)"})))


(deftest
 t149_l739
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v148_l734)))


(def
 v151_l744
 (->
  {:category ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"],
   :value [120 200 150 80 70 110 130]}
  (pj/lay-bar :category :value)
  (pj/options {:title "Weekly Sales"})))


(deftest
 t152_l749
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v151_l744)))


(def
 v154_l754
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})
  (pj/options
   {:title "Sepal Length by Species", :x-label "Sepal Length (cm)"})))


(deftest
 t155_l759
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v154_l754)))


(def
 v157_l764
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options {:title "Iris: Scatter + LOESS by Species"})))


(deftest
 t158_l770
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v157_l764)))


(def
 v160_l777
 (->
  (rdatasets/datasets-iris)
  (pj/lay-summary :species :sepal-length)
  (pj/options {:title "Mean Sepal Length +/- SE by Species"})))


(deftest
 t161_l781
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v160_l777)))


(def
 v163_l786
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/lay-rug
  (pj/options {:title "Iris: Scatter with Rug Marks"})))


(deftest
 t164_l792
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v163_l786)))


(def
 v166_l803
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
 t167_l812
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v166_l803)))


(def
 v169_l822
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
 t170_l830
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:polygons s)))))
   v169_l822)))


(def
 v172_l839
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
 t173_l847
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v172_l839)))


(def
 v175_l854
 (->
  (rdatasets/ggplot2-txhousing)
  (tc/select-rows
   (fn*
    [p1__81213#]
    (#{"Houston" "Dallas" "San Antonio" "Austin"} (:city p1__81213#))))
  (pj/pose :date :median {:color :city})
  pj/lay-line
  (pj/options
   {:title "Texas Median Home Prices",
    :x-label "Date",
    :y-label "Median Price ($)"})))


(deftest
 t176_l862
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v175_l854)))


(def
 v178_l869
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
 t179_l877
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (= 180 (:points s)))))
   v178_l869)))


(def
 v181_l884
 (->
  (rdatasets/lme4-sleepstudy)
  (tc/select-rows
   (fn* [p1__81214#] (= "308" (str (:subject p1__81214#)))))
  (pj/pose :days :reaction)
  pj/lay-step
  pj/lay-point
  (pj/options
   {:title "Subject 308: Reaction Time (Step)",
    :x-label "Days",
    :y-label "Reaction Time (ms)"})))


(deftest
 t182_l893
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v181_l884)))


(def
 v184_l903
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-point
  (pj/options
   {:title "Old Faithful Geyser",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t185_l910
 (is ((fn [v] (= 272 (:points (pj/svg-summary v)))) v184_l903)))


(def
 v187_l915
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
 t188_l923
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:lines s)))))
   v187_l915)))


(def
 v190_l934
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.05})
  (pj/options
   {:title "Diamond Price vs Carat (alpha = 0.05)",
    :x-label "Carat",
    :y-label "Price (US dollars)",
    :format :bufimg})))


(deftest
 t191_l941
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v190_l934)))


(def
 v193_l948
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :wt :mpg {:color :hp})
  (pj/options
   {:title "Cars: Color by Horsepower",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t194_l954
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v193_l948)))


(def
 v196_l959
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :hp :mpg {:color :cyl, :size :disp})
  (pj/options
   {:title "Cars: Color by Cylinders, Size by Displacement",
    :x-label "Horsepower",
    :y-label "Miles per Gallon"})))


(deftest
 t197_l965
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v196_l959)))


(def
 v199_l970
 (->
  (tc/select-rows
   (rdatasets/gapminder-gapminder)
   (fn* [p1__81215#] (= 2007 (:year p1__81215#))))
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
 t200_l977
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v199_l970)))


(def
 v202_l982
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
 t203_l988
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v202_l982)))


(def
 v205_l993
 (def
  msleep
  (tc/drop-missing
   (rdatasets/ggplot2-msleep)
   [:sleep-total :bodywt :brainwt :vore])))


(def
 v206_l996
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
 t207_l1004
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v206_l996)))


(def
 v209_l1009
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
 t210_l1017
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v209_l1009)))


(def
 v212_l1022
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
 t213_l1032
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (pos? (count (:texts s))))))
   v212_l1022)))


(def
 v215_l1039
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
 t216_l1047
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v215_l1039)))


(def
 v218_l1054
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
 t219_l1062
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 32 (:points s)) (pos? (:lines s)) (pos? (:polygons s)))))
   v218_l1054)))


(def
 v221_l1072
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
 t222_l1081
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 32 (:points s)) (>= (:lines s) 2))))
   v221_l1072)))


(def
 v224_l1091
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions)
  (pj/lay-histogram {:normalize :density, :binwidth 0.25})
  pj/lay-density
  (pj/options
   {:title "Old Faithful: Histogram + Density",
    :x-label "Eruption Duration (min)",
    :y-label "Density"})))


(deftest
 t225_l1099
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v224_l1091)))


(def
 v227_l1104
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
 t228_l1112
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v227_l1104)))


(def
 v230_l1119
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :depth)
  pj/lay-density
  (pj/options
   {:title "Distribution of Diamond Depth",
    :x-label "Depth (%)",
    :y-label "Density"})))


(deftest
 t231_l1126
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v230_l1119)))


(def
 v233_l1131
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :depth)
  (pj/lay-histogram {:normalize :density})
  pj/lay-density
  (pj/options
   {:title "Diamond Depth: Histogram + Density",
    :x-label "Depth (%)",
    :y-label "Density"})))


(deftest
 t234_l1139
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v233_l1131)))


(def
 v236_l1144
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :petal-width {:color :species})
  (pj/options
   {:title "Iris Petal Width by Species",
    :x-label "Petal Width (cm)",
    :y-label "Density"})))


(deftest
 t237_l1150
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v236_l1144)))


(def
 v239_l1155
 (->
  msleep
  (pj/lay-density :sleep-total {:color :vore})
  (pj/options
   {:title "Sleep Duration by Diet Type",
    :x-label "Total Sleep (hours)",
    :y-label "Density"})))


(deftest
 t240_l1161
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v239_l1155)))


(def
 v242_l1166
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :waiting)
  (pj/lay-histogram {:bins 15})
  (pj/options
   {:title "Waiting Time Between Eruptions (15 bins)",
    :x-label "Waiting Time (min)",
    :y-label "Count"})))


(deftest
 t243_l1173
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v242_l1166)))


(def
 v245_l1178
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
  pj/lay-density
  (pj/options
   {:title "Simulated Distribution: Histogram + Density",
    :x-label "Value",
    :y-label "Density"})))


(deftest
 t246_l1190
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v245_l1178)))


(def
 v248_l1195
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed :weight {:color :feed})
  pj/lay-boxplot
  (pj/options
   {:title "Chick Weight by Feed Type",
    :x-label "Feed",
    :y-label "Weight (g)"})))


(deftest
 t249_l1202
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v248_l1195)))


(def
 v251_l1207
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
 t252_l1215
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v251_l1207)))


(def
 v254_l1222
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :sex})
  pj/lay-boxplot
  (pj/options
   {:title "Tips by Day and Gender (Grouped Boxplot)",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t255_l1229
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v254_l1222)))


(def
 v257_l1234
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :sepal-width {:color :species})
  pj/lay-violin
  (pj/options
   {:title "Iris Sepal Width (Violin)",
    :x-label "Species",
    :y-label "Sepal Width (cm)"})))


(deftest
 t258_l1241
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v257_l1234)))


(def
 v260_l1246
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
 t261_l1254
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v260_l1246)))


(def
 v263_l1262
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
 t264_l1270
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 244 (:points s)))))
   v263_l1262)))


(def
 v266_l1279
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
 t267_l1288
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v266_l1279)))


(def
 v269_l1295
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :smoker :total-bill {:color :smoker})
  pj/lay-violin
  (pj/options
   {:title "Total Bill by Smoking Status",
    :x-label "Smoker",
    :y-label "Total Bill ($)"})))


(deftest
 t270_l1302
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v269_l1295)))


(def
 v272_l1307
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :petal-length)
  pj/lay-ridgeline
  (pj/options
   {:title "Iris Petal Length by Species (Ridgeline)",
    :x-label "Species",
    :y-label "Petal Length (cm)"})))


(deftest
 t273_l1314
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v272_l1307)))


(def
 v275_l1319
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :color :price)
  pj/lay-ridgeline
  (pj/options
   {:title "Diamond Price by Color Grade (Ridgeline)",
    :x-label "Color",
    :y-label "Price ($)"})))


(deftest
 t276_l1326
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v275_l1319)))


(def
 v278_l1331
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
       [p1__81216#]
       (get {5 "May", 6 "Jun", 7 "Jul", 8 "Aug", 9 "Sep"} p1__81216#))
      (ds :month)))))))


(def
 v279_l1338
 (->
  airquality
  (pj/pose :month-name :ozone {:color :month-name})
  pj/lay-boxplot
  (pj/options
   {:title "New York Ozone by Month",
    :x-label "Month",
    :y-label "Ozone (ppb)"})))


(deftest
 t280_l1345
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v279_l1338)))


(def
 v282_l1353
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :class)
  pj/lay-bar
  (pj/options
   {:title "Vehicle Count by Class",
    :x-label "Class",
    :y-label "Count"})))


(deftest
 t283_l1360
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v282_l1353)))


(def
 v285_l1367
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options
   {:title "Tips Count by Day and Gender",
    :x-label "Day",
    :y-label "Count"})))


(deftest
 t286_l1373
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v285_l1367)))


(def
 v288_l1378
 (->
  {:country ["US" "China" "Japan" "Germany" "UK" "India" "France"],
   :gdp [21.4 14.7 5.1 3.8 2.8 2.7 2.6]}
  (pj/lay-bar :gdp :country)
  (pj/options
   {:title "GDP by Country (2019)",
    :x-label "GDP (Trillion $)",
    :y-label "Country"})))


(deftest
 t289_l1385
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v288_l1378)))


(def
 v291_l1392
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
 t292_l1400
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:polygons s)) (pos? (:lines s)))))
   v291_l1392)))


(def
 v294_l1407
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
 t295_l1416
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v294_l1407)))


(def
 v297_l1423
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
 t298_l1432
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v297_l1423)))


(def
 v300_l1442
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-density-2d
  (pj/options
   {:title "Old Faithful: 2D Density",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t301_l1449
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v300_l1442)))


(def
 v303_l1454
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
 t304_l1462
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:visible-tiles s)))))
   v303_l1454)))


(def
 v306_l1469
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
 t307_l1477
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:lines s)))))
   v306_l1469)))


(def
 v309_l1484
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :petal-length)
  pj/lay-contour
  (pj/options
   {:title "Iris: Sepal vs Petal Length Contour",
    :x-label "Sepal Length",
    :y-label "Petal Length"})))


(deftest
 t310_l1491
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v309_l1484)))


(def
 v312_l1498
 (->
  (rdatasets/ggplot2-faithfuld)
  (pj/pose :eruptions :waiting {:fill :density})
  pj/lay-tile
  (pj/options
   {:title "Old Faithful: Pre-computed Density Heatmap",
    :x-label "Eruption Duration",
    :y-label "Waiting Time"})))


(deftest
 t313_l1505
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v312_l1498)))


(def
 v315_l1510
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 3000)
  (pj/pose :carat :price)
  pj/lay-point
  pj/lay-density-2d
  (pj/options
   {:title "Diamonds: Scatter + 2D Density",
    :x-label "Carat",
    :y-label "Price ($)"})))


(deftest
 t316_l1519
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:visible-tiles s)))))
   v315_l1510)))


(def
 v318_l1526
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-density-2d
  (pj/options
   {:title "MPG: Displacement vs Highway (Density)",
    :x-label "Displacement (L)",
    :y-label "Highway MPG"})))


(deftest
 t319_l1533
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v318_l1526)))


(def
 v321_l1538
 (->
  {:row (mapcat (fn* [p1__81217#] (repeat 6 p1__81217#)) (range 6)),
   :col (flatten (repeat 6 (range 6))),
   :value
   (map (fn* [p1__81218#] (Math/sin (* p1__81218# 0.5))) (range 36))}
  (pj/pose :col :row {:fill :value})
  pj/lay-tile
  (pj/options
   {:title "Synthetic Heatmap (sin wave)",
    :x-label "Column",
    :y-label "Row"})))


(deftest
 t322_l1547
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v321_l1538)))


(def
 v324_l1555
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
 t325_l1568
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v324_l1555)))


(def
 v327_l1575
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-summary :day :tip {:color :sex})
  (pj/options
   {:title "Mean Tip +/- SE by Day and Gender",
    :x-label "Day",
    :y-label "Tip ($)"})))


(deftest
 t328_l1581
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v327_l1575)))


(def
 v330_l1593
 (->
  (rdatasets/ggplot2-economics)
  (pj/arrange [{:x :date, :y :unemploy} {:x :date, :y :uempmed}])
  pj/lay-line
  (pj/options {:title "Unemployment: Total vs Median Duration"})))


(deftest
 t331_l1599
 (is ((fn [v] (>= (:lines (pj/svg-summary v)) 2)) v330_l1593)))


(def
 v333_l1604
 (->
  (rdatasets/ggplot2-economics)
  (pj/arrange
   [{:x :date, :y :unemploy}
    {:x :date, :y :uempmed}
    {:x :date, :y :psavert}])
  pj/lay-line
  (pj/options {:title "US Economic Indicators"})))


(deftest
 t334_l1611
 (is ((fn [v] (>= (:lines (pj/svg-summary v)) 3)) v333_l1604)))


(def
 v336_l1621
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
 t337_l1629
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v336_l1621)))


(def
 v339_l1639
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
 t340_l1650
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:lines s) 4))))
   v339_l1639)))


(def
 v342_l1657
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
 t343_l1666
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v342_l1657)))


(def
 v345_l1671
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
 t346_l1679
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v345_l1671)))


(def
 v348_l1686
 (->
  airquality
  (pj/lay-line :rownames :ozone)
  (pj/lay-rule-h {:y-intercept 60})
  (pj/options
   {:title "NYC Ozone with Threshold at 60 ppb",
    :x-label "Observation",
    :y-label "Ozone (ppb)"})))


(deftest
 t349_l1693
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (and (pos? (:lines s)))))
   v348_l1686)))


(def
 v351_l1699
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
 t352_l1707
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v351_l1699)))


(def
 v354_l1715
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
 t355_l1723
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v354_l1715)))


(def
 v357_l1728
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
 t358_l1736
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 6 (:panels (pj/svg-summary v)))
      (= ["f" "4" "r"] (distinct (map :col-label panels)))
      (= ["1999" "2008"] (distinct (map :row-label panels))))))
   v357_l1728)))


(def
 v360_l1749
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
 t361_l1757
 (is ((fn [v] (pos? (:panels (pj/svg-summary v)))) v360_l1749)))


(def
 v363_l1762
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
 t364_l1770
 (is
  ((fn
    [v]
    (and
     (= 3 (:panels (pj/svg-summary v)))
     (= #{0} (set (map :col (:panels (pj/plan v)))))))
   v363_l1762)))


(def
 v366_l1777
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
 t367_l1785
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v366_l1777)))


(def
 v369_l1790
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
 t370_l1798
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 2 (:panels s)))))
   v369_l1790)))


(def
 v372_l1805
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
 t373_l1813
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 2 (:panels s)))))
   v372_l1805)))


(def
 v375_l1820
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
 t376_l1828
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v375_l1820)))


(def
 v378_l1833
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
 t379_l1842
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (= 3 (:lines s)) (= 3 (:panels s)))))
   v378_l1833)))


(def
 v381_l1850
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
 t382_l1858
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v381_l1850)))


(def
 v384_l1863
 (->
  (tc/select-rows
   (rdatasets/gapminder-gapminder)
   (fn* [p1__81219#] (= 2007 (:year p1__81219#))))
  (pj/pose :gdp-percap :life-exp)
  pj/lay-point
  (pj/scale :x :log)
  (pj/facet :continent)
  (pj/options
   {:title "Gapminder 2007 by Continent",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t385_l1872
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v384_l1863)))


(def
 v387_l1877
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
 t388_l1886
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (= 180 (:points s)))))
   v387_l1877)))


(def
 v390_l1893
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
 t391_l1902
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v390_l1893)))


(def
 v393_l1912
 (->
  (rdatasets/datasets-mtcars)
  (pj/facet {:col {:x [:mpg :hp :wt]}, :row {:y [:mpg :hp :wt]}})
  (pj/options {:title "Motor Trend Cars: 3x3 SPLOM"})))


(deftest
 t394_l1917
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v393_l1912)))


(def
 v396_l1922
 (->
  (rdatasets/datasets-mtcars)
  (pj/facet {:col {:x [:mpg :wt]}, :row {:y [:mpg :wt]}})
  (pj/options {:title "MPG vs Weight: 2x2 SPLOM"})))


(deftest
 t397_l1927
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v396_l1922)))


(def
 v399_l1935
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 2000)
  (pj/lay-point :carat :price {:alpha 0.15})
  (pj/scale :y :log)
  (pj/options
   {:title "Diamond Price (Log Scale)",
    :x-label "Carat",
    :y-label "Price ($, log)"})))


(deftest
 t400_l1943
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v399_l1935)))


(def
 v402_l1948
 (->
  msleep
  (pj/lay-point :bodywt :sleep-total {:color :vore})
  (pj/scale :x :log)
  (pj/options
   {:title "Body Weight vs Sleep (log x-axis)",
    :x-label "Body Weight (kg, log)",
    :y-label "Total Sleep (hours)"})))


(deftest
 t403_l1955
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v402_l1948)))


(def
 v405_l1963
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut {:color :color})
  (pj/lay-bar {:position :stack})
  (pj/options
   {:title "Diamonds by Cut and Color (Stacked)",
    :x-label "Cut",
    :y-label "Count"})))


(deftest
 t406_l1970
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v405_l1963)))


(def
 v408_l1979
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
        [p1__81220#]
        (/
         (reduce + (:total-bill p1__81220#))
         (tc/row-count p1__81220#)))})),
    :alpha 0.4})
  (pj/lay-point {:jitter true})
  (pj/options
   {:title "Tips: Mean Total Bill by Day, and Each Bill",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t409_l1991
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) n (tc/row-count (rdatasets/reshape2-tips))]
     (and (= 4 (:polygons s)) (= n (:points s)))))
   v408_l1979)))


(def
 v411_l2006
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-density-2d
  (pj/options
   {:title "Iris: 2D Density",
    :x-label "Sepal Length",
    :y-label "Sepal Width"})))


(deftest
 t412_l2013
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v411_l2006)))


(def
 v414_l2018
 (->
  (rdatasets/ggplot2-diamonds)
  (tc/head 1000)
  (pj/pose :carat :price)
  pj/lay-contour
  pj/lay-point
  (pj/options
   {:title "Diamonds: Contour + Scatter",
    :x-label "Carat",
    :y-label "Price ($)"})))


(deftest
 t415_l2027
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v414_l2018)))


(def
 v417_l2039
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
 t418_l2050
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v417_l2039)))
