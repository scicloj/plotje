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
 v5_l171
 (def
  trace-pose
  (->
   (rdatasets/datasets-iris)
   (pj/pose :petal-length :petal-width {:color :species})
   pj/lay-point
   (pj/lay-smooth {:stat :linear-model}))))


(def v7_l179 trace-pose)


(deftest
 t8_l181
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 3 (:lines s)))))
   v7_l179)))


(def v10_l189 (kind/pprint trace-pose))


(deftest
 t11_l191
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
   v10_l189)))


(def v13_l222 (def trace-draft (pj/pose->draft trace-pose)))


(def v14_l225 (kind/pprint trace-draft))


(deftest
 t15_l227
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
        (fn* [p1__78855#] (= :petal-length (:x p1__78855#)))
        layers)
       (every?
        (fn* [p1__78856#] (= :petal-width (:y p1__78856#)))
        layers)
       (every?
        (fn* [p1__78857#] (= :species (:color p1__78857#)))
        layers)))
     (= {} (:opts d))))
   v14_l225)))


(def v17_l244 (def trace-plan (pj/draft->plan trace-draft)))


(def v19_l251 (kind/pprint trace-plan))


(deftest
 t20_l253
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
   v19_l251)))


(def v22_l265 (pj/valid-plan? trace-plan))


(deftest t23_l267 (is (true? v22_l265)))


(def v25_l276 (def trace-membrane (pj/plan->membrane trace-plan)))


(def v27_l283 (kind/pprint trace-membrane))


(deftest
 t28_l285
 (is
  ((fn
    [v]
    (and
     (pj/membrane? v)
     (pos? (count (:drawables v)))
     (every?
      (fn*
       [p1__78858#]
       (.startsWith (.getName (class p1__78858#)) "membrane.ui."))
      (:drawables v))))
   v27_l283)))


(def
 v30_l310
 (def trace-plot (pj/membrane->plot trace-membrane :svg {})))


(def v31_l313 (kind/pprint trace-plot))


(deftest
 t32_l315
 (is ((fn [v] (and (vector? v) (= :svg (first v)))) v31_l313)))


(def v34_l319 (kind/hiccup trace-plot))


(deftest
 t35_l321
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)) (= 3 (:lines s)))))
   v34_l319)))


(def
 v37_l353
 (kind/mermaid
  "\ngraph LR\n  X[\"Raw data\"] --> B[\"Pose\"] --> D[\"Draft\"] --> P[\"Plan\"] --> M[\"Membrane\"] --> F[\"Plot\"]\n  X -.->|pj/pose| B\n  X -.->|pj/draft| D\n  X -.->|pj/plan| P\n  X -.->|pj/membrane| M\n  X -.->|pj/plot| F\n  style X fill:#eee,stroke-dasharray:3 3\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v39_l490
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
 t40_l506
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
   v39_l490)))


(def
 v42_l533
 (pj/pose {:x [1 2 3 4 5], :y [2 4 3 5 4], :g [:a :a :b :b :b]}))


(deftest
 t43_l537
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
         (fn* [p1__78859#] (.startsWith p1__78859# "rgb"))
         (:colors s)))))))
   v42_l533)))


(def
 v45_l592
 (->>
  (->
   {:a [1 2 3 4], :b [2 4 3 5], :c ["u" "u" "v" "v"]}
   pj/->pose
   pj/lay-point
   (pj/lay-smooth :a :b)
   pj/pose->draft)
  :layers
  (mapv
   (fn* [p1__78860#] (select-keys p1__78860# [:mark :x :y :color])))))


(deftest
 t46_l600
 (is
  ((fn
    [layers]
    (=
     [{:mark :point, :x :a, :y :b, :color :c}
      {:mark :line, :x :a, :y :b, :color :c}]
     layers))
   v45_l592)))


(def
 v48_l617
 (->
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}
  (pj/lay-point :week :adults)
  (pj/lay-line {:y :children})
  pj/lay-smooth
  pose/elaborate
  (dissoc :data)
  kind/pprint))


(deftest
 t49_l627
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
   v48_l617)))


(def
 v51_l681
 (def
  counted-beside-points
  (->
   {:day ["mon" "tue" "mon" "wed"], :visits [3 5 2 4]}
   (pj/lay-point :day :visits)
   (pj/lay-bar :day))))


(def
 v52_l687
 (->> counted-beside-points pose/elaborate :layers (mapv :mapping)))


(deftest
 t53_l692
 (is
  ((fn
    [mappings]
    (and
     (= [{:x :day, :y :visits} {:x :day}] mappings)
     (=
      (pose/elaborate counted-beside-points)
      (pose/elaborate (pose/elaborate counted-beside-points)))))
   v52_l687)))


(def
 v55_l711
 (->
  {:height [1.6 1.7 1.8 1.7 1.75]}
  (pj/lay-histogram {:y :height})
  pose/elaborate
  (dissoc :data)
  kind/pprint))


(deftest
 t56_l717
 (is
  ((fn
    [elaborated]
    (and
     (= {:coord :flip} (:opts elaborated))
     (= [{:x :height}] (mapv :mapping (:layers elaborated)))))
   v55_l711)))


(def
 v58_l744
 (def
  sepals-and-petals
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/lay-point :petal-length :petal-width)
   (pj/lay-smooth {:color "black"}))))


(def
 v59_l750
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
 t60_l759
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
   v59_l750)))


(def
 v62_l783
 (def
  composite-pose
  (->
   (rdatasets/datasets-iris)
   (pj/arrange
    [{:x :petal-length, :y :petal-width}
     {:x :sepal-length, :y :sepal-width}])
   (pj/pose {:color :species}))))


(def v63_l789 composite-pose)


(deftest
 t64_l791
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v63_l789)))


(def v66_l799 (-> composite-pose pj/draft kind/pprint))


(deftest
 t67_l801
 (is
  ((fn [d] (and (pj/composite-draft? d) (= 2 (count (:sub-drafts d)))))
   v66_l799)))


(def
 v69_l812
 (-> composite-pose pj/draft :sub-drafts first (update :draft count)))


(deftest
 t70_l818
 (is
  ((fn
    [sub-draft]
    (and
     (= #{:path :draft :opts :rect} (set (keys sub-draft)))
     (= [0 0] (:path sub-draft))
     (= 1 (:draft sub-draft))
     (true? (-> sub-draft :opts :suppress-color-legend))))
   v69_l812)))


(def v72_l828 (pj/plan composite-pose))


(deftest
 t73_l830
 (is
  ((fn [p] (and (pj/composite-plan? p) (= 2 (count (:sub-plots p)))))
   v72_l828)))


(def
 v75_l836
 (-> composite-pose pj/plan :sub-plots first :plan pj/leaf-plan?))


(deftest t76_l843 (is (true? v75_l836)))


(def v78_l850 (pj/membrane composite-pose))


(deftest
 t79_l852
 (is
  ((fn
    [m]
    (and
     (pj/membrane? m)
     (pos? (count (:drawables m)))
     (number? (membrane.ui/width m))
     (number? (membrane.ui/height m))))
   v78_l850)))


(def v81_l861 (-> composite-pose pj/plot kind/pprint))


(deftest
 t82_l863
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 300 (:points s)))))
   v81_l861)))


(def
 v84_l891
 (def
  north
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [30 20 25 15]}))


(def
 v85_l896
 (def
  south
  {:week ["w1" "w1" "w2" "w2"],
   :age ["adults" "children" "adults" "children"],
   :visits [60 45 50 40]}))


(def
 v86_l901
 (def
  arranged-clinics
  (pj/arrange
   [(pj/lay-bar north :week :visits {:color :age, :position :stack})
    (pj/lay-bar south :week :visits {:color :age, :position :stack})]
   {:share-scales #{:y}})))


(def v87_l906 arranged-clinics)


(deftest
 t88_l908
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v87_l906)))


(def
 v89_l910
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


(def v90_l919 faceted-clinics)


(deftest
 t91_l921
 (is
  ((fn
    [v]
    (and
     (pj/composite-plan? (pj/plan arranged-clinics))
     (not (pj/composite-plan? (pj/plan v)))
     (= 2 (count (:panels (pj/plan v))))))
   v90_l919)))


(def v93_l935 (->> arranged-clinics pj/draft :sub-drafts (mapv :share)))


(deftest
 t94_l940
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v93_l935)))


(def
 v96_l959
 (->>
  (pj/arrange
   [(pj/coord (pj/lay-bar north :week :visits) :flip)
    (pj/coord (pj/lay-bar south :week :visits) :flip)]
   {:share-scales #{:x}})
  pj/draft
  :sub-drafts
  (mapv :share)))


(deftest
 t97_l966
 (is
  ((fn [shares] (= [{:y {:group 0}} {:y {:group 0}}] shares))
   v96_l959)))


(def
 v99_l974
 (->>
  (pj/arrange
   {:week ["w2" "w1" "w2" "w1"],
    :adults [30 25 28 35],
    :children [20 15 18 22]}
   [{:x :week, :y :adults} {:x :week, :y :children}])
  pj/draft
  :sub-drafts
  (mapv :share)))


(deftest
 t100_l982
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
   v99_l974)))


(def
 v102_l1002
 (def
  weekly-visits
  {:week [1 2 3 4], :adults [30 25 28 35], :children [20 15 18 22]}))


(def
 v103_l1007
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
 t104_l1017
 (is
  ((fn
    [shares]
    (= [{:y {:group 0}} {:y {:group 0}} {:y {:group 0}}] shares))
   v103_l1007)))


(def
 v106_l1028
 (->>
  arranged-clinics
  pj/draft
  :sub-drafts
  (mapv
   (fn
    [sub-draft]
    (plan/draft->extents (:draft sub-draft) (:opts sub-draft))))))


(deftest
 t107_l1034
 (is
  ((fn [extents] (= [[0.0 50.0] [0.0 105.0]] (mapv :y extents)))
   v106_l1028)))


(def
 v109_l1045
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv (fn [sub-plot] (:y-scale-domain (:opts sub-plot))))))


(deftest
 t110_l1050
 (is ((fn [domains] (= [[0.0 105.0] [0.0 105.0]] domains)) v109_l1045)))


(def
 v112_l1055
 (->>
  arranged-clinics
  pj/plan
  :sub-plots
  (mapv
   (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot))))))))


(deftest
 t113_l1060
 (is
  ((fn
    [domains]
    (and (apply = domains) (< 105.0 (second (first domains)))))
   v112_l1055)))


(def v115_l1096 (->> faceted-clinics pj/plan :panels (mapv :y-domain)))


(deftest
 t116_l1101
 (is
  ((fn
    [domains]
    (=
     domains
     (mapv
      (fn [sub-plot] (:y-domain (first (:panels (:plan sub-plot)))))
      (:sub-plots (pj/plan arranged-clinics)))))
   v115_l1096)))


(def
 v118_l1119
 (def
  series-by-key
  (->
   weekly-visits
   (pj/lay-line :week [:adults :children])
   (pj/facet :series))))


(def v119_l1124 series-by-key)


(deftest
 t120_l1126
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v119_l1124)))


(def
 v122_l1132
 (-> series-by-key pose/expand-facet-nodes (dissoc :data) kind/pprint))


(deftest
 t123_l1137
 (is
  ((fn
    [expanded]
    (and
     (= {:x :week} (:mapping expanded))
     (=
      [{:y :adults} {:y :children}]
      (mapv :mapping (mapcat :poses (:poses expanded))))))
   v122_l1132)))


(def
 v125_l1148
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
 t126_l1156
 (is
  ((fn
    [mappings]
    (=
     [{:x :adults, :y :adults}
      {:x nil, :y :adults}
      {:x :adults, :y :children}
      {:x nil, :y :children}]
     mappings))
   v125_l1148)))


(def
 v128_l1186
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
 t129_l1195
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
       [[:point :bar] [:point :bar]]
       (marks-per-cell
        (->
         weekly-visits
         (pj/pose {:x :week})
         pj/lay-point
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
   v128_l1186)))


(def v131_l1226 (->> series-by-key pj/draft :sub-drafts (mapv :share)))


(deftest
 t132_l1231
 (is
  ((fn [shares] (= [{:x {:group 0}} {:x {:group 0}}] shares))
   v131_l1226)))


(def
 v134_l1241
 (let
  [points (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
  (->>
   [[{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]
    [{:where {:drv "f"}} {:where {:drv "4"}}]]
   (mapv
    (fn*
     [p1__78861#]
     (pj/composite-plan? (pj/plan (pj/arrange points p1__78861#))))))))


(deftest
 t135_l1246
 (is ((fn [composite?] (= [false true] composite?)) v134_l1241)))


(def
 v137_l1265
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
 t138_l1311
 (is
  ((fn
    [table]
    (=
     (concat
      (repeat 3 "the call")
      (repeat 4 "pj/pose->draft")
      (repeat 3 "pj/draft->plan"))
     (map second (:row-vectors table))))
   v137_l1265)))


(def
 v140_l1328
 (kind/mermaid
  "\ngraph LR\n  A[\"Pose + draft\"] -->|plan| P[\"Plan\"]\n  P --> R[\"membrane + plot\"]\n  style A fill:#e8f5e9\n  style P fill:#fff3e0\n  style R fill:#e3f2fd\n"))


(def
 v142_l1421
 (kind/mermaid
  "\ngraph TD\n  API[\"api.clj\"] --> POSE[\"impl/pose.clj\"]\n  API --> RES[\"impl/resolve.clj\"]\n  API --> PL[\"impl/plan.clj\"]\n  API --> COMP[\"impl/compositor.clj\"]\n  POSE --> RES\n  COMP --> POSE\n  COMP --> PL\n  PL --> RES\n  PL --> STAT[\"impl/stat.clj\"]\n  PL --> SCALE[\"impl/scale.clj\"]\n  PL --> DEFAULTS[\"impl/defaults.clj\"]\n  PL --> PS[\"impl/plan_schema.clj\"]\n  PL --> LAYOUT[\"impl/layout.clj\"]\n  API --> FRAMES[\"impl/frames.clj\"]\n  API --> IMEM[\"impl/membrane.clj\"]\n  API --> RENDER[\"impl/render.clj\"]\n  RENDER --> SVG[\"render/svg.clj\"]\n  RENDER -.->|loaded on first use| BUFIMG[\"render/bufimg.clj\"]\n  SVG --> MEMBRANE[\"render/membrane.clj\"]\n  BUFIMG --> MEMBRANE\n  MEMBRANE --> IMEM\n  MEMBRANE --> FRAMES\n  MEMBRANE --> LAYOUT\n  MEMBRANE --> PANEL[\"render/panel.clj\"]\n  PANEL --> MARK[\"render/mark.clj\"]\n  PANEL --> SCALE\n  PANEL --> COORD[\"impl/coord.clj\"]\n  API --> RC[\"render/composite.clj\"]\n  RC --> MEMBRANE\n  RC --> IMEM\n  style API fill:#c8e6c9\n  style COMP fill:#d1c4e9\n  style PL fill:#d1c4e9\n  style SVG fill:#f8bbd0\n  style MEMBRANE fill:#f8bbd0\n  style RC fill:#f8bbd0\n"))
