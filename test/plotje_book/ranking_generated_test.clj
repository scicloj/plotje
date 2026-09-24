(ns
 plotje-book.ranking-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l16
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v4_l28 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t5_l31
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v4_l28)))


(def
 v7_l40
 (-> (rdatasets/reshape2-tips) (pj/lay-bar :day {:color :smoker})))


(deftest
 t8_l43
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v7_l40)))


(def
 v10_l52
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :smoker})))


(deftest
 t11_l55
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v10_l52)))


(def
 v13_l64
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t14_l67
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panel
      (first
       (:panels
        (pj/plan
         (->
          (rdatasets/palmerpenguins-penguins)
          (pj/lay-bar :island {:position :fill, :color :species})))))
      [y0 y1]
      (:y-domain panel)]
     (and
      (= 1 (:panels s))
      (pos? (:polygons s))
      (== 0.0 y0)
      (== 1.0 y1))))
   v13_l64)))


(def
 v16_l87
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t17_l91
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/datasets-iris)
        (pj/lay-bar :species)
        (pj/coord :flip)))
      panel
      (first (:panels plan))
      iris-order
      (vec (distinct ((rdatasets/datasets-iris) :species)))]
     (and
      (= 1 (:panels s))
      (pos? (:polygons s))
      (= iris-order (:values (:y-ticks panel))))))
   v16_l87)))


(def
 v19_l115
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :time})
  (pj/coord :flip)))


(deftest
 t20_l119
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v19_l115)))


(def v22_l133 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t23_l136
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v22_l133)))


(def v25_l144 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t26_l147
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v25_l144)))


(def
 v28_l160
 (def sales-ranked (-> sales tc/dataset (tc/order-by :revenue :asc))))


(def v29_l165 (-> sales-ranked (pj/lay-bar :revenue :product)))


(deftest
 t30_l168
 (is
  ((fn
    [v]
    (let
     [panel
      (-> v pj/frames :panels first)
      bands
      (-> v pj/plan :panels first :y-domain vec)
      height-of
      (fn [c] (second (pj/to-drawing panel 100 c)))]
     (and
      (= ["doohickey" "widget" "gizmo" "gadget"] bands)
      (= 4 (:polygons (pj/svg-summary v)))
      (apply > (mapv height-of bands))
      (= "gadget" (last bands))
      (= 340 (apply max (:revenue sales))))))
   v29_l165)))


(def
 v32_l189
 (->
  {:department
   ["Office Supplies"
    "Electronics"
    "Home Goods"
    "Sporting Gear"
    "Garden Tools"
    "Toys"],
   :revenue [120 340 210 95 160 80]}
  (pj/lay-bar :department :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t33_l195
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 6 (:polygons s))
      (.contains (pr-str (pj/plot v)) "rotate(-45"))))
   v32_l189)))


(def v35_l207 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t36_l210
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v35_l207)))


(def
 v38_l218
 (-> sales (pj/lay-lollipop :product :revenue) (pj/coord :flip)))


(deftest
 t39_l222
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v38_l218)))


(def
 v41_l231
 (->
  {:product ["A" "B" "C" "D" "E" "F"],
   :revenue [120 95 150 80 200 110],
   :region ["North" "South" "North" "South" "North" "South"]}
  (pj/lay-lollipop :product :revenue {:color :region})))


(deftest
 t42_l236
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 6 (:points s)) (= 2 (count fills)))))
   v41_l231)))


(def v44_l249 (-> sales (pj/lay-segment :product :revenue {:y-end 0})))


(deftest
 t45_l252
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:lines s)) (zero? (:points s)))))
   v44_l249)))


(def
 v47_l260
 (->
  {:index (range 1 41),
   :residual
   (map
    (fn*
     [p1__11193#]
     (* (Math/sin p1__11193#) (Math/exp (- (/ p1__11193# 30.0)))))
    (range 1 41))}
  (pj/lay-segment :index :residual {:y-end 0})
  (pj/lay-rule-h {:y-intercept 0})))


(deftest
 t48_l265
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 41 (:lines s)))))
   v47_l260)))
