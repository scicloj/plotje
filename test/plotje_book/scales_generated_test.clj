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
   (tc/select-rows (fn* [p1__72264#] (= 2007 (:year p1__72264#)))))))


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
        [p1__72265#]
        (-> p1__72265# :plan :panels first :x-scale :type))))))
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
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v42_l267
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t43_l271
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v42_l267)))


(def
 v45_l293
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


(deftest t46_l299 (is ((fn [spec] (= {:type :log} spec)) v45_l293)))


(def
 v48_l303
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
 t49_l310
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
   v48_l303)))


(def v51_l332 (tc/dataset pj/aesthetic-scales))


(deftest
 t52_l334
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :shape :alpha :x}
     (set (map :aesthetic pj/aesthetic-scales))))
   v51_l332)))


(def
 v54_l345
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t55_l352
 (is ((fn [m] (re-find #":x reads no :range" m)) v54_l345)))


(def
 v57_l384
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp {:color :continent})
   (pj/scale :color :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t58_l392
 (is
  ((fn [m] (re-find #"does not support a :categorical scale" m))
   v57_l384)))


(def v60_l408 (def by-tens {:user [:a :b :c], :n [10 100 1000]}))


(def
 v61_l411
 (-> by-tens (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t62_l414
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (< (- mid small) (- large mid))))
   v61_l411)))


(def
 v64_l424
 (->
  by-tens
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t65_l428
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (:points (pj/svg-summary v)))
      (> (- mid small) (- large mid)))))
   v64_l424)))


(def
 v67_l438
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t68_l443
 (is ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v67_l438)))


(def v70_l462 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t71_l465
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v70_l462)))


(def
 v73_l475
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t74_l478
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v73_l475)))


(def
 v76_l484
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t77_l487
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v76_l484)))


(def
 v79_l493
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t80_l496
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v79_l493)))


(def
 v82_l532
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:domain [3.0 3.5]})))


(deftest
 t83_l536
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v82_l532)))


(def
 v85_l550
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:include 0})))


(deftest
 t86_l554
 (is
  ((fn
    [fr]
    (= [0.0 86.73315] (-> fr pj/plan :panels first :y-domain vec)))
   v85_l550)))


(def
 v88_l568
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:include [0 100]})))


(deftest
 t89_l572
 (is
  ((fn
    [fr]
    (= [0.0 100.0] (-> fr pj/plan :panels first :y-domain vec)))
   v88_l568)))


(def
 v91_l580
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:domain [100 0]})))


(deftest
 t92_l584
 (is
  ((fn
    [fr]
    (=
     [100.0 90.0 80.0 70.0 60.0 50.0 40.0 30.0 20.0 10.0 0.0]
     (-> fr pj/plan :panels first :y-ticks :values vec)))
   v91_l580)))


(def
 v94_l600
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks [40 50 60 70 80]})))


(deftest
 t95_l604
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v94_l600)))


(def
 v97_l619
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :tick-labels ["First" "Fourth"]})))


(deftest
 t98_l624
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v97_l619)))


(def
 v100_l634
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
 t101_l641
 (is
  ((fn
    [fr]
    (=
     ["2016" "2020" "2024"]
     (->> fr pj/plan :panels first :x-ticks :labels)))
   v100_l634)))


(def
 v103_l653
 (->
  {:when
   (mapv (fn [year] (jt/local-date year 6 15)) (range 2015 2025)),
   :reading [12 15 11 18 22 19 25 24 28 31]}
  (pj/lay-line :when :reading)
  (pj/scale
   :x
   {:domain [(jt/local-date 2010 1 1) (jt/local-date 2030 1 1)]})))


(deftest
 t104_l659
 (is
  ((fn
    [fr]
    (=
     ["2011"
      "2013"
      "2015"
      "2017"
      "2019"
      "2021"
      "2023"
      "2025"
      "2027"
      "2029"]
     (->> fr pj/plan :panels first :x-ticks :labels vec)))
   v103_l653)))


(def
 v106_l671
 (->
  {:bin (map (fn* [p1__72266#] (str "bin-" p1__72266#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t107_l676
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__72267#] (.startsWith p1__72267# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v106_l671)))


(def
 v109_l686
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:n-ticks 3})))


(deftest
 t110_l690
 (is
  ((fn
    [v]
    (=
     [0.0 5.0 10.0 15.0]
     (->> v pj/plan :panels first :x-ticks :values (mapv double))))
   v109_l686)))


(def
 v112_l701
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:tick-spacing 200})))


(deftest
 t113_l705
 (is
  ((fn [v] (= 2 (->> v pj/plan :panels first :x-ticks :values count)))
   v112_l701)))


(def
 v115_l724
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t116_l728
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v115_l724)))


(def
 v118_l736
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t119_l740
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v118_l736)))


(def
 v121_l754
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t122_l759
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v121_l754)))


(def
 v124_l767
 (->
  {:day ["Mon" "Mon" "Mon" "Tue" "Tue" "Tue"],
   :meal ["breakfast" "lunch" "dinner" "breakfast" "lunch" "dinner"],
   :n [10 30 20 12 25 30]}
  (pj/lay-bar :day :n {:color :meal, :position :stack})
  (pj/scale :color {:domain ["dinner" "lunch" "breakfast"]})))


(deftest
 t125_l773
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
   v124_l767)))


(def
 v127_l790
 (def
  two-grades
  {:grade [1 2 1 2], :hours [3 9 2 11], :score [72 88 64 91]}))


(def
 v128_l795
 (->
  two-grades
  (pj/lay-point
   :hours
   :score
   {:color :grade, :color-type :categorical})
  (pj/scale :color {:domain [2 1]})))


(deftest
 t129_l799
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
   v128_l795)))


(def
 v131_l836
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t132_l841
 (is
  ((fn
    [fr]
    (and
     (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr))))
     (= (tc/row-count gapminder-2007) (:points (pj/svg-summary fr)))))
   v131_l836)))


(def
 v134_l857
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-tile :displ :hwy)
  (pj/scale :fill {:domain [0 60]})))


(deftest
 t135_l861
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
   v134_l857)))


(def
 v137_l895
 (def
  squares
  {:step [1 2 3 4 5 6], :row [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v138_l898
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  pj/svg-summary
  :sizes))


(deftest
 t139_l903
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v138_l898)))


(def
 v141_l908
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t142_l912
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
   v141_l908)))


(def
 v144_l929
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t145_l934
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
   v144_l929)))


(def
 v147_l970
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
 v149_l984
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
 v150_l994
 (->
  (for
   [by [:linear :area :sqrt] :let [ms (legend-magnitudes {:by by})]]
   {:by by,
    :smallest-labelled (first ms),
    :middle (nth ms (quot (count ms) 2)),
    :largest-labelled (last ms)})
  tc/dataset))


(deftest
 t151_l1002
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
         (fn [ns] (mapv (fn* [p1__72268#] (* 100 p1__72268#)) ns)))
        (pj/lay-point :step :row {:size :n})
        pj/plan
        :size-legend
        :entries)))))
   v150_l994)))


(def
 v153_l1040
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t154_l1044
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
   v153_l1040)))


(def
 v156_l1062
 (->
  squares
  (pj/lay-point :step :row {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t157_l1066
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
   v156_l1062)))


(def v159_l1095 (:varies (layer-type/lookup :point)))


(deftest
 t160_l1097
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v159_l1095)))


(def
 v162_l1129
 (try
  (->
   {:hour [1 2 3], :day [1 2 3], :shift ["early" "late" "early"]}
   (pj/lay-tile :hour :day {:fill :shift})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t163_l1136
 (is ((fn [m] (re-find #":fill needs a numeric column" m)) v162_l1129)))


(def
 v165_l1144
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t166_l1148
 (is
  ((fn
    [v]
    (=
     #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v165_l1144)))


(def
 v168_l1157
 (->
  {:district ["a" "b" "c" "d" "e" "f"],
   :share [10 20 30 40 50 60],
   :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
  (pj/lay-point :district :share {:color :party})
  (pj/scale
   :color
   {:values {"rep" "red", "dem" "blue", "ind" "green"}})))


(deftest
 t169_l1163
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v168_l1157)))


(def
 v171_l1178
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale
   :color
   {:range {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}})))


(deftest
 t172_l1182
 (is
  ((fn
    [v]
    (=
     {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}
     (-> v pj/plan :legend :color-range)))
   v171_l1178)))


(def
 v174_l1189
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale :color {:range (fn [t] [t 0.0 (- 1.0 t) 1.0])})))


(deftest
 t175_l1193
 (is
  ((fn
    [v]
    (let
     [stops (-> v pj/plan :legend :stops)]
     (and
      (= [0.0 0.0 1.0 1.0] (:color (first stops)))
      (= 0.0 (second (:color (last stops)))))))
   v174_l1189)))


(def
 v177_l1206
 (->
  {:step (range 40),
   :row (range 40),
   :n
   (map
    (fn* [p1__72269#] (Math/pow 10 (/ p1__72269# 10.0)))
    (range 40))}
  (pj/lay-point :step :row {:color :n})
  (pj/scale :color {:type :log, :range :viridis})))


(deftest
 t178_l1212
 (is
  ((fn
    [v]
    (let
     [legend (-> v pj/plan :legend)]
     (and
      (= :log (:scale-type legend))
      (= :viridis (:color-range legend)))))
   v177_l1206)))


(def
 v180_l1223
 (->
  {:region ["n" "s" "e" "w" "c"],
   :year [1 2 3 4 5],
   :change [-40 -10 5 30 60]}
  (pj/lay-point :year :change {:color :change})
  (pj/scale :color {:range :diverging, :midpoint 0})))


(deftest
 t181_l1229
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
         {:region ["n" "s" "e" "w" "c"],
          :year [1 2 3 4 5],
          :change [-40 -10 5 30 60]}
         (pj/lay-point :year :change {:color :change})
         (pj/scale :color {:range :diverging}))))
      (< 0.16666 (:gradient-t (first stops)) 0.16667)
      (== 1.0 (:gradient-t (last stops))))))
   v180_l1223)))


(def
 v183_l1264
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
 t184_l1271
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v183_l1264)))


(def v186_l1293 pj/shape-symbols)


(deftest
 t187_l1295
 (is ((fn [syms] (= syms (distinct syms))) v186_l1293)))


(def
 v189_l1304
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t190_l1310
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
   v189_l1304)))


(def
 v192_l1324
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})
  (pj/scale :color {:label "Continent"})))


(deftest
 t193_l1329
 (is
  ((fn
    [fr]
    (=
     ["GDP per capita, log scale" "Continent"]
     (-> fr pj/plan ((juxt :x-label (comp :title :legend))))))
   v192_l1324)))


(def
 v195_l1339
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:color-label "From the options"})
  (pj/scale :color {:label "From the spec"})))


(deftest
 t196_l1344
 (is
  ((fn [fr] (= "From the spec" (-> fr pj/plan :legend :title)))
   v195_l1339)))


(def
 v198_l1361
 (->
  squares
  (pj/lay-point :step :row {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t199_l1365
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v198_l1361)))


(def
 v201_l1389
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t202_l1398
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v201_l1389)))


(def
 v204_l1404
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp {:size :pop})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/lay-point {})
  (pj/scale :x :log)))


(deftest
 t205_l1410
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
   v204_l1404)))
