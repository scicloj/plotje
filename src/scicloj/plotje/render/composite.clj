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

(def ^:private grid-strip-font-size
  "Font size for column/row strip labels on grid composites."
  11)

;; ---- Chrome drawables ----

(defn- title-drawable
  "Membrane drawable for a centered title band at the top of a
   composite of width w. Nil when no title."
  [title w]
  (when title
    (ui/translate (/ (double w) 2.0) 16
                  (ui/with-color composite-text-color
                    (assoc (ui/label title (ui/font nil 15))
                           :text-anchor "middle")))))

(defn- matrix-col-strip-drawables
  "Like col-strip-drawables but for `:direction :matrix` composites,
   where leaves are at flat paths `[i]` and the (col, row) position
   comes from pose/matrix-axes. Places one centered label above
   each column at strip-top, computing the column center directly
   from the grid rect rather than looking it up via a SPLOM path."
  [col-labels [grid-x _ grid-w _] n-cols strip-top]
  (vec
   (for [ci (range n-cols)
         :let [label (nth col-labels ci nil)]
         :when label]
     (let [cw (/ (double grid-w) n-cols)
           cx (+ (double grid-x) (* ci cw) (/ cw 2.0))]
       (ui/translate cx (double strip-top)
                     (ui/with-color composite-text-color
                       (assoc (ui/label label (ui/font nil grid-strip-font-size))
                              :text-anchor "middle")))))))

(defn- matrix-row-strip-drawables
  "Like row-strip-drawables but for `:direction :matrix` composites.
   Places one centered label to the left of each row, computing the
   row center directly from the grid rect."
  [row-labels [_ grid-y _ grid-h] n-rows strip-left strip-right]
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
                         (assoc (ui/label label (ui/font nil grid-strip-font-size))
                                :text-anchor "middle"))))))))

(defn- col-strip-drawables
  "Build a vector of membrane drawables: one centered text per column
   label, positioned above its column's top-row rect."
  [col-labels layout n-cols strip-top]
  (vec
   (for [ci (range n-cols)
         :let [label (nth col-labels ci nil)
               rect  (get layout [0 ci])]
         :when (and label rect)]
     (let [[x _ w _] rect
           cx (+ (double x) (/ (double w) 2.0))]
       (ui/translate cx (double strip-top)
                     (ui/with-color composite-text-color
                       (assoc (ui/label label (ui/font nil grid-strip-font-size))
                              :text-anchor "middle")))))))

(defn- row-strip-drawables
  "Build a vector of membrane drawables: one text per row label,
   positioned to the left of its row's leftmost rect."
  [row-labels layout n-rows strip-left strip-right]
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
                         (assoc (ui/label label (ui/font nil grid-strip-font-size))
                                :text-anchor "middle"))))))))

(defn- shared-legend-drawables
  "Build membrane drawables for the shared legend positioned at
   (legend-x, legend-y-top). Takes the representative plan's legend
   spec and renders color / size / alpha legends stacked vertically.
   Returns a vector of drawables; empty when the rep plan has no
   legend data."
  [rep-plan legend-x legend-y-top]
  (let [{:keys [legend size-legend alpha-legend]} rep-plan
        cfg       (defaults/resolve-config {})
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
                          alpha-legend]])]
    (vec (apply concat sections))))

;; ---- plan->membrane dispatch for composites ----
;;
;; The CompositePlan defmethod recurses: it calls plan->membrane on
;; each sub-plot's leaf plan (dispatching to the LeafPlan method),
;; translates each result into the composite coordinate space, and
;; layers chrome drawables (title, strip labels, shared legend) on
;; top.

(defmethod membrane/plan->membrane true
  [composite-plan _opts]
  (let [{:keys [width height sub-plots chrome]} composite-plan
        {:keys [title title-band-h grid-rect strip-h strip-w
                col-labels row-labels n-cols n-rows matrix?
                shared-legend layout]} chrome
        strips? (boolean (or (seq col-labels) (seq row-labels)))
        leaf-trees (mapv (fn [{:keys [plan rect]}]
                           (let [tooltip? (:tooltip plan)
                                 tree (if tooltip?
                                        (membrane/plan->membrane plan {:tooltip true})
                                        (membrane/plan->membrane plan {}))
                                 [x y _ _] rect]
                             (ui/translate (double x) (double y) tree)))
                         sub-plots)
        col-strips (when (and strips? (seq col-labels))
                     (if matrix?
                       (matrix-col-strip-drawables col-labels grid-rect n-cols
                                                   (+ title-band-h 2))
                       (col-strip-drawables col-labels layout n-cols
                                            (+ title-band-h 2))))
        row-strips (when (and strips? (seq row-labels))
                     (if matrix?
                       (matrix-row-strip-drawables row-labels grid-rect n-rows
                                                   0 strip-w)
                       (row-strip-drawables row-labels layout n-rows
                                            0 strip-w)))
        [_ _ grid-w _] grid-rect
        legend-tree (when shared-legend
                      (shared-legend-drawables
                       shared-legend
                       (+ (double strip-w) (double grid-w) 20)
                       (double (+ title-band-h strip-h))))
        composed (cond-> leaf-trees
                   (seq col-strips) (into col-strips)
                   (seq row-strips) (into row-strips)
                   (seq legend-tree) (into legend-tree)
                   title             (conj (title-drawable title width)))]
    (cond-> (mem/->PlotjeMembrane (vec composed) (long width) (long height))
      title (assoc :plotje/title title))))
