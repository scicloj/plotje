(ns
 plotje-book.architecture-generated-test
 (:require
  [clojure.string :as str]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.pose :as pose]
  [scicloj.plotje.impl.plan :as plan]
  [clojure.test :refer [deftest is]]))


(def
 v3_l33
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] -->|pj/->pose| B[\"Pose\"]\n  B -->|pj/options pj/lay-* ...| B\n  B -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l170
 (def
  trace-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v7_l178 trace-pose)


(deftest
 t8_l180
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v7_l178)))


(def v10_l188 (kind/pprint trace-pose))


(deftest
 t11_l190
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
   v10_l188)))


(def v13_l221 (def trace-draft (pj/pose->draft trace-pose)))


(def v14_l224 (kind/pprint trace-draft))


(deftest
 t15_l226
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
        (fn* [p1__79581#] (= :petal-length (:x p1__79581#)))
        layers)
       (every?
        (fn* [p1__79582#] (= :petal-width (:y p1__79582#)))
        layers)
       (every?
        (fn* [p1__79583#] (= :species (:color p1__79583#)))
        layers)))
     (= {} (:opts d))))
   v14_l224)))


(def v17_l243 (def trace-plan (pj/draft->plan trace-draft)))


(def v19_l250 (kind/pprint trace-plan))


(deftest
 t20_l252
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
   v19_l250)))


(def v22_l264 (pj/valid-plan? trace-plan))


(deftest t23_l266 (is (true? v22_l264)))


(def v25_l275 (def trace-membrane (pj/plan->membrane trace-plan)))


(def
 v27_l282
 (select-keys trace-membrane [:width :height :plotje/title]))


(deftest
 t28_l284
 (is
  ((fn
    [m]
    (and
     (= #{:width :height} (set (keys m)))
     (== (:total-width trace-plan) (:width m))
     (== (:total-height trace-plan) (:height m))))
   v27_l282)))


(def
 v30_l291
 (->>
  trace-membrane
  :drawables
  (mapv (fn* [p1__79584#] (.getSimpleName (class p1__79584#))))))


(deftest
 t31_l295
 (is
  ((fn [names] (and (seq names) (every? #{"Translate"} names)))
   v30_l291)))


(def
 v33_l317
 (def trace-plot (pj/membrane->plot trace-membrane :svg {})))


(def v35_l324 (pj/svg-summary trace-plot))


(deftest
 t36_l326
 (is
  ((fn
    [s]
    (and
     (vector? trace-plot)
     (= :svg (first trace-plot))
     (= 1 (:panels s))
     (= 150 (:points s))
     (= 3 (:lines s))))
   v35_l324)))


(def v38_l334 (kind/hiccup trace-plot))


(deftest
 t39_l336
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v38_l334)))


(def
 v41_l379
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] --> B[\"Pose\"] --> D[\"Draft\"] --> P[\"Plan\"] --> M[\"Membrane\"] --> F[\"Plot\"]\n  X -.->|pj/pose| B\n  X -.->|pj/draft| D\n  X -.->|pj/plan| P\n  X -.->|pj/membrane| M\n  X -.->|pj/plot| F\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v43_l516
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
 t44_l532
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
   v43_l516)))


(def
 v46_l559
 (pj/pose {:x [1 2 3 4 5], :y [2 4 3 5 4], :g [:a :a :b :b :b]}))


(deftest
 t47_l563
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
         (fn* [p1__79585#] (.startsWith p1__79585# "rgb"))
         (:colors s)))))))
   v46_l559)))


(def
 v49_l620
 (->>
  (->
   {:a [1 2 3 4], :b [2 4 3 5], :c ["u" "u" "v" "v"]}
   pj/->pose
   pj/lay-point
   (pj/lay-smooth :a :b)
   pj/pose->draft)
  :layers
  (mapv
   (fn* [p1__79586#] (select-keys p1__79586# [:mark :x :y :color])))))


(deftest
 t50_l628
 (is
  ((fn
    [layers]
    (=
     [{:mark :point, :x :a, :y :b, :color :c}
      {:mark :line, :x :a, :y :b, :color :c}]
     layers))
   v49_l620)))


(def
 v52_l663
 (def
  composite-pose
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [{:x :petal-length, :y :petal-width}
     {:x :sepal-length, :y :sepal-width}])
   (pj/pose {:color :species}))))


(def v53_l669 composite-pose)


(deftest
 t54_l671
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v53_l669)))


(def v56_l679 (-> composite-pose pj/draft kind/pprint))


(deftest
 t57_l683
 (is
  ((fn [d] (and (pj/composite-draft? d) (= 2 (count (:sub-drafts d)))))
   v56_l679)))


(def
 v59_l694
 (-> composite-pose pj/draft :sub-drafts first (update :draft count)))


(deftest
 t60_l700
 (is
  ((fn
    [sub-draft]
    (and
     (= #{:path :draft :opts :rect} (set (keys sub-draft)))
     (= [0 0] (:path sub-draft))
     (= 1 (:draft sub-draft))
     (true? (-> sub-draft :opts :suppress-color-legend))))
   v59_l694)))


(def v62_l721 (pj/plan composite-pose))


(deftest
 t63_l723
 (is
  ((fn [p] (and (pj/composite-plan? p) (= 2 (count (:sub-plots p)))))
   v62_l721)))


(def
 v65_l729
 (-> composite-pose pj/plan :sub-plots first :plan pj/leaf-plan?))


(deftest t66_l736 (is (true? v65_l729)))


(def
 v68_l743
 (->
  composite-pose
  pj/membrane
  (select-keys [:width :height :plotje/title])))


(deftest
 t69_l747
 (is
  ((fn [m] (and (number? (:width m)) (number? (:height m)))) v68_l743)))


(def
 v70_l750
 (->>
  composite-pose
  pj/membrane
  :drawables
  (mapv (fn* [p1__79587#] (.getSimpleName (class p1__79587#))))))


(deftest
 t71_l755
 (is ((fn [names] (every? #{"Translate"} names)) v70_l750)))


(def v73_l760 (-> composite-pose pj/plot pj/svg-summary))


(deftest
 t74_l764
 (is ((fn [s] (and (= 2 (:panels s)) (= 300 (:points s)))) v73_l760)))


(def
 v76_l793
 (def
  north
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [30 20 25 15]}))


(def
 v77_l798
 (def
  south
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [60 45 50 40]}))


(def
 v78_l803
 (def
  arranged-clinics
  (pj/arrange
   [(pj/lay-bar north :week :visits {:color :age, :position :stack})
    (pj/lay-bar south :week :visits {:color :age, :position :stack})]
   {:share-scales #{:y}})))


(def v79_l808 arranged-clinics)


(deftest
 t80_l810
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v79_l808)))


(def
 v81_l812
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


(def v82_l821 faceted-clinics)


(deftest
 t83_l823
 (is
  ((fn
    [v]
    (and
     (pj/composite-plan? (pj/plan arranged-clinics))
     (not (pj/composite-plan? (pj/plan v)))
     (= 2 (count (:panels (pj/plan v))))))
   v82_l821)))


(def v85_l837 (->> arranged-clinics pj/draft :sub-drafts (mapv :share)))


(deftest
 t86_l842
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v85_l837)))


(def
 v88_l861
 (->>
  arranged-clinics
  pj/draft
  :sub-drafts
  (mapv
   (fn
    [sub-draft]
    (plan/draft->extents (:draft sub-draft) (:opts sub-draft))))))


(deftest
 t89_l867
 (is
  ((fn [extents] (= [[0.0 50.0] [0.0 105.0]] (mapv :y extents)))
   v88_l861)))


(def
 v91_l878
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv (fn [sub-plot] (:y-scale-domain (:opts sub-plot))))))


(deftest
 t92_l883
 (is ((fn [domains] (= [[0.0 105.0] [0.0 105.0]] domains)) v91_l878)))


(def
 v94_l888
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv
   (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot))))))))


(deftest
 t95_l893
 (is
  ((fn
    [domains]
    (and (apply = domains) (< 105.0 (second (first domains)))))
   v94_l888)))


(def v97_l913 (->> faceted-clinics pj/plan :panels (mapv :y-domain)))


(deftest
 t98_l918
 (is
  ((fn
    [domains]
    (=
     domains
     (mapv
      (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot)))))
      (:sub-plots (pj/plan arranged-clinics)))))
   v97_l913)))


(def
 v100_l936
 (def
  weekly-visits
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}))


(def
 v101_l941
 (def
  series-by-key
  (->
   weekly-visits
   (pj/lay-line :week [:adults :children])
   (pj/facet :series))))


(def v102_l946 series-by-key)


(deftest
 t103_l948
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v102_l946)))


(def
 v105_l954
 (-> series-by-key pose/expand-facet-nodes (dissoc :data) kind/pprint))


(deftest
 t106_l959
 (is
  ((fn
    [expanded]
    (and
     (= {:x :week} (:mapping expanded))
     (=
      [{:y :adults} {:y :children}]
      (mapv :mapping (mapcat :poses (:poses expanded))))))
   v105_l954)))


(def v108_l971 (->> series-by-key pj/draft :sub-drafts (mapv :share)))


(deftest
 t109_l976
 (is
  ((fn [shares] (= [{:x {:group 0}} {:x {:group 0}}] shares))
   v108_l971)))


(def
 v111_l1001
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
      ["A key pj/facet's map does not accept"
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
      ["A layer naming a column no cell has, on a facet over columns or an arrangement"
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
 t112_l1047
 (is
  ((fn
    [table]
    (=
     (concat
      (repeat 3 "the call")
      (repeat 4 "pj/pose->draft")
      (repeat 3 "pj/draft->plan"))
     (map second (:row-vectors table))))
   v111_l1001)))
