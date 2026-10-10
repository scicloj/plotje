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
 v13_l72
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t14_l77
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v13_l72)))


(def
 v16_l83
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t17_l88
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v16_l83)))


(def
 v19_l97
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t20_l100
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v19_l97)))


(def
 v22_l111
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t23_l115
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v22_l111)))


(def
 v25_l120
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t26_l124
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v25_l120)))


(def
 v28_l136
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet :species)))


(deftest
 t29_l141
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v28_l136)))


(def
 v31_l146
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:col :species, :row {:x [:height :depth]}})))


(deftest
 t32_l151
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v31_l146)))


(def
 v34_l156
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t35_l161
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v34_l156)))
