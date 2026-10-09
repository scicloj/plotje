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


(def v25_l109 (-> composite-pose :poses first :poses count))


(deftest t26_l111 (is ((fn [n] (= 2 n)) v25_l109)))


(def
 v28_l117
 (-> composite-pose :poses first :poses first keys sort vec))


(deftest t29_l119 (is ((fn [ks] (= [:data :layers] ks)) v28_l117)))


(def
 v30_l121
 (-> composite-pose :poses first :poses first :layers first :mapping))


(deftest
 t31_l123
 (is ((fn [m] (= {:x :petal-length, :y :petal-width} m)) v30_l121)))


(def
 v33_l170
 (def
  jittered-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point
    :petal-length
    :petal-width
    {:jitter 5, :in :data, :size 5}))))


(def v34_l174 jittered-pose)


(deftest
 t35_l176
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v34_l174)))


(def v36_l178 (kind/pprint jittered-pose))


(def v38_l183 (-> jittered-pose :layers first :mapping))


(deftest
 t39_l185
 (is
  ((fn
    [m]
    (=
     {:x :petal-length, :y :petal-width, :jitter 5, :in :data, :size 5}
     m))
   v38_l183)))


(def v41_l204 (:mapping jittered-pose))


(deftest t42_l206 (is (nil? v41_l204)))


(def v44_l214 (def iris-draft (pj/draft iris-pose)))


(def v45_l216 (kind/pprint iris-draft))


(def v46_l218 (-> iris-draft :layers count))


(deftest t47_l220 (is ((fn [n] (= 2 n)) v46_l218)))


(def v49_l225 (-> iris-draft :layers first keys sort vec))


(deftest
 t50_l227
 (is
  ((fn
    [ks]
    (= [:__panel-idx :color :data :layer-type :mark :stat :x :y] ks))
   v49_l225)))


(def v52_l244 (-> iris-draft keys sort vec))


(deftest t53_l246 (is ((fn [ks] (= [:layers :opts] ks)) v52_l244)))


(def
 v55_l255
 (def
  annotated
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :petal-length :petal-width)
   (pj/lay-text {:x 2, :y 2, :text "a note"}))))


(def v56_l260 annotated)


(deftest
 t57_l262
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (contains? (set (:texts s)) "a note"))))
   v56_l260)))


(def v59_l270 (kind/pprint annotated))


(def v60_l272 (-> annotated :layers second :mapping))


(deftest
 t61_l274
 (is ((fn [m] (= {:x 2, :y 2, :text "a note"} m)) v60_l272)))


(def v63_l280 (def annotated-draft (pj/draft annotated)))


(def v64_l282 (kind/pprint annotated-draft))


(def
 v65_l284
 (-> annotated-draft :layers second (select-keys [:x :y :text])))


(deftest
 t66_l286
 (is ((fn [m] (= {:x :x, :y :y, :text :text} m)) v65_l284)))


(def v68_l294 (-> annotated-draft :layers second :data))


(deftest
 t69_l296
 (is
  ((fn
    [ds]
    (and
     (= [:x :y :text] (vec (tc/column-names ds)))
     (= 1 (tc/row-count ds))))
   v68_l294)))


(def v71_l326 (def composite-draft (pj/draft composite-pose)))


(def v72_l328 (kind/pprint composite-draft))


(def v73_l330 (-> composite-draft keys sort vec))


(deftest
 t74_l332
 (is
  ((fn [ks] (= [:chrome-spec :height :layout :sub-drafts :width] ks))
   v73_l330)))


(def v76_l338 (:layout composite-draft))


(deftest
 t77_l340
 (is
  ((fn
    [m]
    (= {[0 0] [0.0 0.0 300.0 400.0], [0 1] [300.0 0.0 300.0 400.0]} m))
   v76_l338)))


(def v79_l357 (-> iris-pose pj/plan pj/valid-plan?))


(deftest t80_l359 (is (true? v79_l357)))


(def
 v82_l363
 (->
  iris-pose
  pj/plan
  (assoc :width "not-a-number")
  pj/explain-plan
  some?))


(deftest t83_l365 (is (true? v82_l363)))
