(ns
 plotje-book.column-types-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l30 (def numerical {:k [1 2 3 4], :v [10 20 30 40]}))


(def v4_l33 (def categorical {:k ["a" "b" "c" "d"], :v [10 20 30 40]}))


(def
 v5_l36
 (def
  temporal
  {:k
   [(java.time.LocalDate/parse "2026-01-15")
    (java.time.LocalDate/parse "2026-02-15")
    (java.time.LocalDate/parse "2026-03-15")
    (java.time.LocalDate/parse "2026-04-15")],
   :v [10 20 30 40]}))


(def v7_l49 (-> numerical (pj/lay-point :k :v)))


(deftest
 t8_l52
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (false? (:categorical? ticks))
      (contains? (set (:labels ticks)) "2.0"))))
   v7_l49)))


(def v10_l61 (-> categorical (pj/lay-point :k :v)))


(deftest
 t11_l64
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["a" "b" "c" "d"] (vec (:labels ticks))))))
   v10_l61)))


(def v13_l73 (-> temporal (pj/lay-point :k :v)))


(deftest
 t14_l76
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
   v13_l73)))


(def v16_l87 (-> categorical (pj/lay-point :v :v {:color :k})))


(deftest
 t17_l90
 (is
  ((fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v16_l87)))


(def v19_l97 (-> numerical (pj/lay-point :v :v {:color :k})))


(deftest
 t20_l100
 (is
  ((fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v19_l97)))


(def
 v22_l115
 (try
  (-> numerical (pj/lay-boxplot :k :v) pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t23_l121
 (is ((fn [m] (re-find #"requires a categorical column" m)) v22_l115)))


(def
 v25_l136
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue {:x-type :categorical})))


(deftest
 t26_l139
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))
   v25_l136)))


(def v28_l175 (-> numerical (pj/lay-point :k :v) (pj/scale :x :log)))


(deftest
 t29_l179
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :x-scale :type)))
   v28_l175)))


(def
 v31_l185
 (try
  (->
   numerical
   (pj/lay-point :k :v)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t32_l193
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v31_l185)))


(def
 v34_l199
 (try
  (-> categorical (pj/lay-point :k :v) (pj/scale :x :log) pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t35_l207
 (is ((fn [m] (re-find #"requires numeric data" m)) v34_l199)))


(def
 v37_l215
 (-> categorical (pj/lay-point :k :v) (pj/scale :x :linear)))


(deftest
 t38_l219
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary (-> categorical (pj/lay-point :k :v)))))
   v37_l215)))
