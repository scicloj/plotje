(ns
 plotje-book.inference-rules-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l32
 (def five-points {:x [1.0 2.0 3.0 4.0 5.0], :y [2.1 4.3 3.0 5.2 4.8]}))


(def v4_l36 (def scatter-pose (-> five-points (pj/lay-point :x :y))))


(def v5_l40 scatter-pose)


(deftest
 t6_l42
 (is
  ((fn
    [v]
    (let
     [plan
      (pj/plan scatter-pose)
      p
      (first (:panels plan))
      g
      (first (:groups (first (:layers p))))
      default-hex
      (:default-color (pj/config))
      explicit-default-color
      (->
       {:x [1], :y [1]}
       (pj/lay-point :x :y {:color default-hex})
       pj/plan
       :panels
       first
       :layers
       first
       :groups
       first
       :color)]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= "#333" (:default-color (pj/config)))
      (= :single (:layout-type plan))
      (= 1 (count (:panels plan)))
      (= "x" (:x-label plan))
      (= "y" (:y-label plan))
      (nil? (:legend plan))
      (zero? (get-in plan [:layout :legend-w]))
      (= :linear (get-in p [:x-scale :type]))
      (= 1 (count (:groups (first (:layers p)))))
      (= explicit-default-color (:color g)))))
   v5_l40)))


(def v8_l136 (-> {:values [1 2 3 4 5 6]} pj/lay-histogram))


(deftest
 t9_l139
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v8_l136)))


(def v11_l143 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t12_l146
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v11_l143)))


(def
 v14_l150
 (-> {:x [1 2 3 4], :y [4 5 6 7], :g ["a" "a" "b" "b"]} pj/lay-point))


(deftest
 t15_l153
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (some #{"a"} (:texts s)))))
   v14_l150)))


(def
 v17_l164
 (def
  two-col-pose
  (pj/pose {:x [1.0 2.0 3.0 4.0 5.0], :y [1.0 4.0 9.0 16.0 25.0]})))


(def v18_l168 two-col-pose)


(deftest
 t19_l170
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l168)))


(def
 v21_l175
 (->
  two-col-pose
  pj/infer-mapping
  (select-keys [:mapping :layers])
  kind/pprint))


(deftest
 t22_l177
 (is
  ((fn
    [pose]
    (and (= {:x :x, :y :y} (:mapping pose)) (empty? (:layers pose))))
   v21_l175)))


(def
 v24_l189
 (def
  seedlings
  {:day [1 2 3 4 5 6 1 2 3 4 5 6],
   :height [2.0 3.1 3.9 5.2 6.1 6.8 1.5 2.1 2.4 3.3 3.6 4.4],
   :plot
   ["north"
    "north"
    "north"
    "north"
    "north"
    "north"
    "south"
    "south"
    "south"
    "south"
    "south"
    "south"]}))


(def v25_l195 (-> seedlings pj/lay-point (pj/lay-smooth :day :height)))


(deftest
 t26_l199
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 2 (:lines s))
      (some #{"north"} (:texts s))
      (= :plot (:color (:mapping (pj/infer-mapping v)))))))
   v25_l195)))


(def v28_l208 (-> seedlings pj/lay-point (pj/lay-rug :day)))


(deftest
 t29_l212
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 12 (:lines s))
      (some #{"north"} (:texts s)))))
   v28_l208)))


(def v31_l220 (-> seedlings (pj/lay-point :day :height) pj/lay-smooth))


(deftest
 t32_l224
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 1 (:lines s))
      (not (some #{"north"} (:texts s)))
      (nil? (:mapping (pj/infer-mapping v))))))
   v31_l220)))


(def
 v34_l243
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t35_l246
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v34_l243)))


(def
 v37_l264
 (def
  animals
  {:animal ["cat" "dog" "bird" "fish"], :count [12 8 15 5]}))


(def v38_l268 (def bar-pose (-> animals (pj/lay-bar :animal :count))))


(def v39_l272 bar-pose)


(deftest
 t40_l274
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= ["cat" "dog" "bird" "fish"] (:x-domain p))
      (true? (:categorical? (:x-ticks p))))))
   v39_l272)))


(def
 v42_l292
 (def
  temporal-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"
     #inst "2024-12-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/lay-point :date :val))))


(def v43_l297 temporal-pose)


(deftest
 t44_l299
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan temporal-pose)))]
     (and
      (number? (first (:x-domain p)))
      (= 6 (count (:values (:x-ticks p))))
      (= "Jan 2024" (first (:labels (:x-ticks p)))))))
   v43_l297)))


(def
 v46_l313
 (let
  [panel (first (:panels (pj/plan temporal-pose)))]
  {:x-scale (:x-scale panel),
   :x-domain (:x-domain panel),
   :x-tick-labels (:labels (:x-ticks panel))}))


(deftest
 t47_l318
 (is
  ((fn
    [m]
    (and
     (= {:type :linear} (:x-scale m))
     (every? number? (:x-domain m))
     (= 6 (count (:x-tick-labels m)))))
   v46_l313)))


(def
 v49_l331
 (let
  [labels
   (fn
    [a b]
    (->
     {:when [a b], :level [1 2]}
     (pj/lay-point :when :level)
     pj/plan
     :panels
     first
     :x-ticks
     :labels))]
  {:six-hours
   (labels
    #inst "2024-01-15T00:00:00.000-00:00"
    #inst "2024-01-15T06:00:00.000-00:00"),
   :three-days
   (labels
    #inst "2024-01-15T00:00:00.000-00:00"
    #inst "2024-01-18T00:00:00.000-00:00"),
   :nine-years
   (labels
    #inst "2015-01-15T00:00:00.000-00:00"
    #inst "2024-01-15T00:00:00.000-00:00")}))


(deftest
 t50_l339
 (is
  ((fn
    [m]
    (and
     (every?
      (fn* [p1__11199#] (re-matches #"\d{2}:\d{2}" p1__11199#))
      (:six-hours m))
     (every?
      (fn*
       [p1__11200#]
       (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__11200#))
      (:three-days m))
     (every?
      (fn* [p1__11201#] (re-matches #"\d{4}" p1__11201#))
      (:nine-years m))
     (= 9 (count (:nine-years m)))))
   v49_l331)))


(def
 v52_l363
 (def
  hour-bar-pose
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count {:x-type :categorical}))))


(def v53_l367 hour-bar-pose)


(deftest
 t54_l369
 (is
  ((fn
    [v]
    (and
     (= 4 (:polygons (pj/svg-summary v)))
     (=
      ["9" "10" "11" "12"]
      (:x-domain (first (:panels (pj/plan hour-bar-pose)))))))
   v53_l367)))


(def
 v56_l391
 (def
  colored-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g}))))


(def v57_l397 colored-pose)


(deftest
 t58_l399
 (is
  ((fn
    [v]
    (let
     [plan
      (pj/plan colored-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and
      (= 6 (:points (pj/svg-summary v)))
      (= 2 (count (:groups layer)))
      (some? (:legend plan))
      (= 100 (get-in plan [:layout :legend-w])))))
   v57_l397)))


(def
 v60_l417
 (def
  fixed-color-pose
  (-> five-points (pj/lay-point :x :y {:color "#E74C3C"}))))


(def v61_l421 fixed-color-pose)


(deftest
 t62_l423
 (is
  ((fn
    [v]
    (let
     [plan
      (pj/plan fixed-color-pose)
      layer
      (first (:layers (first (:panels plan))))
      c
      (:color (first (:groups layer)))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (nil? (:legend plan))
      (zero? (get-in plan [:layout :legend-w]))
      (= 1 (count (:groups layer)))
      (= [(/ 231.0 255.0) (/ 76.0 255.0) (/ 60.0 255.0) 1.0] c))))
   v61_l421)))


(def
 v64_l447
 (-> five-points (pj/lay-point :x :y {:color "steelblue"})))


(deftest
 t65_l450
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v64_l447)))


(def
 v67_l490
 (def
  red-color-pose
  (-> five-points (pj/lay-point :x :y {:color "red"}))))


(def v68_l494 red-color-pose)


(deftest
 t69_l496
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan red-color-pose)
      c
      (:color
       (first (:groups (first (:layers (first (:panels plan)))))))]
     (and (nil? (:legend plan)) (> (first c) 0.9))))
   v68_l494)))


(def v71_l529 colored-pose)


(deftest
 t72_l531
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan colored-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and
      (= 2 (count (:groups layer)))
      (= ["a" "b"] (mapv :label (:groups layer)))
      (some? (:legend plan)))))
   v71_l529)))


(def
 v74_l546
 (->
  {:x [1 2 3], :y [4 5 6], :hex ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-point :x :y {:color {:column :hex, :scale false}})))


(deftest
 t75_l549
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v74_l546)))


(def
 v77_l565
 (def
  numeric-color-pose
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4], :val [10 20 30 40 50]}
   (pj/lay-point :x :y {:color :val}))))


(def v78_l571 numeric-color-pose)


(deftest
 t79_l573
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan numeric-color-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and
      (= 1 (count (:groups layer)))
      (= :continuous (:type (:legend plan)))
      (= 20 (count (:stops (:legend plan)))))))
   v78_l571)))


(def
 v81_l592
 (def
  study-data
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}))


(def
 v83_l599
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v84_l603 study-continuous-pose)


(deftest
 t85_l605
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan study-continuous-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and
      (= 1 (count (:groups layer)))
      (= :continuous (:type (:legend plan))))))
   v84_l603)))


(def
 v87_l614
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v88_l619 study-categorical-pose)


(deftest
 t89_l621
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan study-categorical-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and
      (= 3 (count (:groups layer)))
      (= 3 (count (:entries (:legend plan)))))))
   v88_l619)))


(def
 v91_l633
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t92_l641
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v91_l633)))


(def
 v94_l651
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v95_l656
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v96_l660 explicit-group-pose)


(deftest
 t97_l662
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v96_l660)))


(def
 v99_l682
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t100_l687
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v99_l682)))


(def
 v102_l693
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t103_l698
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v102_l693)))


(def v105_l741 (def hist-pose (-> five-points (pj/pose :x))))


(def v106_l745 hist-pose)


(deftest
 t107_l747
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v106_l745)))


(def
 v109_l760
 (def
  temporal-hist-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"
     #inst "2024-04-01T00:00:00.000-00:00"
     #inst "2024-05-01T00:00:00.000-00:00"]}
   (pj/pose :date))))


(def v110_l765 temporal-hist-pose)


(deftest
 t111_l767
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v110_l765)))


(def v113_l775 (def count-pose (-> animals (pj/pose :animal))))


(def v114_l779 count-pose)


(deftest
 t115_l781
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v114_l779)))


(def
 v117_l794
 (def count-pose-on-y (-> animals (pj/pose {:y :animal}))))


(def v118_l798 count-pose-on-y)


(deftest
 t119_l800
 (is
  ((fn
    [v]
    (let
     [plan
      (pj/plan count-pose-on-y)
      layer
      (first (:layers (first (:panels plan))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer))
      (= "animal" (:y-label plan)))))
   v118_l798)))


(def v121_l818 (-> animals (pj/lay-point {:y :count})))


(deftest
 t122_l821
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:x-label plan))
      (= "count" (:y-label plan))
      (= 4 (:points (pj/svg-summary v))))))
   v121_l818)))


(def v124_l832 (-> animals (pj/lay-bar {:y :animal})))


(deftest
 t125_l835
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:y-label plan))
      (=
       (:polygons (pj/svg-summary count-pose-on-y))
       (:polygons (pj/svg-summary v))))))
   v124_l832)))


(def v127_l845 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v128_l848 num-num-pose)


(deftest
 t129_l850
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v128_l848)))


(def
 v131_l859
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v132_l864 ts-line-pose)


(deftest
 t133_l866
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v132_l864)))


(def
 v135_l875
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v136_l880 boxplot-pose)


(deftest
 t137_l882
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v136_l880)))


(def
 v139_l892
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v140_l897 horizontal-boxplot-pose)


(deftest
 t141_l899
 (is
  ((fn
    [v]
    (let
     [layer
      (first
       (:layers (first (:panels (pj/plan horizontal-boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v140_l897)))


(def v143_l911 scatter-pose)


(deftest
 t144_l913
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v143_l911)))


(def v146_l934 bar-pose)


(deftest
 t147_l936
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v146_l934)))


(def
 v149_l943
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v150_l948 fill-pose)


(deftest
 t151_l950
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v150_l948)))


(def v153_l982 scatter-pose)


(deftest
 t154_l984
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (= [1.0 2.0 3.0 4.0 5.0] (:values (:x-ticks p)))
      (= ["1" "2" "3" "4" "5"] (:labels (:x-ticks p)))
      (=
       ["2.0" "2.5" "3.0" "3.5" "4.0" "4.5" "5.0"]
       (:labels (:y-ticks p))))))
   v153_l982)))


(def
 v156_l997
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v157_l1003 log-scale-pose)


(deftest
 t158_l1005
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v157_l1003)))


(def v160_l1017 bar-pose)


(deftest
 t161_l1019
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v160_l1017)))


(def
 v163_l1035
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v164_l1039 iris-label-pose)


(deftest
 t165_l1041
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v164_l1039)))


(def v167_l1051 (def x-only-pose (-> five-points (pj/pose :x))))


(def v168_l1054 x-only-pose)


(deftest
 t169_l1056
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (nil? (:y-label plan)))))
   v168_l1054)))


(def
 v171_l1064
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v172_l1069 explicit-label-pose)


(deftest
 t173_l1071
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v172_l1069)))


(def v175_l1083 colored-pose)


(deftest
 t176_l1085
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v175_l1083)))


(def v178_l1096 scatter-pose)


(deftest
 t179_l1098
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v178_l1096)))


(def v181_l1103 fixed-color-pose)


(deftest
 t182_l1105
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v181_l1103)))


(def
 v184_l1110
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v185_l1114 continuous-color-pose)


(deftest
 t186_l1116
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v185_l1114)))


(def
 v188_l1129
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v189_l1133 size-legend-pose)


(deftest
 t190_l1135
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v189_l1133)))


(def v192_l1145 scatter-pose)


(deftest
 t193_l1147
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v192_l1145)))


(def
 v195_l1156
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v196_l1160 alpha-legend-pose)


(deftest
 t197_l1162
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v196_l1160)))


(def v199_l1171 scatter-pose)


(deftest
 t200_l1173
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v199_l1171)))


(def v202_l1183 scatter-pose)


(def
 v203_l1185
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v204_l1192 full-layout-pose)


(deftest
 t205_l1194
 (is
  ((fn
    [_]
    (let
     [bare (pj/plan scatter-pose) full (pj/plan full-layout-pose)]
     (and
      (zero? (get-in bare [:layout :title-pad]))
      (pos? (get-in full [:layout :title-pad]))
      (zero? (get-in bare [:layout :legend-w]))
      (= 100 (get-in full [:layout :legend-w])))))
   v204_l1192)))


(def v207_l1213 scatter-pose)


(deftest
 t208_l1215
 (is
  ((fn
    [_]
    (let
     [iris (rdatasets/datasets-iris)]
     (and
      (= :single (:layout-type (pj/plan scatter-pose)))
      (=
       :facet-grid
       (:layout-type
        (pj/plan
         (->
          iris
          (pj/lay-point :sepal-length :sepal-width)
          (pj/facet :species)))))
      (=
       :single
       (:layout-type
        (pj/plan
         (->
          iris
          (pj/lay-point :sepal-length :sepal-width)
          (pj/lay-point :petal-length :petal-width))))))))
   v207_l1213)))


(def
 v210_l1231
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v211_l1235 normal-pose)


(def
 v212_l1237
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v213_l1242 flip-pose)


(deftest
 t214_l1244
 (is
  ((fn
    [v]
    (let
     [np
      (first (:panels (pj/plan normal-pose)))
      fp
      (first (:panels (pj/plan flip-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (true? (:categorical? (:x-ticks np)))
      (not (:categorical? (:y-ticks np)))
      (not (:categorical? (:x-ticks fp)))
      (true? (:categorical? (:y-ticks fp))))))
   v213_l1242)))


(def
 v216_l1259
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v217_l1264 flipped-labels-pose)


(deftest
 t218_l1266
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v217_l1264)))


(def
 v220_l1283
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v221_l1289 multi-pose)


(deftest
 t222_l1291
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v221_l1289)))


(def
 v224_l1308
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
