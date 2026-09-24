(ns scicloj.plotje.per-row-color-test
  "A mark drawn once per row wears that row's colour.

   A numeric `:color` column is a gradient. Until this, only `:point`
   and `:interval-h` painted it: every other mark drew one colour per
   group and ignored the column, while the legend beside it showed a
   gradient -- so the plot explained an encoding the marks did not
   carry. The marks that draw one element per row now read the same
   per-row buffer the point has always read."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def rows
  {:x   [1 2 3 4 5 6]
   :y   [1 4 9 16 25 36]
   :n   [0.0 0.2 0.4 0.6 0.8 1.0]
   :lo  [0 3 8 14 22 32]
   :hi  [2 5 10 18 28 40]
   :lab ["a" "b" "c" "d" "e" "f"]
   :cat ["a" "b" "c" "d" "e" "f"]})

(defn- colours
  "Distinct colours drawn on `tag` elements under attribute `k`."
  [pose tag k]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= tag (first %)) (map? (second %))))
       (keep #(get (second %) k))
       (remove #{"none" "rgb(232,232,232)"})
       distinct
       count))

(defn- extra-colours
  "How many colours the mark gains from a numeric `:color` column, over
   the same plot drawn in one written colour. Taken as a difference
   because axis lines and tick labels are drawn on the same tags, and a
   written colour holds every one of them fixed."
  [pose-fn tag k]
  ;; The legend is turned off in both: a gradient bar is drawn from many
  ;; small rects, which would otherwise count as the point mark's own.
  (let [drawn (fn [opts] (-> (pose-fn opts)
                             (pj/options {:legend-position :none})
                             (colours tag k)))]
    (- (drawn {:color :n}) (drawn {:color "#377eb8"}))))

(deftest a-mark-drawn-once-per-row-paints-the-gradient-test
  ;; Six rows, so five colours beyond the single one a written colour draws.
  (testing "a value bar wears its own row's colour"
    (is (= 5 (extra-colours #(pj/lay-bar rows :cat :y %) :polygon :fill))))
  (testing "so does a bar at a numeric position"
    (is (= 5 (extra-colours #(pj/lay-bar rows :x :y %) :polygon :fill))))
  (testing "and a point, which has always read the buffer"
    (is (= 5 (extra-colours #(pj/lay-point rows :x :y %) :rect :fill))))
  (testing "text takes its colour per row"
    (is (= 5 (extra-colours #(pj/lay-text rows :x :y (assoc % :text :lab))
                            :text :fill))))
  (testing "a rug's ticks do"
    (is (= 5 (extra-colours #(-> rows (pj/lay-point :x :y) (pj/lay-rug %))
                            :polyline :stroke))))
  (testing "an errorbar does"
    (is (= 5 (extra-colours #(pj/lay-errorbar rows :x :y
                                              (assoc % :y-min :lo :y-max :hi))
                            :polyline :stroke))))
  (testing "and a lollipop's stems do"
    (is (= 5 (extra-colours #(pj/lay-lollipop rows :cat :y %) :polyline :stroke)))))

(deftest a-mark-drawn-once-for-many-rows-keeps-one-colour-test
  ;; The other half of the rule, and why the warning still exists: one
  ;; path drawn from six rows has no row to read.
  (testing "a line gains nothing from a numeric colour"
    (is (zero? (extra-colours #(pj/lay-line rows :x :y %) :polyline :stroke))))
  (testing "nor does an area"
    (is (zero? (extra-colours #(pj/lay-area rows :x :y %) :polygon :fill))))
  (testing "nor a bar counting a category, which has no row to read"
    (is (zero? (extra-colours #(pj/lay-bar rows :cat %) :polygon :fill)))))

(deftest a-categorical-colour-still-groups-test
  ;; Grouping is untouched: a categorical column still splits the data,
  ;; which is what a per-group stat is built on.
  (testing "a line splits into one line per category"
    (is (= 5 (- (colours (-> rows (pj/lay-line :x :y {:color :cat})
                             (pj/options {:legend-position :none}))
                         :polyline :stroke)
                (colours (-> rows (pj/lay-line :x :y {:color "#377eb8"})
                             (pj/options {:legend-position :none}))
                         :polyline :stroke))))))

(deftest a-drawn-color-column-does-not-group-test
  ;; The rule this release is for: `:scale false` says the column holds
  ;; colors to draw rather than data to read through a scale, so no
  ;; categories come out of it -- no groups, no dodge, no stat per color.
  (let [drawn {:cat ["a" "b" "c"] :v [10 20 30]
               :c ["red" "blue" "green"]}
        layer (fn [pose] (-> pose pj/plan :panels first :layers first))]
    (testing "one group, and the colors ride on it per row"
      (let [l (layer (pj/lay-bar drawn :cat :v {:color {:column :c :scale false}}))]
        (is (= 1 (count (:groups l))))
        (is (= 3 (count (:colors (first (:groups l))))))))
    (testing "three bars, each in its own written color"
      (is (= 3 (colours (pj/lay-bar drawn :cat :v
                                    {:color {:column :c :scale false}})
                        :polygon :fill))))
    (testing "while the same column read through the scale still groups"
      (let [l (layer (pj/lay-bar drawn :cat :v {:color :c}))]
        (is (= 3 (count (:groups l))))
        (is (nil? (:colors (first (:groups l)))))))
    (testing "interval-h reads it too, as it always has for a gradient"
      ;; It kept its own copy of the gradient computation and so never
      ;; learned about drawn colors; the check above then refused a mark
      ;; that can draw them. One helper answers for every mark now.
      (let [gantt {:task ["a" "b" "c"] :start [1 2 3] :end [4 5 6]
                   :c ["red" "blue" "green"]}]
        (is (= 3 (colours (pj/lay-interval-h gantt :start :task
                                             {:x-end :end
                                              :color {:column :c :scale false}})
                          :polygon :fill)))))

    (testing "and a mark drawn from many rows is refused, naming the way out"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"cannot read one.*drop the :scale"
           (pj/plot (pj/lay-line drawn :v :v {:color {:column :c :scale false}})
                    {:format :svg}))))))

(deftest a-tile-fill-legend-is-titled-by-its-column-test
  ;; #58: the bar was titled `fill` wherever a tile read a column.
  (let [d {:x ["a" "b" "a" "b"] :y ["p" "p" "q" "q"] :corr [1.0 -0.6 0.33 0.9]}
        texts #(set (:texts (pj/svg-summary %)))]
    (is (contains? (texts (pj/lay-tile d :x :y {:fill :corr})) "corr"))
    (is (not (contains? (texts (pj/lay-tile d :x :y {:fill :corr})) "fill")))
    (is (contains? (texts (-> (pj/lay-tile d :x :y {:fill :corr})
                              (pj/options {:fill-label "Correlation"})))
                   "Correlation"))
    (is (contains? (texts (-> (pj/lay-tile d :x :y {:fill :corr})
                              (pj/scale :fill {:label "L"})))
                   "L"))))

(deftest a-tile-reading-a-numeric-color-is-not-warned-about-test
  ;; A tile carries its colour on each of its `:tiles`, a shape the
  ;; monochrome check did not read, so it warned while the gradient drew.
  (let [d {:x ["a" "b" "a" "b"] :y ["p" "p" "q" "q"] :corr [1.0 -0.6 0.33 0.9]}]
    (is (not (re-find #"do not read the column as a gradient"
                      (with-out-str (pj/plan (pj/lay-tile d :x :y {:color :corr}))))))))

(deftest a-fill-legend-reads-the-fill-chain-test
  ;; #58: the title of a legend drawn in a fill falls back the way its
  ;; range and midpoint already did -- a `:fill` spec, a `:color` spec,
  ;; `:fill-label`, then `:color-label`.
  (let [d {:x ["a" "b" "a" "b"] :y ["p" "p" "q" "q"] :corr [1.0 -0.6 0.33 0.9]}
        title (fn [p] (get-in (pj/plan p) [:legend :title]))]
    (is (= "CL" (title (-> (pj/lay-tile d :x :y {:fill :corr}) (pj/options {:color-label "CL"})))))
    (is (= "FL" (title (-> (pj/lay-tile d :x :y {:fill :corr})
                           (pj/options {:color-label "CL" :fill-label "FL"})))))
    (is (= "SC" (title (-> (pj/lay-tile d :x :y {:fill :corr}) (pj/scale :color {:label "SC"})))))
    (testing "a tile reading :color is titled and painted through the same chain"
      (is (= "FL" (title (-> (pj/lay-tile d :x :y {:color :corr}) (pj/options {:fill-label "FL"})))))
      ;; The cells were viridis and the bar the default blues.
      (let [stops #(mapv :color (get-in (pj/plan %) [:legend :stops]))]
        (is (= (stops (-> (pj/lay-tile d :x :y {:fill :corr}) (pj/options {:fill-range :viridis})))
               (stops (-> (pj/lay-tile d :x :y {:color :corr}) (pj/options {:fill-range :viridis})))))))
    (testing "a contour is coloured through :color, and titled through it"
      (let [iris {:a [1 2 3 2 1 3 2 2] :b [1 3 2 2 1 3 1 3]}]
        (is (= "CL" (title (-> (pj/lay-contour iris :a :b) (pj/options {:color-label "CL"})))))
        (is (not= "SF" (title (-> (pj/lay-contour iris :a :b) (pj/scale :fill {:label "SF"})))))))))

(deftest a-colour-option-nothing-reads-warns-test
  (let [d {:x ["a" "b"] :y ["p" "q"] :v [1.0 2.0]}
        out (fn [p] (with-out-str (pj/plan p)))]
    (is (re-find #":fill-label \"FL\" titles the legend of a mark drawn in a fill"
                 (out (-> (pj/lay-point d :x :y {:color :v}) (pj/options {:fill-label "FL"})))))
    (is (re-find #":fill-range :viridis is the gradient of a mark drawn in a fill"
                 (out (-> (pj/lay-point d :x :y {:color :v}) (pj/options {:fill-range :viridis})))))
    (is (re-find #":color-values .* colours no categories"
                 (out (-> (pj/lay-point d :x :y {:color :v}) (pj/options {:color-values ["red"]})))))
    (is (re-find #":color-label \"CL\" titles a colour or fill legend, and this plot draws none"
                 (out (-> (pj/lay-point d :x :y) (pj/options {:color-label "CL"})))))
    (testing "an option something reads says nothing"
      (is (not (re-find #"Warning"
                        (out (-> (pj/lay-tile d :x :y {:fill :v})
                                 (pj/options {:fill-label "FL" :fill-range :viridis
                                              :color-label "CL"})))))))))
