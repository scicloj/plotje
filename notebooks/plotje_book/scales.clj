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
;; There are two places.
;;
;; `pj/scale` sets a scale on a pose. Its second argument is the
;; aesthetic, which can be either axis or any visual aesthetic: `:x`, `:y`,
;; `:color`, `:size`, `:alpha`, `:fill` or `:shape`. The examples in
;; this section use `:x`, but the same call works for the others, and
;; each aesthetic is a separate setting.
;;
;; A scale set this way applies to the pose it is called on and to
;; everything below it.
;;
;; A scale is written either as a type keyword or as a map. The keyword
;; is shorthand: `:log` is read as `{:type :log}` where it is written,
;; and everything after that sees the map. So these two are the same
;; plot, and the same is true of a `:scale` in a mapping:

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

;; A map says more than a type can. Which keys it may carry depends on
;; the aesthetic, and a key the aesthetic does not read is refused
;; rather than ignored -- in `pj/scale` and in a mapping alike.
;;
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

;; The second place is the mapping. A mapping written out in full takes
;; a `:scale` key, which sets the scale for that mapping alone. Here
;; `:size` reads population through a log scale, so that the smaller
;; countries are still distinguishable; the x axis is log-scaled
;; separately, by `pj/scale`:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:size {:column :pop :scale :log}})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))])

;; A mapping can be written on the pose as well as on a layer, and its
;; `:scale` follows the ordinary scope rules. Here the pose maps
;; `:size` through a log scale and the layer overrides it:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp {:size {:column :pop :scale :log}})
    (pj/lay-point {:size {:column :pop :scale :linear}})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :linear (-> fr pj/plan :size-legend :scale-type)))])

;; The layer's mapping replaces the pose's for that aesthetic, so its
;; `:scale` replaces the pose's rather than merging into it. A `:range`
;; set in the pose's mapping is lost this way.
;;
;; `pj/scale` behaves differently, because it writes to the pose's
;; options rather than to a mapping. A mapping's `:scale` then
;; overrides it key by key. Here the range comes from `pj/scale` and
;; the type from the mapping:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp)
    (pj/lay-point {:size {:column :pop :scale :log}})
    (pj/scale :size {:range [3 16]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= {:type :log :range [3 16]}
              (-> fr pj/plan :panels first :layers first :size-scale)))])

;; `:scale true` says only that the value passes through the
;; aesthetic's scale. It sets no type and no other key, so the scale it
;; passes through is whatever `pj/scale` set on the pose, or the
;; default where nothing set one.

;; ### `pj/scale` compared with a mapping's `:scale`
;;
;; For an aesthetic mapped on the pose, the two produce the same plot.
;; The scale the size mapping is read through, written in the mapping:

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

;; They differ in two ways:
;;
;; - `pj/scale` sets an aesthetic's scale on its own. A mapping's
;;   `:scale` comes with a mapping, so it also says which column or
;;   value the aesthetic reads. That is the practical difference: to
;;   set a scale without touching the mapping, use `pj/scale`.
;; - Where both are written, the mapping wins.
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
;; | `:x`, `:y` | `:linear`, `:log`, `:categorical` | `:breaks`, `:labels`, `:n-ticks` |
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
;; the same part of it. A categorical column uses a palette instead,
;; and `:domain` sets the category order that the legend follows.
;;
;; `:shape` is categorical only, since symbols have no continuous
;; ordering. Its `:values` sets which symbols to use, in the same order
;; as `:domain`.

;; ## Not supported yet
;;
;; - Two layers of one pose cannot read an aesthetic through different
;;   scales. For `:x` and `:y` this makes no difference, since a panel
;;   has one of each axis. For `:size`, `:color` and `:alpha` it is a
;;   limitation, and lifting it requires a plot to carry two legends
;;   for one aesthetic.
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
