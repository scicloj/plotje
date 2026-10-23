(ns
 plotje-book.glossary-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure2d.color :as c2d]
  [clojure.test :refer [deftest is]]))


(def
 v3_l34
 (def
  my-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Iris"}))))


(def v4_l39 my-pose)


(deftest
 t5_l41
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v4_l39)))


(def v7_l48 (kind/pprint my-pose))


(deftest
 t8_l50
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
   v7_l48)))


(def v10_l125 (-> my-pose :layers first :layer-type))


(deftest t11_l127 (is ((fn [k] (= :point k)) v10_l125)))


(def
 v13_l180
 (def
  tips
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}))


(def
 v14_l184
 (-> tips (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t15_l187
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
   v14_l184)))


(def v17_l223 (-> my-pose pj/draft kind/pprint))


(deftest
 t18_l225
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v17_l223)))


(def v20_l239 (-> my-pose pj/draft :layers first kind/pprint))


(deftest
 t21_l241
 (is
  ((fn
    [d]
    (and
     (some? (:data d))
     (= :sepal-length (:x d))
     (= :sepal-width (:y d))
     (= :species (:color d))
     (= :point (:mark d))))
   v20_l239)))


(def
 v23_l340
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :size :petal-length, :alpha 0.7})))


(deftest
 t24_l344
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= #{0.7} (:alphas s)))))
   v23_l340)))


(def
 v26_l361
 (kind/table
  {:column-names [:aesthetic :role],
   :row-vectors
   (->>
    (pj/aesthetic-roles)
    (sort-by (comp str key))
    (mapv (fn [[k role]] [k role])))}))


(deftest
 t27_l367
 (is
  ((fn
    [_]
    (=
     #{:grouping :panel :positional :appearance}
     (set (vals (pj/aesthetic-roles)))))
   v26_l361)))


(def
 v29_l407
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:group :species})))


(deftest
 t30_l410
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
   v29_l407)))


(def
 v32_l534
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x 1.5, :y 4.5, :align-x :center, :text "between two"})))


(deftest
 t33_l538
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
   v32_l534)))


(def
 v35_l603
 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y {:dx 0.5})))


(deftest
 t36_l606
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
   v35_l603)))


(def
 v38_l622
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
 t39_l627
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
   v38_l622)))


(def
 v41_l654
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:jitter true})))


(deftest
 t42_l657
 (is
  ((fn [v] (and (pj/pose? v) (pos? (:points (pj/svg-summary v)))))
   v41_l654)))


(def
 v44_l670
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t45_l673
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v44_l670)))


(def v47_l687 (def my-plan (pj/plan my-pose)))


(def v48_l689 (kind/pprint my-plan))


(deftest
 t49_l691
 (is
  ((fn
    [plan]
    (and
     (vector? (:panels plan))
     (= 1 (count (:panels plan)))
     (= 600 (:width plan))
     (= 400 (:height plan))
     (some? (:legend plan))))
   v48_l689)))


(def v51_l706 (kind/pprint (first (:panels my-plan))))


(deftest
 t52_l708
 (is
  ((fn
    [p]
    (and
     (= :cartesian (:coord p))
     (= [4.12 8.08] (:x-domain p))
     (= 1 (count (:layers p)))))
   v51_l706)))


(def v54_l720 (kind/pprint (get-in my-plan [:panels 0 :layers 0])))


(deftest
 t55_l722
 (is
  ((fn
    [layer]
    (and
     (= :point (:mark layer))
     (= 3 (count (:groups layer)))
     (every? :xs (:groups layer))))
   v54_l720)))


(def
 v57_l797
 (let
  [p (first (:panels my-plan))]
  {:x-domain (:x-domain p), :y-domain (:y-domain p)}))


(deftest
 t58_l801
 (is
  ((fn
    [m]
    (and
     (= [4.12 8.08] (:x-domain m))
     (= 2 (count (:y-domain m)))
     (number? (first (:y-domain m)))))
   v57_l797)))


(def v60_l828 (-> my-pose pj/plot pj/svg-summary :clips))


(deftest t61_l830 (is ((fn [n] (= 1 n)) v60_l828)))


(def v63_l841 (-> my-plan :panels first :x-ticks))


(deftest
 t64_l843
 (is
  ((fn
    [m]
    (and
     (vector? (:values m))
     (vector? (:labels m))
     (= (count (:values m)) (count (:labels m)))
     (false? (:categorical? m))))
   v63_l841)))


(def
 v66_l922
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t67_l926
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
   v66_l922)))


(def
 v69_l957
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t70_l961
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
   v69_l957)))


(def
 v72_l988
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t73_l992
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v72_l988)))


(def
 v75_l1007
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t76_l1011
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v75_l1007)))


(def
 v78_l1020
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t79_l1024
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__74154#] (-> p1__74154# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v78_l1020)))


(def
 v81_l1080
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-rule-h {:y-intercept 3.0}))))


(def v82_l1085 annotated)


(def v83_l1087 (kind/pprint (nth (:layers annotated) 1)))


(deftest
 t84_l1089
 (is
  ((fn
    [layer]
    (and
     (= :rule-h (:layer-type layer))
     (= 3.0 (get-in layer [:mapping :y-intercept]))))
   v83_l1087)))


(def v86_l1118 (kind/pprint (:legend my-plan)))


(deftest
 t87_l1120
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
   v86_l1118)))


(def
 v89_l1144
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})))


(deftest
 t90_l1148
 (is
  ((fn
    [v]
    (let
     [small
      (pj/options v {:theme {:font-size 8}})
      sizes
      (frequencies
       (keep
        (fn* [p1__74155#] (get-in p1__74155# [1 :font-size]))
        (filter
         (fn*
          [p1__74156#]
          (and (vector? p1__74156#) (= :text (first p1__74156#))))
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
   v89_l1144)))


(def v92_l1182 (def my-membrane (pj/plan->membrane my-plan)))


(def v94_l1188 (kind/pprint my-membrane))


(deftest
 t95_l1190
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
   v94_l1188)))


(def v97_l1214 (def my-plot (pj/plan->plot my-plan :svg {})))


(def v99_l1221 (kind/hiccup my-plot))


(deftest
 t100_l1223
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= :svg (first my-plot))
      (= 150 (:points s))
      (= 600.0 (double (:width s))))))
   v99_l1221)))


(def
 v102_l1240
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values :set2})))


(deftest
 t103_l1244
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v102_l1240)))


(def v105_l1250 (count (c2d/find-palette #".*")))


(deftest t106_l1252 (is ((fn [n] (<= 5000 n)) v105_l1250)))


(def
 v108_l1265
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/scale :color {:range :inferno})))


(deftest
 t109_l1269
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
   v108_l1265)))


(def
 v111_l1296
 (select-keys
  (pj/config)
  [:width :height :theme :color-values :color-range]))


(deftest
 t112_l1298
 (is
  ((fn
    [m]
    (and (number? (:width m)) (number? (:height m)) (map? (:theme m))))
   v111_l1296)))


(def v114_l1315 (sort (keys pj/plot-option-docs)))


(deftest
 t115_l1317
 (is
  ((fn
    [ks]
    (and
     (= 14 (count ks))
     (some #{:caption :title :y-label :x-label :subtitle} ks)))
   v114_l1315)))


(def v117_l1342 (sort (keys pj/layer-option-docs)))


(deftest
 t118_l1344
 (is
  ((fn
    [ks]
    (and
     (pos? (count ks))
     (some #{:group :color :size :alpha :position} ks)))
   v117_l1342)))


(def
 v120_l1357
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true, :brush true})))


(deftest
 t121_l1361
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"data-tooltip" s) (re-find #"nsk-brush-sel" s))))
   v120_l1357)))
