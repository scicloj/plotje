(ns scicloj.plotje.impl.stat
  (:require [clojure.string :as str]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [tech.v3.datatype.argops :as argops]
            [tech.v3.tensor :as tensor]
            [fastmath.ml.regression :as regr]
            [fastmath.stats :as stats]
            [fastmath.interpolation.acm :as interp]
            [fastmath.kernel :as kernel]
            [fastmath.random :as frand]
            [scicloj.plotje.impl.defaults :as defaults]))

;; ---- Helpers ----

(defn- format-category-column
  "Replace a categorical column with its display labels, so the band scale
   and the tick labels key on the same strings.

   `defaults/fmt-category-label` turns the separators of a keyword into
   spaces, which is what a reader wants of :not-applicable but which two
   values differing only by separator survive alike. Here that is a data
   transform rather than a label, so such a pair is combined into one
   category and one band. Warn rather than let a bar quietly go missing --
   the case is rare, so the warning costs nothing until it matters."
  [ds col]
  (let [distinct-vals (distinct (ds col))
        collisions (->> distinct-vals
                        (group-by defaults/fmt-category-label)
                        (filter (fn [[_ vs]] (next vs))))]
    (doseq [[label vs] collisions]
      (println (str "Warning: categories " (str/join " and " (map pr-str vs))
                    " both display as " (pr-str label)
                    " and have been combined into one."
                    " Convert column " (pr-str col)
                    " to strings to keep them apart.")))
    (tc/map-columns ds col [col] defaults/fmt-category-label)))

(defn numeric-extent
  "Min/max pair from a numeric column."
  [col]
  [(dfn/reduce-min col) (dfn/reduce-max col)])

(defn- quantile-r7
  "R type 7 quantile (ggplot2/R default). Uses linear interpolation:
   h = (n-1)*p + 1, Q = x[floor(h)] + (h - floor(h)) * (x[ceil(h)] - x[floor(h)]).
   This matches R's quantile(x, p, type=7) and ggplot2's boxplot quartiles."
  [sorted-arr p]
  (let [n (alength sorted-arr)
        h (+ (* (dec n) (double p)) 1.0)
        lo-idx (max 0 (min (dec n) (dec (long (Math/floor h)))))
        hi-idx (max 0 (min (dec n) (dec (long (Math/ceil h)))))
        frac (- h (Math/floor h))]
    (+ (aget sorted-arr lo-idx)
       (* frac (- (aget sorted-arr hi-idx) (aget sorted-arr lo-idx))))))

(defn- near-constant?
  "True if the span of the column is numerically negligible. Used to detect
   degenerate inputs (single point, or near-identical x values like
   [1.0000001 1.0000002]) that would otherwise make OLS try to invert a
   singular matrix or LOESS divide by a tiny step.
   Empirically, fastmath OLS becomes singular when span/scale < ~1e-4
   in double precision, so we use that as the cutoff."
  [col]
  (let [lo (double (dfn/reduce-min col))
        hi (double (dfn/reduce-max col))
        span (- hi lo)
        scale (max (Math/abs lo) (Math/abs hi) 1.0)]
    (<= span (* 1e-4 scale))))

(defn- drop-missing-reported
  "Drop rows with a missing value in any of `cols`, and say how many went
   and which column they were missing from.

   The numeric aesthetics are filtered a stage earlier, by
   `plan/filter-infinities`, which reports them in its own words; by the
   time a stat runs, its numeric columns hold no nils and this removes
   nothing more. What it does remove is a row whose *category* is
   missing -- a nil among the values of a string or keyword column,
   which until now took its mark, its band and its legend entry with it
   and said nothing.

   Only the columns that actually held a missing value are named, so a
   message points at the column to look at rather than at every column
   the stat happens to read."
  [ds cols]
  ;; A faithful wrapper: `cols` reaches `tc/drop-missing` exactly as it
  ;; was given, so which rows go is unchanged and only the message is new.
  (let [before (tc/row-count ds)
        out (tc/drop-missing ds cols)
        removed (- before (tc/row-count out))]
    (when (pos? removed)
      (let [culprits (filterv #(pos? (- before (tc/row-count (tc/drop-missing ds [%]))))
                              cols)
            ;; A column holding strings or keywords is missing a
            ;; *category*; one holding nothing but nils -- an all-nil
            ;; :size, say -- is missing a value, and calling that a
            ;; category would name something the data never had.
            categories? (fn [col] (some #(or (string? %) (keyword? %)) (ds col)))]
        (println (str "Warning: Removed " removed " rows with a missing "
                      (if (every? categories? culprits) "category" "value")
                      " in " (str/join ", " (map pr-str culprits)) "."))))
    out))

(defn group-by-columns
  "Split dataset by grouping columns, apply f to each group.
   Iterates groups in the order they first appear in the dataset, so
   results are deterministic regardless of `tc/group-by`'s underlying
   map type (which is a PersistentHashMap, scrambled by murmur3 hash
   for >8 groups). This also preserves user-visible stacking and
   draw order."
  [ds group-cols f]
  (if (seq group-cols)
    (let [grouped (tc/group-by ds group-cols {:result-type :as-map})
          ;; Determine canonical order: zip the group columns and walk
          ;; the dataset rows in order, taking the first occurrence of
          ;; each composite key.
          canonical-keys
          (let [n (tc/row-count ds)
                cols (mapv #(get ds %) group-cols)]
            (loop [i 0, seen #{}, out (transient [])]
              (if (>= i n)
                (persistent! out)
                (let [k (zipmap group-cols (mapv #(nth % i) cols))]
                  (if (contains? seen k)
                    (recur (inc i) seen out)
                    (recur (inc i) (conj seen k) (conj! out k)))))))]
      (for [gk canonical-keys
            :let [gds (get grouped gk)]
            :when gds]
        (f gds (if (= 1 (count group-cols))
                 (get gk (first group-cols))
                 (mapv gk group-cols)))))
    [(f ds nil)]))

;; ---- Prepare Points ----

(def ^:private temporal-datatypes
  "The datatypes to offer for a column of text dates, finest spelling
   last. `tc/convert-types` reads each shape of text as one of these and
   refuses the others, so the order is the order they are tried in."
  [:local-date :local-date-time :instant])

(defn- temporal-datatype-for
  "The datatype `tc/convert-types` reads `values` as, or nil where none
   of them does.

   The answer is measured by running the conversion the error message
   is about to recommend, on one value, rather than by matching the text
   against a pattern. A GitHub timestamp such as
   `2015-03-21T10:00:00Z` is an `:instant` and refuses `:local-date`, so
   a message naming `:local-date` for every column told the reader to
   run a call that throws on the data that produced the message."
  [values]
  (when-let [sample (first (remove nil? values))]
    (some (fn [dt]
            (let [converted (try (-> (tc/dataset {:c [sample]})
                                     (tc/convert-types :c dt)
                                     :c
                                     meta
                                     :datatype)
                                 (catch Throwable _ nil))]
              (when (= dt converted) dt)))
          temporal-datatypes)))

(defn- validate-numeric-column
  "Report a column the stat cannot read, and say what to do about it.

   The column name is printed with `pr-str` rather than `name`: a
   dataset built without column names gets integer ones, and `name`
   threw `Long cannot be cast to clojure.lang.Named` on those -- a cast
   exception in place of the clear error this function exists to give.

   Dates written as text are the common way to arrive here, so the
   message names the conversion, and names the datatype this column's
   own text converts to. A `:bin` stat also names the layer that counts
   categories instead, which is what a categorical column usually
   wants."
  [draft-layer col-key stat-name]
  (let [type-key (keyword (str (name col-key) "-type"))
        col-type (get draft-layer type-key)
        col (get draft-layer col-key)]
    (when (= col-type :categorical)
      (let [dt (temporal-datatype-for (get (:data draft-layer) col))]
        (throw (ex-info (str "Stat :" (name stat-name) " requires a numeric column for :"
                             (name col-key) ", but " (pr-str col) " is categorical. "
                             (if dt
                               (str "Dates written as text stay categorical until they are "
                                    "parsed: (tc/convert-types ds " (pr-str col) " " dt
                                    ") makes the column temporal, which this stat can read.")
                               (str "A stat that bins or smooths reads numbers; a column of "
                                    "text names groups instead. Dates written as text stay "
                                    "categorical until they are parsed with "
                                    "tc/convert-types."))
                             (when (= stat-name :bin)
                               " To count the rows in each category instead of binning them, use pj/lay-bar."))
                        (cond-> {:stat stat-name :column col :column-type col-type}
                          dt (assoc :convert-to dt))))))))

(defn- min-adjacent-gap
  "Smallest gap between adjacent distinct values, or nil when fewer than two
   distinct values exist. Used to size bars on a numeric/temporal axis where
   there is no categorical band to provide a width."
  [xs]
  (let [sorted (vec (sort (distinct (remove nil? xs))))]
    (when (> (count sorted) 1)
      (reduce min (map (fn [a b] (- (double b) (double a)))
                       sorted (rest sorted))))))

(defn prepare-points
  "Clean data, compute domains, group by columns.
   Drops rows with missing values in x/y AND in any referenced numeric
   aesthetic column (color/size/alpha/y-min/y-max/fill) so downstream code
   never sees nil/NaN values where it tries to coerce to double.

   For a `:rect` mark with both axes numeric (a bar at a numeric or temporal
   position), returns a `:bar-width` in data units and widens the x-domain by
   half a bar on each side so edge bars are not clipped."
  [draft-layer]
  (let [{:keys [data x y color color-type size alpha shape text-col tooltip-col
                x-type y-type group mark y-min y-max x-end fill bar-width]} draft-layer
        x-only? (or (nil? y) (= x y))
        data-idx (tc/add-column data :__row-idx (range (tc/row-count data)))
        ds-cols (set (tc/column-names data-idx))
        col-ref? (fn [v] (and v (or (keyword? v) (string? v)) (contains? ds-cols v)))
        ;; Referenced numeric aesthetic columns that exist in the dataset
        aesthetic-cols (cond-> []
                         (col-ref? color) (conj color)
                         (col-ref? size)  (conj size)
                         (col-ref? alpha) (conj alpha)
                         (col-ref? y-min)  (conj y-min)
                         (col-ref? y-max)  (conj y-max)
                         (col-ref? x-end)  (conj x-end)
                         (col-ref? fill)  (conj fill))
        drop-cols (vec (distinct (concat (if x-only? [x] [x y]) aesthetic-cols)))
        clean (cond-> (drop-missing-reported data-idx drop-cols)
                (= x-type :categorical) (format-category-column x)
                ;; Format the categorical axis whichever side it is on, so a
                ;; horizontal value bar's category labels (on y) match its band
                ;; scale -- keyword/number categories become display strings.
                (and (not x-only?) (= y-type :categorical))
                (format-category-column y))]
    (if (zero? (tc/row-count clean))
      {:points [] :x-domain [0 1] :y-domain [0 1]}
      (let [xs-col (clean x)
            ys-col (if x-only? nil (clean y))
            cat-x? (= x-type :categorical)
            cat-y? (= y-type :categorical)
            ;; A :rect bar with neither axis categorical sits at a numeric (or
            ;; temporal) x position and needs a width in data units: the
            ;; explicit :bar-width, else 0.9 of the smallest gap between
            ;; adjacent x positions, else 1.0 for a lone bar.
            numeric-bar? (and (= mark :rect) (not x-only?) (not cat-x?) (not cat-y?))
            w (when numeric-bar?
                (double (or bar-width
                            (some-> (min-adjacent-gap xs-col) (* 0.9))
                            1.0)))
            x-dom (if cat-x?
                    (distinct xs-col)
                    (let [[lo hi] (numeric-extent xs-col)]
                      (cond
                        (col-ref? x-end)
                        (let [[lo2 hi2] (numeric-extent (clean x-end))]
                          [(min lo lo2) (max hi hi2)])
                        ;; Horizontal value bars (categorical y, numeric x):
                        ;; the bar runs along x, so anchor the x-domain at 0,
                        ;; mirroring the vertical bar's y-domain anchoring below.
                        (and (= mark :rect) cat-y?)
                        [(min 0 lo) (max 0 hi)]
                        ;; Numeric-position bars: widen by half a bar each side
                        ;; so the first and last bars are not clipped.
                        numeric-bar?
                        [(- lo (/ w 2.0)) (+ hi (/ w 2.0))]
                        :else [lo hi])))
            y-dom (cond
                    x-only? nil
                    cat-y? (distinct ys-col)
                    :else (let [[lo hi] (numeric-extent ys-col)]
                            (if (= mark :rect) [(min 0 lo) (max 0 hi)]
                                ;; Extend domain to include y-min/y-max if present
                                (if (and y-min y-max)
                                  [(min lo (dfn/reduce-min (clean y-min)))
                                   (max hi (dfn/reduce-max (clean y-max)))]
                                  [lo hi]))))
            numeric-color? (and color (= color-type :numerical))
            ;; Extract color value from group key when color is part of group
            color-idx (when color (.indexOf ^java.util.List group color))
            extract-color (fn [group-val]
                            (cond
                              (nil? group-val) nil
                              ;; Single group col — group-val is the value itself
                              (= 1 (count group)) group-val
                              ;; Multiple group cols — extract color column value
                              (and color-idx (>= color-idx 0)) (nth group-val color-idx)
                              :else nil))
            zero-ys (fn [ds] (dtype/const-reader 0.0 (tc/row-count ds)))
            point-group (fn [ds group-val]
                          (cond-> {:xs (ds x) :ys (if x-only? (zero-ys ds) (ds y))
                                   :row-indices (ds :__row-idx)}
                            (some? group-val) (assoc :color (extract-color group-val))
                            numeric-color? (assoc :color-values (ds color))
                            size (assoc :sizes (ds size))
                            alpha (assoc :alphas (ds alpha))
                            shape (assoc :shapes (ds shape))
                            text-col (assoc :labels (ds text-col))
                            tooltip-col (assoc :tooltips (ds tooltip-col))
                            y-min (assoc :ymins (ds y-min))
                            y-max (assoc :ymaxs (ds y-max))
                            (col-ref? x-end) (assoc :x-ends (ds x-end))))
            groups (group-by-columns clean (or group []) point-group)]
        (cond-> {:points groups :x-domain x-dom :y-domain y-dom}
          numeric-bar? (assoc :bar-width w))))))

;; ---- compute-stat multimethod ----

(defmulti compute-stat
  "Compute a statistical transform for a draft-layer."
  (fn [draft-layer] (or (:stat draft-layer) :identity)))

(defmethod compute-stat :default [draft-layer]
  (let [stat-key (or (:stat draft-layer) :identity)
        registered (sort (filter keyword?
                                 (remove #(or (vector? %) (= :default %))
                                         (keys (methods compute-stat)))))]
    (throw (ex-info (str "Unknown stat: " (pr-str stat-key)
                         ". Supported stats: " (vec registered))
                    {:stat stat-key :supported (vec registered)}))))

;; ---- Doc methods (dispatching on [stat-key :doc]) ----

(defmethod compute-stat [:identity :doc] [_] "Pass-through — no transform")
(defmethod compute-stat [:bin :doc] [_] "Bin numerical values into ranges")
(defmethod compute-stat [:count :doc] [_] "Count occurrences per category")
(defmethod compute-stat [:linear-model :doc] [_] "Linear model — OLS regression line + optional confidence band")
(defmethod compute-stat [:loess :doc] [_] "LOESS (local regression) smoothing")
(defmethod compute-stat [:density :doc] [_] "Density — 1D kernel density estimation (KDE)")
(defmethod compute-stat [:boxplot :doc] [_] "Five-number summary + outliers")
(defmethod compute-stat [:violin :doc] [_] "KDE per category (density profile)")
(defmethod compute-stat [:summary :doc] [_] "Mean ± standard error per category")
(defmethod compute-stat [:bin2d :doc] [_] "2D grid binning (heatmap counts)")
(defmethod compute-stat [:density-2d :doc] [_] "Density 2D — 2D Gaussian kernel density estimation (KDE)")

(defmethod compute-stat :identity [{:keys [mark x y x-type] :as draft-layer}]
  (when (and (#{:lollipop :errorbar :pointrange} mark)
             (or (nil? y) (= x y))
             (= x-type :categorical))
    (throw (ex-info (str "Mark :" (name mark) " requires both :x and :y columns with numeric :y.")
                    {:mark mark :x x :y y})))
  (prepare-points draft-layer))

;; ---- Binning ----

(defn- exact-width-bins
  "Build exact-`bw`-wide bin maps for a numeric column, in the ggplot2
   style (bins anchored at `lo`, extending in both directions to cover
   the data). Returns a vector of maps `{:min :max :count}`. Each bin
   covers the half-open interval `[min, max)` except for the last bin,
   which is closed on the right so the max value is included.
   `lo-anchor` is the anchor of the first bin's left edge — typically
   the column's min value, so bin 0 starts exactly at the minimum."
  [col lo-anchor bw]
  (let [lo (double lo-anchor)
        bw (double bw)
        [data-lo data-hi] (numeric-extent col)
        dlo (double data-lo)
        dhi (double data-hi)
        ;; Pick the lowest bin index that covers data-lo.
        i0 (long (Math/floor (/ (- dlo lo) bw)))
        ;; And the highest bin index that covers data-hi. When data-hi
        ;; sits exactly on a boundary, fold it into the previous bin
        ;; (right-closed on the last bin).
        raw-i1 (long (Math/floor (/ (- dhi lo) bw)))
        i1 (if (and (> raw-i1 i0)
                    (== (+ lo (* bw raw-i1)) dhi))
             (dec raw-i1)
             raw-i1)
        n-bins (inc (- i1 i0))
        edges (mapv (fn [k] (+ lo (* bw (double (+ i0 k))))) (range (inc n-bins)))
        counts (long-array n-bins)
        n (dtype/ecount col)]
    (dotimes [k n]
      (let [v (double (col k))
            idx (long (Math/floor (/ (- v lo) bw)))
            idx (min (dec n-bins) (max 0 (- idx i0)))]
        (aset counts idx (inc (aget counts idx)))))
    (mapv (fn [k]
            {:min (nth edges k)
             :max (nth edges (inc k))
             :count (aget counts k)})
          (range n-bins))))

(defmethod compute-stat :bin [{:keys [data x x-type group cfg normalize] :as draft-layer}]
  (validate-numeric-column draft-layer :x :bin)
  (when-let [b (:bins draft-layer)]
    (when-not (and (number? b) (pos? b))
      (throw (ex-info (str ":bins must be a positive number, got: " (pr-str b))
                      {:bins b}))))
  (when-let [bw (:binwidth draft-layer)]
    (when-not (and (number? bw) (pos? bw))
      (throw (ex-info (str ":binwidth must be a positive number, got: " (pr-str bw))
                      {:binwidth bw}))))
  (let [clean (cond-> (drop-missing-reported data [x])
                (= x-type :categorical) (format-category-column x))
        xs-col (clean x)
        user-binwidth (:binwidth draft-layer)
        ;; Compute a shared anchor when :binwidth is supplied, so every
        ;; group uses the same bin boundaries (important for stacked and
        ;; colored histograms — otherwise groups with different minima
        ;; produce misaligned bars).
        shared-lo (when user-binwidth
                    (when (pos? (tc/row-count clean))
                      (double (dfn/reduce-min xs-col))))]
    (if (zero? (tc/row-count clean))
      {:bins [] :max-count 0 :x-domain [0 1] :y-domain [0 1]}
      (let [;; Determine bin method: :bins (exact count) > :binwidth
            ;; (exact width) > cfg heuristic.
            bin-arg (or (:bins draft-layer)
                        (:bin-method (or cfg defaults/defaults)))
            all-bin-data (group-by-columns
                          clean (or group [])
                          (fn [ds gv]
                            (let [bin-maps (if user-binwidth
                                             ;; Exact-width path: respects the
                                             ;; user's :binwidth literally.
                                             (exact-width-bins (ds x) shared-lo user-binwidth)
                                             ;; Equal-count path (default or
                                             ;; explicit :bins): lets fastmath
                                             ;; pick bin edges.
                                             (:bins-maps (stats/histogram (ds x) bin-arg)))]
                              (cond-> {:bin-maps bin-maps}
                                (some? gv) (assoc :color gv)))))
            ;; When normalize=:density, convert counts to density (area integrates to 1)
            all-bin-data (if (= normalize :density)
                           (mapv (fn [bd]
                                   (let [n (reduce + 0 (map :count (:bin-maps bd)))
                                         bin-maps' (mapv (fn [b]
                                                           (let [bw (- (double (:max b)) (double (:min b)))
                                                                 density (if (and (pos? n) (pos? bw))
                                                                           (/ (double (:count b)) (* n bw))
                                                                           0.0)]
                                                             (assoc b :count density)))
                                                         (:bin-maps bd))]
                                     (assoc bd :bin-maps bin-maps')))
                                 all-bin-data)
                           all-bin-data)
            ;; Floor at 1 for raw counts; floor at 0 for density (values are typically < 1)
            floor (if (= normalize :density) 0 1)
            all-counts (for [{:keys [bin-maps]} all-bin-data
                             b bin-maps]
                         (:count b))
            max-count (reduce max floor all-counts)
            ;; Truthful y-domain: range of values that produce visible bars.
            ;; Zero-count bins emit no bar, so they don't contribute. Lower
            ;; bound = min positive count; bar's visual baseline-at-zero is
            ;; a mark concern, applied at the global-domain stage.
            positive-counts (filter pos? all-counts)
            min-y (if (seq positive-counts) (reduce min positive-counts) floor)]
        {:bins all-bin-data
         :max-count max-count
         :x-domain (numeric-extent xs-col)
         :y-domain [min-y max-count]}))))

;; ---- Counting ----

(defn- counts->points
  "The same counts read as points -- one per bar, at (category, count), with
   the count as its label. This is what makes a computed count available to a
   text mark: `(pj/lay-label {:stat :count})` labels each bar with its own
   height without the caller pre-aggregating (issue #14). Derived from the
   bars rather than counted again, so a label cannot disagree with the bar it
   names."
  [bars]
  (mapv (fn [{:keys [color counts]}]
          {:color color
           :xs (mapv :category counts)
           :ys (mapv :count counts)
           :labels (mapv :count counts)})
        bars))

(defmethod compute-stat :count [draft-layer]
  (when-not (= (:x-type draft-layer) :categorical)
    (throw (ex-info (str "lay-bar (counting) requires a categorical column for :x, "
                         "but " (:x draft-layer) " is " (name (or (:x-type draft-layer) :unknown))
                         ". Use lay-histogram to bin numeric data, convert " (:x draft-layer)
                         " to a string column, or pass {:x-type :categorical} to treat a numeric"
                         " column as categorical. For horizontal bars, add (pj/coord :flip).")
                    {:stat :count :x (:x draft-layer) :x-type (:x-type draft-layer)})))
  (let [{:keys [data x x-type group]} draft-layer
        group-cols (or group [])
        clean (cond-> (drop-missing-reported data [x])
                (= x-type :categorical) (format-category-column x))
        categories (distinct (clean x))]
    (if (empty? categories)
      {:categories [] :bars [] :max-count 0 :x-domain ["?"] :y-domain [0 1]}
      ;; Use tc/group-by for efficient counting
      (let [color-col (first group-cols)
            has-color? (seq group-cols)
            clean-c (if has-color? (drop-missing-reported clean group-cols) clean)
            color-cats (when has-color? (sort (distinct (clean-c color-col))))
            ;; Single tc/group-by handles both colored and uncolored cases
            grouped (tc/group-by clean-c
                                 (if has-color? [x color-col] [x])
                                 {:result-type :as-map})]
        (if has-color?
          (let [count-fn (fn [cat cc]
                           ;; When color column == x column, cat must equal cc
                           ;; (otherwise the combination is impossible)
                           (if (and (= x color-col) (not= cat cc))
                             0
                             (if-let [ds (get grouped (zipmap [x color-col] [cat cc]))]
                               (tc/row-count ds) 0)))
                all-counts (for [cat categories, cc color-cats] (count-fn cat cc))
                max-count (reduce max 1 all-counts)
                positive-counts (filter pos? all-counts)
                min-y (if (seq positive-counts) (reduce min positive-counts) 1)
                bars (vec (for [cc color-cats]
                            {:color cc
                             :counts (mapv (fn [cat] {:category cat :count (count-fn cat cc)})
                                           categories)}))]
            {:categories categories
             :bars bars
             :points (counts->points bars)
             :max-count max-count
             :x-domain categories
             :y-domain [min-y max-count]})
          (let [counts-by-cat (mapv (fn [cat]
                                      {:category cat
                                       :count (if-let [ds (get grouped {x cat})]
                                                (tc/row-count ds) 0)})
                                    categories)
                all-counts (map :count counts-by-cat)
                max-count (reduce max 1 all-counts)
                positive-counts (filter pos? all-counts)
                min-y (if (seq positive-counts) (reduce min positive-counts) 1)
                bars [{:counts counts-by-cat}]]
            {:categories categories
             :bars bars
             :points (counts->points bars)
             :max-count max-count
             :x-domain categories
             :y-domain [min-y max-count]}))))))

;; ---- Linear Regression ----

(defn fit-lm
  "Fit a linear model, return {:x1 :y1 :x2 :y2}."
  [xs-col ys-col]
  (let [model (regr/lm ys-col xs-col)
        x-min (dfn/reduce-min xs-col)
        x-max (dfn/reduce-max xs-col)]
    {:x1 x-min :y1 (regr/predict model [x-min])
     :x2 x-max :y2 (regr/predict model [x-max])}))

(defn fit-lm-with-se
  "Fit a linear model and compute confidence band on a grid of x-values.
   Returns {:xs [...] :ys [...] :ymins [...] :ymaxs [...] :x1 :y1 :x2 :y2}."
  [xs-col ys-col n-grid level]
  (let [model (regr/lm ys-col xs-col)
        sigma (double (:sigma model))
        xtxinv (:xtxinv model)
        df-resid (long (get-in model [:df :residual]))
        ;; t critical value for confidence level
        t-dist (frand/distribution :t {:degrees-of-freedom df-resid})
        t-val (frand/icdf t-dist (+ 0.5 (/ (double level) 2.0)))
        ;; Matrix entries for se computation: [1, x] * xtxinv * [1, x]^T
        a00 (.getEntry xtxinv 0 0)
        a01 (.getEntry xtxinv 0 1)
        a11 (.getEntry xtxinv 1 1)
        x-min (dfn/reduce-min xs-col)
        x-max (dfn/reduce-max xs-col)
        step (if (<= n-grid 1) 0.0 (/ (- x-max x-min) (dec (double n-grid))))
        grid-xs (dfn/+ x-min (dfn/* step (range n-grid)))
        grid-ys (dtype/emap #(regr/predict model [%]) :float64 grid-xs)
        h (dfn/+ a00 (dfn/* 2.0 a01 grid-xs) (dfn/* a11 grid-xs grid-xs))
        se (dfn/* sigma (dfn/sqrt h))
        grid-ymins (dfn/- grid-ys (dfn/* t-val se))
        grid-ymaxs (dfn/+ grid-ys (dfn/* t-val se))]
    {:xs grid-xs :ys grid-ys
     :ymins grid-ymins :ymaxs grid-ymaxs
     :x1 x-min :y1 (regr/predict model [x-min])
     :x2 x-max :y2 (regr/predict model [x-max])}))

(defmethod compute-stat :linear-model [{:keys [data x y group cfg] :as draft-layer}]
  (validate-numeric-column draft-layer :x :linear-model)
  (validate-numeric-column draft-layer :y :linear-model)
  (let [se (:confidence-band draft-layer)
        level (or (:level draft-layer) 0.95)
        n-grid (or (:se-n-grid (or cfg defaults/defaults)) 80)
        clean (drop-missing-reported data [x y])
        n (tc/row-count clean)]
    (if (or (< n 3)
            (near-constant? (clean x)))
      (cond-> {:lines []
               :x-domain (if (pos? n) (numeric-extent (clean x)) [0 1])
               :y-domain (if (pos? n) (numeric-extent (clean y)) [0 1])}
        se (assoc :ribbons []))
      (if se
        ;; With confidence band
        (let [results (group-by-columns
                       clean (or group [])
                       (fn [ds gv]
                         (when (and (>= (tc/row-count ds) 3)
                                    (not (near-constant? (ds x))))
                           (cond-> (fit-lm-with-se (ds x) (ds y) n-grid level)
                             (some? gv) (assoc :color gv)))))
              results (remove nil? results)
              lines (mapv (fn [{:keys [x1 y1 x2 y2 color]}]
                            (cond-> {:x1 x1 :y1 y1 :x2 x2 :y2 y2}
                              (some? color) (assoc :color color)))
                          results)
              ribbons (mapv (fn [{:keys [xs ys ymins ymaxs color]}]
                              (cond-> {:xs xs :ys ys :ymins ymins :ymaxs ymaxs}
                                (some? color) (assoc :color color)))
                            results)
              ymin-bufs (seq (map :ymins results))
              ymax-bufs (seq (map :ymaxs results))
              y-ext (numeric-extent (clean y))
              y-lo (min (first y-ext) (if ymin-bufs (dfn/reduce-min (dtype/concat-buffers ymin-bufs)) (first y-ext)))
              y-hi (max (second y-ext) (if ymax-bufs (dfn/reduce-max (dtype/concat-buffers ymax-bufs)) (second y-ext)))]
          {:lines lines
           :ribbons ribbons
           :x-domain (numeric-extent (clean x))
           :y-domain [y-lo y-hi]})
        ;; Without confidence band (original behavior)
        (let [lines (group-by-columns
                     clean (or group [])
                     (fn [ds gv]
                       (when (and (>= (tc/row-count ds) 3)
                                  (not (near-constant? (ds x))))
                         (cond-> (fit-lm (ds x) (ds y))
                           (some? gv) (assoc :color gv)))))]
          {:lines (vec (remove nil? lines))
           :x-domain (numeric-extent (clean x))
           :y-domain (numeric-extent (clean y))})))))

;; ---- LOESS Smoothing ----

(defn- dedup-xy
  "Average y-values for duplicate x-values in sorted arrays.
   Returns [unique-xs averaged-ys] as double arrays.
   Inputs must be sorted double arrays of the same length."
  [^doubles xs ^doubles ys]
  (let [n (alength xs)]
    (if (zero? n)
      [(double-array 0) (double-array 0)]
      (let [;; worst case: all unique → n entries
            out-x (double-array n)
            out-y (double-array n)]
        (loop [i 1
               run-start 0
               y-sum (aget xs 0) ;; will be overwritten below
               out-idx 0]
          (let [cur-x (aget xs run-start)
                y-sum (if (== i 1) (aget ys 0) y-sum)]
            (if (< i n)
              (let [xi (aget xs i)]
                (if (== xi cur-x)
                  (recur (inc i) run-start (+ y-sum (aget ys i)) out-idx)
                  (do
                    (aset out-x out-idx cur-x)
                    (aset out-y out-idx (/ y-sum (double (- i run-start))))
                    (recur (inc i) i (aget ys i) (inc out-idx)))))
              ;; flush last run
              (let [cnt (- i run-start)]
                (aset out-x out-idx cur-x)
                (aset out-y out-idx (/ y-sum (double cnt)))
                (let [total (inc out-idx)]
                  [(java.util.Arrays/copyOf out-x total)
                   (java.util.Arrays/copyOf out-y total)])))))))))

(defn fit-loess
  "Fit a LOESS curve, return {:xs ... :ys ...} evaluated on a grid."
  [xs-col ys-col n-grid bandwidth]
  (let [order (dtype/->int-array (argops/argsort xs-col))
        sorted-xs (dtype/->double-array (dtype/indexed-buffer order xs-col))
        sorted-ys (dtype/->double-array (dtype/indexed-buffer order ys-col))
        [uxs uys] (dedup-xy sorted-xs sorted-ys)
        ;; iters 4 matches R's loess default; Apache Commons' default of 2
        ;; is less robust against outliers.
        loess-fn (interp/loess uxs uys {:bandwidth bandwidth :iters 4})
        x-min (dfn/reduce-min xs-col)
        x-max (dfn/reduce-max xs-col)
        step (if (<= n-grid 1) 0.0 (/ (- x-max x-min) (dec (double n-grid))))
        grid-xs (dfn/+ x-min (dfn/* step (range n-grid)))
        grid-ys (dtype/emap loess-fn :float64 grid-xs)]
    {:xs grid-xs :ys grid-ys}))

(defn fit-loess-with-se
  "Fit LOESS with bootstrap confidence band. Returns
   {:xs :ys :ymins :ymaxs} evaluated on a grid."
  [xs-col ys-col n-grid bandwidth level n-boot]
  (let [;; Original fit (with dedup for duplicate x-values)
        order (dtype/->int-array (argops/argsort xs-col))
        sorted-xs (dtype/->double-array (dtype/indexed-buffer order xs-col))
        sorted-ys (dtype/->double-array (dtype/indexed-buffer order ys-col))
        [uxs uys] (dedup-xy sorted-xs sorted-ys)
        ;; iters 4 matches R's loess default; Apache Commons' default of 2
        ;; is less robust against outliers.
        loess-fn (interp/loess uxs uys {:bandwidth bandwidth :iters 4})
        x-min (dfn/reduce-min xs-col)
        x-max (dfn/reduce-max xs-col)
        step (if (<= n-grid 1) 0.0 (/ (- x-max x-min) (dec (double n-grid))))
        grid-xs (dfn/+ x-min (dfn/* step (range n-grid)))
        grid-ys (dtype/emap loess-fn :float64 grid-xs)

        ;; Bootstrap: resample via tensor, fit LOESS, evaluate on grid
        t (tensor/->tensor [xs-col ys-col] :datatype :float64)
        n (long (second (dtype/shape t)))
        rng-inst (frand/rng :jdk 42)
        boot-grid-ys (for [_ (range n-boot)]
                       (let [indices (int-array (repeatedly n #(frand/irandom rng-inst (int n))))
                             sampled (tensor/select t :all indices)
                             order (argops/argsort (tensor/select sampled 0 :all))
                             sorted (tensor/select sampled :all order)
                             sorted-xs (dtype/->double-array (tensor/select sorted 0 :all))
                             sorted-ys (dtype/->double-array (tensor/select sorted 1 :all))
                             [bsx bsy] (dedup-xy sorted-xs sorted-ys)]
                         (when (>= (alength bsx) 4)
                           (try
                             (let [bfn (interp/loess bsx bsy {:bandwidth bandwidth :iters 4})]
                               (dtype/emap bfn :float64 grid-xs))
                             (catch Exception _ nil)))))
        valid-boots (vec (remove nil? boot-grid-ys))
        n-valid (count valid-boots)]
    (if (zero? n-valid)
      ;; No valid bootstrap samples — fall back to the point estimate curve
      ;; with zero-width ribbons so downstream code has shape stability.
      {:xs grid-xs :ys grid-ys
       :ymins (vec grid-ys) :ymaxs (vec grid-ys)}
      (let [alpha (- 1.0 (double level))
            lo-idx (max 0 (long (* (/ alpha 2.0) n-valid)))
            hi-idx (min (dec n-valid)
                        (long (* (- 1.0 (/ alpha 2.0)) n-valid)))
            ymins (mapv (fn [i]
                          (let [vals (sort (map #(% i) valid-boots))]
                            (nth vals lo-idx)))
                        (range n-grid))
            ymaxs (mapv (fn [i]
                          (let [vals (sort (map #(% i) valid-boots))]
                            (nth vals hi-idx)))
                        (range n-grid))]
        {:xs grid-xs :ys grid-ys :ymins ymins :ymaxs ymaxs}))))

(defmethod compute-stat :loess [{:keys [data x y group cfg] :as draft-layer}]
  (validate-numeric-column draft-layer :x :loess)
  (validate-numeric-column draft-layer :y :loess)
  (let [se (:confidence-band draft-layer)
        level (or (:level draft-layer) 0.95)
        clean (drop-missing-reported data [x y])
        n (tc/row-count clean)
        ;; Adaptive bootstrap count: LOESS fits are O(n^1.7), so running
        ;; 200 bootstraps on 100k rows is prohibitive. Scale the sample
        ;; count down for large inputs while preserving good CI coverage
        ;; on small/medium datasets. Clamp to at least 20 so the
        ;; quantile estimates remain usable. Users can still override
        ;; via an explicit `:bootstrap-resamples N` on the draft-layer.
        n-boot (or (:bootstrap-resamples draft-layer)
                   (cond
                     (<= n 500)    200
                     (<= n 2000)   100
                     (<= n 10000)  50
                     :else         30))
        n-grid (or (:loess-n-grid (or cfg defaults/defaults)) 80)
        bandwidth (or (:loess-bandwidth (or cfg defaults/defaults)) 0.75)]
    (if (or (< n 4)
            (near-constant? (clean x)))
      (cond-> {:points []
               :x-domain (if (pos? n) (numeric-extent (clean x)) [0 1])
               :y-domain (if (pos? n) (numeric-extent (clean y)) [0 1])}
        se (assoc :ribbons []))
      (if se
        ;; With confidence band (bootstrap)
        (let [results (group-by-columns
                       clean (or group [])
                       (fn [ds gv]
                         (when (and (>= (tc/row-count ds) 4)
                                    (not (near-constant? (ds x))))
                           (cond-> (fit-loess-with-se (ds x) (ds y)
                                                      n-grid bandwidth level n-boot)
                             (some? gv) (assoc :color gv)))))
              results (remove nil? results)
              curves (mapv (fn [{:keys [xs ys color]}]
                             (cond-> {:xs xs :ys ys}
                               (some? color) (assoc :color color)))
                           results)
              ribbons (mapv (fn [{:keys [xs ys ymins ymaxs color]}]
                              (cond-> {:xs xs :ys ys :ymins ymins :ymaxs ymaxs}
                                (some? color) (assoc :color color)))
                            results)
              ymin-bufs (seq (map :ymins results))
              ymax-bufs (seq (map :ymaxs results))
              y-ext (numeric-extent (clean y))
              y-lo (min (first y-ext) (if ymin-bufs (dfn/reduce-min (dtype/concat-buffers ymin-bufs)) (first y-ext)))
              y-hi (max (second y-ext) (if ymax-bufs (dfn/reduce-max (dtype/concat-buffers ymax-bufs)) (second y-ext)))]
          {:points curves
           :ribbons ribbons
           :x-domain (numeric-extent (clean x))
           :y-domain [y-lo y-hi]})
        ;; Without confidence band (original behavior)
        (let [curves (group-by-columns
                      clean (or group [])
                      (fn [ds gv]
                        (when (and (>= (tc/row-count ds) 4)
                                   (not (near-constant? (ds x))))
                          (cond-> (fit-loess (ds x) (ds y) n-grid bandwidth)
                            (some? gv) (assoc :color gv)))))
              curves (remove nil? curves)
              ys-bufs (seq (map :ys curves))
              y-min (if ys-bufs (dfn/reduce-min (dtype/concat-buffers ys-bufs)) (dfn/reduce-min (clean y)))
              y-max (if ys-bufs (dfn/reduce-max (dtype/concat-buffers ys-bufs)) (dfn/reduce-max (clean y)))]
          {:points curves
           :x-domain (numeric-extent (clean x))
           :y-domain [y-min y-max]})))))

;; ---- KDE (kernel density estimation) ----

(defn- bw-nrd0
  "R's `bw.nrd0`, the bandwidth rule `stats::density` and so ggplot2 use:
   `0.9 * min(sd, IQR/1.34) * n^(-1/5)`, with R type-7 quantiles for the
   IQR.

   Fastmath offers `:nrd0` too, but its interquartile range comes from a
   different quantile estimator -- on iris virginica it reads 0.75 where
   R reads 0.675, which is enough to widen the bandwidth by 11% and
   visibly flatten that group's peak. The boxplot summary in this file
   already settled on type-7 for the same reason, so reuse it and the two
   agree."
  [col]
  (let [arr (double-array (sort col))
        n (alength arr)
        sd (double (stats/stddev arr))
        iqr (- (quantile-r7 arr 0.75) (quantile-r7 arr 0.25))
        lo (min sd (/ iqr 1.34))
        ;; R falls back through sd, then |x1|, then 1 when the spread
        ;; degenerates -- e.g. a column where over half the values tie.
        lo (cond (pos? lo) lo
                 (pos? sd) sd
                 (pos? (Math/abs (aget arr 0))) (Math/abs (aget arr 0))
                 :else 1.0)]
    (* 0.9 lo (Math/pow n -0.2))))

(defn- fit-kde
  "Compute KDE for a numeric column. Returns {:xs [...] :ys [...]}.

   A density estimate is defined everywhere, so drawing one means picking
   an interval to evaluate it on. By default that is the column's own
   extent, so the curve ends where the data ends. Two overrides, both
   mirroring what `trim` selects in ggplot2:

   - `:interval` evaluates over an explicit `[lo hi]` instead -- a
     density layer passes the whole layer's range so every group spans
     the same axis, which is `geom_density`'s default.
   - `:pad-bandwidths` extends the interval by that many bandwidths on
     each side, letting the tails fall away -- `geom_violin(trim =
     FALSE)` uses 3. Padding is expressed in bandwidths, not in a
     fraction of the data span, because it is the smoothing that decides
     how far a tail reaches.

   Without an explicit bandwidth the smoothing follows `nrd0`, the rule
   R's `density()` and so ggplot2 use. Fastmath's own default is `nrd`,
   which scales by 1.06 where `nrd0` scales by 0.9 and therefore smooths
   about 18% wider -- enough to visibly lower and broaden a peak against
   the same plot drawn in ggplot2."
  ([col n-grid bandwidth] (fit-kde col n-grid bandwidth nil))
  ([col n-grid bandwidth {:keys [interval pad-bandwidths]}]
   (let [xs (double-array col)
         h (or bandwidth (bw-nrd0 col))
         info (kernel/kernel-density :gaussian xs h true)
         kd (:kde info)
         pad (* (double (or pad-bandwidths 0)) (double (:h info)))
         [lo0 hi0] (or interval (numeric-extent col))
         lo (- (double lo0) pad)
         hi (+ (double hi0) pad)
         step (if (<= n-grid 1) 0.0 (/ (- hi lo) (double (dec n-grid))))
         grid-xs (dfn/+ lo (dfn/* step (range n-grid)))
         grid-ys (dtype/emap kd :float64 grid-xs)]
     {:xs grid-xs :ys grid-ys})))

(defmethod compute-stat :density [draft-layer]
  (validate-numeric-column draft-layer :x :kde)
  (let [{:keys [data x group cfg]} draft-layer
        clean (drop-missing-reported data [x])
        n (tc/row-count clean)
        ;; 512 grid points, matching ggplot2. 100 looked identical at a
        ;; default 600px plot but drew a visibly faceted peak once the
        ;; plot was enlarged, since the segments between grid points grow
        ;; with it.
        n-grid (or (:kde-n-grid (or cfg defaults/defaults)) 512)
        bandwidth (:kde-bandwidth (or cfg defaults/defaults))
        ;; Untrimmed by default, matching ggplot2's geom_density: every
        ;; group is estimated across the whole layer's values, so grouped
        ;; curves share one interval and each falls away to nothing
        ;; instead of being cut off at its own group's extremes. `:trim
        ;; true` estimates each group over its own values, which ends the
        ;; curves abruptly but shows where each group's data lies.
        trim? (get draft-layer :trim false)]
    (if (or (< n 2)
            (near-constant? (clean x)))
      {:points []
       :x-domain (if (pos? n) (numeric-extent (clean x)) [0 1])
       :y-domain [0 1]}
      (let [layer-interval (numeric-extent (clean x))
            curves (group-by-columns
                    clean (or group [])
                    (fn [ds gv]
                      (when (>= (tc/row-count ds) 2)
                        (cond-> (fit-kde (ds x) n-grid bandwidth
                                         (when-not trim? {:interval layer-interval}))
                          (some? gv) (assoc :color gv)))))
            curves (remove nil? curves)
            ys-bufs (seq (map :ys curves))
            y-max (if ys-bufs (dfn/reduce-max (dtype/concat-buffers ys-bufs)) 1)
            xs-bufs (seq (map :xs curves))
            ;; The curve is estimated over exactly the interval the axis
            ;; reports, so the reported domain is the curve's own extent.
            ;; Either way that comes to the observed range -- untrimmed
            ;; every group already spans it, trimmed the per-group extents
            ;; union to it -- which is why the curve ends where the data
            ;; ends instead of running off the panel.
            curve-domain (when xs-bufs
                           (let [all-xs (dtype/concat-buffers xs-bufs)]
                             [(dfn/reduce-min all-xs) (dfn/reduce-max all-xs)]))]
        {:points curves
         :x-domain (or curve-domain (numeric-extent (clean x)))
         :y-domain [0 y-max]}))))

;; ---- Boxplot ----

(defn- per-category-stat
  "Shared iteration for boxplot/violin: clean data, group by category and
   optional color, apply per-group-fn to each subset's numeric column.
   Works in both orientations -- categorical x + numerical y (vertical,
   the traditional shape) and numerical x + categorical y (horizontal).
   The returned :x-domain / :y-domain are swapped accordingly so the
   downstream panel layout picks the right scale type on each axis.
   per-group-fn: (fn [num-col category color-or-nil]) -> map
   min-n: minimum row count to include a group."
  [draft-layer min-n per-group-fn]
  (let [{:keys [data x y x-type y-type group]} draft-layer
        ;; Pick the categorical axis. When y is categorical (and x isn't),
        ;; the plot is horizontal; otherwise default to x as the cat axis.
        flipped? (and (= y-type :categorical) (not= x-type :categorical))
        cat-col (if flipped? y x)
        num-col (if flipped? x y)
        clean (-> (drop-missing-reported data [x y])
                  (format-category-column cat-col))
        categories (distinct (clean cat-col))]
    (if (empty? categories)
      {:items [] :categories [] :color-categories nil
       :x-domain (if flipped? [0 1] ["?"])
       :y-domain (if flipped? ["?"] [0 1])}
      (let [group-cols (or group [])
            color-col (first group-cols)
            has-color? (and (seq group-cols) color-col)
            clean-c (if has-color? (drop-missing-reported clean group-cols) clean)
            color-cats (when has-color? (vec (sort (distinct (clean-c color-col)))))
            ;; Single tc/group-by replaces O(cats × colors) select-rows calls
            group-keys (if has-color? [cat-col color-col] [cat-col])
            grouped (tc/group-by clean-c group-keys {:result-type :as-map})
            ;; When cat-col == color-col, each category IS its own color group
            ;; (no cross-product — a "setosa" category can only have "setosa" color)
            cat-is-color? (and has-color? (= cat-col color-col))
            items (vec
                   (cond
                     cat-is-color?
                     (for [cat categories
                           :let [gk {cat-col cat}
                                 ds (get grouped gk)]
                           :when (and ds (>= (tc/row-count ds) min-n))]
                       (per-group-fn (ds num-col) cat cat))

                     has-color?
                     (for [cat categories
                           cc color-cats
                           :let [gk (zipmap group-keys [cat cc])
                                 ds (get grouped gk)]
                           :when (and ds (>= (tc/row-count ds) min-n))]
                       (per-group-fn (ds num-col) cat cc))

                     :else
                     (for [cat categories
                           :let [gk {cat-col cat}
                                 ds (get grouped gk)]
                           :when (and ds (>= (tc/row-count ds) min-n))]
                       (per-group-fn (ds num-col) cat nil))))
            all-nums (clean num-col)
            num-min (dfn/reduce-min all-nums)
            num-max (dfn/reduce-max all-nums)]
        {:items items
         :categories categories
         :color-categories color-cats
         :x-domain (if flipped? [num-min num-max] categories)
         :y-domain (if flipped? categories [num-min num-max])}))))

(defn- five-number-summary
  "Compute boxplot five-number summary for a numeric column.
   Uses R type 7 quantiles (ggplot2/R default) for Q1, median, Q3.
   Returns {:median :q1 :q3 :whisker-lo :whisker-hi :outliers}."
  [col]
  (let [sorted (double-array (sort col))
        q1 (quantile-r7 sorted 0.25)
        median (quantile-r7 sorted 0.5)
        q3 (quantile-r7 sorted 0.75)
        iqr (- (double q3) (double q1))
        fence-lo (- (double q1) (* 1.5 iqr))
        fence-hi (+ (double q3) (* 1.5 iqr))
        ;; Whiskers: closest data values within fences
        ;; Outliers: data values outside fences
        whisker-lo (atom (double q1))
        whisker-hi (atom (double q3))
        outliers (java.util.ArrayList.)]
    (dotimes [i (count col)]
      (let [v (double (col i))]
        (cond
          (< v fence-lo) (.add outliers v)
          (> v fence-hi) (.add outliers v)
          :else (do (when (< v @whisker-lo) (reset! whisker-lo v))
                    (when (> v @whisker-hi) (reset! whisker-hi v))))))
    {:median median :q1 q1 :q3 q3
     :whisker-lo @whisker-lo :whisker-hi @whisker-hi
     :outliers (vec outliers)}))

(defmethod compute-stat :boxplot [draft-layer]
  (let [result (per-category-stat draft-layer 1
                                  (fn [y-col cat cc]
                                    (cond-> (merge (five-number-summary y-col)
                                                   {:category cat})
                                      cc (assoc :color cc))))]
    {:boxes (:items result)
     :categories (:categories result)
     :color-categories (:color-categories result)
     :x-domain (:x-domain result)
     :y-domain (:y-domain result)}))

;; ---- Violin ----

(defmethod compute-stat :violin [draft-layer]
  ;; Validate the numeric axis -- y in vertical orientation, x in horizontal.
  (let [num-axis (if (and (= (:y-type draft-layer) :categorical)
                          (not= (:x-type draft-layer) :categorical))
                   :x :y)]
    (validate-numeric-column draft-layer num-axis :violin))
  (let [{:keys [cfg]} draft-layer
        ;; 80 rather than the density's 512: a violin compresses the
        ;; density onto a narrow band, so the same grid buys far less
        ;; smoothness. Measured at 512 the outline is indistinguishable
        ;; even enlarged, while the SVG grows fivefold.
        n-grid (or (:kde-n-grid (or cfg defaults/defaults)) 80)
        bandwidth (:kde-bandwidth (or cfg defaults/defaults))
        ;; Trimmed by default, matching ggplot2's geom_violin: each body
        ;; ends at its category's values rather than tapering into a long
        ;; needle past them. `:trim false` lets the tails fall away by
        ;; three bandwidths on each side, which is what geom_violin does
        ;; when untrimmed -- note this differs from an untrimmed density,
        ;; where ggplot2 widens to the whole scale instead.
        trim? (get draft-layer :trim true)
        result (per-category-stat draft-layer 2
                                  (fn [y-col cat cc]
                                    (let [kde (fit-kde y-col n-grid bandwidth
                                                       (when-not trim? {:pad-bandwidths 3}))]
                                      (cond-> {:category cat
                                               :ys (:xs kde) :densities (:ys kde)}
                                        cc (assoc :color cc)))))
        ;; Cover the estimated curves on the numeric axis. Trimmed, each
        ;; category's curve spans that category's own values, so this comes
        ;; to the overall data range; untrimmed it widens by the padded
        ;; tails. Which axis is numeric depends on orientation: y for
        ;; vertical, x for horizontal.
        flipped? (and (= (:y-type draft-layer) :categorical)
                      (not= (:x-type draft-layer) :categorical))
        kde-nums-bufs (seq (map :ys (:items result)))
        kde-num-domain (when kde-nums-bufs
                         (let [all (dtype/concat-buffers kde-nums-bufs)]
                           [(dfn/reduce-min all) (dfn/reduce-max all)]))
        x-domain (if (and flipped? kde-num-domain)
                   kde-num-domain
                   (:x-domain result))
        y-domain (if (and (not flipped?) kde-num-domain)
                   kde-num-domain
                   (:y-domain result))]
    {:violins (:items result)
     :categories (:categories result)
     :color-categories (:color-categories result)
     :x-domain x-domain
     :y-domain y-domain}))

;; ---- 2D Binning (for heatmap/tile) ----

;; ---- Summary (mean ± SE per category) ----

(defmethod compute-stat :summary [{:keys [data x y x-type group] :as draft-layer}]
  (validate-numeric-column draft-layer :y :summary)
  (let [clean (cond-> (drop-missing-reported data [x y])
                (= x-type :categorical) (format-category-column x))
        categories (distinct (clean x))]
    (if (empty? categories)
      {:points [] :x-domain ["?"] :y-domain [0 1]}
      (let [group-cols (or group [])
            ;; Use tc/group-by for O(n) grouping instead of per-category select-rows
            all-groups (group-by-columns
                        clean group-cols
                        (fn [ds gv]
                          (let [cat-grouped (tc/group-by ds [x] {:result-type :as-map})
                                per-cat (for [cat categories
                                              :let [cat-ds (get cat-grouped {x cat})]
                                              :when (and cat-ds (pos? (tc/row-count cat-ds)))
                                              :let [ys-col (cat-ds y)
                                                    n (tc/row-count cat-ds)
                                                    mean-val (stats/mean ys-col)
                                                    se (if (>= n 2)
                                                         (/ (stats/stddev ys-col) (Math/sqrt (double n)))
                                                         0.0)]]
                                          {:cat cat :mean mean-val :se se})]
                            (cond-> {:xs (mapv :cat per-cat)
                                     :ys (mapv :mean per-cat)
                                     :ymins (mapv #(- (:mean %) (:se %)) per-cat)
                                     :ymaxs (mapv #(+ (:mean %) (:se %)) per-cat)}
                              (some? gv) (assoc :color gv)))))
            all-ys (mapcat (fn [g] (concat (:ymins g) (:ymaxs g))) all-groups)
            y-min (if (seq all-ys) (reduce min all-ys) 0)
            y-max (if (seq all-ys) (reduce max all-ys) 1)]
        {:points all-groups
         :x-domain categories
         :y-domain [y-min y-max]}))))

(defmethod compute-stat :bin2d [{:keys [data x y cfg] :as draft-layer}]
  (validate-numeric-column draft-layer :x :bin2d)
  (validate-numeric-column draft-layer :y :bin2d)
  (let [cfg (or cfg defaults/defaults)
        clean (drop-missing-reported data [x y])
        n (tc/row-count clean)]
    (if (zero? n)
      {:tiles [] :x-domain [0 1] :y-domain [0 1] :fill-range [0 1]}
      (let [xs-col (clean x)
            ys-col (clean y)
            [x-min x-max] (numeric-extent xs-col)
            [y-min y-max] (numeric-extent ys-col)
            n-bins (or (:tile-bins cfg) 15)
            x-step (/ (- (double x-max) (double x-min)) n-bins)
            y-step (/ (- (double y-max) (double y-min)) n-bins)
            ;; Count points in each bin
            counts (reduce (fn [acc i]
                             (let [xv (double (xs-col i))
                                   yv (double (ys-col i))
                                   xi (min (int (/ (- xv x-min) (max 1e-10 x-step)))
                                           (dec n-bins))
                                   yi (min (int (/ (- yv y-min) (max 1e-10 y-step)))
                                           (dec n-bins))
                                   k [xi yi]]
                               (assoc acc k (inc (get acc k 0)))))
                           {}
                           (range n))
            max-count (reduce max 1 (vals counts))
            ;; Build tile dataset — each tile is an observation with bounds and fill.
            ;; Iterate in row-major order (yi, xi) so the output is deterministic
            ;; regardless of the hash map's internal key ordering.
            tile-data (reduce (fn [acc [xi yi]]
                                (let [cnt (get counts [xi yi])]
                                  (if cnt
                                    (-> acc
                                        (update :x-lo conj (+ x-min (* xi x-step)))
                                        (update :x-hi conj (+ x-min (* (inc xi) x-step)))
                                        (update :y-lo conj (+ y-min (* yi y-step)))
                                        (update :y-hi conj (+ y-min (* (inc yi) y-step)))
                                        (update :fill conj cnt))
                                    acc)))
                              {:x-lo [] :x-hi [] :y-lo [] :y-hi [] :fill []}
                              (for [yi (range n-bins) xi (range n-bins)] [xi yi]))
            tiles (tc/dataset tile-data)]
        {:tiles tiles
         :x-domain [x-min x-max]
         :y-domain [y-min y-max]
         :fill-range [0 max-count]}))))

(defmethod compute-stat :density-2d [{:keys [data x y cfg] :as draft-layer}]
  (validate-numeric-column draft-layer :x :kde2d)
  (validate-numeric-column draft-layer :y :kde2d)
  (let [cfg (or cfg defaults/defaults)
        clean (drop-missing-reported data [x y])
        n (tc/row-count clean)]
    (if (< n 2)
      ;; Empty-data branch: return :grid nil explicitly so downstream
      ;; contour extraction (which reads :grid) has shape stability.
      {:tiles [] :x-domain [0 1] :y-domain [0 1] :fill-range [0 1] :grid nil}
      (let [xs-col (clean x)
            ys-col (clean y)
            xs (double-array xs-col)
            ys (double-array ys-col)
            [x-min x-max] (numeric-extent xs-col)
            [y-min y-max] (numeric-extent ys-col)
            x-range (- (double x-max) (double x-min))
            y-range (- (double y-max) (double y-min))
            ;; Extend domain by 30% for smooth falloff at edges
            x-lo (- (double x-min) (* 0.3 x-range))
            x-hi (+ (double x-max) (* 0.3 x-range))
            y-lo (- (double y-min) (* 0.3 y-range))
            y-hi (+ (double y-max) (* 0.3 y-range))
            ;; Grid resolution
            n-grid (or (:density-2d-grid cfg) 25)
            x-step (max 1e-10 (/ (- x-hi x-lo) n-grid))
            y-step (max 1e-10 (/ (- y-hi y-lo) n-grid))
            ;; Bandwidth: Silverman's rule for 2D product-kernel KDE.
            ;; h_j = sigma_j * n^(-1/6), matching the d=2 case of the general
            ;; formula h = (4/(d+2))^(1/(d+4)) * sigma * n^(-1/(d+4)).
            ;; ggplot2 uses MASS::bandwidth.nrd which is slightly different
            ;; (Sheather-Jones plug-in), but Silverman-2D is a good default.
            bw-cfg (:kde2d-bandwidth cfg)
            x-std (stats/stddev xs)
            y-std (stats/stddev ys)
            n-pow (Math/pow (double n) (/ -1.0 6.0))
            bw-x (max 1e-10 (if bw-cfg (first bw-cfg) (* x-std n-pow)))
            bw-y (max 1e-10 (if bw-cfg (second bw-cfg) (* y-std n-pow)))
            inv-bwx2 (/ 1.0 (* 2.0 bw-x bw-x))
            inv-bwy2 (/ 1.0 (* 2.0 bw-y bw-y))
            ;; Compute density at each grid cell center
            densities (double-array (* n-grid n-grid))
            _ (dotimes [gi n-grid]
                (dotimes [gj n-grid]
                  (let [gx (+ x-lo (* (+ gi 0.5) x-step))
                        gy (+ y-lo (* (+ gj 0.5) y-step))
                        d (loop [k 0 acc 0.0]
                            (if (< k n)
                              (let [dx (- gx (aget xs k))
                                    dy (- gy (aget ys k))
                                    w (Math/exp (- (+ (* dx dx inv-bwx2)
                                                      (* dy dy inv-bwy2))))]
                                (recur (inc k) (+ acc w)))
                              acc))]
                    (aset densities (+ (* gi n-grid) gj) d))))
            max-d (dfn/reduce-max densities)
            ;; Build tile dataset with density as fill value
            tile-data (reduce (fn [acc [gi gj]]
                                (let [d (aget densities (+ (* gi n-grid) gj))]
                                  (if (> d (* 0.01 max-d))
                                    (-> acc
                                        (update :x-lo conj (+ x-lo (* gi x-step)))
                                        (update :x-hi conj (+ x-lo (* (inc gi) x-step)))
                                        (update :y-lo conj (+ y-lo (* gj y-step)))
                                        (update :y-hi conj (+ y-lo (* (inc gj) y-step)))
                                        (update :fill conj d))
                                    acc)))
                              {:x-lo [] :x-hi [] :y-lo [] :y-hi [] :fill []}
                              (for [gi (range n-grid) gj (range n-grid)] [gi gj]))
            tiles (tc/dataset tile-data)]
        {:tiles tiles
         :x-domain [x-lo x-hi]
         :y-domain [y-lo y-hi]
         :fill-range [0 max-d]
         ;; Raw grid data for contour extraction
         :grid {:densities densities
                :n-grid n-grid
                :x-lo x-lo :x-hi x-hi
                :y-lo y-lo :y-hi y-hi
                :x-step x-step :y-step y-step
                :max-d max-d}}))))
