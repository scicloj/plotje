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
 v12_l75
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-length}])
  pj/lay-point))


(deftest
 t13_l80
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 300 (:points s))
      (= 1 (count (filter #{"sepal length"} (:texts s)))))))
   v12_l75)))


(def
 v15_l94
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-length}])
  (pj/arrange [{:x :sepal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t16_l100
 (is
  ((fn
    [v]
    (and
     (= 3 (:panels (pj/svg-summary v)))
     (=
      (pj/svg-summary v)
      (pj/svg-summary
       (->
        (rdatasets/datasets-iris)
        (pj/arrange
         [{:x :sepal-length, :y :sepal-width}
          {:x :sepal-length, :y :petal-length}
          {:x :sepal-length, :y :petal-width}])
        pj/lay-point)))))
   v15_l94)))


(def
 v18_l133
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


(def v20_l145 weighted)


(deftest
 t21_l147
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
   v20_l145)))


(def v23_l161 (kind/pprint weighted))


(deftest
 t24_l163
 (is
  ((fn
    [pose]
    (and
     (= [2 1] (get-in pose [:layout :weights]))
     (= 2 (count (:poses pose)))))
   v23_l161)))


(def
 v26_l184
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} :hwy] {:col-weights [2 1]})))


(deftest
 t27_l187
 (is
  ((fn
    [v]
    (let
     [[a b]
      (mapv
       (fn* [p1__78629#] (get-in p1__78629# [:plan :panel-width]))
       (:sub-plots (pj/plan v)))]
     (and
      (= [0.0 0.0 600.0 400.0] (:canvas (pj/frames v)))
      (<= (Math/abs (- a (* 2 b))) 2.0))))
   v26_l184)))


(def
 v29_l196
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [{:x :displ, :y :hwy} :hwy]
   {:panel-width [300 150], :panel-height 200})))


(deftest
 t30_l199
 (is
  ((fn
    [v]
    (every?
     (fn [[got want]] (<= (Math/abs (- got want)) 1.0))
     (map
      vector
      (mapcat
       (fn
        [s]
        [(get-in s [:plan :panel-width])
         (get-in s [:plan :panel-height])])
       (:sub-plots (pj/plan v)))
      [300 200 150 200])))
   v29_l196)))


(def
 v32_l226
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78630#] (= "setosa" (:species p1__78630#))))
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78631#] (= "virginica" (:species p1__78631#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t33_l235
 (is
  ((fn
    [v]
    (let
     [domains
      (fn
       [pose axis]
       (mapv
        (fn* [p1__78632#] (-> p1__78632# :plan :panels first axis))
        (:sub-plots (pj/plan pose))))]
     (and
      (apply = (domains v :x-domain))
      (apply not= (domains v :y-domain))
      (apply
       not=
       (domains (update v :opts dissoc :share-scales) :x-domain)))))
   v32_l226)))


(def
 v35_l272
 (def
  north-shop
  {:drink ["tea" "tea" "coffee" "coffee"],
   :half ["first" "second" "first" "second"],
   :sold [30 45 20 25]}))


(def
 v36_l277
 (def
  south-shop
  {:drink ["tea" "tea" "cocoa" "cocoa"],
   :half ["first" "second" "first" "second"],
   :sold [60 70 10 15]}))


(def
 v37_l282
 (pj/arrange
  [(->
    north-shop
    (pj/lay-bar :drink :sold {:color :half, :position :stack}))
   (->
    south-shop
    (pj/lay-bar :drink :sold {:color :half, :position :stack}))]
  {:share-scales #{:y}}))


(deftest
 t38_l287
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__78633#] (-> p1__78633# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))
      tallest-stack
      (+ 60 70)
      largest-value
      (reduce max (concat (:sold north-shop) (:sold south-shop)))]
     (and
      (apply = domains)
      (< largest-value tallest-stack (second (first domains))))))
   v37_l282)))


(def v40_l299 (def limit 8.5))


(def
 v41_l301
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78634#] (= "setosa" (:species p1__78634#))))
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-v {:x-intercept limit, :color "firebrick"}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78635#] (= "virginica" (:species p1__78635#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t42_l311
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__78636#] (:panels (:plan p1__78636#)))
       (:sub-plots (pj/plan v)))
      domains
      (mapv
       (fn* [p1__78637#] (mapv double (:x-domain p1__78637#)))
       panels)]
     (and
      (= 2 (count domains))
      (apply = domains)
      (< limit (second (first domains))))))
   v41_l301)))


(def
 v44_l327
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78638#] (= "setosa" (:species p1__78638#))))
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78639#] (= "virginica" (:species p1__78639#))))
    (pj/lay-point
     {:x {:column :sepal-length, :scale {:domain [0 10]}},
      :y :sepal-width}))]
  {:share-scales #{:x}}))


(deftest
 t45_l337
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__78640#] (:panels (:plan p1__78640#)))
       (:sub-plots (pj/plan v)))]
     (=
      [[0.0 10.0] [0.0 10.0]]
      (mapv
       (fn* [p1__78641#] (mapv double (:x-domain p1__78641#)))
       panels))))
   v44_l327)))


(def
 v47_l359
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t48_l365
 (is
  ((fn
    [v]
    (let
     [doms
      (mapv
       (fn* [p1__78642#] (-> p1__78642# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (count doms))
      (not= (first doms) (second doms))
      (re-find #"no two cells share :y" (with-out-str (pj/plan v))))))
   v47_l359)))


(def
 v50_l389
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})
  (pj/options {:share-scales {:x :same-column, :y [[:hwy :cty]]}})))


(deftest
 t51_l396
 (is
  ((fn
    [v]
    (let
     [doms
      (fn
       [pose]
       (mapv
        (fn*
         [p1__78643#]
         (-> p1__78643# :plan :panels first :y-domain))
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
   v50_l389)))


(def
 v53_l416
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :year})
  pj/lay-point
  (pj/facet {:y [:hwy :cty :displ]})
  (pj/options {:share-scales {:x :same-column, :y [[:hwy :cty]]}})))


(deftest
 t54_l423
 (is
  ((fn
    [v]
    (let
     [[hwy cty displ]
      (mapv
       (fn* [p1__78644#] (-> p1__78644# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and (= hwy cty) (not= hwy displ))))
   v53_l416)))


(def
 v56_l433
 (pj/arrange
  [(->
    {:day [1 2 3], :indoor [19.0 20.5 21.0]}
    (pj/lay-line :day :indoor))
   (->
    {:day [1 2 3], :outdoor [4.0 9.5 14.0]}
    (pj/lay-line :day :outdoor))]
  {:share-scales {:y [[:indoor :outdoor]]}}))


(deftest
 t57_l440
 (is
  ((fn
    [v]
    (let
     [doms
      (mapv
       (fn* [p1__78645#] (-> p1__78645# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))
      [lo hi]
      (first doms)]
     (and (apply = doms) (<= lo 4.0) (>= hi 21.0))))
   v56_l433)))


(def
 v59_l452
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
 t60_l461
 (is
  ((fn
    [msg]
    (re-find
     #"cannot share :y between these cells: columns :hwy, :drv.*differ in column type"
     msg))
   v59_l452)))


(def
 v62_l473
 (def north-totals {:drink ["tea" "coffee"], :sold [75 45]}))


(def
 v63_l476
 (def south-totals {:drink ["tea" "cocoa"], :sold [130 25]}))


(def
 v64_l479
 (pj/arrange
  [(-> north-totals (pj/lay-bar :drink :sold))
   (-> south-totals (pj/lay-bar :drink :sold))]
  {:share-scales #{:y :x}}))


(deftest
 t65_l484
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__78646#] (-> p1__78646# :plan :panels first :x-domain))
       (:sub-plots (pj/plan v)))]
     (= [["tea" "coffee" "cocoa"] ["tea" "coffee" "cocoa"]] domains)))
   v64_l479)))


(def
 v67_l502
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78647#] (= "setosa" (:species p1__78647#))))
    (pj/lay-histogram :sepal-length))
   (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))]
  {:share-scales #{:y}}))


(deftest
 t68_l510
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__78648#] (-> p1__78648# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (apply = domains)))
   v67_l502)))


(def
 v70_l519
 (->
  (rdatasets/ggplot2-mpg)
  pj/pose
  pj/lay-histogram
  (pj/facet {:x [:hwy :cty]})
  (pj/options {:share-scales {:y [[:hwy :cty]]}})))


(deftest
 t71_l525
 (is
  ((fn
    [v]
    (let
     [count-axes
      (fn
       [pose]
       (mapv
        (fn*
         [p1__78649#]
         (-> p1__78649# :plan :panels first :y-domain))
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
   v70_l519)))


(def
 v73_l542
 (pj/arrange
  [(-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :hwy))
   (-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :cty))
   (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy))]
  {:share-scales {:y :all}}))


(deftest
 t74_l548
 (is
  ((fn
    [v]
    (let
     [[hwy-count cty-count hwy-values]
      (mapv
       (fn* [p1__78650#] (-> p1__78650# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and (= hwy-count cty-count) (not= hwy-count hwy-values))))
   v73_l542)))


(def
 v76_l570
 (pj/arrange
  [(-> north-totals (pj/lay-bar :drink :sold) (pj/coord :flip))
   (-> south-totals (pj/lay-bar :drink :sold) (pj/coord :flip))]
  {:share-scales #{:x}}))


(deftest
 t77_l575
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__78651#] (-> p1__78651# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (apply = (map :x-domain panels))
      (number? (first (:x-domain (first panels))))
      (apply not= (map :y-domain panels)))))
   v76_l570)))


(def
 v79_l592
 (def
  readings
  {:t [1 2 3 4 5],
   :rate [1.0 2.0 3.0 2.0 4.0],
   :total [1200000.0 2400000.0 1800000.0 3100000.0 2600000.0]}))


(def
 v80_l597
 (pj/arrange
  [(-> readings (pj/lay-line :t :rate))
   (-> readings (pj/lay-line :t :total))]
  {:cols 1, :share-scales #{:x}, :align-panels true}))


(deftest
 t81_l602
 (is
  ((fn
    [v]
    (let
     [pads-of
      (fn
       [pose]
       (mapv
        (fn*
         [p1__78652#]
         (get-in p1__78652# [:plan :layout :y-label-pad]))
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
   v80_l597)))


(def
 v83_l634
 (def drv-facet (-> (pj/pose nil :displ :hwy) (pj/facet :drv))))


(def
 v84_l638
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [[drv-facet] [{:x :displ, :y :cty} {:x :displ, :y :hwy}]]
   {:share-scales {:x :all}})
  pj/lay-point))


(deftest
 t85_l644
 (is
  ((fn
    [v]
    (let
     [facet-y
      (->>
       (pj/plan v)
       :sub-plots
       first
       :plan
       :panels
       (mapv :y-domain))]
     (and (= 3 (count facet-y)) (= 3 (count (distinct facet-y))))))
   v84_l638)))


(def
 v87_l658
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [[(pj/options drv-facet {:share-scales #{:y :x}})]
    [{:x :displ, :y :cty} {:x :displ, :y :hwy}]]
   {:share-scales {:x :all}})
  pj/lay-point))


(deftest
 t88_l664
 (is
  ((fn
    [v]
    (let
     [facet-y
      (->>
       (pj/plan v)
       :sub-plots
       first
       :plan
       :panels
       (mapv :y-domain))]
     (and (= 3 (count facet-y)) (= 1 (count (distinct facet-y))))))
   v87_l658)))


(def
 v90_l683
 (def
  marginal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/marginal :top))))


(def v91_l688 marginal)


(deftest
 t92_l690
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal)))
      panels
      (mapv (fn* [p1__78653#] (-> p1__78653# :panels first)) plans)
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
        (fn* [p1__78654#] (get-in p1__78654# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__78655#] (get-in p1__78655# [:layout :legend-w]))
        plans)))))
   v91_l688)))


(def
 v94_l728
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.35})))


(deftest
 t95_l732
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v94_l728)))


(def
 v97_l744
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/marginal :right)))


(deftest
 t98_l748
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan v)))
      panels
      (mapv (fn* [p1__78656#] (-> p1__78656# :panels first)) plans)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels)))
      (= [] (:values (:y-ticks (second panels))))
      (nil? (:y-label (second plans)))
      (apply
       ==
       (map
        (fn* [p1__78657#] (get-in p1__78657# [:layout :x-label-pad]))
        plans)))))
   v97_l744)))


(def
 v100_l787
 (pj/matrix
  (rdatasets/ggplot2-mpg)
  [:displ nil]
  [nil :hwy]
  {:col-weights [3 1], :row-weights [1 3]}))


(deftest
 t101_l790
 (is
  ((fn
    [v]
    (let
     [subs
      (:sub-plots (pj/plan v))
      w
      (into
       {}
       (map
        (juxt
         :path
         (fn* [p1__78658#] (get-in p1__78658# [:plan :panel-width]))))
       subs)
      h
      (into
       {}
       (map
        (juxt
         :path
         (fn* [p1__78659#] (get-in p1__78659# [:plan :panel-height]))))
       subs)]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (<= (Math/abs (- (w [1 0]) (* 3 (w [1 1])))) 3.0)
      (<= (Math/abs (- (h [1 0]) (* 3 (h [0 0])))) 3.0))))
   v100_l787)))


(def
 v103_l804
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


(def v104_l815 marginal-by-hand)


(deftest
 t105_l817
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
      panels
      (mapv (fn* [p1__78660#] (-> p1__78660# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= d-x s-x)
      (=
       [0 102]
       (mapv
        (fn* [p1__78661#] (get-in p1__78661# [:layout :legend-w]))
        plans)))))
   v104_l815)))


(def v107_l846 (assoc-in marginal-by-hand [:opts :align-panels] true))


(deftest
 t108_l848
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
        (fn* [p1__78662#] (get-in p1__78662# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__78663#] (get-in p1__78663# [:layout :legend-w]))
        plans)))))
   v107_l846)))


(def
 v110_l873
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


(def v111_l880 dashboard)


(deftest
 t112_l882
 (is
  ((fn
    [v]
    (let
     [chrome (-> dashboard pj/plan :chrome)]
     (and
      (= 4 (:panels (pj/svg-summary v)))
      (= #{} (:shared-aesthetics chrome)))))
   v111_l880)))


(def v114_l911 (def overlay-base {:fitted [1 2 3], :residual [1 2 3]}))


(def
 v115_l915
 (def overlay-other (tc/dataset {:x [0.5 1.5 2.5], :y [1.5 2.5 3.5]})))


(def
 v116_l919
 (->
  overlay-base
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point :x :y {:color "#e6550d", :data overlay-other})))


(deftest
 t117_l923
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:points s)))))
   v116_l919)))


(def
 v119_l939
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
 t120_l946
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
   v119_l939)))


(def
 v122_l964
 (pj/arrange
  [(pj/lay-point overlay-base :fitted :residual {:color "#377eb8"})
   (pj/lay-point overlay-other :x :y {:color "#e6550d"})]))


(deftest
 t123_l967
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
   v122_l964)))


(def
 v125_l988
 (def
  bounded
  (->
   {:x [1 2 3 4 5], :y [10 20 15 25 18]}
   (pj/lay-point :x :y)
   (pj/lay-line {:data {:x [1 5], :y [-200 300]}})
   (pj/scale :y {:type :linear, :domain [0 30]}))))


(def v126_l994 (pj/arrange [bounded bounded] {:cols 1}))


(deftest
 t127_l996
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:clips s)))))
   v126_l994)))


(def
 v129_l1068
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78664#] (not= "virginica" (:species p1__78664#))))
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__78665#] (not= "setosa" (:species p1__78665#))))
    (pj/lay-point :sepal-length :sepal-width {:color :species}))]))


(deftest
 t130_l1076
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
         [p1__78666#]
         (when
          (= "versicolor" (:label p1__78666#))
          (:color p1__78666#)))
        (-> sp :plan :panels first :layers first :groups)))
      [left right]
      (:sub-plots plan)]
     (and
      (= #{:color} (-> plan :chrome :shared-aesthetics))
      (=
       ["setosa" "versicolor" "virginica"]
       (mapv :label (-> plan :chrome :shared-legend :legend :entries)))
      (some? (versicolor left))
      (= (versicolor left) (versicolor right))
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
        :shared-aesthetics)))))
   v129_l1068)))


(def
 v132_l1100
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
 t133_l1110
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v132_l1100)))
