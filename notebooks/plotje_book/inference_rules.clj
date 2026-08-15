;; # Inference Rules
;;
;; Plotje infers many parameters automatically so you can write
;; less and get reasonable defaults. This notebook walks each rule
;; with a worked example: a small pose, the rendered plot, and a
;; description of what was inferred. Every rule is also checked
;; against the resolved plot on every run, so the claims here stay
;; honest as the library evolves.
;;
;; This chapter is a reference: each rule with its default and its
;; override. For the conceptual overview, read [Poses](./plotje_book.pose_model.html)
;; and [Core Concepts](./plotje_book.core_concepts.html) first. On a
;; first read through the book you can skim or skip it, and return
;; when you need a specific default. The examples use small inline
;; datasets so the relationships are easy to read at a glance.

(ns plotje-book.inference-rules
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## A Worked Example
;;
;; Before the rule-by-rule tour, here is what "inference" looks
;; like in practice: a five-point scatter where Plotje filled in
;; almost everything for us.

(def five-points
  {:x [1.0 2.0 3.0 4.0 5.0]
   :y [2.1 4.3 3.0 5.2 4.8]})

(def scatter-pose
  (-> five-points
      (pj/lay-point :x :y)))

scatter-pose

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan scatter-pose)
          p (first (:panels plan))
          g (first (:groups (first (:layers p))))
          ;; Render with an explicit :color set to the configured
          ;; default; the rendered RGBA must match what we get with
          ;; no :color (where inference picks the same default).
          ;; This avoids reaching into the private hex->rgba helper.
          default-hex (:default-color (pj/config))
          explicit-default-color (-> {:x [1] :y [1]}
                                     (pj/lay-point :x :y {:color default-hex})
                                     pj/plan
                                     :panels first :layers first :groups first :color)]
      (and (= 5 (:points (pj/svg-summary v)))
           (= "#333" (:default-color (pj/config)))
           (= :single (:layout-type plan))
           (= 1 (count (:panels plan)))
           (= "x" (:x-label plan))
           (= "y" (:y-label plan))
           (nil? (:legend plan))
           (zero? (get-in plan [:layout :legend-w]))
           (= :linear (get-in p [:x-scale :type]))
           (= 1 (count (:groups (first (:layers p)))))
           (= explicit-default-color (:color g)))))])

;; Notice what was inferred:
;;
;; - The x-axis label `"x"` and y-axis label `"y"`, taken from
;;   the column keywords
;; - A linear scale on each axis, since both columns are numeric
;; - The data range `[1.0, 5.0]` widened to `[0.8, 5.2]` -- a 5%
;;   padding so the extreme points do not sit on the panel edge
;; - Round tick values: `1.0, 1.5, 2.0, ...`
;; - No legend, since no color mapping was given
;; - A single point group rendered in the default color (dark gray,
;;   `#333`)
;;
;; Each of those decisions is its own inference rule, with a default
;; and an explicit override.

;; ## Overrides at a Glance
;;
;; Every inference rule has an explicit override. The table below
;; lists them all -- scan it to find what you need, then jump to the
;; matching section for the details and worked examples.
;;
;; | What is inferred | Default | Override |
;; |:-----------------|:--------|:---------|
;; | Column selection | one column fills x; two fill x, y; three fill x, y, color | explicit column args in `pj/pose` or `pj/lay-*` |
;; | Column type | dtype inspection | `:x-type`, `:y-type`, `:color-type` in pose or layer options |
;; | Aesthetic classification | the data decides: a name it carries is a column, anything else is a value | `{:color {:column "red"}}` / `{:color {:value "red"}}` |
;; | Grouping | categorical color column | `:group` aesthetic |
;; | Layer type (mark + stat) | column types (see Layer Type section) | `pj/lay-point`, `pj/lay-histogram`, etc. |
;; | Domain extent | data range + 5% padding | `(pj/scale pose :x {:domain [0 10]})` |
;; | Domain zero-anchor | bar/stacked charts include zero | `(pj/scale pose :y {:domain [5 20]})` |
;; | Fill domain | `[0.0, 1.0]` for fill position | `(pj/scale pose :y {:domain [0 2]})` |
;; | Tick values | round intervals (linear), powers of 10 (log) | `(pj/scale pose :x {:breaks [1 2 3]})` |
;; | Tick labels | number formatting, calendar formatting | `(pj/scale pose :x {:breaks [...] :labels [...]})` |
;; | Axis labels | column name, with underscores replaced by spaces | `(pj/options {:x-label "Custom"})` |
;; | Color legend | categorical = discrete, numerical = continuous, none = no legend | `:color` mapping controls presence |
;; | Size legend | 5 graduated circles when `:size` maps to numerical column | `:size` mapping controls presence |
;; | Alpha legend | ~5 graduated opacity squares (count varies with range) when `:alpha` maps to numerical column | `:alpha` mapping controls presence |
;; | Layout padding | adjusts for title, labels, legend | `:width`, `:height` in options |
;; | Layout type | single, facet-grid, multi-variable | `pj/facet`, multiple x-y pairs |
;; | Coordinate system | `:cartesian` | `(pj/coord :flip)`, `(pj/coord :polar)` |
;;
;; The sections below walk each rule in detail. The order roughly
;; follows how a pose is resolved into a plot -- column selection,
;; column types, aesthetics, grouping, layer type, domains, ticks,
;; labels, legends, layout, coord flip -- with two cross-cutting
;; closing sections on how the rules combine in multi-layer plots
;; and a diagram of the full resolution flow.

;; ## Column Selection
;;
;; When column names are omitted, Plotje infers them from
;; the dataset shape:
;;
;; | Number of columns | Inferred mapping |
;; |:------------------|:-----------------|
;; | 1 | first column becomes x |
;; | 2 | first becomes x, second becomes y |
;; | 3 | first becomes x, second becomes y, third becomes color |
;; | 4+ | no inference -- see the note below |
;;
;; The same rule applies whether you start with `pj/lay-*` on raw
;; data or `pj/pose` on raw data. Both read the first 1-3 columns
;; of the dataset in the order they appear and build the mapping
;; from there.
;;
;; One column:

(-> {:values [1 2 3 4 5 6]}
    pj/lay-histogram)

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; Two columns:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    pj/lay-point)

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; Three columns -- the third becomes `:color`:

(-> {:x [1 2 3 4] :y [4 5 6 7] :g ["a" "a" "b" "b"]}
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (some #{"a"} (:texts s)))))])

;; ### Pose construction infers the same mapping
;;
;; Calling `pj/pose` on raw data without explicit column arguments
;; runs the same column-selection rule. A 1-3 column dataset gets
;; its mapping filled in; the resulting pose carries the mapping
;; but has no layer attached yet, so layer type inference (covered
;; below) supplies the mark at render time.

(def two-col-pose
  (pj/pose {:x [1.0 2.0 3.0 4.0 5.0]
            :y [1.0 4.0 9.0 16.0 25.0]}))

two-col-pose

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; The inferred mapping is visible on the pose itself:

(-> two-col-pose (select-keys [:mapping :layers]) kind/pprint)

(kind/test-last [(fn [pose] (and (= {:x :x :y :y} (:mapping pose))
                                 (empty? (:layers pose))))])

;; ### 4+ columns
;;
;; With four or more columns there is no unambiguous default, so
;; inference stops:
;;
;; - `(pj/lay-* data)` throws with a message listing the available
;;   columns, asking you to pass explicit `:x` and `:y`.
;; - `(pj/pose data)` is gentler -- it builds a pose with the data
;;   attached but no mapping, so you can add one downstream with
;;   `(pj/pose pose :col-a :col-b)` or `(pj/lay-point pose :col-a :col-b)`.
;;
;; When you provide explicit columns, inference is skipped -- you
;; are in full control:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Column Types
;;
;; Once columns are selected, the next step is determining the type
;; of each column -- **numerical**, **categorical**, or **temporal**.
;; This determines the scale type, domain, tick style, and the
;; default mark.
;;
;; | Column dtype | Inferred type |
;; |:-------------|:--------------|
;; | float, int | `:numerical` |
;; | string, keyword, boolean, symbol, text | `:categorical` |
;; | LocalDate, LocalDateTime, Instant, java.util.Date | `:temporal` (numerical, with calendar-aware ticks) |
;;
;; A categorical column produces a band scale with string domain values.
;; Compare:

(def animals
  {:animal ["cat" "dog" "bird" "fish"]
   :count [12 8 15 5]})

(def bar-pose
  (-> animals
      (pj/lay-bar :animal :count)))

bar-pose

(kind/test-last
 [(fn [v]
    (let [p (first (:panels (pj/plan bar-pose)))]
      (and (= 4 (:polygons (pj/svg-summary v)))
           (= ["cat" "dog" "bird" "fish"] (:x-domain p))
           (true? (:categorical? (:x-ticks p))))))])

;; The x-axis lays out the four animal names in order of appearance
;; -- strings, treated as a categorical band scale. The y-axis starts
;; at zero because this is a bar chart.

;; ### Temporal columns
;;
;; Plotje accepts `java.util.Date` (from Clojure's `#inst` reader
;; literal), `LocalDate`, `LocalDateTime` and `Instant`. Each is
;; detected automatically and converted to epoch-milliseconds -- one
;; number per value -- before any scale is built.

(def temporal-pose
  (-> {:date [#inst "2024-01-01" #inst "2024-06-01" #inst "2024-12-01"]
       :val [10 25 18]}
      (pj/lay-point :date :val)))

temporal-pose

(kind/test-last
 [(fn [_]
    (let [p (first (:panels (pj/plan temporal-pose)))]
      (and (number? (first (:x-domain p)))
           (= 10 (count (:values (:x-ticks p))))
           (= "Feb-01" (first (:labels (:x-ticks p)))))))])

;; The axis reads as dates. The plan underneath it holds none: the
;; scale is an ordinary `:linear` one, the domain is a pair of
;; epoch-millisecond numbers, and only the tick labels are
;; calendar-aware. This is what the type table above means by
;; "numerical, with calendar-aware ticks" -- it describes the scaling,
;; not the display.

(let [panel (first (:panels (pj/plan temporal-pose)))]
  {:x-scale       (:x-scale panel)
   :x-domain      (:x-domain panel)
   :x-tick-labels (:labels (:x-ticks panel))})

(kind/test-last
 [(fn [m]
    (and (= {:type :linear} (:x-scale m))
         (every? number? (:x-domain m))
         (= 10 (count (:x-tick-labels m)))))])

;; The label format follows the span the axis covers. The same
;; two-point layer over three spans gives clock time, weekday and
;; hour, and year and month:

(let [labels (fn [a b]
               (-> {:when [a b] :level [1 2]}
                   (pj/lay-point :when :level)
                   pj/plan :panels first :x-ticks :labels))]
  {:six-hours  (labels #inst "2024-01-15T00:00" #inst "2024-01-15T06:00")
   :three-days (labels #inst "2024-01-15" #inst "2024-01-18")
   :nine-years (labels #inst "2015-01-15" #inst "2024-01-15")})

(kind/test-last
 [(fn [m]
    (and (every? #(re-matches #"\d{2}:\d{2}" %) (:six-hours m))
         (every? #(re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" %) (:three-days m))
         (every? #(re-matches #"\d{4}-\d{2}" %) (:nine-years m))))])

;; Over the longer spans the tick values are placed on calendar
;; boundaries as well, rather than at evenly spaced numbers: the
;; nine-year axis above carries one tick per year, and the twelve-month
;; axis at the top of this section carries one per month. Below a month
;; the ticks are evenly spaced and it is the label format alone that is
;; calendar-aware.

;; ### Overriding inferred types with `:x-type` / `:y-type`
;;
;; Sometimes a numeric column is really categorical -- for example,
;; hours of the day, years, or subject IDs. The inference system sees
;; numbers and treats them as numerical, but you may want discrete
;; categorical bands. Pass `:x-type :categorical` (or `:y-type`) to
;; the pose or layer options to override:

(def hour-bar-pose
  (-> {:hour [9 10 11 12] :count [5 8 12 7]}
      (pj/lay-bar :hour :count {:x-type :categorical})))

hour-bar-pose

(kind/test-last
 [(fn [v]
    (and (= 4 (:polygons (pj/svg-summary v)))
         (= ["9" "10" "11" "12"]
            (:x-domain (first (:panels (pj/plan hour-bar-pose)))))))])

;; Four bars at discrete hour bands. Without the override,
;; `lay-bar` would reject the numeric `:hour` column; with
;; it, the column is treated as categorical (values cast to strings
;; for display). The same override exists for `:y-type` and for
;; `:color-type` (see the Grouping section below for a `:color-type`
;; example).

;; ## Aesthetic Resolution
;;
;; The `:color` parameter does different things depending on
;; what you pass. Each aesthetic channel (`:color`, `:size`,
;; `:alpha`, `:shape`, `:text`) is classified as either a column
;; reference or a written value.

;; ### Column reference -- colored by palette

(def colored-pose
  (-> {:x [1 2 3 4 5 6]
       :y [3 5 4 7 6 8]
       :g ["a" "a" "a" "b" "b" "b"]}
      (pj/lay-point :x :y {:color :g})))

colored-pose

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan colored-pose)
          layer (first (:layers (first (:panels plan))))]
      (and (= 6 (:points (pj/svg-summary v)))
           (= 2 (count (:groups layer)))
           (some? (:legend plan))
           (= 100 (get-in plan [:layout :legend-w])))))])

;; The categorical column `:g` splits the data into two groups, each
;; with its own color drawn from the palette. A legend appears on the
;; right (100 drawing units wide) and the panel shrinks to make room.
;;
;; The next section explores why a categorical color column creates
;; groups while a numeric color column does not.

;; ### Fixed color string -- single color, no legend

(def fixed-color-pose
  (-> five-points
      (pj/lay-point :x :y {:color "#E74C3C"})))

fixed-color-pose

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan fixed-color-pose)
          layer (first (:layers (first (:panels plan))))
          c (:color (first (:groups layer)))]
      (and (= 5 (:points (pj/svg-summary v)))
           (nil? (:legend plan))
           (zero? (get-in plan [:layout :legend-w]))
           (= 1 (count (:groups layer)))
           (= [(/ 231.0 255.0)
               (/ 76.0 255.0)
               (/ 60.0 255.0)
               1.0]
              c))))])

;; A written hex string maps every point to that single color: no
;; grouping, no legend, no legend strip. The hex was parsed into the
;; RGBA tuple `[0.906 0.298 0.235 1.0]`.

;; ### Named colors and string disambiguation
;;
;; CSS color names like `"red"` and `"steelblue"` also work as
;; fixed colors:

(-> five-points
    (pj/lay-point :x :y {:color "steelblue"}))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; This raises a question: since `:color` also accepts column names
;; (like `"species"` or `:species`), how does the system decide whether
;; `"red"` means the column `red` or the color red?
;;
;; The rule is: **check the dataset first**. If the value names a
;; column of the layer's data, it is a column reference. Otherwise it
;; is asked whether it is a color.
;;
;; Here is the full resolution order for a `:color` value:
;;
;; 1. If it names a dataset column, it is a column reference (grouping)
;; 2. If it starts with `#` and parses as hex, it is that color
;;    (`"#E74C3C"`, `"#F00"`)
;; 3. If it is a CSS color name, it is that color -- as a string
;;    (`"red"`, `"steelblue"`) or as a keyword (`:red`, `:steelblue`)
;; 4. Otherwise, error. A string is reported against both readings,
;;    since a hex code written without its `#` arrives that way. A
;;    keyword is reported as a missing column alone: a keyword naming a
;;    color would have been drawn at step 3, so the only reading left
;;    to name is the column.
;;
;; A keyword and a string are asked the same two questions, in the same
;; order. What differs is what each can match -- matching is strict, so
;; `:species` finds a keyword-named column and `"species"` finds a
;; string-named one -- and how much the error says.
;;
;; Hex without its `#` is **not** a color. clojure2d reads a bare `abc`
;; as `#aabbcc`, which would make `"beef"` and `"fff"` colors -- far
;; likelier mistyped column names. Write the `#`.
;;
;; In practice, ambiguity is rare. Column names like `"species"` or
;; `"temperature"` are not valid CSS colors, and color names like
;; `"red"` are unlikely column names. When true ambiguity exists, say
;; which you mean: `{:color {:column "red"}}` reads the column and
;; `{:color {:value "red"}}` draws the color.

;; Verify: `"red"` is a fixed color when the dataset has no `red` column:

(def red-color-pose
  (-> five-points
      (pj/lay-point :x :y {:color "red"})))

red-color-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan red-color-pose)
          c (:color (first (:groups (first (:layers (first (:panels plan)))))))]
      (and (nil? (:legend plan))
           (> (first c) 0.9))))])

;; No legend, points drawn red -- treated as a fixed color, not a
;; column.

;; ### No color -- default gray

;; The Worked Example at the top of the chapter shows this case:
;; with no `:color` mapping, all points render in the default dark
;; gray (`#333`) and no legend appears.

;; ## Grouping
;;
;; The colored examples above all rest on the same concept:
;; **grouping** controls how data is split into independent subsets.
;; Each group gets its own visual elements -- its own set of points,
;; its own regression line, its own density curve, its own bar in a
;; dodged layout.
;;
;; Grouping can be **derived** (from a categorical `:color` mapping)
;; or **explicit** (via the `:group` aesthetic).

;; ### Categorical color implies grouping
;;
;; When `:color` maps to a categorical column (as with `colored-pose`
;; above), the data is split into one group per category. Each group
;; gets a distinct palette color and a legend entry:

colored-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan colored-pose)
          layer (first (:layers (first (:panels plan))))]
      (and (= 2 (count (:groups layer)))
           (= ["a" "b"] (mapv :label (:groups layer)))
           (some? (:legend plan)))))])

;; Two groups, two legend entries -- one per category in `:g`.

;; ### Numeric color does not create groups
;;
;; When `:color` maps to a numerical column, data is NOT split.
;; Instead, each point gets an individual color from a continuous
;; gradient. There is one group, and the legend is continuous
;; with 20 pre-computed color stops.

(def numeric-color-pose
  (-> {:x [1 2 3 4 5]
       :y [2 4 3 5 4]
       :val [10 20 30 40 50]}
      (pj/lay-point :x :y {:color :val})))

numeric-color-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan numeric-color-pose)
          layer (first (:layers (first (:panels plan))))]
      (and (= 1 (count (:groups layer)))
           (= :continuous (:type (:legend plan)))
           (= 20 (count (:stops (:legend plan)))))))])

;; A single group with a continuous legend of 20 color stops -- the
;; color is a visual encoding, not a grouping variable.

;; ### Overriding color type with `:color-type`
;;
;; Sometimes a numeric column is really a categorical identifier -- for
;; example, subject IDs in a repeated-measures study. Inference
;; treats numeric columns as continuous, but you want discrete
;; groups. Setting `:color-type :categorical` overrides this so
;; the column is treated as categorical despite its numeric dtype.
;;
;; **Inference provides good defaults, but the user can always
;; override.**

(def study-data
  {:subject [1 1 1 2 2 2 3 3 3]
   :day     [1 2 3 1 2 3 1 2 3]
   :score   [5 7 6 3 4 5 8 9 7]})

;; Without override -- one group, continuous gradient:

(def study-continuous-pose
  (-> study-data
      (pj/lay-line :day :score {:color :subject})))

study-continuous-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan study-continuous-pose)
          layer (first (:layers (first (:panels plan))))]
      (and (= 1 (count (:groups layer)))
           (= :continuous (:type (:legend plan))))))])

;; With `:color-type :categorical` -- three groups, one per subject:

(def study-categorical-pose
  (-> study-data
      (pj/lay-line :day :score {:color :subject
                                :color-type :categorical})))

study-categorical-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan study-categorical-pose)
          layer (first (:layers (first (:panels plan))))]
      (and (= 3 (count (:groups layer)))
           (= 3 (count (:entries (:legend plan)))))))])

;; The same data, the same columns -- but `:color-type :categorical`
;; changes inference from "one gradient" to "three distinct groups."
;; This affects grouping, line splitting, legend style, and palette
;; assignment. The rendered plots differ:

(-> {:subject [1 1 1 2 2 2 3 3 3]
     :day     [1 2 3 1 2 3 1 2 3]
     :score   [5 7 6 3 4 5 8 9 7]}
    (pj/lay-line :day :score {:color :subject
                              :color-type :categorical})
    pj/lay-point
    (pj/options {:title "Scores by Subject (categorical override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (pos? (:lines s))
                                (pos? (:points s)))))])

;; ### Explicit grouping with `:group`
;;
;; The `:group` aesthetic splits data into groups without
;; assigning distinct colors or creating a legend. This is useful
;; when you want per-group statistics but uniform appearance.

(def grouped-data
  {:x [1 2 3 4 5 6]
   :y [3 5 4 7 6 8]
   :g ["a" "a" "a" "b" "b" "b"]})

(def explicit-group-pose
  (-> grouped-data
      (pj/lay-point :x :y {:group :g})))

explicit-group-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan explicit-group-pose)
          layer (first (:layers (first (:panels plan))))]
      (and (= 2 (count (:groups layer)))
           (nil? (:legend plan)))))])

;; Two groups, but no legend and no color differentiation.
;; Use `:group` when you need separate statistical fits but
;; want a uniform visual style.

;; ### What grouping affects
;;
;; Grouping determines how statistical transformations operate.
;; Without grouping, `(pj/lay-smooth {:stat :linear-model})` fits one
;; regression line through all the data. With grouping, it fits one
;; line per group.

;; One regression line -- no grouping:

(-> grouped-data
    (pj/pose :x :y)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 6 (:points s))
                                (= 1 (:lines s)))))])

;; Two regression lines -- grouped by color:

(-> grouped-data
    (pj/pose :x :y {:color :g})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 6 (:points s))
                                (= 2 (:lines s)))))])

;; The same applies to other statistics: density curves, LOESS
;; smoothers, boxplots, and dodge/stack positioning all operate
;; per group.

;; ## Layer Type
;;
;; When you use `pj/pose` without an explicit `pj/lay-*` call,
;; Plotje infers the **layer type** -- a mark + stat bundle --
;; from the column types of the referenced columns.
;;
;; ### Single-column cases
;;
;; | Column type | Inferred | Mark + stat |
;; |:------------|:---------|:------------|
;; | numerical | histogram | `:bar` + `:bin` |
;; | temporal | histogram (over epoch-ms, with calendar-aware ticks) | `:bar` + `:bin` |
;; | categorical | bar chart of category counts | `:rect` + `:count` |
;;
;; ### Two-column cases
;;
;; | x type | y type | Inferred | Mark + stat |
;; |:-------|:-------|:---------|:------------|
;; | numerical | numerical | scatter | `:point` + `:identity` |
;; | temporal | numerical | time-series line | `:line` + `:identity` |
;; | categorical | numerical | boxplot (vertical) | `:boxplot` + `:boxplot` |
;; | numerical | categorical | boxplot (horizontal) | `:boxplot` + `:boxplot` |
;; | any other pair | | scatter (fallback) | `:point` + `:identity` |
;;
;; Fallback pairs include temporal x + categorical y, categorical x +
;; categorical y, and temporal x + temporal y. These are rarer in
;; practice, and giving them a dedicated inference is deferred. You
;; can always override with an explicit `pj/lay-*` call; the
;; inferred layer type is only a default.
;;
;; When you use `pj/lay-point`, `pj/lay-histogram`, etc., the layer
;; type's stat takes precedence -- column-type inference is bypassed.
;;
;; A single numerical column produces a histogram:

(def hist-pose
  (-> five-points
      (pj/pose :x)))

hist-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
      (and (pos? (:polygons (pj/svg-summary v)))
           (= :bar (:mark layer)))))])

;; The inferred layer is a histogram -- a `:bar` mark fed by the
;; `:bin` stat, so the data is binned into rectangles before
;; rendering.

;; A single temporal column also becomes a histogram, binned over
;; epoch-milliseconds with calendar-aware tick labels:

(def temporal-hist-pose
  (-> {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"
              #inst "2024-04-01" #inst "2024-05-01"]}
      (pj/pose :date)))

temporal-hist-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
      (and (pos? (:polygons (pj/svg-summary v)))
           (= :bar (:mark layer)))))])

;; A single categorical column produces a bar chart of counts:

(def count-pose
  (-> animals
      (pj/pose :animal)))

count-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
      (and (= 4 (:polygons (pj/svg-summary v)))
           (= :rect (:mark layer)))))])

;; The inferred layer uses a `:rect` mark fed by the `:count` stat,
;; which tallied each of the 4 categories.

;; Two numerical columns produce a scatter (the chapter's opening
;; `scatter-pose` is such a pose):

(def num-num-pose
  (-> five-points (pj/pose :x :y)))

num-num-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
      (and (= 5 (:points (pj/svg-summary v)))
           (= :point (:mark layer)))))])

;; A temporal x with a numerical y infers a time-series line. Row
;; order is preserved, so pre-sort temporal data to avoid zigzag:

(def ts-line-pose
  (-> {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"]
       :val  [10 25 18]}
      (pj/pose :date :val)))

ts-line-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
      (and (= 1 (:lines (pj/svg-summary v)))
           (= :line (:mark layer)))))])

;; A categorical x with a numerical y infers a boxplot -- the default
;; for summarizing a distribution across groups:

(def boxplot-pose
  (-> {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]
       :val     [8  10  12  18  20  22  14  15  17]}
      (pj/pose :species :val)))

boxplot-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
      (and (pos? (:lines (pj/svg-summary v)))
           (= :boxplot (:mark layer))
           (= 3 (count (:boxes layer))))))])

;; A numerical x with a categorical y infers a horizontal boxplot --
;; the same summary laid out with the category axis on y:

(def horizontal-boxplot-pose
  (-> {:val     [8  10  12  18  20  22  14  15  17]
       :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
      (pj/pose :val :species)))

horizontal-boxplot-pose

(kind/test-last
 [(fn [v]
    (let [layer (first (:layers (first (:panels (pj/plan horizontal-boxplot-pose)))))]
      (and (pos? (:lines (pj/svg-summary v)))
           (= :boxplot (:mark layer))
           (= 3 (count (:boxes layer))))))])

;; ## Domains
;;
;; Numerical domains extend 5% beyond the data range so points
;; aren't clipped at the edges.

scatter-pose

(kind/test-last
 [(fn [_]
    (let [p (first (:panels (pj/plan scatter-pose)))]
      (and (== 0.8 (first (:x-domain p)))
           (== 5.2 (second (:x-domain p))))))])

;; The x-domain is `[0.8, 5.2]` -- the data range `[1.0, 5.0]` plus
;; 0.2 padding on each side (5% of the data range, 4.0).
;;
;; Special domain rules apply in certain contexts:
;;
;; Bar charts always include zero on the value axis -- for these
;; vertical bars, that is the y-domain:

bar-pose

(kind/test-last
 [(fn [_]
    (let [p (first (:panels (pj/plan bar-pose)))]
      (<= (first (:y-domain p)) 0)))])

;; Percentage-filled layers normalize the y-domain to `[0.0, 1.0]`:

(def fill-pose
  (-> {:x ["a" "a" "b" "b"]
       :g ["m" "n" "m" "n"]}
      (pj/lay-bar :x {:position :fill :color :g})))

fill-pose

(kind/test-last
 [(fn [_]
    (let [d (:y-domain (first (:panels (pj/plan fill-pose))))]
      (and (== 0.0 (first d))
           (== 1.0 (second d)))))])

;; The y-domain is exactly `[0.0, 1.0]` -- each category sums to 100%.

;; Multi-layer plots merge domains across layers -- see
;; "Multi-Layer Plots" below.

;; ## Ticks
;;
;; Once domains are computed, Plotje selects "nice" round tick
;; values. The logic depends on the scale type:
;;
;; - **Linear** -- wadogo selects ticks at round intervals (1, 2, 2.5, 5, ...)
;; - **Log** -- 1-2-5 nice numbers: powers of 10 when they give at
;;   least 3 ticks, otherwise intermediates at 1-2-5 or 1-2-3-5
;;   multiples per decade
;; - **Categorical** -- tick at each category, in order of appearance
;; - **Temporal** -- calendar-aware snapping (year, month, day, hour)
;;   with adaptive formatting
;;
;; Linear ticks for the scatter example:

scatter-pose

(kind/test-last
 [(fn [_]
    (let [p (first (:panels (pj/plan scatter-pose)))]
      (and (= [1.0 1.5 2.0 2.5 3.0 3.5 4.0 4.5 5.0]
              (:values (:x-ticks p)))
           (= ["1.0" "1.5" "2.0" "2.5" "3.0" "3.5" "4.0" "4.5" "5.0"]
              (:labels (:x-ticks p))))))])

;; Nine ticks from 1.0 to 5.0 at 0.5 intervals.
;;
;; Log ticks for a multi-decade range:

(def log-scale-pose
  (-> {:x [0.1 1.0 10.0 100.0 1000.0]
       :y [5 10 15 20 25]}
      (pj/lay-point :x :y)
      (pj/scale :x :log)))

log-scale-pose

(kind/test-last
 [(fn [_]
    (let [p (first (:panels (pj/plan log-scale-pose)))]
      (and (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
           (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))])

;; Five ticks at exact powers of 10 -- no irrational intermediates.
;; Whole numbers display without decimals, sub-1 values use minimal
;; decimal places.

;; Categorical ticks match domain order:

bar-pose

(kind/test-last
 [(fn [_]
    (let [p (first (:panels (pj/plan bar-pose)))]
      (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))])

;; ## Axis Labels
;;
;; Labels come from column names. Underscores and hyphens become spaces.

(def iris-label-pose
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)))

iris-label-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan iris-label-pose)]
      (and (= "sepal length" (:x-label plan))
           (= "sepal width" (:y-label plan)))))])

;; When only one column is specified, the y-axis shows computed
;; counts. There is no y source column, so there is no column name to
;; draw a y-label from, and the y-label stays empty:

(def x-only-pose
  (-> five-points (pj/pose :x)))

x-only-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan x-only-pose)]
      (and (= "x" (:x-label plan))
           (nil? (:y-label plan)))))])

;; Explicit labels override inference:

(def explicit-label-pose
  (-> five-points
      (pj/lay-point :x :y)
      (pj/options {:x-label "Length (cm)" :y-label "Width (cm)"})))

explicit-label-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan explicit-label-pose)]
      (and (= "Length (cm)" (:x-label plan))
           (= "Width (cm)" (:y-label plan)))))])

;; ## Legends
;;
;; A legend appears when a column is mapped to color. Three cases:
;;
;; A categorical color mapping produces a discrete legend with one entry per category:

colored-pose

(kind/test-last
 [(fn [_]
    (let [leg (:legend (pj/plan colored-pose))]
      (and (= :g (:title leg))
           (= 2 (count (:entries leg))))))])

;; The legend's title is the column name; each entry has a `:label`
;; and a palette color.
;;
;; No color mapping means no legend:

scatter-pose

(kind/test-last
 [(fn [_] (nil? (:legend (pj/plan scatter-pose))))])

;; A fixed color string also suppresses the legend:

fixed-color-pose

(kind/test-last
 [(fn [_] (nil? (:legend (pj/plan fixed-color-pose))))])

;; A numeric color mapping produces a continuous legend (gradient bar):

(def continuous-color-pose
  (-> {:x [1 2 3] :y [4 5 6] :val [10 20 30]}
      (pj/lay-point :x :y {:color :val})))

continuous-color-pose

(kind/test-last
 [(fn [_]
    (let [leg (:legend (pj/plan continuous-color-pose))]
      (and (= :continuous (:type leg))
           (= 20 (count (:stops leg))))))])

;; ### Size Legend
;;
;; When `:size` maps to a numerical column, a size legend shows
;; five graduated circles spanning the data range, with radii
;; proportional to the values they represent.

(def size-legend-pose
  (-> {:x [1 2 3 4 5] :y [1 2 3 4 5] :s [10 20 30 40 50]}
      (pj/lay-point :x :y {:size :s})))

size-legend-pose

(kind/test-last
 [(fn [_]
    (let [leg (:size-legend (pj/plan size-legend-pose))]
      (and (= :size (:type leg))
           (= :s (:title leg))
           (= 5 (count (:entries leg))))))])

;; The legend has 5 entries, each pairing a value with a circle of
;; the corresponding radius. No size mapping means no size legend:

scatter-pose

(kind/test-last
 [(fn [_] (nil? (:size-legend (pj/plan scatter-pose))))])

;; ### Alpha Legend
;;
;; When `:alpha` maps to a numerical column, an alpha legend shows
;; graduated opacity squares -- about five nice 1/2/5 breaks; the
;; exact count depends on the range (here `[0.1, 0.9]` yields four).

(def alpha-legend-pose
  (-> {:x [1 2 3 4 5] :y [1 2 3 4 5] :a [0.1 0.3 0.5 0.7 0.9]}
      (pj/lay-point :x :y {:alpha :a})))

alpha-legend-pose

(kind/test-last
 [(fn [_]
    (let [leg (:alpha-legend (pj/plan alpha-legend-pose))]
      (and (= :alpha (:type leg))
           (= :a (:title leg))
           (= 4 (count (:entries leg))))))])

;; No alpha mapping means no alpha legend:

scatter-pose

(kind/test-last
 [(fn [_] (nil? (:alpha-legend (pj/plan scatter-pose))))])

;; ## Layout
;;
;; Layout padding adjusts based on what elements are present --
;; titles, axis labels, and legends each reserve their own space.
;;
;; Compare a bare plot to one with title, labels, and legend:

scatter-pose

(def full-layout-pose
  (-> {:x [1 2 3 4 5 6]
       :y [3 5 4 7 6 8]
       :g ["a" "a" "a" "b" "b" "b"]}
      (pj/lay-point :x :y {:color :g})
      (pj/options {:title "My Plot"})))

full-layout-pose

(kind/test-last
 [(fn [_]
    (let [bare (pj/plan scatter-pose)
          full (pj/plan full-layout-pose)]
      (and (zero? (get-in bare [:layout :title-pad]))
           (pos? (get-in full [:layout :title-pad]))
           (zero? (get-in bare [:layout :legend-w]))
           (= 100 (get-in full [:layout :legend-w])))))])

;; The bare plot reserves no space for a title and no legend strip.
;; The full plot adds padding above for the title and 100 drawing
;; units on the right for the legend.

;; Layout type is also inferred from the pose structure:
;;
;; - A single panel is `:single`
;; - A facet grid (`:facet-row` or `:facet-col`) is `:facet-grid`
;; - Multiple x-y pairs (scatter plot matrix) are `:multi-variable`

scatter-pose

(kind/test-last
 [(fn [_] (= :single (:layout-type (pj/plan scatter-pose))))])

;; ## Coordinate Flipping
;;
;; Setting `:coord :flip` swaps the visual axes. The data stays the
;; same -- the categorical band that was on x ends up on y, with
;; ticks and labels following along.

(def normal-pose
  (-> animals
      (pj/lay-bar :animal :count)))

normal-pose

(def flip-pose
  (-> animals
      (pj/lay-bar :animal :count)
      (pj/coord :flip)))

flip-pose

(kind/test-last
 [(fn [v]
    (let [np (first (:panels (pj/plan normal-pose)))
          fp (first (:panels (pj/plan flip-pose)))]
      (and (= 4 (:polygons (pj/svg-summary v)))
           (true? (:categorical? (:x-ticks np)))
           (not (:categorical? (:y-ticks np)))
           (not (:categorical? (:x-ticks fp)))
           (true? (:categorical? (:y-ticks fp))))))])

;; The categorical axis moved from x to y.
;;
;; Labels are also swapped -- the x-label and y-label follow their
;; visual axis, not the data axis:

(def flipped-labels-pose
  (-> five-points
      (pj/lay-point :x :y)
      (pj/coord :flip)))

flipped-labels-pose

(kind/test-last
 [(fn [_]
    (let [plan (pj/plan flipped-labels-pose)]
      (and (= "y" (:x-label plan))
           (= "x" (:y-label plan)))))])

;; After flipping, the visual x-axis shows "y" and the visual y-axis
;; shows "x" -- labels track the visual axes.
;;
;; Polar coordinates (`:coord :polar`) are covered separately --
;; see the [Polar Coordinates](./plotje_book.polar.html) chapter
;; for rose charts, radial bars, and related plots.

;; ## Multi-Layer Plots
;;
;; When multiple layers share a panel, their domains are merged:

(def multi-pose
  (-> five-points
      (pj/pose :x :y)
      pj/lay-point
      (pj/lay-smooth {:stat :linear-model})))

multi-pose

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          p (first (:panels (pj/plan multi-pose)))]
      (and (= 5 (:points s))
           (= 1 (:lines s))
           (= 2 (count (:layers p))))))])

;; Two layers -- one `:point`, one `:line` -- sharing the same domain.
;; The line carries the regression curve as a polyline.

;; ## Resolution Overview
;;
;; The diagram below sketches how the rules above combine -- which
;; inferences feed which others on the way from a pose to a rendered
;; plot:

^:kindly/hide-code
(kind/mermaid "
graph TD
  POSE[\"pose + options\"]
  POSE --> CT[\"Column types\"]
  POSE --> AE[\"Aesthetics\"]
  CT --> GR[\"Grouping\"]
  AE --> GR
  CT --> ME[\"Layer type\"]
  GR --> STATS[\"Statistics\"]
  ME --> STATS

  STATS --> DOM[\"Domains\"]
  DOM --> TK[\"Ticks\"]

  POSE --> LBL[\"Axis labels\"]
  AE --> LEG[\"Color legend\"]
  AE --> SLEG[\"Size legend\"]
  AE --> ALEG[\"Alpha legend\"]

  DOM --> LAYOUT[\"Layout\"]
  LBL --> LAYOUT
  LEG --> LAYOUT
  SLEG --> LAYOUT
  ALEG --> LAYOUT

  DOM --> PLOT[\"Rendered plot\"]
  TK --> PLOT
  LBL --> PLOT
  LEG --> PLOT
  SLEG --> PLOT
  ALEG --> PLOT
  LAYOUT --> PLOT
  STATS --> PLOT

  style POSE fill:#e8f5e9
  style PLOT fill:#fff3e0
  style STATS fill:#e3f2fd
  style DOM fill:#e3f2fd
")

;; Column types and aesthetic classification are the starting
;; points; everything else flows from them. Statistics and domains
;; together set the geometry; labels, legends, and layout round out
;; the surrounding plot.

;; ## What's Next
;;
;; - [**Layer Types**](./plotje_book.layer_types.html) -- the full registry of marks, stats, and positions that inference selects from
;; - [**Relationships**](./plotje_book.relationships.html) -- see inference in action on scatter, regression, and SPLOM
