;; # Scales
;;
;; A scale maps data values to something visible: a place along an
;; axis, a radius, an opacity, a color, or a symbol. Every plot has
;; scales, whether or not you set any. This chapter is about setting
;; them.
;;
;; Scales are named in several other chapters -- column-to-aesthetic
;; mapping in
;; [Core Concepts](./plotje_book.core_concepts.html#mappings-and-layers),
;; the full mapping form in
;; [Specifying Aesthetics](./plotje_book.specifying_aesthetics.html),
;; free domains in [Faceting](./plotje_book.faceting.html), and the
;; catalogue of named palettes and gradients in
;; [Customization](./plotje_book.customization.html#discovering-palettes-and-gradients).
;; What a scale is, and what each one takes, is here.

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
   [tablecloth.api :as tc]
   ;; Tablecloth -- column-level operations
   [tablecloth.column.api :as tcc]
   ;; java-time -- dates, for the temporal breaks below
   [java-time.api :as jt]))

;; The examples use one year of the gapminder data.

(def gapminder-2007
  (-> (rdatasets/gapminder-gapminder)
      (tc/select-rows #(= 2007 (:year %)))))

gapminder-2007

;; Two of its columns cover very wide intervals, where a log scale and
;; a linear one differ most:

(-> gapminder-2007
    (tc/aggregate-columns [:gdp-percap :pop]
                          (fn [xs]
                            {:min (tcc/reduce-min xs)
                             :max (tcc/reduce-max xs)})))

;; ## The parts of a scale
;;
;; Three parts come up in most of what follows, and this chapter has a
;; section for each. The domain is the data side of a scale, the range
;; is the visible side, and the type is the relation between them.
;;
;; - The **type** decides how a data value becomes a drawn one. A
;;   `:linear` type maps equal differences in the data to equal
;;   differences in what is drawn, a `:log` type does the same for equal
;;   ratios, and a `:categorical` type gives each distinct value a place
;;   of its own.
;; - The **domain** is the extent of data values the scale reads: the
;;   lowest and highest value for a continuous scale, or the list of
;;   distinct categories for a categorical one. Plotje takes it from the
;;   column the aesthetic is mapped to unless the scale spec sets it.
;; - The **range** is the extent of drawn values the scale produces. For
;;   `:x` and `:y` it is the panel, measured in drawing units, whose
;;   size follows from the plot's `:width` and `:height`. For `:size` it
;;   is an interval of radii in drawing units, for `:alpha` an interval
;;   of opacities, for `:color` a palette or a gradient, for `:fill` a
;;   gradient, and for `:shape` a list of symbols.
;;
;; Written down, these go in a scale **spec** -- the map that says
;; which scale, and how. Two of the three are written the same way
;; throughout: `:type` and `:domain` are keys on every aesthetic that
;; has a scale. The range is not. `:size` and `:alpha` take a `:range`,
;; `:shape` names its symbols with `:values`, `:color` names a palette
;; with `:values` and a gradient with `:range`, and an axis has no
;; range to set, because the panel decides it. A spec can also carry keys that are
;; none of the three parts, such as `:by` on `:size` or the tick keys
;; on an axis. So the table further down lists `:type` and `:domain`
;; once, and then what each aesthetic reads beside them.
;;
;; A plot usually has several scales at once, and each one is set
;; separately.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:size :pop :color :continent}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)
          panel (first (:panels p))]
      ;; One scale per aesthetic mapped: the two axes, size and color.
      (and (some? (:x-scale panel))
           (some? (:y-scale panel))
           (some? (:size-legend p))
           (some? (:legend p)))))])

;; The pose maps four aesthetics, and the plot has one scale for each:
;; `:gdp-percap` and
;; `:life-exp` on the two axes, `:pop` on size, and `:continent` on
;; color. The two that have no axis are explained by legends.

;; ## Where a scale is set
;;
;; A scale belongs to the aesthetic it reads, so it is set in that
;; aesthetic's mapping. There are two ways to write it, and both end
;; up in the same place.
;;
;; `pj/scale` is the first. Its second argument is the aesthetic,
;; which can be either axis or any appearance one: `:x`, `:y`, `:color`,
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

;; Written on one pose of a composite, a scale covers that pose alone,
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

;; ### A type, or a map
;;
;; Both ways take the scale in the same two forms: a type keyword, or
;; a spec map. The keyword is shorthand -- `:log` is read as
;; `{:type :log}` where it is written, and every stage after that
;; reads the map.
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

;; Which keys the map may carry depends on the aesthetic, and a key the
;; aesthetic does not read is refused rather than ignored -- in
;; `pj/scale` and in a mapping alike:

(try
  (-> gapminder-2007
      (pj/lay-point :gdp-percap :life-exp)
      (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m))])

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

;; The rest of a mapping is replaced by the mapping below it, because a
;; mapping states one source and two sources cannot combine --
;; `{:column :n :value 7}` is refused by name. A scale is a set of
;; independent settings, so it accumulates.
;;
;; `:scale false` does not accumulate: it says the value passes through
;; no scale at all, so it replaces whatever was set above.
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

;; On `:x` and `:y`, `:scale false` means the value is a distance in
;; drawing units from the top left of the panel background rather than
;; a data value, so the mark is placed on the panel rather than in the
;; data. One layer can be placed on the panel while another is placed
;; in the data, and `{:in :drawing-area}` says the same for both axes
;; of a layer at once. See
;; [Placing Marks](./plotje_book.placing_marks.html).
;;
;; `:scale true` says only that the value passes through the
;; aesthetic's scale. It sets no type and no other key, so the scale it
;; passes through is whatever `pj/scale` set on the pose, or the
;; default where nothing set one.

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

;; `:scale` is written where the mapping is, so the source can be named
;; beside it. `pj/scale` names no source, so it can set the scale for an
;; aesthetic that is mapped further in -- on a layer below the pose the
;; call was made on. Everything a spec can carry is available in both.

;; ## What each aesthetic's scale takes
;;
;; `pj/aesthetic-scales` is the table `pj/scale` and a mapping's
;; `:scale` are both checked against, so the one below is read out of
;; Plotje rather than written down beside it. `:types` are the types the
;; aesthetic can be read through, and `:keys` are the spec keys it reads
;; beside `:type` and `:domain`.

(tc/dataset pj/aesthetic-scales)

(kind/test-last
 [(fn [_]
    ;; Every aesthetic `pj/scale` accepts appears, and none that has no
    ;; scale does.
    (= #{:x :y :size :alpha :color :fill :shape}
       (set (map :aesthetic pj/aesthetic-scales))))])

;; A spec key an aesthetic does not read is refused where it is
;; written, rather than accepted and ignored. An axis has no range to set,
;; because the panel size determines it:

(try
  (-> gapminder-2007
      (pj/lay-point :gdp-percap :life-exp)
      (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #":x reads no :range" m))])

;; The aesthetics absent from the table have no scale to set. `:text`
;; and `:group` have none at all: a label is drawn as written, and
;; grouping only splits the data into drawn groups, so `:scale` is
;; refused on both. The aesthetics drawn *through* the axes --
;; `:x-end`, `:x-min`, `:x-max`, `:y-min`, `:y-max` -- take no
;; `:scale` either. Their values are read on the panel's axis, which is
;; where that scale is set.

;; ## Type
;;
;; The type decides how a data value becomes a drawn one.
;;
;; - A `:linear` type maps equal differences in the data to equal
;;   differences in what is drawn, so a difference of ten covers the
;;   same part of the range wherever in the domain it falls.
;; - A `:log` type maps equal ratios in the data to equal differences in
;;   what is drawn, so each factor of ten covers the same part of the
;;   range. This is what makes a column spanning orders of magnitude
;;   readable.
;; - A `:categorical` type has no arithmetic to preserve. It gives each
;;   distinct value in the domain a place of its own: a band on an axis,
;;   one color from a palette, or one symbol.
;;
;; The table above says which types each aesthetic accepts: the axes
;; take all three, `:size`, `:alpha`, `:color` and `:fill` take
;; `:linear` and `:log`, and `:shape` takes `:categorical` alone.
;; Asking an aesthetic for a type it does not accept is refused, and
;; the message lists the ones it does:

(try
  (-> gapminder-2007
      (pj/lay-point :gdp-percap :life-exp {:color :continent})
      (pj/scale :color :categorical)
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #"does not support a :categorical scale" m))])

;; That refusal is about the scale type, not about the column: a
;; categorical column mapped to `:color` is drawn from a palette
;; without any type being named. Which of the two happens is settled
;; below.

;; ### A log scale on an appearance aesthetic
;;
;; `:log` is not only for axes. Values that jump by factors of ten
;; crowd together at the bottom of a size range under the default
;; `:linear`, because a linear scale reads absolute distance, which the
;; largest value dominates. Here the step from 10 to 100 is a small
;; part of the way to 1000:

(def by-tens
  {:user [:a :b :c] :n [10 100 1000]})

(-> by-tens
    (pj/lay-point :user :n {:size :n :x-type :categorical}))

(kind/test-last
 [(fn [v]
    (let [[small mid large] (sort (:sizes (pj/svg-summary v)))]
      ;; The middle radius is nearer the smallest than the largest.
      (< (- mid small) (- large mid))))])

;; Under `:log` each factor of ten covers the same part of the range,
;; so the two smaller points are no longer pressed together -- the gap
;; between them is now the wider of the two:

(-> by-tens
    (pj/lay-point :user :n {:size :n :x-type :categorical})
    (pj/scale :size :log))

(kind/test-last
 [(fn [v]
    (let [[small mid large] (sort (:sizes (pj/svg-summary v)))]
      (and (= 3 (:points (pj/svg-summary v)))
           (> (- mid small) (- large mid)))))])

;; A `:color` or `:fill` gradient is spaced the same way. Under `:log`
;; each factor covers the same part of the gradient, so the countries
;; below the top of the population range are no longer all one shade:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :pop})
    (pj/scale :color :log)
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= :log (-> fr pj/plan :legend :scale-type)))])

;; ### What settles continuous or categorical
;;
;; Nothing above asked for a continuous scale or a categorical one. The
;; column's type decides, and the scale's type does not: a column of
;; numbers is read as a quantity, a column of anything else as a set of
;; categories, and a scale's `:type` then chooses between `:linear` and
;; `:log` for the numbers. Writing `:categorical` as a scale type cannot
;; make a column categorical -- on a numeric column it is refused, and
;; on a categorical one the scale's `:type` is not read at all.
;; [Column Types](./plotje_book.column_types.html#the-columns-type-and-the-scales-type)
;; sets the two side by side.
;;
;; On an axis the column's type is the difference between a measured
;; line and a row of bands, and a column of names takes the bands
;; without being asked:

(-> gapminder-2007
    (pj/lay-bar :continent))

(kind/test-last
 [(fn [fr] (= ["Asia" "Europe" "Africa" "Americas" "Oceania"]
              (->> fr pj/plan :panels first :x-domain vec)))])

;; Where the column's type is not what the values mean, `:x-type`,
;; `:y-type` and `:color-type` say so in the mapping. `:cyl` counts
;; cylinders -- 4, 5, 6 and 8 -- and the numbers stand for kinds of
;; engine rather than for a quantity to measure along. `:x-type
;; :categorical` says so, and each count gets its own band:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :cyl :hwy {:x-type :categorical}))

(kind/test-last
 [(fn [fr] (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))])

;; `:color` reads the same column the same way. Left alone, four counts
;; of cylinders are drawn through a gradient, as a continuous quantity:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:color :cyl}))

(kind/test-last
 [(fn [fr] (= :continuous (-> fr pj/plan :legend :type)))])

;; `:color-type :categorical` reads them as categories instead, and a
;; palette with one entry per count replaces the gradient:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :displ :hwy {:color :cyl :color-type :categorical}))

(kind/test-last
 [(fn [fr] (= ["4" "6" "8" "5"]
              (->> fr pj/plan :legend :entries (mapv :label))))])

;; Both plots put the five-cylinder cars last, because the bands and
;; the legend rows follow the order the rows arrive in. Ordering them
;; is what a `:domain` is for.
;;
;; What Plotje infers from a column, and every way of overriding it, is
;; in
;; [Inference Rules](./plotje_book.inference_rules.html#overriding-inferred-types-with-x-type-y-type).

;; ## Domain
;;
;; The domain is the extent of data values the scale reads. Plotje takes
;; it from the column the aesthetic is mapped to -- its lowest and
;; highest value, or its distinct categories -- and a `:domain` in the
;; scale spec replaces that.
;;
;; The column's type decides how a written `:domain` is read, not the
;; shape of the domain written. Against a continuous column a `:domain`
;; replaces the interval; against a categorical one it supplies the
;; order of the categories, however many are listed.

;; ### On an axis: a view window
;;
;; A `:domain` on `:x` or `:y` sets what the panel shows. It does not
;; remove rows. A mark outside the window is drawn and clipped at the
;; panel edge, so the data behind the plot is unchanged. This matches
;; ggplot2's `coord_cartesian`, which zooms the view, rather than its
;; scale limits, which drop rows.
;;
;; Here the sepal-width domain is tightened to a band the data
;; overflows in both directions. All 150 observations are still
;; rendered, behind one clip region:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:domain [3.0 3.5]}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 150 (:points s))
                 (= 1 (:clips s)))))])

;; ### Where the ticks go
;;
;; An axis scale carries the keys that place and word its tick marks. `:breaks` pins the values that get a tick, which is
;; ggplot2's `scale_*_continuous(breaks=)`:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp)
    (pj/scale :y {:breaks [40 50 60 70 80]}))

(kind/test-last
 [(fn [fr] (= [40.0 50.0 60.0 70.0 80.0]
              (->> fr pj/plan :panels first :y-ticks :values (mapv double))))])

;; `:tick-labels` pairs custom text with those breaks, one label each. It is
;; how an axis that is numerically indexed gets worded labels -- days
;; of the week along a heatmap indexed 1 to 7, for instance. That case
;; is worked through in
;; [Customization](./plotje_book.customization.html#tick-placement-and-text).
;;
;; On a categorical axis the same two keys select and reword. Each
;; break is matched to a category by the text it is drawn with, and a
;; break naming no category is dropped with a warning. Here two of the
;; four quarters are ticked, with text of their own:

(-> {:quarter ["Q1" "Q2" "Q3" "Q4"]
     :revenue [120 150 90 200]}
    (pj/lay-bar :quarter :revenue)
    (pj/scale :x {:breaks ["Q1" "Q4"] :tick-labels ["First" "Fourth"]}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (and (contains? texts "First")
                 (contains? texts "Fourth")
                 (not (contains? texts "Q2")))))])

;; A temporal axis takes breaks written as dates. Ten years of
;; readings, ticked once a decade rather than wherever the generator
;; would have put them:

(-> {:when (mapv (fn [year] (jt/local-date year 6 15)) (range 2015 2025))
     :reading [12 15 11 18 22 19 25 24 28 31]}
    (pj/lay-line :when :reading)
    (pj/scale :x {:breaks [(jt/local-date 2016 1 1)
                           (jt/local-date 2020 1 1)
                           (jt/local-date 2024 1 1)]}))

(kind/test-last
 [(fn [fr] (= ["2016" "2020" "2024"]
              (->> fr pj/plan :panels first :x-ticks :labels)))])

;; A `LocalDate`, a `LocalDateTime` and a `java.util.Date` are all
;; accepted, and `:tick-labels` rewords them as it does any other break.

;; A categorical axis labels every category by default, so with many of
;; them the labels overlap. `:n-ticks` keeps roughly that many
;; evenly-spaced labels instead. Explicit `:breaks` win where both are
;; given, and rotating the labels with `:x-tick-angle` is the other
;; answer to crowding -- see
;; [Customization](./plotje_book.customization.html#rotating-tick-labels).

(-> {:bin (map #(str "bin-" %) (range 40))
     :count (range 40)}
    (pj/lay-bar :bin :count)
    (pj/scale :x {:n-ticks 8}))

(kind/test-last
 [(fn [v] (let [labels (filter #(.startsWith ^String % "bin-")
                               (:texts (pj/svg-summary v)))]
            (= 8 (count labels))))])

;; A numeric axis reads `:n-ticks` too. Its ticks are chosen rather
;; than listed, so the count is a target: the chooser rounds to values
;; a reader can read off, and may land near the number asked for rather
;; than on it.

(-> {:hour (range 20) :load (range 20)}
    (pj/lay-point :hour :load)
    (pj/scale :x {:n-ticks 3}))

(kind/test-last
 [(fn [v] (= [0.0 5.0 10.0 15.0]
             (->> v pj/plan :panels first :x-ticks :values (mapv double))))])

;; `:tick-spacing` asks the same question the other way round: about
;; how much room, in drawing units, each tick should have. The count
;; is then how many fit. It is a target in the way `:n-ticks` is, and
;; for the same reason -- the chooser still rounds to values a reader
;; can read off, so the room each tick ends up with can come out under
;; the number asked for.

(-> {:hour (range 20) :load (range 20)}
    (pj/lay-point :hour :load)
    (pj/scale :x {:tick-spacing 200}))

(kind/test-last
 [(fn [v] (= 2 (->> v pj/plan :panels first :x-ticks :values count)))])

;; `:x-tick-spacing` and `:y-tick-spacing` in `pj/options` name that
;; setting one scope further out, and a spec wins over them. There is
;; nothing for a spacing to steer on a categorical axis, whose ticks
;; are its categories, so one written in a spec there says so and
;; leaves the categories alone.

;; The axis title is a separate key, `:label`, which every aesthetic
;; reads -- see [Titling a scale](#titling-a-scale).

;; ### Ordering categories
;;
;; On a categorical column a `:domain` supplies the order of the
;; categories -- the order of the bands on an axis, and of the rows in
;; a legend. The continents above arrived in the order the data holds
;; them; this puts them in another:

(-> gapminder-2007
    (pj/lay-bar :continent)
    (pj/scale :x {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]}))

(kind/test-last
 [(fn [fr] (= ["Oceania" "Africa" "Asia" "Americas" "Europe"]
              (->> fr pj/plan :panels first :x-domain vec)))])

;; Categories are matched by the text they are drawn with, so a column
;; of numbers read as categories can be ordered by those numbers. The
;; five-cylinder cars go back where a reader expects them:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-point :cyl :hwy {:x-type :categorical})
    (pj/scale :x {:domain [4 5 6 8]}))

(kind/test-last
 [(fn [fr] (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))])

;; `:color` and `:shape` read a categorical `:domain` the same way.
;; For `:color` the palette is assigned in the order given, so the
;; domain moves the legend rows and the colors together: the first
;; category listed takes the palette's first color wherever it sits in
;; the data. It moves the marks with them: where the groups of one
;; layer are piled up or set side by side, they follow the same order,
;; so a stacked bar reads down its height in the order the legend reads
;; down its rows. `:fill` reads no categorical column at all, as
;; [The colors a scale spans](#the-colors-a-scale-spans) shows, so
;; there is no categorical `:domain` for it to read.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :continent})
    (pj/scale :color {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr] (= ["Oceania" "Europe" "Asia" "Americas" "Africa"]
              (mapv :label (:entries (:legend (pj/plan fr))))))])

;; On a stacked bar the same domain moves the segments. Listed the
;; other way round, breakfast goes from the top of each bar to its
;; foot, and the legend rows go with it:

(-> {:day ["Mon" "Mon" "Mon" "Tue" "Tue" "Tue"]
     :meal ["breakfast" "lunch" "dinner" "breakfast" "lunch" "dinner"]
     :n [10 30 20 12 25 30]}
    (pj/lay-bar :day :n {:color :meal :position :stack})
    (pj/scale :color {:domain ["dinner" "lunch" "breakfast"]}))

(kind/test-last
 [(fn [fr]
    (let [plan (pj/plan fr)
          groups (-> plan :panels first :layers first :groups)]
      (and
       ;; The legend reads down in the order the domain gives.
       (= ["dinner" "lunch" "breakfast"]
          (mapv :label (:entries (:legend plan))))
       ;; And so do the segments: the first listed is on top, the last
       ;; rests on the baseline.
       (= ["dinner" "lunch" "breakfast"] (mapv :label groups))
       (every? zero? (:y0s (last groups))))))])

;; The number of categories listed changes nothing. A column holding
;; exactly two numeric categories is ordered by the same `:domain` on
;; whichever aesthetic reads it:

(def two-grades
  {:grade [1 2 1 2]
   :hours [3 9 2 11]
   :score [72 88 64 91]})

(-> two-grades
    (pj/lay-point :hours :score {:color :grade :color-type :categorical})
    (pj/scale :color {:domain [2 1]}))

(kind/test-last
 [(fn [fr]
    ;; The same two-element numeric domain, read by the column's type
    ;; on each of the three aesthetics that take one. Read by the
    ;; domain's own shape instead, a pair of numbers looked like an
    ;; interval, and :color and :shape dropped it without a message
    ;; while :x honoured it.
    (let [on-axis (-> two-grades
                      (pj/lay-point :grade :score {:x-type :categorical})
                      (pj/scale :x {:domain [2 1]})
                      pj/plan
                      :panels first :x-domain vec)
          on-shape (-> two-grades
                       (pj/lay-point :hours :score {:shape :grade})
                       (pj/scale :shape {:domain [2 1]})
                       pj/plan
                       :shape-legend :entries
                       (->> (mapv :label)))
          on-color (-> fr pj/plan :legend :entries (->> (mapv :label)))]
      (= ["2" "1"] on-axis on-color on-shape)))])

;; A category the domain leaves out is still drawn, ordered after the
;; ones listed, and Plotje says so -- an incomplete list is usually a
;; typo or a stale set of names rather than a request.

;; ### On an appearance aesthetic
;;
;; A size, an opacity or a color has no panel to clip against, so a
;; numeric `:domain` there is not a view window. A value outside it is
;; drawn at the nearer end of the range: the smallest radius, the
;; faintest opacity, the end of the gradient.
;;
;; Population runs to well over a billion, and a handful of countries
;; at that end leave everywhere else crowded into one shade. Ending the
;; gradient at fifty million spreads the countries most of the data
;; holds:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :pop})
    (pj/scale :color {:domain [0 5.0E7]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr]
    (and (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr))))
         ;; Every row still drawn, the ones above the domain included.
         (= (tc/row-count gapminder-2007)
            (:points (pj/svg-summary fr)))))])

;; The countries above fifty million are drawn at the light end rather
;; than dropped. A domain says what the reader should compare, and a
;; dropped row would leave no trace on the panel to say so.
;;
;; A `:fill` column reads a domain the same way, and the bar beside the
;; tiles spans what the tiles were drawn against. The counts here reach
;; nineteen; ending the gradient at sixty leaves room for a second
;; heatmap to be read on the same scale:

(-> (rdatasets/ggplot2-mpg)
    (pj/lay-tile :displ :hwy)
    (pj/scale :fill {:domain [0 60]}))

(kind/test-last
 [(fn [fr]
    (and (= [0.0 60.0] ((juxt :min :max) (:legend (pj/plan fr))))
         ;; Nineteen is what the data reaches without the domain.
         (= [0.0 19.0]
            ((juxt :min :max)
             (:legend (pj/plan (-> (rdatasets/ggplot2-mpg)
                                   (pj/lay-tile :displ :hwy))))))))])

;; Fixing the domain this way is also how every panel of a facet is
;; given one size or color scale to share -- see
;; [Faceting](./plotje_book.faceting.html).

;; ## Range
;;
;; The range is the extent of drawn values the scale produces: an
;; interval of radii for a `:size` column, an interval of opacities for
;; an `:alpha` column, a gradient for a numeric `:color` or `:fill`
;; one. Those four aesthetics write it under a `:range` key -- two
;; numbers for the first pair, a gradient for the second.
;;
;; An axis has none to set, since the panel size determines it -- the
;; refusal is [above](#what-each-aesthetics-scale-takes). `:shape` spans
;; a list of symbols, written with `:values` rather than `:range`,
;; because symbols are named one by one rather than spanned between two
;; ends; a categorical `:color` spans a palette, written the same way.
;; [The section below](#the-colors-a-scale-spans) draws the color
;; cases.
;;
;; The smallest and largest value in a column are drawn at the two ends
;; of the range, so the default can be read off the marks themselves.
;; These are the distinct radii a default size scale draws, in drawing
;; units, smallest first:

(def squares
  {:step [1 2 3 4 5 6] :row [1 1 1 1 1 1] :n [1 4 9 16 25 36]})

(-> squares
    (pj/lay-point :step :row {:size :n})
    pj/svg-summary
    :sizes)

(kind/test-last
 [(fn [radii] (= [2.0 8.0] [(first radii) (last radii)]))])

;; A wider range draws every mark wider:

(-> squares
    (pj/lay-point :step :row {:size :n})
    (pj/scale :size {:range [3 20]}))

(kind/test-last
 [(fn [fr]
    (let [radii (sort (:sizes (pj/svg-summary fr)))
          default (sort (:sizes (pj/svg-summary
                                 (-> squares
                                     (pj/lay-point :step :row {:size :n})))))]
      ;; The marks themselves, since the prose is about the marks. The
      ;; two ends are the two the range names, and every mark in
      ;; between is wider than the same mark under the default range.
      (and (= [3.0 20.0] [(first radii) (last radii)])
           (= (count default) (count radii))
           (every? true? (map < default radii)))))])

;; `:alpha` takes a range of opacities in the same way. The one written
;; out below is the default, so it draws the same plot as leaving it
;; alone -- China and India nearly opaque, most of the world faint:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
    (pj/scale :alpha {:range [0.1 1.0]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr]
    ;; Compared against the same plot rather than stated in prose, so
    ;; the claim stays true if the default ever moves.
    (= (->> fr pj/plan :alpha-legend :entries (mapv :alpha))
       (->> (-> gapminder-2007
                (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
                (pj/scale :x :log))
            pj/plan :alpha-legend :entries (mapv :alpha))))])

;; ### How a size spreads across its range
;;
;; A circle with twice the radius covers four times the area. Readers
;; compare the area, so the way values spread across the radii changes
;; what the plot appears to say.
;;
;; `:by` sets that spreading. All three read a value as how far along
;; the domain it falls -- at the smallest value in the column, at the
;; largest, or some way between -- and differ in what they spread
;; evenly across the range:
;;
;; - `:sqrt` (the default) spreads the square root of that distance
;;   across the radii, which is what ggplot2's `scale_size` does. The
;;   smallest value is drawn at the low end of the range rather than at
;;   zero.
;; - `:linear` spreads the distance itself across the radii, as
;;   ggplot2's `scale_radius` does. Differences in area then exaggerate
;;   differences in value.
;; - `:area` spreads the area, so equal steps in value give equal steps
;;   in area.
;;
;; It is the distance along the domain, not the value, so a column and
;; the same column scaled up draw the same circles.
;;
;; The same six values under all three:

(-> (pj/arrange
     [(-> squares (pj/lay-point :step :row {:size :n})
          (pj/options {:title ":sqrt (default)"}))
      (-> squares (pj/lay-point :step :row {:size :n}) (pj/scale :size {:by :linear})
          (pj/options {:title ":linear"}))
      (-> squares (pj/lay-point :step :row {:size :n}) (pj/scale :size {:by :area})
          (pj/options {:title ":area"}))])
    (pj/options {:width 900 :height 340}))

;; All three draw the smallest value at the low end of the range and
;; the largest at the high end. They differ for the values in between.
;; The legend is computed from the same function as the marks, so its
;; sizes show the difference:

(defn legend-magnitudes
  [spec]
  (mapv :magnitude
        (-> squares
            (pj/lay-point :step :row {:size :n})
            (pj/scale :size spec)
            pj/plan
            :size-legend
            :entries)))

(-> (for [by [:linear :area :sqrt]
          :let [ms (legend-magnitudes {:by by})]]
      {:by by
       :smallest-labelled (first ms)
       :middle (nth ms (quot (count ms) 2))
       :largest-labelled (last ms)})
    tc/dataset)

(kind/test-last
 [(fn [_]
    ;; For every value the legend labels, `:linear` gives the smallest
    ;; radius and `:sqrt` the largest, with `:area` between them. The
    ;; labelled values stop short of the ends of the domain, which is
    ;; why no column reaches the 2.0 and 8.0 of the default range.
    (and (every? (fn [[l a s]] (< l a s))
                 (map vector
                      (legend-magnitudes {:by :linear})
                      (legend-magnitudes {:by :area})
                      (legend-magnitudes {:by :sqrt})))
         ;; What is spread is the distance along the domain, not the
         ;; value. Multiplying every value by a hundred moves the
         ;; domain and leaves every radius where it was; taking the
         ;; root of the value itself would not.
         (= (legend-magnitudes {})
            (mapv :magnitude
                  (-> (update squares :n (fn [ns] (mapv #(* 100 %) ns)))
                      (pj/lay-point :step :row {:size :n})
                      pj/plan
                      :size-legend
                      :entries)))))])

;; ### Anchoring at zero
;;
;; `:from-zero` starts both the domain and the range at zero. The area
;; is then proportional to the value: twice the value is twice the
;; area. Together with `:by :area` this matches ggplot2's
;; `scale_size_area`.
;;
;; Anchored at zero it is the distance from zero that decides the ink,
;; so a value of -5 draws the size a value of 5 draws and the domain
;; reaches to whichever value is furthest from zero. A column holding
;; both signs is drawn by magnitude, which is what proportional area
;; means when the values run both ways -- see
;; [Edge Cases](./plotje_book.edge_cases.html) for what that looks
;; like, and map the sign to `:color` where it matters.

(-> squares
    (pj/lay-point :step :row {:size :n})
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

;; `:alpha` takes the key too. It asks a different question there --
;; not how a value spreads, but whether the drawn quantity is
;; proportional to it. An opacity can answer that, since zero opacity
;; is absence the way zero area is:

(-> squares
    (pj/lay-point :step :row {:alpha :n})
    (pj/scale :alpha {:from-zero true}))

(kind/test-last
 [(fn [fr]
    (let [entries (->> fr pj/plan :alpha-legend :entries)
          by-value (into {} (map (juxt :value :alpha) entries))]
      ;; Where the legend labels both a value and its half, the
      ;; opacities are in the same 2-to-1 ratio.
      (every? (fn [[v a]]
                (if-let [half (by-value (/ v 2))]
                  (< (Math/abs (- (/ a half) 2.0)) 1e-6)
                  true))
              by-value)))])

;; `:color` cannot answer it, and does not take the key: the low end of
;; a gradient is a color rather than an absence, so there is no
;; quantity there for a value to be proportional to.
;;
;; `:from-zero` cannot be combined with a log scale either, since a log
;; scale has no value for zero. Setting both is refused.

;; ### Which quantity a mark draws
;;
;; `:by :area` works the same way on every mark, because the quantity
;; each mark draws an aesthetic as is known before any value is
;; scaled. `:point` is the one built-in layer type that says so
;; itself, under `:varies`: it draws `:size` as a radius and `:alpha`
;; as an opacity. A registered mark drawing a stroke would say it
;; draws a width. The quantity also fixes how the area grows with it:
;; as the square for a radius, linearly for a width.

(:varies (layer-type/lookup :point))

(kind/test-last
 [(fn [m] (= {:size :radius :alpha :opacity} m))])

;; Where the area grows linearly with the quantity, the three `:by`
;; methods give the same result, since there is no area correction to
;; make. That is why `:alpha` takes `:range` but not `:by`: an opacity
;; has no shape.
;;
;; The declaration also decides which aesthetics get a legend. A mark
;; that does not declare an aesthetic draws one value for the whole
;; layer, so a column mapped to that aesthetic changes nothing. Plotje
;; warns and draws no legend for it, and the warning names the marks
;; that do vary it. A mark that does not take the aesthetic as a layer
;; option at all -- `:bar` and `:area` do not take `:size` -- reports
;; it as an option it does not recognize instead. Registering a mark
;; that declares its own is covered in
;; [Extensibility](./plotje_book.extensibility.html).

;; ### The colors a scale spans
;;
;; A `:color` scale draws from a palette where the column is
;; categorical, and from a gradient where it is numeric. The column's
;; type decides which, and each has its own spec key: `:values` for the
;; palette, `:range` for the gradient. A `:range` is spanned between
;; two ends, as an interval of radii is for `:size`; a `:values` is
;; enumerated and assigned by index, as the marker symbols are for
;; `:shape`.
;;
;; `:fill` takes only the gradient half. It draws a magnitude, so it
;; needs a numeric column and reads no `:values`; a categorical column
;; belongs on `:color`, and the message says so:

(try
  (-> {:hour [1 2 3] :day [1 2 3] :shift ["early" "late" "early"]}
      (pj/lay-tile :hour :day {:fill :shift})
      pj/plan)
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [m] (re-find #":fill needs a numeric column" m))])

;; A palette is a vector of colors, a map from category to color, or
;; the name of a built-in one. It is assigned in domain order, so
;; [a `:domain`](#ordering-categories) moves the colors together with
;; the legend rows:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]}))

(kind/test-last
 [(fn [v] (= #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
             (disj (:colors (pj/svg-summary v)) "none")))])

;; Written as a map, a palette names the color for each category, so
;; the pairing does not depend on the order the categories arrive in.
;; The column still supplies the domain, so the legend is drawn from it
;; as it always is:

(-> {:district ["a" "b" "c" "d" "e" "f"]
     :share [10 20 30 40 50 60]
     :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
    (pj/lay-point :district :share {:color :party})
    (pj/scale :color {:values {"rep" "red" "dem" "blue" "ind" "green"}}))

(kind/test-last
 [(fn [v]
    (= [["rep" [1.0 0.0 0.0 1.0]]
        ["dem" [0.0 0.0 1.0 1.0]]
        ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
       (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))])

;; A gradient is named -- `:viridis` and the rest of the catalogue, or
;; the family names `:sequential` and `:diverging` -- or written as a
;; map of stops, where `:low`, `:mid` and `:high` build one of three
;; and `:mid` may be left out for two. The third form is a function,
;; taking a number from zero at the low end of the domain to one at
;; the high end and answering with `[r g b a]`, each from zero to one.
;; It is the way to a gradient the other two forms cannot write.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
    (pj/scale :color {:range {:low "#2166AC" :mid "#F7F7F7" :high "#B2182B"}}))

(kind/test-last
 [(fn [v] (= {:low "#2166AC" :mid "#F7F7F7" :high "#B2182B"}
             (-> v pj/plan :legend :color-range)))])

;; The same gradient written as a function, opaque throughout and
;; running blue to red:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
    (pj/scale :color {:range (fn [t] [t 0.0 (- 1.0 t) 1.0])}))

(kind/test-last
 [(fn [v]
    (let [stops (-> v pj/plan :legend :stops)]
      ;; The bar is built from the function that colors the marks, so
      ;; its ends are the two the function answers with.
      (and (= [0.0 0.0 1.0 1.0] (:color (first stops)))
           (= 0.0 (second (:color (last stops)))))))])

;; The range and the type are separate settings, and a plot may name
;; both. The column below spans four orders of magnitude, so the
;; gradient says which colors and the scale's type says how the values
;; are spaced along them; the legend labels the decades:

(-> {:step (range 40)
     :row (range 40)
     :n (map #(Math/pow 10 (/ % 10.0)) (range 40))}
    (pj/lay-point :step :row {:color :n})
    (pj/scale :color {:type :log :range :viridis}))

(kind/test-last
 [(fn [v]
    (let [legend (-> v pj/plan :legend)]
      (and (= :log (:scale-type legend))
           (= :viridis (:color-range legend)))))])

;; `:midpoint` names the value the middle of a gradient is drawn at.
;; A diverging gradient reads as a departure from a centre, and without
;; a midpoint that centre falls halfway along the data rather than
;; where the reader expects it:

(-> {:region ["n" "s" "e" "w" "c"]
     :year [1 2 3 4 5]
     :change [-40 -10 5 30 60]}
    (pj/lay-point :year :change {:color :change})
    (pj/scale :color {:range :diverging :midpoint 0}))

(kind/test-last
 [(fn [v]
    (let [colors (fn [p] (-> p pj/plan :panels first :layers first
                             :groups first :colors vec))
          stops (-> v pj/plan :legend :stops)]
      (and
       ;; The centre of the gradient sits at zero rather than at 10,
       ;; the middle of -40 to 60, so the marks are drawn in different
       ;; colors than they would be without the midpoint.
       (not= (colors v)
             (colors (-> {:region ["n" "s" "e" "w" "c"]
                          :year [1 2 3 4 5]
                          :change [-40 -10 5 30 60]}
                         (pj/lay-point :year :change {:color :change})
                         (pj/scale :color {:range :diverging}))))
       ;; The two ends of the bar are the two ends of the data, and the
       ;; place in the gradient each is drawn at is what the prose
       ;; below states: a sixth of the way along, and all the way.
       (< 0.16666 (:gradient-t (first stops)) 0.16667)
       (== 1.0 (:gradient-t (last stops))))))])

;; The gradient is centred on the midpoint and widened until it reaches
;; whichever end of the domain is further from it, so a departure of
;; the same size in either direction is drawn at the same saturation.
;; Sixty is further from zero than -40 is, so sixty takes the extreme
;; color and -40 is drawn a sixth of the way along the gradient rather
;; than at its other end. The bar beside the panel spans the same
;; stretch, so a color read off it is a color some mark carries.

;; Each of the three has a plot option of the same name, one scope
;; further out: `:color-values`, `:color-range` and `:color-midpoint`,
;; and `:fill-range` and `:fill-midpoint` for the other color
;; aesthetic. They set the whole plot where a spec sets one mapping or
;; one layer, and the spec wins where both are written.

(-> {:district ["a" "b" "c" "d" "e" "f"]
     :share [10 20 30 40 50 60]
     :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
    (pj/lay-point :district :share {:color :party})
    (pj/options {:color-values {"rep" "grey" "dem" "grey" "ind" "grey"}})
    (pj/scale :color {:values {"rep" "red" "dem" "blue" "ind" "green"}}))

(kind/test-last
 [(fn [v]
    (= [["rep" [1.0 0.0 0.0 1.0]]
        ["dem" [0.0 0.0 1.0 1.0]]
        ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
       (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))])

;; Which palettes and gradients are available, and how to search them,
;; is
;; [Customization](./plotje_book.customization.html#discovering-palettes-and-gradients).

;; ### The symbols `:shape` spans
;;
;; `:shape` takes only a `:categorical` type, so what it spans is a
;; list rather than an interval, and two things about that list can be
;; set: which order the
;; categories are assigned symbols in, and which symbols those are.
;; Both matter when a reader compares two plots: one category should
;; keep one marker across both.
;;
;; `pj/shape-symbols` is the list, in the order categories take them:

pj/shape-symbols

(kind/test-last [(fn [syms] (= syms (distinct syms)))])

;; A plot with more categories than that repeats a symbol, so two
;; categories cannot be told apart; Plotje warns when it happens.
;;
;; `:domain` sets the category order, as it does for `:color`, and
;; `:values` names the symbols themselves, paired with the categories
;; in that same order:

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:shape :continent})
    (pj/scale :shape {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"]
                      :values [:circle :square :triangle :diamond :cross]})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr]
    (= [["Africa" :circle] ["Americas" :square] ["Asia" :triangle]
        ["Europe" :diamond] ["Oceania" :cross]]
       (mapv (juxt :label :shape)
             (:entries (:shape-legend (pj/plan fr))))))])

;; ## Titling a scale
;;
;; A scale is explained to the reader by something drawn beside the
;; marks: an axis for `:x` and `:y`, a legend for the rest. `:label`
;; titles that thing, and every aesthetic with a scale reads it. Where
;; none is written, the column's name is drawn.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :continent})
    (pj/scale :x {:type :log :label "GDP per capita, log scale"})
    (pj/scale :color {:label "Continent"}))

(kind/test-last
 [(fn [fr] (= ["GDP per capita, log scale" "Continent"]
              (-> fr pj/plan ((juxt :x-label (comp :title :legend))))))])

;; Each has a plot option of the same name one scope further out:
;; `:x-label` and `:y-label` for the axes, and `:color-label`,
;; `:fill-label`, `:size-label`, `:alpha-label` and `:shape-label` for
;; the legends. They title the whole plot where a spec titles one
;; mapping or one layer, and the spec wins where both are written.

(-> gapminder-2007
    (pj/lay-point :gdp-percap :life-exp {:color :continent})
    (pj/options {:color-label "From the options"})
    (pj/scale :color {:label "From the spec"}))

(kind/test-last
 [(fn [fr] (= "From the spec" (-> fr pj/plan :legend :title)))])

;; ## One legend per aesthetic
;;
;; A size legend's swatches are computed from the same function as the
;; marks, so on a single panel the swatch next to a value is the size
;; a mark of that value is drawn at. Across the panels of a facet it
;; is not, for the reason given under
;; [Not supported yet](#not-supported-yet) below.
;;
;; A swatch is also drawn in the shape of the quantity it explains. One
;; column mapped to both `:size` and `:alpha` earns two legends over
;; the same values, and they look nothing alike: a radius is explained
;; by graduated circles, an opacity by squares of one size at graduated
;; opacities.

(-> squares
    (pj/lay-point :step :row {:size :n :alpha :n})
    (pj/options {:width 620}))

(kind/test-last
 [(fn [fr]
    (let [p (pj/plan fr)]
      (and (= :radius (:quantity (:size-legend p)))
           (= :circle (:swatch (:size-legend p)))
           (= :square (:swatch (layer-type/quantities :opacity))))))])

;; A mark that draws a size as the width of a stroke rather than the
;; radius of a circle is explained by strokes of that thickness. No
;; built-in mark draws one, so seeing that swatch means registering a
;; layer type that declares `{:size :width}`.
;;
;; One legend per aesthetic is also the reason an aesthetic has one
;; scale. A panel has one of each axis, so two layers naming different
;; scales for `:x` are refused; a plot has one legend per appearance
;; aesthetic, so two layers reading `:size` through different scales
;; are refused in the same way.
;;
;; A layer that names no scale is no disagreement, on any aesthetic:
;; it is drawn against whichever scale the plot settles on. Only the
;; layers that named one are compared. Two layers naming different
;; scales are refused, and the message says the two ways out -- name
;; the same scale in each mapping, or set it once with `pj/scale`.

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

;; The same two layers with the second naming nothing are drawn, and
;; both read the log scale the first one named:

(-> gapminder-2007
    (pj/pose :gdp-percap :life-exp {:size :pop})
    (pj/lay-point {:size {:column :pop :scale :log}})
    (pj/lay-point {})
    (pj/scale :x :log))

(kind/test-last
 [(fn [fr]
    (let [plan (pj/plan fr)]
      (and
       ;; Both layers on the panel, both reading the one scale.
       (= [{:type :log} {:type :log}]
          (mapv :size-scale (-> plan :panels first :layers)))
       ;; The same shape on :x answers the same way, which is the
       ;; point: an aesthetic that names nothing has no opinion,
       ;; whichever aesthetic it is.
       (= {:type :log}
          (-> gapminder-2007
              (pj/pose :gdp-percap :life-exp)
              (pj/lay-point {:x {:column :gdp-percap :scale :log}})
              (pj/lay-point {})
              pj/plan
              :panels first :x-scale)))))])

;; ## Not supported yet
;;
;; - Two layers of one pose cannot read an aesthetic through different
;;   scales, as just shown. Lifting this would need a plot to carry two
;;   legends for one aesthetic.
;; - Facet panels share their scale types. Their domains can already
;;   differ: `{:scales :free}` gives each panel its own.

;; ## See Also
;;
;; - [Customization](./plotje_book.customization.html#discovering-palettes-and-gradients)
;;   -- the named palettes and gradients available, and how to search them
;; - [Customization](./plotje_book.customization.html#tick-placement-and-text)
;;   -- wording the tick labels of a numerically indexed axis
;; - [Specifying Aesthetics](./plotje_book.specifying_aesthetics.html)
;;   -- the full mapping form a `:scale` is written in
;; - [Faceting](./plotje_book.faceting.html) -- shared and free domains
;; - [Glossary](./plotje_book.glossary.html#scale) -- the short
;;   definition
