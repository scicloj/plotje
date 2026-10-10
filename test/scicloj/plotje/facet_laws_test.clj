(ns scicloj.plotje.facet-laws-test
  "Laws of the facet and of `pj/pose`, checked over several datasets:
   a facet draws what its hand-written cells draw; expanding a facet and
   collapsing it again gives the pose back; a verb written before or
   after `pj/facet` gives an equal pose; `pj/pose` is idempotent."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.api :as tc]))

(def cases
  "A pose to facet, and the category column to facet it by."
  [["iris by species" (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width) :species]
   ["mpg by drv" (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy) :drv]
   ["mpg by cyl, a number" (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy) :cyl]
   ["mtcars by gear, histogram" (pj/lay-histogram (rdatasets/datasets-mtcars) :mpg) :gear]
   ["small, coloured" (pj/lay-point {:x [1 2 3 4 5 6] :y [2 1 4 3 6 5]
                                     :g ["a" "b" "a" "c" "b" "c"]}
                                    :x :y {:color :g})
    :g]])

(defn- cells-of
  "The facet written out by hand: one cell per value, in data order."
  [base col]
  (let [d (:data base)
        ds (if (tc/dataset? d) d (tc/dataset d))
        values (distinct (ds col))]
    (assoc base
           :layout {:direction :horizontal}
           :poses (mapv (fn [v] {:where {col v}}) values))))

(deftest a-facet-draws-what-its-cells-draw-test
  (doseq [[label base col] cases]
    (testing label
      (is (= (pj/plot (pj/facet base col))
             (pj/plot (cells-of base col)))))))

(deftest expanding-and-collapsing-gives-the-pose-back-test
  (doseq [[label base col] cases
          :let [faceted (pj/facet base col)
                expanded (pose/expand-facets faceted)]]
    (testing label
      (is (pose/composite? expanded) "the facet expands to cells")
      (is (= (:mapping faceted) (:mapping (pose/collapse-regular-split expanded))))
      (is (= (pj/plot faceted) (pj/plot expanded))))))

(deftest a-verb-before-or-after-the-facet-test
  (doseq [[label base col] cases
          :when (not= :histogram (:layer-type (first (:layers base))))]
    (testing label
      (is (= (-> base (pj/facet col) (pj/options {:title "t"}))
             (-> base (pj/options {:title "t"}) (pj/facet col))))
      (is (= (pj/plot (-> base (pj/facet col) pj/lay-smooth))
             (pj/plot (-> base pj/lay-smooth (pj/facet col)))))))
  (testing "a facet over columns"
    (let [mpg (rdatasets/ggplot2-mpg)]
      (is (= (-> mpg (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]}))
             (-> mpg (pj/pose {:x :displ}) (pj/facet {:y [:hwy :cty]}) pj/lay-point))))))

(deftest pose-is-idempotent-test
  (doseq [[label base _] cases]
    (testing label
      (is (= base (pj/pose base)))
      (is (= base (pj/pose base {})))
      (let [m {:alpha 0.5}]
        (is (= (pj/pose base m) (pj/pose (pj/pose base m) m)))))))

(deftest a-layer-of-one-facet-column-stays-in-its-panel-test
  (let [mpg (rdatasets/ggplot2-mpg)
        y-domains (fn [p] (mapv (comp :y-domain first :panels pj/plan)
                                (:poses (pose/expand-facets p))))]
    (testing "a layer naming :hwy is not drawn in the :cty panel"
      (let [p (-> mpg (pj/pose {:x :displ}) (pj/lay-point {:y :hwy}) (pj/facet {:y [:hwy :cty]}))
            summary (pj/svg-summary p)]
        (is (= 2 (:panels summary)))
        (is (some #{"cty"} (:texts summary)))
        (is (= (y-domains p)
               (y-domains (-> mpg (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]})))))))))

(deftest a-layer-goes-to-the-cells-whose-columns-it-draws-test
  (let [iris (rdatasets/datasets-iris)
        cols [:sepal-length :sepal-width]
        matrix (-> iris (pj/pose {:color :species}) (pj/facet {:col {:x cols} :row {:y cols}}))
        diagonal-marks (fn [p] (->> (:sub-plots (pj/plan p))
                                    (keep (fn [{:keys [path plan]}]
                                            (when (apply = path)
                                              (-> plan :panels first :layers first :mark))))
                                    vec))]
    (testing "a point layer on a matrix leaves the diagonal to inference"
      (is (= [:bar :bar] (diagonal-marks (pj/lay-point matrix))))
      (is (= (pj/svg-summary matrix) (pj/svg-summary (pj/lay-point matrix)))))
    (testing "a density goes to the diagonal, the points elsewhere"
      (is (= [:area :area] (diagonal-marks (-> matrix pj/lay-point pj/lay-density)))))
    (testing "a layer that fits no cell is drawn in every cell"
      (let [s (pj/svg-summary (-> iris (pj/pose {:x :sepal-length :y :sepal-width})
                                  pj/lay-histogram (pj/facet :species)))]
        (is (= 3 (:panels s)))
        (is (zero? (:points s)))
        (is (pos? (:polygons s)))))))
