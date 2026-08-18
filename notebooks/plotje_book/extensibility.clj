;; # Extensibility
;;
;; Plotje is built on multimethods -- open dispatch points that let
;; you add new marks, statistics, scales, coordinate systems, and
;; output formats without modifying the core library.
;;
;; This notebook catalogs every multimethod, shows its dispatch
;; mechanism, lists the built-in implementations, and demonstrates
;; how to extend each one.

(ns plotje-book.extensibility
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth and dtype-next -- dataset and buffer operations, for the
   ;; custom stat below
   [tablecloth.api :as tc]
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Layer-type registry -- for inspecting layer-type data
   [scicloj.plotje.layer-type :as layer-type]
   ;; Implementation namespaces -- for extension points
   [scicloj.plotje.impl.resolve :as resolve]
   [scicloj.plotje.impl.stat :as stat]
   [scicloj.plotje.impl.extract :as extract]
   [scicloj.plotje.render.mark :as mark]
   [scicloj.plotje.render.svg :as svg]
   [scicloj.plotje.impl.render :as render]
   ;; Membrane UI protocols -- read membrane width/height
   [membrane.ui]))

;; ## Overview
;;
;; The pipeline from pose to plot has five stages, with the
;; transitions between them governed by multimethods or fixed-
;; function steps. The dotted `plan->plot` edge is an alternative
;; direct path -- skipping the membrane stage -- that backends can
;; register against if they build their figure directly from plan
;; data.

^:kindly/hide-code
(kind/mermaid "
graph LR
  B[\"Pose\"] -->|pj/pose->draft| D[\"Draft\"]
  D -->|pj/draft->plan| P[\"Plan\"]
  P -->|pj/plan->membrane| M[\"Membrane\"]
  M -->|pj/membrane->plot| F[\"Plot\"]
  P -.->|pj/plan->plot| F
  style B fill:#d1c4e9
  style D fill:#e8f5e9
  style P fill:#fff3e0
  style M fill:#e3f2fd
  style F fill:#fce4ec
")

;; | Multimethod | Namespace | Dispatches on | Purpose |
;; |:------------|:----------|:--------------|:--------|
;; | `compute-stat` | `impl/stat.clj` | `:stat` key | Transform data (identity, bin, count, lm, loess, kde, boxplot) |
;; | `extract-layer` | `impl/extract.clj` | `:mark` key | Convert a stat result into a plan layer descriptor |
;; | `layer->membrane` | `render/mark.clj` | `:mark` key | Render a plan layer as membrane drawables |
;; | `mark-clip-region` | `render/mark.clj` | `:mark` key | Name the panel region a mark's geometry clips to |
;; | `plan->plot` | `impl/render.clj` | format keyword | Convert a plan directly into a figure (the direct path) |
;; | `membrane->plot` | `impl/render.clj` | format keyword | Convert a membrane tree into a figure (the membrane path) |
;; | `make-scale` | `impl/scale.clj` | domain type + spec | Build a wadogo scale |
;; | `make-coord` | `impl/coord.clj` | coord-type keyword | Build a coordinate function |
;; | `apply-position` | `impl/position.clj` | position keyword | Adjust group layout (dodge, stack, fill) |

;; ## `compute-stat`
;;
;; Transforms raw data into a statistical summary. Each layer type
;; uses a stat to prepare data for rendering.
;;
;; Dispatch function: `(fn [draft-layer] (or (:stat draft-layer) :identity))`

(kind/table
 {:column-names ["Dispatch value" "What it does"]
  :row-maps
  (->> (methods stat/compute-stat)
       keys
       (filter keyword?)
       (remove #{:default})
       sort
       (mapv (fn [k] {"Dispatch value" (kind/code (pr-str k))
                      "What it does" (pj/stat-doc k)})))})

(kind/test-last [(fn [t] (= 11 (count (:row-maps t))))])

;; ### A stat from end to end
;;
;; Before any reference material, one complete stat. It computes a running
;; maximum -- for each group, the largest y seen so far, which is the shape
;; of a record-to-date line. Everything a stat has to do is here: read the
;; layer it is handed, return the geometry, and declare what the axes must
;; cover.
;;
;; It is written the way the numeric code in this library is written. The x
;; column passes through untouched, because a dataset column is already a
;; buffer the renderer can read. `dfn/cummax` performs the scan as one
;; vectorized operation instead of a sequence walk. The domains come from
;; `dfn/reduce-min` and `dfn/reduce-max` over the concatenated buffers. A
;; stat that copied its columns into Clojure vectors on the way through
;; would work, and would allocate a second copy of every value for nothing.

(defmethod stat/compute-stat :running-max [{:keys [data x y group]}]
  (let [subsets (if (seq group)
                  (vals (tc/group-by data group {:result-type :as-map}))
                  [data])
        points (mapv (fn [ds]
                       (cond-> {:xs (ds x)
                                :ys (dfn/cummax (ds y))}
                         ;; A grouped stat names each group by the value it
                         ;; was split on. extract-layer turns that name into
                         ;; a color and a legend entry.
                         (seq group) (assoc :color (first (ds (first group))))))
                     subsets)
        all-xs (dtype/concat-buffers (map :xs points))
        all-ys (dtype/concat-buffers (map :ys points))]
    {:points points
     :x-domain [(dfn/reduce-min all-xs) (dfn/reduce-max all-xs)]
     :y-domain [(dfn/reduce-min all-ys) (dfn/reduce-max all-ys)]}))

(defmethod stat/compute-stat [:running-max :doc] [_]
  "Running maximum -- the largest y seen so far")

;; Nothing else is needed -- no mark, no extractor, no renderer.
;; `pj/lay-line` already knows how to draw a group of `:xs` and `:ys`.
;;
;; (These examples end in `pj/plot` rather than leaving a pose to render
;; itself. A pose renders lazily, and this stat is removed a few forms
;; below; drawing now, while the method exists, keeps them reproducible.)

(def rainfall
  {:month [1 2 3 4 5 6 7 8 9 10 11 12]
   :rain [42 30 55 20 61 48 35 70 25 58 44 66]})

(-> rainfall
    (pj/lay-point :month :rain {:color "#bbbbbb"})
    (pj/lay-line :month :rain {:stat :running-max})
    (pj/options {:title "Rainfall and its running maximum"})
    pj/plot)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 12 (:points s))
                                (= 1 (:lines s)))))])

;; Grouping needs no extra work either. `:group` arrives already worked out,
;; and one group in gives one line out:

(-> {:month (concat (range 1 13) (range 1 13))
     :rain [42 30 55 20 61 48 35 70 25 58 44 66
            10 33 21 40 18 52 29 47 60 22 38 55]
     :city (concat (repeat 12 "north") (repeat 12 "south"))}
    (pj/lay-line :month :rain {:stat :running-max :color :city})
    (pj/options {:title "Running maximum per city"})
    pj/plot)

(kind/test-last [(fn [v] (= 2 (:lines (pj/svg-summary v))))])

;; Cleanup -- remove the example stat, and confirm it is gone:

(do (remove-method stat/compute-stat :running-max)
    (remove-method stat/compute-stat [:running-max :doc])
    (contains? (methods stat/compute-stat) :running-max))

(kind/test-last [false?])

;; ### Where a layer's stat comes from
;;
;; `compute-stat` dispatches on a layer's `:stat`, which is settled before
;; it is called. Three cases decide it, and reading them as one rule
;; explains a behaviour that otherwise looks arbitrary.
;;
;; This helper reports what a pose's first layer resolves to:

(defn mark-and-stat
  "The mark and stat a pose's first layer resolves to."
  [pose]
  (-> pose
      pj/draft
      :layers
      first
      resolve/resolve-draft-layer
      (select-keys [:mark :stat])))

;; **The layer type names the stat.** `(layer-type/lookup :histogram)`
;; carries `:stat :bin`, so a histogram bins whatever it is given:

(layer-type/lookup :histogram)

(kind/test-last [(fn [m] (= :bin (:stat m)))])

;; **The layer type names a mark but no stat.** `(layer-type/lookup :bar)`
;; fixes `:mark :rect` and leaves `:stat` empty:

(layer-type/lookup :bar)

(kind/test-last [(fn [m] (and (= :rect (:mark m)) (nil? (:stat m))))])

;; The mark is already decided, so the column-type rules below do not run.
;; A narrower rule fills the gap instead, and it turns on one thing only --
;; whether a y column was given. Without one, the bar counts its
;; categories; with one, it takes that y as the bar height:

{"lay-bar with x only" (mark-and-stat (-> (rdatasets/datasets-iris)
                                          (pj/lay-bar :species)))
 "lay-bar with x and y" (mark-and-stat (-> {:city ["north" "south"] :rain [42 30]}
                                           (pj/lay-bar :city :rain)))}

(kind/test-last
 [(fn [m] (= {"lay-bar with x only" {:mark :rect :stat :count}
              "lay-bar with x and y" {:mark :rect :stat :identity}}
             m))])

;; So `pj/lay-bar` is two charts under one name. That is why the shapes
;; table below lists `:rect` twice: a counting bar reads one shape, a value
;; bar reads another.

;; **Nothing names a mark at all.** When no `lay-*` function has chosen a
;; layer type -- a bare `pj/pose`, or raw data handed straight to `pj/plot`
;; -- both mark and stat are inferred from the column types:

{"categorical x, numerical y" (mark-and-stat (-> (rdatasets/datasets-iris)
                                                 (pj/pose :species :sepal-width)))
 "numerical x, numerical y" (mark-and-stat (-> (rdatasets/datasets-iris)
                                               (pj/pose :sepal-length :sepal-width)))
 "categorical x only" (mark-and-stat (-> (rdatasets/datasets-iris)
                                         (pj/pose :species)))
 "numerical x only" (mark-and-stat (-> (rdatasets/datasets-iris)
                                       (pj/pose :sepal-length)))}

(kind/test-last
 [(fn [m] (= {"categorical x, numerical y" {:mark :boxplot :stat :boxplot}
              "numerical x, numerical y" {:mark :point :stat :identity}
              "categorical x only" {:mark :rect :stat :count}
              "numerical x only" {:mark :bar :stat :bin}}
             m))])

;; An explicit `{:stat ...}` on a layer overrides all three, which is what
;; makes the cross-pairings later in this section possible.
;;
;; #### What `:identity` is
;;
;; `:identity` turned up in all three cases above -- named by a layer type,
;; filled in for a mark that named none, and the value the dispatch
;; function itself falls back to.
;;
;; `:identity` means *the data is already what should be plotted*. It
;; applies no statistical transform, but it still does the preparation
;; every layer needs: it drops rows missing an x, a y or a mapped aesthetic
;; value, computes the domains, and splits the rows into groups. What comes
;; out is one entry per surviving row, at the values the dataset holds.
;;
;; It is what a scatter uses:

(layer-type/lookup :point)

(kind/test-last [(fn [m] (= :identity (:stat m)))])

;; Choosing it explicitly is how a caller says "do not aggregate this".
;; That is the case a bar chart of pre-computed totals is in -- and it is
;; what `pj/lay-bar` resolves to on its own as soon as a y column is
;; present, which is the rule from a few forms above seen from the other
;; side.

;; (The other fields in those lookups belong to the layer type rather
;; than to the stat: `:x-only` says the layer type works from an x column
;; alone, `:accepts` lists the layer options it takes beyond the
;; universal ones, tabulated per layer type in
;; [Layer Types](./plotje_book.layer_types.html#layer-type-specific-options),
;; and `:varies` names the appearance aesthetics its mark varies from row
;; to row -- `:point` declares `{:size :radius :alpha :opacity}`. A
;; aesthetic left out of `:varies` is one the mark draws once for the
;; whole layer, so a column mapped to it varies nothing, and the plan
;; says so rather than drawing a legend for an encoding the panel does
;; not carry.)
;;
;; One entry per row is visible in the output: a scatter of the 150-row
;; iris dataset draws 150 marks.

(def grouped-scatter
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})))

(-> grouped-scatter pj/plot pj/svg-summary :points)

(kind/test-last [(fn [n] (= 150 n))])

;; ### What a stat receives
;;
;; A stat is handed one layer, as a map. It is not the draft layer
;; `pj/draft` returns: planning first infers the column types, resolves each
;; aesthetic to either a column name or a fixed value, and works out the
;; grouping, so that a stat can read those decisions instead of repeating
;; them. `resolve/resolve-draft-layer` is the step that makes them, and a
;; stat is never called with anything less -- exercising one by hand means
;; resolving first.

(def resolved-layer
  (-> grouped-scatter pj/draft :layers first resolve/resolve-draft-layer))

;; The keys a stat reads, on that layer (`:data` holds the dataset itself
;; and is left out here so the rest prints readably):

(-> resolved-layer
    (select-keys [:x :y :x-type :y-type :group :color :size :alpha :fixed-color]))

(kind/test-last
 [(fn [m] (= {:x :sepal-length :y :sepal-width
              :x-type :numerical :y-type :numerical
              :group [:species] :color :species
              :size nil :alpha nil :fixed-color nil}
             m))])

;; Reading those:
;;
;; - `:x`, `:y` -- resolved column names
;; - `:x-type`, `:y-type` -- `:numerical` or `:categorical`
;; - `:group` -- the columns to split the data by, as a vector; empty when
;;   the layer is ungrouped. A stat that ignores this produces one group
;;   where the user asked for several.
;; - `:color`, `:size`, `:alpha` -- a column name when that aesthetic maps a
;;   column, `nil` when it does not. A constant lives in `:fixed-color` and
;;   its siblings instead, and is applied later, so a stat can ignore it.
;; - `:cfg` -- resolved configuration, where a stat finds its own options,
;;   such as `:kde-bandwidth` for the density stats

;; ### What a stat returns
;;
;; Two keys are required of every stat, whatever it computes:
;;
;; - `:x-domain` -- the extent the x axis must cover: `[lo hi]` for a
;;   numerical axis, or the sequence of categories for a categorical one
;; - `:y-domain` -- the same for y
;;
;; Alongside them the stat returns at least one **geometry shape**: a key
;; whose value follows a structure that some `extract-layer` method knows
;; how to read. The shape, not the stat's name, is what decides which marks
;; can draw the result.

;; | Shape | Carries | Produced by | Read by |
;; |:------|:--------|:------------|:--------|
;; | `:points` | a vector of point groups | `:identity`, `:linear-model`, `:loess`, `:density`, `:summary`, `:count` | the ten point-reading marks: `:point`, `:line`, `:step`, `:area`, `:text`, `:rug`, `:lollipop`, `:errorbar`, `:pointrange`, `:interval-h` |
;; | `:bins` | histogram bins, per group | `:bin` | `:bar` |
;; | `:bars` with `:categories` | a count per category, per group | `:count` | `:rect`, when counting |
;; | `:boxes` with `:categories` | a five-number summary per category | `:boxplot` | `:boxplot` |
;; | `:violins` with `:categories` | a density profile per category | `:violin` | `:violin`, `:ridgeline` |
;; | `:tiles` with `:fill-range` | a grid of cells and the range of their fill values | `:bin2d`, `:density-2d` | `:tile`, `:contour` |
;;
;; `:rect` and `:tile` each read a second shape as well: both fall back to
;; `:points` when their own shape is absent. For `:rect` that is not a
;; special case but the two-stat rule from above -- a counting bar gets
;; `:count` and so reads `:bars`, while a value bar gets `:identity` and so
;; reads `:points`.
;;
;; A stat may also return more than one shape, when its result means
;; something to more than one family of marks. Two built-ins do:
;; `:linear-model` returns `:points` for the fitted line and `:ribbons` for
;; its confidence band, and `:count` returns `:bars` for the bar marks and
;; `:points` for the point-reading ones. Some stats return values that are
;; neither domain nor geometry: `:bin` reports `:max-count`, and
;; `:density-2d` reports the `:grid` it estimated over.
;;
;; The grouped scatter's stat, computed by hand:

(def scatter-stat
  (-> resolved-layer
      (assoc :cfg {})
      stat/compute-stat))

(sort (keys scatter-stat))

(kind/test-last [(fn [ks] (= [:points :x-domain :y-domain] ks))])

;; ### The `:points` shape
;;
;; Most stats speak `:points` and most marks read it. It is a **vector of
;; groups** -- one per combination of the grouping columns, so three
;; species give three groups -- and each group is a map of parallel
;; sequences.

(count (:points scatter-stat))

(kind/test-last [(fn [n] (= 3 n))])

;; One group, with its sequences cut to three values so that it prints
;; readably:

(-> scatter-stat
    :points
    first
    (update :xs #(vec (take 3 %)))
    (update :ys #(vec (take 3 %)))
    (update :row-indices #(vec (take 3 %))))

(kind/test-last [(fn [g] (and (= "setosa" (:color g))
                              (= [5.1 4.9 4.7] (:xs g))))])

;; Every group carries:
;;
;; - `:xs`, `:ys` -- parallel sequences of data-space coordinates, one entry
;;   per row in that group. The two must be the same length as each other;
;;   groups need not be the same length as one another. Any indexed
;;   sequence works, but a numeric buffer is what the library uses and what
;;   a stat should return: dataset columns here, and the result of
;;   `dfn/cummax` in the running-max stat above. A stat with no y to report,
;;   such as `:bin` or `:count`, synthesizes one rather than omitting `:ys`.
;; - `:row-indices` -- where each entry sat in the original dataset, which
;;   is what lets a tooltip name the row it came from.
;;
;; The rest appear only when the group has something to put in them. Most
;; are per-row aesthetics, attached by the preparation step `:identity`
;; runs from the columns the layer maps -- which is why
;; `{:size :petal-length}` means something on a scatter and nothing on a
;; histogram, since a stat that aggregates its input has no per-row values
;; left to attach. A stat that computes a value per point may fill one in
;; itself instead, as `:count` does for `:labels`:
;;
;; | Key | Present when | Read by |
;; |:----|:-------------|:--------|
;; | `:color` | the layer is grouped | every mark, to choose the group's color |
;; | `:labels` | the layer maps `:text`, or the stat computes one per point | `:text` |
;; | `:ymins`, `:ymaxs` | `:y-min` and `:y-max` map columns | `:errorbar`, `:pointrange` |
;; | `:sizes` | `:size` maps a column | `:point` |
;; | `:alphas` | `:alpha` maps a column | `:point` |
;; | `:shapes` | `:shape` maps a column | `:point` |
;; | `:color-values` | `:color` maps a numerical column | `:point`, `:interval-h` |
;; | `:x-ends` | `:x-end` maps a column | `:interval-h` |

;; #### `:color` in a stat result is a category, not a color
;;
;; A group's `:color` holds the **category value** the group was split on
;; -- the string `"setosa"` -- and not a color. Nothing is resolved against a palette until
;; `extract-layer` runs, which is where the same key changes meaning: the
;; plan layer's `:color` is a resolved color, and the category moves to
;; `:label`.

[(-> scatter-stat :points first :color)
 (-> grouped-scatter pj/plan :panels first :layers first :groups first
     (select-keys [:color :label]))]

(kind/test-last [(fn [[stat-color plan-group]]
                   (and (= "setosa" stat-color)
                        (= "setosa" (:label plan-group))
                        (vector? (:color plan-group))))])

;; ### Which marks read which shape
;;
;; The stat and the extractor are two halves of one contract, but they are
;; not paired one to one. Most of the mark extractors read `:points`, and
;; most of those go through one shared reader, `extract-xy-groups`, which
;; is why they agree about what a group means. So a stat returning
;; `:points` is readable by any of them, not only by the layer type it was
;; written for -- and an explicit `{:stat ...}` is how a caller says so.
;;
;; A density drawn as a line, rather than as the filled area
;; `pj/lay-density` would give:

(-> (rdatasets/datasets-iris)
    (pj/lay-line :sepal-length {:stat :density}))

(kind/test-last [(fn [v] (= 1 (:lines (pj/svg-summary v))))])

;; And a per-category mean drawn as bare points, rather than as the
;; point-with-error-bar `pj/lay-summary` would give -- three species, three
;; points:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-width {:stat :summary}))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; The converse holds too: a mark handed a shape it does not read finds
;; nothing to draw. Four
;; extractors guard against this -- `:bar`, `:boxplot`, `:violin` and
;; `:ridgeline` -- and raise an error naming the key they wanted. The
;; `:points` readers do not guard, so they produce a layer with no groups,
;; and the plot comes out missing that layer rather than failing.
;;
;; To write a stat in a shape other than `:points`, read the built-in stat
;; and extractor that already produce and consume it as a matched template:
;; `:bin` with `:bar`, `:boxplot` with `:boxplot`, `:bin2d` with `:tile`.

;; ## `extract-layer`
;;
;; Converts a stat result into a plan layer descriptor -- a plain
;; map with data-space geometry and resolved colors. This is the half of
;; the contract that reads the shapes tabulated above, and where a
;; category value becomes a color.
;;
;; Dispatch function: `(fn [draft-layer stat all-colors cfg] (:mark draft-layer))`

(kind/table
 {:column-names ["Dispatch value" "Output"]
  :row-maps
  (->> (methods extract/extract-layer)
       keys
       (filter keyword?)
       (remove #{:default})
       sort
       (mapv (fn [k] {"Dispatch value" (kind/code (pr-str k))
                      "Output" (pj/mark-doc k)})))})

(kind/test-last [(fn [t] (= 17 (count (:row-maps t))))])

;; A plan layer looks like this. Starting from a familiar iris
;; scatter:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; The plan layer extracted from that pose:

(let [s (-> (rdatasets/datasets-iris)
            (pj/lay-point :sepal-length :sepal-width {:color :species})
            pj/plan)
      layer (first (:layers (first (:panels s))))]
  layer)

(kind/test-last [(fn [m] (and (= :point (:mark m))
                              (number? (get-in m [:style :opacity]))))])

;; ## `layer->membrane`
;;
;; Renders a plan layer descriptor into membrane drawable primitives.
;; This is the "membrane path" -- used when the target format goes through
;; membrane (e.g., SVG).
;;
;; Dispatch function: `(fn [layer ctx] (:mark layer))`

(kind/table
 {:column-names ["Dispatch value" "Membrane output"]
  :row-maps
  (->> (methods mark/layer->membrane)
       keys
       (filter keyword?)
       (remove #{:default})
       sort
       (mapv (fn [k] {"Dispatch value" (kind/code (pr-str k))
                      "Membrane output" (pj/membrane-mark-doc k)})))})

(kind/test-last [(fn [t] (= 17 (count (:row-maps t))))])
;;
;; ### Drawing an aesthetic the layer type declares it varies
;;
;; A layer type says which appearance aesthetics its mark varies from
;; row to row under `:varies`, and that declaration earns the layer a
;; legend. Drawing them is the renderer's half, and it has to agree
;; with the legend: both read the column's values through the same
;; scale, and a mark that spreads the values itself instead will draw
;; sizes its own legend does not explain.
;;
;; `layer-type/channel-magnitude-fn` is that shared function. It turns
;; a value from the layer's per-row buffers into the quantity the mark
;; draws -- a radius, a width, an opacity:

(def bubble-layer
  (-> {:x [1 2 3] :y [1 2 3] :n [1 4 9]}
      (pj/lay-point :x :y {:size :n})
      pj/plan
      :panels first :layers first))

(let [groups (:groups bubble-layer)
      radius-of (layer-type/channel-magnitude-fn bubble-layer :size
                                                 (keep :sizes groups))]
  (mapv radius-of [1 4 9]))

(kind/test-last
 [(fn [radii]
    ;; The ends of the size range, which is what the legend explains.
    (= [2.0 8.0] [(first radii) (last radii)]))])

;; It answers `identity` where the mapping asked for the column's
;; values as they stand (`{:scale false}`), so a renderer applies the
;; result either way, and nil where the layer carries nothing to draw.
;;
;; A mark that declares an aesthetic and then draws no per-row values
;; for it is reported at `pj/plan`, naming the mark and the aesthetic.

;; ### How to extend: add a new mark type
;;
;; Adding a new mark (e.g., `:area` for area charts) requires methods
;; on both `extract-layer` and `layer->membrane`.
;;
;; Pseudocode:
;;
;; ```clojure
;; ;; 1. Extract geometry from stat result
;; (defmethod extract/extract-layer :area [draft-layer stat all-colors cfg]
;;   {:mark :area
;;    :style {:opacity 0.5}
;;    :groups (vec (for [{:keys [color xs ys]} (:points stat)]
;;                   {:color (extract/resolve-color ...)
;;                    :xs xs :ys ys}))})
;;
;; ;; 2. Render to membrane drawables
;; (defmethod mark/layer->membrane :area [layer ctx]
;;   ;; Build filled polygon from xs/ys + baseline
;;   ...)
;; ```
;;
;; ### How to extend: register a layer type and create a pose-compatible layer function
;;
;; After defining `compute-stat` and `extract-layer` for your custom
;; mark, register a layer type and create a convenience function that
;; works with the pose API:
;;
;; ```clojure
;; ;; Register the layer type
;; (layer-type/register! :waterfall
;;   {:mark :waterfall :stat :waterfall
;;    :doc "Waterfall -- running total with increase/decrease bars."})
;;
;; ;; Users can then call:
;; ;; (pj/lay data (layer-type/lookup :waterfall))
;;
;; ;; Or create a convenience function using lay:
;; (defn lay-waterfall
;;   ([pose] (pj/lay pose (layer-type/lookup :waterfall)))
;;   ([data x y] (-> data (pj/pose x y) (pj/lay (layer-type/lookup :waterfall))))
;;   ([data x y opts] (-> data (pj/pose x y) (pj/lay (merge (layer-type/lookup :waterfall) opts)))))
;; ```
;;
;; Users can then call `(lay-waterfall data :category :amount)`.
;;
;; Note: if your custom mark is not one of the built-in marks, you also
;; need a `layer->membrane` defmethod for the SVG renderer. Without one,
;; the library throws an error explaining which defmethod to add.

;; ### Rule and band marks live on the panel's `:annotations`, not `:layers`
;;
;; Four marks -- `:rule-h`, `:rule-v`, `:band-h`, `:band-v` -- are split
;; out of the per-panel `:layers` list during planning and rendered from
;; a separate `:annotations` slot on each panel of the resolved plan.
;; Extension authors building tooling that walks a plan should expect to
;; find these on panel `:annotations`, not on panel `:layers`. The split
;; is driven by `scicloj.plotje.impl.resolve/annotation-marks`, the
;; canonical set of those mark keywords; if you add a custom mark that
;; should follow the same lifecycle, register it there.
;;
;; The `:annotations` slot holds only these four marks. The
;; [Glossary](./plotje_book.glossary.html#annotation) defines an annotation more
;; broadly -- any mark that explains a plot rather than showing data,
;; including notes and leader lines. Those are ordinary text and line
;; layers, and they stay on `:layers`.

;; ## `mark-clip-region`
;;
;; Each panel clips its marks so geometry past the domain cannot paint
;; outside the panel. The region a mark clips to is itself an extension
;; point: `mark-clip-region` (in `render/mark.clj`, dispatching on the
;; `:mark` key) names the region, and the panel renderer resolves it to
;; a rectangle and clips the mark to it. (The clip itself is a Membrane
;; scissor; the [Membranes](./plotje_book.membranes.html#clipping-marks-to-the-panel) chapter shows
;; the mechanism.)
;;
;; Two regions exist:
;;
;; - `:drawing-area` (the default) -- the grey panel background. Data
;;   marks clip here, so geometry past the domain is masked at the
;;   plotting edge and nothing spills into the axis margin.
;; - `:panel-box` -- the wider panel rectangle including the axis
;;   margin, for marks that draw in the margin on purpose. The built-in
;;   `:rug` mark uses it so its ticks are not cut at the drawing-area
;;   edge.

(mark/mark-clip-region :point)

(kind/test-last [#(= :drawing-area %)])

(mark/mark-clip-region :rug)

(kind/test-last [#(= :panel-box %)])

;; ### How to extend: a mark that draws in the margin
;;
;; A custom mark that paints in the axis margin (a marginal density, an
;; axis-edge glyph) registers a `mark-clip-region` defmethod returning
;; `:panel-box`, so the renderer does not clip it at the drawing-area
;; edge. Without this, a margin-drawing mark defaults to
;; `:drawing-area` and is cut off.

(defmethod mark/mark-clip-region :margin-glyph [_] :panel-box)

(mark/mark-clip-region :margin-glyph)

(kind/test-last [#(= :panel-box %)])

;; Cleanup -- remove the example defmethod:

(remove-method mark/mark-clip-region :margin-glyph)

(contains? (methods mark/mark-clip-region) :margin-glyph)

(kind/test-last [false?])

;; ## `plan->plot`
;;
;; Orchestrates the full path from plan to plot. The `:svg`
;; implementation goes through the membrane path: plan, then membrane,
;; then plot. Other renderers can skip membrane and go directly from
;; plan to their target format.
;;
;; Dispatch function: `(fn [plan format opts] format)`
;;
;; | Dispatch value | Path |
;; |:---------------|:-----|
;; | `:svg` | plan, then membrane, then `membrane->plot :svg` |
;; | `:bufimg` | plan, then membrane, then `membrane->plot :bufimg` (raster image) |
;;
;; Note that `pj/save`'s `:png` file format is not a separate
;; dispatch value -- it routes through the `:bufimg` path internally
;; and encodes the resulting `BufferedImage` as PNG bytes on disk.
;; The save-side keyword names the file format; the dispatch-side
;; keyword names the JVM render target.

;; Using `plan->plot` directly:

(def my-plan
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      pj/plan))

(first (pj/plan->plot my-plan :svg {}))

(kind/test-last [(fn [v] (= :svg v))])

;; The same plan can be rendered to different formats:

(def my-figure (pj/plan->plot my-plan :svg {}))

(vector? my-figure)

(kind/test-last [(fn [v] (true? v))])

;; ### How to extend: add a new direct format
;;
;; To add a Plotly renderer that reads plan data directly
;; (no membrane needed), register a `plan->plot` defmethod.
;;
;; Pseudocode:
;;
;; ```clojure
;; (ns mylib.render.plotly
;;   (:require [scicloj.plotje.impl.render :as render]))
;;
;; (defmethod render/plan->plot :plotly [plan _ opts]
;;   ;; Read plan domains, layers, legend, layout
;;   ;; Build a Plotly.js spec directly -- no membrane needed
;;   {:data (mapcat plan-layer->plotly-traces
;;                  (:layers (first (:panels plan))))
;;    :layout {:xaxis {:title (:x-label plan)}
;;             :yaxis {:title (:y-label plan)}}})
;; ```
;;
;; Then users opt in by requiring the namespace:
;;
;; ```clojure
;; (require '[mylib.render.plotly])
;; (pj/plot pose {:format :plotly})
;; ```

;; ## `membrane->plot`
;;
;; Converts a `PlotjeMembrane` into a plot for a given format. This
;; is the extensibility point for membrane-based output formats --
;; formats that consume the same drawable tree but walk it
;; differently. The [Membranes](./plotje_book.membranes.html#anatomy)
;; chapter walks the `PlotjeMembrane` record itself and the Membrane
;; UI protocols it implements.
;;
;; Dispatch function: `(fn [membrane-tree format opts] format)`
;;
;; | Dispatch value | Output |
;; |:---------------|:-------|
;; | `:svg` | SVG hiccup wrapped in `kind/hiccup` |
;; | `:bufimg` | Java BufferedImage wrapped in `kind/buffered-image` (raster) |

;; `pj/plan->membrane` builds the membrane, `pj/membrane->plot`
;; converts it. The record carries plan-derived dimensions as fields
;; and the title as `:plotje/title`, so `pj/membrane->plot` reads
;; them off the value directly:

(def my-membrane (pj/plan->membrane my-plan))

(pj/membrane? my-membrane)

(kind/test-last [(fn [v] (true? v))])

(membrane.ui/width my-membrane)

(kind/test-last [(fn [v] (number? v))])

(first (pj/membrane->plot my-membrane :svg {}))

(kind/test-last [(fn [v] (= :svg v))])

;; The shorter shortcut `pj/membrane` runs the full chain from a
;; pose to a membrane in one call -- the natural starting point for
;; a custom backend that consumes membranes:

(def shortcut-membrane
  (pj/membrane (-> (rdatasets/datasets-iris)
                   (pj/lay-point :sepal-length :sepal-width
                                 {:color :species}))))

(pj/membrane? shortcut-membrane)

(kind/test-last [(fn [v] (true? v))])

;; ### How to extend: add a new membrane-based format
;;
;; To add a format that reuses the membrane tree (e.g., Canvas, PDF),
;; register a `membrane->plot` defmethod. The defmethod reads
;; canvas dimensions via the Membrane UI protocols and the title via
;; `:plotje/title`, so it operates without the plan:
;;
;; Pseudocode:
;;
;; ```clojure
;; (ns mylib.render.canvas
;;   (:require [membrane.ui :as ui]
;;             [scicloj.plotje.impl.render :as render]
;;             [scicloj.plotje.render.membrane :as membrane]))
;;
;; (defmethod render/membrane->plot :canvas [membrane-tree _ opts]
;;   (let [w     (ui/width membrane-tree)
;;         h     (ui/height membrane-tree)
;;         title (:plotje/title membrane-tree)]
;;     ;; Walk the drawable tree (via membrane.ui/children), emit
;;     ;; canvas draw calls at the correct canvas size
;;     (canvas-walk membrane-tree w h title)))
;;
;; ;; Optionally register plan->plot for users who want a one-call
;; ;; plan-to-figure path; pj/plot, pj/membrane, and pj/save do not
;; ;; need it -- they go through plan->membrane and membrane->plot
;; ;; directly.
;; (defmethod render/plan->plot :canvas [plan _ opts]
;;   (-> plan
;;       (membrane/plan->membrane opts)
;;       (render/membrane->plot :canvas opts)))
;; ```

;; ## `make-scale`
;;
;; Builds a wadogo scale from a domain and a drawing-space range.
;;
(kind/table
 {:column-names ["Dispatch value" "Scale type"]
  :row-maps
  (->> (methods scicloj.plotje.impl.scale/make-scale)
       keys
       (filter keyword?)
       sort
       (mapv (fn [k] {"Dispatch value" (kind/code (pr-str k))
                      "Scale type" (pj/scale-doc k)})))})

(kind/test-last [(fn [t] (= 3 (count (:row-maps t))))])
;;
;; Dispatch: inferred from the domain type and scale spec.
;; Categorical domains dispatch to `:categorical`. Numerical domains default to
;; `:linear`, overridden to `:log` by `(pj/scale pose :x :log)`.

;; ## `make-coord`
;;
;; Builds a coordinate function that maps data-space (x, y) to
;; drawing units (px, py).
;;
(kind/table
 {:column-names ["Dispatch value" "Behavior"]
  :row-maps
  (->> (methods scicloj.plotje.impl.coord/make-coord)
       keys
       (filter keyword?)
       (remove #{:default})
       sort
       (mapv (fn [k] {"Dispatch value" (kind/code (pr-str k))
                      "Behavior" (pj/coord-doc k)})))})

(kind/test-last [(fn [t] (= 4 (count (:row-maps t))))])
;;
;; All four use the same scales -- `:flip` swaps which scale maps to
;; which drawing-space axis, and `:polar` maps x to angle and y to radius.

;; ## `make-inverse`
;;
;; The other direction: canvas coordinates back to data-space x and y.
;; `pj/to-data` reads it, and so does anything answering which value
;; sits under a pointer. A coordinate system needs a method here only
;; if it can be inverted coordinate by coordinate:

(->> (methods scicloj.plotje.impl.coord/make-inverse)
     keys
     (filter keyword?)
     (remove #{:default})
     sort
     vec)

(kind/test-last [(fn [ks] (= [:cartesian :fixed :flip] ks))])

;; `:polar` registers none, so it falls to the default, which returns
;; nil. A panel under a coordinate system with no inverse reports
;; `:invertible? false` in `pj/frames`, and `pj/to-data` throws for it
;; rather than answering with a position that is only sometimes the one
;; asked about. A custom coord that can be inverted should register a
;; method; one that cannot needs no action.

;; A flipped bar chart uses `:flip` coordinates:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/coord :flip))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Self-Documenting Extensions
;;
;; Every generated dispatch table in this notebook is built by
;; introspecting multimethod keys at render time. When you extend
;; a multimethod, your new dispatch value automatically appears in
;; the table. You can also register a `[:key :doc]` defmethod to
;; provide a description.
;;
;; ### Adding a documented extension
;;
;; Register both the implementation and a `[:key :doc]` defmethod:

(defmethod stat/compute-stat :quantile [draft-layer]
  {:points [] :x-domain [0 1] :y-domain [0 1]})

(defmethod stat/compute-stat [:quantile :doc] [_]
  "Quantile regression bands")

;; The doc helper picks it up immediately:

(pj/stat-doc :quantile)

(kind/test-last [(fn [v] (= "Quantile regression bands" v))])

;; ### Missing documentation degrades gracefully
;;
;; If you skip the `[:key :doc]` defmethod, the table still renders --
;; the description falls back to "(no description)" instead of
;; throwing an error. Let us remove the doc defmethod and verify:

(remove-method stat/compute-stat [:quantile :doc])

(pj/stat-doc :quantile)

(kind/test-last [(fn [v] (= "(no description)" v))])

;; ### Cleanup
;;
;; Remove the example extension so it does not affect other tests:

(remove-method stat/compute-stat :quantile)

(count (remove #{:default} (filter keyword? (keys (methods stat/compute-stat)))))

(kind/test-last [(fn [v] (= 11 v))])

;; ## Summary
;;
;; | To add... | Extend... |
;; |:----------|:----------|
;; | A new statistical transform | `compute-stat` |
;; | A new mark type | `extract-layer` + `layer->membrane` |
;; | A new output format (direct) | `plan->plot` |
;; | A new output format (membrane-based) | `membrane->plot` + `plan->plot` |
;; | A new scale type | `make-scale` |
;; | A new coordinate system | `make-coord` |
;; | A new position adjustment | `apply-position` |

;; ## Background
;;
;; - [**Architecture**](./plotje_book.architecture.html) -- the five-stage pipeline and key libraries

;; ## What's Next
;;
;; - [**Extension Example**](./plotje_book.waterfall_extension.html) -- a worked example that uses the extension points above to add a waterfall chart
;; - [**Edge Cases**](./plotje_book.edge_cases.html) -- how the library handles unusual inputs
