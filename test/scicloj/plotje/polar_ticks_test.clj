(ns scicloj.plotje.polar-ticks-test
  "Tick labels under `:coord :polar`: the angle's around the circle, the
   radius's at the left edge, and a grid drawn at the ticks."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def counts {:kind ["a" "b" "c" "d"] :n [3 5 2 4]})

(deftest polar-tick-labels-test
  (let [rose (-> counts (pj/lay-bar :kind :n) (pj/coord :polar))
        texts (set (:texts (pj/svg-summary rose)))]
    (testing "each category is named around the circle"
      (is (every? texts ["a" "b" "c" "d"])))
    (testing "the radius is labelled, from zero, at as many ticks as its length holds"
      (is (texts "0"))
      (is (<= 2 (count (filter #(re-matches #"\d+" %) texts)) 4))))
  (testing "a numeric angle whose two ends meet labels them once, joined"
    (let [full {:t [0 90 180 270 400] :r [1 2 3 2 1]}
          texts (:texts (pj/svg-summary (-> full
                                            (pj/lay-point :t :r)
                                            (pj/scale :x {:domain [0 400]})
                                            (pj/coord :polar))))]
      (is (some #{"0/400"} texts))
      (is (not-any? #{"0" "400"} texts))))
  (testing "the angle labels of a shared x are drawn on the bottom panel only, as a facet's x ticks are"
    (let [texts (:texts (pj/svg-summary (-> {:u [1 2 3] :a [1 2 3] :b [3 2 1]}
                                            (pj/pose {:x :u})
                                            pj/lay-point
                                            (pj/facet {:y [:a :b]})
                                            (pj/coord :polar))))]
      ;; "2" is a radius label of each panel and an angle label once.
      (is (= 3 (count (filter #{"2"} texts)))))))
