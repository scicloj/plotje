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
  (tc/pivot->longer
   #{:revenue :tax :cost}
   {:target-columns :series, :value-column-name :value})
  (pj/lay-bar :quarter :value {:color :series, :position :dodge})))


(deftest
 t13_l76
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
   v12_l71)))


(def
 v15_l101
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t16_l106
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v15_l101)))


(def
 v18_l119
 (try
  (->
   sales
   (pj/lay-bar :quarter {:series [:revenue :cost], :label :measure}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t19_l125
 (is
  ((fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))
   v18_l119)))


(def
 v21_l135
 (->
  sales
  (pj/pose :quarter [:revenue :cost])
  pj/lay-line
  pj/lay-point))


(deftest
 t22_l140
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)) (= 8 (:points s)))))
   v21_l135)))


(def
 v24_l148
 (try
  (->
   sales
   (pj/pose {:x :quarter, :y [:revenue :cost]})
   (pj/lay-line :quarter [:revenue :tax]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t25_l155
 (is
  ((fn [msg] (re-find #"two pivots have no shared shape" msg))
   v24_l148)))


(def
 v27_l162
 (-> sales (pj/lay-bar {:x :quarter, :y [:revenue :cost :tax]})))


(deftest
 t28_l165
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v27_l162)))


(def
 v30_l174
 (-> sales (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))


(deftest
 t31_l177
 (is ((fn [v] (= 12 (:points (pj/svg-summary v)))) v30_l174)))


(def v33_l181 (-> sales (pj/lay-point [:revenue :cost :tax] :quarter)))


(deftest
 t34_l184
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))))
   v33_l181)))


(def
 v36_l204
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))


(deftest
 t37_l207
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
   v36_l204)))


(def
 v39_l221
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t40_l224
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v39_l221)))


(def
 v42_l230
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t43_l233
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v42_l230)))


(def
 v45_l238
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t46_l241
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v45_l238)))


(def
 v48_l254
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t49_l263
 (is
  ((fn
    [msg]
    (and
     (re-find
      #"key column :series, and the data already has a :series column"
      msg)
     (not (re-find #":value" msg))))
   v48_l254)))


(def
 v51_l271
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t52_l277
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v51_l271)))


(def
 v54_l294
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])))


(deftest
 t55_l300
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
   v54_l294)))


(def
 v57_l312
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])
  (pj/options {:x-label "Time (s)", :y-label "Reading"})))


(deftest
 t58_l319
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "Time (s)")
      (contains? texts "Reading")
      (not (contains? texts "x value")))))
   v57_l312)))


(def
 v60_l329
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
 t61_l337
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
   v60_l329)))


(def
 v63_l348
 (try
  (->
   {:time-a [0 1], :time-b [2 3], :reading-a [1 2], :reading-b [3 4]}
   (pj/lay-line [:time-a :time-b] [:reading-a :reading-b :time-a]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t64_l354
 (is ((fn [msg] (re-find #"as many columns each" msg)) v63_l348)))


(def
 v66_l365
 (->>
  (pj/cross [:sepal-length :petal-length] [:sepal-width :petal-width])
  (apply map vector)
  (apply pj/lay-point (rdatasets/datasets-iris))))


(deftest
 t67_l369
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
   v66_l365)))


(def
 v69_l388
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t70_l395
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-line reads \[:tax :units\] as series" msg)
     (re-find #"already reads \[:revenue :cost\]" msg)))
   v69_l388)))


(def
 v72_l401
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t73_l407
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v72_l401)))


(def v75_l420 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t76_l423
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v75_l420)))


(def v77_l425 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t78_l428
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v77_l425)))


(def
 v80_l432
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t81_l435
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v80_l432)))


(def v82_l437 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t83_l440
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v82_l437)))


(def
 v85_l445
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t86_l448
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v85_l445)))


(def v88_l456 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t89_l459
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v88_l456)))


(def
 v91_l469
 (try
  (pj/plot (-> sales (pj/lay-smooth :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t92_l474
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v91_l469)))


(def
 v94_l484
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t95_l487
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v94_l484)))


(def
 v97_l499
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t98_l502
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v97_l499)))


(def
 v100_l510
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t101_l513
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v100_l510)))


(def
 v103_l519
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t104_l523
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v103_l519)))


(def
 v106_l529
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t107_l533
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
   v106_l529)))


(def
 v109_l542
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t110_l546
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v109_l542)))


(def
 v112_l551
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t113_l555
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v112_l551)))


(def
 v115_l562
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t116_l567
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
   v115_l562)))


(def
 v118_l580
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t119_l583
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (:entries (:legend (pj/plan v))))))
   v118_l580)))


(def
 v121_l590
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t122_l594
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v121_l590)))


(def
 v124_l611
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t125_l616
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v124_l611)))


(def
 v127_l624
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  pj/draft
  :layers
  first
  (select-keys [:color :color-type])))


(deftest
 t128_l633
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v127_l624)))


(def
 v130_l648
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t131_l653
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v130_l648)))


(def
 v133_l657
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
    (pj/lay-point :quarter [:revenue :cost])))))


(deftest
 t134_l663
 (is
  ((fn
    [s]
    (re-find
     #"Removed 1 rows with a missing value among the columns read as series \(:revenue, :cost\)"
     s))
   v133_l657)))


(def
 v136_l672
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q1" "Q2" "Q2"],
     :measure ["revenue" "cost" "revenue" "cost"],
     :value [120 90 nil 100]}
    (pj/lay-point :quarter :value {:color :measure})))))


(deftest
 t137_l678
 (is ((fn [s] (re-find #"Removed 1 rows" s)) v136_l672)))


(def
 v139_l689
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t140_l693
 (is
  ((fn
    [v]
    (let
     [panels (mapcat (comp :panels :plan) (:sub-plots (pj/plan v)))]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (= 3 (count (distinct (map :y-domain panels)))))))
   v139_l689)))


(def
 v142_l701
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)
  (pj/options {:share-scales {:y :all}})))


(deftest
 t143_l706
 (is
  ((fn
    [v]
    (let
     [panels (mapcat (comp :panels :plan) (:sub-plots (pj/plan v)))]
     (= 1 (count (distinct (map :y-domain panels))))))
   v142_l701)))


(def
 v145_l712
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t146_l716
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v145_l712)))


(def
 v148_l725
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t149_l728
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v148_l725)))


(def
 v151_l732
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t152_l735
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v151_l732)))


(def
 v154_l743
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t155_l746
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 4 (:polygons s)))))
   v154_l743)))


(def
 v157_l757
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t158_l760
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
   v157_l757)))


(def
 v160_l769
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t161_l776
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
   v160_l769)))


(def
 v163_l798
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t164_l801
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v163_l798)))


(def
 v166_l808
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:group :region})))


(deftest
 t167_l811
 (is
  ((fn
    [v]
    (let
     [groups (:groups (first (:layers (first (:panels (pj/plan v))))))]
     (and
      (= 4 (count groups))
      (= 4 (count (distinct (map :dodge-idx groups))))
      (= 2 (count (distinct (map :color groups)))))))
   v166_l808)))


(def
 v169_l829
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t170_l832
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
   v169_l829)))


(def
 v172_l845
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t173_l849
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v172_l845)))


(def
 v175_l861
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t176_l865
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v175_l861)))


(def
 v178_l874
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t179_l878
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v178_l874)))


(def
 v181_l882
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t182_l886
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v181_l882)))


(def
 v183_l888
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t184_l892
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v183_l888)))


(def
 v186_l901
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t187_l906
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
   v186_l901)))


(def
 v189_l917
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t190_l922
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v189_l917)))


(def
 v192_l929
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t193_l933
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v192_l929)))


(def
 v195_l942
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t196_l949
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
   v195_l942)))


(def
 v198_l967
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t199_l971
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v198_l967)))


(def
 v201_l980
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t202_l984
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
   v201_l980)))


(def
 v204_l1001
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t205_l1005
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
      #"Note: on :y one axis holds :revenue .*:cost .*:units"
      (with-out-str (pj/plan v)))))
   v204_l1001)))


(def
 v207_l1016
 (pj/arrange
  [(pj/lay-bar sales :quarter [:revenue :cost] {:position :stack})
   (pj/lay-line sales :quarter :units)]
  {:cols 1}))


(deftest
 t208_l1020
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v207_l1016)))


(def
 v210_l1026
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t211_l1031
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:lines s)))))
   v210_l1026)))


(def
 v213_l1044
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t214_l1050
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v213_l1044)))


(def
 v216_l1055
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
 t217_l1065
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v216_l1055)))


(def
 v219_l1069
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
 t220_l1077
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v219_l1069)))


(def
 v222_l1081
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
 t223_l1090
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v222_l1081)))


(def
 v225_l1094
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t226_l1101
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v225_l1094)))


(def
 v228_l1105
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
 t229_l1120
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v228_l1105)))


(def
 v231_l1124
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
 t232_l1131
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v231_l1124)))


(def
 v234_l1135
 (pj/arrange
  [(-> sales (pj/lay-line :quarter [:revenue :cost]))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(def
 v236_l1151
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
 t237_l1179
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
   v236_l1151)))


(def
 v239_l1204
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t240_l1207
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v239_l1204)))


(def
 v242_l1213
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t243_l1216
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v242_l1213)))
