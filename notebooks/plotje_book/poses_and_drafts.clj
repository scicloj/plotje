;; # Poses and Drafts
;;
;; A pose and a draft are both plain Clojure data, and both can be read
;; at the REPL. What makes them useful for debugging a plot, or for
;; writing a backend that consumes one, is knowing two things the value
;; in front of you does not say on its own: which keys may appear, and
;; what the pipeline has already settled by the time you are looking.
;;
;; [Architecture](./plotje_book.architecture.html#pipeline-overview)
;; shows the five stages and traces one plot through them. This chapter
;; stays on the first two and says what each may contain. The plan is
;; taken apart in [Exploring
;; Plans](./plotje_book.exploring_plans.html#a-minimal-scatter-plot).

(ns plotje-book.poses-and-drafts
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## A pose is a map, and every key is optional

;; One pose runs through the whole chapter: iris petal measurements
;; colored by species, with a scatter and a per-species regression
;; line.

(def iris-pose
  (-> (rdatasets/datasets-iris)
      (pj/pose :petal-length :petal-width {:color :species})
      pj/lay-point
      (pj/lay-smooth {:stat :linear-model})))

iris-pose

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 150 (:points s))
                 (= 3 (:lines s)))))])

;; The value behind that plot:

(kind/pprint iris-pose)

;; Three keys, and each is there because something put it there:
;; `pj/pose` set `:data` and `:mapping`, and the two `pj/lay-*` calls
;; conjoined onto `:layers`.

(-> iris-pose keys sort vec)

(kind/test-last [(fn [ks] (= [:data :layers :mapping] ks))])

;; Four more keys may appear, and none of them is required:
;;
;; - `:poses` holds sub-poses. A pose that has it is a composite; a
;;   pose without it is a leaf, and describes a single panel.
;; - `:layout` says how a composite's sub-poses tile their rectangle.
;; - `:opts` holds the plot options set by `pj/options`, `pj/scale`,
;;   `pj/coord` and `pj/facet`.
;; - `:share-scales` names the axes whose domains a composite pools
;;   across its descendants.
;;
;; Any pose may carry any of them, so there is one shape to read
;; rather than two. `pj/arrange` produces the composite form -- two
;; panels side by side:

(def composite-pose
  (pj/arrange [(pj/lay-point (rdatasets/datasets-iris) :petal-length :petal-width)
               (pj/lay-line (rdatasets/datasets-iris) :petal-length :petal-width)]))

composite-pose

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 150 (:points s))
                 (= 1 (:lines s)))))])

;; The value behind it. The two sub-poses sit under `:poses`, each a
;; leaf of the shape shown above, and the composite adds `:layout`:

(kind/pprint composite-pose)

(-> composite-pose keys sort vec)

(kind/test-last [(fn [ks] (= [:layout :opts :poses] ks))])

;; Its sub-poses are not the two panels. A composite is a tree, and
;; `pj/arrange` builds a grid out of it: the top pose holds one
;; sub-pose per row, stacked vertically, and each row holds the panels
;; in it, side by side. Two panels make one row, so `:poses` has a
;; single entry:

(-> composite-pose :poses count)

(kind/test-last [(fn [n] (= 1 n))])

[(:layout composite-pose)
 (-> composite-pose :poses first :layout)]

(kind/test-last
 [(fn [ls] (= [{:direction :vertical} {:direction :horizontal}] ls))])

;; One level further down are the leaves, each carrying the three keys
;; the first pose in this chapter carried:

(-> composite-pose :poses first :poses count)

(kind/test-last [(fn [n] (= 2 n))])

(-> composite-pose :poses first :poses first keys sort vec)

(kind/test-last [(fn [ks] (= [:data :layers :mapping] ks))])

;; ## What a mapping value may be

;; A mapping's key is an aesthetic. Its value says two things: where
;; the value comes from, and which side of the aesthetic's scale it is
;; read on. Written plainly, two conventions answer for it -- **the
;; layer's data decides the source, and the aesthetic's category
;; decides the scale.**
;;
;; | Reading | When | Example |
;; |:--------|:-----|:--------|
;; | a column, scaled | the layer's data has a column of that name | `{:color :species}` |
;; | a value, drawn | the value is one the aesthetic can draw | `{:color "steelblue"}`, `{:size 4}` |
;; | a value, scaled | written out in full | `{:color {:value "Model A" :scale true}}` |
;; | a column, drawn | written out in full | `{:color {:column :hex :scale false}}` |
;;
;; The value's type decides nothing, which is what lets a dataset
;; built without column names -- whose columns are integers -- map an
;; aesthetic to column 2. A value that is neither is reported, naming
;; both places it was looked for.
;;
;; `{:column c}` and `{:value v}` say which source you want where the
;; data would pick the other source, and `:scale` says which side of the
;; scale, overriding the convention in either direction. `:scale` also
;; takes a scale type or a whole spec, which names *which* scale that
;; one mapping is read through.
;;
;; `nil` is a legal value for every aesthetic. It names nothing, and
;; cancels a mapping inherited from an outer scope.
;;
;; Not every aesthetic has every reading. `:fill` and `:group` take a
;; column and nothing else, so a value that names no column is refused
;; there; `:text` and `:group` have no scale, so a `:scale` is refused
;; on them.
;;
;; [Specifying Aesthetics](./plotje_book.specifying_aesthetics.html)
;; teaches this with a worked example for each reading. What matters
;; here is where it happens: once, at `pj/pose->draft`.

;; ## A mapping carries more than mappings

;; A layer's `:mapping` is not only aesthetics. Every option a `lay-*`
;; call accepts that is not `:data`, `:mark`, `:stat` or `:position`
;; lands there beside them. This pose jitters its points, fixes their
;; radius, and says which space its positions are in:

(def jittered-pose
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width {:jitter 5 :in :data :size 5})))

jittered-pose

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

(kind/pprint jittered-pose)

;; Three of those four options went into the layer's `:mapping`:

(-> jittered-pose :layers first :mapping)

(kind/test-last
 [(fn [m] (= {:jitter 5 :in :data :size 5} m))])

;; `:size` is an aesthetic; `:jitter` and `:in` are not. Nothing in the
;; map distinguishes them, so a reader who wants only the aesthetics
;; has to know which keys those are.
;;
;; Which keys those are is also not a property of the layer. It is a
;; single list for the whole library, so whether `:y-min` is an
;; aesthetic is settled globally, while what `:y-min` *means* is
;; settled by the layer type: `lay-errorbar` reads a column from it and
;; `lay-band-h` reads a value in data space. One key, two readings,
;; and one scope rule carrying both.
;;
;; The positions went elsewhere --
;; `:petal-length` and `:petal-width` were given as arguments rather
;; than in the options map, so they sit on the pose's own `:mapping`
;; and flow down to every layer:

(:mapping jittered-pose)

(kind/test-last
 [(fn [m] (= {:x :petal-length :y :petal-width} m))])

;; ## A draft layer is one layer, fully situated

;; `pj/draft` flattens a pose into one entry per layer that renders.
;; `iris-pose` has two layers and one panel, so its draft has two
;; entries:

(def iris-draft (pj/draft iris-pose))

(kind/pprint iris-draft)

(-> iris-draft :layers count)

(kind/test-last [(fn [n] (= 2 n))])

;; Each entry is a flat map, and every key on it came from one of four
;; places:

(-> iris-draft :layers first keys sort vec)

(kind/test-last
 [(fn [ks] (= [:__panel-idx :color :data :layer-type :mark :stat :x :y] ks))])

;; - `:x`, `:y` and `:color` are the merged mapping. They were written
;;   on the pose, and each layer received a copy, so a layer no longer
;;   needs to know where its mappings came from.
;; - `:layer-type`, `:mark` and `:stat` come from the layer-type
;;   registry, with anything the call site overrode written over them.
;; - `:data` is the dataset this layer draws from -- the layer's own
;;   when it has one, the pose's otherwise.
;; - `:__panel-idx` is an internal marker. Keys prefixed with two
;;   underscores are Plotje's own bookkeeping; they pass through the
;;   later stages and are not for a reader to consume.
;;
;; A leaf draft holds those entries and the pose's options, and nothing
;; else:

(-> iris-draft keys sort vec)

(kind/test-last [(fn [ks] (= [:layers :opts] ks))])

;; ## By draft time, an x or a y names a column

;; A value written for `:x` or `:y` places a mark at that value, which
;; is how a note is put on a plot. Here the note sits at petal length 2
;; and petal width 2, in data space, where the scatter leaves room for
;; it:

(def annotated
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width)
      (pj/lay-text {:x 2 :y 2 :text "a note"})))

annotated

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 150 (:points s))
                 (contains? (set (:texts s)) "a note"))))])

;; The pose holds the two numbers exactly as they were written, on the
;; text layer's own `:mapping`:

(kind/pprint annotated)

(-> annotated :layers second :mapping)

(kind/test-last
 [(fn [m] (= {:x 2 :y 2 :text "a note"} m))])

;; Its draft does not. The text layer names columns, and carries a
;; dataset of its own that the scatter layer beside it does not share:

(def annotated-draft (pj/draft annotated))

(kind/pprint annotated-draft)

(-> annotated-draft :layers second (select-keys [:x :y :text]))

(kind/test-last
 [(fn [m] (= {:x :x :y :y :text :text} m))])

;; The numbers became data. Because no positional aesthetic on that
;; layer named a column, one row holding the values is enough to draw
;; the single mark it asks for, and the synthesized columns take the
;; aesthetics' own names:

(-> annotated-draft :layers second :data)

(kind/test-last
 [(fn [ds] (and (= [:x :y :text] (vec (tc/column-names ds)))
                (= 1 (tc/row-count ds))))])

;; Beside a column the same value broadcasts instead. `{:x 2 :y
;; :petal-width}` labels every row at one x, so the synthesized column
;; is as long as the data -- and it costs one number, because it is a
;; constant reader rather than a materialized column.
;;
;; So a draft layer reads the same way whatever was written: columns
;; named by a mapping. Every later stage -- the stat, the extract, the
;; domains, every mark -- receives that one shape.
;;
;; A written value asked to scale takes the same route. `{:color
;; {:value "Model A" :scale true}}` becomes a constant column named
;; `:color`, and because it is a column like any other it pools into
;; the color scale and earns a legend entry. That is the whole of the
;; feature: no stage after the draft learns a new vocabulary.
;;
;; The exception is a layer with no data anywhere. There is no dataset
;; to add a column to, so the value stays a value and resolves to
;; nothing downstream, exactly as a column reference beside it would.

;; ## A composite draft carries what a leaf draft has nowhere to put

;; The book calls the draft a specification and the plan the stage
;; where geometry is computed. That holds for a leaf. Drafting
;; `composite-pose` from the first section shows that it does not hold
;; for a composite, which carries drawing-space geometry already:

(def composite-draft (pj/draft composite-pose))

(kind/pprint composite-draft)

(-> composite-draft keys sort vec)

(kind/test-last
 [(fn [ks] (= [:chrome-spec :height :layout :sub-drafts :width] ks))])

;; `:layout` gives each sub-draft its rectangle inside the composite,
;; in drawing units:

(:layout composite-draft)

(kind/test-last
 [(fn [m] (= {[0 0] [0.0 0.0 300.0 400.0]
              [0 1] [300.0 0.0 300.0 400.0]}
             m))])

;; A leaf draft carries none of that, and not because the information
;; is unavailable at that point. The difference is arity: a leaf draft
;; is a vector of per-layer maps, and where two panels sit relative to
;; each other is a fact about the set rather than about any one layer.
;; A leaf draft has nowhere to put it.

;; ## The plan is the stage that is checked

;; All three stages have a Malli schema. Only the plan's runs: `pj/plan`
;; validates its result unless the `:validate` configuration key turns
;; it off, and `pj/valid-plan?` runs the same check by hand.

(-> iris-pose pj/plan pj/valid-plan?)

(kind/test-last [true?])

;; A malformed plan reports which part failed:

(-> iris-pose pj/plan (assoc :width "not-a-number") pj/explain-plan some?)

(kind/test-last [true?])

;; The pose and draft schemas are documentation rather than
;; enforcement. Nothing calls them at runtime, so a pose is
;; structurally valid by convention: `pj/pose` and the `lay-*`
;; functions check their own arguments as they build it, and a
;; mistake surfaces where it is made rather than at a validation gate.

;; ## See Also
;;
;; - [Architecture](./plotje_book.architecture.html#the-single-step-transitions)
;;   -- the five stages and the function for each transition.
;; - [Exploring Plans](./plotje_book.exploring_plans.html#a-minimal-scatter-plot)
;;   -- what the next stage computes.
;; - [Glossary](./plotje_book.glossary.html#draft) -- pose, draft, draft
;;   layer, data space, drawing space.
