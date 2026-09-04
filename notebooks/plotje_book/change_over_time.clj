;; # Change Over Time
;;
;; Line charts and their variants -- showing change over a sequence.

(ns plotje-book.change-over-time
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Line

;; Connected line through data points.

(def wave {:x (range 30)
           :y (map #(Math/sin (* % 0.3)) (range 30))})

(-> wave
    (pj/lay-line :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1 (:lines s)))))])

;; ## Grouped Lines

;; Color separates multiple series. Real datasets often start in
;; **wide form** -- each series in its own column. Plotje plots
;; **long form** -- one row per observation, with the series label
;; in a column. Use `tc/pivot->longer` to reshape, then map the
;; label column to `:color`. See
;; [Datasets](./plotje_book.datasets.html#from-wide-to-long) for more on the
;; wide-to-long reshape.

(def waves-wide
  (tc/dataset
   {:x   (range 30)
    :sin (map #(Math/sin (* % 0.3)) (range 30))
    :cos (map #(Math/cos (* % 0.3)) (range 30))}))

(def waves
  (tc/pivot->longer waves-wide [:sin :cos]
                    {:target-columns :function
                     :value-column-name :y}))

(-> waves
    (pj/lay-line :x :y {:color :function}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 2 (:lines s)))))])

;; ## Thick Line

;; Constant stroke width via `:size`.

(-> wave
    (pj/lay-line :x :y {:size 4}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1 (:lines s)))))])

;; ## Line with Points

;; Overlay points on a grouped line plot.

(def growth
  {:day [1 2 3 4 5 1 2 3 4 5]
   :value [10 15 13 18 22 8 12 11 16 19]
   :group [:a :a :a :a :a :b :b :b :b :b]})

(-> growth
    (pj/pose :day :value {:color :group})
    pj/lay-line
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 10 (:points s))
                                (= 2 (:lines s)))))])

;; ## Step

;; Horizontal-then-vertical connected points.

(-> {:x [1 2 3 4 5]
     :y [2 4 1 5 3]}
    (pj/lay-step :x :y)
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 5 (:points s))
                                (= 1 (:lines s)))))])

;; ## Step by Group

;; Grouped step lines.

(-> growth
    (pj/pose :day :value {:color :group})
    pj/lay-step
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 10 (:points s))
                                (= 2 (:lines s)))))])

;; ## Stacked Step

;; The groups pile up in the order the legend lists them, the first on
;; top, with horizontal-then-vertical segments.

(-> {:x (concat (range 5) (range 5) (range 5))
     :y (concat [1 2 3 4 5] [2 2 2 2 2] [3 1 2 1 2])
     :group (concat (repeat 5 "A") (repeat 5 "B") (repeat 5 "C"))}
    (pj/lay-step :x :y {:position :stack :color :group}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          groups (-> v pj/plan :panels first :layers first :groups)]
      (and (= 1 (:panels s))
           (= 3 (:lines s))
           ;; Stacked, not three lines drawn at their own values: C
           ;; rests on the baseline and each group above it starts
           ;; where the one below ends.
           (= ["A" "B" "C"] (mapv :label groups))
           (every? zero? (:y0s (last groups)))
           (= (vec (:ys (second groups))) (vec (:y0s (first groups))))
           (= (vec (:ys (last groups))) (vec (:y0s (second groups)))))))])

;; ## Area

;; Filled area under a line.

(-> {:x (range 30)
     :y (map #(Math/sin (* % 0.3)) (range 30))}
    (pj/lay-area :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1 (:polygons s)))))])

;; ## [Stacked Area](https://en.wikipedia.org/wiki/Area_chart)

;; The bands pile up in the order the legend lists them, the first on
;; top.

(-> {:x (concat (range 10) (range 10) (range 10))
     :y (concat [1 2 3 4 5 4 3 2 1 0]
                [2 2 2 3 3 3 2 2 2 2]
                [1 1 1 1 2 2 2 1 1 1])
     :group (concat (repeat 10 "A") (repeat 10 "B") (repeat 10 "C"))}
    (pj/lay-area :x :y {:position :stack :color :group}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:polygons s)))))])

;; ## Dates on the x-axis

;; Real time-series usually have an actual date column, not an
;; integer step. Plotje detects temporal columns
;; (`java.util.Date` via `#inst`, `java.time.LocalDate`,
;; `LocalDateTime`, `Instant`) and picks calendar-aware tick
;; labels automatically.

(def temp-pose
  (-> {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"
              #inst "2024-04-01" #inst "2024-05-01" #inst "2024-06-01"]
       :temperature [3 5 9 14 19 23]}
      (pj/lay-line :date :temperature)
      pj/lay-point))

temp-pose

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          panel (first (:panels (pj/plan temp-pose)))
          tick-labels (:labels (:x-ticks panel))]
      (and (= 6 (:points s))
           (= 1 (:lines s))
           ;; Tick labels are calendar-aware (e.g., "Feb-03", not raw
           ;; epoch-millisecond numbers).
           (some #(re-find #"[A-Z][a-z]{2}" %) tick-labels))))])

;; ## Multiple Series Over Time

;; Pass `{:color :group}` to get one line per category. Rows are
;; drawn in their given order, so pre-sort by date if your data
;; is not already sorted.

(def months
  [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"
   #inst "2024-04-01" #inst "2024-05-01" #inst "2024-06-01"])

(-> {:date        (concat months months)
     :temperature [3  5  9 14 19 23
                   15 17 19 22 25 28]
     :city        (concat (repeat 6 "Zurich")
                          (repeat 6 "Athens"))}
    (pj/lay-line :date :temperature {:color :city})
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 12 (:points s))
                                (= 2 (:lines s)))))])

;; ## Area Over Dates

;; Filled area also works on a date axis -- useful for cumulative
;; metrics where the volume below the curve carries meaning.

(-> {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"
            #inst "2024-04-01" #inst "2024-05-01" #inst "2024-06-01"]
     :sales [10 25 30 22 35 40]}
    (pj/lay-area :date :sales))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1 (:polygons s)))))])

;; See [Inference Rules](./plotje_book.inference_rules.html#temporal-columns)
;; for details on how dates are detected and formatted.

;; ## Two Series on One Date Axis
;;
;; Two series measured over different stretches of time read against
;; each other only if their axes agree. Stacked without saying so, each
;; panel spans its own dates across the same width, so a reading above
;; another stands for a different month.

(def zurich
  {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"
          #inst "2024-04-01" #inst "2024-05-01" #inst "2024-06-01"]
   :temperature [3 5 9 14 19 23]})

(def athens
  {:date [#inst "2024-05-01" #inst "2024-06-01" #inst "2024-07-01"
          #inst "2024-08-01" #inst "2024-09-01" #inst "2024-10-01"]
   :temperature [25 28 31 32 27 22]})

(def cities
  [(-> zurich (pj/lay-line :date :temperature) pj/lay-point)
   (-> athens (pj/lay-line :date :temperature) pj/lay-point)])

(pj/arrange cities {:cols 1})

(kind/test-last
 [(fn [v]
    (let [panels (mapv #(-> % :plan :panels first) (:sub-plots (pj/plan v)))]
      (and (= 2 (:panels (pj/svg-summary v)))
           (= 12 (:points (pj/svg-summary v)))
           ;; Each panel spans its own dates and is ticked over them.
           (apply not= (mapv :x-domain panels))
           (apply not= (mapv #(:labels (:x-ticks %)) panels)))))])

;; `:share-scales #{:x}` pools the date column across the cells. The
;; pooled range is the union of both, so each series occupies the part
;; of the axis it covers, and both panels carry the same tick labels.
;; One above the other, that is something to look at rather than to
;; check: a month is at the same place in both panels.

(pj/arrange cities {:cols 1 :share-scales #{:x}})

(kind/test-last
 [(fn [v]
    (let [panels (mapv #(-> % :plan :panels first) (:sub-plots (pj/plan v)))
          widths (->> (tree-seq vector? seq (pj/plot v))
                      (filter #(and (vector? %) (= :rect (first %))
                                    (= "rgb(232,232,232)" (:fill (second %)))))
                      (mapv #(double (:width (second %)))))]
      (and (= 2 (:panels (pj/svg-summary v)))
           (= 12 (:points (pj/svg-summary v)))
           (apply = (mapv :x-domain panels))
           (apply = (mapv #(:labels (:x-ticks %)) panels))
           ;; And the panels are the same width, which is what puts a
           ;; month at the same place in both.
           (apply = widths))))])

;; An axis holds a date as a number of milliseconds, which is what lets
;; the two ranges be pooled at all.
;;
;; The two panels line up here because their y axes label at the same
;; width. Where they do not -- one series in single digits and one in
;; millions -- each panel reserves the room its own labels need, and the
;; shared axis comes out spanning different widths. Writing the
;; composite out and setting `:align-panels` reserves the same room on
;; every cell; the
;; [Composition](./plotje_book.composition.html#shared-scales) chapter
;; covers that and `:share-scales` on any column.

;; ## A Distribution of the Dates
;;
;; A time series says what the readings were, and less about when they
;; were taken. `pj/marginal` puts a distribution of the pose's `:x`
;; column in a thin panel above the plot, sharing its axis -- and on a
;; date column that distribution is how the observations are spread
;; through time.

(def sightings
  {:date [#inst "2021-03-14" #inst "2021-07-02" #inst "2021-11-28"
          #inst "2022-02-09" #inst "2022-05-30" #inst "2022-06-11"
          #inst "2022-06-25" #inst "2022-07-08" #inst "2022-09-17"
          #inst "2023-01-22" #inst "2023-08-05" #inst "2024-02-19"]
   :count [2 5 3 8 6 11 9 14 12 7 4 2]})

(-> sightings
    (pj/lay-point :date :count)
    (pj/marginal :top :histogram))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          panels (mapv #(-> % :plan :panels first) (:sub-plots (pj/plan v)))]
      (and (= 2 (:panels s))
           (= 12 (:points s))
           ;; Five bars over twelve sightings.
           (= 5 (:polygons s))
           ;; The strip and the plot are pinned to one range, so a bar
           ;; stands over the readings it counts.
           (apply = (mapv :x-domain panels))
           ;; The strip's own date ticks are dropped; the axis below
           ;; describes both panels.
           (= [] (:values (:x-ticks (first panels)))))))])

;; ## Smoothed Time Series

;; A LOESS smoother overlaid on a noisy time series makes the
;; underlying trend easier to see. `pj/lay-smooth` works on any
;; numerical y axis, including dates on x.

(-> {:date  [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"
             #inst "2024-04-01" #inst "2024-05-01" #inst "2024-06-01"
             #inst "2024-07-01" #inst "2024-08-01" #inst "2024-09-01"
             #inst "2024-10-01" #inst "2024-11-01" #inst "2024-12-01"]
     :sales [10 14 12 18 22 19 25 28 24 30 27 33]}
    (pj/pose :date :sales)
    pj/lay-line
    pj/lay-smooth)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                ;; one raw line plus the LOESS smooth
                                ;; overlay -- two lines total.
                                (= 2 (:lines s)))))])

;; ## Zero-Line Baseline

;; Time series with positive and negative values often benefit from a
;; horizontal reference at zero, drawn with `pj/lay-rule-h`. The rule
;; takes its position from the options map -- not from a data column
;; -- so it's an annotation, not a data layer.

(-> {:t (range 12)
     :delta [-3 -1 -2 0 2 4 -1 3 5 -2 1 4]}
    (pj/lay-line :t :delta)
    pj/lay-point
    (pj/lay-rule-h {:y-intercept 0 :color "#888"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 12 (:points s))
                                (= 2 (:lines s)))))])

;; ## What's Next
;;
;; - [**Timelines**](./plotje_book.timelines.html) -- events, intervals, and schedules on a time axis (Gantt charts, Marey diagrams, annotated time series)
;; - [**Relationships**](./plotje_book.relationships.html) -- heatmaps, contours, and 2D density
;; - [**Polar Coordinates**](./plotje_book.polar.html) -- radial charts for cyclical data
;; - [**Gallery**](./plotje_book.gallery.html) -- more chart variations with side-by-side code
