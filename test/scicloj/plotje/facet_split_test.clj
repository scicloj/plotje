(ns scicloj.plotje.facet-split-test
  "A facet splits its data in one pass over the key column, and the
   panels come out in the order the data first holds each key."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]))

(def rows
  {:v [1 2 3 4 5 6 7 8 9 10]
   :g ["b" nil "a" "b" nil "c" "a" "b" "c" "a"]
   :h [2 1 2 1 2 1 nil 2 1 2]})

(defn- panels
  "Per draft layer: the facet labels and the :v values of its rows."
  [pose]
  (mapv (fn [l] [(:facet-col l) (:facet-row l) (vec ((:data l) :v))])
        (:layers (pj/draft pose))))

(deftest panels-keep-their-order-and-their-rows
  (testing "one column: first appearance, a missing key as a panel of its own"
    (is (= [["b" nil [1 4 8]] ["" nil [2 5]] ["a" nil [3 7 10]] ["c" nil [6 9]]]
           (panels (-> rows (pj/lay-point :v :v) (pj/facet :g))))))
  (testing "two columns crossed: a combination the data lacks keeps a panel with no rows"
    (is (= [["b" "2" [1 8]] ["b" "1" [4]] ["b" "" []]
            ["" "2" [5]] ["" "1" [2]] ["" "" []]
            ["a" "2" [3 10]] ["a" "1" []] ["a" "" [7]]
            ["c" "2" []] ["c" "1" [6 9]] ["c" "" []]]
           (panels (-> rows (pj/lay-point :v :v) (pj/facet-grid :g :h))))))
  (testing "a compound key: one panel per combination the data holds"
    (is (= [["b / 2" nil [1 8]] [" / 1" nil [2]] ["a / 2" nil [3 10]] ["b / 1" nil [4]]
            [" / 2" nil [5]] ["c / 1" nil [6 9]] ["a / " nil [7]]]
           (panels (-> rows (pj/lay-point :v :v) (pj/facet [:g :h])))))))

(deftest the-key-column-is-read-once
  (let [scans (atom 0)
        rows-where (deref #'pose/rows-where)
        n 2000
        data (tc/dataset {:v (range n) :g (map #(str "g" (mod % 40)) (range n))})]
    (with-redefs [pose/rows-where (fn [ks k] (swap! scans inc) (rows-where ks k))]
      (is (= 40 (count (:layers (pj/draft (-> data (pj/lay-point :v :v) (pj/facet :g))
                                          {:width 4000})))))
      (is (zero? (deref scans))
          "no pass over the key column per panel"))))
