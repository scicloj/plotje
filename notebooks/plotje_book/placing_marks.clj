;; # Placing Marks
;;
;; This chapter is about where a mark is drawn, and about the units the
;; numbers that place it are measured in.
;;
;; A scale turns each data value into a position on the page, and for most
;; plots that is all the placement anyone needs to think about. Three
;; situations call for more control:
;;
;; - a label that would otherwise be drawn on top of the point it names
;; - a note that belongs at a position no row of the data holds
;; - code that needs to know where a value was drawn, so that it can draw
;;   something else beside it
;;
;; Most of what follows works on any layer type. Two things here are
;; text-only, and both are marked where they appear: the anchor options
;; `:align-x` and `:align-y`, and the `:fit-text-domain` setting that
;; makes room for a label at the end of an axis. Text runs into placement
;; problems first, because a label is drawn at the coordinates of another
;; mark and takes up room of its own.

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
;; The number 12 does not say where a mark goes until you know what it
;; measures. Plotje measures positions in two spaces.
;;
;; **Data space** holds values in their original units -- a sepal length
;; of 6.5, a year of 2007, the category `"setosa"`. **Drawing space**
;; holds distances on the page, measured in **drawing units** from the
;; top left, where one drawing unit is one unit of the plot's `:width`
;; and `:height`. The scales map the first onto the second.
;;
;; Three rectangles are named in drawing space, and the
;; [Glossary](./plotje_book.glossary.html#canvas) defines all three. The
;; **canvas** is the whole image. A **panel box** is one panel, including
;; its axis margin. The **drawing area** is the shaded background inside
;; that margin, where the marks are drawn.
;;
;; Every option in this chapter takes its numbers in one of the two
;; spaces, and each section says which.

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
;; ggplot2 makes the same choice with `hjust` and `vjust`, which take
;; numbers between 0 and 1. Here it is made with named values.
;;
;; The value names the part of the text placed at the point. `:align-x
;; :right` pins the right edge, so the label extends leftward; `:align-x
;; :center` straddles the point. `:align-y` reads in data orientation:
;; `:top` pins the top edge, so the text reads downward from the point,
;; and `:bottom` pins the bottom edge, so the text floats above it.
;;
;; The plot below draws three points at the same hour and labels each one
;; with a different `:align-x`. The first label starts at its point and
;; runs to the right, the second is centered on its point, and the third
;; ends at its point:

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

;; `pj/lay-text` and `pj/lay-label` accept both options and every one of
;; their values. A value that is not one of the three is rejected when the
;; plan is built, rather than being ignored.

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

;; `:align-x` and `:align-y` are the only *layer* options in this chapter
;; that apply to text alone. Passing one to `pj/lay-point` prints a
;; warning that names the layer types accepting it, and the option is
;; dropped. (`:fit-text-domain`, further down, is also about text, but it
;; is a plot option rather than a layer option.)

;; ## Shifting a mark by a distance on the page
;;
;; Anchoring places one edge of the label exactly on the point, so a
;; point drawn with a radius still overlaps it. Clearing the mark takes a
;; shift of a few units on the page, which is not a data quantity: it
;; depends on the size the mark is drawn at, not on what the axis
;; measures.
;;
;; `:dx` cannot do this, because it shifts the data value itself,
;; before the scales run.
;;
;; The examples that follow use six cars from `mtcars`, with their
;; weights in thousands of pounds and their displacements in cubic
;; inches:

(def cars
  (-> (rdatasets/datasets-mtcars)
      (tc/select-rows (range 0 30 5))
      (tc/select-columns [:rownames :wt :mpg :disp])))

cars

(kind/test-last
 [(fn [ds] (and (= 6 (tc/row-count ds))
                (= [1.935 5.424] [(apply min (ds :wt)) (apply max (ds :wt))])
                (= [79.0 460.0] [(apply min (ds :disp)) (apply max (ds :disp))])))])

;; Here each car is labelled with its name, and the labels are shifted
;; along the weight axis by `:dx`. A `:dx` of 0.08 happens to be about
;; the width of a marker on this axis, so it clears them:

(-> cars
    (pj/lay-point :wt :mpg {:size 5})
    (pj/lay-text {:text :rownames :dx 0.08}))

(kind/test-last
 [(fn [fr]
    (let [panel (-> fr pj/frames :panels first)
          at    #(first (pj/to-drawing panel % 20.0))]
      ;; Wider bounds than the bare scatter would give: this pose carries
      ;; the labels, so its axis was widened to make room for them.
      (< 8.0 (- (at 2.08) (at 2.0)) 11.0)))])

;; The same number does not work on a different axis. Displacement spans
;; 79 to 460 where weight spans about 1.9 to 5.4, so a `:dx` of 0.08
;; moves the text by about a tenth of a drawing unit. The labels are back
;; on top of their markers:

(-> cars
    (pj/lay-point :disp :mpg {:size 5})
    (pj/lay-text {:text :rownames :dx 0.08}))

(kind/test-last
 [(fn [fr]
    (let [panel (-> fr pj/frames :panels first)
          at    #(first (pj/to-drawing panel % 20.0))]
      (and (= [79.0 460.0]
              [(reduce min (cars :disp)) (reduce max (cars :disp))])
           (= [1.935 5.424]
              [(reduce min (cars :wt)) (reduce max (cars :wt))])
           (< (- (at 79.08) (at 79.0)) 0.2))))])

;; `:offset-x` and `:offset-y` shift a layer by a number of drawing units
;; after the scales have run. Because the number is a distance on the
;; page, the same one works on any axis. Here `:offset-x 10` clears the
;; markers on the displacement axis that a `:dx` could not clear:

(-> cars
    (pj/lay-point :disp :mpg {:size 5})
    (pj/lay-text {:text :rownames :offset-x 10}))

(kind/test-last
 [(fn [fr]
    (= [nil 10]
       (->> fr pj/plan :panels first :layers (mapv :offset-x))))])

;; Every layer type accepts these two options. `:offset-y -6` lifts each
;; value label six drawing units above its bar -- negative, because a
;; positive `:offset-y` moves down the page, as drawing coordinates do.
;; The categories are on x here, and an offset works on that axis just
;; as it does on a numeric one:

(-> {:team ["red" "green" "blue"] :score [3 5 4]}
    (pj/lay-bar :team :score)
    (pj/lay-text {:text :score :align-x :center :offset-y -6}))

(kind/test-last
 [(fn [fr]
    (= [nil -6]
       (->> fr pj/plan :panels first :layers (mapv :offset-y))))])

;; A `:dx` along a categorical axis is a fraction of a band. The
;; categories carry no numbers of their own, so the amount is counted in
;; bands from wherever the mark sits: `0.5` is half a band along, which
;; is halfway to the next category. The note below is shifted that far
;; from the bar it names, so it sits over the gap beside the bar rather
;; than above it. The bars take a light fill, because a label that
;; crosses one has to stay readable against it -- the other way round is
;; to give the text its own background, which
;; [Customization](./plotje_book.customization.html#text-on-a-background-box)
;; covers and `pj/lay-label` does by default:

(-> {:team ["red" "green" "blue"] :score [3 5 4]}
    (pj/lay-bar :team :score {:color "#a6cee3"})
    (pj/lay-text {:x {:value "red"} :y 3 :align-x :center
                  :dx 0.5 :offset-y -10 :text "half a band along"}))

(kind/test-last
 [(fn [fr]
    (and (= [nil 0.5]
            (->> fr pj/plan :panels first :layers (mapv :dx)))
         ;; Half a band along from a category is the place halfway to the
         ;; next one, so a `:dx` draws what writing that place draws. The
         ;; picture above shifts every row from its own bar, which no one
         ;; written place matches, so the two spellings are held together
         ;; here on a single label.
         (= (pj/plot (-> {:team ["red" "green" "blue"] :score [3 5 4]}
                         (pj/lay-bar :team :score)
                         (pj/lay-text {:x {:value "red"} :dx 0.5
                                       :y 4.5 :text "note"})))
            (pj/plot (-> {:team ["red" "green" "blue"] :score [3 5 4]}
                         (pj/lay-bar :team :score)
                         (pj/lay-text {:x 1.5 :y 4.5 :text "note"}))))
         ;; And from the second category, so it is the step that is half
         ;; a band rather than the first band being special.
         (= (pj/plot (-> {:team ["red" "green" "blue"] :score [3 5 4]}
                         (pj/lay-bar :team :score)
                         (pj/lay-text {:x {:value "green"} :dx 0.5
                                       :y 4.5 :text "note"})))
            (pj/plot (-> {:team ["red" "green" "blue"] :score [3 5 4]}
                         (pj/lay-bar :team :score)
                         (pj/lay-text {:x 2.5 :y 4.5 :text "note"}))))))])

;; One more difference between the two: a `:dx` does not change the axis
;; domain, so a `:dx` large enough to carry a mark past the end of the
;; axis leaves it clipped there. ggplot2's `nudge_x` widens the range
;; instead. The [Glossary](./plotje_book.glossary.html#shift) entry for
;; Shift describes that difference. A number written in the slot itself is
;; answered the other way round -- Giving `:x` and `:y` as values, the
;; next section, shows both readings.
;;
;; An offset does not keep labels from overlapping each other. It moves a
;; whole layer by one amount, so two labels at nearby values stay as
;; close together as they were. Moving them apart individually is not
;; something Plotje does yet -- see
;; [Known Limitations](./plotje_book.known_limitations.html#layout-and-visuals).

;; ## Giving `:x` and `:y` as values
;;
;; A note in a corner, a caption beside a peak, or a marker on one
;; interesting observation has no row in the dataset. `:x` and `:y` may
;; be given as a value rather than as a column, the way `:color`, `:size`
;; and `:alpha` have always allowed -- `{:color :species}` beside
;; `{:color "red"}`.
;;
;; Give both as values and the layer draws a single mark. A string
;; `:text` naming no column is the text itself -- here there is no data
;; holding a column for it to name, and on a layer that has data it
;; labels every row with it:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-text {:x 7.5 :y 4.2 :text "outliers up here"}))

(kind/test-last
 [(fn [fr] (some #{"outliers up here"}
                 (:texts (pj/svg-summary (pj/plot fr)))))])

;; The note is placed *in the data*: 7.5 is a sepal length, and the axis
;; treats it like any other sepal length, widening to include it if the
;; note falls beyond the points. ggplot2's `annotate()` works the same
;; way, building a one-row data frame out of the values it is given.
;;
;; Give one coordinate as a value and the other as a column, and the
;; value is used for every row. This is how a label at one fixed x is
;; written. Below, all six names are drawn at x 5.6, each at the mpg of
;; its own row, so they line up in a column instead of following the
;; points:

(-> cars
    (pj/lay-point :wt :mpg)
    (pj/lay-text {:x 5.6 :y :mpg :text :rownames :offset-x 6}))

(kind/test-last
 [(fn [fr] (every? (set (:texts (pj/svg-summary (pj/plot fr))))
                   ["Mazda RX4" "Valiant" "Merc 280C"]))])

;; On a categorical axis the same value is read differently, because the
;; axis cannot widen to reach it -- its categories are its whole extent.
;; A number there is a place among the categories, counted from one: `1`
;; is the first category, `1.5` sits halfway to the second, and on three
;; categories the axis runs from `0.5` to `3.5`. So the note below lands
;; between the first bar and the second, and adds no category of its own:

(-> {:team ["red" "green" "blue"] :score [3 5 4]}
    (pj/lay-bar :team :score {:color "#a6cee3"})
    (pj/lay-point {:x 1.5 :y 4.5 :size 6 :color "#cc3311"})
    (pj/lay-text {:x 1.5 :y 4.5 :align-x :center :offset-y -10
                  :text "between two teams"}))

(kind/test-last
 [(fn [fr]
    (let [panel (-> fr pj/plan :panels first)
          frame (-> fr pj/frames :panels first)
          at (fn [v] (first (pj/to-drawing frame v 4.5)))]
      (and (= ["red" "green" "blue"] (:x-domain panel))
           (= ["red" "green" "blue"] (:values (:x-ticks panel)))
           ;; Counted from one: 1 is the first category and 2 the second.
           (= (at 1) (at "red"))
           (= (at 2) (at "green"))
           ;; So 1.5 is the midpoint of those two, which is what "lands
           ;; between the first bar and the second" means.
           (< (abs (- (at 1.5) (/ (+ (at "red") (at "green")) 2.0))) 1e-9)
           ;; And on three categories the axis runs from 0.5 to 3.5: both
           ;; ends answer, and anything past either is refused.
           (number? (at 0.5))
           (number? (at 3.5))
           (every? (fn [bad]
                     (try (at bad)
                          false
                          (catch Exception e
                            (boolean (re-find #"past the ends of this axis"
                                              (ex-message e))))))
                   [0.4 3.6]))))])

;; To name a category rather than count to one, write the value in full
;; as `{:value ...}`. That spelling matters most where the categories are
;; themselves numbers, since there the two readings pick different bands
;; -- on an axis of the years 2020, 2021 and 2022, `{:value "2021"}` is
;; the middle band and the bare number `2021` is a place far past the
;; third:

(-> {:cohort [2020 2021 2022] :n [3 5 4]}
    (pj/lay-bar :cohort :n {:x-type :categorical})
    (pj/lay-text {:x {:value "2021"} :y 5.5 :align-x :center
                  :text "the 2021 cohort"}))

(kind/test-last
 [(fn [fr]
    (and (some #{"the 2021 cohort"} (:texts (pj/svg-summary (pj/plot fr))))
         ;; The named category is the middle band, which is place 2 --
         ;; the two readings meet there, and part elsewhere.
         (let [frame (-> fr pj/frames :panels first)]
           (= (first (pj/to-drawing frame "2021" 5.5))
              (first (pj/to-drawing frame 2 5.5))))
         ;; The bare number is the reading this example is not asking
         ;; for, and the axis says so rather than drawing off the panel.
         (try (-> {:cohort [2020 2021 2022] :n [3 5 4]}
                  (pj/lay-bar :cohort :n {:x-type :categorical})
                  (pj/lay-text {:x 2021 :y 5.5 :text "the 2021 cohort"})
                  pj/plan)
              false
              (catch Exception e
                (boolean (re-find #"past the ends of this axis" (ex-message e)))))))])

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

;; This is what the two spaces are for. A number in data space is a data
;; value, so it widens the domain if it falls outside it. A number in
;; drawing space is a measurement of the page, so it must not affect the
;; domain at all. The x domains below come from the same plot three
;; times: without a note, with the note at `{:x 12 :y 12}` in data space,
;; and with it at the same numbers in drawing space. Only the data-space
;; note moves the axis:

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
;; `pj/lay-band-*` take `:in` too, reading their intercept or their
;; bounds as drawing units from the panel background's corner.

;; ## Where a value lands on the page
;;
;; The scales are what connect the two spaces, and `pj/frames` makes that
;; connection available. It reports where each panel sits on the canvas,
;; as plain data, using the same computation the renderer draws with.

(def scatter
  (-> cars
      (pj/lay-point :wt :mpg)
      (pj/options {:width 620 :height 380})))

scatter

(-> scatter pj/frames kind/pprint)

;; The `:canvas` is reported once for the whole plot. `:panels` holds one
;; entry per panel, each carrying its `:row` and `:col`, its domains, and
;; the two rectangles described above. The axis margin is not stored
;; separately; it is the difference between those two rectangles.
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

;; Not every coordinate system can be read backwards. Each panel says
;; whether its own can, in `:invertible?`. Under `:coord :polar` it
;; cannot, because that projection combines x and y into an angle and a
;; radius, and `pj/to-data` throws there rather than returning an answer
;; that would sometimes be wrong.
;;
;; Both functions also accept a dataset of coordinates, with `:x` and
;; `:y` columns, and return a dataset with the same two columns. This
;; form maps whole columns and builds the panel's scales only once, so it
;; is the one to use when placing many positions at a time:

(pj/to-drawing (-> scatter pj/frames :panels first)
               {:x [2.62 3.44 5.25] :y [21.0 18.1 10.4]})

(kind/test-last [(fn [ds] (= [:x :y] (vec (tc/column-names ds))))])

;; The argument is a dataset rather than a collection of `[x y]` pairs
;; because the two coordinates of a point belong to the same row. A
;; dataset records that; two separate sequences would leave it to the
;; caller to keep them in step.

;; These functions can answer the question the `:dx` section left open:
;; how far does a `:dx` of 0.08 actually move a label on each of those
;; two axes? Ask each panel where 0.08 of its own units comes to:

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

;; Both figures are the same `:dx 0.08`, measured on the two axes
;; shown earlier.

;; ## Mapping a categorical axis
;;
;; A categorical axis is a band scale, and `pj/to-drawing` answers two
;; kinds of value on it. A category maps to the middle of its band. A
;; number maps to a place among the categories, counted from one, so a
;; number between two whole ones lands between the bands they name and
;; the axis reaches half a place past each end. What it refuses is a
;; value that is neither -- a category the axis does not carry, or a
;; number past the ends of the axis -- naming the value and the
;; categories it could have been.
;;
;; The two directions are not symmetrical. `pj/to-data` reads back the
;; category whose band holds the coordinate, and answers nil outside
;; every band, so a place between two bands comes back as one of the two
;; categories rather than as the number that produced it -- halfway
;; between two band centres is the edge they share, and an edge reads as
;; the earlier band:

(let [panel (-> {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
                 :tickets   [462389 181444 163294]}
                (pj/lay-bar :tickets :violation)
                pj/frames
                :panels
                first)]
  {:band-middle     (pj/to-drawing panel 200000 "Over Time Limit")
   :read-back       (->> (pj/to-drawing panel 200000 "Over Time Limit")
                         (apply pj/to-data panel))
   :halfway-up      (pj/to-drawing panel 200000 1.5)
   :place-read-back (->> (pj/to-drawing panel 200000 1.5)
                         (apply pj/to-data panel))
   :first-end       (pj/to-drawing panel 200000 0.5)
   :last-end        (pj/to-drawing panel 200000 3.5)
   :outside-a-band  (pj/to-data panel 325.0 5.0)
   :not-a-category  (try (pj/to-drawing panel 200000 "Double Parked")
                         (catch Exception e (ex-message e)))
   :past-the-ends   (try (pj/to-drawing panel 200000 99)
                         (catch Exception e (ex-message e)))})

(kind/test-last
 [(fn [m] (and (= [200000.0 "Over Time Limit"] (:read-back m))
               (nil? (second (:outside-a-band m)))
               (re-find #"Double Parked" (:not-a-category m))
               (re-find #"Meter Expired" (:not-a-category m))
               (re-find #"past the ends of this axis" (:past-the-ends m))
               ;; Three categories, so the axis runs from 0.5 to 3.5 and
               ;; both ends answer with a place on the panel.
               (every? number? (concat (:first-end m) (:last-end m)))
               ;; The place comes back as a category, not as the 1.5
               ;; that produced it. Halfway between two band centres is
               ;; the edge the two bands share, and an edge answers with
               ;; the earlier of them.
               (= "Meter Expired" (second (:place-read-back m)))
               ;; The place is on the categorical axis, which is y here,
               ;; and sits midway between the first band's middle and the
               ;; second's -- the two the number 1.5 falls between.
               (let [mid (fn [v] (second (pj/to-drawing
                                          (-> {:violation ["Meter Expired" "Over Time Limit"
                                                           "Stop Prohibited"]
                                               :tickets   [462389 181444 163294]}
                                              (pj/lay-bar :tickets :violation)
                                              pj/frames :panels first)
                                          200000 v)))]
                 (< (abs (- (second (:halfway-up m))
                            (/ (+ (mid "Meter Expired") (mid "Over Time Limit")) 2.0)))
                    1e-9))))])

;; Both functions take and answer in data order, and `(pj/coord :flip)`
;; does not change that -- the categorical column stays the `:x`
;; argument even once it is drawn up the side. A panel entry's
;; `:x-domain` and `:y-domain` describe the drawn axes, so under a flip
;; those two are the pair that swap:

(let [panel (-> {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
                 :tickets   [462389 181444 163294]}
                (pj/lay-bar :violation :tickets)
                (pj/coord :flip)
                pj/frames
                :panels
                first)]
  {:x-domain  (:x-domain panel)
   :y-domain  (:y-domain panel)
   :read-back (pj/to-data panel (pj/to-drawing panel {:x ["Over Time Limit"]
                                                      :y [200000]}))})

(kind/test-last
 [(fn [m] (and (= ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
                  (:y-domain m))
               (number? (first (:x-domain m)))
               (= ["Over Time Limit"] (vec ((:read-back m) :x)))
               (< (abs (- 200000.0 (first ((:read-back m) :y)))) 1e-6)))])

;; ## Drawing from a measurement
;;
;; The frames report canvas coordinates, measured from the top left of
;; the whole image. A `{:in :drawing-area}` layer measures from the top
;; left of the drawing area instead. The two origins are different, so
;; drawing a mapped position back onto the plot means subtracting the
;; drawing area's corner from it first:

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

;; Measure the pose you are going to draw, not one like it. The frames
;; describe a particular pose, and anything that changes the layout
;; changes them. Adding a title, for instance, shortens the drawing area,
;; so a caption positioned from frames measured before the title was
;; added would land below the panel and be clipped away:

(let [drawing-area #(-> % pj/frames :panels first :frames :drawing-area)]
  {:untitled (drawing-area scatter)
   :titled   (drawing-area (pj/options scatter {:title "Motor Trend Cars"}))})

(kind/test-last
 [(fn [m] (< (last (:titled m)) (last (:untitled m))))])

;; Adding a drawing-space layer after measuring is safe, though. Such a
;; layer takes no part in the domains and asks for no extra margin, so it
;; lands in the same drawing area it was measured against. The caption
;; below goes in the bottom left corner, which requires the panel's
;; height:

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
;; Labelling each bar with its own value is the usual way to let a reader
;; take an exact number off a chart. The label on the longest bar is the
;; one at risk of being cut off. A text mark has a fixed size in drawing
;; units rather than a size measured in data units, so a label anchored
;; at the largest value in the data reaches past the end of the axis
;; whatever range that axis covers, and the panel clips it there. A count
;; in the hundreds of thousands can end up reading as its first four
;; digits.
;;
;; To avoid that, the axis is widened by however much the text needs.
;; This happens by default. The amount comes from the labels themselves,
;; so an axis only grows when something drawn on it would otherwise be
;; cut off:

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

;; Remove the labels and the two settings agree again, because an axis is
;; only widened for text that is actually drawn on it:

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
;; it past the number you wrote would contradict what you asked for, so
;; making room for the labels becomes your job. The domain that comes
;; back is exactly the one requested:

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
;; The options in this chapter combine. A callout that names a single
;; observation is built from four layers: the whole dataset in grey, a
;; marker on the point being discussed, a dotted line running from the
;; note to that marker, and the note itself, positioned with values
;; rather than columns.

(-> (rdatasets/datasets-mtcars)
    (pj/lay-point :wt :mpg {:color "#bbbbbb"})
    (pj/lay-point {:data {:wt [5.424] :mpg [10.4]}
                   :x :wt :y :mpg :color "#cc3311" :size 6})
    (pj/lay-line {:data {:wt [4.3 5.32] :mpg [13.5 10.8]}
                  :x :wt :y :mpg
                  :color "#777777" :stroke-dash :dotted})
    (pj/lay-text {:x 4.25 :y 13.7 :align-x :right :offset-x -4
                  :color "#333333"
                  :text "heaviest car in the set"}))

(kind/test-last
 [(fn [fr]
    (and (some #{"heaviest car in the set"}
               (:texts (pj/svg-summary (pj/plot fr))))
         ;; The marker is on the heaviest car, not merely near it.
         (= 5.424 (apply max ((rdatasets/datasets-mtcars) :wt)))))])

;; The [Cookbook](./plotje_book.cookbook.html#annotated-charts) collects more of these
;; under Annotated Charts: reference lines and bands, labels on the lines
;; in place of a legend, and a few named series over many pale ones.

;; ## What this chapter does not cover
;;
;; Sometimes marks overlap because of what the data says, rather than
;; because of how a label is drawn -- two bars in the same category, for
;; example. Those are separated by a **position adjustment**: `:dodge`,
;; `:stack`, `:fill`, and the related `:jitter`. A position adjustment
;; moves marks relative to each other by a rule, before the scales run,
;; and the [Layer Types](./plotje_book.layer_types.html#positions) and
;; [Core Concepts](./plotje_book.core_concepts.html#mark-stat-and-position) chapters cover them.
;; An offset is different: it moves a whole layer by one fixed distance
;; on the page.

;; ## See Also
;;
;; - [**Customization**](./plotje_book.customization.html) -- how a mark looks, once it is placed: text weight, background boxes, palettes, themes
;; - [**Cookbook**](./plotje_book.cookbook.html#annotated-charts) -- Annotated Charts, and the label recipes
;; - [**Glossary**](./plotje_book.glossary.html#data-space) -- canvas, panel box, drawing area, data space, drawing space
;; - [**Known Limitations**](./plotje_book.known_limitations.html) -- what placement still cannot do

;; ## What's Next
;;
;; - [**Faceting**](./plotje_book.faceting.html) -- split any chart into panels by one or two variables
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
