(ns
 plotje-book.exploring-plans-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [clojure.test :refer [deftest is]]))


(def v3_l33 (def tiny {:x [1 2 3 4 5], :y [2 4 1 5 3]}))


(def v5_l38 (-> tiny (pj/lay-point :x :y)))


(deftest
 t6_l41
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:points s)))))
   v5_l38)))


(def v8_l48 (def tiny-plan (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v10_l57 tiny-plan)


(deftest
 t11_l59
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
   v10_l57)))


(def v13_l79 (def tiny-panel (first (:panels tiny-plan))))


(def v14_l81 (keys tiny-panel))


(deftest
 t15_l83
 (is
  ((fn [ks] (every? (set ks) [:x-domain :y-domain :layers])) v14_l81)))


(def v17_l87 (:x-domain tiny-panel))


(deftest
 t18_l89
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v17_l87)))


(def v19_l91 (:y-domain tiny-panel))


(deftest
 t20_l93
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v19_l91)))


(def v22_l97 (:x-scale tiny-panel))


(deftest t23_l99 (is ((fn [s] (= :linear (:type s))) v22_l97)))


(def v25_l103 (:x-ticks tiny-panel))


(deftest
 t26_l105
 (is
  ((fn
    [t]
    (and
     (vector? (:values t))
     (vector? (:labels t))
     (= (count (:values t)) (count (:labels t)))))
   v25_l103)))


(def v28_l117 (def tiny-layer (first (:layers tiny-panel))))


(def v29_l119 tiny-layer)


(deftest t30_l121 (is ((fn [m] (= :point (:mark m))) v29_l119)))


(def v32_l126 (count (:groups tiny-layer)))


(deftest t33_l128 (is ((fn [n] (= 1 n)) v32_l126)))


(def v35_l133 (first (:groups tiny-layer)))


(deftest
 t36_l135
 (is
  ((fn
    [g]
    (and
     (= 4 (count (:color g)))
     (= [1 2 3 4 5] (mapv int (:xs g)))
     (= [2 4 1 5 3] (mapv int (:ys g)))))
   v35_l133)))


(def
 v38_l149
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l152
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v38_l149)))


(def
 v40_l156
 (def
  iris-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v42_l162 iris-plan)


(deftest
 t43_l164
 (is
  ((fn
    [m]
    (and
     (= 3 (count (:entries (:legend m))))
     (= 1 (count (:panels m)))))
   v42_l162)))


(def
 v45_l169
 (def iris-layer (first (:layers (first (:panels iris-plan))))))


(def v46_l171 (count (:groups iris-layer)))


(deftest t47_l173 (is ((fn [n] (= 3 n)) v46_l171)))


(def
 v49_l177
 (mapv
  (fn [g] {:color (:color g), :n-points (count (:xs g))})
  (:groups iris-layer)))


(deftest
 t50_l182
 (is
  ((fn
    [gs]
    (and
     (= 3 (count gs))
     (every? (fn* [p1__89041#] (= 50 (:n-points p1__89041#))) gs)))
   v49_l177)))


(def v52_l187 (:legend iris-plan))


(deftest
 t53_l189
 (is ((fn [leg] (= 3 (count (:entries leg)))) v52_l187)))


(def
 v55_l199
 (def
  cont-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
   pj/plan)))


(def v56_l203 (:legend cont-plan))


(deftest t57_l205 (is ((fn [m] (= :continuous (:type m))) v56_l203)))


(def
 v59_l209
 (select-keys
  (:legend cont-plan)
  [:title :type :min :max :color-scale]))


(deftest
 t60_l211
 (is
  ((fn
    [m]
    (and (= :continuous (:type m)) (not (contains? m :gradient-fn))))
   v59_l209)))


(def v62_l216 (count (:stops (:legend cont-plan))))


(deftest t63_l218 (is ((fn [n] (= 20 n)) v62_l216)))


(def
 v65_l225
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t66_l228
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v65_l225)))


(def
 v67_l232
 (def
  hist-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length)
   pj/plan)))


(def v68_l236 hist-plan)


(deftest t69_l238 (is ((fn [m] (= 1 (count (:panels m)))) v68_l236)))


(def
 v70_l240
 (def hist-layer (first (:layers (first (:panels hist-plan))))))


(def v71_l242 (:mark hist-layer))


(deftest t72_l244 (is ((fn [m] (= :bar m)) v71_l242)))


(def v74_l248 (let [g (first (:groups hist-layer))] (:bars g)))


(deftest
 t75_l251
 (is
  ((fn
    [bars]
    (and
     (> (count bars) 3)
     (every?
      (fn* [p1__89042#] (< (:lo p1__89042#) (:hi p1__89042#)))
      bars)
     (every? (fn* [p1__89043#] (pos? (:count p1__89043#))) bars)))
   v74_l248)))


(def
 v77_l263
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})))


(deftest
 t78_l266
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v77_l263)))


(def
 v79_l270
 (def
  bar-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:color :species})
   pj/plan)))


(def
 v80_l274
 (def bar-layer (first (:layers (first (:panels bar-plan))))))


(def v82_l278 bar-layer)


(deftest
 t83_l280
 (is
  ((fn
    [m]
    (and
     (= :rect (:mark m))
     (= :dodge (:position m))
     (= 3 (count (:categories m)))))
   v82_l278)))


(def
 v85_l286
 (mapv
  (fn [g] {:label (:label g), :counts (:counts g)})
  (:groups bar-layer)))


(deftest t86_l291 (is ((fn [gs] (= 3 (count gs))) v85_l286)))


(def
 v88_l300
 (def
  stacked-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:position :stack, :color :species})
   pj/plan)))


(def
 v89_l304
 (def stacked-layer (first (:layers (first (:panels stacked-plan))))))


(def v90_l306 (:position stacked-layer))


(deftest t91_l308 (is ((fn [p] (= :stack p)) v90_l306)))


(def
 v93_l317
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t94_l321
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v93_l317)))


(def
 v95_l325
 (def
  lm-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def v97_l332 (mapv :mark (:layers (first (:panels lm-plan)))))


(deftest t98_l333 (is ((fn [marks] (= [:point :line] marks)) v97_l332)))


(def
 v99_l334
 (def lm-layer (second (:layers (first (:panels lm-plan))))))


(def v101_l338 (first (:groups lm-layer)))


(deftest
 t102_l340
 (is
  ((fn
    [m]
    (and (< (:x1 m) (:x2 m)) (number? (:x1 m)) (number? (:y2 m))))
   v101_l338)))


(def
 v104_l352
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t105_l357
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v104_l352)))


(def
 v106_l360
 (def
  grp-plan
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def
 v107_l366
 (let
  [line-layer (second (:layers (first (:panels grp-plan))))]
  (mapv
   (fn
    [g]
    {:color (:color g),
     :x1 (some-> (:x1 g) (Math/round) int),
     :x2 (some-> (:x2 g) (Math/round) int)})
   (:groups line-layer))))


(deftest t108_l373 (is ((fn [gs] (= 3 (count gs))) v107_l366)))


(def
 v110_l381
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__89044#] (Math/sin (* p1__89044# 0.3))) (range 30))}))


(def v111_l384 (-> wave (pj/lay-line :x :y)))


(deftest
 t112_l387
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v111_l384)))


(def v113_l391 (def wave-plan (-> wave (pj/lay-line :x :y) pj/plan)))


(def
 v114_l395
 (def
  wave-group
  (first (:groups (first (:layers (first (:panels wave-plan))))))))


(def
 v115_l397
 {:n-points (count (:xs wave-group)),
  :first-x (first (:xs wave-group)),
  :last-x (last (:xs wave-group))})


(deftest t116_l401 (is ((fn [m] (= 30 (:n-points m))) v115_l397)))


(def
 v118_l410
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v119_l413 (-> sales (pj/lay-value-bar :product :revenue)))


(deftest
 t120_l416
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v119_l413)))


(def
 v121_l420
 (def
  sales-plan
  (-> sales (pj/lay-value-bar :product :revenue) pj/plan)))


(def
 v122_l424
 (let
  [g (first (:groups (first (:layers (first (:panels sales-plan))))))]
  {:xs (:xs g), :ys (:ys g)}))


(deftest t123_l428 (is ((fn [m] (= 4 (count (:xs m)))) v122_l424)))


(def
 v125_l434
 (def
  flip-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/coord :flip)
   pj/plan)))


(def v126_l439 (:coord (first (:panels flip-plan))))


(deftest t127_l441 (is ((fn [c] (= :flip c)) v126_l439)))


(def
 v129_l445
 (let
  [p (first (:panels flip-plan))]
  {:x-domain-type
   (if (number? (first (:x-domain p))) :numeric :categorical),
   :y-domain-type
   (if (number? (first (:y-domain p))) :numeric :categorical)}))


(deftest
 t130_l449
 (is
  ((fn
    [m]
    (and
     (= :numeric (:x-domain-type m))
     (= :categorical (:y-domain-type m))))
   v129_l445)))


(def
 v132_l459
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


(def v133_l467 opts-plan)


(deftest
 t134_l469
 (is
  ((fn
    [m]
    (and
     (= "My Custom Title" (:title m))
     (= 800 (:width m))
     (= 300 (:height m))))
   v133_l467)))


(def v136_l475 (:layout opts-plan))


(deftest
 t137_l477
 (is
  ((fn
    [lay]
    (and
     (pos? (:title-pad lay))
     (pos? (:x-label-pad lay))
     (pos? (:y-label-pad lay))))
   v136_l475)))


(def
 v139_l488
 (def
  final-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def
 v140_l494
 (def final-plan (pj/plan final-pose {:title "Iris Petals"})))


(def v141_l496 final-plan)


(deftest
 t142_l498
 (is ((fn [m] (= "Iris Petals" (:title m))) v141_l496)))


(def
 v144_l502
 (mapv
  (fn [l] {:mark (:mark l), :n-groups (count (:groups l))})
  (:layers (first (:panels final-plan)))))


(deftest t145_l507 (is ((fn [ls] (= 2 (count ls))) v144_l502)))


(def v147_l511 (-> final-pose (pj/options {:title "Iris Petals"})))


(deftest
 t148_l513
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v147_l511)))


(def
 v150_l522
 (def
  faceted-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   pj/plan)))


(def v152_l530 (:grid faceted-plan))


(deftest
 t153_l532
 (is ((fn [g] (and (= 1 (:rows g)) (= 3 (:cols g)))) v152_l530)))


(def v155_l536 (count (:panels faceted-plan)))


(deftest t156_l538 (is ((fn [n] (= 3 n)) v155_l536)))


(def v158_l542 (:panels faceted-plan))


(deftest
 t159_l544
 (is
  ((fn [ps] (and (= 3 (count ps)) (every? :col-label ps))) v158_l542)))


(def v161_l549 (:panels faceted-plan))


(deftest t162_l551 (is ((fn [ps] (every? :x-domain ps)) v161_l549)))


(def
 v164_l558
 (select-keys
  faceted-plan
  [:layout-type :grid :total-width :total-height]))


(deftest
 t165_l560
 (is ((fn [m] (= :facet-grid (:layout-type m))) v164_l558)))


(def v167_l564 (pj/valid-plan? faceted-plan))


(deftest t168_l566 (is (true? v167_l564)))


(def v170_l576 (pj/valid-plan? tiny-plan))


(deftest t171_l578 (is (true? v170_l576)))


(def v172_l580 (pj/valid-plan? iris-plan))


(deftest t173_l582 (is (true? v172_l580)))


(def v174_l584 (pj/valid-plan? hist-plan))


(deftest t175_l586 (is (true? v174_l584)))


(def v176_l588 (pj/valid-plan? bar-plan))


(deftest t177_l590 (is (true? v176_l588)))


(def v178_l592 (pj/valid-plan? lm-plan))


(deftest t179_l594 (is (true? v178_l592)))


(def v180_l596 (pj/valid-plan? final-plan))


(deftest t181_l598 (is (true? v180_l596)))


(def
 v183_l602
 (pj/explain-plan (assoc tiny-plan :width "not-a-number")))


(deftest t184_l604 (is (some? v183_l602)))


(def
 v186_l613
 (type
  (:xs
   (first (:groups (first (:layers (first (:panels tiny-plan)))))))))


(deftest
 t187_l615
 (is ((fn [t] (not= clojure.lang.PersistentVector t)) v186_l613)))


(def
 v189_l619
 (vec
  (:xs
   (first (:groups (first (:layers (first (:panels tiny-plan)))))))))


(deftest
 t190_l621
 (is ((fn [v] (and (vector? v) (number? (first v)))) v189_l619)))
