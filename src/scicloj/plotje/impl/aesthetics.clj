(ns scicloj.plotje.impl.aesthetics
  "What a mapping value means, decided in one place.

   Specifying an aesthetic is two independent decisions, and this
   namespace answers both:

   - **source** -- a written value, or a reference to a column
   - **scale** -- whether that value passes through the aesthetic's
     scale, or is drawn as it stands

   Two by two is four cells, and every one of them is a thing someone
   writes. ggplot2 has all four: `aes(size=5)` scales a written value
   and gives it a legend, `geom_point(size=5)` draws it, `aes(size=b)`
   scales a column, and `scale_*_identity()` draws one.

   **The two decisions are asked of different things.** The source is
   asked of the layer's data: a name it carries is a column. The scale
   is asked of the *value*, and only of the value -- the convention
   sends a column through the scale whatever it holds, and nothing
   inspects its contents. An
   earlier reading did inspect it, drawing a column whose every value
   named a color, and three defects came out of that one idea: the
   same column behaved differently for `\"red\"` and `\"Red\"`, `:shape`
   answered \"drawn\" while `collect-shapes` scaled it anyway and
   labelled a circle \"cross\", and `:fill` held the same disagreement
   unnoticed. Asking the value question of a column is what produced
   all three.

   Nothing here decides by *type*. Deciding by type is how the old rule
   came to differ per aesthetic without anyone choosing that it should:
   `column-ref?` was `(or (keyword? v) (string? v))`, and a carve-out
   for a string on `:color` was bolted beside it. The rules here are
   the layer's data, then what the aesthetic can draw -- the first read
   from the caller's dataset, the second from
   `pose-schema/drawn-value-schemas`, with which side of the scale the
   answer falls on coming from `defaults/aesthetic-registry`. Nothing
   is restated here.

   Two functions, meant to be called in that order:

       (let [src (source v col-names)]
         (scaled? k {:source src :value v}))"
  (:require [malli.core :as m]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.pose-schema :as pose-schema]))

(defn source
  "Which side of the source axis `v` falls on: `:column` when
   `column-names` holds it, `:value` otherwise.

   The data decides, for every aesthetic and every type of value. That
   uniformity is the point -- a dataset built without column names gets
   integer ones, so a number can name a column, and a string can name
   one whose name is a color. Matching is strict, as it is everywhere
   else in Plotje: `\"species\"` finds a string-named column and
   `:species` does not.

   Whether the aesthetic accepts a written value at all is a separate
   question, answered by the registry's `:value?` and reported where
   the mapping is built. This function says what was written, not
   whether it was allowed.

   The three-argument arity takes what the writer said explicitly, from
   an `{:column ...}` or `{:value ...}` mapping, and honors it. That is
   the only way to reach a value whose name a column also carries --
   `{:value \"blue\"}` is the color on a dataset with a column called
   blue, and `{:column \"blue\"}` is the column on one without."
  ([v column-names] (source v column-names nil))
  ([v column-names explicit]
   (or explicit
       (if (contains? column-names v) :column :value))))

(defn drawable?
  "True when `aesthetic` could draw `v` as it stands -- `\"red\"` or
   `:steelblue` on `:color`, `7` on `:size`, `:circle` on `:shape`.

   Reads `pose-schema/drawn-value-schemas`, so the grammar is stated
   once and this is what keeps it honest. An aesthetic absent from that
   map draws nothing: `:group` splits the data and has no appearance of
   its own.

   It decides one thing, and `scaled?` no longer asks it: whether a
   value that names no column can be used at all. That is the third
   step of the rule -- ask the data, ask what the aesthetic can draw,
   then report that neither reading fits. A value the aesthetic cannot
   draw is not quietly read as data instead; `{:value v :scale true}`
   is how a datum is asked for."
  [aesthetic v]
  (if-let [schema (get pose-schema/drawn-value-schemas aesthetic)]
    (m/validate schema v)
    false))

(defn scaled?
  "Whether this mapping passes through `aesthetic`'s scale.

   `source` and `value` are what `source` above returned and what was
   written. `scale` is an explicit `:scale` from the mapping, and
   overrides the convention in either direction -- `false` draws,
   `true` scales.

   **A column passes through the scale unless the mapping says
   otherwise**, for every aesthetic that has one, and nothing here
   reads the column's contents in deciding that. The first half is the
   convention, and an explicit `:scale false` overrides it -- that
   branch is above. The second half holds either way, and it is what
   makes a column's meaning independent of the rows that happen to be
   in it: a color column of `\"red\"`, `\"green\"`, `\"blue\"` is three
   categories, exactly as `\"Red\"`, `\"Green\"`, `\"Blue\"` is, and
   `scale_colour_identity()` is spelled `{:scale false}` here.

   The convention decides only what a **written value** means, from
   the registry's `:scale-default`, and it splits by category rather
   than by aesthetic:

   - `:always` -- scaled. Every positional aesthetic. `{:x 6.5}` is a
     datum on the axis, not 6.5 drawing units, and the vocabulary does
     not get a say: 6.5 is a perfectly good drawing-unit distance too.
     Position can afford this because `{:in :drawing-area}` is a
     second vocabulary for page geometry; appearance has none, so
     nothing but `{:size 7}` could say a radius of seven.
   - `:by-source` -- drawn. Every appearance aesthetic. The
     aesthetic's vocabulary is not consulted here, but the gate
     refuses a value against it, so `\"red\"` is drawn and
     `\"notacolor\"` -- neither a column nor a color -- is reported.
   - `:never` -- the aesthetic has a reading but no scale. `:text`.
   - `nil` -- no scale at all. `:group` splits the data and draws
     nothing of its own, which is why `pj/scale` refuses it too.

   The last two answer `false` whatever the mapping says. An explicit
   `:scale` cannot conjure a scale that is not there, so it is
   reported where the mapping is built rather than honored here.

   Reading a written value as **data** -- ggplot2's constant inside
   `aes()` -- is reached by saying so: `{:value \"Model A\" :scale
   true}`. It is not inferred, because inferring it would have to turn
   on the value, and a value that is neither a column nor something the
   aesthetic can draw is a mistake far more often than it is a series
   label."
  [aesthetic {:keys [source scale]}]
  (let [{:keys [scale-default]} (defaults/aesthetic-registry aesthetic)]
    (cond
      ;; The two aesthetics with no scale answer before `scale` is
      ;; consulted, because there is nothing for it to select. Both
      ;; refuse a `:scale` where the mapping is built, so the pipeline
      ;; never reaches here that way; answering `true` regardless was
      ;; an internal claim nothing could act on.
      (nil? scale-default)     false
      (= :never scale-default) false
      (false? scale)           false
      (some? scale)            true
      ;; The source half: a column scales wherever there is a scale to
      ;; pass through, and the two that have none answered above.
      (= :column source)       true
      :else                    (= :always scale-default))))
