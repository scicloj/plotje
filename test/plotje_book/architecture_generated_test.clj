(ns
 plotje-book.architecture-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.pose :as pose]
  [scicloj.plotje.impl.plan :as plan]
  [membrane.ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l33
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] -->|pj/->pose| B[\"Pose\"]\n  B -->|pj/options pj/lay-* ...| B\n  B -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l163
 (def
  trace-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v7_l171 trace-pose)


(deftest
 t8_l173
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v7_l171)))


(def v10_l181 (kind/pprint trace-pose))


(deftest
 t11_l183
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
   v10_l181)))


(def v13_l205 (def trace-draft (pj/pose->draft trace-pose)))


(def v14_l208 (kind/pprint trace-draft))


(deftest
 t15_l210
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
        (fn* [p1__76344#] (= :petal-length (:x p1__76344#)))
        layers)
       (every?
        (fn* [p1__76345#] (= :petal-width (:y p1__76345#)))
        layers)
       (every?
        (fn* [p1__76346#] (= :species (:color p1__76346#)))
        layers)))
     (= {} (:opts d))))
   v14_l208)))


(def v17_l227 (def trace-plan (pj/draft->plan trace-draft)))


(def v19_l234 (kind/pprint trace-plan))


(deftest
 t20_l236
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
   v19_l234)))


(def v22_l248 (pj/valid-plan? trace-plan))


(deftest t23_l250 (is (true? v22_l248)))


(def v25_l259 (def trace-membrane (pj/plan->membrane trace-plan)))


(def v27_l266 (kind/pprint trace-membrane))


(deftest
 t28_l268
 (is
  ((fn
    [v]
    (and
     (pj/membrane? v)
     (pos? (count (:drawables v)))
     (every?
      (fn*
       [p1__76347#]
       (.startsWith (.getName (class p1__76347#)) "membrane.ui."))
      (:drawables v))))
   v27_l266)))


(def
 v30_l293
 (def trace-plot (pj/membrane->plot trace-membrane :svg {})))


(def v31_l296 (kind/pprint trace-plot))


(deftest
 t32_l298
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v31_l296)))


(def v34_l302 (kind/hiccup trace-plot))


(deftest
 t35_l304
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 3 (:lines s)))))
   v34_l302)))


(def
 v37_l332
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] --> B[\"Pose\"] --> D[\"Draft\"] --> P[\"Plan\"] --> M[\"Membrane\"] --> F[\"Plot\"]\n  X -.->|pj/pose| B\n  X -.->|pj/draft| D\n  X -.->|pj/plan| P\n  X -.->|pj/membrane| M\n  X -.->|pj/plot| F\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v39_l466
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
 t40_l482
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
   v39_l466)))


(def
 v42_l509
 (pj/pose {:x [1 2 3 4 5], :y [2 4 3 5 4], :g [:a :a :b :b :b]}))


(deftest
 t43_l513
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
         (fn* [p1__76348#] (.startsWith p1__76348# "rgb"))
         (:colors s)))))))
   v42_l509)))


(def
 v45_l574
 (->
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}
  (pj/lay-point :week :adults)
  (pj/lay-line {:y :children})
  pj/lay-smooth
  pose/elaborate
  (dissoc :data)
  kind/pprint))


(deftest
 t46_l584
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
   v45_l574)))


(def
 v48_l622
 (def
  composite-pose
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [[:petal-length :petal-width] [:sepal-length :sepal-width]])
   (pj/pose {:color :species}))))


(def v49_l628 composite-pose)


(deftest
 t50_l630
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v49_l628)))


(def v52_l638 (-> composite-pose pj/draft kind/pprint))


(deftest
 t53_l640
 (is
  ((fn [d] (and (pj/composite-draft? d) (= 2 (count (:sub-drafts d)))))
   v52_l638)))


(def v55_l645 (pj/plan composite-pose))


(deftest
 t56_l647
 (is
  ((fn [p] (and (pj/composite-plan? p) (= 2 (count (:sub-plots p)))))
   v55_l645)))


(def v58_l655 (pj/membrane composite-pose))


(deftest
 t59_l657
 (is
  ((fn
    [m]
    (and
     (pj/membrane? m)
     (pos? (count (:drawables m)))
     (number? (membrane.ui/width m))
     (number? (membrane.ui/height m))))
   v58_l655)))


(def v61_l666 (-> composite-pose pj/plot kind/pprint))


(deftest
 t62_l668
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v61_l666)))


(def
 v64_l696
 (def
  north
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [30 20 25 15]}))


(def
 v65_l701
 (def
  south
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [60 45 50 40]}))


(def
 v66_l706
 (def
  arranged-clinics
  (pj/arrange
   [(pj/lay-bar north :week :visits {:color :age, :position :stack})
    (pj/lay-bar south :week :visits {:color :age, :position :stack})]
   {:share-scales #{:y}})))


(def v67_l711 arranged-clinics)


(deftest
 t68_l713
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v67_l711)))


(def
 v69_l715
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


(def v70_l724 faceted-clinics)


(deftest
 t71_l726
 (is
  ((fn
    [v]
    (and
     (pj/composite-plan? (pj/plan arranged-clinics))
     (not (pj/composite-plan? (pj/plan v)))
     (= 2 (count (:panels (pj/plan v))))))
   v70_l724)))


(def v73_l737 (->> arranged-clinics pj/draft :sub-drafts (mapv :share)))


(deftest
 t74_l742
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v73_l737)))


(def
 v76_l757
 (->>
  (pj/arrange
   [(pj/coord (pj/lay-bar north :week :visits) :flip)
    (pj/coord (pj/lay-bar south :week :visits) :flip)]
   {:share-scales #{:x}})
  pj/draft
  :sub-drafts
  (mapv :share)))


(deftest
 t77_l764
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v76_l757)))


(def
 v79_l774
 (->>
  arranged-clinics
  pj/draft
  :sub-drafts
  (mapv
   (fn
    [sub-draft]
    (plan/draft->extents (:draft sub-draft) (:opts sub-draft))))))


(deftest
 t80_l780
 (is
  ((fn [extents] (= [[0.0 50.0] [0.0 105.0]] (mapv :y extents)))
   v79_l774)))


(def
 v82_l791
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv (fn [sub-plot] (:y-scale-domain (:opts sub-plot))))))


(deftest
 t83_l796
 (is ((fn [domains] (= [[0.0 105.0] [0.0 105.0]] domains)) v82_l791)))


(def
 v85_l801
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv
   (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot))))))))


(deftest
 t86_l806
 (is
  ((fn
    [domains]
    (and (apply = domains) (< 105.0 (second (first domains)))))
   v85_l801)))


(def v88_l839 (->> faceted-clinics pj/plan :panels (mapv :y-domain)))


(deftest
 t89_l844
 (is
  ((fn
    [domains]
    (=
     domains
     (mapv
      (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot)))))
      (:sub-plots (pj/plan arranged-clinics)))))
   v88_l839)))


(def
 v91_l862
 (def
  weekly-visits
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}))


(def
 v92_l867
 (def
  series-by-key
  (->
   weekly-visits
   (pj/lay-line :week [:adults :children])
   (pj/facet :series))))


(def v93_l872 series-by-key)


(deftest
 t94_l874
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v93_l872)))


(def
 v96_l880
 (-> series-by-key pose/expand-facet-nodes (dissoc :data) kind/pprint))


(deftest
 t97_l885
 (is
  ((fn
    [expanded]
    (and
     (= {:x :week} (:mapping expanded))
     (=
      [{:y :adults} {:y :children}]
      (mapv :mapping (mapcat :poses (:poses expanded))))))
   v96_l880)))


(def
 v99_l897
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
 t100_l905
 (is
  ((fn
    [mappings]
    (=
     [{:x :adults, :y :adults}
      {:x nil, :y :adults}
      {:x :adults, :y :children}
      {:x nil, :y :children}]
     mappings))
   v99_l897)))


(def
 v102_l935
 (kind/mermaid
  "\ngraph LR\n  A[\"Pose + draft\"] -->|plan| P[\"Plan\"]\n  P --> R[\"membrane + plot\"]\n  style A fill:#e8f5e9\n  style P fill:#fff3e0\n  style R fill:#e3f2fd\n"))


(def
 v104_l1026
 (kind/mermaid
  "\ngraph TD\n  API[\"api.clj\"] --> POSE[\"impl/pose.clj\"]\n  API --> RES[\"impl/resolve.clj\"]\n  API --> PL[\"impl/plan.clj\"]\n  API --> COMP[\"impl/compositor.clj\"]\n  POSE --> RES\n  COMP --> POSE\n  COMP --> PL\n  PL --> RES\n  PL --> STAT[\"impl/stat.clj\"]\n  PL --> SCALE[\"impl/scale.clj\"]\n  PL --> DEFAULTS[\"impl/defaults.clj\"]\n  PL --> PS[\"impl/plan_schema.clj\"]\n  API --> RENDER[\"impl/render.clj\"]\n  RENDER --> SVG[\"render/svg.clj\"]\n  SVG --> MEMBRANE[\"render/membrane.clj\"]\n  MEMBRANE --> PANEL[\"render/panel.clj\"]\n  PANEL --> MARK[\"render/mark.clj\"]\n  PANEL --> SCALE\n  PANEL --> COORD[\"impl/coord.clj\"]\n  API --> RC[\"render/composite.clj\"]\n  RC --> MEMBRANE\n  style API fill:#c8e6c9\n  style COMP fill:#d1c4e9\n  style PL fill:#d1c4e9\n  style SVG fill:#f8bbd0\n  style MEMBRANE fill:#f8bbd0\n  style RC fill:#f8bbd0\n"))
