(ns
 plotje-book.series-generated-test
 (:require
  [scicloj.plotje.api :as pj]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l18
 (def
  sales
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"],
    :region ["EU" "EU" "EU" "EU" "AS" "AS" "AS" "AS"],
    :channel ["web" "web" "shop" "shop" "web" "web" "shop" "shop"],
    :revenue [120 150 140 190 90 120 160 210],
    :cost [90 100 115 120 70 85 110 130],
    :tax [18 24 21 30 14 19 26 34],
    :units [12 15 14 19 9 12 16 21]})))


(def v4_l27 sales)


(def v6_l31 (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))


(deftest
 t7_l34
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v6_l31)))


(def
 v9_l38
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t10_l41
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v9_l38)))


(def
 v11_l43
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t12_l46
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v11_l43)))


(def
 v13_l48
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t14_l51
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v13_l48)))


(def v16_l55 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t17_l58
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v16_l55)))


(def v18_l60 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t19_l63
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v18_l60)))


(def
 v21_l67
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t22_l72
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v21_l67)))


(def
 v24_l79
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t25_l83
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v24_l79)))


(def
 v27_l88
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t28_l92
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v27_l88)))


(def
 v30_l96
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t31_l100
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v30_l96)))


(def
 v32_l102
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :channel)))


(deftest
 t33_l106
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v32_l102)))


(def
 v35_l113
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/lay-point :quarter :value)))


(deftest
 t36_l117
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)))))
   v35_l113)))


(def
 v38_l123
 (-> sales (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t39_l126
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v38_l123)))


(def
 v41_l130
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet-grid :region :channel)
  (pj/options {:title "Measures by region and channel"})))


(deftest
 t42_l135
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v41_l130)))


(def
 v44_l142
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t45_l148
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v44_l142)))


(def
 v47_l153
 (pj/arrange
  [(pj/arrange
    [(->
      sales
      (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
     (->
      sales
      (pj/lay-bar :quarter [:revenue :cost] {:position :stack}))]
    {:cols 1})
   (-> sales (pj/lay-line :quarter [:revenue :cost :tax]))]))


(deftest
 t48_l163
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v47_l153)))


(def
 v50_l167
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))
   (-> sales (pj/lay-line :quarter :units) (pj/facet :region))]))


(deftest
 t51_l175
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v50_l167)))


(def
 v53_l179
 (pj/arrange
  (vec
   (for
    [r ["EU" "AS"]]
    (->
     sales
     (tc/select-rows (fn [row] (= r (:region row))))
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
     (pj/options {:title r}))))
  {:share-scales #{:y}, :align-panels true}))


(deftest
 t54_l188
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v53_l179)))


(def
 v56_l192
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t57_l199
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v56_l192)))


(def
 v59_l206
 (->
  sales
  (pj/pose [[:quarter :revenue] [:quarter :cost]])
  (pj/lay-point)))


(deftest
 t60_l210
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v59_l206)))


(def
 v62_l214
 (-> sales (pj/lay-line :quarter :revenue {:group [:region :channel]})))


(deftest
 t63_l217
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v62_l214)))
