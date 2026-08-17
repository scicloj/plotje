;; # Specifying Aesthetics
;;
;; This chapter explains how to say what a
;; [mapping](./plotje_book.glossary.html#mapping) means.
;;
;; A mapping can always be written out in full. The full form answers
;; two questions: where the values come from, and whether they are read
;; through a scale. Answering both is never wrong, and it is never
;; ambiguous.
;;
;; Most of the time you do not write the full form. Plotje has
;; conventions that supply the two answers from a shorter notation, and
;; the shorter notation is what the rest of the book uses. Each
;; convention is a shorthand for something you could have written out,
;; not a rule of its own.
;;
;; So this chapter goes in that order. First the full form, and the
;; combinations of the two answers it can express. Then the
;; conventions, and which combination each one produces. Then the cases
;; where the conventions cannot decide, and you write the full form
;; instead.

(ns plotje-book.specifying-aesthetics
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## The words used here
;;
;; The terms below are used throughout this chapter. Each has a fuller
;; entry in the [Glossary](./plotje_book.glossary.html).
;;
;; An **aesthetic** is a visual property of a mark: where it sits
;; (`:x`, `:y`), what color it is (`:color`), how large (`:size`), how
;; opaque (`:alpha`), which symbol (`:shape`), what it says (`:text`).
;; See [Aesthetic](./plotje_book.glossary.html#aesthetic).
;;
;; A **mapping** says where one aesthetic gets its values.
;; `{:color :species}` is a mapping. What it supplies may differ from
;; row to row, or be a single value for the whole layer. A mapping is
;; written either on a [pose](./plotje_book.glossary.html#pose), where
;; every layer of that pose sees it, or in the options map of a single
;; [layer](./plotje_book.glossary.html#layer), where only that layer
;; does. See [Mapping](./plotje_book.glossary.html#mapping).
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
;; Plants, measured. Two columns hold numbers, one holds category
;; names, and one holds colors written as hex codes.

(def plants
  {:height  [12 25 18 31]
   :weight  [1.4 3.9 2.2 4.6]
   :species ["fern" "moss" "fern" "ivy"]
   ;; Deliberately nothing like the palette's first three colors, so
   ;; that a plot which scales this column and a plot which draws it
   ;; as it stands cannot be mistaken for each other.
   :shade   ["#EE7733" "#AA3377" "#EE7733" "#000000"]})

plants

;; The `:shade` column is there because it is the awkward case: its
;; values are colors, and it is still a column of data like any other.

;; ## A mapping written in full
;;
;; The full form is a map with two keys, written where the shorter
;; notation would otherwise go:
;;
;; ```clojure
;; {:color {:column :species :scale true}}
;; ```
;;
;; The first key names the **source** -- where the values come from.
;; There are two possibilities and they are spelled differently:
;;
;; - `:column` names a column of the layer's data. Every row supplies
;;   its own value, so the aesthetic can differ from mark to mark.
;; - `:value` gives the value itself, written in the mapping. There is
;;   one of it, so it is the same for every mark in the layer.
;;
;; A mapping names one or the other, never both. A third spelling,
;; `:from`, leaves the choice to the data -- the section
;; [Leaving the source to the data](#leaving-the-source-to-the-data)
;; below covers it.
;;
;; The second key, `:scale`, says whether that source is read through
;; the aesthetic's scale:
;;
;; - `:scale true` sends it through the scale. What you supplied is
;;   data, and the scale decides what is drawn for it.
;; - `:scale false` draws it as it stands. What you supplied is already
;;   the visible thing, and no scale is involved.
;;
;; The two keys are independent: either source can be read either way,
;; which makes four combinations. The next four sections take them one
;; at a time, each written in full.

;; ## A column, read through the scale
;;
;; `{:column :species :scale true}` says: take each mark's value from
;; the `:species` column, and send it through the color scale.
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
;; `{:column :shade :scale false}` says: take each mark's value from
;; the `:shade` column, and draw it without a scale.
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
           (= [(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
              (-> p :panels first :layers first :groups first :color)))))])

;; The first plant's `:shade` is `"#EE7733"`, and the first group is
;; drawn in exactly that color: 238, 119 and 51 out of 255.

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
;; is not a color -- it is data, and the scale supplies the color.

(-> plants
    (pj/lay-point :height :weight {:color {:value "Model A" :scale true}
                                   :size 9}))

(kind/test-last
 [(fn [fr] (= ["Model A"]
              (mapv :label (:entries (:legend (pj/plan fr))))))])

;; The layers of a plot share one color scale, so a value read through
;; it joins the categories the other layers supply. The points below
;; take their color from the `:species` column, and the line is written
;; as `"ivy"`, which that column already holds. The line comes out in
;; the color `"ivy"` was assigned, and the legend still has one row per
;; species:

(-> plants
    (pj/pose :height :weight)
    (pj/lay-point {:color {:column :species :scale true} :size 9})
    (pj/lay-line {:color {:value "ivy" :scale true}}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)
          ivy (->> p :legend :entries
                   (filter (fn [e] (= "ivy" (:label e))))
                   first :color)]
      ;; The line is the second layer, and it draws a single group.
      (and (= ["fern" "moss" "ivy"] (mapv :label (:entries (:legend p))))
           (= ivy (-> p :panels first :layers second :groups first :color)))))])

;; So a layer with no column of its own can be drawn in the color the
;; scale already gives a category. A value the column does not hold is a
;; new category instead, and takes a color and a legend row of its own:

(-> plants
    (pj/pose :height :weight)
    (pj/lay-point {:color {:column :species :scale true} :size 9})
    (pj/lay-line {:color {:value "predicted" :scale true}}))

(kind/test-last
 [(fn [fr] (= ["fern" "moss" "ivy" "predicted"]
              (mapv :label (:entries (:legend (pj/plan fr))))))])

;; One scale per aesthetic is a plot-wide rule, not a rule about
;; values; [Scales](./plotje_book.scales.html#one-legend-per-aesthetic)
;; states it and shows what happens when two layers ask for different
;; ones.

;; ## The four combinations
;;
;; | Source | `:scale` | What you supply | What is drawn |
;; |:--|:--|:--|:--|
;; | `:column` | `true` | a column of data | one color per distinct value, from the palette, with a legend |
;; | `:column` | `false` | a column of colors | each row's own color, no legend |
;; | `:value` | `false` | one color | that color for every mark, no legend |
;; | `:value` | `true` | one data value | one color from the palette, with a legend entry for that value |
;;
;; The same four exist for `:size`, where the scale turns numbers into
;; radii, and for `:alpha`, where it turns numbers into opacities.

;; ## The short form
;;
;; Writing every mapping in full would be tiring, and most mappings are
;; one of the four for an obvious reason. So a mapping may be written
;; short -- a bare column name, or the bare thing to be drawn -- and
;; Plotje supplies the two answers.

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
;; So, on the plants data above, `{:color :species}` has the source of
;; `{:column :species}`, because the data carries a `:species` column;
;; and `{:color "#0077BB"}` has the source of `{:value "#0077BB"}`,
;; because no column is named that.

;; ### How the scale is decided
;;
;; **A column is read through the scale.** That is the default, and
;; `:scale false` overrides it. What the column holds makes no
;; difference -- `:shade` holds colors, and the short form
;; `{:color :shade}` still sends it through the color scale, so three
;; distinct values become three palette colors:

(-> plants
    (pj/lay-point :height :weight {:color :shade}))

(kind/test-last
 [(fn [fr] (= 3 (count (:entries (:legend (pj/plan fr))))))])

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

;; The two groups differ because what people usually mean differs. A
;; number written for `:x` or `:y` is usually a place in the data -- a
;; threshold to mark, a point to label -- and only rarely a distance
;; across the page. A number written for `:size` or `:alpha` is usually
;; the appearance itself, and only rarely a measurement to be scaled.
;; Each convention takes the common case, and `:scale` says the other
;; one where it is meant.
;;
;; A layer's `:x` and `:y` can both be given in drawing units at once,
;; rather than one at a time, with `{:in :drawing-area}`, described in
;; [Placing Marks](./plotje_book.placing_marks.html#placing-a-mark-on-the-panel-instead-of-in-the-data).

;; ### What the short forms come out as
;;
;; Each short form below produces exactly the mapping beside it:
;;
;; | Short form | Full form |
;; |:--|:--|
;; | `{:color :species}` | `{:color {:column :species :scale true}}` |
;; | `{:color :shade}` | `{:color {:column :shade :scale true}}` |
;; | `{:color "#0077BB"}` | `{:color {:value "#0077BB" :scale false}}` |
;;
;; `pj/svg-summary` reports what a pose renders to, so the colors each
;; pair draws can be read side by side. The pairs come out the same:

(defn drawn-colors
  "The colors a `:color` mapping draws on the plants scatter."
  [mapping]
  (-> plants
      (pj/lay-point :height :weight mapping)
      pj/svg-summary
      :colors
      (disj "none")))

{:species-short (drawn-colors {:color :species})
 :species-full  (drawn-colors {:color {:column :species :scale true}})
 :shade-short   (drawn-colors {:color :shade})
 :shade-full    (drawn-colors {:color {:column :shade :scale true}})
 :written-short (drawn-colors {:color "#0077BB"})
 :written-full  (drawn-colors {:color {:value "#0077BB" :scale false}})}

(kind/test-last
 [(fn [m] (and (= (:species-short m) (:species-full m))
               (= (:shade-short m) (:shade-full m))
               (= (:written-short m) (:written-full m))
               (= 3 (count (:species-short m)))
               (= #{"rgb(0,119,187)"} (:written-short m))))])

;; The positional short form works the same way: `{:x 6.0}` is
;; `{:x {:value 6.0 :scale true}}`, which is what stretched the y axis
;; in the example above.

;; ### Naming the source without choosing the scale
;;
;; The two keys can be written separately. A full form that names only
;; a source leaves the scale to the convention, which is useful when
;; the source is the ambiguous part and the scale is not.

{:column-alone   (drawn-colors {:color {:column :shade}})
 :column-scaled  (drawn-colors {:color {:column :shade :scale true}})
 :value-alone    (drawn-colors {:color {:value "#0077BB"}})
 :value-drawn    (drawn-colors {:color {:value "#0077BB" :scale false}})}

(kind/test-last
 [(fn [m] (and (= (:column-alone m) (:column-scaled m))
               (= (:value-alone m) (:value-drawn m))))])

;; ### Leaving the source to the data
;;
;; The third source key is `:from`. It names the source the way the
;; plain form does -- ask the data, and take whichever answer it gives
;; -- while leaving room for a `:scale`, which a plain mapping has
;; nowhere to put.
;;
;; `{:color {:from :shade}}` and the plain `{:color :shade}` say the
;; same thing, and draw the same plot:

(-> plants
    (pj/lay-point :height :weight {:color {:from :shade}}))

(kind/test-last
 [(fn [fr] (= (pj/svg-summary fr)
              (-> plants
                  (pj/lay-point :height :weight {:color :shade})
                  pj/svg-summary)))])

;; The point of writing it that way is what can be added. `:shade`
;; holds colors, and the palette is drawn over them because a column
;; passes through the color scale. `:scale false` draws them as they
;; stand -- and the plain form cannot say it:

(-> plants
    (pj/lay-point :height :weight {:color {:from :shade :scale false}}))

(kind/test-last
 [(fn [fr]
    (= [[(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
        [(/ 170.0 255) (/ 51.0 255) (/ 119.0 255) 1.0]
        [0.0 0.0 0.0 1.0]]
       (->> fr pj/plan :panels first :layers first :groups (mapv :color))))])

;; `{:column :shade :scale false}` would draw the same plot here, and
;; the two differ on data that carries no `:shade` column: `:column`
;; insists on a column and reports it missing, while `:from` reads
;; `:shade` as a value, exactly as the plain form does.

;; ## The positional aesthetics (`:x`, `:y`) and `:scale false`
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
;; A dataset built without column names gets integer ones. A number on
;; `:x` or `:y` then has both readings available -- it could be a
;; column name or a value to place a mark at -- and the source rule
;; decides between them the way it decides everywhere: by asking the
;; data.

(def integer-named
  {0 [1 2 3]
   1 [4 5 6]})

integer-named

;; The data carries a column named 0, so the bare `{:x 0}` reads it.
;; Its three values run from 1 to 3, so the x domain covers that range
;; with the usual padding.

(-> integer-named
    (pj/lay-point {:x 0 :y 1}))

(kind/test-last
 [(fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))])

;; That is the reading the data gives it, not a reading of the number.
;; On a dataset with no column named 0 the same mapping places every
;; mark at an x of zero, and the domain is padded outward from that
;; single value:

(-> plants
    (pj/lay-point {:x 0 :y :weight}))

(kind/test-last
 [(fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))])

;; So the same code means different things on differently named data.
;; Where that matters, write the mapping in full and the data has no
;; say. `{:column 0}` reads the column even where one exists only by
;; accident:

(-> integer-named
    (pj/lay-point {:x {:column 0} :y 1}))

(kind/test-last
 [(fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))])

;; and `{:value 0}` places every mark at zero even where a column named
;; 0 is there to be read:

(-> integer-named
    (pj/lay-point {:x {:value 0} :y 1}))

(kind/test-last
 [(fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))])

;; ## What `:scale` accepts
;;
;; `true` and `false` say which side of the scale a mapping is read
;; through. `:scale` also accepts a scale **type** -- linear or
;; logarithmic, for instance -- and a whole scale **spec**, the map
;; that says which scale and how, and then it says which scale as
;; well:

(-> plants
    (pj/lay-point :height :weight {:size {:column :weight :scale :log}}))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))])

;; A spec is the same map `pj/scale` takes, so a mapping can set the
;; range an aesthetic spans as well as its type. The one below is twice
;; the default at both ends, and every mark is drawn twice as wide:

(-> plants
    (pj/lay-point :height :weight
                  {:size {:column :weight :scale {:range [4 16]}}}))

(kind/test-last
 [(fn [fr]
    (= (->> fr pj/plan :size-legend :entries (mapv :magnitude))
       (->> (-> plants (pj/lay-point :height :weight {:size :weight}))
            pj/plan :size-legend :entries (mapv #(* 2 (:magnitude %))))))])

;; A type written here reads **that one mapping** through it, which is
;; what distinguishes it from `pj/scale`. `pj/scale` sets the scale on
;; the pose it is called on and everything below: called on the pose
;; you are building, it covers the whole plot; called on one cell
;; before the cells are arranged, that cell alone. Where
;; both are written, the mapping is the narrower scope and wins, key by
;; key -- so a pose that sets a range and a mapping that names a type
;; give a plot with both.
;;
;; `true` is not an opinion about which scale. It says the value passes
;; through whatever scale the aesthetic has, so a `pj/scale` above it
;; still decides the type.
;;
;; Each aesthetic gets its own: `(pj/scale pose :x :log)` and
;; `(pj/scale pose :size :log)` are separate decisions, and either can
;; be made without the other. Which keys a spec may carry depends on
;; the aesthetic -- `:size` reads `:range`, `:by` and `:from-zero`
;; beside `:type` and `:domain`, an axis reads `:breaks` and `:labels`
;; -- and a key the aesthetic does not read is refused rather than
;; dropped. [Scales](./plotje_book.scales.html#what-each-aesthetics-scale-takes)
;; has each of them.
;;
;; ### The axes take one scale per panel
;;
;; `:x` and `:y` take a type or a spec like any other aesthetic:

(-> plants
    (pj/lay-point {:x {:column :height :scale {:type :log}} :y :weight}))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))])

;; A panel has one x axis and one y axis, and every layer is drawn
;; against them, so a panel carries one scale per axis. Two layers
;; naming different ones are refused; a layer naming none is drawn
;; against whichever the panel has.

(try
  (-> plants
      (pj/pose :height :weight)
      (pj/lay-point {:x {:column :height :scale :log}})
      (pj/lay-line {:x {:column :height :scale :linear}})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"Layers name different scales for the :x axis" m))])

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
;; combinations are not new. ggplot2 has all four and reaches them through
;; different syntax, using `:size` as the example:
;;
;; | Plotje | ggplot2 |
;; |:--|:--|
;; | `{:size {:column :weight :scale true}}` | `aes(size = weight)` |
;; | `{:size {:column :radii :scale false}}` | `aes(size = radii) + scale_size_identity()` |
;; | `{:size {:value 7 :scale false}}` | `geom_point(size = 7)` |
;; | `{:size {:value 7 :scale true}}` | `aes(size = 7)` |
;;
;; The table was checked by running each ggplot2 form on version 4.0.0
;; and comparing what it produced. What matches is which of the four
;; combinations each form gives, not the sizes drawn: the two libraries
;; spread a scaled column across different intervals of radii, so the
;; same data draws different circles.
;;
;; The difference is where the choice is written. In ggplot2 it is
;; spread across three places -- inside `aes()`, outside it, and in a
;; `scale_*` call. In Plotje the two questions are answered in the
;; mapping itself, by the two keys.
