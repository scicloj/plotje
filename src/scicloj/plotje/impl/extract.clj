(ns scicloj.plotje.impl.extract
  "Extract data-space geometry from resolved draft layers and stat results.
   Produces layer descriptor maps — plain Clojure maps with mark type,
   style, and groups of data-space coordinates."
  (:require [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.impl.scale :as scale]
            [tech.v3.datatype.functional :as dfn]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]))

;; ---- Color Resolution (data-space) ----

(defn resolve-color
  "Resolve a color value to [r g b a]. Handles column values, fixed colors, and defaults.

   A fixed color wins over a group's palette color. The two cannot
   conflict: `:color` is one key, so a literal there means there is no
   color column, and any group color present came from `:group` instead.
   Grouping by one column while drawing every group in one color is a
   common case: many pale lines behind a few named ones. It needs the
   fixed color to survive the grouping.

   Takes the resolved draft layer rather than its `:fixed-color`,
   because the layer answers a second question too: whether its color
   column is drawn as it stands. `resolve/resolve-aesthetics` decides
   that once for the column, so what arrives here is a group value that
   already *is* a color and needs no palette."
  [all-colors color-val draft-layer cfg]
  (let [fixed-color (:fixed-color draft-layer)]
    (cond
      ;; A drawn color arrives as whatever was written -- `"red"`,
      ;; `"#FF0000"` or `:steelblue` -- and only an RGBA vector is
      ;; already in the form the plan carries. The keyword case reaches
      ;; here now that the data decides which values name columns;
      ;; before, a keyword was a column reference and nothing else.
      fixed-color (if (vector? fixed-color) fixed-color (defaults/hex->rgba fixed-color))
      (and (:color-drawn? draft-layer) (some? color-val))
      (defaults/hex->rgba color-val)
      (some? color-val) (defaults/color-for
                         all-colors color-val
                         (defaults/scale-setting
                          :color :values (:color-scale draft-layer) cfg))
      :else (defaults/hex->rgba (:default-color cfg)))))

(defn fill-setting
  "A scale setting for a mark drawn in a fill: what a `:fill` scale spec
   or the matching plot option says, and failing that what the `:color`
   ones say.

   A tile reads `:color` as a synonym for `:fill`, so a gradient or a
   midpoint written either way has to reach it. The spec side already
   falls back this way; this keeps the option side in step rather than
   letting the two disagree."
  [k draft-layer cfg]
  (let [v (defaults/scale-setting :fill k (:fill-scale draft-layer) cfg)]
    (if (some? v)
      v
      (defaults/scale-setting :color k (:color-scale draft-layer) cfg))))

(def dash-presets
  "Named stroke-dash presets, resolved to a `[dash gap]` pixel pattern.
   `:solid` means no dash (returns nil)."
  {:solid  nil
   :dashed [6 4]
   :dotted [1 3]})

(defn resolve-dash
  "Resolve a `:stroke-dash` value to a `[dash gap ...]` vector or nil.
   Accepts a named preset (`:solid`/`:dashed`/`:dotted`), a raw
   `[dash gap ...]` vector (passed through), or nil (no dash)."
  [stroke-dash]
  (cond
    (nil? stroke-dash) nil
    (keyword? stroke-dash) (get dash-presets stroke-dash)
    (sequential? stroke-dash) (vec stroke-dash)
    :else nil))

(defn- apply-nudge
  "Apply nudge-x/nudge-y offsets to a layer's groups.
   Nudge shifts data coordinates by a constant amount — orthogonal to
   position adjustment (dodge/stack). Handles three group shapes:
     - polyline groups with :xs/:ys (and optional :ymins/:ymaxs)
     - line-segment groups with :x1/:y1/:x2/:y2 (from :lm regression)
     - any group with any subset of the above

   Previously, `:lm` and `:loess` listed `:nudge-x`/`:nudge-y` in their
   `:accepts` but the segment-style groups they produced were silently
   untouched because only :xs/:ys were updated.

   Nudge is a data-space shift, so it only applies to a numeric or
   temporal axis. On a categorical axis the coordinates are still
   category labels at this stage (positions are assigned later by the
   renderer), so a numeric shift has no meaning -- refuse it with a
   message pointing at the tools that do place marks on a categorical
   axis."
  [layer {:keys [nudge-x nudge-y x-type y-type]}]
  (when (and nudge-x (= x-type :categorical))
    (throw (ex-info (str ":nudge-x is a data-space shift and does not apply to a "
                         "categorical x axis. To move a mark by a distance on the "
                         "page use :offset-x, which works on any axis; to place a "
                         "label relative to its point use :align-x; to spread "
                         "overlapping marks use :jitter or :position :dodge.")
                    {:nudge-x nudge-x :x-type x-type})))
  (when (and nudge-y (= y-type :categorical))
    (throw (ex-info (str ":nudge-y is a data-space shift and does not apply to a "
                         "categorical y axis. To move a mark by a distance on the "
                         "page use :offset-y, which works on any axis; to place a "
                         "label relative to its point use :align-y; to spread "
                         "overlapping marks use :jitter or :position :dodge.")
                    {:nudge-y nudge-y :y-type y-type})))
  (if (or nudge-x nudge-y)
    (let [nx (when nudge-x (double nudge-x))
          ny (when nudge-y (double nudge-y))]
      (update layer :groups
              (fn [gs]
                (mapv (fn [g]
                        (cond-> g
                          (and nx (:xs g))    (update :xs dfn/+ nx)
                          (and ny (:ys g))    (update :ys dfn/+ ny)
                          (and ny (:ymins g)) (update :ymins dfn/+ ny)
                          (and ny (:ymaxs g)) (update :ymaxs dfn/+ ny)
                          (and nx (:x1 g))    (update :x1 + nx)
                          (and ny (:y1 g))    (update :y1 + ny)
                          (and nx (:x2 g))    (update :x2 + nx)
                          (and ny (:y2 g))    (update :y2 + ny)))
                      gs))))
    layer))

(defn- default-position
  "Default position for marks that normally dodge.
   When :color is the same column as :x, dodge is suppressed — each x-band
   already contains exactly one color group, so dodging shrinks and
   offsets the mark unnecessarily."
  [draft-layer]
  (or (:position draft-layer)
      (if (and (:color draft-layer) (= (:color draft-layer) (:x draft-layer)))
        :identity
        :dodge)))

(defn- check-stat
  "Assert that stat result contains expected key for the given mark.
   Raises a clear error instead of letting nil propagate to the renderer."
  [stat expected-key mark]
  (when-not (contains? stat expected-key)
    (throw (ex-info (str "Stat result for :" (name mark)
                         " mark must contain :" (name expected-key)
                         ", got keys: " (pr-str (keys stat)))
                    {:mark mark :expected expected-key :stat-keys (keys stat)}))))

(defn- extract-xy-groups
  "Extract groups from stat :points, resolving colors. Common to most mark types.
   Options:
     :with-range? — include :ymins/:ymaxs (errorbar, pointrange)
     :with-labels? — include :labels from :labels key (text, label marks)"
  [draft-layer stat all-colors cfg & {:keys [with-range? with-labels?]}]
  (let [groups (vec
                (for [{:keys [color xs ys ymins ymaxs labels]} (:points stat)]
                  (cond-> {:color (resolve-color all-colors color draft-layer cfg)
                           :xs xs :ys ys}
                    (some? color) (assoc :label (defaults/fmt-category-label color))
                    (and with-range? ymins) (assoc :ymins ymins)
                    (and with-range? ymaxs) (assoc :ymaxs ymaxs)
                    ;; Text drawn from a data column is the one label kind that
                    ;; reports a measured quantity, so it honors
                    ;; :thousands-separator; category names, legend entries and
                    ;; facet strips stay as they are.
                    (and with-labels? labels)
                    (assoc :labels (mapv #(defaults/fmt-value-label
                                           % (defaults/number-separators cfg))
                                         labels)))))]
    (when (and with-range? (seq groups)
               (not-any? :ymins groups))
      (throw (ex-info (str "errorbar/pointrange requires :y-min and :y-max columns. "
                           "Pass them as options: (pj/lay-errorbar :x :y {:y-min :lo :y-max :hi})")
                      {:mark (:mark draft-layer)})))
    (when (and with-labels? (seq groups)
               (not-any? :labels groups))
      (throw (ex-info (str "text/label mark requires a :text column. "
                           "Pass it as an option: (pj/lay-text :x :y {:text :label-column})")
                      {:mark (:mark draft-layer)})))
    groups))

;; ---- Geometry Extraction (stat → layer descriptors) ----

(defmulti extract-layer
  "Extract data-space geometry from a resolved draft layer and its stat result.
   Returns a layer descriptor map."
  (fn [draft-layer stat all-colors cfg] (:mark draft-layer)))

;; ---- Doc methods (dispatching on [mark-key :doc]) ----

(defmethod extract-layer [:point :doc] [_ _ _ _] "Filled circle")
(defmethod extract-layer [:line :doc] [_ _ _ _] "Connected path")
(defmethod extract-layer [:step :doc] [_ _ _ _] "Horizontal-then-vertical path")
(defmethod extract-layer [:bar :doc] [_ _ _ _] "Vertical rectangles (binned)")
(defmethod extract-layer [:rect :doc] [_ _ _ _] "Positioned rectangles")
(defmethod extract-layer [:area :doc] [_ _ _ _] "Filled region under a curve")
(defmethod extract-layer [:tile :doc] [_ _ _ _] "Grid of colored cells")
(defmethod extract-layer [:contour :doc] [_ _ _ _] "Iso-value polylines")
(defmethod extract-layer [:boxplot :doc] [_ _ _ _] "Box-and-whisker")
(defmethod extract-layer [:violin :doc] [_ _ _ _] "Mirrored density shape")
(defmethod extract-layer [:ridgeline :doc] [_ _ _ _] "Stacked density curves")
(defmethod extract-layer [:pointrange :doc] [_ _ _ _] "Point with error bar")
(defmethod extract-layer [:errorbar :doc] [_ _ _ _] "Vertical error bar")
(defmethod extract-layer [:lollipop :doc] [_ _ _ _] "Stem with dot")
(defmethod extract-layer [:text :doc] [_ _ _ _] "Data-driven label, optionally on a background box")
(defmethod extract-layer [:rug :doc] [_ _ _ _] "Axis-margin tick marks")
(defmethod extract-layer [:interval-h :doc] [_ _ _ _] "Horizontal bars from x to x-end at categorical y")
(defmethod extract-layer :point [draft-layer stat all-colors cfg]
  (let [numeric-color? (= (:color-type draft-layer) :numerical)
        ;; For numeric color: compute global min/max for normalization
        all-color-buf (when numeric-color?
                        (let [bufs (keep :color-values (:points stat))]
                          (when (seq bufs) (dtype/concat-buffers bufs))))
        ;; A `:domain` on the scale spec replaces what the data covers.
        [c-min c-max] (scale/numeric-color-domain
                       (:color-scale draft-layer)
                       (when all-color-buf (dfn/reduce-min all-color-buf))
                       (when all-color-buf (dfn/reduce-max all-color-buf)))]
    (-> {:mark :point
         :size-scale (:size-scale draft-layer)
         :alpha-scale (:alpha-scale draft-layer)
         ;; A column told not to scale holds radii and opacities
         ;; already, so the renderer reads them straight rather than
         ;; normalizing them into its output range.
         :size-drawn? (:size-drawn? draft-layer)
         :alpha-drawn? (:alpha-drawn? draft-layer)
         :style (cond-> {:opacity (or (:fixed-alpha draft-layer) (:point-opacity cfg))
                         :radius (or (:fixed-size draft-layer) (:point-radius cfg))}
                  ;; One symbol for the whole layer, beside the fixed
                  ;; radius and opacity it sits with. A per-row `:shapes`
                  ;; buffer still wins where there is one.
                  (:fixed-shape draft-layer) (assoc :shape (:fixed-shape draft-layer))
                  (:jitter draft-layer) (assoc :jitter (:jitter draft-layer))
                  (and (:point-stroke cfg)
                       (not= (:point-stroke cfg) "none"))
                  (assoc :stroke (:point-stroke cfg)
                         :stroke-width (or (:point-stroke-width cfg) 0)))
         :groups (vec
                  (for [{:keys [color xs ys sizes alphas shapes row-indices color-values]} (:points stat)]
                    (cond-> {:color (resolve-color all-colors color draft-layer cfg)
                             :xs xs :ys ys}
                      (some? color) (assoc :label (defaults/fmt-category-label color))
                      (and numeric-color? color-values)
                      (assoc :colors (vec (map (fn [v]
                                                 (let [spec (:color-scale draft-layer)
                                                       scale-type (or (:type spec) :linear)
                                                       t (defaults/normalize-continuous
                                                          scale-type v (or c-min 0) (or c-max 1)
                                                          (defaults/scale-setting :color :midpoint spec cfg))
                                                       grad-fn (defaults/scale-gradient-fn :color spec cfg)]
                                                   (grad-fn t)))
                                               color-values)))
                      sizes (assoc :sizes sizes)
                      alphas (assoc :alphas alphas)
                      shapes (assoc :shapes (vec shapes))
                      row-indices (assoc :row-indices row-indices))))}
        (cond-> (:position draft-layer) (assoc :position (:position draft-layer)))
        (apply-nudge draft-layer))))

(defmethod extract-layer :bar [draft-layer stat all-colors cfg]
  (check-stat stat :bins :bar)
  {:mark :bar
   :style {:opacity (or (:fixed-alpha draft-layer) (:bar-opacity cfg))}
   :groups (vec
            (for [{:keys [color bin-maps]} (:bins stat)]
              {:color (resolve-color all-colors color draft-layer cfg)
               :bars (vec (for [{:keys [min max count]} bin-maps]
                            {:lo min :hi max :count count}))}))})

(defmethod extract-layer :line [draft-layer stat all-colors cfg]
  (-> (cond-> {:mark :line
               :style (cond-> {:stroke-width (or (:fixed-size draft-layer) (:line-width cfg))
                               :opacity (or (:fixed-alpha draft-layer) 1.0)}
                        (resolve-dash (:stroke-dash draft-layer))
                        (assoc :dash (resolve-dash (:stroke-dash draft-layer))))
               :groups (vec
                        (concat
                         ;; Regression lines
                         (when-let [lines (:lines stat)]
                           (for [{:keys [color x1 y1 x2 y2]} lines]
                             {:color (resolve-color all-colors color draft-layer cfg)
                              :label (defaults/fmt-category-label color)
                              :x1 x1 :y1 y1 :x2 x2 :y2 y2}))
                         ;; Polylines
                         (when-let [pts (:points stat)]
                           (for [{:keys [color xs ys]} pts]
                             {:color (resolve-color all-colors color draft-layer cfg)
                              :label (defaults/fmt-category-label color)
                              :xs xs :ys ys}))))}
        ;; Confidence ribbons from :lm {:confidence-band true}
        (:ribbons stat)
        (assoc :ribbons (vec
                         (for [{:keys [color xs ymins ymaxs]} (:ribbons stat)]
                           {:color (resolve-color all-colors color draft-layer cfg)
                            :xs xs :ymins ymins :ymaxs ymaxs})))
        (:position draft-layer)
        (assoc :position (:position draft-layer)))
      (apply-nudge draft-layer)))

(defmethod extract-layer :step [draft-layer stat all-colors cfg]
  {:mark :step
   :style (cond-> {:stroke-width (or (:fixed-size draft-layer) (:line-width cfg))
                   :opacity (or (:fixed-alpha draft-layer) 1.0)}
            (resolve-dash (:stroke-dash draft-layer))
            (assoc :dash (resolve-dash (:stroke-dash draft-layer))))
   :groups (extract-xy-groups draft-layer stat all-colors cfg)})

(defmethod extract-layer :rect [draft-layer stat all-colors cfg]
  (if (:bars stat)
    ;; Categorical bars (from :count stat) -- :count stat already validated
    {:mark :rect
     :style {:opacity (or (:fixed-alpha draft-layer) (:bar-opacity cfg))}
     :position (default-position draft-layer)
     :categories (vec (:categories stat))
     :groups (vec
              (for [{:keys [color counts]} (:bars stat)]
                {:color (resolve-color all-colors color draft-layer cfg)
                 :label (defaults/fmt-category-label color)
                 :counts (vec counts)}))}
    ;; Value bars (from :identity stat) -- need a categorical axis for band
    ;; layout, on x (vertical bars) or y (horizontal bars). When the
    ;; categorical axis is y, the orientation is flipped (mirrors boxplot).
    (let [x-cat? (= (:x-type draft-layer) :categorical)
          y-cat? (= (:y-type draft-layer) :categorical)
          flipped? (and y-cat? (not x-cat?))]
      (if-not (or x-cat? y-cat?)
        ;; Numeric/temporal-position bars: neither axis is categorical, so each
        ;; bar sits at its x with a width from the stat (:bar-width). Emit the
        ;; histogram :bar mark -- it draws rectangles from explicit x-edges --
        ;; instead of the categorical band layout.
        (let [w (double (or (:bar-width stat) 1.0))
              half (/ w 2.0)]
          {:mark :bar
           :style {:opacity (or (:fixed-alpha draft-layer) (:bar-opacity cfg))}
           :groups (vec
                    (for [{:keys [color xs ys]} (:points stat)]
                      {:color (resolve-color all-colors color draft-layer cfg)
                       :bars (mapv (fn [x y]
                                     (let [xd (double x)]
                                       {:lo (- xd half) :hi (+ xd half) :count y}))
                                   xs ys)}))})
        ;; Categorical value bars -- band layout, on x (vertical bars) or y
        ;; (horizontal bars). When the categorical axis is y, the orientation
        ;; is flipped (mirrors boxplot).
        (do
          ;; Horizontal value bars (categorical y) stack/fill along x, but the
          ;; stacking machinery accumulates on the y-axis. Until that is made
          ;; orientation-aware, route this case through coord :flip (which lays
          ;; the bars out vertically, then flips), where stacking works.
          (when (and flipped? (#{:stack :fill} (:position draft-layer)))
            (throw (ex-info (str "Stacked/filled horizontal value bars are not supported directly yet. "
                                 "Put the category on :x and use (pj/coord :flip): "
                                 "(-> data (pj/lay-bar " (pr-str (:y draft-layer)) " " (pr-str (:x draft-layer))
                                 " {:position " (pr-str (:position draft-layer)) " ...}) (pj/coord :flip)).")
                            {:mark :rect :position (:position draft-layer)
                             :x (:x draft-layer) :y (:y draft-layer)})))
          {:mark :rect
           :style {:opacity (or (:fixed-alpha draft-layer) (:bar-opacity cfg))}
           :position (default-position draft-layer)
           :groups (vec
                    (for [{:keys [color xs ys]} (:points stat)]
                      {:color (resolve-color all-colors color draft-layer cfg)
                       :label (defaults/fmt-category-label color)
                       ;; Canonical slots: category on :xs, numeric value on :ys.
                       ;; The renderer bands :xs (on the auto-swapped band scale)
                       ;; and measures :ys, so this draws correctly either way.
                       :xs (if flipped? ys xs)
                       :ys (if flipped? xs ys)}))})))))

(def ^:private align-x-values
  "Horizontal anchor values: which part of the text lands on the data point."
  #{:left :center :right})

(def ^:private align-y-values
  "Vertical anchor values: which part of the text lands on the data point.
   Data-oriented -- :top means the top edge of the text sits at the point
   (text extends downward), :bottom the bottom edge (text extends upward)."
  #{:bottom :center :top})

(defn- resolve-align
  "Resolve and validate the text anchor for a :text/:label draft layer.
   Defaults :align-x to :left and :align-y to :center, preserving the
   placement text marks had before anchoring existed."
  [draft-layer]
  (let [ax (or (:align-x draft-layer) :left)
        ay (or (:align-y draft-layer) :center)]
    (when-not (align-x-values ax)
      (throw (ex-info (str ":align-x must be one of " (sort align-x-values)
                           ", got: " (pr-str ax))
                      {:align-x ax})))
    (when-not (align-y-values ay)
      (throw (ex-info (str ":align-y must be one of " (sort align-y-values)
                           ", got: " (pr-str ay))
                      {:align-y ay})))
    {:align-x ax :align-y ay}))

(def ^:private font-weight-values
  "Text weight values. Java2D distinguishes bold from not-bold and nothing
   finer, so the numeric CSS weights are not accepted."
  #{:normal :bold})

(def ^:private font-style-values
  "Text slant values. CSS :oblique is not accepted: SVG would slant it but
   java.awt.Font has no oblique style (only PLAIN, BOLD, ITALIC), so the
   PNG backend would draw it upright -- the same plot would differ by
   output format."
  #{:normal :italic})

(defn- resolve-font
  "Resolve and validate the weight and slant for a :text/:label draft layer.
   Both default to :normal, preserving the plain text drawn before font
   styling existed."
  [draft-layer]
  (let [w (or (:font-weight draft-layer) :normal)
        s (or (:font-style draft-layer) :normal)]
    (when-not (font-weight-values w)
      (throw (ex-info (str ":font-weight must be one of " (sort font-weight-values)
                           ", got: " (pr-str w))
                      {:font-weight w})))
    (when-not (font-style-values s)
      (throw (ex-info (str ":font-style must be one of " (sort font-style-values)
                           ", got: " (pr-str s))
                      {:font-style s})))
    {:font-weight w :font-style s}))

(def ^:private default-box
  "Properties of the background box behind text, as ggplot2's geom_label
   draws it: a rounded panel, radius in pixels."
  {:corner-radius 3.0})

(def ^:private box-properties
  "Box properties a `:box` map may set. Anything else is a typo -- silently
   ignoring it would draw the default box and give no hint why."
  #{:corner-radius})

(defn- resolve-box
  "Normalize the `:box` option to nil (no box) or a map of box properties.
   `true` takes the defaults, a map overrides them, and false or absent
   means no box -- which is what a bare `pj/lay-text` layer gets."
  [draft-layer]
  (let [b (:box draft-layer)]
    (cond
      (or (nil? b) (false? b)) nil
      (true? b) default-box
      (map? b)
      (let [unknown (remove box-properties (keys b))
            r (get b :corner-radius (:corner-radius default-box))]
        (when (seq unknown)
          (throw (ex-info (str ":box accepts only " (sort box-properties)
                               ", got: " (pr-str (vec unknown)))
                          {:unknown-box-properties (vec unknown)})))
        (when-not (and (number? r) (not (neg? (double r))))
          (throw (ex-info (str ":box :corner-radius must be a non-negative number, got: "
                               (pr-str r))
                          {:corner-radius r})))
        (assoc default-box :corner-radius (double r)))
      :else
      (throw (ex-info (str ":box must be true, false, or a map of box properties, got: "
                           (pr-str b))
                      {:box b})))))

(defmethod extract-layer :text [draft-layer stat all-colors cfg]
  (-> {:mark :text
       :style (merge {:font-size (or (:font-size draft-layer) 10)
                      :box (resolve-box draft-layer)
                      :opacity (or (:fixed-alpha draft-layer) 1.0)}
                     (resolve-align draft-layer)
                     (resolve-font draft-layer))
       :groups (extract-xy-groups draft-layer stat all-colors cfg :with-labels? true)}
      (apply-nudge draft-layer)))

(defmethod extract-layer :area [draft-layer stat all-colors cfg]
  (let [stroke (:stroke draft-layer)
        dash (resolve-dash (:stroke-dash draft-layer))]
    (cond-> {:mark :area
             :style (cond-> {:opacity (or (:fixed-alpha draft-layer) 0.5)}
                      ;; Optional outline on the top curve (opt-in via :stroke).
                      stroke (assoc :stroke-color (if (string? stroke)
                                                    (defaults/hex->rgba stroke)
                                                    stroke)
                                    :stroke-width (or (:stroke-width draft-layer)
                                                      (:line-width cfg)))
                      dash (assoc :dash dash))
             :groups (extract-xy-groups draft-layer stat all-colors cfg)}
      (:position draft-layer) (assoc :position (:position draft-layer)))))

(defmethod extract-layer :errorbar [draft-layer stat all-colors cfg]
  (-> {:mark :errorbar
       :style {:stroke-width (or (:fixed-size draft-layer) 1.5)
               :cap-width (or (:cap-width draft-layer) 6)
               :opacity (or (:fixed-alpha draft-layer) 1.0)}
       :groups (extract-xy-groups draft-layer stat all-colors cfg :with-range? true)}
      (cond-> (:position draft-layer) (assoc :position (:position draft-layer)))
      (apply-nudge draft-layer)))

(defmethod extract-layer :lollipop [draft-layer stat all-colors cfg]
  {:mark :lollipop
   :style {:radius (or (:fixed-size draft-layer) (:point-radius cfg))
           :stroke-width 1.5
           :opacity (or (:fixed-alpha draft-layer) 1.0)}
   :position (default-position draft-layer)
   :groups (extract-xy-groups draft-layer stat all-colors cfg)})

(defmethod extract-layer :boxplot [draft-layer stat all-colors cfg]
  (check-stat stat :boxes :boxplot)
  (let [color-cats (:color-categories stat)]
    {:mark :boxplot
     :style {:box-width (or (:box-width draft-layer) 0.6)
             :stroke-width (or (:fixed-size draft-layer) 1.5)
             :opacity (or (:fixed-alpha draft-layer) 1.0)}
     :position (default-position draft-layer)
     :color-categories color-cats
     :boxes (vec
             (for [b (:boxes stat)]
               (cond-> {:category (:category b)
                        :color (resolve-color all-colors (:color b) draft-layer cfg)
                        :median (:median b) :q1 (:q1 b) :q3 (:q3 b)
                        :whisker-lo (:whisker-lo b) :whisker-hi (:whisker-hi b)}
                 (:color b) (assoc :color-category (:color b))
                 (seq (:outliers b)) (assoc :outliers (:outliers b)))))}))

(defmethod extract-layer :violin [draft-layer stat all-colors cfg]
  (check-stat stat :violins :violin)
  (let [color-cats (:color-categories stat)]
    {:mark :violin
     :style {:opacity (or (:fixed-alpha draft-layer) 0.7)
             :stroke-width (or (:fixed-size draft-layer) 1.0)}
     :position (default-position draft-layer)
     :color-categories color-cats
     :violins (vec
               (for [v (:violins stat)]
                 (cond-> {:category (:category v)
                          :color (resolve-color all-colors (:color v) draft-layer cfg)
                          :ys (:ys v)
                          :densities (:densities v)}
                   (:color v) (assoc :color-category (:color v)))))}))

(defn- min-step
  "Minimum step between sorted distinct values in a sequence."
  [vals]
  (let [sorted (vec (sort (distinct vals)))
        n (count sorted)]
    (if (<= n 1)
      1.0
      (reduce min (map #(- (double (sorted (inc %)))
                           (double (sorted %)))
                       (range (dec n)))))))

(defmethod extract-layer :tile [draft-layer stat all-colors cfg]
  (let [;; Accept :color as a synonym for :fill on tiles -- users coming
        ;; from the other marks reach for :color by default. If both are
        ;; set, :fill wins (explicit).
        fill-col (or (:fill draft-layer)
                     (when (let [c (:color draft-layer)] (and c (or (keyword? c) (string? c))))
                       (:color draft-layer)))
        grad-fn (defaults/resolve-gradient-fn (fill-setting :range draft-layer cfg))
        midpoint (fill-setting :midpoint draft-layer cfg)
        ;; :fill-scale wins over the colour scale spec, which is honored
        ;; on tiles when the user wrote :color (synonym for :fill).
        fill-spec (or (:fill-scale draft-layer)
                      (:color-scale draft-layer))
        fill-scale-type (or (:type fill-spec) :linear)
        ;; Two paths: bin2d/kde2d stat produces :tiles as a dataset;
        ;; identity stat with :fill uses point groups
        tiles (if (and (:tiles stat)
                       (or (and (tc/dataset? (:tiles stat)) (pos? (tc/row-count (:tiles stat))))
                           (and (not (tc/dataset? (:tiles stat))) (seq (:tiles stat)))))
                ;; bin2d/kde2d path — :tiles is a dataset with :x-lo :x-hi :y-lo :y-hi :fill
                (let [tile-ds (:tiles stat)
                      [f-lo f-hi] (let [[lo hi] (:fill-range stat)]
                                    (or (scale/numeric-color-domain fill-spec lo hi)
                                        [lo hi]))
                      ;; Derive :color column from :fill using gradient function
                      with-color (tc/add-column tile-ds :color
                                                (fn [ds]
                                                  (mapv (fn [f]
                                                          (grad-fn (defaults/normalize-continuous
                                                                    fill-scale-type
                                                                    f f-lo f-hi midpoint)))
                                                        (ds :fill))))]
                  (vec (tc/rows (tc/select-columns with-color
                                                   [:x-lo :x-hi :y-lo :y-hi :color])
                                :as-maps)))
                ;; identity path — derive tile bounds from point coordinates
                (let [data (:data draft-layer)
                      fill-vals (when fill-col (data fill-col))
                      [f-lo f-hi] (scale/numeric-color-domain
                                   fill-spec
                                   (when (seq fill-vals) (dfn/reduce-min fill-vals))
                                   (when (seq fill-vals) (dfn/reduce-max fill-vals)))
                      all-xs (mapcat :xs (:points stat))
                      all-ys (mapcat :ys (:points stat))
                      x-half (/ (min-step all-xs) 2.0)
                      y-half (/ (min-step all-ys) 2.0)
                      ;; Build a dataset from the parallel buffers
                      all-x-vals (vec (mapcat :xs (:points stat)))
                      all-y-vals (vec (mapcat :ys (:points stat)))
                      pt-ds (tc/dataset {:x all-x-vals :y all-y-vals})
                      ;; Derive tile bounds as columns
                      with-bounds (-> pt-ds
                                      (tc/add-column :x-lo (dfn/- (pt-ds :x) x-half))
                                      (tc/add-column :x-hi (dfn/+ (pt-ds :x) x-half))
                                      (tc/add-column :y-lo (dfn/- (pt-ds :y) y-half))
                                      (tc/add-column :y-hi (dfn/+ (pt-ds :y) y-half)))
                      ;; Derive color column
                      with-color (tc/add-column with-bounds :color
                                                (fn [ds]
                                                  (if fill-vals
                                                    (mapv (fn [f]
                                                            (grad-fn (defaults/normalize-continuous
                                                                      fill-scale-type
                                                                      f (or f-lo 0) (or f-hi 1) midpoint)))
                                                          fill-vals)
                                                    (vec (repeat (tc/row-count ds)
                                                                 (grad-fn 0.5))))))]
                  (vec (tc/rows (tc/select-columns with-color
                                                   [:x-lo :x-hi :y-lo :y-hi :color])
                                :as-maps))))]
    {:mark :tile
     :style {:opacity (or (:fixed-alpha draft-layer) 1.0)}
     :fill-scale (:fill-scale draft-layer)
     :color-scale (:color-scale draft-layer)
     :tiles tiles}))

(defn- marching-squares-segments
  "Apply marching squares to a density grid for a given threshold.
   Returns a sequence of line segments [[x1 y1] [x2 y2]] in data coordinates.
   Grid is n×n with densities[i*n+j] at cell (i,j)."
  [^doubles densities n-grid threshold x-lo y-lo x-step y-step]
  (let [grid-val (fn [i j]
                   (if (and (< i n-grid) (< j n-grid) (>= i 0) (>= j 0))
                     (aget densities (+ (* i n-grid) j))
                     0.0))
        interp (fn [v1 v2 p1 p2]
                 ;; Linear interpolation between two points based on threshold.
                 ;; Clamp t to [0,1] to prevent numerical blowup when v1 ≈ v2.
                 (let [t (/ (- threshold v1) (max 1e-15 (- v2 v1)))
                       t (max 0.0 (min 1.0 t))]
                   (+ p1 (* t (- p2 p1)))))]
    (loop [i 0 segments (transient [])]
      (if (>= i (dec n-grid))
        (persistent! segments)
        (let [segs (loop [j 0 segs segments]
                     (if (>= j (dec n-grid))
                       segs
                       ;; Four corners of the cell: TL(i,j) TR(i+1,j) BR(i+1,j+1) BL(i,j+1)
                       (let [tl (grid-val i j)
                             tr (grid-val (inc i) j)
                             br (grid-val (inc i) (inc j))
                             bl (grid-val i (inc j))
                             ;; Corner coordinates in data space (cell center positions)
                             x0 (+ x-lo (* (+ i 0.5) x-step))
                             x1 (+ x-lo (* (+ i 1.5) x-step))
                             y0 (+ y-lo (* (+ j 0.5) y-step))
                             y1 (+ y-lo (* (+ j 1.5) y-step))
                             ;; Cell index (4 bits: TL TR BR BL)
                             idx (bit-or (if (>= tl threshold) 8 0)
                                         (if (>= tr threshold) 4 0)
                                         (if (>= br threshold) 2 0)
                                         (if (>= bl threshold) 1 0))
                             ;; Edge midpoints with interpolation
                             top [(interp tl tr x0 x1) y0]
                             right [x1 (interp tr br y0 y1)]
                             bottom [(interp bl br x0 x1) y1]
                             left [x0 (interp tl bl y0 y1)]]
                         (recur (inc j)
                                (case (int idx)
                                  (0 15) segs
                                  (1 14) (conj! segs [left bottom])
                                  (2 13) (conj! segs [bottom right])
                                  (3 12) (conj! segs [left right])
                                  (4 11) (conj! segs [top right])
                                  ;; Cases 5 and 10 are saddle points — the
                                  ;; level curve has two valid topologies.
                                  ;; Pick by comparing the cell-center value
                                  ;; (average of the 4 corners) to the
                                  ;; threshold: if the center is above, the
                                  ;; two "above" corners connect through the
                                  ;; center and the curve encloses each
                                  ;; "below" corner separately. Otherwise the
                                  ;; roles flip.
                                  5 (let [avg (/ (+ tl tr br bl) 4.0)]
                                      (if (>= avg threshold)
                                        ;; Center above: curves isolate TL and BR (below corners)
                                        (-> segs (conj! [left top]) (conj! [bottom right]))
                                        ;; Center below: curves isolate TR and BL (above corners)
                                        (-> segs (conj! [top right]) (conj! [left bottom]))))
                                  (6 9) (conj! segs [top bottom])
                                  (7 8) (conj! segs [left top])
                                  10 (let [avg (/ (+ tl tr br bl) 4.0)]
                                       (if (>= avg threshold)
                                         ;; Center above: curves isolate TR and BL (below corners)
                                         (-> segs (conj! [top right]) (conj! [left bottom]))
                                         ;; Center below: curves isolate TL and BR (above corners)
                                         (-> segs (conj! [left top]) (conj! [bottom right]))))
                                  segs)))))]
          (recur (inc i) segs))))))

(defn- join-segments
  "Join line segments into polylines by connecting shared endpoints.
   Segments are [[x1 y1] [x2 y2]] pairs. Uses tolerance-based matching
   for floating-point coordinates. Returns a sequence of polylines,
   each a vector of [x y] points."
  [segments]
  (if (empty? segments)
    []
    (let [;; Round coords to 6 decimal places for matching
          round6 (fn [v] (/ (Math/round (* v 1e6)) 1e6))
          snap (fn [[x y]] [(round6 x) (round6 y)])
          ;; Build adjacency: endpoint -> list of (other-endpoint, segment-index)
          adj (reduce
               (fn [m [idx [p1 p2]]]
                 (let [k1 (snap p1) k2 (snap p2)]
                   (-> m
                       (update k1 (fnil conj []) {:other p2 :other-key k2 :idx idx})
                       (update k2 (fnil conj []) {:other p1 :other-key k1 :idx idx}))))
               {}
               (map-indexed vector segments))
          ;; Walk chains greedily
          used (boolean-array (count segments))]
      (loop [remaining (range (count segments))
             polylines []]
        (let [start-idx (first (drop-while #(aget used %) remaining))]
          (if (nil? start-idx)
            polylines
            (do
              (aset used start-idx true)
              (let [[p1 p2] (nth segments start-idx)
                    ;; Walk forward from p2
                    forward
                    (loop [chain [p2]
                           cur-key (snap p2)]
                      (let [neighbors (get adj cur-key [])
                            next-seg (first (filter #(not (aget used (:idx %))) neighbors))]
                        (if next-seg
                          (do
                            (aset used (:idx next-seg) true)
                            (recur (conj chain (:other next-seg))
                                   (:other-key next-seg)))
                          chain)))
                    ;; Walk backward from p1
                    backward
                    (loop [chain []
                           cur-key (snap p1)]
                      (let [neighbors (get adj cur-key [])
                            next-seg (first (filter #(not (aget used (:idx %))) neighbors))]
                        (if next-seg
                          (do
                            (aset used (:idx next-seg) true)
                            (recur (conj chain (:other next-seg))
                                   (:other-key next-seg)))
                          chain)))
                    polyline (vec (concat (rseq backward) [p1] forward))]
                (recur remaining (conj polylines polyline))))))))))

(defmethod extract-layer :contour [draft-layer stat all-colors cfg]
  (let [{:keys [grid fill-range]} stat]
    (if (nil? grid)
      {:mark :contour :levels [] :style {:stroke-width 1.5 :opacity 0.8}}
      (let [{:keys [densities n-grid x-lo x-hi y-lo y-hi x-step y-step max-d]} grid
            n-levels (or (:levels draft-layer) 5)
            max-d (if (and max-d (Double/isFinite max-d) (pos? max-d)) max-d 1.0)
            ;; Threshold levels evenly spaced from 5% to 95% of max density.
            ;; ggplot2 uses a similar approach via pretty() over the density
            ;; range. Starting at 5% (not 0%) avoids noise contours at the
            ;; very edge of the density falloff.
            thresholds (vec (for [i (range n-levels)]
                              (let [frac (+ 0.05 (* 0.9 (/ (double i) (max 1 (dec n-levels)))))]
                                (* max-d frac))))
            levels (vec (for [threshold thresholds
                              :let [t (/ threshold max-d)
                                    segments (marching-squares-segments
                                              densities n-grid threshold
                                              x-lo y-lo x-step y-step)
                                    polylines (join-segments segments)]
                              :when (seq polylines)]
                          {:threshold threshold
                           :t t
                           :color ((defaults/scale-gradient-fn
                                    :color (:color-scale draft-layer) cfg)
                                   t)
                           :polylines (vec polylines)}))]
        {:mark :contour
         :levels levels
         :style {:stroke-width (or (:fixed-size draft-layer) 1.5)
                 :opacity (or (:fixed-alpha draft-layer) 0.8)}
         :x-domain [x-lo x-hi]
         :y-domain [y-lo y-hi]}))))

(defmethod extract-layer :ridgeline [draft-layer stat all-colors cfg]
  (check-stat stat :violins :ridgeline)
  (let [violins (:violins stat)
        categories (:categories stat)]
    {:mark :ridgeline
     :style {:opacity (or (:fixed-alpha draft-layer) 0.7)}
     :ridges (vec (for [v violins]
                    (cond-> {:category (:category v)
                             :color (resolve-color all-colors (:color v) draft-layer cfg)
                             :ys (:ys v)
                             :densities (:densities v)}
                      (:color v) (assoc :color-category (:color v)))))
     :categories (vec categories)}))

(defmethod extract-layer :rug [draft-layer stat all-colors cfg]
  {:mark :rug
   :style {:length (or (:length draft-layer) 6)
           :stroke-width (or (:fixed-size draft-layer) 1.0)
           :opacity (or (:fixed-alpha draft-layer) 0.5)}
   :side (or (:side draft-layer) :x)
   :groups (extract-xy-groups draft-layer stat all-colors cfg)})

(defmethod extract-layer :pointrange [draft-layer stat all-colors cfg]
  {:mark :pointrange
   :style {:radius (or (:fixed-size draft-layer) 3.5)
           :stroke-width 1.5
           :opacity (or (:fixed-alpha draft-layer) 1.0)}
   :groups (extract-xy-groups draft-layer stat all-colors cfg :with-range? true)})

(defmethod extract-layer :interval-h [draft-layer stat all-colors cfg]
  (let [numeric-color? (= (:color-type draft-layer) :numerical)
        all-color-buf (when numeric-color?
                        (let [bufs (keep :color-values (:points stat))]
                          (when (seq bufs) (dtype/concat-buffers bufs))))
        [c-min c-max] (scale/numeric-color-domain
                       (:color-scale draft-layer)
                       (when all-color-buf (dfn/reduce-min all-color-buf))
                       (when all-color-buf (dfn/reduce-max all-color-buf)))
        groups (vec
                (for [{:keys [color xs ys x-ends color-values row-indices]} (:points stat)]
                  (cond-> {:color (resolve-color all-colors color draft-layer cfg)
                           :xs xs :ys ys :x-ends x-ends}
                    row-indices (assoc :row-indices row-indices)
                    (some? color) (assoc :label (defaults/fmt-category-label color))
                    (and numeric-color? color-values)
                    (assoc :colors
                           (vec (map (fn [v]
                                       (let [spec (:color-scale draft-layer)
                                             scale-type (or (:type spec) :linear)
                                             t (defaults/normalize-continuous
                                                scale-type v
                                                (or c-min 0) (or c-max 1)
                                                (defaults/scale-setting :color :midpoint spec cfg))
                                             grad-fn (defaults/scale-gradient-fn :color spec cfg)]
                                         (grad-fn t)))
                                     color-values))))))]
    (when (and (seq groups) (not-any? :x-ends groups))
      (throw (ex-info (str "lay-interval-h requires an :x-end column. "
                           "Pass it as an option: "
                           "(pj/lay-interval-h data :start :task {:x-end :end})")
                      {:mark :interval-h})))
    {:mark :interval-h
     :style {:opacity (or (:fixed-alpha draft-layer) 0.85)
             :interval-thickness (or (:interval-thickness draft-layer) 0.7)}
     :x-temporal? (boolean (:x-temporal? draft-layer))
     :groups groups}))

(defmethod extract-layer :default [draft-layer _stat _all-colors _cfg]
  (let [mark (:mark draft-layer)
        registered (sort (filter keyword?
                                 (remove #(or (vector? %) (= :default %))
                                         (keys (methods extract-layer)))))]
    (throw (ex-info (str "Unknown mark: " (pr-str mark)
                         ". Supported marks: " (vec registered))
                    {:mark mark :supported (vec registered)}))))

