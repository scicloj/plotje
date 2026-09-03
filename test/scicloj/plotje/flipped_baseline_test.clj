(ns scicloj.plotje.flipped-baseline-test
  "An area or density under `(pj/coord :flip)` is measured from the
   value axis. `make-coord :flip` is `(fn [dx dy] [(sx dy) (sy dx)])`,
   so the baseline reaches it as the argument scaled by `sx`, and the
   plan has already swapped the domains for a flip -- so the value axis
   is x there. Reading the y domain gave the column's minimum instead."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [scicloj.plotje.api :as pj]))

(def iris (rdatasets/datasets-iris))

(def ^:private panel-background "rgb(232,232,232)")

(defn- panel-width [pose]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %)
                     (= :rect (first %))
                     (= panel-background (:fill (second %)))))
       (map (comp :width second))
       first
       double))

(defn- polygon-xs [pose]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= :polygon (first %))))
       (map (comp :points second))
       (mapcat (fn [pts]
                 (map #(parse-double (first (str/split % #",")))
                      (str/split pts #"\s+"))))))

(deftest flipped-density-stays-inside-its-panel-test
  (testing "a density under coord :flip does not overrun the panel"
    ;; It was drawn about ten panel widths wide: the column's minimum,
    ;; 4.12, closed a fill on a 0-to-0.42 scale.
    (let [pose (-> iris (pj/lay-density :sepal-length) (pj/coord :flip))]
      (is (<= (reduce max 0.0 (polygon-xs pose))
              (+ (panel-width pose) 20.0)))))
  (testing "and it is filled from the value axis"
    (let [pose (-> iris (pj/lay-density :sepal-length) (pj/coord :flip))]
      (is (< (reduce min Double/MAX_VALUE (polygon-xs pose)) 30.0)))))

(deftest flipped-area-is-measured-from-zero-test
  (testing "a flipped area closes at zero, not at its column's smallest value"
    ;; This one stayed inside the panel and so went unnoticed: the fill
    ;; began nine-tenths of the way across instead of at the edge.
    (let [pose (-> iris
                   (pj/lay-area :sepal-length :sepal-width)
                   (pj/coord :flip))]
      (is (< (reduce min Double/MAX_VALUE (polygon-xs pose)) 30.0)))))

(deftest unflipped-areas-are-unchanged-test
  (testing "an upright area still measures from its own baseline"
    (let [pose (pj/lay-area iris :sepal-length :sepal-width)]
      (is (= 1 (:panels (pj/svg-summary pose))))
      (is (pos? (count (polygon-xs pose)))))))
