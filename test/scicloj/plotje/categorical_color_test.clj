(ns scicloj.plotje.categorical-color-test
  "A categorical column mapped to both an axis and `:color` drew every
   mark in the first palette colour while the legend listed the rest.
   Reported on #plotje 2026-09-02, reproduced on 0.10.1."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def keyword-rows [{:g :a :v 3} {:g :b :v 5} {:g :c :v 4}])
(def string-rows  [{:g "a" :v 3} {:g "b" :v 5} {:g "c" :v 4}])

(def hyphenated-rows
  "Keyword categories a separator away from their display labels:
   :sepal-length is drawn on the axis as \"sepal length\". The plain
   keywords above survive `name`, so they were already matched; these
   are the ones the first fix missed."
  [{:g :sepal-length :v 3} {:g :sepal-width :v 5}
   {:g :petal-length :v 4} {:g :petal-width :v 2}])

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

(deftest hyphenated-keyword-category-colors-test
  (testing "a hyphenated keyword column on the axis and on :color keeps its colours"
    ;; `fmt-category-label` turns the separator into a space for the
    ;; axis, so `name` is not enough to match the value back to its
    ;; category and every mark took the first palette entry.
    (is (= 4 (fill-count (pj/lay-bar hyphenated-rows :g :v {:color :g}) :polygon))))
  (testing "one hyphenated category among plain ones keeps its own colour"
    (is (= 4 (fill-count (pj/lay-bar [{:g :a :v 3} {:g :b :v 5}
                                      {:g :c :v 4} {:g :d-e :v 2}]
                                     :g :v {:color :g})
                         :polygon))))
  (testing "and a map palette keyed as the column holds it is read the same way"
    (is (= 4 (fill-count (-> (pj/lay-bar hyphenated-rows :g :v {:color :g})
                             (pj/scale :color {:values {:sepal-length "#111111"
                                                        :sepal-width  "#222222"
                                                        :petal-length "#333333"
                                                        :petal-width  "#444444"}}))
                         :polygon)))))

(deftest group-without-a-color-column-test
  (testing ":group alone draws every group in the plot's default colour"
    ;; The split value rides in the point's `:color` slot, where it
    ;; labels the group and keys the dodge. It is not a colour: sending
    ;; it to the palette drew every group in the first entry, which is
    ;; one colour like the default is, so the colour is named here
    ;; rather than counted.
    (is (= #{"rgb(51,51,51)"}
           (->> (svg-elements (pj/lay-point keyword-rows :v :v {:group :g}) :rect)
                (keep :fill)
                (remove #{panel-background})
                set))))
  (testing "one grouping column agrees with two"
    (let [rows [{:x 1 :y 1 :g "a" :h "p"} {:x 2 :y 2 :g "a" :h "p"}
                {:x 1 :y 3 :g "b" :h "q"} {:x 2 :y 4 :g "b" :h "q"}]
          colors (fn [opts] (->> (svg-elements (pj/lay-line rows :x :y opts) :polyline)
                                 (keep :stroke) distinct set))]
      (is (= (colors {:group :g}) (colors {:group [:g :h]})))
      (is (= (colors {:group :g}) (colors {})))))
  (testing "a fixed colour beside a grouping column still wins"
    ;; The grid lines are polylines too, so the mark's colour is asked
    ;; for by name rather than by the whole set.
    (is (contains? (->> (svg-elements (pj/lay-line [{:x 1 :y 1 :g "a"} {:x 2 :y 2 :g "a"}]
                                                   :x :y {:group :g :color "#123456"})
                                      :polyline)
                        (keep :stroke) set)
                   "rgb(18,52,86)")))
  (testing "a colour column still takes the palette"
    (is (= 3 (fill-count (pj/lay-point keyword-rows :v :v {:color :g}) :rect)))))
