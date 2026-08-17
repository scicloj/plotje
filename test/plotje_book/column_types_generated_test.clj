(ns
 plotje-book.column-types-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
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
 v22_l119
 (defn
  inferred-mark
  "The mark a pose is drawn with when no layer type is named."
  [pose]
  (-> pose pj/plan :panels first :layers first :mark)))


(def
 v23_l124
 (tc/dataset
  [{:x-column "numerical",
    :y-column "none",
    :mark (inferred-mark (pj/pose numerical :k))}
   {:x-column "temporal",
    :y-column "none",
    :mark (inferred-mark (pj/pose temporal :k))}
   {:x-column "categorical",
    :y-column "none",
    :mark (inferred-mark (pj/pose categorical :k))}
   {:x-column "temporal",
    :y-column "numerical",
    :mark (inferred-mark (pj/pose temporal :k :v))}
   {:x-column "categorical",
    :y-column "numerical",
    :mark (inferred-mark (pj/pose categorical :k :v))}
   {:x-column "numerical",
    :y-column "categorical",
    :mark (inferred-mark (pj/pose categorical :v :k))}
   {:x-column "numerical",
    :y-column "numerical",
    :mark (inferred-mark (pj/pose numerical :k :v))}]))


(deftest
 t24_l133
 (is
  ((fn
    [ds]
    (=
     [:bar :bar :rect :line :boxplot :boxplot :point]
     (vec (:mark ds))))
   v23_l124)))


(def v26_l144 (pj/pose temporal :k :v))


(deftest
 t27_l146
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v26_l144)))


(def
 v29_l154
 (def
  readings
  {:batch ["a" "a" "a" "a" "b" "b" "b" "c" "c"],
   :reading [3 5 4 6 8 9 7 2 6]}))


(def v30_l158 (pj/pose readings :batch :reading))


(deftest
 t31_l160
 (is ((fn [v] (= :boxplot (inferred-mark v))) v30_l158)))


(def v33_l166 (pj/pose readings :batch))


(deftest
 t34_l168
 (is
  ((fn
    [v]
    (and
     (= :rect (inferred-mark v))
     (= 3 (:polygons (pj/svg-summary v)))))
   v33_l166)))


(def
 v36_l185
 (try
  (-> numerical (pj/lay-boxplot :k :v) pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t37_l191
 (is ((fn [m] (re-find #"requires a categorical column" m)) v36_l185)))


(def
 v39_l206
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue {:x-type :categorical})))


(deftest
 t40_l209
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))
   v39_l206)))


(def v42_l245 (-> numerical (pj/lay-point :k :v) (pj/scale :x :log)))


(deftest
 t43_l249
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :x-scale :type)))
   v42_l245)))


(def
 v45_l255
 (try
  (->
   numerical
   (pj/lay-point :k :v)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t46_l263
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v45_l255)))


(def
 v48_l269
 (try
  (-> categorical (pj/lay-point :k :v) (pj/scale :x :log) pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t49_l277
 (is ((fn [m] (re-find #"requires numeric data" m)) v48_l269)))


(def
 v51_l285
 (-> categorical (pj/lay-point :k :v) (pj/scale :x :linear)))


(deftest
 t52_l289
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary (-> categorical (pj/lay-point :k :v)))))
   v51_l285)))
