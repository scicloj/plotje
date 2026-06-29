(ns scicloj.plotje.layer-type
  "Layer-type registry — keyword → layer-type map (mark + stat + position).
   Layer types are plain data maps. The registry makes them discoverable
   and extensible. Use `lookup` to get a layer type by keyword, `registered`
   to enumerate all layer types, and `register!` to add new ones."
  (:require [scicloj.plotje.impl.resolve :as resolve]))

;; ---- Registry ----

(def universal-layer-options
  "Layer options accepted by all layer types. :x and :y are included so a
   layer can override the pose's position mapping (an overlay-like
   pattern); the four-level merge in pose/leaf->draft already honors
   layer-level x/y, this list keeps build-layer's unknown-option
   warning consistent with that behavior. :x-type/:y-type/:color-type
   override the inferred column types (e.g. :x-type :categorical
   treats a numeric x as categorical). :mark and :stat let any layer
   override the visual mark or statistical transform supplied by its
   layer-type entry; unknown keywords raise a clear error at build time."
  [:x :y :color :color-type :alpha :group :position :data
   :x-type :y-type :mark :stat])

(def layer-option-docs
  "Documentation for layer option keys. Maps key to description string."
  {:color "Column keyword (categorical grouping) or literal color string"
   :color-type "Override inferred color type — :categorical or :numerical. Use :categorical to treat numeric IDs as groups."
   :x-type "Override inferred x-column type — :categorical, :numerical, or :temporal. Use :categorical on numeric x (hours, years, IDs) when a categorical-axis mark (bar, boxplot) is needed."
   :y-type "Override inferred y-column type — :categorical, :numerical, or :temporal. Mirror of :x-type, used for horizontal layouts."
   :alpha "Column keyword (per-point opacity) or fixed number 0.0–1.0"
   :group "Column keyword for grouping without color"
   :position "Position adjustment keyword — how overlapping groups are arranged (see pj/position-doc)"
   :nudge-x "Shift all x-coordinates by this data-space amount"
   :nudge-y "Shift all y-coordinates by this data-space amount"
   :align-x "Horizontal text anchor — :left, :center, or :right (default :left); which part of the label sits at the x position"
   :align-y "Vertical text anchor — :top, :center, or :bottom (default :center); which part of the label sits at the y position. Data-oriented: :top puts the label's top edge at the point"
   :size "Column keyword or fixed number — point radius or stroke width"
   :shape "Column keyword for per-point shape"
   :jitter "true or pixel amount — random offset to reduce overplotting"
   :text "Column keyword for label content"
   :confidence-band "true to show a standard-error confidence ribbon around the fitted line"
   :bootstrap-resamples "Number of bootstrap resamples for a LOESS confidence ribbon (default 200)"
   :bandwidth "Smoothing bandwidth for density and LOESS methods"
   :normalize "Histogram normalization — :density (area integrates to 1) or nil"
   :levels "Number of contour iso-levels (default 5)"
   :fill "Column keyword for tile fill values (pre-computed heatmap)"
   :y-min "Column keyword for lower error bound of an errorbar, or lower y bound of a horizontal shaded band"
   :y-max "Column keyword for upper error bound of an errorbar, or upper y bound of a horizontal shaded band"
   :side "Rug tick position — :x (default), :y, or :both"
   :density-2d-grid "2D density grid resolution — number of bins per axis (default 25)"
   :y-intercept "Numeric or temporal y-axis position for a horizontal reference line"
   :x-intercept "Numeric or temporal x-axis position for a vertical reference line"
   :x-min "Lower x bound of a vertical shaded band"
   :x-max "Upper x bound of a vertical shaded band"
   :x-end "Column keyword for the right-edge x value of a horizontal interval bar"
   :interval-thickness "Fraction (0.0–1.0) of the categorical band that an interval bar fills (default 0.7)"})

(def ^:private registry*
  "Atom holding keyword → layer-type entry map."
  (atom {}))

(defn register!
  "Register a layer type. `k` is a keyword, `entry` is a map with
   :mark, :stat, and optionally :position and :doc.
   Position defaults to nil (identity) — only :dodge, :stack, :fill are explicit."
  [k entry]
  (swap! registry* assoc k (resolve/map->LayerType entry))
  k)

(defn lookup
  "Look up a registered layer type by keyword. Returns the layer-type map
   (with :mark, :stat, :position, :doc), or nil if not found."
  [k]
  (get @registry* k))

(defn registered
  "Return all registered layer types as a map of keyword → entry."
  []
  @registry*)

(def layer-type-order
  "Canonical display order for built-in layer types."
  [:point :line :step :area
   :histogram :bar
   :smooth :density
   :tile :density-2d :contour
   :boxplot :violin :ridgeline
   :summary :errorbar :lollipop
   :text :label :rug
   :interval-h
   :rule-h :rule-v :band-h :band-v])

;; ---- Built-in layer types ----

(register! :point {:mark :point :stat :identity :accepts [:size :shape :jitter :text :nudge-x :nudge-y] :doc "Scatter — individual data points."})
(register! :line {:mark :line :stat :identity :accepts [:size :nudge-x :nudge-y] :doc "Line — connects data points in order."})
(register! :step {:mark :step :stat :identity :accepts [:size] :doc "Step — horizontal-then-vertical connected points."})
(register! :area {:mark :area :stat :identity :accepts [] :doc "Area — filled region under a line."})
(register! :histogram {:mark :bar :stat :bin :x-only true :accepts [:normalize :bins :binwidth] :doc "Histogram — bins numerical data into bars."})
(register! :bar {:mark :rect :accepts [] :doc "Bar — counts categories (x only), or uses y as the bar height when a y column is given."})
(register! :smooth {:mark :line :stat :loess :accepts [:confidence-band :level :bootstrap-resamples :bandwidth :size :nudge-x :nudge-y] :doc "Smoothed trend line — defaults to LOESS; pass {:stat :linear-model} for OLS."})
(register! :density {:mark :area :stat :density :x-only true :accepts [:bandwidth] :doc "Density — KDE (kernel density estimation) as filled area."})
(register! :tile {:mark :tile :stat :bin2d :accepts [:fill :density-2d-grid] :doc "Tile/heatmap — 2D grid binning."})
(register! :density-2d {:mark :tile :stat :density-2d :accepts [:density-2d-grid] :doc "2D density — kernel density estimation (KDE) smoothed heatmap."})
(register! :contour {:mark :contour :stat :density-2d :accepts [:levels :size] :doc "Contour — iso-density contour lines."})
(register! :boxplot {:mark :boxplot :stat :boxplot :accepts [:size :box-width] :doc "Boxplot — median, quartiles, whiskers, outliers."})
(register! :violin {:mark :violin :stat :violin :accepts [:bandwidth :size] :doc "Violin — mirrored density curve per category."})
(register! :ridgeline {:mark :ridgeline :stat :violin :accepts [:bandwidth] :doc "Ridgeline — stacked density curves per category."})
(register! :summary {:mark :pointrange :stat :summary :accepts [:size] :doc "Summary — mean ± standard error per category."})
(register! :errorbar {:mark :errorbar :stat :identity :accepts [:y-min :y-max :size :cap-width :nudge-x :nudge-y] :doc "Errorbar — vertical error bars."})
(register! :lollipop {:mark :lollipop :stat :identity :accepts [:size] :doc "Lollipop — stem with dot."})
(register! :text {:mark :text :stat :identity :accepts [:text :font-size :nudge-x :nudge-y :align-x :align-y] :doc "Text — data-driven labels."})
(register! :label {:mark :label :stat :identity :accepts [:text :font-size :nudge-x :nudge-y :align-x :align-y] :doc "Label — text with background box."})
(register! :rug {:mark :rug :stat :identity :x-only true :accepts [:side :length] :doc "Rug — axis-margin tick marks."})
(register! :interval-h {:mark :interval-h :stat :identity :accepts [:x-end :interval-thickness]
                        ;; Dodge/stack/fill don't compose with interval-h yet -- a Gantt
                        ;; row may have multiple non-overlapping or overlapping intervals
                        ;; per category and we draw each row's rect at the band centre.
                        :rejects [:position]
                        :doc "Interval — horizontal bars from x to x-end at categorical y. For Gantt-style timelines."})
;; Annotation methods reject the universal options that have no
;; meaning for a single rule/band: there are no groups to dodge or
;; stack, no shape/jitter to vary across an aggregated mark, and the
;; column-type overrides only matter for stat-based marks.
(def ^:private annotation-rejects
  [:position :group :x-type :y-type :color-type])

(register! :rule-h {:mark :rule-h :stat :identity :accepts [:y-intercept] :rejects annotation-rejects :doc "Horizontal reference line at y = y-intercept."})
(register! :rule-v {:mark :rule-v :stat :identity :accepts [:x-intercept] :rejects annotation-rejects :doc "Vertical reference line at x = x-intercept."})
(register! :band-h {:mark :band-h :stat :identity :accepts [:y-min :y-max] :rejects annotation-rejects :doc "Horizontal shaded band between y = y-min and y = y-max."})
(register! :band-v {:mark :band-v :stat :identity :accepts [:x-min :x-max] :rejects annotation-rejects :doc "Vertical shaded band between x = x-min and x = x-max."})

