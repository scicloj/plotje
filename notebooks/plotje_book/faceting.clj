;; # Faceting
;;
;; Faceting draws a pose as several panels: one panel for each category
;; of a column, or one panel for each of several columns. Panels side by
;; side make it easy to compare patterns across groups.
;;
;; A facet over the categories of a column is a mapping: `:col` and
;; `:row` are aesthetics, and `pj/facet` is the call that writes one.
;; Much follows from that -- a facet reaches into a composite, a cell
;; can override the facet it inherits, and several columns under `:col`
;; unite into one distinction. A facet over columns is written beside
;; the mapping, and a grid crosses the two kinds. The sections below teach
;; those ideas, and the Examples section at the end shows faceted plots
;; of several kinds.

(ns plotje-book.faceting
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## A Panel per Category
;;
;; `pj/facet` splits a pose into panels by one categorical column, one
;; panel for each category. By default the panels run across, in one
;; row:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          panels (:panels (pj/plan (-> (rdatasets/datasets-iris)
                                       (pj/lay-point :sepal-length :sepal-width
                                                     {:color :species})
                                       (pj/facet :species))))
          x-doms (mapv :x-domain panels)
          y-doms (mapv :y-domain panels)]
      (and (= 3 (:panels s))
           (= 150 (:points s))
           ;; one row, three panels across
           (= #{0} (set (map :row panels)))
           (= [0 1 2] (mapv :col panels))
           ;; By default every panel shares both
           ;; x-domain and y-domain.
           (apply = x-doms)
           (apply = y-doms))))])

;; Each species gets its own panel with a strip label on top.
;; Scales are shared by default -- all panels use the same x and y range,
;; making direct comparison easy.

;; ### Ordering the panels
;;
;; The panels follow the order in which the data holds the categories.
;; A `:domain` on `:col` orders them instead, as a `:domain` on an axis
;; orders its categories, and one on `:row` orders the rows of panels:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet :drv)
    (pj/scale :col {:domain ["r" "f" "4"]}))

(kind/test-last
 [(fn [v] (= ["r" "f" "4"]
             (filterv #{"r" "f" "4"} (drop-while #(not= "r" %) (:texts (pj/svg-summary v))))))])

;; The panels, the categories of an axis and the entries of a legend
;; follow one rule: the order the data holds them in, unless a `:domain`
;; says otherwise. Colouring by the faceted column and giving both
;; scales the same `:domain` lists the legend in the order of the
;; panels:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:color :drv})
    (pj/facet :drv)
    (pj/scale :col {:domain ["r" "f" "4"]})
    (pj/scale :color {:domain ["r" "f" "4"]}))

(kind/test-last
 [(fn [v] (let [plan (pj/plan v)]
            (= ["r" "f" "4"]
               (mapv :col-label (:panels plan))
               (mapv :label (:entries (:legend plan))))))])

;; A category the `:domain` leaves out still draws its panel, after the
;; listed ones, and a name the `:domain` lists that the data does not
;; hold draws no panel. Each prints a warning:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet :drv)
    (pj/scale :col {:domain ["r" "f" "4" "x"]}))

(kind/test-last
 [(fn [v] (let [printed (with-out-str (pj/plan v))
                omitting (-> (rdatasets/ggplot2-mpg)
                             (pj/lay-point :displ :hwy)
                             (pj/facet :drv)
                             (pj/scale :col {:domain ["r" "f"]}))]
            (and (= 3 (:panels (pj/svg-summary v)))
                 (re-find #":domain names \[.x.\], which the data does not hold" printed)
                 (= 3 (:panels (pj/svg-summary omitting)))
                 (re-find #":domain omits \[.4.\]" (with-out-str (pj/plan omitting))))))])

;; ## Panels Down, and a Grid of Categories
;;
;; `{:row :species}` puts the panels down, in one column:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet {:row :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s)))))])

;; `{:col ... :row ...}` splits by two columns -- the column under `:col`
;; gives a column of panels for each of its values, the column under
;; `:row` a row of panels:

(-> (rdatasets/reshape2-tips)
    (pj/lay-point :total-bill :tip {:color :sex})
    (pj/facet {:col :smoker :row :sex}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:panels s))
                                (= 244 (:points s)))))])

;; Row labels appear on the right, column labels on top. A strip prints
;; the category alone, so in this grid nothing on the plot says which
;; strips are values of `smoker` and which of `sex`. The plot option
;; `{:strip-labels :both}` prints the column's name before each value:

(-> (rdatasets/reshape2-tips)
    (pj/lay-point :total-bill :tip {:color :sex})
    (pj/facet {:col :smoker :row :sex} {:strip-labels :both}))

(kind/test-last [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
                           (and (texts "smoker: No")
                                (texts "sex: Female"))))])

;; ## A Panel per Column
;;
;; `pj/facet` given a map of columns draws a panel for each column.
;; Under `:x` the panels run across, each with one of the columns on its
;; `:x` axis; under `:y` they run down, each with one of the columns on
;; its `:y` axis. With no layer written, each panel draws the layer type
;; inferred from its columns -- here a histogram of each column:

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/facet {:x [:sepal-length :sepal-width :petal-length]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               boxes (map (comp :panel-box :frames)
                                          (:panels (pj/frames v)))]
                           (and (= 3 (:panels s))
                                (pos? (:polygons s))
                                ;; across: three left edges, one top edge
                                (= 3 (count (distinct (map first boxes))))
                                (= 1 (count (distinct (map second boxes)))))))])

;; A layer that names none of the facet's columns is drawn in every
;; panel. Name the column the panels have in common on the pose, and
;; leave the other column to the facet:

(-> (rdatasets/ggplot2-mpg)
    (pj/pose {:x :displ})
    (pj/lay-smooth {:color "black"})
    (pj/facet {:y [:hwy :cty]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               boxes (map (comp :panel-box :frames)
                                          (:panels (pj/frames v)))]
                           (and (= 2 (:panels s))
                                (= 2 (:lines s))
                                (zero? (:points s))
                                ;; down: one left edge, two top edges
                                (= 1 (count (distinct (map first boxes))))
                                (= 2 (count (distinct (map second boxes)))))))])

;; A layer that names one of the facet's columns is drawn only in the
;; panel of that column. A panel left with no layer draws the layer type
;; inferred from its columns, and Plotje prints a Note naming the layer,
;; the panels, and the change that draws the layer in every panel:

(with-out-str
  (-> (rdatasets/ggplot2-mpg)
      (pj/lay-smooth :displ :hwy {:color "black"})
      (pj/facet {:y [:hwy :cty]})
      pj/plan))

(kind/test-last
 [(fn [out] (and (re-find #"pj/lay-smooth names :hwy on :y" out)
                 (re-find #"\{:y :cty\} has no layer" out)
                 (re-find #"leave :y off the layer and name :x on the pose" out)))])

;; ## Grids Crossing Categories and Columns
;;
;; A grid compares along two directions at once: species across and
;; measurements down, or every measurement against every other. Every
;; grid is one `pj/facet` call with a map under `:col` and `:row`, and
;; each of the two entries is one of the two kinds of facet this chapter
;; has shown -- a column of categories, as in `{:col :species}`, or a
;; map of columns, as in `{:y [:hwy :cty]}`. The grid has a panel for
;; every pair of a value across and a value down.
;;
;; | under `:col` | under `:row` | the grid |
;; |:--|:--|:--|
;; | a column of categories | a column of categories | categories by categories, `{:col :smoker :row :sex}` above |
;; | a column of categories | a map of columns | each category across, each column down |
;; | a map of columns | a column of categories | each column across, each category down |
;; | a map of columns | a map of columns | each column against each column -- a scatter plot matrix when the two lists are the same |
;;
;; ### Categories crossed with columns
;;
;; Here the species run across and two measurements run down, each
;; drawn against `:sepal-length`:

(-> (rdatasets/datasets-iris)
    (pj/pose {:x :sepal-length})
    pj/lay-point
    (pj/facet {:col :species :row {:y [:sepal-width :petal-width]}}))

(kind/test-last [(fn [v] (let [base (-> (rdatasets/datasets-iris)
                                        (pj/pose {:x :sepal-length})
                                        pj/lay-point)
                               ys [:sepal-width :petal-width]]
                           (and (= {:panels 6 :points 300}
                                   (select-keys (pj/svg-summary v) [:panels :points]))
                                ;; the two calls, in either order, give this pose
                                (= v (-> base (pj/facet :species) (pj/facet {:y ys})))
                                (= v (-> base (pj/facet {:y ys}) (pj/facet :species)))
                                ;; the prose below: a third facet suggests a colour or pj/arrange
                                (let [m (try (pj/facet v {:row :species}) nil
                                             (catch clojure.lang.ExceptionInfo e (ex-message e)))]
                                  (and (re-find #"both directions" m)
                                       (re-find #":color" m)
                                       (re-find #"pj/arrange" m))))))])

;; The grid may also be written as two calls, one facet over the
;; categories and one over the columns, in either order:
;; `(-> pose (pj/facet {:y [...]}) (pj/facet :species))`. The categories
;; go into the pose's mapping and the columns under its `:facet`, and
;; the two are crossed when the pose is drawn. When both directions are
;; taken, a third facet reports an error that suggests a colour or
;; `pj/arrange` instead.

;; ### Scatter plot matrix
;;
;; A **scatter plot matrix (SPLOM)** draws every column of a list
;; against every column of the same list, to show all their pairwise
;; relationships at once. The same list under `:col`, on `:x`, and
;; under `:row`, on `:y`, gives the matrix, and the panel of a column
;; against itself draws that one column, as a histogram. The histogram
;; is drawn within the column's own range, so it shares its row's `:y`
;; axis and the row's tick labels stand at the left edge; its bar
;; heights are relative, with no count axis:

(-> (rdatasets/datasets-iris)
    (pj/pose {:color :species})
    (pj/facet {:col {:x [:sepal-length :sepal-width :petal-length]}
               :row {:y [:sepal-length :sepal-width :petal-length]}})
    pj/lay-point)

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 9 (:panels s))
                                ;; the six panels off the diagonal draw every row
                                (= (* 6 150) (:points s))
                                (pos? (:polygons s))
                                ;; the diagonal panel of each row reads the row's :y domain
                                (let [panels (into {} (map (juxt :path #(first (:panels (:plan %)))))
                                                   (:sub-plots (pj/plan v)))]
                                  (every? (fn [r] (= (:y-domain (panels [r r]))
                                                     (:y-domain (panels [r (mod (inc r) 3)]))))
                                          (range 3))))))])

;; Two different lists cross the same way: `{:col {:x xs} :row {:y ys}}`
;; draws each column of `xs` against each column of `ys`. Written
;; without `:col` and `:row`, `{:x xs :y ys}` pairs the columns one by
;; one instead, a panel per pair, as the section on layers on different
;; columns shows below. The
;; [Relationships](./plotje_book.relationships.html#scatter-plot-matrix-splom)
;; chapter builds scatter plot matrices with more layers.

;; ## Faceting is a mapping

;; The panel aesthetics `:col` and `:row` give each value of a column
;; a panel of its own, told apart by a strip label -- where `:color`
;; tells the marks apart by colour on one panel. They are documented
;; here, where they are written, rather than beside the keys of a
;; layer's mapping,
;; because no `pj/lay-*` call accepts one.

(kind/table
 {:column-names [:aesthetic :what-it-reads]
  :row-vectors (mapv (fn [[k doc]] [k doc])
                     (sort-by (comp str key) pj/panel-aesthetic-docs))})

(kind/test-last
 [(fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))])

;; `pj/facet` writes `:col` into the pose's mapping. Here is the plot,
;; and then the pose that draws it.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species))

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species)
    kind/pprint)

(kind/test-last
 [(fn [fr] (= :species (get-in fr [:mapping :col])))])

;; `(pj/facet my-pose {:row col})` writes `:row` instead, and
;; `(pj/facet my-pose {:col col-a :row col-b})` writes both.

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet {:col :drv :row :cyl})
    kind/pprint)

(kind/test-last
 [(fn [fr] (and (= :drv (get-in fr [:mapping :col]))
                (= :cyl (get-in fr [:mapping :row]))))])

;; Any mapping value may be written as a map naming its source, and a
;; panel aesthetic is no exception. These two draw the same plot.

(-> (rdatasets/datasets-iris)
    (pj/pose {:x :sepal-length :y :sepal-width :col {:column :species}})
    (pj/lay-point))

(kind/test-last
 [(fn [fr] (= (pj/svg-summary fr)
              (pj/svg-summary (-> (rdatasets/datasets-iris)
                                  (pj/lay-point :sepal-length :sepal-width)
                                  (pj/facet :species)))))])

;; ## What the scope rules give

;; Mappings flow downward: a mapping written on a pose reaches every
;; layer and every sub-pose below it, and a sub-pose that writes its
;; own key overrides what it inherits. A panel aesthetic is a mapping,
;; so all of that applies to faceting.

;; ### A composite can be faceted

;; A facet written on an arranged pose divides every cell of it.

(-> (pj/arrange [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
                 [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
    (pj/lay-point)
    (pj/facet :species)
    (pj/options {:width 700 :height 560}))

(kind/test-last
 [(fn [fr] (= 6 (:panels (pj/svg-summary fr))))])

;; Two cells, three species, six panels. `pj/arrange` takes one vector
;; per row, so `[[a] [b]]` stacks the two cells and each has the full
;; width for its three panels.
;;
;; A facet over columns is not a mapping, so it does not reach into a
;; composite: `pj/facet` given an arranged pose and a map of columns
;; reports an error naming the spellings that draw -- a facet on each
;; cell, or the columns arranged as cells.

(kind/test-last
 [(fn [_]
    (try (pj/facet (pj/arrange [(pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
                                (pj/lay-point (rdatasets/ggplot2-mpg) :displ :cty)])
                   {:y [:hwy :cty]})
         false
         (catch clojure.lang.ExceptionInfo e
           (boolean (re-find #"on a composite pose" (ex-message e))))))])

;; ### One cell can be faceted differently from another

;; A cell that writes its own `:col` overrides the one it inherits,
;; which is the rule for every mapping key. Here the top cell is
;; divided by species and the bottom cell is not divided at all.

(-> (pj/arrange [[(-> (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
                      (pj/facet :species))]
                 [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
    (pj/lay-point)
    (pj/options {:width 700 :height 560}))

(kind/test-last
 [(fn [fr] (and (= 4 (:panels (pj/svg-summary fr)))
                (= [3 1] (mapv #(count (:panels (:plan %)))
                               (:sub-plots (pj/plan fr))))))])

;; Three panels in the top row and one in the bottom row.

;; ### Layers on different columns, faceted

;; The layers of a pose share its panel, so a pose whose layers name
;; different columns draws one panel, and faceting it by a column draws
;; one panel per value:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width)
    (pj/facet :species)
    (pj/options {:width 900 :height 480}))

(kind/test-last
 [(fn [fr] (= 3 (:panels (pj/svg-summary fr))))])

;; To give each pair of columns a row of its own as well, cross the
;; categories with the columns in one `pj/facet` call. Each layer is
;; drawn in the row of the columns it names. Two calls, one facet over
;; the pairs and one over `:species`, write the same pose in either
;; order:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width)
    (pj/facet {:col :species
               :row {:x [:sepal-length :petal-length]
                     :y [:sepal-width :petal-width]}})
    (pj/options {:width 900 :height 480}))

(kind/test-last
 [(fn [fr] (and (= {:panels 6 :points 300}
                   (select-keys (pj/svg-summary fr) [:panels :points]))
                (= fr (-> (rdatasets/datasets-iris)
                          (pj/lay-point :sepal-length :sepal-width)
                          (pj/lay-point :petal-length :petal-width)
                          (pj/facet {:x [:sepal-length :petal-length]
                                     :y [:sepal-width :petal-width]})
                          (pj/facet :species)
                          (pj/options {:width 900 :height 480})))
                (= fr (-> (rdatasets/datasets-iris)
                          (pj/lay-point :sepal-length :sepal-width)
                          (pj/lay-point :petal-length :petal-width)
                          (pj/facet :species)
                          (pj/facet {:x [:sepal-length :petal-length]
                                     :y [:sepal-width :petal-width]})
                          (pj/options {:width 900 :height 480})))))])

;; ### A marginal follows the division

;; `pj/marginal` builds a fresh leaf rather than a descendant, so the
;; scope rules do not reach it on their own. It carries the panel
;; aesthetics across itself, giving a faceted main panel a marginal
;; above each of its panels. The strip label is drawn once per column,
;; at the top.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species)
    (pj/marginal :top)
    (pj/options {:width 900 :height 480}))

(kind/test-last
 [(fn [fr] (let [s (pj/svg-summary fr)
                 labelled (->> (pj/plan fr) :sub-plots
                               (mapcat (comp :panels :plan))
                               (keep :col-label))]
             (and (= 6 (:panels s))
                  (= 150 (:points s))
                  (= ["setosa" "versicolor" "virginica"] (vec labelled)))))])

;; ## Several columns as one key

;; Under some aesthetics, several columns in a vector are one
;; distinction made of those columns united -- a compound key. That is
;; what `{:group [:a :b]}` means, and the panel aesthetics read a vector
;; the same way. `pj/compound-key-aesthetics` names the set, and it is
;; the set the check reads, so an aesthetic outside it reports several
;; columns where one goes rather than uniting them:

(pj/compound-key-aesthetics)

(kind/test-last [(fn [s] (= #{:group :col :row} s))])

;; The table below holds three of the four combinations of `:part` and
;; `:dimension`: there is no petal width row.

(def measures
  (tc/dataset {:part      ["sepal" "sepal" "sepal" "sepal" "petal" "petal"]
               :dimension ["length" "length" "width" "width" "length" "length"]
               :t         [1 2 1 2 1 2]
               :v         [1.0 2.0 1.5 2.5 2.0 3.0]}))

measures

;; `:color` is outside the set, because a mark has one colour. A vector
;; written there is reported rather than united:

(try
  (-> measures
      (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #":color was given several columns" msg))])

;; A compound facet draws one panel per combination the data holds,
;; labelled by each column's value in turn.

(-> measures
    (pj/lay-point :t :v)
    (pj/facet [:part :dimension]))

(kind/test-last
 [(fn [fr] (and (= 3 (:panels (pj/svg-summary fr)))
                (= ["sepal / length" "sepal / width" "petal / length"]
                   (mapv :col-label (:panels (pj/plan fr))))))])

;; `{:col :part :row :dimension}` crosses two distinctions and fills the
;; rectangle, so the combination the data does not hold is drawn as an
;; empty panel.

(-> measures
    (pj/lay-point :t :v)
    (pj/facet {:col :part :row :dimension}))

(kind/test-last
 [(fn [fr] (= 4 (:panels (pj/svg-summary fr))))])

;; ## Free Scales (Independent Axis Ranges)
;;
;; By default the panels of a facet share each axis on which they draw
;; the same column. In a facet over categories every panel draws the
;; same columns, so the panels share both axis ranges, and a mark at
;; the same height stands for the same value in every panel. In a facet
;; over columns, the panels drawing different columns keep an axis
;; range each. The
;; `:share-scales` option names the axes the panels share, as a set. An
;; axis left out of the set is fitted to the rows of each panel. A map,
;; as in `{:y :all}`, shares an axis between panels that draw different
;; columns, as the [Series](./plotje_book.series.html) chapter shows. An
;; axis the map leaves out is not shared: on a facet over columns,
;; `{:y :all}` alone leaves `:x` unshared, so every panel labels its own
;; `:x` ticks, and `{:x :same-column :y :all}` keeps `:x` shared among
;; the panels drawing the same column, labelled once.
;;
;; Both axes shared -- the default, written out:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species)
    (pj/options {:share-scales #{:x :y}}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          default (-> (rdatasets/datasets-iris)
                      (pj/lay-point :sepal-length :sepal-width {:color :species})
                      (pj/facet :species))
          doms (mapv (juxt :x-domain :y-domain) (:panels (pj/plan v)))]
      (and (= 3 (:panels s))
           (= 150 (:points s))
           (apply = doms)
           ;; writing the default changes nothing
           (= (pj/plot v) (pj/plot default)))))])

;; Only `:x` shared -- each panel has its own y range:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species)
    (pj/options {:share-scales #{:x}}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          ;; [distinct x ranges, distinct y ranges] under a set of axes
          ranges (fn [axes]
                   (let [panels (:panels (pj/plan
                                          (-> (rdatasets/datasets-iris)
                                              (pj/lay-point :sepal-length :sepal-width)
                                              (pj/facet :species)
                                              (pj/options {:share-scales axes}))))]
                     [(count (distinct (map :x-domain panels)))
                      (count (distinct (map :y-domain panels)))]))]
      (and (= 3 (:panels s))
           (= [1 3] (ranges #{:x}))
           ;; the two sets the prose below names
           (= [3 1] (ranges #{:y}))
           (= [3 3] (ranges #{})))))])

;; `#{:y}` shares `:y` and fits `:x` to each panel, and `#{}` fits both
;; axes to each panel. In a grid of categories too, an axis left out of the
;; set is fitted to each panel, where ggplot2's `facet_grid(scales =
;; "free_y")` fits it to each row of panels.
;;
;; The options of a faceted plot may also be written in the facet call,
;; in a map after the column. `pj/facet` passes that map to
;; `pj/options`, so the two spellings build the same pose:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species {:share-scales #{:x}}))

(kind/test-last
 [(fn [v]
    (= v (-> (rdatasets/datasets-iris)
             (pj/lay-point :sepal-length :sepal-width {:color :species})
             (pj/facet :species)
             (pj/options {:share-scales #{:x}}))))])
;;
;; A shared axis covers what the panels sharing it draw, and is padded
;; once. So the panels of a facet read on the axes the same plot has
;; when it is not split:

(let [whole (-> (rdatasets/datasets-iris)
                (pj/lay-point :sepal-length :sepal-width))
      domains (fn [pose] (mapv (juxt :x-domain :y-domain) (:panels (pj/plan pose))))]
  {:not-split (first (domains whole))
   :each-panel (distinct (domains (pj/facet whole :species)))})

(kind/test-last
 [(fn [{:keys [not-split each-panel]}]
    (= [not-split] each-panel))])

;; A shared axis of categories lists every category the panels hold, in
;; the order of the data, so a category sits at the same place in every
;; panel. The panels of a faceted histogram share their count axis.
;;
;; `pj/arrange` takes the same option for its cells, where it can also
;; say which columns share an axis; see
;; [Composition](./plotje_book.composition.html#shared-scales) and, for
;; what a shared axis covers,
;; [What a Shared Axis Covers](./plotje_book.composition.html#what-a-shared-axis-covers).

;; ## Appearance Aesthetics Across Panels
;;
;; `:share-scales` is about the axes, and the axes alone. A `:size` or `:alpha`
;; column is read against every value the plot holds, whichever panel a
;; mark sits in, so one value is one size everywhere and the legend
;; explains every panel at once.
;;
;; Below, the left panel's values run from 1 to 3 and the right panel's
;; from 4 to 10, so every mark on the left is smaller than every mark
;; on the right:

(def per-panel
  {:g ["L" "L" "L" "R" "R" "R"]
   :x [1 2 3 1 2 3]
   :y [1 1 1 1 1 1]
   :n [1 2 3 4 7 10]})

(-> per-panel
    (pj/lay-point :x :y {:size :n})
    (pj/facet :g))

(kind/test-last
 [(fn [v]
    (let [layers (->> v pj/plan :panels (mapcat :layers))]
      (and
       ;; One extent for the plot rather than one per panel -- the
       ;; lowest and highest value anywhere, which is what the legend
       ;; is built from as well.
       (= 2 (count layers))
       (= [[1 10] [1 10]] (mapv :size-extent layers))
       ;; And the values each panel draws, which the shared extent
       ;; turns into sizes that can be compared across the two.
       (= [[1 2 3] [4 7 10]]
          (mapv (fn [l] (vec (mapcat :sizes (:groups l)))) layers)))))])

;; A `:color` or `:shape` column is read against the whole data too. The
;; legend lists the categories in the order the rows of the data hold
;; them, whichever panel a row is drawn in, so a category has the colour
;; it has in the same plot drawn without the facet. Below, the first
;; panel holds some of the classes and not others, and the legend is
;; the legend of the unfaceted plot:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:color :class})
    (pj/facet :drv))

(kind/test-last
 [(fn [v]
    (let [labels (fn [pose] (mapv :label (:entries (:legend (pj/plan pose)))))
          mpg (rdatasets/ggplot2-mpg)]
      (and (= (labels v)
              (labels (pj/lay-point mpg :displ :hwy {:color :class}))
              (vec (distinct (:class mpg))))
           ;; Panel by panel the classes would come in another order:
           ;; the first panel does not hold the first three alone.
           (not= (vec (distinct (:class mpg)))
                 (vec (distinct (mapcat (fn [drv] (:class (tc/select-rows mpg #(= drv (:drv %)))))
                                        (distinct (:drv mpg)))))))))])

;; A `:domain` sets that one extent. Given more room than the data
;; needs, no mark reaches either end of the range:

(-> per-panel
    (pj/lay-point :x :y {:size :n})
    (pj/facet :g)
    (pj/scale :size {:domain [0 20]}))

(kind/test-last
 [(fn [v]
    (let [radii (sort (:sizes (pj/svg-summary v)))]
      ;; The range runs 2 to 8 by default, and the widened domain keeps
      ;; every mark inside it.
      (and (= 6 (count radii))
           (> (first radii) 2.0)
           (< (last radii) 8.0))))])

;; ## Where it is refused

;; A facet divides every layer of the pose alike, and a panel aesthetic
;; is read from a pose's mapping and not from a layer's, so a panel
;; aesthetic belongs on the pose. Written in a `pj/lay-*` mapping
;; it is reported, and the report names the places to write it
;; instead.

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:col :species})
      pj/plan)
  (catch Exception e (ex-message e)))

(kind/test-last
 ;; The message names the aesthetic and says where a panel aesthetic is
 ;; read from. It used to say "Faceting is plot-level, not
 ;; layer-level", which this chapter's own subject made false --
 ;; faceting is a mapping and obeys the scope rules.
 [(fn [m] (and (string? m)
               (re-find #"panel aesthetic" m)
               (re-find #"read from a pose's mapping" m)
               ;; and the places to write it instead
               (re-find #"pj/facet pose" m)
               (re-find #"in the pose's mapping" m)))])

;; A second facet in the same direction on one pose is reported rather
;; than replacing the first. A facet written on a cell of a composite
;; overrides the facet the cell inherits, by the scope rules.

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/facet :species)
      (pj/facet :sepal-length))
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (and (string? m) (re-find #"already facets by" m)))])

;; The map given to `pj/facet` says where the panels come from, and
;; takes no other key. A plot option written inside it is reported, and
;; the message says to write the option in a map of its own, after the
;; first:

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/facet {:col :species :share-scales #{:x}}))
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (and (string? m)
               (re-find #"unexpected key\(s\): \[:share-scales\]" m)
               (re-find #"in a map of its own after this one" m)))])

;; A facet by one column draws its panels in one row, or in one column,
;; and does not wrap them onto further rows. Where the column holds so
;; many values that each panel would be narrower than the smallest
;; panel size, the error names the column and the
;; number of its values, and suggests arranging one pose per value with
;; `pj/arrange`, which does wrap:

(try
  (-> (rdatasets/ggplot2-mpg)
      (pj/lay-point :displ :hwy)
      (pj/facet :model)
      pj/plan)
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (and (string? m)
               (re-find #"a facet by :model" m)
               (re-find #"does not wrap its panels" m)
               (re-find #"pj/arrange poses" m)))])

;; ## Facet or Arrange
;;
;; `pj/facet` and `pj/arrange` both draw several panels, and many plots
;; can be written with either call. Which call fits depends on where the
;; panels come from. A facet generates its panels from one pose: a panel
;; for each category of a column, or a panel for each column the facet
;; names, all drawn from the same data with the same layers. An
;; arrangement lists its cells, and each cell is a pose of its own, with
;; its own mapping and layers. The cells read the data the arrangement
;; is given, or each holds data of its own.
;;
;; Where both calls can draw a plot, they agree on what its panels show.
;; Here `:hwy` and `:cty` are drawn against `:displ`, one panel above the
;; other, first by a facet over the two columns:

(-> (rdatasets/ggplot2-mpg)
    (pj/pose {:x :displ})
    pj/lay-point
    (pj/facet {:y [:hwy :cty]}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plan (pj/plan v)
          x-domains (for [sp (:sub-plots plan)
                          panel (:panels (:plan sp))]
                      (:x-domain panel))]
      (and (= 2 (:panels s))
           (= 468 (:points s))
           (apply = x-domains)
           ;; The shared :x axis is titled once, under the lower panel.
           (= 1 (count (filter #{"displ"} (:texts s)))))))])

;; and then by an arrangement of two mappings, each naming a pair of
;; columns, in one column:

(-> (rdatasets/ggplot2-mpg)
    (pj/arrange [{:x :displ :y :hwy} {:x :displ :y :cty}] {:cols 1})
    pj/lay-point)

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          domains (fn [pose]
                    (vec (for [sp (:sub-plots (pj/plan pose))
                               panel (:panels (:plan sp))]
                           [(:x-domain panel) (:y-domain panel)])))
          faceted (-> (rdatasets/ggplot2-mpg)
                      (pj/pose {:x :displ})
                      pj/lay-point
                      (pj/facet {:y [:hwy :cty]}))]
      (and (= 2 (:panels s))
           (= 468 (:points s))
           ;; The same :x and :y domains, panel by panel, as the facet.
           (= (domains faceted) (domains v))
           ;; The shared :x axis is titled once, as in the facet.
           (= 1 (count (filter #{"displ"} (:texts s)))))))])

;; The two cells split the data of the arrangement and both draw
;; `:displ`, so they share the `:x` axis, and the axis is titled once,
;; under the lower cell, as in the facet.
;;
;; Cells that each hold data of their own share no axis. Here three cells
;; each hold the rows of one kind of drive, and each `:y` axis covers
;; only its own cell's rows:

(pj/arrange (vec (for [drive ["4" "f" "r"]]
                   (-> (rdatasets/ggplot2-mpg)
                       (tc/select-rows #(= drive (:drv %)))
                       (pj/lay-point :displ :hwy)
                       (pj/options {:title drive})))))

(kind/test-last
 [(fn [v]
    (let [y-domains (for [sp (:sub-plots (pj/plan v))
                          panel (:panels (:plan sp))]
                      (:y-domain panel))]
      (and (= 3 (count y-domains))
           (= 3 (count (distinct y-domains))))))])

;; A facet over `:drv` divides one dataset, so its three panels share
;; both axes:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet :drv))

(kind/test-last
 [(fn [v]
    (let [panels (:panels (pj/plan v))]
      (and (= 3 (count panels))
           (apply = (map :y-domain panels))
           (apply = (map :x-domain panels)))))])

;; A facet over categories is itself a shorthand for an arrangement:
;; one cell per category, each holding the whole data and a `:where`,
;; the row filter that keeps the rows of that category. Written out by
;; hand, the cells draw the same plot as the facet:

(pj/arrange (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
            [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}])

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> (rdatasets/ggplot2-mpg)
                          (pj/lay-point :displ :hwy)
                          (pj/facet :drv)))))])

;; The cells are parts of the arrangement's data, so they share both
;; axes, as the panels of the facet do -- the cells holding data of
;; their own, above, share none.
;;
;; `{:share-scales #{:y}}` in the arrangement's options gives the three
;; cells one `:y` axis, as the Shared Scales section of the Composition
;; chapter shows.

(kind/test-last
 [(fn [_]
    (let [arranged (pj/arrange (vec (for [drive ["4" "f" "r"]]
                                      (-> (rdatasets/ggplot2-mpg)
                                          (tc/select-rows #(= drive (:drv %)))
                                          (pj/lay-point :displ :hwy))))
                               {:share-scales #{:y}})]
      (apply = (for [sp (:sub-plots (pj/plan arranged))
                     panel (:panels (:plan sp))]
                 (:y-domain panel)))))])

;; So write `pj/facet` when the panels are parts of one plot: one
;; dataset and the same layers, divided by the categories of a column or
;; across columns. A facet also reaches every cell of an arrangement it
;; is written on, as "A composite can be faceted" above shows. Write
;; `pj/arrange` when the cells are different plots -- different data,
;; different layers, a dashboard -- or when the layout needs what only a
;; list of cells can say: a column count, an empty cell written `nil`,
;; rows of different lengths, or one arrangement placed inside another.

;; ## The Order of the Calls
;;
;; `pj/facet` may be written anywhere in a pipeline. `pj/options`,
;; `pj/scale` and `pj/coord` written before the facet give the pose they
;; give written after it, and a layer added before or after the facet
;; draws the same plot. These two pipelines write the same calls in
;; different orders:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet :drv)
    (pj/scale :y {:domain [0 50]})
    pj/lay-smooth)

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    pj/lay-smooth
    (pj/scale :y {:domain [0 50]})
    (pj/facet :drv))

(kind/test-last
 [(fn [v]
    (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
      (and (= (pj/plot v)
              (pj/plot (-> p
                           (pj/facet :drv)
                           (pj/scale :y {:domain [0 50]})
                           pj/lay-smooth)))
           (= (-> p (pj/facet :drv) (pj/options {:title "T"}))
              (-> p (pj/options {:title "T"}) (pj/facet :drv)))
           (= (-> p (pj/facet :drv) (pj/coord :flip))
              (-> p (pj/coord :flip) (pj/facet :drv))))))])

;; The same holds under a facet over columns, where a layer added before
;; or after the facet gives an equal pose.

(kind/test-last
 [(fn [_]
    (= (-> (rdatasets/ggplot2-mpg) (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]}))
       (-> (rdatasets/ggplot2-mpg) (pj/pose {:x :displ}) (pj/facet {:y [:hwy :cty]}) pj/lay-point)))])

;; ## Coming from ggplot2
;;
;; A reader who knows ggplot2 looks for its faceting functions by name.
;; Each has a Plotje spelling, or none:
;;
;; | ggplot2 | Plotje |
;; |:--|:--|
;; | `facet_wrap(~ drv)` | `(pj/facet pose :drv)` draws the panels in one row and does not wrap them; `pj/arrange` with `{:cols n}` wraps poses into rows |
;; | `facet_grid(cyl ~ drv)` | `(pj/facet pose {:col :drv :row :cyl})` |
;; | `facet_grid(scales = "free")` | `{:share-scales #{}}` in the options map after the facet |
;; | `facet_grid(scales = "free_y")` | `{:share-scales #{:x}}`, which fits `:y` to each panel rather than to each row |
;; | `GGally::ggpairs(data, columns = cols)` | `(pj/facet data {:col {:x cols} :row {:y cols}})`, the same layer above and below the diagonal |
;; | `facet_grid(labeller = label_both)` | `{:strip-labels :both}` in the options map after the facet |
;; | `facet_grid(margins = TRUE)` | no option; stack the data with a copy labelled `"(all)"`, as below |
;;
;; Panels follow the order the data holds the values, where ggplot2
;; sorts them; `(pj/scale pose :col {:domain [...]})` gives another
;; order, as the section on ordering the panels shows.
;;
;; ### A panel for all rows
;;
;; ggplot2's `margins = TRUE` adds a panel that draws every category
;; together, so each panel can be compared with the whole. The same
;; panel comes from the data: stack the dataset with a copy of itself
;; whose facet column reads `"(all)"`, and facet the stacked rows. The
;; copy is the last value the data holds, so its panel comes last:

(let [mpg (rdatasets/ggplot2-mpg)]
  (-> (tc/concat mpg (tc/add-column mpg :drv "(all)"))
      (pj/lay-point :displ :hwy)
      (pj/facet :drv)))

(kind/test-last [(fn [v] (let [mpg (rdatasets/ggplot2-mpg)
                               s (pj/svg-summary v)]
                           (and (= (inc (count (distinct (mpg :drv)))) (:panels s))
                                (= (* 2 (tc/row-count mpg)) (:points s))
                                (some #{"(all)"} (:texts s)))))])

;; Each row is drawn twice, in its own panel and in the `"(all)"` panel.
;; For a grid, stack a copy for each direction and one more for the
;; corner, where both columns read `"(all)"`.

;; ## Examples

;; Faceted plots of other kinds, each built from the ideas above.

;; ### Faceted Histogram

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length {:color :species})
    (pj/facet :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (pos? (:polygons s)))))])

;; ### Faceted Regression
;;
;; Layers compose with faceting -- scatter plus regression per panel:

(-> (rdatasets/reshape2-tips)
    (pj/pose :total-bill :tip {:color :sex})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model})
    (pj/facet {:col :smoker :row :sex}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:panels s))
                                (= 244 (:points s))
                                (= 4 (:lines s)))))])

;; ### Faceted Bar Chart

;; Five bars total across the three island panels: Adelie appears on
;; every island, while Gentoo lives only on Biscoe and Chinstrap only
;; on Dream. Empty species-island combinations produce no bar.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :species {:color :species})
    (pj/facet :island)
    (pj/options {:width 800}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 5 (:polygons s)))))])

;; ### Labels and Faceting
;;
;; `pj/options` works with faceted plots:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species)
    (pj/options {:title "Iris by Species"
                 :x-label "Sepal Length (cm)" :y-label "Sepal Width (cm)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s))
                                (some #{"Iris by Species"} (:texts s))
                                (some #{"Sepal Length (cm)"} (:texts s)))))])

;; ## See Also
;;
;; - [**Specifying Aesthetics**](./plotje_book.specifying_aesthetics.html#what-a-distinction-is-put-to-work-as) -- the role of every aesthetic, `:col` and `:row` among them
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) -- the scope rules a panel aesthetic obeys
;; - [**Composition**](./plotje_book.composition.html) -- composite poses, shared scales, and arbitrary layouts
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- mapping scope as it flows through faceted panels

;; ## What's Next
;;
;; - [**Series**](./plotje_book.series.html) -- reading several columns by name rather than by value
;; - [**Troubleshooting**](./plotje_book.troubleshooting.html) -- common issues and how to fix them
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
