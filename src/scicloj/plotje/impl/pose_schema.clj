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
  (:require [malli.core :as m]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.temporal :as temporal]))

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

   The types are `impl.temporal/readings`, asked rather than listed
   again here: a fourth copy of that list is how a type came to be
   accepted in one place and refused in another."
  [:fn {:error/message (str "should be a " temporal/accepted-names)}
   temporal/temporal-value?])

(def PositionalLiteral
  "A literal value that places a mark on its own, as `{:x 6.5}` does,
   rather than naming a column to read it from. Mirrors
   `impl.resolve/literal-position?`."
  [:or number? Temporal])

(def PositionalAesthetic
  "What may be written for `:x`, `:y` and the range endpoints: a
   column reference, or a literal value in data space."
  [:or ColumnRef PositionalLiteral])

(def Color
  "A value that unmistakably names a color: a `#`-prefixed hex string,
   or a CSS color name as a string or a keyword. Mirrors
   `impl.defaults/names-a-color?`, which is deliberately narrower than
   what `hex->rgba` will convert -- a bare `abc` converts, and is a
   mistyped column name far more often than it is a shade."
  [:fn {:error/message "should be a CSS color name or a # hex string"}
   (fn [v] (defaults/names-a-color? v))])

(def Shape
  "One of the marker symbols a `:shape` mapping draws with."
  (into [:enum] defaults/shape-syms))

(def ExplicitMapping
  "A mapping written out in full: which source it takes, and
   optionally which side of the scale to read it through.

   `{:column :species}` insists on the column even where a value of
   that name could be drawn; `{:value \"blue\"}` insists on the color
   even where the data carries a column called blue; `{:from :species}`
   is the plain reading spelled out -- ask the data, and take whichever
   answer it gives. `:from` is what lets a mapping that leaves the
   source to the data still carry a `:scale`.

   `:scale false` draws what the convention would scale, `:scale true`
   scales what it would draw, and omitting `:scale` -- or writing
   `nil` -- leaves the convention in charge. A scale type or a spec map
   there names which scale.

   A map is unambiguous as a mapping value because no aesthetic takes
   one: a color is a string or a keyword, a size is a number, a shape
   is a symbol from a fixed list."
  [:and
   [:map
    [:column {:optional true} any?]
    [:value {:optional true} any?]
    [:from {:optional true} any?]
    [:scale {:optional true} any?]]
   [:fn {:error/message "should name exactly one of :column, :value or :from"}
    (fn [m] (= 1 (count (filter #(contains? m %) [:column :value :from]))))]])

(def drawn-value-schemas
  "What each aesthetic accepts as a **written value** -- the half of the
   grammar that is not a column reference.

   Split out from `aesthetic-value-schemas` because one `[:or ColumnRef
   ...]` cannot answer \"is this drawable\", which is the third step of
   the rule the draft resolves by: ask the layer's data, then ask what
   the aesthetic can draw, then report that neither reading fits. Fusing
   the two halves is the same mistake the registry's old `:literal` key
   made a level up, and it is corrected the same way -- state each half,
   compose the whole.

   `impl.aesthetics` is the runtime consumer. That matters: a schema
   nothing validates drifts from the code, which is how the six
   disagreeing aesthetic lists came about.

   An aesthetic absent from this map has no reading for a written value
   at all -- `:group` splits the data and draws nothing of its own, so
   there is nothing a value could mean. `:shape` and `:fill` are listed
   because a value on them *means* something; whether one is accepted
   yet is the registry's `:value?`, and the two are deliberately
   separate."
  {:x     PositionalLiteral
   :y     PositionalLiteral
   :x-end PositionalLiteral
   :y-min PositionalLiteral
   :y-max PositionalLiteral
   :x-min PositionalLiteral
   :x-max PositionalLiteral
   :color Color
   :fill  Color
   :shape Shape
   ;; Every positive number is a valid radius, and every number within
   ;; 0 and 1 a valid opacity. Neither says whether it was meant as one
   ;; -- which is why these two decide by source rather than by value.
   :size  [:and number? [:fn {:error/message "should be positive"} pos?]]
   :alpha [:and number? [:fn {:error/message "should be within 0 and 1"}
                         (fn [v] (<= 0 v 1))]]
   ;; Any string labels a mark; there is nothing to check beyond type.
   :text  string?})

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
  (merge
   ;; Composed, not restated: each aesthetic accepts a column
   ;; reference, plus whatever `drawn-value-schemas` says it draws. The
   ;; two halves cannot drift because only one of them is written down.
   ;;
   ;; Positional aesthetics -- where the mark sits. A written value
   ;; becomes a constant column in the draft
   ;; (`impl.pose/resolve-positional-values`), so by draft time these
   ;; are column references -- see `impl.draft-schema/DraftLayer` for
   ;; the one exception. `:y-min` / `:y-max` place a mark the same way
   ;; but are not in `impl.resolve/positional-aesthetics`, so a value
   ;; there is never turned into a column; `:band-h` reads it from the
   ;; mapping. The glossary counts `:x-min` / `:x-max` among the
   ;; positional aesthetics too, but they are absent from
   ;; `defaults/column-keys`, so nothing validates them.
   ;; Which aesthetics take both readings is the registry's answer, not
   ;; a list kept beside it: `:column?` and `:value?` together. An
   ;; aesthetic that gains `:value?` without a drawn-value grammar
   ;; fails here at load rather than passing every check and drawing
   ;; nothing.
   (into {} (for [k (defaults/aesthetics-where #(and (:column? %) (:value? %)))]
              [k [:or ColumnRef (drawn-value-schemas k) ExplicitMapping]]))
   ;; `:fill` has a drawn-value grammar and does not accept one yet --
   ;; the registry's `:value?` is what says so, and flipping it is what
   ;; moves its entry into the composed `:or` above.
   {:fill  [:or ColumnRef ExplicitMapping]
    ;; `:group` is neither: it draws nothing of its own, it splits a
    ;; layer into one drawn group per value. Several grouping columns
    ;; are allowed beside a single one.
    :group [:or ColumnRef [:sequential ColumnRef] ExplicitMapping]}))

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
