;; # Specifying Aesthetics
;;
;; Every mapping value has to answer two questions before anything can
;; be drawn. Where does the value come from -- a column of the data, or
;; the value written there? And does it pass through the aesthetic's
;; scale, or is it what gets drawn?
;;
;; The questions are independent, so there are four answers, and all
;; four are things people write. ggplot2 has the same four and reaches
;; them through different syntax:
;;
;; | | passes through the scale | drawn as it stands |
;; |:--|:--|:--|
;; | **a written value** | `{:size {:value 7 :scale true}}` -- `aes(size=7)` | `{:size 7}` -- `geom_point(size=7)` |
;; | **a column** | `{:size :weight}` -- `aes(size=weight)` | `{:size {:column :r :scale false}}` -- `scale_size_identity()` |
;;
;; Most of the time the conventions pick the cell you meant and you
;; write the short form. This chapter is about what those conventions
;; are, and how to say the other thing when they guess wrong.

(ns plotje-book.specifying-aesthetics
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; Four plants, measured. `:shade` holds colors and `:species` holds
;; categories, which is the distinction the second question turns on.

(def plants
  {:height  [12 25 18 31]
   :weight  [1.4 3.9 2.2 4.6]
   :species ["fern" "moss" "fern" "ivy"]
   :shade   ["#CC3311" "#0077BB" "#CC3311" "#009988"]})

plants

;; ## Where the value comes from
;;
;; The layer's data decides. A value naming one of its columns is a
;; column reference; anything else is the value itself. The same two
;; questions are asked of a keyword and of a string, and matching is
;; exact -- `"species"` finds a string-named column, `:species` finds a
;; keyword-named one. The
;; [Inference Rules](./plotje_book.inference_rules.html#color-literal-vs-column-reference)
;; chapter works through that resolution in full.
;;
;; What matters here is the one case it cannot settle: a dataset whose
;; column is named after a color.

(def named-after-a-colour
  {:height [12 25]
   :weight [1.4 3.9]
   "blue"  ["p" "q"]})

;; Writing the mapping out in full says which reading you meant.
;; `{:value "blue"}` is the color:

(-> named-after-a-colour
    (pj/lay-point :height :weight {:color {:value "blue"}}))

(kind/test-last
 [(fn [fr] (= [[0.0 0.0 1.0 1.0]]
              (->> (pj/plan fr) :panels first :layers first :groups
                   (mapv :color))))])

;; and `{:column "blue"}` is the column, which splits the layer in two
;; and earns a legend:

(-> named-after-a-colour
    (pj/lay-point :height :weight {:color {:column "blue"}}))

(kind/test-last
 [(fn [fr] (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))])

;; ## Whether it passes through the scale
;;
;; A scale turns data into something visible: a category into a color,
;; a number into a radius, a measurement into a place on an axis. The
;; second question is whether this value is data on its way through
;; that machinery, or already the visible thing.
;;
;; ### A column always passes through
;;
;; Whatever it holds. `:shade` holds nothing but colors, and it is
;; still four rows of data, so it becomes categories and takes palette
;; colors like any other column:

(-> plants
    (pj/lay-point :height :weight {:color :shade}))

(kind/test-last
 [(fn [fr] (= 3 (count (:entries (:legend (pj/plan fr))))))])

;; This is worth stating as a rule because the alternative is
;; tempting and does not work. A convention that read the column's
;; contents -- drawing it as itself when every value happens to name a
;; color -- makes what a column means depend on which rows are in it,
;; so the same plot changes behaviour when the data does. It also has
;; no honest answer for a column of varieties called olive, plum and
;; tomato.
;;
;; ### A written value depends on what the aesthetic is for
;;
;; On the appearance aesthetics a written value is the appearance.
;; `{:color "steelblue"}` is that color, and `{:size 9}` is that
;; radius:

(-> plants
    (pj/lay-point :height :weight {:color "steelblue" :size 9}))

(kind/test-last
 [(fn [fr] (let [layer (-> fr pj/plan :panels first :layers first)]
             (and (= 9 (:radius (:style layer)))
                  (nil? (:legend (pj/plan fr))))))])

;; On the positional aesthetics it is a data value instead. `{:x 20}`
;; puts the mark at 20 on the axis, not 20 units across the page --
;; which is why the axis below stretches to reach it:

(-> plants
    (pj/lay-point :height :weight)
    (pj/lay-text {:x 20 :y 5.0 :text "target"}))

(kind/test-last
 [(fn [fr] (< 5.0 (second (-> fr pj/plan :panels first :y-domain))))])

;; The two differ because position already has a second way to say
;; "measured on the page" and appearance does not. A whole layer can be
;; placed in drawing units with `{:in :drawing-area}`, so a bare
;; `{:x 20}` is free to mean the data value. Nothing but `{:size 9}`
;; can say a radius of nine.

;; ## Saying the other thing
;;
;; Each convention is a default, and `:scale` overrides it in either
;; direction.
;;
;; ### `:scale false` -- draw the column's values
;;
;; This is the cell the conventions cannot reach, and the reason the
;; notation exists. `:shade` holds colors; saying so draws them, and
;; drops the legend, because a legend explains a choice and here there
;; was none to make:

(-> plants
    (pj/lay-point :height :weight {:color {:column :shade :scale false}
                                   :size 9}))

(kind/test-last
 [(fn [fr] (and (nil? (:legend (pj/plan fr)))
                (= [(/ 204.0 255) (/ 51.0 255) (/ 17.0 255) 1.0]
                   (-> fr pj/plan :panels first :layers first :groups
                       first :color))))])

;; `:size` and `:alpha` read a column the same way. Here `:r` holds
;; radii in drawing units rather than measurements to be spread across
;; the output range:

(-> (assoc plants :r [4 8 12 16])
    (pj/lay-point :height :weight {:size {:column :r :scale false}}))

(kind/test-last
 [(fn [fr] (and (true? (-> fr pj/plan :panels first :layers first :size-drawn?))
                (nil? (:size-legend (pj/plan fr)))))])

;; ### `:scale true` -- read the value as data
;;
;; One value over every row is a column of one distinct value, so the
;; scales, the domains and the legends read it as they read any column.
;; That is what labels a whole layer as a named series:

(-> plants
    (pj/lay-point :height :weight {:color {:value "Model A" :scale true}
                                   :size 9}))

(kind/test-last
 [(fn [fr] (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))])

;; Without the `:scale`, `"Model A"` names no column and is no color,
;; and rather than guess it is reported:

(try
  (-> plants
      (pj/lay-point :height :weight {:color "Model A"})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (and (re-find #"not found in dataset" m)
               (re-find #"not a color either" m)))])

;; ### One axis and not the other
;;
;; `:scale false` on an axis measures in drawing units from the top
;; left of the panel background, and it is asked per axis. Here the
;; label's `:x` is a data value, so it sits at 20 on the axis, while
;; its `:y` is fourteen units down from the top of the panel wherever
;; the data happens to run:

(-> plants
    (pj/lay-point :height :weight)
    (pj/lay-text {:x 20
                  :y {:value 14 :scale false}
                  :text "fixed to the panel"}))

(kind/test-last
 [(fn [fr] (let [panel (-> fr pj/plan :panels first)]
             (and (true? (-> panel :layers last :y-drawn?))
                  (< (second (:y-domain panel)) 14))))])

;; An unscaled axis informs no domain -- the label above does not
;; stretch the y axis to reach 14 -- and the marks that can read one
;; are the marks that place through the panel's coordinates. A bar or a
;; boxplot reads the axis scales directly and says so rather than
;; ignoring the request.

;; ### Where a number is also a column name
;;
;; A dataset built without column names gets integer ones, and there a
;; bare number could be either reading. Plotje refuses to guess:

(def integer-named
  {0 [1 2 3]
   1 [4 5 6]})

integer-named

;; `{:column 0}` reads the column,

(-> integer-named
    (pj/lay-point {:x {:column 0} :y 1}))

(kind/test-last
 [(fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))])

;; and `{:value 0}` places every mark at zero:

(-> integer-named
    (pj/lay-point {:x {:value 0} :y 1}))

(kind/test-last
 [(fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))])

;; ## What `:scale` accepts
;;
;; `true` and `false`, and nothing else. Choosing a scale's *type* --
;; linear against logarithmic -- is a decision about the whole plot
;; rather than about one mapping, so it belongs to `pj/scale`:

(try
  (-> plants
      (pj/lay-point :height :weight {:size {:column :weight :scale :log}})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"A mapping's :scale is true or false" m))])

;; ## See Also
;;
;; - [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers)
;;   -- what a mapping is, and where one may be written
;; - [Inference Rules](./plotje_book.inference_rules.html#color-literal-vs-column-reference)
;;   -- the first question in full, and what else Plotje infers
;; - [Placing Marks](./plotje_book.placing_marks.html) -- `:x` and `:y`
;;   given as values, and `{:in :drawing-area}` for a whole layer
