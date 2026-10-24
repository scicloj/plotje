(ns scicloj.plotje.impl.compositor
  "Composite-pose chrome layout, composite-pose->draft, and
   composite-draft->plan. Pure data-side: shared-scale reconciliation,
   chrome geometry computation, per-leaf opt adjustment. The
   plan-to-membrane rendering for composites lives in
   `render/composite.clj`, keeping this namespace free of membrane
   dependencies.

   Shared scales: the pose stage says which cells share an axis
   (impl.pose/shared-axis-groups), and `composite-draft->plan` measures
   what those cells draw, pools it, and plans each with the pooled
   extent.

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

(defn- leaf-share
  "The share groups of a leaf (`pose/shared-axis-groups`), leaving out
   an axis mapped with `:scale false`: it measures in drawing units and
   has no domain to share."
  [leaf share]
  (let [unscaled? (fn [axis]
                    (false? (:scale (get-in leaf [:mapping axis]))))]
    (not-empty (into {} (remove (fn [[axis _]] (unscaled? axis))) share))))

(defn- shared-extents
  "The extent each sharing cell is planned with, as
   `{path {:x-scale-domain e :y-scale-domain e}}`.

   Every cell in a share group is asked what it draws
   (`plan/draft->extents`: its stats run, so a stack's top, an error
   bar's ends and a count are all in it), and a group's answers are
   pooled -- numbers by lowest and highest, categories by union, in the
   order of the data where the cells read one dataset
   (`plan/order-categories`) and cell by cell where they do not. The
   plan reads the pooled extent from the cell's opts as the extent its
   own values would give, and pads and anchors it there, so every cell
   of a group ends on one domain.

   An extent, not a `:domain`: a `:domain` says draw exactly this
   interval, and the plan honours it without padding, which is right
   for a domain the writer specified and wrong for one the cells were
   given.

   A `:domain` one member specifies for the shared axis is the group's:
   every member is planned as though it had specified it, exactly and
   unpadded, under `:x-shared-domain` / `:y-shared-domain` in its opts.
   Members specifying different domains, or a `:domain` beside another
   member's `:include`, report an error. A categorical `:domain` orders
   the pooled categories, as it orders the axis of one plot.

   One function with the facet's panels: both pool what is drawn and
   pad once. Until a stat's result is kept between the two passes, a
   sharing cell's stats run twice."
  [sub-drafts]
  (let [sharing (filter :share sub-drafts)
        measured (into {} (map (fn [{:keys [path draft opts]}]
                                 [path (plan/draft->extents draft opts)]))
                       sharing)
        members (for [{:keys [path share]} sharing
                      [axis {:keys [group order]}] share]
                  {:path path :axis axis :group group :order order})
        ;; The scale spec a member specifies for the shared axis, read where
        ;; the plan reads it -- the first of its draft layers naming one
        ;; (`plan/resolve-panel-domains`).
        draft-of (into {} (map (juxt :path :draft)) sharing)
        layers-of (fn [path] (let [d (draft-of path)] (if (map? d) [d] d)))
        spec-of (fn [{:keys [path axis]}]
                  (some (if (= axis :x) :x-scale :y-scale) (layers-of path)))
        specified (comp :domain spec-of)
        column-of (fn [{:keys [path axis]}] (some axis (layers-of path)))
        flipped? (fn [{:keys [path]}] (= :flip (some :coord (layers-of path))))
        ;; `:share-scales` names the axis as drawn.
        drawn-axis (fn [m] (if (flipped? m) ({:x :y :y :x} (:axis m)) (:axis m)))
        same (fn [d] (mapv #(if (number? %) (double %) %) d))]
    (reduce (fn [m [_ ms]]
              (let [specifying (filter specified ms)
                    domains (vals (group-by (comp same specified) specifying))
                    shared-axis (drawn-axis (first ms))
                    _ (when (next domains)
                        (let [[a b] (map first domains)]
                          (throw (ex-info
                                  (str "The cells sharing " shared-axis " specify different"
                                       " domains for it: " (pr-str (vec (specified a)))
                                       " in the cell drawing " (pr-str (column-of a))
                                       " and " (pr-str (vec (specified b)))
                                       " in the cell drawing " (pr-str (column-of b))
                                       ". A shared axis has one domain. Keep the :domain"
                                       " on one of these cells and take it off the"
                                       " others"
                                       ;; A scale on the arrangement reaches only
                                       ;; the cells specifying none, so on these
                                       ;; cells as they stand it changes nothing.
                                       (when-not (some flipped? ms)
                                         (str ", or take it off every cell and specify"
                                              " it once on the arrangement, (pj/scale"
                                              " <the arrangement> " shared-axis
                                              " {:domain ...})"))
                                       "; or leave " shared-axis " out of :share-scales and"
                                       " each cell keeps its own.")
                                  {:caller "share-scales"
                                   :axis shared-axis
                                   :domains (mapv (fn [g] {:domain (vec (specified (first g)))
                                                           :columns (mapv column-of g)
                                                           :paths (mapv :path g)})
                                                  domains)}))))
                    the-domain (some-> (first specifying) specified vec)
                    ;; On one plot a `:domain` and an `:include` for one
                    ;; axis report an error. Handed the group's domain,
                    ;; a member specifying an `:include` would be blamed
                    ;; for both, so the group says it here.
                    _ (when-let [inc (and the-domain (first (filter (comp :include spec-of) ms)))]
                        (throw (ex-info
                                (str "The cells sharing " shared-axis " specify both a :domain"
                                     " and an :include for it: :domain " (pr-str the-domain)
                                     " in the cell drawing " (pr-str (column-of (first specifying)))
                                     " and :include " (pr-str (:include (spec-of inc)))
                                     " in the cell drawing " (pr-str (column-of inc))
                                     ". A shared axis has one domain, and a :domain replaces"
                                     " the extent an :include adds to. Take one off, or leave "
                                     shared-axis " out of :share-scales.")
                                {:caller "share-scales" :axis shared-axis
                                 :domain the-domain :include (:include (spec-of inc))})))
                    ;; The members specifying a domain first: where the
                    ;; axis holds categories their order leads, as it
                    ;; does on a single plot.
                    ms' (concat specifying (remove specified ms))
                    pooled (plan/pool-extents (map #(get-in measured [(:path %) (:axis %)]) ms'))
                    _ (when (= :scicloj.plotje.impl.plan/mixed pooled)
                        (throw (ex-info
                                (str "The cells sharing an axis disagree about what it"
                                     " holds: some draw categories there and some draw"
                                     " numbers. Leave the axis out of :share-scales, or"
                                     " give the cells a column of one type.")
                                {:caller "share-scales"
                                 :extents (mapv #(get-in measured [(:path %) (:axis %)]) ms)})))
                    categories? (and (seq pooled) (not (number? (first pooled))))
                    pooled (if (and categories? (not the-domain))
                             (plan/order-categories pooled (some :order ms))
                             pooled)
                    ;; A specified domain on an axis of numbers or dates
                    ;; is the group's domain, exactly: every member is
                    ;; planned as though it had specified it.
                    exact (when (and the-domain (not categories?)) the-domain)]
                (if (seq pooled)
                  (reduce (fn [m {:keys [path axis]}]
                            ;; An exact domain stands alone: handed the
                            ;; pooled extent beside it, a date axis
                            ;; picked its ticks over the extent and not
                            ;; over the domain, as a single plot does.
                            (if exact
                              (assoc-in m [path (if (= axis :x) :x-shared-domain :y-shared-domain)]
                                        exact)
                              (assoc-in m [path (if (= axis :x) :x-scale-domain :y-scale-domain)]
                                        pooled)))
                          m ms)
                  m)))
            {}
            (group-by :group members))))

(defn- shared-floors
  "Where the planned cells of a share group ended on different domains,
   the widest of them for each of its cells, as `{path {axis [lo hi]}}`
   by data axis; empty where every group agrees. Categorical domains
   are left out: they are shared whole and nothing widens them."
  [sub-drafts sub-plots]
  (let [plan-of (into {} (map (juxt :path :plan)) sub-plots)
        final (fn [path axis]
                (let [doms (for [p (:panels (plan-of path))
                                 :let [k (if (= (= axis :x) (not= :flip (:coord p)))
                                           :x-domain :y-domain)
                                       d (get p k)]
                                 :when (and (sequential? d) (= 2 (count d)) (number? (first d)))]
                             d)]
                  (when (seq doms)
                    [(reduce min (map first doms)) (reduce max (map second doms))])))
        members (for [{:keys [path share]} sub-drafts
                      [axis {:keys [group]}] share
                      :let [d (final path axis)]
                      :when d]
                  {:path path :axis axis :group group :domain d})]
    (reduce (fn [m [_ ms]]
              (let [widest [(reduce min (map (comp first :domain) ms))
                            (reduce max (map (comp second :domain) ms))]]
                (if (every? #(= widest (:domain %)) ms)
                  m
                  (reduce (fn [m {:keys [path axis]}] (assoc-in m [path axis] widest)) m ms))))
            {}
            (group-by :group members))))

(defn- settle-shared
  "The planned cells, with the cells of every share group on one domain.

   A cell's numeric domain is widened after it is planned so that its
   text marks are drawn in full, by an amount that depends on its own
   labels and its own width. Two cells sharing an axis start from one
   pooled extent and can end apart. Where they do, they are planned
   again with the widest domain as a floor, which each may still widen
   for its labels; if they are still apart, once more with the floor
   and the fit switched off on that axis, which leaves them equal. A
   facet does the same within its one plan, pooling again after each
   fit."
  [sub-drafts sub-plots replan]
  (loop [plots sub-plots
         k 0]
    (let [floors (shared-floors sub-drafts plots)]
      (if (or (empty? floors) (>= k 2))
        plots
        (recur (mapv (fn [sub plot]
                       (if-let [f (get floors (:path sub))]
                         (replan (update sub :opts merge
                                         (cond-> {:domain-floors f}
                                           (= k 1) (assoc :domains-settled (set (keys f))))))
                         plot))
                     sub-drafts plots)
               (inc k))))))

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

(def ^:private axis-title-band
  "Room for an axis title a composite draws once beside its grid."
  22)

(def ^:private title-band-h
  "Height reserved at the top of a composite when :title is set."
  30)

(def ^:private subtitle-band-h
  "Height reserved under the title band when :subtitle is set."
  18)

(def ^:private caption-band-h
  "Height reserved under the grid when :caption is set."
  18)

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
  (let [;; An empty cell draws no legend, so it neither agrees nor
        ;; disagrees with the cells that do.
        leaves (remove :plotje/empty-cell (pose/resolve-tree composite))]
    (set
     (filter (fn [a]
               (let [vss (mapv #(leaf-aesthetic-values % a) leaves)]
                 (and (seq vss) (every? seq vss) (apply = vss))))
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

(defn- leaf-aesthetic-columns
  "The columns mapped to `a` in a resolved leaf, on its mapping or on
   one of its layers'."
  [leaf a]
  (distinct (keep #(when-let [c (mapping-column %)]
                     (when (resolve/column-ref? c) c))
                  (cons (get-in leaf [:mapping a])
                        (map #(get-in % [:mapping a]) (:layers leaf))))))

(defn- leaf-aesthetic-data
  "Every non-nil value the columns mapped to `a` take in a resolved leaf."
  [leaf a]
  (let [cols (leaf-aesthetic-columns leaf a)]
    (for [c cols
          ds (distinct (keep identity (cons (:data leaf) (map :data (:layers leaf)))))
          :when (contains? (set (tc/column-names ds)) c)
          v (ds c)
          :when (some? v)]
      v)))

(defn- leaf-scale-kinds
  "How the columns mapped to `a` in a resolved leaf are read, as the
   plan reads them: a set of `:categorical` and `:numerical`. A shape
   is always a set of categories; a colour follows
   `resolve/color-column-type`, so a numeric column written
   `{:color-type :categorical}` is categories; a size or an alpha is
   numeric when its values are numbers."
  [leaf a]
  (set
   (for [[m ds] (cons [(:mapping leaf) (:data leaf)]
                      (map (fn [l] [(merge (:mapping leaf) (:mapping l))
                                    (or (:data l) (:data leaf))])
                           (:layers leaf)))
         :let [c (mapping-column (get m a))]
         :when (and (resolve/column-ref? c) ds
                    (contains? (set (tc/column-names ds)) c))]
     (case a
       :shape :categorical
       :color (if (= :categorical (resolve/color-column-type (:color-type m) ds c))
                :categorical
                :numerical)
       (if (every? number? (remove nil? (ds c))) :numerical :categorical)))))

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
   one. Returns {:leaves :categories}. `whole` is the dataset every
   cell reads, whole or in part, or nil where they read no one dataset:
   categories are then listed in the order of its rows
   (`plan/order-categories`), as a shared categorical axis lists them,
   and cell by cell otherwise."
  [leaves a cfg whole]
  (let [per-leaf (mapv #(vec (leaf-aesthetic-data % a)) leaves)
        all (apply concat per-leaf)
        written (mapv #(written-scale % a) leaves)
        ;; Numeric or categorical is decided as the plan decides it,
        ;; not from the values: a column of numbers written
        ;; `:color-type :categorical` is shared as categories.
        kinds (apply clojure.set/union (map #(leaf-scale-kinds % a) leaves))]
    (cond
      ;; Sharing is between cells: one cell keeps the scale it has.
      (or (empty? all) (< (count (filter seq per-leaf)) 2)) {:leaves leaves}

      (and (= kinds #{:numerical}) (every? number? all))
      (when (#{:color :size :alpha} a)
        (let [free (keep-indexed (fn [i vs] (when-not (:domain (written i)) vs)) per-leaf)
              vs (apply concat free)
              leaves' (if (seq vs)
                        (let [dom [(reduce min vs) (reduce max vs)]]
                          (mapv (fn [leaf w] (if (or (:domain w) (:plotje/empty-cell leaf)) leaf (stamp-scale leaf a {:domain dom} [])))
                                leaves written))
                        leaves)]
          (when (apply = (map #(:domain (written-scale % a)) (remove :plotje/empty-cell leaves')))
            {:leaves leaves'})))

      (= kinds #{:categorical})
      (when (#{:color :shape} a)
        (let [domain (some #(let [d (:domain %)] (when (sequential? d) d)) written)
              union (vec (distinct all))
              union (plan/order-categories
                     union
                     (pose/data-category-order
                      whole (distinct (mapcat #(leaf-aesthetic-columns % a) leaves))))
              union (if domain
                      (vec (concat (filter (set union) domain) (remove (set domain) union)))
                      union)
              own-map (fn [w] (map? (:values w)))
              palette (some #(let [v (:values %)] (when (and v (not (map? v))) v)) written)
              values (if (= a :color)
                       (let [pal (or palette (defaults/scale-setting :color :values nil cfg))]
                         (into {} (map (fn [c] [c (rgba->hex (defaults/color-for union c pal))]) union)))
                       (zipmap union (cycle (or (seq palette) (defaults/shape-palette)))))
              leaves' (mapv (fn [leaf w] (if (or (own-map w) (:plotje/empty-cell leaf)) leaf (stamp-scale leaf a {:values values} [:values])))
                            leaves written)]
          (when (apply = (map #(:values (written-scale % a)) (remove :plotje/empty-cell leaves')))
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
  [leaves shared cfg whole]
  (reduce (fn [acc a]
            (if-let [{ls :leaves cats :categories} (unify-one (:leaves acc) a cfg whole)]
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
      :leaves resolved leaves with their share groups (`::share`) + suppress-* applied
      :layout map of leaf-path -> [x y w h]
      :shared? true when the composite carries a legend-producing root mapping
      :chrome {:title :title-band-h :grid-rect :legend-w :strip-h :strip-w
               :col-labels :row-labels :n-cols :n-rows :matrix? :layout}}"
  [composite]
  (let [[w h] (outer-dimensions composite)
        opts (or (:opts composite) {})
        title (:title opts)
        subtitle (:subtitle opts)
        caption (:caption opts)
        ;; A subtitle takes a line under the title band; a caption a
        ;; line under the grid.
        top-pad (+ (if title title-band-h 0) (if subtitle subtitle-band-h 0))
        caption-h (if caption caption-band-h 0)
        ;; Strip composite-chrome keys before resolve-tree so leaves
        ;; don't inherit them. The composite itself still renders
        ;; chrome via the title band above the leaf trees.
        stripped (composite-with-stripped-leaf-opts composite)
        share-groups (pose/shared-axis-groups stripped)
        ;; Detect which legend-bearing aesthetics are unanimous across
        ;; all descendant leaves; those produce one shared legend at
        ;; composite level. Aesthetics that disagree (or are absent)
        ;; render per-leaf as before.
        shared-aesthetics (shared-aesthetics-by-leaves composite)
        ;; One scale behind each shared legend; an aesthetic whose
        ;; cells cannot share one is dropped, and drawn per cell.
        {leaves :leaves shared-aesthetics :shared shared-categories :categories}
        (unify-legend-scales (mapv (fn [leaf]
                                     (if-let [sh (leaf-share leaf (get share-groups (:path leaf)))]
                                       (assoc leaf ::share sh)
                                       leaf))
                                   (pose/resolve-tree stripped))
                             shared-aesthetics
                             (defaults/resolve-config opts)
                             ;; The one dataset the cells read, where
                             ;; they do: the composite's own, each cell
                             ;; holding it whole or a part of it -- the
                             ;; test `pose/shared-axis-groups` makes for
                             ;; a shared axis.
                             (when-let [data (:data stripped)]
                               (when (every? #(or (:plotje/empty-cell %)
                                                  (identical? data (:data %))
                                                  (:plotje/part-of-parent-data %))
                                             (pose/resolve-tree stripped))
                                 data)))
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
        ;; Axis titles the composite draws once, beside the grid, as a
        ;; facet does: a band below for x, one at the left for y.
        axis-titles (set (:grid-axis-titles composite))
        x-title-h (if (axis-titles :x) axis-title-band 0)
        y-title-w (if (axis-titles :y) axis-title-band 0)
        grid-w (max 1 (- w legend-w strip-w y-title-w))
        grid-rect [(double (+ strip-w y-title-w))
                   (double (+ top-pad strip-h))
                   (double grid-w)
                   (double (- h top-pad strip-h x-title-h caption-h))]
        layout (pose/compute-layout stripped grid-rect)
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
                :subtitle subtitle
                :caption caption
                :title-band-h top-pad
                :axis-title-axes axis-titles
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

(defn- align-direction
  "Which way a composite's cells run, for aligning their drawing areas.
   `pj/arrange` builds rows stacked vertically, so the root says
   `:vertical` for a single row and for a grid alike, and aligning
   either as a column left its bottom edges uneven. Rows of several
   cells are aligned both ways -- equal widths and level bottoms -- as a
   grid; a single column, and anything else, by the direction the root
   writes. An axis title shared by every cell is drawn once by the
   composite (`:grid-axis-titles`), so no row pays for a title only the
   bottom row draws."
  [composite]
  (let [d (:direction (:layout composite))
        rows (:poses composite)
        row-of? #(= :horizontal (get-in % [:layout :direction]))]
    (if (and (= :vertical d) (seq rows) (every? row-of? rows))
      (let [n-rows (count rows)
            n-cols (reduce max 0 (map #(count (:poses %)) rows))]
        (if (> n-cols 1) :grid d))
      d)))

(defn composite-pose->draft
  "Resolve a composite pose into a CompositeDraft. Each sub-draft entry
   carries the leaf's path, its rect inside the composite, the
   contextualized leaf draft (shared-scale domains injected,
   per-leaf opts adjusted), and the per-leaf opts (width/height
   merged from the rect).

   Per-leaf draft contextualization happens here, not at plan stage:
     - The share groups of each leaf ride along as `:share`; the
       plan stage pools what the groups draw (`shared-extents`).
     - Matrix-layout strip labels suppress the per-leaf x-label /
       y-label (so axis labels appear only on the strip, not inside
       each cell).

   The chrome-spec captures the resolved chrome geometry for the
   composite as a whole; the layout map (path -> rect) is kept as a
   first-class field on the CompositeDraft so downstream stages do
   not need to recompute layout from the original pose tree."
  [composite]
  (pose/report-unread-positions! composite :composites)
  (let [;; The draft's choices written in first -- a default mapping, a
        ;; concrete layer type for every inferred layer -- so sharing,
        ;; layout and the leaf drafts read layer types rather than
        ;; predicting them.
        composite (-> composite
                      pose/fit-layers
                      pose/elaborate
                      pose/infer-chrome)
        {:keys [width height leaves layout shared? shared-aesthetics shared-categories chrome]}
        (resolve-composite-chrome composite)
        suppress-keys (mapv aesthetic->suppress-key shared-aesthetics)
        ;; When the composite carries its own title/subtitle/caption,
        ;; suppress the same keys on each leaf so they don't double-
        ;; render. The composite title sits in the title-band-h strip
        ;; above the grid; per-leaf titles inside each cell would be
        ;; redundant with the outer chrome.
        composite-chrome-suppress (filterv #(get-in composite [:opts %])
                                           [:title :subtitle :caption])
        blank-notes (atom [])
        sub-drafts (binding [pose/*blank-notes* blank-notes]
                     (mapv (fn [leaf]
                             (let [rect (get layout (:path leaf))
                                   [_ _ rw rh] rect
                                   leaf' (dissoc leaf ::share)
                                   leaf-opts (cond-> (assoc (or (:opts leaf') {})
                                                            :width (max 1 (long-or rw 1))
                                                            :height (max 1 (long-or rh 1)))
                                               (seq suppress-keys)
                                               (as-> $ (reduce #(assoc %1 %2 true)
                                                               $ suppress-keys))
                                               (seq composite-chrome-suppress)
                                               (as-> $ (reduce #(dissoc %1 %2)
                                                               $ composite-chrome-suppress)))
                                 ;; The shared categories are in one order
                                 ;; for every cell, so each cell lists and
                                 ;; draws its own in that order rather than
                                 ;; in the order of its own rows -- the key
                                 ;; `pose/leaf->draft` writes for the
                                 ;; panels of a facet.
                                   order-keys (cond-> {}
                                                (seq (:color shared-categories))
                                                (assoc :__color-order (:color shared-categories))
                                                (seq (:shape shared-categories))
                                                (assoc :__shape-order (:shape shared-categories)))
                                   draft (cond->> (pose/leaf->draft leaf')
                                           (seq order-keys) (mapv #(merge % order-keys)))]
                               (cond-> {:path (:path leaf)
                                        :rect rect
                                        :draft draft
                                        :opts leaf-opts}
                                 (::share leaf) (assoc :share (::share leaf)))))
                           leaves))
        ;; Once, and only where the whole arrangement draws nothing.
        _ (when (every? (comp empty? :draft) sub-drafts)
            (run! println (distinct (deref blank-notes))))
        chrome-spec (-> chrome
                        (assoc :y-ticks-inferred-off (set (:plotje/y-ticks-inferred-off composite)))
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
             :align-direction (align-direction composite)))))

(defn- cell-too-small
  "The error for a cell of an arrangement too small to draw its panels,
   from the error its own plan raised (`layout/too-small-error`).

   A cell's width and height are its share of the arrangement's, so the
   cell's own message -- \"The plot's height, 40 ... Increase :height\"
   -- names a size nobody wrote and an option that is not read on a
   cell. This names the arrangement's size, how many rows or columns of
   cells divide it, and where the size is written."
  [e {:keys [rect path]} sub-drafts width height]
  (let [d (ex-data e)
        across? (contains? d :panel-width)
        {:keys [overhead breakdown]} (:too-small d)
        [_ _ rw rh] rect
        r #(Math/round (double %))
        ;; Rows and columns of cells, where the arrangement is one grid:
        ;; a row of cells, or rows of cells.
        grid? (every? #(<= (count (:path %)) 2) sub-drafts)
        n (when grid?
            (if across?
              (reduce max 1 (vals (frequencies (map #(r (second (:rect %))) sub-drafts))))
              (count (distinct (map #(r (second (:rect %))) sub-drafts)))))
        dim (if across? "width" "height")
        total (if across? width height)
        cell (if across? rw rh)]
    (ex-info
     (str "A cell of this arrangement is " (r cell) (if across? " wide" " high")
          ", which leaves no room for its panel: the arrangement's " dim ", " (r total)
          ", is divided among "
          (if n
            (str n (if across?
                     (if (= 1 n) " column" " columns")
                     (if (= 1 n) " row" " rows"))
                 " of cells")
            "its cells")
          ", and the cell's labels and legend take " (r overhead) " (" breakdown
          "). An arrangement keeps its " dim " however many cells it holds."
          " Give the arrangement more " dim ", as in (pj/arrange cells {:" dim " ...})"
          " or (pj/options arrangement {:" dim " ...})"
          (if across?
            ", or fewer cells in a row with a smaller :cols."
            ", or fewer rows with a larger :cols."))
     (assoc d :caller "pj/arrange" :path path :cell-rect rect
            :width width :height height)
     e)))

(defn- keep-ticks-beside-a-legend
  "The paths of the cells that get their y tick labels back: a cell
   whose tick labels were dropped because a cell at its left shares its
   y axis (`pose/infer-axis-chrome`, `inferred-off`), where the nearest
   cell at its left in the row draws a legend of its own. A cell's own
   legend is drawn at the right of its panel, so it lies between the two
   panels, and the tick labels at the far side of the legend are not
   beside this panel to be read.

   Asked here and not where the tick labels are dropped, because
   whether a cell draws a legend is known once it is planned
   (`:legend-w` of its layout): a layer's default colour, and a literal
   colour that names no column, are both decided past the pose."
  [inferred-off planned]
  (let [same? (fn [a b] (< (Math/abs (- (double a) (double b))) 1e-6))
        left? (fn [[x y _ h] [x2 y2 _ h2]]
                (and (< x2 x) (same? y y2) (same? h h2)))]
    (set (for [{:keys [path rect]} planned
               :when (contains? inferred-off path)
               :let [beside (filter #(left? rect (:rect %)) planned)]
               :when (seq beside)
               :let [nearest (apply max-key #(double (first (:rect %))) beside)]
               :when (pos? (double (or (get-in nearest [:plan :layout :legend-w]) 0.0)))]
           path))))

(defn composite-draft->plan
  "Convert a CompositeDraft into a CompositePlan. Per sub-draft, this
   calls draft->plan to produce a leaf plan and wraps it with its rect
   and path in :sub-plots. The shared-legend spec is computed once
   from a representative leaf draft (eliminating the rep-leaf-plan
   N+1 issue from the round-2 internals review)."
  [composite-draft]
  (let [{:keys [width height sub-drafts chrome-spec layout]} composite-draft
        ;; The cells sharing an axis are measured and pooled first, and
        ;; every later pass plans a cell with its pooled extent.
        shared (shared-extents sub-drafts)
        sub-drafts (mapv #(update % :opts merge (get shared (:path %))) sub-drafts)
        ;; `:opts` rides along so the render stage can read what the
        ;; cell asked for. Plan-stage options already reach the cell
        ;; through `draft->plan` here; render-stage ones -- `:theme`
        ;; above all -- are resolved where the drawables are made, and
        ;; the composite used to hand every cell its own options there,
        ;; so a per-cell `{:theme {:bg ...}}` was accepted and dropped.
        plan-sub (fn [{:keys [path rect draft opts] :as sub}]
                   {:path path
                    :rect rect
                    :opts opts
                    :plan (try
                            (plan/draft->plan draft opts)
                            (catch clojure.lang.ExceptionInfo e
                              ;; A size the cell pins itself is the
                              ;; cell's own, and its message stands.
                              (throw (if (and (:too-small (ex-data e))
                                              (not (:facet (ex-data e)))
                                              (not (or (:panel-width opts) (:panel-height opts))))
                                       (cell-too-small e sub sub-drafts width height)
                                       e))))})
        ;; Each cell's unread-option warnings are collected rather
        ;; than printed, and a line is printed only where every cell
        ;; raised it -- see `plan/*unread-option-warnings*`. The
        ;; second, aligning pass would raise them all again, so its
        ;; are dropped.
        warnings (atom [])
        first-pass (binding [plan/*unread-option-warnings* warnings]
                     (mapv plan-sub sub-drafts))
        ;; A cell beside a legend keeps its y tick labels, and is
        ;; planned again with them.
        keep-ticks (keep-ticks-beside-a-legend (:y-ticks-inferred-off chrome-spec) first-pass)
        sub-drafts (mapv #(cond-> % (keep-ticks (:path %)) (update :opts dissoc :suppress-y-ticks))
                         sub-drafts)
        first-pass (if (seq keep-ticks)
                     (binding [plan/*unread-option-warnings* (atom [])]
                       (mapv (fn [sub plot] (if (keep-ticks (:path sub)) (plan-sub sub) plot))
                             sub-drafts first-pass))
                     first-pass)
        direction (:align-direction composite-draft)
        ;; In an aligned grid, the cells of one grid column share their
        ;; left edge, so each reserves the widest y-label pad of its
        ;; column, whether it draws its own tick labels or not. A column
        ;; whose cells all hide them keeps a pad of zero; one whose
        ;; cells draw them in some rows only -- a scatter plot matrix,
        ;; ticked where each scale is first drawn -- lines up. The grid
        ;; columns are then resized so the drawing areas are equal: a
        ;; column is as wide as the common drawing width plus its own
        ;; pad, as a facet's margin sits outside its panels. Only where
        ;; nothing else a cell reserves differs between the columns.
        {:keys [sub-drafts layout column-pad]}
        (let [grid-columns (when (and (:align-panels composite-draft) (= direction :grid))
                             (->> first-pass
                                  (group-by (fn [{[x _ w _] :rect}] [(Math/round (double x)) (Math/round (double w))]))
                                  (sort-by ffirst)
                                  vec))
              pad-of (fn [p] (double (or (get-in p [:plan :layout :y-label-pad]) 0.0)))
              legend-ws (set (map #(Math/round (double (or (get-in % [:plan :layout :legend-w]) 0.0))) first-pass))]
          (if (and (next grid-columns) (= 1 (count legend-ws)))
            (let [pads (mapv (fn [[_ ps]] (reduce max 0.0 (map pad-of ps))) grid-columns)
                  xs (mapv (comp first first) grid-columns)
                  ws (mapv (comp second first) grid-columns)
                  gaps (mapv (fn [i] (- (nth xs (inc i)) (+ (nth xs i) (nth ws i)))) (range (dec (count xs))))
                  drawing (/ (- (reduce + ws) (reduce + pads)) (count ws))
                  new-ws (mapv #(Math/round (double (+ drawing %))) pads)
                  ;; Rounding is settled on the last column, so the grid
                  ;; keeps its width.
                  new-ws (update new-ws (dec (count new-ws)) + (- (reduce + ws) (reduce + new-ws)))
                  new-xs (reductions + (first xs) (map + new-ws gaps))
                  resize (into {} (map-indexed (fn [i [k _]] [k [(nth new-xs i) (nth new-ws i)]])) grid-columns)
                  column-pad (into {} (mapcat (fn [[_ ps] pad] (map (fn [p] [(:path p) pad]) ps)) grid-columns pads))
                  moved (fn [[x y w h]]
                          (if-let [[nx nw] (get resize [(Math/round (double x)) (Math/round (double w))])]
                            [nx y nw h]
                            [x y w h]))
                  sub-drafts (mapv (fn [sub]
                                     (let [[_ _ nw _ :as r] (moved (:rect sub))]
                                       (-> sub
                                           (assoc :rect r)
                                           (assoc-in [:opts :width] (max 1 (long nw))))))
                                   sub-drafts)]
              {:sub-drafts sub-drafts
               :layout (into {} (map (fn [[k r]] [k (moved r)])) layout)
               :column-pad column-pad})
            {:sub-drafts sub-drafts :layout layout :column-pad nil}))
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
        align-floors (cond-> {}
                       (not= direction :horizontal)
                       (assoc :min-y-label-pad (widest :y-label-pad)
                              :min-legend-w (widest :legend-w))
                       (not= direction :vertical)
                       (assoc :min-x-label-pad (widest :x-label-pad)))
        ;; Elsewhere a cell that draws no :y tick labels takes no floor
        ;; for them: its pad stays zero, so its drawing area keeps the
        ;; width of its neighbours that draw none either.
        floors-for (fn [sub]
                     (cond
                       column-pad
                       (assoc align-floors :min-y-label-pad (get column-pad (:path sub) 0.0))

                       (get-in sub [:opts :suppress-y-ticks])
                       (dissoc align-floors :min-y-label-pad)

                       :else align-floors))
        sub-plots (if (:align-panels composite-draft)
                    (binding [plan/*unread-option-warnings* (atom [])]
                      (mapv (fn [sub]
                              (plan-sub (update sub :opts merge (floors-for sub))))
                            sub-drafts))
                    first-pass)
        sub-plots (settle-shared sub-drafts sub-plots
                                 (fn [sub]
                                   (binding [plan/*unread-option-warnings* (atom [])]
                                     (plan-sub (cond-> sub
                                                 (:align-panels composite-draft)
                                                 (update :opts #(merge (floors-for sub) %)))))))
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
                                                      ;; Gathered cell by cell, then put in
                                                      ;; the order of the union: an entry's
                                                      ;; label is the display form of its
                                                      ;; category.
                                                      (plan/order-categories-by
                                                       :label
                                                       (:out (reduce (fn [{:keys [seen] :as acc} e]
                                                                       (if (seen (:label e))
                                                                         acc
                                                                         (-> acc
                                                                             (update :seen conj (:label e))
                                                                             (update :out conj e))))
                                                                     {:seen #{} :out []}
                                                                     (mapcat :entries all)))
                                                       (map defaults/fmt-category-label (get cats aes))))))
                                      legends)))]
                            (reduce merge-entries (select-keys rep-plan shared-keys) shared-keys))))
        _ (when (seq (deref warnings))
            (run! println (sort (reduce clojure.set/intersection (deref warnings)))))
        ;; The titles a composite draws once are the ones a cell would
        ;; draw: planned from one cell with its own titles left in, so a
        ;; written `:x-label` is read by the one rule that reads it.
        axis-titles (when-let [axes (seq (:axis-title-axes chrome-spec))]
                      (when-let [sub (first sub-drafts)]
                        (let [p (binding [plan/*unread-option-warnings* (atom [])]
                                  ;; Planned at the cell's own size, so a
                                  ;; cell too narrow for its titles is
                                  ;; reported here although the titles
                                  ;; are drawn beside the grid: fifteen
                                  ;; cells in a row of 600 fit without
                                  ;; them, on panels a few units wide.
                                  (try
                                    (plan/draft->plan (:draft sub)
                                                      (dissoc (:opts sub) :suppress-x-label
                                                              :suppress-y-label))
                                    (catch clojure.lang.ExceptionInfo e
                                      (throw (if (:too-small (ex-data e))
                                               (cell-too-small e sub sub-drafts width height)
                                               e)))))]
                          (into {} (keep (fn [k]
                                           (when-let [t (get p (if (= k :x) :x-label :y-label))]
                                             [k t])))
                                axes))))
        chrome (-> chrome-spec
                   (dissoc :shared?)
                   (assoc :shared-legend shared-legend)
                   (assoc :axis-titles axis-titles)
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

