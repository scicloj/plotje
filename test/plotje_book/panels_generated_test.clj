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
   :depth [2 3 4 5],
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
      (> hi 5)
      (= ["height" "depth"] (mapv :label (:entries (:legend plan)))))))
   v10_l57)))


(def
 v13_l83
 (-> measurements (pj/lay-point :height :weight) pj/lay-smooth))


(deftest
 t14_l87
 (is
  ((fn
    [v]
    (=
     [:height :weight]
     ((juxt :x :y) (second (:layers (pj/draft v))))))
   v13_l83)))


(def
 v16_l95
 (-> measurements pj/lay-smooth (pj/lay-point :height :weight)))


(deftest
 t17_l99
 (is
  ((fn
    [v]
    (=
     [[:height :weight] [:height :weight]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v16_l95)))


(def
 v19_l106
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line {:y :depth})))


(deftest
 t20_l110
 (is
  ((fn
    [v]
    (and
     (=
      [:height :depth]
      ((juxt :x :y) (second (:layers (pj/draft v)))))
     (some #{"weight, depth"} (:texts (pj/svg-summary v)))))
   v19_l106)))


(def
 v22_l118
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-rug {:x :depth})))


(deftest
 t23_l122
 (is
  ((fn
    [v]
    (let
     [rug (second (:layers (pj/draft v)))]
     (and (= :depth (:x rug)) (nil? (:y rug)))))
   v22_l118)))


(def
 v25_l132
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t26_l137
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v25_l132)))


(def
 v28_l146
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/facet {:y [:weight :depth]})))


(deftest
 t29_l150
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
   v28_l146)))


(def
 v31_l162
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t32_l167
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v31_l162)))


(def
 v34_l180
 (->
  measurements
  (pj/facet
   {:col {:x [:height :weight :depth]},
    :row {:y [:height :weight :depth]}})
  pj/lay-point))


(deftest
 t35_l185
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
        [p1__11199#]
        (:mark (first (:layers (first (:panels (:plan p1__11199#)))))))
       (:sub-plots (pj/plan v))))))
   v34_l180)))


(def
 v37_l196
 (-> measurements (pj/matrix [:height :weight :depth]) pj/lay-point))


(deftest
 t38_l200
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
   v37_l196)))


(def
 v40_l210
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t41_l214
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
   v40_l210)))


(def
 v43_l230
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t44_l233
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 2 (count (filter #{"weight"} (:texts s)))))))
   v43_l230)))


(def
 v46_l243
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point))


(deftest
 t47_l247
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v46_l243)))


(def
 v49_l267
 (->
  measurements
  (pj/pose {:x :height, :y :weight})
  (pj/arrange [{:where {:species "a"}} {:where {:species "b"}}])
  pj/lay-point))


(deftest
 t50_l273
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
   v49_l267)))


(def
 v52_l290
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:weight :depth]}})))


(deftest
 t53_l295
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v52_l290)))


(def
 v55_l300
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:y [:weight :depth]})
  (pj/facet :species)))


(deftest
 t56_l306
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
   v55_l300)))


(def
 v58_l313
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t59_l318
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v58_l313)))
