(ns
 plotje-book.gallery-generated-test
 (:require
  [scicloj.plotje.api :as pj]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [tech.v3.datatype.functional :as dfn]
  [fastmath.stats :as fstats]
  [clojure.test :refer [deftest is]]))


(def
 v3_l28
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
 t4_l36
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v3_l28)))


(def
 v6_l46
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
 t7_l54
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 500 (:points s)))) v6_l46)))


(def
 v9_l62
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :sex})
  pj/lay-point
  (pj/options
   {:title "Total Bill by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t10_l69
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 244 (:points s)))) v9_l62)))


(def
 v12_l79
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :price)
  pj/lay-histogram
  (pj/options
   {:title "Distribution of Diamond Prices",
    :x-label "Price (USD)",
    :y-label "Count"})))


(deftest
 t13_l86
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s)))) v12_l79)))


(def
 v15_l91
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :price {:color :cut})
  pj/lay-histogram
  (pj/options
   {:title "Diamond Prices by Cut",
    :x-label "Price (USD)",
    :y-label "Count"})))


(deftest
 t16_l98
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (< 1 (:polygons s)))) v15_l91)))


(def
 v18_l106
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :carat {:color :cut})
  pj/lay-density
  (pj/options
   {:title "Carat Distribution by Cut",
    :x-label "Carat",
    :y-label "Density"})))


(deftest
 t19_l113
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v18_l106)))


(def
 v21_l118
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
 t22_l127
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v21_l118)))


(def
 v24_l136
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-boxplot
  (pj/options
   {:title "Total Bill by Day",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t25_l143
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v24_l136)))


(def
 v27_l148
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
 t28_l156
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v27_l148)))


(def
 v30_l165
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :day})
  pj/lay-violin
  (pj/options
   {:title "Total Bill by Day (violin)",
    :x-label "Day",
    :y-label "Total Bill (USD)"})))


(deftest
 t31_l172
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v30_l165)))


(def
 v33_l177
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
 t34_l185
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v33_l177)))


(def
 v36_l193
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut :price)
  pj/lay-ridgeline
  (pj/options
   {:title "Price Distribution by Cut (ridgeline)",
    :x-label "Cut",
    :y-label "Price (USD)"})))


(deftest
 t37_l200
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v36_l193)))


(def
 v39_l210
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/options
   {:title "Diamond Count by Cut", :x-label "Cut", :y-label "Count"})))


(deftest
 t40_l217
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v39_l210)))


(def
 v42_l226
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
 t43_l234
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v42_l226)))


(def
 v45_l242
 (def
  mpg-mfr-counts
  (->
   (rdatasets/ggplot2-mpg)
   (tc/group-by [:manufacturer])
   (tc/aggregate {:count tc/row-count})
   (tc/order-by [:count] :desc)
   (tc/select-rows (range 8)))))


(def
 v46_l249
 (->
  mpg-mfr-counts
  (pj/pose :manufacturer :count)
  pj/lay-lollipop
  (pj/options
   {:title "Top Manufacturers by Model Count",
    :x-label "Manufacturer",
    :y-label "Count"})))


(deftest
 t47_l256
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v46_l249)))


(def
 v49_l262
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
 t50_l270
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v49_l262)))


(def
 v52_l281
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-line
  (pj/options
   {:title "US Unemployment Over Time",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t53_l288
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:lines s)))) v52_l281)))


(def
 v55_l293
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn*
    [p1__88834#]
    (#{"Australia" "Brazil" "Japan" "Nigeria" "Germany"}
     (:country p1__88834#))))
  (pj/pose :year :life-exp {:color :country})
  pj/lay-line
  pj/lay-point
  (pj/options
   {:title "Life Expectancy Over Time",
    :x-label "Year",
    :y-label "Life Expectancy"})))


(deftest
 t56_l303
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v55_l293)))


(def
 v58_l312
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose :date :unemploy)
  pj/lay-area
  (pj/options
   {:title "US Unemployment Over Time (area)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t59_l319
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 1 (:polygons s)))) v58_l312)))


(def
 v61_l329
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
 t62_l337
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:visible-tiles s))))
   v61_l329)))


(def
 v64_l345
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
 t65_l353
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v64_l345)))


(def
 v67_l362
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  pj/lay-contour
  (pj/options
   {:title "Iris Sepal Dimensions (contour)",
    :x-label "Sepal Length",
    :y-label "Sepal Width"})))


(deftest
 t68_l370
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v67_l362)))


(def
 v70_l381
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy {:color :class})
  pj/lay-point
  (pj/facet-grid :drv nil)
  (pj/options
   {:title "Highway MPG by Engine Size, faceted by Drive",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t71_l389
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v70_l381)))


(def
 v73_l394
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :hwy)
  pj/lay-histogram
  (pj/facet-grid :drv nil)
  (pj/options
   {:title "Highway MPG by Drive Type",
    :x-label "Highway MPG",
    :y-label "Count"})))


(deftest
 t74_l402
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v73_l394)))


(def
 v76_l414
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   (pj/cross
    [:sepal-length :sepal-width :petal-length :petal-width]
    [:sepal-length :sepal-width :petal-length :petal-width])
   {:color :species})
  (pj/options {:title "Iris SPLOM"})))


(deftest
 t77_l420
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 16 (:panels s))
      (= (* 12 150) (:points s))
      (pos? (:polygons s)))))
   v76_l414)))


(def
 v79_l432
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day {:color :sex})
  (pj/lay-bar {:position :stack})
  (pj/options
   {:title "Tips by Day and Sex (stacked bar)",
    :x-label "Day",
    :y-label "Count"})))


(deftest
 t80_l439
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v79_l432)))


(def
 v82_l445
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day {:color :sex})
  (pj/lay-bar {:position :fill})
  (pj/options
   {:title "Proportion by Day and Sex",
    :x-label "Day",
    :y-label "Proportion"})))


(deftest
 t83_l452
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 8 (:polygons s)))))
   v82_l445)))


(def
 v85_l461
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
 t86_l471
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v85_l461)))


(def
 v88_l481
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Diamond Cut (rose chart)"})))


(deftest
 t89_l487
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 5 (:polygons s)))) v88_l481)))


(def
 v91_l493
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Tips Count by Day (Rose)"})))


(deftest
 t92_l499
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v91_l493)))


(def
 v94_l504
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)
  (pj/options {:title "Chick Count by Feed (Rose)"})))


(deftest
 t95_l510
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v94_l504)))


(def
 v97_l515
 (->
  {:day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"],
   :hours [8 7 6 9 5 3 4]}
  (pj/lay-bar :day :hours)
  (pj/coord :polar)
  (pj/options {:title "Weekly Working Hours (Polar)"})))


(deftest
 t98_l521
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v97_l515)))


(def
 v100_l529
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
 t101_l537
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (count (:texts s))))))
   v100_l529)))


(def
 v103_l544
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
 t104_l552
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v103_l544)))


(def
 v106_l561
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options {:title "Tips by Day and Gender"})))


(deftest
 t107_l565
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v106_l561)))


(def
 v109_l575
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
 t110_l583
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v109_l575)))


(def
 v112_l588
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-summary :day :total-bill {:color :sex})
  (pj/options {:title "Average Bill with Standard Error"})))


(deftest
 t113_l592
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:points s)))) v112_l588)))


(def
 v115_l598
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows (fn* [p1__88835#] (= 2007 (:year p1__88835#))))
  (pj/lay-point :gdp-percap :life-exp {:color :continent, :size :pop})
  (pj/scale :x :log)
  (pj/options
   {:title "Gapminder 2007: Life Expectancy vs GDP",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t116_l606
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v115_l598)))


(def
 v118_l611
 (->
  (rdatasets/gapminder-gapminder)
  (tc/select-rows
   (fn*
    [p1__88836#]
    (#{"Brazil" "United States" "Japan" "China" "India"}
     (:country p1__88836#))))
  (pj/lay-line :year :life-exp {:color :country})
  (pj/options
   {:title "Life Expectancy Over Time",
    :x-label "Year",
    :y-label "Life Expectancy"})))


(deftest
 t119_l618
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v118_l611)))


(def
 v121_l623
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-step :date :unemploy)
  (pj/options
   {:title "US Unemployment (Step)",
    :x-label "Date",
    :y-label "Unemployed (thousands)"})))


(deftest
 t122_l629
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v121_l623)))


(def
 v124_l634
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  pj/lay-density
  pj/lay-rug
  (pj/options {:title "Iris Sepal Length: Density + Rug"})))


(deftest
 t125_l640
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v124_l634)))


(def
 v127_l645
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
 t128_l653
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v127_l645)))


(def
 v130_l660
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-histogram :hwy {:color :drv})
  (pj/facet :drv)
  (pj/options {:title "Highway MPG by Drive Type"})))


(deftest
 t131_l665
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 3 (:panels s)))) v130_l660)))


(def
 v133_l671
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-rule-v {:x-intercept 6.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  (pj/options {:title "Iris with Reference Lines and Band"})))


(deftest
 t134_l678
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v133_l671)))


(def
 v136_l685
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-violin
  pj/lay-boxplot
  (pj/options {:title "Tips Distribution by Day"})))


(deftest
 t137_l691
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (pos? (:polygons s))))
   v136_l685)))


(def
 v139_l697
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-lollipop :rownames :mpg)
  (pj/coord :flip)
  (pj/options {:title "Cars Ranked by MPG"})))


(deftest
 t140_l702
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v139_l697)))


(def
 v142_l709
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :fill, :color :sex})
  (pj/options {:title "Gender Proportion by Day (100% stacked)"})))


(deftest
 t143_l713
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v142_l709)))


(def
 v145_l718
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-histogram {:normalize :density})
  pj/lay-density
  (pj/options {:title "Sepal Length: Histogram + Density Curve"})))


(deftest
 t146_l724
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v145_l718)))


(def
 v148_l730
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :fixed)
  (pj/options {:title "Iris Sepals (Equal Aspect Ratio)"})))


(deftest
 t149_l735
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v148_l730)))


(def
 v151_l740
 (->
  {:category ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"],
   :value [120 200 150 80 70 110 130]}
  (pj/lay-bar :category :value)
  (pj/options {:title "Weekly Sales"})))


(deftest
 t152_l745
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v151_l740)))


(def
 v154_l750
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})
  (pj/options
   {:title "Sepal Length by Species", :x-label "Sepal Length (cm)"})))


(deftest
 t155_l755
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v154_l750)))


(def
 v157_l760
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:confidence-band true})
  (pj/options {:title "Iris: Scatter + LOESS by Species"})))


(deftest
 t158_l766
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v157_l760)))


(def
 v160_l773
 (->
  (rdatasets/datasets-iris)
  (pj/lay-summary :species :sepal-length)
  (pj/options {:title "Mean Sepal Length +/- SE by Species"})))


(deftest
 t161_l777
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v160_l773)))


(def
 v163_l782
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  pj/lay-rug
  (pj/options {:title "Iris: Scatter with Rug Marks"})))


(deftest
 t164_l788
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v163_l782)))


(def
 v166_l799
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
 t167_l808
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v166_l799)))


(def
 v169_l818
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
 t170_l826
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:polygons s)))))
   v169_l818)))


(def
 v172_l835
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
 t173_l843
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v172_l835)))


(def
 v175_l850
 (->
  (rdatasets/ggplot2-txhousing)
  (tc/select-rows
   (fn*
    [p1__88837#]
    (#{"Houston" "Dallas" "San Antonio" "Austin"} (:city p1__88837#))))
  (pj/pose :date :median {:color :city})
  pj/lay-line
  (pj/options
   {:title "Texas Median Home Prices",
    :x-label "Date",
    :y-label "Median Price ($)"})))


(deftest
 t176_l858
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v175_l850)))


(def
 v178_l865
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
 t179_l873
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (= 180 (:points s)))))
   v178_l865)))


(def
 v181_l880
 (->
  (rdatasets/lme4-sleepstudy)
  (tc/select-rows
   (fn* [p1__88838#] (= "308" (str (:subject p1__88838#)))))
  (pj/pose :days :reaction)
  pj/lay-step
  pj/lay-point
  (pj/options
   {:title "Subject 308: Reaction Time (Step)",
    :x-label "Days",
    :y-label "Reaction Time (ms)"})))


(deftest
 t182_l889
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v181_l880)))


(def
 v184_l899
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-point
  (pj/options
   {:title "Old Faithful Geyser",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t185_l906
 (is ((fn [v] (= 272 (:points (pj/svg-summary v)))) v184_l899)))


(def
 v187_l911
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
 t188_l919
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:lines s)))))
   v187_l911)))


(def
 v190_l930
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.05})
  (pj/options
   {:title "Diamond Price vs Carat (alpha = 0.05)",
    :x-label "Carat",
    :y-label "Price (US dollars)",
    :format :bufimg})))


(deftest
 t191_l937
 (is
  ((fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))
   v190_l930)))


(def
 v193_l944
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :wt :mpg {:color :hp})
  (pj/options
   {:title "Cars: Color by Horsepower",
    :x-label "Weight (1000 lbs)",
    :y-label "Miles per Gallon"})))


(deftest
 t194_l950
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v193_l944)))


(def
 v196_l955
 (->
  (rdatasets/datasets-mtcars)
  (pj/lay-point :hp :mpg {:color :cyl, :size :disp})
  (pj/options
   {:title "Cars: Color by Cylinders, Size by Displacement",
    :x-label "Horsepower",
    :y-label "Miles per Gallon"})))


(deftest
 t197_l961
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v196_l955)))


(def
 v199_l966
 (->
  (tc/select-rows
   (rdatasets/gapminder-gapminder)
   (fn* [p1__88839#] (= 2007 (:year p1__88839#))))
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
 t200_l973
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v199_l966)))


(def
 v202_l978
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
 t203_l984
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v202_l978)))


(def
 v205_l989
 (def
  msleep
  (tc/drop-missing
   (rdatasets/ggplot2-msleep)
   [:sleep-total :bodywt :brainwt :vore])))


(def
 v206_l992
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
 t207_l1000
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v206_l992)))


(def
 v209_l1005
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
 t210_l1013
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v209_l1005)))


(def
 v212_l1018
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
 t213_l1028
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (pos? (count (:texts s))))))
   v212_l1018)))


(def
 v215_l1035
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
 t216_l1043
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v215_l1035)))


(def
 v218_l1050
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
 t219_l1058
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 32 (:points s)) (pos? (:lines s)) (pos? (:polygons s)))))
   v218_l1050)))


(def
 v221_l1068
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
 t222_l1077
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 32 (:points s)) (>= (:lines s) 2))))
   v221_l1068)))


(def
 v224_l1087
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
 t225_l1095
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v224_l1087)))


(def
 v227_l1100
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
 t228_l1108
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v227_l1100)))


(def
 v230_l1115
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :depth)
  pj/lay-density
  (pj/options
   {:title "Distribution of Diamond Depth",
    :x-label "Depth (%)",
    :y-label "Density"})))


(deftest
 t231_l1122
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v230_l1115)))


(def
 v233_l1127
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
 t234_l1135
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v233_l1127)))


(def
 v236_l1140
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :petal-width {:color :species})
  (pj/options
   {:title "Iris Petal Width by Species",
    :x-label "Petal Width (cm)",
    :y-label "Density"})))


(deftest
 t237_l1146
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v236_l1140)))


(def
 v239_l1151
 (->
  msleep
  (pj/lay-density :sleep-total {:color :vore})
  (pj/options
   {:title "Sleep Duration by Diet Type",
    :x-label "Total Sleep (hours)",
    :y-label "Density"})))


(deftest
 t240_l1157
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v239_l1151)))


(def
 v242_l1162
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :waiting)
  (pj/lay-histogram {:bins 15})
  (pj/options
   {:title "Waiting Time Between Eruptions (15 bins)",
    :x-label "Waiting Time (min)",
    :y-label "Count"})))


(deftest
 t243_l1169
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v242_l1162)))


(def
 v245_l1174
 (->
  {:value
   (repeatedly
    500
    (fn* [] (+ (* 2.0 (rand)) (* 2.0 (rand)) (* 2.0 (rand)) -3.0)))}
  (pj/pose :value)
  (pj/lay-histogram {:bins 30, :normalize :density})
  pj/lay-density
  (pj/options
   {:title "Simulated Distribution: Histogram + Density",
    :x-label "Value",
    :y-label "Density"})))


(deftest
 t246_l1182
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v245_l1174)))


(def
 v248_l1187
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed :weight {:color :feed})
  pj/lay-boxplot
  (pj/options
   {:title "Chick Weight by Feed Type",
    :x-label "Feed",
    :y-label "Weight (g)"})))


(deftest
 t249_l1194
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v248_l1187)))


(def
 v251_l1199
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
 t252_l1207
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v251_l1199)))


(def
 v254_l1214
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill {:color :sex})
  pj/lay-boxplot
  (pj/options
   {:title "Tips by Day and Gender (Grouped Boxplot)",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t255_l1221
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v254_l1214)))


(def
 v257_l1226
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :sepal-width {:color :species})
  pj/lay-violin
  (pj/options
   {:title "Iris Sepal Width (Violin)",
    :x-label "Species",
    :y-label "Sepal Width (cm)"})))


(deftest
 t258_l1233
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v257_l1226)))


(def
 v260_l1238
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
 t261_l1246
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v260_l1238)))


(def
 v263_l1254
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
 t264_l1262
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 244 (:points s)))))
   v263_l1254)))


(def
 v266_l1271
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
 t267_l1280
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v266_l1271)))


(def
 v269_l1287
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :smoker :total-bill {:color :smoker})
  pj/lay-violin
  (pj/options
   {:title "Total Bill by Smoking Status",
    :x-label "Smoker",
    :y-label "Total Bill ($)"})))


(deftest
 t270_l1294
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v269_l1287)))


(def
 v272_l1299
 (->
  (rdatasets/datasets-iris)
  (pj/pose :species :petal-length)
  pj/lay-ridgeline
  (pj/options
   {:title "Iris Petal Length by Species (Ridgeline)",
    :x-label "Species",
    :y-label "Petal Length (cm)"})))


(deftest
 t273_l1306
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v272_l1299)))


(def
 v275_l1311
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :color :price)
  pj/lay-ridgeline
  (pj/options
   {:title "Diamond Price by Color Grade (Ridgeline)",
    :x-label "Color",
    :y-label "Price ($)"})))


(deftest
 t276_l1318
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v275_l1311)))


(def
 v278_l1323
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
       [p1__88840#]
       (get {5 "May", 6 "Jun", 7 "Jul", 8 "Aug", 9 "Sep"} p1__88840#))
      (ds :month)))))))


(def
 v279_l1330
 (->
  airquality
  (pj/pose :month-name :ozone {:color :month-name})
  pj/lay-boxplot
  (pj/options
   {:title "New York Ozone by Month",
    :x-label "Month",
    :y-label "Ozone (ppb)"})))


(deftest
 t280_l1337
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v279_l1330)))


(def
 v282_l1345
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :class)
  pj/lay-bar
  (pj/options
   {:title "Vehicle Count by Class",
    :x-label "Class",
    :y-label "Count"})))


(deftest
 t283_l1352
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v282_l1345)))


(def
 v285_l1359
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :sex})
  (pj/options
   {:title "Tips Count by Day and Gender",
    :x-label "Day",
    :y-label "Count"})))


(deftest
 t286_l1365
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v285_l1359)))


(def
 v288_l1370
 (->
  {:country ["US" "China" "Japan" "Germany" "UK" "India" "France"],
   :gdp [21.4 14.7 5.1 3.8 2.8 2.7 2.6]}
  (pj/lay-bar :gdp :country)
  (pj/options
   {:title "GDP by Country (2019)",
    :x-label "GDP (Trillion $)",
    :y-label "Country"})))


(deftest
 t289_l1377
 (is ((fn [v] (= 7 (:polygons (pj/svg-summary v)))) v288_l1370)))


(def
 v291_l1384
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
 t292_l1392
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:polygons s)) (pos? (:lines s)))))
   v291_l1384)))


(def
 v294_l1399
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
 t295_l1408
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v294_l1399)))


(def
 v297_l1415
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
 t298_l1424
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v297_l1415)))


(def
 v300_l1434
 (->
  (rdatasets/datasets-faithful)
  (pj/pose :eruptions :waiting)
  pj/lay-density-2d
  (pj/options
   {:title "Old Faithful: 2D Density",
    :x-label "Eruption Duration (min)",
    :y-label "Waiting Time (min)"})))


(deftest
 t301_l1441
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v300_l1434)))


(def
 v303_l1446
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
 t304_l1454
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:visible-tiles s)))))
   v303_l1446)))


(def
 v306_l1461
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
 t307_l1469
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 272 (:points s)) (pos? (:lines s)))))
   v306_l1461)))


(def
 v309_l1476
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :petal-length)
  pj/lay-contour
  (pj/options
   {:title "Iris: Sepal vs Petal Length Contour",
    :x-label "Sepal Length",
    :y-label "Petal Length"})))


(deftest
 t310_l1483
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v309_l1476)))


(def
 v312_l1490
 (->
  (rdatasets/ggplot2-faithfuld)
  (pj/pose :eruptions :waiting {:fill :density})
  pj/lay-tile
  (pj/options
   {:title "Old Faithful: Pre-computed Density Heatmap",
    :x-label "Eruption Duration",
    :y-label "Waiting Time"})))


(deftest
 t313_l1497
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v312_l1490)))


(def
 v315_l1502
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
 t316_l1511
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:visible-tiles s)))))
   v315_l1502)))


(def
 v318_l1518
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-density-2d
  (pj/options
   {:title "MPG: Displacement vs Highway (Density)",
    :x-label "Displacement (L)",
    :y-label "Highway MPG"})))


(deftest
 t319_l1525
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v318_l1518)))


(def
 v321_l1530
 (->
  {:row (mapcat (fn* [p1__88841#] (repeat 6 p1__88841#)) (range 6)),
   :col (flatten (repeat 6 (range 6))),
   :value
   (map (fn* [p1__88842#] (Math/sin (* p1__88842# 0.5))) (range 36))}
  (pj/pose :col :row {:fill :value})
  pj/lay-tile
  (pj/options
   {:title "Synthetic Heatmap (sin wave)",
    :x-label "Column",
    :y-label "Row"})))


(deftest
 t322_l1539
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v321_l1530)))


(def
 v324_l1547
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
 t325_l1560
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v324_l1547)))


(def
 v327_l1567
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-summary :day :tip {:color :sex})
  (pj/options
   {:title "Mean Tip +/- SE by Day and Gender",
    :x-label "Day",
    :y-label "Tip ($)"})))


(deftest
 t328_l1573
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v327_l1567)))


(def
 v330_l1586
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose [[:date :unemploy] [:date :uempmed]])
  pj/lay-line
  (pj/options {:title "Unemployment: Total vs Median Duration"})))


(deftest
 t331_l1591
 (is ((fn [v] (>= (:lines (pj/svg-summary v)) 2)) v330_l1586)))


(def
 v333_l1596
 (->
  (rdatasets/ggplot2-economics)
  (pj/pose [[:date :unemploy] [:date :uempmed] [:date :psavert]])
  pj/lay-line
  (pj/options {:title "US Economic Indicators"})))


(deftest
 t334_l1601
 (is ((fn [v] (>= (:lines (pj/svg-summary v)) 3)) v333_l1596)))


(def
 v336_l1611
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
 t337_l1619
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v336_l1611)))


(def
 v339_l1629
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
 t340_l1640
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:lines s) 4))))
   v339_l1629)))


(def
 v342_l1647
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
 t343_l1656
 (is ((fn [v] (= 32 (:points (pj/svg-summary v)))) v342_l1647)))


(def
 v345_l1661
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
 t346_l1669
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:lines s)))))
   v345_l1661)))


(def
 v348_l1676
 (->
  airquality
  (pj/lay-line :rownames :ozone)
  (pj/lay-rule-h {:y-intercept 60})
  (pj/options
   {:title "NYC Ozone with Threshold at 60 ppb",
    :x-label "Observation",
    :y-label "Ozone (ppb)"})))


(deftest
 t349_l1683
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (and (pos? (:lines s)))))
   v348_l1676)))


(def
 v351_l1689
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
 t352_l1697
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v351_l1689)))


(def
 v354_l1705
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
 t355_l1713
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v354_l1705)))


(def
 v357_l1718
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet-grid :drv :year)
  (pj/options
   {:title "MPG: Drive Type x Model Year",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t358_l1726
 (is ((fn [v] (= 6 (:panels (pj/svg-summary v)))) v357_l1718)))


(def
 v360_l1735
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet-grid :drv :class)
  (pj/options
   {:title "MPG: Drive x Class",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t361_l1743
 (is ((fn [v] (pos? (:panels (pj/svg-summary v)))) v360_l1735)))


(def
 v363_l1748
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose :displ :hwy)
  pj/lay-point
  (pj/facet-grid nil :drv)
  (pj/options
   {:title "MPG: Column Facets by Drive Type",
    :x-label "Displacement",
    :y-label "Highway MPG"})))


(deftest
 t364_l1756
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v363_l1748)))


(def
 v366_l1761
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
 t367_l1769
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v366_l1761)))


(def
 v369_l1774
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
 t370_l1782
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 2 (:panels s)))))
   v369_l1774)))


(def
 v372_l1789
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
 t373_l1797
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (= 2 (:panels s)))))
   v372_l1789)))


(def
 v375_l1804
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
 t376_l1812
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v375_l1804)))


(def
 v378_l1817
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
 t379_l1826
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (= 3 (:lines s)) (= 3 (:panels s)))))
   v378_l1817)))


(def
 v381_l1834
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-boxplot
  (pj/facet-grid :time :smoker)
  (pj/options
   {:title "Tips: Day x Time x Smoker",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t382_l1842
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v381_l1834)))


(def
 v384_l1847
 (->
  (tc/select-rows
   (rdatasets/gapminder-gapminder)
   (fn* [p1__88843#] (= 2007 (:year p1__88843#))))
  (pj/pose :gdp-percap :life-exp)
  pj/lay-point
  (pj/scale :x :log)
  (pj/facet :continent)
  (pj/options
   {:title "Gapminder 2007 by Continent",
    :x-label "GDP per Capita (log)",
    :y-label "Life Expectancy"})))


(deftest
 t385_l1856
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v384_l1847)))


(def
 v387_l1861
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
 t388_l1870
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (= 180 (:points s)))))
   v387_l1861)))


(def
 v390_l1877
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
 t391_l1886
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v390_l1877)))


(def
 v393_l1896
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose (pj/cross [:mpg :hp :wt] [:mpg :hp :wt]))
  (pj/options {:title "Motor Trend Cars: 3x3 SPLOM"})))


(deftest
 t394_l1900
 (is ((fn [v] (= 9 (:panels (pj/svg-summary v)))) v393_l1896)))


(def
 v396_l1905
 (->
  (rdatasets/datasets-mtcars)
  (pj/pose (pj/cross [:mpg :wt] [:mpg :wt]))
  (pj/options {:title "MPG vs Weight: 2x2 SPLOM"})))


(deftest
 t397_l1909
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v396_l1905)))


(def
 v399_l1917
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
 t400_l1925
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v399_l1917)))


(def
 v402_l1930
 (->
  msleep
  (pj/lay-point :bodywt :sleep-total {:color :vore})
  (pj/scale :x :log)
  (pj/options
   {:title "Body Weight vs Sleep (log x-axis)",
    :x-label "Body Weight (kg, log)",
    :y-label "Total Sleep (hours)"})))


(deftest
 t403_l1937
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v402_l1930)))


(def
 v405_l1945
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/pose :cut {:color :color})
  (pj/lay-bar {:position :stack})
  (pj/options
   {:title "Diamonds by Cut and Color (Stacked)",
    :x-label "Cut",
    :y-label "Count"})))


(deftest
 t406_l1952
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v405_l1945)))


(def
 v408_l1959
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :day :total-bill)
  pj/lay-bar
  pj/lay-point
  (pj/options
   {:title "Tips: Bar Count with Individual Points",
    :x-label "Day",
    :y-label "Total Bill ($)"})))


(deftest
 t409_l1967
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:polygons s)) (pos? (:points s)))))
   v408_l1959)))


(def
 v411_l1980
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-density-2d
  (pj/options
   {:title "Iris: 2D Density by Species",
    :x-label "Sepal Length",
    :y-label "Sepal Width"})))


(deftest
 t412_l1987
 (is ((fn [v] (pos? (:visible-tiles (pj/svg-summary v)))) v411_l1980)))


(def
 v414_l1992
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
 t415_l2001
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:points s)) (pos? (:lines s)))))
   v414_l1992)))


(def
 v417_l2013
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
 t418_l2024
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v417_l2013)))
