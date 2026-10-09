(ns scicloj.plotje.composite-chrome-test
  "What a composite draws around its cells: title, subtitle and caption,
   strips, and the drawing areas `:align-panels` evens out."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:a [1.0 2.0 3.0] :b [1.0 4.0 9.0] :c [100000.0 200000.0 300000.0]
        :n ["a long category" "another long one" "x"]})

(deftest arrange-draws-its-subtitle-and-caption-test
  ;; `pj/arrange` kept only `:title`, and dropped the other two in silence.
  (let [v (pj/arrange [(pj/lay-point d :a :b) (pj/lay-point d :a :c)]
                      {:title "T" :subtitle "SUB" :caption "CAP"})]
    (is (= #{"T" "SUB" "CAP"} (set (filter #{"T" "SUB" "CAP"} (:texts (pj/svg-summary v))))))))

(deftest a-row-of-aligned-cells-has-level-bottoms-test
  ;; A row was aligned as a column, so a cell whose x labels need more
  ;; room stood shorter than its neighbour: [362 277] on 0.16.1.
  (let [v (pj/arrange [(pj/lay-point d :a :b)
                       (-> (pj/lay-bar d :n) (pj/options {:x-tick-angle 90}))]
                      {:align-panels true})
        heights (mapv #(get-in % [:plan :panel-height]) (:sub-plots (pj/plan v)))]
    (is (apply = heights))))
