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

(defn- heights [pose]
  (mapv :height (panel-rects pose)))

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

(deftest marginal-on-the-right-test
  (let [scatter (pj/lay-point iris :sepal-length :sepal-width)]
    (testing "a right marginal is a second panel beside the first"
      (let [pose (pj/marginal scatter :right)]
        (is (= 2 (:panels (pj/svg-summary pose))))
        (is (= 150 (:points (pj/svg-summary pose))))
        (is (= 1 (:polygons (pj/svg-summary pose))))
        ;; Equal heights are what a row has to agree on, the way a
        ;; column agrees on widths: they are what puts a value at the
        ;; same place in both panels.
        (is (apply = (heights pose)))
        ;; And the widths differ, since one cell is the thin one.
        (is (not (apply = (widths pose))))))
    (testing "the marginal describes the pose's :y column"
      ;; Sharing pins both cells to that column's own extent, so the
      ;; scatter's y range and the marginal's are the same interval --
      ;; the column's 2.0 to 4.4, padded as any data extent is.
      (let [plan (pj/plan (pj/marginal scatter :right))
            y-doms (mapv #(-> % :plan :panels first :y-domain) (:sub-plots plan))
            alone (-> (pj/plan scatter) :panels first :y-domain)]
        (is (apply = y-doms))
        (is (= alone (first y-doms))
            "and the scatter's own y range is untouched by the marginal")))
    (testing ":size sets the marginal's share of the width"
      (let [[_ marg] (panel-rects (pj/marginal scatter :right))
            [_ marg-big] (panel-rects (pj/marginal scatter :right :density {:size 0.4}))]
        (is (< (:width marg) (:width marg-big)))))
    (testing "a histogram on the right draws its bars across the panel"
      (let [pose (pj/marginal scatter :right :histogram)
            summary (pj/svg-summary pose)]
        (is (= 2 (:panels summary)))
        (is (apply = (heights pose)))
        (is (< 1 (:polygons summary)))))
    (testing "a legend on the main panel does not push the two out of step"
      (is (apply = (heights (pj/marginal (pj/lay-point iris :sepal-length :sepal-width
                                                       {:color :species})
                                         :right)))))))

(deftest flipped-histogram-bars-span-the-panel-test
  ;; A bin edge belongs on the axis the bins run along and a count on
  ;; the other, and `:coord :flip` swaps which is which. Scaling a bin
  ;; edge with the count scale drew every bar of a flipped histogram
  ;; inside the first few drawing units of the panel.
  (testing "a flipped histogram fills the panel as an upright one does"
    (let [upright (pj/lay-histogram iris :sepal-width)
          flipped (-> iris (pj/lay-histogram :sepal-width) (pj/coord :flip))
          span (fn [pose axis]
                 (let [vals (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
                                 (filter #(and (vector? %) (= :polygon (first %))))
                                 (mapcat #(re-seq #"[-0-9.]+" (str (:points (second %)))))
                                 (map #(Double/parseDouble %))
                                 (drop (if (= :y axis) 1 0))
                                 (take-nth 2))]
                   (- (apply max vals) (apply min vals))))]
      ;; Upright: the bins run across, the counts up. Flipped: the
      ;; other way round, so the two spans swap.
      (is (< 300.0 (span upright :x)))
      (is (< 300.0 (span flipped :x)))
      (is (< 200.0 (span upright :y)))
      (is (< 200.0 (span flipped :y))))))

(deftest marginal-refusals-test
  (let [scatter (pj/lay-point iris :sepal-length :sepal-width)]
    (testing "a side that would put the distribution behind an axis is refused"
      (is (thrown-with-msg? Exception #":top or :right"
                            (pj/marginal scatter :bottom))))
    (testing "only a distribution is drawn"
      (is (thrown-with-msg? Exception #":density or :histogram"
                            (pj/marginal scatter :top :point))))
    (testing "a composite has no single panel to sit above"
      (is (thrown-with-msg? Exception #"leaf pose"
                            (pj/marginal (pj/arrange [scatter scatter]) :top))))
    (testing "a pose naming no :x column has nothing to describe"
      (is (thrown-with-msg? Exception #":x column"
                            (pj/marginal (pj/pose iris) :top))))
    (testing "and a right marginal names the :y column instead"
      (is (thrown-with-msg? Exception #":y column"
                            (pj/marginal (pj/pose iris) :right))))))
