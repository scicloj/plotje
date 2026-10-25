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
 t10_l55
 (is ((fn [ks] (= [:data :layers :mapping] ks)) v9_l53)))


(deftest
 t12_l71
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
 v14_l84
 (def
  composite-pose
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :petal-length :petal-width)
    (pj/lay-line
     (rdatasets/datasets-iris)
     :petal-length
     :petal-width)])))


(def v15_l88 composite-pose)


(deftest
 t16_l90
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v15_l88)))


(def v18_l98 (kind/pprint composite-pose))


(def v19_l100 (-> composite-pose keys sort vec))


(deftest
 t20_l102
 (is ((fn [ks] (= [:layout :opts :poses] ks)) v19_l100)))


(def v22_l110 (-> composite-pose :poses count))


(deftest t23_l112 (is ((fn [n] (= 1 n)) v22_l110)))


(def
 v24_l114
 [(:layout composite-pose) (-> composite-pose :poses first :layout)])


(deftest
 t25_l117
 (is
  ((fn [ls] (= [{:direction :vertical} {:direction :horizontal}] ls))
   v24_l114)))


(def v27_l122 (-> composite-pose :poses first :poses count))


(deftest t28_l124 (is ((fn [n] (= 2 n)) v27_l122)))


(def
 v30_l130
 (-> composite-pose :poses first :poses first keys sort vec))


(deftest t31_l132 (is ((fn [ks] (= [:data :layers] ks)) v30_l130)))


(def
 v32_l134
 (-> composite-pose :poses first :poses first :layers first :mapping))


(deftest
 t33_l136
 (is ((fn [m] (= {:x :petal-length, :y :petal-width} m)) v32_l134)))


(def
 v35_l183
 (def
  jittered-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point
    :petal-length
    :petal-width
    {:jitter 5, :in :data, :size 5}))))


(def v36_l187 jittered-pose)


(deftest
 t37_l189
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v36_l187)))


(def v38_l191 (kind/pprint jittered-pose))


(def v40_l196 (-> jittered-pose :layers first :mapping))


(deftest
 t41_l198
 (is
  ((fn
    [m]
    (=
     {:x :petal-length, :y :petal-width, :jitter 5, :in :data, :size 5}
     m))
   v40_l196)))


(def v43_l217 (:mapping jittered-pose))


(deftest t44_l219 (is (nil? v43_l217)))


(def v46_l227 (def iris-draft (pj/draft iris-pose)))


(def v47_l229 (kind/pprint iris-draft))


(def v48_l231 (-> iris-draft :layers count))


(deftest t49_l233 (is ((fn [n] (= 2 n)) v48_l231)))


(def v51_l238 (-> iris-draft :layers first keys sort vec))


(deftest
 t52_l240
 (is
  ((fn
    [ks]
    (= [:__panel-idx :color :data :layer-type :mark :stat :x :y] ks))
   v51_l238)))


(def v54_l257 (-> iris-draft keys sort vec))


(deftest t55_l259 (is ((fn [ks] (= [:layers :opts] ks)) v54_l257)))


(def
 v57_l268
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width)
   (pj/lay-text {:x 2, :y 2, :text "a note"}))))


(def v58_l273 annotated)


(deftest
 t59_l275
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (contains? (set (:texts s)) "a note"))))
   v58_l273)))


(def v61_l283 (kind/pprint annotated))


(def v62_l285 (-> annotated :layers second :mapping))


(deftest
 t63_l287
 (is ((fn [m] (= {:x 2, :y 2, :text "a note"} m)) v62_l285)))


(def v65_l293 (def annotated-draft (pj/draft annotated)))


(def v66_l295 (kind/pprint annotated-draft))


(def
 v67_l297
 (-> annotated-draft :layers second (select-keys [:x :y :text])))


(deftest
 t68_l299
 (is ((fn [m] (= {:x :x, :y :y, :text :text} m)) v67_l297)))


(def v70_l307 (-> annotated-draft :layers second :data))


(deftest
 t71_l309
 (is
  ((fn
    [ds]
    (and
     (= [:x :y :text] (vec (tc/column-names ds)))
     (= 1 (tc/row-count ds))))
   v70_l307)))


(def v73_l339 (def composite-draft (pj/draft composite-pose)))


(def v74_l341 (kind/pprint composite-draft))


(def v75_l343 (-> composite-draft keys sort vec))


(deftest
 t76_l345
 (is
  ((fn [ks] (= [:chrome-spec :height :layout :sub-drafts :width] ks))
   v75_l343)))


(def v78_l351 (:layout composite-draft))


(deftest
 t79_l353
 (is
  ((fn
    [m]
    (= {[0 0] [0.0 0.0 300.0 400.0], [0 1] [300.0 0.0 300.0 400.0]} m))
   v78_l351)))


(def v81_l370 (-> iris-pose pj/plan pj/valid-plan?))


(deftest t82_l372 (is (true? v81_l370)))


(def
 v84_l376
 (->
  iris-pose
  pj/plan
  (assoc :width "not-a-number")
  pj/explain-plan
  some?))


(deftest t85_l378 (is (true? v84_l376)))
