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
  ([title w cfg] (title-drawable title (/ (double w) 2.0) 0.0 cfg))
  ([title cx top cfg]
   (when title
     (ui/translate (double cx) (+ (double top) 16)
                   (ui/with-color composite-text-color
                     (assoc (ui/label title (ui/font nil (:title-font-size cfg)))
                            :text-anchor "middle"))))))

(defn- subtitle-drawable
  "Membrane drawable for a subtitle centred at `cx`, in the band whose
   top is `top`: under the title, as a leaf's subtitle is, two points
   smaller."
  [subtitle title? cx top cfg]
  (ui/translate (double cx)
                (+ (double top) (if title? 32 6))
                (ui/with-color composite-text-color
                  (assoc (ui/label subtitle
                                   (ui/font nil (- (:title-font-size cfg) 2)))
                         :text-anchor "middle"))))

(defn- caption-drawable
  "Membrane drawable for a caption ending at `right`, its line placed at
   `top`: small and grey, at the right, as a leaf's caption is."
  [caption right top]
  (ui/translate (double right) (double top)
                (ui/with-color [0.5 0.5 0.5 1.0]
                  (assoc (ui/label caption (ui/font nil 9))
                         :text-anchor "end"))))

(defn- strip-label-drawable
  "Membrane drawable for one strip label centred at (`cx`, `top`)."
  [label cx top cfg]
  (ui/translate (double cx) (double top)
                (ui/with-color composite-text-color
                  (assoc (ui/label label (ui/font nil (:strip-font-size cfg)))
                         :text-anchor "middle"))))

(declare row-strip-label)

(defn- x-axis-title-drawable
  "The `:x` title a grid draws once, centred at `cx` under its cells,
   whose bottom is `bottom`."
  [title cx bottom cfg]
  (ui/translate (double cx) (+ (double bottom) 4)
                (ui/with-color composite-text-color
                  (assoc (ui/label title (ui/font nil (:label-font-size cfg)))
                         :text-anchor "middle"))))

(defn- y-axis-title-drawable
  "The `:y` title a grid draws once, turned upright, centred at `cy`
   beside its cells, whose left edge is `left`."
  [title left cy cfg]
  (ui/translate (- (double left) 18) (double cy)
                (membrane.ui.Rotate. -90
                                     (ui/with-color composite-text-color
                                       (assoc (ui/label title (ui/font nil (:label-font-size cfg)))
                                              :text-anchor "middle")))))

(defn- block-strip-drawables
  "The strips of a nested composite, drawn beside its block as a root
   composite's are beside its grid: a label over each column of cells,
   centred on the column's panel where its plan is known, in the band
   above the cells, and a label beside each row of cells in the band at
   their right, as a facet's row strips stand."
  [{:keys [col-labels row-labels cells strip-h]} plan-of cfg]
  (concat
   (for [[ci label] (map-indexed vector col-labels)
         :let [{:keys [path rect]} (nth (first cells) ci nil)]
         :when (and label rect)]
     (let [[x y w _] rect
           p (get plan-of path)
           pad (get-in p [:layout :y-label-pad])
           pw (:panel-width p)
           cx (if (and pad pw)
                (+ (double x) (double pad) (/ (double pw) 2.0))
                (+ (double x) (/ (double w) 2.0)))]
       (strip-label-drawable label cx (+ (- (double y) (double strip-h)) 2) cfg)))
   (for [[ri label] (map-indexed vector row-labels)
         :let [{:keys [rect]} (last (nth cells ri nil))]
         :when (and label rect)]
     (let [[x y w h] rect]
       (row-strip-label label (+ (double x) (double w)) (+ (double y) (/ (double h) 2.0)) cfg)))))

(defn- block-title-drawables
  "The title and subtitle of each nested composite, centred over its
   block in the band reserved above it; its strips beside the block;
   and its caption at the right of the band reserved below it
   (`compositor/block-titles`)."
  [block-titles plan-of cfg]
  (vec
   (mapcat (fn [{:keys [title subtitle caption caption-rect cells axis-titles
                        x-title-rect y-title-rect] [x y w _] :rect :as bt}]
             (let [cx (+ (double x) (/ (double w) 2.0))]
               (cond-> []
                 (and (:x axis-titles) x-title-rect)
                 (conj (let [[tx ty tw _] x-title-rect]
                         (x-axis-title-drawable (:x axis-titles) (+ (double tx) (/ (double tw) 2.0)) ty cfg)))
                 (and (:y axis-titles) y-title-rect)
                 (conj (let [[tx ty tw th] y-title-rect]
                         (y-axis-title-drawable (:y axis-titles) (+ (double tx) (double tw))
                                                (+ (double ty) (/ (double th) 2.0)) cfg)))
                 title (conj (title-drawable title cx y cfg))
                 subtitle (conj (subtitle-drawable subtitle (some? title) cx y cfg))
                 cells (into (block-strip-drawables bt plan-of cfg))
                 (and caption caption-rect)
                 (conj (let [[cx0 cy0 cw ch] caption-rect]
                         (caption-drawable caption (- (+ (double cx0) (double cw)) 10)
                                           (- (+ (double cy0) (double ch)) 13)))))))
           block-titles)))

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

(defn- row-strip-label
  "Membrane drawable for a row strip label beginning just right of `x`,
   centred on `cy`, as a facet's row labels stand beside its panels."
  [label x cy cfg]
  (ui/translate (+ (double x) 6) (- (double cy) 5)
                (ui/with-color composite-text-color
                  (ui/label label (ui/font nil (:strip-font-size cfg))))))

(defn- matrix-row-strip-drawables
  "Like row-strip-drawables but for `:direction :matrix` composites.
   Places one label at the right of each row, computing the row center
   directly from the grid rect."
  [row-labels [_ grid-y _ grid-h] n-rows strip-left _strip-right cfg]
  (vec
   (for [ri (range n-rows)
         :let [label (nth row-labels ri nil)]
         :when label]
     (let [rh (/ (double grid-h) n-rows)
           cy (+ (double grid-y) (* ri rh) (/ rh 2.0))]
       (row-strip-label label strip-left cy cfg)))))

(defn- col-strip-drawables
  "Build a vector of membrane drawables: one centered text per column
   label, positioned above its column's top-row cell. Centred on the
   cell's panel where the cell's plan is known -- the cell's rectangle
   also holds the room its y labels take, and centring on that put the
   label left of the panel under it."
  ([col-labels layout n-cols strip-top cfg]
   (col-strip-drawables col-labels layout n-cols strip-top cfg {}))
  ([col-labels layout n-cols strip-top cfg plan-of]
   (vec
    (for [ci (range n-cols)
          :let [label (nth col-labels ci nil)
                ;; Rows of cells, as `pj/arrange` builds them, or one
                ;; flat row of cells.
                path  (if (get layout [0 ci]) [0 ci] [ci])
                rect  (get layout path)]
          :when (and label rect)]
      (let [[x _ w _] rect
            p (get plan-of path)
            pad (get-in p [:layout :y-label-pad])
            pw (:panel-width p)
            cx (if (and pad pw)
                 (+ (double x) (double pad) (/ (double pw) 2.0))
                 (+ (double x) (/ (double w) 2.0)))]
        (ui/translate cx (double strip-top)
                      (ui/with-color composite-text-color
                        (assoc (ui/label label (ui/font nil (:strip-font-size cfg)))
                               :text-anchor "middle"))))))))

(defn- row-strip-drawables
  "Build a vector of membrane drawables: one text per row label, in the
   strip band at the right of the grid, level with its row's first
   rect."
  [row-labels layout n-rows strip-left _strip-right cfg]
  (vec
   (for [ri (range n-rows)
         :let [label (nth row-labels ri nil)
               rect  (or (get layout [ri 0]) (get layout [ri]))]
         :when (and label rect)]
     (let [[_ y _ h] rect]
       (row-strip-label label strip-left (+ (double y) (/ (double h) 2.0)) cfg)))))

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
        {:keys [title subtitle caption title-band-h grid-rect strip-h strip-w
                col-labels row-labels n-cols n-rows matrix?
                shared-legend layout axis-titles block-titles]} chrome
        strips? (boolean (or (seq col-labels) (seq row-labels)))
        ;; The composite's own options reach each cell. Passing `{}` here
        ;; left every panel of a composite resolving its configuration
        ;; from the global chain alone, so a plot option that moved a
        ;; leaf -- :thousands-separator, :label-font-size, the grid
        ;; stroke and the rule colour -- did nothing once the same pose was
        ;; arranged. Tooltip stays per cell: it is decided on each
        ;; sub-plot's own plan, not on the composite.
        ;; The cell's own options win over the composite's, so a
        ;; `:theme` set on one cell reaches it. Without that merge a
        ;; per-cell render-stage option was accepted by `pj/options`
        ;; and then had no effect once the cell was arranged.
        leaf-trees (mapv (fn [{:keys [plan rect] cell-opts :opts}]
                           (let [tree (membrane/plan->membrane
                                       plan
                                       (-> (merge opts cell-opts)
                                           (assoc :tooltip (boolean (:tooltip plan)))))
                                 [x y _ _] rect]
                             (ui/translate (double x) (double y) tree)))
                         sub-plots)
        col-strips (when (and strips? (seq col-labels))
                     (if matrix?
                       (matrix-col-strip-drawables col-labels grid-rect n-cols
                                                   (+ title-band-h 2) cfg)
                       (col-strip-drawables col-labels layout n-cols
                                            (+ title-band-h 2) cfg
                                            (into {} (map (juxt :path :plan)) sub-plots))))
        ;; Row strips stand at the right of the grid, as a facet's do.
        strip-left (let [[gx _ gw _] grid-rect] (+ (double gx) (double gw)))
        row-strips (when (and strips? (seq row-labels))
                     (if matrix?
                       (matrix-row-strip-drawables row-labels grid-rect n-rows
                                                   strip-left (+ strip-left strip-w) cfg)
                       (row-strip-drawables row-labels layout n-rows
                                            strip-left (+ strip-left strip-w) cfg)))
        [_ _ grid-w _] grid-rect
        legend-tree (when shared-legend
                      (shared-legend-drawables
                       shared-legend
                       (+ (double strip-w) (double grid-w) 20)
                       (double (+ title-band-h strip-h))
                       cfg))
        ;; Axis titles drawn once beside the grid, as a facet's are.
        [gx gy gw gh] grid-rect
        title-trees (cond-> []
                      (:x axis-titles)
                      (conj (x-axis-title-drawable (:x axis-titles) (+ (double gx) (/ (double gw) 2.0))
                                                   (+ (double gy) (double gh)) cfg))
                      (:y axis-titles)
                      (conj (y-axis-title-drawable (:y axis-titles) gx
                                                   (+ (double gy) (/ (double gh) 2.0)) cfg)))
        ;; Under the title, as a leaf's subtitle is: centred, two
        ;; points smaller. The caption sits under the grid, at the right.
        title-trees (cond-> title-trees
                      subtitle
                      (conj (subtitle-drawable subtitle (some? title) (/ (double width) 2.0) 0.0 cfg))
                      caption
                      (conj (caption-drawable caption (- (double width) 10) (- (double height) 13))))
        composed (cond-> leaf-trees
                   (seq title-trees) (into title-trees)
                   (seq col-strips) (into col-strips)
                   (seq row-strips) (into row-strips)
                   (seq legend-tree) (into legend-tree)
                   (seq block-titles) (into (block-title-drawables block-titles
                                                                     (into {} (map (juxt :path :plan)) sub-plots)
                                                                     cfg))
                   title             (conj (title-drawable title width cfg)))]
    (cond-> (mem/->PlotjeMembrane (vec composed) (long width) (long height))
      title (assoc :plotje/title title))))
