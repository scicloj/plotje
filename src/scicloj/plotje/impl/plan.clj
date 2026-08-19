(ns scicloj.plotje.impl.plan
  "Draft-to-plan pipeline: domains, ticks, legends, layout, and grid inference.
   Takes draft maps (from pose/leaf->draft) and produces a Plan record
   with all geometry needed for rendering."
  (:require [wadogo.scale :as ws]
            [java-time.api :as jt]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [tech.v3.datatype.casting :as casting]
            [clojure.string :as str]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.impl.stat :as stat]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.impl.coord :as coord]
            [scicloj.plotje.impl.position :as position]
            [scicloj.plotje.impl.extract :as extract]
            [scicloj.plotje.impl.layout :as layout]
            [scicloj.plotje.impl.text :as text]
            [scicloj.plotje.impl.plan-schema :as ss]
            [scicloj.plotje.layer-type :as layer-type]))

;; ---- Domain Helpers ----

(defn collect-domain
  "Collect and merge domains from stat results along axis-key.
   Throws if some stat results contribute numeric domains and others
   contribute categorical domains -- mixing the two on one axis is
   ambiguous.

   `padding` is the resolved `:domain-padding`."
  [stat-results axis-key scale-spec padding]
  (let [parsed (keep (fn [sr]
                       (when-let [d (axis-key sr)]
                         {:vals (if (and (= 2 (count d)) (number? (first d)))
                                  d
                                  (mapv str d))
                          :numeric? (and (= 2 (count d)) (number? (first d)))}))
                     stat-results)
        types (distinct (map :numeric? parsed))]
    (when (seq parsed)
      (when (> (count types) 1)
        (throw (ex-info (str "Cannot merge numeric and categorical domains on " axis-key
                             ". Each layer must use a consistent column type for this axis.")
                        {:axis axis-key
                         :domains (mapv :vals parsed)})))
      (let [vals (mapcat :vals parsed)]
        (if (number? (first vals))
          (scale/pad-domain [(reduce min vals) (reduce max vals)] scale-spec padding)
          (distinct vals))))))

(defn compute-global-y-domain
  "Compute global y-domain from position-adjusted layers.
   Reads pre-computed :y0/:y1 from stacked layers. Extends domain to
   include 0 for marks that draw from a zero baseline (bar, area,
   lollipop) on linear scales. On log scales, baseline
   extension is skipped -- the lower bound is the smallest positive
   value the layers report -- because log scales have no zero.

   `padding` is the resolved `:domain-padding`."
  [plan-layers scale-spec padding]
  (let [fill-layers (filter #(= :fill (:position %)) plan-layers)
        stack-layers (filter #(= :stack (:position %)) plan-layers)
        log? (= :log (:type scale-spec))
        ;; Marks whose visual identity anchors at y=0 (rectangle base, fill
        ;; baseline, lollipop stem). :rect is the categorical-bar mark (from
        ;; lay-bar, whether counting or using y heights); :bar is the
        ;; histogram mark.
        zero-baseline-marks #{:bar :rect :lollipop :area}
        needs-zero? (and (not log?)
                         (some #(zero-baseline-marks (:mark %)) plan-layers))
        extend-to-zero (fn [[lo hi]] [(min 0.0 (double lo)) (max 0.0 (double hi))])]
    (cond
      ;; Fill mode: normalized to [0, 1]
      (seq fill-layers)
      [0.0 1.0]

      ;; Stack mode: read pre-computed y0/y1 values from adjusted layers
      (seq stack-layers)
      (let [;; Stacked rect: collect all y0 and y1 values
            rect-vals (for [l stack-layers
                            :when (:categories l)
                            g (:groups l)
                            {:keys [y0 y1]} (:counts g)
                            v [y0 y1]
                            :when v]
                        v)
            ;; Stacked area: all ys and y0s (already accumulated)
            area-vals (for [l stack-layers
                            :when (and (not (:categories l)) (:groups l))
                            g (:groups l)
                            y (concat (:ys g) (or (:y0s g) []))]
                        y)
            ;; Other (non-stacked) layers: use their y-domain
            other-yd (mapcat (fn [l]
                               (when-not (#{:stack :fill} (:position l))
                                 (:y-domain l)))
                             plan-layers)
            ;; Include 0 for the stacked-bar baseline on linear scales.
            ;; Log scales have no zero -- skip the injection and rely on
            ;; the data's own positive values for the lower bound.
            baseline-vals (if log? [] [0])
            raw-vals (concat rect-vals area-vals other-yd baseline-vals)
            all-vals (if log? (filter pos? raw-vals) raw-vals)]
        (if (seq all-vals)
          (let [lo (double (reduce min all-vals))
                hi (double (reduce max all-vals))]
            (if (< lo hi)
              (scale/pad-domain [lo hi] scale-spec padding)
              [(if log? 1.0 0.0) (if log? 10.0 1.0)]))
          [(if log? 1.0 0.0) (if log? 10.0 1.0)]))

      ;; Normal: collect y-domains from layers
      :else
      (let [all-yds (keep :y-domain plan-layers)
            vals (mapcat (fn [d]
                           (if (and (= 2 (count d)) (number? (first d)))
                             d (map str d)))
                         all-yds)]
        (when (seq vals)
          (if (number? (first vals))
            (let [raw-lo (reduce min vals)
                  raw-hi (reduce max vals)]
              (if needs-zero?
                ;; Baseline marks: include zero, only pad away from zero.
                (let [lo (min 0.0 raw-lo)
                      hi (max 0.0 raw-hi)
                      [plo phi] (scale/pad-domain [lo hi] scale-spec padding)]
                  [(if (>= raw-lo 0.0) 0.0 plo)
                   (if (<= raw-hi 0.0) 0.0 phi)])
                (scale/pad-domain [raw-lo raw-hi] scale-spec padding)))
            (distinct vals)))))))

;; ---- Tick Computation ----

(defn- merge-temporal-extents
  "Merge temporal extents from multiple draft layers into a single [min max] pair."
  [extents]
  (let [extents (remove nil? extents)]
    (when (seq extents)
      [(apply jt/min (map first extents))
       (apply jt/max (map second extents))])))

(defn- warn-out-of-range-breaks!
  "When user-supplied :breaks include values outside the [lo hi]
   numeric domain, the corresponding ticks render off-panel and the
   chart appears unlabelled. Print a warning naming the offending
   values and suggesting :domain (which extends the axis range) for
   users who meant to broaden the visible scale rather than pin
   tick locations only. Strict mode upgrades to ex-info."
  [breaks domain]
  (let [[lo hi] domain]
    (when (and (number? lo) (number? hi))
      (let [out-of-range (vec (filter #(or (< (double %) (double lo))
                                           (> (double %) (double hi)))
                                      breaks))]
        (when (seq out-of-range)
          (let [strict-val (:strict (defaults/config))
                msg (str "pj/scale :breaks " (vec breaks)
                         " include value(s) " out-of-range
                         " outside the data domain [" (double lo) " " (double hi)
                         "]. Out-of-range tick(s) render off the panel."
                         " To extend the axis to encompass these values,"
                         " set :domain explicitly (e.g. {:domain [lo hi]}).")]
            (when-not (or (nil? strict-val) (boolean? strict-val))
              (throw (ex-info (str ":strict config value must be true or false, got: "
                                   (pr-str strict-val))
                              {:value strict-val})))
            (if strict-val
              (throw (ex-info msg
                              {:caller "pj/plan"
                               :breaks (vec breaks)
                               :domain [lo hi]
                               :out-of-range out-of-range}))
              (println (str "Warning: " msg)))))))))

(defn- warn-unknown-categorical-breaks!
  "When user-supplied :breaks on a categorical axis include values that
   are not among the categories, those ticks have nowhere to render.
   Print a warning naming the offending values (breaks are matched to
   categories by their displayed label). Strict mode upgrades to ex-info."
  [unknown domain]
  (when (seq unknown)
    (let [strict-val (:strict (defaults/config))
          shown (vec (take 12 (map defaults/fmt-category-label domain)))
          msg (str "pj/scale :breaks " (vec unknown)
                   " include value(s) not among the axis categories "
                   shown (when (> (count domain) 12) " ...")
                   ". Those ticks are dropped. Breaks select which categories"
                   " get a tick and are matched by their displayed label.")]
      (when-not (or (nil? strict-val) (boolean? strict-val))
        (throw (ex-info (str ":strict config value must be true or false, got: "
                             (pr-str strict-val))
                        {:value strict-val})))
      (if strict-val
        (throw (ex-info msg
                        {:caller "pj/plan"
                         :unknown (vec unknown)
                         :categories (vec domain)}))
        (println (str "Warning: " msg))))))

(defn- warn-categorical-tick-spacing!
  "Warn when a scale spec asks a categorical axis for a tick spacing.

   A numeric axis chooses its ticks, and a spacing tells that choice
   about how much room a tick should have. A categorical axis has no such
   choice to steer -- its ticks are its categories -- so `:n-ticks` is
   what thins them. Written in a spec the key would otherwise do
   nothing, which is the reading this release exists to remove; the
   plot option of the same name is left alone, since it steers
   whichever axes on the plot are numeric."
  [scale-spec]
  (when (contains? scale-spec :tick-spacing)
    (println (str "Warning: :tick-spacing " (pr-str (:tick-spacing scale-spec))
                  " on a categorical axis places no ticks. The ticks of a"
                  " categorical axis are its categories; :n-ticks thins"
                  " them to about that many, and :breaks names the ones"
                  " to keep."))))

(defn compute-ticks
  "Compute tick values and labels for a domain+pixel range, using wadogo transiently.
   When temporal-extent is provided (a [min max] pair of temporal objects),
   uses wadogo :datetime scale for calendar-aware ticks and formatting.
   When `scale-spec` contains `:breaks` (a vector of numbers), those
   exact values are used as ticks instead of the auto-computed ones
   -- ggplot2's `scale_*_continuous(breaks = ...)` equivalent. When
   `scale-spec` also contains `:tick-labels` (a vector of strings), those
   replace the auto-formatted labels at the corresponding break
   positions.

   On a categorical axis, `:breaks` selects which categories get a tick
   (ggplot2's discrete `breaks`): each break is matched to a category by
   its displayed label, unmatched breaks are dropped with a warning, and
   `:tick-labels` relabels the kept ticks. Explicit `:breaks` take precedence
   over `:n-ticks` -- when both are given, the exact breaks win and no
   thinning is applied."
  ([domain pixel-range scale-spec spacing]
   (compute-ticks domain pixel-range scale-spec spacing nil nil))
  ([domain pixel-range scale-spec spacing temporal-extent]
   (compute-ticks domain pixel-range scale-spec spacing temporal-extent nil))
  ([domain pixel-range scale-spec spacing temporal-extent separators]
   (if (scale/categorical-domain? domain)
     (let [user-breaks (:breaks scale-spec)
           user-labels (:tick-labels scale-spec)]
       (if (and user-breaks (sequential? user-breaks) (seq user-breaks))
         ;; Explicit category subset -- :breaks wins over :n-ticks. Match
         ;; each break to a category by displayed label, keep them in the
         ;; user's order, and relabel with :tick-labels when given.
         (let [display->cat (into {} (map (juxt defaults/fmt-category-label identity)
                                          domain))
               break-labels (if (and user-labels (sequential? user-labels))
                              (mapv str user-labels)
                              (mapv defaults/fmt-category-label user-breaks))
               matched? (fn [b] (contains? display->cat (defaults/fmt-category-label b)))
               pairs (map vector user-breaks break-labels)]
           (warn-unknown-categorical-breaks! (remove matched? user-breaks) domain)
           (let [kept (filter (fn [[b _]] (matched? b)) pairs)]
             {:values (mapv (fn [[b _]] (display->cat (defaults/fmt-category-label b))) kept)
              :labels (mapv second kept)
              :categorical? true}))
         (let [s (scale/make-scale domain pixel-range scale-spec)
               ;; The ticks of a categorical axis are its categories,
               ;; and `:n-ticks` thins them. A tick spacing steers the
               ;; algorithm that picks numeric ticks, and there is no
               ;; such algorithm here -- see `warn-categorical-tick-spacing!`.
               ticks (if-let [n (:n-ticks scale-spec)]
                       (ws/ticks s n)
                       (ws/ticks s))]
           (warn-categorical-tick-spacing! scale-spec)
           {:values (vec ticks)
            :labels (mapv defaults/fmt-category-label ticks)
            :categorical? true})))
     (let [n (scale/tick-count (Math/abs (double (- (second pixel-range) (first pixel-range))))
                               scale-spec spacing)
           log? (= :log (:type scale-spec))
           user-breaks (:breaks scale-spec)
           user-labels (:tick-labels scale-spec)]
       (cond
         ;; User-supplied breaks override everything — use the exact values
         ;; they asked for. Labels come from user-supplied :tick-labels when
         ;; provided, otherwise from the same format the scale uses.
         (and user-breaks (sequential? user-breaks) (seq user-breaks))
         (let [vs (vec user-breaks)
               _ (warn-out-of-range-breaks! vs domain)
               labels (cond
                        (and user-labels (sequential? user-labels))
                        (mapv str user-labels)
                        log?
                        (vec (scale/format-log-ticks vs))
                        :else
                        (let [s (scale/make-scale domain pixel-range scale-spec)]
                          (vec (scale/format-ticks s vs separators))))]
           {:values vs :labels labels :categorical? false})

         temporal-extent
         ;; Temporal: use wadogo :datetime scale for calendar-aware ticks
         (if (= (first temporal-extent) (second temporal-extent))
           ;; Single-value temporal domain: one tick at the single value
           {:values [(first domain)] :labels [(str (first temporal-extent))] :categorical? false}
           (let [dt-scale (ws/scale :datetime {:domain temporal-extent :range [0.0 1.0]})
                 dt-ticks (ws/ticks dt-scale n)
                 labels (vec (ws/format dt-scale dt-ticks))
                 values (mapv resolve/temporal->epoch-ms dt-ticks)]
             {:values values :labels labels :categorical? false}))

         log?
         ;; Log: use ggplot2-style 1-2-5 nice breaks
         (let [ticks (scale/log-ticks domain n)
               labels (scale/format-log-ticks ticks)]
           {:values (vec ticks) :labels (vec labels) :categorical? false})

         :else
         ;; Linear: use wadogo
         (let [s (scale/make-scale domain pixel-range scale-spec)
               ticks (ws/ticks s n)
               labels (scale/format-ticks s ticks separators)]
           {:values (vec ticks) :labels (vec labels) :categorical? false}))))))

;; ---- Per-Panel Resolution ----

(def ^:private channel->scale-keyword
  "Channel keyword to the resolved-layer key holding its scale spec.
   The same table `pj/scale` writes through, minus `:shape`, whose
   scale carries symbols rather than a `:type` this function reads."
  (dissoc defaults/channel->scale-key :shape))

(defn- log-scaled-cols
  "Return [[channel column-name] ...] for every channel in `rv` whose
   scale is {:type :log} and whose data column reference resolves to a
   keyword in the dataset."
  [rv]
  (vec
   (for [[ch scale-key] channel->scale-keyword
         :let [col (get rv ch)]
         :when (and (= :log (:type (get rv scale-key)))
                    (keyword? col))]
     [ch col])))

(defn- filter-log-nonpositive
  "Filter rows with non-positive values on log-scaled channels.
   When any of :x-scale / :y-scale / :size-scale / :alpha-scale /
   :fill-scale / :color-scale is {:type :log}, removes rows where the
   corresponding column has values <= 0 and prints a warning. Throws a
   clear error if log scale is applied to non-numeric data.
   Returns the resolved draft layer with filtered :data."
  [rv]
  (let [ds (:data rv)]
    (if-not (tc/dataset? ds)
      rv
      (let [pairs (filter (fn [[_ col]] (ds col)) (log-scaled-cols rv))
            _ (doseq [[ch col] pairs]
                (when-not (casting/numeric-type? (dtype/elemwise-datatype (ds col)))
                  (throw (ex-info (str "Log scale on " ch " requires numeric data, but column "
                                       col " is non-numeric.")
                                  {:channel ch :column col
                                   :type (dtype/elemwise-datatype (ds col))}))))
            n-before (tc/row-count ds)
            ds (reduce (fn [ds [_ col]]
                         (tc/select-rows ds (dfn/> (ds col) 0)))
                       ds
                       pairs)
            n-after (tc/row-count ds)
            removed (- n-before n-after)]
        (when (pos? removed)
          (let [where (str/join " and " (map (fn [[_ col]] (str col)) pairs))]
            (println (str "Warning: Removed " removed " rows containing non-positive values (log scale on " where ")."))))
        (if (pos? removed)
          (assoc rv :data ds)
          rv)))))

(defn- numeric-col-ref
  "If the value at `k` in resolved draft layer `rv` is a column ref that exists in
   `ds` and has a numeric dtype, return the resolved column name; else nil.
   For :x and :y, a user-declared `:x-type :categorical` / `:y-type :categorical`
   suppresses the numeric treatment -- otherwise packed temporal columns
   (e.g. :packed-local-date) report as numeric and trip later finite? checks."
  [rv ds k]
  (let [v (get rv k)
        type-key (case k :x :x-type :y :y-type nil)
        declared-cat? (and type-key (= :categorical (get rv type-key)))]
    (when (and v (resolve/column-ref? v) (not declared-cat?))
      (let [col (resolve/resolve-col-name ds v)]
        (when (and col (ds col)
                   (casting/numeric-type? (dtype/elemwise-datatype (ds col))))
          col)))))

(defn- aesthetic-col
  "Look up an aesthetic column from a resolved draft layer's :data.
   Returns nil when the draft-layer has no :data, no value at `k`, or
   the column name does not literally match a dataset column."
  [draft-layer k]
  (let [ds (:data draft-layer)
        col-name (get draft-layer k)]
    (when (and ds col-name)
      (get ds col-name))))

(defn- filter-infinities
  "Filter rows containing non-finite values on numeric x/y and numeric
   aesthetic columns (color/size/alpha/ymin/ymax/fill). Removes rows where
   any of these columns contain nil, NaN, Inf, or -Inf and prints an
   appropriate warning. Non-numeric and non-referenced columns are skipped.
   Returns the resolved draft layer with filtered :data."
  [rv]
  (let [ds (:data rv)]
    (if-not (tc/dataset? ds)
      rv
      (let [numeric-cols (distinct
                          (keep #(numeric-col-ref rv ds %)
                                defaults/numeric-aesthetic-keys))
            n-before (tc/row-count ds)
            ;; First pass: drop missing (nil)
            ds (if (seq numeric-cols)
                 (tc/drop-missing ds numeric-cols)
                 ds)
            n-after-missing (tc/row-count ds)
            n-missing (- n-before n-after-missing)
            ;; Second pass: drop NaN/Inf on each numeric column
            ds (reduce (fn [ds col]
                         (tc/select-rows ds (dfn/finite? (ds col))))
                       ds
                       numeric-cols)
            n-after (tc/row-count ds)
            n-infinite (- n-after-missing n-after)
            removed (+ n-missing n-infinite)]
        (when (pos? removed)
          (let [parts (cond-> []
                        (pos? n-missing) (conj (str n-missing " missing (nil/NaN)"))
                        (pos? n-infinite) (conj (str n-infinite " non-finite (Inf/-Inf)")))]
            (println (str "Warning: Removed " removed " rows with non-finite values: "
                          (str/join ", " parts) "."))))
        (if (pos? removed)
          (assoc rv :data ds)
          rv)))))

(defn- order-groups-by-categories
  "Put a layer's drawn groups in the order the categories were settled
   in -- the order the legend lists them, and the order a `:domain`
   asked for.

   Without this the stat's own grouping decides where a group is
   stacked or dodged while the legend is built from `all-colors`, so a
   `:domain` moved the legend rows and left the marks where they were.
   One step here rather than one per mark, because every mark answers
   the same question.

   Matched on the formatted label, which is what a group carries by the
   time it is drawn; `all-colors` holds the raw values. A group whose
   label names no category keeps its place at the end."
  [layer all-colors]
  (let [order (when (seq all-colors)
                (into {} (map-indexed (fn [i c] [(defaults/fmt-category-label c) i])
                                      all-colors)))]
    (if (and order (seq (:groups layer)))
      (update layer :groups
              #(vec (sort-by (fn [g] (get order (:label g) Long/MAX_VALUE)) %)))
      layer)))

(defn resolve-panel-draft-layers
  "Resolve draft layers and compute stats for a group of draft layers belonging to one panel.
   If pre-resolved draft layers are provided, skips resolve-draft-layer.
   `:shape-map` is the plot-wide category-to-symbol assignment, carried
   onto every layer so the marks draw the symbols the legend advertises.
   Returns {:resolved [...] :stat-results [...] :layers [...]}."
  [panel-draft-layers all-colors cfg & {:keys [resolved shape-map]}]
  (let [resolved (or resolved (mapv (comp filter-log-nonpositive filter-infinities resolve/resolve-draft-layer) panel-draft-layers))
        stat-results (mapv #(stat/compute-stat (assoc % :cfg (merge cfg (:cfg %)))) resolved)
        raw-plan-layers (vec (map (fn [rv sr]
                                    (-> (resolve/map->PlanLayer (extract/extract-layer rv sr all-colors cfg))
                                        (assoc :y-domain (:y-domain sr)
                                               :x-domain (:x-domain sr))
                                        (cond-> shape-map (assoc :shape-map shape-map))
                                        ;; A drawing-unit offset is carried whole rather than
                                        ;; folded into the positions: it is not a data value, so
                                        ;; it must not reach the domains, and every mark shifts
                                        ;; by it the same way. The renderer applies it once.
                                        (cond-> (:offset-x rv) (assoc :offset-x (:offset-x rv))
                                                (:offset-y rv) (assoc :offset-y (:offset-y rv))
                                                (:in rv) (assoc :in (:in rv))
                                                ;; Carried for the same reason as :in --
                                                ;; the renderer measures an unscaled axis
                                                ;; from the panel corner rather than
                                                ;; through the scale, one axis at a time.
                                                (:x-drawn? rv) (assoc :x-drawn? true)
                                                (:y-drawn? rv) (assoc :y-drawn? true))))
                                  resolved stat-results))
        ordered (mapv #(order-groups-by-categories % all-colors) raw-plan-layers)
        plan-layers (position/apply-positions ordered)]
    {:resolved resolved :stat-results stat-results :layers plan-layers}))

(defn- scaled-color-column?
  "True of a resolved draft layer whose `:color` names a column that
   passes through the color scale.

   A column drawn as it stands holds colors, not categories: it takes
   no palette entry and explains no scale, so it belongs in neither
   the category list nor the legend title. Left in, its values took
   palette slots away from a scaled layer beside it -- so a two-layer
   plot drew its categories in the wrong colors -- and earned legend
   rows pairing `#00FF00` with the palette blue that drew nothing."
  [resolved-layer]
  (and (resolve/column-ref? (:color resolved-layer))
       (not (:color-drawn? resolved-layer))))

(defn- categorical-domain
  "The order a `:domain` on a scale spec supplies for a categorical
   aesthetic.

   Reached only where the column's type has already settled that the
   aesthetic is categorical: `collect-colors` calls it inside its
   `when-not numeric-color?` branch, and `:shape` accepts no other
   type. So the column's type decides how a written `:domain` is read
   and the domain's own shape decides nothing -- the same rule
   `axis-domain` states and follows.

   One key carries both readings, because `:color` can be drawn either
   way: on a numeric column it reads `[lo hi]` as the ends of a
   gradient, which is `scale/numeric-color-domain`, reached from the
   other branch of that same `numeric-color?` test.

   Testing the domain's shape here instead was a second rule for one
   question. It discarded `{:domain [2 1]}` on a column of exactly two
   numeric categories, without a message, while `:x` honoured the same
   domain on the same column."
  [spec]
  (let [d (:domain spec)]
    (when (and (sequential? d) (seq d))
      d)))

(defn- category-label
  "How a category is matched against an entry of a written `:domain`.

   By the text the category is drawn with, so a column of numbers read
   as categories can be ordered by those numbers as well as by the text
   they become: an axis holds `\"4\"` where the column held `4`, and
   both should name the same band."
  [v]
  (defaults/fmt-category-label v))

(defn- order-by-domain
  "Order `observed` categories by an explicit `domain`. Domain entries
   come first, in the order given; observed categories the domain omits
   follow in the order they appear in the data, so nothing silently
   loses its place. `warn-category-domain-gap!` reports the omissions.

   The values answered are always the observed ones. The marks carry
   the column's own representation and are placed against these, so
   answering the written domain's would leave a scale whose categories
   no mark matches."
  [observed domain]
  (let [by-label (into {} (map (juxt category-label identity) (reverse observed)))
        listed (vec (distinct (keep (comp by-label category-label) domain)))
        listed-set (set listed)]
    (into listed (remove listed-set observed))))

(defn- warn-category-domain-gap!
  "Warn when a `pj/scale` `:domain` leaves out categories the data
   contains. Those categories still get drawn -- appended after the
   listed ones -- but the user asked for an order that does not cover
   them, which is usually a typo or a stale category list."
  [aesthetic observed domain]
  (let [listed (set (map category-label domain))]
    (when-let [missing (seq (remove (comp listed category-label) observed))]
      (println (str "Warning: pj/scale " aesthetic " :domain omits " (vec missing)
                    ". Those categories are still drawn, ordered after the "
                    "listed ones. List every category to control the whole "
                    "legend order.")))))

(defn- axis-domain
  "The domain an axis is drawn against, given its scale spec and what
   the data covers.

   The column's type decides how a written `:domain` is read, not the
   written domain's own shape. Against a categorical column it supplies
   the order of the categories, as it does for `:color` and `:shape`;
   against a continuous one it replaces the range outright, which is
   how a view window is set.

   Reading the shape instead made `{:domain [4 5 6 8]}` on a
   categorical axis look like a numeric range: the axis was built as a
   linear scale, and every mark -- text by the time it got there --
   died casting inside it. A temporal axis is unaffected either way,
   since its domain reaches here as epoch numbers."
  [aesthetic spec data-domain]
  (let [written (:domain spec)]
    (cond
      (nil? written) data-domain

      (and (some? data-domain) (scale/categorical-domain? data-domain))
      (do (warn-category-domain-gap! aesthetic data-domain written)
          (order-by-domain data-domain written))

      :else written)))

(defn- collect-colors
  "Resolve draft layers and collect color categories across all draft layers.
   Attaches :__resolved to each draft layer for downstream re-use.
   Filters infinite values and non-positive values on log-scaled axes.

   Category order follows the data unless a `:domain` on the colour
   scale supplied one, as `collect-shapes` does for `:shape`. The
   palette is assigned in this order, so the domain reorders the legend
   and the colours together.

   Returns {:resolved-all :numeric-color? :all-colors :color-cols :tagged-draft-layers}."
  [draft-layers]
  (let [resolved-all (mapv (comp filter-log-nonpositive filter-infinities resolve/resolve-draft-layer) draft-layers)
        tagged-draft-layers (mapv (fn [v rv] (assoc v :__resolved rv)) draft-layers resolved-all)
        numeric-color? (some #(= :numerical (:color-type %)) resolved-all)
        all-colors (when-not numeric-color?
                     (let [color-draft-layers (filter #(and (scaled-color-column? %)
                                                            (:data %)) resolved-all)]
                       (when (seq color-draft-layers)
                         (let [observed (vec (distinct (remove nil? (mapcat #(aesthetic-col % :color)
                                                                            color-draft-layers))))
                               domain (seq (categorical-domain
                                            (some :color-scale color-draft-layers)))]
                           (when (and domain (seq observed))
                             (warn-category-domain-gap! :color observed domain))
                           (if domain
                             (order-by-domain observed domain)
                             observed)))))
        color-cols (distinct (keep #(when (scaled-color-column? %) (:color %)) resolved-all))]
    {:resolved-all resolved-all
     :numeric-color? numeric-color?
     :all-colors all-colors
     :color-cols color-cols
     :tagged-draft-layers tagged-draft-layers}))

(defn- warn-shape-wrap!
  "Warn when there are more shape categories than symbols to draw them
   with, so two categories share a symbol and become indistinguishable."
  [all-shapes syms]
  (let [n-cats (count all-shapes)
        n-syms (count syms)]
    (when (> n-cats n-syms)
      (println (str "Warning: " n-cats " shape categories exceeds the "
                    n-syms " available shape symbols. Symbols will repeat, "
                    "so some categories cannot be told apart. Reduce the "
                    "number of categories, or supply your own symbols via "
                    "(pj/scale pose :shape {:values [...]}).")))))

(defn- collect-shapes
  "Collect the categories the `:shape` aesthetic takes across all draft
   layers and decide which symbol each one draws with. Returns nil when
   no draft layer maps `:shape` to a column.

   Category order follows the data unless `pj/scale :shape` supplied a
   `:domain`; the symbols are `defaults/shape-syms` unless that scale
   supplied `:values`. Deciding here rather than at render time is what
   lets the legend show the symbol the marks actually draw.

   Returns {:all-shapes :shape-cols :shape-map}."
  [resolved-all]
  (let [shape-draft-layers (filter #(and (resolve/column-ref? (:shape %))
                                         (:data %)) resolved-all)]
    (when (seq shape-draft-layers)
      (let [scale (some :shape-scale shape-draft-layers)
            domain (seq (categorical-domain scale))
            syms (or (seq (:values scale)) defaults/shape-syms)
            observed (vec (distinct (remove nil? (mapcat #(aesthetic-col % :shape)
                                                         shape-draft-layers))))
            all-shapes (if domain
                         (order-by-domain observed domain)
                         observed)]
        (when (seq all-shapes)
          (when domain
            (warn-category-domain-gap! :shape observed domain))
          (warn-shape-wrap! all-shapes syms)
          {:all-shapes all-shapes
           :shape-cols (distinct (keep #(when (resolve/column-ref? (:shape %)) (:shape %))
                                       resolved-all))
           :shape-map (zipmap all-shapes (cycle syms))})))))

(defn- warn-palette-wrap!
  "Warn if:
     (1) the number of color categories exceeds the resolved palette's
         size, so colors will visibly repeat;
     (2) the user passed a gradient-family palette name (`:viridis`,
         `:plasma`, `:inferno`, `:magma`, `:turbo`, `:rocket`, `:mako`,
         `:cividis`, `:RdBu`, `:RdYlBu`, `:BrBG`, `:coolwarm`) as the
         colours themselves. These are continuous gradients, not
         categorical palettes -- the user probably wanted a `:range`
         with a numeric color column; or
     (3) the user passed an explicit palette keyword that resolves
         to nothing (typo or unknown name). `resolve-palette` silently
         falls back to the default; we warn here so the user knows.

   Takes the resolved palette rather than the configuration, so it
   warns about the colours the marks are actually drawn in."
  [all-colors palette]
  (when (seq all-colors)
    (let [n-cats (count all-colors)
          resolved (when (keyword? palette) (defaults/resolve-palette palette))
          pal-size (cond
                     (map? palette) nil ;; explicit mapping — no wrap possible
                     (sequential? palette) (count palette)
                     (keyword? palette) (count resolved)
                     :else (count (defaults/resolve-palette defaults/default-palette-name)))
          gradient? (and (keyword? palette)
                         (contains? defaults/gradient-palette-keywords palette))
          unknown? (and (keyword? palette)
                        (not gradient?)
                        (= resolved (defaults/resolve-palette defaults/default-palette-name))
                        (not= palette defaults/default-palette-name))]
      (when (and pal-size (> n-cats pal-size))
        (println (str "Warning: " n-cats " color categories exceeds palette size "
                      pal-size ". Colors will repeat. Use a larger palette via "
                      ":color-values, or reduce the number of categories.")))
      (when gradient?
        (println (str "Warning: :color :values " palette " is a continuous gradient, "
                      "not a categorical palette, so the first " n-cats " colors "
                      "will look nearly identical. For continuous color mapping "
                      "use a numeric :color column with :range "
                      palette " instead. For a discrete palette, try "
                      ":set1, :dark2, or :tableau-10.")))
      (when unknown?
        (println (str "Warning: :color :values " palette " is not a known categorical "
                      "palette; using default (" defaults/default-palette-name
                      "). Try :set1, :dark2, :tableau-10, or pass an explicit "
                      "vector of hex colors."))))))

(defn- adjust-fixed-aspect
  "Adjust panel dimensions for coord :fixed so that 1 data unit = 1 data unit
   on both axes. Shrinks the larger dimension to match the data aspect ratio."
  [pw ph x-domain y-domain]
  (let [x-range (- (double (second x-domain)) (double (first x-domain)))
        y-range (- (double (second y-domain)) (double (first y-domain)))]
    (if (or (<= x-range 0) (<= y-range 0))
      {:pw pw :ph ph}
      (let [data-ratio (/ x-range y-range)
            panel-ratio (/ pw ph)]
        (if (> panel-ratio data-ratio)
          {:pw (* ph data-ratio) :ph ph}
          {:pw pw :ph (/ pw data-ratio)})))))

(defn- resolve-labels
  "Resolve effective title and axis labels.
   Title comes from opts only. An axis label is one setting written at
   two scopes: `:label` in a scale spec on a mapping or a layer, and
   the `:x-label` / `:y-label` plot options on a pose. The innermost
   wins, as it does for every other scale setting, so a spec beats an
   option; with neither, the column name is inferred."
  [x-vars y-vars x-scale-spec y-scale-spec
   title x-label y-label auto-label?]
  {:eff-title title
   :eff-x-label (or (:label x-scale-spec)
                    x-label
                    (when auto-label?
                      (when-let [x (first x-vars)] (defaults/fmt-name x))))
   :eff-y-label (or (:label y-scale-spec)
                    y-label
                    (when auto-label?
                      (when-let [y (first y-vars)]
                        (when (not= y (first x-vars))
                          (defaults/fmt-name y)))))})

(defn- finite-vals
  "Concatenate a seq of column buffers into a single Clojure vector with
   nil/NaN/Inf stripped. Returns nil when the result is empty. Used to
   compute min/max for numeric-aesthetic legends without tripping over
   missing values or boolean-typed all-nil columns."
  [bufs]
  (let [out (into [] (comp cat (filter #(and (some? %) (number? %) (Double/isFinite (double %))))) bufs)]
    (when (seq out) out)))

(def ^:private monochrome-marks
  "Marks that draw each group in one solid color and therefore cannot
   show a continuous color ramp along the mark itself. `:point` is the
   only mark that carries a per-row color; every mark listed here draws
   the default color instead, whatever a numeric `:color` column holds.
   We emit the legend anyway -- a reader can still see the range the
   column covers -- but we print a one-line warning, since the gradient
   in the bar is not painted on the mark.

   Spelled in mark names, which is what a resolved layer carries. A
   stat or a layer-type name written here would match nothing and warn
   about nothing: `:bar` is the mark under `lay-histogram` and `:rect`
   the mark under `lay-bar`."
  #{:area :bar :boxplot :errorbar :line :lollipop :pointrange
    :rect :ridgeline :rug :step :text :violin})

(defn- warn-monochrome-numeric-color!
  "Warn once per plan when a numeric :color is paired with a mark that
   draws one color per group. The column is not read as a gradient at
   all; the user may have meant `:color-type :categorical`, which
   splits the data into a group per category and gives each its own
   color."
  [resolved-all]
  (when-let [affected (seq (filter #(and (= :numerical (:color-type %))
                                         (contains? monochrome-marks (:mark %)))
                                   resolved-all))]
    (let [marks (vec (distinct (map :mark affected)))]
      (println (str "Warning: " marks " with a numeric :color draw one "
                    "color per group and do not read the column as a "
                    "gradient, though the legend shows one. Use "
                    ":color-type :categorical to give each category its "
                    "own color.")))))

(def ^:private fill-drawing-marks
  "Marks that paint an interior through the `:fill` aesthetic, and so
   read a `:fill` scale spec.

   Every other mark that computes a fill range draws through `:color`
   instead: a contour builds its levels from the `:color` scale alone,
   and `warn-fill-scale-without-fill!` tells a user who wrote `:fill`
   on one that `:color` is the key it reads. A legend is built through
   whichever of the two its marks were drawn through, so the bar and
   the marks cannot answer differently."
  #{:tile})

(defn- warn-fill-scale-without-fill!
  "Warn once when a fill scale is set but no draft layer reads `:fill`.
   Most marks paint with `:color`, not `:fill`; this catches the common
   slip of writing `(pj/scale :fill ...)` when `:color` was meant.

   A layer reads `:fill` by mapping a column to it, and a tile reads it
   whether or not one was mapped -- the `:bin2d` and `:density-2d`
   stats compute the fill themselves, and the scale still decides the
   gradient and the domain those tiles are drawn against.

   Read off the draft layers rather than off `:opts`: `pj/scale` writes
   the mapping now, so the `:fill-scale` key this looked for stopped
   being written and the warning stopped firing. The generic
   orphan-mapping warning still caught the case, but without the
   fill-versus-colour guidance that is the whole point of this one."
  [resolved-all _opts]
  (when (and (some (defaults/channel->scale-key :fill) resolved-all)
             (not-any? #(or (:fill %) (contains? fill-drawing-marks (:mark %)))
                       resolved-all))
    (println "Warning: pj/scale :fill set but no descendant layer uses"
             ":fill -- did you mean :color? :fill paints interior of"
             "tile/density-2d/bin2d marks; :color paints stroke or"
             "outline (point edge, line).")))

(defn- continuous-legend-ticks
  "Tick values for a log-scaled gradient bar, each with the fraction of
   the bar it sits at.

   A linear gradient bar is labelled at its two ends and needs none of
   this. A log one has to say where the decades fall, or the bar cannot
   be read back to a value.

   Stated once because both continuous legends need it: the fill legend
   had it and the colour legend did not, so `pj/scale :color :log`
   spaced the marks and left the legend describing a linear scale."
  [lo hi]
  (let [lo-l (Math/log10 (max 1e-300 (double lo)))
        hi-l (Math/log10 (max 1e-300 (double hi)))
        span (max 1e-6 (- hi-l lo-l))]
    (vec (for [v (scale/log-ticks [lo hi] 5)]
           {:value v
            :t (/ (- (Math/log10 (max 1e-300 (double v))) lo-l) span)}))))

(defn- gradient-stops
  "The colours along a continuous legend's bar, low end first.

   `:t` is the place on the bar, evenly spaced. `:gradient-t` is the
   place in the gradient a value there is drawn at -- the same number
   `normalize-continuous` gives the marks -- so the bar cannot show a
   colour no mark on the panel can take.

   The two agree wherever the domain covers the whole gradient, which
   is every scale without a midpoint, `:log` among them. A midpoint
   parts them: the gradient is centred on the midpoint and reaches its
   extremes at the further of the two domain ends, so the nearer end
   stops short, and the bar stops there with it."
  [grad-fn scale-type lo hi midpoint]
  (let [n 20
        lo-t (defaults/normalize-continuous scale-type lo lo hi midpoint)
        hi-t (defaults/normalize-continuous scale-type hi lo hi midpoint)]
    (vec (for [i (range n)
               :let [t (/ (double i) (dec n))
                     g (+ lo-t (* t (- hi-t lo-t)))]]
           {:t t :gradient-t g :color (grad-fn g)}))))

(defn- build-legend
  "Build legend from resolved draft layers and color info. Returns nil when the
   legend would be empty (no data, or all nil/NaN in the color column).
   `opts-title` overrides the inferred column-name title (from a
   user-supplied `:color-label` plot option).

   A color column that is drawn as it stands earns no legend, the way
   ggplot2's `scale_colour_identity()` defaults to none. A legend
   explains a scale by pairing each category with the color chosen for
   it; where the value already is the color there is no choice to
   explain, and rows reading `#FF0000` beside a red dot tell a reader
   nothing they cannot see. `collect-colors` has already left
   those layers out of `all-colors` and `color-cols`, so a plot whose
   every color column is drawn arrives here with nothing to list --
   and a plot that mixes the two legends only the scaled half, rather
   than all of it or none."
  [resolved-all numeric-color? all-colors color-cols cfg opts-title]
  (let [title (or opts-title (first color-cols))]
    (cond
      numeric-color?
      (let [color-draft-layers (filter #(and (scaled-color-column? %)
                                             (:data %)) resolved-all)
            all-bufs (map #(aesthetic-col % :color) color-draft-layers)]
        (when-let [all-vals (finite-vals all-bufs)]
          (let [spec (some :color-scale color-draft-layers)
                ;; Through the same function the marks read, so the bar
                ;; a reader matches a colour against spans what the
                ;; marks were drawn against.
                grad-fn (defaults/scale-gradient-fn :color spec cfg)
                [c-min c-max] (scale/numeric-color-domain
                               spec
                               (dfn/reduce-min all-vals)
                               (dfn/reduce-max all-vals))
                scale-type (or (some #(:type (:color-scale %)) color-draft-layers)
                               :linear)
                ;; The midpoint the marks were drawn around, read the
                ;; way they read it, so the bar is centred where they
                ;; are.
                midpoint (defaults/scale-setting :color :midpoint spec cfg)]
            (cond-> {:title title
                     :type :continuous
                     :min c-min :max c-max
                     :scale-type scale-type
                     :color-range (defaults/scale-setting :color :range spec cfg)
                     :range-from-spec? (contains? spec :range)
                     :stops (gradient-stops grad-fn scale-type c-min c-max midpoint)}
              (= :log scale-type)
              (assoc :ticks (continuous-legend-ticks c-min c-max))))))
      (seq all-colors)
      {:title title
       :entries (vec (for [cat all-colors]
                       {:label (defaults/fmt-category-label cat)
                        :color (defaults/color-for
                                all-colors cat
                                (defaults/scale-setting
                                 :color :values
                                 (some :color-scale resolved-all) cfg))}))})))

(defn- nice-legend-values
  "Generate ~n nicely-rounded tick-like values spanning [lo, hi].
   Delegates to wadogo's linear scale so the breaks are 1/2/5-aligned
   (e.g., [17, 83] → [20 40 60 80] rather than [17.0 33.5 50.0 66.5 83.0]).
   Falls back to evenly-spaced rounded values when wadogo returns fewer
   than two ticks."
  [lo hi n]
  (let [lo (double lo) hi (double hi)]
    (if (= lo hi)
      [lo]
      (let [s (ws/scale :linear {:domain [lo hi] :range [0.0 1.0]})
            nice (vec (ws/ticks s n))]
        (if (>= (count nice) 2)
          nice
          ;; Fallback: evenly-spaced with enough decimals to distinguish
          ;; adjacent values. Preserves backward-compatible behavior for
          ;; pathological inputs (tiny spans, NaN-ish).
          (let [step (/ (- hi lo) (dec n))
                decimals (if (pos? step)
                           (min 6 (max 1 (+ 1 (long (Math/ceil (- (Math/log10 step)))))))
                           1)
                factor (Math/pow 10.0 decimals)]
            (mapv (fn [i]
                    (let [v (+ lo (* i step))]
                      (/ (Math/round (* v factor)) factor)))
                  (range n))))))))

(def ^:private channel-mapper
  "The legend's half of the size and alpha mapping. `render.mark` draws
   the marks from the same function, which is why it lives in
   `impl.scale` rather than here."
  scale/channel-mapper)

(defn- continuous-channel-ticks
  "Pick `n` tick values across [d-min, d-max] for a continuous channel
   legend. Log scale uses the shared 1-2-5 log-tick generator; linear
   falls through to nice-legend-values."
  [scale-type d-min d-max n]
  (if (= scale-type :log)
    (vec (scale/log-ticks [d-min d-max] n))
    (nice-legend-values d-min d-max n)))

(defn- channel-quantity
  "The quantity a draft layer's mark draws `channel` as, and how ink
   grows with it, as `[quantity ink-exponent]`."
  [draft-layer channel]
  [(or (layer-type/mark-varies (:mark draft-layer) channel)
       (layer-type/default-quantities channel))
   (layer-type/ink-exponent (:mark draft-layer) channel)])

(def ^:private per-row-buffer-keys
  "Where a plan layer carries the per-row values for an appearance
   channel, keyed by channel. What `warn-undrawn-varies!` looks for to
   tell a declaration that is drawn from one that is only made."
  {:size :sizes :alpha :alphas})

(defn- warn-undrawn-varies!
  "Warn when a mark declares that it varies a channel from row to row,
   a column is mapped to that channel, and the layer carries no per-row
   values for it.

   The declaration is what earns the legend and silences the
   nothing-on-this-plot-varies warning, so a mark that declares one and
   draws a single value for the whole layer produces a plot advertising
   an encoding it does not apply. `register!` checks that the layer
   types sharing a mark agree with each other; only the extracted layer
   can say whether the mark carries the values through to the renderer.

   Reported once per mark and channel, however many panels drew it."
  [panel-data]
  (let [drawn? (fn [layer buffer-key]
                 (some (fn [g] (let [b (get g buffer-key)]
                                 (and (some? b) (pos? (count b)))))
                       (:groups layer)))
        offenders (for [pd panel-data
                        [rv layer] (map vector (:resolved pd) (:layers pd))
                        [channel buffer-key] per-row-buffer-keys
                        :when (and (resolve/column-ref? (get rv channel))
                                   (:data rv)
                                   (seq (:groups layer))
                                   (layer-type/mark-varies (:mark rv) channel)
                                   (not (drawn? layer buffer-key)))]
                    [(:mark rv) channel])]
    (doseq [[mark channel] (distinct offenders)]
      (println (str "Warning: the " mark " mark declares that it varies "
                    channel " from row to row, and drew no per-row "
                    (name channel) " values. Its legend explains an encoding"
                    " the panel does not show. Either draw " channel
                    " per row in the mark's extractor, or take " channel
                    " out of the layer type's :varies.")))))

(defn- reads-per-row?
  "Whether `mark` varies `channel` from row to row -- the registry's
   `:varies` declaration, asked per mark.

   Every mark that does not declare the channel draws one value for the
   whole layer: `:line` takes one stroke width, `:boxplot` one opacity.
   A column there varies nothing, because the extractor never reads the
   buffer -- so the request passed every check and changed not one
   pixel, while the plot still grew a legend explaining the encoding it
   did not apply.

   This was a closed table here until the marks declared it, and a mark
   the table had never heard of answered no. An extension that varied
   size per row was warned about and denied its legend while drawing
   correctly, with no way to say otherwise."
  [channel mark]
  (some? (layer-type/mark-varies mark channel)))

(defn- thin-to
  "At most `k` of `vs`, evenly spaced, keeping the first and the last.

   The tick generators answer with nicely-rounded values rather than
   with exactly the count they are asked for -- wadogo reads `n` as a
   target and returns whatever the 1-2-5 step gives -- so a hard limit
   has to be applied after the fact."
  [vs k]
  (let [n (count vs)]
    (if (or (<= n k) (< k 2))
      (vec vs)
      (mapv #(nth vs %)
            (distinct (map #(long (Math/round (* (/ (double %) (dec k))
                                                 (dec n))))
                           (range k)))))))

(defn- size-legend-rows-that-fit
  "How many rows of swatches the canvas has room for at this range.

   While the range was fixed at 2 to 8 every row was eighteen tall and
   the question never arose -- the tick generator's seven rows fit any
   plot worth drawing. A range the writer widens makes each row as
   tall as the mark it draws, and seven rows of forty-two ran off the
   bottom of the canvas. The tick generator is still asked for the
   same five; this only takes rows away when they would not fit."
  [height range-hi]
  (let [row-h (max 18.0 (+ 2.0 (* 2.0 (double range-hi))))
        ;; What is left of the canvas under the title, the axis and the
        ;; legends above this one -- measured against the standard
        ;; chrome rather than the layout, which has not run yet.
        room (- (double height) 120.0)]
    (max 2 (long (Math/floor (/ room row-h))))))

(defn- build-size-legend
  "Build size legend when :size maps to a numerical column. Returns nil
   when all values are nil/NaN (suppressing the legend).
   `opts-title` overrides the inferred column-name title (from a
   user-supplied `:size-label` plot option). `height` is the plot's, so
   that a wide range takes fewer rows rather than more room than there
   is."
  [resolved-all opts-title height]
  (let [size-draft-layers (filter #(and (resolve/column-ref? (:size %))
                                        (nil? (:fixed-size %))
                                        ;; A column drawn as it stands
                                        ;; passed through no scale, so
                                        ;; there is no scale to explain.
                                        (not (:size-drawn? %))
                                        ;; And a mark that draws one
                                        ;; radius for the layer never
                                        ;; read the column, so a legend
                                        ;; here would pair values with
                                        ;; radii the panel does not draw.
                                        (reads-per-row? :size (:mark %))
                                        (:data %)) resolved-all)]
    (when (seq size-draft-layers)
      (let [size-col (:size (first size-draft-layers))
            spec (:size-scale (first size-draft-layers))
            scale-type (or (:type spec) :linear)
            [quantity ink-exponent] (channel-quantity (first size-draft-layers) :size)
            all-bufs (map #(aesthetic-col % :size) size-draft-layers)]
        (when-let [all-vals (finite-vals all-bufs)]
          (let [s-min (dfn/reduce-min all-vals)
                s-max (dfn/reduce-max all-vals)
                ;; The values the legend labels span the scale's own
                ;; domain, not the data's, so a `:domain` that widens
                ;; what a size means is what the reader is told.
                [dom-lo dom-hi] (scale/channel-domain spec s-min s-max)
                [_ range-hi] (or (:range spec) (:size defaults/channel-ranges))
                values (thin-to (continuous-channel-ticks
                                 scale-type dom-lo dom-hi 5)
                                (size-legend-rows-that-fit height range-hi))
                magnitude-fn (channel-mapper spec s-min s-max
                                             (:size defaults/channel-ranges)
                                             ink-exponent)]
            {:title (or opts-title size-col)
             :type :size
             :min s-min :max s-max
             :scale-type scale-type
             ;; What the mark draws the channel as, so the swatch beside
             ;; a value is the shape the panel varies. Graduated circles
             ;; beside a width encoding have the reader comparing
             ;; diameters while the panel shows thicknesses.
             :quantity quantity
             :swatch (:swatch (layer-type/quantities quantity))
             ;; A row whose swatch has no size explains nothing, and
             ;; `:from-zero` puts one there: the domain starts at zero
             ;; and zero draws nothing, so the legend read "0" beside
             ;; an empty space.
             :entries (vec (for [v values
                                 :let [magnitude (magnitude-fn v)]
                                 :when (> magnitude 0.001)]
                             {:value v
                              :magnitude magnitude}))}))))))

(defn- build-alpha-legend
  "Build alpha legend when :alpha maps to a numerical column. Returns nil
   when all values are nil/NaN (suppressing the legend).
   `opts-title` overrides the inferred column-name title (from a
   user-supplied `:alpha-label` plot option)."
  [resolved-all opts-title]
  (let [alpha-draft-layers (filter #(and (resolve/column-ref? (:alpha %))
                                         (nil? (:fixed-alpha %))
                                         (not (:alpha-drawn? %))
                                         ;; As with `:size`: no legend for
                                         ;; a channel the mark never varied.
                                         (reads-per-row? :alpha (:mark %))
                                         (:data %)) resolved-all)]
    (when (seq alpha-draft-layers)
      (let [alpha-col (:alpha (first alpha-draft-layers))
            spec (:alpha-scale (first alpha-draft-layers))
            scale-type (or (:type spec) :linear)
            [_ ink-exponent] (channel-quantity (first alpha-draft-layers) :alpha)
            all-bufs (map #(aesthetic-col % :alpha) alpha-draft-layers)]
        (when-let [all-vals (finite-vals all-bufs)]
          (let [a-min (dfn/reduce-min all-vals)
                a-max (dfn/reduce-max all-vals)
                [dom-lo dom-hi] (scale/channel-domain spec a-min a-max)
                values (continuous-channel-ticks scale-type dom-lo dom-hi 5)
                alpha-fn (channel-mapper spec a-min a-max
                                         (:alpha defaults/channel-ranges)
                                         ink-exponent)]
            {:title (or opts-title alpha-col)
             :type :alpha
             :min a-min :max a-max
             :scale-type scale-type
             ;; A row drawn at no opacity explains nothing, as a row
             ;; with no size does not: `:from-zero` anchors the domain
             ;; at zero, and a value of zero is drawn invisible.
             :entries (vec (for [v values
                                 :let [alpha (alpha-fn v)]
                                 :when (> alpha 0.001)]
                             {:value v
                              :alpha alpha}))}))))))

(defn- build-shape-legend
  "Build the shape legend from the category-to-symbol assignment
   `collect-shapes` already made. Returns nil when no draft layer maps
   `:shape` to a column.
   `opts-title` overrides the inferred column-name title (from a
   user-supplied `:shape-label` plot option)."
  [{:keys [all-shapes shape-cols shape-map]} opts-title]
  (when (seq all-shapes)
    {:title (or opts-title (first shape-cols))
     :type :shape
     :entries (vec (for [cat all-shapes]
                     {:label (defaults/fmt-category-label cat)
                      :shape (shape-map cat)}))}))

(defn- merge-shape-into-color-legend
  "When one column drives both `:color` and `:shape`, the two legends
   would repeat the same categories under the same title. Fold the
   symbols into the color legend's entries instead, so each key draws
   its own symbol in its own color, and drop the separate shape legend.
   This is what ggplot2 does with matching guides.

   Merges only when both legends carry the same title and the same
   labels in the same order -- a user who titled one of them with a
   `:label` of its own asked for two distinct legends.

   Returns [legend shape-legend], one of which may be nil."
  [legend shape-legend]
  (if (and (:entries legend)
           (:entries shape-legend)
           (= (:title legend) (:title shape-legend))
           (= (mapv :label (:entries legend))
              (mapv :label (:entries shape-legend))))
    [(update legend :entries
             (fn [entries]
               (mapv (fn [entry shape-entry]
                       (assoc entry :shape (:shape shape-entry)))
                     entries (:entries shape-legend))))
     nil]
    [legend shape-legend]))

;; ---- Main Entry Point ----

(def ^:private polar-supported-marks
  "Marks that render correctly under polar coordinates."
  #{:point :bar :rect :text :rug})

(defn- validate-polar-marks
  "Check that all resolved draft layers use marks compatible with polar coordinates.
   Throws an ex-info with details when an unsupported mark is found."
  [resolved-draft-layers coord-type]
  (when (= coord-type :polar)
    (doseq [v resolved-draft-layers
            :let [m (:mark v)]
            :when (and m (not (polar-supported-marks m)))]
      (throw (ex-info (str "Mark :" (name m) " is not supported with polar coordinates. "
                           "Supported polar marks: " (sort polar-supported-marks))
                      {:mark m :supported polar-supported-marks})))))

(def ^:private drawn-axis-marks
  "The marks that place through the panel's `coord-fn`, and so measure
   an axis told not to scale in drawing units.

   The rest read the oriented scales (`sx` / `sy`) directly, so the
   request never reaches them: `:bar` drew full-height bars and
   `:boxplot` drew an empty panel, both in silence. Deriving this from
   the renderer is not possible at plan time, so it is a list -- a new
   mark that places through the oriented scales belongs on the other
   side of it, and the test below renders both sides to keep the list
   honest."
  #{:area :contour :errorbar :line :point :pointrange :rug :step :text :tile})

(defn- validate-unscaled-axis-marks
  "Refuse a request to measure an axis in drawing units on a mark that
   cannot honour one, by either route.

   Both routes reach the same place. `{:in :drawing-area}` says it of
   the whole layer and a per-axis `{:scale false}` says it of one, and
   a mark that reads the oriented scales directly ignores either: a
   bar drew one rectangle across half the panel and a histogram drew
   an empty panel, both without a word. The whole-layer route used to
   die on `Number.doubleValue() because \"x\" is null` instead, so
   refusing it costs no working plot -- and leaving it drawing while
   refusing the per-axis form would be the same request answered two
   ways in one release."
  [resolved-draft-layers]
  (doseq [v resolved-draft-layers
          :let [whole-layer? (= :drawing-area (:in v))
                axes (cond-> []
                       (or whole-layer? (:x-drawn? v)) (conj :x)
                       (or whole-layer? (:y-drawn? v)) (conj :y))
                m (:mark v)]
          :when (and (seq axes) m (not (drawn-axis-marks m)))]
    (throw (ex-info (str (if whole-layer?
                           "{:in :drawing-area} places a whole layer in drawing units"
                           (str "A :scale false on " (str/join " and " axes)
                                " measures in drawing units"))
                         " from the top left of the panel background, and the "
                         m " mark places through the axis scales instead, so it"
                         " cannot read one. The marks that can: "
                         (str/join ", " (sort drawn-axis-marks)) "."
                         " A " m " layer is placed by its data.")
                    {:mark m :axes axes :in (:in v) :supported drawn-axis-marks}))))

(defn- validate-drawn-channel-marks
  "Refuse a column told not to scale on a mark that draws one value for
   the whole layer.

   `{:size {:column :r :scale false}}` asks for the column's own values
   as radii -- ggplot2's `scale_size_identity()`. A mark that draws one
   radius has none to give, and answered by drawing exactly what it
   drew before: `pj/save` of a lollipop with and without the option
   produced byte-identical PNGs.

   This is the appearance twin of `validate-unscaled-axis-marks`, and
   is refused rather than warned for the same reason: the spelling is
   new, so no plot can break, and it asks for something the mark cannot
   do rather than something it merely ignores."
  [resolved-draft-layers]
  (doseq [v resolved-draft-layers
          channel [:size :alpha]
          :let [m (:mark v)
                drawn? (get v (keyword (str (name channel) "-drawn?")))]
          :when (and m drawn? (not (reads-per-row? channel m)))]
    (throw (ex-info (str "A :scale false on " channel " draws the column's own"
                         " values, one per row, and the " m " mark draws one "
                         (name channel) " for the whole layer, so it cannot read"
                         " one. The marks that can: "
                         (str/join ", " (sort (layer-type/marks-varying channel)))
                         ". For one " (name channel) " over the layer, write the"
                         " value itself.")
                    {:mark m :channel channel
                     :supported (layer-type/marks-varying channel)}))))

(defn- warn-unread-channel-columns
  "Warn when a channel names a column and no mark in the plot varies it.

   The scaled spelling, unlike the drawn one above, has been accepted
   since before 0.8.1, so refusing it would break plots that draw --
   badly, but they draw. What it must not do is stay silent: the layer
   ignored the column and the plot grew a legend for it, so the picture
   advertised an encoding it did not contain.

   Asked of the whole plot rather than of each layer, because a column
   mapped on the pose flows into every layer: a point layer beside a
   line layer is the case the channel exists for, and warning about the
   line there would be noise. The legend is suppressed per layer --
   see `build-size-legend`."
  [resolved-all]
  (doseq [channel [:size :alpha]
          :let [named (filter #(and (resolve/column-ref? (get % channel))
                                    (:data %))
                              resolved-all)
                marks (into #{} (keep :mark named))]
          :when (and (seq named) (not-any? #(reads-per-row? channel %) marks))]
    (println (str "Warning: " channel " names the column "
                  (pr-str (get (first named) channel))
                  ", and no mark on this plot varies " (name channel)
                  " from row to row"
                  (when (seq marks) (str " -- " (str/join ", " (sort marks))))
                  ". The marks that do: "
                  (str/join ", " (sort (layer-type/marks-varying channel)))
                  ". For one " (name channel) " over the layer, write the value"
                  " itself; the column is ignored and earns no legend."))))

(defn- validate-unscaled-axis-coord
  "Refuse a per-axis `:scale false` under a coord that rearranges the
   axes.

   An unscaled `:x` or `:y` is a distance measured from the panel
   background's top left, which names one screen direction. `:coord
   :flip` swaps which screen direction the mapping's `:y` reaches and
   `:polar` gives it no straight direction at all, so the distance no
   longer says where the mark goes -- and the renderer drew every mark
   into one corner rather than saying so.

   Which screen direction an unscaled axis should mean once the coord
   has moved it is an open question, not an oversight. Refusing keeps
   it open. The whole-layer `{:in :drawing-area}` does not face it:
   both of its coordinates are drawing units from the same corner, so
   there is no axis to follow through the swap."
  [resolved-draft-layers coord-type]
  (when (contains? #{:flip :polar} coord-type)
    (doseq [v resolved-draft-layers
            :let [axes (cond-> []
                         (:x-drawn? v) (conj :x)
                         (:y-drawn? v) (conj :y))]
            :when (seq axes)]
      (throw (ex-info (str "A :scale false on " (str/join " and " axes)
                           " measures in drawing units from the top left of the"
                           " panel background, and :coord " coord-type
                           " moves that axis elsewhere, so the two cannot be"
                           " combined. Either drop the :coord, or place the whole"
                           " layer with {:in :drawing-area}, whose two coordinates"
                           " are drawing units from that corner whatever the coord.")
                      {:coord coord-type :axes axes})))))

(defn- describe-spec
  "A scale spec as it should read in a message. A layer that names none
   still reads a scale -- the default one -- and printing `nil` for it
   said nothing useful."
  [spec]
  (if (nil? spec) "the default" (pr-str spec)))

(defn- validate-axis-spec-agreement
  "Refuse two layers of one panel that name different scales for an
   axis.

   A panel has one x axis and one y axis, and every layer is drawn
   against them, so the panel can carry only one spec per axis. A layer
   that names none is no disagreement -- it is drawn against whichever
   axis the panel has -- which is where this differs from the
   appearance channels below, where each layer scales its own values.

   Nothing could reach this before a mapping could name an axis scale:
   the spec came from the pose's options, so every layer carried the
   same one."
  [draft-layers]
  (doseq [[channel scale-key] [[:x :x-scale] [:y :y-scale]]
          :let [named (distinct (keep scale-key draft-layers))]
          :when (> (count named) 1)]
    (throw (ex-info (str "Layers name different scales for the " channel
                         " axis: " (pr-str (vec named)) ". A panel has one "
                         (name channel) " axis, and every layer is drawn"
                         " against it, so it can carry only one. Set it once"
                         " with (pj/scale pose " channel " ...), or write the"
                         " same :scale in each mapping.")
                    {:channel channel :specs (vec named)}))))

(defn- validate-channel-spec-agreement
  "Refuse two layers that read one appearance channel through different
   scales, or draw it as different quantities.

   One legend explains one scale. Two layers whose sizes run through a
   log scale and a linear one need two, and so do a point layer and a
   width-varying layer sharing a size column -- the legend can draw
   circles or strokes, not both. Until a plot can carry two legends for
   one channel, drawing the picture would mean labelling one of the two
   encodings wrongly.

   A layer that names no scale counts as naming the default, unlike on
   an axis: each layer here scales its own values, so a layer left on
   the default really is drawn through a different scale.

   Counting as the default means being compared as it, though. A layer
   that writes the type the default already has agrees with a layer
   that writes nothing, so the two are resolved to the same spec before
   they are compared -- otherwise `{:scale :linear}` beside a layer left
   alone was refused for disagreeing with itself.

   Only layers that read the channel are asked, and what counts as
   reading it differs. `:size` and `:alpha` are read by the marks that
   declare they vary them: a column mapped on the pose flows into every
   layer, and a mark that draws one size for the whole layer has no
   scale to disagree about. `:color` and `:fill` have no such
   declaration -- a mark that paints at all paints through the palette
   or the gradient -- so every layer mapping a scaled column to them is
   asked instead.

   The two colour aesthetics went unchecked until now, so the first
   layer's scale silently decided for both and a log scale written on
   the second changed nothing."
  [draft-layers]
  (doseq [[channel reads?] [[:size #(and (resolve/column-ref? (:size %))
                                         (reads-per-row? :size (:mark %)))]
                            [:alpha #(and (resolve/column-ref? (:alpha %))
                                          (reads-per-row? :alpha (:mark %)))]
                            [:color scaled-color-column?]
                            [:fill #(resolve/column-ref? (:fill %))]]
          :let [scale-key (defaults/channel->scale-key channel)
                varying (filter #(and (reads? %) (:data %)) draft-layers)
                resolve-spec #(merge {:type (defaults/default-scale-type channel)}
                                     (get % scale-key))
                specs (distinct (map resolve-spec varying))
                drawn-as (distinct (map #(layer-type/mark-varies (:mark %) channel)
                                        varying))]]
    (when (> (count specs) 1)
      (throw (ex-info (str "Layers read " channel " through different scales: "
                           (str/join ", " (map describe-spec specs))
                           ". One legend explains one"
                           " scale, so the plot cannot say which of the two a"
                           " mark's " (name channel) " means. Set the scale"
                           " once with (pj/scale pose " channel " ...), or"
                           " write the same :scale in each mapping.")
                      {:channel channel :specs (vec specs)})))
    (when (> (count drawn-as) 1)
      (throw (ex-info (str "Layers draw " channel " as different quantities: "
                           (pr-str (vec drawn-as)) ", so one legend cannot"
                           " explain both -- a swatch is a circle or a stroke,"
                           " not both at once. Map " channel " on the layer"
                           " that should carry it rather than on the pose.")
                      {:channel channel :quantities (vec drawn-as)})))))

(defn- warn-conflicting-specs
  "Warn when draft layers disagree about the coord.

   The axis scales were warned about here too, and using the first
   layer's; they are refused by `validate-axis-spec-agreement` now that
   a mapping can name one."
  [draft-layers]
  (let [coords (distinct (keep :coord draft-layers))]
    (when (> (count coords) 1)
      (println (str "Warning: Layers have conflicting coord types " (vec coords)
                    ". Using first layer's coord: " (first coords) ".")))))

;; ================================================================
;; Grid Layout
;; ================================================================

(defn- infer-grid
  "Infer grid structure from panel groups (one panel per group).
   Grid position determined by:
   - Faceted draft layers: :facet-col -> grid col, :facet-row -> grid row
   - Non-faceted draft layers: :x column -> grid col, :y column -> grid row
   Returns {:grid-cols N :grid-rows N :panels [{:draft-layers [...] :row R :col C ...}]}"
  [panel-groups {:keys [grid-cols grid-rows] :as user-grid}]
  (let [;; Detect faceting: check if any panel group has :facet-col or :facet-row
        first-draft-layers (mapv #(first (:draft-layers %)) panel-groups)
        has-facet-col? (some :facet-col first-draft-layers)
        has-facet-row? (some :facet-row first-draft-layers)
        faceted? (or has-facet-col? has-facet-row?)

        ;; Collect grid axis values
        col-vals (if faceted?
                   (vec (distinct (keep :facet-col first-draft-layers)))
                   (or grid-cols (vec (distinct (map :x first-draft-layers)))))
        row-vals (if faceted?
                   (vec (distinct (keep :facet-row first-draft-layers)))
                   (or grid-rows (vec (distinct (map :y first-draft-layers)))))
        ;; For non-faceted with single x/y, use nil sentinels
        col-vals (if (empty? col-vals) [nil] col-vals)
        row-vals (if (empty? row-vals) [nil] row-vals)

        ;; Position each panel
        stack-counts (atom {})
        panels (vec
                (for [pg panel-groups
                      :let [v (first (:draft-layers pg))
                            ;; Determine grid position
                            [ci col-label]
                            (if has-facet-col?
                              (let [fc (:facet-col v)
                                    i (.indexOf ^java.util.List col-vals fc)]
                                [(max 0 i) (str fc)])
                              (let [xv (:x v)
                                    i (.indexOf ^java.util.List col-vals xv)
                                    ;; Only show col-label when multiple columns exist
                                    ;; (avoids redundant x-label on single-column layouts)
                                    show-label? (> (count col-vals) 1)]
                                [(max 0 i) (when (and xv show-label?) (defaults/fmt-name xv))]))
                            [ri row-label]
                            (if has-facet-row?
                              (let [fr (:facet-row v)
                                    i (.indexOf ^java.util.List row-vals fr)]
                                [(max 0 i) (str fr)])
                              (let [yv (:y v)
                                    i (.indexOf ^java.util.List row-vals yv)
                                    ;; Only show row-label when multiple rows exist
                                    ;; (avoids redundant y-label on single-row facets)
                                    show-label? (> (count row-vals) 1)]
                                [(max 0 i) (when (and yv show-label?) (defaults/fmt-name yv))]))
                            ;; Stacking: when multiple panel groups share the same
                            ;; grid position, offset each by one row below the base.
                            ;; sub=0 -> base position; sub>0 -> stacked below.
                            stack-key [ri ci]
                            sub (get @stack-counts stack-key 0)
                            _ (swap! stack-counts update stack-key (fnil inc 0))]]
                  {:draft-layers (:draft-layers pg)
                   :row (if (> sub 0) (+ (* ri (count col-vals)) sub) ri)
                   :col ci
                   :var-x (:x v) :var-y (:y v)
                   :col-label col-label
                   :row-label row-label}))
        ;; Compute actual grid dimensions
        max-row (if (seq panels) (inc (apply max (map :row panels))) 1)
        max-col (if (seq panels) (inc (apply max (map :col panels))) 1)
        layout-type (cond
                      faceted? :facet-grid
                      (and (= 1 max-row) (= 1 max-col)) :single
                      :else :multi-variable)]
    {:grid-cols max-col
     :grid-rows max-row
     :layout-type layout-type
     :x-vars (vec (distinct (map :x first-draft-layers)))
     :y-vars (vec (distinct (map :y first-draft-layers)))
     :facet-col-vals (when has-facet-col? col-vals)
     :facet-row-vals (when has-facet-row? row-vals)
     :panels panels}))

(defn- coordinate-facet-domains
  "Replace each panel's :x-dom and/or :y-dom with the cross-panel
   aggregate so faceted layouts share scales. Numeric domains
   merge as `[min(lows), max(highs)]`; categorical domains union
   their distinct values. Controlled by `:scales` opt:
     :shared (default) -- coordinate both axes
     :free-y           -- coordinate x only (y per-panel)
     :free-x           -- coordinate y only (x per-panel)
     :free             -- no coordination (per-panel)

   Only applied when the layout is `:facet-grid` -- multi-variable
   layouts (where panels carry different x/y vars) keep per-panel
   domains because aggregating across different columns is
   meaningless."
  [panel-domains scales-opt]
  (let [scales (or scales-opt :shared)
        share-x? (#{:shared :free-y} scales)
        share-y? (#{:shared :free-x} scales)
        agg (fn [k]
              (let [doms (keep k panel-domains)]
                (when (seq doms)
                  (let [numeric? (and (seq (first doms))
                                      (number? (first (first doms))))]
                    (if numeric?
                      [(reduce min (map first doms))
                       (reduce max (map second doms))]
                      (vec (distinct (mapcat seq doms))))))))
        x-agg (when share-x? (agg :x-dom))
        y-agg (when share-y? (agg :y-dom))]
    (mapv (fn [pd]
            (cond-> pd
              x-agg (assoc :x-dom x-agg)
              y-agg (assoc :y-dom y-agg)))
          panel-domains)))

;; ---- Fitting text marks into the panel ----

;; A text mark is sized in pixels, not in data units, so a data label
;; sitting at the largest value runs past the drawing area however wide
;; the axis is -- the classic cut-off bar label. The fix is to give the
;; axis a little more data range. Everything below computes how much.

(def ^:private text-fit-slack
  "Pixels of clearance left between a fitted text mark and the panel
   edge, so a fitted label is not shaved by antialiasing and so a
   second fitting pass over an already-fitted domain finds nothing to
   do."
  1.0)

(defn- text-fit-items
  "Room the text marks among `plan-layers` need on one axis.

   Returns a seq of [value low high]: the value the mark is anchored at
   on this axis, and the pixel offsets from that anchor to the near and
   far edges of what the mark draws, measured in the direction the axis
   values increase. `value-key` names the group buffer this axis scales
   (:xs or :ys, swapped under :coord :flip) and `axis` is :x or :y --
   screen y grows downward while y data grows upward, so the y offsets
   are the negated screen ones."
  [plan-layers value-key axis]
  (for [layer plan-layers
        ;; A drawing-space text mark is placed on the panel, not in the
        ;; data, so widening the domain would not move it -- and its
        ;; position is a page measurement that has no business being read
        ;; as a data value. The same holds one axis at a time, which is
        ;; why the buffer rather than `axis` decides: `value-key` names
        ;; the mapping axis this pass measures, and `:coord :flip` has
        ;; already swapped which panel axis that lands on.
        :when (and (= :text (:mark layer))
                   (contains? #{nil :data} (:in layer))
                   (not (if (= :xs value-key) (:x-drawn? layer) (:y-drawn? layer))))
        :let [style (:style layer)
              ;; An offset moves the drawn text away from its anchor, so
              ;; the room it needs moves with it. Screen y grows downward
              ;; while y data grows upward, which is why the y offset is
              ;; negated along with the extents.
              off (double (or (if (= axis :x) (:offset-x layer) (:offset-y layer)) 0))
              off (if (= axis :x) off (- off))]
        g (:groups layer)
        :let [labels (:labels g)
              vs (value-key g)]
        :when (and labels vs)
        i (range (min (count labels) (count vs)))
        :let [v (nth vs i)]
        :when (number? v)
        :let [[left right top bottom] (text/extent style (nth labels i))
              pad text/fit-pad]]
    (if (= axis :x)
      [v (- (+ off left) pad) (+ off right pad)]
      [v (- (+ off (- bottom)) pad) (+ off (- top) pad)])))

(defn- fit-domain
  "Widen the numeric domain [d0 d1] until every item fits inside an axis
   `w` pixels long. `items` come from text-fit-items.

   Returns the domain unchanged when nothing overflows, and also when a
   single item is wider than the whole axis -- no amount of widening
   fits that one, and trying would grow the domain without bound.

   Iterates because widening lowers the pixels-per-data-unit rate, so
   the room just added is worth slightly less than it was measured at.
   Each pass only widens and only by the overflow it still measures, so
   the sequence converges from below."
  [[d0 d1] items w log?]
  (let [->space (if log? #(Math/log (double %)) double)
        <-space (if log? #(Math/exp %) identity)
        w (double w)
        items (->> items
                   (filter (fn [[v _ _]] (or (not log?) (pos? (double v)))))
                   (mapv (fn [[v low high]]
                           [(->space v)
                            (- (double low) text-fit-slack)
                            (+ (double high) text-fit-slack)])))]
    (if (or (empty? items)
            (not (pos? w))
            (some (fn [[_ low high]] (>= (- high low) w)) items))
      [d0 d1]
      (let [a0 (->space d0)
            b0 (->space d1)]
        (loop [a a0 b b0 k 0]
          (let [rate (/ w (- b a))
                px (fn [v] (* (- v a) rate))
                low-over (reduce max 0.0 (map (fn [[v low _]] (- (+ (px v) low))) items))
                high-over (reduce max 0.0 (map (fn [[v _ high]] (- (+ (px v) high) w)) items))]
            (if (or (>= k 10) (and (< low-over 0.25) (< high-over 0.25)))
              (if (and (== a a0) (== b b0))
                [d0 d1]
                [(<-space a) (<-space b)])
              (recur (- a (/ low-over rate))
                     (+ b (/ high-over rate))
                     (inc k)))))))))

(defn- fit-panel-text
  "Widen one panel's numeric domains so its text marks are drawn in
   full. Leaves alone a domain the user pinned with pj/scale, a
   categorical domain, and any panel under :coord :polar, where the
   value-to-pixel mapping is not a straight line and this arithmetic
   would not describe it."
  [pd pw ph m]
  (if (= :polar (:coord pd))
    pd
    (let [flip? (= :flip (:coord pd))
          fit (fn [pd dom-key scale-key value-key axis length]
                (let [dom (dom-key pd)
                      spec (scale-key pd)]
                  (if (or (:domain spec)
                          (not (and (sequential? dom)
                                    (= 2 (count dom))
                                    (number? (first dom)))))
                    pd
                    (assoc pd dom-key
                           (fit-domain dom
                                       (text-fit-items (:layers pd) value-key axis)
                                       (- (double length) m m)
                                       (= :log (:type spec)))))))]
      (-> pd
          (fit :x-dom :x-scale (if flip? :ys :xs) :x pw)
          (fit :y-dom :y-scale (if flip? :xs :ys) :y ph)))))

(defn- resolve-panel-domains
  "Given a panel-data map (with :stat-results, :layers, and :draft-layers),
   compute the oriented x/y domains, scale specs, and temporal extents.
   Applies the :coord :flip swap so downstream code doesn't have to.
   Does NOT compute ticks -- that happens after panel dimensions are
   known."
  [pd default-x-scale default-y-scale default-coord padding]
  (let [;; A layer placed in a drawing-space frame is on the panel, not in
        ;; the data: its numbers are drawing units, so letting them reach a
        ;; domain would stretch the axis to a page measurement. It is left
        ;; out of the domain computation and kept in the panel -- it still
        ;; has to be drawn. Stat results are index-aligned with the draft
        ;; layers they came from.
        data-space? (fn [layer] (contains? #{nil :data} (:in layer)))
        ;; A value that does not go through a scale cannot inform that
        ;; scale's domain -- it is a distance across the panel, and
        ;; letting it in would stretch the axis to a page measurement.
        ;; Asked per axis, because `{:y {:column :b :scale false}}`
        ;; leaves x scaled and only y out.
        x-informs? (fn [layer] (and (data-space? layer) (not (:x-drawn? layer))))
        y-informs? (fn [layer] (and (data-space? layer) (not (:y-drawn? layer))))
        local-plan-layers (:layers pd)
        ;; An annotation-only panel carries synthesized stat-results with
        ;; no resolved layer behind them, so the two are only pairable
        ;; when their counts agree. Where they do not, nothing is in a
        ;; drawing-space frame either, and every result counts.
        srs-for (fn [informs?]
                  (let [rs (:resolved pd)
                        srs (:stat-results pd)]
                    (if (= (count rs) (count srs))
                      (->> (map vector rs srs)
                           (filter (comp informs? first))
                           (mapv second))
                      srs)))
        local-srs (srs-for x-informs?)
        local-srs-y (srs-for y-informs?)
        domain-layers (filterv y-informs? local-plan-layers)
        first-draft-layer (first (:draft-layers pd))
        ;; The first layer that *names* a spec, not the first layer's
        ;; value. A spec can now come from a mapping, so it may sit on
        ;; the second layer while the first says nothing; taking the
        ;; first layer's value dropped it in silence. Two layers naming
        ;; different specs are refused before this runs.
        ;;
        ;; Merged over the default so the spec carries a `:type`
        ;; whatever was written: a mapping may name `:domain` alone,
        ;; and `plan-schema/ScaleSpec` requires the type.
        x-scale-spec (merge default-x-scale (some :x-scale (:draft-layers pd)))
        y-scale-spec (merge default-y-scale (some :y-scale (:draft-layers pd)))
        coord-type (or (:coord first-draft-layer) default-coord)
        ;; The same `[0 1]` fallback `y-dom` has carried all along. A
        ;; panel where nothing informs the x domain -- every layer in a
        ;; drawing-space frame, or on an unscaled x -- left `x-dom` nil,
        ;; and a nil domain is not a domain: `lay-bar`, `lay-histogram`
        ;; and `lay-interval-h` died on
        ;; `Number.doubleValue() because "x" is null`, and the marks
        ;; that survived drew no x ticks at all.
        x-dom (or (axis-domain :x x-scale-spec
                               (collect-domain local-srs :x-domain x-scale-spec padding))
                  [0 1])
        y-dom (or (axis-domain :y y-scale-spec
                               (or (compute-global-y-domain domain-layers y-scale-spec padding)
                                   ;; Annotation-only panels have no plan
                                   ;; layers; their y-domain lives in the
                                   ;; synthesized stat-results.
                                   (when (empty? domain-layers)
                                     (collect-domain local-srs-y :y-domain y-scale-spec padding))))
                  [0 1])
        ;; Whether anything on this panel gives the axis a meaning in
        ;; the data. Where nothing does, the `[0 1]` fallback above is
        ;; a coordinate function and nothing more -- drawing ticks off
        ;; it wrote numbers no mark stands for. `{:y {:column :b :scale
        ;; false}}` on values of 10, 50 and 90 drew a y axis reading
        ;; 0.0 to 1.0 labelled `b`, beside a real x axis, and nothing
        ;; about the picture looked wrong. A domain the writer set with
        ;; `pj/scale` counts as a meaning; so does a panel carrying
        ;; only annotations, whose extent lives in its stat-results.
        informed? (fn [informs? spec]
                    (boolean (or (:domain spec)
                                 (empty? (:resolved pd))
                                 (some informs? (:resolved pd)))))
        x-informed? (informed? x-informs? x-scale-spec)
        y-informed? (informed? y-informs? y-scale-spec)
        [x-dom' y-dom'] (if (= coord-type :flip)
                          [y-dom x-dom]
                          [x-dom y-dom])
        [x-informed?' y-informed?'] (if (= coord-type :flip)
                                      [y-informed? x-informed?]
                                      [x-informed? y-informed?])
        [x-sspec' y-sspec'] (if (= coord-type :flip)
                              [y-scale-spec x-scale-spec]
                              [x-scale-spec y-scale-spec])
        resolved-draft-layers (:resolved pd)
        x-temp-ext (merge-temporal-extents (map :x-temporal-extent resolved-draft-layers))
        y-temp-ext (merge-temporal-extents (map :y-temporal-extent resolved-draft-layers))
        [x-te y-te] (if (= coord-type :flip)
                      [y-temp-ext x-temp-ext]
                      [x-temp-ext y-temp-ext])]
    {:x-dom x-dom'
     :y-dom y-dom'
     :x-informed? x-informed?'
     :y-informed? y-informed?'
     :x-scale x-sspec'
     :y-scale y-sspec'
     :coord coord-type
     :x-te x-te
     :y-te y-te
     :layers (or local-plan-layers [])
     :row (:row pd)
     :col (:col pd)
     :row-label (:row-label pd)
     :col-label (:col-label pd)
     :var-x (:var-x pd)
     :var-y (:var-y pd)}))

(defn- finalize-panel
  "Given a pre-tick panel domain map and pixel dimensions, compute the
   tick sets for both axes and assemble the final panel map."
  [{:keys [x-dom y-dom x-scale y-scale coord x-te y-te
           x-informed? y-informed?
           row col row-label col-label var-x var-y]
    plan-layers :layers}
   pw ph m cfg annotations]
  (let [x-px [m (- pw m)]
        y-px [(- ph m) m]
        seps (defaults/number-separators cfg)
        ;; An axis nothing gives a data meaning gets no ticks. The
        ;; domain stays -- the coordinate function needs one -- but
        ;; numbers drawn off it would name values no mark carries.
        ;; The spec names a tick spacing where the writer set one, and
        ;; the `:x-tick-spacing` / `:y-tick-spacing` plot option names
        ;; it one scope further out.
        x-ticks (when (and x-dom x-informed?)
                  (compute-ticks x-dom x-px x-scale
                                 (defaults/scale-setting :x :tick-spacing x-scale cfg)
                                 x-te seps))
        y-ticks (when (and y-dom y-informed?)
                  (compute-ticks y-dom y-px y-scale
                                 (defaults/scale-setting :y :tick-spacing y-scale cfg)
                                 y-te seps))]
    (cond-> {:x-domain (vec (if (sequential? x-dom) x-dom [x-dom]))
             :y-domain (vec (if (sequential? y-dom) y-dom [y-dom]))
             :x-scale x-scale
             :y-scale y-scale
             :coord coord
             :x-ticks (or x-ticks {:values [] :labels [] :categorical? false})
             :y-ticks (or y-ticks {:values [] :labels [] :categorical? false})
             :layers plan-layers
             :row row
             :col col}
      (seq annotations)
      (assoc :annotations
             (let [panel-anns
                   (filterv
                    (fn [a]
                      (and (or (nil? (:x a)) (= (:x a) var-x))
                           (or (nil? (:y a)) (= (:y a) var-y))))
                    annotations)]
               (mapv #(dissoc % :facet-col :facet-row :x :y) panel-anns)))
      row-label (assoc :row-label row-label)
      col-label (assoc :col-label col-label))))

(defn- build-fill-fallback-legend
  "If no color legend was built (no :color column), check for layers
   with computed fill ranges (:bin2d, :density-2d, or identity tiles
   with :fill). Returns a continuous legend map or nil.
   When the scale the marks read is {:type :log}, the gradient
   stops sample colors in log-space and the legend carries log-spaced
   ticks for the renderer to label.
   `opts-title` (from a user-supplied `:fill-label` plot option)
   overrides the inferred title (`:count`, `:relative-density`, or
   `:fill`)."
  [panel-data resolved-all cfg opts-title]
  (let [stat-fill-range (some (fn [pd]
                                (some :fill-range (:stat-results pd)))
                              panel-data)
        stat-kind (when stat-fill-range
                    (some (fn [rv]
                            (when (#{:bin2d :density-2d} (:stat rv))
                              (:stat rv)))
                          resolved-all))
        draft-layer-fill-range (when-not stat-fill-range
                                 (some (fn [rv]
                                         (when (and (= :tile (:mark rv)) (:fill rv) (:data rv))
                                           (let [vals ((:data rv) (:fill rv))]
                                             (when (seq vals)
                                               [(dfn/reduce-min vals) (dfn/reduce-max vals)]))))
                                       resolved-all))
        [data-lo data-hi] (or stat-fill-range draft-layer-fill-range)
        ;; Which aesthetic the marks read decides which the bar reads.
        fill-mark? (boolean (some #(contains? fill-drawing-marks (:mark %))
                                  resolved-all))
        ;; One layer answers, so the gradient, the domain and the scale
        ;; type cannot arrive from three different scales.
        fill-draft-layer (or (when fill-mark?
                               (some #(when (:fill-scale %) %) resolved-all))
                             (some #(when (:color-scale %) %) resolved-all)
                             {})
        ;; Through the same resolver the marks were drawn with: a tile
        ;; reads :color as a synonym for :fill, a contour reads :color
        ;; alone. The provenance is that resolver's own answer, so
        ;; render-time configuration repaints a bar built from a plot
        ;; option and leaves one the spec decided.
        spec (if fill-mark?
               (extract/fill-spec fill-draft-layer)
               (:color-scale fill-draft-layer))
        color-range (if fill-mark?
                      (extract/fill-setting :range fill-draft-layer cfg)
                      (defaults/scale-setting :color :range spec cfg))
        midpoint (if fill-mark?
                   (extract/fill-setting :midpoint fill-draft-layer cfg)
                   (defaults/scale-setting :color :midpoint spec cfg))
        range-from-spec? (if fill-mark?
                           (extract/fill-setting-from-spec? :range fill-draft-layer cfg)
                           (= :spec (defaults/scale-setting-source spec :range)))
        scale-type (or (:type spec) :linear)
        ;; The bar spans what the marks were drawn against, through the
        ;; resolver that decided their domain, so a :domain that moved
        ;; them moves it too.
        [f-lo f-hi] (or (scale/numeric-color-domain spec data-lo data-hi)
                        [data-lo data-hi])]
    (when f-lo
      (let [grad-fn (defaults/resolve-gradient-fn color-range)
            title (or opts-title
                      (cond
                        (= stat-kind :bin2d) :count
                        (= stat-kind :density-2d) :relative-density
                        :else :fill))
            log? (= :log scale-type)
            stops (gradient-stops grad-fn scale-type f-lo f-hi midpoint)
            ticks (when log? (continuous-legend-ticks f-lo f-hi))]
        (cond-> {:title title
                 :type :continuous
                 :min f-lo :max f-hi
                 :scale-type scale-type
                 :color-range color-range
                 :range-from-spec? range-from-spec?
                 :stops stops}
          ticks (assoc :ticks ticks))))))

(defn- synthesize-annotation-domain
  "For an annotation draft layer in an annotation-only panel, compute
   a synthetic stat-result whose :x-domain / :y-domain come from the
   draft-layer's data columns plus the annotation's own position values
   (:y-intercept / :x-intercept for rules, :y-min/:y-max or
   :x-min/:x-max for bands) so the panel's axes are well-defined even
   when no data layer is attached to the panel. Falls back to [0 1] on
   the axis perpendicular to a rule (where the annotation alone supplies
   no extent) so the line still draws. Skips non-numeric columns
   (string/keyword/temporal) and nil cells so a categorical or
   partly-missing column on a pose-scope annotation-only panel doesn't
   crash the reducer."
  [{:keys [mark data x y y-intercept x-intercept y-min y-max x-min x-max]}]
  (let [col-vals (fn [col]
                   (when (and data col (tc/dataset? data))
                     (let [resolved (resolve/resolve-col-name data col)]
                       (try
                         (let [vs (seq (data resolved))
                               numeric (->> vs (remove nil?) (filter number?) seq)]
                           numeric)
                         (catch Exception _ nil)))))
        x-col-vals (col-vals x)
        y-col-vals (col-vals y)
        x-extra (case mark
                  :rule-v (when (number? x-intercept) [x-intercept])
                  :band-v (when (and (number? x-min) (number? x-max)) [x-min x-max])
                  nil)
        y-extra (case mark
                  :rule-h (when (number? y-intercept) [y-intercept])
                  :band-h (when (and (number? y-min) (number? y-max)) [y-min y-max])
                  nil)
        x-all (cond-> []
                x-col-vals (into x-col-vals)
                x-extra (into x-extra))
        y-all (cond-> []
                y-col-vals (into y-col-vals)
                y-extra (into y-extra))
        ;; The axis perpendicular to a rule has no annotation-supplied
        ;; extent. If no data column fills it either, default to [0 1]
        ;; so the line is drawable.
        x-fallback? (and (empty? x-all) (#{:rule-h :band-h} mark))
        y-fallback? (and (empty? y-all) (#{:rule-v :band-v} mark))]
    (cond-> {}
      (seq x-all) (assoc :x-domain [(reduce min x-all) (reduce max x-all)])
      (seq y-all) (assoc :y-domain [(reduce min y-all) (reduce max y-all)])
      x-fallback? (assoc :x-domain [0.0 1.0])
      y-fallback? (assoc :y-domain [0.0 1.0]))))

(defn draft->plan
  "Pipeline: convert a draft into a plan using panel-based grid layout.
   Grid position from structural columns.

   New layout pipeline (2026-04-11): stats first, then scene → padding →
   dimensions, then per-panel ticks at the now-known panel dimensions.
   `:width`/`:height` are total SVG dimensions; panel dimensions are
   derived by subtracting layout overhead. `:panel-width`/`:panel-height`
   in opts are escape hatches that pin panel size on their axis."
  ([draft] (draft->plan draft {}))
  ([draft {:keys [x-label y-label title subtitle caption
                  scales legend-position grid-cols grid-rows] :as opts}]
   (let [cfg (defaults/resolve-config opts)
         validate? (:validate cfg true)
         ;; Effective width/height: user opts override cfg. These are
         ;; total SVG dimensions under the new semantics.
         width (or (:width opts) (:width cfg))
         height (or (:height opts) (:height cfg))
         ;; Build the opts map that layout/compute-dims sees. It must
         ;; contain the effective :width, :height, and any explicit
         ;; :panel-width / :panel-height escape-hatch keys.
         layout-opts (assoc opts :width width :height height)
         draft-layers (if (map? draft) [draft] draft)

         ;; Annotations-as-layers: split annotation draft layers
         ;; (created by pj/lay-rule-*/pj/lay-band-*) from data draft
         ;; layers. Annotations skip the stat/extract-layer pipeline
         ;; and join the plot-level :annotations list merged below.
         ;; A draft-layer with only annotations (no data layer) still needs
         ;; a panel inferred for it, so annotation-only panel-idx
         ;; groups are threaded into grid inference separately.
         layer-annotations (filterv #(resolve/annotation-marks (:mark %)) draft-layers)
         draft-layers (filterv #(not (resolve/annotation-marks (:mark %))) draft-layers)
         data-panel-ids (set (map :__panel-idx draft-layers))
         ann-only-by-idx (->> layer-annotations
                              (remove #(contains? data-panel-ids (:__panel-idx %)))
                              (group-by :__panel-idx))

         ;; Group draft layers by source panel index. Annotation-only
         ;; entries get their own groups tagged :__annotation-only?
         ;; so downstream Phase 1 synthesizes their domains instead of
         ;; running the stat pipeline.
         draft-layer-groups (vec
                             (concat
                              (for [[idx vs] (sort-by key (group-by :__panel-idx draft-layers))]
                                {:panel-idx idx :draft-layers (vec vs)})
                              (for [[idx vs] (sort-by key ann-only-by-idx)]
                                {:panel-idx idx
                                 :draft-layers (vec vs)
                                 :__annotation-only? true})))

         ;; Infer grid from draft layer groups
         grid (infer-grid draft-layer-groups
                          (cond-> {}
                            grid-cols (assoc :grid-cols grid-cols)
                            grid-rows (assoc :grid-rows grid-rows)))
         {:keys [layout-type x-vars y-vars]} grid
         grid-rows-n (:grid-rows grid)
         grid-cols-n (:grid-cols grid)

         ;; Colors + warnings
         {:keys [resolved-all numeric-color? all-colors color-cols tagged-draft-layers]}
         (collect-colors draft-layers)
         _ (warn-palette-wrap! all-colors
                               (defaults/scale-setting
                                :color :values
                                (some :color-scale resolved-all) cfg))
         _ (warn-monochrome-numeric-color! resolved-all)
         _ (warn-fill-scale-without-fill! resolved-all opts)

         ;; Shape symbols. Decided here, once per plan, so every panel's
         ;; marks and the legend agree on which symbol a category draws.
         shape-info (collect-shapes resolved-all)

         ;; Representative scale/coord (first draft layer) for plot-level decisions
         default-x-scale {:type :linear}
         default-y-scale {:type :linear}
         default-coord :cartesian
         ;; As in `resolve-panel-domains`: the first layer that names a
         ;; spec, merged over the default so a `:type` is always there.
         rep-x-scale (merge default-x-scale (some :x-scale draft-layers))
         rep-y-scale (merge default-y-scale (some :y-scale draft-layers))
         rep-coord (or (:coord (first draft-layers)) default-coord)
         _ (validate-polar-marks resolved-all rep-coord)
         _ (validate-unscaled-axis-coord resolved-all rep-coord)
         _ (validate-unscaled-axis-marks resolved-all)
         _ (validate-drawn-channel-marks resolved-all)
         _ (warn-unread-channel-columns resolved-all)
         _ (warn-conflicting-specs draft-layers)
         _ (validate-axis-spec-agreement resolved-all)
         _ (validate-channel-spec-agreement resolved-all)

         ;; Plot-level annotations -- from pj/lay-rule-* / pj/lay-band-*
         ;; layers extracted above. Root-scope annotations (carried
         ;; down through pose/resolve-tree) need deduping because
         ;; facet expansion repeats the same annotation for every
         ;; panel. Strip :x/:y on root-scope so they apply to all
         ;; panels. Panel-scope annotations keep their :x/:y so
         ;; finalize-panel can match them to the right panel.
         ;; Annotations only support literal :color (string) and
         ;; literal :alpha (number); column-mapped aesthetics are
         ;; silently dropped (annotations don't participate in
         ;; column-mapped scales).
         ;; An annotation draws one color for itself and takes no part
         ;; in a scale, so what survives here is a written color and
         ;; nothing else. The test asks whether the value names a color
         ;; rather than whether it is a string: the pose gate accepts a
         ;; keyword naming one now, so `{:color :red}` reached this
         ;; point and was dropped without a word, while
         ;; `{:color :notacolour}` was still reported -- the gate and
         ;; the draw path disagreeing about the same value.
         ;; Annotations carry their color as a string to the renderer,
         ;; so a keyword naming one is spelled out here rather than
         ;; widening the plan schema for a second spelling of the same
         ;; value. Computed once rather than as two `cond->` branches:
         ;; the tests there read the original map, so dropping a
         ;; non-color and then spelling out a keyword both fired on the
         ;; same value and `name` was handed the nil that the drop had
         ;; just left behind.
         clean-aesthetics (fn [m]
                            (let [c (:color m)
                                  c (when (defaults/names-a-color? c)
                                      (if (keyword? c) (name c) c))]
                              (cond-> (dissoc m :color)
                                c (assoc :color c)
                                (not (number? (:alpha m))) (dissoc :alpha))))
         annotation-position-keys [:y-intercept :x-intercept :y-min :y-max :x-min :x-max]
         ;; Annotations cross-product when a leaf is expanded by
         ;; pj/facet (one identical copy per facet panel). Dedup by
         ;; content so finalize-panel matches a single annotation
         ;; against every panel that shares the leaf's x/y.
         annotations (->> layer-annotations
                          (map #(-> %
                                    (select-keys (into [:mark :color :alpha :stroke-dash :offset-x :offset-y :x :y]
                                                       annotation-position-keys))
                                    clean-aesthetics))
                          distinct
                          vec)

         ;; --- Phase 1: compute stats for every panel (no pixel math) ---
         tagged-by-idx (group-by :__panel-idx tagged-draft-layers)
         panel-data (mapv
                     (fn [pg]
                       (let [dls (:draft-layers pg)
                             annotation-only? (and (seq dls)
                                                   (every? #(resolve/annotation-marks (:mark %)) dls))]
                         (cond
                           ;; Annotation-only panel: no stat pipeline,
                           ;; synthesize domains from the annotation's
                           ;; draft-layer data plus the annotation's positions.
                           annotation-only?
                           (merge pg {:resolved []
                                      :stat-results (mapv synthesize-annotation-domain dls)
                                      :layers []})
                           :else
                           (let [pidx (:__panel-idx (first dls))
                                 panel-tagged (or (get tagged-by-idx pidx) dls)
                                 pre-resolved (mapv :__resolved panel-tagged)]
                             (if (seq panel-tagged)
                               (merge pg (resolve-panel-draft-layers panel-tagged all-colors cfg
                                                                     :resolved pre-resolved
                                                                     :shape-map (:shape-map shape-info)))
                               pg)))))
                     (:panels grid))

         _ (warn-undrawn-varies! panel-data)

         ;; --- Phase 2: per-panel domains (still no pixel math) ---
         panel-domains (vec
                        (for [pd panel-data
                              :when (seq (:draft-layers pd))]
                          (resolve-panel-domains pd default-x-scale default-y-scale default-coord
                                                 (:domain-padding cfg))))

         ;; Ridgeline swap: categories go on y, density on x. Swap
         ;; per-panel domains/scales/temporal extents before anything
         ;; reads them for layout or rendering.
         has-ridgeline? (some #(= :ridgeline (:mark %)) draft-layers)
         panel-domains (if has-ridgeline?
                         (mapv (fn [d]
                                 (-> d
                                     (assoc :x-dom (:y-dom d) :y-dom (:x-dom d)
                                            :x-scale (:y-scale d) :y-scale (:x-scale d)
                                            :x-informed? (:y-informed? d)
                                            :y-informed? (:x-informed? d)
                                            :x-te (:y-te d) :y-te (:x-te d))))
                               panel-domains)
                         panel-domains)

         ;; Facet scale coordination: by default, faceted layouts share
         ;; one x-domain and one y-domain across all panels so panels
         ;; are visually comparable. The :scales opt controls which axes
         ;; coordinate. Multi-variable layouts (different x/y vars per
         ;; panel) skip this -- aggregating across different columns
         ;; would be meaningless.
         panel-domains (if (= layout-type :facet-grid)
                         (coordinate-facet-domains panel-domains (:scales opts))
                         panel-domains)

         ;; --- Phase 3: labels, legends, and the three layout fns ---
         multi? (and (= layout-type :multi-variable) (> grid-cols-n 1) (> grid-rows-n 1))
         auto-label? (and (not multi?) (coord/show-ticks? rep-coord))
         {:keys [eff-title eff-x-label eff-y-label]}
         (resolve-labels x-vars y-vars rep-x-scale rep-y-scale
                         title x-label y-label auto-label?)
         swap-labels? (or (= rep-coord :flip) has-ridgeline?)
         [eff-x-label eff-y-label] (if swap-labels?
                                     [eff-y-label eff-x-label]
                                     [eff-x-label eff-y-label])
         ;; :suppress-x-label / :suppress-y-label on opts drop the axis
         ;; label text. Used by the compositor on grid-composite inner
         ;; cells so only the edge cells carry labels -- reduces chart
         ;; junk in a SPLOM.
         eff-x-label (if (:suppress-x-label opts) nil eff-x-label)
         eff-y-label (if (:suppress-y-label opts) nil eff-y-label)
         ;; An axis no panel gives a data meaning carries no ticks (see
         ;; `finalize-panel`), and naming it after the column would be
         ;; the same false claim one line lower: the marks are placed by
         ;; a distance across the panel, not by that column's values.
         eff-x-label (if (some :x-informed? panel-domains) eff-x-label nil)
         eff-y-label (if (some :y-informed? panel-domains) eff-y-label nil)

         ;; Legends -- depend on resolved draft layers + cfg, not on pixel math.
         ;; :suppress-legend on opts skips ALL legend construction; used
         ;; by the compositor on sub-plots (e.g. SPLOM cells) so the legend
         ;; doesn't eat the per-cell render rectangle. The per-channel
         ;; flags (:suppress-color-legend, :suppress-size-legend,
         ;; :suppress-alpha-legend, :suppress-shape-legend) are set by
         ;; the aware-chrome path when
         ;; only some aesthetics are unanimous across composite cells --
         ;; the per-leaf legend renders for the non-unanimous aesthetics
         ;; while the unanimous ones get one shared legend at composite
         ;; level.
         suppress-legend? (:suppress-legend opts)
         suppress-color? (or suppress-legend? (:suppress-color-legend opts))
         suppress-size? (or suppress-legend? (:suppress-size-legend opts))
         suppress-alpha? (or suppress-legend? (:suppress-alpha-legend opts))
         suppress-shape? (or suppress-legend? (:suppress-shape-legend opts))
         ;; A legend title is one setting at two scopes, as an axis
         ;; title is: `:label` in the aesthetic's scale spec, and the
         ;; `<aesthetic>-label` plot option one scope out. The spec is
         ;; written further in, so it wins.
         legend-title (fn [aesthetic]
                        (defaults/scale-setting
                         aesthetic :label
                         (some (defaults/channel->scale-key aesthetic) resolved-all)
                         opts))
         legend (when-not suppress-color?
                  (build-legend resolved-all numeric-color? all-colors color-cols cfg
                                (legend-title :color)))
         legend (or legend
                    (when-not suppress-color?
                      (build-fill-fallback-legend panel-data resolved-all cfg
                                                  (legend-title :fill))))
         size-legend (when-not suppress-size?
                       (build-size-legend resolved-all (legend-title :size) height))
         alpha-legend (when-not suppress-alpha?
                        (build-alpha-legend resolved-all (legend-title :alpha)))
         shape-legend (when-not suppress-shape?
                        (build-shape-legend shape-info (legend-title :shape)))
         [legend shape-legend] (merge-shape-into-color-legend legend shape-legend)

         ;; Scene: everything compute-padding + compute-dims need to
         ;; know about the data and options, all data-derived or
         ;; opts-derived. No pixel math yet. Taken as a function of the
         ;; panel domains because the text fit below revises them and
         ;; then needs the layout those revised domains imply.
         layout-for
         (fn [pds]
           (let [scene (layout/compute-scene
                        {:layout-type layout-type
                         :grid-rows grid-rows-n
                         :grid-cols grid-cols-n
                         :eff-title eff-title
                         :subtitle subtitle
                         :caption caption
                         :eff-x-label eff-x-label
                         :eff-y-label eff-y-label
                         :facet-row-vals (:facet-row-vals grid)
                         :facet-col-vals (:facet-col-vals grid)
                         :coord-type rep-coord
                         :panel-x-domains (mapv :x-dom pds)
                         :panel-y-domains (mapv :y-dom pds)
                         :x-scale-spec rep-x-scale
                         :y-scale-spec rep-y-scale
                         :x-temporal (some :x-te pds)
                         :y-temporal (some :y-te pds)
                         :panel-row-labels (mapv :row-label pds)
                         :panel-col-labels (mapv :col-label pds)
                         :legend legend
                         :size-legend size-legend
                         :alpha-legend alpha-legend
                         :shape-legend shape-legend})
                 padding (layout/compute-padding scene cfg layout-opts)]
             {:padding padding
              :dims (layout/compute-dims scene padding cfg layout-opts)}))

         m (if multi? (:margin-multi cfg) (:margin cfg))

         ;; --- Phase 3b: fit text marks into the panel ---
         ;; A text mark is sized in pixels, so a data label at the edge of
         ;; the data is drawn past the edge of the drawing area and cut
         ;; off there. Widen the domains until the labels fit. A widened
         ;; domain can print wider tick labels, which moves the panel
         ;; width the fit was measured against, so layout and fit
         ;; alternate until the domains settle. Ridgeline panels are
         ;; skipped: their domains have already been swapped away from
         ;; the axes their layers were extracted on.
         fit-text? (and (:fit-text-domain cfg) (not has-ridgeline?))
         [panel-domains {:keys [padding dims]}]
         (loop [pds panel-domains k 0]
           (let [lay (layout-for pds)
                 fitted (if fit-text?
                          (cond-> (mapv #(fit-panel-text % (:pw (:dims lay))
                                                         (:ph (:dims lay)) m)
                                        pds)
                            (= layout-type :facet-grid)
                            (coordinate-facet-domains (:scales opts)))
                          pds)]
             (if (or (= fitted pds) (>= k 2))
               [pds lay]
               (recur fitted (inc k)))))
         {:keys [pw ph total-w total-h]} dims

         ;; --- Phase 4: :coord :fixed aspect adjustment ---
         ;; When the user asked for a 1:1 data-unit aspect ratio, shrink
         ;; the larger panel axis so that one data unit on x equals one
         ;; data unit on y. This runs AFTER compute-dims so we have real
         ;; pw/ph to adjust. If the adjustment fires we also recompute
         ;; total-w/total-h to reflect the shrink.
         fixed-result (when (and (= rep-coord :fixed) (seq panel-domains))
                        (let [p1 (first panel-domains)
                              gx (:x-dom p1)
                              gy (:y-dom p1)]
                          (when (and (sequential? gx) (= 2 (count gx)) (number? (first gx))
                                     (sequential? gy) (= 2 (count gy)) (number? (first gy)))
                            (adjust-fixed-aspect pw ph gx gy))))
         [pw ph] (if fixed-result
                   [(:pw fixed-result) (:ph fixed-result)]
                   [pw ph])
         total-w (if fixed-result
                   (+ (:horiz-overhead dims) (* grid-cols-n pw))
                   total-w)
         total-h (if fixed-result
                   (+ (:vert-overhead dims) (* grid-rows-n ph))
                   total-h)

         ;; --- Phase 5: compute ticks at the final panel dimensions ---
         panels (mapv #(finalize-panel % pw ph m cfg annotations) panel-domains)
         ;; :suppress-x-ticks / :suppress-y-ticks on opts blank the
         ;; tick set for the corresponding axis. Compositor sets these
         ;; on inner cells of grid-composites (SPLOM) so only the
         ;; bottom row carries x-tick numbers and only the leftmost
         ;; column carries y-tick numbers -- matches the legacy SPLOM
         ;; chrome and avoids visual noise from per-cell tick text.
         empty-ticks {:values [] :labels [] :categorical? false}
         panels (cond-> panels
                  (:suppress-x-ticks opts)
                  (->> (mapv #(assoc % :x-ticks empty-ticks)))
                  (:suppress-y-ticks opts)
                  (->> (mapv #(assoc % :y-ticks empty-ticks))))

         plan
         (resolve/map->Plan
          {:width width :height height :margin m
           :total-width total-w :total-height total-h
           :panel-width pw :panel-height ph
           :grid {:rows grid-rows-n :cols grid-cols-n}
           :layout-type layout-type
           :title eff-title :subtitle subtitle :caption caption
           :x-label eff-x-label :y-label eff-y-label
           :legend legend :size-legend size-legend :alpha-legend alpha-legend
           :shape-legend shape-legend
           :legend-position (:legend-position padding)
           :panels panels
           :tooltip (:tooltip opts)
           :layout (select-keys padding [:x-label-pad :y-label-pad :title-pad
                                         :subtitle-pad :caption-pad
                                         :legend-w :legend-h :strip-h :strip-w])})]
     (when validate?
       (when-let [explanation (ss/explain plan)]
         (throw (ex-info "Plan does not conform to schema" {:explanation explanation}))))
     plan)))
