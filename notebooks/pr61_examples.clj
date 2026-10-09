;; # PR 61 Examples
;;
;; Timothy's API changes on branch `simplify-pose`, as understood.
;; Each pose is drawn, then printed.

(ns pr61-examples
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

(def iris3
  (tc/select-columns (rdatasets/datasets-iris) [:sepal-length :sepal-width :species]))

;; ## `pj/pose`: one pose, mapping last

;; Data only -- the pose carries no mapping; one is inferred from the
;; first columns when the pose is drawn (`pj/infer-mapping` shows it).

(def inferred (pj/pose iris3))

inferred

(kind/pprint inferred)

(kind/test-last [(fn [v] (and (nil? (:mapping v))
                              (= {:x :sepal-length :y :sepal-width :color :species}
                                 (:mapping (pj/infer-mapping v)))))])

;; One column.

(def one-column (pj/pose (rdatasets/datasets-iris) :sepal-length))

one-column

(kind/pprint one-column)

(kind/test-last [(fn [v] (= {:x :sepal-length} (:mapping v)))])

;; One column, then a mapping.

(def one-column-mapped (pj/pose (rdatasets/datasets-iris) :sepal-length {:color :species}))

one-column-mapped

(kind/pprint one-column-mapped)

(kind/test-last [(fn [v] (= {:x :sepal-length :color :species} (:mapping v)))])

;; Two columns.

(def two-columns (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width))

two-columns

(kind/pprint two-columns)

(kind/test-last [(fn [v] (= {:x :sepal-length :y :sepal-width} (:mapping v)))])

;; Two columns, then a mapping.

(def two-columns-mapped
  (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width {:color :species}))

two-columns-mapped

(kind/pprint two-columns-mapped)

(kind/test-last [(fn [v] (= {:x :sepal-length :y :sepal-width :color :species}
                            (:mapping v)))])

;; A mapping only.

(def mapping-only
  (pj/pose (rdatasets/datasets-iris) {:x :sepal-length :y :sepal-width :color :species}))

mapping-only

(kind/pprint mapping-only)

(kind/test-last [(fn [v] (= {:x :sepal-length :y :sepal-width :color :species}
                            (:mapping v)))])

;; ## `pj/pose` no longer reads a vector as panels

;; A vector in the column slot is a series on `:x`; with no layer, the
;; draft pivots it and infers one -- a histogram of the pooled values.

(pj/pose (rdatasets/datasets-iris) [:sepal-length :sepal-width])

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## `pj/pose` on a pose

;; Promotion is replaced on `pr61-unify` -- see `pr61_unify_examples.clj`.

;; ## `pj/cross-matrix`: the matrix of plots

;; Columns crossed with themselves.

(def matrix-3
  (pj/cross-matrix (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length]))

matrix-3

(kind/pprint matrix-3)

(kind/test-last [(fn [v] (= 9 (:panels (pj/svg-summary v))))])

;; With a mapping. Sharing, strip labels and suppressed ticks are written
;; into the pose when it is built.

(def matrix-mapped
  (pj/cross-matrix (rdatasets/datasets-iris) [:sepal-length :sepal-width] {:color :species}))

matrix-mapped

(kind/pprint matrix-mapped)

(kind/test-last [(fn [v] (and (= 4 (:panels (pj/svg-summary v)))
                              (= #{:x :y} (get-in v [:opts :share-scales]))
                              (= ["sepal-length" "sepal-width"]
                                 (get-in v [:grid-strip-labels :col-labels]))))])

;; Two lists of columns: the first across, the second down.

(def matrix-2x2
  (pj/cross-matrix (rdatasets/datasets-iris)
                   [:sepal-length :sepal-width]
                   [:petal-length :petal-width]))

matrix-2x2

(kind/pprint matrix-2x2)

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Two lists, then a mapping.
;; Observed: the diagonal cell draws a histogram; strips keep the hyphen
;; (`sepal-length`) where axis titles drop it.

(def matrix-2x3
  (pj/cross-matrix (rdatasets/datasets-iris)
                   [:sepal-length :sepal-width]
                   [:petal-length :petal-width :sepal-length]
                   {:color :species}))

matrix-2x3

(kind/pprint matrix-2x3)

(kind/test-last [(fn [v] (= 6 (:panels (pj/svg-summary v))))])

;; Every column of the data.

(def matrix-all
  (-> (rdatasets/datasets-iris)
      (tc/select-columns [:sepal-length :sepal-width :petal-length])
      pj/cross-matrix))

matrix-all

(kind/pprint matrix-all)

(kind/test-last [(fn [v] (= 9 (:panels (pj/svg-summary v))))])

;; A layer added afterwards goes on the root and reaches every cell.

(def matrix-points
  (-> (rdatasets/datasets-iris)
      (pj/cross-matrix [:sepal-length :sepal-width] [:petal-length :petal-width])
      pj/lay-point))

matrix-points

(kind/pprint matrix-points)

(kind/test-last [(fn [v] (and (= 4 (:panels (pj/svg-summary v)))
                              (= [{:layer-type :point}] (:layers v))))])

;; ## `pj/arrange` with column shorthand

;; A column is a one-column panel.

(def arranged-columns
  (pj/arrange (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length]))

arranged-columns

(kind/pprint arranged-columns)

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; A pair of columns is a two-column panel.

(def arranged-pairs
  (pj/arrange (rdatasets/datasets-iris)
              [[:sepal-length :sepal-width] [:petal-length :petal-width]]))

arranged-pairs

(kind/pprint arranged-pairs)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Options last.

(def arranged-two-columns
  (pj/arrange (rdatasets/datasets-iris)
              [:sepal-length :sepal-width :petal-length :petal-width]
              {:cols 2}))

arranged-two-columns

(kind/pprint arranged-two-columns)

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Layers added afterwards reach every cell.

(def arranged-densities
  (-> (rdatasets/datasets-iris)
      (pj/arrange [:sepal-length :sepal-width :petal-length])
      (pj/lay-density {:color :species})))

arranged-densities

(kind/pprint arranged-densities)

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; ## `pj/arrange` from a pose

;; The pose's data, mapping and layers go on the root and reach every cell.

(def arranged-from-pose
  (-> (rdatasets/datasets-iris)
      (pj/pose {:color :species})
      pj/lay-point
      (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])))

arranged-from-pose

(kind/pprint arranged-from-pose)

(kind/test-last [(fn [v] (and (= {:panels 2 :points 300}
                                 (select-keys (pj/svg-summary v) [:panels :points]))
                              (= {:color :species} (:mapping v))))])

;; On a composite, the cells are appended.

(def arranged-twice
  (-> (rdatasets/datasets-iris)
      (pj/arrange [[:sepal-length :sepal-width]])
      (pj/arrange [[:petal-length :petal-width]])
      pj/lay-point))

arranged-twice

(kind/pprint arranged-twice)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; On a composite, options are an error.

(try
  (-> (rdatasets/datasets-iris)
      (pj/arrange [[:sepal-length :sepal-width]])
      (pj/arrange [[:petal-length :petal-width]] {:cols 1}))
  (catch Exception e (ex-message e)))

(kind/test-last [(fn [s] (and (string? s) (re-find #"not used" s)))])

;; ## Mixed cells

;; A pose extended by a column, a pair, and a nested arrangement.
;; Observed: the first cell's legend narrows its panel.

(def sepal-scatter
  (-> (rdatasets/datasets-iris)
      (pj/pose {:color :species})
      pj/lay-point))

(def mixed
  (pj/arrange (rdatasets/datasets-iris)
              [[sepal-scatter :sepal-length :sepal-width]
               [:petal-length :petal-width]
               [[:sepal-length :petal-length]]]))

mixed

(kind/pprint mixed)

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; Poses as cells, unchanged from before.

(def two-poses
  (pj/arrange [(pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
               (pj/lay-histogram (rdatasets/datasets-iris) :petal-length)]))

two-poses

(kind/pprint two-poses)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ## `lay-*` with a vector of columns

;; A series: one panel, a density per column. The vector is written on
;; the pose's mapping, as `pj/pose` writes it; the draft pivots it into
;; long data read as `{:x :value :color :series}`.

(def density-series
  (pj/lay-density (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length]))

density-series

(kind/pprint density-series)

(kind/test-last [(fn [v] (and (= 1 (:panels (pj/svg-summary v)))
                              (= {:x [:sepal-length :sepal-width :petal-length]}
                                 (:mapping v))
                              (let [l (-> v pj/draft :layers first)]
                                (and (= [:value :series] [(:x l) (:color l)])
                                     (= 450 (tc/row-count (:data l)))))))])

;; With a written `:color`, the colour follows that column; the columns are pooled.

(def density-series-colored
  (pj/lay-density (rdatasets/datasets-iris)
                  [:sepal-length :sepal-width :petal-length]
                  {:color :species}))

density-series-colored

(kind/pprint density-series-colored)

(kind/test-last [(fn [v] (and (= {:panels 1 :polygons 3}
                                 (select-keys (pj/svg-summary v) [:panels :polygons]))
                              (= :species (-> v pj/plan :legend :title))))])

;; ## Where things live in the pose

;; A leaf pose: `:data`, `:mapping`, `:layers`, `:opts`.
;; A composite pose: also `:poses` and `:layout`.

;; A `lay-*` call writes its columns into its layer's `:mapping`, beside
;; the layer's own aesthetics and settings such as `:bins`; the pose's
;; `:mapping` is what `pj/pose` writes. `:position` and `:stat` sit on
;; the layer itself.
;; Observed: `:position :stack` on this histogram draws the same as without
;; it, on 0.16.1 too.

(def where-layer-things-go
  (pj/lay-histogram (rdatasets/datasets-iris)
                    :sepal-length
                    {:bins 8 :alpha 0.5 :color :species :position :stack}))

where-layer-things-go

(kind/pprint where-layer-things-go)

(kind/test-last [(fn [v] (and (nil? (:mapping v))
                              (= {:x :sepal-length :bins 8 :alpha 0.5 :color :species}
                                 (get-in v [:layers 0 :mapping]))
                              (= :stack (get-in v [:layers 0 :position]))
                              ;; Records the defect: remove when stacking applies.
                              (= (pj/plot v)
                                 (pj/plot (pj/lay-histogram (rdatasets/datasets-iris)
                                                            :sepal-length
                                                            {:bins 8 :alpha 0.5 :color :species})))))])

(def where-stat-goes
  (pj/lay-smooth (rdatasets/datasets-iris) :sepal-length :sepal-width {:stat :linear-model}))

where-stat-goes

(kind/pprint where-stat-goes)

(kind/test-last [(fn [v] (= :linear-model (get-in v [:layers 0 :stat])))])

;; A column type is written in the mapping.

(def where-type-goes
  (-> (rdatasets/datasets-iris)
      (pj/pose {:x :species :y :sepal-width :x-type :categorical})
      pj/lay-boxplot))

where-type-goes

(kind/pprint where-type-goes)

(kind/test-last [(fn [v] (= :categorical (get-in v [:mapping :x-type])))])

;; A scale is written in the pose's mapping. Here the column is on the
;; layer, so the pose's `:x` carries the scale alone, and the two meet
;; when the pose is drawn.

(def where-scale-goes
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/scale :x {:type :log})))

where-scale-goes

(kind/pprint where-scale-goes)

(kind/test-last [(fn [v] (and (= {:scale {:type :log}} (get-in v [:mapping :x]))
                              (= :sepal-length (get-in v [:layers 0 :mapping :x]))
                              (= :log (-> v pj/plan :panels first :x-scale :type))))])

;; A facet is written in the mapping, as `:col` or `:row`.

(def where-facet-goes
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/facet :species)))

where-facet-goes

(kind/pprint where-facet-goes)

(kind/test-last [(fn [v] (= :species (get-in v [:mapping :col])))])

;; Titles, labels, size and coordinates are written in `:opts`.

(def where-options-go
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/options {:title "Iris" :x-label "sepal length (cm)" :width 400})
      (pj/coord :flip)))

where-options-go

(kind/pprint where-options-go)

(kind/test-last [(fn [v] (= {:title "Iris" :x-label "sepal length (cm)" :width 400 :coord :flip}
                            (:opts v)))])

;; In a composite, cells carry their own mapping; the root carries what
;; reaches every cell.

(def where-composite-things-go
  (-> (rdatasets/datasets-iris)
      (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
      (pj/pose {:color :species})
      pj/lay-point))

where-composite-things-go

(kind/pprint where-composite-things-go)

(kind/test-last [(fn [v] (and (= {:color :species} (:mapping v))
                              (= [{:layer-type :point}] (:layers v))))])

;; ## Where things do not go

;; A layer setting in the pose's mapping warns and is ignored: the
;; histogram draws its default bins.

(def bins-on-the-pose
  (-> (rdatasets/datasets-iris)
      (pj/pose {:x :sepal-length :bins 5})
      pj/lay-histogram))

bins-on-the-pose

(kind/pprint bins-on-the-pose)

(kind/test-last [(fn [v] (and (not (contains? (:mapping v) :bins))
                              (= 9 (:polygons (pj/svg-summary v)))))])

;; A plot option in a layer warns and is ignored.

(def label-on-a-layer
  (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width {:x-label "D"}))

label-on-a-layer

(kind/pprint label-on-a-layer)

(kind/test-last [(fn [v] (not (contains? (:opts v) :x-label)))])

;; A label in an aesthetic's mapping is an error.

(try
  (pj/lay-point (rdatasets/datasets-iris)
                {:x {:column :sepal-length :label "SL"} :y :sepal-width})
  (catch Exception e (ex-message e)))

(kind/test-last [(fn [s] (and (string? s) (re-find #"unexpected key" s)))])

;; ## Not decided: three homes for what is written

;; About an aesthetic -- its mapping: column or value, `:scale`, `:label`.
;; About a layer type -- the mapping, at any scope: `:bins`, `:stat`,
;; `:position`, `:bar-width`, reaching every layer whose type reads them.
;; About the plot or a cell -- `:opts`: title, size, theme, legend,
;; scale sharing. Options do not reach layers.
;; Pseudocode, a pose written that way:

(comment
  {:data iris
   :mapping {:x {:column :sepal-length :label "sepal length (cm)"}
             :bins 8}
   :layers [{:layer-type :histogram
             :mapping {:color :species :position :stack}}]
   :opts {:title "Iris" :width 400}})
