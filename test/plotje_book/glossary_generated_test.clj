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
         (fn* [p1__81528#] (nth p1__81528# 2))
         :drawing-area
         :frames)))]
     (and (= 2 (:panels (pj/svg-summary v))) (> wide (* 1.5 narrow)))))
   v10_l95)))


(def
 v13_l125
 (pj/arrange
  (rdatasets/datasets-iris)
  [:sepal-length {:x :sepal-length, :y :sepal-width} nil]))


(deftest
 t14_l128
 (is
  ((fn
    [v]
    (and
     (= 3 (count (:sub-plots (pj/plan v))))
     (=
      {:panels 2, :points 150}
      (select-keys (pj/svg-summary v) [:panels :points]))))
   v13_l125)))


(def v16_l167 (-> my-pose :layers first :layer-type))


(deftest t17_l169 (is ((fn [k] (= :point k)) v16_l167)))


(def
 v19_l222
 (def
  tips
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}))


(def
 v20_l226
 (-> tips (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t21_l229
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
   v20_l226)))


(def v23_l267 (-> my-pose pj/draft kind/pprint))


(deftest
 t24_l269
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v23_l267)))


(def v26_l283 (-> my-pose pj/draft :layers first kind/pprint))


(deftest
 t27_l289
 (is
  ((fn
    [d]
    (and
     (some? (:data d))
     (= :sepal-length (:x d))
     (= :sepal-width (:y d))
     (= :species (:color d))
     (= :point (:mark d))))
   v26_l283)))


(def
 v29_l351
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t30_l356
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v29_l351)))


(def
 v32_l420
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :size :petal-length, :alpha 0.7})))


(deftest
 t33_l424
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= #{0.7} (:alphas s)))))
   v32_l420)))


(def
 v35_l441
 (kind/table
  {:column-names [:aesthetic :role],
   :row-vectors
   (->>
    (pj/aesthetic-roles)
    (sort-by (comp str key))
    (mapv (fn [[k role]] [k role])))}))


(deftest
 t36_l447
 (is
  ((fn
    [_]
    (=
     #{:grouping :panel :positional :appearance}
     (set (vals (pj/aesthetic-roles)))))
   v35_l441)))


(def
 v38_l482
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:group :species})))


(deftest
 t39_l485
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
   v38_l482)))


(def
 v41_l609
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x 1.5, :y 4.5, :align-x :center, :text "between two"})))


(deftest
 t42_l613
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
   v41_l609)))


(def
 v44_l678
 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y {:dx 0.5})))


(deftest
 t45_l681
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
   v44_l678)))


(def
 v47_l697
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
 t48_l702
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
   v47_l697)))


(def
 v50_l729
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:jitter true})))


(deftest
 t51_l732
 (is
  ((fn [v] (and (pj/pose? v) (pos? (:points (pj/svg-summary v)))))
   v50_l729)))


(def
 v53_l751
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t54_l754
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v53_l751)))


(def v56_l768 (def my-plan (pj/plan my-pose)))


(def v57_l770 (kind/pprint my-plan))


(deftest
 t58_l772
 (is
  ((fn
    [plan]
    (and
     (vector? (:panels plan))
     (= 1 (count (:panels plan)))
     (= 600 (:width plan))
     (= 400 (:height plan))
     (some? (:legend plan))))
   v57_l770)))


(def v60_l793 (kind/pprint (first (:panels my-plan))))


(deftest
 t61_l795
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
   v60_l793)))


(def v63_l814 (kind/pprint (get-in my-plan [:panels 0 :layers 0])))


(deftest
 t64_l816
 (is
  ((fn
    [layer]
    (and
     (= :point (:mark layer))
     (= 3 (count (:groups layer)))
     (every? :xs (:groups layer))))
   v63_l814)))


(def
 v66_l892
 (let
  [p (first (:panels my-plan))]
  {:x-domain (:x-domain p), :y-domain (:y-domain p)}))


(deftest
 t67_l896
 (is
  ((fn
    [m]
    (and
     (= [4.12 8.08] (:x-domain m))
     (= 2 (count (:y-domain m)))
     (number? (first (:y-domain m)))))
   v66_l892)))


(def v69_l923 (-> my-pose pj/plot pj/svg-summary :clips))


(deftest t70_l928 (is ((fn [n] (= 1 n)) v69_l923)))


(def v72_l939 (-> my-plan :panels first :x-ticks))


(deftest
 t73_l941
 (is
  ((fn
    [m]
    (and
     (vector? (:values m))
     (vector? (:labels m))
     (= (count (:values m)) (count (:labels m)))
     (false? (:categorical? m))))
   v72_l939)))


(def
 v75_l1020
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t76_l1024
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
   v75_l1020)))


(def
 v78_l1059
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t79_l1063
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
   v78_l1059)))


(def
 v81_l1093
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t82_l1097
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v81_l1093)))


(def
 v84_l1110
 (pj/lay-point
  (rdatasets/datasets-iris)
  :sepal-length
  [:sepal-width :petal-width]))


(deftest
 t85_l1112
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
   v84_l1110)))


(def
 v87_l1145
 (pj/arrange
  (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
  [{:where {:species "setosa"}}
   {:where {:species "versicolor"}}
   {:where {:species "virginica"}}]))


(deftest
 t88_l1148
 (is
  ((fn
    [v]
    (and
     (=
      (pj/plot v)
      (pj/plot
       (->
        (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width)
        (pj/facet :species))))
     (let
      [base
       (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
       panels
       (fn
        [pose]
        (-> pose (pj/lay-point :displ :cty) pj/svg-summary :panels))]
      (and
       (= 3 (panels (pj/facet base :drv)))
       (=
        4
        (panels
         (pj/arrange
          base
          [{:where {:drv "f"}}
           {:where {:drv "4"}}
           {:where {:drv "r"}}])))))))
   v87_l1145)))


(def
 v90_l1177
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t91_l1181
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v90_l1177)))


(def
 v93_l1190
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t94_l1194
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__81529#] (-> p1__81529# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v93_l1190)))


(def
 v96_l1252
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-rule-h {:y-intercept 3.0}))))


(def v97_l1257 annotated)


(def v98_l1259 (kind/pprint (nth (:layers annotated) 1)))


(deftest
 t99_l1261
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
   v98_l1259)))


(def v101_l1302 (kind/pprint (:legend my-plan)))


(deftest
 t102_l1304
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
   v101_l1302)))


(def
 v104_l1328
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})))


(deftest
 t105_l1332
 (is
  ((fn
    [v]
    (let
     [small
      (pj/options v {:theme {:font-size 8}})
      sizes
      (frequencies
       (keep
        (fn* [p1__81530#] (get-in p1__81530# [1 :font-size]))
        (filter
         (fn*
          [p1__81531#]
          (and (vector? p1__81531#) (= :text (first p1__81531#))))
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
   v104_l1328)))


(def v107_l1366 (def my-membrane (pj/plan->membrane my-plan)))


(def v109_l1372 (kind/pprint my-membrane))


(deftest
 t110_l1374
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
   v109_l1372)))


(def v112_l1398 (def my-plot (pj/plan->plot my-plan :svg {})))


(def v114_l1405 (kind/hiccup my-plot))


(deftest
 t115_l1407
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= :svg (first my-plot))
      (= 150 (:points s))
      (= 600.0 (double (:width s))))))
   v114_l1405)))


(def
 v117_l1424
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values :set2})))


(deftest
 t118_l1428
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v117_l1424)))


(def v120_l1434 (count (c2d/find-palette #".*")))


(deftest t121_l1436 (is ((fn [n] (<= 5000 n)) v120_l1434)))


(def
 v123_l1449
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/scale :color {:range :inferno})))


(deftest
 t124_l1453
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
   v123_l1449)))


(def
 v126_l1480
 (select-keys
  (pj/config)
  [:width :height :theme :color-values :color-range]))


(deftest
 t127_l1482
 (is
  ((fn
    [m]
    (and (number? (:width m)) (number? (:height m)) (map? (:theme m))))
   v126_l1480)))


(def v129_l1499 (sort (keys pj/plot-option-docs)))


(deftest
 t130_l1501
 (is
  ((fn
    [ks]
    (and
     (= 16 (count ks))
     (some #{:caption :title :y-label :x-label :subtitle} ks)))
   v129_l1499)))


(def v132_l1530 (sort (keys pj/layer-mapping-docs)))


(deftest
 t133_l1532
 (is
  ((fn
    [ks]
    (and
     (pos? (count ks))
     (some #{:group :color :size :alpha :position} ks)))
   v132_l1530)))


(def
 v135_l1545
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true, :brush true})))


(deftest
 t136_l1549
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"data-tooltip" s) (re-find #"nsk-brush-sel" s))))
   v135_l1545)))
