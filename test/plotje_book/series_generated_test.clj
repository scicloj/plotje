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
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-bar
        :quarter
        [:revenue :cost :tax]
        {:position :dodge})))))
   v12_l71)))


(def
 v15_l92
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t16_l97
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v15_l92)))


(def
 v18_l110
 (try
  (->
   sales
   (pj/lay-bar :quarter {:series [:revenue :cost], :label :measure}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t19_l116
 (is
  ((fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))
   v18_l110)))


(def
 v21_l126
 (->
  sales
  (pj/pose :quarter [:revenue :cost])
  pj/lay-line
  pj/lay-point))


(deftest
 t22_l131
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)) (= 8 (:points s)))))
   v21_l126)))


(def
 v24_l139
 (try
  (->
   sales
   (pj/pose {:x :quarter, :y [:revenue :cost]})
   (pj/lay-line :quarter [:revenue :tax]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t25_l146
 (is
  ((fn [msg] (re-find #"two pivots have no shared shape" msg))
   v24_l139)))


(def
 v27_l153
 (-> sales (pj/lay-bar {:x :quarter, :y [:revenue :cost :tax]})))


(deftest
 t28_l156
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v27_l153)))


(def
 v30_l165
 (-> sales (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))


(deftest
 t31_l168
 (is ((fn [v] (= 12 (:points (pj/svg-summary v)))) v30_l165)))


(def v33_l172 (-> sales (pj/lay-point [:revenue :cost :tax] :quarter)))


(deftest
 t34_l175
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))))
   v33_l172)))


(def
 v36_l195
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))


(deftest
 t37_l198
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
   v36_l195)))


(def
 v39_l212
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t40_l215
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v39_l212)))


(def
 v42_l221
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t43_l224
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v42_l221)))


(def
 v45_l229
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t46_l232
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v45_l229)))


(def
 v48_l245
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t49_l254
 (is
  ((fn
    [msg]
    (and
     (re-find
      #"key column :series, and the data already has a :series column"
      msg)
     (not (re-find #":value" msg))))
   v48_l245)))


(def
 v51_l262
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t52_l268
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v51_l262)))


(def
 v54_l284
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])))


(deftest
 t55_l290
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
      (contains? texts "x value")
      (contains? texts "y value"))))
   v54_l284)))


(def
 v57_l303
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])
  (pj/options {:x-label "Time (s)", :y-label "Reading"})))


(deftest
 t58_l310
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "Time (s)")
      (contains? texts "Reading")
      (not (contains? texts "x value")))))
   v57_l303)))


(def
 v60_l320
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
 t61_l328
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
   v60_l320)))


(def
 v63_l339
 (try
  (->
   {:time-a [0 1], :time-b [2 3], :reading-a [1 2], :reading-b [3 4]}
   (pj/lay-line [:time-a :time-b] [:reading-a :reading-b :time-a]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t64_l345
 (is ((fn [msg] (re-find #"as many columns each" msg)) v63_l339)))


(def
 v66_l353
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t67_l360
 (is
  ((fn
    [msg]
    (and
     (re-find #"the data already has a :value column" msg)
     (re-find #"the data has as well" msg)))
   v66_l353)))


(def
 v69_l369
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t70_l375
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v69_l369)))


(def v72_l388 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t73_l391
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v72_l388)))


(def v74_l393 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t75_l396
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v74_l393)))


(def
 v77_l400
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t78_l403
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v77_l400)))


(def v79_l405 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t80_l408
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v79_l405)))


(def
 v82_l413
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t83_l416
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v82_l413)))


(def v85_l424 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t86_l427
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v85_l424)))


(def
 v88_l437
 (try
  (pj/plot (-> sales (pj/lay-smooth :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t89_l442
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v88_l437)))


(def
 v91_l452
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t92_l455
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v91_l452)))


(def
 v94_l467
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t95_l470
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v94_l467)))


(def
 v97_l478
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t98_l481
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v97_l478)))


(def
 v100_l487
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t101_l491
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v100_l487)))


(def
 v103_l497
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t104_l501
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
   v103_l497)))


(def
 v106_l510
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t107_l514
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v106_l510)))


(def
 v109_l519
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t110_l523
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v109_l519)))


(def
 v112_l530
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t113_l535
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
   v112_l530)))


(def
 v115_l550
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t116_l553
 (is
  ((fn
    [v]
    (and
     (=
      (pj/plot v)
      (pj/plot
       (->
        sales
        (pj/lay-bar
         :quarter
         [:revenue :cost :tax]
         {:position :stack}))))
     (=
      (pj/plot
       (->
        sales
        (pj/lay-bar
         :quarter
         [:tax :revenue :cost]
         {:position :dodge})))
      (pj/plot
       (->
        sales
        (pj/lay-bar
         :quarter
         [:revenue :cost :tax]
         {:position :dodge}))))))
   v115_l550)))


(def
 v118_l570
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t119_l574
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v118_l570)))


(def
 v121_l591
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t122_l596
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v121_l591)))


(def
 v124_l603
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  :layers
  first
  :mapping))


(deftest
 t125_l611
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v124_l603)))


(def
 v127_l626
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t128_l631
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v127_l626)))


(def
 v130_l635
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
    (pj/lay-point :quarter [:revenue :cost])))))


(deftest
 t131_l641
 (is
  ((fn
    [s]
    (re-find
     #"Removed 1 rows with a missing value among the columns read as series \(:revenue, :cost\)"
     s))
   v130_l635)))


(def
 v133_l650
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q1" "Q2" "Q2"],
     :measure ["revenue" "cost" "revenue" "cost"],
     :value [120 90 nil 100]}
    (pj/lay-point :quarter :value {:color :measure})))))


(deftest
 t134_l656
 (is ((fn [s] (re-find #"Removed 1 rows" s)) v133_l650)))


(def
 v136_l667
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t137_l671
 (is
  ((fn
    [v]
    (and
     (= 3 (:panels (pj/svg-summary v)))
     (=
      1
      (count
       (distinct
        (map
         (fn [panel] (mapv double (:y-domain panel)))
         (:panels (pj/plan v))))))))
   v136_l667)))


(def
 v139_l679
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t140_l683
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v139_l679)))


(def
 v142_l692
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t143_l695
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v142_l692)))


(def
 v145_l699
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t146_l702
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v145_l699)))


(def
 v148_l710
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t149_l713
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 4 (:polygons s)))))
   v148_l710)))


(def
 v151_l724
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t152_l727
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
   v151_l724)))


(def
 v154_l736
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t155_l743
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
   v154_l736)))


(def
 v157_l765
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t158_l768
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v157_l765)))


(def
 v160_l775
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:group :region})))


(deftest
 t161_l778
 (is
  ((fn
    [v]
    (let
     [groups (:groups (first (:layers (first (:panels (pj/plan v))))))]
     (and
      (= 4 (count groups))
      (= 4 (count (distinct (map :dodge-idx groups))))
      (= 2 (count (distinct (map :color groups)))))))
   v160_l775)))


(def
 v163_l796
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t164_l799
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
   v163_l796)))


(def
 v166_l812
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t167_l816
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v166_l812)))


(def
 v169_l828
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t170_l832
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v169_l828)))


(def
 v172_l841
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t173_l845
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v172_l841)))


(def
 v175_l849
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t176_l853
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v175_l849)))


(def
 v177_l855
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t178_l859
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v177_l855)))


(def
 v180_l868
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t181_l873
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
   v180_l868)))


(def
 v183_l884
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t184_l889
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v183_l884)))


(def
 v186_l896
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t187_l900
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v186_l896)))


(def
 v189_l909
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t190_l916
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
   v189_l909)))


(def
 v192_l934
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t193_l938
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v192_l934)))


(def
 v195_l947
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t196_l951
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
   v195_l947)))


(def
 v198_l965
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t199_l969
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v198_l965)))


(def
 v200_l971
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)
  pj/overlay))


(deftest
 t201_l976
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v200_l971)))


(def
 v203_l984
 (with-out-str
  (pj/plot
   (->
    sales
    (pj/lay-bar :quarter [:revenue :cost])
    (pj/lay-line :quarter :units)))))


(deftest
 t204_l989
 (is
  ((fn
    [out]
    (and
     (re-find #"panel of its own" out)
     (re-find #"pj/overlay" out)
     (not (re-find #"as series" out))))
   v203_l984)))


(def
 v206_l998
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t207_l1003
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:lines s)))))
   v206_l998)))


(def
 v209_l1016
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t210_l1022
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v209_l1016)))


(def
 v212_l1027
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
 t213_l1037
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v212_l1027)))


(def
 v215_l1041
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
 t216_l1049
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v215_l1041)))


(def
 v218_l1053
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
 t219_l1062
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v218_l1053)))


(def
 v221_l1066
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t222_l1073
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v221_l1066)))


(def
 v224_l1077
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
 t225_l1092
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v224_l1077)))


(def
 v227_l1096
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
 t228_l1103
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v227_l1096)))


(def
 v230_l1112
 (try
  (->
   sales
   (pj/pose [[:quarter :revenue] [:quarter :cost]])
   (pj/lay-point :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t231_l1119
 (is
  ((fn
    [msg]
    (and
     (re-find #"composite pose" msg)
     (re-find #"before arranging" msg)))
   v230_l1112)))


(def
 v233_l1135
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
 t234_l1163
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
   v233_l1135)))


(def
 v236_l1187
 (->
  sales
  (pj/pose [[:quarter :revenue] [:quarter :cost]])
  (pj/lay-point)))


(deftest
 t237_l1191
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v236_l1187)))


(def v239_l1197 (-> sales (pj/lay-histogram [:revenue :cost])))


(deftest
 t240_l1200
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v239_l1197)))


(def
 v242_l1206
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t243_l1209
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v242_l1206)))


(def
 v245_l1215
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t246_l1218
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v245_l1215)))
