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

(deftest a-matrix-reserves-tick-room-only-where-ticks-are-drawn-test
  (let [cols [:sepal-length :sepal-width :petal-length :petal-width]
        subs (:sub-plots (pj/plan (pj/facet (rdatasets/datasets-iris) {:col {:x cols} :row {:y cols}})))
        x-pad #(get-in % [:plan :layout :x-label-pad])
        last-row? #(= 3 (first (:path %)))]
    (testing "the rows above the last draw no :x tick labels and reserve no room for them"
      (is (every? #(zero? (double (x-pad %))) (remove last-row? subs)))
      (is (every? #(pos? (double (x-pad %))) (filter last-row? subs))))
    (testing "the last row is taller by its tick labels, so every panel is as tall"
      ;; Each row is as tall as the common drawing height plus its own
      ;; pad; rounding to whole drawing units leaves at most one apart.
      (let [heights (map #(double (get-in % [:plan :panel-height])) subs)]
        (is (<= (- (apply max heights) (apply min heights)) 1.0))))))

(deftest a-matrix-takes-rotated-tick-labels-test
  (let [cols [:sepal-length :sepal-width :petal-length :petal-width :species]
        subs (:sub-plots (pj/plan (-> (pj/facet (rdatasets/datasets-iris) {:col {:x cols} :row {:y cols}})
                                      (pj/options {:x-tick-angle -45}))))
        heights (map #(double (get-in % [:plan :panel-height])) subs)]
    (testing "a cell 76 high keeps a panel under rotated labels"
      ;; The room for rotated labels is capped at 30% of the cell's
      ;; height, its least as well as its most; uncapped, the least
      ;; left the bottom row's panels 16 high, and the plot failed.
      (is (= 25 (count subs))))
    (testing "and every panel is as tall, the bottom row's labels below it"
      (is (<= (- (apply max heights) (apply min heights)) 1.0)))))
