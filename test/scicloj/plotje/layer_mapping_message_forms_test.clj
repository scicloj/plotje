(ns scicloj.plotje.layer-mapping-message-forms-test
  "A message that shows a pj/lay-* call shows a whole one: the data
   argument first, and the map called the layer's mapping."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def rows
  {:quarter ["q1" "q2" "q3"] :revenue [10 12 14] :lo [9 11 13] :hi [11 13 15]
   :note ["a" "b" "c"]})

(defn- message [pose]
  (try (binding [*out* (java.io.StringWriter.)] (pj/plan pose))
       nil
       (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest an-errorbar-with-no-bounds
  (let [m (message (pj/lay-errorbar rows :quarter :revenue))]
    (is (re-find #"Name both in the layer's mapping: \(pj/lay-errorbar data :quarter :revenue \{:y-min :lo-column :y-max :hi-column\}\)" m))
    (is (not (re-find #"options" m)))
    (testing "the form, with this data and its columns"
      (is (= 1 (:panels (pj/svg-summary
                         (pj/lay-errorbar rows :quarter :revenue {:y-min :lo :y-max :hi}))))))))

(deftest an-errorbar-with-a-written-bound
  (let [m (message (pj/lay-errorbar rows :quarter :revenue {:y-min 2.0 :y-max 20.0}))]
    (is (re-find #"one bound per row: \(pj/lay-errorbar data :x-column :y-column \{:y-min :lo-column :y-max :hi-column\}\)" m))))

(deftest a-text-layer-with-no-text
  (let [m (message (pj/lay-text rows :revenue :lo))]
    (is (re-find #"Name it in the layer's mapping: \(pj/lay-text data :revenue :lo \{:text :label-column\}\)" m))
    (is (not (re-find #"option" m)))
    (testing "the form, with this data and its columns"
      (is (= 3 (count (filter #{"a" "b" "c"}
                              (:texts (pj/svg-summary
                                       (pj/lay-text rows :revenue :lo {:text :note}))))))))))
