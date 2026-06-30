;; # Poses
;;
;; Plotje is a composable plotting library inspired by
;; Wilkinson's [Grammar of Graphics](https://link.springer.com/book/10.1007/0-387-28695-0)
;; and Julia's [AlgebraOfGraphics.jl](https://aog.makie.org/stable/).
;; Its operators are shaped by Clojure idioms -- threading, merge,
;; plain maps -- rather than a custom DSL.
;;
;; Plotje calls the value at the heart of each plot a **pose**.
;; The word is photographic: a pose is the arrangement you settle
;; into before the picture is taken. In Plotje, the same shape is a
;; plain Clojure value -- which columns become axes, which become
;; color, which chart-type layers sit on top -- that you build,
;; inspect, and recombine until you call `pj/plot` and capture it.
;;
;; This chapter introduces the pose value step by step. Each
;; section shows a rendered plot followed by the printed pose
;; value, so you can see both what the library produces and the
;; data structure underneath.

(ns plotje-book.pose-model
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## A pose describes a plot
;;
;; A pose flows through the pipeline. The composition functions --
;; `pj/pose`, `pj/lay-*`, `pj/options`, `pj/scale`, `pj/coord`,
;; `pj/facet`, `pj/arrange` -- all take a pose and return a pose,
;; so plots build up through ordinary `->` threading. When the pose
;; is ready, `pj/plot` renders it. (`pj/plot` is one of a few output
;; functions that turn a finished pose into a rendered result; the
;; [Architecture](./plotje_book.architecture.html) chapter walks the
;; full pipeline behind it.)
;;
;; The simplest pose carries some data and picks columns. With no
;; explicit chart type, the library infers one from the column
;; types:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Two numerical columns produced a scatter. And because a pose is
;; plain data, you can inspect it with `kind/pprint`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    kind/pprint)

(kind/test-last [(fn [v] (and (seq (:data v))
                              (= :sepal-length (:x (:mapping v)))))])

;; A pose is a plain Clojure map. The dataset lives under `:data`,
;; the column mapping under `:mapping`, chart-type layers under
;; `:layers` (empty here, since none was attached), and plot-level
;; options under `:opts`.

;; ## Poses carry mappings
;;
;; A **mapping** connects columns to visual properties. We added
;; `:x` and `:y` above; the pose also accepts appearance
;; aesthetics like `:color`, `:size`, `:alpha`, and `:shape`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; The printed pose shows the full mapping:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    kind/pprint)

(kind/test-last [(fn [v] (= :species (:color (:mapping v))))])

;; All three pairs -- `:x -> :sepal-length`, `:y -> :sepal-width`,
;; `:color -> :species` -- end up in the one `:mapping` map. Future
;; layers on this pose will inherit the whole set.

;; ## Poses carry layers
;;
;; A **layer** is a chart-type recipe -- mark, stat, position --
;; attached to a pose. The pose says what to plot (its mapping
;; picks the columns); the layer says how it is drawn (points, a
;; smooth, error bars). Several layers attached to one pose share
;; its mapping and coordinate system, so a scatter and its
;; regression line stack as one picture. This split -- mapping for
;; what, layer for how -- runs through the rest of the library.
;; Written as a literal map, `pj/pose` accepts the
;; nested-map shape directly:

(def multi-layer
  (pj/pose
   {:mapping {:x :sepal-length :y :sepal-width :color :species}
    :layers [{:layer-type :point}
             {:layer-type :smooth :stat :linear-model}]
    :data (rdatasets/datasets-iris)}))

multi-layer

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; Printed, the mapping and the layers are visibly separate:

(kind/pprint multi-layer)

(kind/test-last [(fn [v] (and (= 2 (count (:layers v)))
                              (= :species (:color (:mapping v)))))])

;; The threaded form builds the same pose step by step: `pj/pose`
;; sets the mapping, then each `pj/lay-*` appends a layer.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; Printed, the threaded form produces the same shape as the
;; explicit-map form shown earlier -- `:mapping` at the top,
;; `:layers` alongside it.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    kind/pprint)

(kind/test-last [(fn [v] (and (= 2 (count (:layers v)))
                              (= :species (get-in v [:mapping :color]))))])

;; ## Layer order is paint order
;;
;; Layers paint in the order they are added: the first `lay-*` call
;; renders first, and each later one on top of it. When layers overlap,
;; that decides what stays visible -- so add the layer you want on top
;; last.
;;
;; Two points at one spot. The large one is added first and the small
;; one second, so the small dot reads on top of the large one:

(-> {:x [1] :y [1]}
    (pj/lay-point :x :y {:size 30 :color "#a6cee3"})
    (pj/lay-point :x :y {:size 12 :color "#1f78b4"}))

(kind/test-last
 [(fn [fr]
    (= [30 12]
       (->> fr pj/plan :panels first :layers
            (mapv (comp :radius :style)))))])

;; The same rule lets a value label sit on a bar: add the label after
;; the bar so it paints on top, and use `:align-x :right` so the label
;; tucks inside the bar's end rather than spilling past it. (Bars and
;; `pj/coord` are covered later; anchoring is the Text and Label
;; Placement section of
;; [Customization](./plotje_book.customization.html), and the
;; [Cookbook](./plotje_book.cookbook.html) has a value-labels recipe.)

(-> {:species ["setosa" "versicolor" "virginica"]
     :pct     [33.3 33.3 33.3]}
    (pj/lay-bar :species :pct {:color "#a6cee3"})
    (pj/lay-text :species :pct {:text :pct :align-x :right})
    (pj/coord :flip))

(kind/test-last
 [(fn [fr]
    (= [:rect :text]
       (->> fr pj/plan :panels first :layers (mapv :mark))))])

;; Swapping the two calls swaps which layer paints on top -- paint
;; order follows the order layers were added, not the layer type.

(kind/test-last
 [(fn [_]
    (let [marks (fn [pose]
                  (->> pose pj/plan :panels first :layers (mapv :mark)))
          data {:species ["setosa" "versicolor" "virginica"]
                :pct     [33.3 33.3 33.3]}]
      (and (= [:rect :text]
              (marks (-> data
                         (pj/lay-bar :species :pct)
                         (pj/lay-text :species :pct {:text :pct}))))
           (= [:text :rect]
              (marks (-> data
                         (pj/lay-text :species :pct {:text :pct})
                         (pj/lay-bar :species :pct)))))))])

;; ## Inference fills the gaps
;;
;; When you omit a choice, Plotje infers it from the data.
;; One numerical column becomes a histogram:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; The printed pose shows an empty `:layers` -- the histogram
;; layer is chosen by inference at render time, not stored on the
;; pose:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length)
    kind/pprint)

(kind/test-last [(fn [v] (empty? (:layers v)))])

;; The principle: **the inferred value fills in only when you have
;; not specified one yourself.** An explicit choice is always kept;
;; inference supplies only what you leave out. (How explicit choices
;; override inherited ones -- including the special case of an
;; explicit `nil` -- comes up once scope is in play; see Core
;; Concepts and Pose Rule S3.)
;;
;; Inference covers marks (the shape shown, like points or bars),
;; stats (the computation before rendering, like binning), color
;; types, and grouping. See
;; [Inference Rules](./plotje_book.inference_rules.html) for the
;; full set.

;; ## Poses compose
;;
;; The poses we have built so far are **leaf** poses -- each
;; describes a single plot panel with its own data, mapping, and
;; layers. A **composite** pose is the other shape: a plain map
;; that holds sub-poses under `:poses` and an optional `:layout`
;; describing how to tile them. Composition functions take any
;; pose and return a pose. Here is a two-panel composite written
;; as an explicit map:

(def two-panel
  (pj/pose
   {:layout {:direction :horizontal}
    :poses [{:mapping {:x :sepal-length :y :sepal-width :color :species}
             :layers [{:layer-type :point}]}
            {:mapping {:x :petal-length :y :petal-width :color :species}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

two-panel

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; Printed, its structure is visible at once:

(kind/pprint two-panel)

(kind/test-last [(fn [v] (and (= 2 (count (:poses v)))
                              (= :horizontal (get-in v [:layout :direction]))))])

;; The outer `:data` is inherited by every sub-pose. `pj/arrange`
;; is the convenient way to build this shape from a list of already-
;; built poses:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (pj/pose :sepal-length :sepal-width {:color :species})
      pj/lay-point)
  (-> (rdatasets/datasets-iris)
      (pj/pose :petal-length :petal-width {:color :species})
      pj/lay-point)])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Printed, `pj/arrange` always wraps its plots in a top-level
;; vertical layout whose rows are horizontal strips -- a single
;; row of two panels shows up here as a vertical-of-horizontal
;; composite. The sub-poses themselves are clean leaf maps that
;; match the shape of the explicit-map form above:

(-> (pj/arrange
     [(-> (rdatasets/datasets-iris)
          (pj/pose :sepal-length :sepal-width {:color :species})
          pj/lay-point)
      (-> (rdatasets/datasets-iris)
          (pj/pose :petal-length :petal-width {:color :species})
          pj/lay-point)])
    kind/pprint)

(kind/test-last [(fn [v] (and (= :vertical (get-in v [:layout :direction]))
                              (= 1 (count (:poses v)))
                              (= 2 (count (:poses (first (:poses v)))))
                              (= :horizontal (get-in v [:poses 0 :layout :direction]))))])

;; Composites nest the same
;; shape inside `:poses`. The
;; [Composition](./plotje_book.composition.html) chapter covers
;; the multi-pose patterns in depth.

;; ## Summary
;;
;; | Concept | In code |
;; |:--------|:--------|
;; | A pose describes a plot | `pj/pose`, `pj/lay-*` return poses; inspect with `kind/pprint` |
;; | Poses carry mappings | Column-to-aesthetic pairs live in `:mapping` |
;; | Poses carry layers | Chart-type recipes attached via `pj/lay-*`, listed in `:layers` |
;; | What vs how | `pj/pose` declares what; `pj/lay-*` declares how |
;; | Inference fills gaps | Omit choices, the library infers from data |
;; | Poses compose | `pj/arrange` tiles sibling poses; composites nest under `:poses` |
;;
;; ## What's Next
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- data formats, marks, stats, color, grouping, coordinates
;; - [**Composition**](./plotje_book.composition.html) -- composite poses and multi-panel patterns in depth
;; - [**Inference Rules**](./plotje_book.inference_rules.html) -- how Plotje chooses defaults
;; - [**Relationships**](./plotje_book.relationships.html) -- scatter, regression, density, and SPLOM examples to explore
