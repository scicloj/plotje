(ns scicloj.plotje.unread-pose-columns-test
  "A column a pose names on `:x` or `:y` that no layer of the pose
   reads: the layers share one panel, each drawing the columns it names,
   so the pose's column is not drawn, and the draft says so."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]))

(def flowers
  {:sepal-length [5.1 4.9 4.7 6.4 6.9 5.5]
   :sepal-width  [3.5 3.0 3.2 3.2 3.1 2.3]
   :petal-length [1.4 1.4 1.3 4.5 4.9 4.0]
   :petal-width  [0.2 0.2 0.2 1.5 1.5 1.3]})

(defn- draft-columns [pose]
  (mapv (juxt :mark :x :y)
        (filter #(and (map? %) (contains? % :__panel-idx))
                (tree-seq coll? seq (binding [*out* (java.io.StringWriter.)]
                                      (pj/draft pose))))))

(deftest a-pose-column-no-layer-reads-is-reported-test
  (testing "every layer names both columns: the pose's are not drawn, and both are named"
    (let [p (-> flowers
                (pj/pose :sepal-length :sepal-width)
                (pj/lay-point :petal-length :petal-width))
          out (with-out-str (pj/plan p))]
      (is (= [[:point :petal-length :petal-width]] (draft-columns p)))
      (is (= [[[] :x] [[] :y]] (pose/unread-positions p)))
      (is (re-find #"Warning: a pose maps :x to :sepal-length, and every layer drawn on it names its own :x, so :sepal-length is not drawn" out))
      (is (re-find #"Warning: a pose maps :y to :sepal-width" out))))
  (testing "one axis read, one not"
    (let [p (-> flowers
                (pj/pose :sepal-length :sepal-width)
                (pj/lay-point {:y :petal-width}))]
      (is (= [[[] :y]] (pose/unread-positions p)))
      (is (= [[:point :sepal-length :petal-width]] (draft-columns p)))))
  (testing "the form the message suggests draws both, with no warning"
    (let [p (-> flowers
                (pj/pose :sepal-length :sepal-width)
                (pj/lay-point :petal-length :petal-width)
                pj/lay-point)
          out (with-out-str (pj/plan p))]
      (is (= [] (pose/unread-positions p)))
      (is (not (re-find #"is not drawn" out)))
      (is (= [[:point :petal-length :petal-width] [:point :sepal-length :sepal-width]]
             (draft-columns p)))))
  (testing "no warning for a pose with no layer, or whose layers name no column"
    (is (= [] (pose/unread-positions (pj/pose flowers :sepal-length :sepal-width))))
    (is (= [] (pose/unread-positions (-> flowers
                                         (pj/pose :sepal-length :sepal-width)
                                         pj/lay-point
                                         pj/lay-smooth))))
    (is (= "" (with-out-str (pj/plan (-> flowers
                                         (pj/pose :sepal-length :sepal-width)
                                         pj/lay-point))))))
  (testing "a cell of an arrangement is asked the same"
    (let [cell (-> flowers
                   (pj/pose :sepal-length :sepal-width)
                   (pj/lay-point :petal-length :petal-width))
          c (pj/arrange [cell (pj/lay-point flowers :sepal-length :sepal-width)])]
      (is (= [:x :y] (mapv second (pose/unread-positions c))))
      (is (re-find #"a pose maps :x to :sepal-length" (with-out-str (pj/plan c)))))))
