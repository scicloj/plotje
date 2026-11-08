;; # Development
;;
;; How to set up a development environment, run tests,
;; author notebooks, and release new versions.

^:kindly/hide-code
(ns plotje-book.development
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]))

;; ## REPL Setup
;;
;; Start an nREPL server with the `:dev` alias, which adds
;; notebooks, tests, Clay, and related dependencies to the classpath.
;; One way to start the server is the following:
;;
;; ```bash
;; clojure -M:dev -m nrepl.cmdline
;; ```
;;
;; Connect to the REPL from your editor (Calva, CIDER, Cursive, or
;; another)
;; using the port printed to the console (also written to `.nrepl-port`).
;;
;; ## Namespace Structure
;;
;; The source is split by pipeline stage (see
;; [Architecture](./plotje_book.architecture.html#pipeline-overview)).
;; In the diagram, an arrow runs from a namespace to a namespace it
;; requires, and the dashed arrow points to a namespace that is loaded
;; the first time it is needed. The diagram leaves out helper
;; namespaces such as `impl/text.clj` and `impl/rowid.clj`.

^:kindly/hide-code
(kind/mermaid "
graph TD
  API[\"api.clj\"] --> POSE[\"impl/pose.clj\"]
  API --> RES[\"impl/resolve.clj\"]
  API --> PL[\"impl/plan.clj\"]
  API --> COMP[\"impl/compositor.clj\"]
  POSE --> RES
  COMP --> POSE
  COMP --> PL
  PL --> RES
  PL --> STAT[\"impl/stat.clj\"]
  PL --> SCALE[\"impl/scale.clj\"]
  PL --> DEFAULTS[\"impl/defaults.clj\"]
  PL --> PS[\"impl/plan_schema.clj\"]
  PL --> LAYOUT[\"impl/layout.clj\"]
  API --> FRAMES[\"impl/frames.clj\"]
  API --> IMEM[\"impl/membrane.clj\"]
  API --> RENDER[\"impl/render.clj\"]
  API --> SVG[\"render/svg.clj\"]
  SVG --> RENDER
  BUFIMG[\"render/bufimg.clj\"] --> RENDER
  RENDER -.->|loaded on first use| BUFIMG
  SVG --> MEMBRANE[\"render/membrane.clj\"]
  BUFIMG --> MEMBRANE
  MEMBRANE --> IMEM
  MEMBRANE --> FRAMES
  MEMBRANE --> LAYOUT
  MEMBRANE --> PANEL[\"render/panel.clj\"]
  PANEL --> MARK[\"render/mark.clj\"]
  PANEL --> SCALE
  PANEL --> COORD[\"impl/coord.clj\"]
  API --> RC[\"render/composite.clj\"]
  RC --> MEMBRANE
  RC --> IMEM
  style API fill:#c8e6c9
  style COMP fill:#d1c4e9
  style PL fill:#d1c4e9
  style SVG fill:#f8bbd0
  style MEMBRANE fill:#f8bbd0
  style RC fill:#f8bbd0
")

;; `impl/pose.clj` holds the pose operations: `resolve-tree` (merges
;; mappings, data and options from the root down to every leaf),
;; `elaborate` (writes in what the draft chooses), `expand-facet-nodes`
;; (a facet over columns as cells), `fit-layers` (the cells of a
;; composite, or of a facet over columns, that draw each layer),
;; `shared-axis-groups` (which cells share an axis), and
;; `leaf->draft` (the leaf flattening that the public `pj/pose->draft`
;; calls).
;;
;; `impl/compositor.clj` handles composite framing layout,
;; `composite-pose->draft`, and `composite-draft->plan` -- pure
;; data-side, no membrane dependency.
;;
;; `impl/plan.clj` holds the leaf-plan computation (domains, ticks,
;; legends, layout) that the public `pj/draft->plan` calls.
;;
;; `impl/layout.clj` sizes the canvas: the room the axes, titles and
;; legends take, and the size of each panel. `impl/frames.clj` turns a
;; plan into the rectangles each panel occupies on the canvas, which
;; `pj/frames` returns.
;;
;; `impl/membrane.clj` defines the `PlotjeMembrane` record, the value
;; of the membrane stage.
;;
;; `impl/resolve.clj` defines the `Plan`, `CompositePlan`,
;; `LeafDraft`, `CompositeDraft`, `PlanLayer`, and `LayerType`
;; records, and holds `resolve-draft-layer` (single draft layer
;; resolution, column type inference, grouping).
;;
;; The `impl/` directory holds data with no Membrane dependency,
;; except `impl/membrane.clj`, whose `PlotjeMembrane` is a Membrane
;; component by definition. The `render/` directory uses Membrane for
;; drawing and for SVG and raster conversion; `render/bufimg.clj`, the
;; raster backend, is loaded the first time a `:bufimg` or `:png` plot
;; is rendered. `render/composite.clj` carries the composite
;; `plan->membrane` defmethod and the membrane drawables for
;; composite framing (title, strip labels, shared legend).

;; ## Running Tests
;;
;; The test suite runs via [cognitect test-runner](https://github.com/cognitect-labs/test-runner):
;;
;; ```bash
;; ./run_tests.sh
;; ```
;;
;; This is equivalent to:
;;
;; ```bash
;; clojure -M:dev:test -m cognitect.test-runner
;; ```
;;
;; There are two kinds of tests:
;;
;; - **Hand-written tests** in `test/scicloj/plotje/` -- unit tests for
;; core logic (pose resolution, plan resolution, rendering, and more)
;;
;; - **Generated tests** in `test/plotje_book/` -- produced from notebooks
;; every time [Clay](https://scicloj.github.io/clay/) renders notebooks
;; using Clay's [test generation](https://scicloj.github.io/clay/clay_book.test_generation.html).
;; Every `kind/test-last` form in a notebook becomes
;; an assertion in the corresponding `*_generated_test.clj` file.

;; ## Notebook Authoring
;;
;; Notebooks live in `notebooks/plotje_book/` and are Clojure source
;; files with `;;` comment blocks for prose (rendered as Markdown by Clay).
;;
;; You can explore the data visualizations interactively with Clay.
;;
;; To add assertions that verify computed values, use `kind/test-last` with a
;; function in a vector:
;;
;; ```clojure
;; (+ 1 2)
;;
;; (kind/test-last [(fn [v] (= 3 v))])
;; ```
;; 
;; You may also add arguments after the function:
;; ```clojure
;; (+ 1 2)
;;
;; (kind/test-last [= 3])
;; ```
;;
;; `kind/test-last` forms are invisible in rendered output -- they only
;; generate test assertions.
;;
;; ## Regenerating Tests
;;
;; After editing a notebook, you may regenerate its test file by rendering it:
;;
;; ```clojure
;; (require '[dev :as dev])
;; (dev/make-gfm! "plotje_book/relationships.clj")
;; ```
;;
;; This updates `test/plotje_book/relationships_generated_test.clj`.
;; The generated test files are checked into version control.
;;
;; To regenerate all notebooks at once:
;;
;; ```clojure
;; (require 'dev)
;; (dev/make-gfm!) ; in Github Flavored Markdown format
;; ;; or:
;; (dev/make-book!) ; as an HTML book
;; ```

;; ## Building the Book
;;
;; The full HTML book is rendered through [Clay](https://scicloj.github.io/clay/)
;; and [Quarto](https://quarto.org/):
;;
;; ```clojure
;; (require '[dev :as dev])
;; (dev/make-book!)
;; ```
;;
;; Chapter ordering is defined in `notebooks/chapters.edn`. The output
;; goes to the `docs/` directory and is published to
;; [scicloj.github.io/plotje](https://scicloj.github.io/plotje/).

;; ## Generating the README
;;
;; The `README.md` is generated from `notebooks/readme.clj` using Clay's
;; GFM (GitHub-Flavored Markdown) output. The notebook contains live code
;; examples that produce SVG plots, so the README always shows current output.
;;
;; ```clojure
;; (require '[dev :as dev])
;; (dev/make-readme!)
;; ```
;;
;; This produces:
;;
;; - `README.md` at the repository root
;; - `readme_files/*.svg` -- rendered plot images referenced by the README
;;
;; The book's `index.clj` reads `README.md` and copies `readme_files/`
;; into `notebooks/` so that the book's front page shows the same images.
;; Run `make-readme!` before `make-book!` when the README content changes.

;; ## Building a JAR
;;
;; The build uses [tools.build](https://clojure.org/guides/tools_build)
;; with [deps-deploy](https://github.com/slipset/deps-deploy):
;;
;; ```bash
;; clojure -T:build ci                 # run tests + build JAR
;; clojure -T:build ci :snapshot true  # build snapshot JAR
;; ```
;;
;; The JAR is written to `target/`.

;; ## Deploying to Clojars
;;
;; Two convenience scripts handle the full build-and-deploy workflow:
;;
;; ```bash
;; ./release.sh    # test + build + deploy release
;; ./snapshot.sh   # test + build + deploy snapshot
;; ```
;;
;; Both run tests first, then build the JAR, then deploy to
;; [Clojars](https://clojars.org/org.scicloj/plotje).
;; Deployment requires `CLOJARS_USERNAME` and `CLOJARS_PASSWORD`
;; environment variables (or a `~/.m2/settings.xml` with credentials).

;; ## Continuous Integration
;;
;; GitHub Actions runs the test suite on every push and pull request
;; to `main` (see `.github/workflows/tests.yml`). The workflow uses
;; Java 21 (Temurin) and the latest Clojure CLI.
