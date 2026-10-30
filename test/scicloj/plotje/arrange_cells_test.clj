(ns scicloj.plotje.arrange-cells-test
  "What `pj/arrange` says of a cell list it cannot read: cells given one
   by one, an empty vector, a row beside cells, columns with no data --
   and how it reads one: a vector is always a list of cells."
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
  (testing "a vector beside cells is an arrangement of its own"
    (is (= 3 (count (:sub-plots (pj/plan (pj/arrange [a [b b]]))))))))

(deftest a-vector-of-columns-test
  (testing "a vector of one column, beside a mapping, is an arrangement of that column"
    (is (= 2 (:panels (pj/svg-summary (pj/lay-point (pj/arrange tiny [{:x :u :y :v} [:w]])))))))
  (testing "columns with no data"
    (is (re-find #"given the column :u as a cell, and no data to read it from"
                 (message #(pj/arrange [[:u :v]])))))
  (testing "rendered hiccup is still reported as hiccup"
    (is (re-find #"looks like rendered hiccup"
                 (message #(pj/arrange [(pj/plot a) (pj/plot b)]))))))

(defn- cell-structure
  "The rows of an arrangement, each the mappings of its cells, with a
   nested arrangement written as its own rows."
  [p]
  (mapv (fn [row]
          (mapv #(if (:poses %) (cell-structure %) (:mapping %)) (:poses row)))
        (:poses p)))

(deftest a-vector-is-a-list-of-cells-test
  (testing "a cell is what one argument after the data is to pj/pose"
    (is (= [[{:x :u} {:x :u :y :v}]]
           (cell-structure (pj/arrange tiny [:u {:x :u :y :v}])))))
  (testing "a list of vectors is a list of rows, the same for columns and for poses"
    (is (= [[{:x :u} {:x :v}] [{:x :w} {:x :u}]]
           (cell-structure (pj/arrange tiny [[:u :v] [:w :u]]))))
    (is (= [2 2] (mapv #(count (:poses %)) (:poses (pj/arrange [[a b] [b a]]))))))
  (testing "a vector beside cells is an arrangement of its own"
    (is (= [[{:x :u} [[{:x :v} {:x :w}]]]]
           (cell-structure (pj/arrange tiny [:u [:v :w]])))))
  (testing "a pose with no data is drawn from the data given first, as a mapping is"
    (is (= (pj/plot (pj/lay-point (pj/arrange tiny [{:x :u :y :v} {:x :u :y :w}])))
           (pj/plot (pj/lay-point (pj/arrange tiny [(pj/pose nil :u :v) (pj/pose nil :u :w)]))))))
  (testing "appended to a composite, a list is read the same way"
    (is (= [[{:x :u} {:x :v}] [{:x :w} {:x :u}]]
           (cell-structure (pj/arrange (pj/arrange tiny [:u :v]) [[:w :u]]))))))

(deftest appending-is-concatenation-test
  (let [same? (fn [a b] (= (pj/plot (pj/lay-histogram a)) (pj/plot (pj/lay-histogram b))))]
    (testing "two calls draw what one call with both lists draws"
      (is (same? (pj/arrange (pj/arrange tiny [:u :v]) [:w])
                 (pj/arrange tiny [:u :v :w])))
      (is (same? (pj/arrange (pj/arrange tiny [:u :v :w :u]) [:v])
                 (pj/arrange tiny [:u :v :w :u :v])))
      (is (same? (pj/arrange (pj/arrange tiny [:u :v] {:cols 1}) [:w])
                 (pj/arrange tiny [:u :v :w] {:cols 1}))))
    (testing "a nil cell is kept, and the padding of a short last row is taken"
      (is (same? (pj/arrange (pj/arrange tiny [:u nil]) [:w])
                 (pj/arrange tiny [:u nil :w])))
      (is (same? (pj/arrange (pj/arrange tiny [:u :v :w :u :v]) [:w])
                 (pj/arrange tiny [:u :v :w :u :v :w]))))
    (testing "a pose is a cell when appended, as in one call"
      (is (= (pj/plot (pj/arrange (pj/arrange tiny [{:x :u :y :v}]) [(pj/lay-point tiny :u :w)]))
             (pj/plot (pj/arrange tiny [{:x :u :y :v} (pj/lay-point tiny :u :w)])))))
    (testing "a lay-* naming new columns adds its cell the same way"
      (is (= [[{:x :u :y :v} [{:x :u :y :w}]]]
             (mapv (fn [row] (mapv #(or (:mapping %) (mapv :mapping (:layers %))) (:poses row)))
                   (:poses (pj/lay-point (pj/arrange tiny [{:x :u :y :v}]) :u :w))))))
    (testing "an arrangement written as rows takes an appended list as a new row"
      (is (= [2 1] (mapv #(count (:poses %))
                         (:poses (pj/arrange (pj/arrange tiny [[:u :v]]) [:w]))))))))

(deftest lay-on-nil-is-a-template-test
  (testing "nil with columns or a mapping is a template, as in pj/pose"
    (is (= [{:layer-type :point :mapping {:x :u :y :v}}]
           (:layers (pj/lay-point nil :u :v))))
    (is (nil? (:data (pj/lay-point nil {:x :u :y :v}))))
    (is (= (pj/plot (pj/with-data (pj/lay-point nil :u :v) tiny))
           (pj/plot (pj/lay-point tiny :u :v)))))
  (testing "as a cell, drawn from the data given first"
    (is (= {:panels 2 :points 6}
           (select-keys (pj/svg-summary (pj/arrange tiny [(pj/lay-point nil :u :v) {:x :u :y :w}]))
                        [:panels :points]))))
  (testing "nil alone is reported, as (pj/pose nil) is"
    (is (re-find #"requires data, but got nil" (message #(pj/lay-point nil))))))

(deftest where-in-pose-test
  (testing "pj/pose takes :where beside a mapping, kept on the pose"
    (is (= {:u 1.0} (:where (pj/pose tiny :u :v {:where {:u 1.0}}))))
    (is (= 2 (:points (pj/svg-summary (pj/lay-point (pj/pose tiny :u :v {:where {:u #{1.0 2.0}}})))))))
  (testing "a second :where merges into the first, a column at a time"
    (is (= {:u 1.0 :v 4.0} (:where (-> tiny (pj/pose {:where {:u 1.0}}) (pj/pose {:where {:v 4.0}}))))))
  (testing "a :where cell is what (pj/pose nil {:where ...}) writes"
    (is (= (pj/plot (pj/lay-point (pj/arrange (pj/pose tiny :u :v) [{:where {:u 1.0}} {:where {:u 2.0}}])))
           (pj/plot (pj/lay-point (pj/arrange (pj/pose tiny :u :v) [(pj/pose nil {:where {:u 1.0}})
                                                                   (pj/pose nil {:where {:u 2.0}})]))))))
  (testing "a :where that is not a map reports an error"
    (is (re-find #"takes :where as a map" (message #(pj/pose tiny {:where :u}))))))
