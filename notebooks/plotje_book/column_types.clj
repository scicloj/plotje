;; # Column Types
;;
;; Every column Plotje reads has a type. That type decides the shape of
;; the axis the column is drawn against, how the ticks are labelled,
;; whether a color mapping splits the data into groups, and which layer
;; types accept the column.
;;
;; There are three types, and a column has exactly one:
;;
;; | The column holds | Its type |
;; |:-----------------|:---------|
;; | numbers | numerical |
;; | anything else that is not a date -- strings, keywords, booleans | categorical |
;; | dates and timestamps | temporal |
;;
;; Plotje does not work the type out from scratch. Every column of a
;; dataset already carries a Tablecloth datatype, and for most columns
;; that datatype is what decides. Plain Clojure data gets its datatypes
;; on the way in, since Plotje coerces it to a dataset with Tablecloth
;; first; [Datasets](./plotje_book.datasets.html#datasets-and-plotje)
;; is the background on that. Where the datatype does not settle the
;; question, Plotje reads the values instead.
;;
;; The sections below take each of those in turn. The last one sets the
;; column's type beside a scale's type, a separate setting that shares
;; the word `:categorical`.

(ns plotje-book.column-types
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Tablecloth -- column-level operations
   [tablecloth.column.api :as tcc]))

;; The three datasets below differ only in what their `:k` column
;; holds.

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

;; A fourth holds ratios, which is the case the datatype does not
;; settle.

(def ratios
  {:k [(/ 1 3) (/ 2 3) (/ 4 3) (/ 5 3)] :v [10 20 30 40]})

;; Tablecloth gives each `:k` column a datatype. For the first three it
;; is the datatype that decides the type:

(tc/dataset
 (for [[label d] [["numerical" numerical]
                  ["categorical" categorical]
                  ["temporal" temporal]
                  ["ratios" ratios]]]
   {:dataset label
    :datatype (tcc/typeof (:k (tc/dataset d)))}))

(kind/test-last
 [(fn [ds] (= [:int64 :string :packed-local-date :object]
              (vec (:datatype ds))))])

;; Ratios are numbers, but Tablecloth has no datatype of its own for
;; them, so the column is `:object` and the values decide instead. The
;; column is drawn as a quantity, like any other numerical one:

(-> ratios
    (pj/lay-point :k :v))

(kind/test-last
 [(fn [v] (false? (:categorical? (-> v pj/plan :panels first :x-ticks))))])

;; ## What the type decides about the axis

;; A numerical column gives an axis that runs continuously between the
;; smallest and largest value, with padding at each end so the outermost
;; marks are not drawn on the panel edge:

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

;; ## What the type decides about color

;; The same three columns, mapped to `:color` instead of to a position.
;; A categorical column draws one palette color per distinct value:

(-> categorical
    (pj/lay-point :v :v {:color :k}))

(kind/test-last
 [(fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))])

;; A numerical column shades each row from a gradient, so the plot
;; carries many more colors than it has rows -- the gradient bar in the
;; legend is drawn from a stack of them:

(-> numerical
    (pj/lay-point :v :v {:color :k}))

(kind/test-last
 [(fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))])

;; Beyond the colors themselves, a categorical mapping divides the rows
;; into groups, and every layer computes itself once per group. A
;; numerical mapping does not.
;; [Inference Rules](./plotje_book.inference_rules.html#categorical-color-implies-grouping)
;; works through that difference.

;; ## What the type decides about the layer type
;;
;; A pose with no `pj/lay-*` call still draws something. The column
;; types choose the layer type, which is a mark together with the
;; statistic computed before that mark is drawn. Every combination of
;; the three types is settled by the same rules, and the three datasets
;; above cover them:

(defn inferred-mark
  "The mark a pose is drawn with when no layer type is named."
  [pose]
  (-> pose pj/plan :panels first :layers first :mark))

(tc/dataset
 [{:x-column "numerical"   :y-column "none"        :mark (inferred-mark (pj/pose numerical :k))}
  {:x-column "temporal"    :y-column "none"        :mark (inferred-mark (pj/pose temporal :k))}
  {:x-column "categorical" :y-column "none"        :mark (inferred-mark (pj/pose categorical :k))}
  {:x-column "temporal"    :y-column "numerical"   :mark (inferred-mark (pj/pose temporal :k :v))}
  {:x-column "categorical" :y-column "numerical"   :mark (inferred-mark (pj/pose categorical :k :v))}
  {:x-column "numerical"   :y-column "categorical" :mark (inferred-mark (pj/pose categorical :v :k))}
  {:x-column "numerical"   :y-column "numerical"   :mark (inferred-mark (pj/pose numerical :k :v))}])

(kind/test-last
 [(fn [ds] (= [:bar :bar :rect :line :boxplot :boxplot :point]
              (vec (:mark ds))))])

;; With one column, a categorical one is counted and anything else is
;; binned. A temporal column is binned like a numerical one, since it is
;; not categorical.
;;
;; With two, a temporal x against a numerical y is a time series, drawn
;; as a line:

(pj/pose temporal :k :v)

(kind/test-last
 [(fn [v] (= 1 (:lines (pj/svg-summary v))))])

;; A categorical column against a numerical one is summarised as a
;; boxplot, whichever of the two axes the categories are on. The
;; datasets above hold one row per category, which makes a box with
;; nothing in it, so this section uses one with repeats:

(def readings
  {:batch   ["a" "a" "a" "a" "b" "b" "b" "c" "c"]
   :reading [3 5 4 6 8 9 7 2 6]})

(pj/pose readings :batch :reading)

(kind/test-last
 [(fn [v] (= :boxplot (inferred-mark v)))])

;; The same column alone is counted instead, one bar per batch, as tall
;; as the number of rows that batch holds -- four, three and two:

(pj/pose readings :batch)

(kind/test-last
 [(fn [v] (and (= :rect (inferred-mark v))
               (= 3 (:polygons (pj/svg-summary v)))
               (= [4 3 2] (->> (pj/plan v) :panels first :layers first
                               :groups first :counts (mapv :count)))))])

;; Two numerical columns are a scatter, which is the case every
;; combination not listed above falls back to.
;;
;; Naming a layer type with `pj/lay-*` settles the question instead, and
;; the column types then decide only what that layer type does with the
;; column.

;; ## Which layer types accept the column

;; Some layer types need a categorical axis. `pj/lay-boxplot` draws one
;; box per category, so on a numerical column it reports the mismatch
;; rather than drawing nothing:

(try
  (-> numerical
      (pj/lay-boxplot :k :v)
      pj/plot)
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"requires a categorical column" m))])

;; On a categorical column it draws one box per category. The
;; `readings` data above has several rows per batch, so the boxes have
;; something in them:

(-> readings
    (pj/lay-boxplot :batch :reading))

(kind/test-last
 [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; Other layer types need the opposite: `pj/lay-histogram` bins
;; numbers, and has nothing to bin on a categorical column. [Layer
;; Types](./plotje_book.layer_types.html) lists what each one needs.

;; ## Overriding the inferred type

;; The type is read from the values, and the values do not always match
;; what they stand for. A year, a postcode and a region code are numbers
;; that name something rather than measure it. `:x-type`, `:y-type` and
;; `:color-type` override the inference for one axis or one mapping:

(-> {:year [2020 2021 2022 2023] :revenue [10 20 30 40]}
    (pj/lay-bar :year :revenue {:x-type :categorical}))

(kind/test-last
 [(fn [v] (let [ticks (-> v pj/plan :panels first :x-ticks)]
            (and (true? (:categorical? ticks))
                 (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))])

;; Without the override the same call reads `:year` as a quantity and
;; draws bars along a continuous axis, ticked at the round numbers a
;; numerical axis is ticked at rather than at one per year:

(-> {:year [2020 2021 2022 2023] :revenue [10 20 30 40]}
    (pj/lay-bar :year :revenue))

(kind/test-last
 [(fn [v]
    (let [ticks (-> v pj/plan :panels first :x-ticks)
          groups (fn [mapping]
                   (-> {:year [2020 2021 2022 2023] :revenue [10 20 30 40]}
                       (pj/lay-point :year :revenue mapping)
                       pj/plan
                       :panels first :layers first :groups count))]
      (and (false? (:categorical? ticks))
           (contains? (set (:labels ticks)) "2020.5")
           ;; The same override on :color: one group shaded from a
           ;; gradient without it, one group per year with it.
           (= 1 (groups {:color :year}))
           (= 4 (groups {:color :year :color-type :categorical})))))])

;; The override changes the type, and so changes everything the type
;; decides -- the axis becomes bands, a color mapping on the same
;; column splits rather than shades, and the layer types that need a
;; categorical axis start accepting it.

;; ### Only :categorical can be asked of a column holding something else
;;
;; Every value can be the name of a group, whatever the column holds, so
;; `:categorical` is always available. The other direction has nothing
;; to read: there is no number in "setosa" and no instant either.
;; `:numerical` and `:temporal` are accepted where they name the
;; column's own type, and reported where they do not:

(try
  (-> {:species ["setosa" "versicolor"] :count [50 50]}
      (pj/lay-point :species :count {:x-type :numerical})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"which holds categorical values" m))])

;; ## The column's type and the scale's type
;;
;; Two settings are written with the word `:categorical`, and they are
;; not the same setting.
;;
;; | Setting | Written as | Values | Says |
;; |:--|:--|:--|:--|
;; | Column type | `:x-type`, `:y-type`, `:color-type`, in the mapping | `:categorical`, `:numerical`, `:temporal` | what kind of data the column holds |
;; | Scale type | `:type` in a scale spec, or the bare keyword given to `pj/scale` | `:linear`, `:log`, `:categorical` | how a numeric domain is spaced |
;;
;; They meet at the domain. The column's type decides what the domain
;; is -- a list of distinct categories, or an interval of numbers -- and
;; a domain of categories gives a scale that places categories. The
;; scale's own `:type` chooses between `:linear` and `:log`, which is a
;; choice only an interval of numbers offers.
;;
;; So the column's type decides whether the scale places categories, and
;; the scale's type decides how a numeric scale is spaced. Naming
;; `:categorical` as a scale type cannot make a column categorical.
;;
;; On a numeric column the scale's type chooses between `:linear` and
;; `:log`. The four values of `:k` are spaced by ratio here:

(-> numerical
    (pj/lay-point :k :v)
    (pj/scale :x :log))

(kind/test-last
 [(fn [v] (= :log (-> v pj/plan :panels first :x-scale :type)))])

;; Asking that same column for a `:categorical` scale is refused, and
;; the message names the setting that would do it instead:

(try
  (-> numerical
      (pj/lay-point :k :v)
      (pj/scale :x :categorical)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"set :x-type or :y-type to :categorical" m))])

;; A categorical column is refused in the other direction: it has
;; nothing to take a logarithm of, so `:log` is refused on one:

(try
  (-> categorical
      (pj/lay-point :k :v)
      (pj/scale :x :log)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"requires numeric data" m))])

;; A `:linear` written on a categorical column is not read at all. The
;; scale places categories because the domain holds categories, so the
;; call below draws exactly what the same call without the scale
;; draws.

(-> categorical
    (pj/lay-point :k :v)
    (pj/scale :x :linear))

(kind/test-last
 [(fn [v] (= (pj/svg-summary v)
             (pj/svg-summary (-> categorical (pj/lay-point :k :v)))))])

;; A temporal column is spaced linearly, over the instants underneath
;; its dates, and its ticks are labelled as calendar dates -- the axis
;; section above draws one.

;; ## See Also
;;
;; - [Datasets](./plotje_book.datasets.html#datasets-and-plotje) -- where
;;   a column's Tablecloth datatype comes from, and what a dataset is
;; - [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers)
;;   -- what a mapping is, and how a column reaches an aesthetic
;; - [Inference Rules](./plotje_book.inference_rules.html#column-types) --
;;   the datatype-by-datatype table behind the three types
;; - [Layer Types](./plotje_book.layer_types.html) -- which layer types
;;   need which kind of axis
