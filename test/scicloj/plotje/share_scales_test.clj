(ns scicloj.plotje.share-scales-test
  "`:share-scales` on a facet and on an arrangement: the set of axes,
   the map from an axis to which columns go together, and the retired
   `:scales` option it replaces."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]))

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
