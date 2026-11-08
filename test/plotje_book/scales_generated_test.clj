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
   (tc/select-rows (fn* [p1__87741#] (= 2007 (:year p1__87741#)))))))


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
 v14_l136
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v15_l141
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v16_l146 (pj/arrange [linear-cell log-cell]))


(deftest
 t17_l148
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
        [p1__87742#]
        (-> p1__87742# :plan :panels first :x-scale :type))))))
   v16_l146)))


(def
 v19_l164
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t20_l168
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v19_l164)))


(def
 v22_l183
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t23_l191 (is ((fn [spec] (= {:type :log} spec)) v22_l183)))


(def
 v24_l193
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t25_l201 (is ((fn [spec] (= {:type :log} spec)) v24_l193)))


(def
 v27_l205
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t28_l212 (is ((fn [spec] (= {:type :log} spec)) v27_l205)))


(def
 v29_l214
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t30_l221 (is ((fn [spec] (= {:type :log} spec)) v29_l214)))


(def
 v32_l227
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l234
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v32_l227)))


(def
 v35_l245
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t36_l250
 (is
  ((fn
    [fr]
    (=
     {:range [3 16], :type :log}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v35_l245)))


(def
 v38_l263
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t39_l269
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v38_l263)))


(def
 v41_l291
 (->
  gapminder-2007
  (pj/pose {})
  (pj/scale :x :log)
  (pj/pose :gdp-percap :life-exp)
  pj/lay-point))


(deftest
 t42_l297
 (is
  ((fn [fr] (= {:type :log} (-> fr pj/plan :panels first :x-scale)))
   v41_l291)))


(def
 v44_l311
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/scale :x :log)
  (pj/pose {:x nil})
  (pj/pose {:x :pop})
  pj/lay-point))


(deftest
 t45_l318
 (is
  ((fn [fr] (= {:type :linear} (-> fr pj/plan :panels first :x-scale)))
   v44_l311)))


(def
 v47_l331
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:color :continent})
  (pj/scale :color {:label "Continent"})
  (pj/scale :x :log)
  pj/lay-point
  (pj/lay-smooth {:color "black"})))


(deftest
 t48_l338
 (is
  ((fn
    [fr]
    (and
     (not-any?
      #{"black"}
      (map :label (:entries (:legend (pj/plan fr)))))
     (contains? (:colors (pj/svg-summary fr)) "rgb(0,0,0)")))
   v47_l331)))


(def
 v50_l346
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:color :continent})
  (pj/scale :color {:label "Continent"})
  (pj/scale :x :log)
  pj/lay-point
  (pj/lay-smooth {:color {:value "trend", :scale true}})))


(deftest
 t51_l353
 (is
  ((fn
    [fr]
    (= "trend" (:label (last (:entries (:legend (pj/plan fr)))))))
   v50_l346)))


(def
 v53_l362
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v54_l367
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t55_l371
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v54_l367)))


(def
 v57_l399
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


(deftest t58_l409 (is ((fn [spec] (= {:type :log} spec)) v57_l399)))


(def
 v60_l413
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
 t61_l424
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
   v60_l413)))


(def v63_l450 (tc/dataset pj/aesthetic-scales))


(deftest
 t64_l452
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :col :shape :alpha :x :row}
     (set (map :aesthetic pj/aesthetic-scales))))
   v63_l450)))


(def
 v66_l463
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t67_l470
 (is ((fn [m] (re-find #":x reads no :range" m)) v66_l463)))


(def
 v69_l501
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp {:color :continent})
   (pj/scale :color :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t70_l509
 (is
  ((fn
    [m]
    (re-find
     #"has no :categorical type; its types are \[:linear :log\]"
     m))
   v69_l501)))


(def v72_l525 (def by-tens {:user [:a :b :c], :n [10 100 1000]}))


(def
 v73_l528
 (-> by-tens (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t74_l531
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (< (- mid small) (- large mid))))
   v73_l528)))


(def
 v76_l541
 (->
  by-tens
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t77_l545
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (:points (pj/svg-summary v)))
      (> (- mid small) (- large mid)))))
   v76_l541)))


(def
 v79_l555
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t80_l560
 (is ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v79_l555)))


(def v82_l582 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t83_l585
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v82_l582)))


(def
 v85_l600
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t86_l603
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v85_l600)))


(def
 v88_l614
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t89_l617
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v88_l614)))


(def
 v91_l626
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t92_l629
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v91_l626)))


(def
 v94_l669
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:domain [3.0 3.5]})))


(deftest
 t95_l673
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v94_l669)))


(def
 v97_l686
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:include 0})))


(deftest
 t98_l690
 (is
  ((fn
    [fr]
    (= [0.0 86.73315] (-> fr pj/plan :panels first :y-domain vec)))
   v97_l686)))


(def
 v100_l711
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:include [0 100]})))


(deftest
 t101_l715
 (is
  ((fn
    [fr]
    (= [0.0 100.0] (-> fr pj/plan :panels first :y-domain vec)))
   v100_l711)))


(def
 v103_l728
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:domain [100 0]})))


(deftest
 t104_l732
 (is
  ((fn
    [fr]
    (=
     [100.0 90.0 80.0 70.0 60.0 50.0 40.0 30.0 20.0 10.0 0.0]
     (-> fr pj/plan :panels first :y-ticks :values vec)))
   v103_l728)))


(def
 v106_l754
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks [40 50 60 70 80]})))


(deftest
 t107_l758
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v106_l754)))


(def
 v109_l773
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks []})))


(deftest
 t110_l777
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
   v109_l773)))


(def
 v112_l805
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :tick-labels ["First" "Fourth"]})))


(deftest
 t113_l810
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v112_l805)))


(def
 v115_l820
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
 t116_l827
 (is
  ((fn
    [fr]
    (=
     ["2016" "2020" "2024"]
     (->> fr pj/plan :panels first :x-ticks :labels)))
   v115_l820)))


(def
 v118_l844
 (->
  {:when
   (mapv (fn [year] (jt/local-date year 6 15)) (range 2015 2025)),
   :reading [12 15 11 18 22 19 25 24 28 31]}
  (pj/lay-line :when :reading)
  (pj/scale
   :x
   {:domain [(jt/local-date 2010 1 1) (jt/local-date 2030 1 1)]})))


(deftest
 t119_l850
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
   v118_l844)))


(def
 v121_l868
 (->
  {:bin (map (fn* [p1__87743#] (str "bin-" p1__87743#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t122_l873
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__87744#] (.startsWith p1__87744# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v121_l868)))


(def
 v124_l883
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:n-ticks 3})))


(deftest
 t125_l887
 (is
  ((fn
    [v]
    (=
     [0.0 5.0 10.0 15.0]
     (->> v pj/plan :panels first :x-ticks :values (mapv double))))
   v124_l883)))


(def
 v127_l904
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:tick-spacing 200})))


(deftest
 t128_l908
 (is
  ((fn [v] (= 2 (->> v pj/plan :panels first :x-ticks :values count)))
   v127_l904)))


(def
 v130_l933
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t131_l937
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v130_l933)))


(def
 v133_l950
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t134_l954
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v133_l950)))


(def
 v136_l973
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t137_l978
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v136_l973)))


(def
 v139_l986
 (->
  {:day ["Mon" "Mon" "Mon" "Tue" "Tue" "Tue"],
   :meal ["breakfast" "lunch" "dinner" "breakfast" "lunch" "dinner"],
   :n [10 30 20 12 25 30]}
  (pj/lay-bar :day :n {:color :meal, :position :stack})
  (pj/scale :color {:domain ["dinner" "lunch" "breakfast"]})))


(deftest
 t140_l992
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
   v139_l986)))


(def
 v142_l1014
 (def
  two-grades
  {:grade [1 2 1 2], :hours [3 9 2 11], :score [72 88 64 91]}))


(def
 v143_l1019
 (->
  two-grades
  (pj/lay-point
   :hours
   :score
   {:color :grade, :color-type :categorical})
  (pj/scale :color {:domain [2 1]})))


(deftest
 t144_l1023
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
   v143_l1019)))


(def
 v146_l1063
 (with-out-str
  (->
   {:team ["red" "green"], :score [3 5]}
   (pj/lay-bar :team :score)
   (pj/scale :x {:domain ["red" "green" "blue"]})
   pj/plan)))


(deftest
 t147_l1069
 (is
  ((fn
    [out]
    (and
     (re-find #":domain names \[\"blue\"\]" out)
     (re-find #"adds none to them" out)))
   v146_l1063)))


(def
 v149_l1075
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
 t150_l1084
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
   v149_l1075)))


(def
 v152_l1118
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t153_l1123
 (is
  ((fn
    [fr]
    (and
     (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr))))
     (= (tc/row-count gapminder-2007) (:points (pj/svg-summary fr)))))
   v152_l1118)))


(def
 v155_l1139
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-tile :displ :hwy)
  (pj/scale :fill {:domain [0 60]})))


(deftest
 t156_l1143
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
   v155_l1139)))


(def
 v158_l1178
 (def
  squares
  {:step [1 2 3 4 5 6], :row [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v159_l1181
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  pj/svg-summary
  :sizes))


(deftest
 t160_l1186
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v159_l1181)))


(def
 v162_l1191
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t163_l1195
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
   v162_l1191)))


(def
 v165_l1212
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t166_l1217
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
   v165_l1212)))


(def
 v168_l1260
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
 v170_l1279
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
 v171_l1289
 (->
  (for
   [by [:linear :area :sqrt] :let [ms (legend-magnitudes {:by by})]]
   {:by by,
    :smallest-labelled (first ms),
    :middle (nth ms (quot (count ms) 2)),
    :largest-labelled (last ms)})
  tc/dataset))


(deftest
 t172_l1297
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
         (fn [ns] (mapv (fn* [p1__87745#] (* 100 p1__87745#)) ns)))
        (pj/lay-point :step :row {:size :n})
        pj/plan
        :size-legend
        :entries)))))
   v171_l1289)))


(def
 v174_l1335
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t175_l1339
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
   v174_l1335)))


(def
 v177_l1359
 (->
  squares
  (pj/lay-point :step :row {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t178_l1363
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
   v177_l1359)))


(def v180_l1395 (:varies (layer-type/lookup :point)))


(deftest
 t181_l1397
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v180_l1395)))


(def
 v183_l1429
 (try
  (->
   {:hour [1 2 3], :day [1 2 3], :shift ["early" "late" "early"]}
   (pj/lay-tile :hour :day {:fill :shift})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t184_l1436
 (is ((fn [m] (re-find #":fill needs a numeric column" m)) v183_l1429)))


(def
 v186_l1442
 (->
  {:hour [1 2 3 1 2 3], :day [1 1 1 2 2 2], :load [10 20 30 40 50 60]}
  (pj/lay-tile :hour :day {:color :load})))


(deftest
 t187_l1447
 (is
  ((fn
    [v]
    (let
     [legend (:legend (pj/plan v))]
     (and (= :continuous (:type legend)) (= :load (:title legend)))))
   v186_l1442)))


(def
 v189_l1458
 (->
  {:hour [1 2 3 1 2 3],
   :day [1 1 1 2 2 2],
   :shift ["early" "late" "early" "late" "early" "late"]}
  (pj/lay-tile :hour :day {:color :shift})))


(deftest
 t190_l1463
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
   v189_l1458)))


(def
 v192_l1474
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t193_l1478
 (is
  ((fn
    [v]
    (=
     #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v192_l1474)))


(def
 v195_l1487
 (->
  {:district ["a" "b" "c" "d" "e" "f"],
   :share [10 20 30 40 50 60],
   :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
  (pj/lay-point :district :share {:color :party})
  (pj/scale
   :color
   {:values {"rep" "red", "dem" "blue", "ind" "green"}})))


(deftest
 t196_l1493
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v195_l1487)))


(def
 v198_l1508
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale
   :color
   {:range {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}})))


(deftest
 t199_l1512
 (is
  ((fn
    [v]
    (=
     {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}
     (-> v pj/plan :legend :color-range)))
   v198_l1508)))


(def
 v201_l1523
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale :color {:range {:low "blue", :high "red"}})))


(deftest
 t202_l1527
 (is
  ((fn
    [v]
    (let
     [stops
      (-> v pj/plan :legend :stops)
      [r g b]
      (:color (nth stops (quot (count stops) 2)))]
     (and (< g 0.2) (> r 0.2) (> b 0.2))))
   v201_l1523)))


(def
 v204_l1541
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale :color {:range (fn [t] [t 0.0 (- 1.0 t) 1.0])})))


(deftest
 t205_l1545
 (is
  ((fn
    [v]
    (let
     [stops (-> v pj/plan :legend :stops)]
     (and
      (= [0.0 0.0 1.0 1.0] (:color (first stops)))
      (= 0.0 (second (:color (last stops)))))))
   v204_l1541)))


(def
 v207_l1561
 (->
  {:step (range 40),
   :row (range 40),
   :n
   (map
    (fn* [p1__87746#] (Math/pow 10 (/ p1__87746# 10.0)))
    (range 40))}
  (pj/lay-point :step :row {:color :n})
  (pj/scale :color {:type :log, :range :viridis})))


(deftest
 t208_l1567
 (is
  ((fn
    [v]
    (let
     [legend (-> v pj/plan :legend)]
     (and
      (= :log (:scale-type legend))
      (= :viridis (:color-range legend)))))
   v207_l1561)))


(def
 v210_l1580
 (->
  {:year [1 2 3 4 5], :change [-40 -10 5 30 60]}
  (pj/lay-point :year :change {:color :change, :size 6, :alpha 1})
  (pj/scale :color {:range :diverging, :midpoint 0})
  (pj/options {:point-stroke "grey", :point-stroke-width 1})))


(deftest
 t211_l1586
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
   v210_l1580)))


(def
 v213_l1643
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
 t214_l1650
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v213_l1643)))


(def v216_l1673 (pj/shape-palette))


(deftest
 t217_l1675
 (is ((fn [syms] (= syms (distinct syms))) v216_l1673)))


(def v219_l1684 (pj/shape-symbols))


(deftest
 t220_l1686
 (is
  ((fn
    [syms]
    (and
     (=
      (pj/shape-palette)
      (vec (take (count (pj/shape-palette)) syms)))
     (some #{:circle-open} syms)))
   v219_l1684)))


(def
 v222_l1695
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :circle-open})
  (pj/scale :x :log)))


(deftest
 t223_l1699
 (is
  ((fn
    [fr]
    (let
     [plan (pj/plan fr) layer (first (:layers (first (:panels plan))))]
     (and
      (nil? (:shape-legend plan))
      (= :circle-open (:shape (:style layer))))))
   v222_l1695)))


(def
 v225_l1715
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t226_l1721
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
   v225_l1715)))


(def
 v228_l1732
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:shape :species})
  (pj/scale
   :shape
   {:values
    {"setosa" :square, "versicolor" :triangle, "virginica" :diamond}})))


(deftest
 t229_l1738
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
   v228_l1732)))


(def
 v231_l1753
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})))


(deftest
 t232_l1756
 (is
  ((fn
    [fr]
    (let
     [texts (set (:texts (pj/svg-summary fr)))]
     (every? texts ["gdp percap" "life exp" "continent"])))
   v231_l1753)))


(def
 v234_l1763
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})
  (pj/scale :color {:label "Continent"})))


(deftest
 t235_l1768
 (is
  ((fn
    [fr]
    (=
     ["GDP per capita, log scale" "Continent"]
     (-> fr pj/plan ((juxt :x-label (comp :title :legend))))))
   v234_l1763)))


(def
 v237_l1781
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:x-label "GDP per capita", :color-label "Continent"})))


(deftest
 t238_l1785
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= "GDP per capita" (:x-label p))
      (= "Continent" (:title (:legend p))))))
   v237_l1781)))


(def
 v240_l1793
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:color-label "From the options"})
  (pj/scale :color {:label "From the spec"})))


(deftest
 t241_l1798
 (is
  ((fn
    [fr]
    (let
     [texts (set (:texts (pj/svg-summary fr)))]
     (and
      (= "From the spec" (-> fr pj/plan :legend :title))
      (not (texts "From the options")))))
   v240_l1793)))


(def
 v243_l1820
 (->
  squares
  (pj/lay-point :step :row {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t244_l1824
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v243_l1820)))


(def
 v246_l1848
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t247_l1857
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v246_l1848)))


(def
 v249_l1863
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size :pop})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/lay-point {})
  (pj/scale :x :log)))


(deftest
 t250_l1869
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
   v249_l1863)))
