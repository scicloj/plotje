(ns scicloj.plotje.impl.compositor
  "Composite-pose chrome layout, composite-pose->draft, and
   composite-draft->plan. Pure data-side: shared-scale reconciliation,
   chrome geometry computation, per-leaf opt adjustment. The
   plan-to-membrane rendering for composites lives in
   `render/composite.clj`, keeping this namespace free of membrane
   dependencies.

   Shared scales are reconciled before drafting by stamping a forced
   domain on matching leaves (impl.pose/inject-shared-scales).

   When the composite root carries a legend-producing mapping
   (:color/:size/:alpha), the chrome reserves a strip on the right
   of the grid; the per-leaf opts get :suppress-legend true so each
   cell hides its own legend, and the rendering side (render/
   composite.clj) draws ONE shared legend in the reserved strip."
  (:require [scicloj.plotje.impl.pose :as pose]
            [scicloj.plotje.impl.plan :as plan]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]))

(def ^:private default-width 600)
(def ^:private default-height 400)

(defn- long-or [x default]
  (long (Math/round (double (or x default)))))

(defn- domain->scale-entry
  [existing-scale domain]
  (let [base (or existing-scale {:type :linear})]
    (assoc base :domain domain)))

(defn- apply-shared-scale-domains
  "If :x-scale-domain / :y-scale-domain were stamped on the leaf by
   inject-shared-scales, write them as a `:domain` on the axis's scale,
   where the plan reads it.

   A scale lives with the mapping it reads, so a shared domain is set
   the same way `pj/scale` sets one. An axis mapped with `:scale false`
   is left alone: it measures in drawing units and has no domain to
   share."
  [leaf]
  (let [opts (or (:opts leaf) {})
        x-dom (:x-scale-domain opts)
        y-dom (:y-scale-domain opts)
        unscaled? (fn [axis]
                    (false? (:scale (get-in leaf [:mapping axis]))))
        put (fn [mapping axis dom]
              (if (or (nil? dom) (unscaled? axis))
                mapping
                (pose/put-scale mapping axis {:domain dom})))]
    (if (or x-dom y-dom)
      (-> leaf
          (assoc :opts (dissoc opts :x-scale-domain :y-scale-domain))
          (update :mapping put :x x-dom)
          (update :mapping put :y y-dom))
      leaf)))

(defn- outer-dimensions
  [pose]
  (let [opts (or (:opts pose) {})]
    [(long-or (:width opts) default-width)
     (long-or (:height opts) default-height)]))

(def ^:private title-band-h
  "Pixel height reserved at the top of a composite when :title is set."
  30)

(def ^:private composite-chrome-opt-keys
  "Opts that live at the composite root -- the outer title band, etc.
   They must not inherit down to leaves (where resolve-tree's parent/
   child merge would otherwise stamp them into every cell's chrome)."
  [:title :subtitle :caption])

(defn- composite-with-stripped-leaf-opts
  "Return the composite with composite-level chrome opts removed from
   its root :opts, so resolve-tree doesn't propagate them into leaves."
  [composite]
  (update composite :opts #(apply dissoc % composite-chrome-opt-keys)))

(def ^:private legend-bearing-aesthetics
  "Aesthetics that produce a legend at render time. Derived from
   `defaults/aesthetic-registry`."
  defaults/legend-bearing-aesthetics)

(defn- leaf-aesthetic-values
  "Set of mapping values an aesthetic resolves to inside a single
   leaf (counting both the leaf's :mapping and each layer's
   :mapping). Two leaves agree on the aesthetic when their value
   sets are equal -- the same legend will be produced for both."
  [leaf a]
  (let [v (get-in leaf [:mapping a])
        layer-vs (keep #(get-in % [:mapping a]) (:layers leaf))]
    (cond-> (set layer-vs)
      v (conj v))))

(defn- shared-aesthetics-by-leaves
  "The subset of legend-bearing aesthetics that produce the same
   legend across every leaf. Considers the merged-down :mapping
   (so root-level mappings flow into every leaf, and sub-pose-only
   mappings count as shared when every sub-pose agrees). Layer-
   level mappings (e.g. lay-point's opts {:color :c}) are folded
   in, so a cell whose color comes from a layer mapping participates
   in the unanimity check. When the result is non-empty, the
   compositor renders one shared legend at composite level and
   per-channel suppresses only those aesthetics on each leaf --
   legends for non-unanimous aesthetics keep rendering per-leaf."
  [composite]
  (let [leaves (pose/resolve-tree composite)]
    (set
     (filter (fn [a]
               (let [vss (mapv #(leaf-aesthetic-values % a) leaves)]
                 (and (every? seq vss) (apply = vss))))
             legend-bearing-aesthetics))))

(def ^:private aesthetic->suppress-key
  {:color :suppress-color-legend
   :size :suppress-size-legend
   :alpha :suppress-alpha-legend
   :shape :suppress-shape-legend})

(def ^:private aesthetic->legend-plan-key
  {:color :legend
   :size :size-legend
   :alpha :alpha-legend
   :shape :shape-legend})

(def ^:private shared-legend-strip-w
  "Pixel width reserved on the right side of the composite for the
   shared legend strip."
  120)

(def ^:private grid-strip-h
  "Pixel height reserved at the top of a grid composite for column
   strip labels."
  20)

(defn- grid-strip-w
  "Pixel width reserved at the left of a grid composite for row strip
   labels. Scales with the longest label so long column names fit."
  [row-labels]
  (let [max-chars (reduce max 0 (map count row-labels))]
    (+ 8 (* max-chars 7))))

(defn- resolve-composite-chrome
  "Compute the resolved leaves, layout map, and chrome geometry for a
   composite pose. Used by composite-pose->draft to produce the
   sub-drafts and the chrome-spec.

   Returns:
     {:width  outer width
      :height outer height
      :leaves resolved leaves with shared-scale + suppress-* applied
      :layout map of leaf-path -> [x y w h]
      :shared? true when the composite carries a legend-producing root mapping
      :chrome {:title :title-band-h :grid-rect :legend-w :strip-h :strip-w
               :col-labels :row-labels :n-cols :n-rows :matrix? :layout}}"
  [composite]
  (let [[w h] (outer-dimensions composite)
        opts (or (:opts composite) {})
        title (:title opts)
        top-pad (if title title-band-h 0)
        ;; Strip composite-chrome keys before resolve-tree so leaves
        ;; don't inherit them. The composite itself still renders
        ;; chrome via the title band above the leaf trees.
        stripped (composite-with-stripped-leaf-opts composite)
        injected (pose/inject-shared-scales stripped)
        ;; Detect which legend-bearing aesthetics are unanimous across
        ;; all descendant leaves; those produce one shared legend at
        ;; composite level. Aesthetics that disagree (or are absent)
        ;; render per-leaf as before.
        shared-aesthetics (shared-aesthetics-by-leaves composite)
        shared? (boolean (seq shared-aesthetics))
        legend-w (if shared? shared-legend-strip-w 0)
        ;; Grid-composite (rows-of-cols SPLOM) stamps :grid-strip-labels
        ;; on its root. Matrix-direction composites have the labels
        ;; derived lazily from leaf positions via pose/matrix-axes.
        ;; Either way, we end up with :col-labels / :row-labels.
        ;; Reserve strip-h at the top for column labels and strip-w
        ;; at the left for row labels, and draw the strips outside
        ;; the cell rects so per-cell layout stays untouched.
        matrix-axes (when (= :matrix (:direction (:layout composite)))
                      (pose/matrix-axes composite))
        {:keys [col-labels row-labels]}
        (or (:grid-strip-labels composite)
            (when matrix-axes
              {:col-labels (:col-labels matrix-axes)
               :row-labels (:row-labels matrix-axes)}))
        col-labels (vec col-labels)
        row-labels (vec row-labels)
        strip-h (if (seq col-labels) grid-strip-h 0)
        strip-w (if (seq row-labels) (grid-strip-w row-labels) 0)
        n-cols (count col-labels)
        n-rows (count row-labels)
        grid-w (max 1 (- w legend-w strip-w))
        grid-rect [(double strip-w)
                   (double (+ top-pad strip-h))
                   (double grid-w)
                   (double (- h top-pad strip-h))]
        layout (pose/compute-layout injected grid-rect)
        leaves (pose/resolve-tree injected)
        ;; In matrix layout, the strip labels at the top carry the
        ;; column's x-col name and the strip labels on the left carry
        ;; the row's y-col name. Suppress the per-leaf x-label /
        ;; y-label so they don't render redundantly inside each cell.
        ;; Same idea SPLOM cells use, applied uniformly here.
        leaves (if matrix-axes
                 (let [suppress-x? (seq (:col-labels matrix-axes))
                       suppress-y? (seq (:row-labels matrix-axes))]
                   (mapv (fn [leaf]
                           (update leaf :opts
                                   (fn [o]
                                     (cond-> (or o {})
                                       suppress-x? (assoc :suppress-x-label true)
                                       suppress-y? (assoc :suppress-y-label true)))))
                         leaves))
                 leaves)
        chrome {:title title
                :title-band-h top-pad
                :grid-rect grid-rect
                :legend-w legend-w
                :strip-h strip-h
                :strip-w strip-w
                :col-labels col-labels
                :row-labels row-labels
                :n-cols n-cols
                :n-rows n-rows
                :matrix? (boolean matrix-axes)
                :layout layout}]
    {:width w
     :height h
     :leaves leaves
     :layout layout
     :shared? shared?
     :shared-aesthetics shared-aesthetics
     :chrome chrome}))

(defn composite-pose->draft
  "Resolve a composite pose into a CompositeDraft. Each sub-draft entry
   carries the leaf's path, its rect inside the composite, the
   contextualized leaf draft (shared-scale domains injected,
   per-leaf opts adjusted), and the per-leaf opts (width/height
   merged from the rect).

   Per-leaf draft contextualization happens here, not at plan stage:
     - Shared-scale domains are stamped via inject-shared-scales /
       apply-shared-scale-domains, so per-leaf drafts carry forced
       :x-scale / :y-scale.
     - Matrix-layout strip labels suppress the per-leaf x-label /
       y-label (so axis labels appear only on the strip, not inside
       each cell).

   The chrome-spec captures the resolved chrome geometry for the
   composite as a whole; the layout map (path -> rect) is kept as a
   first-class field on the CompositeDraft so downstream stages do
   not need to recompute layout from the original pose tree."
  [composite]
  (let [{:keys [width height leaves layout shared? shared-aesthetics chrome]}
        (resolve-composite-chrome composite)
        suppress-keys (mapv aesthetic->suppress-key shared-aesthetics)
        ;; When the composite carries its own title/subtitle/caption,
        ;; suppress the same keys on each leaf so they don't double-
        ;; render. The composite title sits in the title-band-h strip
        ;; above the grid; per-leaf titles inside each cell would be
        ;; redundant with the outer chrome.
        composite-chrome-suppress (filterv #(get-in composite [:opts %])
                                           [:title :subtitle :caption])
        sub-drafts (mapv (fn [leaf]
                           (let [rect (get layout (:path leaf))
                                 [_ _ rw rh] rect
                                 leaf' (apply-shared-scale-domains leaf)
                                 leaf-opts (cond-> (assoc (or (:opts leaf') {})
                                                          :width (max 1 (long-or rw 1))
                                                          :height (max 1 (long-or rh 1)))
                                             (seq suppress-keys)
                                             (as-> $ (reduce #(assoc %1 %2 true)
                                                             $ suppress-keys))
                                             (seq composite-chrome-suppress)
                                             (as-> $ (reduce #(dissoc %1 %2)
                                                             $ composite-chrome-suppress)))
                                 draft (pose/leaf->draft leaf')]
                             {:path (:path leaf)
                              :rect rect
                              :draft draft
                              :opts leaf-opts}))
                         leaves)
        chrome-spec (-> chrome
                        (assoc :shared? shared?)
                        (assoc :shared-aesthetics shared-aesthetics))]
    (cond-> (resolve/->CompositeDraft width height sub-drafts chrome-spec layout)
      (get-in composite [:opts :align-panels])
      (assoc :align-panels true))))

(defn composite-draft->plan
  "Convert a CompositeDraft into a CompositePlan. Per sub-draft, this
   calls draft->plan to produce a leaf plan and wraps it with its rect
   and path in :sub-plots. The shared-legend spec is computed once
   from a representative leaf draft (eliminating the rep-leaf-plan
   N+1 issue from the round-2 internals review)."
  [composite-draft]
  (let [{:keys [width height sub-drafts chrome-spec layout]} composite-draft
        plan-sub (fn [{:keys [path rect draft opts]}]
                   {:path path
                    :rect rect
                    :plan (plan/draft->plan draft opts)})
        first-pass (mapv plan-sub sub-drafts)
        ;; Aligning drawing areas takes a second pass, because the pad a
        ;; cell needs is only known once that cell has been planned.
        ;; Pass one measures every cell's y-label pad; pass two re-plans
        ;; them all with the widest as a floor. A cell whose own labels
        ;; need more still gets more, so one extra pass reaches a common
        ;; value and a third would change nothing.
        ;; Both sides of the drawing area, since either can differ: the
        ;; y-label pad on the left, because only a cell with a y title
        ;; pays for one, and the legend column on the right, because
        ;; only a cell that draws a legend reserves it.
        widest (fn [k] (->> first-pass
                            (map #(double (or (get-in % [:plan :layout k]) 0.0)))
                            (reduce max 0.0)))
        sub-plots (if (:align-panels composite-draft)
                    (let [pad (widest :y-label-pad)
                          legend-w (widest :legend-w)]
                      (mapv (fn [sub]
                              (plan-sub (update sub :opts assoc
                                                :min-y-label-pad pad
                                                :min-legend-w legend-w)))
                            sub-drafts))
                    first-pass)
        shared-legend (when (:shared? chrome-spec)
                        (when-let [first-sub (first sub-drafts)]
                          (let [shared-aes (:shared-aesthetics chrome-spec)
                                shared-suppress-keys (mapv aesthetic->suppress-key shared-aes)
                                rep-opts (-> (:opts first-sub)
                                             (dissoc :suppress-legend)
                                             (as-> $ (reduce #(dissoc %1 %2)
                                                             $ shared-suppress-keys))
                                             (assoc :width 600 :height 400))
                                rep-plan (plan/draft->plan (:draft first-sub) rep-opts)
                                shared-keys (mapv aesthetic->legend-plan-key shared-aes)]
                            (select-keys rep-plan shared-keys))))
        chrome (-> chrome-spec
                   (dissoc :shared?)
                   (assoc :shared-legend shared-legend)
                   (assoc :layout layout))]
    (assoc (resolve/->CompositePlan width height sub-plots chrome)
           :composite? true
           ;; Mirror the LeafPlan keys read by the plan->plot
           ;; defmethods (render/svg.clj, render/bufimg.clj) -- these
           ;; are how render-opts flow into membrane->plot. Without
           ;; them, plan->plot on a CompositePlan would build a
           ;; figure with no title and zero outer dimensions.
           :total-width width
           :total-height height
           :title (:title chrome))))

