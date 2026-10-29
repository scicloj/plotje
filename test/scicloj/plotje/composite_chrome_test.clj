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

(deftest a-nested-composite-draws-its-title-once-test
  ;; An arrangement placed as a cell drew its subtitle over each of its
  ;; cells and lost its title.
  (let [inner (pj/arrange [(pj/lay-point d :a :b) (pj/lay-point d :a :c)]
                          {:title "Inner" :subtitle "SUB"})
        texts (:texts (pj/svg-summary (pj/arrange [inner (pj/lay-point d :b :c)]
                                                  {:title "Outer"})))]
    (is (= {"Outer" 1 "Inner" 1 "SUB" 1}
           (select-keys (frequencies texts) ["Outer" "Inner" "SUB"])))))

(deftest a-facet-over-columns-as-a-cell-draws-its-title-once-test
  (let [cell (-> (pj/pose d {:x :a})
                 pj/lay-point
                 (pj/facet {:y [:b :c]})
                 (pj/options {:title "Columns"}))
        v (pj/arrange [cell (pj/lay-point d :b :c)])]
    (is (= 1 (get (frequencies (:texts (pj/svg-summary v))) "Columns")))
    (testing "the band above the block pushes its cells down, not the cell beside it"
      (let [rects (into {} (map (juxt :path :rect)) (:sub-plots (pj/plan v)))]
        (is (= 30.0 (get-in rects [[0 0 0 0] 1])))
        (is (= 0.0 (get-in rects [[0 1] 1])))))))

(deftest a-flipped-facet-over-columns-titles-each-column-test
  ;; Under a flip the column every panel shares is drawn up the side,
  ;; and each panel's own column along the bottom. The check for a
  ;; column every panel draws read the mapping's :x, so the panels were
  ;; titled once along the bottom by the first panel's column.
  (let [texts (:texts (pj/svg-summary (-> (pj/pose d {:x :a})
                                          pj/lay-line
                                          (pj/facet {:col {:y [:b :c]}})
                                          (pj/coord :flip))))]
    (is (= {"a" 1 "b" 1 "c" 1} (select-keys (frequencies texts) ["a" "b" "c"])))))
