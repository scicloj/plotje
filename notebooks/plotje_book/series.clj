;; # Series
;;
;; A table often carries one measure per column. Writing several of
;; those columns where one goes reads them as several series of one
;; layer: Plotje pivots them, invents a key column naming which measure
;; a row holds and a value column holding the number, and maps the key
;; column to `:color`. Because the measures are labelled, they can be
;; placed against each other by a position adjustment.

(ns plotje-book.series
  (:require
   [scicloj.plotje.api :as pj]
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]))

;; ## The data

(def sales
  (tc/dataset {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"]
               :region  ["EU" "EU" "EU" "EU" "AS" "AS" "AS" "AS"]
               :channel ["web" "web" "shop" "shop" "web" "web" "shop" "shop"]
               :revenue [120 150 140 190 90 120 160 210]
               :cost    [90 100 115 120 70 85 110 130]
               :tax     [18 24 21 30 14 19 26 34]
               :units   [12 15 14 19 9 12 16 21]}))

sales

;; ## Several measures on one panel

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax]))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Placing the measures against each other

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill}))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Lines and points

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax]))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

(-> sales
    (pj/lay-point :quarter [:revenue :cost :tax]))

(kind/test-last [(fn [v] (pos? (:points (pj/svg-summary v))))])

;; ## Naming the key column

(-> sales
    (pj/lay-bar :quarter {:series [:revenue :cost :tax] :as :measure}
                {:position :dodge})
    (pj/options {:y-label "Euros"}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (and (contains? texts "measure")
                 (contains? texts "Euros"))))])

;; ## A scale on the value axis

(-> sales
    (pj/lay-point :quarter {:series [:revenue :cost :tax]
                            :scale {:type :log}}))

(kind/test-last
 [(fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))])

;; ## Sideways

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/coord :flip))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Series in panels

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
    (pj/facet-grid :region :channel))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## Series and a second layer

;; The pivot happens on the pose, so a later layer reads the columns it
;; invented -- `:value` for the number and `:series` for the key.

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
    (pj/lay-point :quarter :value))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s)) (pos? (:points s)))))])

;; ## Series and a grouping column

(-> sales
    (pj/lay-line :quarter [:revenue :cost] {:group :region}))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; ## Series in each panel of a faceted grid, drawn as lines

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/facet-grid :region :channel)
    (pj/options {:title "Measures by region and channel"}))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## Nested poses

;; `pj/arrange` places poses side by side. Each input is a pose, so a
;; series pose goes in a cell like any other.

(pj/arrange
 [(-> sales
      (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
  (-> sales
      (pj/lay-line :quarter :units))])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A cell may be a composite itself, so an arranged pose can be
;; arranged again and the cell draws its own grid.

(pj/arrange
 [(pj/arrange
   [(-> sales
        (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
    (-> sales
        (pj/lay-bar :quarter [:revenue :cost] {:position :stack}))]
   {:cols 1})
  (-> sales
      (pj/lay-line :quarter [:revenue :cost :tax]))])

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; ## Sub-plots of faceted panels of series

(pj/arrange
 [(-> sales
      (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
      (pj/facet :region))
  (-> sales
      (pj/lay-line :quarter :units)
      (pj/facet :region))])

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## Sub-plots read against one axis

(pj/arrange
 (vec (for [r ["EU" "AS"]]
        (-> sales
            (tc/select-rows (fn [row] (= r (:region row))))
            (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
            (pj/options {:title r}))))
 {:share-scales #{:y}
  :align-panels true})

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ## A written-out composite of series

(pj/pose
 {:layout {:direction :vertical :weights [2 1]}
  :poses [(-> sales
              (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
          (-> sales
              (pj/lay-line :quarter [:revenue :cost]))]})

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ## What is not a series

;; A vector of `[x y]` pairs is the multi-panel form, one panel per
;; pair:

(-> sales
    (pj/pose [[:quarter :revenue] [:quarter :cost]])
    (pj/lay-point))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A vector on `:group` is one compound key rather than several series:

(-> sales
    (pj/lay-line :quarter :revenue {:group [:region :channel]}))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; ## See Also
;;
;; - [Faceting](./plotje_book.faceting.html) -- panels from a column's
;;   values
;; - [Composition](./plotje_book.composition.html) -- arranging poses
;;   into grids
;; - [Placing Marks](./plotje_book.placing_marks.html) -- every
;;   position adjustment in full
