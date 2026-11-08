(ns
 plotje-book.pose-rules-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l28 (def iris (rdatasets/datasets-iris)))


(def
 v5_l34
 (defn
  strip-data
  [pose]
  (cond->
   (dissoc pose :data)
   (:layers pose)
   (update
    :layers
    (partial mapv (fn* [p1__83585#] (dissoc p1__83585# :data))))
   (:poses pose)
   (update :poses (partial mapv strip-data)))))


(def
 v6_l39
 (defn
  pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose))))


(def
 v8_l47
 (defn
  cells
  [pose]
  (filterv
   (fn* [p1__83586#] (not (contains? p1__83586# :poses)))
   (rest (tree-seq :poses :poses pose)))))


(def v10_l92 (-> iris (pj/pose :sepal-length :sepal-width)))


(deftest
 t11_l95
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v10_l92)))


(def
 v12_l97
 (-> iris (pj/pose :sepal-length :sepal-width) pose-summary))


(deftest
 t13_l101
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (= [] (:layers pose))
     (not (contains? pose :poses))))
   v12_l97)))


(def v15_l113 (-> iris (pj/pose {:color :species}) pose-summary))


(deftest
 t16_l117
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (not (contains? pose :poses))))
   v15_l113)))


(def v18_l128 (-> iris pj/pose (pj/pose :sepal-length :sepal-width)))


(deftest
 t19_l132
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (not (contains? pose :poses))))
   v18_l128)))


(def
 v21_l139
 (->
  iris
  (pj/pose {:color :species})
  (pj/pose :sepal-length :sepal-width)))


(deftest
 t22_l143
 (is
  ((fn
    [pose]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping pose)))
   v21_l139)))


(def
 v24_l151
 (=
  (->
   iris
   pj/pose
   (pj/pose {:color :species})
   (pj/pose :sepal-length :sepal-width))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t25_l157 (is (true? v24_l151)))


(def
 v27_l167
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v28_l173
 (-> (trend-plot iris) (pj/pose :petal-length :petal-width)))


(def
 v29_l176
 (->
  (trend-plot iris)
  (pj/pose :petal-length :petal-width)
  pose-summary))


(deftest
 t30_l180
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping pose))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers pose))))
   v29_l176)))


(def
 v32_l190
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)))


(deftest
 t33_l194
 (is
  ((fn
    [pose]
    (let
     [printed (with-out-str (pj/plan pose))]
     (and
      (= {:x :petal-length, :y :petal-width} (:mapping pose))
      (=
       [{:x :sepal-length, :y :sepal-width}]
       (mapv :mapping (:layers pose)))
      (re-find
       #"a pose maps :x to :petal-length, and every layer drawn on it names its own :x"
       printed)
      (re-find #"a pose maps :y to :petal-width" printed))))
   v32_l190)))


(def
 v35_l205
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t36_l209
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v35_l205)))


(def
 v38_l215
 (=
  (trend-plot iris)
  (pj/pose (trend-plot iris) :sepal-length :sepal-width)))


(deftest t39_l218 (is (true? v38_l215)))


(def
 v41_l226
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})))


(def
 v42_l230
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t43_l235
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v42_l230)))


(def
 v45_l244
 (=
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/pose {:color :species}))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t46_l249 (is (true? v45_l244)))


(def
 v48_l257
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})))


(def
 v49_l262
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t50_l268
 (is
  ((fn
    [pose]
    (and
     (= [{:layer-type :point}] (:layers pose))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v49_l262)))


(def
 v52_l280
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/pose {:color :species})
  pj/lay-point))


(def
 v53_l285
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/pose {:color :species})
  pj/lay-point
  pose-summary))


(deftest
 t54_l291
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}]
      (mapv :mapping (cells pose)))))
   v53_l285)))


(def
 v56_l303
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/pose :sepal-length :petal-length)
  pj/lay-point))


(deftest
 t57_l308
 (is
  ((fn
    [pose]
    (= 2 (count (re-seq #"not drawn" (with-out-str (pj/plan pose))))))
   v56_l303)))


(def
 v59_l321
 (def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width))))


(def v60_l323 leaf-pose)


(def v61_l325 (pose-summary leaf-pose))


(def v63_l329 (= leaf-pose (pj/pose leaf-pose) (pj/pose leaf-pose {})))


(deftest t64_l333 (is (true? v63_l329)))


(def
 v65_l335
 (=
  (pj/pose leaf-pose {:color :species})
  (pj/pose (pj/pose leaf-pose {:color :species}) {:color :species})))


(deftest t66_l338 (is (true? v65_l335)))


(def
 v68_l342
 (def
  composite-pose
  (->
   iris
   (pj/arrange
    [{:x :sepal-length, :y :sepal-width}
     {:x :petal-length, :y :petal-width}])
   pj/lay-point)))


(def v69_l347 composite-pose)


(def v70_l349 (pose-summary composite-pose))


(def
 v72_l353
 (=
  composite-pose
  (pj/pose composite-pose)
  (pj/pose composite-pose {})))


(deftest t73_l357 (is (true? v72_l353)))


(def
 v75_l374
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t76_l382
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= :vertical (get-in pose [:layout :direction]))
     (= 1 (count (:poses pose)))
     (= 2 (count (:poses (first (:poses pose)))))
     (let
      [p (pj/lay-point nil :sepal-length :sepal-width)]
      (=
       (pj/svg-summary
        (pj/arrange iris [p {:x :petal-length, :y :petal-width}]))
       (pj/svg-summary
        (pj/arrange
         iris
         [(pj/pose nil p)
          (pj/pose nil {:x :petal-length, :y :petal-width})]))))))
   v75_l374)))


(def
 v78_l401
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t79_l406
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v78_l401)))


(def
 v81_l416
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-histogram))


(deftest
 t82_l420
 (is
  ((fn
    [pose]
    (and
     (= 4 (:panels (pj/svg-summary pose)))
     (=
      [2 2]
      (mapv
       (fn* [p1__83587#] (count (:poses p1__83587#)))
       (:poses pose)))
     (=
      [{:x :sepal-length} {:x :sepal-width}]
      (mapv :mapping (:poses (first (:poses pose)))))))
   v81_l416)))


(def
 v84_l435
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :sepal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t85_l441
 (is
  ((fn
    [pose]
    (let
     [one-per-row
      (pj/arrange
       (vec (mapcat :poses (:poses pose)))
       {:cols 1, :x-label "X"})]
     (and
      (= "Arranged" (get-in pose [:opts :title]))
      (= #{:y} (get-in pose [:opts :share-scales]))
      (= "X" (get-in one-per-row [:opts :x-label]))
      (not (contains? (:opts one-per-row) :cols))
      (= 1 (get-in one-per-row [:layout :cols]))
      (= :auto (get-in pose [:layout :cols]))
      (=
       [1 1]
       (mapv
        (fn* [p1__83588#] (count (:poses p1__83588#)))
        (:poses one-per-row))))))
   v84_l435)))


(def
 v87_l463
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:sepal-length :sepal-width]}})))


(deftest
 t88_l468
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (=
      {:col {:x [:sepal-length :sepal-width]},
       :row {:y [:sepal-length :sepal-width]}}
      (:facet pose))
     (nil? (:poses pose))
     (= 4 (:panels (pj/svg-summary pose)))))
   v87_l463)))


(def
 v90_l481
 (let
  [a
   (->
    iris
    (pj/pose {:color :species})
    (pj/facet
     {:col {:x [:sepal-length :sepal-width]},
      :row {:y [:sepal-length :sepal-width]}}))
   b
   (->
    iris
    (pj/facet
     {:col {:x [:sepal-length :sepal-width]},
      :row {:y [:sepal-length :sepal-width]}})
    (pj/pose {:color :species}))]
  (= a b)))


(deftest t91_l491 (is (true? v90_l481)))


(def
 v93_l519
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t94_l523
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v93_l519)))


(def
 v96_l532
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v97_l537
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t98_l543
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v97_l537)))


(def
 v100_l554
 (let
  [before
   (pj/arrange
    [(pj/pose iris :sepal-length :sepal-width)
     (pj/pose iris :petal-length :petal-width)])
   after
   (->
    (pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    pj/lay-point)]
  [(count (or (:layers before) [])) (count (or (:layers after) []))]))


(deftest
 t101_l564
 (is
  ((fn
    [counts]
    (let
     [marks
      (fn
       [pose]
       (mapv
        (fn*
         [p1__83589#]
         (mapv :mark (-> p1__83589# :plan :panels first :layers)))
        (:sub-plots (pj/plan pose))))]
     (and
      (= [0 1] counts)
      (=
       [[:point :line] [:point]]
       (marks
        (->
         (pj/arrange
          [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-smooth)
           (pj/pose iris :petal-length :petal-width)])
         pj/lay-point)))
      (=
       [[:line] [:line]]
       (marks
        (->
         (pj/arrange
          [(pj/pose iris :sepal-length :sepal-width)
           (pj/pose iris :petal-length :petal-width)])
         pj/lay-smooth)))
      (=
       [[:bar] [:point] [:point] [:bar]]
       (marks
        (->
         iris
         (pj/facet
          {:col {:x [:sepal-length :sepal-width]},
           :row {:y [:sepal-length :sepal-width]}})
         pj/lay-point)))
      (=
       [[:bar] [:bar]]
       (marks
        (->
         iris
         (pj/arrange
          [{:x :sepal-length, :y :sepal-width}
           {:x :petal-length, :y :petal-width}])
         pj/lay-histogram)))
      (=
       [[:point] [:bar]]
       (marks
        (->
         iris
         (pj/arrange
          [{:x :sepal-length, :y :sepal-width} :petal-length])
         pj/lay-histogram))))))
   v100_l554)))


(def
 v103_l638
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width)))


(deftest
 t104_l643
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= [:point :smooth] (mapv :layer-type (:layers pose)))
     (every?
      (fn* [p1__83590#] (empty? (:layers p1__83590#)))
      (cells pose))
     (=
      [[:point :line] [:point]]
      (mapv
       (fn*
        [p1__83591#]
        (mapv :mark (-> p1__83591# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))))
   v103_l638)))


(def
 v106_l660
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/arrange
   [{:where {:species "setosa"}}
    {:where {:species "versicolor"}}
    {:where {:species "virginica"}}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width)))


(deftest
 t107_l668
 (is
  ((fn
    [pose]
    (and
     (=
      {:panels 3, :points 150, :lines 3}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (=
      (pj/plot pose)
      (pj/plot
       (->
        iris
        (pj/pose :sepal-length :sepal-width)
        pj/lay-point
        (pj/lay-smooth :sepal-length :sepal-width)
        (pj/facet :species))))))
   v106_l660)))


(def
 v109_l685
 (->
  iris
  (pj/pose {:x :petal-length})
  (pj/facet {:y [:sepal-length :sepal-width]})
  (pj/lay-smooth {:y :sepal-length})))


(deftest
 t110_l690
 (is
  ((fn
    [pose]
    (and
     (=
      {:panels 2, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :lines]))
     (= "" (with-out-str (pj/plan pose)))))
   v109_l685)))


(def
 v112_l700
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :petal-length :petal-width)))


(deftest
 t113_l704
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [[:point] [:line :point]]
      (mapv
       (fn*
        [p1__83592#]
        (mapv :mark (-> p1__83592# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v112_l700)))


(def
 v115_l724
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth {:y :sepal-width})))


(deftest
 t116_l730
 (is
  ((fn
    [pose]
    (and
     (=
      [[:point :line] [:point]]
      (mapv
       (fn*
        [p1__83593#]
        (mapv :mark (-> p1__83593# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (= "" (with-out-str (pj/plan pose)))))
   v115_l724)))


(def
 v118_l756
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point {:color "#377eb8"})
  (pj/lay-point :sepal-length :petal-length {:color "#999999"})))


(deftest
 t119_l761
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [{:color "#377eb8"}
       {:x :sepal-length, :y :petal-length, :color "#999999"}]
      (mapv :mapping (:layers pose)))
     (=
      [[:point :point] [:point :point]]
      (mapv
       (fn*
        [p1__83594#]
        (mapv :mark (-> p1__83594# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))
     (=
      {:panels 2, :points 600}
      (select-keys (pj/svg-summary pose) [:panels :points]))))
   v118_l756)))


(def
 v121_l782
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-histogram :sepal-length)))


(deftest
 t122_l787
 (is
  ((fn
    [pose]
    (and
     (=
      [[:point :bar] [:point :bar]]
      (mapv
       (fn*
        [p1__83595#]
        (mapv :mark (-> p1__83595# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))
     (=
      2
      (count
       (re-seq
        #"Note: the layers of this pose share one panel"
        (with-out-str (pj/plan pose)))))))
   v121_l782)))


(def
 v124_l806
 (try
  (pj/plan
   (->
    iris
    (pj/arrange
     [{:x :sepal-length, :y :sepal-width}
      {:x :petal-length, :y :petal-width}])
    (pj/lay-point :sepal-length :petal-length)))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t125_l813
 (is
  ((fn
    [msg]
    (and
     (re-find
      #"pj/lay-point names :petal-length on :y, and no panel draws it"
      msg)
     (re-find
      #"no panel draws it"
      (try
       (pj/plan
        (->
         (pj/arrange
          iris
          [(pj/lay-point nil :sepal-length :sepal-width)
           {:x :petal-length, :y :petal-width}])
         (pj/lay-point :sepal-length :petal-length)))
       ""
       (catch clojure.lang.ExceptionInfo e (ex-message e))))))
   v124_l806)))


(def
 v127_l829
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :sepal-length :petal-length)))


(deftest
 t128_l833
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      {:panels 2, :points 300, :lines 2}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (not (some #{"no data"} (:texts (pj/svg-summary pose))))))
   v127_l829)))


(def
 v130_l845
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/arrange [(pj/lay-smooth iris :sepal-length :petal-length)])))


(deftest
 t131_l850
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      [3]
      (mapv
       (fn* [p1__83596#] (count (:poses p1__83596#)))
       (:poses pose)))
     (=
      [[:point] [:point] [:point :line]]
      (mapv
       (fn*
        [p1__83597#]
        (mapv :mark (-> p1__83597# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))))
   v130_l845)))


(def
 v133_l870
 (let
  [via-facet
   (->
    iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width)
    (pj/facet
     {:x [:sepal-length :petal-length],
      :y [:sepal-width :petal-width]}))
   via-arrange
   (->
    iris
    (pj/arrange
     [{:x :sepal-length, :y :sepal-width}
      {:x :petal-length, :y :petal-width}])
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))
   drawn
   (fn [pose] (select-keys (pj/svg-summary pose) [:panels :points]))]
  {:via-facet-drawn (drawn via-facet),
   :via-arrange-drawn (drawn via-arrange),
   :via-facet-shape (if (:poses via-facet) :composite :leaf),
   :via-arrange-shape (if (:poses via-arrange) :composite :leaf)}))


(deftest
 t134_l885
 (is
  ((fn
    [m]
    (and
     (= (:via-facet-drawn m) (:via-arrange-drawn m))
     (= :leaf (:via-facet-shape m))
     (= :composite (:via-arrange-shape m))))
   v133_l870)))


(def v136_l898 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v137_l902 (-> tiny (pj/lay-point :a :b)))


(deftest
 t138_l905
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v137_l902)))


(def v140_l912 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t141_l917
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v140_l912)))


(def v143_l928 (pj/lay-point nil :a :b))


(deftest
 t144_l930
 (is
  ((fn
    [pose]
    (and
     (nil? (:data pose))
     (= [{:x :a, :y :b}] (mapv :mapping (:layers pose)))
     (= 5 (:points (pj/svg-summary (pj/with-data pose tiny))))))
   v143_l928)))


(def
 v146_l948
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t147_l953
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      axis-titles
      (filter
       #{"sepal length" "sepal width" "petal width" "petal length"}
       (:texts s))]
     (and
      (= 1 (:panels s))
      (= 300 (:points s))
      (contains? (:colors s) "rgb(55,126,184)")
      (contains? (:colors s) "rgb(230,85,13)")
      (empty? axis-titles)
      (some #{"sepal length, petal length"} (:texts s))
      (some #{"sepal width, petal width"} (:texts s)))))
   v146_l948)))


(def
 v149_l975
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t150_l981
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (=
      [{:color "#377eb8", :x :sepal-length, :y :sepal-width}
       {:color "#e6550d", :x :petal-length, :y :petal-width}]
      (mapv :mapping (:layers pose)))))
   v149_l975)))


(def
 v152_l992
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t153_l998
 (is
  ((fn
    [v]
    (=
     {:panels 2, :points 600}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v152_l992)))


(def
 v155_l1010
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t156_l1014
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= {:x :sepal-length, :y :sepal-width} (:mapping fr))
     (=
      [{:x :petal-length, :y :petal-width}]
      (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 150}
      (select-keys (pj/svg-summary fr) [:panels :points]))
     (re-find
      #"a pose maps :x to :sepal-length, and every layer drawn on it names its own :x, so :sepal-length is not drawn"
      (with-out-str (pj/plan fr)))))
   v155_l1010)))


(def
 v158_l1036
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t159_l1041
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 1 (:panels (pj/svg-summary pose)))
     (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))
   v158_l1036)))


(def
 v161_l1052
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t162_l1056
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v161_l1052)))


(def
 v164_l1066
 (try
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t165_l1073
 (is
  ((fn
    [msg]
    (and
     (re-find #":overlay is retired" msg)
     (re-find #"pj/facet" msg)
     (re-find #"pj/arrange" msg)
     (let
      [m
       (try
        (pj/overlay
         (pj/lay-point iris :sepal-length :sepal-width)
         false)
        nil
        (catch clojure.lang.ExceptionInfo e (ex-message e)))]
      (and m (re-find #"pj/facet" m) (re-find #"pj/arrange" m)))))
   v164_l1066)))


(def
 v167_l1094
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-line {:y :petal-width})
  pj/lay-smooth))


(deftest
 t168_l1099
 (is
  ((fn
    [v]
    (=
     [[:sepal-length :sepal-width]
      [:sepal-length :petal-width]
      [:sepal-length :sepal-width]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v167_l1094)))


(def
 v170_l1109
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:x :petal-length})))


(deftest
 t171_l1113
 (is
  ((fn
    [v]
    (=
     {:mark :rug, :x :petal-length}
     (-> v pj/draft :layers second (select-keys [:mark :x :y]))))
   v170_l1109)))


(def
 v173_l1136
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t174_l1143
 (is
  ((fn
    [msg]
    (let
     [in-mapping
      (->
       iris
       (pj/pose :sepal-length :sepal-width)
       (pj/lay-point {:y :nope}))]
     (and
      (string? msg)
      (re-find #"doesn't exist in the data" msg)
      (pj/pose? in-mapping)
      (re-find
       #"Column :nope \(from :y\) not found"
       (try
        (pj/plan in-mapping)
        ""
        (catch Exception e (ex-message e)))))))
   v173_l1136)))


(def
 v176_l1160
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t177_l1165
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 3}
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v176_l1160)))


(def
 v179_l1193
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t180_l1196
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v179_l1193)))


(def
 v182_l1203
 (->
  {:a [1 2], :b [3 4], :c [5 6], :d [7 8]}
  pj/lay-point
  (pj/pose {:x :a, :y :b})))


(deftest
 t183_l1207
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v182_l1203)))


(def
 v185_l1213
 (try
  (-> {:a [1 2], :b [3 4], :c [5 6], :d [7 8]} pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t186_l1220
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns: \[:a :b :c :d\]" msg)))
   v185_l1213)))


(def
 v188_l1249
 (def
  s1-composite
  (pj/pose
   {:mapping {:color :species},
    :poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v189_l1258 s1-composite)


(deftest
 t190_l1260
 (is
  ((fn
    [pose]
    (let
     [plan
      (pj/plan pose)
      panels
      (mapv (comp :panels :plan) (:sub-plots plan))]
     (every?
      (fn [pp] (= 3 (count (:groups (first (:layers (first pp)))))))
      panels)))
   v189_l1258)))


(def
 v192_l1272
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v193_l1280 s1-siblings)


(deftest
 t194_l1282
 (is
  ((fn
    [pose]
    (let
     [sub-plots
      (:sub-plots (pj/plan pose))
      panel-groups
      (mapv
       (fn
        [sp]
        (count
         (:groups (first (:layers (first (-> sp :plan :panels)))))))
       sub-plots)]
     (= [1 3] panel-groups)))
   v193_l1280)))


(def
 v196_l1302
 (def
  s2-tree
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :a, :y :b},
      :layers [{:layer-type :point}],
      :data (tc/dataset {:a [1 2 3], :b [3 5 4]})}],
    :data iris})))


(def v197_l1311 s2-tree)


(deftest
 t198_l1313
 (is
  ((fn
    [pose]
    (let
     [sub-plots
      (:sub-plots (pj/plan pose))
      counts
      (mapv
       (fn
        [sp]
        (->
         sp
         :plan
         :panels
         first
         :layers
         first
         :groups
         first
         :xs
         count))
       sub-plots)]
     (= [150 3] counts)))
   v197_l1311)))


(def
 v200_l1338
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t201_l1343
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v200_l1338)))


(def
 v203_l1357
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t204_l1362
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v203_l1357)))


(def
 v206_l1377
 (->
  iris
  (pj/lay-line :sepal-length [:sepal-width :petal-width])
  pj/lay-point))


(deftest
 t207_l1381
 (is
  ((fn
    [v]
    (and
     (= 300 (:points (pj/svg-summary v)))
     (=
      150
      (->
       iris
       (pj/lay-line :sepal-length :sepal-width)
       pj/lay-point
       pj/svg-summary
       :points))))
   v206_l1377)))


(def
 v209_l1408
 (->
  iris
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :where {:species "setosa"}})
  (pj/arrange
   [{:where {:species "versicolor"}} {:where {:petal-width 0.2}}])
  (pj/lay-point {:color :species})))


(deftest
 t210_l1414
 (is
  ((fn
    [v]
    (let
     [cell-points
      (fn
       [w]
       (->
        iris
        (pj/pose :sepal-length :sepal-width {:where w})
        pj/lay-point
        pj/svg-summary
        :points))]
     (=
      (:points (pj/svg-summary v))
      (+
       (cell-points {:species "versicolor"})
       (cell-points {:species "setosa", :petal-width 0.2})))))
   v209_l1408)))


(def
 v212_l1428
 (->
  iris
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :where {:species "setosa"}})
  (pj/pose {:where {:species "versicolor"}})
  pj/lay-point))


(deftest
 t213_l1433
 (is ((fn [v] (= {:species "versicolor"} (:where v))) v212_l1428)))


(def
 v215_l1446
 (->
  iris
  (pj/pose {})
  (pj/scale :x :log)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t216_l1452
 (is
  ((fn [v] (= {:type :log} (-> v pj/plan :panels first :x-scale)))
   v215_l1446)))


(def
 v218_l1475
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t219_l1480
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v218_l1475)))


(def
 v221_l1485
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t222_l1491
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v221_l1485)))


(def
 v224_l1510
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t225_l1516
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v224_l1510)))


(def
 v227_l1526
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t228_l1531
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v227_l1526)))


(def
 v230_l1539
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t231_l1544
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v230_l1539)))


(def
 v233_l1566
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t234_l1571
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v233_l1566)))


(def
 v236_l1578
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip)
  pj/lay-point
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t237_l1583
 (is
  ((fn
    [pose]
    (and
     (= :smoker (get-in pose [:mapping :col]))
     (= :sex (get-in pose [:mapping :row]))
     (= 4 (:panels (pj/svg-summary pose)))))
   v236_l1578)))


(def
 v239_l1593
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})
  (select-keys [:mapping :facet])
  kind/pprint))


(deftest
 t240_l1600
 (is
  ((fn
    [m]
    (=
     {:mapping {:x :sepal-length, :col :species},
      :facet {:y [:sepal-width :petal-width]}}
     m))
   v239_l1593)))


(def
 v242_l1610
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t243_l1615
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v242_l1610)))


(def
 v244_l1618
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t245_l1623
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v244_l1618)))


(def
 v247_l1633
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})
  pose-summary))


(deftest
 t248_l1639
 (is
  ((fn
    [pose]
    (and
     (= {:y [:sepal-width :petal-width]} (:facet pose))
     (nil? (get-in pose [:mapping :col]))
     (= 2 (:panels (pj/svg-summary pose)))))
   v247_l1633)))


(def
 v250_l1659
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t251_l1664
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__83598#]
        (when (= :rule-h (:layer-type p1__83598#)) p1__83598#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v250_l1659)))


(def
 v253_l1674
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t254_l1678
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= [:rule-h] (mapv :layer-type (:layers pose)))
     (=
      [[:rule-h] [:point]]
      (mapv
       (fn*
        [p1__83599#]
        (mapv :mark (-> p1__83599# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))))
   v253_l1674)))


(def
 v256_l1696
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-rule-h :petal-length :petal-width {:y-intercept 3.0})))


(deftest
 t257_l1701
 (is
  ((fn
    [pose]
    (let
     [y-domain
      (fn*
       [p1__83600#]
       (-> p1__83600# pj/plan :panels first :y-domain))
      points
      (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)]
     (and
      (= 1 (:panels (pj/svg-summary pose)))
      (= (y-domain points) (y-domain pose))
      (re-find
       #"pj/lay-rule-h names :petal-length on :x and :petal-width on :y, columns the panel does not draw, so they are not read"
       (with-out-str (pj/plan pose))))))
   v256_l1696)))


(def
 v259_l1724
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:theme {:bg "#FFFFFF"}})
  (pj/options {:theme {:grid "#DDDDDD"}})))


(deftest
 t260_l1730
 (is
  ((fn
    [pose]
    (= {:bg "#FFFFFF", :grid "#DDDDDD"} (get-in pose [:opts :theme])))
   v259_l1724)))


(def
 v262_l1735
 (->
  iris
  (pj/arrange
   [(->
     (pj/pose nil {:x :sepal-length, :y :sepal-width})
     (pj/options {:theme {:grid "#DDDDDD"}}))
    {:x :petal-length, :y :petal-width}])
  (pj/options {:theme {:bg "#FFFFFF"}})
  pj/lay-point))


(deftest
 t263_l1742
 (is
  ((fn [v] (not (re-find #"rgb\(232,232,232\)" (str (pj/plot v)))))
   v262_l1735)))


(def
 v265_l1765
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t266_l1774
 (is
  ((fn
    [pose]
    (let
     [plan
      (pj/plan pose)
      panel-layer-counts
      (mapv
       (fn [sp] (count (:layers (first (-> sp :plan :panels)))))
       (:sub-plots plan))]
     (= [2 1] panel-layer-counts)))
   v265_l1765)))


(def
 v268_l1793
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t269_l1797
 (is
  ((fn
    [_]
    (let
     [draft
      (->
       iris
       (pj/pose :sepal-length :sepal-width {:color :species})
       pj/lay-point
       pj/draft)
      layers
      (:layers draft)]
     (and
      (= 1 (count layers))
      (let
       [d (first layers)]
       (and
        (= :sepal-length (:x d))
        (= :sepal-width (:y d))
        (= :species (:color d))
        (= :point (:mark d))
        (= 150 (tc/row-count (:data d))))))))
   v268_l1793)))


(def
 v271_l1830
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t272_l1834
 (is
  ((fn
    [pose]
    (let
     [plan
      (pj/plan pose)
      by-columns
      (pj/plan
       (->
        iris
        (pj/pose {:x :sepal-length})
        pj/lay-point
        (pj/facet {:y [:sepal-width :petal-width]})))]
     (and
      (:composite? plan)
      (= 2 (count (:sub-plots plan)))
      (:composite? by-columns)
      (= 2 (count (:sub-plots by-columns)))
      (empty? (:panels by-columns)))))
   v271_l1830)))


(def
 v274_l1850
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t275_l1854
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 1 (:panels (pj/svg-summary pose)))))
   v274_l1850)))


(def
 v277_l1865
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t278_l1870
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v277_l1865)))


(def
 v280_l1886
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t281_l1891
 (is
  ((fn
    [pose]
    (let
     [mpg-pose (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
     (and
      (= 3 (count (:panels (pj/plan pose))))
      (=
       {:col :drv, :row :cyl}
       (select-keys
        (:mapping (pj/facet mpg-pose {:col :drv, :row :cyl}))
        [:col :row]))
      (=
       "Faceted"
       (get-in
        (pj/facet pose :species {:title "Faceted"})
        [:opts :title]))
      (=
       "Faceted"
       (get-in
        (pj/facet mpg-pose {:col :drv, :row :cyl} {:title "Faceted"})
        [:opts :title]))
      (=
       "Faceted"
       (get-in
        (pj/facet mpg-pose {:y [:hwy :cty]} {:title "Faceted"})
        [:opts :title])))))
   v280_l1886)))


(def
 v283_l1914
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :class {:cols 4})))


(deftest
 t284_l1918
 (is
  ((fn
    [pose]
    (and
     (= {:cols 4} (:layout pose))
     (nil? (:poses pose))
     (= 7 (:panels (pj/svg-summary pose)))))
   v283_l1914)))


(def
 v286_l1947
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v287_l1957 l4-shared)


(deftest
 t288_l1959
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__83601#]
         (get-in p1__83601# [:plan :panels 0 :x-domain]))
        (:sub-plots (pj/plan p))))
      domains
      (x-domains pose)
      cells
      (fn
       [share]
       (pj/arrange
        [(->
          iris
          (tc/select-rows
           (fn* [p1__83602#] (= "setosa" (:species p1__83602#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__83603#] (= "virginica" (:species p1__83603#))))
          (pj/pose :sepal-length :petal-width)
          pj/lay-point)]
        (if share {:share-scales #{:x}} {})))]
     (and
      (= 2 (count domains))
      (every? some? domains)
      (apply = domains)
      (apply = (x-domains (cells true)))
      (apply not= (x-domains (cells false)))
      (apply
       =
       (mapv
        (fn*
         [p1__83604#]
         (get-in p1__83604# [:plan :panels 0 :y-domain]))
        (:sub-plots
         (pj/plan
          (->
           iris
           (pj/lay-point :sepal-length :sepal-width)
           (pj/marginal :right))))))
      (apply
       =
       (mapv
        (fn*
         [p1__83605#]
         (get-in p1__83605# [:plan :panels 0 :y-domain]))
        (:sub-plots
         (pj/plan
          (pj/arrange
           [(->
             iris
             (tc/select-rows
              (fn* [p1__83606#] (= "setosa" (:species p1__83606#))))
             (pj/lay-histogram :sepal-length))
            (->
             iris
             (tc/select-rows
              (fn* [p1__83607#] (= "virginica" (:species p1__83607#))))
             (pj/lay-histogram :sepal-length))]
           {:share-scales #{:y}}))))))))
   v287_l1957)))


(def
 v290_l2026
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:petal-length :petal-width]}})))


(deftest
 t291_l2031
 (is
  ((fn
    [pose]
    (and
     (=
      {:col {:x [:sepal-length :sepal-width]},
       :row {:y [:petal-length :petal-width]}}
      (:facet pose))
     (= {:color :species} (:mapping pose))
     (nil? (:poses pose))
     (let
      [msg
       (try
        (pj/pose
         iris
         [[:sepal-length :petal-length] [:sepal-length :petal-width]])
        ""
        (catch Exception e (ex-message e)))]
      (and
       (re-find #"pj/pose was given pairs of columns" msg)
       (re-find #"pj/arrange" msg)
       (re-find #"pj/matrix data xs ys" msg)))))
   v290_l2026)))


(def
 v293_l2056
 (->
  iris
  (pj/matrix [:sepal-length :sepal-width] [:petal-length :petal-width])
  pj/lay-point))


(deftest
 t294_l2060
 (is
  ((fn
    [v]
    (and
     (= 4 (:panels (pj/svg-summary v)))
     (=
      v
      (->
       iris
       (pj/facet
        {:col {:x [:sepal-length :sepal-width]},
         :row {:y [:petal-length :petal-width]}})
       pj/lay-point))))
   v293_l2056)))
