(ns
 plotje-book.distributions-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l19
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t4_l22
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v3_l19)))


(def
 v6_l31
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})))


(deftest
 t7_l34
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v6_l31)))


(def
 v9_l43
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :petal-width)))


(deftest
 t10_l46
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v9_l43)))


(def
 v12_l53
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-histogram :total-bill)
  (pj/options
   {:title "Distribution of Total Bill", :x-label "Amount ($)"})))


(deftest
 t13_l58
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (pos? (:polygons s))
      (some
       (fn* [p1__81571#] (= "Distribution of Total Bill" p1__81571#))
       (:texts s)))))
   v12_l53)))


(def
 v15_l70
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:normalize :density, :alpha 0.5})
  pj/lay-density))


(deftest
 t16_l74
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      domain
      (fn*
       [p1__81572#]
       (-> p1__81572# pj/plan :panels first :x-domain))]
     (and
      (= 1 (:panels s))
      (= 10 (:polygons s))
      (= [4.12 8.08] (domain v))
      (=
       (domain v)
       (domain
        (->
         (rdatasets/datasets-iris)
         (pj/lay-histogram
          :sepal-length
          {:normalize :density, :alpha 0.5})))))))
   v15_l70)))


(def
 v18_l98
 (->
  {:x (mapcat (fn [i] (repeat (long (Math/pow 2 i)) i)) (range 10))}
  (pj/lay-histogram {:bins 10})
  (pj/scale :y :log)
  (pj/options {:title "Log Y on Histogram"})))


(deftest
 t19_l103
 (is
  ((fn
    [v]
    (let
     [panel (-> v pj/plan :panels first) [lo hi] (:y-domain panel)]
     (and
      (= :log (:type (:y-scale panel)))
      (pos? lo)
      (< lo 1.0)
      (< 500.0 hi 2000.0))))
   v18_l98)))


(def
 v21_l117
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t22_l120
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      curve-xs
      (mapcat :xs (-> v pj/plan :panels first :layers first :groups))]
     (and
      (= 1 (:panels s))
      (= 1 (:polygons s))
      (= [4.12 8.08] (-> v pj/plan :panels first :x-domain))
      (= [4.3 7.9] [(apply min curve-xs) (apply max curve-xs)]))))
   v21_l117)))


(def
 v24_l132
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t25_l135
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      per-group
      (mapv
       (fn [g] [(apply min (:xs g)) (apply max (:xs g))])
       (-> v pj/plan :panels first :layers first :groups))]
     (and
      (= 1 (:panels s))
      (= 3 (:polygons s))
      (= [4.12 8.08] (-> v pj/plan :panels first :x-domain))
      (= [[4.3 7.9] [4.3 7.9] [4.3 7.9]] per-group))))
   v24_l132)))


(def
 v27_l150
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species, :trim true})))


(deftest
 t28_l153
 (is
  ((fn
    [v]
    (let
     [per-group
      (mapv
       (fn [g] [(apply min (:xs g)) (apply max (:xs g))])
       (-> v pj/plan :panels first :layers first :groups))]
     (and
      (= [[4.3 5.8] [4.9 7.0] [4.9 7.9]] per-group)
      (= [4.12 8.08] (-> v pj/plan :panels first :x-domain)))))
   v27_l150)))


(def
 v30_l163
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:bandwidth 0.3})))


(deftest
 t31_l166
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v30_l163)))


(def
 v33_l179
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density
   :sepal-length
   {:color "lightblue", :stroke "black", :stroke-width 2})))


(deftest
 t35_l185
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 1 (:polygons s))
      (= 1 (:lines s))
      (contains? (:colors s) "rgb(173,216,230)")
      (contains? (:colors s) "rgb(0,0,0)"))))
   v33_l179)))


(def
 v37_l206
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length)
  pj/lay-rug))


(deftest
 t38_l210
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      domain
      (fn*
       [p1__81573#]
       (-> p1__81573# pj/plan :panels first :x-domain))]
     (and
      (= 1 (:panels s))
      (= 1 (:polygons s))
      (= 150 (:lines s))
      (= [4.12 8.08] (domain v))
      (=
       (domain v)
       (domain
        (-> (rdatasets/datasets-iris) (pj/lay-rug :sepal-length))))
      (let
       [curve-xs
        (mapcat
         :xs
         (-> v pj/plan :panels first :layers first :groups))]
       (= [4.3 7.9] [(apply min curve-xs) (apply max curve-xs)])))))
   v37_l206)))


(def
 v40_l229
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter true})))


(deftest
 t41_l232
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v40_l229)))


(def
 v43_l238
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter 10, :alpha 0.5})))


(deftest
 t44_l241
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v43_l238)))


(def
 v46_l249
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t47_l252
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v46_l249)))


(deftest
 t49_l262
 (is
  ((fn
    [_]
    (let
     [plan
      (->
       (rdatasets/datasets-iris)
       (pj/lay-boxplot :species :sepal-width)
       pj/plan)
      box-layer
      (first
       (filter
        (fn* [p1__81574#] (= :boxplot (:mark p1__81574#)))
        (:layers (first (:panels plan)))))
      results
      (mapv
       (fn
        [{:keys [q1 q3 whisker-lo whisker-hi outliers]}]
        (let
         [iqr
          (- q3 q1)
          lo-fence
          (- q1 (* 1.5 iqr))
          hi-fence
          (+ q3 (* 1.5 iqr))]
         {:whisker-lo-in-fence (>= whisker-lo lo-fence),
          :whisker-hi-in-fence (<= whisker-hi hi-fence),
          :outliers-outside-fence
          (every?
           (fn [o] (or (< o lo-fence) (> o hi-fence)))
           outliers)}))
       (:boxes box-layer))]
     (and
      (= 3 (count results))
      (every?
       (fn
        [r]
        (and
         (:whisker-lo-in-fence r)
         (:whisker-hi-in-fence r)
         (:outliers-outside-fence r)))
       results))))
   v46_l249)))


(def
 v51_l289
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-boxplot :day :total-bill {:color :smoker})))


(deftest
 t53_l295
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/reshape2-tips)
        (pj/lay-boxplot :day :total-bill {:color :smoker})))
      box-layer
      (first
       (filter
        (fn* [p1__81575#] (= :boxplot (:mark p1__81575#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (pos? (:lines s))
      (= 2 (count (:color-categories box-layer))))))
   v51_l289)))


(def
 v55_l311
 (->
  (rdatasets/datasets-iris)
  (pj/lay-boxplot :species :sepal-width)
  (pj/coord :flip)))


(deftest
 t56_l315
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v55_l311)))


(def
 v58_l326
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t59_l329
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      bodies
      (-> v pj/plan :panels first :layers first :violins)
      col
      (:total-bill (rdatasets/reshape2-tips))
      lo
      (apply min col)
      hi
      (apply max col)]
     (and
      (= 1 (:panels s))
      (= 4 (:polygons s))
      (every?
       (fn
        [b]
        (and (>= (apply min (:ys b)) lo) (<= (apply max (:ys b)) hi)))
       bodies))))
   v58_l326)))


(def
 v61_l350
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-violin :day :total-bill {:color :smoker})))


(deftest
 t63_l356
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/reshape2-tips)
        (pj/lay-violin :day :total-bill {:color :smoker})))
      viol-layer
      (first
       (filter
        (fn* [p1__81576#] (= :violin (:mark p1__81576#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (= 2 (count (:color-categories viol-layer))))))
   v61_l350)))


(def
 v65_l369
 (->
  (rdatasets/datasets-iris)
  (pj/lay-violin :species :petal-length)
  (pj/coord :flip)))


(deftest
 t66_l373
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v65_l369)))


(def
 v68_l383
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t69_l386
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v68_l383)))


(def
 v71_l395
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length {:color :species})))


(deftest
 t72_l398
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v71_l395)))


(def
 v74_l409
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]))


(deftest
 t75_l411
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v74_l409)))


(def
 v77_l418
 (pj/lay-density
  (rdatasets/datasets-iris)
  [:sepal-length :sepal-width :petal-length]
  {:color :species}))


(deftest
 t78_l420
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v77_l418)))
