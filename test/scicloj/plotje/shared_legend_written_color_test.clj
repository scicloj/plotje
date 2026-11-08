(ns scicloj.plotje.shared-legend-written-color-test
  "A composite draws one legend for an aesthetic its cells share. A
   layer with a written colour, such as a black trend line drawn in
   some cells, earns no legend entry, so it does not stop the cells
   sharing the legend of the column they colour by."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

(defn- legend-titles [pose]
  (count (filter #{"species"} (:texts (pj/svg-summary pose)))))

(deftest a-written-colour-in-some-cells-keeps-one-legend-test
  (let [iris (rdatasets/datasets-iris)
        black-smooth #(pj/lay-smooth % :sepal-length :sepal-width {:color "black"})]
    (testing "a scatter plot matrix"
      (let [m (-> iris (pj/pose {:color :species}) (pj/matrix [:sepal-length :sepal-width]))]
        (is (= 1 (legend-titles m)))
        (is (= 1 (legend-titles (black-smooth m))))))
    (testing "an arrangement"
      (let [a (-> iris
                  (pj/arrange [{:x :sepal-length :y :sepal-width} {:x :petal-length :y :petal-width}])
                  (pj/lay-point {:color :species}))]
        (is (= 1 (legend-titles (black-smooth a))))))
    (testing "cells carrying their own data"
      (let [c (pj/arrange [(pj/lay-point iris :sepal-length :sepal-width {:color :species})
                           (pj/lay-point iris :petal-length :petal-width {:color :species})])]
        (is (= 1 (legend-titles (black-smooth c))))))))

(deftest a-scale-written-per-cell-keeps-a-legend-per-cell-test
  ;; A scale written for the column is part of what the legend explains:
  ;; cells spreading :n by different rules each draw their own legend.
  (let [squares {:step [1 2 3 4 5 6] :row [1 1 1 1 1 1] :n [1 2 4 8 16 32]}
        cell (fn [by] (cond-> (pj/lay-point squares :step :row {:size :n})
                        by (pj/scale :size {:by by})))
        v (pj/arrange [(cell nil) (cell :linear) (cell :area)])]
    (is (= 3 (count (filter #{"n"} (:texts (pj/svg-summary v))))))))
