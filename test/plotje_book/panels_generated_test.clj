(ns
 plotje-book.panels-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v2_l37
 (def
  measurements
  {:height [1 2 3 4],
   :weight [1 2 3 4],
   :depth [1.6 2.3 3.4 4.7],
   :species ["a" "a" "b" "b"]}))


(def v4_l48 (-> measurements (pj/lay-point :height :weight)))


(deftest
 t5_l51
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v4_l48)))


(def
 v7_l56
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line :height :weight)))


(deftest
 t8_l60
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v7_l56)))


(def
 v10_l67
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)))


(deftest
 t11_l71
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
   v10_l67)))


(def
 v13_l96
 (-> measurements (pj/lay-point :height :weight) pj/lay-smooth))


(deftest
 t14_l100
 (is
  ((fn
    [v]
    (=
     [:height :weight]
     ((juxt :x :y) (second (:layers (pj/draft v))))))
   v13_l96)))


(def
 v16_l108
 (-> measurements pj/lay-smooth (pj/lay-point :height :weight)))


(deftest
 t17_l112
 (is
  ((fn
    [v]
    (=
     [[:height :weight] [:height :weight]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v16_l108)))


(def
 v19_l119
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-line {:y :depth})))


(deftest
 t20_l123
 (is
  ((fn
    [v]
    (and
     (=
      [:height :depth]
      ((juxt :x :y) (second (:layers (pj/draft v)))))
     (some #{"weight, depth"} (:texts (pj/svg-summary v)))))
   v19_l119)))


(def
 v22_l131
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-rug {:x :depth})))


(deftest
 t23_l135
 (is
  ((fn
    [v]
    (let
     [rug (second (:layers (pj/draft v)))]
     (and (= :depth (:x rug)) (nil? (:y rug)))))
   v22_l131)))


(def
 v25_l145
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/lay-point :depth :weight)
  (pj/facet {:x [:height :depth]})))


(deftest
 t26_l150
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v25_l145)))


(def
 v28_l159
 (->
  measurements
  (pj/lay-point :height :weight)
  (pj/facet {:y [:weight :depth]})))


(deftest
 t29_l163
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
   v28_l159)))


(def
 v31_l175
 (->
  measurements
  (pj/pose {:y :weight})
  pj/lay-point
  (pj/facet {:x [:height :depth]})))


(deftest
 t32_l180
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v31_l175)))


(def
 v34_l195
 (->
  measurements
  (pj/facet
   {:col {:x [:height :weight :depth]},
    :row {:y [:height :weight :depth]}})
  pj/lay-point))


(deftest
 t35_l200
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
        [p1__78609#]
        (:mark (first (:layers (first (:panels (:plan p1__78609#)))))))
       (:sub-plots (pj/plan v))))))
   v34_l195)))


(def
 v37_l211
 (-> measurements (pj/matrix [:height :weight :depth]) pj/lay-point))


(deftest
 t38_l215
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
   v37_l211)))


(def
 v40_l225
 (-> measurements (pj/lay-point :height :weight) (pj/facet :species)))


(deftest
 t41_l229
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
   v40_l225)))


(def
 v43_l249
 (pj/arrange
  [(pj/lay-point measurements :height :weight)
   (pj/lay-point measurements :depth :weight)]))


(deftest
 t44_l252
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 2 (count (filter #{"weight"} (:texts s)))))))
   v43_l249)))


(def
 v46_l262
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point))


(deftest
 t47_l266
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 1 (count (filter #{"weight"} (:texts s)))))))
   v46_l262)))


(def
 v49_l282
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point
  (pj/lay-line :depth :weight)))


(deftest
 t50_l287
 (is
  ((fn
    [v]
    (and
     (= [:point :line] (mapv :layer-type (:layers v)))
     (=
      [[:point] [:point :line]]
      (mapv
       (fn*
        [p1__78610#]
        (mapv :mark (-> p1__78610# :plan :panels first :layers)))
       (-> v pj/plan :sub-plots)))
     (=
      {:panels 2, :points 8, :lines 1}
      (select-keys (pj/svg-summary v) [:panels :points :lines]))
     (= "" (with-out-str (pj/plan v)))))
   v49_l282)))


(def
 v52_l313
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point
  (pj/lay-histogram :height)))


(deftest
 t53_l318
 (is
  ((fn
    [v]
    (and
     (=
      [[:point :bar] [:point :bar]]
      (mapv
       (fn*
        [p1__78611#]
        (mapv :mark (-> p1__78611# :plan :panels first :layers)))
       (-> v pj/plan :sub-plots)))
     (=
      2
      (count
       (re-seq
        #"Note: the layers of this pose share one panel"
        (with-out-str (pj/plan v)))))))
   v52_l313)))


(def
 v55_l334
 (->
  (pj/arrange
   measurements
   [{:x :height, :y :weight} {:x :depth, :y :weight}])
  pj/lay-point
  (pj/arrange [:height])))


(deftest
 t56_l339
 (is
  ((fn
    [v]
    (and
     (=
      [[:point] [:point] [:bar]]
      (mapv
       (fn*
        [p1__78612#]
        (mapv :mark (-> p1__78612# :plan :panels first :layers)))
       (-> v pj/plan :sub-plots)))
     (=
      {:panels 3, :points 8, :polygons 3}
      (select-keys (pj/svg-summary v) [:panels :points :polygons]))))
   v55_l334)))


(def
 v58_l357
 (pj/arrange
  measurements
  [(->
    (pj/lay-point nil :height :weight)
    (pj/lay-smooth {:stat :linear-model}))
   {:x :depth, :y :weight}]))


(deftest
 t59_l361
 (is
  ((fn
    [v]
    (=
     {:panels 2, :points 8, :lines 1}
     (select-keys (pj/svg-summary v) [:panels :points :lines])))
   v58_l357)))


(def
 v61_l368
 (->
  (pj/arrange
   measurements
   [(pj/lay-point nil :height :weight) {:x :depth, :y :weight}])
  pj/lay-line))


(deftest
 t62_l372
 (is
  ((fn
    [v]
    (=
     [[:line :point] [:line]]
     (mapv
      (fn*
       [p1__78613#]
       (mapv :mark (-> p1__78613# :plan :panels first :layers)))
      (-> v pj/plan :sub-plots))))
   v61_l368)))


(def
 v64_l396
 (->
  measurements
  (pj/pose {:x :height, :y :weight})
  (pj/arrange [{:where {:species "a"}} {:where {:species "b"}}])
  pj/lay-point))


(deftest
 t65_l402
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
   v64_l396)))


(def
 v67_l412
 (->
  measurements
  (pj/pose {:x :height, :y :weight})
  (pj/arrange [{:where {:species "a"}} {:where {:species "b"}}])
  pj/lay-point
  (pj/lay-line :height :weight)))


(deftest
 t68_l419
 (is
  ((fn
    [v]
    (=
     {:panels 2, :lines 2}
     (select-keys (pj/svg-summary v) [:panels :lines])))
   v67_l412)))


(def
 v70_l434
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:weight :depth]}})))


(deftest
 t71_l439
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v70_l434)))


(def
 v73_l444
 (->
  measurements
  (pj/pose {:x :height})
  pj/lay-point
  (pj/facet {:y [:weight :depth]})
  (pj/facet :species)))


(deftest
 t74_l450
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
   v73_l444)))


(def
 v76_l461
 (->
  (pj/arrange
   [(pj/pose measurements :height :weight)
    (pj/pose measurements :depth :weight)])
  (pj/lay-point)
  (pj/facet :species)))


(deftest
 t77_l466
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v76_l461)))
