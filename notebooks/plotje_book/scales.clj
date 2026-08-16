;; # Scales
;;
;; A scale maps data values to something visible: a place along an
;; axis, a radius, an opacity, a color, or a symbol. Every plot has
;; scales, whether or not you set any. This chapter is about setting
;; them.
;;
;; Scales come up in several other chapters -- column-to-aesthetic
;; mapping in
;; [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers),
;; the full mapping form in Specifying Aesthetics, log axes and
;; gradients in [Customization](./plotje_book.customization.html#scales),
;; and free domains in [Faceting](./plotje_book.faceting.html). This
;; chapter collects the scale material in one place.

(ns plotje-book.scales
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Plotje -- the layer-type registry, where a mark declares what it varies
   [scicloj.plotje.layer-type :as layer-type]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]))

;; The examples use one year of the gapminder data. Its columns cover
;; wide intervals -- income per person runs from about 300 to about
;; 50,000, and population from about 200,000 to about 1.3 billion --
;; which is what makes the difference between a linear and a log scale
;; easy to see.

(def gapminder-2007
  (-> (rdatasets/gapminder-gapminder)
      (tc/select-rows #(= 2007 (:year %)))))

gapminder-2007

;; ## The parts of a scale
;;
;; Three parts of a scale come up in most of what follows:
;;
;; - The **domain** is the data the scale reads: the lowest and highest
;;   values for a continuous scale, or the list of categories for a
;;   categorical one. It is taken from the column unless you set it.
;; - The **range** is what the mark spans. For an axis it is the panel,
;;   whose size follows from the plot dimensions. For `:size` it is a
;;   radius in drawing units, from 2 to 8 by default.
;; - The **type** is how values are spaced. `:linear` reads
;;   differences and `:log` reads ratios; `:categorical` gives each
;;   distinct value its own place.
;;
;; A scale spec can carry more than these -- `:by` and `:from-zero` on
;; `:size`, tick options on an axis, symbols on `:shape`. Which keys an
;; aesthetic reads is listed further down.
;;
;; A plot usually has several scales at once, and each one is set
;; separately.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:size :pop :color :continent}))

;; This plot has four scales: `:gdp-percap` and `:life-exp` on the two
;; axes, `:pop` on size, and `:continent` on color. The two that have
;; no axis are explained by legends.

;; ## Where a scale is set
;;
;; A scale belongs to the aesthetic it reads, so it is set in that
;; aesthetic's mapping. There are two ways to write it, and both end
;; up in the same place.
;;
;; `pj/scale` is the first. Its second argument is the aesthetic,
;; which can be either axis or any visual one: `:x`, `:y`, `:color`,
;; `:size`, `:alpha`, `:fill` or `:shape`. The examples in this section
;; use `:x`, but the same call works for the others, and each
;; aesthetic is a separate setting.
;;
;; A scale set this way applies to the pose it is called on and to
;; everything below it.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp)
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))])

;; Written on one plot of a composite, a scale covers that plot alone,
;; so two of them can differ:

(def linear-cell
  (-> gapminder-2007
      (pj/lay-point :gdp-percap :life-exp)
      (pj/options {:title "linear"})))

(def log-cell
  (-> linear-cell
      (pj/scale :x :log)
      (pj/options {:title "log"})))

(pj/arrange [linear-cell log-cell])

(kind/test-last
 [(fn [fr] (= [:linear :log]
              (->> (pj/plan fr) :sub-plots
                   (mapv #(-> % :plan :panels first :x-scale :type)))))])

;; On the left most countries are pressed against the edge, because a
;; few rich ones stretch the axis. The log plot spreads them out, since
;; equal distances there mean equal ratios.

;; The second way is to write the mapping out in full and give it a
;; `:scale` key, which sets the scale for that mapping alone. Here
;; `:size` reads population through a log scale, so that the smaller
;; countries are still distinguishable; the x axis is log-scaled
;; separately, by `pj/scale`:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:size {:column :pop :scale :log}})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))])

;; ### Scales accumulate
;;
;; Wherever a scale is set -- with `pj/scale`, in a pose's mapping, in
;; a layer's mapping -- the settings accumulate down the scope chain,
;; and the innermost wins for each key it names. So a range set once on
;; the pose and a type named on a layer give a plot with both:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp {:size {:column :pop :scale {:range [3 16]}}})
    (pj/lay-point {:size {:column :pop :scale :log}})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= {:range [3 16] :type :log}
              (-> fr pj/plan :panels first :layers first :size-scale)))])

;; The same holds when the range comes from `pj/scale` rather than from
;; the pose's mapping:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp)
    (pj/lay-point {:size {:column :pop :scale :log}})
    (pj/scale :size {:range [3 16]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= {:type :log :range [3 16]}
              (-> fr pj/plan :panels first :layers first :size-scale)))])

;; This is only true of the scale. The rest of a mapping is replaced by
;; the mapping below it, because a mapping states one source and two
;; sources cannot combine -- `{:column :n :value 7}` is refused by
;; name. A scale is a set of independent settings, so it accumulates.
;;
;; `:scale false` is not a setting to accumulate. It says the value
;; passes through no scale at all, so it replaces whatever was set
;; above.
;;
;; A column read this way has to hold what the aesthetic draws. For
;; `:size` that is a radius in drawing units, so the column below
;; holds 4, 8 and 12 -- not a population count, which would ask for
;; circles millions of units across:

(def measured-radii
  [{:reading 1 :level 2 :spread 4}
   {:reading 2 :level 5 :spread 8}
   {:reading 3 :level 3 :spread 12}])

(-> measured-radii
    (pj/pose :reading :level {:size {:column :spread :scale {:range [3 16]}}})
    (pj/lay-point {:size {:column :spread :scale false}}))

(kind/test-last
 [(fn [fr] (nil? (-> fr pj/plan :panels first :layers first :size-scale)))])

;; `:scale true` says only that the value passes through the
;; aesthetic's scale. It sets no type and no other key, so the scale it
;; passes through is whatever `pj/scale` set on the pose, or the
;; default where nothing set one.

;; ### A type, or a map
;;
;; Both ways take the scale in the same two forms: a type keyword, or
;; a map. The keyword is shorthand -- `:log` is read as `{:type :log}`
;; where it is written, and everything after that sees the map.
;;
;; With `pj/scale`, the two spellings leave the same scale on the axis:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp)
    (pj/scale :x :log)
    pj/plan
    :panels first :x-scale)

(kind/test-last [(fn [spec] (= {:type :log} spec))])

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp)
    (pj/scale :x {:type :log})
    pj/plan
    :panels first :x-scale)

(kind/test-last [(fn [spec] (= {:type :log} spec))])

;; and in a mapping for the same aesthetic they do the same:

(-> gapminder-2007
    (pj/lay-point {:x {:column :gdp-percap :scale :log} :y :life-exp})
    pj/plan
    :panels first :x-scale)

(kind/test-last [(fn [spec] (= {:type :log} spec))])

(-> gapminder-2007
    (pj/lay-point {:x {:column :gdp-percap :scale {:type :log}} :y :life-exp})
    pj/plan
    :panels first :x-scale)

(kind/test-last [(fn [spec] (= {:type :log} spec))])

;; The map is what says more than a type can. Which keys it may carry
;; depends on the aesthetic, and a key the aesthetic does not read is
;; refused rather than ignored -- in `pj/scale` and in a mapping alike:

(try
  (-> gapminder-2007
      (pj/lay-point :gdp-percap :life-exp)
      (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m))])

;; ### `pj/scale` compared with a mapping's `:scale`
;;
;; They write the same thing in the same place, so for an aesthetic
;; mapped on the pose they produce the same plot. The scale the size
;; mapping is read through, written in the mapping:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp {:size {:column :pop :scale :log}})
    pj/lay-point
    pj/plan
    :panels first :layers first :size-scale)

(kind/test-last [(fn [spec] (= {:type :log} spec))])

;; and the same mapping with the scale set by `pj/scale`:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp {:size :pop})
    pj/lay-point
    (pj/scale :size :log)
    pj/plan
    :panels first :layers first :size-scale)

(kind/test-last
 [(fn [spec]
    (= spec
       (-> gapminder-2007
           (pj/pose :gdp-percap :life-exp {:size {:column :pop :scale :log}})
           pj/lay-point
           pj/plan
           :panels first :layers first :size-scale)))])

;; What differs is only what else they say. A mapping's `:scale` comes
;; with a mapping, so it also names the column or value the aesthetic
;; reads; `pj/scale` sets the scale on its own, leaving the source to
;; be named wherever it already is -- including on a layer, below the
;; pose the call was made on.
;;
;; Everything a spec can carry is available in both. A mapping can set
;; an axis type, its `:domain`, and its tick options:

(-> gapminder-2007
    (pj/lay-point {:x :gdp-percap
                   :y {:column :life-exp
                       :scale {:domain [35 85] :breaks [40 60 80]}}}))

(kind/test-last
 [(fn [fr] (= [40.0 60.0 80.0]
              (->> fr pj/plan :panels first :y-ticks :values (mapv double))))])

;; ### What `:x` and `:y` do with a `:scale`
;;
;; The axes take the same values as the other aesthetics -- a type, a
;; spec, `true` or `false` -- but two of those answer different
;; questions, and it is worth separating them.
;;
;; A type or a spec describes the panel's axis. Writing it in a mapping
;; and writing it with `pj/scale` come to the same thing, and the axis
;; takes it from whichever layer names it:

(-> gapminder-2007
    (pj/pose {:x {:column :gdp-percap :scale {:type :log}} :y :life-exp})
    pj/lay-point)

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))])

;; What a panel cannot have is two of them. Every layer is drawn
;; against the one axis, so two layers naming different scales for it
;; are refused. A layer naming none is no disagreement -- it is drawn
;; against whichever axis the panel has.
;;
;; `:scale false` is a different matter. It does not select another
;; scale: it says the value is not a data value at all, but a distance
;; in drawing units from the top left of the panel background, so the
;; mark is placed on the panel rather than in the data. That is a
;; per-layer question -- one layer can be placed on the panel while
;; another is placed in the data -- and `{:in :drawing-area}` says the
;; same for both axes of a layer at once.
;;
;; The aesthetics drawn *through* the axes -- `:x-end`, `:x-min`,
;; `:x-max`, `:y-min`, `:y-max` -- have no scale of their own, so they
;; take no `:scale`. Their values are read on the panel's axis, which
;; is where the scale is set.

;; ## What each aesthetic's scale takes
;;
;; | Aesthetic | Types | Beside `:type` and `:domain` |
;; |:--------|:------|:-----------------------------|
;; | `:x`, `:y` | `:linear`, `:log`, `:categorical` | `:breaks`, `:labels`, `:n-ticks`, `:label` |
;; | `:size` | `:linear`, `:log` | `:range`, `:by`, `:from-zero` |
;; | `:alpha` | `:linear`, `:log` | `:range` |
;; | `:color`, `:fill` | `:linear`, `:log` | -- |
;; | `:shape` | `:categorical` | `:values` |
;;
;; An option an aesthetic does not read is refused where it is written,
;; rather than accepted and ignored. An axis has no range to set,
;; because the panel size determines it:

(try
  (-> gapminder-2007
      (pj/lay-point :gdp-percap :life-exp)
      (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #":x reads no :range" m))])

;; `:text` and `:group` have no scale at all. A label is drawn as
;; written, and grouping only splits the data into drawn groups, so
;; `:scale` is refused on both.

;; ## How a size spreads across its range
;;
;; A circle with twice the radius covers four times the area. Readers
;; compare the area, so the way values spread across the radii changes
;; what the plot appears to say.
;;
;; `:by` sets that spreading:
;;
;; - `:sqrt` (the default) spreads the square root of the value across
;;   the radii, which is what ggplot2's `scale_size` does. The smallest
;;   value is drawn at the low end of the range rather than at zero.
;; - `:linear` spreads the value itself across the radii, as ggplot2's
;;   `scale_radius` does. Differences in area then exaggerate
;;   differences in value.
;; - `:area` spreads the area, so equal steps in value give equal steps
;;   in area.
;;
;; The same six values under all three:

(def squares
  {:x [1 2 3 4 5 6] :y [1 1 1 1 1 1] :n [1 4 9 16 25 36]})

(-> (pj/arrange
     [(-> squares (pj/lay-point :x :y {:size :n})
          (pj/options {:title ":sqrt (default)"}))
      (-> squares (pj/lay-point :x :y {:size :n}) (pj/scale :size {:by :linear})
          (pj/options {:title ":linear"}))
      (-> squares (pj/lay-point :x :y {:size :n}) (pj/scale :size {:by :area})
          (pj/options {:title ":area"}))])
    (pj/options {:width 900 :height 340}))

;; All three draw the smallest value at the low end of the range and
;; the largest at the high end. They differ for the values in between.
;; The legend is computed from the same function as the marks, so its
;; sizes show the difference:

(defn legend-magnitudes
  [spec]
  (->> (-> squares
           (pj/lay-point :x :y {:size :n})
           (pj/scale :size spec)
           pj/plan
           :size-legend
           :entries)
       (mapv :magnitude)))

(kind/table
 {:column-names ["by" "smallest labelled" "middle" "largest labelled"]
  :row-maps (for [by [:linear :area :sqrt]
                  :let [ms (legend-magnitudes {:by by})
                        mid (nth ms (quot (count ms) 2))]]
              {"by" (kind/code (pr-str by))
               "smallest labelled" (format "%.2f" (first ms))
               "middle" (format "%.2f" mid)
               "largest labelled" (format "%.2f" (last ms))})})

(kind/test-last
 [(fn [_]
    ;; For every value the legend labels, `:linear` gives the smallest
    ;; radius and `:sqrt` the largest, with `:area` between them. The
    ;; labelled values stop short of the ends of the domain, which is
    ;; why no column shows 2.00 or 8.00.
    (every? (fn [[l a s]] (< l a s))
            (map vector
                 (legend-magnitudes {:by :linear})
                 (legend-magnitudes {:by :area})
                 (legend-magnitudes {:by :sqrt}))))])

;; `:range` sets what the aesthetic spans, in the quantity the mark
;; draws -- a radius for a point. A wider one draws every mark wider:

(-> squares
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:range [3 20]}))

(kind/test-last
 [(fn [fr]
    (let [widest (->> fr pj/plan :size-legend :entries
                      (map :magnitude) (apply max) double)]
      ;; Wider than the default 8, and within the 20 that was set. It
      ;; does not reach 20 because the largest labelled value is below
      ;; the largest value in the data.
      (< 8.0 widest 20.0)))])

;; `:from-zero` starts both the domain and the range at zero. The area
;; is then proportional to the value: twice the value is twice the
;; area. Together with `:by :area` this matches ggplot2's
;; `scale_size_area`.

(-> squares
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:by :area :from-zero true}))

(kind/test-last
 [(fn [fr]
    (let [{:keys [entries]} (-> fr pj/plan :size-legend)
          area (fn [e] (Math/pow (:magnitude e) 2))
          by-value (into {} (map (juxt :value area) entries))]
      ;; Where the legend labels both a value and its half, the areas
      ;; are in the same 2-to-1 ratio.
      (every? (fn [[v a]]
                (if-let [half (by-value (/ v 2))]
                  (< (Math/abs (- (/ a half) 2.0)) 1e-6)
                  true))
              by-value)))])

;; `:from-zero` cannot be combined with a log scale, since a log scale
;; has no value for zero. Setting both is refused.

;; ## Which quantity a mark draws
;;
;; `:by :area` works the same way on every mark, because each mark
;; declares what it draws each aesthetic as. A point draws `:size` as a
;; radius and `:alpha` as an opacity. A mark drawing a stroke would
;; declare a width. The declaration also fixes how the area grows with
;; the quantity: as the square for a radius, linearly for a width. The
;; scale uses that, so it does not need to know the shape.

(:varies (layer-type/lookup :point))

(kind/test-last
 [(fn [m] (= {:size :radius :alpha :opacity} m))])

;; Where the area grows linearly with the quantity, the three `:by`
;; methods give the same result, since there is no area correction to
;; make. That is why `:alpha` takes `:range` but not `:by`: an opacity
;; has no shape.
;;
;; The declaration also decides which aesthetics get a legend. A mark
;; that does not declare an aesthetic draws one value for the whole layer,
;; so a column mapped to that aesthetic changes nothing. Plotje warns in
;; that case and draws no legend for it.

;; ## One legend per aesthetic
;;
;; A size legend's swatches are computed from the same function as the
;; marks, so the swatch next to a value is the size a mark of that
;; value is drawn at. The swatch shape follows the quantity: circles
;; for a radius, strokes for a width.
;;
;; A plot has one legend per aesthetic, so two layers cannot read one
;; aesthetic through different scales. That is refused:

(try
  (-> gapminder-2007
      (pj/pose :gdp-percap :life-exp)
      (pj/lay-point {:size {:column :pop :scale :log}})
      (pj/lay-point {:size {:column :pop :scale :linear}})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"read :size through different scales" m))])

;; ## Axes
;;
;; An axis scale takes `:type` and `:domain` like any other scale, plus
;; the options that control ticks. `:domain` sets what the panel shows.
;; It does not remove rows.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp)
    (pj/scale :y {:domain [35 85] :breaks [40 50 60 70 80]}))

(kind/test-last
 [(fn [fr] (= [40.0 50.0 60.0 70.0 80.0]
              (->> fr pj/plan :panels first :y-ticks :values (mapv double))))])

;; `:labels` pairs custom text with `:breaks`, and `:n-ticks` reduces
;; the number of labels on a crowded categorical axis. Both are covered
;; in [Customization](./plotje_book.customization.html#scales).

;; ## Color, fill and shape
;;
;; A numeric `:color` or `:fill` column is drawn through a gradient.
;; The scale type spaces that gradient: with `:log`, each factor covers
;; the same part of it. A categorical column uses a palette instead.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :pop})
    (pj/scale :color :log)
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :legend :scale-type)))])

;; `:shape` is categorical only, since symbols have no continuous
;; ordering. A `:domain` sets the category order the legend follows,
;; and `:values` sets which symbols to use, in that same order.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:shape :continent})
    (pj/scale :shape {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= ["Africa" "Americas" "Asia" "Europe" "Oceania"]
              (mapv :label (:entries (:shape-legend (pj/plan fr))))))])

;; `:color` and `:fill` read a categorical `:domain` the same way. The
;; palette is assigned in the order given, so the domain moves the
;; legend rows and the colors together -- the first category listed
;; takes the palette's first color wherever it sits in the data:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :continent})
    (pj/scale :color {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= ["Oceania" "Europe" "Asia" "Americas" "Africa"]
              (mapv :label (:entries (:legend (pj/plan fr))))))])

;; A category the domain leaves out is still drawn, ordered after the
;; ones listed, and Plotje says so -- an incomplete list is usually a
;; typo or a stale set of names rather than a request.
;;
;; On a numeric column the same key means the other thing a domain can
;; mean: the two ends of the gradient. Population runs to well over a
;; billion, and a handful of countries at that end leave everywhere
;; else crowded into the dark. Ending the gradient at fifty million
;; spreads the countries most of the data holds:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :pop})
    (pj/scale :color {:domain [0 5.0E7]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= [0.0 5.0E7]
              ((juxt :min :max) (:legend (pj/plan fr)))))])

;; The countries above fifty million are drawn at the light end rather
;; than dropped -- a value outside a numeric domain takes the nearer
;; end of the gradient, as it takes the nearest radius on `:size`. A
;; domain says what the reader should compare, and a dropped row would
;; leave no trace on the panel to say so.
;;
;; Which of the two readings applies is decided by the domain itself:
;; two numbers are a range, and anything else is a list of categories.
;; Fixing the domain is also how every panel of a facet is given one
;; gradient to share.

;; ## Not supported yet
;;
;; - Two layers of one pose cannot read an aesthetic through different
;;   scales. For `:x` and `:y` this makes no difference, since a panel
;;   has one of each axis. For `:size` and `:alpha` it is a limitation,
;;   and the two layers are refused rather than drawn; lifting it
;;   requires a plot to carry two legends for one aesthetic. `:color`
;;   and `:fill` are not checked at all, so the first layer's scale
;;   silently decides for both.
;; - Facet panels share their scale types. Their domains can already
;;   differ: `{:scales :free}` gives each panel its own.
;; - A `:size` mark is scaled against its own panel, while the legend
;;   is scaled against the whole plot. Two facet panels whose values
;;   cover different intervals can therefore look the same. Setting
;;   `:domain` explicitly avoids this.

;; ## See Also
;;
;; - [Customization](./plotje_book.customization.html#scales) -- log
;;   axes, gradients and tick text
;; - [Faceting](./plotje_book.faceting.html) -- shared and free domains
;; - [Glossary](./plotje_book.glossary.html#scale) -- the short
;;   definition
