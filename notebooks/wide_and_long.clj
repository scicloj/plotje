;; # Wide and Long
;;
;; A table carrying several measures can be written two ways. **Wide**
;; gives each measure a column of its own. **Long** gives them one
;; value column and a key column naming which measure a row holds.
;; `tc/pivot->longer` and `tc/pivot->wider` move between the two and
;; lose nothing.
;;
;; This notebook proposes that Plotje's layout vocabulary has a
;; wide-side reading and a long-side reading of the same questions,
;; that most of its constructs come in pairs across that line, and that
;; the pairs were built separately and drifted. It then shows the
;; wide-side reading this branch adds, and the four repairs that came
;; with it.
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
;; | several measures shown together | `:color`, `:group` | several columns in one slot |
;; | several measures in their own panels | `pj/facet` | a vector of pairs, `pj/cross` |
;; | whether those panels share a scale | `:scales` | `:share-scales` |
;; | how competitors share a place | `:position` | *absent* |

;; Outside the line entirely: `pj/arrange`, `:layout` and `pj/marginal`
;; read no columns and no values. That is why `pj/arrange` is the
;; universal escape hatch -- it works on either side because it never
;; looks at the data.
;;
;; And one construct on the wide side only, rightly: an interval's
;; `:x-min` and `:x-max`, and an errorbar's `:y-min` and `:y-max`. A
;; mark needs both ends of an interval at once, so a long form with a
;; bound column would have nothing to draw. One side only is not
;; automatically a gap.

;; ---

;; # What the pairing predicts

;; ## Where both sides exist, they diverged

;; The same information, drawn from each shape:

(-> sales-wide
    (pj/pose [[:quarter :revenue] [:quarter :cost] [:quarter :tax]])
    (pj/lay-point))

(-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure))

(kind/test-last
 [(fn [_]
    (let [w (pj/svg-summary (-> sales-wide
                                (pj/pose [[:quarter :revenue] [:quarter :cost]
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

;; **The vector of pairs is faceting's wide-side twin**, and nothing in
;; the library or the book says so. What they differ in was never
;; decided -- each side was built on its own.

;; The second pair is the same story. `:scales` is the long-side
;; setting, and has no effect on the wide side:

(defn y-ticks [pose]
  (filterv #(re-matches #"\d+" %) (:texts (pj/svg-summary pose))))

(md-table
 ["shape" ":scales :free" ":scales :shared"]
 [["long side, `pj/facet`"
   (pr-str (y-ticks (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure)
                        (pj/options {:scales :free}))))
   (pr-str (y-ticks (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure)
                        (pj/options {:scales :shared}))))]
  ["wide side, pairs grid"
   (pr-str (y-ticks (-> sales-wide (pj/pose [[:quarter :revenue] [:quarter :cost]
                                             [:quarter :tax]])
                        (pj/lay-point) (pj/options {:scales :free}))))
   (pr-str (y-ticks (-> sales-wide (pj/pose [[:quarter :revenue] [:quarter :cost]
                                             [:quarter :tax]])
                        (pj/lay-point) (pj/options {:scales :shared}))))]])

(kind/test-last
 [(fn [_]
    (let [grid (fn [s] (-> sales-wide (pj/pose [[:quarter :revenue] [:quarter :cost]
                                                [:quarter :tax]])
                           (pj/lay-point) (pj/options {:scales s})))
          facet (fn [s] (-> sales-long (pj/lay-point :quarter :value) (pj/facet :measure)
                            (pj/options {:scales s})))]
      ;; The setting moves the long side and does nothing on the wide one.
      (and (not= (y-ticks (facet :free)) (y-ticks (facet :shared)))
           (= (y-ticks (grid :free)) (y-ticks (grid :shared))))))])

;; **The opposite defaults are right, which is the interesting part.**
;; Facet panels are one column split by a key, so their values are
;; commensurable by construction and sharing is safe. Pairs-grid panels
;; hold different columns, so sharing is a claim about the data rather
;; than a fact. Collapsing the two settings under one default would
;; break whichever side lost.

;; ## What the wide side can and cannot do

;; A position adjustment divides a band among **labelled**
;; competitors. Two layers over wide data are labelled by the column
;; each draws -- overlaid, they disagree about a column, so they are
;; told apart by a colour and a legend, and that label is what the
;; dodge divides by:

(-> sales-wide
    pj/overlay
    (pj/lay-bar :quarter :revenue {:position :dodge})
    (pj/lay-bar :quarter :cost {:position :dodge}))

(->> (pj/plan (-> sales-wide pj/overlay
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
 {:column-names [:adjustment :overlaid-layers :one-series]
  :row-vectors
  (let [tops (fn [fr] (->> (pj/plan fr) :panels first :layers
                           (mapcat :groups)
                           (mapcat (fn [g] (seq (:ys g))))
                           (mapv double)))]
    (vec (for [adj [:dodge :stack :fill]]
           [adj
            (pr-str (tops (-> sales-wide pj/overlay
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

(-> sales-wide
    pj/overlay
    (pj/lay-bar :quarter :revenue)
    (pj/lay-line :quarter :cost {:color "#e6550d"}))

;; **One grouped layer over long data can have its groups placed
;; relative to each other** -- which one layer type cannot do for two
;; layers:

(-> sales-long (pj/lay-bar :quarter :value {:color :measure :position :dodge}))

(kind/test-last [(fn [v] (= 12 (:polygons (pj/svg-summary v))))])

;; That is a trade, not a gap. The argument about `overlay vs panel`
;; is not which default is backwards -- it is that the two spellings
;; are two representations, and a writer comparing measures against
;; each other wants the long one.

;; ---

;; # The wide-side reading this branch adds

;; Several columns where one goes are read as several series: the
;; columns are pivoted, and the layer draws from the two the pivot
;; invents.

(-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))

(kind/test-last [(fn [v] (= 12 (:polygons (pj/svg-summary v))))])

;; The pivot happens on the pose, so everything after it reads an
;; ordinary pose -- one dataset, one mapping per aesthetic:

(-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax]) kind/pprint)

(kind/test-last [(fn [pose] (= {:x :quarter :y :value} (:mapping pose)))])

;; ## Every adjustment reaches them

;; The four below differ in one word. Read them against each other: the
;; data is the same in each.

(-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :identity}))

(-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))

(-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack}))

(-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax] {:position :fill}))

(kind/test-last
 [(fn [_]
    (let [l (fn [pos] (-> (pj/plan (-> sales-wide (pj/lay-bar :quarter [:revenue :cost :tax]
                                                              {:position pos})))
                          :panels first :layers first))]
      ;; Three labelled groups with a slot each, where three overlaid
      ;; layers would have three unlabelled groups and one slot between
      ;; them.
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

;; The key column the pivot invents titles the legend, and is `:series`
;; unless the written-out form names it. The value column is `:value`,
;; which titles the value axis and is renamed as any axis is.

(-> sales-wide
    (pj/lay-bar :quarter {:series [:revenue :cost :tax] :as :measure}
                {:position :dodge})
    (pj/options {:y-label "Euros"}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (and (contains? texts "measure")
                 (contains? texts "Euros")
                 (not (contains? texts "series"))
                 (not (contains? texts "value")))))])

;; ## What is not a series

;; A vector of pairs is still the multi-panel form, two parallel
;; vectors are still paired panels, and a vector that is not column
;; references is not columns:

(md-table
 ["written" "read as"]
 [["`(pj/lay-point data [[:q :revenue] [:q :cost]])`"
   (str (:panels (pj/svg-summary (-> sales-wide (pj/lay-point [[:quarter :revenue]
                                                               [:quarter :cost]]))))
        " panels")]
  ["`(pj/lay-point data [:revenue :cost :tax] :quarter)`"
   (str (:polygons (pj/svg-summary (-> sales-wide (pj/lay-bar [:revenue :cost :tax] :quarter))))
        " bars in 1 panel")]
  ["`{:stroke-dash [5 5]}`"
   (str (:panels (pj/svg-summary (-> sales-wide (pj/lay-line :quarter :revenue
                                                             {:stroke-dash [5 5]}))))
        " panel -- a dash pattern")]
  ["`{:group [:revenue :cost :tax]}`"
   (str (:panels (pj/svg-summary (-> sales-wide (pj/lay-point :quarter :revenue
                                                              {:group [:revenue :cost :tax]}))))
        " panel -- one compound key")]])

;; `:group` is the exception, and it belongs to a *different* duality:
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

;; ## What is refused, and what the message says

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

;; One panel, two series. The band is divided between them:

(-> sales (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))

(kind/test-last [(fn [v] (= 24 (:polygons (pj/svg-summary v))))])

;; Two panels, two series in each. The canvas is divided by a column
;; and each panel's bands are divided between the measures:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
    (pj/facet :region))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Four panels, two series in each, piled rather than set side by side:

(-> sales
    (pj/lay-bar :quarter [:revenue :cost :tax] {:position :stack})
    (pj/facet-grid :region :channel))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Two sub-plots, each of two series, read against one y axis:

(pj/arrange (vec (for [r ["EU" "AS"]]
                   (-> (tc/select-rows sales (fn [row] (= r (:region row))))
                       (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge}))))
            {:share-scales #{:y} :align-panels true})

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Sub-plots of faceted panels of series -- three levels of division,
;; and one sub-plot drawing something else entirely:

(pj/arrange [(-> sales (pj/lay-bar :quarter [:revenue :cost :tax] {:position :dodge})
                 (pj/facet :region))
             (-> sales (pj/lay-line :quarter :units) (pj/facet :region))])

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 4 (:panels s)) (= 24 (:polygons s)) (= 2 (:lines s)))))])

;; ## The one combination that is missing, and why

;; A grid of panels whose cells each show several series cannot be
;; written:

(try
  (-> sales
      (pj/pose [[:revenue :units] [:cost :units] [:tax :units]])
      (pj/lay-point :revenue [:units :cost :tax]))
  (catch Exception e (ex-message e)))

(kind/test-last [(fn [m] (and (string? m) (re-find #"composite pose" m)))])

;; The duality explains it. **The pairs grid wants the wide reading of
;; the dataset and the series wants the long one**, and a dataset has
;; one shape at a time. Not an arbitrary limitation. The workaround is
;; on the long side throughout: `pj/facet` reaches the same picture
;; where the split comes from a column, and `pj/arrange` of series
;; poses where it does not.

;; ---

;; # Four repairs that came with it

;; ## The panel split says what it did

;; Two layers disagreeing about an axis still get a panel each -- the
;; default is unchanged -- but the split no longer happens in silence,
;; and the note names the onward routes. `pj/overlay` draws the second
;; column against the axis the panel already has, and always applies.
;; The series route replaces the two calls with one, and is named only
;; where following it draws.

;; The split is settled where `:overlay` is read, which is at draft
;; time, so the note is said when the pose is drawn rather than when a
;; layer is added.

(defn note-of
  "What a pose says when it is drawn."
  [pose]
  (with-out-str (pj/plot pose)))

(note-of (-> sales (pj/lay-point :quarter :revenue)
             (pj/lay-point :quarter :cost)))

(kind/test-last
 [(fn [out] (and (re-find #"panel of its own" out)
                 (re-find #"pj/overlay" out)
                 (re-find #"\[:revenue :cost\]" out)))])

;; Nothing is said where the layers agree, or under `pj/overlay`, which
;; asked for one panel -- and nothing where `pj/overlay` is written
;; after the layers, which asks for the same thing:

[(note-of (-> sales (pj/lay-point :quarter :revenue)
              (pj/lay-line :quarter :revenue)))
 (note-of (-> sales pj/overlay (pj/lay-point :quarter :revenue)
              (pj/lay-point :quarter :cost)))
 (note-of (-> sales (pj/lay-point :quarter :revenue)
              (pj/lay-point :quarter :cost)
              pj/overlay))]

(kind/test-last [(fn [outs] (= ["" "" ""] outs))])

;; The series route has two conditions, and the note leaves it out
;; where either fails. The pivot reads one dataset, so where the layer
;; brings data of its own neither dataset carries both columns. And a
;; call takes one series, so where both axes disagree the second
;; disagreement would be left standing.

[(note-of
  (-> {:fitted [1 2 3] :residual [1 2 3]}
      (pj/lay-point :fitted :residual)
      (pj/lay-point :x :y {:data (tc/dataset {:x [1 2 3] :y [1 2 3]})})))
 (note-of
  (-> (assoc sales :units [3 4 5 6])
      (pj/lay-point :revenue :cost)
      (pj/lay-point :quarter :units)))]

(kind/test-last
 [(fn [outs] (every? (fn [out] (and (re-find #"panel of its own" out)
                                    (re-find #"pj/overlay" out)
                                    (not (re-find #"series" out))))
                     outs))])

;; ## Composites nest

;; `pose/compute-layout` recurses for `:horizontal` and `:vertical` at
;; any depth, and the cells render correctly. The refusal at the door
;; named a limitation the renderer does not have -- and `pj/arrange` of
;; three plots builds a composite of composites itself, so the advice
;; the message gave produced what it refused.

(pj/arrange [(pj/arrange [(pj/lay-point sales :quarter :revenue)])
             (pj/lay-point sales :quarter :cost)])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A tree written out, three levels deep:

{:opts {:width 800 :height 560}
 :layout {:direction :vertical}
 :poses [(pj/lay-point sales :quarter :revenue)
         {:layout {:direction :horizontal :weights [2 1]}
          :poses [(pj/lay-point sales :quarter :cost)
                  {:layout {:direction :vertical}
                   :poses [(pj/lay-point sales :quarter :units)
                           (pj/lay-bar sales :region)]}]}]}

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

(-> valued (pj/lay-bar :k :v {:color :g :position :fill}))

(-> over-t (pj/lay-area :t :v {:color :s :position :fill}))

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

(-> months (pj/lay-area :month :value {:color :reading :position :stack}))

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

(pj/arrange [(-> (pj/lay-point sales :quarter :revenue)
                 (pj/options {:theme {:bg "#FFFFFF"}}))
             (pj/lay-point sales :quarter :cost)]
            {:width 700 :height 280})

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

;; **Two decisions this does not take.**
;;
;; - **One scale-sharing setting** whose default follows the side that
;;   produced the panels. The two already disagree about whether a
;;   categorical axis can be shared at all, one unioning the categories
;;   and the other reporting that a union domain is undefined. It is
;;   the largest change of the set and the only one that would move
;;   behaviour a reader depends on.
;; - **A fourth `:layout` direction** for two cells sharing a canvas.
;;   The geometry is a few lines; what it needs is chrome -- a
;;   transparent cell background, an axis that can draw on the right,
;;   and the tick-suppression keys made public. It should be called
;;   `:inset` rather than `:overlay`, with `:insets` required, because
;;   `:overlay true` on a composite already says where a *layer* lands
;;   rather than where a *cell* is drawn.

;; ## The test this frame gives
;;
;; For any new layout feature: **what is this on the other side of the
;; pivot?**
;;
;; - If there is a twin, build one thing rather than two. Both existing
;;   twins were built separately and drifted.
;; - If there is no twin, say why. The interval endpoints have none and
;;   need none.
;; - If the twin exists and is missing, ask whether it is missing for
;;   the label reason. Grouping and position adjustment both were, and
;;   one change answered both.
;;
;; Where the frame stops: it says nothing about `pj/arrange`,
;; `:layout` or `pj/marginal`, which read neither columns nor values.
;; That is the right silence -- those are geometry, and geometry has no
;; wide and no long.
