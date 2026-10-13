(ns scicloj.plotje.impl.pose-test
  "Unit tests for impl.pose. The substrate is not wired into the
   public API yet (Phase 3 does that); these tests exercise the tree
   operations in isolation."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.impl.pose :as pose]))

;; ============================================================
;; Structural predicates
;; ============================================================

(deftest predicates-test
  (testing "pose? requires :layers or :poses"
    (is (pose/pose? {:layers []}))
    (is (pose/pose? {:layers [{:layer-type :point}]}))
    (is (pose/pose? {:poses []}))
    (is (pose/pose? {:poses [{:layers []}]}))
    (is (not (pose/pose? {})))
    (is (not (pose/pose? {:data {:x [1]}})))
    (is (not (pose/pose? "not a map"))))

  (testing "leaf? = no non-empty :poses"
    (is (pose/leaf? {:layers [{:layer-type :point}]}))
    (is (pose/leaf? {:poses []})
        "An empty :poses vector is effectively a leaf")
    (is (not (pose/leaf? {:poses [{:layers []}]}))))

  (testing "composite? is the inverse of leaf?"
    (is (pose/composite? {:poses [{:layers []}]}))
    (is (not (pose/composite? {:layers [{:layer-type :point}]})))))

;; ============================================================
;; resolve-tree
;; ============================================================

(deftest resolve-tree-leaf-test
  (testing "a single leaf resolves to itself with :path []"
    (let [leaf {:layers [{:layer-type :point}]}
          [r] (pose/resolve-tree leaf)]
      (is (= [] (:path r)))
      (is (= [{:layer-type :point}] (:layers r)))
      (is (= {} (:mapping r)))
      (is (nil? (:data r)))
      (is (= {} (:opts r))))))

(deftest resolve-tree-context-inheritance-test
  (testing ":data, :mapping, :layers, :opts inherit top-down"
    (let [ds {:x [1 2 3]}
          tree {:data    ds
                :mapping {:color :species}
                :opts    {:title "outer"}
                :layers  [{:layer-type :lay-a}]
                :poses  [{:layers [{:layer-type :lay-b}]}
                         {:data    {:x [10 20]}
                          :mapping {:color :override
                                    :size  :other}
                          :opts    {:title "inner"}
                          :layers  [{:layer-type :lay-c}]}]}
          leaves (pose/resolve-tree tree)]
      (is (= 2 (count leaves)))

      (testing "first leaf inherits everything"
        (let [l1 (first leaves)]
          (is (= [0] (:path l1)))
          (is (= ds (:data l1)))
          (is (= {:color :species} (:mapping l1)))
          (is (= {:title "outer"} (:opts l1)))
          (is (= [{:layer-type :lay-a} {:layer-type :lay-b}] (:layers l1)))))

      (testing "second leaf overrides data/mapping/opts and accumulates layers"
        (let [l2 (second leaves)]
          (is (= [1] (:path l2)))
          (is (= {:x [10 20]} (:data l2)))
          (is (= {:color :override :size :other} (:mapping l2)))
          (is (= {:title "inner"} (:opts l2)))
          (is (= [{:layer-type :lay-a} {:layer-type :lay-c}] (:layers l2))))))))

(deftest resolve-tree-preserves-extras-test
  (testing "non-structural keys on a leaf pass through to the resolved leaf"
    (let [tree {:poses [{:layers [{:layer-type :point}]
                         :panel-label "row=a, col=b"}]}
          [l] (pose/resolve-tree tree)]
      (is (= "row=a, col=b" (:panel-label l))))))

(deftest resolve-tree-deep-nesting-test
  (testing "paths accumulate through arbitrary depth"
    (let [tree {:poses [{:poses [{:poses [{:layers [{:layer-type :point}]}]}]}]}
          [l] (pose/resolve-tree tree)]
      (is (= [0 0 0] (:path l))))))

;; ============================================================
;; compute-layout
;; ============================================================

(deftest compute-layout-leaf-test
  (testing "a leaf gets the bounding rectangle verbatim"
    (let [leaf {:layers []}]
      (is (= {[] [0.0 0.0 100.0 50.0]}
             (pose/compute-layout leaf [0 0 100 50]))))))

(deftest compute-layout-horizontal-equal-weights-test
  (testing "two children, horizontal, default equal weights"
    (let [tree {:poses [{:layers []} {:layers []}]}
          layout (pose/compute-layout tree [0 0 100 50])]
      (is (= [0.0 0.0 50.0 50.0] (layout [0])))
      (is (= [50.0 0.0 50.0 50.0] (layout [1]))))))

(deftest compute-layout-vertical-weighted-test
  (testing "two children, vertical, 3:1 weights"
    (let [tree {:layout {:direction :vertical :weights [3 1]}
                :poses [{:layers []} {:layers []}]}
          layout (pose/compute-layout tree [0 0 100 400])]
      (is (= [0.0 0.0 100.0 300.0] (layout [0])))
      (is (= [0.0 300.0 100.0 100.0] (layout [1]))))))

(deftest compute-layout-rejects-nonpositive-weights-test
  (testing "weights summing to zero throw"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"must sum to a positive number"
                          (pose/compute-layout {:layout {:weights [0 0]}
                                                :poses [{:layers []} {:layers []}]}
                                               [0 0 100 100])))))

(deftest compute-layout-nested-test
  (testing "nested composite: outer vertical, inner row horizontal"
    (let [tree {:layout {:direction :vertical :weights [1 3]}
                :poses [{:layers []} ; 25% top
                        {:layout {:direction :horizontal :weights [3 1]}
                         :poses [{:layers []} {:layers []}]}]}
          layout (pose/compute-layout tree [0 0 400 400])]
      ;; top band: full width, top quarter
      (is (= [0.0 0.0 400.0 100.0] (layout [0])))
      ;; bottom-left: 75% width x 75% height offset
      (is (= [0.0 100.0 300.0 300.0] (layout [1 0])))
      ;; bottom-right
      (is (= [300.0 100.0 100.0 300.0] (layout [1 1]))))))

;; ============================================================
;; Tree utilities
;; ============================================================

(deftest last-leaf-path-test
  (testing "root leaf has empty path"
    (is (= [] (pose/last-leaf-path {:layers []}))))

  (testing "composites walk to the rightmost leaf"
    (is (= [1] (pose/last-leaf-path
                {:poses [{:layers []} {:layers []}]})))
    (is (= [1 0] (pose/last-leaf-path
                  {:poses [{:layers []}
                           {:poses [{:layers []}]}]}))))

  (testing "empty-poses vector counts as a leaf (path = [])"
    (is (= [] (pose/last-leaf-path {:poses []})))))

(deftest leaf-at-test
  (let [tree {:poses [{:layers [{:layer-type :point}]}
                      {:poses [{:layers [{:layer-type :line}]}]}]}]
    (testing "path lands on a leaf"
      (is (= :point (-> (pose/leaf-at tree [0]) :layers first :layer-type)))
      (is (= :line (-> (pose/leaf-at tree [1 0]) :layers first :layer-type))))

    (testing "path lands on a composite -> nil"
      (is (nil? (pose/leaf-at tree [1]))))))

(deftest path->update-in-path-test
  (testing "empty path translates to empty"
    (is (= [] (pose/path->update-in-path []))))
  (testing "single-level path interleaves :poses"
    (is (= [:poses 0] (pose/path->update-in-path [0]))))
  (testing "deep paths interleave :poses at every level"
    (is (= [:poses 2 :poses 1 :poses 0]
           (pose/path->update-in-path [2 1 0])))))

(deftest last-matching-leaf-path-test
  (testing "two leaves with identical :x/:y match -- picks the last (Rule 6)"
    (is (= [1]
           (pose/last-matching-leaf-path
            {:poses [{:layers [] :mapping {:x :a :y :b}}
                     {:layers [] :mapping {:x :a :y :b}}]}
            {:x :a :y :b}))))

  (testing "no matching leaf returns nil"
    (is (nil? (pose/last-matching-leaf-path
               {:poses [{:layers [] :mapping {:x :c :y :d}}]}
               {:x :a :y :b}))))

  (testing "strict matching -- :x and \"x\" are distinct column references"
    (is (nil? (pose/last-matching-leaf-path
               {:poses [{:layers [] :mapping {:x "a" :y "b"}}]}
               {:x :a :y :b})))
    (is (nil? (pose/last-matching-leaf-path
               {:poses [{:layers [] :mapping {:x :a :y :b}}]}
               {:x "a" :y "b"}))))

  (testing "ancestor :mapping inherits into a bare leaf (resolve-tree merge)"
    (is (= [0] (pose/last-matching-leaf-path
                {:mapping {:x :a :y :b}
                 :poses  [{:layers []}]}
                {:x :a :y :b}))))

  (testing "DFS order across different depths"
    (is (= [1 0] (pose/last-matching-leaf-path
                  {:poses [{:layers [] :mapping {:x :a :y :b}}
                           {:poses [{:layers [] :mapping {:x :a :y :b}}]}]}
                  {:x :a :y :b}))))

  (testing "nil position components match bare leaves (no :x/:y mapping)"
    (is (= [0] (pose/last-matching-leaf-path
                {:poses [{:layers []}]}
                {:x nil :y nil})))))

;; ============================================================
;; shared-axis-groups
;; ============================================================
;;
;; Which cells share an axis with which. No extent is read here: what a
;; group's axis covers is measured at the plan stage from what its
;; cells draw (`share_scales_test`).

(def iris-like
  (tc/dataset {:sepal-length [4.0 5.0 6.0 7.0 8.0]
               :sepal-width  [2.0 3.0 3.5 4.0 4.5]
               :species      ["a" "a" "b" "b" "c"]}))

(defn- group-of [groups path axis] (get-in groups [path axis :group]))

(defn- together?
  "True when the cells at `paths` are in one group on `axis`."
  [groups axis & paths]
  (let [ids (map #(group-of groups % axis) paths)]
    (and (every? some? ids) (apply = ids))))

(deftest shared-axis-groups-no-share-test
  (testing "composite with :share-scales #{} puts no cells together"
    (let [tree {:data iris-like
                :share-scales #{}
                :poses [{:layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}
                        {:layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}]}]
      (is (= {} (pose/shared-axis-groups tree))))))

(deftest shared-axis-groups-declared-split-test
  (testing "cells drawing parts of the composite's data share with nothing written"
    (let [tree {:data iris-like
                :poses [{:where {:species "a"}
                         :layers [{:layer-type :point :mapping {:x :sepal-length}}]}
                        {:where {:species "c"}
                         :layers [{:layer-type :point :mapping {:x :sepal-length}}]}]}
          groups (pose/shared-axis-groups (pose/elaborate tree))]
      (is (together? groups :x [0] [1]))
      (is (nil? (get-in groups [[0] :x :order]))
          "a numeric column has no order of categories")))
  (testing "a categorical column they share is listed in the order of the data"
    (let [tree {:data iris-like
                :poses [{:where {:sepal-length 8.0}
                         :layers [{:layer-type :point :mapping {:x :species :y :sepal-width}}]}
                        {:where {:sepal-length 4.0}
                         :layers [{:layer-type :point :mapping {:x :species :y :sepal-width}}]}]}
          groups (pose/shared-axis-groups (pose/elaborate tree))]
      (is (= ["a" "b" "c"] (get-in groups [[0] :x :order])))))
  (testing "cells bringing data of their own do not"
    (let [tree {:poses [{:data (tc/dataset {:sepal-length [1.0 2.0]})
                         :layers [{:layer-type :point :mapping {:x :sepal-length}}]}
                        {:data (tc/dataset {:sepal-length [10.0 20.0]})
                         :layers [{:layer-type :point :mapping {:x :sepal-length}}]}]}]
      (is (= {} (pose/shared-axis-groups tree))))))

(deftest shared-axis-groups-same-column-test
  (testing "leaves drawing one column are put together"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:sepal-length [1.0 2.0 3.0]})
                         :layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}
                        {:data (tc/dataset {:sepal-length [10.0 20.0]})
                         :layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}]}
          groups (pose/shared-axis-groups tree)]
      (is (together? groups :x [0] [1]))
      (is (nil? (get-in groups [[0] :x :order]))
          "cells with data of their own have no one dataset to take an order from"))))

(deftest shared-axis-groups-column-bucketed-test
  (testing "leaves drawing different columns on an axis are not put together"
    (let [tree {:data iris-like
                :share-scales #{:x}
                :poses [{:layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}
                        {:layers [{:layer-type :point
                                   :mapping {:x :sepal-width}}]}]}
          groups (atom nil)
          out (with-out-str (reset! groups (pose/shared-axis-groups tree)))]
      (is (= {} (deref groups)))
      (is (re-find #"no two cells share :x" out)))))

(deftest shared-axis-groups-nested-test
  (testing "SPLOM-like: columns of the grid share :x, rows share :y"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]
                          :c [100.0 200.0 300.0]})
          tree {:data ds
                :share-scales #{:x :y}
                :layout {:direction :vertical}
                :poses [{:layout {:direction :horizontal}
                         :poses [{:layers [{:layer-type :point :mapping {:x :a :y :a}}]}
                                 {:layers [{:layer-type :point :mapping {:x :b :y :a}}]}]}
                        {:layout {:direction :horizontal}
                         :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                                 {:layers [{:layer-type :point :mapping {:x :b :y :b}}]}]}]}
          groups (pose/shared-axis-groups tree)]
      (is (together? groups :x [0 0] [1 0]) "leaves with :x :a")
      (is (together? groups :x [0 1] [1 1]) "leaves with :x :b")
      (is (together? groups :y [0 0] [0 1]) "leaves with :y :a")
      (is (together? groups :y [1 0] [1 1]) "leaves with :y :b")
      (is (not (together? groups :x [0 0] [0 1])) ":a and :b are apart")
      (is (not (together? groups :y [0 0] [1 0]))))))

(deftest shared-axis-groups-count-axis-test
  (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                        :b [10.0 20.0 30.0]})]
    (testing "a count axis is not the axis of the column it counts"
      (let [tree {:data ds
                  :share-scales #{:y}
                  :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                          {:layers [{:layer-type :histogram :mapping {:x :b}}]
                           ;; The pose mapping names :b on :y; the layer
                           ;; still counts.
                           :mapping {:y :b}}]}]
        (is (= {} (pose/shared-axis-groups tree)))))
    (testing "an explicit :stat :density likewise"
      (let [tree {:data ds
                  :share-scales #{:y}
                  :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                          {:mapping {:y :b}
                           :layers [{:layer-type :line
                                     :stat :density
                                     :mapping {:x :a}}]}]}]
        (is (= {} (pose/shared-axis-groups tree)))))
    (testing "an empty-layers diagonal cell, elaborated to a histogram, likewise"
      (let [tree {:data ds
                  :share-scales #{:y}
                  :poses [{:mapping {:x :a :y :a} :layers []}
                          {:mapping {:x :b :y :a}
                           :layers [{:layer-type :point}]}]}
            elaborated (pose/elaborate tree)]
        (is (= [:histogram] (mapv :layer-type (:layers (first (pose/resolve-tree elaborated))))))
        (is (= {} (pose/shared-axis-groups elaborated)))))
    (testing "counts of one column share"
      (let [tree {:share-scales #{:y}
                  :poses [{:data ds :layers [{:layer-type :histogram :mapping {:x :a}}]}
                          {:data ds :layers [{:layer-type :histogram :mapping {:x :a}}]}]}]
        (is (together? (pose/shared-axis-groups tree) :y [0] [1]))))
    (testing "counts of two columns share where :all or a group says so"
      (let [tree (fn [how]
                   {:data ds
                    :share-scales {:y how}
                    :poses [{:layers [{:layer-type :histogram :mapping {:x :a}}]}
                            {:layers [{:layer-type :histogram :mapping {:x :b}}]}]})
            groups (fn [how] (let [g (atom nil)]
                               (with-out-str (reset! g (pose/shared-axis-groups (tree how))))
                               (deref g)))]
        (is (= {} (groups :same-column)))
        (is (together? (groups :all) :y [0] [1]))
        (is (together? (groups [[:a :b]]) :y [0] [1]))))
    (testing ":bin2d is a data axis on :y -- the count goes to the fill"
      (let [tree {:data ds
                  :share-scales #{:y}
                  :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                          {:layers [{:layer-type :tile
                                     :stat :bin2d
                                     :mapping {:x :a :y :b}}]}]}]
        (is (together? (pose/shared-axis-groups tree) :y [0] [1]))))))

(deftest shared-axis-groups-name-the-axis-as-drawn-test
  (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                        :b [10.0 20.0 30.0]})]
    (testing "a flipped cell naming the panel's :y column on :x shares the vertical axis with it"
      (let [tree {:data ds
                  :share-scales #{:y}
                  :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                          {:opts {:coord :flip}
                           :layers [{:layer-type :histogram :mapping {:x :b}}]}]}
            groups (pose/shared-axis-groups tree)]
        (is (some? (group-of groups [0] :y)))
        (is (= (group-of groups [0] :y) (group-of groups [1] :x))
            "the group is on each leaf's own axis: the panel's :y, the flipped cell's :x")))
    (testing "two flipped cells share :x on the axis that runs across, their :y"
      (let [tree {:data ds
                  :share-scales #{:x}
                  :poses [{:opts {:coord :flip}
                           :layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                          {:opts {:coord :flip}
                           :layers [{:layer-type :point :mapping {:x :a :y :b}}]}]}
            groups (pose/shared-axis-groups tree)]
        (is (together? groups :y [0] [1]))
        (is (nil? (group-of groups [0] :x)))))
    (testing "a flipped cell and an upright one drawing :a the same way do not share the other way"
      (let [tree {:data ds
                  :share-scales #{:x}
                  :poses [{:opts {:coord :flip}
                           :layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                          {:layers [{:layer-type :point :mapping {:x :a :y :b}}]}]}
            groups (atom nil)]
        ;; Across the page one draws :b and the other :a.
        (with-out-str (reset! groups (pose/shared-axis-groups tree)))
        (is (= {} (deref groups)))))))

(deftest shared-axis-groups-rejects-mixed-type-test
  (testing "share-scales refuses mixing numerical and categorical for the same column ref"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x ["a" "b" "c"] :y [40.0 50.0 60.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"cannot share :x between these cells.*differ in column type"
           (pose/shared-axis-groups tree))))))

(deftest shared-axis-groups-rejects-mixed-scale-type-test
  (testing "share-scales refuses mixing :linear and :log on the same column"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :mapping {:x {:from :x :scale {:type :log}}}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"cannot share :x between these cells.*differ in scale type"
           (pose/shared-axis-groups tree))))))

(deftest shared-axis-groups-warns-on-free-conflict-test
  (testing "share-scales :x warns when a sub-pose writes a :share-scales leaving :x out"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :opts {:share-scales #{}}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}
          out (with-out-str (pose/shared-axis-groups tree))]
      (is (re-find #"shares :x, and a pose inside it writes a :share-scales that leaves :x out \(#\{\}\)" out))))

  (testing "share-scales :y does not warn when the sub-pose shares :y too"
    (let [tree {:share-scales #{:y}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :opts {:share-scales #{:y}}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}
          out (with-out-str (pose/shared-axis-groups tree))]
      (is (= "" out)))))

(deftest shared-axis-groups-categorical-and-temporal-test
  (testing "cells drawing a categorical column share it"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x ["a" "b"] :y [1.0 2.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x ["c" "d"] :y [3.0 4.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (together? (pose/shared-axis-groups tree) :x [0] [1]))))
  (testing "and cells drawing a temporal one"
    (let [d (fn [s] (java.time.LocalDate/parse s))
          tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [(d "2020-01-01") (d "2020-06-01")]
                                            :y [1.0 2.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [(d "2020-03-01") (d "2020-12-01")]
                                            :y [3.0 4.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (together? (pose/shared-axis-groups tree) :x [0] [1])))))

(deftest shared-axis-groups-reads-ancestor-context-test
  (testing "a layer on the composite counts for each leaf it renders on"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]})
          ;; The shape of a grid of pairs given a histogram afterwards:
          ;; the layer lives on the composite and the cells carry only
          ;; their mapping. Each cell's y axis is a count -- of :a in
          ;; one and of :b in the other -- whatever :y the cells name.
          tree {:data ds
                :share-scales #{:y}
                :layers [{:layer-type :histogram}]
                :poses [{:mapping {:x :a :y :a} :layers []}
                        {:mapping {:x :b :y :a} :layers []}]}]
      (is (= {} (pose/shared-axis-groups tree)))))
  (testing "a scale set on the composite keeps the leaf's column in its bucket"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]})
          ;; The parent names the column and the leaf adds only a
          ;; scale: a plain merge replaced the column with the scale,
          ;; so the leaf found no bucket and went unshared.
          tree {:data ds
                :share-scales #{:x}
                :mapping {:x :a}
                :poses [{:mapping {:y :b} :layers [{:layer-type :point}]}
                        {:mapping {:x {:scale {:type :linear}} :y :b}
                         :layers [{:layer-type :point}]}]}]
      (is (together? (pose/shared-axis-groups tree) :x [0] [1])))))
