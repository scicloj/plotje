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
        [p1__80614#]
        (-> p1__80614# :plan :panels first :x-ticks :labels))
       (:sub-plots (pj/plan v)))]
     (and
      (every?
       (fn* [p1__80615#] (re-matches #"\d{2}:\d{2}" p1__80615#))
       six-hours)
      (every?
       (fn*
        [p1__80616#]
        (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__80616#))
       three-days)
      (every?
       (fn* [p1__80617#] (re-matches #"\d{4}" p1__80617#))
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
 v88_l676
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v89_l680 study-continuous-pose)


(deftest
 t90_l682
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
   v89_l680)))


(def
 v92_l691
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v93_l696 study-categorical-pose)


(deftest
 t94_l698
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
   v93_l696)))


(def
 v96_l710
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t97_l718
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v96_l710)))


(def
 v99_l728
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v100_l733
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v101_l737 explicit-group-pose)


(deftest
 t102_l739
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v101_l737)))


(def
 v104_l759
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t105_l764
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v104_l759)))


(def
 v107_l770
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l775
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v107_l770)))


(def v110_l821 (-> five-points (pj/pose :x :x)))


(deftest
 t111_l824
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
   v110_l821)))


(def
 v113_l857
 (->
  (rdatasets/datasets-iris)
  (pj/matrix [:sepal-length :sepal-width])))


(deftest
 t114_l860
 (is
  ((fn
    [v]
    (=
     [[:bar] [:point] [:point] [:bar]]
     (mapv
      (fn*
       [p1__80618#]
       (mapv :mark (-> p1__80618# :plan :panels first :layers)))
      (:sub-plots (pj/plan v)))))
   v113_l857)))


(def v116_l872 (def hist-pose (-> five-points (pj/pose :x))))


(def v117_l876 hist-pose)


(deftest
 t118_l878
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v117_l876)))


(def
 v120_l891
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


(def v121_l896 temporal-hist-pose)


(deftest
 t122_l898
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v121_l896)))


(def v124_l906 (def count-pose (-> animals (pj/pose :animal))))


(def v125_l910 count-pose)


(deftest
 t126_l912
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v125_l910)))


(def
 v128_l925
 (def count-pose-on-y (-> animals (pj/pose {:y :animal}))))


(def v129_l929 count-pose-on-y)


(deftest
 t130_l931
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
   v129_l929)))


(def v132_l950 (-> animals (pj/lay-point {:y :count})))


(deftest
 t133_l953
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (= "animal" (:x-label plan))
      (= "count" (:y-label plan))
      (= 4 (:points (pj/svg-summary v))))))
   v132_l950)))


(def v135_l964 (-> animals (pj/lay-bar {:y :animal})))


(deftest
 t136_l967
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
   v135_l964)))


(def v138_l977 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v139_l980 num-num-pose)


(deftest
 t140_l982
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v139_l980)))


(def
 v142_l991
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v143_l996 ts-line-pose)


(deftest
 t144_l998
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v143_l996)))


(def
 v146_l1007
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v147_l1012 boxplot-pose)


(deftest
 t148_l1014
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v147_l1012)))


(def
 v150_l1024
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v151_l1029 horizontal-boxplot-pose)


(deftest
 t152_l1031
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
   v151_l1029)))


(def v154_l1043 scatter-pose)


(deftest
 t155_l1045
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v154_l1043)))


(def v157_l1066 bar-pose)


(deftest
 t158_l1068
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v157_l1066)))


(def
 v160_l1075
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v161_l1080 fill-pose)


(deftest
 t162_l1082
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v161_l1080)))


(def v164_l1114 scatter-pose)


(deftest
 t165_l1116
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
   v164_l1114)))


(def
 v167_l1129
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v168_l1135 log-scale-pose)


(deftest
 t169_l1137
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v168_l1135)))


(def v171_l1149 bar-pose)


(deftest
 t172_l1151
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v171_l1149)))


(def
 v174_l1167
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v175_l1171 iris-label-pose)


(deftest
 t176_l1173
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v175_l1171)))


(def v178_l1184 (def x-only-pose (-> five-points (pj/pose :x))))


(def v179_l1187 x-only-pose)


(deftest
 t180_l1189
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (= "count" (:y-label plan)))))
   v179_l1187)))


(def
 v182_l1197
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v183_l1202 explicit-label-pose)


(deftest
 t184_l1204
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v183_l1202)))


(def v186_l1216 colored-pose)


(deftest
 t187_l1218
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v186_l1216)))


(def v189_l1230 scatter-pose)


(deftest
 t190_l1232
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v189_l1230)))


(def v192_l1237 fixed-color-pose)


(deftest
 t193_l1239
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v192_l1237)))


(def
 v195_l1244
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v196_l1248 continuous-color-pose)


(deftest
 t197_l1250
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v196_l1248)))


(def
 v199_l1263
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v200_l1267 size-legend-pose)


(deftest
 t201_l1269
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v200_l1267)))


(def v203_l1279 scatter-pose)


(deftest
 t204_l1281
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v203_l1279)))


(def
 v206_l1290
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v207_l1294 alpha-legend-pose)


(deftest
 t208_l1296
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v207_l1294)))


(deftest
 t210_l1305
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v207_l1294)))


(def v212_l1315 scatter-pose)


(def
 v213_l1317
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v214_l1324 full-layout-pose)


(deftest
 t215_l1326
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
   v214_l1324)))


(def v217_l1348 scatter-pose)


(deftest
 t218_l1350
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
          (fn* [p1__80619#] (get-in p1__80619# [:plan :layout-type]))
          (:sub-plots plan))))))))
   v217_l1348)))


(def
 v220_l1374
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v221_l1378 normal-pose)


(def
 v222_l1380
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v223_l1385 flip-pose)


(deftest
 t224_l1387
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
   v223_l1385)))


(def
 v226_l1402
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v227_l1407 flipped-labels-pose)


(deftest
 t228_l1409
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v227_l1407)))


(def
 v230_l1426
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v231_l1432 multi-pose)


(deftest
 t232_l1434
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v231_l1432)))


(def
 v234_l1450
 (def
  two-columns-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length)
   (pj/lay-point {:y :sepal-width})
   (pj/lay-point {:y :petal-width}))))


(def v235_l1456 two-columns-pose)


(deftest
 t236_l1458
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
   v235_l1456)))


(def
 v238_l1470
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length)
  (pj/lay-point {:y :sepal-width, :color "#7f7f7f"})
  (pj/lay-point {:y :petal-width})))


(deftest
 t239_l1475
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
   v238_l1470)))


(def
 v241_l1493
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
