;; # ggplot2 Equivalents
;;
;; A reader who knows [ggplot2](https://ggplot2.tidyverse.org/) arrives
;; with a plot already in mind and a function name for each part of
;; it. The question is what that function is called in Plotje, and the
;; answers are taught across several chapters, each in the context of
;; its own topic. This chapter collects them: each row gives a ggplot2
;; spelling, the Plotje spelling, and the section of the book that
;; teaches it. Rows with no Plotje spelling are listed at the end.
;;
;; The examples use the iris and mpg datasets from the
;; [RDatasets collection](./plotje_book.datasets.html#the-rdatasets-collection).

(ns plotje-book.ggplot2-equivalents
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Mappings and layers
;;
;; | ggplot2 | Plotje | Taught in |
;; |:--|:--|:--|
;; | `ggplot(df, aes(x = a, y = b))` | `(pj/pose df :a :b)`, or `(pj/pose df {:x :a :y :b})` | [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers) |
;; | `+ geom_point()` | `pj/lay-point` | [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers) |
;; | `+ geom_smooth(method = "lm")` | `(pj/lay-smooth {:stat :linear-model})` | [Relationships](./plotje_book.relationships.html#linear-regression) |
;; | `+ geom_point(data = df2)` | `(pj/lay-point {:data df2})` | [Core Concepts](./plotje_book.core_concepts.html#layer-level-data) |
;; | `+ geom_point(aes(x = c, y = d))`, a layer on other columns | `(pj/lay-point :c :d)`, drawn on the same panel, each axis titled by both columns | [Panels](./plotje_book.panels.html#the-panel-of-a-leaf-pose) |
;; | `pivot_longer()` then `aes(color = name)` | `(pj/lay-line df :x [:a :b])`, a series | [Series](./plotje_book.series.html#a-series-is-a-layer-per-column) |
;;
;; Where the layers of a panel draw different columns and no layer is
;; colored by a column, Plotje gives each layer a color and a legend
;; entry naming its columns; ggplot2 draws both layers in one color with
;; no legend.
;;
;; A pose with a point layer and a linear-model smooth, the Plotje
;; spelling of `ggplot(iris, aes(Sepal.Length, Sepal.Width)) +
;; geom_point() + geom_smooth(method = "lm")`:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 150 (:points s))
           (= 1 (:lines s))
           ;; the two spellings of the pose's mapping build the same pose
           (= (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
              (pj/pose (rdatasets/datasets-iris) {:x :sepal-length :y :sepal-width})))))])

;; Under a facet, ggplot2 divides a layer's `data =` among the panels by
;; the facet column, as it divides the plot's data. Plotje divides the
;; pose's data only: a layer that carries `:data` of its own is drawn
;; whole in every panel. Here
;; the regression fitted to the setosa rows is drawn in each of the
;; three species panels, where ggplot2 4.0.0 draws it in the setosa
;; panel alone:

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model
                    :data (tc/select-rows (rdatasets/datasets-iris)
                                          #(= "setosa" (:species %)))})
    (pj/facet :species))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 3 (:panels s))
           (= 150 (:points s))
           ;; the setosa line, once in each panel
           (= 3 (:lines s)))))])

;; For layer data that differs from panel to panel, build the panels
;; with `pj/arrange`, as
;; [Known Limitations](./plotje_book.known_limitations.html#ggplot2-features-not-yet-implemented)
;; describes.

;; ## Scales
;;
;; | ggplot2 | Plotje | Taught in |
;; |:--|:--|:--|
;; | `+ scale_x_log10()` | `(pj/scale :x :log)` | [Core Concepts](./plotje_book.core_concepts.html#coordinates-and-scales), [Scales](./plotje_book.scales.html#type) |
;; | `+ scale_x_reverse()` | `(pj/scale :x {:domain [high low]})`, a `:domain` written high to low | [Scales](./plotje_book.scales.html#making-an-axis-reach-a-value) |
;; | `+ expand_limits(y = 0)` | `(pj/scale :y {:include 0})` | [Scales](./plotje_book.scales.html#making-an-axis-reach-a-value) |
;; | `+ scale_color_manual(values = ...)` | `(pj/scale :color {:values [...]})` | [Scales](./plotje_book.scales.html#the-colors-a-scale-spans) |
;;
;; A log axis and a reversed axis on one plot -- the population on a
;; log scale, the area written from high to low:

(-> {:population [1000 5000 50000 200000 1000000 5000000]
     :area [2 8 30 120 500 2100]}
    (pj/lay-point :population :area)
    (pj/scale :x :log)
    (pj/scale :y {:domain [2500 0]}))

(kind/test-last
 [(fn [v]
    (let [panel (-> v
                    pj/plan
                    :panels
                    first)]
      (and (= 6 (:points (pj/svg-summary v)))
           (= :log (-> panel
                       :x-scale
                       :type))
           ;; the domain is kept in the order written
           (= [2500.0 0.0] (mapv double (:y-domain panel))))))])

;; `:include` makes an axis reach a value the data does not:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/scale :y {:include 0}))

(kind/test-last
 [(fn [v]
    (and (= 0.0 (-> v
                    pj/plan
                    :panels
                    first
                    :y-domain
                    first))
         ;; the scale_color_manual row: three written colors are drawn
         (= #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
            (disj (:colors (pj/svg-summary
                            (-> (rdatasets/datasets-iris)
                                (pj/lay-point :sepal-length :sepal-width {:color :species})
                                (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]}))))
                  "none"))))])

;; ## Coordinates
;;
;; | ggplot2 | Plotje | Taught in |
;; |:--|:--|:--|
;; | `+ coord_flip()` | `(pj/coord :flip)` | [Core Concepts](./plotje_book.core_concepts.html#coordinates-and-scales) |
;; | `+ coord_fixed()` | `(pj/coord :fixed)` | [Core Concepts](./plotje_book.core_concepts.html#coordinates-and-scales) |
;; | `+ coord_polar()` | `(pj/coord :polar)` | [Polar](./plotje_book.polar.html) |
;; | `+ coord_cartesian(ylim = c(3, 3.5))` | `(pj/scale :y {:domain [3 3.5]})` | [Scales](./plotje_book.scales.html#on-an-axis-a-view-window) |
;;
;; ggplot2 has two ways to narrow an axis. `coord_cartesian(ylim =)`
;; narrows the view and keeps every row, while the scale limits
;; `scale_y_continuous(limits =)` drop the rows outside them -- measured
;; with ggplot2 4.0.0 on iris narrowed to sepal widths from 3 to 3.5,
;; `coord_cartesian` keeps all 150 rows and the scale limits keep 74. A
;; `:domain` on an axis behaves as `coord_cartesian` does, though it is
;; written on the scale. All 150 rows are drawn and clipped at the
;; panel edge:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/scale :y {:domain [3 3.5]}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 150 (:points s))
           (= 1 (:clips s))
           ;; the other two coordinates in the table draw as well
           (= 150 (:points (pj/svg-summary (pj/coord v :flip))))
           (= 150 (:points (pj/svg-summary (pj/coord v :fixed)))))))])

;; ## Facets and arrangements
;;
;; | ggplot2 | Plotje | Taught in |
;; |:--|:--|:--|
;; | `+ facet_wrap(~ drv)` | `(pj/facet pose :drv)`, which draws its panels in one row | [Faceting](./plotje_book.faceting.html#coming-from-ggplot2) |
;; | `+ facet_wrap(~ class, ncol = 4)` | `(pj/facet pose :class {:cols 4})` | [Wrapping the panels into rows](./plotje_book.faceting.html#wrapping-the-panels-into-rows) |
;; | `+ facet_grid(cyl ~ drv)` | `(pj/facet pose {:col :drv :row :cyl})` | [Faceting](./plotje_book.faceting.html#coming-from-ggplot2) |
;; | `pivot_longer()` then `facet_wrap(~ name, scales = "free_y")` | `(pj/facet pose {:y [:a :b]})` | [A Panel per Column](./plotje_book.faceting.html#a-panel-per-column) |
;; | patchwork's <code>p1 &#124; p2</code> | `(pj/arrange [p1 p2])` | [Composition](./plotje_book.composition.html#side-by-side-via-pjarrange) |
;; | `GGally::ggpairs(df, columns = cols)` | `(pj/matrix df cols)` | [Relationships](./plotje_book.relationships.html#scatter-plot-matrix-splom) |
;;
;; Faceting's own table covers the faceting options -- free scales,
;; strip labels, margins -- in more detail.
;;
;; A grid of categories, the Plotje spelling of `facet_grid(cyl ~ drv)`:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/facet {:col :drv :row :cyl}))

(kind/test-last
 [(fn [v]
    (let [mpg (rdatasets/ggplot2-mpg)]
      (and (= (* (count (distinct (mpg :drv)))
                 (count (distinct (mpg :cyl))))
              (:panels (pj/svg-summary v)))
           ;; the arrangement and the matrix in the table draw too
           (= 2 (:panels (pj/svg-summary
                          (pj/arrange [(pj/lay-point mpg :displ :hwy)
                                       (pj/lay-point mpg :displ :cty)]))))
           (= 4 (:panels (pj/svg-summary
                          (pj/matrix (rdatasets/datasets-iris)
                                     [:sepal-length :petal-length])))))))])

;; ## Labels and options
;;
;; | ggplot2 | Plotje | Taught in |
;; |:--|:--|:--|
;; | `+ labs(title = ..., x = ..., y = ...)` | `(pj/options {:title ... :x-label ... :y-label ...})` | [Customization](./plotje_book.customization.html#titles-and-labels) |
;; | `+ labs(subtitle = ..., caption = ...)` | `(pj/options {:subtitle ... :caption ...})` | [Customization](./plotje_book.customization.html#titles-and-labels) |
;; | `+ labs(color = ...)` | `(pj/options {:color-label ...})` | [Customization](./plotje_book.customization.html#titles-and-labels) |
;; | `theme_set(...)` | `pj/set-config!` | [Configuration](./plotje_book.configuration.html#global-overrides-with-set-config) |
;;
;; [Options and Scopes](./plotje_book.options_and_scopes.html#coming-from-ggplot2)
;; says which of Plotje's categories -- mapping, layer, plot option,
;; configuration -- each ggplot2 construct falls in.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Iris Sepal Measurements"
                 :x-label "Length (cm)"
                 :y-label "Width (cm)"
                 :color-label "Species"}))

(kind/test-last
 [(fn [v]
    (let [texts (set (:texts (pj/svg-summary v)))]
      (every? texts ["Iris Sepal Measurements" "Length (cm)" "Width (cm)" "Species"])))])

;; ## Not available
;;
;; Some ggplot2 features have no Plotje spelling in this release:
;;
;; | ggplot2 | Plotje | Taught in |
;; |:--|:--|:--|
;; | `after_stat(count)` inside `aes()` | not available; aggregate the data first and map the computed column | [Known Limitations](./plotje_book.known_limitations.html#ggplot2-features-not-yet-implemented) |
;; | `aes(linetype = ...)` | not available; `{:stroke-dash [5 5]}` on a line layer dashes the whole layer, and no column varies the dashes | [Known Limitations](./plotje_book.known_limitations.html#ggplot2-features-not-yet-implemented) |
;; | `scale_x_sqrt()` | not available; the axis types are `:linear`, `:log` and `:categorical` | [Known Limitations](./plotje_book.known_limitations.html#ggplot2-features-not-yet-implemented) |
;; | `position_jitterdodge()` | not available | |
;; | `facet_grid(scales = "free_y")`, one `:y` per row of panels | not available; `{:share-scales #{:x}}` fits `:y` to each panel instead | [Faceting](./plotje_book.faceting.html#coming-from-ggplot2) |
;;
;; A square-root axis reports an error that lists the types an axis
;; accepts:

(try
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/scale :x :sqrt)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m]
    (and (re-find #"Supported for :x: \[:categorical :linear :log\]" m)
         ;; the linetype row: a written dash draws one dashed line
         (= 1 (:dashed-lines (pj/svg-summary
                              (pj/lay-line {:quarter [1 2 3 4] :revenue [3 5 4 6]}
                                           :quarter :revenue {:stroke-dash [5 5]}))))))])

;; ## See Also
;;
;; - [Options and Scopes](./plotje_book.options_and_scopes.html#coming-from-ggplot2) --
;;   which category each ggplot2 construct falls in.
;; - [Faceting](./plotje_book.faceting.html#coming-from-ggplot2) --
;;   the faceting functions and options, one row each.
;; - [Specifying Aesthetics](./plotje_book.specifying_aesthetics.html#appendix-source-and-scale-in-ggplot2) --
;;   `aes()` inside and outside, and the identity scales.
;; - [Known Limitations](./plotje_book.known_limitations.html) --
;;   every gap in the current release.
