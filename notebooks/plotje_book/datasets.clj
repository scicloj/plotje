;; # Datasets
;;
;; **You do not need to know about datasets to plot with Plotje** --
;; you can pass plain Clojure data (maps, vectors of maps) directly.
;; But understanding datasets is recommended background for four reasons:
;;
;; - **Performance**: datasets are columnar and backed by typed arrays.
;;   For large data (thousands of rows and above), they are significantly
;;   faster than plain Clojure maps and vectors.
;;
;; - **Ergonomics**: many people find that dataset operations -- filtering,
;;   grouping, aggregation -- read more naturally as a pipeline than the
;;   equivalent Clojure core code. This is a matter of taste, but the
;;   convention is widespread in the Clojure data science ecosystem.
;;
;; - **Column types matter for plotting**: dataset columns carry type
;;   information (numeric, categorical, temporal) that Plotje uses
;;   to choose scales, axis formatting, and statistical transforms. A column
;;   of doubles gets a continuous axis; a column of strings gets a
;;   categorical axis. When you pass plain Clojure data, types are
;;   inferred on coercion -- sometimes not the way you expect. Working
;;   with datasets gives you explicit control.
;;
;; - **Understanding Plotje internals**: Plotje coerces your
;;   data to a dataset internally. Knowing what a dataset is helps you
;;   understand column names, types, and the inference rules.
;;
;; This chapter gives a brief introduction. For full documentation, see
;; the [Tablecloth](https://scicloj.github.io/tablecloth/) and
;; [tech.ml.dataset](https://techascent.github.io/tech.ml.dataset/) docs.

(ns plotje-book.datasets
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   [clojure.string :as str]))

;; ## Plain Data Works
;;
;; Plotje accepts plain Clojure data -- a map of columns or a
;; vector of row maps. No dataset wrapping needed:

(-> [{:month "Jan" :temperature 5}
     {:month "Feb" :temperature 7}
     {:month "Mar" :temperature 12}
     {:month "Apr" :temperature 16}]
    (pj/lay-line :month :temperature)
    pj/lay-point)

(kind/test-last [(fn [v] (= 4 (:points (pj/svg-summary v))))])

;; This is all you need for quick plots. The rest of this chapter
;; covers datasets, which become useful as your data grows.

;; ## What Is a Dataset?
;;
;; A dataset is a columnar table backed by efficient typed arrays.
;; It is the Clojure equivalent of an R data frame or a Python pandas
;; DataFrame.
;;
;; The core implementation is
;; [tech.ml.dataset](https://techascent.github.io/tech.ml.dataset/).
;; [Tablecloth](https://scicloj.github.io/tablecloth/) is a
;; higher-level wrapper with a more ergonomic API. Plotje
;; uses Tablecloth internally and in its documentation.

;; ## Creating Datasets
;;
;; ### From a map of columns

(tc/dataset {:x [1 2 3 4 5]
             :y [10 20 15 30 25]})

(kind/test-last [(fn [ds] (= 5 (tc/row-count ds)))])

;; ### From a vector of row maps

(tc/dataset [{:name "Alice" :score 92}
             {:name "Bob"   :score 85}
             {:name "Carol" :score 97}])

(kind/test-last [(fn [ds] (= 3 (tc/row-count ds)))])

;; ### From a sequence of row vectors

(tc/dataset [["Alice" 92]
             ["Bob"   85]
             ["Carol" 97]]
            {:column-names [:name :score]})

(kind/test-last [(fn [ds] (= 3 (tc/row-count ds)))])

;; ### From a CSV or URL

(tc/dataset "https://vincentarelbundock.github.io/Rdatasets/csv/datasets/iris.csv"
            {:key-fn keyword})

(kind/test-last [(fn [ds] (= 150 (tc/row-count ds)))])

;; (The `:key-fn keyword` option converts CSV string headers like
;; `"Sepal.Length"` to keywords like `:Sepal.Length`. Without it,
;; column names remain strings.)

;; ## The RDatasets collection
;;
;; Many examples in this book use datasets from the
;; [RDatasets](https://vincentarelbundock.github.io/Rdatasets/articles/data.html)
;; collection -- over 2,300 datasets from R packages, available as CSV
;; files.
;;
;; The Clojure bridge is provided by the
;; [metamorph.ml](https://github.com/scicloj/metamorph.ml) library.
;; You can add it as a direct dependency:
;; [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/metamorph.ml.svg)](https://clojars.org/org.scicloj/metamorph.ml)
;;
;; Or use the [Noj](https://scicloj.github.io/noj/) toolkit, which
;; includes it along with other data science libraries.
;;
;; Each dataset has a memoized accessor function. The first call
;; fetches the CSV from the web; subsequent calls return the cached
;; dataset instantly:

(rdatasets/datasets-iris)

(kind/test-last [(fn [ds] (and (tc/dataset? ds)
                               (= 150 (tc/row-count ds))))])

;; Column names are kebab-case keywords (`:sepal-length`, not
;; `Sepal.Length`).
;;
;; A few datasets used throughout this book:

^:kindly/hide-code
(-> {:var [#'rdatasets/datasets-iris
           #'rdatasets/reshape2-tips
           #'rdatasets/ggplot2-mpg
           #'rdatasets/ggplot2-diamonds
           #'rdatasets/gapminder-gapminder
           #'rdatasets/datasets-mtcars]}
    tc/dataset
    (tc/map-columns :function :var
                    #(-> % meta :name))
    (tc/map-columns :dataset :var
                    #(%))
    (tc/map-columns :rows :dataset
                    tc/row-count)
    (tc/map-columns :description :var
                    #(-> %
                         meta
                         :doc-link
                         slurp
                         str/split-lines
                         first
                         (str/replace "<!DOCTYPE html><html><head><title>R: " "")
                         (str/replace "</title>" "")))
    (tc/select-columns [:function :rows :description]))

;; ## Useful Tablecloth operations
;;
;; The examples in this book use a handful of Tablecloth functions.
;; Here is a quick reference:

;; `tc/head` -- first N rows:

(tc/head (rdatasets/datasets-iris) 3)

(kind/test-last [(fn [ds] (= 3 (tc/row-count ds)))])

;; `tc/select-rows` -- filter rows by predicate:

(-> (rdatasets/datasets-iris)
    (tc/select-rows #(= "setosa" (:species %))))

(kind/test-last [(fn [ds] (= 50 (tc/row-count ds)))])

;; `tc/group-by` and `tc/aggregate` -- split and summarize:

(-> (rdatasets/datasets-iris)
    (tc/group-by [:species])
    (tc/aggregate {:mean-sl (fn [ds] (/ (reduce + (ds :sepal-length))
                                        (tc/row-count ds)))}))

(kind/test-last [(fn [ds] (= 3 (tc/row-count ds)))])

;; `tc/order-by` -- sort rows:

(-> (rdatasets/datasets-mtcars)
    (tc/order-by [:mpg] :desc)
    (tc/head 3))

(kind/test-last [(fn [ds] (= 3 (tc/row-count ds)))])

;; `tc/column-names` -- list columns:

(tc/column-names (rdatasets/datasets-iris))

(kind/test-last [(fn [cols] (= 6 (count cols)))])

;; `tc/row-count` -- number of rows:

(tc/row-count (rdatasets/ggplot2-diamonds))

(kind/test-last [(fn [n] (= 53940 n))])

;; ## Datasets and Plotje
;;
;; When you pass plain data to Plotje, it is coerced to a dataset
;; internally. You can also pass a dataset directly -- the result is the
;; same:

;; Plain data:

(-> {:x [1 2 3] :y [4 5 6]}
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Dataset:

(-> (tc/dataset {:x [1 2 3] :y [4 5 6]})
    (pj/lay-point :x :y))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Both produce the same plot. Use whichever is more convenient for
;; your workflow.

;; ## From Wide to Long
;;
;; Plotje plots **long-form** (tidy) data: one row per observation,
;; with categories and groupings in their own columns. Real
;; datasets often arrive in **wide form** -- each measurement in
;; its own column. `tc/pivot->longer` is the canonical reshape:

(def temps-wide
  (tc/dataset
   {:month   ["Jan" "Feb" "Mar"]
    :tokyo   [3 5 9]
    :paris   [4 6 11]
    :nairobi [22 23 24]}))

temps-wide

(kind/test-last [(fn [ds] (= 4 (count (tc/column-names ds))))])

;; Three city columns become two: a `:city` label column and a
;; `:temperature` value column. The row count triples (3 months
;; times 3 cities equals 9):

(def temps-long
  (tc/pivot->longer temps-wide [:tokyo :paris :nairobi]
                    {:target-columns :city
                     :value-column-name :temperature}))

temps-long

(kind/test-last [(fn [ds] (and (= 3 (count (tc/column-names ds)))
                               (= 9 (tc/row-count ds))))])

;; Plot the long form by mapping the new label column to `:color`:

(-> temps-long
    (pj/lay-line :month :temperature
                 {:color :city}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 3 (:lines s)))))])

;; The inverse, `tc/pivot->wider`, reshapes long back to wide --
;; useful for tabular reports but rarely the right shape for
;; plotting.

;; ## What's Next
;;
;; - [**Poses**](./plotje_book.pose_model.html) -- how Plotje composes layers, aesthetics, and layer types
;; - [**Quickstart**](./plotje_book.quickstart.html) -- if you skipped straight here, go back and build your first plots
