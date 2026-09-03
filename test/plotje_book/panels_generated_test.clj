(ns
 plotje-book.panels-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l25
 (def
  measurements
  {:height [1 2 3 4],
   :weight [1 2 3 4],
   :depth [2 3 4 5],
   :species ["a" "a" "b" "b"]}))


(def v4_l36 (-> measurements (pj/lay-point :height :weight)))


(deftest
 t5_l39
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v4_l36)))


(def
 v7_l44
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line :height :weight)))


(deftest
 t8_l48
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v7_l44)))


(def
 v10_l59
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t11_l63
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v10_l59)))


(def
 v13_l75
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t14_l78
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v13_l75)))


(def
 v16_l93
 (->
  measurements
  pj/overlay
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t17_l98
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v16_l93)))


(def
 v19_l109
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight {:overlay true})))


(deftest
 t20_l113
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v19_l109)))


(def
 v22_l120
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t23_l124
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v22_l120)))


(def
 v25_l129
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t26_l133
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v25_l129)))


(def
 v28_l146
 (try
  (->
   measurements
   (pj/lay-point :height :weight)
   (pj/lay-point :depth :weight)
   (pj/facet :species)
   pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t29_l154
 (is
  ((fn [m] (re-find #"not yet supported on composite poses" m))
   v28_l146)))
