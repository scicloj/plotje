;; # Distributions
;;
;; [Histograms](https://en.wikipedia.org/wiki/Histogram), [density](https://en.wikipedia.org/wiki/Kernel_density_estimation) plots, [boxplots](https://en.wikipedia.org/wiki/Box_plot), [violins](https://en.wikipedia.org/wiki/Violin_plot), and ridgelines
;; for exploring the shape and spread of data.

(ns plotje-book.distributions
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Histogram
;;
;; Distribution of sepal length across all species.

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Colored Histogram
;;
;; Split by species -- each group gets its own color.

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length {:color :species}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Petal Width Histogram
;;
;; Petal width has a bimodal distribution.

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :petal-width))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Histogram with Custom Title

(-> (rdatasets/reshape2-tips)
    (pj/lay-histogram :total-bill)
    (pj/options {:title "Distribution of Total Bill"
                 :x-label "Amount ($)"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s))
                 (some #(= "Distribution of Total Bill" %) (:texts s)))))])

;; ## Density-Normalized Histogram
;;
;; Pass `{:normalize :density}` so the y-axis shows probability
;; density instead of raw counts. This makes the histogram directly
;; comparable with a density curve overlay.

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length {:normalize :density :alpha 0.5})
    pj/lay-density)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Log-Scale Histogram
;;
;; When bin counts span orders of magnitude, the smallest bars
;; disappear next to the largest on a linear y-axis. A log y-scale
;; lets every bar register. The data below doubles the count of
;; each successive bin (1, 2, 4, ..., 512); on a log axis the
;; doubling shows as a uniform staircase -- each bar a fixed step
;; above the previous, the same step every time.

;; The lower bound under log comes from the smallest positive bin
;; count, not from the visual zero baseline -- log scales have no
;; zero. Empty bins emit no bar and do not pull the axis down.

(-> {:x (mapcat (fn [i] (repeat (long (Math/pow 2 i)) i)) (range 10))}
    (pj/lay-histogram {:bins 10})
    (pj/scale :y :log)
    (pj/options {:title "Log Y on Histogram"}))

(kind/test-last
 [(fn [v]
    (let [panel (-> v pj/plan :panels first)
          [lo hi] (:y-domain panel)]
      (and (= :log (:type (:y-scale panel)))
           (pos? lo)
           (< lo 1.0)
           (< 500.0 hi 2000.0))))])

;; ## Density Plot
;;
;; A smooth curve estimating the probability density function.
;; Less sensitive to bin width than histograms.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s)))))])

;; ## Grouped Density
;;
;; Per-species density curves with automatic color mapping.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:color :species}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 3 (:polygons s)))))])

;; ## Density with Custom Bandwidth
;;
;; A narrow bandwidth reveals more detail; a wide bandwidth smooths more.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:bandwidth 0.3}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s)))))])

;; ## Density with Fill and Outline
;;
;; By default a density fills the area under the curve in the mapped
;; color. To make the curve's shape read more crisply, add an outline
;; with `:stroke`. The fill still comes from `:color`, and the outline
;; traces only the top curve, not the baseline. `:stroke-width` sets the
;; outline thickness.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:color "lightblue" :stroke "black" :stroke-width 2}))

;; One filled polygon plus one outline line, and the render carries both
;; the light-blue fill and the black outline color.

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s))
                 (= 1 (:lines s))
                 (contains? (:colors s) "rgb(173,216,230)")
                 (contains? (:colors s) "rgb(0,0,0)"))))])

;; ## Rug
;;
;; A rug shows the raw data positions as short tick marks along the
;; axis. Layered with a density curve, it shows the smooth shape and
;; the underlying observations together.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length)
    pj/lay-rug)

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s))
                 (= 150 (:lines s)))))])

;; ## Strip Plot (Jitter)
;;
;; When plotting a numeric column against a categorical column,
;; points stack on the same band positions. `:jitter true` spreads
;; them with small random offsets along the categorical axis.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-width {:jitter true}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; Pass a number to control the jitter amount in drawing units.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-width {:jitter 10 :alpha 0.5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; ## Boxplot
;;
;; Median, quartiles, whiskers at 1.5 times the IQR (interquartile range), and outlier points.

(-> (rdatasets/datasets-iris)
    (pj/lay-boxplot :species :sepal-width))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 3 (:polygons s))
                 (pos? (:lines s)))))])

;; The 1.5-times-IQR claim is structural: each whisker stays within the
;; Tukey fence `[Q1 - 1.5*IQR, Q3 + 1.5*IQR]`, and every outlier
;; falls outside it.

(kind/test-last
 [(fn [_]
    (let [plan (-> (rdatasets/datasets-iris)
                   (pj/lay-boxplot :species :sepal-width)
                   pj/plan)
          box-layer (first (filter #(= :boxplot (:mark %))
                                   (:layers (first (:panels plan)))))
          results (mapv (fn [{:keys [q1 q3 whisker-lo whisker-hi outliers]}]
                          (let [iqr (- q3 q1)
                                lo-fence (- q1 (* 1.5 iqr))
                                hi-fence (+ q3 (* 1.5 iqr))]
                            {:whisker-lo-in-fence (>= whisker-lo lo-fence)
                             :whisker-hi-in-fence (<= whisker-hi hi-fence)
                             :outliers-outside-fence
                             (every? (fn [o] (or (< o lo-fence) (> o hi-fence)))
                                     outliers)}))
                        (:boxes box-layer))]
      (and (= 3 (count results))
           (every? (fn [r] (and (:whisker-lo-in-fence r)
                                (:whisker-hi-in-fence r)
                                (:outliers-outside-fence r)))
                   results))))])

;; ## Grouped Boxplot
;;
;; Side-by-side boxplots colored by a grouping variable.

(-> (rdatasets/reshape2-tips)
    (pj/lay-boxplot :day :total-bill {:color :smoker}))

;; Each color group gets a distinct dodge offset, visible as
;; side-by-side boxes within each day.

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plan (pj/plan (-> (rdatasets/reshape2-tips)
                            (pj/lay-boxplot :day :total-bill {:color :smoker})))
          box-layer (first (filter #(= :boxplot (:mark %))
                                   (:layers (first (:panels plan)))))]
      (and (= 1 (:panels s))
           (= 8 (:polygons s))
           (pos? (:lines s))
           (= 2 (count (:color-categories box-layer))))))])

;; ## Horizontal Boxplot
;;
;; Flipped coordinate for horizontal orientation.

(-> (rdatasets/datasets-iris)
    (pj/lay-boxplot :species :sepal-width)
    (pj/coord :flip))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 3 (:polygons s))
                 (pos? (:lines s)))))])

;; ## Violin Plot
;;
;; A violin shows the full density shape per category -- more
;; informative than a boxplot for multimodal distributions.

(-> (rdatasets/reshape2-tips)
    (pj/lay-violin :day :total-bill))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 4 (:polygons s)))))])

;; ## Grouped Violin
;;
;; Color splits each category into side-by-side violins.

(-> (rdatasets/reshape2-tips)
    (pj/lay-violin :day :total-bill {:color :smoker}))

;; Each color group gets a distinct dodge offset, visible as
;; side-by-side violins within each day.

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plan (pj/plan (-> (rdatasets/reshape2-tips)
                            (pj/lay-violin :day :total-bill {:color :smoker})))
          viol-layer (first (filter #(= :violin (:mark %))
                                    (:layers (first (:panels plan)))))]
      (and (= 1 (:panels s))
           (= 8 (:polygons s))
           (= 2 (count (:color-categories viol-layer))))))])

;; ## Horizontal Violin

(-> (rdatasets/datasets-iris)
    (pj/lay-violin :species :petal-length)
    (pj/coord :flip))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 3 (:polygons s)))))])

;; ## [Ridgeline](https://en.wikipedia.org/wiki/Ridgeline_plot) Plot
;;
;; Overlapping density curves stacked vertically by category -- good
;; for comparing distribution shapes across many groups.

(-> (rdatasets/datasets-iris)
    (pj/lay-ridgeline :species :sepal-length))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Colored Ridgeline
;;
;; Map color to the same categorical column for distinct curves.

(-> (rdatasets/datasets-iris)
    (pj/lay-ridgeline :species :sepal-length {:color :species}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 3 (:polygons s)))))])

;; ## Comparing Multiple Columns
;;
;; Pass a vector of column names to `pj/lay-histogram` (or any
;; `lay-*` function) to create one panel per column. This is useful
;; for comparing the shape of different variables side by side.

(pj/lay-histogram (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length])

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:panels s))
                 (pos? (:polygons s)))))])

;; Combine with `:color` to see group differences within each column.

(pj/lay-density (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length] {:color :species})

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:panels s))
                 (pos? (:polygons s)))))])

;; The multi-column vector works with any `lay-*` function -- histograms,
;; density curves, boxplots, violin plots, and more.

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- mappings and aesthetics referenced throughout
;; - [**Relationships**](./plotje_book.relationships.html) -- two-distribution comparisons via heatmap, contour, and SPLOM

;; ## What's Next
;;
;; - [**Ranking**](./plotje_book.ranking.html) -- bar charts and lollipop plots for categorical comparisons
;; - [**Faceting**](./plotje_book.faceting.html) -- split distributions by groups into separate panels
