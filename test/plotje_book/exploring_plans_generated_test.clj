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


(def v46_l180 (count (:groups iris-layer)))


(deftest t47_l182 (is ((fn [n] (= 3 n)) v46_l180)))


(def
 v49_l186
 (mapv
  (fn [g] {:color (:color g), :n-points (count (:xs g))})
  (:groups iris-layer)))


(deftest
 t50_l191
 (is
  ((fn
    [gs]
    (and
     (= 3 (count gs))
     (every? (fn* [p1__90246#] (= 50 (:n-points p1__90246#))) gs)))
   v49_l186)))


(def v52_l196 (:legend iris-plan))


(deftest
 t53_l198
 (is ((fn [leg] (= 3 (count (:entries leg)))) v52_l196)))


(def
 v55_l208
 (def
  cont-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
   pj/plan)))


(def v56_l212 (:legend cont-plan))


(deftest t57_l214 (is ((fn [m] (= :continuous (:type m))) v56_l212)))


(def
 v59_l218
 (select-keys
  (:legend cont-plan)
  [:title :type :min :max :color-range]))


(deftest
 t60_l220
 (is
  ((fn
    [m]
    (and (= :continuous (:type m)) (not (contains? m :gradient-fn))))
   v59_l218)))


(def v62_l225 (count (:stops (:legend cont-plan))))


(deftest t63_l227 (is ((fn [n] (= 20 n)) v62_l225)))


(def
 v65_l234
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t66_l237
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v65_l234)))


(def
 v67_l241
 (def
  hist-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length)
   pj/plan)))


(def v68_l245 hist-plan)


(deftest t69_l247 (is ((fn [m] (= 1 (count (:panels m)))) v68_l245)))


(def
 v70_l249
 (def hist-layer (-> hist-plan :panels first :layers first)))


(def v71_l251 (:mark hist-layer))


(deftest t72_l253 (is ((fn [m] (= :bar m)) v71_l251)))


(def v74_l258 (-> hist-layer :groups first :bars))


(deftest
 t75_l260
 (is
  ((fn
    [bars]
    (and
     (> (count bars) 3)
     (every?
      (fn* [p1__90247#] (< (:lo p1__90247#) (:hi p1__90247#)))
      bars)
     (every? (fn* [p1__90248#] (pos? (:count p1__90248#))) bars)))
   v74_l258)))


(def
 v77_l272
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})))


(deftest
 t78_l275
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v77_l272)))


(def
 v79_l279
 (def
  bar-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:color :species})
   pj/plan)))


(def v80_l283 (def bar-layer (-> bar-plan :panels first :layers first)))


(def v82_l287 bar-layer)


(deftest
 t83_l289
 (is
  ((fn
    [m]
    (and
     (= :rect (:mark m))
     (= :dodge (:position m))
     (= 3 (count (:categories m)))))
   v82_l287)))


(def
 v85_l295
 (mapv
  (fn [g] {:label (:label g), :counts (:counts g)})
  (:groups bar-layer)))


(deftest t86_l300 (is ((fn [gs] (= 3 (count gs))) v85_l295)))


(def
 v88_l310
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t89_l313
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v88_l310)))


(def
 v91_l321
 (def
  stacked-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:position :stack, :color :species})
   pj/plan)))


(def
 v92_l325
 (def stacked-layer (-> stacked-plan :panels first :layers first)))


(def v93_l327 (:position stacked-layer))


(deftest t94_l329 (is ((fn [p] (= :stack p)) v93_l327)))


(def
 v96_l338
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t97_l342
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v96_l338)))


(def
 v98_l346
 (def
  lm-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def v100_l353 (mapv :mark (-> lm-plan :panels first :layers)))


(deftest
 t101_l354
 (is ((fn [marks] (= [:point :line] marks)) v100_l353)))


(def v102_l355 (def lm-layer (-> lm-plan :panels first :layers second)))


(def v104_l359 (first (:groups lm-layer)))


(deftest
 t105_l361
 (is
  ((fn
    [m]
    (and (< (:x1 m) (:x2 m)) (number? (:x1 m)) (number? (:y2 m))))
   v104_l359)))


(def
 v107_l373
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l378
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v107_l373)))


(def
 v109_l381
 (def
  grp-plan
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def
 v110_l387
 (let
  [line-layer (-> grp-plan :panels first :layers second)]
  (mapv
   (fn
    [g]
    {:color (:color g),
     :x1 (some-> (:x1 g) (Math/round) int),
     :x2 (some-> (:x2 g) (Math/round) int)})
   (:groups line-layer))))


(deftest t111_l394 (is ((fn [gs] (= 3 (count gs))) v110_l387)))


(def
 v113_l402
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__90249#] (Math/sin (* p1__90249# 0.3))) (range 30))}))


(def v114_l405 (-> wave (pj/lay-line :x :y)))


(deftest
 t115_l408
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v114_l405)))


(def v116_l412 (def wave-plan (-> wave (pj/lay-line :x :y) pj/plan)))


(def
 v117_l416
 (def
  wave-group
  (-> wave-plan :panels first :layers first :groups first)))


(def
 v118_l418
 {:n-points (count (:xs wave-group)),
  :first-x (first (:xs wave-group)),
  :last-x (last (:xs wave-group))})


(deftest t119_l422 (is ((fn [m] (= 30 (:n-points m))) v118_l418)))


(def
 v121_l431
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v122_l434 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t123_l437
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v122_l434)))


(def
 v124_l441
 (def sales-plan (-> sales (pj/lay-bar :product :revenue) pj/plan)))


(def
 v125_l445
 (let
  [g (-> sales-plan :panels first :layers first :groups first)]
  {:xs (:xs g), :ys (:ys g)}))


(deftest t126_l449 (is ((fn [m] (= 4 (count (:xs m)))) v125_l445)))


(def
 v128_l455
 (def
  flip-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/coord :flip)
   pj/plan)))


(def v129_l460 (:coord (first (:panels flip-plan))))


(deftest t130_l462 (is ((fn [c] (= :flip c)) v129_l460)))


(def
 v132_l466
 (let
  [p (first (:panels flip-plan))]
  {:x-domain-type
   (if (number? (first (:x-domain p))) :numeric :categorical),
   :y-domain-type
   (if (number? (first (:y-domain p))) :numeric :categorical)}))


(deftest
 t133_l470
 (is
  ((fn
    [m]
    (and
     (= :numeric (:x-domain-type m))
     (= :categorical (:y-domain-type m))))
   v132_l466)))


(def
 v135_l480
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


(def v136_l488 opts-plan)


(deftest
 t137_l490
 (is
  ((fn
    [m]
    (and
     (= "My Custom Title" (:title m))
     (= 800 (:width m))
     (= 300 (:height m))))
   v136_l488)))


(def v139_l496 (:layout opts-plan))


(deftest
 t140_l498
 (is
  ((fn
    [lay]
    (and
     (pos? (:title-pad lay))
     (pos? (:x-label-pad lay))
     (pos? (:y-label-pad lay))))
   v139_l496)))


(def
 v142_l509
 (def
  final-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def
 v143_l515
 (def final-plan (pj/plan final-pose {:title "Iris Petals"})))


(def v144_l517 final-plan)


(deftest
 t145_l519
 (is ((fn [m] (= "Iris Petals" (:title m))) v144_l517)))


(def
 v147_l523
 (mapv
  (fn [l] {:mark (:mark l), :n-groups (count (:groups l))})
  (-> final-plan :panels first :layers)))


(deftest t148_l528 (is ((fn [ls] (= 2 (count ls))) v147_l523)))


(def v150_l532 (-> final-pose (pj/options {:title "Iris Petals"})))


(deftest
 t151_l534
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v150_l532)))


(def
 v153_l544
 (def
  faceted-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   pj/plan)))


(def v155_l552 (:grid faceted-plan))


(deftest
 t156_l554
 (is ((fn [g] (and (= 1 (:rows g)) (= 3 (:cols g)))) v155_l552)))


(def v158_l558 (count (:panels faceted-plan)))


(deftest t159_l560 (is ((fn [n] (= 3 n)) v158_l558)))


(def v161_l564 (:panels faceted-plan))


(deftest
 t162_l566
 (is
  ((fn
    [ps]
    (and
     (= 3 (count ps))
     (every? :col-label ps)
     (= [[0 0] [0 1] [0 2]] (mapv (juxt :row :col) ps))))
   v161_l564)))


(def v164_l572 (:panels faceted-plan))


(deftest t165_l574 (is ((fn [ps] (every? :x-domain ps)) v164_l572)))


(def v167_l580 (mapv :y-domain (:panels faceted-plan)))


(deftest t168_l582 (is ((fn [ds] (apply = ds)) v167_l580)))


(def
 v170_l587
 (def
  free-y-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   (pj/options {:share-scales #{:x}})
   pj/plan)))


(def v171_l594 (mapv :y-domain (:panels free-y-plan)))


(deftest t172_l596 (is ((fn [ds] (not (apply = ds))) v171_l594)))


(def
 v174_l601
 (select-keys
  faceted-plan
  [:layout-type :grid :total-width :total-height]))


(deftest
 t175_l603
 (is ((fn [m] (= :facet-grid (:layout-type m))) v174_l601)))


(def v177_l607 (pj/valid-plan? faceted-plan))


(deftest t178_l609 (is (true? v177_l607)))


(def v180_l619 (pj/valid-plan? tiny-plan))


(deftest t181_l621 (is (true? v180_l619)))


(def v182_l623 (pj/valid-plan? iris-plan))


(deftest t183_l625 (is (true? v182_l623)))


(def v184_l627 (pj/valid-plan? hist-plan))


(deftest t185_l629 (is (true? v184_l627)))


(def v186_l631 (pj/valid-plan? bar-plan))


(deftest t187_l633 (is (true? v186_l631)))


(def v188_l635 (pj/valid-plan? lm-plan))


(deftest t189_l637 (is (true? v188_l635)))


(def v190_l639 (pj/valid-plan? final-plan))


(deftest t191_l641 (is (true? v190_l639)))


(def
 v193_l645
 (pj/explain-plan (assoc tiny-plan :width "not-a-number")))


(deftest t194_l647 (is (some? v193_l645)))


(def
 v196_l656
 (-> tiny-plan :panels first :layers first :groups first :xs type))


(deftest
 t197_l658
 (is ((fn [t] (not= clojure.lang.PersistentVector t)) v196_l656)))


(def
 v199_l662
 (-> tiny-plan :panels first :layers first :groups first :xs vec))


(deftest
 t200_l664
 (is ((fn [v] (and (vector? v) (number? (first v)))) v199_l662)))
