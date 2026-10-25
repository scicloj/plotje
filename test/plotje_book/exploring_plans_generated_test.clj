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
       (pj/plan (pj/facet-matrix {:a [1 2 3], :b [3 1 2]} [:a :b]))]
      (and
       (pj/composite-plan? matrix-plan)
       (= 4 (count (:sub-plots matrix-plan)))))))
   v14_l82)))


(def v17_l94 (:x-domain tiny-panel))


(deftest
 t18_l96
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v17_l94)))


(def v19_l98 (:y-domain tiny-panel))


(deftest
 t20_l100
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v19_l98)))


(def v22_l104 (:x-scale tiny-panel))


(deftest t23_l106 (is ((fn [s] (= :linear (:type s))) v22_l104)))


(def v25_l110 (:x-ticks tiny-panel))


(deftest
 t26_l112
 (is
  ((fn
    [t]
    (and
     (vector? (:values t))
     (vector? (:labels t))
     (= (count (:values t)) (count (:labels t)))))
   v25_l110)))


(def v28_l124 (def tiny-layer (first (:layers tiny-panel))))


(def v29_l126 tiny-layer)


(deftest t30_l128 (is ((fn [m] (= :point (:mark m))) v29_l126)))


(def v32_l133 (count (:groups tiny-layer)))


(deftest t33_l135 (is ((fn [n] (= 1 n)) v32_l133)))


(def v35_l140 (first (:groups tiny-layer)))


(deftest
 t36_l142
 (is
  ((fn
    [g]
    (and
     (= 4 (count (:color g)))
     (= [1 2 3 4 5] (mapv int (:xs g)))
     (= [2 4 1 5 3] (mapv int (:ys g)))))
   v35_l140)))


(def
 v38_l156
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l159
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v38_l156)))


(def
 v40_l163
 (def
  iris-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v42_l169 iris-plan)


(deftest
 t43_l171
 (is
  ((fn
    [m]
    (and
     (= 3 (count (:entries (:legend m))))
     (= 1 (count (:panels m)))))
   v42_l169)))


(def
 v45_l176
 (def iris-layer (-> iris-plan :panels first :layers first)))


(def v46_l178 (count (:groups iris-layer)))


(deftest t47_l180 (is ((fn [n] (= 3 n)) v46_l178)))


(def
 v49_l184
 (mapv
  (fn [g] {:color (:color g), :n-points (count (:xs g))})
  (:groups iris-layer)))


(deftest
 t50_l189
 (is
  ((fn
    [gs]
    (and
     (= 3 (count gs))
     (every? (fn* [p1__88542#] (= 50 (:n-points p1__88542#))) gs)))
   v49_l184)))


(def v52_l194 (:legend iris-plan))


(deftest
 t53_l196
 (is ((fn [leg] (= 3 (count (:entries leg)))) v52_l194)))


(def
 v55_l206
 (def
  cont-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
   pj/plan)))


(def v56_l210 (:legend cont-plan))


(deftest t57_l212 (is ((fn [m] (= :continuous (:type m))) v56_l210)))


(def
 v59_l216
 (select-keys
  (:legend cont-plan)
  [:title :type :min :max :color-range]))


(deftest
 t60_l218
 (is
  ((fn
    [m]
    (and (= :continuous (:type m)) (not (contains? m :gradient-fn))))
   v59_l216)))


(def v62_l223 (count (:stops (:legend cont-plan))))


(deftest t63_l225 (is ((fn [n] (= 20 n)) v62_l223)))


(def
 v65_l232
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t66_l235
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v65_l232)))


(def
 v67_l239
 (def
  hist-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length)
   pj/plan)))


(def v68_l243 hist-plan)


(deftest t69_l245 (is ((fn [m] (= 1 (count (:panels m)))) v68_l243)))


(def
 v70_l247
 (def hist-layer (-> hist-plan :panels first :layers first)))


(def v71_l249 (:mark hist-layer))


(deftest t72_l251 (is ((fn [m] (= :bar m)) v71_l249)))


(def v74_l255 (-> hist-layer :groups first :bars))


(deftest
 t75_l257
 (is
  ((fn
    [bars]
    (and
     (> (count bars) 3)
     (every?
      (fn* [p1__88543#] (< (:lo p1__88543#) (:hi p1__88543#)))
      bars)
     (every? (fn* [p1__88544#] (pos? (:count p1__88544#))) bars)))
   v74_l255)))


(def
 v77_l269
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})))


(deftest
 t78_l272
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v77_l269)))


(def
 v79_l276
 (def
  bar-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:color :species})
   pj/plan)))


(def v80_l280 (def bar-layer (-> bar-plan :panels first :layers first)))


(def v82_l284 bar-layer)


(deftest
 t83_l286
 (is
  ((fn
    [m]
    (and
     (= :rect (:mark m))
     (= :dodge (:position m))
     (= 3 (count (:categories m)))))
   v82_l284)))


(def
 v85_l292
 (mapv
  (fn [g] {:label (:label g), :counts (:counts g)})
  (:groups bar-layer)))


(deftest t86_l297 (is ((fn [gs] (= 3 (count gs))) v85_l292)))


(def
 v88_l307
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t89_l310
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v88_l307)))


(def
 v91_l318
 (def
  stacked-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:position :stack, :color :species})
   pj/plan)))


(def
 v92_l322
 (def stacked-layer (-> stacked-plan :panels first :layers first)))


(def v93_l324 (:position stacked-layer))


(deftest t94_l326 (is ((fn [p] (= :stack p)) v93_l324)))


(def
 v96_l335
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t97_l339
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v96_l335)))


(def
 v98_l343
 (def
  lm-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def v100_l350 (mapv :mark (-> lm-plan :panels first :layers)))


(deftest
 t101_l351
 (is ((fn [marks] (= [:point :line] marks)) v100_l350)))


(def v102_l352 (def lm-layer (-> lm-plan :panels first :layers second)))


(def v104_l356 (first (:groups lm-layer)))


(deftest
 t105_l358
 (is
  ((fn
    [m]
    (and (< (:x1 m) (:x2 m)) (number? (:x1 m)) (number? (:y2 m))))
   v104_l356)))


(def
 v107_l370
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l375
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v107_l370)))


(def
 v109_l378
 (def
  grp-plan
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def
 v110_l384
 (let
  [line-layer (-> grp-plan :panels first :layers second)]
  (mapv
   (fn
    [g]
    {:color (:color g),
     :x1 (some-> (:x1 g) (Math/round) int),
     :x2 (some-> (:x2 g) (Math/round) int)})
   (:groups line-layer))))


(deftest t111_l391 (is ((fn [gs] (= 3 (count gs))) v110_l384)))


(def
 v113_l399
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__88545#] (Math/sin (* p1__88545# 0.3))) (range 30))}))


(def v114_l402 (-> wave (pj/lay-line :x :y)))


(deftest
 t115_l405
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v114_l402)))


(def v116_l409 (def wave-plan (-> wave (pj/lay-line :x :y) pj/plan)))


(def
 v117_l413
 (def
  wave-group
  (-> wave-plan :panels first :layers first :groups first)))


(def
 v118_l415
 {:n-points (count (:xs wave-group)),
  :first-x (first (:xs wave-group)),
  :last-x (last (:xs wave-group))})


(deftest t119_l419 (is ((fn [m] (= 30 (:n-points m))) v118_l415)))


(def
 v121_l428
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v122_l431 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t123_l434
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v122_l431)))


(def
 v124_l438
 (def sales-plan (-> sales (pj/lay-bar :product :revenue) pj/plan)))


(def
 v125_l442
 (let
  [g (-> sales-plan :panels first :layers first :groups first)]
  {:xs (:xs g), :ys (:ys g)}))


(deftest t126_l446 (is ((fn [m] (= 4 (count (:xs m)))) v125_l442)))


(def
 v128_l452
 (def
  flip-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/coord :flip)
   pj/plan)))


(def v129_l457 (:coord (first (:panels flip-plan))))


(deftest t130_l459 (is ((fn [c] (= :flip c)) v129_l457)))


(def
 v132_l463
 (let
  [p (first (:panels flip-plan))]
  {:x-domain-type
   (if (number? (first (:x-domain p))) :numeric :categorical),
   :y-domain-type
   (if (number? (first (:y-domain p))) :numeric :categorical)}))


(deftest
 t133_l467
 (is
  ((fn
    [m]
    (and
     (= :numeric (:x-domain-type m))
     (= :categorical (:y-domain-type m))))
   v132_l463)))


(def
 v135_l477
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


(def v136_l485 opts-plan)


(deftest
 t137_l487
 (is
  ((fn
    [m]
    (and
     (= "My Custom Title" (:title m))
     (= 800 (:width m))
     (= 300 (:height m))))
   v136_l485)))


(def v139_l493 (:layout opts-plan))


(deftest
 t140_l495
 (is
  ((fn
    [lay]
    (and
     (pos? (:title-pad lay))
     (pos? (:x-label-pad lay))
     (pos? (:y-label-pad lay))))
   v139_l493)))


(def
 v142_l506
 (def
  final-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def
 v143_l512
 (def final-plan (pj/plan final-pose {:title "Iris Petals"})))


(def v144_l514 final-plan)


(deftest
 t145_l516
 (is ((fn [m] (= "Iris Petals" (:title m))) v144_l514)))


(def
 v147_l520
 (mapv
  (fn [l] {:mark (:mark l), :n-groups (count (:groups l))})
  (-> final-plan :panels first :layers)))


(deftest t148_l525 (is ((fn [ls] (= 2 (count ls))) v147_l520)))


(def v150_l529 (-> final-pose (pj/options {:title "Iris Petals"})))


(deftest
 t151_l531
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v150_l529)))


(def
 v153_l541
 (def
  faceted-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   pj/plan)))


(def v155_l549 (:grid faceted-plan))


(deftest
 t156_l551
 (is ((fn [g] (and (= 1 (:rows g)) (= 3 (:cols g)))) v155_l549)))


(def v158_l555 (count (:panels faceted-plan)))


(deftest t159_l557 (is ((fn [n] (= 3 n)) v158_l555)))


(def v161_l561 (:panels faceted-plan))


(deftest
 t162_l563
 (is
  ((fn [ps] (and (= 3 (count ps)) (every? :col-label ps))) v161_l561)))


(def v164_l568 (:panels faceted-plan))


(deftest t165_l570 (is ((fn [ps] (every? :x-domain ps)) v164_l568)))


(def v167_l576 (mapv :y-domain (:panels faceted-plan)))


(deftest t168_l578 (is ((fn [ds] (apply = ds)) v167_l576)))


(def
 v170_l583
 (def
  free-y-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   (pj/options {:share-scales #{:x}})
   pj/plan)))


(def v171_l590 (mapv :y-domain (:panels free-y-plan)))


(deftest t172_l592 (is ((fn [ds] (not (apply = ds))) v171_l590)))


(def
 v174_l597
 (select-keys
  faceted-plan
  [:layout-type :grid :total-width :total-height]))


(deftest
 t175_l599
 (is ((fn [m] (= :facet-grid (:layout-type m))) v174_l597)))


(def v177_l603 (pj/valid-plan? faceted-plan))


(deftest t178_l605 (is (true? v177_l603)))


(def v180_l615 (pj/valid-plan? tiny-plan))


(deftest t181_l617 (is (true? v180_l615)))


(def v182_l619 (pj/valid-plan? iris-plan))


(deftest t183_l621 (is (true? v182_l619)))


(def v184_l623 (pj/valid-plan? hist-plan))


(deftest t185_l625 (is (true? v184_l623)))


(def v186_l627 (pj/valid-plan? bar-plan))


(deftest t187_l629 (is (true? v186_l627)))


(def v188_l631 (pj/valid-plan? lm-plan))


(deftest t189_l633 (is (true? v188_l631)))


(def v190_l635 (pj/valid-plan? final-plan))


(deftest t191_l637 (is (true? v190_l635)))


(def
 v193_l641
 (pj/explain-plan (assoc tiny-plan :width "not-a-number")))


(deftest t194_l643 (is (some? v193_l641)))


(def
 v196_l652
 (-> tiny-plan :panels first :layers first :groups first :xs type))


(deftest
 t197_l654
 (is ((fn [t] (not= clojure.lang.PersistentVector t)) v196_l652)))


(def
 v199_l658
 (-> tiny-plan :panels first :layers first :groups first :xs vec))


(deftest
 t200_l660
 (is ((fn [v] (and (vector? v) (number? (first v)))) v199_l658)))
