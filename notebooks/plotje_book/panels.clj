;; # Panels
;;
;; A plot can be one set of axes or a grid of them, and which you get is
;; never chosen directly. A leaf pose draws one panel, however many
;; layers it holds and whatever columns they name. More panels come from
;; three places:
;;
;; - **`pj/facet` over the categories of a column** -- a panel per
;;   category.
;; - **`pj/facet` over columns** -- a panel per column, or per pair of
;;   columns.
;; - **`pj/arrange`** -- a panel per pose given to it.
;;
;; This chapter shows each, and how they combine.
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

;; A pose built from one dataset with one mapping is a single leaf, and
;; draws a single panel:

(-> measurements
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; Adding a layer does not add a panel. The layers of a pose share its
;; axes and paint over one another, which is the point of them:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-line :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; That holds for a layer naming other columns too. Its columns are drawn
;; against the axes the panel already has, each axis covers every column
;; drawn on it and is titled by all of them, and each layer takes a
;; colour and a legend entry naming its columns:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (some #{"height, depth"} (:texts s)))))])

;; The colours per column are a default, given where no `:color` is
;; specified on the pose or on any layer. With a `:color` on the pose,
;; the layers are coloured by it, and the axis is still titled by every
;; column:

(-> measurements
    (pj/pose {:color :species})
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (some #{"height, depth"} (:texts s))
                                (some #{"species"} (:texts s))
                                ;; no entry naming a column
                                (not-any? #{"height" "depth"} (:texts s)))))])

;; ## A panel per column

;; To draw the two columns apart, split the pose over them with
;; `pj/facet`. Each layer naming one of the columns is drawn only in
;; that column's panel:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight)
    (pj/facet {:x [:height :depth]}))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

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

;; ## Arranging poses side by side

;; `pj/arrange` takes finished poses and makes each one a cell of a
;; single composite:

(pj/arrange [(pj/lay-point measurements :height :weight)
             (pj/lay-point measurements :depth :weight)])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A composite pose can also be written directly, which
;; [Composition](./plotje_book.composition.html#explicit-composite-poses)
;; covers. It is the same structure `pj/arrange` builds.

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

;; Every panel of a facet over categories shares one axis range, so the
;; panels can be read against each other. That is what makes faceting different from
;; arranging two poses that happen to hold the same columns.

;; ## Combining them

;; A facet over categories cuts each panel a pose draws into one per
;; category. A pose whose layers name different columns draws one panel,
;; so faceting it by `:species` draws two:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight)
    (pj/facet :species))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Columns and categories cross in one `pj/facet` call, the categories
;; across and the columns down:

(-> measurements
    (pj/pose {:y :weight})
    pj/lay-point
    (pj/facet {:col :species :row {:x [:height :depth]}}))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; `:col` and `:row` are aesthetics, so a facet written on an arranged
;; pose reaches every cell:

(-> (pj/arrange [(pj/pose measurements :height :weight)
                 (pj/pose measurements :depth :weight)])
    (pj/lay-point)
    (pj/facet :species))

(kind/test-last
 [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## See Also
;;
;; - [Poses](./plotje_book.pose_model.html) -- leaf and composite poses
;; - [Composition](./plotje_book.composition.html) -- arranging poses,
;;   shared scales, and the patterns built from them
;; - [Faceting](./plotje_book.faceting.html) -- faceting in practice,
;;   including grids of two columns
;; - [Pose Rules](./plotje_book.pose_rules.html) -- the rules that
;;   decide where a `pj/lay-*` call's layer goes
