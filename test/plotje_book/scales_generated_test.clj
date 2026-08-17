(ns
 plotje-book.scales-generated-test
 (:require
  [clojure.string :as str]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [tablecloth.column.api :as tcc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l37
 (def
  gapminder-2007
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows (fn* [p1__11193#] (= 2007 (:year p1__11193#)))))))


(def v4_l41 gapminder-2007)


(def
 v6_l46
 (->
  gapminder-2007
  (tc/aggregate-columns
   [:gdp-percap :pop]
   (fn [xs] {:min (tcc/reduce-min xs), :max (tcc/reduce-max xs)}))))


(def
 v8_l77
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:size :pop, :color :continent})))


(deftest
 t9_l80
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
   v8_l77)))


(def
 v11_l110
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)))


(deftest
 t12_l114
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v11_l110)))


(def
 v14_l120
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v15_l125
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v16_l130 (pj/arrange [linear-cell log-cell]))


(deftest
 t17_l132
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
   v16_l130)))


(def
 v19_l147
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t20_l151
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v19_l147)))


(def
 v22_l163
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t23_l169 (is ((fn [spec] (= {:type :log} spec)) v22_l163)))


(def
 v24_l171
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t25_l177 (is ((fn [spec] (= {:type :log} spec)) v24_l171)))


(def
 v27_l181
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t28_l186 (is ((fn [spec] (= {:type :log} spec)) v27_l181)))


(def
 v29_l188
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t30_l193 (is ((fn [spec] (= {:type :log} spec)) v29_l188)))


(def
 v32_l199
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l206
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v32_l199)))


(def
 v35_l216
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t36_l221
 (is
  ((fn
    [fr]
    (=
     {:range [3 16], :type :log}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v35_l216)))


(def
 v38_l228
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t39_l234
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v38_l228)))


(def
 v41_l251
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v42_l256
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t43_l260
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v42_l256)))


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
 v51_l321
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
 t52_l332
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :shape :alpha :x}
     (set (map :aesthetic pj/aesthetic-scales))))
   v51_l321)))


(def
 v54_l343
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t55_l350
 (is ((fn [m] (re-find #":x reads no :range" m)) v54_l343)))


(def
 v57_l378
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp {:color :continent})
   (pj/scale :color :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t58_l386
 (is
  ((fn [m] (re-find #"does not support :categorical scale" m))
   v57_l378)))


(def v60_l402 (def by-tens {:user [:a :b :c], :n [10 100 1000]}))


(def
 v61_l405
 (-> by-tens (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t62_l408
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (< (- mid small) (- large mid))))
   v61_l405)))


(def
 v64_l418
 (->
  by-tens
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t65_l422
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (:points (pj/svg-summary v)))
      (> (- mid small) (- large mid)))))
   v64_l418)))


(def
 v67_l432
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t68_l437
 (is ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v67_l432)))


(def v70_l448 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t71_l451
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v70_l448)))


(def
 v73_l462
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :cyl :hwy)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t74_l470
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v73_l462)))


(def
 v76_l477
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t77_l480
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v76_l477)))


(def
 v79_l486
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t80_l489
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v79_l486)))


(def
 v82_l495
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t83_l498
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v82_l495)))


(def
 v85_l536
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:domain [3.0 3.5]})))


(deftest
 t86_l540
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v85_l536)))


(def
 v88_l551
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks [40 50 60 70 80]})))


(deftest
 t89_l555
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v88_l551)))


(def
 v91_l570
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :labels ["First" "Fourth"]})))


(deftest
 t92_l575
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v91_l570)))


(def
 v94_l588
 (->
  {:bin (map (fn* [p1__11195#] (str "bin-" p1__11195#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t95_l593
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__11196#] (.startsWith p1__11196# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v94_l588)))


(def
 v97_l603
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})))


(deftest
 t98_l607
 (is
  ((fn [fr] (= "GDP per capita, log scale" (-> fr pj/plan :x-label)))
   v97_l603)))


(def
 v100_l617
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t101_l621
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v100_l617)))


(def
 v103_l629
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t104_l633
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v103_l629)))


(def
 v106_l642
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t107_l647
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v106_l642)))


(def
 v109_l667
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t110_l672
 (is
  ((fn [fr] (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr)))))
   v109_l667)))


(def
 v112_l704
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v113_l707
 (-> squares (pj/lay-point :x :y {:size :n}) pj/svg-summary :sizes))


(deftest
 t114_l712
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v113_l707)))


(def
 v116_l717
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t117_l721
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
   v116_l717)))


(def
 v119_l734
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t120_l739
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
   v119_l734)))


(def
 v122_l768
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
 v124_l782
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
 v125_l792
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
 t126_l802
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
   v125_l792)))


(def
 v128_l829
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t129_l833
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
   v128_l829)))


(def
 v131_l851
 (->
  squares
  (pj/lay-point :x :y {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t132_l855
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
   v131_l851)))


(def v134_l882 (:varies (layer-type/lookup :point)))


(deftest
 t135_l884
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v134_l882)))


(def v137_l909 pj/shape-symbols)


(deftest
 t138_l911
 (is ((fn [syms] (= syms (distinct syms))) v137_l909)))


(def
 v140_l920
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t141_l926
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
   v140_l920)))


(def
 v143_l945
 (->
  squares
  (pj/lay-point :x :y {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t144_l949
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v143_l945)))


(def
 v146_l968
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t147_l977
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v146_l968)))
