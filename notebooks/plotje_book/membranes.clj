;; # Membranes
;;
;; The fourth stage of Plotje's pipeline turns a fully-resolved plan
;; into a `PlotjeMembrane`: a tree of layout-and-drawing primitives
;; sized exactly for the output canvas, ready to be rendered to SVG,
;; PNG, or any other format the
;; [Membrane](https://github.com/phronmophobic/membrane) library
;; supports.
;;
;; A `PlotjeMembrane` is itself a Membrane UI component. It implements
;; the standard Membrane protocols (`IOrigin`, `IBounds`, `IChildren`),
;; so any tool that consumes Membrane components consumes a Plotje
;; plot without special-casing, and any Plotje plot can be composed
;; into a larger Membrane interface alongside hand-built Membrane
;; elements. This chapter walks the membrane stage end to end and
;; shows that interop in action.

(ns plotje-book.membranes
  (:require
   ;; Kindly notebook annotations
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje, the public API
   [scicloj.plotje.api :as pj]
   ;; Rdatasets for example data
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Membrane core: protocols and primitives
   [membrane.ui :as ui]))

;; ## A running example
;;
;; Throughout the chapter we lift one iris scatter into the membrane
;; stage. The pose looks like:

(def iris-pose
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width
                    {:color :species})
      (pj/options {:title "Iris" :y-label "width"})))

iris-pose

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 150 (:points s)))))])

;; ## Producing a membrane
;;
;; The composition shortcut `pj/membrane` runs the full pipeline up
;; through the membrane stage in one call:

(def iris-membrane (pj/membrane iris-pose))

(pj/membrane? iris-membrane)

(kind/test-last [true?])

;; The atomic step `pj/plan->membrane` does the same starting from an
;; already-resolved plan -- handy when you have a plan in hand from
;; inspection and want to skip re-running the earlier stages:

(def iris-plan (pj/plan iris-pose))

(pj/membrane? (pj/plan->membrane iris-plan))

(kind/test-last [true?])

;; Both routes return the same shape: a `PlotjeMembrane` record.

;; ## Anatomy
;;
;; The record carries three structural fields plus optional
;; `:plotje/`-namespaced attributes. The full value prints with
;; every drawable in `:drawables`, so it is long, but every part is
;; visible:

iris-membrane

;; A summary view of the structural pieces:

{:width       (ui/width iris-membrane)
 :height      (ui/height iris-membrane)
 :origin      (ui/origin iris-membrane)
 :title       (:plotje/title iris-membrane)
 :n-drawables (count (ui/children iris-membrane))}

(kind/test-last
 [(fn [info]
    (and (= 600 (:width info))
         (= 400 (:height info))
         (= [0 0] (:origin info))
         (= "Iris" (:title info))
         (= 9 (:n-drawables info))))])

;; The width and height come from the plan -- the membrane stage does
;; not recompute them; it inherits the canvas size the plan
;; resolved. The origin is `[0 0]` because a `PlotjeMembrane` is the
;; full canvas; when it is embedded in a larger Membrane layout, the
;; layout's translation places it. The top-level drawables for this
;; iris scatter are the title, the axis labels, the panel (its
;; background and grid, and the marks drawn on it), and the legend
;; (a heading plus one row per category).

;; The map keys, as the record presents them, are three record
;; fields plus the namespaced title:

(sort (filter keyword? (keys iris-membrane)))

(kind/test-last
 [(fn [ks] (= [:drawables :height :width :plotje/title] ks))])

;; ## The Membrane protocols
;;
;; A `PlotjeMembrane` implements three Membrane UI protocols:
;;
;; - `IOrigin` -- the component's top-left corner inside its parent.
;;   Plotje's membrane is the canvas itself, so `(membrane.ui/origin m)`
;;   returns `[0 0]`.
;; - `IBounds` -- the component's `[width height]`. Read directly via
;;   `(membrane.ui/width m)` and `(membrane.ui/height m)`, both of
;;   which derive from `(membrane.ui/bounds m)`.
;; - `IChildren` -- the sub-elements for traversal. Returns the
;;   underlying `:drawables` vector.
;;
;; Why these three and not more? Membrane's protocols partition into
;; "what every UI element supports" (origin, bounds, children) and
;; "what the element does" (drawing, mouse events, key events, ...).
;; Plotje implements the first set so a `PlotjeMembrane` participates
;; in every Membrane consumer that walks a tree generically. Drawing
;; is delegated to the children: when a Membrane backend draws our
;; record, the default `IDraw` impl walks `(children record)` and
;; draws each child individually -- the existing `Translate`,
;; `WithColor`, `Path`, `Label` primitives already have per-backend
;; draw implementations.

;; ## Title and namespaced attributes
;;
;; The plot title rides as `:plotje/title` -- not a defrecord field
;; but a namespaced map entry assoc'd onto the record. This keeps the
;; record's arity stable as we add per-membrane attributes in the
;; future. A pose without a title produces a membrane without that
;; key:

(:plotje/title (pj/membrane (-> (rdatasets/datasets-iris)
                                (pj/lay-point :sepal-length :sepal-width))))

(kind/test-last [nil?])

;; Future per-membrane attributes will use the same `:plotje/*`
;; namespace (for instance `:plotje/subtitle` or `:plotje/caption`).
;; A backend reading the membrane can pick up whichever ones it
;; recognizes and ignore the rest -- the `:plotje/` prefix makes our
;; keys collision-free with any other library composing into the
;; same map.

;; ## Composing with other Membrane components
;;
;; Because a `PlotjeMembrane` is a Membrane UI component, you can
;; drop it into any Membrane layout. Two Plotje plots side by side
;; is one line:

(def two-up
  (ui/horizontal-layout
   (pj/membrane (-> (rdatasets/datasets-iris)
                    (pj/lay-point :sepal-length :sepal-width
                                  {:color :species})
                    (pj/options {:title "Sepal length vs sepal width"
                                 :y-label "width"})))
   (pj/membrane (-> (rdatasets/datasets-iris)
                    (pj/lay-point :sepal-length :petal-length
                                  {:color :species})
                    (pj/options {:title "Sepal length vs petal length"
                                 :y-label "petal"})))))

;; The result is itself a Membrane component with bounds derived from
;; its children. Two 600 by 400 plots side by side, with a 1-unit gap
;; that `horizontal-layout` inserts, give a 1201 by 400 canvas:

{:width  (ui/width two-up)
 :height (ui/height two-up)}

(kind/test-last
 [(fn [info]
    (and (= 1201 (:width info))
         (= 400 (:height info))))])

;; The combined component is no longer a `PlotjeMembrane` (it is
;; whatever `horizontal-layout` chose to return -- here, a vector of
;; the two children), but it speaks the same protocol vocabulary, so
;; any Membrane backend can render it. To produce a raster image we
;; bypass `pj/membrane->plot` (which validates a `PlotjeMembrane`
;; specifically) and call Membrane's Java2D backend directly:

(def two-up-png
  ((requiring-resolve 'membrane.java2d/draw-to-image)
   two-up
   [(ui/width two-up) (ui/height two-up)]))

(instance? java.awt.image.BufferedImage two-up-png)

(kind/test-last [true?])

;; The same pose stages, rendered as raster:

two-up-png

;; This is why the membrane stage matters as a separate boundary.
;; Plotje produces values that any Membrane consumer understands;
;; Plotje plots compose with hand-built Membrane elements; nothing
;; about the value type is Plotje-specific beyond the namespaced
;; title key.

;; ## Rendering through Plotje's backends
;;
;; The composition shortcut `pj/plot` is the convenience case; the
;; explicit step `pj/membrane->plot` dispatches on a format keyword.
;; `:svg` is always available -- the path that auto-rendered
;; `iris-pose` at the top of the chapter:

(pj/membrane->plot iris-membrane :svg {})

(kind/test-last [(fn [v] (= :svg (first v)))])

;; `:bufimg` is registered when `scicloj.plotje.render.bufimg` is
;; loaded; it produces a Java `BufferedImage`, the raster form used
;; in the previous section's composition:

(pj/membrane->plot iris-membrane :bufimg {})

(kind/test-last [(fn [v] (instance? java.awt.image.BufferedImage v))])

;; The membrane stage is format-agnostic: the same `iris-membrane`
;; produced one valid value, and that value renders to multiple
;; output formats without re-walking the plan. Adding a new format
;; means writing a `defmethod` on
;; `scicloj.plotje.impl.render/membrane->plot`. The
;; [Extensibility](./plotje_book.extensibility.html) chapter walks a
;; worked example.

;; ## Clipping marks to the panel
;;
;; Clipping -- keeping each panel's marks inside the panel (the
;; [Glossary](./plotje_book.glossary.html) entry for clip defines the
;; term) -- needs no special machinery in the membrane stage: the clip
;; is just another node in the tree. The panel's marks are wrapped in a
;; `membrane.ui/scissor-view`, a Membrane primitive that masks its
;; contents to a rectangle, so the clip can be found and inspected like
;; any other drawable.
;;
;; A panel uses two clip rectangles. Data marks clip to the drawing
;; area (the grey panel background); rug ticks, which sit in the axis
;; margin on purpose, clip to the wider panel box. Which rectangle a
;; mark uses is itself an extension point -- the
;; [Extensibility](./plotje_book.extensibility.html) chapter covers it.
;;
;; Build a membrane with one mark of each kind: a line running far past
;; a tightened y-domain (a data mark) and a rug (a margin mark).

(def clipped-membrane
  (-> {:x [1 2 3 4 5] :y [10 20 15 25 18]}
      (pj/lay-point :x :y)
      (pj/lay-line {:data {:x [1 5] :y [-200 300]}})
      (pj/lay-rug :x)
      (pj/scale :y {:type :linear :domain [0 30]})
      pj/membrane))

;; The scissors sit inside the panel's drawable, so finding them means
;; walking the tree. Each one's clip rectangle (offset and bounds, in
;; drawing units): the data scissor at the drawing area, its offset the
;; panel margin; the rug scissor at the panel box, its offset [0 0].

(->> (ui/children clipped-membrane)
     (tree-seq coll? seq)
     (filter #(instance? membrane.ui.ScissorView %))
     (mapv #(select-keys % [:offset :bounds])))

(kind/test-last
 [(fn [rects]
    (and (= 2 (count rects))
         ;; one clips to the panel box (offset [0 0]), one to the
         ;; drawing area (offset = the positive panel margin)
         (some #(= [0 0] (:offset %)) rects)
         (some #(every? pos? (:offset %)) rects)))])

;; ## Schema
;;
;; Plotje publishes a Malli schema for the `PlotjeMembrane` shape.
;; Backend authors validating an incoming membrane (or constructing
;; their own for testing) can use it:

(pj/valid-membrane? iris-membrane)

(kind/test-last [true?])

;; A non-membrane value fails validation:

(some? (pj/explain-membrane {:not :a-membrane}))

(kind/test-last [true?])

;; ## See also
;;
;; - [Architecture](./plotje_book.architecture.html) -- the full
;;   five-stage pipeline and where the membrane stage fits.
;; - [Extensibility](./plotje_book.extensibility.html) -- adding a
;;   new format by registering a `membrane->plot` defmethod, plus
;;   the other extension points (marks, stats, scales, coordinates).
