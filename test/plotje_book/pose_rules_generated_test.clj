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
    (partial mapv (fn* [p1__76171#] (dissoc p1__76171# :data))))
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
   (fn* [p1__76172#] (not (contains? p1__76172# :poses)))
   (rest (tree-seq :poses :poses pose)))))


(def v10_l89 (-> iris (pj/pose :sepal-length :sepal-width)))


(deftest
 t11_l92
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v10_l89)))


(def
 v12_l94
 (-> iris (pj/pose :sepal-length :sepal-width) pose-summary))


(deftest
 t13_l98
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (= [] (:layers pose))
     (not (contains? pose :poses))))
   v12_l94)))


(def v15_l108 (-> iris (pj/pose {:color :species}) pose-summary))


(deftest
 t16_l112
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (not (contains? pose :poses))))
   v15_l108)))


(def v18_l124 (-> iris pj/pose (pj/pose :sepal-length :sepal-width)))


(deftest
 t19_l128
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (not (contains? pose :poses))))
   v18_l124)))


(def
 v21_l135
 (->
  iris
  (pj/pose {:color :species})
  (pj/pose :sepal-length :sepal-width)))


(deftest
 t22_l139
 (is
  ((fn
    [pose]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping pose)))
   v21_l135)))


(def
 v24_l147
 (=
  (->
   iris
   pj/pose
   (pj/pose {:color :species})
   (pj/pose :sepal-length :sepal-width))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t25_l153 (is (true? v24_l147)))


(def
 v27_l163
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v28_l169
 (-> (trend-plot iris) (pj/pose :petal-length :petal-width)))


(def
 v29_l172
 (->
  (trend-plot iris)
  (pj/pose :petal-length :petal-width)
  pose-summary))


(deftest
 t30_l176
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping pose))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers pose))))
   v29_l172)))


(def
 v32_l184
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point))


(deftest
 t33_l188
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v32_l184)))


(def
 v35_l194
 (=
  (trend-plot iris)
  (pj/pose (trend-plot iris) :sepal-length :sepal-width)))


(deftest t36_l197 (is (true? v35_l194)))


(def
 v38_l204
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})))


(def
 v39_l208
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t40_l213
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v39_l208)))


(def
 v42_l222
 (=
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/pose {:color :species}))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t43_l227 (is (true? v42_l222)))


(def
 v45_l235
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})))


(def
 v46_l240
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t47_l246
 (is
  ((fn
    [pose]
    (and
     (= [{:layer-type :point}] (:layers pose))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v46_l240)))


(def
 v49_l258
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose {:color :species})
  pj/lay-point))


(def
 v50_l263
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose {:color :species})
  pj/lay-point
  pose-summary))


(deftest
 t51_l269
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}]
      (mapv :mapping (cells pose)))))
   v50_l263)))


(def
 v53_l280
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose :sepal-length :petal-length)
  pj/lay-point))


(deftest
 t54_l285
 (is
  ((fn [pose] (re-find #"not drawn" (with-out-str (pj/plan pose))))
   v53_l280)))


(def
 v56_l298
 (def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width))))


(def v57_l300 leaf-pose)


(def v58_l302 (pose-summary leaf-pose))


(def v60_l306 (= leaf-pose (pj/pose leaf-pose) (pj/pose leaf-pose {})))


(deftest t61_l310 (is (true? v60_l306)))


(def
 v62_l312
 (=
  (pj/pose leaf-pose {:color :species})
  (pj/pose (pj/pose leaf-pose {:color :species}) {:color :species})))


(deftest t63_l315 (is (true? v62_l312)))


(def
 v65_l319
 (def
  composite-pose
  (->
   iris
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   pj/lay-point)))


(def v66_l324 composite-pose)


(def v67_l326 (pose-summary composite-pose))


(def
 v69_l330
 (=
  composite-pose
  (pj/pose composite-pose)
  (pj/pose composite-pose {})))


(deftest t70_l334 (is (true? v69_l330)))


(def
 v72_l343
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t73_l347
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= :vertical (get-in pose [:layout :direction]))
     (= 1 (count (:poses pose)))
     (= 2 (count (:poses (first (:poses pose)))))))
   v72_l343)))


(def
 v75_l360
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t76_l365
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v75_l360)))


(def
 v78_l375
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :sepal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t79_l381
 (is
  ((fn
    [pose]
    (and
     (= "Arranged" (get-in pose [:opts :title]))
     (= #{:y} (get-in pose [:opts :share-scales]))))
   v78_l375)))


(def
 v81_l392
 (->
  iris
  (pj/cross-matrix [:sepal-length :sepal-width] {:color :species})))


(deftest
 t82_l396
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (= 2 (count (:poses pose)))
     (every?
      (fn* [p1__76173#] (= 2 (count (:poses p1__76173#))))
      (:poses pose))))
   v81_l392)))


(def
 v84_l405
 (let
  [a
   (->
    iris
    (pj/pose {:color :species})
    (pj/cross-matrix [:sepal-length :sepal-width]))
   b
   (->
    iris
    (pj/cross-matrix [:sepal-length :sepal-width] {:color :species}))]
  (= a b)))


(deftest t85_l413 (is (true? v84_l405)))


(def
 v87_l436
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t88_l440
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v87_l436)))


(def
 v90_l449
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v91_l454
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t92_l460
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v91_l454)))


(def
 v94_l472
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


(deftest t95_l482 (is ((fn [counts] (= [0 1] counts)) v94_l472)))


(def
 v97_l499
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t98_l503
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :point (:layer-type (first (:layers (first (cells pose))))))))
   v97_l499)))


(def
 v100_l515
 (->
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])
  (pj/lay-smooth :petal-length :petal-width)))


(deftest
 t101_l519
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [[:point] [:point :smooth]]
      (mapv
       (fn* [p1__76174#] (mapv :layer-type (:layers p1__76174#)))
       (cells pose)))
     (=
      {:panels 2, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))))
   v100_l515)))


(def
 v103_l535
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t104_l539
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
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v103_l535)))


(def
 v106_l559
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t107_l564
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 1 (:panels (pj/svg-summary pose)))
     (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))
   v106_l559)))


(def
 v109_l575
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t110_l579
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v109_l575)))


(def
 v112_l593
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t113_l597
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (nil? (:mapping (nth (cells pose) 2)))
     (=
      [{:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:layers (nth (cells pose) 2))))))
   v112_l593)))


(def
 v115_l609
 (def
  cells-with-their-own-data
  (pj/arrange
   [(pj/lay-point iris :sepal-length :sepal-width)
    (pj/lay-point iris :petal-length :petal-width)])))


(def
 v116_l613
 (with-out-str
  (pj/lay-smooth
   cells-with-their-own-data
   :sepal-length
   :petal-length)))


(deftest
 t117_l616
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
   v116_l613)))


(def
 v119_l625
 (pj/lay-smooth
  cells-with-their-own-data
  :sepal-length
  :petal-length
  {:data iris}))


(deftest
 t120_l628
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (=
      {:panels 3, :points 300, :lines 1}
      (select-keys (pj/svg-summary pose) [:panels :points :lines]))
     (not (some #{"no data"} (:texts (pj/svg-summary pose))))))
   v119_l625)))


(def
 v122_l643
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
 t123_l658
 (is
  ((fn
    [m]
    (and
     (= (:via-facet-drawn m) (:via-arrange-drawn m))
     (= :leaf (:via-facet-shape m))
     (= :composite (:via-arrange-shape m))))
   v122_l643)))


(def
 v125_l671
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t126_l678
 (is
  ((fn
    [msg]
    (and (string? msg) (re-find #"doesn't exist in the data" msg)))
   v125_l671)))


(def
 v128_l686
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t129_l691
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (=
      {:panels 1, :points 3}
      (select-keys (pj/svg-summary fr) [:panels :points]))))
   v128_l686)))


(def v131_l706 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v132_l710 (-> tiny (pj/lay-point :a :b)))


(deftest
 t133_l713
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v132_l710)))


(def v135_l720 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t136_l725
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v135_l720)))


(def
 v138_l743
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t139_l748
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
   v138_l743)))


(def
 v141_l770
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t142_l776
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
   v141_l770)))


(def
 v144_l787
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t145_l793
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v144_l787)))


(def
 v147_l802
 (try
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t148_l809
 (is
  ((fn
    [msg]
    (and
     (re-find #":overlay is retired" msg)
     (re-find #"pj/facet" msg)
     (re-find #"pj/arrange" msg)))
   v147_l802)))


(def
 v150_l823
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-line {:y :petal-width})
  pj/lay-smooth))


(deftest
 t151_l828
 (is
  ((fn
    [v]
    (=
     [[:sepal-length :sepal-width]
      [:sepal-length :petal-width]
      [:sepal-length :sepal-width]]
     (mapv (juxt :x :y) (:layers (pj/draft v)))))
   v150_l823)))


(def
 v153_l838
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rug {:x :petal-length})
  pj/draft
  :layers
  second
  (select-keys [:mark :x :y])))


(deftest
 t154_l846
 (is ((fn [m] (= {:mark :rug, :x :petal-length} m)) v153_l838)))


(def
 v156_l871
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t157_l874
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v156_l871)))


(def
 v159_l881
 (->
  (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
  pj/lay-point
  (pj/pose {:x :a, :y :b})))


(deftest
 t160_l885
 (is ((fn [v] (= 2 (:points (pj/svg-summary v)))) v159_l881)))


(def
 v162_l891
 (try
  (->
   (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
   pj/lay-point
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t163_l898
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns: \[:a :b :c :d\]" msg)))
   v162_l891)))


(def
 v165_l920
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


(def v166_l929 s1-composite)


(deftest
 t167_l931
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
   v166_l929)))


(def
 v169_l943
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v170_l951 s1-siblings)


(deftest
 t171_l953
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
   v170_l951)))


(def
 v173_l971
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


(def v174_l980 s2-tree)


(deftest
 t175_l982
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
   v174_l980)))


(def
 v177_l999
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t178_l1004
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v177_l999)))


(def
 v180_l1018
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t181_l1023
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v180_l1018)))


(def
 v183_l1044
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t184_l1049
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v183_l1044)))


(def
 v186_l1054
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t187_l1060
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v186_l1054)))


(def
 v189_l1079
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t190_l1085
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v189_l1079)))


(def
 v192_l1095
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t193_l1100
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v192_l1095)))


(def
 v195_l1108
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t196_l1113
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v195_l1108)))


(def
 v198_l1129
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t199_l1134
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v198_l1129)))


(def
 v201_l1139
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet-grid :species :species)))


(deftest
 t202_l1144
 (is
  ((fn
    [pose]
    (and
     (= :species (get-in pose [:mapping :col]))
     (= :species (get-in pose [:mapping :row]))))
   v201_l1139)))


(def
 v204_l1153
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t205_l1158
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v204_l1153)))


(def
 v206_l1161
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t207_l1166
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v206_l1161)))


(def
 v209_l1176
 (->
  iris
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-width]})
  pose-summary))


(deftest
 t210_l1182
 (is
  ((fn
    [pose]
    (and
     (= {:y [:sepal-width :petal-width]} (:facet pose))
     (nil? (get-in pose [:mapping :col]))
     (= 2 (:panels (pj/svg-summary pose)))))
   v209_l1176)))


(def
 v212_l1197
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t213_l1202
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__76175#]
        (when (= :rule-h (:layer-type p1__76175#)) p1__76175#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v212_l1197)))


(def
 v215_l1212
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t216_l1216
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :rule-h (:layer-type (first (:layers (first (cells pose))))))))
   v215_l1212)))


(def
 v218_l1237
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t219_l1243
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
   v218_l1237)))


(def
 v221_l1260
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t222_l1264
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
   v221_l1260)))


(def
 v224_l1293
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point))


(deftest
 t225_l1297
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose)]
     (and (:composite? plan) (= 2 (count (:sub-plots plan))))))
   v224_l1293)))


(def
 v227_l1305
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t228_l1309
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 1 (:panels (pj/svg-summary pose)))))
   v227_l1305)))


(def
 v230_l1320
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t231_l1325
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v230_l1320)))


(def
 v233_l1338
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t234_l1343
 (is ((fn [pose] (= 3 (count (:panels (pj/plan pose))))) v233_l1338)))


(def
 v236_l1364
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v237_l1370 l4-shared)


(deftest
 t238_l1372
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__76176#]
         (get-in p1__76176# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__76177#] (= "setosa" (:species p1__76177#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__76178#] (= "virginica" (:species p1__76178#))))
          (pj/pose :sepal-length :petal-width)
          pj/lay-point)]
        (if share {:share-scales #{:x}} {})))]
     (and
      (= 2 (count domains))
      (every? some? domains)
      (apply = domains)
      (apply = (x-domains (cells true)))
      (apply not= (x-domains (cells false))))))
   v237_l1370)))


(def
 v240_l1432
 (->
  iris
  (pj/cross-matrix
   [:sepal-length :sepal-width]
   [:petal-length :petal-width]
   {:color :species})))


(deftest
 t241_l1437
 (is
  ((fn
    [pose]
    (and
     (= :vertical (get-in pose [:layout :direction]))
     (= #{:y :x} (get-in pose [:opts :share-scales]))
     (= 2 (count (:poses pose)))
     (every?
      (fn* [p1__76179#] (= 2 (count (:poses p1__76179#))))
      (:poses pose))
     (= {:color :species} (:mapping pose))
     (=
      [[{:x :sepal-length, :y :petal-length}
        {:x :sepal-width, :y :petal-length}]
       [{:x :sepal-length, :y :petal-width}
        {:x :sepal-width, :y :petal-width}]]
      (mapv (fn [row] (mapv :mapping (:poses row))) (:poses pose)))))
   v240_l1432)))


(def v243_l1466 (pj/cross [:a :b] [:c :d]))


(deftest
 t244_l1468
 (is
  ((fn [pairs] (= [[:a :c] [:a :d] [:b :c] [:b :d]] pairs))
   v243_l1466)))
