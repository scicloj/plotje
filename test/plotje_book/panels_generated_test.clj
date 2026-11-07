(ns
 plotje-book.panels-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l27
 (def
  measurements
  {:height [1 2 3 4],
   :weight [1 2 3 4],
   :depth [1.6 2.3 3.4 4.7],
   :species ["a" "a" "b" "b"]}))


(def v4_l38 (-> measurements (pj/lay-point :height :weight)))


(deftest
 t5_l41
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v4_l38)))


(def
 v7_l46
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line :height :weight)))


(deftest
 t8_l50
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v7_l46)))


(def
 v10_l57
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t11_l61
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan v)
      [lo hi]
      (:x-domain (first (:panels plan)))]
     (and
      (= 1 (:panels s))
      (= "height, depth" (:x-label plan))
      (< lo 1)
      (> hi 4.7)
      (= ["height" "depth"] (mapv :label (:entries (:legend plan)))))))
   v10_l57)))


(def
 v13_l86
 (-> measurements (pj/lay-point :height :weight) pj/lay-smooth))


(deftest
 t14_l90
 (is
  ((fn
    [v]
    (=
     [:height :weight]
     ((juxt :x :y) (second (:layers (pj/draft v))))))
   v13_l86)))


(def
 v16_l98
 (-> measurements pj/lay-smooth (pj/lay-point :height :weight)))


(deftest
 t17_l102
 (is
  ((fn
    [v]
    (=
     [[:height :weight] [:height :weight]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v16_l98)))


(def
 v19_l109
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line {:y :depth})))


(deftest
 t20_l113
 (is
  ((fn
    [v]
    (and
     (=
      [:height :depth]
      ((juxt :x :y) (second (:layers (pj/draft v)))))
     (some #{"weight, depth"} (:texts (pj/svg-summary v)))))
   v19_l109)))


(def
 v22_l121
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-rug {:x :depth})))


(deftest
 t23_l125
 (is
  ((fn
    [v]
    (let
     [rug (second (:layers (pj/draft v)))]
     (and (= :depth (:x rug)) (nil? (:y rug)))))
   v22_l121)))


(def
 v25_l135
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t26_l140
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v25_l135)))


(def
 v28_l149
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/facet {:y [:weight :depth]})))


(deftest
 t29_l153
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= {:panels 2, :points 8} (select-keys s [:panels :points]))
      (=
       ["weight" "depth" "height"]
       (filterv #{"height" "depth" "weight"} (:texts s))))))
   v28_l149)))


(def
 v31_l165
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t32_l170
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v31_l165)))


(def
 v34_l183
 (->
  measurements
  (pj/facet
   {:col {:x [:height :weight :depth]},
    :row {:y [:height :weight :depth]}})
  pj/lay-point))


(deftest
 t35_l188
 (is
  ((fn
    [v]
    (and
     (=
      {:col {:x [:height :weight :depth]},
       :row {:y [:height :weight :depth]}}
      (:facet v))
     (= 9 (:panels (pj/svg-summary v)))
     (=
      [:bar :point :point :point :bar :point :point :point :bar]
      (mapv
       (fn*
        [p1__79039#]
        (:mark (first (:layers (first (:panels (:plan p1__79039#)))))))
       (:sub-plots (pj/plan v))))))
   v34_l183)))


(def
 v37_l199
 (-> measurements (pj/matrix [:height :weight :depth]) pj/lay-point))


(deftest
 t38_l203
 (is
  ((fn
    [v]
    (=
     v
     (->
      measurements
      (pj/facet
       {:col {:x [:height :weight :depth]},
        :row {:y [:height :weight :depth]}})
      pj/lay-point)))
   v37_l199)))


(def
 v40_l213
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t41_l217
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v)) [lo hi] (:x-domain (first panels))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (apply = (map :x-domain panels))
      (apply = (map :y-domain panels))
      (< lo 1)
      (> hi 4))))
   v40_l213)))


(def
 v43_l233
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t44_l236
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 2 (count (filter #{"weight"} (:texts s)))))))
   v43_l233)))


(def
 v46_l246
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point))


(deftest
 t47_l250
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v46_l246)))


(def
 v49_l271
 (->
  measurements
  (pj/pose {:x :height, :y :weight})
  (pj/arrange [{:where {:species "a"}} {:where {:species "b"}}])
  pj/lay-point))


(deftest
 t50_l277
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary
      (->
       measurements
       (pj/pose {:x :height, :y :weight})
       pj/lay-point
       (pj/facet :species)))))
   v49_l271)))


(def
 v52_l294
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:weight :depth]}})))


(deftest
 t53_l299
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v52_l294)))


(def
 v55_l304
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:y [:weight :depth]})
  (pj/facet :species)))


(deftest
 t56_l310
 (is
  ((fn
    [v]
    (let
     [base (-> measurements (pj/pose {:x :height}) pj/lay-point)]
     (and
      (= v (pj/facet base {:col :species, :row {:y [:weight :depth]}}))
      (=
       v
       (->
        base
        (pj/facet :species)
        (pj/facet {:y [:weight :depth]}))))))
   v55_l304)))


(def
 v58_l317
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t59_l322
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v58_l317)))
