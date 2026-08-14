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
   scales a column, and `scale_*_identity()` draws one. The fourth is
   the one Plotje has lacked -- a column of hex codes on `:color` became
   three categories and drew palette colors instead of the colors it
   held.

   Nothing here decides by *type*. Deciding by type is how the old rule
   came to differ per aesthetic without anyone choosing that it should:
   `column-ref?` was `(or (keyword? v) (string? v))`, and a carve-out
   for a string on `:color` was bolted beside it. The rules here are
   the layer's data, then what the aesthetic can draw -- the first read
   from the caller's dataset, the second from
   `pose-schema/drawn-value-schemas`, with which side of the scale the
   answer falls on coming from `defaults/aesthetic-registry`. Nothing
   is restated here.

   Two functions, meant to be called in that order -- `source` first,
   because whether a column's values are worth reading depends on the
   answer:

       (let [src  (source v col-names)
             vals (when (= :column src) (get ds v))]
         (scaled? k {:source src :value v :column-values vals}))"
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
   whether it was allowed."
  [v column-names]
  (if (contains? column-names v) :column :value))

(defn drawable?
  "True when `aesthetic` could draw `v` as it stands -- `\"red\"` or
   `:steelblue` on `:color`, `7` on `:size`, `:circle` on `:shape`.

   Reads `pose-schema/drawn-value-schemas`, so the grammar is stated
   once and this is what keeps it honest. An aesthetic absent from that
   map draws nothing: `:group` splits the data and has no appearance of
   its own.

   Answers two questions that happen to coincide. For a `:by-value`
   aesthetic it decides scaling, because being drawable *is* being in
   the vocabulary -- `\"red\"` is a color, `\"setosa\"` is not. For every
   aesthetic it also decides whether a value that names no column can
   be used at all, which is the third step of the rule: ask the data,
   ask what the aesthetic can draw, then report that neither fits. The
   two do not coincide for `:size`, where `7` is drawable and a datum
   alike -- which is why `:size` decides by source and never asks this."
  [aesthetic v]
  (if-let [schema (get pose-schema/drawn-value-schemas aesthetic)]
    (m/validate schema v)
    false))

(defn scaled?
  "Whether this mapping passes through `aesthetic`'s scale.

   `source` and `value` are what `source` above returned and what was
   written. `column-values` are the named column's values, needed only
   when a `:by-value` aesthetic was given a column. `scale` is an
   explicit `:scale` from the mapping, and overrides the convention in
   either direction -- `false` draws, anything else scales.

   The convention comes from the registry's `:scale-default`:

   - `:always` -- scaled whatever the source. Every `:x` and `:y`.
   - `:by-source` -- a column is scaled, a written value is drawn.
     `:size` and `:alpha`, where no value predicate could decide.
   - `:by-value` -- the vocabulary decides. A written color is drawn; a
     column is drawn only when **every non-missing value** of it is a
     color, which is what earns the identity mapping without anyone
     asking for it.
   - `:never` -- the aesthetic has a reading but no scale. `:text`.
   - `nil` -- no scale at all. `:group` splits the data and draws
     nothing of its own, which is why `pj/scale` refuses it too. An
     explicit `:scale` cannot conjure one, so it is ignored here and
     reported where the mapping is built.

   Requiring *all* values keeps the column case honest: one `\"setosa\"`
   among the colors and the column is categorical again. `every?`
   stops at the first, so the common path costs one check and only a
   column that really is all colors pays a full scan. Missing values
   are skipped, matching how a `nil` in a categorical column is already
   dropped -- but a column with nothing left after skipping them is
   scaled, since vacuous agreement is not evidence."
  [aesthetic {:keys [source value column-values scale]}]
  (let [{:keys [scale-default]} (defaults/aesthetic-registry aesthetic)]
    (cond
      (nil? scale-default)  false
      (false? scale)        false
      (some? scale)         true
      :else
      (case scale-default
        :always    true
        :never     false
        :by-source (= :column source)
        :by-value  (if (= :column source)
                     (let [present (remove nil? column-values)]
                       (not (and (seq present)
                                 (every? #(drawable? aesthetic %) present))))
                     (not (drawable? aesthetic value)))))))
