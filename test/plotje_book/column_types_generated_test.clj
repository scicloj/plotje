(ns
 plotje-book.column-types-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l32 (def numerical {:k [1 2 3 4], :v [10 20 30 40]}))


(def v4_l35 (def categorical {:k ["a" "b" "c" "d"], :v [10 20 30 40]}))


(def
 v5_l38
 (def
  temporal
  {:k
   [(java.time.LocalDate/parse "2026-01-15")
    (java.time.LocalDate/parse "2026-02-15")
    (java.time.LocalDate/parse "2026-03-15")
    (java.time.LocalDate/parse "2026-04-15")],
   :v [10 20 30 40]}))


(def v7_l51 (-> numerical (pj/lay-point :k :v)))


(deftest
 t8_l54
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (false? (:categorical? ticks))
      (contains? (set (:labels ticks)) "2.0"))))
   v7_l51)))


(def v10_l63 (-> categorical (pj/lay-point :k :v)))


(deftest
 t11_l66
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["a" "b" "c" "d"] (vec (:labels ticks))))))
   v10_l63)))


(def v13_l75 (-> temporal (pj/lay-point :k :v)))


(deftest
 t14_l78
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (false? (:categorical? ticks))
      (some
       (fn [l] (re-find #"^[A-Z][a-z]{2}-\d\d$" l))
       (:labels ticks)))))
   v13_l75)))


(def v16_l89 (-> categorical (pj/lay-point :v :v {:color :k})))


(deftest
 t17_l92
 (is
  ((fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v16_l89)))


(def v19_l99 (-> numerical (pj/lay-point :v :v {:color :k})))


(deftest
 t20_l102
 (is
  ((fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v19_l99)))


(def
 v22_l118
 (try
  (-> numerical (pj/lay-boxplot :k :v) pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t23_l124
 (is ((fn [m] (re-find #"requires a categorical column" m)) v22_l118)))


(def
 v25_l139
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue {:x-type :categorical})))


(deftest
 t26_l142
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))
   v25_l139)))
