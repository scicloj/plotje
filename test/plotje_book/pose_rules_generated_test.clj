(ns
 plotje-book.pose-rules-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l29 (def iris (rdatasets/datasets-iris)))


(def
 v5_l35
 (defn
  strip-data
  [pose]
  (cond->
   (dissoc pose :data)
   (:layers pose)
   (update
    :layers
    (partial mapv (fn* [p1__78644#] (dissoc p1__78644# :data))))
   (:poses pose)
   (update :poses (partial mapv strip-data)))))


(def
 v6_l40
 (defn
  pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose))))


(def
 v8_l48
 (defn
  cells
  [pose]
  (filterv
   (fn* [p1__78645#] (not (contains? p1__78645# :poses)))
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
 v35_l204
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
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
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose {:color :species})
  pj/lay-point))


(def
 v53_l284
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
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
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
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
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
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
 v75_l367
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t76_l371
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= :vertical (get-in pose [:layout :direction]))
     (= 1 (count (:poses pose)))
     (= 2 (count (:poses (first (:poses pose)))))))
   v75_l367)))


(def
 v78_l384
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t79_l389
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v78_l384)))


(def
 v81_l401
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :sepal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t82_l407
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
        (fn* [p1__78646#] (count (:poses p1__78646#)))
        (:poses one-per-row))))))
   v81_l401)))


(def
 v84_l427
 (->
  iris
  (pj/facet-matrix [:sepal-length :sepal-width] {:color :species})))


(deftest
 t85_l431
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
   v84_l427)))


(def
 v87_l443
 (let
  [a
   (->
    iris
    (pj/pose {:color :species})
    (pj/facet-matrix [:sepal-length :sepal-width]))
   b
   (->
    iris
    (pj/facet-matrix [:sepal-length :sepal-width] {:color :species}))]
  (= a b)))


(deftest t88_l451 (is (true? v87_l443)))


(def
 v90_l475
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t91_l479
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v90_l475)))


(def
 v93_l488
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v94_l493
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t95_l499
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v94_l493)))


(def
 v97_l511
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
 t98_l521
 (is
  ((fn
    [counts]
    (let
     [marks
      (fn
       [pose]
       (mapv
        (fn*
         [p1__78647#]
         (mapv :mark (-> p1__78647# :plan :panels first :layers)))
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
         (pj/facet-matrix [:sepal-length :sepal-width])
         pj/lay-point))))))
   v97_l511)))


(def
 v100_l564
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t101_l568
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :point (:layer-type (first (:layers (first (cells pose))))))))
   v100_l564)))


(def
 v103_l580
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :petal-length :petal-width)))


(deftest
 t104_l584
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [[:point] [:point :smooth]]
      (mapv
       (fn* [p1__78648#] (mapv :layer-type (:layers p1__78648#)))
       (cells pose)))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v103_l580)))


(def
 v106_l601
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t107_l605
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
   v106_l601)))


(def
 v109_l627
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t110_l632
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 1 (:panels (pj/svg-summary pose)))
     (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))
   v109_l627)))


(def
 v112_l643
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t113_l647
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v112_l643)))


(def
 v115_l661
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t116_l665
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (nil? (:mapping (nth (cells pose) 2)))
     (=
      [{:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:layers (nth (cells pose) 2))))))
   v115_l661)))


(def
 v118_l679
 (def
  cells-with-their-own-data
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])))


(def
 v119_l683
 (with-out-str
  (pj/lay-smooth
   cells-with-their-own-data
   :sepal-length
   :petal-length)))


(deftest
 t120_l686
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
   v119_l683)))


(def
 v122_l695
 (pj/lay-smooth
  cells-with-their-own-data
  :sepal-length
  :petal-length
  {:data iris}))


(deftest
 t123_l698
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      {:panels 3, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (not (some #{"no data"} (:texts (pj/svg-summary pose))))))
   v122_l695)))


(def
 v125_l713
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
     [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))
   drawn
   (fn [pose] (select-keys (pj/svg-summary pose) [:panels :points]))]
  {:via-facet-drawn (drawn via-facet),
   :via-arrange-drawn (drawn via-arrange),
   :via-facet-shape (if (:poses via-facet) :composite :leaf),
   :via-arrange-shape (if (:poses via-arrange) :composite :leaf)}))


(deftest
 t126_l728
 (is
  ((fn
    [m]
    (and
     (= (:via-facet-drawn m) (:via-arrange-drawn m))
     (= :leaf (:via-facet-shape m))
     (= :composite (:via-arrange-shape m))))
   v125_l713)))


(def
 v128_l747
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t129_l754
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
   v128_l747)))


(def
 v131_l771
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t132_l776
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 3}
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v131_l771)))


(def v134_l791 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v135_l795 (-> tiny (pj/lay-point :a :b)))


(deftest
 t136_l798
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v135_l795)))


(def v138_l805 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t139_l810
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v138_l805)))


(def
 v141_l828
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t142_l833
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
   v141_l828)))


(def
 v144_l855
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t145_l861
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
   v144_l855)))


(def
 v147_l872
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t148_l878
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v147_l872)))


(def
 v150_l887
 (try
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t151_l894
 (is
  ((fn
    [msg]
    (and
     (re-find #":overlay is retired" msg)
     (re-find #"pj/facet" msg)
     (re-find #"pj/arrange" msg)))
   v150_l887)))


(def
 v153_l908
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-line {:y :petal-width})
  pj/lay-smooth))


(deftest
 t154_l913
 (is
  ((fn
    [v]
    (=
     [[:sepal-length :sepal-width]
      [:sepal-length :petal-width]
      [:sepal-length :sepal-width]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v153_l908)))


(def
 v156_l923
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:x :petal-length})
  pj/draft
  :layers
  second
  (select-keys [:mark :x :y])))


(deftest
 t157_l931
 (is ((fn [m] (= {:mark :rug, :x :petal-length} m)) v156_l923)))


(def
 v159_l956
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t160_l959
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v159_l956)))


(def
 v162_l966
 (->
  (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
  pj/lay-point
  (pj/pose {:x :a, :y :b})))


(deftest
 t163_l970
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v162_l966)))


(def
 v165_l976
 (try
  (->
   (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
   pj/lay-point
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t166_l983
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns: \[:a :b :c :d\]" msg)))
   v165_l976)))


(def
 v168_l1005
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


(def v169_l1014 s1-composite)


(deftest
 t170_l1016
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
   v169_l1014)))


(def
 v172_l1028
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v173_l1036 s1-siblings)


(deftest
 t174_l1038
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
   v173_l1036)))


(def
 v176_l1056
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


(def v177_l1065 s2-tree)


(deftest
 t178_l1067
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
   v177_l1065)))


(def
 v180_l1084
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t181_l1089
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v180_l1084)))


(def
 v183_l1103
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t184_l1108
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v183_l1103)))


(def
 v186_l1129
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t187_l1134
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v186_l1129)))


(def
 v189_l1139
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t190_l1145
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v189_l1139)))


(def
 v192_l1164
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t193_l1170
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v192_l1164)))


(def
 v195_l1180
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t196_l1185
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v195_l1180)))


(def
 v198_l1193
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t199_l1198
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v198_l1193)))


(def
 v201_l1214
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t202_l1219
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v201_l1214)))


(def
 v204_l1224
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet-grid :species :species)))


(deftest
 t205_l1229
 (is
  ((fn
    [pose]
    (and
     (= :species (get-in pose [:mapping :col]))
     (= :species (get-in pose [:mapping :row]))))
   v204_l1224)))


(def
 v207_l1238
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t208_l1243
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v207_l1238)))


(def
 v209_l1246
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t210_l1251
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v209_l1246)))


(def
 v212_l1261
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})
  pose-summary))


(deftest
 t213_l1267
 (is
  ((fn
    [pose]
    (and
     (= {:y [:sepal-width :petal-width]} (:facet pose))
     (nil? (get-in pose [:mapping :col]))
     (= 2 (:panels (pj/svg-summary pose)))))
   v212_l1261)))


(def
 v215_l1286
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t216_l1291
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__78649#]
        (when (= :rule-h (:layer-type p1__78649#)) p1__78649#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v215_l1286)))


(def
 v218_l1301
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t219_l1305
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :rule-h (:layer-type (first (:layers (first (cells pose))))))))
   v218_l1301)))


(def
 v221_l1317
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-rule-h :petal-length :petal-width {:y-intercept 3.0})))


(deftest
 t222_l1322
 (is
  ((fn
    [pose]
    (let
     [y-domain
      (fn*
       [p1__78650#]
       (-> p1__78650# pj/plan :panels first :y-domain))
      points
      (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)]
     (and
      (= 1 (:panels (pj/svg-summary pose)))
      (= (y-domain points) (y-domain pose))
      (re-find
       #"pj/lay-rule-h names :petal-length on :x and :petal-width on :y, columns the panel does not draw, so they are not read"
       (with-out-str (pj/plan pose))))))
   v221_l1317)))


(def
 v224_l1345
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t225_l1351
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
   v224_l1345)))


(def
 v227_l1368
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t228_l1372
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
   v227_l1368)))


(def
 v230_l1405
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point))


(deftest
 t231_l1409
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
   v230_l1405)))


(def
 v233_l1425
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t234_l1429
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 1 (:panels (pj/svg-summary pose)))))
   v233_l1425)))


(def
 v236_l1440
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t237_l1445
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v236_l1440)))


(def
 v239_l1461
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t240_l1466
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
        (:mapping (pj/facet-grid mpg-pose :drv :cyl))
        [:col :row]))
      (=
       "Faceted"
       (get-in
        (pj/facet pose :species {:title "Faceted"})
        [:opts :title]))
      (=
       "Faceted"
       (get-in
        (pj/facet-grid mpg-pose :drv :cyl {:title "Faceted"})
        [:opts :title]))
      (=
       "Faceted"
       (get-in
        (pj/facet mpg-pose {:y [:hwy :cty]} {:title "Faceted"})
        [:opts :title])))))
   v239_l1461)))


(def
 v242_l1507
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v243_l1513 l4-shared)


(deftest
 t244_l1515
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__78651#]
         (get-in p1__78651# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__78652#] (= "setosa" (:species p1__78652#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__78653#] (= "virginica" (:species p1__78653#))))
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
         [p1__78654#]
         (get-in p1__78654# [:plan :panels 0 :y-domain]))
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
         [p1__78655#]
         (get-in p1__78655# [:plan :panels 0 :y-domain]))
        (:sub-plots
         (pj/plan
          (pj/arrange
           [(->
             iris
             (tc/select-rows
              (fn* [p1__78656#] (= "setosa" (:species p1__78656#))))
             (pj/lay-histogram :sepal-length))
            (->
             iris
             (tc/select-rows
              (fn* [p1__78657#] (= "virginica" (:species p1__78657#))))
             (pj/lay-histogram :sepal-length))]
           {:share-scales #{:y}}))))))))
   v243_l1513)))


(def
 v246_l1582
 (->
  iris
  (pj/facet-matrix
   [:sepal-length :sepal-width]
   [:petal-length :petal-width]
   {:color :species})))


(deftest
 t247_l1587
 (is
  ((fn
    [pose]
    (and
     (=
      {:col {:x [:sepal-length :sepal-width]},
       :row {:y [:petal-length :petal-width]}}
      (:facet pose))
     (= {:color :species} (:mapping pose))
     (=
      (pj/plot pose)
      (pj/plot
       (->
        iris
        (pj/pose {:color :species})
        (pj/facet
         {:col {:x [:sepal-length :sepal-width]},
          :row {:y [:petal-length :petal-width]}}))))
     (let
      [msg
       (try
        (pj/pose
         iris
         (pj/cross [:sepal-length] [:petal-length :petal-width]))
        ""
        (catch Exception e (ex-message e)))]
      (and
       (re-find #"pj/pose was given pairs of columns" msg)
       (re-find #"pj/arrange" msg)
       (re-find #"pj/facet-matrix" msg)))))
   v246_l1582)))


(def v249_l1616 (pj/cross [:a :b] [:c :d]))


(deftest
 t250_l1618
 (is
  ((fn
    [pairs]
    (and
     (= [[:a :c] [:a :d] [:b :c] [:b :d]] pairs)
     (=
      4
      (:panels
       (pj/svg-summary
        (->
         iris
         (pj/arrange
          (pj/cross
           [:sepal-length :sepal-width]
           [:petal-length :petal-width]))
         pj/lay-point))))))
   v249_l1616)))
