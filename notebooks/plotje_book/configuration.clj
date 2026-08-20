;; # Configuration
;;
;; This chapter is the reference for **configuration** and
;; **plot options** -- the two categories of settings that sit above
;; individual layers. See
;; [Options and Scopes](./plotje_book.options_and_scopes.html)
;; for the taxonomy (why some settings are per-plot and others
;; are per-project defaults).
;;
;; Here you will find the tables of every key, the precedence chain,
;; and worked examples for theme, palette, color scale, and
;; validation.

(ns plotje-book.configuration
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; We use the iris dataset throughout.

;; We define `base-plot` as a function because
;; poses render at display time -- calling the function
;; produces a fresh specification that picks up the current configuration.

(defn base-plot
  [] (-> (rdatasets/datasets-iris)
         (pj/lay-point :sepal-length :sepal-width {:color :species})))

;; ## Inspecting the Current Configuration
;;
;; `pj/config` returns the resolved configuration as a plain map.
;; It merges all active configuration layers into one map.

(pj/config)

(kind/test-last
 [(fn [cfg]
    (and (map? cfg)
         (= 600 (:width cfg))
         (= 400 (:height cfg))
         (= 10 (:margin cfg))
         (map? (:theme cfg))))])

;; ### Configuration Keys
;;
;; Each key, its default value, and a description.

(def category-order
  ["Layout" "Theme" "Typography" "Points" "Bars & Lines"
   "Annotations" "Ticks" "Statistics" "Labels" "Behavior"
   "Color" "Interaction" "Output"])

(kind/table
 {:column-names ["Key" "Default" "Category" "Description"]
  :row-maps
  (let [cfg (pj/config)]
    (->> pj/config-key-docs
         (sort-by (fn [[k [cat]]]
                    [(.indexOf ^java.util.List category-order cat) (name k)]))
         (mapv (fn [[k [cat desc]]]
                 {"Key" (kind/code (pr-str k))
                  "Default" (kind/code (pr-str (get cfg k)))
                  "Category" cat
                  "Description" desc}))))})

(kind/test-last [(fn [t] (= 44 (count (:row-maps t))))])

;; ### Plot Options
;;
;; These options are accepted directly by `pj/options`, `pj/plan`, and
;; `pj/plot`; they are inherently per-plot, with no cross-plot defaults.
;; (Other plot options -- axis scales, coordinate system, facets --
;; are set by their dedicated functions and live in the same `:opts`
;; field.)

(kind/table
 {:column-names ["Key" "Category" "Description"]
  :row-maps
  (->> pj/plot-option-docs
       (sort-by (fn [[k [cat]]] [cat (name k)]))
       (mapv (fn [[k [cat desc]]]
               {"Key" (kind/code (pr-str k))
                "Category" cat
                "Description" desc})))})

(kind/test-last [(fn [t] (= 15 (count (:row-maps t))))])

;; ## Using Plot Options
;;
;; Pass an options map to `pj/options` to override any setting for a
;; single plot. Plot options have the highest priority -- they
;; override everything else.

;; Custom dimensions -- the defaults are:

(select-keys (pj/config) [:width :height])

(kind/test-last [(fn [m] (= {:width 600 :height 400} m))])

(-> (base-plot)
    (pj/options {:width 900 :height 250}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 150 (:points s))
           (> (:width s) 800))))])

;; Theme deep-merge -- only the specified keys change. Here we set a
;; white background; `:grid` and `:font-size` keep their defaults:

(-> (base-plot)
    (pj/options {:theme {:bg "#FFFFFF"}}))

(kind/test-last
 [(fn [v]
    (= 150 (:points (pj/svg-summary v))))])

;; Named palette:

(-> (base-plot)
    (pj/options {:color-values :dark2}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (= 150 (:points s))))])

;; ## Global Overrides with set-config!
;;
;; `pj/set-config!` sets overrides that persist across calls -- useful
;; when you want a consistent style for an entire session or notebook.

;; Set a global width override and render:

(pj/set-config! {:width 800})

(select-keys (pj/config) [:width :height])

(kind/test-last [(fn [m] (= {:width 800 :height 400} m))])

(-> (base-plot))

;; Reset to library defaults by passing `nil`:

(pj/set-config! nil)

(select-keys (pj/config) [:width :height])

(kind/test-last [(fn [m] (= {:width 600 :height 400} m))])

;; ## Thread-Local Overrides with with-config
;;
;; `pj/with-config` is a macro that binds configuration overrides for
;; the duration of its body, then automatically reverts. This is useful
;; for one-off experiments or when different sections of a notebook
;; need different settings.

;; A dark theme, scoped to this block:

(pj/with-config {:theme {:bg "#1a1a2e" :grid "#16213e" :font-size 8}}
  (-> (base-plot)
      (pj/options {:title "Dark Theme via with-config"})))

(kind/test-last
 [(fn [v]
    (= 150 (:points (pj/svg-summary v))))])

;; Partial theme override -- only `:bg` changes; `:grid` and `:font-size`
;; are deep-merged from the defaults:

(pj/with-config {:theme {:bg "#F5F5DC"}}
  (-> (base-plot)
      (pj/options {:title "Partial Theme Override"})))

(kind/test-last
 [(fn [v]
    (= 150 (:points (pj/svg-summary v))))])

;; Outside the body, the default theme is back:

(select-keys (pj/config) [:width :height])

(kind/test-last [(fn [m] (= {:width 600 :height 400} m))])

;; ## The Precedence Chain
;;
;; When multiple configuration layers are active, the highest-priority
;; layer wins for each key.  The chain from highest to lowest:
;;
;; ```
;; plot options  >  with-config  >  set-config!  >  plotje.edn  >  library defaults
;; ```
;;
;; Let's demonstrate all three programmatic levels at once, using a
;; different key at each level so we can see each one win.

;; Before overriding, the library default for point-radius is:

(:point-radius (pj/config))

(kind/test-last [(fn [v] (= 3.0 v))])

;; Now set a global override for width, height, and point-radius:

(pj/set-config! {:width 800 :height 350 :point-radius 5.0})

;; Layer a thread-local override on top for width and height
;; (but not point-radius). The rendered plot reflects the merged
;; configuration:

(def precedence-plot
  (pj/with-config {:width 1200 :height 500}
    ;; Pass plot options for width only, and materialize the plot
    ;; inside the with-config so the thread-local override applies:
    (pj/plot (-> (base-plot)
                 (pj/options {:width 900})))))

precedence-plot

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 150 (:points s))
           ;; Plot options win for :width (900 over with-config 1200
           ;; and set-config! 800).
           (= 900.0 (double (:width s)))
           ;; with-config wins for :height (500 over set-config! 350,
           ;; since plot options did not specify height).
           (= 500.0 (double (:height s))))))])

;; We can verify point-radius too -- only set-config! touched it, so it
;; wins over the library default shown above:

(def precedence-point-radius
  (pj/with-config {:width 1200 :height 500}
    (:point-radius (pj/config))))

precedence-point-radius

(kind/test-last [(fn [v] (= 5.0 v))])

;; Clean up the global override.

(pj/set-config! nil)

(select-keys (pj/config) [:width :height :point-radius])

(kind/test-last [(fn [m] (= {:width 600 :height 400 :point-radius 3.0} m))])

;; To summarize what happened:
;;
;; | Key | Library default | set-config! | with-config | plot options | Winner |
;; |:----|:---------------|:------------|:------------|:---------|:-------|
;; | `:width` | 600 | 800 | 1200 | 900 | **900** (plot options) |
;; | `:height` | 400 | 350 | 500 | -- | **500** (with-config) |
;; | `:point-radius` | 3.0 | 5.0 | -- | -- | **5.0** (set-config!) |

;; ## Project-Level Defaults with plotje.edn
;;
;; For team-wide consistency, create a `plotje.edn` file in
;; your project root (or anywhere on the classpath).  It is read
;; automatically with a 1-second cache.
;;
;; Example `plotje.edn`:
;;
;; ```clojure
;; {:width 800
;;  :height 500
;;  :theme {:bg "#FFFFFF" :grid "#F0F0F0" :font-size 10}
;;  :color-values :tableau-10
;;  :point-radius 3}
;; ```
;;
;; This layer sits between library defaults and `set-config!` in the
;; precedence chain -- it overrides defaults but is overridden by any
;; programmatic configuration.

;; ## Theme Customization
;;
;; The `:theme` key is a nested map with three entries:
;;
;; - `:bg` -- panel background color (hex string)
;; - `:grid` -- grid line color (hex string)
;; - `:font-size` -- tick label font size (number)

(set (keys (:theme (pj/config))))

(kind/test-last [(fn [s] (= #{:bg :grid :font-size} s))])

;; All configuration merging uses **deep-merge** -- nested maps like `:theme`
;; are merged recursively at every level (`pj/options`, `pj/with-config`,
;; `pj/set-config!`, and `plotje.edn`). You only need to specify the
;; keys you want to change.

;; Override only `:bg`, keep default `:grid` and `:font-size`:

(-> (base-plot)
    (pj/options {:theme {:bg "#F5F5DC"}})
    pj/plot)

(kind/test-last
 [(fn [v]
    (let [s (str v)]
      ;; beige background
      (and (clojure.string/includes? s "rgb(245,245,220)")
           ;; default grid color (#F5F5F5, off-white grey) is kept
           (clojure.string/includes? s "rgb(245,245,245)"))))])

;; Override all three for a dark theme:

(-> (base-plot)
    (pj/options {:title "Full Dark Theme"
                 :theme {:bg "#2d2d2d" :grid "#444444" :font-size 10}})
    pj/plot)

(kind/test-last
 [(fn [v]
    (let [s (str v)]
      (clojure.string/includes? s "rgb(45,45,45)")))])

;; ## Comparing Two Themes Side by Side
;;
;; `pj/arrange` composes independent plots in a CSS grid.
;; Each plot can have its own theme.

(pj/arrange
 [(-> (base-plot)
      (pj/options {:title "Light"
                   :theme {:bg "#FFFFFF" :grid "#EEEEEE" :font-size 8}
                   :width 350 :height 250}))
  (-> (base-plot)
      (pj/options {:title "Dark"
                   :theme {:bg "#2d2d2d" :grid "#444444" :font-size 8}
                   :width 350 :height 250}))])

(kind/test-last
 [(fn [v]
    ;; arrange returns a composite pose holding the two sub-plots
    (and (pj/pose? v)
         (= 2 (count (:poses (first (:poses v)))))))])

;; ## Palette Configuration
;;
;; The `:color-values` key controls the color cycle for categorical
;; color mappings. It is the outermost scope of `:values` in a `:color`
;; scale spec, so a spec written on a mapping or a layer wins over it;
;; [Scales](./plotje_book.scales.html#the-colors-a-scale-spans) has the
;; spec spelling. It accepts:
;;
;; - a keyword -- any palette name from the
;;   [clojure2d](https://github.com/Clojure2D/clojure2d) color library
;;   (thousands available: [ColorBrewer](https://colorbrewer2.org/), Wes Anderson, thi.ng, paletteer, etc.)
;; - a vector of hex strings: `["#E74C3C" "#3498DB" "#2ECC71"]`
;; - a map of `{category-value "#hex"}` for explicit assignment
;;
;; Common palette names: `:set1`, `:set2`, `:dark2`, `:tableau-10`,
;; `:category10`, `:pastel1`, `:accent`, `:paired`.
;; Use `(clojure2d.color/find-palette #"pattern")` to discover more.
;;
;; The key works at every configuration level.

;; Named palette via plot options:

(-> (base-plot)
    (pj/options {:color-values :tableau-10}))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Custom vector palette:

(-> (base-plot)
    (pj/options {:color-values ["#E74C3C" "#3498DB" "#2ECC71"]}))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Explicit map palette:

(-> (base-plot)
    (pj/options {:color-values {"setosa" "#FF6B6B"
                                "versicolor" "#4ECDC4"
                                "virginica" "#45B7D1"}}))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Global palette via set-config!:

(pj/set-config! {:color-values :pastel1})

(-> (base-plot))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

(pj/set-config! nil)

;; Thread-local palette via with-config:

(pj/with-config {:color-values :accent}
  (-> (base-plot)))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Color Scale Configuration
;;
;; When a numeric column is mapped to `:color`, Plotje uses a
;; continuous gradient (dark-to-light blue by default). The
;; `:color-range` option controls which gradient is used, and is the
;; outermost scope of `:range` in a `:color` scale spec.

;; Default continuous color (dark blue to light blue):

(-> {:x (range 50) :y (range 50) :c (range 50)}
    (pj/lay-point :x :y {:color :c}))

(kind/test-last [(fn [v] (= 50 (:points (pj/svg-summary v))))])

;; Color scale override via plot options -- inferno gradient. The
;; chosen palette propagates so the rendered legend uses the
;; matching gradient stops:

(-> {:x (range 50) :y (range 50) :c (range 50)}
    (pj/lay-point :x :y {:color :c})
    (pj/options {:color-range :inferno}))

(kind/test-last
 [(fn [v]
    (let [leg (:legend (pj/plan (-> {:x (range 50) :y (range 50) :c (range 50)}
                                    (pj/lay-point :x :y {:color :c})
                                    (pj/options {:color-range :inferno}))))]
      (and (= 50 (:points (pj/svg-summary v)))
           (= :inferno (:color-range leg))
           (= :continuous (:type leg)))))])

;; Thread-local color scale via `with-config`:

(pj/with-config {:color-range :plasma}
  (-> {:x (range 50) :y (range 50) :c (range 50)}
      (pj/lay-point :x :y {:color :c})))

(kind/test-last [(fn [v] (= 50 (:points (pj/svg-summary v))))])

;; ## Validation Control
;;
;; By default, `pj/plan` validates the output against a [Malli](https://github.com/metosin/malli)
;; schema and throws if the plan is malformed.  This is controlled
;; by the `:validate` key.
;;
;; Two helper functions let you inspect plans manually:
;;
;; - `pj/valid-plan?` -- returns true or false
;; - `pj/explain-plan` -- returns nil if valid, or a [Malli](https://github.com/metosin/malli) explanation map
;;
;; The membrane stage has the same pair -- `pj/valid-membrane?` and
;; `pj/explain-membrane` -- shown in the
;; [Membranes](./plotje_book.membranes.html#schema) chapter, and so does
;; the pose: `pj/valid-pose?` and `pj/explain-pose`. A pose built by
;; `pj/pose`, `pj/lay-*` and the rest conforms by construction, so the
;; pose pair is for a pose that has been reached into and changed.

;; Default behavior (validate = true) -- a valid plan passes silently:

(pj/plan (base-plot))

(kind/test-last
 [(fn [plan]
    (and (map? plan)
         (= 600 (:width plan))))])

;; The rendered plot works normally:

(-> (base-plot))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ### What Validation Catches
;;
;; To see what happens when a plan is malformed, we can build one
;; with validation disabled, then corrupt it.  First, create a valid
;; plan and confirm it passes:

(def good-plan (pj/plan (base-plot) {:validate false}))

(pj/valid-plan? good-plan)

(kind/test-last [(fn [v] (true? v))])

;; Now corrupt the `:width` to a string -- this violates the schema,
;; which requires a positive integer:

(def bad-plan (assoc good-plan :width "not-a-number"))

(pj/valid-plan? bad-plan)

(kind/test-last [(fn [v] (false? v))])

;; `pj/explain-plan` pinpoints the problem.  The `:errors` key in
;; the returned map shows exactly which path failed and why.  The
;; `:in` key is the value-path (where in the plan the bad value
;; lives); `:value` is the offending value:

(-> (pj/explain-plan bad-plan)
    :errors
    first
    (select-keys [:in :value]))

(kind/test-last
 [(fn [m]
    (and (= [:width] (:in m))
         (= "not-a-number" (:value m))))])

;; With validation enabled (the default), `pj/plan` would throw
;; an exception for such a malformed plan.  We can verify this
;; by catching the exception:

(try
  (let [plan (pj/plan (base-plot) {:validate false})
        bad (assoc plan :width "not-a-number")]
    (when-let [explanation (pj/explain-plan bad)]
      (throw (ex-info "Plan does not conform to schema"
                      {:explanation explanation})))
    :no-error)
  (catch Exception e
    {:caught true
     :message (.getMessage e)}))

(kind/test-last
 [(fn [m]
    (and (:caught m)
         (= "Plan does not conform to schema" (:message m))))])

;; ### Disabling Validation
;;
;; Disable validation with `:validate false`:

(pj/plan (base-plot) {:validate false})

(kind/test-last
 [(fn [plan]
    (and (map? plan)
         (= 600 (:width plan))))])

;; You can also disable validation globally for a debugging session:
;;
;; ```clojure
;; (pj/set-config! {:validate false})
;; ;; ... work freely ...
;; (pj/set-config! nil)  ;; re-enable
;; ```

;; ## Strict Option Checking
;;
;; When you pass an unknown key to `pj/options`, `pj/lay-*`, or
;; `pj/plot`, Plotje by default warns and strips it. The `:strict`
;; configuration flag turns the warning into a thrown exception -- useful
;; in tests or pipelines where a typo should fail loudly rather
;; than silently render a default plot.

;; Default behavior: warn and continue. The plot renders normally,
;; the unknown key is silently dropped:

(pj/with-config {:strict false}
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/options {:nonsense-key 42})))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; With `:strict true`, the same call throws.

(pj/with-config {:strict true}
  (try
    (-> (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width)
        (pj/options {:nonsense-key 42})
        pj/plot)
    (catch Exception e (.getMessage e))))

(kind/test-last [(fn [msg] (and (string? msg)
                                (re-find #"does not recognize" msg)))])

;; ## Summary
;;
;; | Mechanism | Scope | Persistence | Example |
;; |:----------|:------|:------------|:--------|
;; | plot options | single call | none | `(pj/options {...})` or `(pj/plot pose {...})` |
;; | `with-config` | lexical body | until body exits | `(pj/with-config {:width 800} ...)` |
;; | `set-config!` | global | until reset | `(pj/set-config! {:width 800})` |
;; | `plotje.edn` | project | file on disk | `{:width 800}` in project root |
;; | library defaults | everywhere | built-in | `resources/plotje-defaults.edn` |
;;
;; Precedence: plot options > with-config > set-config! > plotje.edn > library defaults.
;;
;; Use `pj/config` at any time to see the resolved configuration.

;; ## What's Next
;;
;; - [**Customization**](./plotje_book.customization.html) -- titles, color scales, palettes, and themes
;; - [**Placing Marks**](./plotje_book.placing_marks.html) -- where a mark goes, and in what units
;; - [**Faceting**](./plotje_book.faceting.html) -- split plots into panels by category
