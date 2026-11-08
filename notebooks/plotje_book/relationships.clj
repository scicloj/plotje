;; # Relationships
;;
;; Scatter plots, regression, smoothing, density estimation, and
;; heatmaps -- revealing structure between two variables.
;;
;; Scatter is the foundation. Each row becomes a point in the
;; plane, and clusters or trends become visible. Regression
;; and smoothing draw trend lines through it; 2D density and
;; contours reveal where points are dense or sparse; the
;; scatter plot matrix (SPLOM) at the end shows every pair of
;; columns at once.

(ns plotje-book.relationships
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Fastmath -- random number generation
   [fastmath.random :as rng]))

;; ## Basic Scatter

;; Sepal dimensions, no color -- the default mark.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (zero? (:lines s)))))])

;; ## Colored by Species

;; Adding `:color :species` groups points by species with distinct colors.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (zero? (:lines s)))))])

;; ## Petal Dimensions

;; Petal length against petal width -- a strongly correlated pair, set up
;; here as the running example for the regression sections below.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (zero? (:lines s)))))])

;; ## Linear Regression

;; A single regression line through all data.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; ## Per-Group Regression

;; Fit a regression line per group.

(-> (rdatasets/datasets-iris)
    (pj/pose :petal-length :petal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; ## Regression with Confidence Ribbon

;; Pass `{:confidence-band true}` to show a 95% confidence band around the line.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model :confidence-band true}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          base (-> (rdatasets/datasets-iris)
                   (pj/pose :sepal-length :sepal-width {:color :species})
                   pj/lay-point)
          default-band (-> base
                           (pj/lay-smooth {:stat :linear-model
                                           :confidence-band true})
                           pj/plan
                           :panels
                           first
                           :layers
                           last
                           :ribbons)
          explicit-95 (-> base
                          (pj/lay-smooth {:stat :linear-model
                                          :confidence-band true
                                          :level 0.95})
                          pj/plan
                          :panels
                          first
                          :layers
                          last
                          :ribbons)]
      (and (= 150 (:points s))
           (= 3 (:lines s))
           (= 3 (:polygons s))
           ;; Default :confidence-band level is 0.95 -- ribbons match
           ;; explicit :level 0.95.
           (= default-band explicit-95))))])

;; Pass `:level` to widen or narrow the band. A 99% interval covers
;; more of the regression's uncertainty than the default 95%; an
;; 80% interval covers less:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model :confidence-band true :level 0.80}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))])

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model :confidence-band true :level 0.99}))

(kind/test-last
 [(fn [v]
    (let [iris (rdatasets/datasets-iris)
          median-width (fn [level]
                         (let [r (-> iris
                                     (pj/pose :sepal-length :sepal-width)
                                     (pj/lay-smooth {:stat :linear-model
                                                     :confidence-band true
                                                     :level level})
                                     pj/plan
                                     :panels
                                     first
                                     :layers
                                     first
                                     :ribbons
                                     first)
                               widths (map - (:ymaxs r) (:ymins r))]
                           (nth (sort widths) (quot (count widths) 2))))]
      ;; Wider :level produces a wider median band: 0.99 > 0.95 > 0.80.
      (> (median-width 0.99) (median-width 0.95) (median-width 0.80))))])

;; ## Tips with Regression

;; Do smokers and non-smokers tip differently?

(-> (rdatasets/reshape2-tips)
    (pj/pose :total-bill :tip {:color :smoker})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 244 (:points s))
                                (= 2 (:lines s)))))])

;; ## [LOESS](https://en.wikipedia.org/wiki/Local_regression) Smoothing

;; A smooth curve through noisy data.

(-> (let [r (rng/rng :jdk 42)
          xs (vec (range 50))]
      {:x xs
       :y (mapv #(+ (Math/sin (* % 0.2))
                    (* 0.3 (- (rng/drandom r) 0.5)))
                xs)})
    (pj/lay-point :x :y)
    (pj/lay-smooth {:bandwidth 0.2}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 50 (:points s))
                                (= 1 (:lines s)))))])

;; ## [Heatmap](https://en.wikipedia.org/wiki/Heat_map) (Auto-Binned)

;; Bin x and y into a grid, count points per cell.

(-> (rdatasets/datasets-iris)
    (pj/lay-tile :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:visible-tiles s)))))])

;; ## Heatmap (Pre-Computed)

;; Use a numeric column for tile color. On a numeric axis, the axis
;; reaches the outer edges of the tiles, so the first and last rows
;; and columns are drawn at full size.

(def grid-data
  (let [r (rng/rng :jdk 99)]
    {:x (for [i (range 5) _j (range 5)] i)
     :y (for [_i (range 5) j (range 5)] j)
     :value (repeatedly 25 #(rng/irandom r 100))}))

(-> grid-data
    (pj/lay-tile :x :y {:fill :value}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               [lo hi] (:x-domain (first (:panels (pj/plan v))))]
                           (and (= 1 (:panels s))
                                (pos? (:visible-tiles s))
                                ;; The outer tiles' edges, half a step
                                ;; past 0 and 4, are inside the axis.
                                (<= lo -0.5) (>= hi 4.5))))])

;; ## Density 2D

;; [KDE](https://en.wikipedia.org/wiki/Kernel_density_estimation)-smoothed 2D density heatmap.

(-> (rdatasets/datasets-iris)
    (pj/lay-density-2d :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:visible-tiles s)))))])

;; ## Density 2D with Points

;; Scatter points drawn on top of the density heatmap.

(-> (rdatasets/datasets-iris)
    (pj/lay-density-2d :sepal-length :sepal-width)
    (pj/lay-point {:alpha 0.5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (pos? (:visible-tiles s)))))])

;; ## [Contour](https://en.wikipedia.org/wiki/Contour_line) Lines

;; Iso-density contour lines from 2D KDE.

(-> (rdatasets/datasets-iris)
    (pj/lay-contour :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:lines s)))))])

;; ## Contour with Points

;; Contour lines drawn over scatter points.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:alpha 0.3})
    (pj/lay-contour {:levels 8}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (pos? (:lines s)))))])

;; ## Scatter with a Marginal Distribution
;;
;; A scatter shows how two columns move together and says little about
;; how either is spread on its own. `pj/marginal` puts that spread back:
;; a distribution of one of the pose's columns, drawn in a thin panel
;; against one edge of the scatter and sharing the scatter's axis for
;; that column. `:top` describes the `:x` column.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/marginal :top))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 150 (:points s)))))])

;; The strip keeps its own value scale, its duplicate ticks and axis
;; title are dropped, and the two drawing areas are aligned so a value
;; sits at the same place in both. `:histogram` draws bars instead of a
;; curve, and `:size` sets the strip's share of the height.
;;
;; `:right` describes the `:y` column instead, in a strip beside the
;; scatter. The distribution is drawn under `pj/coord :flip`, so it
;; runs up the panel with its baseline against the scatter:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/marginal :right :histogram))

(kind/test-last
 [(fn [v] (let [panels (mapv #(-> % :plan :panels first)
                             (:sub-plots (pj/plan v)))]
            (and (= 2 (:panels (pj/svg-summary v)))
                 (= 150 (:points (pj/svg-summary v)))
                 ;; The scatter and the strip are pinned to one range
                 ;; of sepal width.
                 (= (:y-domain (first panels))
                    (:y-domain (second panels))))))])

;; A pose takes one marginal: `pj/marginal` returns a composite, and a
;; composite has no single panel for a second strip to sit against. For
;; a distribution on both edges, `pj/matrix` with `nil` in each list of
;; columns draws the scatter and both marginals. A `nil` leaves a place
;; with no column on that axis, so that panel draws the distribution of
;; the other column, and the corner, `nil` on both axes, is left empty.
;; The marginals are histograms, where `pj/marginal` draws a density by
;; default, and the weights draw them a third the size of the scatter:

(pj/matrix (rdatasets/datasets-iris)
           [:sepal-length nil]
           [nil :sepal-width]
           {:col-weights [3 1] :row-weights [1 3]})

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                subs (:sub-plots (pj/plan v))
                marks (into {} (map (juxt :path #(mapv :mark (-> %
                                                                 :plan
                                                                 :panels
                                                                 first
                                                                 :layers)))) subs)
                w (into {} (map (juxt :path #(get-in % [:plan :panel-width]))) subs)
                h (into {} (map (juxt :path #(get-in % [:plan :panel-height]))) subs)]
            (and (= 3 (:panels s))
                 (= 150 (:points s))
                 ;; scatter at the lower left, a histogram above it and
                 ;; one at its right, the corner empty
                 (= {[0 0] [:bar] [0 1] [] [1 0] [:point] [1 1] [:bar]} marks)
                 (<= (Math/abs (- (w [1 0]) (* 3 (w [1 1])))) 3.0)
                 (<= (Math/abs (- (h [1 0]) (* 3 (h [0 0])))) 3.0))))])

;; A `:color` mapping does not reach the strip that `pj/marginal` adds
;; -- the strip describes the column as a whole. Faceting does reach
;; the strip, giving one distribution per panel, so facet first and add
;; the marginal after:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species)
    (pj/marginal :top)
    (pj/options {:width 900 :height 480}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                ;; One strip label per column, not one per cell: the
                ;; lower row is divided by species and draws none, so
                ;; three of the six panels carry a label.
                labelled (->> (pj/plan v)
                              :sub-plots
                              (mapcat (comp :panels :plan))
                              (keep :col-label))]
            (and (= 6 (:panels s))
                 (= 150 (:points s))
                 (= ["setosa" "versicolor" "virginica"] (vec labelled)))))])

;; `:col` is a mapping, and the marginal carries it across to the
;; distribution it builds, so both rows are divided by species. The
;; strip label is drawn once, at the top of each column, above the
;; marginal.

;; ## Scatter Plot Matrix (SPLOM)
;;
;; `pj/matrix` given a list of columns draws a grid with a panel for
;; every pair of those columns -- a scatter plot matrix. With no layer
;; written, each panel draws the layer type inferred from its columns:
;; a histogram where a column meets itself, on the diagonal, and a
;; scatter elsewhere.
;;
;; Start small: two variables crossed with themselves give a 2x2 grid.

(def small-cols [:sepal-length :petal-length])

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/matrix small-cols))

(kind/test-last
 [(fn [v]
    (let [marks (->> (:sub-plots (pj/plan v))
                     (mapv (fn [{:keys [path plan]}]
                             (let [[r c] path
                                   m (-> plan
                                         :panels
                                         first
                                         :layers
                                         first
                                         :mark)]
                               [r c m]))))]
      (and (= 4 (:panels (pj/svg-summary v)))
           (every? (fn [[r c m]] (= m (if (= r c) :bar :point))) marks))))])

;; `pj/matrix` writes a facet over columns: the columns across under
;; `:col` and the columns down under `:row`, so
;; `(pj/matrix data cols)` is `(pj/facet data {:col {:x cols} :row {:y cols}})`,
;; as [Faceting](./plotje_book.faceting.html#scatter-plot-matrix) shows.

(kind/test-last
 [(fn [v] (= v (-> (rdatasets/datasets-iris)
                   (pj/pose {:color :species})
                   (pj/facet {:col {:x small-cols} :row {:y small-cols}}))))])

;; The full 4x4 SPLOM follows the same pattern with iris's four
;; numeric columns:

(def cols [:sepal-length :sepal-width :petal-length :petal-width])

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/matrix cols))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 16 (:panels s))
           (= (* 12 150) (:points s))
           (pos? (:polygons s))
           (->> (:sub-plots (pj/plan v))
                (every? (fn [{:keys [path plan]}]
                          (let [[r c] path
                                mark (-> plan
                                         :panels
                                         first
                                         :layers
                                         first
                                         :mark)]
                            (= mark (if (= r c) :bar :point)))))))))])

;; Inference picks the layer type for each panel: diagonal panels,
;; where a column meets itself, get histograms; panels off the diagonal
;; get scatter plots. All panels share the color aesthetic set on the
;; pose.

;; A layer written on the matrix is drawn in the panels whose columns it
;; draws. A scatter draws two columns, so `pj/lay-point` is drawn in the
;; panels off the diagonal, and each diagonal panel, which names one
;; column, keeps the histogram that inference gives it:

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/matrix small-cols)
    pj/lay-point)

(kind/test-last
 [(fn [v]
    (->> (:sub-plots (pj/plan v))
         (every? (fn [{:keys [path plan]}]
                   (let [[r c] path
                         mark (-> plan
                                  :panels
                                  first
                                  :layers
                                  first
                                  :mark)]
                     (= mark (if (= r c) :bar :point)))))))])

;; A density draws one column, so `pj/lay-density` is drawn on the
;; diagonal, and the diagonal shows densities in place of histograms:

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/matrix small-cols)
    pj/lay-point
    pj/lay-density)

(kind/test-last
 [(fn [v]
    (->> (:sub-plots (pj/plan v))
         (every? (fn [{:keys [path plan]}]
                   (let [[r c] path
                         marks (set (map :mark (-> plan
                                                   :panels
                                                   first
                                                   :layers)))]
                     (= marks (if (= r c) #{:area} #{:point})))))))])

;; A layer that names its columns is drawn only in the panel of those
;; columns. Here a trend line is added to one panel of the matrix:

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/matrix small-cols)
    pj/lay-point
    pj/lay-density
    (pj/lay-smooth (first small-cols) (second small-cols) {:color "black"}))

(kind/test-last
 [(fn [v]
    (let [with-line (->> (:sub-plots (pj/plan v))
                         (filter (fn [{:keys [plan]}]
                                   (some #{:line} (map :mark (-> plan
                                                                 :panels
                                                                 first
                                                                 :layers))))))]
      (and (= 1 (count with-line))
           (= #{:point :line}
              (set (map :mark (-> with-line
                                  first
                                  :plan
                                  :panels
                                  first
                                  :layers)))))))])

;; A layer that fits no panel -- a histogram written on a pose whose
;; panels all name two columns -- is drawn in every panel, as it would be
;; on a single pose.
;;
;; See the [Gallery](./plotje_book.gallery.html#scatter-plot-matrices)
;; chapter for more SPLOM variations, and the
;; [Interactivity](./plotje_book.interactivity.html#brush-selection)
;; chapter for brush selection.

;; ## See Also
;;
;; - [**Composition**](./plotje_book.composition.html#marginal-plots) -- composite poses, the marginal among them, and shared scales
;; - [**Distributions**](./plotje_book.distributions.html) -- one-variable shape and spread

;; ## What's Next
;;
;; - [**Cookbook**](./plotje_book.cookbook.html) -- recipes for common multi-layer plots
;; - [**Faceting**](./plotje_book.faceting.html) -- divide a chart into panels by category, or give each of several columns a panel of its own
;; - [**Polar Coordinates**](./plotje_book.polar.html) -- radial charts and pie-style visualizations
;; - [**Customization**](./plotje_book.customization.html) -- mark styling, palettes, and themes
