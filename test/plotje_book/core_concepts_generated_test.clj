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
 v6_l53
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})))


(deftest
 t7_l57
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v6_l53)))


(def
 v9_l63
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  kind/pprint))


(deftest
 t10_l68
 (is
  ((fn
    [v]
    (and
     (= :sepal-length (get-in v [:mapping :x]))
     (= 1 (count (:layers v)))
     (= :species (get-in v [:layers 0 :mapping :color]))))
   v9_l63)))


(def v12_l79 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-point :x :y)))


(deftest
 t13_l83
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v12_l79)))


(def
 v15_l87
 (->
  [{:city "Paris", :temperature 22}
   {:city "London", :temperature 18}
   {:city "Berlin", :temperature 20}
   {:city "Rome", :temperature 28}]
  (pj/lay-bar :city :temperature)))


(deftest
 t16_l93
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v15_l87)))


(def v18_l99 (-> [[1 2] [3 4] [5 7]] pj/lay-point))


(deftest
 t19_l102
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v18_l99)))


(def v21_l115 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t22_l118
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v21_l115)))


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
 v34_l190
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t35_l195
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v34_l190)))


(def
 v37_l204
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t38_l209
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v37_l204)))


(def
 v40_l216
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t41_l222
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:layers 0 :mapping :color]))
     (not (contains? (or (get-in v [:layers 1 :mapping]) {}) :color))))
   v40_l216)))


(def
 v43_l234
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t44_l239
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v43_l234)))


(def
 v46_l247
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t47_l253
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:mapping :color]))
     (contains? (get (first (:layers v)) :mapping) :color)
     (nil? (get-in (first (:layers v)) [:mapping :color]))))
   v46_l247)))


(def
 v49_l284
 (def
  setosa
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__88553#] (= "setosa" (:species p1__88553#))))))


(def
 v50_l288
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__88554#] (= "versicolor" (:species p1__88554#))))))


(def
 v51_l292
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})))


(deftest
 t52_l297
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v51_l292)))


(def
 v54_l305
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})
  kind/pprint))


(deftest
 t55_l311
 (is
  ((fn
    [v]
    (and
     (some? (:data v))
     (contains? (first (:layers v)) :data)
     (contains? (second (:layers v)) :data)))
   v54_l305)))


(def
 v57_l320
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t58_l324
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v57_l320)))


(def
 v60_l343
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t61_l347
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 1 (:lines s)))))
   v60_l343)))


(def
 v63_l361
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point)))


(deftest
 t64_l365
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v63_l361)))


(def
 v66_l369
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point)
  kind/pprint))


(deftest
 t67_l374
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:poses v)))
     (= :sepal-length (get-in v [:poses 0 :mapping :x]))
     (= :sepal-width (get-in v [:poses 0 :mapping :y]))
     (= :petal-length (get-in v [:poses 1 :mapping :x]))
     (= :petal-width (get-in v [:poses 1 :mapping :y]))
     (let
      [[a b] (map (comp :panel-box :frames) (:panels (pj/frames v)))]
      (and (not= (first a) (first b)) (not= (second a) (second b))))))
   v66_l369)))


(def
 v69_l398
 (pj/arrange
  [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
   (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))]))


(deftest
 t70_l402
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v69_l398)))


(def
 v72_l410
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t73_l415
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v72_l410)))


(def v75_l458 (pj/layer-type-lookup :histogram))


(deftest t76_l460 (is ((fn [m] (= :bar (:mark m))) v75_l458)))


(def
 v78_l464
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t79_l467
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v78_l464)))


(def v81_l471 (pj/layer-type-lookup :smooth))


(deftest t82_l473 (is ((fn [m] (= :loess (:stat m))) v81_l471)))


(def
 v84_l477
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t85_l482
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v84_l477)))


(def
 v87_l513
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t88_l516
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v87_l513)))


(def
 v90_l523
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t91_l526
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v90_l523)))


(def v93_l530 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t94_l533
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v93_l530)))


(def
 v96_l550
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v98_l556 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t99_l558
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v98_l556)))


(def v101_l564 (-> scatter-base pj/lay-smooth))


(deftest
 t102_l566
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v101_l564)))


(def
 v104_l578
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :x, :y :y, :color :group})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v106_l587 (kind/pprint scatter-with-regression))


(deftest
 t107_l589
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v106_l587)))


(def
 v109_l595
 (->
  scatter-with-regression
  (pj/with-data
   {:x [1 2 3 4 5 6],
    :y [2 4 3 5 6 8],
    :group ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t110_l600
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v109_l595)))


(def
 v112_l606
 (->
  scatter-with-regression
  (pj/with-data
   {:x [10 20 30 40 50 60],
    :y [15 18 22 20 25 28],
    :group ["x" "x" "x" "y" "y" "y"]})))


(deftest
 t113_l611
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v112_l606)))


(def
 v115_l628
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t116_l631
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v115_l628)))


(def
 v118_l637
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t119_l640
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v118_l637)))


(def
 v121_l644
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t122_l647
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v121_l644)))


(def
 v124_l666
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3], "blue" ["a" "b" "c"]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t125_l669
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v124_l666)))


(def
 v127_l676
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t128_l679
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v127_l676)))


(def
 v130_l687
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t131_l690
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v130_l687)))


(def
 v133_l700
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t134_l704
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v133_l700)))


(def
 v136_l710
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t137_l713
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
   v136_l710)))


(def
 v139_l727
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})))


(deftest
 t140_l731
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
   v139_l727)))


(def
 v142_l740
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t143_l745
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v142_l740)))


(def
 v145_l766
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements", :width 500, :color-values :dark2})))


(deftest
 t146_l771
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v145_l766)))


(def
 v148_l780
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t149_l785
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v148_l780)))


(def
 v151_l791
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t152_l797
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v151_l791)))


(def
 v154_l813
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t155_l817
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v154_l813)))


(def
 v157_l824
 (->
  {:x [-1 1 -1 1], :y [-1 -1 1 1]}
  (pj/lay-point :x :y)
  (pj/coord :fixed)))


(deftest
 t158_l828
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (< (:width s) 600))))
   v157_l824)))


(def
 v160_l837
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t161_l843
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v160_l837)))


(def
 v163_l853
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t164_l859
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v163_l853)))


(def
 v166_l866
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t167_l873
 (is ((fn [v] (= :species (get-in v [:opts :facet-col]))) v166_l866)))


(def
 v169_l877
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram [:sepal-length :sepal-width :petal-length])))


(deftest
 t170_l880
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v169_l877)))


(def
 v172_l886
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram [:sepal-length :sepal-width :petal-length])
  kind/pprint))


(deftest
 t173_l890
 (is
  ((fn
    [v]
    (and
     (= 3 (count (:poses v)))
     (= :sepal-length (get-in v [:poses 0 :mapping :x]))
     (= :sepal-width (get-in v [:poses 1 :mapping :x]))
     (= :petal-length (get-in v [:poses 2 :mapping :x]))))
   v172_l886)))


(def
 v175_l897
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width))]))


(deftest
 t176_l903
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v175_l897)))
