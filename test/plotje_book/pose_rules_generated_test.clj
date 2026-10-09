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
    (partial mapv (fn* [p1__11193#] (dissoc p1__11193# :data))))
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
   (fn* [p1__11194#] (not (contains? p1__11194# :poses)))
   (rest (tree-seq :poses :poses pose)))))


(def v10_l90 (-> iris (pj/pose :sepal-length :sepal-width)))


(deftest
 t11_l93
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v10_l90)))


(def
 v12_l95
 (-> iris (pj/pose :sepal-length :sepal-width) pose-summary))


(deftest
 t13_l99
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (= [] (:layers pose))
     (not (contains? pose :poses))))
   v12_l95)))


(def v15_l109 (-> iris (pj/pose {:color :species}) pose-summary))


(deftest
 t16_l113
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (not (contains? pose :poses))))
   v15_l109)))


(def v18_l125 (-> iris pj/pose (pj/pose :sepal-length :sepal-width)))


(deftest
 t19_l129
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (not (contains? pose :poses))))
   v18_l125)))


(def
 v21_l136
 (->
  iris
  (pj/pose {:color :species})
  (pj/pose :sepal-length :sepal-width)))


(deftest
 t22_l140
 (is
  ((fn
    [pose]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping pose)))
   v21_l136)))


(def
 v24_l148
 (=
  (->
   iris
   pj/pose
   (pj/pose {:color :species})
   (pj/pose :sepal-length :sepal-width))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t25_l154 (is (true? v24_l148)))


(def
 v27_l164
 (defn
  trend-plot
  [data]
  (->
   data
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/lay-smooth)))


(def
 v28_l170
 (-> (trend-plot iris) (pj/pose :petal-length :petal-width)))


(def
 v29_l173
 (->
  (trend-plot iris)
  (pj/pose :petal-length :petal-width)
  pose-summary))


(deftest
 t30_l177
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (= {:x :petal-length, :y :petal-width} (:mapping pose))
     (= [{:layer-type :point} {:layer-type :smooth}] (:layers pose))))
   v29_l173)))


(def
 v32_l185
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point))


(deftest
 t33_l189
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v32_l185)))


(def
 v35_l195
 (=
  (trend-plot iris)
  (pj/pose (trend-plot iris) :sepal-length :sepal-width)))


(deftest t36_l198 (is (true? v35_l195)))


(def
 v38_l205
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})))


(def
 v39_l209
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t40_l214
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :poses))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v39_l209)))


(def
 v42_l223
 (=
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/pose {:color :species}))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t43_l228 (is (true? v42_l223)))


(def
 v45_l236
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})))


(def
 v46_l241
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t47_l247
 (is
  ((fn
    [pose]
    (and
     (= [{:layer-type :point}] (:layers pose))
     (=
      {:x :sepal-length, :y :sepal-width, :color :species}
      (:mapping pose))))
   v46_l241)))


(def
 v49_l259
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose {:color :species})
  pj/lay-point))


(def
 v50_l264
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose {:color :species})
  pj/lay-point
  pose-summary))


(deftest
 t51_l270
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}]
      (mapv :mapping (cells pose)))))
   v50_l264)))


(def
 v53_l281
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/pose :sepal-length :petal-length)
  pj/lay-point))


(deftest
 t54_l286
 (is
  ((fn [pose] (re-find #"not drawn" (with-out-str (pj/plan pose))))
   v53_l281)))


(def
 v56_l299
 (def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width))))


(def v57_l301 leaf-pose)


(def v58_l303 (pose-summary leaf-pose))


(def v60_l307 (= leaf-pose (pj/pose leaf-pose) (pj/pose leaf-pose {})))


(deftest t61_l311 (is (true? v60_l307)))


(def
 v62_l313
 (=
  (pj/pose leaf-pose {:color :species})
  (pj/pose (pj/pose leaf-pose {:color :species}) {:color :species})))


(deftest t63_l316 (is (true? v62_l313)))


(def
 v65_l320
 (def
  composite-pose
  (->
   iris
   (pj/arrange
    [[:sepal-length :sepal-width] [:petal-length :petal-width]])
   pj/lay-point)))


(def v66_l325 composite-pose)


(def v67_l327 (pose-summary composite-pose))


(def
 v69_l331
 (=
  composite-pose
  (pj/pose composite-pose)
  (pj/pose composite-pose {})))


(deftest t70_l335 (is (true? v69_l331)))


(def
 v72_l344
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t73_l348
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= :vertical (get-in pose [:layout :direction]))
     (= 1 (count (:poses pose)))
     (= 2 (count (:poses (first (:poses pose)))))))
   v72_l344)))


(def
 v75_l361
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t76_l366
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v75_l361)))


(def
 v78_l376
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :petal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t79_l382
 (is
  ((fn
    [pose]
    (and
     (= "Arranged" (get-in pose [:opts :title]))
     (= #{:y} (get-in pose [:opts :share-scales]))))
   v78_l376)))


(def
 v81_l393
 (->
  iris
  (pj/cross-matrix [:sepal-length :sepal-width] {:color :species})))


(deftest
 t82_l397
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (= 2 (count (:poses pose)))
     (every?
      (fn* [p1__11195#] (= 2 (count (:poses p1__11195#))))
      (:poses pose))))
   v81_l393)))


(def
 v84_l406
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


(deftest t85_l414 (is (true? v84_l406)))


(def
 v87_l438
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t88_l442
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v87_l438)))


(def
 v90_l451
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v91_l456
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t92_l462
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v91_l456)))


(def
 v94_l474
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


(deftest t95_l484 (is ((fn [counts] (= [0 1] counts)) v94_l474)))


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
 v100_l519
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t101_l523
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= {:x :sepal-length, :y :sepal-width} (:mapping fr))
     (=
      [{:x :petal-length, :y :petal-width}]
      (mapv :mapping (:layers fr)))
     (= 2 (:panels (pj/svg-summary fr)))))
   v100_l519)))


(def
 v103_l544
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t104_l549
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
     (= 2 (:panels (pj/svg-summary pose)))))
   v103_l544)))


(def
 v106_l561
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t107_l565
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v106_l561)))


(def
 v109_l582
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t110_l586
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (cells pose)))
     (nil? (:mapping (nth (cells pose) 2)))
     (=
      [{:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:layers (nth (cells pose) 2))))))
   v109_l582)))


(def
 v112_l600
 (let
  [via-lay
   (->
    iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))
   via-pose
   (->
    iris
    (pj/arrange
     [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))
   drawn
   (fn [pose] (select-keys (pj/svg-summary pose) [:panels :points]))]
  {:via-lay-drawn (drawn via-lay),
   :via-pose-drawn (drawn via-pose),
   :via-lay-shape (if (:poses via-lay) :composite :leaf),
   :via-pose-shape (if (:poses via-pose) :composite :leaf)}))


(deftest
 t113_l613
 (is
  ((fn
    [m]
    (and
     (= (:via-lay-drawn m) (:via-pose-drawn m))
     (= :leaf (:via-lay-shape m))
     (= :composite (:via-pose-shape m))))
   v112_l600)))


(def
 v115_l627
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t116_l634
 (is
  ((fn
    [msg]
    (and
     (string? msg)
     (re-find #"doesn't exist in the data" msg)
     (re-find #"panel of its own" msg)))
   v115_l627)))


(def
 v118_l643
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t119_l648
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (= 2 (:panels (pj/svg-summary fr)))))
   v118_l643)))


(def v121_l662 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v122_l666 (-> tiny (pj/lay-point :a :b)))


(deftest
 t123_l669
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v122_l666)))


(def v125_l676 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t126_l681
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v125_l676)))


(def
 v128_l701
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t129_l707
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
      (= #{"sepal length" "sepal width"} (set axis-titles))
      (not-any?
       (fn* [p1__11196#] (re-find #"petal" p1__11196#))
       (:texts s)))))
   v128_l701)))


(def
 v131_l730
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t132_l737
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
   v131_l730)))


(def
 v134_l750
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pj/overlay
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t135_l757
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 450 (:points s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
       (disj (:colors s) "none")))))
   v134_l750)))


(def
 v137_l769
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pj/overlay
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})
  pose-summary))


(deftest
 t138_l777
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (=
      [{:color "#377eb8", :x :sepal-length, :y :sepal-width}]
      (mapv :mapping (:layers (first (cells pose)))))
     (=
      [{:color "#e6550d", :x :petal-length, :y :petal-width}
       {:color "#4daf4a", :x :sepal-width, :y :petal-width}]
      (mapv :mapping (:layers (second (cells pose)))))))
   v137_l769)))


(def
 v140_l790
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/lay-point
   :petal-length
   :petal-width
   {:color "#e6550d", :overlay false})))


(deftest
 t141_l796
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v140_l790)))


(def
 v143_l803
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/overlay false)
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t144_l810
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v143_l803)))


(def
 v146_l822
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/overlay false)
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t147_l830
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :overlay))
     (nil? (:poses pose))
     (=
      [{:x :petal-length, :y :petal-width, :color "#e6550d"}]
      (mapv :mapping (rest (:layers pose))))))
   v146_l822)))


(def
 v149_l840
 [(->
   iris
   pj/overlay
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width)
   pj/svg-summary
   :panels)
  (->
   iris
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width)
   pj/overlay
   pj/svg-summary
   :panels)])


(deftest t150_l853 (is ((fn [v] (= [1 1] v)) v149_l840)))


(def
 v152_l881
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t153_l884
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v152_l881)))


(def
 v155_l889
 (try
  (->
   (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
   pj/lay-point)
  (catch Exception e (ex-message e))))


(deftest
 t156_l895
 (is ((fn [msg] (re-find #"Cannot auto-infer columns" msg)) v155_l889)))


(def
 v158_l916
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


(def v159_l925 s1-composite)


(deftest
 t160_l927
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
   v159_l925)))


(def
 v162_l939
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v163_l947 s1-siblings)


(deftest
 t164_l949
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
   v163_l947)))


(def
 v166_l967
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


(def v167_l976 s2-tree)


(deftest
 t168_l978
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
   v167_l976)))


(def
 v170_l995
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t171_l1000
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v170_l995)))


(def
 v173_l1014
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t174_l1019
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v173_l1014)))


(def
 v176_l1040
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t177_l1045
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v176_l1040)))


(def
 v179_l1050
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t180_l1056
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v179_l1050)))


(def
 v182_l1075
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t183_l1081
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v182_l1075)))


(def
 v185_l1091
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t186_l1096
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v185_l1091)))


(def
 v188_l1104
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t189_l1109
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v188_l1104)))


(def
 v191_l1125
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t192_l1130
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v191_l1125)))


(def
 v194_l1135
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet-grid :species :species)))


(deftest
 t195_l1140
 (is
  ((fn
    [pose]
    (and
     (= :species (get-in pose [:mapping :col]))
     (= :species (get-in pose [:mapping :row]))))
   v194_l1135)))


(def
 v197_l1149
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t198_l1154
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v197_l1149)))


(def
 v199_l1157
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t200_l1162
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v199_l1157)))


(def
 v202_l1176
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t203_l1181
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__11197#]
        (when (= :rule-h (:layer-type p1__11197#)) p1__11197#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v202_l1176)))


(def
 v205_l1191
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t206_l1195
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (cells pose)))
     (= 1 (count (:layers (first (cells pose)))))
     (= 0 (count (:layers (second (cells pose)))))
     (= :rule-h (:layer-type (first (:layers (first (cells pose))))))))
   v205_l1191)))


(def
 v208_l1216
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t209_l1222
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
   v208_l1216)))


(def
 v211_l1239
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t212_l1243
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
   v211_l1239)))


(def
 v214_l1273
 (->
  iris
  (pj/arrange
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  pj/lay-point))


(deftest
 t215_l1277
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose)]
     (and (:composite? plan) (= 2 (count (:sub-plots plan))))))
   v214_l1273)))


(def
 v217_l1285
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t218_l1289
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 2 (:panels (pj/svg-summary pose)))))
   v217_l1285)))


(def
 v220_l1302
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t221_l1307
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v220_l1302)))


(def
 v223_l1320
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t224_l1325
 (is ((fn [pose] (= 3 (count (:panels (pj/plan pose))))) v223_l1320)))


(def
 v226_l1345
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v227_l1351 l4-shared)


(deftest
 t228_l1353
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__11198#]
         (get-in p1__11198# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__11199#] (= "setosa" (:species p1__11199#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__11200#] (= "virginica" (:species p1__11200#))))
          (pj/pose :sepal-length :petal-width)
          pj/lay-point)]
        (if share {:share-scales #{:x}} {})))]
     (and
      (= 2 (count domains))
      (every? some? domains)
      (apply = domains)
      (apply = (x-domains (cells true)))
      (apply not= (x-domains (cells false))))))
   v227_l1351)))


(def
 v230_l1413
 (->
  iris
  (pj/cross-matrix
   [:sepal-length :sepal-width]
   [:petal-length :petal-width]
   {:color :species})))


(deftest
 t231_l1418
 (is
  ((fn
    [pose]
    (and
     (= :vertical (get-in pose [:layout :direction]))
     (= #{:y :x} (get-in pose [:opts :share-scales]))
     (= 2 (count (:poses pose)))
     (every?
      (fn* [p1__11201#] (= 2 (count (:poses p1__11201#))))
      (:poses pose))
     (= {:color :species} (:mapping pose))
     (=
      [[{:x :sepal-length, :y :petal-length}
        {:x :sepal-width, :y :petal-length}]
       [{:x :sepal-length, :y :petal-width}
        {:x :sepal-width, :y :petal-width}]]
      (mapv (fn [row] (mapv :mapping (:poses row))) (:poses pose)))))
   v230_l1413)))


(def v233_l1447 (pj/cross [:a :b] [:c :d]))


(deftest
 t234_l1449
 (is
  ((fn [pairs] (= [[:a :c] [:a :d] [:b :c] [:b :d]] pairs))
   v233_l1447)))
