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
   (fn* [p1__11193#] (= "setosa" (:species p1__11193#))))))


(def
 v50_l311
 (def
  versicolor
  (tc/select-rows
   (rdatasets/datasets-iris)
   (fn* [p1__11194#] (= "versicolor" (:species p1__11194#))))))


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
 v63_l384
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort)
  (pj/lay-bar :tax :cohort {:bar-width 0.4})))


(deftest
 t64_l388
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 6 (:polygons s))
      (some #{"growth, tax"} (:texts s)))))
   v63_l384)))


(def
 v66_l401
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} {:x :displ, :y :cty}])
  pj/lay-point))


(deftest
 t67_l405
 (is
  ((fn
    [v]
    (and
     (= [{:layer-type :point}] (:layers v))
     (=
      {:panels 2, :points 468}
      (select-keys (pj/svg-summary v) [:panels :points]))))
   v66_l401)))


(def
 v69_l417
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} {:x :displ, :y :cty}])
  pj/lay-point
  (pj/lay-smooth :displ :cty)
  (pj/lay-smooth :cty :hwy)))


(deftest
 t70_l423
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
   v69_l417)))


(def v72_l460 (pj/layer-type-lookup :histogram))


(deftest t73_l462 (is ((fn [m] (= :bar (:mark m))) v72_l460)))


(def
 v75_l466
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t76_l469
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v75_l466)))


(def v78_l475 (pj/layer-type-lookup :smooth))


(deftest t79_l477 (is ((fn [m] (= :loess (:stat m))) v78_l475)))


(def
 v81_l481
 (->
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}
  (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t82_l486
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v81_l481)))


(def
 v84_l516
 (-> {:height [170 180 165 175], :weight [70 80 65 75]} pj/lay-point))


(deftest
 t85_l519
 (is ((fn [v] (= 4 (:points (pj/svg-summary v)))) v84_l516)))


(def
 v87_l526
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t88_l529
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v87_l526)))


(def v90_l533 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))


(deftest
 t91_l536
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v90_l533)))


(def
 v93_l553
 (def
  scatter-base
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width))))


(def v95_l559 (-> scatter-base (pj/lay-smooth {:stat :linear-model})))


(deftest
 t96_l561
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v95_l559)))


(def v98_l567 (-> scatter-base pj/lay-smooth))


(deftest
 t99_l569
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v98_l567)))


(def
 v101_l581
 (def
  scatter-with-regression
  (->
   (pj/pose nil {:x :height, :y :weight, :color :cohort})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   (pj/options {:title "Scatter with Regression"}))))


(def v103_l590 (kind/pprint scatter-with-regression))


(deftest
 t104_l592
 (is
  ((fn
    [v]
    (and
     (nil? (:data v))
     (= 2 (count (:layers v)))
     (= "Scatter with Regression" (get-in v [:opts :title]))))
   v103_l590)))


(def v106_l599 scatter-with-regression)


(deftest
 t107_l601
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
   v106_l599)))


(def
 v109_l610
 (->
  scatter-with-regression
  (pj/with-data
   {:height [1 2 3 4 5 6],
    :weight [2 4 3 5 6 8],
    :cohort ["a" "a" "a" "b" "b" "b"]})))


(deftest
 t110_l615
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v109_l610)))


(def
 v112_l621
 (->
  scatter-with-regression
  (pj/with-data
   {:height [10 20 30 40 50 60],
    :weight [15 18 22 20 25 28],
    :cohort ["c" "c" "c" "d" "d" "d"]})))


(deftest
 t113_l626
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 6 (:points s)) (= 2 (:lines s)))))
   v112_l621)))


(def
 v115_l636
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [(->
     (pj/lay-point nil :displ :hwy)
     (pj/lay-smooth {:stat :linear-model}))
    {:x :displ, :y :cty}])))


(deftest
 t116_l641
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 1 (:lines s)) (= 468 (:points s)))))
   v115_l636)))


(def
 v118_l658
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t119_l661
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (some #{"setosa"} (:texts s)))))
   v118_l658)))


(def
 v121_l667
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})))


(deftest
 t122_l670
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v121_l667)))


(def
 v124_l674
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color "steelblue"})))


(deftest
 t125_l677
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v124_l674)))


(def
 v127_l696
 (->
  {"height" [1 2 3], "weight" [1 2 3], "blue" ["a" "b" "c"]}
  (pj/lay-point "height" "weight" {:color "blue"})))


(deftest
 t128_l699
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= 3 (count colors))))
   v127_l696)))


(def
 v130_l706
 (->
  {"height" [1 2 3], "weight" [1 2 3]}
  (pj/lay-point "height" "weight" {:color "blue"})))


(deftest
 t131_l709
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) colors (disj (:colors s) "none")]
     (= #{"rgb(0,0,255)"} colors)))
   v130_l706)))


(def
 v133_l717
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t134_l720
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v133_l717)))


(def
 v136_l730
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :petal-length, :size :petal-width, :alpha 0.7})))


(deftest
 t137_l734
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v136_l730)))


(def
 v139_l740
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})))


(deftest
 t140_l743
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
   v139_l740)))


(def
 v142_l757
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :shape :species})))


(deftest
 t143_l761
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
   v142_l757)))


(def
 v145_l770
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:group :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t146_l775
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v145_l770)))


(def
 v148_l794
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:title "Iris Measurements", :color-values :dark2})))


(deftest
 t149_l799
 (is
  ((fn [v] (some #{"Iris Measurements"} (:texts (pj/svg-summary v))))
   v148_l794)))


(def
 v151_l811
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})))


(deftest
 t152_l816
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v151_l811)))


(def
 v154_l823
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-h {:y-intercept 3.0})
  (pj/lay-band-v {:x-min 5.0, :x-max 6.0, :alpha 0.1})
  kind/pprint))


(deftest
 t155_l829
 (is
  ((fn
    [v]
    (and
     (= :point (get-in v [:layers 0 :layer-type]))
     (= :rule-h (get-in v [:layers 1 :layer-type]))
     (= 3.0 (get-in v [:layers 1 :mapping :y-intercept]))
     (= :band-v (get-in v [:layers 2 :layer-type]))
     (= 5.0 (get-in v [:layers 2 :mapping :x-min]))))
   v154_l823)))


(def
 v157_l845
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/coord :flip)))


(deftest
 t158_l849
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v157_l845)))


(def
 v160_l857
 (->
  {:east [-1 1 -1 1], :north [-1 -1 1 1]}
  (pj/lay-point :east :north)
  (pj/coord :fixed)))


(deftest
 t161_l861
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      [_ _ w h]
      (-> (pj/frames v) :panels first :frames :drawing-area)]
     (and (= 4 (:points s)) (< (:width s) 600) (= w h))))
   v160_l857)))


(def
 v163_l872
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y :log)))


(deftest
 t164_l878
 (is ((fn [v] (= 6 (:points (pj/svg-summary v)))) v163_l872)))


(def
 v166_l890
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t167_l896
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v166_l890)))


(def
 v169_l904
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  (pj/facet :species)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  kind/pprint))


(deftest
 t170_l911
 (is ((fn [v] (= :species (get-in v [:mapping :col]))) v169_l904)))


(def
 v172_l917
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t173_l920
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (count (disj (:colors s) "none"))))))
   v172_l917)))


(def
 v175_l926
 (->
  (rdatasets/datasets-iris)
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})
  (pj/lay-histogram)))


(deftest
 t176_l930
 (is
  ((fn
    [v]
    (and
     (= 3 (:panels (pj/svg-summary v)))
     (= {:x [:sepal-length :sepal-width :petal-length]} (:facet v))
     (nil? (:mapping v))))
   v175_l926)))


(def
 v178_l941
 (pj/arrange
  [(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-width))
   (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-width))]))


(deftest
 t179_l945
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v178_l941)))
