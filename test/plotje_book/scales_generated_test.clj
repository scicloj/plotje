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
 v3_l33
 (def
  gapminder-2007
  (->
   (rdatasets/gapminder-gapminder)
   (tc/select-rows (fn* [p1__11193#] (= 2007 (:year p1__11193#)))))))


(def v4_l37 gapminder-2007)


(def
 v6_l42
 (kind/table
  {:column-names ["column" "lowest" "highest"],
   :row-maps
   (for
    [col [:gdp-percap :pop]]
    {"column" (kind/code (pr-str col)),
     "lowest" (reduce min (gapminder-2007 col)),
     "highest" (reduce max (gapminder-2007 col))})}))


(def
 v8_l71
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:size :pop, :color :continent})))


(deftest
 t9_l74
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
   v8_l71)))


(def
 v11_l103
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)))


(deftest
 t12_l107
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v11_l103)))


(def
 v14_l113
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v15_l118
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v16_l123 (pj/arrange [linear-cell log-cell]))


(deftest
 t17_l125
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
   v16_l123)))


(def
 v19_l140
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t20_l144
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v19_l140)))


(def
 v22_l154
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t23_l159
 (is
  ((fn
    [fr]
    (=
     {:range [3 16], :type :log}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v22_l154)))


(def
 v25_l166
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t26_l172
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v25_l166)))


(def
 v28_l190
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v29_l195
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t30_l199
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v29_l195)))


(def
 v32_l215
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t33_l221 (is ((fn [spec] (= {:type :log} spec)) v32_l215)))


(def
 v34_l223
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t35_l229 (is ((fn [spec] (= {:type :log} spec)) v34_l223)))


(def
 v37_l233
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t38_l238 (is ((fn [spec] (= {:type :log} spec)) v37_l233)))


(def
 v39_l240
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t40_l245 (is ((fn [spec] (= {:type :log} spec)) v39_l240)))


(def
 v42_l251
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t43_l258
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v42_l251)))


(def
 v45_l267
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


(deftest t46_l273 (is ((fn [spec] (= {:type :log} spec)) v45_l267)))


(def
 v48_l277
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
 t49_l284
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
   v48_l277)))


(def
 v51_l302
 (->
  gapminder-2007
  (pj/lay-point
   {:x :gdp-percap,
    :y
    {:column :life-exp,
     :scale {:domain [35 85], :breaks [40 60 80]}}})))


(deftest
 t52_l307
 (is
  ((fn
    [fr]
    (=
     [40.0 60.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v51_l302)))


(def
 v54_l321
 (->
  gapminder-2007
  (pj/pose
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/lay-point))


(deftest
 t55_l325
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v54_l321)))


(def
 v57_l353
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
 t58_l364
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :shape :alpha :x}
     (set (map :aesthetic pj/aesthetic-scales))))
   v57_l353)))


(def
 v60_l375
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:range [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t61_l382
 (is ((fn [m] (re-find #":x reads no :range" m)) v60_l375)))


(def
 v63_l408
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v64_l411
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
 v66_l425
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
 v67_l435
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
 t68_l445
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
   v67_l435)))


(def
 v70_l463
 (-> squares (pj/lay-point :x :y {:size :n}) pj/svg-summary :sizes))


(deftest
 t71_l468
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v70_l463)))


(def
 v73_l473
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t74_l477
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
   v73_l473)))


(def
 v76_l499
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t77_l503
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
   v76_l499)))


(def v79_l528 (:varies (layer-type/lookup :point)))


(deftest
 t80_l530
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v79_l528)))


(def
 v82_l555
 (->
  squares
  (pj/lay-point :x :y {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t83_l559
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v82_l555)))


(def
 v85_l575
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t86_l584
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v85_l575)))


(def
 v88_l594
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:domain [35 85], :breaks [40 50 60 70 80]})))


(deftest
 t89_l598
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v88_l594)))


(def v91_l612 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t92_l615
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v91_l612)))


(def
 v94_l622
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:type :categorical,
    :domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t95_l627
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v94_l622)))


(def
 v97_l637
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :cyl :hwy)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t98_l645
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v97_l637)))


(def
 v100_l652
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t101_l655
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v100_l652)))


(def
 v103_l664
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t104_l668
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v103_l664)))


(def
 v106_l677
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t107_l682
 (is
  ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v106_l677)))


(def
 v109_l689
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t110_l692
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v109_l689)))


(def
 v112_l700
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t113_l703
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v112_l700)))


(def
 v115_l712
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})
  (pj/scale :color {:domain [4 5 6 8]})))


(deftest
 t116_l716
 (is
  ((fn
    [fr]
    (=
     ["4" "5" "6" "8"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v115_l712)))


(def
 v118_l724
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"]})
  (pj/scale :x :log)))


(deftest
 t119_l729
 (is
  ((fn
    [fr]
    (=
     ["Africa" "Americas" "Asia" "Europe" "Oceania"]
     (mapv :label (:entries (:shape-legend (pj/plan fr))))))
   v118_l724)))


(def
 v121_l738
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t122_l743
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v121_l738)))


(def
 v124_l757
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t125_l762
 (is
  ((fn [fr] (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr)))))
   v124_l757)))
