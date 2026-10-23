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
      [p1__77464#]
      (and (vector? p1__77464#) (= :computes (second p1__77464#)))))
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
      (fn* [p1__77465#] (first (get p1__77465# "Stat")))
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
 v50_l363
 (def
  grouped-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v51_l367 (-> grouped-scatter pj/plot pj/svg-summary :points))


(deftest t52_l369 (is ((fn [n] (= 150 n)) v51_l367)))


(def
 v54_l381
 (def
  resolved-layer
  (->
   grouped-scatter
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer)))


(def
 v56_l387
 (->
  resolved-layer
  (select-keys
   [:x :y :x-type :y-type :group :color :size :alpha :fixed-color])))


(deftest
 t57_l390
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
   v56_l387)))


(def
 v59_l448
 (def
  scatter-stat
  (-> resolved-layer (assoc :cfg {}) stat/compute-stat)))


(def v60_l453 (sort (keys scatter-stat)))


(deftest
 t61_l455
 (is ((fn [ks] (= [:points :x-domain :y-domain] ks)) v60_l453)))


(def v63_l464 (count (:points scatter-stat)))


(deftest t64_l466 (is ((fn [n] (= 3 n)) v63_l464)))


(def
 v66_l471
 (->
  scatter-stat
  :points
  first
  (update :xs (fn* [p1__77466#] (vec (take 3 p1__77466#))))
  (update :ys (fn* [p1__77467#] (vec (take 3 p1__77467#))))
  (update :row-indices (fn* [p1__77468#] (vec (take 3 p1__77468#))))))


(deftest
 t67_l478
 (is
  ((fn [g] (and (= "setosa" (:color g)) (= [5.1 4.9 4.7] (:xs g))))
   v66_l471)))


(def
 v69_l520
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
 t70_l524
 (is
  ((fn
    [[stat-color plan-group]]
    (and
     (= "setosa" stat-color)
     (= "setosa" (:label plan-group))
     (vector? (:color plan-group))))
   v69_l520)))


(def
 v72_l541
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length {:stat :density})))


(deftest
 t73_l544
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v72_l541)))


(def
 v75_l550
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:stat :summary})))


(deftest
 t76_l553
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v75_l550)))


(def
 v78_l575
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


(deftest t79_l586 (is ((fn [t] (= 22 (count (:row-maps t)))) v78_l575)))


(def
 v81_l591
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t82_l594
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v81_l591)))


(def
 v84_l598
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
 t85_l604
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v84_l598)))


(def
 v87_l615
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


(deftest t88_l626 (is ((fn [t] (= 22 (count (:row-maps t)))) v87_l615)))


(def
 v90_l641
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
 v91_l647
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
 t92_l652
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v91_l647)))


(def
 v94_l732
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
 t95_l739
 (is
  ((fn [m] (and (= :rule-h (:mark m)) (= 3.0 (:y-intercept m))))
   v94_l732)))


(def v97_l767 (mark/mark-clip-region :point))


(deftest
 t98_l769
 (is ((fn* [p1__77469#] (= :drawing-area p1__77469#)) v97_l767)))


(def v99_l771 (mark/mark-clip-region :rug))


(deftest
 t100_l773
 (is ((fn* [p1__77470#] (= :panel-box p1__77470#)) v99_l771)))


(def
 v102_l783
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v103_l785 (mark/mark-clip-region :margin-glyph))


(deftest
 t104_l787
 (is ((fn* [p1__77471#] (= :panel-box p1__77471#)) v103_l785)))


(def v106_l791 (remove-method mark/mark-clip-region :margin-glyph))


(def
 v107_l793
 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t108_l795 (is (false? v107_l793)))


(def
 v110_l819
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v111_l824 (first (pj/plan->plot my-plan :svg {})))


(deftest t112_l826 (is ((fn [v] (= :svg v)) v111_l824)))


(def v114_l830 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v115_l832 (vector? my-figure))


(deftest t116_l834 (is ((fn [v] (true? v)) v115_l832)))


(def v118_l884 (def my-membrane (pj/plan->membrane my-plan)))


(def v119_l886 (pj/membrane? my-membrane))


(deftest t120_l888 (is ((fn [v] (true? v)) v119_l886)))


(def v121_l890 (membrane.ui/width my-membrane))


(deftest t122_l892 (is ((fn [v] (number? v)) v121_l890)))


(def v123_l894 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t124_l896 (is ((fn [v] (= :svg v)) v123_l894)))


(def
 v126_l902
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v127_l907 (pj/membrane? shortcut-membrane))


(deftest t128_l909 (is ((fn [v] (true? v)) v127_l907)))


(def
 v130_l948
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
 t131_l958
 (is ((fn [t] (= 3 (count (:row-maps t)))) v130_l948)))


(def
 v133_l969
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
 t134_l980
 (is ((fn [t] (= 4 (count (:row-maps t)))) v133_l969)))


(def
 v136_l992
 (->>
  (methods scicloj.plotje.impl.coord/make-inverse)
  keys
  (filter keyword?)
  (remove #{:default})
  sort
  vec))


(deftest
 t137_l999
 (is ((fn [ks] (= [:cartesian :fixed :flip] ks)) v136_l992)))


(def
 v139_l1010
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t140_l1014
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v139_l1010)))


(def
 v142_l1030
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v143_l1033
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v145_l1038 (pj/stat-doc :quantile))


(deftest
 t146_l1040
 (is ((fn [v] (= "Quantile regression bands" v)) v145_l1038)))


(def v148_l1048 (remove-method stat/compute-stat [:quantile :doc]))


(def v149_l1050 (pj/stat-doc :quantile))


(deftest t150_l1052 (is ((fn [v] (= "(no description)" v)) v149_l1050)))


(def v152_l1058 (remove-method stat/compute-stat :quantile))


(def
 v153_l1060
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t154_l1062 (is ((fn [v] (= 11 v)) v153_l1060)))
