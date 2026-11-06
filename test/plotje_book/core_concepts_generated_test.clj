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


(def v21_l114 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t22_l117
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v21_l114)))


(def
 v24_l134
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t25_l139
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v24_l134)))


(def
 v27_l148
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t28_l151
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v27_l148)))


(def
 v30_l158
 (def
  two-panel
  (pj/arrange
   [(->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width))
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :petal-width))])))


(def v31_l165 two-panel)


(deftest
 t32_l167
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v31_l165)))


(def
 v34_l191
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t35_l196
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v34_l191)))


(def
 v37_l205
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t38_l210
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v37_l205)))


(def
 v40_l217
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t41_l223
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:layers 0 :mapping :color]))
     (not (contains? (or (get-in v [:layers 1 :mapping]) {}) :color))))
   v40_l217)))


(def
 v43_l235
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t44_l240
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v43_l235)))


(def
 v46_l248
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t47_l254
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:mapping :color]))
     (contains? (get (first (:layers v)) :mapping) :color)
     (nil? (get-in (first (:layers v)) [:mapping :color]))))
   v46_l248)))


(def
 v49_l296
 (def
  setosa
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__11193#] (= "setosa" (:species p1__11193#))))))


(def
 v50_l300
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__11194#] (= "versicolor" (:species p1__11194#))))))


(def
 v51_l304
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})))


(deftest
 t52_l309
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v51_l304)))


(def
 v54_l317
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})
  kind/pprint))


(deftest
 t55_l323
 (is
  ((fn
    [v]
    (and
     (some? (:data v))
     (contains? (first (:layers v)) :data)
     (contains? (second (:layers v)) :data)))
   v54_l317)))


(def
 v57_l332
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t58_l336
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v57_l332)))


(def
 v60_l352
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort {:bar-width 0.4})))


(deftest
 t61_l356
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (some #{"growth, tax"} (:texts s)))))
   v60_l352)))


(def
 v63_l370
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet
   {:x [:sepal-length :petal-length], :y [:sepal-width :petal-width]})))


(deftest
 t64_l376
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 2, :points 300}
      (select-keys (pj/svg-summary v) [:panels :points]))
     (=
      [[1] [1]]
      (mapv
       (fn [s] (mapv (comp count :layers) (:panels (:plan s))))
       (:sub-plots (pj/plan v))))))
   v63_l370)))


(def
 v66_l389
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :petal-width}])
  (kind/pprint)))


(deftest
 t67_l394
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v66_l389)))


(deftest
 t68_l396
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
   v66_l389)))


(def
 v70_l420
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} {:x :displ, :y :cty}])
  pj/lay-point))


(deftest
 t71_l424
 (is
  ((fn
    [v]
    (and
     (= [{:layer-type :point}] (:layers v))
     (=
      {:panels 2, :points 468}
      (select-keys (pj/svg-summary v) [:panels :points]))))
   v70_l420)))


(def
 v73_l434
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} {:x :displ, :y :cty}])
  pj/lay-point
  (pj/lay-smooth :displ :cty)
  (pj/lay-smooth :cty :hwy)))


(deftest
 t74_l440
 (is
  ((fn
    [v]
    (let
     [cells (:poses (first (:poses v)))]
     (and
      (=
       [[] [:smooth] [:smooth]]
       (mapv
        (fn* [p1__11195#] (mapv :layer-type (:layers p1__11195#)))
        cells))
      (=
       {:panels 3, :points 702, :lines 2}
       (select-keys (pj/svg-summary v) [:panels :points :lines])))))
   v73_l434)))


(def
 v76_l449
 (pj/arrange
  [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
   (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))]))


(deftest
 t77_l453
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v76_l449)))


(def v79_l486 (pj/layer-type-lookup :histogram))


(deftest t80_l488 (is ((fn [m] (= :bar (:mark m))) v79_l486)))


(def
 v82_l492
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t83_l495
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v82_l492)))


(def v85_l499 (pj/layer-type-lookup :smooth))


(deftest t86_l501 (is ((fn [m] (= :loess (:stat m))) v85_l499)))


(def
 v88_l505
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t89_l510
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v88_l505)))


(def
 v91_l540
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t92_l543
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v91_l540)))


(def
 v94_l550
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t95_l553
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v94_l550)))


(def v97_l557 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t98_l560
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v97_l557)))


(def
 v100_l577
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v102_l583 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t103_l585
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v102_l583)))


(def v105_l591 (-> scatter-base pj/lay-smooth))


(deftest
 t106_l593
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v105_l591)))


(def
 v108_l605
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :x, :y :y, :color :group})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v110_l614 (kind/pprint scatter-with-regression))


(deftest
 t111_l616
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v110_l614)))


(def v113_l623 scatter-with-regression)


(deftest
 t114_l625
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
       #"Note: the panel drawing :x / :y has no data"
       (with-out-str (pj/plan v))))))
   v113_l623)))


(def
 v116_l634
 (->
  scatter-with-regression
  (pj/with-data
   {:x [1 2 3 4 5 6],
    :y [2 4 3 5 6 8],
    :group ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t117_l639
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v116_l634)))


(def
 v119_l645
 (->
  scatter-with-regression
  (pj/with-data
   {:x [10 20 30 40 50 60],
    :y [15 18 22 20 25 28],
    :group ["x" "x" "x" "y" "y" "y"]})))


(deftest
 t120_l650
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v119_l645)))


(def
 v122_l659
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [(->
     (pj/lay-point nil :displ :hwy)
     (pj/lay-smooth {:stat :linear-model}))
    {:x :displ, :y :cty}])))


(deftest
 t123_l664
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 1 (:lines s)) (= 468 (:points s)))))
   v122_l659)))


(def
 v125_l681
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t126_l684
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v125_l681)))


(def
 v128_l690
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t129_l693
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v128_l690)))


(def
 v131_l697
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t132_l700
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v131_l697)))


(def
 v134_l719
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3], "blue" ["a" "b" "c"]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t135_l722
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v134_l719)))


(def
 v137_l729
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t138_l732
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v137_l729)))


(def
 v140_l740
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t141_l743
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v140_l740)))


(def
 v143_l753
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t144_l757
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v143_l753)))


(def
 v146_l763
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t147_l766
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
   v146_l763)))


(def
 v149_l780
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})))


(deftest
 t150_l784
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
   v149_l780)))


(def
 v152_l793
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t153_l798
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v152_l793)))


(def
 v155_l817
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:title "Iris Measurements", :color-values :dark2})))


(deftest
 t156_l822
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v155_l817)))


(def
 v158_l834
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t159_l839
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v158_l834)))


(def
 v161_l846
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t162_l852
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v161_l846)))


(def
 v164_l868
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t165_l872
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v164_l868)))


(def
 v167_l880
 (->
  {:x [-1 1 -1 1], :y [-1 -1 1 1]}
  (pj/lay-point :x :y)
  (pj/coord :fixed)))


(deftest
 t168_l884
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      [_ _ w h]
      (-> (pj/frames v) :panels first :frames :drawing-area)]
     (and (= 4 (:points s)) (< (:width s) 600) (= w h))))
   v167_l880)))


(def
 v170_l895
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t171_l901
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v170_l895)))


(def
 v173_l911
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t174_l917
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v173_l911)))


(def
 v176_l925
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t177_l932
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v176_l925)))


(def
 v179_l937
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t180_l940
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (count (disj (:colors s) "none"))))))
   v179_l937)))


(def
 v182_l946
 (->
  (rdatasets/datasets-iris)
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})
  (pj/lay-histogram)))


(deftest
 t183_l950
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v182_l946)))


(def
 v185_l954
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width))]))


(deftest
 t186_l960
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v185_l954)))
