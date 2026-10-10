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
 v10_l55
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t11_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (some #{"height, depth"} (:texts s)))))
   v10_l55)))


(def
 v13_l67
 (->
  measurements
  (pj/pose {:color :species})
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t14_l72
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (some #{"height, depth"} (:texts s))
      (some #{"species"} (:texts s))
      (not-any? #{"height" "depth"} (:texts s)))))
   v13_l67)))


(def
 v16_l85
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t17_l90
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v16_l85)))


(def
 v19_l96
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t20_l101
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v19_l96)))


(def
 v22_l110
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t23_l113
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v22_l110)))


(def
 v25_l124
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t26_l128
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v25_l124)))


(def
 v28_l133
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t29_l137
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v28_l133)))


(def
 v31_l149
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet :species)))


(deftest
 t32_l154
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v31_l149)))


(def
 v34_l159
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:col :species, :row {:x [:height :depth]}})))


(deftest
 t35_l164
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v34_l159)))


(def
 v37_l169
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t38_l174
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v37_l169)))
