;; # Cookbook
;;
;; Practical plotting recipes -- how to combine marks, overlay stats,
;; and build finished charts.

(ns plotje-book.cookbook
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Fastmath -- random number generation
   [fastmath.random :as rng]
   ;; Java-time -- idiomatic date/time construction
   [java-time.api :as jt]
   ;; Rdatasets -- additional datasets beyond the shared ones
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## Quick Recipes

;; ### Boxplot with jittered points
;;
;; Overlay raw observations on a boxplot summary. The auto-[jitter](https://en.wikipedia.org/wiki/Jitter)
;; detects the categorical axis and constrains points to the band width.

(-> (rdatasets/datasets-iris)
    (pj/lay-boxplot :species :sepal-length)
    (pj/lay-point {:jitter true :alpha 0.3}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:points s))
                                (= 3 (:polygons s)))))])

;; ### Histogram with density overlay
;;
;; Normalize the histogram to density scale so it is comparable with the KDE (kernel density estimation) curve.

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length {:normalize :density :alpha 0.5})
    pj/lay-density)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ### Scatter with regression lines
;;
;; Fit a linear regression per group to reveal trends across species.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    (pj/lay-point {:alpha 0.6})
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; ### Violin with jittered points
;;
;; Show the density shape and every observation together.

(-> (rdatasets/datasets-iris)
    (pj/lay-violin :species :petal-width {:alpha 0.3})
    (pj/lay-point {:jitter true :alpha 0.4}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:polygons s)))))])

;; ### Time series with multiple layers
;;
;; Combine area, line, and points. Date columns are detected
;; automatically -- ticks snap to calendar boundaries.

(def ts-dates (take 52 (jt/iterate jt/plus (jt/local-date 2020 1 6) (jt/weeks 1))))

(def ts-ds {:date ts-dates
            :value (map #(+ 100.0 (* 30.0 (Math/sin (* (double %) 0.12))))
                        (range 52))})

(-> ts-ds
    (pj/lay-area :date :value {:alpha 0.2})
    pj/lay-line
    (pj/lay-point {:alpha 0.5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 52 (:points s))
                                (= 1 (:lines s))
                                (= 1 (:polygons s)))))])

;; ### Faceted comparison
;;
;; Split a scatter plot by species to compare patterns side by side.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:panels s))))])

;; ### Gantt schedule
;;
;; A horizontal interval bar per task, with the bar's left edge at
;; the start date and the right edge at the end date. The
;; [Timelines](./plotje_book.timelines.html) chapter has more
;; variations (color by team, vertical orientation, presidential
;; terms); this is the minimal recipe.

(-> {:task  ["Design" "Build" "Test" "Ship"]
     :start [#inst "2024-01-01" #inst "2024-02-01"
             #inst "2024-03-15" #inst "2024-04-15"]
     :end   [#inst "2024-02-01" #inst "2024-03-20"
             #inst "2024-04-15" #inst "2024-05-01"]}
    (pj/lay-interval-h :start :task {:x-end :end})
    (pj/options {:title "Project schedule"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:polygons s)))))])

;; ### Ridgeline with color
;;
;; Compare distribution shapes across categories with overlapping
;; density curves. Grid lines at each baseline aid comparison.

(-> (rdatasets/datasets-iris)
    (pj/lay-ridgeline :species :sepal-length {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:polygons s))
                                (= 3 (:lines s)))))])

;; ### Stacked bars (proportions)
;;
;; Show the proportion of each species per island using 100% stacked bars.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:position :fill :color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Multi-Layer Compositions

;; ### Overall regression with per-group points
;;
;; Color points by group, but fit a single overall regression line.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-smooth {:stat :linear-model :color nil}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; ### Different data per layer
;;
;; Each `lay-*` accepts `{:data ...}` to override the pose-level
;; dataset. This lets you overlay marks from two different tables --
;; ggplot2's `geom_line(data=df2) + geom_point(data=df1)` pattern.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:alpha 0.3})
    (pj/lay-point {:data {:sepal-length [5.0 6.5]
                          :sepal-width [3.5 3.0]}
                   :x :sepal-length :y :sepal-width
                   :color "red" :size 6}))

(kind/test-last [(fn [v] (= 152 (:points (pj/svg-summary v))))])

;; ### Points with [error bars](https://en.wikipedia.org/wiki/Error_bar)
;;
;; Combining `point` and `errorbar` layers shows measurements
;; with uncertainty.

(def experiment
  {:condition ["A" "B" "C" "D"]
   :mean [10.0 15.0 12.0 18.0]
   :ci_lo [8.0 12.0 9.5 15.5]
   :ci_hi [12.0 18.0 14.5 20.5]})

(-> experiment
    (pj/lay-point :condition :mean {:size 5})
    (pj/lay-errorbar {:y-min :ci_lo :y-max :ci_hi}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 12 (:lines s)))))])

;; ### Lollipop with error bars
;;
;; Composing lollipop stems with error bars.

(-> experiment
    (pj/lay-lollipop :condition :mean)
    (pj/lay-errorbar {:y-min :ci_lo :y-max :ci_hi}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 16 (:lines s)))))])

;; ### Summary (mean +/- SE) with raw data
;;
;; The `summary` layer type computes mean and SE (standard error) per category.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-length {:alpha 0.3 :jitter 5})
    (pj/lay-summary {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 153 (:points s))
                                (= 3 (:lines s)))))])

;; ### Tipping behavior
;;
;; Scatter + per-group regression to compare smoker tipping patterns.

(-> (rdatasets/reshape2-tips)
    (pj/pose :total-bill :tip {:color :smoker})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/options {:title "Tipping Behavior"
                 :x-label "Total Bill ($)"
                 :y-label "Tip ($)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (pos? (:points s))
                                (= 2 (:lines s))
                                (some #{"Tipping Behavior"} (:texts s)))))])

;; ## More Recipes

;; ### Confidence band

;; A scatter plot with per-group linear regressions and 95%
;; confidence bands.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    (pj/lay-point {:alpha 0.5})
    (pj/lay-smooth {:stat :linear-model :confidence-band true})
    (pj/options {:title "Sepal Regression with Confidence Bands"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:points s))
                                (pos? (:lines s)))))])

;; ### Stacked vs grouped bars

;; Side-by-side comparison: default dodged bars vs stacked bars.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :sex})
    (pj/options {:title "Dodged Bars (default)"}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:position :stack :color :sex})
    (pj/options {:title "Stacked Bars"}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ### Step line

;; A step plot for discrete time series data -- useful when values
;; hold constant between observations.

(def daily-temps
  {:day (range 1 15)
   :temp [12 14 14 16 18 17 15 13 14 16 19 21 20 18]})

(-> daily-temps
    (pj/lay-step :day :temp {:color "#2196F3"})
    (pj/lay-point {:color "#2196F3" :size 3})
    (pj/options {:title "Daily Temperature (Step)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (pos? (:lines s))
                                (pos? (:points s))
                                (contains? (:colors s) "rgb(33,150,243)")
                                (contains? (:sizes s) 3.0))))])

;; ### Contour + scatter

;; Density contour lines overlaid on a scatter plot -- reveals
;; high-density regions in a point cloud.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species :alpha 0.4})
    (pj/lay-contour {:levels 5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (pos? (:points s))
                                (pos? (:lines s)))))])

;; ### Label marks

;; Annotate specific data points with text labels.

(def top5
  (-> (rdatasets/datasets-iris)
      (tc/order-by :sepal-length :desc)
      (tc/head 5)))

(-> top5
    (pj/lay-point :sepal-length :sepal-width {:size 5})
    (pj/lay-label {:text :species :nudge-y 0.15}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (pos? (:points s))
                                (some #(= "virginica" %) (:texts s)))))])

;; ### Value labels inside bars
;;
;; To print a value on each bar, overlay a `lay-text` layer added after
;; the bar so it paints on top. Use `:align-x :right` so the label's
;; right edge sits at the bar's end, tucking the text inside the fill
;; (extending leftward) rather than spilling past the bar.

(def species-share
  {:species ["setosa" "versicolor" "virginica"]
   :percent [33.3 33.3 33.3]})

(-> species-share
    (pj/lay-bar :species :percent {:color "#a6cee3"})
    (pj/lay-text :species :percent {:text :percent :align-x :right})
    (pj/coord :flip))

(kind/test-last
 [(fn [fr]
    (let [text-layer (->> fr pj/plan :panels first :layers
                          (filter #(= :text (:mark %)))
                          first)]
      (= :right (-> text-layer :style :align-x))))])

;; ### Labelling counted bars with their counts
;;
;; `pj/lay-bar` with only a category column counts the rows in each
;; category, and there is no column holding those counts to point a
;; label at. `{:stat :count}` on the label layer reads the same counted
;; values the bars are drawn from, so each bar is labelled with its own
;; height and the two cannot disagree.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/lay-label {:stat :count :align-x :center}))

(kind/test-last
 [(fn [fr]
    (= ["50" "50" "50"]
       (->> fr pj/plan :panels first :layers
            (filter #(= :text (:mark %)))
            first :groups first :labels)))])

;; ### Labels on grouped bars
;;
;; Bars grouped by color are dodged side by side within each category. A
;; label layer grouped by that same column is dodged along with them, so
;; each label sits over the bar it names rather than at the middle of the
;; category. Group the labels with `:group`, which is the label layer's
;; way of naming the column the bars use for `:color`.

(-> {:sex ["male" "male" "female" "female"]
     :species ["cat" "dog" "cat" "dog"]
     :percent [21 17 9 14]}
    (pj/pose :sex :percent)
    (pj/lay-bar {:color :species})
    (pj/lay-label {:text :percent :group :species :align-x :center}))

(kind/test-last
 [(fn [fr]
    (let [layers (->> fr pj/plan :panels first :layers)
          groups (fn [mark] (->> layers
                                 (filter #(= mark (:mark %)))
                                 first :groups
                                 (mapv (juxt :label :dodge-idx))))]
      (= (groups :rect) (groups :text))))])

;; ### Custom palette map

;; Assign specific colors to each category using a palette map.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette {:setosa "#E91E63"
                           :versicolor "#4CAF50"
                           :virginica "#2196F3"}
                 :title "Custom Palette Map"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:points s)))))])

;; ### Fixed aspect ratio
;;
;; Use `pj/coord :fixed` so one unit on x equals one unit on y.
;; This makes the plot square when x and y have equal ranges.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/coord :fixed)
    (pj/options {:title "Fixed Aspect Ratio"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (pos? (:points s))
                                (= 3 (:lines s)))))])

;; ### Diverging color scale
;;
;; Use `:color-scale :diverging` with `:color-midpoint` to center
;; a red-white-blue gradient on a meaningful value (e.g., zero).

(-> {:x (range 20)
     :y (map #(Math/sin (/ % 3.0)) (range 20))
     :change (map #(- % 10) (range 20))}
    (pj/lay-point :x :y {:color :change})
    (pj/options {:color-scale :diverging
                 :color-midpoint 0
                 :title "Diverging Color Scale"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 20 (:points s)))))])

;; ### LOESS (local regression) [confidence ribbon](https://en.wikipedia.org/wiki/Confidence_band)
;;
;; Add `{:confidence-band true}` to a LOESS smoother for a bootstrap confidence band.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:confidence-band true})
    (pj/options {:title "LOESS with 95% CI"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s))
                                (= 3 (:polygons s)))))])

;; ### Multi-plot dashboard
;;
;; Use `pj/arrange` to combine independent plots into a grid layout.

(def iris-sepal
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/options {:title "Sepal" :width 300 :height 250})))

(def iris-petal
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width {:color :species})
      (pj/options {:title "Petal" :width 300 :height 250})))

(pj/arrange [iris-sepal iris-petal]
            {:title "Iris Dashboard" :cols 2})

(kind/test-last [(fn [v] (and (pj/pose? v)
                              (= "Iris Dashboard" (-> v :opts :title))))])

;; ### Labeled scatter

;; Combine points with text labels, using nudge to offset text from data points.

(def top-cities
  {:city ["Tokyo" "Delhi" "Shanghai" "São Paulo" "Mumbai"]
   :population [37.4 32.9 29.2 22.4 21.7]
   :area [2194 1484 6341 1521 603]})

(-> top-cities
    (pj/lay-point :area :population)
    (pj/lay-text {:text :city :nudge-y 1.0})
    (pj/options {:title "Population vs Area"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 5 (:points s))
                                (every? (set (:texts s)) ["Tokyo" "Delhi"]))))])

;; ## Annotated Charts
;;
;; These recipes place text and marks that explain a plot: names on the
;; lines instead of a legend, a note beside the shape it describes, a
;; caption in a corner. The options they lean on -- offsets, values for
;; `:x` and `:y`, and `:in :drawing-area` -- are taught in
;; [Placing Marks](./plotje_book.placing_marks.html).
;;
;; Two examples to look at first:
;;
;; - [*Seeing With Fresh Eyes*](https://www.edwardtufte.com/book/seeing-with-fresh-eyes-meaning-space-data-truth/),
;;   Edward Tufte, 2020 -- a spread comparing a chart that names its
;;   series in a legend with one that names them on the lines
;; - [the FT coronavirus trajectory charts](https://ig.ft.com/coronavirus-chart/),
;;   John Burn-Murdoch, Financial Times -- direct labels, callouts with
;;   leader lines, a dashed reference slope, and a de-emphasised
;;   background of series that are shown but not discussed
;;
;; The recipes below use different data and reproduce neither graphic.
;;
;; The data is free material from [Gapminder](https://www.gapminder.org/),
;; CC-BY, reached here through `rdatasets`.

;; ### Reference lines and bands
;;
;; The simplest annotations have their own layer types. A rule draws a
;; line across the panel at one value; a band shades the region between
;; two. Both take their positions as values rather than columns, and
;; `:alpha` controls a band's opacity.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-rule-h {:y-intercept 3.0})
    (pj/lay-band-v {:x-min 5.5 :x-max 6.5 :alpha 0.3}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; ### Labels on the lines instead of a legend
;;
;; A legend puts the series names in one corner and the series in
;; another. A label at the end of each line puts the name beside the
;; line it names. Five countries, first with a legend:

(def life-tracks
  (-> (rdatasets/gapminder-gapminder)
      (tc/select-rows #(#{"Rwanda" "Cambodia" "China" "Japan" "Botswana"}
                        (:country %)))
      (tc/select-columns [:country :year :life-exp])))

(-> life-tracks
    (pj/lay-line :year :life-exp {:color :country})
    (pj/options {:title "Life expectancy at birth"
                 :width 620 :height 380}))

(kind/test-last
 [(fn [v] (= 5 (:lines (pj/svg-summary v))))])

;; Now with the names on the lines. The label layer draws from the last
;; year alone -- one row per country -- and takes its color from the
;; same `:country` column, so each name matches its line. `:offset-x`
;; moves the text clear of the line's end by a few drawing units. A
;; nudge would not serve here: the gap is a distance on the page, not a
;; number of years.

(-> life-tracks
    (pj/lay-line :year :life-exp {:color :country})
    (pj/lay-text {:data (tc/select-rows life-tracks #(= 2007 (:year %)))
                  :x :year :y :life-exp :text :country :color :country
                  :offset-x 8})
    (pj/options {:title "Life expectancy at birth"
                 :width 620 :height 380
                 :legend-position :none}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 5 (:lines s))
                 (every? (set (:texts s))
                         ["Rwanda" "Cambodia" "China" "Japan" "Botswana"]))))])

;; The axis grew to the right to hold the labels. A numeric domain is
;; widened to fit its text marks, and the offset counts toward the width
;; to fit.

;; ### Callout with a leader line
;;
;; One country, with four layers doing the annotating: a marker on the
;; point being discussed, a dotted leader from the note to the marker,
;; the note itself at a position given as values rather than columns,
;; and a caption placed on the panel with `:in :drawing-area`.

(-> (rdatasets/gapminder-gapminder)
    (tc/select-rows #(= "Rwanda" (:country %)))
    (pj/lay-line :year :life-exp {:color "#4477aa"})
    (pj/lay-point {:data {:year [1992] :life-exp [23.599]}
                   :x :year :y :life-exp :color "#cc3311" :size 6})
    (pj/lay-line {:data {:year [1972 1990] :life-exp [30 24.5]}
                  :x :year :y :life-exp
                  :color "#777777" :stroke-dash :dotted})
    (pj/lay-text {:x 1971 :y 30 :align-x :right :offset-x -4
                  :color "#333333"
                  :text "life expectancy fell to 23.6 years in 1992"})
    (pj/lay-text {:in :drawing-area :x 10 :y 8 :color "#777777"
                  :text "Rwanda, 1952-2007"})
    (pj/options {:width 640 :height 400
                 :y-label "life expectancy at birth"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (every? (set (:texts s))
                         ["life expectancy fell to 23.6 years in 1992"
                          "Rwanda, 1952-2007"])
                 (= 1 (:points s)))))])

;; The two text layers are placed in different spaces on purpose. The
;; callout's position is a year and a life expectancy, so it tracks the
;; shape it describes when the axis changes. The caption's position is
;; in drawing units from the corner of the panel background, so it does
;; not move with the data.

;; ### A few named series over many pale ones
;;
;; The two recipes above each showed a handful of series. Every series
;; can be drawn while only some are named, with the rest in one pale
;; color. The Financial Times charts do this with the countries that
;; appear but are not discussed.
;;
;; All the pale series are one layer. `pj/lay-line` with a `:group` and
;; a literal `:color` draws one line per country, every line in that
;; color.
;;
;; Pick the three to name from the data rather than by hand -- the
;; country that ends highest, the one that gained most,
;; and the one with the sharpest single fall:

(def life-history
  (-> (rdatasets/gapminder-gapminder)
      (tc/select-columns [:country :year :life-exp])))

(def ends-highest
  (-> life-history
      (tc/select-rows #(= 2007 (:year %)))
      (tc/order-by :life-exp :desc)
      (tc/rows :as-maps)
      first :country))

(def gained-most
  (-> life-history
      (tc/group-by :country)
      (tc/aggregate {:gain (fn [ds] (- (reduce max (:life-exp ds))
                                       (reduce min (:life-exp ds))))})
      (tc/order-by :gain :desc)
      (tc/rows :as-maps)
      first :$group-name))

;; The sharpest fall, as a country and as the size of the drop between
;; two consecutive readings:

(def sharpest-fall
  (-> life-history
      (tc/order-by [:country :year])
      (tc/group-by :country)
      (tc/aggregate {:fall (fn [ds]
                             (let [ys (vec (:life-exp ds))]
                               (reduce min 0 (map - (rest ys) ys))))})
      (tc/order-by :fall)
      (tc/rows :as-maps)
      first))

[ends-highest gained-most sharpest-fall]

(kind/test-last
 [(fn [[a b c]] (and (= "Japan" a)
                     (= "Oman" b)
                     (= "Rwanda" (:$group-name c))
                     (< -21 (:fall c) -20)))])

;; Now the chart. Every country in pale grey, those three in color with
;; their names at the line ends, a callout whose wording is built from
;; the number computed above, and a caption placed on the panel.

(let [named #{ends-highest gained-most (:$group-name sharpest-fall)}
      chosen (tc/select-rows life-history #(named (:country %)))]
  (-> life-history
      (pj/lay-line :year :life-exp {:group :country :color "#d0d0d0"})
      (pj/lay-line {:data chosen :x :year :y :life-exp :color :country})
      (pj/lay-text {:data (tc/select-rows chosen #(= 2007 (:year %)))
                    :x :year :y :life-exp :text :country
                    :color :country :offset-x 8})
      (pj/lay-line {:data {:year [1972 1989] :life-exp [31 25]}
                    :x :year :y :life-exp
                    :color "#777777" :stroke-dash :dotted})
      (pj/lay-text {:x 1971 :y 31 :align-x :right :offset-x -4
                    :color "#333333"
                    :text (format "%s, 1992: a fall of %.0f years in one step"
                                  (:$group-name sharpest-fall)
                                  (- (:fall sharpest-fall)))})
      (pj/lay-text {:in :drawing-area :x 10 :y 8 :color "#888888"
                    :text (format "%d countries, 1952-2007"
                                  (count (distinct (:country life-history))))})
      (pj/options {:width 760 :height 430 :legend-position :none
                   :y-label "life expectancy at birth"})))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (every? (set (:texts s)) ["Japan" "Oman" "Rwanda"])
                 (some #(re-find #"^Rwanda, 1992: a fall of 20 years" %)
                       (:texts s))
                 (some #(= "142 countries, 1952-2007" %) (:texts s)))))])

;; Every number in the annotations was computed from the data the chart
;; draws, so refreshing the data updates the wording with it.

;; ## Simulated Data
;;
;; Generate data from a known model and verify the regression recovers it.

(let [r (rng/rng :jdk 77)
      xs (range 0 10 0.5)
      ys (map #(+ (* 3 %)
                  5
                  (* 2 (- (rng/drandom r) 0.5)))
              xs)]
  (-> {:x xs :y ys}
      (pj/lay-point :x :y)
      (pj/lay-smooth {:stat :linear-model})
      (pj/options {:title "Simulated: y = 3x + 5 + noise"})))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 20 (:points s))
                                (= 1 (:lines s))
                                (some #{"Simulated: y = 3x + 5 + noise"} (:texts s)))))])

;; ## Analytical Walkthroughs

;; ### Palmer Penguins

;; Bill dimensions separate the three species clearly.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
    (pj/options {:title "Palmer Penguins: Bill Dimensions"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 342 (:points s)))))])

;; Per-species regression reveals different slopes.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/pose :bill-length-mm :bill-depth-mm {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/options {:title "Bill Length vs Depth with Regression"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 342 (:points s))
                                (= 3 (:lines s)))))])

;; Without grouping, the overall trend appears negative -- an example
;; of Simpson's paradox.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-point :bill-length-mm :bill-depth-mm {:color :species})
    (pj/lay-smooth {:stat :linear-model :color nil})
    (pj/options {:title "Simpson's Paradox: Overall vs Per-Group Trend"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 342 (:points s))
                                (= 1 (:lines s)))))])

;; Species distribution across islands.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:color :species})
    (pj/options {:title "Species by Island"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; Flipper length vs body mass -- a strong positive correlation.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/pose :flipper-length-mm :body-mass-g {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/options {:title "Flipper Length vs Body Mass"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 342 (:points s))
                                (= 3 (:lines s)))))])

;; Body mass distribution by species.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-histogram :body-mass-g {:color :species})
    (pj/options {:title "Body Mass Distribution"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ### Tips

;; Tipping behavior: smokers vs non-smokers.

(-> (rdatasets/reshape2-tips)
    (pj/pose :total-bill :tip {:color :smoker})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/options {:title "Tipping: Smokers vs Non-Smokers"
                 :x-label "Total Bill ($)" :y-label "Tip ($)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 244 (:points s))
                                (= 2 (:lines s)))))])

;; Tip amounts by day, colored by meal time.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :time})
    (pj/options {:title "Visits by Day and Meal Time"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; Stacked view of the same data.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:position :stack :color :time})
    (pj/options {:title "Visits by Day (Stacked)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; Horizontal bar chart of party sizes.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :sex})
    (pj/coord :flip)
    (pj/options {:title "Day by Gender (Horizontal)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ### MPG

;; Engine displacement vs highway fuel efficiency, colored by vehicle class.

(-> (rdatasets/ggplot2-mpg)
    (pj/pose :displ :hwy {:color :class})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/options {:title "Displacement vs Highway MPG by Class"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 234 (:points s))
                                (pos? (:lines s)))))])

;; Displacement vs city MPG -- a similar negative correlation.

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :cty {:color :drv})
    (pj/options {:title "Engine Displacement vs City Fuel Efficiency"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 234 (:points s)))))])

;; Count of cars by drive type.

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-bar :drv)
    (pj/options {:title "Cars by Drive Type"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Large Datasets and Raster Output
;;
;; By default Plotje renders to SVG, which produces scalable charts
;; that stay sharp at any zoom. But when a plot has tens of thousands
;; of points, the browser
;; must parse and layout a huge SVG DOM. For example, the full diamonds
;; dataset (53,940 rows) produces an 11 MB SVG file.
;;
;; Setting `:format :bufimg` renders the plot to a
;; `java.awt.image.BufferedImage` via membrane's Java2D backend instead
;; of SVG. For plots with many thousands of points, the raster output
;; is substantially smaller than the equivalent SVG.

;; ### SVG (default)

;; This is the default SVG output for a smaller subset:

(-> (rdatasets/ggplot2-diamonds)
    (tc/head 500)
    (pj/lay-point :carat :price {:color :cut})
    (pj/options {:title "Diamonds (500 rows, SVG)"}))

(kind/test-last [(fn [v] (= 500 (:points (pj/svg-summary v))))])

;; ### BufferedImage output
;;
;; With `:format :bufimg`, the full dataset renders as a raster image
;; in the notebook:

(-> (rdatasets/ggplot2-diamonds)
    (pj/lay-point :carat :price {:color :cut :alpha 0.3})
    (pj/options {:title "Diamonds (53,940 rows, BufferedImage)"
                 :format :bufimg}))

(kind/test-last [(fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))])

;; ### Raster text sits where SVG text sits
;;
;; Both paths draw the same plan, so choosing raster changes how the marks
;; are stored, not where anything lands. That matters most for text, since
;; a tick label is placed relative to its tick rather than at a coordinate
;; of its own: a y tick label is right-aligned against the axis, an x tick
;; label centers on its tick, and `:x-tick-angle` turns each one about its
;; own origin.
;;
;; One pose exercising all three, first as SVG:

(def quarterly-revenue
  (-> {:quarter ["Q1 2024" "Q2 2024" "Q3 2024" "Q4 2024"]
       :revenue [1250000 1480000 1310000 1720000]}
      (pj/lay-bar :quarter :revenue)
      (pj/options {:x-tick-angle -45
                   :y-label "revenue in US dollars"
                   :thousands-separator ","})))

quarterly-revenue

(kind/test-last
 [(fn [v] (.contains ^String (pr-str (pj/plot v)) "rotate(-45"))])

;; and then the same pose through the raster path -- same slanted tick
;; labels, same grouped digits right-aligned against the axis, same axis
;; title turned through a quarter turn and printed in full:

(pj/options quarterly-revenue {:format :bufimg})

(kind/test-last
 [(fn [v] (instance? java.awt.image.BufferedImage (pj/plot v)))])

;; ### Saving to PNG
;;
;; Use `pj/save` with a `.png` path to write a raster image to disk.
;; The format is inferred from the extension:

(let [path (str (java.io.File/createTempFile "plotje-diamonds" ".png"))]
  (-> (rdatasets/ggplot2-diamonds)
      (pj/lay-point :carat :price {:color :cut})
      (pj/save path))
  ;; Read the first eight bytes and check for PNG magic.
  (with-open [in (java.io.FileInputStream. path)]
    (let [bs (byte-array 8)]
      (.read in bs)
      (mapv #(bit-and ^int % 0xFF) (vec bs)))))

(kind/test-last [(fn [bs] (= [137 80 78 71 13 10 26 10] bs))])

;; The same call with an explicit `{:format :png}` makes the format
;; choice unambiguous, useful when the path is built dynamically:

;; ```clojure
;; (-> (rdatasets/ggplot2-diamonds)
;;     (pj/lay-point :carat :price {:color :cut})
;;     (pj/save out-path {:format :png}))
;; ```

;; ### Two vocabularies: plot return type vs save file format
;;
;; The two paths above use the same `:format` keyword for different
;; jobs. `pj/plot` names a JVM return type:
;;
;; - `:svg` -- hiccup
;; - `:bufimg` -- a Java2D `BufferedImage`
;;
;; `pj/save` names the file format:
;;
;; - `:svg` -- SVG file
;; - `:png` -- PNG file
;;
;; A pose's `:opts {:format ...}` flows into both contexts. If you
;; pin `:format :bufimg` on a pose so the notebook renders raster,
;; saving that pose still produces a PNG file -- the save path
;; reinterprets the pose-level `:bufimg` as `:png` because what is
;; written to disk is a PNG.

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- the mapping, scope, and identity rules behind these recipes
;; - [**Composition**](./plotje_book.composition.html) -- composite poses for multi-panel layouts

;; ## What's Next
;;
;; - [**Configuration**](./plotje_book.configuration.html) -- control dimensions, palettes, and themes at every scope
;; - [**Customization**](./plotje_book.customization.html) -- titles, palettes, themes, and mark styling
;; - [**Placing Marks**](./plotje_book.placing_marks.html) -- the placement options the annotation recipes use
