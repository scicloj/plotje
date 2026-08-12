;; # Specifying Aesthetics
;;
;; `{:color :species}`, `{:color "steelblue"}` and `{:color :score}`
;; give one aesthetic three different values, and no two of them do the
;; same thing. The first splits the data into groups and draws a legend
;; naming each one; the second paints every mark a single colour and
;; draws no legend at all; the third shades each mark by its own value
;; and splits nothing.
;;
;; Two questions decide which of those happens, asked in this order for
;; every mapping:
;;
;; - Does the value name a column? A keyword does; a string does when
;;   the data has a column of that name, and a number does too, but only
;;   on `:x` and `:y`. `nil` names nothing and cancels.
;; - If it names one, what kind of column is it? Values that are not
;;   numbers make categories, and categories split the data; numeric
;;   values make a range, and a range shades it.
;;
;; A third question is separate, and about the pair rather than the
;; value: not every aesthetic may be written on every layer type.
;;
;; `:color` is the aesthetic to learn all three on, because it is the
;; one that answers the first question both ways. The sections below
;; take the questions in order, and every case in the chapter is one
;; answer to one of them.
;;
;; Read [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers)
;; first -- this chapter takes the mapping vocabulary from there and
;; asks what a mapping's *value* may be.

(ns plotje-book.specifying-aesthetics
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## Does the value name a column?

;; The value's type answers most of it, and where two readings are
;; possible the data settles which:
;;
;; | Written | Read as |
;; |:--------|:--------|
;; | `:species` | the `:species` column, always |
;; | `"species"` | the `"species"` column, when the data has one |
;; | `"steelblue"` | a literal, when no column has that name |
;; | `4` | a literal -- except on `:x` and `:y`, where the data decides |
;; | `nil` | nothing at all; it cancels a value from an outer scope |
;;
;; The rest of this section takes the rows one at a time.

;; ### A keyword always names one

;; `{:color :species}` asks for the `:species` column. There is no
;; second reading of a keyword, so a keyword naming a column that is
;; not there is a mistake rather than a colour:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Asking for a column the data does not have names the aesthetic that
;; asked and lists what is available:

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :colour})
      pj/plot)
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (and (re-find #"Column :colour \(from :color\) not found" m)
               (re-find #":species" m)))])

;; ### A string names one when the data has it

;; Datasets loaded from CSV often have string column names, so a string
;; has to be able to mean either. It is read as a column name when the
;; data has a column of exactly that name, and as a literal when it
;; does not. With a column named `"blue"`, the column wins -- three
;; palette colours, not one literal blue:

(-> (tc/dataset {"x" [1 2 3] "y" [1 2 3] "blue" ["a" "b" "c"]})
    (pj/lay-point "x" "y" {:color "blue"}))

(kind/test-last
 [(fn [v] (= 3 (count (disj (:colors (pj/svg-summary v)) "none"))))])

;; The same mapping over data with no such column reads `"blue"` as the
;; CSS colour:

(-> (tc/dataset {"x" [1 2 3] "y" [1 2 3]})
    (pj/lay-point "x" "y" {:color "blue"}))

(kind/test-last
 [(fn [v] (= #{"rgb(0,0,255)"} (disj (:colors (pj/svg-summary v)) "none")))])

;; Matching is exact: a string matches only a string column name, and a
;; keyword only a keyword one. A hex code like `"#0000ff"` is unlikely
;; to be a column name and so is unambiguous in practice.
;;
;; **Coming from ggplot2.** ggplot2 tells the cases apart by syntax
;; rather than by asking the data. A bare symbol in `aes()` names a
;; column. A string there is taken literally too, but as a value rather
;; than as a colour: `aes(colour = "blue")` gives every row the constant
;; "blue", which then goes through the colour scale as a category of
;; one, so the marks come out the first palette colour with a legend
;; beside them -- whether or not a column named `blue` exists. The
;; literal colour is `aes(colour = I("blue"))`, or a `colour =` given
;; outside `aes()`. Plotje has one form for all of them and decides by
;; looking at the data, which is what lets `"sepal length"` name a
;; column.

;; ### A number names one only on `:x` and `:y`

;; On the two placing aesthetics a number behaves the way a string does.
;; Data that
;; arrives as bare columns, with no names to go by, gets integer ones
;; from Tablecloth, and `{:x 0 :y 1}` reads those two columns -- three
;; rows, three marks:

(-> (tc/dataset (map vector [1 2 3] [4 5 6]))
    (pj/lay-point {:x 0 :y 1}))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; With no column of that name, the same pair is taken as a pair of
;; data values, and one mark is drawn where they meet. Here the scatter
;; contributes three marks and the text layer a fourth:

(-> {:height [1 2 3] :weight [4 5 6]}
    (pj/lay-point :height :weight)
    (pj/lay-text {:x 2.0 :y 5.5 :text "here"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:points s))
                 (contains? (set (:texts s)) "here"))))])

;; [Placing Marks](./plotje_book.placing_marks.html#giving-x-and-y-as-values)
;; is the chapter for values given this way, including `:in
;; :drawing-area`, which measures from the panel's corner instead of in
;; the data's own units.
;;
;; An integer column name can be written only in a layer's mapping, as
;; above -- not in `pj/pose`'s, and not as one of the column arguments
;; before it.
;; [Known Limitations](./plotje_book.known_limitations.html#integer-column-names)
;; has the reason and the workaround.
;;
;; Everywhere else a number is a literal and nothing more. `{:size 4}`
;; is a radius and `{:alpha 0.3}` an opacity, whatever the columns are
;; called; only `:x` and `:y` weigh a number against the column names.

;; ### `nil` cancels an inherited value

;; A mapping written on a pose reaches every layer under it, and `nil`
;; on a layer is how one layer opts out. Colouring by species on the
;; pose gives two colours; the same plot with the layer cancelling it
;; gives one:

[(-> {:height [1 2 3 4] :weight [1 2 3 4] :species ["a" "a" "b" "b"]}
     (pj/pose {:x :height :y :weight :color :species})
     pj/lay-point)
 (-> {:height [1 2 3 4] :weight [1 2 3 4] :species ["a" "a" "b" "b"]}
     (pj/pose {:x :height :y :weight :color :species})
     (pj/lay-point {:color nil}))]

(kind/test-last
 [(fn [[coloured plain]]
    (and (= 2 (count (disj (:colors (pj/svg-summary coloured)) "none")))
         (= 1 (count (disj (:colors (pj/svg-summary plain)) "none")))))])

;; Scope is [Options and
;; Scopes](./plotje_book.options_and_scopes.html)'s subject; the rule
;; that `nil` cancels rather than inherits is Rule S3 in
;; [Pose Rules](./plotje_book.pose_rules.html).

;; ## What kind of column is it?

;; Naming a column is half an answer. What the aesthetic then does with
;; it follows from the values inside:
;;
;; | The column holds | What follows |
;; |:-----------------|:-------------|
;; | values that are not numbers | one appearance per distinct value, the rows split into a group each, and a legend naming them |
;; | numbers | one appearance per row taken from a range, no split, and a legend labelled at its two ends |
;;
;; The same `:color` mapping therefore draws two quite different
;; pictures, and which one is not a property of `:color`.

;; ### A categorical column splits the data

;; A column counts as categorical when its values are not numbers --
;; strings, keywords and booleans all do. Colouring by one does more
;; than choose colours: it divides the rows into groups, and every layer
;; computes itself once per group. Three species give three density curves, each estimated
;; from its own rows, and a legend naming them:

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length {:color :species}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:polygons s))
                 (= 3 (count (disj (:colors s) "none")))
                 (= ["setosa" "versicolor" "virginica"]
                    (filterv #{"setosa" "versicolor" "virginica"} (:texts s))))))])

;; ### A numeric column shades it

;; Colouring by a numeric column shades each row by its own value and
;; groups nothing. All 150 points are drawn, and the legend is a
;; gradient labelled at its two ends rather than a list of names:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :petal-width}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                texts (set (:texts s))]
            (and (= 150 (:points s))
                 (contains? texts "0.1")
                 (contains? texts "2.5")
                 (not (contains? texts "setosa")))))])

;; The distinction is not cosmetic. A layer that computes something --
;; a density, a regression, a boxplot -- computes it once per group, so
;; whether a colour column splits decides how many curves you get, not
;; only what colour they are.

;; ### Overriding the choice

;; A numeric column that names something rather than measures it -- a
;; region code, a year used as a label -- should be read as categorical.
;; `:color-type` says so, and the gradient becomes one colour per
;; distinct value:

(-> {:height [1 2 3 4] :weight [1 2 3 4] :zone [10 20 30 40]}
    (pj/lay-point :height :weight {:color :zone :color-type :categorical}))

(kind/test-last
 [(fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))])

;; `:x-type` and `:y-type` do the same for `:x` and `:y`.

;; ## Where an aesthetic may be given

;; Some aesthetics are universal: `:color`, `:alpha`, `:group` and the
;; `:x`/`:y` pair may be written on any layer type. The rest belong to
;; particular ones, and `pj/layer-type-lookup` reports what a layer type
;; adds beyond the universal set -- so `:color` appears in none of these
;; lists, and each of the three has its own additions:

[(:accepts (pj/layer-type-lookup :point))
 (:accepts (pj/layer-type-lookup :line))
 (:accepts (pj/layer-type-lookup :bar))]

(kind/test-last
 [(fn [[point line bar]]
    (and (not-any? (fn [a] (contains? (set a) :color)) [point line bar])
         (contains? (set point) :shape)
         (not (contains? (set line) :shape))
         (contains? (set line) :stroke-dash)))])

;; A point has a marker to vary, so it takes `:shape`; a line has a
;; stroke, so it takes `:stroke-dash`; a bar has a width. None of them
;; takes another's.

;; ### An aesthetic the mark does not read is refused

;; A line has no marker to vary, so `:shape` on a line is not quietly
;; dropped -- it is reported, with the layer types that do read it:

(-> (rdatasets/datasets-iris)
    (pj/lay-line :sepal-length :sepal-width {:shape :species}))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; The warning above names the option, the layer types that do read it,
;; and everything this one accepts. Under `{:strict true}` the same text
;; is thrown rather than printed -- see
;; [Options and Scopes](./plotje_book.options_and_scopes.html) for that
;; setting.

;; ## Splitting without drawing

;; The split in the second question is the column's doing, not
;; `:color`'s -- so it can be asked for on its own. `:group` takes a
;; categorical column, divides the rows exactly as a categorical
;; `:color` would, and then draws nothing: every line comes out the
;; same colour, and there is no legend. It is how you get one line per
;; country without twelve colours in a strip down the side:

(-> {:month (vec (concat (range 1 5) (range 1 5) (range 1 5)))
     :value [3 5 4 6 2 3 5 4 4 6 5 7]
     :country ["fr" "fr" "fr" "fr" "de" "de" "de" "de" "it" "it" "it" "it"]}
    (pj/lay-line :month :value {:group :country}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:lines s))
                 (= 1 (count (disj (:colors s) "none")))
                 (not-any? #{"fr" "de" "it"} (:texts s)))))])

;; A literal `:color` can be given alongside it, and survives the
;; split: the lines are grouped by country and all drawn in the one
;; grey. This is the shape behind many pale lines with a few named ones
;; over them.

(-> {:month (vec (concat (range 1 5) (range 1 5) (range 1 5)))
     :value [3 5 4 6 2 3 5 4 4 6 5 7]
     :country ["fr" "fr" "fr" "fr" "de" "de" "de" "de" "it" "it" "it" "it"]}
    (pj/lay-line :month :value {:group :country :color "#d0d0d0"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:lines s))
                 (= #{"rgb(208,208,208)"} (disj (:colors s) "none")))))])

;; ## See Also
;;
;; - [Core Concepts](./plotje_book.core_concepts.html) -- the mapping and
;;   scope vocabulary this chapter builds on
;; - [Placing Marks](./plotje_book.placing_marks.html) -- `:x` and `:y`
;;   given as values, offsets, and drawing-space placement
;; - [Layer Types](./plotje_book.layer_types.html) -- which aesthetics
;;   each layer type reads
;; - [Customization](./plotje_book.customization.html#color-and-fill) --
;;   choosing the colours themselves, once the mapping is decided
