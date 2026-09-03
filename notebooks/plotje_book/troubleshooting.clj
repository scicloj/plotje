;; # Troubleshooting
;;
;; Common mistakes and how to fix them.

(ns plotje-book.troubleshooting
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Column Not Found
;;
;; **Symptom**: `"Column :foo (from :x) not found in dataset"`
;; error, listing the available columns at the end of the message.
;;
;; **Cause**: The column reference does not match a dataset column
;; name. Matching is strict -- `:foo` matches keyword column `:foo`
;; only, and `"foo"` matches string column `"foo"` only. The two
;; forms do not interchange. Three common triggers:
;;
;; **1. Typo.** A misspelled column name. Always check the spelling
;; against the dataset's actual columns:

(tc/column-names (rdatasets/datasets-iris))

(kind/test-last [(fn [v] (some #{:sepal-length} v))])

;; **2. Keyword vs string.** A CSV loaded without `:key-fn keyword`
;; produces string column names; using a keyword reference against
;; that dataset throws:

(try
  (-> (tc/dataset {"sepal_length" [5.0 6.0] "sepal_width" [3.0 3.5]})
      (pj/pose :sepal_length :sepal_width)
      pj/lay-point pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"Column :sepal_\w+.*not found" msg))])

;; The fix is to either pass `{:key-fn keyword}` when loading the
;; CSV (so the dataset has keyword columns) or to use string
;; references everywhere.
;;
;; **3. Whitespace or punctuation mismatch.** A column literally
;; named `"sepal length"` (with a space) does not match
;; `:sepal-length` (with a hyphen):

(try
  (-> (tc/dataset {"sepal length" [5.0 6.0] "sepal width" [3.0 3.5]})
      (pj/pose :sepal-length :sepal-width)
      pj/lay-point pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"Column :sepal-\w+.*not found" msg))])

;; Note that `:key-fn keyword` on `"sepal length"` produces
;; `:sepal length` -- a keyword whose printed form contains a
;; space, not the hyphenated form a Clojure reader would normally
;; produce. Spaces and other special characters in CSV headers
;; usually need a custom `:key-fn`, e.g.
;; `(comp keyword #(clojure.string/replace % " " "-"))`.

;; ## Wrong Chart Type from Inference
;;
;; **Symptom**: `pj/pose` produces a chart type that isn't what you
;; wanted -- a boxplot when you wanted individual points, a line
;; when you wanted a scatter.
;;
;; **Cause**: `pj/pose` infers the layer type from column types. The
;; defaults fit the most common use case for each column-type pair
;; (see [Inference Rules](./plotje_book.inference_rules.html#layer-type)),
;; but they can be overridden.
;;
;; **Fix**: Use an explicit `pj/lay-*` function. For example, a
;; categorical x with a numerical y defaults to a boxplot:

(-> (rdatasets/datasets-iris)
    (pj/pose :species :sepal-width))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; Use `pj/lay-point` if you want the individual points instead:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Numeric IDs Treated as Continuous Color
;;
;; **Symptom**: You color by a subject/group ID column that contains
;; numbers (e.g., 1, 2, 3), but instead of discrete colored groups you
;; get a single continuous gradient.
;;
;; **Cause**: The inference system sees a numeric column and treats it
;; as continuous. Continuous color means no grouping -- all data stays
;; in one group with a gradient legend.

(def subject-scores
  {:day     [1 2 3 4 1 2 3 4 1 2 3 4]
   :score   [3 5 4 6 6 7 5 8 8 9 7 10]
   :subject [1 1 1 1 2 2 2 2 3 3 3 3]})

;; Gradient (wrong for IDs) -- one line for the whole dataset, with
;; the color sampled from the gradient legend. Plotje also prints a
;; warning at the REPL pointing at the fix:

(-> subject-scores
    (pj/lay-line :day :score {:color :subject}))

(kind/test-last [(fn [v] (= 1 (:lines (pj/svg-summary v))))])

;; **Fix**: Add `:color-type :categorical` to override the inference --
;; three discrete groups, one line per subject:

(-> subject-scores
    (pj/lay-line :day :score {:color :subject :color-type :categorical}))

(kind/test-last [(fn [v] (= 3 (:lines (pj/svg-summary v))))])

;; See [Inference Rules](./plotje_book.inference_rules.html#overriding-color-type-with-color-type)
;; for the full mechanism.

;; ## Numeric Column Treated as Continuous Instead of Categorical
;;
;; **Symptom**: A column of discrete numbers (hour of day, year,
;; subject ID) is treated as a continuous axis. A categorical-axis
;; mark like `:boxplot`, `:violin`, or `:lollipop` rejects it with an
;; error like `"requires a categorical column"`. (`pj/lay-bar` does
;; not error on a numeric axis -- it draws a bar at each numeric
;; position; use the same fix below to get evenly-spaced bands
;; instead.)
;;
;; **Cause**: The column contains numbers, so column-type inference
;; classifies it as `:numerical`. These marks need `:categorical`.
;;
;; A boxplot keyed by hour runs into this -- `:hour` looks like
;; integers, so it is inferred numerical:

(try
  (-> {:hour [9 9 10 10 11 11] :value [1 2 3 4 5 6]}
      (pj/lay-boxplot :hour :value)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"requires a categorical column" msg))])

;; **Fix**: Add `:x-type :categorical` (or `:y-type :categorical` for
;; horizontal layouts) to override the inferred type. No need to
;; convert the column itself:

(-> {:hour [9 9 10 10 11 11] :value [1 2 3 4 5 6]}
    (pj/lay-boxplot :hour :value {:x-type :categorical}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; The override propagates into `infer-column-types`, so every
;; downstream step (scale type, tick placement, domain) treats
;; `:hour` as categorical. The same switch works for `:y-type` when
;; a numeric column is on the y axis of a horizontal boxplot or
;; similar layout. See
;; [Inference Rules](./plotje_book.inference_rules.html#overriding-inferred-types-with-x-type-y-type)
;; for a worked example.

;; ## Nudge on a Categorical Axis
;;
;; **Symptom**: Passing `:nudge-x` (or `:nudge-y`) to a label or point
;; layer whose corresponding axis is categorical raises an error like
;; `":nudge-x is a data-space shift and does not apply to a categorical
;; x axis"`.
;;
;; **Cause**: `:nudge-x`/`:nudge-y` shift coordinates by a data-space
;; amount. On a categorical axis the coordinates are still category
;; labels at this stage -- their drawing positions are assigned later by
;; the renderer -- so a numeric shift has no defined meaning. A value
;; label on a bar runs into this, because the bar's axis is categorical:

(try
  (-> {:species ["setosa" "versicolor" "virginica"] :pct [33.3 33.3 33.3]}
      (pj/lay-bar :species :pct)
      (pj/lay-text :species :pct {:text :pct :nudge-x -2})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #":nudge-x .* categorical x axis" msg))])

;; **Fix**: To move a mark by a distance on the page, use `:offset-x`
;; or `:offset-y`. These are drawing units applied after the scales, so
;; they apply on a categorical axis as on any other:

(-> {:species ["setosa" "versicolor" "virginica"] :pct [33.3 33.3 33.3]}
    (pj/lay-bar :species :pct)
    (pj/lay-text :species :pct {:text :pct :align-x :center :offset-y -6}))

(kind/test-last
 [(fn [fr]
    (= [nil -6]
       (->> fr pj/plan :panels first :layers (mapv :offset-y))))])

;; To place a label relative to its own point, anchor it with
;; `:align-x`/`:align-y` -- `:align-x :right` tucks the label
;; inside a bar's end. (To spread overlapping marks on a categorical
;; axis, use `:jitter` or `:position :dodge`.)

(-> {:species ["setosa" "versicolor" "virginica"] :pct [33.3 33.3 33.3]}
    (pj/lay-bar :species :pct {:color "#a6cee3"})
    (pj/lay-text :species :pct {:text :pct :align-x :right})
    (pj/coord :flip))

(kind/test-last
 [(fn [fr]
    (= :right
       (->> fr pj/plan :panels first :layers
            (filter #(= :text (:mark %)))
            first :style :align-x)))])

;; Anchoring is covered in
;; [Placing Marks](./plotje_book.placing_marks.html#anchoring-a-text-mark). `:nudge-x` and
;; `:nudge-y` remain available on numeric and temporal axes.

;; ## Log Scale via `:scale-x` / `:scale-y` Options
;;
;; **Symptom**: Passing `{:scale-x :log}` (or `{:scale-y :log}`)
;; to a layer or to `pj/options` prints a warning --
;; `"does not recognize option(s): [:scale-x]"` -- and the chart
;; comes out on a linear axis.
;;
;; **Cause**: `:scale-x` and `:scale-y` are not option keys at all. A
;; scale is set with `pj/scale`, called on the pose, or inside a
;; mapping written out in full -- `{:x {:column :carat :scale :log}}`.
;;
;; The wrong form does not throw; it warns and silently falls back
;; to a linear axis:

(with-out-str
  (-> (rdatasets/ggplot2-diamonds)
      (pj/lay-point :carat :price {:scale-y :log})
      pj/plan))

(kind/test-last
 [(fn [out] (re-find #"does not recognize option.*:scale-y" out))])

;; **Fix**: Use `pj/scale`:

(-> (rdatasets/ggplot2-diamonds)
    (pj/lay-point :carat :price {:alpha 0.1})
    (pj/scale :y :log))

(kind/test-last [(fn [v] (pos? (:points (pj/svg-summary v))))])

;; `pj/scale` takes the pose, an aesthetic -- an axis (`:x`, `:y`) or a
;; visual one (`:size`, `:alpha`, `:color`, `:fill`, `:shape`) -- and
;; either a type keyword (`:linear`, `:log`) or a scale specification
;; map with `:type` and an optional `:domain` override.
;; See the [Inference Rules](./plotje_book.inference_rules.html#domains)
;; chapter for how scale types and domains interact with column
;; inference.

;; ## x-Only Layer Types Do Not Accept a y Column
;;
;; **Symptom**: `"lay-histogram uses only the x column; do not pass
;; a y column"` error.
;;
;; **Cause**: Histogram, density, and rug layer types use only
;; the x column. Passing a y column is an error. (`lay-bar` is not
;; among them -- it uses a y column as the bar height when given one.)

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-histogram :sepal-length :sepal-width)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"uses only the x column" msg))])

;; **Fix**: Remove the y column:

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ## Categorical Column with Log Scale
;;
;; **Symptom**: `"Log scale requires numeric data"` error.
;;
;; **Cause**: Log scales only work with numerical columns. Categorical
;; columns (strings, keywords) have no meaningful log transform.

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-bar :species)
      (pj/scale :x :log)
      pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last [(fn [msg] (re-find #"[Ll]og scale" msg))])

;; **Fix**: Use a numerical column for the log-scaled axis, or drop
;; the log scale on the categorical axis.

;; ## Polar Coordinates with Unsupported Marks
;;
;; **Symptom**: `"Mark :line is not supported with polar
;; coordinates. Supported polar marks: (:bar :point :rect :rug
;; :text)"` (or the same message for `:area` and other unsupported
;; marks).
;;
;; **Cause**: Polar coordinates currently support a subset of marks:
;; `:bar`, `:point`, `:rect`, `:rug`, and `:text`. Layer types built
;; on these marks (such as `:histogram`, which renders as bars, and
;; `:bar` with a y column, which renders as rectangles) work too.

(try
  (-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
      (pj/lay-line :x :y)
      (pj/coord :polar)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"not supported with polar coordinates" msg))])

;; **Fix for now**: Use a supported mark. A bar chart flipped to polar
;; becomes a rose chart:

(-> (rdatasets/datasets-chickwts)
    (pj/pose :feed)
    pj/lay-bar
    (pj/coord :polar))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; Support for `:line`, `:area`, and other marks in polar is
;; planned. See the [Polar Coordinates](./plotje_book.polar.html)
;; chapter for the full set of currently supported marks and
;; examples.

;; ## Tooltip and Brush Not Working
;;
;; **Symptom**: You set `{:tooltip true}` but no tooltip appears when
;; hovering over points.
;;
;; **Cause**: Tooltip and brush interactivity use JavaScript that
;; requires a compatible notebook viewer. Static HTML export or some
;; viewers may not support it.
;;
;; **Fix**: Use [Clay](https://scicloj.github.io/clay/) or another
;; Kindly-compatible tool that supports `kind/hiccup` with embedded
;; scripts.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:tooltip true}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Faceting Keys in a Layer's Options Map
;;
;; **Symptom**: An error like
;; `"Faceting is plot-level, not layer-level. Use (pj/facet pose col) ..."`
;; when you put `:facet-col`, `:facet-row`, `:facet-x`, or
;; `:facet-y` inside a `pj/lay-*` options map.
;;
;; **Cause**: Faceting configures the plot as a whole, not a single
;; layer. Putting these keys in a layer's options map is rejected
;; with a guidance message.

(try
  (-> (rdatasets/datasets-iris)
      (pj/pose :sepal-length :sepal-width)
      (pj/lay-point {:facet-col :species})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"Faceting is plot-level" msg))])

;; **Fix**: Use `pj/facet` (single-axis) or `pj/facet-grid`
;; (two-axis) as a top-level step in the pipeline:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species))

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; ## Constant `:x` or `:y` in a Layer's Options
;;
;; **Symptom**: a note is needed at one fixed spot, and no column holds
;; the x and y it belongs at.
;;
;; **This is not an error.** `:x` and `:y` may be given as a value, the
;; same way a color may be `"red"` rather than a column. The layer
;; below places its text at x 6.5 and y 3.5 with no dataset of its
;; own, and a string `:text` on such a layer is the text itself
;; rather than a column name:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-text {:x 6.5 :y 3.5 :text "mean"}))

(kind/test-last [(fn [v] (some #{"mean"} (:texts (pj/svg-summary v))))])

;; The other shape a value takes is beside a column. When `:y` is a
;; column the layer describes the data, so the value given for `:x`
;; repeats for every row -- which is how a label at one fixed x is
;; written. The five team names below line up at x 33 rather than
;; sitting over their points, each at the revenue of its own row:

(-> {:team ["North" "South" "East" "West" "Central"]
     :spend [12 19 15 24 31]
     :revenue [30 45 38 62 74]}
    (pj/lay-point :spend :revenue)
    (pj/lay-text {:x 33 :y :revenue :text :team}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 5 (:points s))
                 (every? (set (:texts s))
                         ["North" "South" "East" "West" "Central"]))))])

;; `:x` and `:y` alone decide which of the two a value draws. A layer
;; that gives both as values does not read the data at all, so it draws
;; once; a value beside a column applies to each row, so it repeats. A
;; string `:text` splits the same way: it is the text on a layer of
;; values alone, and a column name once the layer has data to name a
;; column in.

;; A value given for `:x` or `:y` is a data value like any other, so it
;; takes part in the axis domains: a note placed beyond the data widens the axis
;; to hold it. To place a mark on the panel instead -- in drawing
;; units from the corner of the panel background, leaving the axes
;; alone -- give the layer `:in :drawing-area`. The
;; [Placing Marks](./plotje_book.placing_marks.html#placing-a-mark-on-the-panel-instead-of-in-the-data) chapter covers
;; that choice, and the two spaces the two readings belong to.
;;
;; Reference lines remain their own layer types: `pj/lay-rule-h` with
;; `:y-intercept` and `pj/lay-rule-v` with `:x-intercept` draw a line
;; across the whole panel, which one `:x` and one `:y` cannot say.

;; **A number is read against the data.** A dataset built without
;; column names is given integer ones, and a number in a mapping names
;; such a column where the data carries it:

(-> (tc/dataset [[1 2] [3 4] [5 7]])
    (pj/lay-point 0 1))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Where the data carries no column of that name, the same number is a
;; value to place a mark at, and the axis stretches to hold it. To say
;; which reading you mean rather than letting the data decide, write
;; the mapping in full: `{:x {:column 0}}` reads the column and
;; `{:x {:value 0}}` places every mark at zero. Renaming the columns is
;; the other remedy, and usually the clearer one:

(-> (tc/dataset [[1 2] [3 4] [5 7]])
    (tc/rename-columns [:x :y])
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; The [Datasets](./plotje_book.datasets.html#column-names) chapter covers column
;; names in full.

;; ## Two Panels Where One Was Wanted
;;
;; **Symptom**: a second `pj/lay-*` meant to draw on the same panel
;; draws beside it instead, and the plot comes out with two panels.
;;
;; **Cause**: a `pj/lay-*` call naming `:x` and `:y` joins the panel
;; whose own `:x` and `:y` match. When none matches it cannot share
;; those axes, so it becomes a panel. Below, the second bar layer names
;; a different value column from the first, so it gets one:

(-> {:cohort [:a :b :c] :growth [12 19 15] :tax [3 5 4]}
    (pj/pose :growth :cohort)
    (pj/lay-bar :growth :cohort {:color "#377eb8"})
    (pj/lay-bar :tax :cohort {:color "#e6550d"}))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; **Fix**: Add `pj/overlay` before the layers. Every layer added after
;; it goes on the panel it is added to, keeping its own columns, and
;; the axis covers every column drawn on it:

(-> {:cohort [:a :b :c] :growth [12 19 15] :tax [3 5 4]}
    pj/overlay
    (pj/lay-bar :growth :cohort {:color "#377eb8"})
    (pj/lay-bar :tax :cohort {:bar-width 0.4 :color "#e6550d"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 6 (:polygons s))
                                ;; The same two series as the picture
                                ;; above, now sharing one panel.
                                (= #{"rgb(55,126,184)" "rgb(230,85,13)"}
                                   (disj (:colors s) "none")))))])

;; To join one layer without changing where later layers go, write
;; `{:overlay true}` in that layer's own options map.
;;
;; Joining has to be asked for because two panels are right when the
;; columns are unrelated. There is a second remedy for the case where
;; the incoming values are the same quantity under another name: rename
;; those columns to the panel's own, and the layer joins without
;; `pj/overlay`. The
;; [Composition](./plotje_book.composition.html#overlay-on-the-same-panel)
;; chapter shows both.

;; ## Dataset Missing Columns a Template References
;;
;; **Symptom**: An error like
;; `"Cannot attach data: pose references column(s) [:group] not
;; present in the dataset. Available columns: [:x :y]"` when
;; calling `pj/with-data` on a dataless template pose.
;;
;; **Cause**: `pj/with-data` validates at attach time -- every
;; keyword column reference in the template must exist in the
;; dataset, or the attachment fails immediately.

(def template
  (-> (pj/pose nil {:x :x :y :y :color :group})
      pj/lay-point))

(try
  (-> template
      (pj/with-data {:x [1 2 3] :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"\[:group\] not present in the dataset" msg))])

;; **Fix**: Either rename the dataset columns to match the
;; template (`tc/rename-columns`), or adjust the template to
;; reference the columns the dataset has.

(-> (pj/pose nil {:x :x :y :y})
    pj/lay-point
    (pj/with-data {:x [1 2 3] :y [4 5 6]}))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; ## Horizontal Ranking Bars Draw Biggest-at-Bottom
;;
;; **Symptom**: A horizontal bar chart made with `(pj/coord :flip)`
;; shows the first row of the data at the bottom of the chart.
;; A descending-sorted "top-N" dataset ends up with the biggest
;; bar at the bottom instead of the top.
;;
;; **Cause**: `coord :flip` draws categories bottom-to-top in the
;; order they appear in the data (matching ggplot2's
;; `coord_flip()`).
;;
;; Descending data plotted as-is -- "A" (the biggest) renders at
;; the bottom, not the top:

(-> [{:category "A" :value 100}
     {:category "B" :value 50}
     {:category "C" :value 25}]
    (pj/lay-bar :category :value)
    (pj/coord :flip))

(kind/test-last [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; **Fix for now**: Sort the dataset ascending before plotting -- the
;; ascending order shows up top-to-bottom on the flipped axis,
;; so the biggest value lands at the top:

(-> [{:category "A" :value 100}
     {:category "B" :value 50}
     {:category "C" :value 25}]
    (tc/dataset)
    (tc/order-by [:value] :asc)
    (pj/lay-bar :category :value)
    (pj/coord :flip))

(kind/test-last [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; A future opt-in option (e.g. `(pj/coord :flip
;; {:reverse-categorical true})`) would remove the need to pre-sort.
;; Tracked in `CHANGELOG.md` Known limitations.

;; ## Point Sizes Changed From an Earlier Release
;;
;; **Symptom**: A plot with `{:size :some-column}` draws its middle
;; values larger than it used to, and the difference between the
;; smallest and largest points looks less dramatic.
;;
;; **Cause**: A size scale spreads the square root of the value across
;; the radii, so the area of a mark grows with the value rather than
;; with its square. Earlier releases spread the value itself, which
;; exaggerates the differences. The two ends of the range are
;; unchanged; the values between them moved.

(-> {:x [1 2 3 4 5 6] :y [1 1 1 1 1 1] :n [1 4 9 16 25 36]}
    (pj/lay-point :x :y {:size :n}))

(kind/test-last
 [(fn [fr]
    (let [radii #(sort (:sizes (pj/svg-summary %)))
          now (radii fr)
          before (radii (-> fr (pj/scale :size {:by :linear})))]
      ;; The two ends are the same radii as before. Every value
      ;; between them is drawn larger.
      (and (= (first now) (first before))
           (= (last now) (last before))
           (every? (fn [[a b]] (> a b))
                   (map vector (butlast (rest now)) (butlast (rest before)))))))])

;; **Fix**: `{:by :linear}` restores the earlier reading, and
;; `{:by :area}` gives the strict one, where equal steps in value are
;; equal steps in ink:

(-> {:x [1 2 3 4 5 6] :y [1 1 1 1 1 1] :n [1 4 9 16 25 36]}
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:by :linear}))

(kind/test-last
 [(fn [v] (= 6 (:points (pj/svg-summary v))))])

;; ## A `:size` or `:alpha` Column That Changes Nothing
;;
;; **Symptom**: `{:size :some-column}` on a line, boxplot or lollipop
;; draws marks of one size, and a warning names the marks that vary
;; the aesthetic.
;;
;; **Cause**: Only marks that draw a size per row can read a size
;; column. Among the built-in marks that is `pj/lay-point`. Every other
;; mark draws one width or one opacity for the whole layer, so the
;; column changes nothing. No legend is drawn for it either, since the
;; legend would describe an encoding the panel does not show.
;;
;; **Fix**: Write the value itself for a layer-wide size -- `{:size 2}`
;; on a line is a stroke width -- and map the column on a layer whose
;; mark varies it.

(-> {:x [1 2 3] :y [2 4 3] :r [1 2 3]}
    (pj/pose :x :y)
    (pj/lay-line {:size 2})
    (pj/lay-point {:size :r}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:points s)) (pos? (:lines s)))))])

;; ## Dodge Has No Effect on Point Layers

;; **Symptom**: Adding `:position :dodge` to `pj/lay-point` (or other
;; non-bar marks) does not spread points apart by group -- the plot
;; looks identical to the version without `:position :dodge`.
;;
;; **Cause**: `:position :dodge` is implemented for the bar mark
;; (`pj/lay-bar`). On point/line/jitter and
;; several other marks the option is accepted but silently ignored.
;;
;; The two plans below produce identical x-coordinates for the
;; rendered points -- `:position :dodge` has no effect on points:

(def points-data
  {:x [1 1 2 2 3 3] :y [10 15 20 25 30 35] :group ["A" "B" "A" "B" "A" "B"]})

(defn point-xs [pose]
  (-> pose pj/plan :panels first :layers first :groups
      (->> (mapcat :xs) sort vec)))

(= (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
   (point-xs (-> points-data (pj/lay-point :x :y {:color :group :position :dodge}))))

(kind/test-last [(fn [v] (true? v))])

;; **Fix for now**: For grouped categorical layouts use
;; `pj/lay-bar` (counting with x only, or using a y column as height);
;; dodge works there. To distinguish overlapping points by group on a
;; numeric x, encode the group with `:color`, `:shape`, or
;; pre-compute small offsets in the data. A proper dodge for points
;; is tracked in `CHANGELOG.md` Known limitations.

(-> {:cat   ["A" "A" "B" "B" "C" "C"]
     :y     [10 20 30 40 50 60]
     :group ["a" "b" "a" "b" "a" "b"]}
    (pj/lay-bar :cat :y {:color :group :position :dodge}))

(kind/test-last [(fn [v] (= 6 (:polygons (pj/svg-summary v))))])

;; ## Polar Bar Chart Has No Category Labels

;; **Symptom**: A bar chart flipped to polar (`(pj/coord :polar)`)
;; renders as a rose chart, but no category text appears anywhere
;; around the wedges.
;;
;; **Cause**: Polar coord does not currently emit angular tick labels
;; for bar-family marks -- the underlying axis machinery places
;; labels along Cartesian axes that polar replaces with a circular
;; layout, and the equivalent angular ticks are not yet implemented.
;;
;; The polar version shows the wedges sized by category, but the
;; category names are absent:

(-> (rdatasets/datasets-chickwts)
    (pj/pose :feed)
    pj/lay-bar
    (pj/coord :polar))

(kind/test-last [(fn [v] (zero? (count (filter #{"casein" "horsebean" "linseed"
                                                 "meatmeal" "soybean" "sunflower"}
                                               (:texts (pj/svg-summary v))))))])

;; **Fix for now**: Drop `(pj/coord :polar)` for the labeled view, or
;; combine the polar plot with a separate Cartesian-coord version
;; for the legend. A proper rose-chart label pass is tracked in
;; `CHANGELOG.md` Known limitations.

(-> (rdatasets/datasets-chickwts)
    (pj/pose :feed)
    pj/lay-bar)

(kind/test-last [(fn [v] (pos? (count (filter #{"casein" "horsebean" "linseed"
                                                "meatmeal" "soybean" "sunflower"}
                                              (:texts (pj/svg-summary v))))))])

;; ## Heatmap with Categorical Axes
;;
;; **Symptom**: `"class java.lang.String cannot be cast to class
;; java.lang.Number"` when passing a string column to
;; `pj/lay-tile`.
;;
;; **Cause**: `pj/lay-tile` (and the underlying `:bin2d` stat)
;; requires numeric x and y columns -- the tile boundaries are
;; numeric intervals. Categorical axes are not yet supported for
;; tile.

(try
  (-> {:x ["a" "b" "c"] :y ["a" "b" "c"] :v [1 2 3]}
      (pj/lay-tile :x :y {:fill :v})
      pj/plan)
  (catch Throwable t (.getMessage t)))

(kind/test-last
 [(fn [msg] (re-find #"String cannot be cast to.*Number" msg))])

;; **Fix**: render a numeric-indexed grid (1-N integers in place of
;; the categorical column) and pair `:breaks` with `:tick-labels` on the
;; axis so the tick text shows the original category names:

(-> (for [day (range 1 8) hour (range 0 24)]
      {:day day :hour hour :v (+ (* 0.3 (Math/sin (* 0.5 hour)))
                                 (* 0.2 (mod day 3)))})
    (pj/lay-tile :day :hour {:fill :v})
    (pj/scale :x {:type :linear
                  :breaks [1 2 3 4 5 6 7]
                  :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (every? texts ["Mon" "Sun"])))])

;; If a true categorical *axis* (with binning over labels rather
;; than numeric intervals) is what you need, that is tracked in
;; `CHANGELOG.md` Known limitations. The integer-plus-`:tick-labels`
;; pattern above covers most heatmap-with-categorical-axis cases.

;; ## Empty or All-Missing Column on a Grouping Layer
;;
;; **Symptom**: `"lay-boxplot requires a categorical column on either
;; :x or :y, but :c has no rows and :v has no rows"`, or `"has no
;; values"` where the column holds only `nil`.
;;
;; **Cause**: The layer groups its data by category, and no row carries
;; one. This is usually a data problem upstream of the plot -- a filter
;; that matched nothing, or a join that dropped every row:

(try
  (-> {:group [] :measurement []}
      (pj/lay-boxplot :group :measurement)
      pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"requires a categorical column.*has no rows" msg))])

;; A column holding nothing but `nil` reports having no *values*, which
;; separates the two cases -- rows that never arrived, against rows
;; that arrived empty:

(try
  (-> {:group [nil nil] :measurement [nil nil]}
      (pj/lay-boxplot :group :measurement)
      pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"has no values" msg))])

;; The message names neither column a *type*, deliberately. An empty
;; column and an all-`nil` one are both typed boolean by
;; tech.ml.dataset, and Plotje reads such a column as numerical so the
;; rest of the pipeline has something to work with -- so a message
;; naming the type would send you to check something that is not the
;; problem.

;; ## `:text` Given a Value That Names No Column
;;
;; **Symptom**: `"Column :nope (from :text) not found in dataset"`,
;; ending `"It is not a label either -- a label is a string"`.
;;
;; **Cause**: `:text` takes either a column reference, drawing one
;; row's value at each mark, or a literal string drawn at every mark.
;; A keyword that names no column is neither:

(try
  (-> {:x [1 2] :y [1 2]}
      (pj/lay-text :x :y {:text :nope})
      pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"not a label either" msg))])

;; Write a string for constant text -- `{:text "note"}` -- or name a
;; column the dataset carries.

;; ## An Axis `:domain` That Is Not Two Numbers
;;
;; **Symptom**: `"pj/scale :y :domain [0] is not a pair of two finite
;; numbers, as [0 100] is"`.
;;
;; **Cause**: What a `:domain` means depends on the column it is read
;; for. Against a continuous column it is the interval the panel spans,
;; which is two finite numbers; against a categorical one it is the
;; list of categories, in the order they are to be drawn. Since the
;; column decides, the check is made once the column is known:

(try
  (-> {:height [1 2 3] :weight [1 2 3]}
      (pj/lay-point :height :weight)
      (pj/scale :y {:domain [0]})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"not a pair of two finite numbers" msg))])

;; Write both ends -- `{:domain [0 100]}` -- or, to extend the interval
;; the data gives rather than replace it, write `:include` instead:
;; `{:include 0}` puts zero on the axis and leaves the other end to the
;; data. [Scales](./plotje_book.scales.html#making-an-axis-reach-a-value)
;; works through the difference.

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- the mapping and inference rules behind most of these symptoms

;; ## What's Next
;;
;; - [**Inference Rules**](./plotje_book.inference_rules.html) -- how defaults are chosen and overridden
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
;; - [**Exploring Plans**](./plotje_book.exploring_plans.html) -- inspect the data structures behind your plots
;; - [**Gallery**](./plotje_book.gallery.html) -- more working examples by chart type
