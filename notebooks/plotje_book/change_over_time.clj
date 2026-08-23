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
