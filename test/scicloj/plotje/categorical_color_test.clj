(ns scicloj.plotje.categorical-color-test
  "A categorical column mapped to both an axis and `:color` drew every
   mark in the first palette colour while the legend listed the rest.
   Reported on #plotje 2026-09-02, reproduced on 0.10.1."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def keyword-rows [{:g :a :v 3} {:g :b :v 5} {:g :c :v 4}])
(def string-rows  [{:g "a" :v 3} {:g "b" :v 5} {:g "c" :v 4}])

(def ^:private panel-background "rgb(232,232,232)")

(defn- svg-elements [pose tag]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= tag (first %)) (map? (second %))))
       (map second)))

(defn- fill-count
  "How many distinct mark colours are drawn. The panel background is a
   filled rect too, so it is excluded."
  [pose tag]
  (->> (svg-elements pose tag)
       (keep :fill)
       (remove #{panel-background})
       distinct
       count))

(deftest keyword-category-colors-test
  (testing "a keyword column on the axis and on :color gets one colour per category"
    ;; The axis column is rewritten to display strings before the colour
    ;; lookup, while the category list keeps the raw keywords. The lookup
    ;; missed on every value and fell back to the first palette entry.
    (is (= 3 (fill-count (pj/lay-bar keyword-rows :g :v {:color :g}) :polygon))))
  (testing "and matches what the same data spelled as strings gets"
    (is (= (fill-count (pj/lay-bar string-rows :g :v {:color :g}) :polygon)
           (fill-count (pj/lay-bar keyword-rows :g :v {:color :g}) :polygon))))
  (testing "the same holds for a mark that is not a bar"
    (is (= 3 (fill-count (pj/lay-point keyword-rows :g :v {:color :g}) :rect)))))
