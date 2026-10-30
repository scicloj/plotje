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
    (partial mapv (fn* [p1__11199#] (dissoc p1__11199# :data))))
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
   (fn* [p1__11200#] (not (contains? p1__11200# :poses)))
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
 v75_l371
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t76_l375
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= :vertical (get-in pose [:layout :direction]))
     (= 1 (count (:poses pose)))
     (= 2 (count (:poses (first (:poses pose)))))))
   v75_l371)))


(def
 v78_l388
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t79_l393
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v78_l388)))


(def
 v81_l403
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-histogram))


(deftest
 t82_l407
 (is
  ((fn
    [pose]
    (and
     (= 4 (:panels (pj/svg-summary pose)))
     (=
      [2 2]
      (mapv
       (fn* [p1__11201#] (count (:poses p1__11201#)))
       (:poses pose)))
     (=
      [{:x :sepal-length} {:x :sepal-width}]
      (mapv :mapping (:poses (first (:poses pose)))))))
   v81_l403)))


(def
 v84_l419
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :sepal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t85_l425
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
      (=
       [1 1]
       (mapv
        (fn* [p1__11202#] (count (:poses p1__11202#)))
        (:poses one-per-row))))))
   v84_l419)))


(def
 v87_l445
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:sepal-length :sepal-width]}})))


(deftest
 t88_l450
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
   v87_l445)))


(def
 v90_l463
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


(deftest t91_l473 (is (true? v90_l463)))


(def
 v93_l497
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t94_l501
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v93_l497)))


(def
 v96_l510
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v97_l515
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t98_l521
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v97_l515)))


(def
 v100_l533
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
 t101_l543
 (is
  ((fn
    [counts]
    (let
     [marks
      (fn
       [pose]
       (mapv
        (fn*
         [p1__11203#]
         (mapv :mark (-> p1__11203# :plan :panels first :layers)))
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
   v100_l533)))


(def
 v103_l587
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t104_l591
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :point (:layer-type (first (:layers (first (cells pose))))))))
   v103_l587)))


(def
 v106_l603
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :petal-length :petal-width)))


(deftest
 t107_l607
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [[:point] [:point :smooth]]
      (mapv
       (fn* [p1__11204#] (mapv :layer-type (:layers p1__11204#)))
       (cells pose)))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v106_l603)))


(def
 v109_l624
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t110_l628
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
   v109_l624)))


(def
 v112_l650
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t113_l655
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 1 (:panels (pj/svg-summary pose)))
     (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))
   v112_l650)))


(def
 v115_l666
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t116_l670
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v115_l666)))


(def
 v118_l687
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t119_l691
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      [3]
      (mapv
       (fn* [p1__11205#] (count (:poses p1__11205#)))
       (:poses pose)))
     (nil? (:mapping (nth (cells pose) 2)))
     (=
      [{:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:layers (nth (cells pose) 2))))))
   v118_l687)))


(def
 v121_l706
 (def
  cells-with-their-own-data
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])))


(def
 v122_l710
 (with-out-str
  (pj/lay-smooth
   cells-with-their-own-data
   :sepal-length
   :petal-length)))


(deftest
 t123_l713
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
   v122_l710)))


(def
 v125_l722
 (pj/lay-smooth
  cells-with-their-own-data
  :sepal-length
  :petal-length
  {:data iris}))


(deftest
 t126_l725
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      {:panels 3, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (not (some #{"no data"} (:texts (pj/svg-summary pose))))))
   v125_l722)))


(def
 v128_l740
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
 t129_l755
 (is
  ((fn
    [m]
    (and
     (= (:via-facet-drawn m) (:via-arrange-drawn m))
     (= :leaf (:via-facet-shape m))
     (= :composite (:via-arrange-shape m))))
   v128_l740)))


(def
 v131_l774
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t132_l781
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
   v131_l774)))


(def
 v134_l798
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t135_l803
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 3}
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v134_l798)))


(def v137_l818 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v138_l822 (-> tiny (pj/lay-point :a :b)))


(deftest
 t139_l825
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v138_l822)))


(def v141_l832 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t142_l837
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v141_l832)))


(def
 v144_l855
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t145_l860
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
   v144_l855)))


(def
 v147_l882
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t148_l888
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
   v147_l882)))


(def
 v150_l899
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t151_l905
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v150_l899)))


(def
 v153_l914
 (try
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t154_l921
 (is
  ((fn
    [msg]
    (and
     (re-find #":overlay is retired" msg)
     (re-find #"pj/facet" msg)
     (re-find #"pj/arrange" msg)))
   v153_l914)))


(def
 v156_l935
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-line {:y :petal-width})
  pj/lay-smooth))


(deftest
 t157_l940
 (is
  ((fn
    [v]
    (=
     [[:sepal-length :sepal-width]
      [:sepal-length :petal-width]
      [:sepal-length :sepal-width]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v156_l935)))


(def
 v159_l950
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:x :petal-length})
  pj/draft
  :layers
  second
  (select-keys [:mark :x :y])))


(deftest
 t160_l958
 (is ((fn [m] (= {:mark :rug, :x :petal-length} m)) v159_l950)))


(def
 v162_l982
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t163_l985
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v162_l982)))


(def
 v165_l992
 (->
  (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
  pj/lay-point
  (pj/pose {:x :a, :y :b})))


(deftest
 t166_l996
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v165_l992)))


(def
 v168_l1002
 (try
  (->
   (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
   pj/lay-point
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t169_l1009
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns: \[:a :b :c :d\]" msg)))
   v168_l1002)))


(def
 v171_l1031
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


(def v172_l1040 s1-composite)


(deftest
 t173_l1042
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
   v172_l1040)))


(def
 v175_l1054
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v176_l1062 s1-siblings)


(deftest
 t177_l1064
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
   v176_l1062)))


(def
 v179_l1082
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


(def v180_l1091 s2-tree)


(deftest
 t181_l1093
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
   v180_l1091)))


(def
 v183_l1110
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t184_l1115
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v183_l1110)))


(def
 v186_l1129
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t187_l1134
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v186_l1129)))


(def
 v189_l1155
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t190_l1160
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v189_l1155)))


(def
 v192_l1165
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t193_l1171
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v192_l1165)))


(def
 v195_l1190
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t196_l1196
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v195_l1190)))


(def
 v198_l1206
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t199_l1211
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v198_l1206)))


(def
 v201_l1219
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t202_l1224
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v201_l1219)))


(def
 v204_l1241
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t205_l1246
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v204_l1241)))


(def
 v207_l1251
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet {:col :species, :row :species})))


(deftest
 t208_l1256
 (is
  ((fn
    [pose]
    (and
     (= :species (get-in pose [:mapping :col]))
     (= :species (get-in pose [:mapping :row]))))
   v207_l1251)))


(def
 v210_l1265
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})
  (select-keys [:mapping :facet])
  kind/pprint))


(deftest
 t211_l1272
 (is
  ((fn
    [m]
    (=
     {:mapping {:x :sepal-length, :col :species},
      :facet {:y [:sepal-width :petal-width]}}
     m))
   v210_l1265)))


(def
 v213_l1282
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t214_l1287
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v213_l1282)))


(def
 v215_l1290
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t216_l1295
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v215_l1290)))


(def
 v218_l1305
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})
  pose-summary))


(deftest
 t219_l1311
 (is
  ((fn
    [pose]
    (and
     (= {:y [:sepal-width :petal-width]} (:facet pose))
     (nil? (get-in pose [:mapping :col]))
     (= 2 (:panels (pj/svg-summary pose)))))
   v218_l1305)))


(def
 v221_l1330
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t222_l1335
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__11206#]
        (when (= :rule-h (:layer-type p1__11206#)) p1__11206#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v221_l1330)))


(def
 v224_l1345
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t225_l1349
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :rule-h (:layer-type (first (:layers (first (cells pose))))))))
   v224_l1345)))


(def
 v227_l1361
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-rule-h :petal-length :petal-width {:y-intercept 3.0})))


(deftest
 t228_l1366
 (is
  ((fn
    [pose]
    (let
     [y-domain
      (fn*
       [p1__11207#]
       (-> p1__11207# pj/plan :panels first :y-domain))
      points
      (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)]
     (and
      (= 1 (:panels (pj/svg-summary pose)))
      (= (y-domain points) (y-domain pose))
      (re-find
       #"pj/lay-rule-h names :petal-length on :x and :petal-width on :y, columns the panel does not draw, so they are not read"
       (with-out-str (pj/plan pose))))))
   v227_l1361)))


(def
 v230_l1389
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t231_l1395
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
   v230_l1389)))


(def
 v233_l1412
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t234_l1416
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
   v233_l1412)))


(def
 v236_l1449
 (->
  iris
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t237_l1453
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
   v236_l1449)))


(def
 v239_l1469
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t240_l1473
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 1 (:panels (pj/svg-summary pose)))))
   v239_l1469)))


(def
 v242_l1484
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t243_l1489
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v242_l1484)))


(def
 v245_l1505
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t246_l1510
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
   v245_l1505)))


(def
 v248_l1551
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v249_l1557 l4-shared)


(deftest
 t250_l1559
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__11208#]
         (get-in p1__11208# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__11209#] (= "setosa" (:species p1__11209#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__11210#] (= "virginica" (:species p1__11210#))))
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
         [p1__11211#]
         (get-in p1__11211# [:plan :panels 0 :y-domain]))
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
         [p1__11212#]
         (get-in p1__11212# [:plan :panels 0 :y-domain]))
        (:sub-plots
         (pj/plan
          (pj/arrange
           [(->
             iris
             (tc/select-rows
              (fn* [p1__11213#] (= "setosa" (:species p1__11213#))))
             (pj/lay-histogram :sepal-length))
            (->
             iris
             (tc/select-rows
              (fn* [p1__11214#] (= "virginica" (:species p1__11214#))))
             (pj/lay-histogram :sepal-length))]
           {:share-scales #{:y}}))))))))
   v249_l1557)))


(def
 v252_l1625
 (->
  iris
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:petal-length :petal-width]}})))


(deftest
 t253_l1630
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
       (re-find
        #"pj/facet data \{:col \{:x xs\} :row \{:y ys\}\}"
        msg)))))
   v252_l1625)))


(def
 v255_l1655
 (->
  iris
  (pj/facet
   {:col {:x [:sepal-length :sepal-width]},
    :row {:y [:petal-length :petal-width]}})
  pj/lay-point))


(deftest
 t256_l1660
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v255_l1655)))
