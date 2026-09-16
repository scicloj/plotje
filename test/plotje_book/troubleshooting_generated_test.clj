(ns
 plotje-book.troubleshooting-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l29 (tc/column-names (rdatasets/datasets-iris)))


(deftest t4_l31 (is ((fn [v] (some #{:sepal-length} v)) v3_l29)))


(def
 v6_l37
 (try
  (->
   (tc/dataset {"sepal_length" [5.0 6.0], "sepal_width" [3.0 3.5]})
   (pj/pose :sepal_length :sepal_width)
   pj/lay-point
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t7_l43
 (is ((fn [msg] (re-find #"Column :sepal_\w+.*not found" msg)) v6_l37)))


(def
 v9_l54
 (try
  (->
   (tc/dataset {"sepal length" [5.0 6.0], "sepal width" [3.0 3.5]})
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t10_l60
 (is ((fn [msg] (re-find #"Column :sepal-\w+.*not found" msg)) v9_l54)))


(def
 v12_l84
 (-> (rdatasets/datasets-iris) (pj/pose :species :sepal-width)))


(deftest
 t13_l87
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v12_l84)))


(def
 v15_l91
 (-> (rdatasets/datasets-iris) (pj/lay-point :species :sepal-width)))


(deftest
 t16_l94
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v15_l91)))


(def
 v18_l106
 (def
  subject-scores
  {:day [1 2 3 4 1 2 3 4 1 2 3 4],
   :score [3 5 4 6 6 7 5 8 8 9 7 10],
   :subject [1 1 1 1 2 2 2 2 3 3 3 3]}))


(def
 v20_l117
 (-> subject-scores (pj/lay-line :day :score {:color :subject})))


(deftest
 t21_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (contains? (:colors s) "rgb(51,51,51)"))))
   v20_l117)))


(def
 v23_l130
 (->
  subject-scores
  (pj/lay-line
   :day
   :score
   {:color :subject, :color-type :categorical})))


(deftest
 t24_l133
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v23_l130)))


(def
 v26_l154
 (try
  (->
   {:hour [9 9 10 10 11 11], :value [1 2 3 4 5 6]}
   (pj/lay-boxplot :hour :value)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t27_l160
 (is
  ((fn [msg] (re-find #"requires a categorical column" msg)) v26_l154)))


(def
 v29_l167
 (->
  {:hour [9 9 10 10 11 11], :value [1 2 3 4 5 6]}
  (pj/lay-boxplot :hour :value {:x-type :categorical})))


(deftest
 t30_l170
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v29_l167)))


(def
 v32_l192
 (try
  (->
   {:cohort [2020 2021 2022], :n [3 5 4]}
   (pj/lay-bar :cohort :n {:x-type :categorical})
   (pj/lay-text {:x 2021, :y 5.5, :text "the 2021 cohort"})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l199
 (is
  ((fn
    [msg]
    (and
     (re-find #"got 2021 for :x" msg)
     (re-find #"past the ends of this axis" msg)
     (re-find #"\[\"2020\" \"2021\" \"2022\"\]" msg)))
   v32_l192)))


(def
 v35_l208
 (->
  {:cohort [2020 2021 2022], :n [3 5 4]}
  (pj/lay-bar :cohort :n {:x-type :categorical})
  (pj/lay-text
   {:x {:value "2021"},
    :y 5.5,
    :align-x :center,
    :text "on the band"})))


(deftest
 t36_l213
 (is
  ((fn
    [fr]
    (=
     ["2020" "2021" "2022"]
     (->> fr pj/plan :panels first :x-domain)))
   v35_l208)))


(def
 v38_l233
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct)
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :offset-y -6})))


(deftest
 t39_l237
 (is
  ((fn
    [fr]
    (=
     [nil -6]
     (->> fr pj/plan :panels first :layers (mapv :offset-y))))
   v38_l233)))


(def
 v41_l247
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t42_l252
 (is
  ((fn
    [fr]
    (=
     :right
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      (filter (fn* [p1__75086#] (= :text (:mark p1__75086#))))
      first
      :style
      :align-x)))
   v41_l247)))


(def
 v44_l277
 (with-out-str
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:scale-y :log})
   pj/plan)))


(deftest
 t45_l282
 (is
  ((fn [out] (re-find #"does not recognize option.*:scale-y" out))
   v44_l277)))


(def
 v47_l287
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)))


(deftest
 t48_l291
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v47_l287)))


(def
 v50_l315
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l321
 (is ((fn [msg] (re-find #"uses only the x column" msg)) v50_l315)))


(def
 v53_l326
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t54_l329
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v53_l326)))


(def
 v56_l338
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t57_l345
 (is ((fn [msg] (re-find #"[Ll]og scale" msg)) v56_l338)))


(def
 v59_l362
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t60_l369
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v59_l362)))


(def
 v62_l375
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t63_l380
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l375)))


(def
 v65_l400
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t66_l404
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v65_l400)))


(def
 v68_l417
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t69_l424
 (is ((fn [msg] (re-find #"Faceting is plot-level" msg)) v68_l417)))


(def
 v71_l430
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t72_l434
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v71_l430)))


(def
 v74_l447
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 6.5, :y 3.5, :text "mean"})))


(deftest
 t75_l451
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v74_l447)))


(def
 v77_l459
 (->
  {:team ["North" "South" "East" "West" "Central"],
   :spend [12 19 15 24 31],
   :revenue [30 45 38 62 74]}
  (pj/lay-point :spend :revenue)
  (pj/lay-text {:x 33, :y :revenue, :text :team})))


(deftest
 t78_l465
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:points s))
      (every?
       (set (:texts s))
       ["North" "South" "East" "West" "Central"]))))
   v77_l459)))


(def v80_l494 (-> (tc/dataset [[1 2] [3 4] [5 7]]) (pj/lay-point 0 1)))


(deftest
 t81_l497
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v80_l494)))


(def
 v83_l506
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t84_l510
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v83_l506)))


(def
 v86_l526
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})))


(deftest
 t87_l530
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v86_l526)))


(def
 v89_l542
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t90_l547
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v89_l542)))


(def
 v92_l577
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v93_l581
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t94_l586
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v93_l581)))


(def
 v96_l593
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t97_l597
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v96_l593)))


(def
 v99_l613
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t100_l619
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v99_l613)))


(def
 v102_l625
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t103_l633
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v102_l625)))


(def
 v105_l651
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})))


(deftest
 t106_l654
 (is
  ((fn
    [fr]
    (let
     [radii
      (fn* [p1__75087#] (sort (:sizes (pj/svg-summary p1__75087#))))
      now
      (radii fr)
      before
      (radii (-> fr (pj/scale :size {:by :linear})))]
     (and
      (= (first now) (first before))
      (= (last now) (last before))
      (every?
       (fn [[a b]] (> a b))
       (map vector (butlast (rest now)) (butlast (rest before)))))))
   v105_l651)))


(def
 v108_l670
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :linear})))


(deftest
 t109_l674
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v108_l670)))


(def
 v111_l687
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-band-h {:y-min 3.0, :y-max 3.0}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t112_l693
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min < :y-max" msg)
     (re-find #"lay-rule-h" msg)))
   v111_l687)))


(def
 v114_l700
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t115_l704
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v114_l700)))


(def
 v117_l718
 (with-out-str
  (pj/with-config {:annotation-stroke "firebrick"} (constantly nil))))


(deftest
 t118_l721
 (is
  ((fn
    [msg]
    (and
     (re-find #"does not recognize configuration key" msg)
     (re-find #":annotation-stroke was renamed to :rule-color" msg)))
   v117_l718)))


(def
 v120_l731
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/options {:rule-color "firebrick"})))


(deftest
 t121_l736
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v120_l731)))


(def
 v123_l755
 (->
  {:x [1 2 3], :y [2 4 3], :r [1 2 3]}
  (pj/pose :x :y)
  (pj/lay-line {:size 2})
  (pj/lay-point {:size :r})))


(deftest
 t124_l760
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v123_l755)))


(def
 v126_l777
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v127_l780
 (defn
  point-xs
  [pose]
  (->
   pose
   pj/plan
   :panels
   first
   :layers
   first
   :groups
   (->> (mapcat :xs) sort vec))))


(def
 v128_l784
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t129_l787 (is ((fn [v] (true? v)) v128_l784)))


(def
 v131_l796
 (->
  {:cat ["A" "A" "B" "B" "C" "C"],
   :y [10 20 30 40 50 60],
   :group ["a" "b" "a" "b" "a" "b"]}
  (pj/lay-bar :cat :y {:color :group, :position :dodge})))


(deftest
 t132_l801
 (is ((fn [v] (= 6 (:polygons (pj/svg-summary v)))) v131_l796)))


(def
 v134_l817
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t135_l822
 (is
  ((fn
    [v]
    (zero?
     (count
      (filter
       #{"soybean"
         "meatmeal"
         "sunflower"
         "horsebean"
         "casein"
         "linseed"}
       (:texts (pj/svg-summary v))))))
   v134_l817)))


(def
 v137_l831
 (-> (rdatasets/datasets-chickwts) (pj/pose :feed) pj/lay-bar))


(deftest
 t138_l835
 (is
  ((fn
    [v]
    (pos?
     (count
      (filter
       #{"soybean"
         "meatmeal"
         "sunflower"
         "horsebean"
         "casein"
         "linseed"}
       (:texts (pj/svg-summary v))))))
   v137_l831)))


(def
 v140_l850
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y {:fill :v})
   pj/plan)
  (catch Throwable t (.getMessage t))))


(deftest
 t141_l856
 (is
  ((fn [msg] (re-find #"String cannot be cast to.*Number" msg))
   v140_l850)))


(def
 v143_l863
 (->
  (for
   [day (range 1 8) hour (range 0 24)]
   {:day day,
    :hour hour,
    :v (+ (* 0.3 (Math/sin (* 0.5 hour))) (* 0.2 (mod day 3)))})
  (pj/lay-tile :day :hour {:fill :v})
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :tick-labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t144_l871
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v143_l863)))


(def
 v146_l890
 (try
  (->
   {:group [], :measurement []}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t147_l896
 (is
  ((fn
    [msg]
    (re-find #"requires a categorical column.*has no rows" msg))
   v146_l890)))


(def
 v149_l903
 (try
  (->
   {:group [nil nil], :measurement [nil nil]}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t150_l909
 (is ((fn [msg] (re-find #"has no values" msg)) v149_l903)))


(def
 v152_l928
 (try
  (-> {:x [1 2], :y [1 2]} (pj/lay-text :x :y {:text :nope}) pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t153_l934
 (is ((fn [msg] (re-find #"not a label either" msg)) v152_l928)))


(def
 v155_l951
 (try
  (->
   {:height [1 2 3], :weight [1 2 3]}
   (pj/lay-point :height :weight)
   (pj/scale :y {:domain [0]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t156_l958
 (is
  ((fn [msg] (re-find #"not a pair of two finite numbers" msg))
   v155_l951)))
