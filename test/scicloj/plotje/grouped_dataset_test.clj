(ns scicloj.plotje.grouped-dataset-test
  "A grouped dataset given as data is reported at the call, naming the
   grouping and the way out."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]))

(def rows (tc/dataset {:a [1 2 3 4] :b [4 3 2 1] :kind ["p" "q" "p" "q"]}))
(def grouped (tc/group-by rows :kind))

(deftest a-grouped-dataset-is-reported
  (testing "given to a layer"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"grouped dataset, the result of tc/group-by.*\(tc/ungroup data\)"
                          (pj/lay-point grouped :a :b))))
  (testing "given to pj/pose"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"grouped dataset"
                          (pj/pose grouped {:x :a :y :b}))))
  (testing "given as a layer's own :data"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"grouped dataset"
                          (pj/lay-point rows :a :b {:data grouped}))))
  (testing "ungrouped, it draws every row"
    (is (= 4 (:points (pj/svg-summary (pj/lay-point (tc/ungroup grouped) :a :b)))))))
