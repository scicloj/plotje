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


(def
 v12_l71
 (def
  composite-pose
  (pj/arrange
   [(pj/lay-point (rdatasets/datasets-iris) :petal-length :petal-width)
    (pj/lay-line
     (rdatasets/datasets-iris)
     :petal-length
     :petal-width)])))


(def v13_l75 composite-pose)


(deftest
 t14_l77
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:lines s)))))
   v13_l75)))


(def v16_l85 (kind/pprint composite-pose))


(def v17_l87 (-> composite-pose keys sort vec))


(deftest t18_l89 (is ((fn [ks] (= [:layout :opts :poses] ks)) v17_l87)))


(def v20_l97 (-> composite-pose :poses count))


(deftest t21_l99 (is ((fn [n] (= 1 n)) v20_l97)))


(def
 v22_l101
 [(:layout composite-pose) (-> composite-pose :poses first :layout)])


(deftest
 t23_l104
 (is
  ((fn [ls] (= [{:direction :vertical} {:direction :horizontal}] ls))
   v22_l101)))


(def v25_l110 (-> composite-pose :poses first :poses count))


(deftest t26_l112 (is ((fn [n] (= 2 n)) v25_l110)))


(def
 v27_l114
 (-> composite-pose :poses first :poses first keys sort vec))


(deftest
 t28_l116
 (is ((fn [ks] (= [:data :layers :mapping] ks)) v27_l114)))


(def
 v30_l163
 (def
  jittered-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point
    :petal-length
    :petal-width
    {:jitter 5, :in :data, :size 5}))))


(def v31_l167 jittered-pose)


(deftest
 t32_l169
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v31_l167)))


(def v33_l171 (kind/pprint jittered-pose))


(def v35_l175 (-> jittered-pose :layers first :mapping))


(deftest
 t36_l177
 (is ((fn [m] (= {:jitter 5, :in :data, :size 5} m)) v35_l175)))


(def v38_l196 (:mapping jittered-pose))


(deftest
 t39_l198
 (is ((fn [m] (= {:x :petal-length, :y :petal-width} m)) v38_l196)))


(def v41_l207 (def iris-draft (pj/draft iris-pose)))


(def v42_l209 (kind/pprint iris-draft))


(def v43_l211 (-> iris-draft :layers count))


(deftest t44_l213 (is ((fn [n] (= 2 n)) v43_l211)))


(def v46_l218 (-> iris-draft :layers first keys sort vec))


(deftest
 t47_l220
 (is
  ((fn
    [ks]
    (= [:__panel-idx :color :data :layer-type :mark :stat :x :y] ks))
   v46_l218)))


(def v49_l237 (-> iris-draft keys sort vec))


(deftest t50_l239 (is ((fn [ks] (= [:layers :opts] ks)) v49_l237)))


(def
 v52_l248
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width)
   (pj/lay-text {:x 2, :y 2, :text "a note"}))))


(def v53_l253 annotated)


(deftest
 t54_l255
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (contains? (set (:texts s)) "a note"))))
   v53_l253)))


(def v56_l263 (kind/pprint annotated))


(def v57_l265 (-> annotated :layers second :mapping))


(deftest
 t58_l267
 (is ((fn [m] (= {:x 2, :y 2, :text "a note"} m)) v57_l265)))


(def v60_l273 (def annotated-draft (pj/draft annotated)))


(def v61_l275 (kind/pprint annotated-draft))


(def
 v62_l277
 (-> annotated-draft :layers second (select-keys [:x :y :text])))


(deftest
 t63_l279
 (is ((fn [m] (= {:x :x, :y :y, :text :text} m)) v62_l277)))


(def v65_l287 (-> annotated-draft :layers second :data))


(deftest
 t66_l289
 (is
  ((fn
    [ds]
    (and
     (= [:x :y :text] (vec (tc/column-names ds)))
     (= 1 (tc/row-count ds))))
   v65_l287)))


(def v68_l319 (def composite-draft (pj/draft composite-pose)))


(def v69_l321 (kind/pprint composite-draft))


(def v70_l323 (-> composite-draft keys sort vec))


(deftest
 t71_l325
 (is
  ((fn [ks] (= [:chrome-spec :height :layout :sub-drafts :width] ks))
   v70_l323)))


(def v73_l331 (:layout composite-draft))


(deftest
 t74_l333
 (is
  ((fn
    [m]
    (= {[0 0] [0.0 0.0 300.0 400.0], [0 1] [300.0 0.0 300.0 400.0]} m))
   v73_l331)))


(def v76_l350 (-> iris-pose pj/plan pj/valid-plan?))


(deftest t77_l352 (is (true? v76_l350)))


(def
 v79_l356
 (->
  iris-pose
  pj/plan
  (assoc :width "not-a-number")
  pj/explain-plan
  some?))


(deftest t80_l358 (is (true? v79_l356)))
