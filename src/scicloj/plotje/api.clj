(ns scicloj.plotje.api
  "Public API for plotje -- composable plotting in Clojure."
  (:require [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.impl.pose :as pose]
            [scicloj.plotje.impl.compositor :as compositor]
            [scicloj.plotje.impl.plan :as plan]
            [scicloj.plotje.impl.plan-schema :as ss]
            [scicloj.plotje.impl.pose-schema :as pose-schema]
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
  `(let [cfg# ~config-map]
     (defaults/validate-config-keys! "pj/with-config" cfg#)
     (binding [defaults/*config* cfg#]
       ~@body)))

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
  "Documentation for the keys a `pj/lay-*` mapping accepts: aesthetics
   and layer settings such as `:stat`, `:bins` and `:position`.
   Maps each key to a description string."
  layer-type/layer-option-docs)

(defn aesthetic-roles
  "What a distinction given to each aesthetic is put to work as, as a
   map from aesthetic to role.

   - `:positional` -- the mark is placed by the value.
   - `:appearance` -- the marks share a place and are told apart by how
     they look, and a legend says which is which.
   - `:grouping` -- the marks share a place and are not told apart.
   - `:panel` -- each value gets a panel of its own, told apart by a
     strip label. `pj/facet` and `pj/facet-grid` write these.

   A role is not a category: a category is a value a categorical
   column holds, which is what a role is given.

   A function rather than a value, as `pj/shape-symbols` is: the
   registry grows from one release to the next."
  []
  (into {} (map (fn [[k entry]] [k (:role entry)]))
        defaults/aesthetic-registry))

(defn compound-key-aesthetics
  "The aesthetics that read several columns as one distinction, united,
   as a set. Every other aesthetic that takes a column reports a vector
   as several columns where one goes.

   `{:group [:part :dimension]}` is one grouping keyed on the pair, and
   `{:col [:part :dimension]}` draws a panel per combination the data
   holds -- while `{:color [:part :dimension]}` is reported, because a
   mark has one colour. The three spellings of a vector read alike:
   bare, `{:series [...]}` and `{:column [...]}`.

   The same set the check reads, so a table built from this cannot
   drift from what is enforced. Use it as `pj/aesthetic-scales` is
   used.

   A function rather than a value, as `pj/aesthetic-roles` is: the
   registry grows from one release to the next."
  []
  defaults/compound-key-aesthetics)

(def panel-aesthetic-docs
  "Documentation for the panel aesthetics -- the mapping keys that send
   each value of a distinction to a panel of its own. Written on a
   pose, not on a layer. `pj/facet` given a column, and `pj/facet-grid`,
   write one. Maps each key to a description string."
  defaults/panel-aesthetic-docs)

(def aesthetic-scales
  "What each aesthetic's scale accepts, one entry per aesthetic that
   has a scale, in display order.

   Each entry carries `:aesthetic`, the `:types` that aesthetic can be
   read through, and the spec `:keys` it reads beside `:type` and
   `:domain`, which belong to every scale. Both are ordered vectors,
   and aesthetics with the same capabilities are adjacent so a table
   can group them.

   The same tables `pj/scale` and a mapping's `:scale` validate
   against, so a reference table built from this cannot drift from what
   they enforce. Use it as `pj/config-key-docs` is used."
  defaults/aesthetic-scales)

(defn shape-symbols
  "Every marker symbol a `:shape` mapping can draw.

   `(pj/shape-palette)` is the first part, in the order categories are
   assigned. The rest are drawn only when named -- `:circle-open` is a
   ring rather than a disc, which keeps overlapping points countable.

   Pass a selection of these as `:values` to
   `(pj/scale pose :shape {:values [...]})` to choose them yourself, or
   name one for a whole layer with `{:shape :circle-open}`.

   A function rather than a value, as `pj/config` is: the list grows
   from one release to the next, and a value read at load time could
   not follow it. Adding a symbol of your own is not supported -- the
   shapes are drawn by a fixed table in the renderer."
  []
  (defaults/drawable-shapes))

(defn shape-palette
  "The symbols assigned to categories automatically, in order. A plot
   with more categories than these repeats a symbol, so two categories
   cannot be told apart, and that warns at plan time.

   Published apart from `pj/shape-symbols` because the two answer
   different questions: this is what a plot draws when you say nothing,
   and `pj/shape-symbols` is what you may write. Adding to this one
   changes which symbol every existing plot gives each category; adding
   to the other takes nothing away.

   A function rather than a value, as `pj/config` is."
  []
  (defaults/shape-palette))

(defn set-config!
  "Set global config overrides. Persists across calls until reset.

   - `(set-config! {:color-values :dark2 :theme {:bg \"#FFFFFF\"}})` --
     override the categorical colours and the background.
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
  "Build a vector of `[x y]` pairs from two column-name sequences, as
   cells for `pj/arrange`: `(pj/arrange data (pj/cross xs ys))` draws a
   panel per pair. For a grid of panels with the `xs` across and the
   `ys` down, use `(pj/cross-matrix data xs ys)`.

   - `(pj/cross [:a :b] [:c :d])` returns `[[:a :c] [:a :d] [:b :c] [:b :d]]`."
  [xs ys]
  (when-not (sequential? xs)
    (throw (ex-info (str "pj/cross expects two sequentials of column"
                         " references, got xs: " (pr-str (type xs))
                         " (" (pr-str xs) "). Write two vectors of columns,"
                         " as in (pj/cross [:a :b] [:c :d]).")
                    {:caller "pj/cross" :argument :xs :value xs})))
  (when-not (sequential? ys)
    (throw (ex-info (str "pj/cross expects two sequentials of column"
                         " references, got ys: " (pr-str (type ys))
                         " (" (pr-str ys) "). Write two vectors of columns,"
                         " as in (pj/cross [:a :b] [:c :d]).")
                    {:caller "pj/cross" :argument :ys :value ys})))
  (when (or (empty? xs) (empty? ys))
    (throw (ex-info (str "pj/cross got an empty sequence (xs: "
                         (pr-str xs) ", ys: " (pr-str ys) "). Provide"
                         " at least one column reference per side.")
                    {:caller "pj/cross" :xs xs :ys ys})))
  (resolve/cross xs ys))

;; ---- Series: the wide-side reading of a grouping ----

(def series-mapping
  "The series a mapping value asks for, as `{:cols [...] :as label}`, or
   nil where it asks for none. See `pose/series-mapping`."
  pose/series-mapping)

(def ^:private series-mapping-keys
  "The keys a series written out may carry: the columns it reads, the
   name for the key column the pivot invents, and the scale its value
   column is read through -- the same `:scale` every other mapping map
   takes."
  #{:series :as :scale})

(defn- check-series-keys!
  "Throw where a series written out carries a key it does not take.

   Every other mapping map is held to `pose/check-explicit-mapping!`,
   which reports an unexpected key at the call. Without this the series
   map was the one mapping form with no such check, so a misspelled
   `:as` was ignored in silence and the legend came out titled
   `:series`."
  [caller v]
  (when (map? v)
    (let [unknown (remove series-mapping-keys (keys v))]
      (when (seq unknown)
        (throw (ex-info (str caller " was given a series with unexpected"
                             " key(s): " (vec unknown) ". A series names the"
                             " columns it reads with :series, the key column"
                             " the pivot invents with :as, and the scale its"
                             " value column is read through with :scale.")
                        {:caller caller :value v :unknown (vec unknown)}))))))

(defn series-mapping?
  "True of a mapping value that asks for a series."
  [v]
  (some? (series-mapping v)))

(def ^:private series-slots
  "The aesthetics a series may be written under, as a set."
  (set pose/series-aesthetics))

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
   (`:theme`, `:color-values`, ...) ride on the draft's `:opts` and form
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
   (`:theme`, `:color-values`, ...) ride on the draft's `:opts` and form
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
   opts map with optional `:tooltip`, `:theme`, `:color-values`, etc.

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

;; ---- Pose Validation ----

(defn valid-pose?
  "Check if a pose conforms to the Malli schema.

   - `(valid-pose? (lay-point data :x :y))` -- true if valid.

   The schema is structural and deliberately permissive: it says what
   shape a pose has, not whether the columns it names are in the data.
   A pose built by `pj/pose`, `pj/lay-*`, `pj/options`, `pj/facet`,
   `pj/arrange`, `pj/coord` or `pj/scale` conforms by construction; the
   check is for a pose that has been reached into and changed, which is
   the one place the constructors cannot speak for."
  [pose]
  (pose-schema/valid? pose))

(defn explain-pose
  "Explain why a pose does not conform to the Malli schema.
   Returns `nil` if valid, or a Malli explanation map if invalid.

   - `(explain-pose (assoc (lay-point data :x :y) :layers {}))`"
  [pose]
  (pose-schema/explain pose))

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
    ;; A map whose value is itself a map is not a map of columns. It is
    ;; nearly always a pose written by hand without the key that marks
    ;; one -- `{:opts {...} :panels [...]}` -- and Tablecloth read it as
    ;; a column holding map entries, so the plot drew the keys as data
    ;; with nothing said.
    (and (map? d) (some map? (vals d)))
    (let [ks (vec (keep (fn [[k v]] (when (map? v) k)) d))]
      (throw (ex-info (str "The map given as data holds a map under " ks
                           ", and a column of data is a sequence of values."
                           " If the map is meant as a pose, it is recognized"
                           " by :layers, :poses or a :mapping map -- for"
                           " example {:data {:a [1 2]} :mapping {:x :a}"
                           " :layers []}. Its keys: " (vec (keys d)) ".")
                      {:keys (vec (keys d)) :map-valued ks})))
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
    (plan? x)
    (throw (ex-info (str caller " expects a pose or data, not a plan. A plan"
                         " is the resolved geometry produced by pj/plan;"
                         " pass the original pose, or render the plan with"
                         " pj/plan->plot.")
                    {:caller caller :got :plan}))

    (draft? x)
    (throw (ex-info (str caller " expects a pose or data, not a draft. A draft"
                         " is the intermediate stage produced by pj/draft;"
                         " pass the original pose.")
                    {:caller caller :got :draft}))

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

(defn pose?
  "Return true if x is a pose-shaped plain map: one carrying
   `:layers`, `:poses`, or a map-valued `:mapping`."
  [x]
  (pose/pose? x))

(declare prepare-pose pose-kind validate-pose-shape
         check-position-mapping check-column-ref-types
         check-explicit-mappings check-mapping-in-map-slot
         check-pose-shape! pose-mapping-keys)

(defn- mapping-map?
  "True of a map that names aesthetics rather than holding columns of
   data: every key is one a pose mapping takes, and at least one
   aesthetic names a column.

   A mapping is nearly a pose already -- it is what `pj/pose`'s second
   argument holds -- and reading it as one is what lets a cell of
   `pj/arrange` be written `{:x :mpg :y :cyl}`. The collision it has
   to clear is with data, since a dataset written as a map of columns
   is also a keyword-keyed map. The values decide: a column of data is
   a sequence, and a column reference is a keyword or a string. So
   `{:x [1 2 3] :y [4 5 6]}` is data and `{:x :mpg :y :cyl}` is a
   mapping, which is the reading each already has everywhere else."
  [x]
  (and (map? x)
       (seq x)
       (not (pose? x))
       (every? #(contains? pose-mapping-keys %) (keys x))
       (some (fn [[k v]]
               (and (contains? defaults/column-keys k)
                    (resolve/column-ref? v)))
             x)))

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
   - `(->pose pose)` -- already a pose; idempotent lift
   - `(->pose {:x :a :y :b})` -- a mapping becomes a leaf carrying it,
     with no data, to be completed by `pj/with-data` or by the pose it
     is placed in"
  ([x] (->pose x "pj/->pose"))
  ([x caller]
   (cond
     ;; Ahead of `pose?`, which a draft answers yes to by carrying
     ;; `:layers`; the draft then reached the pipeline as a pose.
     (draft? x) (validate-pose-input! caller x)
     (pose? x) (pose-kind x)
     (mapping-map? x)
     (let [d (:data x)
           mapping (dissoc x :data)]
       (prepare-pose (cond-> {:layers [] :mapping mapping}
                       d (assoc :data (coerce-dataset d)))))
     :else
     (do (validate-pose-input! caller x)
         (let [d (coerce-dataset x)]
           (prepare-pose (cond-> {:layers []} d (assoc :data d))))))))

(defn infer-mapping
  "Infer and attach a default mapping to a pose that carries data but
   no mapping yet -- the fresh leaf `pj/->pose` produces from raw
   input. Position and color are taken from the first 1-3 columns
   (1 column to `:x`, 2 columns to `:x` and `:y`, 3 columns add
   `:color`).

   A pose whose mapping already names an `:x` or `:y` column, that has
   a layer naming every column it draws, that is composite, or whose
   data has 4+ columns is returned unchanged, so the step is idempotent
   and safe to include anywhere in a pipeline. A layer naming one
   column keeps it, and the default supplies the rest. The draft runs
   the same step on every leaf, cells of a composite included. This is
   the step that lets raw data render a default: the shortcuts
   `pj/draft`, `pj/plan`, `pj/membrane`, `pj/plot` and `pj/save` apply
   it to what they are given. `pj/pose` and `pj/->pose` write no
   mapping.

   - `(-> data pj/->pose pj/infer-mapping)` -- lift, then default map
   - `(pj/infer-mapping built-pose)` -- no-op on an already-mapped pose"
  [fr]
  (if (pose? fr)
    (pose/infer-default-mapping fr)
    fr))

(declare retire-scales)

(defn- retire-scales-in-tree
  "A pose with the retired `:scales` option read as `:share-scales`
   wherever a pose map carries it in `:opts` -- a map written by hand
   does not pass through `pj/options`, where the option is otherwise
   read (`retire-scales`)."
  [pose]
  (cond-> pose
    (contains? (:opts pose) :scales) (update :opts #(retire-scales "A pose's :opts" %))
    (seq (:poses pose)) (update :poses #(mapv retire-scales-in-tree %))))

(defn pose->draft
  "Single-step transition: convert a pose into a draft. Dispatches on
   pose shape -- a leaf pose becomes a `LeafDraft` (a record carrying
   `:layers` -- a vector of one map per applicable layer with merged
   scope -- and `:opts` -- the pose-level options that flow into the
   plan stage); a composite pose, and a leaf with a facet over columns,
   which is drawn as one cell per column, becomes a `CompositeDraft` carrying
   per-leaf drafts (each contextualized with shared-scale domains and
   chrome-driven opt adjustments), the resolved chrome geometry, and
   the layout (path -> rect).

   - `(pose->draft (pj/lay-point data :x :y))`"
  [pose]
  (expect-type pose pose? "pose" "pj/pose->draft")
  (check-pose-shape! pose)
  (let [pose (-> pose
                 retire-scales-in-tree
                 pose/coerce-data-in-tree)
        pose (cond-> pose
               true pose/expand-facet-nodes
               true pose/collapse-regular-split)]
    (if (pose/composite? pose)
      (compositor/composite-pose->draft pose)
      (resolve/->LeafDraft (pose/leaf->draft pose) (or (:opts pose) {})))))

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

(defn- layer-accepted-options
  "The keys the mapping of a `layer-type-key` layer accepts: the universal
   ones plus that layer type's own `:accepts`, less anything
   it `:rejects`. An unregistered or missing layer type answers the
   universal keys alone.

   One rule, read by `build-layer` when it warns about a layer's mapping
   and by `validate-pose-shape` when it walks a built layer."
  [layer-type-key]
  (let [reg (when (keyword? layer-type-key)
              (layer-type/lookup layer-type-key))]
    (-> (set layer-type/universal-layer-options)
        (into (:accepts reg))
        (set/difference (set (:rejects reg))))))

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
      (defaults/renamed-to k)
      (str "Renamed to " (defaults/renamed-to k))

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
      "Layer settings belong in the mapping of a pj/lay-* call"

      (and lay? (seq elsewhere))
      (str "Layer settings of " elsewhere " belong in that layer's mapping"))))

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
     listing the accepted set.

   An option written under a name a release retired is rewritten to
   the name it carries now before any of this, when that rename is
   `:accept` -- see `defaults/renamed-options`. So the plot draws as
   it did and the writer is told what to edit, rather than meeting a
   picture the missing setting changed."
  [caller opts accepted]
  (if-not (and (map? opts) (seq opts))
    opts
    (let [[opts accepted-renames] (defaults/apply-renames opts)
          unknown (remove accepted (keys opts))
          strict-val (:strict (defaults/config))]
      (when-not (or (nil? strict-val) (boolean? strict-val))
        (throw (ex-info (str ":strict config value must be true or false, got: "
                             (pr-str strict-val) ". Truthy non-boolean values"
                             " (keywords, strings, numbers) were silently"
                             " treated as enabled in earlier releases; this"
                             " is now an error.")
                        {:value strict-val})))
      (doseq [[from to] accepted-renames]
        (let [msg (str caller ": " from " was renamed to " to
                       ", and is read as " to " for now. Write " to ".")]
          (if strict-val
            (throw (ex-info msg {:caller caller :renamed from :to to}))
            (println (str "Warning: " msg)))))
      (if (seq unknown)
        (let [;; `:xend` for `:x-end`, as ggplot2 spells it: a key that
              ;; differs from an accepted one only by hyphens,
              ;; underscores and case is named, so it need not be found
              ;; in the list below.
              squash #(str/lower-case (str/replace (name %) #"[-_]" ""))
              by-squash (group-by squash (filter keyword? accepted))
              near (for [k unknown
                         :when (keyword? k)
                         :let [m (first (get by-squash (squash k)))]
                         :when m]
                     (str "\n  Did you mean " m " for " k "?"))
              ;; A layer is named as its writer called it.
              named (if (str/starts-with? caller "lay-") (str "pj/" caller) caller)
              msg (str named " does not recognize " (vec unknown) "."
                       (apply str near)
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
    :grid-strip-labels :grid-axis-titles :overlay :where :facet})

(def ^:private pose-print-order
  "Key order used by pj/prepare-pose to make printed pose maps
   readable. Small declarative keys first; :data before :poses so
   each level's data stays visually bound to its own siblings rather
   than trailing past its children; :poses last since children can
   be heavy."
  [:opts :where :mapping :facet :overlay :share-scales :grid-strip-labels :grid-axis-titles :layout :layers :data :poses])

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
  "Warn about keys in a :mapping map that are neither in
   pose-mapping-keys nor in `also-known`. `context` (e.g.
   \"pj/pose\", \"pj/pose layer\") prefixes the message so the user
   can tell which mapping has the typo. Returns nil.

   A layer's `:mapping` holds its layer-type options beside its
   aesthetics -- `:bins`, `:bar-width`, `:jitter` -- so a layer passes
   the options its layer type accepts as `also-known`. Without them
   every option written on a layer reads as a typo."
  ([m context] (warn-unknown-mapping-keys m context nil))
  ([m context also-known]
   (let [known (into pose-mapping-keys also-known)
         unknown (remove known (keys m))]
     (when (seq unknown)
       (println (str "Warning: " context " :mapping has unexpected"
                     " key(s): " (vec unknown)
                     ". Known keys here: "
                     (vec (sort known))))))))

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
    (check-explicit-mappings context m)
    (check-position-mapping context m))
  (doseq [layer (:layers fr)]
    (when-let [lm (:mapping layer)]
      (warn-unknown-mapping-keys lm (str context " layer")
                                 (layer-accepted-options (:layer-type layer)))
      (check-column-ref-types (str context " layer") lm)
      (check-explicit-mappings (str context " layer") lm)
      (check-position-mapping (str context " layer") lm)))
  (doseq [sub (:poses fr)]
    (validate-pose-shape sub (str context " sub-pose")))
  fr)

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
    ;; A composite holding a composite used to be refused here.
    ;; `pose/compute-layout` recurses for `:horizontal` and `:vertical`
    ;; at any depth and the cells render correctly, so the refusal named
    ;; a limitation the renderer does not have -- and `pj/arrange` of
    ;; three plots builds exactly that shape, which made the advice the
    ;; message gave produce what it refused.
    (do (validate-pose-shape fr "pj/pose")
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

(defn- extend-mapping-of
  "Merge `incoming-mapping` into the pose's own `:mapping` -- the root's,
   for a composite. Writing the same mapping twice gives the same pose,
   and an empty mapping changes nothing."
  [fr incoming-mapping]
  (let [merged (merge (:mapping fr) incoming-mapping)]
    (cond-> fr
      (seq merged)    (assoc :mapping merged)
      (empty? merged) (dissoc :mapping))))

(defn- add-cells
  "One sub-pose per mapping in `ms`, for `multi-pair-pose`. Appended to
   a composite; on a leaf, the leaf's own position and the layers that
   name one become the first cell, and its aesthetics, other layers,
   data and options go to the composite's root. A single mapping on a
   leaf with no position is merged into it."
  [base ms]
  (let [cell (fn [m] {:mapping m :layers []})]
    (cond
      (pose/composite? base)
      (update base :poses (fnil into []) (map cell ms))

      (and (= 1 (count ms)) (not (leaf-has-position? base)))
      (extend-mapping-of base (first ms))

      :else
      (let [[root-layers panel-layers] (partition-layers-by-position (:layers base))
            leaf-pos   (position-mapping (:mapping base))
            leaf-aesth (aesthetic-mapping (:mapping base))
            own        (when (leaf-has-position? base)
                         [(cond-> {:layers panel-layers}
                            (seq leaf-pos) (assoc :mapping leaf-pos))])]
        (cond-> {:poses (into (vec own) (map cell ms))
                 :layout {:direction :matrix}}
          (:data base)      (assoc :data (:data base))
          (seq leaf-aesth)  (assoc :mapping leaf-aesth)
          (seq root-layers) (assoc :layers root-layers)
          (seq (:opts base)) (assoc :opts (:opts base)))))))

(defn- extend-pose
  "`(pj/pose existing-pose incoming-mapping)`: the mapping is merged into
   the pose's own -- the root's, on a composite. A new `:x` or `:y`
   replaces the one the pose had; `pj/pose` never adds a panel."
  [fr incoming-mapping]
  (extend-mapping-of fr incoming-mapping))

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
   stays flat (not a grid).

   The rows are indexed by y and their cells by x: one row per
   distinct y, each row running across the distinct x values. That is
   the way every scatterplot matrix is read -- a cell's x is named by
   the column it sits in, its y by the row -- and it is the same
   arrangement `pose/matrix-axes` gives the `:matrix` layout, which
   the non-rectangular case uses. The two must agree: the grid a
   reader sees is the one the tick suppression in `grid-composite`
   assumes, which keeps x ticks on the bottom row and y ticks in the
   leftmost column.

   The input order is unchanged -- `pj/cross` pairs x-major, and the
   rectangularity check above still reads them that way."
  [pairs]
  (let [pairs  (vec pairs)
        xs     (vec (distinct (map first pairs)))
        ys     (vec (distinct (map second pairs)))
        m      (count xs)
        n      (count ys)]
    (when (and (<= 2 m) (<= 2 n)
               (= (count pairs) (* m n))
               (= pairs (vec (for [x xs y ys] [x y]))))
      (mapv (fn [y] (mapv (fn [x] [x y]) xs)) ys))))

(defn- grid-composite
  "Build a 2D rows-of-cols composite from `base` (a leaf or composite)
   and a rectangular grid of [x-col y-col] pairs. Each cell becomes a
   leaf carrying only its position mapping; the base's :data,
   :mapping, :layers, and :opts move to the new composite's root so
   they inherit into every cell via resolve-tree. :share-scales is
   stamped as #{:x :y} so columns share x-axis domains and rows share
   y-axis domains -- SPLOM behavior.

   `rows` arrives from `pairs->rows` with one row per y column and one
   cell per x column, so a cell's x is named by the column it sits in
   and its y by the row. Everything below depends on that: the strip
   labels, and which cells keep their tick numbers.

   Each cell also carries :opts:
   - :suppress-legend on every cell -- one shared legend at composite
     level.
   - :suppress-x-label and :suppress-y-label on every cell -- the
     strip labels carry the axis-variable name; per-cell axis labels
     would duplicate them.
   - :suppress-x-ticks on every cell except the bottom row -- only
     the bottom row's tick numbers stay, since every cell in a column
     carries the same x and so the same tick scale, shared via
     :share-scales.
   - :suppress-y-ticks on every cell except the leftmost column --
     same reasoning across a row, whose cells share one y.

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
        ;; One formatter for every name a reader sees -- strips, axis
        ;; titles, legend titles: `sepal length`, not `sepal-length`.
        col->name defaults/fmt-name
        ;; Each column shares its x column; each row shares its y
        ;; column. Strip labels live at the composite root, not on
        ;; individual cells, so cell layout stays untouched. A column
        ;; header therefore names the x of the cells beneath it, and a
        ;; row header the y of the cells beside it.
        col-labels (when (seq rows)
                     (mapv (fn [[x _]] (col->name x)) (first rows)))
        row-labels (mapv (fn [row] (col->name (second (first row)))) rows)
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
                           :opts              (merge {:share-scales #{:x :y}
                                                      :align-panels true}
                                                     root-o)
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
        column?  (fn [c] (or (keyword? c) (string? c)))
        ;; One reading of an element, wherever it stands in the list: a
        ;; column is a panel of that column, a pair a panel of the two.
        ;; (Read by the first element alone, a pair after a column was a
        ;; series, and a column after a pair failed to destructure.)
        cell-of  (fn [item]
                   (cond
                     (column? item) {:x item}

                     (and (sequential? item) (= 2 (count item)) (every? column? item))
                     {:x (first item) :y (second item)}

                     :else
                     (throw (ex-info
                             (str "pj/arrange appends columns and pairs of columns"
                                  " to a composite pose, and " (pr-str item)
                                  " is neither. Write a column, as :a, or a pair"
                                  " of columns, as [:a :b].")
                             {:item item :cols-or-pairs cols-or-pairs}))))
        cells    (mapv cell-of items)]
    (if-let [rows (and (seq items) (every? sequential? items) (pairs->rows items))]
      ;; A rectangular grid of pairs.
      (grid-composite base rows)
      (prepare-pose (add-cells base cells)))))

(defn- extend-mapping [p m]
  (check-mapping-in-map-slot "pj/pose" m "its mapping map")
  (let [opts      (or (warn-and-strip-unknown-opts "pj/pose" m pose-mapping-keys)
                      {})
        data-over (:data opts)
        mapping   (dissoc opts :data)]
    (check-column-ref-types "pj/pose" mapping)
    (check-explicit-mappings "pj/pose" mapping)
    (check-position-mapping "pj/pose" mapping)
    (if (pose? p)
      (cond-> (prepare-pose (extend-pose p mapping))
        data-over (with-data data-over))
      (prepare-pose (pose-from-data (or data-over p) mapping)))))

(defn pose
  "Construct or extend a pose.

   **On raw data (first argument is not itself a pose):**

   - `(pj/pose)` -- empty leaf.
   - `(pj/pose data)` -- leaf with data and no mapping. On 1-3 column
     datasets the draft infers one (`:x`, then `:y`, then `:color`), so
     the pose renders without an explicit mapping call.
   - `(pj/pose data {:color :species})` -- leaf with aesthetic mapping.
   - `(pj/pose data :x-col)` -- leaf with `{:x :x-col}`.
   - `(pj/pose data :x-col {:color :c})` -- `:x` and a mapping.
   - `(pj/pose data :x-col :y-col)` -- leaf with `:x` and `:y`.
   - `(pj/pose data :x-col :y-col {:color :c})` -- `:x`, `:y` and a mapping.
   - `(pj/pose data [:a :b :c])` -- several columns where one column
     goes are a series, drawn on one panel with a colour for each
     column. Pairs of columns, `[[:a :b] [:c :d]]`, report an error:
     `pj/arrange` takes them as cells.

   **Threaded over an existing pose (first argument is a pose):**

   - `(pj/pose fr)` -- pass-through; lifts a literal map for notebook
     auto-render if it is not already tagged.
   - `(pj/pose fr :x-col :y-col)` -- merge `:x` and `:y` into the
     pose's mapping (the root's, on a composite), replacing the ones it
     had. Layers that name no columns of their own read the new ones.
     `pj/pose` does not add panels; for panels side by side, use
     `pj/arrange`.
   - `(pj/pose fr :x-col :y-col {:color :c})` -- same, with the
     aesthetic merged too.
   - `(pj/pose fr {:color :c})` -- aesthetic-only: merge into the
     mapping.
   - `(pj/pose fr {})` is `fr`, and writing the same mapping twice gives
     the same pose.
   - `(pj/pose fr {:data X :color :c})` -- extend mapping AND replace the
     top-level data with X.

   **On a hand-built pose-shaped map (1-arity, input has `:layers`,
   `:poses`, or a map-valued `:mapping`):** the map is validated and tagged with Kindly auto-render
   metadata, but its keys are not reordered and its `:data` is not
   coerced -- the typed shape is preserved verbatim. A composite is
   supported at any depth: a sub-pose that itself has `:poses` nests,
   which is the shape `pj/arrange` builds and the shape it accepts
   among its own inputs.

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
  ([pose-or-data]
   (->pose pose-or-data "pj/pose"))
  ([pose-or-data x-or-mapping]
   (when-not (pose? pose-or-data)
     (validate-template-data! "pj/pose" pose-or-data))
   (if (map? x-or-mapping)
     (extend-mapping pose-or-data x-or-mapping)
     (extend-mapping pose-or-data {:x x-or-mapping})))
  ([pose-or-data x y-or-mapping]
   (when-not (pose? pose-or-data)
     (validate-template-data! "pj/pose" pose-or-data))
   (if (map? y-or-mapping)
     ;; (pj/pose data x-col opts-map) -- univariate position plus opts
     (extend-mapping pose-or-data (merge y-or-mapping {:x x}))
     ;; else
     (extend-mapping pose-or-data {:x x :y y-or-mapping})))
  ([pose-or-data x y mapping]
   (when-not (pose? pose-or-data)
     (validate-template-data! "pj/pose" pose-or-data))
   (when-not (or (nil? mapping) (map? mapping))
     (throw (ex-info
             (str "pj/pose 4-arity expects a mapping as the last"
                  " argument, got " (pr-str (type mapping)) ": "
                  (pr-str mapping) ". Wrap aesthetic mappings in a map,"
                  " e.g. {:color :species}.")
             {:caller "pj/pose" :value mapping})))
   (check-mapping-in-map-slot "pj/pose" mapping "its mapping map")
   (extend-mapping pose-or-data (merge mapping {:x x :y y}))))

(defn cross-matrix
  "Creates a matrix of plots of cols vs cols.
   When no cols are provided, attempts to cross all columns.

   - `(pj/cross-matrix fr)` -- SPLOM N^2 panels for all columns.
   - `(pj/cross-matrix fr cols)` -- SPLOM N^2 panels for cols.
   - `(pj/cross-matrix fr cols {:color :c})` -- SPLOM plus aesthetic mapping.
   - `(pj/cross-matrix fr cols cols)` -- SPLOM NxM panels for cols1xcols2.
   - `(pj/cross-matrix fr cols1 cols2 {:color :c})` -- SPLOM NxM panels with mapping.

   The name `pj/cross-matrix` is provisional."
  ([pose-or-data]
   (let [p (->pose pose-or-data "pj/cross-matrix")
         cols (tc/column-names (:data p))]
     (cross-matrix p cols)))
  ([pose-or-data cols]
   (multi-pair-pose pose-or-data (cross cols cols)))
  ([pose-or-data cols cols-or-mapping]
   (if (map? cols-or-mapping)
     (-> (extend-mapping pose-or-data cols-or-mapping)
         (multi-pair-pose (cross cols cols)))
     (multi-pair-pose pose-or-data (cross cols cols-or-mapping))))
  ([pose-or-data xcols ycols mapping]
   (-> (extend-mapping pose-or-data mapping)
       (multi-pair-pose (cross xcols ycols)))))

(defn- column-refs-in-mapping
  "The keyword column references a mapping makes, in either spelling.

   `{:column :typo}` is a column reference written more explicitly than
   the plain `:typo`, and reading only the plain one let it past the
   attach-time check `pj/with-data`'s docstring promises, to fail at
   `pj/plan` with a different message. `{:from :typo}` is the same
   reference again, spelled the third way, and had the same hole.
   A `{:value ...}` names no column and is skipped here on purpose."
  [m]
  (keep #(let [v (get m %)
               v (if (and (map? v) (contains? v :value))
                   nil
                   (pose/mapping-source v))]
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
  "Throw a helpful error if a pose's or a layer's mapping contains a
   panel aesthetic, or one of the older `:facet-*` spellings of one.

   A facet divides every layer of the pose alike, so it is written on
   the pose and never in a sub-pose's or a layer's mapping. Both
   spellings are caught here so they report the same thing: `:col` in
   a layer's mapping used to be dropped with a warning naming no
   route, which is the silent-strip behaviour this check exists to
   prevent (user-report-2 Issue 5).

   The message says what is refused and why, and no longer says that
   faceting is plot-level: faceting is a mapping and obeys the scope
   rules, so a composite can be faceted and one cell can be faceted
   differently from its neighbour.
   `pose/report-panel-aesthetic-on-layer` refuses the same thing at
   draft time, for a pose built by hand, and the two messages are kept
   saying the same thing."
  [context m]
  (let [fk (select-keys m (into [:facet-col :facet-row :facet-x :facet-y]
                                defaults/panel-aesthetics))]
    (when (seq fk)
      (throw (ex-info (str "A panel aesthetic "
                           (str/join " / " (map name (keys fk)))
                           " was written in a " context "'s mapping. A facet"
                           " divides every layer of the pose, and a panel"
                           " aesthetic is read from a pose's mapping and not"
                           " from a layer's, so write it on the pose:"
                           " (pj/facet pose col), (pj/facet-grid pose col-col"
                           " row-col), or in the pose's mapping."
                           " On a composite, write it on the cell whose panels"
                           " it divides.")
                      fk)))))

(defn- layer-type-name
  "Human-readable name for a layer-type value in error messages."
  [layer-type]
  (cond
    (keyword? layer-type) (name layer-type)
    (and (map? layer-type) (keyword? (:layer-type layer-type)))
    (name (:layer-type layer-type))
    :else "*"))

(defn- check-new-panel-columns
  "Safety check fired when a `lay-*` names columns no panel draws, so the
   layer would take a panel of its own: LP2 on a leaf, where the leaf
   draws one more panel, or LP3 on a composite, where a new leaf is
   appended. Verify each non-nil column in `position-mapping` exists in
   the data that layer will draw from: its own `:data` if it has one,
   otherwise `fallback-data`, the pose's. A focused error here saves the
   user from a deep plan-stage 'column not found' on a column they
   likely typo'd."
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
                       " the layer would draw from. Available columns: "
                       (vec (sort col-names))
                       ". If you meant another dataset, pass `:data` on"
                       " this lay-* call.")
                {:caller (str "pj/lay-" (layer-type-name layer-type))
                 :key k :column col :available (sort col-names)}))))))

(defn- add-leaf-layer-to-composite
  "Walk the composite depth-first and append the layer to the last
   leaf whose effective :x/:y (after ancestor-merge) match
   `position-mapping`. On miss, append a fresh leaf at the root level
   (LP3) after firing the new-panel column-existence safety check."
  ([fr position-mapping layer]
   (add-leaf-layer-to-composite fr position-mapping layer false))
  ([fr position-mapping layer overlay?]
   (let [match-path (pose/last-matching-leaf-path fr position-mapping)
         ;; Under overlay a miss is not a new panel. The layer joins the
         ;; last leaf in reading order -- the one a reader would call
         ;; "the panel being built" -- carrying its own columns, which
         ;; the four-level merge in pose/leaf->draft honors over the
         ;; leaf's.
         path (or match-path (when overlay? (pose/last-leaf-path fr)))]
     ;; The layer keeps the columns it names on its own mapping, on a
     ;; matching cell and on a new one alike -- a cell's mapping is what
     ;; `pj/pose` writes, and `lay-*` never writes it.
     (let [layer (update layer :mapping #(merge position-mapping %))]
       (if (some? path)
         (update-in fr
                    (conj (pose/path->update-in-path path) :layers)
                    (fnil conj []) layer)
         (do
           (check-new-panel-columns (:layer-type layer)
                                    position-mapping layer (:data fr))
           ;; The new cell reads the root's data. Where the cells carry
           ;; their own and the root has none, it has nothing to read
           ;; and draws a panel reading "no data".
           (when-not (or (:data layer) (:data fr))
             (let [lay (str "pj/lay-" (layer-type-name (:layer-type layer)))
                   cols (keep #(pose/mapping-source (get position-mapping %)) [:x :y])
                   drawn (distinct (keep pose/leaf-place (pose/resolve-tree fr)))]
               (println (str "Note: " lay " names " (str/join " and " (map pr-str cols))
                             ", and no cell of this pose draws "
                             (if (next cols) "those columns" "that column")
                             ", so the layer is drawn on a cell of its own."
                             " That cell has no data: the cells carry their own,"
                             " and the pose they are arranged in has none."
                             " To draw the layer on a cell, name that cell's columns"
                             (when (seq drawn)
                               (str " (the cells draw "
                                    (str/join ", " (map #(str/join " / " (map pr-str (remove nil? %)))
                                                        drawn))
                                    ")"))
                             ". To keep the new cell, pass its data: (" lay
                             " pose " (str/join " " (map pr-str cols)) " {:data data})."))))
           (update fr :poses (fnil conj [])
                   {:layers [layer]})))))))

(defn- check-position-mapping
  "Throw a helpful error if `:x` or `:y` is a value that names no column
   and places no mark -- a vector, a boolean, a set.

   Nothing about *where* the mapping is written enters into it. A
   number is left to the ordinary convention wherever it appears: the
   layer's data is asked at draft time, a name it carries is that
   column, and anything else is a value the axis scales into a datum.
   So `{:x 0}` reads column 0 on a dataset that has one and places
   every mark at zero on a dataset that does not, whether it was
   written in `pj/pose`, in a `lay-*` positional argument, or in a
   layer's mapping.

   This gate used to refuse a bare number outside a layer's mapping,
   on the reasoning that it runs before the data is available and
   cannot tell a column name from a place. That reasoning does not
   hold: the source question is answered at draft time for every other
   mapping, and answering it here as well is what makes the rule one
   rule. `{:column 0}` and `{:value 0}` remain the way to say which
   reading is meant where a reader of the code could not tell.

   Where the explicit form may be written is a separate question, and
   the answer is `pj/pose`, a layer's mapping -- `{:x {:column 0}}`
   -- and any `lay-*` positional argument that is not the last one:
   `(pj/lay-point data {:column 0} :b)` and
   `(pj/lay-point data :a {:column :b} {:color :sp})` both read the map
   as a position. The **last** positional argument is the mapping
   whichever axis it stands in, so the form cannot be spelled there;
   the arity decides before this check is reached, and
   `check-mapping-in-map-slot` is what says so."
  [context opts]
  (doseq [k [:x :y]]
    (when-let [v (get opts k)]
      ;; Pairs of columns, or a cross, ask for a panel each -- what
      ;; `pj/arrange` and `pj/cross-matrix` draw.
      (when (and (sequential? v) (seq v) (every? sequential? v))
        (throw (ex-info (str context " was given pairs of columns, "
                             (pr-str (vec v)) ". A pose draws one panel."
                             " For a panel per pair, write (pj/arrange data "
                             (pr-str (vec (map vec v))) "); for every :x"
                             " column against every :y column, write"
                             " (pj/cross-matrix data xs ys).")
                        {:option k :value v})))
      ;; A series passes: several columns on a positional aesthetic are
      ;; read by name, and the draft pivots them -- on a layer for that
      ;; layer, on a pose for every layer below it, which is what scope
      ;; does for every other mapping.
      (when-not (or (series-mapping? v)
                    (keyword? v) (string? v)
                    (pose/explicit-mapping? v)
                    (resolve/literal-position? v))
        (throw (ex-info (str context " " k " must be a column reference or a"
                             " value to place a mark at, but got " (pr-str v)
                             ", which is neither. For one fixed " k ", add a"
                             " column to :data holding it, e.g. "
                             "`(tc/add-column data " k " (constantly "
                             (pr-str v) "))` and pass "
                             k " " (pr-str (keyword (name k))) ".")
                        {:option k :value v}))))))

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
                             (if (= k :offset-x) ":dx" ":dy") ".")
                        {:option k :value v})))))
  ;; A shift is one amount in the axis's own units, applied to the whole
  ;; layer. Given a column, the plan failed with a raw cast error naming
  ;; Keyword and Number and neither the option nor the layer.
  (doseq [k [:dx :dy]]
    (when-let [v (get opts k)]
      (when-not (number? v)
        (let [axis (if (= k :dx) ":x" ":y")]
          (throw (ex-info (str context " " k " must be a number in the units "
                               "of the " axis " axis, but got " (pr-str v)
                               ". It shifts the whole layer by one amount, so "
                               "it takes one value rather than a column. To "
                               "shift each row by its own amount, add the "
                               "shift to the " axis " column in the data and "
                               "map " axis " to the result.")
                          {:option k :value v}))))))
  ;; How wide a mark is drawn across its band is one number for the
  ;; layer, so a column has nothing to mean. Given one, the plan failed
  ;; either with a raw cast error naming Keyword and Number, or with a
  ;; schema report that named no option.
  (doseq [k [:bar-width :box-width :interval-thickness]]
    (when-let [v (get opts k)]
      (when-not (number? v)
        (throw (ex-info (str context " " k " must be a number, but got "
                             (pr-str v) ". It sets one width for the whole "
                             "layer, so it takes a number rather than a "
                             "column.")
                        {:option k :value v}))))))

(defn- several-columns-named
  "The columns a mapping value names, where it names more than one,
   and nil otherwise.

   Three spellings reach here and they say the same thing: a bare
   vector, `{:series [...]}`, and `{:column [...]}`. Reading only the
   first two let the written-out form past every check that exists for
   it -- `{:color {:column [:a :b]}}` drew one grey mark under a
   warning about a numeric colour, where `{:color [:a :b]}` was
   reported."
  [v]
  (or (:cols (series-mapping v))
      (let [source (pose/mapping-source v)]
        (when (and (sequential? source)
                   (seq source)
                   (every? resolve/column-ref? source))
          (vec source)))))

(defn- check-column-ref-types
  "Throw a helpful error if any aesthetic mapping carries a symbol --
   a common typo from omitting the colon on a keyword (`'x` instead
   of `:x`) that previously flowed into resolution and crashed deep
   in the pipeline. Nil is intentionally allowed: it cancels an
   inherited mapping at the call site (see core_test
   aesthetic-column-validation-test)."
  [context mapping]
  ;; Several columns where one encoding goes. A vector of column
  ;; references is a series request, which belongs on an axis; on an
  ;; appearance aesthetic there is nothing for the pivot to draw. Three
  ;; of these -- :color, :size and :alpha -- used to reach the plan and
  ;; report only that it did not conform to a schema, naming neither the
  ;; aesthetic nor the value.
  (doseq [[k v] mapping
          ;; `:x` and `:y` read a vector as a series, so it is expanded
          ;; before this. `:tooltip` takes hiccup, where a vector is
          ;; markup and its elements look like column references
          ;; without being any. Which aesthetics unite a vector into a
          ;; compound key is the registry's answer rather than a list
          ;; kept here, so adding one does not leave this behind.
          :let [several (several-columns-named v)]
          :when (and (contains? defaults/column-keys k)
                     (not (#{:x :y :tooltip} k))
                     (not (defaults/compound-key-aesthetics k))
                     several)]
    (throw (ex-info (str context " " k " was given several columns, "
                         (pr-str several)
                         ". Several columns where one goes"
                         " are read as several series, which belongs on :x"
                         " or :y -- the aesthetics that name what a mark is"
                         " drawn from. Written there, the key column the"
                         " pivot invents is mapped to " k " on its own.")
                    {:option k :value v :accepted [:x :y]})))
  (doseq [[k v] mapping
          ;; The same typo written out in full is the same typo, in any
          ;; of the three spellings, and reading only the plain one sent
          ;; `{:column 'sp}` and `{:from 'sp}` on to the column lookup,
          ;; where they got a missing-column message and none of this
          ;; help.
          :let [written (pose/mapping-source v)]
          :when (and (contains? defaults/column-keys k) (symbol? written))]
    (throw (ex-info (str context " " k " is a symbol (" (pr-str written)
                         "). A column reference must be a keyword or "
                         "string -- did you mean " (pr-str (keyword (name written)))
                         "?")
                    {:option k :value v}))))

(defn- check-mapping-in-map-slot
  "Throw when a whole map argument is one mapping written in full.

   A mapping written in full says which reading one aesthetic takes, so
   it belongs under an aesthetic key. Both of Plotje's map slots hold
   the aesthetics themselves: `pj/pose`'s map argument is the mapping
   map, and a map in a `lay-*` call's **last** positional argument is
   the layer's mapping, whichever axis that argument stands in. So
   `(pj/lay-point data :a {:column :b})` does not say `:y` -- the map
   is the layer's mapping, `:column` is not a key of one, and the layer came back
   carrying `:x` alone, warned about but drawn: a one-dimensional plot
   from a call that reads as a two-dimensional one.

   The middle argument of a longer `lay-*` call is a position and does
   take the form, which is why the trap is about the slot rather than
   the axis: `(pj/lay-point data :a {:column :b} {:color :sp})` works."
  [caller opts what]
  (when (and (map? opts) (pose/explicit-mapping? opts))
    (throw (ex-info (str caller " was given " (pr-str opts) " where " what
                         " goes, so there is no aesthetic for it to name."
                         " A mapping written in full says which reading one"
                         " aesthetic takes, so write it under one -- {:y "
                         (pr-str opts) "} or {:x " (pr-str opts) "} -- or"
                         " name the column on its own.")
                    {:caller caller :opts opts}))))

(defn- check-explicit-mappings
  "Run the explicit form's data-independent checks where the mapping is
   written, rather than at `pj/draft`.

   Which of the two readings a value has, and whether the map naming it
   is well formed, are decided by the form alone -- so an unknown key
   inside it is visible at the `pj/pose` or `lay-*` call. Left to the
   draft, the pose was built, threaded and composed before anything
   said the mapping was malformed. The checks that need the layer's
   data -- whether the column is there, whether the value is one the
   aesthetic can draw -- still wait for it.

   A map that names no source is reported here too. `{:color {:scale
   false}}` is not the explicit form, so it used to reach the column
   lookup whole and be reported as a column called `{:scale false}`."
  [context mapping]
  (doseq [[k v] mapping
          ;; A series written out names several columns rather than one
          ;; source, so it is not the explicit form and the message below
          ;; does not describe it. `check-position-mapping` reports it on
          ;; `:x` and `:y`, and the several-columns check above on every
          ;; other aesthetic -- both naming the series.
          :when (and (contains? defaults/column-keys k) (map? v)
                     (not (series-mapping? v)))]
    (if (pose/explicit-mapping? v)
      (pose/check-explicit-mapping! k v)
      (throw (ex-info (str context " " k " " (pr-str v) " names no source."
                           " A mapping written in full says which of the two"
                           " readings it means, with :column or :value;"
                           " :scale says only which side of the scale to read"
                           " that source through.")
                      {:option k :value v})))))

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
  "The layer settings written beside `:layer-type` rather than in the
   layer's `:mapping`. `:mark`, `:stat` and `:position` each replace one
   part of the layer type, so they sit next to it. Every other option --
   aesthetics, stat parameters such as `:bins`, drawing options such as
   `:jitter` -- goes in `:mapping` (see `build-layer`)."
  #{:stat :position :mark})

(defn- retired-overlay!
  "Report `:overlay`, or `pj/overlay`, written by `caller` with value `v`.

   The layers of a pose share one panel, so asking for that is ignored
   with a warning -- the picture is the one asked for. Asking for the
   opposite, `false`, cannot be done by a flag: separate panels come
   from a split or from `pj/arrange`, and the error names both."
  [caller v]
  (if v
    (println (str "Warning: " caller " -- :overlay is retired and ignored. The"
                  " layers of a pose share one panel without it."))
    (throw (ex-info (str caller " -- :overlay is retired. The layers of a pose"
                         " share one panel. To draw layers on separate panels,"
                         " split the pose over the columns, as in"
                         " (pj/facet pose {:y [:a :b]}), or give each layer a"
                         " pose of its own and arrange the poses with pj/arrange.")
                    {:caller caller :overlay v}))))

(defn- lay-caller-name
  "How a message names the call that wrote a layer: `lay-point` for a
   registered keyword, `pj/lay` for a layer type written as a map, which
   has no name of its own."
  [layer-type-key]
  (if (keyword? layer-type-key)
    (str "lay-" (name layer-type-key))
    "pj/lay"))

(defn- build-layer
  "Build a layer map from a layer-type-key and optional opts.
   Extracts :data if present. Extracts :stat, :position, :mark as
   first-class sibling keys. Warns and strips unrecognized option keys.
   Rejects unknown :mark or :stat keywords (since every layer
   accepts both, a typo would silently fall through the accept-list).

   Everything else lands in :mapping -- which therefore holds more than
   mappings. Of the keys documented in
   `layer-type/layer-option-docs`, fourteen are aesthetics and the rest
   are drawing options (:jitter, :in, :font-size), stat parameters
   (:bandwidth, :bins) and the values a rule or a band is drawn at
   (:x-intercept). Which of
   them are aesthetics is answered by `defaults/aesthetic-registry`,
   not by the map they share."
  [layer-type-key opts]
  (when opts
    (check-facet-keys "layer" opts)
    (check-mapping-in-map-slot (lay-caller-name layer-type-key) opts "its mapping")
    (check-column-ref-types (lay-caller-name layer-type-key) opts)
    (check-explicit-mappings (lay-caller-name layer-type-key) opts)
    ;; :x and :y here may be a value as well as a column. Which of the
    ;; two it draws is decided in `pose/leaf->draft`, where the pose's
    ;; mapping and the layer's data are both known.
    (check-position-mapping (lay-caller-name layer-type-key) opts)
    (check-numeric-aesthetics (lay-caller-name layer-type-key) opts)
    (validate-mark-stat (lay-caller-name layer-type-key) opts))
  (let [opts (if (and opts (keyword? layer-type-key))
               (warn-and-strip-unknown-opts
                (str "lay-" (name layer-type-key))
                opts
                (layer-accepted-options layer-type-key))
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
                       :registered registered}))))
  ;; A layer type written as a map is an entry: what `pj/layer-type-lookup`
  ;; answers with, alone or with options merged over it. Every entry
  ;; names its mark, so a map without one is not a layer type. It was
  ;; stored as one and its keys read as the layer's options, so a
  ;; mapping written in this slot by mistake drew a layer of the type
  ;; the data chose, with no message.
  (when (map? layer-type-key)
    (when-not (keyword? (:mark layer-type-key))
      (let [written (into {} layer-type-key)
            mapping? (and (seq written)
                          (every? pose-mapping-keys (keys written)))]
        (throw (ex-info (str "pj/lay got the map " (pr-str written)
                             " as its layer type, and a layer type written as a"
                             " map names its :mark, as (pj/layer-type-lookup :point)"
                             " does."
                             (if mapping?
                               (str " This map reads as a mapping. Name the layer type"
                                    " before it: (pj/lay pose :point " (pr-str written)
                                    "), or (pj/lay-point pose " (pr-str written) ").")
                               (str " Name a registered layer type by its keyword,"
                                    " as in (pj/lay pose :point), with any options"
                                    " in a map after it.")))
                        {:caller "pj/lay"
                         :layer-type layer-type-key}))))
    (validate-mark-stat "pj/lay" layer-type-key))
  (when-not (or (keyword? layer-type-key) (map? layer-type-key))
    (throw (ex-info (str "pj/lay got " (pr-str layer-type-key)
                         " as its layer type. A layer type is the keyword it is"
                         " registered under, as in (pj/lay pose :point), or the"
                         " entry (pj/layer-type-lookup :point) answers with."
                         (when (nil? layer-type-key)
                           (str " nil is what pj/layer-type-lookup answers for a"
                                " name that is not registered."))
                         " Registered layer types: "
                         (vec (sort (keys (layer-type/registered)))))
                    {:caller "pj/lay"
                     :layer-type layer-type-key}))))

(defn- lay-layer-type-key
  "The registry key for a `pj/lay` layer-type argument.

   Two spellings reach here: the keyword a layer type is registered
   under, and the entry `pj/layer-type-lookup` answers with, which is
   what the extensibility chapters pass. An entry carries no key of its
   own, so it is matched back to one by identity.

   Everything downstream reads the keyword -- the option messages name
   it, and the accepted-option list is looked up by it -- so an entry
   that arrived unresolved lost the unknown-option check and named
   itself in any warning it did print."
  [layer-type-key]
  (if (or (keyword? layer-type-key) (nil? layer-type-key))
    layer-type-key
    (or (some (fn [[k entry]] (when (= entry layer-type-key) k))
              (layer-type/registered))
        layer-type-key)))

(defn lay
  "Add a root-scope layer. The layer attaches to `:layers` and flows to
   every descendant leaf at plan time (composite) or renders on the
   single panel (leaf).

   The layer type is named either by its keyword or by the entry
   `pj/layer-type-lookup` answers with; both behave the same. An entry
   is a map naming its `:mark`; any other value reports an error."
  ([pose-or-data layer-type-key]
   (lay pose-or-data layer-type-key nil))
  ([pose-or-data layer-type-key mapping]
   (let [layer-type-key (lay-layer-type-key layer-type-key)]
     (validate-lay-layer-type-key layer-type-key)
     (let [layer (build-layer layer-type-key mapping)]
       (update (->pose pose-or-data "pj/lay") :layers (fnil conj []) layer)))))

(defn- x-only?
  "True if layer-type-key is registered as x-only (rejects :y column)."
  [layer-type-key]
  (:x-only (layer-type/lookup layer-type-key)))

(defn- identity-columns
  "The `:x` / `:y` entries of a layer's own mapping that name a column of
   `data` -- the part of a mapping that says which panel the layer
   belongs on.

   Identity reads the columns a layer names, and a layer names them
   either in a `lay-*` argument slot or in its mapping. Both reach
   here. A written value is not a column: it places a mark on the panel
   the layer is added to and asks for no panel of its own, so
   `(pj/lay-text pose {:x 7.5 :y 4.2 :text \"note\"})` annotates the
   panel rather than starting one. The layer's data answers which of the
   two a value is -- the same question `pose/leaf->draft` asks when it
   draws the layer. With no data to ask, a keyword or a string is taken
   for a column name, which is the reading the draft will give it."
  [mapping data]
  (let [col-names (when data (set (tc/column-names data)))]
    (into {}
          (filter (fn [[_ v]]
                    (let [src (pose/mapping-source v)]
                      (if col-names
                        (contains? col-names src)
                        (resolve/column-ref? src)))))
          (select-keys mapping [:x :y]))))

(defn- find-series
  "Where a series is written, as `[aesthetics source]`, or nil where
   none is. `aesthetics` is `[:x]`, `[:y]` or `[:x :y]` -- a series on
   each axis is read as pairs -- and `source` is `:call` for either of a
   `lay-*` call's map slots and `:pose` for the pose's own mapping.

   A series on the pose is read the way every other mapping written
   there is read: it reaches the layers below it. The call is searched
   first, so a layer naming its own series overrides the one it would
   inherit, which is the scope rule for every aesthetic."
  [caller fr position-mapping opts]
  (let [in (fn [m] (filterv #(series-mapping? (get m %)) pose/series-aesthetics))
        at-call (in (merge position-mapping opts))
        on-pose (in (:mapping fr))]
    ;; A series at the call and a series on the pose are two pivots of
    ;; one dataset: the pivot consumes the columns it reads, so the
    ;; second would name columns the first had already taken. The layer
    ;; cannot override the pose here the way another mapping would,
    ;; because what it would override has already reshaped the data.
    (when (and (seq at-call) (seq on-pose))
      (throw (ex-info (str caller " was given a series on " (pr-str (first at-call))
                           ", and the pose it is added to already reads one on "
                           (pr-str (first on-pose)) ". A series pivots the data"
                           " into one value column, and two pivots have no"
                           " shared shape. Read one of them: drop the series"
                           " from the " caller " call to use the pose's, or"
                           " build the layer on a pose of its own.")
                      {:caller caller
                       :at-call (first at-call)
                       :on-pose (first on-pose)})))
    (cond
      (seq at-call) [at-call :call]
      (seq on-pose) [on-pose :pose])))

(defn- check-series-call!
  "Check a `lay-*` call that reads several columns as several series,
   where the call alone can say what is wrong: a series written out
   with a key it does not take, a series at the call on a pose that
   already reads one, a series added to a composite, and -- where the
   call has the data -- what the pivot would refuse. The series
   itself stays on the layer's mapping, and the draft pivots it --
   see `pose/pivot-leaf-series`."
  [caller fr position-mapping opts]
  (when-let [[ks source] (find-series caller fr position-mapping opts)]
    (let [written-at (fn [k] (if (= :pose source)
                               (get (:mapping fr) k)
                               (or (get opts k) (get position-mapping k))))]
      (doseq [k ks] (check-series-keys! caller (written-at k)))
      (when (pose/composite? fr)
        (throw (ex-info (str caller " was given several columns to read as"
                             " series, on a composite pose. The pivot"
                             " reshapes the dataset they are read from, and"
                             " a composite's cells share one, so every cell"
                             " would be reshaped. Add the layer to a cell"
                             " before arranging the cells.")
                        {:caller caller :aesthetic (first ks)})))
      ;; With the data in hand, what the pivot would refuse is reported
      ;; here, naming the call; without it, the draft reports it.
      (when-let [d (or (:data opts) (:data fr))]
        (pose/check-series! caller d
                            (into {} (map (fn [k] [k (written-at k)])) ks))))))

(defn- lay-on-pose*
  "Append a layer to a pose following the DFS-last identity rule.

   The columns the call names are written on the layer's own
   :mapping, and never on a pose's: the pose's :mapping is what
   `pj/pose` writes.

   Composite + columns: the layer lands on the last leaf whose
   effective :x/:y match (via add-leaf-layer-to-composite), or a
   fresh sub-pose holding it is appended at the root (LP3).

   Leaf + columns: the layer is appended, and the leaf draws one panel
   per place its layers name, worked out at draft time -- the leaf
   stays a leaf.

   No columns (leaf or composite + aesthetic-only): append the bare /
   aesthetic layer to :layers; it is drawn on every panel, reading
   that panel's columns."
  [fr layer-type-key position-mapping opts]
  ;; The positional x/y of a lay-* call reach the pose's :mapping without
  ;; passing through build-layer, so they need the same column-reference
  ;; check pj/pose runs on the map it is handed.
  (check-position-mapping (str "lay-" (layer-type-name layer-type-key))
                          position-mapping)
  (when (contains? opts :overlay)
    (retired-overlay! (str "pj/lay-" (layer-type-name layer-type-key)) (:overlay opts)))
  (let [opts (dissoc opts :overlay)
        built (elide-empty-maps (build-layer layer-type-key opts))
        ;; A composite's cells are separate poses, so a layer added to
        ;; one has to be placed now; a hand-written `:overlay` on the
        ;; composite still says to join the last cell.
        overlay? (boolean (:overlay fr))
        bare-layer built
        ;; A position written in the final map is the layer's own
        ;; mapping and stays there, and it decides which panel the layer
        ;; lands on exactly as one written in an argument slot does.
        ;; Before this, the two spellings meant different things: a
        ;; second `:y` in the final map joined the panel silently, and
        ;; `{:overlay false}` could not undo it, because such a layer was
        ;; never a candidate to start a panel.
        position-mapping (merge position-mapping
                                (identity-columns (:mapping bare-layer)
                                                  (or (:data bare-layer)
                                                      (:data fr))))
]
    (cond
      (and (pose/composite? fr) (seq position-mapping))
      ;; The resolved flag rides onto the layer: it lands on a cell whose
      ;; own mapping it may disagree with, and the cell decides its
      ;; panels at draft time from what its layers carry.
      (add-leaf-layer-to-composite fr position-mapping
                                   (cond-> bare-layer
                                     overlay? (assoc :overlay true))
                                   overlay?)

      ;; A layer keeps the columns it names on its own mapping, and the
      ;; pose's mapping is left as it was: a later `pj/pose` retargets
      ;; the layers that leave their columns to the pose, and only those,
      ;; as scope says. Which panel the layer draws on is decided at
      ;; draft time, from every layer's columns read together.
      (seq position-mapping)
      (do (when (some (fn [k]
                        (let [pos-v (pose/mapping-source (get position-mapping k))
                              leaf-v (get (pose/leaf-columns fr) k)]
                          (and pos-v leaf-v (not= pos-v leaf-v))))
                      [:x :y])
            ;; The layer names columns the pose does not draw: say which
            ;; column is missing here rather than deep in the plan.
            (check-new-panel-columns layer-type-key position-mapping
                                     bare-layer (:data fr)))
          (update fr :layers (fnil conj [])
                  (elide-empty-maps
                   ;; The layer's own mapping wins: the final map may
                   ;; have written a value where the position names a
                   ;; column, and `{:x 2.0}` on a label is the place
                   ;; the label goes, not the panel's column.
                   (update bare-layer :mapping
                           #(merge position-mapping %)))))
      :else
      (update fr :layers (fnil conj []) bare-layer))))

(defn- lay-on-pose
  "`lay-on-pose*`, with a series checked first.

   Every `lay-*` arity funnels through here. Several columns written
   where one goes stay on the layer's mapping as a series; the draft
   pivots them into a key column and a value column."
  [fr layer-type-key position-mapping opts]
  (check-series-call! (str "pj/lay-" (layer-type-name layer-type-key))
                      fr position-mapping opts)
  (let [result (lay-on-pose* fr layer-type-key position-mapping opts)]
    ;; The call that makes a conflict with a series reports it: a second
    ;; series on the pose, or a layer naming a column the pivot consumes.
    (when (pose/leaf? result)
      (pose/check-series-leaf! result))
    result))

(declare arrange)

(defn- one-column-layer-given-two-message
  "The message for a layer type that draws from one column, given a
   column in each of the `:x` and `:y` slots of its `lay-*` call."
  [layer-type-key x y]
  (let [lay (str "pj/lay-" (layer-type-name layer-type-key))]
    (str lay " draws from one column, and was given " (pr-str x)
         " on :x and " (pr-str y) " on :y. Name one column; to draw the"
         " layer on its side, name the column on :y, as in (" lay
         " data {:y " (pr-str x) "}).")))

(defn- panel-per-column
  "A `lay-*` call given a lone vector of columns, `(pj/lay-histogram
   data [:a :b :c])`: for this release it draws a panel per column, as
   0.16 did, and warns that the next release reads the vector as a
   series on one panel. Returns the pose the layer is added to -- the
   cells `pj/arrange` builds from the columns."
  [layer-type-key fr cols]
  (let [lay (str "pj/lay-" (layer-type-name layer-type-key))]
    (println (str "Warning: " lay " was given a vector of columns, "
                  (pr-str (vec cols)) ", and drew a panel for each. In the"
                  " next release a vector of columns in a lay-* call is read"
                  " as a series, drawn on one panel. For a panel per column,"
                  " write (-> data (pj/arrange " (pr-str (vec cols)) ") " lay
                  "); for one panel, write (" lay " data {:x "
                  (pr-str (vec cols)) "}).")))
  (arrange fr cols))

(defn- lay-layer-type
  "Shared implementation for all lay-* functions.

   Raw data coerces to a fresh leaf pose. Poses (leaf or composite)
   pass through. All dispatches then route through lay-on-pose, which
   follows the DFS-last identity rule in pose_rules.clj.

   1-arity: append a bare layer. Its columns are the panel's, or the
            first one to three columns of the data, read at draft time.
   2-arity: a column -> a layer naming it on `:x`;
            a vector of columns -> a panel per column, with a warning;
            a vector of pairs -> an error;
            a map -> a layer with that mapping.
   3-arity: two columns -> a layer naming both;
            a column and a vector of columns -> a series;
            a column and a map -> a layer naming it on `:x`, with that
            mapping;
            a vector of columns and a map -> a panel per column, with a
            warning.
   4-arity: two columns and a map."
  ([layer-type-key pose-or-data]
   ;; A layer naming no column is given its columns later: the panel's
   ;; columns at elaboration, or the first one to three columns of the
   ;; data at the draft (`infer-mapping`). A layer left without a column
   ;; is reported at the draft, by `pose/check-layer-columns!`.
   (lay-on-pose (->pose pose-or-data (str "pj/lay-" (name layer-type-key)))
                layer-type-key nil nil))
  ([layer-type-key pose-or-data x-or-opts]
   (let [fr (->pose pose-or-data (str "pj/lay-" (name layer-type-key)))]
     (cond
       (map? x-or-opts)
       (do (check-mapping-in-map-slot
            (str "lay-" (name layer-type-key)) x-or-opts "its mapping")
           (lay-on-pose fr layer-type-key nil x-or-opts))

       ;; Sequential -> build a multi-panel composite via pj/pose, then
       ;; attach the layer at the root so it flows to every panel via
       ;; resolve-tree.
       (and (sequential? x-or-opts) (series-mapping? x-or-opts))
       (lay-on-pose (panel-per-column layer-type-key fr x-or-opts)
                    layer-type-key nil nil)

       (sequential? x-or-opts)
       (do (check-position-mapping (str "pj/lay-" (name layer-type-key)) {:x x-or-opts})
           (lay-on-pose (pose fr x-or-opts) layer-type-key nil nil))

       ;; Anything else in the x slot is a column reference. A value that
       ;; is not one reaches lay-on-pose and is named there; it used to be
       ;; dropped here in silence, giving a layer with no x at all.
       (some? x-or-opts)
       (lay-on-pose fr layer-type-key {:x x-or-opts} nil)

       :else
       (lay-on-pose fr layer-type-key nil nil))))
  ([layer-type-key pose-or-data x y-or-opts]
   (let [fr (->pose pose-or-data (str "pj/lay-" (name layer-type-key)))]
     ;; Checked before any reading of the y slot, so a series written
     ;; there is refused as a single column is.
     (when (and (x-only? layer-type-key) (some? y-or-opts) (not (map? y-or-opts)))
       (throw (ex-info (one-column-layer-given-two-message layer-type-key x y-or-opts)
                       {:layer-type layer-type-key :x x :y y-or-opts})))
     (cond
       ;; Several columns in one positional slot beside a single column
       ;; in the other: the several are read as several series of one
       ;; layer. Before the parallel-vector branch, which reads two
       ;; sequentials of the same length as paired panels.
       (and (series-mapping? y-or-opts) (not (sequential? x)))
       (lay-on-pose fr layer-type-key {:x x :y y-or-opts} nil)

       (and (series-mapping? x) (not (sequential? y-or-opts)) (some? y-or-opts)
            (not (map? y-or-opts)))
       (lay-on-pose fr layer-type-key {:x x :y y-or-opts} nil)

       ;; A series in each slot: read in pairs, as one layer. The same
       ;; reading `pj/pose` and a mapping map give the two vectors.
       (and (series-mapping? x) (series-mapping? y-or-opts))
       (lay-on-pose fr layer-type-key {:x x :y y-or-opts} nil)

       ;; Parallel vectors -> build a multi-panel composite via pj/pose
       ;; with paired x/y, then attach the bare layer at the root so it
       ;; flows to every panel.
       (and (sequential? x) (sequential? y-or-opts))
       (lay-on-pose (pose fr (mapv vector x y-or-opts))
                    layer-type-key nil nil)

       ;; Sequential + opts -> build a multi-panel composite via pj/pose,
       ;; then attach a layer with opts at the root.
       (and (sequential? x) (series-mapping? x) (map? y-or-opts))
       (lay-on-pose (panel-per-column layer-type-key fr x)
                    layer-type-key nil y-or-opts)

       (and (sequential? x) (map? y-or-opts))
       (do (check-position-mapping (str "pj/lay-" (name layer-type-key)) {:x x})
           (lay-on-pose (pose fr x) layer-type-key nil y-or-opts))

       (map? y-or-opts)
       (lay-on-pose fr layer-type-key {:x x} y-or-opts)

       ;; Anything else in the y slot is a column reference. A value that
       ;; is not one reaches lay-on-pose and is named there; it used to be
       ;; passed on as the mapping, so a scalar y failed as
       ;; "find not supported on type: java.lang.Long".
       (some? y-or-opts)
       (lay-on-pose fr layer-type-key {:x x :y y-or-opts} nil)

       :else
       (lay-on-pose fr layer-type-key {:x x} nil))))
  ([layer-type-key pose-or-data x y opts]
   (when (x-only? layer-type-key)
     (throw (ex-info (one-column-layer-given-two-message layer-type-key x y)
                     {:layer-type layer-type-key :x x :y y})))
   (when-not (or (nil? opts) (map? opts))
     (throw (ex-info (str "pj/lay-" (name layer-type-key)
                          " 4-arity expects a mapping as the last"
                          " argument, got " (pr-str (type opts)) ": "
                          (pr-str opts) ". Wrap aesthetic mappings in"
                          " a map, e.g. {:color :species}.")
                     {:caller (str "pj/lay-" (name layer-type-key))
                      :value opts})))
   (lay-on-pose (->pose pose-or-data (str "pj/lay-" (name layer-type-key))) layer-type-key {:x x :y y} opts)))

(defn lay-point
  "Add a `:point` (scatter) layer to a pose.

   With no columns, the layer reads the panel's columns; on a composite
   pose it is added at the root and drawn in every cell. With columns,
   the layer keeps them on its own mapping and is drawn on the pose's
   one panel. On a composite pose, a layer naming columns joins the last
   cell that draws those columns, or is appended as a new cell.

   - `(lay-point pose)` -- a layer reading the panel's columns.
   - `(lay-point pose {:color :species})` -- the same, with a mapping.
   - `(lay-point data :x :y)` -- the data becomes a pose, and the layer
     names its columns.
   - `(lay-point data :x :y {:color :c})` -- the same, with a mapping."
  ([pose-or-data] (lay-layer-type :point pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :point pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :point pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :point pose-or-data x y mapping)))

(defn- last-opts
  "Return the trailing mapping from a lay-* arg list (or nil)."
  [args]
  (let [last-arg (last args)]
    (when (map? last-arg) last-arg)))

(defn- positional-hint
  "If the user passed a non-map last arg (e.g. a bare number), suggest
   wrapping it in a map. args here are the trailing args after
   the pose -- so the bad shape is `(lay-rule-h fr 3)` not `(lay-rule-h fr)`."
  [args]
  (when (and (seq args) (not (map? (last args))))
    (str " Got " (pr-str (last args)) " as the last argument; did you forget"
         " to wrap it in a map?")))

(defn- assert-panel-columns!
  "A rule or a band takes its place from its mapping. The arguments
   written before that map name columns, and only pick the panel the
   rule or band is drawn on. A value there that is not a column of the
   data was read as a written value: it stretched the axis to reach it,
   with no warning, while the rule was drawn at its intercept."
  [layer-type-key pose-or-data cols]
  (let [data (try (:data (->pose pose-or-data)) (catch Exception _ nil))
        names (when data (set (tc/column-names (tc/dataset data))))
        place ({:rule-h "{:y-intercept ...}" :rule-v "{:x-intercept ...}"
                :band-h "{:y-min ... :y-max ...}" :band-v "{:x-min ... :x-max ...}"}
               layer-type-key)]
    (doseq [c cols
            :when (and (some? c) (not (resolve/column-ref? c))
                       names (not (contains? names c)))]
      (throw (ex-info (str "pj/lay-" (name layer-type-key) " was given " (pr-str c)
                           " before its mapping, and the data has no column "
                           (pr-str c) ". The arguments written there name"
                           " columns, which pick the panel the "
                           (if (#{:rule-h :rule-v} layer-type-key) "rule" "band")
                           " is drawn on. Its place is written in the options"
                           " map: " place ".")
                      {:caller (str "pj/lay-" (name layer-type-key)) :value c})))))

(def ^:private rule-position-key
  "Per-layer-type required position key for pj/lay-rule-*."
  {:rule-h :y-intercept :rule-v :x-intercept})

(def ^:private band-position-keys
  "Per-layer-type required [lo-key hi-key] for pj/lay-band-*."
  {:band-h [:y-min :y-max] :band-v [:x-min :x-max]})

(defn- value-argument
  "Read an option that takes a written value and nothing else -- the
   sibling of `column-argument`.

   A band's edge and a rule's intercept are read straight from the
   mapping by the mark that draws them, so there is no column for
   `{:column ...}` to name. A writer who has learned `{:value ...}` for
   mappings will reach for it here all the same, and until this it fell
   through to the finite-number check below, which answered that their
   perfectly-formed `{:value 1.5}` was not a number.

   `:x-min` and `:x-max` carry `:value? true` in the registry, so the
   written value is the only reading they have -- which made them the
   two aesthetics the notation could not reach."
  [caller k v]
  (cond
    (not (map? v)) v
    (= (set (keys v)) #{:value}) (:value v)
    :else
    (throw (ex-info (str caller " " k " takes a number or a date, and "
                         (pr-str v) " is not one. Write the value, or"
                         " {:value ...} on its own -- " k " is read straight"
                         " from the mapping here, so there is no column for"
                         " {:column ...} to name and no scale for :scale to"
                         " choose a side of.")
                    {:caller caller :option k :value v}))))

(defn- unwrap-written-bounds
  "Unwrap `{:value v}` on each of `ks` in a layer's mapping, so the checks and
   the marks below see the number the writer wrote."
  [caller ks opts]
  (if-not (map? opts)
    opts
    (reduce (fn [o k]
              (if (contains? o k)
                (assoc o k (value-argument caller k (get o k)))
                o))
            opts ks)))

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
          opts (unwrap-written-bounds (str "lay-" (name layer-type-key))
                                      [k] opts)
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
    (let [[lo-k hi-k] (band-position-keys layer-type-key)
          opts (unwrap-written-bounds (str "lay-" (name layer-type-key))
                                      [lo-k hi-k] opts)]
      (cond-> opts
        (temporal-intercept? (get opts lo-k)) (update lo-k coerce-intercept)
        (temporal-intercept? (get opts hi-k)) (update hi-k coerce-intercept)))))

(defn- assert-rule-opts! [layer-type-key args]
  (let [k (rule-position-key layer-type-key)
        opts (unwrap-written-bounds (str "lay-" (name layer-type-key))
                                    [k] (last-opts args))
        v (get opts k)]
    (when-not (or (and (number? v) (Double/isFinite (double v)))
                  (temporal-intercept? v))
      (throw (ex-info (str "pj/lay-" (name layer-type-key) " requires a finite numeric "
                           "or temporal " k " in its mapping. "
                           "Example: (pj/lay-" (name layer-type-key) " pose {" k " 3.0})"
                           " or (pj/lay-" (name layer-type-key) " pose {" k
                           " #inst \"2024-06-15\"})."
                           (positional-hint args))
                      {:layer-type layer-type-key :opts opts})))))

(defn- assert-band-opts! [layer-type-key args]
  (let [[lo-k hi-k] (band-position-keys layer-type-key)
        opts (unwrap-written-bounds (str "lay-" (name layer-type-key))
                                    [lo-k hi-k] (last-opts args))
        lo (get opts lo-k) hi (get opts hi-k)
        valid-bound? (fn [v]
                       (or (and (number? v) (Double/isFinite (double v)))
                           (temporal-intercept? v)))]
    (when-not (and (valid-bound? lo) (valid-bound? hi))
      (throw (ex-info (str "pj/lay-" (name layer-type-key) " requires finite numeric or temporal "
                           lo-k " and " hi-k " in its mapping. "
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
        (throw (ex-info (str "pj/lay-" (name layer-type-key) " requires " lo-k " <= " hi-k ", got " lo-k " " lo " " hi-k " " hi ". "
                             "Swap the arguments or check the source of the values.")
                        {:layer-type layer-type-key :opts opts})))
      ;; Equal bounds passed this guard and drew a rectangle of zero
      ;; thickness -- present in the SVG, invisible on the plot, and
      ;; reported by nothing. A band marks a span; a span of nothing is
      ;; a mistake in the values, and a line at that value is a rule.
      (when (== lo-num hi-num)
        (throw (ex-info (str "pj/lay-" (name layer-type-key) " requires " lo-k " < " hi-k ", got both "
                             lo ". A band with equal bounds covers nothing and draws nothing. "
                             "For a line at that value use (pj/lay-"
                             (if (= layer-type-key :band-h) "rule-h pose {:y-intercept " "rule-v pose {:x-intercept ")
                             lo "}).")
                        {:layer-type layer-type-key :opts opts}))))))

(defn- assert-rule-1-arity! [layer-type-key]
  (let [k (rule-position-key layer-type-key)]
    (throw (ex-info (str "pj/lay-" (name layer-type-key) " requires a mapping with " k ". "
                         "Example: (pj/lay-" (name layer-type-key) " pose {" k " 3.0}).")
                    {:layer-type layer-type-key}))))

(defn- assert-band-1-arity! [layer-type-key]
  (let [[lo-k hi-k] (band-position-keys layer-type-key)]
    (throw (ex-info (str "pj/lay-" (name layer-type-key) " requires a mapping with " lo-k " and " hi-k ". "
                         "Example: (pj/lay-" (name layer-type-key) " pose {" lo-k " 2.0 " hi-k " 4.0}).")
                    {:layer-type layer-type-key}))))

(defn lay-rule-h
  "Add `:rule-h` layer -- horizontal reference line at y = y-intercept.
   The value is written in the mapping rather than read from a column; `:y-intercept` is required.
   Accepts `:y-intercept` (numeric or temporal -- LocalDate, LocalDateTime,
   Instant, java.util.Date), `:color` (a written color), `:alpha` (the
   line's opacity), `:size` (its width) and `:stroke-dash`
   (`:dashed`/`:dotted`/`:solid` or a raw `[dash gap]` vector).
   Temporal values are converted internally to match the y-axis scale
   so a date-axis intercept needs no manual conversion.
   `{:in :drawing-area}` reads the intercept as drawing units from the
   top left of the panel background instead of as a data value.
   The rule is drawn in the order its layer was written, so one written
   before a scatter sits under its points and one written after sits
   over them. The 4-arity finds or creates a sub-pose with these x/y
   columns and attaches the rule there (only panels matching that leaf
   show it).

   - `(lay-rule-h pose {:y-intercept 3})` -- root-level, flows to every panel.
   - `(lay-rule-h pose :x :y {:y-intercept 3})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-rule-h pose {:y-intercept 3 :color \"red\"})` -- with override color.
   - `(lay-rule-h pose {:y-intercept (java.time.LocalDate/parse \"2024-01-01\")})`
     -- temporal intercept on a date axis."
  ([_pose-or-data] (assert-rule-1-arity! :rule-h))
  ([pose-or-data x-or-mapping] (assert-rule-opts! :rule-h [x-or-mapping]) (lay-layer-type :rule-h pose-or-data (coerce-rule-opts :rule-h x-or-mapping)))
  ([pose-or-data x y-or-mapping] (assert-rule-opts! :rule-h [y-or-mapping]) (assert-panel-columns! :rule-h pose-or-data [x]) (lay-layer-type :rule-h pose-or-data x (coerce-rule-opts :rule-h y-or-mapping)))
  ([pose-or-data x y mapping] (assert-rule-opts! :rule-h [mapping]) (assert-panel-columns! :rule-h pose-or-data [x y]) (lay-layer-type :rule-h pose-or-data x y (coerce-rule-opts :rule-h mapping))))

(defn lay-rule-v
  "Add `:rule-v` layer -- vertical reference line at x = x-intercept.
   The value is written in the mapping rather than read from a column; `:x-intercept` is required.
   Accepts `:x-intercept` (numeric or temporal -- LocalDate, LocalDateTime,
   Instant, java.util.Date), `:color` (a written color), `:alpha` (the
   line's opacity), `:size` (its width) and `:stroke-dash`
   (`:dashed`/`:dotted`/`:solid` or a raw `[dash gap]` vector).
   Temporal values are converted internally to match the x-axis scale
   so a date-axis intercept needs no manual conversion.
   `{:in :drawing-area}` reads the intercept as drawing units from the
   top left of the panel background instead of as a data value.
   The rule is drawn in the order its layer was written, so one written
   before a scatter sits under its points and one written after sits
   over them. The 4-arity finds or creates a sub-pose with these x/y
   columns and attaches the rule there (only panels matching that leaf
   show it).

   - `(lay-rule-v pose {:x-intercept 5})` -- root-level, flows to every panel.
   - `(lay-rule-v pose :x :y {:x-intercept 5})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-rule-v pose {:x-intercept 5 :color \"red\"})` -- with override color.
   - `(lay-rule-v pose {:x-intercept #inst \"2008-09-15\"})` -- temporal
     intercept on a date axis."
  ([_pose-or-data] (assert-rule-1-arity! :rule-v))
  ([pose-or-data x-or-mapping] (assert-rule-opts! :rule-v [x-or-mapping]) (lay-layer-type :rule-v pose-or-data (coerce-rule-opts :rule-v x-or-mapping)))
  ([pose-or-data x y-or-mapping] (assert-rule-opts! :rule-v [y-or-mapping]) (assert-panel-columns! :rule-v pose-or-data [x]) (lay-layer-type :rule-v pose-or-data x (coerce-rule-opts :rule-v y-or-mapping)))
  ([pose-or-data x y mapping] (assert-rule-opts! :rule-v [mapping]) (assert-panel-columns! :rule-v pose-or-data [x y]) (lay-layer-type :rule-v pose-or-data x y (coerce-rule-opts :rule-v mapping))))

(defn lay-band-h
  "Add `:band-h` layer -- horizontal shaded band between y = y-min and y = y-max.
   The bounds are written in the mapping rather than read from a column; `:y-min` and `:y-max` are
   required and `:y-min` must be <= `:y-max`.
   Accepts `:y-min` (required), `:y-max` (required), `:color` (a
   written color) and `:alpha`. Bounds may be numeric or temporal
   (LocalDate, LocalDateTime, Instant, java.util.Date); temporal values
   are converted internally to match the y-axis scale.
   `{:in :drawing-area}` reads the bounds as drawing units from the top
   left of the panel background instead of as data values.
   The band is drawn in the order its layer was written, so one written
   before a scatter sits under its points. The 4-arity finds or creates
   a sub-pose with these x/y columns and attaches the band there (only
   panels matching that leaf show it).

   - `(lay-band-h pose {:y-min 2 :y-max 4})` -- root-level, flows to every panel.
   - `(lay-band-h pose :x :y {:y-min 2 :y-max 4})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-band-h pose {:y-min 2 :y-max 4 :color \"blue\" :alpha 0.3})`
     -- with color and opacity overrides."
  ([_pose-or-data] (assert-band-1-arity! :band-h))
  ([pose-or-data x-or-mapping] (assert-band-opts! :band-h [x-or-mapping]) (lay-layer-type :band-h pose-or-data (coerce-band-opts :band-h x-or-mapping)))
  ([pose-or-data x y-or-mapping] (assert-band-opts! :band-h [y-or-mapping]) (assert-panel-columns! :band-h pose-or-data [x]) (lay-layer-type :band-h pose-or-data x (coerce-band-opts :band-h y-or-mapping)))
  ([pose-or-data x y mapping] (assert-band-opts! :band-h [mapping]) (assert-panel-columns! :band-h pose-or-data [x y]) (lay-layer-type :band-h pose-or-data x y (coerce-band-opts :band-h mapping))))

(defn lay-band-v
  "Add `:band-v` layer -- vertical shaded band between x = x-min and x = x-max.
   The bounds are written in the mapping rather than read from a column; `:x-min` and `:x-max` are
   required and `:x-min` must be <= `:x-max`.
   Accepts `:x-min` (required), `:x-max` (required), `:color` (a
   written color) and `:alpha`. Bounds may be numeric or temporal
   (LocalDate, LocalDateTime, Instant, java.util.Date); temporal values
   are converted internally to match the x-axis scale.
   `{:in :drawing-area}` reads the bounds as drawing units from the top
   left of the panel background instead of as data values.
   The band is drawn in the order its layer was written, so one written
   before a scatter sits under its points. The 4-arity finds or creates
   a sub-pose with these x/y columns and attaches the band there (only
   panels matching that leaf show it).

   - `(lay-band-v pose {:x-min 4 :x-max 6})` -- root-level, flows to every panel.
   - `(lay-band-v pose :x :y {:x-min 4 :x-max 6})` -- panel-scope (columns pick
     or create a sub-pose).
   - `(lay-band-v pose {:x-min 4 :x-max 6 :color \"blue\" :alpha 0.3})`
     -- with color and opacity overrides."
  ([_pose-or-data] (assert-band-1-arity! :band-v))
  ([pose-or-data x-or-mapping] (assert-band-opts! :band-v [x-or-mapping]) (lay-layer-type :band-v pose-or-data (coerce-band-opts :band-v x-or-mapping)))
  ([pose-or-data x y-or-mapping] (assert-band-opts! :band-v [y-or-mapping]) (assert-panel-columns! :band-v pose-or-data [x]) (lay-layer-type :band-v pose-or-data x (coerce-band-opts :band-v y-or-mapping)))
  ([pose-or-data x y mapping] (assert-band-opts! :band-v [mapping]) (assert-panel-columns! :band-v pose-or-data [x y]) (lay-layer-type :band-v pose-or-data x y (coerce-band-opts :band-v mapping))))

(defn lay-line
  "Add `:line` layer type -- connected line through data points.
   Requires x (numerical) and y (numerical).
   Accepts `:color`, `:alpha`, `:size` (stroke width), `:stroke-dash`
   (`:dashed`/`:dotted`/`:solid` or a raw `[dash gap]` vector), `:dx`,
   `:dy`."
  ([pose-or-data] (lay-layer-type :line pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :line pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :line pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :line pose-or-data x y mapping)))

(defn lay-step
  "Add `:step` layer type -- staircase line (horizontal then vertical).
   Requires x and y (both numerical). Accepts `:stroke-dash`."
  ([pose-or-data] (lay-layer-type :step pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :step pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :step pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :step pose-or-data x y mapping)))

(defn lay-area
  "Add `:area` layer type -- filled region between y and the baseline.
   Requires x and y (both numerical). Accepts `:color` (fill), `:alpha`,
   and an opt-in outline on the top curve: `:stroke` (outline color),
   `:stroke-width`, `:stroke-dash`."
  ([pose-or-data] (lay-layer-type :area pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :area pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :area pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :area pose-or-data x y mapping)))

(defn lay-histogram
  "Add `:histogram` layer type -- bin numerical values into bars.
   Draws from one column: `(lay-histogram pose :hwy)` draws upright
   bars, and `(lay-histogram pose {:y :hwy})` draws the same histogram on
   its side. Accepts `:bins` (count), `:binwidth`, `:color`,
   `:normalize` (`:density` for density-normalized heights)."
  ([pose-or-data] (lay-layer-type :histogram pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :histogram pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :histogram pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :histogram pose-or-data x y mapping)))

(defn lay-bar
  "Add a `:bar` layer type.

   - One column: counts the rows of each category. Named on `:y`, as in
     `(pj/lay-bar pose {:y :class})`, the bars are drawn across. Two
     categorical columns report an error.
   - Two columns (x and y): uses the y value directly as the bar height.

   The stat is inferred from whether a y column is present, and overridable:
   pass `{:stat :count}` to count even with a y column, or `{:stat :identity}`
   to require an explicit height. `:color` (with `:position` `:dodge`/`:stack`)
   gives grouped or stacked bars.

   The categorical axis can be x (vertical bars) or y (horizontal bars):
   `(pj/lay-bar pose :value :category)` with a categorical y draws horizontal value
   bars, no `pj/coord` needed. (Stacked/filled horizontal bars are not yet
   supported directly -- put the category on x and add `(pj/coord :flip)`.)
   To read a numerical column as categories, write the type of the axis
   it is named on: `{:x-type :categorical}` for a column on `:x`,
   `{:y-type :categorical}` for a column on `:y`.

   When both axes are numeric or temporal (`(pj/lay-bar pose :x :y)` with no
   categorical axis), each bar sits at its x position with a width taken from
   `0.9` of the smallest gap between adjacent x values -- a time-series or
   numeric-position bar chart. Grouped numeric bars currently overlap rather
   than dodge.

   `:bar-width` sets how wide a bar is drawn, in the unit its axis offers. On
   a categorical axis it is the fraction of the category band the bar fills,
   `0.8` by default, as `:box-width` is for a box: `{:bar-width 0.4}` draws a
   bar half the usual thickness, which is how a second bar layer is read as an
   overlay rather than a stack. On a numeric or temporal axis, where there is
   no band, it is a width in data units."
  ([pose-or-data] (lay-layer-type :bar pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :bar pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :bar pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :bar pose-or-data x y mapping)))

(defn lay-smooth
  "Add `:smooth` layer type -- a smoothed trend line.
   Defaults to LOESS (local regression). Pass {`:stat` `:linear-model`} for
   ordinary least squares instead. Requires x and y (both numerical).
   Accepts {`:confidence-band` true} for a confidence ribbon, and
   `:stroke-dash`."
  ([pose-or-data] (lay-layer-type :smooth pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :smooth pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :smooth pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :smooth pose-or-data x y mapping)))

(defn lay-density
  "Add `:density` layer type -- kernel density estimate curve.
   Draws from one numerical column: `(lay-density pose :hwy)` draws the
   curve upright, and `(lay-density pose {:y :hwy})` draws it on its
   side. Accepts `:color` (fill),
   `:bandwidth`, and an opt-in outline on the curve: `:stroke` (outline
   color), `:stroke-width`, `:stroke-dash` (`:dashed`/`:dotted`/`:solid`
   or a raw `[dash gap]` vector)."
  ([pose-or-data] (lay-layer-type :density pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :density pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :density pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :density pose-or-data x y mapping)))

(defn lay-tile
  "Add `:tile` layer type -- colored grid cells (heatmap).
   With `:fill` or `:color`: one cell per row, colored from a column.
   With neither: auto-binned 2D histogram (stat `:bin2d`).

   A categorical `:color` column colors each cell from the palette,
   one color per category, as it colors any mark: `:color-values` and
   `:color-label` apply, and the fill settings, which shape a gradient,
   do not.

   A numeric `:color` column is read as the fill. Its cells and their
   legend read the fill settings: a `:fill` scale spec, then a `:color`
   one, then the `:fill-*` plot options, then the `:color-*` ones --
   `:fill-label` before `:color-label` for the legend title.

   Given both `:fill` and `:color`, the cells are painted from `:fill`
   and `:color` is not drawn, with a warning."
  ([pose-or-data] (lay-layer-type :tile pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :tile pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :tile pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :tile pose-or-data x y mapping)))

(defn lay-density-2d
  "Add `:density-2d` layer type -- 2D kernel density heatmap.
   Requires x and y (both numerical). Produces a smoothed density
   surface as colored tiles with a continuous gradient legend."
  ([pose-or-data] (lay-layer-type :density-2d pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :density-2d pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :density-2d pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :density-2d pose-or-data x y mapping)))

(defn lay-contour
  "Add `:contour` layer type -- iso-density contour lines from 2D KDE.
   Requires x and y (both numerical). Accepts {`:levels` 10} for
   the number of contour levels."
  ([pose-or-data] (lay-layer-type :contour pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :contour pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :contour pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :contour pose-or-data x y mapping)))

(defn lay-boxplot
  "Add `:boxplot` layer type -- box-and-whisker plot.
   Requires categorical x and numerical y. Shows median, quartiles,
   whiskers, and outliers. Accepts `:color` for grouped boxplots."
  ([pose-or-data] (lay-layer-type :boxplot pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :boxplot pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :boxplot pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :boxplot pose-or-data x y mapping)))

(defn lay-violin
  "Add `:violin` layer type -- mirrored density estimate by category.
   Requires categorical x and numerical y. Accepts `:color`, `:bandwidth`."
  ([pose-or-data] (lay-layer-type :violin pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :violin pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :violin pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :violin pose-or-data x y mapping)))

(defn lay-ridgeline
  "Add `:ridgeline` layer type -- stacked density curves by category.
   Requires categorical x and numerical y. Categories stack vertically
   with density curves rendered horizontally."
  ([pose-or-data] (lay-layer-type :ridgeline pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :ridgeline pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :ridgeline pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :ridgeline pose-or-data x y mapping)))

(defn lay-summary
  "Add `:summary` layer type -- mean +/- standard error per category.
   Requires categorical x and numerical y. Shows a point at the mean
   with error bars for +/- 1 SE. Accepts `:color` for grouped summaries."
  ([pose-or-data] (lay-layer-type :summary pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :summary pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :summary pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :summary pose-or-data x y mapping)))

(defn lay-errorbar
  "Add `:errorbar` layer type -- vertical error bars from pre-computed bounds.
   Requires x, y, and {`:y-min` `:col` `:y-max` `:col`} for lower/upper bounds."
  ([pose-or-data] (lay-layer-type :errorbar pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :errorbar pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :errorbar pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :errorbar pose-or-data x y mapping)))

(defn lay-lollipop
  "Add `:lollipop` layer type -- dot on a stem from the baseline.
   Requires categorical x and numerical y. Like a value bar but with
   a circle+line instead of a filled rectangle."
  ([pose-or-data] (lay-layer-type :lollipop pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :lollipop pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :lollipop pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :lollipop pose-or-data x y mapping)))

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
  ([pose-or-data x-or-mapping] (lay-layer-type :text pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :text pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :text pose-or-data x y mapping)))

(defn lay-label
  "Add `:label` layer type -- text labels on a background box at data
   coordinates, for readability over dense data.

   `:label` is the `:text` layer type with `{:box true}` preset, so it
   draws through the same mark and takes the same options, including
   `:align-x`/`:align-y` (defaults `:left`/`:center`); the box follows the
   anchored text. Pass `:box` to shape it -- `{:box {:corner-radius 0}}`
   for square corners, `{:box false}` for bare text."
  ([pose-or-data] (lay-layer-type :label pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :label pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :label pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :label pose-or-data x y mapping)))

(defn lay-rug
  "Add `:rug` layer type -- short tick marks along the axis showing individual values.
   Draws from one column: `(lay-rug pose :hwy)` marks the values along
   `:x`, and `(lay-rug pose {:y :hwy})` along `:y`. Often layered with
   density or scatter."
  ([pose-or-data] (lay-layer-type :rug pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :rug pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :rug pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :rug pose-or-data x y mapping)))

(defn lay-segment
  "Add `:segment` layer type -- a straight line from (x, y) to
   (x-end, y-end), one per row.

   `:x-end` and `:y-end` each take a column or a written value, and an
   end left out is the start's own value. So `{:y-end 0}` draws a stem
   from each point down to the zero line -- a stem plot, a lollipop
   without its dot -- and `{:x-end :x1 :y-end :y1}` draws each row's
   segment between two points. `:arrow` puts an arrow head on `:end`,
   `:start` or `:both` ends. A bare name is read as a column, as it is
   on `:x`, so a category is written as a value: `{:x-end {:value
   \"Q4\"}}`.

   - `(lay-segment data :index :distance {:y-end 0})` -- stems.
   - `(lay-segment data :x0 :y0 {:x-end :x1 :y-end :y1 :arrow :end})`
   - `(lay-segment data :quarter :sales {:x-end {:value \"Q4\"}})` --
     each row's segment runs to the Q4 category."
  ([pose-or-data] (lay-layer-type :segment pose-or-data))
  ([pose-or-data x-or-mapping] (lay-layer-type :segment pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :segment pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :segment pose-or-data x y mapping)))

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
  ([pose-or-data x-or-mapping] (lay-layer-type :interval-h pose-or-data x-or-mapping))
  ([pose-or-data x y-or-mapping] (lay-layer-type :interval-h pose-or-data x y-or-mapping))
  ([pose-or-data x y mapping] (lay-layer-type :interval-h pose-or-data x y mapping)))

(defn- wrap-options-list
  "Wrap a sorted seq of option keywords across lines so the rendered
   docstring stays readable. First line is prefixed with 'The mapping
   accepts: '; continuation lines align under the first key.

   Each keyword is wrapped in backticks, as every other code-like token
   in these docstrings is: `kind/doc` renders them as Markdown, so an
   unbacked keyword loses its monospace and reads as prose."
  [opts]
  (let [prefix "   The mapping accepts: "
        cont   "                        "
        max-w  78]
    (loop [[opt & more] opts
           lines []
           current prefix]
      (if (nil? opt)
        (str/join "\n" (conj lines (str current ".")))
        (let [token (str "`" opt "`")
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

(defn- retire-scales
  "`opts` with the retired `:scales` option read as the `:share-scales`
   value it stands for (`defaults/retired-scales`), and a warning naming
   that value -- an error under `:strict`. A `:share-scales` written
   beside it wins. `:scales` named the axes left free; `:share-scales`
   names the axes shared, on a facet and an arrangement alike."
  [caller opts]
  (if-not (and (map? opts) (contains? opts :scales))
    opts
    (let [v (:scales opts)
          to (get defaults/retired-scales v)]
      (when-not (contains? defaults/retired-scales v)
        (throw (ex-info (str caller " :scales is retired, and " (pr-str v)
                             " is not one of the values it took, "
                             (vec (sort (keys defaults/retired-scales)))
                             ". Write :share-scales with the set of axes the"
                             " panels share, as in {:share-scales #{:x}}.")
                        {:caller caller
                         :option :scales
                         :value v
                         :accepted (set (keys defaults/retired-scales))})))
      (let [msg (if to
                  (str caller ": :scales is retired. {:scales " v "} is read as"
                       " {:share-scales " (pr-str to) "} for now -- :share-scales"
                       " names the axes the panels share. Write {:share-scales "
                       (pr-str to) "}.")
                  (str caller ": :scales is retired. {:scales " v "} asked for"
                       " the default, so it is dropped. Remove it; to say which"
                       " axes the panels share, write :share-scales."))]
        (if (:strict (defaults/config))
          (throw (ex-info msg {:caller caller :renamed :scales :to :share-scales}))
          (println (str "Warning: " msg))))
      (cond-> (dissoc opts :scales)
        (and to (not (contains? opts :share-scales))) (assoc :share-scales to)))))

(defn options
  "Set plot-level options (title, labels, width, height, etc.).
   Nested maps (e.g. `:theme`) are deep-merged.
   `:width` and `:height` are coerced to long (rounded) so the plan carries
   integer dimensions through to render. On a composite pose
   the options attach to the root so every descendant leaf inherits
   them at plan time."
  [pose opts]
  (when-not (or (nil? opts) (map? opts))
    (throw (ex-info (str "pj/options expects a map of options as the second"
                         " argument, got " (pr-str (type opts)) ": "
                         (pr-str opts) ". Wrap plot-level options in"
                         " a map, e.g. {:title \"...\" :width 800}.")
                    {:caller "pj/options" :value opts})))
  (let [fr (->pose pose "pj/options")
        opts (retire-scales "pj/options" opts)
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
    (when (contains? opts :share-scales)
      (defaults/check-share-scales! "pj/options" (:share-scales opts)))
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

(defn overlay
  "Retired. The layers of a pose share one panel, so `(pj/overlay pose)`
   returns the pose unchanged and warns.

   `(pj/overlay pose false)` reports an error: separate panels come from
   a split over the columns, as in `(pj/facet pose {:y [:a :b]})`, or
   from separate poses given to `pj/arrange`."
  ([pose-or-data] (overlay pose-or-data true))
  ([pose-or-data on?]
   (retired-overlay! "pj/overlay" on?)
   (->pose pose-or-data "pj/overlay")))

(defn- update-mapping
  "Update the root :mapping of a pose. Non-pose inputs are coerced via
   ->pose first. resolve-tree merges a parent's :mapping into every
   descendant, child keys overriding parent keys, so a root-level write
   reaches every leaf and a cell can override it."
  [pose-or-data f & args]
  (apply update (if (pose? pose-or-data) pose-or-data (->pose pose-or-data))
         :mapping f args))

(defn- report-refacet
  "Report a second facet written in the same direction.

   Two `pj/facet` calls with the same direction used to leave only the
   second, with no word said. A panel aesthetic obeys the mapping scope
   rules, so overriding one lower down is a legitimate thing to do --
   but writing both at the same level is not a scope override, it is
   two answers to one question."
  ([pose aesthetic col] (report-refacet pose aesthetic col "pj/facet"))
  ([pose aesthetic col caller]
   (when-let [existing (get (:mapping pose) aesthetic)]
     (when (not= existing col)
       (throw (ex-info (str caller " was given " (pr-str col) " for " aesthetic
                            ", which this pose already facets by "
                            (pr-str existing) ". "
                            (if (nil? col)
                              ;; `nil` cancels a facet written on a pose
                              ;; above; there is none to cancel here.
                              (str "nil cancels the facet of a pose above this"
                                   " one, as in a cell of a faceted arrangement."
                                   " To draw this pose with no facet, leave out"
                                   " the earlier " caller " call.")
                              (let [columns (fn [v]
                                              (let [v (if (and (map? v) (contains? v :column))
                                                        (:column v)
                                                        v)]
                                                (cond (map? v) nil
                                                      (sequential? v) (vec v)
                                                      :else [v])))
                                    both (when (and (columns existing) (columns col))
                                           (vec (distinct (concat (columns existing)
                                                                  (columns col)))))]
                                (str "A pose divides its panels once"
                                     " per direction: use the other direction, or"
                                     " pj/facet-grid for both at once"
                                     ;; The compound key replaces the earlier
                                     ;; facet: written after it, it is a second
                                     ;; facet in the same direction and is
                                     ;; reported like this one.
                                     (when (and both (< 1 (count both)))
                                       (str ", or leave out the earlier facet and"
                                            " write one compound key in its place"
                                            " -- (pj/facet pose " (pr-str both) ")"))
                                     "."))))
                       {:caller caller
                        :aesthetic aesthetic
                        :existing existing
                        :given col}))))
   pose))

;; ---- pj/facet with a map (provisional, PR #61: the tutorial's API) ----

(defn- facet-categories?
  "True of a facet part that names categories: a column, several
   columns as a compound key, the written-out `{:column ..}`, or nil."
  [v]
  (or (nil? v)
      (and (map? v) (contains? v :column))
      (and (sequential? v) (every? #(not (or (map? %) (sequential? %))) v))
      (not (or (map? v) (sequential? v)))))

(defn- facet-node-columns
  "Every column a facet node names, nil margins left out."
  [node]
  (letfn [(part [v] (cond (nil? v) []
                          ;; `{:column [:a :b]}` is the compound key
                          ;; `[:a :b]` written in full: two columns.
                          (and (map? v) (contains? v :column))
                          (let [c (:column v)]
                            (if (sequential? c) (remove nil? c) [c]))
                          (map? v) (mapcat #(remove nil? (get v %)) [:x :y])
                          (sequential? v) (remove nil? v)
                          :else [v]))]
    (if (and (map? node) (or (contains? node :col) (contains? node :row)))
      (concat (part (:col node)) (part (:row node)))
      (part node))))

(defn- check-facet-node!
  "Report a column a facet node names that the pose's data does not
   have, at the call -- with the message the draw reports, so the two
   checks say one thing. A pose with a series is left to the draw: its
   pivot makes the key column a facet may name (`:series`)."
  [pose node role]
  (when-let [data (and (not-any? pose/series-mapping
                                 (concat (vals (:mapping pose))
                                         (mapcat (comp vals :mapping) (:layers pose))))
                       (:data pose))]
    (let [have (set (tc/column-names data))]
      (doseq [c (facet-node-columns node)
              :when (not (contains? have c))]
        (throw (ex-info (str "Column " c " (from " role ") not found in dataset. Available: "
                             (sort-by str have))
                        {:role role :column c :available (sort-by str have)}))))))

(defn- facet-direction
  "Write one panel aesthetic into a pose's mapping: lift the pose, read
   the column, report a second answer in the same direction, and write
   it.

   `pj/facet` is one of these steps and `pj/facet-grid` is two, and
   each used to carry its own copy of the three parts. Two copies of
   one rule diverge: the grid form went without the second-facet
   report, so it replaced whatever was already on `:col` or `:row`
   with nothing said.

   `caller` names the function the writer called, so a report still
   names it."
  [pose col direction caller]
  (let [fr  (->pose pose caller)
        col (column-argument caller col)]
    (check-facet-node! fr col direction)
    (report-refacet fr direction col caller)
    (update-mapping fr assoc direction col)))

(defn- check-facet-columns-map!
  "Report a `pj/facet` map of columns that cannot be drawn as written:
   one given to a composite, which keeps it nowhere; one given to a pose
   that already holds one, which would replace it; and one writing a
   vector of columns on one axis beside a single column on the other,
   which names no panel for the single column."
  [pose node]
  (when (seq (:poses pose))
    (throw (ex-info (str "pj/facet was given " (pr-str node) " on a composite"
                         " pose. A map of columns splits a pose with one panel."
                         " Facet each cell, as in (pj/arrange data [(pj/facet"
                         " cell " (pr-str node) ") ...]), or arrange the"
                         " columns as cells with pj/arrange.")
                    {:facet node})))
  (when-let [existing (:facet pose)]
    (when (not= existing node)
      (throw (ex-info (str "pj/facet was given " (pr-str node) ", and this pose"
                           " already facets over " (pr-str existing) ". A pose"
                           " splits its columns once: write both in one map,"
                           " as in {:col {:x [..]} :row {:y [..]}}.")
                      {:existing existing :given node}))))
  (when-not (or (contains? node :col) (contains? node :row))
    (let [vec-axes (filter #(sequential? (get node %)) [:x :y])
          one-axes (filter #(and (contains? node %) (not (sequential? (get node %)))) [:x :y])]
      (when (and (seq vec-axes) (seq one-axes))
        (let [one (first one-axes) many (first vec-axes)]
          (throw (ex-info (str "pj/facet was given " (pr-str node) ": a vector of"
                               " columns on " many " and one column on " one
                               ". A map of columns names a panel per column,"
                               " so write the single column on the pose: (-> pose"
                               " (pj/pose {" one " " (pr-str (get node one)) "})"
                               " (pj/facet {" many " " (pr-str (get node many)) "})).")
                          {:facet node})))))))

(defn- check-facet-map-keys!
  "Report a key a `pj/facet` map holds and nothing reads.

   The map says where the panels come from, in one of two shapes: a
   grid, under `:col` and `:row`, or columns, under `:x` and `:y`. A
   part of a grid is a column, a vector of columns, `{:column ..}`, or a
   map of columns under `:x` and `:y`. Any other key was kept or dropped
   with no message, so `{:col :drv :share-scales #{:x}}` drew shared
   scales and `{:col :drv :y [:hwy :cty]}` drew one row of panels.

   The message names where the key belongs where that is known: a plot
   option goes in the options map after this one, a column of a grid
   inside its `:col` or `:row`, and any other mapping to `pj/pose`."
  [node]
  (let [grid? (or (contains? node :col) (contains? node :row))
        accepted (cond grid? #{:col :row}
                       (some #{:x :y} (keys node)) #{:x :y}
                       :else #{:col :row :x :y})
        advice (fn [k]
                 (cond
                   (contains? plot-options-keys k)
                   (str k " is a plot option: write it in a map of its own after"
                        " this one, as in (pj/facet pose {...} {" k " ...}).")
                   (and grid? (#{:x :y} k))
                   (str "In a grid, the columns of " k " go inside :col or"
                        " :row, as in {:col ... :row {" k " [...]}}.")
                   (contains? pose-mapping-keys k)
                   (str k " is a mapping: write it with (pj/pose pose {" k
                        " ...}).")))
        report (fn [where m accepted]
                 (when-let [unknown (seq (remove accepted (keys m)))]
                   (throw (ex-info
                           (str "pj/facet was given " (pr-str node) ", and "
                                where " has unexpected key(s): " (vec unknown)
                                ". It takes " (vec (sort accepted)) "."
                                (when-let [a (some advice unknown)] (str " " a)))
                           {:caller "pj/facet" :facet node
                            :unknown (vec unknown)
                            :accepted (vec (sort accepted))}))))]
    ;; A map holding none of the four keys and no plot option is left
    ;; to `column-argument`, which reports a mapping value given where
    ;; a column goes, as `{:value :g}`.
    (when (or (some #{:col :row :x :y} (keys node))
              (some plot-options-keys (keys node)))
      (report "the map" node accepted)
      (when grid?
        (doseq [k [:col :row]
                :let [part (get node k)]
                :when (and (map? part) (not (contains? part :column)))]
          (report (str "its " k " part") part #{:x :y}))))))

(defn- facet-map
  "`pj/facet` given a map. Categories on `:col` and `:row` are written
   into the mapping, as `pj/facet-grid` writes them; a map holding
   columns -- `{:y [:hwy :cty]}`, pairs `{:x [..] :y [..]}`, or a grid
   whose `:col` or `:row` holds one -- is kept under the pose's `:facet`
   and expanded into cells when the pose is drawn. Provisional (PR #61)."
  [pose node]
  (let [fr (->pose pose "pj/facet")
        _ (check-facet-map-keys! node)
        grid? (or (contains? node :col) (contains? node :row))
        columns? (if grid?
                   (not (and (facet-categories? (:col node))
                             (facet-categories? (:row node))))
                   (some #(sequential? (get node %)) [:x :y]))]
    (cond
      (and grid? (not columns?))
      ;; A key written with `nil` is written into the mapping too: it
      ;; cancels the facet of a pose above, as `(pj/facet cell nil)`
      ;; does.
      (cond-> fr
        (contains? node :col) (facet-direction (:col node) :col "pj/facet")
        (contains? node :row) (facet-direction (:row node) :row "pj/facet"))

      columns?
      (do (check-facet-columns-map! fr node)
          (check-facet-node! fr node :facet)
          (prepare-pose (assoc fr :facet node)))

      ;; Any other map is read as a column argument, which reports it.
      :else
      (facet-direction fr node :col "pj/facet"))))

(defn- facet-with-direction
  "`pj/facet` given a column and a direction."
  [pose col direction]
  (when-not (#{:col :row} direction)
    (throw (ex-info (str "pj/facet direction must be :col or :row, got: "
                         (pr-str direction) ".")
                    {:caller "pj/facet"
                     :direction direction
                     :accepted #{:col :row}})))
  ;; A map without `:column` is the form that says where panels come
  ;; from, and it names the direction by its own keys. Read as a column
  ;; it was reported as "not a column of the data", which named neither
  ;; the direction nor the form that draws.
  (when (and (map? col) (not (contains? col :column)))
    (throw (ex-info (if (or (contains? col :col) (contains? col :row))
                      (str "pj/facet was given the map " (pr-str col)
                           " and the direction " direction
                           ". The map already names its directions: leave "
                           direction " out -- (pj/facet pose " (pr-str col) ").")
                      (str "pj/facet was given the map " (pr-str col)
                           " and the direction " direction
                           ". A map names the direction as its key: "
                           "(pj/facet pose " (pr-str {direction col}) ")."))
                    {:caller "pj/facet" :value col :direction direction})))
  (facet-direction pose col direction "pj/facet"))

(defn facet
  "Facet a pose by a column: one panel per value the column holds.

   `direction` is `:col` (default, panels across) or `:row` (panels
   down).

   Faceting is a mapping. `(pj/facet my-pose :species)` writes
   `{:col :species}` into the pose's mapping, so it follows the same
   scope rules as `:color` or `:size` -- written on a pose it reaches
   every layer and every sub-pose below, and a sub-pose that writes its
   own `:col` overrides it. This is what lets a composite be faceted,
   and lets one cell of a composite be faceted differently from
   another.

   `:col` and `:row` are aesthetics, so the mapping may be written out
   in full, and several columns under either unite into one compound
   key, as they do under `:group` -- `pj/compound-key-aesthetics` lists
   the three:

   - `(pj/facet my-pose :species)` -- one panel per species.
   - `(pj/facet my-pose [:part :dimension])` -- one panel per observed
     combination, which is a compound key. `pj/facet-grid` fills the
     rectangle instead.
   - `{:col {:column :species}}` -- the same as the first, written out.

   A map without `:column` says where panels come from, and may name
   columns rather than categories (provisional, PR #61):

   - `(pj/facet my-pose {:col :year :row :drv})` -- a grid of categories.
   - `(pj/facet my-pose {:y [:hwy :cty]})` -- a panel per column.
   - `(pj/facet my-pose {:x [:a :b] :y [:c :d]})` -- a panel per pair.
   - `(pj/facet my-pose {:col {:x [:a :b]} :row {:y [:a :b]}})` -- a
     matrix; `nil` in a vector of columns leaves that place for a
     margin.

   The columns are checked against the data when `pj/facet` is called.
   A layer naming one of the facet's columns is drawn only in the
   panels of that column. A map of columns is written once, on a pose
   with one panel: a second map, or a map on a composite pose, reports
   an error.

   `nil` in place of the column cancels the facet a pose above writes:
   `(pj/facet cell nil)` on one cell of a faceted arrangement draws that
   cell as one panel.

   A map of plot options may come last, and is what `pj/options` takes:
   `(pj/facet my-pose :species {:share-scales #{:x}})` is
   `(-> my-pose (pj/facet :species) (pj/options {:share-scales #{:x}}))`."
  ([pose col]
   (if (and (map? col) (not (contains? col :column)))
     (facet-map pose col)
     (facet pose col :col)))
  ([pose col direction-or-options]
   (if (map? direction-or-options)
     (options (facet pose col) direction-or-options)
     (facet-with-direction pose col direction-or-options)))
  ([pose col direction opts]
   (options (facet-with-direction pose col direction) opts)))

(defn facet-grid
  "Facet a pose by two columns: a panel for every row-and-column pair,
   whether or not the data holds one.

   Like `pj/facet`, this is a mapping -- it writes `{:col ... :row ...}`
   -- so it scopes downward and reaches a composite's cells.
   `(pj/facet-grid my-pose :a :b)` writes the same mapping as
   `(pj/facet my-pose :a)` followed by `(pj/facet my-pose :b :row)`,
   and is made of those two steps.

   `pj/facet-grid` fills the rectangle; `(pj/facet my-pose [:a :b])`
   draws only the combinations the data holds.

   A map of plot options may come last, as in `pj/facet`:
   `(pj/facet-grid my-pose :a :b {:share-scales #{:x}})`."
  ([pose col-col row-col]
   (-> pose
       (facet-direction col-col :col "pj/facet-grid")
       (facet-direction row-col :row "pj/facet-grid")))
  ([pose col-col row-col opts]
   (options (facet-grid pose col-col row-col) opts)))

(def ^:private aesthetic->scale-key
  "Aesthetic keyword to the opts key holding its scale spec. Derived
   from `defaults/aesthetic-registry`, so a new scalable aesthetic gets
   a scale by carrying a `:scale-key` there.

   `:group` has none on purpose. It used to map to a `:group-scale`
   that `pose/leaf->draft` never stamped onto a layer and nothing ever
   read, so `pj/scale :group` validated its argument and then changed
   nothing about the plot."
  defaults/channel->scale-key)

(def ^:private continuous-appearance-aesthetics
  "The continuous appearance aesthetics. These accept `:linear` and
   `:log` only -- `:categorical` does not apply to a continuous
   encoding."
  #{:size :alpha :fill :color})

(def ^:private discrete-appearance-aesthetics
  "The discrete appearance aesthetics. These accept `:categorical` only
   -- there is no continuous reading of a shape symbol."
  #{:shape})

(defn scale
  "Set scale on a pose. The scale applies to the pose it is called on
   and to everything below it: called on the pose you are building, it
   covers the whole plot; called on one cell before the cells are
   arranged, that cell alone, so two cells can carry different scales.
   Under faceting every panel comes from one pose, so the panels share
   a type; `{:share-scales #{}}` varies their domains.

   A mapping written out in full can name a scale too, for that one
   mapping: `{:size {:column :weight :scale :log}}`. Where a scale is
   set at more than one scope the settings accumulate and the inner
   scope wins, key by key. Written on the same pose, this call is the
   inner one; written on a layer, the layer's mapping is.

   Accepts a type keyword or a scale spec map. `:type` and `:domain`
   belong to every scale. The rest are per aesthetic, and a key an
   aesthetic does not read reports an error:

   - Every aesthetic takes `:label`, the title of whatever explains its
     scale to a reader: the axis for `:x` and `:y`, the legend for the
     rest. It wins over the `<aesthetic>-label` plot option -- `:x-label`,
     `:color-label` and their siblings -- which names the same thing one
     scope further out.
   - `:x` and `:y` take `:include`, a value the axis has to reach, or a
     collection of them. `{:include 0}` puts zero on the axis so that
     lengths drawn along it are proportional to the values, and the
     value lands exactly at the edge of the panel. `:include` extends
     the extent the data gives where `:domain` replaces it, so the two
     are not written together, and it is a set of values where a
     `:domain` is an ordered pair.
   - `:x` and `:y` take `:breaks` (explicit tick locations; `[]`
     draws the axis with no ticks, tick labels or grid lines, as
     ggplot2's `breaks = NULL` does, while `nil` leaves the default
     ticks),
     `:tick-labels` (custom tick text paired with `:breaks`),
     `:n-ticks` (about this many ticks) and `:tick-spacing` (about
     this much room in drawing units per tick). A numeric axis reads
     whichever of the last two is named; a categorical one is ticked at
     its categories, which `:n-ticks` thins.
   - `:size` and `:alpha` take `:range` -- what the aesthetic spans, in
     the quantity the mark draws it as, so `[2 8]` on `:size` is a
     radius in drawing units.
   - `:size` further takes `:by`, how a value spreads across that range
     (`:sqrt` by default, or `:linear` or `:area`), and `:from-zero`,
     which anchors both the domain and the range at zero so that twice
     the value is twice the ink.
   - `:shape` takes `:values`, the marker symbols to draw with.
   - `:color` and `:fill` take `:range`, the gradient a numeric column
     is read through; `:color` also takes `:values`, the colours a
     categorical column is drawn in. Both take `:midpoint`, the value
     the middle of the gradient is drawn at.

   A value outside `:domain` is drawn at the nearer end rather than
   dropped, so a narrower domain says what the reader should compare
   without leaving rows off the panel. On an axis a `:domain` is two
   finite numbers, or two dates where the column is temporal; against a
   categorical column it is the list of categories, in the order they
   are to be drawn.

   Aesthetics and accepted scale types:

   - The axis aesthetics (`:x`, `:y`) accept `:linear`, `:log`,
     `:categorical`.
   - The continuous appearance aesthetics (`:size`, `:alpha`, `:fill`,
     `:color`) accept `:linear` and `:log` only -- `:categorical` does
     not apply.
   - The discrete appearance aesthetic `:shape` accepts `:categorical`
     only -- `:linear` and `:log` do not apply to a discrete encoding.

   `:group` reports an error: it draws nothing of its own, so there is no
   scale to set. It splits a layer into one drawn group per value, and
   the order of those groups is the order of the data.

   To take an aesthetic off its scale for one mapping rather than
   choose a type for the whole plot, write that mapping out in full:
   `{:color {:column :hex :scale false}}` draws the column's values as
   they stand, and `{:size {:value 7 :scale true}}` sends a written
   value through the scale. See `pj/layer-option-docs` for `:color` and
   `:size`.

   A `:domain` has two readings, and the domain itself decides which:
   two numbers are a range, and anything else is a list of categories.

   On a categorical `:color` or `:shape` column it gives the
   order the categories are placed in, which the legend and the palette
   both follow. A category the list leaves out is still drawn, ordered
   after the ones listed, with a warning. On `:shape`, `:values`
   supplies the symbols to draw those categories with in that same
   order; `pj/shape-symbols` lists the ones available.

   On a numeric `:color` or `:fill` column it gives the two ends of the
   gradient, as it gives the ends of the drawn range on `:size` and
   `:alpha`. That is how every panel of a facet can be given one scale
   to share.

   `:tick-labels` requires `:breaks` and must match it in count. Use it
   to draw numeric breaks with text of your own -- for example, days of
   the week on a tile heatmap.

   `:n-ticks` asks for about that many ticks. On a categorical axis it
   thins a crowded one, which otherwise labels every category; on a
   numeric axis it replaces the count that `:tick-spacing` would give.
   `:tick-spacing` asks for about that much room, in drawing units,
   per tick, and the count follows from how many fit. It is a target
   in the way `:n-ticks` is: the ticks are still rounded to values a
   reader can read off, so the room each one ends up with can come out
   under the number asked for. It steers the choice of numeric ticks
   and does nothing on a categorical axis, which says so. The `:x-tick-spacing` / `:y-tick-spacing` plot options name the
   same setting one scope further out.

   - `(scale pose :x :log)` -- log scale on x-axis.
   - `(scale pose :x {:type :categorical :domain [...]})` -- explicit
     category order.
   - `(scale pose :x {:n-ticks 8})` -- about eight ticks, which on a
     crowded categorical axis thins it to eight of its categories.
   - `(scale pose :y {:type :linear :breaks [0 5 10]})` -- pin tick locations.
   - `(scale pose :x {:type :linear :breaks [1 2 3 4 5 6 7]
                      :tick-labels [\"Mon\" \"Tue\" \"Wed\" \"Thu\" \"Fri\" \"Sat\" \"Sun\"]})`
     -- numeric positions with custom tick text.
   - `(scale pose :y {:type :log :domain [1 1000]})` -- log scale with
     explicit range.
   - `(scale pose :size :log)` -- log-spaced point sizes.
   - `(scale pose :size {:range [3 14]})` -- wider points than the
     default 2 to 8.
   - `(scale pose :size {:by :area :from-zero true})` -- a point of
     twice the value covers twice the ink.
   - `(scale pose :fill :log)` -- log-spaced tile fill.
   - `(scale pose :shape {:type :categorical :domain [...]})` -- shape
     legend order.
   - `(scale pose :shape {:values [:cross :plus]})` -- pick the symbols."
  [pose aesthetic scale-type]
  (when (= :group aesthetic)
    (throw (ex-info (str "pj/scale has no :group aesthetic. Grouping draws"
                         " nothing of its own -- it splits a layer into one"
                         " drawn group per value, in the order the data"
                         " gives them -- so there is no scale to set. To"
                         " order or restyle what the reader sees, scale the"
                         " aesthetic that draws it: :color or :shape.")
                    {:aesthetic aesthetic
                     :supported (vec (sort (keys aesthetic->scale-key)))})))
  (let [k (or (aesthetic->scale-key aesthetic)
              (throw (ex-info (str "pj/scale takes one of the aesthetics "
                                   (vec (sort (keys aesthetic->scale-key)))
                                   ", got: " aesthetic)
                              {:aesthetic aesthetic})))
        cont-visual? (continuous-appearance-aesthetics aesthetic)
        disc-visual? (discrete-appearance-aesthetics aesthetic)
        valid-types (defaults/channel-scale-types aesthetic)
        type-kw (if (map? scale-type) (:type scale-type) scale-type)]
    (when-not (or (nil? type-kw) (valid-types type-kw))
      (throw (ex-info
              (cond
                ;; `:color` draws categories too; what it has no scale
                ;; type for is reading numbers as categories, which a
                ;; layer setting does.
                (and (= aesthetic :color) (= type-kw :categorical))
                (str "A :color scale has no :categorical type; its types are "
                     (vec (sort valid-types)) ". A column of categories is"
                     " coloured as categories already. To colour each"
                     " distinct number of a numeric column as a category of"
                     " its own, write :color-type :categorical on the layer,"
                     " for example (pj/lay-point data :x :y {:color :cyl"
                     " :color-type :categorical}).")

                (and cont-visual? (= type-kw :categorical))
                (str "The aesthetic " aesthetic " is continuous and does not"
                     " support a :categorical scale. Supported: "
                     (vec (sort valid-types)) ".")
                (and disc-visual? (#{:linear :log} type-kw))
                (str "The aesthetic " aesthetic " is discrete and does not"
                     " support a continuous scale (" type-kw "). Supported: "
                     (vec (sort valid-types)) ".")
                :else
                (str "Unknown scale type: " type-kw ". Supported for "
                     aesthetic ": " (vec (sort valid-types)) "."))
              {:aesthetic aesthetic :scale-type type-kw
               :supported (vec (sort valid-types))})))
    (when (map? scale-type)
      ;; The same three calls a mapping's `:scale` makes, in the same
      ;; order, so a spec means one thing wherever it is written.
      (scale/validate-spec-values! aesthetic scale-type "pj/scale")
      (scale/validate-drawn-range-options! aesthetic scale-type "pj/scale")
      (scale/validate-spec-keys! aesthetic scale-type "pj/scale"))
    ;; The spec is written as the caller stated it. A map that names no
    ;; :type does not mean the scale is linear -- it means this call had
    ;; no opinion about the type -- so filling one in here would make
    ;; (pj/scale :x {:breaks ...}) silently undo an earlier
    ;; (pj/scale :x :log). The default is applied once, after every
    ;; scope has accumulated, in pose/layer-scale-specs.
    (let [spec (if (map? scale-type)
                 scale-type
                 {:type scale-type})]
      (update (->pose pose "pj/scale") :mapping
              pose/put-scale aesthetic spec))))

(defn coord
  "Set coordinate transform on a pose. The coord applies to the pose it
   is called on and to everything below it, as a scale does: called on
   the pose you are building it covers every panel, and called on one
   cell before the cells are arranged, that cell alone. On a composite
   pose it attaches to the root, so every descendant leaf inherits it
   at plan time.

   Supported coord-types:

   - `:cartesian` -- standard x-right, y-up mapping (the default).
   - `:flip` -- swap x and y axes (horizontal bars / boxplots).
   - `:fixed` -- equal aspect ratio (1 data unit = 1 data unit).
   - `:polar` -- radial mapping: x to angle, y to radius.

   A pose that names a column on `:y` alone, under layers that draw from
   one column, is drawn on its side, and takes `:flip` or no coordinate
   system: `:cartesian`, `:polar` and `:fixed` report an error."
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
   default mapping first, as `pj/infer-mapping` gives one, so `(draft data)`
   works without an explicit `pj/pose` call.

   For a leaf pose, returns a `LeafDraft` record (`:layers` is a
   vector of flat maps, one per applicable layer with merged scope;
   `:opts` carries the pose-level options that flow into the plan
   stage). For a composite pose, and for a leaf with a facet over
   columns, which is drawn as one cell per column, returns a
   `CompositeDraft` carrying
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
  "True for a leaf pose carrying a mapping but no layers and no own
   :data."
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
  "Set of keys a single layer accepts: the universal keys union
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
          ;; A panel aesthetic is consumed at the draft stage rather
          ;; than by a layer -- it divides the leaf into panels and is
          ;; stripped before any layer sees the mapping -- so no layer
          ;; declares it and it is live wherever it is written.
          ;; A `:fill` entry holding only a scale spec -- what
          ;; `pj/scale :fill` writes -- is read by a 2D density, a 2D
          ;; histogram or a tile without any layer naming `:fill`, and
          ;; where nothing reads it `plan` warns with the fill-versus-
          ;; colour guidance. Warned here too, it printed twice, and
          ;; falsely on a 2D density.
          scale-only-fill? (fn [k] (and (= k :fill)
                                        (let [v (get (:mapping p) k)]
                                          (and (map? v) (not (:from v)) (not (:column v))))))
          unused (vec (remove (some-fn accepted defaults/panel-aesthetics scale-only-fill?)
                              (keys (:mapping p))))]
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
   default mapping first, as `pj/infer-mapping` gives one, so `(plan data)`
   works without an explicit `pj/pose` call.

   For a leaf pose, returns a `Plan` record, with one panel per category
   where the pose is faceted by a column. For a composite pose, and for
   a leaf with a facet over columns, which is drawn as one cell per
   column, returns a `CompositePlan` record
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
  "Where data values land on the canvas, for one panel of `pj/frames`.

   Takes a panel entry -- an element of `(:panels (frames plot))` -- and
   either one x and y, or a dataset of them with `:x` and `:y` columns.
   The dataset arity maps whole columns and builds the panel's scales
   once, so it is the one to reach for when placing many of them.

   - `(to-drawing panel 3.2 21.0)` returns `[x y]` in canvas coordinates
   - `(to-drawing panel {:x [3.2 4.0] :y [21.0 18.5]})` returns a dataset
     with the same two column names, now in canvas coordinates

   A dataset rather than a collection of pairs because the two
   coordinates of a point share one index space, which a dataset states
   and two loose sequences only promise. Anything `tc/dataset` coerces
   works.

   The result is in canvas coordinates, measured from the top left of the
   whole image. A `{:in :drawing-area}` layer measures from the drawing
   area's own corner instead, so drawing these coordinates back means
   subtracting that corner first.

   A pose, a plan or the whole frames map in the panel's place reports
   an error naming which of them it got and the call that reaches a
   panel entry from there.

   A categorical axis is a band scale, and it answers two kinds of
   value. A category sits at the middle of its band. A number is a
   place among the categories, counted from one: `1` is the first
   category, `1.5` sits halfway to the second, and on `n` categories
   the axis runs from `0.5` to `n + 0.5`. Anything else throws, naming
   the value and the categories it could have been -- a category the
   axis does not carry, or a number past the ends of the axis.
   Under `:coord :flip` the arguments stay in data order, even though a
   panel entry's `:x-domain` and `:y-domain` describe the drawn axes and
   the flip has already swapped those."
  ([panel x y] (frames-impl/to-drawing panel x y))
  ([panel data] (frames-impl/to-drawing panel data)))

(defn to-data
  "What data values the canvas coordinates name, for one panel of
   `pj/frames`. The direction an interaction reads: which value is
   under the pointer, which range a selection covers.

   Throws under a coordinate system with no inverse. `:polar` maps x and
   y together to an angle and a radius, so a canvas position there does
   not name one pair of data values. A panel entry reports which case it
   is in `:invertible?`.

   - `(to-data panel 412.0 88.5)` returns `[x y]` in data values
   - `(to-data panel {:x [412.0] :y [88.5]})` returns a dataset with the
     same two column names, now in data values

   A continuous axis answers with numbers, so its column is `:float64`,
   and a coordinate `pj/to-drawing` produced comes back as the value it
   was given. A categorical axis answers with the category whose band
   holds the coordinate, so its column holds those -- which is what an
   interaction wants, and is not the inverse of `pj/to-drawing` there:
   a place between two categories, such as `1.5`, comes back as one of
   them.

   A pose, a plan or the whole frames map in the panel's place reports
   an error naming which of them it got and the call that reaches a
   panel entry from there."
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
   `:color-values`, `:color-range`, `:color-midpoint`) ride along on the
   pose's `:opts` and reach `plan->membrane` through this call.

   Useful for exploring rendering targets beyond the SVG and Java2D
   backends Plotje wires in today: any Membrane backend can consume
   the result of `pj/membrane` via the standard Membrane protocols.

   Raw data (a dataset or a bare collection of values) is given a
   default mapping first, as `pj/infer-mapping` gives one, so
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
         fmt  (or (:format opts) :svg)
         plan (-> pose pose->draft draft->plan)]
     (-> plan
         (plan->membrane opts)
         (membrane->plot fmt opts)))`

   Plan-derived dimensions ride as record fields on the membrane
   (accessed via `membrane.ui/width`/`membrane.ui/height`); the
   title rides as `:plotje/title`. `membrane->plot` reads them from
   there.

   Raw data (a dataset or a bare collection of values) is given a
   default mapping first, as `pj/infer-mapping` gives one, so `(plot data)`
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
         fmt (or (:format opts) :svg)
         ;; Named rather than threaded because the interaction check
         ;; reads it: a layer-scoped :tooltip sets a flag on the plan,
         ;; not in opts, so the plan is what says a tooltip was asked
         ;; for. pj/plan->plot runs the same check from its own methods.
         pl (-> fr pose->draft draft->plan)]
     (render-impl/warn-interaction-ignored! pl fmt opts)
     (-> pl
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
   Returns a map of what the SVG draws -- `:width`, `:height`, `:panels`,
   `:points`, `:lines`, `:dashed-lines`, `:dash-patterns`, `:polygons`,
   `:tiles`, `:visible-tiles`, `:texts`, `:bold-texts`, `:italic-texts`,
   `:label-boxes` and `:clips`, and the sets `:sizes`, `:colors`,
   `:alphas` and `:shapes` of the values the marks are drawn with --
   useful for asserting plot structure.
   Accepts SVG hiccup, or anything `pj/plot` accepts, which is rendered
   to SVG first: a pose, or raw data, drawn as `pj/plot` draws it.
   Anything else is reported by `pj/plot`.

   - `(svg-summary (plot fr))` -- summary of rendered SVG.
   - `(svg-summary my-pose)` -- auto-renders pose (leaf or composite)."
  ([svg-or-pose]
   ;; SVG hiccup is a vector headed by a tag. Everything else goes
   ;; through `plot`: read as hiccup, raw data summarized to zeros with
   ;; no message, and a dataset failed on a cast.
   (if (and (vector? svg-or-pose) (keyword? (first svg-or-pose)))
     (svg/svg-summary svg-or-pose)
     (svg/svg-summary (plot svg-or-pose))))
  ([svg-or-pose theme]
   (if (and (vector? svg-or-pose) (keyword? (first svg-or-pose)))
     (svg/svg-summary svg-or-pose theme)
     (svg/svg-summary (plot svg-or-pose) theme))))

;; ---- Multi-Plot Composition ----

(defn- column-ref? [x] (or (keyword? x) (string? x)))

(defn- coerce-arrange-input
  "Turn one pj/arrange input into a pose-shaped plain map. Accepts a
   leaf pose, a composite pose, which nests, and `nil`, a cell left
   empty. Anything else throws with a message tailored to the actual
   type -- plain map, hiccup vector, plain vector -- so the user sees what went wrong
   without re-reading the same hiccup advice for every non-pose
   input."
  [p idx]
  (let [prefix (str "pj/arrange input at index " idx)]
    (cond
      (and (pose? p) (pose/leaf? p)) p

      ;; A composite cell nests: the row it lands in becomes a composite
      ;; holding a composite, which is what `pj/arrange` already builds
      ;; for more than one row.
      (and (pose? p) (pose/composite? p)) p

      ;; A mapping is a cell written at the detail a cell needs: which
      ;; columns this panel draws, and nothing about data or layers,
      ;; both of which come from the pose it is arranged into. Lifted
      ;; rather than special-cased, so `{:x :mpg :y :cyl}` here means
      ;; what it means everywhere else.
      (mapping-map? p) (->pose p "pj/arrange")

      ;; `nil` is a cell left empty: it keeps its place in the layout
      ;; and draws nothing, whatever data and layers the composite
      ;; carries (see `pose/resolve-tree`).
      (nil? p) (assoc (pose) :plotje/empty-cell true)

      ;; An empty vector holds no cell. `nil` is the cell left empty.
      (and (sequential? p) (empty? p))
      (throw (ex-info (str prefix " is an empty vector. A cell left empty is"
                           " written nil, as in (pj/arrange [a nil b]).")
                      {:index idx :type (type p)}))

      ;; Columns and nothing else: one, or three or more. A pair was
      ;; read as a cell before it came here (`shorthand-cell`), so this
      ;; is not rendered hiccup, whose head is a keyword too.
      (and (sequential? p) (every? column-ref? p))
      (throw (ex-info (if (= 2 (count p))
                        ;; With data first a pair is a cell by now.
                        (str prefix " is a pair of columns, " (pr-str (vec p))
                             ", and pj/arrange was given no data to read them"
                             " from. Give the data first: (pj/arrange data cells).")
                        (str prefix " is a vector of " (count p)
                             (if (= 1 (count p)) " column, " " columns, ") (pr-str (vec p))
                             ". A cell drawing one column is the column itself, "
                             (pr-str (first p)) ", and a cell drawing two is a pair,"
                             " as in [:a :b]."))
                      {:index idx :columns (vec p)}))

      (and (vector? p) (keyword? (first p)))
      (throw (ex-info (str prefix " looks like rendered hiccup (head: "
                           (pr-str (first p)) "). pj/arrange takes "
                           "poses, not pre-rendered output; pass "
                           "the pose itself, or build your own [:div ...] "
                           "if you want raw hiccup composition.")
                      {:index idx :head (first p)}))

      (vector? p)
      (throw (ex-info (str prefix " is a plain vector, and a cell is a pose"
                           " or nil. pj/arrange takes its cells in one flat"
                           " vector, (pj/arrange [a b c]) -- cells held in"
                           " several vectors go in with (into [a] more) -- or"
                           " as rows for an explicit grid, every element a"
                           " row: (pj/arrange [[a b] [c d]]).")
                      {:index idx :type (type p)}))

      (map? p)
      (throw (ex-info (str prefix " is a map but not a pose (no :layers "
                           "or :poses key). Build a pose first via "
                           "pj/pose / pj/lay-* and pass that.")
                      {:index idx :type (type p)}))

      :else
      (throw (ex-info (str prefix " must be a pose. Got: "
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

(declare arrange*)

;; Cell shorthand for `pj/arrange`: a column is a univariate panel, a
;; pair of columns is a bivariate one, and a pose followed by one or two
;; columns is that pose extended. Anything else is left as it is.

(defn- shorthand-cell
  "The pose-shaped cell a shorthand cell stands for, or nil when `c`
   is not a shorthand."
  [c]
  (cond
    (column-ref? c) {:x c}

    (and (sequential? c) (= 2 (count c)) (every? column-ref? c))
    {:x (first c) :y (second c)}

    (and (sequential? c) (<= 2 (count c) 3) (pose? (first c))
         (every? column-ref? (rest c)))
    (apply pose c)))

(defn- normalize-arrange-cells
  "Rewrite shorthand cells into poses and mappings. A sequential that
   is no shorthand is a row of cells when the list is already a list of
   rows, and otherwise a nested arrangement of its own."
  [cells]
  (let [cells (vec cells)
        rows? (and (seq cells)
                   (sequential? (first cells))
                   (nil? (shorthand-cell (first cells))))]
    (mapv (fn [c]
            (or (shorthand-cell c)
                (if (and (sequential? c) (seq c) (not (column-ref? (first c))))
                  (let [inner (normalize-arrange-cells c)]
                    (if rows? inner (arrange* inner {})))
                  c)))
          cells)))

(defn- check-arrange-arguments!
  "Report cells given one by one, `(pj/arrange a b)`, and an options
   argument that is no options map. The cell list is one sequential
   argument; a pose is a map, and read as a cell list its entries were
   reported as rendered hiccup."
  [cells opts]
  (let [describe (fn [x] (cond (pose? x) "a pose"
                               (nil? x) "nil"
                               (map? x) "a map"
                               :else (str "a " (.getSimpleName (class x)))))]
    (when-not (sequential? cells)
      (throw (ex-info (str "pj/arrange takes its cells in one vector, as in"
                           " (pj/arrange [a b]), and its options in a map"
                           " after it. It was given " (describe cells)
                           " where the vector of cells goes"
                           (when (pose? opts) ", and a pose after it")
                           ".")
                      {:caller "pj/arrange"})))
    (when (or (pose? opts) (not (or (nil? opts) (map? opts))))
      (throw (ex-info (str "pj/arrange takes its cells in one vector and its"
                           " options in a map after it, as in (pj/arrange"
                           " [a b] {:cols 1}). It was given " (describe opts)
                           " where the options map goes.")
                      {:caller "pj/arrange"})))))

(defn- arrange-from-pose
  "Arrange `cells` beside what the pose `p` already holds."
  [p cells opts]
  (cond
    (pose/composite? p)
    (do (when (seq opts)
          (throw (ex-info (str "pj/arrange options are not used when cells"
                               " are appended to a composite pose.")
                          {:caller "pj/arrange" :opts opts})))
        (multi-pair-pose p cells))

    :else
    (let [{:keys [data mapping layers]} p]
      (cond-> (arrange* (normalize-arrange-cells cells) opts)
        (some? data) (with-data data)
        (seq mapping) (assoc :mapping mapping)
        (seq layers) (assoc :layers layers)))))

(defn arrange
  "Arrange multiple poses in a grid. Returns a composite pose
   that renders through the compositor via membrane -- so `:svg`,
   `:bufimg`, and any other membrane target work uniformly.

   Each input is a pose, leaf or composite. A composite input becomes a
   cell holding its own grid. `nil` is a cell left empty: it keeps its
   place in the layout and draws nothing, whatever data and layers the
   composite carries. Pre-rendered hiccup is not accepted;
   build your own `[:div ...]` if you need to combine already-rendered
   values outside the library.

   Opts are the options below and any plot option `pj/options` takes:
   `(arrange cells {:x-label \"X\"})` is
   `(-> (arrange cells) (pj/options {:x-label \"X\"}))`, and the option
   reaches every cell that does not set it. A key that is neither is
   reported, as `pj/options` reports one.

   - `:cols` -- explicit column count (default: min(4, n-plots)).
   - `:title` -- centered title band above the grid.
   - `:subtitle` -- a line under the title; `:caption` -- a line under
     the grid, at the right.
   - `:width` -- total composite width.
   - `:height` -- total composite height.
   - `:share-scales` -- the axes the cells share. A set of axes,
     `#{:x :y}`, shares each among the cells drawing the same column on
     it; `:x` is the horizontal axis and `:y` the vertical one, also
     for a cell drawn with `:coord :flip`. A map says which columns go
     together on an axis:
     `{:y [[:hwy :cty]]}` shares `:y` between the cells drawing `:hwy`
     and `:cty`, `{:y :all}` between every cell, and `{:y :same-column}`
     is what the set says. A shared axis covers what the cells sharing
     it draw; a categorical one lists every category they hold. A count
     or density axis is shared among cells that count the same column,
     and a group or `:all` shares it between columns. By default, cells
     that split the data of the pose they are arranged into share each
     axis that draws one column, and other cells share nothing.
     `pj/options` takes the same option for the panels of a facet.
   - `:align-panels` -- give every cell the same drawing area, by
     reserving the widest y-label pad and legend column a cell needs on
     all of them (and, for a row, the tallest x-label pad). Two cells
     whose y axes label at different widths otherwise get different
     panel widths, so a shared x axis covers a different extent in
     each. Pair it with `:share-scales` where the cells are meant to be
     read against one another.

   - `(arrange [fr-a fr-b] {:cols 1 :share-scales #{:x} :align-panels true})`
     -- a column of cells whose x axes line up.
   - `(arrange [fr-a fr-b])` -- 1x2 row.
   - `(arrange [fr-a fr-b fr-c] {:cols 2 :width 900})` -- 2x2 grid (wraps).
   - `(arrange [[fr-a fr-b] [fr-c fr-d]])` -- explicit 2x2 grid.

   **Given data first**, the cells are drawn from it, so a cell need
   say no more than which columns its panel draws and the layers are
   added once at the root:

   - `(-> data (arrange [{:x :a :y :b} {:x :c :y :d}]) (lay-point))`
   - `(-> data (arrange cells {:cols 2}) (lay-point))`

   The arity is decided by the second argument: a sequential one is
   the cell list, so the first is data or a pose; a map is the options,
   so the first is the cells.

   A cell may also be written as columns:

   - `(arrange data [:a :b :c])` -- N univariate panels.
   - `(arrange data [[:a :b] [:c :d]])` -- N bivariate panels.
   - `(arrange pose [[:a :b] [:c :d]])` -- the pose's data, mapping and
     layers with N panels; on a composite pose, N panels appended.
   - `(arrange data [[pose1 :a] [:b :c] [[:e :f]]])` -- a pose extended
     by a column, a pair, and a nested arrangement, mixed freely."
  ([plots] (arrange plots {}))
  ([plots-or-data opts-or-cells]
   (if (sequential? opts-or-cells)
     (arrange plots-or-data opts-or-cells {})
     (do (check-arrange-arguments! plots-or-data opts-or-cells)
         (arrange* plots-or-data opts-or-cells))))
  ([data-or-pose cells opts]
   (if (pose? data-or-pose)
     (arrange-from-pose data-or-pose cells opts)
     (-> (arrange* (normalize-arrange-cells cells) opts)
         (with-data data-or-pose)))))

(def ^:private arrange-own-option-keys
  "The options `pj/arrange` takes that `pj/options` does not."
  #{:cols :align-panels})

(def ^:private arrange-written-option-keys
  "The options `arrange*` writes itself: its own, and the plot options
   it reads to build the composite. Any other plot option goes through
   `pj/options`."
  (into arrange-own-option-keys
        #{:title :subtitle :caption :width :height :share-scales}))

(defn- arrange*
  "Build the composite from a cell list and options. `pj/arrange` is
   the public face and decides which of its arguments is data."
  ([plots opts]
   (let [cfg (defaults/config)
         ;; The options map is `pj/arrange`'s own options and the plot
         ;; options `pj/options` takes, as the map after a facet is. A
         ;; key that is neither is reported the way `pj/options` reports
         ;; one -- a warning, an error under `:strict` -- and not
         ;; dropped with no message.
         opts (retire-scales "pj/arrange" opts)
         opts (warn-and-strip-unknown-opts "pj/arrange" opts
                                           (into plot-options-keys arrange-own-option-keys))
         {:keys [cols title subtitle caption share-scales align-panels]
          :or {share-scales #{}}} opts
         _ (defaults/check-share-scales! "pj/arrange" share-scales)
         nested? (and (sequential? plots)
                      (sequential? (first plots))
                      (not (keyword? (ffirst plots))))
         ;; A list of rows is rows throughout, and no row is empty. A
         ;; cell beside the rows was spliced into the cell list entry by
         ;; entry and reported as rendered hiccup; an empty first row
         ;; gave a column count of zero; an empty last row was drawn as
         ;; an empty band.
         _ (when nested?
             (doseq [[i row] (map-indexed vector plots)]
               (cond
                 (not (and (sequential? row) (not (map? row))))
                 (throw (ex-info (str "pj/arrange was given rows of cells -- its"
                                      " first element is a vector -- and element "
                                      i " is not a row. Write every row as a"
                                      " vector, (pj/arrange [[a b] [c d]]), or the"
                                      " cells in one flat vector, (pj/arrange"
                                      " [a b c d] {:cols 2}).")
                                 {:caller "pj/arrange" :index i}))
                 (empty? row)
                 (throw (ex-info (str "pj/arrange was given rows of cells, and row "
                                      i " is an empty vector. A row holds at least"
                                      " one cell; a cell left empty is written nil,"
                                      " as in (pj/arrange [[a b] [nil c]]).")
                                 {:caller "pj/arrange" :index i})))))
         rows-in (if nested? (vec plots) [(vec plots)])
         flat-plots (vec (apply concat rows-in))
         n-plots (count flat-plots)
         _ (when (zero? n-plots)
             (throw (ex-info "pj/arrange requires at least one cell." {:plots plots})))
         leaves (vec (map-indexed (fn [i p] (coerce-arrange-input p i)) flat-plots))
         _ (when (every? :plotje/empty-cell leaves)
             (throw (ex-info (str "pj/arrange was given " n-plots
                                  (if (= 1 n-plots) " cell" " cells")
                                  ", and every one is nil, a cell left empty,"
                                  " so there is nothing to draw.")
                             {:caller "pj/arrange" :plots plots})))
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
         ;; The size is written only where the caller gave one. The
         ;; configuration's is read when the composite is drawn
         ;; (`compositor/outer-dimensions`), as a leaf's is, so a
         ;; `pj/set-config!` after this call still applies.
         composite {:opts (cond-> {}
                            (:width opts) (assoc :width (long (Math/round (double (:width opts)))))
                            (:height opts) (assoc :height (long (Math/round (double (:height opts)))))
                            title (assoc :title title)
                            subtitle (assoc :subtitle subtitle)
                            caption (assoc :caption caption)
                            ;; Written, even empty: `#{}` turns off the
                            ;; sharing a composite has by default.
                            (contains? opts :share-scales) (assoc :share-scales (if (map? share-scales)
                                                                                 share-scales
                                                                                 (set share-scales)))
                            align-panels (assoc :align-panels true))
                    :layout {:direction :vertical}
                    :poses row-poses}]
     (cond-> (kind/fn composite
               {:kindly/f (render-composite defaults/*config*)})
       ;; Every other plot option is written on the arrangement, as
       ;; `pj/options` writes it, and reaches every cell that does not
       ;; set it.
       (seq (apply dissoc opts arrange-written-option-keys))
       (options (apply dissoc opts arrange-written-option-keys))))))

;; ---- Save ----

(defn- plan-panel-count
  "How many panels a plan draws: a leaf plan's own, or those of every
   cell of a composite plan."
  [the-plan]
  (if (resolve/composite-plan? the-plan)
    (reduce + 0 (map (comp plan-panel-count :plan) (:sub-plots the-plan)))
    (count (:panels the-plan))))

(defn- saveable-plan
  "The plan of `fr`, or an error where it would be written as a blank
   document. Catches the silent-blank-file case where an empty
   `(pj/pose)` reaches `pj/save`.

   A pose with no data anywhere -- on itself, a layer or a sub-pose --
   has nothing to plan. Any other pose is planned, and is empty where
   its plan draws no panel: the plan is what decides what is drawn. A
   test of the pose's shape (data on the pose, with a layer or a
   mapping) refused poses that draw -- a facet over columns with no
   mapping of its own, and a pose whose only data is its layer's."
  [caller fr]
  (let [fail (fn []
               (throw (ex-info
                       (str caller " was given an empty pose -- nothing to"
                            " render. Attach data and a layer first, e.g."
                            " (-> data (pj/lay-point :x :y))," " or arrange"
                            " several poses with pj/arrange.")
                       {:caller caller :pose fr})))
        _ (when-not (pose-has-data-anywhere? fr) (fail))
        the-plan (plan fr)]
    (when (zero? (plan-panel-count the-plan)) (fail))
    the-plan))

(def ^:private marginal-sides
  "Sides a marginal can be drawn on. `:bottom` and `:left` would put
   the distribution between the panel and its own axis, so they are
   not offered."
  #{:top :right})

(defn marginal
  "Add a marginal panel beside a leaf pose: a distribution of one of
   the pose's columns, drawn in a thin panel that shares the main
   panel's axis for that column and carries its own value scale.

   - `(marginal pose :top)` -- a density of the `:x` column, above.
   - `(marginal pose :right)` -- a density of the `:y` column, to the
     right, drawn on its side.
   - `(marginal pose :top :histogram)` -- a histogram instead.
   - `(marginal pose :right :density {:size 0.3})` -- a thicker panel.

   `:size` is the marginal's share of the height for `:top` and of the
   width for `:right`, `0.25` by default. The marginal's ticks and axis
   title along the shared axis are dropped, since the main panel's axis
   describes both, and the two drawing areas are aligned so a value
   sits at the same place in each.

   A `:right` marginal is drawn under `pj/coord :flip`, so its value
   axis runs across the panel and its baseline stands at the left edge,
   against the main panel.

   A second layer added to the pose is drawn on the pose's panel; a
   marginal is drawn beside that panel, on a value axis of its own."
  ([pose side] (marginal pose side :density {}))
  ([pose side layer-type] (marginal pose side layer-type {}))
  ([pose side layer-type opts]
   (when-not (marginal-sides side)
     (throw (ex-info (str "pj/marginal draws a marginal on :top or :right,"
                          " got " (pr-str side) ".")
                     {:caller "pj/marginal" :side side
                      :supported marginal-sides})))
   (when-not (pose/leaf? pose)
     (throw (ex-info (str "pj/marginal takes a leaf pose -- one panel to put"
                          " a marginal beside. For a composite, add the"
                          " marginal to a cell before arranging.")
                     {:caller "pj/marginal"})))
   (let [;; A marginal describes the column drawn along the axis it
         ;; sits against: the x column above the panel, the y column
         ;; beside it.
         axis (if (= :right side) :y :x)
         col (get (pose/leaf-columns pose) axis)
         _ (when-not col
             (throw (ex-info (str "pj/marginal needs the pose to name "
                                  (if (= :right side) "a :y" "an :x")
                                  " column, since that is the column a "
                                  (pr-str side) " marginal describes."
                                  ;; `pj/marginal` is the one pose
                                  ;; function that does not lift raw
                                  ;; data, because a marginal has no
                                  ;; column to describe until a mapping
                                  ;; names one. Say so rather than
                                  ;; reporting the missing axis alone.
                                  (when (and (empty? (:mapping pose))
                                             (empty? (pose/leaf-columns pose)))
                                    (str " This pose names no columns"
                                         " yet: name them with pj/pose"
                                         " or a pj/lay-* call first.")))
                             {:caller "pj/marginal"
                              :side side
                              :axis axis
                              :mapping (:mapping pose)})))
         ;; What the column is, before a composite is built around it.
         ;; The refusal below reaches the writer through `pj/marginal`,
         ;; which is where they asked for the marginal; without it the
         ;; message came from `:share-scales`, a setting `pj/marginal`
         ;; writes for the writer and which they cannot drop. A temporal
         ;; column needs no check: an axis holds dates as numbers, so a
         ;; distribution reads them and a shared axis pools them.
         {col-type :x-type} (resolve/infer-column-types (:data pose) {:x col})
         _ (when (= :categorical col-type)
             (throw (ex-info (str "pj/marginal draws a density or a histogram"
                                  " of " (pr-str col) ", and both read"
                                  " numbers; " (pr-str col) " is categorical."
                                  " To count the rows in each category"
                                  " instead, use pj/lay-bar.")
                             {:caller "pj/marginal"
                              :column col
                              :column-type col-type})))
         ;; The public lay-* function rather than a bare `(lay :histogram)`:
         ;; `lay` attaches a layer carrying no column of its own, and the
         ;; binning stat then reads an x that never arrives.
         lay-marginal (case layer-type
                        :density lay-density
                        :histogram lay-histogram
                        (throw (ex-info
                                (str "pj/marginal draws :density or :histogram,"
                                     " got " (pr-str layer-type) ".")
                                {:caller "pj/marginal"
                                 :layer-type layer-type
                                 :supported #{:density :histogram}})))
         size (double (or (:size opts) 0.25))
         right? (= :right side)
         ;; The ticks and title along the shared axis are the main
         ;; panel's to draw. These keys are read by the plan stage, and
         ;; `pj/options` rejects them by whitelist, so they are written
         ;; onto the marginal leaf's own :opts.
         suppress (if right?
                    {:suppress-y-ticks true :suppress-y-label true}
                    {:suppress-x-ticks true :suppress-x-label true})
         ;; A panel aesthetic says how the whole plot is divided, and
         ;; the marginal is part of that plot -- a faceted main panel
         ;; wants a marginal above each of its panels, not one above
         ;; the lot. Carried across explicitly because the marginal is
         ;; a fresh leaf rather than a descendant of `pose`, so the
         ;; mapping scope rules never reach it.
         panels-mapping (select-keys (:mapping pose) defaults/panel-aesthetics)
         ;; A faceted pose divides both cells alike, so the strip
         ;; labels would be drawn once per cell. Stacked, the two sets
         ;; stand in the same column and the lower one repeats the
         ;; upper; side by side they name different columns and both
         ;; are read, so only the stacked case drops a set. The upper
         ;; cell keeps them, which puts them at the top of the column
         ;; where a strip label belongs.
         main-pose (cond-> pose
                     (and (seq panels-mapping) (not right?))
                     (update :opts assoc :suppress-strip-labels true))
         marginal-leaf (cond-> (lay-marginal (:data pose) col)
                         ;; A density or histogram draws its column
                         ;; along x. On the right it has to run up the
                         ;; panel instead, which is what a flip does.
                         right? (coord :flip)
                         (seq panels-mapping) (update :mapping merge panels-mapping)
                         true   (update :opts merge suppress))
         ;; The marginal shares the axis it sits along: the vertical one
         ;; on the right, where it names the panel's `:y` column on its
         ;; own `:x` under the flip, and the horizontal one on top.
         shares (if right? #{:y} #{:x})]
     ;; Through prepare-pose, as every other composite-building path is
     ;; (`pj/arrange`, `pj/pose`'s promoting arities): it coerces :data
     ;; at every depth, puts the printed keys in the book's order, and
     ;; attaches the Kindly metadata that captures the current *config*
     ;; for render time. Returned raw, a marginal printed in a different
     ;; key order from its neighbours and lost a surrounding
     ;; `pj/with-config`.
     (prepare-pose
      {:poses (if right? [pose marginal-leaf] [marginal-leaf main-pose])
       :layout {:direction (if right? :horizontal :vertical)
                :weights (if right?
                           [(- 1.0 size) size]
                           [size (- 1.0 size)])}
       :share-scales shares
       :opts (merge (:opts pose) {:align-panels true})}))))

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
     values), which is given a default mapping first, as
     `pj/infer-mapping` gives one.
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
     (throw (ex-info (str "pj/save expects a map of options as the third"
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
         fr (if (seq opts) (options fr opts) fr)
         the-plan (saveable-plan "pj/save" fr)
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
       :svg (let [out (render-impl/plan->plot the-plan :svg (:opts fr {}))]
              (spit path (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                              (svg/hiccup->svg-str out))))
       ;; The renderer dispatches on :bufimg; the caller asked for a
       ;; PNG. The binding is what the interaction warning names, so it
       ;; reports the word that was written.
       :png (let [img (binding [render-impl/*format-asked* :png]
                        (render-impl/plan->plot the-plan :bufimg (:opts fr {})))]
              ((resolve 'scicloj.plotje.render.bufimg/save-png) img path))
       (throw (ex-info (str "pj/save cannot write format "
                            (pr-str resolved-fmt) " to a file. Supported: "
                            ":svg, :png.")
                       {:format resolved-fmt :path path-str})))
     ;; `path-str`, not `path`: this arity accepts a java.io.File as
     ;; well as a string, and `imeta-file` proxies java.io.File over a
     ;; String pathname. Handed the File itself it died on a cast,
     ;; after the bytes had already been written -- so the file on disk
     ;; was right and the call threw.
     (kind/image (pf/imeta-file path-str) {:class "plotje-plot"}))))
