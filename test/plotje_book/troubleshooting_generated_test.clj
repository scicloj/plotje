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
  (pj/lay-text {:x {:value "2021"}, :y 5.5, :text "on the band"})))


(deftest
 t36_l212
 (is
  ((fn
    [fr]
    (=
     ["2020" "2021" "2022"]
     (->> fr pj/plan :panels first :x-domain)))
   v35_l208)))


(def
 v38_l232
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct)
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :offset-y -6})))


(deftest
 t39_l236
 (is
  ((fn
    [fr]
    (=
     [nil -6]
     (->> fr pj/plan :panels first :layers (mapv :offset-y))))
   v38_l232)))


(def
 v41_l246
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t42_l251
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
      (filter (fn* [p1__73092#] (= :text (:mark p1__73092#))))
      first
      :style
      :align-x)))
   v41_l246)))


(def
 v44_l276
 (with-out-str
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:scale-y :log})
   pj/plan)))


(deftest
 t45_l281
 (is
  ((fn [out] (re-find #"does not recognize option.*:scale-y" out))
   v44_l276)))


(def
 v47_l286
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)))


(deftest
 t48_l290
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v47_l286)))


(def
 v50_l314
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l320
 (is ((fn [msg] (re-find #"uses only the x column" msg)) v50_l314)))


(def
 v53_l325
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t54_l328
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v53_l325)))


(def
 v56_l337
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t57_l344
 (is ((fn [msg] (re-find #"[Ll]og scale" msg)) v56_l337)))


(def
 v59_l361
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t60_l368
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v59_l361)))


(def
 v62_l374
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t63_l379
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l374)))


(def
 v65_l399
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t66_l403
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v65_l399)))


(def
 v68_l416
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t69_l423
 (is ((fn [msg] (re-find #"Faceting is plot-level" msg)) v68_l416)))


(def
 v71_l429
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t72_l433
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v71_l429)))


(def
 v74_l446
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 6.5, :y 3.5, :text "mean"})))


(deftest
 t75_l450
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v74_l446)))


(def
 v77_l458
 (->
  {:team ["North" "South" "East" "West" "Central"],
   :spend [12 19 15 24 31],
   :revenue [30 45 38 62 74]}
  (pj/lay-point :spend :revenue)
  (pj/lay-text {:x 33, :y :revenue, :text :team})))


(deftest
 t78_l464
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
   v77_l458)))


(def v80_l493 (-> (tc/dataset [[1 2] [3 4] [5 7]]) (pj/lay-point 0 1)))


(deftest
 t81_l496
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v80_l493)))


(def
 v83_l505
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t84_l509
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v83_l505)))


(def
 v86_l525
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})))


(deftest
 t87_l529
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
   v86_l525)))


(def
 v89_l541
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t90_l546
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
   v89_l541)))


(def
 v92_l576
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v93_l580
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t94_l585
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v93_l580)))


(def
 v96_l592
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t97_l596
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v96_l592)))


(def
 v99_l612
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t100_l618
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v99_l612)))


(def
 v102_l624
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t103_l632
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v102_l624)))


(def
 v105_l650
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})))


(deftest
 t106_l653
 (is
  ((fn
    [fr]
    (let
     [radii
      (fn* [p1__73093#] (sort (:sizes (pj/svg-summary p1__73093#))))
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
   v105_l650)))


(def
 v108_l669
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :linear})))


(deftest
 t109_l673
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v108_l669)))


(def
 v111_l686
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-band-h {:y-min 3.0, :y-max 3.0}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t112_l692
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min < :y-max" msg)
     (re-find #"lay-rule-h" msg)))
   v111_l686)))


(def
 v114_l699
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t115_l703
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v114_l699)))


(def
 v117_l717
 (with-out-str
  (pj/with-config {:annotation-stroke "firebrick"} (constantly nil))))


(deftest
 t118_l720
 (is
  ((fn
    [msg]
    (and
     (re-find #"does not recognize configuration key" msg)
     (re-find #":annotation-stroke was renamed to :rule-color" msg)))
   v117_l717)))


(def
 v120_l730
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/options {:rule-color "firebrick"})))


(deftest
 t121_l735
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v120_l730)))


(def
 v123_l754
 (->
  {:x [1 2 3], :y [2 4 3], :r [1 2 3]}
  (pj/pose :x :y)
  (pj/lay-line {:size 2})
  (pj/lay-point {:size :r})))


(deftest
 t124_l759
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v123_l754)))


(def
 v126_l776
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v127_l779
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
 v128_l783
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t129_l786 (is ((fn [v] (true? v)) v128_l783)))


(def
 v131_l795
 (->
  {:cat ["A" "A" "B" "B" "C" "C"],
   :y [10 20 30 40 50 60],
   :group ["a" "b" "a" "b" "a" "b"]}
  (pj/lay-bar :cat :y {:color :group, :position :dodge})))


(deftest
 t132_l800
 (is ((fn [v] (= 6 (:polygons (pj/svg-summary v)))) v131_l795)))


(def
 v134_l816
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t135_l821
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
   v134_l816)))


(def
 v137_l830
 (-> (rdatasets/datasets-chickwts) (pj/pose :feed) pj/lay-bar))


(deftest
 t138_l834
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
   v137_l830)))


(def
 v140_l849
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y {:fill :v})
   pj/plan)
  (catch Throwable t (.getMessage t))))


(deftest
 t141_l855
 (is
  ((fn [msg] (re-find #"String cannot be cast to.*Number" msg))
   v140_l849)))


(def
 v143_l862
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
 t144_l870
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v143_l862)))


(def
 v146_l889
 (try
  (->
   {:group [], :measurement []}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t147_l895
 (is
  ((fn
    [msg]
    (re-find #"requires a categorical column.*has no rows" msg))
   v146_l889)))


(def
 v149_l902
 (try
  (->
   {:group [nil nil], :measurement [nil nil]}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t150_l908
 (is ((fn [msg] (re-find #"has no values" msg)) v149_l902)))


(def
 v152_l927
 (try
  (-> {:x [1 2], :y [1 2]} (pj/lay-text :x :y {:text :nope}) pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t153_l933
 (is ((fn [msg] (re-find #"not a label either" msg)) v152_l927)))


(def
 v155_l950
 (try
  (->
   {:height [1 2 3], :weight [1 2 3]}
   (pj/lay-point :height :weight)
   (pj/scale :y {:domain [0]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t156_l957
 (is
  ((fn [msg] (re-find #"not a pair of two finite numbers" msg))
   v155_l950)))
