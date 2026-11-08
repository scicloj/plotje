(ns
 plotje-book.panels-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l36
 (def
  measurements
  {:height [1 2 3 4],
   :weight [1 2 3 4],
   :depth [1.6 2.3 3.4 4.7],
   :species ["a" "a" "b" "b"]}))


(def v4_l47 (-> measurements (pj/lay-point :height :weight)))


(deftest
 t5_l50
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v4_l47)))


(def
 v7_l55
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line :height :weight)))


(deftest
 t8_l59
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v7_l55)))


(def
 v10_l66
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t11_l70
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
   v10_l66)))


(def
 v13_l95
 (-> measurements (pj/lay-point :height :weight) pj/lay-smooth))


(deftest
 t14_l99
 (is
  ((fn
    [v]
    (=
     [:height :weight]
     ((juxt :x :y) (second (:layers (pj/draft v))))))
   v13_l95)))


(def
 v16_l107
 (-> measurements pj/lay-smooth (pj/lay-point :height :weight)))


(deftest
 t17_l111
 (is
  ((fn
    [v]
    (=
     [[:height :weight] [:height :weight]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v16_l107)))


(def
 v19_l118
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line {:y :depth})))


(deftest
 t20_l122
 (is
  ((fn
    [v]
    (and
     (=
      [:height :depth]
      ((juxt :x :y) (second (:layers (pj/draft v)))))
     (some #{"weight, depth"} (:texts (pj/svg-summary v)))))
   v19_l118)))


(def
 v22_l130
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-rug {:x :depth})))


(deftest
 t23_l134
 (is
  ((fn
    [v]
    (let
     [rug (second (:layers (pj/draft v)))]
     (and (= :depth (:x rug)) (nil? (:y rug)))))
   v22_l130)))


(def
 v25_l144
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t26_l149
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v25_l144)))


(def
 v28_l158
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/facet {:y [:weight :depth]})))


(deftest
 t29_l162
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
   v28_l158)))


(def
 v31_l174
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t32_l179
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v31_l174)))


(def
 v34_l192
 (->
  measurements
  (pj/facet
   {:col {:x [:height :weight :depth]},
    :row {:y [:height :weight :depth]}})
  pj/lay-point))


(deftest
 t35_l197
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
        [p1__86223#]
        (:mark (first (:layers (first (:panels (:plan p1__86223#)))))))
       (:sub-plots (pj/plan v))))))
   v34_l192)))


(def
 v37_l208
 (-> measurements (pj/matrix [:height :weight :depth]) pj/lay-point))


(deftest
 t38_l212
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
   v37_l208)))


(def
 v40_l222
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t41_l226
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
   v40_l222)))


(def
 v43_l246
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t44_l249
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 2 (count (filter #{"weight"} (:texts s)))))))
   v43_l246)))


(def
 v46_l259
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point))


(deftest
 t47_l263
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v46_l259)))


(def
 v49_l280
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point
  (pj/lay-line :depth :weight)
  (pj/lay-line :weight :height)))


(deftest
 t50_l286
 (is
  ((fn
    [v]
    (let
     [cells (:poses (first (:poses v)))]
     (and
      (= [:point :line] (mapv :layer-type (:layers v)))
      (=
       [[] [] [:line]]
       (mapv
        (fn* [p1__86224#] (mapv :layer-type (:layers p1__86224#)))
        cells))
      (=
       {:panels 3, :points 12, :lines 2}
       (select-keys (pj/svg-summary v) [:panels :points :lines])))))
   v49_l280)))


(def
 v52_l301
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point
  (pj/lay-histogram :height)))


(deftest
 t53_l306
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v52_l301)))


(def
 v55_l314
 (pj/arrange
  measurements
  [(->
    (pj/lay-point nil :height :weight)
    (pj/lay-smooth {:stat :linear-model}))
   {:x :depth, :y :weight}]))


(deftest
 t56_l318
 (is
  ((fn
    [v]
    (=
     {:panels 2, :points 8, :lines 1}
     (select-keys (pj/svg-summary v) [:panels :points :lines])))
   v55_l314)))


(def
 v58_l335
 (->
  measurements
  (pj/pose {:x :height, :y :weight})
  (pj/arrange [{:where {:species "a"}} {:where {:species "b"}}])
  pj/lay-point))


(deftest
 t59_l341
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
   v58_l335)))


(def
 v61_l359
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:weight :depth]}})))


(deftest
 t62_l364
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v61_l359)))


(def
 v64_l369
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:y [:weight :depth]})
  (pj/facet :species)))


(deftest
 t65_l375
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
   v64_l369)))


(def
 v67_l386
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t68_l391
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v67_l386)))
