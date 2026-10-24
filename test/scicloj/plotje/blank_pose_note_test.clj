(ns scicloj.plotje.blank-pose-note-test
  "A pose with data, no layer and no column named, on data of four or
   more columns, draws nothing: the plan says so. It drew a blank figure
   with no message."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def wide {:a [1 2 3] :b [2 3 4] :c [3 4 5] :e [4 5 6]})

(deftest a-pose-that-draws-nothing-says-so-test
  (testing "four columns, no column named, no layer"
    (let [out (with-out-str (pj/plot (pj/pose wide)))]
      (is (re-find #"Note: this pose draws nothing" out))
      (is (re-find #"its data has 4 columns, so none is read by default" out))
      (is (re-find #"Available columns: \[:a :b :c :e\]" out))
      (is (= 1 (count (re-seq #"Note:" out))))))
  (testing "the plan is still made, with no panel"
    (is (= 0 (count (:panels (binding [*out* (java.io.StringWriter.)]
                               (pj/plan (pj/pose wide))))))))
  (testing "no Note where the pose draws"
    (is (= "" (with-out-str (pj/plot (pj/pose (dissoc wide :e))))))
    (is (= "" (with-out-str (pj/plot (pj/pose wide :a :b)))))
    (is (= "" (with-out-str (pj/plot (pj/lay-point wide :a :b))))))
  (testing "no Note for the template with no data"
    (is (= "" (with-out-str (pj/plan (pj/pose))))))
  (testing "in an arrangement, once, and only where no cell draws"
    (let [cells [{:where {:a 1}} {:where {:a 2}}]]
      (is (= 1 (count (re-seq #"Note: this pose draws nothing"
                              (with-out-str (pj/plan (pj/arrange wide cells)))))))
      ;; The layer is drawn on a cell of its own, and the Note `lay-*`
      ;; prints already says that the other cells draw nothing.
      (let [out (with-out-str (pj/plan (pj/lay-point (pj/arrange wide cells) :a :b)))]
        (is (re-find #"2 cells, which name no column and hold no layer, draw nothing" out))
        (is (not (re-find #"this pose draws nothing" out)))))))
