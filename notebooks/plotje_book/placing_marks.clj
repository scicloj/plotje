;; # Placing Marks
;;
;; Where a mark goes, and in what units.
;;
;; A scale turns a data value into a place on the page, which covers most
;; plotting. Three cases need more: a mark that must sit clear of another
;; one, a note at a position no row of the data holds, and code that needs
;; to know where a value landed in order to draw beside it.
;;
;; Except for the anchor options and `:fit-text-domain`, which are
;; text-only, everything here applies to any layer type. Text meets these
;; problems first, because a label is drawn at another mark's coordinates
;; and has a size of its own.

(ns plotje-book.placing-marks
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; dtype-next -- element-wise column arithmetic
   [tech.v3.datatype.functional :as dfn]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## Two spaces
;;
;; A number that places a mark means nothing without the space it is
;; measured in. Plotje uses two.
;;
;; **Data space** holds values in their original units -- a sepal length
;; of 6.5, a year of 2007, the category `"setosa"`. **Drawing space**
;; holds distances on the page, measured in **drawing units** from the
;; top left, where one drawing unit is one unit of the plot's `:width`
;; and `:height`. The scales map the first onto the second.
;;
;; Three rectangles matter in drawing space, all defined in the
;; [Glossary](./plotje_book.glossary.html): the **canvas** is the whole
;; image, a **panel box** is one panel including its axis margin, and the
;; **drawing area** is the shaded background inside that margin, where
;; the marks are drawn.
;;
;; Every option in this chapter takes a number in one space or the other.

;; ## Anchoring a text mark
;;
;; `pj/lay-text` and `pj/lay-label` place a label at a data point. By
;; default the label starts at the point and reads to the right,
;; centered on the point vertically. Two options move the label relative
;; to the point by choosing which part of the text the point pins:
;;
;; - `:align-x` -- `:left`, `:center`, or `:right` (default `:left`)
;; - `:align-y` -- `:top`, `:center`, or `:bottom` (default `:center`)
;;
;; They do the work of ggplot2's `hjust` and `vjust`, as named values
;; rather than numbers.
;;
;; The value names the part of the text placed at the point. `:align-x
;; :right` pins the right edge, so the label extends leftward; `:align-x
;; :center` straddles the point. `:align-y` reads in data orientation:
;; `:top` pins the top edge, so the text reads downward from the point,
;; and `:bottom` pins the bottom edge, so the text floats above it.
;;
;; Three labels on a column of points at the same hour, one per
;; horizontal anchor -- the text fans right of, across, and left of the
;; point:

(-> {:hour [2 2 2] :level [3 2 1]}
    (pj/lay-point :hour :level {:size 6 :color "#888888"})
    (pj/lay-text :hour :level {:text :tag :align-x :left
                               :data {:hour [2] :level [3]
                                      :tag ["align-x :left"]}})
    (pj/lay-text :hour :level {:text :tag :align-x :center
                               :data {:hour [2] :level [2]
                                      :tag ["align-x :center"]}})
    (pj/lay-text :hour :level {:text :tag :align-x :right
                               :data {:hour [2] :level [1]
                                      :tag ["align-x :right"]}}))

(kind/test-last
 [(fn [fr]
    (= [:left :center :right]
       (->> fr pj/plan :panels first :layers
            (filter #(= :text (:mark %)))
            (mapv #(-> % :style :align-x)))))])

;; A practical use of `:align-y`: float a value label above each bar by
;; pinning the label's bottom edge to the bar top, centered over the
;; bar.

(-> {:species ["setosa" "versicolor" "virginica"]
     :pct     [33.3 33.3 33.3]}
    (pj/lay-bar :species :pct {:color "#a6cee3"})
    (pj/lay-text :species :pct {:text :pct :align-x :center :align-y :bottom}))

;; Both options flow through for `pj/lay-text` and `pj/lay-label` alike,
;; and an unrecognized value is rejected at plan time.

(kind/test-last
 [(fn [fr]
    (let [style-of (->> fr pj/plan :panels first :layers
                        (filter #(= :text (:mark %)))
                        first :style)
          text-style
          (fn [layer-fn opts]
            (->> (-> {:x [1] :y [1] :t ["a"]}
                     (layer-fn :x :y (merge {:text :t} opts)))
                 pj/plan :panels first :layers
                 (filter #(= :text (:mark %)))
                 first :style
                 (#(select-keys % [:align-x :align-y]))))]
      (and
       (= :center (:align-x style-of))
       (= :bottom (:align-y style-of))
       (= {:align-x :left :align-y :center} (text-style pj/lay-text {}))
       (= :left   (:align-x (text-style pj/lay-text {:align-x :left})))
       (= :center (:align-x (text-style pj/lay-text {:align-x :center})))
       (= :right  (:align-x (text-style pj/lay-text {:align-x :right})))
       (= :top    (:align-y (text-style pj/lay-text {:align-y :top})))
       (= :center (:align-y (text-style pj/lay-text {:align-y :center})))
       (= :bottom (:align-y (text-style pj/lay-text {:align-y :bottom})))
       (= {:align-x :right :align-y :top}
          (text-style pj/lay-label {:align-x :right :align-y :top}))
       (try (text-style pj/lay-text {:align-x :middle}) false
            (catch Exception _ true)))))])

;; These two are the only options in this chapter that belong to text
;; alone. A `pj/lay-point` call warns and ignores them, and names the
;; layer types they belong to.

;; ## Shifting a mark by a distance on the page
;;
;; Anchoring places one edge of the label exactly on the point, so a
;; point drawn with a radius still overlaps it. Clearing the mark takes a
;; shift of a few units on the page, which is not a data quantity: it
;; depends on the size the mark is drawn at, not on what the axis
;; measures.
;;
;; `:nudge-x` shifts the datum before the scales run, which is why it
;; cannot do this. These cars are labelled with their names and nudged
;; along an axis of weights in thousands of pounds, where 0.08 is about
;; the width of the marker:

(def cars
  (-> (rdatasets/datasets-mtcars)
      (tc/select-rows (range 0 30 5))
      (tc/select-columns [:rownames :wt :mpg :disp])))

cars

(kind/test-last
 [(fn [ds] (and (= 6 (tc/row-count ds))
                (= [1.935 5.424] [(apply min (ds :wt)) (apply max (ds :wt))])
                (= [79.0 460.0] [(apply min (ds :disp)) (apply max (ds :disp))])))])

(-> cars
    (pj/lay-point :wt :mpg {:size 5})
    (pj/lay-text {:text :rownames :nudge-x 0.08}))

(kind/test-last
 [(fn [fr]
    (let [panel (-> fr pj/frames :panels first)
          at    #(first (pj/to-drawing panel % 20.0))]
      ;; Wider bounds than the bare scatter would give: this pose carries
      ;; the labels, so its axis was widened to make room for them.
      (< 8.0 (- (at 2.08) (at 2.0)) 11.0)))])

;; That number does not transfer to another axis. Displacement spans 79
;; to 460 where weight spans 1.9 to 5.4, so the same nudge moves the text
;; by about a tenth of a drawing unit and the labels sit back on their
;; markers:

(-> cars
    (pj/lay-point :disp :mpg {:size 5})
    (pj/lay-text {:text :rownames :nudge-x 0.08}))

(kind/test-last
 [(fn [fr]
    (let [panel (-> fr pj/frames :panels first)
          at    #(first (pj/to-drawing panel % 20.0))]
      (< (- (at 79.08) (at 79.0)) 0.2)))])

;; `:offset-x` and `:offset-y` shift a layer by a number of drawing units
;; after the scales have run, so one number clears the marker on either
;; axis. Positive `:offset-y` moves down the page, as drawing coordinates
;; do:

(-> cars
    (pj/lay-point :disp :mpg {:size 5})
    (pj/lay-text {:text :rownames :offset-x 10}))

(kind/test-last
 [(fn [fr]
    (= [nil 10]
       (->> fr pj/plan :panels first :layers (mapv :offset-x))))])

;; Every layer type takes them. On a categorical axis there is no numeric
;; value to shift at all, so `:nudge-x` throws there and an offset still
;; works:

(-> {:team ["red" "green" "blue"] :score [3 5 4]}
    (pj/lay-bar :team :score)
    (pj/lay-text {:text :score :align-x :center :offset-y -6}))

(kind/test-last
 [(fn [fr]
    (= [nil -6]
       (->> fr pj/plan :panels first :layers (mapv :offset-y))))])

;; A nudge leaves the axis domain alone, so a nudge large enough to carry
;; a mark past the domain leaves it clipped; ggplot2's `nudge_x` widens
;; the range instead. The [Glossary](./plotje_book.glossary.html) entry
;; for Nudge covers that difference.
;;
;; What an offset does not do is keep labels off each other. It moves a
;; whole layer by one amount, so two labels at nearby values stay as
;; close together as they were. Separating them is a per-mark decision --
;; see [Known Limitations](./plotje_book.known_limitations.html).

;; ## Giving `:x` and `:y` as values
;;
;; A note in a corner, a caption beside a peak, or a marker on one
;; interesting observation has no row in the dataset. `:x` and `:y` may
;; be given as a value rather than as a column, the way `:color`, `:size`
;; and `:alpha` have always allowed -- `{:color :species}` beside
;; `{:color "red"}`.
;;
;; Give both as values and the layer draws a single mark. On such a layer
;; a string `:text` is the text itself rather than a column name, since
;; there is no data holding a column for it to name:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-text {:x 7.5 :y 4.2 :text "outliers up here"}))

(kind/test-last
 [(fn [fr] (some #{"outliers up here"}
                 (:texts (pj/svg-summary (pj/plot fr)))))])

;; That note is placed *in the data*: 7.5 is a sepal length, and the axis
;; holds it like any other value, widening if the note sits beyond the
;; points. ggplot2's `annotate()` works the same way, building a one-row
;; data frame from the values it is given.
;;
;; Give one as a value and the other as a column, and the value repeats
;; for every row. That is how a label at one fixed x is written: the
;; names line up in a stack instead of scattering with the points.

(-> cars
    (pj/lay-point :wt :mpg)
    (pj/lay-text {:x 5.6 :y :mpg :text :rownames :offset-x 6}))

(kind/test-last
 [(fn [fr] (every? (set (:texts (pj/svg-summary (pj/plot fr))))
                   ["Mazda RX4" "Valiant" "Merc 280C"]))])

;; ## Placing a mark on the panel instead of in the data
;;
;; The layer option `:in` names the space a layer's `:x` and `:y` are in:
;; `:data` by default, or `:drawing-area`, which measures drawing units
;; from the top left of the panel background. A `:drawing-area` layer is
;; placed on the panel rather than in the data, so the axis domains are
;; left alone:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-text {:in :drawing-area :x 12 :y 12 :text "n = 150"}))

(kind/test-last
 [(fn [fr]
    (let [dom (fn [p] (:x-domain (first (:panels (pj/plan p)))))]
      (and (= 150 (tc/row-count (rdatasets/datasets-iris)))
           (= (dom (pj/lay-point (rdatasets/datasets-iris)
                                 :sepal-length :sepal-width))
              (dom fr)))))])

;; That distinction is the point of having two spaces. A value in data
;; space is a datum and widens the domain; a number in drawing space is a
;; measurement of the page and must not. The same two numbers, read in
;; the two spaces, land in different places, and only the data-space
;; reading moves the axis:

(let [base       (pj/lay-point (rdatasets/datasets-iris)
                               :sepal-length :sepal-width)
      in-data    (pj/lay-text base {:x 12 :y 12 :text "x"})
      in-drawing (pj/lay-text base {:in :drawing-area :x 12 :y 12 :text "x"})
      x-domain   #(:x-domain (first (:panels (pj/plan %))))]
  {:no-note         (x-domain base)
   :note-in-data    (x-domain in-data)
   :note-in-drawing (x-domain in-drawing)})

(kind/test-last
 [(fn [m] (and (= (:no-note m) (:note-in-drawing m))
               (not= (:no-note m) (:note-in-data m))))])

;; Use a data-space value when the note is about a value, and
;; `:in :drawing-area` when it is about the picture. `pj/lay-rule-*` and
;; `pj/lay-band-*` reject `:in`: those four are carried on the panel's
;; annotations and are placed from data values only.

;; ## Where a value lands on the page
;;
;; The two spaces are connected by the scales, and `pj/frames` publishes
;; that connection. It reports, as plain data, where each panel sits on
;; the canvas -- the same computation the renderer draws with.

(def scatter
  (-> cars
      (pj/lay-point :wt :mpg)
      (pj/options {:width 620 :height 380})))

(-> scatter pj/frames kind/pprint)

;; The `:canvas` is reported once for the plot; `:panels` carries one
;; entry per panel, each with its `:row` and `:col`, its domains, and the
;; two rectangles named above. The axis margin is the difference between
;; them rather than a stored number.
;;
;; `pj/to-drawing` maps a data point onto the page and `pj/to-data` maps
;; back, both taking one panel entry:

(let [panel (-> scatter pj/frames :panels first)]
  {:mazda-rx4-at      (pj/to-drawing panel 2.62 21.0)
   :under-the-pointer (pj/to-data panel 300 200)
   :round-trip        (->> (pj/to-drawing panel 2.62 21.0)
                           (apply pj/to-data panel))})

(kind/test-last
 [(fn [m] (every? true?
                  (map #(< (abs (- %1 %2)) 1e-9)
                       (:round-trip m) [2.62 21.0])))])

;; A panel reports in `:invertible?` whether the inverse exists at all:
;; under `:coord :polar` it does not, and `pj/to-data` throws rather than
;; returning a wrong answer.
;;
;; Both directions also take a dataset of coordinates, with `:x` and `:y`
;; columns, and answer with a dataset shaped the same way. That arity
;; maps whole columns and builds the panel's scales once, so it is the
;; one to reach for when placing many positions:

(pj/to-drawing (-> scatter pj/frames :panels first)
               {:x [2.62 3.44 5.25] :y [21.0 18.1 10.4]})

(kind/test-last [(fn [ds] (= [:x :y] (vec (tc/column-names ds))))])

;; A dataset rather than a collection of pairs because the two
;; coordinates of a point share one index space, which a dataset carries
;; and two separate sequences do not.
;;
;; This answers the question the nudge section left open. How far does a
;; nudge of 0.08 move a label, on each of those two axes? Ask the two
;; panels where 0.08 of their own units lands:

(let [shift (fn [column]
              (let [panel (-> cars
                              (pj/lay-point column :mpg)
                              pj/frames
                              :panels
                              first)
                    lo    (apply min (cars column))
                    at    #(first (pj/to-drawing panel % 20.0))]
                (- (at (+ lo 0.08)) (at lo))))]
  {:on-weight       (shift :wt)
   :on-displacement (shift :disp)})

(kind/test-last
 [(fn [m] (and (< 10.0 (:on-weight m) 13.0)
               (< (:on-displacement m) 0.2)))])

;; Both figures come from the same `:nudge-x 0.08`, read on the two axes
;; above.

;; ## Drawing from a measurement
;;
;; The frames report canvas coordinates, measured from the top left of
;; the whole image. A `{:in :drawing-area}` layer measures from the
;; drawing area's own corner, so drawing a mapped position back means
;; subtracting that corner first:

(let [panel     (-> scatter pj/frames :panels first)
      [dax day] (-> panel :frames :drawing-area)
      ;; The Mazda RX4 and the Lincoln Continental, at their own values.
      canvas    (pj/to-drawing panel {:x [2.62 5.424] :y [21.0 10.4]})]
  (pj/lay-point scatter {:in :drawing-area
                         :data {:x (dfn/- (canvas :x) dax)
                                :y (dfn/- (canvas :y) day)}
                         :x :x :y :y :color "#cc3311" :size 7}))

(kind/test-last
 [(fn [fr]
    ;; Two marks placed through drawing space, over the six drawn from
    ;; the data. Each lands on the point whose values it was mapped from:
    ;; the published mapping and the renderer agree.
    (= 8 (:points (pj/svg-summary (pj/plot fr)))))])

;; Measure the pose you are going to draw. Frames describe one pose, and
;; anything that changes the layout changes them -- adding a title
;; shortens the drawing area, so a caption placed from frames taken
;; before the title lands below the panel and is clipped away:

(let [drawing-area #(-> % pj/frames :panels first :frames :drawing-area)]
  {:untitled (drawing-area scatter)
   :titled   (drawing-area (pj/options scatter {:title "Motor Trend Cars"}))})

(kind/test-last
 [(fn [m] (< (last (:titled m)) (last (:untitled m))))])

;; A drawing-space layer is safe to add after measuring: it takes no part
;; in the domains and asks for no margin, so the drawing area it was
;; placed in is the one it lands in. Here the caption goes in the bottom
;; left corner, which needs the panel's height:

(let [[_ _ _ h] (-> scatter pj/frames :panels first :frames :drawing-area)]
  (pj/lay-text scatter {:in :drawing-area :x 12 :y (- h 16)
                        :text "n = 6" :align-x :left :color "#555555"}))

(kind/test-last
 [(fn [fr]
    (let [drawing-area #(-> % pj/frames :panels first :frames :drawing-area)]
      (and (= 6 (tc/row-count cars))
           (some #{"n = 6"} (:texts (pj/svg-summary (pj/plot fr))))
           (= (drawing-area scatter) (drawing-area fr)))))])

;; ## Room for a label at the edge of the data
;;
;; A bar labelled with its own value is the ordinary way to let a reader
;; take an exact number off a chart. The label for the longest bar is the
;; one at risk: a text mark's size is fixed in drawing units rather than
;; measured in data units, so a label anchored at the largest value
;; reaches past the end of the axis whatever range that axis covers, and
;; the panel cuts it off there. A count in the hundreds of thousands then
;; reads as its first four digits.
;;
;; The axis is widened instead, by however much the text needs. This is
;; on by default, and the widening is driven by the labels themselves, so
;; an axis only grows when something on it would otherwise be cut:

(def tickets-by-violation
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
   :tickets   [462389 181444 163294]})

(-> tickets-by-violation
    (pj/lay-bar :tickets :violation)
    (pj/lay-label :tickets :violation {:text :tickets}))

;; Setting `:fit-text-domain` to false leaves the axis at the range the
;; data alone implies. The same chart, with the top label running off the
;; end of its axis and cut there:

(-> tickets-by-violation
    (pj/lay-bar :tickets :violation)
    (pj/lay-label :tickets :violation {:text :tickets})
    (pj/options {:fit-text-domain false}))

;; The two axes carry that difference. Reading the upper end of each:

(let [top-end (fn [opts]
                (-> tickets-by-violation
                    (pj/lay-bar :tickets :violation)
                    (pj/lay-label :tickets :violation {:text :tickets})
                    (pj/options opts)
                    pj/plan
                    :panels
                    first
                    :x-domain
                    second))]
  {:fitted (top-end {})
   :unfitted (top-end {:fit-text-domain false})})

(kind/test-last
 [(fn [m] (> (:fitted m) (:unfitted m)))])

;; Drop the labels and the two agree again, because an axis only grows for
;; text that is on it:

(let [top-end (fn [opts]
                (-> tickets-by-violation
                    (pj/lay-bar :tickets :violation)
                    (pj/options opts)
                    pj/plan
                    :panels
                    first
                    :x-domain
                    second))]
  {:fitted (top-end {})
   :unfitted (top-end {:fit-text-domain false})})

(kind/test-last
 [(fn [m] (= (:fitted m) (:unfitted m)))])

;; A domain you set yourself with `pj/scale` is never widened. Extending
;; it past the number you wrote would contradict the setting, so leaving
;; room for the labels is yours to do -- the domain that comes back is
;; the one asked for, to the digit:

(-> tickets-by-violation
    (pj/lay-bar :tickets :violation)
    (pj/lay-label :tickets :violation {:text :tickets})
    (pj/scale :x {:domain [0 500000]})
    pj/plan
    :panels
    first
    :x-domain)

(kind/test-last
 [(fn [d] (= [0 500000] d))])

;; ## Composing an annotation
;;
;; The pieces combine. A callout naming one observation is four layers: a
;; marker on the point being discussed, a dotted line running from the
;; note to it, the note itself at values rather than columns, and the
;; original data behind them in grey.

(-> (rdatasets/datasets-mtcars)
    (pj/lay-point :wt :mpg {:color "#bbbbbb"})
    (pj/lay-point {:data {:wt [5.25] :mpg [10.4]}
                   :x :wt :y :mpg :color "#cc3311" :size 6})
    (pj/lay-line {:data {:wt [4.3 5.15] :mpg [13.5 10.8]}
                  :x :wt :y :mpg
                  :color "#777777" :stroke-dash :dotted})
    (pj/lay-text {:x 4.25 :y 13.7 :align-x :right :offset-x -4
                  :color "#333333"
                  :text "heaviest car in the set"}))

(kind/test-last
 [(fn [fr] (some #{"heaviest car in the set"}
                 (:texts (pj/svg-summary (pj/plot fr)))))])

;; The [Cookbook](./plotje_book.cookbook.html) collects more of these
;; under Annotated Charts: reference lines and bands, labels on the lines
;; in place of a legend, and a few named series over many pale ones.

;; ## What this chapter does not cover
;;
;; Marks that overlap because of what the data says, rather than because
;; of how a label is drawn, are moved by a **position adjustment** --
;; `:dodge`, `:stack`, `:fill`, and the related `:jitter`. Those shift
;; marks relative to each other by a rule, before the scales, and they
;; are covered in [Layer Types](./plotje_book.layer_types.html) and
;; [Core Concepts](./plotje_book.core_concepts.html). An offset, by
;; contrast, moves a whole layer by one distance on the page.

;; ## See Also
;;
;; - [**Customization**](./plotje_book.customization.html) -- how a mark looks, once it is placed: text weight, background boxes, palettes, themes
;; - [**Cookbook**](./plotje_book.cookbook.html) -- Annotated Charts, and the label recipes
;; - [**Glossary**](./plotje_book.glossary.html) -- canvas, panel box, drawing area, data space, drawing space
;; - [**Known Limitations**](./plotje_book.known_limitations.html) -- what placement still cannot do

;; ## What's Next
;;
;; - [**Faceting**](./plotje_book.faceting.html) -- split any chart into panels by one or two variables
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
