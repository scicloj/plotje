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
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   [tablecloth.api :as tc]
   [clojure.string :as str]))

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

;; ## The same plot from long data

;; A series is the wide-side spelling of something the long shape
;; already writes. Pivoting the measures into a key column and a value
;; column, then mapping the key column to `:color`, draws the same
;; plot:

(-> sales
    (tc/pivot->longer #{:revenue :cost :tax}
                      {:target-columns :series :value-column-name :value})
    (pj/lay-bar :quarter :value {:color :series :position :dodge}))

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> sales
                          (pj/lay-bar :quarter [:revenue :cost :tax]
                                      {:position :dodge})))))])

;; That equality is what makes the adjustments above work. An
;; adjustment divides a band between labelled competitors, and the
;; pivot is what gives the measures labels.

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

;; `pj/scale` on the axis the series is written on says the same thing,
;; so the scale can go in either place:

(-> sales
    (pj/lay-point :quarter [:revenue :cost :tax])
    (pj/scale :y {:type :log}))

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> sales
                          (pj/lay-point :quarter
                                        {:series [:revenue :cost :tax]
                                         :scale {:type :log}})))))])

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

;; ## Where a series is written

;; `:x` and `:y` are often written on the pose, and a series is the
;; exception. The pivot reshapes the dataset the pose carries, so
;; `pj/pose` reports instead, and names the call to write the series in:

(try
  (-> sales
      (pj/pose :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"lay-\* call's :y instead" msg))])

;; Within the lay-* call a series goes wherever a column goes -- as an
;; argument, or written under `:x` or `:y` in a map. The two spellings
;; draw the same plot:

(-> sales
    (pj/lay-bar {:x :quarter :y [:revenue :cost :tax]}))

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> sales
                          (pj/lay-bar :quarter [:revenue :cost :tax])))))])

;; A series goes where a column goes on an axis, so it can be written
;; on `:x` as readily as on `:y`. On `:x` the measures run along the
;; horizontal axis and the quarters go up the vertical one:

(-> sales
    (pj/lay-point {:series [:revenue :cost :tax]} :quarter))

(kind/test-last [(fn [v] (= 12 (:points (pj/svg-summary v))))])

;; The bare vector means on `:x` what it means on `:y`:

(-> sales
    (pj/lay-point [:revenue :cost :tax] :quarter))

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> sales
                          (pj/lay-point {:series [:revenue :cost :tax]}
                                        :quarter)))))])

;; For bars, write the series on `:y` and turn the plot with
;; `pj/coord` as the section above does. A bar drawn from a category on
;; `:y` is supported less fully -- see
;; [Known Limitations](./plotje_book.known_limitations.html#marks).

;; ## Areas and steps

(-> sales
    (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack}))

(kind/test-last [(fn [v] (pos? (:polygons (pj/svg-summary v))))])

(-> sales
    (pj/lay-step :quarter [:revenue :cost]))

(kind/test-last [(fn [v] (pos? (:lines (pj/svg-summary v))))])

;; An area normalizes under `:fill` the way a bar does, so each
;; quarter reads as shares between zero and one:

(-> sales
    (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill}))

(kind/test-last
 [(fn [v] (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))])

;; ## Marks that take a series

;; Any mark drawing from a value column takes a series. The five above
;; are bars, lines, points, areas and steps; a lollipop is another:

(-> sales
    (pj/lay-lollipop :quarter [:revenue :cost]))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 8 (:points s))
                 (= 8 (:lines s)))))])

;; The pivot does not change what a mark's stat requires. A mark
;; needing a numeric axis column -- a density, a histogram, a smooth --
;; reports when it is drawn, since the quarter column is categorical
;; whether the measures are pivoted or not:

(try
  (pj/plot (-> sales (pj/lay-density :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"requires a numeric column" msg))])

;; ## A stat over a series

;; A stat runs after the pivot, so it reads one series at a time. Each
;; measure of each quarter has a row per region in `sales-by-region`,
;; and `pj/lay-summary` draws the mean of those rows with a
;; standard-error bar around it -- one mean per quarter per measure:

(-> sales-by-region
    (pj/lay-summary :quarter [:revenue :cost]))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 8 (:points s))
                 (= 8 (:lines s)))))])

;; ## A series along a date axis

;; The pivot reshapes the measures and leaves the axis column as it
;; was, so a date axis stays a date axis. Two columns of the economics
;; dataset hold counts of people in thousands -- the population and the
;; number of them unemployed -- which a log scale brings onto one axis:

(-> (rdatasets/ggplot2-economics)
    (pj/lay-line :date {:series [:pop :unemploy] :scale {:type :log}}))

(kind/test-last
 [(fn [v] (and (= 2 (:lines (pj/svg-summary v)))
               (= :log (-> v pj/plan :panels first :y-scale :type))))])

;; A stat needing a numeric axis column runs here where it reported on
;; the quarters above, since a date is not a category -- one smooth per
;; measure:

(-> (rdatasets/ggplot2-economics)
    (pj/lay-smooth :date {:series [:pop :unemploy] :scale {:type :log}}))

(kind/test-last [(fn [v] (= 2 (:lines (pj/svg-summary v))))])

;; ## Choosing the colours

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]}))

(kind/test-last
 [(fn [v] (= #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
             (disj (:colors (pj/svg-summary v)) "none")))])

;; A scale set on the pose carries down to every panel, so the same
;; three colours are used in each:

(-> sales-by-region
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
    (pj/facet :region))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 6 (:lines s))
                 (= #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
                    (disj (:colors s) "none")))))])

;; ## The order of the series

;; The order the measures are drawn in -- the legend's order, the order
;; of the slots in a dodged band, and the order a stack piles them --
;; follows the dataset's columns rather than the order they are written
;; in. Writing the same three measures in another order draws the same
;; plot:

(-> sales
    (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack}))

(kind/test-last
 [(fn [v] (and (= (pj/plot v)
                  (pj/plot (-> sales
                               (pj/lay-bar :quarter [:revenue :cost :tax]
                                           {:position :stack}))))
               ;; the dodged band is the other half of the claim above,
               ;; and it is invariant the same way.
               (= (pj/plot (-> sales
                               (pj/lay-bar :quarter [:tax :revenue :cost]
                                           {:position :dodge})))
                  (pj/plot (-> sales
                               (pj/lay-bar :quarter [:revenue :cost :tax]
                                           {:position :dodge}))))))])

;; A `:domain` on the colour scale sets the order, since the key column
;; is the column the colour reads:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
    (pj/scale :color {:domain [:tax :revenue :cost]}))

(kind/test-last
 [(fn [v] (= ["tax" "revenue" "cost"]
             (mapv :label (-> v pj/plan :panels first :layers first :groups))))])

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

;; ## One series per pose

;; The pivot leaves its two columns on the pose, so a second series on
;; the same pose meets them:

(try
  (-> sales
      (pj/lay-bar :quarter [:revenue :cost])
      (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"the data already has \[:series :value\]" msg))])

;; The message names `:as`, which renames the key column -- but
;; `:value` clashes as well, so a new name for the key column does not
;; clear it. Give each series a pose of its own and arrange the poses:

(pj/arrange
 [(-> sales
      (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
  (-> sales
      (pj/lay-line :quarter [:tax :units]))])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

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

;; A layer naming no columns draws at the same place in the default
;; colour. The key column is mapped on the layer that asked for the
;; series, and a layer added afterwards reads the pose rather than that
;; layer, so the twelve points come out one colour:

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/lay-point))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 12 (:points s))
                 (= 3 (:lines s)))))])

;; ## Panels from the key column

;; The key column is an ordinary column of the pivoted dataset, so
;; `pj/facet` reads it as it reads any other: one panel per measure.
;; The panels share one value axis, which is what lets the measures be
;; read against each other.

(-> sales
    (pj/lay-line :quarter [:revenue :cost :tax])
    (pj/facet :series))

(kind/test-last
 [(fn [v] (and (= 3 (:panels (pj/svg-summary v)))
               (= 1 (count (distinct (map (fn [panel]
                                            (mapv double (:y-domain panel)))
                                          (:panels (pj/plan v))))))))])

;; With `:as`, the facet names the key column by its new name:

(-> sales
    (pj/lay-line :quarter {:series [:revenue :cost :tax] :as :measure})
    (pj/facet :measure))

(kind/test-last [(fn [v] (= 3 (:panels (pj/svg-summary v))))])

;; ## The key column on the axis

;; Naming the key column on an axis draws the measures against each
;; other rather than along the quarters. The pivot runs before the axis
;; is read, so the column is there by the time the mark is drawn: one
;; box per measure, from every row that measure has.

(-> sales-by-region
    (pj/lay-boxplot :series [:revenue :cost :tax]))

(kind/test-last [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; A violin over the same three measures:

(-> sales-by-region
    (pj/lay-violin :series [:revenue :cost :tax]))

(kind/test-last [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; ## Another aesthetic from the key column

;; The layer that asks for a series may map the key column again. Shape
;; beside colour separates the measures twice over, which survives
;; printing in one colour:

(-> sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            ;; twelve marks, and not all of them drawn the same way:
            ;; the default point is a rect and one measure now takes a
            ;; polygon instead.
            (and (= 8 (:points s))
                 (= 4 (:polygons s)))))])

;; `:tooltip` reads the key column as readily, so in the HTML
;; rendering of this page hovering a point says which measure it holds:

(-> sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series}))

(kind/test-last
 [(fn [v] (and (true? (:tooltip (pj/plan v)))
               (= [[:revenue] [:cost] [:tax]]
                  (mapv (fn [group] (vec (distinct (:tooltips group))))
                        (-> v pj/plan :panels first :layers first :groups)))))])

;; `:size` and `:alpha` draw a magnitude, and a measure's name is not
;; one, so both report:

(try
  (pj/plot (-> sales
               (pj/lay-point :quarter [:revenue :cost :tax]
                             {:size :series})))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (and (re-find #":size needs a numeric column" msg)
                 ;; :alpha is the other half of the claim above, and
                 ;; reports the same way.
                 (re-find #":alpha needs a numeric column"
                          (try (do (pj/plot
                                    (-> sales
                                        (pj/lay-point :quarter
                                                      [:revenue :cost :tax]
                                                      {:alpha :series})))
                                   "")
                               (catch clojure.lang.ExceptionInfo e
                                 (ex-message e))))))])

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

;; A bar is the case to be careful with. A band is divided between
;; labelled competitors, and a series has already taken the one label a
;; bar divides by, so a grouping column beside a series draws its marks
;; at the same place rather than beside each other. Facet on the column
;; instead, as the panel sections below do, or draw a mark that needs
;; no slot.

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

;; A `:color` written on the pose does not reach the marks. The pivot
;; writes `:color` on the layer that asks for the series, and a layer's
;; own mapping wins over the pose's, so the region separates nothing and
;; each line runs through both regions -- the same plot as one with no
;; `:color` on the pose at all. Write `:color` on the layer, as the two
;; examples above do:

(-> sales-by-region
    (pj/pose {:color :region})
    (pj/lay-line :quarter [:revenue :cost]))

(kind/test-last
 [(fn [v] (= (pj/plot v)
             (pj/plot (-> sales-by-region
                          (pj/lay-line :quarter [:revenue :cost])))))])

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

;; ## Shares, drawn sideways

;; Normalizing and turning the plot compose, so each quarter becomes a
;; bar of shares running left to right:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
    (pj/coord :flip))

(kind/test-last
 [(fn [v] (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))])

;; ## A named series, scaled and in panels

;; The written-out form composes with the rest. Here the key column is
;; named, the value column is read through a log scale, the value axis
;; is relabelled, and the canvas is divided by region:

(-> sales-by-region
    (pj/lay-point :quarter {:series [:revenue :cost :tax]
                            :as :measure
                            :scale {:type :log}})
    (pj/facet :region)
    (pj/options {:y-label "Euros"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 24 (:points s))
                 (contains? (set (:texts s)) "measure")
                 (contains? (set (:texts s)) "Euros")
                 (= :log (-> v pj/plan :panels first :y-scale :type)))))])

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

;; Drawing the first of those two says what it did, and names both ways
;; on: `pj/overlay`, and reading the two columns as one series.

(with-out-str
  (pj/plot (-> sales
               (pj/lay-bar :quarter [:revenue :cost])
               (pj/lay-line :quarter :units))))

(kind/test-last
 [(fn [out] (and (re-find #"panel of its own" out)
                 (re-find #"pj/overlay" out)
                 (re-find #"as series" out)))])

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

;; ## A grid of panels of series

;; One combination has no spelling. A grid of panels built from pairs
;; of columns reads the dataset wide, and a series reads it long, so a
;; grid whose cells each carry several series asks the dataset to be
;; both shapes at once:

(try
  (-> sales
      (pj/pose [[:quarter :revenue] [:quarter :cost]])
      (pj/lay-point :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"composite pose" msg))])

;; Both routes to the same picture are on the long side. `pj/facet`
;; reaches it where the split comes from a column, and `pj/arrange` of
;; series poses where it does not -- which is what the sub-plot
;; sections above do.

;; ## What a series refuses

;; The sections above work through the refusals a writer meets most
;; often. The rest of the set, with the message each one gives:

(kind/table
 {:column-names ["written" "what it reports"]
  :row-vectors
  (mapv (fn [[written f]]
          [(kind/code written)
           (try (do (pj/plot (f)) "draws")
                (catch Throwable e (first (str/split (ex-message e) #"\. "))))])
        [["{:color [:revenue :cost :tax]}"
          #(-> sales (pj/lay-point :quarter :revenue {:color [:revenue :cost :tax]}))]
         ["(pj/lay-point {:series [:revenue :cost]} {:series [:tax :units]})"
          #(-> sales (pj/lay-point {:series [:revenue :cost]} {:series [:tax :units]}))]
         ["{:series [:revenue]}"
          #(-> sales (pj/lay-bar :quarter {:series [:revenue]}))]
         ["[:revenue 42]"
          #(-> sales (pj/lay-bar :quarter {:series [:revenue 42]}))]
         ["[:revenue :nope]"
          #(-> sales (pj/lay-bar :quarter [:revenue :nope]))]
         ["a mapping already naming a consumed column"
          #(-> sales (pj/pose :quarter :revenue)
               (pj/lay-bar :quarter [:revenue :cost :tax]))]
         ["a series on a composite pose"
          #(-> (pj/arrange [(pj/lay-point sales :quarter :revenue)])
               (pj/lay-bar :quarter [:revenue :cost :tax]))]
         ["a pose carrying no data"
          #(-> (pj/pose) (pj/lay-bar :quarter [:revenue :cost]))]
         ["{:y-min [:cost :tax]}"
          #(-> sales (pj/lay-errorbar :quarter :revenue {:y-min [:cost :tax]}))]
         ["[:revenue :outlet]"
          #(-> sales-by-region (pj/lay-bar :quarter [:revenue :outlet]))]])})

;; The table renders the messages as strings, so the assertion reads
;; the calls again rather than reading the table.

(kind/test-last
 [(fn [t] (and (= 10 (count (:row-vectors t)))
               (every? (fn [f] (try (pj/plot (f)) false (catch Throwable _ true)))
                       [#(-> sales (pj/lay-point :quarter :revenue {:color [:revenue :cost :tax]}))
                        #(-> sales (pj/lay-point {:series [:revenue :cost]} {:series [:tax :units]}))
                        #(-> sales (pj/lay-bar :quarter {:series [:revenue]}))
                        #(-> sales (pj/lay-bar :quarter {:series [:revenue 42]}))
                        #(-> sales (pj/lay-bar :quarter [:revenue :nope]))
                        #(-> sales (pj/pose :quarter :revenue)
                             (pj/lay-bar :quarter [:revenue :cost :tax]))
                        #(-> (pj/arrange [(pj/lay-point sales :quarter :revenue)])
                             (pj/lay-bar :quarter [:revenue :cost :tax]))
                        #(-> (pj/pose) (pj/lay-bar :quarter [:revenue :cost]))
                        #(-> sales (pj/lay-errorbar :quarter :revenue
                                                    {:y-min [:cost :tax]}))
                        #(-> sales-by-region
                             (pj/lay-bar :quarter [:revenue :outlet]))])))])

;; ## What is not a series

;; A vector of `[x y]` pairs is the multi-panel form, one panel per
;; pair:

(-> sales
    (pj/pose [[:quarter :revenue] [:quarter :cost]])
    (pj/lay-point))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A vector with no other column beside it is the multi-panel form as
;; well: a lay-* call reads it as one column per panel, and each panel
;; draws its own mark.

(-> sales
    (pj/lay-histogram [:revenue :cost]))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A vector on `:group` is one compound key rather than several series.
;; Region combined with outlet gives four keys, so the line is drawn in
;; four pieces:

(-> sales-by-region
    (pj/lay-line :quarter :revenue {:group [:region :outlet]}))

(kind/test-last [(fn [v] (= 4 (:lines (pj/svg-summary v))))])

;; A vector whose elements are not column references is whatever the
;; aesthetic it is written on reads it as. A dash pattern is two
;; lengths, not two columns to pivot:

(-> sales
    (pj/lay-line :quarter :revenue {:stroke-dash [5 5]}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 1 (count (:dash-patterns s))))))])

;; ## See Also
;;
;; - [Faceting](./plotje_book.faceting.html) -- panels from a column's
;;   values
;; - [Composition](./plotje_book.composition.html) -- arranging poses
;;   into grids
;; - [Placing Marks](./plotje_book.placing_marks.html) -- every
;;   position adjustment in full
