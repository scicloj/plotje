(ns
 plotje-book.poses-and-drafts-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l32
 (def
  iris-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v4_l38 iris-pose)


(deftest
 t5_l40
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v4_l38)))


(def v7_l47 (kind/pprint iris-pose))


(def v9_l53 (-> iris-pose keys sort vec))


(deftest
 t10_l58
 (is ((fn [ks] (= [:data :layers :mapping] ks)) v9_l53)))


(deftest
 t12_l74
 (is
  ((fn
    [_]
    (let
     [p (pj/lay-point {:a [1 2], :b [3 4], :g ["x" "y"]} :a :b)]
     (and
      (=
       {:share-scales #{}}
       (:opts (pj/options p {:share-scales #{}})))
      (= {:coord :flip} (:opts (pj/coord p :flip)))
      (=
       {:y {:scale {:domain [0 9]}}}
       (:mapping (pj/scale p :y {:domain [0 9]})))
      (= {:col :g} (:mapping (pj/facet p :g)))
      (=
       {:y [:b]}
       (:facet
        (pj/facet
         (pj/pose {:a [1 2], :b [3 4]} {:x :a})
         {:y [:b]}))))))
   v9_l53)))


(def
 v14_l87
 (def
  composite-pose
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :petal-length :petal-width)
    (pj/lay-line
     (rdatasets/datasets-iris)
     :petal-length
     :petal-width)])))


(def v15_l91 composite-pose)


(deftest
 t16_l93
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v15_l91)))


(def v18_l101 (kind/pprint composite-pose))


(def v19_l103 (-> composite-pose keys sort vec))


(deftest t20_l108 (is ((fn [ks] (= [:layout :poses] ks)) v19_l103)))


(def v22_l116 (-> composite-pose :poses count))


(deftest t23_l120 (is ((fn [n] (= 1 n)) v22_l116)))


(def
 v25_l127
 [(:layout composite-pose) (-> composite-pose :poses first :layout)])


(deftest
 t26_l133
 (is
  ((fn
    [ls]
    (=
     [{:direction :vertical, :cols :auto} {:direction :horizontal}]
     ls))
   v25_l127)))


(def v28_l138 (-> composite-pose :poses first :poses count))


(deftest t29_l144 (is ((fn [n] (= 2 n)) v28_l138)))


(def
 v31_l150
 (-> composite-pose :poses first :poses first keys sort vec))


(deftest t32_l159 (is ((fn [ks] (= [:data :layers] ks)) v31_l150)))


(def
 v33_l161
 (-> composite-pose :poses first :poses first :layers first :mapping))


(deftest
 t34_l170
 (is ((fn [m] (= {:x :petal-length, :y :petal-width} m)) v33_l161)))


(def
 v36_l186
 (def
  jittered-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point
    :petal-length
    :petal-width
    {:jitter 5, :in :data, :size 5}))))


(def v37_l190 jittered-pose)


(deftest
 t38_l192
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v37_l190)))


(def v39_l194 (kind/pprint jittered-pose))


(def v41_l199 (-> jittered-pose :layers first :mapping))


(deftest
 t42_l204
 (is
  ((fn
    [m]
    (=
     {:x :petal-length, :y :petal-width, :jitter 5, :in :data, :size 5}
     m))
   v41_l199)))


(def v44_l223 (:mapping jittered-pose))


(deftest t45_l225 (is (nil? v44_l223)))


(def v47_l233 (def iris-draft (pj/draft iris-pose)))


(def v48_l235 (kind/pprint iris-draft))


(def v49_l237 (-> iris-draft :layers count))


(deftest t50_l241 (is ((fn [n] (= 2 n)) v49_l237)))


(def v52_l246 (-> iris-draft :layers first keys sort vec))


(deftest
 t53_l253
 (is
  ((fn
    [ks]
    (= [:__panel-idx :color :data :layer-type :mark :stat :x :y] ks))
   v52_l246)))


(def v55_l270 (-> iris-draft keys sort vec))


(deftest t56_l275 (is ((fn [ks] (= [:layers :opts] ks)) v55_l270)))


(def
 v58_l284
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width)
   (pj/lay-text {:x 2, :y 2, :text "a note"}))))


(def v59_l289 annotated)


(deftest
 t60_l291
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (contains? (set (:texts s)) "a note"))))
   v59_l289)))


(def v62_l299 (kind/pprint annotated))


(def v63_l301 (-> annotated :layers second :mapping))


(deftest
 t64_l306
 (is ((fn [m] (= {:x 2, :y 2, :text "a note"} m)) v63_l301)))


(def v66_l312 (def annotated-draft (pj/draft annotated)))


(def v67_l314 (kind/pprint annotated-draft))


(def
 v68_l316
 (-> annotated-draft :layers second (select-keys [:x :y :text])))


(deftest
 t69_l321
 (is ((fn [m] (= {:x :x, :y :y, :text :text} m)) v68_l316)))


(def v71_l329 (-> annotated-draft :layers second :data))


(deftest
 t72_l334
 (is
  ((fn
    [ds]
    (and
     (= [:x :y :text] (vec (tc/column-names ds)))
     (= 1 (tc/row-count ds))))
   v71_l329)))


(def v74_l364 (def composite-draft (pj/draft composite-pose)))


(def v75_l366 (kind/pprint composite-draft))


(def v76_l368 (-> composite-draft keys sort vec))


(deftest
 t77_l373
 (is
  ((fn [ks] (= [:chrome-spec :height :layout :sub-drafts :width] ks))
   v76_l368)))


(def v79_l379 (:layout composite-draft))


(deftest
 t80_l381
 (is
  ((fn
    [m]
    (= {[0 0] [0.0 0.0 300.0 400.0], [0 1] [300.0 0.0 300.0 400.0]} m))
   v79_l379)))


(def v82_l399 (-> iris-pose pj/plan pj/valid-plan?))


(deftest t83_l403 (is (true? v82_l399)))


(def
 v85_l407
 (->
  iris-pose
  pj/plan
  (assoc :width "not-a-number")
  pj/explain-plan
  some?))


(deftest t86_l413 (is (true? v85_l407)))
