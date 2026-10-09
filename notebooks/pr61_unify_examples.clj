;; # PR 61 Unify Examples
;;
;; Work in progress on branch `pr61-unify`, on top of Timothy's
;; `simplify-pose`. One section per step. Each pose is drawn, then printed.

(ns pr61-unify-examples
  (:require
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
