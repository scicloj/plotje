(ns scicloj.plotje.facet-message-forms-test
  "The form a pj/facet message suggests draws when it is written."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def rows
  {:size [1 2 3 4 5 6 7 8]
   :height [2 4 6 8 1 3 5 7]
   :weight [5 6 7 8 1 2 3 4]
   :kind ["a" "a" "b" "b" "a" "a" "b" "b"]
   :year [1 2 1 2 1 2 1 2]})

(defn- message [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest a-second-facet-in-one-direction-suggests-a-form-that-draws
  (let [base (pj/lay-point rows :size :height)
        m (message #(-> base (pj/facet :kind) (pj/facet :year)))]
    (is (re-find #"leave out the earlier facet and write one compound key in its place -- \(pj/facet pose \[:kind :year\]\)" m))
    (testing "the suggested form, written in place of the two"
      (is (= 4 (:panels (pj/svg-summary (pj/facet base [:kind :year]))))))
    (testing "a compound key after an earlier facet names each column once"
      (is (re-find #"\(pj/facet pose \[:kind :year\]\)"
                   (message #(-> base (pj/facet :kind) (pj/facet [:kind :year]))))))))

(deftest a-facet-map-with-a-direction-names-the-map-that-draws
  (let [base (-> rows (pj/pose {:x :size}) pj/lay-point)
        m (message #(pj/facet base {:y [:height :weight]} :col))]
    (is (re-find #"\(pj/facet pose \{:col \{:y \[:height :weight\]\}\}\)" m))
    (is (not (re-find #"takes a column of the data" m)))
    (testing "the suggested form"
      (is (= 2 (:panels (pj/svg-summary (pj/facet base {:col {:y [:height :weight]}}))))))
    (testing "a map that already names a direction"
      (is (re-find #"already names its directions: leave :row out"
                   (message #(pj/facet base {:col :kind} :row)))))
    (testing "{:column ...} with a direction is still a column, and warns"
      (let [printed (with-out-str
                      (is (= 2 (:panels (pj/svg-summary (pj/facet (pj/lay-point rows :size :height)
                                                                  {:column :kind} :row))))))]
        (is (re-find #"direction argument is retired" printed))))))
