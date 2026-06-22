(ns
 plotje-book.membranes-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.membrane :as plotje-mem]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [membrane.ui :as ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l37
 (def
  iris-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Iris", :y-label "width"}))))


(def v4_l43 iris-pose)


(deftest
 t5_l45
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v4_l43)))


(def v7_l55 (def iris-membrane (pj/membrane iris-pose)))


(def v8_l57 (pj/membrane? iris-membrane))


(deftest t9_l59 (is (true? v8_l57)))


(def v11_l65 (def iris-plan (pj/plan iris-pose)))


(def v12_l67 (pj/membrane? (pj/plan->membrane iris-plan)))


(deftest t13_l69 (is (true? v12_l67)))


(def v15_l80 iris-membrane)


(def
 v17_l84
 {:width (ui/width iris-membrane),
  :height (ui/height iris-membrane),
  :origin (ui/origin iris-membrane),
  :title (:plotje/title iris-membrane),
  :n-drawables (count (ui/children iris-membrane))})


(deftest
 t18_l90
 (is
  ((fn
    [info]
    (and
     (= 600 (:width info))
     (= 400 (:height info))
     (= [0 0] (:origin info))
     (= "Iris" (:title info))
     (= 9 (:n-drawables info))))
   v17_l84)))


(def v20_l110 (sort (filter keyword? (keys iris-membrane))))


(deftest
 t21_l112
 (is
  ((fn [ks] (= [:drawables :height :width :plotje/title] ks))
   v20_l110)))


(def
 v23_l147
 (:plotje/title
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)))))


(deftest t24_l150 (is (nil? v23_l147)))


(def
 v26_l165
 (def
  two-up
  (ui/horizontal-layout
   (pj/membrane
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width {:color :species})
     (pj/options
      {:title "Sepal length vs sepal width", :y-label "width"})))
   (pj/membrane
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :petal-length {:color :species})
     (pj/options
      {:title "Sepal length vs petal length", :y-label "petal"}))))))


(def v28_l182 {:width (ui/width two-up), :height (ui/height two-up)})


(deftest
 t29_l185
 (is
  ((fn [info] (and (= 1201 (:width info)) (= 400 (:height info))))
   v28_l182)))


(def
 v31_l197
 (def
  two-up-png
  ((requiring-resolve 'membrane.java2d/draw-to-image)
   two-up
   [(ui/width two-up) (ui/height two-up)])))


(def v32_l202 (instance? java.awt.image.BufferedImage two-up-png))


(deftest t33_l204 (is (true? v32_l202)))


(def v35_l208 two-up-png)


(def v37_l223 (pj/membrane->plot iris-membrane :svg {}))


(deftest t38_l225 (is ((fn [v] (= :svg (first v))) v37_l223)))


(def v40_l231 (pj/membrane->plot iris-membrane :bufimg {}))


(deftest
 t41_l233
 (is ((fn [v] (instance? java.awt.image.BufferedImage v)) v40_l231)))


(def
 v43_l262
 (def
  clipped-membrane
  (->
   {:x [1 2 3 4 5], :y [10 20 15 25 18]}
   (pj/lay-point :x :y)
   (pj/lay-line {:data {:x [1 5], :y [-200 300]}})
   (pj/lay-rug :x)
   (pj/scale :y {:type :linear, :domain [0 30]})
   pj/membrane)))


(def
 v45_l275
 (->>
  (ui/children clipped-membrane)
  (tree-seq coll? seq)
  (filter
   (fn* [p1__98903#] (instance? membrane.ui.ScissorView p1__98903#)))
  (mapv (fn* [p1__98904#] (select-keys p1__98904# [:offset :bounds])))))


(deftest
 t46_l280
 (is
  ((fn
    [rects]
    (and
     (= 2 (count rects))
     (some (fn* [p1__98905#] (= [0 0] (:offset p1__98905#))) rects)
     (some
      (fn* [p1__98906#] (every? pos? (:offset p1__98906#)))
      rects)))
   v45_l275)))


(def v48_l294 (plotje-mem/valid? iris-membrane))


(deftest t49_l296 (is (true? v48_l294)))


(def v51_l300 (some? (plotje-mem/explain {:not :a-membrane})))


(deftest t52_l302 (is (true? v51_l300)))
