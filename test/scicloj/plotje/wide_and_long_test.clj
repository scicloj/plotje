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

  (testing "the pivot happens on the pose, and everything after is ordinary"
    (let [pose (-> sales (pj/lay-bar :quarter [:revenue :cost]))]
      (is (= {:x :quarter :y :value} (:mapping pose)))
      (is (= [:quarter :series :value] (vec (tc/column-names (:data pose)))))
      (is (= 8 (tc/row-count (:data pose))))))

  (testing "the columns it reads are consumed; the rest come through"
    (let [pose (-> with-target (pj/lay-bar :quarter [:revenue :cost]))
          cols (set (tc/column-names (:data pose)))]
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
    (is (contains? (set (:texts (pj/svg-summary
                                 (-> sales (pj/lay-bar :quarter [:revenue :cost])))))
                   "series")
        "unnamed, the key column is :series"))

  (testing "the value column titles the value axis, and :y-label renames it"
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

  (testing "two overlaid layers carry no label, so a dodge has nothing to divide"
    (let [ls (-> (pj/plan (-> sales
                              pj/overlay
                              (pj/lay-bar :quarter :revenue {:position :dodge})
                              (pj/lay-bar :quarter :cost {:position :dodge})))
                 :panels first :layers)]
      (is (= [[1 [""]] [1 [""]]]
             (mapv (fn [l] [(:n-groups (:dodge-ctx l)) (mapv :label (:groups l))]) ls))
          "the label is the empty string, not a missing one"))))

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
  (testing "a vector of pairs is still the multi-panel form"
    (is (= 2 (:panels (pj/svg-summary
                       (-> sales (pj/lay-point [[:quarter :revenue]
                                                [:quarter :cost]])))))))

  (testing "two parallel vectors are still paired panels"
    (is (= 2 (:panels (pj/svg-summary
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

  (testing "two series in one call have no shared shape"
    ;; Reachable only from the options map: two vectors in the two
    ;; positional slots are the older paired-panels form, and that
    ;; branch is read first.
    (is (thrown-with-msg? Exception #"more than one"
                          (-> sales (pj/lay-point {:x [:revenue :cost]
                                                   :y [:cost :revenue]})))))

  (testing "a column the data does not have is named, with the ones it does"
    (is (thrown-with-msg? Exception #"does not have \[:nope\]"
                          (-> sales (pj/lay-bar :quarter [:revenue :nope])))))

  (testing "one column is not several"
    (is (thrown-with-msg? Exception #"1 column in a slot"
                          (-> sales (pj/lay-bar :quarter {:series [:revenue]})))))

  (testing "the invented columns are not written over in silence"
    (is (thrown-with-msg? Exception #"already has \[:value\]"
                          (-> (assoc sales :value [1 2 3 4])
                              (pj/lay-bar :quarter [:revenue :cost])))))

  (testing "a mapping already naming a consumed column is named, not broken"
    (is (thrown-with-msg? Exception #"consumes the columns it reads"
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

;; ---- The panel split says what it did ----

(defn- note-of
  "What a pose says when it is drawn. The split is settled at draft
   time, where `:overlay` is read, so the note is said there -- capturing
   it around the `lay-*` calls would catch nothing."
  [pose]
  (with-out-str (pj/plot pose)))

(deftest the-panel-split-reports-itself-test
  (testing "two layers disagreeing about an axis get a panel each, and say so"
    (let [out (note-of (-> sales (pj/lay-point :quarter :revenue)
                           (pj/lay-point :quarter :cost)))]
      (is (re-find #"panel of its own" out))
      (is (re-find #"pj/overlay" out) "one onward route")
      (is (re-find #"\[:revenue :cost\]" out) "and the other, spelled")))

  (testing "nothing is said where the layers agree"
    (is (= "" (note-of (-> sales (pj/lay-point :quarter :revenue)
                           (pj/lay-line :quarter :revenue))))))

  (testing "nothing is said under pj/overlay, which asked for one panel"
    (is (= "" (note-of (-> sales pj/overlay
                           (pj/lay-point :quarter :revenue)
                           (pj/lay-point :quarter :cost))))))

  ;; Said at draft rather than at the call, so a pj/overlay written after
  ;; the layers is read before the note is decided. Said at the call, the
  ;; note stated an outcome that pj/overlay then changed.
  (testing "nothing is said where pj/overlay comes after the layers"
    (is (= "" (note-of (-> sales (pj/lay-point :quarter :revenue)
                           (pj/lay-point :quarter :cost)
                           pj/overlay)))))

  (testing "nothing is said where the layer opts itself in"
    (is (= "" (note-of (-> sales (pj/lay-point :quarter :revenue)
                           (pj/lay-point :quarter :cost {:overlay true}))))))

  ;; The series route is named only where following it draws. The note
  ;; used to name it on every split, and both cases below reported a
  ;; missing column or left a disagreement standing.
  (testing "the series route is not offered where the layer brings its own data"
    (let [out (note-of
               (-> {:fitted [1 2 3] :residual [1 2 3]}
                   (pj/lay-point :fitted :residual)
                   (pj/lay-point :x :y {:data (tc/dataset {:x [1 2 3] :y [1 2 3]})})))]
      (is (re-find #"panel of its own" out))
      (is (re-find #"pj/overlay" out) "the route that does apply")
      (is (not (re-find #"series" out))
          "neither dataset carries both columns, so a series reports rather than draws")))

  (testing "the series route is not offered where both axes disagree"
    (let [wide (assoc sales :units [3 4 5 6])
          out (note-of (-> wide (pj/lay-point :revenue :cost)
                           (pj/lay-point :quarter :units)))]
      (is (re-find #"panel of its own" out))
      (is (not (re-find #"series" out))
          "a call takes one series, so the second disagreement would stand")))

  (testing "the route the note names is the one that draws"
    (is (= 1 (:panels (pj/svg-summary
                       (-> sales (pj/lay-line :quarter [:revenue :cost]))))))))

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
