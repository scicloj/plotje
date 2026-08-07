;; # Polar Coordinates

;; For [polar coordinates](https://en.wikipedia.org/wiki/Polar_coordinate_system),
;; `(pj/coord :polar)` maps x to angle and y to radius. Bars become
;; arc-interpolated wedges (rose charts), and scatter points wrap onto
;; a polar plane. The angular spread is set by the x-column's range,
;; so a narrow range produces a wedge cluster rather than a full
;; circle. Supported marks today are `:point`, `:bar`, `:rect`,
;; `:text`, and `:rug`; other marks raise a clear error. Tick labels
;; and axis labels are not yet rendered under `:polar`, but legends
;; render as they would on Cartesian coords -- a `:color` mapping
;; produces a normal legend that can be repositioned via
;; `:legend-position` (see [Customization](./plotje_book.customization.html#legend-position)).

(ns plotje-book.polar
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

(def wind {:direction ["N" "NE" "E" "SE" "S" "SW" "W" "NW"]
           :speed [12 8 15 10 7 13 9 11]})

;; ## Polar Scatter
;;
;; The same scatter plot, wrapped onto a polar plane. x maps to angle
;; (clockwise from 12 o'clock), y maps to radius (center = minimum,
;; edge = maximum). Iris's `:sepal-length` spans roughly 4.3 to 7.9 --
;; a narrow numeric range -- so the points cluster in an arc rather
;; than spreading across the full circle.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/coord :polar))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; ## Rose Chart (Coxcomb Diagram)
;;
;; A bar chart in polar coordinates produces a
;; [rose chart](https://en.wikipedia.org/wiki/Coxcomb_diagram) --
;; wedge area encodes count. Bars are arc-interpolated for smooth
;; curved edges.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/coord :polar))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:polygons s)))))])

;; ## Wind Rose
;;
;; A bar chart with a y column (value bars) in polar makes a wind
;; rose -- each direction gets a wedge proportional to wind speed.

(-> wind
    (pj/lay-bar :direction :speed)
    (pj/coord :polar))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 8 (:polygons s)))))])

;; ## Polar Stacked Bar
;;
;; Stacked bars in polar show composition within each wedge. Without
;; angular tick labels (not yet rendered under `:polar`), the legend
;; carries the category identity -- read color back to species via
;; the legend rather than around the wedge.

(-> (rdatasets/palmerpenguins-penguins)
    (pj/lay-bar :island {:position :stack :color :species})
    (pj/coord :polar))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Polar Histogram
;;
;; A histogram in polar wraps bins around the circle. Useful for
;; circular distributions (e.g., time of day, compass bearing).

(-> (rdatasets/datasets-iris)
    (pj/lay-histogram :sepal-length)
    (pj/coord :polar))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (pos? (:polygons s)))))])

;; ## Explicit Labels with Polar
;;
;; By default, polar suppresses auto-generated axis labels (since there
;; are no rectangular axes). You can still set a title with `pj/options`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/coord :polar)
    (pj/options {:title "Iris in Polar Space"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (some #{"Iris in Polar Space"} (:texts s)))))])

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- coordinate systems and scale types

;; ## What's Next
;;
;; - [**Cookbook**](./plotje_book.cookbook.html) -- recipes for common multi-layer plots
;; - [**Customization**](./plotje_book.customization.html) -- colors, themes, and mark styling
