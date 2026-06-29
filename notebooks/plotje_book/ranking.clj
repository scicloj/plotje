;; # Ranking
;;
;; Bar charts and their variants -- comparing quantities across categories.

(ns plotje-book.ranking
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

(def sales {:product [:widget :gadget :gizmo :doohickey]
            :revenue [120 340 210 95]})

;; ## Bar Chart

;; Count occurrences of a categorical column.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Colored Bar Chart

;; Grouped (dodged) bars -- count by day, colored by smoking status.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :smoker}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Stacked Bar Chart

;; Same data, stacked instead of dodged.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:position :stack :color :smoker}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Stacked Bar (Proportions)

;; 100% stacked bars -- shows proportions instead of counts.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:position :fill :color :species}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          panel (first (:panels (pj/plan
                                 (-> (rdatasets/palmerpenguins-penguins)
                                     (pj/lay-bar :island
                                                 {:position :fill
                                                  :color :species})))))
          [y0 y1] (:y-domain panel)]
      (and (= 1 (:panels s))
           (pos? (:polygons s))
           ;; The y-domain is normalized to [0.0, 1.0] -- proportions.
           (== 0.0 y0)
           (== 1.0 y1))))])

;; ## Horizontal Bar Chart

;; Flip the bar chart for horizontal orientation.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/coord :flip))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plan (pj/plan (-> (rdatasets/datasets-iris)
                            (pj/lay-bar :species)
                            (pj/coord :flip)))
          panel (first (:panels plan))
          iris-order (vec (distinct ((rdatasets/datasets-iris) :species)))]
      (and (= 1 (:panels s))
           (pos? (:polygons s))
           ;; Categories on the y-axis follow the data order, not
           ;; alphabetical order.
           (= iris-order (:values (:y-ticks panel))))))])

;; `(pj/coord :flip)` draws categories **bottom-to-top in data
;; order**, matching ggplot2's `coord_flip()`. For a ranking chart
;; where the biggest value should appear at the top, sort the data
;; ascending before plotting, e.g.
;; `(tc/order-by data [:value] [:asc])`.

;; ## Horizontal Colored Bars

;; Colored bars, flipped.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :time})
    (pj/coord :flip))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Value Bar

;; Pre-computed y values (no counting).

(-> sales
    (pj/lay-bar :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:polygons s)))))])

;; ## Value Bar (Horizontal)

;; Put the category on y for horizontal value bars -- no coord flip needed.

(-> sales
    (pj/lay-bar :revenue :product))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:polygons s)))))])

;; ## [Lollipop](https://en.wikipedia.org/wiki/Lollipop_chart)

;; Stem + dot -- a lighter alternative to bar charts.

(-> sales
    (pj/lay-lollipop :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 4 (:lines s)))))])

;; ## Lollipop (Horizontal)

;; Flipped for horizontal orientation.

(-> sales
    (pj/lay-lollipop :product :revenue)
    (pj/coord :flip))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 4 (:lines s)))))])

;; ## Lollipop with `:color`

;; Map a categorical column to `:color` to distinguish groups
;; visually -- here, products grouped by region.

(-> {:product ["A" "B" "C" "D" "E" "F"]
     :revenue [120 95 150 80 200 110]
     :region  ["North" "South" "North" "South" "North" "South"]}
    (pj/lay-lollipop :product :revenue {:color :region}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               fills (disj (:colors s) "none")]
                           (and (= 6 (:points s))
                                ;; one distinct interior color per region
                                (= 2 (count fills)))))])

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- mappings and aesthetic vocabulary
;; - [**Distributions**](./plotje_book.distributions.html) -- when comparing categories also means comparing their distributions

;; ## What's Next
;;
;; - [**Change Over Time**](./plotje_book.change_over_time.html) -- line charts, step functions, and stacked areas
;; - [**Configuration**](./plotje_book.configuration.html) -- control dimensions, palettes, and themes
