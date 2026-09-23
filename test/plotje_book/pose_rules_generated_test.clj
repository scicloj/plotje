(ns
 plotje-book.pose-rules-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l32 (def iris (rdatasets/datasets-iris)))


(def
 v5_l38
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
 v6_l43
 (defn
  pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose))))


(def v8_l91 (-> iris (pj/pose :sepal-length :sepal-width)))


(deftest
 t9_l94
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v8_l91)))


(def
 v10_l96
 (-> iris (pj/pose :sepal-length :sepal-width) pose-summary))


(deftest
 t11_l100
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (= [] (:layers pose))
     (not (contains? pose :poses))))
   v10_l96)))


(def v13_l110 (-> iris (pj/pose {:color :species}) pose-summary))


(deftest
 t14_l114
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (not (contains? pose :poses))))
   v13_l110)))


(def v16_l126 (-> iris pj/pose (pj/pose :sepal-length :sepal-width)))


(deftest
 t17_l130
 (is
  ((fn
    [pose]
    (and
     (= {:x :sepal-length, :y :sepal-width} (:mapping pose))
     (not (contains? pose :poses))))
   v16_l126)))


(def
 v19_l137
 (->
  iris
  (pj/pose {:color :species})
  (pj/pose :sepal-length :sepal-width)))


(deftest
 t20_l141
 (is
  ((fn
    [pose]
    (=
     {:x :sepal-length, :y :sepal-width, :color :species}
     (:mapping pose)))
   v19_l137)))


(def
 v22_l149
 (=
  (->
   iris
   pj/pose
   (pj/pose {:color :species})
   (pj/pose :sepal-length :sepal-width))
  (pj/pose iris :sepal-length :sepal-width {:color :species})))


(deftest t23_l155 (is (true? v22_l149)))


(def
 v25_l166
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)))


(deftest
 t26_l170
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (:poses pose)))
     (=
      {:x :sepal-length, :y :sepal-width}
      (:mapping (first (:poses pose))))
     (=
      {:x :petal-length, :y :petal-width}
      (:mapping (second (:poses pose))))))
   v25_l166)))


(def
 v28_l182
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/pose :petal-length :petal-width)))


(def
 v29_l186
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/pose :petal-length :petal-width)
  pose-summary))


(deftest
 t30_l191
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (=
      {:x :sepal-length, :y :sepal-width}
      (:mapping (first (:poses pose))))
     (=
      {:x :petal-length, :y :petal-width}
      (:mapping (second (:poses pose))))))
   v29_l186)))


(def
 v32_l203
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/options {:title "Iris"})
  (pj/pose :petal-length :petal-width)))


(def
 v34_l211
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/options {:title "Iris"})
  (pj/pose :petal-length :petal-width)
  pose-summary))


(deftest
 t35_l217
 (is
  ((fn
    [pose]
    (and
     (= "Iris" (get-in pose [:opts :title]))
     (not (contains? (first (:poses pose)) :opts))))
   v34_l211)))


(def
 v37_l230
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose {:color :species})))


(deftest
 t38_l234
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:poses pose)))
     (= {:color :species} (:mapping pose))
     (=
      {:x :sepal-length, :y :sepal-width}
      (:mapping (first (:poses pose))))))
   v37_l230)))


(def
 v40_l247
 (=
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/pose {:color :species})
   (pj/pose :petal-length :petal-width))
  (->
   iris
   (pj/pose :sepal-length :sepal-width {:color :species})
   (pj/pose :petal-length :petal-width))))


(deftest t41_l255 (is (true? v40_l247)))


(def
 v43_l266
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/pose :petal-length :petal-width)))


(deftest
 t44_l271
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (= 2 (count (:poses pose)))
     (= [] (:layers (first (:poses pose))))
     (= [] (:layers (second (:poses pose))))))
   v43_l266)))


(def
 v46_l284
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)))


(deftest
 t47_l289
 (is
  ((fn
    [pose]
    (and
     (or (not (contains? pose :layers)) (= [] (:layers pose)))
     (= 1 (count (:layers (first (:poses pose)))))
     (= [] (:layers (second (:poses pose))))))
   v46_l284)))


(def
 v49_l305
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/pose :sepal-length :petal-length)))


(deftest
 t50_l310
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (:poses pose)))
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}
       {:x :sepal-length, :y :petal-length}]
      (mapv :mapping (:poses pose)))))
   v49_l305)))


(def
 v52_l321
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/pose {:color :species})))


(def
 v53_l326
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/pose {:color :species})
  pose-summary))


(deftest
 t54_l332
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (:poses pose)))
     (= {:color :species} (:mapping pose))
     (=
      {:x :sepal-length, :y :sepal-width}
      (:mapping (first (:poses pose))))))
   v53_l326)))


(def
 v56_l347
 (def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width))))


(def v57_l349 leaf-pose)


(def v58_l351 (pose-summary leaf-pose))


(def v60_l355 (= leaf-pose (pj/pose leaf-pose)))


(deftest t61_l357 (is (true? v60_l355)))


(def
 v63_l361
 (def
  composite-pose
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/pose :petal-length :petal-width))))


(def v64_l366 composite-pose)


(def v65_l368 (pose-summary composite-pose))


(def v67_l372 (= composite-pose (pj/pose composite-pose)))


(deftest t68_l374 (is (true? v67_l372)))


(def
 v70_l383
 (pj/arrange
  [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
   (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)]))


(deftest
 t71_l387
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= :vertical (get-in pose [:layout :direction]))
     (= 1 (count (:poses pose)))
     (= 2 (count (:poses (first (:poses pose)))))))
   v70_l383)))


(def
 v73_l400
 (->
  (pj/arrange
   [(pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    (pj/pose iris :sepal-length :petal-length)])
  pj/lay-point))


(deftest
 t74_l405
 (is
  ((fn
    [pose]
    (and
     (= 3 (:panels (pj/svg-summary pose)))
     (seq (:poses (first (:poses (first (:poses pose))))))))
   v73_l400)))


(def
 v76_l415
 (pj/arrange
  [(pj/pose iris :sepal-length :sepal-width)
   (pj/pose iris :petal-length :petal-width)]
  {:title "Arranged", :share-scales #{:y}}))


(deftest
 t77_l421
 (is
  ((fn
    [pose]
    (and
     (= "Arranged" (get-in pose [:opts :title]))
     (= #{:y} (get-in pose [:opts :share-scales]))))
   v76_l415)))


(def
 v79_l437
 (->
  iris
  (pj/pose
   (pj/cross [:sepal-length :sepal-width] [:petal-length :petal-width])
   {:color :species})))


(deftest
 t80_l442
 (is
  ((fn
    [pose]
    (and
     (= {:color :species} (:mapping pose))
     (= 2 (count (:poses pose)))
     (every?
      (fn* [p1__11194#] (= 2 (count (:poses p1__11194#))))
      (:poses pose))))
   v79_l437)))


(def
 v82_l451
 (let
  [a
   (->
    iris
    (pj/pose {:color :species})
    (pj/pose
     (pj/cross
      [:sepal-length :sepal-width]
      [:petal-length :petal-width])))
   b
   (->
    iris
    (pj/pose
     (pj/cross
      [:sepal-length :sepal-width]
      [:petal-length :petal-width])
     {:color :species}))]
  (= a b)))


(deftest t83_l461 (is (true? v82_l451)))


(def
 v85_l487
 (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point))


(deftest
 t86_l491
 (is
  ((fn
    [pose]
    (and
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))
     (empty? (or (:mapping (first (:layers pose))) {}))))
   v85_l487)))


(def
 v88_l500
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(def
 v89_l505
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  pose-summary))


(deftest
 t90_l511
 (is
  ((fn
    [pose]
    (and
     (contains? pose :poses)
     (= 1 (count (:layers pose)))
     (= :point (:layer-type (first (:layers pose))))))
   v89_l505)))


(def
 v92_l523
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


(deftest t93_l533 (is ((fn [counts] (= [0 1] counts)) v92_l523)))


(def
 v95_l548
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t96_l553
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (:poses pose)))
     (= 1 (count (:layers (first (:poses pose)))))
     (= 0 (count (:layers (second (:poses pose)))))
     (= :point (:layer-type (first (:layers (first (:poses pose))))))))
   v95_l548)))


(def
 v98_l569
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t99_l573
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
   v98_l569)))


(def
 v101_l594
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/pose :X)
  (pj/lay-line {:y :Y})
  (pj/lay-line {:y :Z})))


(deftest
 t102_l599
 (is
  ((fn
    [pose]
    (and
     (nil? (:poses pose))
     (= [{:y :Y} {:x :X, :y :Z}] (mapv :mapping (:layers pose)))
     (= 2 (:panels (pj/svg-summary pose)))))
   v101_l594)))


(def
 v104_l611
 (->
  {:X [1 2 3 4 5], :Y [1 2 3 4 5], :Z [1 4 9 16 25]}
  (pj/lay-point :X :Y)
  (pj/lay-text {:x 2, :y 4, :text "a note"})))


(deftest
 t105_l615
 (is
  ((fn [pose] (and (nil? (:poses pose)) (= 2 (count (:layers pose)))))
   v104_l611)))


(def
 v107_l632
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/lay-point :sepal-length :petal-length)))


(deftest
 t108_l637
 (is
  ((fn
    [pose]
    (and
     (= 3 (count (:poses pose)))
     (=
      {:x :sepal-length, :y :petal-length}
      (:mapping (nth (:poses pose) 2)))
     (= 1 (count (:layers (nth (:poses pose) 2))))))
   v107_l632)))


(def
 v110_l651
 (let
  [via-lay
   (->
    iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))
   via-pose
   (->
    iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))
   drawn
   (fn [pose] (select-keys (pj/svg-summary pose) [:panels :points]))]
  {:via-lay-drawn (drawn via-lay),
   :via-pose-drawn (drawn via-pose),
   :via-lay-shape (if (:poses via-lay) :composite :leaf),
   :via-pose-shape (if (:poses via-pose) :composite :leaf)}))


(deftest
 t111_l665
 (is
  ((fn
    [m]
    (and
     (= (:via-lay-drawn m) (:via-pose-drawn m))
     (= :leaf (:via-lay-shape m))
     (= :composite (:via-pose-shape m))))
   v110_l651)))


(def
 v113_l679
 (try
  (->
   iris
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t114_l686
 (is
  ((fn
    [msg]
    (and
     (string? msg)
     (re-find #"doesn't exist in the data" msg)
     (re-find #"panel of its own" msg)))
   v113_l679)))


(def
 v116_l695
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point
   :foo
   :bar
   {:data (tc/dataset {:foo [1 2 3], :bar [4 5 6]})})))


(deftest
 t117_l700
 (is
  ((fn
    [fr]
    (and
     (nil? (:poses fr))
     (= [{:x :foo, :y :bar}] (mapv :mapping (:layers fr)))
     (= 2 (:panels (pj/svg-summary fr)))))
   v116_l695)))


(def v119_l715 (def tiny {:a [1 2 3 4 5], :b [2 4 3 5 4]}))


(def v120_l719 (-> tiny (pj/lay-point :a :b)))


(deftest
 t121_l722
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v120_l719)))


(def v123_l726 (-> tiny (pj/pose :a :b) pj/lay-point pose-summary))


(deftest
 t124_l731
 (is
  ((fn
    [pose]
    (and
     (= {:x :a, :y :b} (:mapping pose))
     (= 1 (count (:layers pose)))
     (not (contains? pose :poses))))
   v123_l726)))


(def
 v126_l751
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t127_l757
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
       (fn* [p1__11195#] (re-find #"petal" p1__11195#))
       (:texts s)))))
   v126_l751)))


(def
 v129_l780
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t130_l787
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
   v129_l780)))


(def
 v132_l800
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pj/overlay
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})))


(deftest
 t133_l808
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
   v132_l800)))


(def
 v135_l821
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pj/overlay
  (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"})
  pose-summary))


(deftest
 t136_l830
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (:poses pose)))
     (=
      [{:color "#377eb8"}]
      (mapv :mapping (:layers (first (:poses pose)))))
     (=
      [{:color "#e6550d"}
       {:color "#4daf4a", :x :sepal-width, :y :petal-width}]
      (mapv :mapping (:layers (second (:poses pose)))))))
   v135_l821)))


(def
 v138_l843
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
 t139_l849
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v138_l843)))


(def
 v141_l856
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/overlay false)
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})))


(deftest
 t142_l863
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v141_l856)))


(def
 v144_l875
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
  pj/overlay
  (pj/overlay false)
  (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
  pose-summary))


(deftest
 t145_l883
 (is
  ((fn
    [pose]
    (and
     (not (contains? pose :overlay))
     (nil? (:poses pose))
     (=
      [{:x :petal-length, :y :petal-width, :color "#e6550d"}]
      (mapv :mapping (rest (:layers pose))))))
   v144_l875)))


(def
 v147_l893
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


(deftest t148_l906 (is ((fn [v] (= [1 1] v)) v147_l893)))


(def
 v150_l934
 (->
  (tc/dataset
   {:height [1 2 3], :weight [4 5 6], :species ["a" "b" "a"]})
  pj/lay-point))


(deftest
 t151_l937
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v150_l934)))


(def
 v153_l942
 (try
  (->
   (tc/dataset {:a [1 2], :b [3 4], :c [5 6], :d [7 8]})
   pj/lay-point)
  (catch Exception e (ex-message e))))


(deftest
 t154_l948
 (is ((fn [msg] (re-find #"Cannot auto-infer columns" msg)) v153_l942)))


(def
 v156_l969
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


(def v157_l978 s1-composite)


(deftest
 t158_l980
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
   v157_l978)))


(def
 v160_l992
 (def
  s1-siblings
  (pj/pose
   {:poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data iris})))


(def v161_l1000 s1-siblings)


(deftest
 t162_l1002
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
   v161_l1000)))


(def
 v164_l1020
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


(def v165_l1029 s2-tree)


(deftest
 t166_l1031
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
   v165_l1029)))


(def
 v168_l1048
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:color nil, :stat :linear-model})))


(deftest
 t169_l1053
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v168_l1048)))


(def
 v171_l1067
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t172_l1072
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v171_l1067)))


(def
 v174_l1093
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "Iris"})))


(deftest
 t175_l1098
 (is ((fn [pose] (= "Iris" (get-in pose [:opts :title]))) v174_l1093)))


(def
 v177_l1103
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/options {:title "One"})
  (pj/options {:title "Two", :subtitle "Sub"})))


(deftest
 t178_l1109
 (is
  ((fn
    [pose]
    (and
     (= "Two" (get-in pose [:opts :title]))
     (= "Sub" (get-in pose [:opts :subtitle]))))
   v177_l1103)))


(def
 v180_l1128
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/scale :x :log)
  (pj/coord :flip)))


(deftest
 t181_l1134
 (is
  ((fn
    [pose]
    (and
     (= {:type :log} (get-in pose [:mapping :x :scale]))
     (= :flip (get-in pose [:opts :coord]))))
   v180_l1128)))


(def
 v183_l1144
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:size :petal-length})
  pj/lay-point
  (pj/scale :size :log)))


(deftest
 t184_l1149
 (is
  ((fn
    [pose]
    (=
     {:from :petal-length, :scale {:type :log}}
     (get-in pose [:mapping :size])))
   v183_l1144)))


(def
 v186_l1157
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:size :petal-length})
  (pj/scale :size :log)))


(deftest
 t187_l1162
 (is
  ((fn
    [pose]
    (and
     (= {:scale {:type :log}} (get-in pose [:mapping :size]))
     (=
      {:type :log}
      (-> pose pj/plan :panels first :layers first :size-scale))))
   v186_l1157)))


(def
 v189_l1178
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t190_l1183
 (is
  ((fn [pose] (= :species (get-in pose [:mapping :col]))) v189_l1178)))


(def
 v192_l1188
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet-grid :species :species)))


(deftest
 t193_l1193
 (is
  ((fn
    [pose]
    (and
     (= :species (get-in pose [:mapping :col]))
     (= :species (get-in pose [:mapping :row]))))
   v192_l1188)))


(def
 v195_l1202
 (->
  (pj/arrange
   [(pj/pose iris :sepal-length :sepal-width)
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point
  (pj/facet :species)))


(deftest
 t196_l1207
 (is ((fn [pose] (= 6 (:panels (pj/svg-summary pose)))) v195_l1202)))


(def
 v197_l1210
 (->
  (pj/arrange
   [(-> (pj/pose iris :sepal-length :sepal-width) (pj/facet :species))
    (pj/pose iris :petal-length :petal-width)])
  pj/lay-point))


(deftest
 t198_l1215
 (is ((fn [pose] (= 4 (:panels (pj/svg-summary pose)))) v197_l1210)))


(def
 v200_l1229
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t201_l1234
 (is
  ((fn
    [pose]
    (let
     [layers
      (:layers pose)
      rule
      (some
       (fn*
        [p1__11196#]
        (when (= :rule-h (:layer-type p1__11196#)) p1__11196#))
       layers)]
     (and (some? rule) (= 3.0 (get-in rule [:mapping :y-intercept])))))
   v200_l1229)))


(def
 v203_l1244
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0})))


(deftest
 t204_l1249
 (is
  ((fn
    [pose]
    (and
     (= 2 (count (:poses pose)))
     (= 1 (count (:layers (first (:poses pose)))))
     (= 0 (count (:layers (second (:poses pose)))))
     (=
      :rule-h
      (:layer-type (first (:layers (first (:poses pose))))))))
   v203_l1244)))


(def
 v206_l1270
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t207_l1277
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
   v206_l1270)))


(def
 v209_l1294
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point))


(deftest
 t210_l1298
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
   v209_l1294)))


(def
 v212_l1328
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  (pj/pose :petal-length :petal-width)
  pj/lay-point))


(deftest
 t213_l1333
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose)]
     (and (:composite? plan) (= 2 (count (:sub-plots plan))))))
   v212_l1328)))


(def
 v215_l1341
 (->
  iris
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)))


(deftest
 t216_l1345
 (is
  ((fn
    [pose]
    (and (nil? (:poses pose)) (= 2 (:panels (pj/svg-summary pose)))))
   v215_l1341)))


(def
 v218_l1358
 (->
  iris
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t219_l1363
 (is
  ((fn
    [pose]
    (let
     [plan (pj/plan pose) panel (first (:panels plan))]
     (and (= 1 (count (:panels plan))) (= 2 (count (:layers panel))))))
   v218_l1358)))


(def
 v221_l1376
 (->
  iris
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/facet :species)))


(deftest
 t222_l1381
 (is ((fn [pose] (= 3 (count (:panels (pj/plan pose))))) v221_l1376)))


(def
 v224_l1401
 (def
  l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}})))


(def v225_l1407 l4-shared)


(deftest
 t226_l1409
 (is
  ((fn
    [pose]
    (let
     [x-domains
      (fn
       [p]
       (mapv
        (fn*
         [p1__11197#]
         (get-in p1__11197# [:plan :panels 0 :x-domain]))
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
           (fn* [p1__11198#] (= "setosa" (:species p1__11198#))))
          (pj/pose :sepal-length :sepal-width)
          pj/lay-point)
         (->
          iris
          (tc/select-rows
           (fn* [p1__11199#] (= "virginica" (:species p1__11199#))))
          (pj/pose :sepal-length :petal-width)
          pj/lay-point)]
        (if share {:share-scales #{:x}} {})))]
     (and
      (= 2 (count domains))
      (every? some? domains)
      (apply = domains)
      (apply = (x-domains (cells true)))
      (apply not= (x-domains (cells false))))))
   v225_l1407)))


(def
 v228_l1469
 (->
  iris
  (pj/pose
   (pj/cross [:sepal-length :sepal-width] [:petal-length :petal-width])
   {:color :species})))


(deftest
 t229_l1474
 (is
  ((fn
    [pose]
    (and
     (= :vertical (get-in pose [:layout :direction]))
     (= #{:y :x} (get-in pose [:opts :share-scales]))
     (= 2 (count (:poses pose)))
     (every?
      (fn* [p1__11200#] (= 2 (count (:poses p1__11200#))))
      (:poses pose))
     (= {:color :species} (:mapping pose))
     (=
      [[{:x :sepal-length, :y :petal-length}
        {:x :sepal-width, :y :petal-length}]
       [{:x :sepal-length, :y :petal-width}
        {:x :sepal-width, :y :petal-width}]]
      (mapv (fn [row] (mapv :mapping (:poses row))) (:poses pose)))))
   v228_l1469)))


(def v231_l1503 (pj/cross [:a :b] [:c :d]))


(deftest
 t232_l1505
 (is
  ((fn [pairs] (= [[:a :c] [:a :d] [:b :c] [:b :d]] pairs))
   v231_l1503)))
