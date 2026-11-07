(ns
 plotje-book.ggplot2-equivalents-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l38
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})))


(deftest
 t4_l43
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:lines s))
      (=
       (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
       (pj/pose
        (rdatasets/datasets-iris)
        {:x :sepal-length, :y :sepal-width})))))
   v3_l38)))


(def
 v6_l60
 (->
  (rdatasets/datasets-iris)
  (pj/pose :sepal-length :sepal-width)
  pj/lay-point
  (pj/lay-smooth
   {:stat :linear-model,
    :data
    (tc/select-rows
     (rdatasets/datasets-iris)
     (fn* [p1__88907#] (= "setosa" (:species p1__88907#))))})
  (pj/facet :species)))


(deftest
 t7_l68
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)) (= 3 (:lines s)))))
   v6_l60)))


(def
 v9_l93
 (->
  {:population [1000 5000 50000 200000 1000000 5000000],
   :area [2 8 30 120 500 2100]}
  (pj/lay-point :population :area)
  (pj/scale :x :log)
  (pj/scale :y {:domain [2500 0]})))


(deftest
 t10_l99
 (is
  ((fn
    [v]
    (let
     [panel (-> v pj/plan :panels first)]
     (and
      (= 6 (:points (pj/svg-summary v)))
      (= :log (-> panel :x-scale :type))
      (= [2500.0 0.0] (mapv double (:y-domain panel))))))
   v9_l93)))


(def
 v12_l114
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :y {:include 0})))


(deftest
 t13_l118
 (is
  ((fn
    [v]
    (and
     (= 0.0 (-> v pj/plan :panels first :y-domain first))
     (=
      #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
      (disj
       (:colors
        (pj/svg-summary
         (->
          (rdatasets/datasets-iris)
          (pj/lay-point :sepal-length :sepal-width {:color :species})
          (pj/scale
           :color
           {:values ["#E74C3C" "#3498DB" "#2ECC71"]}))))
       "none"))))
   v12_l114)))


(def
 v15_l152
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/scale :y {:domain [3 3.5]})))


(deftest
 t16_l156
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:clips s))
      (= 150 (:points (pj/svg-summary (pj/coord v :flip))))
      (= 150 (:points (pj/svg-summary (pj/coord v :fixed)))))))
   v15_l152)))


(def
 v18_l179
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet {:col :drv, :row :cyl})))


(deftest
 t19_l183
 (is
  ((fn
    [v]
    (let
     [mpg (rdatasets/ggplot2-mpg)]
     (and
      (=
       (* (count (distinct (mpg :drv))) (count (distinct (mpg :cyl))))
       (:panels (pj/svg-summary v)))
      (=
       2
       (:panels
        (pj/svg-summary
         (pj/arrange
          [(pj/lay-point mpg :displ :hwy)
           (pj/lay-point mpg :displ :cty)]))))
      (=
       4
       (:panels
        (pj/svg-summary
         (pj/matrix
          (rdatasets/datasets-iris)
          [:sepal-length :petal-length])))))))
   v18_l179)))


(def
 v21_l210
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Sepal Measurements",
    :x-label "Length (cm)",
    :y-label "Width (cm)",
    :color-label "Species"})))


(deftest
 t22_l217
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every?
      texts
      ["Iris Sepal Measurements"
       "Length (cm)"
       "Width (cm)"
       "Species"])))
   v21_l210)))


(def
 v24_l237
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/scale :x :sqrt)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t25_l245
 (is
  ((fn
    [m]
    (and
     (re-find #"Supported for :x: \[:categorical :linear :log\]" m)
     (=
      1
      (:dashed-lines
       (pj/svg-summary
        (pj/lay-line
         {:quarter [1 2 3 4], :revenue [3 5 4 6]}
         :quarter
         :revenue
         {:stroke-dash [5 5]}))))))
   v24_l237)))
