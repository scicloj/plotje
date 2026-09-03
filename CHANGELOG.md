# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Added

- `:bar-width` sets how wide a bar is drawn on a categorical axis, as a fraction of the category band, `0.8` by default -- the quantity `:box-width` names for a box. `(pj/lay-bar :growth :cohort {:bar-width 0.4})` draws a bar half the usual thickness, so a second bar layer over a first reads as an overlay rather than a stack. On a numeric or temporal axis, where a bar sits at a value rather than in a band, the option keeps its meaning as a width in data units. It applies to value bars, counted bars, and dodged and stacked bars alike.

- `pj/overlay` and the layer option `{:overlay true}` put a layer on the panel it is added to, rather than on a panel of its own. A layer naming columns the panel does not draw normally becomes a new panel, which is right for two unrelated pairs of columns and wrong for two measures meant to be read against one axis; `pj/overlay` says which was meant, and every layer added after it joins. The layer keeps its own columns, the axis covers every column drawn on it, and the axis is named for the panel's own column. `{:overlay false}` on one layer opts it out, and `(pj/overlay pose false)` turns it off from there on.

### Fixed

- A categorical column mapped to both an axis and `:color` draws one palette colour per category. The axis column is rewritten to display strings before the colour is chosen, while the category list keeps the values as written, so a keyword column matched nothing and every mark took the first palette entry while the legend listed the rest. Marks and legend now agree.

- A bar keeps its full width when a colour column is named. A bar fills a fraction of its category band, and the band was being divided by the number of colour categories even where nothing asked for a dodge, which narrowed every bar and pushed it to one side of its band. Dodged bars still divide the band between their groups.

## [0.10.1 - 2026-08-27]

### Fixed

- A scatterplot matrix -- the grid `pj/pose` builds from a rectangular pair list such as `pj/cross` produces -- places each panel by its columns: `:x` decides which column it sits in and `:y` which row, so the tick numbers along the bottom row and down the leftmost column describe every panel above and beside them. The grid was built transposed, so those numbers were true of one row and one column only and every other panel was read against a neighbour's scale. A matrix drawn before this change comes out transposed after it.

## [0.10.0 - 2026-08-25]

Ticks and their labels are most of this release. A number axis, a date axis, a size or alpha legend and a gradient bar are each ticked at round values, and a column drawn on an axis and in a legend at once is labelled the same way in both.

A legend and the marks it explains are drawn from one decision, so a reader can match one against the other.

`:include` names a value an axis has to reach. `(pj/scale pose :y {:include 0})` puts zero on the axis, so a length drawn along it is proportional to the value it stands for.

Many thanks to @phronmophobic & @timothypratley who affected this release a lot.

### Renamed

The retired name reports the one to write.

| Gone | Write instead |
|:--|:--|
| `layer-type/channel-magnitude-fn` | `layer-type/aesthetic-magnitude-fn` |

Every published docstring and arglist says *aesthetic* where it said *channel* -- `pj/scale` takes `[pose aesthetic scale-type]` -- and the errors `layer-type/register!` throws about `:varies` carry `:aesthetic` in their data.

### Plots that look different after upgrading

- **Every numeric axis, size legend and alpha legend whose values are whole numbers.** The ticks are whole too: a column of 0, 1, 2 and 3 is ticked 0, 1, 2, 3. An axis carrying a value between two whole numbers, one given a `:domain` written with a fraction, and a `:position :fill` that draws proportions are ticked as before.
- **Every log axis whose domain does not reach its outermost breaks.** A tick outside the domain is not drawn. A domain too narrow for two breaks is ticked across itself instead: 6 to 9 is ticked 6, 7, 8, 9. ggplot2 answers such a domain the same way round, by picking breaks inside the range rather than by filtering.
- **Every log axis spanning a few decades.** Ticks are chosen by how many of them the axis draws rather than how many the generator offers, so an axis whose powers of ten mostly fall outside its domain carries 1-2-5 or 1-2-3-5 breaks between them. A domain holding three or more powers of ten inside it is ticked at those.
- **Every continuous legend on a log scale.** The gradient bar is labelled only at the ticks that fall on it, and a domain too narrow for two breaks is ticked across itself as an axis is. A size legend, an alpha legend, a gradient bar and an axis read one rule for a log scale.
- **Every date axis.** Its ticks step by a round number of years, months, weeks, days, hours, minutes, seconds or milliseconds, and each one lands on a multiple of that step counted from the calendar rather than from the first value. A span of 1938 to 1971 is ticked 1940, 1945, 1950, a year of monthly readings begins at January, and a weekly axis lands on Mondays. ggplot2 places its date breaks the same way.
- **Every date axis whose ticks step by a whole year or a whole month.** The label carries the unit the ticks step by and nothing finer: fifteen years of January firsts are ticked `2002`, `2004`, `2006`, and a year of month starts is ticked `Jan 2020`, `Mar 2020`, `May 2020`. An axis of a few months carries the month alone; one covering about a year carries the year too. ggplot2 labels both spans the same way.
- **Every date axis whose ticks step by whole days.** The ticks carry their month as well as their day: `Jan-02`, `Jan-05`, `Jan-08`. An axis whose days run across two years names the year in their place, and one whose ticks step by hours is unchanged. ggplot2 labels the same days with a month too.
- **Every stacked bar, area and step drawn from a zero baseline.** The baseline sits on the edge of the panel, where an unstacked bar's already sat. Two stacks of 30 and 70 span 0 to 73.5.
- **Every stacked bar, area and step.** The groups pile up in the order the legend lists them, the first on top, and `:position :fill` stacks the same way round. ggplot2's `position_stack` stacks this way too.
- **Every horizontal dodged chart.** The groups within a band run from the top down, in the order the legend lists them, and bars, boxplots, violins, error bars and point ranges are placed alike. A vertical chart is unchanged: its groups run left to right. ggplot2 runs a horizontal dodge from the bottom up; Plotje matches its legend instead.
- **Every stacked or dodged step.** `pj/lay-step` reads `:position`, so a step drawn with one is stacked or dodged like any other mark.
- **A `:domain` on a categorical `:color`.** The order it gives reaches the marks as well as the legend, so the segments of a stacked bar and the members of a dodged group follow the rows of the legend beside them -- bars, boxplots and violins alike.
- **Every plot setting a `:midpoint`.** The gradient is centred on the midpoint and widened until it reaches whichever end of the domain is further from it -- ggplot2's `rescale_mid` -- so a departure of the same size in either direction is drawn at the same saturation, and the nearer end stops short of the extreme.
- **A `:midpoint` at an end of its domain.** The value there is drawn at the middle of the gradient, so `{:midpoint 0}` on a column of zero and up draws zero at the middle colour.
- **Every faceted plot with a `:size`, `:alpha` or numeric `:color` column, and every plot with two such layers.** The aesthetic is read against every value the plot holds rather than against one panel's or one layer's, so a value means one size, one opacity and one colour wherever it is drawn, and the legend explains all of them. ggplot2 keeps one scale across facets and layers as well.

### A value the axis has to reach

- `:include` on an `:x` or `:y` scale names a value the axis has to reach, as ggplot2's `expand_limits(y = 0)` does. The value lands exactly at the edge of the panel, where `pj/lay-bar` already draws its baseline -- ggplot2 pads past it instead. A collection names more than one: `{:include [0 100]}` gives an axis room the data does not fill. On a temporal axis it is written as a date, as `:domain` and `:breaks` are, and the axis is ticked across the domain it widened.
- A `:domain` and an `:include` on one aesthetic are reported, as is an `:include` on a log axis, which has no reading for zero, or on a categorical one, which has no extent between its categories.

### Checking a pose

- `pj/valid-pose?` and `pj/explain-pose` check a pose against its schema, as the pair `pj/valid-plan?` and `pj/valid-membrane?` already did for the two later stages. The schema is structural: it says what shape a pose has, not whether the columns it names are in the data. A pose the API built conforms by construction, so the pair is for one that has been reached into and changed.

### Reported rather than passed over

- An axis `:domain` is checked against the column it is read for. Against a continuous column it is two finite numbers, and against a categorical one it is the list of categories that orders them.
- `:x-type`, `:y-type` and `:color-type` report a type the column cannot be read as. Only `:categorical` can be asked of a column holding something else; `:numerical` and `:temporal` are accepted where they name the column's own type. The message names the option, the column and what the column holds.
- A row dropped for a missing value on a non-numeric column says so, as a row dropped for a missing number already did: `Removed 1 rows with a missing category in :species`. A column holding no values at all reports a missing *value* rather than a category. Marks that keep such rows rather than dropping them stay silent.
- A stat that needs a numeric column reports one it cannot read, whatever the column is named, and names a way forward: dates written as text stay categorical until they are parsed. The conversion it names is the one that reads that column, so a plain date is offered `:local-date` and a timestamp written `2015-03-21T10:00:00Z` is offered `:instant`. `pj/lay-histogram`, `pj/lay-density`, `pj/lay-smooth`, `pj/lay-tile` and `pj/lay-contour` all report it.
- A mark that groups by category says an empty column has no rows, and one holding nothing but `nil` that it has no values, rather than calling either numerical.
- `:text` given a value that names no column says which value it was.
- A mark that draws one colour per group reports a numeric `:color` column, which it cannot read as a gradient -- bars, boxplots, violins, lollipops, lines, steps, areas, histograms, densities, smooths, rugs, text and labels among them. `:point` is the mark that carries a colour per row, and a contour reads the `:color` scale its levels are built from. The message names `:color-type :categorical`.
- `pj/to-drawing` and `pj/to-data` report a pose, a plan or the whole frames map written in the panel's place. A panel entry is one element of `(:panels (pj/frames plot))`, and each message names which of the three it was given and the call that reaches a panel entry from there.

### Fixed

- `pj/lay-area` and `pj/lay-lollipop` rest on a log value axis: an area fills to the bottom of the panel and a lollipop keeps its stems. The baseline is the panel's own smallest value, which is where `pj/lay-bar` already put it. Both closed to zero instead, which a log scale has no reading for, so an area drew a bowtie and a lollipop lost every stem.
- A histogram bar that cannot be drawn on a log value axis is dropped, and the drop is reported. An empty bin counts zero, so the bar's top scaled to an infinity that reached both the SVG and the Membrane drawables: Java2D clipped it without a word, and Skia refused the drawing with `Value out of range for float: Infinity`. ggplot2 drops the same bars.
- A `ZonedDateTime` and an `OffsetDateTime` column is read as a date. Each carries its own offset, so each names an instant, and both are read at UTC as the other temporal types are. A `ZonedDateTime` was refused by the tick generator, and an `OffsetDateTime` was drawn as one tick per value without a word.
- A column of values naming a stretch of time rather than a moment is reported: a `YearMonth`, a `Year`, a `MonthDay`. The message names what the column holds and the line that maps it to the day it starts. Which instant a month stands for is left to the writer.
- A temporal axis takes a `:domain` written as dates, as its `:breaks` do, and is ticked across it. A domain wider than the data reaches both ends of the axis.
- A date axis takes `:breaks` written as dates: `(pj/scale :x {:breaks [(jt/local-date 1940 1 1) ...]})` places a tick at each of them, and `:tick-labels` relabels them. A `LocalDate`, a `LocalDateTime` and a `java.util.Date` are all accepted.
- A date axis spanning under two seconds is ticked on round fractions of a second, and each tick is labelled to the precision its own step needs: half a second of data reads 00.0 through 00.5. Milliseconds were the one unit the step list did not reach, so such a span was divided by the tick count instead and labelled at tenths.
- A temporal axis with a single tick is labelled, and the tick is drawn where the value sits. The tick sat at the edge of the panel, carrying a date years away from the mark.
- Rows written as vectors are read as data: `(pj/lay-point [[1 2] [3 4]])` draws two points, with columns 0 and 1 inferred as the `:x` and `:y` aesthetics. `pj/pose` accepted the same shape throughout.
- A layer that names no scale has no opinion about it on every aesthetic, as it always has on an axis, and is drawn against the one scale the plot settles on. Two layers that each name a different scale are still refused.
- A size or alpha legend labels only values the data reaches, so a swatch always pairs with a value some mark carries.
- A continuous legend's bar is drawn through the normalization its marks are, so a `:midpoint` moves the bar with them. The bar spans the stretch of gradient the data reaches and no more, and the colour at the end labelled with the domain's low value is the colour the lowest mark carries.
- A `:fill` legend is drawn through the same scale its marks are. A `:domain` set on `:fill` moves the bar together with the tiles, and a setting written in a scale spec wins over the matching plot option -- `:range` and `:midpoint` alike. A tile reads `:color` as a synonym for `:fill` key by key, so a `:fill` spec naming only a gradient leaves the domain to `:color`.
- A log `:fill` legend spans the values the tiles carry. Where the domain was read from the stat's zero floor, the bar carried a label for every decade from 1e-300, printed over each other. A fill that reaches zero even so -- a `:domain` written from it, or a density that underflows -- is reported.
- A contour's legend reads the `:color` scale its levels are built from. `:fill` paints the interior of a tile and a contour draws lines, so a `:fill` scale set on one changes neither the lines nor the bar beside them.

## [0.9.0 - 2026-08-19]

A mapping may now be written out in full, saying which column or value it takes and whether to read it through a scale -- `{:size {:column :weight :scale :log}}`. A scale is written in the mapping it belongs to, and a scale set higher up carries down to the layers below it.

Three chapters are new to the book: [Specifying Aesthetics](https://scicloj.github.io/plotje/plotje_book.specifying_aesthetics.html), [Column Types](https://scicloj.github.io/plotje/plotje_book.column_types.html) and [Scales](https://scicloj.github.io/plotje/plotje_book.scales.html).

### Removed and renamed

The names below no longer work, and every place one can be written reports it: `pj/options`, `pj/scale`, `pj/with-config`, `pj/set-config!`, `plotje.edn` and the `:config` option.

| Gone | Write instead |
|:--|:--|
| `:palette` | `:color-values`, or `:values` in a `:color` scale |
| `:color-scale` | `:color-range`, or `:range` in a `:color` or `:fill` scale |
| `:tick-spacing-x`, `:tick-spacing-y` | `:x-tick-spacing`, `:y-tick-spacing`, or `:tick-spacing` in an axis scale |
| `:labels` in a scale | `:tick-labels` |
| `:x-scale`, `:y-scale`, `:size-scale` and the rest in a pose's `:opts` | `pj/scale`, which now writes the mapping |
| `pj/scale` on `:group` | scale `:color` or `:shape` instead |

### Plots that look different after upgrading

- **Every plot with a `:size` column.** A value's place in its domain is square-rooted before it becomes a radius, matching ggplot2's `scale_size`. `{:by :linear}` gives the previous behaviour, ggplot2's `scale_radius`.
- **Every plot with an `:alpha` column.** The default range is 0.1 to 1.0, matching ggplot2. `{:range [0.2 1.0]}` restores the previous one.
- **Every plot pairing a numeric `:color` column with `pj/scale`.** A scale spec changes only what it names and leaves the gradient alone. The gradient is set with `:range`.
- **A `:size` or `:alpha` column whose values are all equal.** Every mark is drawn at the middle of the domain, read through the scale: radius 6.243 by default.
- **`:from-zero` on a column holding zero or negative values.** Distance from zero is what counts, so -5 and 5 draw the same size.
- **A categorical `:color` given a `:domain`.** The list sets the order of the categories, and the legend and the colours follow it.
- **A plot setting both a scale's `:label` and the matching `:x-label` or `:y-label`.** The `:label` in the scale wins.

### A mapping written in full

- A mapping may say which column or value it takes and whether to read it through a scale: `{:color {:column :variety :scale true}}`, `{:color {:column :hex :scale false}}`, `{:color {:value "blue" :scale false}}`, `{:color {:value "Model A" :scale true}}`.
- A column can be drawn just as it is, with no scale. `{:size {:column :r :scale false}}` uses the column's numbers as radii and `{:color {:column :hex :scale false}}` uses its strings as colours -- ggplot2's `scale_size_identity()` and `scale_colour_identity()`. Neither gets a legend.
- `:x` and `:y` take the full form too: `{:x {:column 0}}` reads column 0, and `{:x {:value 0}}` places every mark at zero. `:scale false` on an axis places marks by drawing units from the top left of the panel background instead of by data value.
- Whether a mapping value names a column or is a value to draw depends on the data, and works the same way for every aesthetic: `{:color :red}` reads a `:red` column if the data has one, and otherwise draws red.

### The scale spec

- `pj/scale` and a mapping's `:scale` take the same forms: `true`, `false`, a scale type, or a whole spec map. A scale set higher up carries down, key by key, and the setting closest to the layer wins -- so a pose that sets a range and a layer that sets a type give a plot with both.
- `pj/scale :size` takes `:range`, `:by` and `:from-zero`; `:alpha` takes `:range` and `:from-zero`. `:range` is what the aesthetic spans: on `:size` that is a radius in drawing units, `[2 8]` by default.
- `:domain` now works on `:size`, `:alpha` and `:color` (issue #39). A value outside a numeric domain is drawn at the nearer end instead of being dropped. The column's type decides how the domain is read, on every aesthetic that takes one. On `:fill` a `:domain` moves the marks and not the legend, so the two disagree -- issue #43.
- An aesthetic has a single scale, so two layers cannot read it through different ones. The error suggests `pj/arrange`, where each cell has its own scales and legend.
- Giving a scale a key it does not use is an error, both in `pj/scale` and in a mapping, and so is giving one a value it cannot carry out -- a marker symbol that does not exist, `:tick-labels` without `:breaks`. `:x-end`, `:x-min`, `:x-max`, `:y-min`, `:y-max` and `:text` take no scale at all, like `:group`.
- `pj/aesthetic-scales` lists what each aesthetic's scale accepts, alongside `pj/config-key-docs`, `pj/plot-option-docs` and `pj/layer-option-docs`.

### One setting, one name

Many scale settings can be written in two places: in the scale itself, or as a plot option that applies to the whole plot. The plot option is named after the aesthetic and the setting, joined by a hyphen -- `:label` on `:x` is `:x-label`, `:values` on `:color` is `:color-values` -- and the setting written closest to the layer wins. Not every setting has a plot option, but where one exists this is its name.

- An axis takes its tick text from `:tick-labels`, paired with `:breaks`; `:label` sets the axis title. Using one where the other belongs is now an error.
- `:n-ticks` and `:tick-spacing` both control how many ticks a numeric axis draws, and it uses whichever you set. A categorical axis draws a tick per category, and `:n-ticks` thins them.
- `:color` takes its colours from its own scale: `:values` is the palette for a categorical column, `:range` the gradient for a numeric one, and `:midpoint` the value drawn at the middle of that gradient. `:fill` draws a magnitude, so it needs a numeric column and takes `:range` and `:midpoint` alone. Each of those has a plot option of the same name.
- `:label` sets the title of whatever explains a scale -- the axis for `:x` and `:y`, the legend for the rest -- and every aesthetic with a scale takes it.

### Extending a layer type

- A layer type says which appearance aesthetics its mark varies per row, and what it draws them as: `(layer-type/register! :bubble {:mark :bubble :varies {:size :radius} ...})`. Of the built-in layer types only `:point` varies an aesthetic, so this mainly matters when writing an extension.
- `layer-type/channel-magnitude-fn` gives a mark's renderer the function its legend is built from, and `layer-type/quantities` lists what a mark can draw an aesthetic as.
- A `:size` or `:alpha` column gets no legend when no mark on the plot varies it, and Plotje says so.

### Pose and plan shape

- `pj/scale` writes the mapping rather than the pose's options. `pj/coord` and `pj/facet` still write `:opts`.
- A size-legend entry carries `:magnitude` rather than `:radius`, and the legend carries `:quantity` and `:swatch`.

### Reported rather than passed over

- Messages say *aesthetic* where they used to say *channel*.
- Mistakes in a full mapping -- an unknown key, a column and a value at once, a source of `nil`, or neither -- are reported at the `pj/pose` or `lay-*` call instead of later.
- Setting `:scale false` and then setting an option for that scale is an error, since nothing would read it. `:scale false` on an axis is also an error on marks that cannot use it.
- Giving `:size`, `:alpha` or `:fill` a categorical column reports which aesthetic and which column. `:fill` and `:group` report a value that names no column, and a hex colour missing its `#` is reported on annotations as well as layers.
- A configuration key Plotje does not read is reported wherever it is written: `plotje.edn`, `pj/set-config!`, `pj/with-config` and the `:config` option. `:strict` turns the report into an error, as it does for options.

### Fixed

- A gradient and a colour scale type can be set together: `(pj/options {:color-range :viridis})` beside `(pj/scale pose :color :log)`.
- A log colour legend labels its decades, as a fill legend does.
- A size or alpha legend measures its own text, so its numbers and a long `:size-label` fit the canvas at any range.
- A panel where nothing places a mark through the x scale falls back to the `[0 1]` domain the y axis has always used.
- An annotation takes a colour by either spelling: `(pj/lay-rule-h pose {:y-intercept 2 :color :red})` draws red.
- `:domain-padding` is read through the configuration chain, so `(pj/options {:domain-padding 0.0})` and `pj/with-config` both reach it.

### Repository

- `run_tests.sh`, `release.sh` and `snapshot.sh` find bash through `#!/usr/bin/env bash`, so they run on distributions that do not keep it at `/bin/bash`, such as NixOS. (PR #35) - thanks, @otfrom

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
