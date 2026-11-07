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


(def
 v11_l144
 (-> {:weight [1 2 3 4 5], :height [2 4 3 5 4]} pj/lay-point))


(deftest
 t12_l147
 (is
  ((fn
    [v]
    (and
     (= 5 (:points (pj/svg-summary v)))
     (= ["weight" "height"] ((juxt :x-label :y-label) (pj/plan v)))))
   v11_l144)))


(def
 v14_l153
 (->
  {:weight [1 2 3 4], :height [4 5 6 7], :site ["a" "a" "b" "b"]}
  pj/lay-point))


(deftest
 t15_l156
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:points s))
      (some #{"a"} (:texts s))
      (=
       {:x :weight, :y :height, :color :site}
       (:mapping (pj/infer-mapping v))))))
   v14_l153)))


(def
 v17_l169
 (def
  two-col-pose
  (pj/pose
   {:side [1.0 2.0 3.0 4.0 5.0], :area [1.0 4.0 9.0 16.0 25.0]})))


(def v18_l173 two-col-pose)


(deftest
 t19_l175
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l173)))


(def
 v21_l180
 (->
  two-col-pose
  pj/infer-mapping
  (select-keys [:mapping :layers])
  kind/pprint))


(deftest
 t22_l185
 (is
  ((fn
    [pose]
    (and
     (= {:x :side, :y :area} (:mapping pose))
     (empty? (:layers pose))))
   v21_l180)))


(deftest
 t24_l191
 (is
  ((fn
    [_]
    (let
     [inferred (pj/infer-mapping two-col-pose)]
     (and
      (= inferred (pj/infer-mapping inferred))
      (= (pj/plot two-col-pose) (pj/plot inferred)))))
   v21_l180)))


(def
 v26_l205
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


(def v27_l211 (-> seedlings pj/lay-point (pj/lay-smooth :day :height)))


(deftest
 t28_l215
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
   v27_l211)))


(def v30_l224 (-> seedlings pj/lay-point (pj/lay-rug :day)))


(deftest
 t31_l228
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 12 (:lines s))
      (some #{"north"} (:texts s)))))
   v30_l224)))


(def v33_l239 (-> seedlings pj/lay-point (pj/lay-smooth :height :day)))


(deftest
 t34_l243
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
   v33_l239)))


(def v36_l253 (-> seedlings (pj/lay-point :day :height) pj/lay-smooth))


(deftest
 t37_l257
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
   v36_l253)))


(def
 v39_l278
 (try
  (-> (rdatasets/datasets-iris) pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t40_l285
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns:" msg)))
   v39_l278)))


(def
 v42_l291
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t43_l294
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v42_l291)))


(def
 v45_l312
 (def
  animals
  {:animal ["cat" "dog" "bird" "fish"], :count [12 8 15 5]}))


(def v46_l316 (def bar-pose (-> animals (pj/lay-bar :animal :count))))


(def v47_l320 bar-pose)


(deftest
 t48_l322
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= ["cat" "dog" "bird" "fish"] (:x-domain p))
      (true? (:categorical? (:x-ticks p))))))
   v47_l320)))


(def
 v50_l340
 (def
  temporal-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"
     #inst "2024-12-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/lay-point :date :val))))


(def v51_l345 temporal-pose)


(deftest
 t52_l347
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan temporal-pose)))]
     (and
      (number? (first (:x-domain p)))
      (= 6 (count (:values (:x-ticks p))))
      (= "Jan 2024" (first (:labels (:x-ticks p)))))))
   v51_l345)))


(def
 v54_l361
 (let
  [panel (first (:panels (pj/plan temporal-pose)))]
  {:x-scale (:x-scale panel),
   :x-domain (:x-domain panel),
   :x-tick-labels (:labels (:x-ticks panel))}))


(deftest
 t55_l366
 (is
  ((fn
    [m]
    (and
     (= {:type :linear} (:x-scale m))
     (every? number? (:x-domain m))
     (= 6 (count (:x-tick-labels m)))))
   v54_l361)))


(def
 v57_l379
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
 t58_l387
 (is
  ((fn
    [m]
    (and
     (every?
      (fn* [p1__80875#] (re-matches #"\d{2}:\d{2}" p1__80875#))
      (:six-hours m))
     (every?
      (fn*
       [p1__80876#]
       (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__80876#))
      (:three-days m))
     (every?
      (fn* [p1__80877#] (re-matches #"\d{4}" p1__80877#))
      (:nine-years m))
     (= 9 (count (:nine-years m)))))
   v57_l379)))


(def
 v60_l411
 (def
  hour-bar-pose
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count {:x-type :categorical}))))


(def v61_l415 hour-bar-pose)


(deftest
 t62_l417
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
   v61_l415)))


(def
 v64_l446
 (def
  colored-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g}))))


(def v65_l452 colored-pose)


(deftest
 t66_l454
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
   v65_l452)))


(def
 v68_l473
 (def
  fixed-color-pose
  (-> five-points (pj/lay-point :x :y {:color "#E74C3C"}))))


(def v69_l477 fixed-color-pose)


(deftest
 t70_l479
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
   v69_l477)))


(def
 v72_l503
 (-> five-points (pj/lay-point :x :y {:color "steelblue"})))


(deftest
 t73_l506
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v72_l503)))


(def
 v75_l546
 (def
  red-color-pose
  (-> five-points (pj/lay-point :x :y {:color "red"}))))


(def v76_l550 red-color-pose)


(deftest
 t77_l552
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
   v76_l550)))


(def v79_l585 colored-pose)


(deftest
 t80_l587
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
   v79_l585)))


(def
 v82_l602
 (->
  {:x [1 2 3], :y [4 5 6], :hex ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-point :x :y {:color {:column :hex, :scale false}})))


(deftest
 t83_l605
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v82_l602)))


(def
 v85_l621
 (def
  numeric-color-pose
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4], :val [10 20 30 40 50]}
   (pj/lay-point :x :y {:color :val}))))


(def v86_l627 numeric-color-pose)


(deftest
 t87_l629
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
   v86_l627)))


(def
 v89_l648
 (def
  study-data
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}))


(def
 v91_l655
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v92_l659 study-continuous-pose)


(deftest
 t93_l661
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
   v92_l659)))


(def
 v95_l670
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v96_l675 study-categorical-pose)


(deftest
 t97_l677
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
   v96_l675)))


(def
 v99_l689
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t100_l697
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v99_l689)))


(def
 v102_l707
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v103_l712
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v104_l716 explicit-group-pose)


(deftest
 t105_l718
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v104_l716)))


(def
 v107_l738
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l743
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v107_l738)))


(def
 v110_l749
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t111_l754
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v110_l749)))


(def v113_l800 (-> five-points (pj/pose :x :x)))


(deftest
 t114_l803
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
   v113_l800)))


(def v116_l827 (def hist-pose (-> five-points (pj/pose :x))))


(def v117_l831 hist-pose)


(deftest
 t118_l833
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v117_l831)))


(def
 v120_l846
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


(def v121_l851 temporal-hist-pose)


(deftest
 t122_l853
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v121_l851)))


(def v124_l861 (def count-pose (-> animals (pj/pose :animal))))


(def v125_l865 count-pose)


(deftest
 t126_l867
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v125_l865)))


(def
 v128_l880
 (def count-pose-on-y (-> animals (pj/pose {:y :animal}))))


(def v129_l884 count-pose-on-y)


(deftest
 t130_l886
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
   v129_l884)))


(def v132_l905 (-> animals (pj/lay-point {:y :count})))


(deftest
 t133_l908
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:x-label plan))
      (= "count" (:y-label plan))
      (= 4 (:points (pj/svg-summary v))))))
   v132_l905)))


(def v135_l919 (-> animals (pj/lay-bar {:y :animal})))


(deftest
 t136_l922
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
   v135_l919)))


(def v138_l932 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v139_l935 num-num-pose)


(deftest
 t140_l937
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v139_l935)))


(def
 v142_l946
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v143_l951 ts-line-pose)


(deftest
 t144_l953
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v143_l951)))


(def
 v146_l962
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v147_l967 boxplot-pose)


(deftest
 t148_l969
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v147_l967)))


(def
 v150_l979
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v151_l984 horizontal-boxplot-pose)


(deftest
 t152_l986
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
   v151_l984)))


(def v154_l998 scatter-pose)


(deftest
 t155_l1000
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v154_l998)))


(def v157_l1021 bar-pose)


(deftest
 t158_l1023
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v157_l1021)))


(def
 v160_l1030
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v161_l1035 fill-pose)


(deftest
 t162_l1037
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v161_l1035)))


(def v164_l1069 scatter-pose)


(deftest
 t165_l1071
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
   v164_l1069)))


(def
 v167_l1084
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v168_l1090 log-scale-pose)


(deftest
 t169_l1092
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v168_l1090)))


(def v171_l1104 bar-pose)


(deftest
 t172_l1106
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v171_l1104)))


(def
 v174_l1122
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v175_l1126 iris-label-pose)


(deftest
 t176_l1128
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v175_l1126)))


(def v178_l1139 (def x-only-pose (-> five-points (pj/pose :x))))


(def v179_l1142 x-only-pose)


(deftest
 t180_l1144
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (= "count" (:y-label plan)))))
   v179_l1142)))


(def
 v182_l1152
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v183_l1157 explicit-label-pose)


(deftest
 t184_l1159
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v183_l1157)))


(def v186_l1171 colored-pose)


(deftest
 t187_l1173
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v186_l1171)))


(def v189_l1185 scatter-pose)


(deftest
 t190_l1187
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v189_l1185)))


(def v192_l1192 fixed-color-pose)


(deftest
 t193_l1194
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v192_l1192)))


(def
 v195_l1199
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v196_l1203 continuous-color-pose)


(deftest
 t197_l1205
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v196_l1203)))


(def
 v199_l1218
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v200_l1222 size-legend-pose)


(deftest
 t201_l1224
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v200_l1222)))


(def v203_l1234 scatter-pose)


(deftest
 t204_l1236
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v203_l1234)))


(def
 v206_l1245
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v207_l1249 alpha-legend-pose)


(deftest
 t208_l1251
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v207_l1249)))


(deftest
 t210_l1260
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v207_l1249)))


(def v212_l1270 scatter-pose)


(def
 v213_l1272
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v214_l1279 full-layout-pose)


(deftest
 t215_l1281
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
   v214_l1279)))


(def v217_l1303 scatter-pose)


(deftest
 t218_l1305
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
          (fn* [p1__80878#] (get-in p1__80878# [:plan :layout-type]))
          (:sub-plots plan))))))))
   v217_l1303)))


(def
 v220_l1329
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v221_l1333 normal-pose)


(def
 v222_l1335
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v223_l1340 flip-pose)


(deftest
 t224_l1342
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
   v223_l1340)))


(def
 v226_l1357
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v227_l1362 flipped-labels-pose)


(deftest
 t228_l1364
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v227_l1362)))


(def
 v230_l1381
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v231_l1387 multi-pose)


(deftest
 t232_l1389
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v231_l1387)))


(def
 v234_l1405
 (def
  two-columns-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length)
   (pj/lay-point {:y :sepal-width})
   (pj/lay-point {:y :petal-width}))))


(def v235_l1411 two-columns-pose)


(deftest
 t236_l1413
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
   v235_l1411)))


(def
 v238_l1425
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-point {:y :sepal-width, :color "#7f7f7f"})
  (pj/lay-point {:y :petal-width})))


(deftest
 t239_l1430
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
   v238_l1425)))


(def
 v241_l1448
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
