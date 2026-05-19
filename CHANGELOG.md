# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
- fix: SVG coordinate formatter now pins `java.util.Locale/ROOT`, so plots render correctly on JVMs whose default locale uses comma as the decimal separator (Czech, German, etc.). Thanks to [@tombarys](https://github.com/tombarys) (#3).

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
