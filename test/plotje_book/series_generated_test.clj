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
 v3_l23
 (def
  sales
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4"],
    :revenue [120 150 140 190],
    :cost [90 100 115 120],
    :tax [18 24 21 30],
    :units [12 15 14 19]})))


(def v4_l30 sales)


(def
 v6_l37
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


(def v7_l46 sales-by-region)


(def v9_l53 (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))


(deftest
 t10_l56
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v9_l53)))


(def
 v12_l67
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))


(deftest
 t13_l70
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
   v12_l67)))


(def
 v15_l84
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t16_l87
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v15_l84)))


(def
 v18_l93
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t19_l96
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v18_l93)))


(def
 v21_l101
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t22_l104
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v21_l101)))


(def
 v24_l114
 (->
  sales
  (tc/pivot->longer
   #{:revenue :tax :cost}
   {:target-columns :series, :value-column-name :value})
  (pj/lay-bar :quarter :value {:color :series, :position :dodge})))


(deftest
 t25_l119
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
   v24_l114)))


(def v27_l131 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t28_l134
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v27_l131)))


(def v29_l136 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t30_l139
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v29_l136)))


(def
 v32_l147
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t33_l152
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v32_l147)))


(def
 v35_l159
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t36_l163
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v35_l159)))


(def
 v38_l169
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t39_l173
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
   v38_l169)))


(def
 v41_l188
 (try
  (->
   sales
   (pj/lay-bar :quarter {:series [:revenue :cost], :label :measure}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t42_l194
 (is
  ((fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))
   v41_l188)))


(def
 v44_l199
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t45_l203
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v44_l199)))


(def
 v47_l212
 (->
  sales
  (pj/pose :quarter [:revenue :cost])
  pj/lay-line
  pj/lay-point))


(deftest
 t48_l217
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)) (= 8 (:points s)))))
   v47_l212)))


(def
 v50_l225
 (try
  (->
   sales
   (pj/pose {:x :quarter, :y [:revenue :cost]})
   (pj/lay-line :quarter [:revenue :tax]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l232
 (is
  ((fn [msg] (re-find #"two pivots have no shared shape" msg))
   v50_l225)))


(def
 v53_l239
 (-> sales (pj/lay-bar {:x :quarter, :y [:revenue :cost :tax]})))


(deftest
 t54_l242
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v53_l239)))


(def
 v56_l251
 (-> sales (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))


(deftest
 t57_l254
 (is ((fn [v] (= 12 (:points (pj/svg-summary v)))) v56_l251)))


(def v59_l258 (-> sales (pj/lay-point [:revenue :cost :tax] :quarter)))


(deftest
 t60_l261
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))))
   v59_l258)))


(def
 v62_l274
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t63_l277
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l274)))


(def v64_l279 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t65_l282
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v64_l279)))


(def
 v67_l287
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t68_l290
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v67_l287)))


(def v70_l298 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t71_l301
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v70_l298)))


(def
 v73_l311
 (try
  (pj/plot (-> sales (pj/lay-density :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t74_l316
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v73_l311)))


(def
 v76_l326
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t77_l329
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v76_l326)))


(def
 v79_l341
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t80_l344
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v79_l341)))


(def
 v82_l352
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t83_l355
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v82_l352)))


(def
 v85_l359
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t86_l363
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v85_l359)))


(def
 v88_l370
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t89_l375
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
   v88_l370)))


(def
 v91_l390
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t92_l393
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
   v91_l390)))


(def
 v94_l410
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t95_l414
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v94_l410)))


(def
 v97_l420
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t98_l424
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v97_l420)))


(def
 v100_l437
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t101_l446
 (is
  ((fn
    [msg]
    (and
     (re-find
      #"key column :series, and the data already has a :series column"
      msg)
     (not (re-find #":value" msg))))
   v100_l437)))


(def
 v103_l454
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t104_l460
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v103_l454)))


(def
 v106_l476
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t107_l481
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v106_l476)))


(def
 v109_l488
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  :layers
  first
  :mapping))


(deftest
 t110_l496
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v109_l488)))


(def
 v112_l511
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t113_l516
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v112_l511)))


(def
 v115_l520
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
    (pj/lay-point :quarter [:revenue :cost])))))


(deftest
 t116_l526
 (is
  ((fn
    [s]
    (re-find
     #"Removed 1 rows with a missing value among the columns read as series \(:revenue, :cost\)"
     s))
   v115_l520)))


(def
 v118_l535
 (with-out-str
  (pj/plan
   (->
    {:quarter ["Q1" "Q1" "Q2" "Q2"],
     :measure ["revenue" "cost" "revenue" "cost"],
     :value [120 90 nil 100]}
    (pj/lay-point :quarter :value {:color :measure})))))


(deftest
 t119_l541
 (is ((fn [s] (re-find #"Removed 1 rows" s)) v118_l535)))


(def
 v121_l548
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t122_l555
 (is
  ((fn
    [msg]
    (and
     (re-find #"the data already has a :value column" msg)
     (re-find #"the data has as well" msg)))
   v121_l548)))


(def
 v124_l564
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t125_l570
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v124_l564)))


(def
 v127_l580
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t128_l584
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v127_l580)))


(def
 v130_l593
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t131_l597
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
   v130_l593)))


(def
 v133_l613
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t134_l617
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
   v133_l613)))


(def
 v136_l625
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t137_l629
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v136_l625)))


(def
 v139_l638
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t140_l641
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v139_l638)))


(def
 v142_l645
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t143_l648
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v142_l645)))


(def
 v145_l656
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t146_l659
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 4 (:polygons s)))))
   v145_l656)))


(def
 v148_l670
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t149_l673
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
   v148_l670)))


(def
 v151_l682
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t152_l689
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
   v151_l682)))


(def
 v154_l705
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t155_l709
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v154_l705)))


(def
 v156_l711
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t157_l715
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v156_l711)))


(def
 v159_l723
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t160_l726
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v159_l723)))


(def
 v162_l734
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:group :region})))


(deftest
 t163_l737
 (is
  ((fn
    [v]
    (let
     [groups (:groups (first (:layers (first (:panels (pj/plan v))))))]
     (and
      (= 4 (count groups))
      (= 4 (count (distinct (map :dodge-idx groups))))
      (= 2 (count (distinct (map :color groups)))))))
   v162_l734)))


(def
 v165_l755
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t166_l758
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
   v165_l755)))


(def
 v168_l771
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t169_l775
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v168_l771)))


(def
 v171_l787
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t172_l791
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v171_l787)))


(def
 v174_l803
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t175_l808
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
   v174_l803)))


(def
 v177_l819
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t178_l824
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v177_l819)))


(def
 v180_l831
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t181_l835
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v180_l831)))


(def
 v183_l844
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t184_l851
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
   v183_l844)))


(def
 v186_l864
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t187_l868
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v186_l864)))


(def
 v188_l870
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)
  pj/overlay))


(deftest
 t189_l875
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v188_l870)))


(def
 v191_l883
 (with-out-str
  (pj/plot
   (->
    sales
    (pj/lay-bar :quarter [:revenue :cost])
    (pj/lay-line :quarter :units)))))


(deftest
 t192_l888
 (is
  ((fn
    [out]
    (and
     (re-find #"panel of its own" out)
     (re-find #"pj/overlay" out)
     (not (re-find #"as series" out))))
   v191_l883)))


(def
 v194_l897
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t195_l902
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 2 (:lines s)))))
   v194_l897)))


(def
 v197_l913
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t198_l919
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v197_l913)))


(def
 v200_l924
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
 t201_l934
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v200_l924)))


(def
 v203_l938
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
 t204_l946
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v203_l938)))


(def
 v206_l950
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
 t207_l959
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v206_l950)))


(def
 v209_l963
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t210_l970
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v209_l963)))


(def
 v212_l974
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
 t213_l989
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v212_l974)))


(def
 v215_l993
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
 t216_l1000
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v215_l993)))


(def
 v218_l1009
 (try
  (->
   sales
   (pj/pose [[:quarter :revenue] [:quarter :cost]])
   (pj/lay-point :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t219_l1016
 (is
  ((fn
    [msg]
    (and
     (re-find #"composite pose" msg)
     (re-find #"before arranging" msg)))
   v218_l1009)))


(def
 v221_l1030
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
     ["(pj/lay-point {:series [:revenue :cost]} {:series [:tax :units]})"
      (fn*
       []
       (->
        sales
        (pj/lay-point
         {:series [:revenue :cost]}
         {:series [:tax :units]})))]
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
 t223_l1063
 (is
  ((fn
    [t]
    (and
     (= 10 (count (:row-vectors t)))
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
       (fn*
        []
        (->
         sales
         (pj/lay-point
          {:series [:revenue :cost]}
          {:series [:tax :units]})))
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
   v221_l1030)))


(def
 v225_l1086
 (->
  sales
  (pj/pose [[:quarter :revenue] [:quarter :cost]])
  (pj/lay-point)))


(deftest
 t226_l1090
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v225_l1086)))


(def v228_l1096 (-> sales (pj/lay-histogram [:revenue :cost])))


(deftest
 t229_l1099
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v228_l1096)))


(def
 v231_l1105
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t232_l1108
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v231_l1105)))


(def
 v234_l1114
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t235_l1117
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v234_l1114)))
