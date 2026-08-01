(ns scicloj.plotje.impl.position
  "Position adjustment — composable transforms on layer descriptors.
   Runs between extract-layer and build-panels in the plan pipeline.

   Position types:
     :identity — no adjustment (default)
     :dodge    — side-by-side within a categorical band (annotation)
     :stack    — cumulative y-values across groups (data transform)
     :fill     — normalized cumulative y, sums to 1.0 (data transform)")

;; ---- Helpers ----

(defn- layer-group-labels
  "Extract group labels from a layer, regardless of structure."
  [layer]
  (cond
    (:groups layer) (keep :label (:groups layer))
    (:boxes layer) (keep (comp str :color-category) (:boxes layer))
    (:violins layer) (keep (comp str :color-category) (:violins layer))
    :else nil))

;; ---- Multimethod ----

(defmulti apply-position
  "Apply position adjustment to layers sharing a position type.
   Returns a vec of adjusted layers."
  (fn [position layers] position))

;; ---- Doc methods (dispatching on [position-key :doc]) ----

(defmethod apply-position [:identity :doc] [_ _] "Plot at exact data coordinates (groups overlap)")
(defmethod apply-position [:dodge :doc] [_ _] "Shift groups side-by-side within a band")
(defmethod apply-position [:stack :doc] [_ _] "Pile groups cumulatively")
(defmethod apply-position [:fill :doc] [_ _] "Stack normalized to [0, 1] (proportions)")

(defmethod apply-position :identity [_ layers] (vec layers))

;; ---- Dodge ----

(defmethod apply-position :dodge [_ layers]
  (let [all-labels (vec (distinct (mapcat layer-group-labels layers)))
        n-groups (max 1 (count all-labels))
        label->idx (zipmap all-labels (range))
        dodge-ctx {:n-groups n-groups}
        dodge-compatible? (fn [layer]
                            (or (:groups layer) (:boxes layer) (:violins layer)))]
    (mapv
     (fn [layer]
       ;; Only attach :dodge-ctx to layers that will actually use it.
       ;; Marks like :tile, :contour, and :ridgeline have neither :groups,
       ;; :boxes, nor :violins — they ignore dodge silently, so don't
       ;; pollute their layer maps with a context they'll never read.
       (cond-> layer
         (dodge-compatible? layer) (assoc :dodge-ctx dodge-ctx)
         (:groups layer)
         (update :groups
                 (fn [gs]
                   (mapv #(assoc % :dodge-idx
                                 (get label->idx (:label %) 0))
                         gs)))
         (:boxes layer)
         (update :boxes
                 (fn [bs]
                   (mapv #(assoc % :dodge-idx
                                 (get label->idx (str (:color-category %)) 0))
                         bs)))
         (:violins layer)
         (update :violins
                 (fn [vs]
                   (mapv #(assoc % :dodge-idx
                                 (get label->idx (str (:color-category %)) 0))
                         vs)))))
     layers)))

;; ---- Stack ----

(defn- stack-rect-layer
  "Apply stack to a :rect layer with categorical bar counts.
   Adds :y0 and :y1 to each count entry."
  [layer]
  (let [{:keys [groups]} layer
        {:keys [adjusted-groups]}
        (reduce
         (fn [{:keys [adjusted-groups cum]} group]
           (let [new-counts
                 (mapv (fn [{:keys [category count]}]
                         (let [base (get cum category 0.0)]
                           {:category category
                            :count count
                            :y0 base
                            :y1 (+ base (double count))}))
                       (:counts group))
                 new-cum (reduce (fn [c {:keys [category y1]}]
                                   (assoc c category (double y1)))
                                 cum new-counts)]
             {:adjusted-groups (conj adjusted-groups
                                     (assoc group :counts new-counts))
              :cum new-cum}))
         {:adjusted-groups [] :cum {}}
         groups)]
    (assoc layer :groups adjusted-groups)))

(defn- stack-area-layer
  "Apply stack to an :area layer.
   Adds :y0s baseline vector to each group.
   NaN y-values are dropped before accumulation — otherwise a single
   NaN would propagate through the cumulative sum and corrupt every
   subsequent category in the stack.
   Duplicate x-values within a group are summed rather than dropped:
   `(into sorted-map ...)` would otherwise silently keep only the last
   (x, y) pair for each x."
  [layer]
  (let [{:keys [groups]} layer
        finite? (fn [v] (and (number? v) (not (Double/isNaN (double v)))))
        group-maps (mapv (fn [{:keys [xs ys]}]
                           (reduce (fn [m [x y]]
                                     (if (finite? y)
                                       (update m x (fnil + 0.0) (double y))
                                       m))
                                   (sorted-map)
                                   (map vector xs ys)))
                         groups)
        all-xs (vec (sort (distinct (mapcat keys group-maps))))
        {:keys [adjusted-groups]}
        (reduce
         (fn [{:keys [adjusted-groups cum]} [group gm]]
           (let [y0s (mapv #(get cum % 0.0) all-xs)
                 ys (mapv #(+ (get cum % 0.0) (get gm % 0.0)) all-xs)
                 new-cum (into cum (map vector all-xs ys))]
             {:adjusted-groups (conj adjusted-groups
                                     (assoc group
                                            :xs all-xs :ys ys :y0s y0s))
              :cum new-cum}))
         {:adjusted-groups [] :cum {}}
         (map vector groups group-maps))]
    (assoc layer :groups adjusted-groups)))

(defmethod apply-position :stack [_ layers]
  ;; Stacking rewrites y-values in every group/bar, so the layer's cached
  ;; :y-domain (populated by extract-layer from the pre-stack stat output)
  ;; is no longer correct. Strip it so consumers only trust the panel-level
  ;; domain computed by compute-global-y-domain.
  (mapv (fn [layer]
          (cond
            (:categories layer) (dissoc (stack-rect-layer layer) :y-domain)
            (:groups layer) (dissoc (stack-area-layer layer) :y-domain)
            :else layer))
        layers))

;; ---- Fill ----

(defn- normalize-fill-rect
  "Normalize bar counts per category to sum to 1.0."
  [layer]
  (let [groups (:groups layer)
        cat-totals (reduce (fn [acc g]
                             (reduce (fn [a {:keys [category count]}]
                                       (update a category (fnil + 0) count))
                                     acc
                                     (:counts g)))
                           {}
                           groups)
        normalized (mapv (fn [g]
                           (update g :counts
                                   (fn [counts]
                                     (mapv (fn [{:keys [category count]}]
                                             (let [total (get cat-totals category 1)]
                                               {:category category
                                                :count (if (pos? total)
                                                         (/ (double count) (double total))
                                                         0.0)}))
                                           counts))))
                         groups)]
    (assoc layer :groups normalized)))

(defmethod apply-position :fill [_ layers]
  ;; Fill normalizes to [0, 1] — the cached :y-domain is always stale after
  ;; this transform. Strip it (same reasoning as :stack).
  (mapv (fn [layer]
          (cond
            (:categories layer) (dissoc (-> layer normalize-fill-rect stack-rect-layer) :y-domain)
            (:groups layer) (dissoc (stack-area-layer layer) :y-domain)
            :else layer))
        layers))

;; ---- Entry point ----

(defmethod apply-position :default [position _]
  (if (and (vector? position) (= :doc (second position)))
    "(no description)"
    (throw (ex-info (str "Position must be :dodge, :stack, :fill, or nil, got: " (pr-str position))
                    {:position position}))))

(def ^:private position-order
  "Deterministic iteration order for apply-positions. Avoids non-determinism
   from group-by's hash-based map when a panel mixes position types."
  [:identity :dodge :stack :fill])

(defn- position-key
  "Which position cohort a layer is adjusted with. Normally its own
   `:position`, but a text layer grouped by the same categories a dodged
   layer is dodged by rides along with that dodge.

   Without this a label over dodged bars is placed at the band centre, where
   every group's label piles up on the boundary between two bars instead of
   sitting over its own (issue #13). The cohort is what makes them line up:
   `apply-position :dodge` derives one `:n-groups` and one label-to-index map
   for every layer in it, so a label cannot drift from the bar it names.

   Only text is carried in. Marks that read `:dodge-idx` but position other
   geometry -- errorbar, pointrange -- keep needing an explicit
   `:position :dodge`, which lands them in the cohort the ordinary way."
  [dodged-labels layer]
  (let [position (or (:position layer) :identity)
        labels (layer-group-labels layer)]
    (if (and (= :identity position)
             (= :text (:mark layer))
             (seq labels)
             (every? dodged-labels labels))
      :dodge
      position)))

(defn apply-positions
  "Apply position adjustments to all layers in a panel.
   Groups layers by position type and applies adjustments per group.
   Stack/fill: modifies y-values (data transform).
   Dodge: annotates groups with indices (layout annotation).

   The returned layers are in the input (pose) order, which becomes the
   paint order downstream. Grouping by position type is only an internal
   step so stack/dodge can compute geometry shared across same-position
   layers; it must not leak into paint order. (It used to: emitting in
   the canonical position order meant text/label marks -- position
   :identity -- always painted before, and so under, bar marks -- position
   :dodge/:stack -- regardless of the order the author added them.)"
  [layers]
  (let [;; Tag each layer with its original index so the author's order
        ;; can be restored after grouping. apply-position preserves
        ;; arbitrary keys on each layer map and the within-group order,
        ;; so this index survives every position transform.
        indexed (map-indexed (fn [i l] (assoc l :__layer-order i)) layers)
        dodged-labels (into #{} (comp (filter #(= :dodge (:position %)))
                                      (mapcat layer-group-labels))
                            indexed)
        by-pos (group-by #(position-key dodged-labels %) indexed)
        ;; Iterate known positions in canonical order first, then any
        ;; unknown ones, so the per-group computation is deterministic
        ;; regardless of group-by's hash-based key order.
        known (set position-order)
        ordered (concat position-order
                        (filter (complement known) (keys by-pos)))]
    (->> ordered
         (mapcat (fn [pos]
                   (when-let [ls (seq (get by-pos pos))]
                     (apply-position pos ls))))
         (sort-by :__layer-order)
         (mapv #(dissoc % :__layer-order)))))
