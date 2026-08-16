(ns scicloj.plotje.impl.draft-schema
  "Malli schemas for the draft data model -- the records returned by
   `pj/pose->draft` (and `pj/draft`).

   Drafts are the intermediate stage between pose and plan. They are
   user-observable (the `pj/draft` shortcut and the predicates
   `pj/leaf-draft?` / `pj/composite-draft?` are public) but are
   primarily inspected, not traversed programmatically.

   `DraftLayer` models the post-scope-merge layer shape. It takes its
   aesthetic value grammar from `impl.pose-schema`, so the two states
   are one description read at two points rather than a copy that can
   drift -- which is what an earlier version of this namespace declined
   to write down. What the draft adds is
   `PositionalAestheticsNameColumns`: the invariant
   `impl.pose/resolve-positional-values` establishes, which is the one
   the appearance aesthetics would extend if they were ever normalized
   the same way.

   Backend authors who consume drafts directly should rely on
   destructuring `:layers` and `:opts` on a leaf draft, or
   `:sub-drafts` / `:chrome-spec` / `:layout` on a composite, and
   apply the layer-type registry to interpret each layer's
   `:layer-type`, `:mark`, and `:stat`."
  (:require [malli.core :as m]
            [scicloj.plotje.impl.plan-schema :as ps]
            [scicloj.plotje.impl.pose-schema :as fs]
            [scicloj.plotje.impl.resolve :as resolve]))

;; ---- Draft layer ----

(defn names-column?
  "True if a draft layer's `k` names a column rather than carrying a
   literal value. A keyword or a string always counts; anything else
   counts when the layer's `:data` has a column of that name, which is
   how an integer column name is written."
  [layer k]
  (let [v (get layer k)]
    (or (nil? v)
        (resolve/column-ref? v)
        (some? (get (:data layer) v)))))

(def PositionalAestheticsNameColumns
  "In a draft layer that carries data, `:x`, `:y` and `:x-end` all name
   columns. `impl.pose/resolve-positional-values` gets them there: a
   literal value beside a column becomes a constant column, and a layer
   where every one of them is a literal becomes a one-row dataset. So
   the stat, the extract, the domains and every mark see one shape.

   The exception is a layer with no data anywhere. There is nothing to
   add a column to, so `{:x :some-column :y 5}` keeps its 5 and
   resolves to nothing downstream, as the column reference beside it
   does."
  [:fn {:error/message "each of :x, :y and :x-end should name a column of :data"}
   (fn [layer]
     (or (nil? (:data layer))
         (every? (partial names-column? layer)
                 resolve/positional-aesthetics)))])

(def DraftLayerShape
  "The keys a draft layer carries, as `impl.pose/leaf->draft` emits
   them. Open on purpose: the merged mapping brings along every layer
   option that is not `:data`, `:mark`, `:stat` or `:position` --
   `:jitter`, `:in`, `:nudge-x`, `:font-size`, `:bandwidth` and the
   rest -- and a layer type's `:defaults` arrive the same way.

   `:data` is always present and may be nil (an annotation layer that
   names no data). `:mark` and `:stat` are absent on a layer whose
   layer type is still to be inferred: `leaf->draft` emits the
   `:infer` sentinel and then drops both, leaving the choice to
   `impl.resolve/infer-layer-type` at plan time."
  (fs/mapping-schema
   ;; Always present.
   [:data [:maybe any?]]
   ;; Which panel of a faceted leaf this layer belongs to. One of the
   ;; documented double-underscore internal draft keys.
   [:__panel-idx int?]
   ;; Layer-structural, from the layer-type registry and the call site.
   ;; A map `:layer-type` is the extension form, passed through
   ;; `impl.pose/resolve-layer-type-info` unchanged. Each of the three
   ;; may be present and nil: a registry entry is a `LayerType` record,
   ;; whose declared fields exist whether or not the entry set them, so
   ;; `lay-bar` -- registered with a `:mark` and no `:stat` -- drafts a
   ;; layer carrying an explicit nil `:stat`.
   [:layer-type {:optional true} [:or keyword? map?]]
   [:mark {:optional true} [:maybe keyword?]]
   [:stat {:optional true} [:maybe keyword?]]
   [:position {:optional true} [:maybe keyword?]]
   [:x-only {:optional true} boolean?]
   ;; Column-type overrides, carried through from the mapping.
   [:x-type {:optional true} [:maybe fs/ColumnTypeOverride]]
   [:y-type {:optional true} [:maybe fs/ColumnTypeOverride]]
   [:color-type {:optional true} [:maybe fs/ColumnTypeOverride]]
   ;; Plot-level settings stamped onto every layer from the leaf's
   ;; `:opts`, so a layer is readable on its own. One per `pj/scale`
   ;; channel; `pj/scale` has no channel these do not cover.
   [:x-scale {:optional true} ps/ScaleSpec]
   [:y-scale {:optional true} ps/ScaleSpec]
   [:size-scale {:optional true} ps/ScaleSpec]
   [:alpha-scale {:optional true} ps/ScaleSpec]
   [:fill-scale {:optional true} ps/ScaleSpec]
   ;; `:color`'s spec has a key of its own. The configuration's
   ;; `:color-scale` holds a gradient, and while `pj/scale :color`
   ;; wrote its spec there too, whichever was written second silently
   ;; discarded the other. The gradient does not travel on a layer; it
   ;; is resolved from the configuration at plan time.
   [:color-scale-spec {:optional true} ps/ScaleSpec]
   [:shape-scale {:optional true} ps/ScaleSpec]
   [:coord {:optional true} keyword?]
   ;; Facet labels, already formatted for display. Their presence is
   ;; what plan.clj detects to build the facet grid.
   [:facet-col {:optional true} string?]
   [:facet-row {:optional true} string?]))

(def DraftLayer
  "One element of a draft: a layer type plus every mapping that
   reached it, plus the data it draws from. Specification, not yet
   computed."
  [:and DraftLayerShape PositionalAestheticsNameColumns])

(def LeafDraftSchema
  "A leaf draft -- the post-scope-merge intermediate produced by
   `pj/pose->draft` for a leaf pose. Canonical contract for the
   `LeafDraft` defrecord in `impl/resolve.clj`.

   `:layers` is one `DraftLayer` per applicable layer, multiplied over
   facet values when the leaf is faceted."
  [:map
   [:layers [:vector DraftLayer]]
   [:opts map?]])

(def SubDraft
  "One entry in a CompositeDraft's `:sub-drafts`: a leaf placed at a
   pose-tree path with its rect inside the composite, plus per-leaf
   `:opts` (suppress-x-label / suppress-y-label / suppress-legend
   flags applied during composite drafting). Parallel to `SubPlot`
   in plan-schema.

   Note: `:draft` here is a bare vector of layer maps (not a
   `LeafDraft` record). Composite sub-drafts skip the `LeafDraft`
   wrapper -- the layers travel directly through the compositor's
   per-leaf draft->plan call."
  [:map
   [:path [:vector int?]]
   [:rect ps/Rect]
   [:draft [:vector DraftLayer]]
   [:opts map?]])

(def CompositeDraftSchema
  "A composite draft -- produced by `pj/pose->draft` for a composite
   pose. Canonical contract for the `CompositeDraft` defrecord in
   `impl/resolve.clj`.

   Sub-drafts wrap leaf drafts in an envelope (path + rect + opts),
   parallel to plan-schema's SubPlot. `:chrome-spec` is shaped like
   `CompositeChrome` from plan-schema; the chrome-spec on a draft is
   the same value that flows through to the plan stage's `:chrome`.
   `:layout` maps each sub-draft's pose-tree path to its rectangle
   inside the composite.

   `:width`, `:height` and `:layout` are drawing-space geometry, which
   a leaf draft carries none of. The stage boundary is arity rather
   than availability: a leaf draft is a vector of per-layer maps with
   nowhere to put a fact about the whole plot."
  [:map
   [:width pos-int?]
   [:height pos-int?]
   [:sub-drafts [:vector SubDraft]]
   [:chrome-spec ps/CompositeChrome]
   [:layout [:map-of [:vector int?] ps/Rect]]])

(def DraftSchema
  "Top-level draft schema -- accepts either shape."
  [:or LeafDraftSchema CompositeDraftSchema])

(defn valid?
  "True if x conforms to a draft schema (leaf or composite)."
  [x]
  (m/validate DraftSchema x))

(defn explain
  "Explain why x does not conform to the draft schema. Returns nil
   if valid."
  [x]
  (m/explain DraftSchema x))

(defn layer-valid?
  "True if x conforms to `DraftLayer`. Separate from `valid?` because
   a composite sub-draft's layers are reached without a `LeafDraft`
   wrapper."
  [x]
  (m/validate DraftLayer x))

(defn explain-layer
  "Explain why x does not conform to `DraftLayer`, or nil if it does."
  [x]
  (m/explain DraftLayer x))
