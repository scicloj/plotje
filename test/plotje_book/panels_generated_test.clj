(ns
 plotje-book.panels-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l25
 (def
  measurements
  {:height [1 2 3 4],
   :weight [1 2 3 4],
   :depth [2 3 4 5],
   :species ["a" "a" "b" "b"]}))


(def v4_l36 (-> measurements (pj/lay-point :height :weight)))


(deftest
 t5_l39
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v4_l36)))


(def
 v7_l44
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line :height :weight)))


(deftest
 t8_l48
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v7_l44)))


(def
 v10_l55
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t11_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (some #{"height, depth"} (:texts s)))))
   v10_l55)))


(def
 v13_l69
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/lay-line :height :weight {:color "black"})))


(deftest
 t14_l74
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= ["height" "depth"] (mapv :label (:entries (:legend plan))))
      (contains? (:colors (pj/svg-summary v)) "rgb(0,0,0)"))))
   v13_l69)))


(def
 v16_l83
 (->
  measurements
  (pj/pose {:color :species})
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t17_l88
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (some #{"height, depth"} (:texts s))
      (some #{"species"} (:texts s))
      (not-any? #{"height" "depth"} (:texts s)))))
   v16_l83)))


(def
 v19_l101
 (->
  measurements
  (pj/lay-point :height :weight {:color :species})
  (pj/lay-point :depth :weight)))


(deftest
 t20_l105
 (is
  ((fn
    [v]
    (= ["a" "b"] (mapv :label (:entries (:legend (pj/plan v))))))
   v19_l101)))


(def
 v22_l120
 (-> measurements (pj/lay-point :height :weight) pj/lay-smooth))


(deftest
 t23_l124
 (is
  ((fn
    [v]
    (=
     [:height :weight]
     ((juxt :x :y) (second (:layers (pj/draft v))))))
   v22_l120)))


(def
 v25_l131
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line {:y :depth})))


(deftest
 t26_l135
 (is
  ((fn
    [v]
    (and
     (=
      [:height :depth]
      ((juxt :x :y) (second (:layers (pj/draft v)))))
     (some #{"weight, depth"} (:texts (pj/svg-summary v)))))
   v25_l131)))


(def
 v28_l143
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-rug {:x :depth})))


(deftest
 t29_l147
 (is
  ((fn
    [v]
    (let
     [rug (second (:layers (pj/draft v)))]
     (and (= :depth (:x rug)) (nil? (:y rug)))))
   v28_l143)))


(def
 v31_l157
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t32_l162
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v31_l157)))


(def
 v34_l171
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/facet {:y [:weight :depth]})))


(deftest
 t35_l175
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
   v34_l171)))


(def
 v37_l187
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t38_l192
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v37_l187)))


(def
 v40_l201
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t41_l204
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v40_l201)))


(def
 v43_l215
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t44_l219
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v43_l215)))


(def
 v46_l224
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t47_l228
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v46_l224)))


(def
 v49_l240
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet :species)))


(deftest
 t50_l245
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v49_l240)))


(def
 v52_l250
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:col :species, :row {:x [:height :depth]}})))


(deftest
 t53_l255
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v52_l250)))


(def
 v55_l260
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t56_l265
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v55_l260)))
