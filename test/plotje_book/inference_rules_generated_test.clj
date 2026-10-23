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
 v24_l190
 (def
  seedlings
  {:day [1 2 3 4 5 6 1 2 3 4 5 6],
   :height [2.0 3.1 3.9 5.2 6.1 6.8 1.5 2.1 2.4 3.3 3.6 4.4],
   :bed
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


(def v25_l196 (-> seedlings pj/lay-point (pj/lay-smooth :day :height)))


(deftest
 t26_l200
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 2 (:lines s))
      (some #{"north"} (:texts s))
      (= :bed (:color (:mapping (pj/infer-mapping v)))))))
   v25_l196)))


(def v28_l209 (-> seedlings pj/lay-point (pj/lay-rug :day)))


(deftest
 t29_l213
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 12 (:lines s))
      (some #{"north"} (:texts s)))))
   v28_l209)))


(def v31_l221 (-> seedlings (pj/lay-point :day :height) pj/lay-smooth))


(deftest
 t32_l225
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
   v31_l221)))


(def
 v34_l242
 (try
  (-> (rdatasets/datasets-iris) pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t35_l249
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns:" msg)))
   v34_l242)))


(def
 v37_l255
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t38_l258
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v37_l255)))


(def
 v40_l276
 (def
  animals
  {:animal ["cat" "dog" "bird" "fish"], :count [12 8 15 5]}))


(def v41_l280 (def bar-pose (-> animals (pj/lay-bar :animal :count))))


(def v42_l284 bar-pose)


(deftest
 t43_l286
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= ["cat" "dog" "bird" "fish"] (:x-domain p))
      (true? (:categorical? (:x-ticks p))))))
   v42_l284)))


(def
 v45_l304
 (def
  temporal-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"
     #inst "2024-12-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/lay-point :date :val))))


(def v46_l309 temporal-pose)


(deftest
 t47_l311
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan temporal-pose)))]
     (and
      (number? (first (:x-domain p)))
      (= 6 (count (:values (:x-ticks p))))
      (= "Jan 2024" (first (:labels (:x-ticks p)))))))
   v46_l309)))


(def
 v49_l325
 (let
  [panel (first (:panels (pj/plan temporal-pose)))]
  {:x-scale (:x-scale panel),
   :x-domain (:x-domain panel),
   :x-tick-labels (:labels (:x-ticks panel))}))


(deftest
 t50_l330
 (is
  ((fn
    [m]
    (and
     (= {:type :linear} (:x-scale m))
     (every? number? (:x-domain m))
     (= 6 (count (:x-tick-labels m)))))
   v49_l325)))


(def
 v52_l343
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
 t53_l351
 (is
  ((fn
    [m]
    (and
     (every?
      (fn* [p1__76828#] (re-matches #"\d{2}:\d{2}" p1__76828#))
      (:six-hours m))
     (every?
      (fn*
       [p1__76829#]
       (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__76829#))
      (:three-days m))
     (every?
      (fn* [p1__76830#] (re-matches #"\d{4}" p1__76830#))
      (:nine-years m))
     (= 9 (count (:nine-years m)))))
   v52_l343)))


(def
 v55_l375
 (def
  hour-bar-pose
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count {:x-type :categorical}))))


(def v56_l379 hour-bar-pose)


(deftest
 t57_l381
 (is
  ((fn
    [v]
    (and
     (= 4 (:polygons (pj/svg-summary v)))
     (=
      ["9" "10" "11" "12"]
      (:x-domain (first (:panels (pj/plan hour-bar-pose)))))
     (let
      [plain
       (pj/lay-bar
        {:hour [9 10 11 12], :count [5 8 12 7]}
        :hour
        :count)
       ticks
       (-> plain pj/plan :panels first :x-ticks)]
      (and
       (= 4 (:polygons (pj/svg-summary plain)))
       (false? (:categorical? ticks))
       (not= ["9" "10" "11" "12"] (:labels ticks))))))
   v56_l379)))


(def
 v59_l410
 (def
  colored-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g}))))


(def v60_l416 colored-pose)


(deftest
 t61_l418
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
   v60_l416)))


(def
 v63_l436
 (def
  fixed-color-pose
  (-> five-points (pj/lay-point :x :y {:color "#E74C3C"}))))


(def v64_l440 fixed-color-pose)


(deftest
 t65_l442
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
   v64_l440)))


(def
 v67_l466
 (-> five-points (pj/lay-point :x :y {:color "steelblue"})))


(deftest
 t68_l469
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v67_l466)))


(def
 v70_l509
 (def
  red-color-pose
  (-> five-points (pj/lay-point :x :y {:color "red"}))))


(def v71_l513 red-color-pose)


(deftest
 t72_l515
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
   v71_l513)))


(def v74_l548 colored-pose)


(deftest
 t75_l550
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
   v74_l548)))


(def
 v77_l565
 (->
  {:x [1 2 3], :y [4 5 6], :hex ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-point :x :y {:color {:column :hex, :scale false}})))


(deftest
 t78_l568
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v77_l565)))


(def
 v80_l584
 (def
  numeric-color-pose
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4], :val [10 20 30 40 50]}
   (pj/lay-point :x :y {:color :val}))))


(def v81_l590 numeric-color-pose)


(deftest
 t82_l592
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
   v81_l590)))


(def
 v84_l611
 (def
  study-data
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}))


(def
 v86_l618
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v87_l622 study-continuous-pose)


(deftest
 t88_l624
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
   v87_l622)))


(def
 v90_l633
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v91_l638 study-categorical-pose)


(deftest
 t92_l640
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
   v91_l638)))


(def
 v94_l652
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t95_l660
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v94_l652)))


(def
 v97_l670
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v98_l675
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v99_l679 explicit-group-pose)


(deftest
 t100_l681
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v99_l679)))


(def
 v102_l701
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t103_l706
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v102_l701)))


(def
 v105_l712
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t106_l717
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v105_l712)))


(def v108_l763 (-> five-points (pj/pose :x :x)))


(deftest
 t109_l766
 (is
  ((fn
    [v]
    (let
     [mark-of
      (fn [pose] (-> pose pj/plan :panels first :layers first :mark))
      dates
      {:t
       [#inst "2024-01-01T00:00:00.000-00:00"
        #inst "2024-02-01T00:00:00.000-00:00"
        #inst "2024-03-01T00:00:00.000-00:00"],
       :u
       [#inst "2023-01-01T00:00:00.000-00:00"
        #inst "2023-02-01T00:00:00.000-00:00"
        #inst "2023-03-01T00:00:00.000-00:00"],
       :c ["a" "b" "a"],
       :k ["p" "q" "q"]}]
     (and
      (= :bar (mark-of v))
      (= :rect (mark-of (pj/pose animals :animal :animal)))
      (= 5 (:points (pj/svg-summary (pj/lay-point five-points :x :x))))
      (= :rect (mark-of (pj/lay-bar animals :animal :animal)))
      (= :boxplot (mark-of (pj/pose dates :t :c)))
      (= :boxplot (mark-of (pj/pose dates :c :t)))
      (= :point (mark-of (pj/pose dates :c :k)))
      (= :point (mark-of (pj/pose dates :t :u))))))
   v108_l763)))


(def v111_l790 (def hist-pose (-> five-points (pj/pose :x))))


(def v112_l794 hist-pose)


(deftest
 t113_l796
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v112_l794)))


(def
 v115_l809
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


(def v116_l814 temporal-hist-pose)


(deftest
 t117_l816
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v116_l814)))


(def v119_l824 (def count-pose (-> animals (pj/pose :animal))))


(def v120_l828 count-pose)


(deftest
 t121_l830
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v120_l828)))


(def
 v123_l843
 (def count-pose-on-y (-> animals (pj/pose {:y :animal}))))


(def v124_l847 count-pose-on-y)


(deftest
 t125_l849
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
   v124_l847)))


(def v127_l867 (-> animals (pj/lay-point {:y :count})))


(deftest
 t128_l870
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:x-label plan))
      (= "count" (:y-label plan))
      (= 4 (:points (pj/svg-summary v))))))
   v127_l867)))


(def v130_l881 (-> animals (pj/lay-bar {:y :animal})))


(deftest
 t131_l884
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
   v130_l881)))


(def v133_l894 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v134_l897 num-num-pose)


(deftest
 t135_l899
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v134_l897)))


(def
 v137_l908
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v138_l913 ts-line-pose)


(deftest
 t139_l915
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v138_l913)))


(def
 v141_l924
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v142_l929 boxplot-pose)


(deftest
 t143_l931
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v142_l929)))


(def
 v145_l941
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v146_l946 horizontal-boxplot-pose)


(deftest
 t147_l948
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
   v146_l946)))


(def v149_l960 scatter-pose)


(deftest
 t150_l962
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v149_l960)))


(def v152_l983 bar-pose)


(deftest
 t153_l985
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v152_l983)))


(def
 v155_l992
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v156_l997 fill-pose)


(deftest
 t157_l999
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v156_l997)))


(def v159_l1031 scatter-pose)


(deftest
 t160_l1033
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
   v159_l1031)))


(def
 v162_l1046
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v163_l1052 log-scale-pose)


(deftest
 t164_l1054
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v163_l1052)))


(def v166_l1066 bar-pose)


(deftest
 t167_l1068
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v166_l1066)))


(def
 v169_l1084
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v170_l1088 iris-label-pose)


(deftest
 t171_l1090
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v170_l1088)))


(def v173_l1100 (def x-only-pose (-> five-points (pj/pose :x))))


(def v174_l1103 x-only-pose)


(deftest
 t175_l1105
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (nil? (:y-label plan)))))
   v174_l1103)))


(def
 v177_l1113
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v178_l1118 explicit-label-pose)


(deftest
 t179_l1120
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v178_l1118)))


(def v181_l1132 colored-pose)


(deftest
 t182_l1134
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v181_l1132)))


(def v184_l1146 scatter-pose)


(deftest
 t185_l1148
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v184_l1146)))


(def v187_l1153 fixed-color-pose)


(deftest
 t188_l1155
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v187_l1153)))


(def
 v190_l1160
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v191_l1164 continuous-color-pose)


(deftest
 t192_l1166
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v191_l1164)))


(def
 v194_l1179
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v195_l1183 size-legend-pose)


(deftest
 t196_l1185
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v195_l1183)))


(def v198_l1195 scatter-pose)


(deftest
 t199_l1197
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v198_l1195)))


(def
 v201_l1206
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v202_l1210 alpha-legend-pose)


(deftest
 t203_l1212
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v202_l1210)))


(def v205_l1221 scatter-pose)


(deftest
 t206_l1223
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v205_l1221)))


(def v208_l1233 scatter-pose)


(def
 v209_l1235
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v210_l1242 full-layout-pose)


(deftest
 t211_l1244
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
   v210_l1242)))


(def v213_l1266 scatter-pose)


(deftest
 t214_l1268
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
          (pj/lay-point :petal-length :petal-width)))))
      (let
       [plan
        (pj/plan
         (->
          iris
          (pj/lay-point :sepal-length :sepal-width)
          (pj/facet {:y [:sepal-width :petal-width]})))]
       (and
        (:composite? plan)
        (=
         [:single :single]
         (mapv
          (fn* [p1__76831#] (get-in p1__76831# [:plan :layout-type]))
          (:sub-plots plan))))))))
   v213_l1266)))


(def
 v216_l1292
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v217_l1296 normal-pose)


(def
 v218_l1298
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v219_l1303 flip-pose)


(deftest
 t220_l1305
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
   v219_l1303)))


(def
 v222_l1320
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v223_l1325 flipped-labels-pose)


(deftest
 t224_l1327
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v223_l1325)))


(def
 v226_l1344
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v227_l1350 multi-pose)


(deftest
 t228_l1352
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v227_l1350)))


(def
 v230_l1368
 (def
  two-columns-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length)
   (pj/lay-point {:y :sepal-width})
   (pj/lay-point {:y :petal-width}))))


(def v231_l1374 two-columns-pose)


(deftest
 t232_l1376
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan two-columns-pose)]
     (and
      (=
       ["sepal width" "petal width"]
       (mapv :label (:entries (:legend plan))))
      (= "sepal length" (:x-label plan))
      (= "sepal width, petal width" (:y-label plan)))))
   v231_l1374)))


(def
 v234_l1388
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-point {:y :sepal-width, :color "#377eb8"})
  (pj/lay-point {:y :petal-width})))


(deftest
 t235_l1393
 (is
  ((fn
    [v]
    (let
     [by-column
      (->
       (rdatasets/datasets-iris)
       (pj/pose :sepal-length)
       (pj/lay-point {:y :sepal-width, :color :species})
       (pj/lay-point {:y :petal-width}))]
     (and
      (=
       ["petal width"]
       (mapv :label (:entries (:legend (pj/plan v)))))
      (=
       ["setosa" "versicolor" "virginica"]
       (mapv :label (:entries (:legend (pj/plan by-column))))))))
   v234_l1388)))


(def
 v237_l1411
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
