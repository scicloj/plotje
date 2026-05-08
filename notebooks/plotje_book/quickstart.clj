;; # Quickstart
;;
;; A minimal introduction to Plotje

;; ## Setup
;;
;; Add Plotje to your `deps.edn`. Current release: [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/plotje.svg)](https://clojars.org/org.scicloj/plotje)
;;
;; For this demo, you will also need:
;; * [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](clay) - to visualize plots with [Clay](https://scicloj.github.io/clay). Follow Clay documentation for recommended usage. You may find the [videos](https://scicloj.github.io/clay/#videos) useful. It is recommended to set Clay up using the [Setup](https://scicloj.github.io/clay/#setup) for your specific IDE or editor.
;; * [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/metamorph.ml.svg)](https://clojars.org/org.scicloj/metamorph.ml) - for [the RDatasets collection](./plotje_book.datasets.html#the-rdatasets-collection)

;; Then require the API:

(ns plotje-book.quickstart
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; Use [Clay](https://scicloj.github.io/clay/) or other
;; [Kindly](https://scicloj.github.io/kindly-noted/)-compatible tools
;; to visualize the examples below.

;; ## Your First Plot
;;
;; Load the classic [iris](https://en.wikipedia.org/wiki/Iris_flower_data_set) dataset and scatter two columns:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; - `rdatasets/datasets-iris` loads the classic iris dataset from
;; [the RDatasets collection](./plotje_book.datasets.html#the-rdatasets-collection)
;; as a [Tablecloth](https://scicloj.github.io/tablecloth/) dataset
;; with keyword column names.
;; - `pj/lay-point` shows each row as a dot (scatter plot).

;; ## Plain Data
;;
;; You do not need to load a CSV -- Plotje accepts plain Clojure
;; data and coerces it into a dataset internally.
;; A map of columns works directly:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; When the dataset has few columns, you can skip the column names --
;; Plotje infers them from the dataset shape:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    pj/lay-point)

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; Auto-inference covers 1-3 column datasets. With 4 or more
;; columns Plotje cannot guess which to plot and asks you to be
;; explicit -- pass column names like `(pj/lay-point data :x :y)`.

;; See [**Core Concepts**](./plotje_book.core_concepts.html) for more input formats.

;; String column names also work -- keywords are conventional but not
;; required:

(-> {"x" [1 2 3 4 5] "y" [2 4 3 5 4]}
    (pj/lay-point "x" "y"))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; ## Color

;; Map a column to `:color` to color points by group.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"setosa"} (:texts s))
                                (some #{"sepal length"} (:texts s)))))])

;; ## More Chart Types
;;
;; Each `pj/lay-*` function adds a different chart type.
;;
;; **Histogram** -- pass a single column for automatic binning:

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s))
                                (zero? (:points s)))))])

;; **Bar chart** -- count occurrences of a categorical column:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:polygons s)))))])

;; **Horizontal bars** -- flip with `pj/coord`:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/coord :flip))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

;; **Line chart** -- connect points in order:

(-> {:x [1 2 3 4 5 6 7 8]
     :y [3 5 4 7 6 8 7 9]}
    (pj/lay-line :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:lines s))
                                (zero? (:points s)))))])

;; **Boxplot** -- compare distributions across categories:

(-> (rdatasets/datasets-iris)
    (pj/lay-boxplot :species :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:lines s)))))])

;; See the [**Layer Types**](./plotje_book.layer_types.html) chapter for the full list of chart types.

;; ## Inference
;;
;; `pj/pose` declares which columns to plot without committing to a
;; chart type. When a pipeline ends at `pj/pose` (no `pj/lay-*`),
;; Plotje picks the chart type from the column types.
;; Two numerical columns produce a scatter plot:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; A single categorical column produces a bar chart:

(-> (rdatasets/datasets-iris)
    (pj/pose :species))

(kind/test-last [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; A single numerical column produces a histogram:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; See the [**Inference Rules**](./plotje_book.inference_rules.html) chapter for the full set of rules.

;; ## Multiple Layers
;;
;; Use `pj/pose` to set column mappings for a pose,
;; then add layers with `pj/lay-*`. All layers on this pose
;; inherit the pose's mappings. Here `(pj/lay-smooth {:stat :linear-model})` adds a linear model
;; (regression line) per group:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; ## Titles and Labels
;;
;; Use `pj/options` for width, height, title, and axis labels:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species})
    (pj/options {:width 500 :height 350
                 :title "Iris Petals"
                 :x-label "Petal Length (cm)"
                 :y-label "Petal Width (cm)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Iris Petals"} (:texts s))
                                (some #{"Petal Length (cm)"} (:texts s)))))])

;; ## Dashboards
;;
;; Combine multiple plots with `pj/arrange`:

(pj/arrange [(pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width {:color :species})
             (pj/lay-histogram (rdatasets/datasets-iris) :sepal-length {:color :species})]
            {:cols 2})

(kind/test-last [(fn [v] (pj/pose? v))])

;; ## Getting the SVG
;;
;; The examples above auto-render in this notebook because
;; [Clay](https://scicloj.github.io/clay/) recognizes Plotje poses
;; and visualizes them. In an interactive workflow with Clay (or
;; another [Kindly](https://scicloj.github.io/kindly-noted/)-compatible
;; tool), you rarely need the SVG explicitly.
;;
;; When you do want it -- to inspect the hiccup or embed the plot
;; in a webpage -- call `pj/plot`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    pj/plot)

(kind/test-last [(fn [v] (and (vector? v) (= :svg (first v))))])

;; The result is a [hiccup](https://github.com/weavejester/hiccup)
;; vector starting with `:svg` -- a Clojure data representation of
;; the SVG markup. The notebook auto-rendered it as a plot above
;; because Clay has built-in hiccup rendering. To see the hiccup value
;; itself rather than its rendering, wrap with `kind/pprint`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    pj/plot
    kind/pprint)

(kind/test-last [(fn [v] (and (vector? v) (= :svg (first v))))])

;; ## Export

;; Save a plot to SVG with `pj/save`. It writes the file and returns the path:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/save "/tmp/iris-scatter.svg"))

(kind/test-last [(fn [p] (and (string? p) (.endsWith ^String p ".svg")))])

;; For PNG output, `(pj/save pose "x.png")` writes a raster image via
;; the Java2D backend; see the Cookbook for other export paths.

;; ## What's Next
;;
;; - [**Datasets**](./plotje_book.datasets.html) -- what kinds of data Plotje accepts (tablecloth datasets, maps of vectors, sequences of row maps)
;; - [**Poses**](./plotje_book.pose_model.html) -- the mental model behind composable plotting
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- data formats, marks, stats, color, grouping, coordinates
;; - [**Relationships**](./plotje_book.relationships.html) -- scatter plots, regression, density, and SPLOM
;; - [**Cookbook**](./plotje_book.cookbook.html) -- recipes for common multi-layer plots
;; - [**Configuration**](./plotje_book.configuration.html) -- themes, backgrounds, palettes, and other plot-level defaults
;; - [**Gallery**](./plotje_book.gallery.html) -- many more chart variations with side-by-side code
