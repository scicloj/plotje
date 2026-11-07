(ns scicloj.plotje.line-join-order-test
  "A line joins its points in order of data x, under `(pj/coord :flip)`
   too, as `pj/lay-step` and `pj/lay-area` do. The points are sorted
   after projection, so the sort reads data x where the flip draws it:
   vertically. Sorting by the drawn horizontal joined a flipped line in
   order of its data y."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

;; Row order, `:a` order and `:b` order all differ.
(def d {:a [2 4 1 3] :b [1 3 4 2]})

(defn- data-polyline
  "The drawn points of the last polyline (the data line; the grid
   lines come first), as [horizontal vertical] pairs."
  [pose]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= :polyline (first %))))
       last
       second
       :points
       (#(str/split % #" "))
       (mapv (fn [pt] (mapv parse-double (str/split pt #","))))))

(defn- order-along [axis pts]
  (let [vs (mapv axis pts)]
    (or (apply < vs) (apply > vs))))

(deftest a-line-is-joined-in-order-of-data-x-test
  (testing "unflipped: data x is horizontal, and the path runs along it"
    (is (order-along first (data-polyline (pj/lay-line d :a :b)))))
  (testing "flipped: data x is vertical, and the path runs along it"
    (let [pts (data-polyline (-> d
                                 (pj/lay-line :a :b)
                                 (pj/coord :flip)))]
      (is (order-along second pts))
      (is (not (order-along first pts)))))
  (testing "a categorical x keeps its band order, flipped or not"
    (let [cat {:k ["c" "b" "a"] :v [1 3 2]}
          plain (data-polyline (pj/lay-line cat :k :v))
          flipped (data-polyline (-> cat
                                     (pj/lay-line :k :v)
                                     (pj/coord :flip)))]
      (is (order-along first plain))
      (is (order-along second flipped)))))
