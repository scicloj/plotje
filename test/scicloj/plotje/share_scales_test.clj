(ns scicloj.plotje.share-scales-test
  "`:share-scales` on a facet and on an arrangement: the set of axes,
   the map from an axis to which columns go together, and the retired
   `:scales` option it replaces."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]
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
