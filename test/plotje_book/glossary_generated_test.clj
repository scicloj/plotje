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
      dinner-bar
      (->
       tips
       (pj/lay-bar :day :count {:color :meal, :position :stack})
       pj/plan
       (get-in [:panels 0 :layers 0 :groups 1]))]
     (and (= 4 (:polygons s)) (every? pos? (:y0s dinner-bar)))))
   v14_l154)))


(def v17_l189 (-> my-pose pj/draft kind/pprint))


(deftest
 t18_l191
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 1 (count (:layers d)))
     (= :point (:mark (first (:layers d))))))
   v17_l189)))


(def v20_l205 (-> my-pose pj/draft :layers first kind/pprint))


(deftest
 t21_l207
 (is
  ((fn
    [d]
    (and
     (some? (:data d))
     (= :sepal-length (:x d))
     (= :sepal-width (:y d))
     (= :species (:color d))
     (= :point (:mark d))))
   v20_l205)))


(def
 v23_l290
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :size :petal-length, :alpha 0.7})))


(deftest
 t24_l294
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= #{0.7} (:alphas s)))))
   v23_l290)))


(def
 v26_l309
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:group :species})))


(deftest
 t27_l312
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
   v26_l309)))


(def
 v29_l418
 (-> {:x [1 2 3], :y [4 5 6]} (pj/lay-point :x :y {:nudge-x 0.5})))


(deftest
 t30_l421
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
   v29_l418)))


(def
 v32_l442
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-length {:jitter true})))


(deftest
 t33_l445
 (is
  ((fn [v] (and (pj/pose? v) (pos? (:points (pj/svg-summary v)))))
   v32_l442)))


(def
 v35_l458
 (-> (rdatasets/datasets-iris) (pj/pose :sepal-length :sepal-width)))


(deftest
 t36_l461
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v35_l458)))


(def v38_l475 (def my-plan (pj/plan my-pose)))


(def v39_l477 (kind/pprint my-plan))


(deftest
 t40_l479
 (is
  ((fn
    [plan]
    (and
     (vector? (:panels plan))
     (= 1 (count (:panels plan)))
     (= 600 (:width plan))
     (= 400 (:height plan))
     (some? (:legend plan))))
   v39_l477)))


(def v42_l494 (kind/pprint (first (:panels my-plan))))


(deftest
 t43_l496
 (is
  ((fn
    [p]
    (and
     (= :cartesian (:coord p))
     (= [4.12 8.08] (:x-domain p))
     (= 1 (count (:layers p)))))
   v42_l494)))


(def v45_l508 (kind/pprint (get-in my-plan [:panels 0 :layers 0])))


(deftest
 t46_l510
 (is
  ((fn
    [layer]
    (and
     (= :point (:mark layer))
     (= 3 (count (:groups layer)))
     (every? :xs (:groups layer))))
   v45_l508)))


(def
 v48_l582
 (let
  [p (first (:panels my-plan))]
  {:x-domain (:x-domain p), :y-domain (:y-domain p)}))


(deftest
 t49_l586
 (is
  ((fn
    [m]
    (and
     (= [4.12 8.08] (:x-domain m))
     (= 2 (count (:y-domain m)))
     (number? (first (:y-domain m)))))
   v48_l582)))


(def v51_l613 (-> my-pose pj/plot pj/svg-summary :clips))


(deftest t52_l615 (is ((fn [n] (= 1 n)) v51_l613)))


(def v54_l626 (-> my-plan :panels first :x-ticks))


(deftest
 t55_l628
 (is
  ((fn
    [m]
    (and
     (vector? (:values m))
     (vector? (:labels m))
     (= (count (:values m)) (count (:labels m)))
     (false? (:categorical? m))))
   v54_l626)))


(def
 v57_l704
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t58_l708
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
   v57_l704)))


(def
 v60_l731
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(deftest
 t61_l735
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
   v60_l731)))


(def
 v63_l792
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-rule-h {:y-intercept 3.0}))))


(def v64_l797 annotated)


(def v65_l799 (kind/pprint (nth (:layers annotated) 1)))


(deftest
 t66_l801
 (is
  ((fn
    [layer]
    (and
     (= :rule-h (:layer-type layer))
     (= 3.0 (get-in layer [:mapping :y-intercept]))))
   v65_l799)))


(def v68_l830 (kind/pprint (:legend my-plan)))


(deftest
 t69_l832
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
   v68_l830)))


(def
 v71_l856
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:theme {:bg "#2d2d2d", :grid "#444444", :font-size 10}})))


(deftest
 t72_l860
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v71_l856)))


(def v74_l883 (def my-membrane (pj/plan->membrane my-plan)))


(def v76_l889 (kind/pprint my-membrane))


(deftest
 t77_l891
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
   v76_l889)))


(def v79_l915 (def my-plot (pj/plan->plot my-plan :svg {})))


(def v81_l922 (kind/hiccup my-plot))


(deftest
 t82_l924
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= :svg (first my-plot))
      (= 150 (:points s))
      (= 600.0 (double (:width s))))))
   v81_l922)))


(def
 v84_l940
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :set2})))


(deftest
 t85_l944
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v84_l940)))


(def v87_l950 (count (c2d/find-palette #".*")))


(deftest t88_l952 (is ((fn [n] (<= 5000 n)) v87_l950)))


(def
 v90_l964
 (->
  {:x (range 50), :y (range 50), :c (range 50)}
  (pj/lay-point :x :y {:color :c})
  (pj/options {:color-scale :inferno})))


(deftest
 t91_l968
 (is
  ((fn
    [v]
    (and
     (= 50 (:points (pj/svg-summary v)))
     (=
      :inferno
      (:color-scale
       (:legend
        (pj/plan
         (->
          {:x (range 50), :y (range 50), :c (range 50)}
          (pj/lay-point :x :y {:color :c})
          (pj/options {:color-scale :inferno}))))))))
   v90_l964)))


(def
 v93_l994
 (select-keys
  (pj/config)
  [:width :height :theme :palette :color-scale]))


(deftest
 t94_l996
 (is
  ((fn
    [m]
    (and (number? (:width m)) (number? (:height m)) (map? (:theme m))))
   v93_l994)))


(def v96_l1013 (sort (keys pj/plot-option-docs)))


(deftest
 t97_l1015
 (is
  ((fn
    [ks]
    (and
     (= 15 (count ks))
     (some #{:caption :title :y-label :x-label :subtitle} ks)))
   v96_l1013)))


(def v99_l1036 (sort (keys pj/layer-option-docs)))


(deftest
 t100_l1038
 (is
  ((fn
    [ks]
    (and
     (pos? (count ks))
     (some #{:group :color :size :alpha :position} ks)))
   v99_l1036)))


(def
 v102_l1051
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:tooltip true, :brush true})))


(deftest
 t103_l1055
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"data-tooltip" s) (re-find #"nsk-brush-sel" s))))
   v102_l1051)))
