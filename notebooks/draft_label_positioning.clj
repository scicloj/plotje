;; # Draft: label positioning
;;
;; A walk through what the `annotations` branch adds, written for the
;; people in the Clojurians Zulip thread
;; [#plotje > label positioning](https://clojurians.zulipchat.com/#narrow/channel/610149-plotje/topic/label.20positioning/):
;; Carsten Behring, Adrian Smith, generateme and Timothy Pratley.
;;
;; Each section starts from something asked in that thread and shows what
;; the branch does about it. The last section lists the asks it does not
;; answer, which are as much a part of the picture.
;;
;; This notebook is exploratory -- it is not in `chapters.edn` and is not
;; part of the published book.

(ns draft-label-positioning
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]))

;; ## The overplotting that started the thread
;;
;; Carsten, 27 June: "In data coordinates text and point plot at the same
;; position, but for a perfect labeling visualisation, the text needs to
;; go a few pixel to the right (which is not expressible in data units)."
;;
;; Six cars from `mtcars`, each point labelled with its name. The text is
;; drawn at the same x and y as the point, so it starts on top of it:

(def cars
  (-> (rdatasets/datasets-mtcars)
      (tc/select-rows (range 0 30 5))
      (tc/select-columns [:rownames :wt :mpg :disp])))

(-> cars
    (pj/lay-point :wt :mpg {:size 5})
    (pj/lay-text {:text :rownames})
    (pj/options {:title "Text and point at the same coordinates"
                 :width 620 :height 380}))

;; ## Why a nudge is not the tool
;;
;; Carsten, the same day: "nudging by data coordinates (and not pixel
;; coordinates) is not super useful. In reality the amount of pixel
;; nudging I want / need depends on the size of the point drawn."
;;
;; `:nudge-x` shifts by a data amount, and on this axis 0.08 of a
;; thousand pounds happens to be about the width of the marker:

(-> cars
    (pj/lay-point :wt :mpg {:size 5})
    (pj/lay-text {:text :rownames :nudge-x 0.08})
    (pj/options {:title "Weight on x: a nudge of 0.08 clears the marker"
                 :width 620 :height 380}))

;; Nothing carries that number to another axis. Displacement runs from 79
;; to 460 rather than 1.9 to 5.4, so the identical nudge moves the text
;; by a third of a screen unit and the overplotting is untouched:

(-> cars
    (pj/lay-point :disp :mpg {:size 5})
    (pj/lay-text {:text :rownames :nudge-x 0.08})
    (pj/options {:title "Displacement on x: the same 0.08 does nothing"
                 :width 620 :height 380}))

;; On a categorical axis there is no data amount to shift by at all.
;; Rather than guess, `:nudge-x` says so:

(try
  (-> {:species ["setosa" "versicolor" "virginica"] :pct [33.3 31.0 35.7]}
      (pj/lay-bar :species :pct)
      (pj/lay-text {:text :pct :nudge-x -0.2})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #":nudge-x .* categorical x axis" msg))])

;; ## The answer: `:offset-x` and `:offset-y`
;;
;; A new pair of layer options, applied after the scales run, measured in
;; **drawing units** -- one unit of the plot's `:width` and `:height`.
;; That is the unit the thread was calling a pixel, renamed because it is
;; a pixel only at the plot's natural size on a standard-resolution
;; screen.
;;
;; An offset is a distance on the page, so one number clears the marker
;; on either axis. Here it is on the displacement axis that defeated the
;; nudge:

(-> cars
    (pj/lay-point :disp :mpg {:size 5})
    (pj/lay-text {:text :rownames :offset-x 10 :align-x :left})
    (pj/options {:title "Offset by 10 drawing units, whatever the axis holds"
                 :width 620 :height 380}))

;; Every layer type takes them, including the categorical case where a
;; nudge throws:

(-> {:species ["setosa" "versicolor" "virginica"] :pct [33.3 31.0 35.7]}
    (pj/lay-bar :species :pct)
    (pj/lay-text {:text :pct :offset-y -12})
    (pj/options {:title "An offset works on a categorical axis"
                 :width 620 :height 340}))

(kind/test-last
 [(fn [v] (= 3 (:polygons (pj/svg-summary v))))])

;; The axis widens to keep an offset label whole, so a label pushed
;; toward the panel edge is not cut off by the offset that moved it.
;;
;; What an offset does not do is keep labels off each other. Adrian, 28
;; June: "you may end up with overlapping labels depending on how dense
;; the data plot is." Two cars of similar weight still collide above --
;; that is the repel problem, and it is in the last section.

;; ## Where is this point on the page?
;;
;; Timothy, 28 July: "I'm wondering if there is a way to calculate the
;; data point viewbox coordinates?"
;;
;; Adrian, 30 June: "it would be better to get layout information from
;; plotje and then use membrane to directly overlay whatever else you
;; need."
;;
;; `pj/frames` answers both. It reports where each panel sits on the
;; canvas, in drawing units, as plain data:

(def scatter
  (-> cars
      (pj/lay-point :wt :mpg)
      (pj/options {:width 620 :height 380})))

(-> scatter pj/frames kind/pprint)

;; Three rectangles, all measured in drawing units from the top left of
;; the image: the **canvas** is the whole image, the **panel box** is one
;; panel with its axis margin, and the **drawing area** is the shaded
;; background inside that margin. A composite reports one entry per
;; panel, each with its `:row` and `:col`, and the canvas once for the
;; whole plot.
;;
;; `pj/to-drawing` maps a data point onto the page and `pj/to-data` maps
;; back -- the direction an interaction needs, to say what value is under
;; the pointer:

(let [panel (-> scatter pj/frames :panels first)]
  {:mazda-rx4-at (pj/to-drawing panel 2.62 21.0)
   :under-the-pointer (pj/to-data panel 300 200)
   :round-trip (->> (pj/to-drawing panel 2.62 21.0)
                    (apply pj/to-data panel))})

(kind/test-last
 [(fn [m] (every? true?
                  (map #(< (abs (- %1 %2)) 1e-9)
                       (:round-trip m) [2.62 21.0])))])

;; Both take a dataset of coordinates as well as a single point, with
;; `:x` and `:y` columns, and answer with a dataset shaped the same way.
;; That arity maps whole columns and builds the panel's scales once:

(pj/to-drawing (-> scatter pj/frames :panels first)
               {:x [2.62 3.44 5.25] :y [21.0 18.1 10.4]})

(kind/test-last [(fn [ds] (= 3 (tc/row-count ds)))])

;; A dataset rather than a collection of pairs because the two
;; coordinates of a point share one index space, which a dataset states
;; and two loose sequences only promise.
;;
;; A panel reports whether the inverse exists at all in `:invertible?`;
;; under `:coord :polar` it does not, and `pj/to-data` throws rather than
;; returning a wrong answer.
;;
;; This is what the `:px-grid` debug layer in the thread was standing in
;; for: the numbers are available without reading them off a rendered
;; grid by eye.

;; ## A note with no row in the data
;;
;; Carsten, 30 July, citing
;; [rfortherestofus](https://rfortherestofus.com/2023/10/annotate-vs-geoms):
;; ggplot's `annotate` is deliberately not a geom.
;;
;; `:x` and `:y` may now be given as a value rather than a column, so a
;; note needs no invented dataset. On a layer that brings no data, a
;; string `:text` is the text itself rather than a column name:

(-> cars
    (pj/lay-point :wt :mpg)
    (pj/lay-text {:x 4.5 :y 30 :text "light and thirsty" :color "#cc3311"})
    (pj/options {:title "A note placed at one x and one y"
                 :width 620 :height 380}))

(kind/test-last
 [(fn [v] (some #{"light and thirsty"} (:texts (pj/svg-summary v))))])

;; The note is placed *in the data*: 4.5 is a weight, and the axis holds
;; it like any other value, widening if the note sits beyond the points.
;; ggplot's `annotate` does the same thing -- it builds a one-row data
;; frame behind the scenes.

;; ## The same value on every row
;;
;; Give one coordinate as a value and the other as a column, and the
;; value repeats for every row. That is how a label at one fixed x is
;; written -- the names line up in a stack instead of scattering with
;; the points:

(-> cars
    (pj/lay-point :wt :mpg)
    (pj/lay-text {:x 5.6 :y :mpg :text :rownames :offset-x 6})
    (pj/options {:title "One x for every row"
                 :width 620 :height 380}))

(kind/test-last
 [(fn [v] (every? (set (:texts (pj/svg-summary v)))
                  ["Mazda RX4" "Valiant" "Merc 280C"]))])

;; ## Off the data entirely
;;
;; Carsten, 29 July, listing what should be possible:
;;
;; - position via data coordinates
;; - position via data coordinates plus a shift on the page
;; - position via page coordinates
;;
;; The first two are the sections above. The third is `:in`, which names
;; the space a layer's `:x` and `:y` are in. `:in :drawing-area` measures
;; drawing units from the top left of the panel background, so the mark
;; lands on the panel and the axis domains are left alone.
;;
;; A corner other than the top left needs the panel's size, which is what
;; `pj/frames` reports. Measure the pose you are going to draw: anything
;; that changes the layout changes the numbers. Adding a title shortens
;; the drawing area, so a caption placed from frames taken before the
;; title lands below the panel and is clipped away -- which is how the
;; first draft of this section went wrong.

(def captioned
  (-> cars
      (pj/lay-point :wt :mpg)
      (pj/options {:title "A caption in the panel's bottom left corner"
                   :width 620 :height 380})))

(let [[_ _ _ h] (-> captioned pj/frames :panels first :frames :drawing-area)]
  (pj/lay-text captioned {:in :drawing-area :x 12 :y (- h 16)
                          :text "n = 6" :align-x :left :color "#555555"}))

(kind/test-last
 [(fn [v] (some #{"n = 6"} (:texts (pj/svg-summary v))))])

;; The caption itself is safe to add after measuring. A drawing-space
;; layer takes no part in the domains and asks for no margin, so the
;; drawing area it was placed in is the one it lands in:

(let [drawing-area #(-> % pj/frames :panels first :frames :drawing-area)]
  {:before (drawing-area captioned)
   :after  (drawing-area (pj/lay-text captioned {:in :drawing-area
                                                 :x 12 :y 20 :text "n = 6"}))})

(kind/test-last [(fn [m] (= (:before m) (:after m)))])

;; The distinction that has to hold: a value in data space is a datum and
;; trains the axis; a number in drawing space is a page measurement and
;; must not. The same numbers, read in the two spaces, land in different
;; places, and only the data-space one moves the axis:

(let [in-data    (pj/lay-text scatter {:x 12 :y 12 :text "x"})
      in-drawing (pj/lay-text scatter {:in :drawing-area :x 12 :y 12 :text "x"})
      x-domain   #(:x-domain (first (:panels (pj/plan %))))]
  {:no-note (x-domain scatter)
   :note-in-data (x-domain in-data)
   :note-in-drawing (x-domain in-drawing)})

(kind/test-last
 [(fn [m] (and (= (:no-note m) (:note-in-drawing m))
               (not= (:no-note m) (:note-in-data m))))])

;; ## A leader line
;;
;; Timothy, 5 August: "I think I can achieve most of my labeling needs by
;; providing a different dataset of coordinates with a translation applied
;; to the point ... Additionally I think a small line from the label to
;; the point helps a lot."
;;
;; There is no single option for this yet. It is a composition: a marker
;; on the point being discussed, a dotted line from the note to it, and
;; the note itself at a position given as values.

(-> (rdatasets/datasets-mtcars)
    (pj/lay-point :wt :mpg {:color "#bbbbbb"})
    (pj/lay-point {:data {:wt [5.25] :mpg [10.4]}
                   :x :wt :y :mpg :color "#cc3311" :size 6})
    (pj/lay-line {:data {:wt [4.3 5.15] :mpg [13.5 10.8]}
                  :x :wt :y :mpg
                  :color "#777777" :stroke-dash :dotted})
    (pj/lay-text {:x 4.25 :y 13.7 :align-x :right :offset-x -4
                  :color "#333333"
                  :text "heaviest car in the set"})
    (pj/options {:title "Callout with a leader line"
                 :width 620 :height 380}))

(kind/test-last
 [(fn [v] (some #{"heaviest car in the set"} (:texts (pj/svg-summary v))))])

;; ## A pale supporting cast
;;
;; An annotated chart usually names a few series and leaves the rest as
;; background. That needs one grouping column and one fixed color, which
;; used to be impossible -- the grouping won and every line took a
;; different palette color. It works now:

(-> (rdatasets/gapminder-gapminder)
    (tc/select-rows #(= "Europe" (:continent %)))
    (pj/lay-line :year :life-exp {:group :country :color "#d8d8d8"})
    (pj/lay-line {:data (-> (rdatasets/gapminder-gapminder)
                            (tc/select-rows #(= "Poland" (:country %))))
                  :x :year :y :life-exp :color "#cc3311"})
    (pj/lay-text {:x 2007 :y 75.563 :text "Poland"
                  :offset-x 8 :align-x :left :color "#cc3311"})
    (pj/options {:title "One country named, the rest as context"
                 :width 620 :height 380 :legend-position :none}))

(kind/test-last
 [(fn [v] (some #{"Poland"} (:texts (pj/svg-summary v))))])

;; ## What this does not answer
;;
;; **Fractional positions on a categorical axis.** Carsten, 28 and 30
;; July: "if the data domain is 1 / 2 / 3 and of type categorical, we
;; cannot have text at coordinate 2.5", where ggplot places categories at
;; 1, 2, 3 and accepts 1.2 or 1.5 between them. Still true. Plotje's
;; categorical axis is a band scale, which answers `nil` for a value
;; between two bands, so there is nothing to draw at. `:offset-x` covers
;; the case where the reason for wanting 2.5 was to clear a mark; it does
;; not cover wanting a place genuinely between two categories. Changing
;; it means making a categorical axis a continuous scale carrying a label
;; table, which is designed but not built.
;;
;; **Repel.** generateme, 30 June, pointing at `ggrepel`; Adrian's R-tree
;; sketch of 28 June solves the same problem a different way. Nothing
;; here does it, and the collisions in the first two plots above are what
;; it would fix. An offset is the manual version of what repel
;; automates, so the channel it would need now exists.
;;
;; **Arrows.** Carsten, 30 June: "arrowed lines and dashed lines and
;; dashed arrowed lines would be nice". Dashed lines shipped in 0.5.0 --
;; the leader line above is `:stroke-dash :dotted`. Arrowheads are
;; [issue #17](https://github.com/scicloj/plotje/issues/17) and are not
;; started.
;;
;; **`:in :canvas`.** `:in` takes `:drawing-area` only. Placing a mark
;; against the whole image, outside any panel, needs an insertion point
;; that escapes the panel's translate and clip.
;;
;; **The label defects Timothy reported on 5 August.** A missing entry
;; still draws an empty background box -- three boxes here for two
;; labels:

(-> {:x [1 2 3] :y [5 3 4] :tag [nil "hi" "there"]}
    (pj/lay-line :x :y)
    (pj/lay-label :x :y {:text :tag})
    (pj/options {:width 620 :height 380}))

(kind/test-last
 [(fn [v] (= 3 (:label-boxes (pj/svg-summary v))))])

;; Two of the three complaints in that message are answerable today:
;; `:offset-y` moves the box clear of the line it sits on, and
;; `:font-size` answers "the text is tiny". The empty box for a `nil` tag
;; is a defect, and drawing nothing there is the fix:

(-> {:x [1 2 3] :y [5 3 4] :tag [nil "hi" "there"]}
    (pj/lay-line :x :y)
    (pj/lay-label :x :y {:text :tag :offset-y -14 :font-size 13})
    (pj/options {:width 620 :height 380}))
