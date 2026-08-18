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
      ;; bottom-left: 75% width × 75% height offset
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
;; inject-shared-scales
;; ============================================================
;;
;; The high-value primitive. Three scenarios cover the behavior that
;; the nested-poses PoC validated:
;; - shared axis with the same column across leaves -> union domain
;; - shared axis with DIFFERENT columns across leaves -> independent buckets
;; - composite without :share-scales -> no injection

(def iris-like
  (tc/dataset {:sepal-length [4.0 5.0 6.0 7.0 8.0]
               :sepal-width  [2.0 3.0 3.5 4.0 4.5]
               :species      ["a" "a" "b" "b" "c"]}))

(deftest inject-shared-scales-no-share-test
  (testing "composite without :share-scales leaves leaves untouched"
    (let [tree {:data iris-like
                :poses [{:layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}
                        {:layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}]}
          injected (pose/inject-shared-scales tree)
          leaves (pose/resolve-tree injected)]
      (is (every? #(nil? (:x-scale-domain (:opts %))) leaves)))))

(deftest inject-shared-scales-same-column-test
  (testing "leaves sharing a column get a union x domain"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:sepal-length [1.0 2.0 3.0]})
                         :layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}
                        {:data (tc/dataset {:sepal-length [10.0 20.0]})
                         :layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}]}
          leaves (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (= 2 (count leaves)))
      (is (= [1.0 20.0] (:x-scale-domain (:opts (first leaves)))))
      (is (= [1.0 20.0] (:x-scale-domain (:opts (second leaves))))))))

(deftest inject-shared-scales-column-bucketed-test
  (testing "leaves using different columns for the same axis get different buckets"
    (let [tree {:data iris-like
                :share-scales #{:x}
                :poses [{:layers [{:layer-type :point
                                   :mapping {:x :sepal-length}}]}
                        {:layers [{:layer-type :point
                                   :mapping {:x :sepal-width}}]}]}
          [l1 l2] (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (= [4.0 8.0] (:x-scale-domain (:opts l1)))
          "first leaf gets the sepal-length domain")
      (is (= [2.0 4.5] (:x-scale-domain (:opts l2)))
          "second leaf gets the sepal-width domain (not unioned with sepal-length)"))))

(deftest inject-shared-scales-nested-test
  (testing "SPLOM-like: outer :x :y share, inner rows and columns each get their own buckets"
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
          leaves (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (= 4 (count leaves)))
      (is (every? #(= [1.0 3.0] (:x-scale-domain (:opts %)))
                  (filter #(= :a (get-in % [:layers 0 :mapping :x])) leaves))
          "leaves with :x :a share the :a domain")
      (is (every? #(= [10.0 30.0] (:x-scale-domain (:opts %)))
                  (filter #(= :b (get-in % [:layers 0 :mapping :x])) leaves))
          "leaves with :x :b share the :b domain")
      (is (every? #(= [1.0 3.0] (:y-scale-domain (:opts %)))
                  (filter #(= :a (get-in % [:layers 0 :mapping :y])) leaves))
          "leaves with :y :a share the :a domain")
      (is (every? #(= [10.0 30.0] (:y-scale-domain (:opts %)))
                  (filter #(= :b (get-in % [:layers 0 :mapping :y])) leaves))
          "leaves with :y :b share the :b domain"))))

(deftest inject-shared-scales-stat-driven-y-test
  (testing "leaves whose y axis is stat-driven (count/density) skip the y-domain stamp"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]})
          ;; SPLOM where one leaf is an explicit histogram on the y=:b
          ;; column. The other y=:b leaf is a scatter -- it should
          ;; still get the shared :b domain. The histogram leaf
          ;; should NOT, since its rendered y-axis is a count axis.
          tree {:data ds
                :share-scales #{:y}
                :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                        {:layers [{:layer-type :histogram :mapping {:x :b}}]
                          ;; Force the histogram leaf into the :y :b
                          ;; bucket via its pose mapping; the layer
                          ;; itself still stat-bins on x.
                         :mapping {:y :b}}]}
          [scatter hist] (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (= [10.0 30.0] (:y-scale-domain (:opts scatter)))
          "scatter leaf still gets the shared y-domain")
      (is (nil? (:y-scale-domain (:opts hist)))
          "histogram leaf does not get the shared y-domain (its y-axis is count, not data)"))))

(deftest inject-shared-scales-empty-layers-diagonal-test
  (testing "an empty-layers leaf whose diagonal mapping infers to :bin gets exempted"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]})
          ;; This mirrors the SPLOM diagonal cell shape produced by
          ;; (pj/cross cols cols): the leaf has :x = :y mapping and
          ;; empty layers. Per-cell inference picks {:mark :bar :stat :bin}.
          tree {:data ds
                :share-scales #{:y}
                :poses [{:mapping {:x :a :y :a} :layers []}      ;; diagonal: will infer :bin
                        {:mapping {:x :b :y :a}                  ;; off-diagonal: will infer scatter
                         :layers [{:layer-type :point}]}]}
          [diag off] (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (nil? (:y-scale-domain (:opts diag)))
          "diagonal cell (inferred to :bin) skips the y-stamp")
      (is (= [1.0 3.0] (:y-scale-domain (:opts off)))
          "off-diagonal scatter still gets the shared y-domain"))))

(deftest inject-shared-scales-bin2d-not-exempted-test
  (testing ":bin2d (heatmap) is NOT exempted -- its y is a data axis, count goes to fill"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]})
          tree {:data ds
                :share-scales #{:y}
                :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                        {:layers [{:layer-type :tile
                                   :stat :bin2d
                                   :mapping {:x :a :y :b}}]}]}
          [scatter heatmap] (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (= [10.0 30.0] (:y-scale-domain (:opts scatter))))
      (is (= [10.0 30.0] (:y-scale-domain (:opts heatmap)))
          "heatmap participates in shared y-domain because its y is data, not count"))))

(deftest inject-shared-scales-rejects-coord-conflict-test
  (testing "share-scales :x refuses when one cell uses :coord :flip and another does not"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :opts {:coord :flip}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"incompatible scale meaning"
           (pose/inject-shared-scales tree))))))

(deftest inject-shared-scales-rejects-mixed-type-test
  (testing "share-scales refuses mixing numerical and categorical for the same column ref"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x ["a" "b" "c"] :y [40.0 50.0 60.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"incompatible scale meaning"
           (pose/inject-shared-scales tree))))))

(deftest inject-shared-scales-rejects-mixed-scale-type-test
  (testing "share-scales refuses mixing :linear and :log on the same column"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :mapping {:x {:from :x :scale {:type :log}}}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"incompatible scale meaning"
           (pose/inject-shared-scales tree))))))

(deftest inject-shared-scales-warns-on-free-conflict-test
  (testing "share-scales :x warns when a sub-pose carries :scales :free"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :opts {:scales :free}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}
          out (with-out-str (pose/inject-shared-scales tree))]
      (is (re-find #":share-scales :x overrides a sub-pose's :scales :free" out))))

  (testing "share-scales :y does not warn when sub-pose has :scales :free-x"
    (let [tree {:share-scales #{:y}
                :poses [{:data (tc/dataset {:x [1.0 2.0 3.0] :y [10.0 20.0 30.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x [4.0 5.0 6.0] :y [40.0 50.0 60.0]})
                         :opts {:scales :free-x}
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}
          out (with-out-str (pose/inject-shared-scales tree))]
      (is (= "" out)))))

(deftest inject-shared-scales-rejects-all-categorical-bucket-test
  (testing "share-scales refuses when no cell in the bucket has numeric values"
    (let [tree {:share-scales #{:x}
                :poses [{:data (tc/dataset {:x ["a" "b"] :y [1.0 2.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}
                        {:data (tc/dataset {:x ["c" "d"] :y [3.0 4.0]})
                         :layers [{:layer-type :point :mapping {:x :x :y :y}}]}]}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"non-numeric across all sharing cells"
           (pose/inject-shared-scales tree))))))

(deftest inject-shared-scales-explicit-density-stat-test
  (testing "explicit :stat :density triggers the exemption"
    (let [ds (tc/dataset {:a [1.0 2.0 3.0]
                          :b [10.0 20.0 30.0]})
          tree {:data ds
                :share-scales #{:y}
                :poses [{:layers [{:layer-type :point :mapping {:x :a :y :b}}]}
                        {:mapping {:y :b}
                         :layers [{:layer-type :line
                                   :stat :density
                                   :mapping {:x :a}}]}]}
          [scatter density] (pose/resolve-tree (pose/inject-shared-scales tree))]
      (is (= [10.0 30.0] (:y-scale-domain (:opts scatter))))
      (is (nil? (:y-scale-domain (:opts density)))
          ":density layer's y axis is density values, not data"))))
