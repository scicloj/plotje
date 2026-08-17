(ns
 plotje-book.column-types-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l31 (def numerical {:k [1 2 3 4], :v [10 20 30 40]}))


(def v4_l34 (def categorical {:k ["a" "b" "c" "d"], :v [10 20 30 40]}))


(def
 v5_l37
 (def
  temporal
  {:k
   [(java.time.LocalDate/parse "2026-01-15")
    (java.time.LocalDate/parse "2026-02-15")
    (java.time.LocalDate/parse "2026-03-15")
    (java.time.LocalDate/parse "2026-04-15")],
   :v [10 20 30 40]}))


(def v7_l50 (-> numerical (pj/lay-point :k :v)))


(deftest
 t8_l53
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (false? (:categorical? ticks))
      (contains? (set (:labels ticks)) "2.0"))))
   v7_l50)))


(def v10_l62 (-> categorical (pj/lay-point :k :v)))


(deftest
 t11_l65
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["a" "b" "c" "d"] (vec (:labels ticks))))))
   v10_l62)))


(def v13_l74 (-> temporal (pj/lay-point :k :v)))


(deftest
 t14_l77
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
   v13_l74)))


(def v16_l88 (-> categorical (pj/lay-point :v :v {:color :k})))


(deftest
 t17_l91
 (is
  ((fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v16_l88)))


(def v19_l98 (-> numerical (pj/lay-point :v :v {:color :k})))


(deftest
 t20_l101
 (is
  ((fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v19_l98)))


(def
 v22_l116
 (try
  (-> numerical (pj/lay-boxplot :k :v) pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t23_l122
 (is ((fn [m] (re-find #"requires a categorical column" m)) v22_l116)))


(def
 v25_l137
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue {:x-type :categorical})))


(deftest
 t26_l140
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))
   v25_l137)))


(def v28_l176 (-> numerical (pj/lay-point :k :v) (pj/scale :x :log)))


(deftest
 t29_l180
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :x-scale :type)))
   v28_l176)))


(def
 v31_l186
 (try
  (->
   numerical
   (pj/lay-point :k :v)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t32_l194
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v31_l186)))


(def
 v34_l200
 (try
  (-> categorical (pj/lay-point :k :v) (pj/scale :x :log) pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t35_l208
 (is ((fn [m] (re-find #"requires numeric data" m)) v34_l200)))


(def
 v37_l216
 (-> categorical (pj/lay-point :k :v) (pj/scale :x :linear)))


(deftest
 t38_l220
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary (-> categorical (pj/lay-point :k :v)))))
   v37_l216)))
