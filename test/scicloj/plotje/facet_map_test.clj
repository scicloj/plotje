(ns scicloj.plotje.facet-map-test
  "`pj/facet` given a map of columns: the forms it reports, and the
   pose's own column on an axis the facet varies."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

(deftest a-map-of-columns-it-cannot-draw-is-reported-test
  (let [iris (rdatasets/datasets-iris)]
    (testing "a vector on one axis beside one column on the other"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"a vector of columns on :x and one column on :y"
                            (pj/facet (pj/pose iris) {:x [:petal-length :sepal-length] :y :sepal-width}))))
    (testing "on a composite"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"on a composite pose"
                            (-> iris
                                (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
                                (pj/facet {:y [:petal-length :petal-width]})))))
    (testing "a second map"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"already facets over"
                            (-> iris
                                (pj/lay-point :sepal-length :sepal-width)
                                (pj/facet {:y [:sepal-width :petal-width]})
                                (pj/facet {:x [:sepal-length :petal-length]})))))))

(deftest the-pose-column-on-a-varied-axis-is-the-cells-test
  (let [mpg (rdatasets/ggplot2-mpg)
        p (-> mpg (pj/pose :displ :hwy) pj/lay-point (pj/facet {:y [:hwy :cty]}))
        out (with-out-str (pj/svg-summary p))]
    (is (not (re-find #"not drawn" out)))
    (is (= {:panels 2 :points 468} (select-keys (pj/svg-summary p) [:panels :points])))))

(deftest a-pose-faceted-over-columns-is-a-cell-of-an-arrangement-test
  ;; The `:facet` node was expanded only on the pose handed to the
  ;; draft, so the same pose one level down kept a layer with no `:y`
  ;; and reported that `:point` needs both columns.
  (let [mpg (rdatasets/ggplot2-mpg)
        faceted (-> mpg (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]}))
        y-domains (fn [pose]
                    (vec (for [sp (:sub-plots (pj/plan pose)) p (:panels (:plan sp))] (:y-domain p))))]
    (is (= {:panels 2 :points 468}
           (select-keys (pj/svg-summary (pj/arrange [faceted])) [:panels :points])))
    (is (= {:panels 3 :points 702}
           (select-keys (pj/svg-summary (pj/arrange [faceted (pj/lay-point mpg :displ :cyl)]))
                        [:panels :points])))
    (testing "the panels draw what the pose draws alone"
      (is (= (y-domains faceted) (y-domains (pj/arrange [faceted])))))
    (testing "a faceted cell with no data of its own reads the data above it"
      (is (= {:panels 2 :points 468}
             (select-keys (pj/svg-summary {:data mpg
                                           :poses [{:mapping {:x :displ}
                                                    :layers [{:layer-type :point}]
                                                    :facet {:y [:hwy :cty]}}]})
                          [:panels :points]))))))
