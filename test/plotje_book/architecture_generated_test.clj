(ns
 plotje-book.architecture-generated-test
 (:require
  [clojure.string :as str]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.pose :as pose]
  [scicloj.plotje.impl.plan :as plan]
  [membrane.ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] -->|pj/->pose| B[\"Pose\"]\n  B -->|pj/options pj/lay-* ...| B\n  B -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l166
 (def
  trace-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v7_l174 trace-pose)


(deftest
 t8_l176
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v7_l174)))


(def v10_l184 (kind/pprint trace-pose))


(deftest
 t11_l186
 (is
  ((fn
    [v]
    (and
     (pj/pose? v)
     (=
      [:petal-length :petal-width :species]
      [(:x (:mapping v)) (:y (:mapping v)) (:color (:mapping v))])
     (= 2 (count (:layers v)))
     (= [:point :smooth] (mapv :layer-type (:layers v)))))
   v10_l184)))


(def v13_l208 (def trace-draft (pj/pose->draft trace-pose)))


(def v14_l211 (kind/pprint trace-draft))


(deftest
 t15_l213
 (is
  ((fn
    [d]
    (and
     (pj/leaf-draft? d)
     (= 2 (count (:layers d)))
     (let
      [layers (:layers d)]
      (and
       (= [:point :line] (mapv :mark layers))
       (every?
        (fn* [p1__11196#] (= :petal-length (:x p1__11196#)))
        layers)
       (every?
        (fn* [p1__11197#] (= :petal-width (:y p1__11197#)))
        layers)
       (every?
        (fn* [p1__11198#] (= :species (:color p1__11198#)))
        layers)))
     (= {} (:opts d))))
   v14_l211)))


(def v17_l230 (def trace-plan (pj/draft->plan trace-draft)))


(def v19_l237 (kind/pprint trace-plan))


(deftest
 t20_l239
 (is
  ((fn
    [v]
    (and
     (pj/leaf-plan? v)
     (= 1 (count (:panels v)))
     (some? (:total-width v))
     (some? (:total-height v))
     (= 3 (count (get-in v [:legend :entries])))
     (let
      [layers (:layers (first (:panels v)))]
      (and
       (= [:point :line] (mapv :mark layers))
       (= 3 (count (:groups (first layers))))
       (= 3 (count (:groups (second layers))))))))
   v19_l237)))


(def v22_l251 (pj/valid-plan? trace-plan))


(deftest t23_l253 (is (true? v22_l251)))


(def v25_l262 (def trace-membrane (pj/plan->membrane trace-plan)))


(def v27_l269 (kind/pprint trace-membrane))


(deftest
 t28_l271
 (is
  ((fn
    [v]
    (and
     (pj/membrane? v)
     (pos? (count (:drawables v)))
     (every?
      (fn*
       [p1__11199#]
       (.startsWith (.getName (class p1__11199#)) "membrane.ui."))
      (:drawables v))))
   v27_l269)))


(def
 v30_l296
 (def trace-plot (pj/membrane->plot trace-membrane :svg {})))


(def v31_l299 (kind/pprint trace-plot))


(deftest
 t32_l301
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v31_l299)))


(def v34_l305 (kind/hiccup trace-plot))


(deftest
 t35_l307
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 3 (:lines s)))))
   v34_l305)))


(def
 v37_l335
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] --> B[\"Pose\"] --> D[\"Draft\"] --> P[\"Plan\"] --> M[\"Membrane\"] --> F[\"Plot\"]\n  X -.->|pj/pose| B\n  X -.->|pj/draft| D\n  X -.->|pj/plan| P\n  X -.->|pj/membrane| M\n  X -.->|pj/plot| F\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v39_l469
 (let
  [pose-with-opts
   (->
    trace-pose
    (pj/options
     {:title "Iris Petals", :x-label "Petal length", :width 700}))
   via-plan
   (pj/plan pose-with-opts)
   via-arrows
   (-> pose-with-opts pj/->pose pj/pose->draft pj/draft->plan)]
  {:title-match (= (:title via-plan) (:title via-arrows)),
   :x-label-match (= (:x-label via-plan) (:x-label via-arrows)),
   :width-match (= (:width via-plan) (:width via-arrows)),
   :title (:title via-plan),
   :x-label (:x-label via-plan),
   :width (:width via-plan)}))


(deftest
 t40_l485
 (is
  ((fn
    [m]
    (and
     (:title-match m)
     (:x-label-match m)
     (:width-match m)
     (= "Iris Petals" (:title m))
     (= "Petal length" (:x-label m))
     (= 700 (:width m))))
   v39_l469)))


(def
 v42_l512
 (pj/pose {:x [1 2 3 4 5], :y [2 4 3 5 4], :g [:a :a :b :b :b]}))


(deftest
 t43_l516
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 5 (:points s))
      (=
       2
       (count
        (filter
         (fn* [p1__11200#] (.startsWith p1__11200# "rgb"))
         (:colors s)))))))
   v42_l512)))


(def
 v45_l570
 (->>
  (->
   {:a [1 2 3 4], :b [2 4 3 5], :c ["u" "u" "v" "v"]}
   pj/->pose
   pj/lay-point
   (pj/lay-smooth :a :b)
   pj/pose->draft)
  :layers
  (mapv
   (fn* [p1__11201#] (select-keys p1__11201# [:mark :x :y :color])))))


(deftest
 t46_l578
 (is
  ((fn
    [layers]
    (=
     [{:mark :point, :x :a, :y :b, :color :c}
      {:mark :line, :x :a, :y :b, :color :c}]
     layers))
   v45_l570)))


(def
 v48_l595
 (->
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}
  (pj/lay-point :week :adults)
  (pj/lay-line {:y :children})
  pj/lay-smooth
  pose/elaborate
  (dissoc :data)
  kind/pprint))


(deftest
 t49_l605
 (is
  ((fn
    [elaborated]
    (and
     (nil? (:mapping elaborated))
     (=
      [{:x :week, :y :adults}
       {:x :week, :y :children}
       {:x :week, :y :adults}]
      (mapv :mapping (:layers elaborated)))))
   v48_l595)))


(def
 v51_l658
 (def
  counted-beside-points
  (->
   {:day ["mon" "tue" "mon" "wed"], :visits [3 5 2 4]}
   (pj/lay-point :day :visits)
   (pj/lay-bar :day))))


(def
 v52_l664
 (->> counted-beside-points pose/elaborate :layers (mapv :mapping)))


(deftest
 t53_l669
 (is
  ((fn
    [mappings]
    (and
     (= [{:x :day, :y :visits} {:x :day}] mappings)
     (=
      (pose/elaborate counted-beside-points)
      (pose/elaborate (pose/elaborate counted-beside-points)))))
   v52_l664)))


(def
 v55_l688
 (->
  {:height [1.6 1.7 1.8 1.7 1.75]}
  (pj/lay-histogram {:y :height})
  pose/elaborate
  (dissoc :data)
  kind/pprint))


(deftest
 t56_l694
 (is
  ((fn
    [elaborated]
    (and
     (= {:coord :flip} (:opts elaborated))
     (= [{:x :height}] (mapv :mapping (:layers elaborated)))))
   v55_l688)))


(def
 v58_l721
 (def
  sepals-and-petals
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width)
   (pj/lay-smooth {:color "black"}))))


(def
 v59_l727
 (->>
  sepals-and-petals
  pj/draft
  :layers
  (mapv
   (fn
    [layer]
    (let
     [color (:color layer)]
     (if (keyword? color) (first ((:data layer) color)) color))))))


(deftest
 t60_l736
 (is
  ((fn
    [colors]
    (and
     (=
      ["sepal length / sepal width"
       "petal length / petal width"
       "black"]
      colors)
     (=
      [{:x :sepal-length, :y :sepal-width}
       {:x :petal-length, :y :petal-width}
       {:color "black"}]
      (mapv :mapping (:layers sepals-and-petals)))
     (let
      [colored
       (->
        (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width {:color :species})
        (pj/lay-point :petal-length :petal-width))]
      (nil? (:color (second (:layers (pj/draft colored))))))))
   v59_l727)))


(def
 v62_l760
 (def
  composite-pose
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:petal-length :petal-width] [:sepal-length :sepal-width]])
   (pj/pose {:color :species}))))


(def v63_l766 composite-pose)


(deftest
 t64_l768
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v63_l766)))


(def v66_l776 (-> composite-pose pj/draft kind/pprint))


(deftest
 t67_l778
 (is
  ((fn [d] (and (pj/composite-draft? d) (= 2 (count (:sub-drafts d)))))
   v66_l776)))


(def v69_l783 (pj/plan composite-pose))


(deftest
 t70_l785
 (is
  ((fn [p] (and (pj/composite-plan? p) (= 2 (count (:sub-plots p)))))
   v69_l783)))


(def v72_l793 (pj/membrane composite-pose))


(deftest
 t73_l795
 (is
  ((fn
    [m]
    (and
     (pj/membrane? m)
     (pos? (count (:drawables m)))
     (number? (membrane.ui/width m))
     (number? (membrane.ui/height m))))
   v72_l793)))


(def v75_l804 (-> composite-pose pj/plot kind/pprint))


(deftest
 t76_l806
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v75_l804)))


(def
 v78_l834
 (def
  north
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [30 20 25 15]}))


(def
 v79_l839
 (def
  south
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [60 45 50 40]}))


(def
 v80_l844
 (def
  arranged-clinics
  (pj/arrange
   [(pj/lay-bar north :week :visits {:color :age, :position :stack})
    (pj/lay-bar south :week :visits {:color :age, :position :stack})]
   {:share-scales #{:y}})))


(def v81_l849 arranged-clinics)


(deftest
 t82_l851
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v81_l849)))


(def
 v83_l853
 (def
  faceted-clinics
  (->
   {:clinic
    ["north" "north" "north" "north" "south" "south" "south" "south"],
    :week ["w1" "w1" "w2" "w2" "w1" "w1" "w2" "w2"],
    :age
    ["adults"
     "children"
     "adults"
     "children"
     "adults"
     "children"
     "adults"
     "children"],
    :visits [30 20 25 15 60 45 50 40]}
   (pj/lay-bar :week :visits {:color :age, :position :stack})
   (pj/facet :clinic))))


(def v84_l862 faceted-clinics)


(deftest
 t85_l864
 (is
  ((fn
    [v]
    (and
     (pj/composite-plan? (pj/plan arranged-clinics))
     (not (pj/composite-plan? (pj/plan v)))
     (= 2 (count (:panels (pj/plan v))))))
   v84_l862)))


(def v87_l878 (->> arranged-clinics pj/draft :sub-drafts (mapv :share)))


(deftest
 t88_l883
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v87_l878)))


(def
 v90_l898
 (->>
  (pj/arrange
   [(pj/coord (pj/lay-bar north :week :visits) :flip)
    (pj/coord (pj/lay-bar south :week :visits) :flip)]
   {:share-scales #{:x}})
  pj/draft
  :sub-drafts
  (mapv :share)))


(deftest
 t91_l905
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v90_l898)))


(def
 v93_l913
 (->>
  (pj/arrange
   {:week ["w2" "w1" "w2" "w1"],
    :adults [30 25 28 35],
    :children [20 15 18 22]}
   [[:week :adults] [:week :children]])
  pj/draft
  :sub-drafts
  (mapv :share)))


(deftest
 t94_l921
 (is
  ((fn
    [shares]
    (and
     (=
      [{:x {:group 0, :order ["w2" "w1"]}}
       {:x {:group 0, :order ["w2" "w1"]}}]
      shares)
     (=
      [nil nil]
      (->>
       (pj/arrange
        [(pj/lay-bar north :week :visits)
         (pj/lay-bar south :week :visits)])
       pj/draft
       :sub-drafts
       (mapv :share)))))
   v93_l913)))


(def
 v96_l941
 (def
  weekly-visits
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}))


(def
 v97_l946
 (->>
  (pj/arrange
   [(pj/lay-point weekly-visits :week :adults)
    (pj/lay-point weekly-visits :week :children)
    (->
     weekly-visits
     (pj/lay-point :week :adults)
     (pj/lay-line :week :children))]
   {:share-scales #{:y}})
  pj/draft
  :sub-drafts
  (mapv :share)))


(deftest
 t98_l956
 (is
  ((fn
    [shares]
    (= [{:y {:group 0}} {:y {:group 0}} {:y {:group 0}}] shares))
   v97_l946)))


(def
 v100_l967
 (->>
  arranged-clinics
  pj/draft
  :sub-drafts
  (mapv
   (fn
    [sub-draft]
    (plan/draft->extents (:draft sub-draft) (:opts sub-draft))))))


(deftest
 t101_l973
 (is
  ((fn [extents] (= [[0.0 50.0] [0.0 105.0]] (mapv :y extents)))
   v100_l967)))


(def
 v103_l984
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv (fn [sub-plot] (:y-scale-domain (:opts sub-plot))))))


(deftest
 t104_l989
 (is ((fn [domains] (= [[0.0 105.0] [0.0 105.0]] domains)) v103_l984)))


(def
 v106_l994
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv
   (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot))))))))


(deftest
 t107_l999
 (is
  ((fn
    [domains]
    (and (apply = domains) (< 105.0 (second (first domains)))))
   v106_l994)))


(def v109_l1034 (->> faceted-clinics pj/plan :panels (mapv :y-domain)))


(deftest
 t110_l1039
 (is
  ((fn
    [domains]
    (=
     domains
     (mapv
      (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot)))))
      (:sub-plots (pj/plan arranged-clinics)))))
   v109_l1034)))


(def
 v112_l1057
 (def
  series-by-key
  (->
   weekly-visits
   (pj/lay-line :week [:adults :children])
   (pj/facet :series))))


(def v113_l1062 series-by-key)


(deftest
 t114_l1064
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v113_l1062)))


(def
 v116_l1070
 (-> series-by-key pose/expand-facet-nodes (dissoc :data) kind/pprint))


(deftest
 t117_l1075
 (is
  ((fn
    [expanded]
    (and
     (= {:x :week} (:mapping expanded))
     (=
      [{:y :adults} {:y :children}]
      (mapv :mapping (mapcat :poses (:poses expanded))))))
   v116_l1070)))


(def
 v119_l1087
 (->>
  (->
   weekly-visits
   pj/pose
   (pj/facet {:col {:x [:adults nil]}, :row {:y [:adults :children]}})
   pose/expand-facet-nodes)
  (tree-seq :poses :poses)
  (remove :poses)
  (mapv :mapping)))


(deftest
 t120_l1095
 (is
  ((fn
    [mappings]
    (=
     [{:x :adults, :y :adults}
      {:x nil, :y :adults}
      {:x :adults, :y :children}
      {:x nil, :y :children}]
     mappings))
   v119_l1087)))


(def
 v122_l1123
 (->>
  (->
   (select-keys weekly-visits [:adults :children])
   pj/lay-point
   pj/lay-density
   (pj/facet
    {:col {:x [:adults :children]}, :row {:y [:adults :children]}}))
  pj/draft
  :sub-drafts
  (mapv (fn [sub-draft] (mapv :mark (:draft sub-draft))))))


(deftest
 t123_l1132
 (is
  ((fn
    [marks]
    (let
     [marks-per-cell
      (fn
       [pose]
       (->>
        pose
        pj/draft
        :sub-drafts
        (mapv (fn [sub-draft] (mapv :mark (:draft sub-draft))))))]
     (and
      (= [[:area] [:point] [:point] [:area]] marks)
      (=
       [[:point :bar] [:bar]]
       (marks-per-cell
        (->
         weekly-visits
         (pj/lay-point :week :adults)
         (pj/lay-histogram {:x :week})
         (pj/facet {:y [:adults :children]}))))
      (re-find
       #"no panel draws it"
       (try
        (pj/draft
         (->
          weekly-visits
          (pj/pose {:x :week})
          (pj/lay-point {:y :week})
          (pj/facet {:y [:adults :children]})))
        ""
        (catch Exception e (ex-message e)))))))
   v122_l1123)))


(def v125_l1162 (->> series-by-key pj/draft :sub-drafts (mapv :share)))


(deftest
 t126_l1167
 (is
  ((fn [shares] (= [{:x {:group 0}} {:x {:group 0}}] shares))
   v125_l1162)))


(def
 v128_l1185
 (kind/table
  {:column-names
   ["Mistake" "Reported by" "First sentence of the message"],
   :row-vectors
   (for
    [[mistake build]
     [["A second column given to pj/lay-histogram"
       (fn* [] (pj/lay-histogram weekly-visits :adults :children))]
      ["Pairs of columns given to pj/pose"
       (fn*
        []
        (pj/pose weekly-visits [[:week :adults] [:week :children]]))]
      ["A key the map of pj/facet does not read"
       (fn*
        []
        (->
         weekly-visits
         (pj/lay-point :week :adults)
         (pj/facet {:y [:adults :children], :z [:week]})))]
      ["A column the data lacks"
       (fn* [] (pj/lay-point weekly-visits :week :teens))]
      ["A layer left without a column, on data of four columns"
       (fn*
        []
        (pj/lay-point (assoc weekly-visits :seniors [5 6 7 8])))]
      ["A category and a number on one axis"
       (fn*
        []
        (->
         north
         (pj/lay-point :visits :week)
         (pj/lay-point :age :week)))]
      ["A layer naming a column no cell of a facet over columns has"
       (fn*
        []
        (->
         weekly-visits
         (pj/pose {:x :week})
         (pj/lay-point {:y :week})
         (pj/facet {:y [:adults :children]})))]
      ["A bar of two categorical columns"
       (fn* [] (pj/lay-bar north :week :age))]
      ["A mark polar coordinates cannot draw"
       (fn*
        []
        (->
         weekly-visits
         (pj/lay-line :week :adults)
         (pj/coord :polar)))]
      ["Panels too small for the canvas"
       (fn*
        []
        (->
         north
         (pj/lay-point :week :visits)
         (pj/facet :age)
         (pj/options {:width 60})))]]]
    (let
     [report
      (fn
       [step f]
       (try
        (f)
        nil
        (catch
         Exception
         e
         [mistake
          step
          (first (str/split (ex-message e) #"(?<=\.) "))])))
      pose
      (try (build) (catch Exception _ nil))]
     (or
      (report "the call" build)
      (report "pj/pose->draft" (fn* [] (pj/pose->draft pose)))
      (report
       "pj/draft->plan"
       (fn* [] (pj/draft->plan (pj/pose->draft pose)))))))}))


(deftest
 t129_l1231
 (is
  ((fn
    [table]
    (=
     (concat
      (repeat 3 "the call")
      (repeat 4 "pj/pose->draft")
      (repeat 3 "pj/draft->plan"))
     (map second (:row-vectors table))))
   v128_l1185)))


(def
 v131_l1247
 (kind/mermaid
  "\ngraph LR\n  A[\"Pose + draft\"] -->|plan| P[\"Plan\"]\n  P --> R[\"membrane + plot\"]\n  style A fill:#e8f5e9\n  style P fill:#fff3e0\n  style R fill:#e3f2fd\n"))


(def
 v133_l1338
 (kind/mermaid
  "\ngraph TD\n  API[\"api.clj\"] --> POSE[\"impl/pose.clj\"]\n  API --> RES[\"impl/resolve.clj\"]\n  API --> PL[\"impl/plan.clj\"]\n  API --> COMP[\"impl/compositor.clj\"]\n  POSE --> RES\n  COMP --> POSE\n  COMP --> PL\n  PL --> RES\n  PL --> STAT[\"impl/stat.clj\"]\n  PL --> SCALE[\"impl/scale.clj\"]\n  PL --> DEFAULTS[\"impl/defaults.clj\"]\n  PL --> PS[\"impl/plan_schema.clj\"]\n  API --> RENDER[\"impl/render.clj\"]\n  RENDER --> SVG[\"render/svg.clj\"]\n  SVG --> MEMBRANE[\"render/membrane.clj\"]\n  MEMBRANE --> PANEL[\"render/panel.clj\"]\n  PANEL --> MARK[\"render/mark.clj\"]\n  PANEL --> SCALE\n  PANEL --> COORD[\"impl/coord.clj\"]\n  API --> RC[\"render/composite.clj\"]\n  RC --> MEMBRANE\n  style API fill:#c8e6c9\n  style COMP fill:#d1c4e9\n  style PL fill:#d1c4e9\n  style SVG fill:#f8bbd0\n  style MEMBRANE fill:#f8bbd0\n  style RC fill:#f8bbd0\n"))
