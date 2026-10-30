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


(def v12_l77 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-point :x :y)))


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


(def v21_l115 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t22_l118
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v21_l115)))


(def
 v24_l135
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t25_l140
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v24_l135)))


(def
 v27_l149
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t28_l152
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v27_l149)))


(def
 v30_l159
 (def
  two-panel
  (pj/arrange
   [(->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width))
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :petal-width))])))


(def v31_l166 two-panel)


(deftest
 t32_l168
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v31_l166)))


(def
 v34_l192
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t35_l197
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v34_l192)))


(def
 v37_l206
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t38_l211
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v37_l206)))


(def
 v40_l218
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t41_l224
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:layers 0 :mapping :color]))
     (not (contains? (or (get-in v [:layers 1 :mapping]) {}) :color))))
   v40_l218)))


(def
 v43_l236
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t44_l241
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v43_l236)))


(def
 v46_l249
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t47_l255
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:mapping :color]))
     (contains? (get (first (:layers v)) :mapping) :color)
     (nil? (get-in (first (:layers v)) [:mapping :color]))))
   v46_l249)))


(def
 v49_l286
 (def
  setosa
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__80233#] (= "setosa" (:species p1__80233#))))))


(def
 v50_l290
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__80234#] (= "versicolor" (:species p1__80234#))))))


(def
 v51_l294
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})))


(deftest
 t52_l299
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v51_l294)))


(def
 v54_l307
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})
  kind/pprint))


(deftest
 t55_l313
 (is
  ((fn
    [v]
    (and
     (some? (:data v))
     (contains? (first (:layers v)) :data)
     (contains? (second (:layers v)) :data)))
   v54_l307)))


(def
 v57_l322
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t58_l326
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v57_l322)))


(def
 v60_l345
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t61_l349
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 1 (:lines s)))))
   v60_l345)))


(def
 v63_l361
 (->
  (rdatasets/datasets-iris)
  (pj/facet
   {:x [:sepal-length :petal-length], :y [:sepal-width :petal-width]})
  pj/lay-point))


(deftest
 t64_l366
 (is
  ((fn
    [v]
    (=
     {:panels 2, :points 300}
     (select-keys (pj/svg-summary v) [:panels :points])))
   v63_l361)))


(def
 v66_l375
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (kind/pprint)))


(deftest
 t67_l380
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v66_l375)))


(deftest
 t68_l382
 (is
  ((fn
    [v]
    (let
     [row
      (first (:poses v))
      cells
      (:poses row)
      [a b]
      (map (comp :panel-box :frames) (:panels (pj/frames v)))]
     (and
      (= :vertical (get-in v [:layout :direction]))
      (= :horizontal (get-in row [:layout :direction]))
      (= 2 (count cells))
      (= :sepal-length (get-in cells [0 :mapping :x]))
      (= :sepal-width (get-in cells [0 :mapping :y]))
      (= :petal-length (get-in cells [1 :mapping :x]))
      (= :petal-width (get-in cells [1 :mapping :y]))
      (not= (first a) (first b))
      (= (second a) (second b)))))
   v66_l375)))


(def
 v70_l405
 (pj/arrange
  [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
   (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))]))


(deftest
 t71_l409
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v70_l405)))


(def
 v73_l416
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort {:bar-width 0.4})))


(deftest
 t74_l420
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (some #{"growth, tax"} (:texts s)))))
   v73_l416)))


(def v76_l461 (pj/layer-type-lookup :histogram))


(deftest t77_l463 (is ((fn [m] (= :bar (:mark m))) v76_l461)))


(def
 v79_l467
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t80_l470
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v79_l467)))


(def v82_l474 (pj/layer-type-lookup :smooth))


(deftest t83_l476 (is ((fn [m] (= :loess (:stat m))) v82_l474)))


(def
 v85_l480
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t86_l485
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v85_l480)))


(def
 v88_l515
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t89_l518
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v88_l515)))


(def
 v91_l525
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t92_l528
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v91_l525)))


(def v94_l532 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t95_l535
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v94_l532)))


(def
 v97_l552
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v99_l558 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t100_l560
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v99_l558)))


(def v102_l566 (-> scatter-base pj/lay-smooth))


(deftest
 t103_l568
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v102_l566)))


(def
 v105_l580
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :x, :y :y, :color :group})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v107_l589 (kind/pprint scatter-with-regression))


(deftest
 t108_l591
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v107_l589)))


(def v110_l598 scatter-with-regression)


(deftest
 t111_l600
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (some #{"no data"} (:texts s))
      (zero? (:points s))
      (zero? (:lines s)))))
   v110_l598)))


(def
 v113_l607
 (->
  scatter-with-regression
  (pj/with-data
   {:x [1 2 3 4 5 6],
    :y [2 4 3 5 6 8],
    :group ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t114_l612
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v113_l607)))


(def
 v116_l618
 (->
  scatter-with-regression
  (pj/with-data
   {:x [10 20 30 40 50 60],
    :y [15 18 22 20 25 28],
    :group ["x" "x" "x" "y" "y" "y"]})))


(deftest
 t117_l623
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v116_l618)))


(def
 v119_l639
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t120_l642
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v119_l639)))


(def
 v122_l648
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t123_l651
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v122_l648)))


(def
 v125_l655
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t126_l658
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v125_l655)))


(def
 v128_l677
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3], "blue" ["a" "b" "c"]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t129_l680
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v128_l677)))


(def
 v131_l687
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t132_l690
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v131_l687)))


(def
 v134_l698
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t135_l701
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v134_l698)))


(def
 v137_l711
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t138_l715
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v137_l711)))


(def
 v140_l721
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t141_l724
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
   v140_l721)))


(def
 v143_l738
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})))


(deftest
 t144_l742
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
   v143_l738)))


(def
 v146_l751
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t147_l756
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v146_l751)))


(def
 v149_l775
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:title "Iris Measurements", :color-values :dark2})))


(deftest
 t150_l780
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v149_l775)))


(def
 v152_l792
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t153_l797
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v152_l792)))


(def
 v155_l804
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t156_l810
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v155_l804)))


(def
 v158_l826
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t159_l830
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v158_l826)))


(def
 v161_l838
 (->
  {:x [-1 1 -1 1], :y [-1 -1 1 1]}
  (pj/lay-point :x :y)
  (pj/coord :fixed)))


(deftest
 t162_l842
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      [_ _ w h]
      (-> (pj/frames v) :panels first :frames :drawing-area)]
     (and (= 4 (:points s)) (< (:width s) 600) (= w h))))
   v161_l838)))


(def
 v164_l853
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t165_l859
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v164_l853)))


(def
 v167_l869
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t168_l875
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v167_l869)))


(def
 v170_l883
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t171_l890
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v170_l883)))


(def
 v173_l895
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t174_l898
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (count (disj (:colors s) "none"))))))
   v173_l895)))


(def
 v176_l904
 (->
  (rdatasets/datasets-iris)
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})
  (pj/lay-histogram)))


(deftest
 t177_l908
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v176_l904)))


(def
 v179_l912
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width))]))


(deftest
 t180_l918
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v179_l912)))
