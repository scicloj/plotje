(ns scicloj.plotje.impl.scale
  (:require [wadogo.scale :as ws]
            [java-time.api :as jt]
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
      ;; A categorical scale places categories, and this domain holds
      ;; numbers. The message used to read "Unknown scale type:
      ;; :categorical. Supported: :linear, :log", which is wrong twice:
      ;; the type is supported, and the reason it does not apply here
      ;; is the column, not the type.
      (= :categorical (:type scale-spec))
      (throw (ex-info (str "A :categorical scale places categories, and this"
                           " axis spans numbers: " (pr-str (vec domain)) "."
                           " To read a numeric column as categories, set"
                           " :x-type or :y-type to :categorical on the layer"
                           " -- the scale follows from the column's type."
                           " A :categorical scale spec supplies the category"
                           " order for a column that is already categorical.")
                      {:domain (vec domain) :scale-spec scale-spec}))
      (let [t (:type scale-spec)]
        (and t (not= t :linear)))
      (throw (ex-info (str "Unknown scale type: " (:type scale-spec)
                           ". Supported: :linear, :log, :categorical")
                      {:scale-spec scale-spec}))
      :else :linear)))

(defmulti make-scale
  "Create a wadogo scale mapping domain values to a pixel range."
  (fn [domain pixel-range scale-spec] (scale-kind domain scale-spec)))

(defmethod make-scale :categorical [domain pixel-range _]
  (ws/scale :bands {:domain domain :range pixel-range}))

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
   compute-global-y-domain for stat-derived ranges).

   `padding` is the resolved `:domain-padding`. The 2-arity falls back
   to the library default, for a caller with no configuration in hand;
   every caller inside `draft->plan` passes the resolved value, which is
   how `pj/options` and `pj/with-config` reach this."
  ([domain scale-spec]
   (pad-domain domain scale-spec (:domain-padding defaults/defaults)))
  ([[lo hi] scale-spec padding]
   (let [log? (= :log (:type scale-spec))
         _ (when (and log? (or (not (pos? (double lo))) (not (pos? (double hi)))))
             (throw (ex-info (str "A log scale cannot include zero or negative values, but this "
                                  "axis spans [" lo ", " hi "]. A mark measured from zero carries "
                                  "zero into the domain whatever the data holds -- a bar drawn to "
                                  "a value, or a density -- and a log scale has no reading for it. "
                                  "Use a linear scale for the value axis, or a mark that is not "
                                  "measured from a baseline (lay-point, lay-line). If the data "
                                  "itself has non-positive values, a log scale does not apply "
                                  "to this axis.")
                             {:lo lo :hi hi :scale-spec scale-spec})))
         [a b] (if log? [(Math/log (double lo)) (Math/log (double hi))] [lo hi])
         span (- b a)
         pad (if (<= span 0.0)
               ;; Constant data: use ±max(1, 5% of |value|)
               (max 1.0 (* padding (Math/abs (double a))))
               (* padding span))
         from (if log? #(Math/exp %) identity)]
     [(from (- a pad)) (from (+ b pad))])))

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

(defn spec-keys
  "The keys a scale spec may carry for `aesthetic`. `:type` and
   `:domain` belong to every scale; the rest are per aesthetic, from
   `defaults/channel-scale-options`."
  [aesthetic]
  (into #{:type :domain} (get defaults/channel-scale-options aesthetic #{})))

(defn validate-spec-keys!
  "Throw when a scale spec names a key `aesthetic` does not read.

   Called from both places a spec can be written, so `pj/scale` and a
   mapping's `:scale` refuse the same keys. `pj/scale` used to take any
   key and read what it understood, so a misspelled `:rnge` set nothing
   and said nothing."
  [aesthetic spec where]
  (let [accepted (spec-keys aesthetic)]
    (when-let [unknown (seq (remove accepted (keys spec)))]
      (throw (ex-info (str where " " aesthetic " " (pr-str spec)
                           " has unexpected key(s): " (vec unknown) ". "
                           aesthetic "'s scale takes "
                           (vec (sort accepted)) ".")
                      {:aesthetic aesthetic :spec spec
                       :unknown (vec unknown)
                       :accepted (vec (sort accepted))
                       :caller where})))))

(def channel-bounds
  "What a channel's `:range` may span, where the quantity it is drawn
   as has limits of its own.

   An opacity outside 0 to 1 is not a fainter or a stronger colour --
   it is not a colour at all, and the PNG path throws from inside AWT
   where the SVG path silently emits an out-of-range attribute. A
   negative radius is not a smaller mark; it emits a negative width and
   draws nothing. Both used to pass every check."
  {:alpha {:lo 0.0 :hi 1.0 :what "an opacity"}
   :size  {:lo 0.0 :what "a size in drawing units"}})

(def ^:private gradient-range-channels
  "The channels whose `:range` is a gradient rather than a pair of
   numbers. A colour is picked from a ramp, not measured along one, so
   the range names the ramp."
  #{:color :fill})

(defn- validate-bounds-pair!
  "Throw when a spec's `:range` or `:domain` is not a pair of two finite
   numbers, or falls outside what the channel can draw.

   A malformed pair used to reach the arithmetic and die there on a
   bare `NullPointerException` naming neither the key nor the channel;
   an out-of-range one drew nothing, or threw from inside the rendering
   library one output format later."
  [channel spec k where]
  (when (contains? spec k)
    (let [v (get spec k)
          numbers? (and (sequential? v)
                        (= 2 (count v))
                        (every? #(and (number? %)
                                      (Double/isFinite (double %)))
                                v))]
      (when-not numbers?
        (throw (ex-info (str where " " channel " " k " " (pr-str v)
                             " is not a pair of two finite numbers, as "
                             (pr-str [0 1]) " is."
                             (when (and (sequential? v) (not= 2 (count v)))
                               (str " It has " (count v) ".")))
                        {:channel channel :option k :value v :caller where})))
      (when-let [{:keys [lo hi what]} (and (= k :range) (channel-bounds channel))]
        (when-let [bad (seq (remove #(and (or (nil? lo) (>= (double %) lo))
                                          (or (nil? hi) (<= (double %) hi)))
                                    v))]
          (throw (ex-info (str where " " channel " :range " (pr-str v)
                               " reaches " (pr-str (vec bad)) ", which "
                               channel " cannot draw: it is " what
                               ", so its range lies between "
                               (if hi (str lo " and " hi) (str lo " and up"))
                               ".")
                          {:channel channel :option k :value v
                           :out-of-bounds (vec bad) :caller where})))))))

(defn validate-drawn-range-options!
  "Throw when a scale spec names a drawn-range option the channel does
   not read, or names one Plotje cannot carry out.

   Called from both places a spec can be written -- `pj/scale` and a
   mapping's `:scale` -- because either can name a key the channel has
   no use for, and a key that is read by nothing is the defect this
   release exists to remove. `where` names the caller for the message.

   Only the drawn-range options are policed here. The rest of the spec
   is checked where it is written."
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
                           " The aesthetics that read " k ": "
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
                    {:channel channel :spec spec :caller where})))
  (when (contains? spec :from-zero)
    (let [v (:from-zero spec)]
      (when-not (or (true? v) (false? v))
        (throw (ex-info (str where " " channel " :from-zero " (pr-str v)
                             " is not true or false. It says whether the"
                             " domain and the range are anchored at zero.")
                        {:channel channel :from-zero v :caller where})))))
  ;; `:include` is numeric on every aesthetic that reads it, so its
  ;; shape is a property of the written value and is checked here.
  ;; What it cannot be checked against here is the column: whether the
  ;; axis is categorical, and whether a `:domain` written in another
  ;; scope has accumulated beside it, are both known only once the
  ;; merged spec meets the data, so `plan/include-anchors` answers
  ;; those.
  (when (contains? spec :include)
    (let [v (:include spec)
          vs (if (number? v) [v] v)
          numbers? (and (or (number? v) (sequential? v))
                        (seq vs)
                        (every? #(and (number? %)
                                      (Double/isFinite (double %)))
                                vs))]
      (when-not numbers?
        (throw (ex-info (str where " " channel " :include " (pr-str v)
                             " is not a finite number or a collection of"
                             " them, as " (pr-str 0) " and "
                             (pr-str [0 100]) " are."
                             (when (and (sequential? v) (empty? v))
                               (str " It is empty, and an empty :include"
                                    " extends the domain to nothing."))
                             " It names the values the domain has to"
                             " reach.")
                        {:channel channel :include v :caller where})))))
  ;; A range is a pair of numbers where the channel measures along it,
  ;; and a gradient where the channel picks a colour from it. One key,
  ;; two shapes, so the check follows the channel.
  (if (contains? gradient-range-channels channel)
    (when (contains? spec :range)
      (try
        (defaults/resolve-gradient-fn (:range spec))
        (catch clojure.lang.ExceptionInfo e
          (throw (ex-info (str where " " channel " :range -- " (ex-message e))
                          (assoc (ex-data e) :channel channel :caller where))))))
    (validate-bounds-pair! channel spec :range where))
  ;; Only where a domain can only be numeric. An axis, a shape and a
  ;; colour all take a domain of categories -- which is what orders
  ;; their legend -- so a pair of numbers is not the shape to demand
  ;; there.
  (when (contains? channel-bounds channel)
    (validate-bounds-pair! channel spec :domain where)))

(defn validate-spec-values!
  "Throw when a scale spec's `:breaks`, `:tick-labels` or `:values`
   holds something the channel cannot carry out.

   Called from both places a spec can be written -- `pj/scale` and a
   mapping's `:scale` -- because a spec means the same thing wherever
   it is written. These four checks lived only in `pj/scale`, so
   `{:shape {:column :c :scale {:values [:blob :thing :whatsit]}}}`
   planned without a word: every mark drew the fallback symbol while
   the legend advertised three symbols that do not exist.

   `where` names the caller for the message. Which channels read these
   keys at all is settled by `validate-spec-keys!`, off the published
   table, so this checks only what a key holds."
  [channel spec where]
  (let [breaks (:breaks spec)
        labels (:tick-labels spec)]
    (when (and (some? labels) (not (sequential? labels)))
      (throw (ex-info (str where " " channel " :tick-labels " (pr-str labels) " is not a"
                           " sequence of tick texts. It draws one text per"
                           " break, so it takes as many as :breaks names."
                           " To title the axis itself, use :label.")
                      {:caller where :channel channel :tick-labels labels})))
    (when (and labels (not breaks))
      (throw (ex-info (str where " " channel " :tick-labels requires :breaks. Pass both,"
                           " or drop :tick-labels to keep auto-formatted tick"
                           " text.")
                      {:caller where :channel channel :tick-labels labels})))
    (when (and breaks labels (not= (count breaks) (count labels)))
      (throw (ex-info (str where " " channel " :breaks and :tick-labels must have the same"
                           " count, got " (count breaks) " breaks and "
                           (count labels) " tick labels.")
                      {:caller where :channel channel
                       :breaks (vec breaks) :tick-labels (vec labels)}))))
  ;; `:values` is the enumerated output set, and what belongs in it
  ;; depends on the channel: marker symbols on `:shape`, colours on
  ;; `:color`. An unrecognized symbol draws as the fallback while the
  ;; legend advertises the name, so refuse it here rather than render a
  ;; legend that disagrees with its own marks.
  (when-let [values (:values spec)]
    (when (= channel :shape)
      (when-let [unknown (seq (remove (set defaults/shape-syms) values))]
        (throw (ex-info (str where " " channel " :values does not recognize "
                             (vec unknown) ". Supported symbols: "
                             defaults/shape-syms ".")
                        {:caller where :channel channel
                         :unknown (vec unknown)
                         :supported defaults/shape-syms}))))))

(defn numeric-color-domain
  "The `[lo hi]` a numeric colour or fill column is read against.

   `:domain` on the spec replaces the range the data covers, so a
   gradient can mean the same thing across plots and across the panels
   of a facet. A value outside it is drawn at the nearer end of the
   gradient rather than dropped, which is what `normalize-continuous`
   clamps for, and matches what `:size` and `:alpha` answer.

   Answers nil where the data gave no range, so a caller that had
   nothing to normalize against still has nothing. A categorical
   `:domain` -- a list of category names -- is not a numeric range and
   is ignored here; `order-by-domain` in `impl.plan` reads that one.

   Stated once because four places normalize a colour and a fifth
   builds the legend, and each held its own min and max."
  [spec d-min d-max]
  (when (and (some? d-min) (some? d-max))
    (let [[lo hi] (:domain spec)]
      (if (and (number? lo) (number? hi))
        [(double lo) (double hi)]
        [(double d-min) (double d-max)]))))

(defn channel-domain
  "The `[lo hi]` a channel's values are read against, given its scale
   spec and the range the data covers.

   `:domain` on the spec replaces the data's own range -- a way to fix
   what a size means across plots that would otherwise each scale to
   their own extremes. `:from-zero` anchors the low end at zero, which
   is what turns a spread across a range into a proportion: with the
   range anchored there too, twice the value is twice the ink.

   Anchored at zero it is the distance from zero that decides the ink,
   so the domain runs to the value furthest from zero in either
   direction. Taking the high end alone put every value at or below
   zero at the bottom of the range: a column of -5, 0 and 10 drew one
   mark of three and said nothing, and an all-negative column drew
   every mark at the maximum beside a legend running the other way.
   ggplot2's `scale_size_area` reads the magnitude here too, though it
   divides by the maximum rather than by the widest magnitude, which on
   an all-negative column draws marks larger than the range's own top."
  [spec d-min d-max]
  (let [[lo hi] (or (:domain spec) [d-min d-max])]
    (if (:from-zero spec)
      [0.0 (max (Math/abs (double lo)) (Math/abs (double hi)))]
      [(double lo) (double hi)])))

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
        ;; Anchored at zero, a value is read by how far it is from
        ;; zero, which is what makes the ink proportional in both
        ;; directions. `:from-zero` beside a log scale is refused, so
        ;; the two transforms never compose.
        from-zero? (boolean (:from-zero spec))
        ->space  (cond
                   log?       #(Math/log10 (max 1e-300 (double %)))
                   from-zero? #(Math/abs (double %))
                   :else      double)
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
      ;; Halfway across the domain, through the same method as every
      ;; other value. Taking the midpoint of the range instead skipped
      ;; `:by`, so a constant column and a nearly-constant one were
      ;; drawn by different arithmetic and jumped between them.
      (constantly (place 0.5))
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

(defn whole-number?
  "True when `x` is a number with nothing after the decimal point."
  [x]
  (and (number? x) (== (double x) (Math/rint (double x)))))

(defn whole-number-ticks
  "Ticks at whole-number multiples across `[lo hi]`, targeting about `n`
   of them, or nil where fewer than two fit.

   The step is a 1-2-5 multiple of a power of ten, never below one, and
   the one whose tick count comes closest to `n` is taken. Taking the
   largest step that stays under `n` instead left a domain of 0.8 to 5.2
   ticked 2 and 4, where 1 2 3 4 5 is what the axis is asking for.

   The steps are tried from the smallest that could give about ten times
   `n` ticks, not from one. A candidate set is built by counting up from
   `lo`, so trying step one whatever the span meant enumerating every
   whole number in the domain: `[0 1e6]` took a tenth of a second to
   answer with six ticks and `[0 1e9]` did not answer at all. No
   candidate that could win is skipped -- a step giving ten times the
   ticks asked for is already far past the best."
  [[lo hi] n]
  (let [lo (double lo)
        hi (double hi)
        n (max 2 (or n 5))
        span (- hi lo)]
    (when (and (Double/isFinite lo) (Double/isFinite hi) (pos? span))
      (let [at (fn [step]
                 (let [start (* step (Math/ceil (/ lo step)))]
                   (vec (take-while #(<= % (+ hi 1e-9))
                                    (iterate #(+ % step) start)))))
            p-lo (max 0 (long (Math/floor (Math/log10 (max 1.0 (/ span (* 10.0 n)))))))
            candidates (for [p (range p-lo (+ p-lo 13))
                             m [1 2 5]
                             :let [step (* m (Math/pow 10.0 p))
                                   ts (at step)]
                             :when (>= (count ts) 2)]
                         {:step step :ticks ts :off (Math/abs (- (count ts) n))})]
        (:ticks (first (sort-by (juxt :off :step) candidates)))))))

(defn linear-ticks
  "The ticks a linear axis draws across `[lo hi]`: wadogo's, or whole
   numbers where the values the axis reads are whole and wadogo's would
   be fractional.

   wadogo picks the step from the span and the count asked for, so an
   axis over four whole values asked for ten ticks is given halves:
   `[0 1 2 3]` was ticked 0.0, 0.5, 1.0 ... 3.0. A reader takes a tick
   at 1.5 to mean the data has a value there.

   `whole?` is whether every value the extent came from is a whole
   number, which the caller knows and this cannot see: the domain
   reaching here has been padded, so a whole 40 to 60 arrives as 39 to
   61 and a fractional 4.3 to 7.9 arrives as 4.12 to 8.08.

   Not ggplot2's rule. `scales::extended_breaks` honours the count it
   is given as wadogo does, so ggplot2 draws 1, 1.5, 2, 2.5, 3 on the
   integers 1 to 3 (measured, 4.0.0); it lands on whole numbers for
   `[0 1 2 3]` because 0-1-2-3 scores well there, not because the
   algorithm prefers them."
  [scale n whole?]
  (let [ts (vec (ws/ticks scale n))]
    (if (and whole? (some #(and (number? %) (not (whole-number? %))) ts))
      (or (whole-number-ticks (ws/domain scale) n) ts)
      ts)))

(defn ticks-inside-domain
  "The ticks of `ticks` that fall within `[lo hi]`.

   One rule, read in two places: `log-ticks` scores a candidate set by
   how many of its ticks this keeps, and `log-ticks-drawn` draws
   exactly those. A tick outside the domain is drawn outside the panel,
   so a candidate set is only as dense as the part of it that survives.
   A non-numeric bound keeps every tick."
  [ticks [lo hi]]
  (if-not (and (number? lo) (number? hi))
    (vec ticks)
    (filterv #(<= (double lo) (double %) (double hi)) ticks)))

(defn log-ticks
  "Generate clean log-scale tick values for a [lo hi] domain, targeting
   approximately n ticks. Uses ggplot2-style 1-2-5 nice numbers instead
   of wadogo's linear-in-log-space approach (which produces irrational
   values like 3.162...). Returns a vector of tick values (doubles).

   Strategy:
   - Powers of 10 only when at least three of them fall inside the
     domain (strongly preferred)
   - 1-2-5 intermediates per decade when more ticks are needed
   - 1-2-3-5 intermediates for dense sub-decade ranges
   - Bounding powers of 10 are included when they fall within a small
     margin (15% of log-span) of the domain edges

   A candidate set is counted by the ticks `ticks-inside-domain` keeps,
   not by the ticks it offers. The bounding-power margin puts breaks
   just outside the domain and those are not drawn, so counting them
   let a 2.5-decade axis choose powers of ten and then draw two of
   them: on gapminder's gross domestic product per capita, 214 to
   63951, the four powers 100 1000 10000 100000 were returned whatever
   `n` asked for, and 1000 and 10000 were all that reached the panel.
   Asked for seven, the same domain now gives 500 1000 2000 5000 10000
   20000 50000."
  [[lo hi] n]
  (let [lo (max (double lo) 1e-300)
        hi (max (double hi) lo)
        log-lo-f (Math/log10 lo)
        log-hi-f (Math/log10 hi)
        log-span (- log-hi-f log-lo-f)
        margin (* 0.15 (max log-span 0.5))
        log-lo-i (long (Math/floor log-lo-f))
        log-hi-i (long (Math/ceil log-hi-f))
        drawn-count (fn [ticks] (count (ticks-inside-domain ticks [lo hi])))
        ;; Powers of 10 with margin (catches nearby bounding powers)
        powers (vec (sort (for [exp (range log-lo-i (inc log-hi-i))
                                :let [v (Math/pow 10.0 exp)]
                                :when (and (>= (double exp) (- log-lo-f margin))
                                           (<= (double exp) (+ log-hi-f margin)))]
                            v)))]
    ;; Strongly prefer powers of 10 — use them if >= 3 are drawn
    (if (>= (drawn-count powers) 3)
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
            candidates [{:breaks powers :cnt (drawn-count powers)}
                        {:breaks breaks-125 :cnt (drawn-count breaks-125)}
                        {:breaks breaks-1235 :cnt (drawn-count breaks-1235)}]
            score (fn [{:keys [cnt]}]
                    (let [diff (- cnt n)]
                      (if (neg? diff) (* 2.0 (Math/abs diff)) (double diff))))
            best (apply min-key score candidates)]
        (:breaks best)))))

(defn log-ticks-drawn
  "The ticks a log scale draws across `[lo hi]`, and whether they are
   the 1-2-5 breaks or the fallback.

   The breaks that fall inside the domain, normally. A domain narrower
   than one 1-2-5 step has fewer than two of them inside, and there the
   ticks are spaced across the domain itself instead -- 6 to 9 is
   ticked 6, 7, 8, 9. ggplot2 answers such a domain the same way round,
   by picking breaks inside the range rather than by filtering.

   One function because an axis, a size legend and a gradient bar all
   ask it, and they used to answer three ways. The axis kept every
   break including the ones outside: a domain of 6 to 9 drew its only
   tick, 10, sixty drawing units above a four-hundred-unit canvas, and
   7 to 8 drew no ticks at all, while the legends beside them stood the
   domain's own ends in.

   `:nice?` says the fallback fired, which is what tells an axis to
   label the values with the linear formatter. `format-log-ticks` reads
   the number of decimals off each value's own magnitude, which is
   right for a 1-2-5 break and turns 0.006, 0.0065, 0.007 into 0.006,
   0.007, 0.007."
  [[lo hi] n whole?]
  (let [inside (ticks-inside-domain (log-ticks [lo hi] n) [lo hi])]
    (if (>= (count inside) 2)
      {:values (vec inside) :nice? false}
      {:values (vec (linear-ticks (ws/scale :linear {:domain [lo hi]
                                                     :range [0.0 1.0]})
                                  n whole?))
       :nice? true})))

(def ^:private calendar-steps
  "The steps a date axis is allowed to take, coarsest unit first and
   largest step first within each unit.

   These are the steps a reader counts in: decades and half-decades on
   a century, quarters and half-years on a few years, weeks and
   fortnights on a season, quarter-hours on an hour. A step outside the
   list -- three years, eleven minutes -- lands the ticks on values
   nobody reads a date off."
  [[:years   [1000 500 250 200 100 50 25 20 10 5 2 1]]
   [:months  [6 3 2 1]]
   [:days    [14 7 2 1]]
   [:hours   [12 6 3 2 1]]
   [:minutes [30 15 10 5 2 1]]
   [:seconds [30 15 10 5 2 1]]])

(defn- step-amount [unit step]
  (case unit
    :years (jt/years step) :months (jt/months step) :days (jt/days step)
    :hours (jt/hours step) :minutes (jt/minutes step) :seconds (jt/seconds step)))

(defn- first-aligned
  "The earliest tick at or after `t` that sits on a multiple of `step`
   `unit` -- a year divisible by the step, the first of such a month,
   midnight of such a day, and so on.

   Aligning is the half of this that wadogo does not do. Its generator
   starts one unit after the truncated first value, so a five-year step
   over 1938 to 1971 begins at 1939 and lands on 1944 and 1949; the
   step was round and the phase was not."
  [^java.time.LocalDateTime t unit step]
  (let [floor-div (fn [^long a ^long b] (long (Math/floor (/ (double a) (double b)))))
        c (case unit
            :years   (jt/local-date-time (* step (floor-div (.getYear t) step)) 1 1 0 0)
            :months  (let [m (* step (floor-div (+ (* 12 (.getYear t))
                                                   (dec (.getMonthValue t)))
                                                step))]
                       ;; `floor-div` again rather than `quot`: a month
                       ;; index before year 1 is negative, and `quot`
                       ;; rounds it towards zero, which names the wrong
                       ;; year for it.
                       (jt/local-date-time (floor-div m 12) (inc (mod m 12)) 1 0 0))
            ;; Epoch day 0 is a Thursday, so a plain multiple of seven
            ;; ticks Thursdays. A week is read from its Monday, which
            ;; the offset of three puts the multiples on. A step of one
            ;; day is unchanged by the offset; a step of two has its
            ;; parity turned over by it, so a January axis is ticked on
            ;; the 2nd, 4th and 6th rather than the 1st, 3rd and 5th --
            ;; which is the phase ggplot2 picks there too.
            :days    (jt/local-date-time
                      (java.time.LocalDate/ofEpochDay
                       (- (* step (floor-div (+ 3 (.toEpochDay (.toLocalDate t))) step)) 3))
                      (jt/local-time 0))
            :hours   (jt/plus (.truncatedTo t java.time.temporal.ChronoUnit/DAYS)
                              (jt/hours (* step (floor-div (.getHour t) step))))
            :minutes (jt/plus (.truncatedTo t java.time.temporal.ChronoUnit/HOURS)
                              (jt/minutes (* step (floor-div (.getMinute t) step))))
            :seconds (jt/plus (.truncatedTo t java.time.temporal.ChronoUnit/MINUTES)
                              (jt/seconds (* step (floor-div (.getSecond t) step)))))]
    (if (jt/before? c t) (jt/plus c (step-amount unit step)) c)))

(defn- aligned-ticks
  "Every multiple of `step` `unit` from `start` to `end` inclusive."
  [start end unit step]
  (let [amount (step-amount unit step)]
    (loop [t (first-aligned start unit step) acc []]
      (if (or (jt/after? t end) (> (count acc) 500))
        acc
        (recur (jt/plus t amount) (conj acc t))))))

(defn date-ticks
  "The ticks a date axis draws between `start` and `end`, given as
   `LocalDateTime`: multiples of a step a reader counts in, as near to
   `n` of them as the calendar allows.

   Both halves matter. wadogo divides the span by the count asked for
   and floors the result into whole units, which gives steps of three
   years and eleven minutes; and it phases the ticks from the first
   value rather than from the step, so 1938 to 1971 was ticked 1939,
   1943, 1947 where a reader expects 1940, 1950. Choosing from
   `calendar-steps` fixes the first and `first-aligned` the second.

   Candidates are ranked by how near their count comes to `n`, then by
   the coarser unit, then by the larger step -- so a span that two
   candidates fit equally well is ticked in the larger, rounder one.
   Returns nil where no step gives two ticks, which leaves the caller
   with wadogo's answer."
  [start end n]
  (->> (for [[i [unit steps]] (map-indexed vector calendar-steps)
             [j step] (map-indexed vector steps)
             :let [ticks (aligned-ticks start end unit step)]
             :when (>= (count ticks) 2)]
         {:ticks ticks
          :rank [(Math/abs (- (count ticks) (long n))) i j]})
       (sort-by :rank)
       first
       :ticks))

(defn tick-count
  "How many ticks an axis asks for across `pixel-range` drawing units.

   `:n-ticks` on the spec names the count outright. Failing that,
   `spacing` names about how much room a tick should have and the count
   is how many fit, never fewer than two. The count is then rounded to
   a step a reader can read off, which is why the room each tick ends
   up with can come out under `spacing`. One answer for both column
   types -- each key used to be read on one of them and ignored on the
   other."
  [pixel-range scale-spec spacing]
  (if-let [n (:n-ticks scale-spec)]
    n
    (max 2 (int (/ pixel-range spacing)))))
