# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
- fix: `pj/svg-summary` counts square markers. A `:square` marker draws as a rounded rectangle of radius 0, and the summary required a positive radius to count a point and no radius at all to count a tile, so squares fell in neither bucket: `(pj/lay-point :sepal-length :sepal-width {:shape :species})` on a 150-row dataset summarized as 100 marks. Squares now count in `:points` alongside circles, which also brings their colors and opacities into `:colors` and `:alphas`. `:sizes` still reports only positive radii, so a square contributes nothing there.
- `pj/plot`, `pj/save`, `pj/draft`, `pj/plan`, and `pj/membrane` now accept raw data directly, giving it a default mapping first exactly as `pj/pose` does. Previously `(pj/plot some-dataset)` (data not wrapped in `pj/pose`) rendered a blank figure; it now renders the same inferred default as `(pj/plot (pj/pose some-dataset))`.
- new public pipeline step `pj/infer-mapping`: given a pose that carries data but no mapping (the bare leaf `pj/->pose` produces), it attaches a default position/color mapping from the first 1-3 columns; it is a no-op on any pose that already has a mapping, has layers, is composite, or has 4+ columns. This is the step the terminal shortcuts apply after `pj/->pose`, exposed so pipeline-minded users can build their own chains (`(-> data pj/->pose pj/infer-mapping pj/pose->draft pj/draft->plan)`).
- a bare collection of scalar values -- numbers, strings, or keywords, e.g. `[1 4 1 5 6]` -- is now accepted as plot data and read as a single column named `:value` (previously only `{:column [values]}` maps or sequences of row-maps were accepted). Combined with the default-mapping change, `(pj/plot [1 4 1 5 6])` renders a histogram.
- `pj/save` now returns the written file as a `java.io.File` carrying `:kind/image` metadata (instead of the path string), so evaluating a `pj/save` call in a notebook also displays the saved chart. The file prints as its path and compares equal to a plain `java.io.File` on the same path, so `(str (pj/save ...))` still yields the path string. (PR #29) - thanks, @timothypratley
- add `plotje-plot` as a class on svg output (PR #28) - thanks, @timothypratley

## [0.5.0 - 2026-07-03]
- `pj/lay-density` and `pj/lay-area` accept an opt-in outline on the curve: `:stroke` (outline color) with optional `:stroke-width`. The fill still comes from `:color`, so `(pj/lay-density :x {:color "lightblue" :stroke "black"})` draws a light-blue area with a black outline. The outline strokes only the top curve, not the baseline. Without `:stroke` the appearance is unchanged. (Closes #11) - thanks, @behrica
- dashed and dotted strokes: `pj/lay-line`, `pj/lay-step`, `pj/lay-smooth`, the reference lines `pj/lay-rule-h` / `pj/lay-rule-v`, and a density/area outline accept `:stroke-dash`, either a named preset (`:dashed`, `:dotted`, `:solid`) or a raw `[dash gap ...]` pixel pattern (`{:stroke-dash [6 3]}`). Renders through both the SVG and PNG (Java2D) paths. (Closes #12) - thanks, @behrica
- `pj/svg-summary` reports `:dashed-lines` (count of polylines carrying a stroke-dasharray) and `:dash-patterns` (the distinct stroke-dasharray strings), for asserting that a dashed line, rule, or area outline rendered dashed and with which pattern.

## [0.4.0 - 2026-07-01]
- **Breaking:** `pj/lay-value-bar` is removed. `pj/lay-bar` now covers both cases: with x only it counts each category (as before), and with a y column it uses the y value as the bar height (the former `pj/lay-value-bar`). The stat is inferred from whether a y column is present and is overridable with `{:stat :count}` or `{:stat :identity}`. To migrate, replace `(pj/lay-value-bar data :x :y)` with `(pj/lay-bar data :x :y)`. This also lifts the previous "stacked bars reject pre-aggregated counts" limitation -- `pj/lay-bar` with `{:position :stack}` and a y column now stacks pre-computed values. - thanks, @timothypratley
- `pj/lay-bar` value bars now accept the categorical axis on either x or y: `(pj/lay-bar :value :category)` with a categorical y draws horizontal bars directly, no `pj/coord :flip` needed (matching how `pj/lay-boxplot` auto-orients). Plain and dodged horizontal bars are supported; stacked/filled horizontal bars still need `(pj/coord :flip)`.
- `pj/lay-bar` with two numeric or temporal axes now draws a bar at each x position -- a numeric-position or time-series bar chart (`(pj/lay-bar :month :revenue)`), which previously errored. Bar width defaults to `0.9` of the smallest gap between adjacent x values; set it with `{:bar-width n}`. Grouped numeric bars currently overlap rather than dodge.
- `pj/lay-bar`'s categorical-x error now points to the `{:x-type :categorical}` override and `(pj/coord :flip)`, matching the guidance other categorical-axis marks already give.
- `pj/valid-membrane?` and `pj/explain-membrane` validate a membrane against its Malli schema, mirroring the existing `pj/valid-plan?` / `pj/explain-plan` pair for plans.
- fix: render-stage options set on a pose with `pj/options` -- notably `:theme`, but also `:palette` -- now flow through the explicit `pj/draft->membrane` and `pj/draft->plot` steps, not only through the `pj/plot` / `pj/membrane` shortcuts. These steps default their options to the draft's own options (any options passed explicitly override per key), so a theme set before drafting is no longer dropped at the membrane stage. (Closes #20) - thanks, @behrica
- fix: a panel's marks are now clipped to the panel. Geometry running past the axis domain -- a `pj/lay-line` reference line drawn beyond a narrowed `pj/scale` domain, say -- is masked at the panel edge instead of painting across neighbouring panels in a `pj/arrange` or facet layout. A narrowed `:domain` acts as a view window (like ggplot2's `coord_cartesian`): the data is kept, only the view is bounded. (Closes #16) - thanks, @behrica
- `pj/options` accepts `:x-tick-angle` to rotate x-axis tick labels (in degrees; -45 is a common diagonal), so dense or long categorical labels stay readable instead of overlapping. `:x-tick-label-pad` overrides the vertical space reserved below the panel for the angled labels. The rotation flows through `pj/save` (SVG and PNG) as well as the notebook `pj/plot` path. Long labels can still run past the left plot edge (see Known Limitations). (PR #6) - thanks, @tombarys
- `pj/scale` accepts `:n-ticks` on a categorical axis to thin a crowded axis to about that many evenly-spaced tick labels, instead of labelling every category (`(pj/scale :x {:n-ticks 8})`). An alternative to rotating the labels for dense categorical axes. (PR #25) - thanks, @behrica
- fix: `pj/scale` `:breaks` and `:labels` now work on a categorical axis, not just numeric ones. On a discrete axis `:breaks` selects which categories get a tick (each matched to a category by its displayed label) and `:labels` relabels them; a break naming no category is dropped with a warning (an error under `:strict`). Previously the categorical branch ignored both. When both `:breaks` and `:n-ticks` are given, explicit `:breaks` win and no thinning is applied. (Closes #22) - thanks, @behrica

## [0.3.1 - 2026-06-02]
- Layers sharing a panel now paint in the order they were added -- each `pj/lay-*` call renders on top of the previous one -- instead of being reordered by position type. A `pj/lay-text` or `pj/lay-label` added after a bar now reads on top of it rather than being hidden underneath.
- `pj/lay-text` and `pj/lay-label` accept `:align-x` (`:left`/`:center`/`:right`) and `:align-y` (`:top`/`:center`/`:bottom`) to set which part of the label sits on the data point -- e.g. `:align-x :right` places a value label inside a bar's end. Defaults `:left`/`:center` preserve the previous placement.
- `:nudge-x`/`:nudge-y` on a categorical axis now raise a clear error pointing to `:align-x`/`:align-y` (and `:jitter`/`:position :dodge`). Nudge is a data-space shift and applies only to numeric or temporal axes.

## [0.3.0 - 2026-05-28]
- `pj/lay-*` with different x/y columns from the existing pose now produces a two-panel composite instead of throwing.
- When `pj/lay-*` would create a new panel using columns that don't exist in the data, the error now fires at the lay call with a clear message, instead of later during `pj/plan` or `pj/plot`.
- When a layer carries its own `:data` but the pose's x/y columns are missing from it, the error now names where the missing column came from and suggests two fixes: rename the column to match, or set a different x/y on the layer.

## [0.2.2 - 2026-05-19]
- fix: `pj/scale :y :log` now works on histograms and categorical bar charts. (Closes #5) - thanks, @harold.
- fix: SVG coordinate formatter now pins `java.util.Locale/ROOT`, so plots render correctly on JVMs whose default locale uses comma as the decimal separator (Czech, German, etc.). (PR #3) - thanks, @tombarys

## [0.2.1 - 2026-05-09]
- `pj/scale` accepts `:labels` paired with `:breaks` -- render numeric tick positions with custom text (e.g. days of the week 1-7 labelled "Mon"-"Sun" on a tile heatmap). Length must match `:breaks`; `:labels` without `:breaks` throws.
- docstring updates

## [0.2.0 - 2026-05-05]
- the membrane stage now returns a `PlotjeMembrane` record implementing the [Membrane](https://github.com/phronmophobic/membrane) UI protocols (`IOrigin`, `IBounds`, `IChildren`), so Plotje plots compose with hand-built Membrane elements. Width and height read via `(membrane.ui/width m)`/`(height m)`; title rides as `:plotje/title`. Replaces the prior metadata-tagged-vector contract.
- new `pj/membrane?` predicate

## [0.1.0 - 2026-05-03]
- initial public alpha release
- composable five-stage pipeline: pose -> draft -> plan -> membrane -> plot
- layer types for distributions, ranking, time series, relationships, and polar
- composite poses with faceting and shared scales
- SVG and PNG rendering via membrane
