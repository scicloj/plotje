(ns
 plotje-book.architecture-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [membrane.ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l30
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] -->|pj/->pose| B[\"Pose\"]\n  B -->|pj/options pj/lay-* ...| B\n  B -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l156
 (def
  trace-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v7_l164 trace-pose)


(deftest
 t8_l166
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v7_l164)))


(def v10_l174 (kind/pprint trace-pose))


(deftest
 t11_l176
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
   v10_l174)))


(def v13_l198 (def trace-draft (pj/pose->draft trace-pose)))


(def v14_l201 (kind/pprint trace-draft))


(deftest
 t15_l203
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
        (fn* [p1__76998#] (= :petal-length (:x p1__76998#)))
        layers)
       (every?
        (fn* [p1__76999#] (= :petal-width (:y p1__76999#)))
        layers)
       (every?
        (fn* [p1__77000#] (= :species (:color p1__77000#)))
        layers)))
     (= {} (:opts d))))
   v14_l201)))


(def v17_l220 (def trace-plan (pj/draft->plan trace-draft)))


(def v19_l227 (kind/pprint trace-plan))


(deftest
 t20_l229
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
   v19_l227)))


(def v22_l241 (pj/valid-plan? trace-plan))


(deftest t23_l243 (is (true? v22_l241)))


(def v25_l252 (def trace-membrane (pj/plan->membrane trace-plan)))


(def v27_l259 (kind/pprint trace-membrane))


(deftest
 t28_l261
 (is
  ((fn
    [v]
    (and
     (pj/membrane? v)
     (pos? (count (:drawables v)))
     (every?
      (fn*
       [p1__77001#]
       (.startsWith (.getName (class p1__77001#)) "membrane.ui."))
      (:drawables v))))
   v27_l259)))


(def
 v30_l286
 (def trace-plot (pj/membrane->plot trace-membrane :svg {})))


(def v31_l289 (kind/pprint trace-plot))


(deftest
 t32_l291
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v31_l289)))


(def v34_l295 (kind/hiccup trace-plot))


(deftest
 t35_l297
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 3 (:lines s)))))
   v34_l295)))


(def
 v37_l326
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] --> B[\"Pose\"] --> D[\"Draft\"] --> P[\"Plan\"] --> M[\"Membrane\"] --> F[\"Plot\"]\n  X -.->|pj/pose| B\n  X -.->|pj/draft| D\n  X -.->|pj/plan| P\n  X -.->|pj/membrane| M\n  X -.->|pj/plot| F\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v39_l460
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
 t40_l476
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
   v39_l460)))


(def
 v42_l503
 (pj/pose {:x [1 2 3 4 5], :y [2 4 3 5 4], :g [:a :a :b :b :b]}))


(deftest
 t43_l507
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
         (fn* [p1__77002#] (.startsWith p1__77002# "rgb"))
         (:colors s)))))))
   v42_l503)))


(def
 v45_l561
 (def
  composite-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose
    [[:petal-length :petal-width] [:sepal-length :sepal-width]]
    {:color :species})
   pj/lay-point)))


(def v46_l568 composite-pose)


(deftest
 t47_l570
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v46_l568)))


(def v49_l577 (-> composite-pose pj/draft kind/pprint))


(deftest
 t50_l579
 (is
  ((fn [d] (and (pj/composite-draft? d) (= 2 (count (:sub-drafts d)))))
   v49_l577)))


(def v52_l584 (pj/plan composite-pose))


(deftest
 t53_l586
 (is
  ((fn [p] (and (pj/composite-plan? p) (= 2 (count (:sub-plots p)))))
   v52_l584)))


(def v55_l594 (pj/membrane composite-pose))


(deftest
 t56_l596
 (is
  ((fn
    [m]
    (and
     (pj/membrane? m)
     (pos? (count (:drawables m)))
     (number? (membrane.ui/width m))
     (number? (membrane.ui/height m))))
   v55_l594)))


(def v58_l605 (-> composite-pose pj/plot kind/pprint))


(deftest
 t59_l607
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v58_l605)))


(def
 v61_l635
 (kind/mermaid
  "\ngraph LR\n  A[\"Pose + draft\"] -->|plan| P[\"Plan\"]\n  P --> R[\"membrane + plot\"]\n  style A fill:#e8f5e9\n  style P fill:#fff3e0\n  style R fill:#e3f2fd\n"))


(def
 v63_l726
 (kind/mermaid
  "\ngraph TD\n  API[\"api.clj\"] --> POSE[\"impl/pose.clj\"]\n  API --> RES[\"impl/resolve.clj\"]\n  API --> PL[\"impl/plan.clj\"]\n  API --> COMP[\"impl/compositor.clj\"]\n  POSE --> RES\n  COMP --> POSE\n  COMP --> PL\n  PL --> RES\n  PL --> STAT[\"impl/stat.clj\"]\n  PL --> SCALE[\"impl/scale.clj\"]\n  PL --> DEFAULTS[\"impl/defaults.clj\"]\n  PL --> PS[\"impl/plan_schema.clj\"]\n  API --> RENDER[\"impl/render.clj\"]\n  RENDER --> SVG[\"render/svg.clj\"]\n  SVG --> MEMBRANE[\"render/membrane.clj\"]\n  MEMBRANE --> PANEL[\"render/panel.clj\"]\n  PANEL --> MARK[\"render/mark.clj\"]\n  PANEL --> SCALE\n  PANEL --> COORD[\"impl/coord.clj\"]\n  API --> RC[\"render/composite.clj\"]\n  RC --> MEMBRANE\n  style API fill:#c8e6c9\n  style COMP fill:#d1c4e9\n  style PL fill:#d1c4e9\n  style SVG fill:#f8bbd0\n  style MEMBRANE fill:#f8bbd0\n  style RC fill:#f8bbd0\n"))
