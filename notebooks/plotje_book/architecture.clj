;; # Architecture
;;
;; Plotje has a five-stage pipeline: **pose** -> **draft** -> **plan**
;; -> **membrane** -> **plot**. Each stage is produced from the
;; previous one by a single step. The user-facing functions
;; `pj/draft`, `pj/plan`, and `pj/plot` are literal compositions of
;; those steps, with `pj/options` folded in to inject
;; pose-level options. Because the API is built as a composition, you
;; can inspect each intermediate value and test each transition on
;; its own.
;;
;; This chapter introduces the single-step transitions, walks a small example
;; through every stage, shows the user-facing functions as
;; compositions, and explains how composite poses traverse the same
;; pipeline through internal shape dispatch.

(ns plotje-book.architecture
  (:require
   ;; Clojure string -- splitting a message into sentences
   [clojure.string :as str]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Plotje internals -- read in "The columns a layer reads" and "How Panels Share an Axis"
   [scicloj.plotje.impl.pose :as pose]
   [scicloj.plotje.impl.plan :as plan]
   ;; Membrane UI protocols
   [membrane.ui]))

;; ## Pipeline Overview

^:kindly/hide-code
(kind/mermaid "
graph LR
  X[\"Raw data\"] -->|pj/->pose| B[\"Pose\"]
  B -->|pj/options pj/lay-* ...| B
  B -->|pj/pose->draft| D[\"Draft\"]
  D -->|pj/draft->plan| P[\"Plan\"]
  P -->|pj/plan->membrane| M[\"Membrane\"]
  M -->|pj/membrane->plot| F[\"Plot\"]
  style X fill:#eee,stroke-dasharray:3 3
  style B fill:#d1c4e9
  style D fill:#e8f5e9
  style P fill:#fff3e0
  style M fill:#e3f2fd
  style F fill:#fce4ec
")

;; Two terms used throughout: **data space** is values in their
;; original units (centimeters, dollars, dates, species names);
;; **drawing space** is the drawing-unit coordinates inside the output canvas.
;; The plan stage holds geometry in data space; the membrane stage
;; holds geometry in drawing space.

;; The five stages:
;;
;; - **Pose** -- the composable specification you write. Built by
;;   `pj/pose`, `pj/lay-*`, `pj/options`, `pj/facet`, `pj/arrange`,
;;   `pj/scale`, and `pj/coord`. Lifted from raw data by `pj/->pose`,
;;   so a dataset can flow through the pipeline without an explicit
;;   constructor call. No computation has happened yet.
;;
;; - **Draft** -- the pose flattened. A `LeafDraft` record holds
;;   `:layers` (a vector of one map per applicable layer with all
;;   scope merged in -- `:data`, `:x`, `:y`, `:mark`, `:stat`, and
;;   aesthetic keys) and `:opts` (the pose-level options that flow
;;   into the plan stage). A composite pose produces a
;;   `CompositeDraft` instead, carrying per-leaf drafts
;;   (`:sub-drafts`), the resolved composite-framing geometry (`:chrome-spec`),
;;   the layout map from leaf path to rect (`:layout`), and the
;;   composite's overall dimensions (`:width`, `:height`). Produced by
;;   `pj/pose->draft`.
;;
;; - **Plan** -- fully resolved geometry in data space (domains,
;;   ticks, legends, computed shapes). A `Plan` record (composite
;;   plots use `CompositePlan`) holding panels as plain maps, layers
;;   as `PlanLayer` records, and numeric arrays as dtype-next
;;   buffers. Produced by `pj/draft->plan`. No rendering primitives
;;   yet.
;;
;; - **Membrane** -- a `PlotjeMembrane` record carrying positioned
;;   drawing primitives (Translate, WithColor, Path, Label, ...) in
;;   drawing space, sized to the output canvas. The record itself
;;   implements the
;;   [Membrane](https://github.com/phronmophobic/membrane) library's
;;   UI protocols, so it composes with other Membrane elements.
;;   Produced by `pj/plan->membrane`. The
;;   [Membranes](./plotje_book.membranes.html) chapter walks this
;;   stage in depth.
;;
;; - **Plot** -- rendered output (SVG hiccup or BufferedImage).
;;   Produced by `pj/membrane->plot`, dispatching on a `:format`
;;   keyword.
;;
;; The composition shortcuts `pj/draft`, `pj/plan`, `pj/membrane`,
;; and `pj/plot` run the chain from a pose up through the named
;; stage. They are introduced one section down.

;; Most users only interact with the pose stage and never need to
;; think about the others. The stages below matter when you are
;; debugging unexpected output, building a custom renderer, or
;; extending the library.

;; ## Why these stages?
;;
;; A simpler library could go from data to a rendered image in one
;; function.
;; Plotje splits the work into five stages so each stage addresses
;; a distinct concern, and each boundary between stages has a
;; specific purpose:
;;
;; - The **pose** is what the user specifies.
;; - The **draft** is the
;;   same specification flattened, with scope merged in. This
;;   boundary lets the layer engine run on a uniform input
;;   regardless of how the pose was built (single layer, faceted
;;   leaf, composite tree).
;; - The **plan** holds geometry in data space -- domains, ticks,
;;   computed shapes -- before any drawing. This boundary lets you
;;   inspect and validate plot structure with Malli, and it lets
;;   multiple renderers share the same computed plan. It also carries
;;   the dimensions the layout settled on, so where each panel will sit
;;   on the canvas is decided here: `pj/frames` reports it, and the
;;   membrane stage draws into it.
;; - The **membrane** holds drawing primitives in drawing space.
;;   This boundary decouples "what to draw, where" from the output
;;   format, so SVG and raster renderers consume the same membrane
;;   tree.
;; - The **plot** is the format-specific output: SVG hiccup, a
;;   `BufferedImage`, or any other format a backend supports.
;;
;; This structure has two consequences. Every intermediate value
;; can be inspected with `kind/pprint`. The same pipeline can be
;; extended by registering new methods at any stage -- mark, stat,
;; scale, coordinate system, output format -- without modifying the
;; core.
;; The [Extensibility](./plotje_book.extensibility.html)
;; chapter walks each extension point.

;; ## The Single-Step Transitions
;;
;; Each transition is its own public function. Walk the example
;; below to see what enters and what leaves at each step. The
;; per-function reference (arities, arguments, return types) lives
;; in the [API Reference](./plotje_book.api_reference.html#pipeline).

;; ### Step 1: pj/->pose
;;
;; Lift raw data (or a pose) to a pose. Polymorphic on input: a
;; dataset becomes a leaf pose with `:data` set; an existing pose
;; flows through unchanged (idempotent). This is the structural lift
;; that lets every downstream function accept either raw data or a
;; pose. It stops there -- it sets no mapping. The default mapping
;; that makes a bare dataset render is filled in when the pose is
;; drafted, and `pj/infer-mapping` writes it onto the pose ahead of
;; that (see "Where Inference Happens" below).
;;
;; The example traced through every stage is iris petal
;; measurements, with a scatter and per-species regression line.
;; Pose-level `:x`, `:y`, and `:color` mappings; two layers on top
;; of that mapping.

(def trace-pose
  (-> (rdatasets/datasets-iris)
      (pj/pose :petal-length :petal-width {:color :species})
      pj/lay-point
      (pj/lay-smooth {:stat :linear-model})))

;; The pose auto-renders as the plot it specifies:

trace-pose

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; The underlying value is a plain Clojure map -- `kind/pprint`
;; reveals the structure (without it, the auto-render would re-show
;; the plot):

(kind/pprint trace-pose)

(kind/test-last [(fn [v] (and (pj/pose? v)
                              (= [:petal-length :petal-width :species]
                                 [(:x (:mapping v))
                                  (:y (:mapping v))
                                  (:color (:mapping v))])
                              (= 2 (count (:layers v)))
                              (= [:point :smooth]
                                 (mapv :layer-type (:layers v)))))])

;; ### Step 2: pj/pose->draft
;;
;; Flatten a pose into a draft. For a leaf, returns a `LeafDraft`
;; record carrying the merged layer maps and the pose-level options.
;; The pose-level mapping (`:x :petal-length`, `:y :petal-width`,
;; `:color :species`) appears inside *each* of the two layer maps,
;; alongside layer-specific keys (`:mark`, `:stat`). The layer
;; engine downstream receives a uniform shape regardless of where each
;; mapping was originally specified. Keys prefixed with double
;; underscores (e.g. `:__panel-idx`) are internal markers; they pass
;; through the plan stage and follow the Clojure "do not consume"
;; convention.

(def trace-draft
  (pj/pose->draft trace-pose))

(kind/pprint trace-draft)

(kind/test-last [(fn [d] (and (pj/leaf-draft? d)
                              (= 2 (count (:layers d)))
                              (let [layers (:layers d)]
                                (and (= [:point :line] (mapv :mark layers))
                                     (every? #(= :petal-length (:x %)) layers)
                                     (every? #(= :petal-width (:y %)) layers)
                                     (every? #(= :species (:color %)) layers)))
                              (= {} (:opts d))))])

;; ### Step 3: pj/draft->plan
;;
;; Resolve the draft into computed geometry. Reads `:opts` from the
;; draft to apply title, dimensions, axis labels, and so on. The
;; smooth layer's `:linear-model` stat resolves into per-species
;; line segments here; the point layer keeps the raw observations
;; grouped by species. The legend gets one entry per species.

(def trace-plan
  (pj/draft->plan trace-draft))

;; The plan -- a `Plan` record carrying panels, total dimensions,
;; ticks, the legend spec, and per-layer geometry (groups of
;; dtype-next buffers):

(kind/pprint trace-plan)

(kind/test-last [(fn [v] (and (pj/leaf-plan? v)
                              (= 1 (count (:panels v)))
                              (some? (:total-width v))
                              (some? (:total-height v))
                              (= 3 (count (get-in v [:legend :entries])))
                              (let [layers (:layers (first (:panels v)))]
                                (and (= [:point :line] (mapv :mark layers))
                                     (= 3 (count (:groups (first layers))))
                                     (= 3 (count (:groups (second layers))))))))])

;; The plan validates against a Malli schema:

(pj/valid-plan? trace-plan)

(kind/test-last [true?])

;; ### Step 4: pj/plan->membrane
;;
;; Convert the plan into a `PlotjeMembrane` -- a tree of membrane
;; drawing primitives positioned in drawing-space coordinates,
;; wrapped in a record that itself implements the Membrane UI
;; protocols (`IOrigin`, `IBounds`, `IChildren`).

(def trace-membrane (pj/plan->membrane trace-plan))

;; The membrane carries the rendered drawables plus the canvas size
;; and title. The drawables sit inside the record's `:drawables`
;; field; record fields `:width` and `:height` give the canvas
;; size; and `:plotje/title` carries the title (when set):

(kind/pprint trace-membrane)

(kind/test-last
 [(fn [v]
    (and (pj/membrane? v)
         (pos? (count (:drawables v)))
         (every? #(.startsWith (.getName (class %))
                               "membrane.ui.")
                 (:drawables v))))])

;; The dedicated [Membranes](./plotje_book.membranes.html#the-membrane-protocols) chapter
;; walks the record's protocols, the namespaced-attribute convention,
;; and how a `PlotjeMembrane` composes with hand-built Membrane
;; elements.

;; ### Step 5: pj/membrane->plot
;;
;; Convert the membrane into the rendered output for a chosen
;; format. Dispatches on the format keyword; `:svg` is built in.
;; This is the only stage whose output is format-native rather than
;; portable: every earlier stage is the same across backends, while
;; here the membrane becomes SVG hiccup, a BufferedImage, or another
;; format's bytes.
;; The membrane carries its plan-derived dimensions as record
;; fields (read via `(membrane.ui/width m)`/`(height m)`), so
;; `pj/membrane->plot` does not need them respelled in opts:

(def trace-plot
  (pj/membrane->plot trace-membrane :svg {}))

(kind/pprint trace-plot)

(kind/test-last [(fn [v] (and (vector? v) (= :svg (first v))))])

;; Rendered:

(kind/hiccup trace-plot)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; ## Pipeline Shortcuts: pj/pose, pj/draft, pj/plan, pj/membrane, pj/plot, pj/save
;;
;; Each pipeline stage has a user-facing convenience that runs the
;; chain from raw input up through that stage; `pj/save` is the
;; file-output sibling that runs the chain all the way and writes the
;; result to disk:
;;
;; - **pj/pose** -- not a literal composition. On raw data its
;;   one-argument form is `pj/->pose`. With more arguments it reads
;;   column arguments (`(pj/pose data :x :y)`) and a mapping, and on an
;;   existing pose it merges them into that pose's mapping. It infers
;;   nothing: the default mapping is filled in when the pose is drafted.
;; - **pj/draft** -- raw input -> draft.
;; - **pj/plan** -- raw input -> plan.
;; - **pj/membrane** -- raw input -> membrane tree.
;; - **pj/plot** -- raw input -> rendered figure.
;; - **pj/save** -- raw input -> file on disk (renders, then writes).
;;
;; Each shortcut starts from raw input and stops at a different
;; stage along the same chain. The solid line is the stage-to-stage
;; pipeline; each dashed arrow is one shortcut, running from raw
;; input up to the stage it produces:

^:kindly/hide-code
(kind/mermaid "
graph LR
  X[\"Raw data\"] --> B[\"Pose\"] --> D[\"Draft\"] --> P[\"Plan\"] --> M[\"Membrane\"] --> F[\"Plot\"]
  X -.->|pj/pose| B
  X -.->|pj/draft| D
  X -.->|pj/plan| P
  X -.->|pj/membrane| M
  X -.->|pj/plot| F
  style X fill:#eee,stroke-dasharray:3 3
  style B fill:#d1c4e9
  style D fill:#e8f5e9
  style P fill:#fff3e0
  style M fill:#e3f2fd
  style F fill:#fce4ec
")

;; The shortcuts from `pj/draft` on are near-literal compositions of
;; these steps: each lifts raw input with `pj/->pose`, applies the
;; shared `pj/infer-mapping` step (which gives a pose that names no
;; `:x` or `:y` column the default mapping of its data, and changes
;; nothing on a pose whose mapping names one), and runs the chain up to
;; its stage. Their source shows the pipeline directly. Two things are
;; simplified below for clarity: the `pj/->pose` caller-name argument
;; -- which only tunes error messages -- is elided, and `pj/pose` is
;; shown at its 1-arity (its other arities also read column arguments
;; and a mapping).
;;
;; Pseudocode:
;; ```clojure
;; (defn pose         ; 1-arity; the other arities also read
;;   ([x]             ; column arguments and a mapping
;;    (-> x
;;        ->pose)))
;;
;; (defn draft
;;   ([x]
;;    (-> x
;;        ->pose
;;        infer-mapping
;;        pose->draft))
;;   ([x opts]
;;    (-> x
;;        ->pose
;;        (options opts)
;;        draft)))
;;
;; (defn plan
;;   ([x]
;;    (-> x
;;        ->pose
;;        infer-mapping
;;        pose->draft
;;        draft->plan))
;;   ([x opts]
;;    (-> x
;;        ->pose
;;        (options opts)
;;        plan)))
;;
;; (defn membrane
;;   ([x]
;;    (let [pose (-> x
;;                   ->pose
;;                   infer-mapping)
;;          opts (:opts pose {})]
;;      (-> pose
;;          pose->draft
;;          draft->plan
;;          (plan->membrane opts))))
;;   ([x opts]
;;    (-> x
;;        ->pose
;;        (options opts)
;;        membrane)))
;;
;; (defn plot
;;   ([x]
;;    (let [pose (-> x
;;                   ->pose
;;                   infer-mapping)
;;          opts (:opts pose {})
;;          fmt  (or (:format opts) :svg)]
;;      (-> pose
;;          pose->draft
;;          draft->plan
;;          (plan->membrane opts)
;;          (membrane->plot fmt opts))))
;;   ([x opts]
;;    (-> x
;;        ->pose
;;        (options opts)
;;        plot)))
;;
;; (defn save
;;   ([x path] (save x path nil))
;;   ([x path opts]
;;    (let [pose (-> x
;;                   ->pose
;;                   infer-mapping)
;;          pose (if opts (options pose opts) pose)
;;          fmt  (resolve-format pose path)]  ; :format opt, else path extension
;;      ;; render the plan to fmt bytes and write them to path
;;      (write-file path (plan->plot (plan pose) fmt (:opts pose)))
;;      path)))
;; ```
;;
;; In `plot`, the `let` binds `pose`, `opts`, and `fmt` for use in
;; the subsequent `->` thread, which runs the four transitions
;; (`pose->draft`, `draft->plan`, `plan->membrane`, `membrane->plot`)
;; in order. The membrane carries the dimensions the plan settled on as
;; record fields, and the title under `:plotje/title`, so
;; `membrane->plot` reads them without the plan.
;;
;; `pj/save` is the file-output sibling: it lifts and infers the same
;; way, resolves the file format (from a `:format` option or the path
;; extension), renders the plan to those bytes, and writes them to
;; disk, returning the path. `resolve-format` and `write-file` above
;; stand in for that format-resolution and file-writing detail.
;;
;; The 2-arity of each function folds the options map into the pose
;; using `pj/options` before recursing into the 1-arity.
;;
;; `pj/membrane` is the analogous shortcut for the membrane stage,
;; useful for consumers that want a membrane tree without choosing
;; an output format yet -- for example a custom backend, or a target
;; that Membrane supports but Plotje has not wired in yet.
;;
;; Because the compositions call the steps, redefining a step
;; (with `with-redefs` for testing, or with a custom `defmethod` for
;; plan->membrane) takes effect in every user-facing function.

;; The composition holds at runtime:

(let [pose-with-opts (-> trace-pose
                         (pj/options {:title "Iris Petals"
                                      :x-label "Petal length"
                                      :width 700}))
      via-plan (pj/plan pose-with-opts)
      via-arrows (-> pose-with-opts
                     pj/->pose
                     pj/pose->draft
                     pj/draft->plan)]
  {:title-match (= (:title via-plan) (:title via-arrows))
   :x-label-match (= (:x-label via-plan) (:x-label via-arrows))
   :width-match (= (:width via-plan) (:width via-arrows))
   :title (:title via-plan)
   :x-label (:x-label via-plan)
   :width (:width via-plan)})

(kind/test-last [(fn [m] (and (:title-match m)
                              (:x-label-match m)
                              (:width-match m)
                              (= "Iris Petals" (:title m))
                              (= "Petal length" (:x-label m))
                              (= 700 (:width m))))])

;; Plot-level options (title, x-label, width, ...) are stored on the
;; pose's `:opts`, copied into the `LeafDraft`'s `:opts`, and read
;; by `pj/draft->plan`. Calling the steps directly, without
;; the user-facing convenience, produces the identical plan.
;;
;; The same property allows inspection at any stage: stop the chain
;; before the next step. `(-> data ... pj/pose->draft
;; kind/pprint)` shows the draft; `(-> data ... pj/pose->draft
;; pj/draft->plan)` shows the plan.

;; ## Where Inference Happens
;;
;; Two of the steps also **infer**: they fill in choices the user
;; did not specify. Inference is what lets a dataset alone -- with
;; no mapping, no layers, no options -- produce a complete plot. Most
;; one-line examples in this book rely on inference at one or both
;; of these stages. The
;; [Inference Rules](./plotje_book.inference_rules.html) chapter is
;; the authoritative reference for what each stage infers.

(pj/pose {:x [1 2 3 4 5]
          :y [2 4 3 5 4]
          :g [:a :a :b :b :b]})

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:points s))
                                (= 2 (count (filter #(.startsWith % "rgb")
                                                    (:colors s)))))))])

;; Where each kind of inference lives:
;;
;; - **Mapping inference** fills in `:x`, `:y` and `:color` from the
;;   first one to three columns of the data: one column to `:x`, two
;;   columns to `:x` and `:y`, and a third column to `:color`. It runs
;;   at the start of `pj/pose->draft`, on every leaf, cells of a
;;   composite included. It writes the default where neither the
;;   pose's mapping nor any layer names an `:x` or a `:y` column, and
;;   where the first layer names no column, whatever the later layers
;;   name (the example below this list shows a later layer naming the
;;   default's columns).
;;   `pj/infer-mapping` is the same step as a public function: it
;;   writes the default onto the pose, where it can be read before
;;   drafting. `pj/->pose`, `pj/pose` and the `pj/lay-*` functions read
;;   no column of the data when they are called: a layer naming no
;;   column is given its columns when the pose is drafted, and a layer
;;   left without a column is reported then.
;; - **Layer-type inference** is written into the draft. A pose with no
;;   `pj/lay-*` call is given one layer, of the type its columns choose:
;;   a categorical x with a numerical y gives a boxplot, a temporal x
;;   with a numerical y gives a line, a numerical x and y give a
;;   scatter, and one numerical column gives a histogram.
;;   `pose/elaborate` makes the choice, so the draft layer already
;;   holds its `:mark` and `:stat`. `pj/draft->plan` chooses the stat of
;;   a layer whose type leaves it open -- a bar with no `:y` column
;;   counts -- and gives a draft layer written by hand with no `:mark`
;;   the type its columns choose.
;; - **Column-type inference** lives in `pj/draft->plan`.
;;   `:x-type`/`:y-type`/`:color-type` default from the data
;;   (numerical / categorical / temporal); a user-supplied
;;   `:x-type` overrides the default.
;; - **Geometry inference** lives in `pj/draft->plan`. Domains
;;   default to data ranges; ticks default to evenly-spaced
;;   values; legend entries are derived from the layers' aesthetic
;;   mappings; the coordinate system defaults to `:cartesian`.
;;
;; Mapping inference and the choice of a layer type run in
;; `pj/pose->draft`; the rest runs in `pj/draft->plan`.
;; Every inferred default has an explicit override (`pj/scale`,
;; `pj/coord`, `pj/options`, mapping keys like `:x-type`), so the
;; user can opt out of any single inference without losing the
;; others.
;;
;; Below, `pj/pose->draft` gives a pose built by `pj/->pose` the default
;; mapping of its three columns, with no `pj/infer-mapping` call in
;; between. The points keep the color of the default beside a smooth
;; that names the default's `:x` and `:y` columns:

(->> (-> {:a [1 2 3 4] :b [2 4 3 5] :c ["u" "u" "v" "v"]}
         pj/->pose
         pj/lay-point
         (pj/lay-smooth :a :b)
         pj/pose->draft)
     :layers
     (mapv #(select-keys % [:mark :x :y :color])))

(kind/test-last
 [(fn [layers]
    (= [{:mark :point :x :a :y :b :color :c}
        {:mark :line :x :a :y :b :color :c}]
       layers))])

;; ### The columns a layer reads
;;
;; One choice is made at the start of `pj/pose->draft`, by
;; `pose/elaborate`: which columns a layer reads where it names none, or
;; names one of two. A
;; `pj/lay-*` call keeps its columns on its own layer, so the pose
;; below has no mapping. Its panel's columns are those of the first
;; layer naming one on each axis. `pose/elaborate` writes them into
;; each layer that needs them, and the draft then merges the pose's
;; mapping and each layer's mapping by scope and nothing else:

(-> {:week [1 2 3 4]
     :adults [30 25 28 35]
     :children [20 15 18 22]}
    (pj/lay-point :week :adults)
    (pj/lay-line {:y :children})
    pj/lay-smooth
    pose/elaborate
    (dissoc :data)
    kind/pprint)

(kind/test-last
 [(fn [elaborated]
    (and (nil? (:mapping elaborated))
         (= [{:x :week :y :adults}
             {:x :week :y :children}
             {:x :week :y :adults}]
            (mapv :mapping (:layers elaborated)))))])

;; The column is written into the layer and not into the pose's
;; mapping. Written into the pose, it would reach every layer by
;; scope, and a layer type that can draw from one column -- a bar that
;; counts, a histogram -- would be given a second column it did not
;; ask for. The panel's columns are written into the pose in one place,
;; a facet over columns: its cells are made from the panel's columns,
;; with the facet's column on the axis it names
;; (`pose/expand-facet-nodes`, below). Elaboration writes two other
;; things on the pose: the default mapping of data with one to three
;; columns, and the flip of a column named on `:y` alone (see "A column
;; on `:y` alone" below).
;;
;; The panel's columns are also what a `pj/lay-*` call on a composite
;; compares its columns with, to find the cell the layer joins:
;; `pose/last-matching-leaf-path` reads `pose/leaf-place`, the function
;; `pose/elaborate` reads. A cell built by `pj/lay-*` names its columns
;; on its layers and not in its mapping. Such a cell is matched by its
;; columns all the same, because the panel's columns are read from the
;; layers where the mapping names none.
;;
;; A layer may reach the plan with no data at all: a template drawn
;; before `pj/with-data`, or a cell added to an arrangement whose root
;; has no data. `plan/resolve-panel-draft-layers` gives such a layer a
;; dataset of no rows before its stat runs, so every stat takes the
;; route it takes for data that is all missing, and the panel reads
;; "no data".

;; ### What elaboration writes
;;
;; `pose/elaborate` writes into a pose what the draft would otherwise
;; have to choose, in this order: the rows the pose keeps
;; (`apply-where`); a series pivoted into a key column and a value
;; column; the default mapping of data with one to three columns; one
;; layer, where the pose names columns and has no layer; the panel's
;; columns, into each layer that reads them; a layer type for each
;; layer left to inference; and the flip of a column named on `:y`
;; alone, described in the next section.
;;
;; A layer naming no column is given both of the panel's columns. A
;; layer naming one column is given the other only where its layer type
;; cannot draw from one column, which `layer-type/column-counts` says.
;; The line naming `:children` above is given `:week`. A bar naming
;; `:day` beside points of `:day` against `:visits` is given nothing,
;; and counts:

(def counted-beside-points
  (-> {:day ["mon" "tue" "mon" "wed"]
       :visits [3 5 2 4]}
      (pj/lay-point :day :visits)
      (pj/lay-bar :day)))

(->> counted-beside-points
     pose/elaborate
     :layers
     (mapv :mapping))

(kind/test-last
 [(fn [mappings]
    (and (= [{:x :day :y :visits} {:x :day}] mappings)
         ;; A second call changes nothing.
         (= (pose/elaborate counted-beside-points)
            (pose/elaborate (pose/elaborate counted-beside-points)))))])

;; A second call to `pose/elaborate` changes nothing, so the draft can
;; run it on a pose that was elaborated already.
;;
;; ### A column on `:y` alone
;;
;; A histogram, a density, a rug and a counting bar draw from one
;; column, which their stat reads on `:x`. Where a pose names a column
;; on `:y` and none on `:x`, in its mapping or in its layers, and every
;; layer can draw from one column, `pose/elaborate` moves the column to
;; `:x` and writes `:coord :flip` into the pose's options, so the marks
;; run along the vertical axis:

(-> {:height [1.6 1.7 1.8 1.7 1.75]}
    (pj/lay-histogram {:y :height})
    pose/elaborate
    (dissoc :data)
    kind/pprint)

(kind/test-last
 [(fn [elaborated]
    (and (= {:coord :flip} (:opts elaborated))
         (= [{:x :height}] (mapv :mapping (:layers elaborated)))))])

;; A layer that draws only from two columns, or that names an `:x`,
;; keeps the pose as it is written, and the layer left with no `:x`
;; column is then reported. A `:coord` other than `:flip` on a pose that
;; would be turned on its side reports an error, since the flip is the
;; one drawing for it. A layer type of an extension takes part by
;; declaring `:column-counts`.
;;
;; ### The color a layer is given
;;
;; Where the layers of a pose draw different columns, nothing on the
;; panel says which marks belong to which columns. `pose/leaf->draft`
;; gives each layer a label made of the columns the layer draws
;; (`pose/overlay-labels`), and each axis is titled with the columns of
;; every layer. Each layer whose mapping specifies no `:color` is then
;; given its label as a color: one palette color and one legend entry
;; per layer. A layer that specifies a color keeps that color. On a
;; panel where a layer is colored by a column, no layer is given the
;; default, because the legend of that panel lists the column's values.
;; The color is written into the draft and not into the pose: each
;; draft layer below maps `:color` to a column of labels the draft adds
;; to its data, except the smooth, which keeps its black:

(def sepals-and-petals
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/lay-point :petal-length :petal-width)
      (pj/lay-smooth {:color "black"})))

(->> sepals-and-petals
     pj/draft
     :layers
     (mapv (fn [layer]
             (let [color (:color layer)]
               (if (keyword? color)
                 (first ((:data layer) color))
                 color)))))

(kind/test-last
 [(fn [colors]
    (and (= ["sepal length / sepal width" "petal length / petal width" "black"]
            colors)
         ;; Nothing is written into the pose's layers.
         (= [{:x :sepal-length :y :sepal-width}
             {:x :petal-length :y :petal-width}
             {:color "black"}]
            (mapv :mapping (:layers sepals-and-petals)))
         ;; A layer colored by a column leaves the other layer with no
         ;; color.
         (let [colored (-> (rdatasets/datasets-iris)
                           (pj/lay-point :sepal-length :sepal-width {:color :species})
                           (pj/lay-point :petal-length :petal-width))]
           (nil? (:color (second (:layers (pj/draft colored))))))))])

;; ## Composite Poses
;;
;; A composite pose -- one with `:poses` inside --
;; flows through the same steps. Each step dispatches
;; internally on shape: a leaf pose produces a `LeafDraft`; a
;; composite pose produces a `CompositeDraft`. The user-facing
;; pipeline is unchanged.

(def composite-pose
  (-> (rdatasets/datasets-iris)
      (pj/arrange [[:petal-length :petal-width]
                   [:sepal-length :sepal-width]])
      (pj/pose {:color :species})))

composite-pose

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; `pj/draft` (the composition of `->pose`, `infer-mapping` and
;; `pose->draft`) returns a `CompositeDraft` -- wrap in `kind/pprint` to
;; inspect:

(-> composite-pose pj/draft kind/pprint)

(kind/test-last [(fn [d] (and (pj/composite-draft? d)
                              (= 2 (count (:sub-drafts d)))))])

;; `pj/plan` returns a `CompositePlan`:

(pj/plan composite-pose)

(kind/test-last [(fn [p] (and (pj/composite-plan? p)
                              (= 2 (count (:sub-plots p)))))])

;; `pj/membrane` returns a `PlotjeMembrane` whose `:drawables` carry
;; one `Translate` per leaf plus composite framing (column strip
;; labels, shared legend, title if any). Plan-derived width and height
;; are record fields, and the title is `:plotje/title`.

(pj/membrane composite-pose)

(kind/test-last [(fn [m] (and (pj/membrane? m)
                              (pos? (count (:drawables m)))
                              (number? (membrane.ui/width m))
                              (number? (membrane.ui/height m))))])

;; `pj/plot` (the full pipeline) returns the SVG hiccup -- the
;; same value `composite-pose` auto-renders to at the top of this
;; section, produced explicitly:

(-> composite-pose pj/plot kind/pprint)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; The composition holds for both leaf and composite poses --
;; `pj/plan` is `(-> pose pj/->pose pj/pose->draft pj/draft->plan)`
;; either way -- because each step dispatches on shape at
;; the bottom of its call.

;; The composite path also performs work across leaves that a single
;; leaf has no counterpart for: deciding which cells share an axis, and
;; what that axis covers. The next section follows it through the
;; stages.

;; ## How Panels Share an Axis
;;
;; Panels that are meant to be compared are drawn on one axis: the same
;; numbers, or the same categories, in every panel. Plotje reaches
;; several panels by two paths, and both follow one rule: **a shared
;; axis covers what the panels sharing it draw.**
;;
;; - A facet by the categories of a column is one plan with several
;;   panels.
;; - An arrangement, and a facet over columns, is a `CompositePlan`: one
;;   plan per cell.
;;
;; The same two clinics drawn by each path:

(def north
  {:week ["w1" "w1" "w2" "w2"]
   :age ["adults" "children" "adults" "children"]
   :visits [30 20 25 15]})

(def south
  {:week ["w1" "w1" "w2" "w2"]
   :age ["adults" "children" "adults" "children"]
   :visits [60 45 50 40]})

(def arranged-clinics
  (pj/arrange [(pj/lay-bar north :week :visits {:color :age :position :stack})
               (pj/lay-bar south :week :visits {:color :age :position :stack})]
              {:share-scales #{:y}}))

arranged-clinics

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

(def faceted-clinics
  (-> {:clinic ["north" "north" "north" "north" "south" "south" "south" "south"]
       :week ["w1" "w1" "w2" "w2" "w1" "w1" "w2" "w2"]
       :age ["adults" "children" "adults" "children"
             "adults" "children" "adults" "children"]
       :visits [30 20 25 15 60 45 50 40]}
      (pj/lay-bar :week :visits {:color :age :position :stack})
      (pj/facet :clinic)))

faceted-clinics

(kind/test-last
 [(fn [v] (and (pj/composite-plan? (pj/plan arranged-clinics))
               (not (pj/composite-plan? (pj/plan v)))
               (= 2 (count (:panels (pj/plan v))))))])

;; ### The pose stage decides which cells share
;;
;; `pj/pose->draft` reads the composite's `:share-scales` and puts the
;; cells into groups. It measures no extent to do this. It reads the
;; type of each column, to report cells that cannot share, and for a
;; column of categories the order the data holds them in, which the
;; group carries as `:order`. Each sub-draft carries the groups of its
;; cell under `:share`, by axis:

(->> arranged-clinics
     pj/draft
     :sub-drafts
     (mapv :share))

(kind/test-last [(fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))])

;; Two cells are placed in one group where they draw the same thing
;; along an axis: the values of one column, or what a stat computes from
;; one column -- a count, a density -- which the stat declares (see
;; [What a stat computes](./plotje_book.extensibility.html#what-a-stat-computes)).
;; A count of a column and the values of that column are never in one
;; group. `:share-scales` is a set of axes, as above, or a map from an
;; axis to `:same-column`, to the columns that go together on that
;; axis, `{:y [[:hwy :cty]]}`, or to `:all`, `{:y :all}`, which puts
;; every cell in one group.
;;
;; `:share-scales` names an axis as it is drawn: `:x` is the horizontal
;; axis. The groups are recorded by the axis of each cell's own
;; mapping, so two flipped cells sharing `:x` are in a group on their
;; `:y`, which is the column that runs across:

(->> (pj/arrange [(pj/coord (pj/lay-bar north :week :visits) :flip)
                  (pj/coord (pj/lay-bar south :week :visits) :flip)]
                 {:share-scales #{:x}})
     pj/draft
     :sub-drafts
     (mapv :share))

(kind/test-last [(fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))])

;; With no `:share-scales` written, cells that split the data of one
;; pose -- the cells of `(pj/arrange data [...])` and of a facet over
;; columns -- share each axis on which two of them draw one column.
;; Below, the two cells draw `:week` on `:x` and share it, and the group
;; lists the weeks in the order of the data's rows:

(->> (pj/arrange {:week ["w2" "w1" "w2" "w1"]
                  :adults [30 25 28 35]
                  :children [20 15 18 22]}
                 [[:week :adults] [:week :children]])
     pj/draft
     :sub-drafts
     (mapv :share))

(kind/test-last
 [(fn [shares]
    (and (= [{:x {:group 0 :order ["w2" "w1"]}}
             {:x {:group 0 :order ["w2" "w1"]}}]
            shares)
         ;; Poses with data of their own share nothing by default.
         (= [nil nil]
            (->> (pj/arrange [(pj/lay-bar north :week :visits)
                              (pj/lay-bar south :week :visits)])
                 pj/draft
                 :sub-drafts
                 (mapv :share)))))])

;; Poses that carry data of their own, arranged with
;; `(pj/arrange [pose-a pose-b])`, share no axis unless `:share-scales`
;; names it, as the two clinics above do. A cell whose layers draw two
;; columns on an axis belongs to the groups of both columns, and the two
;; groups become one: three cells sharing `:y`, drawing `:adults`,
;; `:children`, and both, are one group.

(def weekly-visits
  {:week [1 2 3 4]
   :adults [30 25 28 35]
   :children [20 15 18 22]})

(->> (pj/arrange [(pj/lay-point weekly-visits :week :adults)
                  (pj/lay-point weekly-visits :week :children)
                  (-> weekly-visits
                      (pj/lay-point :week :adults)
                      (pj/lay-line :week :children))]
                 {:share-scales #{:y}})
     pj/draft
     :sub-drafts
     (mapv :share))

(kind/test-last
 [(fn [shares] (= [{:y {:group 0}} {:y {:group 0}} {:y {:group 0}}] shares))])

;; ### The plan stage decides what the axis covers
;;
;; What a cell draws is known only after its stats have run. A stack of
;; bars reaches the sum of its parts, which no row of the `:visits`
;; column holds. So `pj/draft->plan` first measures every sharing cell:
;; `plan/draft->extents` runs the cell's stats and returns the extent
;; the cell draws on each axis, before any padding.

(->> arranged-clinics
     pj/draft
     :sub-drafts
     (mapv (fn [sub-draft]
             (plan/draft->extents (:draft sub-draft) (:opts sub-draft)))))

(kind/test-last
 [(fn [extents]
    ;; The largest value in either :visits column is 60; the stacks
    ;; reach 50 and 105.
    (= [[0.0 50.0] [0.0 105.0]] (mapv :y extents)))])

;; The extents of a group are pooled: numbers by the lowest low and the
;; highest high, categories by listing every category of every cell.
;; Each cell is then planned with the pooled extent in place of its
;; own. The pooled extent is in the cell's options in the plan:

(->> arranged-clinics
     pj/plan
     :sub-plots
     (mapv (fn [sub-plot] (:y-scale-domain (:opts sub-plot)))))

(kind/test-last [(fn [domains] (= [[0.0 105.0] [0.0 105.0]] domains))])

;; Every cell pads that one extent the same way, so the panels end on
;; one domain:

(->> arranged-clinics
     pj/plan
     :sub-plots
     (mapv (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot)))))))

(kind/test-last [(fn [domains] (and (apply = domains)
                                    (< 105.0 (second (first domains)))))])

;; These details complete the picture:
;;
;; - **Categories.** Where the cells of a group are parts of one
;;   dataset, the pooled categories are listed in the order of that
;;   dataset's rows, so the split has the axis of the same plot drawn
;;   without the split. Cells with data of their own are listed cell by
;;   cell.
;; - **Legends.** A color legend and a shape legend follow the same
;;   rule: the categories of the legend are listed in the order of the
;;   rows of the dataset the panels are parts of. One function,
;;   `pose/data-category-order`, gives that order to the axis and to the
;;   legend. The palette, the order of a stack and the order of dodged
;;   marks follow the order of the legend.
;; - **A specified domain.** A `:domain` that one cell specifies for the
;;   shared axis is the domain of every cell sharing that axis: every
;;   cell is planned as though it had specified that domain, so no cell
;;   pads the domain. Cells that specify different domains for one
;;   shared axis report an error.
;; - **Text labels.** A cell's numeric domain is widened after planning
;;   so that its text marks are drawn in full, by an amount that depends
;;   on the cell's own labels. Cells of a group that end on different
;;   domains are planned again with the widest domain as a floor, so
;;   that they agree.
;; - **Cost.** A sharing cell's stats run twice: once to measure it, and
;;   once to plan it with the pooled extent. A cell that shares with no
;;   other cell is not measured.
;;
;; A facet in one plan follows the same steps inside `pj/draft->plan`:
;; its panels are measured without padding, pooled, and each panel is
;; then given the pooled extent and padded. The facet of the two
;; clinics ends on the domain the arrangement ends on:

(->> faceted-clinics
     pj/plan
     :panels
     (mapv :y-domain))

(kind/test-last
 [(fn [domains]
    (= domains
       (mapv (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot)))))
             (:sub-plots (pj/plan arranged-clinics)))))])

;; ### Poses rewritten before drafting
;;
;; Some poses are rewritten into the cells they stand for before any of
;; the above runs, so that one spelling cannot be drawn differently
;; from another. `pj/pose->draft` does this in `pose/expand-facet-nodes`.
;;
;; A facet over columns is kept on the pose as written, and expanded
;; into a composite of one cell per column. A series faceted by the key
;; column the series invents asks for the same panels, so it is first
;; rewritten into a facet over the columns of the series, and then
;; expanded like any other, here on the weekly visits above:

(def series-by-key
  (-> weekly-visits
      (pj/lay-line :week [:adults :children])
      (pj/facet :series)))

series-by-key

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; The expanded pose is a composite. The column the layer drew along
;; `:x` has moved to the root, and each cell names one of the series'
;; columns:

(-> series-by-key
    pose/expand-facet-nodes
    (dissoc :data)
    kind/pprint)

(kind/test-last
 [(fn [expanded]
    (and (= {:x :week} (:mapping expanded))
         (= [{:y :adults} {:y :children}]
            (mapv :mapping (mapcat :poses (:poses expanded))))))])

;; Each cell holds what the facet specified for it and nothing else. On
;; the diagonal of a matrix, `{:col {:x cols} :row {:y cols}}`, the cell
;; names its column on both axes, the map `pj/arrange` writes for the
;; same cell, and a `nil` in a list of
;; columns stays in the cell as a `nil`:

(->> (-> weekly-visits
         pj/pose
         (pj/facet {:col {:x [:adults nil]} :row {:y [:adults :children]}})
         pose/expand-facet-nodes)
     (tree-seq :poses :poses)
     (remove :poses)
     (mapv :mapping))

(kind/test-last
 [(fn [mappings]
    (= [{:x :adults :y :adults} {:x nil :y :adults}
        {:x :adults :y :children} {:x nil :y :children}]
       mappings))])

;; The steps after this one read such a cell without a special case.
;; `pose/fit-layers` keeps a layer in a cell where the layer names no
;; column other than the cell's and its layer type can draw from the
;; number of distinct columns the cell names: one for `{:x :adults :y
;; :adults}`, so a density fits and a scatter does not. Inference reads
;; one column on both axes as one column and chooses a histogram, and
;; the stat reads the column on `:x` and puts its count on `:y`. A bar,
;; whose layer type can draw from one column or from two, counts the
;; rows of each category of such a cell (`resolve/infer-layer-type`),
;; and reports an error for a numerical column, as
;; `(pj/lay-bar data :adults)` does. A scatter draws the column against
;; itself.
;;
;; A cell that no layer fits is left with no layer, and elaboration
;; gives it the layer its columns choose. A layer that fits no cell is
;; drawn in every cell, as a histogram written on a faceted scatter is.
;; Where such a layer names a column on an axis on which every cell
;; names another, and some cell would be left with no layer drawing that
;; cell's own columns, `pose/fit-layers` reports an error. On a
;; two-by-two matrix, a scatter fits the two cells off the diagonal and
;; a density fits the two cells on it:

(->> (-> (select-keys weekly-visits [:adults :children])
         pj/lay-point
         pj/lay-density
         (pj/facet {:col {:x [:adults :children]}
                    :row {:y [:adults :children]}}))
     pj/draft
     :sub-drafts
     (mapv (fn [sub-draft] (mapv :mark (:draft sub-draft)))))

(kind/test-last
 [(fn [marks]
    (let [marks-per-cell (fn [pose]
                           (->> pose
                                pj/draft
                                :sub-drafts
                                (mapv (fn [sub-draft] (mapv :mark (:draft sub-draft))))))]
      (and (= [[:area] [:point] [:point] [:area]] marks)
           ;; A histogram fits no cell of a faceted scatter, and is
           ;; drawn in both.
           (= [[:point :bar] [:bar]]
              (marks-per-cell (-> weekly-visits
                                  (pj/lay-point :week :adults)
                                  (pj/lay-histogram {:x :week})
                                  (pj/facet {:y [:adults :children]}))))
           ;; A layer naming :week on :y, where every cell names its
           ;; own :y, leaves the cells with no layer of their own.
           (re-find #"no panel draws it"
                    (try (pj/draft (-> weekly-visits
                                       (pj/pose {:x :week})
                                       (pj/lay-point {:y :week})
                                       (pj/facet {:y [:adults :children]})))
                         ""
                         (catch Exception e (ex-message e)))))))])

;; From here the pose is an ordinary composite: its cells are grouped,
;; measured and planned as the two clinics above were. In the series
;; faceted by its key, each cell draws a column of its own on `:y`, so
;; by default the two cells share `:x` and not `:y`:

(->> series-by-key
     pj/draft
     :sub-drafts
     (mapv :share))

(kind/test-last
 [(fn [shares] (= [{:x {:group 0}} {:x {:group 0}}] shares))])

;; Cells written by hand, each keeping the rows of one value of a
;; column with `:where`, that together keep every value of the column
;; are rewritten the other way, into one pose faceted by that column
;; (`pose/collapse-regular-split`). A list that leaves a value out
;; stays a composite. Here the three drive types of the mpg data
;; plan as one faceted pose, and two of them as a composite:

(let [points (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
  (->> [[{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]
        [{:where {:drv "f"}} {:where {:drv "4"}}]]
       (mapv #(pj/composite-plan? (pj/plan (pj/arrange points %))))))

(kind/test-last [(fn [composite?] (= [false true] composite?))])

;; ## Where a Mistake Is Reported
;;
;; When a pose cannot be drawn, the step that reports the error says
;; what the error depends on. A call reports what its own arguments
;; decide. What depends on the other layers, on the columns the data
;; has, or on the cells of a facet is reported by `pj/pose->draft`. A
;; layer whose stat or mark cannot draw the values it is given, and
;; panels too small for the canvas, are reported by `pj/draft->plan`.
;; So `pj/lay-point` naming a column the data lacks returns a pose, and
;; drafting that pose reports the column.
;;
;; The table below builds a pose for each mistake and runs the steps in
;; order. It shows the first step that reports an error, and the first
;; sentence of the message:

(kind/table
 {:column-names ["Mistake" "Reported by" "First sentence of the message"]
  :row-vectors
  (for [[mistake build]
        [["A second column given to pj/lay-histogram"
          #(pj/lay-histogram weekly-visits :adults :children)]
         ["Pairs of columns given to pj/pose"
          #(pj/pose weekly-visits [[:week :adults] [:week :children]])]
         ["A key the map of pj/facet does not read"
          #(-> weekly-visits
               (pj/lay-point :week :adults)
               (pj/facet {:y [:adults :children] :z [:week]}))]
         ["A column the data lacks"
          #(pj/lay-point weekly-visits :week :teens)]
         ["A layer left without a column, on data of four columns"
          #(pj/lay-point (assoc weekly-visits :seniors [5 6 7 8]))]
         ["A category and a number on one axis"
          #(-> north
               (pj/lay-point :visits :week)
               (pj/lay-point :age :week))]
         ["A layer naming a column no cell of a facet over columns has"
          #(-> weekly-visits
               (pj/pose {:x :week})
               (pj/lay-point {:y :week})
               (pj/facet {:y [:adults :children]}))]
         ["A bar of two categorical columns"
          #(pj/lay-bar north :week :age)]
         ["A mark polar coordinates cannot draw"
          #(-> weekly-visits
               (pj/lay-line :week :adults)
               (pj/coord :polar))]
         ["Panels too small for the canvas"
          #(-> north
               (pj/lay-point :week :visits)
               (pj/facet :age)
               (pj/options {:width 60}))]]]
    (let [report (fn [step f]
                   (try (f)
                        nil
                        (catch Exception e
                          [mistake step (first (str/split (ex-message e) #"(?<=\.) "))])))
          pose (try (build) (catch Exception _ nil))]
      (or (report "the call" build)
          (report "pj/pose->draft" #(pj/pose->draft pose))
          (report "pj/draft->plan" #(pj/draft->plan (pj/pose->draft pose))))))})

(kind/test-last
 [(fn [table]
    (= (concat (repeat 3 "the call")
               (repeat 4 "pj/pose->draft")
               (repeat 3 "pj/draft->plan"))
       (map second (:row-vectors table))))])

;; ## The Plan Boundary
;;
;; The plan is the first pipeline boundary: the boundary between
;; description and rendering. The
;; pose and draft stages assemble the description. The plan resolves
;; it into computed geometry, domains, ticks, and legend -- still as
;; inspectable data, before any layout. The membrane and plot stages
;; then produce the rendered output.

^:kindly/hide-code
(kind/mermaid "
graph LR
  A[\"Pose + draft\"] -->|plan| P[\"Plan\"]
  P --> R[\"membrane + plot\"]
  style A fill:#e8f5e9
  style P fill:#fff3e0
  style R fill:#e3f2fd
")

;; The plan is inspectable as data -- `Plan` and `PlanLayer` records
;; (which behave as maps), plain maps, numbers, strings, keywords,
;; and dtype-next buffers for numeric arrays. It validates against a
;; Malli schema.
;;
;; This separation enables:
;;
;; - Inspecting the plan without rendering
;;
;; - Validating plot structure with Malli
;;
;; - Adding alternate backends that consume plans (SVG and raster
;;   are implemented today)

;; ## The Membrane Stage
;;
;; The membrane is the second pipeline boundary: it separates
;; data-space geometry from output-format bytes. The plan describes
;; what to draw in data coordinates (e.g. a point at (3.4, 7.1) in
;; the color for species `setosa`); the membrane describes the same
;; content in drawing coordinates (e.g. a translation to (218, 134)
;; carrying a colored shape). The plan is renderer-agnostic; the
;; membrane is format-agnostic.
;;
;; Clipping -- keeping each panel's marks within the panel -- is an
;; instance of this same translation. A panel's domain, resolved at the
;; plan stage, is a data-space fact: the range of values the panel
;; shows. The membrane turns it into a drawing-space clip that hides
;; any geometry reaching past that window, so a mark cannot paint
;; outside its panel, or into a neighbor in a multi-panel layout. The
;; [Membranes](./plotje_book.membranes.html#clipping-marks-to-the-panel) chapter shows the
;; mechanism; the [Glossary](./plotje_book.glossary.html#clip) has an entry
;; for **clip**.
;;
;; This boundary lets one membrane tree be rendered to many output
;; formats. The pose, draft, plan, and membrane stages are reused
;; unchanged across formats. A new format that consumes the membrane
;; tree registers a `defmethod` on the render multimethod
;; `scicloj.plotje.impl.render/membrane->plot` (the public
;; `pj/membrane->plot` dispatches through it). A new format that
;; goes from a plan directly to bytes (skipping membrane -- e.g., a
;; Plotly-spec target) registers a `defmethod` on
;; `scicloj.plotje.impl.render/plan->plot` instead. The
;; [Extensibility](./plotje_book.extensibility.html) chapter shows a
;; worked backend registration.
;;
;; The membrane stage of Plotje is built on
;; [Membrane](https://github.com/phronmophobic/membrane) -- the
;; library that defines the primitive types Plotje uses
;; (`Translate`, `WithColor`, `Path`, `Label`, `RoundedRectangle`,
;; ...) and provides the rendering backends. Plotje constructs a
;; membrane tree from a plan; Membrane renders it.
;;
;; Backends Plotje wires into Membrane today:
;;
;; - **SVG hiccup** -- the default. Renders in browsers, in
;;   notebooks via Kindly/Clay, and writes to `.svg` files.
;; - **Java2D / `BufferedImage`** -- raster output via
;;   Membrane's Java2D backend. Used for `.png` files and any
;;   consumer that wants a Java image.
;;
;; Membrane itself supports more rendering targets (terminal,
;; native GUI, GL, ...) than Plotje currently exposes. Wiring a new
;; target into Plotje has not been done end-to-end yet -- the
;; defmethod registration is the extension point, but each backend
;; has its own conventions for opts and interactivity that need to
;; be worked out. As Membrane grows, Plotje can incorporate new
;; targets without changing how plots are described.

;; ## Pipeline Summary

;; | Stage | Type | Coordinates |
;; |:------|:-----|:------------|
;; | Pose | Plain map (leaf or composite) | N/A (declarative) |
;; | Draft | `LeafDraft` (`:layers`, `:opts`) or `CompositeDraft` (`:sub-drafts`, `:chrome-spec`, `:layout`, `:width`, `:height`) record | N/A (declarative) |
;; | Plan | `Plan` or `CompositePlan` record (with `PlanLayer` records and dtype buffers) | Data space |
;; | Membrane | Record tree (membrane.ui primitives in a vector) | Drawing units |
;; | Plot | Hiccup vector (`:svg`) or `BufferedImage` (`:bufimg`) | Drawing units |

;; ## Namespace Structure

^:kindly/hide-code
(kind/mermaid "
graph TD
  API[\"api.clj\"] --> POSE[\"impl/pose.clj\"]
  API --> RES[\"impl/resolve.clj\"]
  API --> PL[\"impl/plan.clj\"]
  API --> COMP[\"impl/compositor.clj\"]
  POSE --> RES
  COMP --> POSE
  COMP --> PL
  PL --> RES
  PL --> STAT[\"impl/stat.clj\"]
  PL --> SCALE[\"impl/scale.clj\"]
  PL --> DEFAULTS[\"impl/defaults.clj\"]
  PL --> PS[\"impl/plan_schema.clj\"]
  API --> RENDER[\"impl/render.clj\"]
  RENDER --> SVG[\"render/svg.clj\"]
  SVG --> MEMBRANE[\"render/membrane.clj\"]
  MEMBRANE --> PANEL[\"render/panel.clj\"]
  PANEL --> MARK[\"render/mark.clj\"]
  PANEL --> SCALE
  PANEL --> COORD[\"impl/coord.clj\"]
  API --> RC[\"render/composite.clj\"]
  RC --> MEMBRANE
  style API fill:#c8e6c9
  style COMP fill:#d1c4e9
  style PL fill:#d1c4e9
  style SVG fill:#f8bbd0
  style MEMBRANE fill:#f8bbd0
  style RC fill:#f8bbd0
")

;; `impl/pose.clj` holds the pose operations: `resolve-tree` (merges
;; mappings, data and options from the root down to every leaf),
;; `elaborate` (writes in what the draft chooses), `expand-facet-nodes`
;; and `fit-layers` (a facet over columns as cells, and the layers each
;; cell draws), `shared-axis-groups` (which cells share an axis), and
;; `leaf->draft` (the leaf flattening that the public `pj/pose->draft`
;; calls).
;;
;; `impl/compositor.clj` handles composite framing layout,
;; `composite-pose->draft`, and `composite-draft->plan` -- pure
;; data-side, no membrane dependency.
;;
;; `impl/plan.clj` holds the leaf-plan computation (domains, ticks,
;; legends, layout) that the public `pj/draft->plan` calls.
;;
;; `impl/resolve.clj` defines the `Plan`, `CompositePlan`,
;; `LeafDraft`, `CompositeDraft`, `PlanLayer`, and `LayerType`
;; records, and holds `resolve-draft-layer` (single draft layer
;; resolution, column type inference, grouping).
;;
;; The `impl/` directory is pure data with no membrane dependency.
;; The `render/` directory uses membrane for layout and SVG/raster
;; conversion. `render/composite.clj` carries the composite
;; `plan->membrane` defmethod and the membrane drawables for
;; composite framing (title, strip labels, shared legend).

;; ## Dependencies
;;
;; Plotje builds on several Clojure libraries:
;;
;; - [Tablecloth](https://scicloj.github.io/tablecloth/) &
;;   [dtype-next](https://github.com/cnuernber/dtype-next) --
;;   dataset manipulation and high-performance numeric arrays
;;
;; - [Membrane](https://github.com/phronmophobic/membrane) --
;;   rendering and layout
;;
;; - [Wadogo](https://github.com/scicloj/wadogo) -- scales
;;
;; - [Clojure2d](https://github.com/Clojure2D/clojure2d) --
;;   color palettes and gradients
;;
;; - [Fastmath](https://github.com/generateme/fastmath) -- statistics
;;
;; - [Malli](https://github.com/metosin/malli) -- schema validation
;;
;; - [Kindly](https://scicloj.github.io/kindly/) &
;;   [Clay](https://scicloj.github.io/clay/) -- notebook rendering

;; ## What's Next
;;
;; - [**Exploring Plans**](./plotje_book.exploring_plans.html) -- a hands-on tour of the plan stage, building intuition for the data shape that the pipeline produces
;; - [**Extensibility**](./plotje_book.extensibility.html) -- add custom marks, stats, scales, coordinate systems, and output formats by extending the multimethods at each pipeline stage
