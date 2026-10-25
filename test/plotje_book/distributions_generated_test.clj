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
 v6_l33
 (-> (rdatasets/datasets-iris) (pj/lay-histogram {:y :sepal-length})))


(deftest
 t7_l36
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       (rdatasets/datasets-iris)
       (pj/lay-histogram {:x :sepal-length})
       (pj/coord :flip)))))
   v6_l33)))


(def
 v9_l46
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})))


(deftest
 t10_l49
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v9_l46)))


(def
 v12_l58
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :petal-width)))


(deftest
 t13_l61
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v12_l58)))


(def
 v15_l68
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-histogram :total-bill)
  (pj/options
   {:title "Distribution of Total Bill", :x-label "Amount ($)"})))


(deftest
 t16_l73
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (pos? (:polygons s))
      (some
       (fn* [p1__79113#] (= "Distribution of Total Bill" p1__79113#))
       (:texts s)))))
   v15_l68)))


(def
 v18_l85
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:normalize :density, :alpha 0.5})
  pj/lay-density))


(deftest
 t19_l89
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      domain
      (fn*
       [p1__79114#]
       (-> p1__79114# pj/plan :panels first :x-domain))]
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
   v18_l85)))


(def
 v21_l113
 (->
  {:x (mapcat (fn [i] (repeat (long (Math/pow 2 i)) i)) (range 10))}
  (pj/lay-histogram {:bins 10})
  (pj/scale :y :log)
  (pj/options {:title "Log Y on Histogram"})))


(deftest
 t22_l118
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
   v21_l113)))


(def
 v24_l132
 (-> (rdatasets/datasets-iris) (pj/lay-density :sepal-length)))


(deftest
 t25_l135
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
   v24_l132)))


(def
 v27_l147
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t28_l150
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
   v27_l147)))


(def
 v30_l165
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species, :trim true})))


(deftest
 t31_l168
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
   v30_l165)))


(def
 v33_l182
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:bandwidth 0.1})))


(deftest
 t34_l185
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      peak
      (fn
       [pose]
       (apply
        max
        (:ys
         (first
          (-> pose pj/plan :panels first :layers first :groups)))))]
     (and
      (= 1 (:panels s))
      (= 1 (:polygons s))
      (>
       (peak v)
       (peak
        (->
         (rdatasets/datasets-iris)
         (pj/lay-density :sepal-length)))))))
   v33_l182)))


(def
 v36_l203
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density
   :sepal-length
   {:color "lightblue", :stroke "black", :stroke-width 2})))


(deftest
 t38_l209
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
   v36_l203)))


(def
 v40_l230
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length)
  pj/lay-rug))


(deftest
 t41_l234
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      domain
      (fn*
       [p1__79115#]
       (-> p1__79115# pj/plan :panels first :x-domain))]
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
   v40_l230)))


(def
 v43_l253
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter true})))


(deftest
 t44_l256
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v43_l253)))


(def
 v46_l262
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:jitter 10, :alpha 0.5})))


(deftest
 t47_l265
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v46_l262)))


(def
 v49_l273
 (-> (rdatasets/datasets-iris) (pj/lay-boxplot :species :sepal-width)))


(deftest
 t51_l279
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      boxes
      (->>
       (pj/plan v)
       :panels
       first
       :layers
       (filter (fn* [p1__79116#] (= :boxplot (:mark p1__79116#))))
       first
       :boxes)
      within-fences?
      (fn
       [{:keys [q1 q3 whisker-lo whisker-hi outliers]}]
       (let
        [iqr
         (- q3 q1)
         lo-fence
         (- q1 (* 1.5 iqr))
         hi-fence
         (+ q3 (* 1.5 iqr))]
        (and
         (>= whisker-lo lo-fence)
         (<= whisker-hi hi-fence)
         (every?
          (fn [o] (or (< o lo-fence) (> o hi-fence)))
          outliers))))]
     (and
      (= 1 (:panels s))
      (= 3 (:polygons s))
      (pos? (:lines s))
      (= 3 (count boxes))
      (every? within-fences? boxes))))
   v49_l273)))


(def
 v53_l303
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-boxplot :day :total-bill {:color :smoker})))


(deftest
 t55_l309
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
        (fn* [p1__79117#] (= :boxplot (:mark p1__79117#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (pos? (:lines s))
      (= 2 (count (:color-categories box-layer))))))
   v53_l303)))


(def
 v57_l325
 (->
  (rdatasets/datasets-iris)
  (pj/lay-boxplot :species :sepal-width)
  (pj/coord :flip)))


(deftest
 t58_l329
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)) (pos? (:lines s)))))
   v57_l325)))


(def
 v60_l340
 (-> (rdatasets/reshape2-tips) (pj/lay-violin :day :total-bill)))


(deftest
 t61_l343
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
   v60_l340)))


(def
 v63_l364
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-violin :day :total-bill {:color :smoker})))


(deftest
 t65_l370
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
        (fn* [p1__79118#] (= :violin (:mark p1__79118#)))
        (:layers (first (:panels plan)))))]
     (and
      (= 1 (:panels s))
      (= 8 (:polygons s))
      (= 2 (count (:color-categories viol-layer))))))
   v63_l364)))


(def
 v67_l383
 (->
  (rdatasets/datasets-iris)
  (pj/lay-violin :species :petal-length)
  (pj/coord :flip)))


(deftest
 t68_l387
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v67_l383)))


(def
 v70_l397
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length)))


(deftest
 t71_l400
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v70_l397)))


(def
 v73_l409
 (->
  (rdatasets/datasets-iris)
  (pj/lay-ridgeline :species :sepal-length {:color :species})))


(deftest
 t74_l412
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v73_l409)))


(def
 v76_l423
 (pj/lay-histogram
  (rdatasets/datasets-iris)
  {:x [:sepal-length :sepal-width :petal-length]}))


(deftest
 t77_l426
 (is
  ((fn
    [v]
    (and
     (= 1 (:panels (pj/svg-summary v)))
     (=
      ["sepal length" "sepal width" "petal length"]
      (mapv :label (:entries (:legend (pj/plan v)))))))
   v76_l423)))


(def
 v79_l434
 (->
  (rdatasets/datasets-iris)
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})
  (pj/lay-histogram)))


(deftest
 t80_l438
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v79_l434)))


(def
 v82_l445
 (->
  (rdatasets/datasets-iris)
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})
  (pj/lay-density {:color :species})))


(deftest
 t83_l449
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v82_l445)))


(def
 v85_l460
 (pj/lay-boxplot
  (rdatasets/datasets-iris)
  {:x :species, :y [:sepal-length :sepal-width :petal-length]}))


(deftest
 t86_l464
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 9 (:polygons s)))))
   v85_l460)))
