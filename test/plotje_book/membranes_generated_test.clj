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


(def
 v15_l80
 (def
  themed-pose
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options
    {:title "Iris", :theme {:bg "aliceblue", :grid "#cccccc"}}))))


(def v16_l87 themed-pose)


(def
 v18_l92
 (=
  (pj/membrane themed-pose)
  (-> themed-pose pj/pose->draft pj/draft->membrane)))


(deftest t19_l97 (is (true? v18_l92)))


(def v21_l106 iris-membrane)


(def
 v23_l110
 {:width (ui/width iris-membrane),
  :height (ui/height iris-membrane),
  :origin (ui/origin iris-membrane),
  :title (:plotje/title iris-membrane),
  :n-drawables (count (ui/children iris-membrane))})


(deftest
 t24_l116
 (is
  ((fn
    [info]
    (and
     (= 600 (:width info))
     (= 400 (:height info))
     (= [0 0] (:origin info))
     (= "Iris" (:title info))
     (= 9 (:n-drawables info))))
   v23_l110)))


(def v26_l136 (sort (filter keyword? (keys iris-membrane))))


(deftest
 t27_l138
 (is
  ((fn [ks] (= [:drawables :height :width :plotje/title] ks))
   v26_l136)))


(def
 v29_l173
 (:plotje/title
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)))))


(deftest t30_l176 (is (nil? v29_l173)))


(def
 v32_l191
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


(def v34_l208 {:width (ui/width two-up), :height (ui/height two-up)})


(deftest
 t35_l211
 (is
  ((fn [info] (and (= 1201 (:width info)) (= 400 (:height info))))
   v34_l208)))


(def
 v37_l223
 (def
  two-up-png
  ((requiring-resolve 'membrane.java2d/draw-to-image)
   two-up
   [(ui/width two-up) (ui/height two-up)])))


(def v38_l228 (instance? java.awt.image.BufferedImage two-up-png))


(deftest t39_l230 (is (true? v38_l228)))


(def v41_l234 two-up-png)


(def v43_l248 (pj/membrane->plot iris-membrane :svg {}))


(deftest t44_l250 (is ((fn [v] (= :svg (first v))) v43_l248)))


(def v46_l256 (pj/membrane->plot iris-membrane :bufimg {}))


(deftest
 t47_l258
 (is ((fn [v] (instance? java.awt.image.BufferedImage v)) v46_l256)))


(def
 v49_l287
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
 v51_l300
 (->>
  (ui/children clipped-membrane)
  (tree-seq coll? seq)
  (filter
   (fn* [p1__89148#] (instance? membrane.ui.ScissorView p1__89148#)))
  (mapv (fn* [p1__89149#] (select-keys p1__89149# [:offset :bounds])))))


(deftest
 t52_l305
 (is
  ((fn
    [rects]
    (and
     (= 2 (count rects))
     (some (fn* [p1__89150#] (= [0 0] (:offset p1__89150#))) rects)
     (some
      (fn* [p1__89151#] (every? pos? (:offset p1__89151#)))
      rects)))
   v51_l300)))


(def v54_l321 (pj/valid-membrane? iris-membrane))


(deftest t55_l323 (is (true? v54_l321)))


(def v57_l328 (some? (pj/explain-membrane {:not :a-membrane})))


(deftest t58_l330 (is (true? v57_l328)))
