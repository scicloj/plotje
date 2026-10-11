(ns
 plotje-book.extensibility-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [tech.v3.datatype :as dtype]
  [tech.v3.datatype.functional :as dfn]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.plotje.impl.resolve :as resolve]
  [scicloj.plotje.impl.stat :as stat]
  [scicloj.plotje.impl.extract :as extract]
  [scicloj.plotje.render.mark :as mark]
  [scicloj.plotje.render.svg :as svg]
  [scicloj.plotje.impl.render :as render]
  [membrane.ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l45
 (kind/mermaid
  "\ngraph LR\n  B[\"Pose\"] -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  P -.->|pj/plan->plot| F\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l79
 (kind/table
  {:column-names ["Dispatch value" "What it does"],
   :row-maps
   (->>
    (methods stat/compute-stat)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "What it does" (pj/stat-doc k)})))}))


(deftest t6_l90 (is ((fn [t] (= 11 (count (:row-maps t)))) v5_l79)))


(def
 v8_l107
 (defmethod
  stat/compute-stat
  :running-max
  [{:keys [data x y group]}]
  (let
   [subsets
    (if
     (seq group)
     (vals (tc/group-by data group {:result-type :as-map}))
     [data])
    points
    (mapv
     (fn
      [ds]
      (cond->
       {:xs (ds x), :ys (dfn/cummax (ds y))}
       (seq group)
       (assoc :color (first (ds (first group))))))
     subsets)
    all-xs
    (dtype/concat-buffers (map :xs points))
    all-ys
    (dtype/concat-buffers (map :ys points))]
   {:points points,
    :x-domain [(dfn/reduce-min all-xs) (dfn/reduce-max all-xs)],
    :y-domain [(dfn/reduce-min all-ys) (dfn/reduce-max all-ys)]})))


(def
 v9_l125
 (defmethod
  stat/compute-stat
  [:running-max :doc]
  [_]
  "Running maximum -- the largest y seen so far"))


(def
 v11_l135
 (def
  rainfall
  {:month [1 2 3 4 5 6 7 8 9 10 11 12],
   :rain [42 30 55 20 61 48 35 70 25 58 44 66]}))


(def
 v12_l139
 (->
  rainfall
  (pj/lay-point :month :rain {:color "#bbbbbb"})
  (pj/lay-line :month :rain {:stat :running-max})
  (pj/options {:title "Rainfall and its running maximum"})
  pj/plot))


(deftest
 t13_l145
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 1 (:lines s)))))
   v12_l139)))


(def
 v15_l152
 (->
  {:month (concat (range 1 13) (range 1 13)),
   :rain
   [42
    30
    55
    20
    61
    48
    35
    70
    25
    58
    44
    66
    10
    33
    21
    40
    18
    52
    29
    47
    60
    22
    38
    55],
   :city (concat (repeat 12 "north") (repeat 12 "south"))}
  (pj/lay-line :month :rain {:stat :running-max, :color :city})
  (pj/options {:title "Running maximum per city"})
  pj/plot))


(deftest
 t16_l160
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v15_l152)))


(def
 v18_l164
 (do
  (remove-method stat/compute-stat :running-max)
  (remove-method stat/compute-stat [:running-max :doc])
  (contains? (methods stat/compute-stat) :running-max)))


(deftest t19_l168 (is (false? v18_l164)))


(def v21_l184 (stat/computes {:stat :bin}))


(deftest t22_l186 (is ((fn [m] (= {:y :count} m)) v21_l184)))


(def v24_l192 (stat/computes {:stat :bin, :normalize :density}))


(deftest t25_l194 (is ((fn [m] (= {:y :density} m)) v24_l192)))


(def v27_l199 (stat/computes {:stat :loess}))


(deftest t28_l201 (is (nil? v27_l199)))


(def
 v30_l206
 (kind/table
  {:column-names ["Stat" "Computes"],
   :row-maps
   (->>
    (methods stat/compute-stat)
    keys
    (filter
     (fn*
      [p1__11193#]
      (and (vector? p1__11193#) (= :computes (second p1__11193#)))))
    (map first)
    sort
    (mapv
     (fn
      [k]
      {"Stat" (kind/code (pr-str k)),
       "Computes" (kind/code (pr-str (stat/computes {:stat k})))})))}))


(deftest
 t31_l217
 (is
  ((fn
    [t]
    (=
     [":bin" ":count" ":density"]
     (mapv
      (fn* [p1__11194#] (first (get p1__11194# "Stat")))
      (:row-maps t))))
   v30_l206)))


(def
 v33_l250
 (defn
  mark-and-stat
  "The mark and stat a pose's first layer resolves to."
  [pose]
  (->
   pose
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer
   (select-keys [:mark :stat]))))


(def v35_l263 (layer-type/lookup :histogram))


(deftest t36_l265 (is ((fn [m] (= :bin (:stat m))) v35_l263)))


(def v38_l270 (layer-type/lookup :bar))


(deftest
 t39_l272
 (is ((fn [m] (and (= :rect (:mark m)) (nil? (:stat m)))) v38_l270)))


(def
 v41_l279
 {"lay-bar with x only"
  (mark-and-stat (-> (rdatasets/datasets-iris) (pj/lay-bar :species))),
  "lay-bar with x and y"
  (mark-and-stat
   (->
    {:city ["north" "south"], :rain [42 30]}
    (pj/lay-bar :city :rain)))})


(deftest
 t42_l284
 (is
  ((fn
    [m]
    (=
     {"lay-bar with x only" {:mark :rect, :stat :count},
      "lay-bar with x and y" {:mark :rect, :stat :identity}}
     m))
   v41_l279)))


(def
 v44_l297
 {"categorical x, numerical y"
  (mark-and-stat
   (-> (rdatasets/datasets-iris) (pj/pose :species :sepal-width))),
  "numerical x, numerical y"
  (mark-and-stat
   (->
    (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width))),
  "categorical x only"
  (mark-and-stat (-> (rdatasets/datasets-iris) (pj/pose :species))),
  "numerical x only"
  (mark-and-stat
   (-> (rdatasets/datasets-iris) (pj/pose :sepal-length)))})


(deftest
 t45_l306
 (is
  ((fn
    [m]
    (=
     {"categorical x, numerical y" {:mark :boxplot, :stat :boxplot},
      "numerical x, numerical y" {:mark :point, :stat :identity},
      "categorical x only" {:mark :rect, :stat :count},
      "numerical x only" {:mark :bar, :stat :bin}}
     m))
   v44_l297)))


(def v47_l330 (layer-type/lookup :point))


(deftest t48_l332 (is ((fn [m] (= :identity (:stat m))) v47_l330)))


(def
 v50_l358
 (def
  grouped-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v51_l362 (-> grouped-scatter pj/plot pj/svg-summary :points))


(deftest t52_l364 (is ((fn [n] (= 150 n)) v51_l362)))


(def
 v54_l376
 (def
  resolved-layer
  (->
   grouped-scatter
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer)))


(def
 v56_l382
 (->
  resolved-layer
  (select-keys
   [:x :y :x-type :y-type :group :color :size :alpha :fixed-color])))


(deftest
 t57_l385
 (is
  ((fn
    [m]
    (=
     {:y :sepal-width,
      :group [:species],
      :color :species,
      :fixed-color nil,
      :size nil,
      :alpha nil,
      :x :sepal-length,
      :x-type :numerical,
      :y-type :numerical}
     m))
   v56_l382)))


(def
 v59_l443
 (def
  scatter-stat
  (-> resolved-layer (assoc :cfg {}) stat/compute-stat)))


(def v60_l448 (sort (keys scatter-stat)))


(deftest
 t61_l450
 (is ((fn [ks] (= [:points :x-domain :y-domain] ks)) v60_l448)))


(def v63_l459 (count (:points scatter-stat)))


(deftest t64_l461 (is ((fn [n] (= 3 n)) v63_l459)))


(def
 v66_l466
 (->
  scatter-stat
  :points
  first
  (update :xs (fn* [p1__11195#] (vec (take 3 p1__11195#))))
  (update :ys (fn* [p1__11196#] (vec (take 3 p1__11196#))))
  (update :row-indices (fn* [p1__11197#] (vec (take 3 p1__11197#))))))


(deftest
 t67_l473
 (is
  ((fn [g] (and (= "setosa" (:color g)) (= [5.1 4.9 4.7] (:xs g))))
   v66_l466)))


(def
 v69_l515
 [(-> scatter-stat :points first :color)
  (->
   grouped-scatter
   pj/plan
   :panels
   first
   :layers
   first
   :groups
   first
   (select-keys [:color :label]))])


(deftest
 t70_l519
 (is
  ((fn
    [[stat-color plan-group]]
    (and
     (= "setosa" stat-color)
     (= "setosa" (:label plan-group))
     (vector? (:color plan-group))))
   v69_l515)))


(def
 v72_l536
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length {:stat :density})))


(deftest
 t73_l539
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v72_l536)))


(def
 v75_l545
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:stat :summary})))


(deftest
 t76_l548
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v75_l545)))


(def
 v78_l570
 (kind/table
  {:column-names ["Dispatch value" "Output"],
   :row-maps
   (->>
    (methods extract/extract-layer)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Output" (pj/mark-doc k)})))}))


(deftest t79_l581 (is ((fn [t] (= 22 (count (:row-maps t)))) v78_l570)))


(def
 v81_l586
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t82_l589
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v81_l586)))


(def
 v84_l593
 (let
  [s
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    pj/plan)
   layer
   (first (:layers (first (:panels s))))]
  layer))


(deftest
 t85_l599
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v84_l593)))


(def
 v87_l610
 (kind/table
  {:column-names ["Dispatch value" "Membrane output"],
   :row-maps
   (->>
    (methods mark/layer->membrane)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Membrane output" (pj/membrane-mark-doc k)})))}))


(deftest t88_l621 (is ((fn [t] (= 22 (count (:row-maps t)))) v87_l610)))


(def
 v90_l636
 (def
  bubble-layer
  (->
   {:x [1 2 3], :y [1 2 3], :n [1 4 9]}
   (pj/lay-point :x :y {:size :n})
   pj/plan
   :panels
   first
   :layers
   first)))


(def
 v91_l642
 (let
  [groups
   (:groups bubble-layer)
   radius-of
   (layer-type/aesthetic-magnitude-fn
    bubble-layer
    :size
    (keep :sizes groups))]
  (mapv radius-of [1 4 9])))


(deftest
 t92_l647
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v91_l642)))


(def
 v94_l724
 (let
  [layer
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rule-h {:y-intercept 3.0})
    pj/plan
    :panels
    first
    :layers
    last)]
  layer))


(deftest
 t95_l731
 (is
  ((fn [m] (and (= :rule-h (:mark m)) (= 3.0 (:y-intercept m))))
   v94_l724)))


(def v97_l759 (mark/mark-clip-region :point))


(deftest
 t98_l761
 (is ((fn* [p1__11198#] (= :drawing-area p1__11198#)) v97_l759)))


(def v99_l763 (mark/mark-clip-region :rug))


(deftest
 t100_l765
 (is ((fn* [p1__11199#] (= :panel-box p1__11199#)) v99_l763)))


(def
 v102_l775
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v103_l777 (mark/mark-clip-region :margin-glyph))


(deftest
 t104_l779
 (is ((fn* [p1__11200#] (= :panel-box p1__11200#)) v103_l777)))


(def v106_l783 (remove-method mark/mark-clip-region :margin-glyph))


(def
 v107_l785
 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t108_l787 (is (false? v107_l785)))


(def
 v110_l811
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v111_l816 (first (pj/plan->plot my-plan :svg {})))


(deftest t112_l818 (is ((fn [v] (= :svg v)) v111_l816)))


(def v114_l822 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v115_l824 (vector? my-figure))


(deftest t116_l826 (is ((fn [v] (true? v)) v115_l824)))


(def v118_l876 (def my-membrane (pj/plan->membrane my-plan)))


(def v119_l878 (pj/membrane? my-membrane))


(deftest t120_l880 (is ((fn [v] (true? v)) v119_l878)))


(def v121_l882 (membrane.ui/width my-membrane))


(deftest t122_l884 (is ((fn [v] (number? v)) v121_l882)))


(def v123_l886 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t124_l888 (is ((fn [v] (= :svg v)) v123_l886)))


(def
 v126_l894
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v127_l899 (pj/membrane? shortcut-membrane))


(deftest t128_l901 (is ((fn [v] (true? v)) v127_l899)))


(def
 v130_l940
 (kind/table
  {:column-names ["Dispatch value" "Scale type"],
   :row-maps
   (->>
    (methods scicloj.plotje.impl.scale/make-scale)
    keys
    (filter keyword?)
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Scale type" (pj/scale-doc k)})))}))


(deftest
 t131_l950
 (is ((fn [t] (= 3 (count (:row-maps t)))) v130_l940)))


(def
 v133_l961
 (kind/table
  {:column-names ["Dispatch value" "Behavior"],
   :row-maps
   (->>
    (methods scicloj.plotje.impl.coord/make-coord)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Behavior" (pj/coord-doc k)})))}))


(deftest
 t134_l972
 (is ((fn [t] (= 4 (count (:row-maps t)))) v133_l961)))


(def
 v136_l984
 (->>
  (methods scicloj.plotje.impl.coord/make-inverse)
  keys
  (filter keyword?)
  (remove #{:default})
  sort
  vec))


(deftest
 t137_l991
 (is ((fn [ks] (= [:cartesian :fixed :flip] ks)) v136_l984)))


(def
 v139_l1002
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t140_l1006
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v139_l1002)))


(def
 v142_l1022
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v143_l1025
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v145_l1030 (pj/stat-doc :quantile))


(deftest
 t146_l1032
 (is ((fn [v] (= "Quantile regression bands" v)) v145_l1030)))


(def v148_l1040 (remove-method stat/compute-stat [:quantile :doc]))


(def v149_l1042 (pj/stat-doc :quantile))


(deftest t150_l1044 (is ((fn [v] (= "(no description)" v)) v149_l1042)))


(def v152_l1050 (remove-method stat/compute-stat :quantile))


(def
 v153_l1052
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t154_l1054 (is ((fn [v] (= 11 v)) v153_l1052)))
