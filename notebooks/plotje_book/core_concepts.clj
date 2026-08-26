;; # Core Concepts
;;
;; The vocabulary you reach for daily: data, mappings, scope, layer
;; types, and how they fit together. This chapter takes the mental
;; model from Poses and turns it into a working reference you
;; can scan while building a plot.
;;
;; Read [Poses](./plotje_book.pose_model.html) first if you
;; have not -- this chapter builds on the pose vocabulary it
;; introduces.

(ns plotje-book.core-concepts
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## Data
;;
;; Plotje accepts plain Clojure data -- maps, vectors of maps --
;; or columnar datasets. No wrapping needed for simple cases.
;; The [Datasets](./plotje_book.datasets.html#creating-datasets) chapter covers
;; data formats and loading in detail.
;;
;; We use the classic iris flower dataset throughout these examples.
;; Each column has a name (a keyword like `:sepal-length`) and holds
;; values of one type.

(rdatasets/datasets-iris)

(kind/test-last
 [(fn [ds]
    (and (= 150 (tc/row-count ds))
         (= 6 (tc/column-count ds))
         (= [:rownames :sepal-length :sepal-width :petal-length :petal-width :species]
            (vec (tc/column-names ds)))))])

;; The dataset has 150 rows and 6 columns: a `:rownames` index plus
;; four **numerical** measurements (in centimeters) and one
;; **categorical** column (the species name -- one of three strings).
;;
;; This distinction matters: Plotje treats numerical and
;; categorical columns differently when choosing axes, colors, and
;; statistical transforms.
;;
;; Here is a scatter plot of sepal dimensions, colored by species:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Printed, the pose carries the data and the `:x`/`:y` position
;; mapping at the top, and one point layer with its own layer-scoped
;; `:color`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species})
    kind/pprint)

(kind/test-last [(fn [v] (and (= :sepal-length (get-in v [:mapping :x]))
                              (= 1 (count (:layers v)))
                              (= :species (get-in v [:layers 0 :mapping :color]))))])

;; ### Input formats
;;
;; Plotje accepts several common Clojure data shapes and
;; coerces them into a dataset internally.
;;
;; **Map of columns** -- keys are column names, values are sequences:

(-> {:x [1 2 3 4 5]
     :y [2 4 3 5 4]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; **Sequence of row maps** -- each map is one row:

(-> [{:city "Paris" :temperature 22}
     {:city "London" :temperature 18}
     {:city "Berlin" :temperature 20}
     {:city "Rome" :temperature 28}]
    (pj/lay-bar :city :temperature))

(kind/test-last [(fn [v] (= 4 (:polygons (pj/svg-summary v))))])

;; **Sequence of row vectors** -- each vector is one row, and the
;; columns are named by their place: 0, 1 and so on. Nothing has to be
;; wrapped first:

(-> [[1 2] [3 4] [5 7]]
    pj/lay-point)

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Those integer names are what the axes are titled with, and they can
;; be mapped like any other column name. `(tc/rename-columns ds [:x :y])`
;; gives them words. A vector of vectors written where a mapping goes
;; means something else -- pairs of column names, one panel each, shown
;; under [Identity](#identity) below. What tells the two apart is where
;; the vector is written, not what it holds.

;; When the dataset has 1, 2, or 3 columns, you can omit the column
;; names entirely -- they are inferred by position: the first column
;; becomes x, the second becomes y, the third becomes color.

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    pj/lay-point)

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; Datasets with four or more columns require explicit column names.
;; See the [Datasets](./plotje_book.datasets.html#from-a-csv-or-url) chapter for
;; loading from CSV, URLs, and other file formats.

;; ## Mappings and Layers
;;
;; The [Poses](./plotje_book.pose_model.html#poses-carry-mappings) chapter introduced
;; the mapping-vs-layer split (what vs how). This section is the
;; practical follow-up: how the split plays out across multi-layer
;; plots and explicit-vs-shorthand calls.
;;
;; Declare a mapping once with `pj/pose`, then add layers with
;; `pj/lay-*` -- both layers share the same axes:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (pos? (:lines s)))))])

;; One mapping, two layers: points and a regression line.
;;
;; When `pj/lay-*` is called **with columns**, it creates a pose and
;; attaches a layer in one step:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; We will revisit where mappings flow (to all layers or to a single
;; one) in the Scope section below.

;; With multiple poses arranged side by side, use `pj/arrange`:

(def two-panel
  (pj/arrange
   [(-> (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width))
    (-> (rdatasets/datasets-iris)
        (pj/lay-point :petal-length :petal-width))]))

two-panel

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Each sub-pose has its own mapping and layers. `pj/arrange`
;; produces a composite pose that contains them as siblings.

;; ## Scope
;;
;; A **mapping** maps a column (or a written value) to a visual
;; property -- like mapping `:species` to color. Where you write a
;; mapping determines who sees it. There are two levels:
;;
;; | Where you write it | What sees it |
;; |:-------------------|:-------------|
;; | `(pj/pose ... {:color :c})` | All layers on this pose |
;; | `(pj/lay-point ... {:color :c})` | This layer only |
;;
;; This is **lexical scope** -- the closest enclosing definition
;; wins.

;; ### Pose-level mapping
;;
;; `pj/pose`'s mapping flows to every layer attached to the pose:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (= 3 (:lines (pj/svg-summary v))))])

;; Both point and smooth layers see `:color :species`. Three
;; regression lines -- one per species.

;; ### Layer-level mapping
;;
;; A mapping in `pj/lay-*` scopes to that layer alone:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species})
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; Printed, `:color` lives on the point layer's own `:mapping`, not
;; on the pose -- so only the point layer sees it:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species})
    (pj/lay-smooth {:stat :linear-model})
    kind/pprint)

(kind/test-last [(fn [v] (and (= :species (get-in v [:layers 0 :mapping :color]))
                              (not (contains? (or (get-in v [:layers 1 :mapping]) {})
                                              :color))))])

;; Color is on the point layer. The smooth layer does not see it --
;; one overall regression line.

;; ### Override
;;
;; Lower scopes override higher ones. A layer can cancel a mapping
;; by setting it to `nil`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    (pj/lay-point {:color nil})
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; Printed, the override appears as `:color nil` in the point
;; layer's own `:mapping`, erasing the pose-level color for that
;; layer only:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    (pj/lay-point {:color nil})
    (pj/lay-smooth {:stat :linear-model})
    kind/pprint)

(kind/test-last [(fn [v] (and (= :species (get-in v [:mapping :color]))
                              (contains? (get (first (:layers v)) :mapping) :color)
                              (nil? (get-in (first (:layers v)) [:mapping :color]))))])

;; The pose says `:color :species`. The point layer cancels it with
;; `nil` -- uncolored points. The smooth layer has no override, so it
;; keeps the pose-level color -- three lines.

;; ### How scope is applied
;;
;; The same scoping principle governs three things -- mappings,
;; layers, and data -- but each combines differently when pose
;; level meets layer level:
;;
;; - **Mappings**: pose and layer mappings merge; the innermost
;;   wins on conflict, and an explicit `nil` erases a mapping
;;   inherited from above. The one part that combines rather than
;;   being replaced is a mapping's `:scale`, whose settings accumulate
;;   down the chain -- see
;;   [Scales](./plotje_book.scales.html#scales-accumulate).
;; - **Layers**: every `pj/lay-*` accumulates a new layer; layers
;;   do not override, they pile up.
;; - **Data**: the first argument to `pj/pose`/`pj/lay-*` sets the
;;   pose-level dataset; passing `:data` in a layer's options
;;   overrides it for that layer alone (innermost non-nil wins).

;; ### Layer-level data
;;
;; Pass `:data` in the options map of `pj/lay-*` to give that layer its
;; own dataset:

(def setosa
  (tc/select-rows (rdatasets/datasets-iris)
                  #(= "setosa" (:species %))))

(def versicolor
  (tc/select-rows (rdatasets/datasets-iris)
                  #(= "versicolor" (:species %))))

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:data setosa})
    (pj/lay-smooth {:stat :linear-model :data versicolor}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 50 (:points s))
                                (= 1 (:lines s)))))])

;; Printed, each layer carries its own `:data` alongside its
;; `:mapping`; the pose-level `:data` remains as a default for
;; any layer that does not override it:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:data setosa})
    (pj/lay-smooth {:stat :linear-model :data versicolor})
    kind/pprint)

(kind/test-last [(fn [v] (and (some? (:data v))
                              (contains? (first (:layers v)) :data)
                              (contains? (second (:layers v)) :data)))])

;; Points from setosa (50 rows), regression from versicolor.
;; Same pose, different data per layer.
;;
;; Faceting splits a single dataset into panels automatically:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s)))))])

;; Three panels, each with its own data subset.

;; ## Identity
;;
;; Scope determines what each pose and layer sees. But how do
;; layers find their pose? The rule is:
;;
;; - `pj/lay-*` with columns **finds the most recent leaf pose**
;;   whose position mappings match, or creates a new one if none
;;   matches.
;;
;; This makes the threading pipeline sequential and predictable.
;; When the columns match, the new layer attaches to the existing
;; pose:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; One pose, two layers: scatter points and a regression line
;; sharing the same axes.
;;
;; To draw two column-pairs as their own panels, use an explicit
;; `pj/pose` call with a vector of column-pairs. Each `[x y]` pair
;; becomes one panel, and a panel's place on the grid follows its
;; columns: its x decides which column of the grid it sits in, its y
;; which row.

(-> (rdatasets/datasets-iris)
    (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Printed, the two-panel outcome is a composite with two sub-poses:

(-> (rdatasets/datasets-iris)
    (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point)
    kind/pprint)

(kind/test-last [(fn [v] (and (= 2 (count (:poses v)))
                              (= :sepal-length (get-in v [:poses 0 :mapping :x]))
                              (= :sepal-width  (get-in v [:poses 0 :mapping :y]))
                              (= :petal-length (get-in v [:poses 1 :mapping :x]))
                              (= :petal-width  (get-in v [:poses 1 :mapping :y]))
                              ;; These two pairs share no column, so the
                              ;; panels take a column and a row each and
                              ;; come out on a diagonal -- they differ in
                              ;; both where they start across and down.
                              (let [[a b] (map (comp :panel-box :frames)
                                               (:panels (pj/frames v)))]
                                (and (not= (first a) (first b))
                                     (not= (second a) (second b))))))])

;; These two pairs have no column in common, so they take a column
;; and a row each: the panels come out on the diagonal of a two-by-two
;; grid, and the other two cells stay empty. Pairs that do share a
;; column line up instead -- a shared y puts them in one row, a shared
;; x in one column.
;;
;; For two plots side by side whatever their columns -- or for plots
;; with different layer kinds, a scatter and a histogram say -- use
;; `pj/arrange` to combine independent poses:

(pj/arrange
 [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
  (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ## The Pose
;;
;; The [Poses](./plotje_book.pose_model.html) chapter walked
;; through the shape of a pose end to end. This is the per-field
;; reference card -- what each slot holds and which API call sets
;; it:
;;
;; | Field | Contains | Set by |
;; |:------|:---------|:-------|
;; | `:data` | the dataset | `pj/pose`, `pj/lay-*`, or `pj/with-data` |
;; | `:mapping` | pose-level mappings, and their scales | `pj/pose`, `pj/scale` |
;; | `:layers` | layers attached to the pose | `pj/lay-*` |
;; | `:opts` | title, width, theme, coord, facets | `pj/options`, `pj/coord`, `pj/facet` |
;;
;; A composite pose adds `:poses` (sub-poses) and optionally
;; `:layout` and `:share-scales`; see the
;; [Composition](./plotje_book.composition.html#explicit-composite-poses) chapter for that
;; shape.

;; ## Mark, Stat, and Position
;;
;; Each layer has a **layer type** -- a rendering recipe with three parts:
;;
;; - **Mark** -- the visual shape (point, bar, line, area, tile, ...)
;; - **Stat** -- the computation before rendering (identity, bin, linear-model, density, ...)
;; - **Position** -- how overlapping groups share space (identity, dodge, stack, ...)
;;
;; A keyword like `:histogram` or `:point` names a layer type --
;; look it up to see its parts:

(pj/layer-type-lookup :histogram)

(kind/test-last [(fn [m] (= :bar (:mark m)))])

;; A histogram: stat `:bin` computes ranges, mark `:bar` shows them:

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; A regression: stat `:linear-model` fits a line, mark `:line` shows it:

(pj/layer-type-lookup :smooth)

(kind/test-last [(fn [m] (= :loess (:stat m)))])

;; Position `:stack` places groups on top of each other:

(-> {:day ["Mon" "Mon" "Tue" "Tue"]
     :count [30 20 45 15]
     :meal ["lunch" "dinner" "lunch" "dinner"]}
    (pj/lay-bar :day :count {:color :meal :position :stack}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; See the [Layer Types](./plotje_book.layer_types.html#marks) chapter
;; for complete tables of every mark, stat, and position.

;; ## Inference
;;
;; Plotje tries to make small poses work without you having
;; to specify everything. You give it what you know -- a dataset,
;; perhaps a column or two -- and it fills in the rest by
;; inspecting the data.
;;
;; The underlying principle is short: wherever you make a choice
;; it wins; wherever you don't, the library picks something
;; sensible.
;;
;; **Column inference** applies when a dataset has up to three
;; columns and you call `pj/pose` (or a `pj/lay-*`) without
;; naming any column. Plotje pairs the columns with
;; aesthetics in dataset order:
;;
;; | Columns in data | Inferred mapping |
;; |:----------------|:-----------------|
;; | 1 | `:x` |
;; | 2 | `:x`, `:y` |
;; | 3 | `:x`, `:y`, `:color` |
;;
;; With four or more columns the library does not guess -- you
;; have to name the columns you want. Column inference is most
;; useful for quick sketches of small, focused datasets.

(-> {:height [170 180 165 175] :weight [70 80 65 75]}
    pj/lay-point)

(kind/test-last [(fn [v] (= 4 (:points (pj/svg-summary v))))])

;; **Layer type inference** happens when a pose has no explicit
;; layer. The library inspects the types of the columns the
;; mapping refers to and picks a chart type that fits. Two
;; numerical columns produce a scatter plot:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; A single numerical column produces a histogram:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; In both cases the inferred plot is the same one you would get
;; from `pj/lay-point` or `pj/lay-histogram`. Inference is a
;; shorthand, not a separate rendering path. Every inferred
;; choice can be overridden -- see
;; [Inference Rules](./plotje_book.inference_rules.html) for the
;; full decision logic and override settings, or
;; [Architecture](./plotje_book.architecture.html#where-inference-happens) for where in
;; the pipeline each kind of inference happens.

;; ## Incremental Building
;;
;; Because poses are plain data, you can save a partial plot and
;; extend it later. Each call returns a new pose without changing
;; the original.

(def scatter-base
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)))

;; Add a regression line:

(-> scatter-base (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; Or a LOESS smoother instead:

(-> scatter-base pj/lay-smooth)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

;; ## Reusable Pose Templates
;;
;; A pose does not need to carry data. `(pj/pose)` creates an
;; empty pose you can evolve like any other -- adding layers,
;; options -- and then attach a dataset at the end with
;; `pj/with-data`. The result is a reusable pose that can
;; be applied to many datasets:

(def scatter-with-regression
  (-> (pj/pose nil {:x :x :y :y :color :group})
      pj/lay-point
      (pj/lay-smooth {:stat :linear-model})
      (pj/options {:title "Scatter with Regression"})))

;; Printed, the template has `:data nil` -- a pose that carries
;; mapping, layers, and options but no data yet:

(kind/pprint scatter-with-regression)

(kind/test-last [(fn [v] (and (nil? (:data v))
                              (= 2 (count (:layers v)))
                              (= "Scatter with Regression" (get-in v [:opts :title]))))])

;; Apply to one dataset:

(-> scatter-with-regression
    (pj/with-data {:x [1 2 3 4 5 6]
                   :y [2 4 3 5 6 8]
                   :group ["a" "a" "a" "b" "b" "b"]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 6 (:points s))
                                (= 2 (:lines s)))))])

;; Apply the same template to a different dataset:

(-> scatter-with-regression
    (pj/with-data {:x [10 20 30 40 50 60]
                   :y [15 18 22 20 25 28]
                   :group ["x" "x" "x" "y" "y" "y"]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 6 (:points s))
                                (= 2 (:lines s)))))])

;; `pj/with-data` validates at attach time: if the dataset is
;; missing a column the pose references, you get a clear error
;; naming the missing columns -- no cryptic failure deep in the
;; rendering path.

;; ## Color and Grouping
;;
;; `:color` controls point and line colors. Its behavior depends on
;; what you pass.
;;
;; **Categorical column** -- each unique value gets a distinct color.
;; A legend maps labels to colors:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"setosa"} (:texts s)))))])

;; **Numeric column** -- values map to a continuous gradient:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :petal-length}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; **Fixed color string** -- all points colored uniformly:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color "steelblue"}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; **Coming from ggplot2.** In ggplot2, `colour="blue"` is always a
;; CSS color. In Plotje, `{:color "blue"}` is a column reference if
;; **a column with that exact name exists in the data**, otherwise the
;; CSS color. A keyword is asked the same two questions in the same
;; order, so `{:color :blue}` is the column `:blue` where there is one
;; and the color blue where there is not. Matching is strict: a string
;; only matches a string column name, and a keyword only matches a
;; keyword column name. Hex codes like `"#0000ff"` are practically
;; unambiguous -- the dataset is asked first for those too, but no
;; dataset carries a column called `#0000ff`. To settle a genuine
;; collision, say which you mean: `{:color {:column "blue"}}` or
;; `{:color {:value "blue"}}`.
;;
;; The disambiguation matters when the dataset uses string column
;; names. With a string column named `"blue"`, the column wins --
;; three palette colors render, not a single blue:

(-> (tc/dataset {"x" [1 2 3] "y" [1 2 3] "blue" ["a" "b" "c"]})
    (pj/lay-point "x" "y" {:color "blue"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               colors (disj (:colors s) "none")]
                           (= 3 (count colors))))])

;; Same string `:color`, dataset without a `"blue"` column -- "blue"
;; parses as a CSS color:

(-> (tc/dataset {"x" [1 2 3] "y" [1 2 3]})
    (pj/lay-point "x" "y" {:color "blue"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               colors (disj (:colors s) "none")]
                           (= #{"rgb(0,0,255)"} colors)))])

;; Categorical color does more than set colors -- it creates
;; **groups**. Each group is processed independently: it gets its
;; own regression line, density curve, or boxplot:

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:color :species}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; Other visual properties include `:alpha` (transparency), `:size`,
;; and `:shape`. Each accepts a value or a column the same way
;; `:color` does -- `{:shape :square}` draws every point square, and
;; `{:shape :species}` gives each category its own symbol.
;;
;; **Bubble plot** -- `:size` mapped to a numeric column gives each
;; point a radius reflecting the value:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width
                  {:color :petal-length :size :petal-width :alpha 0.7}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; **Shape by category** -- `:shape` mapped to a categorical column
;; renders each group with a different marker, and a legend naming
;; them. Useful for monochrome printing:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:shape :species}))

(kind/test-last
 [(fn [v]
    (let [layer (-> v pj/plan :panels first :layers first)
          shape-values (set (mapcat :shapes (:groups layer)))
          s (pj/svg-summary v)]
      (and (= 3 (count shape-values))
           (= 150 (+ (:points s) (:polygons s)))
           (every? (set (:texts s)) ["setosa" "versicolor" "virginica"]))))])

;; `:color` and `:shape` can take the same column at once. Each
;; species then has its own color and its own marker, so a reader who
;; cannot tell the colors apart can still tell the groups apart. The
;; legend has one entry per species, and its key shows both:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species
                                              :shape :species}))

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan v)]
      (and (nil? (:shape-legend plan))
           (= [:circle :square :triangle]
              (mapv :shape (:entries (:legend plan)))))))])

;; The `:group` aesthetic creates groups without changing colors:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:group :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; Three regression lines but all the same color.

;; ## Plot Options and Reference Lines
;;
;; So far you've seen mappings, layers, and data -- all scoped at
;; pose or layer level. The functions in this section set
;; **plot-level options** instead: values that describe a plot
;; rather than a layer. They are written on a pose and reach
;; everything beneath it: written on the pose you are building they
;; describe the whole plot, and written on one cell of a composite,
;; that cell. See
;; [Options and Scopes](./plotje_book.options_and_scopes.html#plot-options)
;; for the full picture.
;;
;; `pj/options` sets plot-level settings -- title, axis labels, size,
;; theme overrides:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Iris Measurements"
                 :width 500 :color-values :dark2}))

(kind/test-last [(fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))])

;; Reference lines and shaded bands are themselves layers, added with
;; `pj/lay-rule-h`, `pj/lay-rule-v`, `pj/lay-band-h`, `pj/lay-band-v`.
;; Positions come from the options map (`:y-intercept` / `:x-intercept` for
;; rules; `:y-min`/`:y-max` or `:x-min`/`:x-max` for bands); appearance
;; aesthetics like `:color` and `:alpha` work the same way they do on
;; any other layer.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-rule-h {:y-intercept 3.0})
    (pj/lay-band-v {:x-min 5.0 :x-max 6.0 :alpha 0.1}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Printed, annotation layers carry their positions (`:y-intercept`,
;; `:x-min`, `:x-max`) and appearance (`:alpha`) inside the `:mapping`
;; slot, the same slot chart layers use for their mappings:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-rule-h {:y-intercept 3.0})
    (pj/lay-band-v {:x-min 5.0 :x-max 6.0 :alpha 0.1})
    kind/pprint)

(kind/test-last [(fn [v] (and (= :point (get-in v [:layers 0 :layer-type]))
                              (= :rule-h (get-in v [:layers 1 :layer-type]))
                              (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
                              (= :band-v (get-in v [:layers 2 :layer-type]))
                              (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))])

;; See the [Customization](./plotje_book.customization.html#reference-line-and-band-appearance)
;; chapter for annotation appearance (default opacity, color
;; overrides), palettes, and themes. Temporal intercepts
;; (`LocalDate`, `Instant`) are covered in
;; [Timelines](./plotje_book.timelines.html#annotated-time-series).

;; ## Coordinates and Scales
;;
;; `pj/coord` sets the coordinate system. `:flip` swaps the axes:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/coord :flip))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; `:fixed` locks the aspect ratio so one data unit on x equals one
;; data unit on y -- useful for spatial data or when distances on
;; the two axes should read the same. The panel width adjusts to
;; preserve the ratio:

(-> {:x [-1 1 -1 1] :y [-1 -1 1 1]}
    (pj/lay-point :x :y)
    (pj/coord :fixed))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                ;; :fixed shrank width to preserve
                                ;; aspect ratio; default would be 600.
                                (< (:width s) 600))))])

;; `pj/scale` changes how a numeric axis is shown. `:log` applies
;; a logarithmic transformation:

(-> {:population [1000 5000 50000 200000 1000000 5000000]
     :area [2 8 30 120 500 2100]}
    (pj/lay-point :population :area)
    (pj/scale :x :log)
    (pj/scale :y :log))

(kind/test-last [(fn [v] (= 6 (:points (pj/svg-summary v))))])

;; Both are written on the pose and reach every layer and panel
;; beneath it. A mapping written out in full can name its own scale
;; for one aesthetic, which takes precedence there.

;; ## Faceting and Multi-Panel Layouts
;;
;; **Faceting** splits a pose into panels by a column's values:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/facet :species)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s)))))])

;; Printed, the facet column lives in `:opts` as `:facet-col` --
;; the pose itself is not split until render time:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/facet :species)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    kind/pprint)

(kind/test-last [(fn [v] (= :species (get-in v [:opts :facet-col])))])

;; A vector of column names creates one panel per variable:

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram [:sepal-length :sepal-width :petal-length]))

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; Printed, each named column becomes a sub-pose with its own x
;; mapping; the bare `pj/lay-histogram` attaches at the root and
;; flows into every panel at plan time:

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram [:sepal-length :sepal-width :petal-length])
    kind/pprint)

(kind/test-last [(fn [v] (and (= 3 (count (:poses v)))
                              (= :sepal-length (get-in v [:poses 0 :mapping :x]))
                              (= :sepal-width (get-in v [:poses 1 :mapping :x]))
                              (= :petal-length (get-in v [:poses 2 :mapping :x]))))])

;; To place whole poses side by side, use `pj/arrange`:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width))
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width))])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Each sub-pose inside `pj/arrange` can have its own data, mapping,
;; layers, and options -- they are independent plots tiled into a
;; single rendered image.

;; ## What's Next
;;
;; To start making plots, the chart-type chapters are the natural
;; next stop:
;;
;; - [**Distributions**](./plotje_book.distributions.html), [**Ranking**](./plotje_book.ranking.html), [**Change Over Time**](./plotje_book.change_over_time.html), and [**Relationships**](./plotje_book.relationships.html) -- recipes for common plotting goals
;;
;; To go deeper into the model:
;;
;; - [**Composition**](./plotje_book.composition.html) -- composite poses, shared scales, and multi-panel patterns
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) -- where options live and how scope determines what they reach
;; - [**Pose Rules**](./plotje_book.pose_rules.html) -- the formal rules, each with tested assertions; reference to return to, not required reading
