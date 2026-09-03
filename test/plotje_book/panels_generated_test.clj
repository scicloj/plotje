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
  (pj/lay-point :height :weight {:color "#377eb8"})
  (pj/lay-point :depth :weight {:color "#e6550d"})))


(deftest
 t17_l98
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v16_l93)))


(def
 v19_l114
 (->
  measurements
  (pj/lay-point :height :weight {:color "#377eb8"})
  (pj/lay-point :depth :weight {:color "#e6550d", :overlay true})))


(deftest
 t20_l118
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v19_l114)))


(def
 v22_l125
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t23_l129
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v22_l125)))


(def
 v25_l134
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t26_l138
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v25_l134)))


(def
 v28_l151
 (try
  (->
   measurements
   (pj/lay-point :height :weight)
   (pj/lay-point :depth :weight)
   (pj/facet :species)
   pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t29_l159
 (is
  ((fn [m] (re-find #"not yet supported on composite poses" m))
   v28_l151)))
