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
 v38_l278
 (def
  grouped-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v39_l282 (-> grouped-scatter pj/plot pj/svg-summary :points))


(deftest t40_l284 (is ((fn [n] (= 150 n)) v39_l282)))


(def
 v42_l296
 (def
  resolved-layer
  (->
   grouped-scatter
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer)))


(def
 v44_l302
 (->
  resolved-layer
  (select-keys
   [:x :y :x-type :y-type :group :color :size :alpha :fixed-color])))


(deftest
 t45_l305
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
   v44_l302)))


(def
 v47_l363
 (def
  scatter-stat
  (-> resolved-layer (assoc :cfg {}) stat/compute-stat)))


(def v48_l368 (sort (keys scatter-stat)))


(deftest
 t49_l370
 (is ((fn [ks] (= [:points :x-domain :y-domain] ks)) v48_l368)))


(def v51_l379 (count (:points scatter-stat)))


(deftest t52_l381 (is ((fn [n] (= 3 n)) v51_l379)))


(def
 v54_l386
 (->
  scatter-stat
  :points
  first
  (update :xs (fn* [p1__78873#] (vec (take 3 p1__78873#))))
  (update :ys (fn* [p1__78874#] (vec (take 3 p1__78874#))))
  (update :row-indices (fn* [p1__78875#] (vec (take 3 p1__78875#))))))


(deftest
 t55_l393
 (is
  ((fn [g] (and (= "setosa" (:color g)) (= [5.1 4.9 4.7] (:xs g))))
   v54_l386)))


(def
 v57_l435
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
 t58_l439
 (is
  ((fn
    [[stat-color plan-group]]
    (and
     (= "setosa" stat-color)
     (= "setosa" (:label plan-group))
     (vector? (:color plan-group))))
   v57_l435)))


(def
 v60_l456
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length {:stat :density})))


(deftest
 t61_l459
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v60_l456)))


(def
 v63_l465
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:stat :summary})))


(deftest
 t64_l468
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v63_l465)))


(def
 v66_l490
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


(deftest t67_l501 (is ((fn [t] (= 17 (count (:row-maps t)))) v66_l490)))


(def
 v69_l506
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t70_l509
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v69_l506)))


(def
 v72_l513
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
 t73_l519
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v72_l513)))


(def
 v75_l530
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


(deftest t76_l541 (is ((fn [t] (= 17 (count (:row-maps t)))) v75_l530)))


(def v78_l630 (mark/mark-clip-region :point))


(deftest
 t79_l632
 (is ((fn* [p1__78876#] (= :drawing-area p1__78876#)) v78_l630)))


(def v80_l634 (mark/mark-clip-region :rug))


(deftest
 t81_l636
 (is ((fn* [p1__78877#] (= :panel-box p1__78877#)) v80_l634)))


(def
 v83_l646
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v84_l648 (mark/mark-clip-region :margin-glyph))


(deftest
 t85_l650
 (is ((fn* [p1__78878#] (= :panel-box p1__78878#)) v84_l648)))


(def v87_l654 (remove-method mark/mark-clip-region :margin-glyph))


(def v88_l656 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t89_l658 (is (false? v88_l656)))


(def
 v91_l682
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v92_l687 (first (pj/plan->plot my-plan :svg {})))


(deftest t93_l689 (is ((fn [v] (= :svg v)) v92_l687)))


(def v95_l693 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v96_l695 (vector? my-figure))


(deftest t97_l697 (is ((fn [v] (true? v)) v96_l695)))


(def v99_l747 (def my-membrane (pj/plan->membrane my-plan)))


(def v100_l749 (pj/membrane? my-membrane))


(deftest t101_l751 (is ((fn [v] (true? v)) v100_l749)))


(def v102_l753 (membrane.ui/width my-membrane))


(deftest t103_l755 (is ((fn [v] (number? v)) v102_l753)))


(def v104_l757 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t105_l759 (is ((fn [v] (= :svg v)) v104_l757)))


(def
 v107_l765
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v108_l770 (pj/membrane? shortcut-membrane))


(deftest t109_l772 (is ((fn [v] (true? v)) v108_l770)))


(def
 v111_l811
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
 t112_l821
 (is ((fn [t] (= 3 (count (:row-maps t)))) v111_l811)))


(def
 v114_l832
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
 t115_l843
 (is ((fn [t] (= 4 (count (:row-maps t)))) v114_l832)))


(def
 v117_l855
 (->>
  (methods scicloj.plotje.impl.coord/make-inverse)
  keys
  (filter keyword?)
  (remove #{:default})
  sort
  vec))


(deftest
 t118_l862
 (is ((fn [ks] (= [:cartesian :fixed :flip] ks)) v117_l855)))


(def
 v120_l873
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t121_l877
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v120_l873)))


(def
 v123_l893
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v124_l896
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v126_l901 (pj/stat-doc :quantile))


(deftest
 t127_l903
 (is ((fn [v] (= "Quantile regression bands" v)) v126_l901)))


(def v129_l911 (remove-method stat/compute-stat [:quantile :doc]))


(def v130_l913 (pj/stat-doc :quantile))


(deftest t131_l915 (is ((fn [v] (= "(no description)" v)) v130_l913)))


(def v133_l921 (remove-method stat/compute-stat :quantile))


(def
 v134_l923
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t135_l925 (is ((fn [v] (= 11 v)) v134_l923)))
