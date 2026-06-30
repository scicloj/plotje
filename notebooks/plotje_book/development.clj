;; # Development
;;
;; How to set up a development environment, run tests,
;; author notebooks, and release new versions.

^:kindly/hide-code
(ns plotje-book.development)

;; ## REPL Setup
;;
;; Start an nREPL server with the `:dev` alias, which adds
;; notebooks, tests, Clay, and related dependencies to the classpath. One way to do it is the following:
;;
;; ```bash
;; clojure -M:dev -m nrepl.cmdline
;; ```
;;
;; Connect to the REPL from your editor (Calva, CIDER, Cursive, etc.)
;; using the port printed to the console (also written to `.nrepl-port`).
;;
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
;; core logic (pose resolution, plan resolution, rendering, etc.)
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
;; generate test assertions. Do not add comments to them.
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
