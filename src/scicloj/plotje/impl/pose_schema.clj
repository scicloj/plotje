(ns scicloj.plotje.impl.pose-schema
  "Malli schema for the Pose data model.

   A pose is a plain recursive map. A leaf pose has no :poses (or
   an empty :poses vector). A composite pose has :poses and an
   optional :layout describing how sub-poses tile a bounding
   rectangle.

   Validation is not wired into any runtime path yet; Phase 6 of the
   pre-alpha refactor adds validation at public API boundaries. Until
   then, impl.pose operates on structurally-valid poses by convention;
   this schema is the authoritative definition of that convention.

   The three stages are documented to different depths and enforced
   differently. The plan schema is checked on every plan, unless the
   `:validate` configuration key turns it off -- it is what raises
   `Plan does not conform to schema`, which is where a mapping value
   with no reading tends to surface. Pose and draft are documented and
   unenforced. What keeps this schema honest instead is
   `impl/pose_schema_test.clj`, which validates a pose from each public
   constructor.

   Decisions made in Phase 2:
   - A leaf with no :data and no :mapping is valid -- leaves inherit
     context from ancestors via impl.pose/resolve-tree.
   - :share-scales is structurally allowed on any pose; it is a no-op
     on leaves (nothing to share).
   - :layout :weights length is not required to equal (count :poses);
     impl.pose/compute-layout tolerates short/long weight vectors.

   `aesthetic-value-schemas` below writes down the mapping value
   grammar per aesthetic, as the code behaves today. It decides
   nothing: where two aesthetics disagree, both readings are
   recorded, because what a mapping value should mean is an open
   design question and this schema is a description of the answer
   in force rather than an argument for one."
  (:require [malli.core :as m]))

;; ---- Sub-schemas ----

(def Layout
  "Compositor layout spec for a composite pose. `:matrix` places
   leaves on a grid derived from their `:x` / `:y` mappings and
   ignores `:weights`; it is what `pj/pose` stamps when a threaded
   `:x` or `:y` promotes a leaf into a composite. See
   `impl.pose/compute-layout`."
  [:map
   [:direction {:optional true} [:enum :horizontal :vertical :matrix]]
   [:weights {:optional true} [:vector pos?]]])

(def ShareScales
  "Axis keys whose scale domains union across descendants of a composite."
  [:set [:enum :x :y]])

;; ---- Mapping values ----

(def ColumnRef
  "A value that can name a dataset column: a keyword or a string.
   Mirrors `impl.resolve/column-ref?`. Matching against the dataset is
   strict -- `:species` does not name a column whose name is the string
   `species`, and the other way round."
  [:or keyword? string?])

(def Temporal
  "A temporal value, which a scale coerces like any other datum.
   Mirrors the instance checks in `impl.resolve/literal-position?`."
  [:fn {:error/message "should be a LocalDate, LocalDateTime, Instant or Date"}
   (fn [v]
     (or (instance? java.time.LocalDate v)
         (instance? java.time.LocalDateTime v)
         (instance? java.time.Instant v)
         (instance? java.util.Date v)))])

(def PositionalLiteral
  "A literal value that places a mark on its own, as `{:x 6.5}` does,
   rather than naming a column to read it from. Mirrors
   `impl.resolve/literal-position?`."
  [:or number? Temporal])

(def PositionalAesthetic
  "What may be written for `:x`, `:y` and the range endpoints: a
   column reference, or a literal value in data space."
  [:or ColumnRef PositionalLiteral])

(def aesthetic-value-schemas
  "Value grammar per aesthetic, as the code behaves today. The key set
   is `impl.defaults/column-keys`; `pose-mapping-value-grammar-test`
   pins the two together so a new aesthetic cannot be added without a
   reading for it.

   Three things Malli cannot state, so they are stated here:

   1. **Which values name a column is decided against the data, for
      some aesthetics only.** A string `:color` names a column when the
      data has a column of that name and is a CSS color otherwise
      (`impl.pose/validate-columns` carves it out, and
      `impl.resolve/resolve-aesthetics` does the lookup). A number in
      `:x`, `:y` or `:x-end` names a column on the same terms
      (`impl.pose/resolve-positional-values`), which matters because a
      dataset built without column names gets integer ones. Every other
      aesthetic reads a keyword or a string as a column reference and
      demands that the column exist.

   2. **Where the mapping is written decides what a number in `:x` or
      `:y` means.** In a layer's options map it places a mark, either
      as one mark or broadcast over the layer's data. In `pj/pose`, or
      in the `:x` / `:y` arguments of a `lay-*` call,
      `api/check-position-mapping` refuses it, so an integer column
      name reports the rename it needs.

   3. **Which layer types accept the key.** `:size`, `:shape`, `:text`,
      `:fill`, `:x-end`, `:y-min` and `:y-max` are not universal layer
      options -- each is on the `:accepts` list of particular layer
      types in `layer-type.clj`, and a layer type that does not accept
      one warns and strips it. `:y-min` / `:y-max` are the sharpest
      case: a column reference on `:errorbar` and `:pointrange`, a
      literal value in data space on `:band-h`.

   `nil` is a legal value for every aesthetic -- it cancels a mapping
   inherited from an ancestor, and survives into the draft as an
   explicit nil, so each entry is wrapped in `:maybe` by
   `mapping-schema`."
  {;; Positional aesthetics -- where the mark sits. A literal value
   ;; becomes a constant column in the draft
   ;; (`impl.pose/resolve-positional-values`), so by draft time these
   ;; are column references -- see `impl.draft-schema/DraftLayer` for
   ;; the one exception.
   :x     PositionalAesthetic
   :y     PositionalAesthetic
   :x-end PositionalAesthetic
   ;; `:y-min` / `:y-max` place a mark in the same way but are not in
   ;; `impl.resolve/positional-aesthetics`, so a literal value here is
   ;; never turned into a column; `:band-h` reads it from the mapping.
   ;; The glossary counts `:x-min` / `:x-max` among the positional
   ;; aesthetics too, but they are absent from `defaults/column-keys`,
   ;; so nothing validates them and they have no entry here.
   :y-min PositionalAesthetic
   :y-max PositionalAesthetic
   ;; Appearance aesthetics -- how the mark looks. A keyword is a
   ;; column reference and nothing else, so the color vocabulary is
   ;; reachable only through strings.
   :color ColumnRef
   :size  [:or ColumnRef [:and number? [:fn {:error/message "should be positive"} pos?]]]
   :alpha [:or ColumnRef [:and number? [:fn {:error/message "should be within 0 and 1"}
                                        (fn [v] (<= 0 v 1))]]]
   :shape ColumnRef
   :text  ColumnRef
   :fill  ColumnRef
   ;; `:group` is neither: it draws nothing of its own, it splits a
   ;; layer into one drawn group per value. Several grouping columns
   ;; are allowed beside a single one.
   :group [:or ColumnRef [:sequential ColumnRef]]})

(def ColumnTypeOverride
  "A `:x-type` / `:y-type` / `:color-type` value: the classification
   `impl.resolve/column-type` would otherwise infer."
  [:enum :categorical :numerical :temporal])

(defn mapping-schema
  "Build a mapping schema from `aesthetic-value-schemas`, optionally
   with extra typed entries. Open on purpose, and open in two
   different ways at the two places a mapping appears:

   - a pose's `:mapping` carries the aesthetics plus the three column
     type overrides (`api/pose-mapping-keys`); `:data` is stripped
     into the pose itself rather than left in the mapping;
   - a layer's `:mapping` carries the aesthetics plus every layer
     option that is not `:data`, `:mark`, `:stat` or `:position` --
     `:jitter`, `:in`, `:nudge-x`, `:font-size`, `:bandwidth` and the
     rest all live there (`api/build-layer`).

   So the name `:mapping` covers more than mappings, and the two
   `:mapping` maps admit different key sets. Keys are always keywords."
  [& extra-entries]
  [:and
   [:map-of keyword? any?]
   (into [:map]
         cat
         [(for [[k s] (sort-by str aesthetic-value-schemas)]
            [k {:optional true} [:maybe s]])
          extra-entries])])

(def Mapping
  "Aesthetic mapping on a pose or a layer. See
   `aesthetic-value-schemas` for what each value may be and for the
   three things that grammar cannot state."
  (mapping-schema [:x-type {:optional true} [:maybe ColumnTypeOverride]]
                  [:y-type {:optional true} [:maybe ColumnTypeOverride]]
                  [:color-type {:optional true} [:maybe ColumnTypeOverride]]))

(def PoseLayer
  "A pose-layer: a layer declaration attached to a pose. :layer-type
   names a registered entry; :mark / :stat / :position are
   layer-structural siblings extracted from user opts by build-layer
   (Phase 6 decision 1)."
  [:map
   [:layer-type {:optional true} keyword?]
   [:mark       {:optional true} keyword?]
   [:stat       {:optional true} keyword?]
   [:position   {:optional true} keyword?]
   [:mapping    {:optional true} Mapping]
   [:data       {:optional true} any?]])

;; ---- Pose (recursive) ----

(def PoseSchema
  "Structural schema for a pose tree.

   Shape:
     {:data         ?  dataset (inherited from ancestor if absent)
      :mapping      ?  aesthetic mappings (merges with ancestors)
      :layers       ?  PoseLayer vec at this level (accumulates into leaves)
      :poses       ?  sub-poses; absent or empty = leaf
      :layout       ?  Layout for composites
      :opts         ?  plot options (inheritable)
      :share-scales ?  ShareScales}

   Permissive {:closed false} intentionally -- generators like facet
   and mosaic attach metadata keys (:panel-label, :facet-row, ...) to
   leaves that pass through resolve-tree unchanged."
  [:schema {:registry {::pose [:map
                               [:data {:optional true} any?]
                               [:mapping {:optional true} Mapping]
                               [:layers {:optional true} [:vector PoseLayer]]
                               [:poses {:optional true} [:vector [:ref ::pose]]]
                               [:layout {:optional true} Layout]
                               [:opts {:optional true} map?]
                               [:share-scales {:optional true} ShareScales]]}}
   [:ref ::pose]])

;; ---- Validation Helpers ----

(defn valid?
  "Check if x conforms to the pose schema."
  [x]
  (m/validate PoseSchema x))

(defn explain
  "Explain why x does not conform to the pose schema, or nil if valid."
  [x]
  (m/explain PoseSchema x))
