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
 v54_l282
 (->
  {:time-a [0 1 2 3],
   :time-b [0.5 1.5 2.5 3.5],
   :reading-a [2 3 5 4],
   :reading-b [1 2 2 3]}
  (pj/lay-line [:time-a :time-b] [:reading-a :reading-b])))


(deftest
 t55_l288
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
   v54_l282)))


(def
 v57_l301
 (try
  (->
   {:time-a [0 1], :time-b [2 3], :reading-a [1 2], :reading-b [3 4]}
   (pj/lay-line [:time-a :time-b] [:reading-a :reading-b :time-a]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t58_l307
 (is ((fn [msg] (re-find #"as many columns each" msg)) v57_l301)))


(def
 v60_l315
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t61_l322
 (is
  ((fn
    [msg]
    (and
     (re-find #"the data already has a :value column" msg)
     (re-find #"the data has as well" msg)))
   v60_l315)))


(def
 v63_l331
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t64_l337
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v63_l331)))


(def v66_l350 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t67_l353
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v66_l350)))


(def v68_l355 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t69_l358
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v68_l355)))


(def
 v71_l362
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t72_l365
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v71_l362)))


(def v73_l367 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t74_l370
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v73_l367)))


(def
 v76_l375
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t77_l378
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v76_l375)))


(def v79_l386 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t80_l389
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v79_l386)))


(def
 v82_l399
 (try
  (pj/plot (-> sales (pj/lay-density :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t83_l404
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v82_l399)))


(def
 v85_l414
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t86_l417
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v85_l414)))


(def
 v88_l429
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t89_l432
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v88_l429)))


(def
 v91_l440
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t92_l443
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v91_l440)))


(def
 v94_l449
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t95_l453
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v94_l449)))


(def
 v97_l459
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t98_l463
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
   v97_l459)))


(def
 v100_l472
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t101_l476
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v100_l472)))


(def
 v103_l481
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t104_l485
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v103_l481)))


(def
 v106_l492
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t107_l497
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
   v106_l492)))


(def
 v109_l512
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t110_l515
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
   v109_l512)))


(def
 v112_l532
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t113_l536
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v112_l532)))


(def
 v115_l553
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t116_l558
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v115_l553)))


(def
 v118_l565
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  :layers
  first
  :mapping))


(deftest
 t119_l573
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v118_l565)))


(def
 v121_l588
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t122_l593
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v121_l588)))


(def
 v124_l597
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
    (pj/lay-point :quarter [:revenue :cost])))))


(deftest
 t125_l603
 (is
  ((fn
    [s]
    (re-find
     #"Removed 1 rows with a missing value among the columns read as series \(:revenue, :cost\)"
     s))
   v124_l597)))


(def
 v127_l612
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q1" "Q2" "Q2"],
     :measure ["revenue" "cost" "revenue" "cost"],
     :value [120 90 nil 100]}
    (pj/lay-point :quarter :value {:color :measure})))))


(deftest
 t128_l618
 (is ((fn [s] (re-find #"Removed 1 rows" s)) v127_l612)))


(def
 v130_l629
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t131_l633
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
   v130_l629)))


(def
 v133_l641
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t134_l645
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v133_l641)))


(def
 v136_l654
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t137_l657
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v136_l654)))


(def
 v139_l661
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t140_l664
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v139_l661)))


(def
 v142_l672
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t143_l675
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 4 (:polygons s)))))
   v142_l672)))


(def
 v145_l686
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t146_l689
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
   v145_l686)))


(def
 v148_l698
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t149_l705
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
   v148_l698)))


(def
 v151_l727
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t152_l730
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v151_l727)))


(def
 v154_l738
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:group :region})))


(deftest
 t155_l741
 (is
  ((fn
    [v]
    (let
     [groups (:groups (first (:layers (first (:panels (pj/plan v))))))]
     (and
      (= 4 (count groups))
      (= 4 (count (distinct (map :dodge-idx groups))))
      (= 2 (count (distinct (map :color groups)))))))
   v154_l738)))


(def
 v157_l759
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t158_l762
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
   v157_l759)))


(def
 v160_l775
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t161_l779
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v160_l775)))


(def
 v163_l791
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t164_l795
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v163_l791)))


(def
 v166_l804
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t167_l808
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v166_l804)))


(def
 v169_l812
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t170_l816
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v169_l812)))


(def
 v171_l818
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t172_l822
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v171_l818)))


(def
 v174_l831
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t175_l836
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
   v174_l831)))


(def
 v177_l847
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t178_l852
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v177_l847)))


(def
 v180_l859
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t181_l863
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v180_l859)))


(def
 v183_l872
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t184_l879
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
   v183_l872)))


(def
 v186_l897
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t187_l901
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v186_l897)))


(def
 v189_l910
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t190_l914
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
   v189_l910)))


(def
 v192_l928
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t193_l932
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v192_l928)))


(def
 v194_l934
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)
  pj/overlay))


(deftest
 t195_l939
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v194_l934)))


(def
 v197_l947
 (with-out-str
  (pj/plot
   (->
    sales
    (pj/lay-bar :quarter [:revenue :cost])
    (pj/lay-line :quarter :units)))))


(deftest
 t198_l952
 (is
  ((fn
    [out]
    (and
     (re-find #"panel of its own" out)
     (re-find #"pj/overlay" out)
     (not (re-find #"as series" out))))
   v197_l947)))


(def
 v200_l961
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t201_l966
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:lines s)))))
   v200_l961)))


(def
 v203_l979
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t204_l985
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v203_l979)))


(def
 v206_l990
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
 t207_l1000
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v206_l990)))


(def
 v209_l1004
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
 t210_l1012
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v209_l1004)))


(def
 v212_l1016
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
 t213_l1025
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v212_l1016)))


(def
 v215_l1029
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t216_l1036
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v215_l1029)))


(def
 v218_l1040
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
 t219_l1055
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v218_l1040)))


(def
 v221_l1059
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
 t222_l1066
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v221_l1059)))


(def
 v224_l1075
 (try
  (->
   sales
   (pj/pose [[:quarter :revenue] [:quarter :cost]])
   (pj/lay-point :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t225_l1082
 (is
  ((fn
    [msg]
    (and
     (re-find #"composite pose" msg)
     (re-find #"before arranging" msg)))
   v224_l1075)))


(def
 v227_l1098
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
 t229_l1129
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
   v227_l1098)))


(def
 v231_l1151
 (->
  sales
  (pj/pose [[:quarter :revenue] [:quarter :cost]])
  (pj/lay-point)))


(deftest
 t232_l1155
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v231_l1151)))


(def v234_l1161 (-> sales (pj/lay-histogram [:revenue :cost])))


(deftest
 t235_l1164
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v234_l1161)))


(def
 v237_l1170
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t238_l1173
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v237_l1170)))


(def
 v240_l1179
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t241_l1182
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v240_l1179)))
