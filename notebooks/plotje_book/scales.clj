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
   [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ## The parts of a scale
;;
;; A scale has three parts:
;;
;; - The **domain** is the range of data values the scale reads. It is
;;   taken from the column unless you set it.
;; - The **range** is what the mark spans. For an axis it is the panel,
;;   whose size follows from the plot dimensions. For `:size` it is a
;;   radius in drawing units.
;; - The **type** is how values are spaced: `:linear` for differences,
;;   `:log` for ratios.
;;
;; A plot usually has several scales at once, and each one is set
;; separately.

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:size :cyl :color :class}))

;; This plot has four scales: `:displ` and `:hwy` on the two axes,
;; `:cyl` on size, and `:class` on color. The two that have no axis are
;; explained by legends.

;; ## Where a scale is set
;;
;; There are two places.
;;
;; `pj/scale` sets a scale on a pose. It applies to that pose and
;; everything below it. On a leaf pose that is the whole plot:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))])

;; On one cell of a composite it applies to that cell only, so two
;; cells can have different scales:

(def linear-cell
  (-> (rdatasets/ggplot2-mpg)
      (pj/lay-point :displ :hwy)
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

;; Facet panels all come from one leaf pose, so they share a scale
;; type. They do not have to share a domain: `{:scales :free}` gives
;; each panel its own.

;; The second place is the mapping. A mapping written out in full takes
;; a `:scale` key, which sets the scale for that mapping alone:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:size {:column :cyl :scale :log}}))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))])

;; When both are set, the mapping wins, key by key. If the pose sets a
;; range and the mapping sets a type, the plot uses both. `:scale true`
;; does not name a scale -- it says the value passes through whichever
;; scale the aesthetic has -- so a `pj/scale` above it still sets the
;; type.
;;
;; `:x` and `:y` are the exception. A panel has one x axis and one y
;; axis, so layers cannot set their own. In a mapping these two take
;; `true` and `false` only.

;; ## What each channel's scale takes
;;
;; | Channel | Types | Beside `:type` and `:domain` |
;; |:--------|:------|:-----------------------------|
;; | `:x`, `:y` | `:linear`, `:log`, `:categorical` | `:breaks`, `:labels`, `:n-ticks` |
;; | `:size` | `:linear`, `:log` | `:range`, `:by`, `:from-zero` |
;; | `:alpha` | `:linear`, `:log` | `:range` |
;; | `:color`, `:fill` | `:linear`, `:log` | -- |
;; | `:shape` | `:categorical` | `:values` |
;;
;; An option a channel does not read is refused where it is written,
;; rather than accepted and ignored. An axis has no range to set,
;; because the panel size determines it:

(try
  (-> (rdatasets/ggplot2-mpg)
      (pj/lay-point :displ :hwy)
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

;; `:range` sets what the channel spans, in the quantity the mark
;; draws. For a point that is a radius in drawing units, and the
;; default is 2 to 8. A wider range draws every mark wider:

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
;; declares what it draws each channel as. A point draws `:size` as a
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
;; The declaration also decides which channels get a legend. A mark
;; that does not declare a channel draws one value for the whole layer,
;; so a column mapped to that channel changes nothing. Plotje warns in
;; that case and draws no legend for it.

;; ## One legend per channel
;;
;; A size legend's swatches are computed from the same function as the
;; marks, so the swatch next to a value is the size a mark of that
;; value is drawn at. The swatch shape follows the quantity: circles
;; for a radius, strokes for a width.
;;
;; A plot has one legend per channel, so two layers cannot read one
;; channel through different scales. That is refused:

(try
  (-> (rdatasets/ggplot2-mpg)
      (pj/pose :displ :hwy)
      (pj/lay-point {:size {:column :cyl :scale :log}})
      (pj/lay-point {:size {:column :cyl :scale :linear}})
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

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/scale :y {:domain [10 50] :breaks [10 20 30 40 50]}))

(kind/test-last
 [(fn [fr] (= [10.0 20.0 30.0 40.0 50.0]
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
;; - Two layers of one pose cannot read a channel through different
;;   scales. For `:x` and `:y` this makes no difference, since a panel
;;   has one of each axis. For `:size`, `:color` and `:alpha` it is a
;;   limitation, and lifting it requires a plot to carry two legends
;;   for one channel.
;; - Facet panels share their scale types. Their domains can already
;;   differ, with `{:scales :free}`.
;; - A `:size` mark is scaled against its own panel, while the legend
;;   is scaled against the whole plot. Two facet panels with different
;;   value ranges can therefore look the same. Setting `:domain`
;;   explicitly avoids this.

;; ## See Also
;;
;; - [Customization](./plotje_book.customization.html#scales) -- log
;;   axes, gradients and tick text
;; - [Faceting](./plotje_book.faceting.html) -- shared and free domains
;; - [Glossary](./plotje_book.glossary.html#scale) -- the short
;;   definition
