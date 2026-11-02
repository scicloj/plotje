(ns scicloj.plotje.computed-axis-title-test
  "The title of an axis a histogram or a density computes: a `:y` column
   that reaches such a layer from the pose's mapping or from the panel's
   columns is not read by it, and does not title the axis. The axis is
   titled by what the layer computes, `count` or `density`."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def flowers
  {:length [5.1 4.9 4.7 6.4 6.9 5.5 6.3 5.8 7.1 6.3]
   :width  [3.5 3.0 3.2 3.2 3.1 2.3 3.3 2.7 3.0 2.9]})

(defn- labels [pose]
  (let [plan (binding [*out* (java.io.StringWriter.)] (pj/plan pose))]
    [(:x-label plan) (:y-label plan)]))

(defn- draft-columns [pose]
  (mapv (juxt :mark :x :y)
        (filter #(and (map? %) (contains? % :__panel-idx))
                (tree-seq coll? seq (binding [*out* (java.io.StringWriter.)]
                                      (pj/draft pose))))))

(deftest a-computed-axis-is-not-titled-with-a-column-test
  ;; A histogram on a pose naming two columns titled its count axis
  ;; "width".
  (testing "the layer alone: the axis is titled by what it computes"
    (is (= ["length" "count"] (labels (pj/lay-histogram flowers :length))))
    (is (= ["length" "density"] (labels (pj/lay-histogram flowers :length {:normalize :density}))))
    (is (= ["kind" "count"] (labels (pj/lay-bar {:kind ["a" "b" "b"]} :kind))))
    (doseq [[layer title] [[pj/lay-histogram "count"] [pj/lay-density "density"]]]
      (is (= ["length" title] (labels (-> flowers (pj/pose :length :width) layer))))
      (is (= ["length" title] (labels (-> flowers (pj/pose {:y :width}) (layer :length)))))))
  (testing "a count beside a density reads in two units and is not titled"
    (is (= ["length" nil] (labels (-> flowers (pj/lay-histogram :length) (pj/lay-density :length))))))
  (testing "a rule or a rug on the x side leaves the title alone"
    (is (= ["length" "count"] (labels (-> flowers (pj/lay-histogram :length) (pj/lay-rule-v {:x-intercept 6})))))
    (is (= ["length" "count"] (labels (-> flowers (pj/lay-histogram :length) pj/lay-rug)))))
  (testing "an empty title removes it, and a written one replaces it"
    (is (= ["length" nil] (labels (-> flowers (pj/lay-histogram :length) (pj/options {:y-label ""})))))
    (is (= ["length" "n"] (labels (-> flowers (pj/lay-histogram :length) (pj/options {:y-label "n"}))))))
  (testing "the draft layer holds no :y column"
    (is (= [[:bar :length nil]]
           (draft-columns (-> flowers (pj/pose :length :width) pj/lay-histogram)))))
  (testing "beside points, the axis is titled by the column the points draw, in either order"
    (is (= ["length" "width"]
           (labels (-> flowers (pj/pose :length :width) pj/lay-point pj/lay-histogram))))
    (is (= ["length" "width"]
           (labels (-> flowers (pj/pose :length :width) pj/lay-histogram pj/lay-point))))
    (is (= [[:point :length :width] [:bar :length nil]]
           (draft-columns (-> flowers (pj/lay-point :length :width) pj/lay-histogram)))))
  (testing "a rug reads a :y column, for the ticks along that axis"
    (is (= [[:point :length :width] [:rug :length :width]]
           (draft-columns (-> flowers (pj/lay-point :length :width) (pj/lay-rug {:side :both}))))))
  (testing "a column on :y alone is still drawn on its side and titled"
    (is (= ["count" "width"] (labels (-> flowers (pj/pose {:y :width}) pj/lay-histogram))))))
