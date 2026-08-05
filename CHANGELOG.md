# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
- fix: `{:tooltip true}` and `{:brush true}` work in the browser again. Since 0.7.0 the plot asked Clay for `:kind/hiccup2`, which escapes every string it renders -- including the bodies of the Scittle scripts that drive the interactions, so they reached the page with `&quot;` where they wrote `"`. A script element's content is raw text in HTML, so nothing decoded them back and neither interaction ran. An interactive plot is now a `:kind/fragment`: the plot itself under `:kind/hiccup2`, which is what keeps a category named `R&D` reaching the page as text, and one `:kind/scittle` script beside it per interaction.
- terminology fixes in docstrings

## [0.7.0 - 2026-08-03]
- Kindly `4-beta23` -> `4-beta25`, brought in by Plotje: `pj/plot` asks for `:kind/hiccup2`, which `4-beta23` does not define.
- fix: `(pj/options {:legend-position :top})` draws its legend inside the image, instead of above the top edge where nothing is visible.
- a keyword category reads as words: `:not-applicable` labels a tick, legend entry or facet strip as `not applicable`. String categories are untouched. - thanks, @timothypratley
- a string column name titles its axis exactly as written: `Cost-Benefit Ratio`, not `Cost Benefit Ratio`. Keyword and symbol names are unchanged. - thanks, @timothypratley
- fix: every `pj/lay-*` arity rejects a non-column position, as `pj/pose` already did. **Breaking:** `(pj/lay-point data 0 1 {})` no longer plots integer column names -- rename them first with `(tc/rename-columns ds [:x :y])`.
- fix: an axis is labelled without crashing on column names that are neither keywords nor strings; such a name formats as its printed form. (PR #32) - thanks, @timothypratley
- fix: a data label at the largest value is drawn in full instead of cut off at the panel edge: a numeric domain is widened by however much its text marks need. A domain set with `pj/scale` is never widened. (Closes #18) - thanks, @behrica
- new configuration key `:fit-text-domain` (default true) turns that widening off.
- fix: an x tick label at the end of an axis is no longer cut in half by the image edge; the outermost labels shift inward when they would otherwise be clipped.
- fix: mapping a column to `:shape` produces a legend. One column driving both `:color` and `:shape` gives one merged legend, as in ggplot2; two different columns give two legends. (Closes #4) - thanks, @timothypratley
- new public var `pj/shape-symbols`, the marker symbols a categorical `:shape` mapping draws with, in assignment order.
- seven shape symbols where there were four: `:circle`, `:square`, `:triangle`, `:diamond`, `:triangle-down`, `:plus`, `:cross`. More categories than symbols repeats one, which now warns.
- `pj/scale` reads `:shape`: `:domain` sets the category and legend order, and new `:values` supplies the symbols -- `(pj/scale pose :shape {:domain ["gold" "silver"] :values [:diamond :cross]})`. An undrawable symbol is rejected, as is `:values` on any other channel.
- new plot option `:shape-label`, titling the shape legend as `:color-label` and `:size-label` do for theirs. Naming one half of a merged legend splits it in two.
- fix: `pj/svg-summary` no longer counts legend symbols as plot data.
- fix: a label layer grouped by the column its bars are dodged by is dodged with them, so each label sits over the bar it names. Errorbar and pointrange still take an explicit `{:position :dodge}`. (Closes #13) - thanks, @behrica
- `pj/lay-text` and `pj/lay-label` accept `{:stat :count}`, labelling each bar of a counting `pj/lay-bar`: `(-> data (pj/lay-bar :species) (pj/lay-label {:stat :count}))`. `pj/lay-point` and `pj/lay-line` plot those counts too. (Closes #14) - thanks, @behrica
- fix: PNG output places its text where the SVG does, within a pixel. Membrane's Java2D backend read neither the alignment nor the rotation the SVG backend honors, so every label and title in a saved PNG was misplaced.
- fix: `:x-tick-angle` rotates the tick labels in PNG output, not only in SVG.
- fix: a long `:y-label` renders in full in PNG output, where it was cut off after about six characters -- the missing rotation above, not the upstream Membrane bug it was listed as.
- **Breaking:** the `:label` mark is gone; `:text` and `:label` are two layer types drawn by the same `:text` mark, so a label layer reports `:mark :text` in a plan. `pj/lay-label` and `(pj/lay :label ...)` are unchanged. Marks drop from 22 to 21; the 25 layer types are unchanged.
- layer types can preset layer options through a `:defaults` map on the registry entry, which the call site overrides: `:label` is `:text` with `{:box true}`.
- text marks can be drawn on a background box: `:box true` gives a white rounded panel with a thin border, and a map sets its properties -- `{:box {:corner-radius 8}}`, or `0` for square corners. (Closes #19, with `:thousands-separator` below; the arrows and out-of-panel text in that issue's image are #17 and #18) - thanks, @behrica
- a background box has rounded corners by default (radius 3, after ggplot2's `geom_label`), where it was square.
- fix: a background box's border is drawn as a stroke, where it was a grey rectangle filled over the box.
- fix: `pj/svg-summary` reports label background boxes as `:label-boxes`, one per label, instead of counting them as `:tiles`.
- `pj/lay-text` and `pj/lay-label` accept `:font-weight` (`:normal` or `:bold`) and `:font-style` (`:normal` or `:italic`), independent and combining, in SVG and PNG. A numeric CSS weight such as `700`, or CSS `:oblique`, is rejected -- Java2D draws neither. (Closes #21) - thanks, @behrica
- `pj/svg-summary` reports `:bold-texts` and `:italic-texts`.
- fix: an SVG's `clipPath` ids are derived from the clip region's geometry instead of a counter shared across the whole JVM, so rendering the same plot twice produces the same bytes.
- size, alpha and continuous color legends honor `:thousands-separator` too, and a whole-valued legend number drops its trailing `.0` -- a size legend for a count reads 100,000, not 100,000.0.
- `pj/layer-option-docs` describes every layer option. Fourteen were missing, among them `:x`, `:y`, `:data`, `:mark` and `:stat`.
- `pj/scale :values` on a channel other than `:shape` now names `:palette` as the way to choose the colors a categorical `:color` mapping draws with.
- new configuration key `:thousands-separator` groups the digits of large numbers in numeric tick labels and in text taken from a column, so `(pj/options {:thousands-separator ","})` draws 462,389. Any string works. Off by default, and never applied to category names, legend entries or facet strips. Part of #19 - thanks, @behrica
- an options map that rejects a key now names where that key does belong: `(pj/lay-bar :x :y {:x-label "sales"})` adds `Plot options belong in pj/options: [:x-label]`. The pointer works in every direction, and under `:strict` the same text appears in the thrown exception. - thanks, @timothypratley
- the `pj/lay-point` docstring called its trailing map "aesthetic opts"; it holds layer options generally.
- fix: text containing the characters markup uses reaches a notebook as text: a category named `R&D`, or a title `"Q1 <profit> & loss"`, was read as markup. `pj/plot` now asks for `:kind/hiccup2`. A saved SVG was never affected. (PR #33) - thanks, @timothypratley

## [0.6.0 - 2026-07-29]
- fix: a density curve is now estimated over the data it describes, instead of over a grid running half the data's span past it on either side. That padded grid was both drawn and reported as the axis range, so `pj/lay-density` on a column spanning 4.3 to 7.9 drew an axis from 2.1 to 10.1 and a `pj/lay-rug` beside it covered only the middle of the plot. The curve now starts and ends with the rug, and the axis matches what ggplot2's `geom_density()` produces for the same data. (Closes #23) - thanks, @behrica
- `pj/lay-violin` and `pj/lay-ridgeline` estimate through the same kernel density, so they are bounded by their category's values too -- each body now ends where that category's data ends instead of tapering into a long needle past it. This matches ggplot2, which trims violins by default (`geom_violin(trim = TRUE)`).
- `pj/lay-density`, `pj/lay-violin` and `pj/lay-ridgeline` accept `:trim`, choosing which values each group's curve is estimated over, after ggplot2's argument of the same name and with its per-geom defaults. A density is untrimmed: every group is estimated across the whole layer, so grouped curves share one interval and each falls away to nothing rather than being cut off at its own group's extremes. `{:trim true}` estimates each group over its own values instead. A violin or ridgeline is trimmed, so each body ends at its category's values; `{:trim false}` extends it by three bandwidths on each side. This changes the appearance of a grouped `pj/lay-density`, which previously behaved as `{:trim true}`.
- density curves are smoothed with `nrd0`, the bandwidth rule R's `density()` and ggplot2 use; the previous default smoothed about 18% wider, drawing every curve slightly flatter and broader than the same data in ggplot2. Affects `pj/lay-density`, `pj/lay-violin` and `pj/lay-ridgeline`. Passing an explicit `:bandwidth` is unchanged.
- a density curve is drawn from 512 points rather than 100, matching ggplot2, so an enlarged plot no longer shows a faceted peak. `pj/lay-violin` and `pj/lay-ridgeline` keep 80, where the extra points make no visible difference.
- `pj/plot`, `pj/save`, `pj/draft`, `pj/plan`, and `pj/membrane` now accept raw data directly, giving it a default mapping first exactly as `pj/pose` does. Previously `(pj/plot some-dataset)` (data not wrapped in `pj/pose`) rendered a blank figure; it now renders the same inferred default as `(pj/plot (pj/pose some-dataset))`.
- new public pipeline step `pj/infer-mapping`: given a pose that carries data but no mapping (the bare leaf `pj/->pose` produces), it attaches a default position/color mapping from the first 1-3 columns; it is a no-op on any pose that already has a mapping, has layers, is composite, or has 4+ columns. This is the step the terminal shortcuts apply after `pj/->pose`, exposed so pipeline-minded users can build their own chains (`(-> data pj/->pose pj/infer-mapping pj/pose->draft pj/draft->plan)`).
- a bare collection of scalar values -- numbers, strings, or keywords, e.g. `[1 4 1 5 6]` -- is now accepted as plot data and read as a single column named `:value` (previously only `{:column [values]}` maps or sequences of row-maps were accepted). Combined with the default-mapping change, `(pj/plot [1 4 1 5 6])` renders a histogram.
- `pj/save` now returns the written file as a `java.io.File` carrying `:kind/image` metadata (instead of the path string), so evaluating a `pj/save` call in a notebook also displays the saved chart. The file prints as its path and compares equal to a plain `java.io.File` on the same path, so `(str (pj/save ...))` still yields the path string. (PR #29) - thanks, @timothypratley
- add `plotje-plot` as a class on svg output (PR #28) - thanks, @timothypratley
- fix: `pj/svg-summary` counts square markers. A `:square` marker draws as a rounded rectangle of radius 0, which fell between the summary's point test and its tile test, so `(pj/lay-point :sepal-length :sepal-width {:shape :species})` on a 150-row dataset summarized as 100 marks. Squares now count in `:points` alongside circles, which also brings their colors and opacities into `:colors` and `:alphas`; `:sizes` still reports only positive radii.

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
