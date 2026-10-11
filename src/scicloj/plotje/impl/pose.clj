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
  (:require [clojure.set]
            [clojure.string :as str]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [scicloj.plotje.impl.aesthetics :as aes]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.impl.stat :as stat]
            [scicloj.plotje.layer-type :as layer-type]))

;; ---- Structural predicates ----

(defn pose?
  "True if x looks pose-shaped: a map carrying :layers, :poses, or a
   map-valued :mapping. Permissive by design -- schema-level
   validation lives in impl.pose-schema.

   A pose carrying only a mapping draws, a layer being inferred for
   it, and is completed later by `api/with-data`. Read as data instead
   -- which is what a map with none of these keys is -- it became a
   dataset whose columns were :mapping and :data, and drew that.

   The :mapping value is tested rather than the key, because a dataset
   written as a map of columns may hold a column called :mapping. Its
   value is then the column, a sequence, and not a mapping."
  [x]
  (and (map? x)
       (or (contains? x :layers)
           (contains? x :poses)
           (map? (:mapping x)))))

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
   - :opts     -- merged (child overrides on key collision). So a
                  `:share-scales` written on a composite reaches a
                  faceted leaf below that writes none, and its panels
                  share the same axes.

   Extra keys on a leaf (anything not in
   #{:data :mapping :layers :poses :layout :opts :share-scales})
   pass through to the resolved leaf so callers can attach metadata
   like :path-labels from facet-style generators."
  ([pose]
   (resolve-tree pose {} []))
  ([pose parent-ctx path]
   (let [ctx (if (:plotje/empty-cell pose)
               ;; A cell left empty -- `nil` in `pj/arrange` -- takes
               ;; nothing from above but the options, so it draws no
               ;; mark: inheriting the root's data and layers, it would
               ;; be an ordinary cell asked to draw with no columns.
               {:data nil :mapping {} :layers []
                :opts (merge {} (:opts parent-ctx) (:opts pose))}
               {:data    (or (:data pose) (:data parent-ctx))
                :mapping (merge-mappings (:mapping parent-ctx) (:mapping pose))
                :layers  (into (vec (:layers parent-ctx))
                               (:layers pose))
                :opts    (merge {} (:opts parent-ctx) (:opts pose))})]
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
     ;; multi-pair-pose produces. Nested matrix-
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

;; ---- Shared scales: who shares with whom ----
;;
;; A composite's `:share-scales` says which cells share an axis. This
;; stage decides only that: the cells are put in groups
;; (`shared-axis-groups`), and no extent is read from a column. What a
;; group's axis covers is what its cells draw, which is known once
;; their stats have run, so the plan stage measures each cell, pools the
;; group and plans every cell with the pooled extent
;; (`compositor/composite-draft->plan`, `plan/draft->extents`).
;;
;; Sharing is by column: cells drawing one column on an axis share it,
;; and cells drawing different columns keep their own. That gives a
;; scatter plot matrix its aligned rows and columns and a marginal its
;; panel's axis.

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
   mappings take precedence over the leaf's own :mapping, and the first
   layer naming `axis` answers.

   A leaf whose layers draw several columns on `axis` draws them on its
   one panel, and this reads only the first; `drawn-axis-cols` reads
   them all."
  [leaf axis]
  (or (some (fn [layer] (mapping-source (get-in layer [:mapping axis])))
            (:layers leaf))
      (mapping-source (get-in leaf [:mapping axis]))))

(defn- panel-axis-cols
  "The columns the layers of a one-panel leaf draw on `axis`, in layer
   order and without repeats: each layer's own column, or the leaf's
   where the layer names none. A layer that draws a count or a density
   on `:y` names no column there and adds none."
  [leaf axis]
  (let [own (mapping-source (get-in leaf [:mapping axis]))
        layers (:layers leaf)]
    (vec (distinct (remove nil? (if (seq layers)
                                  (map #(or (mapping-source (get-in % [:mapping axis])) own) layers)
                                  [own]))))))

(defn- drawn-axis-cols
  "The columns a leaf is bucketed under for a shared scale on `axis`,
   where its layers draw several together on its panel
   (`panel-axis-cols`). Nil where the leaf draws one column."
  [leaf axis]
  (let [cols (panel-axis-cols leaf axis)]
    (when (next cols) cols)))

(defn- data-axis
  "The axis of a leaf's mapping that is drawn along `axis` of the page:
   `axis` itself, and the other one under `:coord :flip`. Sharing names
   the axis as drawn -- `:x` is the horizontal one -- because it is
   along a direction of the page that panels side by side are compared,
   so an upright cell and a flipped one drawing a column the same way
   share it. (`pj/scale` and `:x-label` name the mapping instead.)"
  [leaf axis]
  (if (= :flip (get-in leaf [:opts :coord]))
    ({:x :y :y :x} axis)
    axis))

(defn- axis-buckets
  "The leaves of `subtree` grouped by the column they draw along `axis`
   of the page (`data-axis`), as `{col [leaf ...]}`.

   A leaf whose panels read several columns is placed in the bucket of
   each. Every reader of the buckets goes through this, so the check,
   the union and the stamp agree about which leaves share. Grouping by
   `effective-axis-col` alone placed such a leaf in its first column's
   bucket, and the panel reading the second column was stamped the
   first column's extent: its marks were drawn off the panel, with
   nothing said."
  [subtree axis]
  (reduce (fn [m [col leaf]] (update m col (fnil conj []) leaf))
          {}
          (mapcat (fn [leaf]
                    (let [a (data-axis leaf axis)]
                      (if-let [cols (drawn-axis-cols leaf a)]
                        (map #(vector % leaf) cols)
                        [[(effective-axis-col leaf a) leaf]])))
                  subtree)))

(declare axis-computes)

(defn- counted-key
  "What a leaf whose every layer computes what it draws along its axis
   `a` is grouped under there -- a count or a density on `:y`: what is
   computed (`stat/computes`) and the column it is computed from. Such
   an axis has no column of its own, and counts of one column are one
   measure -- the histograms of a split share their count axis -- while
   a count is not the values of the column counted, nor a density of
   it, so this is a key apart from the column's."
  [leaf a]
  (when-let [c (axis-computes leaf a)]
    {::computes c
     ::of (effective-axis-col leaf ({:x :y :y :x} a))}))

(defn- counted-key? [k] (and (map? k) (contains? k ::computes)))

(defn- share-key-buckets
  "The leaves of `subtree` grouped by what they draw along `axis` of
   the page, for sharing: `axis-buckets`, with a leaf drawing a count or
   a density there under its `counted-key`."
  [subtree axis]
  (reduce (fn [m [k leaf]] (update m k (fnil conj []) leaf))
          {}
          (mapcat (fn [leaf]
                    (let [a (data-axis leaf axis)]
                      (if-let [k (counted-key leaf a)]
                        [[k leaf]]
                        (if-let [cols (drawn-axis-cols leaf a)]
                          (map #(vector % leaf) cols)
                          [[(effective-axis-col leaf a) leaf]]))))
                  subtree)))

(defn- share-groups
  "The sets of keys a share spec puts together on an axis, beyond each
   on its own. Keys go together only where they measure alike: columns
   with columns, counts with counts, densities with densities
   (`counted-key`). `:all` puts every key together with those it
   measures alike with; a vector of groups names columns, and puts
   together those columns, and what is computed from them, each with
   its like; `:same-column` adds nothing."
  [how ks]
  (let [computed (distinct (map ::computes (filter counted-key? ks)))]
    (cond
      (= :all how) (mapv set (vals (group-by #(when (counted-key? %) (::computes %))
                                             (remove nil? ks))))
      (sequential? how) (into (mapv set how)
                              (for [group how
                                    c computed]
                                (set (map (fn [col] {::computes c ::of col}) group))))
      :else [])))

(declare infer-layer-type-key coerce-dataset)

(defn- layer-stat
  "The stat the plan assigns this layer. `elaborate` has written a layer
   type in wherever one was left to inference, so this reads the layer
   type; a layer still `:infer` -- one an ancestor carries to several
   leaves -- takes the type the data chooses here, by the same rule.
   The stat a fixed mark defaults to is `resolve/infer-layer-type`'s, so
   a `:rect` with no `:y` counts. Nil where no mark can be known."
  [layer leaf-mapping leaf-data]
  (let [v (merge leaf-mapping (:mapping layer))
        lt-key (if (= :infer (:layer-type layer))
                 (infer-layer-type-key v (or (:data layer) leaf-data))
                 (:layer-type layer))
        lt-info (when (keyword? lt-key) (layer-type/lookup lt-key))
        mark (or (:mark layer) (:mark lt-info))
        stat (or (:stat layer) (:stat lt-info))
        x (mapping-source (:x v))
        y (mapping-source (:y v))
        ;; One column on both axes is counted where it is categorical
        ;; (`resolve/infer-layer-type`), so the column's type is read
        ;; for that case and no other.
        x-type (when (and x (= x y))
                 (try (:x-type (resolve/infer-column-types
                                (coerce-dataset (or (:data layer) leaf-data))
                                (cond-> {:x x :y y}
                                  (:x-type v) (assoc :x-type (:x-type v)))))
                      (catch Exception _ nil)))]
    (when mark
      (:stat (resolve/infer-layer-type (cond-> {:mark mark
                                        :stat stat
                                                :y y}
                                         x-type (assoc :x x))
                                       x-type nil false false)))))

(defn- layer-computes
  "What the stat of this layer computes for `axis` -- `:count`,
   `:density`, whatever an extension's stat declares
   (`stat/computes`) -- or nil where the layer draws a column there. The
   stat is asked with the layer's options, the layer type's defaults
   under them."
  [layer leaf-mapping leaf-data axis]
  (when-let [stat (layer-stat layer leaf-mapping leaf-data)]
    (let [lt-key (:layer-type layer)
          defaults (when (and (keyword? lt-key) (not= :infer lt-key))
                     (:defaults (layer-type/lookup lt-key)))]
      (get (stat/computes (assoc (merge defaults leaf-mapping (:mapping layer))
                                 :stat stat))
           axis))))

(defn- axis-computes
  "What every layer of this leaf computes for `axis`, where every layer
   computes something there: the first layer's answer. Nil where a
   layer draws a column on it, or the leaf has no layers."
  [leaf axis]
  (let [cs (map #(layer-computes % (:mapping leaf) (:data leaf) axis) (:layers leaf))]
    (when (and (seq cs) (every? some? cs))
      (first cs))))

(defn- col-values
  "Non-nil values for a column reference from a dataset. The reference
   must match a column name literally (a keyword does not match a
   string column with the same characters and vice versa)."
  [ds col-ref]
  (when (and ds col-ref)
    (when (contains? (set (tc/column-names ds)) col-ref)
      (remove nil? (ds col-ref)))))

(defn- leaf-share-key
  "Compatibility descriptor for a leaf when sharing the scale drawn
   along `axis` of the page. Two leaves in one bucket must produce the
   same descriptor to be sharable; mismatches signal a `:share-scales`
   across cells that would mean different things on that axis (mixed
   coord, mixed numeric/categorical/temporal, mixed linear/log). A
   flipped cell and an upright one agree: the flip decides which of the
   leaf's columns is drawn there (`data-axis`), and nothing else."
  [leaf axis col]
  (let [axis (data-axis leaf axis)
        coord (let [c (or (get-in leaf [:opts :coord]) :cartesian)]
                (if (= :flip c) :cartesian c))
        ;; The mapping is where a scale lives, whether it was written
        ;; there or set with `pj/scale`.
        scale-type (or (mapping-scale-type (get-in leaf [:mapping axis]))
                       :linear)
        ds (:data leaf)
        type-temporal (if (counted-key? col)
                        ;; A count or a density is a number.
                        [:numerical false]
                        (when (and ds col)
                          (try
                            (let [{:keys [x-type y-type x-temporal? y-temporal?]}
                                  (resolve/infer-column-types ds {axis col})]
                              (case axis
                                :x [x-type x-temporal?]
                                :y [y-type y-temporal?]))
                            (catch Throwable _ nil))))]
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

(defn- share-buckets
  "The cells that share `axis` under `how`, as a seq of
   `{:cols [...] :pairs [[col leaf] ...]}` -- one entry per set of
   keys that go together (`share-groups`), each key in no group on its
   own. A key is a column, or the `counted-key` of a count or density
   axis. Read by the checks below and by `shared-axis-groups`, so they
   agree about which cells share."
  [subtree axis how]
  (let [by-col (dissoc (share-key-buckets subtree axis) nil)
        groups (share-groups how (keys by-col))
        grouped (reduce (fn [groups link]
                          (let [{hit true miss false} (group-by #(boolean (some link %)) groups)]
                            (conj (vec miss) (reduce into link hit))))
                        (mapv hash-set (keys by-col))
                        groups)]
    (for [group grouped
          :let [cols (filterv group (keys by-col))]
          :when (seq cols)]
      {:cols cols
       :pairs (for [col cols
                    leaf (get by-col col)]
                [col leaf])})))

(defn- describe-share-key
  [k]
  (if (counted-key? k)
    (str "the " (name (::computes k)) " of " (pr-str (::of k)))
    (pr-str k)))

(defn- describe-share-cols
  [cols]
  (if (next cols)
    (str "columns " (str/join ", " (map describe-share-key cols)) " have")
    (str "column " (describe-share-key (first cols)) " has")))

(defn- validate-share-bucket-compatibility!
  "For each set of cells sharing an axis under `spec`, refuse when the
   cells disagree about coord / scale-type / inferred type. Throws an
   ex-info naming the conflict."
  [subtree spec]
  (doseq [[axis how] spec
          {:keys [cols pairs]} (share-buckets subtree axis how)
          :let [keys-set (set (map (fn [[col leaf]] (leaf-share-key leaf axis col)) pairs))]
          :when (> (count keys-set) 1)]
    (throw (ex-info
            (str ":share-scales " axis " refused: "
                 (describe-share-cols cols)
                 " incompatible scale meaning across cells ("
                 (describe-share-conflict keys-set)
                 "). Cells targeting :share-scales must agree on"
                 " coord (e.g. cartesian vs flip), inferred column"
                 " type (numerical / categorical / temporal), and"
                 " scale type (linear / log).")
            {:caller "share-scales"
             :axis axis
             :column (first cols)
             :columns cols
             :share-keys keys-set}))))

(defn- warn-share-scales-overrides-free!
  "Warn when faceted sub-poses write a `:share-scales` of their own that
   leaves out an axis the composite shares. The composite's share wins:
   the shared extent is stamped on the sub-pose, and its panels draw it.
   One message per axis, however many sub-poses it is true of."
  [subtree spec]
  (doseq [axis (keys spec)
          :let [owns (keep (fn [leaf]
                             (let [own (get-in leaf [:opts :share-scales])]
                               (when (and (some? own)
                                          (not (contains? (defaults/share-scales-axes own) axis)))
                                 own)))
                           subtree)]
          :when (seq owns)]
    (println (str "Warning: a composite's :share-scales shares " axis
                  ", and " (if (next owns)
                             (str (count owns) " poses inside it write")
                             "a pose inside it writes")
                  " a :share-scales that leaves " axis " out ("
                  (str/join ", " (distinct (map pr-str owns)))
                  "). The composite's share wins, and the panels of "
                  (if (next owns) "those poses" "that pose")
                  " share " axis ". Drop one of the two to remove the"
                  " conflict."))))

(declare share-buckets)

(defn- warn-share-scales-without-effect!
  "Warn where `:share-scales` names an axis and no two cells share it:
   `:same-column` on an axis where every cell draws a column of its own,
   or a group of which fewer than two columns are drawn. The picture is
   the one drawn without the option, so without this the option reads as
   ignored. Names the group that would share the columns."
  [subtree spec]
  (doseq [[axis how] spec
          :let [buckets (remove (comp empty? :pairs) (share-buckets subtree axis how))
                drawn-keys (vec (mapcat :cols buckets))
                ;; A group names columns; a count is named by the
                ;; column counted.
                drawn (vec (distinct (map #(if (counted-key? %) (::of %) %) drawn-keys)))
                drawn-text (str/join ", " (map describe-share-key drawn-keys))]
          ;; Only where every column is drawn by one cell. A column two
          ;; cells draw, one of them as a count -- the diagonal of a
          ;; two-by-two matrix -- shares nothing either, but the option
          ;; there is the matrix's own, and right for any larger one.
          ;;
          ;; Nor where a column drawn here is drawn on the other axis by
          ;; another cell: a marginal on the right draws the panel's
          ;; `:y` column along its own `:x` under a flip, and the two
          ;; agree by each taking that column's extent.
          :let [other (set (keys (dissoc (axis-buckets subtree (if (= axis :x) :y :x)) nil)))]
          :when (and (< 1 (count buckets))
                     (not-any? #(next (:pairs %)) buckets)
                     (every? #(= 1 (count %))
                             (vals (dissoc (axis-buckets subtree axis) nil)))
                     (not-any? other drawn))]
    (println
     (str "Warning: :share-scales shares " axis
          (if (sequential? how)
            (str " between the columns of " (pr-str how)
                 ", and the cells draw " drawn-text
                 " there, each in one cell, so no two cells share " axis
                 ". Name the columns the cells draw, as in {"
                 axis " " (pr-str [drawn]) "}.")
            (str " among the cells drawing the same column, and the cells"
                 " draw " drawn-text
                 " there, each in one cell, so no two cells share " axis
                 ". To share " axis " between columns that measure one"
                 " quantity, name them as a group: {:share-scales {"
                 axis " " (pr-str [drawn]) "}}; {" axis
                 " :all} shares it between every cell."))))))

(declare share-axes share-spec)

(defn- declared-split?
  "True of a composite whose cells are the parts of one declared split:
   the composite carries the data and no cell brings data of its own,
   so every cell draws a part of the same dataset -- a column, a pair,
   or a `:where` subset. Cells that each carry their own data were
   handed over as separate plots. Provisional (Rule 1, PR #61 agenda);
   a grouped dataset is the case this does not yet read."
  [pose]
  (and (seq (:poses pose))
       (some? (:data pose))
       (every? #(or (nil? (:data %)) (:plotje/part-of-parent-data %))
               (:poses pose))))

(defn shared-axis-groups
  "Which cells of a pose tree share an axis with which, as
   `{path {axis {:group id :order [category ...]}}}` for every leaf in
   a group of two or more. `axis` there is the leaf's own -- the axis
   of its mapping -- while `:share-scales` names the axis as drawn
   (`data-axis`).

   Each composite with a `:share-scales` -- written in `(:opts pose)` or
   at the top of a hand-built composite, or inferred where its cells are
   the parts of one declared split (`share-spec`) -- links the cells of
   each of its buckets (`share-buckets`). Links chain: a cell whose
   layers draw `:a` and `:b` on one panel is in both columns' buckets,
   and a cell linked by an outer composite and by an inner one joins
   the two sets, so sharing is one relation over the whole tree and
   every cell of a group ends on the same domain.

   A flipped cell and an upright one drawing a column along the same
   direction of the page are in one bucket: a marginal on the right
   names the panel's `:y` column on its own `:x` under a flip, and
   follows whatever the panel's layers draw there.

   `:order` is given where every cell of a group reads the dataset of
   the composite that links it, whole or in part, and the columns the
   group draws hold categories: it lists them in the order of that
   dataset's rows, which is the order a shared categorical axis then
   takes (`plan/order-categories`), as the same plot unsplit does.

   The checks and warnings about a `:share-scales` are raised here."
  [root]
  (let [links (atom [])
        walk (fn walk [pose path inherited-mapping inherited-data]
               (when (seq (:poses pose))
                 (let [data (or (:data pose) inherited-data)
                       mapping (merge-mappings inherited-mapping (:mapping pose))]
                   (when-let [spec (not-empty (share-spec pose))]
                     (let [subtree (resolve-tree pose
                                                 {:mapping inherited-mapping
                                                  :data inherited-data}
                                                 path)
                           _ (validate-share-bucket-compatibility! subtree spec)
                           _ (warn-share-scales-overrides-free! subtree spec)
                           _ (warn-share-scales-without-effect! subtree spec)
                           buckets (into {} (for [[axis how] spec]
                                              [axis (vec (share-buckets subtree axis how))]))
                           one-dataset? (fn [pairs]
                                          (and (some? data)
                                               (every? (fn [[_ leaf]]
                                                         (or (identical? data (:data leaf))
                                                             (:plotje/part-of-parent-data leaf)))
                                                       pairs)))]
                       (doseq [[axis bs] buckets
                               {:keys [cols pairs]} bs
                               :when (seq pairs)]
                         (swap! links conj
                                {:nodes (set (map (fn [[_ leaf]] [(:path leaf) (data-axis leaf axis)]) pairs))
                                 :cols (vec (remove counted-key? cols))
                                 :data (when (one-dataset? pairs) data)}))))
                   (doseq [[i child] (map-indexed vector (:poses pose))]
                     (walk child (conj path i) mapping data)))))
        _ (walk root [] {} nil)
        merge-links (fn [a b]
                      {:nodes (into (:nodes a) (:nodes b))
                       :cols (vec (distinct (concat (:cols a) (:cols b))))
                       ;; One dataset for the whole group, or none.
                       :data (when (and (:data a) (identical? (:data a) (:data b)))
                               (:data a))})
        ;; The categories of the group's columns in the order of the
        ;; rows, where the columns hold categories.
        order-of (memoize
                  (fn [data cols]
                    (when data
                      (let [vs (mapcat #(col-values data %) cols)
                            v (first vs)]
                        (when (and (some? v)
                                   (not (or (number? v) (resolve/temporal-value? v))))
                          (vec (distinct (map str vs))))))))
        groups (reduce (fn [groups link]
                         (let [{hit true miss false}
                               (group-by #(boolean (some (:nodes link) (:nodes %))) groups)]
                           (conj (vec miss) (reduce merge-links link hit))))
                       []
                       (deref links))]
    (reduce (fn [m [id {:keys [nodes cols data]}]]
              (if (next nodes)
                (reduce (fn [m [path axis]]
                          (assoc-in m [path axis]
                                    (cond-> {:group id}
                                      (seq (order-of data cols))
                                      (assoc :order (order-of data cols)))))
                        m nodes)
                m))
            {}
            (map-indexed vector groups))))

;; ---- Leaf-to-draft ----
;;
;; The draft emitter. Consumes one resolved leaf and produces a draft
;; vector -- the same shape plan/draft->plan accepts.

(defn- coerce-dataset
  "Coerce raw data to a Tablecloth dataset. Returns nil for nil."
  [d]
  (when d
    (if (tc/dataset? d) d (tc/dataset d))))

(defn coerce-data-in-tree
  "A pose with the `:data` of every pose in it a dataset. `pj/pose`
   coerces the data it is given; a pose written as a map may hold a
   plain map or rows, and the sharing of axes reads columns from
   `:data` as a dataset -- on a plain map it found no column, and cells
   asked to share an axis kept their own, with no message."
  [pose]
  (cond-> pose
    (and (some? (:data pose)) (not (tc/dataset? (:data pose))))
    (update :data coerce-dataset)

    (seq (:poses pose))
    (update :poses #(mapv coerce-data-in-tree %))))

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
                (str/join ", " (map pr-str (defaults/drawable-shapes))))
    :size  "a fixed size is a positive number"
    :alpha "a fixed alpha is a number within 0 and 1"
    :text  "a label is a string"
    :tooltip "hover text is a string, or a hiccup vector for markup"
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
   When the layer carries its own :data and the failing key is `:x` or
   `:y`, say that the layer's data lacks the column and that the layer
   call is where to name one it holds.

   `explicit-column?` marks a reference the writer wrote as
   `{:column ...}`. The drawn-value sentence is dropped there: they
   have said which reading they meant, so the other one is not the
   answer."
  [k col col-names {:keys [layer-type-key layer-own-data? source explicit-column?]}]
  (let [layer-name (if (keyword? layer-type-key) (name layer-type-key) "*")]
    (case (when (and layer-own-data? (#{:x :y} k) (not explicit-column?)) :panel-column)
      ;; The layer reads this column from the pose's mapping, or from
      ;; the panel's columns where the pose names none
      ;; (`read-panel-columns` has then written it into the layer), or
      ;; names it itself. Each way it is a column the layer's own data
      ;; has to hold.
      :panel-column
      (str "lay-" layer-name " " k " " (pr-str col)
           (if (= :pose source)
             ", inherited from the pose's mapping,"
             ", the panel's column or the layer's own,")
           " names a column absent from this layer's :data. Available columns: "
           (vec (sort-by str col-names))
           ". Set " k " on the layer call to a column that exists in"
           " :data.")
      ;; Default: simple "not found" with the available columns
      (str "Column " col " (from " k ") not found in dataset."
           " Available: " (sort-by str col-names)
           (when-not explicit-column?
             (also-not-drawable-sentence k col))
           ;; A slot that places a mark reads a bare name as a column,
           ;; strictly, so a category or a place is written as a value.
           (when (and (not explicit-column?) (#{:x :y :x-end :y-end} k))
             (str " To place the mark at " (pr-str col) " itself rather than"
                  " read a column, write {" k " {:value " (pr-str col) "}}."))))))

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
     describe the data at all -- it marks a place on the panel -- so one
     row holding those values draws the single mark it asks for. `:text`
     is handled only here: a string normally names a column, and a layer
     with no data has no column for it to name, so there the string is
     the text.
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
                                                ;; `:scale true`, or a scale spec
                                                ;; beside a `:value`. A scale spec
                                                ;; that reached a plain value from
                                                ;; a wider scope -- a legend title
                                                ;; set on the pose, above a layer's
                                                ;; `:color "black"` -- configures
                                                ;; the scale and does not ask for
                                                ;; the value to be read through it.
                                                (or (true? scale)
                                                    (and (some? scale)
                                                         (not (false? scale))
                                                         (= :value (said k)))))]
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
      (throw (ex-info (str "Column " ref " (from " role ") not found in dataset. Available: " (sort-by str col-names))
                      {:role role :column ref :available (sort-by str col-names)})))
    fname))

(def facet-label-separator
  "What joins the parts of a compound facet label. A panel faceted by
   several columns is labelled by each column's value in turn, and this
   is what stands between them."
  " / ")

(defn- facet-columns
  "The columns a facet aesthetic names, as a vector, or nil where it
   names none.

   One column is the one-column case of several, which is the rule
   `resolve/infer-grouping` already applies to `:group`: a distinction
   read by value is made of one or more columns, united.

   Any value is passed on as a column reference: the data decides what
   names a column, as it does for every other mapping, so an integer
   names column 2 of a dataset whose columns are integers.
   `resolve-facet-col` reports a reference the data does not have --
   filtered out here by type, an integer drew one panel and no message."
  [v]
  (cond
    (nil? v) nil
    (sequential? v) (when (seq v) (vec v))
    :else [v]))

(defn- facet-keys
  "The facet key of every row, as a vector aligned with the dataset --
   each entry the tuple of values the facet columns hold in that row.

   Read column by column rather than row by row: one column is the
   one-column case, so the tuple is built by zipping the columns the
   aesthetic names."
  [ds role refs]
  (let [names (mapv #(resolve-facet-col ds role %) refs)
        cols  (mapv #(ds %) names)]
    (apply mapv vector cols)))

(defn- facet-label
  "What a facet key prints as: each column's value in turn, through the
   same formatter a category label goes through anywhere else."
  [key-tuple]
  (str/join facet-label-separator (map defaults/fmt-category-label key-tuple)))

(defn- same-value?
  "Whether a column value `x` matches a value `v` written to select it.
   Numbers compare by value, so `4` selects a row holding `4.0`."
  [x v]
  (if (and (number? x) (number? v)) (== x v) (= x v)))

(defn- same-values?
  "Whether two sequences of values match element by element, as
   `same-value?` matches one -- a compound key's values compared within."
  [a b]
  (and (= (count a) (count b))
       (every? true? (map (fn [x y]
                            (if (and (sequential? x) (sequential? y))
                              (same-values? x y)
                              (same-value? x y)))
                          a b))))

(defn- rows-where
  "The indices at which `ks` holds `k`, or one of the values of `k`
   where it is a set. `tc/select-rows` takes them directly, so no row
   map is built to ask a question about a column."
  [ks k]
  (let [match? (if (set? k)
                 (fn [x] (some #(same-value? x %) k))
                 (fn [x] (same-value? x k)))]
    (into [] (keep-indexed (fn [i v] (when (match? v) i))) ks)))

(defn- facet-variants
  "Build one (data + labels) variant per facet-value combination.
   Returns a vector of maps {:data ds-subset, :facet-col <label>?, :facet-row <label>?}.
   When neither axis is faceted, returns a single-element vector carrying the
   input dataset unchanged and no labels.

   A facet aesthetic may name several columns, which are united into a
   compound key -- one panel per combination the data holds, labelled
   by each column's value in turn. That is the same reading `:group`
   and `:color` give a vector, and it differs from `pj/facet-grid`,
   which crosses two distinctions and fills the rectangle whether the
   data holds a combination or not."
  [data facet-col facet-row]
  (let [ds    (coerce-dataset data)
        crefs (facet-columns facet-col)
        rrefs (facet-columns facet-row)]
    (cond
      (not (or crefs rrefs))
      [{:data ds}]

      (and crefs rrefs)
      (let [ck (facet-keys ds :col crefs)
            rk (facet-keys ds :row rrefs)
            both (mapv vector ck rk)]
        (vec
         (for [cv (distinct ck) rv (distinct rk)]
           {:data (tc/select-rows ds (rows-where both [cv rv]))
            :facet-col (facet-label cv)
            :facet-row (facet-label rv)})))

      crefs
      (let [ck (facet-keys ds :col crefs)]
        (vec
         (for [cv (distinct ck)]
           {:data (tc/select-rows ds (rows-where ck cv))
            :facet-col (facet-label cv)})))

      rrefs
      (let [rk (facet-keys ds :row rrefs)]
        (vec
         (for [rv (distinct rk)]
           {:data (tc/select-rows ds (rows-where rk rv))
            :facet-row (facet-label rv)}))))))

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

(defn position-mapping-key
  "A mapping's place, as `[x-source y-source]`, or nil where it names
   neither."
  [m]
  (let [m (or m {})
        x (mapping-source (:x m))
        y (mapping-source (:y m))]
    ;; A scale written by `pj/scale` puts a map under `:x` that names
    ;; no column; that is not a place.
    (when (or x y)
      [x y])))

(defn layer-position-key
  "The place a layer draws at, as `[x-source y-source]` -- what decides
   which panel it lands on. Nil where the layer names no place of its
   own, which is what makes it draw on every panel.

   Whether the layer names a place is read from its own `:mapping`: a
   layer that names none is not choosing the leaf's, it is declining to
   choose, and the two answers place it differently. The place itself is
   read from the leaf's mapping merged with the layer's, so that a layer
   naming one axis draws at the same place as one naming both."
  [leaf-mapping layer data]
  (let [own (or (:mapping layer) {})
        ;; A written value is not a column: it places a mark on the panel
        ;; the layer is added to and asks for no panel of its own, so
        ;; `(pj/lay-text pose {:x 7.5 :y 4.2 :text "note"})` annotates the
        ;; panel rather than starting one. The layer's data answers which
        ;; of the two a value is -- the same question `leaf->draft` asks
        ;; when it draws the layer, and the same rule `identity-columns`
        ;; applies where the layer is added.
        col-names (when data (try (set (tc/column-names (coerce-dataset data)))
                                  (catch Exception _ nil)))
        column? (fn [k]
                  (and (contains? own k)
                       (let [src (mapping-source (get own k))]
                         (if col-names
                           (contains? col-names src)
                           (resolve/column-ref? src)))))
        ;; Only the axes the layer names by column; an axis it writes a
        ;; value at leaves the leaf's column standing, so a label at
        ;; `{:x 2.0 :y :weight}` draws at the place the panel already has
        ;; rather than asking for one whose x is the number 2.0.
        cols (select-keys own (filterv column? [:x :y]))]
    (when (seq cols)
      (let [m (merge-mappings (or leaf-mapping {}) cols)]
        [(mapping-source (:x m)) (mapping-source (:y m))]))))

(defn- layer-place
  "The place a layer of `leaf` names, or nil -- `layer-position-key`
   read against the leaf's mapping and the data the layer draws from."
  [leaf layer]
  (layer-position-key (:mapping leaf) layer (or (:data layer) (:data leaf))))

(defn leaf-place
  "The place a leaf draws at -- the panel's columns -- as
   `[x-source y-source]`, or nil where neither it nor any layer names
   one. Decided axis by axis: the leaf's own column, and where its
   mapping names none on that axis, that of the first layer naming one
   there. A `lay-*` call keeps its columns on its layer, so a pose built
   by `lay-*` alone names them only there. The layers of a leaf share
   this one panel; separate panels come from a split or from
   `pj/arrange`."
  [leaf]
  (let [places (cons (position-mapping-key (:mapping leaf))
                     (keep #(layer-place leaf %) (:layers leaf)))
        x (some first places)
        y (some second places)]
    (when (or x y) [x y])))

(defn leaf-columns
  "The columns a leaf draws on `:x` and `:y` on its first panel, as
   `{:x source :y source}` with the axes it does not name left out. The
   leaf's own mapping where it names an axis, and otherwise the columns
   its layers name: a `lay-*` call keeps its columns on its layer, so a
   pose built by `lay-*` alone names them only there."
  [leaf]
  (let [[x y] (leaf-place leaf)]
    (cond-> {}
      x (assoc :x x)
      y (assoc :y y))))

(defn- panel-columns
  "The panel's columns as a mapping, `{:x v :y v}`, leaving out an axis
   nothing names. Each axis keeps the value it was specified with --
   the leaf's, or that of the first layer naming the column -- so a
   scale specified beside the column comes too."
  [leaf]
  (let [k (leaf-place leaf)
        mappings (cons (or (:mapping leaf) {}) (map :mapping (:layers leaf)))]
    (into {}
          (for [[i axis] [[0 :x] [1 :y]]
                :let [src (nth k i nil)]
                :when src]
            [axis (or (some #(let [v (get % axis)]
                               (when (= src (mapping-source v)) v))
                            mappings)
                      src)]))))

(defn- layer-panel-columns
  "The panel's columns one layer of a leaf reads, as a mapping to put
   under the layer's own: both for a layer naming no column, and for a
   layer naming one column whose layer type draws only from two
   (`layer-type/column-counts` lacks 1), the column on the axis it
   leaves out. A layer type that can draw from one column is given no
   second: `(pj/lay-bar pose :drv)` beside points counts. Nothing for
   an axis the layer's own mapping holds, or for a layer placed in the
   drawing area, which reads drawing units."
  [leaf layer columns]
  (let [own (or (:mapping layer) {})
        k (layer-place leaf layer)
        axes (cond
               (= :drawing-area (:in own)) []
               (nil? k) [:x :y]
               (contains? (layer-type/column-counts (:layer-type layer)) 1) []
               (nil? (first k)) [:x]
               (nil? (second k)) [:y]
               :else [])]
    (into {} (remove (fn [[axis _]] (contains? own axis)))
          (select-keys columns axes))))

(defn- read-panel-columns
  "A leaf whose layers hold the panel's columns they read
   (`layer-panel-columns`), written into each layer's mapping. An axis
   the leaf's own mapping names is left out: it reaches every layer by
   scope. Idempotent: a layer that was given a column names it."
  [leaf]
  (let [m (or (:mapping leaf) {})
        columns (into {} (remove (fn [[axis _]] (mapping-source (get m axis))))
                      (panel-columns leaf))]
    (if (empty? columns)
      leaf
      (update leaf :layers
              (fn [ls]
                (mapv (fn [layer]
                        (let [add (layer-panel-columns leaf layer columns)]
                          (if (seq add)
                            (update layer :mapping #(merge add %))
                            layer)))
                      ls))))))

(defn- write-panel-columns
  "A leaf whose own mapping names the panel's columns: on each axis
   where it names none, the column of the first layer naming one there.
   For the pose a facet over columns expands: its cells are made from
   the panel's columns, and the facet replaces the axis it names."
  [leaf]
  (let [m (or (:mapping leaf) {})
        add (into {} (remove (fn [[axis _]] (mapping-source (get m axis))))
                  (panel-columns leaf))]
    (if (seq add)
      (assoc leaf :mapping (merge-mappings add m))
      leaf)))

(defn overlay-labels
  "The column each layer of a panel draws where its layers disagree, as
   a vector aligned with `(:layers leaf)`, or nil where they all draw
   one place.

   Read for two things, which do not depend on one another. The axis is
   titled by every column drawn on it, so each labelled layer is marked
   for the title. And the label is the default for a layer's `:color`:
   a layer with no `:color` specified takes its label as a colour and a
   legend entry (`default-color-layers`).

   Layers that disagree about a column take a panel each, and the
   panels name the columns. Overlaid, they share a panel and nothing
   names them: the marks are drawn in one colour under an axis titled
   after whichever layer came first, which is a picture that shows
   less than the writer asked for and says nothing about it.

   The label is the column a layer draws where the layers disagree.
   Answered nil where nothing distinguishes them, which is the ordinary
   case of layers drawing one place."
  [leaf]
  (let [layers (vec (:layers leaf))
        leaf-mapping (:mapping leaf)
        ;; A layer naming no place draws at the leaf's place, so it is
        ;; told apart like any other.
        place (leaf-place leaf)
        ;; A layer placed in the drawing area reads drawing units, not
        ;; a column of the panel's axes, so it names nothing an axis
        ;; title or a legend entry could.
        places (mapv #(when-not (= :drawing-area (:in (:mapping %)))
                        (or (layer-position-key leaf-mapping %
                                                (or (:data %) (:data leaf)))
                            place))
                     layers)
        relevant (keep-indexed (fn [i p] (when p i)) places)
        sources (fn [axis] (vec (distinct (keep #(nth (nth places %) axis nil) relevant))))]
    (when (seq relevant)
      (let [disagreeing (vec (keep (fn [axis] (when (< 1 (count (sources axis))) axis))
                                   [0 1]))]
        (when (seq disagreeing)
          (mapv (fn [i place]
                  (when place
                    (str/join " / " (map #(defaults/fmt-name (nth place % nil))
                                         disagreeing))))
                (range (count layers))
                places))))))

(defn- color-specified?
  "Whether `mapping` -- the mapping a layer resolves to -- has a `:color`
   specified.

   A scale set with no column or value -- `(pj/scale pose :color
   {:label \"Measure\"})` -- specifies how a colour is read and leaves
   what is coloured unspecified, so that part is still inferred. `nil`
   specifies no colour."
  [mapping]
  (and (contains? mapping :color)
       (let [v (:color mapping)]
         (or (not (map? v))
             (some #(contains? v %) [:from :column :value])))))

(defn- color-column?
  "Whether the `:color` of `mapping` names a column of `data`: a
   `{:column ...}`, or a plain value or a `:from` that the data holds
   as a column."
  [mapping data]
  (let [v (:color mapping)
        src (mapping-source v)]
    (boolean
     (and (color-specified? mapping)
          (some? src)
          (not (map? src))
          (not (and (map? v) (contains? v :value)))
          (or (and (map? v) (contains? v :column))
              (when-let [ds (when data (coerce-dataset data))]
                (contains? (set (tc/column-names ds)) src)))))))

(defn- default-color-layers
  "Which layers of a leaf take the default colour, as a vector of
   booleans aligned with `(:layers leaf)`.

   The default is decided layer by layer: a layer with no `:color`
   specified, in the mapping it resolves to, takes the colour and the
   legend entry of the column it draws. A `:color` on the pose is a
   mapping like any other and reaches every layer, so no layer below it
   needs a default.

   No layer takes the default on a panel where a layer is coloured by a
   column. The entries of that legend are the column's values, and a
   column name listed among them would read as one more value."
  [leaf]
  (let [leaf-mapping (or (:mapping leaf) {})
        resolved (mapv #(merge-mappings leaf-mapping (:mapping %)) (:layers leaf))
        by-column? (some true? (map #(color-column? %1 (or (:data %2) (:data leaf)))
                                    resolved (:layers leaf)))]
    (mapv #(and (not by-column?) (not (color-specified? %))) resolved)))

(defn- default-color
  "The default colour of a layer labelled `label`, in the form a pose
   can specify: a value read through the colour scale, which draws one
   palette colour and a legend entry. `scale` is `{:label \"\"}` where
   the entries name columns and the legend needs no title, and `true`
   where the legend is titled elsewhere."
  [label scale]
  {:value label :scale scale})

(def default-series-label
  "The name the key column a series invents takes when the writer does
   not give one. It titles the legend, so it is a word a reader can read
   there."
  :series)

(def series-value-column
  "The name the value column a series invents takes. It titles the
   value axis, which `:x-label` and `:y-label` already rename, so it
   needs no second spelling of its own."
  :value)

(defn series-mapping
  "The series a mapping value asks for, as `{:cols [...] :as label}`, or
   nil where it asks for none.

   Two spellings, one meaning. A vector of columns on a positional
   aesthetic is the short one; `{:series [...] :as :measure}` is the
   same thing written out, and the only reason to write it is to name
   the key column the pivot invents.

   A vector of pairs is not a series -- that is the multi-panel form
   `pj/pose` reads -- so a vector whose elements are themselves
   sequential answers nil and is left to it."
  [v]
  (cond
    ;; Every element a column reference, which is what keeps a dash
    ;; pattern, a colour range and a list of breaks from being read as
    ;; columns to pivot.
    (and (sequential? v)
         (not (map? v))
         (seq v)
         (every? resolve/column-ref? v))
    {:cols (vec v) :as default-series-label}

    (and (map? v) (contains? v :series))
    (cond-> {:cols (vec (:series v)) :as (get v :as default-series-label)}
      ;; The scale rides along, so the value column the pivot invents is
      ;; read through it. Without this the `:scale` was accepted and
      ;; dropped, and the axis came out linear with no word said.
      (contains? v :scale) (assoc :scale (:scale v)))

    :else nil))

(def series-aesthetics
  "The aesthetics a series may be written under. A series names the
   columns a mark is drawn from, so it belongs where a column goes on
   an axis; on an appearance aesthetic there would be nothing for the
   pivot to draw."
  [:x :y])

(def series-pair-value-columns
  "The names the two value columns take where a series on `:x` and a
   series on `:y` are pivoted together as pairs. Each titles its axis,
   which `:x-label` and `:y-label` rename."
  {:x :x-value :y :y-value})

(defn series-clashes
  "The columns a series reading `cols`, with its key column named
   `label`, would write over in data whose columns are `column-names`,
   as `{:key <label or nil> :value <value column or nil>}`.

   The pivot drops the columns it reads and writes the two it invents,
   so only the columns it leaves are at risk. One rule for both places
   that ask: `api/pivot-series` reports a clash, and
   `report-panel-split` offers a series only where there is none --
   offering it where the pivot would then refuse sent a writer from one
   message straight into another."
  [cols label column-names]
  (let [remaining (set (remove (set cols) column-names))]
    {:key (when (contains? remaining label) label)
     :value (when (contains? remaining series-value-column) series-value-column)}))

;; ---- The series pivot ----

(defn- check-series-columns!
  "Throw where the columns a series names cannot be pivoted: fewer than
   two, one that is not a column reference, or one the data does not
   have. Shared by a series on one aesthetic and by the pairs two
   series on `:x` and `:y` make."
  [caller ds cols]
  (let [available (vec (sort-by str (tc/column-names ds)))
        present (set available)
        missing (vec (remove present cols))]
    (when (< (count cols) 2)
      (throw (ex-info (str caller " was given " (count cols) " column in a"
                           " slot where several are read as several series: "
                           (pr-str (vec cols)) ". Name the column on its own"
                           " where there is one.")
                      {:caller caller :columns (vec cols)})))
    (doseq [c cols]
      (when-not (resolve/column-ref? c)
        (throw (ex-info (str caller " was given " (pr-str (vec cols))
                             ", and " (pr-str c) " is not a column reference"
                             " -- a keyword or a string naming a column of"
                             " the data.")
                        {:caller caller :column c :columns (vec cols)}))))
    (when (seq missing)
      (throw (ex-info (str caller " was given several columns to read as"
                           " series, and the data does not have "
                           (pr-str missing) ". Available columns: "
                           available ".")
                      {:caller caller :missing missing :columns available})))))

(defn- check-single-series!
  "Throw where one series cannot be pivoted out of `ds`: its columns
   (see `check-series-columns!`), an `:as` naming the value column, or
   a key or value column the data already has."
  [caller ds spec]
  (let [{cols :cols label :as} spec
        present (set (tc/column-names ds))]
    (check-series-columns! caller ds cols)
    ;; Reported before the clash check below, whose set literal held
    ;; both names and threw a bare `Duplicate key: :value` when `:as`
    ;; named the value column.
    (when (= label series-value-column)
      (throw (ex-info (str caller " was given :as " (pr-str label)
                           ", which is the name the pivot gives the value"
                           " column it invents. The key column needs a name"
                           " of its own -- {:series " (pr-str (vec cols))
                           " :as :measure}.")
                      {:caller caller
                       :as label
                       :value-column series-value-column})))
    ;; Each clash is named on its own, with the remedy that moves it:
    ;; `:as` renames the key column and cannot rename the value column,
    ;; so a clash on the value column is offered only a rename in the
    ;; data. The value clash is reported first, because `:as` cannot
    ;; clear it.
    (let [clashes (series-clashes cols label present)
          key-clash? (some? (:key clashes))
          value-clash? (some? (:value clashes))
          key-named (if (= label default-series-label)
                      (str "the pivot names its key column " (pr-str label))
                      (str ":as names the key column " (pr-str label)))]
      (when (or key-clash? value-clash?)
        (throw (ex-info (str caller " cannot pivot these columns: "
                             (if value-clash?
                               (str "the pivot names its value column "
                                    (pr-str series-value-column)
                                    ", and the data already has a "
                                    (pr-str series-value-column) " column."
                                    " Rename that column in the data."
                                    (when key-clash?
                                      (str " Also, " key-named
                                           ", which the data has as well --"
                                           " name the key column something"
                                           " else with :as.")))
                               (str key-named ", and the data already has a "
                                    (pr-str label) " column. Name the key"
                                    " column something else -- {:series "
                                    (pr-str (vec cols)) " :as :measure} -- or"
                                    " rename the column in the data.")))
                        {:caller caller
                         :clashes (vec (cond-> []
                                         key-clash? (conj label)
                                         value-clash? (conj series-value-column)))
                         :key-column label
                         :value-column series-value-column}))))))

(defn- pivot-series
  "Pivot the columns a series names into a key column and a value
   column. Returns the new dataset. Checked by `check-single-series!`."
  [ds spec]
  (let [{cols :cols label :as} spec]
    ;; `tc/pivot->longer` drops a row whose value is missing, and its
    ;; defaults are kept rather than overridden -- a Tablecloth user
    ;; comparing the two should find them the same. What is said about
    ;; the drop is Plotje's own contract: a row that does not reach the
    ;; plot is named. Counted from the pivot's own effect -- one row per
    ;; original row and pivoted column, less what came back.
    (let [pivoted (tc/pivot->longer ds (vec cols)
                                    {:target-columns label
                                     :value-column-name series-value-column})]
      (defaults/report-removed-rows!
       (- (* (tc/row-count ds) (count cols)) (tc/row-count pivoted))
       (str "with a missing value among the columns read as series ("
            (str/join ", " (map pr-str cols)) ")"))
      pivoted)))

(defn- check-pair-column-kinds!
  "Throw where the columns one side of a pair of series names hold
   different kinds of value. They become one value column, which holds
   one kind; checked here so the message names the columns written, not
   the value column the pivot invents."
  [caller ds cols]
  (let [numerical (set (tc/column-names ds :type/numerical))
        temporal (set (tc/column-names ds :type/datetime))
        kind (fn [c] (cond (numerical c) :numerical
                           (temporal c) :temporal
                           :else :categorical))
        kinds (into {} (map (juxt identity kind)) cols)]
    (when (next (distinct (vals kinds)))
      (throw (ex-info (str caller " was given " (pr-str (vec cols))
                           " to read as series in pairs, and their values are"
                           " of different kinds: " (pr-str kinds) ". The"
                           " columns become one value column, so they must"
                           " all be numerical, all temporal or all"
                           " categorical.")
                      {:caller caller :columns (vec cols) :kinds kinds})))))

(defn- series-pairs-label
  "The key column a pair of series names: the `:as` either names, or the
   default."
  [x-spec y-spec]
  (or (first (remove #(= % default-series-label) [(:as x-spec) (:as y-spec)]))
      default-series-label))

(defn- check-series-pairs!
  "Throw where a series on `:x` and a series on `:y` cannot be pivoted
   together out of `ds`: different numbers of columns, columns that are
   missing or hold different kinds of value, two different `:as` names,
   or invented columns the data already has."
  [caller ds x-spec y-spec]
  (let [{xs :cols} x-spec
        {ys :cols} y-spec]
    (when (not= (count xs) (count ys))
      (throw (ex-info (str caller " was given " (count xs)
                           (if (= 1 (count xs)) " column" " columns")
                           " on :x, " (pr-str (vec xs)) ", and " (count ys)
                           " on :y, " (pr-str (vec ys)) ". Two series"
                           " are read in pairs, the first :x column"
                           " with the first :y column, so they take"
                           " as many columns each.")
                      {:caller caller :x (vec xs) :y (vec ys)})))
    (check-series-columns! caller ds xs)
    (check-series-columns! caller ds ys)
    (check-pair-column-kinds! caller ds xs)
    (check-pair-column-kinds! caller ds ys)
    (let [named (distinct (remove #(= % default-series-label)
                                  [(:as x-spec) (:as y-spec)]))]
      (when (next named)
        (throw (ex-info (str caller " was given a series on :x named :as "
                             (pr-str (:as x-spec)) " and a series on :y"
                             " named :as " (pr-str (:as y-spec)) ". The"
                             " two are pivoted together into one key"
                             " column, so name it once.")
                        {:caller caller :as (vec named)}))))
    (let [label (series-pairs-label x-spec y-spec)
          {xv :x yv :y} series-pair-value-columns
          consumed (set (concat xs ys))
          remaining (vec (remove consumed (tc/column-names ds)))
          clashes (filterv (set remaining) [label xv yv])]
      (when (seq clashes)
        (throw (ex-info (str caller " cannot pivot these columns in pairs:"
                             " the pivot names its key column "
                             (pr-str label) " and its value columns "
                             (pr-str xv) " and " (pr-str yv) ", and the"
                             " data already has " (pr-str clashes) "."
                             " Rename that column in the data"
                             (when (some #{label} clashes)
                               ", or name the key column with :as")
                             ".")
                        {:caller caller :clashes clashes}))))))

(defn- pivot-series-pairs
  "Pivot a series on `:x` and a series on `:y` together, pairing their
   columns in order: the first `:x` column with the first `:y` column,
   and so on. Each pair becomes a group of rows labelled with both
   names, `\"a / c\"`, holding the pair's values in two value columns.
   Returns the new dataset. Checked by `check-series-pairs!`."
  [ds x-spec y-spec]
  (let [{xs :cols} x-spec
        {ys :cols} y-spec
        label (series-pairs-label x-spec y-spec)
        {xv :x yv :y} series-pair-value-columns
        consumed (set (concat xs ys))
        remaining (vec (remove consumed (tc/column-names ds)))
        n (tc/row-count ds)
        ;; Through the formatter the legend gives a single series' key
        ;; column, so `:time-a` reads `time a` in both legends.
        col-label defaults/fmt-category-label
        kept (tc/select-columns ds remaining)
        parts (mapv (fn [x y]
                      (-> kept
                          (tc/add-column label (vec (repeat n (str (col-label x) " / " (col-label y)))))
                          (tc/add-column xv (ds x))
                          (tc/add-column yv (ds y))))
                    xs ys)
        joined (apply tc/concat parts)
        ;; A pair missing either value has no place to be drawn. The
        ;; drop is named, as the single-series pivot names its own.
        pivoted (tc/drop-missing joined [xv yv])]
    (defaults/report-removed-rows!
     (- (tc/row-count joined) (tc/row-count pivoted))
     (str "with a missing value among the columns read as series in pairs ("
          (str/join ", " (map pr-str (concat xs ys))) ")"))
    pivoted))

(defn check-series!
  "Throw where the series written in mapping `m` cannot be pivoted out
   of `data` -- the checks the pivot makes, without pivoting. Run by a
   `lay-*` call that has the data, so the error names the call, and by
   the draft before it pivots."
  [caller data m]
  (let [ks (filterv #(series-mapping (get m %)) series-aesthetics)
        specs (into {} (map (fn [k] [k (series-mapping (get m k))])) ks)
        ds (coerce-dataset data)]
    (cond
      (next ks) (check-series-pairs! caller ds (:x specs) (:y specs))
      (seq ks) (check-single-series! caller ds (get specs (first ks))))))

(defn- series-colour-type
  "`{:color-type :categorical}` where the key column the pivot invents
   would otherwise be read as a continuous colour, and `nil` where it
   would not.

   `tc/pivot->longer` reads a column name that looks like a number as
   one, so a wide table of years gives an `:int64` key column. The
   column holds column names whatever its type, and a colour infers
   continuous or categorical from the type, so the years were drawn as
   a gradient. The pivot knows what the column holds and says so here.

   Written only where the column is numeric, so an ordinary series
   carries no `:color-type`, and only where the writer has said nothing
   about the colour's type in the leaf's mapping or the layer's."
  [leaf-mapping layer-mapping pivoted label group-key]
  (when (and (= :color group-key)
             (not (contains? leaf-mapping :color-type))
             (not (contains? (or layer-mapping {}) :color-type))
             (let [col (get pivoted label)]
               (and col (not (contains? #{:string :keyword}
                                        (dtype/elemwise-datatype col))))))
    {:color-type :categorical}))

(defn- series-in
  "The aesthetics of mapping `m` that hold a series."
  [m]
  (filterv #(series-mapping (get m %)) series-aesthetics))

(defn- pivot-mapping
  "Pivot the series written in `m` out of `data`. Returns `{:data
   pivoted :values {aesthetic column} :label key-column :cols [...]}`."
  [caller data m]
  (let [ks (series-in m)
        specs (into {} (map (fn [k] [k (series-mapping (get m k))])) ks)
        pairs? (next ks)
        label (if pairs?
                (or (first (remove #(= % default-series-label) (map :as (vals specs))))
                    default-series-label)
                (:as (get specs (first ks))))
        ds (coerce-dataset data)
        _ (check-series! caller ds m)
        pivoted (if pairs?
                  (pivot-series-pairs ds (:x specs) (:y specs))
                  (pivot-series ds (get specs (first ks))))
        ;; `:from` rather than `:column`, so that a series reading
        ;; through a scale names the invented column as a plain one does.
        ;; A title names the columns the writer wrote, never the one the
        ;; pivot invents: the axis is titled by the series' columns,
        ;; joined, as an axis drawing those columns in separate layers
        ;; is. A `:label` the writer gave the scale wins.
        value-mapping (fn [k]
                        (let [col (if pairs?
                                    (get series-pair-value-columns k)
                                    series-value-column)
                              sc (:scale (get specs k))
                              title (str/join ", " (map defaults/fmt-name (:cols (get specs k))))]
                          (cond
                            (nil? sc) {:from col :scale {:label title}}
                            (and (map? sc) (not (contains? sc :label))) {:from col :scale (assoc sc :label title)}
                            :else {:from col :scale sc})))
        ;; The aesthetics whose title the pivot wrote, rather than the
        ;; writer -- an option the writer sets outranks those.
        titled (set (filter #(let [sc (:scale (get specs %))]
                               (or (nil? sc) (and (map? sc) (not (contains? sc :label)))))
                            ks))]
    {:data pivoted
     :values (into {} (map (fn [k] [k (value-mapping k)])) ks)
     :titled titled
     :label label
     ;; Whether the writer named the key column with `:as`; a name the
     ;; pivot chose titles no legend.
     :named? (not= label default-series-label)
     :cols (vec (mapcat :cols (vals specs)))}))

(defn- layer-series-caller
  [l]
  (let [t (:layer-type l)]
    (if (keyword? t) (str "pj/lay-" (name t)) "pj/lay")))

(defn- leaf-series
  "What pivots the leaf's own data, as `{:from :pose|:layers :cols [...]
   :idxs [...]}` -- the series on the leaf's mapping, or the series the
   layers drawing from the leaf's data write -- or nil where nothing
   does. Throws where two series would pivot one dataset: a series on
   the pose and one on a layer, or two layers writing different series.
   Two layers writing the same series share one pivot."
  [leaf]
  (let [leaf-mapping (or (:mapping leaf) {})
        layers (vec (or (:layers leaf) []))
        on-leaf (series-in leaf-mapping)
        on-layers (vec (keep-indexed (fn [i l] (when (and (seq (series-in (:mapping l)))
                                                          (nil? (:data l)))
                                                 i))
                                     layers))
        spec-of (fn [m] (select-keys m (series-in m)))
        cols-of (fn [m] (vec (mapcat #(:cols (series-mapping (get m %))) (series-in m))))]
    (cond
      (and (seq on-leaf) (seq on-layers))
      (throw (ex-info (str (layer-series-caller (layers (first on-layers)))
                           " reads a series on "
                           (pr-str (first (series-in (:mapping (layers (first on-layers))))))
                           ", and the pose it is on already reads one on "
                           (pr-str (first on-leaf)) ". A series pivots the data"
                           " into one value column, and two pivots have no"
                           " shared shape. Read one of them, or build the"
                           " layer on a pose of its own.")
                      {:on-pose (first on-leaf)}))

      (seq on-leaf)
      {:from :pose :cols (cols-of leaf-mapping)}

      (seq on-layers)
      (let [specs (distinct (map #(spec-of (:mapping (layers %))) on-layers))]
        (when (next specs)
          (throw (ex-info (str (layer-series-caller (layers (last on-layers)))
                               " reads " (pr-str (cols-of (:mapping (layers (last on-layers)))))
                               " as series, and a layer on the same pose already reads "
                               (pr-str (cols-of (:mapping (layers (first on-layers)))))
                               ". A series pivots the pose's data into one value"
                               " column, and two pivots have no shared shape. Give"
                               " each series a pose of its own and arrange the"
                               " poses.")
                          {:series (vec specs)})))
        {:from :layers :cols (cols-of (:mapping (layers (first on-layers)))) :idxs on-layers}))))

(defn check-series-leaf!
  "Throw where the series on a leaf cannot be pivoted together with the
   rest of it: two series pivoting one dataset (see `leaf-series`), or
   the pose's own mapping naming a column the pivot consumes -- that
   aesthetic would apply to the pivoted rows, which no longer hold the
   column. A layer naming such a column is not refused: it reads the
   data as written, before the pivot (`pivot-leaf-series`). Run by every
   `lay-*` call on a leaf, so the call that makes the conflict is the
   one that reports it, and by the draft."
  [leaf]
  (when-let [{:keys [from cols]} (leaf-series leaf)]
    (let [consumed (set cols)
          named (concat (when (= :layers from)
                          [["this pose's mapping" (or (:mapping leaf) {})]])
                        (when (= :pose from)
                          [["this pose's mapping" (dissoc (:mapping leaf) :x :y)]]))
          hits (vec (for [[what mapping] named
                          [k v] mapping
                          :let [src (mapping-source v)]
                          :when (and (not (map? src)) (consumed src))]
                      [what k src]))]
      (when (seq hits)
        (let [[what k col] (first hits)]
          (throw (ex-info (str what " names " (pr-str col) " on " k ", a column that"
                               " a series on this pose has already read: the series"
                               " pivots " (pr-str cols) " into a key column and a"
                               " value column, so the rows it draws have no column "
                               (pr-str col) ". To draw " (pr-str col) " as well, name"
                               " it in a lay-* call -- a layer reads the data as"
                               " written -- or drop the mapping that names it.")
                          {:columns cols :conflicts hits})))))))

(defn- read-series-column
  "A layer beside a series that names one of the series' own columns --
   points on `:revenue` beside lines for `[:revenue :cost]`. On the
   aesthetic the series is on, the layer reads the series' pivoted rows
   for that column: the value column, kept to the rows whose key is the
   column, so it shares the series' panel, axis and colour. On another
   aesthetic it reads the data as written, on the series' panel. A layer
   with data or a series of its own, or naming no such column, is
   returned as it is. `keyed` is what the series' own layer maps to the
   key column -- `{:color label}` -- added beneath the layer's mapping,
   so the layer's marks take the colour of the column's series.

   A layer naming a column of the data as written on every aesthetic
   the series is on -- points on `:cyl` beside a series of `[:hwy
   :cty]` -- draws none of the series, so it reads the data as written too: on the
   pivoted rows each of its rows would be drawn once per series column.
   `unkeyed` is the key column's mapping a series on the pose hands
   down to every layer, written as `nil`, which that layer takes so
   that it does not read a column the data as written does not hold."
  [layer pv consumed wide keyed unkeyed]
  (let [m (:mapping layer)
        hits (into {} (for [[k v] m
                            :let [src (mapping-source v)]
                            :when (and (not (map? src)) (consumed src))]
                        [k src]))
        ;; A column the data as written holds. A layer may instead
        ;; name the columns the pivot invents -- `(pj/lay-point
        ;; :quarter :value {:color :series})` -- and reads the pivoted
        ;; rows for them.
        wide-columns (when wide (set (tc/column-names (coerce-dataset wide))))
        own-column? (fn [k] (let [src (mapping-source (get m k))]
                              (and (some? src) (not (map? src))
                                   (contains? wide-columns src))))]
    (cond
      (or (:data layer) (seq (series-in m)) (nil? wide))
      layer

      (empty? hits)
      (if (every? own-column? (keys (:values pv)))
        (assoc layer
               :data wide
               :mapping (merge unkeyed m))
        layer)

      :else
      (let [on-series (select-keys hits (keys (:values pv)))
            col (first (vals on-series))
            label (:label pv)
            long-data (:data pv)
            nm #(if (keyword? %) (name %) (str %))
            ;; The key the pivot gave this column; none for series in
            ;; pairs, whose keys name two columns.
            key-of (when col
                     (some #(when (= (nm %) (nm col)) %) (distinct (long-data label))))]
        (if (and key-of
                 (= 1 (count (set (vals on-series))))
                 (= (set (keys hits)) (set (keys on-series))))
          (do
            (assoc layer
                   :data (tc/select-rows long-data (rows-where (long-data label) key-of))
                   :mapping (merge keyed m (into {} (for [k (keys on-series)] [k ((:values pv) k)])))))
          (assoc layer :data wide))))))

(defn- untitle-where-written
  "`pv` with the axis title the pivot wrote taken off each axis whose
   title the pose's options write (`:x-label`, `:y-label`). A title the
   writer gave the series' scale is kept: a scale spec outranks an
   option, as everywhere."
  [pv opts]
  (reduce (fn [pv k]
            (if (contains? opts ({:x :x-label :y :y-label} k))
              (update-in pv [:values k] (fn [v]
                                          (let [sc (dissoc (:scale v) :label)]
                                            (if (empty? sc) (:from v) (assoc v :scale sc)))))
              pv))
          pv
          (:titled pv)))

(defn- title-columns-beside
  "`pv` with the columns that layers beside the series draw on its axis
   added to the axis title the pivot wrote: an axis is titled by every
   column drawn on it. `beside-of` answers those columns for an
   aesthetic."
  [pv beside-of]
  (reduce (fn [pv k]
            (let [extra (remove (set (:cols pv)) (beside-of k))]
              (if (and (seq extra) (string? (get-in pv [:values k :scale :label])))
                (update-in pv [:values k :scale :label]
                           #(str/join ", " (cons % (map defaults/fmt-name extra))))
                pv)))
          pv
          (:titled pv)))

(declare note-columns-of-different-size!)

(defn pivot-leaf-series
  "A leaf whose series are pivoted: the columns a series reads become a
   key column and a value column, and the mapping that wrote the series
   names those two instead -- the key column on `:color`, or on `:group`
   where that mapping writes `:color` itself, so the series stay
   separate marks either way.

   A series on the leaf's mapping is read by every layer below it; a
   series on a layer is that layer's. The pivoted data replaces the data
   the series was read from: the leaf's, or the layer's own `:data`.
   Checked first by `check-series-leaf!`.

   A layer with no data or series of its own that names a column the
   series consumes -- points on `:revenue` beside lines for `[:revenue
   :cost]` -- reads that column's rows of the series, in the series'
   colour (`read-series-column`).

   `leaf` carries its full context, as `elaborate-leaf` receives it.
   Idempotent: a pivoted leaf names no series."
  [leaf]
  (check-series-leaf! leaf)
  (let [leaf-mapping (or (:mapping leaf) {})
        layers (vec (or (:layers leaf) []))
        no-data (fn [k]
                  (ex-info (str "A pose reads several columns on " (pr-str k)
                                " as series and has no data to pivot. Pass"
                                " the data to the lay-* call, or put it on"
                                " the pose with pj/pose.")
                           {:aesthetic k}))
        ;; The key column's legend lists the columns, so it is titled
        ;; only where the writer named the key column with `:as`.
        rewrite (fn [m {:keys [data values label named?]} outer]
                  (let [group-key (if (contains? m :color) :group :color)
                        keyed (if (and (= :color group-key) (not named?))
                                {:from label :scale {:label ""}}
                                label)]
                    (merge (apply dissoc m (series-in m))
                           values {group-key keyed}
                           (series-colour-type (or outer m) (when outer m) data label group-key))))
        ;; A layer with data of its own pivots that data.
        layers (mapv (fn [l]
                       (if (and (:data l) (seq (series-in (:mapping l))))
                         (let [pv (-> (pivot-mapping (layer-series-caller l) (:data l) (:mapping l))
                                      (untitle-where-written (:opts leaf)))]
                           (assoc l :data (:data pv) :mapping (rewrite (:mapping l) pv leaf-mapping)))
                         l))
                     layers)
        leaf (assoc leaf :layers layers)]
    (if-let [{:keys [from idxs cols]} (leaf-series leaf)]
      (let [wide (:data leaf)
            ;; The columns of a series share an axis as the layers of a
            ;; panel do, so the same Note is due before they are pivoted
            ;; into one value column, where the columns can no longer
            ;; be told apart.
            ;; A layer beside the series, naming another column on that
            ;; axis, is counted here too: after the pivot the series is
            ;; one column, `:value`, which names nothing the writer
            ;; wrote.
            series-m (if (= :pose from) leaf-mapping (:mapping (layers (first idxs))))
            wide-cols (when wide (set (tc/column-names (coerce-dataset wide))))
            beside-of (fn [k]
                        (when wide
                          (distinct
                           (for [[i l] (map-indexed vector layers)
                                 :when (and (not (contains? (set idxs) i))
                                            (nil? (:data l))
                                            (not= :drawing-area (:in (:mapping l))))
                                 :let [c (mapping-source (get (:mapping l) k))]
                                 :when (and c (wide-cols c))]
                             c))))
            _ (when wide
                (doseq [k (series-in series-m)
                        :let [series-cols (:cols (series-mapping (get series-m k)))]
                        :when (#{:x :y} k)]
                  (note-columns-of-different-size!
                   k
                   (zipmap (distinct (concat series-cols (beside-of k))) (repeat wide))
                   (cons leaf-mapping (map :mapping layers)))))
            ;; Which layers take the default colour, read before the
            ;; pivot gives the series' own layers the key column.
            default-colors (default-color-layers leaf)
            beside-default (fn [i l]
                             (let [label (str/join
                                          " / "
                                          (for [k [:x :y]
                                                :when (seq (series-in (select-keys series-m [k])))
                                                :let [c (mapping-source (get (:mapping l) k))]
                                                :when (some? c)]
                                            (defaults/fmt-name c)))]
                               (when (and (nth default-colors i nil) (seq label))
                                 label)))
            pivoted (if (= :pose from)
                      (let [pv (-> (pivot-mapping "pj/pose"
                                                  (or wide (throw (no-data (first (series-in leaf-mapping)))))
                                                  leaf-mapping)
                                   (title-columns-beside beside-of)
                                   (untitle-where-written (:opts leaf)))]
                        (assoc leaf
                               :data (:data pv)
                               :mapping (rewrite leaf-mapping pv nil)
                               ::pivot pv))
                      (let [first-l (layers (first idxs))
                            pv (-> (pivot-mapping (layer-series-caller first-l)
                                                  (or wide
                                                      (throw (no-data (first (series-in (:mapping first-l))))))
                                                  (:mapping first-l))
                                   (title-columns-beside beside-of)
                                   (untitle-where-written (:opts leaf)))]
                        (assoc leaf
                               :data (:data pv)
                               :layers (reduce (fn [ls i]
                                                 (update-in ls [i :mapping] rewrite pv leaf-mapping))
                                               layers idxs)
                               ::pivot pv)))
            pv (::pivot pivoted)
            ;; A series on the pose's mapping keys every layer already;
            ;; a series on a layer keys only that layer.
            sm (when (= :layers from) (:mapping ((:layers pivoted) (first idxs))))
            keyed (merge (into {} (for [[k v] sm :when (= (mapping-source v) (:label pv))] [k v]))
                         (select-keys sm [:color-type]))
            ;; What a series on the pose's mapping added to that mapping
            ;; for the key column, as `nil`s.
            unkeyed (when (= :pose from)
                      (let [pm (:mapping pivoted)]
                        (into {}
                              (for [[k v] pm
                                    :when (or (= (mapping-source v) (:label pv))
                                              (and (= :color-type k)
                                                   (not (contains? leaf-mapping :color-type))))]
                                [k nil]))))]
        (-> pivoted
            (dissoc ::pivot)
            (update :layers (fn [ls]
                              (vec (map-indexed
                                    (fn [i l]
                                      (if (contains? (set idxs) i)
                                        l
                                        ;; A layer beside the series, drawing a
                                        ;; column of its own, is one more column
                                        ;; on the series' axis: with no `:color`
                                        ;; specified it joins the series' legend
                                        ;; as an entry named after its column,
                                        ;; as it would beside one-column layers
                                        ;; (`default-color-layers`).
                                        (read-series-column
                                         l pv (set cols) wide keyed
                                         (cond-> unkeyed
                                           (beside-default i l)
                                           (assoc :color
                                                  (default-color (beside-default i l)
                                                                 (if (:named? pv)
                                                                   true
                                                                   {:label ""})))))))
                                    ls))))))
      leaf)))

(defn- layer-axis-draws
  "What a layer of `leaf` draws on `axis`, for comparing the layers that
   share its panel: `{:computes c}` where its stat computes what it draws
   there (a count or a density on `:y`, `stat/computes`),
   `{:column c :type t}` for a column (`resolve/column-type`), or nil
   where the layer draws a written value there or nothing."
  [leaf layer axis]
  (let [leaf-mapping (or (:mapping leaf) {})
        data (coerce-dataset (or (:data layer) (:data leaf)))
        computed (layer-computes layer leaf-mapping (:data leaf) axis)
        src (mapping-source (get (merge leaf-mapping (:mapping layer)) axis))]
    (cond
      computed {:computes computed}
      (and src data (contains? (set (tc/column-names data)) src))
      (let [m (merge leaf-mapping (:mapping layer))
            ;; A type the writer gave with `:x-type` or `:y-type` is the
            ;; type the axis reads. (A categorical scale written on the
            ;; mapping is not read here: the plan does not yet place the
            ;; numbers of such a column among another layer's categories.)
            written (get m ({:x :x-type :y :y-type} axis))]
        {:column src :type (or written (resolve/column-type data src))}))))

(def ^:private small-share-of-axis
  "The part of a shared axis below which a column counts as drawn flat:
   a column whose own extent is less than one twentieth of the axis."
  1/20)

(defn- note-columns-of-different-size!
  "Print a Note where numeric columns drawn together on `axis` differ so
   much in size that one of them takes less than `small-share-of-axis`
   of it -- highway mileage beside the model year, drawn as a flat band
   at the foot of an axis that runs to 2008.

   The columns share the axis because the layers of a pose share its
   panel, or because a series names them together, and the picture is
   seldom the one meant.

   The Note belongs to the inference of the axis's extent: it reports
   what fitting one linear axis to these columns gives. So it is
   printed only where that inference runs -- not where the axis has a
   `:domain` specified, where no extent is fitted, nor under a log
   scale, where the columns are not compared on a linear axis
   (`mappings` are every mapping that could carry either). It reads the
   data and the scale specified for the axis, nothing else.

   `col->data` maps each column to the dataset it is read from."
  [axis col->data mappings]
  (let [scale-of (fn [m] (let [v (get m axis)] (when (map? v) (:scale v))))
        settled? (some (fn [m] (let [sc (scale-of m)]
                                 (or (= :log sc)
                                     (and (map? sc) (or (contains? sc :domain)
                                                        (= :log (:type sc)))))))
                       mappings)
        extents (for [[col data] col->data
                      :let [ds (coerce-dataset data)
                            vs (when ds (filter number? (col-values ds col)))]
                      :when (seq vs)]
                  [col (double (reduce min vs)) (double (reduce max vs))])]
    (when (and (not settled?) (next extents))
      (let [lo (reduce min (map second extents))
            hi (reduce max (map #(nth % 2) extents))
            span (- hi lo)
            flat (filter (fn [[_ a b]] (and (< a b) (< (- b a) (* small-share-of-axis span))))
                         extents)
            fmt (fn [v] (if (== v (Math/rint v)) (str (long v)) (format "%.3g" v)))]
        (when (seq flat)
          (println (str "Note: on " axis " one axis holds "
                        (str/join ", " (map (fn [[c a b]] (str (pr-str c) " (" (fmt a) " to " (fmt b) ")"))
                                            extents))
                        ", which together span " (fmt lo) " to " (fmt hi) ", so "
                        (str/join " and " (map (comp pr-str first) flat))
                        (if (next flat) " are each" " is")
                        " drawn within a small part of it. To give each column an"
                        " axis of its own, draw a panel per column: (pj/facet pose "
                        (pr-str {axis (mapv first extents)}) ").")))))))

(defn check-shared-panel!
  "Report layers of a leaf that share its one panel and disagree about
   what an axis holds.

   The layers of a pose share its panel, so each axis is one scale for
   all of them. Columns of different kinds on one axis -- a category
   beside a number -- cannot be placed on one scale, and drawing them
   anyway placed the numbers as category places: an error, naming the
   layers and the two ways to separate panels. A count or a density
   beside a column on `:y` can be placed, and so can a count beside a
   density, but the axis then reads in two units: a Note, with the same
   advice. Numeric columns of very different
   size can be placed too, and one of them is then drawn flat: a Note
   (`note-columns-of-different-size!`)."
  [leaf]
  (let [layers (:layers leaf)
        caller layer-series-caller
        advice (fn [axis cols]
                 (str " To draw them on separate panels, give each its own pose"
                      " and arrange the poses with pj/arrange"
                      (when (next cols)
                        (str ", or split the pose over the columns: (pj/facet pose "
                             (pr-str {axis (vec cols)}) ")"))
                      "."))]
    (when (next layers)
      (doseq [axis [:x :y]
              :let [draws (keep (fn [l] (when-let [d (layer-axis-draws leaf l axis)]
                                          (assoc d :caller (caller l))))
                                layers)
                    columns (filter :column draws)
                    stats (filter :computes draws)
                    describe (fn [d] (str (:caller d) " draws "
                                          (if (:column d)
                                            (str (pr-str (:column d)) " (" (name (:type d)) ")")
                                            (str "a " (name (:computes d))))))]]
        (cond
          (next (distinct (map :type columns)))
          (throw (ex-info (str "The layers of this pose share one panel, and on " axis
                               " they draw columns of different kinds: "
                               (str/join ", " (map (comp describe first) (vals (group-by :type columns)))) ". "
                               "One axis cannot place "
                               (str/join " and " (map {:categorical "categories" :numerical "numbers"
                                                       :temporal "dates"}
                                                      (distinct (map :type columns))))
                               " together."
                               (when (= #{:categorical :numerical} (set (map :type columns)))
                                 (str " To read the numeric column as categories on this"
                                      " axis, set " (if (= :x axis) ":x-type" ":y-type")
                                      " :categorical on its layer."))
                               (advice axis (distinct (map :column columns))))
                          {:axis axis :draws (vec draws)}))

          (and (seq stats) (seq columns))
          (println (str "Note: the layers of this pose share one panel, and on " axis " "
                        (str/join ", " (map describe (concat (take 1 stats) (take 1 columns))))
                        ", so the axis reads in two units."
                        (advice axis nil)))

          ;; A count beside a density: both computed, and not alike.
          (next (distinct (map :computes stats)))
          (println (str "Note: the layers of this pose share one panel, and on " axis " "
                        (str/join ", " (map (comp describe first)
                                            (vals (group-by :computes stats))))
                        ", so the axis reads in two units."
                        (when (some #{:count} (map :computes stats))
                          (str " To draw a histogram as a density, write"
                               " {:normalize :density} on its layer."))
                        (advice axis nil)))

          ;; Not where a series has been pivoted into its value column:
          ;; `pivot-leaf-series` printed the Note for it, naming the
          ;; columns as written. Nor for a layer placed in the drawing
          ;; area, whose values are drawing units and not a column of
          ;; the axis.
          (and (next (distinct (map :column columns)))
               (= [:numerical] (distinct (map :type columns)))
               (not-any? #{series-value-column} (map :column columns)))
          (note-columns-of-different-size!
           axis
           (into {} (for [l layers
                          :when (not= :drawing-area (:in (:mapping l)))
                          :let [d (layer-axis-draws leaf l axis)]
                          :when (:column d)]
                      [(:column d) (or (:data l) (:data leaf))]))
           (cons (:mapping leaf) (map :mapping layers))))))))

(defn report-panel-aesthetic-on-layer
  "Report a panel aesthetic written on a layer.

   `:row` and `:col` send a distinction to panels, and a facet
   multiplies the whole leaf rather than one layer of it, so a panel
   aesthetic on a layer names a division the draft cannot build. It is
   reported here rather than dropped, which is what a mapping key with
   no reading used to do."
  [layer-mapping]
  (let [panel-keys (filter layer-mapping [:col :row])]
    (when (seq panel-keys)
      (throw (ex-info (str "A panel aesthetic " (vec panel-keys) " was written on a"
                           " layer. A facet divides every layer of the pose, so"
                           " write it on the pose: (pj/facet my-pose "
                           (pr-str (get layer-mapping (first panel-keys)))
                           "), or put it in the pose's mapping.")
                      {:layer-mapping layer-mapping
                       :panel-aesthetics (vec panel-keys)}))))
  layer-mapping)

(defn unread-positions
  "The `:x` and `:y` that a composite in `pose`'s tree writes in its own
   `:mapping` and that no leaf below reads, because each leaf, or a pose
   between, writes its own. Returns `[[path k] ...]`, `path` the composite's
   `:poses` indices from the root."
  [pose]
  (letfn [(reaches? [node k]
            (cond (nil? node) false
                  (contains? (:mapping node) k) false
                  (composite? node) (boolean (some #(reaches? % k) (:poses node)))
                  :else true))
          (walk [node path]
            (when (composite? node)
              (concat
               (for [k [:x :y]
                     ;; A scale written with no column names nothing
                     ;; to draw; it reaches each cell's own column.
                     :when (and (some? (mapping-source (get (:mapping node) k)))
                                (not-any? #(reaches? % k) (:poses node)))]
                 [path k])
               (mapcat (fn [i child] (walk child (conj path i)))
                       (range) (:poses node)))))]
    (vec (walk pose []))))

(defn report-unread-positions!
  "Warn about an `:x` or `:y` written on a composite that no cell reads.
   `pj/pose` writes the composite's own mapping, and a cell that maps its
   own `:x` or `:y` overrides it, so without this the write changes
   nothing and says nothing."
  [pose]
  (doseq [[path k] (unread-positions pose)]
    (println (str "Warning: a composite pose maps " k " to "
                  (pr-str (mapping-source (get-in pose (concat (mapcat (fn [i] [:poses i]) path) [:mapping k]))))
                  ", and every cell below it maps its own " k
                  ", so it is not drawn. To change the columns of the cells,"
                  " arrange them anew: (pj/arrange data [[:a :b] [:c :d]]).")))
  pose)

;; ---- Sharing and chrome decided from the listed cells ----
;;
;; Provisional (PR #61; Rule 1 agreed 2026-10-09): cells that are the
;; parts of one declared split share each axis they can, and an axis
;; they share on one column is ticked and titled once, as a facet does.
;; Both are read from the listed pose, so cells written by hand and cells
;; generated by `expand-variation` draw the same picture. A written
;; `:share-scales`, `:grid-axis-titles` or per-cell `:suppress-*` wins.

(defn default-share-axes
  "The axes the cells of a composite do share by default -- two or more
   cells draw one column there (or count one column), and the cells of
   each bucket agree on the kind of scale. Sharing is
   by column, so cells reading different columns on an axis keep their
   own extents, and an axis where every cell draws a column of its own
   is not in the default: written out, the default changes nothing and
   draws no message (`warn-share-scales-without-effect!`). An axis the
   cells cannot share is left alone, with no message: the writer did
   not ask for it."
  [composite]
  (let [subtree (vec (remove :plotje/empty-cell (resolve-tree composite)))]
    (set (filter (fn [axis]
                   (try (validate-share-bucket-compatibility! subtree {axis :same-column})
                        (boolean (some #(next (:pairs %))
                                       (share-buckets subtree axis :same-column)))
                        (catch clojure.lang.ExceptionInfo _ false)))
                 [:x :y]))))

(defn share-spec
  "How a composite's cells share each axis, as the map
   `defaults/share-scales-spec` describes: the written `:share-scales`
   (in `:opts`, or at the top of a hand-built composite), or -- where
   none is written and the cells are the parts of one declared split --
   every axis they can share, each column on its own. Read by
   `shared-axis-groups` and by `infer-chrome`, so the domains and the
   chrome agree."
  [pose]
  (cond
    (contains? (:opts pose) :share-scales) (defaults/share-scales-spec (get-in pose [:opts :share-scales]))
    (contains? pose :share-scales) (defaults/share-scales-spec (:share-scales pose))
    (declared-split? pose) (zipmap (default-share-axes pose) (repeat :same-column))
    :else {}))

(defn share-axes
  "The axes a composite's cells share in some way -- the axes
   `share-spec` names."
  [pose]
  (set (keys (share-spec pose))))

(defn unanimous-column
  "The column every cell of `leaves` draws on `axis`, or nil where they
   do not all draw the same one. Cells left empty do not vote."
  [leaves axis]
  (let [cols (map #(get (leaf-columns %) axis)
                  (remove :plotje/empty-cell leaves))]
    (when (and (seq cols) (every? some? cols) (apply = cols))
      (first cols))))

(defn- infer-axis-chrome
  "Where the cells all draw one column on an axis, that axis is titled
   once beside the grid (`:grid-axis-titles`). On a shared axis, a cell
   drops its tick labels where a cell with rows below it (x) or to its
   left (y) shares that axis with it (`share-buckets`: the same
   column, the same count, or what `:share-scales` puts together) --
   read from where the
   layout places the cells, so a row, a column, a grid and a matrix are
   treated alike: a facet ticks its outer panels, a scatter plot matrix
   ticks its bottom row and left column. An axis left free keeps its
   ticks in every cell, since each cell has its own scale. A cell left
   empty draws nothing and is skipped. A written `:grid-axis-titles` is
   kept as written; a cell's own `:suppress-*` options win over the
   inferred ones. A cell below or beside counts only where it lines up:
   the same extent across (x) or down (y)."
  [composite]
  (if (contains? composite :grid-axis-titles)
    composite
    (let [spec (share-spec composite)
          leaves (resolve-tree composite)
          ;; The buckets each cell is in on a shared axis, by path: two
          ;; cells one scale draws are in a bucket together -- the same
          ;; column, the same count, or what the share spec puts
          ;; together (`share-buckets`, which the domains are shared
          ;; by).
          buckets (into {}
                        (for [[axis how] spec]
                          [axis (reduce (fn [m [i {:keys [pairs]}]]
                                          (reduce (fn [m [_ leaf]]
                                                    (update m (:path leaf) (fnil conj #{}) i))
                                                  m pairs))
                                        {}
                                        (map-indexed vector (share-buckets leaves axis how)))]))
          together? (fn [axis a b]
                      (boolean (some (get-in buckets [axis (:path a)] #{})
                                     (get-in buckets [axis (:path b)] #{}))))
          ;; A shared axis every cell draws the same thing on -- one
          ;; column, which a cell's layers may name where its mapping
          ;; does not, or one count -- is titled once as well.
          live-leaves (remove :plotje/empty-cell leaves)
          one-key? (fn [axis]
                     (let [ks (map #(keys (share-key-buckets [%] axis)) live-leaves)]
                       (and (contains? spec axis)
                            (seq ks)
                            (every? #(and (= 1 (count %)) (some? (first %))) ks)
                            (apply = ks))))
          once-x? (or (unanimous-column leaves :x) (one-key? :x))
          once-y? (or (unanimous-column leaves :y) (one-key? :y))
          rects (compute-layout composite [0 0 1000 1000])
          live (vec (for [leaf live-leaves]
                      {:rect (rects (:path leaf)) :path (:path leaf)}))
          ;; Only a cell in the same column of the grid -- the same
          ;; horizontal extent -- carries ticks a cell above it can read;
          ;; a wider panel below, whose axis does not line up, does not.
          same? (fn [a b] (< (Math/abs (- (double a) (double b))) 1e-6))
          below? (fn [[x y w _] [x2 y2 w2 _]]
                   (and (> y2 y) (same? x x2) (same? w w2)))
          left? (fn [[x y _ h] [x2 y2 _ h2]]
                  (and (< x2 x) (same? y y2) (same? h h2)))
          chrome (fn [leaf]
                   (let [rect (rects (:path leaf))]
                     (cond-> {}
                       once-x? (assoc :suppress-x-label true)
                       (some #(and (together? :x leaf %) (below? rect (:rect %))) live)
                       (assoc :suppress-x-ticks true)
                       once-y? (assoc :suppress-y-label true)
                       (some #(and (together? :y leaf %) (left? rect (:rect %))) live)
                       (assoc :suppress-y-ticks true))))]
      (cond-> (reduce (fn [c leaf]
                        (let [ch (when-not (:plotje/empty-cell leaf) (chrome leaf))]
                          (if (seq ch)
                            (update-in c (vec (interleave (repeat :poses) (:path leaf)))
                                       update :opts #(merge ch %))
                            c)))
                      composite
                      leaves)
        (or once-x? once-y?)
        (assoc :grid-axis-titles (cond-> #{} once-x? (conj :x) once-y? (conj :y)))))))

(defn- cell-grid
  "The cells of a composite as rows of cells, for the two shapes strips
   are drawn against: a flat list of cells (a row, or a column under
   `:vertical`), and rows of cells under a vertical root, as
   `pj/arrange` builds them. Nil for any other shape."
  [composite]
  (let [ps (:poses composite)
        leafish? #(not (:poses %))
        dir (or (get-in composite [:layout :direction]) :horizontal)]
    (cond
      (= :matrix dir) nil
      (every? leafish? ps) (if (= :vertical dir) (mapv vector ps) [(vec ps)])
      (and (= :vertical dir)
           (every? #(and (seq (:poses %)) (every? leafish? (:poses %))) ps)
           (apply = (map #(count (:poses %)) ps)))
      (mapv (comp vec :poses) ps))))

(defn- infer-strips
  "Strips read from the filters the cells draw (`:plotje/where`, kept by
   `elaborate`): a column of cells is labelled by the filter values all
   its cells hold in common, and a row likewise, leaving out the values
   every cell holds -- so a row of species cells is labelled by species,
   and a grid by one value across the top and the other down the side.
   Labels are formatted as a facet's are (`facet-label`).

   In a grid -- several rows and several columns of cells, none empty or
   drawn on its side -- a side the filters do not label is labelled by
   the column the cells draw on its axis, where every cell of a grid
   column draws one `:x` column and they differ across the grid (the
   matrix's strips), and likewise rows by `:y`; the cells then drop that
   axis title, which the strip carries. A single row or column of cells
   told apart by their columns gets no strip: its axis titles name them.
   A written `:grid-strip-labels` is kept."
  [composite]
  (if (contains? composite :grid-strip-labels)
    composite
    (if-let [grid (cell-grid composite)]
      (let [where-of #(or (:plotje/where %) {})
            entries #(set (where-of %))
            all-cells (apply concat grid)
            common (apply clojure.set/intersection (map entries all-cells))
            key-order (vec (distinct (mapcat (comp keys where-of) all-cells)))
            label (fn [cells]
                    (let [m (into {} (clojure.set/difference
                                      (apply clojure.set/intersection (map entries cells))
                                      common))]
                      (when (seq m)
                        (facet-label (keep #(when (contains? m %) (m %)) key-order)))))
            n-rows (count grid)
            n-cols (count (first grid))
            col-labels (when (> n-cols 1)
                         (mapv (fn [ci] (label (map #(nth % ci) grid))) (range n-cols)))
            row-labels (when (> n-rows 1)
                         (mapv label grid))
            ;; The matrix's strips: the column each grid column draws on
            ;; `:x`, and each grid row on `:y`.
            flat? (every? #(not (:poses %)) (:poses composite))
            path-of (fn [r c] (if flat?
                                (if (= 1 n-rows) [c] [r])
                                [r c]))
            by-path (into {} (map (juxt :path identity)) (resolve-tree composite))
            leaf-at (fn [r c] (by-path (path-of r c)))
            regular? (and (> n-rows 1) (> n-cols 1)
                          (every? (fn [[r c]]
                                    (let [l (leaf-at r c)]
                                      (and l (not (:plotje/empty-cell l))
                                           (nil? (get-in l [:opts :coord])))))
                                  (for [r (range n-rows) c (range n-cols)] [r c])))
            axis-labels (fn [axis groups]
                          ;; A cell naming no column on the axis -- the
                          ;; matrix's diagonal, drawing one column's
                          ;; density -- does not vote.
                          (let [cols (mapv (fn [g] (let [cs (set (keep #(get (leaf-columns (apply leaf-at %)) axis) g))]
                                                     (when (= 1 (count cs))
                                                       (first cs))))
                                           groups)]
                            (when (and (every? some? cols) (< 1 (count (distinct cols))))
                              (mapv defaults/fmt-name cols))))
            x-strips (when (and regular? (not-any? some? col-labels))
                       (axis-labels :x (for [c (range n-cols)] (for [r (range n-rows)] [r c]))))
            y-strips (when (and regular? (not-any? some? row-labels))
                       (axis-labels :y (for [r (range n-rows)] (for [c (range n-cols)] [r c]))))
            labels (cond-> {}
                     (some some? col-labels) (assoc :col-labels col-labels)
                     (some some? row-labels) (assoc :row-labels row-labels)
                     x-strips (assoc :col-labels x-strips)
                     y-strips (assoc :row-labels y-strips))
            suppress (cond-> {}
                       x-strips (assoc :suppress-x-label true)
                       y-strips (assoc :suppress-y-label true))]
        (cond-> composite
          (seq labels) (assoc :grid-strip-labels labels)
          (seq suppress)
          (as-> c (reduce (fn [c [r cc]]
                            (update-in c (vec (interleave (repeat :poses) (path-of r cc)))
                                       update :opts #(merge suppress %)))
                          c
                          (for [r (range n-rows) cc (range n-cols)] [r cc])))))
      composite)))

(defn infer-chrome
  "An elaborated composite with its chrome written in, where its cells
   are the parts of one declared split: the once-only axis titles and
   ticks (`infer-axis-chrome`) and the strips naming each cell's part
   of the data (`infer-strips`). Anything written is kept."
  [composite]
  (if (declared-split? composite)
    (-> composite
        infer-axis-chrome
        infer-strips)
    composite))

;; ---- Expansion of a variation into listed cells ----

(declare expand-variation*)

(defn expand-variation
  "A pose with cells generated from a variation, written as the listed
   pose they expand to. Provisional prototype (step 7 of the PR #61
   plan; the verb and whether this is published are open).

   - A column of the data -- `:species` -- is a category: one cell per
     value the column holds, in the order the data holds them, each the
     full data with a written row filter, `{:where {:species v}}`.
   - `{:x [:a :b]}` or `{:y [...]}` names columns: one cell per column,
     each writing its column on that axis, as `pj/pose` would.
   - `{:col c :row r}` crosses two categories into a grid, a row per
     value of `r`; a combination the data does not hold is a filter that
     keeps no rows, which draws as an empty cell.

   The cells inherit the pose's data, mapping and layers, as cells of a
   composite do, so they are the parts of one declared split: they share
   each axis they can, and an axis they share on one column is ticked
   and titled once, and each is labelled in a strip by the value its
   filter keeps (`share-axes`, `infer-chrome`), as cells written by
   hand would be. The expansion adds only one drawing area for the cells."
  [pose variation]
  (let [listed (expand-variation* pose variation)]
    (cond-> listed
      (seq (default-share-axes listed)) (assoc-in [:opts :align-panels] true))))

(defn- expand-variation*
  [pose variation]
  (let [ds (coerce-dataset (:data pose))
        ;; A category is one column, or several united into a compound
        ;; key -- one value per combination the data holds, in the order
        ;; the data holds them.
        values (fn [col]
                 (if (sequential? col)
                   (vec (distinct (apply map vector (map ds col))))
                   (vec (distinct (ds col)))))
        where-of (fn [col v] (if (sequential? col) (zipmap col v) {col v}))]
    (cond
      (and (map? variation) (or (contains? variation :col) (contains? variation :row)))
      (let [{c :col r :row} variation
            cvs (if c (values c) [nil])
            rvs (if r (values r) [nil])
            cell (fn [cv rv]
                   {:where (cond-> {}
                             c (merge (where-of c cv))
                             r (merge (where-of r rv)))})]
        (assoc pose
               :layout {:direction :vertical}
               :poses (vec (for [rv rvs]
                             {:layout {:direction :horizontal}
                              :poses (vec (for [cv cvs] (cell cv rv)))}))))

      (map? variation)
      (let [[k cols] (first variation)]
        (-> pose
            (update :mapping #(not-empty (dissoc % k)))
            (assoc :layout {:direction :horizontal}
                   :poses (vec (for [c cols] {:mapping {k c}})))))

      :else
      (let [vs (values variation)]
        ;; One row in a vertical stack, the shape `pj/arrange` builds,
        ;; which the strips are drawn against.
        (assoc pose
               :layout {:direction :vertical}
               :poses [{:layout {:direction :horizontal}
                        :poses (vec (for [v vs]
                                      {:where (where-of variation v)}))}])))))

;; ---- A facet by category as the cells it means ----
;;
;; A regular split is drawn as one plan (`collapse-regular-split`), so
;; nothing draws through this; it states what a facet means, and the
;; facet laws are tested against it.

(defn expand-facets
  "A leaf whose mapping writes `:col` or `:row`, as the listed pose its
   facet expands to: `:col` alone is a row of cells, `:row` alone a
   column of them, both a grid with an empty cell for each combination
   the data does not hold (as `pj/facet-grid` draws). The facet's
   `:share-scales` stays where it is, on the listed pose. Any other
   pose is returned as it is -- a composite, a leaf with no data of its
   own, a grid naming one column on both sides, and a facet wrapped by
   `:grid-cols` or `:grid-rows`, which needs a strip over each cell."
  [pose]
  (let [m (:mapping pose)
        c (mapping-source (:col m))
        r (mapping-source (:row m))
        opts (or (:opts pose) {})]
    (if (or (composite? pose)
            (not (or c r))
            (nil? (:data pose))
            ;; A column on both sides keeps one cell per value on the
            ;; diagonal; one filter map cannot say "this value and that",
            ;; so such a grid stays one plan as written.
            (and c r (some (set (if (sequential? c) c [c]))
                           (if (sequential? r) r [r])))
            (:grid-cols opts)
            (:grid-rows opts))
      pose
      (let [;; The series is pivoted first, on the leaf (infer, pivot,
            ;; then expand): the cells then filter the long table, and
            ;; no layer reaches them reading a series.
            base (-> pose
                     pivot-leaf-series
                     (update :mapping #(not-empty (dissoc % :col :row))))
            variation (cond (and c r) {:col c :row r}
                            r {:row r}
                            :else c)]
        (expand-variation base variation)))))

;; ---- A regular split drawn as one plan ----
;;
;; The listed cells are what a facet means; for a regular split -- cells
;; that differ only in which values of the facet columns they keep -- one
;; plan is how it is drawn: the facet layout reserves the axis margins
;; once, draws "no data" panels and fits many panels, which composing
;; one plan per cell does not yet. So a facet and the same cells written
;; by hand take the same path.

(defn collapse-regular-split
  "A composite whose cells are a regular split of its data, as the
   faceted leaf that draws it in one plan; any other pose as it is.

   Regular: the composite holds the data and writes no strips or axis
   titles of its own; each cell carries only a `:where` of single
   values; and the cells keep exactly the values the data holds for the
   facet columns, in the order the data holds them -- one row of cells
   by `:col`, one column by `:row`, or a grid by both, with every
   combination present, as `expand-variation` writes it. A written
   `:share-scales` is kept in the faceted leaf's options, where the
   facet plan reads it."
  [pose]
  (let [grid (when (composite? pose) (cell-grid pose))
        cells (vec (apply concat grid))
        n-rows (count grid)
        n-cols (count (first grid))]
    (if-not (and (> (count cells) 1)
                 (some? (:data pose))
                 (not (contains? pose :grid-strip-labels))
                 (not (contains? pose :grid-axis-titles))
                 (every? #(and (map? (:where %))
                               (seq (:where %))
                               (= #{:where} (set (keys %)))
                               (not-any? set? (vals (:where %))))
                         cells))
      pose
      (let [ds (coerce-dataset (:data pose))
            ks (vec (keys (:where (first cells))))
            in-data? (every? (set (tc/column-names ds)) ks)
            values (fn [cols]
                     (if (= 1 (count cols))
                       (vec (distinct (ds (first cols))))
                       (vec (distinct (apply map vector (map ds cols))))))
            kept (fn [cols cs]
                   (mapv #(let [w (:where %)]
                            (if (= 1 (count cols)) (w (first cols)) (mapv w cols)))
                         cs))
            same-keys? (every? #(= (set ks) (set (keys (:where %)))) cells)
            ;; In a grid, the keys that hold one value down each column,
            ;; and those that hold one value along each row.
            constant-in? (fn [k groups]
                           (every? #(let [vs (map (comp k :where) %)]
                                      (every? (fn [v] (same-value? (first vs) v)) vs))
                                   groups))
            columns (for [ci (range n-cols)] (map #(nth % ci) grid))
            col-ks (filterv #(constant-in? % columns) ks)
            row-ks (filterv #(constant-in? % grid) ks)
            facet (cond
                    (not (and in-data? same-keys?)) nil
                    (= 1 n-rows)
                    (when (same-values? (values ks) (kept ks cells)) {:col ks})
                    (= 1 n-cols)
                    (when (same-values? (values ks) (kept ks cells)) {:row ks})
                    (and (seq col-ks) (seq row-ks)
                         (= (set ks) (into (set col-ks) row-ks))
                         (empty? (filter (set col-ks) row-ks))
                         (same-values? (values col-ks) (kept col-ks (first grid)))
                         (same-values? (values row-ks) (kept row-ks (map first grid))))
                    {:col col-ks :row row-ks})
            one-or-many (fn [cols] (if (= 1 (count cols)) (first cols) cols))
            written (cond (contains? (:opts pose) :share-scales) (get-in pose [:opts :share-scales])
                          (contains? pose :share-scales) (:share-scales pose))]
        (if-not facet
          pose
          (cond-> (-> pose
                      (dissoc :poses :layout :share-scales)
                      (update :mapping #(cond-> (or % {})
                                          (:col facet) (assoc :col (one-or-many (:col facet)))
                                          (:row facet) (assoc :row (one-or-many (:row facet)))))
                      (update :opts #(not-empty (dissoc % :share-scales :align-panels))))
            (some? written) (assoc-in [:opts :share-scales] written)))))))

;; ---- A facet over columns: the `:facet` node (provisional) ----
;;
;; `pj/facet` keeps a facet that holds columns under the pose's `:facet`
;; (categories go in the mapping). Drawing expands it into listed cells:
;; a part holding categories gives a cell per value, filtered by `:where`;
;; a part holding columns gives a cell per column, writing it; a grid
;; crosses a `:col` part with a `:row` part. `nil` in a vector of columns
;; leaves that cell without a column on that axis -- a margin.

(defn- facet-part-items
  "The cells one part of a facet node asks for, each a map of the
   `:where` it keeps and the `:mapping` it writes."
  [ds part]
  (cond
    (nil? part) [{}]

    (and (map? part) (not (contains? part :column)))
    (let [varied (filterv #(sequential? (get part %)) [:x :y])
          n (count (get part (first varied)))]
      (when-not (every? #(= n (count (get part %))) varied)
        (throw (ex-info (str "pj/facet reads vectors of columns in step, so they need one"
                             " length; got " (pr-str part) ".")
                        {:facet part})))
      (for [i (range n)]
        {:mapping (into {} (keep (fn [k] (when-let [c (nth (get part k) i)] [k c])) varied))}))

    :else
    (let [col (if (map? part) (:column part) part)
          values (if (sequential? col)
                   (vec (distinct (apply map vector (map ds col))))
                   (vec (distinct (ds col))))]
      (for [v values]
        {:where (if (sequential? col) (zipmap col v) {col v})}))))

(defn- facet-cell
  "One listed cell from the items of a row and a column. A cell naming
   one column on both axes -- the diagonal of a scatter plot matrix --
   names it once, on `:x`."
  [row-item col-item]
  (let [w (merge (:where row-item) (:where col-item))
        m (merge (:mapping row-item) (:mapping col-item))
        m (if (and (:x m) (= (:x m) (:y m))) (dissoc m :y) m)]
    (cond-> {}
      (seq w) (assoc :where w)
      (seq m) (assoc :mapping m))))

(defn expand-facet-node
  "A leaf holding a `:facet` node, as the listed pose it expands to;
   any other pose as it is. A part over columns of `:y` alone stacks its
   cells down, so they share the `:x` axis; any other single part runs
   across; `:col` and `:row` make a grid. The leaf's series is pivoted
   first, as for a facet over categories."
  [pose]
  (let [node (:facet pose)]
    (if (or (nil? node) (composite? pose))
      pose
      (let [base (-> pose
                     (dissoc :facet)
                     (cond-> (:data pose) pivot-leaf-series)
                     write-panel-columns)
            ds (coerce-dataset (:data base))
            grid? (or (contains? node :col) (contains? node :row))
            rows (if grid?
                   (vec (for [r (facet-part-items ds (:row node))]
                          (mapv #(facet-cell r %) (facet-part-items ds (:col node)))))
                   (let [items (facet-part-items ds node)
                         down? (and (map? node) (= [:y] (filterv #(sequential? (get node %)) [:x :y])))]
                     (if down?
                       (mapv #(vector (facet-cell {} %)) items)
                       [(mapv #(facet-cell {} %) items)])))
            ;; An axis the facet names columns on is the cells' to
            ;; write: the pose's own column there is replaced in every
            ;; cell, and left on the pose it was reported as not drawn
            ;; -- or read by a matrix's diagonal, which names one column.
            ;; A scale written there stays, with no column: it is a
            ;; setting of the axis, not of the column, and reaches each
            ;; cell's own column as scale settings do -- so
            ;; `(pj/scale pose :y {:domain [0 50]})` gives every panel
            ;; that domain.
            varied (set (mapcat (comp keys :mapping) (apply concat rows)))
            scale-only (fn [v] (when (and (map? v) (contains? v :scale))
                                 (select-keys v [:scale])))
            base (cond-> base
                   (seq (select-keys (:mapping base) varied))
                   (update :mapping
                           (fn [m]
                             (not-empty
                              (reduce (fn [m k]
                                        (if-let [kept (scale-only (get m k))]
                                          (assoc m k kept)
                                          (dissoc m k)))
                                      m
                                      varied)))))]
        (assoc base
               :layout {:direction :vertical}
               :poses (mapv (fn [cells] {:layout {:direction :horizontal} :poses cells}) rows))))))

(defn- series-key-facet
  "A leaf whose series is faceted by its own key column, as the facet
   over the series' columns it is; any other pose as it is.

   `{:y [:hwy :cty]}` faceted by the key the series invents draws a
   panel per column, which is what `(pj/facet pose {:y [:hwy :cty]})`
   says. The two were drawn by different paths and disagreed about the
   axes, the titles and the legend, so the first is rewritten into the
   second: the series is taken off the axis, leaving a scale written
   beside it, and the facet's `:col` and `:row` move from the mapping
   into a `:facet` node, with the columns where the key was. A facet by
   another column beside the key stays, as the other side of the grid.

   Left alone: a series on both axes (pairs), a compound facet key, a
   pose that already holds a `:facet` node, and a pose where anything
   else reads the key column -- a `:color` or a `:group` specified on
   it -- which needs the pivot's rows."
  [pose]
  (let [m (or (:mapping pose) {})
        layers (vec (:layers pose))
        dir-col (fn [d] (let [v (get m d)]
                          (cond (resolve/column-ref? v) v
                                (and (map? v) (resolve/column-ref? (:column v))) (:column v))))
        on-pose (series-in m)
        on-layers (vec (keep-indexed (fn [i l] (when (and (seq (series-in (:mapping l)))
                                                          (nil? (:data l)))
                                                 i))
                                     layers))
        specs (cond
                (seq on-pose) [(select-keys m on-pose)]
                (seq on-layers) (distinct (map #(select-keys (:mapping (layers %))
                                                             (series-in (:mapping (layers %))))
                                               on-layers)))
        spec (when (and (= 1 (count specs)) (= 1 (count (first specs)))
                        (not (and (seq on-pose) (seq on-layers))))
               (first specs))
        [axis written] (first spec)
        {:keys [cols] label :as} (when spec (series-mapping written))
        key-dir (when label (first (filter #(= label (dir-col %)) [:col :row])))
        other-dir (when key-dir (first (remove #{key-dir} [:col :row])))
        reads-key? (fn [mapping skip]
                     (some (fn [[k v]] (and (not (skip k)) (= label (mapping-source v))))
                           mapping))]
    (if (or (composite? pose)
            (:facet pose)
            (nil? key-dir)
            (reads-key? m #{:col :row})
            (some #(reads-key? (:mapping %) #{}) layers))
      pose
      (let [unseries (fn [mapping]
                       (let [sc (when (map? written) (:scale written))]
                         (if (some? sc)
                           (assoc mapping axis {:scale sc})
                           (dissoc mapping axis))))
            node (cond-> {key-dir {axis cols}}
                   (contains? m other-dir) (assoc other-dir (get m other-dir)))
            ;; The column the series' layers draw on the other axis is
            ;; the panels' common column, so it goes to the pose, where
            ;; a facet over columns reads it: the axis is then titled
            ;; once, as it is where the writer put the column there.
            across ({:x :y :y :x} axis)
            common (when (and (seq on-layers) (not (contains? m across)))
                     (let [vs (distinct (map #(get (:mapping (layers %)) across) on-layers))]
                       (when (and (= 1 (count vs)) (some? (first vs)))
                         (first vs))))
            relayer (fn [mapping] (cond-> (unseries mapping)
                                    (some? common) (dissoc across)))]
        (-> pose
            (assoc :mapping (cond-> (dissoc m :col :row)
                              (seq on-pose) unseries
                              (some? common) (assoc across common)))
            (update :mapping not-empty)
            (cond-> (seq on-layers)
              (assoc :layers (reduce (fn [ls i] (update-in ls [i :mapping] relayer))
                                     layers on-layers)))
            (assoc :facet node))))))

(defn expand-facet-nodes
  "`expand-facet-node` on every pose of a tree that holds a `:facet`
   node, so a pose faceted over columns draws the same panels as a cell
   of an arrangement as it does alone. A faceted cell with no data of
   its own is expanded with the data it inherits, which the expansion
   reads to pivot a series and to list a category's values. A series
   faceted by its own key is such a pose (`series-key-facet`)."
  ([pose] (expand-facet-nodes pose nil))
  ([pose inherited]
   (let [data (or (:data pose) inherited)
         pose (series-key-facet pose)
         pose (if (and (:facet pose) (not (composite? pose)))
                (expand-facet-node (cond-> pose
                                     (and data (nil? (:data pose))) (assoc :data data)))
                pose)]
     (cond-> pose
       (seq (:poses pose))
       (update :poses (fn [ps] (mapv #(expand-facet-nodes % data) ps)))))))

;; ---- Layers fitted to cells ----
;;
;; A layer written above a split reaches every cell below it, and is
;; drawn in the cells whose columns it draws: the number of columns the
;; cell names is one its layer type draws from (`layer-type/column-counts`
;; -- one for a histogram, two for a scatter, either for a bar), and the
;; layer names no other column on an axis the cell's own mapping names.
;; A cell left with no layer is a pose with no layers, so it draws what
;; inference chooses for its columns -- the histogram on the diagonal of
;; a matrix of points -- and a cell naming no column draws nothing.
;;
;; A layer that fits no cell is drawn in every cell, as written: where
;; the cells all have one shape, the layers belong to the split as a
;; whole, as they belong to a single pose -- a histogram written on a
;; faceted scatter draws a histogram in each panel.

(defn- columns-named
  "How many distinct columns a mapping names on `:x` and `:y`."
  [m]
  (count (distinct (remove nil? [(mapping-source (:x m)) (mapping-source (:y m))]))))

(defn- names-another-column?
  "Whether a layer's own mapping names a column on `:x` or `:y` other
   than the one the cell's own mapping names there."
  [own-mapping layer]
  (let [lm (:mapping layer)]
    (boolean (some (fn [k] (let [varied (mapping-source (get own-mapping k))
                                 mine (mapping-source (get lm k))]
                             (and varied mine (not= varied mine))))
                   [:x :y]))))

(defn- layer-fits-cell? [cell-mapping own-mapping layer]
  (and (not (names-another-column? own-mapping layer))
       (contains? (layer-type/column-counts (:layer-type layer))
                  (columns-named (merge cell-mapping (:mapping layer))))))

(defn- leaf-entries
  "Every leaf below a composite, with its path, its mapping as merged
   from the composite down (`:mapping`), and the part of it written
   below the composite (`:own`)."
  [pose inherited own path]
  (if (seq (:poses pose))
    (vec (mapcat (fn [i child]
                   (leaf-entries child
                                 (merge-mappings inherited (:mapping child))
                                 (merge own (:mapping child))
                                 (conj path i)))
                 (range) (:poses pose)))
    [{:path path :mapping inherited :own own :leaf pose}]))

(defn- columns-no-cell-has
  "The columns of a layer that no cell it reaches draws: for each of
   `:x` and `:y` on which the layer names a column of the data and some
   cell names another, `{:axis :column :cells}` -- but only where every
   cell names another column than the layer on some axis, so the layer
   fits none for that reason. Nil otherwise.

   Narrower than `names-another-column?`, which a written value
   satisfies too (`{:x 3 :y 30}` is another source than `:displ`): a
   value marks a place on every panel and is drawn in every cell."
  [entries layer col-names]
  (let [lm (:mapping layer)
        column? (fn [src] (and (some? src)
                               (if col-names
                                 (contains? col-names src)
                                 (resolve/column-ref? src))))
        mine (into {} (for [k [:x :y]
                            :let [src (mapping-source (get lm k))]
                            :when (column? src)]
                        [k src]))
        clash (fn [e k] (let [varied (mapping-source (get (:own e) k))]
                          (and varied (contains? mine k) (not= varied (mine k)))))]
    (when (and (seq mine) (seq entries)
               (every? (fn [e] (some #(clash e %) [:x :y])) entries))
      (vec (for [k [:x :y]
                 :when (some #(clash % k) entries)
                 :let [cells (vec (distinct (keep #(mapping-source (get (:own %) k)) entries)))]]
             {:axis k
              :column (mine k)
              :cells cells
              ;; A column the series pivot invented, known by the title
              ;; the pivot wrote beside it: the message names the series.
              :series (let [v (get lm k)]
                        (when (and (map? v)
                                   (contains? (conj (set (vals series-pair-value-columns))
                                                    series-value-column)
                                              (:from v)))
                          (get-in v [:scale :label])))
              ;; Whether no cell at all names the layer's column here,
              ;; or some do and none of those agrees on the other axis.
              :absent? (not (some #{(mine k)} cells))})))))

(defn- layer-fits-no-cell-message
  "The report for a layer naming a column no panel draws
   (`columns-no-cell-has`). The cells are those of a facet over columns:
   a `lay-*` call naming columns on an arrangement joins a cell or adds
   one at the call, and never reaches the fit."
  [layer clashes entries]
  (let [lt (:layer-type layer)
        fn-name (if (keyword? lt) (str "pj/lay-" (name lt)) "A layer")
        one-of (fn [cs] (if (next cs)
                          (str (str/join ", " (map pr-str (butlast cs))) " and " (pr-str (last cs)))
                          (pr-str (first cs))))
        absent (filterv :absent? clashes)
        ;; Each column is in some cell and no cell has the two together.
        pair? (empty? absent)
        told (if pair? clashes absent)
        it (if (next told) "them" "it")
        series? (some :series told)
        where "the facet over columns has "]
    (str fn-name " names "
         (str/join " and " (for [{:keys [axis column series]} told]
                             (str (if series
                                    (str "the series " series)
                                    (pr-str column))
                                  " on " axis)))
         (if pair?
           (str ", and no panel draws the two together: " where
                (str/join ", "
                          (distinct
                           (for [e entries]
                             (pr-str (into {} (for [k [:x :y]
                                                    :let [c (mapping-source (get (:own e) k))]
                                                    :when c]
                                                [k c]))))))
                ". ")
           (str ", and no panel draws " it ": " where
                (str/join ", and " (for [{:keys [axis cells]} told]
                                     (str (one-of cells) " on " axis)))
                ". "))
         (cond
           series?
           (str "A series draws its columns on one panel, and a facet over columns"
                " draws each on a panel of its own: write one of the two.")

           ;; The layer draws data of its own, so its columns are not
           ;; the facet's to list.
           (some? (:data layer))
           (str "This layer draws data of its own, which the facet's columns are"
                " not read from: draw it on a pose of its own and put the two side"
                " by side with pj/arrange, or name a column the panels draw.")

           :else
           (str (if pair?
                  "List the pair in the facet"
                  (str "List "
                       (str/join " and " (for [{:keys [axis column]} told]
                                           (str (pr-str column) " among the facet's " axis " columns")))))
                " and leave " (str/join " and " (map (comp str :axis) told))
                " off the layer, or draw the layer on a pose of its own and put the"
                " two side by side with pj/arrange."))
         " A layer naming no column is drawn in every panel.")))

(defn fit-layers
  "A composite whose layers are fitted to its cells, as above. Returned
   unchanged where every layer fits every cell it reaches.

   A layer that fits no cell because it names a column on an axis where
   every cell names another (`columns-no-cell-has`) reports an error
   where it leaves some cell with no layer drawing that cell's own
   columns: the facet's column would be drawn nowhere. Beside layers
   that fit every cell it is drawn in every cell, as a column shown in
   each panel for comparison."
  ([pose] (fit-layers pose {} nil))
  ([pose inherited] (fit-layers pose inherited nil))
  ([pose inherited data]
   (if-not (seq (:poses pose))
     pose
     (let [m (merge-mappings inherited (:mapping pose))
           data (or (:data pose) data)
           layers (vec (:layers pose))
           entries (vec (leaf-entries (dissoc pose :mapping) m {} []))
           fits (vec (for [e entries]
                       (mapv #(layer-fits-cell? (:mapping e) (:own e) %) layers)))
           fit-somewhere (set (for [row fits [i f] (map-indexed vector row) :when f] i))
           _ (doseq [[i layer] (map-indexed vector layers)
                     :when (not (fit-somewhere i))]
               (let [d (or (:data layer) data)
                     col-names (when d (try (set (tc/column-names (coerce-dataset d)))
                                            (catch Exception _ nil)))]
                 (when-let [clashes (and
                                     ;; Some cell has no layer drawing its
                                     ;; own columns: none of this level's
                                     ;; fits it, and it has none of its own.
                                     (not (every? (fn [[e row]]
                                                    (or (some true? row)
                                                        (seq (:layers (:leaf e)))))
                                                  (map vector entries fits)))
                                     (columns-no-cell-has entries layer col-names))]
                   (throw (ex-info (layer-fits-no-cell-message layer clashes entries)
                                   {:layer-type (:layer-type layer)
                                    :mapping (:mapping layer)
                                    :clashes clashes})))))
           kept (vec (for [row fits]
                       (vec (keep-indexed (fn [i f] (when (or f (not (fit-somewhere i))) (layers i)))
                                          row))))
           changed? (and (seq layers) (some #(not= (count %) (count layers)) kept))
           pose' (if-not changed?
                   pose
                   (reduce (fn [p [e ls]]
                             (update-in p (vec (interleave (repeat :poses) (:path e)))
                                        (fn [leaf]
                                          (cond
                                            (seq ls) (update leaf :layers #(into (vec ls) %))
                                            ;; Nothing to draw, and nothing to infer
                                            ;; from: an empty cell, as `pj/arrange`
                                            ;; marks a nil one.
                                            (and (empty? (:layers leaf))
                                                 (zero? (columns-named (:mapping e)))
                                                 (nil? (:data leaf)))
                                            (assoc leaf :plotje/empty-cell true)
                                            :else leaf))))
                           (dissoc pose :layers)
                           (map vector entries kept)))]
       (update pose' :poses (fn [ps] (mapv #(fit-layers % m data) ps)))))))

;; ---- Row filter ----

(defn apply-where
  "A pose whose written row filter, `:where`, is applied to its data:
   `{:where {:species \"setosa\"}}` keeps the rows whose `:species` is
   \"setosa\". Several keys must all match; a set is any of its values.
   The filter is consumed, so the result carries the filtered data and
   no `:where`. A pose with no data keeps its filter for the data it
   is given later.

   Numbers compare by value (`same-value?`). A value that appears
   nowhere in its column is reported -- a misspelling, or a category the
   data does not hold -- since it selects no rows; a cell generated from
   the data never names one.

   A cell generated from a category keeps the full data and writes its
   filter, so the listed pose says that its cells split one dataset.
   The spelling `:where` is provisional."
  [pose]
  (let [w (:where pose)]
    (if (and (map? w) (some? (:data pose)))
      (let [ds (coerce-dataset (:data pose))
            names (set (tc/column-names ds))
            missing (vec (remove names (keys w)))
            _ (when (seq missing)
                (throw (ex-info (str "A row filter names " (pr-str missing)
                                     ", which the data does not have. Available"
                                     " columns: " (vec (sort-by str names)) ".")
                                {:where w :missing missing})))
            ;; Stops at the first match, so only a value that is absent
            ;; costs a whole pass over its column.
            absent (vec (for [[c v] w
                              x (if (set? v) v [v])
                              :when (not (some #(same-value? % x) (ds c)))]
                          [c x]))
            ;; An empty set is any of no values, which selects no rows
            ;; and names no value the check above could find absent.
            empty-sets (vec (for [[c v] w :when (and (set? v) (empty? v))] c))
            _ (when (or (seq absent) (seq empty-sets))
                (let [msg (str "A row filter names "
                               (str/join ", " (concat
                                               (for [[c x] absent] (str (pr-str x) " on " (pr-str c)))
                                               (for [c empty-sets] (str "an empty set on " (pr-str c)))))
                               " -- "
                               (cond (seq empty-sets) "no value the data holds"
                                     (next absent) "values the data does not hold"
                                     :else "a value the data does not hold")
                               ", which selects no rows.")]
                  (if (:strict (defaults/config))
                    (throw (ex-info msg {:where w :absent absent}))
                    (println (str "Warning: " msg)))))
            ;; The first key's matches, narrowed by each further key.
            idxs (reduce (fn [idxs [c v]]
                           (let [col (ds c)]
                             (if idxs
                               (let [kept (rows-where (mapv col idxs) v)]
                                 (mapv idxs kept))
                               (rows-where col v))))
                         nil
                         w)]
        (-> pose
            (assoc :data (if idxs (tc/select-rows ds idxs) ds))
            (dissoc :where)))
      pose)))

;; ---- Elaboration ----
;;
;; The choices the draft makes for a pose, written into the pose: series
;; pivoted, the default mapping of raw data, and a concrete layer type wherever one
;; was left to inference. Internal; a pose to a pose, so that everything
;; downstream -- shared scales, panels, the draft -- reads a layer type
;; rather than predicting one.

(defn default-mapping
  "The mapping raw data gets when nothing names a column: the first
   1-3 columns, as `:x`, `:y` and `:color`. Nil for 0 or 4+ columns."
  [d]
  (let [cols (vec (tc/column-names d))]
    (case (count cols)
      1 {:x (cols 0)}
      2 {:x (cols 0) :y (cols 1)}
      3 {:x (cols 0) :y (cols 1) :color (cols 2)}
      nil)))

(def ^:private inferred-layer-types
  "The registered layer type for each mark and stat
   `resolve/infer-layer-type` chooses."
  {[:point :identity]   :point
   [:bar :bin]          :histogram
   [:rect :count]       :bar
   [:line :identity]    :line
   [:boxplot :boxplot]  :boxplot})

(defn- infer-layer-type-key
  "The layer type the data chooses for a layer drawn at `mapping` --
   the choice `resolve/infer-layer-type` makes at plan time, named as a
   registered layer type. Nil where there is nothing to choose from (no
   data, no column) or the columns cannot be read yet; the layer then
   stays `:infer` and the plan chooses."
  [mapping data]
  (let [x (mapping-source (:x mapping))
        y (mapping-source (:y mapping))]
    (when (and data (or x y))
      (try
        (let [ds (coerce-dataset data)
              cols (set (tc/column-names ds))
              ;; One column chooses the same type on either axis: read
              ;; on `:y` alone it is asked as if on `:x`, and `side-on`
              ;; then draws the choice on its side. (Asked on `:y`, a
              ;; lone column fell to a scatter with no `:x`, and the
              ;; plan failed its schema.)
              v (cond
                  (and y (nil? x)) {:x y}
                  :else (cond-> {}
                          x (assoc :x x)
                          y (assoc :y y)))]
          (when (every? cols (vals v))
            (let [{:keys [x-type y-type x-temporal? y-temporal?]}
                  (resolve/infer-column-types ds v)
                  {:keys [mark stat]} (resolve/infer-layer-type
                                       v x-type y-type x-temporal? y-temporal?)]
              (inferred-layer-types [mark stat]))))
        (catch Exception _ nil)))))

(defn- infer-layer?
  [layer]
  (= :infer (:layer-type layer)))

(defn infer-default-mapping
  "A leaf with the columns it draws filled in where nothing names them,
   from data of 1-3 columns.

   - Where neither the leaf's mapping nor any layer names an `:x` or a
     `:y` column, the data's `default-mapping` is written under the
     leaf's own mapping, so a scale it already carries reads the
     default column.
   - Where a `:y` is named and no `:x` is, and a layer's type draws
     only from two columns (`layer-type/column-counts` lacks 1), the
     leaf's `:x` is the first column nothing names, so
     `(pj/lay-point data {:y :a})` draws `:a` against the column before
     it. A leaf with no layer, or whose layers can draw from one
     column, keeps `:y` alone and is drawn on its side -- see
     `side-on` -- so `(pj/pose data {:y :a})` draws the same on data of
     any width. A `:y` is never filled in: whether a layer needs one
     depends on its type, and `(pj/lay-bar data :x)` counts.

   Returns the leaf unchanged otherwise, so it is idempotent."
  [leaf]
  (let [m (or (:mapping leaf) {})
        mappings (cons m (map #(or (:mapping %) {}) (:layers leaf)))
        named? (fn [k] (some #(mapping-source (get % k)) mappings))
        d (when (and (:data leaf) (nil? (:poses leaf)))
            (coerce-dataset (:data leaf)))
        default (when d (default-mapping d))]
    (cond
      (not default) leaf

      (and (not (named? :x)) (not (named? :y)))
      (assoc leaf :mapping (merge-mappings default m))

      (and (not (named? :x)) (named? :y)
           (some #(not (contains? (layer-type/column-counts (:layer-type %)) 1))
                 (:layers leaf)))
      (let [used (set (keep #(when-not (map? %) %)
                            (mapcat #(map mapping-source (vals %)) mappings)))
            free (first (remove used (tc/column-names d)))]
        (if free
          (assoc leaf :mapping (merge-mappings {:x free} m))
          leaf))

      :else leaf)))

(defn- keeps-from-side-on?
  "Whether a layer keeps its leaf from being drawn on its side: its
   layer type does not draw from one column, or it names an `:x`."
  [layer]
  (not (and (keyword? (:layer-type layer))
            (contains? (layer-type/column-counts (:layer-type layer)) 1)
            (not (contains? (:mapping layer) :x)))))

(defn- side-on
  "A leaf naming a column only on `:y` -- in its own mapping or in its
   layers' -- whose layers each draw from one column
   (`layer-type/column-counts` holds 1: histogram, density, rug, bar),
   drawn on its side: the column on `:x` and the coordinates flipped,
   so its distribution runs up the `:y` axis -- the margin beside a
   scatter. `(pj/pose data {:y :a})` with `pj/lay-histogram` and
   `(pj/lay-histogram pose {:y :a})` are one request. A leaf with a
   layer that names `:x`, or draws only from two columns, is left as it
   is (`keeps-from-side-on?`); a `:coord` other than `:flip` specified
   on such a leaf reports an error. (Read on `:y` alone, such a leaf
   drew nothing and failed with a NullPointerException.)"
  [leaf]
  (let [m (:mapping leaf)
        layers (:layers leaf)
        coord (get-in leaf [:opts :coord])
        y (or (:y m) (some #(:y (:mapping %)) layers))
        on-x (fn [mapping] (if (contains? mapping :y)
                             (-> mapping (dissoc :y) (assoc :x (:y mapping)))
                             mapping))]
    (if (and (some? y)
             (nil? (:x m))
             (seq layers)
             (not-any? keeps-from-side-on? layers))
      (do
        ;; On its side is the one way such a leaf is drawn, so `:flip`
        ;; specified says what is inferred, and any other coordinate
        ;; system has no drawing for a column that is on `:y` alone.
        (when-not (contains? #{nil :flip} coord)
          (throw (ex-info (str "This pose names " (pr-str (mapping-source y))
                               " on :y alone, under layers that draw from one column,"
                               " which is drawn on its side with the coordinates"
                               " flipped. :coord " (pr-str coord) " has no drawing for"
                               " it. Name the column on :x, or leave :coord"
                               " unspecified.")
                          {:y (mapping-source y) :coord coord})))
        (-> leaf
            (cond-> (some? m) (assoc :mapping (on-x m)))
            (update :layers (fn [ls] (mapv #(cond-> % (:mapping %) (update :mapping on-x)) ls)))
            (assoc-in [:opts :coord] :flip)))
      leaf)))

(defn elaborate-leaf
  "A leaf with the draft's choices written in. `leaf` carries its full
   context -- a top-level leaf, or one `resolve-tree` resolved.

   - The row filter applied -- see `apply-where`.
   - Series pivoted -- see `pivot-leaf-series`.
   - The data's default mapping, where the leaf needs one -- see
     `infer-default-mapping`.
   - A leaf with a place and no layers draws one layer, of the type the
     data chooses.
   - A place the leaf's own mapping names, where no layer draws, keeps
     its panel, with a layer of the type the data chooses --
     `(-> data (pj/pose :a :b) (pj/lay-point :c :d))` draws a-against-b
     beside c-against-d.
   - A layer holds the panel's columns it reads, where it names none
     or names one and its layer type needs two -- see
     `read-panel-columns`.
   - A layer written as `:infer` takes the type the data chooses for
     the place it draws at.

   Idempotent: a second call finds nothing left to choose."
  [leaf]
  (let [leaf (-> leaf
                 apply-where
                 pivot-leaf-series
                 infer-default-mapping)
        own-mapping (select-keys (or (:mapping leaf) {}) [:x :y])
        own-place (position-mapping-key (:mapping leaf))
        layers (vec (or (:layers leaf) []))
        layers (cond
                 (seq layers) layers
                 own-place [{:layer-type :infer}]
                 :else [])
        leaf (read-panel-columns (assoc leaf :layers layers))
        place-mapping (fn [layer]
                        (let [k (or (layer-place leaf layer)
                                    (leaf-place leaf))]
                          (cond-> (merge (:mapping leaf) (:mapping layer))
                            (first k) (assoc :x (first k))
                            (second k) (assoc :y (second k)))))]
    (-> leaf
        (update :layers
                (fn [ls]
                  (mapv (fn [layer]
                          (if-let [t (and (infer-layer? layer)
                                          (infer-layer-type-key
                                           (place-mapping layer)
                                           (or (:data layer) (:data leaf))))]
                            (assoc layer :layer-type t)
                            layer))
                        ls)))
        side-on)))

(defn elaborate
  "`elaborate-leaf` on every leaf of a pose, each read with the context
   its ancestors give it, and each leaf's choices written back onto that
   leaf. An ancestor's `:infer` layer, which reaches several leaves, is
   left to the plan."
  ([pose] (elaborate pose {}))
  ([pose parent-ctx]
   (cond
     (:plotje/empty-cell pose) pose

     (leaf? pose)
     (let [anc-layers (vec (:layers parent-ctx))
           _ (when-let [l (first (filter #(seq (series-in (:mapping %))) anc-layers))]
               (throw (ex-info (str "A layer added to a composite pose reads "
                                    (pr-str (vec (mapcat #(:cols (series-mapping (get (:mapping l) %)))
                                                         (series-in (:mapping l)))))
                                    " as series. The pivot reshapes the dataset the"
                                    " series is read from, and a composite's cells share"
                                    " one, so every cell would be reshaped. Add the layer"
                                    " to a cell before arranging the cells.")
                               {:layer-type (:layer-type l)})))
           resolved {:data (or (:data pose) (:data parent-ctx))
                     :opts (:opts pose)
                     :where (:where pose)
                     :mapping (merge-mappings (:mapping parent-ctx) (:mapping pose))
                     :layers (into anc-layers (:layers pose))
                     :overlay (if (contains? pose :overlay)
                                (:overlay pose)
                                (:overlay parent-ctx))}
           done (elaborate-leaf resolved)
           own-layers (subvec (:layers done) (count anc-layers))]
       (if (and (:where pose) (:data done) (zero? (tc/row-count (:data done))))
         ;; A cell whose filter leaves no rows is an empty cell: it keeps
         ;; its place, draws nothing, and does not vote on sharing.
         {:layers [] :opts (:opts pose) :plotje/empty-cell true
          :plotje/where (:where pose)}
         (cond-> (dissoc pose :where)
           ;; The filter is consumed, and kept as a record of which part
           ;; of the data the cell draws -- `infer-chrome` labels the
           ;; cells by it.
           (:where pose) (assoc :plotje/where (:where pose))

         ;; A filter above the leaf narrowed the data it inherits, which
         ;; `resolve-tree` would otherwise hand it unfiltered.
           (or (:filtered? parent-ctx)
               (not (identical? (:data resolved) (:data done))))
           (assoc :data (:data done))

           ;; Data written here from what the cell inherited is still a
           ;; part of its ancestor's dataset; sharing reads this to tell
           ;; a split from cells handed over with data of their own.
           (and (nil? (:data pose))
                (or (:filtered? parent-ctx)
                    (not (identical? (:data resolved) (:data done)))))
           (assoc :plotje/part-of-parent-data true)

           (not= (:mapping resolved) (:mapping done))
         ;; The elaborated mapping already holds the leaf's own keys over
         ;; its ancestors', with its choices -- a default mapping, a
         ;; pivoted series -- written in.
           (assoc :mapping (:mapping done))

           (not= own-layers (vec (or (:layers pose) [])))
           (assoc :layers own-layers)

           ;; A coordinate system the draft chose -- a column on `:y`
           ;; alone drawn on its side (`side-on`).
           (and (get-in done [:opts :coord]) (not (get-in pose [:opts :coord])))
           (assoc-in [:opts :coord] (get-in done [:opts :coord])))))

     :else
     (let [ctx {:filtered? (boolean (or (:filtered? parent-ctx) (:where pose)))
                :data (:data (apply-where {:data (or (:data pose) (:data parent-ctx))
                                           :where (:where pose)}))
                :mapping (merge-mappings (:mapping parent-ctx) (:mapping pose))
                :layers (into (vec (:layers parent-ctx)) (:layers pose))
                :overlay (if (contains? pose :overlay)
                           (:overlay pose)
                           (:overlay parent-ctx))}]
       (-> pose
           (dissoc :where)
           (update :poses (fn [ps] (mapv #(elaborate % ctx) ps))))))))

;; ---- A layer left without a column its layer type draws from ----
;;
;; Asked once per draft layer, after its mapping is resolved and its
;; written values are columns: a layer that reads rows needs a column on
;; `:x`, and a `:y` column too unless its type draws from one column or
;; its stat computes `:y`. `resolve/resolve-draft-layer` asks the second
;; half again at plan time, for a draft built by hand. The two share
;; `stat/computes` and the set of marks drawn from written values, and
;; cannot share more, because only this site has the layer's call and
;; the pose's mapping to name.

(defn- one-column-layer-fns
  "The `lay-*` functions of the registered layer types that draw from
   one column, as a phrase."
  []
  (let [names (->> (layer-type/registered)
                   keys
                   (filter #(contains? (layer-type/column-counts %) 1))
                   (map #(str "pj/lay-" (name %)))
                   sort)]
    (if (next names)
      (str (str/join ", " (butlast names)) " or " (last names))
      (first names))))

(defn- layer-missing-columns
  "The axes on which a draft layer lacks a column its type draws from,
   as a vector of `:x` and `:y` -- empty where it has what it needs.
   `resolved` is the layer's mapping after `resolve-positional-values`,
   so a written value is already a column."
  [resolved layer-type-key]
  (let [mark (:mark resolved)
        x (:x resolved)
        y (:y resolved)
        counts (layer-type/column-counts layer-type-key)
        stat (:stat (resolve/infer-layer-type (select-keys resolved [:mark :stat :y])
                                              nil nil false false))
        y-free? (or (contains? counts 1)
                    (:x-only resolved)
                    (= :rug mark)
                    (:y (stat/computes (assoc resolved :stat stat))))]
    (cond
      (or (= :infer mark)
          (= :infer layer-type-key)
          (resolve/written-position-marks mark)) []
      (and (nil? x) (nil? y)) (if y-free? [:x] [:x :y])
      (nil? x) [:x]
      (and (nil? y) (not y-free?)) [:y]
      :else [])))

(defn- missing-columns-message
  [missing resolved d {:keys [layer-type-key layer-mapping leaf-mapping written columns
                              side-on-blockers]}]
  (let [fn-name (if (keyword? layer-type-key)
                  (str "pj/lay-" (name layer-type-key))
                  (str "A layer of mark " (pr-str (:mark resolved))))
        counts (layer-type/column-counts layer-type-key)
        ;; The columns of the data as the layer was given it: a written
        ;; value is a column of `d` by now, under a name nobody wrote.
        cols (vec (sort-by str (or columns (tc/column-names d))))
        col-set (set cols)
        draws (cond
                (= #{1} counts) "one column, on :x"
                (= #{1 2} counts) "one column on :x, or two on :x and :y"
                :else "two columns, one on :x and one on :y")
        ;; What the layer does have, each with where it was named.
        named (for [k (concat [:x :y] (sort (disj defaults/column-keys :x :y)))
                    :let [v (get written k)]
                    :when (and (some? v)
                               (some? (get resolved k))
                               (or (#{:x :y} k) (contains? col-set v)))]
                (str k " " (pr-str v)
                     (cond (contains? layer-mapping k) ""
                           (contains? leaf-mapping k) " (from the pose)"
                           :else "")))
        x (when (:x resolved) (:x written))
        y (when (:y resolved) (:y written))
        ;; How to write the fix: a call for a registered layer type, a
        ;; mapping for a layer whose type is a map.
        lay (fn [& args]
              (if (keyword? layer-type-key)
                (str "(" fn-name " pose " (str/join " " args) ")")
                (str "{" (str/join " " (map #(str %1 " " %2) [:x :y] args))
                     "} in its :mapping")))]
    (str fn-name " draws from " draws ", and this layer has no column on "
         (str/join " or " (map str missing)) ". "
         (if (seq named)
           (str "It names " (str/join ", " named) ". ")
           "It names no column. ")
         (cond
           (= [:x :y] missing)
           (str "Name both on the layer -- " (lay ":x-column" ":y-column")
                " -- or in the pose's mapping, which every layer of the pose reads.")

           (and (= [:x] missing) (nil? y))
           (str "Name it on the layer -- " (lay ":x-column")
                " -- or in the pose's mapping, which every layer of the pose reads.")

           ;; The pose names a column on `:y` alone, which `side-on`
           ;; draws on its side -- unless another layer keeps it from.
           (and (= [:x] missing) (= #{1} counts)
                (seq side-on-blockers)
                (not-any? #{fn-name} side-on-blockers))
           (str "A column named on :y alone is drawn on its side where every"
                " layer draws from one column and names no :x of its own, and "
                (str/join ", " side-on-blockers)
                (if (next side-on-blockers) " do not" " does not")
                ". Name the column on :x -- (pj/pose data {:x " (pr-str y)
                "}) -- and add (pj/coord :flip) to draw it on its side.")

           (= [:x] missing)
           (str "Name the :x column on the layer -- " (lay ":x-column" (pr-str y))
                " -- or in the pose's mapping, which every layer of the pose reads.")

           ;; A value written on `:x`, with nothing on `:y`.
           (not (contains? col-set x))
           (str "Name the :y column, or write a :y value, on the layer -- "
                (if (keyword? layer-type-key)
                  (str "(" fn-name " pose {:x " (pr-str x) " :y ...})")
                  (str "{:x " (pr-str x) " :y ...} in its :mapping"))
                " -- or name the column in the pose's mapping.")

           :else
           (str "Name the :y column on the layer -- " (lay (pr-str x) ":y-column")
                " -- or in the pose's mapping. To draw " (pr-str x)
                " alone, use a layer that draws from one column: "
                (one-column-layer-fns) "."))
         (when (and (nil? x) (nil? y) (nil? (default-mapping d)))
           (str " The data has " (count cols) " columns, so none is read by default."))
         " Available columns: " cols)))

(defn- check-layer-columns!
  "Report a layer left without a column its layer type draws from.
   `ctx` carries the layer's mapping as written (`:written`, before its
   values became columns) and the columns of the data it was given
   (`:columns`), which are what the message names."
  [resolved d ctx]
  (when d
    (let [missing (layer-missing-columns resolved (:layer-type-key ctx))]
      (when (seq missing)
        (throw (ex-info (missing-columns-message missing resolved d ctx)
                        {:layer-type (:layer-type-key ctx)
                         :missing missing
                         :x (:x resolved)
                         :y (:y resolved)
                         :columns (vec (sort-by str (or (:columns ctx)
                                                        (tc/column-names d))))}))))))

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

   :__panel-idx is the panel a draft entry belongs to, which plan.clj
   groups by. A leaf draws one panel -- its layers share it -- and
   faceting multiplies it, so the index is the facet variant."
  [leaf]
  (let [leaf         (elaborate-leaf leaf)
        leaf-mapping (or (:mapping leaf) {})
        leaf-data    (:data leaf)
        opts         (or (:opts leaf) {})
        coord-type   (:coord opts)
        ;; `elaborate-leaf` has written in the layer a leaf with a place
        ;; and no layers draws, and the one the pose's own place keeps
        ;; where no layer draws there. An entirely empty leaf emits
        ;; nothing, so plan produces a minimal placeholder.
        applicable   (or (:layers leaf) [])
        ;; The panel aesthetics are read here and stripped before
        ;; anything downstream sees the mapping: a panel is a
        ;; destination for a distinction, not something a mark draws.
        ;; `:opts` is the older spelling `pj/facet` used to write, kept
        ;; so a hand-built pose carrying it still faceted.
        facet-col    (or (mapping-source (:col leaf-mapping)) (:facet-col opts))
        facet-row    (or (mapping-source (:row leaf-mapping)) (:facet-row opts))
        leaf-mapping (dissoc leaf-mapping :col :row)
        variants     (facet-variants leaf-data facet-col facet-row)
        ;; The layers that keep a column on `:y` alone from being drawn
        ;; on its side, for `check-layer-columns!` to name.
        side-on-blockers (when (and (nil? (:x leaf-mapping))
                                    (or (some? (:y leaf-mapping))
                                        (some #(:y (:mapping %)) applicable)))
                           (vec (distinct
                                 (for [l applicable
                                       :when (keeps-from-side-on? l)]
                                   (if (keyword? (:layer-type l))
                                     (str "pj/lay-" (name (:layer-type l)))
                                     "a layer whose type is a map")))))
        ;; Said once per draft, and only where the leaf's own layers made
        ;; the split -- a faceted leaf has many panels and no disagreement
        ;; to report.
        _            (check-shared-panel! leaf)
        ;; What tells the overlaid layers apart, where they disagree
        ;; about a column and the writer has not said. Written as a
        ;; value read through the scale, which is the form that draws
        ;; one palette colour and earns a legend entry -- the same
        ;; route `{:color {:value "Model A" :scale true}}` takes.
        ov-labels    (overlay-labels leaf)
        ov-colors    (when ov-labels (default-color-layers leaf))
        overlay-legend-titled? (boolean
                                (some->> (:color leaf-mapping)
                                         (#(when (map? %) (:scale %)))
                                         (#(when (map? %) (contains? % :label)))))
        ;; The order the whole data holds a column's categories in,
        ;; which the panels of a facet cannot give: each holds some of
        ;; the rows. The plan orders a shared categorical axis by it
        ;; (`plan/order-categories`).
        whole        (when (next variants) (coerce-dataset leaf-data))
        category-order (memoize
                        (fn [col]
                          (when (and whole (contains? (set (tc/column-names whole)) col))
                            (vec (distinct (map str (remove nil? (whole col))))))))
        categories-of (fn [resolved axis]
                        (let [col (get resolved axis)
                              vs (when (and whole (resolve/column-ref? col)
                                            (contains? (set (tc/column-names whole)) col))
                                   (whole col))
                              v (some identity (take 100 vs))]
                          (when (and (some? v)
                                     (or (= :categorical (get resolved (if (= axis :x) :x-type :y-type)))
                                         (not (or (number? v) (resolve/temporal-value? v)))))
                            (category-order col))))]
    (vec
     (for [[variant-idx variant] (map-indexed vector variants)
           [layer-idx layer] (map-indexed vector applicable)]
       (let [layer-type-info  (resolve-layer-type-info (:layer-type layer))
             layer-mapping    (cond-> (report-panel-aesthetic-on-layer
                                       (or (:mapping layer) {}))
                                (and (nth ov-colors layer-idx nil)
                                     (nth ov-labels layer-idx nil))
                                (assoc :color
                                       ;; The entries name the columns, so a
                                       ;; legend titled "color" says nothing
                                       ;; the reader cannot already see. A
                                       ;; title the writer set is left alone.
                                       (default-color (nth ov-labels layer-idx)
                                                      (if overlay-legend-titled?
                                                        true
                                                        {:label ""}))))
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
             written          resolved
             given-data       (coerce-dataset (or (:data layer) (:data variant)))
             ;; An :x or :y given as a value becomes data before anything
             ;; else looks at the mapping, so every check and every later
             ;; stage sees the ordinary shape: columns named by a mapping.
             [resolved d] (resolve-positional-values
                           resolved
                           given-data
                           layer-own-data?)]
         (validate-columns resolved d
                           {:layer-mapping layer-mapping
                            :layer-type-info layer-type-info
                            :layer-type-key (:layer-type layer)
                            :layer-own-data? layer-own-data?})
         (check-layer-columns! resolved d
                               {:layer-mapping (or (:mapping layer) {})
                                :leaf-mapping leaf-mapping
                                :written written
                                :side-on-blockers side-on-blockers
                                :columns (when given-data (tc/column-names given-data))
                                :layer-type-key (:layer-type layer)})
         (validate-column-only-aesthetics resolved d)
         (validate-unscaled-channel-options resolved opts)
         (-> resolved
             ;; A rule or a band is drawn once, from written values, so a
             ;; colour column has no row to colour it by. Kept, the
             ;; column drew a legend of colours nothing used. Inherited
             ;; from the pose it is meant for the other layers and is
             ;; dropped here without a word; written on the rule itself
             ;; it is marked for `plan`'s warning.
             (as-> r (let [c (:color r)]
                       (if (and (resolve/written-position-marks (:mark r))
                                (resolve/column-ref? c)
                                d (contains? (set (tc/column-names d)) c))
                         (cond-> (dissoc r :color :color-type)
                           (contains? layer-mapping :color)
                           (assoc :undrawn-rule-color [(:layer-type layer) c]))
                         r)))
             (assoc :data d
                    :__panel-idx variant-idx)
             (merge (layer-scale-specs resolved))
             (cond->
              coord-type  (assoc :coord coord-type)
              ;; Marks this layer as one of a set overlaid on a panel
              ;; whose columns disagree, so the axis can name all of
              ;; them, whatever colours the layers are drawn in. Only
              ;; these: an annotation placed in drawing space leaves the
              ;; axis title alone.
              (and ov-labels (nth ov-labels layer-idx nil))
              (assoc :overlay-labelled true)
              (:facet-col variant) (assoc :facet-col (:facet-col variant))
              (:facet-row variant) (assoc :facet-row (:facet-row variant))
              (and (not layer-own-data?) (categories-of resolved :x))
              (assoc :__x-order (categories-of resolved :x))
              (and (not layer-own-data?) (categories-of resolved :y))
              (assoc :__y-order (categories-of resolved :y)))
             (cond-> (= :infer (:mark resolved))
               (-> (dissoc :mark :stat)))))))))
