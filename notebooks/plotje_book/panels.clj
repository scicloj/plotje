;; # Panels
;;
;; A plot can be one set of axes or a grid of them, and which you get is
;; never chosen directly. It follows from two things:
;;
;; - **How many leaf poses the pose contains.** Each leaf draws on its
;;   own axes, so each leaf is a panel.
;; - **How many categories the faceting column has.** Faceting cuts each
;;   panel into one per category.
;;
;; So the count is the leaves multiplied by the facet categories, and
;; every multi-panel plot in the book is one of those two things or
;; both. This chapter shows each way of adding a leaf, then faceting on
;; top, and ends with the case where the two do not combine.
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
   :depth   [2 3 4 5]
   :species ["a" "a" "b" "b"]})

;; ## One leaf is one panel

;; A pose built from one dataset with one position mapping is a single
;; leaf, and draws a single panel:

(-> measurements
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; Adding a layer does not add a panel. Layers of the same leaf share
;; its axes and paint over one another, which is the point of them:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-line :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Three ways to get another leaf

;; ### Laying on columns the pose does not have

;; A `pj/lay-*` call carrying its own `:x` and `:y` attaches to a leaf
;; that already maps those columns. When none does, it cannot share
;; anyone's axes, so it becomes a leaf of its own -- and the plot grows
;; a second panel:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; This is the one that arrives unasked. A layer meant to overlay draws
;; beside instead, and the cause is a position mapping that does not
;; match. [Pose Rules](./plotje_book.pose_rules.html) states the matching
;; rule as LP2.

;; ### Arranging poses side by side

;; `pj/arrange` is the deliberate form of the same thing: it takes
;; finished poses and makes each a leaf of one composite.

(pj/arrange [(pj/lay-point measurements :height :weight)
             (pj/lay-point measurements :depth :weight)])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ### Writing the composite out

;; A composite pose can also be written directly, which
;; [Composition](./plotje_book.composition.html#explicit-composite-poses)
;; covers. It is the same structure `pj/arrange` builds.

;; ## Keeping a layer on the panel

;; The first of those three is the one that arrives unasked, so there is
;; a way to say you meant an overlay. `pj/overlay` marks a pose, and
;; every layer added after it joins the panel it is added to rather than
;; starting one of its own:

(-> measurements
    pj/overlay
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; The layer keeps its own columns; they are drawn against the axes the
;; panel already has, and each axis covers every column drawn on it. An
;; axis is named for the panel's own column, so overlaying two columns
;; with different names leaves the axis named for the first.

;; `{:overlay true}` on a single `pj/lay-*` call does the same for that
;; layer alone, and `{:overlay false}` opts one layer out of a
;; `pj/overlay` set further up.

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight {:overlay true}))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Faceting multiplies the panels

;; Faceting takes the leaf and cuts it into one panel per category of
;; the faceting column. Two species give two panels:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/facet :species))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; The count follows the data, not the call. The same call on a column
;; with four categories gives four panels:

(-> {:height [1 2 3 4] :weight [1 2 3 4] :site ["p" "q" "r" "s"]}
    (pj/lay-point :height :weight)
    (pj/facet :site))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Every panel of a facet shares one axis range, so the panels can be
;; read against each other. That is what makes faceting different from
;; arranging two poses that happen to hold the same columns.

;; ## Where the two do not combine

;; Faceting applies to a leaf. A pose that already has more than one
;; leaf -- from either of the ways above -- cannot also be faceted,
;; because the facet grid and the composite layout would each want to
;; be the plot's grid:

(try
  (-> measurements
      (pj/lay-point :height :weight)
      (pj/lay-point :depth :weight)
      (pj/facet :species)
      pj/plot)
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"not yet supported on composite poses" m))])

;; Facet each pose before arranging them, rather than arranging first.

;; ## See Also
;;
;; - [Poses](./plotje_book.pose_model.html) -- leaf and composite poses
;; - [Composition](./plotje_book.composition.html) -- arranging poses,
;;   shared scales, and the patterns built from them
;; - [Faceting](./plotje_book.faceting.html) -- faceting in practice,
;;   including grids of two columns
;; - [Pose Rules](./plotje_book.pose_rules.html) -- the matching rule
;;   that decides whether a `pj/lay-*` call joins a leaf or makes one
