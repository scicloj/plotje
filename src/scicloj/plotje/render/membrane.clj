(ns scicloj.plotje.render.membrane
  "Build a `PlotjeMembrane` from a plan.
   Plan → membrane is format-agnostic: the resulting Membrane
   component can be rendered to SVG, PNG, or any other format a
   Membrane backend supports.

   The `PlotjeMembrane` record itself, plus the `membrane?` and
   `membrane-tree?` predicates, live in `scicloj.plotje.impl.membrane`
   and are re-exported here for callers already using this namespace."
  (:require [membrane.ui :as ui]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.membrane :as mem]
            [scicloj.plotje.render.panel :as panel]))

;; ---- Detection (re-exports from impl/membrane.clj) ----

(def membrane? mem/membrane?)
(def membrane-tree? mem/membrane-tree?)

;; ---- Legend ----

(defn render-legend-from-plan
  "Render legend from plan legend data as membrane drawables. Public
   so the compositor can reuse the renderer for composite-level
   shared legends (one legend per composite, not per leaf)."
  [legend x y cfg]
  (let [{:keys [title]} legend
        fsize 10
        title-color [0.2 0.2 0.2 1.0]
        sw defaults/legend-swatch-size
        sw-r (/ sw 2.0)]
    (if (= :continuous (:type legend))
      ;; Continuous gradient legend
      (let [{:keys [min max stops color-scale ticks]} legend
            ;; If render-time config overrides the color-scale, resolve fresh
            render-cs (:color-scale cfg)
            override? (and render-cs (not= render-cs color-scale))
            grad-fn (when override?
                      (defaults/resolve-gradient-fn render-cs))
            bar-h 120 bar-w 12
            n-stops (count stops)
            fmt-tick (fn [v]
                       (let [v (double v)]
                         (cond
                           (zero? v) "0"
                           (and (>= v 1.0) (== v (Math/floor v))) (str (long v))
                           :else (format "%.4g" v))))]
        (vec
         (concat
          (when title
            [(ui/translate x (- y 18)
                           (ui/with-color title-color
                             (ui/label (defaults/fmt-name title) (ui/font nil 11))))])
          ;; Gradient bar: stack of small colored rectangles
          (for [i (range n-stops)
                :let [{:keys [t color]} (nth stops i)
                      c (if override? (grad-fn t) color)
                      [cr cg cb _] c
                      ry (+ y (* (- 1.0 t) bar-h))]]
            (ui/translate x ry
                          (ui/with-color [cr cg cb 1.0]
                            (ui/with-style ::ui/style-fill
                              (ui/rectangle bar-w (/ bar-h n-stops))))))
          (if (seq ticks)
            ;; Log scale: label every tick value at its t-position.
            (for [{:keys [value t]} ticks
                  :let [ty (+ y (* (- 1.0 (double t)) bar-h) -4)]]
              (ui/translate (+ x bar-w 4) ty
                            (ui/with-color title-color
                              (ui/label (fmt-tick value) (ui/font nil 10)))))
            ;; Linear scale: just min/max at the ends.
            [(ui/translate (+ x bar-w 4) (+ y bar-h -4)
                           (ui/with-color title-color
                             (ui/label (format "%.4g" (double min)) (ui/font nil 10))))
             (ui/translate (+ x bar-w 4) (+ y 6)
                           (ui/with-color title-color
                             (ui/label (format "%.4g" (double max)) (ui/font nil 10))))]))))
      ;; Categorical swatch legend
      (let [{:keys [entries]} legend]
        (vec
         (concat
          (when title
            [(ui/translate x (- y 18)
                           (ui/with-color title-color
                             (ui/label (defaults/fmt-name title) (ui/font nil 11))))])
          (for [[i {:keys [label color]}] (map-indexed vector entries)
                :let [[cr cg cb _] color]]
            (ui/translate x (+ y (* i 16))
                          [(ui/translate 0 0
                                         (ui/with-color [cr cg cb 1.0]
                                           (ui/with-style ::ui/style-fill
                                             (ui/rounded-rectangle sw sw sw-r))))
                           (ui/translate 12 0
                                         (ui/with-color title-color
                                           (ui/label label (ui/font nil fsize))))]))))))))

(defn- render-legend-horizontal
  "Render a horizontal legend (for :top or :bottom positioning).
   Swatches and labels laid out left to right in a single row."
  [legend x y cfg]
  (let [{:keys [title entries]} legend
        fsize 10
        title-color [0.2 0.2 0.2 1.0]
        sw defaults/legend-swatch-size
        sw-r (/ sw 2.0)]
    (if (= :continuous (:type legend))
      ;; For continuous legends, fall back to vertical rendering
      (render-legend-from-plan legend x y cfg)
      ;; Horizontal categorical swatches
      (let [title-w (if title (* (count (defaults/fmt-name title)) 6) 0)
            start-x (if title (+ title-w 8) 0)]
        (vec
         (concat
          (when title
            [(ui/translate x (- y 1)
                           (ui/with-color title-color
                             (ui/label (defaults/fmt-name title) (ui/font nil 11))))])
          (let [{:keys [elems]}
                (reduce (fn [{:keys [elems cur-x]} {:keys [label color]}]
                          (let [[cr cg cb _] color
                                label-w (* (count label) 6)
                                elem [(ui/translate (+ x cur-x) (- y 1)
                                                    (ui/with-color [cr cg cb 1.0]
                                                      (ui/with-style ::ui/style-fill
                                                        (ui/rounded-rectangle sw sw sw-r))))
                                      (ui/translate (+ x cur-x 10) (- y 1)
                                                    (ui/with-color title-color
                                                      (ui/label label (ui/font nil fsize))))]]
                            {:elems (into elems elem)
                             :cur-x (+ cur-x 10 label-w 12)}))
                        {:elems [] :cur-x start-x}
                        entries)]
            elems)))))))

(defn render-size-legend
  "Render a size legend -- graduated circles with value labels.
   Returns a vector of membrane drawables."
  [size-legend x y]
  (let [{:keys [title entries]} size-legend
        title-color [0.2 0.2 0.2 1.0]
        point-color [0.4 0.4 0.4 1.0]
        max-r (reduce max (map :radius entries))
        row-h 18]
    (vec
     (concat
      (when title
        [(assoc (ui/translate x (- y 16)
                              (ui/with-color title-color
                                (ui/label (defaults/fmt-name title) (ui/font nil 11))))
                :legend true)])
      (for [[i {:keys [value radius]}] (map-indexed vector entries)
            :let [cy (+ y (* i row-h))
                  ;; Center circles horizontally on the max radius
                  cx (+ x max-r)]]
        (assoc
         (ui/translate 0 0
                       [(assoc (ui/translate (- cx radius) (- cy radius)
                                             (ui/with-color point-color
                                               (ui/with-style ::ui/style-fill
                                                 (ui/rounded-rectangle (* 2 radius) (* 2 radius) radius))))
                               :legend true)
                        (ui/translate (+ x (* 2 max-r) 6) cy
                                      (ui/with-color title-color
                                        (ui/label (str value) (ui/font nil 10))))])
         :legend true))))))

(defn render-alpha-legend
  "Render an alpha legend -- squares with varying opacity and value
   labels. Returns a vector of membrane drawables."
  [alpha-legend x y]
  (let [{:keys [title entries]} alpha-legend
        title-color [0.2 0.2 0.2 1.0]
        sw defaults/legend-swatch-size
        row-h 16]
    (vec
     (concat
      (when title
        [(assoc (ui/translate x (- y 16)
                              (ui/with-color title-color
                                (ui/label (defaults/fmt-name title) (ui/font nil 11))))
                :legend true)])
      (for [[i {:keys [value alpha]}] (map-indexed vector entries)
            :let [cy (+ y (* i row-h))]]
        (assoc
         (ui/translate 0 0
                       [(assoc (ui/translate x cy
                                             (ui/with-color [0.3 0.3 0.3 alpha]
                                               (ui/with-style ::ui/style-fill
                                                 (ui/rounded-rectangle sw sw 1))))
                               :legend true)
                        (ui/translate (+ x sw 6) cy
                                      (ui/with-color title-color
                                        (ui/label (str value) (ui/font nil 10))))])
         :legend true))))))

;; ---- Plan → Membrane ----

(defmulti plan->membrane
  "Build a membrane drawable tree from a plan.
   Returns a vector of membrane drawables representing the complete plot.

   Dispatches on `(boolean (:composite? plan))` -- two methods only,
   keyed `false` (leaf) and `true` (composite). The
   `pipeline-composition-test` namespace asserts this invariant at
   CI time.

   ## DO NOT change this dispatch to class-based.

   An earlier version dispatched on the defrecord class
   (`(fn [plan _] (class plan))`). Reloading `impl/resolve.clj`
   (which defines `Plan`/`CompositePlan`) replaced those classes,
   but the multimethod retained the OLD class objects forever,
   pinning their classloaders and causing persistent metaspace
   growth across edit sessions (~70 sibling classes per record
   reload). Fixed in commit 6a9ac87 (2026-04-28) by switching to
   boolean dispatch, which has no class identity and so cannot leak
   across reloads.

   If you need a third dispatch case, add another boolean-derived
   key (e.g. on a property of the plan), not a class.

   2-arity takes an opts map. Recognized keys:
     :tooltip -- when truthy, enables tooltip text generation on data marks."
  (fn [plan & _] (boolean (:composite? plan))))

(defmethod plan->membrane false
  [plan opts]
  (let [{:keys [tooltip]} opts
        cfg (defaults/resolve-config opts)
        {:keys [margin total-width total-height panel-width panel-height
                title subtitle caption x-label y-label
                legend size-legend alpha-legend
                legend-position panels layout grid]} plan
        {:keys [x-label-pad y-label-pad title-pad subtitle-pad caption-pad legend-w legend-h]
         :or   {x-label-pad 0 y-label-pad 0 title-pad 0 subtitle-pad 0
                caption-pad 0 legend-w 0 legend-h 0}} layout
        ;; strip-h / strip-w are nil on composites without strip labels
        ;; (plain multi-panel non-facet grids); default to 0 so arithmetic
        ;; and `pos?` checks below don't NPE.
        strip-h (or (:strip-h layout) 0)
        strip-w (or (:strip-w layout) 0)
        theme (:theme cfg)
        legend-pos (or legend-position :right)
        grid-rows (:rows grid)
        grid-cols (:cols grid)
        pw panel-width
        ph panel-height

        ;; Font sizes from config
        label-fsize (:label-font-size cfg)
        title-fsize (:title-font-size cfg)
        strip-fsize (:strip-font-size cfg 10)
        text-color (if-let [tc (:text-color cfg)]
                     (defaults/hex->rgba tc)
                     [0.2 0.2 0.2 1.0])

        ;; Tick-axis placement per populated cell. In a dense SPLOM this
        ;; matches the global "last row / col 0" rule, but in sparse
        ;; layouts (diagonal, triangular) we place x-ticks on the
        ;; bottommost populated panel of each column and y-ticks on the
        ;; leftmost populated panel of each row, so no axis is orphaned.
        bottom-row-per-col (into {} (for [[ci ps] (group-by :col panels)]
                                      [ci (apply max (map :row ps))]))
        leftmost-col-per-row (into {} (for [[ri ps] (group-by :row panels)]
                                        [ri (apply min (map :col ps))]))

        ;; Two-pass rendering: draw every panel's background first, then
        ;; foreground content on top. In sparse grids an earlier panel's
        ;; x-tick labels sit just below its own plot area, which overlaps
        ;; the cell below it. Without this split, that cell's panel
        ;; background (drawn later in a single pass) would paint over
        ;; those ticks.
        theme-bg (defaults/hex->rgba (:bg theme))
        panel-bgs
        (vec
         (for [p panels
               :let [ri (:row p)
                     ci (:col p)
                     x-off (+ y-label-pad (* ci pw))
                     y-off (+ title-pad strip-h (* ri ph))]]
           (ui/translate (+ x-off margin) (+ y-off margin)
                         (ui/with-color theme-bg
                           (ui/with-style ::ui/style-fill
                             (ui/rectangle (- pw margin margin)
                                           (- ph margin margin)))))))

        ;; Render each panel's foreground (grid, marks, ticks -- no bg)
        panel-elems
        (vec
         (for [p panels
               :let [ri (:row p)
                     ci (:col p)
                     show-x? (= ri (get bottom-row-per-col ci))
                     show-y? (= ci (get leftmost-col-per-row ri))
                     x-off (+ y-label-pad (* ci pw))
                     y-off (+ title-pad strip-h (* ri ph))]]
           (ui/translate x-off y-off
                         (panel/panel->membrane p pw ph margin cfg
                                                :show-x? show-x?
                                                :show-y? show-y?
                                                :include-bg? false
                                                :tooltip tooltip
                                                :x-col-name (or x-label "x")
                                                :y-col-name (or y-label "y")))))

        ;; Strip labels (column headers on top, row headers on right)
        ;; Use first panel found per column/row — handles triangular grids
        ;; where the expected corner panel may not exist.
        strip-label-color text-color

        col-strips
        (when (pos? strip-h)
          (let [by-col (->> panels
                            (filter :col-label)
                            (group-by :col))]
            (vec
             (for [ci (range grid-cols)
                   :let [p (first (get by-col ci))]
                   :when p]
               (let [cx (+ y-label-pad (* ci pw) (/ pw 2.0))
                     label (:col-label p)]
                 (ui/translate cx
                               (+ title-pad 2)
                               (ui/with-color strip-label-color
                                 (assoc (ui/label label (ui/font nil strip-fsize))
                                        :text-anchor "middle"))))))))

        row-strips
        (when (pos? strip-w)
          (let [by-row (->> panels
                            (filter :row-label)
                            (group-by :row))]
            (vec
             (for [ri (range grid-rows)
                   :let [p (first (get by-row ri))]
                   :when p]
               (let [cy (+ title-pad strip-h (* ri ph) (/ ph 2.0))]
                 (ui/translate (+ y-label-pad (* grid-cols pw) 5)
                               (- cy 5)
                               (ui/with-color strip-label-color
                                 (ui/label (:row-label p) (ui/font nil strip-fsize)))))))))]

    ;; Build the full PlotjeMembrane. Width and height become record
    ;; fields (so `(membrane.ui/width m)`/`(height m)` work via the
    ;; IBounds protocol); title is assoc'd as `:plotje/title`.
    (let [drawables
          (vec
           (concat
            ;; Title
            (when title
              (let [fsize title-fsize]
                [(ui/translate (+ y-label-pad (/ (* grid-cols pw) 2.0)) 14
                               (ui/with-color text-color
                                 (assoc (ui/label title (ui/font nil fsize))
                                        :text-anchor "middle")))]))
            ;; Subtitle
            (when subtitle
              [(ui/translate (+ y-label-pad (/ (* grid-cols pw) 2.0)) 30
                             (ui/with-color [0.4 0.4 0.4 1.0]
                               (assoc (ui/label subtitle (ui/font nil (- title-fsize 2)))
                                      :text-anchor "middle")))])
            ;; Y-axis label
            (when y-label
              (let [fsize label-fsize]
                [(ui/translate 12 (+ title-pad strip-h (/ (* grid-rows ph) 2.0))
                               (membrane.ui.Rotate. -90
                                                    (ui/with-color text-color
                                                      (assoc (ui/label y-label (ui/font nil fsize))
                                                             :text-anchor "middle"))))]))
            ;; X-axis label
            (when x-label
              (let [fsize label-fsize]
                [(ui/translate (+ y-label-pad (/ (* grid-cols pw) 2.0)) (- total-height (:label-offset cfg) #_x-label-pad -20)
                               (ui/with-color text-color
                                 (assoc (ui/label x-label (ui/font nil fsize))
                                        :text-anchor "middle")))]))
            ;; Legends — stacked vertically on the right (or horizontal for top/bottom)
            (let [any-legend? (or legend size-legend alpha-legend)]
              (when (and any-legend? (not= legend-pos :none))
                (let [legend-x (+ y-label-pad (* grid-cols pw) strip-w 10)
                      base-y (+ title-pad strip-h 20)]
                  (case legend-pos
                    :right
                    (let [;; Color legend
                          color-elems (when legend
                                        (render-legend-from-plan legend legend-x base-y cfg))
                          ;; Compute y offset after color legend
                          color-h (cond
                                    (nil? legend) 0
                                    (= :continuous (:type legend)) 160
                                    :else (+ 16 (* 16 (count (:entries legend)))))
                          ;; Size legend
                          size-y (+ base-y color-h (if legend 10 0))
                          size-elems (when size-legend
                                       (render-size-legend size-legend legend-x size-y))
                          size-h (if size-legend (+ 16 (* 18 (count (:entries size-legend)))) 0)
                          ;; Alpha legend
                          alpha-y (+ size-y size-h (if size-legend 10 0))
                          alpha-elems (when alpha-legend
                                        (render-alpha-legend alpha-legend legend-x alpha-y))]
                      (concat (or color-elems []) (or size-elems []) (or alpha-elems [])))
                    :top
                    (let [plots-start-y title-pad
                          legend-y (- plots-start-y (or legend-h 30) -5)]
                      (if legend
                        (render-legend-horizontal legend (+ y-label-pad 10) legend-y cfg)
                        []))
                    :bottom
                    (let [bottom-y (- total-height (or legend-h 30) -8)]
                      (if legend
                        (render-legend-horizontal legend (+ y-label-pad 10) bottom-y cfg)
                        []))
                    ;; Fallback to right
                    (when legend
                      (render-legend-from-plan legend legend-x base-y cfg))))))
            ;; Panels: all backgrounds first, then foregrounds, so that
            ;; x-tick labels that land in the cell below their panel are
            ;; not overpainted by that cell's background.
            panel-bgs
            panel-elems
            ;; Strip labels
            (or col-strips [])
            (or row-strips [])
            ;; Caption
            (when caption
              [(ui/translate (+ y-label-pad (* grid-cols pw) -10)
                             (- total-height 6)
                             (ui/with-color [0.5 0.5 0.5 1.0]
                               (assoc (ui/label caption (ui/font nil 9))
                                      :text-anchor "end")))])))]
      (cond-> (mem/->PlotjeMembrane drawables (long total-width) (long total-height))
        title (assoc :plotje/title title)))))
