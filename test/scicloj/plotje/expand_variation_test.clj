(ns scicloj.plotje.expand-variation-test
  "The provisional expansion of a variation into listed cells, and the
   laws it is meant to hold: a generated pose draws what its expansion
   draws."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
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
           (mapv :where (-> e :poses first :poses))))
    (testing "the strips are read from the cells' filters, not written"
      (is (nil? (:grid-strip-labels e)))
      (is (every? (set (:texts (pj/svg-summary e))) ["setosa" "versicolor" "virginica"])))
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
    (testing "a regular grid draws as the facet grid, a no-data panel for each empty combination"
      (is (= (:panels (pj/svg-summary g)) (:panels (pj/svg-summary e)))))))

(deftest a-regular-split-draws-as-its-facet-test
  (testing "expanded and collapsed, every facet shape draws the same picture"
    (let [mpg (rdatasets/ggplot2-mpg)
          base (pj/lay-point mpg :displ :hwy)]
      (doseq [v [(pj/facet base :drv)
                 (pj/facet base :drv :row)
                 (pj/facet base [:year :drv])
                 (pj/facet-grid base :drv :cyl)
                 (-> base (pj/facet :drv) (pj/options {:scales :free}))]]
        (is (= (pj/plot v)
               (binding [pose/*expand-facets* true] (pj/plot v)))))))
  (testing "cells written by hand draw the facet's picture"
    (is (= (pj/plot (assoc scatter
                           :layout {:direction :horizontal}
                           :poses [{:where {:species "setosa"}}
                                   {:where {:species "versicolor"}}
                                   {:where {:species "virginica"}}]))
           (pj/plot (pj/facet scatter :species)))))
  (testing "a subset of the categories is not the facet, and stays cells"
    (is (pose/composite?
         (pose/collapse-regular-split
          (assoc scatter :poses [{:where {:species "setosa"}}
                                 {:where {:species "virginica"}}]))))))

(deftest the-cells-of-one-split-share-separate-plots-do-not-test
  (testing "the expansion writes no sharing; its cells, parts of one split, share"
    (let [e (pose/expand-variation scatter :species)
          ;; A regular split is drawn as one plan, so its panels are
          ;; the plan's own.
          x-domains (map :x-domain (:panels (pj/plan e)))]
      (is (not (contains? e :share-scales)))
      (is (= 3 (count x-domains)))
      (is (apply = x-domains))))
  (testing "plots handed to pj/arrange with data of their own do not share"
    (let [a (-> (tc/select-rows (rdatasets/datasets-iris) (range 50))
                (pj/lay-point :sepal-length :sepal-width))
          b (-> (tc/select-rows (rdatasets/datasets-iris) (range 100 150))
                (pj/lay-point :sepal-length :sepal-width))
          x-domains (->> (pj/plan (pj/arrange [a b])) :sub-plots
                         (map #(-> % :plan :panels first :x-domain)))]
      (is (nil? (get-in (pj/arrange [a b]) [:opts :share-scales])))
      (is (apply not= x-domains)))))

;; ---- pj/facet over columns (the tutorial's API, provisional) ----

(defn- marks-per-cell [v]
  (mapv (fn [sub] (set (mapcat #(map :mark (:layers %)) (:panels (:plan sub)))))
        (:sub-plots (pj/plan v))))

(deftest a-facet-over-columns-is-kept-and-expanded-test
  (let [cars (rdatasets/ggplot2-mpg)
        v (-> cars (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]}))]
    (is (= {:y [:hwy :cty]} (:facet v)))
    (is (= {:x :displ} (:mapping v)))
    (is (= {:panels 2 :points 468} (select-keys (pj/svg-summary v) [:panels :points]))))
  (testing "categories alone go in the mapping, as pj/facet-grid writes them"
    (let [v (-> (rdatasets/ggplot2-mpg) (pj/pose {:x :displ :y :hwy}) pj/lay-point
                (pj/facet {:col :year :row :drv}))]
      (is (nil? (:facet v)))
      (is (= [:year :drv] ((juxt :col :row) (:mapping v))))))
  (testing "a misspelled column is reported at the call"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Column :drive \(from :col\) not found"
                          (-> (rdatasets/ggplot2-mpg) (pj/pose {:x :displ :y :hwy}) (pj/facet :drive))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Column :hwyy \(from :facet\) not found"
                          (-> (rdatasets/ggplot2-mpg) (pj/pose {:x :displ}) (pj/facet {:y [:hwy :hwyy]}))))))

(deftest layers-are-fitted-to-cells-test
  (let [cars (rdatasets/ggplot2-mpg)
        flowers (rdatasets/datasets-iris)]
    (testing "a layer naming one of the facet's columns draws only in that column's cell"
      (is (= [#{:point} #{:line}]
             (marks-per-cell (-> cars (pj/pose {:x :displ}) (pj/lay-point {:y :hwy})
                                 (pj/lay-smooth {:y :cty}) (pj/facet {:y [:hwy :cty]}))))))
    (testing "a matrix: one-column layers on the diagonal, two-column layers off it"
      (is (= [#{:area} #{:point} #{:point} #{:area}]
             (marks-per-cell (-> flowers (pj/pose {:color :species})
                                 (pj/facet {:col {:x [:sepal-length :sepal-width]}
                                            :row {:y [:sepal-length :sepal-width]}})
                                 pj/lay-point pj/lay-density)))))
    (testing "margins: nil leaves a slot with no column; the corner draws nothing"
      (is (= [#{:bar} #{} #{:point} #{:bar}]
             (marks-per-cell (-> cars pj/pose pj/lay-point pj/lay-histogram
                                 (pj/facet {:col {:x [:displ nil]} :row {:y [nil :hwy]}}))))))
    (testing "a cell no layer fits keeps them all: a matrix of points draws its diagonal"
      (is (= 9 (:panels (pj/svg-summary (-> (tc/dataset {:a [1 2 3] :b [3 1 2] :c [2 3 1]})
                                             (pj/cross-matrix [:a :b :c])
                                             pj/lay-point))))))
    (testing "categories crossed with columns"
      (is (= {:panels 6 :points 300}
             (select-keys (pj/svg-summary (-> flowers (pj/pose {:x :sepal-length}) pj/lay-point
                                              (pj/facet {:col :species :row {:y [:sepal-width :petal-length]}})))
                          [:panels :points]))))))

(deftest a-column-on-y-alone-draws-on-its-side-test
  (let [p (pj/plan (-> (rdatasets/ggplot2-mpg) (pj/pose {:y :hwy}) pj/lay-histogram))]
    (is (= 1 (count (:panels p))))
    (is (= :flip (-> p :panels first :coord)))))
