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
 t7_l44
 (is ((fn [msg] (re-find #"Column :sepal_\w+.*not found" msg)) v6_l37)))


(def
 v9_l55
 (try
  (->
   (tc/dataset {"sepal length" [5.0 6.0], "sepal width" [3.0 3.5]})
   (pj/pose :sepal-length :sepal-width)
   pj/lay-point
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t10_l62
 (is ((fn [msg] (re-find #"Column :sepal-\w+.*not found" msg)) v9_l55)))


(def
 v12_l85
 (-> (rdatasets/datasets-iris) (pj/pose :species :sepal-width)))


(deftest
 t13_l88
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v12_l85)))


(def
 v15_l92
 (-> (rdatasets/datasets-iris) (pj/lay-point :species :sepal-width)))


(deftest
 t16_l95
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v15_l92)))


(def
 v18_l107
 (def
  subject-scores
  {:day [1 2 3 4 1 2 3 4 1 2 3 4],
   :score [3 5 4 6 6 7 5 8 8 9 7 10],
   :subject [1 1 1 1 2 2 2 2 3 3 3 3]}))


(def
 v20_l118
 (-> subject-scores (pj/lay-line :day :score {:color :subject})))


(deftest
 t21_l121
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (contains? (:colors s) "rgb(51,51,51)"))))
   v20_l118)))


(def
 v23_l131
 (->
  subject-scores
  (pj/lay-line
   :day
   :score
   {:color :subject, :color-type :categorical})))


(deftest
 t24_l134
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v23_l131)))


(def
 v26_l157
 (try
  (->
   {:hour [9 9 10 10 11 11], :value [1 2 3 4 5 6]}
   (pj/lay-boxplot :hour :value)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t27_l163
 (is
  ((fn [msg] (re-find #"requires a categorical column" msg)) v26_l157)))


(def
 v29_l170
 (->
  {:hour [9 9 10 10 11 11], :value [1 2 3 4 5 6]}
  (pj/lay-boxplot :hour :value {:x-type :categorical})))


(deftest
 t30_l173
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v29_l170)))


(def
 v32_l196
 (try
  (->
   {:cohort [2020 2021 2022], :n [3 5 4]}
   (pj/lay-bar :cohort :n {:x-type :categorical})
   (pj/lay-text {:x 2021, :y 5.5, :text "the 2021 cohort"})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l203
 (is
  ((fn
    [msg]
    (and
     (re-find #"got 2021 for :x" msg)
     (re-find #"past the ends of this axis" msg)
     (re-find #"\[\"2020\" \"2021\" \"2022\"\]" msg)))
   v32_l196)))


(def
 v35_l212
 (->
  {:cohort [2020 2021 2022], :n [3 5 4]}
  (pj/lay-bar :cohort :n {:x-type :categorical, :color "#a6cee3"})
  (pj/lay-text
   {:x {:value "2021"},
    :y 4.5,
    :align-x :center,
    :text "on the band"})))


(deftest
 t36_l217
 (is
  ((fn
    [fr]
    (=
     ["2020" "2021" "2022"]
     (->> fr pj/plan :panels first :x-domain)))
   v35_l212)))


(def
 v38_l241
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct)
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :offset-y -6})))


(deftest
 t39_l245
 (is
  ((fn
    [fr]
    (=
     [nil -6]
     (->> fr pj/plan :panels first :layers (mapv :offset-y))))
   v38_l241)))


(def
 v41_l261
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t42_l266
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
      (filter (fn* [p1__87216#] (= :text (:mark p1__87216#))))
      first
      :style
      :align-x)))
   v41_l261)))


(def
 v44_l297
 (with-out-str
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:scale-y :log})
   pj/plan)))


(deftest
 t45_l302
 (is
  ((fn [out] (re-find #"does not recognize.*:scale-y" out)) v44_l297)))


(def
 v47_l307
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)))


(deftest
 t48_l311
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v47_l307)))


(def
 v50_l339
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l345
 (is
  ((fn
    [msg]
    (re-find
     #"draws from one column, and was given :sepal-length on :x and :sepal-width on :y"
     msg))
   v50_l339)))


(def
 v53_l350
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t54_l353
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v53_l350)))


(def
 v56_l363
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t57_l370
 (is
  ((fn
    [msg]
    (re-find
     #"Log scale on :x requires numeric data, but column :species is non-numeric"
     msg))
   v56_l363)))


(def
 v59_l387
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t60_l394
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v59_l387)))


(def
 v62_l400
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t63_l405
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l400)))


(def
 v65_l432
 (with-out-str
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:tooltip true})
   (pj/plot {:format :bufimg}))))


(deftest
 t66_l438
 (is
  ((fn
    [out]
    (and
     (re-find #":tooltip asked for" out)
     (re-find #":bufimg format draws no interaction" out)
     (re-find #"The formats that do: :svg" out)))
   v65_l432)))


(def
 v68_l446
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t69_l450
 (is
  ((fn
    [v]
    (and
     (= 150 (:points (pj/svg-summary v)))
     (re-find #"data-tooltip" (str (pj/plot v)))))
   v68_l446)))


(def
 v71_l467
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t72_l474
 (is
  ((fn
    [msg]
    (and
     (re-find #"panel aesthetic" msg)
     (re-find #"read from a pose's mapping" msg)))
   v71_l467)))


(def
 v74_l485
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t75_l489
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v74_l485)))


(def
 v77_l502
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text {:x 6.5, :y 3.5, :text "mean"})))


(deftest
 t78_l506
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v77_l502)))


(def
 v80_l514
 (->
  {:team ["North" "South" "East" "West" "Central"],
   :spend [12 19 15 24 31],
   :revenue [30 45 38 62 74]}
  (pj/lay-point :spend :revenue)
  (pj/lay-text {:x 33, :y :revenue, :text :team})))


(deftest
 t81_l520
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
   v80_l514)))


(def v83_l549 (-> (tc/dataset [[1 2] [3 4] [5 7]]) (pj/lay-point 0 1)))


(deftest
 t84_l552
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v83_l549)))


(def
 v86_l561
 (->
  (tc/dataset [[1 2] [3 4] [5 7]])
  (tc/rename-columns [:x :y])
  (pj/lay-point :x :y)))


(deftest
 t87_l565
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v86_l561)))


(def
 v89_l583
 (try
  (->
   (rdatasets/datasets-iris)
   pj/pose
   (pj/lay-point {:y :sepal-width})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t90_l590
 (is
  ((fn
    [msg]
    (and
     (re-find #"pj/lay-point draws from two columns" msg)
     (re-find #"no column on :x" msg)
     (re-find #"It names :y :sepal-width" msg)
     (re-find #"Available columns" msg)))
   v89_l583)))


(def
 v92_l599
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  (pj/lay-point {:y :sepal-width})))


(deftest
 t93_l603
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v92_l599)))


(def
 v95_l618
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :petal-width)
   (pj/facet {:y [:sepal-width :petal-length]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t96_l625
 (is
  ((fn
    [msg]
    (and
     (re-find #"names :petal-width on :y, and no panel draws it" msg)
     (re-find #":sepal-width and :petal-length on :y" msg)))
   v95_l618)))


(def
 v98_l632
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:y [:sepal-width :petal-length :petal-width]})))


(deftest
 t99_l637
 (is
  ((fn
    [v]
    (=
     {:panels 3, :points 450}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v98_l632)))


(def
 v101_l644
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/lay-point {:y :petal-width})
  (pj/facet {:y [:sepal-width :petal-length]})))


(deftest
 t102_l650
 (is
  ((fn
    [v]
    (=
     {:panels 2, :points 600}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v101_l644)))


(def
 v104_l662
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})))


(deftest
 t105_l666
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
   v104_l662)))


(def
 v107_l677
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:color "#e6550d"})
  (pj/facet {:x [:growth :tax]})))


(deftest
 t108_l682
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
   v107_l677)))


(def
 v110_l702
 (->
  (pj/arrange
   [(pj/lay-bar
     {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
     :growth
     :cohort
     {:color "#377eb8"})])
  (pj/arrange
   [(pj/lay-bar
     {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
     :tax
     :cohort
     {:color "#e6550d"})])))


(deftest
 t111_l707
 (is
  ((fn
    [v]
    (let
     [d
      {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
      marks
      (fn
       [pose]
       (mapv
        (fn*
         [p1__87217#]
         (mapv :mark (-> p1__87217# :plan :panels first :layers)))
        (-> pose pj/plan :sub-plots)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= [[:rect] [:rect]] (marks v))
      (=
       [[:rect :rect]]
       (marks
        (->
         (pj/arrange [(pj/lay-bar d :growth :cohort)])
         (pj/lay-bar :tax :cohort)))))))
   v110_l702)))


(def
 v113_l739
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v114_l743
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t115_l748
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v114_l743)))


(def
 v117_l755
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t118_l759
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v117_l755)))


(def
 v120_l775
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t121_l781
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v120_l775)))


(def
 v123_l787
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t124_l795
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v123_l787)))


(def
 v126_l813
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})))


(deftest
 t127_l816
 (is
  ((fn
    [fr]
    (let
     [radii
      (fn* [p1__87218#] (sort (:sizes (pj/svg-summary p1__87218#))))
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
   v126_l813)))


(def
 v129_l832
 (->
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :linear})))


(deftest
 t130_l836
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v129_l832)))


(def
 v132_l849
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-band-h {:y-min 3.0, :y-max 3.0}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t133_l855
 (is
  ((fn
    [msg]
    (and
     (re-find #"requires :y-min < :y-max" msg)
     (re-find #"lay-rule-h" msg)))
   v132_l849)))


(def
 v135_l862
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})))


(deftest
 t136_l866
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v135_l862)))


(def
 v138_l880
 (with-out-str (pj/with-config {:annotation-stroke "firebrick"} nil)))


(deftest
 t139_l883
 (is
  ((fn
    [msg]
    (and
     (re-find #"does not recognize configuration key" msg)
     (re-find #":annotation-stroke was renamed to :rule-color" msg)))
   v138_l880)))


(def
 v141_l893
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/options {:rule-color "firebrick"})))


(deftest
 t142_l898
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v141_l893)))


(def
 v144_l915
 (with-out-str
  (->
   {:team ["red" "green" "blue"], :score [3 5 4]}
   (pj/lay-bar :team :score)
   (pj/lay-text
    {:x {:value "red"}, :y 3, :nudge-x 0.5, :text "note"}))))


(deftest
 t145_l920
 (is
  ((fn
    [out]
    (and
     (re-find #":nudge-x was renamed to :dx" out)
     (re-find #"Write :dx" out)))
   v144_l915)))


(def
 v147_l927
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
 t148_l932
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
   v147_l927)))


(def
 v150_l979
 (->
  {:x [1 2 3], :y [2 4 3], :r [1 2 3]}
  (pj/pose :x :y)
  (pj/lay-line {:size 2})
  (pj/lay-point {:size :r})))


(deftest
 t151_l984
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (pos? (:lines s)))))
   v150_l979)))


(def
 v153_l1002
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v154_l1005
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
 v155_l1017
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t156_l1020 (is ((fn [v] (true? v)) v155_l1017)))


(def
 v158_l1027
 (->
  points-data
  (pj/lay-point
   :x
   :y
   {:color :group, :position :dodge, :x-type :categorical})))


(deftest
 t159_l1030
 (is
  ((fn
    [v]
    (not=
     (pj/plot v)
     (pj/plot
      (->
       points-data
       (pj/lay-point :x :y {:color :group, :x-type :categorical})))))
   v158_l1027)))


(def
 v161_l1047
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y)
   pj/plan)
  (catch Throwable t (ex-message t))))


(deftest
 t162_l1053
 (is
  ((fn [msg] (re-find #"requires a numeric column for :x" msg))
   v161_l1047)))


(def
 v164_l1060
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
 t165_l1066
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v164_l1060)))


(def
 v167_l1080
 (try
  (->
   {:group [], :measurement []}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t168_l1086
 (is
  ((fn
    [msg]
    (re-find #"requires a categorical column.*has no rows" msg))
   v167_l1080)))


(def
 v170_l1093
 (try
  (->
   {:group [nil nil], :measurement [nil nil]}
   (pj/lay-boxplot :group :measurement)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t171_l1099
 (is ((fn [msg] (re-find #"has no values" msg)) v170_l1093)))


(def
 v173_l1118
 (try
  (-> {:x [1 2], :y [1 2]} (pj/lay-text :x :y {:text :nope}) pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t174_l1124
 (is ((fn [msg] (re-find #"not a label either" msg)) v173_l1118)))


(def
 v176_l1141
 (try
  (->
   {:height [1 2 3], :weight [1 2 3]}
   (pj/lay-point :height :weight)
   (pj/scale :y {:domain [0]})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t177_l1148
 (is
  ((fn [msg] (re-find #"not a pair of two finite numbers" msg))
   v176_l1141)))


(def
 v179_l1228
 (try
  (pj/lay-point
   (rdatasets/datasets-iris)
   [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t180_l1233
 (is
  ((fn
    [msg]
    (and
     (re-find #"was given pairs of columns" msg)
     (re-find #"pj/arrange" msg)
     (re-find #"pj/matrix data xs ys" msg)))
   v179_l1228)))


(def
 v182_l1241
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/lay-point :displ :year)))


(deftest
 t183_l1245
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
       #"Note: the :y axis holds :hwy \(12 to 44\), :year \(1999 to 2008\)"
       printed)
      (re-find #"\(pj/facet pose \{:y \[:hwy :year\]\}\)" printed)
      (=
       ""
       (with-out-str
        (->
         (rdatasets/ggplot2-mpg)
         (pj/lay-point :displ :hwy)
         (pj/lay-point :displ :year)))))))
   v182_l1241)))


(def
 v185_l1277
 (pj/with-config
  {:theme {:bg "#1a1a2e"}}
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   pj/plot)))


(deftest
 t186_l1282
 (is
  ((fn
    [svg]
    (->>
     svg
     (tree-seq coll? seq)
     (filter map?)
     (some (fn* [p1__87219#] (= "rgb(26,26,46)" (:fill p1__87219#))))))
   v185_l1277)))


(def
 v188_l1301
 (->
  (pj/arrange
   [(->
     (rdatasets/ggplot2-mpg)
     (pj/pose :displ :hwy {:where {:drv "f"}})
     pj/lay-point)
    (->
     (rdatasets/ggplot2-mpg)
     (pj/pose :displ :hwy {:where {:drv "4"}})
     pj/lay-point)])
  (pj/lay-smooth :displ :hwy)))


(deftest
 t189_l1309
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 2, :lines 2, :points 209}
      (select-keys (pj/svg-summary v) [:panels :lines :points]))
     (= "" (with-out-str (pj/plan v)))
     (=
      [[:line :point] [:line :point]]
      (mapv
       (fn*
        [p1__87220#]
        (mapv :mark (-> p1__87220# :plan :panels first :layers)))
       (-> v pj/plan :sub-plots)))))
   v188_l1301)))


(def
 v191_l1333
 (->
  (pj/arrange
   [(pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
    (pj/lay-point (rdatasets/ggplot2-mpg) :displ :cty)])
  (pj/arrange [(pj/lay-histogram (rdatasets/ggplot2-mpg) :displ)])))


(deftest
 t192_l1337
 (is
  ((fn
    [v]
    (and
     (=
      [[:point] [:point] [:bar]]
      (mapv
       (fn*
        [p1__87221#]
        (mapv :mark (-> p1__87221# :plan :panels first :layers)))
       (-> v pj/plan :sub-plots)))
     (=
      2
      (count
       (re-seq
        #"axis reads in two units"
        (with-out-str
         (->
          (pj/arrange
           [(pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
            (pj/lay-point (rdatasets/ggplot2-mpg) :displ :cty)])
          (pj/lay-histogram :displ)
          pj/plan)))))
     (=
      [[:bar :point] [:bar :point]]
      (mapv
       (fn*
        [p1__87222#]
        (mapv :mark (-> p1__87222# :plan :panels first :layers)))
       (->
        (pj/arrange
         [(pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
          (pj/lay-point (rdatasets/ggplot2-mpg) :displ :cty)])
        (pj/lay-histogram :displ)
        pj/plan
        :sub-plots)))))
   v191_l1333)))
