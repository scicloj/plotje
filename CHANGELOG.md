# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
- **Breaking:** `pj/lay-value-bar` is removed. `pj/lay-bar` now covers both cases: with x only it counts each category (as before), and with a y column it uses the y value as the bar height (the former `pj/lay-value-bar`). The stat is inferred from whether a y column is present and is overridable with `{:stat :count}` or `{:stat :identity}`. To migrate, replace `(pj/lay-value-bar data :x :y)` with `(pj/lay-bar data :x :y)`. This also lifts the previous "stacked bars reject pre-aggregated counts" limitation -- `pj/lay-bar` with `{:position :stack}` and a y column now stacks pre-computed values. - thanks, @timothypratley
- `pj/lay-bar`'s categorical-x error now points to the `{:x-type :categorical}` override and `(pj/coord :flip)`, matching the guidance other categorical-axis marks already give.
- `pj/valid-membrane?` and `pj/explain-membrane` validate a membrane against its Malli schema, mirroring the existing `pj/valid-plan?` / `pj/explain-plan` pair for plans.
- fix: a panel's marks are now clipped to the panel. Geometry running past the axis domain -- a `pj/lay-line` reference line drawn beyond a narrowed `pj/scale` domain, say -- is masked at the panel edge instead of painting across neighbouring panels in a `pj/arrange` or facet layout. A narrowed `:domain` acts as a view window (like ggplot2's `coord_cartesian`): the data is kept, only the view is bounded. (Closes #16) - thanks, @behrica

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
