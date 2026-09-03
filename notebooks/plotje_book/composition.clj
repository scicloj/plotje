;; # Composition
;;
;; Plotje's poses let you combine whole plots into a
;; single rendered image. A **composite pose** holds other poses
;; and a layout; each sub-pose renders independently and the
;; composite tiles them together.
;;
;; This chapter walks through composition patterns from simple
;; side-by-side arrangements to shared-scale marginal plots, using
;; `pj/arrange` and explicit composite-pose maps -- compactly for
;; simple cases, with a bit of literal map construction for nested
;; layouts.

(ns plotje-book.composition
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## Side-by-Side via `pj/arrange`
;;
;; The simplest composite: two independent poses, placed next to
;; each other. `pj/arrange` takes a vector of poses and returns a
;; composite. Each sub-pose has its own data, mapping, layers, and
;; options. Coming from R, this is the same shape as patchwork's
;; `p1 | p2` operator or cowplot's `plot_grid(p1, p2)`.

(pj/arrange
 [(-> (rdatasets/datasets-iris) (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))])

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; Pass `{:cols 1}` for a stacked arrangement (one column means each
;; pose goes on its own row):

(pj/arrange
 [(-> (rdatasets/datasets-iris) (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))]
 {:cols 1})

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; `pj/arrange` divides space equally among its sub-poses. For
;; unequal splits (e.g., give the first panel twice the space of the
;; second), construct the composite as an explicit map; the next
;; section shows how.

;; ## Explicit Composite Poses
;;
;; Under `pj/arrange` there is a plain-map composite pose. You can
;; construct one directly when you need finer control -- unequal
;; weights, shared scales, or (in future work) non-plot leaves like
;; text panels and key performance indicators.
;;
;; An explicit `:layout` accepts `:direction` (`:horizontal` or
;; `:vertical`) and `:weights` (one weight per sub-pose). Here the
;; first panel gets twice the space of the second:

(def weighted
  (pj/pose
   {:layout {:direction :horizontal :weights [2 1]}
    :poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :petal-length :y :petal-width}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

;; `pj/pose` accepts a literal composite map and tags it for notebook
;; auto-render -- the same pose value the threaded API produces:

weighted

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; And printed, showing the composite's structure -- `:layout` with
;; direction and weights at the top, then each sub-pose with its
;; own `:mapping` and `:layers`, and the outer `:data` dataset:

(kind/pprint weighted)

(kind/test-last [(fn [pose] (and (= [2 1] (get-in pose [:layout :weights]))
                                 (= 2 (count (:poses pose)))))])

;; The outer `:data` is inherited by both sub-poses. Each sub-pose
;; has its own `:mapping` and `:layers`, and need not repeat the
;; dataset. Subsequent examples in this chapter follow the same
;; shape and show only the rendered plot.

;; ## Shared Scales
;;
;; By default, sibling poses in a composite compute their own
;; domains. That is fine when their columns differ, but for the same
;; column shown twice (e.g., a marginal above a scatter, or a mosaic
;; of scatters all measuring the same variable) you want the axes
;; aligned. `:share-scales` pins scales across siblings by effective
;; column:

(def shared-x
  (pj/pose
   {:share-scales #{:x}
    :layout {:direction :horizontal :weights [1 1]}
    :poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :sepal-length :y :petal-length}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

shared-x

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; Both panels share the sepal-length x-domain even though their y
;; columns differ. Column bucketing is automatic: only siblings whose
;; effective x-column matches share a scale. Panels with different
;; x-columns would each get their own domain.

;; ## A Marginal-Plot Pattern
;;
;; The classic "scatter with top density" -- a distribution strip
;; above the main plot -- is a vertical composite with shared x:

(def marginal
  (pj/pose
   {:share-scales #{:x}
    :layout {:direction :vertical :weights [1 3]}
    :poses [{:mapping {:x :sepal-length}
             :layers [{:layer-type :density}]}
            {:mapping {:x :sepal-length :y :sepal-width :color :species}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

marginal

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          panels (mapv #(-> % :plan :panels first)
                       (:sub-plots (pj/plan marginal)))
          [d-x s-x] (mapv :x-domain panels)
          [d-y s-y] (mapv :y-domain panels)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           (pos? (:polygons s))
           ;; Both panels share the same x-domain (sepal-length range).
           (= d-x s-x)
           ;; Each panel has its own y-domain (density vs sepal-width).
           (not= d-y s-y))))])

;; The top panel's density curve aligns with the scatter's x-axis.
;; Each panel retains its own y-axis because `:share-scales` here
;; contains only `:x`.

;; ## A Small Dashboard
;;
;; Composite poses can combine heterogeneous chart types. Here is a
;; dashboard-style 2x2 layout: a histogram of sepal length, a boxplot
;; of sepal width by species, a scatter of petal dimensions, and a
;; density of petal length.
;;
;; Each cell is built as its own leaf pose, then `pj/arrange` takes
;; rows of cells and produces the 2x2 grid:

(def dashboard
  (pj/arrange
   [[(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))
     (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width {:color :species}))]
    [(-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))
     (-> (rdatasets/datasets-iris) (pj/lay-density :petal-length {:color :species}))]]))

dashboard

(kind/test-last
 [(fn [v]
    (let [chrome (-> dashboard pj/plan :chrome)]
      (and (= 4 (:panels (pj/svg-summary v)))
           ;; The histogram cell has no :color, so :color is not
           ;; shared across all leaves -- each colored cell renders
           ;; its own legend (see Notes below).
           (= #{} (:shared-aesthetics chrome)))))])

;; Four panels, each its own layer type. `pj/arrange` stacks the rows
;; vertically; each row is laid out horizontally. Every cell is a
;; leaf pose -- `pj/arrange` does not accept composite cells.

;; ## Drawing Layers with Different Data
;;
;; A layer can carry its own `:data` via the layer options map.
;; This is how reference lines, prediction overlays, and small
;; annotation datasets attach to a plot. The wrinkle is what the
;; layer's columns must refer to: a panel has one x-axis and one
;; y-axis, both identified by their column ref, so a layer that
;; renders on a panel uses the panel's column refs to look up
;; values in its data.
;;
;; That rule gives two patterns -- "overlay on the same panel"
;; and "this layer on a separate sub-pose" -- with different
;; mechanics. Knowing which one you want determines the call
;; shape.
;;
;; ### Overlay on the same panel
;;
;; There are two ways to draw the layer on the existing panel with
;; data from elsewhere. The first is to use the panel's column refs,
;; renaming the incoming dataset's columns to match via
;; `tc/rename-columns`; a layer whose columns match the panel's joins
;; it. Here a base scatter is overlaid with a second set of points
;; from another dataset:

(def overlay-base
  {:fitted   [1 2 3]
   :residual [1 2 3]})

(def overlay-other
  (tc/dataset {:x [0.5 1.5 2.5]
               :y [1.5 2.5 3.5]}))

(-> overlay-base
    (pj/lay-point :fitted :residual)
    (pj/lay-point :fitted :residual
                  {:data (tc/rename-columns overlay-other
                                            {:x :fitted :y :residual})}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 1 (:panels s))
           (= 6 (:points s)))))])

;; Both layers use the pose's `:fitted` and `:residual` column
;; refs; the second layer's data, renamed to those columns,
;; renders into the same panel. The result is one panel with
;; six points -- three from each layer. Overlaid layers paint in
;; the order they were added -- see
;; [Poses](./plotje_book.pose_model.html#layer-order-is-paint-order) -- so add the layer you
;; want on top last.
;;
;; The second way is to say outright that you meant an overlay, and
;; leave the incoming columns alone. `pj/overlay` marks the pose, and
;; every layer added after it joins the panel:

(-> overlay-base
    (pj/lay-point :fitted :residual)
    pj/overlay
    (pj/lay-point :x :y {:data overlay-other}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 1 (:panels s))
           (= 6 (:points s)))))])

;; Same picture, no renaming. Which to reach for depends on what the
;; incoming columns mean: rename when they are the same quantity under
;; another name, and use `pj/overlay` when they are a different
;; quantity you want read against the same axes.
;;
;; ### Separate sub-pose for the new layer
;;
;; To put the new layer on its own panel, name the layer's
;; columns directly. When the new layer's position does not match
;; the existing one, the pose splits into two panels: the original
;; leaf becomes panel-1, and a new sub-pose carrying the new
;; position and the new layer becomes panel-2. The two render side
;; by side in a grid (the default `:matrix` layout, which arranges
;; panels in rows and columns). This promotion is specified as Rule
;; LP2 in the [Pose Rules](./plotje_book.pose_rules.html#rule-lp2-position-carrying-lay--attaches-to-the-dfs-last-matching-leaf) chapter.

(-> overlay-base
    (pj/lay-point :fitted :residual)
    (pj/lay-point :x :y {:data overlay-other}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 2 (:panels s))
           (= 6 (:points s)))))])

;; Each panel has three points and its own x/y axis labels:
;; panel-1 shows `fitted` vs `residual`, panel-2 shows `x` vs
;; `y`. (For finer layout control -- different weights, shared
;; scales, or an explicit grid -- build the composite via
;; `pj/arrange` or the explicit composite-pose form shown
;; earlier in the chapter.)

;; ## Panels Clip Their Own Content
;;
;; Each panel confines its marks to its own rectangle. A layer whose
;; data runs past the panel's domain -- a reference line drawn well
;; beyond the visible range, say -- is clipped at the panel edge
;; rather than painting across its neighbours. The clip is applied per
;; panel, so a stacked arrangement reports one clip region per panel.

(def bounded
  (-> {:x [1 2 3 4 5] :y [10 20 15 25 18]}
      (pj/lay-point :x :y)
      (pj/lay-line {:data {:x [1 5] :y [-200 300]}})
      (pj/scale :y {:type :linear :domain [0 30]})))

(pj/arrange [bounded bounded] {:cols 1})

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 2 (:clips s)))))])

;; ## Notes on the Current Implementation
;;
;; A few details about how composition renders today, in case they
;; matter for a layout you're sketching:
;;
;; - **Each leaf draws its own axes, labels, and ticks.** Shared
;;   scales align the data ranges across panels, but these
;;   decorations are per-leaf in `:horizontal` and `:vertical`
;;   layouts, so the marginal example renders the x-axis label
;;   on both the strip and the scatter. (Matrix layouts -- the
;;   SPLOM grid in particular -- replace per-leaf x/y labels with
;;   shared strip labels, and SPLOM additionally suppresses ticks
;;   on interior cells.)
;; - **Plot-area edges may not line up** across composite siblings,
;;   since each leaf reserves its own padding for axes and labels --
;;   two sub-poses with different label lengths can produce visibly
;;   different panel widths.
;; - **Each cell has its own plot options.** A cell is a pose, so
;;   `pj/options`, `pj/scale` and `pj/coord` written on it apply to
;;   that cell: one cell can be log-scaled and its neighbour linear,
;;   and each can have its own title. Written on the composite itself,
;;   they apply to every cell that does not set them. This is also the
;;   way around a limitation within one pose -- two layers cannot read
;;   one aesthetic through different scales, but two cells can.
;; - **Legends merge when sibling sub-poses agree on an aesthetic.**
;;   If every leaf maps the same aesthetic identically (e.g.,
;;   `:color :species` in all cells), the compositor renders a
;;   single shared legend at composite level. When the mappings
;;   disagree -- or when only some leaves carry the aesthetic, as
;;   in the dashboard above -- each leaf with that aesthetic
;;   renders its own legend.
;; - **Multi-row layouts go through `pj/arrange`.** Both `pj/arrange`
;;   and the explicit-map form accept only leaf cells; a row of rows
;;   or column of rows is built by passing nested vectors of leaves
;;   to `pj/arrange` (the dashboard example above shows the shape).
;;   Nested composites (a sub-pose that is itself composite) are out
;;   of scope today.

;; The legend note is the one to see rather than take on trust. Both
;; cells below map `:color` to the same column, so the two legends
;; collapse into a single one drawn for the composite as a whole:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width {:color :species}))])

(kind/test-last
 [(fn [v]
    (and (= #{:color} (-> v pj/plan :chrome :shared-aesthetics))
         ;; The dashboard above pairs a histogram with a colored
         ;; scatter, so its siblings do not agree and nothing merges.
         (= #{} (-> (pj/arrange
                     [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))
                      (-> (rdatasets/datasets-iris)
                          (pj/lay-point :petal-length :petal-width {:color :species}))])
                    pj/plan :chrome :shared-aesthetics))))])

;; ## What's Next
;;
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) --
;;   the taxonomy of layer options, plot options, and configuration
;;   (next chapter in Foundations)
;; - [**Faceting**](./plotje_book.faceting.html) -- panel splits by
;;   a categorical column (a data-driven composite, covered in How-to)
;; - [**Gallery**](./plotje_book.gallery.html) -- more composition
;;   examples alongside single-plot chart types
