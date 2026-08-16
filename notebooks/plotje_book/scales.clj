;; # Scales
;;
;; A scale is what turns a column of data into something a reader can
;; see: a place along an axis, a radius, an opacity, a color, a symbol.
;; Every plot has scales whether or not you write one -- this chapter
;; is about the times you want to say which.
;;
;; It gathers what the rest of the book teaches piecemeal:
;; column-to-aesthetic mapping in
;; [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers),
;; the mapping written out in full in Specifying Aesthetics, log axes
;; and gradients in
;; [Customization](./plotje_book.customization.html#scales), and free
;; domains under [Faceting](./plotje_book.faceting.html).

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

;; ## What a scale is made of
;;
;; Two ends and a rule between them. The **domain** is the stretch of
;; data the scale reads, taken from the column unless you say
;; otherwise. The **range** is what the mark spans as a value runs
;; across that domain -- for an axis it is the panel, which the plot's
;; size decides; for a size it is a radius in drawing units. The
;; **type** is the rule: `:linear` reads differences, `:log` reads
;; ratios.
;;
;; A plot's data usually has more than one scale running at once, and
;; each is a separate decision:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:size :cyl :color :class}))

;; Three scales there. `:displ` and `:hwy` are read by the two axis
;; scales; `:cyl` by the size scale, which spreads it across the radii;
;; `:class` by the color scale, which hands each category a palette
;; color. The legends are how the plot explains the two that have no
;; axis to be read against.

;; ## Where a scale is written
;;
;; Two places, and they nest.
;;
;; `pj/scale` writes on a pose, and flows down to everything beneath
;; it. On a leaf pose that is the whole plot:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))])

;; On one cell of a composite it is that cell, so two cells of one plot
;; can carry different scales:

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

;; Under faceting the panels all come from one leaf, so they share a
;; type. What they need not share is a domain: `{:scales :free}` lets
;; each panel cover its own data.

;; The second place is the mapping itself, written out in full. A
;; `:scale` there says which scale that one mapping is read through:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:size {:column :cyl :scale :log}}))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))])

;; Where both are written, the mapping is the narrower scope and wins,
;; key by key. A pose that sets a range and a mapping that names a type
;; give a plot with both. `true` is not an opinion about which scale --
;; it says the value passes through whatever scale the aesthetic has --
;; so a `pj/scale` above it still decides the type.
;;
;; The axes are the exception. One panel has one x axis, so its layers
;; cannot each have their own, and `:x` and `:y` take `true` and
;; `false` in a mapping and nothing more.

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
;; An option a channel does not read is refused where it is written
;; rather than dropped. An axis has no range to set, since the panel
;; decides that:

(try
  (-> (rdatasets/ggplot2-mpg)
      (pj/lay-point :displ :hwy)
      (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #":x reads no :range" m))])

;; `:text` and `:group` have no scale at all -- a label is drawn as it
;; stands, and grouping draws nothing of its own -- so both refuse a
;; `:scale` outright.

;; ## Size: which quantity, and how it grows
;;
;; A size scale has a decision the others do not. A circle drawn at
;; twice the radius covers four times the ink, so how a value spreads
;; across the radii decides what the reader perceives.
;;
;; `:by` names what spreads evenly across the range as the value runs
;; across the domain:
;;
;; - `:sqrt` -- the default. The square root of the value spreads
;;   across the radii, which is what ggplot2's `scale_size` does. The
;;   smallest value still draws at the low end of the range rather than
;;   vanishing.
;; - `:linear` -- the radius itself carries the value, which is
;;   ggplot2's `scale_radius`. Differences look larger than they are.
;; - `:area` -- the ink spreads evenly, so equal steps in value are
;;   equal steps in area. The strict reading of what a size claims.
;;
;; The same six values under the three methods:

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

;; The three agree at the ends of the domain -- the smallest value
;; draws at the low end of the range and the largest at the high end,
;; whichever method is chosen -- and differ everywhere between. The
;; legend is where the sizes can be read off, since it is built from
;; the same function the marks are drawn from:

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
    ;; At every value the legend labels, `:linear` draws the smallest
    ;; mark and `:sqrt` the largest, with `:area` between them. The
    ;; labelled values stop short of the domain's own ends, which is
    ;; why none of the three columns reads 2.00 or 8.00.
    (every? (fn [[l a s]] (< l a s))
            (map vector
                 (legend-magnitudes {:by :linear})
                 (legend-magnitudes {:by :area})
                 (legend-magnitudes {:by :sqrt}))))])

;; `:range` is what the channel spans, in the quantity the mark draws
;; it as -- a radius in drawing units for a point, and 2 to 8 by
;; default. A wider range draws every mark wider:

(-> squares
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:range [3 20]}))

(kind/test-last
 [(fn [fr]
    (let [widest (->> fr pj/plan :size-legend :entries
                      (map :magnitude) (apply max) double)]
      ;; Wider than the default range's 8, and inside the 20 asked for
      ;; -- the largest labelled value is below the largest datum.
      (< 8.0 widest 20.0)))])

;; `:from-zero` anchors both the domain and the range at zero, which is
;; what turns a spread into a proportion: with it, twice the value is
;; twice the ink, and the reader can compare two marks as a ratio.
;; Beside `:by :area` it is ggplot2's `scale_size_area`.

(-> squares
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size {:by :area :from-zero true}))

(kind/test-last
 [(fn [fr]
    (let [{:keys [entries]} (-> fr pj/plan :size-legend)
          ink (fn [e] (Math/pow (:magnitude e) 2))
          by-value (into {} (map (juxt :value ink) entries))]
      ;; Whichever two labelled values are in a 2-to-1 ratio, their
      ;; areas are too.
      (every? (fn [[v a]]
                (if-let [half (by-value (/ v 2))]
                  (< (Math/abs (- (/ a half) 2.0)) 1e-6)
                  true))
              by-value)))])

;; A log scale and `:from-zero` cannot both hold -- a log scale has no
;; reading for zero -- and asking for both says so.

;; ## Which quantity a mark draws
;;
;; `:by :area` means the same thing on any mark because each mark
;; declares what it draws a channel as. A point declares that its
;; `:size` is a radius and its `:alpha` an opacity; a mark that drew a
;; stroke would declare a width. The declaration says how ink grows
;; with the quantity -- as the square for a radius, linearly for a
;; width -- so the scale can spread ink evenly without knowing which
;; shape draws it.

(:varies (layer-type/lookup :point))

(kind/test-last
 [(fn [m] (= {:size :radius :alpha :opacity} m))])

;; On a quantity whose ink grows linearly the three methods are one
;; function, because there is no area to correct for. That is why
;; `:alpha` takes a `:range` but no `:by`: an opacity has no shape.
;;
;; The declaration is also what earns a channel its legend. A mark that
;; does not declare a channel draws one value for the whole layer, so a
;; column mapped to it varies nothing -- and the plot says so instead
;; of drawing a legend for an encoding the panel does not carry.

;; ## The legend explains one scale
;;
;; A size legend's swatches are drawn from the same function as the
;; marks, so the swatch beside a value is the size a mark of that value
;; is drawn at. Its shape follows the quantity: circles for a radius,
;; strokes of that thickness for a width.
;;
;; One legend can only explain one scale, so two layers that read one
;; channel through different scales are refused:

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
;; An axis scale carries the same `:type` and `:domain` as any other,
;; plus what it takes to write the ticks. `:domain` is a view window
;; rather than a filter -- it decides what the panel shows, not which
;; rows exist.

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy)
    (pj/scale :y {:domain [10 50] :breaks [10 20 30 40 50]}))

(kind/test-last
 [(fn [fr] (= [10.0 20.0 30.0 40.0 50.0]
              (->> fr pj/plan :panels first :y-ticks :values (mapv double))))])

;; `:labels` pairs custom text with `:breaks`, and `:n-ticks` thins a
;; crowded categorical axis. Both are in
;; [Customization](./plotje_book.customization.html#scales).

;; ## Color, fill and shape
;;
;; A numeric `:color` or `:fill` column is read through a gradient, and
;; its scale type spaces the gradient rather than the marks: `:log`
;; makes each factor cover the same part of the ramp. A categorical
;; column takes a palette instead, and its scale's `:domain` gives the
;; category order the legend follows.
;;
;; `:shape` is categorical only -- there is no continuous reading of a
;; symbol -- and its `:values` names which symbols to draw, in the same
;; order as its `:domain`.

;; ## What has no spelling yet
;;
;; - Two layers of one pose cannot read a channel through different
;;   scales. On `:x` and `:y` that is not a gap, since one panel has
;;   one of each axis. On `:size`, `:color` and `:alpha` it is one, and
;;   it waits on a plot being able to carry two legends for one
;;   channel.
;; - The panels of a facet share their scale types. They can already
;;   vary their domains, with `{:scales :free}`.
;; - A `:size` mark scales against its own panel while its legend
;;   scales against the whole plot, so two facet panels holding
;;   different ranges can draw alike.

;; ## See Also
;;
;; - [Customization](./plotje_book.customization.html#scales) -- log
;;   axes, gradients and tick text
;; - [Faceting](./plotje_book.faceting.html) -- shared and free domains
;; - [Glossary](./plotje_book.glossary.html#scale) -- the one-paragraph
;;   definition
