(ns
 plotje-book.composition-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l40
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]))


(deftest
 t4_l44
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v3_l40)))


(def
 v6_l51
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :petal-length :petal-width {:color :species}))]
  {:cols 1}))


(deftest
 t7_l56
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v6_l51)))


(def
 v9_l62
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
 t10_l68
 (is
  ((fn
    [v]
    (let
     [layout (-> v pj/plan :chrome :layout)]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (= #{[0 0] [1 0] [1 1] [0 1]} (set (keys layout))))))
   v9_l62)))


(def
 v12_l85
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-length}])
  pj/lay-point))


(deftest
 t13_l90
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 300 (:points s))
      (= 1 (count (filter #{"sepal length"} (:texts s)))))))
   v12_l85)))


(def
 v15_l107
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-length}])
  pj/lay-point
  (pj/lay-smooth :sepal-length :sepal-width)))


(deftest
 t16_l113
 (is
  ((fn
    [v]
    (=
     [[:point :line] [:point]]
     (mapv
      (fn*
       [p1__80635#]
       (mapv :mark (-> p1__80635# :plan :panels first :layers)))
      (:sub-plots (pj/plan v)))))
   v15_l107)))


(def
 v18_l132
 (->
  (rdatasets/datasets-iris)
  (pj/arrange
   [{:x :sepal-length, :y :sepal-width}
    {:x :sepal-length, :y :petal-length}])
  (pj/arrange [{:x :sepal-length, :y :petal-width}])
  pj/lay-point))


(deftest
 t19_l138
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
   v18_l132)))


(def
 v21_l170
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


(def v23_l182 weighted)


(deftest
 t24_l184
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
   v23_l182)))


(def v26_l198 (kind/pprint weighted))


(deftest
 t27_l200
 (is
  ((fn
    [pose]
    (and
     (= [2 1] (get-in pose [:layout :weights]))
     (= 2 (count (:poses pose)))))
   v26_l198)))


(def
 v29_l221
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} :hwy] {:col-weights [2 1]})))


(deftest
 t30_l224
 (is
  ((fn
    [v]
    (let
     [[a b]
      (mapv
       (fn* [p1__80636#] (get-in p1__80636# [:plan :panel-width]))
       (:sub-plots (pj/plan v)))]
     (and
      (= [0.0 0.0 600.0 400.0] (:canvas (pj/frames v)))
      (<= (Math/abs (- a (* 2 b))) 2.0))))
   v29_l221)))


(def
 v32_l235
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [{:x :displ, :y :hwy} :hwy]
   {:panel-width [300 150], :panel-height 200})))


(deftest
 t33_l238
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
   v32_l235)))


(def
 v35_l266
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80637#] (= "setosa" (:species p1__80637#))))
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80638#] (= "virginica" (:species p1__80638#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t36_l275
 (is
  ((fn
    [v]
    (let
     [domains
      (fn
       [pose axis]
       (mapv
        (fn* [p1__80639#] (-> p1__80639# :plan :panels first axis))
        (:sub-plots (pj/plan pose))))]
     (and
      (apply = (domains v :x-domain))
      (apply not= (domains v :y-domain))
      (apply
       not=
       (domains (update v :opts dissoc :share-scales) :x-domain)))))
   v35_l266)))


(def
 v38_l316
 (def
  north-shop
  {:drink ["tea" "tea" "coffee" "coffee"],
   :half ["first" "second" "first" "second"],
   :sold [30 45 20 25]}))


(def
 v39_l321
 (def
  south-shop
  {:drink ["tea" "tea" "cocoa" "cocoa"],
   :half ["first" "second" "first" "second"],
   :sold [60 70 10 15]}))


(def
 v40_l326
 (pj/arrange
  [(->
    north-shop
    (pj/lay-bar :drink :sold {:color :half, :position :stack}))
   (->
    south-shop
    (pj/lay-bar :drink :sold {:color :half, :position :stack}))]
  {:share-scales #{:y}}))


(deftest
 t41_l331
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__80640#] (-> p1__80640# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))
      tallest-stack
      (+ 60 70)
      largest-value
      (reduce max (concat (:sold north-shop) (:sold south-shop)))]
     (and
      (apply = domains)
      (< largest-value tallest-stack (second (first domains))))))
   v40_l326)))


(def v43_l347 (def limit 8.5))


(def
 v44_l349
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80641#] (= "setosa" (:species p1__80641#))))
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-v {:x-intercept limit, :color "firebrick"}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80642#] (= "virginica" (:species p1__80642#))))
    (pj/lay-point :sepal-length :sepal-width))]
  {:share-scales #{:x}}))


(deftest
 t45_l359
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__80643#] (:panels (:plan p1__80643#)))
       (:sub-plots (pj/plan v)))
      domains
      (mapv
       (fn* [p1__80644#] (mapv double (:x-domain p1__80644#)))
       panels)]
     (and
      (= 2 (count domains))
      (apply = domains)
      (< limit (second (first domains))))))
   v44_l349)))


(def
 v47_l375
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80645#] (= "setosa" (:species p1__80645#))))
    (pj/lay-point :sepal-length :sepal-width))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80646#] (= "virginica" (:species p1__80646#))))
    (pj/lay-point
     {:x {:column :sepal-length, :scale {:domain [0 10]}},
      :y :sepal-width}))]
  {:share-scales #{:x}}))


(deftest
 t48_l385
 (is
  ((fn
    [v]
    (let
     [panels
      (mapcat
       (fn* [p1__80647#] (:panels (:plan p1__80647#)))
       (:sub-plots (pj/plan v)))]
     (=
      [[0.0 10.0] [0.0 10.0]]
      (mapv
       (fn* [p1__80648#] (mapv double (:x-domain p1__80648#)))
       panels))))
   v47_l375)))


(def
 v50_l407
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t51_l413
 (is
  ((fn
    [v]
    (let
     [doms
      (mapv
       (fn* [p1__80649#] (-> p1__80649# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (count doms))
      (not= (first doms) (second doms))
      (re-find #"no two cells share :y" (with-out-str (pj/plan v))))))
   v50_l407)))


(def
 v53_l441
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})
  (pj/options {:share-scales {:x :same-column, :y [[:hwy :cty]]}})))


(deftest
 t54_l448
 (is
  ((fn
    [v]
    (let
     [doms
      (fn
       [pose]
       (mapv
        (fn*
         [p1__80650#]
         (-> p1__80650# :plan :panels first :y-domain))
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
   v53_l441)))


(def
 v56_l472
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :year})
  pj/lay-point
  (pj/facet {:y [:hwy :cty :displ]})
  (pj/options {:share-scales {:x :same-column, :y [[:hwy :cty]]}})))


(deftest
 t57_l479
 (is
  ((fn
    [v]
    (let
     [[hwy cty displ]
      (mapv
       (fn* [p1__80651#] (-> p1__80651# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and (= hwy cty) (not= hwy displ))))
   v56_l472)))


(def
 v59_l493
 (pj/arrange
  [(->
    {:day [1 2 3], :indoor [19.0 20.5 21.0]}
    (pj/lay-line :day :indoor))
   (->
    {:day [1 2 3], :outdoor [4.0 9.5 14.0]}
    (pj/lay-line :day :outdoor))]
  {:share-scales {:y [[:indoor :outdoor]]}}))


(deftest
 t60_l500
 (is
  ((fn
    [v]
    (let
     [doms
      (mapv
       (fn* [p1__80652#] (-> p1__80652# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))
      [lo hi]
      (first doms)]
     (and (apply = doms) (<= lo 4.0) (>= hi 21.0))))
   v59_l493)))


(def
 v62_l516
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
 t63_l525
 (is
  ((fn
    [msg]
    (re-find
     #"cannot share :y between these cells: columns :hwy, :drv.*differ in column type"
     msg))
   v62_l516)))


(def
 v65_l537
 (def north-totals {:drink ["tea" "coffee"], :sold [75 45]}))


(def
 v66_l540
 (def south-totals {:drink ["tea" "cocoa"], :sold [130 25]}))


(def
 v67_l543
 (pj/arrange
  [(-> north-totals (pj/lay-bar :drink :sold))
   (-> south-totals (pj/lay-bar :drink :sold))]
  {:share-scales #{:y :x}}))


(deftest
 t68_l548
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__80653#] (-> p1__80653# :plan :panels first :x-domain))
       (:sub-plots (pj/plan v)))]
     (= [["tea" "coffee" "cocoa"] ["tea" "coffee" "cocoa"]] domains)))
   v67_l543)))


(def
 v70_l570
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80654#] (= "setosa" (:species p1__80654#))))
    (pj/lay-histogram :sepal-length))
   (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length))]
  {:share-scales #{:y}}))


(deftest
 t71_l578
 (is
  ((fn
    [v]
    (let
     [domains
      (mapv
       (fn* [p1__80655#] (-> p1__80655# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (apply = domains)))
   v70_l570)))


(def
 v73_l591
 (->
  (rdatasets/ggplot2-mpg)
  pj/pose
  pj/lay-histogram
  (pj/facet {:x [:hwy :cty]})
  (pj/options {:share-scales {:y [[:hwy :cty]]}})))


(deftest
 t74_l597
 (is
  ((fn
    [v]
    (let
     [count-axes
      (fn
       [pose]
       (mapv
        (fn*
         [p1__80656#]
         (-> p1__80656# :plan :panels first :y-domain))
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
   v73_l591)))


(def
 v76_l618
 (pj/arrange
  [(-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :hwy))
   (-> (rdatasets/ggplot2-mpg) (pj/lay-histogram :cty))
   (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy))]
  {:share-scales {:y :all}}))


(deftest
 t77_l624
 (is
  ((fn
    [v]
    (let
     [[hwy-count cty-count hwy-values]
      (mapv
       (fn* [p1__80657#] (-> p1__80657# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v)))]
     (and (= hwy-count cty-count) (not= hwy-count hwy-values))))
   v76_l618)))


(def
 v79_l650
 (pj/arrange
  [(-> north-totals (pj/lay-bar :drink :sold) (pj/coord :flip))
   (-> south-totals (pj/lay-bar :drink :sold) (pj/coord :flip))]
  {:share-scales #{:x}}))


(deftest
 t80_l659
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__80658#] (-> p1__80658# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (apply = (map :x-domain panels))
      (number? (first (:x-domain (first panels))))
      (apply not= (map :y-domain panels)))))
   v79_l650)))


(def
 v82_l680
 (def
  readings
  {:t [1 2 3 4 5],
   :rate [1.0 2.0 3.0 2.0 4.0],
   :total [1200000.0 2400000.0 1800000.0 3100000.0 2600000.0]}))


(def
 v83_l685
 (pj/arrange
  [(-> readings (pj/lay-line :t :rate))
   (-> readings (pj/lay-line :t :total))]
  {:cols 1, :share-scales #{:x}, :align-panels true}))


(deftest
 t84_l690
 (is
  ((fn
    [v]
    (let
     [pads-of
      (fn
       [pose]
       (mapv
        (fn*
         [p1__80659#]
         (get-in p1__80659# [:plan :layout :y-label-pad]))
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
   v83_l685)))


(def
 v86_l722
 (def drv-facet (-> (pj/pose nil :displ :hwy) (pj/facet :drv))))


(def
 v87_l726
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [[drv-facet] [{:x :displ, :y :cty} {:x :displ, :y :hwy}]]
   {:share-scales {:x :all}})
  pj/lay-point))


(deftest
 t88_l732
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
   v87_l726)))


(def
 v90_l746
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange
   [[(pj/options drv-facet {:share-scales #{:y :x}})]
    [{:x :displ, :y :cty} {:x :displ, :y :hwy}]]
   {:share-scales {:x :all}})
  pj/lay-point))


(deftest
 t91_l752
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
   v90_l746)))


(def
 v93_l771
 (def
  marginal
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/marginal :top))))


(def v94_l776 marginal)


(deftest
 t95_l778
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal)))
      panels
      (mapv (fn* [p1__80660#] (-> p1__80660# :panels first)) plans)
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
        (fn* [p1__80661#] (get-in p1__80661# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__80662#] (get-in p1__80662# [:layout :legend-w]))
        plans)))))
   v94_l776)))


(def
 v97_l819
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/marginal :top :histogram {:size 0.35})))


(deftest
 t98_l823
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 150 (:points s)))))
   v97_l819)))


(def
 v100_l835
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/marginal :right)))


(deftest
 t101_l839
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan v)))
      panels
      (mapv (fn* [p1__80663#] (-> p1__80663# :panels first)) plans)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= (:y-domain (first panels)) (:y-domain (second panels)))
      (= [] (:values (:y-ticks (second panels))))
      (nil? (:y-label (second plans)))
      (apply
       ==
       (map
        (fn* [p1__80664#] (get-in p1__80664# [:layout :x-label-pad]))
        plans)))))
   v100_l835)))


(def
 v103_l881
 (pj/matrix
  (rdatasets/ggplot2-mpg)
  [:displ nil]
  [nil :hwy]
  {:col-weights [3 1], :row-weights [1 3]}))


(deftest
 t104_l884
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
         (fn* [p1__80665#] (get-in p1__80665# [:plan :panel-width]))))
       subs)
      h
      (into
       {}
       (map
        (juxt
         :path
         (fn* [p1__80666#] (get-in p1__80666# [:plan :panel-height]))))
       subs)]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (<= (Math/abs (- (w [1 0]) (* 3 (w [1 1])))) 3.0)
      (<= (Math/abs (- (h [1 0]) (* 3 (h [0 0])))) 3.0))))
   v103_l881)))


(def
 v106_l898
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


(def v107_l909 marginal-by-hand)


(deftest
 t108_l911
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plans
      (mapv :plan (:sub-plots (pj/plan marginal-by-hand)))
      panels
      (mapv (fn* [p1__80667#] (-> p1__80667# :panels first)) plans)
      [d-x s-x]
      (mapv :x-domain panels)]
     (and
      (= 2 (:panels s))
      (= 150 (:points s))
      (= d-x s-x)
      (=
       [0 102]
       (mapv
        (fn* [p1__80668#] (get-in p1__80668# [:layout :legend-w]))
        plans)))))
   v107_l909)))


(def v110_l943 (assoc-in marginal-by-hand [:opts :align-panels] true))


(deftest
 t111_l945
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
        (fn* [p1__80669#] (get-in p1__80669# [:layout :y-label-pad]))
        plans))
      (apply
       ==
       (map
        (fn* [p1__80670#] (get-in p1__80670# [:layout :legend-w]))
        plans)))))
   v110_l943)))


(def
 v113_l970
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


(def v114_l977 dashboard)


(deftest
 t115_l979
 (is
  ((fn
    [v]
    (let
     [chrome (-> dashboard pj/plan :chrome)]
     (and
      (= 4 (:panels (pj/svg-summary v)))
      (= #{} (:shared-aesthetics chrome)))))
   v114_l977)))


(def v117_l1010 (def base-data {:fitted [1 2 3], :residual [1 2 3]}))


(def
 v118_l1014
 (def other-data (tc/dataset {:x [0.5 1.5 2.5], :y [1.5 2.5 3.5]})))


(def
 v119_l1018
 (->
  base-data
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point :x :y {:color "#e6550d", :data other-data})))


(deftest
 t120_l1022
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 6 (:points s)))))
   v119_l1018)))


(def
 v122_l1038
 (->
  base-data
  (pj/lay-point :fitted :residual {:color "#377eb8"})
  (pj/lay-point
   :fitted
   :residual
   {:color "#e6550d",
    :data (tc/rename-columns other-data {:x :fitted, :y :residual})})))


(deftest
 t123_l1045
 (is
  ((fn
    [v]
    (let
     [unrenamed
      (->
       base-data
       (pj/lay-point :fitted :residual {:color "#377eb8"})
       (pj/lay-point :x :y {:color "#e6550d", :data other-data}))
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
   v122_l1038)))


(def
 v125_l1063
 (pj/arrange
  [(pj/lay-point base-data :fitted :residual {:color "#377eb8"})
   (pj/lay-point other-data :x :y {:color "#e6550d"})]))


(deftest
 t126_l1066
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
   v125_l1063)))


(def
 v128_l1087
 (def
  bounded
  (->
   {:x [1 2 3 4 5], :y [10 20 15 25 18]}
   (pj/lay-point :x :y)
   (pj/lay-line {:data {:x [1 5], :y [-200 300]}})
   (pj/scale :y {:type :linear, :domain [0 30]}))))


(def v129_l1093 (pj/arrange [bounded bounded] {:cols 1}))


(deftest
 t130_l1095
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:clips s)))))
   v129_l1093)))


(def
 v132_l1171
 (pj/arrange
  [(->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80671#] (not= "virginica" (:species p1__80671#))))
    (pj/lay-point :sepal-length :sepal-width {:color :species}))
   (->
    (rdatasets/datasets-iris)
    (tc/select-rows
     (fn* [p1__80672#] (not= "setosa" (:species p1__80672#))))
    (pj/lay-point :sepal-length :sepal-width {:color :species}))]))


(deftest
 t133_l1179
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
         [p1__80673#]
         (when
          (= "versicolor" (:label p1__80673#))
          (:color p1__80673#)))
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
   v132_l1171)))


(def
 v135_l1217
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
 t136_l1227
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v135_l1217)))
