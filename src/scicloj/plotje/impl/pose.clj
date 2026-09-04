(ns scicloj.plotje.impl.pose
  "Pose substrate -- the recursive plain-map type that is the
   library's spec vocabulary. This namespace holds the pure tree
   operations (resolve, layout, shared-scale injection) and the
   leaf->draft emitter that feeds plan.clj.

   Shape of a pose:
     {:data         ?  dataset (inherited from ancestor if absent)
      :mapping      ?  aesthetic mappings (merges with ancestors)
      :layers       ?  layers at this level (accumulate into leaves)
      :poses       ?  sub-poses; absence = leaf
      :layout       ?  {:direction :horizontal|:vertical
                        :weights   [pos-num ...]}
      :opts         ?  plot options (inheritable)
      :share-scales ?  #{:x :y}  for composites}"
  (:require [clojure.string :as str]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [scicloj.plotje.impl.aesthetics :as aes]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.layer-type :as layer-type]))

;; ---- Structural predicates ----

(defn pose?
  "True if x looks pose-shaped: a map carrying at least one of
   :layers or :poses. Permissive by design -- schema-level validation
   lives in impl.pose-schema."
  [x]
  (and (map? x)
       (or (contains? x :layers)
           (contains? x :poses))))

(defn leaf?
  "A leaf pose has no sub-poses. (An empty :poses vector also
   counts as leaf because it has nothing to tile.)"
  [f]
  (not (seq (:poses f))))

(defn composite?
  "A composite pose has at least one sub-pose."
  [f]
  (not (leaf? f)))

;; ---- Tree resolver ----

(def source-keys
  "The three ways an explicit mapping can name its source.

   `:column` reads the value from the layer's data and `:value` is the
   value itself; `:from` is the plain reading spelled out -- ask the
   data, and take whichever answer it gives, exactly as a mapping
   written plainly does. `:from` is what lets a plain mapping carry a
   `:scale`, since a bare `:size :weight` has no room for one."
  #{:column :value :from})

(defn mapping-source
  "What a mapping value names, with the full form unwrapped: the
   `:column`, the `:value` or the `:from`. A mapping that names only a
   scale names no source, and answers nil.

   Identity is decided on this rather than on the written value, so
   that `{:x {:from :a :scale ...}}` and a plain `{:x :a}` are the same
   position -- which they are, since `pj/scale` produces the first from
   the second."
  [v]
  (if (map? v)
    (cond
      (contains? v :column) (:column v)
      (contains? v :value)  (:value v)
      (contains? v :from)   (:from v)
      :else                 nil)
    v))

(defn- as-scale-spec
  "A scale statement as a spec map, or nil where it states no spec.
   `true` and `false` say which side of the scale a value passes and
   name no scale, so neither is one."
  [s]
  (cond
    (map? s)     s
    (keyword? s) {:type s}
    :else        nil))

(defn combine-scales
  "Combine an outer scale statement with an inner one.

   Scale settings accumulate down the scope chain: where both name a
   spec the two merge key by key, so a pose that sets a range and a
   layer that names a type give a plot with both. The innermost wins
   per key.

   `true` and `false` are not specs and do not merge. `false` says the
   value passes through no scale at all, so it replaces whatever was
   set above; `true` says it does pass through one without saying
   which, so it leaves an outer spec standing."
  [outer inner]
  (cond
    (false? inner) false
    (nil? inner)   outer
    (true? inner)  (if (some? outer) outer true)
    :else          (let [o (as-scale-spec outer)
                         i (as-scale-spec inner)]
                     (if o (merge o i) i))))

(defn- merge-mapping-value
  "Combine an outer mapping value for `k` with the inner one that
   overrides it.

   The source is replaced, because a mapping states one source and two
   cannot combine -- a merged `{:column :n :value 7}` is refused by
   name. The scale accumulates, because it is a set of independent
   settings, the same one `pj/scale` writes.

   Where the inner value is written plainly it has no room for a scale,
   so it is rewritten in the full form under `:from`, which is the
   plain reading spelled out: ask the data.

   An inner value that names only a scale replaces no source, because
   it names none: it says how to read whatever is named further out,
   so the outer source is carried through. This is the shape
   `pj/scale` writes, so a scale set on a layer or a composite cell
   would otherwise delete the mapping its own pose made."
  [k outer inner]
  (let [scale-of #(when (map? %) (:scale %))
        outer-scale (scale-of outer)
        inner-scale (scale-of inner)
        combined (combine-scales outer-scale inner-scale)
        inner-source-key (when (map? inner)
                           (some #(when (contains? inner %) %) source-keys))
        outer-source-key (when (map? outer)
                           (some #(when (contains? outer %) %) source-keys))]
    ;; A narrower `:scale false` wins, as a narrower anything does --
    ;; but it leaves the scale set above it doing nothing, and that is
    ;; worth a word.
    (when (and (false? inner-scale) (map? (as-scale-spec outer-scale)))
      (println (str "Warning: " k " is drawn as it stands here (:scale false),"
                    " and a scale is set for it further out: "
                    (pr-str (as-scale-spec outer-scale))
                    ". The nearer setting wins, so nothing reads the scale.")))
    (cond
      (nil? inner)      nil            ; an explicit nil cancels the mapping

      ;; The inner names a scale and no source. Keep the source from
      ;; further out, in the spelling it was written in, and let the
      ;; scale accumulate onto it.
      ;;
      ;; `:scale` has to be present for this to be a mapping written in
      ;; full at all: this function merges every key of a mapping map,
      ;; option keys included, and a plain option map such as a label's
      ;; `{:corner-radius 8}` is also a map naming no source.
      (and (map? inner) (contains? inner :scale)
           (nil? inner-source-key) (some? outer-source-key))
      (cond-> (assoc inner outer-source-key (get outer outer-source-key))
        (some? combined) (assoc :scale combined))

      (and (map? inner) (contains? inner :scale)
           (nil? inner-source-key) (some? outer) (not (map? outer)))
      (cond-> (assoc inner :from outer)
        (some? combined) (assoc :scale combined))

      (nil? combined)   inner
      (map? inner)      (assoc inner :scale combined)
      :else             {:from inner :scale combined})))

(defn merge-mappings
  "Merge an outer mapping into an inner one, the way pose mappings have
   always merged -- the inner value wins -- except for the scale, which
   accumulates. See `merge-mapping-value`."
  [outer inner]
  (reduce (fn [acc [k v]]
            (assoc acc k (if (contains? acc k)
                           (merge-mapping-value k (get acc k) v)
                           v)))
          (or outer {})
          (or inner {})))

(defn put-scale
  "Write a scale spec into `mapping` for `aesthetic` -- what `pj/scale`
   does.

   A scale lives with the mapping it reads, so this is an update of the
   mapping rather than of the pose's options. Where the aesthetic is
   mapped plainly there is no room for a scale, so the value is
   rewritten in the full form under `:from`, which says the same thing:
   ask the data. Where it is not mapped here at all, the scale is
   written on its own and applies to whatever source is named below.

   A mapping cancelled with `nil` stays cancelled: an aesthetic that
   draws nothing has nothing to scale. A mapping that says `:scale
   false` on this same pose is refused rather than overridden: one pose
   cannot both draw a value as it stands and read it through a scale."
  [mapping aesthetic spec]
  (let [mapping (or mapping {})
        existing (get mapping aesthetic)]
    (when (and (map? existing) (false? (:scale existing)))
      (throw (ex-info (str "pj/scale " aesthetic " " (pr-str spec) " sets a"
                           " scale, and " aesthetic " is mapped on this pose"
                           " as " (pr-str existing) ", which passes through"
                           " no scale. Drop the :scale false to scale the"
                           " mapping, or drop the pj/scale call to draw it as"
                           " it stands.")
                      {:aesthetic aesthetic :mapping existing :spec spec})))
    (cond
      (and (contains? mapping aesthetic) (nil? existing)) mapping
      (map? existing) (assoc mapping aesthetic
                             (assoc existing :scale
                                    (combine-scales (:scale existing) spec)))
      (contains? mapping aesthetic) (assoc mapping aesthetic
                                           {:from existing :scale spec})
      :else (assoc mapping aesthetic {:scale spec}))))

(defn resolve-tree
  "Walk the pose tree top-down, merging parent context into each
   descendant. Returns a vector of resolved leaves; each leaf carries
   merged :data, :mapping, :layers, :opts, and a :path vector of
   indices describing its position in the tree.

   Context inheritance rules:
   - :data     -- nearest ancestor wins (child overrides parent).
   - :mapping  -- merged, with child keys overriding parent keys, and
                  scale settings accumulating (see `merge-mappings`).
   - :layers   -- concatenated (ancestor layers distribute down, then
                  the leaf's own layers append).
   - :opts     -- merged (child overrides on key collision).

   Extra keys on a leaf (anything not in
   #{:data :mapping :layers :poses :layout :opts :share-scales})
   pass through to the resolved leaf so callers can attach metadata
   like :path-labels from facet-style generators."
  ([pose]
   (resolve-tree pose {} []))
  ([pose parent-ctx path]
   (let [ctx {:data    (or (:data pose) (:data parent-ctx))
              :mapping (merge-mappings (:mapping parent-ctx) (:mapping pose))
              :layers  (into (vec (:layers parent-ctx))
                             (:layers pose))
              :opts    (merge {} (:opts parent-ctx) (:opts pose))}]
     (if (leaf? pose)
       (let [structural-keys #{:data :mapping :layers :poses :layout
                               :opts :share-scales}
             extras (into {} (remove (fn [[k _]] (structural-keys k))
                                     pose))]
         [(merge extras (assoc ctx :path path))])
       (into []
             (mapcat (fn [[i child]]
                       (resolve-tree child ctx (conj path i))))
             (map vector (range) (:poses pose)))))))

;; ---- Layout computer ----

(defn- normalize-weights
  "Convert weights to fractions summing to 1."
  [weights]
  (let [total (double (reduce + weights))]
    (when (or (zero? total) (neg? total))
      (throw (ex-info "Layout :weights must sum to a positive number."
                      {:weights (vec weights)})))
    (mapv #(/ (double %) total) weights)))

(def ^:private no-x-key
  "Sentinel row/col key for leaves that have no :x mapping (univariate
   on y) or no :y mapping (univariate on x). They get their own grid
   row/column distinct from any data column."
  ::no-x)

(def ^:private no-y-key
  ::no-y)

(defn matrix-axes
  "For a composite whose layout is `:matrix`, walk its leaves in
   DFS order and compute the grid axes:

   - col-key per leaf: the leaf's :x mapping. Two leaves sharing
     (x, y) keep the same col-key.
   - row-key per leaf: the leaf's :y mapping, with a DFS-occurrence
     discriminator when (x, y) repeats. The first (a, b) gets row
     b; the second (a, b) gets row [b 1]; the third [b 2]; etc.
     Same column, new row in DFS order.
   - col-keys / row-keys: distinct keys in order of first appearance.
   - col-labels / row-labels: human-readable strings via
     defaults/fmt-name; nil when only one column or one row exists
     so we don't render a redundant strip header.

   Univariate leaves (missing :x or :y) use the no-x-key / no-y-key
   sentinels so they get their own grid lane.

   Returns {:col-keys [...] :row-keys [...]
            :col-labels [...|nil] :row-labels [...|nil]
            :positions {path -> [col-idx row-idx]}
            :x-vars [...] :y-vars [...]}.

   The compositor consumes :positions for rect math and the labels
   for strip rendering; :x-vars / :y-vars surface in plan introspection."
  [composite]
  (let [resolved   (resolve-tree composite)
        leaves     (filterv :path resolved)
        path+xy    (mapv (fn [leaf]
                           (let [m (:mapping leaf)]
                             [(:path leaf)
                              (or (:x m) no-x-key)
                              (or (:y m) no-y-key)]))
                         leaves)
        ;; DFS-occurrence index for each (x, y) pair.
        xy-counts  (volatile! {})
        annotated  (mapv (fn [[path x y]]
                           (let [counts  (vswap! xy-counts update [x y] (fnil inc 0))
                                 sub     (dec (get counts [x y]))
                                 row-key (if (zero? sub) y [y sub])]
                             {:path path :col-key x :row-key row-key
                              :x x :y y}))
                         path+xy)
        col-keys   (vec (distinct (map :col-key annotated)))
        row-keys   (vec (distinct (map :row-key annotated)))
        col-idx    (zipmap col-keys (range))
        row-idx    (zipmap row-keys (range))
        positions  (into {} (map (fn [{:keys [path col-key row-key]}]
                                   [path [(col-idx col-key) (row-idx row-key)]])
                                 annotated))
        ;; Strip labels: use the data-column name for non-sentinel keys;
        ;; nil for sentinels and for single-axis grids.
        label-of   (fn [k]
                     (cond
                       (= k no-x-key) ""
                       (= k no-y-key) ""
                       (vector? k)    (defaults/fmt-name (first k))
                       :else          (defaults/fmt-name k)))
        col-labels (when (> (count col-keys) 1) (mapv label-of col-keys))
        row-labels (when (> (count row-keys) 1) (mapv label-of row-keys))
        x-vars     (vec (distinct (keep #(when-not (= % no-x-key) %)
                                        (map :x annotated))))
        y-vars     (vec (distinct (keep #(when-not (= % no-y-key) %)
                                        (map :y annotated))))]
    {:col-keys   col-keys
     :row-keys   row-keys
     :col-labels col-labels
     :row-labels row-labels
     :positions  positions
     :x-vars     x-vars
     :y-vars     y-vars}))

(defn- compute-matrix-layout
  "Place each leaf in its (col, row) cell of an n-cols x n-rows grid.
   Empty cells get no entry in the returned map. The grid takes the
   full rect; cells are equal-sized (matrix layouts don't honour
   :weights -- columns and rows are determined by the data, not by
   user-supplied proportions)."
  [composite [x y w h]]
  (let [{:keys [col-keys row-keys positions]} (matrix-axes composite)
        n-cols (max 1 (count col-keys))
        n-rows (max 1 (count row-keys))
        cw     (/ (double w) n-cols)
        rh     (/ (double h) n-rows)]
    (into {}
          (map (fn [[path [ci ri]]]
                 [path [(+ (double x) (* ci cw))
                        (+ (double y) (* ri rh))
                        cw
                        rh]]))
          positions)))

(defn compute-layout
  "Walk the pose tree and assign a pixel rectangle to each leaf.
   Returns a map of path -> [x y w h].

   Composite :layout is {:direction :horizontal|:vertical|:matrix
                         :weights   [pos-num ...]}. Defaults:
     :direction :horizontal
     :weights   (repeat n 1)  (equal share)

   Matrix layout (`:direction :matrix`) places leaves on a grid
   derived from their :x / :y mappings -- distinct x-cols become
   grid columns, distinct y-cols become grid rows, leaves land at
   their (x, y) intersection cell. Duplicate (x, y) pairs stack
   into new rows in DFS order. Empty cells get no entry. See
   `matrix-axes` for the full algorithm and the corresponding
   strip-label derivation. :weights are ignored under :matrix.

   Rectangle arithmetic is in doubles; callers that need integer
   pixels should coerce at the render boundary (see pj/plot's
   width/height coercion)."
  ([pose rect]
   (compute-layout pose rect []))
  ([pose [x y w h] path]
   (cond
     (leaf? pose)
     {path [(double x) (double y) (double w) (double h)]}

     (= :matrix (:direction (:layout pose)))
     ;; Matrix layout: rect math handled by compute-matrix-layout, but
     ;; we still need to recurse into any nested composites that aren't
     ;; themselves matrix. For now we only support flat matrix
     ;; composites (children are leaves), since that's all
     ;; extend-or-promote / multi-pair-pose produce. Nested matrix-
     ;; in-matrix is reserved for a future iteration.
     (let [cell-rects (compute-matrix-layout pose [x y w h])]
       (into {}
             (map (fn [[child-i [cx cy cw ch]]]
                    (let [child (nth (:poses pose) child-i)
                          full-path (into path [child-i])]
                      [full-path [cx cy cw ch]])))
             ;; cell-rects keys are paths within the composite (single
             ;; integer for direct children); convert to absolute paths.
             (map (fn [[child-path rect]]
                    [(first child-path) rect])
                  cell-rects)))

     :else
     (let [{:keys [direction weights] :or {direction :horizontal}}
           (:layout pose)
           children (:poses pose)
           n (count children)
           ws (normalize-weights (or weights (repeat n 1)))
           horizontal? (= direction :horizontal)]
       (loop [i 0
              cursor (double (if horizontal? x y))
              acc {}]
         (if (>= i n)
           acc
           (let [child (nth children i)
                 frac (nth ws i)
                 child-span (if horizontal? (* (double w) frac) (* (double h) frac))
                 child-rect (if horizontal?
                              [cursor y child-span h]
                              [x cursor w child-span])
                 child-path (conj path i)
                 sub (compute-layout child child-rect child-path)]
             (recur (inc i)
                    (+ cursor child-span)
                    (merge acc sub)))))))))

;; ---- Tree utilities ----

(defn last-leaf-path
  "Return the path vector of the last leaf visited in left-to-right
   depth-first order. Nil if the pose is itself a leaf with no path
   context (the caller is the root leaf)."
  [pose]
  (if (leaf? pose)
    []
    (let [n (count (:poses pose))]
      (when (pos? n)
        (into [(dec n)] (last-leaf-path (peek (:poses pose))))))))

(defn leaf-at
  "Fetch the leaf at `path` in `pose`. Returns nil if the path does
   not land on a leaf."
  [pose path]
  (let [node (reduce (fn [f i] (get-in f [:poses i])) pose path)]
    (when (leaf? node) node)))

(defn path->update-in-path
  "Translate a leaf path like [0 1] into the get-in / update-in navigation
   [:poses 0 :poses 1]. A root path [] translates to []."
  [path]
  (into [] (mapcat (fn [i] [:poses i])) path))

(defn last-matching-leaf-path
  "Walk `pose` in left-to-right DFS order. Return the `:path` of the
   last leaf whose effective `:x` and `:y` (after ancestor-merge of
   `:mapping`) match `position-mapping`. Matching is strict equality:
   `:x` and `\"x\"` are different column references. Returns `nil` if
   no leaf matches.

   `position-mapping` may carry either or both of `:x` and `:y`; a
   `nil` value matches a leaf whose effective mapping has no entry
   for that axis. Matching is against resolved positional mappings
   only -- a bare leaf (no `:x`/`:y`) matches a bare position
   mapping.

   What a mapping names decides the match, not how it is written: a
   position carrying a scale names the same position as the plain
   form, and a mapping that names only a scale names no position at
   all."
  [pose position-mapping]
  (let [px (mapping-source (:x position-mapping))
        py (mapping-source (:y position-mapping))]
    (->> (resolve-tree pose)
         (keep (fn [leaf]
                 (when (and (= (mapping-source (get-in leaf [:mapping :x])) px)
                            (= (mapping-source (get-in leaf [:mapping :y])) py))
                   (:path leaf))))
         last)))

;; ---- Shared-scale injection ----
;;
;; The load-bearing primitive surfaced by the nested-poses PoC.
;;
;; When a composite declares :share-scales #{:x} (or :y, or both), we
;; compute a shared domain across descendants and stamp it onto each
;; leaf's :opts as :x-scale-domain / :y-scale-domain. The compositor
;; later reads these keys and forces the matching scale.
;;
;; Column bucketing: sharing is scoped to leaves whose effective
;; column for the axis matches. Leaves with different columns get
;; their own bucket, independent of the composite's share-scales set.
;; This gives the right behavior for SPLOM (columns align across rows,
;; rows align across columns), marginal plots (x shares between
;; scatter and top density; right density uses its own column), and
;; mosaic-of-scatters.

(defn- mapping-scale-type
  "The scale type a mapping value names, or nil where it names none.

   Reads the mapping as written, before normalizing: an explicit
   mapping's `:scale` can be a type keyword, a spec map holding
   `:type`, or a boolean, which names no type."
  [mapping-value]
  (when (map? mapping-value)
    (let [s (:scale mapping-value)]
      (cond
        (map? s)     (:type s)
        (keyword? s) s
        :else        nil))))

(defn- effective-axis-col
  "The column ref this resolved leaf uses for `axis`. Layer-level
   mappings take precedence over the leaf's own :mapping. Layers
   that would disagree with the leaf's position are redirected
   upstream by lay-on-pose (Pose Rule LP2: distinct positional
   aesthetics mean distinct poses, so the non-matching layer lands
   on a separate sub-pose), so by the time this function runs, all
   layer mappings either match the leaf's column or are absent."
  [leaf axis]
  (or (some (fn [layer] (mapping-source (get-in layer [:mapping axis])))
            (:layers leaf))
      (mapping-source (get-in leaf [:mapping axis]))))

(def ^:private stat-driven-y-stats
  "Stats whose y-axis output is a count or density rather than a
   function of the y-mapped data column. Leaves whose every layer
   resolves to one of these stats should not have a shared
   y-scale-domain stamped on them -- their y axis is independent
   of the column that other cells in the same y-bucket share.

   :bin     1D histogram (count per numeric bin)
   :count   categorical count per category
   :density 1D KDE (density per numeric value)

   :bin2d / :density-2d are NOT included: those are 2D heatmaps
   whose count/density goes to the fill aesthetic, leaving y as a
   data axis that participates in sharing normally."
  #{:bin :count :density})

(defn- predicted-stat
  "Predict the stat that plan/draft->plan will assign to this layer
   when emitted via leaf->draft. Mirrors the precedence used by
   resolve/resolve-draft-layer and leaf->draft:
     - explicit :stat                 -> use it
     - :layer-type registered + :stat -> stat from registry
     - :layer-type registered + :mark -> :identity
     - explicit :mark, no :stat       -> :identity
     - none of the above              -> infer via resolve/infer-layer-type
   Returns nil when prediction is impossible (no data, no x/y)."
  [layer leaf-mapping leaf-data]
  (let [lt-key (:layer-type layer)
        lt-info (when (and lt-key (keyword? lt-key) (not= :infer lt-key))
                  (layer-type/lookup lt-key))]
    (cond
      (:stat layer)         (:stat layer)
      (:stat lt-info)       (:stat lt-info)
      (or (:mark layer)
          (:mark lt-info))  :identity
      :else
      (let [v (merge leaf-mapping (:mapping layer))]
        (when (and leaf-data (or (:x v) (:y v)))
          (try
            (let [{:keys [x-type y-type x-temporal? y-temporal?]}
                  (resolve/infer-column-types leaf-data v)]
              (:stat (resolve/infer-layer-type v x-type y-type
                                               x-temporal? y-temporal?)))
            (catch Throwable _ nil)))))))

(defn- effective-layers
  "The layers that render for a leaf. When the user
   provided layers, those. When :layers is empty but :mapping is
   non-empty, leaf->draft synthesizes one :infer placeholder, so
   we model that as a single empty {} layer."
  [layers mapping]
  (cond
    (seq layers)  layers
    (seq mapping) [{}]
    :else         []))

(defn- y-axis-stat-driven?
  "True if every effective layer of this leaf will resolve to a stat
   whose y-axis is independent of the y-mapped data column. Such a
   leaf should not be stamped with a shared y-scale-domain."
  [layers mapping data]
  (let [effective (effective-layers layers mapping)]
    (and (seq effective)
         (every? (fn [layer]
                   (contains? stat-driven-y-stats
                              (predicted-stat layer mapping data)))
                 effective))))

(defn- col-values
  "Non-nil values for a column reference from a dataset. The reference
   must match a column name literally (a keyword does not match a
   string column with the same characters and vice versa)."
  [ds col-ref]
  (when (and ds col-ref)
    (when (contains? (set (tc/column-names ds)) col-ref)
      (remove nil? (ds col-ref)))))

(defn- numeric-domain
  "[lo hi] across the numeric values in a sequence, or nil if none.

   A temporal value counts as numeric here, read as the epoch
   milliseconds an axis holds it in. That is the same reading
   `axis-domain` gives a `:domain` written in dates, so a shared extent
   and a written one arrive at the plan in the same units."
  [vals]
  (let [nums (keep (fn [v]
                     (cond
                       (number? v) v
                       (resolve/temporal-value? v) (resolve/temporal->epoch-ms v)
                       :else nil))
                   vals)]
    (when (seq nums)
      [(dfn/reduce-min nums) (dfn/reduce-max nums)])))

(defn- union-domain
  "Merge two [lo hi] pairs into a single enclosing [lo hi]. Nil-safe."
  [a b]
  (cond
    (nil? a) b
    (nil? b) a
    :else [(min (first a) (first b))
           (max (second a) (second b))]))

(defn- leaf-share-key
  "Compatibility descriptor for a leaf when sharing scales on `axis`.
   Two leaves whose effective-axis-col agree must produce the same
   descriptor to be sharable; mismatches signal that the user wrote
   :share-scales across cells that would mean different things on the
   target axis (mixed coord, mixed numeric/categorical/temporal,
   mixed linear/log).

   Layers whose y axis is stat-driven (count/density on a histogram /
   KDE / count layer) are not target-axis comparable for y-sharing;
   the existing :y-axis-stat-driven? path skips the y-stamp entirely
   for those leaves, so they are also exempt here."
  [leaf axis]
  (let [coord (or (get-in leaf [:opts :coord]) :cartesian)
        ;; The mapping is where a scale lives, whether it was written
        ;; there or set with `pj/scale`.
        scale-type (or (mapping-scale-type (get-in leaf [:mapping axis]))
                       :linear)
        col (effective-axis-col leaf axis)
        ds (:data leaf)
        type-temporal (when (and ds col)
                        (try
                          (let [{:keys [x-type y-type x-temporal? y-temporal?]}
                                (resolve/infer-column-types ds {axis col})]
                            (case axis
                              :x [x-type x-temporal?]
                              :y [y-type y-temporal?]))
                          (catch Throwable _ nil)))]
    {:coord coord :scale-type scale-type :type-temporal type-temporal}))

(defn- describe-share-conflict
  "Build a human-readable list of differing fields across a set of
   share-keys, for use in the error message."
  [keys-set]
  (let [fields [:coord :scale-type :type-temporal]]
    (->> fields
         (keep (fn [field]
                 (let [vs (set (map field keys-set))]
                   (when (> (count vs) 1)
                     (str (name field) " " (vec vs))))))
         (str/join "; "))))

(defn- validate-share-bucket-compatibility!
  "For each (axis, col) bucket under share-scales, refuse when leaves
   in the same bucket have incompatible coord / scale-type / inferred
   type. Stat-driven y-axis leaves (count/density) are exempt on
   axis :y -- they would skip the stamp downstream anyway. Throws an
   ex-info naming the conflict."
  [subtree axes]
  (doseq [axis axes
          [col leaves-in-bucket] (group-by #(effective-axis-col % axis) subtree)
          :when col
          :let [filtered (if (= axis :y)
                           (remove (fn [l]
                                     (y-axis-stat-driven? (:layers l)
                                                          (:mapping l)
                                                          (:data l)))
                                   leaves-in-bucket)
                           leaves-in-bucket)
                keys-set (set (map #(leaf-share-key % axis) filtered))]
          :when (> (count keys-set) 1)]
    (throw (ex-info
            (str ":share-scales " axis " refused: column "
                 (pr-str col)
                 " has incompatible scale meaning across cells ("
                 (describe-share-conflict keys-set)
                 "). Cells targeting :share-scales must agree on"
                 " coord (e.g. cartesian vs flip), inferred column"
                 " type (numerical / categorical / temporal), and"
                 " scale type (linear / log).")
            {:caller "share-scales"
             :axis axis
             :column col
             :share-keys keys-set}))))

(defn- warn-share-scales-overrides-free!
  "Warn when a sub-pose carries :scales :free / :free-x / :free-y
   on an axis that the composite is also trying to share. The share
   wins; the sub-pose's directive is silently dropped today."
  [subtree axes]
  (doseq [axis axes
          leaf subtree
          :let [s (get-in leaf [:opts :scales])]
          :when (or (= s :free)
                    (and (= axis :x) (= s :free-x))
                    (and (= axis :y) (= s :free-y)))]
    (println (str "Warning: composite :share-scales " axis
                  " overrides a sub-pose's :scales " s
                  ". The share wins; the sub-pose's :scales setting"
                  " is ignored. Drop one of the two to remove the"
                  " conflict."))))

(defn- assert-share-bucket-has-extent!
  "Refuse :share-scales on a bucket whose data column has no extent to
   pool -- a categorical one.
   A category has no extent to pool, so the silent path would produce
   no shared domain and the cells would quietly disagree.

   A temporal column is shared: an axis holds dates as epoch
   milliseconds, so the union is defined, and `numeric-domain` reads
   them that way.

   A bucket of one cell is left alone: sharing is between cells, and a
   cell that shares with nobody is stamped with its own extent, which
   is what it would have had anyway. Refusing those reported an axis
   the writer never asked to share -- `pj/marginal` sets
   `:share-scales` itself, so a right marginal on a numeric column was
   refused because the *other* axis held dates, and the message named a
   setting the writer had not written."
  [subtree axes]
  (doseq [axis axes
          [col leaves-in-bucket] (group-by #(effective-axis-col % axis) subtree)
          :when col
          :let [filtered (if (= axis :y)
                           (remove (fn [l]
                                     (y-axis-stat-driven? (:layers l)
                                                          (:mapping l)
                                                          (:data l)))
                                   leaves-in-bucket)
                           leaves-in-bucket)
                vals (mapcat #(col-values (:data %)
                                          (effective-axis-col % axis))
                             filtered)]
          :when (and (< 1 (count filtered)) (seq vals)
                     (not-any? #(or (number? %) (resolve/temporal-value? %)) vals))]
    (throw (ex-info
            (str ":share-scales " axis " refused: column "
                 (pr-str col)
                 " is categorical across all sharing cells, so a"
                 " union domain is not defined. Drop :share-scales"
                 " on this axis, or share scales only on numerical"
                 " or temporal columns.")
            {:caller "share-scales"
             :axis axis
             :column col}))))

(defn inject-shared-scales
  "Walk a pose tree. For each composite with :share-scales, compute a
   union domain per (axis, effective-column) bucket across descendant
   leaves, and stamp those domains onto matching leaves' :opts as
   :x-scale-domain / :y-scale-domain. Returns a new tree.

   :share-scales may live in `(:opts pose)` (the canonical location;
   set via pj/options or pj/arrange) or directly at the top of the
   pose (legacy location for hand-built composites). The :opts entry
   wins if both are present.

   `inherited-domains` carries `{axis {col-ref [lo hi]}}` down the
   tree. `inherited-mapping` carries the ancestor-merged mapping so a
   leaf can resolve its effective axis column from (inherited + own +
   layer) when deciding which bucket to claim. `inherited-data` is
   the nearest-ancestor dataset, threaded through so a leaf can
   predict whether its layers' y axis is stat-driven (count/density)
   and skip the shared y-domain stamp on such leaves -- e.g., the
   diagonal histogram cells of a SPLOM."
  ([pose]
   (inject-shared-scales pose {} {} nil))
  ([pose inherited-domains inherited-mapping inherited-data]
   (let [my-mapping  (merge inherited-mapping (:mapping pose))
         my-data     (or (:data pose) inherited-data)
         my-shares   (or (get-in pose [:opts :share-scales])
                         (:share-scales pose))
         new-domains (when (and my-shares (seq (:poses pose)))
                       (let [subtree (resolve-tree pose
                                                   {:mapping inherited-mapping
                                                    :data inherited-data}
                                                   [])]
                         (validate-share-bucket-compatibility! subtree my-shares)
                         (assert-share-bucket-has-extent! subtree my-shares)
                         (warn-share-scales-overrides-free! subtree my-shares)
                         (into {}
                               (keep
                                (fn [axis]
                                  (let [by-col (group-by #(effective-axis-col % axis)
                                                         subtree)
                                        col->dom (into {}
                                                       (keep
                                                        (fn [[col leaves]]
                                                          (when col
                                                            (when-let [d (numeric-domain
                                                                          (mapcat #(col-values (:data %)
                                                                                               (effective-axis-col % axis))
                                                                                  leaves))]
                                                              [col d])))
                                                        by-col))]
                                    (when (seq col->dom)
                                      [axis col->dom])))
                                my-shares))))
         child-domains (merge-with (partial merge-with union-domain)
                                   inherited-domains
                                   new-domains)]
     (if (leaf? pose)
       (if (seq child-domains)
         (let [pose-ctx {:mapping my-mapping :layers (:layers pose)}
               x-col (effective-axis-col pose-ctx :x)
               y-col (effective-axis-col pose-ctx :y)
               x-dom (get-in child-domains [:x x-col])
               y-dom (get-in child-domains [:y y-col])
               ;; Drop the y-domain when this leaf's y-axis is
               ;; stat-driven (count/density) -- the shared-data
               ;; bucket value would clip the bars / curve.
               y-dom (when-not (y-axis-stat-driven? (:layers pose)
                                                    my-mapping
                                                    my-data)
                       y-dom)]
           (if (or x-dom y-dom)
             (update pose :opts merge
                     (cond-> {}
                       x-dom (assoc :x-scale-domain x-dom)
                       y-dom (assoc :y-scale-domain y-dom)))
             pose))
         pose)
       (update pose :poses
               (fn [children]
                 (mapv #(inject-shared-scales % child-domains my-mapping my-data)
                       children)))))))

;; ---- Leaf-to-draft ----
;;
;; The draft emitter. Consumes one resolved leaf and produces a draft
;; vector -- the same shape plan/draft->plan accepts.

(defn- coerce-dataset
  "Coerce raw data to a Tablecloth dataset. Returns nil for nil."
  [d]
  (when d
    (if (tc/dataset? d) d (tc/dataset d))))

(defn- resolve-layer-type-info
  "Look up layer-type info from a layer's :layer-type key.
   Keyword -> registry lookup (throws on unknown). Map -> pass through.
   :infer -> sentinel.

   A layer type's `:defaults` come along as ordinary layer options, which
   is how two layer types sharing one mark differ -- `:label` is `:text`
   with `{:box true}`. The caller's own options are merged over this map
   (see the resolved layer below), so an explicit `{:box false}` wins."
  [layer-type-key]
  (cond
    (= :infer layer-type-key)
    {:mark :infer}

    (keyword? layer-type-key)
    (let [m (layer-type/lookup layer-type-key)]
      (if m
        (-> (select-keys m [:mark :stat :position :x-only])
            (merge (:defaults m))
            (assoc :layer-type layer-type-key))
        (let [registered (sort (keys (layer-type/registered)))]
          (throw (ex-info (str "Unknown layer type: " layer-type-key
                               ". Use pj/lay-* with a registered layer type, or "
                               "(pj/layer-type-lookup ...) to inspect. Registered layer types: "
                               (vec registered))
                          {:layer-type layer-type-key :registered registered})))))

    :else
    layer-type-key))

(defn- heterogeneous-types
  "If the column has :object dtype and the first 100 values have more
   than one distinct (clojure.core/type), return a sorted list of
   those type names. Otherwise nil."
  [col]
  (when (and col (= :object (dtype/elemwise-datatype col)))
    (let [sample (take 100 col)
          types (->> sample
                     (remove nil?)
                     (map type)
                     distinct)]
      (when (> (count types) 1)
        (sort (map #(.getSimpleName ^Class %) types))))))

(defn- mapping-origin
  "Return :layer, :layer-type, or :pose to indicate where the
   effective value of `k` in the resolved mapping originated.

   Not to be confused with `mapping-source`, which answers what a
   mapping names rather than where it was written."
  [k layer-mapping layer-type-info]
  (cond
    (contains? layer-mapping k)   :layer
    (contains? layer-type-info k) :layer-type
    :else                          :pose))

(def ^:private drawn-value-nouns
  "What a written value on each aesthetic is, in one word. Only the
   aesthetics that accept one appear -- the registry's `:value?` is
   what says so, and naming a reading an aesthetic does not have would
   send the reader after a fix that is not there."
  {:color "a color"
   :shape "a symbol"
   :size  "a radius"
   :alpha "an opacity"
   :text  "a label"
   :tooltip "hover text"})

(defn- drawn-value-description
  "What each aesthetic accepts as a written value, spelled out. Stated
   once, for the two messages that have to name it: the value that
   names no column, and the value the writer marked `:value` out loud."
  [k]
  (case k
    :color "a color is a CSS name, or a hex string written with its #"
    :shape (str "one layer's shape is one of "
                (str/join ", " (map pr-str defaults/shape-syms)))
    :size  "a fixed size is a positive number"
    :alpha "a fixed alpha is a number within 0 and 1"
    :text  "a label is a string"
    :tooltip "hover text is a string"
    nil))

(defn- also-not-drawable-sentence
  "Extra guidance when the failing value was plausibly meant as
   something to draw rather than a column, naming what the aesthetic
   would have accepted.

   Not offered for a keyword on `:color`. Every aesthetic reads a
   keyword as a column name, and a keyword that names a color is drawn
   rather than reaching here -- so a keyword at this point is a
   mistyped column name, and the column message is the whole of the
   answer. A string is the case worth catching: it is how a hex code
   missing its `#` arrives."
  [k col]
  (when (and (:value? (defaults/aesthetic-registry k))
             (or (not= :color k) (string? col)))
    (when-let [noun (drawn-value-nouns k)]
      (str " It is not " noun " either -- " (drawn-value-description k) "."))))

(defn- not-drawable-message
  "The message for a value the writer marked `:value` out loud that the
   aesthetic cannot draw.

   It names no columns on purpose. `{:value :sp}` says the value is not
   a column reference, so listing the columns available answers a
   question the writer has already declined to ask -- and reads as a
   contradiction when the dataset does carry a column of that name."
  [k col]
  (str k " " (pr-str {:value col}) " is not "
       (or (drawn-value-nouns k) (str "one " k " can draw"))
       (when-let [d (drawn-value-description k)] (str " -- " d))
       "."))

(defn- column-not-found-message
  "Build a focused error message for a missing column reference.
   When the layer carries its own :data and the failing key was
   inherited from the pose, surface that asymmetry and offer two
   fixes (`pj/overlay` to keep it on the panel, or override on
   the layer to create a separate sub-pose).

   `explicit-column?` marks a reference the writer wrote as
   `{:column ...}`. The drawn-value sentence is dropped there: they
   have said which reading they meant, so the other one is not the
   answer."
  [k col col-names {:keys [layer-type-key layer-own-data? source explicit-column?]}]
  (let [layer-name (if (keyword? layer-type-key) (name layer-type-key) "*")]
    (case (when (and layer-own-data? (= :pose source)) :pose-inherited)
      :pose-inherited
      (str "lay-" layer-name " " k " " (pr-str col)
           ", inherited from the pose's mapping, names a column"
           " absent from this layer's :data. Available columns: "
           (vec (sort-by str col-names))
           ". To draw this layer on the existing panel, write"
           " pj/overlay before it, or {:overlay true} in its own"
           " options map. To draw it on a separate sub-pose, set "
           k " on the layer call to a column that exists in :data.")
      ;; Default: simple "not found" with the available columns
      (str "Column " col " (from " k ") not found in dataset."
           " Available: " (sort-by str col-names)
           (when-not explicit-column?
             (also-not-drawable-sentence k col))))))

(def ^:private column-only-accepts
  "What to tell the user each column-only aesthetic takes. The set
   itself comes from `defaults/aesthetic-registry` -- these are the
   aesthetics whose entry has a `:column?` and no `:value?`.

   A value on one of them used to pass every check in silence and then
   do nothing, because `resolve/resolve-aesthetics` classifies
   `:color`, `:size`, `:alpha` and `:text` only. `{:shape 4}` drew
   default circles and `{:group 4}` drew nothing at all -- the layer
   resolved to zero groups, so a plot came back with a layer missing
   and no word about it.

   `:shape` has since gained that reading, which is what took it off
   this list. `:fill` is to gain one: its registry entry already
   carries the `:by-source` scale default, and flipping `:value?` is
   what would turn this error into that reading -- **necessary and not
   sufficient.** The tile extractor derives its color through a
   gradient and has no branch for a value that already is one, which is
   why `:drawn-column?` is false there too, so flipping the flag alone
   would put back the silent nothing this gate was added to remove.
   `dev-notes/backlog.md` lists what else the path needs. `:group`
   keeps it -- it splits the data and draws nothing of its own, so
   there is nothing a value could mean."
  {:group "one column, or a vector of columns"})

(defn- column-only-accepts-str [k]
  (get column-only-accepts k "one column"))

(defn- validate-column-only-aesthetics
  "Throw when an aesthetic that reads a column and nothing else --
   `:fill` or `:group`, which is `defaults/column-only-aesthetics`,
   derived from the registry's `:column?` and `:value?` -- carries a
   value that names no column."
  [resolved d]
  (when d
    (doseq [k defaults/column-only-aesthetics
            :let [v (get resolved k)]
            :when (some? v)]
      (when-not (or (resolve/column-ref? v)
                    (and (= k :group)
                         (sequential? v)
                         (every? resolve/column-ref? v)))
        (throw (ex-info (str k " takes " (column-only-accepts-str k)
                             ", but got " (pr-str v)
                             ". A column reference is a keyword or a string"
                             " naming a column of the layer's data;"
                             " " k " has no reading for a value.")
                        {:option k :value v}))))))

(def ^:private channel-type-key
  "The `-type` override that goes with an aesthetic, where there is
   one. Not in the registry, because two of the three name an axis
   rather than an aesthetic's own reading."
  {:color :color-type :x :x-type :y :y-type})

(def ^:private scale-reading-keys
  "The scale spec keys that say how a value is read through the scale,
   as opposed to how the result is worded. A `:scale false` contradicts
   these -- the aesthetic passes through no scale for them to
   configure. It does not contradict an axis title or its tick text,
   which are drawn either way."
  #{:range :values :by :from-zero :midpoint})

(defn- validate-unscaled-channel-options
  "Throw when a channel told not to scale also carries an option that
   configures the scale it has just left.

   `{:color {:column :hex :scale false} :color-type :categorical}` and
   `(pj/scale pose :color {:domain [...]})` both rendered identically
   to the option being absent: a column drawn as it stands passes
   through no scale, so there is nothing for either to configure. That
   follows from the convention rather than contradicting it -- but a
   silently ignored option is the defect this release exists to
   remove, and the writer has asked for two things that cannot both
   hold. `:scale true` is the one word that makes both take effect.

   The scale itself is no longer among the options: a `:scale false`
   written below a scale replaces it, which is the ordinary rule for a
   setting written in a narrower scope. What is left here are the
   settings that do not accumulate -- the column-type overrides, and
   the plot options that are the outer scope of a scale setting, such
   as `:color-range`."
  [resolved opts]
  (doseq [[k {:keys [scale-key]}] defaults/aesthetic-registry
          :when (and scale-key (false? (get-in resolved [:__scale k])))
          :let [type-key (channel-type-key k)
                named (cond-> (vec (for [sk (sort (get defaults/channel-scale-options k #{}))
                                         :when (contains? scale-reading-keys sk)
                                         :let [option (defaults/scale-option-key k sk)]
                                         :when (contains? opts option)]
                                     (str option)))
                        (and type-key (get resolved type-key))
                        (conj (str type-key " " (pr-str (get resolved type-key)))))]
          :when (seq named)]
    (let [one? (= 1 (count named))]
      (throw (ex-info (str k " was given :scale false, so it passes through no"
                           " scale, and " (str/join " and " named)
                           (if one? " configures" " configure")
                           " the scale it just left -- nothing reads "
                           (if one? "it" "them") ". Drop the :scale false to"
                           " configure the scale, write :scale true to do both,"
                           " or drop " (if one? "the option" "the options")
                           " to draw the column as it stands.")
                      {:key k :options named})))))

(defn- drawn-at-this-gate?
  "Whether a value naming no column is one this gate should let past:
   the aesthetic accepts a written value at all, and the value is one
   it can draw -- ask the layer's data, then ask what the aesthetic can
   draw.

   **Both questions.** `drawn-value-schemas` states the grammar for
   every aesthetic that has one, including the ones not accepting a
   written value yet, so it answers what a value *would* mean and not
   whether it is allowed. That permission is the registry's `:value?`,
   and consulting the grammar without it let `{:fill \"red\"}` past:
   `:fill` has a `Color` grammar and `:value? false`, so the tile drew
   the default blue in silence, where `main` reported the column it
   could not find. A value that happens to name a color was the only
   one affected, which is why nothing noticed.

   `:text` needs no exception here even though its written value is a
   label rather than a column. `resolve-positional-values` runs first
   and has already turned that string into a constant column, so what
   reaches this gate is an ordinary column reference."
  [k col]
  (and (:value? (defaults/aesthetic-registry k))
       (aes/drawable? k col)))

(defn- validate-columns
  "Validate that every aesthetic column reference in the resolved
   mapping names a real column in the dataset. Rejects heterogeneous
   object columns (mixed numbers/strings/keywords). Matching is
   strict: a keyword reference does not match a string column name
   and vice versa. The optional `ctx` map carries source-of-mapping
   information so the error can name whether the failing reference
   came from the pose or from the layer.

   The data decides which values are column references, for every
   aesthetic alike. What used to stand here was a type test plus a
   carve-out -- `column-ref?` was keyword-or-string, and `:color`
   strings were exempted so that literal colors survived. That exempted
   `\"notacolor\"` too, deferring it to a render-time `Unknown color`,
   and it exempted nothing on the other aesthetics, so `:color :red`
   was reported as a missing column. Asking what the aesthetic can draw
   answers both: a value that names no column and cannot be drawn is
   the mistake, whatever its type.

   Two kinds of value are policed. One that could plausibly be a column
   name -- a keyword or a string -- and one written for an aesthetic
   that accepts written values, whatever its type: `{:shape 4}` names
   no column and is no symbol either, and saying so beats drawing a
   default circle in silence. A value on an aesthetic that takes none
   is `validate-column-only-aesthetics`'s business, and it names what
   that aesthetic takes, which is the more useful message of the two.

   Honoring `{:column ...}` is not the same as believing it. The
   writer's choice settles which of the two readings applies; whether
   the column is there is still asked, and asked here."
  ([resolved d] (validate-columns resolved d nil))
  ([resolved d ctx]
   (when d
     (let [col-names (set (tc/column-names d))
           col-lookup #(get d %)
           {:keys [layer-mapping layer-type-info layer-type-key layer-own-data?]} ctx
           not-found! (fn [k col explicit-column?]
                        (let [source (mapping-origin k (or layer-mapping {}) (or layer-type-info {}))]
                          (throw (ex-info
                                  (column-not-found-message
                                   k col col-names
                                   {:layer-type-key layer-type-key
                                    :layer-own-data? layer-own-data?
                                    :source source
                                    :explicit-column? explicit-column?})
                                  {:key k :column col :available (sort-by str col-names)
                                   :source source}))))]
       (doseq [k defaults/column-keys
               :let [col (get resolved k)
                     said (get-in resolved [:__source k])]
               :when (and col
                          (not (sequential? col))
                          (or (resolve/column-ref? col)
                              (:value? (defaults/aesthetic-registry k))))]
         (if (= :column (aes/source col col-names said))
           (do
             ;; A written `{:column :typo}` reaches here as a column
             ;; reference whatever the data holds, because that is what
             ;; the writer said it was. Unchecked it is dropped without
             ;; a word: a mistyped `:color` drew the default grey and a
             ;; mistyped `:group` resolved to zero groups, so the layer
             ;; left the plot and nothing said why.
             (when-not (contains? col-names col)
               (not-found! k col true))
             (when-let [types (heterogeneous-types (col-lookup col))]
               (throw (ex-info (str "Column " col " (from " k ") has mixed value types: " (vec types)
                                    ". Convert it to a single type (number, string, etc.) before plotting.")
                               {:key k :column col :types types})))
             ;; Whether a column of this aesthetic can be drawn at all.
             ;; `:scale-default` says what the reading would mean and
             ;; `:drawn-column?` says whether it is written; asking
             ;; only the first let `{:shape {:column c :scale false}}`
             ;; through to `collect-shapes`, which assigned symbols by
             ;; category order regardless -- so a column holding
             ;; `:cross` drew a circle, under a legend labelling that
             ;; circle "cross".
             (when (and (false? (get-in resolved [:__scale k]))
                        (not (:drawn-column? (defaults/aesthetic-registry k))))
               (throw (ex-info (str k " " (pr-str col) " was given :scale false,"
                                    " and a " k " column drawn as it stands is"
                                    " not a reading Plotje has yet -- it would"
                                    " be ignored, and the column read through "
                                    k "'s scale as though the :scale were absent."
                                    " Drop the :scale to ask for that in so many"
                                    " words.")
                               {:key k :column col})))
             ;; What a column told not to scale has to hold, which
             ;; differs by what the aesthetic draws.
             (when (false? (get-in resolved [:__scale k]))
               (case (:scale-default (defaults/aesthetic-registry k))
                 ;; An axis measures in drawing units from the panel
                 ;; background's top left, so the column holds numbers.
                 ;; Unchecked it reached the renderer and died on
                 ;; `String cannot be cast to Number`, which is the
                 ;; error this work replaced for `:size` and `:alpha`.
                 :always
                 (let [ctype (resolve/column-type d col)]
                   (when-not (= :numerical ctype)
                     (throw (ex-info (str k " " (pr-str col) " was given :scale false,"
                                          " so its values are drawing units measured"
                                          " from the top left of the panel background,"
                                          " but " (pr-str col) " holds " (name ctype)
                                          " values. Drop the :scale to place the column"
                                          " through the axis instead.")
                                     {:key k :column col :column-type ctype}))))

                 ;; The values are drawn as they stand, so they have to
                 ;; be values the aesthetic can draw. On `:color`,
                 ;; `hex->rgba` reads a bare `a` as `#aaaaaa`, so a
                 ;; category column came out in near-identical greys
                 ;; with nothing said. On `:size`, a column holding a
                 ;; negative simply did not draw that mark -- the plan
                 ;; counted it, `svg-summary` counted it, and the
                 ;; picture was two points short. The written forms
                 ;; `{:size -4}` and `{:size {:value -4}}` were refused
                 ;; all along, so only the column skipped the
                 ;; constraint.
                 :by-source
                 (when-let [bad (->> (col-lookup col)
                                     (remove nil?)
                                     (remove #(aes/drawable? k %))
                                     seq)]
                   (throw (ex-info (str k " " (pr-str col) " was given :scale false,"
                                        " so its values are drawn as they stand,"
                                        " but " (pr-str (first bad)) " is not one "
                                        k " can draw."
                                        (also-not-drawable-sentence k (first bad))
                                        " Drop the :scale to let the column be"
                                        " read through " k "'s scale instead.")
                                   {:key k :column col :value (first bad)})))

                 nil)))

           (when-not (drawn-at-this-gate? k col)
             (cond
               ;; The writer wrote `:value` out loud on an aesthetic
               ;; that has no reading for one. Neither the missing
               ;; column nor the vocabulary is the answer -- what it
               ;; takes is.
               (and (= :value said)
                    (not (:value? (defaults/aesthetic-registry k))))
               (throw (ex-info (str k " " (pr-str {:value col}) " writes a value, and "
                                    k " takes " (column-only-accepts-str k)
                                    " -- it has no reading for a value.")
                               {:option k :value col}))

               (= :value said)
               (throw (ex-info (not-drawable-message k col)
                               {:key k :value col}))

               :else (not-found! k col false)))))))))

(def explicit-mapping-keys
  "The keys an explicit mapping map may carry: one source, and
   optionally the scale to read it through."
  (conj source-keys :scale))

(defn explicit-mapping?
  "True of a mapping value written in the explicit form -- a map naming
   its source, as `{:column :species}` or `{:value \"red\" :scale true}`,
   or naming only a scale, as `pj/scale` writes.

   A map is unambiguous here because no aesthetic takes one as a value:
   a color is a string or a keyword, a size is a number, a shape is a
   symbol from a fixed list."
  [v]
  (and (map? v)
       (boolean (some #(contains? v %) (conj source-keys :scale)))))

(defn scale-only-mapping?
  "True of a mapping value that names a scale and no source -- what
   `pj/scale` writes for an aesthetic this pose does not map. It says
   how to read whatever source is named elsewhere, and where none is,
   nothing is drawn and the scale is inert."
  [v]
  (and (map? v)
       (contains? v :scale)
       (not-any? #(contains? v %) source-keys)))

(defn check-explicit-mapping!
  "Throw when an explicit mapping is malformed. Nothing here reads the
   layer's data, which is what lets `api` run it at the `pj/pose` or
   `lay-*` call rather than at `pj/draft`: a mistyped key inside the
   map is a mistake about the form, and the form is fully visible
   where it is written. The checks that need the data -- whether the
   column is there, whether the value is one the aesthetic can draw --
   stay in `validate-columns`.

   Called both from the call site and from
   `normalize-explicit-mapping`, so a pose built by hand and threaded
   straight to `pj/draft` is held to the same rules."
  [k v]
  (let [unknown (remove explicit-mapping-keys (keys v))]
    (when (seq unknown)
      (throw (ex-info (str k " " (pr-str v) " has unexpected key(s): "
                           (vec unknown) ". An explicit mapping names"
                           " its source with :column or :value, and may"
                           " add :scale.")
                      {:key k :value v :unknown (vec unknown)})))
    (let [named (filterv #(contains? v %) [:column :value :from])]
      (when (> (count named) 1)
        (throw (ex-info (str k " " (pr-str v) " names " (count named)
                             " sources: " named ". It is one of them:"
                             " :column reads the value from the layer's"
                             " data, :value is the value itself, and :from"
                             " lets the data decide between the two.")
                        {:key k :value v :named named}))))
    ;; This is the "reported where the mapping is built" that
    ;; `impl.aesthetics/scaled?` defers to. An aesthetic with no scale
    ;; cannot be taken off one, and silently dropping the key would
    ;; leave a writer believing they had changed something.
    ;;
    ;; Two aesthetics have none, for different reasons, and both are
    ;; refused here: `nil` is `:group`, which draws nothing of its own,
    ;; and `:never` is `:text`, which draws a label as it stands. Only
    ;; `:group` was refused at first, so `{:text {:value "hi" :scale
    ;; true}}` was accepted and the key dropped -- the very thing this
    ;; check exists to prevent, under the same reasoning one entry over.
    (when (contains? v :scale)
      (let [{:keys [scale-default scale-key]} (defaults/aesthetic-registry k)]
        (when-let [why (cond
                         (nil? scale-default)
                         (str "It splits a layer into one drawn group per"
                              " value and draws nothing of its own, so"
                              " there is no scale for a value to pass"
                              " through.")

                         (= :never scale-default)
                         (str "A label is drawn as it stands, whether it"
                              " comes from a column or is written in the"
                              " mapping, so there is no scale for it to"
                              " pass through.")

                         ;; The secondary positional aesthetics -- a
                         ;; band's edges, an errorbar's bounds, an
                         ;; interval's far end -- are drawn through the
                         ;; panel's own axis and have no scale of their
                         ;; own. A `:scale` here was accepted and read
                         ;; by nothing.
                         (nil? scale-key)
                         (let [axis (if (= \x (first (name k))) :x :y)]
                           (str "It is drawn through the panel's " axis
                                " axis, which takes its scale from " axis
                                ". Write the :scale there, or set it for"
                                " the pose with (pj/scale pose " axis
                                " ...)."))

                         :else nil)]
          (throw (ex-info (str k " " (pr-str v) " sets :scale, and " k
                               " has no scale to set. " why)
                          {:key k :value v})))))
    ;; Beside `true` and `false`, `:scale` takes a scale type or a whole
    ;; spec, and reads *this mapping* through it: `{:size {:column :w
    ;; :scale :log}}` is one log-scaled size mapping, whatever the pose
    ;; says. `true` keeps meaning the aesthetic's default type -- it is
    ;; not an opinion about the scale, only about which side of it the
    ;; value passes.
    (let [s (:scale v)]
      (when-not (contains? #{true false nil} s)
        (when-not (or (keyword? s) (map? s))
          (throw (ex-info (str k " " (pr-str v) " sets :scale to " (pr-str s)
                               ". A mapping's :scale is true or false --"
                               " whether this value passes through " k "'s"
                               " scale -- or a scale type such as :log, or a"
                               " spec map such as {:type :log :range [2 12]}.")
                          {:key k :value v :scale s})))
        (let [spec (if (keyword? s) {:type s} s)
              valid-types (defaults/channel-scale-types k)]
          ;; Before the unknown-key check, so that a drawn-range option
          ;; written on an aesthetic that has no such quantity is
          ;; answered with what it means rather than with a list of keys.
          (scale/validate-spec-values! k spec "A mapping's :scale on")
          (scale/validate-drawn-range-options! k spec "A mapping's :scale on")
          (scale/validate-spec-keys! k spec "A mapping's :scale on")
          (when-let [t (:type spec)]
            (when-not (contains? valid-types t)
              (throw (ex-info (str k " " (pr-str v) " sets :scale type "
                                   (pr-str t) ", and " k " has no such scale."
                                   " Supported for " k ": "
                                   (vec (sort valid-types)) ".")
                              {:key k :value v :scale-type t
                               :supported (vec (sort valid-types))})))))))
    ;; A source named as nothing is not a source. Left through, a nil
    ;; `:value` broadcast a column of nils and drew an empty panel
    ;; reading "no data", and a nil `:column` reached `pj/plan` and died
    ;; on a schema error -- both because the checks below skip a nil.
    (when (empty? (select-keys v (conj source-keys :scale)))
      (throw (ex-info (str k " " (pr-str v) " names no source and no scale."
                           " A mapping written in full says which reading it"
                           " means, with :column, :value or :from; :scale says"
                           " which scale to read that source through.")
                      {:key k :value v})))
    (let [named (first (filter #(contains? v %) [:column :value :from]))]
      (when (and named (nil? (get v named)))
        (throw (ex-info (str k " " (pr-str v) " names " named " nil."
                             " To cancel a mapping inherited from an"
                             " outer scope, write " k " nil on its own.")
                        {:key k :value v :source named}))))))

(defn- normalize-explicit-mapping
  "Rewrite one explicit mapping into the plain value the rest of the
   pipeline already reads, and return `[value source scale]`.

   Normalizing rather than carrying the map onward is the same move
   `:x` makes when a value becomes a constant column: what the stat,
   the extract and every mark receive is the ordinary shape, and only
   what cannot be re-derived travels beside it. What cannot be
   re-derived is exactly the two things the writer said out loud --
   which source they meant, and which side of the scale.

   `:from` names no source of its own: it says to ask the data, which
   is what happens where no source was said at all. So it normalizes
   to a source of `nil` and the conventions decide, exactly as they do
   for a mapping written plainly."
  [k v]
  (check-explicit-mapping! k v)
  (cond
    (contains? v :column) [(:column v) :column (get v :scale)]
    (contains? v :value)  [(:value v) :value (get v :scale)]
    :else                 [(:from v) nil (get v :scale)]))

(defn- normalize-explicit-mappings
  "Rewrite every explicit mapping in `resolved` into its plain value,
   collecting what was said explicitly under two internal keys:
   `:__source` and `:__scale`, each a map from aesthetic to the choice.

   Both are absent for a mapping written plainly, which is what lets
   the conventions decide there. Absence and an explicit `nil` cannot
   be told apart by a lookup, which is why an explicit `:scale nil`
   means the same as writing no `:scale` at all: the convention
   decides. `false` is the way to say unscaled.

   A `:from` mapping names no source, so it records none and the
   conventions decide, as they do for a mapping written plainly. What
   it does carry is the accumulated scale -- `merge-mappings` rewrites
   a plain value that way when an outer scope set one."
  [resolved]
  (reduce (fn [acc [k v]]
            (if-not (explicit-mapping? v)
              acc
              (let [[value source scale] (normalize-explicit-mapping k v)]
                (cond-> (if (scale-only-mapping? v)
                          ;; It names how to read a source, not one to
                          ;; read. Leaving a nil value here would cancel
                          ;; the aesthetic rather than leave it unmapped.
                          (dissoc acc k)
                          (assoc acc k value))
                  (some? source) (assoc-in [:__source k] source)
                  (some? scale)  (assoc-in [:__scale k] scale)
                  ;; An unscaled `:x` or `:y` is a distance across the
                  ;; panel rather than a value on the axis. The renderer
                  ;; needs to know per axis, because the two can differ:
                  ;; x read through its scale while y is a place.
                  (and (false? scale) (= :x k)) (assoc :x-drawn? true)
                  (and (false? scale) (= :y k)) (assoc :y-drawn? true)))))
          resolved
          (select-keys resolved defaults/column-keys)))

(defn- forget-explicit-source
  "Drop the recorded `:__source` for aesthetics whose written value has
   just become a column.

   `{:x {:value 2}}` says the 2 is a value and not the column named 2,
   and that is true of what the writer wrote. Once the value has been
   broadcast into a constant column the mapping names that column, so
   leaving the note in place would have the gate check a column
   reference against a rule for values and report a column it can see
   as missing. The choice has been honored; what is left is ordinary."
  [resolved ks]
  (let [remaining (apply dissoc (:__source resolved) ks)]
    (if (seq remaining)
      (assoc resolved :__source remaining)
      (dissoc resolved :__source))))

(defn- free-column-name
  "A name close to `k` that `taken` does not hold, for a synthesized
   constant column whose natural name the data has already used.

   Keeps the aesthetic's own name recognizable, because it is what the
   axis and the legend are titled with."
  [k taken]
  (let [base (if (keyword? k) (name k) (str k))]
    (first (remove taken (map #(keyword (str base "-" %)) (iterate inc 1))))))

(defn- resolve-positional-values
  "Rewrite an `:x` or `:y` given as a value into data the rest of the
   pipeline already handles, and return the new `[mapping data]` pair.

   `:color`, `:size` and `:alpha` accept a column or a value --
   `{:color :species}` beside `{:color \"red\"}`. `:x` and `:y` accept
   both too, and which of two shapes a value takes is decided here, where
   the merged mapping and the layer's data are both known:

   - **No positional aesthetic reads the data.** The layer does not
     describe the data at all -- it is an annotation -- so one row holding
     those values draws the single mark it asks for. `:text` is handled
     only here: a string normally names a column, and a layer with no data
     has no column for it to name, so there the string is the text.
   - **One of them is a column.** The layer describes the data, and each
     value broadcasts over it as a constant column, so `{:x 6.5 :y :weight}`
     labels every row at one x. The column is a `dtype/const-reader`, so a
     value costs one number however long the data is -- less than the
     `(tc/add-column data :x (constantly 6.5))` this replaces.

   Either way the synthesized column takes the aesthetic's own name, so a
   plot made only of values labels its axes \"x\" and \"y\" -- the names raw
   data without a mapping already gets. Reducing a value to data rather
   than carrying it as a second kind of coordinate means the stat, the
   extract, the domains and every mark receive the shape they handle."
  [resolved d layer-own-data?]
  (let [col-names (when d (set (tc/column-names d)))
        ;; A dataset built without column names is given integer ones, so
        ;; a number here can be either a column name or a value to draw
        ;; at. The data decides: a number naming a column reads it.
        ;; An explicit `{:column ...}` or `{:value ...}` has already
        ;; been unwrapped, leaving its choice under `:__source`. That is
        ;; the only way to settle a number on a dataset whose columns
        ;; carry integer names, where the shorthand is ambiguous and so
        ;; refused.
        said     (fn [k] (get-in resolved [:__source k]))
        column?  (fn [k v] (if-let [s (said k)]
                             (= :column s)
                             (or (resolve/column-ref? v) (contains? col-names v))))
        value?   (fn [k v] (if-let [s (said k)]
                             (= :value s)
                             (and (resolve/literal-position? v)
                                  (not (contains? col-names v)))))
        literals (into {} (for [k resolve/positional-aesthetics
                                :let [v (get resolved k)]
                                :when (value? k v)]
                            [k v]))
        ;; A string on `:text` naming no column is the label itself. It
        ;; broadcasts the same way a value on `:x` does, so every row is
        ;; labelled with it -- which is what `{:text "n = 150"}` beside a
        ;; column of x means. Handled here rather than downstream for the
        ;; same reason the positional values are: once it is a column,
        ;; the stat, the extract and the mark all receive the shape they
        ;; already handle.
        text-literal (let [t (:text resolved)]
                       (when (and (string? t) (not (contains? col-names t))) t))
        ;; A written value asked to go *through* its scale -- ggplot2's
        ;; constant inside `aes()`. One value repeated over every row is
        ;; a column of one distinct value, and a column is what the
        ;; scales, the domains and the legends already read. So the
        ;; datum reading needs no machinery of its own: broadcast it and
        ;; the rest falls out, legend entry included.
        ;; Only where the writer said so. The convention never reads a
        ;; written value as data on an appearance aesthetic: a value
        ;; that is neither a column nor something the aesthetic can
        ;; draw is a mistake far more often than a series label, and
        ;; inferring the datum would turn `{:color :speceis}` into a
        ;; legend entry rather than the report it should be. The
        ;; positional aesthetics do scale a value by convention, and
        ;; `literals` above is that rule -- stated through
        ;; `literal-position?`, which also refuses a value that could
        ;; not be data at all.
        scaled-values (into {} (for [k defaults/column-keys
                                     :let [scale (get-in resolved [:__scale k])
                                           v (get resolved k)]
                                     :when (and (some? v)
                                                (= :value (aes/source v col-names (said k)))
                                                (some? scale)
                                                (not (false? scale)))]
                                 [k v]))
        broadcast (cond-> (merge literals scaled-values)
                    (and text-literal d) (assoc :text text-literal))]
    (cond
      (empty? broadcast)
      [resolved d]

      (and (not layer-own-data?)
           (not-any? #(column? % (get resolved %))
                     resolve/positional-aesthetics))
      ;; The one-row dataset this branch synthesizes carries the scaled
      ;; values too. A datum is a column of one distinct value wherever
      ;; it is written, and leaving it out of the row left
      ;; `{:x 2 :y 3 :color {:value "Model A" :scale true}}` reporting a
      ;; column missing from a dataset the writer never wrote.
      (let [text   (:text resolved)
            values (cond-> (merge literals scaled-values)
                     (string? text) (assoc :text text))]
        [(-> resolved
             (merge (zipmap (keys values) (keys values)))
             (forget-explicit-source (keys values)))
         (coerce-dataset (into {} (for [[k v] values] [k [v]])))])

      ;; A column reference with no data anywhere resolves to nothing
      ;; downstream, as it does without a value beside it.
      (nil? d)
      [resolved d]

      :else
      ;; The synthesized column takes the aesthetic's own name where
      ;; that name is free, so a plot made only of values labels its
      ;; axes "x" and "y". Where the data already carries a column of
      ;; that name, taking it anyway *replaced* the data:
      ;; `(pj/lay-point :a :size {:size {:value 7 :scale true}})` drew
      ;; every mark at y=7 on an axis still labelled `size`. Mapping a
      ;; coordinate to a column named after an aesthetic is ordinary,
      ;; so the synthesized name steps aside instead.
      (let [names (into {} (for [k (keys broadcast)]
                             [k (if (contains? col-names k)
                                  (free-column-name k col-names)
                                  k)]))]
        [(-> resolved
             (merge names)
             (forget-explicit-source (keys broadcast)))
         (reduce (fn [ds [k v]]
                   (tc/add-column ds (names k) (dtype/const-reader v (tc/row-count ds))))
                 d
                 broadcast)]))))

(defn- resolve-facet-col
  "Resolve a facet column ref against a dataset; throw with a clear
   message if the column is missing."
  [ds role ref]
  (let [col-names (set (tc/column-names ds))
        fname (resolve/resolve-col-name ds ref)]
    (when-not (contains? col-names fname)
      (throw (ex-info (str "Facet column " ref " (from " role ") not found in dataset. Available: " (sort-by str col-names))
                      {:role role :column ref :available (sort-by str col-names)})))
    fname))

(defn- facet-variants
  "Build one (data + labels) variant per facet-value combination.
   Returns a vector of maps {:data ds-subset, :facet-col <label>?, :facet-row <label>?}.
   When neither axis is faceted, returns a single-element vector carrying the
   input dataset unchanged and no labels."
  [data facet-col facet-row]
  (when (and facet-col (sequential? facet-col))
    (throw (ex-info (str "Facet column must be a single keyword or string, got vector: " (pr-str facet-col)
                         ". For 2D grids use (pj/facet-grid pose col-col row-col).")
                    {:facet-col facet-col})))
  (when (and facet-row (sequential? facet-row))
    (throw (ex-info (str "Facet row must be a single keyword or string, got vector: " (pr-str facet-row)
                         ". For 2D grids use (pj/facet-grid pose col-col row-col).")
                    {:facet-row facet-row})))
  (let [nc? (resolve/column-ref? facet-col)
        nr? (resolve/column-ref? facet-row)
        ds  (coerce-dataset data)]
    (cond
      (not (or nc? nr?))
      [{:data ds}]

      (and nc? nr?)
      (let [fcol (resolve-facet-col ds :facet-col facet-col)
            frow (resolve-facet-col ds :facet-row facet-row)]
        (vec
         (for [cv (distinct (ds fcol)) rv (distinct (ds frow))]
           {:data (tc/select-rows ds (fn [r] (and (= (r fcol) cv) (= (r frow) rv))))
            :facet-col (defaults/fmt-category-label cv)
            :facet-row (defaults/fmt-category-label rv)})))

      nc?
      (let [fcol (resolve-facet-col ds :facet-col facet-col)]
        (vec
         (for [cv (distinct (ds fcol))]
           {:data (tc/select-rows ds (fn [r] (= (r fcol) cv)))
            :facet-col (defaults/fmt-category-label cv)})))

      nr?
      (let [frow (resolve-facet-col ds :facet-row facet-row)]
        (vec
         (for [rv (distinct (ds frow))]
           {:data (tc/select-rows ds (fn [r] (= (r frow) rv)))
            :facet-row (defaults/fmt-category-label rv)}))))))

(defn- mapping-scale-spec
  "The scale spec a mapping states, or nil where it states none.

   `true` and `false` are not opinions about the scale: they say which
   side of it a value passes, which is a different question from which
   scale it is. So `(pj/scale pose :size :log)` beside
   `{:size {:column :w :scale true}}` stays logarithmic, while
   `{:scale :linear}` there overrides it for that one mapping."
  [scale]
  (cond
    (map? scale)     scale
    (keyword? scale) {:type scale}
    :else            nil))

(defn- layer-scale-specs
  "The scale spec each channel is drawn through on this layer, keyed by
   the key the plan reads it from.

   Every scale comes from the mapping now -- `pj/scale` writes one
   there too -- so what arrives here has already accumulated down the
   scope chain, and there is nothing left to combine.

   The type is defaulted here rather than where a scale is written,
   because a spec that names no type is not an opinion that the scale
   is linear. Filling one in at the call would make the second of two
   `pj/scale` calls silently undo the type the first one set, and would
   leave a scale written as a mapping without a type at all."
  [resolved]
  (into {} (for [[k scale-key] defaults/channel->scale-key
                 :let [spec (mapping-scale-spec (get-in resolved [:__scale k]))]
                 :when (some? spec)]
             [scale-key (if (some? (:type spec))
                          spec
                          (assoc spec :type (defaults/default-scale-type k)))])))

(defn leaf->draft
  "Emit a draft vector from a leaf pose. A draft has one entry per
   applicable layer; each entry is a flat map carrying the merged
   aesthetic mapping (pose < layer-type-info < layer), the layer's
   :stat/:position/:mark as first-class siblings, each aesthetic's
   resolved scale spec under its own key, and plot-level :coord
   stamped from :opts. The scales come from the mapping, which is
   where `pj/scale` writes them; only :coord is still an option.

   If the leaf's :opts carry :facet-col or :facet-row, the draft is
   multiplied over distinct facet values. Each variant carries a
   filtered :data plus :facet-col / :facet-row labels that plan.clj
   detects to build the facet grid.

   The leaf's :opts is passed through to plan/draft->plan; in
   particular the compositor uses :suppress-legend on grid cells.

   An empty :layers vector yields one {:mark :infer ...} placeholder so
   downstream inference can still choose a layer type from the data.

   Data precedence: layer :data > leaf :data.

   Every emitted draft carries :__panel-idx 0 because a single leaf is
   a single panel; plan.clj uses the key to group layers by panel, and
   a leaf has no sub-panel structure."
  [leaf]
  (let [leaf-mapping (or (:mapping leaf) {})
        leaf-data    (:data leaf)
        opts         (or (:opts leaf) {})
        coord-type   (:coord opts)
        layers       (or (:layers leaf) [])
        ;; An entirely empty leaf (no mapping, no layers) has nothing
        ;; to infer from -- emit nothing so plan produces a minimal
        ;; placeholder instead of crashing on a mark-:infer with no x.
        applicable   (cond
                       (seq layers) layers
                       (seq leaf-mapping) [{:layer-type :infer}]
                       :else [])
        variants     (facet-variants leaf-data (:facet-col opts) (:facet-row opts))]
    (vec
     (for [[variant-idx variant] (map-indexed vector variants)
           layer applicable]
       (let [layer-type-info  (resolve-layer-type-info (:layer-type layer))
             layer-mapping    (or (:mapping layer) {})
             layer-structural (select-keys layer [:stat :position :mark])
             ;; Explicit mappings are unwrapped before anything reads
             ;; the merged map, so every check and every later stage
             ;; sees a plain value with the writer's two choices
             ;; recorded beside it.
             resolved (normalize-explicit-mappings
                       (-> leaf-mapping
                           (merge-mappings layer-type-info)
                           (merge-mappings layer-mapping)
                           (merge-mappings layer-structural)))
             layer-own-data?  (some? (:data layer))
             ;; An :x or :y given as a value becomes data before anything
             ;; else looks at the mapping, so every check and every later
             ;; stage sees the ordinary shape: columns named by a mapping.
             [resolved d] (resolve-positional-values
                           resolved
                           (coerce-dataset (or (:data layer) (:data variant)))
                           layer-own-data?)]
         (validate-columns resolved d
                           {:layer-mapping layer-mapping
                            :layer-type-info layer-type-info
                            :layer-type-key (:layer-type layer)
                            :layer-own-data? layer-own-data?})
         (validate-column-only-aesthetics resolved d)
         (validate-unscaled-channel-options resolved opts)
         (-> resolved
             (assoc :data d
                    :__panel-idx variant-idx)
             (merge (layer-scale-specs resolved))
             (cond->
              coord-type  (assoc :coord coord-type)
              (:facet-col variant) (assoc :facet-col (:facet-col variant))
              (:facet-row variant) (assoc :facet-row (:facet-row variant)))
             (cond-> (= :infer (:mark resolved))
               (-> (dissoc :mark :stat)))))))))
