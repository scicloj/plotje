;; # Options and Scopes
;;
;; Plotje has three kinds of values you set, each answering
;; a different question:
;;
;; - **Mappings** -- *what does this layer draw from, and how?* Its
;;   aesthetics (`:color`, `:size`, `:group`), and its **layer
;;   settings**: the parameters of its layer type (`:bandwidth`,
;;   `:bins`), its data, its position adjustment. A layer's mapping
;;   describes that layer; aesthetics and `:data` can also be written
;;   in the pose's mapping, to reach every layer. The word "mapping"
;;   names both the column-to-aesthetic pairs and the map they are
;;   written in -- see [Mapping](./plotje_book.glossary.html#mapping).
;; - **Plot options** -- *what describes this plot as a whole?*
;;   Its title and labels, its coordinate system, and how its
;;   panels share their axes. Per-plot by nature; there is one of
;;   each per plot.
;; - **Configuration** -- *what are the rendering defaults?*
;;   Palette, theme, default dimensions, color scale. Shared
;;   across every plot you render; any one plot can override a
;;   configuration key.
;;
;; **Configuration holds the values that can meaningfully have a
;; cross-plot default.** A title cannot -- each plot needs its own.
;; A palette can -- a whole project can share one. A layer's color
;; mapping is per-layer, but when several layers should share the
;; mapping, you set it at a wider scope.

;; ## Setup

(ns plotje-book.options-and-scopes
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; A helper to inspect pose structure. It prints `:mapping`,
;; `:layers`, and `:opts` -- everything except the `:data` field,
;; which is omitted for readability.

(defn strip-data [pose]
  (cond-> (dissoc pose :data)
    (:layers pose) (update :layers (partial mapv #(dissoc % :data)))
    (:poses pose) (update :poses (partial mapv strip-data))))

(defn pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose)))

;; ## Mappings
;;
;; The final argument of a `pj/lay-*` function is a map, the layer's
;; **mapping**. A layer's mapping describes that layer, and it takes
;; two kinds of key.
;;
;; **Aesthetics** say how the layer maps data to visuals: `:color`,
;; `:size`, `:alpha`, `:shape`, `:group`, plus mark-specific keys like
;; `:text` and `:fill`. An aesthetic is either mapped from a column
;; (`:color :species`) or given as a constant (`:alpha 0.3`).
;;
;; **Layer settings** are the other keys of that map. A layer setting
;; says how the layer is computed or drawn:
;;
;; - **The parts of the layer type** -- the rendering recipe is a mark
;;   (point, line, bar, ...), a stat (identity, linear-model, density,
;;   binning, ...), and a position adjustment (dodge, stack, fill).
;;   The layer type is chosen by which `pj/lay-*` function you call,
;;   and the `:mark`, `:stat`, and `:position` keys override its
;;   parts.
;; - **Layer-type parameters** -- keys specific to one layer type,
;;   like `:bandwidth` for `pj/lay-density` or `:bins` for
;;   `pj/lay-histogram`.
;; - **Data** -- a per-layer `:data` key, if the layer should
;;   use a different dataset from the rest of the pose.
;;
;; All of these are written in the one map passed to `pj/lay-*`. In
;; the printed pose, the parameters of the layer type stay in the
;; layer's `:mapping` beside its aesthetics, while `:mark`, `:stat`,
;; `:position` and `:data` are stored on the layer next to its
;; `:mapping`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-smooth {:stat :loess :bandwidth 0.5})
    pose-summary)

(kind/test-last
 [(fn [m]
    (and (= :loess (get-in m [:layers 0 :stat]))
         (= {:bandwidth 0.5} (get-in m [:layers 0 :mapping]))
         ;; :data is stored on the layer, outside its :mapping
         (let [layer (-> (rdatasets/datasets-iris)
                         (pj/pose :sepal-length :sepal-width)
                         (pj/lay-point {:data (rdatasets/datasets-iris)})
                         :layers
                         first)]
           (and (contains? layer :data)
                (not (contains? (:mapping layer) :data))))))])

;;
;; An aesthetic written in the mapping of `pj/lay-*`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; And the pose structure:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species})
    pose-summary)

(kind/test-last
 [(fn [m]
    (= :species (get-in m [:layers 0 :mapping :color])))])

;; ### Scope: writing an aesthetic for several layers
;;
;; An aesthetic can also be written at a wider **scope** when it
;; should apply to more than one layer: `pj/pose` takes a mapping
;; too, and a pose's mapping takes aesthetics and `:data`. The
;; parameters of a layer type and `:mark`, `:stat` and `:position` are
;; written on the layer itself. For a single-panel plot the scope
;; hierarchy has two levels, from narrow to broad:
;;
;; | Scope  | Set via              | Reaches                    |
;; |:-------|:---------------------|:---------------------------|
;; | Layer  | `pj/lay-*` mapping   | that one layer             |
;; | Pose   | `pj/pose` mapping    | every layer on this pose  |
;;
;; When the same key is set at both scopes, the narrower one wins.
;;
;; Composite poses introduce a third level: an outer pose's
;; mapping flows into each of its descendant leaves, where it
;; combines with the leaf's own mapping. See
;; [Pose Rule S1](./plotje_book.pose_rules.html#rule-s1-mapping-scope-is-a-tree-walk-merge-narrower-wins)
;; for an example.
;;
;; The layer type itself is chosen per layer -- you pick which
;; `pj/lay-*` to call. [Core Concepts](./plotje_book.core_concepts.html#scope)
;; teaches where layers go and how values combine at each scope;
;; [Pose Rules](./plotje_book.pose_rules.html#scope) S1 to S6 state the
;; rules.

;; ## Plot Options
;;
;; Plot options describe a plot as a whole: its title, labels,
;; coordinate system, and how its panels share their axes. A
;; single-panel plot has
;; one of each. A composite has a scope for them: every cell is a
;; pose, so each cell can have its own title and its own scales, and
;; the options written on the composite itself apply to every cell
;; that does not set them -- except the title, subtitle and caption,
;; which an arrangement draws once, above and below its cells.
;;
;; Two functions are dedicated to plot options, and write them to the
;; pose's `:opts` field:
;;
;; - `pj/options` -- plot text (title, subtitle, caption, axis
;;   and legend labels) and panel dimensions. It also accepts
;;   configuration keys as per-plot overrides (see Configuration
;;   below).
;; - `pj/coord` -- coordinate system (cartesian, flipped, polar,
;;   fixed).
;;
;; `pj/facet` splits the plot into panels. A facet
;; by category is a mapping: the column is written into the pose's
;; `:mapping` under `:col` or `:row`. A facet over columns,
;; `(pj/facet pose {:y [:hwy :cty]})`, is kept under the pose's
;; `:facet` key. `pj/facet` takes a map of plot options as a last
;; argument and passes that map to `pj/options`. `pj/arrange` takes
;; the same map of plot options as its last argument.
;;
;; `pj/scale` does not write to `:opts`: a scale belongs to the
;; aesthetic it reads, so it is written into that aesthetic's mapping.
;; It takes an axis (`:x`, `:y`) or a visual
;; aesthetic. Axis scales accept `:linear`, `:log`, `:categorical`; the
;; continuous visual ones (`:size`, `:alpha`, `:fill`, `:color`) accept
;; `:linear` and `:log`; `:shape` accepts `:categorical`. `:group` has
;; no scale to set -- it splits a layer into one drawn group per value
;; and draws nothing of its own -- so `(pj/scale pose :group ...)`
;; reports an error.

;; Reference lines and shaded bands -- `pj/lay-rule-h`,
;; `pj/lay-rule-v`, `pj/lay-band-h`, `pj/lay-band-v` -- are layers,
;; not plot options. They are added like any other layer. On a pose
;; with one panel the rule or band is drawn on that panel; columns
;; named in the call that the panel does not draw are not read there,
;; and a warning says so. On a composite pose the rule or band is added
;; at the root and placed as any layer is (Rules LP2 and LP3 in
;; [Pose Rules](./plotje_book.pose_rules.html)): with no columns named,
;; it is drawn in every cell whose panel draws two columns, and with
;; columns named, in every cell whose panel draws no other column; where
;; no cell qualifies, it is drawn in every cell.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/options {:title "Iris"})
    (pj/coord :flip))

(kind/test-last [(fn [v] (some #{"Iris"} (:texts (pj/svg-summary v))))])

;; And the pose structure:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/options {:title "Iris"})
    (pj/coord :flip)
    pose-summary)

(kind/test-last
 [(fn [m]
    (and (= "Iris" (get-in m [:opts :title]))
         (= :flip (get-in m [:opts :coord]))))])

;; Both the title and the coordinate system landed in `:opts`. On a
;; single-panel plot like this one they describe the whole plot;
;; written on one cell of a composite they would describe that cell.
;;
;; On a faceted plot, a scale has two parts:
;;
;; - Scale **type** (log, categorical, linear, and so on) is shared
;;   across all panels -- if you set `pj/scale :x :log` on a
;;   faceted pose, every panel has a log x-axis.
;; - Scale **domain** (the extent of values shown) is shared as well,
;;   so the panels can be compared. `:share-scales` names the axes the
;;   panels share -- `{:share-scales #{:x}}` gives each panel its own
;;   `:y` domain, and `{:share-scales #{}}` its own on both axes -- and
;;   the appearance aesthetics keep one domain either way.
;;
;; `pj/scale` and `pj/coord`
;; write at the pose they are called on and flow down from there, so
;; called on the pose you are building they cover the whole plot, and
;; called on one cell before the cells are arranged, that cell alone.
;; A scale written in a mapping is the same setting written closer to
;; the layer, and the two combine key by key. What has no spelling yet is a
;; type that varies across the panels of a facet: those panels come
;; from one pose, so they share it.
;;
;; Other chapters call these values *plot-level options*. *Plot-level*
;; here names a category, while *pose-level* (used for mappings)
;; names a place in a scope hierarchy -- the shared word "level"
;; refers to different things. Putting a plot option inside a
;; `pj/lay-*` mapping -- for example `{:title "Growth"}` -- is a
;; category mistake: plot options belong in `:opts` via their
;; dedicated functions above.
;;
;; A scale is not a plot option at all. `pj/scale` writes the
;; mapping the scale reads, so neither `{:x-scale {:type :log}}`
;; nor any other `*-scale` key belongs in `pj/options` or in a
;; mapping. Write `(pj/scale pose :x :log)`, or say it in the
;; mapping as `{:x {:column :year :scale :log}}`.

;; ## Configuration
;;
;; Configuration controls the rendering defaults -- palette,
;; theme, default dimensions, color scale. A configuration value
;; is resolved at render time from a layered stack of sources,
;; from highest priority to lowest:
;;
;; 1. **Plot options** on the pose (`pj/options`) -- a
;;    per-plot override that wins for that one plot.
;; 2. **Thread-local overrides** via `pj/with-config`, for what is
;;    rendered inside its body. A pose returned from the body is drawn
;;    under the configuration in effect when it is shown, so end the
;;    body in `pj/plot` to keep the override.
;; 3. **Global overrides** via `pj/set-config!`.
;; 4. **Project file** (`plotje.edn`), if present.
;; 5. **Library defaults** -- the baseline included with
;;    Plotje.
;;
;; Sources 2-5 sit outside any specific pose and carry across
;; every plot you render. Source 1 lets a specific pose override a
;; configuration key for itself --
;; `(pj/options {:color-values :dark2})` sets `:color-values` on one
;; pose, and at render time wins over any palette set through the
;; other four sources.
;;
;; The [Configuration](./plotje_book.configuration.html)
;; chapter covers each source in depth and lists every
;; configuration key.
;;
;; `pj/config` returns the resolved configuration -- the merged
;; result of all five sources above.

(select-keys (pj/config) [:width :height :margin])

(kind/test-last
 [(fn [m]
    (and (number? (:width m))
         (number? (:height m))
         (number? (:margin m))))])

;; ## A Worked Example
;;
;; The pose below touches two categories explicitly. The third,
;; configuration, shows up at render time.

(def demo
  (-> (rdatasets/datasets-iris)
      (pj/pose :sepal-length :sepal-width)
      ;; mapping: a color aesthetic at layer scope
      (pj/lay-point {:color :species})
      ;; plot options: stored in :opts
      (pj/options {:title "Iris measurements"})
      (pj/coord :flip)))

;; The rendered plot:

demo

(kind/test-last [(fn [v] (some #{"Iris measurements"} (:texts (pj/svg-summary v))))])

;; The pose structure:

(pose-summary demo)

(kind/test-last
 [(fn [m]
    (and
     ;; layer-scope mapping
     (= :species (get-in m [:layers 0 :mapping :color]))
     ;; plot options
     (= "Iris measurements" (get-in m [:opts :title]))
     (= :flip (get-in m [:opts :coord]))))])

;; Reading the summary:
;;
;; - `:color :species` is inside the layer's mapping -- an aesthetic
;;   at layer scope.
;; - `:title` and `:coord` are in `:opts` -- plot options, which on
;;   this single-panel plot describe the whole of it.
;; - Configuration does not appear in the pose. `(pj/config)`
;;   supplies the theme, default dimensions and other defaults when
;;   the plot is drawn.

;; ## Coming from ggplot2
;;
;; For readers familiar with [ggplot2](https://ggplot2.tidyverse.org/),
;; the three categories map roughly as follows:
;;
;; | ggplot2 construct                            | Plotje category       |
;; |:---------------------------------------------|:----------------------------|
;; | `geom_*(aes(...))`                           | a layer's mapping           |
;; | `ggplot(aes(...))`                           | a pose's mapping            |
;; | `+ geom_*()`                                 | a layer                     |
;; | `+ coord_*()`                                | plot option                 |
;; | `+ scale_*()`                                | a pose's mapping: the aesthetic's `:scale` (`pj/scale`) |
;; | `+ facet_*()`                                | a pose's mapping: the panel aesthetics `:col` and `:row` (`pj/facet`); a facet over columns is kept beside the mapping, under `:facet` |
;; | `+ labs(...)`, one-off `+ theme(...)`        | plot option                 |
;; | `theme_set(...)`, `options(...)`             | configuration               |

;; In ggplot2, these constructs are reached via separate function
;; families (`labs()`, `theme()`, `coord_*()`, `scale_*()`). Plotje
;; folds the plot-level concerns into one entry point: `pj/options`
;; accepts title, axis labels, theme, legend position, dimensions,
;; and other settings as keys in a single map. `pj/scale`, `pj/coord`,
;; and `pj/facet` remain dedicated functions because each carries
;; more structure, but everything else merges.
;;
;; [ggplot2 Equivalents](./plotje_book.ggplot2_equivalents.html)
;; gives the Plotje spelling of each of these ggplot2 constructs, one
;; row each.

;; ## See Also
;;
;; - [Core Concepts](./plotje_book.core_concepts.html) --
;;   the scope hierarchy for mappings in full.
;; - [Pose Rules](./plotje_book.pose_rules.html) --
;;   precise rules for each pose-world function: `pj/pose`,
;;   `pj/lay-*`, `pj/arrange`, `pj/options`, `pj/scale`, `pj/coord`,
;;   `pj/facet`, each rule with tested assertions.
;; - [Configuration](./plotje_book.configuration.html) --
;;   the configuration sources and every configuration key.
;; - [Glossary](./plotje_book.glossary.html) -- definitions
;;   of mapping, layer setting, plot option, and configuration.
