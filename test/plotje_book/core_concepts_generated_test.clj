(ns
 plotje-book.core-concepts-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def v3_l34 (rdatasets/datasets-iris))


(deftest t4_l36 (is ((fn [ds] (= 150 (count (tc/rows ds)))) v3_l34)))


(def
 v6_l48
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})))


(deftest
 t7_l52
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v6_l48)))


(def
 v9_l58
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  kind/pprint))


(deftest
 t10_l63
 (is
  ((fn
    [v]
    (and
     (= :sepal-length (get-in v [:mapping :x]))
     (= 1 (count (:layers v)))
     (= :species (get-in v [:layers 0 :mapping :color]))))
   v9_l58)))


(def v12_l74 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-point :x :y)))


(deftest
 t13_l78
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v12_l74)))


(def
 v15_l82
 (->
  [{:city "Paris", :temperature 22}
   {:city "London", :temperature 18}
   {:city "Berlin", :temperature 20}
   {:city "Rome", :temperature 28}]
  (pj/lay-value-bar :city :temperature)))


(deftest
 t16_l88
 (is ((fn [v] (= 4 (:polygons (pj/svg-summary v)))) v15_l82)))


(def v18_l94 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} pj/lay-point))


(deftest
 t19_l97
 (is ((fn [v] (= 5 (:points (pj/svg-summary v)))) v18_l94)))


(def
 v21_l114
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t22_l119
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (pos? (:lines s)))))
   v21_l114)))


(def
 v24_l128
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)))


(deftest
 t25_l131
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v24_l128)))


(def
 v27_l138
 (def
  two-panel
  (pj/arrange
   [(->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width))
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :petal-width))])))


(def v28_l145 two-panel)


(deftest
 t29_l147
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v28_l145)))


(def
 v31_l171
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t32_l176
 (is ((fn [v] (= 3 (:lines (pj/svg-summary v)))) v31_l171)))


(def
 v34_l185
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t35_l190
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v34_l185)))


(def
 v37_l197
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:color :species})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t38_l203
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:layers 0 :mapping :color]))
     (not (contains? (or (get-in v [:layers 1 :mapping]) {}) :color))))
   v37_l197)))


(def
 v40_l215
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t41_l220
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v40_l215)))


(def
 v43_l228
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t44_l234
 (is
  ((fn
    [v]
    (and
     (= :species (get-in v [:mapping :color]))
     (contains? (get (first (:layers v)) :mapping) :color)
     (nil? (get-in (first (:layers v)) [:mapping :color]))))
   v43_l228)))


(def
 v46_l262
 (def
  setosa
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__89663#] (= "setosa" (:species p1__89663#))))))


(def
 v47_l266
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__89664#] (= "versicolor" (:species p1__89664#))))))


(def
 v48_l270
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})))


(deftest
 t49_l275
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 50 (:points s)) (= 1 (:lines s)))))
   v48_l270)))


(def
 v51_l283
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/lay-point {:data setosa})
  (pj/lay-smooth {:stat :linear-model, :data versicolor})
  kind/pprint))


(deftest
 t52_l289
 (is
  ((fn
    [v]
    (and
     (some? (:data v))
     (contains? (first (:layers v)) :data)
     (contains? (second (:layers v)) :data)))
   v51_l283)))


(def
 v54_l298
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t55_l302
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v54_l298)))


(def
 v57_l322
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth :sepal-length :sepal-width {:stat :linear-model})))


(deftest
 t58_l326
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 1 (:lines s)))))
   v57_l322)))


(def
 v60_l337
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point)))


(deftest
 t61_l341
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v60_l337)))


(def
 v63_l345
 (->
  (rdatasets/datasets-iris)
  (pj/pose [[:sepal-length :sepal-width] [:petal-length :petal-width]])
  (pj/lay-point)
  kind/pprint))


(deftest
 t64_l350
 (is
  ((fn
    [v]
    (and
     (= 2 (count (:poses v)))
     (= :sepal-length (get-in v [:poses 0 :mapping :x]))
     (= :sepal-width (get-in v [:poses 0 :mapping :y]))
     (= :petal-length (get-in v [:poses 1 :mapping :x]))
     (= :petal-width (get-in v [:poses 1 :mapping :y]))))
   v63_l345)))


(def
 v66_l360
 (pj/arrange
  [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
   (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))]))


(deftest
 t67_l364
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v66_l360)))


(def v69_l398 (pj/layer-type-lookup :histogram))


(deftest t70_l400 (is ((fn [m] (= :bar (:mark m))) v69_l398)))


(def
 v72_l404
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t73_l407
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v72_l404)))


(def v75_l411 (pj/layer-type-lookup :smooth))


(deftest t76_l413 (is ((fn [m] (= :loess (:stat m))) v75_l411)))


(def
 v78_l417
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-value-bar :day :count {:color :meal, :position :stack})))


(deftest
 t79_l422
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v78_l417)))


(def
 v81_l455
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t82_l458
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v81_l455)))


(def
 v84_l465
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t85_l468
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v84_l465)))


(def v87_l472 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t88_l475
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v87_l472)))


(def
 v90_l493
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v92_l499 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t93_l501
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v92_l499)))


(def v95_l507 (-> scatter-base pj/lay-smooth))


(deftest
 t96_l509
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v95_l507)))


(def
 v98_l522
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :x, :y :y, :color :group})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v100_l531 (kind/pprint scatter-with-regression))


(deftest
 t101_l533
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v100_l531)))


(def
 v103_l539
 (->
  scatter-with-regression
  (pj/with-data
   {:x [1 2 3 4 5 6],
    :y [2 4 3 5 6 8],
    :group ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t104_l544
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v103_l539)))


(def
 v106_l550
 (->
  scatter-with-regression
  (pj/with-data
   {:x [10 20 30 40 50 60],
    :y [15 18 22 20 25 28],
    :group ["x" "x" "x" "y" "y" "y"]})))


(deftest
 t107_l555
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v106_l550)))


(def
 v109_l573
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t110_l576
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v109_l573)))


(def
 v112_l582
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t113_l585
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v112_l582)))


(def
 v115_l589
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t116_l592
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v115_l589)))


(def
 v118_l608
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3], "blue" ["a" "b" "c"]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t119_l611
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v118_l608)))


(def
 v121_l618
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t122_l621
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v121_l618)))


(def
 v124_l629
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t125_l632
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v124_l629)))


(def
 v127_l641
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t128_l645
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v127_l641)))


(def
 v130_l651
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t131_l654
 (is
  ((fn
    [v]
    (let
     [layer
      (-> v pj/plan :panels first :layers first)
      shape-values
      (set (mapcat :shapes (:groups layer)))]
     (= 3 (count shape-values))))
   v130_l651)))


(def
 v133_l662
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t134_l667
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v133_l662)))


(def
 v136_l686
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements", :width 500, :palette :dark2})))


(deftest
 t137_l691
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v136_l686)))


(def
 v139_l700
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t140_l705
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v139_l700)))


(def
 v142_l711
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t143_l717
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v142_l711)))


(def
 v145_l734
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t146_l738
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v145_l734)))


(def
 v148_l745
 (->
  {:x [-1 1 -1 1], :y [-1 -1 1 1]}
  (pj/lay-point :x :y)
  (pj/coord :fixed)))


(deftest
 t149_l749
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:points s)) (< (:width s) 600))))
   v148_l745)))


(def
 v151_l758
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t152_l764
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v151_l758)))


(def
 v154_l773
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t155_l779
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v154_l773)))


(def
 v157_l786
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t158_l793
 (is ((fn [v] (= :species (get-in v [:opts :facet-col]))) v157_l786)))


(def
 v160_l797
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram [:sepal-length :sepal-width :petal-length])))


(deftest
 t161_l800
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v160_l797)))


(def
 v163_l806
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram [:sepal-length :sepal-width :petal-length])
  kind/pprint))


(deftest
 t164_l810
 (is
  ((fn
    [v]
    (and
     (= 3 (count (:poses v)))
     (= :sepal-length (get-in v [:poses 0 :mapping :x]))
     (= :sepal-width (get-in v [:poses 1 :mapping :x]))
     (= :petal-length (get-in v [:poses 2 :mapping :x]))))
   v163_l806)))


(def
 v166_l817
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width))]))


(deftest
 t167_l823
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v166_l817)))
