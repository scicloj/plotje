# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

Every aesthetic can now be specified explicitly. A mapping may be written out in full, saying which source it takes and whether to read it through a scale; a scale is written in the mapping it belongs to, and accumulates down the scope chain; and a layer type declares which aesthetics its mark varies from row to row. Almost everything below follows from one of those three.

### Plots that redraw

- **Every plot with a `:size` column.** A size scale spreads the square root of the value across the radii, which is ggplot2's `scale_size`, where it spread the value itself, which is `scale_radius`. A radius linear in the value overstates every difference by squaring it. `(pj/scale pose :size {:by :linear})` restores the old picture, and `{:by :area}` spreads the ink itself.
- **Every plot with an `:alpha` column.** The default range is 0.1 to 1.0, which is ggplot2's, rather than 0.2 to 1.0. `(pj/scale pose :alpha {:range [0.2 1.0]})` restores it.
- **Every plot pairing a numeric `:color` column with `pj/scale`.** Any spec used to be read as a custom gradient, so `:linear` and a `:domain` swapped the default blue ramp for red-white-blue just as `:log` did. A spec now changes what it names and leaves the gradient alone; a gradient map is one naming at least one of `:low`, `:mid`, `:high`.
- **A `:size` or `:alpha` column whose values are all equal.** Every mark is drawn halfway across the domain, read through the scale's own `:by` method: 6.243 at the default range, where the bottom of the range was drawn before. `{:size {:value 7 :scale true}}` is drawn by the same rule.
- **`:from-zero` on a column holding zero or negative values.** The ink is proportional to distance from zero, so -5 draws the size 5 draws and the domain reaches whichever value is furthest from zero in either direction. Exactly zero draws no ink, which is what proportional area means; where the sign matters, map it to another aesthetic.
- **A categorical `:color` or `:fill` given a `:domain`.** The list orders the categories and the palette is assigned in that order, so the legend rows and the colours move together. The key used to validate and change nothing.

### Pose and plan shape

- `pj/scale` writes the mapping rather than the pose's options. A scale belongs to the aesthetic it reads, and a mapping written in full already carried one. `:opts` no longer holds `:x-scale`, `:y-scale`, `:size-scale` and the rest; `pj/coord` and `pj/facet` still write `:opts`, having no aesthetic to belong to.
- A size-legend entry carries `:magnitude` where it carried `:radius`, and the legend carries `:quantity` and `:swatch`. A swatch follows the quantity the mark draws -- circles for a radius, squares for a side, strokes of that thickness for a width.
- `pj/scale` has no `:group` aesthetic; it used to write a `:group-scale` that nothing read. To order or restyle what the reader sees, scale `:color` or `:shape`.

### A mapping written in full

- A mapping may say which source it takes and which side of the scale to read it through. `{:color {:column :variety :scale true}}` scales a column the conventions would have drawn, and `{:color {:column :hex :scale false}}` draws one they would have scaled. `{:value "blue"}` insists on the colour where the data carries a column called blue, and `{:column "blue"}` insists on the column. Omitting `:scale` leaves the convention in charge, and `:from` names the source the way the plain form does while leaving room for a `:scale`.
- `:x` and `:y` take the form too, which is how a number is pinned to one reading on a dataset with integer column names: `{:x {:column 0}}` reads column 0 and `{:x {:value 0}}` places every mark at zero. Otherwise a number is read against the layer's data wherever it is written, so `(pj/pose ds {:x 0 :y 1})` and `(pj/lay-point ds 0 1)` are no longer refused. `:scale false` on an axis measures in drawing units from the top left of the panel background, is asked per axis, and informs no domain.
- `:size` and `:alpha` read a column as it stands when told not to scale it: `{:size {:column :r :scale false}}` draws radii of 4, 8 and 12, which is ggplot2's `scale_size_identity()`. No legend either -- nothing passed through a scale. The column has to hold what the aesthetic can draw, so a radius column holding a negative is reported.
- A written value asked to scale becomes a datum, the way a constant inside ggplot2's `aes()` does: `{:color {:value "Model A" :scale true}}` draws a palette colour and earns a legend entry reading "Model A". One value over every row is a column of one distinct value, so the scales, domains and legends read it as they read any column.
- A column passes through its aesthetic's scale whatever it holds, and `{:scale false}` is the only way off it -- ggplot2's `scale_colour_identity()`. Nothing reads a column's contents to decide this, so what a column means does not change when its rows do.
- The layer's data decides which mapping values name a column, for every aesthetic alike: `{:color :red}` draws red instead of reporting a missing column. `:shape` takes one symbol for a whole layer beside the column it already took, and `:text` given a string that names no column labels every row with it.

### The scale spec

- `pj/scale` and a mapping's `:scale` speak one language on every aesthetic that has a scale. Beside `true` and `false`, `:scale` takes a type or a whole spec, so `{:size {:column :weight :scale :log}}` reads that one mapping through a log scale. Settings accumulate down the scope chain and the innermost wins key by key, so a pose that sets a range and a layer that names a type give a plot with both; the source still replaces, since a mapping states one source and two cannot combine. `:scale false` replaces too.
- `pj/scale :size` takes `:range`, `:by` and `:from-zero`; `pj/scale :alpha` takes `:range` and `:from-zero`. `:range` is what the aesthetic spans in the quantity the mark draws it as -- `[2 8]` on `:size` is a radius in drawing units, and the default. `:from-zero` anchors the domain and the range at zero, so twice the value is twice the ink; with `:by :area` that is ggplot2's `scale_size_area`. `:by` is refused on `:alpha`, which has no shape to correct for.
- `:domain` is honoured on `:size`, `:alpha`, `:color` and `:fill`, where it used to validate and change nothing (issue #39); a value outside a numeric one is drawn at the nearer end rather than dropped. On an axis it is read according to the column's type -- the order of the categories on a categorical column, the range the panel shows on a continuous one -- with categories matched by the text they are labelled with, so `{:domain [4 5 6 8]}` and `{:domain ["4" "5" "6" "8"]}` order the same bands. A category the domain omits is drawn after the listed ones with a warning.
- One aesthetic has one scale. Two layers reading `:size`, `:alpha`, `:color` or `:fill` through different scales, or drawing one as different quantities, are refused, since one legend explains one scale. A panel has one x axis and one y axis, so two layers naming different scales for an axis are refused as well; a layer naming none is no disagreement. The refusal points at `pj/arrange`, where each cell carries its own scales and legend.
- A key the aesthetic does not read is refused where it is written, in `pj/scale` and in a mapping alike, and the values are checked as well as the keys: a `:range` or numeric `:domain` that is not two finite numbers is named rather than dying in the arithmetic, and a range outside what the aesthetic can draw is refused. `:x-end`, `:x-min`, `:x-max`, `:y-min`, `:y-max` and `:text` refuse a `:scale` outright, as `:group` already did.
- A scale spec's `:label` titles the axis, beside `:labels`, which is tick text. The `:x-label` and `:y-label` plot options say the same thing and win over it.
- `pj/aesthetic-scales` publishes what each aesthetic's scale accepts -- the types it can be read through, and the spec keys it reads beside `:type` and `:domain`. It is the table `pj/scale` and a mapping's `:scale` are both validated against, so a reference built from it cannot drift from what they enforce. It sits with `pj/config-key-docs`, `pj/plot-option-docs` and `pj/layer-option-docs`.

### What a layer type varies

- A layer type declares which appearance aesthetics its mark varies from row to row, and as what: `(layer-type/register! :bubble {:mark :bubble :varies {:size :radius} ...})`. This was a closed table inside `impl.plan`, so an extension varying size per row was warned about and denied its legend while drawing correctly. The declared quantity also decides how ink grows with the value, which is what lets `:by :area` mean the same thing on a mark drawing a radius and one drawing a width. The declaration is checked at registration and again at plan time, where a mark that declares an aesthetic and draws no per-row values for it is reported.
- `layer-type/channel-magnitude-fn` gives a mark's renderer the function its legend is built from: a value from the layer's per-row buffers in, the quantity the mark draws it as out. It answers `identity` where a mapping asked for the column's values as they stand, and nil where the layer carries nothing to draw. The built-in point mark reads the same function, so an extension and a built-in cannot differ.
- A `:size` or `:alpha` column earns no legend where no mark on the plot varies that aesthetic, and says so; `{:size {:column :r :scale false}}` is refused outright on the sixteen marks that draw one value for the whole layer. `(pj/lay-line data :a :b {:size :r})` used to draw one uniform width beside a legend pairing the column's values with radii.

### Reported rather than passed over

- A mapping written in full is checked where it is written, so an unknown key, both sources at once, a source named `nil`, and a map naming no source are reported at the `pj/pose` or `lay-*` call rather than at `pj/draft`. It cannot stand where a whole map of mappings goes: `(pj/lay-point data :a {:column :b})` reads that map as the options map, so a full mapping goes under an aesthetic key, `{:y {:column :b}}`.
- An aesthetic told not to scale refuses an option that configures the scale it just left, such as `:color-type` or a `pj/scale` call beside `{:scale false}`; both used to render identically to the option being absent. A per-axis `:scale false` is refused on the marks that cannot read one, and under `:coord :flip` and `:coord :polar`, which move the axis it measures along -- a bar used to draw full-height bars and a boxplot an empty panel, both in silence. `{:in :drawing-area}` is refused on those same marks.
- `:size`, `:alpha` and `:fill` name the aesthetic and the column when given a categorical one, where they used to die on a class-cast error, and `:fill` and `:group` refuse a value that names no column -- `{:group 4}` resolved to zero groups, so the layer drew nothing and nothing said why. A hex colour written without its `#` is refused on the four annotation constructors as well as on layers. `(pj/scale pose :x :categorical)` on a numeric column points at `:x-type`/`:y-type` instead of calling the type unsupported. `pj/with-data` checks a column named in full, which used to attach and fail later at `pj/plan`.
- The layer-option docs say what each aesthetic actually reads. Thirteen entries were wrong: `:color` was documented as a "literal color string" when a keyword there was always a column, and `:y-min` and `:y-max` as columns when the band marks read a number.

### Fixed

- A colour gradient and a colour scale can be asked for together. `pj/scale :color` wrote its spec where the configuration keeps the gradient, so `(pj/options {:color-scale :viridis})` beside `(pj/scale pose :color :log)` kept whichever was written second and dropped the other in silence.
- A log colour legend labels its decades. The marks were log-spaced already, but the gradient bar carried only its two end labels, so nothing on the plot said the scale was logarithmic.
- A size or alpha legend's own text is measured when the legend column is sized, and the legend fits the canvas at any range. A size legend's numbers used to clip at the canvas edge and a long `:size-label` was cut mid-word. Rows are as tall as the widest swatch on them and are thinned to what the canvas holds; a row whose swatch has no size is dropped, so `:from-zero` no longer labels a zero beside an empty space.
- A panel where nothing places a mark through the x scale gets the same `[0 1]` fallback domain the y axis has always had, so `lay-bar`, `lay-histogram` and `lay-interval-h` stop dying on a null `x`. An axis no layer gives a data meaning draws no ticks and takes no label, because numbers drawn off that fallback name values no mark carries. A layer placed in the data on the same panel gives the axis its meaning back, and a domain set with `pj/scale` counts too.
- An annotation takes a colour by either spelling: `(pj/lay-rule-h pose {:y-intercept 2 :color :red})` draws red, where the keyword form used to be dropped in silence. Applies to all four of `pj/lay-rule-h`, `-v`, `pj/lay-band-h` and `-v`.
- `pj/lay` takes the layer-type entry `pj/layer-type-lookup` answers with, as well as the keyword, and the two behave the same.

## [0.8.1 - 2026-08-12]
- fix: tick labels, legend endpoints and tooltip numbers are formatted under `Locale/ROOT`, so a plot draws the same numbers whatever machine renders it.
- new configuration key `:decimal-separator` draws the decimal point as whatever string you name: `(pj/options {:thousands-separator "." :decimal-separator ","})` writes 1.234,5. Off by default.
- fix: a continuous legend's two end labels are written to six significant digits in plain notation, with trailing zeros dropped: `123456`, `0.1`, `2.5`.
- fix: the tick labels of a log fill legend drop their trailing zeros too: `0.001`, `0.01`, `0.1`.
- fix: `pj/save` honors every plot option `pj/plot` honors, in SVG and PNG -- `:title-font-size`, `:label-font-size`, `:thousands-separator`, `:grid-stroke-width` and `:annotation-stroke` among them.
- fix: a composite's cells read the composite's options. A cell still decides its own `:tooltip`.
- fix: a composite's title honors `:title-font-size`, and its shared legend honors the configuration its cells honor, `:thousands-separator` among it.
- strip labels read `:strip-font-size` wherever they are drawn, in a facet and in a grid composite alike. The default is 11.
- fix: `pj/to-data` reads a categorical axis, answering with the category whose band holds the position, and nil outside every band.
- fix: `pj/to-drawing` refuses a value a categorical axis has no position for -- a category it does not carry, or a fractional place such as 2.5 -- naming the value and the categories it could have been.
- fix: under `:coord :flip`, `pj/to-data`'s dataset arity reads a categorical x back as its categories. Both functions take and answer in data order under a flip.
- fix: a missing column is named, with the available ones listed, on a dataset whose column names are not all of one type.
- fix: `(pj/lay-point data {:x :height :y :weight})` reads the mapping from its options map on a dataset of four or more columns. A mapping given only partly there, such as `{:y :weight}`, still takes its other axis from the inferred one.

## [0.8.0 - 2026-08-07]
This release mostly follows the [label-positioning](https://clojurians.zulipchat.com/#narrow/channel/610149-plotje/topic/label.20positioning/) topic thread. Many thanks to @behrica, @timothypratley, @phronmophobic, and @generateme for the fruitful work over that thread.
See the new [Placing Marks](https://scicloj.github.io/plotje/plotje_book.placing_marks.html) documentation chapter for the main fruits of this release. 
- new function `pj/frames` reports where a plot's panels sit on the canvas: per panel, the `:panel-box` and `:drawing-area` rectangles in drawing units, plus the `:canvas` once for the plot. A composite reports every cell in canvas coordinates. Plain data throughout. - thanks, @timothypratley
- new functions `pj/to-drawing` and `pj/to-data` map between data space and drawing space for one panel of `pj/frames`. Both take a single x and y, or a dataset with `:x` and `:y` columns, and answer in kind. `pj/to-data` throws under `:polar`, which has no inverse; a panel's `:invertible?` says which case it is.
- new layer options `:offset-x` and `:offset-y` shift a layer by a number of drawing units, after the scales run -- the distance a label needs to clear its point. A nudge cannot: it shifts by a data amount, and throws on a categorical axis. Every layer type takes them, and a numeric domain widens to keep an offset label whole. - thanks, @behrica
- `:x` and `:y` may be given as a value, not only as a column. Both as values draws one mark: `(pj/lay-text pose {:x 6.5 :y 3.5 :text "mean"})`. One value beside a column repeats for every row, so `{:x 33 :y :revenue}` labels each row at one fixed x. The value counts toward the axis domains, as ggplot2's `annotate()` does. - thanks, @behrica
- new layer option `:in` names the space a layer's `:x` and `:y` are in: `:data` (default) or `:drawing-area`, which measures drawing units from the top left of the panel background. `(pj/lay-text pose {:in :drawing-area :x 12 :y 12 :text "n = 150"})` puts a note in the corner without touching the domains.
- `pj/lay-rule-*` and `pj/lay-band-*` warn that they do not accept `:in`, instead of taking it and doing nothing. They are placed from data values only.
- fix: `:group` no longer discards a fixed `:color`. `(pj/lay-line data :x :y {:group :country :color "#d0d0d0"})` draws one line per country, all in that grey; before, each line took a different palette color.
- fix: `{:tooltip true}` and `{:brush true}` work in the browser again. Since 0.7.0 the Scittle scripts reached the page with `&quot;` where they wrote `"`, and a script element's content is raw text in HTML, so nothing decoded them back and neither interaction ran.
- fix: the brush's selection rectangle follows the drag when CSS scales the plot. In a column narrow enough to halve the plot, a drag of 150 by 100 drew a marquee of 75 by 50, at half the distance from the corner. Which rows a release selects is unchanged.
- a text mark that widens its axis to be drawn in full now leaves a few drawing units of air, so the last glyph no longer touches the panel edge.
- docstrings say **drawing units** where they said pixels: one unit of the plot's `:width` and `:height`, which is a screen pixel only at the plot's natural size on a standard-resolution display.

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
