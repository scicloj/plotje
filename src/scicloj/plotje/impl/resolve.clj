(ns scicloj.plotje.impl.resolve
  (:require [clojure.string :as str]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.datetime :as dt-dt]
            [java-time.api :as jt]
            [scicloj.plotje.impl.aesthetics :as aes]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.temporal :as temporal]))

;; ---- Helpers ----

(defn column-ref?
  "True if v is a column reference (keyword or string).
   Both keyword and string column names are valid references."
  [v]
  (or (keyword? v) (string? v)))

(def positional-aesthetics
  "The aesthetics whose literal value becomes a constant column before
   anything else reads the mapping. Derived from
   `defaults/aesthetic-registry`, which is where a new one is added.

   Narrower than the glossary's positional aesthetics: the band bounds
   place a mark too, but their marks read the value straight from the
   mapping, so turning it into a column would take it from the only
   code that wants it."
  defaults/literal-to-column-aesthetics)

(def temporal-value?
  "True of a value a temporal axis reads as a date.
   `impl.temporal/temporal-value?`, named here because this namespace is
   where most callers already look."
  temporal/temporal-value?)

(defn literal-position?
  "True of a value that places a mark on its own, as `{:x 6.5}` does,
   rather than naming a column to read one from. Numbers are the whole of
   it today; a temporal value is coerced by the scale like any other, so
   it counts too."
  [v]
  (or (number? v) (temporal-value? v)))

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
          ;; A temporal type the dataset stores as :object -- a
          ;; java.util.Date, an OffsetDateTime. Asked of the one table
          ;; rather than named here: an OffsetDateTime was read as a
          ;; category while `temporal->epoch-ms` knew how to convert it.
          (temporal/temporal-value? (first (remove nil? c))) :temporal
          ;; Check actual values
          (every? number? (take 100 c)) :numerical
          :else :categorical)))))

(def type-override-values
  "What `:x-type`, `:y-type` and `:color-type` accept."
  #{:categorical :numerical :temporal})

(defn check-type-override!
  "Report a type override the column cannot carry out, naming the
   option and the column rather than dying in the scale several stages
   later.

   `:categorical` is the override that does something: every value can
   be the name of a group, so a column of numbers, dates or strings can
   all be read as categories, and reading numbers that way -- hours,
   years, identifiers -- is why the option exists.

   The other two only ever confirm what the column already holds. There
   is no number in \"setosa\" and no instant either, so `:numerical` on
   a column of strings has nothing to read, and neither has `:temporal`
   on a column of numbers. Both used to reach the cast, which reported
   a String or a LocalDate and named neither the column nor the option
   that sent it there."
  [option-key written ds col]
  (when (some? written)
    (when-not (type-override-values written)
      (throw (ex-info (str option-key " " (pr-str written) " is not a column type. "
                           "Accepted: " (vec (sort type-override-values)) ".")
                      {:option option-key :written written
                       :accepted (vec (sort type-override-values))})))
    (let [actual (when (and ds col) (column-type ds col))]
      (when (and actual
                 (not= :categorical written)
                 (not= written actual))
        (let [aesthetic (keyword (str/replace (name option-key) #"-type$" ""))]
          (throw (ex-info (str option-key " " written " names column " (pr-str col)
                               ", which holds " (name actual) " values. Only"
                               " :categorical can be asked of a column holding"
                               " something else, because every value can name a"
                               " group; a column is otherwise read as what it"
                               " holds. Write :categorical, or map " aesthetic
                               " to a " (name written) " column.")
                          {:option option-key :written written
                           :aesthetic aesthetic
                           :column col :column-type actual}))))))
  nil)

(def temporal->local-date-time
  "A temporal value as the `LocalDateTime` the tick generators read.
   `impl.temporal/->local-date-time`."
  temporal/->local-date-time)

(defn temporal->epoch-ms
  "Convert a temporal value to epoch-milliseconds (double), through the
   one reading `temporal-readings` gives it. Returns ##NaN for nil, and
   coerces a number as it stands.

   Every type goes by way of `LocalDateTime` at UTC rather than by a
   shortcut of its own: two routes to one number are two things to keep
   in step, and this list has already grown past the four it started
   with."
  [v]
  (cond
    (nil? v) ##NaN
    (temporal-value? v) (double (jt/to-millis-from-epoch
                                 (jt/instant (temporal->local-date-time v)
                                             (jt/zone-offset 0))))
    :else (double v)))

(defn epoch-ms->local-date-time
  "The inverse of `temporal->epoch-ms`, read at UTC -- the offset that
   function converts with, so a value round-trips.

   Needed wherever an epoch-ms number has to be shown as a date again:
   an axis holds epoch-ms, and wadogo's `:datetime` scale formats
   `LocalDateTime` and refuses a `LocalDate`."
  [ms]
  (java.time.LocalDateTime/ofInstant
   (java.time.Instant/ofEpochMilli (long (double ms)))
   java.time.ZoneOffset/UTC))

(defn format-local-date-time
  "A `LocalDateTime` as a date string, dropping a midnight time of day so
   a value that began life as a `LocalDate` reads back as one."
  [^java.time.LocalDateTime ldt]
  (if (= 0 (.getHour ldt) (.getMinute ldt) (.getSecond ldt))
    (str (.toLocalDate ldt))
    (.toString ldt)))

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

(defn- temporal-extent
  "Return [min max] of original temporal values in a column, as LocalDateTime.
   All temporal types are normalized to LocalDateTime for wadogo :datetime scale."
  [ds col]
  (let [vals (vec (remove nil? (ds col)))]
    (when (seq vals)
      (let [ldts (mapv temporal->local-date-time vals)]
        [(apply jt/min ldts) (apply jt/max ldts)]))))

;; ---- Resolve Draft Layer ----

(defn warn-unread-temporal!
  "Report a column of values that name a stretch of time rather than a
   moment, which an axis reads as categories.

   A `YearMonth` is the one a reader meets: grouping by month is what
   `->year-month` is for, and the column that comes back was drawn with
   one tick per value -- a hundred and eighty of them over fifteen
   years -- rather than on a calendar axis. `Year` and `MonthDay` are
   the same shape.

   Reported rather than converted. A `YearMonth` has no instant of its
   own, so reading it as the first of the month is a decision, and one
   the writer can make in a line. `dev-notes/backlog.md` holds the
   question of whether Plotje should make it."
  [aesthetic ds col]
  (when-let [c (and col (ds col))]
    (when-let [v (first (remove nil? c))]
      (when (temporal/names-a-period? v)
        (println (str "Warning: " aesthetic " names the column " (pr-str col)
                      ", which holds " (.getName (class v))
                      ". That names a stretch of time rather than a moment, so"
                      " it has no place on a calendar axis and is drawn as one"
                      " tick per value. A temporal axis reads "
                      temporal/accepted-names
                      ". To place these on a calendar, map each to the day it"
                      " starts: (tc/map-columns ds " (pr-str col) " [" (pr-str col)
                      "] #(.atDay % 1))."))))))

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
        _ (check-type-override! :x-type (:x-type v) ds x-res)
        _ (check-type-override! :y-type (:y-type v) ds y-res)
        _ (warn-unread-temporal! :x ds x-res)
        _ (warn-unread-temporal! :y ds y-res)
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
   a column reference or a fixed value it is drawn as.

   **The data decides**, uniformly: a value is a column reference when
   the layer's data carries a column of that exact name, and is drawn
   as it stands otherwise. That one rule replaces three that disagreed
   -- `:color` looked the value up, `:size` and `:alpha` called anything
   keyword-or-string a column without checking, and each was written
   out separately here. `impl.aesthetics/source` is where it now lives,
   and `impl.pose/validate-columns` reports a value that is neither a
   column nor something the aesthetic can draw, so nothing arrives here
   having failed both readings.

   `:text` keeps the older test on purpose. Its drawn reading exists
   only on a layer with no data -- `impl.pose/resolve-positional-values`
   holds it -- and this function is only ever called with a dataset, so
   asking the data here would turn a reported mistake into a layer that
   draws no labels.

   Returns a map with keys :color, :color-is-col?, :color-type, :fixed-color,
   :size, :size-is-col?, :fixed-size, :alpha, :alpha-is-col?, :fixed-alpha,
   :text-col, :tooltip-col, :fixed-tooltip."
  [ds v]
  (let [col-names (set (tc/column-names ds))
        ;; `:__source` and `:__scale` carry what an explicit mapping
        ;; said out loud; both are absent where one was written plainly,
        ;; which is what leaves the conventions in charge there.
        said-source (fn [k] (get-in v [:__source k]))
        said-scale  (fn [k] (get-in v [:__scale k]))
        column? (fn [k x] (and x (= :column (aes/source x col-names (said-source k)))))
        color-val (:color v)
        color-is-col? (column? :color color-val)
        ;; A color column drawn as it stands -- ggplot2's
        ;; `scale_colour_identity()`, spelled `{:scale false}`. Only
        ;; reachable by saying so: a column passes through its scale by
        ;; convention whatever it holds, so a column of hex codes is
        ;; three categories until the writer says otherwise.
        color-drawn? (and color-is-col?
                          (not (aes/scaled? :color {:source :column
                                                    :value color-val
                                                    :scale (said-scale :color)})))
        ;; Still classified, and still grouped by: a drawn color column
        ;; splits the layer into one group per distinct color exactly as
        ;; a category column does. All that changes is where each
        ;; group's color comes from.
        c-type (when color-is-col?
                 (check-type-override! :color-type (:color-type v) ds color-val)
                 (or (:color-type v) (column-type ds color-val)))
        fixed-color (when (and color-val (not color-is-col?)) color-val)
        size-val (:size v)
        size-is-col? (column? :size size-val)
        fixed-size (when (and size-val (not size-is-col?)) size-val)
        ;; A column told not to scale holds radii already --
        ;; `scale_size_identity()`. Reachable only by saying so, as
        ;; every identity reading is.
        size-drawn? (and size-is-col?
                         (not (aes/scaled? :size {:source :column
                                                  :value size-val
                                                  :scale (said-scale :size)})))
        alpha-val (:alpha v)
        alpha-is-col? (column? :alpha alpha-val)
        fixed-alpha (when (and alpha-val (not alpha-is-col?)) alpha-val)
        alpha-drawn? (and alpha-is-col?
                          (not (aes/scaled? :alpha {:source :column
                                                    :value alpha-val
                                                    :scale (said-scale :alpha)})))
        text-val (:text v)
        text-col (when (and text-val (column-ref? text-val)) text-val)
        ;; What a mark says on hover, read exactly as `:text` is: a
        ;; column names one string per row, and anything else is one
        ;; string for every mark of the layer.
        tooltip-val (:tooltip v)
        ;; Through `column?`, as `:color` and `:size` are, rather than
        ;; through `column-ref?` as `:text` is: a tooltip is written as
        ;; a string more often than not, and a string is column-shaped.
        ;; The data decides, which is the rule everywhere else.
        tooltip-is-col? (column? :tooltip tooltip-val)
        tooltip-col (when tooltip-is-col? tooltip-val)
        fixed-tooltip (when (and (some? tooltip-val) (not tooltip-is-col?))
                        tooltip-val)]
    {:color (when color-is-col? color-val)
     :color-is-col? color-is-col?
     :color-drawn? color-drawn?
     :color-type c-type
     :fixed-color fixed-color
     :size (when size-is-col? size-val)
     :size-is-col? size-is-col?
     :size-drawn? size-drawn?
     :fixed-size fixed-size
     :alpha (when alpha-is-col? alpha-val)
     :alpha-is-col? alpha-is-col?
     :alpha-drawn? alpha-drawn?
     :fixed-alpha fixed-alpha
     :text-col text-col
     :tooltip-col tooltip-col
     :fixed-tooltip fixed-tooltip}))

(defn validate-continuous-aesthetics
  "Throw a clear error when `:size`, `:alpha` or `:fill` names a
   categorical column. Without it the column reaches the encoder and
   dies casting a string to a number, naming neither the aesthetic nor
   the column."
  [ds v]
  (let [col-names (set (tc/column-names ds))]
    (doseq [k defaults/continuous-column-aesthetics
            :let [col (get v k)]
            :when (and col (column-ref? col) (contains? col-names col))]
      (when (= :categorical (column-type ds col))
        (throw (ex-info (str "Aesthetic " k " needs a numeric column, but "
                             (pr-str col) " holds categories. " k
                             " draws a magnitude, so there is nothing for a"
                             " category to be. To tell categories apart, map"
                             " the column to :color (a palette) or to :group"
                             " (one drawn group each).")
                        {:aesthetic k :column col :column-type :categorical}))))))

(defn infer-grouping
  "Build the grouping vector from explicit :group and categorical color column.
   Explicit groups are passed through; categorical color columns are appended.
   Returns a vector of column references (keywords or strings).

   A color column drawn as it stands -- `{:color {:column :c :scale
   false}}`, where the column already holds colors -- is not a grouping.
   Reading a column through its scale turns each distinct value into a
   category and splits the rows into a group per category: the stat is
   computed once per group, the legend says which group is which, and a
   bar divides its band between them. `:scale false` says the values are
   drawn rather than read, and the library already acts on that by
   drawing no legend for them, so no categories come out of the column
   and the rows are not split. `:size` and `:alpha` have never grouped in
   either spelling."
  [v color-type color-col color-drawn?]
  (let [explicit-group (let [g (:group v)]
                         (cond (nil? g) nil
                               (column-ref? g) [g]
                               (sequential? g) (vec g)
                               :else [g]))
        color-group (when (and (= color-type :categorical) (not color-drawn?))
                      [color-col])
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

(defn- column-phrase
  "How to describe a column in an error message: what it holds, or that
   it holds nothing.

   An empty column and an all-missing one are both typed `:boolean` by
   tech.ml.dataset, and `column-type` reads both as `:numerical` so the
   rest of the pipeline has something to work with. Saying \"is
   numerical\" of either states something about the data that is not
   true, and sends the reader to check a column type that is not the
   problem."
  [ds col]
  (let [c (when (and ds col) (get ds col))]
    (cond
      (nil? c) "is not in the data"
      (zero? (count c)) "has no rows"
      (every? nil? (take 100 c)) "has no values"
      :else "is numerical")))

(defn- no-values?
  "True where `column-phrase` would report the column as holding
   nothing, so a caller can choose different advice."
  [ds col]
  (not= "is numerical" (column-phrase ds col)))

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
              ;; A `:shape` naming no column is one symbol for the whole
              ;; layer, so it moves to `:fixed-shape` and leaves `:shape`
              ;; empty -- `collect-shapes` reads categories out of a
              ;; column, and there is no column and no category here.
              ;; Same shape of move as `:size` 7 becoming `:fixed-size`.
              ;; `:__source` is passed for the same reason every other
              ;; reading consults it: `{:value :circle}` on data that
              ;; carries a column called `:circle` is the symbol.
              (and (:shape v)
                   (= :value (aes/source (:shape v)
                                         (set (tc/column-names resolved-ds))
                                         (get-in v [:__source :shape]))))
              (-> (assoc :fixed-shape (:shape v)) (dissoc :shape))
              (and (:x-end v) (column-ref? (:x-end v)))
              (assoc :x-end (resolve-col-name resolved-ds (:x-end v))))
          _ (validate-continuous-aesthetics resolved-ds v)
          {:keys [color color-type color-drawn? fixed-color
                  size size-drawn? fixed-size
                  alpha alpha-drawn? fixed-alpha text-col
                  tooltip-col fixed-tooltip]} (resolve-aesthetics resolved-ds v)
          group (infer-grouping v color-type color color-drawn?)
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
                                   "but " (pr-str (:x v)) " " (column-phrase resolved-ds (:x v))
                                   " and " (pr-str (:y v)) " " (column-phrase resolved-ds (:y v)) ". "
                                   (if (or (no-values? resolved-ds (:x v))
                                           (no-values? resolved-ds (:y v)))
                                     (str "A layer that groups by category needs at least one row "
                                          "carrying a category.")
                                     (str "Override with {:x-type :categorical} or "
                                          "{:y-type :categorical} to treat a numeric column "
                                          "as categorical.")))
                              {:mark mark :x (:x v) :y (:y v)})))
          _ (when (and (contains? x-only-cat-marks mark)
                       (= stat (x-only-cat-marks mark))
                       (= x-type :numerical))
              (throw (ex-info (str "lay-" (name user-fn-name) " requires a categorical :x column, but "
                                   (pr-str (:x v)) " " (column-phrase resolved-ds (:x v)) ". "
                                   (if (no-values? resolved-ds (:x v))
                                     (str "A layer that groups by category needs at least one row "
                                          "carrying a category.")
                                     (str "Use a categorical column (e.g., species names) for the "
                                          "x-axis, or pass {:x-type :categorical} to treat a "
                                          "numeric column as categorical.")))
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
                                  :color-drawn? color-drawn?
                                  :size size :fixed-size fixed-size
                                  :size-drawn? size-drawn?
                                  :alpha alpha :fixed-alpha fixed-alpha
                                  :alpha-drawn? alpha-drawn?
                                  :text-col text-col
                                  :tooltip-col tooltip-col
                                  :fixed-tooltip fixed-tooltip)
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

