(ns scicloj.plotje.impl.layout
  "Layout inference pipeline. Three pure functions turn scene facts
   and configuration into concrete pixel dimensions.

   Under the new semantics, `:width` and `:height` in opts mean the
   TOTAL SVG dimensions. Panel dimensions are derived by subtracting
   layout overhead (titles, axis labels, legends, facet strips) from
   the total. `:panel-width`/`:panel-height` are escape hatches: when
   set, they pin the panel size on that axis and `:width`/`:height`
   become the derived total.

   The classic width→tick-count→label-width→y-label-pad→panel-width
   cycle is broken by a single reformulation: `max-label-pixel-width`
   runs the tick picker at a pixel budget equal to the user-supplied
   `:height` (or `:width`), not the actual panel size. Label width is
   monotonic non-decreasing in tick count across every scale type we
   support (verified at the REPL), so this over-estimate is always safe.

   Pipeline:

     compute-scene    scene data from resolved draft layers + opts (no pixels)
     compute-padding  scene + cfg + opts -> padding map (no pixel dims yet)
     compute-dims     scene + padding + cfg + opts -> pw/ph/total-w/total-h

   None of these need actual per-panel tick positions -- the tick
   budget is baked into y-label-pad in `compute-padding` via the
   over-estimate trick. Real per-panel ticks are computed AFTER
   `compute-dims` when the final pw/ph are known."
  (:require [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.scale :as scale]
            [wadogo.scale :as ws]))

;; ---- Tick label width over-estimate ----

(defn- fmt-category-label-count
  "Character count of a category label after fmt-category-label. Handles
   keyword/nil/number/string values the same way the renderer will."
  [v]
  (count (defaults/fmt-category-label v)))

(defn- ticks-at-budget
  "Run the tick picker at a given pixel budget for a numeric or
   temporal scale. Returns the tick info map with :labels. This mirrors
   `plan/compute-ticks` but operates without pulling in plan.clj, so
   layout.clj stays acyclic (plan.clj will require layout.clj)."
  [domain scale-spec temporal-extent pixel-budget tick-spacing]
  (let [n (scale/tick-count (double pixel-budget) tick-spacing)
        log? (= :log (:type scale-spec))
        user-breaks (:breaks scale-spec)]
    (cond
      (and user-breaks (sequential? user-breaks) (seq user-breaks))
      (let [vs (vec user-breaks)
            labels (if log?
                     (vec (scale/format-log-ticks vs))
                     (let [s (scale/make-scale domain [0.0 (double pixel-budget)] scale-spec)]
                       (vec (scale/format-ticks s vs))))]
        {:values vs :labels labels})

      temporal-extent
      (if (= (first temporal-extent) (second temporal-extent))
        {:values [(first domain)] :labels [(str (first temporal-extent))]}
        (let [dt-scale (ws/scale :datetime {:domain temporal-extent :range [0.0 1.0]})
              dt-ticks (ws/ticks dt-scale n)
              labels (vec (ws/format dt-scale dt-ticks))]
          {:values dt-ticks :labels labels}))

      log?
      (let [ticks (scale/log-ticks domain n)
            labels (scale/format-log-ticks ticks)]
        {:values (vec ticks) :labels (vec labels)})

      :else
      (let [s (scale/make-scale domain [0.0 (double pixel-budget)] scale-spec)
            ticks (ws/ticks s n)
            labels (scale/format-ticks s ticks)]
        {:values (vec ticks) :labels (vec labels)}))))

(defn max-label-pixel-width
  "Conservative upper bound on the widest tick label, in pixels, for a
   single domain on one axis.

   Runs the tick picker at `pixel-budget` (typically the user-supplied
   `:height` for the y-axis or `:width` for the x-axis) and returns the
   widest resulting label in pixels. Because label character width is
   monotonic non-decreasing in tick count, the pixel budget is always
   >= the eventual panel size and the answer is always >= the real
   max label width.

   For categorical domains the label set is the domain itself; for
   numeric/log/temporal domains the tick picker produces labels that
   depend on the tick count, which in turn depends on the budget."
  [domain scale-spec temporal-extent pixel-budget tick-spacing font-size]
  (cond
    (nil? domain)
    0.0

    (scale/categorical-domain? domain)
    (let [max-chars (reduce max 0 (map fmt-category-label-count domain))]
      (* (double font-size) 0.5 max-chars))

    :else
    (let [tick-info (ticks-at-budget domain scale-spec temporal-extent
                                     pixel-budget tick-spacing)
          max-chars (reduce max 0 (map count (:labels tick-info)))]
      (* (double font-size) 0.5 max-chars))))

(defn max-label-pixel-width-over-panels
  "Maximum of `max-label-pixel-width` across a collection of panel
   domains. Faceted layouts with free scales have different per-panel
   domains but one shared label pad; this reserves enough space for
   the widest panel."
  [panel-domains scale-spec temporal-extent pixel-budget tick-spacing font-size]
  (reduce max 0.0
          (for [d panel-domains]
            (max-label-pixel-width d scale-spec temporal-extent
                                   pixel-budget tick-spacing font-size))))

;; ---- compute-scene ----

(defn compute-scene
  "Pull layout-relevant facts out of the resolved draft layers, grid, labels,
   and legends. Result depends only on the data and the user's options,
   never on pixel math.

   Inputs map keys:

     :layout-type             :single / :facet-grid / :multi-variable
     :grid-rows, :grid-cols   grid dimensions from infer-grid
     :eff-title / :subtitle / :caption / :eff-x-label / :eff-y-label
                              resolved title/label strings (or nil)
     :facet-row-vals / :facet-col-vals  facet axis values
     :coord-type              :cartesian / :flip / :polar / :fixed
     :panel-x-domains         vector of x-domains, one per panel
     :panel-y-domains         vector of y-domains, one per panel
     :x-scale-spec / :y-scale-spec   representative scale specs
     :x-temporal / :y-temporal  temporal extents, if any
     :panel-row-labels / :panel-col-labels  strip labels per panel
     :legend / :size-legend / :alpha-legend  already-built legends

   Output map carries everything compute-padding and compute-dims need."
  [{:keys [layout-type grid-rows grid-cols
           eff-title subtitle caption eff-x-label eff-y-label
           facet-row-vals facet-col-vals
           coord-type
           panel-x-domains panel-y-domains
           x-scale-spec y-scale-spec
           x-temporal y-temporal
           panel-row-labels panel-col-labels
           legend size-legend alpha-legend]}]
  (let [multi? (and (= layout-type :multi-variable)
                    (> grid-cols 1) (> grid-rows 1))
        has-col-strips? (or (and (= layout-type :facet-grid) (seq facet-col-vals))
                            multi?
                            (some some? panel-col-labels))
        has-row-strips? (or (and (= layout-type :facet-grid) (seq facet-row-vals))
                            multi?
                            (some some? panel-row-labels))
        max-row-strip-chars (reduce max 0
                                    (keep #(when % (count %)) panel-row-labels))
        legend-entry-count (cond
                             (:entries legend) (count (:entries legend))
                             (= :continuous (:type legend)) 5
                             :else 0)
        ;; Widest label across title + entry labels. Used to extend
        ;; the legend column width when a category name is longer
        ;; than the fixed default.
        legend-title-chars (count (str (some-> legend :title name)))
        legend-entry-max-chars (reduce max 0
                                       (map #(count (str (:label %)))
                                            (:entries legend)))
        legend-max-chars (max legend-title-chars legend-entry-max-chars)]
    {:layout-type layout-type
     :multi? multi?
     :grid-rows grid-rows
     :grid-cols grid-cols
     :title? (boolean eff-title)
     :subtitle? (boolean subtitle)
     :caption? (boolean caption)
     :x-label? (boolean eff-x-label)
     :y-label? (boolean eff-y-label)
     :has-col-strips? has-col-strips?
     :has-row-strips? has-row-strips?
     :max-row-strip-chars max-row-strip-chars
     :coord-type coord-type
     :panel-x-domains panel-x-domains
     :panel-y-domains panel-y-domains
     :x-scale-spec x-scale-spec
     :y-scale-spec y-scale-spec
     :x-temporal x-temporal
     :y-temporal y-temporal
     :legend-present? (boolean (or legend size-legend alpha-legend))
     :legend-entry-count legend-entry-count
     :legend-max-chars legend-max-chars
     :size-legend-entry-count (count (:entries size-legend))
     :alpha-legend-entry-count (count (:entries alpha-legend))}))

;; ---- compute-padding: one helper per output key ----

(defn- pad-title [{:keys [title?]} cfg]
  ;; Integer-typed where cfg keys are ints so tests with `=` keep working.
  (if title?
    (+ (:title-offset cfg) (:title-font-size cfg))
    0))

(defn- pad-subtitle [{:keys [subtitle?]} _cfg]
  (if subtitle? 16 0))

(defn- pad-caption [{:keys [caption?]} _cfg]
  (if caption? 18 0))

(defn- tick-font-size [cfg]
  (get-in cfg [:theme :font-size]
          (get-in defaults/defaults [:theme :font-size])))

(defn- pad-x-label
  "Vertical space reserved below the bottom row of panels. When a
   global x-label is shown, `:label-offset` covers both the x-axis
   title and the tick labels above it. When there is no global
   x-label (e.g. a multi-variable grid whose columns have different
   x axes, each titled by a strip label at the top), we still need
   room for the x-tick labels themselves; reserve tick-font-size
   plus a small padding."
  [{:keys [x-label?]} cfg]
  (let [base (cond x-label? (:label-offset cfg)
                   :else (+ (tick-font-size cfg) 6))
        angle (get cfg :x-tick-angle 0)
        extra (or (:x-tick-label-height cfg)
                  (long (* 50 (Math/abs (Math/sin (Math/toRadians (double angle)))))))]
    (+ base extra)))

(defn- y-tick-text-width
  "Over-estimate of the widest y-tick label in pixels. Runs the tick
   picker at a pixel budget of `:height` — safe because tick label
   width is monotonic in tick count across every supported scale."
  [{:keys [coord-type panel-y-domains y-scale-spec y-temporal]} cfg opts]
  (if (= coord-type :polar)
    0.0
    (max-label-pixel-width-over-panels
     panel-y-domains y-scale-spec y-temporal
     (double (:height opts))
     (:tick-spacing-y cfg)
     (tick-font-size cfg))))

(defn- pad-y-label
  "y-label-pad = label-offset + max(0, tick-text-width − 12) when a
   y-label is present; otherwise just the tick-text-width (so tick
   labels don't collide with the panel edge). Polar axes suppress
   y-ticks entirely, so the pad is zero."
  [scene cfg opts]
  (let [{:keys [coord-type y-label?]} scene]
    (cond
      (= coord-type :polar) 0.0
      :else (let [tick-w (y-tick-text-width scene cfg opts)]
              (if y-label?
                (+ (double (:label-offset cfg))
                   (max 0.0 (- tick-w 12.0)))
                tick-w)))))

(defn- pad-strip-h [{:keys [has-col-strips?]} cfg]
  (if has-col-strips?
    (:strip-height cfg 16)
    0))

(defn- pad-strip-w [{:keys [has-row-strips? max-row-strip-chars]} cfg]
  (if has-row-strips?
    (let [strip-fsize (double (:strip-font-size cfg 10))
          text-w (* (double max-row-strip-chars) (/ strip-fsize 1.8))]
      (max 40.0 (+ text-w 12.0)))
    0.0))

(defn- resolved-legend-position
  "Effective legend position. Defaults to :right when a legend exists;
   collapses to :none when no legend is present."
  [{:keys [legend-present?]} cfg opts]
  (let [raw (or (:legend-position opts)
                (:legend-position cfg)
                :right)]
    (if (or (not legend-present?) (= raw :none))
      :none
      raw)))

(defn- pad-legend-w
  "Width reserved for a left- or right-positioned legend column.
   The base width from config (default 100) is extended when the
   longest legend label (title or category) exceeds what fits there,
   so long category names like 'tech.ml.dataset.dev' don't clip at
   the SVG right edge."
  [legend-pos scene cfg]
  (if (#{:left :right} legend-pos)
    (let [base (:legend-width cfg 100)
          ;; Swatch + gap takes ~24px; estimate label font advance as
          ;; ~7px per char; leave ~8px right margin.
          char-px 7
          chrome  32
          estimated (+ chrome (* char-px (:legend-max-chars scene 0)))]
      (max base estimated))
    0))

(defn- pad-legend-h
  "Height reserved for top/bottom legends. Parameterized via cfg:
   `:legend-header-pad` (default 20) and `:legend-entry-height`
   (default 18) replace the old magic numbers."
  [legend-pos scene cfg]
  (if (#{:top :bottom} legend-pos)
    (let [{:keys [legend-entry-count size-legend-entry-count
                  alpha-legend-entry-count]} scene
          header (double (:legend-header-pad cfg 20))
          row-h  (double (:legend-entry-height cfg 18))]
      (+ header
         (* row-h (max 1 legend-entry-count))
         (* row-h size-legend-entry-count)
         (* row-h alpha-legend-entry-count)))
    0.0))

(defn compute-padding
  "Pure function from scene + cfg + opts to padding map. Depends only
   on: cfg, opts, and data-derived scene properties. Does NOT depend
   on panel-width or panel-height.

   The y-label-pad's tick-width input uses the user's `:height` as a
   pixel budget rather than the (unknown) real panel height. This is
   the key trick that breaks the `panel-width ↔ y-label-pad` cycle."
  [scene cfg opts]
  (let [legend-pos (resolved-legend-position scene cfg opts)
        legend-h (pad-legend-h legend-pos scene cfg)
        top-pad (if (= legend-pos :top) legend-h 0.0)
        bot-pad (if (= legend-pos :bottom) legend-h 0.0)]
    {:title-pad          (pad-title scene cfg)
     :subtitle-pad       (pad-subtitle scene cfg)
     :caption-pad        (pad-caption scene cfg)
     :x-label-pad        (pad-x-label scene cfg)
     :y-label-pad        (pad-y-label scene cfg opts)
     :strip-h            (pad-strip-h scene cfg)
     :strip-w            (pad-strip-w scene cfg)
     :legend-w           (pad-legend-w legend-pos scene cfg)
     :legend-h           legend-h
     :legend-position    legend-pos
     :top-legend-pad     top-pad
     :bottom-legend-pad  bot-pad}))

;; ---- compute-dims ----

(defn- min-panel-size [cfg]
  (double (:min-panel-size cfg 40)))

(defn- too-small-error
  [axis computed total horiz-overhead grid-n component-breakdown]
  (ex-info
   (str "Computed " (name axis) " is too small: " (Math/round (double computed)) "px. "
        "Total :" (name (if (= axis :panel-width) :width :height)) "="
        (Math/round (double total))
        " minus layout overhead " (Math/round (double horiz-overhead))
        " (" component-breakdown ")"
        " leaves " (Math/round (double (- total horiz-overhead)))
        "px for " grid-n " "
        (if (= axis :panel-width) "column(s)" "row(s)")
        ". Increase :"
        (name (if (= axis :panel-width) :width :height))
        ", set :" (name axis) " directly, or lower :min-panel-size "
        "if you intended a sparkline-sized plot.")
   {axis computed}))

(defn compute-dims
  "Derive panel-width, panel-height, total-width, total-height from
   the user's size intent and the computed padding.

   Precedence:
     1. Explicit `:panel-width` in opts: pins the panel width. The
        user's `:width` becomes the derived total (which can be
        smaller or larger than what they asked for). `:panel-height`
        is symmetric -- the two axes are independent.
     2. Otherwise `:width` and `:height` are the total SVG dimensions
        and apply to every layout type including SPLOMs. Panel
        dimensions are derived by subtracting layout overhead
        (labels, legend, facet strips, title, etc.) from the total
        and dividing by the grid size.

   SPLOMs with many variables may end up with very small panels at
   the default 600x400 total -- raise `:width` / `:height` or set
   `:panel-width` / `:panel-height` explicitly when that happens.
   The `:panel-size` cfg key is no longer consulted; it lingers as
   an ignored legacy key for now.

   Throws with a detailed error when the derived panel is smaller
   than `:min-panel-size` (cfg, default 20) -- the user gave more
   overhead budget than they left space for."
  [scene padding cfg opts]
  (let [{:keys [grid-rows grid-cols]} scene
        {:keys [title-pad subtitle-pad caption-pad x-label-pad y-label-pad
                strip-h strip-w legend-w legend-h
                top-legend-pad bottom-legend-pad]} padding

        width  (double (:width opts))
        height (double (:height opts))

        panel-width-opt  (:panel-width opts)
        panel-height-opt (:panel-height opts)

        horiz-overhead (+ y-label-pad strip-w legend-w)
        vert-overhead  (+ title-pad subtitle-pad top-legend-pad
                          strip-h x-label-pad caption-pad bottom-legend-pad)

        pw (cond
             panel-width-opt   (double panel-width-opt)
             :else             (/ (- width horiz-overhead) grid-cols))
        ph (cond
             panel-height-opt  (double panel-height-opt)
             :else             (/ (- height vert-overhead) grid-rows))

        min-panel (min-panel-size cfg)
        _ (when (< pw min-panel)
            (throw (too-small-error
                    :panel-width pw width horiz-overhead grid-cols
                    (str "y-label-pad=" (Math/round (double y-label-pad))
                         ", strip-w=" (Math/round (double strip-w))
                         ", legend-w=" (Math/round (double legend-w))))))
        _ (when (< ph min-panel)
            (throw (too-small-error
                    :panel-height ph height vert-overhead grid-rows
                    (str "title-pad=" (Math/round (double title-pad))
                         ", x-label-pad=" (Math/round (double x-label-pad))
                         ", strip-h=" (Math/round (double strip-h))
                         ", legend-h=" (Math/round (double legend-h))))))

        ;; When :panel-width is pinned explicitly, the user's :width
        ;; becomes the derived total (horiz-overhead + grid * pw).
        ;; Otherwise :width is the total and we return it unchanged.
        total-w (if panel-width-opt
                  (+ horiz-overhead (* grid-cols pw))
                  width)
        total-h (if panel-height-opt
                  (+ vert-overhead (* grid-rows ph))
                  height)]
    {:pw pw :ph ph
     :total-w total-w :total-h total-h
     :horiz-overhead horiz-overhead
     :vert-overhead vert-overhead}))
