(ns
 plotje-book.inference-rules-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l34
 (def five-points {:x [1.0 2.0 3.0 4.0 5.0], :y [2.1 4.3 3.0 5.2 4.8]}))


(def v4_l38 (def scatter-pose (-> five-points (pj/lay-point :x :y))))


(def v5_l42 scatter-pose)


(deftest
 t6_l44
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
   v5_l42)))


(def v8_l138 (-> {:values [1 2 3 4 5 6]} pj/lay-histogram))


(deftest
 t9_l141
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v8_l138)))


(def v11_l145 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t12_l148
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v11_l145)))


(def
 v14_l152
 (-> {:x [1 2 3 4], :y [4 5 6 7], :g ["a" "a" "b" "b"]} pj/lay-point))


(deftest
 t15_l155
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (some #{"a"} (:texts s)))))
   v14_l152)))


(def
 v17_l167
 (def
  two-col-pose
  (pj/pose {:x [1.0 2.0 3.0 4.0 5.0], :y [1.0 4.0 9.0 16.0 25.0]})))


(def v18_l171 two-col-pose)


(deftest
 t19_l173
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l171)))


(def
 v21_l177
 (-> two-col-pose (select-keys [:mapping :layers]) kind/pprint))


(deftest
 t22_l179
 (is
  ((fn
    [pose]
    (and (= {:x :x, :y :y} (:mapping pose)) (empty? (:layers pose))))
   v21_l177)))


(def
 v24_l196
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :petal-length :petal-width {:color :species})))


(deftest
 t25_l199
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v24_l196)))


(def
 v27_l217
 (def
  animals
  {:animal ["cat" "dog" "bird" "fish"], :count [12 8 15 5]}))


(def v28_l221 (def bar-pose (-> animals (pj/lay-bar :animal :count))))


(def v29_l225 bar-pose)


(deftest
 t30_l227
 (is
  ((fn
    [v]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= ["cat" "dog" "bird" "fish"] (:x-domain p))
      (true? (:categorical? (:x-ticks p))))))
   v29_l225)))


(def
 v32_l245
 (def
  temporal-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"
     #inst "2024-12-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/lay-point :date :val))))


(def v33_l250 temporal-pose)


(deftest
 t34_l252
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan temporal-pose)))]
     (and
      (number? (first (:x-domain p)))
      (= 6 (count (:values (:x-ticks p))))
      (= "Jan 2024" (first (:labels (:x-ticks p)))))))
   v33_l250)))


(def
 v36_l266
 (let
  [panel (first (:panels (pj/plan temporal-pose)))]
  {:x-scale (:x-scale panel),
   :x-domain (:x-domain panel),
   :x-tick-labels (:labels (:x-ticks panel))}))


(deftest
 t37_l271
 (is
  ((fn
    [m]
    (and
     (= {:type :linear} (:x-scale m))
     (every? number? (:x-domain m))
     (= 6 (count (:x-tick-labels m)))))
   v36_l266)))


(def
 v39_l284
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
 t40_l292
 (is
  ((fn
    [m]
    (and
     (every?
      (fn* [p1__11193#] (re-matches #"\d{2}:\d{2}" p1__11193#))
      (:six-hours m))
     (every?
      (fn*
       [p1__11194#]
       (re-matches #"[A-Z][a-z]{2} \d{2}:\d{2}" p1__11194#))
      (:three-days m))
     (every?
      (fn* [p1__11195#] (re-matches #"\d{4}" p1__11195#))
      (:nine-years m))
     (= 9 (count (:nine-years m)))))
   v39_l284)))


(def
 v42_l316
 (def
  hour-bar-pose
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count {:x-type :categorical}))))


(def v43_l320 hour-bar-pose)


(deftest
 t44_l322
 (is
  ((fn
    [v]
    (and
     (= 4 (:polygons (pj/svg-summary v)))
     (=
      ["9" "10" "11" "12"]
      (:x-domain (first (:panels (pj/plan hour-bar-pose)))))))
   v43_l320)))


(def
 v46_l344
 (def
  colored-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g}))))


(def v47_l350 colored-pose)


(deftest
 t48_l352
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
   v47_l350)))


(def
 v50_l370
 (def
  fixed-color-pose
  (-> five-points (pj/lay-point :x :y {:color "#E74C3C"}))))


(def v51_l374 fixed-color-pose)


(deftest
 t52_l376
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
   v51_l374)))


(def
 v54_l400
 (-> five-points (pj/lay-point :x :y {:color "steelblue"})))


(deftest
 t55_l403
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v54_l400)))


(def
 v57_l443
 (def
  red-color-pose
  (-> five-points (pj/lay-point :x :y {:color "red"}))))


(def v58_l447 red-color-pose)


(deftest
 t59_l449
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
   v58_l447)))


(def v61_l482 colored-pose)


(deftest
 t62_l484
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
   v61_l482)))


(def
 v64_l499
 (->
  {:x [1 2 3], :y [4 5 6], :hex ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-point :x :y {:color {:column :hex, :scale false}})))


(deftest
 t65_l502
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v64_l499)))


(def
 v67_l518
 (def
  numeric-color-pose
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4], :val [10 20 30 40 50]}
   (pj/lay-point :x :y {:color :val}))))


(def v68_l524 numeric-color-pose)


(deftest
 t69_l526
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
   v68_l524)))


(def
 v71_l548
 (def
  study-data
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}))


(def
 v73_l555
 (def
  study-continuous-pose
  (-> study-data (pj/lay-line :day :score {:color :subject}))))


(def v74_l559 study-continuous-pose)


(deftest
 t75_l561
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
   v74_l559)))


(def
 v77_l570
 (def
  study-categorical-pose
  (->
   study-data
   (pj/lay-line
    :day
    :score
    {:color :subject, :color-type :categorical}))))


(def v78_l575 study-categorical-pose)


(deftest
 t79_l577
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
   v78_l575)))


(def
 v81_l589
 (->
  {:subject [1 1 1 2 2 2 3 3 3],
   :day [1 2 3 1 2 3 1 2 3],
   :score [5 7 6 3 4 5 8 9 7]}
  (pj/lay-line :day :score {:color :subject, :color-type :categorical})
  pj/lay-point
  (pj/options {:title "Scores by Subject (categorical override)"})))


(deftest
 t82_l597
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (pos? (:lines s)) (pos? (:points s)))))
   v81_l589)))


(def
 v84_l607
 (def
  grouped-data
  {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}))


(def
 v85_l612
 (def
  explicit-group-pose
  (-> grouped-data (pj/lay-point :x :y {:group :g}))))


(def v86_l616 explicit-group-pose)


(deftest
 t87_l618
 (is
  ((fn
    [_]
    (let
     [plan
      (pj/plan explicit-group-pose)
      layer
      (first (:layers (first (:panels plan))))]
     (and (= 2 (count (:groups layer))) (nil? (:legend plan)))))
   v86_l616)))


(def
 v89_l638
 (->
  grouped-data
  (pj/pose :x :y)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t90_l643
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 1 (:lines s)))))
   v89_l638)))


(def
 v92_l649
 (->
  grouped-data
  (pj/pose :x :y {:color :g})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t93_l654
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v92_l649)))


(def v95_l697 (def hist-pose (-> five-points (pj/pose :x))))


(def v96_l701 hist-pose)


(deftest
 t97_l703
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v96_l701)))


(def
 v99_l716
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


(def v100_l721 temporal-hist-pose)


(deftest
 t101_l723
 (is
  ((fn
    [v]
    (let
     [layer
      (first (:layers (first (:panels (pj/plan temporal-hist-pose)))))]
     (and
      (pos? (:polygons (pj/svg-summary v)))
      (= :bar (:mark layer)))))
   v100_l721)))


(def v103_l731 (def count-pose (-> animals (pj/pose :animal))))


(def v104_l735 count-pose)


(deftest
 t105_l737
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan count-pose)))))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= :rect (:mark layer)))))
   v104_l735)))


(def v107_l749 (def num-num-pose (-> five-points (pj/pose :x :y))))


(def v108_l752 num-num-pose)


(deftest
 t109_l754
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan num-num-pose)))))]
     (and
      (= 5 (:points (pj/svg-summary v)))
      (= :point (:mark layer)))))
   v108_l752)))


(def
 v111_l763
 (def
  ts-line-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"],
    :val [10 25 18]}
   (pj/pose :date :val))))


(def v112_l768 ts-line-pose)


(deftest
 t113_l770
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan ts-line-pose)))))]
     (and (= 1 (:lines (pj/svg-summary v))) (= :line (:mark layer)))))
   v112_l768)))


(def
 v115_l779
 (def
  boxplot-pose
  (->
   {:species ["a" "a" "a" "b" "b" "b" "c" "c" "c"],
    :val [8 10 12 18 20 22 14 15 17]}
   (pj/pose :species :val))))


(def v116_l784 boxplot-pose)


(deftest
 t117_l786
 (is
  ((fn
    [v]
    (let
     [layer (first (:layers (first (:panels (pj/plan boxplot-pose)))))]
     (and
      (pos? (:lines (pj/svg-summary v)))
      (= :boxplot (:mark layer))
      (= 3 (count (:boxes layer))))))
   v116_l784)))


(def
 v119_l796
 (def
  horizontal-boxplot-pose
  (->
   {:val [8 10 12 18 20 22 14 15 17],
    :species ["a" "a" "a" "b" "b" "b" "c" "c" "c"]}
   (pj/pose :val :species))))


(def v120_l801 horizontal-boxplot-pose)


(deftest
 t121_l803
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
   v120_l801)))


(def v123_l815 scatter-pose)


(deftest
 t124_l817
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan scatter-pose)))]
     (and
      (== 0.8 (first (:x-domain p)))
      (== 5.2 (second (:x-domain p))))))
   v123_l815)))


(def v126_l838 bar-pose)


(deftest
 t127_l840
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (<= (first (:y-domain p)) 0)))
   v126_l838)))


(def
 v129_l847
 (def
  fill-pose
  (->
   {:x ["a" "a" "b" "b"], :g ["m" "n" "m" "n"]}
   (pj/lay-bar :x {:position :fill, :color :g}))))


(def v130_l852 fill-pose)


(deftest
 t131_l854
 (is
  ((fn
    [_]
    (let
     [d (:y-domain (first (:panels (pj/plan fill-pose))))]
     (and (== 0.0 (first d)) (== 1.0 (second d)))))
   v130_l852)))


(def v133_l886 scatter-pose)


(deftest
 t134_l888
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
   v133_l886)))


(def
 v136_l901
 (def
  log-scale-pose
  (->
   {:x [0.1 1.0 10.0 100.0 1000.0], :y [5 10 15 20 25]}
   (pj/lay-point :x :y)
   (pj/scale :x :log))))


(def v137_l907 log-scale-pose)


(deftest
 t138_l909
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan log-scale-pose)))]
     (and
      (= [0.1 1.0 10.0 100.0 1000.0] (:values (:x-ticks p)))
      (= ["0.1" "1" "10" "100" "1000"] (:labels (:x-ticks p))))))
   v137_l907)))


(def v140_l921 bar-pose)


(deftest
 t141_l923
 (is
  ((fn
    [_]
    (let
     [p (first (:panels (pj/plan bar-pose)))]
     (= ["cat" "dog" "bird" "fish"] (:values (:x-ticks p)))))
   v140_l921)))


(def
 v143_l939
 (def
  iris-label-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v144_l943 iris-label-pose)


(deftest
 t145_l945
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan iris-label-pose)]
     (and
      (= "sepal length" (:x-label plan))
      (= "sepal width" (:y-label plan)))))
   v144_l943)))


(def v147_l955 (def x-only-pose (-> five-points (pj/pose :x))))


(def v148_l958 x-only-pose)


(deftest
 t149_l960
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan x-only-pose)]
     (and (= "x" (:x-label plan)) (nil? (:y-label plan)))))
   v148_l958)))


(def
 v151_l968
 (def
  explicit-label-pose
  (->
   five-points
   (pj/lay-point :x :y)
   (pj/options {:x-label "Length (cm)", :y-label "Width (cm)"}))))


(def v152_l973 explicit-label-pose)


(deftest
 t153_l975
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan explicit-label-pose)]
     (and
      (= "Length (cm)" (:x-label plan))
      (= "Width (cm)" (:y-label plan)))))
   v152_l973)))


(def v155_l987 colored-pose)


(deftest
 t156_l989
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan colored-pose))]
     (and (= :g (:title leg)) (= 2 (count (:entries leg))))))
   v155_l987)))


(def v158_l1000 scatter-pose)


(deftest
 t159_l1002
 (is ((fn [_] (nil? (:legend (pj/plan scatter-pose)))) v158_l1000)))


(def v161_l1007 fixed-color-pose)


(deftest
 t162_l1009
 (is ((fn [_] (nil? (:legend (pj/plan fixed-color-pose)))) v161_l1007)))


(def
 v164_l1014
 (def
  continuous-color-pose
  (->
   {:x [1 2 3], :y [4 5 6], :val [10 20 30]}
   (pj/lay-point :x :y {:color :val}))))


(def v165_l1018 continuous-color-pose)


(deftest
 t166_l1020
 (is
  ((fn
    [_]
    (let
     [leg (:legend (pj/plan continuous-color-pose))]
     (and (= :continuous (:type leg)) (= 20 (count (:stops leg))))))
   v165_l1018)))


(def
 v168_l1033
 (def
  size-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :s [10 20 30 40 50]}
   (pj/lay-point :x :y {:size :s}))))


(def v169_l1037 size-legend-pose)


(deftest
 t170_l1039
 (is
  ((fn
    [_]
    (let
     [leg (:size-legend (pj/plan size-legend-pose))]
     (and
      (= :size (:type leg))
      (= :s (:title leg))
      (= 5 (count (:entries leg))))))
   v169_l1037)))


(def v172_l1049 scatter-pose)


(deftest
 t173_l1051
 (is
  ((fn [_] (nil? (:size-legend (pj/plan scatter-pose)))) v172_l1049)))


(def
 v175_l1060
 (def
  alpha-legend-pose
  (->
   {:x [1 2 3 4 5], :y [1 2 3 4 5], :a [0.1 0.3 0.5 0.7 0.9]}
   (pj/lay-point :x :y {:alpha :a}))))


(def v176_l1064 alpha-legend-pose)


(deftest
 t177_l1066
 (is
  ((fn
    [_]
    (let
     [leg (:alpha-legend (pj/plan alpha-legend-pose))]
     (and
      (= :alpha (:type leg))
      (= :a (:title leg))
      (= 4 (count (:entries leg))))))
   v176_l1064)))


(def v179_l1075 scatter-pose)


(deftest
 t180_l1077
 (is
  ((fn [_] (nil? (:alpha-legend (pj/plan scatter-pose)))) v179_l1075)))


(def v182_l1087 scatter-pose)


(def
 v183_l1089
 (def
  full-layout-pose
  (->
   {:x [1 2 3 4 5 6], :y [3 5 4 7 6 8], :g ["a" "a" "a" "b" "b" "b"]}
   (pj/lay-point :x :y {:color :g})
   (pj/options {:title "My Plot"}))))


(def v184_l1096 full-layout-pose)


(deftest
 t185_l1098
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
   v184_l1096)))


(def v187_l1117 scatter-pose)


(deftest
 t188_l1119
 (is
  ((fn [_] (= :single (:layout-type (pj/plan scatter-pose))))
   v187_l1117)))


(def
 v190_l1128
 (def normal-pose (-> animals (pj/lay-bar :animal :count))))


(def v191_l1132 normal-pose)


(def
 v192_l1134
 (def
  flip-pose
  (-> animals (pj/lay-bar :animal :count) (pj/coord :flip))))


(def v193_l1139 flip-pose)


(deftest
 t194_l1141
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
   v193_l1139)))


(def
 v196_l1156
 (def
  flipped-labels-pose
  (-> five-points (pj/lay-point :x :y) (pj/coord :flip))))


(def v197_l1161 flipped-labels-pose)


(deftest
 t198_l1163
 (is
  ((fn
    [_]
    (let
     [plan (pj/plan flipped-labels-pose)]
     (and (= "y" (:x-label plan)) (= "x" (:y-label plan)))))
   v197_l1161)))


(def
 v200_l1180
 (def
  multi-pose
  (->
   five-points
   (pj/pose :x :y)
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v201_l1186 multi-pose)


(deftest
 t202_l1188
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) p (first (:panels (pj/plan multi-pose)))]
     (and
      (= 5 (:points s))
      (= 1 (:lines s))
      (= 2 (count (:layers p))))))
   v201_l1186)))


(def
 v204_l1205
 (kind/mermaid
  "\ngraph TD\n  POSE[\"pose + options\"]\n  POSE --> CT[\"Column types\"]\n  POSE --> AE[\"Aesthetics\"]\n  CT --> GR[\"Grouping\"]\n  AE --> GR\n  CT --> ME[\"Layer type\"]\n  GR --> STATS[\"Statistics\"]\n  ME --> STATS\n\n  STATS --> DOM[\"Domains\"]\n  DOM --> TK[\"Ticks\"]\n\n  POSE --> LBL[\"Axis labels\"]\n  AE --> LEG[\"Color legend\"]\n  AE --> SLEG[\"Size legend\"]\n  AE --> ALEG[\"Alpha legend\"]\n\n  DOM --> LAYOUT[\"Layout\"]\n  LBL --> LAYOUT\n  LEG --> LAYOUT\n  SLEG --> LAYOUT\n  ALEG --> LAYOUT\n\n  DOM --> PLOT[\"Rendered plot\"]\n  TK --> PLOT\n  LBL --> PLOT\n  LEG --> PLOT\n  SLEG --> PLOT\n  ALEG --> PLOT\n  LAYOUT --> PLOT\n  STATS --> PLOT\n\n  style POSE fill:#e8f5e9\n  style PLOT fill:#fff3e0\n  style STATS fill:#e3f2fd\n  style DOM fill:#e3f2fd\n"))
