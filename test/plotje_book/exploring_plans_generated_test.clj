(ns
 plotje-book.exploring-plans-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def v3_l30 (def tiny {:x [1 2 3 4 5], :y [2 4 1 5 3]}))


(def v5_l35 (-> tiny (pj/lay-point :x :y)))


(deftest
 t6_l38
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:points s)))))
   v5_l35)))


(def v8_l45 (def tiny-plan (-> tiny (pj/lay-point :x :y) pj/plan)))


(def v10_l54 tiny-plan)


(deftest
 t11_l56
 (is
  ((fn
    [m]
    (and
     (record? m)
     (= 600 (:width m))
     (= 400 (:height m))
     (== 10 (:margin m))
     (nil? (:title m))
     (= "x" (:x-label m))
     (= "y" (:y-label m))
     (nil? (:legend m))))
   v10_l54)))


(def v13_l80 (def tiny-panel (first (:panels tiny-plan))))


(def v14_l82 (keys tiny-panel))


(deftest
 t15_l84
 (is
  ((fn
    [ks]
    (and
     (every? (set ks) [:x-domain :y-domain :layers])
     (let
      [matrix-plan
       (pj/plan
        (pj/facet
         {:a [1 2 3], :b [3 1 2]}
         {:col {:x [:a :b]}, :row {:y [:a :b]}}))]
      (and
       (pj/composite-plan? matrix-plan)
       (= 4 (count (:sub-plots matrix-plan)))))))
   v14_l82)))


(def v17_l96 (:x-domain tiny-panel))


(deftest
 t18_l98
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v17_l96)))


(def v19_l100 (:y-domain tiny-panel))


(deftest
 t20_l102
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v19_l100)))


(def v22_l106 (:x-scale tiny-panel))


(deftest t23_l108 (is ((fn [s] (= :linear (:type s))) v22_l106)))


(def v25_l112 (:x-ticks tiny-panel))


(deftest
 t26_l114
 (is
  ((fn
    [t]
    (and
     (vector? (:values t))
     (vector? (:labels t))
     (= (count (:values t)) (count (:labels t)))))
   v25_l112)))


(def v28_l126 (def tiny-layer (first (:layers tiny-panel))))


(def v29_l128 tiny-layer)


(deftest t30_l130 (is ((fn [m] (= :point (:mark m))) v29_l128)))


(def v32_l135 (count (:groups tiny-layer)))


(deftest t33_l137 (is ((fn [n] (= 1 n)) v32_l135)))


(def v35_l142 (first (:groups tiny-layer)))


(deftest
 t36_l144
 (is
  ((fn
    [g]
    (and
     (= 4 (count (:color g)))
     (= [1 2 3 4 5] (mapv int (:xs g)))
     (= [2 4 1 5 3] (mapv int (:ys g)))))
   v35_l142)))


(def
 v38_l158
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l161
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v38_l158)))


(def
 v40_l165
 (def
  iris-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v42_l171 iris-plan)


(deftest
 t43_l173
 (is
  ((fn
    [m]
    (and
     (= 3 (count (:entries (:legend m))))
     (= 1 (count (:panels m)))))
   v42_l171)))


(def
 v45_l178
 (def iris-layer (-> iris-plan :panels first :layers first)))


(def v46_l184 (count (:groups iris-layer)))


(deftest t47_l186 (is ((fn [n] (= 3 n)) v46_l184)))


(def
 v49_l190
 (mapv
  (fn [g] {:color (:color g), :n-points (count (:xs g))})
  (:groups iris-layer)))


(deftest
 t50_l195
 (is
  ((fn
    [gs]
    (and
     (= 3 (count gs))
     (every? (fn* [p1__90258#] (= 50 (:n-points p1__90258#))) gs)))
   v49_l190)))


(def v52_l200 (:legend iris-plan))


(deftest
 t53_l202
 (is ((fn [leg] (= 3 (count (:entries leg)))) v52_l200)))


(def
 v55_l212
 (def
  cont-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
   pj/plan)))


(def v56_l216 (:legend cont-plan))


(deftest t57_l218 (is ((fn [m] (= :continuous (:type m))) v56_l216)))


(def
 v59_l222
 (select-keys
  (:legend cont-plan)
  [:title :type :min :max :color-range]))


(deftest
 t60_l224
 (is
  ((fn
    [m]
    (and (= :continuous (:type m)) (not (contains? m :gradient-fn))))
   v59_l222)))


(def v62_l229 (count (:stops (:legend cont-plan))))


(deftest t63_l231 (is ((fn [n] (= 20 n)) v62_l229)))


(def
 v65_l238
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t66_l241
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v65_l238)))


(def
 v67_l245
 (def
  hist-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length)
   pj/plan)))


(def v68_l249 hist-plan)


(deftest t69_l251 (is ((fn [m] (= 1 (count (:panels m)))) v68_l249)))


(def
 v70_l253
 (def hist-layer (-> hist-plan :panels first :layers first)))


(def v71_l259 (:mark hist-layer))


(deftest t72_l261 (is ((fn [m] (= :bar m)) v71_l259)))


(def v74_l266 (-> hist-layer :groups first :bars))


(deftest
 t75_l271
 (is
  ((fn
    [bars]
    (and
     (> (count bars) 3)
     (every?
      (fn* [p1__90259#] (< (:lo p1__90259#) (:hi p1__90259#)))
      bars)
     (every? (fn* [p1__90260#] (pos? (:count p1__90260#))) bars)))
   v74_l266)))


(def
 v77_l283
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})))


(deftest
 t78_l286
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v77_l283)))


(def
 v79_l290
 (def
  bar-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:color :species})
   pj/plan)))


(def v80_l294 (def bar-layer (-> bar-plan :panels first :layers first)))


(def v82_l302 bar-layer)


(deftest
 t83_l304
 (is
  ((fn
    [m]
    (and
     (= :rect (:mark m))
     (= :dodge (:position m))
     (= 3 (count (:categories m)))))
   v82_l302)))


(def
 v85_l310
 (mapv
  (fn [g] {:label (:label g), :counts (:counts g)})
  (:groups bar-layer)))


(deftest t86_l315 (is ((fn [gs] (= 3 (count gs))) v85_l310)))


(def
 v88_l325
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t89_l328
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v88_l325)))


(def
 v91_l336
 (def
  stacked-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:position :stack, :color :species})
   pj/plan)))


(def
 v92_l340
 (def stacked-layer (-> stacked-plan :panels first :layers first)))


(def v93_l346 (:position stacked-layer))


(deftest t94_l348 (is ((fn [p] (= :stack p)) v93_l346)))


(def
 v96_l357
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t97_l361
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v96_l357)))


(def
 v98_l365
 (def
  lm-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def v100_l372 (mapv :mark (-> lm-plan :panels first :layers)))


(deftest
 t101_l376
 (is ((fn [marks] (= [:point :line] marks)) v100_l372)))


(def v102_l377 (def lm-layer (-> lm-plan :panels first :layers second)))


(def v104_l385 (first (:groups lm-layer)))


(deftest
 t105_l387
 (is
  ((fn
    [m]
    (and (< (:x1 m) (:x2 m)) (number? (:x1 m)) (number? (:y2 m))))
   v104_l385)))


(def
 v107_l399
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l404
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v107_l399)))


(def
 v109_l407
 (def
  grp-plan
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def
 v110_l413
 (let
  [line-layer (-> grp-plan :panels first :layers second)]
  (mapv
   (fn
    [g]
    {:color (:color g),
     :x1 (some-> (:x1 g) (Math/round) int),
     :x2 (some-> (:x2 g) (Math/round) int)})
   (:groups line-layer))))


(deftest t111_l428 (is ((fn [gs] (= 3 (count gs))) v110_l413)))


(def
 v113_l436
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__90261#] (Math/sin (* p1__90261# 0.3))) (range 30))}))


(def v114_l439 (-> wave (pj/lay-line :x :y)))


(deftest
 t115_l442
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v114_l439)))


(def v116_l446 (def wave-plan (-> wave (pj/lay-line :x :y) pj/plan)))


(def
 v117_l450
 (def
  wave-group
  (-> wave-plan :panels first :layers first :groups first)))


(def
 v118_l458
 {:n-points (count (:xs wave-group)),
  :first-x (first (:xs wave-group)),
  :last-x (last (:xs wave-group))})


(deftest t119_l462 (is ((fn [m] (= 30 (:n-points m))) v118_l458)))


(def
 v121_l471
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v122_l474 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t123_l477
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v122_l474)))


(def
 v124_l481
 (def sales-plan (-> sales (pj/lay-bar :product :revenue) pj/plan)))


(def
 v125_l485
 (let
  [g (-> sales-plan :panels first :layers first :groups first)]
  {:xs (:xs g), :ys (:ys g)}))


(deftest t126_l495 (is ((fn [m] (= 4 (count (:xs m)))) v125_l485)))


(def
 v128_l501
 (def
  flip-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/coord :flip)
   pj/plan)))


(def v129_l506 (:coord (first (:panels flip-plan))))


(deftest t130_l508 (is ((fn [c] (= :flip c)) v129_l506)))


(def
 v132_l512
 (let
  [p (first (:panels flip-plan))]
  {:x-domain-type
   (if (number? (first (:x-domain p))) :numeric :categorical),
   :y-domain-type
   (if (number? (first (:y-domain p))) :numeric :categorical)}))


(deftest
 t133_l516
 (is
  ((fn
    [m]
    (and
     (= :numeric (:x-domain-type m))
     (= :categorical (:y-domain-type m))))
   v132_l512)))


(def
 v135_l526
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


(def v136_l534 opts-plan)


(deftest
 t137_l536
 (is
  ((fn
    [m]
    (and
     (= "My Custom Title" (:title m))
     (= 800 (:width m))
     (= 300 (:height m))))
   v136_l534)))


(def v139_l542 (:layout opts-plan))


(deftest
 t140_l544
 (is
  ((fn
    [lay]
    (and
     (pos? (:title-pad lay))
     (pos? (:x-label-pad lay))
     (pos? (:y-label-pad lay))))
   v139_l542)))


(def
 v142_l555
 (def
  final-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def
 v143_l561
 (def final-plan (pj/plan final-pose {:title "Iris Petals"})))


(def v144_l563 final-plan)


(deftest
 t145_l565
 (is ((fn [m] (= "Iris Petals" (:title m))) v144_l563)))


(def
 v147_l569
 (mapv
  (fn [l] {:mark (:mark l), :n-groups (count (:groups l))})
  (-> final-plan :panels first :layers)))


(deftest t148_l577 (is ((fn [ls] (= 2 (count ls))) v147_l569)))


(def v150_l581 (-> final-pose (pj/options {:title "Iris Petals"})))


(deftest
 t151_l583
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v150_l581)))


(def
 v153_l593
 (def
  faceted-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   pj/plan)))


(def v155_l601 (:grid faceted-plan))


(deftest
 t156_l603
 (is ((fn [g] (and (= 1 (:rows g)) (= 3 (:cols g)))) v155_l601)))


(def v158_l607 (count (:panels faceted-plan)))


(deftest t159_l609 (is ((fn [n] (= 3 n)) v158_l607)))


(def v161_l613 (:panels faceted-plan))


(deftest
 t162_l615
 (is
  ((fn
    [ps]
    (and
     (= 3 (count ps))
     (every? :col-label ps)
     (= [[0 0] [0 1] [0 2]] (mapv (juxt :row :col) ps))))
   v161_l613)))


(def v164_l621 (:panels faceted-plan))


(deftest t165_l623 (is ((fn [ps] (every? :x-domain ps)) v164_l621)))


(def v167_l629 (mapv :y-domain (:panels faceted-plan)))


(deftest t168_l631 (is ((fn [ds] (apply = ds)) v167_l629)))


(def
 v170_l636
 (def
  free-y-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   (pj/options {:share-scales #{:x}})
   pj/plan)))


(def v171_l643 (mapv :y-domain (:panels free-y-plan)))


(deftest t172_l645 (is ((fn [ds] (not (apply = ds))) v171_l643)))


(def
 v174_l650
 (select-keys
  faceted-plan
  [:layout-type :grid :total-width :total-height]))


(deftest
 t175_l652
 (is ((fn [m] (= :facet-grid (:layout-type m))) v174_l650)))


(def v177_l656 (pj/valid-plan? faceted-plan))


(deftest t178_l658 (is (true? v177_l656)))


(def v180_l668 (pj/valid-plan? tiny-plan))


(deftest t181_l670 (is (true? v180_l668)))


(def v182_l672 (pj/valid-plan? iris-plan))


(deftest t183_l674 (is (true? v182_l672)))


(def v184_l676 (pj/valid-plan? hist-plan))


(deftest t185_l678 (is (true? v184_l676)))


(def v186_l680 (pj/valid-plan? bar-plan))


(deftest t187_l682 (is (true? v186_l680)))


(def v188_l684 (pj/valid-plan? lm-plan))


(deftest t189_l686 (is (true? v188_l684)))


(def v190_l688 (pj/valid-plan? final-plan))


(deftest t191_l690 (is (true? v190_l688)))


(def
 v193_l694
 (pj/explain-plan (assoc tiny-plan :width "not-a-number")))


(deftest t194_l696 (is (some? v193_l694)))


(def
 v196_l705
 (-> tiny-plan :panels first :layers first :groups first :xs type))


(deftest
 t197_l715
 (is ((fn [t] (not= clojure.lang.PersistentVector t)) v196_l705)))


(def
 v199_l719
 (-> tiny-plan :panels first :layers first :groups first :xs vec))


(deftest
 t200_l729
 (is ((fn [v] (and (vector? v) (number? (first v)))) v199_l719)))
