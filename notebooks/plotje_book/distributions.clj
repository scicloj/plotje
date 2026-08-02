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

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               domain #(-> % pj/plan :panels first :x-domain)]
                           (and (= 1 (:panels s))
                                (= 10 (:polygons s))
                                (= [4.12 8.08] (domain v))
                                (= (domain v)
                                   (domain (-> (rdatasets/datasets-iris)
                                               (pj/lay-histogram
                                                :sepal-length
                                                {:normalize :density :alpha 0.5})))))))])

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
 [(fn [v] (let [s (pj/svg-summary v)
                curve-xs (mapcat :xs (-> v pj/plan :panels first :layers first :groups))]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s))
                 (= [4.12 8.08] (-> v pj/plan :panels first :x-domain))
                 (= [4.3 7.9] [(apply min curve-xs) (apply max curve-xs)]))))])

;; ## Grouped Density
;;
;; Per-species density curves with automatic color mapping.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:color :species}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                per-group (mapv (fn [g] [(apply min (:xs g)) (apply max (:xs g))])
                                (-> v pj/plan :panels first :layers first :groups))]
            (and (= 1 (:panels s))
                 (= 3 (:polygons s))
                 (= [4.12 8.08] (-> v pj/plan :panels first :x-domain))
                 (= [[4.3 7.9] [4.3 7.9] [4.3 7.9]] per-group))))])

;; Each curve is estimated across every species' values, not just its
;; own, so all three share one interval and each falls away to nothing at
;; both ends. Pass `{:trim true}` to estimate each species over its own
;; values instead. That shows where each group's data actually lies, at
;; the cost of cutting each curve off at its extremes.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:color :species :trim true}))

(kind/test-last
 [(fn [v] (let [per-group (mapv (fn [g] [(apply min (:xs g)) (apply max (:xs g))])
                                (-> v pj/plan :panels first :layers first :groups))]
            (and (= [[4.3 5.8] [4.9 7.0] [4.9 7.9]] per-group)
                 (= [4.12 8.08] (-> v pj/plan :panels first :x-domain)))))])

;; ## Density with Custom Bandwidth
;;
;; Bandwidth sets how heavily the curve is smoothed. Left alone it
;; follows the rule R's `density()` uses, which for this column works out
;; near 0.27. A narrower bandwidth reveals more detail -- the curve below
;; rises higher and breaks into bumps the default smooths away -- while a
;; wider one flattens it further.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:bandwidth 0.1}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                peak (fn [pose] (apply max (:ys (first (-> pose pj/plan :panels first
                                                           :layers first :groups)))))]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s))
                 (> (peak v)
                    (peak (-> (rdatasets/datasets-iris)
                              (pj/lay-density :sepal-length)))))))])

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
;;
;; The curve and the rug start and stop together. A density estimate is
;; defined everywhere, not only where the data is, so drawing one means
;; choosing an interval to estimate it over, and Plotje uses the observed
;; values. Both therefore stop a little short of the axis ends, which
;; carry the same padding every numeric axis gets. This matches what
;; ggplot2's `geom_density()` draws for the same data.

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length)
    pj/lay-rug)

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                domain #(-> % pj/plan :panels first :x-domain)]
            (and (= 1 (:panels s))
                 (= 1 (:polygons s))
                 (= 150 (:lines s))
                 (= [4.12 8.08] (domain v))
                 (= (domain v)
                    (domain (-> (rdatasets/datasets-iris)
                                (pj/lay-rug :sepal-length))))
                 (let [curve-xs (mapcat :xs (-> v pj/plan :panels first :layers first :groups))]
                   (= [4.3 7.9] [(apply min curve-xs) (apply max curve-xs)])))))])

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

;; The 1.5-times-IQR claim is structural: each whisker stays within the
;; Tukey fence `[Q1 - 1.5*IQR, Q3 + 1.5*IQR]`, and every outlier
;; falls outside it.

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          boxes (->> (pj/plan v) :panels first :layers
                     (filter #(= :boxplot (:mark %)))
                     first :boxes)
          within-fences? (fn [{:keys [q1 q3 whisker-lo whisker-hi outliers]}]
                           (let [iqr (- q3 q1)
                                 lo-fence (- q1 (* 1.5 iqr))
                                 hi-fence (+ q3 (* 1.5 iqr))]
                             (and (>= whisker-lo lo-fence)
                                  (<= whisker-hi hi-fence)
                                  (every? (fn [o] (or (< o lo-fence) (> o hi-fence)))
                                          outliers))))]
      (and (= 1 (:panels s))
           (= 3 (:polygons s))
           (pos? (:lines s))
           (= 3 (count boxes))
           (every? within-fences? boxes))))])

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
 [(fn [v] (let [s (pj/svg-summary v)
                bodies (-> v pj/plan :panels first :layers first :violins)
                col (:total-bill (rdatasets/reshape2-tips))
                lo (apply min col)
                hi (apply max col)]
            (and (= 1 (:panels s))
                 (= 4 (:polygons s))
                 (every? (fn [b] (and (>= (apply min (:ys b)) lo)
                                      (<= (apply max (:ys b)) hi)))
                         bodies))))])

;; A violin takes the opposite default to a density: each body is
;; estimated over its own category's values, so it ends where that day's
;; bills end rather than tapering past them. Pass `{:trim false}` to let
;; the tails fall away instead.

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
