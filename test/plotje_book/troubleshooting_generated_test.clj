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
   (pj/lay-value-bar :hour :count)
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
  (pj/lay-value-bar :hour :count {:x-type :categorical})))


(deftest
 t30_l161
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v29_l158)))


(def
 v32_l184
 (try
  (->
   {:species ["setosa" "versicolor" "virginica"],
    :pct [33.3 33.3 33.3]}
   (pj/lay-value-bar :species :pct)
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
  (pj/lay-value-bar :species :pct {:color "#a6cee3"})
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
      (filter (fn* [p1__96064#] (= :text (:mark p1__96064#))))
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
 v44_l260
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length :sepal-width)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t45_l266
 (is ((fn [msg] (re-find #"uses only the x column" msg)) v44_l260)))


(def
 v47_l271
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t48_l274
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v47_l271)))


(def
 v50_l283
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/scale :x :log)
   pj/plot)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l290
 (is ((fn [msg] (re-find #"[Ll]og scale" msg)) v50_l283)))


(def
 v53_l307
 (try
  (->
   {:x [1 2 3 4 5], :y [2 4 3 5 4]}
   (pj/lay-line :x :y)
   (pj/coord :polar)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t54_l314
 (is
  ((fn [msg] (re-find #"not supported with polar coordinates" msg))
   v53_l307)))


(def
 v56_l320
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t57_l325
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v56_l320)))


(def
 v59_l345
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true})))


(deftest
 t60_l349
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v59_l345)))


(def
 v62_l362
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/pose :sepal-length :sepal-width)
   (pj/lay-point {:facet-col :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t63_l369
 (is ((fn [msg] (re-find #"Faceting is plot-level" msg)) v62_l362)))


(def
 v65_l375
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t66_l379
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v65_l375)))


(def
 v68_l394
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-text {:x :sepal-length, :y 3.0, :text :species})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t69_l401
 (is
  ((fn [msg] (re-find #":y must be a column reference" msg)) v68_l394)))


(def
 v71_l407
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-text
   {:data {:sepal-length [6.5], :species ["mean"], :yy [3.5]},
    :x :sepal-length,
    :y :yy,
    :text :species})))


(deftest
 t72_l412
 (is ((fn [v] (some #{"mean"} (:texts (pj/svg-summary v)))) v71_l407)))


(def
 v74_l425
 (def
  template
  (-> (pj/pose nil {:x :x, :y :y, :color :group}) pj/lay-point)))


(def
 v75_l429
 (try
  (-> template (pj/with-data {:x [1 2 3], :y [4 5 6]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t76_l434
 (is
  ((fn [msg] (re-find #"\[:group\] not present in the dataset" msg))
   v75_l429)))


(def
 v78_l441
 (->
  (pj/pose nil {:x :x, :y :y})
  pj/lay-point
  (pj/with-data {:x [1 2 3], :y [4 5 6]})))


(deftest
 t79_l445
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v78_l441)))


(def
 v81_l461
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (pj/lay-value-bar :category :value)
  (pj/coord :flip)))


(deftest
 t82_l468
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v81_l461)))


(def
 v84_l474
 (->
  [{:category "A", :value 100}
   {:category "B", :value 50}
   {:category "C", :value 25}]
  (tc/dataset)
  (tc/order-by [:value] :asc)
  (pj/lay-value-bar :category :value)
  (pj/coord :flip)))


(deftest
 t85_l482
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v84_l474)))


(def
 v87_l498
 (try
  (->
   {:x [1 2 3], :y [10 20 30], :group ["A" "B" "A"]}
   (pj/lay-bar :x :y {:position :stack, :color :group})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t88_l504
 (is ((fn [msg] (re-find #"uses only the x column" msg)) v87_l498)))


(def
 v90_l513
 (->
  {:x (concat (range 5) (range 5)),
   :y [1 2 3 4 5 2 2 2 3 3],
   :group (concat (repeat 5 "A") (repeat 5 "B"))}
  (pj/lay-area :x :y {:position :stack, :color :group})))


(deftest
 t91_l518
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v90_l513)))


(def
 v93_l533
 (def
  points-data
  {:x [1 1 2 2 3 3],
   :y [10 15 20 25 30 35],
   :group ["A" "B" "A" "B" "A" "B"]}))


(def
 v94_l536
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
 v95_l540
 (=
  (point-xs (-> points-data (pj/lay-point :x :y {:color :group})))
  (point-xs
   (->
    points-data
    (pj/lay-point :x :y {:color :group, :position :dodge})))))


(deftest t96_l543 (is ((fn [v] (true? v)) v95_l540)))


(def
 v98_l552
 (->
  {:cat ["A" "A" "B" "B" "C" "C"],
   :y [10 20 30 40 50 60],
   :group ["a" "b" "a" "b" "a" "b"]}
  (pj/lay-value-bar :cat :y {:color :group, :position :dodge})))


(deftest
 t99_l557
 (is ((fn [v] (= 6 (:polygons (pj/svg-summary v)))) v98_l552)))


(def
 v101_l573
 (->
  (rdatasets/datasets-chickwts)
  (pj/pose :feed)
  pj/lay-bar
  (pj/coord :polar)))


(deftest
 t102_l578
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
   v101_l573)))


(def
 v104_l587
 (-> (rdatasets/datasets-chickwts) (pj/pose :feed) pj/lay-bar))


(deftest
 t105_l591
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
   v104_l587)))


(def
 v107_l606
 (try
  (->
   {:x ["a" "b" "c"], :y ["a" "b" "c"], :v [1 2 3]}
   (pj/lay-tile :x :y {:fill :v})
   pj/plan)
  (catch Throwable t (.getMessage t))))


(deftest
 t108_l612
 (is
  ((fn [msg] (re-find #"String cannot be cast to.*Number" msg))
   v107_l606)))


(def
 v110_l619
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
 t111_l627
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Sun"])))
   v110_l619)))
