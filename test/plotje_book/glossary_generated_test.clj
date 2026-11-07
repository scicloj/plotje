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
         (fn* [p1__79300#] (nth p1__79300# 2))
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


(def v16_l169 (-> my-pose :layers first :layer-type))


(deftest t17_l171 (is ((fn [k] (= :point k)) v16_l169)))


(def
 v19_l224
 (def
  tips
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}))


(def
 v20_l228
 (-> tips (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t21_l231
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
   v20_l228)))


(def v23_l269 (-> my-pose pj/draft kind/pprint))


(deftest
 t24_l271
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v23_l269)))


(def v26_l285 (-> my-pose pj/draft :layers first kind/pprint))


(deftest
 t27_l291
 (is
  ((fn
    [d]
    (and
     (some? (:data d))
     (= :sepal-length (:x d))
     (= :sepal-width (:y d))
     (= :species (:color d))
     (= :point (:mark d))))
   v26_l285)))


(def
 v29_l353
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width {:color :species})
  (pj/lay-point {:color nil})
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t30_l358
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v29_l353)))


(def
 v32_l422
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :size :petal-length, :alpha 0.7})))


(deftest
 t33_l426
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= #{0.7} (:alphas s)))))
   v32_l422)))


(def
 v35_l443
 (kind/table
  {:column-names [:aesthetic :role],
   :row-vectors
   (->>
    (pj/aesthetic-roles)
    (sort-by (comp str key))
    (mapv (fn [[k role]] [k role])))}))


(deftest
 t36_l449
 (is
  ((fn
    [_]
    (=
     #{:grouping :panel :positional :appearance}
     (set (vals (pj/aesthetic-roles)))))
   v35_l443)))


(def
 v38_l484
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:group :species})))


(deftest
 t39_l487
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
   v38_l484)))


(def
 v41_l611
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x 1.5, :y 4.5, :align-x :center, :text "between two"})))


(deftest
 t42_l615
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
   v41_l611)))


(def
 v44_l680
 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y {:dx 0.5})))


(deftest
 t45_l683
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
   v44_l680)))


(def
 v47_l699
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
 t48_l704
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
   v47_l699)))


(def
 v50_l731
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:jitter true})))


(deftest
 t51_l734
 (is
  ((fn [v] (and (pj/pose? v) (pos? (:points (pj/svg-summary v)))))
   v50_l731)))


(def
 v53_l753
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t54_l756
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v53_l753)))


(def v56_l770 (def my-plan (pj/plan my-pose)))


(def v57_l772 (kind/pprint my-plan))


(deftest
 t58_l774
 (is
  ((fn
    [plan]
    (and
     (vector? (:panels plan))
     (= 1 (count (:panels plan)))
     (= 600 (:width plan))
     (= 400 (:height plan))
     (some? (:legend plan))))
   v57_l772)))


(def v60_l795 (kind/pprint (first (:panels my-plan))))


(deftest
 t61_l797
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
   v60_l795)))


(def v63_l816 (kind/pprint (get-in my-plan [:panels 0 :layers 0])))


(deftest
 t64_l818
 (is
  ((fn
    [layer]
    (and
     (= :point (:mark layer))
     (= 3 (count (:groups layer)))
     (every? :xs (:groups layer))))
   v63_l816)))


(def
 v66_l894
 (let
  [p (first (:panels my-plan))]
  {:x-domain (:x-domain p), :y-domain (:y-domain p)}))


(deftest
 t67_l898
 (is
  ((fn
    [m]
    (and
     (= [4.12 8.08] (:x-domain m))
     (= 2 (count (:y-domain m)))
     (number? (first (:y-domain m)))))
   v66_l894)))


(def v69_l925 (-> my-pose pj/plot pj/svg-summary :clips))


(deftest t70_l930 (is ((fn [n] (= 1 n)) v69_l925)))


(def v72_l941 (-> my-plan :panels first :x-ticks))


(deftest
 t73_l943
 (is
  ((fn
    [m]
    (and
     (vector? (:values m))
     (vector? (:labels m))
     (= (count (:values m)) (count (:labels m)))
     (false? (:categorical? m))))
   v72_l941)))


(def
 v75_l1022
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t76_l1026
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
   v75_l1022)))


(def
 v78_l1061
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t79_l1065
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
   v78_l1061)))


(def
 v81_l1095
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t82_l1099
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v81_l1095)))


(def
 v84_l1112
 (pj/lay-point
  (rdatasets/datasets-iris)
  :sepal-length
  [:sepal-width :petal-width]))


(deftest
 t85_l1114
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
   v84_l1112)))


(def
 v87_l1147
 (pj/arrange
  (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)
  [{:where {:species "setosa"}}
   {:where {:species "versicolor"}}
   {:where {:species "virginica"}}]))


(deftest
 t88_l1150
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
   v87_l1147)))


(def
 v90_l1179
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t91_l1183
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v90_l1179)))


(def
 v93_l1192
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t94_l1196
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__79301#] (-> p1__79301# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v93_l1192)))


(def
 v96_l1254
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-rule-h {:y-intercept 3.0}))))


(def v97_l1259 annotated)


(def v98_l1261 (kind/pprint (nth (:layers annotated) 1)))


(deftest
 t99_l1263
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
   v98_l1261)))


(def v101_l1304 (kind/pprint (:legend my-plan)))


(deftest
 t102_l1306
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
   v101_l1304)))


(def
 v104_l1330
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})))


(deftest
 t105_l1334
 (is
  ((fn
    [v]
    (let
     [small
      (pj/options v {:theme {:font-size 8}})
      sizes
      (frequencies
       (keep
        (fn* [p1__79302#] (get-in p1__79302# [1 :font-size]))
        (filter
         (fn*
          [p1__79303#]
          (and (vector? p1__79303#) (= :text (first p1__79303#))))
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
   v104_l1330)))


(def v107_l1369 (def my-membrane (pj/plan->membrane my-plan)))


(def v109_l1375 (kind/pprint my-membrane))


(deftest
 t110_l1377
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
   v109_l1375)))


(def v112_l1401 (def my-plot (pj/plan->plot my-plan :svg {})))


(def v114_l1408 (kind/hiccup my-plot))


(deftest
 t115_l1410
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= :svg (first my-plot))
      (= 150 (:points s))
      (= 600.0 (double (:width s))))))
   v114_l1408)))


(def
 v117_l1427
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values :set2})))


(deftest
 t118_l1431
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v117_l1427)))


(def v120_l1437 (count (c2d/find-palette #".*")))


(deftest t121_l1439 (is ((fn [n] (<= 5000 n)) v120_l1437)))


(def
 v123_l1452
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/scale :color {:range :inferno})))


(deftest
 t124_l1456
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
   v123_l1452)))


(def
 v126_l1483
 (select-keys
  (pj/config)
  [:width :height :theme :color-values :color-range]))


(deftest
 t127_l1485
 (is
  ((fn
    [m]
    (and (number? (:width m)) (number? (:height m)) (map? (:theme m))))
   v126_l1483)))


(def v129_l1502 (sort (keys pj/plot-option-docs)))


(deftest
 t130_l1504
 (is
  ((fn
    [ks]
    (and
     (= 16 (count ks))
     (some #{:caption :title :y-label :x-label :subtitle} ks)))
   v129_l1502)))


(def v132_l1533 (sort (keys pj/layer-mapping-docs)))


(deftest
 t133_l1535
 (is
  ((fn
    [ks]
    (and
     (pos? (count ks))
     (some #{:group :color :size :alpha :position} ks)))
   v132_l1533)))


(def
 v135_l1548
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true, :brush true})))


(deftest
 t136_l1552
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"data-tooltip" s) (re-find #"nsk-brush-sel" s))))
   v135_l1548)))
