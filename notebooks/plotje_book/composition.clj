;; # Composition
;;
;; Plotje's poses let you combine whole plots into a
;; single rendered image. A **composite pose** holds other poses
;; and a layout; each sub-pose renders independently and the
;; composite tiles them together.
;;
;; This chapter walks through composition patterns from simple
;; side-by-side arrangements to shared-scale marginal plots, using
;; `pj/arrange`, `pj/marginal` and explicit composite-pose maps --
;; compactly for simple cases, with a bit of literal map construction
;; for nested layouts.

(ns plotje-book.composition
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## Side-by-Side via `pj/arrange`
;;
;; The simplest composite: two independent poses, placed next to
;; each other. `pj/arrange` takes a vector of poses and returns a
;; composite. Each sub-pose has its own data, mapping, layers, and
;; options. Coming from R, this is the same shape as patchwork's
;; `p1 | p2` operator or cowplot's `plot_grid(p1, p2)`.

(pj/arrange
 [(-> (rdatasets/datasets-iris) (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))])

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; Pass `{:cols 1}` for a stacked arrangement (one column means each
;; pose goes on its own row):

(pj/arrange
 [(-> (rdatasets/datasets-iris) (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))]
 {:cols 1})

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A vector of vectors gives one row per inner vector. `nil` in place
;; of a pose is a cell left empty: it keeps its place in the layout and
;; draws nothing, so the other cells stay where they are.

(pj/arrange
 [[(-> (rdatasets/datasets-iris) (pj/lay-point :sepal-length :sepal-width {:color :species}))
   nil]
  [(-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :sepal-width {:color :species}))
   (-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))]])

(kind/test-last
 [(fn [v] (let [layout (-> v pj/plan :chrome :layout)]
            (and (= 3 (:panels (pj/svg-summary v)))
                 ;; four slots, the empty one included
                 (= #{[0 0] [0 1] [1 0] [1 1]} (set (keys layout))))))])

;; `pj/arrange` divides space equally among its sub-poses. For
;; unequal splits (e.g., give the first panel twice the space of the
;; second), construct the composite as an explicit map; the next
;; section shows how.

;; ## Explicit Composite Poses
;;
;; Under `pj/arrange` there is a plain-map composite pose. You can
;; construct one directly when you need finer control -- unequal
;; weights, shared scales, or (in future work) non-plot leaves like
;; text panels and key performance indicators.
;;
;; An explicit `:layout` accepts `:direction` (`:horizontal` or
;; `:vertical`) and `:weights` (one weight per sub-pose). Here the
;; first panel gets twice the space of the second:

(def weighted
  (pj/pose
   {:layout {:direction :horizontal :weights [2 1]}
    :poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :petal-length :y :petal-width}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

;; `pj/pose` accepts a literal composite map and tags it for notebook
;; auto-render -- the same pose value the threaded API produces:

weighted

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; And printed, showing the composite's structure -- `:layout` with
;; direction and weights at the top, then each sub-pose with its
;; own `:mapping` and `:layers`, and the outer `:data` dataset:

(kind/pprint weighted)

(kind/test-last [(fn [pose] (and (= [2 1] (get-in pose [:layout :weights]))
                                 (= 2 (count (:poses pose)))))])

;; The outer `:data` is inherited by both sub-poses. Each sub-pose
;; has its own `:mapping` and `:layers`, and need not repeat the
;; dataset. Subsequent examples in this chapter follow the same
;; shape and show only the rendered plot.

;; ## Shared Scales
;;
;; By default, sibling poses in a composite compute their own
;; domains. That is fine when their columns differ, but for the same
;; column shown twice (e.g., a marginal above a scatter, or a mosaic
;; of scatters all measuring the same variable) you want the axes
;; aligned. `:share-scales` names the axes the siblings share, and the
;; siblings drawing the same column on such an axis are given one
;; range:

(def shared-x
  (pj/pose
   {:share-scales #{:x}
    :layout {:direction :horizontal :weights [1 1]}
    :poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :sepal-length :y :petal-length}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

shared-x

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 300 (:points s)))))])

;; A date column pools the same way -- an axis holds a date as a number
;; of milliseconds, so the union across cells is defined, and the cells
;; are ticked over it rather than each over its own dates. See
;; [Change Over Time](./plotje_book.change_over_time.html#two-series-on-one-date-axis).
;;
;; Both panels share the sepal-length x-domain even though their y
;; columns differ. Column bucketing is automatic: only siblings whose
;; effective x-column matches share a scale. Panels with different
;; x-columns would each get their own domain.

;; ### What a Shared Axis Covers
;;
;; A shared axis covers what the cells sharing it draw. What a cell
;; draws is not always what its column holds: a stack of bars reaches
;; the total of its segments, an error bar reaches its ends, and a
;; smoothed line stays inside the points it was fitted to. Plotje
;; measures each cell after its statistics have run, pools the
;; measurements of the cells sharing the axis, and pads the pooled
;; extent once. Every sharing cell then draws the same axis, and no mark
;; is cut off at the edge of a panel.
;;
;; Two shops below sell the same drinks in two halves of a year. No
;; value in the `:sold` column reaches the tallest stack, and the shared
;; `:y` axis of both cells reaches past that stack:

(def north-shop
  {:drink ["tea" "tea" "coffee" "coffee"]
   :half ["first" "second" "first" "second"]
   :sold [30 45 20 25]})

(def south-shop
  {:drink ["tea" "tea" "cocoa" "cocoa"]
   :half ["first" "second" "first" "second"]
   :sold [60 70 10 15]})

(pj/arrange
 [(-> north-shop (pj/lay-bar :drink :sold {:color :half :position :stack}))
  (-> south-shop (pj/lay-bar :drink :sold {:color :half :position :stack}))]
 {:share-scales #{:y}})

(kind/test-last
 [(fn [v] (let [domains (mapv #(-> % :plan :panels first :y-domain)
                              (:sub-plots (pj/plan v)))
                tallest-stack (+ 60 70)
                largest-value (reduce max (concat (:sold north-shop) (:sold south-shop)))]
            (and (apply = domains)
                 (< largest-value tallest-stack (second (first domains))))))])

;; A rule or a band is covered too. A rule is drawn at a value written
;; on its layer, which no column holds. Here one cell carries a limit
;; beyond anything either species measures, and both cells reach it:

(def limit 8.5)

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (tc/select-rows #(= "setosa" (:species %)))
      (pj/lay-point :sepal-length :sepal-width)
      (pj/lay-rule-v {:x-intercept limit :color "firebrick"}))
  (-> (rdatasets/datasets-iris)
      (tc/select-rows #(= "virginica" (:species %)))
      (pj/lay-point :sepal-length :sepal-width))]
 {:share-scales #{:x}})

(kind/test-last
 ;; Both cells reach past the limit, and they reach it together. Note
 ;; that the shape count says nothing here -- the rule is drawn whether
 ;; or not the axis reaches it, and an axis stopping short would put the
 ;; line outside the panel to be clipped away in silence.
 [(fn [v] (let [panels (mapcat #(:panels (:plan %)) (:sub-plots (pj/plan v)))
                domains (mapv #(mapv double (:x-domain %)) panels)]
            (and (= 2 (count domains))
                 (apply = domains)
                 (< limit (second (first domains))))))])

;; A `:domain` that one cell specifies for a shared axis is the domain
;; of every cell sharing that axis, since a shared axis is one axis.
;; Below, the second cell specifies 0 to 10 for `:x`, and both cells are
;; drawn on exactly that interval:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (tc/select-rows #(= "setosa" (:species %)))
      (pj/lay-point :sepal-length :sepal-width))
  (-> (rdatasets/datasets-iris)
      (tc/select-rows #(= "virginica" (:species %)))
      (pj/lay-point {:x {:column :sepal-length :scale {:domain [0 10]}}
                     :y :sepal-width}))]
 {:share-scales #{:x}})

(kind/test-last
 [(fn [v] (let [panels (mapcat #(:panels (:plan %)) (:sub-plots (pj/plan v)))]
            (= [[0.0 10.0] [0.0 10.0]]
               (mapv #(mapv double (:x-domain %)) panels))))])

;; Two cells that specify different domains for one shared axis report
;; an error naming both. `(pj/scale arrangement :x {:domain [0 10]})`
;; specifies the domain once for every cell.

;; ### Columns That Share an Axis
;;
;; A set of axes shares an axis among the cells that draw the same
;; column on it. Two cells drawing different columns keep their own
;; ranges, which is right when the columns are in different units and
;; wrong when they measure one quantity. Highway mileage and city
;; mileage are both in miles per gallon, and under `#{:x :y}` each
;; panel still fits its own `:y`. Plotje prints a warning that no two
;; cells share `:y`, naming the form shown next:

(-> (rdatasets/ggplot2-mpg)
    (pj/pose {:x :displ})
    pj/lay-point
    (pj/facet {:y [:hwy :cty]})
    (pj/options {:share-scales #{:x :y}}))

(kind/test-last
 [(fn [v] (let [doms (mapv #(-> % :plan :panels first :y-domain)
                           (:sub-plots (pj/plan v)))]
            (and (= 2 (count doms))
                 (not= (first doms) (second doms))
                 (re-find #"no two cells share :y"
                          (with-out-str (pj/plan v))))))])

;; The full form of `:share-scales` is a map from an axis to the columns
;; that go together on that axis. Cells whose columns go together share
;; the axis:
;;
;; - `:same-column` -- each column goes with itself only, which is what
;;   the set says;
;; - a vector of groups of columns, such as `[[:hwy :cty]]` -- the
;;   columns of a group go together, and a column in no group goes with
;;   itself only;
;; - `:all` -- every column goes together, so every cell shares the
;;   axis.
;;
;; An axis the map leaves out is not shared. Here `:x` is shared as
;; before, and the two mileage columns share `:y`, so a point at the
;; same height is the same mileage in both panels:

(-> (rdatasets/ggplot2-mpg)
    (pj/pose {:x :displ})
    pj/lay-point
    (pj/facet {:y [:hwy :cty]})
    (pj/options {:share-scales {:x :same-column
                                :y [[:hwy :cty]]}}))

(kind/test-last
 [(fn [v] (let [doms (fn [pose] (mapv #(-> % :plan :panels first :y-domain)
                                      (:sub-plots (pj/plan pose))))
                [lo hi] (first (doms v))
                all (-> (rdatasets/ggplot2-mpg)
                        (pj/pose {:x :displ})
                        pj/lay-point
                        (pj/facet {:y [:hwy :cty]})
                        (pj/options {:share-scales {:x :same-column :y :all}}))]
            (and (apply = (doms v))
                 ;; the one range covers both columns
                 (<= lo (reduce min (:cty (rdatasets/ggplot2-mpg))))
                 (>= hi (reduce max (:hwy (rdatasets/ggplot2-mpg))))
                 ;; with two columns, :all is the same request
                 (= (doms v) (doms all)))))])

;; A group leaves the other columns alone. With engine displacement as
;; a third panel, the mileage panels share a range and displacement
;; keeps its own:

(-> (rdatasets/ggplot2-mpg)
    (pj/pose {:x :year})
    pj/lay-point
    (pj/facet {:y [:hwy :cty :displ]})
    (pj/options {:share-scales {:x :same-column
                                :y [[:hwy :cty]]}}))

(kind/test-last
 [(fn [v] (let [[hwy cty displ] (mapv #(-> % :plan :panels first :y-domain)
                                      (:sub-plots (pj/plan v)))]
            (and (= hwy cty)
                 (not= hwy displ))))])

;; The columns of a group may come from different datasets, which is
;; how two poses that name one quantity differently are read against
;; each other:

(pj/arrange
 [(-> {:day [1 2 3] :indoor [19.0 20.5 21.0]}
      (pj/lay-line :day :indoor))
  (-> {:day [1 2 3] :outdoor [4.0 9.5 14.0]}
      (pj/lay-line :day :outdoor))]
 {:share-scales {:y [[:indoor :outdoor]]}})

(kind/test-last
 [(fn [v] (let [doms (mapv #(-> % :plan :panels first :y-domain)
                           (:sub-plots (pj/plan v)))
                [lo hi] (first doms)]
            (and (apply = doms)
                 (<= lo 4.0)
                 (>= hi 21.0))))])

;; Columns that go together have to hold one type of value: numbers
;; with numbers, dates with dates, categories with categories. A group
;; holding a numeric column and a categorical one reports an error:

(try
  (-> (rdatasets/ggplot2-mpg)
      (pj/pose {:x :displ})
      pj/lay-point
      (pj/facet {:y [:hwy :drv]})
      (pj/options {:share-scales {:y :all}})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #":share-scales :y refused: columns :hwy, :drv" msg))])

;; ### A Shared Categorical Axis
;;
;; A categorical axis is shared as the list of every category the
;; sharing cells hold. A bar for `tea` then stands at the same place in
;; each cell, and a cell with no rows for a category leaves the place of
;; that category empty. The north shop sells no cocoa and the south shop
;; no coffee; with `:x` shared, both cells list all three drinks. Each
;; bar here is a drink's total for the year:

(def north-totals
  {:drink ["tea" "coffee"] :sold [75 45]})

(def south-totals
  {:drink ["tea" "cocoa"] :sold [130 25]})

(pj/arrange
 [(-> north-totals (pj/lay-bar :drink :sold))
  (-> south-totals (pj/lay-bar :drink :sold))]
 {:share-scales #{:x :y}})

(kind/test-last
 [(fn [v] (let [domains (mapv #(-> % :plan :panels first :x-domain)
                              (:sub-plots (pj/plan v)))]
            (= [["tea" "coffee" "cocoa"] ["tea" "coffee" "cocoa"]] domains)))])

;; The two cells above have data of their own, so the categories come in
;; the order of the cells: the drinks of the first cell, then the drink
;; the second cell adds. Cells that are parts of one dataset -- the
;; panels of a facet, or the cells of a pose arranged over its own data
;; -- list the categories in the order of that dataset's rows, which is
;; the order the same plot draws them in when it is not split.

;; ### Count and Density Axes
;;
;; A histogram draws a count on `:y`, and no column holds that count. An
;; axis like this is shared by what is counted. Two histograms of one
;; column share their count axis:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (tc/select-rows #(= "setosa" (:species %)))
      (pj/lay-histogram :sepal-length))
  (-> (rdatasets/datasets-iris)
      (pj/lay-histogram :sepal-length))]
 {:share-scales #{:y}})

(kind/test-last
 [(fn [v] (let [domains (mapv #(-> % :plan :panels first :y-domain)
                              (:sub-plots (pj/plan v)))]
            (apply = domains)))])

;; Histograms of different columns keep a count axis each under a set of
;; axes, as cells drawing different columns do. A group naming the
;; columns shares their count axes:

(-> (rdatasets/ggplot2-mpg)
    pj/pose
    pj/lay-histogram
    (pj/facet {:x [:hwy :cty]})
    (pj/options {:share-scales {:y [[:hwy :cty]]}}))

(kind/test-last
 [(fn [v] (let [count-axes (fn [pose] (mapv #(-> % :plan :panels first :y-domain)
                                            (:sub-plots (pj/plan pose))))]
            (and (apply = (count-axes v))
                 ;; without the option, a count axis each
                 (apply not= (count-axes (-> (rdatasets/ggplot2-mpg)
                                             pj/pose
                                             pj/lay-histogram
                                             (pj/facet {:x [:hwy :cty]})))))))])

;; A count, a density and the values of a column are measured in
;; different units, so an axis holding one is never pooled with an axis
;; holding another. `{:y :all}` shares every count axis with the other
;; count axes, every density axis with the other density axes, and the
;; axes of columns with each other. Below, the two histograms share one
;; count axis and the scatter keeps the axis of its `:hwy` column:

(pj/arrange
 [(-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :hwy))
  (-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :cty))
  (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy))]
 {:share-scales {:y :all}})

(kind/test-last
 [(fn [v] (let [[hwy-count cty-count hwy-values]
                (mapv #(-> % :plan :panels first :y-domain)
                      (:sub-plots (pj/plan v)))]
            (and (= hwy-count cty-count)
                 (not= hwy-count hwy-values))))])

;; A histogram normalized with `{:normalize :density}` draws a density,
;; and shares with a density curve of the same column. A layer type
;; added by an extension takes part by declaring what its statistic
;; computes; see
;; [Extensibility](./plotje_book.extensibility.html#what-a-stat-computes).

;; ### Cells Drawn Flipped
;;
;; `:share-scales` names an axis as it is drawn: `:x` is the horizontal
;; axis and `:y` the vertical one. Under `(pj/coord :flip)` the column
;; mapped to `:y` runs across the page, and `#{:x}` shares the axis that
;; column is drawn on. (`pj/scale` and `:x-label` name the column's own
;; axis, whichever way that axis is drawn.) The totals of the two shops
;; as flipped bars, sharing the horizontal axis:

(pj/arrange
 [(-> north-totals (pj/lay-bar :drink :sold) (pj/coord :flip))
  (-> south-totals (pj/lay-bar :drink :sold) (pj/coord :flip))]
 {:share-scales #{:x}})

(kind/test-last
 [(fn [v] (let [panels (mapv #(-> % :plan :panels first) (:sub-plots (pj/plan v)))]
            (and (apply = (map :x-domain panels))
                 ;; the horizontal axis holds the numbers
                 (number? (first (:x-domain (first panels))))
                 ;; the vertical axis is each cell's own drinks
                 (apply not= (map :y-domain panels)))))])

;; ### Cells That Line Up
;;
;; Each cell reserves the room its own y labels need, so a cell
;; labelled in single digits comes out wider than a cell labelled in
;; millions, and the axis the two share covers a different extent in
;; each. `:align-panels` reserves the widest y-label pad and legend
;; column any cell needs on all of them, so every cell gets the same
;; drawing area:

(def readings
  {:t [1 2 3 4 5]
   :rate [1.0 2.0 3.0 2.0 4.0]
   :total [1200000.0 2400000.0 1800000.0 3100000.0 2600000.0]})

(pj/arrange
 [(-> readings (pj/lay-line :t :rate))
  (-> readings (pj/lay-line :t :total))]
 {:cols 1 :share-scales #{:x} :align-panels true})

(kind/test-last
 [(fn [v]
    (let [pads-of (fn [pose]
                    (mapv #(get-in % [:plan :layout :y-label-pad])
                          (:sub-plots (pj/plan pose))))
          plain (pads-of (pj/arrange
                          [(-> readings (pj/lay-line :t :rate))
                           (-> readings (pj/lay-line :t :total))]
                          {:cols 1 :share-scales #{:x}}))]
      (and (= 2 (:panels (pj/svg-summary v)))
           ;; `==` rather than `=`: the cell that computed the pad
           ;; carries a double, the cell given it as a floor carries
           ;; whatever it was written as.
           (apply == (pads-of v))
           ;; Without the key the two cells differ, which is the whole
           ;; reason the key exists.
           (not (apply == plain)))))])

;; Pair it with `:share-scales` where the cells are meant to be read
;; against one another. `pj/marginal` sets both for you -- the next
;; section takes one apart.

;; ## Marginal Plots
;;
;; The classic "scatter with top density" -- a distribution strip beside
;; the main plot -- has a function of its own. `pj/marginal` takes a leaf
;; pose and puts a distribution of one of its columns in a thin panel
;; against one edge: the `:x` column above the panel, the `:y` column to
;; its right.

(def marginal
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/marginal :top)))

marginal

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plans (mapv :plan (:sub-plots (pj/plan marginal)))
          panels (mapv #(-> % :panels first) plans)
          [d-x s-x] (mapv :x-domain panels)
          [d-y s-y] (mapv :y-domain panels)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           (pos? (:polygons s))
           ;; Both panels share the same x-domain (sepal-length range).
           (= d-x s-x)
           ;; Each panel has its own y-domain (density vs sepal-width).
           (not= d-y s-y)
           ;; The strip carries neither x ticks nor an x title.
           (= [] (:values (:x-ticks (first panels))))
           (nil? (:x-label (first plans)))
           ;; Both cells reserve the same room on the left for the
           ;; y title and on the right for the legend, so the two
           ;; drawing areas line up. `==` rather than `=`: the cell
           ;; that computed the pad carries a double and the cell
           ;; given it as a floor carries whatever it was written as.
           (apply == (map #(get-in % [:layout :y-label-pad]) plans))
           (apply == (map #(get-in % [:layout :legend-w]) plans)))))])

;; The strip shares the scatter's x axis and keeps its own value scale.
;; Its duplicate x ticks and axis title are dropped, since the axis below
;; describes both panels, and the two drawing areas are aligned so a value
;; sits at the same place in each -- including here, where the scatter
;; carries a legend and the strip does not.
;;
;; The color mapping does not reach the strip. A marginal describes the
;; pose's `:x` column as a whole, so one grey curve is drawn over the
;; three colored groups below it.
;;
;; `:histogram` draws the distribution as bars instead of a curve, and
;; `:size` sets the strip's share of the height (`0.25` by default):

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/marginal :top :histogram {:size 0.35}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 150 (:points s)))))])

;; ### A marginal on the right
;;
;; `:right` describes the pose's `:y` column instead, in a strip beside
;; the panel. The distribution is drawn under `pj/coord :flip`, so its
;; value axis runs across the strip and its baseline stands at the left
;; edge, against the scatter:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/marginal :right))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plans (mapv :plan (:sub-plots (pj/plan v)))
          panels (mapv #(-> % :panels first) plans)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           ;; The scatter comes first here, the strip second -- a
           ;; right marginal reads left to right.
           (= (:y-domain (first panels)) (:y-domain (second panels)))
           ;; The strip carries neither y ticks nor a y title.
           (= [] (:values (:y-ticks (second panels))))
           (nil? (:y-label (second plans)))
           ;; A row of cells lines up on its top and bottom edges, so
           ;; the room reserved below each is what has to agree.
           (apply == (map #(get-in % [:layout :x-label-pad]) plans)))))])

;; The strip shares the panel's vertical axis. `pj/marginal` writes
;; `{:share-scales #{:y}}` for a marginal on the right, and under the
;; flip the strip's `:x` column is the column drawn along that axis.
;;
;; A `:top` marginal and a `:right` one line up on different edges. Two
;; panels in a column share their left and right edges, so what has to
;; agree is the room reserved for the y title and for the legend; two
;; panels in a row share their top and bottom edges, so what has to
;; agree is the room below them. `:align-panels` reserves whichever the
;; layout calls for.
;;
;; `:bottom` and `:left` are not drawn: a strip there would sit between
;; the panel and the axis that describes it.

;; ### What a marginal is made of
;;
;; A `:top` marginal is a vertical composite with a shared x, which can
;; also be written out. Four things make it up: the weights, the shared
;; scale, the ticks and axis title suppressed on the strip, and
;; `:align-panels`. Here are the first three:

(def marginal-by-hand
  (pj/pose
   {:share-scales #{:x}
    :layout {:direction :vertical :weights [1 3]}
    :poses [{:mapping {:x :sepal-length}
             :opts {:suppress-x-ticks true :suppress-x-label true}
             :layers [{:layer-type :density}]}
            {:mapping {:x :sepal-length :y :sepal-width :color :species}
             :layers [{:layer-type :point}]}]
    :data (rdatasets/datasets-iris)}))

marginal-by-hand

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plans (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
          panels (mapv #(-> % :panels first) plans)
          [d-x s-x] (mapv :x-domain panels)]
      (and (= 2 (:panels s))
           (= 150 (:points s))
           (= d-x s-x)
           ;; The strip reserves no legend column and the scatter
           ;; reserves 102 drawing units for one, so the two drawing
           ;; areas are different widths.
           (= [0 102] (mapv #(get-in % [:layout :legend-w]) plans)))))])

;; So the two drawing areas do not line up: the strip draws no legend,
;; reserves no column for one, and comes out wider than the scatter
;; below it. The density then sits at a different scale from the points
;; it describes.
;;
;; That is the fourth part, and the one `pj/marginal` adds:
;; `:align-panels` in the composite's own `:opts`. It plans every cell
;; twice, the second time with the widest pad any of them needed as a
;; floor, so each cell reserves what the others do -- the y-label pad
;; and the legend column for a column of cells, the room below for a
;; row of them. `pj/arrange` takes `:align-panels` among its options,
;; as above, and a written-out composite carries it in its own
;; `:opts`. `pj/options` does not accept it -- it is a composite's own
;; setting rather than a plot option:

(assoc-in marginal-by-hand [:opts :align-panels] true)

(kind/test-last
 [(fn [v]
    (let [plans (mapv :plan (:sub-plots (pj/plan v)))]
      (and (= 2 (:panels (pj/svg-summary v)))
           ;; `==` rather than `=`: the cell that computed the value
           ;; carries a double, the cell given it as a floor carries
           ;; whatever it was written as.
           (apply == (map #(get-in % [:layout :y-label-pad]) plans))
           (apply == (map #(get-in % [:layout :legend-w]) plans)))))])

;; With the key in place the written-out form is the composite
;; `pj/marginal` returns, up to the weights it computes from `:size`.
;; Nothing in the shape is specific to distributions, so the same four
;; parts serve any stack of panels read against one x axis.

;; ## A Small Dashboard
;;
;; Composite poses can combine heterogeneous chart types. Here is a
;; dashboard-style 2x2 layout: a histogram of sepal length, a boxplot
;; of sepal width by species, a scatter of petal dimensions, and a
;; density of petal length.
;;
;; Each cell is built as its own leaf pose, then `pj/arrange` takes
;; rows of cells and produces the 2x2 grid:

(def dashboard
  (pj/arrange
   [[(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))
     (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width {:color :species}))]
    [(-> (rdatasets/datasets-iris) (pj/lay-point :petal-length :petal-width {:color :species}))
     (-> (rdatasets/datasets-iris) (pj/lay-density :petal-length {:color :species}))]]))

dashboard

(kind/test-last
 [(fn [v]
    (let [chrome (-> dashboard pj/plan :chrome)]
      (and (= 4 (:panels (pj/svg-summary v)))
           ;; The histogram cell has no :color, so :color is not
           ;; shared across all leaves -- each colored cell renders
           ;; its own legend (see Notes below).
           (= #{} (:shared-aesthetics chrome)))))])

;; Four panels, each its own layer type. `pj/arrange` stacks the rows
;; vertically; each row is laid out horizontally. Every cell is a
;; leaf pose -- `pj/arrange` does not accept composite cells.

;; ## Drawing Layers with Different Data
;;
;; A layer can carry its own `:data` via the layer options map.
;; This is how reference lines, predictions drawn over the data, and
;; small annotation datasets attach to a plot. The layer reads the
;; columns it names from its own data, and the columns it leaves to the
;; pose from its own data too.
;;
;; A layer with data of its own is drawn on the pose's panel, like any
;; other layer of the pose. A separate panel for it comes from
;; `pj/arrange`.
;;
;; ### On the same panel
;;
;; Here a base scatter gets a second set of points from another
;; dataset, whose columns are named `:x` and `:y`:

(def overlay-base
  {:fitted   [1 2 3]
   :residual [1 2 3]})

(def overlay-other
  (tc/dataset {:x [0.5 1.5 2.5]
               :y [1.5 2.5 3.5]}))

(-> overlay-base
    (pj/lay-point :fitted :residual {:color "#377eb8"})
    (pj/lay-point :x :y {:color "#e6550d" :data overlay-other}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 1 (:panels s))
           (= 6 (:points s)))))])

;; One panel with six points -- three from each layer. Layers paint in
;; the order they were added -- see
;; [Poses](./plotje_book.pose_model.html#layer-order-is-paint-order) --
;; so add the layer you want on top last.
;;
;; Each axis is titled by both columns drawn on it, `fitted, x` and
;; `residual, y`. Where the incoming columns are the same quantity under
;; another name, renaming them to the pose's columns with
;; `tc/rename-columns` draws the same marks under one name per axis:

(-> overlay-base
    (pj/lay-point :fitted :residual {:color "#377eb8"})
    (pj/lay-point :fitted :residual
                  {:color "#e6550d"
                   :data (tc/rename-columns overlay-other
                                            {:x :fitted :y :residual})}))

(kind/test-last
 [(fn [v]
    (let [unrenamed (-> overlay-base
                        (pj/lay-point :fitted :residual {:color "#377eb8"})
                        (pj/lay-point :x :y {:color "#e6550d" :data overlay-other}))
          titles (fn [pose] ((juxt :x-label :y-label) (pj/plan pose)))
          domains (fn [pose] ((juxt :x-domain :y-domain) (first (:panels (pj/plan pose)))))]
      (and (= ["fitted" "residual"] (titles v))
           (= ["fitted, x" "residual, y"] (titles unrenamed))
           ;; the same marks on the same axes
           (= (domains v) (domains unrenamed))
           (= (:points (pj/svg-summary v)) (:points (pj/svg-summary unrenamed))))))])

;; ### A panel of its own
;;
;; To draw the other dataset beside the base scatter, build each as a
;; pose and arrange them:

(pj/arrange [(pj/lay-point overlay-base :fitted :residual {:color "#377eb8"})
             (pj/lay-point overlay-other :x :y {:color "#e6550d"})])

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 2 (:panels s))
           (= 6 (:points s))
           ;; The same two colours as the two examples above, so the
           ;; outcomes can be compared mark for mark.
           (= #{"rgb(55,126,184)" "rgb(230,85,13)"}
              (disj (:colors s) "none")))))])

;; Each panel has three points and its own axis titles: the first
;; shows `fitted` against `residual`, the second `x` against `y`.

;; ## Panels Clip Their Own Content
;;
;; Each panel confines its marks to its own rectangle. A layer whose
;; data runs past the panel's domain -- a reference line drawn well
;; beyond the visible range, say -- is clipped at the panel edge
;; rather than painting across its neighbours. The clip is applied per
;; panel, so a stacked arrangement reports one clip region per panel.

(def bounded
  (-> {:x [1 2 3 4 5] :y [10 20 15 25 18]}
      (pj/lay-point :x :y)
      (pj/lay-line {:data {:x [1 5] :y [-200 300]}})
      (pj/scale :y {:type :linear :domain [0 30]})))

(pj/arrange [bounded bounded] {:cols 1})

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 2 (:clips s)))))])

;; ## Notes on the Current Implementation
;;
;; A few details about how composition renders today:
;;
;; - **Each leaf draws its own axes, labels, and ticks.** Shared
;;   scales align the data ranges across panels, but these
;;   decorations are per-leaf in `:horizontal` and `:vertical`
;;   layouts, so a stack of cells reading one x column carries that
;;   axis label once per cell. A cell drops its own with
;;   `:suppress-x-ticks` and `:suppress-x-label` in that cell's
;;   `:opts`, which is what `pj/marginal` writes on the strip it
;;   builds. (Matrix layouts -- the SPLOM grid in particular --
;;   replace per-leaf x/y labels with shared strip labels, and
;;   SPLOM additionally suppresses ticks on interior cells.)
;; - **Plot-area edges may not line up** across composite siblings,
;;   since each leaf reserves its own padding for axes and labels --
;;   two sub-poses with different label lengths, or one drawing a
;;   legend that its neighbour does not, produce visibly different
;;   panel widths. `:align-panels` on `pj/arrange` plans the cells
;;   against a common floor for both the y-label pad and the legend
;;   column, and `pj/marginal` turns it on (see Cells That Line Up).
;; - **Each cell has its own plot options.** A cell is a pose, so
;;   `pj/options`, `pj/scale` and `pj/coord` written on it apply to
;;   that cell: one cell can be log-scaled and its neighbour linear,
;;   and each can have its own title. Written on the composite itself,
;;   they apply to every cell that does not set them. This is also the
;;   way around a limitation within one pose -- two layers cannot read
;;   one aesthetic through different scales, but two cells can.
;; - **Cells that map the same column share one scale and one
;;   legend.** If every leaf maps an aesthetic to the same column
;;   (e.g., `:color :species` in all cells), each cell is given one
;;   scale for it -- the categories of all the cells together, or the
;;   range of all their values -- and a single legend is drawn for the
;;   composite. A category is then the same colour, and a value the
;;   same shade, in every cell, even where the cells hold different
;;   data. A legend is drawn once, so its scale has to hold in every
;;   cell; an axis is labelled in each cell, which is why axes stay
;;   independent unless `:share-scales` names them. A cell that writes
;;   its own scale for the aesthetic keeps it, and each cell then
;;   draws its own legend. When the mappings disagree -- or when only
;;   some leaves carry the aesthetic, as in the dashboard above --
;;   each leaf with that aesthetic draws its own legend.
;; - **Multi-row layouts go through `pj/arrange`.** A row of rows is
;;   built by passing nested vectors of poses to `pj/arrange` (the
;;   dashboard example above shows the shape). A cell may be a
;;   composite itself, so an arranged pose can be arranged again and
;;   the cell draws its own grid. Unequal cell weights are the one
;;   layout feature `pj/arrange` does not reach: write `:weights` in
;;   a literal composite map and pass the map to `pj/pose`.

;; Both cells below map `:color` to the same column, so the composite
;; draws one legend for both:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width {:color :species}))])

(kind/test-last
 [(fn [v]
    (and (= #{:color} (-> v pj/plan :chrome :shared-aesthetics))
         ;; The dashboard above pairs a histogram with a colored
         ;; scatter, so its siblings do not agree and nothing merges.
         (= #{} (-> (pj/arrange
                     [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))
                      (-> (rdatasets/datasets-iris)
                          (pj/lay-point :petal-length :petal-width {:color :species}))])
                    pj/plan :chrome :shared-aesthetics))))])

;; The cells need not hold the same rows. Below, the left cell holds
;; setosa and versicolor and the right cell versicolor and virginica.
;; Versicolor is drawn in the same colour in both, and the one legend
;; lists all three species:

(pj/arrange
 [(-> (rdatasets/datasets-iris)
      (tc/select-rows #(not= "virginica" (:species %)))
      (pj/lay-point :sepal-length :sepal-width {:color :species}))
  (-> (rdatasets/datasets-iris)
      (tc/select-rows #(not= "setosa" (:species %)))
      (pj/lay-point :sepal-length :sepal-width {:color :species}))])

(kind/test-last
 [(fn [v]
    (let [plan (pj/plan v)
          ;; each cell's colour for versicolor, read off its plan
          versicolor (fn [sp] (some #(when (= "versicolor" (:label %)) (:color %))
                                    (-> sp :plan :panels first :layers first :groups)))
          [left right] (:sub-plots plan)]
      (and (= ["setosa" "versicolor" "virginica"]
              (mapv :label (-> plan :chrome :shared-legend :legend :entries)))
           (some? (versicolor left))
           (= (versicolor left) (versicolor right)))))])

;; A cell that is itself a composite draws its own grid. Here the left
;; cell holds two panels of its own and the right cell holds one, so
;; the arranged pose draws three panels in two columns:

(pj/arrange
 [(pj/arrange
   [(-> (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width))
    (-> (rdatasets/datasets-iris)
        (pj/lay-point :petal-length :petal-width))]
   {:cols 1})
  (-> (rdatasets/datasets-iris)
      (pj/lay-histogram :sepal-length))])

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; ## What's Next
;;
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) --
;;   the taxonomy of layer options, plot options, and configuration
;;   (next chapter in Foundations)
;; - [**Faceting**](./plotje_book.faceting.html) -- panel splits by
;;   a categorical column (a data-driven composite, covered in How-to)
;; - [**Gallery**](./plotje_book.gallery.html) -- more composition
;;   examples alongside single-plot chart types
