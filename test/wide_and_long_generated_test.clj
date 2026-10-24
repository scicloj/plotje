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
 v3_l42
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
            (map (fn* [p1__78677#] (Double/parseDouble p1__78677#)))
            (partition 2)
            (map first))]
          [[(+ dx (apply min xs)) (+ dx (apply max xs))]]))]
       (into
        (vec own)
        (mapcat (fn* [p1__78678#] (walk p1__78678# (+ dx tx))))
        kids))
      (sequential? node)
      (into [] (mapcat (fn* [p1__78679#] (walk p1__78679# dx))) node)
      :else
      []))]
   (vec (sort (walk (pj/plot pose {:width 600, :height 400}) 0.0))))))


(def
 v5_l69
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
 v7_l80
 (def
  sales-wide
  (tc/dataset
   {:quarter ["Q1" "Q2" "Q3" "Q4"],
    :revenue [120 150 140 190],
    :cost [90 100 115 120],
    :tax [18 24 21 30]})))


(def v8_l86 sales-wide)


(def
 v9_l88
 (def
  sales-long
  (tc/pivot->longer
   sales-wide
   #{:revenue :tax :cost}
   {:target-columns :measure, :value-column-name :value})))


(def v10_l92 sales-long)


(deftest
 t11_l94
 (is
  ((fn
    [ds]
    (and
     (= 12 (tc/row-count ds))
     (= [:quarter :measure :value] (vec (tc/column-names ds)))))
   v10_l92)))


(def
 v13_l128
 (def
  wide-cells
  (->
   sales-wide
   (pj/arrange [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
   (pj/lay-point))))


(def v14_l135 wide-cells)


(def v15_l137 (kind/pprint wide-cells))


(def
 v17_l142
 (def
  wide-facet
  (->
   sales-wide
   (pj/pose {:x :quarter})
   pj/lay-point
   (pj/facet {:y [:revenue :cost :tax]}))))


(def v18_l148 wide-facet)


(def v19_l150 (kind/pprint wide-facet))


(deftest
 t20_l152
 (is
  ((fn
    [pose]
    (let
     [s (pj/svg-summary pose)]
     (and
      (= {:y [:revenue :cost :tax]} (:facet pose))
      (= 3 (:panels s))
      (= 12 (:points s))
      (every? (set (:texts s)) ["revenue" "cost" "tax"]))))
   v19_l150)))


(def
 v22_l163
 (def
  long-facet
  (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure))))


(def v23_l168 long-facet)


(def v24_l170 (kind/pprint long-facet))


(deftest
 t25_l172
 (is
  ((fn
    [_]
    (let
     [w
      (pj/svg-summary
       (->
        sales-wide
        (pj/arrange
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
   v24_l170)))


(def
 v27_l198
 (defn
  y-ticks
  [pose]
  (filterv
   (fn* [p1__78680#] (re-matches #"\d+" p1__78680#))
   (:texts (pj/svg-summary pose)))))


(def
 v28_l201
 (md-table
  ["shape" "nothing written" "`{:share-scales {:y :all}}`"]
  [["long side, `pj/facet`"
    (pr-str
     (y-ticks
      (->
       sales-long
       (pj/lay-point :quarter :value)
       (pj/facet :measure))))
    (pr-str
     (y-ticks
      (->
       sales-long
       (pj/lay-point :quarter :value)
       (pj/facet :measure)
       (pj/options {:share-scales {:y :all}}))))]
   ["wide side, pairs grid"
    (pr-str
     (y-ticks
      (->
       sales-wide
       (pj/arrange
        [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
       (pj/lay-point))))
    (pr-str
     (y-ticks
      (->
       sales-wide
       (pj/arrange
        [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
       (pj/lay-point)
       (pj/options {:share-scales {:y :all}}))))]]))


(deftest
 t29_l220
 (is
  ((fn
    [_]
    (let
     [grid
      (fn
       [opts]
       (->
        sales-wide
        (pj/arrange
         [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
        (pj/lay-point)
        (pj/options opts)))
      facet
      (fn
       [opts]
       (->
        sales-long
        (pj/lay-point :quarter :value)
        (pj/facet :measure)
        (pj/options opts)))]
     (and
      (=
       (y-ticks (facet {}))
       (y-ticks (facet {:share-scales {:y :all}})))
      (not=
       (y-ticks (grid {}))
       (y-ticks (grid {:share-scales {:y :all}}))))))
   v28_l201)))


(def
 v31_l249
 (def
  dodged-layers
  (->
   sales-wide
   (pj/lay-bar :quarter :revenue {:position :dodge})
   (pj/lay-bar :quarter :cost {:position :dodge}))))


(def v32_l254 dodged-layers)


(def v33_l256 (kind/pprint dodged-layers))


(def
 v34_l258
 (->>
  (pj/plan
   (->
    sales-wide
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
 t35_l265
 (is
  ((fn
    [ls]
    (=
     [{:n-groups 2, :labels ["revenue"]}
      {:n-groups 2, :labels ["cost"]}]
     ls))
   v34_l258)))


(def
 v37_l280
 (kind/table
  {:column-names [:adjustment :a-layer-per-column :one-series],
   :row-vectors
   (let
    [tops
     (fn
      [fr]
      (->>
       (pj/plan fr)
       :panels
       first
       :layers
       (mapcat :groups)
       (mapcat (fn [g] (seq (:ys g))))
       (mapv double)))]
    (vec
     (for
      [adj [:dodge :stack :fill]]
      [adj
       (pr-str
        (tops
         (->
          sales-wide
          (pj/lay-bar :quarter :revenue {:position adj})
          (pj/lay-bar :quarter :cost {:position adj}))))
       (pr-str
        (tops
         (->
          sales-wide
          (pj/lay-bar
           :quarter
           [:revenue :cost]
           {:position adj}))))])))}))


(deftest
 t38_l296
 (is
  ((fn
    [rows]
    (let
     [by
      (into
       {}
       (map (fn [[adj wide long]] [adj [wide long]]))
       (:row-vectors rows))]
     (and
      (= (first (by :dodge)) (second (by :dodge)))
      (not= (first (by :stack)) (second (by :stack)))
      (= "[1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0]" (first (by :fill))))))
   v37_l280)))


(def
 v40_l320
 (def
  bar-and-line
  (->
   sales-wide
   (pj/lay-bar :quarter :revenue)
   (pj/lay-line :quarter :cost {:color "#e6550d"}))))


(def v41_l325 bar-and-line)


(def v42_l327 (kind/pprint bar-and-line))


(def
 v44_l332
 (def
  stacked-long
  (->
   sales-long
   (pj/lay-bar :quarter :value {:color :measure, :position :stack}))))


(def v45_l336 stacked-long)


(def v46_l338 (kind/pprint stacked-long))


(deftest
 t47_l340
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (==
      340
      (->>
       (pj/plan v)
       :panels
       first
       :layers
       first
       :groups
       (mapcat :ys)
       (reduce max)))))
   v46_l338)))


(def
 v49_l360
 (def
  dodged-series
  (->
   sales-wide
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))


(def v50_l363 dodged-series)


(deftest
 t51_l365
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v50_l363)))


(def v53_l371 (kind/pprint dodged-series))


(deftest
 t54_l373
 (is
  ((fn
    [pose]
    (and
     (= [:revenue :cost :tax] (-> pose :layers first :mapping :y))
     (=
      [:quarter :value]
      ((juxt :x :y) (-> pose pj/draft :layers first)))))
   v53_l371)))


(def
 v56_l382
 (def
  series-identity
  (->
   sales-wide
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity}))))


(def v57_l385 series-identity)


(def v58_l387 (kind/pprint series-identity))


(def
 v59_l389
 (def
  series-dodge
  (->
   sales-wide
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))


(def v60_l392 series-dodge)


(def v61_l394 (kind/pprint series-dodge))


(def
 v62_l396
 (def
  series-stack
  (->
   sales-wide
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))))


(def v63_l399 series-stack)


(def v64_l401 (kind/pprint series-stack))


(def
 v65_l403
 (def
  series-fill
  (->
   sales-wide
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill}))))


(def v66_l406 series-fill)


(def v67_l408 (kind/pprint series-fill))


(deftest
 t68_l410
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
   v67_l408)))


(def
 v70_l423
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
 t71_l431
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
   v70_l423)))


(def
 v73_l448
 (->
  sales-wide
  (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
  pj/svg-summary
  :texts))


(deftest
 t74_l453
 (is
  ((fn
    [texts]
    (let
     [texts (set texts)]
     (and
      (contains? texts "revenue, cost, tax")
      (not (contains? texts "series"))
      (not (contains? texts "value")))))
   v73_l448)))


(def
 v76_l463
 (def
  named-series
  (->
   sales-wide
   (pj/lay-bar
    :quarter
    {:series [:revenue :cost :tax], :as :measure}
    {:position :dodge})
   (pj/options {:y-label "Euros"}))))


(def v77_l469 named-series)


(def v78_l471 (kind/pprint named-series))


(deftest
 t79_l473
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "measure")
      (contains? texts "Euros")
      (not (contains? texts "revenue, cost, tax"))
      (not (contains? texts "series"))
      (not (contains? texts "value")))))
   v78_l471)))


(def
 v81_l487
 (md-table
  ["written" "read as"]
  [["`(pj/lay-bar data [:revenue :cost :tax] :quarter)`"
    (str
     (:polygons
      (pj/svg-summary
       (-> sales-wide (pj/lay-bar [:revenue :cost :tax] :quarter))))
     " bars in "
     (:panels
      (pj/svg-summary
       (-> sales-wide (pj/lay-bar [:revenue :cost :tax] :quarter))))
     " panel -- a series on `:x`")]
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
 v83_l510
 (md-table
  ["role" "aesthetics"]
  (->>
   defaults/aesthetic-registry
   (mapv (fn [[k v]] [k (:role v)]))
   (group-by second)
   (sort-by key)
   (mapv
    (fn
     [[role ks]]
     [(str role) (pr-str (vec (sort (map first ks))))])))))


(deftest
 t84_l517
 (is
  ((fn
    [_]
    (=
     [:group]
     (->>
      defaults/aesthetic-registry
      (filter (fn [[_ v]] (= :grouping (:role v))))
      (mapv first))))
   v83_l510)))


(def
 v86_l525
 (defn
  verdict
  [f]
  (try
   (str "draws -- " (:panels (pj/svg-summary (f))) " panel(s)")
   (catch Exception e (str/replace (ex-message e) #"\s+" " ")))))


(def
 v87_l529
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
 t89_l547
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
   v87_l529)))


(def
 v91_l562
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
 v93_l573
 (def
  regions-in-one-panel
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))


(def v94_l576 regions-in-one-panel)


(def v95_l578 (kind/pprint regions-in-one-panel))


(deftest
 t96_l580
 (is ((fn [v] (= 24 (:polygons (pj/svg-summary v)))) v95_l578)))


(def
 v98_l585
 (def
  series-by-region
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
   (pj/facet :region))))


(def v99_l590 series-by-region)


(def v100_l592 (kind/pprint series-by-region))


(deftest
 t101_l594
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v100_l592)))


(def
 v103_l599
 (def
  series-by-region-and-channel
  (->
   sales
   (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
   (pj/facet-grid :region :channel))))


(def v104_l604 series-by-region-and-channel)


(def v105_l606 (kind/pprint series-by-region-and-channel))


(deftest
 t106_l608
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v105_l606)))


(def
 v108_l612
 (def
  regions-arranged
  (pj/arrange
   (vec
    (for
     [r ["EU" "AS"]]
     (->
      (tc/select-rows sales (fn [row] (= r (:region row))))
      (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))
   {:share-scales #{:y}, :align-panels true})))


(def v109_l618 regions-arranged)


(def v110_l620 (kind/pprint regions-arranged))


(deftest
 t111_l622
 (is
  ((fn
    [v]
    (and
     (= 2 (:panels (pj/svg-summary v)))
     (apply
      =
      (map
       (fn* [p1__78681#] (-> p1__78681# :plan :panels first :y-domain))
       (:sub-plots (pj/plan v))))))
   v110_l620)))


(def
 v113_l632
 (def
  series-and-units
  (pj/arrange
   [(->
     sales
     (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
     (pj/facet :region))
    (-> sales (pj/lay-line :quarter :units) (pj/facet :region))])))


(def v114_l637 series-and-units)


(def v115_l639 (kind/pprint series-and-units))


(deftest
 t116_l641
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 24 (:polygons s)) (= 2 (:lines s)))))
   v115_l639)))


(def
 v118_l658
 (defn
  note-of
  "What a pose says when it is drawn."
  [pose]
  (with-out-str (pj/plot pose))))


(def
 v119_l663
 (let
  [p
   (->
    sales
    (pj/lay-point :quarter :revenue)
    (pj/lay-point :quarter :cost))]
  [(note-of p) (:panels (pj/svg-summary p))]))


(deftest
 t120_l667
 (is ((fn [[out panels]] (and (= "" out) (= 1 panels))) v119_l663)))


(def
 v121_l669
 (->
  sales
  (pj/lay-point :quarter :revenue)
  (pj/lay-point :quarter :cost)
  pj/svg-summary
  (select-keys [:texts :colors])))


(deftest
 t122_l675
 (is
  ((fn
    [{:keys [texts colors]}]
    (and
     (contains? (set texts) "revenue, cost")
     (= 2 (count (disj colors "none")))))
   v121_l669)))


(def
 v124_l685
 (note-of
  (->
   sales
   (tc/map-columns
    :margin
    [:revenue :cost]
    (fn [revenue cost] (/ (- revenue cost) (double revenue))))
   (pj/lay-point :quarter :revenue)
   (pj/lay-point :quarter :margin))))


(deftest
 t125_l691
 (is
  ((fn
    [out]
    (and
     (re-find #"Note: the :y axis holds :revenue" out)
     (re-find #":margin is drawn within a small part of it" out)
     (re-find #"\(pj/facet pose \{:y \[:revenue :margin\]\}\)" out)))
   v124_l685)))


(def
 v127_l699
 (note-of
  (-> sales (pj/lay-histogram :revenue) (pj/lay-point :revenue :cost))))


(deftest
 t128_l701
 (is
  ((fn
    [out]
    (re-find
     #"Note: the layers of this pose share one panel, and on :y"
     out))
   v127_l699)))


(def
 v130_l707
 (try
  (pj/plot
   (->
    sales
    (pj/lay-point :quarter :revenue)
    (pj/lay-point :units :revenue)))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t131_l711
 (is
  ((fn
    [msg]
    (and
     (re-find #"columns of different kinds" msg)
     (re-find #"pj/arrange" msg)
     (re-find #"pj/facet" msg)))
   v130_l707)))


(def
 v133_l724
 (def
  nested-arrangement
  (pj/arrange
   [(pj/arrange [(pj/lay-point sales :quarter :revenue)])
    (pj/lay-point sales :quarter :cost)])))


(def v134_l728 nested-arrangement)


(def v135_l730 (kind/pprint nested-arrangement))


(deftest
 t136_l732
 (is ((fn [v] (= 2 (:panels (pj/svg-summary v)))) v135_l730)))


(def
 v138_l737
 (def
  written-tree
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
        (pj/lay-bar sales :region)]}]}]}))


(def v139_l747 (pj/pose written-tree))


(def v140_l749 (kind/pprint written-tree))


(deftest
 t141_l751
 (is ((fn [v] (= 4 (:panels (pj/svg-summary v)))) v140_l749)))


(def
 v143_l762
 (def
  valued
  {:k ["a" "a" "a" "b" "b" "b"],
   :v [30 20 10 45 15 20],
   :g ["p" "q" "r" "p" "q" "r"]}))


(def
 v144_l765
 (def
  over-t
  {:t [1 2 3 1 2 3 1 2 3],
   :v [1 2 3 3 2 1 2 2 2],
   :s ["A" "A" "A" "B" "B" "B" "C" "C" "C"]}))


(def
 v145_l769
 (def
  filled-bars
  (-> valued (pj/lay-bar :k :v {:color :g, :position :fill}))))


(def v146_l772 filled-bars)


(def v147_l774 (kind/pprint filled-bars))


(def
 v148_l776
 (def
  filled-areas
  (-> over-t (pj/lay-area :t :v {:color :s, :position :fill}))))


(def v149_l779 filled-areas)


(def v150_l781 (kind/pprint filled-areas))


(deftest
 t151_l783
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
   v150_l781)))


(def
 v153_l804
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
 v154_l814
 (def
  stacked-months
  (->
   months
   (pj/lay-area :month :value {:color :reading, :position :stack}))))


(def v155_l817 stacked-months)


(def v156_l819 (kind/pprint stacked-months))


(def
 v157_l821
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
 t158_l826
 (is
  ((fn
    [rows]
    (= ["Jan" "Feb" "Mar" "Apr" "May" "Jun"] (second (first rows))))
   v157_l821)))


(def
 v160_l835
 (def
  themed-cell
  (pj/arrange
   [(->
     (pj/lay-point sales :quarter :revenue)
     (pj/options {:theme {:bg "#FFFFFF"}}))
    (pj/lay-point sales :quarter :cost)]
   {:width 700, :height 280})))


(def v161_l841 themed-cell)


(def v162_l843 (kind/pprint themed-cell))


(deftest
 t163_l845
 (is
  ((fn
    [v]
    (let
     [fills
      (->>
       (tree-seq sequential? seq (pj/plot v))
       (filter
        (fn*
         [p1__78682#]
         (and
          (vector? p1__78682#)
          (= :rect (first p1__78682#))
          (map? (second p1__78682#))
          (number? (:width (second p1__78682#)))
          (> (:width (second p1__78682#)) 100))))
       (keep (fn* [p1__78683#] (:fill (second p1__78683#))))
       distinct
       vec)]
     (= ["rgb(255,255,255)" "rgb(232,232,232)"] fills)))
   v162_l843)))


(def
 v165_l866
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
       (fn* [p1__78684#] (vec (take 2 (:tooltips p1__78684#))))))])
   [["a column" {:tooltip :units}]
    ["valid hiccup" {:tooltip [:b "a note"]}]])))


(def v166_l874 tooltip-slots)


(deftest
 t167_l876
 (is
  ((fn
    [rows]
    (and
     (seq (first (second (first rows))))
     (empty? (first (second (second rows))))))
   v166_l874)))


(def
 v169_l893
 (mapv
  (fn
   [pose]
   (try
    (:panels (pj/svg-summary (pj/options pose {:share-scales #{:x}})))
    (catch clojure.lang.ExceptionInfo e (ex-message e))))
  [(-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure))
   (->
    sales-wide
    (pj/pose {:x :quarter})
    pj/lay-point
    (pj/facet {:y [:revenue :cost :tax]}))
   (->
    sales-wide
    (pj/arrange [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
    pj/lay-point)]))


(deftest
 t170_l909
 (is
  ((fn
    [[long-side wide-facet wide-arrange]]
    (= 3 long-side wide-facet wide-arrange))
   v169_l893)))
