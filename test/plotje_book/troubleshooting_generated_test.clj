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
 v20_l115
 (-> subject-scores (pj/lay-line :day :score {:color :subject})))


(deftest
 t21_l118
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v20_l115)))


(def
 v23_l123
 (->
  subject-scores
  (pj/lay-line
   :day
   :score
   {:color :subject, :color-type :categorical})))


(deftest
 t24_l126
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v23_l123)))


(def
 v26_l145
 (try
  (->
   {:hour [9 10 11 12], :count [5 8 12 7]}
   (pj/lay-bar :hour :count)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t27_l151
 (is
  ((fn [msg] (re-find #"requires a categorical column for :x" msg))
   v26_l145)))


(def
 v29_l158
 (->
  {:hour [9 10 11 12], :count [5 8 12 7]}
  (pj/lay-bar :hour :count {:x-type :categorical})))


(deftest
 t30_l161
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v29_l158)))


(def
 v32_l184
 (try
  (->
   {:species ["setosa" "versicolor" "virginica"],
    :pct [33.3 33.3 33.3]}
   (pj/lay-bar :species :pct)
   (pj/lay-text :species :pct {:text :pct, :nudge-x -2})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l191
 (is
  ((fn [msg] (re-find #":nudge-x .* categorical x axis" msg))
   v32_l184)))


(def
 v35_l199
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text :species :pct {:text :pct, :align-x :right})
  (pj/coord :flip)))


(deftest
 t36_l204
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
      (filter (fn* [p1__86310#] (= :text (:mark p1__86310#))))
      first
      :style
      :align-x)))
   v35_l199)))


(def
 v38_l229
 (with-out-str
  (->
   (rdatasets/ggplot2-diamonds)
   (pj/lay-point :carat :price {:scale-y :log})
   pj/plan)))


(deftest
 t39_l234
 (is
  ((fn [out] (re-find #"does not recognize option.*:scale-y" out))
   v38_l229)))


(def
 v41_l239
 (->
  (rdatasets/ggplot2-diamonds)
  (pj/lay-point :carat :price {:alpha 0.1})
  (pj/scale :y :log)))


(deftest
 t42_l243
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v41_l239)))


(def
 v44_l261
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t45_l267
 (is ((fn [msg] (re-find #"uses only the x column" msg)) v44_l261)))


(def
 v47_l272
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t48_l275
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v47_l272)))


(def
 v50_l284
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l291
 (is ((fn [msg] (re-find #"[Ll]og scale" msg)) v50_l284)))


(def
 v53_l308
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t54_l315
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v53_l308)))


(def
 v56_l321
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t57_l326
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v56_l321)))


(def
 v59_l346
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t60_l350
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v59_l346)))


(def
 v62_l363
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t63_l370
 (is ((fn [msg] (re-find #"Faceting is plot-level" msg)) v62_l363)))


(def
 v65_l376
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t66_l380
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v65_l376)))


(def
 v68_l395
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-text {:x :sepal-length, :y 3.0, :text :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t69_l402
 (is
  ((fn [msg] (re-find #":y must be a column reference" msg)) v68_l395)))


(def
 v71_l408
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text
   {:data {:sepal-length [6.5], :species ["mean"], :yy [3.5]},
    :x :sepal-length,
    :y :yy,
    :text :species})))


(deftest
 t72_l413
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v71_l408)))


(def
 v74_l426
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v75_l430
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t76_l435
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v75_l430)))


(def
 v78_l442
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t79_l446
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v78_l442)))


(def
 v81_l462
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t82_l469
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v81_l462)))


(def
 v84_l475
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-bar :category :value)
  (pj/coord :flip)))


(deftest
 t85_l483
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v84_l475)))


(def
 v87_l502
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v88_l505
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
 v89_l509
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t90_l512 (is ((fn [v] (true? v)) v89_l509)))


(def
 v92_l521
 (->
  {:cat ["A" "A" "B" "B" "C" "C"],
   :y [10 20 30 40 50 60],
   :group ["a" "b" "a" "b" "a" "b"]}
  (pj/lay-bar :cat :y {:color :group, :position :dodge})))


(deftest
 t93_l526
 (is ((fn [v] (= 6 (:polygons (pj/svg-summary v)))) v92_l521)))


(def
 v95_l542
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t96_l547
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
   v95_l542)))


(def
 v98_l556
 (-> (rdatasets/datasets-chickwts) (pj/pose :feed) pj/lay-bar))


(deftest
 t99_l560
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
   v98_l556)))


(def
 v101_l575
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y {:fill :v})
   pj/plan)
  (catch Throwable t (.getMessage t))))


(deftest
 t102_l581
 (is
  ((fn [msg] (re-find #"String cannot be cast to.*Number" msg))
   v101_l575)))


(def
 v104_l588
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
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})))


(deftest
 t105_l596
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v104_l588)))
