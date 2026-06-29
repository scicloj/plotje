;; # Known Limitations
;;
;; Gaps in the current Plotje release. None produce crashes on
;; canonical inputs; each is documented and tracked for post-alpha
;; work.

(ns plotje-book.known-limitations
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]))

;; ## Layout and Visuals
;;
;; - Multi-layer overlays like
;;   `(-> data (pj/lay-point ...) (pj/lay-smooth {:stat :linear-model}) (pj/lay-smooth {:stat :loess}))`
;;   do not auto-generate a layer-kind legend to distinguish the two
;;   regression curves. Workaround: color each layer explicitly.
;;
;; - Histograms, stacked bars, step plots, and other stat-derived
;;   marks do not default to a `"count"` or `"density"` y-label.
;;
;; - Linear continuous color legends (numeric `:color` mapping with
;;   `:linear` scale) label only the endpoint tick marks on the
;;   gradient bar. Intermediate values are unlabeled, making it hard
;;   to map interior colors back to data values. Log-scaled
;;   continuous color and fill legends do carry intermediate ticks.
;;
;; - SPLOMs with 6+ variables at the default 600x400 have tight
;;   panels. Bump `:width`/`:height` or pin
;;   `:panel-width`/`:panel-height`.
;;
;; - Horizontal bars from `(pj/coord :flip)` render the first row of
;;   data at the bottom of the chart, so a dataset sorted descending
;;   (natural "top N" order) produces the biggest bar at the bottom.
;;   The behavior matches ggplot2's `coord_flip()`. Workaround: sort
;;   the dataset ascending before plotting, e.g.
;;   `(tc/order-by data [:value] [:asc])`. A future opt-in flag such
;;   as `(pj/coord :flip {:reverse-categorical true})` would spare
;;   users the sort.

;; ## Marks
;;
;; - **Aesthetic-gate versus mark-consumer asymmetry.** Several
;;   aesthetics are accepted at the universal pose-mapping gate but
;;   consumed only by one or two mark extractors. Setting them on
;;   other marks has no effect and raises no error.
;;
;;   | Aesthetic | Consumed on | Silently ignored on |
;;   |:----------|:------------|:------------------|
;;   | `:size` (column ref) | `lay-point` | every other lay-* |
;;   | `:alpha` (column ref) | `lay-point` | every other lay-* (literal `:alpha N` works on most via `:fixed-alpha`) |
;;   | `:shape` | `lay-point` | text, label, lollipop, summary, ... |
;;   | numeric (continuous) `:color` | `lay-point`, `lay-interval-h` | every other lay-* (a numeric column on a categorical-color path produces banded palette colors instead of a gradient) |
;;   | tooltip / row-indices plumbing | `lay-point`, `lay-interval-h` | every other lay-* |
;;
;;   Workaround: pre-bin or convert the numeric column into a
;;   discrete color column where appropriate, or use `lay-point` for
;;   the mark where the aesthetic must vary per row.
;;
;; - `:alpha` on `pj/lay-rule-h`/`pj/lay-rule-v` is silently dropped
;;   at render time (the rendering path reads `:color` only). Bands
;;   honor `:alpha`. Workaround: use a lighter `:color` to simulate
;;   the visual effect on rules.
;;
;; - `pj/lay-rule-h` rendered under `(pj/coord :flip)` becomes a
;;   vertical line; `pj/lay-rule-v` becomes a horizontal line. The
;;   mark name still reflects the unflipped semantics. Add a
;;   one-line note in the surrounding prose if the chart's flipped
;;   state is non-obvious.
;;
;; - `:position :dodge` is ignored at render-time on nine marks
;;   including `summary` -- on `lay-bar` and `lay-summary` the dodge
;;   request is dropped at construction; on `lay-point` and
;;   `lay-line` the dodge metadata reaches the plan but no geometric
;;   offset is applied. Workaround: pre-compute dodge offsets via
;;   `tc/group-by`.
;;
;; - Polar plots for bar-family marks don't auto-emit category
;;   labels -- rose charts currently render with zero text.
;;
;; - Stacked bars don't split positive and negative values;
;;   all-positive data works, but mixed-sign data stacks
;;   incorrectly.
;;
;; - `pj/lay-tile` (and the underlying `:bin2d` stat) requires
;;   numeric x and y columns. Passing a categorical axis throws a
;;   clear "Stat :bin2d requires a numeric column" error at plan
;;   time. The recommended workaround is to render a numeric-indexed
;;   grid (1-N integers in place of the categorical column) and
;;   pair `:breaks` with `:labels` on the axis -- see Customization
;;   and Troubleshooting for a worked example. For a true categorical
;;   axis (binning over labels rather than numeric intervals),
;;   `pj/lay-bar` with a `y` column and `{:color :value}` gives a
;;   categorical "heatmap" look.
;;
;; - Horizontal value bars (`pj/lay-bar` with the category on `:y`)
;;   support plain and dodged layouts, but not `:position :stack` or
;;   `:position :fill` -- stacking accumulates along the y-axis, which
;;   is the categorical band for a horizontal bar. Workaround: put the
;;   category on `:x` and add `(pj/coord :flip)`, where stacking works.
;;
;; - Numeric/temporal-position bars (`pj/lay-bar` with both axes numeric
;;   or temporal) draw one bar per row at its x position; grouped bars
;;   (via `:color`) overlap rather than dodge, and `:position :stack` is
;;   not applied. Pre-aggregate or jitter positions to separate groups.
;;
;; - Value bars (`pj/lay-bar` with a value column) cannot use a log scale
;;   on the value axis: a bar rests on a zero baseline, and a log scale has
;;   no zero. Plotje raises a clear error pointing to the alternatives --
;;   a linear scale, or a baseline-free mark (`pj/lay-point`, `pj/lay-line`,
;;   `pj/lay-lollipop`). Count bars and histograms do support a log axis.
;;
;; - Stack order in `pj/lay-area` and `pj/lay-bar` (with
;;   `:position :stack`) follows the sort order of the `:color`
;;   column. There is no `:stack-order` / `:color-order` option yet,
;;   so forcing a specific bottom-to-top order requires prefixing
;;   category labels with sort-stable ordinal characters
;;   (`"01: ..."`, `"02: ..."`), which leaks into the legend.
;;
;; - Annotations are silently skipped under `(pj/coord :polar)`. A
;;   polar rule would need to render as a circle (fixed radius) or
;;   spoke (fixed angle); those shapes are not implemented. Use
;;   Cartesian or flip coords for annotated plots.
;;
;; - Large scatters produce large SVGs (~220 bytes/point). For >10k
;;   points, use `:format :bufimg` for raster output.
;;
;; - Saving to PNG (`(pj/save pose "plot.png")`, and the `:bufimg`
;;   raster path generally) truncates the rotated y-axis label after
;;   ~6 characters. The
;;   SVG path (`pj/plot` + Clay GFM, or `rsvg-convert` on the saved
;;   SVG) renders the full label. Root cause lives in Membrane's
;;   Java2D backend (the rotated-text bounding box is clipped).
;;   Workaround: render to SVG and rasterize externally, or pad
;;   `:y-label` to stay short. Needs an upstream fix in Membrane.
;;
;; - LOESS with confidence bands is O(n^2); subsample above ~5k
;;   rows.

;; ## Options and Configuration
;;
;; - `:panel-size` is a legacy configuration key from the
;;   pre-total-first layout. It now emits a deprecation warning and
;;   is ignored. Use `:panel-width` / `:panel-height` (total-first
;;   escape hatches).
;;
;; - The `:width` key on a `pj/plan` result preserves the user's
;;   original request even when `:panel-width` pins the real size --
;;   inspect `:total-width`/`:total-height` for the rendered canvas.
;;
;; - `pj/plan` called on a plan or on a hiccup value now throws a
;;   clear error. Call `pj/plan` only on poses.

;; ## Mixing Keyword and String Column References
;;
;; - Mapping the same column with a keyword in one place and a
;;   string in another (e.g. `(pj/pose ds {:color :group})` then
;;   `(pj/lay-point :x :y {:color "group"})`) is not normalized: the
;;   scope hierarchy treats them as different keys and the result is
;;   a silent empty plot. Workaround: pick one form (keyword or
;;   string) and use it consistently within a pose.

;; ## ggplot2 Features Not Yet Implemented
;;
;; - The `:fill` aesthetic is currently consumed only by `lay-tile`
;;   (and the `:bin2d` output beneath `lay-density-2d`). On filled
;;   marks like `lay-bar`, `lay-area`, and `lay-violin`, `:color`
;;   paints the interior; there is no separate stroke channel.
;;
;; - The `:linetype` aesthetic (ggplot2's `aes(linetype=...)` for
;;   solid vs. dashed lines) is not implemented. Workaround: encode
;;   the same distinction via `:color` instead.
;;
;; - No `after_stat()` analog. ggplot2 idioms like
;;   `geom_bar(aes(label=after_stat(count)))` and
;;   `geom_histogram(aes(y=after_stat(density)))` reference computed
;;   stat values inside aesthetic mappings; Plotje requires a
;;   pre-computed column. Workaround: aggregate the data first via
;;   `tc/group-by` + `tc/aggregate`, then map the count or density
;;   column directly.
;;
;; - Theme support is shallow versus ggplot2: today the theme map
;;   carries `:bg`, `:grid`, `:font-size`, and a small handful of
;;   other keys. Named theme presets (`theme_minimal`, `theme_bw`,
;;   `theme_classic`), axis text rotation, panel borders, strip
;;   text styling, and `legend.position` by coordinate are not yet
;;   exposed.
;;
;; - Per-layer `data`, `guides()` for per-aesthetic legend control,
;;   `scale_*_sqrt`/`reverse`/`date`. All tracked in the backlog.
