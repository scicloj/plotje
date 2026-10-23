(ns scicloj.plotje.arrange-cells-test
  "What `pj/arrange` says of a cell list it cannot read: cells given one
   by one, an empty vector, a row beside cells, and a vector of columns."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def tiny {:u [1.0 2.0 3.0] :v [4.0 5.0 6.0] :w [7.0 9.0 8.0]})

(def a (pj/lay-point tiny :u :v))
(def b (pj/lay-point tiny :u :w))

(defn- message [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest cells-given-one-by-one-test
  (let [msg (message #(pj/arrange a b))]
    (is (re-find #"takes its cells in one vector, as in \(pj/arrange \[a b\]\)" msg))
    (is (re-find #"given a pose where the vector of cells goes" msg))
    (is (not (re-find #"hiccup" msg))))
  (testing "the form the message names draws"
    (is (= 2 (count (:sub-plots (pj/plan (pj/arrange [a b])))))))
  (testing "a pose where the options map goes"
    (is (re-find #"given a pose where the options map goes"
                 (message #(pj/arrange [a] b)))))
  (testing "a dataset alone is not a cell list"
    (is (re-find #"where the vector of cells goes"
                 (message #(pj/arrange tiny {:cols 2}))))))

(deftest an-empty-vector-names-nil-test
  (testing "as a cell"
    (let [msg (message #(pj/arrange [a [] b]))]
      (is (re-find #"input at index 1 is an empty vector" msg))
      (is (re-find #"written nil, as in \(pj/arrange \[a nil b\]\)" msg))
      (is (not (re-find #"apply pj/arrange" msg)))))
  (testing "as a row, wherever it stands"
    (doseq [cells [[[] [a b]] [[a b] []] [[a b] [] [a b]]]]
      (let [msg (message #(pj/arrange cells))]
        (is (re-find #"row \d is an empty vector" (str msg)))
        (is (re-find #"written nil" (str msg)))
        (is (not (re-find #":cols must be" (str msg)))))))
  (testing "the form the message names draws"
    (is (= 3 (count (:sub-plots (pj/plan (pj/arrange [a nil b]))))))
    (is (= 4 (count (:sub-plots (pj/plan (pj/arrange [[a b] [nil a]]))))))))

(deftest a-row-beside-cells-test
  (let [msg (message #(pj/arrange [[a] a b]))]
    (is (re-find #"given rows of cells .* and element 1 is not a row" msg))
    (is (not (re-find #"hiccup" msg))))
  (testing "an empty vector first, then cells"
    (is (re-find #"row 0 is an empty vector" (message #(pj/arrange [[] a b])))))
  (testing "a plain vector as a cell names no (apply pj/arrange ...)"
    (let [msg (message #(pj/arrange [a [b b]]))]
      (is (re-find #"is a plain vector" msg))
      (is (not (re-find #"apply" msg))))))

(deftest a-vector-of-columns-test
  (testing "one column in a vector, among pairs"
    (let [msg (message #(pj/arrange tiny [[:u :v] [:w]]))]
      (is (re-find #"input at index 1 is a vector of 1 column, \[:w\]" msg))
      (is (re-find #"the column itself, :w" msg))
      (is (not (re-find #"hiccup" msg)))))
  (testing "the form the message names draws"
    (is (= 2 (count (:sub-plots (pj/plan (pj/lay-point (pj/arrange tiny [[:u :v] :w]))))))))
  (testing "pairs with no data"
    (is (re-find #"is a pair of columns, \[:u :v\], and pj/arrange was given no data"
                 (message #(pj/arrange [[:u :v] [:u :w]])))))
  (testing "rendered hiccup is still reported as hiccup"
    (is (re-find #"looks like rendered hiccup"
                 (message #(pj/arrange [(pj/plot a) (pj/plot b)]))))))
