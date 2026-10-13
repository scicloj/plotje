(ns scicloj.plotje.share-scales-test
  "`:share-scales` on a facet and on an arrangement: the set of axes,
   the map from an axis to which columns go together, and the retired
   `:scales` option it replaces."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.stat :as stat]
            [scicloj.plotje.layer-type :as layer-type]
            [tablecloth.api :as tc]))

(def cars
  {:size [1.0 2.0 3.0 4.0 5.0 6.0]
   :fast [30.0 28.0 26.0 24.0 22.0 20.0]
   :slow [20.0 18.0 16.0 14.0 12.0 10.0]
   :weight [1000.0 1100.0 1200.0 1300.0 1400.0 1500.0]
   :kind ["a" "a" "a" "b" "b" "b"]})

(defn- domains
  "Every panel's [x-domain y-domain], for a leaf or a composite plan."
  [pose]
  (let [walk (fn walk [plan]
               (if-let [subs (:sub-plots plan)]
                 (mapcat #(walk (:plan %)) subs)
                 (map (juxt :x-domain :y-domain) (:panels plan))))]
    (vec (walk (pj/plan pose)))))

(defn- distinct-counts [pose]
  (let [ds (domains pose)]
    [(count (distinct (map first ds))) (count (distinct (map second ds)))]))

(def by-kind
  (-> cars
      (pj/pose {:x :size :y :fast})
      pj/lay-point
      (pj/facet :kind)))

(def over-columns
  (-> cars
      (pj/pose {:x :size})
      pj/lay-point
      (pj/facet {:y [:fast :slow]})))

(deftest share-scales-spec-test
  (testing "a set is the short form of :same-column on each axis"
    (is (= {:x :same-column :y :same-column} (defaults/share-scales-spec #{:x :y})))
    (is (= {} (defaults/share-scales-spec #{})))
    (is (nil? (defaults/share-scales-spec nil)))
    (is (= {:y :all} (defaults/share-scales-spec {:y :all}))))
  (testing "the axes named, where an empty set is written and nil is not"
    (is (= #{} (defaults/share-scales-axes #{})))
    (is (nil? (defaults/share-scales-axes nil)))
    (is (= #{:y} (defaults/share-scales-axes {:y [[:a :b]]})))))

(deftest share-scales-on-a-facet-test
  (testing "the panels of a facet share both axes with nothing written"
    (is (= [1 1] (distinct-counts by-kind))))
  (testing "a set names the axes shared; an axis left out is per panel"
    (is (= [1 1] (distinct-counts (pj/options by-kind {:share-scales #{:x :y}}))))
    (is (= [1 2] (distinct-counts (pj/options by-kind {:share-scales #{:x}}))))
    (is (= [2 1] (distinct-counts (pj/options by-kind {:share-scales #{:y}}))))
    (is (= [2 2] (distinct-counts (pj/options by-kind {:share-scales #{}})))))
  (testing "writing the default draws the default"
    (is (= (pj/plot by-kind)
           (pj/plot (pj/options by-kind {:share-scales #{:x :y}})))))
  (testing "the map form names the same axes on a facet"
    (is (= [1 2] (distinct-counts (pj/options by-kind {:share-scales {:x :same-column}}))))))

(deftest retired-scales-test
  (testing ":scales draws what the :share-scales it stands for draws, and warns"
    (doseq [[old new] [[:free-y #{:x}] [:free-x #{:y}] [:free #{}]]]
      (let [out (java.io.StringWriter.)
            retired (binding [*out* out] (pj/options by-kind {:scales old}))]
        (is (re-find #":scales is retired" (str out)))
        (is (re-find (re-pattern (java.util.regex.Pattern/quote (str "{:share-scales " (pr-str new) "}")))
                     (str out)))
        (is (= new (get-in retired [:opts :share-scales])))
        (is (not (contains? (:opts retired) :scales)))
        (is (= (pj/plot retired)
               (pj/plot (pj/options by-kind {:share-scales new})))))))
  (testing ":scales :shared asked for the default and is dropped"
    (let [out (java.io.StringWriter.)
          retired (binding [*out* out] (pj/options by-kind {:scales :shared}))]
      (is (re-find #"asked for the default, so it is dropped" (str out)))
      (is (not (contains? (:opts retired) :share-scales)))
      (is (= (pj/plot retired) (pj/plot by-kind)))))
  (testing "written by hand in a pose map, it is read when the pose is drawn"
    (let [out (java.io.StringWriter.)
          by-hand (assoc-in by-kind [:opts :scales] :free-y)
          ds (binding [*out* out] (domains by-hand))]
      (is (re-find #"A pose's :opts: :scales is retired" (str out)))
      (is (= ds (domains (pj/options by-kind {:share-scales #{:x}}))))))
  (testing "a :share-scales written beside it wins"
    (let [p (binding [*out* (java.io.StringWriter.)]
              (pj/options by-kind {:scales :free :share-scales #{:x :y}}))]
      (is (= #{:x :y} (get-in p [:opts :share-scales])))))
  (testing "under :strict the retired option is an error"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #":scales is retired"
         (pj/with-config {:strict true}
           (pj/options by-kind {:scales :free-y}))))))

(deftest column-groups-test
  (testing "a set shares per column: two columns keep two ranges"
    (is (= [1 2] (distinct-counts over-columns)))
    (is (= [1 2] (distinct-counts (pj/options over-columns {:share-scales #{:x :y}})))))
  (testing "a group puts its columns on one range covering both"
    (let [p (pj/options over-columns {:share-scales {:x :same-column :y [[:fast :slow]]}})
          [[_ [lo hi]] [_ y2]] (domains p)]
      (is (= [1 1] (distinct-counts p)))
      (is (= [lo hi] y2))
      (is (<= lo 10.0))
      (is (>= hi 30.0))))
  (testing ":all puts every column together"
    (is (= (domains (pj/options over-columns {:share-scales {:x :same-column :y [[:fast :slow]]}}))
           (domains (pj/options over-columns {:share-scales {:x :same-column :y :all}})))))
  (testing "a column in no group keeps its own range"
    (let [p (-> cars
                (pj/pose {:x :size})
                pj/lay-point
                (pj/facet {:y [:fast :slow :weight]})
                (pj/options {:share-scales {:x :same-column :y [[:fast :slow]]}}))
          [[_ fast] [_ slow] [_ weight]] (domains p)]
      (is (= fast slow))
      (is (not= fast weight))))
  (testing "an axis the map leaves out is not shared"
    (let [a (-> {:t [1 2 3] :u [1.0 2.0 3.0]} (pj/lay-point :t :u))
          b (-> {:t [10 20 30] :v [5.0 6.0 7.0]} (pj/lay-point :t :v))
          p (pj/arrange [a b] {:share-scales {:y [[:u :v]]}})]
      (is (= [2 1] (distinct-counts p)))))
  (testing "a group reaches across datasets naming one quantity differently"
    (let [a (-> {:day [1 2 3] :indoor [19.0 20.5 21.0]} (pj/lay-line :day :indoor))
          b (-> {:day [1 2 3] :outdoor [4.0 9.5 14.0]} (pj/lay-line :day :outdoor))]
      (is (= 2 (second (distinct-counts (pj/arrange [a b])))))
      (is (= 1 (second (distinct-counts
                        (pj/arrange [a b] {:share-scales {:y [[:indoor :outdoor]]}})))))))
  (testing "panels sharing through a group label the ticks once, on the outer panel"
    (let [texts (fn [pose] (count (filter #(re-matches #"\d+(\.\d+)?" %)
                                          (:texts (pj/svg-summary pose)))))
          side-by-side (fn [share]
                         (-> cars
                             (pj/pose {:x :size})
                             pj/lay-point
                             (assoc :layout {:direction :horizontal}
                                    :poses [{:mapping {:y :fast}} {:mapping {:y :slow}}])
                             (pj/options {:share-scales share})))]
      (is (< (texts (side-by-side {:x :same-column :y [[:fast :slow]]}))
             (texts (side-by-side {:x :same-column})))))))

(deftest share-scales-errors-test
  (testing "columns of different kinds cannot go together"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":share-scales :y refused: columns :fast, :kind have incompatible"
         (-> cars
             (pj/pose {:x :size})
             pj/lay-point
             (pj/facet {:y [:fast :kind]})
             (pj/options {:share-scales {:y :all}})
             pj/plan))))
  (testing "a value of the wrong shape is reported where it is written"
    (doseq [bad [{:y [[:fast]]} {:y :everything} {:z :all} {:y [:fast :slow]} #{:z} :x]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #":share-scales"
           (pj/options over-columns {:share-scales bad}))
          (pr-str bad))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"pj/arrange :share-scales"
           (pj/arrange [by-kind by-kind] {:share-scales bad}))
          (pr-str bad)))))

(deftest sharing-reaches-a-facet-below-test
  (testing "a :share-scales written on an arrangement reaches the facets of its cells"
    (let [arranged (pj/arrange [by-kind by-kind] {:share-scales #{:x}})
          per-cell (mapv #(count (distinct (map :y-domain (:panels (:plan %)))))
                         (:sub-plots (pj/plan arranged)))]
      (is (= [2 2] per-cell))))
  (testing "a cell that writes its own keeps it"
    (let [arranged (pj/arrange [(pj/options by-kind {:share-scales #{:x :y}}) by-kind]
                               {:share-scales #{:x}})
          per-cell (mapv #(count (distinct (map :y-domain (:panels (:plan %)))))
                         (:sub-plots (pj/plan arranged)))]
      (is (= [1 2] per-cell)))))

(deftest a-scale-written-on-a-facet-over-columns-test
  (testing "a domain set on the axis the facet varies reaches every panel"
    (doseq [pose [(pj/scale over-columns :y {:domain [0 50]})
                  (-> cars
                      (pj/pose {:x :size})
                      pj/lay-point
                      (pj/scale :y {:domain [0 50]})
                      (pj/facet {:y [:fast :slow]}))]]
      (let [out (java.io.StringWriter.)
            ys (binding [*out* out] (mapv second (domains pose)))]
        (is (= [[0 50] [0 50]] ys))
        (is (= "" (str out))))))
  (testing "a pose's own column on that axis is still replaced, with no message"
    (let [out (java.io.StringWriter.)
          ys (binding [*out* out]
               (mapv second (domains (-> cars
                                         (pj/pose :size :fast)
                                         pj/lay-point
                                         (pj/facet {:y [:fast :slow]})))))]
      (is (= 2 (count (distinct ys))))
      (is (= "" (str out))))))

(deftest share-scales-messages-test
  (let [printed (fn [pose] (let [out (java.io.StringWriter.)]
                             (binding [*out* out] (pj/plan pose))
                             (str out)))]
    (testing "a set on an axis where every cell draws its own column says so, once, and names the group"
      (let [out (printed (pj/options over-columns {:share-scales #{:x :y}}))]
        (is (= 1 (count (re-seq #"Warning" out))))
        (is (re-find #"no two cells share :y" out))
        (is (re-find #"\{:share-scales \{:y \[\[:fast :slow\]\]\}\}" out))))
    (testing "the same on an arrangement"
      (let [a (pj/lay-point cars :size :fast)
            b (pj/lay-point cars :size :slow)]
        (is (re-find #"no two cells share :y"
                     (printed (pj/arrange [a b] {:share-scales #{:y}}))))
        (is (= "" (printed (pj/arrange [a b] {:share-scales {:y [[:fast :slow]]}}))))))
    (testing "a group naming fewer than two drawn columns says so"
      (is (re-find #"Name the columns the cells draw"
                   (printed (pj/options over-columns {:share-scales {:y [[:fast :weight]]}})))))
    (testing "nothing is printed for the default, a working set, or a matrix of two columns"
      (is (= "" (printed over-columns)))
      (is (= "" (printed by-kind)))
      (is (= "" (printed (pj/options by-kind {:share-scales #{:x}}))))
      (is (= "" (printed (pj/cross-matrix cars [:fast :slow])))))
    (testing "a conflict with the cells' own :share-scales is one message, however many cells"
      (let [free (pj/options by-kind {:share-scales #{}})
            out (printed (pj/arrange [free free] {:share-scales #{:y}}))]
        (is (= 1 (count (re-seq #"Warning" out))))
        (is (re-find #"2 poses inside it write" out))))))

(deftest bars-drawn-across-start-at-zero-test
  ;; A bar is measured from zero on whichever axis its values run
  ;; along. With the values on `:x` the domain was padded below zero
  ;; alone, and under a shared `:x` it started at the smallest value, so
  ;; every bar was cut off there.
  (let [a {:cohort ["a" "b" "c"] :growth [12 20 29]}
        b {:cohort ["a" "b" "c"] :growth [15 22 25]}
        x-domains (fn [pose]
                    (let [plan (pj/plan pose)]
                      (if (:sub-plots plan)
                        (vec (for [sp (:sub-plots plan) p (:panels (:plan sp))] (:x-domain p)))
                        (mapv :x-domain (:panels plan)))))
        y-domains (fn [pose]
                    (vec (for [sp (:sub-plots (pj/plan pose)) p (:panels (:plan sp))] (:y-domain p))))]
    (testing "alone, the baseline sits on the panel edge"
      (is (= [[0.0 30.45]] (x-domains (pj/lay-bar a :growth :cohort)))))
    (testing "under a shared :x, as under a shared :y"
      (is (= [[0.0 30.45] [0.0 30.45]]
             (x-domains (pj/arrange [(pj/lay-bar a :growth :cohort) (pj/lay-bar b :growth :cohort)]
                                    {:share-scales #{:x}}))))
      (is (= [[0.0 30.45] [0.0 30.45]]
             (y-domains (pj/arrange [(pj/lay-bar a :cohort :growth) (pj/lay-bar b :cohort :growth)]
                                    {:share-scales #{:y}})))))
    (testing "values below zero are still padded past"
      (is (= [[-5.75 10.75]]
             (x-domains (pj/lay-bar {:cohort ["a" "b"] :growth [-5 10]} :growth :cohort)))))))

(deftest data-in-a-pose-written-as-a-map-test
  ;; `pj/pose` coerces its data; a pose written as a map may hold a
  ;; plain map, and its cells were not shared, with no message.
  (let [pose (fn [data]
               {:data data
                :mapping {:x :quarter}
                :layers [{:layer-type :bar}]
                :layout {:direction :vertical}
                :poses [{:mapping {:y :revenue}} {:mapping {:y :cost}}]
                :opts {:share-scales {:y :all}}})
        sales {:quarter ["Q1" "Q2" "Q3" "Q4"]
               :revenue [120.0 135.0 150.0 170.0]
               :cost [80.0 85.0 95.0 100.0]}]
    (is (= [[0.0 178.5] [0.0 178.5]] (mapv second (domains (pose sales)))))
    (is (= (domains (pose sales)) (domains (pose (tc/dataset sales)))))))

;; A shared axis covers what the sharing cells draw. The arrangement
;; measures each cell after its stats have run, as a facet measures its
;; panels, so the two agree.

(def survey
  {:group ["a" "a" "a" "a" "b" "b" "b" "b"]
   :item  ["p" "p" "q" "q" "q" "q" "r" "r"]
   :half  ["1" "2" "1" "2" "1" "2" "1" "2"]
   :n     [4.0 6.0 3.0 2.0 9.0 8.0 1.0 2.0]
   :low   [2.0 5.0 1.0 1.0 6.0 7.0 0.5 1.0]
   :high  [7.0 9.0 4.0 5.0 14.0 11.0 3.0 6.0]})

(defn- rows-of [group]
  (tc/select-rows (tc/dataset survey) #(= group (:group %))))

(defn- three-ways
  "The same two panels through a facet, through the facet's cells
   written out in the other order, and through an arrangement of two
   poses with data of their own and `axes` shared: three vectors of
   `[x-domain y-domain]`, each in the order a, b."
  [lay axes]
  (let [whole (lay (tc/dataset survey))
        cell (fn [g] {:where {:group g}})]
    [(domains (pj/facet whole :group))
     (vec (reverse (domains (assoc whole :poses [(cell "b") (cell "a")]))))
     (domains (pj/arrange [(lay (rows-of "a")) (lay (rows-of "b"))]
                          {:share-scales axes}))]))

(deftest a-shared-axis-covers-what-is-drawn-test
  (testing "stacked bars: the top of the tallest stack"
    (let [[facet split arranged]
          (three-ways #(pj/lay-bar % :item :n {:color :half :position :stack}) #{:x :y})]
      (is (= [0.0 17.85] (second (first facet))))
      (is (= facet split arranged))))
  (testing "error bars: the ends of the whiskers"
    (let [[facet split arranged]
          (three-ways #(pj/lay-errorbar % :item :n {:y-min :low :y-max :high}) #{:x :y})]
      (is (<= (first (second (first facet))) 0.5))
      (is (>= (second (second (first facet))) 14.0))
      (is (= facet split arranged))))
  (testing "a smooth alone: the lines, not the column"
    (let [data (tc/dataset {:group (vec (concat (repeat 6 "a") (repeat 6 "b")))
                            :x [1 2 3 4 5 6 1 2 3 4 5 6]
                            :y [1.0 9.0 2.0 8.0 3.0 7.0 11.0 19.0 12.0 18.0 13.0 17.0]})
          lay #(pj/lay-smooth % :x :y)
          part (fn [g] (tc/select-rows data #(= g (:group %))))
          facet (domains (pj/facet (lay data) :group))
          arranged (domains (pj/arrange [(lay (part "a")) (lay (part "b"))]
                                        {:share-scales #{:x :y}}))]
      (is (= facet arranged))
      (is (< 1.0 (first (second (first arranged)))))
      (is (> 19.0 (second (second (first arranged))))))))

(deftest a-count-axis-shares-by-the-column-counted-test
  (let [data (tc/dataset {:group (vec (concat (repeat 3 "a") (repeat 9 "b")))
                          :v [1.0 1.1 5.0 1.0 1.1 1.2 1.3 1.4 1.5 1.6 5.0 5.1]
                          :w [1.0 1.0 1.0 1.0 1.0 1.0 2.0 2.0 2.0 2.0 2.0 5.0]})
        part (fn [g] (tc/select-rows data #(= g (:group %))))
        count-axes (fn [pose] (mapv second (domains pose)))]
    (testing "histograms of one column, split: one count axis, whatever the order of the cells"
      (let [whole (pj/lay-histogram data :v)
            facet (count-axes (pj/facet whole :group))
            split (count-axes (assoc whole :poses [{:where {:group "b"}} {:where {:group "a"}}]))]
        (is (apply = facet))
        (is (= facet split))))
    (testing "arranged with :y shared"
      (is (apply = (count-axes (pj/arrange [(pj/lay-histogram (part "a") :v)
                                            (pj/lay-histogram (part "b") :v)]
                                           {:share-scales #{:y}})))))
    (testing "histograms of two columns keep a count axis each"
      (let [over (-> data (pj/pose {:x :v}) pj/lay-histogram (pj/facet {:x [:v :w]}))]
        (is (apply not= (count-axes over)))
        (testing "until :all or a group of the columns shares them"
          (is (apply = (count-axes (pj/options over {:share-scales {:y :all}}))))
          (is (apply = (count-axes (pj/options over {:share-scales {:y [[:v :w]]}})))))))))

(deftest a-shared-categorical-axis-test
  (let [lay #(pj/lay-bar % :item :n)]
    (testing "every category of every sharing cell, in every cell"
      (let [[facet split arranged] (three-ways lay #{:x})]
        (is (= [["p" "q" "r"] ["p" "q" "r"]] (mapv first facet)))
        (is (= (mapv first facet) (mapv first split) (mapv first arranged)))))
    (testing "in the order of the data where the cells read one dataset"
      ;; The rows hold r before p; the first panel holds only p and q.
      (let [data (tc/dataset {:group ["b" "a" "a" "b"]
                              :item  ["r" "p" "q" "q"]
                              :n     [1.0 2.0 3.0 4.0]})
            whole (pj/lay-bar data :item :n)
            unsplit (ffirst (domains whole))]
        (is (= ["r" "p" "q"] unsplit))
        (is (= [unsplit unsplit] (mapv first (domains (pj/facet whole :group)))))
        (is (= [unsplit unsplit]
               (mapv first (domains (assoc whole :poses [{:where {:group "a"}}
                                                         {:where {:group "b"}}])))))))
    (testing "cell by cell where the cells have data of their own"
      (is (= [["q" "r" "p"] ["q" "r" "p"]]
             (mapv first (domains (pj/arrange [(lay (rows-of "b")) (lay (rows-of "a"))]
                                              {:share-scales #{:x}}))))))
    (testing "not shared where nobody asked"
      (is (= [["p" "q"] ["q" "r"]]
             (mapv first (domains (pj/arrange [(lay (rows-of "a")) (lay (rows-of "b"))]))))))))

(deftest a-marginal-follows-its-panel-test
  ;; The panel's layers reach past the column -- a rule at 60 -- and the
  ;; marginal on the right, which names the column on its own :x under a
  ;; flip, covers the same extent.
  (let [data (tc/dataset {:a [1.0 2.0 3.0 4.0] :b [10.0 20.0 30.0 40.0]})
        plan (pj/plan (-> data
                          (pj/lay-point :a :b)
                          (pj/lay-rule-h {:y-intercept 60})
                          (pj/marginal :right)))
        [panel marginal] (map (comp first :panels :plan) (:sub-plots plan))]
    (is (<= 60 (second (:y-domain panel))))
    (is (= (:y-domain panel) (:y-domain marginal)))))

(deftest share-scales-names-the-axis-as-drawn-test
  ;; Flipped, :v runs across and :u runs up. `#{:x}` shares what runs
  ;; across, in a facet and in an arrangement alike.
  (let [data (tc/dataset {:g ["a" "a" "a" "b" "b" "b"]
                          :u [1.0 2.0 3.0 11.0 12.0 13.0]
                          :v [10.0 20.0 30.0 400.0 500.0 600.0]})
        flipped #(-> % (pj/lay-point :u :v) (pj/coord :flip))
        part (fn [g] (tc/select-rows data #(= g (:g %))))
        facet (domains (-> (flipped data) (pj/facet :g) (pj/options {:share-scales #{:x}})))
        arranged (domains (pj/arrange [(flipped (part "a")) (flipped (part "b"))]
                                      {:share-scales #{:x}}))]
    (is (apply = (map first facet)) "the horizontal axis is shared")
    (is (apply not= (map second facet)) "the vertical one is each panel's own")
    (is (= facet arranged))))

;; What an axis measures: the values of a column, or what a stat
;; computes from one -- a count, a density. Axes share only where they
;; measure alike.

(deftest axes-share-only-where-they-measure-alike-test
  (let [data (tc/dataset {:v [1.0 1.1 1.2 1.3 2.0 2.1 2.2 3.0 3.1 5.0 5.1 9.0]
                          :w [1.0 1.0 1.0 1.0 2.0 2.0 2.0 2.0 2.0 5.0 5.0 5.0]})
        ys (fn [cells share]
             (let [out (atom nil)]
               (with-out-str (reset! out (mapv second (domains (pj/arrange cells {:share-scales share})))))
               (deref out)))
        counts #(pj/lay-histogram data %)
        normalised #(pj/lay-histogram data % {:normalize :density})
        density #(pj/lay-density data %)
        points (pj/lay-point data :w :v)]
    (testing "a count and a density of one column are apart"
      (is (apply not= (ys [(counts :v) (density :v)] #{:y}))))
    (testing "a normalised histogram is a density"
      (is (apply = (ys [(normalised :v) (density :v)] #{:y})))
      (is (apply not= (ys [(counts :v) (normalised :v)] #{:y}))))
    (testing ":all shares counts with counts, and nothing else with them"
      (let [[a b c d] (ys [(counts :v) (counts :w) (density :v) points] {:y :all})]
        (is (= a b) "the two counts")
        (is (not= a c) "not the density")
        (is (not= a d) "not the column's values")
        (is (not= c d))))
    (testing "a group shares the counts of its columns, and their densities, each with its like"
      (let [[a b c d] (ys [(counts :v) (counts :w) (density :v) (density :w)] {:y [[:v :w]]})]
        (is (= a b))
        (is (= c d))
        (is (not= a c))))))

;; The share of the rows at or below each value: 0 to 1 on :y, computed
;; from one column. An extension's stat, declared as the built-in ones
;; are.
(defmethod stat/compute-stat ::running-share [{:keys [data x]}]
  (let [xs (vec (sort (data x)))
        n (count xs)
        ys (mapv #(/ (double (inc %)) n) (range n))]
    {:points [{:xs xs :ys ys}]
     :x-domain [(first xs) (peek xs)]
     :y-domain [0.0 1.0]}))

(defmethod stat/compute-stat [::running-share :computes] [_] {:y :running-share})

(deftest a-stat-declares-what-it-computes-test
  (testing "the built-in declarations"
    (is (= {:y :count} (stat/computes {:stat :bin})))
    (is (= {:y :density} (stat/computes {:stat :bin :normalize :density})))
    (is (= {:y :count} (stat/computes {:stat :count})))
    (is (= {:y :density} (stat/computes {:stat :density})))
    (is (nil? (stat/computes {:stat :identity})))
    (is (nil? (stat/computes {}))))
  (testing "an extension's stat is read as the built-in ones are"
    (layer-type/register! ::running-share
                          {:mark :line :stat ::running-share :column-counts #{1}})
    (let [data (tc/dataset {:v [1.0 2.0 3.0 4.0] :w [10.0 20.0 30.0 40.0]})
          share #(pj/lay data ::running-share {:x %})
          ys (fn [cells how] (mapv second (domains (pj/arrange cells {:share-scales how}))))]
      (testing "it needs no :y column, and draws"
        (let [[lo hi] (second (first (domains (share :v))))]
          (is (< lo 0.25 1.0 hi))))
      (testing "its axis is not the axis of the column's values"
        (is (apply not= (ys [(share :v) (pj/lay-point data :w :v)] {:y :all}))))
      (testing "nor a count's"
        (is (apply not= (ys [(share :v) (pj/lay-histogram data :v)] {:y :all}))))
      (testing "and two of them share"
        (is (apply = (ys [(share :v) (share :w)] {:y :all}))))
      (testing "one panel holding it beside a count says the axis reads in two units"
        (is (re-find #"draws a count, .* draws a running-share, so the axis reads in two units"
                     (with-out-str (pj/plan (-> data
                                                (pj/lay-histogram :v)
                                                (pj/lay ::running-share {:x :v}))))))))))

;; A series faceted by its own key column is a facet over the series'
;; columns, and is drawn as one.

(deftest a-series-faceted-by-its-key-is-a-facet-over-its-columns-test
  (let [texts (fn [pose] (vec (remove #(re-matches #"-?[\d.,]+" %) (:texts (pj/svg-summary pose)))))
        same? (fn [a b] (and (= (domains a) (domains b)) (= (texts a) (texts b))))
        over-y (fn [] (-> cars (pj/pose {:x :size}) pj/lay-point))]
    (testing "the same panels, axes and titles"
      (is (same? (-> cars (pj/lay-point :size [:fast :slow]) (pj/facet :series :row))
                 (-> (over-y) (pj/facet {:y [:fast :slow]}))))
      (is (same? (-> cars (pj/lay-point :size [:fast :slow]) (pj/facet :series))
                 (-> (over-y) (pj/facet {:col {:y [:fast :slow]}})))))
    (testing "a series on the pose, and a key named with :as"
      (is (same? (-> cars (pj/pose {:x :size :y {:series [:fast :slow] :as :speed}})
                     pj/lay-point (pj/facet :speed :row))
                 (-> (over-y) (pj/facet {:y [:fast :slow]})))))
    (testing "an axis per column by default, one where a group shares it"
      (let [by-key (-> cars (pj/lay-point :size [:fast :slow]) (pj/facet :series))]
        (is (= [1 2] (distinct-counts by-key)))
        (is (= [1 1] (distinct-counts (pj/options by-key {:share-scales {:y [[:fast :slow]]}}))))))
    (testing "histograms of a series: a count axis per column"
      (is (same? (-> cars (pj/lay-histogram {:x [:fast :slow]}) (pj/facet :series))
                 (-> cars (pj/pose {:x :fast}) pj/lay-histogram (pj/facet {:col {:x [:fast :slow]}})))))
    (testing "beside a facet by another column, a grid"
      (is (same? (-> cars (pj/lay-point :size [:fast :slow]) (pj/facet :series) (pj/facet :kind :row))
                 (-> (over-y) (pj/facet {:col {:y [:fast :slow]} :row :kind})))))
    (testing "a series faceted by another column keeps its panel of both columns"
      (let [pose (-> cars (pj/lay-point :size [:fast :slow]) (pj/facet :kind))]
        (is (nil? (:sub-plots (pj/plan pose))))
        (is (= [1 1] (distinct-counts pose)))
        (is (some #{"fast, slow"} (texts pose)))))
    (testing "a colour specified on the key keeps the series' own rows"
      (let [pose (-> cars (pj/lay-point :size [:fast :slow] {:color :series}) (pj/facet :series))]
        (is (nil? (:sub-plots (pj/plan pose))))
        (is (= 2 (count (:panels (pj/plan pose)))))))))

(deftest a-domain-on-one-cell-is-the-shared-axis-domain-test
  (let [cell (fn [y] (pj/lay-point cars {:x :size :y y}))
        dom (fn [col d] {:column col :scale {:domain d}})
        ys (fn [pose] (mapv #(mapv double (second %)) (domains pose)))
        share (fn [cells] (pj/arrange cells {:share-scales {:y :all}}))]
    (testing "on either cell, in the layer, on the pose or by pj/scale on the cell"
      (is (= [[0.0 100.0] [0.0 100.0]]
             (ys (share [(cell (dom :fast [0 100])) (cell :slow)]))
             (ys (share [(cell :fast) (cell (dom :slow [0 100]))]))
             (ys (share [(-> cars (pj/pose {:x :size :y (dom :fast [0 100])}) pj/lay-point)
                         (cell :slow)]))
             (ys (share [(pj/scale (cell :fast) :y {:domain [0 100]}) (cell :slow)])))))
    (testing "three cells, and a reversed domain"
      (is (= (repeat 3 [0.0 50.0])
             (ys (share [(cell :fast) (cell (dom :slow [0 50])) (cell :weight)]))))
      (is (= [[100.0 0.0] [100.0 0.0]]
             (ys (share [(cell (dom :fast [100 0])) (cell :slow)])))))
    (testing "a group of columns, and an axis not shared"
      (is (= [[0.0 100.0] [0.0 100.0]]
             (ys (pj/arrange [(cell (dom :fast [0 100])) (cell :slow)]
                             {:share-scales {:y [[:fast :slow]]}}))))
      (let [[a b] (ys (pj/arrange [(cell (dom :fast [0 100])) (cell :slow)]
                                  {:share-scales {:x :all}}))]
        (is (= [0.0 100.0] a))
        (is (not= a b))))
    (testing "the same domain twice agrees; two domains report an error naming both"
      (is (= [[0.0 100.0] [0.0 100.0]]
             (ys (share [(cell (dom :fast [0 100])) (cell (dom :slow [0 100]))]))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"sharing :y specify different domains.*\[0 100\].*:fast.*\[0 50\].*:slow"
                            (pj/plan (share [(cell (dom :fast [0 100])) (cell (dom :slow [0 50]))]))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"specify different domains"
                            (pj/plan (-> (share [(cell :fast) (cell (dom :slow [0 50]))])
                                         (pj/scale :y {:domain [0 100]}))))))
    (testing "a :domain on one cell beside an :include on another reports an error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"both a :domain and an :include"
                            (pj/plan (share [(cell (dom :fast [0 100]))
                                             (cell {:column :slow :scale {:include [200]}})])))))
    (testing "an :include alone is pooled and padded, as before"
      (let [[a b] (ys (share [(cell {:column :fast :scale {:include [0 100]}}) (cell :slow)]))]
        (is (= a b))
        (is (= [-5.0 105.0] a))))
    (testing "a categorical domain on the second cell orders both"
      (let [bars (fn [x] (pj/lay-bar cars {:x x}))
            xs (mapv (comp vec first)
                     (domains (pj/arrange [(bars :kind)
                                           (bars {:column :kind :scale {:domain ["b" "a"]}})]
                                          {:share-scales {:x :all}})))]
        (is (= [["b" "a"] ["b" "a"]] xs))))))

(deftest cells-sharing-an-axis-agree-after-the-text-fit-test
  ;; Each cell is widened for its own labels; the cells sharing the
  ;; axis are then planned again until they are on one domain.
  (let [data (tc/dataset {:g ["a" "a" "b" "b"]
                          :u [1.0 2.0 1.0 2.0]
                          :v [10.0 20.0 30.0 40.0]
                          :t ["s" "a very long label, longer than the others" "x" "y"]})
        part (fn [g] (-> (tc/select-rows data #(= g (:g %)))
                         (pj/lay-text :u :v {:text :t})))
        alone (ffirst (domains (part "a")))
        shared (domains (pj/arrange [(part "a") (part "b")] {:share-scales #{:x :y}}))]
    (is (apply = shared))
    (is (< 2.0 (second (ffirst shared))) "and wide enough for the long label")
    (is (not= alone (ffirst (domains (part "b")))) "the cells alone differ")
    (testing "flipped cells too"
      (let [flipped (domains (pj/arrange [(pj/coord (part "a") :flip) (pj/coord (part "b") :flip)]
                                         {:share-scales #{:x :y}}))]
        (is (apply = flipped))))))

(deftest a-shared-count-axis-is-ticked-once-test
  ;; The histograms of a split share their count axis, so only the
  ;; first cell of a row draws its ticks, as a facet's panels do.
  (let [data (tc/dataset {:g (vec (concat (repeat 4 "a") (repeat 8 "b")))
                          :v [1.0 1.1 1.2 5.0 1.0 1.1 1.2 1.3 1.4 1.5 5.0 5.1]})
        whole (pj/lay-histogram data :v)
        split (assoc whole :poses [{:where {:g "b"}} {:where {:g "a"}}])
        y-ticks (fn [pose]
                  (mapv (comp count :values :y-ticks first :panels :plan)
                        (:sub-plots (pj/plan pose))))]
    (is (pos? (first (y-ticks split))))
    (is (zero? (second (y-ticks split))))))
