(ns
 plotje-book.specifying-aesthetics-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure.test :refer [deftest is]]))


(def
 v3_l64
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t4_l67
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v3_l64)))


(def
 v6_l72
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :colour})
   pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t7_l78
 (is
  ((fn
    [m]
    (and
     (re-find #"Column :colour \(from :color\) not found" m)
     (re-find #":species" m)))
   v6_l72)))


(def
 v9_l90
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3], "blue" ["a" "b" "c"]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t10_l93
 (is
  ((fn [v] (= 3 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v9_l90)))


(def
 v12_l99
 (->
  (tc/dataset {"x" [1 2 3], "y" [1 2 3]})
  (pj/lay-point "x" "y" {:color "blue"})))


(deftest
 t13_l102
 (is
  ((fn
    [v]
    (= #{"rgb(0,0,255)"} (disj (:colors (pj/svg-summary v)) "none")))
   v12_l99)))


(def
 v15_l129
 (->
  (tc/dataset (map vector [1 2 3] [4 5 6]))
  (pj/lay-point {:x 0, :y 1})))


(deftest
 t16_l132
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v15_l129)))


(def
 v18_l138
 (->
  {:height [1 2 3], :weight [4 5 6]}
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 2.0, :y 5.5, :text "here"})))


(deftest
 t19_l142
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:points s)) (contains? (set (:texts s)) "here"))))
   v18_l138)))


(def
 v21_l169
 [(->
   {:height [1 2 3 4], :weight [1 2 3 4], :species ["a" "a" "b" "b"]}
   (pj/pose {:x :height, :y :weight, :color :species})
   pj/lay-point)
  (->
   {:height [1 2 3 4], :weight [1 2 3 4], :species ["a" "a" "b" "b"]}
   (pj/pose {:x :height, :y :weight, :color :species})
   (pj/lay-point {:color nil}))])


(deftest
 t22_l176
 (is
  ((fn
    [[coloured plain]]
    (and
     (= 2 (count (disj (:colors (pj/svg-summary coloured)) "none")))
     (= 1 (count (disj (:colors (pj/svg-summary plain)) "none")))))
   v21_l169)))


(def
 v24_l207
 (->
  (rdatasets/datasets-iris)
  (pj/lay-density :sepal-length {:color :species})))


(deftest
 t25_l210
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:polygons s))
      (= 3 (count (disj (:colors s) "none")))
      (=
       ["setosa" "versicolor" "virginica"]
       (filterv #{"versicolor" "setosa" "virginica"} (:texts s))))))
   v24_l207)))


(def
 v27_l223
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-width})))


(deftest
 t28_l226
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) texts (set (:texts s))]
     (and
      (= 150 (:points s))
      (contains? texts "0.1")
      (contains? texts "2.5")
      (not (contains? texts "setosa")))))
   v27_l223)))


(def
 v30_l246
 (->
  {:height [1 2 3 4], :weight [1 2 3 4], :zone [10 20 30 40]}
  (pj/lay-point
   :height
   :weight
   {:color :zone, :color-type :categorical})))


(deftest
 t31_l249
 (is
  ((fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v30_l246)))


(def
 v33_l262
 [(:accepts (pj/layer-type-lookup :point))
  (:accepts (pj/layer-type-lookup :line))
  (:accepts (pj/layer-type-lookup :bar))])


(deftest
 t34_l266
 (is
  ((fn
    [[point line bar]]
    (and
     (not-any? (fn [a] (contains? (set a) :color)) [point line bar])
     (contains? (set point) :shape)
     (not (contains? (set line) :shape))
     (contains? (set line) :stroke-dash)))
   v33_l262)))


(def
 v36_l282
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length :sepal-width {:shape :species})))


(deftest
 t37_l285
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v36_l282)))


(def
 v39_l302
 (->
  {:month (vec (concat (range 1 5) (range 1 5) (range 1 5))),
   :value [3 5 4 6 2 3 5 4 4 6 5 7],
   :country
   ["fr" "fr" "fr" "fr" "de" "de" "de" "de" "it" "it" "it" "it"]}
  (pj/lay-line :month :value {:group :country})))


(deftest
 t40_l307
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:lines s))
      (= 1 (count (disj (:colors s) "none")))
      (not-any? #{"it" "fr" "de"} (:texts s)))))
   v39_l302)))


(def
 v42_l318
 (->
  {:month (vec (concat (range 1 5) (range 1 5) (range 1 5))),
   :value [3 5 4 6 2 3 5 4 4 6 5 7],
   :country
   ["fr" "fr" "fr" "fr" "de" "de" "de" "de" "it" "it" "it" "it"]}
  (pj/lay-line :month :value {:group :country, :color "#d0d0d0"})))


(deftest
 t43_l323
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:lines s))
      (= #{"rgb(208,208,208)"} (disj (:colors s) "none")))))
   v42_l318)))
