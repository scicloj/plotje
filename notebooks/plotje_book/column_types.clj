;; # What a Column's Type Decides
;;
;; Plotje never asks you what kind of axis to draw. It reads the values
;; in the column and decides, and that one decision then settles a
;; surprising amount: the shape of the axis, how ticks are labelled,
;; whether a colour mapping splits the data, and which layer types will
;; accept the column at all.
;;
;; There are three types, and a column has exactly one:
;;
;; | The column holds | Its type |
;; |:-----------------|:---------|
;; | numbers | numerical |
;; | anything else that is not a date -- strings, keywords, booleans | categorical |
;; | dates and timestamps | temporal |
;;
;; This chapter follows that decision through to each of its
;; consequences. [Specifying
;; Aesthetics](./plotje_book.specifying_aesthetics.html#what-kind-of-column-is-it)
;; asks the same question of one aesthetic; this is the general answer.

(ns plotje-book.column-types
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; The three datasets below differ only in what their `:k` column
;; holds. Everything that follows is a consequence of that.

(def numerical
  {:k [1 2 3 4] :v [10 20 30 40]})

(def categorical
  {:k ["a" "b" "c" "d"] :v [10 20 30 40]})

(def temporal
  {:k [(java.time.LocalDate/parse "2026-01-15")
       (java.time.LocalDate/parse "2026-02-15")
       (java.time.LocalDate/parse "2026-03-15")
       (java.time.LocalDate/parse "2026-04-15")]
   :v [10 20 30 40]})

;; ## What it decides about the axis

;; A numerical column gives an axis that runs continuously between the
;; smallest and largest value, with a little padding at each end so the
;; outermost marks are not on the edge:

(-> numerical
    (pj/lay-point :k :v))

(kind/test-last
 [(fn [v] (let [ticks (-> v pj/plan :panels first :x-ticks)]
            (and (false? (:categorical? ticks))
                 (contains? (set (:labels ticks)) "2.0"))))])

;; A categorical column gives one band per distinct value, in the order
;; the values first appear. There is nothing between two bands, so the
;; axis carries exactly as many places as there are categories:

(-> categorical
    (pj/lay-point :k :v))

(kind/test-last
 [(fn [v] (let [ticks (-> v pj/plan :panels first :x-ticks)]
            (and (true? (:categorical? ticks))
                 (= ["a" "b" "c" "d"] (vec (:labels ticks))))))])

;; A temporal column runs continuously like a numerical one, but its
;; ticks land on calendar dates and are labelled as dates rather than
;; as the numbers underneath:

(-> temporal
    (pj/lay-point :k :v))

(kind/test-last
 [(fn [v] (let [ticks (-> v pj/plan :panels first :x-ticks)]
            (and (false? (:categorical? ticks))
                 (some (fn [l] (re-find #"^[A-Z][a-z]{2}-\d\d$" l))
                       (:labels ticks)))))])

;; ## What it decides about a colour mapping

;; The same three columns, mapped to `:color` instead of to a position.
;; A categorical column draws one palette colour per distinct value:

(-> categorical
    (pj/lay-point :v :v {:color :k}))

(kind/test-last
 [(fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))])

;; A numerical column shades each row from a gradient, so the plot
;; carries many more colours than it has rows -- the gradient bar in the
;; legend is drawn from a stack of them:

(-> numerical
    (pj/lay-point :v :v {:color :k}))

(kind/test-last
 [(fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))])

;; The consequence that matters is not the colours but the splitting: a
;; categorical mapping divides the rows into groups and every layer
;; computes itself once per group, and a numerical one does not.
;; [Specifying
;; Aesthetics](./plotje_book.specifying_aesthetics.html#what-kind-of-column-is-it)
;; works through that difference.

;; ## What it decides about which layer types apply

;; Some layer types need a categorical axis to have anything to do.
;; `pj/lay-boxplot` draws one box per category, so it says so rather
;; than drawing nothing:

(try
  (-> numerical
      (pj/lay-boxplot :k :v)
      pj/plot)
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"requires a categorical column" m))])

;; The same call on the categorical column draws four boxes. Others
;; work the other way round: `pj/lay-histogram` bins numbers and has
;; nothing to bin on a categorical column. [Layer
;; Types](./plotje_book.layer_types.html) lists what each one needs.

;; ## Saying which you meant

;; The reading is inferred from the values, and the values do not always
;; say what they mean. A year, a postcode and a region code are numbers
;; that name something rather than measure it. `:x-type`, `:y-type` and
;; `:color-type` override the inference for one axis or one mapping:

(-> {:year [2020 2021 2022 2023] :revenue [10 20 30 40]}
    (pj/lay-bar :year :revenue {:x-type :categorical}))

(kind/test-last
 [(fn [v] (let [ticks (-> v pj/plan :panels first :x-ticks)]
            (and (true? (:categorical? ticks))
                 (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))])

;; Without the override the same call reads `:year` as a quantity, and
;; draws bars along a continuous axis instead of one per year.
;;
;; The override changes the type, and so changes everything the type
;; decides -- the axis becomes bands, a colour mapping on the same
;; column would split rather than shade, and the layer types that need a
;; categorical axis start accepting it.

;; ## See Also
;;
;; - [Specifying Aesthetics](./plotje_book.specifying_aesthetics.html) --
;;   what a mapping's value means, of which the column's type is half
;; - [Inference Rules](./plotje_book.inference_rules.html#column-types) --
;;   the dtype-by-dtype table behind the three types
;; - [Layer Types](./plotje_book.layer_types.html) -- which layer types
;;   need which kind of axis
