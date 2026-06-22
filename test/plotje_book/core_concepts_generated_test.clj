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
  (pj/lay-value-bar :city :temperature)))


(deftest
 t16_l93
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v15_l87)))


(def v18_l99 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t19_l102
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l99)))


(def
 v21_l119
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t22_l124
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v21_l119)))


(def
 v24_l133
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t25_l136
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v24_l133)))


(def
 v27_l143
 (def
  two-panel
  (pj/arrange
   [(->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width))
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :petal-width))])))


(def v28_l150 two-panel)


(deftest
 t29_l152
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v28_l150)))


(def
 v31_l176
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t32_l181
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v31_l176)))


(def
 v34_l190
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t35_l195
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v34_l190)))


(def
 v37_l202
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t38_l208
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:layers 0 :mapping :color]))
     (not (contains? (or (get-in v [:layers 1 :mapping]) {}) :color))))
   v37_l202)))


(def
 v40_l220
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t41_l225
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v40_l220)))


(def
 v43_l233
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t44_l239
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:mapping :color]))
     (contains? (get (first (:layers v)) :mapping) :color)
     (nil? (get-in (first (:layers v)) [:mapping :color]))))
   v43_l233)))


(def
 v46_l267
 (def
  setosa
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__81924#] (= "setosa" (:species p1__81924#))))))


(def
 v47_l271
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__81925#] (= "versicolor" (:species p1__81925#))))))


(def
 v48_l275
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})))


(deftest
 t49_l280
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v48_l275)))


(def
 v51_l288
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})
  kind/pprint))


(deftest
 t52_l294
 (is
  ((fn
    [v]
    (and
     (some? (:data v))
     (contains? (first (:layers v)) :data)
     (contains? (second (:layers v)) :data)))
   v51_l288)))


(def
 v54_l303
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t55_l307
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v54_l303)))


(def
 v57_l327
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t58_l331
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 1 (:lines s)))))
   v57_l327)))


(def
 v60_l342
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point)))


(deftest
 t61_l346
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v60_l342)))


(def
 v63_l350
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point)
  kind/pprint))


(deftest
 t64_l355
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:poses v)))
     (= :sepal-length (get-in v [:poses 0 :mapping :x]))
     (= :sepal-width (get-in v [:poses 0 :mapping :y]))
     (= :petal-length (get-in v [:poses 1 :mapping :x]))
     (= :petal-width (get-in v [:poses 1 :mapping :y]))))
   v63_l350)))


(def
 v66_l365
 (pj/arrange
  [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
   (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))]))


(deftest
 t67_l369
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v66_l365)))


(def v69_l403 (pj/layer-type-lookup :histogram))


(deftest t70_l405 (is ((fn [m] (= :bar (:mark m))) v69_l403)))


(def
 v72_l409
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t73_l412
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v72_l409)))


(def v75_l416 (pj/layer-type-lookup :smooth))


(deftest t76_l418 (is ((fn [m] (= :loess (:stat m))) v75_l416)))


(def
 v78_l422
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-value-bar :day :count {:color :meal, :position :stack})))


(deftest
 t79_l427
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v78_l422)))


(def
 v81_l459
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t82_l462
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v81_l459)))


(def
 v84_l469
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t85_l472
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v84_l469)))


(def v87_l476 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t88_l479
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v87_l476)))


(def
 v90_l497
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v92_l503 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t93_l505
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v92_l503)))


(def v95_l511 (-> scatter-base pj/lay-smooth))


(deftest
 t96_l513
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v95_l511)))


(def
 v98_l526
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :x, :y :y, :color :group})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v100_l535 (kind/pprint scatter-with-regression))


(deftest
 t101_l537
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v100_l535)))


(def
 v103_l543
 (->
  scatter-with-regression
  (pj/with-data
   {:x [1 2 3 4 5 6],
    :y [2 4 3 5 6 8],
    :group ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t104_l548
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v103_l543)))


(def
 v106_l554
 (->
  scatter-with-regression
  (pj/with-data
   {:x [10 20 30 40 50 60],
    :y [15 18 22 20 25 28],
    :group ["x" "x" "x" "y" "y" "y"]})))


(deftest
 t107_l559
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v106_l554)))


(def
 v109_l577
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t110_l580
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v109_l577)))


(def
 v112_l586
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t113_l589
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v112_l586)))


(def
 v115_l593
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t116_l596
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v115_l593)))


(def
 v118_l612
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3], "blue" ["a" "b" "c"]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t119_l615
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v118_l612)))


(def
 v121_l622
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t122_l625
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v121_l622)))


(def
 v124_l633
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t125_l636
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v124_l633)))


(def
 v127_l645
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t128_l649
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v127_l645)))


(def
 v130_l655
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t131_l658
 (is
  ((fn
    [v]
    (let
     [layer
      (-> v pj/plan :panels first :layers first)
      shape-values
      (set (mapcat :shapes (:groups layer)))]
     (= 3 (count shape-values))))
   v130_l655)))


(def
 v133_l666
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t134_l671
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v133_l666)))


(def
 v136_l690
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements", :width 500, :palette :dark2})))


(deftest
 t137_l695
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v136_l690)))


(def
 v139_l704
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t140_l709
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v139_l704)))


(def
 v142_l715
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t143_l721
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v142_l715)))


(def
 v145_l738
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t146_l742
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v145_l738)))


(def
 v148_l749
 (->
  {:x [-1 1 -1 1], :y [-1 -1 1 1]}
  (pj/lay-point :x :y)
  (pj/coord :fixed)))


(deftest
 t149_l753
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (< (:width s) 600))))
   v148_l749)))


(def
 v151_l762
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t152_l768
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v151_l762)))


(def
 v154_l777
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t155_l783
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v154_l777)))


(def
 v157_l790
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t158_l797
 (is ((fn [v] (= :species (get-in v [:opts :facet-col]))) v157_l790)))


(def
 v160_l801
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram [:sepal-length :sepal-width :petal-length])))


(deftest
 t161_l804
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v160_l801)))


(def
 v163_l810
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram [:sepal-length :sepal-width :petal-length])
  kind/pprint))


(deftest
 t164_l814
 (is
  ((fn
    [v]
    (and
     (= 3 (count (:poses v)))
     (= :sepal-length (get-in v [:poses 0 :mapping :x]))
     (= :sepal-width (get-in v [:poses 1 :mapping :x]))
     (= :petal-length (get-in v [:poses 2 :mapping :x]))))
   v163_l810)))


(def
 v166_l821
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width))]))


(deftest
 t167_l827
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v166_l821)))
