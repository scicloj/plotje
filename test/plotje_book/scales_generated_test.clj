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
   (tc/select-rows (fn* [p1__73986#] (= 2007 (:year p1__73986#)))))))


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
        [p1__73987#]
        (-> p1__73987# :plan :panels first :x-scale :type))))))
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
  ((fn [m] (re-find #"does not support a :categorical scale" m))
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
  {:bin (map (fn* [p1__73988#] (str "bin-" p1__73988#)) (range 40)),
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
       (fn* [p1__73989#] (.startsWith p1__73989# "bin-"))
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
 v97_l611
 (->
  {:hour (range 20), :load (range 20)}
  (pj/lay-point :hour :load)
  (pj/scale :x {:tick-spacing 200})))


(deftest
 t98_l615
 (is
  ((fn [v] (= 2 (->> v pj/plan :panels first :x-ticks :values count)))
   v97_l611)))


(def
 v100_l634
 (->
  gapminder-2007
  (pj/lay-bar :continent)
  (pj/scale
   :x
   {:domain ["Oceania" "Africa" "Asia" "Americas" "Europe"]})))


(deftest
 t101_l638
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Africa" "Asia" "Americas" "Europe"]
     (->> fr pj/plan :panels first :x-domain vec)))
   v100_l634)))


(def
 v103_l646
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :cyl :hwy {:x-type :categorical})
  (pj/scale :x {:domain [4 5 6 8]})))


(deftest
 t104_l650
 (is
  ((fn
    [fr]
    (= ["4" "5" "6" "8"] (->> fr pj/plan :panels first :x-domain vec)))
   v103_l646)))


(def
 v106_l661
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale
   :color
   {:domain ["Oceania" "Europe" "Asia" "Americas" "Africa"]})
  (pj/scale :x :log)))


(deftest
 t107_l666
 (is
  ((fn
    [fr]
    (=
     ["Oceania" "Europe" "Asia" "Americas" "Africa"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v106_l661)))


(def
 v109_l674
 (def
  two-grades
  {:grade [1 2 1 2], :hours [3 9 2 11], :score [72 88 64 91]}))


(def
 v110_l679
 (->
  two-grades
  (pj/lay-point
   :hours
   :score
   {:color :grade, :color-type :categorical})
  (pj/scale :color {:domain [2 1]})))


(deftest
 t111_l683
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
   v110_l679)))


(def
 v113_l720
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :pop})
  (pj/scale :color {:domain [0 5.0E7]})
  (pj/scale :x :log)))


(deftest
 t114_l725
 (is
  ((fn
    [fr]
    (and
     (= [0.0 5.0E7] ((juxt :min :max) (:legend (pj/plan fr))))
     (= (tc/row-count gapminder-2007) (:points (pj/svg-summary fr)))))
   v113_l720)))


(def
 v116_l741
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-tile :displ :hwy)
  (pj/scale :fill {:domain [0 60]})))


(deftest
 t117_l745
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
   v116_l741)))


(def
 v119_l779
 (def
  squares
  {:step [1 2 3 4 5 6], :row [1 1 1 1 1 1], :n [1 4 9 16 25 36]}))


(def
 v120_l782
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  pj/svg-summary
  :sizes))


(deftest
 t121_l787
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v120_l782)))


(def
 v123_l792
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:range [3 20]})))


(deftest
 t124_l796
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
   v123_l792)))


(def
 v126_l813
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:alpha :pop})
  (pj/scale :alpha {:range [0.1 1.0]})
  (pj/scale :x :log)))


(deftest
 t127_l818
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
   v126_l813)))


(def
 v129_l854
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
 v131_l868
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
 v132_l878
 (->
  (for
   [by [:linear :area :sqrt] :let [ms (legend-magnitudes {:by by})]]
   {:by by,
    :smallest-labelled (first ms),
    :middle (nth ms (quot (count ms) 2)),
    :largest-labelled (last ms)})
  tc/dataset))


(deftest
 t133_l886
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
         (fn [ns] (mapv (fn* [p1__73990#] (* 100 p1__73990#)) ns)))
        (pj/lay-point :step :row {:size :n})
        pj/plan
        :size-legend
        :entries)))))
   v132_l878)))


(def
 v135_l924
 (->
  squares
  (pj/lay-point :step :row {:size :n})
  (pj/scale :size {:by :area, :from-zero true})))


(deftest
 t136_l928
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
   v135_l924)))


(def
 v138_l946
 (->
  squares
  (pj/lay-point :step :row {:alpha :n})
  (pj/scale :alpha {:from-zero true})))


(deftest
 t139_l950
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
   v138_l946)))


(def v141_l979 (:varies (layer-type/lookup :point)))


(deftest
 t142_l981
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v141_l979)))


(def
 v144_l1013
 (try
  (->
   {:hour [1 2 3], :day [1 2 3], :shift ["early" "late" "early"]}
   (pj/lay-tile :hour :day {:fill :shift})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t145_l1020
 (is ((fn [m] (re-find #":fill needs a numeric column" m)) v144_l1013)))


(def
 v147_l1028
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :color {:values ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t148_l1032
 (is
  ((fn
    [v]
    (=
     #{"rgb(231,76,60)" "rgb(52,152,219)" "rgb(46,204,113)"}
     (disj (:colors (pj/svg-summary v)) "none")))
   v147_l1028)))


(def
 v150_l1041
 (->
  {:district ["a" "b" "c" "d" "e" "f"],
   :share [10 20 30 40 50 60],
   :party ["rep" "dem" "dem" "ind" "rep" "ind"]}
  (pj/lay-point :district :share {:color :party})
  (pj/scale
   :color
   {:values {"rep" "red", "dem" "blue", "ind" "green"}})))


(deftest
 t151_l1047
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v150_l1041)))


(def
 v153_l1062
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale
   :color
   {:range {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}})))


(deftest
 t154_l1066
 (is
  ((fn
    [v]
    (=
     {:low "#2166AC", :mid "#F7F7F7", :high "#B2182B"}
     (-> v pj/plan :legend :color-range)))
   v153_l1062)))


(def
 v156_l1073
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :petal-length})
  (pj/scale :color {:range (fn [t] [t 0.0 (- 1.0 t) 1.0])})))


(deftest
 t157_l1077
 (is
  ((fn
    [v]
    (let
     [stops (-> v pj/plan :legend :stops)]
     (and
      (= [0.0 0.0 1.0 1.0] (:color (first stops)))
      (= 0.0 (second (:color (last stops)))))))
   v156_l1073)))


(def
 v159_l1090
 (->
  {:step (range 40),
   :row (range 40),
   :n
   (map
    (fn* [p1__73991#] (Math/pow 10 (/ p1__73991# 10.0)))
    (range 40))}
  (pj/lay-point :step :row {:color :n})
  (pj/scale :color {:type :log, :range :viridis})))


(deftest
 t160_l1096
 (is
  ((fn
    [v]
    (let
     [legend (-> v pj/plan :legend)]
     (and
      (= :log (:scale-type legend))
      (= :viridis (:color-range legend)))))
   v159_l1090)))


(def
 v162_l1107
 (->
  {:region ["n" "s" "e" "w" "c"],
   :year [1 2 3 4 5],
   :change [-40 -10 5 30 60]}
  (pj/lay-point :year :change {:color :change})
  (pj/scale :color {:range :diverging, :midpoint 0})))


(deftest
 t163_l1113
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
        vec))]
     (not=
      (colors v)
      (colors
       (->
        {:region ["n" "s" "e" "w" "c"],
         :year [1 2 3 4 5],
         :change [-40 -10 5 30 60]}
        (pj/lay-point :year :change {:color :change})
        (pj/scale :color {:range :diverging}))))))
   v162_l1107)))


(def
 v165_l1133
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
 t166_l1140
 (is
  ((fn
    [v]
    (=
     [["rep" [1.0 0.0 0.0 1.0]]
      ["dem" [0.0 0.0 1.0 1.0]]
      ["ind" [0.0 (/ 128.0 255) 0.0 1.0]]]
     (mapv (juxt :label :color) (:entries (:legend (pj/plan v))))))
   v165_l1133)))


(def v168_l1162 pj/shape-symbols)


(deftest
 t169_l1164
 (is ((fn [syms] (= syms (distinct syms))) v168_l1162)))


(def
 v171_l1173
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:shape :continent})
  (pj/scale
   :shape
   {:domain ["Africa" "Americas" "Asia" "Europe" "Oceania"],
    :values [:circle :square :triangle :diamond :cross]})
  (pj/scale :x :log)))


(deftest
 t172_l1179
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
   v171_l1173)))


(def
 v174_l1193
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/scale :x {:type :log, :label "GDP per capita, log scale"})
  (pj/scale :color {:label "Continent"})))


(deftest
 t175_l1198
 (is
  ((fn
    [fr]
    (=
     ["GDP per capita, log scale" "Continent"]
     (-> fr pj/plan ((juxt :x-label (comp :title :legend))))))
   v174_l1193)))


(def
 v177_l1208
 (->
  gapminder-2007
  (pj/lay-point :gdp-percap :life-exp {:color :continent})
  (pj/options {:color-label "From the options"})
  (pj/scale :color {:label "From the spec"})))


(deftest
 t178_l1213
 (is
  ((fn [fr] (= "From the spec" (-> fr pj/plan :legend :title)))
   v177_l1208)))


(def
 v180_l1230
 (->
  squares
  (pj/lay-point :step :row {:size :n, :alpha :n})
  (pj/options {:width 620})))


(deftest
 t181_l1234
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= :radius (:quantity (:size-legend p)))
      (= :circle (:swatch (:size-legend p)))
      (= :square (:swatch (layer-type/quantities :opacity))))))
   v180_l1230)))


(def
 v183_l1259
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp)
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {:size {:column :pop, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t184_l1268
 (is
  ((fn [m] (re-find #"read :size through different scales" m))
   v183_l1259)))


(def
 v186_l1274
 (try
  (->
   gapminder-2007
   (pj/pose :gdp-percap :life-exp {:size :pop})
   (pj/lay-point {:size {:column :pop, :scale :log}})
   (pj/lay-point {})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t187_l1283
 (is
  ((fn
    [m]
    (and
     (re-find #"\{:type :log\}, \{:type :linear\}" m)
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
       :x-scale))))
   v186_l1274)))
