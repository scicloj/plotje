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
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]
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

(defn resolve-tree
  "Walk the pose tree top-down, merging parent context into each
   descendant. Returns a vector of resolved leaves; each leaf carries
   merged :data, :mapping, :layers, :opts, and a :path vector of
   indices describing its position in the tree.

   Context inheritance rules:
   - :data     -- nearest ancestor wins (child overrides parent).
   - :mapping  -- merged, with child keys overriding parent keys.
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
              :mapping (merge {} (:mapping parent-ctx) (:mapping pose))
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
   mapping."
  [pose position-mapping]
  (let [px (:x position-mapping)
        py (:y position-mapping)]
    (->> (resolve-tree pose)
         (keep (fn [leaf]
                 (when (and (= (get-in leaf [:mapping :x]) px)
                            (= (get-in leaf [:mapping :y]) py))
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

(defn- effective-axis-col
  "The column ref this resolved leaf uses for `axis`. Layer-level
   mappings take precedence over the leaf's own :mapping. Layers
   that would disagree with the leaf's position are redirected
   upstream by lay-on-pose (Pose Rule LP2: distinct positional
   aesthetics mean distinct poses, so the non-matching layer lands
   on a separate sub-pose), so by the time this function runs, all
   layer mappings either match the leaf's column or are absent."
  [leaf axis]
  (or (some (fn [layer] (get-in layer [:mapping axis]))
            (:layers leaf))
      (get-in leaf [:mapping axis])))

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
  "[lo hi] across the numeric values in a sequence, or nil if none."
  [vals]
  (let [nums (filter number? vals)]
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
        scale-key (case axis :x :x-scale :y :y-scale)
        scale-type (or (get-in leaf [:opts scale-key :type]) :linear)
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

(defn- assert-share-bucket-numeric!
  "Refuse :share-scales on a bucket whose data column is non-numeric.
   Categorical / temporal sharing is deferred to post-alpha; today
   the silent path produces no shared domain."
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
          :when (and (seq filtered) (seq vals) (not-any? number? vals))]
    (throw (ex-info
            (str ":share-scales " axis " refused: column "
                 (pr-str col)
                 " is non-numeric across all sharing cells, so a"
                 " union domain is not defined. Drop :share-scales"
                 " on this axis, or share scales only on numeric"
                 " columns.")
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
                         (assert-share-bucket-numeric! subtree my-shares)
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

(defn- mapping-source
  "Return :layer, :layer-type, or :pose to indicate where the
   effective value of `k` in the resolved mapping originated."
  [k layer-mapping layer-type-info]
  (cond
    (contains? layer-mapping k)   :layer
    (contains? layer-type-info k) :layer-type
    :else                          :pose))

(defn- column-not-found-message
  "Build a focused error message for a missing column reference.
   When the layer carries its own :data and the failing key was
   inherited from the pose, surface that asymmetry and offer two
   fixes (rename to align with pose for overlay, or override on
   the layer to create a separate sub-pose)."
  [k col col-names {:keys [layer-type-key layer-own-data? source]}]
  (let [layer-name (if (keyword? layer-type-key) (name layer-type-key) "*")]
    (case (when (and layer-own-data? (= :pose source)) :pose-inherited)
      :pose-inherited
      (str "lay-" layer-name " " k " " (pr-str col)
           ", inherited from the pose's mapping, names a column"
           " absent from this layer's :data. Available columns: "
           (vec (sort-by str col-names))
           ". To overlay on the existing panel, rename the column"
           " in :data to " (pr-str col) ". To draw this layer on"
           " a separate sub-pose, set " k " on the layer call"
           " to a column that exists in :data.")
      ;; Default: simple "not found" with the available columns
      (str "Column " col " (from " k ") not found in dataset."
           " Available: " (sort-by str col-names)))))

(def ^:private column-only-accepts
  "What to tell the user each column-only aesthetic takes. The set
   itself comes from `defaults/aesthetic-registry` -- these are the
   aesthetics whose entry has a `:column?` and no `:literal`.

   A value on one of them used to pass every check in silence and then
   do nothing, because `resolve/resolve-aesthetics` classifies
   `:color`, `:size`, `:alpha` and `:text` only. `{:shape 4}` drew
   default circles and `{:group 4}` drew nothing at all -- the layer
   resolved to zero groups, so a plot came back with a layer missing
   and no word about it. Whether any of them should gain a reading for
   a value is an open design question; until it is answered, saying so
   beats drawing the wrong picture."
  {:group "one column, or a vector of columns"})

(defn- column-only-accepts-str [k]
  (get column-only-accepts k "one column"))

(defn- validate-column-only-aesthetics
  "Throw when `:shape`, `:fill` or `:group` carries a value that names
   no column."
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

(defn- validate-columns
  "Validate that every aesthetic column reference in the resolved
   mapping names a real column in the dataset. Rejects heterogeneous
   object columns (mixed numbers/strings/keywords). Matching is
   strict: a keyword reference does not match a string column name
   and vice versa. The optional `ctx` map carries source-of-mapping
   information so the error can name whether the failing reference
   came from the pose or from the layer."
  ([resolved d] (validate-columns resolved d nil))
  ([resolved d ctx]
   (when d
     (let [col-names (set (tc/column-names d))
           col-exists? col-names
           col-lookup #(get d %)
           {:keys [layer-mapping layer-type-info layer-type-key layer-own-data?]} ctx]
       (doseq [k defaults/column-keys
               :let [col (get resolved k)]
               :when (and col
                          (resolve/column-ref? col)
                          (not (and (= k :color) (string? col))))]
         (when-not (col-exists? col)
           (let [source (mapping-source k (or layer-mapping {}) (or layer-type-info {}))]
             (throw (ex-info
                     (column-not-found-message
                      k col col-names
                      {:layer-type-key layer-type-key
                       :layer-own-data? layer-own-data?
                       :source source})
                     {:key k :column col :available (sort-by str col-names)
                      :source source}))))
         (when-let [types (heterogeneous-types (col-lookup col))]
           (throw (ex-info (str "Column " col " (from " k ") has mixed value types: " (vec types)
                                ". Convert it to a single type (number, string, etc.) before plotting.")
                           {:key k :column col :types types}))))))))

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
        column?  (fn [v] (or (resolve/column-ref? v) (contains? col-names v)))
        value?   (fn [v] (and (resolve/literal-position? v)
                              (not (contains? col-names v))))
        literals (into {} (for [k resolve/positional-aesthetics
                                :let [v (get resolved k)]
                                :when (value? v)]
                            [k v]))]
    (cond
      (empty? literals)
      [resolved d]

      (and (not layer-own-data?)
           (not-any? #(column? (get resolved %))
                     resolve/positional-aesthetics))
      (let [text   (:text resolved)
            values (cond-> literals
                     (string? text) (assoc :text text))]
        [(merge resolved (zipmap (keys values) (keys values)))
         (coerce-dataset (into {} (for [[k v] values] [k [v]])))])

      ;; A column reference with no data anywhere resolves to nothing
      ;; downstream, as it does without a value beside it.
      (nil? d)
      [resolved d]

      :else
      [(merge resolved (zipmap (keys literals) (keys literals)))
       (reduce (fn [ds [k v]]
                 (tc/add-column ds k (dtype/const-reader v (tc/row-count ds))))
               d
               literals)])))

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

(defn leaf->draft
  "Emit a draft vector from a leaf pose. A draft has one entry per
   applicable layer; each entry is a flat map carrying the merged
   aesthetic mapping (pose < layer-type-info < layer), the layer's
   :stat/:position/:mark as first-class siblings, and plot-level
   :x-scale/:y-scale/:coord stamped from :opts.

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
        x-scale      (:x-scale opts)
        y-scale      (:y-scale opts)
        size-scale   (:size-scale opts)
        alpha-scale  (:alpha-scale opts)
        fill-scale   (:fill-scale opts)
        color-scale  (:color-scale opts)
        shape-scale  (:shape-scale opts)
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
             resolved (merge leaf-mapping
                             layer-type-info
                             layer-mapping
                             layer-structural)
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
         (-> resolved
             (assoc :data d
                    :__panel-idx variant-idx)
             (cond->
              x-scale     (assoc :x-scale x-scale)
              y-scale     (assoc :y-scale y-scale)
              size-scale  (assoc :size-scale size-scale)
              alpha-scale (assoc :alpha-scale alpha-scale)
              fill-scale  (assoc :fill-scale fill-scale)
              color-scale (assoc :color-scale color-scale)
              shape-scale (assoc :shape-scale shape-scale)
              coord-type  (assoc :coord coord-type)
              (:facet-col variant) (assoc :facet-col (:facet-col variant))
              (:facet-row variant) (assoc :facet-row (:facet-row variant)))
             (cond-> (= :infer (:mark resolved))
               (-> (dissoc :mark :stat)))))))))
