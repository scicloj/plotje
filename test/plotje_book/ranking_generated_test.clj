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


(def v16_l87 (-> (rdatasets/datasets-iris) (pj/lay-bar {:y :species})))


(deftest
 t17_l90
 (is
  ((fn
    [v]
    (let
     [panel (first (:panels (pj/plan v)))]
     (and
      (= 3 (:polygons (pj/svg-summary v)))
      (= ["setosa" "versicolor" "virginica"] (:y-domain panel))
      (zero? (first (:x-domain panel))))))
   v16_l87)))


(def
 v19_l101
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t20_l105
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
      (= iris-order (:values (:y-ticks panel)))
      (=
       s
       (pj/svg-summary
        (-> (rdatasets/datasets-iris) (pj/lay-bar {:y :species})))))))
   v19_l101)))


(def
 v22_l133
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-bar :day {:color :time})
  (pj/coord :flip)))


(deftest
 t23_l137
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v22_l133)))


(def v25_l151 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t26_l154
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v25_l151)))


(def v28_l162 (-> sales (pj/lay-bar :revenue :product)))


(deftest
 t29_l165
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v28_l162)))


(def
 v31_l178
 (def sales-ranked (-> sales tc/dataset (tc/order-by :revenue :asc))))


(def v32_l183 (-> sales-ranked (pj/lay-bar :revenue :product)))


(deftest
 t33_l186
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
      (= 340 (apply max (:revenue sales)))
      (=
       bands
       (->
        sales-ranked
        (pj/lay-bar :product :revenue)
        (pj/coord :flip)
        pj/plan
        :panels
        first
        :y-domain
        vec)))))
   v32_l183)))


(def
 v35_l216
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
 t36_l222
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 6 (:polygons s))
      (.contains (pr-str (pj/plot v)) "rotate(-45"))))
   v35_l216)))


(def v38_l234 (-> sales (pj/lay-lollipop :product :revenue)))


(deftest
 t39_l237
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v38_l234)))


(def
 v41_l245
 (-> sales (pj/lay-lollipop :product :revenue) (pj/coord :flip)))


(deftest
 t42_l249
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (= 4 (:lines s)))))
   v41_l245)))


(def
 v44_l258
 (->
  {:product ["A" "B" "C" "D" "E" "F"],
   :revenue [120 95 150 80 200 110],
   :region ["North" "South" "North" "South" "North" "South"]}
  (pj/lay-lollipop :product :revenue {:color :region})))


(deftest
 t45_l263
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 6 (:points s)) (= 2 (count fills)))))
   v44_l258)))


(def v47_l276 (-> sales (pj/lay-segment :product :revenue {:y-end 0})))


(deftest
 t48_l279
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:lines s)) (zero? (:points s)))))
   v47_l276)))


(def
 v50_l287
 (->
  {:index (range 1 41),
   :residual
   (map
    (fn*
     [p1__79686#]
     (* (Math/sin p1__79686#) (Math/exp (- (/ p1__79686# 30.0)))))
    (range 1 41))}
  (pj/lay-segment :index :residual {:y-end 0})
  (pj/lay-rule-h {:y-intercept 0})))


(deftest
 t51_l292
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 41 (:lines s)))))
   v50_l287)))


(def
 v53_l306
 (->
  {:team ["Ops" "Sales" "Support" "Research"],
   :before [62 48 71 55],
   :after [74 66 69 81]}
  (pj/lay-segment :before :team {:x-end :after})
  (pj/lay-point :before :team)
  (pj/lay-point :after :team)))


(deftest
 t54_l313
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (pj/plan v)]
     (and
      (= 1 (:panels s))
      (= 4 (:lines s))
      (= 8 (:points s))
      (= ["before" "after"] (mapv :label (:entries (:legend p)))))))
   v53_l306)))


(def
 v56_l326
 (->
  {:team ["Ops" "Sales" "Support" "Research"],
   :before [62 48 71 55],
   :after [74 66 69 81]}
  (pj/lay-segment :before :team {:x-end :after, :color "grey"})
  (pj/lay-point :before :team)
  (pj/lay-point :after :team)))


(deftest
 t57_l333
 (is
  ((fn
    [v]
    (let
     [segment-color
      (fn
       [fr]
       (->
        fr
        pj/plan
        :panels
        first
        :layers
        first
        :groups
        first
        :color))
      before-color
      (fn [fr] (-> fr pj/plan :legend :entries first :color))
      default-pose
      (->
       {:team ["Ops" "Sales" "Support" "Research"],
        :before [62 48 71 55],
        :after [74 66 69 81]}
       (pj/lay-segment :before :team {:x-end :after})
       (pj/lay-point :before :team)
       (pj/lay-point :after :team))]
     (and
      (= 4 (:lines (pj/svg-summary v)))
      (= (before-color default-pose) (segment-color default-pose))
      (not= (before-color v) (segment-color v))
      (=
       ["before" "after"]
       (mapv :label (:entries (:legend (pj/plan v))))))))
   v56_l326)))
