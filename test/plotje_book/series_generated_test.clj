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
 v54_l294
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t55_l303
 (is
  ((fn
    [msg]
    (and
     (re-find
      #"key column :series, and the data already has a :series column"
      msg)
     (not (re-find #":value" msg))))
   v54_l294)))


(def
 v57_l311
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t58_l317
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v57_l311)))


(def
 v60_l334
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])))


(deftest
 t61_l340
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
   v60_l334)))


(def
 v63_l352
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])
  (pj/options {:x-label "Time (s)", :y-label "Reading"})))


(deftest
 t64_l359
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "Time (s)")
      (contains? texts "Reading")
      (not (contains? texts "x value")))))
   v63_l352)))


(def
 v66_l369
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
 t67_l377
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
   v66_l369)))


(def
 v69_l388
 (try
  (->
   {:time-a [0 1], :time-b [2 3], :reading-a [1 2], :reading-b [3 4]}
   (pj/lay-line [:time-a :time-b] [:reading-a :reading-b :time-a]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t70_l394
 (is ((fn [msg] (re-find #"as many columns each" msg)) v69_l388)))


(def
 v72_l406
 (->>
  (for
   [x [:sepal-length :petal-length] y [:sepal-width :petal-width]]
   [x y])
  (apply map vector)
  (apply pj/lay-point (rdatasets/datasets-iris))))


(deftest
 t73_l412
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
   v72_l406)))


(def
 v75_l433
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t76_l440
 (is
  ((fn
    [msg]
    (and
     (re-find #"lay-line reads \[:tax :units\] as series" msg)
     (re-find #"already reads \[:revenue :cost\]" msg)))
   v75_l433)))


(def
 v78_l446
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t79_l452
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v78_l446)))


(def v81_l465 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t82_l468
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v81_l465)))


(def v83_l470 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t84_l473
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v83_l470)))


(def
 v86_l477
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t87_l480
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v86_l477)))


(def v88_l482 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t89_l485
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v88_l482)))


(def
 v91_l490
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t92_l493
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v91_l490)))


(def v94_l505 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t95_l508
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v94_l505)))


(def
 v97_l518
 (try
  (pj/plot (-> sales (pj/lay-smooth :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t98_l523
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v97_l518)))


(def
 v100_l533
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t101_l536
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v100_l533)))


(def
 v103_l548
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t104_l551
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v103_l548)))


(def
 v106_l564
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t107_l567
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v106_l564)))


(def
 v109_l573
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t110_l577
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v109_l573)))


(def
 v112_l588
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t113_l592
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
   v112_l588)))


(def
 v115_l601
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t116_l605
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v115_l601)))


(def
 v118_l615
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t119_l619
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v118_l615)))


(def
 v121_l626
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t122_l631
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
   v121_l626)))


(def
 v124_l644
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t125_l647
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (:entries (:legend (pj/plan v))))))
   v124_l644)))


(def
 v127_l654
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t128_l658
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v127_l654)))


(def
 v130_l681
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t131_l686
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v130_l681)))


(def
 v133_l693
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  pj/draft
  :layers
  first
  (select-keys [:color :color-type])
  kind/pprint))


(deftest
 t134_l703
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v133_l693)))


(def
 v136_l718
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t137_l723
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v136_l718)))


(def
 v139_l727
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
    (pj/lay-point :quarter [:revenue :cost])))))


(deftest
 t140_l733
 (is
  ((fn
    [s]
    (re-find
     #"Removed 1 row with a missing value among the columns read as series \(:revenue, :cost\)"
     s))
   v139_l727)))


(def
 v142_l742
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q1" "Q2" "Q2"],
     :measure ["revenue" "cost" "revenue" "cost"],
     :value [120 90 nil 100]}
    (pj/lay-point :quarter :value {:color :measure})))))


(deftest
 t143_l748
 (is ((fn [s] (re-find #"Removed 1 row" s)) v142_l742)))


(def
 v145_l758
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t146_l762
 (is
  ((fn
    [v]
    (let
     [panels (mapcat (comp :panels :plan) (:sub-plots (pj/plan v)))]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (= 3 (count (distinct (map :y-domain panels)))))))
   v145_l758)))


(def
 v148_l771
 (->
  sales
  (pj/pose {:x :quarter})
  pj/lay-line
  (pj/facet {:col {:y [:revenue :cost :tax]}})))


(deftest
 t149_l776
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
          [{:x :quarter, :y :revenue}
           {:x :quarter, :y :cost}
           {:x :quarter, :y :tax}])
         pj/lay-line))))))
   v148_l771)))


(deftest
 t151_l793
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
   v148_l771)))


(def
 v153_l804
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)
  (pj/options {:share-scales {:y :all}})))


(deftest
 t154_l809
 (is
  ((fn
    [v]
    (let
     [panels (mapcat (comp :panels :plan) (:sub-plots (pj/plan v)))]
     (= 1 (count (distinct (map :y-domain panels))))))
   v153_l804)))


(def
 v156_l815
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t157_l819
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v156_l815)))


(def
 v159_l828
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t160_l831
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v159_l828)))


(def
 v162_l835
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t163_l838
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v162_l835)))


(def
 v165_l847
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t166_l850
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
   v165_l847)))


(def
 v168_l865
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t169_l868
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
   v168_l865)))


(def
 v171_l883
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t172_l890
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
   v171_l883)))


(def
 v174_l912
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t175_l915
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v174_l912)))


(def
 v177_l922
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:group :region})))


(deftest
 t178_l925
 (is
  ((fn
    [v]
    (let
     [groups (:groups (first (:layers (first (:panels (pj/plan v))))))]
     (and
      (= 4 (count groups))
      (= 4 (count (distinct (map :dodge-idx groups))))
      (= 2 (count (distinct (map :color groups)))))))
   v177_l922)))


(def
 v180_l943
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t181_l946
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
   v180_l943)))


(def
 v183_l959
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t184_l963
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v183_l959)))


(def
 v186_l975
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t187_l979
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v186_l975)))


(def
 v189_l988
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t190_l992
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v189_l988)))


(def
 v192_l996
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t193_l1000
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v192_l996)))


(def
 v194_l1002
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet {:col :region, :row :outlet})))


(deftest
 t195_l1006
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v194_l1002)))


(def
 v197_l1015
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t198_l1020
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
   v197_l1015)))


(def
 v200_l1031
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t201_l1036
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v200_l1031)))


(def
 v203_l1043
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t204_l1047
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v203_l1043)))


(def
 v206_l1060
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t207_l1067
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
   v206_l1060)))


(def
 v209_l1088
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t210_l1092
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
   v209_l1088)))


(def
 v212_l1104
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t213_l1108
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v212_l1104)))


(def
 v215_l1117
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t216_l1121
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
   v215_l1117)))


(def
 v218_l1138
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t219_l1142
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
   v218_l1138)))


(def
 v221_l1154
 (pj/arrange
  [(pj/lay-bar sales :quarter [:revenue :cost] {:position :stack})
   (pj/lay-line sales :quarter :units)]
  {:cols 1}))


(deftest
 t222_l1158
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v221_l1154)))


(def
 v224_l1164
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t225_l1169
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:lines s)))))
   v224_l1164)))


(def
 v227_l1182
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t228_l1188
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v227_l1182)))


(def
 v230_l1193
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
 t231_l1203
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v230_l1193)))


(def
 v233_l1207
 (pj/arrange
  [(->
    sales-by-region
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))
   (->
    sales-by-region
    (pj/lay-line :quarter :units)
    (pj/facet :region))]
  {:width 900}))


(deftest
 t234_l1216
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v233_l1207)))


(def
 v236_l1220
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
 t237_l1229
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v236_l1220)))


(def
 v239_l1233
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t240_l1240
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v239_l1233)))


(def
 v242_l1244
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
 t243_l1259
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v242_l1244)))


(def
 v245_l1263
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
 t246_l1270
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v245_l1263)))


(def
 v248_l1277
 (pj/arrange
  [(-> sales (pj/lay-line :quarter [:revenue :cost]))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t249_l1281
 (is
  ((fn
    [v]
    (=
     {:panels 2, :lines 4}
     (select-keys (pj/svg-summary v) [:panels :lines])))
   v248_l1277)))


(def
 v251_l1296
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
 t252_l1324
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
   v251_l1296)))


(def
 v254_l1349
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t255_l1352
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v254_l1349)))


(def
 v257_l1358
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t258_l1361
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v257_l1358)))
