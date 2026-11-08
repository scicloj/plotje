(ns scicloj.plotje.tile-seams-test
  "Heatmap tiles are drawn with `shape-rendering crispEdges`, by the rule
   filled bars are, so neighbouring tiles of one colour meet with no
   anti-aliased seam. A rectangle thinner than one drawing unit is left
   to anti-alias, and an outline is never snapped."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(defn- rects [pose]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= :rect (first %)) (map? (second %))))
       (map second)))

(def grid {:a (vec (for [i (range 6) _ (range 6)] i))
           :b (vec (for [_ (range 6) j (range 6)] j))
           :z (vec (repeat 36 1.0))})

(deftest heatmap-tiles-are-snapped-test
  (let [pose (pj/lay-tile grid :a :b {:fill :z})
        snapped (filter #(= "crispEdges" (:shape-rendering %)) (rects pose))]
    (testing "every tile is snapped"
      (is (<= 36 (count snapped))))
    (testing "an outline is not snapped"
      (is (not-any? #(and (= "crispEdges" (:shape-rendering %))
                          (= "none" (:fill %)))
                    (rects pose))))))

(deftest a-thin-rectangle-is-left-to-anti-alias-test
  ;; 400 columns across a 300-unit-wide panel: each tile is under one
  ;; drawing unit wide.
  (let [n 400
        thin {:a (vec (range n)) :b (vec (repeat n 0)) :z (vec (range n))}
        pose (-> (pj/lay-tile thin :a :b {:fill :z})
                 (pj/options {:width 300}))]
    (is (not-any? #(and (= "crispEdges" (:shape-rendering %))
                        (< (double (:width %)) 1.0))
                  (rects pose)))))
