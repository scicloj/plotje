(ns
 plotje-book.scales-generated-test
 (:require
  [clojure.string :as str]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (def
  gapminder-2007
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows (fn* [p1__11193#] (= 2007 (:year p1__11193#)))))))


(def v4_l39 gapminder-2007)


(def
 v6_l44
 (kind/table
  {:column-names ["column" "lowest" "highest"],
   :row-maps
   (for
    [col [:gdp-percap :pop]]
    {"column" (kind/code (pr-str col)),
     "lowest" (reduce min (gapminder-2007 col)),
     "highest" (reduce max (gapminder-2007 col))})}))


(def
 v8_l76
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:size :pop, :color :continent})))


(deftest
 t9_l79
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
   v8_l76)))


(def
 v11_l108
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)))


(deftest
 t12_l112
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v11_l108)))


(def
 v14_l118
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v15_l123
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v16_l128 (pj/arrange [linear-cell log-cell]))


(deftest
 t17_l130
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
        [p1__11194#]
        (-> p1__11194# :plan :panels first :x-scale :type))))))
   v16_l128)))


(def
 v19_l145
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t20_l149
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v19_l145)))


(def
 v22_l161
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t23_l167 (is ((fn [spec] (= {:type :log} spec)) v22_l161)))


(def
 v24_l169
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t25_l175 (is ((fn [spec] (= {:type :log} spec)) v24_l169)))


(def
 v27_l179
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t28_l184 (is ((fn [spec] (= {:type :log} spec)) v27_l179)))


(def
 v29_l186
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t30_l191 (is ((fn [spec] (= {:type :log} spec)) v29_l186)))


(def
 v32_l197
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l204
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v32_l197)))


(def
 v35_l214
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t36_l219
 (is
  ((fn
    [fr]
    (=
     {:range [3 16], :type :log}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v35_l214)))


(def
 v38_l226
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t39_l232
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v38_l226)))


(def
 v41_l250
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v42_l255
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t43_l259
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v42_l255)))


(def
 v45_l282
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


(deftest t46_l288 (is ((fn [spec] (= {:type :log} spec)) v45_l282)))


(def
 v48_l292
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
 t49_l299
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
   v48_l292)))


(def
 v51_l322
 (kind/table
  {:column-names ["Aesthetic" "Types" "Beside :type and :domain"],
   :row-maps
   (for
    [group (partition-by (juxt :types :keys) pj/aesthetic-scales)]
    (let
     [names (fn [ks] (str/join ", " (map pr-str ks)))]
     {"Aesthetic" (kind/code (names (map :aesthetic group))),
      "Types" (kind/code (names (:types (first group)))),
      "Beside :type and :domain"
      (if-let
       [ks (seq (:keys (first group)))]
       (kind/code (names ks))
       "--")}))}))


(deftest
 t52_l333
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :shape :alpha :x}
     (set (map :aesthetic pj/aesthetic-scales))))
   v51_l322)))


(def
 v54_l344
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t55_l351
 (is ((fn [m] (re-find #":x reads no :range" m)) v54_l344)))


(def
 v57_l379
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp {:color :continent})
   (pj/scale :color :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t58_l387
 (is
  ((fn [m] (re-find #"does not support :categorical scale" m))
   v57_l379)))


(def v60_l403 (def by-tens {:user [:a :b :c], :n [10 100 1000]}))


(def
 v61_l406
 (-> by-tens (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t62_l409
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (< (- mid small) (- large mid))))
   v61_l406)))


(def
 v64_l419
 (->
  by-tens
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t65_l423
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (:points (pj/svg-summary v)))
      (> (- mid small) (- large mid)))))
   v64_l419)))


(def
 v67_l433
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t68_l438
 (is ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v67_l433)))


(def v70_l449 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t71_l452
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v70_l449)))


(def
 v73_l463
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :cyl :hwy)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t74_l471
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v73_l463)))


(def
 v76_l478
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t77_l481
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v76_l478)))


(def
 v79_l487
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t80_l490
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v79_l487)))


(def
 v82_l496
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t83_l499
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v82_l496)))


(def
 v85_l537
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:domain [3.0 3.5]})))


(deftest
 t86_l541
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v85_l537)))


(def
 v88_l552
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks [40 50 60 70 80]})))


(deftest
 t89_l556
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v88_l552)))


(def
 v91_l571
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :labels ["First" "Fourth"]})))


(deftest
 t92_l576
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v91_l571)))


(def
 v94_l589
 (->
  {:bin (map (fn* [p1__11195#] (str "bin-" p1__11195#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t95_l594
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__11196#] (.startsWith p1__11196# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v94_l589)))


(def
 v97_l604
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})))


(deftest
 t98_l608
 (is
  ((fn [fr] (= "GDP per capita, log scale" (-> fr pj/plan :x-label)))
   v97_l604)))


(def
 v100_l618
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t101_l622
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v100_l618)))


(def
 v103_l630
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t104_l634
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v103_l630)))


(def
 v106_l643
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t107_l648
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v106_l643)))


(def
 v109_l668
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t110_l673
 (is
  ((fn [fr] (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr)))))
   v109_l668)))


(def
 v112_l705
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v113_l708
 (-> squares (pj/lay-point :x :y {:size :n}) pj/svg-summary :sizes))


(deftest
 t114_l713
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v113_l708)))


(def
 v116_l718
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t117_l722
 (is
  ((fn
    [fr]
    (let
     [widest
      (->>
       fr
       pj/plan
       :size-legend
       :entries
       (map :magnitude)
       (apply max)
       double)]
     (< 8.0 widest 20.0)))
   v116_l718)))


(def
 v119_l735
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t120_l740
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
   v119_l735)))


(def
 v122_l769
 (->
  (pj/arrange
   [(->
     squares
     (pj/lay-point :x :y {:size :n})
     (pj/options {:title ":sqrt (default)"}))
    (->
     squares
     (pj/lay-point :x :y {:size :n})
     (pj/scale :size {:by :linear})
     (pj/options {:title ":linear"}))
    (->
     squares
     (pj/lay-point :x :y {:size :n})
     (pj/scale :size {:by :area})
     (pj/options {:title ":area"}))])
  (pj/options {:width 900, :height 340})))


(def
 v124_l783
 (defn
  legend-magnitudes
  [spec]
  (->>
   (->
    squares
    (pj/lay-point :x :y {:size :n})
    (pj/scale :size spec)
    pj/plan
    :size-legend
    :entries)
   (mapv :magnitude))))


(def
 v125_l793
 (kind/table
  {:column-names
   ["by" "smallest labelled" "middle" "largest labelled"],
   :row-maps
   (for
    [by
     [:linear :area :sqrt]
     :let
     [ms
      (legend-magnitudes {:by by})
      mid
      (nth ms (quot (count ms) 2))]]
    {"by" (kind/code (pr-str by)),
     "smallest labelled" (format "%.2f" (first ms)),
     "middle" (format "%.2f" mid),
     "largest labelled" (format "%.2f" (last ms))})}))


(deftest
 t126_l803
 (is
  ((fn
    [_]
    (every?
     (fn [[l a s]] (< l a s))
     (map
      vector
      (legend-magnitudes {:by :linear})
      (legend-magnitudes {:by :area})
      (legend-magnitudes {:by :sqrt}))))
   v125_l793)))


(def
 v128_l830
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t129_l834
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
   v128_l830)))


(def
 v131_l852
 (->
  squares
  (pj/lay-point :x :y {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t132_l856
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
   v131_l852)))


(def v134_l884 (:varies (layer-type/lookup :point)))


(deftest
 t135_l886
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v134_l884)))


(def v137_l911 pj/shape-symbols)


(deftest
 t138_l913
 (is ((fn [syms] (= syms (distinct syms))) v137_l911)))


(def
 v140_l922
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t141_l928
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
   v140_l922)))


(def
 v143_l947
 (->
  squares
  (pj/lay-point :x :y {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t144_l951
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v143_l947)))


(def
 v146_l970
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t147_l979
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v146_l970)))
