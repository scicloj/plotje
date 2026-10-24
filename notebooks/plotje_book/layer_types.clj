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
;; Each row is a registered layer type showing its mark, stat, position,
;; and the layer settings it presets. Two layer types can share a mark and
;; differ only in the stat or the presets: `:histogram` is the
;; `:bar` mark with a binning stat, and `:label` is the `:text` mark with
;; a background box preset.

(kind/table
 {:column-names ["Layer type" "Mark" "Stat" "Position" "Columns" "Presets"]
  :row-maps
  (for [k layer-type/layer-type-order
        :let [m (layer-type/lookup k)]]
    {"Layer type" (kind/code (pr-str k))
     "Mark" (kind/code (pr-str (:mark m)))
     "Stat" (kind/code (pr-str (:stat m)))
     "Position" (kind/code (pr-str (or (:position m) :identity)))
     "Columns" (kind/code (pr-str (layer-type/column-counts k)))
     "Presets" (if-let [d (:defaults m)] (kind/code (pr-str d)) "")})})

(kind/test-last
 [(fn [t]
    (= 26 (count (:row-maps t))))])

;; ### Columns a layer type draws from
;;
;; The "Columns" entry is the layer type's `:column-counts`: the set of
;; numbers of columns it draws from. `#{1}` draws from an x column
;; alone, as a histogram does; `#{2}` draws from an x column and a y
;; column, as a scatter does; `#{1 2}` draws from either, as a bar does --
;; it counts the categories of one column, and takes a second column as
;; the heights of its bars:

(layer-type/column-counts :bar)

(kind/test-last
 [(fn [cc]
    (let [columns (fn [pose] (mapv (juxt :x :y) (:layers (pj/draft pose))))
          data {:a [1 2 3] :b [4 5 6] :c [7 8 9]}]
      (and (= #{1 2} cc)
           ;; Backs the third bullet below: the line is given :a, the
           ;; rug is given no :y.
           (= [[:a :b] [:a :c]]
              (columns (-> data
                           (pj/lay-point :a :b)
                           (pj/lay-line {:y :c}))))
           (= [[:a :b] [:c nil]]
              (columns (-> data
                           (pj/lay-point :a :b)
                           (pj/lay-rug {:x :c})))))))])

;; Among the places that read it:
;;
;; - A layer added after `pj/facet-matrix`, or on the root of a
;;   composite, is drawn in the panels whose number
;;   of columns it draws from -- the
;;   [scatter plot matrix](./plotje_book.relationships.html#scatter-plot-matrix-splom)
;;   is the case where the panels differ.
;; - A pose that names a column only on `:y` draws a layer that draws
;;   from one column on its side.
;; - A layer that names one column is given the panel's column on the
;;   other axis only where its layer type cannot draw from one column:
;;   after `(pj/lay-point data :a :b)`, `(pj/lay-line {:y :c})` draws
;;   `:c` against `:a`, and `(pj/lay-rug {:x :c})` draws `:c` alone.
;;
;; A layer type registered without `:column-counts` draws from two
;; columns, or from one where it is registered `:x-only true`.

;; A layer type carries one more field the table leaves out, because
;; only one built-in layer type has it. `:varies` names the appearance
;; aesthetics its mark varies from row to row, and the quantity it draws
;; each as. `:point` is the one that has it: it draws `:size` as a
;; radius and `:alpha` as an opacity. The plan reads this to decide
;; whether a column mapped to an aesthetic is drawn at all:

(:varies (layer-type/lookup :point))

(kind/test-last
 [(fn [m] (= {:size :radius :alpha :opacity} m))])

;; Every other mark draws one size and one opacity for the whole layer,
;; so a column mapped to those aesthetics changes nothing, and Plotje
;; warns. See
;; [Extensibility](./plotje_book.extensibility.html#drawing-an-aesthetic-the-layer-type-declares-it-varies)
;; for declaring `:varies` on a layer type of your own.

;; `:text` and `:label` share a mark, a stat, and a position; the
;; "Presets" column shows that a label starts with its box switched
;; on:

(mapv #(select-keys (layer-type/lookup %) [:mark :stat :defaults])
      [:text :label])

(kind/test-last
 [(fn [rows]
    (= [{:mark :text :stat :identity}
        {:mark :text :stat :identity :defaults {:box true}}]
       rows))])

;; The "Position" column shows each layer-type's registered default.
;; A few marks (`:bar`, `:lollipop`, `:boxplot`,
;; `:violin`) carry no registered default and the table therefore
;; lists `:identity` for them, but they apply `:dodge` at extract
;; time when a categorical color or group splits the data into
;; multiple sub-bars per category. So a colored
;; `(pj/lay-bar :species {:color :group})` produces dodged bars
;; even though the table shows `:identity` for `:bar`. To force a
;; different layout, write `:position` in the layer's mapping.

;; ## Marks
;;
;; A **mark** is the visual shape shown for each data point or
;; group. Several layer types may share the same mark -- for
;; instance, `tile` and `density-2d` both produce tiles, and `line`
;; and `smooth` both produce lines.

(kind/table
 {:column-names ["Mark" "Shape" "Used by"]
  :row-maps
  (for [mk (distinct-in-order :mark)]
    {"Mark" (kind/code (pr-str mk))
     "Shape" (pj/mark-doc mk)
     "Used by" (used-by :mark mk)})})

(kind/test-last
 [(fn [t]
    (and (= 22 (count (:row-maps t)))
         ;; The two pairs the prose above names.
         (= :tile (:mark (layer-type/lookup :tile)) (:mark (layer-type/lookup :density-2d)))
         (= :line (:mark (layer-type/lookup :line)) (:mark (layer-type/lookup :smooth)))))])

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

;; You can override the default position by writing `:position` in
;; the layer's mapping.
;; When multiple layers share `:position :dodge`, they are coordinated
;; together -- error bars automatically align with bars.

;; ## A Layer's Mapping
;;
;; The mapping passed to a `pj/lay-*` function takes that layer's
;; aesthetics and its layer settings: statistical parameters, the
;; position adjustment, the layer's own data.
;;
;; ### Keys every layer type accepts

(kind/table
 {:column-names ["Key" "Description"]
  :row-maps
  (for [k layer-type/universal-mapping-keys]
    {"Key" (kind/code (pr-str k))
     "Description" (get layer-type/layer-mapping-docs k)})})

(kind/test-last
 [(fn [t] (pos? (count (:row-maps t))))])

;; ### Layer-type-specific keys
;;
;; Some layer types accept additional keys beyond the keys every layer
;; type accepts. Layer types not listed here accept only the keys in
;; the table above.

(kind/table
 {:column-names ["Layer type" "Additional keys"]
  :row-maps
  (for [k layer-type/layer-type-order
        :let [m (layer-type/lookup k)
              accepts (:accepts m)]
        :when (seq accepts)]
    {"Layer type" (kind/code (pr-str k))
     "Additional keys" accepts})})

(kind/test-last
 [(fn [t] (pos? (count (:row-maps t))))])

;; ### All keys of a layer's mapping

(kind/table
 {:column-names ["Key" "Description"]
  :row-maps
  (for [[k desc] (sort-by key layer-type/layer-mapping-docs)]
    {"Key" (kind/code (pr-str k))
     "Description" desc})})

(kind/test-last
 [(fn [t]
    (let [documented (set (keys layer-type/layer-mapping-docs))
          in-use (into (set layer-type/universal-mapping-keys)
                       (mapcat :accepts (vals (layer-type/registered))))]
      (and (= (count documented) (count (:row-maps t)))
           (empty? (remove documented in-use))
           (empty? (remove in-use documented)))))])

;; ## What's Next
;;
;; - [**Relationships**](./plotje_book.relationships.html) -- see point, line, and regression layer types in action
;; - [**Distributions**](./plotje_book.distributions.html) -- histograms, density, boxplots, violins
;; - [**Customization**](./plotje_book.customization.html) -- colors, palettes, themes, and per-layer styling
