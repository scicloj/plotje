;; # Panels
;;
;; A plot can be one set of axes or a grid of them, and which you get is
;; never chosen directly. A leaf pose draws one panel, however many
;; layers it holds and whatever columns they name. More panels come from
;; `pj/facet` and `pj/arrange`.
;;
;; Two things to compare are either two columns or two categories of
;; one column, and they are drawn either inside one panel or in panels
;; of their own. The table names the form that draws each combination:
;;
;; | | inside one panel | across panels |
;; |:--|:--|:--|
;; | two columns | a series, `{:x [:height :depth]}` | a facet over columns, `(pj/facet pose {:x [...]})`, or `pj/arrange` |
;; | two categories | an aesthetic, `{:color :species}` | a facet over categories, `(pj/facet pose :species)` |
;;
;; A series is drawn by pivoting its columns into a key column,
;; `:series`, mapped to `:color`, so two columns on one panel are drawn
;; as two categories are: a color and a legend entry each.
;; [Series](./plotje_book.series.html) teaches the series, and
;; [Color and Grouping](./plotje_book.core_concepts.html#color-and-grouping)
;; the `:color` aesthetic. This chapter shows the forms in the
;; across-panels column of the table, and how they combine.
;; [Faceting](./plotje_book.faceting.html) and
;; [Composition](./plotje_book.composition.html) teach each in depth.
;;
;; Read [Poses](./plotje_book.pose_model.html) first for what a leaf is.

(ns plotje-book.panels
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

(def measurements
  {:height  [1 2 3 4]
   :weight  [1 2 3 4]
   :depth   [1.6 2.3 3.4 4.7]
   :species ["a" "a" "b" "b"]})

;; ## The panel of a leaf pose

;; A pose built from one dataset with one mapping is a single leaf, and
;; draws a single panel:

(-> measurements
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; Adding a layer does not add a panel. The layers of a pose share its
;; axes and are drawn over one another:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-line :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; A layer naming other columns adds no panel either. The layer's
;; columns are drawn against the axes the panel already has, each axis
;; covers every column drawn on it and is titled by all of them, and
;; each layer takes a colour and a legend entry naming its columns:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               plan (pj/plan v)
                               [lo hi] (:x-domain (first (:panels plan)))]
                           (and (= 1 (:panels s))
                                (= "height, depth" (:x-label plan))
                                ;; :height runs 1-4 and :depth 1.6-4.7
                                (< lo 1) (> hi 4.7)
                                (= ["height" "depth"]
                                   (mapv :label (:entries (:legend plan)))))))])

;; How the colour of each layer is decided -- a colour it specifies, a
;; `:color` on the pose, or a layer coloured by a column -- is in
;; [Pose Rules](./plotje_book.pose_rules.html#rule-lp5-the-layers-of-a-leaf-share-its-panel),
;; and [Inference Rules](./plotje_book.inference_rules.html#multi-layer-plots)
;; shows a colour specified on each layer and a layer coloured by a
;; column.

;; ### The panel's columns

;; A `pj/lay-*` call keeps its columns on its own layer, so a pose built
;; by such calls has no mapping of its own. The panel still has columns:
;; on each axis, the column the pose names, and where the pose names
;; none there, the column of the first layer that names one. A layer
;; that names no columns reads both of the panel's columns:

(-> measurements
    (pj/lay-point :height :weight)
    pj/lay-smooth)

(kind/test-last [(fn [v] (= [:height :weight]
                            ((juxt :x :y) (second (:layers (pj/draft v))))))])

;; The layer that names no columns may be written first. A `pj/lay-*`
;; call does not read the data's columns when it is called; the layer
;; is given its columns when the pose is drawn, so a later call may
;; name them:

(-> measurements
    pj/lay-smooth
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= [[:height :weight] [:height :weight]]
                            (mapv (juxt :x :y) (:layers (pj/draft v)))))])

;; A layer that names one column reads the panel's column on the other
;; axis, where its layer type draws from two columns. The line below
;; names `:depth` on `:y` and is drawn against the panel's `:height`:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-line {:y :depth}))

(kind/test-last [(fn [v] (and (= [:height :depth]
                                 ((juxt :x :y) (second (:layers (pj/draft v)))))
                              (some #{"weight, depth"} (:texts (pj/svg-summary v)))))])

;; A layer type that can draw from one column is not given a second
;; column. A rug draws from one column, so the rug below marks `:depth`
;; along the `:x` axis and reads no `:y`:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-rug {:x :depth}))

(kind/test-last [(fn [v] (let [rug (second (:layers (pj/draft v)))]
                           (and (= :depth (:x rug))
                                (nil? (:y rug)))))])

;; ## A panel per column

;; To draw the two columns apart, split the pose over them with
;; `pj/facet`. Each layer naming one of the columns is drawn only in
;; that column's panel:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight)
    (pj/facet {:x [:height :depth]}))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; The cells of a facet over columns are made from the panel's columns,
;; and the facet replaces the column on the axis it names. Below, the
;; point layer names `:weight`, so it is drawn in the `:weight` panel.
;; The `:depth` panel keeps the panel's `:height` on `:x`. No layer
;; names `:depth`, so that panel draws the layer type inferred from
;; `:height` and `:depth`, which is a scatter too:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/facet {:y [:weight :depth]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= {:panels 2 :points 8}
                                   (select-keys s [:panels :points]))
                                ;; each panel titled by its own column on :y,
                                ;; and the shared :x titled once
                                (= ["weight" "depth" "height"]
                                   (filterv #{"weight" "depth" "height"} (:texts s))))))])

;; Where one layer is meant for every column, name the columns in the
;; facet alone. The panels share the axis they agree about, `:weight`,
;; and title it once:

(-> measurements
    (pj/pose {:y :weight})
    pj/lay-point
    (pj/facet {:x [:height :depth]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 1 (count (filter #{"weight"} (:texts s)))))))])

;; ## A matrix of columns

;; A facet over columns can cross a list of columns with itself:
;; `{:col {:x columns} :row {:y columns}}` draws a panel for every pair
;; of columns, the columns across drawn on `:x` and the same columns down
;; drawn on `:y`. A layer added after it is drawn in the panels whose
;; columns it draws, and a panel with the same column on both axes draws
;; a histogram:

(-> measurements
    (pj/facet {:col {:x [:height :weight :depth]}
              :row {:y [:height :weight :depth]}})
    pj/lay-point)

(kind/test-last [(fn [v] (and (= {:col {:x [:height :weight :depth]}
                                  :row {:y [:height :weight :depth]}}
                                 (:facet v))
                              (= 9 (:panels (pj/svg-summary v)))
                              ;; bars on the diagonal, points elsewhere
                              (= [:bar :point :point :point :bar :point :point :point :bar]
                                 (mapv #(:mark (first (:layers (first (:panels (:plan %))))))
                                       (:sub-plots (pj/plan v))))))])

;; `pj/matrix` writes that facet from the list of columns:

(-> measurements
    (pj/matrix [:height :weight :depth])
    pj/lay-point)

(kind/test-last [(fn [v] (= v (-> measurements
                                  (pj/facet {:col {:x [:height :weight :depth]}
                                             :row {:y [:height :weight :depth]}})
                                  pj/lay-point)))])

;; ## A panel per category

;; A facet over the categories of a column draws one panel per category,
;; each drawing the rows of its category. Two species give two panels:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/facet :species))

(kind/test-last [(fn [v] (let [panels (:panels (pj/plan v))
                               [lo hi] (:x-domain (first panels))]
                           (and (= 2 (:panels (pj/svg-summary v)))
                                (apply = (map :x-domain panels))
                                (apply = (map :y-domain panels))
                                (< lo 1) (> hi 4))))])

;; The panels share both axes, so the categories can be read against
;; each other: species "a" spans 1 to 2 on `:height` and species "b" 3
;; to 4, and both panels run from below 1 to above 4.

;; ## Arranging poses side by side

;; `pj/arrange` takes finished poses and makes each one a cell of a
;; single composite:

(pj/arrange [(pj/lay-point measurements :height :weight)
             (pj/lay-point measurements :depth :weight)])

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 2 (count (filter #{"weight"} (:texts s)))))))])

;; Each pose above carries the dataset, so each cell computes its own
;; axes and titles `weight` beside its own panel. Given the dataset
;; first, `pj/arrange` makes its cells parts of one dataset. Cells that
;; draw the same column on an axis then share that axis, titled once
;; beside the grid, as the panels of a facet are:

(-> (pj/arrange measurements [{:x :height :y :weight}
                              {:x :depth :y :weight}])
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 1 (count (filter #{"weight"} (:texts s)))))))])

;; [Composition](./plotje_book.composition.html#shared-scales) gives the
;; rule for which cells share an axis, and how `:share-scales` changes it.

;; A `pj/lay-*` call on an arrangement places its layer by the columns
;; it names. In the arrangement above, `pj/lay-point` names no columns,
;; so it is **added at the root** of the composite and drawn in every
;; cell. A call naming columns is added at the root too, and is
;; **drawn in every cell whose panel draws those columns**; where no
;; cell's panel draws them, the call appends a new cell. Here the line
;; of `:weight` against `:depth` is drawn in the second cell, and the
;; line of `:height` against `:weight` appends a third, where the points
;; added at the root are drawn too:

(-> (pj/arrange measurements [{:x :height :y :weight}
                              {:x :depth :y :weight}])
    pj/lay-point
    (pj/lay-line :depth :weight)
    (pj/lay-line :weight :height))

(kind/test-last [(fn [v] (let [cells (:poses (first (:poses v)))]
                           (and (= [:point :line] (mapv :layer-type (:layers v)))
                                (= [[] [] [:line]]
                                   (mapv #(mapv :layer-type (:layers %)) cells))
                                (= {:panels 3 :points 12 :lines 2}
                                   (select-keys (pj/svg-summary v) [:panels :points :lines])))))])

;; A call naming one column is drawn in every cell whose panel draws no
;; other column on that axis, and reads the cell's other column, as on
;; a leaf.

;; A cell may also be a template -- a pose with layers and no data, as
;; [Reusable Pose Templates](./plotje_book.core_concepts.html#reusable-pose-templates)
;; describes. When `pj/arrange` is given data first, the template cell
;; draws from the arrangement's data. Here a cell with layers of its own
;; sits beside a cell drawn by inference:

(pj/arrange measurements [(-> (pj/lay-point nil :height :weight)
                              (pj/lay-smooth {:stat :linear-model}))
                          {:x :depth :y :weight}])

(kind/test-last [(fn [v] (= {:panels 2 :points 8 :lines 1}
                            (select-keys (pj/svg-summary v) [:panels :points :lines])))])

;; A composite pose can also be written directly, which
;; [Composition](./plotje_book.composition.html#explicit-composite-poses)
;; covers. It is the same structure `pj/arrange` builds.

;; ## Cells that filter the data

;; A cell of `pj/arrange` may hold `:where`, a map from a column to a
;; value, and draws only the rows that match. A cell is written as the
;; argument after the data in `pj/pose` is written, so the cell
;; `{:where {:species "a"}}` is read as it is read in
;; `(pj/pose nil {:where {:species "a"}})`. Cells that split one
;; dataset by the values of one column draw what `pj/facet` draws over
;; that column:

(-> measurements
    (pj/pose {:x :height :y :weight})
    (pj/arrange [{:where {:species "a"}}
                 {:where {:species "b"}}])
    pj/lay-point)

(kind/test-last [(fn [v] (= (pj/svg-summary v)
                            (pj/svg-summary (-> measurements
                                                (pj/pose {:x :height :y :weight})
                                                pj/lay-point
                                                (pj/facet :species)))))])

;; The [Facet or Arrange](./plotje_book.faceting.html#facet-or-arrange)
;; section of Faceting compares a facet with cells that filter the data,
;; and
;; [Pose Rules](./plotje_book.pose_rules.html#rule-s5-a-row-filter-combines-column-by-column-at-every-level)
;; says how a filter on the pose combines with a cell's.

;; ## Combining facets and arrangements

;; Columns and categories cross in one `pj/facet` call, the categories
;; across and the columns down. Each entry under `:col` and `:row` is one
;; of the two kinds of facet, as in the matrix above:

(-> measurements
    (pj/pose {:x :height})
    pj/lay-point
    (pj/facet {:col :species :row {:y [:weight :depth]}}))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Two `pj/facet` calls, one over the categories and one over the
;; columns, write the same grid, in either order:

(-> measurements
    (pj/pose {:x :height})
    pj/lay-point
    (pj/facet {:y [:weight :depth]})
    (pj/facet :species))

(kind/test-last [(fn [v] (let [base (-> measurements
                                        (pj/pose {:x :height})
                                        pj/lay-point)]
                           (and (= v (pj/facet base {:col :species :row {:y [:weight :depth]}}))
                                (= v (-> base
                                         (pj/facet :species)
                                         (pj/facet {:y [:weight :depth]}))))))])

;; `:col` and `:row` are aesthetics, so a facet written on an arranged
;; pose reaches every cell:

(-> (pj/arrange [(pj/pose measurements :height :weight)
                 (pj/pose measurements :depth :weight)])
    (pj/lay-point)
    (pj/facet :species))

(kind/test-last
 [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## What's Next
;;
;; - [Specifying Aesthetics](./plotje_book.specifying_aesthetics.html) --
;;   the next chapter: how to write what a mapping means -- where its
;;   values come from, and whether a scale reads them
;;
;; ## See Also
;;
;; - [Poses](./plotje_book.pose_model.html) -- leaf and composite poses
;; - [Composition](./plotje_book.composition.html) -- arranging poses,
;;   shared scales, and the patterns built from them
;; - [Faceting](./plotje_book.faceting.html) -- faceting in practice,
;;   including grids of two columns
;; - [Pose Rules](./plotje_book.pose_rules.html) -- the rules that
;;   decide where a `pj/lay-*` call's layer goes
