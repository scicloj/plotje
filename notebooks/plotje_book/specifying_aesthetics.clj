;; # Specifying Aesthetics
;;
;; This chapter explains how to say what a mapping means.
;;
;; A mapping can always be written out in full. The full form states
;; two things: where the value comes from, and whether it is read
;; through a scale. Writing both of them is never wrong, and it is
;; never ambiguous.
;;
;; Most of the time you do not write the full form. Plotje has
;; conventions that supply the two answers from a shorter notation, and
;; the shorter notation is what the rest of the book uses. The
;; conventions are worth learning as conventions -- as a shorthand for
;; something you could have written out -- rather than as rules of their
;; own.
;;
;; So this chapter goes in that order. First the full form and the four
;; things it can say. Then the conventions, and which of the four each
;; one produces. Then the cases where the conventions cannot decide, and
;; you write the full form instead.

(ns plotje-book.specifying-aesthetics
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## The words used here
;;
;; Four terms carry this chapter. Each has a fuller entry in the
;; [Glossary](./plotje_book.glossary.html).
;;
;; An **aesthetic** is a visual property of a mark: where it sits
;; (`:x`, `:y`), what color it is (`:color`), how large (`:size`), how
;; opaque (`:alpha`), which symbol (`:shape`), what it says (`:text`).
;; See [Aesthetic](./plotje_book.glossary.html#aesthetic).
;;
;; A **mapping** gives one aesthetic one value. `{:color :species}` is a
;; mapping. It is written either on a pose, where every layer of that
;; pose sees it, or in a single layer's options map, where only that
;; layer does. See [Mapping](./plotje_book.glossary.html#mapping).
;;
;; A **scale** turns data into something visible. The color scale turns
;; categories into colors; the size scale turns numbers into radii; the
;; x scale turns data values into places across the panel. A scale is
;; built from the data it is given, so it depends on the whole column
;; and not on any one row. See
;; [Scale](./plotje_book.glossary.html#scale).
;;
;; A **legend** is the key drawn beside the plot that explains a scale
;; -- which color stands for which category, which radius for which
;; number. A legend appears when there is a scale to explain. See
;; [Legend](./plotje_book.glossary.html#legend).

;; ## The data used here
;;
;; Four plants, measured. Two columns hold numbers, one holds category
;; names, and one holds colors written as hex codes.

(def plants
  {:height  [12 25 18 31]
   :weight  [1.4 3.9 2.2 4.6]
   :species ["fern" "moss" "fern" "ivy"]
   :shade   ["#CC3311" "#0077BB" "#CC3311" "#009988"]})

plants

;; The `:shade` column is there because it is the awkward case: its
;; values are colors, and it is still a column of data like any other.

;; ## A mapping written in full
;;
;; The full form is a map with two keys. It replaces the plain value:
;;
;; ```clojure
;; {:color {:column :species :scale true}}
;; ```
;;
;; The first key names the **source** -- where the value comes from.
;; There are two possibilities and they are spelled differently:
;;
;; - `:column` names a column of the layer's data. Every row supplies
;;   its own value, so the aesthetic can differ from mark to mark.
;; - `:value` gives the value itself, written in the mapping. There is
;;   one of it, so it is the same for every mark in the layer.
;;
;; A mapping names one or the other, never both.
;;
;; The second key, `:scale`, says whether that source is read through
;; the aesthetic's scale:
;;
;; - `:scale true` sends it through the scale. What you supplied is
;;   data, and the scale decides what is drawn for it.
;; - `:scale false` draws it as it stands. What you supplied is already
;;   the visible thing, and no scale is involved.
;;
;; The two keys are independent. Either source can be read either way,
;; which makes four mappings in all. The next four sections are those
;; four, one at a time, each written in full.

;; ## A column, read through the scale
;;
;; `{:column :species :scale true}` says: take the value from the
;; `:species` column, and send it through the color scale.
;;
;; The color scale is built from every value in the column. There are
;; three distinct species, so the scale assigns three colors from the
;; palette, and a legend appears to explain which is which. Nothing
;; about the drawn colors comes from the column -- `"fern"` is not a
;; color, and the scale is what supplies one for it.

(-> plants
    (pj/lay-point :height :weight {:color {:column :species :scale true}}))

(kind/test-last
 [(fn [fr] (= ["fern" "moss" "ivy"]
              (mapv :label (:entries (:legend (pj/plan fr))))))])

;; ## A column, drawn as it stands
;;
;; `{:column :shade :scale false}` says: take the value from the
;; `:shade` column, and draw it without a scale.
;;
;; This only makes sense when the column already holds what the
;; aesthetic draws, which for `:color` means colors. `:shade` holds hex
;; codes, so each mark is drawn in the color its own row carries.
;;
;; No legend appears. A legend explains a scale, and there is no scale
;; here -- nothing was decided that a reader would need explaining.

(-> plants
    (pj/lay-point :height :weight {:color {:column :shade :scale false}
                                   :size 9}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)]
      (and (nil? (:legend p))
           (= [(/ 204.0 255) (/ 51.0 255) (/ 17.0 255) 1.0]
              (-> p :panels first :layers first :groups first :color)))))])

;; The first plant's `:shade` is `"#CC3311"`, and the first group is
;; drawn in exactly that color: 204, 51 and 17 out of 255.

;; ## A written value, drawn as it stands
;;
;; `{:value "#0077BB" :scale false}` says: use this one color, and draw
;; it without a scale.
;;
;; Every mark in the layer is drawn in it. There is one value, so there
;; is nothing to tell marks apart by, and no legend.

(-> plants
    (pj/lay-point :height :weight {:color {:value "#0077BB" :scale false}
                                   :size 9}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)]
      (and (nil? (:legend p))
           (= 1 (count (-> p :panels first :layers first :groups))))))])

;; ## A written value, read through the scale
;;
;; `{:value "Model A" :scale true}` says: treat this one value as data,
;; and send it through the color scale.
;;
;; One value repeated over every row is a column with one distinct
;; value in it. The color scale is built from that, so it assigns one
;; color, and a legend appears with one entry reading `"Model A"`.
;;
;; This is how a whole layer is labelled as a named series. The value
;; is not a color and does not have to be one -- it is data, and the
;; scale supplies the color.

(-> plants
    (pj/lay-point :height :weight {:color {:value "Model A" :scale true}
                                   :size 9}))

(kind/test-last
 [(fn [fr] (= ["Model A"]
              (mapv :label (:entries (:legend (pj/plan fr))))))])

;; ## The four together
;;
;; | Source | `:scale` | What you supply | What is drawn |
;; |:--|:--|:--|:--|
;; | `:column` | `true` | a column of data | one color per distinct value, from the palette, with a legend |
;; | `:column` | `false` | a column of colors | each row's own color, no legend |
;; | `:value` | `false` | one color | that color for every mark, no legend |
;; | `:value` | `true` | one data value | one color from the palette, with a one-entry legend |
;;
;; The same four exist for `:size`, where the scale turns numbers into
;; radii, and for `:alpha`, where it turns numbers into opacities. The
;; positional aesthetics have their own version of the fourth column,
;; described further down.

;; ## The short form
;;
;; Writing every mapping in full would be tiring, and most mappings are
;; one of the four for an obvious reason. So a mapping may be written
;; as a plain value, and Plotje supplies the two answers.

;; ### How the source is decided
;;
;; **The layer's data decides.** If the value names a column of that
;; data, it is a column reference. If it does not, it is the value
;; itself.
;;
;; Matching is exact. A keyword finds a keyword-named column and a
;; string finds a string-named one, so `:species` and `"species"` are
;; not interchangeable. The
;; [Inference Rules](./plotje_book.inference_rules.html#named-colors-and-string-disambiguation)
;; chapter works through this in full.
;;
;; So `{:color :species}` has the source of `{:column :species}`, and
;; `{:color "#0077BB"}` has the source of `{:value "#0077BB"}`.

;; ### How the scale is decided
;;
;; **A column is always read through the scale.** There is no exception
;; and the column's contents are not consulted.
;;
;; This is worth stating plainly, because a column of colors invites the
;; opposite guess. `:shade` holds nothing but colors, and the short form
;; `{:color :shade}` still sends it through the color scale: three
;; distinct values become three palette colors, and the hex codes the
;; column holds are not drawn.

(-> plants
    (pj/lay-point :height :weight {:color :shade}))

(kind/test-last
 [(fn [fr] (= 3 (count (:entries (:legend (pj/plan fr))))))])

;; The reason for the rule is that the alternative is unstable. A
;; convention that read the column's contents would make the meaning of
;; a mapping depend on which rows are in the data, so the same plotting
;; code would behave differently on a different day's data. It would
;; also have no answer for a column of plant varieties called olive,
;; plum and tomato, whose values name colors by coincidence.
;;
;; To draw a column as it stands, say so. That is the second of the four
;; mappings above, and `:scale false` is the only way to reach it.
;;
;; **A written value is decided by what the aesthetic is for.** The
;; aesthetics fall into two groups here, and the two groups answer
;; differently.
;;
;; On the appearance aesthetics -- `:color`, `:size`, `:alpha`,
;; `:shape` -- a written value is drawn as it stands. `{:color "steelblue"}`
;; is that color and `{:size 9}` is that radius.

(-> plants
    (pj/lay-point :height :weight {:color "steelblue" :size 9}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)]
      (and (= 9 (:radius (:style (-> p :panels first :layers first))))
           (nil? (:legend p)))))])

;; On the positional aesthetics -- `:x`, `:y` and the range endpoints --
;; a written value is read through the scale instead. It is a data
;; value, so the axis takes it into account. The text below is placed at
;; a `:weight` of 6.0, which is past the largest measured weight of 4.6,
;; and the y axis stretches to reach it.

(-> plants
    (pj/lay-point :height :weight)
    (pj/lay-text {:x 20 :y 6.0 :text "target"}))

(kind/test-last
 [(fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))])

;; The two groups differ because `:x` and `:y` already have a second way
;; of saying "measured on the page". A whole layer can be placed in
;; drawing units with `{:in :drawing-area}`, described in
;; [Placing Marks](./plotje_book.placing_marks.html#placing-a-mark-on-the-panel-instead-of-in-the-data).
;; The appearance aesthetics have no such second way, so `{:size 9}` has
;; to mean a radius of nine.

;; ### What the short forms come out as
;;
;; Each short form below produces exactly the mapping beside it. The
;; form under the table draws each pair and compares the colors that
;; come out.
;;
;; | Short form | Full form |
;; |:--|:--|
;; | `{:color :species}` | `{:color {:column :species :scale true}}` |
;; | `{:color :shade}` | `{:color {:column :shade :scale true}}` |
;; | `{:color "#0077BB"}` | `{:color {:value "#0077BB" :scale false}}` |
;; | `{:x 6.0}` | `{:x {:value 6.0 :scale true}}` |

(let [colors (fn [m] (->> (pj/lay-point plants :height :weight m)
                          pj/plan :panels first :layers first :groups
                          (mapv :color)))]
  {:species (= (colors {:color :species})
               (colors {:color {:column :species :scale true}}))
   :shade   (= (colors {:color :shade})
               (colors {:color {:column :shade :scale true}}))
   :written (= (colors {:color "#0077BB"})
               (colors {:color {:value "#0077BB" :scale false}}))})

(kind/test-last [(fn [m] (= {:species true :shade true :written true} m))])

;; ### Naming the source without choosing the scale
;;
;; The two keys can be written separately. A full form that names only
;; a source leaves the scale to the convention, which is useful when
;; the source is the ambiguous part and the scale is not.

(let [colors (fn [m] (->> (pj/lay-point plants :height :weight m)
                          pj/plan :panels first :layers first :groups
                          (mapv :color)))]
  {:column-without-scale (= (colors {:color {:column :shade}})
                            (colors {:color {:column :shade :scale true}}))
   :value-without-scale  (= (colors {:color {:value "#0077BB"}})
                            (colors {:color {:value "#0077BB" :scale false}}))})

(kind/test-last [(fn [m] (= {:column-without-scale true
                             :value-without-scale true} m))])

;; ## The positional aesthetics and `:scale false`
;;
;; On `:x` and `:y`, `:scale false` means the value is a distance in
;; **drawing units** measured from the top left of the panel background,
;; rather than a data value. Drawing units are explained in
;; [Drawing Space](./plotje_book.glossary.html#drawing-space).
;;
;; It is asked one axis at a time. Below, the note's `:x` is a data
;; value, so it sits at a height of 20 on the x axis; its `:y` is 14
;; drawing units down from the top of the panel, wherever the weights
;; happen to run.

(-> plants
    (pj/lay-point :height :weight)
    (pj/lay-text {:x 20
                  :y {:value 14 :scale false}
                  :text "fixed to the panel"}))

(kind/test-last
 [(fn [fr]
    (let [panel (-> fr pj/plan :panels first)]
      (and (true? (:y-drawn? (last (:layers panel))))
           (< (second (:y-domain panel)) 14))))])

;; A value in drawing units informs no domain, which is why the y axis
;; above still ends near the largest weight rather than stretching to
;; 14. The marks that can read an axis in drawing units are the marks
;; placed through the panel's coordinates; a bar or a boxplot reads the
;; axis scale directly and reports the request rather than ignoring it.

;; ## When the conventions cannot decide
;;
;; The source rule asks the data one question: does this value name a
;; column? Two situations make that question the wrong one, and in both
;; you write the full form.

;; ### A column named after a color
;;
;; If the data carries a column called `"blue"`, then `{:color "blue"}`
;; names that column, and the color blue is out of reach through the
;; short form.

(def named-after-a-colour
  {:height [12 25]
   :weight [1.4 3.9]
   "blue"  ["p" "q"]})

named-after-a-colour

;; `{:value "blue"}` is the color. One group, drawn blue, no legend.

(-> named-after-a-colour
    (pj/lay-point :height :weight {:color {:value "blue"} :size 9}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)]
      (and (nil? (:legend p))
           (= [[0.0 0.0 1.0 1.0]]
              (->> p :panels first :layers first :groups (mapv :color))))))])

;; `{:column "blue"}` is the column. Two groups, two palette colors, and
;; a legend reading the column's two values.

(-> named-after-a-colour
    (pj/lay-point :height :weight {:color {:column "blue"} :size 9}))

(kind/test-last
 [(fn [fr] (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))])

;; ### Columns named by number
;;
;; A dataset built without column names gets integer ones. A number in a
;; mapping could then be either a column name or a value to place a mark
;; at, and which one is meant cannot be recovered from the number.

(def integer-named
  {0 [1 2 3]
   1 [4 5 6]})

integer-named

;; `{:column 0}` reads column 0. Its three values run from 1 to 3, so
;; the x domain covers that range with the usual padding.

(-> integer-named
    (pj/lay-point {:x {:column 0} :y 1}))

(kind/test-last
 [(fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))])

;; `{:value 0}` places every mark at an x of zero. Every mark shares one
;; x, so the domain is padded outward from that single value.

(-> integer-named
    (pj/lay-point {:x {:value 0} :y 1}))

(kind/test-last
 [(fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))])

;; Where the two readings cannot both be available, Plotje reports the
;; ambiguity rather than choosing. `(pj/pose data {:x 0 :y 1})` runs
;; before any data is at hand, so it refuses a bare number and names
;; both readings:

(try
  (pj/pose integer-named {:x 0 :y 1})
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (and (re-find #"must be a column reference" m)
               (re-find #"If 0 is a column name" m)))])

;; ## What `:scale` accepts
;;
;; `true` and `false`, and nothing else.
;;
;; A scale also has a **type** -- linear or logarithmic, for instance.
;; Choosing a type is a decision about the whole plot rather than about
;; one mapping, so it belongs to `pj/scale`, and writing a type in a
;; mapping is reported:

(try
  (-> plants
      (pj/lay-point :height :weight {:size {:column :weight :scale :log}})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"A mapping's :scale is true or false" m))])

;; ### Aesthetics with no scale
;;
;; Two aesthetics have no scale at all, so `:scale` has nothing to
;; select and is reported on both.
;;
;; `:text` draws a label as it stands, whether the label comes from a
;; column or is written in the mapping. `:group` splits a layer into one
;; drawn group per value and draws nothing of its own.

(try
  (pj/lay-text plants :height :weight {:text {:column :species :scale false}})
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #":text has no scale to set" m))])

;; Both still take the full form's source key, which is what settles a
;; column name that could be read as a value.

;; ## See Also
;;
;; - [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers)
;;   -- what a mapping is, and where one may be written
;; - [Inference Rules](./plotje_book.inference_rules.html#named-colors-and-string-disambiguation)
;;   -- how the source is decided, in full, and what else Plotje infers
;; - [Placing Marks](./plotje_book.placing_marks.html) -- `:x` and `:y`
;;   given as values, and `{:in :drawing-area}` for a whole layer
;; - [Glossary](./plotje_book.glossary.html#aesthetic) -- aesthetic,
;;   mapping, scale, legend

;; ## Appendix: the same four in ggplot2
;;
;; Readers coming from R may find it useful to see that the four
;; mappings are not new. ggplot2 has all four and reaches them through
;; different syntax, using `:size` as the example:
;;
;; | Plotje | ggplot2 |
;; |:--|:--|
;; | `{:size {:column :weight :scale true}}` | `aes(size = weight)` |
;; | `{:size {:column :radii :scale false}}` | `scale_size_identity()` |
;; | `{:size {:value 7 :scale false}}` | `geom_point(size = 7)` |
;; | `{:size {:value 7 :scale true}}` | `aes(size = 7)` |
;;
;; The difference is where the choice is written. In ggplot2 it is
;; spread across three places -- inside `aes()`, outside it, and in a
;; `scale_*` call. In Plotje the two questions are answered in the
;; mapping itself, by the two keys.
