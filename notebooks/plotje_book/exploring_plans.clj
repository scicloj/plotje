;; # Exploring Plans
;;
;; The [Architecture](./plotje_book.architecture.html#pipeline-overview) chapter
;; introduced the five-stage pipeline that turns a pose into a
;; rendered plot. This notebook zooms into one of those stages --
;; the **plan** -- walking through it step by step to build
;; intuition for what `pj/plan` produces for different poses.
;;
;; You would explore plans when:
;;
;; - **Debugging** -- a plot looks wrong and you want to see what data
;;   the renderer received (domains, groups, tick values)
;; - **Building a custom renderer** -- you need to understand the plan
;;   structure to consume it
;; - **Validating plans** -- you want to assert plan properties in tests

(ns plotje-book.exploring-plans
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## A Minimal Scatter Plot
;;
;; Let's start with the simplest plot: 5 points, no color,
;; no title.

(def tiny {:x [1 2 3 4 5]
           :y [2 4 1 5 3]})

;; Here is the rendered plot:

(-> tiny
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:points s)))))])

;; And here is the plan -- the data structure that drives the rendering.
;; We'll use `pj/plan` with the same pose:

(def tiny-plan (-> tiny
                   (pj/lay-point :x :y)
                   pj/plan))

;; ### What's in a plan?
;;
;; At the top level, a plan describes dimensions and layout.
;; Here is the entire plan -- a plain Clojure map:

tiny-plan

(kind/test-last [(fn [m] (and (= 600 (:width m))
                              (= 400 (:height m))
                              (== 10 (:margin m))
                              (nil? (:title m))
                              (= "x" (:x-label m))
                              (= "y" (:y-label m))
                              (nil? (:legend m))))])

;; Notice:
;;
;; - Dimensions are 600x400 drawing units, with a margin of 10
;; - Labels `"x"` and `"y"` are inferred from column names
;; - No legend (we didn't map a column to color)
;; - One panel with `:x-domain`, `:y-domain`, ticks, and layers

;; ### The panel
;;
;; The plan contains one or more panels. A simple plot has one panel;
;; faceting and SPLOM (scatter plot matrix) produce multiple. Each panel holds its own data space:

(def tiny-panel (first (:panels tiny-plan)))

(keys tiny-panel)

(kind/test-last [(fn [ks] (every? (set ks) [:x-domain :y-domain :layers]))])

;; **Domains** -- the numeric range of the data, with a small padding:

(:x-domain tiny-panel)

(kind/test-last [(fn [d] (and (<= (first d) 1) (>= (second d) 5)))])

(:y-domain tiny-panel)

(kind/test-last [(fn [d] (and (<= (first d) 1) (>= (second d) 5)))])

;; **Scale specs** -- what kind of scale to use:

(:x-scale tiny-panel)

(kind/test-last [(fn [s] (= :linear (:type s)))])

;; **Ticks** -- pre-computed tick positions and their text labels:

(:x-ticks tiny-panel)

(kind/test-last [(fn [t] (and (vector? (:values t))
                              (vector? (:labels t))
                              (= (count (:values t)) (count (:labels t)))))])

;; These are the actual numbers that will appear on the axis.
;; They are in data space -- not drawing units.

;; ### The layer
;;
;; Each layer in the pose produces one plan-level layer entry. Our
;; scatter has a single point layer:

(def tiny-layer (first (:layers tiny-panel)))

tiny-layer

(kind/test-last [(fn [m] (= :point (:mark m)))])

;; The style gives rendering hints (opacity, radius) but the geometry
;; is in the **groups**. Without a color mapping, there is one group:

(count (:groups tiny-layer))

(kind/test-last [(fn [n] (= 1 n))])

;; The group contains the actual data -- x/y coordinates in data space,
;; plus a resolved RGBA color:

(first (:groups tiny-layer))

(kind/test-last [(fn [g] (and (= 4 (count (:color g)))
                              (= [1 2 3 4 5] (mapv int (:xs g)))
                              (= [2 4 1 5 3] (mapv int (:ys g)))))])

;; These are the original data values -- not drawing units.
;; The renderer maps them through scales to get drawing-unit coordinates.
;;
;; In other words, the plan describes geometry in data space.

;; ## Adding Color
;;
;; When we map a column to color, the plan splits data into groups
;; and adds a legend.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

(def iris-plan (-> (rdatasets/datasets-iris)
                   (pj/lay-point :sepal-length :sepal-width {:color :species})
                   pj/plan))

;; Here is the full plan -- notice the legend and three groups:

iris-plan

(kind/test-last [(fn [m] (and (= 3 (count (:entries (:legend m))))
                              (= 1 (count (:panels m)))))])

;; Now we have three groups -- one per species:

(def iris-layer (-> iris-plan :panels first :layers first))

(count (:groups iris-layer))

(kind/test-last [(fn [n] (= 3 n))])

;; Each group has its own resolved color and a subset of the data:

(mapv (fn [g]
        {:color (:color g)
         :n-points (count (:xs g))})
      (:groups iris-layer))

(kind/test-last [(fn [gs] (and (= 3 (count gs))
                               (every? #(= 50 (:n-points %)) gs)))])

;; The legend describes the color mapping:

(:legend iris-plan)

(kind/test-last [(fn [leg] (= 3 (count (:entries leg))))])

;; Colors are resolved to `[r g b a]` vectors -- no symbolic references.
;; The same color appears in both the layer groups and the legend entries.

;; ### Continuous Color
;;
;; When `:color` maps to a **numeric** column, the plan stores
;; per-point colors and a continuous gradient legend.

(def cont-plan (-> (rdatasets/datasets-iris)
                   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
                   pj/plan))

(:legend cont-plan)

(kind/test-last [(fn [m] (= :continuous (:type m)))])

;; The legend has pre-computed gradient stops -- no functions:

(select-keys (:legend cont-plan) [:title :type :min :max :color-range])

(kind/test-last [(fn [m] (and (= :continuous (:type m))
                              (not (contains? m :gradient-fn))))])

;; Twenty evenly spaced stops store the gradient colors:

(count (:stops (:legend cont-plan)))

(kind/test-last [(fn [n] (= 20 n))])

;; ## Histograms
;;
;; A histogram computes bins from the data. The plan stores the
;; bin edges and counts -- still in data space.

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

(def hist-plan (-> (rdatasets/datasets-iris)
                   (pj/lay-histogram :sepal-length)
                   pj/plan))

hist-plan

(kind/test-last [(fn [m] (= 1 (count (:panels m))))])

(def hist-layer (-> hist-plan :panels first :layers first))

(:mark hist-layer)

(kind/test-last [(fn [m] (= :bar m))])

;; The geometry is in `:bars` -- each bin has a lo edge, hi edge, and count:

(-> hist-layer :groups first :bars)

(kind/test-last [(fn [bars] (and (> (count bars) 3)
                                 (every? #(< (:lo %) (:hi %)) bars)
                                 (every? #(pos? (:count %)) bars)))])

;; The renderer will draw a rectangle from `(lo, 0)` to `(hi, count)`
;; in data space, then map it through the scales into drawing space.

;; ## Categorical Bars
;;
;; A bar chart counts occurrences of each category. The plan records
;; the categories and counts per group.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

(def bar-plan (-> (rdatasets/palmerpenguins-penguins)
                  (pj/lay-bar :island {:color :species})
                  pj/plan))

(def bar-layer (-> bar-plan :panels first :layers first))

;; The mark type is `:rect` and the layer carries the categories:

bar-layer

(kind/test-last [(fn [m] (and (= :rect (:mark m))
                              (= :dodge (:position m))
                              (= 3 (count (:categories m)))))])

;; Each group (one per color) has counts for every category:

(mapv (fn [g]
        {:label (:label g)
         :counts (:counts g)})
      (:groups bar-layer))

(kind/test-last [(fn [gs] (= 3 (count gs)))])

;; The `:position` field (`:dodge` or `:stack`) controls how
;; multiple groups arrange within each category.

;; ## Stacked Bars
;;
;; The same data with `{:position :stack}` piles the species on top of
;; one another instead of setting them side by side:

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:position :stack :color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                ;; One rectangle per species that the
                                ;; island has, not one per island.
                                (= 5 (:polygons s)))))])

;; In the plan that difference is one field:

(def stacked-plan (-> (rdatasets/palmerpenguins-penguins)
                      (pj/lay-bar :island {:position :stack :color :species})
                      pj/plan))

(def stacked-layer (-> stacked-plan :panels first :layers first))

(:position stacked-layer)

(kind/test-last [(fn [p] (= :stack p))])

;; The counts are the same -- only the rendering instruction differs.
;; The plan describes *what* to draw; the rendering stage handles *how*.

;; ## Regression Lines
;;
;; A regression produces line segments in data space.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

(def lm-plan (-> (rdatasets/datasets-iris)
                 (pj/lay-point :sepal-length :sepal-width)
                 (pj/lay-smooth {:stat :linear-model})
                 pj/plan))

;; Two layers -- points and line:

(mapv :mark (-> lm-plan :panels first :layers))
(kind/test-last [(fn [marks] (= [:point :line] marks))])
(def lm-layer (-> lm-plan :panels first :layers second))

;; Its group has endpoints -- a line segment in data space:

(first (:groups lm-layer))

(kind/test-last [(fn [m] (and (< (:x1 m) (:x2 m))
                              (number? (:x1 m))
                              (number? (:y2 m))))])

;; The renderer maps these two points through scales to get a
;; rendered line segment.

;; ## Per-Group Regression
;;
;; When both points and regression have a color mapping, the line
;; layer gets one segment per group:

(-> (rdatasets/datasets-iris)
    (pj/pose :petal-length :petal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])
(def grp-plan (-> (rdatasets/datasets-iris)
                  (pj/pose :petal-length :petal-width {:color :species})
                  pj/lay-point
                  (pj/lay-smooth {:stat :linear-model})
                  pj/plan))

(let [line-layer (-> grp-plan :panels first :layers second)]
  (mapv (fn [g]
          {:color (:color g)
           :x1 (some-> (:x1 g) (Math/round) int)
           :x2 (some-> (:x2 g) (Math/round) int)})
        (:groups line-layer)))

(kind/test-last [(fn [gs] (= 3 (count gs)))])

;; Three line segments, each with its own color -- one per species.

;; ## Connected Lines (Polylines)
;;
;; Line marks from identity data (not regression) store xs/ys vectors:

(def wave {:x (range 30)
           :y (map #(Math/sin (* % 0.3)) (range 30))})

(-> wave
    (pj/lay-line :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1 (:lines s)))))])

(def wave-plan (-> wave
                   (pj/lay-line :x :y)
                   pj/plan))

(def wave-group (-> wave-plan :panels first :layers first :groups first))

{:n-points (count (:xs wave-group))
 :first-x (first (:xs wave-group))
 :last-x (last (:xs wave-group))}

(kind/test-last [(fn [m] (= 30 (:n-points m)))])

;; The renderer connects these points in order to draw a polyline.

;; ## Value Bars
;;
;; Value bars map a categorical axis to a numeric value without any
;; counting. The plan stores the raw x/y pairs:

(def sales {:product [:widget :gadget :gizmo :doohickey]
            :revenue [120 340 210 95]})

(-> sales
    (pj/lay-bar :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:polygons s)))))])

(def sales-plan (-> sales
                    (pj/lay-bar :product :revenue)
                    pj/plan))

(let [g (-> sales-plan :panels first :layers first :groups first)]
  {:xs (:xs g)
   :ys (:ys g)})

(kind/test-last [(fn [m] (= 4 (count (:xs m))))])

;; ## Flipped Coordinates
;;
;; Setting `:coord :flip` swaps x and y in the plan's panel:

(def flip-plan (-> (rdatasets/datasets-iris)
                   (pj/lay-bar :species)
                   (pj/coord :flip)
                   pj/plan))

(:coord (first (:panels flip-plan)))

(kind/test-last [(fn [c] (= :flip c))])

;; The domains are swapped -- the categorical axis is now y:

(let [p (first (:panels flip-plan))]
  {:x-domain-type (if (number? (first (:x-domain p))) :numeric :categorical)
   :y-domain-type (if (number? (first (:y-domain p))) :numeric :categorical)})

(kind/test-last [(fn [m] (and (= :numeric (:x-domain-type m))
                              (= :categorical (:y-domain-type m))))])

;; The layer data is unchanged -- the coord type signals an axis
;; swap during mapping.

;; ## Options Affect the Plan
;;
;; Title, labels, and dimensions are recorded in the plan:

(def opts-plan (-> (rdatasets/datasets-iris)
                   (pj/lay-point :sepal-length :sepal-width)
                   (pj/plan {:title "My Custom Title"
                             :x-label "Length (cm)"
                             :y-label "Width (cm)"
                             :width 800
                             :height 300})))

opts-plan

(kind/test-last [(fn [m] (and (= "My Custom Title" (:title m))
                              (= 800 (:width m))
                              (= 300 (:height m))))])

;; The layout records how much space to reserve for each label:

(:layout opts-plan)

(kind/test-last [(fn [lay] (and (pos? (:title-pad lay))
                                (pos? (:x-label-pad lay))
                                (pos? (:y-label-pad lay))))])

;; ## Plan vs Plot -- Side by Side
;;
;; `pj/plan` and `pj/plot` accept the same pose.
;; `pj/plan` returns the intermediate data map; `pj/plot` returns the final SVG.

;; The plan (a plain Clojure map):

(def final-pose
  (-> (rdatasets/datasets-iris)
      (pj/pose :petal-length :petal-width {:color :species})
      pj/lay-point
      (pj/lay-smooth {:stat :linear-model})))

(def final-plan (pj/plan final-pose {:title "Iris Petals"}))

final-plan

(kind/test-last [(fn [m] (= "Iris Petals" (:title m)))])

;; Layer summary:

(mapv (fn [l]
        {:mark (:mark l)
         :n-groups (count (:groups l))})
      (-> final-plan :panels first :layers))

(kind/test-last [(fn [ls] (= 2 (count ls)))])

;; The rendered plot (SVG):

(-> final-pose (pj/options {:title "Iris Petals"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; ## Multi-Panel Plans
;;
;; Faceting produces plans with multiple panels. Each panel has
;; its own domains, ticks, and layers, plus grid positioning.

(def faceted-plan
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/facet :species)
      pj/plan))

;; The grid records the layout:

(:grid faceted-plan)

(kind/test-last [(fn [g] (and (= 1 (:rows g)) (= 3 (:cols g))))])

;; Three panels -- one per species:

(count (:panels faceted-plan))

(kind/test-last [(fn [n] (= 3 n))])

;; Each panel has a grid position and strip label:

(:panels faceted-plan)

(kind/test-last [(fn [ps] (and (= 3 (count ps))
                               (every? :col-label ps)))])

;; Panel-level domains show the data range for each subset:

(:panels faceted-plan)

(kind/test-last [(fn [ps] (every? :x-domain ps))])

;; With shared scales (the default), all panels have the same
;; domains. The faceted plan above uses the default, so its panels
;; share one y-domain:

(mapv :y-domain (:panels faceted-plan))

(kind/test-last [(fn [ds] (apply = ds))])

;; With `:scales :free-y`, each panel gets its own y-domain:

(def free-y-plan
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/facet :species)
      (pj/options {:scales :free-y})
      pj/plan))

(mapv :y-domain (:panels free-y-plan))

(kind/test-last [(fn [ds] (not (apply = ds)))])

;; The plan also records the panel grid and the overall dimensions,
;; in drawing units:

(select-keys faceted-plan [:layout-type :grid :total-width :total-height])

(kind/test-last [(fn [m] (= :facet-grid (:layout-type m)))])

;; Multi-panel plans validate against the same Malli schema:

(pj/valid-plan? faceted-plan)

(kind/test-last [true?])

;; ## Malli Validation
;;
;; Every plan conforms to a Malli schema. Validation runs automatically
;; when `pj/plan` is called (default `:validate true`).
;; Pass `{:validate false}` to skip it.
;;
;; You can also check manually with `pj/valid-plan?`:

(pj/valid-plan? tiny-plan)

(kind/test-last [true?])

(pj/valid-plan? iris-plan)

(kind/test-last [true?])

(pj/valid-plan? hist-plan)

(kind/test-last [true?])

(pj/valid-plan? bar-plan)

(kind/test-last [true?])

(pj/valid-plan? lm-plan)

(kind/test-last [true?])

(pj/valid-plan? final-plan)

(kind/test-last [true?])

;; When a plan is invalid, `pj/explain-plan` shows which part failed:

(pj/explain-plan (assoc tiny-plan :width "not-a-number"))

(kind/test-last [some?])

;; ## Data Types
;;
;; Plans are plain inspectable data -- maps, numbers, strings,
;; keywords, and dtype-next buffers for numeric arrays (`:xs`,
;; `:ys`, etc.). The buffers support `nth`, `count`, `seq`, and
;; standard sequence operations.

(-> tiny-plan :panels first :layers first :groups first :xs type)

(kind/test-last [(fn [t] (not= clojure.lang.PersistentVector t))])

;; You can convert any numeric buffer to a plain vector with `vec`:

(-> tiny-plan :panels first :layers first :groups first :xs vec)

(kind/test-last [(fn [v] (and (vector? v) (number? (first v))))])

;; ## What's Next
;;
;; - [**Extensibility**](./plotje_book.extensibility.html) -- add custom marks, stats, and renderers
