(ns scicloj.plotje.series-messages-test
  "A message about a layer whose columns are a series names the columns
   written, never `:value` and `:series`, which the pivot invents."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def data {:quarter ["Q1" "Q2" "Q3" "Q4"] :revenue [10 12 14 9] :cost [6 7 8 9]})

(defn- message [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest a-series-is-named-by-its-columns-test
  (testing "a stacked horizontal value bar"
    (let [m (message #(pj/plan (pj/lay-bar data [:revenue :cost] :quarter {:position :stack})))]
      (is (re-find #"Write the layer with :quarter first and the series revenue, cost second" m))
      (is (not (re-find #":value" m)))
      (testing "and the layer the message describes draws"
        (is (= 8 (:polygons (pj/svg-summary (-> data
                                                (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
                                                (pj/coord :flip)))))))))
  (testing "a boxplot given a series and no :y column"
    (let [m (message #(pj/plan (pj/lay-boxplot data {:x [:revenue :cost]})))]
      (is (re-find #"It names :x the series revenue, cost\." m))
      (is (re-find #"Available columns besides the series: \[:quarter\]" m))
      (is (not (re-find #":value|:series" m)))))
  (testing "the Note about two units on one axis"
    (let [printed (with-out-str (pj/plan (-> data (pj/lay-bar :quarter) (pj/lay-point :quarter [:revenue :cost]))))]
      (is (re-find #"pj/lay-point draws the series revenue, cost \(numerical\)" printed))
      (is (not (re-find #":value" printed)))))
  (testing "a column the writer named :value is printed as written"
    (let [m (message #(pj/plan (pj/lay-boxplot {:value [1 2 3]} {:x :value})))]
      (is (re-find #"It names :x :value\." m)))))
