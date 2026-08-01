(ns scicloj.plotje.render.mark
  (:require [clojure.string :as str]
            [membrane.ui :as ui]
            [scicloj.plotje.render.dash :as dash]
            [scicloj.plotje.impl.defaults :as defaults]
            [fastmath.random :as rng]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [wadogo.scale :as ws]))

;; ---- Helpers ----

(defn maybe-dash
  "Wrap a stroked drawable in a stroke-dash pattern when `dash` is set
   (a `[dash gap ...]` vector); otherwise return it unchanged. Placed
   outside the stroke-width wrapper so both backends apply the dash."
  [dash drawable]
  (if dash
    (dash/with-stroke-dash dash drawable)
    drawable))

(defn orient-scales
  "Extract oriented scales from rendering context.
   For categorical marks that need band positioning, returns the
   categorical (band) scale and numeric scale, swapped when flipped.
   Flipped means either `:coord :flip` is in effect, or the panel's
   y-axis is itself a band scale (numerical x + categorical y).
   Returns {:flipped? :band-s :num-s}."
  [ctx]
  (let [sx (:sx ctx) sy (:sy ctx)
        coord-flipped? (= (:coord-type ctx) :flip)
        y-is-band? (boolean (try (ws/data sy :bandwidth) (catch Exception _ nil)))
        flipped? (or coord-flipped? y-is-band?)]
    {:flipped? flipped?
     :band-s (if flipped? sy sx)
     :num-s (if flipped? sx sy)}))

(defn arc-interpolate
  "Subdivide a line segment in pixel space through coord-px reprojection.
   Given two pixel-space endpoints (already in Cartesian panel coords),
   interpolates n intermediate points, projects each through coord-px,
   and returns a seq of [x y] pairs. Used for smooth wedge edges in polar."
  [coord-px px1 py1 px2 py2 n-seg]
  (mapv (fn [i]
          (let [t (/ (double i) (double n-seg))
                px (+ px1 (* t (- px2 px1)))
                py (+ py1 (* t (- py2 py1)))]
            (coord-px px py)))
        (range (inc n-seg))))

(defn bar-polygon
  "Compute polygon points for a rectangular bar, with arc interpolation
   when coord-px is provided (polar). In cartesian/flip, produces a
   simple 4-corner closed polygon.
   - coord-px: pixel-space reprojection fn, or nil for cartesian
   - flipped?: true when axes are swapped
   - cat-lo, cat-hi: pixel range along the categorical (band) axis
   - val-lo, val-hi: pixel range along the numeric (value) axis"
  [coord-px flipped? cat-lo cat-hi val-lo val-hi]
  (if (and coord-px (not flipped?))
    (let [arc-seg 20
          top (arc-interpolate coord-px cat-lo val-hi cat-hi val-hi arc-seg)
          bottom (arc-interpolate coord-px cat-hi val-lo cat-lo val-lo arc-seg)]
      (concat top bottom))
    (let [[x1 y1 x2 y2 x3 y3 x4 y4]
          (if flipped?
            [val-lo cat-lo val-hi cat-lo val-hi cat-hi val-lo cat-hi]
            [cat-lo val-lo cat-hi val-lo cat-hi val-hi cat-lo val-hi])]
      [[x1 y1] [x2 y2] [x3 y3] [x4 y4] [x1 y1]])))

(defn band-position
  "Compute dodged position within a categorical band.
   Returns {:lo :hi :mid} pixel coordinates for one sub-band.
   - band-s: wadogo band scale
   - category: category value to look up
   - group-idx: index of this group within n-groups
   - n-groups: total number of dodge groups
   - frac: fraction of band width to use (e.g. 0.8)"
  [band-s category group-idx n-groups frac]
  (let [bw (ws/data band-s :bandwidth)
        band-info (band-s category true)
        band-start (:rstart band-info)
        band-end (:rend band-info)
        band-mid (/ (+ band-start band-end) 2.0)
        sub-bw (/ (* bw frac) (max 1 n-groups))
        group-start (- band-mid (/ (* n-groups sub-bw) 2.0))
        lo (+ group-start (* group-idx sub-bw))
        hi (+ lo sub-bw)]
    {:lo lo :hi hi :mid (/ (+ lo hi) 2.0) :sub-bw sub-bw}))

(defn- draw-shape
  "Draw a shape symbol centered at (0,0) with given radius."
  [shape-kw r]
  (let [d (* 2 r)]
    (case shape-kw
      :square (ui/with-style ::ui/style-fill
                (ui/rounded-rectangle d d 0))
      :triangle (let [h (* r 1.73)] ;; equilateral triangle height
                  (ui/with-style ::ui/style-fill
                    (ui/path [r 0] [(+ r r) h] [(- r r) h] [r 0])))
      :diamond (ui/with-style ::ui/style-fill
                 (ui/path [r 0] [d r] [r d] [0 r] [r 0]))
      ;; default: circle
      (ui/with-style ::ui/style-fill
        (ui/rounded-rectangle d d r)))))

(defn- fmt-val
  "Format a value for tooltip display."
  [v]
  (cond
    (float? v) (format "%.4g" (double v))
    (double? v) (format "%.4g" (double v))
    :else (str v)))

(defn- make-tooltip
  "Build a tooltip text string from context and point data."
  [ctx x-val y-val color-label]
  (let [{:keys [x-col-name y-col-name]} ctx
        parts (cond-> [(str x-col-name ": " (fmt-val x-val))
                       (str y-col-name ": " (fmt-val y-val))]
                color-label (conj (str "color: " color-label)))]
    (str/join ", " parts)))

(defn- fmt-temporal-val
  "Format a temporalized x value (epoch-ms) as a date string."
  [v]
  (try
    (let [ms (long (double v))
          inst (java.time.Instant/ofEpochMilli ms)
          ldt (java.time.LocalDateTime/ofInstant inst java.time.ZoneOffset/UTC)]
      ;; Drop the time-of-day when it's exactly midnight (LocalDate-only inputs).
      (if (= 0 (.getHour ldt) (.getMinute ldt) (.getSecond ldt))
        (str (.toLocalDate ldt))
        (.toString ldt)))
    (catch Exception _ (fmt-val v))))

(defn- make-interval-tooltip
  "Build a tooltip for a horizontal interval bar -- shows the lane (y),
   the start, the end, and the optional color label. When the x-axis is
   temporal, x-start and x-end are formatted as date strings rather than
   raw epoch-ms doubles."
  [ctx x-temporal? y-cat x-start x-end color-label]
  (let [{:keys [x-col-name y-col-name]} ctx
        fmt-x (if x-temporal? fmt-temporal-val fmt-val)
        parts (cond-> [(str y-col-name ": " (str y-cat))
                       (str x-col-name ": " (fmt-x x-start) " → " (fmt-x x-end))]
                color-label (conj (str "color: " color-label)))]
    (str/join ", " parts)))

;; ---- layer->membrane multimethod ----
;; layer->membrane takes plan layer descriptors (data-space geometry,
;; resolved colors) and renders them as membrane drawable primitives.

(defmulti layer->membrane
  "Convert a plan layer into membrane drawable primitives.
   `layer` is a plan layer map with data-space geometry and resolved colors.
   `ctx` contains :coord-fn, :sx, :sy, :coord-type.
   Dispatches on (:mark layer)."
  (fn [layer ctx] (:mark layer)))

;; ---- Doc defmethods ----
(defmethod layer->membrane [:point :doc] [_ _] "Translated colored rounded-rectangles")
(defmethod layer->membrane [:bar :doc] [_ _] "Filled polygons (histogram bars)")
(defmethod layer->membrane [:line :doc] [_ _] "Stroked polylines")
(defmethod layer->membrane [:rect :doc] [_ _] "Filled polygons (categorical/value bars)")
(defmethod layer->membrane [:text :doc] [_ _] "Translated text labels, on a rounded background box when one is asked for")
(defmethod layer->membrane [:area :doc] [_ _] "Closed filled polygons with baseline")
(defmethod layer->membrane [:boxplot :doc] [_ _] "Box + whiskers + median line + outlier points")
(defmethod layer->membrane [:violin :doc] [_ _] "Mirrored filled density polygon")
(defmethod layer->membrane [:errorbar :doc] [_ _] "Vertical lines with caps")
(defmethod layer->membrane [:lollipop :doc] [_ _] "Stems with dots at category positions")
(defmethod layer->membrane [:tile :doc] [_ _] "Translated colored rectangles (heatmap cells)")
(defmethod layer->membrane [:ridgeline :doc] [_ _] "Overlapping filled density curves")
(defmethod layer->membrane [:rug :doc] [_ _] "Short stroked tick marks at axis margins")
(defmethod layer->membrane [:interval-h :doc] [_ _] "Filled rectangles spanning x to x-end at categorical y")
(defmethod layer->membrane [:step :doc] [_ _] "Stroked step polylines")
(defmethod layer->membrane [:pointrange :doc] [_ _] "Point at mean + vertical SE line")
(defmethod layer->membrane [:contour :doc] [_ _] "Stroked iso-density polylines")
(defmethod layer->membrane [:default :doc] [_ _] "Generic layer fallback")

;; ---- Clip region ----

(defmulti mark-clip-region
  "Names the panel region a mark's geometry is clipped to -- bounded so
   it cannot paint outside the region. The panel renderer resolves the
   name to a rectangle and clips the mark to it. Two regions exist:

   - :drawing-area (the default) -- the grey panel background. The
     right choice for data marks: geometry past the domain is hidden
     at the plotting edge, and nothing spills into the axis margin (the
     band around the plot where ticks and labels sit).
   - :panel-box -- the wider panel rectangle, including that margin.
     For marks that draw in the margin on purpose (rug ticks), so they
     are not cut off at the drawing-area edge.

   This keeps the clip decision with each mark rather than hardcoded
   in the panel renderer. An extension whose mark draws in the margin
   registers (defmethod mark-clip-region :my-mark [_] :panel-box).
   Dispatches on the mark keyword."
  (fn [mark] mark))

(defmethod mark-clip-region :default [_] :drawing-area)
(defmethod mark-clip-region :rug [_] :panel-box)

;; ---- Point ----

(defmethod layer->membrane :point [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [coord-fn tooltip sx sy]} ctx
        {:keys [opacity radius jitter stroke stroke-width]} style
        stroke-rgba (when stroke (defaults/c2d->rgba stroke))
        stroke-w (when stroke (or stroke-width 1))
        ;; Detect if x-axis is categorical (band scale) for smarter jitter
        x-bandwidth (try (ws/data sx :bandwidth) (catch Exception _ nil))
        cat-jitter? (and jitter x-bandwidth)
        jitter-amount (cond cat-jitter? (/ (double x-bandwidth) 3.0)
                            (number? jitter) (double jitter)
                            jitter 5.0
                            :else 0.0)
        jitter? (pos? jitter-amount)
        ;; Dodge support: when dodge-ctx present and x is categorical band scale
        {:keys [n-groups]} (:dodge-ctx layer)
        dodge? (and n-groups x-bandwidth)
        size-log? (= :log (:type (:size-scale layer)))
        alpha-log? (= :log (:type (:alpha-scale layer)))
        size-bufs (keep :sizes groups)
        size-scale (when (seq size-bufs)
                     (let [lo (reduce min (map dfn/reduce-min size-bufs))
                           hi (reduce max (map dfn/reduce-max size-bufs))]
                       (if size-log?
                         (let [lo-l (Math/log10 (max 1e-300 (double lo)))
                               hi-l (Math/log10 (max 1e-300 (double hi)))
                               span (max 1e-6 (- hi-l lo-l))]
                           (fn [v] (+ 2.0 (* 6.0 (/ (- (Math/log10 (max 1e-300 (double v))) lo-l) span)))))
                         (let [span (max 1e-6 (- (double hi) (double lo)))]
                           (fn [v] (+ 2.0 (* 6.0 (/ (- (double v) (double lo)) span))))))))
        alpha-bufs (keep :alphas groups)
        alpha-scale (when (seq alpha-bufs)
                      (let [lo (reduce min (map dfn/reduce-min alpha-bufs))
                            hi (reduce max (map dfn/reduce-max alpha-bufs))]
                        (if alpha-log?
                          (let [lo-l (Math/log10 (max 1e-300 (double lo)))
                                hi-l (Math/log10 (max 1e-300 (double hi)))
                                span (max 1e-6 (- hi-l lo-l))]
                            (fn [v] (+ 0.2 (* 0.8 (/ (- (Math/log10 (max 1e-300 (double v))) lo-l) span)))))
                          (let [span (max 1e-6 (- (double hi) (double lo)))]
                            (fn [v] (+ 0.2 (* 0.8 (/ (- (double v) (double lo)) span))))))))
        shape-bufs (keep :shapes groups)
        shape-map (when (seq shape-bufs)
                    (let [all-vals (distinct (apply concat shape-bufs))]
                      (zipmap all-vals (cycle defaults/shape-syms))))]
    (vec
     (for [{:keys [color colors xs ys sizes alphas shapes row-indices label dodge-idx] :as group} groups
           :let [;; One seeded RNG per group for deterministic jitter
                 jitter-rng (when jitter? (rng/rng :jdk (hash (:color group))))]
           i (range (clojure.core/count xs))
           :let [[px py] (coord-fn (xs i) (ys i))
                 ;; For dodged points on categorical axis, override px
                 px (if dodge?
                      (:mid (band-position sx (xs i) (or dodge-idx 0) n-groups 0.8))
                      px)
                 [px py] (if jitter?
                           (if cat-jitter?
                             ;; Categorical: jitter along band axis only
                             [(+ (double px) (* jitter-amount (- (* 2.0 (rng/drandom jitter-rng)) 1.0)))
                              py]
                             ;; Numeric: jitter both axes
                             [(+ (double px) (* jitter-amount (- (* 2.0 (rng/drandom jitter-rng)) 1.0)))
                              (+ (double py) (* jitter-amount (- (* 2.0 (rng/drandom jitter-rng)) 1.0)))])
                           [px py])
                 pt-r (if sizes (size-scale (sizes i)) radius)
                 pt-alpha (if alphas (alpha-scale (alphas i)) (or opacity 1.0))
                 pt-shape (if shapes (get shape-map (shapes i) :circle) :circle)
                 [cr cg cb _] (if colors (colors i) color)]]
       (-> (ui/translate (- (double px) pt-r) (- (double py) pt-r)
                         [(ui/with-color [cr cg cb pt-alpha]
                            (draw-shape pt-shape pt-r))
                          (when stroke-rgba
                            (let [d (* 2 pt-r)]
                              (ui/with-color stroke-rgba
                                (ui/with-stroke-width stroke-w
                                  (ui/with-style ::ui/style-stroke
                                    (ui/rounded-rectangle d d pt-r))))))])
           (cond-> row-indices (assoc :row-idx (row-indices i))
                   tooltip (assoc :tooltip (make-tooltip ctx (xs i) (ys i) label))))))))

;; ---- Text ----

(defn- text-anchor-offset
  "Pixel offset to add to a text origin (top-left) so the anchored part of
   the text lands on the data point. `text-w`/`text-h` are estimated glyph
   box dimensions. `align-x` (default :left) is :left/:center/:right;
   `align-y` (default :center) is :top/:center/:bottom, data-oriented so
   :top puts the text's top edge at the point (text extends downward)."
  [align-x align-y text-w text-h]
  [(case (or align-x :left)
     :left   0.0
     :center (- (/ (double text-w) 2.0))
     :right  (- (double text-w)))
   (case (or align-y :center)
     :top    0.0
     :center (- (/ (double text-h) 2.0))
     :bottom (- (double text-h)))])

(defn- text-font
  "Membrane Font for a text or label mark. `ui/font` takes only a name and a
   size, so weight and slant are assoc'd onto the record it returns: the
   Java2D backend reads :weight :bold and :slant :italic (see
   membrane.java2d/get-java-font) and the SVG backend maps them to the
   font-weight and font-style attributes."
  [fsize font-weight font-style]
  (cond-> (ui/font nil fsize)
    (= :bold font-weight) (assoc :weight :bold)
    (= :italic font-style) (assoc :slant :italic)))

(defn- text-box
  "The two drawables behind a text mark carrying a `:box`: a white panel and
   its border. Sized from the text's estimated extent plus padding, and
   placed relative to the already-anchored text origin."
  [text-w fsize radius op pad-x pad-y]
  [(ui/translate (- pad-x) (- pad-y)
                 (ui/with-color [1.0 1.0 1.0 (* 0.85 op)]
                   (ui/with-style ::ui/style-fill
                     (ui/rounded-rectangle (+ text-w (* 2 pad-x))
                                           (+ fsize (* 2 pad-y)) radius))))
   ;; The border must say ::ui/style-stroke: a bare ui/rectangle paints
   ;; filled in both backends, so this rect used to cover the white box in
   ;; flat grey instead of outlining it.
   (ui/translate (- pad-x) (- pad-y)
                 (ui/with-color [0.7 0.7 0.7 (* 0.5 op)]
                   (ui/with-style ::ui/style-stroke
                     (ui/rounded-rectangle (+ text-w (* 2 pad-x))
                                           (+ fsize (* 2 pad-y)) radius))))])

(defmethod layer->membrane :text [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [coord-fn]} ctx
        {:keys [font-size opacity align-x align-y font-weight font-style box]} style
        fsize (or font-size 10)
        op (or opacity 1.0)
        font (text-font fsize font-weight font-style)
        radius (double (or (:corner-radius box) 3.0))
        pad-x 3 pad-y 2
        char-w (* fsize 0.6)
        ;; A label naming a dodged bar has to sit over that bar rather than at
        ;; the band centre, so it takes its band-axis position from the same
        ;; band-position the bar marks use, on the same dodge context. The
        ;; convention the bar renderers set holds here too: :xs carries the
        ;; category, :ys the value.
        {:keys [flipped? band-s]} (orient-scales ctx)
        {:keys [n-groups]} (:dodge-ctx layer)
        dodge? (boolean (and n-groups
                             (try (ws/data band-s :bandwidth)
                                  (catch Exception _ nil))))]
    (vec
     (for [{:keys [color xs ys labels dodge-idx]} groups
           i (range (count xs))
           :let [[px py] (coord-fn (xs i) (ys i))
                 [px py] (if dodge?
                           (let [mid (:mid (band-position band-s (xs i)
                                                          (or dodge-idx 0)
                                                          n-groups 0.8))]
                             (if flipped? [px mid] [mid py]))
                           [px py])
                 label (if labels (labels i) "")
                 [cr cg cb _] color
                 text-w (* (count label) char-w)
                 [dx dy] (text-anchor-offset align-x align-y text-w fsize)
                 glyphs (ui/with-color [cr cg cb op] (ui/label label font))]]
       (ui/translate (+ (double px) dx) (+ (double py) dy)
                     (if box
                       (conj (text-box text-w fsize radius op pad-x pad-y) glyphs)
                       glyphs))))))

;; ---- Area ----

(defn- area-outline
  "Optional stroked polyline over an area's top curve (opt-in via :stroke).
   Strokes only the curve, not the baseline -- like ggplot outline.type
   \"upper\" and the requested fill+outline look. Returns nil when no
   :stroke-color is set."
  [style top-pts]
  (let [{:keys [stroke-color stroke-width dash]} style]
    (when (and stroke-color (>= (count top-pts) 2))
      (let [[sr sg sb sa] stroke-color]
        (maybe-dash dash
                    (ui/with-color [sr sg sb (or sa 1.0)]
                      (ui/with-stroke-width (or stroke-width 2)
                        (ui/with-style ::ui/style-stroke
                          (apply ui/path top-pts)))))))))

(defmethod layer->membrane :area [layer ctx]
  (let [{:keys [style groups position]} layer
        {:keys [coord-fn y-domain-min]} ctx
        {:keys [opacity]} style
        baseline 0]
    (if (and (#{:stack :fill} position) (some :y0s groups))
      ;; Stacked: use pre-computed y0s from position/apply-positions
      (vec
       (mapcat
        (fn [{:keys [color xs ys y0s]}]
          (let [top-pts (map (fn [x y] (coord-fn x y)) xs ys)
                base-pts (reverse (map (fn [x y0] (coord-fn x y0)) xs y0s))
                all-pts (concat top-pts base-pts)
                [cr cg cb _] color]
            (keep identity
                  [(ui/with-color [cr cg cb (or opacity 0.5)]
                     (ui/with-style ::ui/style-fill
                       (apply ui/path all-pts)))
                   (area-outline style top-pts)])))
        groups))
      ;; Non-stacked: each group has baseline at y-domain-min
      (vec
       (mapcat
        (fn [{:keys [color xs ys]}]
          (let [sorted (sort-by first (map vector xs ys))
                top-pts (map (fn [[x y]] (coord-fn x y)) sorted)
                x-first (ffirst sorted)
                x-last (first (last sorted))
                [bx-right by-right] (coord-fn x-last baseline)
                [bx-left by-left] (coord-fn x-first baseline)
                all-pts (concat top-pts [[bx-right by-right] [bx-left by-left]])
                [cr cg cb _] color]
            (keep identity
                  [(ui/with-color [cr cg cb (or opacity 0.5)]
                     (ui/with-style ::ui/style-fill
                       (apply ui/path all-pts)))
                   (area-outline style top-pts)])))
        groups)))))

;; ---- Errorbar ----

(defmethod layer->membrane :errorbar [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [coord-fn sx sy]} ctx
        {:keys [flipped?]} (orient-scales ctx)
        {:keys [stroke-width cap-width opacity]} style
        sw (or stroke-width 1.5)
        op (or opacity 1.0)
        cap-hw (/ (or cap-width 6) 2.0)
        ;; Dodge support: when dodge-ctx is present, use band-position on
        ;; the categorical axis. Under flip the categorical axis is y.
        {:keys [n-groups]} (:dodge-ctx layer)
        cat-scale (if flipped? sy sx)
        band-scale? (and n-groups (try (ws/data cat-scale :bandwidth) (catch Exception _ nil)))]
    (vec
     (for [{:keys [color xs ys ymins ymaxs dodge-idx]} groups
           i (range (clojure.core/count xs))
           :let [x (xs i)
                 ymin-val (ymins i)
                 ymax-val (ymaxs i)
                 ;; Pixel coords of the endpoints: coord-fn already
                 ;; swaps x and y under :coord :flip, so we just read
                 ;; the two ends from coord-fn and use them directly.
                 [px-min py-min] (coord-fn x ymin-val)
                 [px-max py-max] (coord-fn x ymax-val)
                 ;; For dodged errorbars on the categorical band, override
                 ;; the category coordinate with band-position.
                 [px-min py-min px-max py-max]
                 (if band-scale?
                   (let [bp (:mid (band-position cat-scale x (or dodge-idx 0) n-groups 0.8))]
                     (if flipped?
                       [px-min bp px-max bp]
                       [bp py-min bp py-max]))
                   [px-min py-min px-max py-max])
                 [cr cg cb _] color]]
       (ui/with-color [cr cg cb op]
         (if flipped?
           ;; Horizontal range bar: line from (px-min, py) to (px-max, py),
           ;; caps at each end drawn as short vertical segments.
           [(ui/with-style ::ui/style-stroke
              (ui/with-stroke-width sw
                (ui/path [px-min py-min] [px-max py-max])))
            (ui/with-style ::ui/style-stroke
              (ui/with-stroke-width sw
                (ui/path [px-min (- (double py-min) cap-hw)]
                         [px-min (+ (double py-min) cap-hw)])))
            (ui/with-style ::ui/style-stroke
              (ui/with-stroke-width sw
                (ui/path [px-max (- (double py-max) cap-hw)]
                         [px-max (+ (double py-max) cap-hw)])))]
           ;; Vertical range bar (normal orientation).
           [(ui/with-style ::ui/style-stroke
              (ui/with-stroke-width sw
                (ui/path [px-min py-min] [px-max py-max])))
            (ui/with-style ::ui/style-stroke
              (ui/with-stroke-width sw
                (ui/path [(- (double px-min) cap-hw) py-min]
                         [(+ (double px-min) cap-hw) py-min])))
            (ui/with-style ::ui/style-stroke
              (ui/with-stroke-width sw
                (ui/path [(- (double px-max) cap-hw) py-max]
                         [(+ (double px-max) cap-hw) py-max])))]))))))

;; ---- Lollipop ----

(defmethod layer->membrane :lollipop [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [flipped? band-s num-s]} (orient-scales ctx)
        {:keys [radius stroke-width opacity]} style
        {:keys [n-groups] :or {n-groups (clojure.core/count groups)}} (:dodge-ctx layer)
        r (or radius 4)
        op (or opacity 1.0)]
    (vec
     (for [group groups
           i (range (clojure.core/count (:xs group)))
           :let [[cr cg cb _] (:color group)
                 cat (nth (:xs group) i)
                 val (nth (:ys group) i)
                 dodge-idx (or (:dodge-idx group) 0)
                 bp (band-position band-s cat dodge-idx n-groups 0.8)
                 cat-pos (:mid bp)
                 val-base (num-s 0)
                 val-top (num-s val)]]
       [(ui/with-color [cr cg cb op]
          (ui/with-style ::ui/style-stroke
            (ui/with-stroke-width (or stroke-width 1.5)
              (if flipped?
                (ui/path [val-base cat-pos] [val-top cat-pos])
                (ui/path [cat-pos val-base] [cat-pos val-top])))))
        (ui/translate (if flipped? (- (double val-top) r) (- (double cat-pos) r))
                      (if flipped? (- (double cat-pos) r) (- (double val-top) r))
                      (ui/with-color [cr cg cb op]
                        (ui/with-style ::ui/style-fill
                          (ui/rounded-rectangle (* 2 r) (* 2 r) r))))]))))

;; ---- Boxplot ----

(defmethod layer->membrane :boxplot [layer ctx]
  (let [{:keys [style boxes color-categories]} layer
        {:keys [flipped? band-s num-s]} (orient-scales ctx)
        {:keys [box-width stroke-width opacity]} style
        box-frac (or box-width 0.6)
        sw (or stroke-width 1.5)
        op (or opacity 1.0)
        ;; n-groups comes from dodge-ctx (set by position.clj when dodge is active).
        ;; When dodge-ctx is absent (position = :identity), use 1 — the mark
        ;; should fill the full band, not subdivide by color-categories.
        {:keys [n-groups] :or {n-groups 1}}
        (:dodge-ctx layer)]
    (vec
     (mapcat
      (fn [{:keys [category color dodge-idx color-category median q1 q3 whisker-lo whisker-hi outliers]}]
        (let [[cr cg cb _] color
              ci (or dodge-idx 0)
              bp (band-position band-s category ci n-groups box-frac)
              box-lo (:lo bp)
              box-hi (:hi bp)
              box-mid (:mid bp)
              sub-bw (:sub-bw bp)
              ;; Y positions via numeric scale
              py-q1 (num-s q1)
              py-q3 (num-s q3)
              py-med (num-s median)
              py-wlo (num-s whisker-lo)
              py-whi (num-s whisker-hi)
              ;; Build primitives depending on orientation
              mk-box (fn [x1 y1 x2 y2 x3 y3 x4 y4]
                       [(ui/with-color [cr cg cb (* 0.7 op)]
                          (ui/with-style ::ui/style-fill
                            (ui/path [x1 y1] [x2 y2] [x3 y3] [x4 y4] [x1 y1])))
                        (ui/with-color [cr cg cb op]
                          (ui/with-stroke-width sw
                            (ui/with-style ::ui/style-stroke
                              (ui/path [x1 y1] [x2 y2] [x3 y3] [x4 y4] [x1 y1]))))])
              mk-line (fn [xa ya xb yb]
                        (ui/with-color [cr cg cb op]
                          (ui/with-stroke-width sw
                            (ui/with-style ::ui/style-stroke
                              (ui/path [xa ya] [xb yb])))))
              mk-point (fn [px py]
                         (let [r 2.5 d (* 2 r)]
                           (ui/translate (- (double px) r) (- (double py) r)
                                         (ui/with-color [cr cg cb op]
                                           (ui/with-style ::ui/style-fill
                                             (ui/rounded-rectangle d d r))))))]
          (if flipped?
            (concat
             (mk-box py-q1 box-lo py-q3 box-lo py-q3 box-hi py-q1 box-hi)
             [(mk-line py-med box-lo py-med box-hi)
              (mk-line py-wlo box-mid py-q1 box-mid)
              (mk-line py-whi box-mid py-q3 box-mid)
              (mk-line py-wlo (- box-mid (* sub-bw 0.15))
                       py-wlo (+ box-mid (* sub-bw 0.15)))
              (mk-line py-whi (- box-mid (* sub-bw 0.15))
                       py-whi (+ box-mid (* sub-bw 0.15)))]
             (for [o outliers] (mk-point (num-s o) box-mid)))
            (concat
             (mk-box box-lo py-q3 box-hi py-q3 box-hi py-q1 box-lo py-q1)
             [(mk-line box-lo py-med box-hi py-med)
              (mk-line box-mid py-q1 box-mid py-wlo)
              (mk-line box-mid py-q3 box-mid py-whi)
              (mk-line (- box-mid (* sub-bw 0.15)) py-wlo
                       (+ box-mid (* sub-bw 0.15)) py-wlo)
              (mk-line (- box-mid (* sub-bw 0.15)) py-whi
                       (+ box-mid (* sub-bw 0.15)) py-whi)]
             (for [o outliers] (mk-point box-mid (num-s o)))))))
      boxes))))

;; ---- Violin ----

(defmethod layer->membrane :violin [layer ctx]
  (let [{:keys [style violins color-categories]} layer
        {:keys [flipped? band-s num-s]} (orient-scales ctx)
        {:keys [opacity stroke-width]} style
        ;; See boxplot comment — n-groups defaults to 1 when no dodge-ctx.
        {:keys [n-groups] :or {n-groups 1}}
        (:dodge-ctx layer)]
    (vec
     (mapcat
      (fn [{:keys [category color dodge-idx color-category ys densities]}]
        (let [[cr cg cb _] color
              ci (or dodge-idx 0)
              bp (band-position band-s category ci n-groups 0.8)
              viol-mid (:mid bp)
              half-w (/ (:sub-bw bp) 2.0)
              ;; Normalize densities so max density fills half the band width
              max-d (max 0.001 (dfn/reduce-max densities))
              norm (/ half-w max-d)
              ;; Build mirrored polygon points
              n (count ys)
              right-pts (mapv (fn [i]
                                (let [y-val (ys i)
                                      d (densities i)
                                      py (num-s y-val)
                                      px (+ viol-mid (* d norm))]
                                  (if flipped? [py px] [px py])))
                              (range n))
              left-pts (mapv (fn [i]
                               (let [y-val (ys (- n 1 i))
                                     d (densities (- n 1 i))
                                     py (num-s y-val)
                                     px (- viol-mid (* d norm))]
                                 (if flipped? [py px] [px py])))
                             (range n))
              all-pts (concat right-pts left-pts)]
          [(ui/with-color [cr cg cb (or opacity 0.7)]
             (ui/with-style ::ui/style-fill
               (apply ui/path all-pts)))
           (ui/with-color [cr cg cb 1.0]
             (ui/with-stroke-width (or stroke-width 1.0)
               (ui/with-style ::ui/style-stroke
                 (apply ui/path all-pts))))]))
      violins))))

;; ---- Tile (heatmap) ----

(defmethod layer->membrane :tile [layer ctx]
  (let [{:keys [style tiles]} layer
        {:keys [coord-fn panel-width panel-height margin]} ctx
        {:keys [opacity]} style
        m (double (or margin 0))
        ;; Drawing area bounds for clamping
        x-lo-clip m
        y-lo-clip m
        x-hi-clip (- (double panel-width) m)
        y-hi-clip (- (double panel-height) m)]
    (vec
     (for [{:keys [x-lo x-hi y-lo y-hi color]} tiles
           :let [[px1 py1] (coord-fn x-lo y-lo)
                 [px2 py2] (coord-fn x-hi y-hi)
                 ;; Clamp to drawing area
                 rx1 (max x-lo-clip (min px1 px2))
                 ry1 (max y-lo-clip (min py1 py2))
                 rx2 (min x-hi-clip (max px1 px2))
                 ry2 (min y-hi-clip (max py1 py2))
                 w (- rx2 rx1)
                 h (- ry2 ry1)
                 [cr cg cb _] color]
           :when (and (pos? w) (pos? h))]
       (ui/translate rx1 ry1
                     (ui/with-color [cr cg cb (or opacity 1.0)]
                       (ui/with-style ::ui/style-fill
                         (ui/rectangle w h))))))))

(defmethod layer->membrane :contour [layer ctx]
  (let [{:keys [levels style]} layer
        {:keys [coord-fn]} ctx
        {:keys [stroke-width opacity]} style]
    (vec
     (for [{:keys [color polylines]} levels
           :let [[cr cg cb _] color]
           polyline polylines
           :let [pts (mapv (fn [[x y]] (coord-fn x y)) polyline)]
           :when (>= (count pts) 2)]
       (ui/with-color [cr cg cb (or opacity 0.8)]
         (ui/with-stroke-width (or stroke-width 1.5)
           (ui/with-style ::ui/style-stroke
             (apply ui/path pts))))))))

;; ---- Ridgeline ----

(defn ridgeline-positions
  "Compute ridgeline band positions for a list of categories.
   Returns a vector of [category-name {:mid pixel-y :bw band-width}]
   pairs, preserving the input category order. Used by both the
   ridgeline renderer and panel tick label placement.

   Returns a vector (not a map) so iteration order is deterministic
   regardless of category count -- `(into (array-map) ...)` quietly
   promotes to PersistentHashMap on the 9th key, scrambling the
   order via Clojure hash. Callers do `(get pos cat)` style lookup
   so they need to convert to a map locally if random access matters."
  [categories panel-height margin]
  (let [n-cats (count categories)
        m (or margin 25)
        ph (or panel-height 400)
        overlap 1.5
        usable (- (double ph) (* 2.0 m))
        bw (/ usable (+ (double n-cats) (* 2.0 overlap) -1.0))
        pad (* bw (- overlap 0.5))]
    (vec (map-indexed
          (fn [i cat]
            [cat {:mid (+ m pad (* bw (+ (double i) 0.5)))
                  :bw bw}])
          categories))))

(defmethod layer->membrane :ridgeline [layer ctx]
  (if (empty? (:ridges layer))
    []
    (let [{:keys [style ridges categories]} layer
          {:keys [sx sy margin panel-height]} ctx
          {:keys [opacity]} style
          m (or margin 25)
          ph (or panel-height 400)
          cat-pos-pairs (ridgeline-positions categories ph m)
          cat-positions (into {} cat-pos-pairs)
          ;; Overlap factor and normalization
          overlap 1.5
          bw (or (:bw (second (first cat-pos-pairs))) 1.0)
          ;; Max density across all ridges for normalization
          all-densities (seq (keep #(when (seq (:densities %)) (:densities %)) ridges))
          max-d (if all-densities
                  (max 0.001 (dfn/reduce-max (dtype/concat-buffers all-densities)))
                  0.001)
          norm (* bw overlap (/ 1.0 max-d))
          ;; After domain swap in plan, sx is the numeric scale (maps to x-pixels)
          ;; and sy is categorical. Use sx for numeric value → pixel mapping.
          num-scale sx]
      (vec
       ;; Render from back (last category) to front (first category)
       ;; so that front ridges overlap back ones
       (for [ridge (reverse ridges)
             :let [{:keys [category color ys densities]} ridge
                   {:keys [mid]} (get cat-positions category)
                   n (count ys)
                   [cr cg cb _] color
                   ;; Build polygon: baseline at mid, curve goes upward (toward lower px)
                   curve-pts (mapv (fn [i]
                                     (let [y-val (ys i)
                                           d (densities i)
                                           py (num-scale y-val)
                                           px (- (double mid) (* d norm))]
                                       [py px]))
                                   (range n))
                   ;; Baseline points (flat line at mid)
                   base-pts [(let [py (num-scale (ys (dec n)))] [py mid])
                             (let [py (num-scale (ys 0))] [py mid])]
                   all-pts (concat curve-pts base-pts)]]
         [(ui/with-color [cr cg cb (or opacity 0.7)]
            (ui/with-style ::ui/style-fill
              (apply ui/path all-pts)))
          (ui/with-color [cr cg cb 1.0]
            (ui/with-stroke-width 1.0
              (ui/with-style ::ui/style-stroke
                (apply ui/path curve-pts))))])))))

;; ---- Rug ----

(defmethod layer->membrane :rug [layer ctx]
  (let [{:keys [style groups side]} layer
        {:keys [coord-fn panel-width panel-height margin]} ctx
        {:keys [length stroke-width opacity]} style
        len (or length 6)]
    (vec
     (for [{:keys [color xs ys]} groups
           i (range (count xs))
           :let [[cr cg cb _] color
                 [px py] (coord-fn (xs i) (ys i))]
           tick (cond-> []
                  (#{:x :both} (or side :x))
                  (conj (ui/with-color [cr cg cb (or opacity 0.5)]
                          (ui/with-stroke-width (or stroke-width 1.0)
                            (ui/with-style ::ui/style-stroke
                              (ui/path [px (- (double panel-height) (double margin))]
                                       [px (- (double panel-height) (double margin) (- len))])))))
                  (#{:y :both} (or side :x))
                  (conj (ui/with-color [cr cg cb (or opacity 0.5)]
                          (ui/with-stroke-width (or stroke-width 1.0)
                            (ui/with-style ::ui/style-stroke
                              (ui/path [(double margin) py]
                                       [(+ (double margin) len) py]))))))]
       tick))))

;; ---- Pointrange (dot + vertical line from ymin to ymax) ----

(defmethod layer->membrane :pointrange [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [coord-fn sx sy]} ctx
        {:keys [flipped?]} (orient-scales ctx)
        {:keys [radius stroke-width opacity]} style
        r (or radius 3.5)
        sw (or stroke-width 1.5)
        op (or opacity 1.0)
        ;; Dodge support: band scale is on the categorical axis, which
        ;; is y under :coord :flip.
        {:keys [n-groups]} (:dodge-ctx layer)
        cat-scale (if flipped? sy sx)
        band-scale? (and n-groups (try (ws/data cat-scale :bandwidth) (catch Exception _ nil)))]
    (vec
     (for [{:keys [color xs ys ymins ymaxs dodge-idx]} groups
           i (range (count xs))
           :let [[cr cg cb _] color
                 x (xs i)
                 y (ys i)
                 ymin-val (ymins i)
                 ymax-val (ymaxs i)
                 [px py] (coord-fn x y)
                 [px-min py-min] (coord-fn x ymin-val)
                 [px-max py-max] (coord-fn x ymax-val)
                 [px py px-min py-min px-max py-max]
                 (if band-scale?
                   (let [bp (:mid (band-position cat-scale x (or dodge-idx 0) n-groups 0.8))]
                     (if flipped?
                       [px bp px-min bp px-max bp]
                       [bp py bp py-min bp py-max]))
                   [px py px-min py-min px-max py-max])]]
       [(ui/with-color [cr cg cb op]
          (ui/with-stroke-width sw
            (ui/with-style ::ui/style-stroke
              (ui/path [px-min py-min] [px-max py-max]))))
        (ui/translate (- (double px) r) (- (double py) r)
                      (ui/with-color [cr cg cb op]
                        (ui/with-style ::ui/style-fill
                          (ui/rounded-rectangle (* 2 r) (* 2 r) r))))]))))

(defmethod layer->membrane :default [layer ctx]
  (let [m (:mark layer)]
    (if (and (vector? m) (= :doc (second m)))
      "(no description)"
      (throw (ex-info (str "No layer->membrane renderer for mark " (pr-str m)
                           ". Define (defmethod layer->membrane " (pr-str m) " [layer ctx] ...)")
                      {:mark m})))))

;; ---- Bar (histogram) ----

(defmethod layer->membrane :bar [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [sx sy y-domain-min]} ctx
        coord-px (:coord-px ctx)
        {:keys [opacity]} style
        ;; Bar baseline. On a linear axis, :bar is in zero-baseline-marks
        ;; so the domain already includes 0. On a log axis (no zero), the
        ;; baseline is the panel's smallest positive value -- bars rest
        ;; on the axis bottom rather than vanishing.
        y-base (max 0.0 (double y-domain-min))]
    (vec
     (for [{:keys [color bars]} groups
           {:keys [lo hi count]} bars
           :let [pts (bar-polygon coord-px false (sx lo) (sx hi) (sy y-base) (sy count))
                 [cr cg cb _] color]]
       (ui/with-color [cr cg cb (or opacity 1.0)]
         (ui/with-style ::ui/style-fill
           (apply ui/path pts)))))))

;; ---- Line ----

(defmethod layer->membrane :line [layer ctx]
  (let [{:keys [style groups ribbons position]} layer
        {:keys [coord-fn]} ctx
        {:keys [stroke-width opacity dash]} style
        op (or opacity 1.0)
        ;; Render ribbons first (behind lines)
        ribbon-elems
        (when ribbons
          (for [{:keys [color xs ymins ymaxs]} ribbons
                :let [[cr cg cb _] color
                      top-pts (mapv (fn [x y] (coord-fn x y)) xs ymaxs)
                      bot-pts (reverse (mapv (fn [x y] (coord-fn x y)) xs ymins))
                      all-pts (concat top-pts bot-pts)]]
            (ui/with-color [cr cg cb (* 0.2 op)]
              (ui/with-style ::ui/style-fill
                (apply ui/path all-pts)))))
        ;; Stacked fill areas (behind lines, when position is :stack/:fill)
        stack-fill-elems
        (when (and (#{:stack :fill} position) (some :y0s groups))
          (for [{:keys [color xs ys y0s]} groups
                :let [top-pts (map (fn [x y] (coord-fn x y)) xs ys)
                      base-pts (reverse (map (fn [x y0] (coord-fn x y0)) xs y0s))
                      all-pts (concat top-pts base-pts)
                      [cr cg cb _] color]]
            (ui/with-color [cr cg cb (* 0.3 op)]
              (ui/with-style ::ui/style-fill
                (apply ui/path all-pts)))))
        ;; Render lines on top
        line-elems
        (for [group groups
              :let [[cr cg cb _] (:color group)]]
          (if (:x1 group)
            ;; Regression line segment
            (let [{:keys [x1 y1 x2 y2]} group
                  [px1 py1] (coord-fn x1 y1)
                  [px2 py2] (coord-fn x2 y2)]
              (maybe-dash dash
                          (ui/with-color [cr cg cb op]
                            (ui/with-stroke-width (or stroke-width 2)
                              (ui/with-style ::ui/style-stroke
                                (ui/path [px1 py1] [px2 py2]))))))
            ;; Polyline (connected points)
            (let [{:keys [xs ys]} group
                  projected (sort-by first (map coord-fn xs ys))]
              (maybe-dash dash
                          (ui/with-color [cr cg cb op]
                            (ui/with-stroke-width (or stroke-width 2)
                              (ui/with-style ::ui/style-stroke
                                (apply ui/path projected))))))))]
    (vec (concat ribbon-elems stack-fill-elems line-elems))))

;; ---- Step Line ----

(defmethod layer->membrane :step [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [coord-fn]} ctx
        {:keys [stroke-width opacity dash]} style
        op (or opacity 1.0)]
    (vec
     (for [{:keys [color xs ys]} groups
           :let [[cr cg cb _] color
                 sorted (sort-by first (map vector xs ys))
                 ;; Build step path: for each pair of consecutive points,
                 ;; go horizontal first, then vertical
                 step-pts (loop [pts []
                                 remaining sorted]
                            (if (empty? remaining)
                              pts
                              (let [[x y] (first remaining)
                                    [px py] (coord-fn x y)]
                                (if (empty? pts)
                                  (recur [[px py]] (rest remaining))
                                  (let [[prev-px _prev-py] (last pts)]
                                    ;; Horizontal to new x at old y, then vertical to new y
                                    (recur (conj pts [px (second (last pts))] [px py])
                                           (rest remaining)))))))]]
       (when (>= (count step-pts) 2)
         (maybe-dash dash
                     (ui/with-color [cr cg cb op]
                       (ui/with-stroke-width (or stroke-width 2)
                         (ui/with-style ::ui/style-stroke
                           (apply ui/path step-pts))))))))))

;; ---- Rect (categorical bars / value bars) ----

(defn layer->membrane-categorical-bars
  "Render categorical count bars from a plan :rect layer.
   Stack: reads pre-computed :y0/:y1 from position adjustment.
   Dodge: reads :dodge-idx/:dodge-ctx from position adjustment."
  [layer ctx]
  (let [{:keys [style groups position categories]} layer
        {:keys [flipped? band-s num-s]} (orient-scales ctx)
        {:keys [opacity]} style
        coord-px (:coord-px ctx)
        position (or position :dodge)
        ;; Numeric-axis baseline: 0 on linear (extended into domain by
        ;; zero-baseline-marks rule), smallest positive on log (where
        ;; 0 has no representation).
        num-base (max 0.0 (double (if flipped?
                                    (:x-domain-min ctx 0)
                                    (:y-domain-min ctx 0))))
        mk-rect (fn [[cr cg cb _] cat-lo cat-hi val-lo val-hi]
                  (let [pts (bar-polygon coord-px flipped? cat-lo cat-hi val-lo val-hi)]
                    (ui/with-color [cr cg cb (or opacity 1.0)]
                      (ui/with-style ::ui/style-fill
                        (apply ui/path pts)))))]
    (if (#{:stack :fill} position)
      ;; Stacked: positions pre-computed by position/apply-positions
      (vec
       (for [group groups
             {:keys [category y0 y1]} (:counts group)
             :when (and y0 y1 (not= y0 y1))]
         (let [bp (band-position band-s category 0 1 0.8)
               y0' (max num-base (double y0))]
           (mk-rect (:color group) (:lo bp) (:hi bp)
                    (num-s y0') (num-s y1)))))
      ;; Dodged: use dodge-ctx annotations from position/apply-positions
      (let [{:keys [n-groups] :or {n-groups 1}} (:dodge-ctx layer)
            frac 0.8]
        (vec
         (for [group groups
               {:keys [category count]} (:counts group)
               :when (pos? count)]
           (let [dodge-idx (or (:dodge-idx group) 0)
                 bp (band-position band-s category dodge-idx n-groups frac)]
             (mk-rect (:color group) (:lo bp) (:hi bp)
                      (num-s num-base) (num-s count)))))))))

(defn layer->membrane-value-bars
  "Render value bars from a plan :rect layer."
  [layer ctx]
  (let [{:keys [style groups]} layer
        {:keys [flipped? band-s num-s]} (orient-scales ctx)
        {:keys [opacity]} style
        coord-px (:coord-px ctx)
        {:keys [n-groups] :or {n-groups (clojure.core/count groups)}} (:dodge-ctx layer)
        frac 0.8
        num-base (max 0.0 (double (if flipped?
                                    (:x-domain-min ctx 0)
                                    (:y-domain-min ctx 0))))]
    (vec
     (for [group groups
           i (range (clojure.core/count (:xs group)))
           :let [[cr cg cb _] (:color group)
                 cat (nth (:xs group) i)
                 val (nth (:ys group) i)
                 base (max num-base
                           (double (if-let [y0s (:y0s group)] (nth y0s i) 0)))
                 dodge-idx (or (:dodge-idx group) 0)
                 bp (band-position band-s cat dodge-idx n-groups frac)
                 pts (bar-polygon coord-px flipped? (:lo bp) (:hi bp) (num-s base) (num-s val))]]
       (ui/with-color [cr cg cb (or opacity 1.0)]
         (ui/with-style ::ui/style-fill
           (apply ui/path pts)))))))

(defmethod layer->membrane :rect [layer ctx]
  (if (:categories layer)
    (layer->membrane-categorical-bars layer ctx)
    (layer->membrane-value-bars layer ctx)))

;; ---- Interval-h (Gantt-style horizontal bars) ----

(defmethod layer->membrane :interval-h [layer ctx]
  (let [{:keys [style groups x-temporal?]} layer
        {:keys [sx sy tooltip]} ctx
        coord-px (:coord-px ctx)
        ;; Under default coords the lane is on sy; under :coord :flip
        ;; the panel has already swapped domains so the lane lives on sx.
        ;; Detect either way by looking for which scale carries a band.
        sy-band? (try (ws/data sy :bandwidth) (catch Exception _ nil))
        sx-band? (try (ws/data sx :bandwidth) (catch Exception _ nil))
        band-s (cond sy-band? sy sx-band? sx :else nil)
        num-s (if (= band-s sy) sx sy)
        ;; bar-polygon's `flipped?` means "cat axis is vertical pixels".
        bp-flipped? (= band-s sy)
        {:keys [opacity interval-thickness]} style
        op (or opacity 0.85)
        frac (or interval-thickness 0.7)]
    (when-not band-s
      (throw (ex-info (str "lay-interval-h requires a categorical :y column "
                           "(use a string/keyword column, or pass {:y-type :categorical}).")
                      {:mark :interval-h})))
    (vec
     (for [{:keys [color colors xs ys x-ends row-indices label]} groups
           i (range (clojure.core/count xs))
           :let [x-start (xs i)
                 x-end (x-ends i)
                 y-cat (ys i)
                 ;; Per-row :colors (numeric color gradient) wins; otherwise
                 ;; the group's resolved fixed/categorical color applies.
                 [cr cg cb _] (if colors (nth colors i) color)
                 bp (band-position band-s y-cat 0 1 frac)
                 pts (bar-polygon coord-px bp-flipped?
                                  (:lo bp) (:hi bp)
                                  (num-s x-start) (num-s x-end))]]
       (-> (ui/translate 0 0
                         [(ui/with-color [cr cg cb op]
                            (ui/with-style ::ui/style-fill
                              (apply ui/path pts)))])
           (cond->
            row-indices (assoc :row-idx (row-indices i))
            tooltip     (assoc :tooltip (make-interval-tooltip
                                         ctx x-temporal? y-cat x-start x-end label))))))))
