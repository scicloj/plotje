(ns scicloj.plotje.matrix-ticks-test
  "Which cells of a matrix keep their tick labels is decided in one
   place (`pose/infer-chrome`), so a scatter plot matrix is ticked alike
   whether `pj/facet` is given the data or a pose."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

(deftest a-matrix-is-ticked-as-the-same-facet-is-test
  (let [mpg (rdatasets/ggplot2-mpg)
        cols [:displ :hwy :cty]
        matrix (pj/facet mpg {:col {:x cols} :row {:y cols}})
        facet (pj/facet (pj/pose mpg) {:col {:x cols} :row {:y cols}})
        texts #(sort (:texts (pj/svg-summary %)))]
    (testing "the data and a pose draw the same text"
      (is (= (texts facet) (texts matrix))))
    (testing "the first row's column is ticked on :y as well as on :x"
      ;; Written by row and column, only the leftmost cell of a row kept
      ;; its ticks, and in the first row that cell is the histogram: the
      ;; tick label 6 of :displ was drawn once, under the first column.
      (is (= 2 (count (filter #{"6"} (texts matrix))))))))
