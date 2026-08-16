(ns scicloj.plotje.impl.scale
  (:require [wadogo.scale :as ws]
            [clojure.string :as str]
            [scicloj.plotje.impl.defaults :as defaults]))

(defn categorical-domain?
  "True if domain is a sequence of non-numeric values (categorical)."
  [dom]
  (and (sequential? dom) (seq dom) (not (number? (first dom)))))

(defn scale-kind
  "Determine the wadogo scale type (:categorical, :log, or :linear) from domain and spec.
   Passes through vector dispatch values like [:linear :doc] for doc defmethods."
  [domain scale-spec]
  (if (and (vector? domain) (= :doc (second domain)))
    domain
    (cond
      (and (categorical-domain? domain) (= :log (:type scale-spec)))
      (throw (ex-info (str "Log scale requires numeric data, but domain is categorical: "
                           (vec (take 5 domain))
                           (when (> (count domain) 5) " ..."))
                      {:domain domain :scale-spec scale-spec}))
      (categorical-domain? domain) :categorical
      (= :log (:type scale-spec)) :log
      (let [t (:type scale-spec)]
        (and t (not= t :linear)))
      (throw (ex-info (str "Unknown scale type: " (:type scale-spec)
                           ". Supported: :linear, :log")
                      {:scale-spec scale-spec}))
      :else :linear)))

(defmulti make-scale
  "Create a wadogo scale mapping domain values to a pixel range."
  (fn [domain pixel-range scale-spec] (scale-kind domain scale-spec)))

(defmethod make-scale :categorical [domain pixel-range scale-spec]
  (let [ticks-option
        (if (some? (:n-ticks scale-spec))
          {:ticks (:n-ticks scale-spec)}
          {})]
    (ws/scale :bands (merge {:domain domain
                             :range pixel-range}
                            ticks-option))))

(defmethod make-scale :linear [domain pixel-range _]
  (ws/scale :linear {:domain domain :range pixel-range}))

(defmethod make-scale :log [domain pixel-range _]
  (ws/scale :log {:domain domain :range pixel-range}))

(defn- band-value
  "The category whose band contains `v`, or nil when no band does.

   A band's `:rstart` and `:rend` are in range order, which runs
   downward on a y axis, so the interval is read through min and max
   rather than assuming rstart is the lower one. Bands are adjacent at
   the default zero padding, so a position exactly on a shared edge
   answers with the earlier category."
  [sc v]
  (let [v (double v)]
    (some (fn [{:keys [value rstart rend]}]
            (let [a (double rstart)
                  b (double rend)]
              (when (<= (min a b) v (max a b)) value)))
          (ws/data sc :bands))))

(defn invert
  "Read a drawing-space position back as a data value. On a band scale
   this answers with the category whose band contains the position, and
   with nil outside every band.

   The band case is computed here rather than through `ws/inverse`.
   Wadogo's `bands-inverse-fn` destructures `:start` and `:end` from
   band maps whose keys are `:rstart` and `:rend`, so both locals are
   nil and its `^double` hint throws a NullPointerException for every
   input. The band data it needs is on the scale either way. Reported
   in `dev-notes/wadogo-band-inverse-issue.md`."
  [sc v]
  (if (= :bands (ws/kind sc))
    (band-value sc v)
    (ws/inverse sc v)))

(defmethod make-scale [:categorical :doc] [_ _ _] "Band scale (one band per category)")
(defmethod make-scale [:linear :doc] [_ _ _] "Continuous linear mapping")
(defmethod make-scale [:log :doc] [_ _ _] "Logarithmic mapping")

(defn pad-domain
  "Add padding to a numeric domain. When lo == hi (constant data),
   pads by ±1 or ±5% of |lo|, whichever is larger.
   For log scales, callers must supply positive lo and hi -- the
   responsibility for excluding non-positive values lives upstream
   (filter-log-nonpositive for raw data, the scale-aware branch of
   compute-global-y-domain for stat-derived ranges)."
  [[lo hi] scale-spec]
  (let [log? (= :log (:type scale-spec))
        padding (:domain-padding defaults/defaults)
        _ (when (and log? (or (not (pos? (double lo))) (not (pos? (double hi)))))
            (throw (ex-info (str "A log scale cannot include zero or negative values, but this "
                                 "axis spans [" lo ", " hi "]. Bar and area marks draw from a "
                                 "zero baseline, so they cannot sit on a log-scaled value axis -- "
                                 "use a linear scale for those, or a mark without a baseline "
                                 "(lay-point, lay-line, lay-lollipop). If the data itself has "
                                 "non-positive values, a log scale does not apply to this axis.")
                            {:lo lo :hi hi :scale-spec scale-spec})))
        [a b] (if log? [(Math/log (double lo)) (Math/log (double hi))] [lo hi])
        span (- b a)
        pad (if (<= span 0.0)
              ;; Constant data: use ±max(1, 5% of |value|)
              (max 1.0 (* padding (Math/abs (double a))))
              (* padding span))
        from (if log? #(Math/exp %) identity)]
    [(from (- a pad)) (from (+ b pad))]))

(def by-methods
  "How a value spreads across the range a channel is drawn over.

   Each names what is spread evenly as the value runs from the low end
   of the domain to the high end, and each is read in the mark's own
   quantity -- a circle's radius, a stroke's width:

   - `:linear` -- the quantity itself. A value twice another is drawn
     twice as wide, which on a circle is four times the ink. ggplot2
     spells this `scale_radius`.
   - `:sqrt` -- the quantity against the root of the value, the root
     being the quantity's ink exponent. The default, and what ggplot2's
     `scale_size` does. The smallest value still draws at the low end
     of the range rather than vanishing, and growth is compressed the
     way area demands; it is exactly area-proportional when the range
     starts at zero.
   - `:area` -- the ink. Equal steps in value are equal steps in area,
     which is the strict reading of what a size encoding claims.

   On a quantity whose ink grows linearly -- a stroke's width, an
   opacity -- all three are one function, because there is no area to
   correct for."
  #{:linear :sqrt :area})

(def default-by
  "The method a channel spreads by when its scale does not say.

   ggplot2's default for a point, adopted here: the perceptual
   correction is on, and the smallest mark stays visible."
  :sqrt)

(defn validate-drawn-range-options!
  "Throw when a scale spec names a drawn-range option the channel does
   not read, or names one Plotje cannot carry out.

   Called from both places a spec can be written -- `pj/scale` and a
   mapping's `:scale` -- because either can name a key the channel has
   no use for, and a key that is read by nothing is the defect this
   release exists to remove. `where` names the caller for the message.

   Only the drawn-range options are policed here. The rest of the spec
   is checked where it is written, because `:color-scale` carries a
   gradient as well as a scale spec and the two share the key."
  [channel spec where]
  (let [read-here (get defaults/channel-scale-options channel #{})]
    (doseq [k defaults/drawn-range-options
            :when (and (contains? spec k) (not (contains? read-here k)))]
      (throw (ex-info (str where " " channel " " k " " (pr-str (get spec k))
                           ", and " channel " reads no " k ". "
                           (case k
                             :range (str "A range is what a mark spans as the"
                                         " value runs across the domain, and "
                                         channel
                                         (if (#{:x :y} channel)
                                           " spans the panel, which the plot's size decides."
                                           " draws no such quantity."))
                             :by (str "It asks how a value spreads across that"
                                      " range in the quantity a mark draws --"
                                      " a radius, a width. " channel
                                      " has no such quantity to spread across.")
                             :from-zero (str "It anchors a range at zero so that"
                                             " twice the value is twice the ink,"
                                             " and " channel " draws no ink to"
                                             " double."))
                           " The channels that read " k ": "
                           (vec (sort (for [[c ks] defaults/channel-scale-options
                                            :when (contains? ks k)]
                                        c)))
                           ".")
                      {:channel channel :option k :value (get spec k)
                       :caller where}))))
  (when-let [by (:by spec)]
    (when-not (contains? by-methods by)
      (throw (ex-info (str where " " channel " :by " (pr-str by)
                           " is not a method a value can spread by."
                           " Supported: " (vec (sort by-methods)) ".")
                      {:channel channel :by by
                       :supported (vec (sort by-methods))
                       :caller where}))))
  (when (and (:from-zero spec) (= :log (:type spec)))
    (throw (ex-info (str where " " channel " sets :from-zero beside a log"
                         " scale. Anchoring at zero asks the scale to read a"
                         " value of zero, and a log scale has no reading for"
                         " it. Take one of the two off: :from-zero for a"
                         " proportional size, or the log scale for a"
                         " multiplicative one.")
                    {:channel channel :spec spec :caller where}))))

(defn channel-domain
  "The `[lo hi]` a channel's values are read against, given its scale
   spec and the range the data covers.

   `:domain` on the spec replaces the data's own range -- a way to fix
   what a size means across plots that would otherwise each scale to
   their own extremes. `:from-zero` anchors the low end at zero, which
   is what turns a spread across a range into a proportion: with the
   range anchored there too, twice the value is twice the ink."
  [spec d-min d-max]
  (let [[lo hi] (or (:domain spec) [d-min d-max])]
    [(if (:from-zero spec) 0.0 (double lo)) (double hi)]))

(defn channel-mapper
  "A function from a data value to the quantity a mark draws it as -- a
   radius, a width, an opacity.

   `spec` is the channel's scale spec: `:type` (`:linear` or `:log`),
   `:domain`, `:range`, `:by` and `:from-zero`, each optional.
   `default-range` is what the channel spans where the spec names no
   `:range`. `ink-exponent` comes from the mark's declared quantity --
   2 for a radius or a side, 1 for a width -- and is what lets `:by`
   mean the same thing whatever shape draws it.

   Stated once because the marks and the legend have to agree: the mark
   drawn beside a value in the legend is the size a mark of that value
   is drawn at on the panel. `impl.plan` builds the legend from this and
   `render.mark` draws from it, and they held separate copies of the
   arithmetic until both were found to share a defect.

   A value outside the domain is clamped to its nearer end rather than
   drawn outside the range. A `:domain` narrower than the data is a
   statement about what the reader should compare, not an instruction
   to drop rows -- and a dropped row leaves no trace on the panel.

   A domain of one distinct value has no spread to map, so every value
   takes the middle of the range. ggplot2's `scales::rescale` answers a
   zero range the same way, and for the same reason: with nothing to
   compare a value against, the midpoint is the only unprejudiced
   answer. Collapsing to the low end instead drew
   `{:size {:value 7 :scale true}}` at radius 2.0 -- smaller than the
   default 3.0 -- beside a legend reading 7, and did the same to any
   size column whose values happen to be equal."
  [spec d-min d-max default-range ink-exponent]
  (let [log?     (= :log (:type spec))
        ->space  (if log? #(Math/log10 (max 1e-300 (double %))) double)
        [d-lo d-hi] (channel-domain spec d-min d-max)
        lo       (->space d-lo)
        hi       (->space d-hi)
        [dr-lo dr-hi] default-range
        [r-lo r-hi]   (or (:range spec) [dr-lo dr-hi])
        r-lo     (if (:from-zero spec) 0.0 (double r-lo))
        r-hi     (double r-hi)
        e        (double ink-exponent)
        by       (or (:by spec) default-by)
        span     (- hi lo)
        ;; Relative, as `scales::zero_range` is. An absolute floor
        ;; leaves a band just above it where the domain is treated as
        ;; real and the output is squeezed against the low end: with
        ;; the old `(max 1e-6 span)`, a span of 1e-7 drew every mark
        ;; between radius 2.0 and 2.6 -- below the default 3.0, which
        ;; is the very picture the degenerate case was fixed to avoid.
        ;; Either side of the test is now a whole answer: the midpoint,
        ;; or the full range.
        degenerate? (<= (Math/abs span)
                        (* 1e-12 (max 1.0 (Math/abs lo) (Math/abs hi))))
        ;; `t` is the fraction of the domain a value has reached. Each
        ;; method spreads a different function of it evenly across the
        ;; range; on an ink exponent of 1 the three collapse into one.
        place    (fn [t]
                   (case by
                     :linear (+ r-lo (* (- r-hi r-lo) t))
                     :sqrt   (+ r-lo (* (- r-hi r-lo) (Math/pow t (/ 1.0 e))))
                     :area   (Math/pow (+ (Math/pow r-lo e)
                                          (* t (- (Math/pow r-hi e)
                                                  (Math/pow r-lo e))))
                                       (/ 1.0 e))))]
    (if degenerate?
      (constantly (+ r-lo (* 0.5 (- r-hi r-lo))))
      (fn [v] (place (min 1.0 (max 0.0 (/ (- (->space v) lo) span))))))))

(defn- format-ticks*
  "Format tick values without any digit grouping. See format-ticks."
  [sx ticks]
  (if (every? #(== (Math/floor %) %) ticks)
    ;; All whole numbers — strip the .0
    (mapv #(str (long %)) ticks)
    ;; Float ticks — determine decimal places from step
    (let [n (count ticks)]
      (if (< n 2)
        (ws/format sx ticks)
        (let [step (Math/abs (- (double (nth ticks 1)) (double (nth ticks 0))))
              ;; Number of decimal places needed: -floor(log10(step)) clamped to [0,10]
              decimals (if (pos? step)
                         (min 10 (max 0 (long (Math/ceil (- (Math/log10 step))))))
                         1)
              fmt (str "%." decimals "f")
              neg-zero (defaults/fmt-root fmt -0.0)
              zero (defaults/fmt-root fmt 0.0)]
          (mapv (fn [v]
                  (let [s (defaults/fmt-root fmt (double v))
                        ;; Clean up -0.0 → 0.0
                        s (if (= s neg-zero) zero s)]
                    ;; Strip trailing zeros after decimal point, but keep at least one
                    ;; "1.20" → "1.2", "1.00" → "1.0", "0.0010" → "0.001"
                    (if (.contains s ".")
                      (let [trimmed (str/replace s #"0+$" "")]
                        (if (.endsWith trimmed ".")
                          (str trimmed "0")
                          trimmed))
                      s)))
                ticks))))))

(defn format-ticks
  "Format tick values: integers shown without decimals, floats rounded to the
   precision implied by the tick step size (avoids floating-point noise like
   0.30000000000000004). Falls back to wadogo formatting only when the step
   cannot be determined (< 2 ticks).

   Every value is formatted under `Locale/ROOT`, so what a tick reads
   does not depend on the JVM it renders on.

   The 3-arity writes each formatted tick with `separators`, the
   `{:thousands ... :decimal ...}` map `defaults/number-separators`
   reads off a config. Layout measures label widths through this same
   function, so a grouped axis reserves room for the separators it will
   draw."
  ([sx ticks]
   (format-ticks sx ticks nil))
  ([sx ticks separators]
   (mapv #(defaults/fmt-number % separators)
         (format-ticks* sx ticks))))

(def ^:private endpoint-significant-digits
  "Significant digits kept in a continuous legend's endpoint labels.
   Six distinguishes values a plot is likely to show without printing
   the full width of a double: 123456 and 123999 read apart, while
   0.10000000000000009 reads 0.1."
  6)

(defn- plain-significant
  "`v` to `sig` significant digits, written out in full rather than in
   scientific notation, with trailing zeros stripped.

   Java's `%g` gives up on plain notation once the exponent reaches the
   precision, which is what made a legend read 1.235e+05. The decimal
   count is derived from the value's own magnitude instead, so a large
   number keeps its digits and a small one keeps its precision."
  [v sig]
  (let [v (double v)]
    (if (zero? v)
      "0"
      (let [magnitude (long (Math/floor (Math/log10 (Math/abs v))))
            decimals (min 12 (max 0 (- (long sig) 1 magnitude)))
            s (defaults/fmt-root (str "%." decimals "f") v)]
        (if (str/includes? s ".")
          (-> s (str/replace #"0+$" "") (str/replace #"\.$" ""))
          s)))))

(defn format-range-endpoints
  "The two labels a continuous legend prints at the ends of its bar.

   Each end is written to six significant digits in plain notation, so
   what a legend reads matches what its axis reads. `%.4g` did neither
   half of that: four significant digits switch to scientific notation
   at 10000, so a legend for a count read 1.235e+05 beside an axis
   reading 100,000, and two ends as far apart as 123456 and 123999 read
   alike. Below the switch it padded rather than truncated, so a span of
   0.1 to 2.5 read 0.1000 and 2.500.

   Precision follows each value's own magnitude rather than the span
   between them: a legend from 0.001 to 1000 has to show its low end as
   0.001, which a span-derived step would have rounded to 0.

   `separators` is the map `defaults/number-separators` reads off a
   config, as for `format-ticks`."
  [lo hi separators]
  (mapv #(defaults/fmt-number (plain-significant % endpoint-significant-digits)
                              separators)
        [lo hi]))

(defn format-log-ticks
  "Format log scale tick values. Values are always clean 1-2-3-5 multiples
   of powers of 10, so formatting is straightforward: integers >= 1 shown
   without decimals, sub-1 values use minimal decimal places."
  [ticks]
  (mapv (fn [v]
          (let [v (double v)]
            (if (or (zero? v) (neg? v))
              (str v)
              (if (and (>= v 1.0) (== v (Math/floor v)))
                (str (long v))
                (if (< v 1.0)
                  (let [exp (long (Math/ceil (- (Math/log10 v))))]
                    (defaults/fmt-root (str "%." exp "f") v))
                  (str v))))))
        ticks))

(defn log-ticks
  "Generate clean log-scale tick values for a [lo hi] domain, targeting
   approximately n ticks. Uses ggplot2-style 1-2-5 nice numbers instead
   of wadogo's linear-in-log-space approach (which produces irrational
   values like 3.162...). Returns a vector of tick values (doubles).

   Strategy:
   - Powers of 10 only when they give >= 3 ticks (strongly preferred)
   - 1-2-5 intermediates per decade when more ticks are needed
   - 1-2-3-5 intermediates for dense sub-decade ranges
   - Bounding powers of 10 are included when they fall within a small
     margin (15% of log-span) of the domain edges"
  [[lo hi] n]
  (let [lo (max (double lo) 1e-300)
        hi (max (double hi) lo)
        log-lo-f (Math/log10 lo)
        log-hi-f (Math/log10 hi)
        log-span (- log-hi-f log-lo-f)
        margin (* 0.15 (max log-span 0.5))
        log-lo-i (long (Math/floor log-lo-f))
        log-hi-i (long (Math/ceil log-hi-f))
        ;; Powers of 10 with margin (catches nearby bounding powers)
        powers (vec (sort (for [exp (range log-lo-i (inc log-hi-i))
                                :let [v (Math/pow 10.0 exp)]
                                :when (and (>= (double exp) (- log-lo-f margin))
                                           (<= (double exp) (+ log-hi-f margin)))]
                            v)))]
    ;; Strongly prefer powers of 10 — use them if >= 3 ticks
    (if (>= (count powers) 3)
      powers
      ;; Need intermediates for narrow ranges (< 3 decades visible)
      (let [make-intermediate
            (fn [mset]
              (let [lo-pow (Math/pow 10.0 log-lo-i)
                    hi-pow (Math/pow 10.0 log-hi-i)]
                (vec (sort (distinct
                            (concat
                             (filter #(and (>= (Math/log10 %) (- log-lo-f margin))
                                           (<= (Math/log10 %) (+ log-hi-f margin)))
                                     [lo-pow hi-pow])
                             (for [exp (range log-lo-i (inc log-hi-i))
                                   mult mset
                                   :let [v (* (double mult) (Math/pow 10.0 exp))]
                                   :when (and (>= v (* lo 0.9999))
                                              (<= v (* hi 1.0001)))]
                               v)))))))
            breaks-125 (make-intermediate [1 2 5])
            breaks-1235 (make-intermediate [1 2 3 5])
            ;; Also consider powers with the bounding powers included
            candidates [{:breaks powers :cnt (count powers)}
                        {:breaks breaks-125 :cnt (count breaks-125)}
                        {:breaks breaks-1235 :cnt (count breaks-1235)}]
            score (fn [{:keys [cnt]}]
                    (let [diff (- cnt n)]
                      (if (neg? diff) (* 2.0 (Math/abs diff)) (double diff))))
            best (apply min-key score candidates)]
        (:breaks best)))))

(defn tick-count
  "Suggested tick count based on available pixel range."
  [pixel-range spacing]
  (max 2 (int (/ pixel-range spacing))))
