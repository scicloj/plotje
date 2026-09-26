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
  (:require [clojure.set]
            [tablecloth.api :as tc]
            [scicloj.plotje.impl.pose :as pose]
            [scicloj.plotje.impl.plan :as plan]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]))

(defn- long-or [x default]
  (long (Math/round (double (or x default)))))

(defn- apply-shared-scale-domains
  "Keep the :x-scale-domain / :y-scale-domain that inject-shared-scales
   stamped on the leaf, dropping either where that axis has no domain
   to share. The plan reads them from the leaf's opts and uses each as
   the extent that axis's domain is computed from.

   An extent, not a `:domain`: a `:domain` says draw exactly this
   interval, and the plan honours it without padding, which is right
   for a domain the writer wrote and wrong for one the cells were
   given. Written as a `:domain`, a shared axis lost its padding and
   drew its outermost marks on the panel edge.

   An axis mapped with `:scale false` is left alone: it measures in
   drawing units and has no domain to share."
  [leaf]
  (let [opts (or (:opts leaf) {})
        unscaled? (fn [axis]
                    (false? (:scale (get-in leaf [:mapping axis]))))
        keep-dom (fn [o axis opt-key]
                   (if (and (contains? o opt-key) (unscaled? axis))
                     (dissoc o opt-key)
                     o))]
    (assoc leaf :opts (-> opts
                          (keep-dom :x :x-scale-domain)
                          (keep-dom :y :y-scale-domain)
                          (keep-dom :x :x-scale-domain-by-column)
                          (keep-dom :y :y-scale-domain-by-column)))))

(defn- outer-dimensions
  "The composite's total `[width height]`: its own `:width`/`:height`,
   and otherwise the configuration's, resolved at draw time the way
   `plan/draft->plan` resolves a leaf's -- so `pj/set-config!` and
   `pj/with-config` size a composite as they size a single plot. The
   fallback used to be 600 by 400 written here, which a composite built
   by `pj/pose` from `pj/cross` always drew at."
  [pose]
  (let [opts (or (:opts pose) {})
        cfg (defaults/resolve-config opts)]
    [(long-or (:width opts) (:width cfg))
     (long-or (:height opts) (:height cfg))]))

(def ^:private title-band-h
  "Height reserved at the top of a composite when :title is set."
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

;; ---- One scale behind a shared legend ----
;;
;; A shared legend is drawn once for every cell, so it is only true if
;; every cell encodes the column the same way. Each cell used to scale
;; its own data: a numeric column stretched the gradient over the
;; cell's own range, and categories took palette entries in the cell's
;; own order. The legend, taken from the first cell, then misdescribed
;; the others -- ggpubr's `ggarrange(common.legend = TRUE)` does the
;; same. Here the cells sharing a column are given one scale: the union
;; of their ranges, or one colour (or symbol) per category of their
;; union. A cell that writes its own scale keeps it, and the legend is
;; then drawn per cell rather than shared.

(defn- mapping-column [v]
  (if (map? v) (or (:from v) (:column v)) v))

(defn- written-scale
  "The scale spec written for aesthetic `a` on a resolved leaf -- on the
   leaf's mapping or on one of its layers' -- or nil."
  [leaf a]
  (or (let [v (get-in leaf [:mapping a])] (when (map? v) (:scale v)))
      (some #(let [v (get-in % [:mapping a])] (when (map? v) (:scale v)))
            (:layers leaf))))

(defn- leaf-aesthetic-data
  "Every non-nil value the columns mapped to `a` take in a resolved leaf."
  [leaf a]
  (let [cols (distinct (keep #(when-let [c (mapping-column %)]
                                (when (resolve/column-ref? c) c))
                             (cons (get-in leaf [:mapping a])
                                   (map #(get-in % [:mapping a]) (:layers leaf)))))]
    (for [c cols
          ds (distinct (keep identity (cons (:data leaf) (map :data (:layers leaf)))))
          :when (contains? (set (tc/column-names ds)) c)
          v (ds c)
          :when (some? v)]
      v)))

(defn- stamp-scale
  "Give leaf a scale spec for `a` beneath whatever it wrote: a key the
   leaf wrote wins, unless `force-keys` names it."
  [leaf a spec force-keys]
  (update leaf :mapping
          (fn [m]
            (let [v (get m a)
                  put (fn [written] (merge spec (apply dissoc written force-keys)))]
              (assoc m a (cond (map? v) (update v :scale #(put (or % {})))
                               (some? v) {:from v :scale spec}
                               :else {:scale spec}))))))

(defn- rgba->hex [[r g b a]]
  (let [c #(long (Math/round (* 255.0 (double %))))]
    (format "#%02X%02X%02X%02X" (c r) (c g) (c b) (c (or a 1.0)))))

(defn- unify-one
  "One scale for `a` over `leaves`, or nil where the cells cannot share
   one. Returns {:leaves :categories}."
  [leaves a cfg]
  (let [per-leaf (mapv #(vec (leaf-aesthetic-data % a)) leaves)
        all (apply concat per-leaf)
        written (mapv #(written-scale % a) leaves)]
    (cond
      ;; Sharing is between cells: one cell keeps the scale it has.
      (or (empty? all) (< (count (filter seq per-leaf)) 2)) {:leaves leaves}

      (every? number? all)
      (when (#{:color :size :alpha} a)
        (let [free (keep-indexed (fn [i vs] (when-not (:domain (written i)) vs)) per-leaf)
              vs (apply concat free)
              leaves' (if (seq vs)
                        (let [dom [(reduce min vs) (reduce max vs)]]
                          (mapv (fn [leaf w] (if (:domain w) leaf (stamp-scale leaf a {:domain dom} [])))
                                leaves written))
                        leaves)]
          (when (apply = (map #(:domain (written-scale % a)) leaves'))
            {:leaves leaves'})))

      (not-any? number? all)
      (when (#{:color :shape} a)
        (let [domain (some #(let [d (:domain %)] (when (sequential? d) d)) written)
              union (vec (distinct all))
              union (if domain
                      (vec (concat (filter (set union) domain) (remove (set domain) union)))
                      union)
              own-map (fn [w] (map? (:values w)))
              palette (some #(let [v (:values %)] (when (and v (not (map? v))) v)) written)
              values (if (= a :color)
                       (let [pal (or palette (defaults/scale-setting :color :values nil cfg))]
                         (into {} (map (fn [c] [c (rgba->hex (defaults/color-for union c pal))]) union)))
                       (zipmap union (cycle (or (seq palette) (defaults/shape-palette)))))
              leaves' (mapv (fn [leaf w] (if (own-map w) leaf (stamp-scale leaf a {:values values} [:values])))
                            leaves written)]
          (when (apply = (map #(:values (written-scale % a)) leaves'))
            {:leaves leaves' :categories union})))

      :else
      (do (println (str "Warning: the cells of this composite map " a
                        " to a column that holds numbers in some cells and"
                        " categories in others, so they cannot share one "
                        (name a) " scale, and each cell draws its own legend."))
          nil))))

(defn- unify-legend-scales
  "Give the cells of a composite one scale for each aesthetic whose
   legend they would share. Returns {:leaves :shared :categories}: the
   aesthetics that could not be unified are dropped from `shared`, so
   each cell keeps its own legend for them."
  [leaves shared cfg]
  (reduce (fn [acc a]
            (if-let [{ls :leaves cats :categories} (unify-one (:leaves acc) a cfg)]
              (cond-> (assoc acc :leaves ls)
                cats (assoc-in [:categories a] cats))
              (update acc :shared disj a)))
          {:leaves leaves :shared (set shared) :categories {}}
          (filter #{:color :size :alpha :shape} shared)))

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
        ;; One scale behind each shared legend; an aesthetic whose
        ;; cells cannot share one is dropped, and drawn per cell.
        {leaves :leaves shared-aesthetics :shared shared-categories :categories}
        (unify-legend-scales (pose/resolve-tree injected) shared-aesthetics
                             (defaults/resolve-config opts))
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
     :shared-categories shared-categories
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
  (let [{:keys [width height leaves layout shared? shared-aesthetics shared-categories chrome]}
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
                        (assoc :shared-aesthetics shared-aesthetics)
                        (assoc :shared-categories shared-categories))]
    (cond-> (resolve/->CompositeDraft width height sub-drafts chrome-spec layout)
      (get-in composite [:opts :align-panels])
      ;; The direction as the composite wrote it, not as the computed
      ;; layout holds it: `layout` here is a map of path to rect, and
      ;; which pads have to agree is a question about how the cells
      ;; were asked to run.
      (assoc :align-panels true
             :align-direction (:direction (:layout composite))))))

(defn composite-draft->plan
  "Convert a CompositeDraft into a CompositePlan. Per sub-draft, this
   calls draft->plan to produce a leaf plan and wraps it with its rect
   and path in :sub-plots. The shared-legend spec is computed once
   from a representative leaf draft (eliminating the rep-leaf-plan
   N+1 issue from the round-2 internals review)."
  [composite-draft]
  (let [{:keys [width height sub-drafts chrome-spec layout]} composite-draft
        ;; `:opts` rides along so the render stage can read what the
        ;; cell asked for. Plan-stage options already reach the cell
        ;; through `draft->plan` here; render-stage ones -- `:theme`
        ;; above all -- are resolved where the drawables are made, and
        ;; the composite used to hand every cell its own options there,
        ;; so a per-cell `{:theme {:bg ...}}` was accepted and dropped.
        plan-sub (fn [{:keys [path rect draft opts]}]
                   {:path path
                    :rect rect
                    :opts opts
                    :plan (plan/draft->plan draft opts)})
        ;; Each cell's unread-option warnings are collected rather
        ;; than printed, and a line is printed only where every cell
        ;; raised it -- see `plan/*unread-option-warnings*`. The
        ;; second, aligning pass would raise them all again, so its
        ;; are dropped.
        warnings (atom [])
        first-pass (binding [plan/*unread-option-warnings* warnings]
                     (mapv plan-sub sub-drafts))
        ;; Aligning drawing areas takes a second pass, because the pad a
        ;; cell needs is only known once that cell has been planned.
        ;; Pass one measures every cell's pads; pass two re-plans them
        ;; all with the widest of each as a floor. A cell whose own
        ;; labels need more still gets more, so one extra pass reaches a
        ;; common value and a third would change nothing.
        widest (fn [k] (->> first-pass
                            (map #(double (or (get-in % [:plan :layout k]) 0.0)))
                            (reduce max 0.0)))
        ;; Which pads have to agree depends on which way the cells run.
        ;; A column of cells shares its left and right edges, so the
        ;; y-label pad and the legend column are floored: only a cell
        ;; with a y title pays for the first, and only a cell drawing a
        ;; legend reserves the second. A row shares its top and bottom
        ;; edges instead, so the x-label pad is floored. A layout that
        ;; is neither -- a grid -- shares both.
        direction (:align-direction composite-draft)
        align-floors (cond-> {}
                       (not= direction :horizontal)
                       (assoc :min-y-label-pad (widest :y-label-pad)
                              :min-legend-w (widest :legend-w))
                       (not= direction :vertical)
                       (assoc :min-x-label-pad (widest :x-label-pad)))
        sub-plots (if (:align-panels composite-draft)
                    (binding [plan/*unread-option-warnings* (atom [])]
                      (mapv (fn [sub]
                              (plan-sub (update sub :opts merge align-floors)))
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
                                plan-legends (fn [sub]
                                               (plan/draft->plan (:draft sub)
                                                                 (merge (:opts sub) (select-keys rep-opts [:width :height]))))
                                rep-plan (binding [plan/*unread-option-warnings* warnings]
                                           (plan/draft->plan (:draft first-sub) rep-opts))
                                shared-keys (mapv aesthetic->legend-plan-key shared-aes)
                                ;; The first cell's legend lists only the
                                ;; categories it holds. Where the cells
                                ;; hold more between them, every cell's
                                ;; entries are gathered, in the order of
                                ;; the union their colours were given by.
                                cats (:shared-categories chrome-spec)
                                merge-entries
                                (fn [legends k]
                                  (let [legend (get rep-plan k)
                                        aes ({:legend :color :shape-legend :shape} k)
                                        n (count (get cats aes))]
                                    (if (and (:entries legend) (> n (count (:entries legend))))
                                      (let [all (binding [plan/*unread-option-warnings* (atom [])]
                                                  (mapv #(get (plan-legends
                                                               (update % :opts
                                                                       (fn [o] (reduce dissoc (dissoc o :suppress-legend)
                                                                                       shared-suppress-keys))))
                                                              k)
                                                        sub-drafts))]
                                        (assoc legends k
                                               (assoc legend :entries
                                                      (:out (reduce (fn [{:keys [seen] :as acc} e]
                                                                      (if (seen (:label e))
                                                                        acc
                                                                        (-> acc
                                                                            (update :seen conj (:label e))
                                                                            (update :out conj e))))
                                                                    {:seen #{} :out []}
                                                                    (mapcat :entries all))))))
                                      legends)))]
                            (reduce merge-entries (select-keys rep-plan shared-keys) shared-keys))))
        _ (when (seq (deref warnings))
            (run! println (sort (reduce clojure.set/intersection (deref warnings)))))
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

