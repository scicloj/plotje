;; # Composition
;;
;; Plotje's poses let you combine whole plots into a
;; single rendered image. A **composite pose** holds other poses
;; and a layout; each sub-pose renders independently and the
;; composite tiles them together.
;;
;; This chapter walks through composition patterns from simple
;; side-by-side arrangements to shared-scale marginal plots, using
;; `pj/arrange`, `pj/marginal` and explicit composite-pose maps --
;; compactly for simple cases, with a bit of literal map construction
;; for nested layouts.

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

;; A date column pools the same way -- an axis holds a date as a number
;; of milliseconds, so the union across cells is defined, and the cells
;; are ticked over it rather than each over its own dates. See
;; [Change Over Time](./plotje_book.change_over_time.html#two-series-on-one-date-axis).
;;
;; Both panels share the sepal-length x-domain even though their y
;; columns differ. Column bucketing is automatic: only siblings whose
;; effective x-column matches share a scale. Panels with different
;; x-columns would each get their own domain.

;; ## Marginal Plots
;;
;; The classic "scatter with top density" -- a distribution strip beside
;; the main plot -- has a function of its own. `pj/marginal` takes a leaf
;; pose and puts a distribution of one of its columns in a thin panel
;; against one edge: the `:x` column above the panel, the `:y` column to
;; its right.

(def marginal
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/marginal :top)))

marginal

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plans (mapv :plan (:sub-plots (pj/plan marginal)))
          panels (mapv #(-> % :panels first) plans)
          [d-x s-x] (mapv :x-domain panels)
          [d-y s-y] (mapv :y-domain panels)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           (pos? (:polygons s))
           ;; Both panels share the same x-domain (sepal-length range).
           (= d-x s-x)
           ;; Each panel has its own y-domain (density vs sepal-width).
           (not= d-y s-y)
           ;; The strip carries neither x ticks nor an x title.
           (= [] (:values (:x-ticks (first panels))))
           (nil? (:x-label (first plans)))
           ;; Both cells reserve the same room on the left for the
           ;; y title and on the right for the legend, so the two
           ;; drawing areas line up. `==` rather than `=`: the cell
           ;; that computed the pad carries a double and the cell
           ;; given it as a floor carries whatever it was written as.
           (apply == (map #(get-in % [:layout :y-label-pad]) plans))
           (apply == (map #(get-in % [:layout :legend-w]) plans)))))])

;; The strip shares the scatter's x axis and keeps its own value scale.
;; Its duplicate x ticks and axis title are dropped, since the axis below
;; describes both panels, and the two drawing areas are aligned so a value
;; sits at the same place in each -- including here, where the scatter
;; carries a legend and the strip does not.
;;
;; The color mapping does not reach the strip. A marginal describes the
;; pose's `:x` column as a whole, so one grey curve is drawn over the
;; three colored groups below it.
;;
;; `:histogram` draws the distribution as bars instead of a curve, and
;; `:size` sets the strip's share of the height (`0.25` by default):

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/marginal :top :histogram {:size 0.35}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 150 (:points s)))))])

;; ### A marginal on the right
;;
;; `:right` describes the pose's `:y` column instead, in a strip beside
;; the panel. The distribution is drawn under `pj/coord :flip`, so its
;; value axis runs across the strip and its baseline stands at the left
;; edge, against the scatter:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/marginal :right))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plans (mapv :plan (:sub-plots (pj/plan v)))
          panels (mapv #(-> % :panels first) plans)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           ;; The scatter comes first here, the strip second -- a
           ;; right marginal reads left to right.
           (= (:y-domain (first panels)) (:y-domain (second panels)))
           ;; The strip carries neither y ticks nor a y title.
           (= [] (:values (:y-ticks (second panels))))
           (nil? (:y-label (second plans)))
           ;; A row of cells lines up on its top and bottom edges, so
           ;; the room reserved below each is what has to agree.
           (apply == (map #(get-in % [:layout :x-label-pad]) plans)))))])

;; A `:top` marginal and a `:right` one line up on different edges. Two
;; panels in a column share their left and right edges, so what has to
;; agree is the room reserved for the y title and for the legend; two
;; panels in a row share their top and bottom edges, so what has to
;; agree is the room below them. `:align-panels` reserves whichever the
;; layout calls for.
;;
;; `:bottom` and `:left` are not drawn: a strip there would sit between
;; the panel and the axis that describes it.

;; ### What a marginal is made of
;;
;; A `:top` marginal is a vertical composite with a shared x, which can
;; also be written out. Four things make it up: the weights, the shared
;; scale, the ticks and axis title suppressed on the strip, and
;; `:align-panels`. Here are the first three:

(def marginal-by-hand
  (pj/pose
   {:share-scales #{:x}
    :layout {:direction :vertical :weights [1 3]}
    :poses [{:mapping {:x :sepal-length}
             :opts {:suppress-x-ticks true :suppress-x-label true}
             :layers [{:layer-type :density}]}
            {:mapping {:x :sepal-length :y :sepal-width :color :species}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

marginal-by-hand

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plans (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
          panels (mapv #(-> % :panels first) plans)
          [d-x s-x] (mapv :x-domain panels)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           (= d-x s-x)
           ;; The strip reserves no legend column and the scatter
           ;; reserves 102 drawing units for one, so the two drawing
           ;; areas are different widths.
           (= [0 102] (mapv #(get-in % [:layout :legend-w]) plans)))))])

;; So the two drawing areas do not line up: the strip draws no legend,
;; reserves no column for one, and comes out wider than the scatter
;; below it. The density then sits at a different scale from the points
;; it describes.
;;
;; That is the fourth part, and the one `pj/marginal` adds:
;; `:align-panels` in the composite's own `:opts`. It plans every cell
;; twice, the second time with the widest pad any of them needed as a
;; floor, so each cell reserves what the others do -- the y-label pad
;; and the legend column for a column of cells, the room below for a
;; row of them. `pj/options` does not accept the key -- it is a
;; composite's internal setting rather than a plot option -- so a
;; written-out composite carries it in the map:

(assoc-in marginal-by-hand [:opts :align-panels] true)

(kind/test-last
 [(fn [v]
    (let [plans (mapv :plan (:sub-plots (pj/plan v)))]
      (and (= 2 (:panels (pj/svg-summary v)))
           ;; `==` rather than `=`: the cell that computed the value
           ;; carries a double, the cell given it as a floor carries
           ;; whatever it was written as.
           (apply == (map #(get-in % [:layout :y-label-pad]) plans))
           (apply == (map #(get-in % [:layout :legend-w]) plans)))))])

;; With the key in place the written-out form is the composite
;; `pj/marginal` returns, up to the weights it computes from `:size`.
;; Nothing in the shape is specific to distributions, so the same four
;; parts serve any stack of panels read against one x axis.

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
    (pj/lay-point :fitted :residual {:color "#377eb8"})
    (pj/lay-point :fitted :residual
                  {:color "#e6550d"
                   :data (tc/rename-columns overlay-other
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
    (pj/lay-point :fitted :residual {:color "#377eb8"})
    pj/overlay
    (pj/lay-point :x :y {:color "#e6550d" :data overlay-other}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          renamed (-> overlay-base
                      (pj/lay-point :fitted :residual {:color "#377eb8"})
                      (pj/lay-point :fitted :residual
                                    {:color "#e6550d"
                                     :data (tc/rename-columns
                                            overlay-other
                                            {:x :fitted :y :residual})}))]
      (and (= 1 (:panels s))
           (= 6 (:points s))
           ;; "Same picture" taken literally: the renamed form above
           ;; draws the same SVG, which the written colours make
           ;; checkable rather than merely plausible.
           (= (pj/plot renamed) (pj/plot v)))))])

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
;; position and the new layer becomes panel-2. The default `:matrix`
;; layout places a panel by its columns -- `:x` picks the column of
;; the grid, `:y` picks the row -- so two panels naming four different
;; columns sit on the diagonal of a two-by-two grid and the other two
;; cells stay empty. This promotion is specified as Rule
;; LP2 in the [Pose Rules](./plotje_book.pose_rules.html#rule-lp2-position-carrying-lay--attaches-to-the-dfs-last-matching-leaf) chapter.

(-> overlay-base
    (pj/lay-point :fitted :residual {:color "#377eb8"})
    (pj/lay-point :x :y {:color "#e6550d" :data overlay-other}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 2 (:panels s))
           (= 6 (:points s))
           ;; The two-by-two grid the prose describes: two distinct
           ;; x columns give two grid columns, two distinct y columns
           ;; give two rows, and only two of the four cells are filled.
           (= [2 2] ((juxt :n-rows :n-cols) (:chrome (pj/plan v))))
           ;; The same two colours as the two examples above, so the
           ;; three outcomes can be compared mark for mark.
           (= #{"rgb(55,126,184)" "rgb(230,85,13)"}
              (disj (:colors s) "none")))))])

;; Each panel has three points and its own x/y axis labels:
;; panel-1 shows `fitted` vs `residual`, panel-2 shows `x` vs
;; `y`. To put the two panels in a row instead of on a diagonal --
;; or to set weights, share scales, or give an explicit grid --
;; build the composite via `pj/arrange` or the explicit
;; composite-pose form shown earlier in the chapter.

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
;;   layouts, so a stack of cells reading one x column carries that
;;   axis label once per cell. A cell drops its own with
;;   `:suppress-x-ticks` and `:suppress-x-label` in that cell's
;;   `:opts`, which is what `pj/marginal` writes on the strip it
;;   builds. (Matrix layouts -- the SPLOM grid in particular --
;;   replace per-leaf x/y labels with shared strip labels, and
;;   SPLOM additionally suppresses ticks on interior cells.)
;; - **Plot-area edges may not line up** across composite siblings,
;;   since each leaf reserves its own padding for axes and labels --
;;   two sub-poses with different label lengths, or one drawing a
;;   legend that its neighbour does not, produce visibly different
;;   panel widths. `pj/marginal` is the one construction that
;;   answers this today: it plans its two cells against a common
;;   floor for both the y-label pad and the legend column.
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
