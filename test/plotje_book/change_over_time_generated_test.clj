(ns
 plotje-book.change-over-time-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l18
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__91570#] (Math/sin (* p1__91570# 0.3))) (range 30))}))


(def v4_l21 (-> wave (pj/lay-line :x :y)))


(deftest
 t5_l24
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v4_l21)))


(def
 v7_l38
 (def
  waves-wide
  (tc/dataset
   {:x (range 30),
    :sin
    (map (fn* [p1__91571#] (Math/sin (* p1__91571# 0.3))) (range 30)),
    :cos
    (map
     (fn* [p1__91572#] (Math/cos (* p1__91572# 0.3)))
     (range 30))})))


(def
 v8_l44
 (def
  waves
  (tc/pivot->longer
   waves-wide
   [:sin :cos]
   {:target-columns :function, :value-column-name :y})))


(def v9_l49 (-> waves (pj/lay-line :x :y {:color :function})))


(deftest
 t10_l52
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v9_l49)))


(def v12_l60 (-> wave (pj/lay-line :x :y {:size 4})))


(deftest
 t13_l63
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v12_l60)))


(def
 v15_l71
 (def
  growth
  {:day [1 2 3 4 5 1 2 3 4 5],
   :value [10 15 13 18 22 8 12 11 16 19],
   :group [:a :a :a :a :a :b :b :b :b :b]}))


(def
 v16_l76
 (->
  growth
  (pj/pose :day :value {:color :group})
  pj/lay-line
  pj/lay-point))


(deftest
 t17_l81
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 10 (:points s)) (= 2 (:lines s)))))
   v16_l76)))


(def
 v19_l89
 (-> {:x [1 2 3 4 5], :y [2 4 1 5 3]} (pj/lay-step :x :y) pj/lay-point))


(deftest
 t20_l94
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v19_l89)))


(def
 v22_l102
 (->
  growth
  (pj/pose :day :value {:color :group})
  pj/lay-step
  pj/lay-point))


(deftest
 t23_l107
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 10 (:points s)) (= 2 (:lines s)))))
   v22_l102)))


(def
 v25_l116
 (->
  {:x (concat (range 5) (range 5) (range 5)),
   :y (concat [1 2 3 4 5] [2 2 2 2 2] [3 1 2 1 2]),
   :group (concat (repeat 5 "A") (repeat 5 "B") (repeat 5 "C"))}
  (pj/lay-step :x :y {:position :stack, :color :group})))


(deftest
 t26_l121
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:lines s)))))
   v25_l116)))


(def
 v28_l129
 (->
  {:x (range 30),
   :y
   (map (fn* [p1__91573#] (Math/sin (* p1__91573# 0.3))) (range 30))}
  (pj/lay-area :x :y)))


(deftest
 t29_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v28_l129)))


(def
 v31_l141
 (->
  {:x (concat (range 10) (range 10) (range 10)),
   :y
   (concat
    [1 2 3 4 5 4 3 2 1 0]
    [2 2 2 3 3 3 2 2 2 2]
    [1 1 1 1 2 2 2 1 1 1]),
   :group (concat (repeat 10 "A") (repeat 10 "B") (repeat 10 "C"))}
  (pj/lay-area :x :y {:position :stack, :color :group})))


(deftest
 t32_l148
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v31_l141)))


(def
 v34_l160
 (def
  temp-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"
     #inst "2024-04-01T00:00:00.000-00:00"
     #inst "2024-05-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"],
    :temperature [3 5 9 14 19 23]}
   (pj/lay-line :date :temperature)
   pj/lay-point)))


(def v35_l167 temp-pose)


(deftest
 t36_l169
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panel
      (first (:panels (pj/plan temp-pose)))
      tick-labels
      (:labels (:x-ticks panel))]
     (and
      (= 6 (:points s))
      (= 1 (:lines s))
      (some
       (fn* [p1__91574#] (re-find #"[A-Z][a-z]{2}" p1__91574#))
       tick-labels))))
   v35_l167)))


(def
 v38_l186
 (def
  months
  [#inst "2024-01-01T00:00:00.000-00:00"
   #inst "2024-02-01T00:00:00.000-00:00"
   #inst "2024-03-01T00:00:00.000-00:00"
   #inst "2024-04-01T00:00:00.000-00:00"
   #inst "2024-05-01T00:00:00.000-00:00"
   #inst "2024-06-01T00:00:00.000-00:00"]))


(def
 v39_l190
 (->
  {:date (concat months months),
   :temperature [3 5 9 14 19 23 15 17 19 22 25 28],
   :city (concat (repeat 6 "Zurich") (repeat 6 "Athens"))}
  (pj/lay-line :date :temperature {:color :city})
  pj/lay-point))


(deftest
 t40_l198
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 2 (:lines s)))))
   v39_l190)))


(def
 v42_l207
 (->
  {:date
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"],
   :sales [10 25 30 22 35 40]}
  (pj/lay-area :date :sales)))


(deftest
 t43_l212
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v42_l207)))


(def
 v45_l225
 (->
  {:date
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"
    #inst "2024-07-01T00:00:00.000-00:00"
    #inst "2024-08-01T00:00:00.000-00:00"
    #inst "2024-09-01T00:00:00.000-00:00"
    #inst "2024-10-01T00:00:00.000-00:00"
    #inst "2024-11-01T00:00:00.000-00:00"
    #inst "2024-12-01T00:00:00.000-00:00"],
   :sales [10 14 12 18 22 19 25 28 24 30 27 33]}
  (pj/pose :date :sales)
  pj/lay-line
  pj/lay-smooth))


(deftest
 t46_l234
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v45_l225)))


(def
 v48_l247
 (->
  {:t (range 12), :delta [-3 -1 -2 0 2 4 -1 3 5 -2 1 4]}
  (pj/lay-line :t :delta)
  pj/lay-point
  (pj/lay-rule-h {:y-intercept 0, :color "#888"})))


(deftest
 t49_l253
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 2 (:lines s)))))
   v48_l247)))
