;; # Layer Types

;; A **layer type** is the bundle that determines how data becomes a
;; visual element. It combines three concepts:
;;
;; - **mark** -- what shape to show (points, bars, lines, ...)
;; - **stat** -- what computation to apply first (pass through, bin, count, regress, ...)
;; - **position** -- how overlapping groups share space (identity, dodge, stack, fill)
;;
;; Layer functions (`pj/lay-point`, `pj/lay-histogram`, `pj/lay-bar`,
;; `(pj/lay-smooth {:stat :linear-model})`, etc.) each add a layer
;; with the corresponding layer type. When no layer is added,
;; Plotje infers a layer type from the column types.
;;
;; All built-in layer types are registered in a data registry. The
;; tables below are generated from that registry -- they stay in
;; sync with the code.

(ns plotje-book.layer-types
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Layer-type registry -- for inspecting layer-type data
   [scicloj.plotje.layer-type :as layer-type]
   ;; String utilities
   [clojure.string :as str]))

;; ## Reading the Registry
;;
;; The tables below are generated directly from the layer-type
;; registry, so they track whatever is currently registered. Two
;; small helpers query the registry: `used-by` returns the
;; comma-separated list of layer types whose given field equals a
;; value, and `distinct-in-order` returns each distinct field value
;; in the order layer types were registered. Both are used to
;; populate the Mark, Stat, and Position tables further down.

(defn used-by
  "Sorted comma-separated layer-type names whose `field` equals `value`."
  [field value]
  (->> (layer-type/registered)
       (filter (fn [[_ m]] (= value (or (get m field) :identity))))
       (map (comp name key))
       sort
       (str/join ", ")))

(defn distinct-in-order
  "Distinct values of `field` across layer types, in first-seen order."
  [field]
  (let [seen (volatile! #{})]
    (reduce (fn [acc k]
              (let [v (get (layer-type/lookup k) field)]
                (if (@seen v) acc
                    (do (vswap! seen conj v) (conj acc v)))))
            [] layer-type/layer-type-order)))

;; ## Layer Types
;;
;; Each row is a registered layer type showing its mark, stat, and position.

(kind/table
 {:column-names ["Layer type" "Mark" "Stat" "Position"]
  :row-maps
  (for [k layer-type/layer-type-order
        :let [m (layer-type/lookup k)]]
    {"Layer type" (kind/code (pr-str k))
     "Mark" (kind/code (pr-str (:mark m)))
     "Stat" (kind/code (pr-str (:stat m)))
     "Position" (kind/code (pr-str (or (:position m) :identity)))})})

(kind/test-last
 [(fn [t]
    (= 25 (count (:row-maps t))))])

;; The "Position" column shows each layer-type's registered default.
;; A few marks (`:bar`, `:lollipop`, `:boxplot`,
;; `:violin`) carry no registered default and the table therefore
;; lists `:identity` for them, but they apply `:dodge` at extract
;; time when a categorical color or group splits the data into
;; multiple sub-bars per category. So a colored
;; `(pj/lay-bar :species {:color :group})` produces dodged bars
;; even though the table shows `:identity` for `:bar`. To force a
;; different layout, pass `:position` explicitly in the layer options.

;; ## Marks
;;
;; A **mark** is the visual shape shown for each data point or
;; group. Several layer types may share the same mark -- for
;; instance, `tile` and `density-2d` both produce tiles, and `lm`
;; (linear model) and `loess` (local regression) both produce lines.

(kind/table
 {:column-names ["Mark" "Shape" "Used by"]
  :row-maps
  (for [mk (distinct-in-order :mark)]
    {"Mark" (kind/code (pr-str mk))
     "Shape" (pj/mark-doc mk)
     "Used by" (used-by :mark mk)})})

(kind/test-last
 [(fn [t]
    (= 22 (count (:row-maps t))))])

;; ## Stats
;;
;; A **stat** (statistical transform) processes raw data before
;; rendering. Each stat takes data-space inputs and produces the
;; geometry that its mark will show.

(kind/table
 {:column-names ["Stat" "What it computes" "Used by"]
  :row-maps
  (for [st (distinct-in-order :stat)]
    {"Stat" (kind/code (pr-str st))
     "What it computes" (pj/stat-doc st)
     "Used by" (used-by :stat st)})})

(kind/test-last
 [(fn [t]
    (pos? (count (:row-maps t))))])

;; ## Positions
;;
;; A **position** adjustment determines how groups share a categorical
;; axis slot. Position runs between stat computation and rendering.

(kind/table
 {:column-names ["Position" "What it does" "Used by"]
  :row-maps
  (for [pos [:identity :dodge :stack :fill]]
    {"Position" (kind/code (pr-str pos))
     "What it does" (pj/position-doc pos)
     "Used by" (used-by :position pos)})})

(kind/test-last
 [(fn [t]
    (pos? (count (:row-maps t))))])

;; You can override the default position by passing `:position` in
;; the layer options.
;; When multiple layers share `:position :dodge`, they are coordinated
;; together -- error bars automatically align with bars.

;; ## Layer Options
;;
;; The options map passed to `lay-` functions controls aesthetics,
;; statistical parameters, and spatial adjustments for that layer.
;;
;; ### Universal options
;;
;; Accepted by every layer type:

(kind/table
 {:column-names ["Option" "Description"]
  :row-maps
  (for [k layer-type/universal-layer-options]
    {"Option" (kind/code (pr-str k))
     "Description" (get layer-type/layer-option-docs k)})})

(kind/test-last
 [(fn [t] (pos? (count (:row-maps t))))])

;; ### Layer-type-specific options
;;
;; Some layer types accept additional keys beyond the universal set.
;; Layer types not listed here accept only the universal options above.

(kind/table
 {:column-names ["Layer type" "Additional options"]
  :row-maps
  (for [k layer-type/layer-type-order
        :let [m (layer-type/lookup k)
              accepts (:accepts m)]
        :when (seq accepts)]
    {"Layer type" (kind/code (pr-str k))
     "Additional options" accepts})})

(kind/test-last
 [(fn [t] (pos? (count (:row-maps t))))])

;; ### All layer option keys

(kind/table
 {:column-names ["Option" "Description"]
  :row-maps
  (for [[k desc] (sort-by key layer-type/layer-option-docs)]
    {"Option" (kind/code (pr-str k))
     "Description" desc})})

(kind/test-last
 [(fn [t] (pos? (count (:row-maps t))))])

;; ## What's Next
;;
;; - [**Relationships**](./plotje_book.relationships.html) -- see point, line, and regression layer types in action
;; - [**Distributions**](./plotje_book.distributions.html) -- histograms, density, boxplots, violins
;; - [**Customization**](./plotje_book.customization.html) -- colors, palettes, themes, and per-layer options
