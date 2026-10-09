;; # PR 61 Unify Examples
;;
;; Work in progress on branch `pr61-unify`, on top of Timothy's
;; `simplify-pose`. One section per step. Each pose is drawn, then printed.

(ns pr61-unify-examples
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Step 1: `pj/pose` writes one mapping

;; ### A different position replaces the old one

;; A pose returned by a function, pointed at other columns.

(defn trend-plot [data]
  (-> data
      (pj/pose :sepal-length :sepal-width)
      pj/lay-point
      pj/lay-smooth))

(def moved
  (-> (trend-plot (rdatasets/datasets-iris))
      (pj/pose :petal-length :petal-width)))

moved

(kind/pprint moved)

(kind/test-last [(fn [v] (and (not (contains? v :poses))
                              (= {:x :petal-length :y :petal-width} (:mapping v))
                              (= [{:layer-type :point} {:layer-type :smooth}] (:layers v))))])

;; A layer that names its own columns keeps them. Until the identity rule
;; retires, the pose's new columns draw a panel of their own beside it.

(def own-columns
  (-> (rdatasets/datasets-iris)
      pj/pose
      (pj/lay-point {:x :sepal-length :y :sepal-width})
      (pj/pose :petal-length :petal-width)))

own-columns

(kind/pprint own-columns)

(kind/test-last [(fn [v] (and (= {:x :sepal-length :y :sepal-width}
                                 (get-in v [:layers 0 :mapping]))
                              (= 2 (:panels (pj/svg-summary v)))))])

;; Panels side by side: `pj/arrange`.

(def side-by-side
  (-> (rdatasets/datasets-iris)
      (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
      pj/lay-point))

side-by-side

(kind/pprint side-by-side)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ### The same position again changes nothing

(def scatter
  (-> (rdatasets/datasets-iris)
      (pj/pose :sepal-length :sepal-width)
      pj/lay-point))

scatter

(kind/pprint scatter)

(= scatter (pj/pose scatter :sepal-length :sepal-width))

(kind/test-last [true?])

;; ### An aesthetic merges; the layers stay

(def colored-scatter
  (pj/pose scatter {:color :species}))

colored-scatter

(kind/pprint colored-scatter)

(kind/test-last [(fn [v] (and (not (contains? v :poses))
                              (= {:x :sepal-length :y :sepal-width :color :species}
                                 (:mapping v))
                              (= [{:layer-type :point}] (:layers v))))])

;; The same pose as writing the aesthetic in the first call.

(= colored-scatter
   (-> (rdatasets/datasets-iris)
       (pj/pose :sepal-length :sepal-width {:color :species})
       pj/lay-point))

(kind/test-last [true?])

;; ### Laws

;; `(pj/pose p)` is `p`; `(pj/pose p {})` is `p`; writing twice is writing once.

(= colored-scatter
   (pj/pose colored-scatter)
   (pj/pose colored-scatter {}))

(kind/test-last [true?])

(= (pj/pose scatter {:color :species})
   (pj/pose (pj/pose scatter {:color :species}) {:color :species}))

(kind/test-last [true?])

;; ### On a composite

;; An aesthetic merges into the root and reaches every cell.

(def colored-side-by-side
  (pj/pose side-by-side {:color :species}))

colored-side-by-side

(kind/pprint colored-side-by-side)

(kind/test-last [(fn [v] (and (= {:color :species} (:mapping v))
                              (= 2 (:panels (pj/svg-summary v)))))])

;; A position goes to the root. Cells that map their own override it,
;; and drawing warns that it is not drawn.

(def unread-position
  (pj/pose side-by-side :sepal-length :petal-length))

unread-position

(kind/pprint unread-position)

(kind/test-last [(fn [v] (and (= {:x :sepal-length :y :petal-length} (:mapping v))
                              (re-find #"not drawn" (with-out-str (pj/plan v)))))])

;; ### `pj/cross-matrix` is unchanged

;; It built its cells through promotion; it builds the same composite directly.

(def matrix
  (pj/cross-matrix (rdatasets/datasets-iris) [:sepal-length :sepal-width] {:color :species}))

matrix

(kind/pprint matrix)

(kind/test-last [(fn [v] (and (= 4 (:panels (pj/svg-summary v)))
                              (= {:color :species} (:mapping v))))])

;; A 1xN matrix is a flat composite of N cells.

(def matrix-row
  (pj/cross-matrix (rdatasets/datasets-iris) [:sepal-length] [:sepal-width :petal-length :petal-width]))

matrix-row

(kind/pprint matrix-row)

(kind/test-last [(fn [v] (and (= 3 (:panels (pj/svg-summary v)))
                              (= 3 (count (:poses v)))))])

;; ## Step 3: a `lay-*` call keeps its columns; the draft infers

;; ### The columns are on the layer

;; The pose carries no mapping; each layer carries the columns its call
;; named.

(def two-layers
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/lay-point :petal-length :petal-width)))

two-layers

(kind/pprint two-layers)

(kind/test-last [(fn [v] (and (nil? (:mapping v))
                              (= [{:x :sepal-length :y :sepal-width}
                                  {:x :petal-length :y :petal-width}]
                                 (mapv :mapping (:layers v)))
                              (= 2 (:panels (pj/svg-summary v)))))])

;; ### A layer naming no columns reads each panel's columns

;; The smooth is drawn on both panels, fitted to that panel's points. On
;; 0.16.1 the petal panel's smooth was fitted to the sepal columns.

(def smooth-per-panel
  (-> two-layers
      pj/lay-smooth))

smooth-per-panel

(kind/pprint smooth-per-panel)

(kind/test-last [(fn [v] (= [[0 :sepal-length :sepal-width] [1 :petal-length :petal-width]]
                            (->> (pj/draft v)
                                 :layers
                                 (filter #(= :smooth (:layer-type %)))
                                 (mapv (juxt :__panel-idx :x :y)))))])

;; Written between the two point layers, the smooth draws the same.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    pj/lay-smooth
    (pj/lay-point :petal-length :petal-width))

(kind/test-last [(fn [v] (= [[0 :sepal-length :sepal-width] [1 :petal-length :petal-width]]
                            (->> (pj/draft v)
                                 :layers
                                 (filter #(= :smooth (:layer-type %)))
                                 (mapv (juxt :__panel-idx :x :y)))))])

;; ### A later `pj/pose` reaches the layers that name no columns

;; The point layer named its columns, so it keeps them, and the pose's
;; own `:x` keeps a panel of its own.

(def pose-after-lay
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/pose {:x :petal-length})))

pose-after-lay

(kind/pprint pose-after-lay)

(kind/test-last [(fn [v] (and (= {:x :petal-length} (:mapping v))
                              (= 2 (:panels (pj/svg-summary v)))))])

;; ### The default mapping is inferred when the pose is drawn

(def raw-scatter
  (pj/lay-point {:height [170 180 165] :weight [70 80 65]}))

raw-scatter

(kind/pprint raw-scatter)

(kind/test-last [(fn [v] (and (nil? (:mapping v))
                              (= {:x :height :y :weight} (:mapping (pj/infer-mapping v)))
                              (= 3 (:points (pj/svg-summary v)))))])

;; ## Step 3 (b): the series pivot happens at draft

(def sales
  {:quarter ["Q1" "Q2" "Q3" "Q4"]
   :revenue [120 150 140 190]
   :cost    [90 100 115 120]})

;; ### The layer keeps its series

;; The pose's data is the data written; the draft pivots it.

(def series-line
  (pj/lay-line sales :quarter [:revenue :cost]))

series-line

(kind/pprint series-line)

(kind/test-last [(fn [v] (and (= [:revenue :cost] (-> v :layers first :mapping :y))
                              (= [:quarter :revenue :cost] (vec (tc/column-names (:data v))))
                              (= [:quarter :value :series]
                                 ((juxt :x :y :color) (-> v pj/draft :layers first)))))])

;; ### Two layers writing the same series share one pivot

;; On 0.16.1 the second call met the columns the first had pivoted away.

(def line-and-points
  (-> sales
      (pj/lay-line :quarter [:revenue :cost])
      (pj/lay-point :quarter [:revenue :cost])))

line-and-points

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s)) (= 8 (:points s)) (= 2 (:lines s)))))])

;; ### A series on a pose that no layer is added to is drawn

;; On 0.16.1 this reported that no layer was added to read the series.

(def series-no-layer
  (pj/pose sales {:x :quarter :y [:revenue :cost]}))

series-no-layer

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])
