;; # Faceting
;;
;; Faceting splits data into subsets and draws each in its own panel,
;; making it easy to compare patterns across groups.
;;
;; A facet is a mapping: `:col` and `:row` are aesthetics, and
;; `pj/facet` is the call that writes one. Everything else follows
;; from that -- a facet reaches into a composite, a cell can override
;; the facet it inherits, and several columns under `:col` unite into
;; one distinction. The sections below teach those ideas, and the
;; Examples section at the end shows faceted plots of several kinds.

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

;; ## Facet Wrap
;;
;; `pj/facet` splits a pose into panels by one categorical column.
;; The default direction is `:col` -- a horizontal row of panels:

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
           ;; Default :scales is :shared -- every panel shares both
           ;; x-domain and y-domain.
           (apply = x-doms)
           (apply = y-doms))))])

;; Each species gets its own panel with a strip label on top.
;; Scales are shared by default -- all panels use the same x and y range,
;; making direct comparison easy.

;; ## Vertical Facet and Facet Grid
;;
;; Pass `:row` as the direction for a vertical column of panels:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species :row))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s)))))])

;; `pj/facet-grid` splits by two columns -- one for rows, one for columns:

(-> (rdatasets/reshape2-tips)
    (pj/lay-point :total-bill :tip {:color :sex})
    (pj/facet-grid :smoker :sex))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:panels s))
                                (= 244 (:points s)))))])

;; Row labels appear on the right, column labels on top.

;; ## Faceting is a mapping

;; The panel aesthetics `:col` and `:row` give each value of a column
;; a panel of its own, told apart by a strip label -- where `:color`
;; tells the marks apart by colour on one panel. They are documented
;; here, where they are written, rather than beside the layer options,
;; because no `pj/lay-*` call accepts one.

(kind/table
 {:column-names [:aesthetic :what-it-reads]
  :row-vectors (mapv (fn [[k doc]] [k doc])
                     (sort-by (comp str key) pj/panel-aesthetic-docs))})

(kind/test-last
 [(fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))])

;; `pj/facet` writes `:col` into the pose's mapping, beside `:x` and
;; `:y`. Here is the plot, and then the pose that draws it.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species))

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species)
    kind/pprint)

(kind/test-last
 [(fn [fr] (= :species (get-in fr [:mapping :col])))])

;; `(pj/facet my-pose col :row)` writes `:row` instead, and
;; `pj/facet-grid` writes both.

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet-grid :drv :cyl)
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

;; ### A facet multiplies the panels a pose already draws

;; A pose whose layers name different columns draws a panel for each
;; place. Faceting such a pose draws every one of those panels once
;; per value the column holds: the facet is the outer division.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width)
    (pj/facet :species)
    (pj/options {:width 900 :height 480}))

(kind/test-last
 [(fn [fr] (= 6 (:panels (pj/svg-summary fr))))])

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

;; `pj/facet-grid` asks a different question. It crosses two
;; distinctions and fills the rectangle, so the combination the data
;; does not hold is drawn as an empty panel.

(-> measures
    (pj/lay-point :t :v)
    (pj/facet-grid :part :dimension))

(kind/test-last
 [(fn [fr] (= 4 (:panels (pj/svg-summary fr))))])

;; ## Free Scales (Independent Axis Ranges)
;;
;; By default all panels share the same axis ranges. Use the `:scales`
;; option to let axes vary per panel.
;;
;; Shared (default) -- all panels carry the same x and y ranges:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species)
    (pj/options {:scales :shared}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          doms (mapv :x-domain
                     (:panels (pj/plan
                               (-> (rdatasets/datasets-iris)
                                   (pj/lay-point :sepal-length :sepal-width)
                                   (pj/facet :species)))))]
      (and (= 3 (:panels s))
           (= 150 (:points s))
           (apply = doms))))])

;; Free y -- each panel has its own y-range:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species)
    (pj/options {:scales :free-y}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          doms (mapv :y-domain
                     (:panels (pj/plan
                               (-> (rdatasets/datasets-iris)
                                   (pj/lay-point :sepal-length :sepal-width)
                                   (pj/facet :species)
                                   (pj/options {:scales :free-y})))))]
      (and (= 3 (:panels s))
           (= 3 (count (distinct doms))))))])

;; Other values: `:free-x` (x per-panel, y shared), `:free`
;; (both axes per-panel).

;; ## Appearance Aesthetics Across Panels
;;
;; `:scales` frees the axes, and the axes alone. A `:size` or `:alpha`
;; column is read against every value the plot holds, whichever panel a
;; mark sits in, so one value is one size everywhere and the legend
;; explains every panel at once. Comparing panels is what faceting is
;; for, and a size that meant something different in each would take
;; that away.
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

;; A `:domain` sets that one extent rather than working around it. Given
;; more room than the data needs, no mark reaches either end of the
;; range:

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

;; A related multi-panel layout, the **scatter plot matrix (SPLOM)**,
;; uses `pj/cross` rather than `pj/facet` -- the panels show all
;; pairs of variables instead of one variable split across panels.
;; See the [Relationships](./plotje_book.relationships.html#scatter-plot-matrix-splom) chapter
;; for the canonical SPLOM example.

;; ## Where it is refused

;; A facet divides every layer of the pose alike, and a panel aesthetic
;; is read from a pose's mapping and not from a layer's, so a panel
;; aesthetic belongs on the pose. Written in a `pj/lay-*` options map
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
               (re-find #"pj/facet-grid" m)
               (re-find #"in the pose's mapping" m)))])

;; Two facets in the same direction on one pose are two answers to one
;; question, and the second is reported rather than quietly replacing
;; the first. Overriding one further down a composite is a different
;; thing, and is what the scope rules are for.

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/facet :species)
      (pj/facet :sepal-length))
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (and (string? m) (re-find #"already facets by" m)))])

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
    (pj/facet-grid :smoker :sex))

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
    (pj/facet :island))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 5 (:polygons s)))))])

;; ### Comparing Multiple Columns
;;
;; Pass a vector of column names to create one panel per column:

(pj/lay-histogram (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length] {:color :species})

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (pos? (:polygons s)))))])

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
