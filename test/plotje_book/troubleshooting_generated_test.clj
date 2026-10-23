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
 v32_l193
 (try
  (->
   {:cohort [2020 2021 2022], :n [3 5 4]}
   (pj/lay-bar :cohort :n {:x-type :categorical})
   (pj/lay-text {:x 2021, :y 5.5, :text "the 2021 cohort"})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l200
 (is
  ((fn
    [msg]
    (and
     (re-find #"got 2021 for :x" msg)
     (re-find #"past the ends of this axis" msg)
     (re-find #"\[\"2020\" \"2021\" \"2022\"\]" msg)))
   v32_l193)))


(def
 v35_l209
 (->
  {:cohort [2020 2021 2022], :n [3 5 4]}
  (pj/lay-bar :cohort :n {:x-type :categorical, :color "#a6cee3"})
  (pj/lay-text
   {:x {:value "2021"},
    :y 4.5,
    :align-x :center,
    :text "on the band"})))


(deftest
 t36_l214
 (is
  ((fn
    [fr]
    (=
     ["2020" "2021" "2022"]
     (->> fr pj/plan :panels first :x-domain)))
   v35_l209)))


(def
 v38_l234
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct)
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :offset-y -6})))


(deftest
 t39_l238
 (is
  ((fn
    [fr]
    (=
     [nil -6]
     (->> fr pj/plan :panels first :layers (mapv :offset-y))))
   v38_l234)))


(def
 v41_l249
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t42_l254
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
      (filter (fn* [p1__82520#] (= :text (:mark p1__82520#))))
      first
      :style
      :align-x)))
   v41_l249)))


(def
 v44_l279
 (with-out-str
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:scale-y :log})
   pj/plan)))


(deftest
 t45_l284
 (is
  ((fn [out] (re-find #"does not recognize.*:scale-y" out)) v44_l279)))


(def
 v47_l289
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)))


(deftest
 t48_l293
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v47_l289)))


(def
 v50_l321
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l327
 (is
  ((fn
    [msg]
    (re-find
     #"draws from one column, and was given :sepal-length on :x and :sepal-width on :y"
     msg))
   v50_l321)))


(def
 v53_l332
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t54_l335
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v53_l332)))


(def
 v56_l345
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t57_l352
 (is
  ((fn
    [msg]
    (re-find
     #"Log scale on :x requires numeric data, but column :species is non-numeric"
     msg))
   v56_l345)))


(def
 v59_l369
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t60_l376
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v59_l369)))


(def
 v62_l382
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t63_l387
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l382)))


(def
 v65_l414
 (with-out-str
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:tooltip true})
   (pj/plot {:format :bufimg}))))


(deftest
 t66_l420
 (is
  ((fn
    [out]
    (and
     (re-find #":tooltip asked for" out)
     (re-find #":bufimg format draws no interaction" out)
     (re-find #"The formats that do: :svg" out)))
   v65_l414)))


(def
 v68_l428
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t69_l432
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (re-find #"data-tooltip" (str (pj/plot v)))))
   v68_l428)))


(def
 v71_l449
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t72_l456
 (is
  ((fn
    [msg]
    (and
     (re-find #"panel aesthetic" msg)
     (re-find #"read from a pose's mapping" msg)))
   v71_l449)))


(def
 v74_l466
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t75_l470
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v74_l466)))


(def
 v77_l483
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 6.5, :y 3.5, :text "mean"})))


(deftest
 t78_l487
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v77_l483)))


(def
 v80_l495
 (->
  {:team ["North" "South" "East" "West" "Central"],
   :spend [12 19 15 24 31],
   :revenue [30 45 38 62 74]}
  (pj/lay-point :spend :revenue)
  (pj/lay-text {:x 33, :y :revenue, :text :team})))


(deftest
 t81_l501
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
   v80_l495)))


(def v83_l530 (-> (tc/dataset [[1 2] [3 4] [5 7]]) (pj/lay-point 0 1)))


(deftest
 t84_l533
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v83_l530)))


(def
 v86_l542
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t87_l546
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v86_l542)))


(def
 v89_l564
 (try
  (->
   (rdatasets/datasets-iris)
   pj/pose
   (pj/lay-point {:y :sepal-width})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t90_l571
 (is
  ((fn
    [msg]
    (and
     (re-find #"pj/lay-point draws from two columns" msg)
     (re-find #"no column on :x" msg)
     (re-find #"It names :y :sepal-width" msg)
     (re-find #"Available columns" msg)))
   v89_l564)))


(def
 v92_l580
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  (pj/lay-point {:y :sepal-width})))


(deftest
 t93_l584
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v92_l580)))


(def
 v95_l597
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :petal-width)
   (pj/facet {:y [:sepal-width :petal-length]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t96_l604
 (is
  ((fn
    [msg]
    (and
     (re-find #"names :petal-width on :y, and no panel draws it" msg)
     (re-find #":sepal-width and :petal-length on :y" msg)))
   v95_l597)))


(def
 v98_l611
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-length :petal-width]})))


(deftest
 t99_l616
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v98_l611)))


(def
 v101_l623
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/lay-point {:y :petal-width})
  (pj/facet {:y [:sepal-width :petal-length]})))


(deftest
 t102_l629
 (is
  ((fn
    [v]
    (=
     {:panels 2, :points 600}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v101_l623)))


(def
 v104_l641
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})))


(deftest
 t105_l645
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v104_l641)))


(def
 v107_l656
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})
  (pj/facet {:x [:growth :tax]})))


(deftest
 t108_l661
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 6 (:polygons s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v107_l656)))


(def
 v110_l719
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t111_l724
 (is
  ((fn
    [msg]
    (and
     (re-find #"was given pairs of columns" msg)
     (re-find #"pj/arrange" msg)
     (re-find #"pj/facet-matrix" msg)))
   v110_l719)))


(def
 v113_l732
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/lay-point :displ :year)))


(deftest
 t114_l736
 (is
  ((fn
    [v]
    (let
     [[lo hi]
      (-> v pj/plan :panels first :y-domain)
      printed
      (with-out-str (pj/plan v))
      hwy
      (:hwy (rdatasets/ggplot2-mpg))]
     (and
      (< lo 50)
      (> hi 2000)
      (< (* 20 (- (reduce max hwy) (reduce min hwy))) (- hi lo))
      (re-find
       #"Note: on :y one axis holds :hwy \(12 to 44\), :year \(1999 to 2008\)"
       printed)
      (re-find #"\(pj/facet pose \{:y \[:hwy :year\]\}\)" printed))))
   v113_l732)))


(def
 v116_l760
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v117_l764
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t118_l769
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v117_l764)))


(def
 v120_l776
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t121_l780
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v120_l776)))


(def
 v123_l796
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t124_l802
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v123_l796)))


(def
 v126_l808
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t127_l816
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v126_l808)))


(def
 v129_l834
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})))


(deftest
 t130_l837
 (is
  ((fn
    [fr]
    (let
     [radii
      (fn* [p1__82521#] (sort (:sizes (pj/svg-summary p1__82521#))))
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
   v129_l834)))


(def
 v132_l853
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :linear})))


(deftest
 t133_l857
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v132_l853)))


(def
 v135_l870
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-band-h {:y-min 3.0, :y-max 3.0}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t136_l876
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min < :y-max" msg)
     (re-find #"lay-rule-h" msg)))
   v135_l870)))


(def
 v138_l883
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t139_l887
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v138_l883)))


(def
 v141_l901
 (with-out-str
  (pj/with-config {:annotation-stroke "firebrick"} (constantly nil))))


(deftest
 t142_l904
 (is
  ((fn
    [msg]
    (and
     (re-find #"does not recognize configuration key" msg)
     (re-find #":annotation-stroke was renamed to :rule-color" msg)))
   v141_l901)))


(def
 v144_l914
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/options {:rule-color "firebrick"})))


(deftest
 t145_l919
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v144_l914)))


(def
 v147_l936
 (with-out-str
  (->
   {:team ["red" "green" "blue"], :score [3 5 4]}
   (pj/lay-bar :team :score)
   (pj/lay-text
    {:x {:value "red"}, :y 3, :nudge-x 0.5, :text "note"}))))


(deftest
 t148_l941
 (is
  ((fn
    [out]
    (and
     (re-find #":nudge-x was renamed to :dx" out)
     (re-find #"Write :dx" out)))
   v147_l936)))


(def
 v150_l948
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
 t151_l953
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
   v150_l948)))


(def
 v153_l1000
 (->
  {:x [1 2 3], :y [2 4 3], :r [1 2 3]}
  (pj/pose :x :y)
  (pj/lay-line {:size 2})
  (pj/lay-point {:size :r})))


(deftest
 t154_l1005
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v153_l1000)))


(def
 v156_l1023
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v157_l1026
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
 v158_l1030
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t159_l1033 (is ((fn [v] (true? v)) v158_l1030)))


(def
 v161_l1040
 (->
  points-data
  (pj/lay-point
   :x
   :y
   {:color :group, :position :dodge, :x-type :categorical})))


(deftest
 t162_l1043
 (is
  ((fn
    [v]
    (not=
     (pj/plot v)
     (pj/plot
      (->
       points-data
       (pj/lay-point :x :y {:color :group, :x-type :categorical})))))
   v161_l1040)))


(def
 v164_l1065
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t165_l1070
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
   v164_l1065)))


(def
 v167_l1079
 (-> (rdatasets/datasets-chickwts) (pj/pose :feed) pj/lay-bar))


(deftest
 t168_l1083
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
   v167_l1079)))


(def
 v170_l1096
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y)
   pj/plan)
  (catch Throwable t (ex-message t))))


(deftest
 t171_l1102
 (is
  ((fn [msg] (re-find #"requires a numeric column for :x" msg))
   v170_l1096)))


(def
 v173_l1109
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
 t174_l1115
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v173_l1109)))


(def
 v176_l1129
 (try
  (->
   {:group [], :measurement []}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t177_l1135
 (is
  ((fn
    [msg]
    (re-find #"requires a categorical column.*has no rows" msg))
   v176_l1129)))


(def
 v179_l1142
 (try
  (->
   {:group [nil nil], :measurement [nil nil]}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t180_l1148
 (is ((fn [msg] (re-find #"has no values" msg)) v179_l1142)))


(def
 v182_l1167
 (try
  (-> {:x [1 2], :y [1 2]} (pj/lay-text :x :y {:text :nope}) pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t183_l1173
 (is ((fn [msg] (re-find #"not a label either" msg)) v182_l1167)))


(def
 v185_l1190
 (try
  (->
   {:height [1 2 3], :weight [1 2 3]}
   (pj/lay-point :height :weight)
   (pj/scale :y {:domain [0]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t186_l1197
 (is
  ((fn [msg] (re-find #"not a pair of two finite numbers" msg))
   v185_l1190)))
