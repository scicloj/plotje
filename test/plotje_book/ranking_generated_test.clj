(ns
 plotje-book.ranking-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l14
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v4_l26 (-> (rdatasets/datasets-iris) (pj/lay-bar :species)))


(deftest
 t5_l29
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v4_l26)))


(def
 v7_l38
 (-> (rdatasets/reshape2-tips) (pj/lay-bar :day {:color :smoker})))


(deftest
 t8_l41
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v7_l38)))


(def
 v10_l50
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:position :stack, :color :smoker})))


(deftest
 t11_l53
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v10_l50)))


(def
 v13_l62
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :fill, :color :species})))


(deftest
 t14_l65
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
   v13_l62)))


(def
 v16_l85
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t17_l89
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
   v16_l85)))


(def
 v19_l113
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :time})
  (pj/coord :flip)))


(deftest
 t20_l117
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v19_l113)))


(def v22_l131 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t23_l134
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v22_l131)))


(def v25_l142 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t26_l145
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v25_l142)))


(def
 v28_l157
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
 t29_l163
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 6 (:polygons s))
      (.contains (pr-str (pj/plot v)) "rotate(-45"))))
   v28_l157)))


(def v31_l171 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t32_l174
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v31_l171)))


(def
 v34_l182
 (-> sales (pj/lay-lollipop :product :revenue) (pj/coord :flip)))


(deftest
 t35_l186
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v34_l182)))


(def
 v37_l195
 (->
  {:product ["A" "B" "C" "D" "E" "F"],
   :revenue [120 95 150 80 200 110],
   :region ["North" "South" "North" "South" "North" "South"]}
  (pj/lay-lollipop :product :revenue {:color :region})))


(deftest
 t38_l200
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 6 (:points s)) (= 2 (count fills)))))
   v37_l195)))
