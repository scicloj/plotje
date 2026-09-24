;; # Ranking
;;
;; Bar charts and their variants -- comparing quantities across categories.

(ns plotje-book.ranking
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

(def sales {:product [:widget :gadget :gizmo :doohickey]
            :revenue [120 340 210 95]})

;; ## Count bars
;;
;; `pj/lay-bar` with only a category column counts the rows in each
;; category -- the bar height is the number of occurrences.

;; ### Plain

;; Count occurrences of a categorical column.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ### Colored (dodged)

;; Grouped (dodged) bars -- count by day, colored by smoking status.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :smoker}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ### Stacked

;; Same data, stacked instead of dodged.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:position :stack :color :smoker}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ### Proportions (100% stacked)

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

;; ### Horizontal

;; Counting bars have no native horizontal form (the category would
;; need to be counted onto the y-axis), so flip the chart instead.

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

;; ### Horizontal, colored

;; Colored bars, flipped.

(-> (rdatasets/reshape2-tips)
    (pj/lay-bar :day {:color :time})
    (pj/coord :flip))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (pos? (:polygons s)))))])

;; ## Value bars
;;
;; Give `pj/lay-bar` a value column too and it uses those numbers
;; directly as the bar heights -- no counting.

;; ### Vertical

;; One bar per product; the bar height is the revenue.

(-> sales
    (pj/lay-bar :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:polygons s)))))])

;; ### Horizontal

;; Put the category on y -- no coord flip needed (unlike count bars).

(-> sales
    (pj/lay-bar :revenue :product))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:polygons s)))))])

;; ### Ranked
;;
;; A ranking chart is read down from the biggest, and the bars above
;; are in whatever order the data happened to hold. Plotje draws the
;; bands of a categorical y axis in data order, running bottom to top,
;; so sorting the rows ascending by the value puts the biggest bar at
;; the top. The same sort does the same thing under `(pj/coord :flip)`,
;; which is how the count-bar form gets there.

(def sales-ranked
  (-> sales
      tc/dataset
      (tc/order-by :revenue :asc)))

(-> sales-ranked
    (pj/lay-bar :revenue :product))

(kind/test-last
 [(fn [v]
    (let [panel (-> v pj/frames :panels first)
          bands (-> v pj/plan :panels first :y-domain vec)
          ;; Smaller drawing coordinates are higher on the page, so the
          ;; biggest seller having the smallest one is "at the top".
          height-of (fn [c] (second (pj/to-drawing panel 100 c)))]
      (and (= ["doohickey" "widget" "gizmo" "gadget"] bands)
           (= 4 (:polygons (pj/svg-summary v)))
           (apply > (mapv height-of bands))
           (= "gadget" (last bands))
           (= 340 (apply max (:revenue sales))))))])

;; ### Rotated labels

;; Horizontal bars are one way to fit long category names. To keep
;; the bars vertical instead, rotate the x-tick labels with
;; `:x-tick-angle` (see
;; [Customization](./plotje_book.customization.html#rotating-tick-labels)
;; for details, including `:x-tick-label-pad`):

(-> {:department ["Office Supplies" "Electronics" "Home Goods"
                  "Sporting Gear" "Garden Tools" "Toys"]
     :revenue [120 340 210 95 160 80]}
    (pj/lay-bar :department :revenue)
    (pj/options {:x-tick-angle -45}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 6 (:polygons s))
                                (.contains ^String (pr-str (pj/plot v)) "rotate(-45"))))])

;; ## Lollipop
;;
;; A stem and a dot in place of a bar -- a
;; [lollipop chart](https://en.wikipedia.org/wiki/Lollipop_chart) draws
;; the same comparison with less ink.

;; ### Vertical

(-> sales
    (pj/lay-lollipop :product :revenue))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 4 (:lines s)))))])

;; ### Horizontal

;; Flipped for horizontal orientation.

(-> sales
    (pj/lay-lollipop :product :revenue)
    (pj/coord :flip))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:points s))
                                (= 4 (:lines s)))))])

;; ### With a color column

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

;; ### Without the dot

;; `pj/lay-segment` draws the stem alone. A segment runs from each row's
;; `:x` and `:y` to its `:x-end` and `:y-end`; an end left out keeps the
;; start's value, so `{:y-end 0}` draws each stem from the value down
;; to the zero line:

(-> sales
    (pj/lay-segment :product :revenue {:y-end 0}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 4 (:lines s))
                                (zero? (:points s)))))])

;; The x axis can hold numbers as well as categories. One stem per
;; observation over its index is a stem plot, the shape of a
;; residual or Cook's distance diagnostic:

(-> {:index (range 1 41)
     :residual (map #(* (Math/sin %) (Math/exp (- (/ % 30.0)))) (range 1 41))}
    (pj/lay-segment :index :residual {:y-end 0})
    (pj/lay-rule-h {:y-intercept 0}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                ;; 40 stems and the zero line
                                (= 41 (:lines s)))))])

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- mappings and aesthetic vocabulary
;; - [**Distributions**](./plotje_book.distributions.html) -- when comparing categories also means comparing their distributions

;; ## What's Next
;;
;; - [**Change Over Time**](./plotje_book.change_over_time.html) -- line charts, step functions, and stacked areas
;; - [**Configuration**](./plotje_book.configuration.html) -- control dimensions, palettes, and themes
