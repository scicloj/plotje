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
;;   to map interior colors back to data values. Log-scaled color and
;;   fill legends do carry intermediate ticks.
;;
;; - SPLOMs with 6+ variables at the default 600x400 have tight
;;   panels. Increase `:width`/`:height` or pin
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
;;
;; - `:fit-text-domain` widens a numeric domain so that text and label
;;   marks near its edge are drawn in full. Nothing widens the domain
;;   for a mark whose size is in drawing units for another reason: a
;;   `pj/lay-point` given a large `:size` at the extreme of its domain is
;;   cut at the panel edge, as is a long rug tick. The 5% domain padding absorbs
;;   this at ordinary radii, and a scale given a wide `:range` -- say
;;   `(pj/scale pose :size {:range [3 20]})` -- reaches past it.
;;   Workaround: widen the domain with `pj/scale`.
;;
;; - Rotated x-tick labels (`:x-tick-angle`) reserve extra vertical
;;   space below the panel, but not extra horizontal space. A long
;;   label rotated to a diagonal extends to the left of its tick, so
;;   very long category names on the leftmost tick can run past the
;;   left edge of the plotting area. Workaround: shorten the labels,
;;   reduce the angle, or widen the plot with `:width`.
;;
;; - A categorical axis accepts its categories and nothing between
;;   them. ggplot2 places categories at 1, 2, 3 and lets a mark be
;;   drawn at 1.5 or 2.2, halfway along the gap; Plotje's categorical
;;   axis is a band scale, which answers nothing for a value between
;;   two bands, so a mark asking for one is not drawn. Workaround:
;;   where the reason for wanting a fractional place was to clear
;;   another mark, `:offset-x`/`:offset-y` shift by a distance on the
;;   page and work on any axis. Wanting a place genuinely between two
;;   categories has no workaround; it needs the categorical axis to
;;   become a continuous scale carrying a label table, which is
;;   designed but not implemented.
;;
;; - A line has no arrowhead. `pj/lay-line` draws a plain stroke, with
;;   `:stroke-dash` for dashed and dotted styles, so a leader line
;;   pointing from a note to the mark it describes ends bluntly.
;;   Workaround: end the line on a small `pj/lay-point` marker, as the
;;   callout recipe in the
;;   [Cookbook](./plotje_book.cookbook.html#callout-with-a-leader-line) does.
;;
;; - Nothing moves a label out of the way of another label. Two text
;;   marks at nearby positions are drawn on top of each other, and
;;   labelling every point of a dense scatter produces an unreadable
;;   pile. Workarounds: label a subset, or separate them by hand with
;;   `:offset-x`/`:offset-y`. Automatic placement, as in R's `ggrepel`,
;;   is designed but not implemented.
;;
;; - A long label at the right edge of a panel costs axis range.
;;   `:fit-text-domain` makes room for it by widening the domain,
;;   which is the only lever available -- there is no way to reserve
;;   drawing-space room for labels outside the drawing area. Several
;;   sentence-length labels at the line ends can push the axis well
;;   past the data. Workarounds: shorten the end labels and put the
;;   sentences in a callout, or turn the widening off with
;;   `{:fit-text-domain false}` and accept the clipping.
;;
;; - `:in` places a layer in the data or on the panel
;;   (`:in :drawing-area`), and nothing places one on the canvas as a
;;   whole. A note outside every panel -- beside the legend, or under
;;   the title -- has no position to be given. Workaround: `:caption`
;;   and `:subtitle` for text below and above the panels.
;;
;; - `pj/marginal` draws on `:top` and `:right`. `:bottom` and `:left`
;;   would put the strip between the panel and the axis that describes
;;   it, so they are reported rather than drawn.
;;
;; - A pose gets one marginal at a time. `pj/marginal` takes a leaf
;;   pose and returns a composite, and a composite has no single panel
;;   to put a second strip beside, so a scatter with a density above it
;;   and another to its right has to be written out as a composite by
;;   hand.
;;
;; - A `pj/marginal` on a faceted pose draws each facet's strip label
;;   twice, once over the marginal row and once over the row below it.
;;   Faceting lives in the pose's `:opts`, which the marginal copies
;;   onto the composite it builds, so both rows facet and each marginal
;;   describes its own facet. Only the repeated label is wrong, and
;;   there is no workaround for it.
;;
;; - `(pj/marginal pose :top :histogram)` draws its tallest bar close to
;;   the top of the marginal panel. The value axis is padded by the same
;;   fraction any axis is, and the strip is a quarter of the height, so
;;   the gap comes to a few drawing units -- which a density hides,
;;   since its tail approaches zero, and a histogram does not.
;;   Workaround: `pj/marginal` builds a composite whose marginal cell is
;;   the first sub-pose for `:top` and the second for `:right`, so
;;   `(update-in m [:poses 0] pj/scale :y {:include n})` with `n` above
;;   the tallest count gives the strip more room over the bars.
;;
;; - The pose's `:color` mapping does not reach its marginal. A
;;   marginal describes one whole column, so one undivided curve or set
;;   of bars is drawn beside however many colored groups the panel
;;   carries. Workaround: facet instead of
;;   coloring -- faceting does reach the marginal, giving one
;;   distribution per panel.

;; ## Marks
;;
;; - **Aesthetic-gate versus mark-consumer asymmetry.** Several
;;   aesthetics are accepted at the universal pose-mapping gate but
;;   consumed only by one or two mark extractors. Setting them on
;;   other marks does not vary the picture.
;;
;;   | Aesthetic | Consumed on | Elsewhere |
;;   |:----------|:------------|:------------------|
;;   | `:size` (column ref) | `lay-point` | warned, and no legend is drawn for it |
;;   | `:alpha` (column ref) | `lay-point` | warned, and no legend is drawn for it (a written `:alpha N` works on most via `:fixed-alpha`) |
;;   | numeric (continuous) `:color` | `lay-point`, `lay-interval-h` | every other lay-* ignores it (a numeric column on a categorical-color path produces banded palette colors instead of a gradient) |
;;   | `:tooltip`, and the row-index plumbing beside it | `lay-point`, `lay-interval-h` | every other lay-* accepts the mapping and draws no tooltip, with no message |
;;
;;   Only `lay-point` varies a radius or an opacity from row to row, so
;;   `{:size {:column :r :scale false}}` -- which asks for the column's
;;   own values as radii -- is refused elsewhere rather than warned:
;;   there is no reading for it at all. The warning and the refusal both
;;   name `lay-point`. Which marks those are is read from the registry,
;;   where each layer type declares what its mark varies under
;;   `:varies`, so an extension that draws a per-row size says so and is
;;   named alongside `lay-point`.
;;
;;   `:shape` is not in this table: it is drawn only by `lay-point`, and
;;   passing it anywhere else is rejected rather than ignored -- every
;;   other layer type warns and names `lay-point` as the one that takes
;;   it. `:text` is the same, and names `lay-text` and `lay-label`.
;;
;;   Workaround: pre-bin or convert the numeric column into a
;;   discrete color column where appropriate, or use `lay-point` for
;;   the mark where the aesthetic must vary per row.
;;
;; - `:alpha` on `pj/lay-rule-h`/`pj/lay-rule-v` is silently dropped
;;   at render time (the rendering path reads `:color` only). Bands
;;   honor `:alpha`. Workaround: use a lighter `:color` to simulate
;;   the visual effect on rules.

;; - `pj/lay-rule-h` rendered under `(pj/coord :flip)` becomes a
;;   vertical line; `pj/lay-rule-v` becomes a horizontal line. The
;;   mark name still reflects the unflipped semantics. Add a
;;   one-line note in the surrounding prose if the chart's flipped
;;   state is non-obvious.
;;
;; - `:position :dodge` is ignored at render-time on several marks.
;;   The request fails in one of two ways: on `lay-bar` and
;;   `lay-summary` the dodge request is dropped at construction; on
;;   `lay-point` and `lay-line` the dodge metadata reaches the plan
;;   but no geometric offset is applied. Workaround: pre-compute
;;   dodge offsets via `tc/group-by`.
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
;;   pair `:breaks` with `:tick-labels` on the axis -- see Customization
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
;;   on the value axis: a bar is measured from zero, so its axis always
;;   reaches zero, and a log scale has no reading for zero. Plotje reports
;;   this and names the alternatives -- a linear value axis, or a mark that
;;   is not measured from a baseline (`pj/lay-point`, `pj/lay-line`). Count
;;   bars and histograms do support a log axis, as do the other marks that
;;   are measured from a baseline: `pj/lay-area` and `pj/lay-lollipop` rest
;;   on the panel's smallest value there, as a count bar does.
;;
;; - Annotations are silently skipped under `(pj/coord :polar)`. A
;;   polar rule would need to render as a circle (fixed radius) or
;;   spoke (fixed angle); those shapes are not implemented. Use
;;   Cartesian or flip coords for annotated plots.
;;
;; - Large scatters produce large SVGs (~220 bytes/point). For >10k
;;   points, use `:format :bufimg` for raster output.
;;
;; - LOESS with confidence bands is O(n^2); subsample above ~5k
;;   rows.

;; ## Scales
;;
;; - One pose reads an aesthetic through one scale. Two layers asking
;;   for different scales on `:size`, `:alpha`, `:color` or `:fill`
;;   are refused rather than drawn, since a plot has one legend per
;;   aesthetic. Two layers naming different scales for `:x` or `:y`
;;   are refused as well, because a panel has one of each axis.
;;   Workaround in every case: put the layers in separate poses with
;;   `pj/arrange`, where each cell has its own scales and its own
;;   legend.
;;
;; - Facet panels share their scale *types*. `{:scales :free}` gives
;;   each panel its own domain, but there is no equivalent for giving
;;   one panel a log axis and another a linear one. Workaround:
;;   `pj/arrange` again.
;;

;; ## Options and Configuration
;;
;; - `:panel-size` is a legacy configuration key from the
;;   pre-total-first layout. It now emits a deprecation warning and
;;   is ignored. Use `:panel-width` / `:panel-height` (which set the
;;   panel size directly).
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
;;   scope hierarchy treats them as different keys. The mismatch is
;;   reported rather than drawn -- the spelling that does not match a
;;   column name is looked up, found missing, and named alongside the
;;   columns the data does carry. The exception is a string that
;;   happens to name something the aesthetic can draw: `"red"` on
;;   `:color` is that color, so the mapping quietly stops grouping
;;   instead. Workaround: pick one form (keyword or string) and use it
;;   consistently within a pose.

;; ## Integer Column Names
;;
;; - A dataset built without column names gets integer ones, and
;;   Plotje reads them wherever a mapping is written:
;;   `(pj/lay-point ds {:x 0 :y 1})`, `(pj/lay-point ds 0 1)` and
;;   `(pj/pose ds {:x 0 :y 1})` all plot those two columns, and the
;;   derived axis titles read as the names.
;;
;;   What remains is that a number has two possible readings on `:x`
;;   and `:y`, and the data decides between them: a number the data
;;   carries as a column name is that column, and any other number is a
;;   value to place a mark at. So the same code can change meaning on a
;;   dataset whose column names differ. Where that matters, write the
;;   mapping out in full -- `{:x {:column 0}}` and `{:x {:value 0}}`
;;   are each unambiguous -- or rename with
;;   `(tc/rename-columns ds [:x :y])` as shown in the
;;   [Datasets](./plotje_book.datasets.html#column-names) chapter.
;;
;;   The appearance aesthetics work the same way. `{:size 1}` reads
;;   column 1 where the data has one, and is a radius where it does
;;   not; `{:size {:value 1}}` insists on the radius.

;; ## ggplot2 Features Not Yet Implemented
;;
;; - The `:fill` aesthetic is currently consumed only by `lay-tile`
;;   (and the `:bin2d` output beneath `lay-density-2d`). On filled
;;   marks like `lay-bar`, `lay-area`, and `lay-violin`, `:color`
;;   paints the interior; there is no separate stroke aesthetic.
;;
;; - The `:linetype` aesthetic (ggplot2's `aes(linetype=...)` for
;;   solid vs. dashed lines) is not implemented. Workaround: encode
;;   the same distinction via `:color` instead.
;;
;; - A layer's own `:data` is not split by `pj/facet`. The pose's data
;;   is divided among the panels, but a layer carrying its own dataset
;;   is drawn whole in every panel, whether or not that dataset has the
;;   facet column. ggplot2 divides a layer's data the same way it
;;   divides the plot's, so `geom_hline(data = means, aes(yintercept =
;;   m))` puts one line in each panel where Plotje puts all of them in
;;   each. Nothing errors; the picture simply shows every group's mark
;;   in every panel. Workaround: build the panels with `pj/arrange`
;;   rather than `pj/facet`, so each cell is its own pose with its own
;;   layer data.
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
;;   `theme_classic`), panel borders, strip text styling, and
;;   `legend.position` by coordinate are not yet exposed. Rotating
;;   the x-axis tick labels is available, but as the `:x-tick-angle`
;;   plot option rather than through the theme -- see
;;   [Customization](./plotje_book.customization.html#rotating-tick-labels).
;;
;; - Per-layer `data`, `guides()` for per-aesthetic legend control,
;;   `scale_*_sqrt`/`reverse`/`date`. All tracked in the backlog. The
;;   axis types are `:linear`, `:log` and `:categorical`; a square-root
;;   axis is not among them. (`pj/scale :size {:by :sqrt}` is a
;;   different setting -- it says how a size spreads across the radii,
;;   not how an axis is transformed.)
