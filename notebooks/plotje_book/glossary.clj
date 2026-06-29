;; # Glossary
;;
;; Key terms used throughout Plotje, with brief definitions
;; and code examples.

(ns plotje-book.glossary
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; clojure2d -- color palettes and gradients
   [clojure2d.color :as c2d]))

;; ## Pose
;;
;; A **pose** is the composable value in Plotje. A leaf pose
;; describes one plot panel; a composite pose contains other poses
;; arranged together. Every pose-shaping function in the API
;; (`pj/pose`, `pj/lay-*`, `pj/facet`, `pj/arrange`, `pj/options`,
;; `pj/scale`, `pj/coord`) takes a pose and returns a pose.
;; `pj/->pose` is the polymorphic lift -- it accepts raw data or a
;; pose and returns a pose, so any of the output functions can
;; start from a dataset directly.
;; Poses auto-render in
;; [Kindly](https://scicloj.github.io/kindly-noted/)-compatible
;; tools like [Clay](https://scicloj.github.io/clay/).

(def my-pose
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/options {:title "Iris"})))

my-pose

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; A pose is a plain Clojure value -- printed, the same value reveals
;; its underlying map shape with `:data`, `:mapping`, `:layers`, and
;; `:opts`:

(kind/pprint my-pose)

(kind/test-last
 [(fn [pose]
    (and (some? (:data pose))
         (= :sepal-length (get-in pose [:mapping :x]))
         (= :sepal-width (get-in pose [:mapping :y]))
         (= :species (get-in pose [:layers 0 :mapping :color]))
         (= "Iris" (get-in pose [:opts :title]))))])

;; ## Leaf Pose
;;
;; A **leaf pose** is a pose that describes a single plot panel.
;; It carries `:data`, a `:mapping` from columns to aesthetics, and
;; `:layers` -- the chart-type layers attached to it. Created by
;; `pj/pose` or `pj/lay-*`.

;; ## Composite Pose
;;
;; A **composite pose** is a pose that contains other poses under
;; `:poses` plus an optional `:layout`. Created by `pj/arrange`. Its
;; leaves render independently and are tiled into the final plot.
;;
;; Some features are not yet exposed through `pj/arrange` -- unequal
;; weights and nested composite cells. To use them, build the
;; composite as a literal map; `pj/pose` accepts the literal form.

;; ## Arrange
;;
;; `pj/arrange` builds a composite pose from a sequence of poses.
;; Each input becomes one of the composite's `:poses`; the
;; composite tiles them via `:layout`. It accepts `:cols`, `:title`,
;; `:width`, `:height`, and `:share-scales`. For features it does
;; not yet expose -- unequal weights and nested composite cells --
;; pass a literal map to `pj/pose`.

;; ## Layer Type
;;
;; A **layer type** is the bundle of mark + stat + position that
;; determines how data becomes a visual element. It is a context-free
;; recipe; placing it on a pose produces a *layer* (next entry).
;; See the [Layer Types](./plotje_book.layer_types.html) chapter for
;; detailed tables of all built-in layer types, marks, stats, and
;; positions.

;; ## Layer
;;
;; A **layer** is a layer type placed on a pose, optionally with
;; scoped mappings. Created by `pj/lay-*`.
;;
;; Layers attach to poses in three ways, depending on what you
;; pass to `pj/lay-*`:
;;
;; - **Bare** -- `pj/lay-*` without columns attaches the layer
;;   using the current pose's mapping (inherited from `pj/pose`
;;   or a prior `pj/lay-*`).
;; - **Matching columns** -- `pj/lay-*` with columns that match the
;;   most recent matching leaf reuses that leaf, so the new layer
;;   joins the existing panel.
;; - **Non-matching columns** -- `pj/lay-*` with columns that do not
;;   match any existing leaf creates a fresh leaf pose with the
;;   layer attached.

(-> my-pose :layers first :layer-type)

(kind/test-last [(fn [k] (= :point k))])

;; ## Mark
;;
;; The **mark** is the visual shape shown for each data point or
;; group. Several layer types may share the same mark -- for
;; instance, `:line` and `:smooth` both produce lines, and `:area`
;; and `:density` both produce filled regions.
;; See the [Layer Types](./plotje_book.layer_types.html) chapter for
;; a table of all built-in marks.

;; ## Stat
;;
;; A **stat** (statistical transform) processes raw data before
;; rendering. Each stat takes data-space inputs and produces the
;; geometry that its mark will show.
;; See the [Layer Types](./plotje_book.layer_types.html) chapter for
;; a table of all built-in stats.

;; ## Position
;;
;; A **position** adjustment determines how overlapping marks are
;; placed in coordinate space: kept at their data values
;; (`:identity`), dodged side-by-side along a categorical band
;; (`:dodge`), stacked end-to-end so bar tops sit on the previous
;; bar's top (`:stack`), or normalized to fill `[0, 1]` proportions
;; (`:fill`). Position runs between stat computation and rendering.
;; You can override the default position by passing `:position` in
;; the layer options.
;; When multiple layers share `:position :dodge`, they are coordinated
;; together -- error bars automatically align with bars.
;; See the [Layer Types](./plotje_book.layer_types.html) chapter for
;; a table of all built-in positions.

(def tips {:day ["Mon" "Mon" "Tue" "Tue"]
           :count [30 20 45 15]
           :meal ["lunch" "dinner" "lunch" "dinner"]})

(-> tips
    (pj/lay-bar :day :count {:color :meal :position :stack}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          dinner-bar (-> tips
                         (pj/lay-bar :day :count
                                     {:color :meal :position :stack})
                         pj/plan
                         (get-in [:panels 0 :layers 0 :groups 1]))]
      (and (= 4 (:polygons s))
           ;; Stacking lifts dinner bars off the baseline -- their
           ;; y0 sits on top of the lunch bar's y1.
           (every? pos? (:y0s dinner-bar)))))])

;; ## Draft
;;
;; A **draft** is the record produced by `pj/draft`. For a leaf
;; pose, it is a `LeafDraft` carrying `:layers` (a vector of flat
;; maps, one per applicable layer with merged pose-and-layer scope)
;; and `:opts` (the pose-level options that flow into the plan
;; stage). For a composite pose, it is a `CompositeDraft` carrying
;; per-leaf drafts plus composite layout geometry. Draft layers carry all the
;; information the plan stage needs: data, columns, mark, stat,
;; color, grouping.
;;
;; `pj/draft` is useful for inspecting exactly what the plan stage
;; will consume before any domains, ticks, or coordinate math are
;; computed.
;;
;; Keys prefixed with double underscores (e.g. `:__panel-idx`) are
;; internal markers used by later stages and follow the Clojure
;; convention "do not consume." Ignore them when reading a draft.

(-> my-pose pj/draft kind/pprint)

(kind/test-last [(fn [d] (and (pj/leaf-draft? d)
                              (= 1 (count (:layers d)))
                              (= :point (:mark (first (:layers d))))))])

;; ## Draft Layer
;;
;; A **draft layer** is one entry of a draft's `:layers` vector --
;; a single map that bundles the layer type, the merged mappings
;; (pose + layer scopes), and the effective dataset for one (leaf,
;; applicable-layer) pair. It is the specification of what the
;; renderer will draw for that layer, before any geometry, domains,
;; or ticks are computed. The plan layer (entry below) is the same
;; idea after geometry has been resolved.

(-> my-pose pj/draft :layers first kind/pprint)

(kind/test-last
 [(fn [d]
    (and (some? (:data d))
         (= :sepal-length (:x d))
         (= :sepal-width (:y d))
         (= :species (:color d))
         (= :point (:mark d))))])

;; ## Mapping
;;
;; A **mapping** maps a column (or a literal value) to an
;; aesthetic. Aesthetics come in two groups:
;;
;; - **Positional aesthetics** (`:x`, `:y`, plus `:x-end`, `:x-min`,
;;   `:x-max`, `:y-min`, `:y-max` for marks that need them) place
;;   marks in coordinate space.
;; - **Appearance aesthetics** (`:color`, `:size`, `:alpha`, `:shape`,
;;   `:text`, `:fill`) shape how each mark looks.
;;
;; Mappings live on a pose -- where they flow into every layer
;; attached to it -- or on a single layer, where they scope to that
;; layer alone. Lower scope wins on conflict; an explicit `nil`
;; cancels a mapping inherited from above.

;; ## Aesthetic
;;
;; An **aesthetic** is a property of a mark that can be mapped to a
;; data column or fixed to a literal value. Plotje supports two
;; groups:
;;
;; **Positional aesthetics** -- where the mark sits:
;;
;; | Key | Controls | Column type |
;; |:----|:---------|:------------|
;; | `:x` | Horizontal position | Numerical, temporal, or categorical |
;; | `:y` | Vertical position | Numerical, temporal, or categorical |
;; | `:x-end`, `:x-min`, `:x-max` | Range endpoints (interval, band, rule marks) | Same type as `:x` |
;; | `:y-min`, `:y-max` | Range endpoints (band, ribbon) | Same type as `:y` |
;;
;; **Appearance aesthetics** -- how the mark looks:
;;
;; | Key | Controls | Column type |
;; |:----|:---------|:------------|
;; | `:color` | Fill/stroke color | Categorical or numerical |
;; | `:size` | Point radius | Numerical |
;; | `:alpha` | Opacity | Numerical |
;; | `:shape` | Point shape | Categorical |
;; | `:text` | Label content | Any |
;; | `:fill` | Tile gradient color | Numerical |
;;
;; When a keyword is passed, it maps to a dataset column.
;; A literal value (e.g., `"#E74C3C"`, `"red"`, `0.5`) sets a fixed
;; aesthetic for all points.
;;
;; A single layer can mix all three: positional column refs (`:x`,
;; `:y`), an appearance column ref (`:color :species`,
;; `:size :petal-length`), and a literal appearance (`:alpha 0.7`,
;; the same opacity for every point):

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width
                  {:color :species :size :petal-length :alpha 0.7}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 150 (:points s))
           ;; :alpha 0.7 is a literal -- every point gets the same
           ;; opacity, so the rendered set has a single alpha value.
           (= #{0.7} (:alphas s)))))])

;; ## Group
;;
;; A **group** is a subset of data that is processed and rendered
;; together. Mapping `:color` to a categorical column automatically
;; creates groups -- one per unique value. You can also create groups
;; without color using the `:group` key.

(-> (rdatasets/datasets-iris)
    (pj/lay-line :sepal-length :sepal-width {:group :species}))

(kind/test-last
 [(fn [v]
    (let [groups (-> (rdatasets/datasets-iris)
                     (pj/lay-line :sepal-length :sepal-width
                                  {:group :species})
                     pj/plan
                     (get-in [:panels 0 :layers 0 :groups]))]
      (and (= 3 (:lines (pj/svg-summary v)))
           ;; One group per :species value: the plan layer's
           ;; :groups vector has 3 entries.
           (= 3 (count groups))
           (= ["setosa" "versicolor" "virginica"]
              (mapv :label groups)))))])

;; ## Nudge
;;
;; A **nudge** shifts data coordinates by a constant offset.
;; It is orthogonal to position -- you can nudge within a dodge,
;; or nudge at identity. Applied via `:nudge-x` and `:nudge-y`
;; keys in the layer options.

(-> {:x [1 2 3] :y [4 5 6]}
    (pj/lay-point :x :y {:nudge-x 0.5}))

(kind/test-last
 [(fn [v]
    (let [xs (-> {:x [1 2 3] :y [4 5 6]}
                 (pj/lay-point :x :y {:nudge-x 0.5})
                 pj/plan
                 (get-in [:panels 0 :layers 0 :groups 0 :xs]))]
      (and (= 3 (:points (pj/svg-summary v)))
           ;; The original xs were [1 2 3]; nudge-x 0.5 shifts each
           ;; by 0.5 before the scale is applied.
           (= [1.5 2.5 3.5] xs))))])

;; ## Jitter
;;
;; **Jitter** adds random offsets in drawing units to reduce
;; overplotting. Unlike position and nudge, jitter operates after
;; scaling (not in data space) and is deterministic -- seeded by a
;; hash of the group's color so repeated renders produce identical
;; output.
;;
;; On categorical x-axes, jitter is applied along the band axis only.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-length {:jitter true}))

(kind/test-last [(fn [v] (and (pj/pose? v)
                              (pos? (:points (pj/svg-summary v)))))])

;; ## Inference
;;
;; **Inference** is the automatic selection of a layer type
;; (mark + stat + position) when you bypass `pj/lay-*` and pass
;; columns to `pj/pose`. Plotje picks a layer type based on
;; column types: numerical x and y defaults to `:point`, categorical
;; x with numerical y to `:boxplot`, a single numerical column to
;; `:histogram`, and so on. Use `:x-type` / `:y-type` on a pose or
;; layer to override the detected type.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width))

(kind/test-last [(fn [v] (pos? (:points (pj/svg-summary v))))])

;; ## Plan
;;
;; A **plan** is the fully resolved intermediate representation --
;; a plain Clojure map containing everything needed to render a
;; plot: data-space geometry, domains, tick info, legend, layout
;; dimensions. No membrane types, no datasets, no scale objects.
;;
;; Created with `pj/plan`. Numeric arrays (`:xs`, `:ys`, etc.) are
;; [dtype-next](https://github.com/cnuernber/dtype-next) buffers for
;; efficiency -- they print with their length and a small preview
;; rather than every element.

(def my-plan (pj/plan my-pose))

(kind/pprint my-plan)

(kind/test-last
 [(fn [plan]
    (and (vector? (:panels plan))
         (= 1 (count (:panels plan)))
         (= 600 (:width plan))
         (= 400 (:height plan))
         (some? (:legend plan))))])

;; ## Panel
;;
;; A **panel** is a single plotting area within a plan. It contains
;; x/y domains, scale specs, tick info, coordinate type, and layers.
;; A simple plot has one panel; `pj/facet` and `pj/facet-grid`
;; produce multiple.

(kind/pprint (first (:panels my-plan)))

(kind/test-last
 [(fn [p]
    (and (= :cartesian (:coord p))
         (= [4.12 8.08] (:x-domain p))
         (= 1 (count (:layers p)))))])

;; ## Plan Layer
;;
;; A **plan layer** is the resolved descriptor inside a plan panel:
;; resolved mark type, style, and groups of data-space geometry.
;; The user-level layer becomes the plan layer through `pj/plan`.

(kind/pprint (get-in my-plan [:panels 0 :layers 0]))

(kind/test-last
 [(fn [layer]
    (and (= :point (:mark layer))
         (= 3 (count (:groups layer)))
         (every? :xs (:groups layer))))])

;; ## Dataset
;;
;; A **dataset** is the tabular data backing a plot. Plotje uses
;; [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset)
;; datasets internally -- column-oriented, dtype-next-backed tables.
;; The most ergonomic way to create and manipulate them is the
;; [Tablecloth](https://scicloj.github.io/tablecloth/) API, which is
;; a Clojure-idiomatic wrapper over `tech.ml.dataset`.
;;
;; Raw input (a map of `{column-name [values]}`, a sequence of
;; row-maps, or a CSV/URL string) is coerced via
;; `tablecloth.api/dataset` at construction time. The dataset lives
;; on a pose under `:data`; layers can override with their own
;; `:data`, and inside a composite each pose in `:poses` can carry
;; its own `:data` too.

;; ## Pipeline
;;
;; The **pipeline** is the five-stage flow from user code to
;; rendered output: `pose -> draft -> plan -> membrane -> plot`.
;; A pose is what you compose; `pj/draft` flattens it into a draft
;; (a `LeafDraft` or `CompositeDraft` record); `pj/plan` resolves
;; geometry and layout; the membrane stage turns the plan into
;; drawable primitives; the plot is the terminal SVG hiccup or PNG
;; output. See the [Architecture](./plotje_book.architecture.html)
;; chapter for the per-stage details.

;; ## Sub-plot
;;
;; A **sub-plot** is one resolved entry of a composite pose's
;; `:poses`, in the plan. Where a leaf pose's plan carries
;; `:panels` (one per faceted variant), a composite pose's plan
;; carries `:sub-plots` (one per inner pose), each with its own
;; nested `:plan` map. The compositor reads `:sub-plots` and tiles
;; their rendered membranes into the final canvas.

;; ## Resolve Tree
;;
;; The **resolve tree** is the scope-merge walk that propagates a
;; root pose's `:mapping`, `:opts`, and root-attached `:layers`
;; downward into every descendant leaf during plan construction.
;; Lower (narrower) scopes override higher ones; root-attached
;; layers reach every applicable leaf in the tree.

;; ## Domain
;;
;; A **domain** is the range of data values along an axis.
;;
;; - Numerical: `[min max]`, where `min`/`max` are the raw data
;;   extent extended by 5% on each side so points do not sit on
;;   the panel edge. For `my-pose` above, sepal-length runs from
;;   `4.3` to `7.9` in the data; the x-domain becomes
;;   `[4.12 8.08]`.
;; - Temporal: same `[min max]` form, but the values are
;;   epoch-milliseconds (Plotje converts `LocalDate`,
;;   `LocalDateTime`, `Instant`, and `java.util.Date` automatically).
;;   Tick labels stay calendar-aware.
;; - Categorical: a vector of distinct values **in the order they
;;   first appear in the data**, not alphabetical (e.g., iris
;;   gives `["setosa" "versicolor" "virginica"]`).
;;
;; Each panel carries its own `:x-domain` and `:y-domain`. With
;; `:scales :shared` faceting, all panels share one domain pair;
;; with `:scales :free` (or `:free-x`/`:free-y`), each panel
;; computes its own.

(let [p (first (:panels my-plan))]
  {:x-domain (:x-domain p)
   :y-domain (:y-domain p)})

(kind/test-last
 [(fn [m]
    (and (= [4.12 8.08] (:x-domain m))
         (= 2 (count (:y-domain m)))
         (number? (first (:y-domain m)))))])

;; ## Clip
;;
;; **Clipping** bounds a panel's marks to the panel, so geometry that
;; runs past the axis domain -- a line beyond a narrowed scale, a
;; point at the very edge -- is masked at the panel boundary rather
;; than painting outside it (and, in a multi-panel layout, into a
;; neighbour). It is the visual counterpart of the **Domain**: the
;; domain names the window, the clip enforces it. A narrowed domain
;; therefore acts as a view window -- the data is kept, only the view
;; is bounded.
;;
;; Data marks clip to the drawing area (the grey panel background);
;; rug ticks, which sit in the axis margin by design, clip to the
;; wider panel box -- the panel rectangle including that margin. The
;; clip is realized in the **Membrane** stage as a scissor (a
;; `membrane.ui/scissor-view`, a Membrane primitive that masks its
;; contents to a rectangle), so every backend that honours it clips
;; identically. `pj/svg-summary` reports the clip regions as `:clips`
;; -- one per panel for the data marks, plus one per panel that also
;; carries a margin mark such as rug.

(:clips (pj/svg-summary (pj/plot my-pose)))

(kind/test-last [(fn [n] (= 1 n))])

;; ## Tick
;;
;; A **tick** is an axis mark with a label at a domain value. Ticks
;; are chosen at layout time to fit the axis length in drawing
;; units -- label widths, minimum spacing, and calendar boundaries
;; (for temporal axes) all feed into the selection. Each panel in
;; the plan carries its own `:x-ticks` and `:y-ticks` maps with
;; parallel `:values` and `:labels` vectors.

(-> my-plan :panels first :x-ticks)

(kind/test-last
 [(fn [m]
    (and (vector? (:values m))
         (vector? (:labels m))
         (= (count (:values m)) (count (:labels m)))
         (false? (:categorical? m))))])

;; ## Scale
;;
;; A **scale** maps data values to positions in drawing units.
;; Built from a domain and an output range using
;; [wadogo](https://github.com/scicloj/wadogo).
;;
;; | Type | Use |
;; |:-----|:----|
;; | `:linear` | Numerical data (default) |
;; | `:log` | Orders-of-magnitude data |
;; | `:categorical` | Distinct categories (band scale) |
;;
;; Scales are created at render time, not stored in the plan.
;; The plan stores scale *specs* (`:type`, `:domain`).
;;
;; **Temporal columns** (`LocalDate`, `LocalDateTime`, `Instant`,
;; `java.util.Date`) are automatically detected and treated as
;; numerical. Tick labels are calendar-aware -- snapped to year,
;; month, day, or hour boundaries depending on the time span.

;; ## Coord
;;
;; A **coord** (coordinate system) defines how data-space maps to
;; drawing units.
;;
;; | Type | Behavior |
;; |:-----|:---------|
;; | `:cartesian` | Standard: x rightward, y upward |
;; | `:flip` | Swap x and y axes |
;; | `:polar` | Radial: x as angle, y as radius |
;; | `:fixed` | Equal aspect ratio: 1 data unit = 1 data unit |

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/coord :flip))

(kind/test-last
 [(fn [v]
    (and (= 3 (:polygons (pj/svg-summary v)))
         (= :flip
            (-> (rdatasets/datasets-iris)
                (pj/lay-bar :species)
                (pj/coord :flip)
                pj/plan
                (get-in [:panels 0 :coord])))))])

;; ## Facet
;;
;; A **facet** splits data into multiple panels by a categorical
;; column. Each panel shows a subset of the data.
;;
;; - `pj/facet` creates a row or column of panels
;; - `pj/facet-grid` creates a row-by-column grid from two columns
;;
;; By default all panels share the same x and y domains, derived
;; from the full dataset (`:scales :shared`). To let each panel use
;; its own data range, set `{:scales :free}` (or `:free-x`/`:free-y`)
;; in `pj/options`.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          n-panels (count
                    (:panels (pj/plan
                              (-> (rdatasets/datasets-iris)
                                  (pj/lay-point :sepal-length :sepal-width)
                                  (pj/facet :species)))))]
      (and (= 3 (:panels s))
           (= 3 n-panels))))])

;; ## Annotation
;;
;; An **annotation** is a reference mark layered on a plot --
;; horizontal/vertical lines (rules) or shaded bands. Today,
;; positions live in the layer's `:mapping` slot
;; (`:y-intercept` or `:x-intercept` for rules; `:y-min`/`:y-max`
;; or `:x-min`/`:x-max` for bands) as literal values rather than
;; column references, and a single annotation draws at exactly
;; one place. Data-driven annotations (column refs in those slots,
;; producing one mark per row, like ggplot2's
;; `geom_hline(aes(yintercept = ...))`) are planned but not yet
;; implemented.
;;
;; Annotations are regular layers, so they attach under the same
;; three cases as any `lay-*`: bare call sits on the pose, matching
;; columns join the most recent matching leaf, non-matching columns
;; create a new leaf.
;;
;; | Constructor | What |
;; |:------------|:-----|
;; | `pj/lay-rule-v` | Vertical line at x = x-intercept |
;; | `pj/lay-rule-h` | Horizontal line at y = y-intercept |
;; | `pj/lay-band-v` | Vertical shaded region from x = x-min to x = x-max |
;; | `pj/lay-band-h` | Horizontal shaded region from y = y-min to y = y-max |

(def annotated
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/lay-rule-h {:y-intercept 3.0})))

annotated

(kind/pprint (nth (:layers annotated) 1))

(kind/test-last
 [(fn [layer]
    (and (= :rule-h (:layer-type layer))
         (= 3.0 (get-in layer [:mapping :y-intercept]))))])

;; ## Legend
;;
;; A **legend** is generated automatically when a color (or shape)
;; aesthetic maps to a data column. It appears in the plan as a
;; `:legend` key containing entries with labels and colors.
;; Position is controlled via `{:legend-position :bottom}` in options.

(kind/pprint (:legend my-plan))

(kind/test-last
 [(fn [leg]
    (and (map? leg)
         (= :species (:title leg))
         (= 3 (count (:entries leg)))
         (= ["setosa" "versicolor" "virginica"]
            (mapv :label (:entries leg)))))])

;; ## Theme
;;
;; A **theme** controls the visual appearance of non-data elements.
;; It is a nested map under `:theme` with three keys:
;;
;; | Key | Controls |
;; |:----|:---------|
;; | `:bg` | Panel background color |
;; | `:grid` | Gridline color |
;; | `:font-size` | Base font size in pixels |
;;
;; Passed as `{:theme {...}}` via `pj/options`, `pj/with-config`, or
;; `pj/set-config!`. Other visual settings (margins, legend width, tick
;; spacing) are top-level configuration keys, not theme entries --
;; see `pj/config-key-docs`.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:theme {:bg "#2d2d2d" :grid "#444444" :font-size 10}}))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Membrane
;;
;; A **membrane** is a `PlotjeMembrane` -- a record carrying a tree
;; of layout and drawing primitives (`Translate`, `WithColor`,
;; `RoundedRectangle`, `Label`, etc.) sized to a complete plot
;; canvas. The record itself implements the
;; [Membrane](https://github.com/phronmophobic/membrane) library's
;; UI protocols (`IOrigin`, `IBounds`, `IChildren`), so it composes
;; with other Membrane elements and any Membrane consumer can use
;; it without special-casing.
;;
;; Plotje produces a membrane via `pj/plan->membrane` (single-step
;; transition from a plan) or `pj/membrane` (composition shortcut
;; from a pose). The record carries `:width` and `:height` as
;; fields (read via `(membrane.ui/width m)`/`(membrane.ui/height m)`),
;; and the title as `:plotje/title`. Direct renderers (e.g., Plotly)
;; skip the membrane entirely. The
;; [Membranes](./plotje_book.membranes.html) chapter walks the stage
;; in depth.

(def my-membrane (pj/plan->membrane my-plan))

;; A complete membrane is large -- one drawable per data point on
;; top of axes, gridlines, ticks, and labels. The record's children
;; (the underlying drawable tree) for `my-plan`:

(kind/pprint my-membrane)

(kind/test-last
 [(fn [m]
    (let [walk-text (fn walk [d]
                      (cond
                        (string? (:text d)) (:text d)
                        (:drawable d) (walk (:drawable d))
                        (:drawables d) (some walk (:drawables d))))
          drawables (membrane.ui/children m)
          texts (mapv walk-text drawables)]
      (and (pj/membrane? m)
           (= 9 (count drawables))
           ;; The first four top-level entries carry the title,
           ;; axis labels, and legend title.
           (= ["Iris" "sepal width" "sepal length" "species"]
              (vec (take 4 texts))))))])

;; ## Plot
;;
;; A **plot** is the final rendered output -- the result of rendering
;; a plan to a specific format. For SVG, the plot is hiccup markup
;; wrapped in `kind/hiccup`.
;;
;; Created by `pj/plot` or by auto-rendering a pose.

(def my-plot (pj/plan->plot my-plan :svg {}))

;; The plot is hiccup -- a vector starting with `:svg` followed by
;; an attribute map and the plot's drawable elements. Wrapped in
;; `kind/hiccup`, it renders as the same picture we saw at the
;; top of this chapter:

(kind/hiccup my-plot)

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= :svg (first my-plot))
           (= 150 (:points s))
           (= 600.0 (double (:width s))))))])

;; ## Palette
;;
;; A **palette** is an ordered set of colors used for categorical
;; aesthetics. When `:color` maps to a categorical column, colors
;; are assigned from the active palette in order.
;;
;; Plotje uses [clojure2d](https://github.com/Clojure2D/clojure2d)
;; for palettes. Set via `{:palette :set2}` in options:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette :set2}))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; clojure2d provides thousands of named palettes -- the count below
;; is the size of the catalogue:

(count (c2d/find-palette #".*"))

(kind/test-last [(fn [n] (<= 5000 n))])

;; ## Gradient
;;
;; A **gradient** (or color scale) maps a continuous numeric range
;; to a smooth color ramp. Used when `:color` maps to a numerical
;; column.
;;
;; Common gradients: `:viridis`, `:inferno`, `:plasma`,
;; `:magma`. Diverging gradients center on a midpoint value.
;; Set via `{:color-scale :inferno}` in options:

(-> {:x (range 50) :y (range 50) :c (range 50)}
    (pj/lay-point :x :y {:color :c})
    (pj/options {:color-scale :inferno}))

(kind/test-last
 [(fn [v]
    (and (= 50 (:points (pj/svg-summary v)))
         (= :inferno
            (:color-scale
             (:legend (pj/plan
                       (-> {:x (range 50) :y (range 50) :c (range 50)}
                           (pj/lay-point :x :y {:color :c})
                           (pj/options {:color-scale :inferno}))))))))])

;; ## Configuration
;;
;; **Configuration** controls rendering behavior -- dimensions, theme,
;; palette, color scale, margins, and more. It is one of three option
;; scopes -- the others are [plot options](#plot-options) and
;; [layer options](#layer-options). Configuration follows a
;; precedence chain:
;;
;; plot options > `pj/with-config` > `pj/set-config!` > `plotje.edn` > library defaults
;;
;; `plotje.edn` is an optional file in your project root that provides
;; project-level defaults (e.g., a consistent palette or theme across all plots).
;;
;; See the Configuration chapter for details. The active configuration
;; is itself a Clojure map -- `pj/config` returns a snapshot:

(select-keys (pj/config) [:width :height :theme :palette :color-scale])

(kind/test-last
 [(fn [m]
    (and (number? (:width m))
         (number? (:height m))
         (map? (:theme m))))])

;; ## Plot Options
;;
;; **Plot options** are per-plot settings passed to `pj/options`,
;; `pj/plan`, or `pj/plot`. They include text content (title,
;; subtitle, caption, axis labels) and a nested `:config` override.
;; Unlike configuration keys, plot options are inherently per-plot --
;; a title does not make sense as a global default.
;;
;; See `pj/plot-option-docs` for the full list, or the
;; [Configuration](./plotje_book.configuration.html) chapter for usage examples.

(sort (keys pj/plot-option-docs))

(kind/test-last
 [(fn [ks]
    (and (= 14 (count ks))
         (some #{:title :subtitle :caption :x-label :y-label} ks)))])

;; ## Layer Options
;;
;; **Layer options** are per-layer settings passed in the options map
;; of layer functions (`pj/lay-point`, `pj/lay-histogram`, etc.).
;; They control aesthetics (`:color`, `:size`, `:alpha`, `:shape`),
;; grouping (`:group`), position adjustment (`:position`), and
;; layer-type-specific parameters (`:bandwidth`, `:confidence-band`,
;; `:normalize`, etc.).
;;
;; Four keys are universal -- accepted by every layer -- and each
;; layer type may accept additional keys. The
;; [Layer Types](./plotje_book.layer_types.html) chapter lists which
;; options each layer type accepts. See also `pj/layer-option-docs`
;; for descriptions, or inspect a specific layer type with
;; `pj/layer-type-lookup`.

(sort (keys pj/layer-option-docs))

(kind/test-last
 [(fn [ks]
    (and (pos? (count ks))
         (some #{:color :size :alpha :group :position} ks)))])

;; ## Tooltip and Brush
;;
;; A **tooltip** shows data values on hover. A **brush** enables
;; click-and-drag selection that highlights a rectangular region.
;; Both are JavaScript-based interactions added to the SVG output.
;;
;; Enabled via `{:tooltip true}` and `{:brush true}` in options:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:tooltip true :brush true}))

(kind/test-last
 [(fn [pose]
    (let [s (str (pj/plot pose))]
      (and (re-find #"data-tooltip" s)
           (re-find #"nsk-brush-sel" s))))])

;; ## Summary Table
;;
;; | Term | What | Key functions |
;; |:-----|:-----|:-------------|
;; | Pose | Composable value: data + mapping + layers (+ inner poses) | All `pj/` functions return poses |
;; | Leaf pose | Pose describing one plot panel | `pj/pose`, `pj/lay-*` with columns |
;; | Composite pose | Pose containing other poses (in `:poses`) and a layout | `pj/arrange` |
;; | Mapping | Column-to-aesthetic association on a pose or layer | `pj/pose` mapping, `pj/lay-*` options |
;; | Layer | Layer type attached to a pose, optionally with scoped mappings | `pj/lay-*` |
;; | Dataset | Tabular data backing a plot -- a `tech.ml.dataset`, conveniently built and manipulated via the Tablecloth API | `:data` slot, `pj/with-data` |
;; | Pipeline | Five-stage flow `pose -> draft -> plan -> membrane -> plot` | Architecture chapter |
;; | Sub-plot | One resolved entry of a composite pose's `:poses`, in the plan | `:sub-plots` in plan |
;; | Resolve tree | Scope-merge walk: root mappings propagate to every leaf | Internal to `pj/plan` |
;; | Draft | `LeafDraft` or `CompositeDraft` record carrying merged layer maps and pose-level options | `pj/draft`, automatic during `pj/plan` |
;; | Draft layer | One element of a draft: layer type + merged mappings + data | Element of `pj/draft` output |
;; | Layer type | Mark + stat + position bundle | `pj/layer-type-lookup`, `pj/lay-*` |
;; | Mark | Visual shape: point, line, bar, area, ... | Key in layer-type map |
;; | Stat | Data transform: identity, bin, count, linear-model, density, ... | Key in layer-type map |
;; | Position | How overlapping marks are placed: identity, dodge, stack, fill | Key in layer-type map |
;; | Inference | Auto-choosing mark/stat from column types | When `pj/lay-*` is omitted |
;; | Aesthetic | Mark property bindable to a column: positional (x, y, ...) or appearance (color, size, alpha, ...) | Key in mapping or layer |
;; | Group | Subset of data rendered together | From `:color` or `:group` |
;; | Plan | Fully resolved plot description | `pj/plan` |
;; | Panel | One plotting area (domain, ticks, layers) | One or more per plan |
;; | Plan layer | Resolved geometry + style for one mark | Inside plan panels |
;; | Domain | Data range on an axis | Part of panel |
;; | Tick | Axis mark with label at a domain value | Part of panel |
;; | Scale | Data-to-drawing-units mapping (linear, log, categorical) | `pj/scale` |
;; | Coord | Coordinate system (cartesian, flip, polar, fixed) | `pj/coord` |
;; | Facet | Split into panels by a categorical column | `pj/facet`, `pj/facet-grid` |
;; | Arrange | Compose multiple poses into a grid | `pj/arrange` |
;; | Share scales | Make sibling poses of a composite share data ranges across named axes | `:share-scales` in composite `:opts` |
;; | Annotation | Reference marks (rules, bands); positions in `:mapping` as literal values today, data-driven planned | `pj/lay-rule-*`, `pj/lay-band-*` |
;; | Legend | Color/size/alpha key from aesthetic mappings | Automatic in plan |
;; | Plot options | Title, subtitle, caption, labels, dimensions | `pj/options` |
;; | Layer options | Per-layer aesthetics and layer-type parameters | `pj/lay-*` options map |
;; | Theme | Visual styling: background, grid, fonts | `:theme` in `pj/options` |
;; | Palette | Ordered color set for categorical aesthetics | `:palette` in `pj/options` |
;; | Gradient | Continuous color ramp for numerical mappings | `:color-scale` in `pj/options` |
;; | Configuration | Global rendering defaults | `pj/config`, `pj/set-config!`, `pj/with-config` |
;; | Membrane | `PlotjeMembrane` record -- a Membrane UI component carrying the drawable tree, plan-derived dimensions, and `:plotje/title` | `pj/membrane`, `pj/plan->membrane` |
;; | Plot | Final output (SVG hiccup) | `pj/plot`, `pj/save` |
;; | Tooltip / Brush | JavaScript hover and selection interactions | `{:tooltip true}` in options |
