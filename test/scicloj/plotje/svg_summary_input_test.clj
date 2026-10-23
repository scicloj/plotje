(ns scicloj.plotje.svg-summary-input-test
  "pj/svg-summary reads SVG hiccup as it stands and renders anything
   else as pj/plot does, so raw data is summarized as the plot pj/plot
   draws from it and a value pj/plot cannot draw is reported."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]))

(def rows {:a [1 2 3] :b [4 5 6]})

(deftest what-svg-summary-accepts
  (testing "SVG hiccup"
    (is (= 3 (:points (pj/svg-summary (pj/plot (pj/lay-point rows :a :b)))))))
  (testing "a pose"
    (is (= 3 (:points (pj/svg-summary (pj/lay-point rows :a :b))))))
  (testing "raw data, as pj/plot draws it"
    (is (= (pj/svg-summary (pj/plot rows)) (pj/svg-summary rows)))
    (is (= 1 (:panels (pj/svg-summary rows))))
    (is (= 3 (:points (pj/svg-summary rows)))))
  (testing "a dataset"
    (is (= 3 (:points (pj/svg-summary (tc/dataset rows))))))
  (testing "a value pj/plot cannot draw is reported"
    (is (thrown? clojure.lang.ExceptionInfo (pj/svg-summary nil)))
    (is (thrown? clojure.lang.ExceptionInfo (pj/svg-summary "text")))))
