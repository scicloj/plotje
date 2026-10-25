(ns
 plotje-book.series-generated-test
 (:require
  [scicloj.plotje.api :as pj]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def
 v3_l29
 (def
  sales
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4"],
    :revenue [120 150 140 190],
    :cost [90 100 115 120],
    :tax [18 24 21 30],
    :units [12 15 14 19]})))


(def v4_l36 sales)


(def
 v6_l43
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


(def v7_l52 sales-by-region)


(def v9_l59 (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))


(deftest
 t10_l62
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v9_l59)))


(def
 v12_l71
 (->
  sales
  (pj/lay-line :quarter :revenue)
  (pj/lay-line :quarter :cost)
  (pj/lay-line :quarter :tax)))


(deftest
 t13_l76
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (pj/lay-line sales :quarter [:revenue :cost :tax]))))
   v12_l71)))


(def
 v15_l92
 (->
  sales
  (tc/pivot->longer
   #{:revenue :tax :cost}
   {:target-columns :series, :value-column-name :value})
  (pj/lay-bar :quarter :value {:color :series, :position :dodge})))


(deftest
 t16_l97
 (is
  ((fn
    [v]
    (let
     [series
      (pj/svg-summary
       (->
        sales
        (pj/lay-bar
         :quarter
         [:revenue :cost :tax]
         {:position :dodge})))
      long
      (pj/svg-summary v)]
     (and
      (= (dissoc long :texts) (dissoc series :texts))
      (some #{"value"} (:texts long))
      (some #{"revenue, cost, tax"} (:texts series)))))
   v15_l92)))


(def
 v18_l116
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/options {:color-label "Measure"})))


(deftest
 t19_l120
 (is
  ((fn
    [v]
    (and
     (= "Measure" (:title (:legend (pj/plan v))))
     (=
      "Measure"
      (:title
       (:legend
        (pj/plan
         (->
          sales
          (pj/lay-line :quarter [:revenue :cost :tax])
          (pj/scale :color {:label "Measure"}))))))
     (=
      ""
      (:title
       (:legend
        (pj/plan
         (pj/lay-line sales :quarter [:revenue :cost :tax])))))))
   v18_l116)))


(def
 v21_l137
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t22_l142
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v21_l137)))


(def
 v24_l155
 (try
  (->
   sales
   (pj/lay-bar :quarter {:series [:revenue :cost], :label :measure}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t25_l161
 (is
  ((fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))
   v24_l155)))


(def
 v27_l171
 (->
  sales
  (pj/pose :quarter [:revenue :cost])
  pj/lay-line
  pj/lay-point))


(deftest
 t28_l176
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)) (= 8 (:points s)))))
   v27_l171)))


(def
 v30_l184
 (try
  (->
   sales
   (pj/pose {:x :quarter, :y [:revenue :cost]})
   (pj/lay-line :quarter [:revenue :tax]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t31_l191
 (is
  ((fn [msg] (re-find #"two pivots have no shared shape" msg))
   v30_l184)))


(def
 v33_l198
 (-> sales (pj/lay-bar {:x :quarter, :y [:revenue :cost :tax]})))


(deftest
 t34_l201
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v33_l198)))


(def
 v36_l210
 (-> sales (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))


(deftest
 t37_l213
 (is ((fn [v] (= 12 (:points (pj/svg-summary v)))) v36_l210)))


(def v39_l217 (-> sales (pj/lay-point [:revenue :cost :tax] :quarter)))


(deftest
 t40_l220
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))))
   v39_l217)))


(def
 v42_l240
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))


(deftest
 t43_l243
 (is
  ((fn
    [v]
    (let
     [top
      (fn [pose] (second (:y-domain (first (:panels (pj/plan pose))))))
      stacked
      (->
       sales
       (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))]
     (and
      (not=
       (pj/plot v)
       (pj/plot
        (-> sales (pj/lay-bar :quarter [:revenue :cost :tax]))))
      (<= 190 (top v) 338)
      (<= 338 (top stacked)))))
   v42_l240)))


(def
 v45_l257
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t46_l260
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v45_l257)))


(def
 v48_l266
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t49_l269
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v48_l266)))


(def
 v51_l274
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t52_l277
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v51_l274)))


(def
 v54_l290
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t55_l299
 (is
  ((fn
    [msg]
    (and
     (re-find
      #"key column :series, and the data already has a :series column"
      msg)
     (not (re-find #":value" msg))))
   v54_l290)))


(def
 v57_l307
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t58_l313
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v57_l307)))


(def
 v60_l330
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])))


(deftest
 t61_l336
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) texts (set (:texts s))]
     (and
      (= 1 (:panels s))
      (= 2 (:lines s))
      (contains? texts "time a / reading a")
      (contains? texts "time b / reading b")
      (contains? texts "time a, time b")
      (contains? texts "reading a, reading b"))))
   v60_l330)))


(def
 v63_l348
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])
  (pj/options {:x-label "Time (s)", :y-label "Reading"})))


(deftest
 t64_l355
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "Time (s)")
      (contains? texts "Reading")
      (not (contains? texts "x value")))))
   v63_l348)))


(def
 v66_l365
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/pose
   {:x [:time-a :time-b],
    :y {:series [:reading-a :reading-b], :as :sensor}})
  pj/lay-line))


(deftest
 t67_l373
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) texts (set (:texts s))]
     (and
      (= 1 (:panels s))
      (= 2 (:lines s))
      (contains? texts "sensor")
      (contains? texts "time a / reading a"))))
   v66_l365)))


(def
 v69_l384
 (try
  (->
   {:time-a [0 1], :time-b [2 3], :reading-a [1 2], :reading-b [3 4]}
   (pj/lay-line [:time-a :time-b] [:reading-a :reading-b :time-a]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t70_l390
 (is ((fn [msg] (re-find #"as many columns each" msg)) v69_l384)))


(def
 v72_l401
 (->>
  (pj/cross [:sepal-length :petal-length] [:sepal-width :petal-width])
  (apply map vector)
  (apply pj/lay-point (rdatasets/datasets-iris))))


(deftest
 t73_l405
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 600 (:points s))
      (=
       ["sepal length / sepal width"
        "sepal length / petal width"
        "petal length / sepal width"
        "petal length / petal width"]
       (mapv :label (:entries (:legend (pj/plan v))))))))
   v72_l401)))


(def
 v75_l426
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t76_l433
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-line reads \[:tax :units\] as series" msg)
     (re-find #"already reads \[:revenue :cost\]" msg)))
   v75_l426)))


(def
 v78_l439
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t79_l445
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v78_l439)))


(def v81_l458 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t82_l461
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v81_l458)))


(def v83_l463 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t84_l466
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v83_l463)))


(def
 v86_l470
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t87_l473
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v86_l470)))


(def v88_l475 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t89_l478
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v88_l475)))


(def
 v91_l483
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t92_l486
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v91_l483)))


(def v94_l494 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t95_l497
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v94_l494)))


(def
 v97_l507
 (try
  (pj/plot (-> sales (pj/lay-smooth :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t98_l512
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v97_l507)))


(def
 v100_l522
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t101_l525
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v100_l522)))


(def
 v103_l537
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t104_l540
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v103_l537)))


(def
 v106_l548
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t107_l551
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v106_l548)))


(def
 v109_l557
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t110_l561
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v109_l557)))


(def
 v112_l567
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t113_l571
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-point
        :quarter
        {:series [:revenue :cost :tax], :scale {:type :log}})))))
   v112_l567)))


(def
 v115_l580
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t116_l584
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v115_l580)))


(def
 v118_l589
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t119_l593
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v118_l589)))


(def
 v121_l600
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t122_l605
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 6 (:lines s))
      (=
       #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
       (disj (:colors s) "none")))))
   v121_l600)))


(def
 v124_l618
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t125_l621
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (:entries (:legend (pj/plan v))))))
   v124_l618)))


(def
 v127_l628
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t128_l632
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v127_l628)))


(def
 v130_l649
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t131_l654
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v130_l649)))


(def
 v133_l662
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  pj/draft
  :layers
  first
  (select-keys [:color :color-type])))


(deftest
 t134_l671
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v133_l662)))


(def
 v136_l686
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t137_l691
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v136_l686)))


(def
 v139_l695
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
    (pj/lay-point :quarter [:revenue :cost])))))


(deftest
 t140_l701
 (is
  ((fn
    [s]
    (re-find
     #"Removed 1 row with a missing value among the columns read as series \(:revenue, :cost\)"
     s))
   v139_l695)))


(def
 v142_l710
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q1" "Q2" "Q2"],
     :measure ["revenue" "cost" "revenue" "cost"],
     :value [120 90 nil 100]}
    (pj/lay-point :quarter :value {:color :measure})))))


(deftest
 t143_l716
 (is ((fn [s] (re-find #"Removed 1 row" s)) v142_l710)))


(def
 v145_l726
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t146_l730
 (is
  ((fn
    [v]
    (let
     [panels (mapcat (comp :panels :plan) (:sub-plots (pj/plan v)))]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (= 3 (count (distinct (map :y-domain panels)))))))
   v145_l726)))


(def
 v148_l739
 (->
  sales
  (pj/pose {:x :quarter})
  pj/lay-line
  (pj/facet {:col {:y [:revenue :cost :tax]}})))


(deftest
 t149_l744
 (is
  ((fn
    [v]
    (let
     [split
      (pj/plot
       (->
        sales
        (pj/lay-line :quarter [:revenue :cost :tax])
        (pj/facet :series)))]
     (and
      (= split (pj/plot v))
      (=
       split
       (pj/plot
        (->
         sales
         (pj/arrange
          [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
         pj/lay-line))))))
   v148_l739)))


(deftest
 t151_l759
 (is
  ((fn
    [_]
    (let
     [long-facet
      (->
       sales
       (tc/pivot->longer
        #{:revenue :tax :cost}
        {:target-columns :series, :value-column-name :value})
       (pj/lay-line :quarter :value)
       (pj/facet :series))]
     (=
      1
      (count
       (distinct (map :y-domain (:panels (pj/plan long-facet))))))))
   v148_l739)))


(def
 v153_l770
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)
  (pj/options {:share-scales {:y :all}})))


(deftest
 t154_l775
 (is
  ((fn
    [v]
    (let
     [panels (mapcat (comp :panels :plan) (:sub-plots (pj/plan v)))]
     (= 1 (count (distinct (map :y-domain panels))))))
   v153_l770)))


(def
 v156_l781
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t157_l785
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v156_l781)))


(def
 v159_l794
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t160_l797
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v159_l794)))


(def
 v162_l801
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t163_l804
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v162_l801)))


(def
 v165_l813
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t166_l816
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 8 (:points s))
      (= 4 (:polygons s))
      (nil? (:shape-legend (pj/plan v)))
      (every? :shape (:entries (:legend (pj/plan v)))))))
   v165_l813)))


(def
 v168_l831
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t169_l834
 (is
  ((fn
    [v]
    (and
     (true? (:tooltip (pj/plan v)))
     (=
      [[:revenue] [:cost] [:tax]]
      (mapv
       (fn [group] (vec (distinct (:tooltips group))))
       (-> v pj/plan :panels first :layers first :groups)))))
   v168_l831)))


(def
 v171_l843
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t172_l850
 (is
  ((fn
    [msg]
    (and
     (re-find #":size needs a numeric column" msg)
     (re-find
      #":alpha needs a numeric column"
      (try
       (do
        (pj/plot
         (->
          sales
          (pj/lay-point
           :quarter
           [:revenue :cost :tax]
           {:alpha :series})))
        "")
       (catch clojure.lang.ExceptionInfo e (ex-message e))))))
   v171_l843)))


(def
 v174_l872
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t175_l875
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v174_l872)))


(def
 v177_l882
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:group :region})))


(deftest
 t178_l885
 (is
  ((fn
    [v]
    (let
     [groups (:groups (first (:layers (first (:panels (pj/plan v))))))]
     (and
      (= 4 (count groups))
      (= 4 (count (distinct (map :dodge-idx groups))))
      (= 2 (count (distinct (map :color groups)))))))
   v177_l882)))


(def
 v180_l903
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t181_l906
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:lines s))
      (= 2 (count (disj (:colors s) "none")))
      (contains? (:colors s) "rgb(228,26,28)")
      (contains? (:colors s) "rgb(55,126,184)")
      (contains? (set (:texts s)) "region"))))
   v180_l903)))


(def
 v183_l919
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t184_l923
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v183_l919)))


(def
 v186_l935
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t187_l939
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v186_l935)))


(def
 v189_l948
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t190_l952
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v189_l948)))


(def
 v192_l956
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t193_l960
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v192_l956)))


(def
 v194_l962
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t195_l966
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v194_l962)))


(def
 v197_l975
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t198_l980
 (is
  ((fn
    [v]
    (and
     (= 2 (:panels (pj/svg-summary v)))
     (every?
      #{2}
      (vals
       (frequencies
        (map
         vector
         (sales-by-region :region)
         (sales-by-region :outlet)))))))
   v197_l975)))


(def
 v200_l991
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t201_l996
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v200_l991)))


(def
 v203_l1003
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t204_l1007
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v203_l1003)))


(def
 v206_l1016
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t207_l1023
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 2 (:panels s))
      (= 24 (:points s))
      (contains? (set (:texts s)) "measure")
      (contains? (set (:texts s)) "Euros")
      (= :log (-> v pj/plan :panels first :y-scale :type)))))
   v206_l1016)))


(def
 v209_l1039
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t210_l1043
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-line :quarter [:revenue :cost :tax])
       (pj/lay-point :quarter :value {:color :series})))))
   v209_l1039)))


(def
 v212_l1055
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t213_l1059
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v212_l1055)))


(def
 v215_l1068
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t216_l1072
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      points
      (second (:layers (first (:panels (pj/plan v)))))]
     (and
      (= 12 (:points s))
      (= 3 (:lines s))
      (= :point (:mark points))
      (= 1 (count (:groups points))))))
   v215_l1068)))


(def
 v218_l1089
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t219_l1093
 (is
  ((fn
    [v]
    (and
     (= 1 (:panels (pj/svg-summary v)))
     (= "revenue, cost, units" (:y-label (pj/plan v)))
     (=
      ["revenue" "cost" "units"]
      (mapv :label (:entries (:legend (pj/plan v)))))
     (re-find
      #"Note: the :y axis holds :revenue .*:cost .*:units"
      (with-out-str (pj/plan v)))))
   v218_l1089)))


(def
 v221_l1105
 (pj/arrange
  [(pj/lay-bar sales :quarter [:revenue :cost] {:position :stack})
   (pj/lay-line sales :quarter :units)]
  {:cols 1}))


(deftest
 t222_l1109
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v221_l1105)))


(def
 v224_l1115
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t225_l1120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:lines s)))))
   v224_l1115)))


(def
 v227_l1133
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t228_l1139
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v227_l1133)))


(def
 v230_l1144
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
 t231_l1154
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v230_l1144)))


(def
 v233_l1158
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
 t234_l1166
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v233_l1158)))


(def
 v236_l1170
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
 t237_l1179
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v236_l1170)))


(def
 v239_l1183
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t240_l1190
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v239_l1183)))


(def
 v242_l1194
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
 t243_l1209
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v242_l1194)))


(def
 v245_l1213
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
 t246_l1220
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v245_l1213)))


(def
 v248_l1227
 (pj/arrange
  [(-> sales (pj/lay-line :quarter [:revenue :cost]))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t249_l1231
 (is
  ((fn
    [v]
    (=
     {:panels 2, :lines 4}
     (select-keys (pj/svg-summary v) [:panels :lines])))
   v248_l1227)))


(def
 v251_l1246
 (kind/table
  {:column-names ["written" "what it reports"],
   :row-vectors
   (mapv
    (fn
     [[written f]]
     [(kind/code written)
      (try
       (do (pj/plot (f)) "draws")
       (catch Throwable e (first (str/split (ex-message e) #"\. "))))])
    [["{:color [:revenue :cost :tax]}"
      (fn*
       []
       (->
        sales
        (pj/lay-point
         :quarter
         :revenue
         {:color [:revenue :cost :tax]})))]
     ["{:series [:revenue]}"
      (fn* [] (-> sales (pj/lay-bar :quarter {:series [:revenue]})))]
     ["[:revenue 42]"
      (fn*
       []
       (-> sales (pj/lay-bar :quarter {:series [:revenue 42]})))]
     ["[:revenue :nope]"
      (fn* [] (-> sales (pj/lay-bar :quarter [:revenue :nope])))]
     ["a mapping already naming a consumed column"
      (fn*
       []
       (->
        sales
        (pj/pose :quarter :revenue)
        (pj/lay-bar :quarter [:revenue :cost :tax])))]
     ["a series on a composite pose"
      (fn*
       []
       (->
        (pj/arrange [(pj/lay-point sales :quarter :revenue)])
        (pj/lay-bar :quarter [:revenue :cost :tax])))]
     ["a pose carrying no data"
      (fn* [] (-> (pj/pose) (pj/lay-bar :quarter [:revenue :cost])))]
     ["{:y-min [:cost :tax]}"
      (fn*
       []
       (->
        sales
        (pj/lay-errorbar :quarter :revenue {:y-min [:cost :tax]})))]
     ["[:revenue :outlet]"
      (fn*
       []
       (->
        sales-by-region
        (pj/lay-bar :quarter [:revenue :outlet])))]])}))


(deftest
 t252_l1274
 (is
  ((fn
    [t]
    (and
     (= 9 (count (:row-vectors t)))
     (every?
      (fn [f] (try (pj/plot (f)) false (catch Throwable _ true)))
      [(fn*
        []
        (->
         sales
         (pj/lay-point
          :quarter
          :revenue
          {:color [:revenue :cost :tax]})))
       (fn* [] (-> sales (pj/lay-bar :quarter {:series [:revenue]})))
       (fn*
        []
        (-> sales (pj/lay-bar :quarter {:series [:revenue 42]})))
       (fn* [] (-> sales (pj/lay-bar :quarter [:revenue :nope])))
       (fn*
        []
        (->
         sales
         (pj/pose :quarter :revenue)
         (pj/lay-bar :quarter [:revenue :cost :tax])))
       (fn*
        []
        (->
         (pj/arrange [(pj/lay-point sales :quarter :revenue)])
         (pj/lay-bar :quarter [:revenue :cost :tax])))
       (fn* [] (-> (pj/pose) (pj/lay-bar :quarter [:revenue :cost])))
       (fn*
        []
        (->
         sales
         (pj/lay-errorbar :quarter :revenue {:y-min [:cost :tax]})))
       (fn*
        []
        (->
         sales-by-region
         (pj/lay-bar :quarter [:revenue :outlet])))])))
   v251_l1246)))


(def
 v254_l1299
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t255_l1302
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v254_l1299)))


(def
 v257_l1308
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t258_l1311
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v257_l1308)))
