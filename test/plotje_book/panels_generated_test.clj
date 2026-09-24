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
 v13_l74
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t14_l77
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v13_l74)))


(def
 v16_l92
 (->
  measurements
  pj/overlay
  (pj/lay-point :height :weight {:color "#377eb8"})
  (pj/lay-point :depth :weight {:color "#e6550d"})))


(deftest
 t17_l97
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none"))
      (some #{"height"} (:texts s))
      (not-any? #{"height, depth"} (:texts s))
      (some
       #{"height, depth"}
       (:texts
        (pj/svg-summary
         (->
          measurements
          pj/overlay
          (pj/lay-point :height :weight)
          (pj/lay-point :depth :weight))))))))
   v16_l92)))


(def
 v19_l128
 (->
  measurements
  (pj/lay-point :height :weight {:color "#377eb8"})
  (pj/lay-point :depth :weight {:color "#e6550d", :overlay true})))


(deftest
 t20_l132
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v19_l128)))


(def
 v22_l139
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t23_l143
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v22_l139)))


(def
 v25_l148
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t26_l152
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v25_l148)))


(def
 v28_l165
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet :species)))


(deftest
 t29_l170
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 4 (:panels s)))) v28_l165)))


(def
 v31_l179
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t32_l184
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v31_l179)))
