;; # Faceting
;;
;; Faceting splits data into subsets and draws each in its own panel,
;; making it easy to compare patterns across groups.

(ns plotje-book.faceting
  (:require
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

;; ## Vertical Facet
;;
;; Pass `:row` as the direction for a vertical column of panels:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/facet :species :row))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 150 (:points s)))))])

;; ## Facet Grid
;;
;; `pj/facet-grid` splits by two columns -- one for rows, one for columns:

(-> (rdatasets/reshape2-tips)
    (pj/lay-point :total-bill :tip {:color :sex})
    (pj/facet-grid :smoker :sex))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:panels s))
                                (= 244 (:points s)))))])

;; Row labels appear on the right, column labels on top.

;; ## Faceted Histogram

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length {:color :species})
    (pj/facet :species))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Faceted Regression
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

;; ## Appearance Channels Across Panels
;;
;; `:scales` governs the axes. An appearance channel is a separate
;; question, and its answer is one to watch: a `:size` or `:alpha`
;; column is scaled against the panel it is drawn in, while its legend
;; is built from the whole plot. Two panels holding different ranges
;; therefore draw the same radii, and the legend explains neither.
;;
;; Below, the left panel's values run 1 to 3 and the right panel's 4 to
;; 10, and the two panels draw the same three sizes:

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
    ;; Six marks, three distinct radii -- each panel spans the full
    ;; range on its own.
    (= 3 (count (:sizes (pj/svg-summary v)))))])

;; Giving the scale an explicit `:domain` holds every panel to one
;; reading, so a mark's size means the same thing in both:

(-> per-panel
    (pj/lay-point :x :y {:size :n})
    (pj/facet :g)
    (pj/scale :size {:domain [1 10]}))

(kind/test-last
 [(fn [v] (= 5 (count (:sizes (pj/svg-summary v)))))])

;; A related multi-panel layout, the **scatter plot matrix (SPLOM)**,
;; uses `pj/cross` rather than `pj/facet` -- the panels show all
;; pairs of variables instead of one variable split across panels.
;; See the [Relationships](./plotje_book.relationships.html#scatter-plot-matrix-splom) chapter
;; for the canonical SPLOM example.

;; ## Comparing Multiple Columns
;;
;; Pass a vector of column names to create one panel per column:

(pj/lay-histogram (rdatasets/datasets-iris) [:sepal-length :sepal-width :petal-length] {:color :species})

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Faceted Bar Chart

;; Five bars total across the three island panels: Adelie appears on
;; every island, while Gentoo lives only on Biscoe and Chinstrap only
;; on Dream. Empty species-island combinations produce no bar.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :species {:color :species})
    (pj/facet :island))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:panels s))
                                (= 5 (:polygons s)))))])

;; ## Labels and Faceting
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
;; - [**Composition**](./plotje_book.composition.html) -- composite poses, shared scales, and arbitrary layouts
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- mapping scope as it flows through faceted panels

;; ## What's Next
;;
;; - [**Troubleshooting**](./plotje_book.troubleshooting.html) -- common issues and how to fix them
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
