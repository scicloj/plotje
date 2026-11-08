(ns
 plotje-book.glossary-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure2d.color :as c2d]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (def
  my-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Iris"}))))


(def v4_l40 my-pose)


(deftest
 t5_l42
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v4_l40)))


(def v7_l49 (kind/pprint my-pose))


(deftest
 t8_l51
 (is
  ((fn
    [pose]
    (and
     (some? (:data pose))
     (nil? (:mapping pose))
     (= :sepal-length (get-in pose [:layers 0 :mapping :x]))
     (= :sepal-width (get-in pose [:layers 0 :mapping :y]))
     (= :species (get-in pose [:layers 0 :mapping :color]))
     (= "Iris" (get-in pose [:opts :title]))))
   v7_l49)))


(def
 v10_l95
 (pj/arrange
  (rdatasets/datasets-iris)
  [{:x :sepal-length, :y :sepal-width} :sepal-length]
  {:col-weights [2 1]}))


(deftest
 t11_l99
 (is
  ((fn
    [v]
    (let
     [[wide narrow]
      (->>
       (pj/frames v)
       :panels
       (mapv
        (comp
         (fn* [p1__82736#] (nth p1__82736# 2))
         :drawing-area
         :frames)))]
     (and (= 2 (:panels (pj/svg-summary v))) (> wide (* 1.5 narrow)))))
   v10_l95)))


(def
 v13_l126
 (pj/arrange
  (rdatasets/datasets-iris)
  [:sepal-length {:x :sepal-length, :y :sepal-width} nil]))


(deftest
 t14_l129
 (is
  ((fn
    [v]
    (and
     (= 3 (count (:sub-plots (pj/plan v))))
     (=
      {:panels 2, :points 150}
      (select-keys (pj/svg-summary v) [:panels :points]))))
   v13_l126)))


(def v16_l149 (-> my-pose :layers first :layer-type))


(deftest t17_l154 (is ((fn [k] (= :point k)) v16_l149)))


(def
 v19_l193
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :petal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-width}])
  pj/lay-point
  (pj/lay-smooth {:y :sepal-width})))


(deftest
 t20_l200
 (is
  ((fn
    [v]
    (=
     [[:point :line] [:point :line] [:point]]
     (mapv
      (fn*
       [p1__82737#]
       (mapv :mark (-> p1__82737# :plan :panels first :layers)))
      (:sub-plots (pj/plan v)))))
   v19_l193)))


(def
 v22_l261
 (def
  tips
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}))


(def
 v23_l265
 (-> tips (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t24_l268
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      groups
      (->
       tips
       (pj/lay-bar :day :count {:color :meal, :position :stack})
       pj/plan
       (get-in [:panels 0 :layers 0 :groups]))
      lunch-bar
      (first groups)
      dinner-bar
      (second groups)]
     (and
      (= 4 (:polygons s))
      (every? zero? (:y0s dinner-bar))
      (every? pos? (:y0s lunch-bar)))))
   v23_l265)))


(def v26_l306 (-> my-pose pj/draft kind/pprint))


(deftest
 t27_l310
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v26_l306)))


(def v29_l324 (-> my-pose pj/draft :layers first kind/pprint))


(deftest
 t30_l330
 (is
  ((fn
    [d]
    (and
     (some? (:data d))
     (= :sepal-length (:x d))
     (= :sepal-width (:y d))
     (= :species (:color d))
     (= :point (:mark d))))
   v29_l324)))


(def
 v32_l392
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t33_l397
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v32_l392)))


(def
 v35_l461
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :size :petal-length, :alpha 0.7})))


(deftest
 t36_l465
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= #{0.7} (:alphas s)))))
   v35_l461)))


(def
 v38_l482
 (kind/table
  {:column-names [:aesthetic :role],
   :row-vectors
   (->>
    (pj/aesthetic-roles)
    (sort-by (comp str key))
    (mapv (fn [[k role]] [k role])))}))


(deftest
 t39_l488
 (is
  ((fn
    [_]
    (=
     #{:grouping :panel :positional :appearance}
     (set (vals (pj/aesthetic-roles)))))
   v38_l482)))


(def
 v41_l523
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:group :species})))


(deftest
 t42_l526
 (is
  ((fn
    [v]
    (let
     [groups
      (->
       (rdatasets/datasets-iris)
       (pj/lay-line :sepal-length :sepal-width {:group :species})
       pj/plan
       (get-in [:panels 0 :layers 0 :groups]))]
     (and
      (= 3 (:lines (pj/svg-summary v)))
      (= 3 (count groups))
      (= ["setosa" "versicolor" "virginica"] (mapv :label groups)))))
   v41_l523)))


(def
 v44_l650
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x 1.5, :y 4.5, :align-x :center, :text "between two"})))


(deftest
 t45_l654
 (is
  ((fn
    [v]
    (let
     [panel
      (-> v pj/frames :panels first)
      at
      (fn [c] (first (pj/to-drawing panel c 4.5)))]
     (and
      (= ["red" "green" "blue"] (-> v pj/plan :panels first :x-domain))
      (= (at 1) (at "red"))
      (< (abs (- (at 1.5) (/ (+ (at "red") (at "green")) 2.0))) 1.0E-9)
      (every? number? [(at 0.5) (at 3.5)])
      (every?
       (fn
        [bad]
        (try
         (at bad)
         false
         (catch
          Exception
          e
          (boolean
           (re-find #"past the ends of this axis" (ex-message e))))))
       [0.4 3.6])
      (=
       3
       (:points
        (pj/svg-summary
         (pj/plot
          (->
           {:h [1 2 ##Inf 4], :w [1 2 3 4]}
           (pj/lay-point :h :w)))))))))
   v44_l650)))


(def
 v47_l726
 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y {:dx 0.5})))


(deftest
 t48_l729
 (is
  ((fn
    [v]
    (let
     [xs
      (->
       {:x [1 2 3], :y [4 5 6]}
       (pj/lay-point :x :y {:dx 0.5})
       pj/plan
       (get-in [:panels 0 :layers 0 :groups 0 :xs]))]
     (and (= 3 (:points (pj/svg-summary v))) (= [1.5 2.5 3.5] xs))))
   v47_l726)))


(def
 v50_l745
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
 t51_l750
 (is
  ((fn
    [v]
    (let
     [panel
      (-> v pj/frames :panels first)
      at
      (fn [c] (first (pj/to-drawing panel c 4.5)))]
     (and
      (=
       (pj/plot v)
       (pj/plot
        (->
         {:team ["red" "green" "blue"], :score [3 5 4]}
         (pj/lay-bar :team :score {:color "#a6cee3"})
         (pj/lay-text
          {:x 1.5,
           :y 3,
           :align-x :center,
           :offset-y -10,
           :text "half a band"}))))
      (< (abs (- (at 1.5) (/ (+ (at "red") (at "green")) 2.0))) 1.0E-9)
      (=
       ["red" "green" "blue"]
       (-> v pj/plan :panels first :x-domain)))))
   v50_l745)))


(def
 v53_l784
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:jitter true})))


(deftest
 t54_l787
 (is
  ((fn [v] (and (pj/pose? v) (pos? (:points (pj/svg-summary v)))))
   v53_l784)))


(def
 v56_l806
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t57_l809
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v56_l806)))


(def v59_l823 (def my-plan (pj/plan my-pose)))


(def v60_l825 (kind/pprint my-plan))


(deftest
 t61_l827
 (is
  ((fn
    [plan]
    (and
     (vector? (:panels plan))
     (= 1 (count (:panels plan)))
     (= 600 (:width plan))
     (= 400 (:height plan))
     (some? (:legend plan))))
   v60_l825)))


(def v63_l849 (kind/pprint (first (:panels my-plan))))


(deftest
 t64_l851
 (is
  ((fn
    [p]
    (and
     (= :cartesian (:coord p))
     (= [4.12 8.08] (:x-domain p))
     (= 1 (count (:layers p)))
     (let
      [plan
       (pj/plan
        (->
         (rdatasets/datasets-iris)
         (pj/pose {:x :sepal-length})
         pj/lay-point
         (pj/facet {:y [:sepal-width :petal-width]})))]
      (and (empty? (:panels plan)) (= 2 (count (:sub-plots plan)))))))
   v63_l849)))


(def v66_l870 (kind/pprint (get-in my-plan [:panels 0 :layers 0])))


(deftest
 t67_l872
 (is
  ((fn
    [layer]
    (and
     (= :point (:mark layer))
     (= 3 (count (:groups layer)))
     (every? :xs (:groups layer))))
   v66_l870)))


(def
 v69_l948
 (let
  [p (first (:panels my-plan))]
  {:x-domain (:x-domain p), :y-domain (:y-domain p)}))


(deftest
 t70_l952
 (is
  ((fn
    [m]
    (and
     (= [4.12 8.08] (:x-domain m))
     (= 2 (count (:y-domain m)))
     (number? (first (:y-domain m)))))
   v69_l948)))


(def v72_l979 (-> my-pose pj/plot pj/svg-summary :clips))


(deftest t73_l984 (is ((fn [n] (= 1 n)) v72_l979)))


(def v75_l995 (-> my-plan :panels first :x-ticks))


(deftest
 t76_l1000
 (is
  ((fn
    [m]
    (and
     (vector? (:values m))
     (vector? (:labels m))
     (= (count (:values m)) (count (:labels m)))
     (false? (:categorical? m))))
   v75_l995)))


(def
 v78_l1079
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t79_l1083
 (is
  ((fn
    [v]
    (and
     (= 3 (:polygons (pj/svg-summary v)))
     (=
      :flip
      (->
       (rdatasets/datasets-iris)
       (pj/lay-bar :species)
       (pj/coord :flip)
       pj/plan
       (get-in [:panels 0 :coord])))))
   v78_l1079)))


(def
 v81_l1121
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t82_l1125
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      n-panels
      (count
       (:panels
        (pj/plan
         (->
          (rdatasets/datasets-iris)
          (pj/lay-point :sepal-length :sepal-width)
          (pj/facet :species)))))]
     (and
      (= 3 (:panels s))
      (= 3 n-panels)
      (=
       {:panels 2, :points 300}
       (select-keys
        (pj/svg-summary
         (->
          (rdatasets/datasets-iris)
          (pj/pose {:x :sepal-length})
          pj/lay-point
          (pj/facet {:y [:sepal-width :petal-width]})))
        [:panels :points])))))
   v81_l1121)))


(def
 v84_l1157
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t85_l1161
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v84_l1157)))


(def
 v87_l1174
 (pj/lay-point
  (rdatasets/datasets-iris)
  :sepal-length
  [:sepal-width :petal-width]))


(deftest
 t88_l1176
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width)
       (pj/lay-point :sepal-length :petal-width)))))
   v87_l1174)))


(def
 v90_l1218
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t91_l1222
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v90_l1218)))


(def
 v93_l1231
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t94_l1235
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__82738#] (-> p1__82738# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v93_l1231)))


(def
 v96_l1297
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-rule-h {:y-intercept 3.0}))))


(def v97_l1302 annotated)


(def v98_l1304 (kind/pprint (nth (:layers annotated) 1)))


(deftest
 t99_l1306
 (is
  ((fn
    [layer]
    (and
     (= :rule-h (:layer-type layer))
     (= 3.0 (get-in layer [:mapping :y-intercept]))
     (=
      (pj/plot annotated)
      (pj/plot
       (->
        (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width)
        (pj/lay-rule-h {:y-intercept {:value 3.0}}))))
     (re-find
      #"there is no column for \{:column \.\.\.\} to name"
      (try
       (->
        (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width)
        (pj/lay-rule-h {:y-intercept {:column :sepal-width}})
        pj/plan)
       "no error"
       (catch clojure.lang.ExceptionInfo e (ex-message e))))))
   v98_l1304)))


(def v101_l1347 (kind/pprint (:legend my-plan)))


(deftest
 t102_l1349
 (is
  ((fn
    [leg]
    (and
     (map? leg)
     (= :species (:title leg))
     (= 3 (count (:entries leg)))
     (=
      ["setosa" "versicolor" "virginica"]
      (mapv :label (:entries leg)))))
   v101_l1347)))


(def
 v104_l1373
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})))


(deftest
 t105_l1377
 (is
  ((fn
    [v]
    (let
     [small
      (pj/options v {:theme {:font-size 8}})
      sizes
      (frequencies
       (keep
        (fn* [p1__82739#] (get-in p1__82739# [1 :font-size]))
        (filter
         (fn*
          [p1__82740#]
          (and (vector? p1__82740#) (= :text (first p1__82740#))))
         (tree-seq vector? seq (pj/plot small)))))
      panel
      (first (:panels (pj/plan small)))
      n-tick-labels
      (+
       (count (:labels (:x-ticks panel)))
       (count (:labels (:y-ticks panel))))]
     (and
      (= 150 (:points (pj/svg-summary v)))
      (= n-tick-labels (get sizes 8)))))
   v104_l1373)))


(def v107_l1412 (def my-membrane (pj/plan->membrane my-plan)))


(def v109_l1418 (kind/pprint my-membrane))


(deftest
 t110_l1420
 (is
  ((fn
    [m]
    (let
     [walk-text
      (fn
       walk
       [d]
       (cond
        (string? (:text d))
        (:text d)
        (:drawable d)
        (walk (:drawable d))
        (:drawables d)
        (some walk (:drawables d))))
      drawables
      (membrane.ui/children m)
      texts
      (mapv walk-text drawables)]
     (and
      (pj/membrane? m)
      (= 9 (count drawables))
      (=
       ["Iris" "sepal width" "sepal length" "species"]
       (vec (take 4 texts))))))
   v109_l1418)))


(def v112_l1444 (def my-plot (pj/plan->plot my-plan :svg {})))


(def v114_l1451 (kind/hiccup my-plot))


(deftest
 t115_l1453
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= :svg (first my-plot))
      (= 150 (:points s))
      (= 600.0 (double (:width s))))))
   v114_l1451)))


(def
 v117_l1470
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values :set2})))


(deftest
 t118_l1474
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v117_l1470)))


(def v120_l1480 (count (c2d/find-palette #".*")))


(deftest t121_l1482 (is ((fn [n] (<= 5000 n)) v120_l1480)))


(def
 v123_l1495
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/scale :color {:range :inferno})))


(deftest
 t124_l1499
 (is
  ((fn
    [v]
    (and
     (= 50 (:points (pj/svg-summary v)))
     (=
      :inferno
      (:color-range
       (:legend
        (pj/plan
         (->
          {:x (range 50), :y (range 50), :c (range 50)}
          (pj/lay-point :x :y {:color :c})
          (pj/scale :color {:range :inferno}))))))))
   v123_l1495)))


(def
 v126_l1526
 (select-keys
  (pj/config)
  [:width :height :theme :color-values :color-range]))


(deftest
 t127_l1528
 (is
  ((fn
    [m]
    (and (number? (:width m)) (number? (:height m)) (map? (:theme m))))
   v126_l1526)))


(def v129_l1545 (sort (keys pj/plot-option-docs)))


(deftest
 t130_l1547
 (is
  ((fn
    [ks]
    (and
     (= 16 (count ks))
     (some #{:caption :title :y-label :x-label :subtitle} ks)))
   v129_l1545)))


(def v132_l1576 (sort (keys pj/layer-mapping-docs)))


(deftest
 t133_l1578
 (is
  ((fn
    [ks]
    (and
     (pos? (count ks))
     (some #{:group :color :size :alpha :position} ks)))
   v132_l1576)))


(def
 v135_l1591
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true, :brush true})))


(deftest
 t136_l1595
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"data-tooltip" s) (re-find #"nsk-brush-sel" s))))
   v135_l1591)))
