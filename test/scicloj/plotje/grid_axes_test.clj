(ns scicloj.plotje.grid-axes-test
  "A grid built from a rectangular pair list -- the SPLOM shape that
   `pj/pose` with `pj/cross` produces -- draws one x axis per column
   and one y axis per row, and suppresses the rest.

   That is only readable if a cell's x really is constant down its
   column and its y constant across its row. When the grid was built
   the other way round, the suppression still kept the bottom row's x
   ticks and the leftmost column's y ticks, so every other row and
   column was left with no axis of its own and a reader took a
   neighbour's numbers for its own: on the grid below, petal-width
   points (never above 2.5) sat under a y axis reading to 6.

   These tests pin both halves -- the arrangement, and the tick
   labels that arrangement is supposed to make readable."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; Four iris columns with well-separated ranges, so a tick label can
;; be traced back to the one column that could have produced it:
;;
;;   :sepal-length [4.3 7.9]   :sepal-width  [2.0 4.4]
;;   :petal-length [1.0 6.9]   :petal-width  [0.1 2.5]
(def cross-2x2
  (-> (rdatasets/datasets-iris)
      (pj/pose (pj/cross [:sepal-length :sepal-width]
                         [:petal-length :petal-width]))
      (pj/options {:width 800 :height 600})))

(defn- cell-mappings
  "The [x y] of each cell, as a vector of rows."
  [pose]
  (mapv (fn [row]
          (mapv (fn [cell] [(get-in cell [:mapping :x])
                            (get-in cell [:mapping :y])])
                (:poses row)))
        (:poses pose)))

(deftest columns-carry-x-rows-carry-y
  (testing "every cell in a column shares one x"
    (let [rows (cell-mappings cross-2x2)]
      (doseq [ci (range (count (first rows)))]
        (is (= 1 (count (set (map (fn [row] (first (nth row ci))) rows))))
            (str "column " ci " must have one x")))))
  (testing "every cell in a row shares one y"
    (doseq [row (cell-mappings cross-2x2)]
      (is (= 1 (count (set (map second row))))
          "a row must have one y")))
  (testing "the cells are the full cross, however they are arranged"
    (is (= #{[:sepal-length :petal-length] [:sepal-length :petal-width]
             [:sepal-width :petal-length]  [:sepal-width :petal-width]}
           (set (apply concat (cell-mappings cross-2x2)))))))

(deftest every-axis-of-the-grid-is-drawn
  (let [texts (set (map str (:texts (pj/svg-summary cross-2x2))))]
    (testing "each x column contributes tick labels"
      ;; 7 is above every other column's maximum, so only
      ;; sepal-length's axis can have drawn it.
      (is (contains? texts "7") "sepal-length has no x axis")
      ;; 3.5 falls between petal-width's maximum and sepal-length's
      ;; minimum, and petal-length is ticked at whole numbers.
      (is (contains? texts "3.5") "sepal-width has no x axis"))
    (testing "each y column contributes tick labels"
      ;; Only petal-width reaches below 1.
      (is (contains? texts "0.5") "petal-width has no y axis")
      ;; Only petal-length is ticked at 1: sepal-length and
      ;; sepal-width start above it, petal-width labels 1 as "1.0".
      (is (contains? texts "1") "petal-length has no y axis"))))

(deftest strip-labels-name-the-axis-they-sit-on
  ;; The column header names the x of the cells beneath it, the row
  ;; header the y of the cells beside it. The composite root carries
  ;; both lists; the compositor draws them from there.
  (is (= ["sepal-length" "sepal-width"]
         (get-in cross-2x2 [:grid-strip-labels :col-labels])))
  (is (= ["petal-length" "petal-width"]
         (get-in cross-2x2 [:grid-strip-labels :row-labels]))))
