(ns scicloj.plotje.impl.resolve
  (:require [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.datetime :as dt-dt]
            [java-time.api :as jt]
            [scicloj.plotje.impl.defaults :as defaults]))

;; ---- Helpers ----

(defn column-ref?
  "True if v is a column reference (keyword or string).
   Both keyword and string column names are valid references."
  [v]
  (or (keyword? v) (string? v)))

(def positional-aesthetics
  "The aesthetics that place a mark, and so may be given as a value.
   Named for the glossary's sake: `:position` there is the dodge / stack
   / fill adjustment, which these have nothing to do with."
  [:x :y :x-end :y-end])

(defn literal-position?
  "True of a value that places a mark on its own, as `{:x 6.5}` does,
   rather than naming a column to read one from. Numbers are the whole of
   it today; a temporal value is coerced by the scale like any other, so
   it counts too."
  [v]
  (or (number? v)
      (instance? java.time.LocalDate v)
      (instance? java.time.LocalDateTime v)
      (instance? java.time.Instant v)
      (instance? java.util.Date v)))

;; The fields declared on the records below are a minimal subset for
;; ergonomic construction; instances carry more keys, and the
;; canonical contract for each record is a Malli schema. See:
;;
;; - Plan           -> `LeafPlanSchema`      in `impl.plan-schema`
;; - CompositePlan  -> `CompositePlanSchema` in `impl.plan-schema`
;; - PlanLayer      -> `PlanLayer`           in `impl.plan-schema`
;; - LeafDraft      -> `LeafDraftSchema`     in `impl.draft-schema`
;; - CompositeDraft -> `CompositeDraftSchema` in `impl.draft-schema`

(defrecord Plan [panels width height])

(defrecord CompositePlan [width height sub-plots chrome])

(defrecord CompositeDraft [width height sub-drafts chrome-spec layout])

(defrecord LeafDraft [layers opts])

(defn leaf-plan?
  "True if x is a leaf plan (single-pose resolved geometry)."
  [x]
  (instance? Plan x))

(defn composite-plan?
  "True if x is a composite plan (a tree of sub-plots with chrome)."
  [x]
  (instance? CompositePlan x))

(defn plan?
  "True if x is a plan (leaf or composite)."
  [x]
  (or (leaf-plan? x) (composite-plan? x)))

(defn composite-draft?
  "True if x is a composite draft (a tree of sub-drafts with chrome-spec)."
  [x]
  (instance? CompositeDraft x))

(defn leaf-draft?
  "True if x is a leaf draft (a vector of draft layers + pose-level opts)."
  [x]
  (instance? LeafDraft x))

(defn draft?
  "True if x is a draft (leaf or composite)."
  [x]
  (or (leaf-draft? x) (composite-draft? x)))

(defrecord PlanLayer [mark style])

(defn plan-layer?
  "True if x is a plan-layer (resolved geometry for one mark)."
  [x]
  (instance? PlanLayer x))

(defrecord LayerType [mark stat])

(defn layer-type?
  "True if x is a layer-type (mark + stat + position bundle)."
  [x]
  (instance? LayerType x))

;; ---- Layer ----

(def annotation-marks
  "Mark types that render as annotations (rules, bands) rather than data layers."
  #{:rule-h :rule-v :band-h :band-v})

;; ---- Cross ----

(defn cross
  "Cartesian product of two sequences."
  [xs ys]
  (for [x xs, y ys] [x y]))

;; ---- Faceting ----

(defn resolve-col-name
  "Look up a column name in a dataset. Returns the name unchanged
   whether or not the dataset contains it; the existence check is
   informational. Matching is strict: `:x` does not match a column
   literally named `\"x\"`. The reference and the dataset column name
   must be identical (same type, same characters)."
  [_ds ref]
  ref)

;; ---- Column Type Detection ----

(defn column-type
  "Classify a dataset column as `:categorical`, `:numerical`, or `:temporal`.
   The column name must literally match a column in the dataset."
  [ds col]
  (let [resolved (resolve-col-name ds col)
        c (when resolved (ds resolved))
        n (count c)]
    (if (or (nil? c) (zero? n))
      ;; Missing or empty column — treat as numerical (can't infer, let
      ;; downstream handle gracefully)
      :numerical
      (let [dt (dtype/elemwise-datatype c)
            t (try (tcc/typeof c) (catch Exception _ nil))
            ;; All-missing columns (e.g., [##NaN ##NaN]) get :boolean dtype
            ;; with nil values. Treat as numerical since the input was numeric.
            all-missing? (every? nil? (take 100 c))]
        (cond
          all-missing? :numerical
          ;; Check dtype first to catch numeric columns
          (#{:float32 :float64 :int8 :int16 :int32 :int64} dt) :numerical
          (#{:string :keyword :symbol :text} t) :categorical
          ;; Check for temporal types via dtype-next metadata
          (dt-dt/datetime-datatype? dt) :temporal
          ;; Fallback for java.util.Date (:object dtype)
          (instance? java.util.Date (first c)) :temporal
          ;; Check actual values
          (every? number? (take 100 c)) :numerical
          :else :categorical)))))

(defn temporal->epoch-ms
  "Convert a temporal value to epoch-milliseconds (double).
   Accepts LocalDate, LocalDateTime, Instant, and java.util.Date.
   Returns ##NaN for nil input."
  [v]
  (cond
    (nil? v) ##NaN
    (jt/instant? v) (double (jt/to-millis-from-epoch v))
    (jt/local-date-time? v) (double (jt/to-millis-from-epoch (jt/instant v (jt/zone-offset 0))))
    (jt/local-date? v) (double (jt/to-millis-from-epoch (jt/instant (jt/local-date-time v (jt/local-time 0)) (jt/zone-offset 0))))
    (instance? java.util.Date v) (double (.getTime ^java.util.Date v))
    :else (double v)))

(defn- temporalize-column
  "Replace a temporal column in a dataset with its epoch-ms numeric equivalent.
   Uses vectorized dt-dt/datetime->epoch for typed temporal columns;
   falls back to scalar map-columns for java.util.Date (:object dtype).
   Casts to :float64 so NaN from nil temporal values is recognized as missing."
  [ds col]
  (if (dt-dt/datetime-datatype? (dtype/elemwise-datatype (ds col)))
    (tc/add-column ds col (dtype/elemwise-cast
                           (dt-dt/datetime->epoch :epoch-milliseconds (ds col))
                           :float64))
    (tc/map-columns ds col [col] temporal->epoch-ms)))

(defn- temporal->local-date-time
  "Convert any supported temporal value to LocalDateTime (required by wadogo :datetime scale).
   LocalDate gets midnight, Instant and java.util.Date get UTC conversion."
  [v]
  (cond
    (jt/local-date-time? v) v
    (jt/local-date? v) (jt/local-date-time v (jt/local-time 0))
    (jt/instant? v) (jt/local-date-time v "UTC")
    (instance? java.util.Date v) (jt/local-date-time (jt/instant v) "UTC")
    :else v))

(defn- temporal-extent
  "Return [min max] of original temporal values in a column, as LocalDateTime.
   All temporal types are normalized to LocalDateTime for wadogo :datetime scale."
  [ds col]
  (let [vals (vec (remove nil? (ds col)))]
    (when (seq vals)
      (let [ldts (mapv temporal->local-date-time vals)]
        [(apply jt/min ldts) (apply jt/max ldts)]))))

;; ---- Resolve Draft Layer ----

(defn infer-column-types
  "Detect x and y column types (`:categorical`, `:numerical`, `:temporal`).
   Temporal columns are converted to epoch-ms numbers; their original
   extents (as `LocalDateTime`) are preserved for wadogo `:datetime` ticks.
   Returns a map with keys `:ds`, `:x-type`, `:y-type`, `:x-temporal?`,
   `:y-temporal?`, `:x-temporal-extent`, `:y-temporal-extent`,
   `:x-resolved`, `:y-resolved`."
  [ds v]
  (let [x-res (resolve-col-name ds (:x v))
        y-res (resolve-col-name ds (:y v))
        x-end-res (resolve-col-name ds (:x-end v))
        x-type (or (:x-type v) (column-type ds x-res))
        ;; When x and y reference the same column, propagate x-type to y-type
        ;; rather than returning nil — callers (e.g., `validate-numeric-column`)
        ;; rely on y-type being populated for validation.
        y-type (or (:y-type v) (when y-res
                                 (if (= x-res y-res)
                                   x-type
                                   (column-type ds y-res))))
        ;; If :x-end is given, it must share an axis with :x: same column type
        ;; (both numerical or both temporal) and same data-type family. Catch
        ;; mismatches early so the user does not see a downstream NaN/cast crash.
        x-end-type (when x-end-res (column-type ds x-end-res))
        _ (when (and x-end-type (not= x-end-type x-type))
            (throw (ex-info (str ":x-end column " (pr-str (:x-end v))
                                 " has type " x-end-type
                                 " but :x " (pr-str (:x v))
                                 " has type " x-type
                                 ". They must share the same axis type "
                                 "(both numerical or both temporal).")
                            {:x (:x v) :x-end (:x-end v)
                             :x-type x-type :x-end-type x-end-type})))
        x-temporal? (= x-type :temporal)
        y-temporal? (= y-type :temporal)
        ;; If x is temporal, x-end (when present) is implicitly temporal too —
        ;; they must share an axis. Extend the temporal extent across both.
        x-temp-extent (when x-temporal?
                        (let [xe (temporal-extent ds x-res)
                              xee (when x-end-res (temporal-extent ds x-end-res))]
                          (if (and xe xee)
                            [(jt/min (first xe) (first xee))
                             (jt/max (second xe) (second xee))]
                            xe)))
        y-temp-extent (when y-temporal? (temporal-extent ds y-res))
        ds (cond-> ds
             x-temporal? (temporalize-column x-res)
             (and x-temporal? x-end-res) (temporalize-column x-end-res)
             y-temporal? (temporalize-column y-res))]
    {:ds ds
     :x-type (if x-temporal? :numerical x-type)
     :y-type (if y-temporal? :numerical y-type)
     :x-temporal? x-temporal?
     :y-temporal? y-temporal?
     :x-temporal-extent x-temp-extent
     :y-temporal-extent y-temp-extent
     :x-resolved x-res
     :y-resolved y-res}))

(defn resolve-aesthetics
  "Classify each aesthetic channel (:color, :size, :alpha, :text) as either
   a column reference or a fixed literal value.
   For :color, a string value is checked against dataset column names
   (both string and keyword) — if it matches, it's treated as a column ref;
   otherwise it's a literal color string.
   Returns a map with keys :color, :color-is-col?, :color-type, :fixed-color,
   :size, :size-is-col?, :fixed-size, :alpha, :alpha-is-col?, :fixed-alpha,
   :text-col."
  [ds v]
  (let [color-val (:color v)
        color-is-col? (and color-val (column-ref? color-val)
                           ;; A string :color is a column reference only when a
                           ;; column with that exact name exists; otherwise it
                           ;; is treated as a literal CSS color.
                           (contains? (set (tc/column-names ds)) color-val))
        c-type (when color-is-col?
                 (or (:color-type v) (column-type ds color-val)))
        fixed-color (when (and color-val (not color-is-col?)) color-val)
        size-val (:size v)
        size-is-col? (and size-val (column-ref? size-val))
        fixed-size (when (and size-val (not size-is-col?)) size-val)
        alpha-val (:alpha v)
        alpha-is-col? (and alpha-val (column-ref? alpha-val))
        fixed-alpha (when (and alpha-val (not alpha-is-col?)) alpha-val)
        text-val (:text v)
        text-col (when (and text-val (column-ref? text-val)) text-val)]
    {:color (when color-is-col? color-val)
     :color-is-col? color-is-col?
     :color-type c-type
     :fixed-color fixed-color
     :size (when size-is-col? size-val)
     :size-is-col? size-is-col?
     :fixed-size fixed-size
     :alpha (when alpha-is-col? alpha-val)
     :alpha-is-col? alpha-is-col?
     :fixed-alpha fixed-alpha
     :text-col text-col}))

(defn infer-grouping
  "Build the grouping vector from explicit :group and categorical color column.
   Explicit groups are passed through; categorical color columns are appended.
   Returns a vector of column references (keywords or strings)."
  [v color-type color-col]
  (let [explicit-group (let [g (:group v)]
                         (cond (nil? g) nil
                               (column-ref? g) [g]
                               (sequential? g) (vec g)
                               :else [g]))
        color-group (when (= color-type :categorical) [color-col])
        group (vec (distinct (concat (or explicit-group color-group [])
                                     (when (and color-group explicit-group) color-group))))]
    group))

(def ^:private x-only-stats
  "Stats that consume only an x column and synthesize y themselves
   (counts, bins, densities). Used to permit x-only draft-layers for marks
   driven by these stats even when the layer-type registry's :x-only flag
   is missing (e.g., tests that construct draft-layers directly)."
  #{:bin :count :density})

(defn infer-layer-type
  "Choose mark and stat from column types when the user hasn't specified them.
   Rules:
     - x only, categorical       → :rect    + :count    (bar chart)
     - x only, non-categorical   → :bar     + :bin      (histogram)
     - temporal x + numerical y  → :line    + :identity (time-series line)
     - categorical x + numerical y → :boxplot + :boxplot (vertical)
     - numerical x + categorical y → :boxplot + :boxplot (horizontal)
     - otherwise                 → :point   + :identity (scatter)
   `x-type`/`y-type` come from `infer-column-types`, which reports
   temporal columns as `:numerical` (they're stored as epoch-ms);
   `x-temporal?`/`y-temporal?` flag the original temporal classification.
   When the user provides an explicit mark, stat defaults to :identity
   unless they also provided an explicit stat."
  [v x-type y-type x-temporal? y-temporal?]
  (let [diagonal? (= (:x v) (:y v))
        [default-mark default-stat]
        (cond
          ;; x only (or diagonal): count categories or bin numbers
          (or diagonal? (nil? (:y v)))
          (if (= x-type :categorical) [:rect :count] [:bar :bin])
          ;; temporal x + numerical y → time-series line
          (and x-temporal? (= y-type :numerical) (not y-temporal?))
          [:line :identity]
          ;; categorical x + numerical y → boxplot
          (and (= x-type :categorical) (= y-type :numerical))
          [:boxplot :boxplot]
          ;; numerical x + categorical y → horizontal boxplot
          (and (= x-type :numerical) (= y-type :categorical))
          [:boxplot :boxplot]
          ;; everything else: scatter
          :else [:point :identity])
        mark (or (:mark v) default-mark)
        ;; When the user fixed a mark (e.g. via lay-bar -> :rect) but not a
        ;; stat, default the stat. A :rect mark is a bar: with no y column it
        ;; counts categories (:count), with a y column it uses the y as the
        ;; bar height (:identity). Any other fixed mark defaults to :identity.
        ;; An explicit :stat always overrides this -- the same infer-then-
        ;; override rule the rest of the pipeline follows.
        stat (or (:stat v)
                 (cond
                   (nil? (:mark v))                        default-stat
                   (and (= (:mark v) :rect) (nil? (:y v))) :count
                   :else                                   :identity))]
    {:mark mark :stat stat}))

(defn resolve-draft-layer
  "Resolve a single draft layer: infer column types, aesthetics, grouping, and layer type.
   Delegates to `infer-column-types`, `resolve-aesthetics`, `infer-grouping`,
   and `infer-layer-type` — each named for the inference step it performs.
   Also normalizes user-facing shorthand options:
     - `:bandwidth` → `:cfg {:<stat>-bandwidth ...}` (routed per stat)
     - `:tile` with `:fill` → stat `:identity`"
  [v]
  (if-not (:data v)
    v
    (let [ds (let [d (:data v)] (if (tc/dataset? d) d (tc/dataset d)))
          {:keys [x-type y-type x-temporal? y-temporal?
                  x-temporal-extent y-temporal-extent
                  x-resolved y-resolved]
           resolved-ds :ds} (infer-column-types ds v)
          ;; Update v with resolved column names so downstream code
          ;; (stat.clj, extract.clj) can use (ds (:x v)) directly.
          v (cond-> v
              x-resolved (assoc :x x-resolved)
              y-resolved (assoc :y y-resolved))
          ;; Also resolve aesthetic column refs
          v (cond-> v
              (and (:size v) (column-ref? (:size v)))
              (assoc :size (resolve-col-name resolved-ds (:size v)))
              (and (:alpha v) (column-ref? (:alpha v)))
              (assoc :alpha (resolve-col-name resolved-ds (:alpha v)))
              (and (:text v) (column-ref? (:text v)))
              (assoc :text (resolve-col-name resolved-ds (:text v)))
              (and (:group v) (column-ref? (:group v)))
              (assoc :group (resolve-col-name resolved-ds (:group v)))
              (and (:x-end v) (column-ref? (:x-end v)))
              (assoc :x-end (resolve-col-name resolved-ds (:x-end v))))
          {:keys [color color-type fixed-color
                  size fixed-size alpha fixed-alpha text-col]} (resolve-aesthetics resolved-ds v)
          group (infer-grouping v color-type color)
          {:keys [mark stat]} (infer-layer-type v x-type y-type x-temporal? y-temporal?)
          ;; Validate that category-grouping marks have a categorical axis.
          ;; :boxplot and :violin accept a categorical axis on either x or y
          ;; (boxplot renders horizontally when y is categorical). The others
          ;; currently only support categorical x; they will crash the plan
          ;; otherwise, so the pre-check produces a friendlier error.
          both-axes-marks  {:boxplot :boxplot :violin :violin}
          x-only-cat-marks {:lollipop :identity :summary :summary
                            :ridgeline :violin :pointrange :summary}
          both-numerical?  (and (= x-type :numerical) (= y-type :numerical))
          ;; Prefer the user-facing layer-function name (:layer-type) over the
          ;; internal :mark for error messages. The :layer-type key is stamped
          ;; onto the draft layer by resolve-layer-type-info in pose.clj;
          ;; fall back to :mark if it is missing (e.g. a raw layer-type map).
          user-fn-name (or (:layer-type v) mark)
          _ (when (and (contains? both-axes-marks mark)
                       (= stat (both-axes-marks mark))
                       both-numerical?)
              (throw (ex-info (str "lay-" (name user-fn-name) " requires a categorical column on either :x or :y, "
                                   "but both " (pr-str (:x v)) " and " (pr-str (:y v))
                                   " are numerical. Override with "
                                   "{:x-type :categorical} or {:y-type :categorical} "
                                   "to treat a numeric column as categorical.")
                              {:mark mark :x (:x v) :y (:y v)})))
          _ (when (and (contains? x-only-cat-marks mark)
                       (= stat (x-only-cat-marks mark))
                       (= x-type :numerical))
              (throw (ex-info (str "lay-" (name user-fn-name) " requires a categorical :x column, but "
                                   (pr-str (:x v)) " is numerical. Use a categorical column "
                                   "(e.g., species names) for the x-axis, or pass "
                                   "{:x-type :categorical} to treat a numeric column as categorical.")
                              {:mark mark :x (:x v) :x-type x-type})))
          _ (when (and (contains? x-only-cat-marks mark)
                       (= stat (x-only-cat-marks mark))
                       (= y-type :categorical))
              (throw (ex-info (str "lay-" (name user-fn-name) " requires a numerical :y column, "
                                   "but :y-type was declared :categorical. Drop the :y-type "
                                   "override; to flip the chart so categories appear on the "
                                   "visual y-axis, add (pj/coord :flip).")
                              {:mark mark :y (:y v) :y-type y-type})))
          ;; Reject x-only draft-layers (no :y) for layer types that require y.
          ;; Otherwise prepare-points silently fabricates y=0 for every
          ;; point and renders a flat line at the bottom of a [0, 1] domain.
          ;; Three sources of x-only permission:
          ;;   1. :x-only true from the layer-type registry (e.g., :histogram, :rug)
          ;;   2. stat is in `x-only-stats` — :bin/:count/:density synthesize y
          ;;      from x alone (covers the :rect mark + bar stat too)
          ;;   3. mark is :rug, which is structurally x-only even when
          ;;      constructed without the layer-type registry
          _ (when (and (nil? y-resolved)
                       (not (:x-only v))
                       (not (contains? x-only-stats stat))
                       (not= :rug mark))
              (throw (ex-info (str ":" (name mark) " requires both :x and :y columns. "
                                   "Either pass a y column (e.g., (pj/lay-" (name mark)
                                   " data :x :y)) or use an x-only mark like histogram, "
                                   "density, bar, or rug.")
                              {:mark mark :x (:x v)})))
          resolved (cond-> (assoc v :data resolved-ds :x-type x-type :y-type y-type
                                  :color-type color-type :group group :mark mark :stat stat
                                  :color color :fixed-color fixed-color
                                  :size size :fixed-size fixed-size
                                  :alpha alpha :fixed-alpha fixed-alpha
                                  :text-col text-col)
                     x-temporal? (assoc :x-temporal? true)
                     y-temporal? (assoc :y-temporal? true)
                     x-temporal-extent (assoc :x-temporal-extent x-temporal-extent)
                     y-temporal-extent (assoc :y-temporal-extent y-temporal-extent))
          ;; Normalize :bandwidth shorthand to the stat-specific cfg key.
          ;; :loess -> :loess-bandwidth; :density-2d -> :kde2d-bandwidth;
          ;; :density and :violin read :kde-bandwidth.
          bw (:bandwidth resolved)
          resolved (if bw
                     (let [cfg-key (case (:stat resolved)
                                     :loess :loess-bandwidth
                                     :density-2d :kde2d-bandwidth
                                     :kde-bandwidth)]
                       (-> resolved (dissoc :bandwidth)
                           (assoc-in [:cfg cfg-key] bw)))
                     resolved)
          ;; Tile + default bin2d stat with a user-supplied :fill (or
          ;; :color as a synonym) → override stat to :identity so the
          ;; pre-computed fill values drive the tile colors directly.
          ;; Only applies to lay-tile (which defaults to :bin2d) -- NOT
          ;; to lay-density-2d (:density-2d) or lay-contour, which intentionally
          ;; compute their own fill values from x/y.
          ;; Accepting :color keeps lay-tile friendly for users who
          ;; reach for :color by habit from the other marks. :fill wins
          ;; when both are set.
          tile-override? (and (= (:mark resolved) :tile)
                              (= (:stat resolved) :bin2d)
                              (or (:fill resolved) (:color resolved)))
          resolved (if tile-override?
                     (cond-> (assoc resolved :stat :identity)
                       ;; Promote :color to :fill when :fill is absent so
                       ;; the downstream extract path finds the data.
                       (and (not (:fill resolved)) (:color resolved))
                       (assoc :fill (:color resolved)))
                     resolved)]
      resolved)))

