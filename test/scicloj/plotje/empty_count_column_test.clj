(ns scicloj.plotje.empty-count-column-test
  "A counting bar on a column holding nothing says so, as the other
   layers that group by category do, and does not call the column
   numerical."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(defn- message [pose]
  (try (binding [*out* (java.io.StringWriter.)] (pj/plan pose))
       nil
       (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest a-counting-bar-on-a-column-holding-nothing
  (testing "an empty column"
    (let [m (message (pj/lay-bar {:g []} :g))]
      (is (re-find #":g has no rows" m))
      (is (re-find #"at least one row carrying a category" m))
      (is (not (re-find #"numerical" m)))))
  (testing "a column of nothing but nil"
    (let [m (message (pj/lay-bar {:g [nil nil]} :g))]
      (is (re-find #":g has no values" m))
      (is (not (re-find #"numerical" m)))))
  (testing "a numerical column keeps the advice about its type"
    (is (re-find #":g is numerical. Use pj/lay-histogram"
                 (message (pj/lay-bar {:g [1 2 3]} :g))))))
