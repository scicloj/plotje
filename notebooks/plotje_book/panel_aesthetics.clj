;; # Panel Aesthetics
;;
;; [Faceting](./plotje_book.faceting.html) shows the pictures a facet
;; draws. This chapter is about what a facet *is*: `:col` and `:row`
;; are aesthetics, written in a mapping, and `pj/facet` is the call
;; that writes one. Everything else here follows from that -- a facet
;; reaches into a composite, a cell can override the facet it
;; inherits, and several columns under `:col` unite into one
;; distinction.

(ns plotje-book.panel-aesthetics
  (:require
   [scicloj.kindly.v4.kind :as kind]
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   [scicloj.plotje.api :as pj]
   [tablecloth.api :as tc]))

;; ## What a distinction is put to work as

;; A distinction is a set of things to tell apart, and it is made of
;; columns. Every aesthetic answers two questions about the
;; distinction it is given: where the marks go, and how a reader tells
;; them apart. The answer is the aesthetic's **role**, and there are
;; four. A role is not a category -- a category is a value a
;; categorical column holds, which is what a role is given.

(kind/table
 {:column-names [:aesthetic :role]
  :row-vectors (->> (pj/aesthetic-roles)
                    (sort-by (comp str key))
                    (mapv (fn [[k role]] [k role])))})

(kind/test-last
 [(fn [_] (= #{:positional :appearance :grouping :panel}
             (set (vals (pj/aesthetic-roles)))))])

;; `:color` puts a distinction in one place and tells the marks apart
;; by colour, under a legend. `:group` puts it in one place and tells
;; them apart not at all. The panel aesthetics give each value a panel
;; of its own, told apart by a strip label.
;;
;; They are documented where they are written, rather than beside the
;; layer options, because no `pj/lay-*` call accepts one.

(kind/table
 {:column-names [:aesthetic :what-it-reads]
  :row-vectors (mapv (fn [[k doc]] [k doc])
                     (sort-by (comp str key) pj/panel-aesthetic-docs))})

(kind/test-last
 [(fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))])

;; ## Faceting is a mapping

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
 [(fn [fr] (= 4 (:panels (pj/svg-summary fr))))])

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

;; ## A vector is a compound key

;; Under a categorical aesthetic, several columns in a vector are one
;; distinction made of those columns united -- a compound key. That is
;; what `{:color [:a :b]}` and `{:group [:a :b]}` mean, and the panel
;; aesthetics read a vector the same way.
;;
;; The table below holds three of the four combinations of `:part` and
;; `:dimension`: there is no petal width row.

(def measures
  (tc/dataset {:part      ["sepal" "sepal" "sepal" "sepal" "petal" "petal"]
               :dimension ["length" "length" "width" "width" "length" "length"]
               :t         [1 2 1 2 1 2]
               :v         [1.0 2.0 1.5 2.5 2.0 3.0]}))

measures

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

;; ## Written out in full

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

;; ## Where it is refused

;; A facet divides every layer of the pose alike, so a panel aesthetic
;; belongs on the pose. Written in a `pj/lay-*` options map it is
;; reported, and the report names the call to use instead.

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:col :species})
      pj/plan)
  (catch Exception e (ex-message e)))

(kind/test-last
 [(fn [m] (and (string? m) (re-find #"Faceting is plot-level" m)))])

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

;; ## See Also
;;
;; - [**Faceting**](./plotje_book.faceting.html) -- the pictures a facet draws, and the options that shape them
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) -- the scope rules a panel aesthetic obeys
;; - [**Composition**](./plotje_book.composition.html) -- composite poses, shared scales, and arbitrary layouts

;; ## What's Next
;;
;; - [**Series**](./plotje_book.series.html) -- reading several columns by name rather than by value
;; - [**Troubleshooting**](./plotje_book.troubleshooting.html) -- common issues and how to fix them
