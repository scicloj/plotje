;; # Wide and Long
;;
;; A table carrying several measures can be written two ways. **Wide**
;; gives each measure a column of its own. **Long** gives them one
;; value column and a key column naming which measure a row holds.
;; `tc/pivot->longer` and `tc/pivot->wider` move between the two and
;; lose nothing.
;;
;; This notebook proposes that Plotje's layout vocabulary has a
;; wide-side reading and a long-side reading of the same questions, and
;; that most of its constructs come in pairs across that line. The pairs
;; were built separately, and two of them have since been made one:
;; `pj/facet` reads a column's values or a list of columns, and
;; `:share-scales` is the one sharing option on both sides. The notebook
;; then shows the series, which is the wide-side reading of a grouped
;; layer, and the repairs that came with it.
;;
;; It is a design document rather than a chapter. The book teaches what
;; exists; this argues for what should, so it is not in
;; `notebooks/chapters.edn`. Every number below is measured here and
;; checked by a test that runs with the suite.

(ns wide-and-long
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; The registry a rule below is read from
   [scicloj.plotje.impl.defaults :as defaults]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   [clojure.string :as str]))

;; ## Measuring what is drawn
;;
;; A shape's own coordinates are pre-transform: a mark sits inside a
;; per-panel `<g transform="translate(...)">`, so its points are
;; measured from the panel rather than from the canvas. Accumulating
;; the translations is what makes two plots comparable.

(defn shape-boxes
  "Every polygon in a rendered tree, as `[left right]` along x in
   absolute canvas coordinates."
  [pose]
  (let [walk (fn walk [node dx]
               (cond
                 (and (vector? node) (keyword? (first node)))
                 (let [attrs (if (map? (second node)) (second node) {})
                       kids (if (map? (second node)) (drop 2 node) (rest node))
                       tx (or (when-let [t (:transform attrs)]
                                (when-let [[_ a] (re-find #"translate\(\s*([-\d.eE]+)" (str t))]
                                  (Double/parseDouble a)))
                              0.0)
                       own (when (and (= :polygon (first node)) (:points attrs))
                             (let [xs (->> (str/split (str/trim (str (:points attrs))) #"[\s,]+")
                                           (map #(Double/parseDouble %))
                                           (partition 2)
                                           (map first))]
                               [[(+ dx (apply min xs)) (+ dx (apply max xs))]]))]
                   (into (vec own) (mapcat #(walk % (+ dx tx))) kids))
                 (sequential? node) (into [] (mapcat #(walk % dx)) node)
                 :else []))]
    (vec (sort (walk (pj/plot pose {:width 600 :height 400}) 0.0)))))

;; Tables computed from the library are written out as Markdown, which
;; renders in any format this file is read in.

(defn md-table [headers rows]
  (kind/md
   (str/join "\n"
             (concat [(str "| " (str/join " | " headers) " |")
                      (str "|" (str/join "|" (repeat (count headers) ":--")) "|")]
                     (map (fn [row] (str "| " (str/join " | " (map str row)) " |")) rows)))))

;; ---

;; # The two shapes

(def sales-wide
  (tc/dataset {:quarter ["Q1" "Q2" "Q3" "Q4"]
               :revenue [120 150 140 190]
               :cost    [90 100 115 120]
               :tax     [18 24 21 30]}))

sales-wide

(def sales-long
  (tc/pivot->longer sales-wide #{:revenue :cost :tax}
                    {:target-columns :measure :value-column-name :value}))

sales-long

(kind/test-last
 [(fn [ds] (and (= 12 (tc/row-count ds))
                (= [:quarter :measure :value] (vec (tc/column-names ds)))))])

;; ## The pairs

;; Most of what decides where a mark goes has a reading on each side.

;; | the question | long side, from a column's **values** | wide side, from **column names** |
;; |:--|:--|:--|
;; | several measures shown together | `:color`, `:group` | a layer per column, or several columns in one slot (a series) |
;; | several measures in their own panels | `pj/facet` with a column | `pj/facet` with a map of columns, `pj/arrange` with columns as cells, `pj/cross-matrix` |
;; | whether those panels share a scale | `:share-scales` | `:share-scales` |
;; | how competitors share a place | `:position` | `:position` on a series; `:dodge` alone across layers |

;; Outside the line entirely: `:layout`, and `pj/arrange` given finished
;; poses, read no columns and no values. That is why arranging poses
;; works for either shape of data.
;;
;; And one construct on the wide side only, rightly: an interval's
;; `:x-min` and `:x-max`, and an errorbar's `:y-min` and `:y-max`. A
;; mark needs both ends of an interval at once, so a long form with a
;; bound column would have nothing to draw. One side only is not
;; automatically a gap.

;; ---

;; # What the pairing predicts

;; ## Where both sides exist, one construct serves both

;; The same information, drawn from each shape. From the wide table,
;; `pj/arrange` takes a pair of columns as a cell:

(def wide-cells
  (-> sales-wide
      (pj/arrange [[:quarter :revenue]
                   [:quarter :cost]
                   [:quarter :tax]])
      (pj/lay-point)))

wide-cells

(kind/pprint wide-cells)

;; From the wide table again, `pj/facet` takes a map of columns and draws
;; a panel for each column, one under the other:

(def wide-facet
  (-> sales-wide
      (pj/pose {:x :quarter})
      pj/lay-point
      (pj/facet {:y [:revenue :cost :tax]})))

wide-facet

(kind/pprint wide-facet)

(kind/test-last
 [(fn [pose]
    (let [s (pj/svg-summary pose)]
      (and (= {:y [:revenue :cost :tax]} (:facet pose))
           (= 3 (:panels s))
           (= 12 (:points s))
           (every? (set (:texts s)) ["revenue" "cost" "tax"]))))])

;; From the long table, `pj/facet` takes the key column and draws a panel
;; for each of its values:

(def long-facet
  (-> sales-long
      (pj/lay-point :quarter :value)
      (pj/facet :measure)))

long-facet

(kind/pprint long-facet)

(kind/test-last
 [(fn [_]
    (let [w (pj/svg-summary (-> sales-wide
                                (pj/arrange [[:quarter :revenue]
                                             [:quarter :cost]
                                             [:quarter :tax]])
                                (pj/lay-point)))
          l (pj/svg-summary (-> sales-long (pj/lay-point :quarter :value)
                                (pj/facet :measure)))]
      ;; Three panels and twelve points either way, and both label their
      ;; strips with the measure names.
      (and (= 3 (:panels w) (:panels l))
           (= 12 (:points w) (:points l))
           (every? (set (:texts w)) ["revenue" "cost" "tax"])
           (every? (set (:texts l)) ["revenue" "cost" "tax"]))))])

;; **A panel per column is faceting's wide-side twin**, and `pj/facet`
;; is the one function for both: a column splits the rows by the
;; column's values, and a map of columns gives each column a panel.

;; The second pair is one option as well. `:share-scales` names the axes
;; panels share on both sides, and the two sides start from opposite
;; defaults: the long side shares `:y` with nothing
;; written, and the wide side shares it when asked to put every column
;; together, `{:y :all}`:

(defn y-ticks [pose]
  (filterv #(re-matches #"\d+" %) (:texts (pj/svg-summary pose))))

(md-table
 ["shape" "nothing written" "`{:share-scales {:y :all}}`"]
 [["long side, `pj/facet`"
   (pr-str (y-ticks (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure))))
   (pr-str (y-ticks (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure)
                        (pj/options {:share-scales {:y :all}}))))]
  ["wide side, pairs grid"
   (pr-str (y-ticks (-> sales-wide
                        (pj/arrange [[:quarter :revenue]
                                     [:quarter :cost]
                                     [:quarter :tax]])
                        (pj/lay-point))))
   (pr-str (y-ticks (-> sales-wide
                        (pj/arrange [[:quarter :revenue]
                                     [:quarter :cost]
                                     [:quarter :tax]])
                        (pj/lay-point)
                        (pj/options {:share-scales {:y :all}}))))]])

(kind/test-last
 [(fn [_]
    (let [grid (fn [opts] (-> sales-wide
                              (pj/arrange [[:quarter :revenue]
                                           [:quarter :cost]
                                           [:quarter :tax]])
                              (pj/lay-point)
                              (pj/options opts)))
          facet (fn [opts] (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure)
                               (pj/options opts)))]
      ;; The long side already shares; the wide side shares when asked.
      (and (= (y-ticks (facet {})) (y-ticks (facet {:share-scales {:y :all}})))
           (not= (y-ticks (grid {})) (y-ticks (grid {:share-scales {:y :all}}))))))])

;; **The opposite defaults are right, which is the interesting part.**
;; Facet panels are one column split by a key, so their values are
;; commensurable by construction and sharing is safe. Pairs-grid panels
;; hold different columns, so sharing is a claim about the data rather
;; than a fact. One setting serves both sides; one default would break
;; whichever side lost.

;; ## What the wide side can and cannot do

;; A position adjustment divides a band among **labelled**
;; competitors. Two layers over wide data are labelled by the column
;; each draws: where no `:color` is specified, each layer takes a colour
;; and a legend entry naming its column, and that label is what the
;; dodge divides by:

(def dodged-layers
  (-> sales-wide
      (pj/lay-bar :quarter :revenue {:position :dodge})
      (pj/lay-bar :quarter :cost {:position :dodge})))

dodged-layers

(kind/pprint dodged-layers)

(->> (pj/plan (-> sales-wide
                  (pj/lay-bar :quarter :revenue {:position :dodge})
                  (pj/lay-bar :quarter :cost {:position :dodge})))
     :panels first :layers
     (mapv (fn [l] {:n-groups (:n-groups (:dodge-ctx l))
                    :labels (mapv :label (:groups l))})))

(kind/test-last
 [(fn [ls] (= [{:n-groups 2 :labels ["revenue"]}
               {:n-groups 2 :labels ["cost"]}] ls))])

;; `:stack` and `:fill` are a different matter, and the wide side
;; cannot do them. A dodge is an annotation: it records which slot of
;; a band each mark takes, and the slots are counted across every
;; layer that dodges, so two layers can share them. Stacking and
;; filling rewrite the values themselves, accumulating each group's
;; on top of the one before it -- and that accumulation runs within
;; one layer, over its own groups. Two layers have no group in common
;; to accumulate across, so each is piled on itself and nothing moves.
;;
;; The bar tops each adjustment produces, wide against long:

(kind/table
 {:column-names [:adjustment :a-layer-per-column :one-series]
  :row-vectors
  (let [tops (fn [fr] (->> (pj/plan fr) :panels first :layers
                           (mapcat :groups)
                           (mapcat (fn [g] (seq (:ys g))))
                           (mapv double)))]
    (vec (for [adj [:dodge :stack :fill]]
           [adj
            (pr-str (tops (-> sales-wide
                              (pj/lay-bar :quarter :revenue {:position adj})
                              (pj/lay-bar :quarter :cost {:position adj}))))
            (pr-str (tops (-> sales-wide
                              (pj/lay-bar :quarter [:revenue :cost]
                                          {:position adj}))))])))})

(kind/test-last
 [(fn [rows]
    (let [by (into {} (map (fn [[adj wide long]] [adj [wide long]])) (:row-vectors rows))]
      (and ;; A dodge leaves the values alone on both sides, so the two agree.
       (= (first (by :dodge)) (second (by :dodge)))
           ;; A stack accumulates on the long side and not on the wide one.
       (not= (first (by :stack)) (second (by :stack)))
           ;; A fill normalizes each wide layer against itself, so every
           ;; bar fills its band and the picture says nothing.
       (= "[1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0]" (first (by :fill))))))])

;; So the two sides differ in what an adjustment can reach, not in
;; whether the marks are labelled: **a dodge divides labelled
;; competitors wherever they are, and a stack needs their values in
;; one layer.**

;; ## The pivot is invertible on the data and not on the plot

;; The two shapes hold the same information and do not draw the same
;; pictures. Each can do something the other cannot.
;;
;; **Several layers over wide data can differ in kind** -- a bar with a
;; line across it, different stats, different options per layer:

(def bar-and-line
  (-> sales-wide
      (pj/lay-bar :quarter :revenue)
      (pj/lay-line :quarter :cost {:color "#e6550d"})))

bar-and-line

(kind/pprint bar-and-line)

;; **One grouped layer over long data can have its groups stacked on
;; each other** -- which the table above shows two layers cannot do:

(def stacked-long
  (-> sales-long
      (pj/lay-bar :quarter :value {:color :measure :position :stack})))

stacked-long

(kind/pprint stacked-long)

(kind/test-last
 [(fn [v]
    (and (= 12 (:polygons (pj/svg-summary v)))
         ;; the tallest stack is Q4: 190 + 120 + 30
         (== 340 (->> (pj/plan v) :panels first :layers first :groups
                      (mapcat :ys)
                      (reduce max)))))])

;; That is a trade, not a gap. The two spellings are two
;; representations, and a writer comparing measures against each other
;; needs the measures in one layer: the long table, or a series.

;; ---

;; # A series: the wide-side reading of a grouped layer

;; Several columns where one goes are read as several series: the
;; columns are pivoted, and the layer draws from the two the pivot
;; invents.

(def dodged-series
  (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))

dodged-series

(kind/test-last [(fn [v] (= 12 (:polygons (pj/svg-summary v))))])

;; The layer keeps the series it was given. The pivot happens when the
;; pose is drawn, so everything after it reads ordinary columns -- one
;; dataset, one mapping per aesthetic:

(kind/pprint dodged-series)

(kind/test-last [(fn [pose] (and (= [:revenue :cost :tax] (-> pose :layers first :mapping :y))
                                 (= [:quarter :value]
                                    ((juxt :x :y) (-> pose pj/draft :layers first)))))])

;; ## Every adjustment reaches them

;; The four below differ in one word. Read them against each other: the
;; data is the same in each.

(def series-identity
  (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity})))

series-identity

(kind/pprint series-identity)

(def series-dodge
  (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))

series-dodge

(kind/pprint series-dodge)

(def series-stack
  (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})))

series-stack

(kind/pprint series-stack)

(def series-fill
  (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill})))

series-fill

(kind/pprint series-fill)

(kind/test-last
 [(fn [_]
    (let [l (fn [pos] (-> (pj/plan (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax]
                                                              {:position pos})))
                          :panels first :layers first))]
      ;; Three labelled groups with a slot each.
      (and (= 3 (:n-groups (:dodge-ctx (l :dodge))))
           (= ["revenue" "cost" "tax"] (mapv :label (:groups (l :dodge))))
           (= [0 1 2] (mapv :dodge-idx (:groups (l :dodge)))))))])

;; A dodge divides the band the identity case gave one bar, and the
;; slots abut:

(md-table
 ["position" "the first band's bars"]
 (mapv (fn [pos] [(str pos) (pr-str (take 3 (shape-boxes
                                             (-> sales-wide
                                                 (pj/lay-bar :quarter [:revenue :cost :tax]
                                                             {:position pos})))))])
       [:identity :dodge]))

(kind/test-last
 [(fn [_]
    (let [[[a0 a1] [b0 b1] [c0 c1]]
          (take 3 (shape-boxes (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax]
                                                          {:position :dodge}))))
          [[w0 w1]] (shape-boxes (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax]
                                                            {:position :identity})))]
      ;; Three slots, each abutting the next, and the three together are
      ;; the band that :identity gave one bar.
      (and (== a1 b0) (== b1 c0) (== (- c1 a0) (- w1 w0)))))])

;; ## Naming

;; The pivot invents a key column, `:series`, and a value column,
;; `:value`, and neither name is drawn by default. The value axis is
;; titled by the columns of the series, and the legend has no title:

(-> sales-wide
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    pj/svg-summary
    :texts)

(kind/test-last
 [(fn [texts] (let [texts (set texts)]
                (and (contains? texts "revenue, cost, tax")
                     (not (contains? texts "series"))
                     (not (contains? texts "value")))))])

;; The written-out form names the key column with `:as`, and that name
;; titles the legend. `:y-label` sets the title of the value axis, as it
;; does on any axis:

(def named-series
  (-> sales-wide
      (pj/lay-bar :quarter {:series [:revenue :cost :tax] :as :measure}
                  {:position :dodge})
      (pj/options {:y-label "Euros"})))

named-series

(kind/pprint named-series)

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (and (contains? texts "measure")
                 (contains? texts "Euros")
                 (not (contains? texts "revenue, cost, tax"))
                 (not (contains? texts "series"))
                 (not (contains? texts "value")))))])

;; ## What is not a series

;; A series belongs on `:x` or on `:y`. The first row below is a series
;; on `:x`; a vector written anywhere else is not read as columns to
;; pivot:

(md-table
 ["written" "read as"]
 [["`(pj/lay-bar data [:revenue :cost :tax] :quarter)`"
   (str (:polygons (pj/svg-summary (-> sales-wide (pj/lay-bar [:revenue :cost :tax] :quarter))))
        " bars in "
        (:panels (pj/svg-summary (-> sales-wide (pj/lay-bar [:revenue :cost :tax] :quarter))))
        " panel -- a series on `:x`")]
  ["`{:stroke-dash [5 5]}`"
   (str (:panels (pj/svg-summary (-> sales-wide (pj/lay-line :quarter :revenue
                                                             {:stroke-dash [5 5]}))))
        " panel -- a dash pattern")]
  ["`{:group [:revenue :cost :tax]}`"
   (str (:panels (pj/svg-summary (-> sales-wide (pj/lay-point :quarter :revenue
                                                              {:group [:revenue :cost :tax]}))))
        " panel -- one compound key")]])

;; A vector of columns under `:group` is read as columns, but it is not a
;; series, and it belongs to a *different* duality:
;; a compound key relates to a single interaction column by uniting
;; columns, not by pivoting them. It is the only `:grouping` aesthetic
;; in the registry -- the one that splits the data and draws nothing of
;; its own:

(md-table ["role" "aesthetics"]
          (->> defaults/aesthetic-registry
               (mapv (fn [[k v]] [k (:role v)]))
               (group-by second)
               (sort-by key)
               (mapv (fn [[role ks]] [(str role) (pr-str (vec (sort (map first ks))))]))))

(kind/test-last
 [(fn [_] (= [:group]
             (->> defaults/aesthetic-registry
                  (filter (fn [[_ v]] (= :grouping (:role v))))
                  (mapv first))))])

;; ## What reports an error, and what the message says

(defn verdict [f]
  (try (str "draws -- " (:panels (pj/svg-summary (f))) " panel(s)")
       (catch Exception e (str/replace (ex-message e) #"\s+" " "))))

(md-table
 ["written" "result"]
 [["`{:color [:revenue :cost :tax]}`"
   (verdict #(-> sales-wide (pj/lay-point :quarter :revenue {:color [:revenue :cost :tax]})))]
  ["a column the data lacks"
   (verdict #(-> sales-wide (pj/lay-bar :quarter [:revenue :nope])))]
  ["one column, where several go"
   (verdict #(-> sales-wide (pj/lay-bar :quarter {:series [:revenue]})))]
  ["a mapping already naming a consumed column"
   (verdict #(-> sales-wide (pj/pose :quarter :revenue)
                 (pj/lay-bar :quarter [:revenue :cost :tax])))]
  ["on a composite pose"
   (verdict #(-> (pj/arrange [(pj/lay-point sales-wide :quarter :revenue)])
                 (pj/lay-bar :quarter [:revenue :cost :tax])))]])

;; The table is a rendered string, so the assertion below reads the
;; calls again rather than reading the table.

(kind/test-last
 [(fn [_]
    (every? (fn [f] (try (f) false (catch Exception _ true)))
            [#(-> sales-wide (pj/lay-point :quarter :revenue {:color [:revenue :cost :tax]}))
             #(-> sales-wide (pj/lay-bar :quarter [:revenue :nope]))
             #(-> sales-wide (pj/lay-bar :quarter {:series [:revenue]}))
             #(-> sales-wide (pj/pose :quarter :revenue)
                  (pj/lay-bar :quarter [:revenue :cost :tax]))
             #(-> (pj/arrange [(pj/lay-point sales-wide :quarter :revenue)])
                  (pj/lay-bar :quarter [:revenue :cost :tax]))]))])

;; ---

;; # How the readings compose

(def sales
  (tc/dataset {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"]
               :region  ["EU" "EU" "EU" "EU" "AS" "AS" "AS" "AS"]
               :channel ["web" "web" "shop" "shop" "web" "web" "shop" "shop"]
               :revenue [120 150 140 190 90 120 160 210]
               :cost    [90 100 115 120 70 85 110 130]
               :tax     [18 24 21 30 14 19 26 34]
               :units   [12 15 14 19 9 12 16 21]}))

;; One panel, three series. The band is divided between them:

(def regions-in-one-panel
  (-> sales (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})))

regions-in-one-panel

(kind/pprint regions-in-one-panel)

(kind/test-last [(fn [v] (= 24 (:polygons (pj/svg-summary v))))])

;; Two panels, three series in each. The canvas is divided by a column
;; and each panel's bands are divided between the measures:

(def series-by-region
  (-> sales
      (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
      (pj/facet :region)))

series-by-region

(kind/pprint series-by-region)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Four panels, three series in each, stacked rather than set side by
;; side:

(def series-by-region-and-channel
  (-> sales
      (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
      (pj/facet-grid :region :channel)))

series-by-region-and-channel

(kind/pprint series-by-region-and-channel)

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Two sub-plots, each of three series, sharing one `:y` scale:

(def regions-arranged
  (pj/arrange (vec (for [r ["EU" "AS"]]
                     (-> (tc/select-rows sales (fn [row] (= r (:region row))))
                         (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))
              {:share-scales #{:y} :align-panels true}))

regions-arranged

(kind/pprint regions-arranged)

(kind/test-last
 [(fn [v]
    (and (= 2 (:panels (pj/svg-summary v)))
         ;; one y extent in both sub-plots
         (apply = (map #(-> % :plan :panels first :y-domain)
                       (:sub-plots (pj/plan v))))))])

;; Sub-plots of faceted panels of series -- three levels of division,
;; and one sub-plot drawing something else entirely:

(def series-and-units
  (pj/arrange [(-> sales (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
                   (pj/facet :region))
               (-> sales (pj/lay-line :quarter :units) (pj/facet :region))]))

series-and-units

(kind/pprint series-and-units)

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 4 (:panels s)) (= 24 (:polygons s)) (= 2 (:lines s)))))])

;; ---

;; # Repairs that came with it

;; ## Layers on other columns share the panel, and say where they disagree

;; Two layers naming different columns used to get a panel each. The
;; layers of a pose now share its panel, and separate panels come from
;; `pj/facet` over the columns or from `pj/arrange`. Two numeric columns
;; of similar extent on one axis need no report: the axis covers both
;; columns and is titled by both, and where no `:color` is specified
;; each layer takes a colour and a legend entry naming its column.

(defn note-of
  "What a pose says when it is drawn."
  [pose]
  (with-out-str (pj/plot pose)))

(let [p (-> sales (pj/lay-point :quarter :revenue)
            (pj/lay-point :quarter :cost))]
  [(note-of p) (:panels (pj/svg-summary p))])

(kind/test-last [(fn [[out panels]] (and (= "" out) (= 1 panels)))])

(-> sales
    (pj/lay-point :quarter :revenue)
    (pj/lay-point :quarter :cost)
    pj/svg-summary
    (select-keys [:texts :colors]))

(kind/test-last
 [(fn [{:keys [texts colors]}]
    (and (contains? (set texts) "revenue, cost")
         (= 2 (count (disj colors "none")))))])

;; Where the columns differ so much that one of them takes less than a
;; twentieth of the axis, the draw prints a Note naming the columns and
;; the `pj/facet` call that gives each a panel. A margin, a fraction
;; between 0 and 1, beside the revenue it is a fraction of:

(note-of (-> sales
             (tc/map-columns :margin [:revenue :cost]
                             (fn [revenue cost] (/ (- revenue cost) (double revenue))))
             (pj/lay-point :quarter :revenue)
             (pj/lay-point :quarter :margin)))

(kind/test-last
 [(fn [out] (and (re-find #"Note: on :y one axis holds :revenue" out)
                 (re-find #":margin is drawn within a small part of it" out)
                 (re-find #"\(pj/facet pose \{:y \[:revenue :margin\]\}\)" out)))])

;; A count beside a column on `:y` can be drawn, but the axis then reads
;; in two units, and the draw says so:

(note-of (-> sales (pj/lay-histogram :revenue) (pj/lay-point :revenue :cost)))

(kind/test-last
 [(fn [out] (re-find #"Note: the layers of this pose share one panel, and on :y" out))])

;; A category beside a number on one axis cannot be placed at all, and
;; reports an error naming both routes to separate panels:

(try (pj/plot (-> sales (pj/lay-point :quarter :revenue)
                  (pj/lay-point :units :revenue)))
     (catch clojure.lang.ExceptionInfo e (ex-message e)))

(kind/test-last
 [(fn [msg] (and (re-find #"columns of different kinds" msg)
                 (re-find #"pj/arrange" msg)
                 (re-find #"pj/facet" msg)))])

;; ## Composites nest

;; `pose/compute-layout` recurses for `:horizontal` and `:vertical` at
;; any depth, and the cells render correctly. `pj/arrange` used to
;; report an error for a composite among its cells, naming a limitation
;; the renderer does not have -- and `pj/arrange` builds a composite of
;; composites itself, a column of rows.

(def nested-arrangement
  (pj/arrange [(pj/arrange [(pj/lay-point sales :quarter :revenue)])
               (pj/lay-point sales :quarter :cost)]))

nested-arrangement

(kind/pprint nested-arrangement)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A tree written out, three levels deep. A map written by hand is
;; drawn once `pj/pose` has read it, and is printed as the data it is:

(def written-tree
  {:opts {:width 800 :height 560}
   :layout {:direction :vertical}
   :poses [(pj/lay-point sales :quarter :revenue)
           {:layout {:direction :horizontal :weights [2 1]}
            :poses [(pj/lay-point sales :quarter :cost)
                    {:layout {:direction :vertical}
                     :poses [(pj/lay-point sales :quarter :units)
                             (pj/lay-bar sales :region)]}]}]})

(pj/pose written-tree)

(kind/pprint written-tree)

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## A fill normalizes every layer shape

;; `:fill` branched on a layer carrying `:categories`. A counting bar
;; has them. A bar with a value column, and every area, has `:groups`
;; instead and fell to the stacking branch, which never normalized --
;; while the y domain was set to zero-to-one regardless. So the axis
;; read 0 to 1, the legend listed both series, the polygon count was
;; right, and only the last series was visible.

(def valued {:k ["a" "a" "a" "b" "b" "b"]
             :v [30 20 10 45 15 20]
             :g ["p" "q" "r" "p" "q" "r"]})
(def over-t {:t [1 2 3 1 2 3 1 2 3]
             :v [1 2 3 3 2 1 2 2 2]
             :s ["A" "A" "A" "B" "B" "B" "C" "C" "C"]})

(def filled-bars
  (-> valued (pj/lay-bar :k :v {:color :g :position :fill})))

filled-bars

(kind/pprint filled-bars)

(def filled-areas
  (-> over-t (pj/lay-area :t :v {:color :s :position :fill})))

filled-areas

(kind/pprint filled-areas)

(kind/test-last
 [(fn [_]
    (let [tops (fn [pose]
                 (->> (:groups (-> (pj/plan pose) :panels first :layers first))
                      (mapv (fn [g] (if-let [c (:counts g)]
                                      (apply max (map (comp double :y1) c))
                                      (apply max (map double (:ys g))))))))]
      ;; Both reach exactly one, where they used to reach the raw
      ;; cumulative sum far above the panel.
      (and (== 1.0 (apply max (tops (-> valued (pj/lay-bar :k :v {:color :g
                                                                  :position :fill})))))
           (== 1.0 (apply max (tops (-> over-t (pj/lay-area :t :v {:color :s
                                                                   :position :fill}))))))))])

;; ## A stack keeps the order its axis carries

;; `stack-area-layer` sorted its x values, which on strings is
;; dictionary order, so a stacked area over month names drew a polygon
;; zig-zagging between them. A numerical axis is still sorted, since
;; nothing else says what order an area joins its points in.

(def months
  {:month ["Jan" "Feb" "Mar" "Apr" "May" "Jun"
           "Jan" "Feb" "Mar" "Apr" "May" "Jun"
           "Jan" "Feb" "Mar" "Apr" "May" "Jun"]
   :value [78 64 58 47 52 60 44 68 112 158 196 204
           31 28 34 22 19 17]
   :reading ["rain" "rain" "rain" "rain" "rain" "rain"
             "sun" "sun" "sun" "sun" "sun" "sun"
             "wind" "wind" "wind" "wind" "wind" "wind"]})

(def stacked-months
  (-> months (pj/lay-area :month :value {:color :reading :position :stack})))

stacked-months

(kind/pprint stacked-months)

(->> (pj/plan (-> months (pj/lay-area :month :value
                                      {:color :reading :position :stack})))
     :panels first :layers first :groups
     (mapv (fn [g] [(:label g) (vec (:xs g))])))

(kind/test-last
 [(fn [rows] (= ["Jan" "Feb" "Mar" "Apr" "May" "Jun"] (second (first rows))))])

;; ## A cell keeps the options it was given

;; `:theme` is read where the drawables are made, and a composite used
;; to hand every cell its own options there, so a per-cell theme was
;; accepted by `pj/options` and then had no effect.

(def themed-cell
  (pj/arrange [(-> (pj/lay-point sales :quarter :revenue)
                   (pj/options {:theme {:bg "#FFFFFF"}}))
               (pj/lay-point sales :quarter :cost)]
              {:width 700 :height 280}))

themed-cell

(kind/pprint themed-cell)

(kind/test-last
 [(fn [v]
    (let [fills (->> (tree-seq sequential? seq (pj/plot v))
                     (filter #(and (vector? %) (= :rect (first %)) (map? (second %))
                                   (number? (:width (second %)))
                                   (> (:width (second %)) 100)))
                     (keep #(:fill (second %)))
                     distinct vec)]
      (= ["rgb(255,255,255)" "rgb(232,232,232)"] fills)))])

;; ---

;; # What is left

;; **One correction to record.** An earlier draft listed a vector on
;; `:tooltip` as a defect, on the grounds that it drew with the tooltip
;; gone. That was a misreading: a tooltip takes hiccup, so a vector
;; there is markup rather than columns, and valid hiccup reports the
;; same empty `:tooltips` slot -- hiccup does not travel in it. There
;; is no defect, and `:tooltip` is left alone.

(def tooltip-slots
  (mapv (fn [[label m]]
          [label (->> (pj/plan (-> sales (pj/lay-point :quarter :revenue m)))
                      :panels first :layers first :groups
                      (mapv #(vec (take 2 (:tooltips %)))))])
        [["a column" {:tooltip :units}]
         ["valid hiccup" {:tooltip [:b "a note"]}]]))

tooltip-slots

(kind/test-last
 [(fn [rows]
    ;; A column populates the slot; hiccup does not, and that is how
    ;; hiccup works rather than a fault in it.
    (and (seq (first (second (first rows))))
         (empty? (first (second (second rows))))))])

;; **One decision taken since.** There is one scale-sharing option,
;; `:share-scales`, and its default follows what produced the panels: a
;; split by a column's values shares both axes, and panels of different
;; columns share only the axis that draws one column in all of them.
;;
;; **One disagreement that remains.** The two sides still differ on
;; whether a categorical axis can be named in `:share-scales`. Every
;; panel below draws `:quarter` on `:x`. The long side takes
;; `{:share-scales #{:x}}` and draws; the wide side reports an error for
;; the same option, although with nothing specified it draws the same
;; four quarters in every panel:

(mapv (fn [pose]
        (try (:panels (pj/svg-summary (pj/options pose {:share-scales #{:x}})))
             (catch clojure.lang.ExceptionInfo e (ex-message e))))
      [(-> sales-long
           (pj/lay-point :quarter :value)
           (pj/facet :measure))
       (-> sales-wide
           (pj/pose {:x :quarter})
           pj/lay-point
           (pj/facet {:y [:revenue :cost :tax]}))
       (-> sales-wide
           (pj/arrange [[:quarter :revenue]
                        [:quarter :cost]
                        [:quarter :tax]])
           pj/lay-point)])

(kind/test-last
 [(fn [[long-side wide-facet wide-arrange]]
    (and (= 3 long-side)
         (re-find #"column :quarter is categorical across all sharing cells" wide-facet)
         (= wide-facet wide-arrange)))])

;; **One decision this does not take.**
;;
;; - **A fourth `:layout` direction** for two cells sharing a canvas.
;;   The geometry is a few lines; what it needs is chrome -- a
;;   transparent cell background and an axis that can draw on the
;;   right. It should be called `:inset`, with `:insets` required.
;;   `:overlay` is a retired name for a layer option and should not
;;   return as a name for where a cell is drawn.

;; ## The test this frame gives
;;
;; For any new layout feature: **what is this on the other side of the
;; pivot?**
;;
;; - If there is a twin, build one thing rather than two. Faceting and
;;   scale sharing were each built twice, drifted, and had to be made
;;   one afterwards.
;; - If there is no twin, say why. The interval endpoints have none and
;;   need none.
;; - If the twin exists and is missing, ask whether it is missing for
;;   the label reason. Grouping and position adjustment both were, and
;;   one change answered both.
;;
;; Where the frame stops: it says nothing about `:layout`, or about
;; `pj/arrange` given finished poses, which read neither columns nor
;; values. That is the right silence -- those are geometry, and geometry
;; has no wide and no long.
