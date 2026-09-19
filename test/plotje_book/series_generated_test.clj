(ns
 plotje-book.series-generated-test
 (:require
  [scicloj.plotje.api :as pj]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l21
 (def
  sales
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4"],
    :revenue [120 150 140 190],
    :cost [90 100 115 120],
    :tax [18 24 21 30],
    :units [12 15 14 19]})))


(def v4_l28 sales)


(def
 v6_l35
 (def
  sales-by-region
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"],
    :region ["EU" "EU" "EU" "EU" "AS" "AS" "AS" "AS"],
    :outlet ["web" "web" "shop" "shop" "web" "web" "shop" "shop"],
    :revenue [120 150 140 190 90 120 160 210],
    :cost [90 100 115 120 70 85 110 130],
    :tax [18 24 21 30 14 19 26 34],
    :units [12 15 14 19 9 12 16 21]})))


(def v7_l44 sales-by-region)


(def v9_l51 (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))


(deftest
 t10_l54
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v9_l51)))


(def
 v12_l65
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))


(deftest
 t13_l68
 (is
  ((fn
    [v]
    (not=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v12_l65)))


(def
 v15_l75
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t16_l78
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v15_l75)))


(def
 v18_l84
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t19_l87
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v18_l84)))


(def
 v21_l92
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t22_l95
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v21_l92)))


(def v24_l100 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t25_l103
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v24_l100)))


(def v26_l105 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t27_l108
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v26_l105)))


(def
 v29_l116
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t30_l121
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v29_l116)))


(def
 v32_l128
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t33_l132
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v32_l128)))


(def
 v35_l143
 (try
  (->
   sales
   (pj/lay-bar :quarter {:series [:revenue :cost], :label :measure}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t36_l149
 (is
  ((fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))
   v35_l143)))


(def
 v38_l154
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t39_l158
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v38_l154)))


(def
 v41_l162
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t42_l165
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v41_l162)))


(def v43_l167 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t44_l170
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v43_l167)))


(def
 v46_l174
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t47_l178
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v46_l174)))


(def
 v49_l184
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t50_l188
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v49_l184)))


(def
 v52_l201
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t53_l210
 (is
  ((fn [msg] (re-find #"the data already has \[:series\]" msg))
   v52_l201)))


(def
 v55_l217
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t56_l223
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v55_l217)))


(def
 v58_l236
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t59_l240
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v58_l236)))


(def
 v61_l246
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t62_l250
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v61_l246)))


(def
 v63_l252
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t64_l256
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v63_l252)))


(def
 v66_l264
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t67_l267
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v66_l264)))


(def
 v69_l277
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t70_l280
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:lines s))
      (= 2 (count (disj (:colors s) "none")))
      (contains? (set (:texts s)) "region"))))
   v69_l277)))


(def
 v72_l291
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t73_l295
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v72_l291)))


(def
 v75_l307
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t76_l312
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v75_l307)))


(def
 v78_l316
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t79_l321
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v78_l316)))


(def
 v81_l328
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t82_l332
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v81_l328)))


(def
 v83_l334
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)
  pj/overlay))


(deftest
 t84_l339
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v83_l334)))


(def
 v86_l345
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t87_l350
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v86_l345)))


(def
 v89_l357
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t90_l363
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v89_l357)))


(def
 v92_l368
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
 t93_l378
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v92_l368)))


(def
 v95_l382
 (pj/arrange
  [(->
    sales-by-region
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))
   (->
    sales-by-region
    (pj/lay-line :quarter :units)
    (pj/facet :region))]))


(deftest
 t96_l390
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v95_l382)))


(def
 v98_l394
 (pj/arrange
  (vec
   (for
    [r ["EU" "AS"]]
    (->
     sales-by-region
     (tc/select-rows (fn [row] (= r (:region row))))
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
     (pj/options {:title r}))))
  {:share-scales #{:y}, :align-panels true}))


(deftest
 t99_l403
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v98_l394)))


(def
 v101_l407
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t102_l414
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v101_l407)))


(def
 v104_l418
 (pj/arrange
  [(pj/arrange
    [(->
      sales
      (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
     (-> sales (pj/lay-line :quarter [:revenue :cost]))]
    {:cols 1})
   (pj/arrange
    [(->
      sales
      (pj/lay-area :quarter [:revenue :cost] {:position :stack}))
     (-> sales (pj/lay-point :quarter :units))]
    {:cols 1})]
  {:title "Measures four ways"}))


(deftest
 t105_l433
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v104_l418)))


(def
 v107_l437
 (pj/arrange
  (vec
   (for
    [pos [:identity :dodge :stack :fill]]
    (->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position pos})
     (pj/options {:title (name pos)}))))
  {:cols 2}))


(deftest
 t108_l444
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v107_l437)))


(def
 v110_l451
 (->
  sales
  (pj/pose [[:quarter :revenue] [:quarter :cost]])
  (pj/lay-point)))


(deftest
 t111_l455
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v110_l451)))


(def
 v113_l459
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t114_l462
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v113_l459)))
