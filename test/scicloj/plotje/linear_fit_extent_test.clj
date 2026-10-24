(ns scicloj.plotje.linear-fit-extent-test
  "A straight line fitted to the rows can end above or below every row;
   the axis covers its ends, so the line is not cut off at the edge of
   the panel."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def data {:a [0 1 2 3] :b [0 0 1 1]})

(deftest a-fitted-line-is-inside-the-axis-test
  ;; The least-squares line through these rows runs from -0.1 to 1.1.
  (doseq [band [false true]]
    (testing (str "with :confidence-band " band)
      (let [panel (-> data
                      (pj/lay-point :a :b)
                      (pj/lay-smooth {:stat :linear-model :confidence-band band})
                      pj/plan :panels first)
            [lo hi] (:y-domain panel)]
        (is (< lo -0.1))
        (is (> hi 1.1)))))
  (testing "a line inside the data leaves the axis as the data gives it"
    (let [straight {:a [0 1 2 3] :b [0 1 2 3]}
          domain (fn [p] (-> p pj/plan :panels first :y-domain))]
      ;; Up to rounding: the fitted ends are computed, not read.
      (is (every? #(< (Math/abs (double %)) 1e-9)
                  (map - (domain (pj/lay-point straight :a :b))
                       (domain (-> straight
                                   (pj/lay-point :a :b)
                                   (pj/lay-smooth {:stat :linear-model})))))))))
