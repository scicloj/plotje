(ns
 plotje-book.exploring-plans-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l31 (def tiny {:x [1 2 3 4 5], :y [2 4 1 5 3]}))


(def v5_l36 (-> tiny (pj/lay-point :x :y)))


(deftest
 t6_l39
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:points s)))))
   v5_l36)))


(def v8_l46 (def tiny-plan (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v10_l55 tiny-plan)


(deftest
 t11_l57
 (is
  ((fn
    [m]
    (and
     (= 600 (:width m))
     (= 400 (:height m))
     (== 10 (:margin m))
     (nil? (:title m))
     (= "x" (:x-label m))
     (= "y" (:y-label m))
     (nil? (:legend m))))
   v10_l55)))


(def v13_l77 (def tiny-panel (first (:panels tiny-plan))))


(def v14_l79 (keys tiny-panel))


(deftest
 t15_l81
 (is
  ((fn [ks] (every? (set ks) [:x-domain :y-domain :layers])) v14_l79)))


(def v17_l85 (:x-domain tiny-panel))


(deftest
 t18_l87
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v17_l85)))


(def v19_l89 (:y-domain tiny-panel))


(deftest
 t20_l91
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v19_l89)))


(def v22_l95 (:x-scale tiny-panel))


(deftest t23_l97 (is ((fn [s] (= :linear (:type s))) v22_l95)))


(def v25_l101 (:x-ticks tiny-panel))


(deftest
 t26_l103
 (is
  ((fn
    [t]
    (and
     (vector? (:values t))
     (vector? (:labels t))
     (= (count (:values t)) (count (:labels t)))))
   v25_l101)))


(def v28_l115 (def tiny-layer (first (:layers tiny-panel))))


(def v29_l117 tiny-layer)


(deftest t30_l119 (is ((fn [m] (= :point (:mark m))) v29_l117)))


(def v32_l124 (count (:groups tiny-layer)))


(deftest t33_l126 (is ((fn [n] (= 1 n)) v32_l124)))


(def v35_l131 (first (:groups tiny-layer)))


(deftest
 t36_l133
 (is
  ((fn
    [g]
    (and
     (= 4 (count (:color g)))
     (= [1 2 3 4 5] (mapv int (:xs g)))
     (= [2 4 1 5 3] (mapv int (:ys g)))))
   v35_l131)))


(def
 v38_l147
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l150
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v38_l147)))


(def
 v40_l154
 (def
  iris-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v42_l160 iris-plan)


(deftest
 t43_l162
 (is
  ((fn
    [m]
    (and
     (= 3 (count (:entries (:legend m))))
     (= 1 (count (:panels m)))))
   v42_l160)))


(def
 v45_l167
 (def iris-layer (-> iris-plan :panels first :layers first)))


(def v46_l169 (count (:groups iris-layer)))


(deftest t47_l171 (is ((fn [n] (= 3 n)) v46_l169)))


(def
 v49_l175
 (mapv
  (fn [g] {:color (:color g), :n-points (count (:xs g))})
  (:groups iris-layer)))


(deftest
 t50_l180
 (is
  ((fn
    [gs]
    (and
     (= 3 (count gs))
     (every? (fn* [p1__88438#] (= 50 (:n-points p1__88438#))) gs)))
   v49_l175)))


(def v52_l185 (:legend iris-plan))


(deftest
 t53_l187
 (is ((fn [leg] (= 3 (count (:entries leg)))) v52_l185)))


(def
 v55_l197
 (def
  cont-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
   pj/plan)))


(def v56_l201 (:legend cont-plan))


(deftest t57_l203 (is ((fn [m] (= :continuous (:type m))) v56_l201)))


(def
 v59_l207
 (select-keys
  (:legend cont-plan)
  [:title :type :min :max :color-scale]))


(deftest
 t60_l209
 (is
  ((fn
    [m]
    (and (= :continuous (:type m)) (not (contains? m :gradient-fn))))
   v59_l207)))


(def v62_l214 (count (:stops (:legend cont-plan))))


(deftest t63_l216 (is ((fn [n] (= 20 n)) v62_l214)))


(def
 v65_l223
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t66_l226
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v65_l223)))


(def
 v67_l230
 (def
  hist-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length)
   pj/plan)))


(def v68_l234 hist-plan)


(deftest t69_l236 (is ((fn [m] (= 1 (count (:panels m)))) v68_l234)))


(def
 v70_l238
 (def hist-layer (-> hist-plan :panels first :layers first)))


(def v71_l240 (:mark hist-layer))


(deftest t72_l242 (is ((fn [m] (= :bar m)) v71_l240)))


(def v74_l246 (let [g (first (:groups hist-layer))] (:bars g)))


(deftest
 t75_l249
 (is
  ((fn
    [bars]
    (and
     (> (count bars) 3)
     (every?
      (fn* [p1__88439#] (< (:lo p1__88439#) (:hi p1__88439#)))
      bars)
     (every? (fn* [p1__88440#] (pos? (:count p1__88440#))) bars)))
   v74_l246)))


(def
 v77_l261
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})))


(deftest
 t78_l264
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v77_l261)))


(def
 v79_l268
 (def
  bar-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:color :species})
   pj/plan)))


(def v80_l272 (def bar-layer (-> bar-plan :panels first :layers first)))


(def v82_l276 bar-layer)


(deftest
 t83_l278
 (is
  ((fn
    [m]
    (and
     (= :rect (:mark m))
     (= :dodge (:position m))
     (= 3 (count (:categories m)))))
   v82_l276)))


(def
 v85_l284
 (mapv
  (fn [g] {:label (:label g), :counts (:counts g)})
  (:groups bar-layer)))


(deftest t86_l289 (is ((fn [gs] (= 3 (count gs))) v85_l284)))


(def
 v88_l298
 (def
  stacked-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:position :stack, :color :species})
   pj/plan)))


(def
 v89_l302
 (def stacked-layer (-> stacked-plan :panels first :layers first)))


(def v90_l304 (:position stacked-layer))


(deftest t91_l306 (is ((fn [p] (= :stack p)) v90_l304)))


(def
 v93_l315
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t94_l319
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v93_l315)))


(def
 v95_l323
 (def
  lm-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def v97_l330 (mapv :mark (-> lm-plan :panels first :layers)))


(deftest t98_l331 (is ((fn [marks] (= [:point :line] marks)) v97_l330)))


(def v99_l332 (def lm-layer (-> lm-plan :panels first :layers second)))


(def v101_l336 (first (:groups lm-layer)))


(deftest
 t102_l338
 (is
  ((fn
    [m]
    (and (< (:x1 m) (:x2 m)) (number? (:x1 m)) (number? (:y2 m))))
   v101_l336)))


(def
 v104_l350
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t105_l355
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v104_l350)))


(def
 v106_l358
 (def
  grp-plan
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def
 v107_l364
 (let
  [line-layer (-> grp-plan :panels first :layers second)]
  (mapv
   (fn
    [g]
    {:color (:color g),
     :x1 (some-> (:x1 g) (Math/round) int),
     :x2 (some-> (:x2 g) (Math/round) int)})
   (:groups line-layer))))


(deftest t108_l371 (is ((fn [gs] (= 3 (count gs))) v107_l364)))


(def
 v110_l379
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__88441#] (Math/sin (* p1__88441# 0.3))) (range 30))}))


(def v111_l382 (-> wave (pj/lay-line :x :y)))


(deftest
 t112_l385
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v111_l382)))


(def v113_l389 (def wave-plan (-> wave (pj/lay-line :x :y) pj/plan)))


(def
 v114_l393
 (def
  wave-group
  (-> wave-plan :panels first :layers first :groups first)))


(def
 v115_l395
 {:n-points (count (:xs wave-group)),
  :first-x (first (:xs wave-group)),
  :last-x (last (:xs wave-group))})


(deftest t116_l399 (is ((fn [m] (= 30 (:n-points m))) v115_l395)))


(def
 v118_l408
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v119_l411 (-> sales (pj/lay-value-bar :product :revenue)))


(deftest
 t120_l414
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v119_l411)))


(def
 v121_l418
 (def
  sales-plan
  (-> sales (pj/lay-value-bar :product :revenue) pj/plan)))


(def
 v122_l422
 (let
  [g (-> sales-plan :panels first :layers first :groups first)]
  {:xs (:xs g), :ys (:ys g)}))


(deftest t123_l426 (is ((fn [m] (= 4 (count (:xs m)))) v122_l422)))


(def
 v125_l432
 (def
  flip-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/coord :flip)
   pj/plan)))


(def v126_l437 (:coord (first (:panels flip-plan))))


(deftest t127_l439 (is ((fn [c] (= :flip c)) v126_l437)))


(def
 v129_l443
 (let
  [p (first (:panels flip-plan))]
  {:x-domain-type
   (if (number? (first (:x-domain p))) :numeric :categorical),
   :y-domain-type
   (if (number? (first (:y-domain p))) :numeric :categorical)}))


(deftest
 t130_l447
 (is
  ((fn
    [m]
    (and
     (= :numeric (:x-domain-type m))
     (= :categorical (:y-domain-type m))))
   v129_l443)))


(def
 v132_l457
 (def
  opts-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/plan
    {:title "My Custom Title",
     :x-label "Length (cm)",
     :y-label "Width (cm)",
     :width 800,
     :height 300}))))


(def v133_l465 opts-plan)


(deftest
 t134_l467
 (is
  ((fn
    [m]
    (and
     (= "My Custom Title" (:title m))
     (= 800 (:width m))
     (= 300 (:height m))))
   v133_l465)))


(def v136_l473 (:layout opts-plan))


(deftest
 t137_l475
 (is
  ((fn
    [lay]
    (and
     (pos? (:title-pad lay))
     (pos? (:x-label-pad lay))
     (pos? (:y-label-pad lay))))
   v136_l473)))


(def
 v139_l486
 (def
  final-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def
 v140_l492
 (def final-plan (pj/plan final-pose {:title "Iris Petals"})))


(def v141_l494 final-plan)


(deftest
 t142_l496
 (is ((fn [m] (= "Iris Petals" (:title m))) v141_l494)))


(def
 v144_l500
 (mapv
  (fn [l] {:mark (:mark l), :n-groups (count (:groups l))})
  (-> final-plan :panels first :layers)))


(deftest t145_l505 (is ((fn [ls] (= 2 (count ls))) v144_l500)))


(def v147_l509 (-> final-pose (pj/options {:title "Iris Petals"})))


(deftest
 t148_l511
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v147_l509)))


(def
 v150_l520
 (def
  faceted-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   pj/plan)))


(def v152_l528 (:grid faceted-plan))


(deftest
 t153_l530
 (is ((fn [g] (and (= 1 (:rows g)) (= 3 (:cols g)))) v152_l528)))


(def v155_l534 (count (:panels faceted-plan)))


(deftest t156_l536 (is ((fn [n] (= 3 n)) v155_l534)))


(def v158_l540 (:panels faceted-plan))


(deftest
 t159_l542
 (is
  ((fn [ps] (and (= 3 (count ps)) (every? :col-label ps))) v158_l540)))


(def v161_l547 (:panels faceted-plan))


(deftest t162_l549 (is ((fn [ps] (every? :x-domain ps)) v161_l547)))


(def
 v164_l556
 (select-keys
  faceted-plan
  [:layout-type :grid :total-width :total-height]))


(deftest
 t165_l558
 (is ((fn [m] (= :facet-grid (:layout-type m))) v164_l556)))


(def v167_l562 (pj/valid-plan? faceted-plan))


(deftest t168_l564 (is (true? v167_l562)))


(def v170_l574 (pj/valid-plan? tiny-plan))


(deftest t171_l576 (is (true? v170_l574)))


(def v172_l578 (pj/valid-plan? iris-plan))


(deftest t173_l580 (is (true? v172_l578)))


(def v174_l582 (pj/valid-plan? hist-plan))


(deftest t175_l584 (is (true? v174_l582)))


(def v176_l586 (pj/valid-plan? bar-plan))


(deftest t177_l588 (is (true? v176_l586)))


(def v178_l590 (pj/valid-plan? lm-plan))


(deftest t179_l592 (is (true? v178_l590)))


(def v180_l594 (pj/valid-plan? final-plan))


(deftest t181_l596 (is (true? v180_l594)))


(def
 v183_l600
 (pj/explain-plan (assoc tiny-plan :width "not-a-number")))


(deftest t184_l602 (is (some? v183_l600)))


(def
 v186_l611
 (-> tiny-plan :panels first :layers first :groups first :xs type))


(deftest
 t187_l613
 (is ((fn [t] (not= clojure.lang.PersistentVector t)) v186_l611)))


(def
 v189_l617
 (-> tiny-plan :panels first :layers first :groups first :xs vec))


(deftest
 t190_l619
 (is ((fn [v] (and (vector? v) (number? (first v)))) v189_l617)))
