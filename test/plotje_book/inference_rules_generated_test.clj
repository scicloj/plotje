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


(deftest
 t24_l183
 (is
  ((fn
    [_]
    (let
     [inferred (pj/infer-mapping two-col-pose)]
     (and
      (= inferred (pj/infer-mapping inferred))
      (= (pj/plot two-col-pose) (pj/plot inferred)))))
   v21_l175)))


(def
 v26_l197
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


(def v27_l203 (-> seedlings pj/lay-point (pj/lay-smooth :day :height)))


(deftest
 t28_l207
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
   v27_l203)))


(def v30_l216 (-> seedlings pj/lay-point (pj/lay-rug :day)))


(deftest
 t31_l220
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 12 (:lines s))
      (some #{"north"} (:texts s)))))
   v30_l216)))


(def v33_l231 (-> seedlings pj/lay-point (pj/lay-smooth :height :day)))


(deftest
 t34_l235
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) [points line] (:layers (pj/draft v))]
     (and
      (= 12 (:points s))
      (some #{"north"} (:texts s))
      (= [:day :height :bed] ((juxt :x :y :color) points))
      (= [:height :day] ((juxt :x :y) line)))))
   v33_l231)))


(def v36_l245 (-> seedlings (pj/lay-point :day :height) pj/lay-smooth))


(deftest
 t37_l249
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
   v36_l245)))


(def
 v39_l270
 (try
  (-> (rdatasets/datasets-iris) pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t40_l277
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns:" msg)))
   v39_l270)))


(def
 v42_l283
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t43_l286
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v42_l283)))


(def
 v45_l304
 (def
  animals
  {:animal ["cat" "dog" "bird" "fish"], :count [12 8 15 5]}))


(def v46_l308 (def bar-pose (-> animals (pj/lay-bar :animal :count))))


(def v47_l312 bar-pose)


(deftest
 t48_l314
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= ["cat" "dog" "bird" "fish"] (:x-domain p))
      (true? (:categorical? (:x-ticks p))))))
   v47_l312)))


(def
 v50_l332
 (def
  temporal-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"
     #inst "2024-12-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/lay-point :date :val))))


(def v51_l337 temporal-pose)


(deftest
 t52_l339
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan temporal-pose)))]
     (and
      (number? (first (:x-domain p)))
      (= 6 (count (:values (:x-ticks p))))
      (= "Jan 2024" (first (:labels (:x-ticks p)))))))
   v51_l337)))


(def
 v54_l353
 (let
  [panel (first (:panels (pj/plan temporal-pose)))]
  {:x-scale (:x-scale panel),
   :x-domain (:x-domain panel),
   :x-tick-labels (:labels (:x-ticks panel))}))


(deftest
 t55_l358
 (is
  ((fn
    [m]
    (and
     (= {:type :linear} (:x-scale m))
     (every? number? (:x-domain m))
     (= 6 (count (:x-tick-labels m)))))
   v54_l353)))


(def
 v57_l371
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
 t58_l379
 (is
  ((fn
    [m]
    (and
     (every?
      (fn* [p1__77559#] (re-matches #"\d{2}:\d{2}" p1__77559#))
      (:six-hours m))
     (every?
      (fn*
       [p1__77560#]
       (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__77560#))
      (:three-days m))
     (every?
      (fn* [p1__77561#] (re-matches #"\d{4}" p1__77561#))
      (:nine-years m))
     (= 9 (count (:nine-years m)))))
   v57_l371)))


(def
 v60_l403
 (def
  hour-bar-pose
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count {:x-type :categorical}))))


(def v61_l407 hour-bar-pose)


(deftest
 t62_l409
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
   v61_l407)))


(def
 v64_l438
 (def
  colored-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g}))))


(def v65_l444 colored-pose)


(deftest
 t66_l446
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
   v65_l444)))


(def
 v68_l464
 (def
  fixed-color-pose
  (-> five-points (pj/lay-point :x :y {:color "#E74C3C"}))))


(def v69_l468 fixed-color-pose)


(deftest
 t70_l470
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
   v69_l468)))


(def
 v72_l494
 (-> five-points (pj/lay-point :x :y {:color "steelblue"})))


(deftest
 t73_l497
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v72_l494)))


(def
 v75_l537
 (def
  red-color-pose
  (-> five-points (pj/lay-point :x :y {:color "red"}))))


(def v76_l541 red-color-pose)


(deftest
 t77_l543
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
   v76_l541)))


(def v79_l576 colored-pose)


(deftest
 t80_l578
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
   v79_l576)))


(def
 v82_l593
 (->
  {:x [1 2 3], :y [4 5 6], :hex ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-point :x :y {:color {:column :hex, :scale false}})))


(deftest
 t83_l596
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v82_l593)))


(def
 v85_l612
 (def
  numeric-color-pose
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4], :val [10 20 30 40 50]}
   (pj/lay-point :x :y {:color :val}))))


(def v86_l618 numeric-color-pose)


(deftest
 t87_l620
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
   v86_l618)))


(def
 v89_l639
 (def
  study-data
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}))


(def
 v91_l646
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v92_l650 study-continuous-pose)


(deftest
 t93_l652
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
   v92_l650)))


(def
 v95_l661
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v96_l666 study-categorical-pose)


(deftest
 t97_l668
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
   v96_l666)))


(def
 v99_l680
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t100_l688
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v99_l680)))


(def
 v102_l698
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v103_l703
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v104_l707 explicit-group-pose)


(deftest
 t105_l709
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v104_l707)))


(def
 v107_l729
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l734
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v107_l729)))


(def
 v110_l740
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t111_l745
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v110_l740)))


(def v113_l791 (-> five-points (pj/pose :x :x)))


(deftest
 t114_l794
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
   v113_l791)))


(def v116_l818 (def hist-pose (-> five-points (pj/pose :x))))


(def v117_l822 hist-pose)


(deftest
 t118_l824
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v117_l822)))


(def
 v120_l837
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


(def v121_l842 temporal-hist-pose)


(deftest
 t122_l844
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v121_l842)))


(def v124_l852 (def count-pose (-> animals (pj/pose :animal))))


(def v125_l856 count-pose)


(deftest
 t126_l858
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v125_l856)))


(def
 v128_l871
 (def count-pose-on-y (-> animals (pj/pose {:y :animal}))))


(def v129_l875 count-pose-on-y)


(deftest
 t130_l877
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
   v129_l875)))


(def v132_l896 (-> animals (pj/lay-point {:y :count})))


(deftest
 t133_l899
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:x-label plan))
      (= "count" (:y-label plan))
      (= 4 (:points (pj/svg-summary v))))))
   v132_l896)))


(def v135_l910 (-> animals (pj/lay-bar {:y :animal})))


(deftest
 t136_l913
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
   v135_l910)))


(def v138_l923 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v139_l926 num-num-pose)


(deftest
 t140_l928
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v139_l926)))


(def
 v142_l937
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v143_l942 ts-line-pose)


(deftest
 t144_l944
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v143_l942)))


(def
 v146_l953
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v147_l958 boxplot-pose)


(deftest
 t148_l960
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v147_l958)))


(def
 v150_l970
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v151_l975 horizontal-boxplot-pose)


(deftest
 t152_l977
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
   v151_l975)))


(def v154_l989 scatter-pose)


(deftest
 t155_l991
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v154_l989)))


(def v157_l1012 bar-pose)


(deftest
 t158_l1014
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v157_l1012)))


(def
 v160_l1021
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v161_l1026 fill-pose)


(deftest
 t162_l1028
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v161_l1026)))


(def v164_l1060 scatter-pose)


(deftest
 t165_l1062
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
   v164_l1060)))


(def
 v167_l1075
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v168_l1081 log-scale-pose)


(deftest
 t169_l1083
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v168_l1081)))


(def v171_l1095 bar-pose)


(deftest
 t172_l1097
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v171_l1095)))


(def
 v174_l1113
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v175_l1117 iris-label-pose)


(deftest
 t176_l1119
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v175_l1117)))


(def v178_l1129 (def x-only-pose (-> five-points (pj/pose :x))))


(def v179_l1132 x-only-pose)


(deftest
 t180_l1134
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (nil? (:y-label plan)))))
   v179_l1132)))


(def
 v182_l1142
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v183_l1147 explicit-label-pose)


(deftest
 t184_l1149
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v183_l1147)))


(def v186_l1161 colored-pose)


(deftest
 t187_l1163
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v186_l1161)))


(def v189_l1175 scatter-pose)


(deftest
 t190_l1177
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v189_l1175)))


(def v192_l1182 fixed-color-pose)


(deftest
 t193_l1184
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v192_l1182)))


(def
 v195_l1189
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v196_l1193 continuous-color-pose)


(deftest
 t197_l1195
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v196_l1193)))


(def
 v199_l1208
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v200_l1212 size-legend-pose)


(deftest
 t201_l1214
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v200_l1212)))


(def v203_l1224 scatter-pose)


(deftest
 t204_l1226
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v203_l1224)))


(def
 v206_l1235
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v207_l1239 alpha-legend-pose)


(deftest
 t208_l1241
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v207_l1239)))


(def v210_l1250 scatter-pose)


(deftest
 t211_l1252
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v210_l1250)))


(def v213_l1262 scatter-pose)


(def
 v214_l1264
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v215_l1271 full-layout-pose)


(deftest
 t216_l1273
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
   v215_l1271)))


(def v218_l1295 scatter-pose)


(deftest
 t219_l1297
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
          (fn* [p1__77562#] (get-in p1__77562# [:plan :layout-type]))
          (:sub-plots plan))))))))
   v218_l1295)))


(def
 v221_l1321
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v222_l1325 normal-pose)


(def
 v223_l1327
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v224_l1332 flip-pose)


(deftest
 t225_l1334
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
   v224_l1332)))


(def
 v227_l1349
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v228_l1354 flipped-labels-pose)


(deftest
 t229_l1356
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v228_l1354)))


(def
 v231_l1373
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v232_l1379 multi-pose)


(deftest
 t233_l1381
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v232_l1379)))


(def
 v235_l1397
 (def
  two-columns-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length)
   (pj/lay-point {:y :sepal-width})
   (pj/lay-point {:y :petal-width}))))


(def v236_l1403 two-columns-pose)


(deftest
 t237_l1405
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
   v236_l1403)))


(def
 v239_l1417
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-point {:y :sepal-width, :color "#7f7f7f"})
  (pj/lay-point {:y :petal-width})))


(deftest
 t240_l1422
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
   v239_l1417)))


(def
 v242_l1440
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
