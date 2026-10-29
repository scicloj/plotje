(ns scicloj.plotje.wide-and-long-test
  "A dataset carrying several measures can be written wide, one column
   per measure, or long, one value column beside a key column naming
   which measure a row holds. The two shapes hold the same information.

   These tests cover the wide-side readings added on this branch --
   several columns where one goes, read as several series -- and the
   four repairs that came with them."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [tablecloth.api :as tc]))

(def sales
  {:quarter ["Q1" "Q2" "Q3" "Q4"]
   :revenue [120 150 140 190]
   :cost    [90 100 115 120]})

(def with-target (assoc sales :target [100 130 130 170]))

(defn- layer-of [pose]
  (-> (pj/plan pose) :panels first :layers first))

(defn- bar-spans
  "Each bar's [left right] along x, absolute. A shape's own points are
   pre-transform, so the enclosing translations are accumulated."
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
                       off (+ dx tx)
                       own (when (and (= :polygon (first node)) (:points attrs))
                             (let [xs (->> (clojure.string/split
                                            (clojure.string/trim (str (:points attrs)))
                                            #"[\s,]+")
                                           (map #(Double/parseDouble %))
                                           (partition 2)
                                           (map first))]
                               [[(+ dx (apply min xs)) (+ dx (apply max xs))]]))]
                   (into (vec own) (mapcat #(walk % off)) kids))
                 (sequential? node) (into [] (mapcat #(walk % dx)) node)
                 :else []))]
    (vec (sort (walk (pj/plot pose {:width 600 :height 400}) 0.0)))))

;; ---- Several columns where one goes ----

(deftest several-columns-are-read-as-series-test
  (testing "a vector in either positional slot"
    (is (= 8 (:polygons (pj/svg-summary
                         (-> sales (pj/lay-bar :quarter [:revenue :cost]
                                               {:position :dodge}))))))
    (is (= 8 (:polygons (pj/svg-summary
                         (-> sales (pj/lay-bar [:revenue :cost] :quarter))))))
    (is (= 1 (:panels (pj/svg-summary
                       (-> sales (pj/lay-line :quarter [:revenue :cost]))))))
    (is (= 8 (:polygons (pj/svg-summary
                         (-> sales (pj/lay-bar {:x :quarter
                                                :y [:revenue :cost]
                                                :position :dodge})))))))

  (testing "the layer keeps its series; the draft pivots it into ordinary columns"
    (let [pose (-> sales (pj/lay-bar :quarter [:revenue :cost]))
          drafted (-> pose pj/draft :layers first)]
      (is (= [:revenue :cost] (-> pose :layers first :mapping :y)))
      (is (= [:quarter :value] [(:x drafted) (:y drafted)]))
      ;; and the row each pivoted row came from
      (is (= [:quarter :scicloj.plotje/row :series :value] (vec (tc/column-names (:data drafted)))))
      (is (= 8 (tc/row-count (:data drafted))))))

  (testing "the columns it reads are consumed; the rest come through"
    (let [pose (-> with-target (pj/lay-bar :quarter [:revenue :cost]))
          cols (set (tc/column-names (-> pose pj/draft :layers first :data)))]
      (is (contains? cols :target))
      (is (not (contains? cols :revenue)))))

  (testing "the written-out form names the key column, which titles the legend"
    (let [texts (set (:texts (pj/svg-summary
                              (-> sales (pj/lay-bar :quarter
                                                    {:series [:revenue :cost]
                                                     :as :measure})))))]
      (is (contains? texts "measure"))
      (is (contains? texts "revenue"))
      (is (not (contains? texts "series"))))
    (is (not (contains? (set (:texts (pj/svg-summary
                                      (-> sales (pj/lay-bar :quarter [:revenue :cost])))))
                        "series"))
        "unnamed, the key column titles no legend"))

  (testing "the series' columns title the value axis, and :y-label renames it"
    (let [texts (set (:texts (pj/svg-summary
                              (-> sales (pj/lay-bar :quarter [:revenue :cost])
                                  (pj/options {:y-label "Euros"})))))]
      (is (contains? texts "Euros"))
      (is (not (contains? texts "value"))))))

(deftest series-carry-a-label-and-layers-do-not-test
  (testing "a series is two labelled groups, which is what an adjustment needs"
    (let [l (layer-of (-> sales (pj/lay-bar :quarter [:revenue :cost]
                                            {:position :dodge})))]
      (is (= 2 (:n-groups (:dodge-ctx l))))
      (is (= ["revenue" "cost"] (mapv :label (:groups l))))
      (is (= [0 1] (mapv :dodge-idx (:groups l))))))

  (testing "two layers sharing a panel are labelled by the column each draws, so a dodge divides them"
    ;; Overlaid layers that disagree about a column are told apart by a
    ;; colour and a legend, and the label that legend prints is what
    ;; the dodge divides by. Both labels used to be the empty string,
    ;; so the cohort saw one group, the dodge was inert, and the two
    ;; sets of bars were drawn at the same places.
    (let [ls (-> (pj/plan (-> sales
                              (pj/lay-bar :quarter :revenue {:position :dodge})
                              (pj/lay-bar :quarter :cost {:position :dodge})))
                 :panels first :layers)]
      (is (= [[2 ["revenue"]] [2 ["cost"]]]
             (mapv (fn [l] [(:n-groups (:dodge-ctx l)) (mapv :label (:groups l))]) ls)))
      (is (= [[0] [1]] (mapv (fn [l] (mapv :dodge-idx (:groups l))) ls))
          "and each takes a slot of its own"))))

(deftest which-marks-draw-a-dodge-test
  ;; Pins what `known_limitations.clj` says about `:position :dodge`
  ;; per mark, which had drifted from the code: it claimed the request
  ;; was dropped at construction on `lay-bar` and `lay-summary` and
  ;; that `lay-point` drew no offset. Measured, the request survives
  ;; construction on all four, `lay-point` does draw an offset, and
  ;; `lay-bar` dodges whether or not it is asked.
  (let [d {:q ["a" "b" "a" "b"] :v [1.0 2.0 3.0 4.0] :g ["x" "x" "y" "y"]}
        plan-position (fn [mk opts]
                        (-> (mk d :q :v opts) pj/plan :panels first :layers first :position))]

    (testing "the request survives construction on every mark"
      (doseq [mk [pj/lay-bar pj/lay-point pj/lay-line pj/lay-summary]]
        (is (= :dodge (:position (first (:layers (mk d :q :v {:color :g
                                                              :position :dodge}))))))))

    (testing "a bar dodges whether or not it is asked"
      (is (= :dodge (plan-position pj/lay-bar {:color :g})))
      (is (= :dodge (plan-position pj/lay-bar {:color :g :position :dodge}))))

    (testing "a point and a line carry it to the plan"
      (is (= :dodge (plan-position pj/lay-point {:color :g :position :dodge})))
      (is (= :dodge (plan-position pj/lay-line {:color :g :position :dodge}))))

    (testing "a summary loses it between the pose and the plan"
      (is (nil? (plan-position pj/lay-summary {:color :g :position :dodge}))))))

(deftest every-adjustment-reaches-a-series-test
  (testing "a dodge divides the band into abutting slots"
    (let [spans (bar-spans (-> sales (pj/lay-bar :quarter [:revenue :cost]
                                                 {:position :dodge})))
          [[a0 a1] [b0 b1]] (take 2 spans)
          [[w0 w1]] (bar-spans (-> sales (pj/lay-bar :quarter [:revenue :cost]
                                                     {:position :identity})))]
      (is (= 8 (count spans)))
      (is (== a1 b0))
      (is (== (- b1 a0) (- w1 w0)))))

  (testing "a stack reaches the total"
    (let [l (layer-of (-> sales (pj/lay-bar :quarter [:revenue :cost]
                                            {:position :stack})))]
      (is (== 310.0 (apply max (mapcat #(map double (:ys %)) (:groups l)))))))

  (testing "a fill reaches one"
    (is (= [0.0 1.0]
           (mapv double (:y-domain (first (:panels (pj/plan
                                                    (-> sales (pj/lay-bar :quarter
                                                                          [:revenue :cost]
                                                                          {:position :fill})))))))))))

(deftest what-is-not-a-series-test
  (testing "two parallel vectors of columns are series in pairs, on one panel"
    (is (= 1 (:panels (pj/svg-summary
                       (-> with-target (pj/lay-point [:revenue :cost]
                                                     [:target :target])))))))

  (testing "a vector that is not column references is not a series"
    (is (= 1 (:panels (pj/svg-summary
                       (-> sales (pj/lay-line :quarter :revenue
                                              {:stroke-dash [5 5]})))))
        "a dash pattern")
    (is (thrown-with-msg? Exception #"must be a column reference"
                          (-> sales (pj/lay-point [5 5] :revenue)))
        "a vector of numbers on an axis"))

  (testing "a vector on :group is still one compound key"
    (is (= 1 (:panels (pj/svg-summary
                       (-> sales (pj/lay-point :quarter :revenue
                                               {:group [:revenue :cost]})))))))

  (testing "a vector on :tooltip is hiccup, not columns"
    (is (= 1 (:panels (pj/svg-summary
                       (-> sales (pj/lay-point :quarter :revenue
                                               {:tooltip [:b "a note"]}))))))))

(deftest series-refusals-test
  (testing "an appearance aesthetic has nothing for a pivot to draw"
    (doseq [k [:color :size :alpha :shape :fill]]
      (is (thrown-with-msg? Exception #"belongs on :x or :y"
                            (-> sales (pj/lay-point :quarter :revenue
                                                    {k [:revenue :cost]})))
          (str k))))

  (testing "a series on each axis is read in pairs, however it is written"
    ;; The four spellings -- positional, a mapping map on the call, a
    ;; pose's mapping map, and pj/pose's positional slots -- draw the
    ;; same single panel, one series per pair.
    (let [d {:t1 [1 2 3] :t2 [1.5 2.5 3.5] :v1 [5 6 7] :v2 [1 1 2]}
          seen (fn [p] (let [s (pj/svg-summary p)]
                         [(:panels s) (:points s) (set (:texts s))]))
          one (seen (pj/lay-point d [:t1 :t2] [:v1 :v2]))]
      (is (= 1 (first one)))
      (is (= 6 (second one)))
      (is (every? (nth one 2) ["t1 / v1" "t2 / v2" "t1, t2" "v1, v2"]))
      (is (= one (seen (pj/lay-point d {:x [:t1 :t2] :y [:v1 :v2]}))))
      (is (= one (seen (-> d (pj/pose {:x [:t1 :t2] :y [:v1 :v2]}) pj/lay-point))))
      (is (= one (seen (-> (pj/pose d [:t1 :t2] [:v1 :v2]) pj/lay-point))))
      (testing "the rows pair the columns in order"
        (let [ds (-> (pj/lay-point d [:t1 :t2] [:v1 :v2]) pj/draft :layers first :data)]
          (is (= [1.0 2.0 3.0 1.5 2.5 3.5] (mapv double (ds :x-value))))
          (is (= [5 6 7 1 1 2] (vec (ds :y-value))))))
      (testing ":as on either series names the key column"
        (is (contains? (nth (seen (pj/lay-point d {:x {:series [:t1 :t2] :as :run}
                                                   :y [:v1 :v2]}))
                            2)
                       "run")))))

  (testing "series of different lengths cannot be paired"
    (is (thrown-with-msg? Exception #"as many columns each"
                          (pj/lay-point with-target {:x [:revenue :cost]
                                                     :y [:cost :target :quarter]}))))

  (testing "a pose reading a series on each axis with no layer draws them"
    ;; The draft pivots the pose's series and infers a layer for it.
    (is (= {:panels 1 :points 8}
           (select-keys (pj/svg-summary (pj/pose with-target {:x [:revenue :cost]
                                                              :y [:cost :target]}))
                        [:panels :points]))))

  (testing "a column the data does not have is named, with the ones it does"
    (is (thrown-with-msg? Exception #"does not have \[:nope\]"
                          (-> sales (pj/lay-bar :quarter [:revenue :nope])))))

  (testing "one column is not several"
    (is (thrown-with-msg? Exception #"1 column in a slot"
                          (-> sales (pj/lay-bar :quarter {:series [:revenue]})))))

  (testing "the invented columns are not written over in silence"
    ;; Each clash names only the column that clashed, and the remedy
    ;; that moves it: :as renames the key column, never the value one.
    (let [msg (fn [data spec]
                (try (pj/lay-bar data :quarter spec) nil
                     (catch Exception e (ex-message e))))
          value-msg (msg (assoc sales :value [1 2 3 4]) [:revenue :cost])
          key-msg (msg (assoc sales :series [1 2 3 4]) [:revenue :cost])
          as-msg (msg (assoc sales :region [1 2 3 4])
                      {:series [:revenue :cost] :as :region})]
      (is (re-find #"value column :value, and the data already has a :value column" value-msg))
      (is (not (re-find #":as|:series" value-msg)))
      (is (re-find #"key column :series, and the data already has a :series column" key-msg))
      (is (not (re-find #":value" key-msg)))
      (is (re-find #":as names the key column :region" as-msg))))

  (testing "a mapping already naming a consumed column is named, not broken"
    (is (thrown-with-msg? Exception #"a series on this pose has already read"
                          (-> sales (pj/pose :quarter :revenue)
                              (pj/lay-bar :quarter [:revenue :cost])))))

  (testing "a composite's cells share one dataset, so the pivot is refused"
    (is (thrown-with-msg? Exception #"on a composite pose"
                          (-> (pj/arrange [(pj/lay-point with-target :quarter :target)])
                              (pj/lay-bar :quarter [:revenue :cost])))))

  ;; A series written out is a mapping map, and every other mapping map
  ;; is held to `pose/check-explicit-mapping!`. Without this check the
  ;; series map was the one form where a misspelled `:as` was ignored
  ;; and the legend came out titled `:series`.
  (testing "a key a series does not take is named"
    (is (thrown-with-msg? Exception #"unexpected key\(s\): \[:azz\]"
                          (-> sales (pj/lay-bar :quarter {:series [:revenue :cost]
                                                          :azz :measure})))))

  (testing "an appearance aesthetic reads a series written out, as it reads a vector"
    (is (thrown-with-msg? Exception #"belongs on :x or :y"
                          (-> sales (pj/lay-point :quarter :revenue
                                                  {:color {:series [:revenue :cost]}}))))))

;; ---- Where a series may be written ----

(deftest a-series-may-be-written-on-a-pose-test
  ;; A series on a pose reaches the layers below it, which is what
  ;; scope does for every other mapping. `pj/pose` used to report one
  ;; and name the lay-* call.
  (testing "pj/pose reads a series, in both spellings"
    (doseq [written [[:revenue :cost] {:series [:revenue :cost] :as :measure}]]
      (is (= {:panels 1 :lines 2}
             (select-keys (pj/svg-summary
                           (-> sales (pj/pose {:x :quarter :y written}) pj/lay-line))
                          [:panels :lines]))
          (pr-str written))))

  (testing "the positional arity says the same"
    (is (= {:panels 1 :lines 2}
           (select-keys (pj/svg-summary
                         (-> sales (pj/pose :quarter [:revenue :cost]) pj/lay-line))
                        [:panels :lines]))))

  (testing "every layer below the pose reads it"
    (is (= {:panels 1 :lines 2 :points 8}
           (select-keys (pj/svg-summary
                         (-> sales (pj/pose {:x :quarter :y [:revenue :cost]})
                             pj/lay-line pj/lay-point))
                        [:panels :lines :points]))))

  (testing "the invented key column can then be faceted"
    (is (= 2 (:panels (pj/svg-summary
                       (-> sales (pj/pose {:x :quarter :y [:revenue :cost]})
                           pj/lay-line (pj/facet :series)))))))

  (testing "a series at the call and one on the pose are two pivots, and reported"
    ;; The pivot consumes the columns it reads, so the second would
    ;; name columns the first had taken -- the same rule that refuses
    ;; two series in one call.
    (is (thrown-with-msg? Exception #"two pivots have no shared shape"
                          (-> with-target (pj/pose {:x :quarter :y [:revenue :cost]})
                              (pj/lay-line :quarter [:revenue :target])))))

  ;; The capability a series on pj/pose would add is already reachable:
  ;; the first lay-* pivots onto the pose, so every later layer reads
  ;; the invented columns as it reads any other mapping.
  (testing "layers of different kinds read one series"
    (is (= {:panels 1 :points 8}
           (select-keys (pj/svg-summary
                         (-> sales (pj/lay-bar :quarter [:revenue :cost])
                             (pj/lay-point :quarter :value)))
                        [:panels :points])))))

;; ---- A series reads its value column through a scale ----

(deftest a-series-carries-its-scale-test
  ;; The scale used to be accepted and dropped: the pivot replaced the
  ;; whole mapping value with the invented column, and the axis came out
  ;; linear with no word said.
  (testing "a scale written in the series is the scale pj/scale sets"
    (doseq [spec [{:type :log} {:domain [0 500]}]]
      (is (= (-> (pj/plan (-> sales (pj/lay-line :quarter [:revenue :cost])
                              (pj/scale :y spec)))
                 :panels first ((juxt :y-domain (comp :type :y-scale))))
             (-> (pj/plan (-> sales (pj/lay-line :quarter {:series [:revenue :cost]
                                                           :scale spec})))
                 :panels first ((juxt :y-domain (comp :type :y-scale)))))
          (pr-str spec))))

  (testing "a series with no scale reads its value column plainly"
    (is (= :linear (-> (pj/plan (-> sales (pj/lay-line :quarter [:revenue :cost])))
                       :panels first :y-scale :type)))))

;; ---- Layers sharing a panel ----

(deftest layers-on-other-columns-share-the-panel-quietly-test
  ;; Two numeric columns on one axis need no report; the legend and the
  ;; axis title name both.
  (let [p (-> sales (pj/lay-point :quarter :revenue) (pj/lay-point :quarter :cost))]
    (is (= "" (with-out-str (pj/plot p))))
    (is (= 1 (:panels (pj/svg-summary p))))
    (is (contains? (set (:texts (pj/svg-summary p))) "revenue, cost"))))

;; ---- Composites nest ----

(deftest composites-nest-test
  (testing "a composite may be a cell of another"
    (is (= 2 (:panels (pj/svg-summary
                       (pj/arrange [(pj/arrange [(pj/lay-point sales :quarter :revenue)])
                                    (pj/lay-point sales :quarter :cost)]))))))

  (testing "a hand-written tree renders at depth"
    (is (= 4 (:panels (pj/svg-summary
                       {:opts {:width 800 :height 600}
                        :layout {:direction :vertical}
                        :poses [(pj/lay-point sales :quarter :revenue)
                                {:layout {:direction :horizontal :weights [2 1]}
                                 :poses [(pj/lay-point sales :quarter :cost)
                                         {:layout {:direction :vertical}
                                          :poses [(pj/lay-point sales :quarter :revenue)
                                                  (pj/lay-point sales :quarter :cost)]}]}]})))))

  (testing "pj/arrange of three plots builds such a tree itself"
    (let [three (pj/arrange [(pj/lay-point sales :quarter :revenue)
                             (pj/lay-point sales :quarter :cost)
                             (pj/lay-point sales :quarter :revenue)]
                            {:cols 2})]
      (is (= 3 (:panels (pj/svg-summary three)))))))

;; ---- The two position repairs ----

(defn- fill-tops [pose]
  (->> (:groups (layer-of pose))
       (mapv (fn [g]
               (if-let [counts (:counts g)]
                 (apply max (map (comp double :y1) counts))
                 (apply max (map double (:ys g))))))))

(deftest fill-normalizes-every-layer-shape-test
  (let [counted {:k ["a" "a" "a" "b" "b"] :g ["p" "p" "q" "p" "q"]}
        valued  {:k ["a" "a" "b" "b"] :v [30 20 45 15] :g ["p" "q" "p" "q"]}
        area    {:t [1 2 3 1 2 3] :v [1 2 3 3 2 1] :s ["A" "A" "A" "B" "B" "B"]}]

    (testing "a counted bar, which always normalized"
      (is (== 1.0 (apply max (fill-tops (-> counted (pj/lay-bar :k {:color :g
                                                                    :position :fill})))))))

    (testing "a bar with a value column, which stacked without normalizing"
      ;; Before the repair these reached 60.0 -- the raw cumulative sum --
      ;; under a y domain of [0, 1], so the outer series was drawn
      ;; thousands of drawing units above the panel and was invisible.
      (is (== 1.0 (apply max (fill-tops (-> valued (pj/lay-bar :k :v {:color :g
                                                                      :position :fill})))))))

    (testing "an area, which has no counting form and so never normalized"
      (is (== 1.0 (apply max (fill-tops (-> area (pj/lay-area :t :v {:color :s
                                                                     :position :fill})))))))

    (testing "the shares at one place are the values' shares of their total"
      (let [[p q] (:groups (layer-of (-> valued (pj/lay-bar :k :v {:color :g
                                                                   :position :fill}))))]
        ;; At "a": p is 30 and q is 20 of 50, so q runs 0 to 0.4 and p
        ;; 0.4 to 1.0. The last group is laid down first, so q sits on
        ;; the baseline and p's bounds are the ones that vary.
        (is (= [1.0 1.0] (mapv double (:ys p))))
        (is (= [0.4 0.25] (mapv double (:y0s p))))
        (is (= [0.4 0.25] (mapv double (:ys q))))
        (is (every? zero? (map double (:y0s q))))))

    (testing "a place whose values sum to zero gets a zero share"
      (let [l (layer-of (-> {:k ["a" "a" "b" "b"] :v [0 0 45 15] :g ["p" "q" "p" "q"]}
                            (pj/lay-bar :k :v {:color :g :position :fill})))]
        (is (every? #(<= 0.0 (double %) 1.0)
                    (mapcat #(map double (:ys %)) (:groups l))))))

    (testing "a stack is unchanged by the repair"
      (let [[p q] (:groups (layer-of (-> valued (pj/lay-bar :k :v {:color :g
                                                                   :position :stack}))))]
        (is (= [50.0 60.0] (mapv double (:ys p))))
        (is (= [20.0 15.0] (mapv double (:ys q))))))))

(deftest a-stack-keeps-the-axis-order-test
  (let [months {:month ["Jan" "Feb" "Mar" "Apr" "May" "Jun"
                        "Jan" "Feb" "Mar" "Apr" "May" "Jun"]
                :value [78 64 58 47 52 60 44 68 112 158 196 204]
                :reading ["rain" "rain" "rain" "rain" "rain" "rain"
                          "sun" "sun" "sun" "sun" "sun" "sun"]}]

    (testing "a categorical x keeps the order the axis carries"
      ;; It used to be sorted, so the months came back in dictionary
      ;; order -- Apr, Feb, Jan, Jun, Mar, May -- while the axis drew
      ;; them in data order, and the polygon zig-zagged between them.
      (is (= [["rain" ["Jan" "Feb" "Mar" "Apr" "May" "Jun"]]
              ["sun" ["Jan" "Feb" "Mar" "Apr" "May" "Jun"]]]
             (->> (pj/plan (-> months (pj/lay-area :month :value
                                                   {:color :reading :position :stack})))
                  :panels first :layers first :groups
                  (mapv (fn [g] [(:label g) (vec (:xs g))]))))))

    (testing "a numerical x is still sorted, since nothing else orders it"
      (is (= [1 2 3]
             (-> (pj/plan (-> {:t [3 1 2 3 1 2] :v [1 2 3 3 2 1]
                               :s ["A" "A" "A" "B" "B" "B"]}
                              (pj/lay-area :t :v {:color :s :position :stack})))
                 :panels first :layers first :groups first :xs vec))))))

;; ---- A cell keeps the options it was given ----

(deftest a-cell-keeps-its-render-options-test
  (let [fills (fn [v]
                (->> (tree-seq sequential? seq v)
                     (filter #(and (vector? %) (= :rect (first %)) (map? (second %))
                                   (number? (:width (second %)))
                                   (> (:width (second %)) 100)))
                     (keep #(:fill (second %)))
                     distinct vec))
        themed (-> (pj/lay-point sales :quarter :revenue)
                   (pj/options {:theme {:bg "#FFFFFF"}}))]

    (testing "a theme set on one cell reaches that cell once arranged"
      ;; The composite used to hand every cell its own options at the
      ;; render stage, so a per-cell :theme was accepted and dropped.
      (is (= ["rgb(255,255,255)"] (fills (pj/plot themed {:width 300 :height 200}))))
      (is (= ["rgb(255,255,255)" "rgb(232,232,232)"]
             (fills (pj/plot (pj/arrange [themed (pj/lay-point sales :quarter :cost)]
                                         {:width 500 :height 200}))))))))

(deftest a-series-no-layer-is-added-to-is-drawn-test
  ;; The draft pivots a series wherever it is written, so a series on a
  ;; pose that no layer was added to is drawn: a cell of pj/arrange
  ;; written as a mapping, which takes its layers from above, and a bare
  ;; pj/pose, which takes an inferred one.
  (testing "a cell of a data-first arrangement"
    (is (= {:panels 2 :points 12}
           (select-keys (pj/svg-summary
                         (-> (pj/arrange sales [{:x :quarter :y [:revenue :cost]}
                                                {:x :quarter :y :revenue}])
                             pj/lay-point))
                        [:panels :points]))))
  (testing "a pose given a series and no layer"
    (is (= 1 (:panels (pj/svg-summary (pj/pose sales {:x :quarter :y [:revenue :cost]}))))))
  (testing "and with a layer added"
    (is (= 1 (:panels (pj/svg-summary
                       (-> sales
                           (pj/pose {:x :quarter :y [:revenue :cost]})
                           pj/lay-line)))))))

(deftest a-layer-on-other-columns-checks-its-columns-test
  (let [msg (try (-> sales (pj/lay-point :quarter :revenue) (pj/lay-point :nope :nada))
                 nil
                 (catch clojure.lang.ExceptionInfo e (ex-message e)))]
    (is (re-find #"names a column that doesn't exist in the data" msg))
    (is (not (re-find #"sub-pose|pj/overlay" msg)))))

(deftest series-pairs-refused-on-an-x-only-mark-test
  ;; The pair branch ran before the "uses only the x column" check, so a
  ;; histogram drew a count axis titled "y value" beside unused y columns.
  (let [w {:t1 [1 2 3] :t2 [2 3 4] :v1 [10 20 30] :v2 [15 25 35]}]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"draws from one column, and was given"
                          (pj/lay-histogram w [:t1 :t2] [:v1 :v2])))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"draws from one column, and was given"
                          (pj/lay-histogram w :t1 [:v1 :v2])))))

(deftest series-pairs-errors-name-the-written-columns-test
  (let [w {:t1 [1 2] :t2 ["a" "b"] :v1 [1 2] :v2 [3 4]}]
    (testing "unequal pairs get the pairs message, not the one-column one"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"as many columns each"
                            (pj/plan (pj/lay-line w [:t1 :t2] [:v1])))))
    (testing "mixed kinds name the columns written, not the pivot's :x-value"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"\[:t1 :t2\] to read as series in pairs"
                            (pj/plan (pj/lay-line w [:t1 :t2] [:v1 :v2])))))))

(deftest a-series-keeps-the-written-column-order-test
  ;; The pivot read the columns as a set, so the legend listed them in
  ;; the data's order whatever order they were written in.
  (let [labels (fn [cols] (mapv :label (:entries (:legend (pj/plan (pj/lay-line sales :quarter cols))))))]
    (is (= ["revenue" "cost"] (labels [:revenue :cost])))
    (is (= ["cost" "revenue"] (labels [:cost :revenue])))))

(deftest a-series-legend-takes-a-specified-title-test
  ;; The entries of a series' legend name its columns, so the legend is
  ;; given no title. That empty title is a `:label` in the colour scale,
  ;; which outranks the `:color-label` option, so it is written only
  ;; where the writer titled nothing.
  (let [q {:q [1 2 3 4] :revenue [3 4 5 6] :cost [1 2 2 3] :units [2 2 3 3]}
        title (fn [pose] (:title (:legend (pj/plan pose))))
        series (pj/lay-line q :q [:revenue :cost])]
    (testing "no title where none is specified"
      (is (= "" (title series)))
      (is (= "" (title (-> q (pj/pose {:x :q :y [:revenue :cost]}) pj/lay-line)))))
    (testing ":color-label titles it"
      (is (= "Measure" (title (pj/options series {:color-label "Measure"}))))
      (is (= "Measure" (title (-> q
                                  (pj/pose {:x :q :y [:revenue :cost]})
                                  pj/lay-line
                                  (pj/options {:color-label "Measure"}))))))
    (testing "a :label in the colour scale titles it, and outranks the option"
      (is (= "Measure" (title (pj/scale series :color {:label "Measure"}))))
      (is (= "Scale" (title (-> series
                                (pj/scale :color {:label "Scale"})
                                (pj/options {:color-label "Option"})))))
      (is (= "Measure" (title (-> q
                                  (pj/pose {:x :q :y [:revenue :cost]})
                                  pj/lay-line
                                  (pj/scale :color {:label "Measure"}))))))
    (testing "a colour scale set beside a series on the pose reaches the series"
      (is (= [[1.0 0.0 0.0 1.0] [0.0 0.0 1.0 1.0]]
             (->> (-> q
                      (pj/pose {:x :q :y [:revenue :cost]})
                      pj/lay-line
                      (pj/scale :color {:values ["red" "blue"]}))
                  pj/plan :legend :entries (mapv :color)))))
    (testing "a layer beside the series joins the titled legend"
      (let [pose (-> series (pj/lay-point :q :units) (pj/options {:color-label "Measure"}))]
        (is (= "Measure" (title pose)))
        (is (= 3 (count (:entries (:legend (pj/plan pose))))))))
    (testing "layers drawing different columns take the title too"
      (is (= "Measure" (title (-> q
                                  (pj/lay-line :q :revenue)
                                  (pj/lay-line :q :cost)
                                  (pj/options {:color-label "Measure"}))))))
    (testing "a :shape reading the key column shares the colour legend"
      (let [shaped (pj/lay-point q :q [:revenue :cost] {:shape :series})
            one-legend? (fn [pose]
                          (let [plan (pj/plan pose)]
                            (and (nil? (:shape-legend plan))
                                 (every? :shape (:entries (:legend plan))))))]
        (is (one-legend? shaped))
        (is (= "" (title shaped)))
        (is (one-legend? (pj/options shaped {:color-label "Measure" :shape-label "Measure"})))
        (is (= "Measure" (title (pj/options shaped {:color-label "Measure" :shape-label "Measure"}))))))))

(deftest a-layer-beside-a-series-counts-the-rows-as-written-test
  ;; A layer that names no column on the series' axis, and whose type
  ;; draws from one column, reads the data as written: on the pivoted
  ;; rows it would count each row once per series column.
  (let [mpg-like {:drv ["f" "f" "r" "4" "4" "4"]
                  :displ [1.8 2.0 5.7 4.2 3.0 2.5]
                  :hwy [29 31 25 17 20 22]
                  :cty [18 21 15 12 14 16]}
        counts (fn [pose] (-> (pj/plan pose) :panels first :layers first
                              :groups first :counts (->> (mapv :count))))
        bins (fn [pose] (-> (pj/plan pose) :panels first :layers first
                            :groups first :bars (->> (mapv :count))))]
    (testing "a counting bar keeps its counts beside a series"
      (is (= (counts (pj/lay-bar mpg-like :drv))
             (counts (-> mpg-like (pj/lay-bar :drv) (pj/lay-point :drv [:hwy :cty]))))))
    (testing "a histogram keeps its counts beside a series"
      (is (= (reduce + (bins (pj/lay-histogram mpg-like :displ)))
             (reduce + (bins (-> mpg-like (pj/lay-histogram :displ)
                                 (pj/lay-line :displ [:hwy :cty])))))))
    (testing "a layer reading the pose's column on the series' axis keeps its rows"
      (let [ys (fn [pose] (-> (pj/plan pose) :panels first :layers first
                              :groups (->> (mapv (comp vec :ys)))))
            base (pj/pose mpg-like :drv :displ)]
        (is (= (ys (pj/lay-bar base :drv))
               (ys (-> base (pj/lay-bar :drv) (pj/lay-point :drv [:hwy :cty])))))))
    (testing "a layer naming no column still reads the series' rows"
      (is (= 12 (-> (pj/plan (-> mpg-like (pj/lay-line :displ [:hwy :cty]) (pj/lay-point)))
                    :panels first :layers second :groups
                    (->> (map (comp count :xs)) (reduce +))))))))

(deftest series-pairs-title-each-column-once-test
  ;; Four pairs from two columns on each axis: each axis names its two
  ;; columns once, not once per pair.
  (let [pose (pj/lay-point {:a [1 2 3] :b [2 3 4] :c [5 6 7] :d [1 1 2]}
                           [:a :a :b :b] [:c :d :c :d])
        plan (pj/plan pose)]
    (is (= "a, b" (:x-label plan)))
    (is (= "c, d" (:y-label plan)))))

(deftest a-series-column-named-again-is-reported-before-the-pivot-test
  ;; The message names the columns the writer wrote, not the key and
  ;; value columns the pivot invents.
  (let [msg (fn [f] (try (pj/plan (f)) nil (catch clojure.lang.ExceptionInfo e (ex-message e))))]
    (testing "on :x of the same layer"
      (let [m (msg #(pj/lay-point sales :revenue [:revenue :cost]))]
        (is (re-find #"names :revenue on :x" m))
        (is (not (re-find #":value" m)))))
    (testing "on :x of the pose"
      (is (re-find #"names :revenue on :x"
                   (msg #(pj/pose sales :revenue [:revenue :cost])))))))
