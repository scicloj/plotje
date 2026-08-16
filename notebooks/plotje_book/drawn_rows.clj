;; # Which Rows Get Drawn
;;
;; A scatter of a hundred rows usually has a hundred marks, and when it
;; does not, the reason is worth knowing. Rows leave a plot in two quite
;; different ways, and telling them apart is the whole of this chapter:
;;
;; - **Dropped.** The row cannot be placed at all, so it takes no part
;;   in the plot. It does not reach the axis range either, which means
;;   dropping a row can move the axes.
;; - **Clipped.** The row is placed, counted and drawn, and then falls
;;   outside the view. It still counts toward the axis range.
;;
;; Everything below is one or the other. Dropping is reported on the
;; console as it happens, with one exception noted at the end.

(ns plotje-book.drawn-rows
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; Four rows, all of them drawable, as the baseline:

(-> {:height [1 2 3 4] :weight [1 2 3 4]}
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 4 (:points (pj/svg-summary v))))])

;; ## Dropped: a position that is not a number

;; A mark needs a number on each axis to be placed. A row whose
;; position is `nil` or `NaN` has none, so it is dropped and three
;; marks are drawn from four rows:

(-> {:height [1 2 nil 4] :weight [1 2 3 4]}
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Plotje prints
;; `Warning: Removed 1 rows with non-finite values: 1 missing (nil/NaN)`
;; when it does this. `NaN` is counted with `nil` in that message, since
;; both mean "no value here".

;; ## Dropped: a position that is infinite

;; An infinity is a number but not a place, so it goes the same way,
;; under its own count in the warning -- `1 non-finite (Inf/-Inf)`:

(-> {:height [1 2 ##Inf 4] :weight [1 2 3 4]}
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; ## Dropped: a value a log scale has no room for

;; A log scale has no place for zero or for a negative number. Asking
;; for one drops those rows, and says which axis it happened on:
;; `Warning: Removed 1 rows containing non-positive values (log scale
;; on :y)`.

(-> {:height [1 2 3 4] :weight [1 0 10 100]}
    (pj/lay-point :height :weight)
    (pj/scale :y :log))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; ## Clipped: a value outside a domain you set

;; Narrowing an axis with `pj/scale` does not remove anything. The
;; domain is a view window: every row is still placed, still counted,
;; and still contributes to what the layer computes -- the ones outside
;; the window are simply not shown, because each panel clips to its own
;; drawing area.
;;
;; Here the fourth row sits at 40 on an axis stopping at 5. The plot
;; shows three marks and the drawing contains four:

(-> {:height [1 2 3 4] :weight [1 2 3 40]}
    (pj/lay-point :height :weight)
    (pj/scale :y {:domain [0 5]}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 4 (:points s))
                 (= 1 (:clips s)))))])

;; This is deliberate, and matches ggplot2's `coord_cartesian`: a
;; regression line over these four rows is fitted to all four, and the
;; window then decides how much of it you see. Removing rows from the
;; computation is a job for the data, with `tc/select-rows`, before the
;; pose is built.

;; ## The one that says nothing

;; A `nil` among the values of a **categorical** column is dropped like
;; any unplaceable row, but without a warning. Four rows, three marks,
;; and a legend naming only the two real categories:

(-> {:height [1 2 3 4] :weight [1 2 3 4] :species ["a" nil "b" "a"]}
    (pj/lay-point :height :weight {:color :species}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:points s))
                 (= ["a" "b"] (filterv #{"a" "b"} (:texts s))))))])

;; The numeric cases above all announce themselves; this one does not,
;; so a plot quietly short of a mark is worth checking for a `nil`
;; category. Filling or filtering the column before plotting makes the
;; choice yours -- `(tc/replace-missing ds :species :value "unknown")`
;; keeps those rows and names them.

;; ## See Also
;;
;; - [Datasets](./plotje_book.datasets.html) -- preparing the data
;;   before it reaches a pose
;; - [Composition](./plotje_book.composition.html#panels-clip-their-own-content) --
;;   what clipping does across several panels
;; - [Troubleshooting](./plotje_book.troubleshooting.html) -- symptoms
;;   first, when a plot is not what you expected
