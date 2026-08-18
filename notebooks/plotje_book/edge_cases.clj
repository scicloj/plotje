;; # Edge Cases
;;
;; This chapter tests how Plotje handles unusual or boundary
;; inputs -- missing values, extreme numbers, degenerate datasets,
;; and uncommon configurations.

(ns plotje-book.edge-cases
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Fastmath -- random number generation
   [fastmath.random :as rng]
   ;; Java-time -- idiomatic date/time construction
   [java-time.api :as jt]
   ;; dtype-next datetime -- vectorized temporal arithmetic
   [tech.v3.datatype.datetime :as dt-dt]
   ;; dtype-next core -- const-reader for temporal sequences
   [tech.v3.datatype :as dtype]))

;; ## Data Shape
;; ### Missing Data

;; Rows with `nil` values are dropped before rendering.

(def with-missing
  {:x [1 2 nil 4 5 nil 7]
   :y [3 nil 5 6 nil 8 9]})

(-> with-missing
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:points s)))))])

;; ### Infinite Values
;;
;; Rows with `Double/POSITIVE_INFINITY` or `Double/NEGATIVE_INFINITY`
;; are filtered automatically with a warning -- similar to log-scale filtering.

(def with-infinity
  {:x [1 2 3 4 5]
   :y [10.0 Double/POSITIVE_INFINITY 30.0 Double/NEGATIVE_INFINITY 50.0]})

(-> with-infinity
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:points s))
                                (not (clojure.string/includes? (str v) "NaN")))))])
;; ### Single Point

;; A lone data point should render without errors.

(-> {:x [3] :y [7]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1 (:points s)))))])

;; ### Two Points with Regression

;; Regression requires at least 3 points. With only 2,
;; the line is omitted and the points still render.

(-> {:x [1 10] :y [5 50]}
    (pj/lay-point :x :y)
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:points s))
                                (zero? (:lines s)))))])

;; ### Three Points with Regression

;; With 3 points, the regression line appears.

(-> {:x [1 5 10] :y [5 25 50]}
    (pj/lay-point :x :y)
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:points s))
                                (= 1 (:lines s)))))])

;; ### Constant X

;; All x values are the same -- the plot should still render.

(-> {:x [5 5 5 5 5] :y [1 2 3 4 5]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:points s)))))])

;; ### Constant Y

;; All y values are the same.

(-> {:x [1 2 3 4 5] :y [3 3 3 3 3]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:points s)))))])

;; ## Numeric Range
;; ### Negative Values

;; Data spanning positive and negative ranges.

(-> {:x [-5 -3 0 3 5] :y [-2 4 0 -4 2]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:points s)))))])

;; ### Very Large Values

(-> {:x [1e6 2e6 3e6] :y [1e9 2e9 3e9]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:points s)))))])

;; ### Very Small Values

(-> {:x [0.001 0.002 0.003] :y [0.0001 0.0002 0.0003]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:points s)))))])

;; ### Large Dataset

;; 1000 random points, colored by group.

(def large-data
  (let [r (rng/rng :jdk 42)]
    {:x (repeatedly 1000 #(rng/drandom r))
     :y (repeatedly 1000 #(rng/drandom r))
     :group (repeatedly 1000 #([:a :b :c] (rng/irandom r 3)))}))

(-> large-data
    (pj/lay-point :x :y {:color :group}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 1000 (:points s)))))])

;; ### Many Categories

;; A bar chart with 12 categories.

(-> (let [r (rng/rng :jdk 99)]
      {:category (map #(keyword (str "cat-" %)) (range 12))
       :value (repeatedly 12 #(+ 10 (rng/irandom r 90)))})
    (pj/lay-bar :category :value))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 12 (:polygons s)))))])

;; ### Computed Columns

;; Derive a new column and plot it.

(-> (rdatasets/datasets-iris)
    (tc/map-columns :sepal-ratio [:sepal-length :sepal-width] /)
    (pj/lay-point :sepal-length :sepal-ratio {:color :species})
    (pj/options {:title "Sepal Length/Width Ratio"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; ### Filtered Subset

;; Plot only one species.

(-> (rdatasets/datasets-iris)
    (tc/select-rows #(= "setosa" (% :species)))
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-smooth {:stat :linear-model})
    (pj/options {:title "Setosa Only"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 50 (:points s))
                                (= 1 (:lines s)))))])

;; ## Position and Layout

;; ### Stacked bar -- single group

;; Stack with only one color value -- no actual stacking needed.

(-> {:category ["a" "b" "c"]
     :count [10 20 15]}
    (pj/lay-bar :category :count {:position :stack}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ### Dodge -- missing category in one group

;; Group "g1" has data for "a" and "b", but "g2" only has "a".
;; Dodge should still align correctly.

(-> {:x ["a" "b" "a"]
     :g ["g1" "g1" "g2"]}
    (pj/lay-bar :x {:color :g}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ### Fill -- zero count category

;; One group has zero count for a category.
;; Fill should handle the zero without error.

(-> {:x ["a" "a" "b" "b" "b"]
     :g ["g1" "g2" "g1" "g1" "g1"]}
    (pj/lay-bar :x {:position :fill :color :g}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ### Nudge on scatter

;; Nudge-x on continuous data -- shifts points without error.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:nudge-x 0.1 :nudge-y -0.05}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ### Confidence ribbon -- small n

;; Linear regression with `:confidence-band true` on exactly 3 points
;; (the minimum for a linear model).

(-> {:x [1 2 3] :y [2 4 5]}
    (pj/lay-point :x :y)
    (pj/lay-smooth {:stat :linear-model :confidence-band true}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:points s))
                                (= 1 (:lines s)))))])

;; ### Stacked area -- single series

;; Stack with a single color group -- should render as a plain area.

(-> (let [r (rng/rng :jdk 55)]
      {:x (range 10)
       :y (repeatedly 10 #(rng/irandom r 20))})
    (pj/lay-area :x :y {:position :stack}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ## Scale and Coordinate

;; ### Log scale with clean powers of 10

(-> {:x [1 10 100 1000 10000]
     :y [2 20 200 2000 20000]}
    (pj/lay-point :x :y)
    (pj/scale :x :log)
    (pj/scale :y :log))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 5 (:points s))
                                (= 1 (:panels s)))))])

;; ### Log scale spanning decimals to large values

(-> {:x [0.001 0.01 0.1 1 10 100]
     :y [1 2 3 4 5 6]}
    (pj/lay-point :x :y)
    (pj/scale :x :log))

(kind/test-last [(fn [v] (= 6 (:points (pj/svg-summary v))))])

;; ### Log scale with non-positive values
;;
;; Non-positive values are filtered on log-scaled axes, since `log`
;; requires positive inputs. Here x includes 0 and -1:

(-> {:x [0 -1 1 10 100] :y [1 2 3 4 5]}
    (pj/lay-point :x :y)
    (pj/scale :x :log))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; ### Continuous color -- constant value
;;
;; All points have the same numeric color value. The gradient
;; should still render and not divide by zero.

(-> {:x [1 2 3] :y [4 5 6] :c [5 5 5]}
    (pj/lay-point :x :y {:color :c}))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; ### Diverging color with midpoint at zero

(-> {:x (range 20)
     :y (map #(- % 10) (range 20))
     :val (map #(- % 10.0) (range 20))}
    (pj/lay-point :x :y {:color :val})
    (pj/options {:color-range :diverging :color-midpoint 0}))

(kind/test-last [(fn [v] (= 20 (:points (pj/svg-summary v))))])

;; ### Dates with very narrow range (two days apart)

(-> {:date [(jt/local-date 2025 1 1)
            (jt/local-date 2025 1 2)]
     :val [10 20]}
    (pj/lay-point :date :val))

(kind/test-last [(fn [v] (= 2 (:points (pj/svg-summary v))))])

;; ### Sub-day precision (LocalDateTime spanning hours)
;;
;; `LocalDateTime` values preserve sub-day precision. Tick labels
;; show `HH:MM` format when the range is less than a day.

(-> {:time (dt-dt/plus-temporal-amount
            (dtype/const-reader (jt/local-date-time 2025 3 15 8 0) 24)
            (map #(* (long %) 15) (range 24)) :minutes)
     :value (map #(+ 18.0 (* 4.0 (Math/sin (* % 0.3)))) (range 24))}
    (pj/lay-line :time :value)
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 24 (:points s))
                                (= 1 (:lines s)))))])

;; ### Instant with sub-day precision
;;
;; `java.time.Instant` values are converted to `LocalDateTime` (UTC) for
;; calendar-aware tick formatting. Tick labels show hours when the range
;; spans less than a day.

(-> {:time (dt-dt/plus-temporal-amount
            (dtype/const-reader (jt/instant 1750003200000) 12)
            (range 12) :hours)
     :temp (map #(+ 20.0 (* 5.0 (Math/sin (* % 0.5)))) (range 12))}
    (pj/lay-line :time :temp)
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 12 (:points s))
                                (= 1 (:lines s))
                                (some #(re-find #":\d\d" %) (:texts s)))))])

;; ### Multi-year date range
;;
;; With a date range spanning several years, tick labels show year values.

(-> {:date (dt-dt/plus-temporal-amount
            (dtype/const-reader (jt/local-date 2020 1 1) 20)
            (map #(* (long %) 120) (range 20)) :days)
     :value (map #(+ 100 (* 50 (Math/sin (* % 0.4)))) (range 20))}
    (pj/lay-line :date :value)
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 20 (:points s))
                                (= 1 (:lines s)))))])

;; ### Polar with many categories

(-> {:cat (map #(str "cat-" %) (range 12))
     :val (repeatedly 12 #(rand-int 100))}
    (pj/lay-bar :cat :val)
    (pj/coord :polar))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

;; ### Log scale + coord flip combined
;;
;; When log scale and coord flip are both applied, the panel should
;; have log ticks on the (now vertical) axis and the domain should
;; reflect the flipped layout.

(-> {:x [1 10 100 1000] :y [2 4 8 16]}
    (pj/lay-point :x :y)
    (pj/scale :x :log)
    (pj/coord :flip))

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan v)
          panel (first (:panels plan))]
      (and (= 4 (:points (pj/svg-summary v)))
           (= :flip (:coord panel))
           ;; After flip: y-scale is original x-scale (log)
           (= {:type :log} (:y-scale panel))
           ;; After flip: x-scale is original y-scale (linear)
           (= {:type :linear} (:x-scale panel)))))])

;; ### Scale with explicit domain

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/scale :y {:domain [0 6]}))

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan v)
          panel (first (:panels plan))]
      (= [0 6] (:y-domain panel))))])

;; ### A size scale anchored at zero, on data holding a zero
;;
;; `:from-zero` makes the area proportional to the value, so a value of
;; zero gives a radius of zero and that mark is not visible. This is
;; the correct result for a proportional scale, and ggplot2's
;; `scale_size_area` behaves the same way, but a row can disappear
;; without anything being wrong.

(-> {:x [1 2 3] :y [1 2 3] :n [0 5 10]}
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:from-zero true}))

(kind/test-last
 [(fn [v]
    ;; Three marks are drawn; two of them have a size to be seen at.
    (= 2 (count (:sizes (pj/svg-summary v)))))])

;; ### A size scale anchored at zero, on data holding a negative
;;
;; Anchored at zero it is the distance from zero that decides the ink,
;; so a value of -5 is drawn the size a value of 5 is drawn. The mark
;; stays on the panel and keeps its place on the axis; only its size
;; stops distinguishing the two directions. Where the sign matters,
;; map it to another aesthetic -- `:color` splits the two apart.

(-> {:x [1 2 3] :y [1 2 3] :n [-5 5 10]}
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:from-zero true}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      ;; Three marks are drawn, at two distinct sizes: -5 and 5 are the
      ;; same distance from zero, so they are drawn alike.
      (and (= 3 (:points s))
           (= [5.656854249492381 8.0] (vec (:sizes s))))))])

;; ### A size domain with no spread
;;
;; When every value is equal there is nothing to compare, so every mark
;; is drawn halfway across the domain rather than at one of its ends.
;; Halfway is read through the scale's own spread method, so a constant
;; column and a nearly-constant one are drawn alike.

(-> {:x [1 2 3] :y [1 2 3] :n [5 5 5]}
    (pj/lay-point :x :y {:size :n}))

(kind/test-last
 [(fn [v] (= [6.242640687119286] (vec (:sizes (pj/svg-summary v)))))])

;; ### A size range written backwards
;;
;; `[8 2]` runs from large to small. This reverses the encoding rather
;; than raising an error: the largest value is drawn smallest.

(-> {:x [1 2 3] :y [1 2 3] :n [1 5 10]}
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:range [8 2]}))

(kind/test-last
 [(fn [v]
    (let [ms (->> v pj/plan :size-legend :entries (mapv :magnitude))]
      ;; The legend runs from small values to large, and its swatches
      ;; shrink as it goes.
      (and (= 2.0 (last ms)) (apply > ms))))])

;; ### Fixed aspect ratio with extreme domain ratio

(-> {:x (range 100) :y (range 0 10 0.1)}
    (pj/lay-point :x :y)
    (pj/coord :fixed))

(kind/test-last [(fn [v] (= 100 (:points (pj/svg-summary v))))])

;; ### Full grid -- cross plot
;;
;; `pj/cross` produces a full NxN grid of panels. Column names
;; appear as axis labels on each cell.

(-> (rdatasets/datasets-iris)
    (pj/pose (pj/cross [:sepal-length :sepal-width :petal-length]
                       [:sepal-length :sepal-width :petal-length])
             {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               texts (:texts s)
                               col-label? #(re-find #"sepal|petal" %)]
                           (and (= 9 (:panels s))
                                (seq (filter col-label? texts)))))])
;; ## Error Messages
;; Plotje produces clear error messages for common mistakes.

;; ### Non-existent column

(try
  (-> {:x [1 2 3] :y [4 5 6]}
      (pj/lay-point :nonexistent :y)
      pj/plot)
  (catch Exception e
    (ex-message e)))

(kind/test-last [(fn [m] (string? m))])

;; ### Non-existent color column

(try
  (-> {:x [1 2 3] :y [4 5 6]}
      (pj/lay-point :x :y {:color :bogus})
      pj/plot)
  (catch Exception e
    (ex-message e)))

(kind/test-last [(fn [m] (string? m))])

;; ### Layer with own :data, missing the pose's position columns
;;
;; When a layer carries its own `:data` and the pose's position
;; columns are absent from that data, the error names the source
;; (inherited from the pose's mapping) and offers two paths: rename
;; the column for an overlay, or set the axis on the layer call for
;; a separate sub-pose. This is the diagnostic for the common
;; ggplot2-trained reflex of attaching a second layer with a fresh
;; dataset whose columns don't align with the panel's axes.

(try
  (-> (tc/dataset {:fitted [1 2 3] :residual [1 2 3]})
      (pj/lay-point :fitted :residual)
      (pj/lay-text {:data (tc/dataset {:x    [1 2 3]
                                       :y    [1 2 3]
                                       :text [:a :b :c]})})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m]
    (and (string? m)
         (re-find #"inherited from the pose's mapping" m)
         (re-find #"absent from this layer's :data" m)))])

;; ### Unsupported polar mark

(try
  (-> {:x [1 2 3] :y [4 5 6]}
      (pj/lay-line :x :y)
      (pj/coord :polar)
      pj/plot)
  (catch Exception e
    (ex-message e)))

(kind/test-last [(fn [m] (re-find #"not supported with polar" m))])

;; ### Mismatched mark and stat

(try
  (-> {:x [1 2 3]}
      (pj/pose :x)
      (pj/lay {:mark :boxplot :stat :bin})
      pj/plot)
  (catch Exception e
    (ex-message e)))

(kind/test-last [(fn [m] (re-find #"must contain :boxes" m))])

;; ### x-only layer type with y column
;;
;; Layer types that use only the x column (histogram, bar, density,
;; rug) reject a y column with a clear message.

;; Histogram uses only the x column. Passing a y column is now an error:

(try
  (-> {:x [1 2 3] :y [4 5 6]}
      (pj/lay-histogram :x :y))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last [(fn [m] (re-find #"lay-histogram uses only the x column" m))])
