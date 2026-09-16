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


(def v7_l47 (kind/pprint my-pose))


(deftest
 t8_l49
 (is
  ((fn
    [pose]
    (and
     (some? (:data pose))
     (= :sepal-length (get-in pose [:mapping :x]))
     (= :sepal-width (get-in pose [:mapping :y]))
     (= :species (get-in pose [:layers 0 :mapping :color]))
     (= "Iris" (get-in pose [:opts :title]))))
   v7_l47)))


(def v10_l110 (-> my-pose :layers first :layer-type))


(deftest t11_l112 (is ((fn [k] (= :point k)) v10_l110)))


(def
 v13_l150
 (def
  tips
  {:day ["Mon" "Mon" "Tue" "Tue"],
   :count [30 20 45 15],
   :meal ["lunch" "dinner" "lunch" "dinner"]}))


(def
 v14_l154
 (-> tips (pj/lay-bar :day :count {:color :meal, :position :stack})))


(deftest
 t15_l157
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
   v14_l154)))


(def v17_l193 (-> my-pose pj/draft kind/pprint))


(deftest
 t18_l195
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v17_l193)))


(def v20_l209 (-> my-pose pj/draft :layers first kind/pprint))


(deftest
 t21_l211
 (is
  ((fn
    [d]
    (and
     (some? (:data d))
     (= :sepal-length (:x d))
     (= :sepal-width (:y d))
     (= :species (:color d))
     (= :point (:mark d))))
   v20_l209)))


(def
 v23_l294
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :size :petal-length, :alpha 0.7})))


(deftest
 t24_l298
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= #{0.7} (:alphas s)))))
   v23_l294)))


(def
 v26_l313
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:group :species})))


(deftest
 t27_l316
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
   v26_l313)))


(def
 v29_l428
 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y {:nudge-x 0.5})))


(deftest
 t30_l431
 (is
  ((fn
    [v]
    (let
     [xs
      (->
       {:x [1 2 3], :y [4 5 6]}
       (pj/lay-point :x :y {:nudge-x 0.5})
       pj/plan
       (get-in [:panels 0 :layers 0 :groups 0 :xs]))]
     (and (= 3 (:points (pj/svg-summary v))) (= [1.5 2.5 3.5] xs))))
   v29_l428)))


(def
 v32_l447
 (->
  {:team ["red" "green" "blue"], :score [3 5 4]}
  (pj/lay-bar :team :score {:color "#a6cee3"})
  (pj/lay-text
   {:x {:value "red"},
    :y 3,
    :align-x :center,
    :nudge-x 0.5,
    :offset-y -10,
    :text "half a band"})))


(deftest
 t33_l452
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
   v32_l447)))


(def
 v35_l479
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:jitter true})))


(deftest
 t36_l482
 (is
  ((fn [v] (and (pj/pose? v) (pos? (:points (pj/svg-summary v)))))
   v35_l479)))


(def
 v38_l495
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t39_l498
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v38_l495)))


(def v41_l512 (def my-plan (pj/plan my-pose)))


(def v42_l514 (kind/pprint my-plan))


(deftest
 t43_l516
 (is
  ((fn
    [plan]
    (and
     (vector? (:panels plan))
     (= 1 (count (:panels plan)))
     (= 600 (:width plan))
     (= 400 (:height plan))
     (some? (:legend plan))))
   v42_l514)))


(def v45_l531 (kind/pprint (first (:panels my-plan))))


(deftest
 t46_l533
 (is
  ((fn
    [p]
    (and
     (= :cartesian (:coord p))
     (= [4.12 8.08] (:x-domain p))
     (= 1 (count (:layers p)))))
   v45_l531)))


(def v48_l545 (kind/pprint (get-in my-plan [:panels 0 :layers 0])))


(deftest
 t49_l547
 (is
  ((fn
    [layer]
    (and
     (= :point (:mark layer))
     (= 3 (count (:groups layer)))
     (every? :xs (:groups layer))))
   v48_l545)))


(def
 v51_l621
 (let
  [p (first (:panels my-plan))]
  {:x-domain (:x-domain p), :y-domain (:y-domain p)}))


(deftest
 t52_l625
 (is
  ((fn
    [m]
    (and
     (= [4.12 8.08] (:x-domain m))
     (= 2 (count (:y-domain m)))
     (number? (first (:y-domain m)))))
   v51_l621)))


(def v54_l652 (-> my-pose pj/plot pj/svg-summary :clips))


(deftest t55_l654 (is ((fn [n] (= 1 n)) v54_l652)))


(def v57_l665 (-> my-plan :panels first :x-ticks))


(deftest
 t58_l667
 (is
  ((fn
    [m]
    (and
     (vector? (:values m))
     (vector? (:labels m))
     (= (count (:values m)) (count (:labels m)))
     (false? (:categorical? m))))
   v57_l665)))


(def
 v60_l746
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t61_l750
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
   v60_l746)))


(def
 v63_l773
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t64_l777
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
     (and (= 3 (:panels s)) (= 3 n-panels))))
   v63_l773)))


(def
 v66_l798
 (->
  {:cohort [:a :b :c], :growth [12 19 15], :tax [3 5 4]}
  pj/overlay
  (pj/lay-bar :growth :cohort {:color "#377eb8"})
  (pj/lay-bar :tax :cohort {:bar-width 0.4, :color "#e6550d"})))


(deftest
 t67_l803
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:polygons s)))))
   v66_l798)))


(def
 v69_l818
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top)))


(deftest
 t70_l822
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v69_l818)))


(def
 v72_l831
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :right)))


(deftest
 t73_l835
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__72622#] (-> p1__72622# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= (:y-domain (first panels)) (:y-domain (second panels))))))
   v72_l831)))


(def
 v75_l891
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-rule-h {:y-intercept 3.0}))))


(def v76_l896 annotated)


(def v77_l898 (kind/pprint (nth (:layers annotated) 1)))


(deftest
 t78_l900
 (is
  ((fn
    [layer]
    (and
     (= :rule-h (:layer-type layer))
     (= 3.0 (get-in layer [:mapping :y-intercept]))))
   v77_l898)))


(def v80_l929 (kind/pprint (:legend my-plan)))


(deftest
 t81_l931
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
   v80_l929)))


(def
 v83_l955
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})))


(deftest
 t84_l959
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v83_l955)))


(def v86_l982 (def my-membrane (pj/plan->membrane my-plan)))


(def v88_l988 (kind/pprint my-membrane))


(deftest
 t89_l990
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
   v88_l988)))


(def v91_l1014 (def my-plot (pj/plan->plot my-plan :svg {})))


(def v93_l1021 (kind/hiccup my-plot))


(deftest
 t94_l1023
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= :svg (first my-plot))
      (= 150 (:points s))
      (= 600.0 (double (:width s))))))
   v93_l1021)))


(def
 v96_l1040
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values :set2})))


(deftest
 t97_l1044
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v96_l1040)))


(def v99_l1050 (count (c2d/find-palette #".*")))


(deftest t100_l1052 (is ((fn [n] (<= 5000 n)) v99_l1050)))


(def
 v102_l1065
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/scale :color {:range :inferno})))


(deftest
 t103_l1069
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
   v102_l1065)))


(def
 v105_l1095
 (select-keys
  (pj/config)
  [:width :height :theme :color-values :color-range]))


(deftest
 t106_l1097
 (is
  ((fn
    [m]
    (and (number? (:width m)) (number? (:height m)) (map? (:theme m))))
   v105_l1095)))


(def v108_l1114 (sort (keys pj/plot-option-docs)))


(deftest
 t109_l1116
 (is
  ((fn
    [ks]
    (and
     (= 15 (count ks))
     (some #{:caption :title :y-label :x-label :subtitle} ks)))
   v108_l1114)))


(def v111_l1137 (sort (keys pj/layer-option-docs)))


(deftest
 t112_l1139
 (is
  ((fn
    [ks]
    (and
     (pos? (count ks))
     (some #{:group :color :size :alpha :position} ks)))
   v111_l1137)))


(def
 v114_l1152
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true, :brush true})))


(deftest
 t115_l1156
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"data-tooltip" s) (re-find #"nsk-brush-sel" s))))
   v114_l1152)))
