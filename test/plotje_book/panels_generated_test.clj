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
 v13_l69
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/lay-line :height :weight {:color "black"})))


(deftest
 t14_l74
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= ["height" "depth"] (mapv :label (:entries (:legend plan))))
      (contains? (:colors (pj/svg-summary v)) "rgb(0,0,0)"))))
   v13_l69)))


(def
 v16_l83
 (->
  measurements
  (pj/pose {:color :species})
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t17_l88
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
   v16_l83)))


(def
 v19_l101
 (->
  measurements
  (pj/lay-point :height :weight {:color :species})
  (pj/lay-point :depth :weight)))


(deftest
 t20_l105
 (is
  ((fn
    [v]
    (= ["a" "b"] (mapv :label (:entries (:legend (pj/plan v))))))
   v19_l101)))


(def
 v22_l118
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t23_l123
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v22_l118)))


(def
 v25_l129
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t26_l134
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v25_l129)))


(def
 v28_l143
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t29_l146
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v28_l143)))


(def
 v31_l157
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t32_l161
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v31_l157)))


(def
 v34_l166
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t35_l170
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v34_l166)))


(def
 v37_l182
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet :species)))


(deftest
 t38_l187
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v37_l182)))


(def
 v40_l192
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:col :species, :row {:x [:height :depth]}})))


(deftest
 t41_l197
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v40_l192)))


(def
 v43_l202
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t44_l207
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v43_l202)))
