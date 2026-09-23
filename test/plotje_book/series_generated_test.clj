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
    (not=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v12_l67)))


(def
 v15_l77
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t16_l80
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v15_l77)))


(def
 v18_l86
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t19_l89
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v18_l86)))


(def
 v21_l94
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t22_l97
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v21_l94)))


(def
 v24_l107
 (->
  sales
  (tc/pivot->longer
   #{:revenue :tax :cost}
   {:target-columns :series, :value-column-name :value})
  (pj/lay-bar :quarter :value {:color :series, :position :dodge})))


(deftest
 t25_l112
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
   v24_l107)))


(def v27_l124 (-> sales (pj/lay-line :quarter [:revenue :cost :tax])))


(deftest
 t28_l127
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v27_l124)))


(def v29_l129 (-> sales (pj/lay-point :quarter [:revenue :cost :tax])))


(deftest
 t30_l132
 (is ((fn [v] (pos? (:points (pj/svg-summary v)))) v29_l129)))


(def
 v32_l140
 (->
  sales
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t33_l145
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (contains? texts "measure") (contains? texts "Euros"))))
   v32_l140)))


(def
 v35_l152
 (->
  sales
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :scale {:type :log}})))


(deftest
 t36_l156
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :y-scale :type)))
   v35_l152)))


(def
 v38_l162
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax])
  (pj/scale :y {:type :log})))


(deftest
 t39_l166
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
   v38_l162)))


(def
 v41_l181
 (try
  (->
   sales
   (pj/lay-bar :quarter {:series [:revenue :cost], :label :measure}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t42_l187
 (is
  ((fn [msg] (re-find #"unexpected key\(s\): \[:label\]" msg))
   v41_l181)))


(def
 v44_l192
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/coord :flip)))


(deftest
 t45_l196
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v44_l192)))


(def
 v47_l205
 (->
  sales
  (pj/pose :quarter [:revenue :cost])
  pj/lay-line
  pj/lay-point))


(deftest
 t48_l210
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)) (= 8 (:points s)))))
   v47_l205)))


(def
 v50_l218
 (try
  (->
   sales
   (pj/pose {:x :quarter, :y [:revenue :cost]})
   (pj/lay-line :quarter [:revenue :tax]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t51_l225
 (is
  ((fn [msg] (re-find #"two pivots have no shared shape" msg))
   v50_l218)))


(def
 v53_l232
 (-> sales (pj/lay-bar {:x :quarter, :y [:revenue :cost :tax]})))


(deftest
 t54_l235
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot (-> sales (pj/lay-bar :quarter [:revenue :cost :tax])))))
   v53_l232)))


(def
 v56_l244
 (-> sales (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))


(deftest
 t57_l247
 (is ((fn [v] (= 12 (:points (pj/svg-summary v)))) v56_l244)))


(def v59_l251 (-> sales (pj/lay-point [:revenue :cost :tax] :quarter)))


(deftest
 t60_l254
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       sales
       (pj/lay-point {:series [:revenue :cost :tax]} :quarter)))))
   v59_l251)))


(def
 v62_l267
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :stack})))


(deftest
 t63_l270
 (is ((fn [v] (pos? (:polygons (pj/svg-summary v)))) v62_l267)))


(def v64_l272 (-> sales (pj/lay-step :quarter [:revenue :cost])))


(deftest
 t65_l275
 (is ((fn [v] (pos? (:lines (pj/svg-summary v)))) v64_l272)))


(def
 v67_l280
 (->
  sales
  (pj/lay-area :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t68_l283
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :y-domain))))
   v67_l280)))


(def v70_l291 (-> sales (pj/lay-lollipop :quarter [:revenue :cost])))


(deftest
 t71_l294
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v70_l291)))


(def
 v73_l304
 (try
  (pj/plot (-> sales (pj/lay-density :quarter [:revenue :cost])))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t74_l309
 (is ((fn [msg] (re-find #"requires a numeric column" msg)) v73_l304)))


(def
 v76_l319
 (-> sales-by-region (pj/lay-summary :quarter [:revenue :cost])))


(deftest
 t77_l322
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 8 (:lines s)))))
   v76_l319)))


(def
 v79_l334
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-line :date {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t80_l337
 (is
  ((fn
    [v]
    (and
     (= 2 (:lines (pj/svg-summary v)))
     (= :log (-> v pj/plan :panels first :y-scale :type))))
   v79_l334)))


(def
 v82_l345
 (->
  (rdatasets/ggplot2-economics)
  (pj/lay-smooth
   :date
   {:series [:pop :unemploy], :scale {:type :log}})))


(deftest
 t83_l348
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v82_l345)))


(def
 v85_l352
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})))


(deftest
 t86_l356
 (is
  ((fn
    [v]
    (=
     #{"rgb(55,126,184)" "rgb(230,85,13)" "rgb(77,175,74)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v85_l352)))


(def
 v88_l363
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/scale :color {:values ["#377eb8" "#e6550d" "#4daf4a"]})
  (pj/facet :region)))


(deftest
 t89_l368
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
   v88_l363)))


(def
 v91_l383
 (->
  sales
  (pj/lay-bar :quarter [:tax :revenue :cost] {:position :stack})))


(deftest
 t92_l386
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
   v91_l383)))


(def
 v94_l403
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/scale :color {:domain [:tax :revenue :cost]})))


(deftest
 t95_l407
 (is
  ((fn
    [v]
    (=
     ["tax" "revenue" "cost"]
     (mapv :label (-> v pj/plan :panels first :layers first :groups))))
   v94_l403)))


(def
 v97_l413
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost])
  (pj/scale :y {:domain [0 250]})))


(deftest
 t98_l417
 (is
  ((fn [v] (= [0 250] (-> v pj/plan :panels first :y-domain vec)))
   v97_l413)))


(def
 v100_l430
 (try
  (->
   {:quarter ["Q1" "Q2"],
    :revenue [120 150],
    :cost [90 100],
    :series ["a" "b"]}
   (pj/lay-bar :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t101_l439
 (is
  ((fn [msg] (re-find #"the data already has \[:series\]" msg))
   v100_l430)))


(def
 v103_l446
 (->
  {:quarter ["Q1" "Q2"],
   :revenue [120 150],
   :cost [90 100],
   :series ["a" "b"]}
  (pj/lay-bar :quarter {:series [:revenue :cost], :as :measure})))


(deftest
 t104_l452
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (= 1 (:panels (pj/svg-summary v)))
      (contains? texts "measure"))))
   v103_l446)))


(def
 v106_l468
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])))


(deftest
 t107_l473
 (is
  ((fn
    [v]
    (= ["2019" "2020"] (mapv :label (:entries (:legend (pj/plan v))))))
   v106_l468)))


(def
 v109_l480
 (->
  {"country" ["a" "b" "c"], "2019" [10 20 30], "2020" [12 25 28]}
  (pj/lay-line "country" ["2019" "2020"])
  :layers
  first
  :mapping))


(deftest
 t110_l488
 (is
  ((fn [m] (= {:color :series, :color-type :categorical} m))
   v109_l480)))


(def
 v112_l505
 (->
  {:quarter ["Q1" "Q2"], :revenue [120 nil], :cost [90 100]}
  (pj/lay-point :quarter [:revenue :cost])))


(deftest
 t113_l510
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v112_l505)))


(def
 v115_l523
 (try
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost])
   (pj/lay-line :quarter [:tax :units]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t116_l530
 (is
  ((fn [msg] (re-find #"the data already has \[:series :value\]" msg))
   v115_l523)))


(def
 v118_l537
 (pj/arrange
  [(-> sales (pj/lay-bar :quarter [:revenue :cost] {:position :dodge}))
   (-> sales (pj/lay-line :quarter [:tax :units]))]))


(deftest
 t119_l543
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v118_l537)))


(def
 v121_l553
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/lay-point :quarter :value {:color :series})))


(deftest
 t122_l557
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:points s)) (pos? (:lines s)))))
   v121_l553)))


(def
 v124_l566
 (-> sales (pj/lay-line :quarter [:revenue :cost :tax]) (pj/lay-point)))


(deftest
 t125_l570
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 3 (:lines s)))))
   v124_l566)))


(def
 v127_l582
 (->
  sales
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :series)))


(deftest
 t128_l586
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
   v127_l582)))


(def
 v130_l594
 (->
  sales
  (pj/lay-line :quarter {:series [:revenue :cost :tax], :as :measure})
  (pj/facet :measure)))


(deftest
 t131_l598
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v130_l594)))


(def
 v133_l607
 (-> sales-by-region (pj/lay-boxplot :series [:revenue :cost :tax])))


(deftest
 t134_l610
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v133_l607)))


(def
 v136_l614
 (-> sales-by-region (pj/lay-violin :series [:revenue :cost :tax])))


(deftest
 t137_l617
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v136_l614)))


(def
 v139_l625
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:shape :series})))


(deftest
 t140_l628
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 8 (:points s)) (= 4 (:polygons s)))))
   v139_l625)))


(def
 v142_l639
 (->
  sales
  (pj/lay-point :quarter [:revenue :cost :tax] {:tooltip :series})))


(deftest
 t143_l642
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
   v142_l639)))


(def
 v145_l651
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter [:revenue :cost :tax] {:size :series})))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t146_l658
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
   v145_l651)))


(def
 v148_l674
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t149_l678
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v148_l674)))


(def
 v150_l680
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :outlet)))


(deftest
 t151_l684
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v150_l680)))


(def
 v153_l692
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:group :region})))


(deftest
 t154_l695
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v153_l692)))


(def
 v156_l712
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})))


(deftest
 t157_l715
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 4 (:lines s))
      (= 2 (count (disj (:colors s) "none")))
      (contains? (set (:texts s)) "region"))))
   v156_l712)))


(def
 v159_l726
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost] {:color :region})
  (pj/facet :outlet)))


(deftest
 t160_l730
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 8 (:lines s)))))
   v159_l726)))


(def
 v162_l742
 (->
  sales-by-region
  (pj/pose {:color :region})
  (pj/lay-line :quarter [:revenue :cost])))


(deftest
 t163_l746
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (-> sales-by-region (pj/lay-line :quarter [:revenue :cost])))))
   v162_l742)))


(def
 v165_l758
 (->
  sales-by-region
  (pj/lay-line :quarter [:revenue :cost :tax])
  (pj/facet :region)
  (pj/options {:title "Measures by region"})))


(deftest
 t166_l763
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v165_l758)))


(def
 v168_l767
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/coord :flip)
  (pj/facet :region)))


(deftest
 t169_l772
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v168_l767)))


(def
 v171_l779
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})
  (pj/coord :flip)))


(deftest
 t172_l783
 (is
  ((fn
    [v]
    (= [0.0 1.0] (mapv double (-> v pj/plan :panels first :x-domain))))
   v171_l779)))


(def
 v174_l792
 (->
  sales-by-region
  (pj/lay-point
   :quarter
   {:series [:revenue :cost :tax], :as :measure, :scale {:type :log}})
  (pj/facet :region)
  (pj/options {:y-label "Euros"})))


(deftest
 t175_l799
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
   v174_l792)))


(def
 v177_l812
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)))


(deftest
 t178_l816
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v177_l812)))


(def
 v179_l818
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost] {:position :stack})
  (pj/lay-line :quarter :units)
  pj/overlay))


(deftest
 t180_l823
 (is ((fn [v] (= 1 (:panels (pj/svg-summary v)))) v179_l818)))


(def
 v182_l828
 (with-out-str
  (pj/plot
   (->
    sales
    (pj/lay-bar :quarter [:revenue :cost])
    (pj/lay-line :quarter :units)))))


(deftest
 t183_l833
 (is
  ((fn
    [out]
    (and
     (re-find #"panel of its own" out)
     (re-find #"pj/overlay" out)
     (re-find #"as series" out)))
   v182_l828)))


(def
 v185_l842
 (->
  sales-by-region
  (pj/lay-bar :quarter [:revenue :cost] {:position :dodge})
  (pj/facet :region)
  (pj/lay-rule-h {:y-intercept 120})))


(deftest
 t186_l847
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v185_l842)))


(def
 v188_l854
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))
   (-> sales (pj/lay-line :quarter :units))]))


(deftest
 t189_l860
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v188_l854)))


(def
 v191_l865
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
 t192_l875
 (is ((fn [v] (= 3 (:panels (pj/svg-summary v)))) v191_l865)))


(def
 v194_l879
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
 t195_l887
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v194_l879)))


(def
 v197_l891
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
 t198_l900
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v197_l891)))


(def
 v200_l904
 (pj/pose
  {:layout {:direction :vertical, :weights [2 1]},
   :poses
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))
    (-> sales (pj/lay-line :quarter [:revenue :cost]))]}))


(deftest
 t201_l911
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v200_l904)))


(def
 v203_l915
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
 t204_l930
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v203_l915)))


(def
 v206_l934
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
 t207_l941
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v206_l934)))


(def
 v209_l950
 (try
  (->
   sales
   (pj/pose [[:quarter :revenue] [:quarter :cost]])
   (pj/lay-point :quarter [:revenue :cost]))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t210_l957
 (is ((fn [msg] (re-find #"composite pose" msg)) v209_l950)))


(def
 v212_l970
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
 t214_l1003
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
   v212_l970)))


(def
 v216_l1026
 (->
  sales
  (pj/pose [[:quarter :revenue] [:quarter :cost]])
  (pj/lay-point)))


(deftest
 t217_l1030
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v216_l1026)))


(def v219_l1036 (-> sales (pj/lay-histogram [:revenue :cost])))


(deftest
 t220_l1039
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v219_l1036)))


(def
 v222_l1045
 (->
  sales-by-region
  (pj/lay-line :quarter :revenue {:group [:region :outlet]})))


(deftest
 t223_l1048
 (is ((fn [v] (= 4 (:lines (pj/svg-summary v)))) v222_l1045)))


(def
 v225_l1054
 (-> sales (pj/lay-line :quarter :revenue {:stroke-dash [5 5]})))


(deftest
 t226_l1057
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (count (:dash-patterns s))))))
   v225_l1054)))
