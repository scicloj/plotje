(ns scicloj.plotje.render.composite
  "Composite plan -> membrane rendering. The CompositePlan dispatch of
   the plan->membrane multimethod, plus the membrane drawables for
   composite chrome (title, strip labels, shared legend).

   Pure data-side composite logic -- chrome geometry, layout
   computation, composite-pose->draft, composite-draft->plan -- stays
   in `impl/compositor.clj`. This namespace only handles the rendering
   side and depends on membrane, keeping `impl/` free of membrane
   dependencies."
  (:require [membrane.ui :as ui]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.membrane :as mem]
            [scicloj.plotje.render.membrane :as membrane]))

;; ---- Drawing constants ----

(def ^:private composite-text-color
  "Text color for composite-level chrome (title + strip labels).
   Matches the leaf title color in render/membrane.clj so single
   plots and composite plots use the same shade -- earlier code
   used [0.1 0.1 0.1] which rendered as rgb(25,25,25), visibly
   darker than leaf titles' rgb(51,51,51)."
  [0.2 0.2 0.2 1.0])

;; ---- Chrome drawables ----

(defn- title-drawable
  "Membrane drawable for a centered title band at the top of a
   composite of width w. Nil when no title.

   The size comes from `:title-font-size`, as a leaf title's does in
   render/membrane.clj. It was the literal 15 that key defaults to, so
   a composite title stayed at 15 however the option was set."
  [title w cfg]
  (when title
    (ui/translate (/ (double w) 2.0) 16
                  (ui/with-color composite-text-color
                    (assoc (ui/label title (ui/font nil (:title-font-size cfg)))
                           :text-anchor "middle")))))

(defn- matrix-col-strip-drawables
  "Like col-strip-drawables but for `:direction :matrix` composites,
   where leaves are at flat paths `[i]` and the (col, row) position
   comes from pose/matrix-axes. Places one centered label above
   each column at strip-top, computing the column center directly
   from the grid rect rather than looking it up via a SPLOM path."
  [col-labels [grid-x _ grid-w _] n-cols strip-top cfg]
  (vec
   (for [ci (range n-cols)
         :let [label (nth col-labels ci nil)]
         :when label]
     (let [cw (/ (double grid-w) n-cols)
           cx (+ (double grid-x) (* ci cw) (/ cw 2.0))]
       (ui/translate cx (double strip-top)
                     (ui/with-color composite-text-color
                       (assoc (ui/label label (ui/font nil (:strip-font-size cfg)))
                              :text-anchor "middle")))))))

(defn- matrix-row-strip-drawables
  "Like row-strip-drawables but for `:direction :matrix` composites.
   Places one centered label to the left of each row, computing the
   row center directly from the grid rect."
  [row-labels [_ grid-y _ grid-h] n-rows strip-left strip-right cfg]
  (let [label-x (+ (double strip-left)
                   (/ (- (double strip-right) (double strip-left)) 2.0))]
    (vec
     (for [ri (range n-rows)
           :let [label (nth row-labels ri nil)]
           :when label]
       (let [rh (/ (double grid-h) n-rows)
             cy (+ (double grid-y) (* ri rh) (/ rh 2.0))]
         (ui/translate label-x cy
                       (ui/with-color composite-text-color
                         (assoc (ui/label label (ui/font nil (:strip-font-size cfg)))
                                :text-anchor "middle"))))))))

(defn- col-strip-drawables
  "Build a vector of membrane drawables: one centered text per column
   label, positioned above its column's top-row rect."
  [col-labels layout n-cols strip-top cfg]
  (vec
   (for [ci (range n-cols)
         :let [label (nth col-labels ci nil)
               rect  (get layout [0 ci])]
         :when (and label rect)]
     (let [[x _ w _] rect
           cx (+ (double x) (/ (double w) 2.0))]
       (ui/translate cx (double strip-top)
                     (ui/with-color composite-text-color
                       (assoc (ui/label label (ui/font nil (:strip-font-size cfg)))
                              :text-anchor "middle")))))))

(defn- row-strip-drawables
  "Build a vector of membrane drawables: one text per row label,
   positioned to the left of its row's leftmost rect."
  [row-labels layout n-rows strip-left strip-right cfg]
  (let [label-x (+ (double strip-left)
                   (/ (- (double strip-right) (double strip-left)) 2.0))]
    (vec
     (for [ri (range n-rows)
           :let [label (nth row-labels ri nil)
                 rect  (get layout [ri 0])]
           :when (and label rect)]
       (let [[_ y _ h] rect
             cy (+ (double y) (/ (double h) 2.0))]
         (ui/translate label-x cy
                       (ui/with-color composite-text-color
                         (assoc (ui/label label (ui/font nil (:strip-font-size cfg)))
                                :text-anchor "middle"))))))))

(defn- shared-legend-drawables
  "Build membrane drawables for the shared legend positioned at
   (legend-x, legend-y-top). Takes the representative plan's legend
   spec and renders color / size / alpha / shape legends stacked
   vertically. Returns a vector of drawables; empty when the rep plan
   has no legend data."
  [rep-plan legend-x legend-y-top cfg]
  (let [{:keys [legend size-legend alpha-legend shape-legend]} rep-plan
        ;; Each channel has a rung on a fixed ladder. The shape legend
        ;; takes the first rung the other three leave free, so a plot
        ;; whose only legend is a shape legend draws it at the top
        ;; rather than 400 pixels below the canvas.
        shape-y   (+ legend-y-top (cond alpha-legend 408
                                        size-legend  288
                                        legend       168
                                        :else        18))
        sections  (keep (fn [[drawer data]]
                          (when data (drawer data)))
                        [[(fn [l] (membrane/render-legend-from-plan
                                   l legend-x (+ legend-y-top 18) cfg))
                          legend]
                         [(fn [l] (membrane/render-size-legend
                                   l legend-x (+ legend-y-top 168) cfg))
                          size-legend]
                         [(fn [l] (membrane/render-alpha-legend
                                   l legend-x (+ legend-y-top 288) cfg))
                          alpha-legend]
                         [(fn [l] (membrane/render-shape-legend
                                   l legend-x shape-y cfg))
                          shape-legend]])]
    (vec (apply concat sections))))

;; ---- plan->membrane dispatch for composites ----
;;
;; The CompositePlan defmethod recurses: it calls plan->membrane on
;; each sub-plot's leaf plan (dispatching to the LeafPlan method),
;; translates each result into the composite coordinate space, and
;; layers chrome drawables (title, strip labels, shared legend) on
;; top.

(defmethod membrane/plan->membrane true
  [composite-plan opts]
  (let [;; The chrome resolves the same configuration the cells do. It
        ;; used to build its own from `{}`, so a shared legend ignored
        ;; every plot option -- :thousands-separator among them, leaving
        ;; a legend reading 100000 beside axes reading 100,000.
        cfg (defaults/resolve-config opts)
        {:keys [width height sub-plots chrome]} composite-plan
        {:keys [title title-band-h grid-rect strip-h strip-w
                col-labels row-labels n-cols n-rows matrix?
                shared-legend layout]} chrome
        strips? (boolean (or (seq col-labels) (seq row-labels)))
        ;; The composite's own options reach each cell. Passing `{}` here
        ;; left every panel of a composite resolving its configuration
        ;; from the global chain alone, so a plot option that moved a
        ;; leaf -- :thousands-separator, :label-font-size, the grid and
        ;; annotation strokes -- did nothing once the same pose was
        ;; arranged. Tooltip stays per cell: it is decided on each
        ;; sub-plot's own plan, not on the composite.
        leaf-trees (mapv (fn [{:keys [plan rect]}]
                           (let [tree (membrane/plan->membrane
                                       plan
                                       (assoc opts :tooltip (boolean (:tooltip plan))))
                                 [x y _ _] rect]
                             (ui/translate (double x) (double y) tree)))
                         sub-plots)
        col-strips (when (and strips? (seq col-labels))
                     (if matrix?
                       (matrix-col-strip-drawables col-labels grid-rect n-cols
                                                   (+ title-band-h 2) cfg)
                       (col-strip-drawables col-labels layout n-cols
                                            (+ title-band-h 2) cfg)))
        row-strips (when (and strips? (seq row-labels))
                     (if matrix?
                       (matrix-row-strip-drawables row-labels grid-rect n-rows
                                                   0 strip-w cfg)
                       (row-strip-drawables row-labels layout n-rows
                                            0 strip-w cfg)))
        [_ _ grid-w _] grid-rect
        legend-tree (when shared-legend
                      (shared-legend-drawables
                       shared-legend
                       (+ (double strip-w) (double grid-w) 20)
                       (double (+ title-band-h strip-h))
                       cfg))
        composed (cond-> leaf-trees
                   (seq col-strips) (into col-strips)
                   (seq row-strips) (into row-strips)
                   (seq legend-tree) (into legend-tree)
                   title             (conj (title-drawable title width cfg)))]
    (cond-> (mem/->PlotjeMembrane (vec composed) (long width) (long height))
      title (assoc :plotje/title title))))
