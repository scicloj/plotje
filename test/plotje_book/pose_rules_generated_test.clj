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
    (partial mapv (fn* [p1__79948#] (dissoc p1__79948# :data))))
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
   (fn* [p1__79949#] (not (contains? p1__79949# :poses)))
   (rest (tree-seq :poses :poses pose)))))


(def v10_l91 (-> iris (pj/pose :sepal-length :sepal-width)))


(deftest
 t11_l94
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v10_l91)))


(def
 v12_l96
 (-> iris (pj/pose :sepal-length :sepal-width) pose-summary))


(deftest
 t13_l100
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (= [] (:layers pose))
     (not (contains? pose :poses))))
   v12_l96)))


(def v15_l112 (-> iris (pj/pose {:color :species}) pose-summary))


(deftest
 t16_l116
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (not (contains? pose :poses))))
   v15_l112)))


(def v18_l127 (-> iris pj/pose (pj/pose :sepal-length :sepal-width)))


(deftest
 t19_l131
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (not (contains? pose :poses))))
   v18_l127)))


(def
 v21_l138
 (->
  iris
  (pj/pose {:color :species})
  (pj/pose :sepal-length :sepal-width)))


(deftest
 t22_l142
 (is
  ((fn
    [pose]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping pose)))
   v21_l138)))


(def
 v24_l150
 (=
  (->
   iris
   pj/pose
   (pj/pose {:color :species})
   (pj/pose :sepal-length :sepal-width))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t25_l156 (is (true? v24_l150)))


(def
 v27_l166
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v28_l172
 (-> (trend-plot iris) (pj/pose :petal-length :petal-width)))


(def
 v29_l175
 (->
  (trend-plot iris)
  (pj/pose :petal-length :petal-width)
  pose-summary))


(deftest
 t30_l179
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping pose))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers pose))))
   v29_l175)))


(def
 v32_l189
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)))


(deftest
 t33_l193
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
   v32_l189)))


(def
 v35_l204
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t36_l208
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v35_l204)))


(def
 v38_l214
 (=
  (trend-plot iris)
  (pj/pose (trend-plot iris) :sepal-length :sepal-width)))


(deftest t39_l217 (is (true? v38_l214)))


(def
 v41_l225
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})))


(def
 v42_l229
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t43_l234
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v42_l229)))


(def
 v45_l243
 (=
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/pose {:color :species}))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t46_l248 (is (true? v45_l243)))


(def
 v48_l256
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})))


(def
 v49_l261
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t50_l267
 (is
  ((fn
    [pose]
    (and
     (= [{:layer-type :point}] (:layers pose))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v49_l261)))


(def
 v52_l279
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/pose {:color :species})
  pj/lay-point))


(def
 v53_l284
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/pose {:color :species})
  pj/lay-point
  pose-summary))


(deftest
 t54_l290
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}]
      (mapv :mapping (cells pose)))))
   v53_l284)))


(def
 v56_l302
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/pose :sepal-length :petal-length)
  pj/lay-point))


(deftest
 t57_l307
 (is
  ((fn
    [pose]
    (= 2 (count (re-seq #"not drawn" (with-out-str (pj/plan pose))))))
   v56_l302)))


(def
 v59_l320
 (def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width))))


(def v60_l322 leaf-pose)


(def v61_l324 (pose-summary leaf-pose))


(def v63_l328 (= leaf-pose (pj/pose leaf-pose) (pj/pose leaf-pose {})))


(deftest t64_l332 (is (true? v63_l328)))


(def
 v65_l334
 (=
  (pj/pose leaf-pose {:color :species})
  (pj/pose (pj/pose leaf-pose {:color :species}) {:color :species})))


(deftest t66_l337 (is (true? v65_l334)))


(def
 v68_l341
 (def
  composite-pose
  (->
   iris
   (pj/arrange
    [{:x :sepal-length, :y :sepal-width}
     {:x :petal-length, :y :petal-width}])
   pj/lay-point)))


(def v69_l346 composite-pose)


(def v70_l348 (pose-summary composite-pose))


(def
 v72_l352
 (=
  composite-pose
  (pj/pose composite-pose)
  (pj/pose composite-pose {})))


(deftest t73_l356 (is (true? v72_l352)))


(def
 v75_l373
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t76_l381
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
   v75_l373)))


(def
 v78_l400
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t79_l405
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v78_l400)))


(def
 v81_l415
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-histogram))


(deftest
 t82_l419
 (is
  ((fn
    [pose]
    (and
     (= 4 (:panels (pj/svg-summary pose)))
     (=
      [2 2]
      (mapv
       (fn* [p1__79950#] (count (:poses p1__79950#)))
       (:poses pose)))
     (=
      [{:x :sepal-length} {:x :sepal-width}]
      (mapv :mapping (:poses (first (:poses pose)))))))
   v81_l415)))


(def
 v84_l434
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :sepal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t85_l440
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
        (fn* [p1__79951#] (count (:poses p1__79951#)))
        (:poses one-per-row))))))
   v84_l434)))


(def
 v87_l462
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:sepal-length :sepal-width]}})))


(deftest
 t88_l467
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
   v87_l462)))


(def
 v90_l480
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


(deftest t91_l490 (is (true? v90_l480)))


(def
 v93_l514
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t94_l518
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v93_l514)))


(def
 v96_l527
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v97_l532
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t98_l538
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v97_l532)))


(def
 v100_l550
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
 t101_l560
 (is
  ((fn
    [counts]
    (let
     [marks
      (fn
       [pose]
       (mapv
        (fn*
         [p1__79952#]
         (mapv :mark (-> p1__79952# :plan :panels first :layers)))
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
         pj/lay-point))))))
   v100_l550)))


(def
 v103_l610
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t104_l614
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :point (:layer-type (first (:layers (first (cells pose))))))))
   v103_l610)))


(def
 v106_l626
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :petal-length :petal-width)))


(deftest
 t107_l630
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [[:point] [:point :smooth]]
      (mapv
       (fn* [p1__79953#] (mapv :layer-type (:layers p1__79953#)))
       (cells pose)))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v106_l626)))


(def
 v109_l646
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth {:y :sepal-width})))


(deftest
 t110_l652
 (is
  ((fn
    [pose]
    (and
     (=
      [[:smooth] []]
      (mapv
       (fn* [p1__79954#] (mapv :layer-type (:layers p1__79954#)))
       (cells pose)))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v109_l646)))


(def
 v112_l668
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t113_l672
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
   v112_l668)))


(def
 v115_l694
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t116_l699
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 1 (:panels (pj/svg-summary pose)))
     (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))
   v115_l694)))


(def
 v118_l710
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t119_l714
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v118_l710)))


(def
 v121_l732
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t122_l736
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      [3]
      (mapv
       (fn* [p1__79955#] (count (:poses p1__79955#)))
       (:poses pose)))
     (nil? (:mapping (nth (cells pose) 2)))
     (=
      [{:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:layers (nth (cells pose) 2))))))
   v121_l732)))


(def
 v124_l751
 (def
  cells-with-their-own-data
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])))


(def
 v125_l755
 (with-out-str
  (pj/lay-smooth
   cells-with-their-own-data
   :sepal-length
   :petal-length)))


(deftest
 t126_l758
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
   v125_l755)))


(def
 v128_l767
 (pj/lay-smooth
  cells-with-their-own-data
  :sepal-length
  :petal-length
  {:data iris}))


(deftest
 t129_l770
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      {:panels 3, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (not (some #{"no data"} (:texts (pj/svg-summary pose))))))
   v128_l767)))


(def
 v131_l785
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
 t132_l800
 (is
  ((fn
    [m]
    (and
     (= (:via-facet-drawn m) (:via-arrange-drawn m))
     (= :leaf (:via-facet-shape m))
     (= :composite (:via-arrange-shape m))))
   v131_l785)))


(def
 v134_l819
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t135_l826
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
   v134_l819)))


(def
 v137_l843
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t138_l848
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 3}
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v137_l843)))


(def v140_l863 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v141_l867 (-> tiny (pj/lay-point :a :b)))


(deftest
 t142_l870
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v141_l867)))


(def v144_l877 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t145_l882
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v144_l877)))


(def v147_l893 (pj/lay-point nil :a :b))


(deftest
 t148_l895
 (is
  ((fn
    [pose]
    (and
     (nil? (:data pose))
     (= [{:x :a, :y :b}] (mapv :mapping (:layers pose)))
     (= 5 (:points (pj/svg-summary (pj/with-data pose tiny))))))
   v147_l893)))


(def
 v150_l913
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t151_l918
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
   v150_l913)))


(def
 v153_l940
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t154_l946
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
   v153_l940)))


(def
 v156_l957
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t157_l963
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v156_l957)))


(def
 v159_l972
 (try
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t160_l979
 (is
  ((fn
    [msg]
    (and
     (re-find #":overlay is retired" msg)
     (re-find #"pj/facet" msg)
     (re-find #"pj/arrange" msg)))
   v159_l972)))


(def
 v162_l993
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-line {:y :petal-width})
  pj/lay-smooth))


(deftest
 t163_l998
 (is
  ((fn
    [v]
    (=
     [[:sepal-length :sepal-width]
      [:sepal-length :petal-width]
      [:sepal-length :sepal-width]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v162_l993)))


(def
 v165_l1008
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:x :petal-length})))


(deftest
 t166_l1012
 (is
  ((fn
    [v]
    (=
     {:mark :rug, :x :petal-length}
     (-> v pj/draft :layers second (select-keys [:mark :x :y]))))
   v165_l1008)))


(def
 v168_l1042
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t169_l1045
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v168_l1042)))


(def
 v171_l1052
 (->
  {:a [1 2], :b [3 4], :c [5 6], :d [7 8]}
  pj/lay-point
  (pj/pose {:x :a, :y :b})))


(deftest
 t172_l1056
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v171_l1052)))


(def
 v174_l1062
 (try
  (-> {:a [1 2], :b [3 4], :c [5 6], :d [7 8]} pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t175_l1069
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns: \[:a :b :c :d\]" msg)))
   v174_l1062)))


(def
 v177_l1098
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


(def v178_l1107 s1-composite)


(deftest
 t179_l1109
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
   v178_l1107)))


(def
 v181_l1121
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v182_l1129 s1-siblings)


(deftest
 t183_l1131
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
   v182_l1129)))


(def
 v185_l1151
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


(def v186_l1160 s2-tree)


(deftest
 t187_l1162
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
   v186_l1160)))


(def
 v189_l1187
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t190_l1192
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v189_l1187)))


(def
 v192_l1206
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t193_l1211
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v192_l1206)))


(def
 v195_l1226
 (->
  iris
  (pj/lay-line :sepal-length [:sepal-width :petal-width])
  pj/lay-point))


(deftest
 t196_l1230
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
   v195_l1226)))


(def
 v198_l1257
 (->
  iris
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :where {:species "setosa"}})
  (pj/arrange
   [{:where {:species "versicolor"}} {:where {:petal-width 0.2}}])
  (pj/lay-point {:color :species})))


(deftest
 t199_l1263
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
   v198_l1257)))


(def
 v201_l1277
 (->
  iris
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :where {:species "setosa"}})
  (pj/pose {:where {:species "versicolor"}})
  pj/lay-point))


(deftest
 t202_l1282
 (is ((fn [v] (= {:species "versicolor"} (:where v))) v201_l1277)))


(def
 v204_l1295
 (->
  iris
  (pj/pose {})
  (pj/scale :x :log)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point))


(deftest
 t205_l1301
 (is
  ((fn [v] (= {:type :log} (-> v pj/plan :panels first :x-scale)))
   v204_l1295)))


(def
 v207_l1324
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t208_l1329
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v207_l1324)))


(def
 v210_l1334
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t211_l1340
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v210_l1334)))


(def
 v213_l1359
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t214_l1365
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v213_l1359)))


(def
 v216_l1375
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t217_l1380
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v216_l1375)))


(def
 v219_l1388
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t220_l1393
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v219_l1388)))


(def
 v222_l1415
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t223_l1420
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v222_l1415)))


(def
 v225_l1427
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip)
  pj/lay-point
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t226_l1432
 (is
  ((fn
    [pose]
    (and
     (= :smoker (get-in pose [:mapping :col]))
     (= :sex (get-in pose [:mapping :row]))
     (= 4 (:panels (pj/svg-summary pose)))))
   v225_l1427)))


(def
 v228_l1442
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})
  (select-keys [:mapping :facet])
  kind/pprint))


(deftest
 t229_l1449
 (is
  ((fn
    [m]
    (=
     {:mapping {:x :sepal-length, :col :species},
      :facet {:y [:sepal-width :petal-width]}}
     m))
   v228_l1442)))


(def
 v231_l1459
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t232_l1464
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v231_l1459)))


(def
 v233_l1467
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t234_l1472
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v233_l1467)))


(def
 v236_l1482
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})
  pose-summary))


(deftest
 t237_l1488
 (is
  ((fn
    [pose]
    (and
     (= {:y [:sepal-width :petal-width]} (:facet pose))
     (nil? (get-in pose [:mapping :col]))
     (= 2 (:panels (pj/svg-summary pose)))))
   v236_l1482)))


(def
 v239_l1507
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t240_l1512
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__79956#]
        (when (= :rule-h (:layer-type p1__79956#)) p1__79956#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v239_l1507)))


(def
 v242_l1522
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t243_l1526
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :rule-h (:layer-type (first (:layers (first (cells pose))))))))
   v242_l1522)))


(def
 v245_l1538
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-rule-h :petal-length :petal-width {:y-intercept 3.0})))


(deftest
 t246_l1543
 (is
  ((fn
    [pose]
    (let
     [y-domain
      (fn*
       [p1__79957#]
       (-> p1__79957# pj/plan :panels first :y-domain))
      points
      (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)]
     (and
      (= 1 (:panels (pj/svg-summary pose)))
      (= (y-domain points) (y-domain pose))
      (re-find
       #"pj/lay-rule-h names :petal-length on :x and :petal-width on :y, columns the panel does not draw, so they are not read"
       (with-out-str (pj/plan pose))))))
   v245_l1538)))


(def
 v248_l1566
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:theme {:bg "#FFFFFF"}})
  (pj/options {:theme {:grid "#DDDDDD"}})))


(deftest
 t249_l1572
 (is
  ((fn
    [pose]
    (= {:bg "#FFFFFF", :grid "#DDDDDD"} (get-in pose [:opts :theme])))
   v248_l1566)))


(def
 v251_l1577
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
 t252_l1584
 (is
  ((fn [v] (not (re-find #"rgb\(232,232,232\)" (str (pj/plot v)))))
   v251_l1577)))


(def
 v254_l1604
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t255_l1612
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
   v254_l1604)))


(def
 v257_l1631
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t258_l1635
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
   v257_l1631)))


(def
 v260_l1668
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t261_l1672
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
   v260_l1668)))


(def
 v263_l1688
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t264_l1692
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 1 (:panels (pj/svg-summary pose)))))
   v263_l1688)))


(def
 v266_l1703
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t267_l1708
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v266_l1703)))


(def
 v269_l1724
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t270_l1729
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
   v269_l1724)))


(def
 v272_l1770
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v273_l1780 l4-shared)


(deftest
 t274_l1782
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__79958#]
         (get-in p1__79958# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__79959#] (= "setosa" (:species p1__79959#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__79960#] (= "virginica" (:species p1__79960#))))
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
         [p1__79961#]
         (get-in p1__79961# [:plan :panels 0 :y-domain]))
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
         [p1__79962#]
         (get-in p1__79962# [:plan :panels 0 :y-domain]))
        (:sub-plots
         (pj/plan
          (pj/arrange
           [(->
             iris
             (tc/select-rows
              (fn* [p1__79963#] (= "setosa" (:species p1__79963#))))
             (pj/lay-histogram :sepal-length))
            (->
             iris
             (tc/select-rows
              (fn* [p1__79964#] (= "virginica" (:species p1__79964#))))
             (pj/lay-histogram :sepal-length))]
           {:share-scales #{:y}}))))))))
   v273_l1780)))


(def
 v276_l1849
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:petal-length :petal-width]}})))


(deftest
 t277_l1854
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
   v276_l1849)))


(def
 v279_l1879
 (->
  iris
  (pj/matrix [:sepal-length :sepal-width] [:petal-length :petal-width])
  pj/lay-point))


(deftest
 t280_l1883
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
   v279_l1879)))
