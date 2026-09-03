(ns scicloj.plotje.marginal-test
  "`pj/marginal` and the `:align-panels` pass it rests on."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [scicloj.plotje.api :as pj]))

(def iris (rdatasets/datasets-iris))

(def ^:private panel-background "rgb(232,232,232)")

(defn- panel-rects
  "Every panel background rect, as {:width :height}."
  [pose]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %)
                     (= :rect (first %))
                     (= panel-background (:fill (second %)))))
       (mapv (comp #(select-keys % [:width :height]) second))))

(defn- widths [pose]
  (mapv :width (panel-rects pose)))

(deftest marginal-adds-an-aligned-panel-test
  (testing "a marginal is a second panel whose drawing area matches the first"
    (let [pose (pj/marginal (pj/lay-point iris :sepal-length :sepal-width) :top)]
      (is (= 2 (:panels (pj/svg-summary pose))))
      ;; The scatter keeps its 150 points; the density adds one polygon.
      (is (= 150 (:points (pj/svg-summary pose))))
      (is (= 1 (:polygons (pj/svg-summary pose))))
      ;; Equal widths are what makes a value sit at the same place in
      ;; both panels -- the whole point of a marginal.
      (is (apply = (widths pose)))))
  (testing "the marginal is the thinner panel, and :size sets its share"
    (let [[marg main] (panel-rects (pj/marginal (pj/lay-point iris :sepal-length :sepal-width)
                                                :top))
          [marg-big _] (panel-rects (pj/marginal (pj/lay-point iris :sepal-length :sepal-width)
                                                 :top :density {:size 0.4}))]
      (is (< (:height marg) (:height main)))
      (is (< (:height marg) (:height marg-big)))))
  (testing "a histogram is the other distribution it draws"
    (let [pose (pj/marginal (pj/lay-point iris :sepal-length :sepal-width) :top :histogram)]
      (is (= 2 (:panels (pj/svg-summary pose))))
      (is (apply = (widths pose)))
      ;; Bars, not one filled curve.
      (is (< 1 (:polygons (pj/svg-summary pose)))))))

(deftest marginal-aligns-past-a-legend-test
  (testing "a legend on the main panel does not push the two out of step"
    ;; A legend takes width from the cell that draws it. Without the
    ;; matching reservation on the marginal, its x scale ran 100 drawing
    ;; units wider and the two panels described different values.
    (is (apply = (widths (pj/marginal (pj/lay-point iris :sepal-length :sepal-width
                                                    {:color :species})
                                      :top))))
    (is (apply = (widths (pj/marginal (pj/lay-tile iris :sepal-length :sepal-width)
                                      :top))))))

(deftest align-panels-is-usable-on-its-own-test
  (testing ":align-panels equalises drawing areas in any composite"
    (let [cells [(pj/lay-density iris :sepal-length)
                 (pj/lay-point iris :sepal-length :sepal-width)]
          plain {:poses cells
                 :layout {:direction :vertical :weights [0.25 0.75]}
                 :share-scales #{:x}}
          aligned (assoc plain :opts {:align-panels true})]
      ;; Only the cell with a y-axis title pays for the label offset, so
      ;; without the pass the two differ.
      (is (not (apply = (widths plain))))
      (is (apply = (widths aligned))))))

(deftest marginal-refusals-test
  (let [scatter (pj/lay-point iris :sepal-length :sepal-width)]
    (testing "only :top is available, and the message says what is missing"
      (is (thrown-with-msg? Exception #"marginal on :top"
                            (pj/marginal scatter :right))))
    (testing "only a distribution is drawn"
      (is (thrown-with-msg? Exception #":density or :histogram"
                            (pj/marginal scatter :top :point))))
    (testing "a composite has no single panel to sit above"
      (is (thrown-with-msg? Exception #"leaf pose"
                            (pj/marginal (pj/arrange [scatter scatter]) :top))))
    (testing "a pose naming no :x column has nothing to describe"
      (is (thrown-with-msg? Exception #":x column"
                            (pj/marginal (pj/pose iris) :top))))))
