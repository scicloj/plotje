(ns scicloj.plotje.impl.plan-schema
  "Malli schemas for the plan data model."
  (:require [malli.core :as m]))

;; ---- Primitive Schemas ----

(def Color
  "RGBA color vector [r g b a], values 0-1."
  [:vector {:min 3 :max 4} number?])

(def ScaleSpec
  "Scale specification."
  [:map
   [:type [:enum :linear :log :categorical]]])

;; ---- Tick Info ----

(def TickInfo
  "Tick values and labels for an axis."
  [:map
   [:values [:vector any?]]
   [:labels [:vector string?]]
   [:categorical? boolean?]])

;; ---- Layer Groups (per mark type) ----

(def PointGroup
  "Data-space positions for a :point layer. :xs and :ys are
  :sequential any? rather than number? because either axis may be
  categorical (strings travel through a band scale); per-panel
  scales enforce what values are valid at render time."
  [:map
   [:color Color]
   [:xs [:sequential any?]]
   [:ys [:sequential any?]]
   [:colors {:optional true} [:sequential Color]]
   [:sizes {:optional true} [:sequential number?]]
   [:alphas {:optional true} [:sequential number?]]
   [:shapes {:optional true} [:vector any?]]
   [:row-indices {:optional true} [:sequential int?]]
   [:dodge-idx {:optional true} int?]
   [:y0s {:optional true} [:sequential number?]]])

(def BarBin
  [:map
   [:lo number?]
   [:hi number?]
   [:count number?]])

(def BarGroup
  [:map
   [:color Color]
   [:bars [:vector BarBin]]])

(def CountEntry
  [:map
   [:category any?]
   [:count number?]
   ;; :y0 / :y1 are populated by apply-position :stack to describe the
   ;; cumulative baseline and top of each stacked bar segment.
   [:y0 {:optional true} number?]
   [:y1 {:optional true} number?]])

(def RectCountGroup
  [:map
   [:color Color]
   [:label string?]
   [:counts [:vector CountEntry]]])

(def RectValueGroup
  [:map
   [:color Color]
   [:label {:optional true} string?]
   [:xs [:sequential any?]]
   [:ys [:sequential number?]]])

(def LineSegmentGroup
  "Regression line segment."
  [:map
   [:color Color]
   [:x1 number?] [:y1 number?]
   [:x2 number?] [:y2 number?]])

(def PolylineGroup
  "Connected points. :xs/:ys are any? for the same reason as PointGroup."
  [:map
   [:color Color]
   [:xs [:sequential any?]]
   [:ys [:sequential any?]]])

(def TextGroup
  "Text labels at data positions. :xs/:ys are any? for the same reason as PointGroup."
  [:map
   [:color Color]
   [:xs [:sequential any?]]
   [:ys [:sequential any?]]
   [:labels {:optional true} [:vector string?]]])

(def ErrorbarGroup
  "Error bars at data positions."
  [:map
   [:color Color]
   [:xs [:sequential number?]]
   [:ys [:sequential number?]]
   [:ymins [:sequential number?]]
   [:ymaxs [:sequential number?]]])

(def BoxplotBox
  "A single boxplot box with five-number summary."
  [:map
   [:category any?]
   [:color Color]
   [:color-category {:optional true} any?]
   [:median number?]
   [:q1 number?]
   [:q3 number?]
   [:whisker-lo number?]
   [:whisker-hi number?]
   [:outliers {:optional true} [:sequential number?]]
   ;; :dodge-idx is populated by apply-position :dodge when multiple
   ;; boxplots share an x-category (side-by-side layout).
   [:dodge-idx {:optional true} int?]])

(def ViolinEntry
  "A single violin curve with category and mirrored density."
  [:map
   [:category any?]
   [:color Color]
   [:color-category {:optional true} any?]
   [:ys [:sequential number?]]
   [:densities [:sequential number?]]
   ;; Same rationale as BoxplotBox: dodged violin layouts annotate
   ;; each entry with its position in the dodge group.
   [:dodge-idx {:optional true} int?]])

(def TileEntry
  "A single tile (heatmap cell) with bounds and fill color."
  [:map
   [:x-lo number?]
   [:x-hi number?]
   [:y-lo number?]
   [:y-hi number?]
   [:color Color]])

;; ---- PlanLayer ----

(def MarkStyle
  [:map
   [:opacity {:optional true} number?]
   [:radius {:optional true} number?]
   [:stroke-width {:optional true} number?]
   [:font-size {:optional true} number?]
   [:box-width {:optional true} number?]
   [:cap-width {:optional true} number?]
   [:length {:optional true} number?]
   [:jitter {:optional true} [:or boolean? number?]]])

(def FiniteNumber
  "A finite number. Rejects NaN and Infinity, which would render as
   silently-invisible annotations at unresolvable axis positions."
  [:and number? [:fn {:error/message "must be finite (not NaN or Infinity)"}
                 (fn [n] (Double/isFinite (double n)))]])

(def Annotation
  "A reference line or band annotation. Dispatched on :mark:
   rule-h requires :y-intercept; rule-v requires :x-intercept;
   band-h requires :y-min and :y-max; band-v requires :x-min and :x-max.
   :color must be a literal string (not a column reference); pose-level
   column-mapped aesthetics are filtered before annotations reach the plan."
  [:multi {:dispatch :mark}
   [:rule-h [:map
             [:mark [:= :rule-h]]
             [:y-intercept FiniteNumber]
             [:color {:optional true} string?]
             [:alpha {:optional true} number?]
             [:offset-x {:optional true} number?]
             [:offset-y {:optional true} number?]]]
   [:rule-v [:map
             [:mark [:= :rule-v]]
             [:x-intercept FiniteNumber]
             [:color {:optional true} string?]
             [:alpha {:optional true} number?]
             [:offset-x {:optional true} number?]
             [:offset-y {:optional true} number?]]]
   [:band-h [:map
             [:mark [:= :band-h]]
             [:y-min FiniteNumber]
             [:y-max FiniteNumber]
             [:color {:optional true} string?]
             [:alpha {:optional true} number?]
             [:offset-x {:optional true} number?]
             [:offset-y {:optional true} number?]]]
   [:band-v [:map
             [:mark [:= :band-v]]
             [:x-min FiniteNumber]
             [:x-max FiniteNumber]
             [:color {:optional true} string?]
             [:alpha {:optional true} number?]
             [:offset-x {:optional true} number?]
             [:offset-y {:optional true} number?]]]])

(def PlanLayer
  "A plan-layer: a rendered mark with data-space geometry. Canonical
   contract for the `PlanLayer` defrecord in `impl/resolve.clj`."
  [:map
   [:mark keyword?]
   [:style MarkStyle]
   [:groups {:optional true} [:vector [:or PointGroup BarGroup RectCountGroup RectValueGroup
                                       LineSegmentGroup PolylineGroup TextGroup ErrorbarGroup]]]
   [:boxes {:optional true} [:vector BoxplotBox]]
   [:violins {:optional true} [:vector ViolinEntry]]
   [:tiles {:optional true} [:vector TileEntry]]
   [:ridges {:optional true} [:vector [:map [:category any?] [:color Color] [:ys [:sequential number?]] [:densities [:sequential number?]]]]]
   [:levels {:optional true} [:vector any?]]
   [:ribbons {:optional true} [:vector any?]]
   [:color-categories {:optional true} [:maybe [:vector any?]]]
   [:position {:optional true} keyword?]
   ;; Drawing-unit shift of the whole layer, applied after the scales.
   ;; Numbers, not columns: one value shifts everything the layer drew.
   [:offset-x {:optional true} number?]
   [:offset-y {:optional true} number?]
   ;; The space this layer's positions are in. Absent means :data.
   [:in {:optional true} [:enum :data :drawing-area]]
   [:dodge-ctx {:optional true} any?]
   [:categories {:optional true} [:vector any?]]
   [:side {:optional true} [:enum :x :y :both]]
   [:x-domain {:optional true} [:maybe [:sequential any?]]]
   [:y-domain {:optional true} [:maybe [:sequential any?]]]
   [:size-scale {:optional true} [:maybe any?]]
   [:alpha-scale {:optional true} [:maybe any?]]
   ;; Plot-wide category-to-symbol assignment for the :shape aesthetic,
   ;; decided at plan time so marks and legend agree.
   [:shape-map {:optional true} [:maybe [:map-of any? keyword?]]]])

;; ---- Panel ----

(def Panel
  "A single plot panel."
  [:map
   [:x-domain [:vector any?]]
   [:y-domain [:vector any?]]
   [:x-scale ScaleSpec]
   [:y-scale ScaleSpec]
   [:coord [:enum :cartesian :flip :polar :fixed]]
   [:x-ticks TickInfo]
   [:y-ticks TickInfo]
   [:layers [:vector PlanLayer]]
   [:row int?]
   [:col int?]
   [:annotations {:optional true} [:vector Annotation]]
   [:row-label {:optional true} [:maybe string?]]
   [:col-label {:optional true} [:maybe string?]]])

;; ---- Legend ----

(def LegendEntry
  [:map
   [:label string?]
   [:color Color]
   ;; Present when one column drives both :color and :shape: the key
   ;; draws this symbol in its own color instead of a square swatch.
   [:shape {:optional true} keyword?]])

(def GradientStop
  [:map
   [:t number?]
   [:color Color]])

(def Legend
  [:or
   ;; Categorical legend
   [:map
    [:title [:or keyword? string?]]
    [:entries [:vector LegendEntry]]]
   ;; Continuous gradient legend
   [:map
    [:title [:or keyword? string?]]
    [:type [:= :continuous]]
    [:min number?]
    [:max number?]
    ;; :color-scale carries the cfg's color-scale value, which may be a
    ;; keyword (e.g. :viridis) or a map (e.g. {:type :log}); accept both.
    [:color-scale {:optional true} [:maybe [:or keyword? map?]]]
    ;; :scale-type and :ticks are added by the legend builder when the
    ;; color scale is log (build-fill-fallback-legend, plan.clj). Both
    ;; need to be declared optional so the plan validates.
    [:scale-type {:optional true} [:enum :linear :log]]
    [:ticks {:optional true} [:vector [:map [:value number?] [:t number?]]]]
    [:stops [:vector GradientStop]]]])

(def SizeLegendEntry
  [:map
   [:value number?]
   [:radius number?]])

(def SizeLegend
  [:map
   [:title [:or keyword? string?]]
   [:type [:= :size]]
   [:min number?]
   [:max number?]
   [:entries [:vector SizeLegendEntry]]])

(def AlphaLegendEntry
  [:map
   [:value number?]
   [:alpha number?]])

(def AlphaLegend
  [:map
   [:title [:or keyword? string?]]
   [:type [:= :alpha]]
   [:min number?]
   [:max number?]
   [:entries [:vector AlphaLegendEntry]]])

(def ShapeLegendEntry
  [:map
   [:label string?]
   [:shape keyword?]])

(def ShapeLegend
  [:map
   [:title [:or keyword? string?]]
   [:type [:= :shape]]
   [:entries [:vector ShapeLegendEntry]]])

;; ---- Layout ----

(def Layout
  [:map
   [:x-label-pad number?]
   [:y-label-pad number?]
   [:title-pad number?]
   [:subtitle-pad number?]
   [:caption-pad number?]
   [:legend-w number?]
   [:legend-h number?]
   [:strip-h number?]
   [:strip-w number?]])

;; ---- Plan (top-level) ----

(def LeafPlanSchema
  "A fully resolved leaf plan -- the geometry for a single
   composable pose with one or more panels (one per facet variant).
   Data-space geometry, no membrane types, no datasets.
   Numeric arrays (xs, ys, etc.) may be dtype-next buffers.
   Canonical contract for the `Plan` defrecord in `impl/resolve.clj`."
  [:map
   [:width pos-int?]
   [:height pos-int?]
   [:margin number?]
   [:total-width number?]
   [:total-height number?]
   [:panel-width number?]
   [:panel-height number?]
   [:grid [:map [:rows pos-int?] [:cols pos-int?]]]
   [:layout-type [:enum :single :facet-grid :multi-variable]]
   [:title {:optional true} [:maybe string?]]
   [:subtitle {:optional true} [:maybe string?]]
   [:caption {:optional true} [:maybe string?]]
   [:x-label {:optional true} [:maybe string?]]
   [:y-label {:optional true} [:maybe string?]]
   [:legend {:optional true} [:maybe Legend]]
   [:size-legend {:optional true} [:maybe SizeLegend]]
   [:alpha-legend {:optional true} [:maybe AlphaLegend]]
   [:shape-legend {:optional true} [:maybe ShapeLegend]]
   [:legend-position [:enum :right :bottom :top :none]]
   [:panels [:vector Panel]]
   [:layout Layout]
   [:tooltip {:optional true} [:maybe boolean?]]])

(def Rect
  "A pixel rectangle as `[x y w h]`."
  [:vector {:min 4 :max 4} number?])

(def SubPlot
  "One entry in a CompositePlan's :sub-plots: a leaf plan placed at a
   pose-tree path with its rect inside the composite."
  [:map
   [:path [:vector int?]]
   [:rect Rect]
   [:plan LeafPlanSchema]])

(def CompositeChrome
  "Resolved chrome geometry on a CompositePlan. All inputs the
   plan->membrane CompositePlan defmethod needs to render -- title,
   grid-rect, strip labels and dimensions, shared-legend spec, and
   the per-leaf rect map."
  [:map
   [:title {:optional true} [:maybe string?]]
   [:title-band-h number?]
   [:grid-rect Rect]
   [:legend-w number?]
   [:strip-h number?]
   [:strip-w number?]
   [:col-labels [:vector any?]]
   [:row-labels [:vector any?]]
   [:n-cols int?]
   [:n-rows int?]
   [:matrix? boolean?]
   [:shared-legend {:optional true}
    [:maybe [:map
             [:legend {:optional true} [:maybe Legend]]
             [:size-legend {:optional true} [:maybe SizeLegend]]
             [:alpha-legend {:optional true} [:maybe AlphaLegend]]
             [:shape-legend {:optional true} [:maybe ShapeLegend]]]]]
   [:layout [:map-of [:vector int?] Rect]]])

(def CompositePlanSchema
  "A fully resolved composite plan. Tiles per-leaf plans by rect with
   shared chrome (title band, strip labels, shared legend) on top.
   Canonical contract for the `CompositePlan` defrecord in
   `impl/resolve.clj`."
  [:map
   [:width pos-int?]
   [:height pos-int?]
   [:sub-plots [:vector SubPlot]]
   [:chrome CompositeChrome]
   [:composite? boolean?]
   [:total-width pos-int?]
   [:total-height pos-int?]
   [:title {:optional true} [:maybe string?]]])

(def PlanSchema
  "Top-level plan schema -- accepts either shape."
  [:or LeafPlanSchema CompositePlanSchema])

;; ---- Validation Helpers ----

(defn valid?
  "Check if a plan is valid (leaf or composite)."
  [plan]
  (m/validate PlanSchema plan))

(defn explain
  "Explain why a plan is invalid."
  [plan]
  (m/explain PlanSchema plan))
