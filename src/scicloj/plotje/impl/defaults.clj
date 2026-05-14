(ns scicloj.plotje.impl.defaults
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure2d.color :as c]
            [scicloj.kindly.v4.api :as kindly]))

;; ---- Palette and Theme ----

(defn c2d->rgba
  "Convert a clojure2d color (Vec4, 0-255 channels) to [r g b a] in 0-1 range."
  [color]
  (let [cc (c/to-color color)]
    [(/ (double (c/red cc)) 255.0)
     (/ (double (c/green cc)) 255.0)
     (/ (double (c/blue cc)) 255.0)
     (/ (double (c/alpha cc)) 255.0)]))

(def default-palette-name
  "Default categorical palette name (clojure2d palette keyword)."
  :set1)

(def ^:private palette-aliases
  "Short aliases for clojure2d palette names that differ from our old naming."
  {:tableau10 :tableau-10})

(defn resolve-palette
  "Resolve a keyword to a clojure2d palette, trying aliases.
   Returns a non-empty palette vector, falling back to the default palette."
  [k]
  (let [pal (c/palette (get palette-aliases k k))]
    (if (seq pal) pal (c/palette default-palette-name))))

(def theme
  "Default theme: background color, grid color, and font size."
  {:bg "#E8E8E8" :grid "#F5F5F5" :font-size 11})

;; ---- Visual Defaults ----

(def defaults
  "In-code fallback for keys not present in the EDN resource. The EDN
   file (`resources/plotje-defaults.edn`) is the canonical source;
   this map is consulted only when the EDN load fails or as a secondary
   lookup in `(or cfg defaults)` patterns in stat/layout code."
  {;; Layout — must match EDN
   :width 600 :height 400
   :margin 10 :margin-multi 10 :panel-size 200 :legend-width 100
   ;; Ticks
   :tick-spacing-x 60 :tick-spacing-y 40
   ;; Points
   :point-radius 3.0 :point-opacity 0.75
   :point-stroke "none" :point-stroke-width 0
   ;; Bars and lines
   :bar-opacity 0.85 :line-width 2.5 :grid-stroke-width 0.6
   ;; Annotations
   :annotation-stroke "#333" :annotation-dash [4 3] :band-opacity 0.15
   ;; Statistics
   :bin-method :sturges
   :domain-padding 0.05
   ;; Labels and titles
   :label-font-size 13 :title-font-size 15
   :label-offset 38 :title-offset 18
   ;; Facet strips
   :strip-font-size 10 :strip-height 16
   ;; Fallback
   :default-color "#333"})

;; ---- Aesthetic Registry ----

(def aesthetic-registry
  "Per-aesthetic semantic properties. Single source of truth for the set
   of mapping keys that can reference dataset columns. :numeric? marks
   aesthetics whose column values, when present, are numeric -- those
   are the keys eligible for finite-value filtering at plan time."
  {:x     {:numeric? true}
   :y     {:numeric? true}
   :color {:numeric? true}
   :size  {:numeric? true}
   :alpha {:numeric? true}
   :y-min {:numeric? true}
   :y-max {:numeric? true}
   :x-end {:numeric? true}
   :fill  {:numeric? true}
   :shape {:numeric? false}
   :group {:numeric? false}
   :text  {:numeric? false}})

(def column-keys
  "Set of keywords that can reference dataset columns in mappings."
  (set (keys aesthetic-registry)))

(def numeric-aesthetic-keys
  "Aesthetics whose column values are numeric -- subject to finite-value
   filtering. Derived from aesthetic-registry."
  (into []
        (comp (filter (comp :numeric? val)) (map key))
        aesthetic-registry))

;; ---- Shape Symbols ----

(def shape-syms
  "Available shape symbols for categorical shape mapping."
  [:circle :square :triangle :diamond])

(def legend-swatch-size
  "Side length of legend color swatches (square, in pixels)."
  8)

;; ---- Color Helpers ----

(defn hex->rgba
  "Convert any color representation to [r g b a] in 0-1 range.
   Accepts hex strings (#RGB, #RRGGBB, #RRGGBBAA, or without #),
   named color strings (\"red\", \"steelblue\"), keywords (:red, :darkblue),
   or any value that clojure2d.color/to-color understands."
  [color]
  (if (and (string? color) (not (.startsWith ^String color "#")))
    ;; Non-# string: try as hex first, then as named color keyword
    (let [cc (try (c/to-color color)
                  (catch NumberFormatException _ nil))]
      (if cc
        (c2d->rgba cc)
        (let [cc (c/to-color (keyword color))]
          (if cc
            (c2d->rgba cc)
            (throw (ex-info (str "Unknown color: \"" color
                                 "\". Use a hex string like \"#FF0000\" or a CSS color name like \"red\".")
                            {:color color}))))))
    (c2d->rgba color)))

(defn- spread-idx
  "Map category index `i` of `n` categories into a palette index across
   `p` palette entries, spreading evenly so that n categories against
   a p-entry palette pick values stretched across the whole range
   instead of the first n entries.

   Currently unused by `color-for` — the canonical path for continuous
   color is `:color-scale`, not `:palette` with a gradient name. This
   helper stays as a dormant utility in case we later expose a
   `:palette-sampling :spread` escape hatch."
  [^long i ^long n ^long p]
  (cond
    (<= p 0) 0
    (<= n 1) (mod i p)
    (>= n p) (mod i p)
    :else (mod (long (Math/round (* (double i) (/ (double (dec p)) (double (dec n))))))
               p)))

(defn color-for
  "Look up the color for a categorical value from the palette.
   Returns [r g b a] in 0-1 range.
   palette can be: nil (default), a keyword (any clojure2d palette name),
   a vector of hex strings, or a map of {category-value color}.

   Map lookup is tolerant of string/keyword mismatch: {:setosa \"#F00\"}
   matches both :setosa and \"setosa\" data values.

   For keyword/vector palettes, the i-th category gets the i-th
   palette entry (wrapping modulo the palette size). This preserves
   the authorial ordering of designed-categorical palettes like
   :set1, :dark2, :tableau-10. If you want a continuous color ramp
   (viridis, inferno, etc.) for a numeric column, use the dedicated
   `:color-scale` option instead -- that's the canonical gradient
   path and interpolates the full color range smoothly."
  ([categories val]
   (color-for categories val nil))
  ([categories val palette]
   (let [raw-idx (if categories (.indexOf ^java.util.List categories val) -1)
         idx (if (neg? raw-idx) 0 raw-idx)]
     (if (map? palette)
       ;; Explicit mapping: look up value, try alternate string/keyword form,
       ;; fall back to index in default palette
       (let [cv (or (get palette val)
                    (cond
                      (keyword? val) (get palette (name val))
                      (string? val) (get palette (keyword val))
                      :else nil))]
         (if cv
           (hex->rgba cv)
           (let [pal (resolve-palette default-palette-name)]
             (c2d->rgba (nth pal (mod idx (count pal)))))))
       ;; Index-based: keyword → c/palette, vector → use directly, nil → default
       (cond
         (keyword? palette)
         (let [pal (resolve-palette palette)]
           (c2d->rgba (nth pal (mod idx (count pal)))))
         (sequential? palette)
         (hex->rgba (nth palette (mod idx (count palette))))
         :else
         (let [pal (resolve-palette default-palette-name)]
           (c2d->rgba (nth pal (mod idx (count pal))))))))))

;; ---- Continuous Color ----

(defn- wrap-gradient
  "Wrap a clojure2d gradient function to return [r g b a] in 0-1 range."
  [g]
  (fn [t] (c2d->rgba (g t))))

(def gradient-color
  "Default gradient function (dark blue → light blue, matching ggplot2).
   Takes t in [0,1], returns [r g b a] 0-1."
  (wrap-gradient (c/gradient [(c/to-color "#132B43") (c/to-color "#56B1F7")])))

(def diverging-color
  "Diverging gradient function (RdBu). Takes t in [0,1], returns [r g b a] 0-1."
  (wrap-gradient (c/gradient :grDevices/RdBu)))

(def ^:private gradient-aliases
  "Short aliases for common clojure2d gradient names."
  {:viridis :viridis/viridis :inferno :viridis/inferno
   :plasma :viridis/plasma :magma :viridis/magma
   :cividis :viridis/cividis :turbo :viridis/turbo
   :rocket :viridis/rocket :mako :viridis/mako
   :RdBu :grDevices/RdBu :RdYlBu :grDevices/RdYlBu
   :BrBG :grDevices/BrBG :coolwarm :pals/coolwarm})

(def gradient-palette-keywords
  "Set of keywords that resolve to a continuous gradient rather than a
   categorical palette. Users who pass these to `:palette` almost always
   meant `:color-scale` with a numeric color column. `plan/warn-palette-wrap!`
   fires a warning when it sees one of these on the `:palette` slot."
  (set (keys gradient-aliases)))

(defn- resolve-gradient-name
  "Resolve a keyword to a clojure2d gradient, trying aliases then direct lookup."
  [k]
  (or (c/gradient (get gradient-aliases k k))
      (c/gradient k)))

(defn resolve-gradient-fn
  "Resolve a :color-scale option to a gradient function t→[r g b a] (0-1 range).
   nil or :sequential → dark blue to light blue (ggplot2 default).
   :diverging → RdBu.
   keyword → clojure2d gradient name (:inferno, :viridis/plasma, etc.).
   map {:low hex :mid hex :high hex} → custom 3-stop gradient.
   function → used directly.
   Throws on unrecognized keyword."
  [color-scale]
  (cond
    (nil? color-scale) gradient-color
    (= :sequential color-scale) gradient-color
    (= :diverging color-scale) diverging-color
    (fn? color-scale) color-scale
    (keyword? color-scale)
    (if-let [g (resolve-gradient-name color-scale)]
      (wrap-gradient g)
      (throw (ex-info (str "Unknown color scale: " color-scale
                           ". Use a clojure2d gradient name (e.g. :inferno, :viridis, :plasma)"
                           " or :sequential / :diverging.")
                      {:color-scale color-scale})))
    (map? color-scale)
    (let [{:keys [low mid high]
           :or {low "#B2182B" mid "#F7F7F7" high "#2166AC"}} color-scale
          g (c/gradient [(c/to-color low) (c/to-color mid) (c/to-color high)])]
      (wrap-gradient g))
    :else (throw (ex-info (str "Invalid color scale: " (pr-str color-scale)
                               ". Expected nil, keyword, map, or function.")
                          {:color-scale color-scale}))))

(defn normalize-midpoint
  "Remap a value v from [vmin, vmax] to [0,1] with optional midpoint.
   Without midpoint: linear (v-vmin)/(vmax-vmin).
   With midpoint: values below midpoint → [0, 0.5], above → [0.5, 1.0]."
  [v vmin vmax midpoint]
  (if midpoint
    (let [v (double v) vmin (double vmin) vmax (double vmax) mid (double midpoint)]
      (cond
        (<= v vmin) 0.0
        (>= v vmax) 1.0
        (<= v mid) (if (<= mid vmin) 0.5 (* 0.5 (/ (- v vmin) (- mid vmin))))
        :else (if (>= mid vmax) 0.5 (+ 0.5 (* 0.5 (/ (- v mid) (- vmax mid)))))))
    (let [span (- (double vmax) (double vmin))]
      (if (<= span 0) 0.5 (/ (- (double v) (double vmin)) span)))))

(defn normalize-continuous
  "Remap a value v from [vmin, vmax] to [0,1] using a scale-type aware
   transform. :linear (default) uses normalize-midpoint with the optional
   midpoint. :log uses log10 endpoints; midpoint is ignored under :log."
  [scale-type v vmin vmax midpoint]
  (if (= scale-type :log)
    (let [vl   (Math/log10 (max 1e-300 (double v)))
          minl (Math/log10 (max 1e-300 (double vmin)))
          maxl (Math/log10 (max 1e-300 (double vmax)))
          span (- maxl minl)]
      (if (<= span 0) 0.5 (/ (- vl minl) span)))
    (normalize-midpoint v vmin vmax midpoint)))

;; ---- Name Formatting ----

(defn fmt-name
  "Format a keyword as a readable name: :sepal-length -> \"sepal length\"."
  [k]
  (str/replace (name k) #"[-_]" " "))

(defn fmt-category-label
  "Format a category value (keyword, string, number, etc.) for display.
   Keywords are rendered without their leading colon: :widget -> \"widget\".
   Used for axis tick labels, legend entries, facet strip labels, and any
   other user-visible category text."
  [v]
  (cond
    (nil? v) ""
    (keyword? v) (name v)
    :else (str v)))

;; ---- Configuration Precedence Chain ----
;;
;; Resolved with precedence (highest to lowest):
;;   1. plot options (passed to pj/options, pj/plot, pj/plan, etc.)
;;   2. binding *config* (thread-local override)
;;   3. set-config! (global mutable state)
;;   4. plotje.edn (project root or classpath)
;;   5. library defaults (plotje-defaults.edn)

(def ^:private library-defaults
  "Library defaults loaded from plotje-defaults.edn on classpath.
   Falls back to the static `defaults` and `theme` maps if the resource is missing."
  (delay
    (if-let [r (io/resource "plotje-defaults.edn")]
      (edn/read-string (slurp r))
      (merge defaults {:theme theme}))))

(def ^:dynamic *config*
  "Dynamic var for thread-local config overrides.
   Bind to a map to override any config keys for the current thread.
   (binding [defaults/*config* {:theme {:bg \"#FFF\"}}] ...)"
  nil)

(defonce ^:private config-atom
  (atom nil))

(defn set-config!
  "Set global config overrides. Persists across calls until reset.
   (set-config! {:palette :dark2 :theme {:bg \"#FFFFFF\"}})
   (set-config! nil)  — reset to defaults"
  [m]
  (reset! config-atom m))

(def ^:private edn-cache
  "TTL cache for plotje.edn (1 second)."
  (atom {:value nil :timestamp 0}))

(defn- read-plotje-edn
  "Read plotje.edn from classpath or the current working directory.
   Returns nil if the file does not exist. Cached with 1-second TTL."
  []
  (let [{:keys [value timestamp]} @edn-cache
        now (System/currentTimeMillis)]
    (if (< (- now timestamp) 1000)
      value
      (let [from-cp (io/resource "plotje.edn")
            from-cwd (let [f (io/file "plotje.edn")]
                       (when (.exists f) f))
            source (or from-cp from-cwd)
            v (when source (edn/read-string (slurp source)))]
        (reset! edn-cache {:value v :timestamp now})
        v))))

(def config-key-docs
  "Documentation metadata for configuration keys.
   Each entry maps a key to [category description]."
  {:width ["Layout" "Plot width in pixels"]
   :height ["Layout" "Plot height in pixels"]
   :margin ["Layout" "Margin around single-panel plots (pixels)"]
   :margin-multi ["Layout" "Margin around multi-panel plots (pixels)"]
   :panel-size ["Layout" "Default panel size for faceted/multi-variable grids"]
   :legend-width ["Layout" "Width reserved for the legend column"]
   :legend-position ["Layout" "Legend placement — :right, :bottom, :top, or :none"]
   :theme ["Theme" "Nested map {:bg :grid :font-size} — visual identity"]
   :label-font-size ["Typography" "Font size for axis labels"]
   :title-font-size ["Typography" "Font size for the plot title"]
   :strip-font-size ["Typography" "Font size for facet strip labels"]
   :point-radius ["Points" "Default point radius"]
   :point-opacity ["Points" "Default point opacity (0.0–1.0)"]
   :point-stroke ["Points" "Point border stroke color (\"none\" to disable)"]
   :point-stroke-width ["Points" "Point border stroke width"]
   :bar-opacity ["Bars & Lines" "Default bar fill opacity"]
   :line-width ["Bars & Lines" "Default line stroke width"]
   :grid-stroke-width ["Bars & Lines" "Grid line stroke width"]
   :annotation-stroke ["Annotations" "Stroke color for annotation marks"]
   :annotation-dash ["Annotations" "Dash pattern [dash gap] for annotation lines"]
   :band-opacity ["Annotations" "Opacity for confidence bands"]
   :tick-spacing-x ["Ticks" "Minimum pixel spacing between x-axis ticks"]
   :tick-spacing-y ["Ticks" "Minimum pixel spacing between y-axis ticks"]
   :x-tick-angle ["Ticks" "Rotation angle for x-axis tick labels in degrees (0 = horizontal, -45 = common diagonal)"]
   :x-tick-label-height ["Ticks" "Extra vertical space (px) reserved below panels for angled x-tick labels, added on top of :label-offset.
                                  When nil, auto-computed from :x-tick-angle. When 0, no extra space is reserved and rotated labels may be 
                                  clipped by the SVG boundary."]
   :bin-method ["Statistics" "Histogram bin count method (:sturges, :sqrt, :rice, :fd)"]
   :domain-padding ["Statistics" "Fractional padding added to numeric domains"]
   :label-offset ["Labels" "Pixel offset for axis labels from the axis"]
   :title-offset ["Labels" "Pixel offset for the title from the top"]
   :strip-height ["Labels" "Height of facet strip label bars"]
   :validate ["Behavior" "When true, validate plans against Malli schema"]
   :strict ["Behavior" "When true, throw on unknown option keys instead of warning and stripping"]
   :default-color ["Behavior" "Fallback color when no color mapping is set"]
   :palette ["Color" "Categorical palette — keyword, vector, or map"]
   :color-scale ["Color" "Continuous color scale — :sequential, :diverging, or keyword"]
   :color-midpoint ["Color" "Center value for diverging color scales"]
   :tooltip ["Interaction" "Enable hover tooltips (truthy value)"]
   :brush ["Interaction" "Enable drag-to-select brush (truthy value)"]
   :format ["Output" "Render format — :svg (default)"]})

(def plot-option-docs
  "Documentation for plot-level option keys.
   These are accepted by pj/options, pj/plan, and pj/plot but are
   inherently per-plot (text content or nested config override).
   Each entry maps a key to [category description]."
  {:title ["Content" "Plot title string"]
   :subtitle ["Content" "Plot subtitle string"]
   :caption ["Content" "Plot caption string (bottom)"]
   :x-label ["Content" "X-axis label (overrides inferred)"]
   :y-label ["Content" "Y-axis label (overrides inferred)"]
   :color-label ["Content" "Color legend title (overrides inferred column name)"]
   :fill-label ["Content" "Fill legend title (overrides inferred column name; used by tile fills, density-2d, bin2d)"]
   :size-label ["Content" "Size legend title (overrides inferred column name)"]
   :alpha-label ["Content" "Alpha legend title (overrides inferred column name)"]
   :panel-width ["Layout" "Pin panel width in pixels (escape hatch; :width becomes derived total)"]
   :panel-height ["Layout" "Pin panel height in pixels (escape hatch; :height becomes derived total)"]
   :scales ["Layout" "Facet scale coordination — :shared (default), :free, :free-x, :free-y"]
   :share-scales ["Layout" "Composite scale coordination — set of axes (#{:x}, #{:y}, or #{:x :y}) shared across composite cells"]
   :config ["Config" "Nested config map merged into resolved config"]})

(defn config
  "Return the effective resolved configuration as a map.
   Deep-merges: library defaults < plotje.edn < set-config! < *config*.
   Nested maps (e.g. :theme) are merged recursively.
   Useful for inspecting which values are in effect."
  []
  (let [base @library-defaults
        from-edn (read-plotje-edn)
        from-atom @config-atom
        from-binding *config*]
    (cond-> base
      from-edn (kindly/deep-merge from-edn)
      from-atom (kindly/deep-merge from-atom)
      from-binding (kindly/deep-merge from-binding))))

(defn- backfill-nil-theme-values
  "Replace any nil entries in the resolved theme with the library defaults.
   A user passing `{:theme {:bg nil}}` through `with-config`/plot-opts
   would otherwise see their nil merged on top of the default and reach
   renderers that call `hex->rgba`, causing NPEs. Backfilling keeps the
   baseline visible while still honoring non-nil overrides."
  [cfg]
  (let [default-theme (:theme @library-defaults)]
    (update cfg :theme
            (fn [t]
              (reduce-kv (fn [m k v] (if (nil? v) (assoc m k (get default-theme k)) m))
                         t
                         t)))))

(def ^:private flat-config-keys
  "Config keys that are forwarded as flat scalars from plot-opts to cfg.
   :theme is excluded (deep-merged separately) and :config is excluded
   (it's the nested escape hatch, not a config key itself)."
  (disj (set (keys config-key-docs)) :theme :config))

(defn resolve-config
  "Resolve config with plot options deep-merged on top of the precedence chain.
   Plot options have the highest priority. Any key from `config-key-docs`
   passed directly in plot-opts is forwarded to the resolved cfg. Nested
   maps (:theme) are merged recursively. The `:config` key is a deep-merge
   escape hatch for arbitrary overrides."
  [plot-opts]
  (let [cfg (config)]
    (when (:panel-size plot-opts)
      (println "Warning: :panel-size is a legacy option and no longer used by the layout pipeline. Use :panel-width and/or :panel-height, or :width/:height for total SVG size."))
    (-> (if (seq plot-opts)
          (let [;; Deep-merge the nested :config escape hatch first (lowest priority
                ;; among plot-opts, highest priority over the cfg chain).
                cfg (if-let [nested (:config plot-opts)]
                      (kindly/deep-merge cfg nested)
                      cfg)
                ;; Deep-merge :theme (map value, not scalar)
                cfg (if-let [th (:theme plot-opts)]
                      (update cfg :theme kindly/deep-merge th)
                      cfg)]
            ;; Forward every flat config key the user passed.
            (reduce-kv (fn [m k v]
                         (if (and (contains? flat-config-keys k)
                                  (some? v))
                           (assoc m k v)
                           m))
                       cfg
                       plot-opts))
          cfg)
        backfill-nil-theme-values)))
