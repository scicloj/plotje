(ns
 plotje-book.core-concepts-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def v3_l34 (rdatasets/datasets-iris))


(deftest
 t4_l36
 (is
  ((fn
    [ds]
    (and
     (= 150 (tc/row-count ds))
     (= 6 (tc/column-count ds))
     (=
      [:rownames
       :sepal-length
       :sepal-width
       :petal-length
       :petal-width
       :species]
      (vec (tc/column-names ds)))))
   v3_l34)))


(def
 v6_l52
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})))


(deftest
 t7_l56
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v6_l52)))


(def
 v9_l61
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  kind/pprint))


(deftest
 t10_l66
 (is
  ((fn
    [v]
    (and
     (= :sepal-length (get-in v [:mapping :x]))
     (= 1 (count (:layers v)))
     (= :species (get-in v [:layers 0 :mapping :color]))))
   v9_l61)))


(def
 v12_l77
 (->
  {:height [1 2 3 4 5], :weight [2 4 3 5 4]}
  (pj/lay-point :height :weight)))


(deftest
 t13_l81
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v12_l77)))


(def
 v15_l85
 (->
  [{:city "Paris", :temperature 22}
   {:city "London", :temperature 18}
   {:city "Berlin", :temperature 20}
   {:city "Rome", :temperature 28}]
  (pj/lay-bar :city :temperature)))


(deftest
 t16_l91
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v15_l85)))


(def v18_l97 (-> [[1 2] [3 4] [5 7]] pj/lay-point))


(deftest
 t19_l100
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v18_l97)))


(def
 v21_l115
 (-> {:weight [1 2 3 4 5], :height [2 4 3 5 4]} pj/lay-point))


(deftest
 t22_l118
 (is
  ((fn
    [v]
    (and
     (= 5 (:points (pj/svg-summary v)))
     (= ["weight" "height"] ((juxt :x-label :y-label) (pj/plan v)))))
   v21_l115)))


(def
 v24_l137
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t25_l142
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v24_l137)))


(def
 v27_l151
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t28_l154
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v27_l151)))


(def
 v30_l161
 (def
  two-panel
  (pj/arrange
   [(->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width))
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :petal-width))])))


(def v31_l168 two-panel)


(deftest
 t32_l170
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v31_l168)))


(def
 v34_l194
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t35_l199
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v34_l194)))


(def
 v37_l208
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t38_l213
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v37_l208)))


(def
 v40_l220
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t41_l226
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:layers 0 :mapping :color]))
     (not (contains? (or (get-in v [:layers 1 :mapping]) {}) :color))))
   v40_l220)))


(def
 v43_l238
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t44_l243
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v43_l238)))


(def
 v46_l251
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t47_l257
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:mapping :color]))
     (contains? (get (first (:layers v)) :mapping) :color)
     (nil? (get-in (first (:layers v)) [:mapping :color]))))
   v46_l251)))


(def
 v49_l307
 (def
  setosa
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__81386#] (= "setosa" (:species p1__81386#))))))


(def
 v50_l311
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__81387#] (= "versicolor" (:species p1__81387#))))))


(def
 v51_l315
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})))


(deftest
 t52_l320
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v51_l315)))


(def
 v54_l328
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})
  kind/pprint))


(deftest
 t55_l334
 (is
  ((fn
    [v]
    (and
     (some? (:data v))
     (contains? (first (:layers v)) :data)
     (contains? (second (:layers v)) :data)))
   v54_l328)))


(def
 v57_l343
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t58_l347
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v57_l343)))


(def
 v60_l358
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model, :data setosa})
  (pj/facet :species)))


(deftest
 t61_l364
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)) (= 3 (:lines s)))))
   v60_l358)))


(def
 v63_l383
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort {:bar-width 0.4})))


(deftest
 t64_l387
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (some #{"growth, tax"} (:texts s)))))
   v63_l383)))


(def v66_l435 (pj/layer-type-lookup :histogram))


(deftest t67_l437 (is ((fn [m] (= :bar (:mark m))) v66_l435)))


(def
 v69_l441
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t70_l444
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v69_l441)))


(def v72_l450 (pj/layer-type-lookup :smooth))


(deftest t73_l452 (is ((fn [m] (= :loess (:stat m))) v72_l450)))


(def
 v75_l456
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t76_l461
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v75_l456)))


(def
 v78_l491
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t79_l494
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v78_l491)))


(def
 v81_l501
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t82_l504
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v81_l501)))


(def v84_l508 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t85_l511
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v84_l508)))


(def
 v87_l528
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v89_l534 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t90_l536
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v89_l534)))


(def v92_l542 (-> scatter-base pj/lay-smooth))


(deftest
 t93_l544
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v92_l542)))


(def
 v95_l556
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :height, :y :weight, :color :cohort})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v97_l565 (kind/pprint scatter-with-regression))


(deftest
 t98_l567
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v97_l565)))


(def v100_l574 scatter-with-regression)


(deftest
 t101_l576
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (some #{"no data"} (:texts s))
      (zero? (:points s))
      (zero? (:lines s))
      (re-find
       #"Note: the panel drawing :height / :weight has no data"
       (with-out-str (pj/plan v))))))
   v100_l574)))


(def
 v103_l585
 (->
  scatter-with-regression
  (pj/with-data
   {:height [1 2 3 4 5 6],
    :weight [2 4 3 5 6 8],
    :cohort ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t104_l590
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v103_l585)))


(def
 v106_l596
 (->
  scatter-with-regression
  (pj/with-data
   {:height [10 20 30 40 50 60],
    :weight [15 18 22 20 25 28],
    :cohort ["c" "c" "c" "d" "d" "d"]})))


(deftest
 t107_l601
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v106_l596)))


(def
 v109_l623
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t110_l626
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v109_l623)))


(def
 v112_l632
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t113_l635
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v112_l632)))


(def
 v115_l639
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t116_l642
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v115_l639)))


(def
 v118_l661
 (->
  {"height" [1 2 3], "weight" [1 2 3], "blue" ["a" "b" "c"]}
  (pj/lay-point "height" "weight" {:color "blue"})))


(deftest
 t119_l664
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v118_l661)))


(def
 v121_l671
 (->
  {"height" [1 2 3], "weight" [1 2 3]}
  (pj/lay-point "height" "weight" {:color "blue"})))


(deftest
 t122_l674
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v121_l671)))


(def
 v124_l682
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t125_l685
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v124_l682)))


(def
 v127_l695
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t128_l699
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v127_l695)))


(def
 v130_l705
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t131_l708
 (is
  ((fn
    [v]
    (let
     [layer
      (-> v pj/plan :panels first :layers first)
      shape-values
      (set (mapcat :shapes (:groups layer)))
      s
      (pj/svg-summary v)]
     (and
      (= 3 (count shape-values))
      (= 150 (+ (:points s) (:polygons s)))
      (every? (set (:texts s)) ["setosa" "versicolor" "virginica"]))))
   v130_l705)))


(def
 v133_l727
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})))


(deftest
 t134_l731
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (and
      (nil? (:shape-legend plan))
      (=
       [:circle :square :triangle]
       (mapv :shape (:entries (:legend plan)))))))
   v133_l727)))


(def
 v136_l740
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t137_l745
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v136_l740)))


(def
 v139_l764
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:title "Iris Measurements", :color-values :dark2})))


(deftest
 t140_l769
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v139_l764)))


(def
 v142_l781
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t143_l786
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v142_l781)))


(def
 v145_l793
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t146_l799
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v145_l793)))


(def
 v148_l815
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t149_l819
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v148_l815)))


(def
 v151_l827
 (->
  {:east [-1 1 -1 1], :north [-1 -1 1 1]}
  (pj/lay-point :east :north)
  (pj/coord :fixed)))


(deftest
 t152_l831
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      [_ _ w h]
      (-> (pj/frames v) :panels first :frames :drawing-area)]
     (and (= 4 (:points s)) (< (:width s) 600) (= w h))))
   v151_l827)))


(def
 v154_l846
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t155_l852
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v154_l846)))


(def
 v157_l864
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t158_l870
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v157_l864)))


(def
 v160_l878
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t161_l885
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v160_l878)))
