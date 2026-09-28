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


(def
 v21_l177
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


(def v23_l190 (layer-type/lookup :histogram))


(deftest t24_l192 (is ((fn [m] (= :bin (:stat m))) v23_l190)))


(def v26_l197 (layer-type/lookup :bar))


(deftest
 t27_l199
 (is ((fn [m] (and (= :rect (:mark m)) (nil? (:stat m)))) v26_l197)))


(def
 v29_l206
 {"lay-bar with x only"
  (mark-and-stat (-> (rdatasets/datasets-iris) (pj/lay-bar :species))),
  "lay-bar with x and y"
  (mark-and-stat
   (->
    {:city ["north" "south"], :rain [42 30]}
    (pj/lay-bar :city :rain)))})


(deftest
 t30_l211
 (is
  ((fn
    [m]
    (=
     {"lay-bar with x only" {:mark :rect, :stat :count},
      "lay-bar with x and y" {:mark :rect, :stat :identity}}
     m))
   v29_l206)))


(def
 v32_l224
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
 t33_l233
 (is
  ((fn
    [m]
    (=
     {"categorical x, numerical y" {:mark :boxplot, :stat :boxplot},
      "numerical x, numerical y" {:mark :point, :stat :identity},
      "categorical x only" {:mark :rect, :stat :count},
      "numerical x only" {:mark :bar, :stat :bin}}
     m))
   v32_l224)))


(def v35_l257 (layer-type/lookup :point))


(deftest t36_l259 (is ((fn [m] (= :identity (:stat m))) v35_l257)))


(def
 v38_l281
 (def
  grouped-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v39_l285 (-> grouped-scatter pj/plot pj/svg-summary :points))


(deftest t40_l287 (is ((fn [n] (= 150 n)) v39_l285)))


(def
 v42_l299
 (def
  resolved-layer
  (->
   grouped-scatter
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer)))


(def
 v44_l305
 (->
  resolved-layer
  (select-keys
   [:x :y :x-type :y-type :group :color :size :alpha :fixed-color])))


(deftest
 t45_l308
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
   v44_l305)))


(def
 v47_l366
 (def
  scatter-stat
  (-> resolved-layer (assoc :cfg {}) stat/compute-stat)))


(def v48_l371 (sort (keys scatter-stat)))


(deftest
 t49_l373
 (is ((fn [ks] (= [:points :x-domain :y-domain] ks)) v48_l371)))


(def v51_l382 (count (:points scatter-stat)))


(deftest t52_l384 (is ((fn [n] (= 3 n)) v51_l382)))


(def
 v54_l389
 (->
  scatter-stat
  :points
  first
  (update :xs (fn* [p1__78614#] (vec (take 3 p1__78614#))))
  (update :ys (fn* [p1__78615#] (vec (take 3 p1__78615#))))
  (update :row-indices (fn* [p1__78616#] (vec (take 3 p1__78616#))))))


(deftest
 t55_l396
 (is
  ((fn [g] (and (= "setosa" (:color g)) (= [5.1 4.9 4.7] (:xs g))))
   v54_l389)))


(def
 v57_l438
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
 t58_l442
 (is
  ((fn
    [[stat-color plan-group]]
    (and
     (= "setosa" stat-color)
     (= "setosa" (:label plan-group))
     (vector? (:color plan-group))))
   v57_l438)))


(def
 v60_l459
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length {:stat :density})))


(deftest
 t61_l462
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v60_l459)))


(def
 v63_l468
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:stat :summary})))


(deftest
 t64_l471
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v63_l468)))


(def
 v66_l493
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


(deftest t67_l504 (is ((fn [t] (= 22 (count (:row-maps t)))) v66_l493)))


(def
 v69_l509
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t70_l512
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v69_l509)))


(def
 v72_l516
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
 t73_l522
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v72_l516)))


(def
 v75_l533
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


(deftest t76_l544 (is ((fn [t] (= 22 (count (:row-maps t)))) v75_l533)))


(def
 v78_l559
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
 v79_l565
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
 t80_l570
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v79_l565)))


(def
 v82_l647
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
 t83_l654
 (is
  ((fn [m] (and (= :rule-h (:mark m)) (= 3.0 (:y-intercept m))))
   v82_l647)))


(def v85_l682 (mark/mark-clip-region :point))


(deftest
 t86_l684
 (is ((fn* [p1__78617#] (= :drawing-area p1__78617#)) v85_l682)))


(def v87_l686 (mark/mark-clip-region :rug))


(deftest
 t88_l688
 (is ((fn* [p1__78618#] (= :panel-box p1__78618#)) v87_l686)))


(def
 v90_l698
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v91_l700 (mark/mark-clip-region :margin-glyph))


(deftest
 t92_l702
 (is ((fn* [p1__78619#] (= :panel-box p1__78619#)) v91_l700)))


(def v94_l706 (remove-method mark/mark-clip-region :margin-glyph))


(def v95_l708 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t96_l710 (is (false? v95_l708)))


(def
 v98_l734
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v99_l739 (first (pj/plan->plot my-plan :svg {})))


(deftest t100_l741 (is ((fn [v] (= :svg v)) v99_l739)))


(def v102_l745 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v103_l747 (vector? my-figure))


(deftest t104_l749 (is ((fn [v] (true? v)) v103_l747)))


(def v106_l799 (def my-membrane (pj/plan->membrane my-plan)))


(def v107_l801 (pj/membrane? my-membrane))


(deftest t108_l803 (is ((fn [v] (true? v)) v107_l801)))


(def v109_l805 (membrane.ui/width my-membrane))


(deftest t110_l807 (is ((fn [v] (number? v)) v109_l805)))


(def v111_l809 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t112_l811 (is ((fn [v] (= :svg v)) v111_l809)))


(def
 v114_l817
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v115_l822 (pj/membrane? shortcut-membrane))


(deftest t116_l824 (is ((fn [v] (true? v)) v115_l822)))


(def
 v118_l863
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
 t119_l873
 (is ((fn [t] (= 3 (count (:row-maps t)))) v118_l863)))


(def
 v121_l884
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
 t122_l895
 (is ((fn [t] (= 4 (count (:row-maps t)))) v121_l884)))


(def
 v124_l907
 (->>
  (methods scicloj.plotje.impl.coord/make-inverse)
  keys
  (filter keyword?)
  (remove #{:default})
  sort
  vec))


(deftest
 t125_l914
 (is ((fn [ks] (= [:cartesian :fixed :flip] ks)) v124_l907)))


(def
 v127_l925
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t128_l929
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v127_l925)))


(def
 v130_l945
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v131_l948
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v133_l953 (pj/stat-doc :quantile))


(deftest
 t134_l955
 (is ((fn [v] (= "Quantile regression bands" v)) v133_l953)))


(def v136_l963 (remove-method stat/compute-stat [:quantile :doc]))


(def v137_l965 (pj/stat-doc :quantile))


(deftest t138_l967 (is ((fn [v] (= "(no description)" v)) v137_l965)))


(def v140_l973 (remove-method stat/compute-stat :quantile))


(def
 v141_l975
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t142_l977 (is ((fn [v] (= 11 v)) v141_l975)))
