(ns scicloj.plotje.continuous-aesthetic-column-name-test
  "A column named by a number is a column for `:size` and `:alpha` as it
   is for every other reading, so mapping one that holds categories
   reports the same error as a keyword-named one."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def data {:a [1 2 3] :b [4 5 6] 2 ["p" "q" "p"] 3 [10 20 30]})

(deftest a-number-named-category-column-on-size-reports
  (testing "bare and {:column} spellings"
    (doseq [m [{:size 2} {:size {:column 2}} {:alpha {:column 2}}]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"needs a numeric column"
                            (pj/plot (pj/lay-point data :a :b m)))
          (pr-str m)))))

(deftest a-number-named-numeric-column-on-size-draws
  (is (= 3 (count (:sizes (pj/svg-summary (pj/lay-point data :a :b {:size 3})))))))

(deftest a-written-value-is-not-checked-as-a-column
  (is (= 3 (:points (pj/svg-summary (pj/lay-point data :a :b {:size {:value 2}}))))))
