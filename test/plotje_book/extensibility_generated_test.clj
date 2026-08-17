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
 v8_l108
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
 v9_l126
 (defmethod
  stat/compute-stat
  [:running-max :doc]
  [_]
  "Running maximum -- the largest y seen so far"))


(def
 v11_l136
 (def
  rainfall
  {:month [1 2 3 4 5 6 7 8 9 10 11 12],
   :rain [42 30 55 20 61 48 35 70 25 58 44 66]}))


(def
 v12_l140
 (->
  rainfall
  (pj/lay-point :month :rain {:color "#bbbbbb"})
  (pj/lay-line :month :rain {:stat :running-max})
  (pj/options {:title "Rainfall and its running maximum"})
  pj/plot))


(deftest
 t13_l146
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 1 (:lines s)))))
   v12_l140)))


(def
 v15_l153
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
 t16_l161
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v15_l153)))


(def
 v18_l165
 (do
  (remove-method stat/compute-stat :running-max)
  (remove-method stat/compute-stat [:running-max :doc])
  (contains? (methods stat/compute-stat) :running-max)))


(deftest t19_l169 (is (false? v18_l165)))


(def
 v21_l179
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


(def v23_l192 (layer-type/lookup :histogram))


(deftest t24_l194 (is ((fn [m] (= :bin (:stat m))) v23_l192)))


(def v26_l199 (layer-type/lookup :bar))


(deftest
 t27_l201
 (is ((fn [m] (and (= :rect (:mark m)) (nil? (:stat m)))) v26_l199)))


(def
 v29_l208
 {"lay-bar with x only"
  (mark-and-stat (-> (rdatasets/datasets-iris) (pj/lay-bar :species))),
  "lay-bar with x and y"
  (mark-and-stat
   (->
    {:city ["north" "south"], :rain [42 30]}
    (pj/lay-bar :city :rain)))})


(deftest
 t30_l213
 (is
  ((fn
    [m]
    (=
     {"lay-bar with x only" {:mark :rect, :stat :count},
      "lay-bar with x and y" {:mark :rect, :stat :identity}}
     m))
   v29_l208)))


(def
 v32_l226
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
 t33_l235
 (is
  ((fn
    [m]
    (=
     {"categorical x, numerical y" {:mark :boxplot, :stat :boxplot},
      "numerical x, numerical y" {:mark :point, :stat :identity},
      "categorical x only" {:mark :rect, :stat :count},
      "numerical x only" {:mark :bar, :stat :bin}}
     m))
   v32_l226)))


(def v35_l259 (layer-type/lookup :point))


(deftest t36_l261 (is ((fn [m] (= :identity (:stat m))) v35_l259)))


(def
 v38_l284
 (def
  grouped-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v39_l288 (-> grouped-scatter pj/plot pj/svg-summary :points))


(deftest t40_l290 (is ((fn [n] (= 150 n)) v39_l288)))


(def
 v42_l302
 (def
  resolved-layer
  (->
   grouped-scatter
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer)))


(def
 v44_l308
 (->
  resolved-layer
  (select-keys
   [:x :y :x-type :y-type :group :color :size :alpha :fixed-color])))


(deftest
 t45_l311
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
   v44_l308)))


(def
 v47_l369
 (def
  scatter-stat
  (-> resolved-layer (assoc :cfg {}) stat/compute-stat)))


(def v48_l374 (sort (keys scatter-stat)))


(deftest
 t49_l376
 (is ((fn [ks] (= [:points :x-domain :y-domain] ks)) v48_l374)))


(def v51_l385 (count (:points scatter-stat)))


(deftest t52_l387 (is ((fn [n] (= 3 n)) v51_l385)))


(def
 v54_l392
 (->
  scatter-stat
  :points
  first
  (update :xs (fn* [p1__11193#] (vec (take 3 p1__11193#))))
  (update :ys (fn* [p1__11194#] (vec (take 3 p1__11194#))))
  (update :row-indices (fn* [p1__11195#] (vec (take 3 p1__11195#))))))


(deftest
 t55_l399
 (is
  ((fn [g] (and (= "setosa" (:color g)) (= [5.1 4.9 4.7] (:xs g))))
   v54_l392)))


(def
 v57_l441
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
 t58_l445
 (is
  ((fn
    [[stat-color plan-group]]
    (and
     (= "setosa" stat-color)
     (= "setosa" (:label plan-group))
     (vector? (:color plan-group))))
   v57_l441)))


(def
 v60_l462
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length {:stat :density})))


(deftest
 t61_l465
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v60_l462)))


(def
 v63_l471
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:stat :summary})))


(deftest
 t64_l474
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v63_l471)))


(def
 v66_l496
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


(deftest t67_l507 (is ((fn [t] (= 17 (count (:row-maps t)))) v66_l496)))


(def
 v69_l512
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t70_l515
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v69_l512)))


(def
 v72_l519
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
 t73_l525
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v72_l519)))


(def
 v75_l536
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


(deftest t76_l547 (is ((fn [t] (= 17 (count (:row-maps t)))) v75_l536)))


(def
 v78_l562
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
 v79_l568
 (let
  [groups
   (:groups bubble-layer)
   radius-of
   (layer-type/channel-magnitude-fn
    bubble-layer
    :size
    (keep :sizes groups))]
  (mapv radius-of [1 4 9])))


(deftest
 t80_l573
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v79_l568)))


(def v82_l672 (mark/mark-clip-region :point))


(deftest
 t83_l674
 (is ((fn* [p1__11196#] (= :drawing-area p1__11196#)) v82_l672)))


(def v84_l676 (mark/mark-clip-region :rug))


(deftest
 t85_l678
 (is ((fn* [p1__11197#] (= :panel-box p1__11197#)) v84_l676)))


(def
 v87_l688
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v88_l690 (mark/mark-clip-region :margin-glyph))


(deftest
 t89_l692
 (is ((fn* [p1__11198#] (= :panel-box p1__11198#)) v88_l690)))


(def v91_l696 (remove-method mark/mark-clip-region :margin-glyph))


(def v92_l698 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t93_l700 (is (false? v92_l698)))


(def
 v95_l724
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v96_l729 (first (pj/plan->plot my-plan :svg {})))


(deftest t97_l731 (is ((fn [v] (= :svg v)) v96_l729)))


(def v99_l735 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v100_l737 (vector? my-figure))


(deftest t101_l739 (is ((fn [v] (true? v)) v100_l737)))


(def v103_l789 (def my-membrane (pj/plan->membrane my-plan)))


(def v104_l791 (pj/membrane? my-membrane))


(deftest t105_l793 (is ((fn [v] (true? v)) v104_l791)))


(def v106_l795 (membrane.ui/width my-membrane))


(deftest t107_l797 (is ((fn [v] (number? v)) v106_l795)))


(def v108_l799 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t109_l801 (is ((fn [v] (= :svg v)) v108_l799)))


(def
 v111_l807
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v112_l812 (pj/membrane? shortcut-membrane))


(deftest t113_l814 (is ((fn [v] (true? v)) v112_l812)))


(def
 v115_l853
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
 t116_l863
 (is ((fn [t] (= 3 (count (:row-maps t)))) v115_l853)))


(def
 v118_l874
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
 t119_l885
 (is ((fn [t] (= 4 (count (:row-maps t)))) v118_l874)))


(def
 v121_l897
 (->>
  (methods scicloj.plotje.impl.coord/make-inverse)
  keys
  (filter keyword?)
  (remove #{:default})
  sort
  vec))


(deftest
 t122_l904
 (is ((fn [ks] (= [:cartesian :fixed :flip] ks)) v121_l897)))


(def
 v124_l915
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t125_l919
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v124_l915)))


(def
 v127_l935
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v128_l938
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v130_l943 (pj/stat-doc :quantile))


(deftest
 t131_l945
 (is ((fn [v] (= "Quantile regression bands" v)) v130_l943)))


(def v133_l953 (remove-method stat/compute-stat [:quantile :doc]))


(def v134_l955 (pj/stat-doc :quantile))


(deftest t135_l957 (is ((fn [v] (= "(no description)" v)) v134_l955)))


(def v137_l963 (remove-method stat/compute-stat :quantile))


(def
 v138_l965
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t139_l967 (is ((fn [v] (= 11 v)) v138_l965)))
