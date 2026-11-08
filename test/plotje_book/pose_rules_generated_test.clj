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
    (partial mapv (fn* [p1__85965#] (dissoc p1__85965# :data))))
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
   (fn* [p1__85966#] (not (contains? p1__85966# :poses)))
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
       (fn* [p1__85967#] (count (:poses p1__85967#)))
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
        (fn* [p1__85968#] (count (:poses p1__85968#)))
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
 v100_l557
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
 t101_l567
 (is
  ((fn
    [counts]
    (let
     [marks
      (fn
       [pose]
       (mapv
        (fn*
         [p1__85969#]
         (mapv :mark (-> p1__85969# :plan :panels first :layers)))
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
   v100_l557)))


(def
 v103_l639
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width)))


(deftest
 t104_l644
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= [:point :smooth] (mapv :layer-type (:layers pose)))
     (every?
      (fn* [p1__85970#] (empty? (:layers p1__85970#)))
      (cells pose))
     (=
      [[:point :line] [:point]]
      (mapv
       (fn*
        [p1__85971#]
        (mapv :mark (-> p1__85971# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))))
   v103_l639)))


(def
 v106_l661
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
 t107_l669
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
   v106_l661)))


(def
 v109_l687
 (->
  iris
  (pj/pose {:x :petal-length})
  (pj/facet {:y [:sepal-length :sepal-width]})
  (pj/lay-smooth {:y :sepal-length})))


(deftest
 t110_l692
 (is
  ((fn
    [pose]
    (and
     (=
      {:panels 2, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :lines]))
     (re-find
      #"Note: pj/lay-smooth names :sepal-length on :y"
      (with-out-str (pj/plan pose)))))
   v109_l687)))


(def
 v112_l703
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :petal-length :petal-width)))


(deftest
 t113_l707
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [[:point] [:line :point]]
      (mapv
       (fn*
        [p1__85972#]
        (mapv :mark (-> p1__85972# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v112_l703)))


(def
 v115_l727
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth {:y :sepal-width})))


(deftest
 t116_l733
 (is
  ((fn
    [pose]
    (and
     (=
      [[:point :line] [:point]]
      (mapv
       (fn*
        [p1__85973#]
        (mapv :mark (-> p1__85973# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (= "" (with-out-str (pj/plan pose)))))
   v115_l727)))


(def
 v118_l757
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t119_l761
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
   v118_l757)))


(def
 v121_l783
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t122_l788
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 1 (:panels (pj/svg-summary pose)))
     (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))
   v121_l783)))


(def
 v124_l799
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t125_l803
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v124_l799)))


(def
 v127_l820
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t128_l824
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      [3]
      (mapv
       (fn* [p1__85974#] (count (:poses p1__85974#)))
       (:poses pose)))
     (nil? (:mapping (nth (cells pose) 2)))
     (=
      [{:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:layers (nth (cells pose) 2))))
     (=
      [[:point] [:point] [:bar]]
      (mapv
       (fn*
        [p1__85975#]
        (mapv :mark (-> p1__85975# :plan :panels first :layers)))
       (->
        iris
        (pj/arrange
         [{:x :sepal-length, :y :sepal-width}
          {:x :petal-length, :y :petal-width}])
        pj/lay-point
        (pj/lay-histogram :sepal-length)
        pj/plan
        :sub-plots)))))
   v127_l820)))


(def
 v130_l856
 (def
  cells-with-their-own-data
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])))


(def
 v131_l860
 (with-out-str
  (pj/lay-smooth
   cells-with-their-own-data
   :sepal-length
   :petal-length)))


(deftest
 t132_l863
 (is
  ((fn
    [note]
    (and
     (re-find
      #"Note: pj/lay-smooth names :sepal-length and :petal-length"
      note)
     (re-find #"That cell has no data" note)
     (re-find
      #"the cells draw :sepal-length / :sepal-width, :petal-length / :petal-width"
      note)
     (re-find
      #"\(pj/lay-smooth pose :sepal-length :petal-length \{:data data\}\)"
      note)))
   v131_l860)))


(def
 v134_l872
 (pj/lay-smooth
  cells-with-their-own-data
  :sepal-length
  :petal-length
  {:data iris}))


(deftest
 t135_l875
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      {:panels 3, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (not (some #{"no data"} (:texts (pj/svg-summary pose))))))
   v134_l872)))


(def
 v137_l890
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
 t138_l905
 (is
  ((fn
    [m]
    (and
     (= (:via-facet-drawn m) (:via-arrange-drawn m))
     (= :leaf (:via-facet-shape m))
     (= :composite (:via-arrange-shape m))))
   v137_l890)))


(def
 v140_l924
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t141_l931
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
   v140_l924)))


(def
 v143_l948
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t144_l953
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 3}
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v143_l948)))


(def v146_l968 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v147_l972 (-> tiny (pj/lay-point :a :b)))


(deftest
 t148_l975
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v147_l972)))


(def v150_l982 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t151_l987
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v150_l982)))


(def v153_l998 (pj/lay-point nil :a :b))


(deftest
 t154_l1000
 (is
  ((fn
    [pose]
    (and
     (nil? (:data pose))
     (= [{:x :a, :y :b}] (mapv :mapping (:layers pose)))
     (= 5 (:points (pj/svg-summary (pj/with-data pose tiny))))))
   v153_l998)))


(def
 v156_l1018
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t157_l1023
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
   v156_l1018)))


(def
 v159_l1045
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t160_l1051
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
   v159_l1045)))


(def
 v162_l1062
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t163_l1068
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v162_l1062)))


(def
 v165_l1077
 (try
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t166_l1084
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
   v165_l1077)))


(def
 v168_l1105
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-line {:y :petal-width})
  pj/lay-smooth))


(deftest
 t169_l1110
 (is
  ((fn
    [v]
    (=
     [[:sepal-length :sepal-width]
      [:sepal-length :petal-width]
      [:sepal-length :sepal-width]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v168_l1105)))


(def
 v171_l1120
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:x :petal-length})))


(deftest
 t172_l1124
 (is
  ((fn
    [v]
    (=
     {:mark :rug, :x :petal-length}
     (-> v pj/draft :layers second (select-keys [:mark :x :y]))))
   v171_l1120)))


(def
 v174_l1154
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t175_l1157
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v174_l1154)))


(def
 v177_l1164
 (->
  {:a [1 2], :b [3 4], :c [5 6], :d [7 8]}
  pj/lay-point
  (pj/pose {:x :a, :y :b})))


(deftest
 t178_l1168
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v177_l1164)))


(def
 v180_l1174
 (try
  (-> {:a [1 2], :b [3 4], :c [5 6], :d [7 8]} pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t181_l1181
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns: \[:a :b :c :d\]" msg)))
   v180_l1174)))


(def
 v183_l1210
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


(def v184_l1219 s1-composite)


(deftest
 t185_l1221
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
   v184_l1219)))


(def
 v187_l1233
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v188_l1241 s1-siblings)


(deftest
 t189_l1243
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
   v188_l1241)))


(def
 v191_l1263
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


(def v192_l1272 s2-tree)


(deftest
 t193_l1274
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
   v192_l1272)))


(def
 v195_l1299
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t196_l1304
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v195_l1299)))


(def
 v198_l1318
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t199_l1323
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v198_l1318)))


(def
 v201_l1338
 (->
  iris
  (pj/lay-line :sepal-length [:sepal-width :petal-width])
  pj/lay-point))


(deftest
 t202_l1342
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
   v201_l1338)))


(def
 v204_l1369
 (->
  iris
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :where {:species "setosa"}})
  (pj/arrange
   [{:where {:species "versicolor"}} {:where {:petal-width 0.2}}])
  (pj/lay-point {:color :species})))


(deftest
 t205_l1375
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
   v204_l1369)))


(def
 v207_l1389
 (->
  iris
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :where {:species "setosa"}})
  (pj/pose {:where {:species "versicolor"}})
  pj/lay-point))


(deftest
 t208_l1394
 (is ((fn [v] (= {:species "versicolor"} (:where v))) v207_l1389)))


(def
 v210_l1407
 (->
  iris
  (pj/pose {})
  (pj/scale :x :log)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t211_l1413
 (is
  ((fn [v] (= {:type :log} (-> v pj/plan :panels first :x-scale)))
   v210_l1407)))


(def
 v213_l1436
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t214_l1441
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v213_l1436)))


(def
 v216_l1446
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t217_l1452
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v216_l1446)))


(def
 v219_l1471
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t220_l1477
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v219_l1471)))


(def
 v222_l1487
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t223_l1492
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v222_l1487)))


(def
 v225_l1500
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t226_l1505
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v225_l1500)))


(def
 v228_l1527
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t229_l1532
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v228_l1527)))


(def
 v231_l1539
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip)
  pj/lay-point
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t232_l1544
 (is
  ((fn
    [pose]
    (and
     (= :smoker (get-in pose [:mapping :col]))
     (= :sex (get-in pose [:mapping :row]))
     (= 4 (:panels (pj/svg-summary pose)))))
   v231_l1539)))


(def
 v234_l1554
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})
  (select-keys [:mapping :facet])
  kind/pprint))


(deftest
 t235_l1561
 (is
  ((fn
    [m]
    (=
     {:mapping {:x :sepal-length, :col :species},
      :facet {:y [:sepal-width :petal-width]}}
     m))
   v234_l1554)))


(def
 v237_l1571
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t238_l1576
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v237_l1571)))


(def
 v239_l1579
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t240_l1584
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v239_l1579)))


(def
 v242_l1594
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})
  pose-summary))


(deftest
 t243_l1600
 (is
  ((fn
    [pose]
    (and
     (= {:y [:sepal-width :petal-width]} (:facet pose))
     (nil? (get-in pose [:mapping :col]))
     (= 2 (:panels (pj/svg-summary pose)))))
   v242_l1594)))


(def
 v245_l1620
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t246_l1625
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__85976#]
        (when (= :rule-h (:layer-type p1__85976#)) p1__85976#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v245_l1620)))


(def
 v248_l1635
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t249_l1639
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
        [p1__85977#]
        (mapv :mark (-> p1__85977# :plan :panels first :layers)))
       (:sub-plots (pj/plan pose))))))
   v248_l1635)))


(def
 v251_l1657
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-rule-h :petal-length :petal-width {:y-intercept 3.0})))


(deftest
 t252_l1662
 (is
  ((fn
    [pose]
    (let
     [y-domain
      (fn*
       [p1__85978#]
       (-> p1__85978# pj/plan :panels first :y-domain))
      points
      (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)]
     (and
      (= 1 (:panels (pj/svg-summary pose)))
      (= (y-domain points) (y-domain pose))
      (re-find
       #"pj/lay-rule-h names :petal-length on :x and :petal-width on :y, columns the panel does not draw, so they are not read"
       (with-out-str (pj/plan pose))))))
   v251_l1657)))


(def
 v254_l1685
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:theme {:bg "#FFFFFF"}})
  (pj/options {:theme {:grid "#DDDDDD"}})))


(deftest
 t255_l1691
 (is
  ((fn
    [pose]
    (= {:bg "#FFFFFF", :grid "#DDDDDD"} (get-in pose [:opts :theme])))
   v254_l1685)))


(def
 v257_l1696
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
 t258_l1703
 (is
  ((fn [v] (not (re-find #"rgb\(232,232,232\)" (str (pj/plot v)))))
   v257_l1696)))


(def
 v260_l1726
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t261_l1735
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
   v260_l1726)))


(def
 v263_l1754
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t264_l1758
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
   v263_l1754)))


(def
 v266_l1791
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t267_l1795
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
   v266_l1791)))


(def
 v269_l1811
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t270_l1815
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 1 (:panels (pj/svg-summary pose)))))
   v269_l1811)))


(def
 v272_l1826
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t273_l1831
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v272_l1826)))


(def
 v275_l1847
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t276_l1852
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
   v275_l1847)))


(def
 v278_l1875
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :class {:cols 4})))


(deftest
 t279_l1879
 (is
  ((fn
    [pose]
    (and
     (= {:cols 4} (:layout pose))
     (nil? (:poses pose))
     (= 7 (:panels (pj/svg-summary pose)))))
   v278_l1875)))


(def
 v281_l1908
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v282_l1918 l4-shared)


(deftest
 t283_l1920
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__85979#]
         (get-in p1__85979# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__85980#] (= "setosa" (:species p1__85980#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__85981#] (= "virginica" (:species p1__85981#))))
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
         [p1__85982#]
         (get-in p1__85982# [:plan :panels 0 :y-domain]))
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
         [p1__85983#]
         (get-in p1__85983# [:plan :panels 0 :y-domain]))
        (:sub-plots
         (pj/plan
          (pj/arrange
           [(->
             iris
             (tc/select-rows
              (fn* [p1__85984#] (= "setosa" (:species p1__85984#))))
             (pj/lay-histogram :sepal-length))
            (->
             iris
             (tc/select-rows
              (fn* [p1__85985#] (= "virginica" (:species p1__85985#))))
             (pj/lay-histogram :sepal-length))]
           {:share-scales #{:y}}))))))))
   v282_l1918)))


(def
 v285_l1987
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:petal-length :petal-width]}})))


(deftest
 t286_l1992
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
   v285_l1987)))


(def
 v288_l2017
 (->
  iris
  (pj/matrix [:sepal-length :sepal-width] [:petal-length :petal-width])
  pj/lay-point))


(deftest
 t289_l2021
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
   v288_l2017)))
