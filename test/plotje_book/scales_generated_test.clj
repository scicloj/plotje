(ns
 plotje-book.scales-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [tablecloth.api :as tc]
  [tablecloth.column.api :as tcc]
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
 (->
  gapminder-2007
  (tc/aggregate-columns
   [:gdp-percap :pop]
   (fn [xs] {:min (tcc/reduce-min xs), :max (tcc/reduce-max xs)}))))


(def
 v8_l86
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:size :pop, :color :continent})))


(deftest
 t9_l89
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
   v8_l86)))


(def
 v11_l119
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)))


(deftest
 t12_l123
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v11_l119)))


(def
 v14_l129
 (def
  linear-cell
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/options {:title "linear"}))))


(def
 v15_l134
 (def
  log-cell
  (-> linear-cell (pj/scale :x :log) (pj/options {:title "log"}))))


(def v16_l139 (pj/arrange [linear-cell log-cell]))


(deftest
 t17_l141
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
   v16_l139)))


(def
 v19_l156
 (->
  gapminder-2007
  (pj/lay-point
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t20_l160
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v19_l156)))


(def
 v22_l172
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x :log)
  pj/plan
  :panels
  first
  :x-scale))


(deftest t23_l178 (is ((fn [spec] (= {:type :log} spec)) v22_l172)))


(def
 v24_l180
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :x {:type :log})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t25_l186 (is ((fn [spec] (= {:type :log} spec)) v24_l180)))


(def
 v27_l190
 (->
  gapminder-2007
  (pj/lay-point {:x {:column :gdp-percap, :scale :log}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t28_l195 (is ((fn [spec] (= {:type :log} spec)) v27_l190)))


(def
 v29_l197
 (->
  gapminder-2007
  (pj/lay-point
   {:x {:column :gdp-percap, :scale {:type :log}}, :y :life-exp})
  pj/plan
  :panels
  first
  :x-scale))


(deftest t30_l202 (is ((fn [spec] (= {:type :log} spec)) v29_l197)))


(def
 v32_l208
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp)
   (pj/scale :x {:rnge [1 10]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l215
 (is ((fn [m] (re-find #"unexpected key\(s\): \[:rnge\]" m)) v32_l208)))


(def
 v35_l225
 (->
  gapminder-2007
  (pj/pose
   :gdp-percap
   :life-exp
   {:size {:column :pop, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :x :log)))


(deftest
 t36_l230
 (is
  ((fn
    [fr]
    (=
     {:range [3 16], :type :log}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v35_l225)))


(def
 v38_l237
 (->
  gapminder-2007
  (pj/pose :gdp-percap :life-exp)
  (pj/lay-point {:size {:column :pop, :scale :log}})
  (pj/scale :size {:range [3 16]})
  (pj/scale :x :log)))


(deftest
 t39_l243
 (is
  ((fn
    [fr]
    (=
     {:type :log, :range [3 16]}
     (-> fr pj/plan :panels first :layers first :size-scale)))
   v38_l237)))


(def
 v41_l260
 (def
  measured-radii
  [{:reading 1, :level 2, :spread 4}
   {:reading 2, :level 5, :spread 8}
   {:reading 3, :level 3, :spread 12}]))


(def
 v42_l265
 (->
  measured-radii
  (pj/pose
   :reading
   :level
   {:size {:column :spread, :scale {:range [3 16]}}})
  (pj/lay-point {:size {:column :spread, :scale false}})))


(deftest
 t43_l269
 (is
  ((fn
    [fr]
    (nil? (-> fr pj/plan :panels first :layers first :size-scale)))
   v42_l265)))


(def
 v45_l291
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


(deftest t46_l297 (is ((fn [spec] (= {:type :log} spec)) v45_l291)))


(def
 v48_l301
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
 t49_l308
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
   v48_l301)))


(def v51_l330 (tc/dataset pj/aesthetic-scales))


(deftest
 t52_l332
 (is
  ((fn
    [_]
    (=
     #{:y :color :fill :size :shape :alpha :x}
     (set (map :aesthetic pj/aesthetic-scales))))
   v51_l330)))


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
 v57_l382
 (try
  (->
   gapminder-2007
   (pj/lay-point :gdp-percap :life-exp {:color :continent})
   (pj/scale :color :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t58_l390
 (is
  ((fn [m] (re-find #"does not support :categorical scale" m))
   v57_l382)))


(def v60_l406 (def by-tens {:user [:a :b :c], :n [10 100 1000]}))


(def
 v61_l409
 (-> by-tens (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t62_l412
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (< (- mid small) (- large mid))))
   v61_l409)))


(def
 v64_l422
 (->
  by-tens
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t65_l426
 (is
  ((fn
    [v]
    (let
     [[small mid large] (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (:points (pj/svg-summary v)))
      (> (- mid small) (- large mid)))))
   v64_l422)))


(def
 v67_l436
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color :log)
  (pj/scale :x :log)))


(deftest
 t68_l441
 (is ((fn [fr] (= :log (-> fr pj/plan :legend :scale-type))) v67_l436)))


(def v70_l460 (-> gapminder-2007 (pj/lay-bar :continent)))


(deftest
 t71_l463
 (is
  ((fn
    [fr]
    (=
     ["Asia" "Europe" "Africa" "Americas" "Oceania"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v70_l460)))


(def
 v73_l473
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})))


(deftest
 t74_l476
 (is
  ((fn
    [fr]
    (= ["4" "6" "8" "5"] (->> fr pj/plan :panels first :x-domain vec)))
   v73_l473)))


(def
 v76_l482
 (-> (rdatasets/ggplot2-mpg) (pj/lay-point :displ :hwy {:color :cyl})))


(deftest
 t77_l485
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v76_l482)))


(def
 v79_l491
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :cyl, :color-type :categorical})))


(deftest
 t80_l494
 (is
  ((fn
    [fr]
    (=
     ["4" "6" "8" "5"]
     (->> fr pj/plan :legend :entries (mapv :label))))
   v79_l491)))


(def
 v82_l530
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:domain [3.0 3.5]})))


(deftest
 t83_l534
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v82_l530)))


(def
 v85_l544
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp)
  (pj/scale :y {:breaks [40 50 60 70 80]})))


(deftest
 t86_l548
 (is
  ((fn
    [fr]
    (=
     [40.0 50.0 60.0 70.0 80.0]
     (->> fr pj/plan :panels first :y-ticks :values (mapv double))))
   v85_l544)))


(def
 v88_l563
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :tick-labels ["First" "Fourth"]})))


(deftest
 t89_l568
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v88_l563)))


(def
 v91_l581
 (->
  {:bin (map (fn* [p1__11195#] (str "bin-" p1__11195#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t92_l586
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__11196#] (.startsWith p1__11196# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v91_l581)))


(def
 v94_l596
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:n-ticks 3})))


(deftest
 t95_l600
 (is
  ((fn
    [v]
    (=
     [0.0 5.0 10.0 15.0]
     (->> v pj/plan :panels first :x-ticks :values (mapv double))))
   v94_l596)))


(def
 v97_l608
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:tick-spacing 200})))


(deftest
 t98_l612
 (is
  ((fn [v] (= 2 (->> v pj/plan :panels first :x-ticks :values count)))
   v97_l608)))


(def
 v100_l631
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t101_l635
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v100_l631)))


(def
 v103_l643
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t104_l647
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v103_l643)))


(def
 v106_l656
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t107_l661
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v106_l656)))


(def
 v109_l681
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t110_l686
 (is
  ((fn [fr] (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr)))))
   v109_l681)))


(def
 v112_l718
 (def
  squares
  {:x [1 2 3 4 5 6], :y [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v113_l721
 (-> squares (pj/lay-point :x :y {:size :n}) pj/svg-summary :sizes))


(deftest
 t114_l726
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v113_l721)))


(def
 v116_l731
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t117_l735
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
   v116_l731)))


(def
 v119_l748
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t120_l753
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
   v119_l748)))


(def
 v122_l782
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
 v124_l796
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
 v125_l806
 (->
  (for
   [by [:linear :area :sqrt] :let [ms (legend-magnitudes {:by by})]]
   {:by by,
    :smallest-labelled (first ms),
    :middle (nth ms (quot (count ms) 2)),
    :largest-labelled (last ms)})
  tc/dataset))


(deftest
 t126_l814
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
   v125_l806)))


(def
 v128_l841
 (->
  squares
  (pj/lay-point :x :y {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t129_l845
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
   v128_l841)))


(def
 v131_l863
 (->
  squares
  (pj/lay-point :x :y {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t132_l867
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
   v131_l863)))


(def v134_l894 (:varies (layer-type/lookup :point)))


(deftest
 t135_l896
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v134_l894)))


(def
 v137_l928
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t138_l932
 (is
  ((fn
    [v]
    (=
     #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v137_l928)))


(def
 v140_l941
 (->
  {:district ["a" "b" "c" "d" "e" "f"],
   :share [10 20 30 40 50 60],
   :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
  (pj/lay-point :district :share {:color :party})
  (pj/scale
   :color
   {:values {"rep" "red", "dem" "blue", "ind" "green"}})))


(deftest
 t141_l947
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v140_l941)))


(def
 v143_l957
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale
   :color
   {:range {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}})))


(deftest
 t144_l961
 (is
  ((fn
    [v]
    (=
     {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}
     (-> v pj/plan :legend :color-range)))
   v143_l957)))


(def
 v146_l970
 (->
  {:x (range 40),
   :y (range 40),
   :n
   (map
    (fn* [p1__11197#] (Math/pow 10 (/ p1__11197# 10.0)))
    (range 40))}
  (pj/lay-point :x :y {:color :n})
  (pj/scale :color {:type :log, :range :viridis})))


(deftest
 t147_l976
 (is
  ((fn
    [v]
    (let
     [legend (-> v pj/plan :legend)]
     (and
      (= :log (:scale-type legend))
      (= :viridis (:color-range legend)))))
   v146_l970)))


(def
 v149_l987
 (->
  {:region ["n" "s" "e" "w" "c"],
   :year [1 2 3 4 5],
   :change [-40 -10 5 30 60]}
  (pj/lay-point :year :change {:color :change})
  (pj/scale :color {:range :diverging, :midpoint 0})))


(deftest
 t150_l993
 (is
  ((fn
    [v]
    (let
     [colours
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
        vec))]
     (not=
      (colours v)
      (colours
       (->
        {:region ["n" "s" "e" "w" "c"],
         :year [1 2 3 4 5],
         :change [-40 -10 5 30 60]}
        (pj/lay-point :year :change {:color :change})
        (pj/scale :color {:range :diverging}))))))
   v149_l987)))


(def
 v152_l1013
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
 t153_l1020
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v152_l1013)))


(def v155_l1042 pj/shape-symbols)


(deftest
 t156_l1044
 (is ((fn [syms] (= syms (distinct syms))) v155_l1042)))


(def
 v158_l1053
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t159_l1059
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
   v158_l1053)))


(def
 v161_l1073
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})
  (pj/scale :color {:label "Continent"})))


(deftest
 t162_l1078
 (is
  ((fn
    [fr]
    (=
     ["GDP per capita, log scale" "Continent"]
     (-> fr pj/plan ((juxt :x-label (comp :title :legend))))))
   v161_l1073)))


(def
 v164_l1088
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:color-label "From the options"})
  (pj/scale :color {:label "From the spec"})))


(deftest
 t165_l1093
 (is
  ((fn [fr] (= "From the spec" (-> fr pj/plan :legend :title)))
   v164_l1088)))


(def
 v167_l1108
 (->
  squares
  (pj/lay-point :x :y {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t168_l1112
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v167_l1108)))


(def
 v170_l1131
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t171_l1140
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v170_l1131)))
