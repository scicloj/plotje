(ns
 plotje-book.extensibility-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [tech.v3.datatype :as dtype]
  [tech.v3.datatype.functional :as dfn]
  [clojure.string :as str]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.plotje.impl.resolve :as resolve]
  [scicloj.plotje.impl.stat :as stat]
  [scicloj.plotje.impl.extract :as extract]
  [scicloj.plotje.impl.position :as position]
  [scicloj.plotje.render.mark :as mark]
  [scicloj.plotje.render.svg :as svg]
  [scicloj.plotje.impl.render :as render]
  [membrane.ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l48
 (kind/mermaid
  "\ngraph LR\n  B[\"Pose\"] -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  P -.->|pj/plan->plot| F\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l82
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


(deftest t6_l93 (is ((fn [t] (= 11 (count (:row-maps t)))) v5_l82)))


(def
 v8_l110
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
 v9_l128
 (defmethod
  stat/compute-stat
  [:running-max :doc]
  [_]
  "Running maximum -- the largest y seen so far"))


(def
 v11_l138
 (def
  rainfall
  {:month [1 2 3 4 5 6 7 8 9 10 11 12],
   :rain [42 30 55 20 61 48 35 70 25 58 44 66]}))


(def
 v12_l142
 (->
  rainfall
  (pj/lay-point :month :rain {:color "#bbbbbb"})
  (pj/lay-line :month :rain {:stat :running-max})
  (pj/options {:title "Rainfall and its running maximum"})
  pj/plot))


(deftest
 t13_l148
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 1 (:lines s)))))
   v12_l142)))


(def
 v15_l155
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
 t16_l163
 (is ((fn [v] (= 2 (:lines (pj/svg-summary v)))) v15_l155)))


(def
 v18_l169
 (->
  rainfall
  (assoc
   :half
   (concat (repeat 6 "January to June") (repeat 6 "July to December")))
  (pj/lay-line :month :rain {:stat :running-max})
  (pj/facet :half)
  pj/plot))


(deftest
 t19_l176
 (is
  ((fn
    [v]
    (let
     [ys
      (fn
       [line]
       (mapv
        (fn*
         [p1__11193#]
         (parse-double (second (str/split p1__11193# #","))))
        (str/split (:points (second line)) #" ")))
      lines
      (filterv
       (fn*
        [p1__11194#]
        (and
         (vector? p1__11194#)
         (= :polyline (first p1__11194#))
         (< 1 (:stroke-width (second p1__11194#)))))
       (tree-seq vector? seq v))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= 2 (count lines))
      (> (first (ys (second lines))) (peek (ys (first lines)))))))
   v18_l169)))


(def
 v21_l197
 (do
  (remove-method stat/compute-stat :running-max)
  (remove-method stat/compute-stat [:running-max :doc])
  (contains? (methods stat/compute-stat) :running-max)))


(deftest t22_l201 (is (false? v21_l197)))


(def v24_l217 (stat/computes {:stat :bin}))


(deftest t25_l219 (is ((fn [m] (= {:y :count} m)) v24_l217)))


(def v27_l225 (stat/computes {:stat :bin, :normalize :density}))


(deftest t28_l227 (is ((fn [m] (= {:y :density} m)) v27_l225)))


(def v30_l232 (stat/computes {:stat :loess}))


(deftest t31_l234 (is (nil? v30_l232)))


(def
 v33_l239
 (kind/table
  {:column-names ["Stat" "Computes"],
   :row-maps
   (->>
    (methods stat/compute-stat)
    keys
    (filter
     (fn*
      [p1__11195#]
      (and (vector? p1__11195#) (= :computes (second p1__11195#)))))
    (map first)
    sort
    (mapv
     (fn
      [k]
      {"Stat" (kind/code (pr-str k)),
       "Computes" (kind/code (pr-str (stat/computes {:stat k})))})))}))


(deftest
 t34_l250
 (is
  ((fn
    [t]
    (=
     [":bin" ":count" ":density"]
     (mapv
      (fn* [p1__11196#] (first (get p1__11196# "Stat")))
      (:row-maps t))))
   v33_l239)))


(def
 v36_l283
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


(def v38_l296 (layer-type/lookup :histogram))


(deftest t39_l298 (is ((fn [m] (= :bin (:stat m))) v38_l296)))


(def v41_l303 (layer-type/lookup :bar))


(deftest
 t42_l305
 (is ((fn [m] (and (= :rect (:mark m)) (nil? (:stat m)))) v41_l303)))


(def
 v44_l312
 {"lay-bar with x only"
  (mark-and-stat (-> (rdatasets/datasets-iris) (pj/lay-bar :species))),
  "lay-bar with x and y"
  (mark-and-stat
   (->
    {:city ["north" "south"], :rain [42 30]}
    (pj/lay-bar :city :rain)))})


(deftest
 t45_l317
 (is
  ((fn
    [m]
    (=
     {"lay-bar with x only" {:mark :rect, :stat :count},
      "lay-bar with x and y" {:mark :rect, :stat :identity}}
     m))
   v44_l312)))


(def
 v47_l330
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
 t48_l339
 (is
  ((fn
    [m]
    (=
     {"categorical x, numerical y" {:mark :boxplot, :stat :boxplot},
      "numerical x, numerical y" {:mark :point, :stat :identity},
      "categorical x only" {:mark :rect, :stat :count},
      "numerical x only" {:mark :bar, :stat :bin}}
     m))
   v47_l330)))


(def v50_l363 (layer-type/lookup :point))


(deftest t51_l365 (is ((fn [m] (= :identity (:stat m))) v50_l363)))


(def
 v53_l396
 (def
  grouped-scatter
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species}))))


(def v54_l400 (-> grouped-scatter pj/plot pj/svg-summary :points))


(deftest t55_l405 (is ((fn [n] (= 150 n)) v54_l400)))


(def
 v57_l419
 (def
  resolved-layer
  (->
   grouped-scatter
   pj/draft
   :layers
   first
   resolve/resolve-draft-layer)))


(def
 v59_l429
 (->
  resolved-layer
  (select-keys
   [:x :y :x-type :y-type :group :color :size :alpha :fixed-color])))


(deftest
 t60_l432
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
   v59_l429)))


(def
 v62_l490
 (def
  scatter-stat
  (-> resolved-layer (assoc :cfg {}) stat/compute-stat)))


(def v63_l495 (sort (keys scatter-stat)))


(deftest
 t64_l497
 (is ((fn [ks] (= [:points :x-domain :y-domain] ks)) v63_l495)))


(def v66_l506 (count (:points scatter-stat)))


(deftest t67_l508 (is ((fn [n] (= 3 n)) v66_l506)))


(def
 v69_l513
 (->
  scatter-stat
  :points
  first
  (update :xs (fn* [p1__11197#] (vec (take 3 p1__11197#))))
  (update :ys (fn* [p1__11198#] (vec (take 3 p1__11198#))))
  (update :row-indices (fn* [p1__11199#] (vec (take 3 p1__11199#))))))


(deftest
 t70_l520
 (is
  ((fn [g] (and (= "setosa" (:color g)) (= [5.1 4.9 4.7] (:xs g))))
   v69_l513)))


(def
 v72_l563
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
 t73_l567
 (is
  ((fn
    [[stat-color plan-group]]
    (and
     (= "setosa" stat-color)
     (= "setosa" (:label plan-group))
     (vector? (:color plan-group))))
   v72_l563)))


(def
 v75_l585
 (->
  (rdatasets/datasets-iris)
  (pj/lay-line :sepal-length {:stat :density})))


(deftest
 t76_l588
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v75_l585)))


(def
 v78_l594
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :species :sepal-width {:stat :summary})))


(deftest
 t79_l597
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v78_l594)))


(def
 v81_l632
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


(deftest t82_l643 (is ((fn [t] (= 22 (count (:row-maps t)))) v81_l632)))


(def
 v84_l648
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t85_l651
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v84_l648)))


(def
 v87_l655
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
 t88_l661
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v87_l655)))


(def
 v90_l688
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


(deftest t91_l699 (is ((fn [t] (= 22 (count (:row-maps t)))) v90_l688)))


(def
 v93_l714
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
 v94_l720
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
 t95_l725
 (is
  ((fn [radii] (= [2.0 8.0] [(first radii) (last radii)])) v94_l720)))


(def
 v97_l841
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
 t98_l848
 (is
  ((fn [m] (and (= :rule-h (:mark m)) (= 3.0 (:y-intercept m))))
   v97_l841)))


(def v100_l876 (mark/mark-clip-region :point))


(deftest
 t101_l878
 (is ((fn* [p1__11200#] (= :drawing-area p1__11200#)) v100_l876)))


(def v102_l880 (mark/mark-clip-region :rug))


(deftest
 t103_l882
 (is ((fn* [p1__11201#] (= :panel-box p1__11201#)) v102_l880)))


(def
 v105_l892
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v106_l894 (mark/mark-clip-region :margin-glyph))


(deftest
 t107_l896
 (is ((fn* [p1__11202#] (= :panel-box p1__11202#)) v106_l894)))


(def v109_l900 (remove-method mark/mark-clip-region :margin-glyph))


(def
 v110_l902
 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t111_l904 (is (false? v110_l902)))


(def
 v113_l928
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v114_l933 (first (pj/plan->plot my-plan :svg {})))


(deftest t115_l935 (is ((fn [v] (= :svg v)) v114_l933)))


(def v117_l939 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v118_l941 (vector? my-figure))


(deftest t119_l943 (is ((fn [v] (true? v)) v118_l941)))


(def v121_l1001 (def my-membrane (pj/plan->membrane my-plan)))


(def v122_l1003 (pj/membrane? my-membrane))


(deftest t123_l1005 (is ((fn [v] (true? v)) v122_l1003)))


(def v124_l1007 (membrane.ui/width my-membrane))


(deftest t125_l1009 (is ((fn [v] (number? v)) v124_l1007)))


(def v126_l1011 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t127_l1013 (is ((fn [v] (= :svg v)) v126_l1011)))


(def
 v129_l1019
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v130_l1024 (pj/membrane? shortcut-membrane))


(deftest t131_l1026 (is ((fn [v] (true? v)) v130_l1024)))


(def
 v133_l1069
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
 t134_l1079
 (is ((fn [t] (= 3 (count (:row-maps t)))) v133_l1069)))


(def
 v136_l1095
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
 t137_l1106
 (is ((fn [t] (= 4 (count (:row-maps t)))) v136_l1095)))


(def
 v139_l1118
 (->>
  (methods scicloj.plotje.impl.coord/make-inverse)
  keys
  (filter keyword?)
  (remove #{:default})
  sort
  vec))


(deftest
 t140_l1125
 (is ((fn [ks] (= [:cartesian :fixed :flip] ks)) v139_l1118)))


(def
 v142_l1136
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t143_l1140
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v142_l1136)))


(def
 v145_l1154
 (kind/table
  {:column-names ["Dispatch value" "What it does"],
   :row-maps
   (->>
    (methods position/apply-position)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "What it does" (pj/position-doc k)})))}))


(deftest
 t146_l1165
 (is ((fn [t] (= 4 (count (:row-maps t)))) v145_l1154)))


(def
 v148_l1173
 (->
  {:site ["north" "north" "south" "south"],
   :yield [3 5 4 6],
   :year ["2024" "2025" "2024" "2025"]}
  (pj/lay-point :site :yield {:color :year, :position :dodge})
  pj/plan
  :panels
  first
  :layers
  first
  ((juxt
    :position
    :dodge-ctx
    (fn* [p1__11203#] (mapv :dodge-idx (:groups p1__11203#)))))))


(deftest
 t149_l1184
 (is ((fn [v] (= [:dodge {:n-groups 2} [0 1]] v)) v148_l1173)))


(def
 v151_l1198
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v152_l1201
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v154_l1206 (pj/stat-doc :quantile))


(deftest
 t155_l1208
 (is ((fn [v] (= "Quantile regression bands" v)) v154_l1206)))


(def v157_l1216 (remove-method stat/compute-stat [:quantile :doc]))


(def v158_l1218 (pj/stat-doc :quantile))


(deftest t159_l1220 (is ((fn [v] (= "(no description)" v)) v158_l1218)))


(def v161_l1226 (remove-method stat/compute-stat :quantile))


(def
 v162_l1228
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t163_l1230 (is ((fn [v] (= 11 v)) v162_l1228)))
