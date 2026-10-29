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
       (pj/plan
        (pj/facet
         {:a [1 2 3], :b [3 1 2]}
         {:col {:x [:a :b]}, :row {:y [:a :b]}}))]
      (and
       (pj/composite-plan? matrix-plan)
       (= 4 (count (:sub-plots matrix-plan)))))))
   v14_l82)))


(def v17_l95 (:x-domain tiny-panel))


(deftest
 t18_l97
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v17_l95)))


(def v19_l99 (:y-domain tiny-panel))


(deftest
 t20_l101
 (is ((fn [d] (and (<= (first d) 1) (>= (second d) 5))) v19_l99)))


(def v22_l105 (:x-scale tiny-panel))


(deftest t23_l107 (is ((fn [s] (= :linear (:type s))) v22_l105)))


(def v25_l111 (:x-ticks tiny-panel))


(deftest
 t26_l113
 (is
  ((fn
    [t]
    (and
     (vector? (:values t))
     (vector? (:labels t))
     (= (count (:values t)) (count (:labels t)))))
   v25_l111)))


(def v28_l125 (def tiny-layer (first (:layers tiny-panel))))


(def v29_l127 tiny-layer)


(deftest t30_l129 (is ((fn [m] (= :point (:mark m))) v29_l127)))


(def v32_l134 (count (:groups tiny-layer)))


(deftest t33_l136 (is ((fn [n] (= 1 n)) v32_l134)))


(def v35_l141 (first (:groups tiny-layer)))


(deftest
 t36_l143
 (is
  ((fn
    [g]
    (and
     (= 4 (count (:color g)))
     (= [1 2 3 4 5] (mapv int (:xs g)))
     (= [2 4 1 5 3] (mapv int (:ys g)))))
   v35_l141)))


(def
 v38_l157
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t39_l160
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v38_l157)))


(def
 v40_l164
 (def
  iris-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v42_l170 iris-plan)


(deftest
 t43_l172
 (is
  ((fn
    [m]
    (and
     (= 3 (count (:entries (:legend m))))
     (= 1 (count (:panels m)))))
   v42_l170)))


(def
 v45_l177
 (def iris-layer (-> iris-plan :panels first :layers first)))


(def v46_l179 (count (:groups iris-layer)))


(deftest t47_l181 (is ((fn [n] (= 3 n)) v46_l179)))


(def
 v49_l185
 (mapv
  (fn [g] {:color (:color g), :n-points (count (:xs g))})
  (:groups iris-layer)))


(deftest
 t50_l190
 (is
  ((fn
    [gs]
    (and
     (= 3 (count gs))
     (every? (fn* [p1__80274#] (= 50 (:n-points p1__80274#))) gs)))
   v49_l185)))


(def v52_l195 (:legend iris-plan))


(deftest
 t53_l197
 (is ((fn [leg] (= 3 (count (:entries leg)))) v52_l195)))


(def
 v55_l207
 (def
  cont-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
   pj/plan)))


(def v56_l211 (:legend cont-plan))


(deftest t57_l213 (is ((fn [m] (= :continuous (:type m))) v56_l211)))


(def
 v59_l217
 (select-keys
  (:legend cont-plan)
  [:title :type :min :max :color-range]))


(deftest
 t60_l219
 (is
  ((fn
    [m]
    (and (= :continuous (:type m)) (not (contains? m :gradient-fn))))
   v59_l217)))


(def v62_l224 (count (:stops (:legend cont-plan))))


(deftest t63_l226 (is ((fn [n] (= 20 n)) v62_l224)))


(def
 v65_l233
 (-> (rdatasets/datasets-iris) (pj/lay-histogram :sepal-length)))


(deftest
 t66_l236
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v65_l233)))


(def
 v67_l240
 (def
  hist-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-histogram :sepal-length)
   pj/plan)))


(def v68_l244 hist-plan)


(deftest t69_l246 (is ((fn [m] (= 1 (count (:panels m)))) v68_l244)))


(def
 v70_l248
 (def hist-layer (-> hist-plan :panels first :layers first)))


(def v71_l250 (:mark hist-layer))


(deftest t72_l252 (is ((fn [m] (= :bar m)) v71_l250)))


(def v74_l256 (-> hist-layer :groups first :bars))


(deftest
 t75_l258
 (is
  ((fn
    [bars]
    (and
     (> (count bars) 3)
     (every?
      (fn* [p1__80275#] (< (:lo p1__80275#) (:hi p1__80275#)))
      bars)
     (every? (fn* [p1__80276#] (pos? (:count p1__80276#))) bars)))
   v74_l256)))


(def
 v77_l270
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:color :species})))


(deftest
 t78_l273
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v77_l270)))


(def
 v79_l277
 (def
  bar-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:color :species})
   pj/plan)))


(def v80_l281 (def bar-layer (-> bar-plan :panels first :layers first)))


(def v82_l285 bar-layer)


(deftest
 t83_l287
 (is
  ((fn
    [m]
    (and
     (= :rect (:mark m))
     (= :dodge (:position m))
     (= 3 (count (:categories m)))))
   v82_l285)))


(def
 v85_l293
 (mapv
  (fn [g] {:label (:label g), :counts (:counts g)})
  (:groups bar-layer)))


(deftest t86_l298 (is ((fn [gs] (= 3 (count gs))) v85_l293)))


(def
 v88_l308
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :island {:position :stack, :color :species})))


(deftest
 t89_l311
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v88_l308)))


(def
 v91_l319
 (def
  stacked-plan
  (->
   (rdatasets/palmerpenguins-penguins)
   (pj/lay-bar :island {:position :stack, :color :species})
   pj/plan)))


(def
 v92_l323
 (def stacked-layer (-> stacked-plan :panels first :layers first)))


(def v93_l325 (:position stacked-layer))


(deftest t94_l327 (is ((fn [p] (= :stack p)) v93_l325)))


(def
 v96_l336
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t97_l340
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v96_l336)))


(def
 v98_l344
 (def
  lm-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def v100_l351 (mapv :mark (-> lm-plan :panels first :layers)))


(deftest
 t101_l352
 (is ((fn [marks] (= [:point :line] marks)) v100_l351)))


(def v102_l353 (def lm-layer (-> lm-plan :panels first :layers second)))


(def v104_l357 (first (:groups lm-layer)))


(deftest
 t105_l359
 (is
  ((fn
    [m]
    (and (< (:x1 m) (:x2 m)) (number? (:x1 m)) (number? (:y2 m))))
   v104_l357)))


(def
 v107_l371
 (->
  (rdatasets/datasets-iris)
  (pj/pose :petal-length :petal-width {:color :species})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t108_l376
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v107_l371)))


(def
 v109_l379
 (def
  grp-plan
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model})
   pj/plan)))


(def
 v110_l385
 (let
  [line-layer (-> grp-plan :panels first :layers second)]
  (mapv
   (fn
    [g]
    {:color (:color g),
     :x1 (some-> (:x1 g) (Math/round) int),
     :x2 (some-> (:x2 g) (Math/round) int)})
   (:groups line-layer))))


(deftest t111_l392 (is ((fn [gs] (= 3 (count gs))) v110_l385)))


(def
 v113_l400
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__80277#] (Math/sin (* p1__80277# 0.3))) (range 30))}))


(def v114_l403 (-> wave (pj/lay-line :x :y)))


(deftest
 t115_l406
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v114_l403)))


(def v116_l410 (def wave-plan (-> wave (pj/lay-line :x :y) pj/plan)))


(def
 v117_l414
 (def
  wave-group
  (-> wave-plan :panels first :layers first :groups first)))


(def
 v118_l416
 {:n-points (count (:xs wave-group)),
  :first-x (first (:xs wave-group)),
  :last-x (last (:xs wave-group))})


(deftest t119_l420 (is ((fn [m] (= 30 (:n-points m))) v118_l416)))


(def
 v121_l429
 (def
  sales
  {:product [:widget :gadget :gizmo :doohickey],
   :revenue [120 340 210 95]}))


(def v122_l432 (-> sales (pj/lay-bar :product :revenue)))


(deftest
 t123_l435
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:polygons s)))))
   v122_l432)))


(def
 v124_l439
 (def sales-plan (-> sales (pj/lay-bar :product :revenue) pj/plan)))


(def
 v125_l443
 (let
  [g (-> sales-plan :panels first :layers first :groups first)]
  {:xs (:xs g), :ys (:ys g)}))


(deftest t126_l447 (is ((fn [m] (= 4 (count (:xs m)))) v125_l443)))


(def
 v128_l453
 (def
  flip-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-bar :species)
   (pj/coord :flip)
   pj/plan)))


(def v129_l458 (:coord (first (:panels flip-plan))))


(deftest t130_l460 (is ((fn [c] (= :flip c)) v129_l458)))


(def
 v132_l464
 (let
  [p (first (:panels flip-plan))]
  {:x-domain-type
   (if (number? (first (:x-domain p))) :numeric :categorical),
   :y-domain-type
   (if (number? (first (:y-domain p))) :numeric :categorical)}))


(deftest
 t133_l468
 (is
  ((fn
    [m]
    (and
     (= :numeric (:x-domain-type m))
     (= :categorical (:y-domain-type m))))
   v132_l464)))


(def
 v135_l478
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


(def v136_l486 opts-plan)


(deftest
 t137_l488
 (is
  ((fn
    [m]
    (and
     (= "My Custom Title" (:title m))
     (= 800 (:width m))
     (= 300 (:height m))))
   v136_l486)))


(def v139_l494 (:layout opts-plan))


(deftest
 t140_l496
 (is
  ((fn
    [lay]
    (and
     (pos? (:title-pad lay))
     (pos? (:x-label-pad lay))
     (pos? (:y-label-pad lay))))
   v139_l494)))


(def
 v142_l507
 (def
  final-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def
 v143_l513
 (def final-plan (pj/plan final-pose {:title "Iris Petals"})))


(def v144_l515 final-plan)


(deftest
 t145_l517
 (is ((fn [m] (= "Iris Petals" (:title m))) v144_l515)))


(def
 v147_l521
 (mapv
  (fn [l] {:mark (:mark l), :n-groups (count (:groups l))})
  (-> final-plan :panels first :layers)))


(deftest t148_l526 (is ((fn [ls] (= 2 (count ls))) v147_l521)))


(def v150_l530 (-> final-pose (pj/options {:title "Iris Petals"})))


(deftest
 t151_l532
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v150_l530)))


(def
 v153_l542
 (def
  faceted-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   pj/plan)))


(def v155_l550 (:grid faceted-plan))


(deftest
 t156_l552
 (is ((fn [g] (and (= 1 (:rows g)) (= 3 (:cols g)))) v155_l550)))


(def v158_l556 (count (:panels faceted-plan)))


(deftest t159_l558 (is ((fn [n] (= 3 n)) v158_l556)))


(def v161_l562 (:panels faceted-plan))


(deftest
 t162_l564
 (is
  ((fn [ps] (and (= 3 (count ps)) (every? :col-label ps))) v161_l562)))


(def v164_l569 (:panels faceted-plan))


(deftest t165_l571 (is ((fn [ps] (every? :x-domain ps)) v164_l569)))


(def v167_l577 (mapv :y-domain (:panels faceted-plan)))


(deftest t168_l579 (is ((fn [ds] (apply = ds)) v167_l577)))


(def
 v170_l584
 (def
  free-y-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/facet :species)
   (pj/options {:share-scales #{:x}})
   pj/plan)))


(def v171_l591 (mapv :y-domain (:panels free-y-plan)))


(deftest t172_l593 (is ((fn [ds] (not (apply = ds))) v171_l591)))


(def
 v174_l598
 (select-keys
  faceted-plan
  [:layout-type :grid :total-width :total-height]))


(deftest
 t175_l600
 (is ((fn [m] (= :facet-grid (:layout-type m))) v174_l598)))


(def v177_l604 (pj/valid-plan? faceted-plan))


(deftest t178_l606 (is (true? v177_l604)))


(def v180_l616 (pj/valid-plan? tiny-plan))


(deftest t181_l618 (is (true? v180_l616)))


(def v182_l620 (pj/valid-plan? iris-plan))


(deftest t183_l622 (is (true? v182_l620)))


(def v184_l624 (pj/valid-plan? hist-plan))


(deftest t185_l626 (is (true? v184_l624)))


(def v186_l628 (pj/valid-plan? bar-plan))


(deftest t187_l630 (is (true? v186_l628)))


(def v188_l632 (pj/valid-plan? lm-plan))


(deftest t189_l634 (is (true? v188_l632)))


(def v190_l636 (pj/valid-plan? final-plan))


(deftest t191_l638 (is (true? v190_l636)))


(def
 v193_l642
 (pj/explain-plan (assoc tiny-plan :width "not-a-number")))


(deftest t194_l644 (is (some? v193_l642)))


(def
 v196_l653
 (-> tiny-plan :panels first :layers first :groups first :xs type))


(deftest
 t197_l655
 (is ((fn [t] (not= clojure.lang.PersistentVector t)) v196_l653)))


(def
 v199_l659
 (-> tiny-plan :panels first :layers first :groups first :xs vec))


(deftest
 t200_l661
 (is ((fn [v] (and (vector? v) (number? (first v)))) v199_l659)))
