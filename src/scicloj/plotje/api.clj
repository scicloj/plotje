(ns scicloj.plotje.api
  "Public API for plotje -- composable plotting in Clojure."
  (:require [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.impl.pose :as pose]
            [scicloj.plotje.impl.compositor :as compositor]
            [scicloj.plotje.impl.plan :as plan]
            [scicloj.plotje.impl.plan-schema :as ss]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.render :as render-impl]
            [scicloj.plotje.impl.stat :as stat]
            [scicloj.plotje.impl.extract :as extract]
            [scicloj.plotje.impl.position :as position]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.impl.coord :as coord]
            [scicloj.plotje.impl.file :as pf]
            [scicloj.plotje.impl.frames :as frames-impl]
            [scicloj.plotje.render.membrane :as membrane]
            [scicloj.plotje.impl.membrane :as membrane-schema]
            [scicloj.plotje.render.composite]
            [scicloj.plotje.render.mark :as mark]
            [scicloj.plotje.render.svg :as svg]
            [scicloj.plotje.layer-type :as layer-type]
            [clojure.set :as set]
            [clojure.string :as str]
            [tablecloth.api :as tc]
            [scicloj.kindly.v4.kind :as kind]))

;; ---- Type predicates ----

(defn plan?
  "Return true if x is a plan (leaf or composite) -- the resolved
   geometry returned by `pj/plan`."
  [x]
  (resolve/plan? x))

(defn leaf-plan?
  "Return true if x is a leaf plan (single-pose resolved geometry)."
  [x]
  (resolve/leaf-plan? x))

(defn composite-plan?
  "Return true if x is a composite plan (a tree of sub-plots with
   shared chrome)."
  [x]
  (resolve/composite-plan? x))

(defn composite-draft?
  "Return true if x is a composite draft (a tree of sub-drafts with
   shared chrome-spec, returned by `pj/draft` on a composite pose)."
  [x]
  (resolve/composite-draft? x))

(defn leaf-draft?
  "Return true if x is a leaf draft (a `LeafDraft` record carrying
   `:layers` -- a vector of layer maps -- and `:opts` -- the
   pose-level options that flow into the plan stage)."
  [x]
  (resolve/leaf-draft? x))

(defn draft?
  "Return true if x is a draft -- the intermediate representation
   produced by `pj/pose->draft` (and so by `pj/draft`). A draft is
   either a `LeafDraft` record (leaf pose) or a `CompositeDraft`
   record (composite pose). Used by cross-stage misuse guards on
   `pj/plan` and `pj/plot`."
  [x]
  (resolve/draft? x))

(defn plan-layer?
  "Return true if x is a plan-layer (resolved geometry for one mark)."
  [x]
  (resolve/plan-layer? x))

(defn layer-type?
  "Return true if x is a layer type (mark + stat + position bundle from the registry)."
  [x]
  (resolve/layer-type? x))

(defn membrane?
  "Return true if x is a `PlotjeMembrane` -- the value returned by
   `pj/plan->membrane` and `pj/membrane`. A `PlotjeMembrane` is a
   Membrane UI component (implements `IOrigin`, `IBounds`,
   `IChildren`) carrying the rendered drawables and plan-derived
   width/height; the plot title rides as `:plotje/title`."
  [x]
  (membrane/membrane? x))

(defn- expect-type
  "Validate that x is of the expected type. Throws with helpful message if not."
  [x pred expected-name fn-name]
  (when-not (pred x)
    (throw (ex-info (str fn-name " expects a " expected-name ". "
                         (cond (resolve/plan? x) "Got a plan."
                               :else (str "Got: " (type x) ".")))
                    {:function fn-name :expected expected-name :got-type (str (type x))}))))

;; ---- Configuration ----

(defmacro with-config
  "Execute body with thread-local config overrides.
   Overrides take precedence over `set-config!` and defaults,
   but plot options still win.

   - `(with-config {:theme {:bg \"#FFF\"}} (plot ...))`"
  [config-map & body]
  `(binding [defaults/*config* ~config-map]
     ~@body))

(defn config
  "Return the effective resolved configuration as a map.
   Merges: library defaults < `plotje.edn` < `set-config!` < `*config*`.
   Useful for inspecting which values are in effect.

   - `(config)` -- show current resolved config."
  []
  (defaults/config))

(def config-key-docs
  "Documentation metadata for configuration keys.
   Maps each config key to [category description].
   Use with (pj/config) to build reference tables."
  defaults/config-key-docs)

(def plot-option-docs
  "Documentation for plot-level option keys.
   These are accepted by pj/options, pj/plan, and pj/plot but are
   inherently per-plot (text content or nested config override).
   Maps each key to [category description]."
  defaults/plot-option-docs)

(def layer-option-docs
  "Documentation for layer option keys accepted by lay- functions.
   Maps each key to a description string."
  layer-type/layer-option-docs)

(def shape-symbols
  "The marker symbols a categorical `:shape` mapping draws with, in the
   order they are assigned to categories. A plot with more categories
   than this repeats a symbol, so two categories cannot be told apart;
   that warns at plan time. Pass a selection of these as `:values` to
   `(pj/scale pose :shape {:values [...]})` to choose them yourself."
  defaults/shape-syms)

(defn set-config!
  "Set global config overrides. Persists across calls until reset.

   - `(set-config! {:palette :dark2 :theme {:bg \"#FFFFFF\"}})` -- override
     palette and background.
   - `(set-config! nil)` -- reset to defaults."
  [m]
  (defaults/set-config! m))

(defn layer-type-lookup
  "Look up a registered layer type by keyword. Returns the layer-type map
   (with `:mark`, `:stat`, `:position`, `:doc`), or `nil` if not found.

   - `(layer-type-lookup :histogram)` returns `{:mark :bar, :stat :bin, ...}`."
  [k]
  (layer-type/lookup k))

(defn registered-layer-types
  "Return all registered layer types as a map of keyword -> layer-type map.
   Useful for generating documentation tables."
  []
  (layer-type/registered))

(defn mark-doc
  "Return the prose description for a mark keyword.
   Returns `\"(no description)\"` if no `[:key :doc]` defmethod is registered.

   - `(mark-doc :point)` returns `\"Filled circle\"`."
  [k]
  (try
    (let [r (extract/extract-layer {:mark [k :doc]} nil nil nil)]
      (if (string? r) r "(no description)"))
    (catch Exception _ "(no description)")))

(defn stat-doc
  "Return the prose description for a stat keyword.
   Returns `\"(no description)\"` if no `[:key :doc]` defmethod is registered.

   - `(stat-doc :bin)` returns `\"Bin numerical values into ranges\"`."
  [k]
  (try
    (let [r (stat/compute-stat {:stat [k :doc]})]
      (if (string? r) r "(no description)"))
    (catch Exception _ "(no description)")))

(defn position-doc
  "Return the prose description for a position keyword.
   Returns `\"(no description)\"` if no `[:key :doc]` defmethod is registered.

   - `(position-doc :dodge)` returns `\"Shift groups side-by-side within a band\"`."
  [k]
  (try
    (let [r (position/apply-position [k :doc] nil)]
      (if (string? r) r "(no description)"))
    (catch Exception _ "(no description)")))

(defn membrane-mark-doc
  "Return the prose description for how a mark renders to membrane drawables.
   Returns `\"(no description)\"` if no `[:key :doc]` defmethod is registered.

   - `(membrane-mark-doc :point)` returns `\"Translated colored rounded-rectangles\"`."
  [k]
  (try
    (let [r (mark/layer->membrane {:mark [k :doc]} nil)]
      (if (string? r) r "(no description)"))
    (catch Exception _ "(no description)")))

(defn scale-doc
  "Return the prose description for a scale keyword.
   Returns `\"(no description)\"` if no `[:key :doc]` defmethod is registered.

   - `(scale-doc :linear)` returns `\"Continuous linear mapping\"`."
  [k]
  (try
    (let [r (scale/make-scale [k :doc] nil nil)]
      (if (string? r) r "(no description)"))
    (catch Exception _ "(no description)")))

(defn coord-doc
  "Return the prose description for a coordinate type keyword.
   Returns `\"(no description)\"` if no `[:key :doc]` defmethod is registered.

   - `(coord-doc :polar)` returns `\"Radial mapping: x->angle, y->radius\"`."
  [k]
  (try
    (let [r (coord/make-coord [k :doc] nil nil nil nil nil)]
      (if (string? r) r "(no description)"))
    (catch Exception _ "(no description)")))

;; ---- Cross ----

(defn cross
  "Build a vector of `[x y]` pairs from two column-name sequences. Pair
   with `pj/pose` for SPLOM grids: when an MxN rectangle of pairs is
   threaded through `pj/pose`, the result is an MxN composite with
   shared scales.

   - `(pj/cross [:a :b] [:c :d])` returns `[[:a :c] [:a :d] [:b :c] [:b :d]]`."
  [xs ys]
  (when-not (sequential? xs)
    (throw (ex-info (str "pj/cross expects two sequentials of column"
                         " references, got xs: " (pr-str (type xs))
                         " (" (pr-str xs) "). Use (pj/cross [:a :b]"
                         " [:c :d]) for a 2x2 grid.")
                    {:caller "pj/cross" :argument :xs :value xs})))
  (when-not (sequential? ys)
    (throw (ex-info (str "pj/cross expects two sequentials of column"
                         " references, got ys: " (pr-str (type ys))
                         " (" (pr-str ys) "). Use (pj/cross [:a :b]"
                         " [:c :d]) for a 2x2 grid.")
                    {:caller "pj/cross" :argument :ys :value ys})))
  (when (or (empty? xs) (empty? ys))
    (throw (ex-info (str "pj/cross got an empty sequence (xs: "
                         (pr-str xs) ", ys: " (pr-str ys) "). Provide"
                         " at least one column reference per side.")
                    {:caller "pj/cross" :xs xs :ys ys})))
  (resolve/cross xs ys))

;; ---- Pipeline Internals ----

(defn draft->plan
  "Single-step transition: convert a draft into a plan. Dispatches on
   draft shape -- a `LeafDraft` carries `:layers` and pose-level `:opts`
   that flow into `plan/draft->plan`; a `CompositeDraft` goes through
   `compositor/composite-draft->plan` (which uses the chrome-spec already
   baked in at draft emission).

   Plan-stage opts (`:width`, `:height`, `:title`, ...) ride on the
   draft itself -- on the `LeafDraft`'s `:opts` for leaves, on the
   `CompositeDraft`'s chrome-spec for composites. Set them on the pose
   via `pj/options` before drafting.

   - `(draft->plan (draft pose))`"
  [draft]
  (expect-type draft draft? "draft (from pj/draft)" "pj/draft->plan")
  (if (resolve/composite-draft? draft)
    (compositor/composite-draft->plan draft)
    (plan/draft->plan (:layers draft)
                      (or (:opts draft) {}))))

(defn draft->membrane
  "Compose draft -> plan -> membrane. The 2-arity takes an opts map
   for `plan->membrane` (e.g. `{:tooltip true}`).

   Render-stage options set on the original pose via `pj/options`
   (`:theme`, `:palette`, ...) ride on the draft's `:opts` and form
   the base; any opts passed here override them per key. This keeps
   the explicit pipeline consistent with `pj/plot`, which feeds the
   pose's opts into the membrane stage.

   - `(draft->membrane (draft pose))`
   - `(draft->membrane (draft pose) {:tooltip true})`"
  ([draft] (draft->membrane draft {}))
  ([draft opts]
   (membrane/plan->membrane (draft->plan draft)
                            (merge (:opts draft) opts))))

(defn draft->plot
  "Compose draft -> plan -> plot for the given format.

   Render-stage options set on the original pose via `pj/options`
   (`:theme`, `:palette`, ...) ride on the draft's `:opts` and form
   the base; the passed opts override them per key.

   - `(draft->plot (draft pose) :svg {})`
   - `(draft->plot (draft pose) :bufimg {})`"
  [draft format opts]
  (render-impl/plan->plot (draft->plan draft)
                          format
                          (merge (:opts draft) opts)))

(defn plan->membrane
  "Convert a plan into a `PlotjeMembrane` -- a Membrane UI component
   carrying the rendered drawables, plan-derived width and height,
   and the plot title.

   The 1-arity uses no rendering options. The 2-arity takes an
   opts map with optional `:tooltip`, `:theme`, `:palette`, etc.

   The result implements `membrane.ui` `IOrigin`, `IBounds`, and
   `IChildren`, so width and height are accessible via
   `(membrane.ui/width m)` and `(membrane.ui/height m)`. The title,
   when set, rides as `:plotje/title`. Future per-membrane
   attributes use the same `:plotje/*` namespaced-keyword convention.
   The shape is captured by the `PlotjeMembraneSchema` in
   `scicloj.plotje.impl.membrane`.

   - `(plan->membrane (plan fr))`
   - `(plan->membrane (plan fr) {:tooltip true})`"
  ([plan-data] (plan->membrane plan-data {}))
  ([plan-data opts]
   (expect-type plan-data resolve/plan? "plan (from pj/plan)" "pj/plan->membrane")
   (membrane/plan->membrane plan-data opts)))

(defn membrane->plot
  "Convert a `PlotjeMembrane` into a figure for the given format.
   Dispatches on format keyword; `:svg` is always available.

   Reads width and height from the membrane via
   `(membrane.ui/width m)` / `(membrane.ui/height m)` (so any
   Membrane backend can introspect the canvas size), and the title
   from `(:plotje/title m)`.

   - `(membrane->plot (plan->membrane (plan pose)) :svg {})`"
  [membrane-tree format opts]
  (expect-type membrane-tree membrane/membrane?
               "PlotjeMembrane (from pj/plan->membrane or pj/membrane)"
               "pj/membrane->plot")
  (render-impl/membrane->plot membrane-tree format opts))

(defn plan->plot
  "Convert a plan into a figure for the given format.
   Dispatches on format keyword. Each renderer is a separate namespace
   that registers a defmethod; `:svg` is always available.

   - `(plan->plot (plan fr) :svg {})`
   - `(plan->plot (plan fr) :plotly {})`"
  [plan format opts]
  (expect-type plan resolve/plan? "plan (from pj/plan)" "pj/plan->plot")
  (render-impl/plan->plot plan format opts))

;; ---- Plan Validation ----

(defn valid-plan?
  "Check if a plan conforms to the Malli schema.

   - `(valid-plan? (plan pose))` -- true if valid."
  [plan]
  (ss/valid? plan))

(defn explain-plan
  "Explain why a plan does not conform to the Malli schema.
   Returns `nil` if valid, or a Malli explanation map if invalid.

   - `(explain-plan (plan pose))`"
  [plan]
  (ss/explain plan))

(defn valid-membrane?
  "Check if a membrane conforms to the Malli schema.

   - `(valid-membrane? (membrane pose))` -- true if valid."
  [membrane]
  (membrane-schema/valid? membrane))

(defn explain-membrane
  "Explain why a membrane does not conform to the Malli schema.
   Returns `nil` if valid, or a Malli explanation map if invalid.

   - `(explain-membrane (membrane pose))`"
  [membrane]
  (membrane-schema/explain membrane))

;; ---- API ----

(defn- value-column-seq?
  "True for a non-empty sequential whose first element is a bare
   scalar (not a map, not itself sequential) -- e.g. `[1 4 1 5 6]`,
   `[\"a\" \"b\"]`, `[:a :b :c]`. Such a collection represents a single
   column of values, coerced to a one-column dataset named :value.
   A sequence of row-maps or of pairs stays on the tc/dataset path."
  [d]
  (and (sequential? d)
       (seq d)
       (not (map? (first d)))
       (not (sequential? (first d)))))

(defn- coerce-dataset
  "Coerce data to a tablecloth dataset. Returns nil for nil; throws
   on non-collection scalars (numbers, strings, keywords) since
   tc/dataset would silently wrap them in a 1-row garbage frame.
   A bare list of scalars becomes a single :value column."
  [d]
  (cond
    (nil? d)         nil
    (tc/dataset? d)  d
    (value-column-seq? d) (tc/dataset {:value (vec d)})
    (or (map? d) (sequential? d)) (tc/dataset d)
    :else            (throw (ex-info
                             (str "Cannot use " (pr-str (type d))
                                  " as plot data. Pass a tablecloth"
                                  " dataset, a map of {:column [values]},"
                                  " or a sequence of row-maps. Got: "
                                  (pr-str d))
                             {:value d :type (type d)}))))

(defn- validate-pose-input!
  "Throw on nil or non-collection scalars when the caller needs real
   data. Used by ->pose and pj/pose 1-arity, where nil cannot
   be a template (no mapping carries it forward).

   `caller` is a public-facing function name (e.g. \"pj/pose\",
   \"pj/lay-point\") used as the prefix in user-visible messages.
   Internal helper names should never reach this argument -- the
   error is shown to the end user, who never called the helper
   directly."
  [caller x]
  (cond
    (nil? x)
    (throw (ex-info
            (str caller " requires data, but got nil. Pass a"
                 " tablecloth dataset, a map of {:column [values]},"
                 " or a sequence of row-maps; or use (pj/pose) for"
                 " an empty pose.")
            {:caller caller :value nil}))

    (and (vector? x) (keyword? (first x)) (not (every? keyword? x)))
    (throw (ex-info
            (str caller " expects a pose, but got what looks like a "
                 "rendered hiccup vector (head: " (pr-str (first x))
                 "). If you're inspecting the rendered output via pj/plot,"
                 " pass the pose itself to " caller ", not the result of"
                 " pj/plot.")
            {:caller caller :value-head (first x)}))

    (membrane/membrane-tree? x)
    (throw (ex-info
            (str caller " expects a pose, but got what looks like a "
                 "Membrane drawable tree (a PlotjeMembrane or a"
                 " hand-built vector of membrane.ui elements). If you"
                 " have a membrane and want to render it, call"
                 " pj/membrane->plot directly; if you want to re-plan,"
                 " pass the original pose to " caller ".")
            {:caller caller :value-head (cond (membrane/membrane? x) :PlotjeMembrane
                                              (vector? x) (first x)
                                              :else (type x))}))

    (or (and (or (sequential? x) (and (map? x) (not (tc/dataset? x))))
             (zero? (count x)))
        (and (tc/dataset? x) (zero? (tc/row-count x))))
    (throw (ex-info
            (str caller " got an empty collection (" (pr-str x)
                 "). Pass a dataset with at least one row, or use"
                 " (pj/pose) for an empty pose template.")
            {:caller caller :value x}))

    (and (not (tc/dataset? x))
         (not (map? x))
         (not (sequential? x)))
    (throw (ex-info
            (str caller " requires data, but got " (pr-str (type x))
                 ": " (pr-str x)
                 ". Pass a tablecloth dataset, a map of {:column"
                 " [values]}, or a sequence of row-maps.")
            {:caller caller :value x :type (type x)}))))

(defn- validate-template-data!
  "Like validate-pose-input!, but nil-tolerant -- nil here is the
   template idiom `(pj/pose nil {...})` where the mapping is set
   first and data is attached later via pj/with-data. Still rejects
   non-collection scalars."
  [caller x]
  (when (and (some? x)
             (not (tc/dataset? x))
             (not (map? x))
             (not (sequential? x)))
    (throw (ex-info
            (str caller " requires data, but got " (pr-str (type x))
                 ": " (pr-str x)
                 ". Pass a tablecloth dataset, a map of {:column"
                 " [values]}, a sequence of row-maps, or nil"
                 " (template; attach data later via pj/with-data).")
            {:caller caller :value x :type (type x)}))))

(defn- try-infer-mapping
  "Infer a position/color mapping from the first 1-3 columns of a
   dataset. Returns nil if the dataset has 0 or 4+ columns -- callers
   decide whether to throw or fall through."
  [d]
  (let [cols (vec (tc/column-names d))
        n (count cols)]
    (case n
      1 {:x (cols 0)}
      2 {:x (cols 0) :y (cols 1)}
      3 {:x (cols 0) :y (cols 1) :color (cols 2)}
      nil)))

(defn- auto-infer-mapping
  "Auto-infer a position/color mapping from the first 1-3 columns of
   a dataset. Throws if the dataset has 4+ columns -- the user must
   pass explicit x/y.

   Applied when 1-arity (pj/lay-* data) lands on a fresh leaf-pose
   with data but no :mapping."
  [layer-type-key d]
  (or (try-infer-mapping d)
      (let [cols (sort (tc/column-names d))
            x-only? (:x-only (layer-type/lookup layer-type-key))
            example (if x-only?
                      (str "(pj/lay-" (name layer-type-key) " data :x)")
                      (str "(pj/lay-" (name layer-type-key) " data :x :y)"))]
        (throw (ex-info (str "Cannot auto-infer columns from " (count cols) " columns. "
                             "Pass explicit " (if x-only? "x" "x and y")
                             ": " example ". Available columns: " cols)
                        {:layer-type layer-type-key
                         :column-count (count cols)
                         :x-only? (boolean x-only?)
                         :columns cols})))))

(defn pose?
  "Return true if x is a pose-shaped plain map (a map carrying at
   least one of `:layers` or `:poses`)."
  [x]
  (pose/pose? x))

(declare prepare-pose pose-kind validate-pose-shape
         check-position-mapping check-column-ref-types
         check-pose-shape!)

(defn ->pose
  "Lift the input to a pose. The first atomic step of the pipeline.
   Polymorphic on input:

   - a pose-shaped map flows through `pose-kind` (validated, `*config*`
     captured, Kindly auto-render metadata attached); idempotent on
     input that already carries the metadata, so repeated lifts are
     cheap;
   - raw data (a dataset, vector of row maps, or column map) becomes
     a leaf pose with `:data` set and no mapping, run through
     `prepare-pose` so the Kindly metadata is attached.

   Throws on nil or non-collection scalars. Use `(pj/pose)` for an
   explicit empty leaf instead of passing nil.

   The optional `caller` argument names the public-facing function
   shown in error messages, so users see \"pj/lay-point requires
   data...\" rather than an internal helper name. Defaults to
   \"pj/->pose\".

   - `(->pose data)` -- raw dataset becomes a leaf pose
   - `(->pose pose)` -- already a pose; idempotent lift"
  ([x] (->pose x "pj/->pose"))
  ([x caller]
   (if (pose? x)
     (pose-kind x)
     (do (validate-pose-input! caller x)
         (let [d (coerce-dataset x)]
           (prepare-pose (cond-> {:layers []} d (assoc :data d))))))))

(defn- fresh-data-only-leaf?
  "True for a lifted leaf pose carrying :data but no mapping, no
   layers, and no sub-poses -- the exact shape `->pose` produces from
   raw data. The semantic-inference seam fires only on this shape, so
   it is idempotent: anything `lay-*`/`pj/pose` already built (a
   mapping, layers, or sub-poses present) is left untouched."
  [fr]
  (and (pose? fr)
       (some? (:data fr))
       (nil? (:poses fr))
       (empty? (:mapping fr))
       (empty? (:layers fr))))

(defn infer-mapping
  "Infer and attach a default mapping to a pose that carries data but
   no mapping yet -- the fresh leaf `pj/->pose` produces from raw
   input. Position and color are taken from the first 1-3 columns
   (1 column to `:x`, 2 columns to `:x` and `:y`, 3 columns add
   `:color`).

   A pose that already has a mapping, has layers, is composite, or
   has 4+ columns is returned unchanged, so the step is idempotent
   and safe to include anywhere in a pipeline. This is the step that
   lets raw data render a sensible default: the user-facing entry
   points (`pj/pose` 1-arity and the shortcuts `pj/draft`, `pj/plan`,
   `pj/membrane`, `pj/plot`, `pj/save`) apply it right after
   `pj/->pose`, while `pj/->pose` itself stays a bare structural lift.

   - `(-> data pj/->pose pj/infer-mapping)` -- lift, then default map
   - `(pj/infer-mapping built-pose)` -- no-op on an already-mapped pose"
  [fr]
  (if-let [m (and (fresh-data-only-leaf? fr)
                  (try-infer-mapping (:data fr)))]
    (assoc fr :mapping m)
    fr))

(defn pose->draft
  "Single-step transition: convert a pose into a draft. Dispatches on
   pose shape -- a leaf pose becomes a `LeafDraft` (a record carrying
   `:layers` -- a vector of one map per applicable layer with merged
   scope -- and `:opts` -- the pose-level options that flow into the
   plan stage); a composite pose becomes a `CompositeDraft` carrying
   per-leaf drafts (each contextualized with shared-scale domains and
   chrome-driven opt adjustments), the resolved chrome geometry, and
   the layout (path -> rect).

   - `(pose->draft (pj/lay-point data :x :y))`"
  [pose]
  (expect-type pose pose? "pose" "pj/pose->draft")
  (check-pose-shape! pose)
  (if (pose/composite? pose)
    (compositor/composite-pose->draft pose)
    (resolve/->LeafDraft (pose/leaf->draft pose) (or (:opts pose) {}))))

(def ^:private pose-mapping-keys
  "Keys accepted in a pose mapping (column refs + per-pose
   data override + type-classification overrides)."
  (into defaults/column-keys #{:data :color-type :x-type :y-type}))

(def ^:private plot-text-keys
  "Keys describing one plot -- title, labels, panel dimensions."
  (set (keys defaults/plot-option-docs)))

(def ^:private config-keys
  "Rendering defaults -- settable for one plot via pj/options, or for
   every plot via pj/set-config!."
  (set (keys defaults/config-key-docs)))

(def ^:private plot-options-keys
  "Keys accepted by pj/options (top-level only; nested theme/config keys
   are validated separately by deep-merge)."
  (into plot-text-keys config-keys))

(def ^:private dedicated-function-keys
  "Keys no options map accepts because a dedicated function sets the
   setting instead. Faceting is caught earlier by check-facet-keys,
   which throws; these only warn, so they need the pointer here."
  {:scale-x "pj/scale" :scale-y "pj/scale"})

(defn- layer-types-accepting
  "Registered layer types whose :accepts list contains `k`, sorted.
   Read from the registry at call time so layer types registered by the
   user count too."
  [k]
  (->> (layer-type/registered)
       (keep (fn [[layer-type-key entry]]
               (when (some #{k} (:accepts entry)) layer-type-key)))
       sort
       vec))

(defn- option-home
  "The category `k` belongs to and the function that sets it, as a
   phrase, given the `caller` that rejected it. Returns nil when `k`
   matches nothing known -- a typo has no better home to point at."
  [caller k]
  (let [lay? (str/starts-with? caller "lay-")
        elsewhere (layer-types-accepting k)]
    (cond
      (dedicated-function-keys k)
      (str "Set by " (dedicated-function-keys k) ", not by an options map")

      (plot-text-keys k)
      "Plot options belong in pj/options"

      (config-keys k)
      (str "Configuration belongs in pj/options for one plot,"
           " or pj/set-config! for every plot")

      (and (not lay?)
           (or (contains? (set layer-type/universal-layer-options) k)
               (seq elsewhere)))
      "Layer options belong in a pj/lay-* options map"

      (and lay? (seq elsewhere))
      (str "Layer options of " elsewhere " belong in that layer's options map"))))

(defn- option-home-lines
  "One line per destination, naming the unknown keys that belong there.
   Keys with no known home are left out, so a plain typo still gets the
   accepted-key list and nothing more."
  [caller unknown]
  (->> unknown
       (group-by (partial option-home caller))
       (keep (fn [[home ks]] (when home (str home ": " (vec ks)))))
       sort
       (map (partial str "\n  "))
       str/join))

(defn- warn-and-strip-unknown-opts
  "Validate `opts` against `accepted`. `caller` is used in the message
   (e.g. \"pj/pose\", \"lay-point\"). If opts is nil or not a map,
   returns it unchanged.

   Behavior depends on the resolved config's :strict flag (read from
   set-config!, *config* binding, plotje.edn, and library defaults --
   in that precedence order):

   - :strict false (default in 0.1.0) -- print a warning and return
     opts with unknown keys stripped, so they don't propagate into
     downstream resolution.
   - :strict true -- throw an ex-info naming the unknown keys and
     listing the accepted set."
  [caller opts accepted]
  (if-not (and (map? opts) (seq opts))
    opts
    (let [unknown (remove accepted (keys opts))
          strict-val (:strict (defaults/config))]
      (when-not (or (nil? strict-val) (boolean? strict-val))
        (throw (ex-info (str ":strict config value must be true or false, got: "
                             (pr-str strict-val) ". Truthy non-boolean values"
                             " (keywords, strings, numbers) were silently"
                             " treated as enabled in earlier releases; this"
                             " is now an error.")
                        {:value strict-val})))
      (if (seq unknown)
        (let [msg (str caller " does not recognize option(s): " (vec unknown) "."
                       (option-home-lines caller unknown)
                       "\n  Accepted: " (vec (sort accepted)))]
          (if strict-val
            (throw (ex-info (str msg
                                 "\n  (Set :strict false in plotje.edn or"
                                 " via with-config to downgrade to a"
                                 " warning.)")
                            {:caller caller
                             :unknown (vec unknown)
                             :accepted (set accepted)}))
            (do (println (str "Warning: " msg))
                (select-keys opts (filter accepted (keys opts))))))
        opts))))

(def ^:private pose-keys
  "Allowed top-level keys on a pose at any depth. Outer-scope
   :layers distribute to every descendant leaf in a composite;
   outer-scope :mapping inherits downward and merges with each
   descendant's own mapping."
  #{:data :mapping :layers :opts :poses :layout :share-scales
    :grid-strip-labels})

(def ^:private pose-print-order
  "Key order used by pj/prepare-pose to make printed pose maps
   readable. Small declarative keys first; :data before :poses so
   each level's data stays visually bound to its own siblings rather
   than trailing past its children; :poses last since children can
   be heavy."
  [:opts :mapping :share-scales :grid-strip-labels :layout :layers :data :poses])

(defn- warn-unknown-pose-keys
  "Warn once about top-level keys in fr that are not in pose-keys.
   Returns fr unchanged."
  [fr]
  (let [unknown (remove pose-keys (keys fr))]
    (when (seq unknown)
      (println (str "Warning: pose has unexpected top-level key(s): "
                    (vec unknown)
                    ". Known pose keys: " (vec (sort pose-keys)))))
    fr))

(defn- warn-unknown-mapping-keys
  "Warn about keys in a :mapping map that are not in pose-mapping-keys.
   `context` (e.g. \"pj/pose\", \"pj/pose layer\") prefixes the message
   so the user can tell which mapping has the typo. Returns nil."
  [m context]
  (let [unknown (remove pose-mapping-keys (keys m))]
    (when (seq unknown)
      (println (str "Warning: " context " :mapping has unexpected"
                    " key(s): " (vec unknown)
                    ". Known mapping keys: "
                    (vec (sort pose-mapping-keys)))))))

(defn- reorder-pose-keys
  "Return a copy of fr with known keys in pose-print-order, followed
   by any unknown keys in their original order (so extensions survive,
   they print last)."
  [fr]
  (let [known (reduce (fn [acc k]
                        (if (contains? fr k) (assoc acc k (fr k)) acc))
                      {}
                      pose-print-order)
        extras (remove (set pose-print-order) (keys fr))]
    (reduce (fn [acc k] (assoc acc k (fr k))) known extras)))

(defn- elide-empty-maps
  "Strip :mapping and :opts keys whose value is an empty map. :layers
   [] is preserved because a leaf must carry :layers. Applied by
   normalize-pose so every builder path produces clean output."
  [m]
  (cond-> m
    (and (contains? m :mapping) (empty? (:mapping m))) (dissoc :mapping)
    (and (contains? m :opts)    (empty? (:opts m)))    (dissoc :opts)))

(defn- normalize-pose
  "Recursively coerce :data to a Tablecloth dataset at every depth,
   apply empty-map elision to the pose and its layers, and reorder
   keys for readable printing. Validation (warnings on unknown keys,
   throws on bad position mappings) lives upstream in pose-kind /
   prepare-pose so a single literal map flowing through both paths
   is only validated once."
  [fr]
  (let [coerced (cond-> fr
                  (:data fr)
                  (update :data coerce-dataset)
                  (:poses fr)
                  (update :poses (partial mapv normalize-pose))
                  (:layers fr)
                  (update :layers (partial mapv elide-empty-maps)))]
    (reorder-pose-keys (elide-empty-maps coerced))))

(declare plot)

(defn- render-pose-map
  "Kindly render function that restores captured *config* and routes
   a pose map (leaf or composite) through pj/plot."
  [captured-config]
  (fn [fr]
    (if captured-config
      (binding [defaults/*config* captured-config]
        (plot fr))
      (plot fr))))

(defn- prepare-pose
  "Internal: fully prepare a pose built by a constructor path
   (pj/pose typed arities, pj/lay-*, pj/options, pj/facet, pj/arrange,
   etc.). Coerces :data at every depth, applies cosmetic cleanup
   (key reordering, empty-map elision), captures the current *config*
   for render-time restoration, and attaches Kindly auto-render
   metadata. Idempotent on already-tagged input -- skips revalidation
   so a pose that flowed through pose-kind earlier does not warn
   twice. Literal user-typed maps go through pose-kind instead, which
   skips the cosmetic cleanup so the typed shape is preserved."
  [fr-map]
  (when-not (map? fr-map)
    (throw (ex-info (str "prepare-pose expects a pose map, got "
                         (pr-str (type fr-map)))
                    {:got fr-map})))
  (when-not (-> fr-map meta :kindly/kind)
    (validate-pose-shape fr-map "pj/pose"))
  (let [prepared (normalize-pose fr-map)
        captured defaults/*config*]
    (kind/fn prepared
      {:kindly/f (render-pose-map captured)})))

(defn- validate-pose-shape
  "Walk a pose-shaped map and validate it: warn on unknown top-level
   keys at every depth, warn on unknown :mapping keys, throw on
   non-column-ref position mappings. Returns fr unchanged. Used by
   pose-kind to surface typos at the literal-map entry point with
   the same safety net the typed pj/pose arities provide."
  [fr context]
  (warn-unknown-pose-keys fr)
  (when-let [m (:mapping fr)]
    (warn-unknown-mapping-keys m context)
    (check-column-ref-types context m)
    (check-position-mapping context m))
  (doseq [layer (:layers fr)]
    (when-let [lm (:mapping layer)]
      (warn-unknown-mapping-keys lm (str context " layer"))
      (check-column-ref-types (str context " layer") lm)
      (check-position-mapping (str context " layer") lm)))
  (doseq [sub (:poses fr)]
    (validate-pose-shape sub (str context " sub-pose")))
  fr)

(def ^:private nested-composite-rejection-msg
  (str "Nested composites (composite-of-composite) are not supported."
       " Build each cell as a separate leaf pose and pass them as a"
       " flat sequence to a single `pj/arrange` call."))

(defn- pose-kind
  "Lift a pose-shaped map into a notebook-renderable pose: validate
   the shape (recursive unknown-key warnings, position-mapping check),
   capture the current *config* for render-time restoration, and
   attach Kindly auto-render metadata.

   Idempotent: if the map already carries Kindly metadata (e.g. from
   a prior pose-kind or prepare-pose call), pass it through unchanged.
   This keeps validation and *config* capture single-shot per pose,
   so a literal map flowing through pj/plot -- which calls ->pose
   on the way in and again indirectly via pj/plan -- warns once, not
   twice.

   Unlike prepare-pose this does not normalize the map's shape:
   :data is not coerced at top level (the pipeline coerces per leaf
   at draft time), keys are not reordered, and empty :mapping/:opts
   are not elided. The user's typed map is preserved verbatim except
   for the metadata. Used by pj/pose 1-arity and ->pose on
   pose-shaped input."
  [fr]
  (if (-> fr meta :kindly/kind)
    fr
    (do (when (and (pose/composite? fr)
                   (some pose/composite? (:poses fr)))
          (throw (ex-info (str "pj/pose does not accept a nested composite"
                               " (a `:poses` element that is itself a"
                               " composite). "
                               nested-composite-rejection-msg)
                          {:got :nested-composite})))
        (validate-pose-shape fr "pj/pose")
        (let [captured defaults/*config*]
          (kind/fn fr {:kindly/f (render-pose-map captured)})))))

;; ---- pj/pose polymorphism (Phase 6) ----

(defn- layer-has-position? [layer]
  (boolean (or (:x (:mapping layer))
               (:y (:mapping layer)))))

(defn- leaf-has-position? [leaf]
  (or (boolean (or (:x (:mapping leaf))
                   (:y (:mapping leaf))))
      (boolean (some layer-has-position? (:layers leaf)))))

(defn- position-mapping [m]
  (select-keys m [:x :y]))

(defn- aesthetic-mapping [m]
  (apply dissoc m [:x :y]))

(defn- partition-layers-by-position
  "Returns [root-origin-layers panel-origin-layers]. A layer is
   panel-origin if its own :mapping carries :x or :y, else root-origin."
  [layers]
  (let [{panel :panel root :root}
        (group-by #(if (layer-has-position? %) :panel :root) (or layers []))]
    [(vec (or root []))
     (vec (or panel []))]))

(defn- promote-leaf
  "Promote a leaf to a composite, folding a new incoming-mapping into
   the result. The leaf's position part + panel-origin layers become
   sub-pose 1; the leaf's aesthetic part + root-origin layers + the
   leaf's :opts move to the composite root; the incoming mapping
   splits the same way (aesthetic -> root, position -> sub-pose 2).
   When the incoming mapping carries no position, no new sub-pose
   is added."
  [leaf incoming-mapping]
  (let [[root-layers panel-layers] (partition-layers-by-position (:layers leaf))
        leaf-pos        (position-mapping (:mapping leaf))
        leaf-aesth      (aesthetic-mapping (:mapping leaf))
        incoming-pos    (position-mapping incoming-mapping)
        incoming-aesth  (aesthetic-mapping incoming-mapping)
        root-aesth      (merge leaf-aesth incoming-aesth)
        leaf-opts       (:opts leaf)
        panel-1         (cond-> {:layers panel-layers}
                          (seq leaf-pos) (assoc :mapping leaf-pos))
        panel-2         (when (seq incoming-pos)
                          {:mapping incoming-pos :layers []})
        poses          (filterv some? [panel-1 panel-2])]
    (cond-> {:poses poses
             ;; Threaded `(pj/pose fr :x :y)` over a leaf-with-position
             ;; promotes into a composite. By default the layout is
             ;; matrix: distinct x-cols become grid columns, distinct
             ;; y-cols become grid rows, leaves land at their (x, y)
             ;; intersection. The user can override later with
             ;; (pj/options fr {:layout {:direction :horizontal}}).
             :layout {:direction :matrix}}
      (:data leaf)      (assoc :data (:data leaf))
      (seq root-aesth)  (assoc :mapping root-aesth)
      (seq root-layers) (assoc :layers root-layers)
      (seq leaf-opts)   (assoc :opts leaf-opts))))

(defn- extend-leaf
  "Extend a leaf that carries no position yet, merging incoming-mapping
   into its :mapping. Used when neither the leaf's :mapping nor its
   layers carry :x or :y."
  [leaf incoming-mapping]
  (let [merged (merge (:mapping leaf) incoming-mapping)]
    (cond-> leaf
      (seq merged)   (assoc :mapping merged)
      (empty? merged) (dissoc :mapping))))

(defn- extend-composite
  "Extend a composite. Aesthetic part of incoming-mapping merges into
   the root :mapping; position part appends a new sub-pose."
  [composite incoming-mapping]
  (let [incoming-pos   (position-mapping incoming-mapping)
        incoming-aesth (aesthetic-mapping incoming-mapping)
        with-aesth (if (seq incoming-aesth)
                     (update composite :mapping (fnil merge {}) incoming-aesth)
                     composite)]
    (if (seq incoming-pos)
      (update with-aesth :poses conj {:mapping incoming-pos :layers []})
      with-aesth)))

(defn- extend-or-promote
  "Dispatch `(pj/pose existing-pose incoming-mapping)`: composite
   inputs extend in place; leaves extend or promote depending on
   whether they already carry position."
  [fr incoming-mapping]
  (cond
    (pose/composite? fr)    (extend-composite fr incoming-mapping)
    (leaf-has-position? fr)  (promote-leaf fr incoming-mapping)
    :else                    (extend-leaf fr incoming-mapping)))

(defn- pose-from-data
  "Build a leaf-pose map from raw data and an already-normalized
   mapping (use {} for no mapping). Empty mapping is elided by
   normalize-pose downstream."
  [data mapping]
  (cond-> {:layers []}
    (some? data) (assoc :data data)
    (seq mapping) (assoc :mapping mapping)))

(declare pose with-data)

(defn- pairs->rows
  "Detect whether `pairs` forms a rectangular M x N grid -- every
   combination of unique first-elements with unique second-elements,
   in cross order. Returns a vec of row-vecs when rectangular, nil
   otherwise. Requires M >= 2 and N >= 2 so a single row or column
   stays flat (not a grid)."
  [pairs]
  (let [pairs  (vec pairs)
        xs     (vec (distinct (map first pairs)))
        ys     (vec (distinct (map second pairs)))
        m      (count xs)
        n      (count ys)]
    (when (and (<= 2 m) (<= 2 n)
               (= (count pairs) (* m n))
               (= pairs (vec (for [x xs y ys] [x y]))))
      (mapv (fn [x] (mapv (fn [y] [x y]) ys)) xs))))

(defn- grid-composite
  "Build a 2D rows-of-cols composite from `base` (a leaf or composite)
   and a rectangular grid of [x-col y-col] pairs. Each cell becomes a
   leaf carrying only its position mapping; the base's :data,
   :mapping, :layers, and :opts move to the new composite's root so
   they inherit into every cell via resolve-tree. :share-scales is
   stamped as #{:x :y} so columns share x-axis domains and rows share
   y-axis domains -- SPLOM behavior.

   Each cell also carries :opts:
   - :suppress-legend on every cell -- one shared legend at composite
     level.
   - :suppress-x-label and :suppress-y-label on every cell -- the
     strip labels carry the axis-variable name; per-cell axis labels
     would duplicate them.
   - :suppress-x-ticks on every cell except the bottom row -- only
     the bottom row's tick numbers stay, since tick scales are
     shared down the column via :share-scales.
   - :suppress-y-ticks on every cell except the leftmost column --
     same reasoning, tick scales shared across the row.

   The composite root carries :grid-strip-labels so the compositor
   can draw column strip labels above the top row and row strip
   labels to the left of the leftmost column (matching the legacy
   SPLOM chrome)."
  [base rows]
  (let [root-data (:data base)
        root-m    (:mapping base)
        root-l    (:layers base)
        root-o    (:opts base)
        n-rows    (count rows)
        col->name (fn [c] (if (keyword? c) (name c) (str c)))
        ;; Each column shares its y column; each row shares its x
        ;; column. Strip labels live at the composite root, not on
        ;; individual cells, so cell layout stays untouched.
        col-labels (when (seq rows)
                     (mapv (fn [[_ y]] (col->name y)) (first rows)))
        row-labels (mapv (fn [row] (col->name (first (first row)))) rows)
        cells      (fn [row-idx row]
                     (let [bottom? (= row-idx (dec n-rows))]
                       (vec
                        (map-indexed
                         (fn [col-idx [x y]]
                           (let [leftmost? (zero? col-idx)]
                             {:mapping {:x x :y y}
                              :opts (cond-> {:suppress-legend true
                                             :suppress-x-label true
                                             :suppress-y-label true}
                                      (not bottom?)   (assoc :suppress-x-ticks true)
                                      (not leftmost?) (assoc :suppress-y-ticks true))
                              :layers []}))
                         row))))
        row-poses (vec
                   (map-indexed
                    (fn [row-idx row]
                      {:layout {:direction :horizontal}
                       :poses (cells row-idx row)})
                    rows))
        composite (cond-> {:layout             {:direction :vertical}
                           :grid-strip-labels  {:col-labels col-labels
                                                :row-labels row-labels}
                           :opts              (merge {:share-scales #{:x :y}} root-o)
                           :poses             row-poses}
                    (some? root-data) (assoc :data root-data)
                    (seq root-m)      (assoc :mapping root-m)
                    (seq root-l)      (assoc :layers root-l))]
    (prepare-pose composite)))

(defn- multi-pair-pose
  "Iteratively apply pj/pose to each column or pair in cols-or-pairs.
   The ground case -- x a non-pose -- first lifts x into a leaf via
   (pj/pose x). Each element in cols-or-pairs may be a column
   reference (keyword or string) -> univariate panel, or a two-element
   sequential -> bivariate panel. Any mixture is accepted.

   When the elements form a rectangular M x N grid of pairs (e.g. the
   output of pj/cross cols cols), the result is a nested rows-of-cols
   composite with :share-scales #{:x :y} -- the canonical SPLOM shape.
   Non-rectangular pair lists and mixed/univariate lists fall through
   to the flat per-element reduce."
  [x cols-or-pairs]
  (let [;; Bare data-only leaf (no inferred :mapping) -- each pair or
        ;; column in `items` contributes its own sub-pose, so the base
        ;; must not carry a position mapping of its own.
        base     (if (pose? x) x (prepare-pose (pose-from-data x {})))
        items    (vec cols-or-pairs)
        first-el (first items)]
    (cond
      ;; Univariate -- columns
      (or (keyword? first-el) (string? first-el))
      (reduce (fn [fr col] (pose fr col)) base items)

      ;; Pairs -- check for rectangular grid before falling back
      (sequential? first-el)
      (if-let [rows (pairs->rows items)]
        (grid-composite base rows)
        (reduce (fn [fr [a b]] (pose fr a b)) base items))

      :else
      (throw (ex-info
              (str "pj/pose multi-pair element must be a column "
                   "reference or a two-element sequential, got: "
                   (pr-str first-el))
              {:item first-el :cols-or-pairs cols-or-pairs})))))

(defn pose
  "Construct or extend a pose.

   **On raw data (first argument is not itself a pose):**

   - `(pj/pose)` -- empty leaf.
   - `(pj/pose data)` -- leaf with data; on 1-3 column datasets the
     mapping is auto-inferred (`:x`, then `:y`, then `:color`) so the
     pose renders without an explicit mapping call.
   - `(pj/pose data {:color :species})` -- leaf with aesthetic mapping.
   - `(pj/pose data :x-col)` -- leaf with `{:x :x-col}`.
   - `(pj/pose data :x-col {:color :c})` -- univariate x with opts.
   - `(pj/pose data :x-col :y-col)` -- leaf with `:x` and `:y`.
   - `(pj/pose data :x-col :y-col {:color :c})` -- positional x/y with opts.
   - `(pj/pose data [[:a :b] [:c :d]])` -- multi-pair: N bivariate panels.
   - `(pj/pose data [:a :b :c])` -- multi-pair: N univariate panels.
   - `(pj/pose data (pj/cross cols cols) {:color :c})` -- multi-pair plus
     aesthetic mapping at the composite root.

   **Threaded over an existing pose (first argument is a pose):**

   - `(pj/pose fr)` -- pass-through; lifts a literal map for notebook
     auto-render if it is not already tagged.
   - `(pj/pose fr :x-col :y-col)` -- extend a leaf-without-position, or
     promote a leaf-with-position into a 2-panel composite, or append a
     panel to a composite.
   - `(pj/pose fr :x-col :y-col {:color :c})` -- same, with aesthetic
     routed to the composite root on promote.
   - `(pj/pose fr {:color :c})` -- aesthetic-only: extend mapping or
     (on leaf-with-position) promote.
   - `(pj/pose fr [[:a :b] [:c :d]])` -- multi-pair: append N panels.
   - `(pj/pose fr (pj/cross cols cols))` -- SPLOM N^2 panels in one call.
   - `(pj/pose fr (pj/cross cols cols) {:color :c})` -- SPLOM plus aesthetic
     mapping at the composite root.
   - `(pj/pose fr {:data X :color :c})` -- extend mapping AND replace the
     top-level data with X.

   **On a hand-built pose-shaped map (1-arity, input has `:layers` or
   `:poses`):** the map is validated and tagged with Kindly auto-render
   metadata, but its keys are not reordered and its `:data` is not
   coerced -- the typed shape is preserved verbatim. A flat composite
   (`:poses` of leaf maps) is supported; literal nested composites
   (any sub-pose itself has `:poses`) are rejected, matching
   `pj/arrange`'s rule that its elements must be leaves.

   **Writing a mapping out in full.** Any mapping value may be written
   as a map naming its source, and optionally which side of the scale
   to read it through:

   - `{:column :species}` -- the column, even where a value of that
     name could be drawn.
   - `{:value \"blue\"}` -- the color, even where the data carries a
     column called blue.
   - `{:scale false}` -- draw the value as it stands rather than pass
     it through the aesthetic's scale. On a column this is the only
     route to an identity scale: `{:color {:column :hex :scale false}}`
     draws the colors the column holds.
   - `{:scale true}` -- read it as data. `{:color {:value \"Model A\"
     :scale true}}` draws one palette color and earns a legend entry.

   The conventions decide when `:scale` is absent: a column passes
   through the scale, a written value is drawn on the appearance
   aesthetics and is a data value on `:x` and `:y`. See
   `pj/layer-option-docs` for what each aesthetic accepts, and
   `pj/scale` for choosing a scale's type."
  ([] (prepare-pose {:layers []}))
  ([x]
   (-> x (->pose "pj/pose") infer-mapping))
  ([x y]
   (when-not (pose? x) (validate-template-data! "pj/pose" x))
   (cond
     (and (sequential? y) (not (map? y)))
     (multi-pair-pose x y)

     :else
     (if (map? y)
       (let [opts      (or (warn-and-strip-unknown-opts
                            "pj/pose" y pose-mapping-keys)
                           {})
             data-over (:data opts)
             mapping   (dissoc opts :data)]
         (check-column-ref-types "pj/pose" mapping)
         (check-position-mapping "pj/pose" mapping)
         (if (pose? x)
           (cond-> (prepare-pose (extend-or-promote x mapping))
             data-over (with-data data-over))
           (prepare-pose (pose-from-data (or data-over x) mapping))))
       (let [mapping {:x y}]
         (check-column-ref-types "pj/pose" mapping)
         (check-position-mapping "pj/pose" mapping)
         (if (pose? x)
           (prepare-pose (extend-or-promote x mapping))
           (prepare-pose (pose-from-data x mapping)))))))
  ([x y z]
   (when-not (pose? x) (validate-template-data! "pj/pose" x))
   (cond
     ;; (pj/pose data multi-pair opts-map) -- attach mapping to the base,
     ;; then multi-pair on top, so opts (e.g. {:color :species}) lives at
     ;; the composite root and flows into every panel.
     (and (sequential? y) (not (map? y)) (map? z))
     (multi-pair-pose (pose x z) y)

     (map? z)
     ;; (pj/pose data x-col opts-map) -- univariate position plus opts
     (let [opts      (warn-and-strip-unknown-opts "pj/pose" z pose-mapping-keys)
           data-over (:data opts)
           mapping   (-> opts (dissoc :data) (merge {:x y}))]
       (check-column-ref-types "pj/pose" mapping)
       (check-position-mapping "pj/pose" mapping)
       (if (pose? x)
         (cond-> (prepare-pose (extend-or-promote x mapping))
           data-over (with-data data-over))
         (prepare-pose (pose-from-data (or data-over x) mapping))))

     :else
     (let [mapping {:x y :y z}]
       (check-column-ref-types "pj/pose" mapping)
       (check-position-mapping "pj/pose" mapping)
       (if (pose? x)
         (prepare-pose (extend-or-promote x mapping))
         (prepare-pose (pose-from-data x mapping))))))
  ([x y z opts]
   (when-not (pose? x) (validate-template-data! "pj/pose" x))
   (when-not (or (nil? opts) (map? opts))
     (throw (ex-info
             (str "pj/pose 4-arity expects an opts map as the last"
                  " argument, got " (pr-str (type opts)) ": "
                  (pr-str opts) ". Wrap aesthetic mappings in a map,"
                  " e.g. {:color :species}.")
             {:caller "pj/pose" :value opts})))
   (let [opts      (warn-and-strip-unknown-opts "pj/pose" opts pose-mapping-keys)
         data-over (:data opts)
         mapping   (-> opts (dissoc :data) (merge {:x y :y z}))]
     (check-column-ref-types "pj/pose" mapping)
     (check-position-mapping "pj/pose" mapping)
     (if (pose? x)
       (cond-> (prepare-pose (extend-or-promote x mapping))
         data-over (with-data data-over))
       (prepare-pose (pose-from-data (or data-over x) mapping))))))

(defn- column-refs-in-mapping [m]
  (keep #(let [v (get m %)]
           (when (keyword? v) v))
        defaults/column-keys))

(defn- column-refs-in-pose
  "Collect every keyword column reference used by a pose's :mapping,
   :layers, :poses (recursively), and :facet-col/:facet-row on opts."
  [fr]
  (distinct
   (concat
    (column-refs-in-mapping (or (:mapping fr) {}))
    (mapcat #(column-refs-in-mapping (or (:mapping %) {})) (:layers fr))
    (mapcat column-refs-in-pose (:poses fr))
    (keep #(let [v (get-in fr [:opts %])]
             (when (keyword? v) v))
          [:facet-col :facet-row]))))

(defn- validate-columns-present
  "Throw a helpful error if any of `refs` is absent from the
   dataset's column-name set. Matching is strict: a keyword reference
   does not satisfy a string column name with the same characters and
   vice versa."
  [refs ds]
  (let [cols (set (tc/column-names ds))
        missing (vec (remove cols refs))]
    (when (seq missing)
      (throw (ex-info (str "Cannot attach data: pose references column(s) "
                           missing
                           " not present in the dataset. Available columns: "
                           (vec (sort cols)) ".")
                      {:missing missing :available (vec (sort cols))})))))

(defn with-data
  "Supply or replace the top-level dataset on a pose.
   Useful for building a template once and applying it to different
   datasets:

       (def template (-> (pj/pose)
                         (pj/pose :x :y {:color :group})
                         pj/lay-point
                         (pj/lay-smooth {:stat :linear-model})))

       (-> template (pj/with-data my-data))
       (-> template (pj/with-data other-data))

   At attach time, every keyword column reference in the template's
   mapping, layers, sub-poses, and facet options must exist in the
   dataset -- otherwise an error is thrown naming the missing columns
   and listing what is available. Per-layer / per-sub-pose `:data`
   still overrides the top-level data."
  [pose data]
  (let [fr (->pose pose "pj/with-data")
        ds (coerce-dataset data)]
    (when ds
      (validate-columns-present (column-refs-in-pose fr) ds))
    (prepare-pose (assoc fr :data ds))))

(defn- check-facet-keys
  "Throw a helpful error if a mapping or layer-options map contains
   :facet-col / :facet-row / :facet-x / :facet-y. Faceting is
   plot-level and is set via pj/facet or pj/facet-grid, never via
   a sub-pose or layer options map -- the silent-strip behaviour on
   such keys confused users (user-report-2 Issue 5)."
  [context m]
  (let [fk (select-keys m [:facet-col :facet-row :facet-x :facet-y])]
    (when (seq fk)
      (throw (ex-info (str "Faceting is plot-level, not " context "-level. "
                           "Use (pj/facet pose col) or (pj/facet-grid pose col-col row-col) "
                           "instead of putting "
                           (str/join " / " (map name (keys fk)))
                           " in a " context "'s options map.")
                      fk)))))

(defn- layer-type-name
  "Human-readable name for a layer-type value in error messages."
  [layer-type]
  (cond
    (keyword? layer-type) (name layer-type)
    (and (map? layer-type) (keyword? (:layer-type layer-type)))
    (name (:layer-type layer-type))
    :else "*"))

(defn- check-new-sub-pose-columns
  "Safety check fired when `lay-*` would create a new sub-pose
   (LP2 promotion of a leaf-with-position, or LP3 miss on a
   composite). Verify each non-nil column in `position-mapping`
   exists in the data the new sub-pose will see: the layer's own
   `:data` if it has one, otherwise `fallback-data` (the existing
   leaf's or composite's data the new sub-pose would inherit). A
   focused error here saves the user from a deep plan-stage
   'column not found' on a column they likely typo'd."
  [layer-type position-mapping layer fallback-data]
  (when-let [d (or (:data layer) fallback-data)]
    (let [col-names (set (tc/column-names d))]
      (doseq [k [:x :y]
              :let [col (get position-mapping k)]
              :when (and col
                         (resolve/column-ref? col)
                         (not (contains? col-names col)))]
        (throw (ex-info
                (str "lay-" (layer-type-name layer-type)
                     " " k " " (pr-str col)
                     " names a column that doesn't exist in the data"
                     " the new sub-pose would use. Available columns: "
                     (vec (sort col-names))
                     ". The " k " differs from the receiving pose's"
                     " position, so this layer would land on a new"
                     " sub-pose -- but that sub-pose has no column"
                     " " (pr-str col) " to map to. If you meant to"
                     " overlay on the existing panel, align the"
                     " layer's columns with the pose's position"
                     " (rename the data's columns if needed). If you"
                     " meant a separate sub-pose, pass `:data` on this"
                     " lay-* call with the new columns.")
                {:caller (str "pj/lay-" (layer-type-name layer-type))
                 :key k :column col :available (sort col-names)}))))))

(defn- add-leaf-layer-to-composite
  "Walk the composite depth-first and append the layer to the last
   leaf whose effective :x/:y (after ancestor-merge) match
   `position-mapping`. On miss, append a fresh leaf at the root level
   (LP3) after firing the new-sub-pose column-existence safety check."
  [fr position-mapping layer]
  (let [match-path (pose/last-matching-leaf-path fr position-mapping)]
    (if (some? match-path)
      (update-in fr
                 (conj (pose/path->update-in-path match-path) :layers)
                 (fnil conj []) layer)
      (do
        (check-new-sub-pose-columns (:layer-type layer)
                                    position-mapping layer (:data fr))
        (update fr :poses (fnil conj [])
                {:mapping position-mapping :layers [layer]})))))

(defn- check-position-mapping
  "Throw a helpful error if :x or :y is a value that names no column and
   places no mark -- a vector, a boolean, a set.

   Where the `:x` or `:y` is written decides what a number means. In a
   layer's options map (`:allow-value`) it is a value to draw at, either
   as one mark or broadcast over the layer's data. In `pj/pose`, or in
   the `:x`/`:y` arguments of a `lay-*` call, it is a column reference
   and nothing else, so an integer column name reports the rename it
   needs rather than silently plotting one mark at that number.

   The explicit form passes this check in both contexts, which is what
   lifts that restriction: a bare number is ambiguous where columns
   carry integer names, and `{:value 2}` and `{:column 2}` are the two
   things it could have meant, each said out loud.

   Where it can be written is a separate question, and the answer is
   `pj/pose` and a layer's options map -- `{:x {:column 0}}`. It cannot
   go in a `lay-*` call's `:y` argument, because a map in that position
   is the options map; the arity decides before this check is reached."
  ([context opts] (check-position-mapping context opts nil))
  ([context opts allow-value]
   (doseq [k [:x :y]]
     (when-let [v (get opts k)]
       (when-not (or (keyword? v) (string? v)
                     (pose/explicit-mapping? v)
                     (and (= :allow-value allow-value)
                          (resolve/literal-position? v)))
         (throw (ex-info (str context " " k " must be a column reference "
                              "(keyword or string), but got " (pr-str v) "."
                              (if (resolve/literal-position? v)
                                (str " To place a mark at that " k ", give it "
                                     "in a layer's options map, e.g. "
                                     "`(pj/lay-text pose {" k " " (pr-str v)
                                     " :y 5.0 :text \"note\"})`. If " (pr-str v)
                                     " is a column name, rename the column "
                                     "first, e.g. "
                                     "`(tc/rename-columns ds [:x :y])`.")
                                (str " For one fixed " k ", add a column "
                                     "to :data holding it, e.g. "
                                     "`(tc/add-column data " k " (constantly "
                                     (pr-str v) "))` and pass "
                                     k " " (pr-str (keyword (name k))) ".")))
                         {:option k :value v})))))))

(defn- check-numeric-aesthetics
  "Throw a helpful error if :alpha or :size in a layer's options is
   a numeric constant outside its valid range. Column references
   (keyword/string) pass through -- per-row range is enforced by the
   encoder. :alpha must be in [0, 1] (an opacity); :size must be
   positive (a radius / thickness)."
  [context opts]
  (when-let [v (get opts :alpha)]
    (when (and (number? v) (not (<= 0 v 1)))
      (throw (ex-info (str context " :alpha must be in [0, 1] when given "
                           "as a constant, but got " (pr-str v) ".")
                      {:option :alpha :value v}))))
  (when-let [v (get opts :size)]
    (when (and (number? v) (not (pos? v)))
      (throw (ex-info (str context " :size must be positive when given "
                           "as a constant, but got " (pr-str v) ".")
                      {:option :size :value v}))))
  (when-let [v (get opts :in)]
    (when-not (contains? layer-type/spaces v)
      (throw (ex-info (str context " :in must be one of "
                           (vec (sort layer-type/spaces))
                           ", but got " (pr-str v) ".")
                      {:option :in :value v
                       :supported (vec (sort layer-type/spaces))}))))
  ;; An offset is a length in drawing units, never a column: it shifts the
  ;; whole layer by one amount, so a per-row value has nothing to mean.
  (doseq [k [:offset-x :offset-y]]
    (when-let [v (get opts k)]
      (when-not (number? v)
        (throw (ex-info (str context " " k " must be a number of drawing "
                             "units, but got " (pr-str v) ". It shifts the "
                             "whole layer, so it takes one value rather than "
                             "a column. For a data-space shift use "
                             (if (= k :offset-x) ":nudge-x" ":nudge-y") ".")
                        {:option k :value v}))))))

(defn- check-column-ref-types
  "Throw a helpful error if any aesthetic mapping carries a symbol --
   a common typo from omitting the colon on a keyword (`'x` instead
   of `:x`) that previously flowed into resolution and crashed deep
   in the pipeline. Nil is intentionally allowed: it cancels an
   inherited mapping at the call site (see core_test
   aesthetic-column-validation-test)."
  [context mapping]
  (doseq [[k v] mapping
          :when (and (contains? defaults/column-keys k) (symbol? v))]
    (throw (ex-info (str context " " k " is a symbol (" (pr-str v)
                         "). A column reference must be a keyword or "
                         "string -- did you mean " (pr-str (keyword (name v)))
                         "?")
                    {:option k :value v}))))

(defn- registered-marks []
  (->> (methods extract/extract-layer)
       keys
       (keep (fn [k] (cond (keyword? k) k
                           (and (vector? k) (keyword? (first k))) (first k))))
       (remove #{:default})
       set))

(defn- registered-stats []
  (->> (methods stat/compute-stat)
       keys
       (keep (fn [k] (cond (keyword? k) k
                           (and (vector? k) (keyword? (first k))) (first k))))
       (remove #{:default})
       set))

(defn- registered-positions []
  (->> (methods position/apply-position)
       keys
       (keep (fn [k] (cond (keyword? k) k
                           (and (vector? k) (keyword? (first k))) (first k))))
       (remove #{:default})
       set))

(defn- validate-mark-stat [fn-name opts]
  (when-let [m (:mark opts)]
    (when-not (contains? (registered-marks) m)
      (throw (ex-info (str fn-name " got :mark " (pr-str m)
                           ", which is not a registered mark. Registered marks: "
                           (vec (sort (registered-marks))))
                      {:mark m :registered (sort (registered-marks))}))))
  (when-let [s (:stat opts)]
    (when-not (contains? (registered-stats) s)
      (throw (ex-info (str fn-name " got :stat " (pr-str s)
                           ", which is not a registered stat. Registered stats: "
                           (vec (sort (registered-stats))))
                      {:stat s :registered (sort (registered-stats))}))))
  (when-let [p (:position opts)]
    (when-not (contains? (registered-positions) p)
      (throw (ex-info (str fn-name " got :position " (pr-str p)
                           ", which is not a registered position. Registered positions: "
                           (vec (sort (registered-positions))))
                      {:position p :registered (sort (registered-positions))})))))

(def ^:private layer-structural-keys
  "User-supplied layer options that are layer-structural (not
   column-to-aesthetic mappings). Promoted to top-level keys on the
   layer map; `:mapping` holds only true mappings."
  #{:stat :position :mark})

(defn- build-layer
  "Build a layer map from a layer-type-key and optional opts.
   Extracts :data if present. Extracts :stat, :position, :mark as
   first-class sibling keys. Warns and strips unrecognized option keys.
   Rejects unknown :mark or :stat keywords (since both are universal
   layer options, a typo would silently fall through the accept-list).

   Everything else lands in :mapping -- which therefore holds more than
   mappings. Of the layer options documented in
   `layer-type/layer-option-docs`, twelve are aesthetics and the rest
   are drawing options (:jitter, :in, :font-size), stat parameters
   (:bandwidth, :bins) and annotation values (:x-intercept). Which of
   them are aesthetics is answered by `defaults/aesthetic-registry`,
   not by the map they share."
  [layer-type-key opts]
  (when opts
    (check-facet-keys "layer" opts)
    (check-column-ref-types (str "lay-" (name layer-type-key)) opts)
    ;; :x and :y here may be a value as well as a column. Which of the
    ;; two it draws is decided in `pose/leaf->draft`, where the pose's
    ;; mapping and the layer's data are both known.
    (check-position-mapping (str "lay-" (name layer-type-key)) opts :allow-value)
    (check-numeric-aesthetics (str "lay-" (name layer-type-key)) opts)
    (validate-mark-stat (str "lay-" (name layer-type-key)) opts))
  (let [opts (if (and opts (keyword? layer-type-key))
               (let [reg (layer-type/lookup layer-type-key)
                     accepted (-> (set layer-type/universal-layer-options)
                                  (into (:accepts reg))
                                  (set/difference (set (:rejects reg))))]
                 (warn-and-strip-unknown-opts (str "lay-" (name layer-type-key))
                                              opts accepted))
               opts)
        opts-map (or opts {})
        d (:data opts-map)
        structural (select-keys opts-map layer-structural-keys)
        mapping (apply dissoc opts-map :data layer-structural-keys)]
    (cond-> (merge {:layer-type layer-type-key
                    :mapping mapping}
                   structural)
      d (assoc :data (coerce-dataset d)))))

(defn- validate-lay-layer-type-key
  "Reject an unregistered layer-type keyword at the pj/lay gate so
   the user gets the same eager error that lay-* gives via :mark.
   :infer is the auto-inference sentinel; maps are the extension
   form (a layer-type entry produced by layer-type/lookup or hand-
   built by an extension author)."
  [layer-type-key]
  (when (and (keyword? layer-type-key)
             (not= :infer layer-type-key)
             (nil? (layer-type/lookup layer-type-key)))
    (let [registered (sort (keys (layer-type/registered)))]
      (throw (ex-info (str "Unknown layer type: " layer-type-key
                           ". Use pj/lay-* with a registered layer type, or "
                           "(pj/layer-type-lookup ...) to inspect. Registered layer types: "
                           (vec registered))
                      {:caller "pj/lay"
                       :layer-type layer-type-key
                       :registered registered})))))

(defn lay
  "Add a root-scope layer. The layer attaches to `:layers` and flows to
   every descendant leaf at plan time (composite) or renders on the
   single panel (leaf)."
  ([pose-or-data layer-type-key]
   (lay pose-or-data layer-type-key nil))
  ([pose-or-data layer-type-key opts]
   (validate-lay-layer-type-key layer-type-key)
   (let [layer (build-layer layer-type-key opts)]
     (update (->pose pose-or-data "pj/lay") :layers (fnil conj []) layer))))

(defn- x-only?
  "True if layer-type-key is registered as x-only (rejects :y column)."
  [layer-type-key]
  (:x-only (layer-type/lookup layer-type-key)))

(defn- lay-on-pose
  "Append a layer to a pose following the DFS-last identity rule.

   Composite + position: the layer lands on the last leaf whose
   effective :x/:y match (via add-leaf-layer-to-composite), or a
   fresh sub-pose is appended at the root.

   Leaf whose own :mapping has no :x/:y, called with a position:
   extend the leaf's :mapping with the position and append a bare
   layer. A position-bearing lay-* on a bare pose sets the pose's
   position.

   Leaf whose own :mapping already has position, called with a
   matching position: append a layer carrying its own :mapping so
   downstream partitioning treats it as panel-origin.

   Leaf whose own :mapping already has position, called with a
   non-matching position: promote the leaf into a composite, then
   attach the layer to the new sub-pose -- mirroring LP3 for the
   composite case. The Identity rule applies symmetrically: non-
   matching columns create a new leaf, whether the receiver is a
   leaf or a composite.

   No position (leaf or composite + aesthetic-only): append the
   bare / aesthetic layer to :layers."
  [fr layer-type-key position-mapping opts]
  ;; The positional x/y of a lay-* call reach the pose's :mapping without
  ;; passing through build-layer, so they need the same column-reference
  ;; check pj/pose runs on the map it is handed.
  (check-position-mapping (str "lay-" (layer-type-name layer-type-key))
                          position-mapping)
  (let [bare-layer (elide-empty-maps (build-layer layer-type-key opts))
        pose-pos? (or (:x (:mapping fr)) (:y (:mapping fr)))]
    (cond
      (and (pose/composite? fr) (seq position-mapping))
      (add-leaf-layer-to-composite fr position-mapping bare-layer)

      (and (seq position-mapping) (not pose-pos?))
      (-> fr
          (update :mapping (fnil merge {}) position-mapping)
          (update :layers (fnil conj []) bare-layer))

      (seq position-mapping)
      (let [leaf-mapping (:mapping fr)
            disagreements (for [k [:x :y]
                                :let [pos-v (get position-mapping k)
                                      leaf-v (get leaf-mapping k)]
                                :when (and pos-v leaf-v
                                           (not= pos-v leaf-v))]
                            [k pos-v leaf-v])]
        (if (seq disagreements)
          ;; Stamp the leaf's position onto each bare layer before
          ;; promotion so promote-leaf treats them as panel-origin
          ;; (they stay with the original panel) rather than root-
          ;; origin (which would make them flow to the new panel and
          ;; misuse its column refs). This is the lay-* analog of
          ;; LP3: a non-matching position on a leaf creates a new
          ;; sub-pose, not a layer that crosses panels.
          (let [leaf-pos (select-keys (:mapping fr) [:x :y])
                stamped (update fr :layers
                                (fn [ls]
                                  (mapv (fn [l]
                                          (if (layer-has-position? l)
                                            l
                                            (assoc l :mapping leaf-pos)))
                                        (or ls []))))]
            (check-new-sub-pose-columns layer-type-key position-mapping
                                        bare-layer (:data fr))
            (-> stamped
                (promote-leaf position-mapping)
                (add-leaf-layer-to-composite position-mapping bare-layer)
                prepare-pose))
          (update fr :layers (fnil conj [])
                  (elide-empty-maps
                   (update bare-layer :mapping (fnil merge {}) position-mapping)))))

      :else
      (update fr :layers (fnil conj []) bare-layer))))

(defn- lay-layer-type
  "Shared implementation for all lay-* functions.

   Raw data coerces to a fresh leaf pose. Poses (leaf or composite)
   pass through. All dispatches then route through lay-on-pose, which
   follows the DFS-last identity rule in pose_rules.clj.

   1-arity: auto-infer columns for a fresh leaf-with-data (<= 3 cols),
            otherwise append a bare/aesthetic layer.
   2-arity: keyword/string -> position-bearing layer;
            vector of columns/pairs -> multi-pair broadcast;
            map -> aesthetic-only layer with opts.
   3-arity: two keywords -> bivariate; keyword+map -> univariate+opts;
            vector+map -> multi-pair broadcast with opts.
   4-arity: bivariate layer with opts."
  ([layer-type-key pose-or-data]
   (let [was-raw? (not (pose? pose-or-data))
         fr (->pose pose-or-data (str "pj/lay-" (name layer-type-key)))
         d (:data fr)]
     (if (and was-raw? d)
       ;; Raw-data 1-arity: auto-infer columns from the first 1-3 columns
       ;; so `(pj/lay-point data)` still produces a renderable plot.
       ;; Threaded `(-> data pj/pose pj/lay-point)` works too because
       ;; pj/pose 1-arity sets the mapping itself for 1-3 col data.
       ;; A pose with no mapping that reaches here (e.g. iris with 7
       ;; cols through pj/pose, or a hand-built map) stays bare so the
       ;; "root layer flows to every panel" M4 pattern keeps working.
       (let [mapping (auto-infer-mapping layer-type-key d)]
         (lay-on-pose (assoc fr :mapping mapping)
                      layer-type-key nil nil))
       (lay-on-pose fr layer-type-key nil nil))))
  ([layer-type-key pose-or-data x-or-opts]
   (let [was-raw? (not (pose? pose-or-data))
         fr (->pose pose-or-data (str "pj/lay-" (name layer-type-key)))]
     (cond
       (map? x-or-opts)
       (let [d (:data fr)
             ;; The options map may carry the position mapping itself.
             ;; When it carries all of it, inference has nothing left to
             ;; supply -- and running it anyway throws on 4+ columns,
             ;; refusing the very mapping the caller passed.
             mapped? (if (x-only? layer-type-key)
                       (contains? x-or-opts :x)
                       (and (contains? x-or-opts :x)
                            (contains? x-or-opts :y)))
             fr (if (and was-raw? d (nil? (:mapping fr)) (not mapped?))
                  (assoc fr :mapping (auto-infer-mapping layer-type-key d))
                  fr)]
         (lay-on-pose fr layer-type-key nil x-or-opts))

       ;; Sequential -> build a multi-panel composite via pj/pose, then
       ;; attach the layer at the root so it flows to every panel via
       ;; resolve-tree.
       (sequential? x-or-opts)
       (lay-on-pose (pose fr x-or-opts) layer-type-key nil nil)

       ;; Anything else in the x slot is a column reference. A value that
       ;; is not one reaches lay-on-pose and is named there; it used to be
       ;; dropped here in silence, giving a layer with no x at all.
       (some? x-or-opts)
       (lay-on-pose fr layer-type-key {:x x-or-opts} nil)

       :else
       (lay-on-pose fr layer-type-key nil nil))))
  ([layer-type-key pose-or-data x y-or-opts]
   (let [fr (->pose pose-or-data (str "pj/lay-" (name layer-type-key)))]
     (cond
       ;; Parallel vectors -> build a multi-panel composite via pj/pose
       ;; with paired x/y, then attach the bare layer at the root so it
       ;; flows to every panel.
       (and (sequential? x) (sequential? y-or-opts))
       (lay-on-pose (pose fr (mapv vector x y-or-opts))
                    layer-type-key nil nil)

       ;; Sequential + opts -> build a multi-panel composite via pj/pose,
       ;; then attach a layer with opts at the root.
       (and (sequential? x) (map? y-or-opts))
       (lay-on-pose (pose fr x) layer-type-key nil y-or-opts)

       (map? y-or-opts)
       (lay-on-pose fr layer-type-key {:x x} y-or-opts)

       ;; Anything else in the y slot is a column reference. A value that
       ;; is not one reaches lay-on-pose and is named there; it used to be
       ;; passed on as the options map, so a scalar y failed as
       ;; "find not supported on type: java.lang.Long".
       (some? y-or-opts)
       (do (when (x-only? layer-type-key)
             (throw (ex-info (str "lay-" (name layer-type-key) " uses only the x column; do not pass a y column")
                             {:layer-type layer-type-key :x x :y y-or-opts})))
           (lay-on-pose fr layer-type-key {:x x :y y-or-opts} nil))

       :else
       (lay-on-pose fr layer-type-key {:x x} nil))))
  ([layer-type-key pose-or-data x y opts]
   (when (x-only? layer-type-key)
     (throw (ex-info (str "lay-" (name layer-type-key) " uses only the x column; do not pass a y column")
                     {:layer-type layer-type-key :x x :y y})))
   (when-not (or (nil? opts) (map? opts))
     (throw (ex-info (str "lay-" (name layer-type-key)
                          " 4-arity expects an opts map as the last"
                          " argument, got " (pr-str (type opts)) ": "
                          (pr-str opts) ". Wrap aesthetic mappings in"
                          " a map, e.g. {:color :species}.")
                     {:caller (str "pj/lay-" (name layer-type-key))
                      :value opts})))
   (lay-on-pose (->pose pose-or-data (str "pj/lay-" (name layer-type-key))) layer-type-key {:x x :y y} opts)))

(defn lay-point
  "Add a `:point` (scatter) layer to a pose.
   Without columns -> bare layer at the pose's root (flows to every leaf).
   With columns -> position-bearing layer (attaches to the matching leaf
   via DFS-last identity, or appends a new sub-pose on miss).

   - `(lay-point fr)` -- bare layer at root.
   - `(lay-point fr {:color :species})` -- bare layer with layer options.
   - `(lay-point data :x :y)` -- coerce data to a leaf, then attach.
   - `(lay-point data :x :y {:color :c})` -- same with layer options."
  ([pose-or-data] (lay-layer-type :point pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :point pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :point pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :point pose-or-data x y opts)))

(defn- last-opts
  "Return the trailing opts map from a lay-* arg list (or nil)."
  [args]
  (let [last-arg (last args)]
    (when (map? last-arg) last-arg)))

(defn- positional-hint
  "If the user passed a non-map last arg (e.g. a bare number), suggest
   wrapping it in an opts map. args here are the trailing args after
   the pose -- so the bad shape is `(lay-rule-h fr 3)` not `(lay-rule-h fr)`."
  [args]
  (when (and (seq args) (not (map? (last args))))
    (str " Got " (pr-str (last args)) " as the last argument; did you forget"
         " to wrap it in an opts map?")))

(def ^:private rule-position-key
  "Per-layer-type required position key for pj/lay-rule-*."
  {:rule-h :y-intercept :rule-v :x-intercept})

(def ^:private band-position-keys
  "Per-layer-type required [lo-key hi-key] for pj/lay-band-*."
  {:band-h [:y-min :y-max] :band-v [:x-min :x-max]})

(defn- temporal-intercept?
  "True if v is a supported temporal value for a rule intercept on a
   temporal axis (LocalDate, LocalDateTime, Instant, java.util.Date)."
  [v]
  (or (instance? java.time.LocalDate v)
      (instance? java.time.LocalDateTime v)
      (instance? java.time.Instant v)
      (instance? java.util.Date v)))

(defn- coerce-intercept
  "Convert a temporal intercept to epoch-ms (double) so it lines up with
   columns that have already been converted by `temporalize-column`. Numeric
   values pass through unchanged. Mirrors `resolve/temporal->epoch-ms`."
  [v]
  (cond
    (number? v) v
    (instance? java.time.LocalDate v)
    (-> ^java.time.LocalDate v
        (.atStartOfDay (java.time.ZoneOffset/UTC))
        .toInstant .toEpochMilli double)
    (instance? java.time.LocalDateTime v)
    (-> ^java.time.LocalDateTime v
        (.toInstant java.time.ZoneOffset/UTC)
        .toEpochMilli double)
    (instance? java.time.Instant v)
    (double (.toEpochMilli ^java.time.Instant v))
    (instance? java.util.Date v)
    (double (.getTime ^java.util.Date v))
    :else v))

(defn- coerce-rule-opts
  "Convert temporal intercept (if present) to epoch-ms so the value lines
   up with temporal columns after their conversion."
  [layer-type-key opts]
  (if-not (map? opts)
    opts
    (let [k (rule-position-key layer-type-key)
          v (get opts k)]
      (if (temporal-intercept? v)
        (assoc opts k (coerce-intercept v))
        opts))))

(defn- coerce-band-opts
  "Convert temporal :y-min/:y-max (or :x-min/:x-max) to epoch-ms so
   the values line up with temporal columns after their conversion.
   Mirrors coerce-rule-opts, but for the two-bound band case."
  [layer-type-key opts]
  (if-not (map? opts)
    opts
    (let [[lo-k hi-k] (band-position-keys layer-type-key)]
      (cond-> opts
        (temporal-intercept? (get opts lo-k)) (update lo-k coerce-intercept)
        (temporal-intercept? (get opts hi-k)) (update hi-k coerce-intercept)))))

(defn- assert-rule-opts! [layer-type-key args]
  (let [opts (last-opts args)
        k (rule-position-key layer-type-key)
        v (get opts k)]
    (when-not (or (and (number? v) (Double/isFinite (double v)))
                  (temporal-intercept? v))
      (throw (ex-info (str "lay-" (name layer-type-key) " requires a finite numeric "
                           "or temporal " k " in its opts map. "
                           "Example: (pj/lay-" (name layer-type-key) " pose {" k " 3.0})"
                           " or (pj/lay-" (name layer-type-key) " pose {" k
                           " #inst \"2024-06-15\"})."
                           (positional-hint args))
                      {:layer-type layer-type-key :opts opts})))))

(defn- assert-band-opts! [layer-type-key args]
  (let [opts (last-opts args)
        [lo-k hi-k] (band-position-keys layer-type-key)
        lo (get opts lo-k) hi (get opts hi-k)
        valid-bound? (fn [v]
                       (or (and (number? v) (Double/isFinite (double v)))
                           (temporal-intercept? v)))]
    (when-not (and (valid-bound? lo) (valid-bound? hi))
      (throw (ex-info (str "lay-" (name layer-type-key) " requires finite numeric or temporal "
                           lo-k " and " hi-k " in its opts map. "
                           "Example: (pj/lay-" (name layer-type-key) " pose {" lo-k " 2.0 " hi-k " 4.0}) "
                           "or (pj/lay-" (name layer-type-key) " pose {" lo-k
                           " #inst \"2024-01-01\" " hi-k " #inst \"2024-06-30\"})."
                           (positional-hint args))
                      {:layer-type layer-type-key :opts opts})))
    ;; Compare bounds in their coerced (numeric) form so temporal
    ;; values are checked against each other meaningfully.
    (let [lo-num (double (coerce-intercept lo))
          hi-num (double (coerce-intercept hi))]
      (when-not (<= lo-num hi-num)
        (throw (ex-info (str "lay-" (name layer-type-key) " requires " lo-k " <= " hi-k ", got " lo-k " " lo " " hi-k " " hi ". "
                             "Swap the arguments or check the source of the values.")
                        {:layer-type layer-type-key :opts opts}))))))

(defn- assert-rule-1-arity! [layer-type-key]
  (let [k (rule-position-key layer-type-key)]
    (throw (ex-info (str "lay-" (name layer-type-key) " requires an opts map with " k ". "
                         "Example: (pj/lay-" (name layer-type-key) " pose {" k " 3.0}).")
                    {:layer-type layer-type-key}))))

(defn- assert-band-1-arity! [layer-type-key]
  (let [[lo-k hi-k] (band-position-keys layer-type-key)]
    (throw (ex-info (str "lay-" (name layer-type-key) " requires an opts map with " lo-k " and " hi-k ". "
                         "Example: (pj/lay-" (name layer-type-key) " pose {" lo-k " 2.0 " hi-k " 4.0}).")
                    {:layer-type layer-type-key}))))

(defn lay-rule-h
  "Add `:rule-h` layer -- horizontal reference line at y = y-intercept.
   Position comes from opts (not data columns); `:y-intercept` is required.
   Accepts `:y-intercept` (numeric or temporal -- LocalDate, LocalDateTime,
   Instant, java.util.Date), `:color` (literal string), and `:stroke-dash`
   (`:dashed`/`:dotted`/`:solid` or a raw `[dash gap]` vector).
   Temporal values are converted internally to match the y-axis scale
   so date-axis annotations work without manual conversion.
   The 4-arity finds or creates a sub-pose with these x/y columns
   and attaches the rule there (only panels matching that leaf show it).

   - `(lay-rule-h pose {:y-intercept 3})` -- root-level, flows to every panel.
   - `(lay-rule-h pose :x :y {:y-intercept 3})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-rule-h pose {:y-intercept 3 :color \"red\"})` -- with override color.
   - `(lay-rule-h pose {:y-intercept (java.time.LocalDate/parse \"2024-01-01\")})`
     -- temporal intercept on a date axis."
  ([_pose-or-data] (assert-rule-1-arity! :rule-h))
  ([pose-or-data x-or-opts] (assert-rule-opts! :rule-h [x-or-opts]) (lay-layer-type :rule-h pose-or-data (coerce-rule-opts :rule-h x-or-opts)))
  ([pose-or-data x y-or-opts] (assert-rule-opts! :rule-h [y-or-opts]) (lay-layer-type :rule-h pose-or-data x (coerce-rule-opts :rule-h y-or-opts)))
  ([pose-or-data x y opts] (assert-rule-opts! :rule-h [opts]) (lay-layer-type :rule-h pose-or-data x y (coerce-rule-opts :rule-h opts))))

(defn lay-rule-v
  "Add `:rule-v` layer -- vertical reference line at x = x-intercept.
   Position comes from opts (not data columns); `:x-intercept` is required.
   Accepts `:x-intercept` (numeric or temporal -- LocalDate, LocalDateTime,
   Instant, java.util.Date), `:color` (literal string), and `:stroke-dash`
   (`:dashed`/`:dotted`/`:solid` or a raw `[dash gap]` vector).
   Temporal values are converted internally to match the x-axis scale
   so date-axis annotations work without manual conversion.
   The 4-arity finds or creates a sub-pose with these x/y columns
   and attaches the rule there (only panels matching that leaf show it).

   - `(lay-rule-v pose {:x-intercept 5})` -- root-level, flows to every panel.
   - `(lay-rule-v pose :x :y {:x-intercept 5})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-rule-v pose {:x-intercept 5 :color \"red\"})` -- with override color.
   - `(lay-rule-v pose {:x-intercept #inst \"2008-09-15\"})` -- temporal
     intercept on a date axis."
  ([_pose-or-data] (assert-rule-1-arity! :rule-v))
  ([pose-or-data x-or-opts] (assert-rule-opts! :rule-v [x-or-opts]) (lay-layer-type :rule-v pose-or-data (coerce-rule-opts :rule-v x-or-opts)))
  ([pose-or-data x y-or-opts] (assert-rule-opts! :rule-v [y-or-opts]) (lay-layer-type :rule-v pose-or-data x (coerce-rule-opts :rule-v y-or-opts)))
  ([pose-or-data x y opts] (assert-rule-opts! :rule-v [opts]) (lay-layer-type :rule-v pose-or-data x y (coerce-rule-opts :rule-v opts))))

(defn lay-band-h
  "Add `:band-h` layer -- horizontal shaded band between y = y-min and y = y-max.
   Position comes from opts (not data columns); `:y-min` and `:y-max` are
   required and `:y-min` must be <= `:y-max`.
   Accepts `:y-min` (required), `:y-max` (required), `:color` (literal
   string), `:alpha`. Bounds may be numeric or temporal (LocalDate,
   LocalDateTime, Instant, java.util.Date); temporal values are
   converted internally to match the y-axis scale.
   The 4-arity finds or creates a sub-pose with these x/y columns
   and attaches the band there (only panels matching that leaf show it).

   - `(lay-band-h pose {:y-min 2 :y-max 4})` -- root-level, flows to every panel.
   - `(lay-band-h pose :x :y {:y-min 2 :y-max 4})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-band-h pose {:y-min 2 :y-max 4 :color \"blue\" :alpha 0.3})`
     -- with color and opacity overrides."
  ([_pose-or-data] (assert-band-1-arity! :band-h))
  ([pose-or-data x-or-opts] (assert-band-opts! :band-h [x-or-opts]) (lay-layer-type :band-h pose-or-data (coerce-band-opts :band-h x-or-opts)))
  ([pose-or-data x y-or-opts] (assert-band-opts! :band-h [y-or-opts]) (lay-layer-type :band-h pose-or-data x (coerce-band-opts :band-h y-or-opts)))
  ([pose-or-data x y opts] (assert-band-opts! :band-h [opts]) (lay-layer-type :band-h pose-or-data x y (coerce-band-opts :band-h opts))))

(defn lay-band-v
  "Add `:band-v` layer -- vertical shaded band between x = x-min and x = x-max.
   Position comes from opts (not data columns); `:x-min` and `:x-max` are
   required and `:x-min` must be <= `:x-max`.
   Accepts `:x-min` (required), `:x-max` (required), `:color` (literal
   string), `:alpha`. Bounds may be numeric or temporal (LocalDate,
   LocalDateTime, Instant, java.util.Date); temporal values are
   converted internally to match the x-axis scale.
   The 4-arity finds or creates a sub-pose with these x/y columns
   and attaches the band there (only panels matching that leaf show it).

   - `(lay-band-v pose {:x-min 4 :x-max 6})` -- root-level, flows to every panel.
   - `(lay-band-v pose :x :y {:x-min 4 :x-max 6})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-band-v pose {:x-min 4 :x-max 6 :color \"blue\" :alpha 0.3})`
     -- with color and opacity overrides."
  ([_pose-or-data] (assert-band-1-arity! :band-v))
  ([pose-or-data x-or-opts] (assert-band-opts! :band-v [x-or-opts]) (lay-layer-type :band-v pose-or-data (coerce-band-opts :band-v x-or-opts)))
  ([pose-or-data x y-or-opts] (assert-band-opts! :band-v [y-or-opts]) (lay-layer-type :band-v pose-or-data x (coerce-band-opts :band-v y-or-opts)))
  ([pose-or-data x y opts] (assert-band-opts! :band-v [opts]) (lay-layer-type :band-v pose-or-data x y (coerce-band-opts :band-v opts))))

(defn lay-line
  "Add `:line` layer type -- connected line through data points.
   Requires x (numerical) and y (numerical).
   Accepts `:color`, `:alpha`, `:size` (stroke width), `:stroke-dash`
   (`:dashed`/`:dotted`/`:solid` or a raw `[dash gap]` vector), `:nudge-x`,
   `:nudge-y`."
  ([pose-or-data] (lay-layer-type :line pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :line pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :line pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :line pose-or-data x y opts)))

(defn lay-step
  "Add `:step` layer type -- staircase line (horizontal then vertical).
   Requires x and y (both numerical). Accepts `:stroke-dash`."
  ([pose-or-data] (lay-layer-type :step pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :step pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :step pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :step pose-or-data x y opts)))

(defn lay-area
  "Add `:area` layer type -- filled region between y and the baseline.
   Requires x and y (both numerical). Accepts `:color` (fill), `:alpha`,
   and an opt-in outline on the top curve: `:stroke` (outline color),
   `:stroke-width`, `:stroke-dash`."
  ([pose-or-data] (lay-layer-type :area pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :area pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :area pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :area pose-or-data x y opts)))

(defn lay-histogram
  "Add `:histogram` layer type -- bin numerical values into bars.
   X-only: pass one column. Accepts `:bins` (count), `:binwidth`, `:color`,
   `:normalize` (`:density` for density-normalized heights)."
  ([pose-or-data] (lay-layer-type :histogram pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :histogram pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :histogram pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :histogram pose-or-data x y opts)))

(defn lay-bar
  "Add a `:bar` layer type.

   - One column (x only): counts occurrences of each category. Requires a
     categorical x.
   - Two columns (x and y): uses the y value directly as the bar height.

   The stat is inferred from whether a y column is present, and overridable:
   pass `{:stat :count}` to count even with a y column, or `{:stat :identity}`
   to require an explicit height. `:color` (with `:position` `:dodge`/`:stack`)
   gives grouped or stacked bars.

   The categorical axis can be x (vertical bars) or y (horizontal bars):
   `(pj/lay-bar :value :category)` with a categorical y draws horizontal value
   bars, no `pj/coord` needed. (Stacked/filled horizontal bars are not yet
   supported directly -- put the category on x and add `(pj/coord :flip)`.)
   To treat a numeric column as categorical, pass `{:x-type :categorical}`
   (or `{:y-type :categorical}`).

   When both axes are numeric or temporal (`(pj/lay-bar :x :y)` with no
   categorical axis), each bar sits at its x position with a width taken from
   `0.9` of the smallest gap between adjacent x values -- a time-series or
   numeric-position bar chart. Pass `{:bar-width n}` (data units) to set the
   width. Grouped numeric bars currently overlap rather than dodge."
  ([pose-or-data] (lay-layer-type :bar pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :bar pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :bar pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :bar pose-or-data x y opts)))

(defn lay-smooth
  "Add `:smooth` layer type -- a smoothed trend line.
   Defaults to LOESS (local regression). Pass {`:stat` `:linear-model`} for
   ordinary least squares instead. Requires x and y (both numerical).
   Accepts {`:confidence-band` true} for a confidence ribbon, and
   `:stroke-dash`."
  ([pose-or-data] (lay-layer-type :smooth pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :smooth pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :smooth pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :smooth pose-or-data x y opts)))

(defn lay-density
  "Add `:density` layer type -- kernel density estimate curve.
   X-only: pass one numerical column. Accepts `:color` (fill),
   `:bandwidth`, and an opt-in outline on the curve: `:stroke` (outline
   color), `:stroke-width`, `:stroke-dash` (`:dashed`/`:dotted`/`:solid`
   or a raw `[dash gap]` vector)."
  ([pose-or-data] (lay-layer-type :density pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :density pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :density pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :density pose-or-data x y opts)))

(defn lay-tile
  "Add `:tile` layer type -- colored grid cells (heatmap).
   With `:fill` option: pre-computed tile colors from a column.
   Without `:fill`: auto-binned 2D histogram (stat `:bin2d`)."
  ([pose-or-data] (lay-layer-type :tile pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :tile pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :tile pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :tile pose-or-data x y opts)))

(defn lay-density-2d
  "Add `:density-2d` layer type -- 2D kernel density heatmap.
   Requires x and y (both numerical). Produces a smoothed density
   surface as colored tiles with a continuous gradient legend."
  ([pose-or-data] (lay-layer-type :density-2d pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :density-2d pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :density-2d pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :density-2d pose-or-data x y opts)))

(defn lay-contour
  "Add `:contour` layer type -- iso-density contour lines from 2D KDE.
   Requires x and y (both numerical). Accepts {`:levels` 10} for
   the number of contour levels."
  ([pose-or-data] (lay-layer-type :contour pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :contour pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :contour pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :contour pose-or-data x y opts)))

(defn lay-boxplot
  "Add `:boxplot` layer type -- box-and-whisker plot.
   Requires categorical x and numerical y. Shows median, quartiles,
   whiskers, and outliers. Accepts `:color` for grouped boxplots."
  ([pose-or-data] (lay-layer-type :boxplot pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :boxplot pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :boxplot pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :boxplot pose-or-data x y opts)))

(defn lay-violin
  "Add `:violin` layer type -- mirrored density estimate by category.
   Requires categorical x and numerical y. Accepts `:color`, `:bandwidth`."
  ([pose-or-data] (lay-layer-type :violin pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :violin pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :violin pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :violin pose-or-data x y opts)))

(defn lay-ridgeline
  "Add `:ridgeline` layer type -- stacked density curves by category.
   Requires categorical x and numerical y. Categories stack vertically
   with density curves rendered horizontally."
  ([pose-or-data] (lay-layer-type :ridgeline pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :ridgeline pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :ridgeline pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :ridgeline pose-or-data x y opts)))

(defn lay-summary
  "Add `:summary` layer type -- mean +/- standard error per category.
   Requires categorical x and numerical y. Shows a point at the mean
   with error bars for +/- 1 SE. Accepts `:color` for grouped summaries."
  ([pose-or-data] (lay-layer-type :summary pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :summary pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :summary pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :summary pose-or-data x y opts)))

(defn lay-errorbar
  "Add `:errorbar` layer type -- vertical error bars from pre-computed bounds.
   Requires x, y, and {`:y-min` `:col` `:y-max` `:col`} for lower/upper bounds."
  ([pose-or-data] (lay-layer-type :errorbar pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :errorbar pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :errorbar pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :errorbar pose-or-data x y opts)))

(defn lay-lollipop
  "Add `:lollipop` layer type -- dot on a stem from the baseline.
   Requires categorical x and numerical y. Like a value bar but with
   a circle+line instead of a filled rectangle."
  ([pose-or-data] (lay-layer-type :lollipop pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :lollipop pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :lollipop pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :lollipop pose-or-data x y opts)))

(defn lay-text
  "Add `:text` layer type -- text labels at data coordinates.
   Requires x, y, and {`:text` `:column`} for label content.
   `:align-x` (`:left`/`:center`/`:right`, default `:left`) and `:align-y`
   (`:top`/`:center`/`:bottom`, default `:center`) set which part of the
   text lands on the data point -- e.g. `:align-x :right` tucks the label
   inside a bar's end, extending leftward.
   `:box` puts the text on a background box: `true` for the default box,
   or a map of box properties (`{:corner-radius 8}`). `pj/lay-label` is
   this layer with the box on."
  ([pose-or-data] (lay-layer-type :text pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :text pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :text pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :text pose-or-data x y opts)))

(defn lay-label
  "Add `:label` layer type -- text labels on a background box at data
   coordinates, for readability over dense data.

   `:label` is the `:text` layer type with `{:box true}` preset, so it
   draws through the same mark and takes the same options, including
   `:align-x`/`:align-y` (defaults `:left`/`:center`); the box follows the
   anchored text. Pass `:box` to shape it -- `{:box {:corner-radius 0}}`
   for square corners, `{:box false}` for bare text."
  ([pose-or-data] (lay-layer-type :label pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :label pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :label pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :label pose-or-data x y opts)))

(defn lay-rug
  "Add `:rug` layer type -- short tick marks along the axis showing individual values.
   X-only: pass one column. Often layered with density or scatter."
  ([pose-or-data] (lay-layer-type :rug pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :rug pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :rug pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :rug pose-or-data x y opts)))

(defn lay-interval-h
  "Add `:interval-h` layer type -- horizontal bar from x to x-end at categorical y.
   Each row becomes one rectangle; the y column is treated categorically
   so each distinct value occupies its own lane.
   Required: x (numeric or temporal start), y (categorical lane),
             :x-end column ref in opts (numeric or temporal end).
   Accepts `:color`, `:alpha`, `:interval-thickness` (band fill fraction,
   0.0-1.0, default 0.7).
   (lay-interval-h data :start :task {:x-end :end :color :status})"
  ([pose-or-data] (lay-layer-type :interval-h pose-or-data))
  ([pose-or-data x-or-opts] (lay-layer-type :interval-h pose-or-data x-or-opts))
  ([pose-or-data x y-or-opts] (lay-layer-type :interval-h pose-or-data x y-or-opts))
  ([pose-or-data x y opts] (lay-layer-type :interval-h pose-or-data x y opts)))

(defn- wrap-options-list
  "Wrap a sorted seq of option keywords across lines so the rendered
   docstring stays readable. First line is prefixed with 'Accepted
   options: '; continuation lines align under the first option."
  [opts]
  (let [prefix "   Accepted options: "
        cont   "                     "
        max-w  78]
    (loop [[opt & more] opts
           lines []
           current prefix]
      (if (nil? opt)
        (str/join "\n" (conj lines (str current ".")))
        (let [token (str opt)
              candidate (if (= current prefix) token (str " " token))
              fits? (<= (+ (count current) (count candidate) 1) max-w)]
          (if fits?
            (recur more lines (str current candidate))
            (recur more (conj lines current) (str cont token))))))))

(defn- append-accepted-options-block!
  "Append the canonical accepted-options list to lay-K's docstring at
   load time. The source docstring carries prose (description,
   required mappings, examples); the registry is the single source of
   truth for the accepted-keys list, so a registry change is
   automatically reflected. Idempotent on reload via the
   ::lay-doc-base meta key, which preserves the pre-append source
   docstring across multiple invocations."
  [layer-type-key]
  (when-let [v (resolve (symbol "scicloj.plotje.api"
                                (str "lay-" (name layer-type-key))))]
    (let [reg (layer-type/lookup layer-type-key)
          opts (-> (set layer-type/universal-layer-options)
                   (into (:accepts reg))
                   (set/difference (set (:rejects reg)))
                   sort)
          base (or (::lay-doc-base (meta v))
                   (:doc (meta v))
                   "")]
      (alter-meta! v assoc
                   :doc (str base "\n\n" (wrap-options-list opts))
                   ::lay-doc-base base))))

(doseq [k (keys (layer-type/registered))]
  (append-accepted-options-block! k))

(defn- deep-merge
  "Recursively merge maps. Non-map values are overwritten."
  [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn- update-opts
  "Update the root :opts of a pose. Non-pose inputs are coerced via
   ->pose first. resolve-tree merges root :opts into every leaf,
   so root-level writes act as plot-level options across the whole
   tree.

   Skips the ->pose call when input is already a pose -- callers
   (options, facet, facet-grid, scale, coord) typically lift first,
   and ->pose is idempotent but not free."
  [sk-or-pose f & args]
  (apply update (if (pose? sk-or-pose) sk-or-pose (->pose sk-or-pose)) :opts f args))

(def ^:private valid-legend-positions
  "Enum of values accepted by :legend-position; mirrors the
   plan_schema.clj :legend-position enum."
  #{:right :bottom :top :none})

(def ^:private valid-scales-values
  "Enum of values accepted by :scales; mirrors the branches in
   impl.plan/coordinate-facet-domains."
  #{:shared :free :free-x :free-y})

(defn options
  "Set plot-level options (title, labels, width, height, etc.).
   Nested maps (e.g. `:theme`) are deep-merged.
   `:width` and `:height` are coerced to long (rounded) so the plan carries
   integer dimensions through to render. On a composite pose
   the options attach to the root so every descendant leaf inherits
   them at plan time."
  [pose opts]
  (when-not (or (nil? opts) (map? opts))
    (throw (ex-info (str "pj/options expects an opts map as the second"
                         " argument, got " (pr-str (type opts)) ": "
                         (pr-str opts) ". Wrap plot-level options in"
                         " a map, e.g. {:title \"...\" :width 800}.")
                    {:caller "pj/options" :value opts})))
  (let [fr (->pose pose "pj/options")
        opts (warn-and-strip-unknown-opts "pj/options" opts plot-options-keys)
        opts (reduce (fn [m k]
                       (if-let [v (get m k)]
                         (do
                           (when-not (number? v)
                             (throw (ex-info (str "pj/options " k " must be a number, got "
                                                  (pr-str (type v)) ": " (pr-str v) ".")
                                             {:caller "pj/options" :option k :value v :type (type v)})))
                           (let [rounded (long (Math/round (double v)))]
                             (when-not (pos? rounded)
                               (throw (ex-info (str "pj/options " k " must round to a positive integer, got: "
                                                    (pr-str v) " (rounds to " rounded ")")
                                               {:caller "pj/options" :option k :value v :rounded rounded})))
                             (assoc m k rounded)))
                         m))
                     opts
                     [:width :height])]
    (when-let [pos (:legend-position opts)]
      (when-not (contains? valid-legend-positions pos)
        (throw (ex-info (str "pj/options :legend-position must be one of "
                             (vec (sort valid-legend-positions))
                             ", got: " (pr-str pos) ".")
                        {:caller "pj/options"
                         :option :legend-position
                         :value pos
                         :accepted valid-legend-positions}))))
    (when (contains? opts :scales)
      (let [v (:scales opts)]
        (when-not (contains? valid-scales-values v)
          (throw (ex-info (str "pj/options :scales must be one of "
                               (vec (sort valid-scales-values))
                               ", got: " (pr-str v) ".")
                          {:caller "pj/options"
                           :option :scales
                           :value v
                           :accepted valid-scales-values})))))
    (update-opts fr deep-merge opts)))

(defn- column-argument
  "Read an argument that names a column and nothing else.

   A mapping value has two readings and `{:column ...}` picks one.
   Faceting has only the one, so the form has no work to do here -- but
   a writer who has learned it for mappings will reach for it, and
   until this it was stashed whole: `(pj/facet pose {:column :g})` drew
   a single unfaceted panel and said nothing, while `(pj/facet pose
   :nosuch)` reported the missing column correctly."
  [caller v]
  (if-not (map? v)
    v
    (if (= (set (keys v)) #{:column})
      (:column v)
      (throw (ex-info (str caller " takes a column of the data, and "
                           (pr-str v) " is not one. Write the column, or"
                           " {:column ...} on its own -- there is no second"
                           " reading here for :value or :scale to choose"
                           " between.")
                      {:caller caller :value v})))))

(defn- reject-composite-for-facet
  "Throw if the input is a composite pose. Facet on composites would
   cross the facet grid with the composite grid, which is deferred."
  [fr]
  (when (and (pose? fr) (pose/composite? fr))
    (throw (ex-info (str "pj/facet and pj/facet-grid are not yet supported on composite poses. "
                         "The facet grid would cross the composite layout. "
                         "Flatten to a single leaf.")
                    {:pose-kind :composite}))))

(defn facet
  "Facet a pose by a column.
   `direction` is `:col` (default, horizontal row) or `:row` (vertical
   column). Faceting is plot-level -- every panel is faceted the same way.
   Composite poses are not supported yet."
  ([pose col] (facet pose col :col))
  ([pose col direction]
   (reject-composite-for-facet pose)
   (when-not (#{:col :row} direction)
     (throw (ex-info (str "pj/facet direction must be :col or :row, got: "
                          (pr-str direction) ".")
                     {:caller "pj/facet"
                      :direction direction
                      :accepted #{:col :row}})))
   (let [col (column-argument "pj/facet" col)
         k (case direction :col :facet-col :row :facet-row)]
     (update-opts pose assoc k col))))

(defn facet-grid
  "Facet a pose by two columns (2D grid).
   Faceting is plot-level -- every panel is faceted the same way.
   Composite poses are not supported yet."
  [pose col-col row-col]
  (reject-composite-for-facet pose)
  (update-opts pose assoc
               :facet-col (column-argument "pj/facet-grid" col-col)
               :facet-row (column-argument "pj/facet-grid" row-col)))

(def ^:private channel->scale-key
  "Channel keyword to the opts key holding its scale spec. Derived from
   `defaults/aesthetic-registry`, so a new scalable aesthetic gets a
   channel by carrying a `:scale-key` there.

   `:group` has none on purpose. It used to map to a `:group-scale`
   that `pose/leaf->draft` never stamped onto a layer and nothing ever
   read, so `pj/scale :group` validated its argument and then changed
   nothing about the plot."
  defaults/channel->scale-key)

(def ^:private continuous-visual-channels
  "Continuous visual channels. These accept `:linear` and `:log` only --
   `:categorical` does not apply to a continuous encoding."
  #{:size :alpha :fill :color})

(def ^:private discrete-visual-channels
  "Discrete visual channels. These accept :categorical only -- there
   is no continuous interpretation for a shape symbol."
  #{:shape})

(def ^:private valid-axis-scale-types
  "Scale types accepted on :x / :y. :linear and :log are continuous;
   :categorical lets users supply an explicit ordering via :domain."
  #{:linear :log :categorical})

(def ^:private valid-continuous-visual-scale-types
  "Scale types accepted on :size / :alpha / :fill / :color."
  #{:linear :log})

(def ^:private valid-discrete-visual-scale-types
  "Scale types accepted on :shape."
  #{:categorical})

(defn scale
  "Set scale on a pose. Scale is plot-level -- it applies across every
   panel. Accepts a type keyword or a scale spec map with `:type`, optional
   `:domain`, optional `:breaks` (explicit tick locations), optional
   `:labels` (custom tick text paired with `:breaks`), and optional
   `:n-ticks` (thin a categorical axis to about this many ticks). On a
   composite pose the scale attaches to the root so every descendant leaf
   inherits it at plan time.

   Channels and accepted scale types:

   - Axis channels (`:x`, `:y`) accept `:linear`, `:log`, `:categorical`.
   - Continuous visual channels (`:size`, `:alpha`, `:fill`, `:color`) accept
     `:linear` and `:log` only -- `:categorical` does not apply.
   - The discrete visual channel `:shape` accepts `:categorical` only --
     `:linear` and `:log` do not apply to a discrete encoding.

   `:group` is refused: it draws nothing of its own, so there is no
   scale to set. It splits a layer into one drawn group per value, and
   the order of those groups is the order of the data.

   To take a channel off its scale for one mapping rather than choose a
   type for the whole plot, write that mapping out in full:
   `{:color {:column :hex :scale false}}` draws the column's values as
   they stand, and `{:size {:value 7 :scale true}}` sends a written
   value through the scale. See the layer option docs for `:color` and
   `:size`.

   The `:domain` on a discrete scale gives explicit category order for the
   legend. On `:shape`, `:values` supplies the symbols to draw those
   categories with, in the same order; `pj/shape-symbols` lists the ones
   available.

   `:labels` requires `:breaks` and must match it in count. Use it to
   render numeric positions with custom text -- for example, days of the
   week on a tile heatmap.

   `:n-ticks` thins a crowded categorical axis to about that many
   evenly-spaced tick labels (a categorical axis otherwise labels every
   category). It applies to discrete axes; numeric axes control tick
   density through the `:tick-spacing-x` / `:tick-spacing-y` options.

   - `(scale pose :x :log)` -- log scale on x-axis.
   - `(scale pose :x {:type :categorical :domain [...]})` -- explicit
     category order.
   - `(scale pose :x {:n-ticks 8})` -- thin a crowded categorical axis to
     about eight evenly-spaced tick labels.
   - `(scale pose :y {:type :linear :breaks [0 5 10]})` -- pin tick locations.
   - `(scale pose :x {:type :linear :breaks [1 2 3 4 5 6 7]
                      :labels [\"Mon\" \"Tue\" \"Wed\" \"Thu\" \"Fri\" \"Sat\" \"Sun\"]})`
     -- numeric positions with custom tick text.
   - `(scale pose :y {:type :log :domain [1 1000]})` -- log scale with
     explicit range.
   - `(scale pose :size :log)` -- log-spaced point sizes.
   - `(scale pose :fill :log)` -- log-spaced tile fill.
   - `(scale pose :shape {:type :categorical :domain [...]})` -- shape
     legend order.
   - `(scale pose :shape {:values [:cross :plus]})` -- pick the symbols."
  [pose channel scale-type]
  (when (= :group channel)
    (throw (ex-info (str "pj/scale has no :group channel. Grouping draws"
                         " nothing of its own -- it splits a layer into one"
                         " drawn group per value, in the order the data"
                         " gives them -- so there is no scale to set. To"
                         " order or restyle what the reader sees, scale the"
                         " aesthetic that draws it: :color or :shape.")
                    {:channel channel
                     :supported (vec (sort (keys channel->scale-key)))})))
  (let [k (or (channel->scale-key channel)
              (throw (ex-info (str "Scale channel must be one of "
                                   (vec (sort (keys channel->scale-key)))
                                   ", got: " channel)
                              {:channel channel})))
        cont-visual? (continuous-visual-channels channel)
        disc-visual? (discrete-visual-channels channel)
        valid-types (cond
                      cont-visual? valid-continuous-visual-scale-types
                      disc-visual? valid-discrete-visual-scale-types
                      :else        valid-axis-scale-types)
        type-kw (if (map? scale-type) (:type scale-type) scale-type)]
    (when-not (or (nil? type-kw) (valid-types type-kw))
      (throw (ex-info
              (cond
                (and cont-visual? (= type-kw :categorical))
                (str "Visual channel " channel " is continuous and does not"
                     " support :categorical scale. Supported: "
                     (vec (sort valid-types)) ".")
                (and disc-visual? (#{:linear :log} type-kw))
                (str "Visual channel " channel " is discrete and does not"
                     " support continuous scale (" type-kw "). Supported: "
                     (vec (sort valid-types)) ".")
                :else
                (str "Unknown scale type: " type-kw ". Supported for "
                     channel ": " (vec (sort valid-types)) "."))
              {:channel channel :scale-type type-kw
               :supported (vec (sort valid-types))})))
    (when (map? scale-type)
      (let [breaks (:breaks scale-type)
            labels (:labels scale-type)]
        (when (and labels (not breaks))
          (throw (ex-info
                  (str "pj/scale :labels requires :breaks. Pass both, or"
                       " drop :labels to keep auto-formatted tick text.")
                  {:caller "pj/scale" :channel channel :labels labels})))
        (when (and breaks labels (not= (count breaks) (count labels)))
          (throw (ex-info
                  (str "pj/scale :breaks and :labels must have the same count, got "
                       (count breaks) " breaks and " (count labels) " labels.")
                  {:caller "pj/scale" :channel channel
                   :breaks (vec breaks) :labels (vec labels)})))
        ;; :values names marker symbols, which only :shape draws. An
        ;; unrecognized symbol would draw as a circle while the legend
        ;; advertised the name, so reject it here rather than render a
        ;; legend that disagrees with its own marks.
        (when-let [values (:values scale-type)]
          (when (not= channel :shape)
            (throw (ex-info
                    (str "pj/scale :values applies to :shape only, got channel "
                         channel ". It names the marker symbols a shape mapping"
                         " draws with; other channels have no symbols to choose."
                         " To choose the colors a categorical :color mapping"
                         " draws with, pass them as a palette:"
                         " (pj/options pose {:palette [\"#e41a1c\" ...]}) for one"
                         " plot, or pj/set-config! for every plot.")
                    {:caller "pj/scale" :channel channel :values (vec values)})))
          (when-let [unknown (seq (remove (set defaults/shape-syms) values))]
            (throw (ex-info
                    (str "pj/scale :shape :values does not recognize "
                         (vec unknown) ". Supported symbols: "
                         defaults/shape-syms ".")
                    {:caller "pj/scale" :channel channel
                     :unknown (vec unknown)
                     :supported defaults/shape-syms}))))))
    (update-opts pose assoc k (if (map? scale-type)
                                (merge {:type (if disc-visual? :categorical :linear)}
                                       scale-type)
                                {:type scale-type}))))

(defn coord
  "Set coordinate transform on a pose. Coord is plot-level -- it
   applies across every panel. On a composite pose the coord attaches
   to the root so every descendant leaf inherits it at plan time.

   Supported coord-types:

   - `:cartesian` -- standard x-right, y-up mapping (the default).
   - `:flip` -- swap x and y axes (horizontal bars / boxplots).
   - `:fixed` -- equal aspect ratio (1 data unit = 1 data unit).
   - `:polar` -- radial mapping: x to angle, y to radius."
  [pose coord-type]
  (when-not (#{:cartesian :flip :polar :fixed} coord-type)
    (throw (ex-info (str "Coordinate must be :cartesian, :flip, :polar, or :fixed, got: " coord-type)
                    {:coord coord-type})))
  (update-opts pose assoc :coord coord-type))

(defn draft
  "Resolve raw input into a draft. Literal composition of the atomic
   steps: `(-> x ->pose pose->draft)`. The 2-arity folds opts into
   the pose with `pj/options` first, mirroring `pj/plan` and `pj/plot`:
   `(-> x ->pose (options opts) draft)`.

   Raw data (a dataset or a bare collection of values) is given a
   default mapping first, exactly as `pj/pose` would, so `(draft data)`
   works without an explicit `pj/pose` call.

   For a leaf pose, returns a `LeafDraft` record (`:layers` is a
   vector of flat maps, one per applicable layer with merged scope;
   `:opts` carries the pose-level options that flow into the plan
   stage). For a composite pose, returns a `CompositeDraft` carrying
   per-leaf drafts (each contextualized -- shared-scale domains
   injected, suppress-* flags applied), the resolved chrome geometry,
   and the layout (path -> rect).

   - `(draft pose)`
   - `(draft pose {:width 800 :title \"Plot\"})`"
  ([pose]
   (when (plan? pose)
     (throw (ex-info (str "pj/draft expects a pose, not a plan. "
                          "A plan is the resolved geometry produced "
                          "by pj/plan; pass the original pose to "
                          "pj/draft, or work with the plan directly.")
                     {:got :plan})))
   (when (draft? pose)
     (throw (ex-info (str "pj/draft expects a pose, not a draft. "
                          "A draft is the intermediate stage produced "
                          "by pj/draft; pass the original pose to "
                          "pj/draft, or call pj/draft->plan on the draft.")
                     {:got :draft})))
   (-> pose (->pose "pj/draft") infer-mapping pose->draft))
  ([pose opts]
   (-> pose
       (->pose "pj/draft")
       (options opts)
       draft)))

(defn- pose-has-data-anywhere?
  "True if any node in the pose tree carries :data -- either on the
   pose itself, on any layer, or on any descendant sub-pose. Used to
   distinguish the legitimate 'data at root flows to mapping-only
   leaves' pattern from the 'no data anywhere' bare-template footgun."
  [pose]
  (or (some? (:data pose))
      (some #(some? (:data %)) (:layers pose))
      (some pose-has-data-anywhere? (:poses pose))))

(defn- bare-template-leaf?
  "True for a leaf pose carrying a mapping but no layers, no
   annotations, and no own :data."
  [leaf]
  (and (not (:poses leaf))
       (seq (:mapping leaf))
       (empty? (:layers leaf))
       (nil? (:data leaf))))

(defn- find-bare-template-leaf
  "Walk the pose tree looking for a bare-template leaf. Returns the
   first one found (or nil)."
  [pose]
  (cond
    (bare-template-leaf? pose) pose
    (:poses pose) (some find-bare-template-leaf (:poses pose))
    :else nil))

(defn- layer-accepted-keys
  "Set of keys a single layer accepts: universal layer options union
   the layer-type's :accepts, minus its :rejects."
  [layer]
  (let [reg (when-let [k (:layer-type layer)]
              (layer-type/lookup k))]
    (-> (set layer-type/universal-layer-options)
        (into (:accepts reg))
        (set/difference (set (:rejects reg))))))

(defn- layers-in-subtree
  "All explicit layers reachable from this pose (its own :layers plus
   every descendant sub-pose's layers)."
  [pose]
  (mapcat :layers (tree-seq :poses :poses pose)))

(defn- accepted-keys-in-subtree
  "Union of accepted keys across every layer reachable from this pose."
  [pose]
  (transduce (map layer-accepted-keys) set/union (layers-in-subtree pose)))

(defn- check-pose-mappings-consumed!
  "Walk every pose in the tree; warn (or strict-throw) when a pose's
   :mapping carries keys that no descendant layer accepts. The pose's
   own :mapping flows down to its descendants via resolve-tree, so a
   key consumed by any descendant counts as live. Layer-only keys
   like :y-min, :x-end, :fill, :text live in :accepts of specific
   layer types; placing them at the pose with no consuming layer is
   almost always a forgotten lay-errorbar / lay-tile / lay-interval-h."
  [root]
  (doseq [p (tree-seq :poses :poses root)
          :when (seq (:mapping p))
          ;; Skip when no explicit layer exists in the subtree -- the
          ;; :infer path may pick a layer at draft time, and we have
          ;; no way to know what it'll accept until then.
          :when (seq (layers-in-subtree p))]
    (let [accepted (accepted-keys-in-subtree p)
          unused (vec (remove accepted (keys (:mapping p))))]
      (when (seq unused)
        (let [strict-val (:strict (defaults/config))
              scope (if (= p root) "the root pose" "a sub-pose")
              msg (str "pose-level mapping at " scope
                       " carries key(s) " unused
                       " that no descendant layer accepts."
                       " Did you forget a consuming layer (e.g."
                       " lay-errorbar for :y-min/:y-max, lay-tile for"
                       " :fill, lay-interval-h for :x-end, lay-text for"
                       " :text)? Or move the key to a specific lay-* opts"
                       " map.")]
          (when-not (or (nil? strict-val) (boolean? strict-val))
            (throw (ex-info (str ":strict config value must be true or false, got: "
                                 (pr-str strict-val))
                            {:value strict-val})))
          (if strict-val
            (throw (ex-info msg
                            {:caller "pj/pose->draft"
                             :unused-keys unused
                             :scope (if (= p root) :root :sub-pose)}))
            (println (str "Warning: " msg))))))))

(defn- check-pose-shape!
  "Precondition for pj/pose->draft (and so for every shortcut that
   threads through it). Surfaces the bare-template footgun (mapping
   set but no data and no layers) and warns on pose-level mappings
   that no layer would consume."
  [fr]
  (when (and (not (pose-has-data-anywhere? fr))
             (find-bare-template-leaf fr))
    (let [bare (find-bare-template-leaf fr)]
      (throw (ex-info (str "pj/pose->draft: got a pose with no data and no layers. "
                           "The mapping " (pr-str (:mapping bare))
                           " is set, but nothing to draft from. "
                           "Add a layer with pj/lay-* (e.g. (pj/lay-point pose :x :y)) "
                           "or attach data via pj/with-data.")
                      {:caller "pj/pose->draft"
                       :pose-shape :bare-template
                       :mapping (:mapping bare)}))))
  (check-pose-mappings-consumed! fr))

(defn plan
  "Convert a pose into a plan. Literal composition of the atomic
   steps: `(-> x ->pose pose->draft draft->plan)`. The 2-arity folds
   opts into the pose with `pj/options` first:
   `(-> x ->pose (options opts) plan)`.

   Raw data (a dataset or a bare collection of values) is given a
   default mapping first, exactly as `pj/pose` would, so `(plan data)`
   works without an explicit `pj/pose` call.

   For a leaf pose, returns a `Plan` record with one panel per facet
   variant. For a composite pose, returns a `CompositePlan` record
   with `:sub-plots` tying each leaf path to its rect and sub-plan,
   plus `:chrome` carrying the resolved layout geometry (title-band,
   grid-rect, strip labels, shared-legend spec).

   - `(plan pose)`
   - `(plan pose {:title \"My Plot\"})`"
  ([pose]
   (when (plan? pose)
     (throw (ex-info (str "pj/plan expects a pose, not a plan. "
                          "Use the plan directly, or call pj/plot on the pose.")
                     {:got :plan})))
   (when (draft? pose)
     (throw (ex-info (str "pj/plan expects a pose, not a draft. "
                          "A draft is the intermediate stage produced "
                          "by pj/draft; pass the original pose to "
                          "pj/plan, or call pj/draft->plan on the draft.")
                     {:got :draft})))
   (-> pose (->pose "pj/plan") infer-mapping pose->draft draft->plan))
  ([pose opts]
   (-> pose
       (->pose "pj/plan")
       (options opts)
       plan)))

(defn frames
  "Where a plot's panels sit on the canvas, and how to get between data
   space and drawing space.

   Takes a plan or a pose; a pose is planned first. Returns a map:

   - `:canvas` -- `[x y width height]` of the whole image, in drawing units
   - `:panels` -- one entry per panel, each carrying `:row`, `:col`,
     `:coord`, `:x-domain`, `:y-domain`, `:x-scale`, `:y-scale`,
     `:invertible?` and `:frames`

   A panel's `:frames` names two rectangles, both `[x y width height]`
   in canvas coordinates: `:panel-box` (the panel with its axis margin)
   and `:drawing-area` (the background inside that margin, where data
   marks are clipped). The canvas is reported once, at the top: it
   belongs to the plot rather than to any panel.

   The result contains no functions, so it can be printed, compared and
   read back from `pr-str`. To map between the spaces, pass a panel entry
   to `pj/to-drawing` or `pj/to-data`.

   For a composite, every cell's panels report canvas coordinates, so
   their rectangles can be compared without further arithmetic.

   This is the same computation the renderer draws with. Use it to place
   your own annotations beside a plot, to compose a Plotje membrane with
   hand-built Membrane views, or to read a pointer position back as data.

   - `(frames my-pose)`
   - `(-> my-plan frames :panels first :frames :drawing-area)`"
  [plan-or-pose]
  (let [p (if (plan? plan-or-pose) plan-or-pose (plan plan-or-pose))
        ;; A leaf plan sizes itself with :total-width/:total-height; a
        ;; composite with :width/:height.
        w (or (:total-width p) (:width p))
        h (or (:total-height p) (:height p))]
    {:canvas [0.0 0.0 (double w) (double h)]
     :panels (frames-impl/plan-frames p [0.0 0.0])}))

(defn to-drawing
  "Where data positions land on the canvas, for one panel of `pj/frames`.

   Takes a panel entry -- an element of `(:panels (frames plot))` -- and
   either one x and y, or a dataset of them with `:x` and `:y` columns.
   The dataset arity maps whole columns and builds the panel's scales
   once, so it is the one to reach for when placing many positions.

   - `(to-drawing panel 3.2 21.0)` returns `[x y]` in canvas coordinates
   - `(to-drawing panel {:x [3.2 4.0] :y [21.0 18.5]})` returns a dataset
     with the same two column names, now in canvas coordinates

   A dataset rather than a collection of pairs because the two
   coordinates of a point share one index space, which a dataset states
   and two loose sequences only promise. Anything `tc/dataset` coerces
   works.

   The result is in canvas coordinates, measured from the top left of the
   whole image. A `{:in :drawing-area}` layer measures from the drawing
   area's own corner instead, so drawing these positions back means
   subtracting that corner first.

   A categorical axis is a band scale: it has a position for each of its
   categories and none between them. Asking it for anything else -- a
   category the axis does not carry, or a fractional place such as 2.5
   -- throws, naming the value and the categories it could have been.
   Under `:coord :flip` the arguments stay in data order, even though a
   panel entry's `:x-domain` and `:y-domain` describe the drawn axes and
   the flip has already swapped those."
  ([panel x y] (frames-impl/to-drawing panel x y))
  ([panel data] (frames-impl/to-drawing panel data)))

(defn to-data
  "What data positions the canvas coordinates name, for one panel of
   `pj/frames`. The inverse of `pj/to-drawing`. An interaction reads
   this direction: which value is under the pointer, which range a
   selection covers.

   Throws under a coordinate system with no inverse. `:polar` maps x and
   y together to an angle and a radius, so a canvas position there does
   not name one pair of data values. A panel entry reports which case it
   is in `:invertible?`.

   - `(to-data panel 412.0 88.5)` returns `[x y]` in data values
   - `(to-data panel {:x [412.0] :y [88.5]})` returns a dataset with the
     same two column names, now in data values

   A continuous axis answers with numbers, so its column is `:float64`.
   A categorical axis answers with the category whose band holds the
   position, so its column holds those."
  ([panel cx cy] (frames-impl/to-data panel cx cy))
  ([panel data] (frames-impl/to-data panel data)))

(defn membrane
  "Resolve a pose into a `PlotjeMembrane`. Literal composition of the
   atomic steps: `(let [pose (->pose x), opts (:opts pose {})]
                    (-> pose
                        pose->draft
                        draft->plan
                        (plan->membrane opts)))`.
   The let lifts the pose once so the chain can pluck pose-level
   opts and pass them to `plan->membrane`. The 2-arity folds opts
   into the pose with `pj/options` first.

   Returns a `PlotjeMembrane` -- a Membrane UI component (implements
   `IOrigin`, `IBounds`, `IChildren`) carrying the rendered drawables
   plus plan-derived width and height; the title, when set, rides as
   `:plotje/title`. Render-time options (`:tooltip`, `:theme`,
   `:palette`, `:color-scale`, `:color-midpoint`) ride along on the
   pose's `:opts` and reach `plan->membrane` through this call.

   Useful for exploring rendering targets beyond the SVG and Java2D
   backends Plotje wires in today: any Membrane backend can consume
   the result of `pj/membrane` via the standard Membrane protocols.

   Raw data (a dataset or a bare collection of values) is given a
   default mapping first, exactly as `pj/pose` would, so
   `(membrane data)` works without an explicit `pj/pose` call.

   - `(membrane pose)`
   - `(membrane pose {:tooltip true})`"
  ([pose]
   (when (plan? pose)
     (throw (ex-info (str "pj/membrane expects a pose, not a plan. "
                          "Call pj/plan->membrane on the plan, or "
                          "pass the original pose to pj/membrane.")
                     {:got :plan})))
   (when (draft? pose)
     (throw (ex-info (str "pj/membrane expects a pose, not a draft. "
                          "Call pj/draft->plan and pj/plan->membrane "
                          "on the draft, or pass the original pose "
                          "to pj/membrane.")
                     {:got :draft})))
   (let [fr (-> pose (->pose "pj/membrane") infer-mapping)
         opts (:opts fr {})]
     (-> fr
         pose->draft
         draft->plan
         (plan->membrane opts))))
  ([pose opts]
   (-> pose
       (->pose "pj/membrane")
       (options opts)
       membrane)))

(defn plot
  "Render a pose to a figure. The format keyword in the pose's
   `:opts` (`{:format :svg}` -- default; `{:format :bufimg}` for
   raster PNG via Java2D; or any other registered backend) selects
   which `membrane->plot` defmethod runs.

   On a composite pose, leaves are rendered individually and tiled
   via the layout in the resolved chrome, in the same chosen format.
   The pose flows through the canonical
   `pose -> draft -> plan -> membrane -> plot` pipeline for both
   leaf and composite shapes. pj/plot is a literal composition of
   the public atomic steps:

   `(let [pose (->pose x)
         opts (:opts pose {})
         fmt  (or (:format opts) :svg)]
     (-> pose
         pose->draft
         draft->plan
         (plan->membrane opts)
         (membrane->plot fmt opts)))`

   Plan-derived dimensions ride as record fields on the membrane
   (accessed via `membrane.ui/width`/`membrane.ui/height`); the
   title rides as `:plotje/title`. `membrane->plot` reads them from
   there.

   Raw data (a dataset or a bare collection of values) is given a
   default mapping first, exactly as `pj/pose` would, so `(plot data)`
   renders the inferred default instead of a blank figure.

   - `(plot pose)`
   - `(plot pose {:width 800 :title \"My Plot\"})`
   - `(plot pose {:format :bufimg})` -- returns a BufferedImage."
  ([pose]
   (when (plan? pose)
     (throw (ex-info (str "pj/plot expects a pose, not a plan. "
                          "A plan is the resolved geometry; call "
                          "pj/plan->plot on the plan, or pass the "
                          "original pose to pj/plot.")
                     {:got :plan})))
   (when (draft? pose)
     (throw (ex-info (str "pj/plot expects a pose, not a draft. "
                          "A draft is an intermediate stage produced "
                          "by pj/draft; pass the original pose to "
                          "pj/plot to render it end-to-end.")
                     {:got :draft})))
   (let [fr (-> pose (->pose "pj/plot") infer-mapping)
         opts (:opts fr {})
         fmt (or (:format opts) :svg)]
     (-> fr
         pose->draft
         draft->plan
         (plan->membrane opts)
         (membrane->plot fmt opts))))
  ([pose opts]
   (-> pose
       (->pose "pj/plot")
       (options opts)
       plot)))

;; ---- SVG Summary ----

(defn svg-summary
  "Extract structural summary from SVG hiccup for testing.
   Returns a map with `:width`, `:height`, `:panels`, `:points`, `:lines`,
   `:dashed-lines`, `:dash-patterns`, `:polygons`, `:tiles`, `:visible-tiles`,
   and `:texts` -- useful for asserting plot structure.
   Accepts SVG hiccup or a pose (auto-renders to SVG first).

   - `(svg-summary (plot fr))` -- summary of rendered SVG.
   - `(svg-summary my-pose)` -- auto-renders pose (leaf or composite)."
  ([svg-or-pose]
   (if (pose? svg-or-pose)
     (svg/svg-summary (plot svg-or-pose))
     (svg/svg-summary svg-or-pose)))
  ([svg-or-pose theme]
   (if (pose? svg-or-pose)
     (svg/svg-summary (plot svg-or-pose) theme)
     (svg/svg-summary svg-or-pose theme))))

;; ---- Multi-Plot Composition ----

(defn- coerce-arrange-input
  "Turn one pj/arrange input into a leaf-pose plain map. Accepts
   pose-shaped leaf maps (passed through). Anything else throws with
   a message tailored to the actual type -- nil, composite pose,
   plain map, hiccup vector, plain vector -- so the user sees what
   went wrong without re-reading the same hiccup advice for every
   non-pose input."
  [p idx]
  (let [prefix (str "pj/arrange input at index " idx)]
    (cond
      (and (pose? p) (pose/leaf? p)) p

      (and (pose? p) (pose/composite? p))
      (throw (ex-info (str prefix " is a composite pose. "
                           nested-composite-rejection-msg)
                      {:index idx}))

      (nil? p)
      (throw (ex-info (str prefix " is nil. Each input must be a leaf "
                           "pose -- e.g. (pj/lay-point data :x :y). "
                           "If you have an optional cell, drop it from "
                           "the input sequence rather than passing nil.")
                      {:index idx}))

      (and (vector? p) (keyword? (first p)))
      (throw (ex-info (str prefix " looks like rendered hiccup (head: "
                           (pr-str (first p)) "). pj/arrange takes "
                           "leaf poses, not pre-rendered output; pass "
                           "the pose itself, or build your own [:div ...] "
                           "if you want raw hiccup composition.")
                      {:index idx :head (first p)}))

      (vector? p)
      (throw (ex-info (str prefix " is a plain vector. pj/arrange takes "
                           "leaf poses; if you have a sequence of poses, "
                           "splice them in (e.g. (apply pj/arrange poses)) "
                           "or use the nested form for an explicit grid: "
                           "(pj/arrange [[a b] [c d]]).")
                      {:index idx :type (type p)}))

      (map? p)
      (throw (ex-info (str prefix " is a map but not a pose (no :layers "
                           "or :poses key). Build a leaf pose first via "
                           "pj/pose / pj/lay-* and pass that.")
                      {:index idx :type (type p)}))

      :else
      (throw (ex-info (str prefix " must be a leaf pose. Got: "
                           (pr-str (type p)) ".")
                      {:index idx :type (type p)})))))

(defn- render-composite
  "Kindly render function for a composite pose returned by pj/arrange.
   Captures the config snapshot so theme/palette/config bindings at
   construction time survive into render time. Delegates to pj/plot,
   which honors :format from :opts uniformly across composite and
   leaf paths."
  [captured-config]
  (fn [composite]
    (if captured-config
      (binding [defaults/*config* captured-config]
        (plot composite))
      (plot composite))))

(defn arrange
  "Arrange multiple leaf poses in a grid. Returns a composite pose
   that renders through the compositor via membrane -- so `:svg`,
   `:bufimg`, and any other membrane target work uniformly.

   Inputs must be leaf poses. Pre-rendered hiccup is not accepted;
   build your own `[:div ...]` if you need to combine already-rendered
   values outside the library.

   Opts:

   - `:cols` -- explicit column count (default: min(4, n-plots)).
   - `:title` -- centered title band above the grid.
   - `:width` -- total composite width.
   - `:height` -- total composite height.
   - `:share-scales` -- subset of `#{:x :y}` shared across cells
     (default: `#{}`).

   - `(arrange [fr-a fr-b])` -- 1x2 row.
   - `(arrange [fr-a fr-b fr-c] {:cols 2 :width 900})` -- 2x2 grid (wraps).
   - `(arrange [[fr-a fr-b] [fr-c fr-d]])` -- explicit 2x2 grid."
  ([plots] (arrange plots {}))
  ([plots opts]
   (let [cfg (defaults/config)
         {:keys [cols title share-scales]
          :or {share-scales #{}}} opts
         _ (when-not (and (or (set? share-scales)
                              (sequential? share-scales))
                          (every? #{:x :y} share-scales))
             (throw (ex-info (str "pj/arrange :share-scales must be a"
                                  " subset of #{:x :y}, got "
                                  (pr-str share-scales) ".")
                             {:caller "pj/arrange"
                              :option :share-scales
                              :value share-scales
                              :accepted #{:x :y}})))
         width  (or (:width opts)  (:width cfg))
         height (or (:height opts) (:height cfg))
         nested? (and (sequential? plots)
                      (sequential? (first plots))
                      (not (keyword? (ffirst plots))))
         rows-in (if nested? (vec plots) [(vec plots)])
         flat-plots (vec (apply concat rows-in))
         n-plots (count flat-plots)
         _ (when (zero? n-plots)
             (throw (ex-info "pj/arrange requires at least one plot." {:plots plots})))
         leaves (vec (map-indexed (fn [i p] (coerce-arrange-input p i)) flat-plots))
         _ (when (= 1 n-plots)
             (let [leaf-opts (:opts (first leaves))
                   leaf-w (:width leaf-opts)
                   leaf-h (:height leaf-opts)
                   composite-w (or (:width opts) (:width cfg))
                   composite-h (or (:height opts) (:height cfg))]
               (when (or (and leaf-w (not= (long leaf-w) (long composite-w)))
                         (and leaf-h (not= (long leaf-h) (long composite-h))))
                 (println (str "Warning: pj/arrange wraps a single leaf in"
                               " a composite that fills the cell;"
                               " the leaf's :width/:height ("
                               (or leaf-w "-") "x" (or leaf-h "-") ") are"
                               " overridden by the composite's geometry ("
                               composite-w "x" composite-h "). Pass"
                               " :width/:height to pj/arrange instead,"
                               " or skip arrange and use the leaf directly.")))))
         n-cols (or cols
                    (if nested? (count (first rows-in))
                        (min 4 n-plots)))
         _ (when-not (pos? (long n-cols))
             (throw (ex-info ":cols must be a positive integer." {:cols cols})))
         row-partitions (if nested?
                          (map #(mapv (fn [i] (nth leaves i))
                                      (range (reduce + (map count (take % rows-in)))
                                             (reduce + (map count (take (inc %) rows-in)))))
                               (range (count rows-in)))
                          (partition-all n-cols leaves))
         row-poses (mapv (fn [row]
                           {:layout {:direction :horizontal}
                            :poses (vec row)})
                         row-partitions)
         composite {:opts (cond-> {:width  (long (Math/round (double width)))
                                   :height (long (Math/round (double height)))}
                            title (assoc :title title)
                            (seq share-scales) (assoc :share-scales (set share-scales)))
                    :layout {:direction :vertical}
                    :poses row-poses}]
     (kind/fn composite
       {:kindly/f (render-composite defaults/*config*)}))))

;; ---- Save ----

(defn- assert-saveable-pose!
  "Throw if `fr` would render as a blank document. A pose is saveable
   if it is a composite (has :poses), or if it has data plus at least
   one of :layers / :mapping / :poses. Catches the silent-blank-file
   case where an empty `(pj/pose)` reaches `pj/save`."
  [caller fr]
  (let [composite? (seq (:poses fr))
        has-data? (some? (:data fr))
        has-layers? (seq (:layers fr))
        has-mapping? (seq (:mapping fr))]
    (when-not (or composite?
                  (and has-data? (or has-layers? has-mapping?)))
      (throw (ex-info
              (str caller " was given an empty pose -- nothing to"
                   " render. Attach data and a layer first, e.g."
                   " (-> data (pj/lay-point :x :y))," " or use"
                   " pj/pose with sub-poses for a composite.")
              {:caller caller :pose fr})))))

(defn- infer-format-from-path
  "Map a path's file extension to a save format. Returns nil for
   unknown extensions; callers fall back to opts or default."
  [path-str]
  (let [lower (.toLowerCase ^String path-str)]
    (cond
      (.endsWith lower ".svg") :svg
      (.endsWith lower ".png") :png
      :else nil)))

(defn save
  "Save a plot to a file. Format resolution, in precedence order:
   1. `:format` in the 3-arity `opts` map wins (must be `:svg` or
      `:png`).
   2. `:format` on the pose's `:opts` (`:svg` or `:png`; legacy
      `:bufimg` is translated to `:png`).
   3. Otherwise inferred from the path extension (`.svg` -> `:svg`,
      `.png` -> `:png`).
   4. Default `:svg`.

   When the resolved format and the path extension disagree, prints
   a warning -- the file still gets the bytes the resolved format
   produces, but the extension is misleading.

   The save vocabulary names the file format. The plot vocabulary
   (`pj/plot`'s `:format`) names the JVM return type -- `:svg` for
   hiccup, `:bufimg` for a Java2D BufferedImage. A pose-level
   `:format` flows into both contexts; save reinterprets `:bufimg`
   as `:png` because the file on disk is a PNG.

   Arguments:

   - `pose` -- a pose, or raw data (a dataset or a bare collection of
     values), which is given a default mapping first, exactly as
     `pj/pose` would.
   - `path` -- file path (string or `java.io.File`).
   - `opts` -- same options as plot, but `:format` accepts only `:svg`
     or `:png`.

   Tooltip and brush interactivity are not included in saved files.

   Returns the written file as a `java.io.File` carrying `:kind/image`
   metadata, so evaluating a `pj/save` call in a notebook also shows
   the saved chart. The file prints as its path and compares equal to
   a plain `java.io.File` on the same path, so `(str (pj/save ...))`
   still gives the path string.

   - `(save my-pose \"plot.svg\")` -- SVG.
   - `(save my-pose \"plot.png\")` -- inferred PNG.
   - `(save my-pose \"plot.svg\" {:format :png})` -- opts override (warns)."
  ([pose path] (save pose path nil))
  ([pose path opts]
   (when-not (or (nil? opts) (map? opts))
     (throw (ex-info (str "pj/save expects an opts map as the third"
                          " argument, got " (pr-str (type opts)) ": "
                          (pr-str opts) ".")
                     {:caller "pj/save" :value opts})))
   (when-let [opts-fmt (:format opts)]
     (when-not (#{:svg :png} opts-fmt)
       (throw (ex-info (str "pj/save :format must be :svg or :png, got "
                            (pr-str opts-fmt) ". The save vocabulary names"
                            " the file format; use pj/plot for in-memory"
                            " return types like :bufimg.")
                       {:caller "pj/save" :format opts-fmt}))))
   (let [path-str (str path)
         fr (-> pose (->pose "pj/save") infer-mapping)
         _ (assert-saveable-pose! "pj/save" fr)
         fr (if (seq opts) (options fr opts) fr)
         pose-fmt (:format (:opts fr))
         pose-fmt (if (= pose-fmt :bufimg) :png pose-fmt)
         path-fmt (infer-format-from-path path-str)
         resolved-fmt (or pose-fmt path-fmt :svg)]
     (when (and path-fmt (not= path-fmt resolved-fmt))
       (println (str "Warning: pj/save writing " (name resolved-fmt)
                     " bytes to a path with extension suggesting "
                     (name path-fmt) ": " path-str)))
     (when-let [parent (.getParentFile (java.io.File. path-str))]
       (when-not (.isDirectory parent)
         (throw (ex-info (str "pj/save: cannot write to " path-str
                              " -- parent directory " (.getPath parent)
                              " does not exist. Create it first or pick"
                              " an existing directory.")
                         {:path path-str :parent (.getPath parent)}))))
     (case resolved-fmt
       :svg (let [out (render-impl/plan->plot (plan fr) :svg (:opts fr {}))]
              (spit path (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                              (svg/hiccup->svg-str out))))
       :png (let [img (render-impl/plan->plot (plan fr) :bufimg (:opts fr {}))]
              ((resolve 'scicloj.plotje.render.bufimg/save-png) img path))
       (throw (ex-info (str "pj/save cannot write format "
                            (pr-str resolved-fmt) " to a file. Supported: "
                            ":svg, :png.")
                       {:format resolved-fmt :path path-str})))
     (kind/image (pf/imeta-file path) {:class "plotje-plot"}))))
