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
 v12_l83
 (-> (rdatasets/datasets-iris) (pj/pose :species :sepal-width)))


(deftest
 t13_l86
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v12_l83)))


(def
 v15_l90
 (-> (rdatasets/datasets-iris) (pj/lay-point :species :sepal-width)))


(deftest
 t16_l93
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v15_l90)))


(def
 v18_l105
 (def
  subject-scores
  {:day [1 2 3 4 1 2 3 4 1 2 3 4],
   :score [3 5 4 6 6 7 5 8 8 9 7 10],
   :subject [1 1 1 1 2 2 2 2 3 3 3 3]}))


(def
 v20_l116
 (-> subject-scores (pj/lay-line :day :score {:color :subject})))


(deftest
 t21_l119
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (contains? (:colors s) "rgb(51,51,51)"))))
   v20_l116)))


(def
 v23_l129
 (->
  subject-scores
  (pj/lay-line
   :day
   :score
   {:color :subject, :color-type :categorical})))


(deftest
 t24_l132
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v23_l129)))


(def
 v26_l153
 (try
  (->
   {:hour [9 9 10 10 11 11], :value [1 2 3 4 5 6]}
   (pj/lay-boxplot :hour :value)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t27_l159
 (is
  ((fn [msg] (re-find #"requires a categorical column" msg)) v26_l153)))


(def
 v29_l166
 (->
  {:hour [9 9 10 10 11 11], :value [1 2 3 4 5 6]}
  (pj/lay-boxplot :hour :value {:x-type :categorical})))


(deftest
 t30_l169
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v29_l166)))


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
  (pj/lay-bar :cohort :n {:x-type :categorical, :color "#a6cee3"})
  (pj/lay-text
   {:x {:value "2021"},
    :y 4.5,
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
 v41_l248
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t42_l253
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
      (filter (fn* [p1__83036#] (= :text (:mark p1__83036#))))
      first
      :style
      :align-x)))
   v41_l248)))


(def
 v44_l278
 (with-out-str
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:scale-y :log})
   pj/plan)))


(deftest
 t45_l283
 (is
  ((fn [out] (re-find #"does not recognize option.*:scale-y" out))
   v44_l278)))


(def
 v47_l288
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)))


(deftest
 t48_l292
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v47_l288)))


(def
 v50_l316
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l322
 (is ((fn [msg] (re-find #"uses only the x column" msg)) v50_l316)))


(def
 v53_l327
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t54_l330
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v53_l327)))


(def
 v56_l339
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t57_l346
 (is ((fn [msg] (re-find #"[Ll]og scale" msg)) v56_l339)))


(def
 v59_l363
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t60_l370
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v59_l363)))


(def
 v62_l376
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t63_l381
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l376)))


(def
 v65_l408
 (with-out-str
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:tooltip true})
   (pj/plot {:format :bufimg}))))


(deftest
 t66_l414
 (is
  ((fn
    [out]
    (and
     (re-find #":tooltip asked for" out)
     (re-find #":bufimg format draws no interaction" out)
     (re-find #"The formats that do: :svg" out)))
   v65_l408)))


(def
 v68_l422
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t69_l426
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (re-find #"data-tooltip" (str (pj/plot v)))))
   v68_l422)))


(def
 v71_l443
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t72_l450
 (is
  ((fn
    [msg]
    (and
     (re-find #"panel aesthetic" msg)
     (re-find #"read from a pose's mapping" msg)))
   v71_l443)))


(def
 v74_l460
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t75_l464
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v74_l460)))


(def
 v77_l477
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 6.5, :y 3.5, :text "mean"})))


(deftest
 t78_l481
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v77_l477)))


(def
 v80_l489
 (->
  {:team ["North" "South" "East" "West" "Central"],
   :spend [12 19 15 24 31],
   :revenue [30 45 38 62 74]}
  (pj/lay-point :spend :revenue)
  (pj/lay-text {:x 33, :y :revenue, :text :team})))


(deftest
 t81_l495
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
   v80_l489)))


(def v83_l524 (-> (tc/dataset [[1 2] [3 4] [5 7]]) (pj/lay-point 0 1)))


(deftest
 t84_l527
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v83_l524)))


(def
 v86_l536
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t87_l540
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v86_l536)))


(def
 v89_l556
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})))


(deftest
 t90_l560
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
   v89_l556)))


(def
 v92_l572
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t93_l577
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
   v92_l572)))


(def
 v95_l607
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v96_l611
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t97_l616
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v96_l611)))


(def
 v99_l623
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t100_l627
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v99_l623)))


(def
 v102_l643
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t103_l649
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v102_l643)))


(def
 v105_l655
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t106_l663
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v105_l655)))


(def
 v108_l681
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})))


(deftest
 t109_l684
 (is
  ((fn
    [fr]
    (let
     [radii
      (fn* [p1__83037#] (sort (:sizes (pj/svg-summary p1__83037#))))
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
   v108_l681)))


(def
 v111_l700
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :linear})))


(deftest
 t112_l704
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v111_l700)))


(def
 v114_l717
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-band-h {:y-min 3.0, :y-max 3.0}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t115_l723
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min < :y-max" msg)
     (re-find #"lay-rule-h" msg)))
   v114_l717)))


(def
 v117_l730
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t118_l734
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v117_l730)))


(def
 v120_l748
 (with-out-str
  (pj/with-config {:annotation-stroke "firebrick"} (constantly nil))))


(deftest
 t121_l751
 (is
  ((fn
    [msg]
    (and
     (re-find #"does not recognize configuration key" msg)
     (re-find #":annotation-stroke was renamed to :rule-color" msg)))
   v120_l748)))


(def
 v123_l761
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/options {:rule-color "firebrick"})))


(deftest
 t124_l766
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v123_l761)))


(def
 v126_l783
 (with-out-str
  (->
   {:team ["red" "green" "blue"], :score [3 5 4]}
   (pj/lay-bar :team :score)
   (pj/lay-text
    {:x {:value "red"}, :y 3, :nudge-x 0.5, :text "note"}))))


(deftest
 t127_l788
 (is
  ((fn
    [out]
    (and
     (re-find #":nudge-x was renamed to :dx" out)
     (re-find #"Write :dx" out)))
   v126_l783)))


(def
 v129_l795
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x {:value "red"},
    :y 3,
    :align-x :center,
    :dx 0.5,
    :offset-y -10,
    :text "half a band"})))


(deftest
 t130_l800
 (is
  ((fn
    [fr]
    (let
     [under-the-old-name
      (atom nil)
      _
      (with-out-str
       (reset!
        under-the-old-name
        (->
         {:team ["red" "green" "blue"], :score [3 5 4]}
         (pj/lay-bar :team :score {:color "#a6cee3"})
         (pj/lay-text
          {:x {:value "red"},
           :y 3,
           :align-x :center,
           :nudge-x 0.5,
           :offset-y -10,
           :text "half a band"}))))]
     (and
      (= (pj/plot fr) (pj/plot (deref under-the-old-name)))
      (not=
       (pj/plot fr)
       (pj/plot
        (->
         {:team ["red" "green" "blue"], :score [3 5 4]}
         (pj/lay-bar :team :score {:color "#a6cee3"})
         (pj/lay-text
          {:x {:value "red"},
           :y 3,
           :align-x :center,
           :offset-y -10,
           :text "half a band"})))))))
   v129_l795)))


(def
 v132_l847
 (->
  {:x [1 2 3], :y [2 4 3], :r [1 2 3]}
  (pj/pose :x :y)
  (pj/lay-line {:size 2})
  (pj/lay-point {:size :r})))


(deftest
 t133_l852
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v132_l847)))


(def
 v135_l870
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v136_l873
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
 v137_l877
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t138_l880 (is ((fn [v] (true? v)) v137_l877)))


(def
 v140_l887
 (->
  points-data
  (pj/lay-point
   :x
   :y
   {:color :group, :position :dodge, :x-type :categorical})))


(deftest
 t141_l890
 (is
  ((fn
    [v]
    (not=
     (pj/plot v)
     (pj/plot
      (->
       points-data
       (pj/lay-point :x :y {:color :group, :x-type :categorical})))))
   v140_l887)))


(def
 v143_l912
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t144_l917
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
   v143_l912)))


(def
 v146_l926
 (-> (rdatasets/datasets-chickwts) (pj/pose :feed) pj/lay-bar))


(deftest
 t147_l930
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
   v146_l926)))


(def
 v149_l943
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y)
   pj/plan)
  (catch Throwable t (ex-message t))))


(deftest
 t150_l949
 (is
  ((fn [msg] (re-find #"requires a numeric column for :x" msg))
   v149_l943)))


(def
 v152_l956
 (->
  (for
   [[i day]
    (map-indexed vector ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])
    hour
    (range 0 24)]
   {:day day,
    :hour hour,
    :v (+ (* 0.3 (Math/sin (* 0.5 hour))) (* 0.2 (mod i 3)))})
  (pj/lay-tile :day :hour {:fill :v})))


(deftest
 t153_l962
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v152_l956)))


(def
 v155_l976
 (try
  (->
   {:group [], :measurement []}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t156_l982
 (is
  ((fn
    [msg]
    (re-find #"requires a categorical column.*has no rows" msg))
   v155_l976)))


(def
 v158_l989
 (try
  (->
   {:group [nil nil], :measurement [nil nil]}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t159_l995
 (is ((fn [msg] (re-find #"has no values" msg)) v158_l989)))


(def
 v161_l1014
 (try
  (-> {:x [1 2], :y [1 2]} (pj/lay-text :x :y {:text :nope}) pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t162_l1020
 (is ((fn [msg] (re-find #"not a label either" msg)) v161_l1014)))


(def
 v164_l1037
 (try
  (->
   {:height [1 2 3], :weight [1 2 3]}
   (pj/lay-point :height :weight)
   (pj/scale :y {:domain [0]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t165_l1044
 (is
  ((fn [msg] (re-find #"not a pair of two finite numbers" msg))
   v164_l1037)))
