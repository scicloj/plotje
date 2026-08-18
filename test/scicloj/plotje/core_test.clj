(ns scicloj.plotje.core-test
  "Hand-written unit tests for plotje core logic."
  (:require [clojure.test :refer [deftest testing is are]]
            [tablecloth.api :as tc]
            [java-time.api :as jt]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.stat :as stat]
            [scicloj.plotje.impl.scale :as scale]
            [scicloj.plotje.impl.position :as position]
            [scicloj.plotje.impl.extract :as extract]
            [scicloj.plotje.impl.resolve :as resolve]
            [scicloj.plotje.layer-type :as layer-type]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ============================================================
;; defaults.clj
;; ============================================================

(deftest hex->rgba-test
  (testing "6-digit hex"
    (let [[r g b a] (defaults/hex->rgba "#FF0000")]
      (is (== 1.0 r))
      (is (== 0.0 g))
      (is (== 0.0 b))
      (is (== 1.0 a))))
  (testing "8-digit hex with alpha"
    (let [[_ _ _ a] (defaults/hex->rgba "#FF000080")]
      (is (< 0.3 a 0.6))))
  (testing "3-digit shorthand"
    (let [[r g b _] (defaults/hex->rgba "#F00")]
      (is (== 1.0 r))
      (is (== 0.0 g))))
  (testing "without #"
    (let [[r _ _ _] (defaults/hex->rgba "00FF00")]
      (is (== 0.0 r)))))

(deftest color-for-test
  (testing "index-based default palette"
    (let [cats ["a" "b" "c"]
          c1 (defaults/color-for cats "a")
          c2 (defaults/color-for cats "b")]
      (is (= 4 (count c1)))
      (is (not= c1 c2))))
  (testing "palette as map"
    (let [cats ["a" "b"]
          c (defaults/color-for cats "a" {"a" "#FF0000"})]
      (is (== 1.0 (first c)))))
  (testing "palette as vector"
    (let [cats ["x" "y"]
          c (defaults/color-for cats "x" ["#00FF00" "#0000FF"])]
      (is (== 0.0 (first c)))
      (is (== 1.0 (second c)))))
  (testing "wrap-around index"
    (let [cats (mapv str (range 20))
          c (defaults/color-for cats "0")
          c2 (defaults/color-for cats "15")]
      (is (= 4 (count c)))
      (is (= 4 (count c2))))))

(deftest fixed-color-survives-grouping-test
  (testing "a literal :color applies to every group made by :group"
    ;; Grouping fills each group's color slot with the group key. A fixed
    ;; color has to win over that, or "one line per series, all in one
    ;; color" cannot be said -- the pale background behind a few named
    ;; series in the Cookbook's annotation recipes.
    (let [grey [0.8 0.8 0.8 1.0]
          colors (fn [pose]
                   (->> (pj/plan pose) :panels first :layers first :groups
                        (mapv :color)))]
      (is (= [grey grey]
             (colors (pj/lay-line {:g ["a" "a" "b" "b"] :x [1 2 1 2] :y [1 2 2 3]}
                                  :x :y {:group :g :color "#cccccc"})))
          "grouping discarded the fixed color")
      (testing "and one group still gets it"
        (is (= [grey]
               (colors (pj/lay-line {:x [1 2] :y [1 2]} :x :y {:color "#cccccc"})))))
      (testing "while a :color column still drives the palette"
        (let [cs (colors (pj/lay-line {:g ["a" "a" "b" "b"] :x [1 2 1 2] :y [1 2 2 3]}
                                      :x :y {:color :g}))]
          (is (= 2 (count cs)))
          (is (apply not= cs)))))))

(deftest gradient-color-test
  (testing "t=0.0 (dark blue — low end)"
    (let [[r g b a] (defaults/gradient-color 0.0)]
      (is (< r 0.1))
      (is (< b 0.3))
      (is (== 1.0 a))))
  (testing "t=1.0 (light blue — high end)"
    (let [[r g b _] (defaults/gradient-color 1.0)]
      (is (> b 0.9))
      (is (> g 0.6))))
  (testing "t=0.5 (mid blue)"
    (let [[r g b _] (defaults/gradient-color 0.5)]
      (is (> b 0.5))
      (is (> g 0.3))
      (is (< r 0.3))))
  (testing "clamping"
    (is (= (defaults/gradient-color -1.0) (defaults/gradient-color 0.0)))
    (is (= (defaults/gradient-color 2.0) (defaults/gradient-color 1.0)))))

(deftest legend-serializable-test
  (testing "continuous legend has :color-range keyword, no :gradient-fn"
    (let [ds (tc/dataset {:x (range 50) :y (range 50) :c (range 50)})
          pl (pj/plan (-> ds (pj/lay-point :x :y {:color :c})))
          legend (:legend pl)]
      (is (= :continuous (:type legend)))
      (is (contains? legend :color-range))
      (is (not (contains? legend :gradient-fn)))
      (is (nil? (:color-range legend)) "default color range is nil")))
  (testing "explicit :color-range is stored as keyword"
    (let [ds (tc/dataset {:x (range 50) :y (range 50) :c (range 50)})
          pl (pj/plan (-> ds (pj/lay-point :x :y {:color :c}))
                      {:color-range :inferno})
          legend (:legend pl)]
      (is (= :inferno (:color-range legend)))
      (is (false? (:range-from-spec? legend)))))
  (testing "a range from a scale spec is marked as such, so render-time"
    ;; configuration does not repaint a legend the spec decided.
    (let [ds (tc/dataset {:x (range 50) :y (range 50) :c (range 50)})
          legend (-> ds
                     (pj/lay-point :x :y {:color :c})
                     (pj/scale :color {:range :inferno})
                     pj/plan :legend)]
      (is (= :inferno (:color-range legend)))
      (is (true? (:range-from-spec? legend)))))
  (testing "legend has 20 pre-computed stops"
    (let [ds (tc/dataset {:x (range 50) :y (range 50) :c (range 50)})
          pl (pj/plan (-> ds (pj/lay-point :x :y {:color :c})))
          legend (:legend pl)]
      (is (= 20 (count (:stops legend))))
      (is (== 0.0 (:t (first (:stops legend)))))
      (is (== 1.0 (:t (last (:stops legend))))))))

(deftest fmt-name-test
  (testing "keywords lose their separators"
    (is (= "sepal length" (defaults/fmt-name :sepal_length)))
    (is (= "sepal length" (defaults/fmt-name :sepal-length)))
    (is (= "x" (defaults/fmt-name :x))))
  (testing "symbols lose theirs too -- a symbol cannot hold a space either"
    (is (= "sepal length" (defaults/fmt-name 'sepal-length)))
    (is (= "sepal length" (defaults/fmt-name 'sepal_length))))
  (testing "a string is left as written -- a string could have held a space"
    (is (= "sepal_length" (defaults/fmt-name "sepal_length")))
    (is (= "Cost-Benefit Ratio" (defaults/fmt-name "Cost-Benefit Ratio"))))
  (testing "a name that is neither formats as its printed form"
    (is (= "0" (defaults/fmt-name 0)))
    (is (= "" (defaults/fmt-name nil)))))

(deftest fmt-category-label-test
  (testing "a keyword category reads as words, like a column name does"
    (is (= "setosa" (defaults/fmt-category-label :setosa)))
    (is (= "not applicable" (defaults/fmt-category-label :not-applicable)))
    (is (= "north east" (defaults/fmt-category-label :north_east))))
  (testing "a string category is left as written"
    (is (= "2026-07-31" (defaults/fmt-category-label "2026-07-31")))
    (is (= "Cost-Benefit Ratio" (defaults/fmt-category-label "Cost-Benefit Ratio"))))
  (testing "other values format as their printed form"
    (is (= "-5" (defaults/fmt-category-label -5)))
    (is (= "" (defaults/fmt-category-label nil)))))

(deftest categories-differing-only-by-separator-are-combined-with-a-warning-test
  ;; Two keyword categories that differ only by separator format alike. On a
  ;; categorical axis that is a data transform, not a relabelling, so they
  ;; land in one band -- rare enough to be worth a warning rather than a rule.
  (let [ds (tc/dataset {:cat [:a-b :a_b :a-b :a_b] :v [1 2 3 4]})
        ticks (atom nil)
        out (with-out-str
              (reset! ticks
                      (-> ds (pj/lay-bar :cat :v) pj/plan :panels first :x-ticks)))]
    (is (= ["a b"] (:values (deref ticks))))
    (is (= ["a b"] (:labels (deref ticks))))
    (is (re-find #"Warning: categories" out))
    (is (re-find #":a-b" out))
    (is (re-find #":a_b" out))
    (is (re-find #"Convert column :cat to strings" out)))

  (testing "a category on :color is only relabelled -- the groups survive"
    ;; The colour path does not map the column, so the two keep their own bars
    ;; and only the legend text repeats.
    (let [pl (-> {:x [1 2 3 4] :y [1 2 1 2] :c [:a-b :a_b :a-b :a_b]}
                 (pj/lay-bar :x :y {:color :c})
                 pj/plan)]
      (is (= ["a b" "a b"] (mapv :label (:entries (:legend pl)))))))

  (testing "no warning when the categories are distinguishable"
    (is (= "" (with-out-str
                (-> (tc/dataset {:cat [:a-b :c-d] :v [1 2]})
                    (pj/lay-bar :cat :v)
                    pj/plan))))))

(deftest integer-column-names-auto-label-test
  ;; A dataset built without column names gets integer ones; auto-labelling
  ;; used to throw on them (issue reported by Timothy Pratley, PR #32).
  (let [pl (-> (tc/dataset [[1 2] [3 4]]) pj/plan)]
    (is (= "0" (:x-label pl)))
    (is (= "1" (:y-label pl)))))

(deftest string-column-names-auto-label-test
  ;; A string name titles its axis as written. Substituting its separators
  ;; would corrupt a name that meant them, which keyword names cannot express.
  (let [pl (-> (tc/dataset {"sepal_length" [1 2 3] "Cost-Benefit Ratio" [4 5 6]})
               (pj/lay-point "sepal_length" "Cost-Benefit Ratio")
               pj/plan)]
    (is (= "sepal_length" (:x-label pl)))
    (is (= "Cost-Benefit Ratio" (:y-label pl)))))

;; ============================================================
;; stat.clj
;; ============================================================

(def tiny-ds (tc/dataset {:x [1 2 3 4 5] :y [2 4 6 8 10]}))
(def cat-ds (tc/dataset {:cat ["a" "a" "b" "b" "b"] :val [1 2 3 4 5]}))

(deftest compute-stat-identity-test
  (let [view {:mark :point :data tiny-ds :x :x :y :y :x-type :numerical :y-type :numerical}
        result (stat/compute-stat (assoc view :cfg defaults/defaults))]
    (is (seq (:points result)))
    (is (= 5 (count (:xs (first (:points result))))))))

(deftest compute-stat-bin-test
  (let [view {:mark :bar :stat :bin :data tiny-ds :x :x :x-type :numerical
              :cfg defaults/defaults}
        result (stat/compute-stat view)]
    (is (= 1 (count (:bins result))))
    (is (pos? (count (:bin-maps (first (:bins result))))))))

(deftest compute-stat-count-test
  (let [view {:mark :rect :stat :count :data cat-ds :x :cat :x-type :categorical
              :cfg defaults/defaults}
        result (stat/compute-stat view)]
    (is (= ["a" "b"] (vec (:categories result))))
    (is (seq (:bars result)))
    ;; counts is a vector of {:category "a" :count N} maps
    (let [counts (:counts (first (:bars result)))]
      (is (= 2 (:count (first (filter #(= "a" (:category %)) counts)))))
      (is (= 3 (:count (first (filter #(= "b" (:category %)) counts))))))))

(deftest compute-stat-lm-test
  (testing "perfect linear data y=2x"
    (let [view {:mark :line :stat :linear-model :data tiny-ds :x :x :y :y
                :x-type :numerical :cfg defaults/defaults}
          result (stat/compute-stat view)
          line (first (:lines result))]
      (is line)
      (is (< (:x1 line) (:x2 line)))
      (is (< (Math/abs (- (:y1 line) 2.0)) 0.01))
      (is (< (Math/abs (- (:y2 line) 10.0)) 0.01))))
  (testing "n=2 produces nil (needs >= 3)"
    (let [ds2 (tc/dataset {:x [1 2] :y [3 4]})
          view {:mark :line :stat :linear-model :data ds2 :x :x :y :y
                :x-type :numerical :cfg defaults/defaults}
          result (stat/compute-stat view)]
      (is (empty? (:lines result))))))

(deftest compute-stat-kde-test
  (let [view {:mark :area :stat :density :data tiny-ds :x :x :x-type :numerical
              :cfg defaults/defaults}
        result (stat/compute-stat view)]
    (is (seq (:points result)))
    (is (> (count (:xs (first (:points result)))) 10))))

(deftest compute-stat-boxplot-test
  (let [ds (tc/dataset {:cat ["a" "a" "a" "a" "a" "a"]
                        :val [1.0 2.0 3.0 4.0 5.0 100.0]})
        view {:mark :boxplot :stat :boxplot :data ds :x :cat :y :val
              :x-type :categorical :cfg defaults/defaults}
        result (stat/compute-stat view)
        box (first (:boxes result))]
    (is box)
    (is (= "a" (:category box)))
    (is (<= (:q1 box) (:median box) (:q3 box)))
    (is (seq (:outliers box)))))

(deftest compute-stat-summary-test
  (let [ds (tc/dataset {:cat ["a" "a" "a" "b" "b" "b"]
                        :val [10.0 20.0 30.0 40.0 50.0 60.0]})
        view {:mark :pointrange :stat :summary :data ds :x :cat :y :val
              :x-type :categorical :cfg defaults/defaults}
        result (stat/compute-stat view)]
    (is (seq (:points result)))
    (let [p (first (:points result))]
      (is (= 2 (count (:xs p))))
      ;; mean of [10 20 30] = 20, mean of [40 50 60] = 50
      (is (< (Math/abs (- (first (:ys p)) 20.0)) 0.01))
      (is (< (Math/abs (- (second (:ys p)) 50.0)) 0.01)))))

(deftest compute-stat-bin2d-test
  (let [ds (tc/dataset {:x (range 100) :y (range 100)})
        view {:mark :tile :stat :bin2d :data ds :x :x :y :y
              :x-type :numerical :y-type :numerical :cfg defaults/defaults}
        result (stat/compute-stat view)]
    (is (seq (:tiles result)))
    (is (= #{:x-lo :x-hi :y-lo :y-hi :fill} (set (tc/column-names (:tiles result)))))))

;; ============================================================
;; position.clj
;; ============================================================

(deftest apply-positions-identity-test
  (let [layers [{:mark :point :groups [{:xs [1 2] :ys [3 4]}]}]]
    (is (= layers (position/apply-positions layers)))))

(deftest apply-positions-dodge-test
  (let [layers [{:mark :rect :position :dodge
                 :categories ["a" "b"]
                 :groups [{:label "g1" :counts [{:category "a" :count 10} {:category "b" :count 20}]}
                          {:label "g2" :counts [{:category "a" :count 30} {:category "b" :count 40}]}]}]
        result (position/apply-positions layers)
        layer (first result)]
    (is (:dodge-ctx layer))
    (is (= 2 (:n-groups (:dodge-ctx layer))))
    (is (= 0 (:dodge-idx (first (:groups layer)))))
    (is (= 1 (:dodge-idx (second (:groups layer)))))))

(deftest apply-positions-stack-test
  (let [layers [{:mark :rect :position :stack
                 :categories ["a" "b"]
                 :groups [{:label "g1" :counts [{:category "a" :count 10} {:category "b" :count 20}]}
                          {:label "g2" :counts [{:category "a" :count 30} {:category "b" :count 40}]}]}]
        result (position/apply-positions layers)
        g2-counts (:counts (second (:groups (first result))))]
    ;; g2 stacked on g1: y0 should be g1's count
    (is (= 10.0 (:y0 (first g2-counts))))
    (is (= 40.0 (:y1 (first g2-counts))))))

(deftest apply-positions-fill-test
  (let [layers [{:mark :rect :position :fill
                 :categories ["a" "b"]
                 :groups [{:label "g1" :counts [{:category "a" :count 10} {:category "b" :count 20}]}
                          {:label "g2" :counts [{:category "a" :count 30} {:category "b" :count 40}]}]}]
        result (position/apply-positions layers)
        g1-counts (:counts (first (:groups (first result))))
        g2-counts (:counts (second (:groups (first result))))]
    ;; fill normalizes: cat "a": g1=10/(10+30)=0.25
    (is (< (Math/abs (- (:y1 (first g1-counts)) 0.25)) 0.01))
    ;; g2 top = 1.0
    (is (< (Math/abs (- (:y1 (first g2-counts)) 1.0)) 0.01))))

(deftest cross-layer-dodge-test
  (let [layers [{:mark :rect :position :dodge
                 :categories ["a" "b"]
                 :groups [{:label "g1" :counts [{:category "a" :count 10} {:category "b" :count 20}]}
                          {:label "g2" :counts [{:category "a" :count 30} {:category "b" :count 40}]}]}
                {:mark :errorbar :position :dodge
                 :groups [{:label "g1" :xs ["a" "b"] :ys [10 20]
                           :ymins [8 18] :ymaxs [12 22]}]}]
        result (position/apply-positions layers)]
    ;; Both layers should share same n-groups
    (is (= (:n-groups (:dodge-ctx (first result)))
           (:n-groups (:dodge-ctx (second result)))))))

(deftest count-stat-x-equals-color-test
  (testing "count stat when x and color map to the same column"
    (let [pl (-> {:species ["setosa" "setosa" "versicolor" "versicolor"]}
                 (pj/lay-bar :species {:color :species})
                 pj/plan)
          groups (get-in pl [:panels 0 :layers 0 :groups])]
      ;; Each group should only have non-zero count for its own species
      (doseq [g groups]
        (doseq [{:keys [category count]} (:counts g)]
          (if (= category (:label g))
            (is (pos? count) (str (:label g) " should have count for " category))
            (is (zero? count) (str (:label g) " should have 0 count for " category))))))))

(deftest stacked-bars-y0s-test
  (testing "stacked value bars use y0s baselines"
    (let [pl (-> {:day ["Mon" "Mon"] :count [30 20] :meal ["lunch" "dinner"]}
                 (pj/lay-bar :day :count {:color :meal :position :stack})
                 pj/plan)
          groups (get-in pl [:panels 0 :layers 0 :groups])
          dinner (second groups)]
      ;; dinner baseline should equal lunch value (30)
      (is (= [30.0] (vec (:y0s dinner)))))))

;; ============================================================
;; scale.clj
;; ============================================================

(deftest make-scale-linear-test
  (let [s (scale/make-scale [0 100] [0 500] {})]
    (is (== 0 (s 0)))
    (is (== 500 (s 100)))
    (is (== 250 (s 50)))))

(deftest make-scale-categorical-test
  (let [s (scale/make-scale ["a" "b" "c"] [0 300] {})]
    (is (some? s))
    (is (pos? (wadogo.scale/data s :bandwidth)))))

(deftest make-scale-categorical-n-ticks-test
  (testing ":n-ticks thins a categorical band scale to roughly n evenly-spaced ticks"
    ;; The count is asked for at the tick call rather than built into
    ;; the scale, so one band scale answers both questions and the
    ;; numeric and categorical axes read `:n-ticks` the same way.
    (let [s (scale/make-scale (mapv str (range 50)) [0 300] {})]
      (is (= 50 (count (wadogo.scale/ticks s))))
      (is (= 10 (count (wadogo.scale/ticks s 10))))))

  (testing "and a tick count is what :n-ticks names, on either column type"
    (is (= 10 (scale/tick-count 600.0 {:n-ticks 10} 60)))
    (is (= 10 (scale/tick-count 600.0 {} 60)) "or how many fit at that spacing")
    (is (= 2 (scale/tick-count 10.0 {} 60)) "never fewer than two"))
  (testing ":n-ticks flows end-to-end through pj/scale onto a categorical x-axis"
    (let [d (tc/dataset {:x (mapv str (range 50)) :y (range 50)})
          labels (fn [pose] (-> pose pj/plan :panels first :x-ticks :labels))]
      (is (= 50 (count (labels (pj/lay-point d :x :y)))))
      (is (= 10 (count (labels (-> (pj/lay-point d :x :y)
                                   (pj/scale :x {:n-ticks 10}))))))))
  (testing ":n-ticks larger than the category count shows every category, no error"
    (let [d (tc/dataset {:x ["a" "b" "c"] :y [1 2 3]})
          labels (-> (pj/lay-point d :x :y)
                     (pj/scale :x {:n-ticks 10})
                     pj/plan :panels first :x-ticks :labels)]
      (is (= ["a" "b" "c"] (vec labels))))))

(deftest categorical-breaks-labels-test
  (let [xticks (fn [pose] (-> pose pj/plan :panels first :x-ticks))]
    (testing ":breaks selects a category subset and :tick-labels relabels it"
      (let [t (xticks (-> (tc/dataset {:x ["a" "m" "q" "z"] :y [1 2 3 4]})
                          (pj/lay-point :x :y)
                          (pj/scale :x {:breaks ["a" "z"] :tick-labels ["A" "Z"]})))]
        (is (= ["a" "z"] (vec (:values t))))
        (is (= ["A" "Z"] (vec (:labels t))))))
    (testing "a break naming no category is dropped (matched categories remain)"
      (let [t (xticks (-> (tc/dataset {:x (mapv str (range 50)) :y (range 50)})
                          (pj/lay-point :x :y)
                          (pj/scale :x {:breaks ["1" "25" "50"]})))]
        ;; "50" is not a category (domain is "0".."49") -- dropped.
        (is (= ["1" "25"] (vec (:values t))))))
    (testing "explicit :breaks win over :n-ticks (no thinning applied)"
      (let [t (xticks (-> (tc/dataset {:x (mapv str (range 50)) :y (range 50)})
                          (pj/lay-point :x :y)
                          (pj/scale :x {:n-ticks 5 :breaks ["3" "40"]})))]
        (is (= ["3" "40"] (vec (:values t))))))
    (testing "breaks match categories by displayed label (keyword column)"
      (let [t (xticks (-> (tc/dataset {:x [:widget :gadget :gizmo] :y [1 2 3]})
                          (pj/lay-point :x :y)
                          (pj/scale :x {:breaks ["widget" "gizmo"]})))]
        (is (= ["widget" "gizmo"] (mapv str (:labels t))))))))

(deftest make-scale-log-test
  (let [s (scale/make-scale [1 1000] [0 300] {:type :log})]
    (is (== 0 (s 1)))
    (is (== 300 (s 1000)))))

(deftest domain-padding-config-test
  (testing ":domain-padding is read through the config chain, not the defaults map"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-point :sepal-length :sepal-width))
          dom  (fn [p] (-> p pj/plan :panels first :x-domain))]
      ;; The data runs 4.3 to 7.9; the default 5% pads it to [4.12 8.08].
      (is (= [4.12 8.08] (mapv #(-> % (* 100) Math/round (/ 100.0)) (dom pose))))
      ;; No padding leaves the raw extent.
      (is (= [4.3 7.9] (dom (pj/options pose {:domain-padding 0.0}))))
      ;; A plot option and a thread-local binding both reach it.
      (is (= (dom (pj/options pose {:domain-padding 0.5}))
             (pj/with-config {:domain-padding 0.5} (dom pose))))
      (is (< (first (dom (pj/options pose {:domain-padding 0.5}))) 4.12)))))

(deftest pad-domain-test
  (testing "numeric padding"
    (let [[lo hi] (scale/pad-domain [0.0 10.0] {})]
      (is (< lo 0.0))
      (is (> hi 10.0))))
  (testing "log padding stays positive"
    (let [[lo hi] (scale/pad-domain [1.0 100.0] {:type :log})]
      (is (pos? lo))
      (is (> hi 100.0)))))

(deftest format-ticks-test
  (testing "integer ticks"
    (let [s (scale/make-scale [0 10] [0 500] {})
          labels (scale/format-ticks s [0.0 5.0 10.0])]
      (is (= ["0" "5" "10"] labels))))
  (testing "decimal ticks"
    (let [s (scale/make-scale [0 1] [0 500] {})
          labels (scale/format-ticks s [0.0 0.5 1.0])]
      (is (every? string? labels)))))

;; ============================================================
;; extract.clj
;; ============================================================

(deftest resolve-color-test
  ;; The third argument is the resolved draft layer, which answers both
  ;; "is a fixed color set" and "is this layer's color column drawn as
  ;; it stands" -- the two ways a color can arrive already decided.
  (let [cfg (assoc defaults/defaults :color-values nil)]
    (testing "column color"
      (let [c (extract/resolve-color ["a" "b"] "a" {} cfg)]
        (is (= 4 (count c)))))
    (testing "fixed hex color"
      (let [c (extract/resolve-color nil nil {:fixed-color "#FF0000"} cfg)]
        (is (== 1.0 (first c)))))
    (testing "fixed color named by a keyword"
      ;; Reaches here only since the data decides which values name
      ;; columns; before, a keyword was a column reference and nothing else.
      (let [c (extract/resolve-color nil nil {:fixed-color :red} cfg)]
        (is (= [1.0 0.0 0.0 1.0] c))))
    (testing "a drawn color column uses its own value, not the palette"
      (let [c (extract/resolve-color ["#FF0000" "#0000FF"] "#0000FF"
                                     {:color-drawn? true} cfg)]
        (is (= [0.0 0.0 1.0 1.0] c))))
    (testing "the same value scaled, for contrast"
      (let [c (extract/resolve-color ["#FF0000" "#0000FF"] "#0000FF" {} cfg)]
        (is (not= [0.0 0.0 1.0 1.0] c) "a palette entry, not the value itself")))
    (testing "nil falls to default"
      (let [c (extract/resolve-color nil nil {} cfg)]
        (is (= 4 (count c)))))))

(deftest extract-layer-point-test
  (let [view {:mark :point :data tiny-ds :x :x :y :y
              :x-type :numerical :y-type :numerical}
        rv (resolve/resolve-draft-layer view)
        stat-result (stat/compute-stat (assoc rv :cfg defaults/defaults))
        layer (extract/extract-layer rv stat-result [] defaults/defaults)]
    (is (= :point (:mark layer)))
    (is (seq (:groups layer)))
    (is (= 5 (count (:xs (first (:groups layer))))))))

(deftest extract-layer-bar-test
  (let [view {:mark :bar :stat :bin :data tiny-ds :x :x
              :x-type :numerical}
        rv (resolve/resolve-draft-layer view)
        stat-result (stat/compute-stat (assoc rv :cfg defaults/defaults))
        layer (extract/extract-layer rv stat-result [] defaults/defaults)]
    (is (= :bar (:mark layer)))
    (is (seq (:groups layer)))
    (is (seq (:bars (first (:groups layer)))))))

(deftest apply-nudge-test
  (testing "nudge-x shifts xs"
    (let [view {:mark :point :data (tc/dataset {:x [1.0 2.0] :y [3.0 4.0]})
                :x :x :y :y :x-type :numerical :nudge-x 0.5}
          rv (resolve/resolve-draft-layer view)
          stat-result (stat/compute-stat (assoc rv :cfg defaults/defaults))
          layer (extract/extract-layer rv stat-result [] defaults/defaults)]
      (is (= [1.5 2.5] (:xs (first (:groups layer)))))))
  (testing "no nudge is no-op"
    (let [view {:mark :point :data tiny-ds :x :x :y :y :x-type :numerical}
          rv (resolve/resolve-draft-layer view)
          stat-result (stat/compute-stat (assoc rv :cfg defaults/defaults))
          layer (extract/extract-layer rv stat-result [] defaults/defaults)]
      (is (= [1 2 3 4 5] (:xs (first (:groups layer)))))))
  (testing "nudge-x on a categorical x axis throws a clear error, not a cast error"
    (let [view {:mark :text :data (tc/dataset {:cat ["a" "b"] :v [1.0 2.0]})
                :x :cat :y :v :text :v :x-type :categorical :y-type :numerical
                :nudge-x 0.5}
          rv (resolve/resolve-draft-layer view)
          stat-result (stat/compute-stat (assoc rv :cfg defaults/defaults))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #":nudge-x .* categorical x axis.* :align-x"
           (extract/extract-layer rv stat-result [] defaults/defaults)))))
  (testing "nudge-y on a categorical y axis throws a clear error"
    (let [view {:mark :text :data (tc/dataset {:v [1.0 2.0] :cat ["a" "b"]})
                :x :v :y :cat :text :v :x-type :numerical :y-type :categorical
                :nudge-y 0.5}
          rv (resolve/resolve-draft-layer view)
          stat-result (stat/compute-stat (assoc rv :cfg defaults/defaults))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #":nudge-y .* categorical y axis.* :align-y"
           (extract/extract-layer rv stat-result [] defaults/defaults))))))

;; ============================================================
;; layer_type.clj -- mark constructors
;; ============================================================

(deftest mark-constructors-test
  (testing "registry entries have the correct :mark key"
    (are [k mk] (= mk (:mark (layer-type/lookup k)))
      :point :point
      :line :line
      :step :step
      :histogram :bar
      :bar :rect
      :smooth :line
      :text :text
      :label :text
      :area :area
      :density :area
      :tile :tile
      :density-2d :tile
      :contour :contour
      :ridgeline :ridgeline
      :boxplot :boxplot
      :violin :violin
      :rug :rug
      :summary :pointrange
      :errorbar :errorbar
      :lollipop :lollipop)))

;; ============================================================
;; draft->plan (integration)
;; ============================================================

(deftest views-to-plan-test
  (let [views (-> tiny-ds
                  (pj/pose [[:x :y]])
                  pj/lay-point)
        pl (pj/plan views)]
    (is (map? pl))
    (is (contains? pl :panels))
    (is (contains? pl :width))
    (is (contains? pl :height))
    (is (= 1 (count (:panels pl))))
    (let [panel (first (:panels pl))]
      (is (seq (:layers panel)))
      (is (contains? panel :x-domain))
      (is (contains? panel :y-domain)))))

(deftest plan-with-color-test
  (let [ds (tc/dataset {:x [1 2 3 4] :y [1 2 3 4] :g ["a" "a" "b" "b"]})
        views (-> ds (pj/pose [[:x :y]]) (pj/lay-point {:color :g}))
        pl (pj/plan views)]
    (is (:legend pl))
    (is (= 2 (count (:entries (:legend pl)))))))

(deftest plan-faceted-test
  (let [ds (tc/dataset {:x [1 2 3 4 5 6] :y [1 2 3 4 5 6]
                        :g ["a" "a" "b" "b" "c" "c"]})
        views (-> ds (pj/pose [[:x :y]]) (pj/facet :g) pj/lay-point)
        pl (pj/plan views)]
    (is (= 3 (count (:panels pl))))))

(deftest coord-fixed-test
  (testing "coord :fixed end-to-end — equal ranges produce square panel"
    (let [ds (tc/dataset {:x [0 10 5] :y [0 10 5]})
          pl (-> ds (pj/pose :x :y) (pj/coord :fixed) pj/lay-point pj/plan)]
      (is (== (:panel-width pl) (:panel-height pl)) "Equal data ranges → square panel")))
  (testing "coord :fixed end-to-end — asymmetric ranges"
    (let [ds (tc/dataset {:x [0 100 50] :y [0 10 5]})
          pl (-> ds (pj/pose :x :y) (pj/coord :fixed) pj/lay-point pj/plan)]
      (is (> (:panel-width pl) (:panel-height pl)) "Wide data → wider panel"))))

(deftest diverging-color-test
  (testing "diverging-color endpoints"
    (let [[r g b _] (defaults/diverging-color 0.0)]
      (is (> r g) "t=0 red > green")
      (is (> r b) "t=0 red > blue"))
    (let [[r g b _] (defaults/diverging-color 1.0)]
      (is (> b r) "t=1 blue > red")
      (is (> b g) "t=1 blue > green"))
    (let [[r g b _] (defaults/diverging-color 0.5)]
      (is (> r 0.9) "t=0.5 is whitish (r)")
      (is (> g 0.9) "t=0.5 is whitish (g)")
      (is (> b 0.9) "t=0.5 is whitish (b)")))
  (testing "normalize-midpoint"
    (is (== 0.0 (defaults/normalize-midpoint -5 -5 5 0)))
    (is (== 0.5 (defaults/normalize-midpoint 0 -5 5 0)))
    (is (== 1.0 (defaults/normalize-midpoint 5 -5 5 0)))
    (is (== 0.25 (defaults/normalize-midpoint -2.5 -5 5 0)))
    (is (== 0.75 (defaults/normalize-midpoint 2.5 -5 5 0)))
    (is (== 0.5 (defaults/normalize-midpoint 5 0 10 nil))))
  (testing "resolve-gradient-fn"
    (is (fn? (defaults/resolve-gradient-fn nil)))
    (is (fn? (defaults/resolve-gradient-fn :diverging)))
    (is (fn? (defaults/resolve-gradient-fn :inferno)))
    (is (fn? (defaults/resolve-gradient-fn {:low "#FF0000" :mid "#FFFFFF" :high "#0000FF"}))))
  (testing "a scale spec is not a gradient"
    ;; A colour scale's `:range` holds a gradient and nothing else. A
    ;; whole spec written there is a mistake, and drawing it as three
    ;; default stops would change a plot's colours without saying so.
    (is (not (defaults/gradient-map? {:type :log})))
    (is (defaults/gradient-map? {:low "#000000" :high "#FFFFFF"}))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"names at least one of :low"
                          (defaults/resolve-gradient-fn {:type :log})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"names at least one of :low"
                          (-> (tc/dataset {:x [1 2] :y [1 2] :c [1.0 2.0]})
                              (pj/lay-point :x :y {:color :c})
                              (pj/scale :color {:range {:type :log}})))))
  (testing "a log color scale keeps the default gradient"
    (let [ds (tc/dataset {:x (range 50) :y (range 50) :c (range 1 51)})
          stops (fn [pose] (->> pose pj/plan :legend :stops (mapv :color)))
          base (-> ds (pj/lay-point :x :y {:color :c}))]
      (is (= (stops base) (stops (pj/scale base :color :log)))
          "the spacing is what :log asks for, not the colors")
      (is (= :log (:scale-type (:legend (pj/plan (pj/scale base :color :log)))))
          "and the legend says which scale it explains")
      (is (seq (:ticks (:legend (pj/plan (pj/scale base :color :log)))))
          "with ticks, so the gradient bar can be read back to a value")
      (is (not= (stops base) (stops (pj/options base {:color-range :inferno})))
          "a gradient name still changes the gradient")
      (is (= (stops (pj/options base {:color-range :inferno}))
             (stops (pj/scale base :color {:range :inferno})))
          "and the spec spelling gives the same gradient as the option")))
  (testing "diverging end-to-end"
    (let [ds (tc/dataset {:x (range 10) :y (range 10) :z (map #(- % 5) (range 10))})
          fig (-> ds (pj/pose :x :y)
                  (pj/lay-point {:color :z})
                  (pj/plot {:color-range :diverging :color-midpoint 0}))
          s (pj/svg-summary fig)]
      (is (= 10 (:points s))))))

(deftest loess-se-test
  (testing "LOESS with SE produces ribbon"
    (let [ds (tc/dataset {:x (range 20) :y (map #(+ (* 0.1 % %) (Math/sin %)) (range 20))})
          fig (-> ds (pj/pose :x :y)
                  pj/lay-point
                  (pj/lay-smooth {:confidence-band true :bootstrap-resamples 50})
                  pj/plot)
          s (pj/svg-summary fig)]
      (is (= 20 (:points s)))
      (is (= 1 (:lines s)))
      (is (= 1 (:polygons s)) "confidence ribbon polygon")))
  (testing "LOESS without SE has no ribbon"
    (let [ds (tc/dataset {:x (range 20) :y (map #(+ (* 0.1 % %) (Math/sin %)) (range 20))})
          fig (-> ds (pj/pose :x :y)
                  pj/lay-point
                  pj/lay-smooth
                  pj/plot)
          s (pj/svg-summary fig)]
      (is (= 1 (:lines s)))
      (is (zero? (:polygons s)))))
  (testing "LOESS dedup handles duplicate x values"
    (let [ds (tc/dataset {:x [1 1 2 2 3 3 4 4 5 5] :y [2 3 4 5 6 7 8 9 10 11]})
          fig (-> ds (pj/pose :x :y) pj/lay-smooth pj/plot)
          s (pj/svg-summary fig)]
      (is (= 1 (:lines s))))))

(deftest arrange-test
  (testing "flat sketches -> composite pose of leaves"
    (let [sk1 (-> tiny-ds (pj/pose :x :y) pj/lay-point)
          sk2 (-> tiny-ds (pj/pose :x :y) pj/lay-point)
          result (pj/arrange [sk1 sk2])]
      (is (pj/pose? result))
      (is (= 1 (count (:poses result))) "one row with both leaves")
      (is (= 2 (count (:poses (first (:poses result))))))))
  (testing "nested rows -> outer vertical of rows"
    (let [sk1 (-> tiny-ds (pj/pose :x :y) pj/lay-point)
          result (pj/arrange [[sk1 sk1] [sk1 sk1]])]
      (is (pj/pose? result))
      (is (= 2 (count (:poses result))) "two rows")
      (is (every? #(= 2 (count (:poses %))) (:poses result))
          "each row has two leaves")))
  (testing "title flows through to composite opts"
    (let [sk1 (-> tiny-ds (pj/pose :x :y) pj/lay-point)
          result (pj/arrange [sk1 sk1] {:title "Test" :cols 2})]
      (is (= "Test" (-> result :opts :title)))
      (let [plotted (pj/plot result)]
        (is (= :svg (first plotted)))
        (is (some #(= "Test" %) (:texts (pj/svg-summary plotted)))
            "title text appears in rendered svg"))))
  (testing "hiccup input is rejected with a clear error"
    (let [pre-rendered (-> tiny-ds (pj/pose :x :y) pj/lay-point pj/plot)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"looks like rendered hiccup"
                            (pj/arrange [pre-rendered pre-rendered]))))))

(deftest valid-plan-test
  (let [views (-> tiny-ds (pj/pose [[:x :y]]) pj/lay-point)
        pl (pj/plan views)]
    (is (pj/valid-plan? pl))))

;; ============================================================
;; Configuration System
;; ============================================================

(deftest config-returns-defaults-test
  (testing "config returns a map with all expected keys"
    (let [cfg (defaults/config)]
      (is (map? cfg))
      (is (= 600 (:width cfg)))
      (is (= 400 (:height cfg)))
      (is (= 10 (:margin cfg)))
      (is (= 3.0 (:point-radius cfg)))
      (is (= 0.75 (:point-opacity cfg)))
      (is (= 0.85 (:bar-opacity cfg)))
      (is (= 2.5 (:line-width cfg)))
      (is (= 0.6 (:grid-stroke-width cfg)))
      (is (string? (:annotation-stroke cfg)))
      (is (= 0.15 (:band-opacity cfg)))
      (is (= 60 (:x-tick-spacing cfg)))
      (is (= 40 (:y-tick-spacing cfg)))
      (is (= :sturges (:bin-method cfg)))
      (is (= 0.05 (:domain-padding cfg)))
      (is (= 13 (:label-font-size cfg)))
      (is (= 15 (:title-font-size cfg)))
      (is (= 11 (:strip-font-size cfg)))
      (is (= 38 (:label-offset cfg)))
      (is (= 18 (:title-offset cfg)))
      (is (= 16 (:strip-height cfg)))
      (is (true? (:validate cfg)))))
  (testing "config includes theme nested map"
    (let [theme (:theme (defaults/config))]
      (is (map? theme))
      (is (string? (:bg theme)))
      (is (string? (:grid theme)))
      (is (number? (:font-size theme))))))

(deftest set-config!-test
  (testing "set-config! overrides specific keys"
    (try
      (defaults/set-config! {:width 800})
      (is (= 800 (:width (defaults/config))))
      ;; Other keys remain at defaults
      (is (= 400 (:height (defaults/config))))
      (finally
        (defaults/set-config! nil))))
  (testing "set-config! nil resets to defaults"
    (try
      (defaults/set-config! {:width 999})
      (is (= 999 (:width (defaults/config))))
      (defaults/set-config! nil)
      (is (= 600 (:width (defaults/config))))
      (finally
        (defaults/set-config! nil))))
  (testing "set-config! overrides theme with merge at top level"
    (try
      (defaults/set-config! {:theme {:bg "#FFFFFF" :grid "#EEEEEE" :font-size 12}})
      (let [theme (:theme (defaults/config))]
        (is (= "#FFFFFF" (:bg theme)))
        (is (= "#EEEEEE" (:grid theme)))
        (is (= 12 (:font-size theme))))
      (finally
        (defaults/set-config! nil)))))

(deftest deep-merge-config-test
  (testing "set-config! partial theme deep-merges, preserving other theme keys"
    (try
      (defaults/set-config! {:theme {:bg "#000"}})
      (let [theme (:theme (defaults/config))]
        (is (= "#000" (:bg theme)))
        ;; grid and font-size preserved from library defaults
        (is (string? (:grid theme)) "grid should be preserved")
        (is (number? (:font-size theme)) "font-size should be preserved"))
      (finally
        (defaults/set-config! nil))))
  (testing "with-config partial theme deep-merges"
    (pj/with-config {:theme {:bg "#111"}}
      (let [theme (:theme (defaults/config))]
        (is (= "#111" (:bg theme)))
        (is (string? (:grid theme)) "grid should be preserved")
        (is (number? (:font-size theme)) "font-size should be preserved"))))
  (testing "partial theme via with-config renders without error"
    (pj/with-config {:theme {:bg "#222"}}
      (let [svg (-> {:x [1 2 3] :y [4 5 6]}
                    (pj/lay-point :x :y)
                    pj/plot)]
        (is (vector? svg)))))
  (testing "pj/options deep-merges theme across calls"
    (let [sketch (-> {:x [1 2 3] :y [4 5 6]}
                     (pj/lay-point :x :y)
                     (pj/options {:theme {:bg "#FFF"} :width 800})
                     (pj/options {:theme {:font-size 14}}))]
      (is (= "#FFF" (get-in (:opts sketch) [:theme :bg])))
      (is (= 14 (get-in (:opts sketch) [:theme :font-size])))
      (is (= 800 (:width (:opts sketch)))))))

(deftest dynamic-binding-test
  (testing "binding *config* overrides set-config!"
    (try
      (defaults/set-config! {:width 800})
      (binding [defaults/*config* {:width 1200}]
        (is (= 1200 (:width (defaults/config)))))
      ;; Outside binding, set-config! still applies
      (is (= 800 (:width (defaults/config))))
      (finally
        (defaults/set-config! nil))))
  (testing "binding *config* overrides defaults"
    (binding [defaults/*config* {:point-radius 5.0}]
      (is (= 5.0 (:point-radius (defaults/config)))))
    ;; Outside binding, back to defaults
    (is (= 3.0 (:point-radius (defaults/config))))))

(deftest resolve-config-test
  (testing "per-call opts override everything"
    (try
      (defaults/set-config! {:width 800})
      (binding [defaults/*config* {:width 1200}]
        (let [cfg (defaults/resolve-config {:width 900})]
          (is (= 900 (:width cfg)))))
      (finally
        (defaults/set-config! nil))))
  (testing "per-call opts with no overrides returns config"
    (let [cfg (defaults/resolve-config {})]
      (is (= 600 (:width cfg)))))
  (testing "per-call theme merges into default theme"
    (let [cfg (defaults/resolve-config {:theme {:bg "#FFF"}})]
      (is (= "#FFF" (get-in cfg [:theme :bg])))
      ;; grid and font-size preserved from defaults
      (is (= "#F5F5F5" (get-in cfg [:theme :grid])))
      (is (= 11 (get-in cfg [:theme :font-size])))))
  (testing "per-call palette"
    (let [cfg (defaults/resolve-config {:color-values :dark2})]
      (is (= :dark2 (:color-values cfg)))))
  (testing "per-call color-scale"
    (let [cfg (defaults/resolve-config {:color-range :diverging})]
      (is (= :diverging (:color-range cfg)))))
  (testing "per-call validate false"
    (let [cfg (defaults/resolve-config {:validate false})]
      (is (false? (:validate cfg))))))

(deftest precedence-chain-test
  (testing "full precedence: per-call > binding > set-config! > defaults"
    (try
      (defaults/set-config! {:width 800 :height 300})
      (binding [defaults/*config* {:width 1200 :height 500}]
        ;; per-call wins for width; binding wins for height
        (let [cfg (defaults/resolve-config {:width 900})]
          (is (= 900 (:width cfg)))
          (is (= 500 (:height cfg)))
          ;; margin untouched by any override → from defaults
          (is (= 10 (:margin cfg)))))
      (finally
        (defaults/set-config! nil)))))

;; ---- Public API config functions ----

(deftest api-config-test
  (testing "pj/config returns resolved config"
    (let [cfg (pj/config)]
      (is (map? cfg))
      (is (= 600 (:width cfg)))))
  (testing "pj/set-config! and reset"
    (try
      (pj/set-config! {:width 777})
      (is (= 777 (:width (pj/config))))
      (pj/set-config! nil)
      (is (= 600 (:width (pj/config))))
      (finally
        (pj/set-config! nil))))
  (testing "pj/with-config overrides for body"
    (pj/with-config {:width 1234}
      (is (= 1234 (:width (pj/config)))))
    (is (= 600 (:width (pj/config))))))

;; ---- Config affects plan output ----

(deftest config-affects-plan-test
  (let [views (-> tiny-ds (pj/pose [[:x :y]]) pj/lay-point)]
    (testing "default width/height in plan"
      (let [s (pj/plan views)]
        (is (= 600 (:width s)))
        (is (= 400 (:height s)))))
    (testing "per-call opts change plan dimensions"
      (let [s (pj/plan views {:width 800 :height 300})]
        (is (= 800 (:width s)))
        (is (= 300 (:height s)))))
    (testing "set-config! changes plan dimensions"
      (try
        (pj/set-config! {:width 700})
        (let [s (pj/plan views)]
          (is (= 700 (:width s))))
        (finally
          (pj/set-config! nil))))
    (testing "with-config changes plan dimensions"
      (pj/with-config {:height 500}
        (let [s (pj/plan views)]
          (is (= 500 (:height s)))))
      ;; After with-config, back to default
      (let [s (pj/plan views)]
        (is (= 400 (:height s)))))
    (testing "plan does NOT contain :theme key"
      (let [s (pj/plan views)]
        (is (not (contains? s :theme)))))))

;; ---- Config affects rendered SVG ----

(deftest config-affects-render-test
  (let [views (-> tiny-ds (pj/pose [[:x :y]]) pj/lay-point)]
    (testing "default theme bg appears in SVG"
      (let [svg (pj/plot views)
            summary (pj/svg-summary svg)]
        (is (= 1 (:panels summary)))
        (is (= 5 (:points summary)))))
    (testing "per-call theme overrides bg in SVG"
      (let [svg (pj/plot views {:theme {:bg "#FFFFFF" :grid "#EEEEEE" :font-size 8}})
            s (str svg)]
        ;; Default bg is rgb(235,235,235); custom is rgb(255,255,255)
        (is (clojure.string/includes? s "rgb(255,255,255)"))))
    (testing "with-config theme overrides bg in SVG"
      (let [svg (pj/with-config {:theme {:bg "#FF0000" :grid "#FFFFFF" :font-size 8}}
                  (pj/plot views))
            s (str svg)]
        (is (clojure.string/includes? s "rgb(255,0,0)"))))
    (testing "per-call width changes SVG viewBox"
      ;; Under the total-dimensions semantics, :width is the TOTAL
      ;; SVG width; the panel is derived by subtracting overhead.
      ;; So the output viewBox width equals :width exactly.
      (let [svg (pj/plot views {:width 800})
            attrs (second svg)]
        (is (= 800.0 (double (:width attrs))))))))

;; ---- Config with palette ----

(defn- group-colors-from-plan [pl]
  ;; Each panel's first layer has :groups; each group has :color [r g b a].
  (->> (:panels pl)
       (mapcat :layers)
       (mapcat :groups)
       (keep :color)
       (mapv vec)
       distinct
       set))

(deftest config-palette-test
  (let [ds (tc/dataset {:x [1 2 3 4 5 6]
                        :y [10 20 30 15 25 35]
                        :g ["a" "a" "a" "b" "b" "b"]})
        views (-> ds (pj/pose [[:x :y]]) (pj/lay-point {:color :g}))]
    (testing "default palette assigns distinct colors per category"
      (let [colors (group-colors-from-plan (pj/plan views))]
        (is (= 2 (count colors))
            "two categories, two colors")))
    (testing "per-call :color-values :dark2 produces colors that differ from default"
      (let [default-colors (group-colors-from-plan (pj/plan views))
            dark2-colors (group-colors-from-plan (pj/plan views {:color-values :dark2}))]
        (is (= 2 (count dark2-colors)))
        (is (not= default-colors dark2-colors)
            "palette change must actually change the rendered colors")
        (is (= dark2-colors
               (group-colors-from-plan
                (pj/plan (pj/scale views :color {:values :dark2}))))
            "and the spec spelling gives the same colours as the option")))
    (testing "set-config! palette flows through"
      (try
        (let [default-colors (group-colors-from-plan (pj/plan views))]
          (pj/set-config! {:color-values :set2})
          (let [set2-colors (group-colors-from-plan (pj/plan views))]
            (is (= 2 (count set2-colors)))
            (is (not= default-colors set2-colors)
                ":set2 palette must produce colors distinct from default")))
        (finally
          (pj/set-config! nil))))))

;; ---- Config validation flag ----

(deftest config-validate-flag-test
  (let [views (-> tiny-ds (pj/pose [[:x :y]]) pj/lay-point)]
    (testing "validate true (default) -- valid plan passes"
      (is (some? (pj/plan views))))
    (testing "validate false skips schema check"
      (is (some? (pj/plan views {:validate false}))))
    (testing "validate true throws when the plan fails schema"
      ;; Force the schema check to fail by stubbing the explain
      ;; function. The flag must actually drive whether the throw
      ;; happens; the previous test only covered the happy path
      ;; on both branches, so the flag could have been ignored.
      (with-redefs [scicloj.plotje.impl.plan-schema/explain
                    (fn [_] {:errors [:fake]})]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"does not conform to schema"
                              (pj/plan views))
            ":validate true must throw on schema failure")
        (is (some? (pj/plan views {:validate false}))
            ":validate false must skip the throw")))))

;; ---- Edge case tests ----

(deftest single-point-dataset-test
  (testing "plan with a single data point does not throw"
    (let [ds (tc/dataset {:x [5] :y [10]})
          pl (pj/plan (-> ds (pj/lay-point :x :y)))]
      (is (= 1 (count (:panels pl))))
      (is (some? (pj/plot (-> ds (pj/lay-point :x :y))))))))

(deftest two-point-dataset-test
  (testing "regression with exactly 2 points — lm needs n>=3 so falls back gracefully"
    (let [ds (tc/dataset {:x [1 2] :y [3 4]})
          views (-> ds (pj/pose :x :y) pj/lay-point)]
      (is (some? (pj/plan views))))))

(deftest all-same-values-test
  (testing "scatter where all x values are identical"
    (let [ds (tc/dataset {:x [5 5 5 5] :y [1 2 3 4]})
          pl (pj/plan (-> ds (pj/lay-point :x :y)))]
      (is (some? pl))
      (is (= 1 (count (:panels pl))))))
  (testing "scatter where all y values are identical"
    (let [ds (tc/dataset {:x [1 2 3 4] :y [5 5 5 5]})
          pl (pj/plan (-> ds (pj/lay-point :x :y)))]
      (is (some? pl)))))

(deftest categorical-single-category-test
  (testing "bar chart with only one category"
    (let [ds (tc/dataset {:cat ["a" "a" "a"] :val [1 2 3]})
          pl (pj/plan (-> ds (pj/lay-bar :cat :val)))]
      (is (= 1 (count (:panels pl)))))))

(deftest histogram-uniform-data-test
  (testing "histogram with all identical values"
    (let [ds (tc/dataset {:x [5 5 5 5 5]})
          pl (pj/plan (-> ds (pj/lay-histogram :x)))]
      (is (some? pl)))))

(deftest polar-coord-test
  (testing "polar coordinate plan structure"
    (let [ds (tc/dataset {:cat ["A" "B" "C"] :val [10 20 30]})
          views (-> ds
                    (pj/pose :cat :val)
                    pj/lay-bar
                    (pj/coord :polar))
          pl (pj/plan views)]
      (is (= :polar (get-in pl [:panels 0 :coord]))))))

(deftest flip-coord-test
  (testing "flipped coordinates swap x/y domains"
    (let [views (-> cat-ds
                    (pj/pose :cat :val)
                    pj/lay-bar
                    (pj/coord :flip))
          pl (pj/plan views)
          panel (first (:panels pl))]
      (is (= :flip (:coord panel))))))

(deftest labs-test
  (testing "axis labels propagate to plan via options"
    (let [pl (-> tiny-ds
                 (pj/pose :x :y)
                 pj/lay-point
                 (pj/options {:x-label "X Axis" :y-label "Y Axis"})
                 pj/plan)]
      (is (= "X Axis" (:x-label pl)))
      (is (= "Y Axis" (:y-label pl)))))
  (testing "title/subtitle/caption propagate via options"
    (let [pl (-> tiny-ds
                 (pj/pose :x :y)
                 pj/lay-point
                 (pj/options {:title "T" :subtitle "ST" :caption "C"})
                 pj/plan)]
      (is (= "T" (:title pl)))
      (is (= "ST" (:subtitle pl)))
      (is (= "C" (:caption pl))))))

(deftest log-scale-test
  (testing "log scale is recorded in plan"
    (let [ds (tc/dataset {:x [1 10 100 1000] :y [1 2 3 4]})
          views (-> ds
                    (pj/pose :x :y)
                    pj/lay-point
                    (pj/scale :x :log))
          pl (pj/plan views)
          panel (first (:panels pl))]
      (is (= :log (get-in panel [:x-scale :type]))))))

(deftest log-scale-nonpositive-test
  (testing "non-positive values are filtered on log-scaled x axis"
    (let [pl (pj/plan (-> {:x [0 -1 1 10 100] :y [1 2 3 4 5]}
                          (pj/lay-point :x :y)
                          (pj/scale :x :log)))
          layer (first (:layers (first (:panels pl))))
          group (first (:groups layer))]
      (is (= 3 (count (:xs group))))
      (is (= [1 10 100] (vec (:xs group))))))
  (testing "non-positive values are filtered on log-scaled y axis"
    (let [pl (pj/plan (-> {:x [1 2 3 4 5] :y [0 -1 1 10 100]}
                          (pj/lay-point :x :y)
                          (pj/scale :y :log)))
          layer (first (:layers (first (:panels pl))))
          group (first (:groups layer))]
      (is (= 3 (count (:xs group))))))
  (testing "all-positive data is not filtered"
    (let [pl (pj/plan (-> {:x [1 10 100] :y [1 2 3]}
                          (pj/lay-point :x :y)
                          (pj/scale :x :log)))
          layer (first (:layers (first (:panels pl))))
          group (first (:groups layer))]
      (is (= 3 (count (:xs group)))))))

(deftest infinity-filtering-test
  (testing "infinite y values are filtered with warning"
    (let [pl (pj/plan (-> {:x [1 2 3 4 5]
                           :y [10.0 Double/POSITIVE_INFINITY 30.0 Double/NEGATIVE_INFINITY 50.0]}
                          (pj/lay-point :x :y)))
          layer (first (:layers (first (:panels pl))))
          group (first (:groups layer))]
      (is (= 3 (count (:xs group))))
      (is (= [1 3 5] (vec (:xs group))))
      (is (= [10.0 30.0 50.0] (vec (:ys group))))))
  (testing "infinite x values are filtered"
    (let [pl (pj/plan (-> {:x [1.0 Double/POSITIVE_INFINITY 3.0]
                           :y [10 20 30]}
                          (pj/lay-point :x :y)))
          group (-> pl :panels first :layers first :groups first)]
      (is (= 2 (count (:xs group))))))
  (testing "SVG has no NaN after infinity filtering"
    (let [svg (pj/plot (-> {:x [1 2 3] :y [1.0 Double/POSITIVE_INFINITY 3.0]}
                           (pj/lay-point :x :y)))]
      (is (not (clojure.string/includes? (str svg) "NaN")))))
  (testing "all-finite data is not filtered"
    (let [pl (pj/plan (-> {:x [1 2 3] :y [10.0 20.0 30.0]}
                          (pj/lay-point :x :y)))
          group (-> pl :panels first :layers first :groups first)]
      (is (= 3 (count (:xs group)))))))

(deftest stacked-negative-domain-test
  (testing "all-negative stacked bars produce correct y-domain"
    (let [pl (pj/plan (-> {:category ["A" "A" "B" "B"]
                           :group ["g1" "g2" "g1" "g2"]
                           :value [-10 -20 -5 -15]}
                          (pj/lay-bar :category :value {:color :group :position :stack})))
          [lo hi] (:y-domain (first (:panels pl)))]
      (is (neg? lo) "lower bound should be negative for all-negative stacked data")
      (is (pos? hi) "upper bound includes 0 baseline with padding")))
  (testing "mixed positive/negative stacked bars span both sides"
    (let [pl (pj/plan (-> {:category ["A" "A" "B" "B"]
                           :group ["g1" "g2" "g1" "g2"]
                           :value [10 -20 5 -15]}
                          (pj/lay-bar :category :value {:color :group :position :stack})))
          [lo hi] (:y-domain (first (:panels pl)))]
      (is (neg? lo) "lower bound extends below zero")
      (is (pos? hi) "upper bound extends above zero")))
  (testing "all-negative stacked bars render without NaN"
    (let [svg (pj/plot (-> {:category ["A" "A" "B" "B"]
                            :group ["g1" "g2" "g1" "g2"]
                            :value [-10 -20 -5 -15]}
                           (pj/lay-bar :category :value {:color :group :position :stack})))]
      (is (not (clojure.string/includes? (str svg) "NaN"))))))

(deftest boolean-color-test
  (testing "Boolean false is not dropped as group key"
    (let [pl (-> {:x [1 2 3 4] :y [10 20 30 40] :flag [true false true false]}
                 (pj/lay-point :x :y {:color :flag})
                 pj/plan)
          groups (-> pl :panels first :layers first :groups)]
      (is (= 2 (count groups)) "two groups for true/false")
      (is (= "true" (:label (first groups))))
      (is (= "false" (:label (second groups))))
      ;; Both groups should get distinct palette colors (not default gray)
      (is (not= (:color (first groups)) (:color (second groups)))
          "true and false get different colors")))
  (testing "Legend matches rendering for boolean groups"
    (let [pl (-> {:x [1 2 3 4] :y [10 20 30 40] :flag [true false true false]}
                 (pj/lay-point :x :y {:color :flag})
                 pj/plan)
          legend-colors (mapv :color (:entries (:legend pl)))
          group-colors (mapv :color (-> pl :panels first :layers first :groups))]
      (is (= (count legend-colors) (count group-colors)))
      (is (= (set legend-colors) (set group-colors))
          "legend colors match group colors"))))

(deftest x-only-validation-test
  (testing "histogram rejects :y column"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"lay-histogram uses only the x column"
                          (pj/lay-histogram {:x [1 2 3] :y [4 5 6]} :x :y))))
  (testing "bar accepts a :y column (value bars)"
    (is (some? (pj/lay-bar {:x ["a" "b"] :y [1 2]} :x :y))))
  (testing "density rejects :y column"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"lay-density uses only the x column"
                          (pj/lay-density {:x [1 2 3] :y [4 5 6]} :x :y))))
  (testing "histogram with opts (not :y) still works"
    (is (some? (pj/lay-histogram {:x [1 2 3 4 5]} :x {:color :x})))))

(deftest multiple-layers-test
  (testing "plan with point + line layers"
    (let [views (-> tiny-ds
                    (pj/pose :x :y)
                    pj/lay-point
                    pj/lay-line)
          pl (pj/plan views)
          layers (get-in pl [:panels 0 :layers])]
      (is (= 2 (count layers))))))

(deftest color-groups-test
  (testing "color mapping with string values produces legend"
    (let [ds (tc/dataset {:x [1 2 3] :y [4 5 6] :g ["a" "b" "a"]})
          pl (pj/plan (-> ds (pj/lay-point :x :y {:color :g})))]
      (is (some? (:legend pl)))
      (is (= 2 (count (get-in pl [:legend :entries])))))))

(deftest plan-dimensions-test
  (testing "custom width and height"
    (let [views (-> tiny-ds (pj/pose :x :y) pj/lay-point)
          pl (pj/plan views {:width 800 :height 300})]
      (is (= 800 (:width pl)))
      (is (= 300 (:height pl))))))

(deftest cross-grid-strip-labels-test
  (testing "cross plot (full grid) shows all strip labels"
    (let [ds (tc/dataset {:a [1 2 3 4 5] :b [5 4 3 2 1] :c [2 4 6 8 10]})
          views (-> ds
                    (pj/pose (pj/cross [:a :b :c] [:a :b :c]))
                    pj/lay-point)
          svg (pj/plot views)
          s (pj/svg-summary svg)
          texts (:texts s)]
      (is (= 9 (:panels s)))
      (is (some #{"a"} texts))
      (is (some #{"b"} texts))
      (is (some #{"c"} texts)))))

(deftest save-test
  (testing "pj/save writes valid SVG file"
    (let [ds (tc/dataset "https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv"
                         {:key-fn keyword})
          path (str (java.io.File/createTempFile "plotje" ".svg"))
          views (-> ds (pj/pose :sepal_length :sepal_width)
                    (pj/lay-point {:color :species}))]
      (pj/save views path)
      (let [content (slurp path)]
        (is (.startsWith content "<?xml"))
        (is (.contains content "<svg"))
        (is (.contains content "setosa")))
      (.delete (java.io.File. path)))))

(deftest temporal-epoch-ms-test
  (testing "LocalDate converts to epoch-ms"
    (let [d (jt/local-date 2025 1 1)
          ms (resolve/temporal->epoch-ms d)]
      (is (double? ms))
      (is (== ms (* (.toEpochDay d) 86400000)))))
  (testing "LocalDateTime preserves sub-day precision"
    (let [dt (jt/local-date-time 2025 3 15 12 30 0)
          ms (resolve/temporal->epoch-ms dt)]
      (is (double? ms))
      ;; Should differ from midnight by 12.5 hours in ms
      (let [midnight-ms (resolve/temporal->epoch-ms (jt/local-date 2025 3 15))]
        (is (== (- ms midnight-ms) (* 12.5 3600000))))))
  (testing "Temporal plan has datetime ticks"
    (let [pl (-> (tc/dataset {:date [(jt/local-date 2025 1 1)
                                     (jt/local-date 2025 6 1)
                                     (jt/local-date 2025 12 1)]
                              :val [10 20 30]})
                 (pj/pose :date :val)
                 pj/lay-point
                 pj/plan)]
      (is (= 1 (count (:panels pl))))
      (is (seq (get-in pl [:panels 0 :x-ticks :labels]))))))

(deftest format-log-ticks-test
  (testing "Powers of 10 >= 1 render as integers"
    (is (= ["1" "10" "100" "1000"]
           (scale/format-log-ticks [1.0 10.0 100.0 1000.0]))))
  (testing "Decimal powers keep decimal form"
    (is (= ["0.001" "0.01" "0.1" "1" "10"]
           (scale/format-log-ticks [0.001 0.01 0.1 1.0 10.0])))))

(deftest column-name-matching-is-strict-test
  ;; Column name matching is strict: a keyword reference does not
  ;; match a string column name with the same characters and vice
  ;; versa. Pick one form and use it consistently with the dataset's
  ;; actual column names.
  (testing "string refs on a string-keyed dataset work"
    (let [ds (tc/dataset {"x" [1 2 3] "y" [4 5 6]})
          s (-> ds (pj/pose "x" "y") pj/lay-point pj/plot pj/svg-summary)]
      (is (= 3 (:points s)))))
  (testing "keyword refs on a keyword-keyed dataset work"
    (let [ds (tc/dataset {:x [1 2 3] :y [4 5 6]})
          s (-> ds (pj/pose :x :y) pj/lay-point pj/plot pj/svg-summary)]
      (is (= 3 (:points s)))))
  (testing "keyword refs on a string-keyed dataset throw"
    (let [ds (tc/dataset {"x" [1 2 3] "y" [4 5 6]})]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"not found in dataset"
                            (-> ds (pj/pose :x :y) pj/lay-point pj/plot)))))
  (testing "string refs on a keyword-keyed dataset throw"
    (let [ds (tc/dataset {:x [1 2 3] :y [4 5 6]})]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"not found in dataset"
                            (-> ds (pj/pose "x" "y") pj/lay-point pj/plot)))))
  (testing "a dataset whose column names are of mixed types still gets the message"
    ;; The message lists the available names, and listing them sorts
    ;; them. Sorting a keyword against a string or a number throws a
    ;; ClassCastException, so the helpful error was replaced by an
    ;; unhelpful one exactly where a name is most likely to be mistyped.
    (doseq [[label ds] [["keyword and string names"
                         (tc/dataset {:x [1 2 3] :y [4 5 6] "blue" ["a" "b" "c"]})]
                        ["keyword and integer names"
                         (tc/dataset {:x [1 2 3] :y [4 5 6] 0 ["a" "b" "c"]})]]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Column :nope \(from :color\) not found in dataset"
                            (-> ds (pj/pose :x :y) (pj/lay-point {:color :nope}) pj/plot))
          label)))
  (testing "string :color falls through to literal CSS when no string column matches"
    ;; :color "#FF0000" is the canonical literal-color case and must
    ;; keep working; it does not name any dataset column.
    (let [v (-> (tc/dataset {:x [1 2 3] :y [4 5 6]})
                (pj/pose :x :y)
                (pj/lay-point {:color "#FF0000"})
                pj/plot)]
      (is (= 3 (:points (pj/svg-summary v))))))
  (testing "Typo gives error at plan time"
    (let [iris (tc/dataset {:sepal_length [5.0 6.0] :sepal_width [3.0 3.5]})]
      (is (thrown? clojure.lang.ExceptionInfo
                   (-> iris (pj/pose :sepl_length :sepal_width)
                       pj/lay-point pj/plot)))))
  (testing "pj/cross is purely structural -- still works with strings"
    (is (= 9 (count (pj/cross ["a" "b" "c"] ["a" "b" "c"]))))))

(deftest string-column-in-lay-test
  (testing "String column names in lay-point directly (no explicit pj/pose)"
    (let [s (-> {"x" [1 2 3] "y" [4 5 6]}
                (pj/lay-point "x" "y") pj/plot pj/svg-summary)]
      (is (= 3 (:points s)))))
  (testing "String column names in lay-line directly"
    (let [s (-> {"x" [1 2 3] "y" [4 5 6]}
                (pj/lay-line "x" "y") pj/plot pj/svg-summary)]
      (is (= 1 (:lines s)))))
  (testing "String column in lay-histogram directly"
    (let [s (-> {"x" [1 2 3 4 5 6 7 8 9 10]}
                (pj/lay-histogram "x") pj/plot pj/svg-summary)]
      (is (pos? (:polygons s))))))

(deftest named-color-test
  (testing "Named color strings work as fixed colors"
    (let [s (-> {:x [1 2 3] :y [4 5 6]}
                (pj/pose :x :y)
                (pj/lay-point {:color "red"})
                pj/plot pj/svg-summary)]
      (is (= 3 (:points s)))))
  (testing "Named color produces correct RGBA"
    (let [pl (-> {:x [1 2 3] :y [4 5 6]}
                 (pj/pose :x :y)
                 (pj/lay-point {:color "steelblue"})
                 pj/plan)
          c (:color (first (:groups (first (:layers (first (:panels pl)))))))]
      (is (> (nth c 2) 0.5) "steelblue should have high blue channel")))
  (testing "Unknown color string is reported at the pose, naming both readings"
    ;; It used to reach the renderer and die on "Unknown color", which
    ;; said nothing about the likelier mistake -- that a column of that
    ;; name was meant. The data is asked first now, so the message can
    ;; name what was looked up and what else it could have been.
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"not found in dataset.*not a color either"
                          (-> {:x [1 2 3] :y [4 5 6]}
                              (pj/pose :x :y)
                              (pj/lay-point {:color "notacolor"})
                              pj/plot))))
  (testing "A keyword naming a color is drawn, not looked up"
    ;; `:color :red` used to be reported as a missing column, because a
    ;; keyword was a column reference and nothing else.
    (let [c (-> {:x [1 2 3] :y [4 5 6]}
                (pj/pose :x :y)
                (pj/lay-point {:color :red})
                pj/plan :panels first :layers first :groups first :color)]
      (is (= [1.0 0.0 0.0 1.0] c))))
  (testing "A hex string missing its # is reported, not read as a shade"
    ;; clojure2d reads a bare `abc` as `#aabbcc`, so this used to draw a
    ;; color for what is far likelier a mistyped column name.
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"not found in dataset.*not a color either"
                          (-> {:x [1 2 3] :y [4 5 6]}
                              (pj/pose :x :y)
                              (pj/lay-point {:color "fff"})
                              pj/plan)))))

(deftest schema-all-marks-test
  (testing "Every mark type produces a valid plan"
    (let [iris (tc/dataset "https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv"
                           {:key-fn keyword})
          xy-ds (tc/dataset {:x (range 10) :y (range 10)})
          eb-ds (tc/dataset {:x ["a" "b"] :y [10 20] :y-min [8 17] :y-max [12 23]})
          txt-ds (tc/dataset {:x [1 2] :y [3 4] :n ["a" "b"]})
          cases [["point" (-> iris (pj/pose :sepal_length :sepal_width) (pj/lay-point {:color :species}))]
                 ["bar" (-> iris (pj/pose :species) pj/lay-bar)]
                 ["histogram" (-> iris (pj/pose :sepal_length) pj/lay-histogram)]
                 ["line" (-> xy-ds (pj/pose :x :y) pj/lay-line)]
                 ["step" (-> xy-ds (pj/pose :x :y) pj/lay-step)]
                 ["lm" (-> iris (pj/pose :sepal_length :sepal_width) (pj/lay-smooth {:stat :linear-model :confidence-band true}))]
                 ["loess" (-> iris (pj/pose :sepal_length :sepal_width) pj/lay-smooth)]
                 ["area" (-> xy-ds (pj/pose :x :y) pj/lay-area)]
                 ["boxplot" (-> iris (pj/pose :species :sepal_width) pj/lay-boxplot)]
                 ["violin" (-> iris (pj/pose :species :sepal_width) pj/lay-violin)]
                 ["density" (-> iris (pj/pose :sepal_length) pj/lay-density)]
                 ["ridgeline" (-> iris (pj/pose :species :sepal_width) pj/lay-ridgeline)]
                 ["text" (-> txt-ds (pj/pose :x :y) (pj/lay-text {:text :n}))]
                 ["tile" (-> iris (pj/pose :sepal_length :sepal_width) pj/lay-tile)]
                 ["contour" (-> iris (pj/pose :sepal_length :sepal_width) pj/lay-contour)]
                 ["errorbar" (-> eb-ds (pj/pose :x :y) (pj/lay-errorbar {:y-min :y-min :y-max :y-max}))]
                 ["lollipop" (-> eb-ds (pj/pose :x :y) pj/lay-lollipop)]
                 ["summary" (-> iris (pj/pose :species :sepal_width) pj/lay-summary)]]]
      (doseq [[mark-name views] cases]
        (testing mark-name
          (is (pj/valid-plan? (pj/plan views {:validate false}))))))))

(deftest validation-test
  (testing "numeric faceting produces correct panels"
    (is (= 3 (-> {:x [1 2 3 4 5 6] :y [10 20 30 40 50 60]
                  :f [1.0 1.0 2.0 2.0 3.0 3.0]}
                 (pj/lay-point :x :y) (pj/facet :f) pj/plan :panels count)))
    (is (= 3 (-> {:x [1 2 3 4 5 6] :y [10 20 30 40 50 60]
                  :s ["a" "a" "b" "b" "c" "c"]}
                 (pj/lay-point :x :y) (pj/facet :s) pj/plan :panels count))))

  (testing "histogram on categorical column throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"numeric"
                          (-> {:x ["a" "b" "c"]} (pj/lay-histogram :x) pj/plan))))

  (testing "lm on categorical x throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"numeric"
                          (-> {:species ["a" "b" "c"] :y [1 2 3]}
                              (pj/lay-smooth :species :y {:stat :linear-model}) pj/plan))))

  (testing "loess on categorical x throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"numeric"
                          (-> {:species ["a" "b" "c" "d"] :y [1 2 3 4]}
                              (pj/lay-smooth :species :y) pj/plan))))

  (testing "errorbar without y-min/y-max throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"y-min.*y-max"
                          (-> {:x ["a" "b" "c"] :y [1 2 3]}
                              (pj/lay-errorbar :x :y) pj/plan))))

  (testing "text without :text column throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"text"
                          (-> {:x [1 2 3] :y [10 20 30]}
                              (pj/lay-text :x :y) pj/plan))))

  (testing "scale channel validation"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Scale channel"
                          (pj/scale [] :z :log))))

  (testing "coord validation"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Coordinate"
                          (pj/coord [] :invalid))))

  (testing "y-type propagated when x and y reference the same column"
    ;; Regression: previously (= x-res y-res) returned nil for y-type,
    ;; letting ClassCastException escape instead of a clear error.
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"numeric"
                          (-> {:species ["a" "b" "c"]}
                              (pj/lay-summary :species :species) pj/plan)))))

(deftest aesthetic-column-validation-test
  ;; persona-16 B1: validate-columns covers :color/:size/:alpha/:shape/:group/
  ;; :text/:y-min/:y-max/:fill, not just :x/:y. Closes the C2 epic:
  ;; P5-R2 C1, P9-R2 F5/F6, Skept-R4 F2/F5/F6, P7-R2 F1/F2/F3, P11-R2 F5,
  ;; P3-R2 footgun.
  (let [data {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0] :g ["a" "b" "c"]}]
    (testing "typoed :color keyword throws with key and column name"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Column :speices \(from :color\) not found"
                            (-> data (pj/lay-point :x :y {:color :speices}) pj/plan))))

    (testing "typoed :size keyword throws"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Column :bogus \(from :size\) not found"
                            (-> data (pj/lay-point :x :y {:size :bogus}) pj/plan))))

    (testing "typoed :alpha keyword throws"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Column :bogus \(from :alpha\) not found"
                            (-> data (pj/lay-point :x :y {:alpha :bogus}) pj/plan))))

    (testing "typoed :group keyword throws"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Column :bogus \(from :group\) not found"
                            (-> data (pj/lay-point :x :y {:group :bogus}) pj/plan))))

    (testing "typoed :y-min keyword throws"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Column :bogus1 \(from :y-min\) not found"
                            (-> data (pj/lay-errorbar :x :y {:y-min :bogus1 :y-max :bogus2}) pj/plan))))

    (testing "literal :color string is not flagged (named color)"
      (is (some? (-> data (pj/lay-point :x :y {:color "red"}) pj/plan :panels))))

    (testing "literal :color hex is not flagged"
      (is (some? (-> data (pj/lay-point :x :y {:color "#FF0000"}) pj/plan :panels))))

    (testing "good :color column still works"
      (is (some? (-> data (pj/lay-point :x :y {:color :g}) pj/plan :panels))))

    (testing "nil :color (explicit cancellation) skipped by validator"
      (is (some? (-> data (pj/lay-point :x :y {:color nil}) pj/plan :panels))))

    (testing "string column ref in :color resolves correctly"
      (is (some? (-> {"a" [1 2 3] "b" [10 20 30] "g" ["x" "y" "z"]}
                     (pj/lay-point "a" "b" {:color "g"}) pj/plan :panels))))

    (testing "available columns listed in error for typo recovery"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Available: \(:g :x :y\)"
                            (-> data (pj/lay-point :x :y {:color :typo}) pj/plan))))))

(deftest facet-validation-test
  ;; persona-16 B3. Closes P9-R2 F9, Skept-R4 F7, P3-R2 Footgun 5.
  (let [data {:x [1 2 3 4 5 6] :y [10 20 30 40 50 60]
              :g ["a" "b" "a" "b" "a" "b"]
              :h ["x" "y" "x" "y" "x" "y"]}]
    (testing "typoed facet column throws with available columns"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Facet column :speices.*not found"
                            (-> data (pj/lay-point :x :y) (pj/facet :speices) pj/plan))))

    (testing "vector facet spec is rejected, points at facet-grid"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"facet-grid"
                            (-> data (pj/lay-point :x :y) (pj/facet [:g :h]) pj/plan))))

    (testing "valid facet column still works"
      (is (= 2 (-> data (pj/lay-point :x :y) (pj/facet :g) pj/plan :panels count))))

    (testing "facet-grid with valid columns still works"
      (is (= 4 (-> data (pj/lay-point :x :y) (pj/facet-grid :g :h) pj/plan :panels count))))

    (testing "typoed facet-grid column throws"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Facet column :speices.*not found"
                            (-> data (pj/lay-point :x :y) (pj/facet-grid :speices :h) pj/plan))))))

(deftest lay-method-auto-infer-test
  ;; persona-16 B4. Closes Skept-R4 F1, P5-R2 C2.
  (let [four-col {:a [1 2 3] :b [4 5 6] :c [7 8 9] :d [10 11 12]}
        three-col {:x [1 2 3] :y [10 20 30] :g ["a" "b" "c"]}]
    (testing "4+ column auto-infer throws with helpful message"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Cannot auto-infer columns from 4 columns"
                            (-> four-col pj/lay-point pj/plan))))

    (testing "error message suggests explicit x/y"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay-point data :x :y"
                            (-> four-col pj/lay-point pj/plan))))

    (testing "error message lists available columns"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Available columns: \(:a :b :c :d\)"
                            (-> four-col pj/lay-point pj/plan))))

    (testing "3-column auto-infer still works"
      (is (= 1 (-> three-col pj/lay-point pj/plan :panels count))))

    (testing "4+ column with explicit x/y still works"
      (is (= 1 (-> four-col (pj/lay-point :a :b) pj/plan :panels count))))))

(deftest tile-color-synonym-test
  ;; persona-11-R2 F8: lay-tile used to silently paint every tile the
  ;; midpoint color when the user passed {:color :value} instead of
  ;; {:fill :value}. Now :color is accepted as a synonym (only when
  ;; stat is :bin2d, i.e. from `pj/lay-tile` -- NOT for density2d/
  ;; contour which have their own intentional kde2d stat).
  (let [data (tc/dataset {:row (mapcat #(repeat 6 %) (range 6))
                          :col (flatten (repeat 6 (range 6)))
                          :value (map #(Math/sin (* % 0.5)) (range 36))})]

    (testing "lay-tile with :color produces the same plan as :fill"
      (let [p-color (-> data (pj/pose :col :row {:color :value})
                        pj/lay-tile pj/plan)
            p-fill (-> data (pj/pose :col :row {:fill :value})
                       pj/lay-tile pj/plan)
            tiles-color (-> p-color :panels first :layers first :tiles)
            tiles-fill (-> p-fill :panels first :layers first :tiles)]
        (is (= (count tiles-color) (count tiles-fill))
            "same number of tiles in both paths")
        (is (= (distinct (map :color tiles-color))
               (distinct (map :color tiles-fill)))
            "same distinct colors in both paths")
        (is (> (count (distinct (map :color tiles-color))) 1)
            ":color path produces varying tile colors, not uniform")))

    (testing "lay-density-2d with :color :species (categorical) still works"
      ;; This used to hit the over-broad tile-override; must stay :density-2d.
      (is (some? (-> (tc/dataset {:x [1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0]
                                  :y [1.0 2.1 3.0 4.2 5.1 6.0 7.0 8.1 9.0 10.0]
                                  :g ["a" "a" "a" "a" "a" "b" "b" "b" "b" "b"]})
                     (pj/pose :x :y {:color :g})
                     pj/lay-density-2d pj/plan))))))

(deftest mixed-type-column-test
  ;; persona-skeptical-round-4 F5: a column whose values are heterogeneous
  ;; (number + string + keyword) used to crash with a multi-KB Malli
  ;; "Plan does not conform to schema" dump. Now thrown with a clear
  ;; column name and the discovered types.
  (testing "mixed-type :y column throws with type list"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"has mixed value types"
                          (-> (tc/dataset {:x [1 2 3 4 5]
                                           :y [1.0 "two" 3.0 :four 5.0]})
                              (pj/lay-point :x :y) pj/plan))))

  (testing "all-string column is fine (categorical)"
    (is (some? (-> {:x [1 2 3] :g ["a" "b" "c"]} (pj/lay-bar :g) pj/plan))))

  (testing "all-numeric column is fine"
    (is (some? (-> {:x [1.0 2.0 3.0] :y [4.0 5.0 6.0]}
                   (pj/lay-point :x :y) pj/plan)))))

(deftest aesthetic-cross-type-lookup-throws-test
  ;; Strict matching: a keyword reference does not satisfy a string
  ;; column name and vice versa. Position references that mismatch
  ;; throw at validation time. String :color references that do not
  ;; match any column fall through to literal CSS color (string
  ;; :color is the documented disambiguation case).
  (testing "string-keyed dataset + keyword position throws"
    (let [str-ds (tc/dataset {"x" [1.0 2.0 3.0]
                              "y" [10.0 20.0 30.0]
                              "g" ["A" "B" "A"]})]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"not found in dataset"
                            (-> str-ds (pj/lay-point :x :y {:color :g}) pj/plan)))))
  (testing "keyword-keyed dataset + string position throws"
    (let [kw-ds (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0] :g ["A" "B" "A"]})]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"not found in dataset"
                            (-> kw-ds (pj/lay-point "x" "y" {:color "g"}) pj/plan)))))
  (testing "keyword-keyed dataset + string :color that is a valid CSS color falls through"
    ;; \"red\" doesn't match any column, so :color is treated as a
    ;; literal CSS color -- the plot renders without grouping.
    (let [kw-ds (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0] :g ["A" "B" "A"]})
          pl (-> kw-ds (pj/lay-point :x :y {:color "red"}) pj/plan)]
      (is (= 1 (count (:panels pl)))))))

(deftest layer-x-y-override-test
  ;; persona-skeptical-round-4 F9: layer-level :x/:y overrides used to
  ;; warn as unknown options even though they actually worked. Now :x/:y
  ;; are accepted on layer opts (universal-layer-options).
  (testing "layer-level :x / :y on lay-* doesn't trigger an unknown-opt warning"
    (let [out (with-out-str
                (-> {:a [1 2 3] :b [4 5 6] :c [7 8 9] :d [10 11 12]}
                    (pj/pose :a :b)
                    (pj/lay-point {:x :c :y :d})))]
      (is (not (re-find #"does not recognize option" out))))))

(deftest plot-level-keys-stripped-from-wrong-scope-test
  ;; persona-skeptical-round-6 F1/F5/F6: plot-level keys like
  ;; :x-scale and :coord used to emit an "unrecognized option"
  ;; warning but still propagate into the layer's or pose's
  ;; mapping and leak into the final panel (identical output to
  ;; the canonical pj/scale / pj/coord form). Now the warning is
  ;; honest: unknown keys are stripped from the mapping, so the
  ;; panel uses default scales/coord.
  (let [ds {:x [1 10 100] :y [1 2 3]}]

    (testing "layer-level :x-scale is stripped, not honored"
      (let [fr (-> ds (pj/pose :x :y) (pj/lay-point {:x-scale {:type :log}}))
            layer-mapping (:mapping (first (:layers fr)))
            panel (first (:panels (pj/plan fr)))]
        (is (not (contains? (or layer-mapping {}) :x-scale))
            ":x-scale should not appear in layer mapping")
        (is (= :linear (get-in panel [:x-scale :type]))
            "panel x-scale should stay at default :linear")))

    (testing "pose-level :x-scale is stripped, not honored"
      (let [fr (-> ds (pj/pose :x :y {:x-scale {:type :log}}) pj/lay-point)
            panel (first (:panels (pj/plan fr)))]
        (is (not (contains? (:mapping fr) :x-scale))
            ":x-scale should not appear in pose mapping")
        (is (= :linear (get-in panel [:x-scale :type]))
            "panel x-scale should stay at default :linear")))

    (testing "layer-level :coord is stripped, not honored"
      (let [fr (-> ds (pj/pose :x :y) (pj/lay-point {:coord :flip}))
            layer-mapping (:mapping (first (:layers fr)))
            panel (first (:panels (pj/plan fr)))]
        (is (not (contains? (or layer-mapping {}) :coord))
            ":coord should not appear in layer mapping")
        (is (not= :flip (:coord panel))
            "panel coord should not reflect a stripped layer-level :flip")))

    (testing "canonical pj/scale still works"
      (let [fr (-> ds (pj/pose :x :y) pj/lay-point (pj/scale :x :log))
            panel (first (:panels (pj/plan fr)))]
        (is (= :log (get-in panel [:x-scale :type])))))

    (testing "canonical pj/coord still works"
      (let [fr (-> ds (pj/pose :x :y) pj/lay-point (pj/coord :flip))
            panel (first (:panels (pj/plan fr)))]
        (is (= :flip (:coord panel)))))))

(deftest input-validation-misc-test
  ;; persona-09-R2 F10/F11/F12. Low-severity input validation.
  (testing "options with width/height = 0 throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":width must round to a positive integer"
                          (-> {:a [1 2 3] :b [4 5 6]} (pj/lay-point :a :b)
                              (pj/options {:width 0}))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":height must round to a positive integer"
                          (-> {:a [1 2 3] :b [4 5 6]} (pj/lay-point :a :b)
                              (pj/options {:height -100})))))

  (testing "options with fractional width that rounds to 0 throws (persona R2 internals)"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":width must round to a positive integer"
                          (-> {:a [1 2 3] :b [4 5 6]} (pj/lay-point :a :b)
                              (pj/options {:width 0.4})))))

  (testing "histogram with :bins <= 0 throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":bins must be a positive number"
                          (-> {:x [1 2 3]} (pj/lay-histogram :x {:bins 0}) pj/plan)))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":bins must be a positive number"
                          (-> {:x [1 2 3]} (pj/lay-histogram :x {:bins -1}) pj/plan))))

  (testing "histogram with :binwidth <= 0 throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":binwidth must be a positive number"
                          (-> {:x [1 2 3]} (pj/lay-histogram :x {:binwidth 0}) pj/plan))))

  (testing "lay-rule-h/v 1-arity throws helpful error pointing at the required intercept opt"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires an opts map with :y-intercept"
                          (pj/lay-rule-h (pj/pose))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires an opts map with :x-intercept"
                          (pj/lay-rule-v (pj/pose)))))

  (testing "lay-band-h/v 1-arity throws helpful error pointing at min/max opts"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires an opts map with :y-min and :y-max"
                          (pj/lay-band-h (pj/pose))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires an opts map with :x-min and :x-max"
                          (pj/lay-band-v (pj/pose)))))

  (testing "lay-* with unknown :position throws (parallel to :mark/:stat validation)"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":position :nonsense.*not a registered position"
                          (pj/lay-point {:a [1 2] :b [3 4]} :a :b {:position :nonsense})))))

(deftest svg-summary-aesthetic-coverage-test
  ;; persona round-2 (test-quality auditor) flagged that notebook
  ;; tests asserting only :points / :lines / :panels could pass even
  ;; if a :color "#hex" / :size N / :alpha N / :shape :col mapping
  ;; were silently dropped (the regression class that bit
  ;; tile-color-synonym-test). pj/svg-summary now extracts data-mark
  ;; aesthetic variety so a single predicate catches each failure
  ;; mode.
  (let [iris-ds (tc/dataset {:x (range 60)
                             :y (mapv (partial * 2) (range 60))
                             :g (concat (repeat 20 "a") (repeat 20 "b") (repeat 20 "c"))})
        base    (-> iris-ds (pj/lay-point :x :y))]
    (testing ":colors set is non-empty even on a default plot"
      (is (seq (:colors (pj/svg-summary base)))))

    (testing "explicit :color literal appears in :colors set"
      (let [s (pj/svg-summary (-> iris-ds (pj/lay-point :x :y {:color "#E74C3C"})))]
        (is (contains? (:colors s) "rgb(231,76,60)")
            "the literal #E74C3C must reach the rendered SVG")))

    (testing ":color column mapping produces strictly more colors than the default"
      (let [s-default (pj/svg-summary base)
            s-mapped  (pj/svg-summary (-> iris-ds (pj/lay-point :x :y {:color :g})))]
        (is (> (count (:colors s-mapped)) (count (:colors s-default)))
            "mapping :color :g (3 categories) must add colors beyond default")))

    (testing "explicit :size literal appears in :sizes set"
      (let [s (pj/svg-summary (-> iris-ds (pj/lay-point :x :y {:size 12})))]
        (is (contains? (:sizes s) 12.0)
            "the literal :size 12 must reach the rendered :rx")))

    (testing "explicit :alpha literal appears in :alphas set"
      (let [s (pj/svg-summary (-> iris-ds (pj/lay-point :x :y {:alpha 0.3})))]
        (is (contains? (:alphas s) 0.3)
            "the literal :alpha 0.3 must reach the rendered fill-opacity")))

    (testing ":shape column mapping produces multiple SVG primitive types"
      (let [s-default (pj/svg-summary base)
            s-mapped  (pj/svg-summary (-> iris-ds (pj/lay-point :x :y {:shape :g})))]
        (is (= 1 (count (:shapes s-default)))
            "default plot uses a single primitive type")
        (is (> (count (:shapes s-mapped)) 1)
            "mapping :shape :g (3 categories) must produce multiple primitive types")))

    (testing "every marker is counted when :shape is mapped"
      ;; A :square marker draws as a rounded rect of radius 0. Requiring a
      ;; positive rx to count a point dropped all 20 squares from the
      ;; summary: they failed that test and the nil-rx test for tiles, so
      ;; they landed in no bucket and 60 rows summarized as 40 marks.
      (let [s (pj/svg-summary (-> iris-ds (pj/lay-point :x :y {:shape :g})))]
        (is (= 60 (+ (:points s) (:polygons s)))
            "circles and squares count as :points, triangles as :polygons")
        (is (zero? (:tiles s))
            "square markers must not be mistaken for heatmap tiles")))))

(deftest alpha-on-marks-test
  ;; persona-16 H4 + H7. Closes P11-R2 F1, F2.
  ;; H4: line/step/lm/loess/errorbar/lollipop -- alpha was put in style but
  ;;     hardcoded to 1.0 in the renderer.
  ;; H7: boxplot/pointrange/text/label -- alpha wasn't in style at all.
  (let [data {:x [1.0 2.0 3.0 4.0 5.0] :y [10.0 20.0 15.0 25.0 18.0]
              :lab ["A" "B" "C" "D" "E"]
              :cat ["a" "b" "a" "b" "a"]}
        tmp (java.io.File/createTempFile "plotje-alpha-test" ".svg")
        renders-with-opacity? (fn [sketch op-pattern]
                                (pj/save sketch (.getAbsolutePath tmp))
                                (boolean (re-find op-pattern (slurp tmp))))]
    (testing "alpha 0.5 propagates to :line"
      (is (renders-with-opacity? (-> data (pj/lay-line :x :y {:alpha 0.5}))
                                 #"opacity=\"0.5\"")))

    (testing "alpha 0.5 propagates to :step"
      (is (renders-with-opacity? (-> data (pj/lay-step :x :y {:alpha 0.5}))
                                 #"opacity=\"0.5\"")))

    (testing "alpha 0.5 propagates to :lm regression line"
      (is (renders-with-opacity? (-> data (pj/lay-smooth :x :y {:stat :linear-model :alpha 0.5}))
                                 #"opacity=\"0.5\"")))

    (testing "alpha 0.4 propagates to :errorbar"
      (is (renders-with-opacity? (-> {:x [1.0 2.0] :y [10.0 20.0]
                                      :lo [5.0 15.0] :hi [15.0 25.0]}
                                     (pj/lay-errorbar :x :y {:y-min :lo :y-max :hi :alpha 0.4}))
                                 #"opacity=\"0.4\"")))

    (testing "alpha 0.6 propagates to :lollipop"
      (is (renders-with-opacity? (-> data (pj/lay-lollipop :cat :y {:alpha 0.6}))
                                 #"opacity=\"0.6\"")))

    (testing "alpha 0.3 propagates to :text"
      (is (renders-with-opacity? (-> data (pj/lay-text :x :y {:text :lab :alpha 0.3}))
                                 #"opacity=\"0.3\"")))

    (testing "alpha 0.7 propagates to :label"
      (is (renders-with-opacity? (-> data (pj/lay-label :x :y {:text :lab :alpha 0.7}))
                                 #"opacity=\"0.7\"")))

    (testing "alpha 0.4 propagates to :boxplot"
      (is (renders-with-opacity? (-> data (pj/lay-boxplot :cat :y {:alpha 0.4}))
                                 #"opacity=\"0.4\"")))

    (testing "alpha 0.8 propagates to :pointrange (via :summary)"
      (is (renders-with-opacity? (-> data (pj/lay-summary :cat :y {:alpha 0.8}))
                                 #"opacity=\"0.8\"")))

    (.delete tmp)))

(deftest bar-numeric-x-test
  ;; persona-16 H9. Closes P9-R2 F7.
  (testing "lay-bar counting with numeric x throws clear error"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"lay-bar \(counting\) requires a categorical column for :x"
                          (-> {:x [1 2 3 4 5]} (pj/lay-bar :x) pj/plan))))

  (testing "lay-bar value bars with two numeric axes draw numeric-position bars"
    ;; (see numeric-position-bar-test) -- no longer an error
    (is (= 3 (:polygons (pj/svg-summary
                         (pj/plot (-> {:x [1.0 2.0 3.0] :y [10 20 30]}
                                      (pj/lay-bar :x :y))))))))

  (testing "the numeric-x error suggests the :x-type override"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"\{:x-type :categorical\}"
                          (-> {:x [1 2 3 4 5]} (pj/lay-bar :x) pj/plan))))

  (testing "lay-bar counting with categorical x still works"
    (is (some? (-> {:cat ["a" "b" "c"]} (pj/lay-bar :cat) pj/plan))))

  (testing "lay-bar value bars with categorical x still works"
    (is (some? (-> {:cat ["a" "b" "c"] :y [10 20 30]}
                   (pj/lay-bar :cat :y) pj/plan))))

  (testing "{:x-type :categorical} lets lay-bar accept a numeric x"
    (is (some? (-> {:x [1 2 3] :y [10 20 30]}
                   (pj/lay-bar :x :y {:x-type :categorical}) pj/plan)))))

(deftest horizontal-value-bar-test
  (let [rank {:country ["US" "China" "Japan"] :gdp [21.4 14.7 5.1]}
        grp {:cat ["A" "A" "B" "B"] :val [10 20 30 40] :g ["x" "y" "x" "y"]}]
    (testing "category on y draws horizontal value bars (band scale on y)"
      (let [p (-> rank (pj/lay-bar :gdp :country) pj/plan :panels first)]
        (is (true? (:categorical? (:y-ticks p))))
        (is (not (:categorical? (:x-ticks p))))
        ;; numeric (value) axis is x, anchored at/through 0
        (is (<= (double (first (:x-domain p))) 0.0))))
    (testing "vertical bars unchanged (band scale on x)"
      (let [p (-> rank (pj/lay-bar :country :gdp) pj/plan :panels first)]
        (is (true? (:categorical? (:x-ticks p))))
        (is (not (:categorical? (:y-ticks p))))))
    (testing "grouped horizontal bars dodge"
      (is (= 4 (:polygons (pj/svg-summary
                           (pj/plot (pj/lay-bar grp :val :cat
                                                {:color :g :position :dodge})))))))
    (testing "horizontal stack/fill redirect to coord :flip with a clear error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Stacked/filled horizontal value bars.*coord :flip"
                            (-> grp (pj/lay-bar :val :cat {:color :g :position :stack}) pj/plan)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Stacked/filled horizontal value bars"
                            (-> grp (pj/lay-bar :val :cat {:color :g :position :fill}) pj/plan))))))

(deftest numeric-position-bar-test
  (testing "numeric x + numeric y draws one bar per row at its x position"
    (let [p (-> {:x [1 2 3 4 5] :y [10 20 15 30 25]} (pj/lay-bar :x :y) pj/plan
                :panels first)]
      (is (not (:categorical? (:x-ticks p))))
      ;; x-domain widened by half a bar each side (gap 1, width 0.9 -> half 0.45)
      ;; plus padding, so it extends below the data minimum of 1.0
      (is (< (double (first (:x-domain p))) 1.0))
      (is (= 5 (:polygons (pj/svg-summary
                           (pj/plot (-> {:x [1 2 3 4 5] :y [10 20 15 30 25]}
                                        (pj/lay-bar :x :y)))))))))
  (testing "{:bar-width ...} overrides the inferred width"
    (let [wide (-> {:x [1 2 3] :y [10 20 15]} (pj/lay-bar :x :y {:bar-width 0.5})
                   pj/plan :panels first :x-domain)
          [lo hi] wide]
      ;; half-width 0.25 -> domain spans roughly [0.75, 3.25] before padding
      (is (< 0.5 (double lo) 0.8))
      (is (< 3.2 (double hi) 3.5))))
  (testing "numeric bars support negative heights (diverging)"
    (is (= 3 (:polygons (pj/svg-summary
                         (pj/plot (-> {:x [1 2 3] :y [-10 20 -5]} (pj/lay-bar :x :y))))))))
  (testing "temporal x draws bars with calendar tick labels"
    (let [p (-> {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"] :v [10 20 15]}
                (pj/lay-bar :date :v) pj/plan :panels first)]
      (is (not (:categorical? (:x-ticks p))))
      (is (= 3 (:polygons (pj/svg-summary
                           (pj/plot (-> {:date [#inst "2024-01-01" #inst "2024-02-01" #inst "2024-03-01"] :v [10 20 15]}
                                        (pj/lay-bar :date :v)))))))))
  (testing "a bare pose with temporal x still infers a line, not bars"
    (is (= :line (-> {:date [#inst "2024-01-01" #inst "2024-02-01"] :v [10 20]}
                     (pj/pose :date :v) pj/plan :panels first :layers first :mark)))))

(deftest value-bar-log-scale-error-test
  ;; Value bars rest on a zero baseline, which a log value-axis cannot
  ;; represent. Instead of an internal pad-domain invariant crash, the user
  ;; gets a clear, actionable message pointing at the alternatives.
  (let [re #"log scale cannot include zero or negative"]
    (testing "vertical value bars on a log y axis"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo re
                            (-> {:c ["a" "b" "c"] :v [10 100 1000]}
                                (pj/lay-bar :c :v) (pj/scale :y :log) pj/plan))))
    (testing "numeric-position bars on a log y axis"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo re
                            (-> {:x [1 2 3] :y [10 100 1000]}
                                (pj/lay-bar :x :y) (pj/scale :y :log) pj/plan))))
    (testing "horizontal value bars on a log x axis"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo re
                            (-> {:c ["a" "b" "c"] :v [10 100 1000]}
                                (pj/lay-bar :v :c) (pj/scale :x :log) pj/plan))))
    (testing "count bars and histograms still work on a log axis"
      (is (some? (-> {:c ["a" "a" "b" "c" "c" "c"]} (pj/lay-bar :c)
                     (pj/scale :y :log) pj/plan)))
      (is (some? (-> {:x (range 100)} (pj/lay-histogram :x {:bins 10})
                     (pj/scale :y :log) pj/plan))))))

(deftest lollipop-y-type-categorical-rejected-test
  ;; user-report-3 issue 4: passing :y-type :categorical on a numeric :y
  ;; column previously NPE'd deep in the renderer; the layer requires a
  ;; numerical :y column.
  (testing "lay-lollipop with :y-type :categorical throws clear guidance"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires a numerical :y column.*pj/coord :flip"
                          (-> {:cat ["a" "b" "c"] :n [10 5 8]}
                              (pj/lay-lollipop :cat :n {:y-type :categorical})
                              pj/plan))))

  (testing "lay-lollipop without the bogus override still works"
    (is (some? (-> {:cat ["a" "b" "c"] :n [10 5 8]}
                   (pj/lay-lollipop :cat :n)
                   (pj/coord :flip)
                   pj/plan)))))

(deftest x-type-categorical-on-localdate-test
  ;; user-report-3 issue 2: {:x-type :categorical} on a LocalDate column
  ;; previously ClassCastException'd inside filter-infinities because
  ;; :packed-local-date reports as a numeric dtype.
  (testing "lay-lollipop with :x-type :categorical on a LocalDate column"
    (let [d (tc/dataset {:date [(java.time.LocalDate/of 2024 1 1)
                                (java.time.LocalDate/of 2024 6 1)
                                (java.time.LocalDate/of 2024 12 1)]
                         :value [3 8 5]})]
      (is (some? (-> d
                     (pj/lay-lollipop :date :value {:x-type :categorical})
                     (pj/coord :flip)
                     pj/plan))))))

(deftest visual-channel-scales-test
  ;; user-report-3 issue 1: pj/scale extended to :size, :alpha, :fill, :color.
  (let [d (tc/dataset {:user [:a :b :c] :n [10 100 1000]})]

    (testing "pj/scale :size :log produces a log-spaced size legend"
      (let [plan (-> d
                     (pj/lay-point :user :n {:size :n :x-type :categorical})
                     (pj/scale :size :log)
                     pj/plan)
            sl (:size-legend plan)]
        (is (= :log (:scale-type sl)))
        ;; log10(10), log10(100), log10(1000) = 1, 2, 3 are evenly
        ;; spaced, so the middle value sits halfway across the domain.
        ;; Where it lands between the radii is the scale's `:by`, which
        ;; defaults to :sqrt: 2 + 6*sqrt(0.5).
        (is (= [10.0 100.0 1000.0] (mapv :value (:entries sl))))
        (is (= [2.0 6.243 8.0]
               (mapv #(-> (:magnitude %) (* 1000) Math/round (/ 1000.0))
                     (:entries sl))))))

    (testing "and :by :linear spreads the same values evenly across the radii"
      (let [sl (-> d
                   (pj/lay-point :user :n {:size :n :x-type :categorical})
                   (pj/scale :size {:type :log :by :linear})
                   pj/plan
                   :size-legend)]
        (is (= [2.0 5.0 8.0] (mapv :magnitude (:entries sl))))))

    (testing "pj/scale :alpha :log produces a log-spaced alpha legend"
      (let [plan (-> d
                     (pj/lay-point :user :n {:alpha :n :x-type :categorical})
                     (pj/scale :alpha :log)
                     pj/plan)
            al (:alpha-legend plan)]
        (is (= :log (:scale-type al)))
        (is (= [10.0 100.0 1000.0] (mapv :value (:entries al))))))

    (testing "linear remains the default and is unchanged"
      (let [plan (-> d
                     (pj/lay-point :user :n {:size :n :x-type :categorical})
                     pj/plan)]
        (is (= :linear (:scale-type (:size-legend plan))))))

    (testing "non-positive values warn under :size :log (mirroring axis behavior)"
      (let [d-zero (tc/dataset {:user [:a :b :c :d] :n [10 100 1000 0]})
            out (with-out-str
                  (-> d-zero
                      (pj/lay-point :user :n {:size :n :x-type :categorical})
                      (pj/scale :size :log)
                      pj/plan))]
        (is (re-find #"non-positive values \(log scale on :n\)" out))))

    (testing ":categorical rejected on continuous visual channels"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Visual channel :size is continuous"
                            (pj/scale (pj/pose d) :size :categorical)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Visual channel :fill is continuous"
                            (pj/scale (pj/pose d) :fill :categorical)))))

  (testing ":fill :log on a tile heatmap plans cleanly with log scale-type"
    (let [d (tc/dataset (for [r (range 10) c (range 10)]
                          {:r r :c c :v (Math/pow 10.0 (/ (+ r c) 4.0))}))
          plan (-> d
                   (pj/lay-tile :r :c {:fill :v})
                   (pj/scale :fill :log)
                   pj/plan)
          legend (:legend plan)]
      (is (= :continuous (:type legend)))
      (is (= :log (:scale-type legend)))
      (is (seq (:ticks legend)) "log-spaced tick labels populated")))

  (testing "user-report-3 repro: pj/plot succeeds on size :log"
    (let [d (tc/dataset {:user [:a :b :c] :n [10 100 1000]})]
      (is (some? (-> d
                     (pj/lay-point :user :n {:size :n :x-type :categorical})
                     (pj/scale :size :log)
                     pj/plot)))))

  (testing "user-report-3 repro: pj/plot succeeds on tile fill :log"
    (let [d (tc/dataset (for [r (range 5) c (range 5)]
                          {:r r :c c :v (Math/pow 10.0 (/ (+ r c) 2.0))}))]
      (is (some? (-> d
                     (pj/lay-tile :r :c {:fill :v})
                     (pj/scale :fill :log)
                     pj/plot))))))

(deftest unknown-option-warning-test
  ;; persona-16 H1. Closes P1-R3 F2/F3/F4, Skept-R4 F4, P5-R2 L4.
  (let [data {:x [1 2 3] :y [10 20 30]}]
    (testing "pj/pose (position + opts) warns on unknown option key"
      (let [out (with-out-str (-> data (pj/pose :x :y {:colour :y}) pj/lay-point))]
        (is (re-find #"Warning: pj/pose does not recognize option" out))
        (is (re-find #":colour" out))))

    (testing "pj/pose (aesthetic-only opts) warns on unknown option key"
      (let [out (with-out-str (pj/pose data {:colour :y}))]
        (is (re-find #"Warning: pj/pose does not recognize option" out))))

    (testing "pj/options warns on unknown option key"
      (let [out (with-out-str (-> data (pj/lay-point :x :y) (pj/options {:titel "Hi"})))]
        (is (re-find #"Warning: pj/options does not recognize option" out))
        (is (re-find #":titel" out))))

    (testing "valid options stay quiet"
      (is (= "" (with-out-str (-> data (pj/pose :x :y {:color :y}))))))))

(deftest raw-data-plan-plot-test
  ;; persona-16 H2. Closes P1-R3 F2/F3, P9-R2 L2.
  (let [data {:x [1 2 3] :y [10 20 30]}]
    (testing "pj/plan on raw data does not throw"
      (is (some? (-> data pj/plan))))

    (testing "pj/plot on raw data does not throw"
      (is (some? (-> data pj/plot))))))

(deftest unknown-layer-type-test
  ;; persona-16 H3. Closes Skept-R4 F3.
  (let [data {:x [1 2 3] :y [10 20 30]}]
    (testing "unknown layer type keyword throws with registered list"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Unknown layer type: :stackedbar.*Registered layer types"
                            (-> data (pj/pose :x :y) (pj/lay :stackedbar) pj/plan))))))

(deftest scale-type-validation-test
  ;; persona-16 H12. Closes P1-R3 F5.
  (let [data {:x [1 2 3] :y [10 20 30]}]
    (testing "pj/scale with bogus type throws at API call time"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Unknown scale type: :bogus.*linear.*log"
                            (-> data (pj/lay-point :x :y) (pj/scale :x :bogus)))))

    (testing "pj/scale :linear and :log accepted"
      (is (some? (-> data (pj/lay-point :x :y) (pj/scale :x :linear))))
      (is (some? (-> data (pj/lay-point :x :y) (pj/scale :y :log)))))))

(deftest keyword-category-label-test
  ;; persona-16 H10. Closes P17 #9, #11.
  (let [data {:cat [:widget :gadget :sprocket] :n [10 20 15]}]
    (testing "keyword categorical x-axis labels render without leading colons"
      (let [svg (-> data (pj/lay-bar :cat) pj/plot pr-str)]
        (is (zero? (count (re-seq #":(widget|gadget|sprocket)" svg))))
        (is (= 3 (count (re-seq #"\"widget\"|\"gadget\"|\"sprocket\"" svg))))))

    (testing "keyword categorical legend labels render without leading colons"
      (let [svg (-> {:x [1 2 3] :y [10 20 30] :g [:cat-0 :cat-1 :cat-2]}
                    (pj/lay-point :x :y {:color :g}) pj/plot pr-str)]
        (is (zero? (count (re-seq #":cat-[0-9]" svg))))))

    (testing "facet strip labels strip keyword colons"
      (let [svg (-> {:x [1 2 3 4] :y [10 20 30 40] :grp [:a :b :a :b]}
                    (pj/lay-point :x :y) (pj/facet :grp) pj/plot pr-str)]
        (is (zero? (count (re-seq #":(a|b)" svg))))))

    (testing "string categories still work (regression check)"
      (let [svg (-> {:cat ["widget" "gadget"] :n [10 20]} (pj/lay-bar :cat) pj/plot pr-str)]
        (is (= 2 (count (re-seq #"\"widget\"|\"gadget\"" svg))))))))

(deftest nil-in-aesthetic-columns-test
  ;; persona-16 B2. Closes C3 epic: P5-R2 C1, P9-R2 F1/F2,
  ;; P7-R2 F1/F2/F3, P11-R2 F5.
  (testing "nil in numeric :size column drops rows cleanly"
    (let [p (-> {:x [1.0 2.0 3.0 4.0] :y [10.0 20.0 30.0 40.0]
                 :sz [1.0 nil 3.0 nil]}
                (pj/lay-point :x :y {:size :sz}) pj/plan)]
      (is (= 1 (count (:panels p))))
      (is (some? (:size-legend p)) "size legend still built from 2 valid rows")))

  (testing "nil in numeric :color column drops rows cleanly"
    (let [p (-> {:x [1.0 2.0 3.0 4.0] :y [10.0 20.0 30.0 40.0]
                 :c [1.0 2.0 nil 4.0]}
                (pj/lay-point :x :y {:color :c}) pj/plan)]
      (is (= 1 (count (:panels p))))
      (is (some? (:legend p)) "numeric color legend still built")))

  (testing "NaN in numeric :size column drops rows cleanly"
    (let [p (-> {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]
                 :sz [1.0 ##NaN 3.0]}
                (pj/lay-point :x :y {:size :sz}) pj/plan)]
      (is (= 1 (count (:panels p))))))

  (testing "all-nil :size column renders without legend"
    (let [p (-> {:x [1.0 2.0] :y [10.0 20.0] :sz [nil nil]}
                (pj/lay-point :x :y {:size :sz}) pj/plan)]
      (is (= 1 (count (:panels p))))
      (is (nil? (:size-legend p)) "size legend suppressed when all nil")))

  (testing "all-nil :alpha column renders without legend"
    (let [p (-> {:x [1.0 2.0] :y [10.0 20.0] :a [nil nil]}
                (pj/lay-point :x :y {:alpha :a}) pj/plan)]
      (is (= 1 (count (:panels p))))
      (is (nil? (:alpha-legend p)) "alpha legend suppressed when all nil")))

  (testing "nil in :y-min / :y-max drops rows for errorbar"
    (let [p (-> {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]
                 :lo [5.0 nil 25.0] :hi [15.0 nil 35.0]}
                (pj/lay-errorbar :x :y {:y-min :lo :y-max :hi}) pj/plan)]
      (is (= 1 (count (:panels p)))))))

(deftest pose-color-mapping-composes-test
  ;; persona-16 B5. Closes P1-R3 F1, P9-R2 F14.
  ;; A color aesthetic on a pose composes with the position mapping.
  (let [data {:x [1 2 3] :y [10 20 30] :g ["a" "b" "c"]}]
    (testing "leaf pose renders with merged color mapping"
      (let [p (-> data (pj/pose :x :y {:color :g}) pj/lay-point pj/plan)
            layer (first (:layers (first (:panels p))))]
        (is (= 3 (count (:groups layer))) "one group per :g value")))))

(deftest facet-broadcast-test
  (testing "Root-scope lay-smooth (loess) applies to all facet panels"
    (let [iris (rdatasets/datasets-iris)
          s (-> iris
                (pj/lay-point :sepal-length :sepal-width {:color :species})
                (pj/facet :species)
                pj/lay-smooth
                pj/plot
                pj/svg-summary)]
      (is (= 3 (:panels s)) "Faceted into 3 panels by species")
      (is (= 3 (:lines s)) "LOESS should appear in all 3 panels")
      (is (= 150 (:points s)) "Scatter points still render")))
  (testing "Existing faceted behavior unchanged"
    (let [s (-> (rdatasets/datasets-iris)
                (pj/lay-point :sepal-length :sepal-width {:color :species})
                (pj/facet :species)
                pj/plot
                pj/svg-summary)]
      (is (= 3 (:panels s)))
      (is (= 150 (:points s))))))

;; ============================================================
;; PROPOSED API () tests
;; ============================================================

(defn- summary
  "Render a pose and return svg-summary."
  [fr]
  (pj/svg-summary (pj/plot fr)))

(deftest basic-test
  (let [iris (tc/dataset "https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv"
                         {:key-fn keyword})]
    (testing "Scatter + lm"
      (let [s (summary (-> (pj/pose iris {:color :species})
                           (pj/pose :sepal_length :sepal_width)
                           (pj/lay-point {:alpha 0.5})
                           (pj/lay-smooth {:stat :linear-model})))]
        (is (= 1 (:panels s)))
        (is (= 150 (:points s)))
        (is (= 3 (:lines s)))))

    (testing "SPLOM inference"
      (let [s (summary (-> (pj/pose iris {:color :species})
                           (pj/pose (pj/cross [:sepal_length :sepal_width :petal_length]
                                              [:sepal_length :sepal_width :petal_length]))))]
        (is (= 9 (:panels s)))
        (is (= 900 (:points s)))))

    (testing "Simpson's paradox via nil cancellation"
      (let [s (summary (-> (pj/pose iris {:color :species})
                           (pj/pose :sepal_length :sepal_width)
                           (pj/lay-point {:alpha 0.4})
                           (pj/lay-smooth {:stat :linear-model})
                           (pj/lay-smooth {:stat :linear-model :color nil})))]
        (is (= 1 (:panels s)))
        (is (= 150 (:points s)))
        (is (= 4 (:lines s)))))

    (testing "Faceted + multiple layers"
      (let [s (summary (-> (pj/pose iris)
                           (pj/pose :sepal_length :sepal_width)
                           pj/lay-point
                           pj/lay-smooth
                           (pj/facet :species)))]
        (is (= 3 (:panels s)))
        (is (= 150 (:points s)))
        (is (= 3 (:lines s)))))

    (testing "Data-first (no sketch call)"
      (let [s (summary (-> iris
                           (pj/pose :sepal_length :sepal_width {:color :species})
                           (pj/lay-point {:alpha 0.5})
                           (pj/lay-smooth {:stat :linear-model})))]
        (is (= 1 (:panels s)))
        (is (= 150 (:points s)))
        (is (= 3 (:lines s)))))

    (testing "Recipe"
      (let [recipe (-> (pj/pose)
                       (pj/pose :sepal_length :sepal_width)
                       (pj/lay-point)
                       (pj/lay-smooth {:stat :linear-model}))
            s (summary (pj/with-data recipe iris))]
        (is (= 1 (:panels s)))
        (is (= 150 (:points s)))))

    ;; "Mixed grid" (composite + facet) is deferred. Once two pj/pose
    ;; calls promote the leaf to a composite, pj/facet can't thread
    ;; through -- facet still routes through ensure-sk, which rejects
    ;; composites.

    (testing "Inference: one numerical column"
      (let [s (summary (-> (pj/pose iris)
                           (pj/pose :sepal_length)))]
        (is (pos? (:polygons s)))
        (is (zero? (:points s)))))

    (testing "Inference: one categorical column"
      (let [s (summary (-> (pj/pose iris)
                           (pj/pose :species)))]
        (is (= 3 (:polygons s)))))

    (testing "2D facet grid"
      (let [tips (tc/dataset "https://raw.githubusercontent.com/mwaskom/seaborn-data/master/tips.csv"
                             {:key-fn keyword})
            s (summary (-> (pj/pose tips {:color :smoker})
                           (pj/pose :total_bill :tip)
                           (pj/lay-point {:alpha 0.5})
                           (pj/facet-grid :day :sex)))]
        (is (= 8 (:panels s)))))

    (testing "Options pass through"
      (let [s (summary (-> (pj/pose iris)
                           (pj/pose :sepal_length :sepal_width)
                           (pj/lay-point)
                           (pj/options {:title "Test" :width 400})))]
        (is (= 1 (:panels s)))
        (is (some #{"Test"} (:texts s)))))

    (testing "Pose first, then layers"
      (let [s (summary (-> iris
                           (pj/pose :sepal_length :sepal_width {:color :species})
                           (pj/lay-point {:alpha 0.5})
                           (pj/lay-smooth {:stat :linear-model})))]
        (is (= 1 (:panels s)))
        (is (= 150 (:points s)))
        (is (= 3 (:lines s)))))

    (testing "Column inference"
      ;; Small dataset: pj/lay-point on raw data auto-infers x/y from
      ;; the first two columns (adapter behavior, preserved through
      ;; the Sketch retirement).
      (let [s (summary (pj/lay-point {:a [1 2 3 4 5] :b [2 4 3 5 4]}))]
        (is (= 5 (:points s)))))))

;; ---- Annotations-as-layers (pj/lay-rule-*, pj/lay-band-*) ----

(deftest lay-rule-band-test
  ;; Reference lines and shaded bands are first-class layers; these
  ;; tests cover root-scope vs layer-scope, facet interaction,
  ;; color/alpha overrides, and annotation-only domain synthesis.
  (let [ds (tc/dataset {:x [1 2 3 4 5] :y [2 4 3 5 4]})]

    (testing "root-scope rule-h attaches a single annotation"
      (let [p (pj/plan (-> ds
                           (pj/lay-point :x :y)
                           (pj/lay-rule-h {:y-intercept 3})))
            panel (first (:panels p))]
        (is (= 1 (count (:annotations panel))))
        (is (= :rule-h (:mark (first (:annotations panel)))))
        (is (= 3 (:y-intercept (first (:annotations panel)))))))

    (testing "root-scope rule applies to every facet panel (deduped)"
      ;; Without dedupe the cross-product would emit N copies per panel.
      (let [iris (tc/dataset "https://vincentarelbundock.github.io/Rdatasets/csv/datasets/iris.csv"
                             {:key-fn keyword})
            p (pj/plan (-> iris
                           (pj/lay-point :Sepal.Length :Sepal.Width)
                           (pj/facet :Species)
                           (pj/lay-rule-h {:y-intercept 3})))]
        (is (= 3 (count (:panels p))))
        (doseq [panel (:panels p)]
          (is (= 1 (count (:annotations panel)))
              (str "panel " (:row panel) "/" (:col panel) " had "
                   (count (:annotations panel)) " annotations")))))

    (testing "pj/lay-rule-v with :color and :alpha flows into the plan"
      (let [p (pj/plan (-> ds
                           (pj/lay-point :x :y)
                           (pj/lay-rule-v {:x-intercept 2 :color "red" :alpha 0.5})))
            a (first (:annotations (first (:panels p))))]
        (is (= :rule-v (:mark a)))
        (is (= 2 (:x-intercept a)))
        (is (= "red" (:color a)))
        (is (= 0.5 (:alpha a)))))

    (testing "pj/lay-band-h / pj/lay-band-v carry their min/max bounds"
      (let [p (pj/plan (-> ds
                           (pj/lay-point :x :y)
                           (pj/lay-band-h {:y-min 2 :y-max 4})
                           (pj/lay-band-v {:x-min 1 :x-max 3})))
            anns (:annotations (first (:panels p)))
            band-h (first (filter #(= :band-h (:mark %)) anns))
            band-v (first (filter #(= :band-v (:mark %)) anns))]
        (is (= 2 (:y-min band-h)))
        (is (= 4 (:y-max band-h)))
        (is (= 1 (:x-min band-v)))
        (is (= 3 (:x-max band-v)))))

    (testing "positioned rule with root-scope data layer renders both"
      ;; Regression: data layer must coexist with a positioned annotation.
      (let [p (pj/plan (-> ds
                           (pj/pose :x :y)
                           pj/lay-point
                           (pj/lay-rule-h :x :y {:y-intercept 3})))
            panel (first (:panels p))]
        (is (= 1 (count (:layers panel))))
        (is (= :point (:mark (first (:layers panel)))))
        (is (= 1 (count (:annotations panel))))))

    (testing "plan with annotation layer validates against schema"
      (let [p (pj/plan (-> ds
                           (pj/lay-point :x :y)
                           (pj/lay-rule-h {:y-intercept 3 :color "red" :alpha 0.5})
                           (pj/lay-band-v {:x-min 1 :x-max 3 :color "blue" :alpha 0.2})))]
        (is (pj/valid-plan? p))))

    (testing "rendered SVG uses :color override for rule"
      (let [sk-red (-> ds
                       (pj/lay-point :x :y)
                       (pj/lay-rule-h {:y-intercept 3 :color "red"}))
            sk-default (-> ds
                           (pj/lay-point :x :y)
                           (pj/lay-rule-h {:y-intercept 3}))
            svg-red (str (pj/plan->plot (pj/plan sk-red) :svg {}))
            svg-default (str (pj/plan->plot (pj/plan sk-default) :svg {}))]
        (is (clojure.string/includes? svg-red "rgb(255,0,0)"))
        (is (not (clojure.string/includes? svg-default "rgb(255,0,0)")))))

    (testing "annotation-only pose still produces a panel"
      ;; Edge case: a pose with only a positioned annotation (no data
      ;; layer) should still infer a panel and render the annotation.
      ;; Domain comes from the pose's data columns plus the
      ;; annotation's own position.
      (let [p (pj/plan (-> ds
                           (pj/pose :x :y)
                           (pj/lay-rule-h :x :y {:y-intercept 3})))
            panel (first (:panels p))]
        (is (= 1 (count (:panels p))))
        (is (= 0 (count (:layers panel))))
        (is (= 1 (count (:annotations panel))))
        (is (= :rule-h (:mark (first (:annotations panel)))))
        ;; Domain spans both the column data and the intercept.
        (let [[y-lo y-hi] (:y-domain panel)]
          (is (<= y-lo 2))
          (is (>= y-hi 5)))))

    (testing "annotation-only panel with band extends domain to include band"
      (let [p (pj/plan (-> ds
                           (pj/pose :x :y)
                           (pj/lay-band-h :x :y {:y-min 10 :y-max 20})))
            panel (first (:panels p))
            [y-lo y-hi] (:y-domain panel)]
        (is (<= y-lo 2))
        (is (>= y-hi 20))))))

