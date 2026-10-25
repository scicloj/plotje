(ns
 plotje-book.scales-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [tablecloth.column.api :as tcc]
  [java-time.api :as jt]
  [clojure.test :refer [deftest is]]))


(def
 v3_l37
 (def
  gapminder-2007
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows (fn* [p1__83046#] (= 2007 (:year p1__83046#)))))))


(def v4_l41 gapminder-2007)


(def
 v6_l46
 (->
  gapminder-2007
  (tc/aggregate-columns
   [:gdp-percap :pop]
   (fn [xs] {:min (tcc/reduce-min xs), :max (tcc/reduce-max xs)}))))


(def
 v8_l88
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:size :pop, :color :continent})))


(deftest
 t9_l91
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr) panel (first (:panels p))]
     (and
      (some? (:x-scale panel))
      (some? (:y-scale panel))
      (some? (:size-legend p))
      (some? (:legend p)))))
   v8_l88)))


(def
 v11_l121
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)))


(deftest
 t12_l125
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v11_l121)))


(def
 v14_l131
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v15_l136
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v16_l141 (pj/arrange [linear-cell log-cell]))


(deftest
 t17_l143
 (is
  ((fn
    [fr]
    (=
     [:linear :log]
     (->>
      (pj/plan fr)
      :sub-plots
      (mapv
       (fn*
        [p1__83047#]
        (-> p1__83047# :plan :panels first :x-scale :type))))))
   v16_l141)))


(def
 v19_l158
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t20_l162
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v19_l158)))


(def
 v22_l174
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t23_l180 (is ((fn [spec] (= {:type :log} spec)) v22_l174)))


(def
 v24_l182
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t25_l188 (is ((fn [spec] (= {:type :log} spec)) v24_l182)))


(def
 v27_l192
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t28_l197 (is ((fn [spec] (= {:type :log} spec)) v27_l192)))


(def
 v29_l199
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t30_l204 (is ((fn [spec] (= {:type :log} spec)) v29_l199)))


(def
 v32_l210
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l217
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v32_l210)))


(def
 v35_l227
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t36_l232
 (is
  ((fn
    [fr]
    (=
     {:range [3 16], :type :log}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v35_l227)))


(def
 v38_l239
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t39_l245
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v38_l239)))


(def
 v41_l262
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:color :continent})
  (pj/scale :color {:label "Continent"})
  (pj/scale :x :log)
  pj/lay-point
  (pj/lay-smooth {:color "black"})))


(deftest
 t42_l269
 (is
  ((fn
    [fr]
    (and
     (not-any?
      #{"black"}
      (map :label (:entries (:legend (pj/plan fr)))))
     (contains? (:colors (pj/svg-summary fr)) "rgb(0,0,0)")))
   v41_l262)))


(def
 v44_l277
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:color :continent})
  (pj/scale :color {:label "Continent"})
  (pj/scale :x :log)
  pj/lay-point
  (pj/lay-smooth {:color {:value "trend", :scale true}})))


(deftest
 t45_l284
 (is
  ((fn
    [fr]
    (= "trend" (:label (last (:entries (:legend (pj/plan fr)))))))
   v44_l277)))


(def
 v47_l293
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v48_l298
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t49_l302
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v48_l298)))


(def
 v51_l324
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size {:column :pop, :scale :log}})
  pj/lay-point
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest t52_l330 (is ((fn [spec] (= {:type :log} spec)) v51_l324)))


(def
 v54_l334
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size :pop})
  pj/lay-point
  (pj/scale :size :log)
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest
 t55_l341
 (is
  ((fn
    [spec]
    (=
     spec
     (->
      gapminder-2007
      (pj/pose
       :gdp-percap
       :life-exp
       {:size {:column :pop, :scale :log}})
      pj/lay-point
      pj/plan
      :panels
      first
      :layers
      first
      :size-scale)))
   v54_l334)))


(def v57_l363 (tc/dataset pj/aesthetic-scales))


(deftest
 t58_l365
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :col :shape :alpha :x :row}
     (set (map :aesthetic pj/aesthetic-scales))))
   v57_l363)))


(def
 v60_l376
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t61_l383
 (is ((fn [m] (re-find #":x reads no :range" m)) v60_l376)))


(def
 v63_l414
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp {:color :continent})
   (pj/scale :color :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t64_l422
 (is
  ((fn
    [m]
    (re-find
     #"has no :categorical type; its types are \[:linear :log\]"
     m))
   v63_l414)))


(def v66_l438 (def by-tens {:user [:a :b :c], :n [10 100 1000]}))


(def
 v67_l441
 (-> by-tens (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t68_l444
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (< (- mid small) (- large mid))))
   v67_l441)))


(def
 v70_l454
 (->
  by-tens
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t71_l458
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (:points (pj/svg-summary v)))
      (> (- mid small) (- large mid)))))
   v70_l454)))


(def
 v73_l468
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t74_l473
 (is ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v73_l468)))


(def v76_l492 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t77_l495
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v76_l492)))


(def
 v79_l505
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t80_l508
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v79_l505)))


(def
 v82_l514
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t83_l517
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v82_l514)))


(def
 v85_l523
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t86_l526
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v85_l523)))


(def
 v88_l562
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:domain [3.0 3.5]})))


(deftest
 t89_l566
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v88_l562)))


(def
 v91_l580
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:include 0})))


(deftest
 t92_l584
 (is
  ((fn
    [fr]
    (= [0.0 86.73315] (-> fr pj/plan :panels first :y-domain vec)))
   v91_l580)))


(def
 v94_l598
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:include [0 100]})))


(deftest
 t95_l602
 (is
  ((fn
    [fr]
    (= [0.0 100.0] (-> fr pj/plan :panels first :y-domain vec)))
   v94_l598)))


(def
 v97_l610
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:domain [100 0]})))


(deftest
 t98_l614
 (is
  ((fn
    [fr]
    (=
     [100.0 90.0 80.0 70.0 60.0 50.0 40.0 30.0 20.0 10.0 0.0]
     (-> fr pj/plan :panels first :y-ticks :values vec)))
   v97_l610)))


(def
 v100_l630
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks [40 50 60 70 80]})))


(deftest
 t101_l634
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v100_l630)))


(def
 v103_l643
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks []})))


(deftest
 t104_l647
 (is
  ((fn
    [fr]
    (let
     [plan (pj/plan fr)]
     (and
      (empty? (-> plan :panels first :y-ticks :values))
      (contains? (set (:texts (pj/svg-summary fr))) "life exp")
      (seq
       (->
        fr
        (pj/scale :y {:breaks nil})
        pj/plan
        :panels
        first
        :y-ticks
        :values)))))
   v103_l643)))


(def
 v106_l666
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :tick-labels ["First" "Fourth"]})))


(deftest
 t107_l671
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v106_l666)))


(def
 v109_l681
 (->
  {:when
   (mapv (fn [year] (jt/local-date year 6 15)) (range 2015 2025)),
   :reading [12 15 11 18 22 19 25 24 28 31]}
  (pj/lay-line :when :reading)
  (pj/scale
   :x
   {:breaks
    [(jt/local-date 2016 1 1)
     (jt/local-date 2020 1 1)
     (jt/local-date 2024 1 1)]})))


(deftest
 t110_l688
 (is
  ((fn
    [fr]
    (=
     ["2016" "2020" "2024"]
     (->> fr pj/plan :panels first :x-ticks :labels)))
   v109_l681)))


(def
 v112_l700
 (->
  {:when
   (mapv (fn [year] (jt/local-date year 6 15)) (range 2015 2025)),
   :reading [12 15 11 18 22 19 25 24 28 31]}
  (pj/lay-line :when :reading)
  (pj/scale
   :x
   {:domain [(jt/local-date 2010 1 1) (jt/local-date 2030 1 1)]})))


(deftest
 t113_l706
 (is
  ((fn
    [fr]
    (=
     ["2010"
      "2012"
      "2014"
      "2016"
      "2018"
      "2020"
      "2022"
      "2024"
      "2026"
      "2028"
      "2030"]
     (->> fr pj/plan :panels first :x-ticks :labels vec)))
   v112_l700)))


(def
 v115_l718
 (->
  {:bin (map (fn* [p1__83048#] (str "bin-" p1__83048#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t116_l723
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__83049#] (.startsWith p1__83049# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v115_l718)))


(def
 v118_l733
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:n-ticks 3})))


(deftest
 t119_l737
 (is
  ((fn
    [v]
    (=
     [0.0 5.0 10.0 15.0]
     (->> v pj/plan :panels first :x-ticks :values (mapv double))))
   v118_l733)))


(def
 v121_l748
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:tick-spacing 200})))


(deftest
 t122_l752
 (is
  ((fn [v] (= 2 (->> v pj/plan :panels first :x-ticks :values count)))
   v121_l748)))


(def
 v124_l771
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t125_l775
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v124_l771)))


(def
 v127_l783
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t128_l787
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v127_l783)))


(def
 v130_l801
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t131_l806
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v130_l801)))


(def
 v133_l814
 (->
  {:day ["Mon" "Mon" "Mon" "Tue" "Tue" "Tue"],
   :meal ["breakfast" "lunch" "dinner" "breakfast" "lunch" "dinner"],
   :n [10 30 20 12 25 30]}
  (pj/lay-bar :day :n {:color :meal, :position :stack})
  (pj/scale :color {:domain ["dinner" "lunch" "breakfast"]})))


(deftest
 t134_l820
 (is
  ((fn
    [fr]
    (let
     [plan
      (pj/plan fr)
      groups
      (-> plan :panels first :layers first :groups)]
     (and
      (=
       ["dinner" "lunch" "breakfast"]
       (mapv :label (:entries (:legend plan))))
      (= ["dinner" "lunch" "breakfast"] (mapv :label groups))
      (every? zero? (:y0s (last groups))))))
   v133_l814)))


(def
 v136_l837
 (def
  two-grades
  {:grade [1 2 1 2], :hours [3 9 2 11], :score [72 88 64 91]}))


(def
 v137_l842
 (->
  two-grades
  (pj/lay-point
   :hours
   :score
   {:color :grade, :color-type :categorical})
  (pj/scale :color {:domain [2 1]})))


(deftest
 t138_l846
 (is
  ((fn
    [fr]
    (let
     [on-axis
      (->
       two-grades
       (pj/lay-point :grade :score {:x-type :categorical})
       (pj/scale :x {:domain [2 1]})
       pj/plan
       :panels
       first
       :x-domain
       vec)
      on-shape
      (->
       two-grades
       (pj/lay-point :hours :score {:shape :grade})
       (pj/scale :shape {:domain [2 1]})
       pj/plan
       :shape-legend
       :entries
       (->> (mapv :label)))
      on-color
      (-> fr pj/plan :legend :entries (->> (mapv :label)))]
     (= ["2" "1"] on-axis on-color on-shape)))
   v137_l842)))


(def
 v140_l878
 (with-out-str
  (->
   {:team ["red" "green"], :score [3 5]}
   (pj/lay-bar :team :score)
   (pj/scale :x {:domain ["red" "green" "blue"]})
   pj/plan)))


(deftest
 t141_l884
 (is
  ((fn
    [out]
    (and
     (re-find #":domain names \[\"blue\"\]" out)
     (re-find #"adds none to them" out)))
   v140_l878)))


(def
 v143_l890
 (->
  {:team ["red" "green"], :score [3 5]}
  (pj/lay-bar :team :score)
  (pj/scale :x {:domain ["red" "green" "blue"]})
  pj/plan
  :panels
  first
  :x-domain
  vec))


(deftest
 t144_l899
 (is
  ((fn
    [d]
    (and
     (= ["red" "green"] d)
     (try
      (->
       {:team ["red" "green"], :score [3 5]}
       (pj/lay-bar :team :score)
       (pj/scale :x {:domain ["red" "green" "blue"]})
       (pj/lay-text {:x 3, :y 4, :text "note"})
       pj/plan)
      false
      (catch
       Exception
       e
       (boolean
        (re-find #"past the ends of this axis" (ex-message e)))))))
   v143_l890)))


(def
 v146_l933
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t147_l938
 (is
  ((fn
    [fr]
    (and
     (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr))))
     (= (tc/row-count gapminder-2007) (:points (pj/svg-summary fr)))))
   v146_l933)))


(def
 v149_l954
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-tile :displ :hwy)
  (pj/scale :fill {:domain [0 60]})))


(deftest
 t150_l958
 (is
  ((fn
    [fr]
    (and
     (= [0.0 60.0] ((juxt :min :max) (:legend (pj/plan fr))))
     (=
      [0.0 19.0]
      ((juxt :min :max)
       (:legend
        (pj/plan
         (-> (rdatasets/ggplot2-mpg) (pj/lay-tile :displ :hwy))))))))
   v149_l954)))


(def
 v152_l993
 (def
  squares
  {:step [1 2 3 4 5 6], :row [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v153_l996
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  pj/svg-summary
  :sizes))


(deftest
 t154_l1001
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v153_l996)))


(def
 v156_l1006
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t157_l1010
 (is
  ((fn
    [fr]
    (let
     [radii
      (sort (:sizes (pj/svg-summary fr)))
      default
      (sort
       (:sizes
        (pj/svg-summary
         (-> squares (pj/lay-point :step :row {:size :n})))))]
     (and
      (= [3.0 20.0] [(first radii) (last radii)])
      (= (count default) (count radii))
      (every? true? (map < default radii)))))
   v156_l1006)))


(def
 v159_l1027
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t160_l1032
 (is
  ((fn
    [fr]
    (=
     (->> fr pj/plan :alpha-legend :entries (mapv :alpha))
     (->>
      (->
       gapminder-2007
       (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
       (pj/scale :x :log))
      pj/plan
      :alpha-legend
      :entries
      (mapv :alpha))))
   v159_l1027)))


(def
 v162_l1068
 (->
  (pj/arrange
   [(->
     squares
     (pj/lay-point :step :row {:size :n})
     (pj/options {:title ":sqrt (default)"}))
    (->
     squares
     (pj/lay-point :step :row {:size :n})
     (pj/scale :size {:by :linear})
     (pj/options {:title ":linear"}))
    (->
     squares
     (pj/lay-point :step :row {:size :n})
     (pj/scale :size {:by :area})
     (pj/options {:title ":area"}))])
  (pj/options {:width 900, :height 340})))


(def
 v164_l1082
 (defn
  legend-magnitudes
  [spec]
  (mapv
   :magnitude
   (->
    squares
    (pj/lay-point :step :row {:size :n})
    (pj/scale :size spec)
    pj/plan
    :size-legend
    :entries))))


(def
 v165_l1092
 (->
  (for
   [by [:linear :area :sqrt] :let [ms (legend-magnitudes {:by by})]]
   {:by by,
    :smallest-labelled (first ms),
    :middle (nth ms (quot (count ms) 2)),
    :largest-labelled (last ms)})
  tc/dataset))


(deftest
 t166_l1100
 (is
  ((fn
    [_]
    (and
     (every?
      (fn [[l a s]] (< l a s))
      (map
       vector
       (legend-magnitudes {:by :linear})
       (legend-magnitudes {:by :area})
       (legend-magnitudes {:by :sqrt})))
     (=
      (legend-magnitudes {})
      (mapv
       :magnitude
       (->
        (update
         squares
         :n
         (fn [ns] (mapv (fn* [p1__83050#] (* 100 p1__83050#)) ns)))
        (pj/lay-point :step :row {:size :n})
        pj/plan
        :size-legend
        :entries)))))
   v165_l1092)))


(def
 v168_l1138
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t169_l1142
 (is
  ((fn
    [fr]
    (let
     [{:keys [entries]}
      (-> fr pj/plan :size-legend)
      area
      (fn [e] (Math/pow (:magnitude e) 2))
      by-value
      (into {} (map (juxt :value area) entries))]
     (every?
      (fn
       [[v a]]
       (if-let
        [half (by-value (/ v 2))]
        (< (Math/abs (- (/ a half) 2.0)) 1.0E-6)
        true))
      by-value)))
   v168_l1138)))


(def
 v171_l1160
 (->
  squares
  (pj/lay-point :step :row {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t172_l1164
 (is
  ((fn
    [fr]
    (let
     [entries
      (->> fr pj/plan :alpha-legend :entries)
      by-value
      (into {} (map (juxt :value :alpha) entries))]
     (every?
      (fn
       [[v a]]
       (if-let
        [half (by-value (/ v 2))]
        (< (Math/abs (- (/ a half) 2.0)) 1.0E-6)
        true))
      by-value)))
   v171_l1160)))


(def v174_l1193 (:varies (layer-type/lookup :point)))


(deftest
 t175_l1195
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v174_l1193)))


(def
 v177_l1227
 (try
  (->
   {:hour [1 2 3], :day [1 2 3], :shift ["early" "late" "early"]}
   (pj/lay-tile :hour :day {:fill :shift})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t178_l1234
 (is ((fn [m] (re-find #":fill needs a numeric column" m)) v177_l1227)))


(def
 v180_l1240
 (->
  {:hour [1 2 3 1 2 3], :day [1 1 1 2 2 2], :load [10 20 30 40 50 60]}
  (pj/lay-tile :hour :day {:color :load})))


(deftest
 t181_l1245
 (is
  ((fn
    [v]
    (let
     [legend (:legend (pj/plan v))]
     (and (= :continuous (:type legend)) (= :load (:title legend)))))
   v180_l1240)))


(def
 v183_l1256
 (->
  {:hour [1 2 3 1 2 3],
   :day [1 1 1 2 2 2],
   :shift ["early" "late" "early" "late" "early" "late"]}
  (pj/lay-tile :hour :day {:color :shift})))


(deftest
 t184_l1261
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 6 (:visible-tiles s))
      (= #{"none" "rgb(55,126,184)" "rgb(228,26,28)"} (:colors s))
      (=
       ["early" "late"]
       (mapv :label (:entries (:legend (pj/plan v))))))))
   v183_l1256)))


(def
 v186_l1272
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t187_l1276
 (is
  ((fn
    [v]
    (=
     #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v186_l1272)))


(def
 v189_l1285
 (->
  {:district ["a" "b" "c" "d" "e" "f"],
   :share [10 20 30 40 50 60],
   :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
  (pj/lay-point :district :share {:color :party})
  (pj/scale
   :color
   {:values {"rep" "red", "dem" "blue", "ind" "green"}})))


(deftest
 t190_l1291
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v189_l1285)))


(def
 v192_l1306
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale
   :color
   {:range {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}})))


(deftest
 t193_l1310
 (is
  ((fn
    [v]
    (=
     {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}
     (-> v pj/plan :legend :color-range)))
   v192_l1306)))


(def
 v195_l1318
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale :color {:range {:low "blue", :high "red"}})))


(deftest
 t196_l1322
 (is
  ((fn
    [v]
    (let
     [stops
      (-> v pj/plan :legend :stops)
      [r g b]
      (:color (nth stops (quot (count stops) 2)))]
     (and (< g 0.2) (> r 0.2) (> b 0.2))))
   v195_l1318)))


(def
 v198_l1333
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale :color {:range (fn [t] [t 0.0 (- 1.0 t) 1.0])})))


(deftest
 t199_l1337
 (is
  ((fn
    [v]
    (let
     [stops (-> v pj/plan :legend :stops)]
     (and
      (= [0.0 0.0 1.0 1.0] (:color (first stops)))
      (= 0.0 (second (:color (last stops)))))))
   v198_l1333)))


(def
 v201_l1350
 (->
  {:step (range 40),
   :row (range 40),
   :n
   (map
    (fn* [p1__83051#] (Math/pow 10 (/ p1__83051# 10.0)))
    (range 40))}
  (pj/lay-point :step :row {:color :n})
  (pj/scale :color {:type :log, :range :viridis})))


(deftest
 t202_l1356
 (is
  ((fn
    [v]
    (let
     [legend (-> v pj/plan :legend)]
     (and
      (= :log (:scale-type legend))
      (= :viridis (:color-range legend)))))
   v201_l1350)))


(def
 v204_l1367
 (->
  {:year [1 2 3 4 5], :change [-40 -10 5 30 60]}
  (pj/lay-point :year :change {:color :change, :size 6, :alpha 1})
  (pj/scale :color {:range :diverging, :midpoint 0})
  (pj/options {:point-stroke "grey", :point-stroke-width 1})))


(deftest
 t205_l1373
 (is
  ((fn
    [v]
    (let
     [colors
      (fn
       [p]
       (->
        p
        pj/plan
        :panels
        first
        :layers
        first
        :groups
        first
        :colors
        vec))
      stops
      (-> v pj/plan :legend :stops)]
     (and
      (not=
       (colors v)
       (colors
        (->
         {:year [1 2 3 4 5], :change [-40 -10 5 30 60]}
         (pj/lay-point
          :year
          :change
          {:color :change, :size 6, :alpha 1})
         (pj/scale :color {:range :diverging})
         (pj/options {:point-stroke "grey", :point-stroke-width 1}))))
      (< 0.16666 (:gradient-t (first stops)) 0.16667)
      (== 1.0 (:gradient-t (last stops)))
      (=
       ["-40" "-20" "0" "20" "40" "60"]
       (mapv :label (-> v pj/plan :legend :ticks))))))
   v204_l1367)))


(def
 v207_l1416
 (->
  {:district ["a" "b" "c" "d" "e" "f"],
   :share [10 20 30 40 50 60],
   :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
  (pj/lay-point :district :share {:color :party})
  (pj/options
   {:color-values {"rep" "grey", "dem" "grey", "ind" "grey"}})
  (pj/scale
   :color
   {:values {"rep" "red", "dem" "blue", "ind" "green"}})))


(deftest
 t208_l1423
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v207_l1416)))


(def v210_l1446 (pj/shape-palette))


(deftest
 t211_l1448
 (is ((fn [syms] (= syms (distinct syms))) v210_l1446)))


(def v213_l1457 (pj/shape-symbols))


(deftest
 t214_l1459
 (is
  ((fn
    [syms]
    (and
     (=
      (pj/shape-palette)
      (vec (take (count (pj/shape-palette)) syms)))
     (some #{:circle-open} syms)))
   v213_l1457)))


(def
 v216_l1468
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :circle-open})
  (pj/scale :x :log)))


(deftest
 t217_l1472
 (is
  ((fn
    [fr]
    (let
     [plan (pj/plan fr) layer (first (:layers (first (:panels plan))))]
     (and
      (nil? (:shape-legend plan))
      (= :circle-open (:shape (:style layer))))))
   v216_l1468)))


(def
 v219_l1488
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t220_l1494
 (is
  ((fn
    [fr]
    (=
     [["Africa" :circle]
      ["Americas" :square]
      ["Asia" :triangle]
      ["Europe" :diamond]
      ["Oceania" :cross]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan fr))))))
   v219_l1488)))


(def
 v222_l1505
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:values
    {"setosa" :square, "versicolor" :triangle, "virginica" :diamond}})))


(deftest
 t223_l1511
 (is
  ((fn
    [fr]
    (=
     [["setosa" :square]
      ["versicolor" :triangle]
      ["virginica" :diamond]]
     (mapv
      (juxt :label :shape)
      (:entries (:shape-legend (pj/plan fr))))))
   v222_l1505)))


(def
 v225_l1526
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})))


(deftest
 t226_l1529
 (is
  ((fn
    [fr]
    (let
     [texts (set (:texts (pj/svg-summary fr)))]
     (every? texts ["gdp percap" "life exp" "continent"])))
   v225_l1526)))


(def
 v228_l1536
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})
  (pj/scale :color {:label "Continent"})))


(deftest
 t229_l1541
 (is
  ((fn
    [fr]
    (=
     ["GDP per capita, log scale" "Continent"]
     (-> fr pj/plan ((juxt :x-label (comp :title :legend))))))
   v228_l1536)))


(def
 v231_l1552
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:x-label "GDP per capita", :color-label "Continent"})))


(deftest
 t232_l1556
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= "GDP per capita" (:x-label p))
      (= "Continent" (:title (:legend p))))))
   v231_l1552)))


(def
 v234_l1564
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:color-label "From the options"})
  (pj/scale :color {:label "From the spec"})))


(deftest
 t235_l1569
 (is
  ((fn
    [fr]
    (let
     [texts (set (:texts (pj/svg-summary fr)))]
     (and
      (= "From the spec" (-> fr pj/plan :legend :title))
      (not (texts "From the options")))))
   v234_l1564)))


(def
 v237_l1588
 (->
  squares
  (pj/lay-point :step :row {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t238_l1592
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v237_l1588)))


(def
 v240_l1616
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t241_l1625
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v240_l1616)))


(def
 v243_l1631
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size :pop})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/lay-point {})
  (pj/scale :x :log)))


(deftest
 t244_l1637
 (is
  ((fn
    [fr]
    (let
     [plan (pj/plan fr)]
     (and
      (=
       [{:type :log} {:type :log}]
       (mapv :size-scale (-> plan :panels first :layers)))
      (=
       {:type :log}
       (->
        gapminder-2007
        (pj/pose :gdp-percap :life-exp)
        (pj/lay-point {:x {:column :gdp-percap, :scale :log}})
        (pj/lay-point {})
        pj/plan
        :panels
        first
        :x-scale)))))
   v243_l1631)))
