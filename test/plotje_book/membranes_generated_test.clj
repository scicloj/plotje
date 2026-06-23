(ns
 plotje-book.membranes-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [membrane.ui :as ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l34
 (def
  iris-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:title "Iris", :y-label "width"}))))


(def v4_l40 iris-pose)


(deftest
 t5_l42
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v4_l40)))


(def v7_l52 (def iris-membrane (pj/membrane iris-pose)))


(def v8_l54 (pj/membrane? iris-membrane))


(deftest t9_l56 (is (true? v8_l54)))


(def v11_l62 (def iris-plan (pj/plan iris-pose)))


(def v12_l64 (pj/membrane? (pj/plan->membrane iris-plan)))


(deftest t13_l66 (is (true? v12_l64)))


(def v15_l77 iris-membrane)


(def
 v17_l81
 {:width (ui/width iris-membrane),
  :height (ui/height iris-membrane),
  :origin (ui/origin iris-membrane),
  :title (:plotje/title iris-membrane),
  :n-drawables (count (ui/children iris-membrane))})


(deftest
 t18_l87
 (is
  ((fn
    [info]
    (and
     (= 600 (:width info))
     (= 400 (:height info))
     (= [0 0] (:origin info))
     (= "Iris" (:title info))
     (= 9 (:n-drawables info))))
   v17_l81)))


(def v20_l107 (sort (filter keyword? (keys iris-membrane))))


(deftest
 t21_l109
 (is
  ((fn [ks] (= [:drawables :height :width :plotje/title] ks))
   v20_l107)))


(def
 v23_l144
 (:plotje/title
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)))))


(deftest t24_l147 (is (nil? v23_l144)))


(def
 v26_l162
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


(def v28_l179 {:width (ui/width two-up), :height (ui/height two-up)})


(deftest
 t29_l182
 (is
  ((fn [info] (and (= 1201 (:width info)) (= 400 (:height info))))
   v28_l179)))


(def
 v31_l194
 (def
  two-up-png
  ((requiring-resolve 'membrane.java2d/draw-to-image)
   two-up
   [(ui/width two-up) (ui/height two-up)])))


(def v32_l199 (instance? java.awt.image.BufferedImage two-up-png))


(deftest t33_l201 (is (true? v32_l199)))


(def v35_l205 two-up-png)


(def v37_l220 (pj/membrane->plot iris-membrane :svg {}))


(deftest t38_l222 (is ((fn [v] (= :svg (first v))) v37_l220)))


(def v40_l228 (pj/membrane->plot iris-membrane :bufimg {}))


(deftest
 t41_l230
 (is ((fn [v] (instance? java.awt.image.BufferedImage v)) v40_l228)))


(def
 v43_l259
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
 v45_l272
 (->>
  (ui/children clipped-membrane)
  (tree-seq coll? seq)
  (filter
   (fn* [p1__98536#] (instance? membrane.ui.ScissorView p1__98536#)))
  (mapv (fn* [p1__98537#] (select-keys p1__98537# [:offset :bounds])))))


(deftest
 t46_l277
 (is
  ((fn
    [rects]
    (and
     (= 2 (count rects))
     (some (fn* [p1__98538#] (= [0 0] (:offset p1__98538#))) rects)
     (some
      (fn* [p1__98539#] (every? pos? (:offset p1__98539#)))
      rects)))
   v45_l272)))


(def v48_l291 (pj/valid-membrane? iris-membrane))


(deftest t49_l293 (is (true? v48_l291)))


(def v51_l297 (some? (pj/explain-membrane {:not :a-membrane})))


(deftest t52_l299 (is (true? v51_l297)))
