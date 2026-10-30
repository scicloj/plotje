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
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (some #{"height, depth"} (:texts s)))))
   v10_l57)))


(def
 v13_l71
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/lay-line :height :weight {:color "black"})))


(deftest
 t14_l76
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= ["height" "depth"] (mapv :label (:entries (:legend plan))))
      (contains? (:colors (pj/svg-summary v)) "rgb(0,0,0)"))))
   v13_l71)))


(def
 v16_l85
 (->
  measurements
  (pj/pose {:color :species})
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t17_l90
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
   v16_l85)))


(def
 v19_l103
 (->
  measurements
  (pj/lay-point :height :weight {:color :species})
  (pj/lay-point :depth :weight)))


(deftest
 t20_l107
 (is
  ((fn
    [v]
    (= ["a" "b"] (mapv :label (:entries (:legend (pj/plan v))))))
   v19_l103)))


(def
 v22_l122
 (-> measurements (pj/lay-point :height :weight) pj/lay-smooth))


(deftest
 t23_l126
 (is
  ((fn
    [v]
    (=
     [:height :weight]
     ((juxt :x :y) (second (:layers (pj/draft v))))))
   v22_l122)))


(def
 v25_l134
 (-> measurements pj/lay-smooth (pj/lay-point :height :weight)))


(deftest
 t26_l138
 (is
  ((fn
    [v]
    (=
     [[:height :weight] [:height :weight]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v25_l134)))


(def
 v28_l145
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line {:y :depth})))


(deftest
 t29_l149
 (is
  ((fn
    [v]
    (and
     (=
      [:height :depth]
      ((juxt :x :y) (second (:layers (pj/draft v)))))
     (some #{"weight, depth"} (:texts (pj/svg-summary v)))))
   v28_l145)))


(def
 v31_l157
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-rug {:x :depth})))


(deftest
 t32_l161
 (is
  ((fn
    [v]
    (let
     [rug (second (:layers (pj/draft v)))]
     (and (= :depth (:x rug)) (nil? (:y rug)))))
   v31_l157)))


(def
 v34_l171
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t35_l176
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v34_l171)))


(def
 v37_l185
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/facet {:y [:weight :depth]})))


(deftest
 t38_l189
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
   v37_l185)))


(def
 v40_l201
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t41_l206
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v40_l201)))


(def
 v43_l215
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t44_l218
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 2 (count (filter #{"weight"} (:texts s)))))))
   v43_l215)))


(def
 v46_l228
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point))


(deftest
 t47_l232
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v46_l228)))


(def
 v49_l248
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t50_l252
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v49_l248)))


(def
 v52_l257
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :site ["p" "q" "r" "s"]}
  (pj/lay-point :height :weight)
  (pj/facet :site)))


(deftest
 t53_l261
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v52_l257)))


(def
 v55_l275
 (->
  measurements
  (pj/pose {:x :height, :y :weight})
  (pj/arrange [{:where {:species "a"}} {:where {:species "b"}}])
  pj/lay-point))


(deftest
 t56_l281
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
   v55_l275)))


(def
 v58_l299
 (->
  measurements
  (pj/facet
   {:col {:x [:height :weight :depth]},
    :row {:y [:height :weight :depth]}})
  pj/lay-point))


(deftest
 t59_l304
 (is
  ((fn
    [v]
    (and
     (=
      {:col {:x [:height :weight :depth]},
       :row {:y [:height :weight :depth]}}
      (:facet v))
     (= 9 (:panels (pj/svg-summary v)))))
   v58_l299)))


(def
 v61_l315
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet :species)))


(deftest
 t62_l320
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v61_l315)))


(def
 v64_l326
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:weight :depth]}})))


(deftest
 t65_l331
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v64_l326)))


(def
 v67_l336
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:y [:weight :depth]})
  (pj/facet :species)))


(deftest
 t68_l342
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
   v67_l336)))


(def
 v70_l349
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t71_l354
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v70_l349)))
