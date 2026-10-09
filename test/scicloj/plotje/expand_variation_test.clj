(ns scicloj.plotje.expand-variation-test
  "The provisional expansion of a variation into listed cells, and the
   laws it is meant to hold: a generated pose draws what its expansion
   draws."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

(def scatter
  (-> (rdatasets/datasets-iris)
      (pj/pose :sepal-length :sepal-width {:color :species})
      pj/lay-point))

(defn drawn [v] (select-keys (pj/svg-summary v) [:panels :points]))

(deftest a-category-expands-into-filtered-cells-test
  (let [e (pose/expand-variation scatter :species)]
    (is (= [{:species "setosa"} {:species "versicolor"} {:species "virginica"}]
           (mapv :where (:poses e))))
    (is (= (drawn (pj/facet scatter :species)) (drawn e)))
    (testing "the listed cells share both axes, titled once, as the facet does"
      (let [texts (:texts (pj/svg-summary e))]
        (is (= 1 (count (filter #{"sepal length"} texts))))
        (is (= 1 (count (filter #{"sepal width"} texts))))))))

(deftest columns-expand-into-cells-writing-a-column-test
  (let [e (pose/expand-variation scatter {:x [:sepal-length :petal-length]})]
    (is (= [{:x :sepal-length} {:x :petal-length}] (mapv :mapping (:poses e))))
    (is (= {:panels 2 :points 300} (drawn e)))
    (testing "the y axis every cell reads is titled once; each x keeps its title"
      (let [texts (:texts (pj/svg-summary e))]
        (is (= 1 (count (filter #{"sepal width"} texts))))
        (is (= 1 (count (filter #{"petal length"} texts))))))))

(deftest a-grid-expands-like-facet-grid-test
  (let [mpg (rdatasets/ggplot2-mpg)
        base (pj/lay-point mpg :displ :hwy)
        e (pose/expand-variation base {:col :drv :row :cyl})
        g (pj/facet-grid base :drv :cyl)]
    (is (= (:points (pj/svg-summary g)) (:points (pj/svg-summary e))))
    (testing "a combination with no rows is an empty cell"
      (is (= 9 (:panels (pj/svg-summary e)))))))

(deftest the-expansion-writes-the-sharing-arrange-does-not-test
  (is (= #{:x :y} (:share-scales (pose/expand-variation scatter :species))))
  (is (nil? (get-in (pj/arrange [scatter scatter]) [:opts :share-scales]))))
