;; # API Reference
;;
;; Complete reference for every public function in
;; `scicloj.plotje.api`.
;;
;; Each entry shows the docstring, a live example, and a test.
;; For galleries of mark variations, see the Visualization Goals
;; chapters (Distributions, Ranking, Change Over Time, Timelines,
;; Relationships).

^{:kindly/hide-code true
  :kindly/options {:kinds-that-hide-code #{:kind/doc}}}
(ns plotje-book.api-reference
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Fastmath -- random number generation
   [fastmath.random :as rng]))

;; ## Sample Data

(def tiny {:x [1 2 3 4 5]
           :y [2 4 1 5 3]
           :group [:a :a :b :b :b]})

(def sales {:product [:widget :gadget :gizmo :doohickey]
            :revenue [120 340 210 95]})

(def measurements {:treatment ["A" "B" "C" "D"]
                   :mean [10.0 15.0 12.0 18.0]
                   :ci-lo [8.0 12.0 9.5 15.5]
                   :ci-hi [12.0 18.0 14.5 20.5]})

;; ## Construction
;;
;; `pj/pose` is not a literal composition of the single-step
;; transitions. Its 1-arity is `(-> x pj/->pose pj/infer-mapping)` --
;; the same lift-and-default-map the other shortcuts (`pj/draft`,
;; `pj/plan`, `pj/membrane`, `pj/plot`, `pj/save`) apply on the way
;; in -- but its typed arities add positional column-arg parsing,
;; multi-pair composite construction, and pose extend-or-promote on
;; top of `pj/->pose`. The examples below walk each shape.

(kind/doc #'pj/pose)

;; Create a leaf pose with data and columns:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; Map form -- include aesthetics on the pose so every layer inherits them:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 3 (:lines s)))))])

;; A bare collection of scalars (numbers, strings, or keywords) is
;; data too -- it becomes a single column named `:value`. With no
;; chart type, a single numeric column infers a histogram:

(pj/pose [1 4 1 5 6 2 3 3 3 2 4 5 1 2 3 4])

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

(kind/doc #'pj/with-data)

;; Attach or replace the top-level dataset on a pose.
;; Useful for building a dataless template and applying it to many
;; datasets:

(def scatter-template
  (-> (pj/pose nil {:x :x :y :y :color :group})
      pj/lay-point))

(-> scatter-template
    (pj/with-data tiny))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; Multi-pair pose -- a vector of `[x y]` pairs creates a composite
;; with one pose per pair:

(-> (rdatasets/datasets-iris)
    (pj/pose [[:sepal-length :sepal-width]
              [:petal-length :petal-width]])
    (pj/lay-point {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; Map form -- explicit keys on a pose:

(-> (rdatasets/datasets-iris)
    (pj/pose {:x :sepal-length :y :sepal-width})
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

(kind/doc #'pj/cross)

(pj/cross [:a :b] [1 2 3])

(kind/test-last [(fn [v] (= [[:a 1] [:a 2] [:a 3] [:b 1] [:b 2] [:b 3]] v))])

;; Combine `pj/cross` with `pj/pose` to build a SPLOM:

(-> (rdatasets/datasets-iris)
    (pj/pose (pj/cross [:sepal-length :petal-length]
                       [:sepal-width :petal-width])
             {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:panels s))
                                (= 600 (:points s)))))])

;; ## Layer Functions

(kind/doc #'pj/lay)

;; The generic layer adder. `pj/lay-point`, `pj/lay-bar`, etc. are
;; convenience wrappers around `pj/lay` with a registered layer-type
;; key. Use `pj/lay` directly when you have a custom layer type (from
;; `pj/layer-type-lookup` on a registered key, or a raw layer-type
;; map from an extension):

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    (pj/lay :point))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; The above delegates to `:point` -- equivalent to
;; `pj/lay-point`. The intended use of `pj/lay` is with a layer
;; type that isn't a built-in convenience: a registered custom
;; layer type from an extension, or a raw layer-type map. See
;; the [Extension Example](./plotje_book.waterfall_extension.html)
;; chapter for the full pattern -- registering a `:waterfall`
;; layer type and calling `(pj/lay pose (layer-type/lookup :waterfall))`
;; or wrapping it in a `pj/lay-waterfall` convenience function.

(kind/doc #'pj/lay-point)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

(kind/doc #'pj/lay-line)

(def wave {:x (range 30)
           :y (map #(Math/sin (* % 0.3)) (range 30))})

(-> wave
    (pj/lay-line :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 1 (:lines s))))])

(kind/doc #'pj/lay-histogram)

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:polygons s))))])

;; A vector of columns creates one panel per column:

(pj/lay-histogram (rdatasets/datasets-iris) [:sepal-length :sepal-width])

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (pos? (:polygons s)))))])

(kind/doc #'pj/lay-bar)

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

;; Stacked bars: pass `{:position :stack}` to `pj/lay-bar`.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:position :stack :color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:polygons s))))])

;; 100% stacked bars: pass `{:position :fill}` to `pj/lay-bar`.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:position :fill :color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:polygons s))))])

;; Pass a y column and `pj/lay-bar` uses it as the bar height instead of
;; counting (value bars):

(-> sales
    (pj/lay-bar :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 4 (:polygons s))))])

;; The stat is inferred from whether a y column is present. Override it to
;; count even when a y column is supplied:

(-> sales
    (pj/lay-bar :product :revenue {:stat :count}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:polygons s))))])

;; A bar chart needs a categorical axis. To put the categories on a
;; numeric column, declare it categorical with `{:x-type :categorical}`
;; (or `{:y-type :categorical}`):

(-> {:hour [9 10 11] :sales [3 5 4]}
    (pj/lay-bar :hour :sales {:x-type :categorical}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

;; The categorical axis can be either x or y. Put the category on y and the
;; value on x for horizontal bars -- no `pj/coord` needed (the orientation
;; follows the categorical axis, the same way `pj/lay-boxplot` does):

(-> sales
    (pj/lay-bar :revenue :product))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 4 (:polygons s))))])

;; Grouped horizontal bars dodge as well. Stacked or filled horizontal bars
;; are not supported directly yet -- put the category on x and add
;; `(pj/coord :flip)` for those.

;; When neither axis is categorical, each bar sits at its numeric x position.
;; The width is `0.9` of the smallest gap between adjacent x values, or set it
;; with `{:bar-width n}` (in data units):

(-> {:x [1 2 3 4 5] :y [10 20 15 30 25]}
    (pj/lay-bar :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 5 (:polygons s))))])

;; A temporal x gives a time-series bar chart, with calendar tick labels:

(-> {:month [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"]
     :revenue [120 180 150]}
    (pj/lay-bar :month :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

(kind/doc #'pj/lay-smooth)

;; Linear regression: pass `{:stat :linear-model}` to `pj/lay-smooth`.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:lines s)))))])

(-> (let [r (rng/rng :jdk 42)
          xs (vec (range 50))]
      {:x xs
       :y (mapv #(+ (Math/sin (* % 0.2))
                    (* 0.3 (- (rng/drandom r) 0.5)))
                xs)})
    (pj/lay-point :x :y)
    (pj/lay-smooth {:bandwidth 0.2}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 50 (:points s))
                                (= 1 (:lines s)))))])

(kind/doc #'pj/lay-density)

(-> (rdatasets/datasets-iris)
    (pj/lay-density :sepal-length))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 1 (:polygons s))))])

(kind/doc #'pj/lay-area)

(-> wave
    (pj/lay-area :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 1 (:polygons s))))])

;; Stacked areas: pass `{:position :stack}` to `pj/lay-area`.

(-> {:x (concat (range 10) (range 10) (range 10))
     :y (concat [1 2 3 4 5 4 3 2 1 0]
                [2 2 2 3 3 3 2 2 2 2]
                [1 1 1 1 2 2 2 1 1 1])
     :group (concat (repeat 10 "A") (repeat 10 "B") (repeat 10 "C"))}
    (pj/lay-area :x :y {:position :stack :color :group}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

(kind/doc #'pj/lay-text)

(-> {:x [1 2 3 4] :y [4 7 5 8] :name ["A" "B" "C" "D"]}
    (pj/lay-text :x :y {:text :name}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (every? (set (:texts s)) ["A" "B" "C" "D"])))])

(kind/doc #'pj/lay-label)

(-> {:x [1 2 3 4] :y [4 7 5 8] :name ["A" "B" "C" "D"]}
    (pj/lay-point :x :y {:size 5})
    (pj/lay-label {:text :name}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (every? (set (:texts s)) ["A" "B" "C" "D"]))))])

(kind/doc #'pj/lay-boxplot)

(-> (rdatasets/datasets-iris)
    (pj/lay-boxplot :species :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:polygons s))
                                (pos? (:lines s)))))])

(kind/doc #'pj/lay-violin)

(-> (rdatasets/reshape2-tips)
    (pj/lay-violin :day :total-bill))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 4 (:polygons s))))])

(kind/doc #'pj/lay-errorbar)

(-> measurements
    (pj/lay-point :treatment :mean)
    (pj/lay-errorbar {:y-min :ci-lo :y-max :ci-hi}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 12 (:lines s)))))])

(kind/doc #'pj/lay-lollipop)

(-> sales
    (pj/lay-lollipop :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 4 (:lines s)))))])

(kind/doc #'pj/lay-tile)

(-> (rdatasets/datasets-iris)
    (pj/lay-tile :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:visible-tiles s))))])

(kind/doc #'pj/lay-density-2d)

(-> (rdatasets/datasets-iris)
    (pj/lay-density-2d :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:visible-tiles s))))])

(kind/doc #'pj/lay-contour)

(-> (rdatasets/datasets-iris)
    (pj/lay-contour :sepal-length :sepal-width))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:lines s))))])

(kind/doc #'pj/lay-ridgeline)

(-> (rdatasets/datasets-iris)
    (pj/lay-ridgeline :species :sepal-length))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:polygons s))))])

(kind/doc #'pj/lay-rug)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rug {:side :both}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 300 (:lines s))))])

(kind/doc #'pj/lay-step)

(-> tiny
    (pj/lay-step :x :y)
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 5 (:points s))
                                (= 1 (:lines s)))))])

(kind/doc #'pj/lay-summary)

(-> (rdatasets/datasets-iris)
    (pj/lay-summary :species :sepal-length))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:points s))
                                (= 3 (:lines s)))))])

(kind/doc #'pj/lay-interval-h)

(-> {:start [#inst "2024-01-01" #inst "2024-03-01" #inst "2024-05-01"]
     :end   [#inst "2024-04-01" #inst "2024-06-01" #inst "2024-08-01"]
     :task  ["Design" "Build" "Test"]}
    (pj/lay-interval-h :start :task {:x-end :end}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

;; ## Reference Lines and Bands

;; Reference lines and shaded bands are regular layers. Position comes
;; from the options map (`:y-intercept` for `lay-rule-h`, `:x-intercept`
;; for `lay-rule-v`; `:y-min`/`:y-max` for `lay-band-h`,
;; `:x-min`/`:x-max` for `lay-band-v`); `:color` overrides the default
;; annotation color, and bands additionally honor `:alpha` to override
;; the `:band-opacity` configuration default. Without x/y columns they attach at the
;; root (every panel); with x/y columns they attach to one matching
;; leaf.
;;
;; Rule intercepts also accept temporal values (`LocalDate`,
;; `LocalDateTime`, `Instant`, `java.util.Date`) so date-axis
;; annotations need no manual conversion -- see the second
;; `lay-rule-v` example below.
;;
;; **Note on `:y-min`/`:y-max`.** The same option keys carry two
;; meanings depending on the layer kind. On `lay-band-h`/`-v` they
;; are written numeric bounds (the band sits at fixed coordinates,
;; independent of the data). On `lay-errorbar` (above) they are
;; column references -- one row per error bar, with `:y-min` and
;; `:y-max` naming columns that supply the lower and upper bounds.
;; ggplot2 keeps these separate via `aes()` (column) versus a value
;; written outside it; Plotje overloads the keyword and dispatches by
;; mark. Writing the mapping in full -- `{:column :lo}` on the
;; errorbar, `{:value 12}` on the band -- says which reading you mean
;; without changing which one the mark accepts.

(kind/doc #'pj/lay-rule-v)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-v {:x-intercept 6.0}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (pos? (:lines s)))))])

;; A temporal rule on a date axis -- the same pose pattern, with
;; `:x-intercept` taking a `LocalDate`.

(-> {:date  [#inst "2024-01-01" #inst "2024-04-01" #inst "2024-08-01"]
     :value [3 5 9]}
    (pj/lay-line :date :value)
    (pj/lay-rule-v {:x-intercept (java.time.LocalDate/parse "2024-06-01")
                    :color "#c0392b"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 2 (:lines s)))))])

(kind/doc #'pj/lay-rule-h)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-h {:y-intercept 3.0}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (pos? (:lines s)))))])

(kind/doc #'pj/lay-band-v)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-band-v {:x-min 5.5 :x-max 6.5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

(kind/doc #'pj/lay-band-h)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-band-h {:y-min 2.5 :y-max 3.5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; ## Transforms

(kind/doc #'pj/coord)

;; Flip axes:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species) (pj/coord :flip))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 3 (:polygons s))))])

;; Polar coordinates:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species) (pj/coord :polar))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (pos? (:polygons s))))])

(kind/doc #'pj/scale)

;; Log scale:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width) (pj/scale :x :log))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; Fixed domain:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width) (pj/scale :x {:domain [3 9]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; Log scale on an appearance aesthetic (`:size`, `:alpha`, `:fill`, or
;; `:color`):

(-> {:user [:a :b :c] :n [10 100 1000]}
    (pj/lay-point :user :n {:size :n :x-type :categorical})
    (pj/scale :size :log))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; What a size spans, and how the values spread across it -- `:range`
;; in the quantity the mark draws (a radius here), `:by` the method,
;; and `:from-zero` to make the ink proportional to the value:

(-> {:user [:a :b :c] :n [10 100 1000]}
    (pj/lay-point :user :n {:size :n :x-type :categorical})
    (pj/scale :size {:range [3 16] :by :area :from-zero true}))

(kind/test-last
 [(fn [v] (= 16.0 (->> v pj/plan :size-legend :entries
                       (map :magnitude) (apply max))))])

;; Shape symbols on a discrete aesthetic -- `:domain` orders the
;; categories, `:values` picks the markers:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:shape :species})
    (pj/scale :shape {:domain ["virginica" "versicolor" "setosa"]
                      :values [:cross :plus :diamond]}))

(kind/test-last
 [(fn [v]
    (= [["virginica" :cross] ["versicolor" :plus] ["setosa" :diamond]]
       (mapv (juxt :label :shape)
             (:entries (:shape-legend (pj/plan v))))))])

(kind/doc #'pj/shape-symbols)

pj/shape-symbols

(kind/test-last [(fn [syms] (and (seq syms) (every? keyword? syms)))])

;; Custom tick labels on a numeric axis -- pair `:breaks` with
;; `:tick-labels`:

(-> (for [d (range 1 8)] {:day d :v (mod d 3)})
    (pj/lay-point :day :v)
    (pj/scale :x {:type :linear
                  :breaks [1 2 3 4 5 6 7]
                  :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (every? texts ["Mon" "Sun"])))])

;; ## Faceting

(kind/doc #'pj/facet)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s)))))])

(kind/doc #'pj/facet-grid)

(-> (rdatasets/reshape2-tips)
    (pj/lay-point :total-bill :tip {:color :sex})
    (pj/facet-grid :smoker :sex))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:panels s))
                                (= 244 (:points s)))))])

;; ## Composition

(kind/doc #'pj/arrange)

(pj/arrange [(-> (rdatasets/datasets-iris)
                 (pj/lay-point :sepal-length :sepal-width {:color :species})
                 (pj/options {:width 250 :height 200}))
             (-> (rdatasets/datasets-iris)
                 (pj/lay-point :petal-length :petal-width {:color :species})
                 (pj/options {:width 250 :height 200}))]
            {:cols 2})

(kind/test-last [(fn [v] (pj/pose? v))])

;; ## Rendering

(kind/doc #'pj/plot)

;; See the Customization notebook for options (title, theme,
;; tooltip, brush, legend position, palette).

(-> tiny
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 5 (:points s))))])

;; `pj/plot` (and the other terminal steps `pj/save`, `pj/draft`,
;; `pj/plan`, `pj/membrane`) also accept raw data directly, giving it
;; a default mapping first -- no explicit `pj/pose` needed:

(pj/plot {:height [150 160 170 175]
          :weight [50 60 72 78]})

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:points s)))))])

(kind/doc #'pj/options)

;; Set render options on a pose:

(-> tiny
    (pj/lay-point :x :y)
    (pj/options {:width 400 :height 200 :title "Small Plot"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (< (:width s) 500)
                                (some #{"Small Plot"} (:texts s)))))])

;; ## Predicates

(kind/doc #'pj/pose?)

;; Check whether a value is a pose (leaf or composite):

(pj/pose? (-> tiny (pj/pose :x :y) pj/lay-point))

(kind/test-last [true?])

(kind/doc #'pj/plan?)

;; Check whether a value is a plan (from `pj/plan`):

(pj/plan? (pj/plan (pj/lay-point tiny :x :y)))

(kind/test-last [true?])

(kind/doc #'pj/leaf-plan?)

;; Check whether a plan is a leaf (single-panel resolved geometry).
;; A non-composite plan from a leaf pose is a leaf plan:

(pj/leaf-plan? (pj/plan (pj/lay-point tiny :x :y)))

(kind/test-last [true?])

(kind/doc #'pj/composite-plan?)

;; Check whether a plan is a composite (a tree of sub-plots, returned
;; by `pj/plan` on a composite pose like one from `pj/arrange`):

(pj/composite-plan?
 (pj/plan (pj/arrange [(pj/lay-point tiny :x :y)
                       (pj/lay-point tiny :x :y)])))

(kind/test-last [true?])

(kind/doc #'pj/draft?)

;; Check whether a value is a draft (from `pj/draft`). True for both
;; `LeafDraft` records and `CompositeDraft` records:

(pj/draft? (pj/draft (pj/lay-point tiny :x :y)))

(kind/test-last [true?])

(kind/doc #'pj/leaf-draft?)

;; Check whether a draft is a leaf (a `LeafDraft` record carrying
;; `:layers` and `:opts`, returned by `pj/draft` on a leaf pose):

(pj/leaf-draft? (pj/draft (pj/lay-point tiny :x :y)))

(kind/test-last [true?])

(kind/doc #'pj/composite-draft?)

;; Check whether a draft is a composite (a tree of sub-drafts,
;; returned by `pj/draft` on a composite pose):

(pj/composite-draft?
 (pj/draft (pj/arrange [(pj/lay-point tiny :x :y)
                        (pj/lay-point tiny :x :y)])))

(kind/test-last [true?])

(kind/doc #'pj/plan-layer?)

;; Check whether a value is a resolved plan layer:

(pj/plan-layer? (first (:layers (first (:panels (pj/plan (pj/lay-point tiny :x :y)))))))

(kind/test-last [true?])

(kind/doc #'pj/layer-type?)

;; Check whether a value is a registered layer-type map:

(pj/layer-type? (pj/layer-type-lookup :point))

(kind/test-last [true?])

(kind/doc #'pj/membrane?)

;; Check whether a value is a `PlotjeMembrane` -- the value returned
;; by `pj/plan->membrane` and `pj/membrane`:

(pj/membrane? (pj/membrane (pj/lay-point tiny :x :y)))

(kind/test-last [true?])

;; ## Inspection

(kind/doc #'pj/draft)

;; Flatten a pose into a draft -- a `LeafDraft` record holding
;; `:layers` (one map per applicable layer, with all scope merged)
;; and `:opts` (pose-level options). Useful for inspecting exactly
;; what the plan stage will consume:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    pj/draft
    kind/pprint)

(kind/test-last [(fn [d] (and (pj/leaf-draft? d)
                              (= 1 (count (:layers d)))
                              (= :point (:mark (first (:layers d))))))])

(kind/doc #'pj/plan)

;; Returns the intermediate plan data structure:

(def plan1 (-> tiny
               (pj/lay-point :x :y)
               pj/plan))

plan1

(kind/test-last [(fn [m] (and (= 600 (:width m))
                              (= "x" (:x-label m))))])

(kind/doc #'pj/frames)

;; The whole result for this single-panel plot: the canvas, and one
;; entry per panel with its position in the grid, its domains and scale
;; specs, whether it can be mapped back to data, and its two frames.

(-> plan1 pj/frames kind/pprint)

(kind/test-last
 [(fn [m] (and (= [0.0 0.0 600.0 400.0] (:canvas m))
               (= 1 (count (:panels m)))
               (true? (-> m :panels first :invertible?))
               (= 4 (count (-> m :panels first :frames :drawing-area)))))])

;; The canvas is reported once rather than on every panel: it belongs to
;; the plot. Each panel's rectangles are given in canvas coordinates, so
;; they nest inside it and can be compared with each other directly.
;; That holds for a composite too, where the panels come from separate
;; sub-plans -- here a 700-wide image of two cells, whose panel boxes
;; start at different x and both end inside the canvas:

(let [f (pj/frames (pj/arrange [(pj/lay-point tiny :x :y)
                                (pj/lay-line tiny :x :y)]
                               {:width 700 :height 300}))
      [_ _ cw ch] (:canvas f)
      boxes (mapv #(-> % :frames :panel-box) (:panels f))
      inside? (fn [[x y w h]] (and (>= x 0) (>= y 0)
                                   (<= (+ x w) cw) (<= (+ y h) ch)))]
  {:canvas (:canvas f)
   :panel-boxes boxes
   :every-box-inside-the-canvas (every? inside? boxes)
   :panel-rectangle-keys (mapv #(vec (keys (:frames %))) (:panels f))})

(kind/test-last
 [(fn [m] (and (= [0.0 0.0 700.0 300.0] (:canvas m))
               (= 2 (count (:panel-boxes m)))
               ;; the two cells sit side by side, not on top of each other
               (apply not= (map first (:panel-boxes m)))
               (true? (:every-box-inside-the-canvas m))
               ;; and no panel carries a canvas of its own to disagree with
               (every? #(= [:panel-box :drawing-area] %)
                       (:panel-rectangle-keys m))))])

;; The result contains no functions, so it can be printed, compared and
;; read back from `pr-str`. The two mappings below take a panel entry
;; as their first argument.

(kind/doc #'pj/to-drawing)

(pj/to-drawing (-> plan1 pj/frames :panels first) 2 5)

(kind/test-last [(fn [v] (= 2 (count v)))])

(kind/doc #'pj/to-data)

;; Many positions at once go in and come back as a dataset:

(pj/to-drawing (-> plan1 pj/frames :panels first)
               {:x [2 3] :y [5 6]})

(kind/test-last [(fn [ds] (and (= [:x :y] (vec (tc/column-names ds)))
                               (= 2 (tc/row-count ds))))])

;; A position survives the round trip:

(let [panel (-> plan1 pj/frames :panels first)]
  (->> (pj/to-drawing panel 2 5)
       (apply pj/to-data panel)
       (mapv #(Math/round (double %)))))

(kind/test-last [(fn [v] (= [2 5] v))])

(kind/doc #'pj/svg-summary)

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}) pj/svg-summary)

(kind/test-last [(fn [m] (and (= 1 (:panels m))
                              (= 150 (:points m))))])

(kind/doc #'pj/valid-plan?)

(pj/valid-plan? plan1)

(kind/test-last [true?])

(kind/doc #'pj/explain-plan)

(pj/explain-plan plan1)

(kind/test-last [nil?])

;; ## Pipeline
;;
;; The pipeline is a composition of single-step transitions. The
;; user-facing functions (`pj/draft`, `pj/plan`, `pj/membrane`,
;; `pj/plot`, `pj/save`) run the chain up through their stage: each
;; lifts raw input with `pj/->pose`, applies `pj/infer-mapping` (a
;; default mapping when the input is a bare dataset, a no-op on a
;; built pose), then runs the single-step transitions. So each of
;; them accepts raw data as well as a pose. The
;; [Architecture](./plotje_book.architecture.html#pipeline-shortcuts-pjpose-pjdraft-pjplan-pjmembrane-pjplot-pjsave) chapter shows the
;; full composition.
;;
;; Each step is independently callable, so you can stop the
;; pipeline at any point to inspect the intermediate value.

(kind/doc #'pj/membrane)

;; Resolve a pose into a `PlotjeMembrane` -- a format-agnostic
;; Membrane UI component (a record implementing `IOrigin`, `IBounds`,
;; `IChildren`). Useful for exploring rendering targets beyond the
;; SVG and Java2D backends Plotje wires in today, and for composing
;; Plotje plots into larger Membrane interfaces. The
;; [Membranes](./plotje_book.membranes.html#anatomy) chapter walks the
;; record's anatomy and the protocols.

(let [m (pj/membrane (pj/lay-point tiny :x :y))]
  {:membrane?    (pj/membrane? m)
   :width        (membrane.ui/width m)
   :height       (membrane.ui/height m)
   :record-keys  (sort (filter keyword? (keys m)))})

(kind/test-last [(fn [info] (and (:membrane? info)
                                 (= 600 (:width info))
                                 (= 400 (:height info))
                                 (= [:drawables :height :width]
                                    (:record-keys info))))])

(kind/doc #'pj/->pose)

;; Lift raw input to a pose. Raw data (datasets, vectors of row
;; maps, column maps) becomes an empty leaf pose with `:data` set;
;; an existing pose flows through unchanged (idempotent). The first
;; step of the pipeline.

(pj/pose? (pj/->pose tiny))

(kind/test-last [true?])

(kind/doc #'pj/infer-mapping)

;; The default-mapping step the shortcuts apply right after
;; `pj/->pose`. On a fresh leaf (data, no mapping) it maps the first
;; 1-3 columns to `:x`, `:y`, and `:color`:

(-> {:height [150 160 170] :weight [50 60 72]}
    pj/->pose
    pj/infer-mapping
    :mapping)

(kind/test-last [(fn [m] (= {:x :height :y :weight} m))])

;; It is a no-op on a pose that already carries a mapping, so it is
;; safe anywhere in a pipeline:

(let [built (pj/lay-point tiny :x :y)]
  (= (:mapping built)
     (:mapping (pj/infer-mapping built))))

(kind/test-last [true?])

(kind/doc #'pj/pose->draft)

;; Single-step transition: pose to draft. Dispatches on shape --
;; leaf poses produce `LeafDraft` records, composite poses produce
;; `CompositeDraft` records.

(pj/leaf-draft?
 (pj/pose->draft (pj/lay-point tiny :x :y)))

(kind/test-last [true?])

(kind/doc #'pj/plan->membrane)

(def m1 (pj/plan->membrane plan1))

(pj/membrane? m1)

(kind/test-last [true?])

(kind/doc #'pj/valid-membrane?)

(pj/valid-membrane? m1)

(kind/test-last [true?])

(kind/doc #'pj/explain-membrane)

(pj/explain-membrane m1)

(kind/test-last [nil?])

(kind/doc #'pj/membrane->plot)

(first (pj/membrane->plot m1 :svg {}))

(kind/test-last [(fn [v] (= :svg v))])

(kind/doc #'pj/plan->plot)

(first (pj/plan->plot plan1 :svg {}))

(kind/test-last [(fn [v] (= :svg v))])

;; The `draft->*` family lets the same pipeline start from a draft
;; instead of a fully-resolved plan. Useful when you have a draft in
;; hand (e.g. from inspection) and want to skip re-running the
;; layer-flattening step.

(kind/doc #'pj/draft->plan)

(def draft1 (pj/draft (pj/lay-point tiny :x :y)))

(pj/plan? (pj/draft->plan draft1))

(kind/test-last [true?])

(kind/doc #'pj/draft->membrane)

(pj/membrane? (pj/draft->membrane draft1))

(kind/test-last [true?])

(kind/doc #'pj/draft->plot)

(first (pj/draft->plot draft1 :svg {}))

(kind/test-last [(fn [v] (= :svg v))])

;; ## Configuration

(kind/doc #'pj/config)

(pj/config)

(kind/test-last [(fn [m] (map? m))])

(kind/doc #'pj/set-config!)

(kind/doc #'pj/with-config)

(pj/with-config {:color-values :pastel1}
  (:color-values (pj/config)))

(kind/test-last [(fn [p] (= :pastel1 p))])

;; ### Documentation Metadata

;; Three maps document the option keys at each scope level.

(kind/doc #'pj/config-key-docs)

(count pj/config-key-docs)

(kind/test-last [(fn [n] (= 44 n))])

(kind/doc #'pj/plot-option-docs)

(count pj/plot-option-docs)

(kind/test-last [(fn [n] (= 15 n))])

(kind/doc #'pj/layer-option-docs)

(count pj/layer-option-docs)

(kind/test-last [(fn [n] (= 54 n))])

;; ## Layer Type Registry

(kind/doc #'pj/layer-type-lookup)

(pj/layer-type-lookup :smooth)

(kind/test-last [(fn [m] (and (= :line (:mark m))
                              (= :loess (:stat m))))])

(kind/doc #'pj/registered-layer-types)

(count (pj/registered-layer-types))

(kind/test-last [(fn [n] (= 25 n))])

(first (pj/registered-layer-types))

(kind/test-last [(fn [[k m]] (and (keyword? k)
                                  (some? (:mark m))
                                  (some? (:stat m))))])

;; ## Documentation Helpers
;;
;; Query the self-documenting dispatch tables for any extensible concept.

(kind/doc #'pj/stat-doc)

(pj/stat-doc :linear-model)

(kind/test-last [(fn [s] (string? s))])

(kind/doc #'pj/mark-doc)

(pj/mark-doc :point)

(kind/test-last [(fn [s] (string? s))])

(kind/doc #'pj/position-doc)

(pj/position-doc :dodge)

(kind/test-last [(fn [s] (string? s))])

(kind/doc #'pj/scale-doc)

(pj/scale-doc :linear)

(kind/test-last [(fn [s] (string? s))])

(kind/doc #'pj/coord-doc)

(pj/coord-doc :cartesian)

(kind/test-last [(fn [s] (string? s))])

(kind/doc #'pj/membrane-mark-doc)

(pj/membrane-mark-doc :point)

(kind/test-last [(fn [s] (string? s))])

;; ## Export

(kind/doc #'pj/save)

;; Save a plot to an SVG file:

(let [path (str (java.io.File/createTempFile "plotje-example" ".svg"))]
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/save path {:title "Iris Export"}))
  (.contains (slurp path) "<svg"))

(kind/test-last [true?])

;; Save a plot to a PNG file. Extension inference picks the raster
;; backend; the returned bytes start with the PNG magic header:

(let [path (str (java.io.File/createTempFile "plotje-example" ".png"))]
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/save path))
  (with-open [in (java.io.FileInputStream. path)]
    (let [bs (byte-array 8)]
      (.read in bs)
      (mapv #(bit-and ^int % 0xFF) (vec bs)))))

(kind/test-last [(fn [bs] (= [137 80 78 71 13 10 26 10] bs))])

;; Pass `{:format :png}` explicitly when the path's extension does
;; not match the desired format, or when it is built dynamically:

(let [path (str (java.io.File/createTempFile "plotje-example" ".out"))]
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/save path {:format :png}))
  (with-open [in (java.io.FileInputStream. path)]
    (let [bs (byte-array 4)]
      (.read in bs)
      (mapv #(bit-and ^int % 0xFF) (vec bs)))))

(kind/test-last [(fn [bs] (= [137 80 78 71] bs))])
