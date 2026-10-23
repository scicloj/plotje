(ns
 plotje-book.composition-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l33
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]))


(deftest
 t4_l37
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v3_l33)))


(def
 v6_l44
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 1}))


(deftest
 t7_l49
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v6_l44)))


(def
 v9_l55
 (pj/arrange
  [[(->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width {:color :species}))
    nil]
   [(->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :sepal-width {:color :species}))
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :petal-length :petal-width {:color :species}))]]))


(deftest
 t10_l61
 (is
  ((fn
    [v]
    (let
     [layout (-> v pj/plan :chrome :layout)]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (= #{[0 0] [1 0] [1 1] [0 1]} (set (keys layout))))))
   v9_l55)))


(def
 v12_l72
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [[:sepal-length :sepal-width] [:sepal-length :petal-length]])
  pj/lay-point))


(deftest
 t13_l77
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 300 (:points s))
      (= 1 (count (filter #{"sepal length"} (:texts s)))))))
   v12_l72)))


(def
 v15_l102
 (def
  weighted
  (pj/pose
   {:layout {:direction :horizontal, :weights [2 1]},
    :poses
    [{:mapping {:x :sepal-length, :y :sepal-width},
      :layers [{:layer-type :point}]}
     {:mapping {:x :petal-length, :y :petal-width},
      :layers [{:layer-type :point}]}],
    :data (rdatasets/datasets-iris)})))


(def v17_l114 weighted)


(deftest
 t18_l116
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      right-edges
      (mapv
       (fn [{{[x _ w _] :panel-box} :frames}] (+ x w))
       (:panels (pj/frames v)))]
     (and
      (= 2 (:panels s))
      (= 300 (:points s))
      (= [400.0 600.0] right-edges))))
   v17_l114)))


(def v20_l130 (kind/pprint weighted))


(deftest
 t21_l132
 (is
  ((fn
    [pose]
    (and
     (= [2 1] (get-in pose [:layout :weights]))
     (= 2 (count (:poses pose)))))
   v20_l130)))


(def
 v23_l156
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11193#] (= "setosa" (:species p1__11193#))))
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11194#] (= "virginica" (:species p1__11194#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t24_l165
 (is
  ((fn
    [v]
    (let
     [domains
      (fn
       [pose axis]
       (mapv
        (fn* [p1__11195#] (-> p1__11195# :plan :panels first axis))
        (:sub-plots (pj/plan pose))))]
     (and
      (apply = (domains v :x-domain))
      (apply not= (domains v :y-domain))
      (apply
       not=
       (domains (update v :opts dissoc :share-scales) :x-domain)))))
   v23_l156)))


(def
 v26_l201
 (def
  north-shop
  {:drink ["tea" "tea" "coffee" "coffee"],
   :half ["first" "second" "first" "second"],
   :sold [30 45 20 25]}))


(def
 v27_l206
 (def
  south-shop
  {:drink ["tea" "tea" "cocoa" "cocoa"],
   :half ["first" "second" "first" "second"],
   :sold [60 70 10 15]}))


(def
 v28_l211
 (pj/arrange
  [(->
    north-shop
    (pj/lay-bar :drink :sold {:color :half, :position :stack}))
   (->
    south-shop
    (pj/lay-bar :drink :sold {:color :half, :position :stack}))]
  {:share-scales #{:y}}))


(deftest
 t29_l216
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__11196#] (-> p1__11196# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))
      tallest-stack
      (+ 60 70)
      largest-value
      (reduce max (concat (:sold north-shop) (:sold south-shop)))]
     (and
      (apply = domains)
      (< largest-value tallest-stack (second (first domains))))))
   v28_l211)))


(def v31_l228 (def limit 8.5))


(def
 v32_l230
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11197#] (= "setosa" (:species p1__11197#))))
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-v {:x-intercept limit, :color "firebrick"}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11198#] (= "virginica" (:species p1__11198#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t33_l240
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__11199#] (:panels (:plan p1__11199#)))
       (:sub-plots (pj/plan v)))
      domains
      (mapv
       (fn* [p1__11200#] (mapv double (:x-domain p1__11200#)))
       panels)]
     (and
      (= 2 (count domains))
      (apply = domains)
      (< limit (second (first domains))))))
   v32_l230)))


(def
 v35_l256
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11201#] (= "setosa" (:species p1__11201#))))
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11202#] (= "virginica" (:species p1__11202#))))
    (pj/lay-point
     {:x {:column :sepal-length, :scale {:domain [0 10]}},
      :y :sepal-width}))]
  {:share-scales #{:x}}))


(deftest
 t36_l266
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__11203#] (:panels (:plan p1__11203#)))
       (:sub-plots (pj/plan v)))]
     (=
      [[0.0 10.0] [0.0 10.0]]
      (mapv
       (fn* [p1__11204#] (mapv double (:x-domain p1__11204#)))
       panels))))
   v35_l256)))


(def
 v38_l286
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t39_l292
 (is
  ((fn
    [v]
    (let
     [doms
      (mapv
       (fn* [p1__11205#] (-> p1__11205# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (count doms))
      (not= (first doms) (second doms))
      (re-find #"no two cells share :y" (with-out-str (pj/plan v))))))
   v38_l286)))


(def
 v41_l316
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})
  (pj/options {:share-scales {:x :same-column, :y [[:hwy :cty]]}})))


(deftest
 t42_l323
 (is
  ((fn
    [v]
    (let
     [doms
      (fn
       [pose]
       (mapv
        (fn*
         [p1__11206#]
         (-> p1__11206# :plan :panels first :y-domain))
        (:sub-plots (pj/plan pose))))
      [lo hi]
      (first (doms v))
      all
      (->
       (rdatasets/ggplot2-mpg)
       (pj/pose {:x :displ})
       pj/lay-point
       (pj/facet {:y [:hwy :cty]})
       (pj/options {:share-scales {:x :same-column, :y :all}}))]
     (and
      (apply = (doms v))
      (<= lo (reduce min (:cty (rdatasets/ggplot2-mpg))))
      (>= hi (reduce max (:hwy (rdatasets/ggplot2-mpg))))
      (= (doms v) (doms all)))))
   v41_l316)))


(def
 v44_l343
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :year})
  pj/lay-point
  (pj/facet {:y [:hwy :cty :displ]})
  (pj/options {:share-scales {:x :same-column, :y [[:hwy :cty]]}})))


(deftest
 t45_l350
 (is
  ((fn
    [v]
    (let
     [[hwy cty displ]
      (mapv
       (fn* [p1__11207#] (-> p1__11207# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and (= hwy cty) (not= hwy displ))))
   v44_l343)))


(def
 v47_l360
 (pj/arrange
  [(->
    {:day [1 2 3], :indoor [19.0 20.5 21.0]}
    (pj/lay-line :day :indoor))
   (->
    {:day [1 2 3], :outdoor [4.0 9.5 14.0]}
    (pj/lay-line :day :outdoor))]
  {:share-scales {:y [[:indoor :outdoor]]}}))


(deftest
 t48_l367
 (is
  ((fn
    [v]
    (let
     [doms
      (mapv
       (fn* [p1__11208#] (-> p1__11208# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))
      [lo hi]
      (first doms)]
     (and (apply = doms) (<= lo 4.0) (>= hi 21.0))))
   v47_l360)))


(def
 v50_l379
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/pose {:x :displ})
   pj/lay-point
   (pj/facet {:y [:hwy :drv]})
   (pj/options {:share-scales {:y :all}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l388
 (is
  ((fn
    [msg]
    (re-find
     #"cannot share :y between these cells: columns :hwy, :drv.*differ in column type"
     msg))
   v50_l379)))


(def
 v53_l400
 (def north-totals {:drink ["tea" "coffee"], :sold [75 45]}))


(def
 v54_l403
 (def south-totals {:drink ["tea" "cocoa"], :sold [130 25]}))


(def
 v55_l406
 (pj/arrange
  [(-> north-totals (pj/lay-bar :drink :sold))
   (-> south-totals (pj/lay-bar :drink :sold))]
  {:share-scales #{:y :x}}))


(deftest
 t56_l411
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__11209#] (-> p1__11209# :plan :panels first :x-domain))
       (:sub-plots (pj/plan v)))]
     (= [["tea" "coffee" "cocoa"] ["tea" "coffee" "cocoa"]] domains)))
   v55_l406)))


(def
 v58_l429
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11210#] (= "setosa" (:species p1__11210#))))
    (pj/lay-histogram :sepal-length))
   (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))]
  {:share-scales #{:y}}))


(deftest
 t59_l437
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__11211#] (-> p1__11211# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (apply = domains)))
   v58_l429)))


(def
 v61_l446
 (->
  (rdatasets/ggplot2-mpg)
  pj/pose
  pj/lay-histogram
  (pj/facet {:x [:hwy :cty]})
  (pj/options {:share-scales {:y [[:hwy :cty]]}})))


(deftest
 t62_l452
 (is
  ((fn
    [v]
    (let
     [count-axes
      (fn
       [pose]
       (mapv
        (fn*
         [p1__11212#]
         (-> p1__11212# :plan :panels first :y-domain))
        (:sub-plots (pj/plan pose))))]
     (and
      (apply = (count-axes v))
      (apply
       not=
       (count-axes
        (->
         (rdatasets/ggplot2-mpg)
         pj/pose
         pj/lay-histogram
         (pj/facet {:x [:hwy :cty]})))))))
   v61_l446)))


(def
 v64_l469
 (pj/arrange
  [(-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :hwy))
   (-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :cty))
   (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy))]
  {:share-scales {:y :all}}))


(deftest
 t65_l475
 (is
  ((fn
    [v]
    (let
     [[hwy-count cty-count hwy-values]
      (mapv
       (fn* [p1__11213#] (-> p1__11213# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and (= hwy-count cty-count) (not= hwy-count hwy-values))))
   v64_l469)))


(def
 v67_l497
 (pj/arrange
  [(-> north-totals (pj/lay-bar :drink :sold) (pj/coord :flip))
   (-> south-totals (pj/lay-bar :drink :sold) (pj/coord :flip))]
  {:share-scales #{:x}}))


(deftest
 t68_l502
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__11214#] (-> p1__11214# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (apply = (map :x-domain panels))
      (number? (first (:x-domain (first panels))))
      (apply not= (map :y-domain panels)))))
   v67_l497)))


(def
 v70_l519
 (def
  readings
  {:t [1 2 3 4 5],
   :rate [1.0 2.0 3.0 2.0 4.0],
   :total [1200000.0 2400000.0 1800000.0 3100000.0 2600000.0]}))


(def
 v71_l524
 (pj/arrange
  [(-> readings (pj/lay-line :t :rate))
   (-> readings (pj/lay-line :t :total))]
  {:cols 1, :share-scales #{:x}, :align-panels true}))


(deftest
 t72_l529
 (is
  ((fn
    [v]
    (let
     [pads-of
      (fn
       [pose]
       (mapv
        (fn*
         [p1__11215#]
         (get-in p1__11215# [:plan :layout :y-label-pad]))
        (:sub-plots (pj/plan pose))))
      plain
      (pads-of
       (pj/arrange
        [(-> readings (pj/lay-line :t :rate))
         (-> readings (pj/lay-line :t :total))]
        {:cols 1, :share-scales #{:x}}))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (apply == (pads-of v))
      (not (apply == plain)))))
   v71_l524)))


(def
 v74_l559
 (def
  marginal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/marginal :top))))


(def v75_l564 marginal)


(deftest
 t76_l566
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal)))
      panels
      (mapv (fn* [p1__11216#] (-> p1__11216# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)
      [d-y s-y]
      (mapv :y-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (pos? (:polygons s))
      (= d-x s-x)
      (not= d-y s-y)
      (= [] (:values (:x-ticks (first panels))))
      (nil? (:x-label (first plans)))
      (apply
       ==
       (map
        (fn* [p1__11217#] (get-in p1__11217# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__11218#] (get-in p1__11218# [:layout :legend-w]))
        plans)))))
   v75_l564)))


(def
 v78_l604
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.35})))


(deftest
 t79_l608
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v78_l604)))


(def
 v81_l620
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/marginal :right)))


(deftest
 t82_l624
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan v)))
      panels
      (mapv (fn* [p1__11219#] (-> p1__11219# :panels first)) plans)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels)))
      (= [] (:values (:y-ticks (second panels))))
      (nil? (:y-label (second plans)))
      (apply
       ==
       (map
        (fn* [p1__11220#] (get-in p1__11220# [:layout :x-label-pad]))
        plans)))))
   v81_l620)))


(def
 v84_l662
 (def
  marginal-by-hand
  (pj/pose
   {:share-scales #{:x},
    :layout {:direction :vertical, :weights [1 3]},
    :poses
    [{:mapping {:x :sepal-length},
      :opts {:suppress-x-ticks true, :suppress-x-label true},
      :layers [{:layer-type :density}]}
     {:mapping {:x :sepal-length, :y :sepal-width, :color :species},
      :layers [{:layer-type :point}]}],
    :data (rdatasets/datasets-iris)})))


(def v85_l673 marginal-by-hand)


(deftest
 t86_l675
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
      panels
      (mapv (fn* [p1__11221#] (-> p1__11221# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= d-x s-x)
      (=
       [0 102]
       (mapv
        (fn* [p1__11222#] (get-in p1__11222# [:layout :legend-w]))
        plans)))))
   v85_l673)))


(def v88_l704 (assoc-in marginal-by-hand [:opts :align-panels] true))


(deftest
 t89_l706
 (is
  ((fn
    [v]
    (let
     [plans (mapv :plan (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (apply
       ==
       (map
        (fn* [p1__11223#] (get-in p1__11223# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__11224#] (get-in p1__11224# [:layout :legend-w]))
        plans)))))
   v88_l704)))


(def
 v91_l731
 (def
  dashboard
  (pj/arrange
   [[(-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))
     (->
      (rdatasets/datasets-iris)
      (pj/lay-boxplot :species :sepal-width {:color :species}))]
    [(->
      (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width {:color :species}))
     (->
      (rdatasets/datasets-iris)
      (pj/lay-density :petal-length {:color :species}))]])))


(def v92_l738 dashboard)


(deftest
 t93_l740
 (is
  ((fn
    [v]
    (let
     [chrome (-> dashboard pj/plan :chrome)]
     (and
      (= 4 (:panels (pj/svg-summary v)))
      (= #{} (:shared-aesthetics chrome)))))
   v92_l738)))


(def v95_l769 (def overlay-base {:fitted [1 2 3], :residual [1 2 3]}))


(def
 v96_l773
 (def overlay-other (tc/dataset {:x [0.5 1.5 2.5], :y [1.5 2.5 3.5]})))


(def
 v97_l777
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point :x :y {:color "#e6550d", :data overlay-other})))


(deftest
 t98_l781
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:points s)))))
   v97_l777)))


(def
 v100_l797
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point
   :fitted
   :residual
   {:color "#e6550d",
    :data
    (tc/rename-columns overlay-other {:x :fitted, :y :residual})})))


(deftest
 t101_l804
 (is
  ((fn
    [v]
    (let
     [unrenamed
      (->
       overlay-base
       (pj/lay-point :fitted :residual {:color "#377eb8"})
       (pj/lay-point :x :y {:color "#e6550d", :data overlay-other}))
      titles
      (fn [pose] ((juxt :x-label :y-label) (pj/plan pose)))
      domains
      (fn
       [pose]
       ((juxt :x-domain :y-domain) (first (:panels (pj/plan pose)))))]
     (and
      (= ["fitted" "residual"] (titles v))
      (= ["fitted, x" "residual, y"] (titles unrenamed))
      (= (domains v) (domains unrenamed))
      (=
       (:points (pj/svg-summary v))
       (:points (pj/svg-summary unrenamed))))))
   v100_l797)))


(def
 v103_l822
 (pj/arrange
  [(pj/lay-point overlay-base :fitted :residual {:color "#377eb8"})
   (pj/lay-point overlay-other :x :y {:color "#e6550d"})]))


(deftest
 t104_l825
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 6 (:points s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)"}
       (disj (:colors s) "none")))))
   v103_l822)))


(def
 v106_l846
 (def
  bounded
  (->
   {:x [1 2 3 4 5], :y [10 20 15 25 18]}
   (pj/lay-point :x :y)
   (pj/lay-line {:data {:x [1 5], :y [-200 300]}})
   (pj/scale :y {:type :linear, :domain [0 30]}))))


(def v107_l852 (pj/arrange [bounded bounded] {:cols 1}))


(deftest
 t108_l854
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:clips s)))))
   v107_l852)))


(def
 v110_l914
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]))


(deftest
 t111_l920
 (is
  ((fn
    [v]
    (and
     (= #{:color} (-> v pj/plan :chrome :shared-aesthetics))
     (=
      #{}
      (->
       (pj/arrange
        [(->
          (rdatasets/datasets-iris)
          (pj/lay-histogram :sepal-length))
         (->
          (rdatasets/datasets-iris)
          (pj/lay-point
           :petal-length
           :petal-width
           {:color :species}))])
       pj/plan
       :chrome
       :shared-aesthetics))))
   v110_l914)))


(def
 v113_l936
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11225#] (not= "virginica" (:species p1__11225#))))
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__11226#] (not= "setosa" (:species p1__11226#))))
    (pj/lay-point :sepal-length :sepal-width {:color :species}))]))


(deftest
 t114_l944
 (is
  ((fn
    [v]
    (let
     [plan
      (pj/plan v)
      versicolor
      (fn
       [sp]
       (some
        (fn*
         [p1__11227#]
         (when
          (= "versicolor" (:label p1__11227#))
          (:color p1__11227#)))
        (-> sp :plan :panels first :layers first :groups)))
      [left right]
      (:sub-plots plan)]
     (and
      (=
       ["setosa" "versicolor" "virginica"]
       (mapv :label (-> plan :chrome :shared-legend :legend :entries)))
      (some? (versicolor left))
      (= (versicolor left) (versicolor right)))))
   v113_l936)))


(def
 v116_l960
 (pj/arrange
  [(pj/arrange
    [(->
      (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width))
     (->
      (rdatasets/datasets-iris)
      (pj/lay-point :petal-length :petal-width))]
    {:cols 1})
   (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))]))


(deftest
 t117_l970
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v116_l960)))
