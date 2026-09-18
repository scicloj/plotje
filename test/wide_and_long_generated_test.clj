(ns
 wide-and-long-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.impl.defaults :as defaults]
  [tablecloth.api :as tc]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def
 v3_l40
 (defn
  shape-boxes
  "Every polygon in a rendered tree, as `[left right]` along x in\n   absolute canvas coordinates."
  [pose]
  (let
   [walk
    (fn
     walk
     [node dx]
     (cond
      (and (vector? node) (keyword? (first node)))
      (let
       [attrs
        (if (map? (second node)) (second node) {})
        kids
        (if (map? (second node)) (drop 2 node) (rest node))
        tx
        (or
         (when-let
          [t (:transform attrs)]
          (when-let
           [[_ a] (re-find #"translate\(\s*([-\d.eE]+)" (str t))]
           (Double/parseDouble a)))
         0.0)
        own
        (when
         (and (= :polygon (first node)) (:points attrs))
         (let
          [xs
           (->>
            (str/split (str/trim (str (:points attrs))) #"[\s,]+")
            (map (fn* [p1__11193#] (Double/parseDouble p1__11193#)))
            (partition 2)
            (map first))]
          [[(+ dx (apply min xs)) (+ dx (apply max xs))]]))]
       (into
        (vec own)
        (mapcat (fn* [p1__11194#] (walk p1__11194# (+ dx tx))))
        kids))
      (sequential? node)
      (into [] (mapcat (fn* [p1__11195#] (walk p1__11195# dx))) node)
      :else
      []))]
   (vec (sort (walk (pj/plot pose {:width 600, :height 400}) 0.0))))))


(def
 v5_l67
 (defn
  md-table
  [headers rows]
  (kind/md
   (str/join
    "\n"
    (concat
     [(str "| " (str/join " | " headers) " |")
      (str "|" (str/join "|" (repeat (count headers) ":--")) "|")]
     (map
      (fn [row] (str "| " (str/join " | " (map str row)) " |"))
      rows))))))


(def
 v7_l78
 (def
  sales-wide
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4"],
    :revenue [120 150 140 190],
    :cost [90 100 115 120],
    :tax [18 24 21 30]})))


(def v8_l84 sales-wide)


(def
 v9_l86
 (def
  sales-long
  (tc/pivot->longer
   sales-wide
   #{:revenue :tax :cost}
   {:target-columns :measure, :value-column-name :value})))


(def v10_l90 sales-long)


(deftest
 t11_l92
 (is
  ((fn
    [ds]
    (and
     (= 12 (tc/row-count ds))
     (= [:quarter :measure :value] (vec (tc/column-names ds)))))
   v10_l90)))


(def
 v13_l126
 (->
  sales-wide
  (pj/pose [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
  (pj/lay-point)))


(def
 v14_l130
 (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure)))


(deftest
 t15_l132
 (is
  ((fn
    [_]
    (let
     [w
      (pj/svg-summary
       (->
        sales-wide
        (pj/pose
         [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
        (pj/lay-point)))
      l
      (pj/svg-summary
       (->
        sales-long
        (pj/lay-point :quarter :value)
        (pj/facet :measure)))]
     (and
      (= 3 (:panels w) (:panels l))
      (= 12 (:points w) (:points l))
      (every? (set (:texts w)) ["revenue" "cost" "tax"])
      (every? (set (:texts l)) ["revenue" "cost" "tax"]))))
   v14_l130)))


(def
 v17_l154
 (defn
  y-ticks
  [pose]
  (filterv
   (fn* [p1__11196#] (re-matches #"\d+" p1__11196#))
   (:texts (pj/svg-summary pose)))))


(def
 v18_l157
 (md-table
  ["shape" ":scales :free" ":scales :shared"]
  [["long side, `pj/facet`"
    (pr-str
     (y-ticks
      (->
       sales-long
       (pj/lay-point :quarter :value)
       (pj/facet :measure)
       (pj/options {:scales :free}))))
    (pr-str
     (y-ticks
      (->
       sales-long
       (pj/lay-point :quarter :value)
       (pj/facet :measure)
       (pj/options {:scales :shared}))))]
   ["wide side, pairs grid"
    (pr-str
     (y-ticks
      (->
       sales-wide
       (pj/pose [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
       (pj/lay-point)
       (pj/options {:scales :free}))))
    (pr-str
     (y-ticks
      (->
       sales-wide
       (pj/pose [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
       (pj/lay-point)
       (pj/options {:scales :shared}))))]]))


(deftest
 t19_l172
 (is
  ((fn
    [_]
    (let
     [grid
      (fn
       [s]
       (->
        sales-wide
        (pj/pose
         [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
        (pj/lay-point)
        (pj/options {:scales s})))
      facet
      (fn
       [s]
       (->
        sales-long
        (pj/lay-point :quarter :value)
        (pj/facet :measure)
        (pj/options {:scales s})))]
     (and
      (not= (y-ticks (facet :free)) (y-ticks (facet :shared)))
      (= (y-ticks (grid :free)) (y-ticks (grid :shared))))))
   v18_l157)))


(def
 v21_l196
 (->
  sales-wide
  pj/overlay
  (pj/lay-bar :quarter :revenue {:position :dodge})
  (pj/lay-bar :quarter :cost {:position :dodge})))


(def
 v22_l201
 (->>
  (pj/plan
   (->
    sales-wide
    pj/overlay
    (pj/lay-bar :quarter :revenue {:position :dodge})
    (pj/lay-bar :quarter :cost {:position :dodge})))
  :panels
  first
  :layers
  (mapv
   (fn
    [l]
    {:n-groups (:n-groups (:dodge-ctx l)),
     :labels (mapv :label (:groups l))}))))


(deftest
 t23_l208
 (is
  ((fn
    [ls]
    (= [{:n-groups 1, :labels [""]} {:n-groups 1, :labels [""]}] ls))
   v22_l201)))


(def
 v25_l226
 (->
  sales-wide
  pj/overlay
  (pj/lay-bar :quarter :revenue)
  (pj/lay-line :quarter :cost {:color "#e6550d"})))


(def
 v27_l235
 (->
  sales-long
  (pj/lay-bar :quarter :value {:color :measure, :position :dodge})))


(deftest
 t28_l237
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v27_l235)))


(def
 v30_l252
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t31_l254
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v30_l252)))


(def
 v33_l259
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax])
  kind/pprint))


(deftest
 t34_l261
 (is
  ((fn [pose] (= {:x :quarter, :y :value} (:mapping pose))) v33_l259)))


(def
 v36_l268
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))


(def
 v37_l270
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(def
 v38_l272
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))


(def
 v39_l274
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))


(deftest
 t40_l276
 (is
  ((fn
    [_]
    (let
     [l
      (fn
       [pos]
       (->
        (pj/plan
         (->
          sales-wide
          (pj/lay-bar :quarter [:revenue :cost :tax] {:position pos})))
        :panels
        first
        :layers
        first))]
     (and
      (= 3 (:n-groups (:dodge-ctx (l :dodge))))
      (= ["revenue" "cost" "tax"] (mapv :label (:groups (l :dodge))))
      (= [0 1 2] (mapv :dodge-idx (:groups (l :dodge)))))))
   v39_l274)))


(def
 v42_l291
 (md-table
  ["position" "the first band's bars"]
  (mapv
   (fn
    [pos]
    [(str pos)
     (pr-str
      (take
       3
       (shape-boxes
        (->
         sales-wide
         (pj/lay-bar
          :quarter
          [:revenue :cost :tax]
          {:position pos})))))])
   [:identity :dodge])))


(deftest
 t43_l299
 (is
  ((fn
    [_]
    (let
     [[[a0 a1] [b0 b1] [c0 c1]]
      (take
       3
       (shape-boxes
        (->
         sales-wide
         (pj/lay-bar
          :quarter
          [:revenue :cost :tax]
          {:position :dodge}))))
      [[w0 w1]]
      (shape-boxes
       (->
        sales-wide
        (pj/lay-bar
         :quarter
         [:revenue :cost :tax]
         {:position :identity})))]
     (and (== a1 b0) (== b1 c0) (== (- c1 a0) (- w1 w0)))))
   v42_l291)))


(def
 v45_l316
 (->
  sales-wide
  (pj/lay-bar
   :quarter
   {:series [:revenue :cost :tax], :as :measure}
   {:position :dodge})
  (pj/options {:y-label "Euros"})))


(deftest
 t46_l321
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "measure")
      (contains? texts "Euros")
      (not (contains? texts "series"))
      (not (contains? texts "value")))))
   v45_l316)))


(def
 v48_l334
 (md-table
  ["written" "read as"]
  [["`(pj/lay-point data [[:q :revenue] [:q :cost]])`"
    (str
     (:panels
      (pj/svg-summary
       (->
        sales-wide
        (pj/lay-point [[:quarter :revenue] [:quarter :cost]]))))
     " panels")]
   ["`(pj/lay-point data [:revenue :cost :tax] :quarter)`"
    (str
     (:polygons
      (pj/svg-summary
       (-> sales-wide (pj/lay-bar [:revenue :cost :tax] :quarter))))
     " bars in 1 panel")]
   ["`{:stroke-dash [5 5]}`"
    (str
     (:panels
      (pj/svg-summary
       (->
        sales-wide
        (pj/lay-line :quarter :revenue {:stroke-dash [5 5]}))))
     " panel -- a dash pattern")]
   ["`{:group [:revenue :cost :tax]}`"
    (str
     (:panels
      (pj/svg-summary
       (->
        sales-wide
        (pj/lay-point
         :quarter
         :revenue
         {:group [:revenue :cost :tax]}))))
     " panel -- one compound key")]]))


(def
 v50_l358
 (md-table
  ["category" "aesthetics"]
  (->>
   defaults/aesthetic-registry
   (mapv (fn [[k v]] [k (:category v)]))
   (group-by second)
   (sort-by key)
   (mapv
    (fn [[cat ks]] [(str cat) (pr-str (vec (sort (map first ks))))])))))


(deftest
 t51_l365
 (is
  ((fn
    [_]
    (=
     [:group]
     (->>
      defaults/aesthetic-registry
      (filter (fn [[_ v]] (= :grouping (:category v))))
      (mapv first))))
   v50_l358)))


(def
 v53_l373
 (defn
  verdict
  [f]
  (try
   (str "draws -- " (:panels (pj/svg-summary (f))) " panel(s)")
   (catch Exception e (str/replace (ex-message e) #"\s+" " ")))))


(def
 v54_l377
 (md-table
  ["written" "result"]
  [["`{:color [:revenue :cost :tax]}`"
    (verdict
     (fn*
      []
      (->
       sales-wide
       (pj/lay-point
        :quarter
        :revenue
        {:color [:revenue :cost :tax]}))))]
   ["a column the data lacks"
    (verdict
     (fn* [] (-> sales-wide (pj/lay-bar :quarter [:revenue :nope]))))]
   ["one column, where several go"
    (verdict
     (fn*
      []
      (-> sales-wide (pj/lay-bar :quarter {:series [:revenue]}))))]
   ["a mapping already naming a consumed column"
    (verdict
     (fn*
      []
      (->
       sales-wide
       (pj/pose :quarter :revenue)
       (pj/lay-bar :quarter [:revenue :cost :tax]))))]
   ["on a composite pose"
    (verdict
     (fn*
      []
      (->
       (pj/arrange [(pj/lay-point sales-wide :quarter :revenue)])
       (pj/lay-bar :quarter [:revenue :cost :tax]))))]]))


(deftest
 t56_l395
 (is
  ((fn
    [_]
    (every?
     (fn [f] (try (f) false (catch Exception _ true)))
     [(fn*
       []
       (->
        sales-wide
        (pj/lay-point
         :quarter
         :revenue
         {:color [:revenue :cost :tax]})))
      (fn* [] (-> sales-wide (pj/lay-bar :quarter [:revenue :nope])))
      (fn*
       []
       (-> sales-wide (pj/lay-bar :quarter {:series [:revenue]})))
      (fn*
       []
       (->
        sales-wide
        (pj/pose :quarter :revenue)
        (pj/lay-bar :quarter [:revenue :cost :tax])))
      (fn*
       []
       (->
        (pj/arrange [(pj/lay-point sales-wide :quarter :revenue)])
        (pj/lay-bar :quarter [:revenue :cost :tax])))]))
   v54_l377)))


(def
 v58_l410
 (def
  sales
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"],
    :region ["EU" "EU" "EU" "EU" "AS" "AS" "AS" "AS"],
    :channel ["web" "web" "shop" "shop" "web" "web" "shop" "shop"],
    :revenue [120 150 140 190 90 120 160 210],
    :cost [90 100 115 120 70 85 110 130],
    :tax [18 24 21 30 14 19 26 34],
    :units [12 15 14 19 9 12 16 21]})))


(def
 v60_l421
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))


(deftest
 t61_l423
 (is ((fn [v] (= 24 (:polygons (pj/svg-summary v)))) v60_l421)))


(def
 v63_l428
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  (pj/facet :region)))


(deftest
 t64_l432
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v63_l428)))


(def
 v66_l436
 (->
  sales
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
  (pj/facet-grid :region :channel)))


(deftest
 t67_l440
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v66_l436)))


(def
 v69_l444
 (pj/arrange
  (vec
   (for
    [r ["EU" "AS"]]
    (->
     (tc/select-rows sales (fn [row] (= r (:region row))))
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))
  {:share-scales #{:y}, :align-panels true}))


(deftest
 t70_l449
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v69_l444)))


(def
 v72_l454
 (pj/arrange
  [(->
    sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))
   (-> sales (pj/lay-line :quarter :units) (pj/facet :region))]))


(deftest
 t73_l458
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 24 (:polygons s)) (= 2 (:lines s)))))
   v72_l454)))


(def
 v75_l467
 (try
  (->
   sales
   (pj/pose [[:revenue :units] [:cost :units] [:tax :units]])
   (pj/lay-point :revenue [:units :cost :tax]))
  (catch Exception e (ex-message e))))


(deftest
 t76_l473
 (is
  ((fn [m] (and (string? m) (re-find #"composite pose" m))) v75_l467)))


(def
 v78_l499
 (defn
  note-of
  "What a pose says when it is drawn."
  [pose]
  (with-out-str (pj/plot pose))))


(def
 v79_l504
 (note-of
  (->
   sales
   (pj/lay-point :quarter :revenue)
   (pj/lay-point :quarter :cost))))


(deftest
 t80_l507
 (is
  ((fn
    [out]
    (and
     (re-find #"panel of its own" out)
     (re-find #"pj/overlay" out)
     (re-find #"\[:revenue :cost\]" out)))
   v79_l504)))


(def
 v82_l516
 [(note-of
   (->
    sales
    (pj/lay-point :quarter :revenue)
    (pj/lay-line :quarter :revenue)))
  (note-of
   (->
    sales
    pj/overlay
    (pj/lay-point :quarter :revenue)
    (pj/lay-point :quarter :cost)))
  (note-of
   (->
    sales
    (pj/lay-point :quarter :revenue)
    (pj/lay-point :quarter :cost)
    pj/overlay))])


(deftest t83_l524 (is ((fn [outs] (= ["" "" ""] outs)) v82_l516)))


(def
 v85_l532
 [(note-of
   (->
    {:fitted [1 2 3], :residual [1 2 3]}
    (pj/lay-point :fitted :residual)
    (pj/lay-point
     :x
     :y
     {:data (tc/dataset {:x [1 2 3], :y [1 2 3]})})))
  (note-of
   (->
    (assoc sales :units [3 4 5 6])
    (pj/lay-point :revenue :cost)
    (pj/lay-point :quarter :units)))])


(deftest
 t86_l541
 (is
  ((fn
    [outs]
    (every?
     (fn
      [out]
      (and
       (re-find #"panel of its own" out)
       (re-find #"pj/overlay" out)
       (not (re-find #"series" out))))
     outs))
   v85_l532)))


(def
 v88_l555
 (pj/arrange
  [(pj/arrange [(pj/lay-point sales :quarter :revenue)])
   (pj/lay-point sales :quarter :cost)]))


(deftest
 t89_l558
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v88_l555)))


(def
 v91_l562
 {:opts {:width 800, :height 560},
  :layout {:direction :vertical},
  :poses
  [(pj/lay-point sales :quarter :revenue)
   {:layout {:direction :horizontal, :weights [2 1]},
    :poses
    [(pj/lay-point sales :quarter :cost)
     {:layout {:direction :vertical},
      :poses
      [(pj/lay-point sales :quarter :units)
       (pj/lay-bar sales :region)]}]}]})


(deftest
 t92_l571
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v91_l562)))


(def
 v94_l582
 (def
  valued
  {:k ["a" "a" "a" "b" "b" "b"],
   :v [30 20 10 45 15 20],
   :g ["p" "q" "r" "p" "q" "r"]}))


(def
 v95_l585
 (def
  over-t
  {:t [1 2 3 1 2 3 1 2 3],
   :v [1 2 3 3 2 1 2 2 2],
   :s ["A" "A" "A" "B" "B" "B" "C" "C" "C"]}))


(def
 v96_l589
 (-> valued (pj/lay-bar :k :v {:color :g, :position :fill})))


(def
 v97_l591
 (-> over-t (pj/lay-area :t :v {:color :s, :position :fill})))


(deftest
 t98_l593
 (is
  ((fn
    [_]
    (let
     [tops
      (fn
       [pose]
       (->>
        (:groups (-> (pj/plan pose) :panels first :layers first))
        (mapv
         (fn
          [g]
          (if-let
           [c (:counts g)]
           (apply max (map (comp double :y1) c))
           (apply max (map double (:ys g))))))))]
     (and
      (==
       1.0
       (apply
        max
        (tops
         (-> valued (pj/lay-bar :k :v {:color :g, :position :fill})))))
      (==
       1.0
       (apply
        max
        (tops
         (->
          over-t
          (pj/lay-area :t :v {:color :s, :position :fill}))))))))
   v97_l591)))


(def
 v100_l614
 (def
  months
  {:month
   ["Jan"
    "Feb"
    "Mar"
    "Apr"
    "May"
    "Jun"
    "Jan"
    "Feb"
    "Mar"
    "Apr"
    "May"
    "Jun"
    "Jan"
    "Feb"
    "Mar"
    "Apr"
    "May"
    "Jun"],
   :value [78 64 58 47 52 60 44 68 112 158 196 204 31 28 34 22 19 17],
   :reading
   ["rain"
    "rain"
    "rain"
    "rain"
    "rain"
    "rain"
    "sun"
    "sun"
    "sun"
    "sun"
    "sun"
    "sun"
    "wind"
    "wind"
    "wind"
    "wind"
    "wind"
    "wind"]}))


(def
 v101_l624
 (->
  months
  (pj/lay-area :month :value {:color :reading, :position :stack})))


(def
 v102_l626
 (->>
  (pj/plan
   (->
    months
    (pj/lay-area :month :value {:color :reading, :position :stack})))
  :panels
  first
  :layers
  first
  :groups
  (mapv (fn [g] [(:label g) (vec (:xs g))]))))


(deftest
 t103_l631
 (is
  ((fn
    [rows]
    (= ["Jan" "Feb" "Mar" "Apr" "May" "Jun"] (second (first rows))))
   v102_l626)))


(def
 v105_l640
 (pj/arrange
  [(->
    (pj/lay-point sales :quarter :revenue)
    (pj/options {:theme {:bg "#FFFFFF"}}))
   (pj/lay-point sales :quarter :cost)]
  {:width 700, :height 280}))


(deftest
 t106_l645
 (is
  ((fn
    [v]
    (let
     [fills
      (->>
       (tree-seq sequential? seq (pj/plot v))
       (filter
        (fn*
         [p1__11197#]
         (and
          (vector? p1__11197#)
          (= :rect (first p1__11197#))
          (map? (second p1__11197#))
          (number? (:width (second p1__11197#)))
          (> (:width (second p1__11197#)) 100))))
       (keep (fn* [p1__11198#] (:fill (second p1__11198#))))
       distinct
       vec)]
     (= ["rgb(255,255,255)" "rgb(232,232,232)"] fills)))
   v105_l640)))


(def
 v108_l666
 (def
  tooltip-slots
  (mapv
   (fn
    [[label m]]
    [label
     (->>
      (pj/plan (-> sales (pj/lay-point :quarter :revenue m)))
      :panels
      first
      :layers
      first
      :groups
      (mapv
       (fn* [p1__11199#] (vec (take 2 (:tooltips p1__11199#))))))])
   [["a column" {:tooltip :units}]
    ["valid hiccup" {:tooltip [:b "a note"]}]])))


(def v109_l674 tooltip-slots)


(deftest
 t110_l676
 (is
  ((fn
    [rows]
    (and
     (seq (first (second (first rows))))
     (empty? (first (second (second rows))))))
   v109_l674)))
