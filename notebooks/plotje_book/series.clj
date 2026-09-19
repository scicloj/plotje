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

;; One row per quarter, one column per measure -- the wide shape a
;; series reads.

(def sales
  (tc/dataset {:quarter ["Q1" "Q2" "Q3" "Q4"]
               :revenue [120 150 140 190]
               :cost    [90 100 115 120]
               :tax     [18 24 21 30]
               :units   [12 15 14 19]}))

sales

;; The same measures split by region and outlet, for the examples that
;; divide the canvas as well as the band. A mark draws one shape per
;; row, so a pose reading this one without a panel or a group per region
;; would draw the two regions on top of each other.

(def sales-by-region
  (tc/dataset {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"]
               :region  ["EU" "EU" "EU" "EU" "AS" "AS" "AS" "AS"]
               :outlet ["web" "web" "shop" "shop" "web" "web" "shop" "shop"]
               :revenue [120 150 140 190 90 120 160 210]
               :cost    [90 100 115 120 70 85 110 130]
               :tax     [18 24 21 30 14 19 26 34]
               :units   [12 15 14 19 9 12 16 21]}))

sales-by-region

;; ## Several measures on one panel

;; A bar divides its band between the series, so each quarter's band
;; holds one bar per measure.

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax]))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Placing the measures against each other

;; `:identity` draws every series at the same place, so the bars
;; overlap. It is the one that differs from the bar's own default.
;; Three partly transparent bars drawn from zero make a third tone
;; wherever they cross, which reads as a stack -- the axis is what
;; tells the two apart, since here it reaches the largest measure and
;; under `:stack` below it reaches their total:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity}))

(kind/test-last
 [(fn [v] (not= (pj/plot v)
                (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))])

;; `:dodge` gives each series a slot of the band, which is what a bar
;; does without being asked:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))])

;; `:stack` piles the series, so the band's height is their total:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; `:fill` piles them and normalizes, so each band reads as shares
;; between zero and one:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill}))

(kind/test-last
 [(fn [v] (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))])

;; ## Lines and points

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax]))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

(-> sales
    (pj/lay-point :quarter [:revenue :cost :tax]))

(kind/test-last [(fn [v] (pos? (:points (pj/svg-summary v))))])

;; ## Naming the key column

;; A series can be written out as a map rather than a bare vector. The
;; reason to write it out is to name the key column the pivot invents,
;; which is what titles the legend.

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

;; ## The keys a series takes

;; A series written out takes three keys: `:series` for the columns it
;; reads, `:as` for the key column the pivot invents, and `:scale` for
;; the scale its value column is read through. Any other key reports an
;; error, so a misspelled `:as` is caught where it is written rather
;; than leaving the legend titled `series`.

(try
  (-> sales
      (pj/lay-bar :quarter {:series [:revenue :cost] :label :measure}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))])

;; ## Sideways

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/coord :flip))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## Areas and steps

(-> sales
    (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

(-> sales
    (pj/lay-step :quarter [:revenue :cost]))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; ## Choosing the colours

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]}))

(kind/test-last
 [(fn [v] (= #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
             (disj (:colors (pj/svg-summary v)) "none")))])

;; ## A domain on the value axis

(-> sales
    (pj/lay-point :quarter [:revenue :cost])
    (pj/scale :y {:domain [0 250]}))

(kind/test-last
 [(fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))])

;; ## The columns the pivot invents

;; The pivot drops the columns it reads and puts two columns in their
;; place: `:value` holds the number, and the key column holds which
;; measure a row came from. The key column is named `:series` unless
;; `:as` names it otherwise.

;; Where the data already carries a column of one of those names, the
;; pivot reports an error instead of overwriting the column:

(try
  (-> {:quarter ["Q1" "Q2"]
       :revenue [120 150]
       :cost    [90 100]
       :series  ["a" "b"]}
      (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"the data already has \[:series\]" msg))])

;; `:as` gives the key column a name the data does not use. A data
;; column named `:value` has to be renamed in the data instead, since
;; `:as` does not reach the value column.

(-> {:quarter ["Q1" "Q2"]
     :revenue [120 150]
     :cost    [90 100]
     :series  ["a" "b"]}
    (pj/lay-bar :quarter {:series [:revenue :cost] :as :measure}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (and (= 1 (:panels (pj/svg-summary v)))
                 (contains? texts "measure"))))])

;; ## Two marks over one series

;; The pivot happens on the pose, so a second layer reads the columns it
;; invented -- `:value` for the number and `:series` for the key -- and
;; draws the same series in another mark. The key column is mapped to
;; `:color` on the layer that asked for the series, so a second layer
;; names it to be coloured the same way.

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/lay-point :quarter :value {:color :series}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))])

;; ## Series in panels

(-> sales-by-region
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

(-> sales-by-region
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
    (pj/facet-grid :region :outlet))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## Series and a grouping column

;; A grouping column keeps its own marks separate, so each measure is
;; drawn once per region: two measures across two regions give four
;; lines, coloured by measure.

(-> sales-by-region
    (pj/lay-line :quarter [:revenue :cost] {:group :region}))

(kind/test-last [(fn [v] (= 4 (:lines (pj/svg-summary v))))])

;; ## Colouring by another column

;; The pivot maps the key column to `:color`, which is what gives each
;; measure a colour of its own. Where the layer maps `:color` itself,
;; the pivot maps the key column to `:group` instead. The measures stay
;; separate marks, and the colour shows the region: still four lines,
;; two of them red and two blue.

(-> sales-by-region
    (pj/lay-line :quarter [:revenue :cost] {:color :region}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 4 (:lines s))
                 ;; two colours over four lines: the colour tracks the
                 ;; region, not the measure, which is what separates
                 ;; this section from the one above it.
                 (= 2 (count (disj (:colors s) "none")))
                 (contains? (set (:texts s)) "region"))))])

;; The same layer in panels, one per outlet:

(-> sales-by-region
    (pj/lay-line :quarter [:revenue :cost] {:color :region})
    (pj/facet :outlet))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 8 (:lines s)))))])

;; ## Series in each panel, drawn as lines

;; Each region has all four quarters, so each panel draws a line per
;; measure across the whole axis. Faceting on region and outlet
;; together would leave two quarters per panel, and a line of two points
;; says less than a bar of two does.

(-> sales-by-region
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/facet :region)
    (pj/options {:title "Measures by region"}))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ## Dodged series, drawn sideways, in panels

(-> sales-by-region
    (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
    (pj/coord :flip)
    (pj/facet :region))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; ## Series beside a layer that keeps its own panel

;; A layer naming a column the pivot did not consume takes a panel of
;; its own, and `pj/overlay` puts it on the series panel instead.

(-> sales
    (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
    (pj/lay-line :quarter :units))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

(-> sales
    (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
    (pj/lay-line :quarter :units)
    pj/overlay)

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; ## A rule across every panel of a series

;; A layer naming no columns of its own is drawn on every panel.

(-> sales-by-region
    (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
    (pj/facet :region)
    (pj/lay-rule-h {:y-intercept 120}))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

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
 [(-> sales-by-region
      (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
      (pj/facet :region))
  (-> sales-by-region
      (pj/lay-line :quarter :units)
      (pj/facet :region))])

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## Sub-plots read against one axis

(pj/arrange
 (vec (for [r ["EU" "AS"]]
        (-> sales-by-region
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

;; ## Three levels of nesting

(pj/arrange
 [(pj/arrange
   [(-> sales
        (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
    (-> sales
        (pj/lay-line :quarter [:revenue :cost]))]
   {:cols 1})
  (pj/arrange
   [(-> sales
        (pj/lay-area :quarter [:revenue :cost] {:position :stack}))
    (-> sales
        (pj/lay-point :quarter :units))]
   {:cols 1})]
 {:title "Measures four ways"})

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## A titled grid of series

(pj/arrange
 (vec (for [pos [:identity :dodge :stack :fill]]
        (-> sales
            (pj/lay-bar :quarter [:revenue :cost :tax] {:position pos})
            (pj/options {:title (name pos)}))))
 {:cols 2})

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## What is not a series

;; A vector of `[x y]` pairs is the multi-panel form, one panel per
;; pair:

(-> sales
    (pj/pose [[:quarter :revenue] [:quarter :cost]])
    (pj/lay-point))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A vector on `:group` is one compound key rather than several series:

(-> sales-by-region
    (pj/lay-line :quarter :revenue {:group [:region :outlet]}))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; ## See Also
;;
;; - [Faceting](./plotje_book.faceting.html) -- panels from a column's
;;   values
;; - [Composition](./plotje_book.composition.html) -- arranging poses
;;   into grids
;; - [Placing Marks](./plotje_book.placing_marks.html) -- every
;;   position adjustment in full
