(ns
 plotje-book.drawn-rows-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l25
 (->
  {:height [1 2 3 4], :weight [1 2 3 4]}
  (pj/lay-point :height :weight)))


(deftest
 t4_l28
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v3_l25)))


(def
 v6_l36
 (->
  {:height [1 2 nil 4], :weight [1 2 3 4]}
  (pj/lay-point :height :weight)))


(deftest
 t7_l39
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v6_l36)))


(def
 v9_l51
 (->
  {:height [1 2 ##Inf 4], :weight [1 2 3 4]}
  (pj/lay-point :height :weight)))


(deftest
 t10_l54
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v9_l51)))


(def
 v12_l63
 (->
  {:height [1 2 3 4], :weight [1 0 10 100]}
  (pj/lay-point :height :weight)
  (pj/scale :y :log)))


(deftest
 t13_l67
 (is
  ((fn
    [v]
    (and
     (= 3 (:points (pj/svg-summary v)))
     (re-find
      #"Removed 1 row containing non-positive values \(log scale on :weight\)"
      (with-out-str (pj/plan v)))))
   v12_l63)))


(def
 v15_l79
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :species ["a" nil "b" "a"]}
  (pj/lay-point :height :weight {:color :species})))


(deftest
 t16_l82
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:points s))
      (= ["a" "b"] (filterv #{"a" "b"} (:texts s))))))
   v15_l79)))


(def
 v18_l104
 (->
  {:step [1 2 3], :height [1 nil 3], :weight [4 5 6]}
  (pj/lay-point :step [:height :weight])))


(deftest
 t19_l107
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l104)))


(def
 v21_l126
 (->
  {:height [1 2 3 4], :weight [1 2 3 40]}
  (pj/lay-point :height :weight)
  (pj/scale :y {:domain [0 5]})))


(deftest
 t22_l130
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 1 (:clips s)))))
   v21_l126)))
