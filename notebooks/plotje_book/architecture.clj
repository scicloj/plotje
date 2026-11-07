;; # Architecture
;;
;; Plotje has a five-stage pipeline: a **pose** becomes a **draft**,
;; then a **plan**, a **membrane**, and finally a **plot**. Each stage is produced from the
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
   ;; Plotje internals -- read in "How Panels Share an Axis" and "Poses rewritten before drafting"
   [scicloj.plotje.impl.pose :as pose]
   [scicloj.plotje.impl.plan :as plan]))

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
;;   into the plan stage). A composite pose, and a leaf with a facet
;;   over columns, which is drawn as one cell per column, produce a
;;   `CompositeDraft` instead, carrying per-leaf drafts
;;   (`:sub-drafts`), the resolved composite-framing geometry (`:chrome-spec`),
;;   the layout map from leaf path to rect (`:layout`), and the
;;   composite's overall dimensions (`:width`, `:height`). A leaf draft
;;   holds no geometry. A composite draft already holds where each cell
;;   sits on the canvas, in drawing units, because the sizes of the
;;   cells are decided before the cells are planned. Produced by
;;   `pj/pose->draft`.
;;
;; - **Plan** -- fully resolved geometry in data space (domains,
;;   ticks, legends, computed shapes), and the layout of the canvas in
;;   drawing units. A `Plan` record (composite
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
;; Every intermediate value can be inspected with `kind/pprint`. The
;; same pipeline can be
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
;;
;; A draft layer's `:data` carries one column the pose was not given:
;; `:scicloj.plotje/row`, the row of the pose's dataset each row came
;; from. A facet splits the data and a series pivots it, and either
;; numbers the resulting rows from zero again, so the row number is
;; attached before both. `:brush true` reads it, which is how
;; highlighting a point in one panel highlights the same row in the
;; others. Like the double-underscore keys, the column is Plotje's: no
;; mapping can name it, and no list of available columns shows it.

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

;; The membrane carries the drawables plus the canvas size and title.
;; The record fields `:width` and `:height` give the canvas size, the
;; size the plan settled on, and `:plotje/title` carries the title
;; when one is set. This plot sets none:

(select-keys trace-membrane [:width :height :plotje/title])

(kind/test-last [(fn [m] (and (= #{:width :height} (set (keys m)))
                              (== (:total-width trace-plan) (:width m))
                              (== (:total-height trace-plan) (:height m))))])

;; The drawables sit in the record's `:drawables` field, a vector of
;; Membrane primitives. Their class names:

(->> trace-membrane
     :drawables
     (mapv #(.getSimpleName (class %))))

(kind/test-last
 [(fn [names]
    (and (seq names)
         (every? #{"Translate"} names)))])

;; The dedicated [Membranes](./plotje_book.membranes.html#the-membrane-protocols) chapter
;; walks the record's protocols, the namespaced-attribute convention,
;; and how a `PlotjeMembrane` composes with hand-built Membrane
;; elements.

;; ### Step 5: pj/membrane->plot
;;
;; Convert the membrane into the rendered output for a chosen
;; format. Dispatches on the format keyword.
;; This is the only stage whose output is format-native rather than
;; portable: every earlier stage is the same across backends, while
;; here the membrane becomes SVG hiccup, a BufferedImage, or another
;; format's bytes.
;; The membrane carries its plan-derived dimensions as record
;; fields (read via `(membrane.ui/width m)`/`(height m)`), so
;; `pj/membrane->plot` does not need them written again in its options:

(def trace-plot
  (pj/membrane->plot trace-membrane :svg {}))

;; The plot is SVG hiccup, a vector starting with `:svg`.
;; `pj/svg-summary` counts what it draws -- one panel, a point per row
;; of the data, and a regression line per species:

(pj/svg-summary trace-plot)

(kind/test-last [(fn [s] (and (vector? trace-plot)
                              (= :svg (first trace-plot))
                              (= 1 (:panels s))
                              (= 150 (:points s))
                              (= 3 (:lines s))))])

;; Rendered:

(kind/hiccup trace-plot)

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Two formats are built in. `:svg` gives SVG hiccup, which renders in
;; browsers and in notebooks through Kindly and Clay, and is written to
;; `.svg` files. `:bufimg` gives a Java2D `BufferedImage` through
;; Membrane's Java2D backend, and is used for `.png` files. A new format
;; that consumes the membrane registers a `defmethod` on the render
;; multimethod `scicloj.plotje.impl.render/membrane->plot`, which the
;; public `pj/membrane->plot` dispatches through. A format that goes from
;; a plan directly to a figure, skipping the membrane -- for example a
;; Plotly-spec target -- registers a `defmethod` on
;; `scicloj.plotje.impl.render/plan->plot` instead, and is called with
;; `pj/plan->plot`; `pj/plot` always renders through the membrane. The
;; [Extensibility](./plotje_book.extensibility.html) chapter shows a
;; worked backend registration.

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
;; - **pj/draft** -- from raw input to a draft.
;; - **pj/plan** -- from raw input to a plan.
;; - **pj/membrane** -- from raw input to a membrane tree.
;; - **pj/plot** -- from raw input to a rendered figure.
;; - **pj/save** -- from raw input to a file on disk (renders, then
;;   writes).
;;
;; `pj/draft->membrane` and `pj/draft->plot` start the same chain from
;; a draft rather than from raw input.
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
;;      ;; render the plan to fmt bytes, write them to path,
;;      ;; and return the java.io.File written
;;      (write-file path (plan->plot (plan pose) fmt (:opts pose))))))
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
;; disk, returning the file it wrote as a `java.io.File` (`str` of it
;; is the path). `resolve-format` and `write-file` above stand in for
;; that format-resolution and file-writing detail.
;;
;; The 2-arity of each function folds the options map into the pose
;; using `pj/options` before recursing into the 1-arity.
;;
;; `pj/membrane` is the analogous shortcut for the membrane stage,
;; useful for a caller that needs a membrane tree without choosing
;; an output format yet -- for example a custom backend, or a target
;; that Membrane supports but Plotje has not wired in yet.
;;
;; Because the compositions call the steps, redefining a step with
;; `with-redefs` takes effect in `pj/draft`, `pj/plan`, `pj/membrane`
;; and `pj/plot`. `pj/save` calls the render multimethods without
;; going through the public step functions, so a `with-redefs` of
;; `pj/plan->membrane` does not reach it.

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
;;   in `pj/pose->draft`, after a facet over columns is expanded into
;;   cells, on every leaf, cells of a
;;   composite included. It writes the default in two cases: where no
;;   mapping and no layer names an `:x` or a `:y` column, and where the
;;   first layer names no column, whatever later layers name (the
;;   example below this list shows a later layer naming the default's
;;   columns).
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

;; ### What elaboration writes
;;
;; Before it flattens a pose, `pj/pose->draft` writes into the pose
;; what the draft would otherwise have to choose, in this order: the
;; rows the pose keeps (its `:where`); a series pivoted into a key
;; column and a value column; the default mapping of data with one to
;; three columns; one layer, where the pose names columns and has no
;; layer; the panel's columns, into each layer that reads them; a layer
;; type for each layer left to inference; and the flip of a column
;; named on `:y` alone. The function that does this is `pose/elaborate`.
;; [Panels](./plotje_book.panels.html#the-panels-columns) shows which
;; of the panel's columns a layer reads, and
;; [Layer Types](./plotje_book.layer_types.html#columns-a-layer-type-draws-from)
;; shows the flip.
;;
;; Where the layers of a panel draw different columns and no layer is
;; colored by a column, `pj/pose->draft` also gives each layer that
;; specifies no `:color` a color and a legend entry naming its columns,
;; as [Panels](./plotje_book.panels.html#the-panel-of-a-leaf-pose) shows.

;; ## Composite Poses
;;
;; A composite pose -- one with `:poses` inside --
;; flows through the same steps. Each step dispatches
;; internally on shape: a leaf pose produces a `LeafDraft`; a
;; composite pose, and a leaf with a facet over columns, produce a
;; `CompositeDraft`. The user-facing pipeline is unchanged.

(def composite-pose
  (-> (rdatasets/datasets-iris)
      (pj/arrange [{:x :petal-length :y :petal-width}
                   {:x :sepal-length :y :sepal-width}])
      (pj/pose {:color :species})))

composite-pose

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; `pj/draft` (the composition of `->pose`, `infer-mapping` and
;; `pose->draft`) returns a `CompositeDraft` -- wrap in `kind/pprint` to
;; inspect:

(-> composite-pose
    pj/draft
    kind/pprint)

(kind/test-last [(fn [d] (and (pj/composite-draft? d)
                              (= 2 (count (:sub-drafts d)))))])

;; Each entry of `:sub-drafts` is a map. It holds the cell's place on
;; the canvas (`:path` in the grid, and `:rect` in drawing units), the
;; options the arrangement decided for the cell under `:opts` -- its
;; size, and which legends, ticks and axis titles it suppresses -- and
;; the cell's draft layers under `:draft`, the vector a `LeafDraft`
;; holds as `:layers`. With the draft layers counted, so that the entry
;; prints readably:

(-> composite-pose
    pj/draft
    :sub-drafts
    first
    (update :draft count))

(kind/test-last [(fn [sub-draft]
                   (and (= #{:path :rect :opts :draft} (set (keys sub-draft)))
                        (= [0 0] (:path sub-draft))
                        (= 1 (:draft sub-draft))
                        ;; One legend for the arrangement, drawn beside
                        ;; it rather than in this cell.
                        (true? (-> sub-draft
                                   :opts
                                   :suppress-color-legend))))])

;; `pj/plan` returns a `CompositePlan`:

(pj/plan composite-pose)

(kind/test-last [(fn [p] (and (pj/composite-plan? p)
                              (= 2 (count (:sub-plots p)))))])

;; Each entry of `:sub-plots` holds the same place and options, and the
;; cell's leaf `Plan` under `:plan`:

(-> composite-pose
    pj/plan
    :sub-plots
    first
    :plan
    pj/leaf-plan?)

(kind/test-last [true?])

;; `pj/membrane` returns a `PlotjeMembrane` whose `:drawables` carry
;; one `Translate` per leaf plus composite framing (column strip
;; labels, shared legend, title if any). Plan-derived width and height
;; are record fields, and the title is `:plotje/title`.

(-> composite-pose
    pj/membrane
    (select-keys [:width :height :plotje/title]))

(kind/test-last [(fn [m] (and (number? (:width m))
                              (number? (:height m))))])

(->> composite-pose
     pj/membrane
     :drawables
     (mapv #(.getSimpleName (class %))))

(kind/test-last [(fn [names] (every? #{"Translate"} names))])

;; `pj/plot` (the full pipeline) returns the SVG hiccup that
;; `composite-pose` auto-renders to at the top of this section:

(-> composite-pose
    pj/plot
    pj/svg-summary)

(kind/test-last [(fn [s] (and (= 2 (:panels s))
                              (= 300 (:points s))))])

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
;; numbers, or the same categories, in every panel. Plotje draws
;; several panels in two ways, and both follow one rule: **a shared
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
;; group. Which axes are shared by default, and the other values
;; `:share-scales` takes, are in
;; [Composition](./plotje_book.composition.html#shared-scales).

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

;; Two details complete the grouping. A cell whose layers draw two
;; columns on an axis belongs to the groups of both columns, and the two
;; groups become one: three cells sharing `:y`, drawing `:adults`,
;; `:children`, and both, are one group. And a cell's numeric domain is
;; widened after planning so that its text marks are drawn in full;
;; cells of a group that end on different domains are planned again with
;; the widest domain as a floor, so that they agree.
;;
;; A sharing cell's stats run twice: once to measure it, and once to
;; plan it with the pooled extent. A cell that shares with no other
;; cell is not measured.
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
;; column the series pivot adds asks for the same panels, so it is first
;; rewritten into a facet over the columns of the series, and then
;; expanded like any other, here on weekly counts of visits:

(def weekly-visits
  {:week [1 2 3 4]
   :adults [30 25 28 35]
   :children [20 15 18 22]})

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

;; From here the pose is an ordinary composite: its cells are grouped,
;; measured and planned as the two clinics above were. Cells that
;; split the data of one pose share by default each axis on which they
;; draw one column. In the series faceted by its key, each cell draws a
;; column of its own on `:y`, so the two cells share `:x` and not `:y`:

(->> series-by-key
     pj/draft
     :sub-drafts
     (mapv :share))

(kind/test-last
 [(fn [shares] (= [{:x {:group 0}} {:x {:group 0}}] shares))])

;; Cells written by hand, each keeping the rows of one value of a
;; column with `:where`, that together keep every value of the column
;; are rewritten the other way, into one pose faceted by that column
;; (`pose/collapse-regular-split`).

;; ## Where a Mistake Is Reported
;;
;; When a pose cannot be drawn, which step reports the error depends on
;; what the error depends on. A call reports what its own arguments
;; decide. `pj/pose->draft` reports what depends on which columns the
;; data has, and on how the layers of a panel or the cells of a facet
;; relate. `pj/draft->plan` reports what is known once a layer's stat
;; and mark meet the values of its columns -- a bar given two
;; categorical columns, a mark polar coordinates have no drawing for --
;; and panels too small for the canvas.
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
         ["A key pj/facet's map does not accept"
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

;; ## Pipeline Summary

;; | Stage | Type | Coordinates |
;; |:------|:-----|:------------|
;; | Pose | Plain map (leaf or composite) | N/A (declarative) |
;; | Draft | `LeafDraft` (`:layers`, `:opts`) or `CompositeDraft` (`:sub-drafts`, `:chrome-spec`, `:layout`, `:width`, `:height`) record | None for a leaf; drawing units for where a composite's cells sit (`:layout`) |
;; | Plan | `Plan` or `CompositePlan` record (with `PlanLayer` records and dtype buffers) | Data space (marks); drawing units (layout) |
;; | Membrane | Record tree (membrane.ui primitives in a vector) | Drawing units |
;; | Plot | Hiccup vector (`:svg`) or `BufferedImage` (`:bufimg`) | Drawing units |

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
