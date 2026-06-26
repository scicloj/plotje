;; # Relationships
;;
;; Scatter plots, regression, smoothing, density estimation, and
;; heatmaps -- revealing structure between two variables.
;;
;; Scatter is the foundation. Each row becomes a point in the
;; plane, and the eye reads structure off the cloud. Regression
;; and smoothing draw trend lines through it; 2D density and
;; contours reveal where the cloud is dense or sparse; the
;; scatter-plot matrix (SPLOM) at the end shows every pair of
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

;; Petal length vs width -- a strongly correlated pair, set up
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
                           :panels first :layers last :ribbons)
          explicit-95 (-> base
                          (pj/lay-smooth {:stat :linear-model
                                          :confidence-band true
                                          :level 0.95})
                          pj/plan
                          :panels first :layers last :ribbons)]
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
                                     :panels first :layers first :ribbons first)
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

;; Use a numeric column for tile color.

(def grid-data
  (let [r (rng/rng :jdk 99)]
    {:x (for [i (range 5) _j (range 5)] i)
     :y (for [_i (range 5) j (range 5)] j)
     :value (repeatedly 25 #(rng/irandom r 100))}))

(-> grid-data
    (pj/lay-tile :x :y {:fill :value}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:visible-tiles s)))))])

;; ## Density 2D

;; [KDE](https://en.wikipedia.org/wiki/Kernel_density_estimation)-smoothed 2D density heatmap.

(-> (rdatasets/datasets-iris)
    (pj/lay-density-2d :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:visible-tiles s)))))])

;; ## Density 2D with Points

;; Overlay scatter points on the density heatmap.

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

;; Contour lines overlaid on scatter points.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:alpha 0.3})
    (pj/lay-contour {:levels 8}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (pos? (:lines s)))))])

;; ## Scatter Plot Matrix (SPLOM)
;;
;; `pj/cross` generates all combinations of two lists. Passing
;; column names produces a grid of scatter plots -- one per pair of
;; variables. The diagonal shows histograms (automatic inference
;; for same-column pairs).
;;
;; Start small: two variables crossed with themselves give a 2x2
;; grid. Off-diagonal cells (where the row and column variables
;; differ) get scatter plots; diagonal cells (where they match) get
;; histograms.

(def small-cols [:sepal-length :petal-length])

(-> (rdatasets/datasets-iris)
    (pj/pose (pj/cross small-cols small-cols) {:color :species}))

(kind/test-last
 [(fn [v]
    (let [marks (->> (:sub-plots (pj/plan v))
                     (mapv (fn [{:keys [path plan]}]
                             (let [[r c] path
                                   m (-> plan :panels first :layers first :mark)]
                               [r c m]))))]
      (and (= 4 (:panels (pj/svg-summary v)))
           (every? (fn [[r c m]] (= m (if (= r c) :bar :point))) marks))))])

;; The full 4x4 SPLOM follows the same pattern with iris's four
;; numeric columns:

(def cols [:sepal-length :sepal-width :petal-length :petal-width])

(-> (rdatasets/datasets-iris)
    (pj/pose (pj/cross cols cols) {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 16 (:panels s))
                                (= (* 12 150) (:points s))
                                (pos? (:polygons s)))))])

(kind/test-last
 [(fn [v]
    (->> (:sub-plots (pj/plan v))
         (every? (fn [{:keys [path plan]}]
                   (let [[r c] path
                         mark (-> plan :panels first :layers first :mark)]
                     (= mark (if (= r c) :bar :point)))))))])

;; Per-cell inference picks the layer type for each panel: diagonal
;; cells (x = y) get histograms; off-diagonal cells get scatter
;; plots. All panels share the color aesthetic set at the composite
;; root.
;;
;; See the [Faceting](./plotje_book.faceting.html) chapter for more
;; SPLOM variations, and the [Customization](./plotje_book.customization.html)
;; chapter for brush selection.

;; ## See Also
;;
;; - [**Composition**](./plotje_book.composition.html) -- composite poses (the SPLOM is one) and shared scales
;; - [**Distributions**](./plotje_book.distributions.html) -- one-variable shape and spread

;; ## What's Next
;;
;; - [**Faceting**](./plotje_book.faceting.html) -- split any chart into panels by category
;; - [**Polar Coordinates**](./plotje_book.polar.html) -- radial charts and pie-style visualizations
;; - [**Customization**](./plotje_book.customization.html) -- mark styling, palettes, and themes
