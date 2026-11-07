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


(def v8_l142 (-> {:values [1 2 3 4 5 6]} pj/lay-histogram))


(deftest
 t9_l145
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v8_l142)))


(def
 v11_l150
 (-> {:weight [1 2 3 4 5], :height [2 4 3 5 4]} pj/lay-point))


(deftest
 t12_l153
 (is
  ((fn
    [v]
    (and
     (= 5 (:points (pj/svg-summary v)))
     (= ["weight" "height"] ((juxt :x-label :y-label) (pj/plan v)))))
   v11_l150)))


(def
 v14_l159
 (->
  {:weight [1 2 3 4], :height [4 5 6 7], :site ["a" "a" "b" "b"]}
  pj/lay-point))


(deftest
 t15_l162
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
   v14_l159)))


(def
 v17_l175
 (def
  two-col-pose
  (pj/pose
   {:side [1.0 2.0 3.0 4.0 5.0], :area [1.0 4.0 9.0 16.0 25.0]})))


(def v18_l179 two-col-pose)


(deftest
 t19_l181
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l179)))


(def
 v21_l186
 (->
  two-col-pose
  pj/infer-mapping
  (select-keys [:mapping :layers])
  kind/pprint))


(deftest
 t22_l191
 (is
  ((fn
    [pose]
    (and
     (= {:x :side, :y :area} (:mapping pose))
     (empty? (:layers pose))))
   v21_l186)))


(deftest
 t24_l197
 (is
  ((fn
    [_]
    (let
     [inferred (pj/infer-mapping two-col-pose)]
     (and
      (= inferred (pj/infer-mapping inferred))
      (= (pj/plot two-col-pose) (pj/plot inferred)))))
   v21_l186)))


(def
 v26_l211
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


(def v27_l217 (-> seedlings pj/lay-point (pj/lay-smooth :day :height)))


(deftest
 t28_l221
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
   v27_l217)))


(def v30_l230 (-> seedlings pj/lay-point (pj/lay-rug :day)))


(deftest
 t31_l234
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 12 (:lines s))
      (some #{"north"} (:texts s)))))
   v30_l230)))


(def v33_l245 (-> seedlings pj/lay-point (pj/lay-smooth :height :day)))


(deftest
 t34_l249
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
   v33_l245)))


(def v36_l259 (-> seedlings (pj/lay-point :day :height) pj/lay-smooth))


(deftest
 t37_l263
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
   v36_l259)))


(def
 v39_l284
 (try
  (-> (rdatasets/datasets-iris) pj/lay-point pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t40_l291
 (is
  ((fn
    [msg]
    (and
     (re-find #"this layer has no column on :x or :y" msg)
     (re-find #"Available columns:" msg)))
   v39_l284)))


(def
 v42_l297
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t43_l300
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v42_l297)))


(def
 v45_l318
 (def
  animals
  {:animal ["cat" "dog" "bird" "fish"], :count [12 8 15 5]}))


(def v46_l322 (def bar-pose (-> animals (pj/lay-bar :animal :count))))


(def v47_l326 bar-pose)


(deftest
 t48_l328
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= ["cat" "dog" "bird" "fish"] (:x-domain p))
      (true? (:categorical? (:x-ticks p))))))
   v47_l326)))


(def
 v50_l346
 (def
  temporal-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"
     #inst "2024-12-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/lay-point :date :val))))


(def v51_l351 temporal-pose)


(deftest
 t52_l353
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan v)))]
     (and
      (= {:type :linear} (:x-scale p))
      (= 2 (count (:x-domain p)))
      (every? number? (:x-domain p))
      (= 6 (count (:values (:x-ticks p))))
      (= 6 (count (:labels (:x-ticks p))))
      (= "Jan 2024" (first (:labels (:x-ticks p)))))))
   v51_l351)))


(def
 v54_l377
 (pj/arrange
  [(->
    {:when
     [#inst "2024-01-15T00:00:00.000-00:00"
      #inst "2024-01-15T06:00:00.000-00:00"],
     :level [1 2]}
    (pj/lay-point :when :level)
    (pj/options {:title "Six hours"}))
   (->
    {:when
     [#inst "2024-01-15T00:00:00.000-00:00"
      #inst "2024-01-18T00:00:00.000-00:00"],
     :level [1 2]}
    (pj/lay-point :when :level)
    (pj/options {:title "Three days"}))
   (->
    {:when
     [#inst "2015-01-15T00:00:00.000-00:00"
      #inst "2024-01-15T00:00:00.000-00:00"],
     :level [1 2]}
    (pj/lay-point :when :level)
    (pj/options {:title "Nine years"}))]
  {:cols 1}))


(deftest
 t55_l389
 (is
  ((fn
    [v]
    (let
     [[six-hours three-days nine-years]
      (mapv
       (fn*
        [p1__80870#]
        (-> p1__80870# :plan :panels first :x-ticks :labels))
       (:sub-plots (pj/plan v)))]
     (and
      (every?
       (fn* [p1__80871#] (re-matches #"\d{2}:\d{2}" p1__80871#))
       six-hours)
      (every?
       (fn*
        [p1__80872#]
        (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__80872#))
       three-days)
      (every?
       (fn* [p1__80873#] (re-matches #"\d{4}" p1__80873#))
       nine-years)
      (= 9 (count nine-years)))))
   v54_l377)))


(def
 v57_l421
 (def
  hour-bar-pose
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count {:x-type :categorical}))))


(def v58_l425 hour-bar-pose)


(deftest
 t59_l427
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
   v58_l425)))


(def
 v61_l460
 (def
  colored-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g}))))


(def v62_l466 colored-pose)


(deftest
 t63_l468
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
   v62_l466)))


(def
 v65_l487
 (def
  fixed-color-pose
  (-> five-points (pj/lay-point :x :y {:color "#E74C3C"}))))


(def v66_l491 fixed-color-pose)


(deftest
 t67_l493
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
   v66_l491)))


(def
 v69_l517
 (-> five-points (pj/lay-point :x :y {:color "steelblue"})))


(deftest
 t70_l520
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v69_l517)))


(def
 v72_l560
 (def
  red-color-pose
  (-> five-points (pj/lay-point :x :y {:color "red"}))))


(def v73_l564 red-color-pose)


(deftest
 t74_l566
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
   v73_l564)))


(def v76_l599 colored-pose)


(deftest
 t77_l601
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
   v76_l599)))


(def
 v79_l616
 (->
  {:x [1 2 3], :y [4 5 6], :hex ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-point :x :y {:color {:column :hex, :scale false}})))


(deftest
 t80_l619
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v79_l616)))


(def
 v82_l640
 (def
  numeric-color-pose
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4], :val [10 20 30 40 50]}
   (pj/lay-point :x :y {:color :val}))))


(def v83_l646 numeric-color-pose)


(deftest
 t84_l648
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
   v83_l646)))


(def
 v86_l667
 (def
  study-data
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}))


(def
 v88_l674
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v89_l678 study-continuous-pose)


(deftest
 t90_l680
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
   v89_l678)))


(def
 v92_l689
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v93_l694 study-categorical-pose)


(deftest
 t94_l696
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
   v93_l694)))


(def
 v96_l708
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t97_l716
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v96_l708)))


(def
 v99_l726
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v100_l731
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v101_l735 explicit-group-pose)


(deftest
 t102_l737
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v101_l735)))


(def
 v104_l757
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t105_l762
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v104_l757)))


(def
 v107_l768
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l773
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v107_l768)))


(def v110_l819 (-> five-points (pj/pose :x :x)))


(deftest
 t111_l822
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
   v110_l819)))


(def v113_l852 (def hist-pose (-> five-points (pj/pose :x))))


(def v114_l856 hist-pose)


(deftest
 t115_l858
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v114_l856)))


(def
 v117_l871
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


(def v118_l876 temporal-hist-pose)


(deftest
 t119_l878
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v118_l876)))


(def v121_l886 (def count-pose (-> animals (pj/pose :animal))))


(def v122_l890 count-pose)


(deftest
 t123_l892
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v122_l890)))


(def
 v125_l905
 (def count-pose-on-y (-> animals (pj/pose {:y :animal}))))


(def v126_l909 count-pose-on-y)


(deftest
 t127_l911
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
   v126_l909)))


(def v129_l930 (-> animals (pj/lay-point {:y :count})))


(deftest
 t130_l933
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:x-label plan))
      (= "count" (:y-label plan))
      (= 4 (:points (pj/svg-summary v))))))
   v129_l930)))


(def v132_l944 (-> animals (pj/lay-bar {:y :animal})))


(deftest
 t133_l947
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
   v132_l944)))


(def v135_l957 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v136_l960 num-num-pose)


(deftest
 t137_l962
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v136_l960)))


(def
 v139_l971
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v140_l976 ts-line-pose)


(deftest
 t141_l978
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v140_l976)))


(def
 v143_l987
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v144_l992 boxplot-pose)


(deftest
 t145_l994
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v144_l992)))


(def
 v147_l1004
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v148_l1009 horizontal-boxplot-pose)


(deftest
 t149_l1011
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
   v148_l1009)))


(def v151_l1023 scatter-pose)


(deftest
 t152_l1025
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v151_l1023)))


(def v154_l1046 bar-pose)


(deftest
 t155_l1048
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v154_l1046)))


(def
 v157_l1055
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v158_l1060 fill-pose)


(deftest
 t159_l1062
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v158_l1060)))


(def v161_l1094 scatter-pose)


(deftest
 t162_l1096
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
   v161_l1094)))


(def
 v164_l1109
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v165_l1115 log-scale-pose)


(deftest
 t166_l1117
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v165_l1115)))


(def v168_l1129 bar-pose)


(deftest
 t169_l1131
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v168_l1129)))


(def
 v171_l1147
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v172_l1151 iris-label-pose)


(deftest
 t173_l1153
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v172_l1151)))


(def v175_l1164 (def x-only-pose (-> five-points (pj/pose :x))))


(def v176_l1167 x-only-pose)


(deftest
 t177_l1169
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (= "count" (:y-label plan)))))
   v176_l1167)))


(def
 v179_l1177
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v180_l1182 explicit-label-pose)


(deftest
 t181_l1184
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v180_l1182)))


(def v183_l1196 colored-pose)


(deftest
 t184_l1198
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v183_l1196)))


(def v186_l1210 scatter-pose)


(deftest
 t187_l1212
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v186_l1210)))


(def v189_l1217 fixed-color-pose)


(deftest
 t190_l1219
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v189_l1217)))


(def
 v192_l1224
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v193_l1228 continuous-color-pose)


(deftest
 t194_l1230
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v193_l1228)))


(def
 v196_l1243
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v197_l1247 size-legend-pose)


(deftest
 t198_l1249
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v197_l1247)))


(def v200_l1259 scatter-pose)


(deftest
 t201_l1261
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v200_l1259)))


(def
 v203_l1270
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v204_l1274 alpha-legend-pose)


(deftest
 t205_l1276
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v204_l1274)))


(deftest
 t207_l1285
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v204_l1274)))


(def v209_l1295 scatter-pose)


(def
 v210_l1297
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v211_l1304 full-layout-pose)


(deftest
 t212_l1306
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
   v211_l1304)))


(def v214_l1328 scatter-pose)


(deftest
 t215_l1330
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
          (fn* [p1__80874#] (get-in p1__80874# [:plan :layout-type]))
          (:sub-plots plan))))))))
   v214_l1328)))


(def
 v217_l1354
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v218_l1358 normal-pose)


(def
 v219_l1360
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v220_l1365 flip-pose)


(deftest
 t221_l1367
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
   v220_l1365)))


(def
 v223_l1382
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v224_l1387 flipped-labels-pose)


(deftest
 t225_l1389
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v224_l1387)))


(def
 v227_l1406
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v228_l1412 multi-pose)


(deftest
 t229_l1414
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v228_l1412)))


(def
 v231_l1430
 (def
  two-columns-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length)
   (pj/lay-point {:y :sepal-width})
   (pj/lay-point {:y :petal-width}))))


(def v232_l1436 two-columns-pose)


(deftest
 t233_l1438
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
   v232_l1436)))


(def
 v235_l1450
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-point {:y :sepal-width, :color "#7f7f7f"})
  (pj/lay-point {:y :petal-width})))


(deftest
 t236_l1455
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
   v235_l1450)))


(def
 v238_l1473
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
