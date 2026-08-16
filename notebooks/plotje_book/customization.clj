;; # Customization
;;
;; How to adjust the look of a plot: dimensions, labels, scales,
;; mark styling, palettes, themes, and legend placement. Where a mark
;; goes, as opposed to how it looks, is
;; [Placing Marks](./plotje_book.placing_marks.html).
;;
;; Other appearance topics live in their natural homes:
;; column-to-aesthetic mapping in
;; [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers), reference
;; lines and bands in
;; [Core Concepts](./plotje_book.core_concepts.html#plot-options-and-reference-lines) (constant
;; positions) and
;; [Timelines](./plotje_book.timelines.html#annotated-time-series) (temporal intercepts),
;; and tooltips/brushing in
;; [Interactivity](./plotje_book.interactivity.html#tooltips).

(ns plotje-book.customization
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Clojure2d -- palette and gradient discovery
   [clojure2d.color :as c2d]))

;; ## Dimensions

;; A wide, short plot.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:width 800 :height 250}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (>= (:width s) 800))))])

;; A tall, narrow plot.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:width 300 :height 500}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (>= (:width s) 300))))])

;; ## Titles and Labels

;; Override axis labels and add a title.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Iris Sepal Measurements"
                 :x-label "Length (cm)"
                 :y-label "Width (cm)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Iris Sepal Measurements"} (:texts s)))))])

;; Add a subtitle and caption for context.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Iris Measurements"
                 :subtitle "Sepal dimensions across three species"
                 :caption "Source: Fisher's Iris dataset (1936)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Iris Measurements"} (:texts s))
                                (some (fn [t] (.contains ^String t "Sepal dimensions")) (:texts s)))))])

;; Legend titles default to the column name. Override with
;; `:color-label`, `:size-label`, `:alpha-label`, or `:shape-label`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:color-label "Species (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Species (override)"} (:texts s)))))])

;; The size legend title comes from `:size-label`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:size :petal-length})
    (pj/options {:size-label "Petal length (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Petal length (override)"} (:texts s)))))])

;; And `:alpha-label` overrides the alpha legend title:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:alpha :petal-length})
    (pj/options {:alpha-label "Petal length (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Petal length (override)"} (:texts s)))))])

;; `:shape-label` does the same for the shape legend. Naming it also
;; splits a merged color-and-shape legend back into two, since asking
;; for a separate name is asking for a separate legend:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species :shape :species})
    (pj/options {:shape-label "Marker (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (+ (:points s) (:polygons s)))
                                (some #{"Marker (override)"} (:texts s)))))])

;; ### Color and fill
;;
;; Most marks expose `:color` as the encoding channel -- scatter
;; dots, lines, bar interiors, area fills, violins, lollipops -- all
;; styled with `:color` and named via `:color-label` in the legend.
;; The separate `:fill` channel is currently reserved for the heatmap
;; family: `lay-tile` (and the `:bin2d` output beneath
;; `lay-density-2d`) reads the encoded value as a continuous fill,
;; with its own legend title override `:fill-label`:

(-> {:x [1 2 3 1 2 3] :y [1 1 1 2 2 2] :z [10 20 30 40 50 60]}
    (pj/lay-tile :x :y {:fill :z})
    (pj/options {:fill-label "Score"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (some #{"Score"} (:texts s))
                                (pos? (:visible-tiles s)))))])

;; **Coming from ggplot2.** ggplot's `colour=` (stroke) and `fill=`
;; (interior) split is partial in Plotje today. On filled marks like
;; `lay-bar`, `lay-area`, and `lay-violin`, the `:color` aesthetic
;; paints the interior; there is no separate stroke channel, and
;; `:fill` is not accepted. A `lay-bar` styled with `{:color :species}`
;; produces one filled polygon per category:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               fills (disj (:colors s) "none")]
                           (and (= 3 (:polygons s))
                                ;; three distinct interior colors
                                (= 3 (count fills)))))])

;; ## Rotating tick labels

;; When a categorical x-axis has many categories, or long category
;; names, the tick labels run into each other and become hard to
;; read. Rotate them with `:x-tick-angle`, given in degrees. A value
;; of -45 is a common diagonal that keeps the text legible while
;; saving horizontal room.

(-> {:product (map #(str "Product " %) (range 12))
     :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
    (pj/lay-bar :product :revenue)
    (pj/options {:x-tick-angle -45}))

(kind/test-last [(fn [v] (and (= 12 (:polygons (pj/svg-summary v)))
                              (.contains ^String (pr-str (pj/plot v)) "rotate(-45")))])

;; Plotje reserves extra vertical space below the panel for the
;; angled labels, scaled by the angle. When that automatic estimate
;; reserves too much or too little, set `:x-tick-label-pad` (in
;; drawing units) to control the reserved height directly:

(-> {:product (map #(str "Product " %) (range 12))
     :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
    (pj/lay-bar :product :revenue)
    (pj/options {:x-tick-angle -45
                 :x-tick-label-pad 90}))

(kind/test-last [(fn [v] (= 12 (:polygons (pj/svg-summary v))))])

;; A label rotated this way extends down and to the left of its
;; tick. Very long names can run past the left edge of the plotting
;; area; see
;; [Known Limitations](./plotje_book.known_limitations.html#layout-and-visuals).

;; ## Grouping digits in large numbers

;; A count in the hundreds of thousands is hard to read as a run of
;; digits: a reader has to count places to tell 462389 from 46238.
;; `:thousands-separator` inserts a string between each group of three
;; digits, in numeric tick labels and in the text that
;; `pj/lay-text` and `pj/lay-label` take from a column.
;;
;; It is off by default. Numbers are left as they are unless you ask,
;; because grouping is wrong for a value that is an identifier rather
;; than a quantity -- a year axis would read 2,026.

(-> {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
     :tickets   [462389 181444 163294]}
    (pj/lay-bar :tickets :violation)
    (pj/lay-label :tickets :violation {:text :tickets :align-x :right})
    (pj/options {:thousands-separator ","}))

(kind/test-last
 [(fn [v]
    (let [texts (set (:texts (pj/svg-summary v)))]
      (and (contains? texts "462,389")
           (contains? texts "100,000"))))])

;; The separator is whatever string you pass, so conventions other than
;; the comma work too -- a space, or the point used across much of
;; Europe:

(-> {:violation ["Meter Expired" "Over Time Limit"]
     :tickets   [462389 181444]}
    (pj/lay-bar :tickets :violation)
    (pj/lay-label :tickets :violation {:text :tickets :align-x :right})
    (pj/options {:thousands-separator "."}))

(kind/test-last
 [(fn [v] (contains? (set (:texts (pj/svg-summary v))) "462.389"))])

;; Grouping widens the tick labels, and the space reserved for them
;; grows to match, so a grouped axis does not push its labels into the
;; panel. Here the same data drawn both ways gives a narrower panel once
;; the separators appear:

(let [panel-width (fn [opts]
                    (-> {:x [1 2 3] :y [1000000 2000000 3000000]}
                        (pj/lay-point :x :y)
                        (pj/options opts)
                        pj/plan
                        :panel-width))]
  {:ungrouped (panel-width {})
   :grouped (panel-width {:thousands-separator ","})})

(kind/test-last
 [(fn [m] (< (:grouped m) (:ungrouped m)))])

;; Only the digits to the left of the decimal point are grouped:

(-> {:x [1] :y [1] :amount [1234.56]}
    (pj/lay-label :x :y {:text :amount})
    (pj/options {:thousands-separator ","}))

(kind/test-last
 [(fn [v] (contains? (set (:texts (pj/svg-summary v))) "1,234.56"))])

;; Grouping applies to the numbers that measure something: tick labels on
;; a numeric axis, the text `pj/lay-text` and `pj/lay-label` take from a
;; column, and the values a size or alpha legend prints. Category names,
;; colour and shape legend labels, and facet strip labels are left alone,
;; because those name a group rather than measure it.
;;
;; A year falls on either side of that, depending on how it is used.
;; Plotted on a numeric axis it is a quantity, so it groups:

(-> (for [y (range 2020 2031)] {:year y :revenue (* 1000 (- y 2019))})
    (pj/lay-point :year :revenue)
    (pj/options {:thousands-separator ","})
    pj/plan
    :panels
    first
    :x-ticks
    :labels)

(kind/test-last
 [(fn [labels] (= "2,020" (first labels)))])

;; Used as categories, the same years name four groups, so they are left
;; alone -- even with a grouped axis beside them:

(-> (for [y (range 2020 2024)] {:year y :revenue (* 1000 (- y 2019))})
    (pj/lay-bar :year :revenue {:x-type :categorical})
    (pj/options {:thousands-separator ","})
    pj/plan
    :panels
    first
    :x-ticks
    :labels)

(kind/test-last
 [(fn [labels] (= ["2020" "2021" "2022" "2023"] (vec labels)))])

;; A size legend groups its values, so it reads the same way as the axis
;; beside it, while the colour legend's category names do not:

(->> (-> (for [i (range 8)] {:xx (double i) :yy (double i)
                             :volume (* 100000 (inc i)) :region (str "region " i)})
         (pj/lay-point :xx :yy {:size :volume :color :region})
         (pj/options {:thousands-separator ","})
         pj/svg-summary
         :texts)
     (filter #(re-find #"," %))
     distinct
     sort)

(kind/test-last
 [(fn [texts] (= ["100,000" "200,000" "300,000" "400,000" "500,000"
                  "600,000" "700,000" "800,000"]
                 (vec texts)))])

;; ## Writing the decimal point

;; Some cultures write the decimal point as a comma: 1234,5 rather than
;; 1234.5. `:decimal-separator` names the string to draw in that place,
;; in the same text `:thousands-separator` groups. It is off by default
;; too. The two usually go together -- where the point groups the digits,
;; the comma separates the fraction, giving 1.234,5.

(-> {:region ["North" "South" "East"]
     :profit [1234.5 1500.25 2680.75]}
    (pj/lay-bar :profit :region)
    (pj/lay-label :profit :region {:text :profit :align-x :right})
    (pj/options {:thousands-separator "." :decimal-separator ","}))

;; Each bar is labelled with its own value, so 1.234,5 shows both
;; separators in one number. The ticks below land on round hundreds and
;; have no decimal part to write, so they show only the grouping.

(kind/test-last
 [(fn [v]
    (let [texts (set (:texts (pj/svg-summary v)))]
      (and (contains? texts "1.234,5")
           (contains? texts "2.680,75")
           (some (fn [t] (re-matches #"\d\.\d00" t)) texts))))])

;; ## Scales

;; Use a log scale for data spanning orders of magnitude.

(def exponential-data
  {:x (range 1 50)
   :y (map #(* 2 (Math/pow 1.1 %)) (range 1 50))})

;; Linear scale -- hard to see the structure.

(-> exponential-data
    (pj/lay-point :x :y)
    (pj/options {:title "Linear Scale"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 49 (:points s)))))])

;; Log y-scale -- reveals the exponential trend.

(-> exponential-data
    (pj/lay-point :x :y)
    (pj/scale :y :log)
    (pj/options {:title "Log Y Scale"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 49 (:points s)))))])

;; Lock the y-axis to a specific range.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :domain [0 6]})
    (pj/options {:title "Fixed Y Domain [0, 6]"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; A domain narrower than the data turns the axis into a view window:
;; marks falling outside it are clipped to the panel, and the
;; underlying data is kept. This matches ggplot2's `coord_cartesian`,
;; which zooms the view, rather than scale limits, which drop rows.
;; Here the sepal-width domain is tightened to [3.0, 3.5], so points
;; above and below that band are clipped at the panel edge. All 150
;; observations are still rendered -- the marks sit behind a clip
;; region, one per panel.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :domain [3.0 3.5]})
    (pj/options {:title "Tight Y Domain [3.0, 3.5]"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:clips s)))))])

;; Pin exact tick locations with `:breaks` (ggplot2's
;; `scale_*_continuous(breaks=...)`).

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :breaks [2.0 3.0 4.0]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (every? (set (:texts s)) ["2" "3" "4"]))))])

;; Pair `:breaks` with `:labels` to render numeric positions with
;; custom tick text. The two vectors must match in count -- each
;; label is shown at its corresponding break. This is the path for
;; cases like a tile heatmap where the axis is numerically indexed
;; (1-7) but the natural labels are categorical (days of the week).

(-> (for [day (range 1 8) hour (range 0 24)]
      {:day day :hour hour :load (+ (* 0.3 (Math/sin (* 0.5 hour)))
                                    (* 0.2 (mod day 3)))})
    (pj/lay-tile :day :hour {:fill :load})
    (pj/scale :x {:type :linear
                  :breaks [1 2 3 4 5 6 7]
                  :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})
    (pj/options {:title "Weekly Load by Hour"}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))])

;; Order a categorical axis explicitly with `:type :categorical`
;; and a `:domain` vector. Without this, categories appear in their
;; order of first occurrence in the data.

(-> {:size ["medium" "small" "large"]
     :count [12 30 7]}
    (pj/lay-bar :size :count)
    (pj/scale :x {:type :categorical :domain ["large" "medium" "small"]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               labels (filter #{"large" "medium" "small"} (:texts s))]
                           (= ["large" "medium" "small"] (vec labels))))])

;; On a categorical axis, `:breaks` selects which categories get a
;; tick, and `:labels` relabels them -- the discrete counterpart to
;; numeric `:breaks` above. Each break is matched to a category by its
;; displayed label; a break that names no category is dropped with a
;; warning. Here only two of the four quarters are ticked, with custom
;; text:

(-> {:quarter ["Q1" "Q2" "Q3" "Q4"]
     :revenue [120 150 90 200]}
    (pj/lay-bar :quarter :revenue)
    (pj/scale :x {:breaks ["Q1" "Q4"] :labels ["First" "Fourth"]}))

(kind/test-last [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
                           (and (contains? texts "First")
                                (contains? texts "Fourth")
                                (not (contains? texts "Q2")))))])

;; Thin a crowded categorical axis with `:n-ticks`. A categorical
;; axis labels every category by default, so with many categories
;; the labels overlap. `:n-ticks` keeps roughly that many
;; evenly-spaced tick labels instead. (When both are given, explicit
;; `:breaks` win over `:n-ticks`. Rotating the labels with
;; `:x-tick-angle` is the other way to handle crowding -- see
;; Rotating tick labels above.)

(-> {:bin (map #(str "bin-" %) (range 40))
     :count (range 40)}
    (pj/lay-bar :bin :count)
    (pj/scale :x {:n-ticks 8}))

(kind/test-last [(fn [v] (let [labels (filter #(.startsWith ^String % "bin-")
                                              (:texts (pj/svg-summary v)))]
                           (= 8 (count labels))))])

;; ### Log scale on visual channels
;;
;; `pj/scale` works on continuous visual channels too -- `:size`,
;; `:alpha`, `:fill`, and `:color`. When the encoded column spans
;; many orders of magnitude, a log scale spaces the legend ticks
;; logarithmically and maps the visual property (radius, alpha,
;; gradient color) in log-space, so each tick step represents the
;; same multiplicative ratio. `:categorical` does not apply to a
;; continuous encoding -- visual channels accept `:linear` (the
;; default) and `:log` only.

;; Point sizes from a column whose values jump by factors of ten. On a
;; linear scale the step from 10 to 100 is a small part of the way to
;; 1000, so those two points sit close together at the bottom of the
;; size range while n=1000 stands apart -- a linear scale reads
;; absolute distance, which the largest value dominates:

(-> {:user [:a :b :c] :n [10 100 1000]}
    (pj/lay-point :user :n {:size :n :x-type :categorical}))

(kind/test-last
 [(fn [v]
    (let [[small mid large] (sort (:sizes (pj/svg-summary v)))]
      ;; The middle radius is nearer the smallest than the largest.
      (< (- mid small) (- large mid))))])

;; With `pj/scale :size :log`, each factor-of-ten step covers the same
;; part of the domain, so the n=10 and n=100 points are no longer
;; crowded together at the bottom -- the gap between them is now the
;; wider of the two:

(-> {:user [:a :b :c] :n [10 100 1000]}
    (pj/lay-point :user :n {:size :n :x-type :categorical})
    (pj/scale :size :log))

(kind/test-last
 [(fn [v]
    (let [[small mid large] (sort (:sizes (pj/svg-summary v)))]
      (and (= 3 (:points (pj/svg-summary v)))
           (> (- mid small) (- large mid)))))])

;; The size legend's tick values are the original numbers (10, 100,
;; 1000), and its dots are drawn from the same scale as the panel's,
;; so the dot beside 100 is the size an n=100 point is drawn at.

;; Tile heatmap with log-scaled fill:

(-> (for [r (range 5) c (range 5)]
      {:r r :c c :v (Math/pow 10.0 (/ (+ r c) 2.0))})
    (pj/lay-tile :r :c {:fill :v})
    (pj/scale :fill :log))

(kind/test-last [(fn [v] (>= (:visible-tiles (pj/svg-summary v)) 25))])

;; The continuous fill legend draws log-spaced tick labels along
;; the gradient bar so a tile's color reads as its log-space
;; position between the data minimum and maximum.
;;
;; To override the inferred type of a column (e.g. force a numeric
;; `:hour` column to render as categorical bands), see
;; [Inference Rules](./plotje_book.inference_rules.html#overriding-inferred-types-with-x-type-y-type).

;; ### Shape symbols
;;
;; `:shape` is a discrete channel, so its scale controls two things a
;; continuous channel has no use for: which order the categories are
;; assigned symbols in, and which symbols those are. Both matter when
;; a reader compares two plots -- the same category should keep the
;; same marker across them.
;;
;; `pj/shape-symbols` lists the available markers, in the order they
;; are assigned to categories:

pj/shape-symbols

(kind/test-last [(fn [syms] (= syms (distinct syms)))])

;; A plot with more categories than that repeats a symbol, so two
;; categories cannot be told apart; it warns when that happens.
;;
;; Left alone, the categories take those symbols in the order they
;; appear in the data:

(-> {:model ["a" "b" "c" "d"] :score [3 1 4 2] :tier ["gold" "silver" "bronze" "gold"]}
    (pj/lay-point :model :score {:shape :tier}))

(kind/test-last
 [(fn [v]
    (= (take 3 pj/shape-symbols)
       (mapv :shape (:entries (:shape-legend (pj/plan v))))))])

;; `:domain` sets the category order, which is also the legend order:

(-> {:model ["a" "b" "c" "d"] :score [3 1 4 2] :tier ["gold" "silver" "bronze" "gold"]}
    (pj/lay-point :model :score {:shape :tier})
    (pj/scale :shape {:domain ["gold" "silver" "bronze"]}))

(kind/test-last
 [(fn [v]
    (= (mapv vector ["gold" "silver" "bronze"] pj/shape-symbols)
       (mapv (juxt :label :shape)
             (:entries (:shape-legend (pj/plan v))))))])

;; `:values` picks the symbols themselves, paired with the categories
;; in that same order:

(-> {:model ["a" "b" "c" "d"] :score [3 1 4 2] :tier ["gold" "silver" "bronze" "gold"]}
    (pj/lay-point :model :score {:shape :tier})
    (pj/scale :shape {:domain ["gold" "silver" "bronze"]
                      :values [:diamond :cross :plus]}))

(kind/test-last
 [(fn [v]
    (= [["gold" :diamond] ["silver" :cross] ["bronze" :plus]]
       (mapv (juxt :label :shape)
             (:entries (:shape-legend (pj/plan v))))))])

;; ## Mark Styling

;; Pass `:alpha` and `:size` directly to layer functions.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species :alpha 0.5 :size 5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (contains? (:alphas s) 0.5)
                                (contains? (:sizes s) 5.0))))])

;; `:size` controls line thickness on line-based marks:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-line :x :y {:size 3}))

(kind/test-last [(fn [v] (= 1 (:lines (pj/svg-summary v))))])

;; `:stroke-dash` draws a line dashed or dotted, so a projected or
;; reference series reads apart from measured data. Pass a named preset
;; or a raw `[dash gap]` pattern in drawing units.
;;
;; `:dashed`:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-line :x :y {:stroke-dash :dashed}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:dashed-lines s))
                                (contains? (:dash-patterns s) "6.00 4.00"))))])

;; `:dotted` -- a shorter dash and gap:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-line :x :y {:stroke-dash :dotted}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:dashed-lines s))
                                (contains? (:dash-patterns s) "1.00 3.00"))))])

;; `:solid` is the default -- an unbroken line, so no dash pattern:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-line :x :y {:stroke-dash :solid}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:lines s))
                                (= 0 (:dashed-lines s)))))])

;; A raw `[dash gap]` vector sets the pattern directly, in drawing
;; units -- here a long dash and a short gap:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-line :x :y {:stroke-dash [12 4]}))

(kind/test-last [(fn [v] (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))])

;; Alpha works on bars and polygons too.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species {:alpha 0.4}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:polygons s))
                                (contains? (:alphas s) 0.4))))])

;; ## Text Placement
;;
;; Where a text mark goes -- anchoring it to its point, shifting it by a
;; distance on the page, placing it at a value rather than a column, or
;; on the panel rather than in the data -- is the subject of
;; [Placing Marks](./plotje_book.placing_marks.html#anchoring-a-text-mark). The rest of this
;; chapter covers how text looks once it is placed.

;; ## Bold and Italic Text
;;
;; A label placed on top of the data has to be read against it. Where
;; `:align-x` and `:align-y` move the text, two further options change
;; how it is drawn:
;;
;; - `:font-weight` -- `:normal` or `:bold` (default `:normal`)
;; - `:font-style` -- `:normal` or `:italic` (default `:normal`)
;;
;; The two are independent, so a label can be both bold and italic. Both
;; apply to `pj/lay-text` and `pj/lay-label`, and to both output formats:
;; the SVG backend writes them as font attributes and the PNG backend
;; draws with the matching Java font style.
;;
;; Bold picks one label out of several. Here the peak is emphasized and
;; the two ordinary points are left plain:

(-> {:x [1 2 3] :y [2 3 1]}
    (pj/lay-point :x :y {:size 5 :color "#888888"})
    (pj/lay-text :x :y {:text :tag :align-x :center :align-y :bottom
                        :data {:x [1 3] :y [2 1] :tag ["steady" "dip"]}})
    (pj/lay-text :x :y {:text :tag :align-x :center :align-y :bottom
                        :font-weight :bold
                        :data {:x [2] :y [3] :tag ["peak"]}}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 1 (:bold-texts s))
           (= 0 (:italic-texts s))
           (every? (set (:texts s)) ["steady" "dip" "peak"]))))])

;; Italic reads as an aside -- a remark about the data rather than a
;; value taken from it. On `pj/lay-label` it sits in the same background
;; box as any other label text:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species :alpha 0.5})
    (pj/lay-label {:text :note :font-style :italic
                   :data {:sepal-length [7.0] :sepal-width [4.2]
                          :note ["setosa sits apart"]}}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 1 (:italic-texts s))
           (= 0 (:bold-texts s)))))])

;; ## Text on a Background Box
;;
;; Text placed over dense data competes with the marks underneath. A
;; background box separates the two: `:box` draws the text on a white
;; panel with rounded corners and a thin border.
;;
;; `pj/lay-label` is the same layer type with the box switched on, so
;; every option in this section applies to both. These two produce the
;; same plot:

(-> {:x [1] :y [1]}
    (pj/lay-label :x :y {:text :tag :data {:x [1] :y [1] :tag ["a boxed label"]}}))

(-> {:x [1] :y [1]}
    (pj/lay-text :x :y {:text :tag :box true
                        :data {:x [1] :y [1] :tag ["a boxed label"]}}))

(kind/test-last
 [(fn [v]
    (and (= 1 (:label-boxes (pj/svg-summary v)))
         ;; "the same plot" is meant literally -- the two render to the
         ;; same bytes, which is checkable because a clip-path id is
         ;; derived from its region rather than from a running counter
         (= (str (pj/plot v))
            (str (pj/plot (-> {:x [1] :y [1]}
                              (pj/lay-label :x :y {:text :tag
                                                   :data {:x [1] :y [1]
                                                          :tag ["a boxed label"]}})))))))])

;; Pass a map to shape the box. `:corner-radius` is how round the corners
;; are, in drawing units -- three labels at decreasing radius, the last
;; square.
;;
;; A box sits at its data point, so it would cover the very point it
;; labels. `:nudge-x` shifts each label clear of its point, in data
;; units -- the same idiom a scatter plot needs when labelling its
;; marks:

(-> {:x [1 1 1] :y [3 2 1]}
    (pj/lay-point :x :y {:size 5 :color "#888888"})
    (pj/lay-label :x :y {:text :tag :box {:corner-radius 8} :nudge-x 0.05
                         :data {:x [1] :y [3] :tag ["corner-radius 8"]}})
    (pj/lay-label :x :y {:text :tag :nudge-x 0.05
                         :data {:x [1] :y [2] :tag ["the default, 3"]}})
    (pj/lay-label :x :y {:text :tag :box {:corner-radius 0} :nudge-x 0.05
                         :data {:x [1] :y [1] :tag ["corner-radius 0"]}}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 3 (:label-boxes s))
           (= 3 (:points s))
           (= [8.0 3.0 0.0]
              (->> (pj/plan v) :panels first :layers
                   (filter #(= :text (:mark %)))
                   (mapv #(-> % :style :box :corner-radius)))))))])

;; `{:box false}` on `pj/lay-label` leaves the text bare, the same as
;; calling `pj/lay-text`:

(-> {:x [1] :y [1]}
    (pj/lay-label :x :y {:text :tag :box false
                         :data {:x [1] :y [1] :tag ["bare text"]}}))

(kind/test-last
 [(fn [v]
    (and (zero? (:label-boxes (pj/svg-summary v)))
         (= (str (pj/plot v))
            (str (pj/plot (-> {:x [1] :y [1]}
                              (pj/lay-text :x :y {:text :tag
                                                  :data {:x [1] :y [1]
                                                         :tag ["bare text"]}})))))))])

;; ## Reference Line and Band Appearance
;;
;; Reference lines and bands are introduced in
;; [Core Concepts](./plotje_book.core_concepts.html#plot-options-and-reference-lines); on temporal
;; axes, intercepts can be `LocalDate` / `Instant` values -- see
;; [Timelines](./plotje_book.timelines.html#annotated-time-series). This section covers
;; the appearance defaults you can override.
;;
;; They take `:offset-x` and `:offset-y` like any other layer, so a
;; rule can sit a fixed distance from the value it marks -- a line
;; drawn just above a threshold rather than on it. `:in` is the one
;; layer option they do not take: their positions come from data
;; values.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:alpha 0.4})
    (pj/lay-rule-h {:y-intercept 3.0 :color "#cc3311"})
    (pj/lay-rule-h {:y-intercept 3.0 :color "#4477aa" :offset-y -25}))

(kind/test-last
 [(fn [fr]
    (= [nil -25]
       (mapv :offset-y (:annotations (first (:panels (pj/plan fr)))))))])
;;
;; Shaded bands draw at a default opacity of 0.15:

(:band-opacity (pj/config))

(kind/test-last [(fn [v] (= 0.15 v))])

;; Pass `{:alpha ...}` on a band layer to override:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-band-v {:x-min 5.5 :x-max 6.5})
    (pj/lay-band-h {:y-min 3.0 :y-max 3.5 :alpha 0.3}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; Note: intercept and band-edge positions must be written values
;; (numbers, or temporal values on a time axis) in this release. A
;; faceted plot with a different reference value per panel
;; (column-mapped intercept, ggplot2's
;; `geom_hline(aes(yintercept=...))`) is on the post-alpha roadmap.
;; Today, an annotation added once with the same intercept appears
;; on every panel of the faceted pose.
;;
;; Giving a line layer its own two-point dataset does not stand in for
;; it: a layer's own `:data` is not split by `pj/facet` either, so each
;; panel draws every row of it. To vary a reference value across
;; panels today, build them with `pj/arrange` -- each cell is its own
;; pose, and takes its own intercept.
;;
;; Reference lines accept `:stroke-dash` too, so a threshold or target
;; line can read as dashed or dotted rather than solid:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-rule-v {:x-intercept 6.0 :color "gray" :stroke-dash :dashed}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:dashed-lines s))
                                (contains? (:dash-patterns s) "6.00 4.00"))))])

;; ## Palettes
;;
;; Pass `:palette` to override the default color cycle. It accepts a
;; vector of hex strings, a map from category to hex, or a keyword
;; naming one of the built-in palettes (`:set1`, `:set2`, `:dark2`,
;; `:tableau-10`, `:category10`, `:pastel1`, `:accent`, `:paired`, and
;; many more).
;;
;; The full list of forms is in
;; [Palette Configuration](./plotje_book.configuration.html#palette-configuration),
;; the project-level / thread-local / plot-level precedence chain in
;; [The Precedence Chain](./plotje_book.configuration.html#the-precedence-chain),
;; and the key table in
;; [Configuration Keys](./plotje_book.configuration.html#configuration-keys).

;; Custom vector:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette ["#E74C3C" "#3498DB" "#2ECC71"]}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Named preset -- here `:dark2` for a high-contrast qualitative palette:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette :dark2}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Discovering Palettes and Gradients
;;
;; Plotje delegates color to the
;; [clojure2d](https://github.com/Clojure2D/clojure2d) library, which
;; bundles thousands of named palettes and gradients.  Use
;; `clojure2d.color/find-palette` and `clojure2d.color/find-gradient`
;; to search by regex pattern.

;; Find palettes whose name contains "budapest".

(c2d/find-palette #"budapest")

(kind/test-last [(fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))])

;; Find palettes whose name contains "set".

(c2d/find-palette #"^:set")

(kind/test-last [(fn [v] (and (sequential? v) (some #{:set1} v)))])

;; Find gradients related to "viridis".

(c2d/find-gradient #"viridis")

(kind/test-last [(fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))])

;; `c2d/palette` returns the colors for a given name.
;; Each color is a clojure2d `Vec4` (RGBA, 0-255 range).

(c2d/palette :grand-budapest-1)

(kind/test-last [(fn [v] (and (sequential? v) (pos? (count v))))])

;; ### Colorblind-friendly palettes
;;
;; For presentations and publications, consider palettes designed for
;; colorblind readers. Several good options are built in:
;;
;; - `:set2` -- muted qualitative, 8 colors
;; - `:dark2` -- dark qualitative, 8 colors
;; - `:khroma/okabeito` -- designed specifically for color vision deficiency
;; - `:tableau-10` -- Tableau default, high contrast

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette :khroma/okabeito}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Theme
;;
;; Customize background color, grid color, and font size.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "White Theme"
                 :theme {:bg "#FFFFFF" :grid "#EEEEEE" :font-size 10}}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; ## Legend Position
;;
;; Control where the legend appears: `:right` (default), `:bottom`,
;; `:top`, or `:none`.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:legend-position :bottom}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (< (:width s) 700))))])

;; Legend on top:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:legend-position :top}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; No legend at all -- useful when the color encoding is documented
;; in the title or caption rather than a separate legend. The panel
;; takes the full width since no legend strip is reserved:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:legend-position :none}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plan (pj/plan (-> (rdatasets/datasets-iris)
                            (pj/lay-point :sepal-length :sepal-width {:color :species})
                            (pj/options {:legend-position :none})))]
      (and (= 150 (:points s))
           (zero? (get-in plan [:layout :legend-w])))))])

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- the mapping and aesthetic vocabulary used throughout this chapter
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) -- where layer options, plot options, and configuration live
;; - [**Placing Marks**](./plotje_book.placing_marks.html) -- where a mark goes: anchoring, offsets, values for `:x` and `:y`, and `pj/frames`
;; - [**Interactivity**](./plotje_book.interactivity.html) -- tooltips and brush selection

;; ## What's Next
;;
;; - [**Placing Marks**](./plotje_book.placing_marks.html) -- where a mark goes, and in what units
;; - [**Faceting**](./plotje_book.faceting.html) -- split any chart into panels by one or two variables
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
