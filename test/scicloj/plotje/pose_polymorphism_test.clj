(ns scicloj.plotje.pose-polymorphism-test
  "Specification for Phase 6's polymorphic pj/pose.

   These tests describe the target behavior after the Sketch record
   and :views adapter are retired. pj/pose dispatches on its first
   argument: raw data creates a leaf; an existing pose may be
   extended, promoted to a composite, or appended-to. Empty :mapping
   and :opts are never present in output (strict elision, Reading A
   per Phase 6 design discussion 2026-04-24).

   Case labels (A*, C*, E*, P*) are local identifiers grouping related
   tests. The C* and A* families align with rule families taught in
   notebooks/plotje_book/pose_rules.clj; E* (extension) and P* (panel)
   are test-internal."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]))

;; ============================================================
;; Shared fixtures
;; ============================================================

(def iris
  "Synthetic stand-in for rdatasets iris, using :a :b :c :d :e :f as
   placeholder column names to match the design-doc notation."
  (tc/dataset {:a       [1.0 2.0 3.0]
               :b       [0.5 1.0 1.5]
               :c       [10.0 20.0 30.0]
               :d       [100.0 200.0 300.0]
               :e       [1000.0 2000.0 3000.0]
               :f       [10000.0 20000.0 30000.0]
               :species ["setosa" "setosa" "versicolor"]}))

(defn- all-nodes
  "Seq of every pose and layer in the tree -- for property checks."
  [fr]
  (concat [fr]
          (:layers fr)
          (mapcat all-nodes (:poses fr))))

(defn- no-empty-map-output? [fr]
  (every? (fn [node]
            (not (or (and (contains? node :mapping) (empty? (:mapping node)))
                     (and (contains? node :opts) (empty? (:opts node))))))
          (all-nodes fr)))

;; ============================================================
;; Creation -- raw data input (C1-C6)
;; ============================================================

(deftest creation-raw-data-test
  (testing "C1: (pj/pose) -- arity-0 empty leaf, mapping elided"
    (let [f (pj/pose)]
      (is (= [] (:layers f)))
      (is (not (contains? f :mapping)))
      (is (not (contains? f :data)))
      (is (not (contains? f :poses)))))

  (testing "C2: (pj/pose iris) -- data only, mapping elided"
    (let [f (pj/pose iris)]
      (is (tc/dataset? (:data f)))
      (is (= [] (:layers f)))
      (is (not (contains? f :mapping)))
      (is (not (contains? f :poses)))))

  (testing "C3: (pj/pose iris {:color :species}) -- aesthetic-only mapping"
    (let [f (pj/pose iris {:color :species})]
      (is (= {:color :species} (:mapping f)))
      (is (= [] (:layers f)))
      (is (not (contains? f :poses)))))

  (testing "C4: (pj/pose iris :a) -- positional x only"
    (let [f (pj/pose iris :a)]
      (is (= {:x :a} (:mapping f)))
      (is (= [] (:layers f)))))

  (testing "C5: (pj/pose iris :a :b) -- positional x and y"
    (let [f (pj/pose iris :a :b)]
      (is (= {:x :a :y :b} (:mapping f)))
      (is (= [] (:layers f)))))

  (testing "C6: (pj/pose iris :a :b {:color :species}) -- position + opts"
    (let [f (pj/pose iris :a :b {:color :species})]
      (is (= {:x :a :y :b :color :species} (:mapping f)))
      (is (= [] (:layers f))))))

;; ============================================================
;; Extend -- existing leaf with room to grow (E1-E2)
;; ============================================================
;;
;; When a leaf has no position-bearing state (no :x/:y in its own
;; :mapping, no position-carrying layers), a subsequent pj/pose call
;; extends the leaf's mapping rather than promoting to composite.

(deftest extend-leaf-test
  (testing "E1: empty leaf, then position -- extends mapping"
    (let [f (-> iris pj/pose (pj/pose :a :b))]
      (is (= {:x :a :y :b} (:mapping f)))
      (is (= [] (:layers f)))
      (is (not (contains? f :poses)))))

  (testing "E2: aesthetic-only leaf, then position -- extends mapping"
    (let [f (-> iris (pj/pose {:color :species}) (pj/pose :a :b))]
      (is (= {:x :a :y :b :color :species} (:mapping f)))
      (is (= [] (:layers f)))
      (is (not (contains? f :poses))))))

;; ============================================================
;; Promote -- leaf-with-position plus a new position (P1-P3)
;; ============================================================
;;
;; When the receiver leaf already carries position (:x or :y in its
;; :mapping), another position-bearing pj/pose call promotes to a
;; composite. Aesthetic parts split to the composite's root; position
;; parts stay on the per-panel sub-poses.

(deftest promote-via-position-test
  (testing "P1 (guiding): identical positions -- two identical panels"
    (let [f (-> iris (pj/pose :a :b) (pj/pose :a :b))]
      (is (= 2 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :a :y :b} (-> f :poses (nth 1) :mapping)))
      (is (not (contains? f :mapping)))
      (is (not (contains? f :layers)))))

  (testing "P2: distinct positions -- two distinct panels"
    (let [f (-> iris (pj/pose :a :b) (pj/pose :c :d))]
      (is (= 2 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping)))
      (is (not (contains? f :mapping)))))

  (testing "P3: aesthetic in first call splits to root; positions to panels"
    (let [f (-> iris (pj/pose :a :b {:color :species}) (pj/pose :c :d))]
      (is (= {:color :species} (:mapping f)))
      (is (= 2 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping))))))

;; ============================================================
;; Promote -- aesthetic-only call on leaf-with-position (A1-A2)
;; ============================================================
;;
;; Aesthetic-only calls do not add a panel; they promote the leaf so
;; the aesthetic lives at composite root (flowing to every descendant
;; panel). Subsequent position calls append panels as usual.

(deftest promote-via-aesthetic-test
  (testing "A1: aesthetic-only on leaf-with-position promotes, no new panel"
    (let [f (-> iris (pj/pose :a :b) (pj/pose {:color :species}))]
      (is (= {:color :species} (:mapping f)))
      (is (= 1 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))))

  (testing "A2: aesthetic-then-position gives root aesthetic + two panels"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/pose {:color :species})
                (pj/pose :c :d))]
      (is (= {:color :species} (:mapping f)))
      (is (= 2 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping))))))

;; ============================================================
;; Layer partitioning during promotion (L1-L4)
;; ============================================================
;;
;; Partition rule is single and self-describing: inspect each layer's
;; own :mapping. Layers carrying :x or :y ("panel-origin") stay with
;; sub-pose 1. Layers without :x/:y ("root-origin") move to the new
;; composite's root :layers -- they distribute to every panel via
;; resolve-tree.

(deftest layer-partitioning-test
  (testing "L1 (Pattern D): bare lay-point routes to root, flows to both"
    (let [f (-> iris (pj/pose :a :b) pj/lay-point (pj/pose :c :d))]
      (is (= 1 (count (:layers f))))
      (is (= :point (-> f :layers (nth 0) :layer-type)))
      (is (not (contains? (-> f :layers (nth 0)) :mapping)))
      (is (= 2 (count (:poses f))))
      (is (= [] (-> f :poses (nth 0) :layers)))
      (is (= [] (-> f :poses (nth 1) :layers)))))

  (testing "L2: position-matching lay-point stays in sub-pose 1"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/lay-point :a :b)
                (pj/pose :c :d))]
      (is (or (not (contains? f :layers))
              (= [] (:layers f))))
      (is (= 1 (count (-> f :poses (nth 0) :layers))))
      (is (= :point (-> f :poses (nth 0) :layers (nth 0) :layer-type)))
      (is (= [] (-> f :poses (nth 1) :layers)))))

  (testing "L3: mixed bare + position layers partition correctly"
    (let [f (-> iris
                (pj/pose :a :b)
                pj/lay-point
                (pj/lay-line :a :b)
                (pj/pose :c :d))]
      (is (= 1 (count (:layers f))))
      (is (= :point (-> f :layers (nth 0) :layer-type)))
      (is (= 1 (count (-> f :poses (nth 0) :layers))))
      (is (= :line (-> f :poses (nth 0) :layers (nth 0) :layer-type)))
      (is (= [] (-> f :poses (nth 1) :layers)))))

  (testing "L4: aesthetic-only on layer (no :x/:y) routes to root"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/lay-point {:color :species})
                (pj/pose :c :d))]
      (is (= 1 (count (:layers f))))
      (is (= {:color :species} (-> f :layers (nth 0) :mapping)))
      (is (= [] (-> f :poses (nth 0) :layers)))
      (is (= [] (-> f :poses (nth 1) :layers))))))

;; ============================================================
;; No-op on empty call (N1-N2)
;; ============================================================
;;
;; A 1-arity pj/pose on an existing leaf or composite returns the
;; input unchanged. (Distinct from (pj/pose data) where `data` is raw
;; -- that creates a new leaf; see C2.)

(deftest no-op-empty-call-test
  (testing "N1: (pj/pose leaf) returns leaf unchanged"
    (let [leaf (pj/pose iris :a :b)
          again (pj/pose leaf)]
      (is (= leaf again))))

  (testing "N2: (pj/pose composite) returns composite unchanged"
    (let [composite (-> iris (pj/pose :a :b) (pj/pose :c :d))
          again (pj/pose composite)]
      (is (= composite again)))))

;; ============================================================
;; Composite append -- third panel and beyond (K1)
;; ============================================================

(deftest composite-append-test
  (testing "K1: three pj/pose position calls in a row give three panels"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/pose :c :d)
                (pj/pose :e :f))]
      (is (= 3 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping)))
      (is (= {:x :e :y :f} (-> f :poses (nth 2) :mapping))))))

;; ============================================================
;; Multi-pair pj/pose -- broadcast N panels in one call (M1-M6)
;; ============================================================
;;
;; (pj/pose fr [[:a :b] [:c :d]]) and (pj/pose fr [:a :b :c]) expand
;; to an iterated sequence of (pj/pose fr ...) calls, one per pair or
;; column. The result is equivalent to threading pj/pose N times.
;; This restores the panel-broadcast half of the old pj/view that was
;; dropped in slice 1. The canonical use is SPLOM:
;;
;;   (-> data (pj/pose {:color :species})
;;            pj/lay-point
;;            (pj/pose (pj/cross cols cols)))

(deftest multi-pair-raw-data-bivariate-test
  (testing "M1: (pj/pose data [[:a :b] [:c :d]]) -- vector of pairs"
    (let [f (pj/pose iris [[:a :b] [:c :d]])]
      (is (= 2 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping))))))

(deftest multi-pair-raw-data-univariate-test
  (testing "M2: (pj/pose data [:a :b :c]) -- vector of columns"
    (let [f (pj/pose iris [:a :b :c])]
      (is (= 3 (count (:poses f))))
      (is (= {:x :a} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :b} (-> f :poses (nth 1) :mapping)))
      (is (= {:x :c} (-> f :poses (nth 2) :mapping))))))

(deftest multi-pair-extend-existing-test
  (testing "M3: threaded onto a leaf-with-position -- promote + append"
    (let [f (-> iris (pj/pose :a :b) (pj/pose [[:c :d] [:e :f]]))]
      (is (= 3 (count (:poses f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping)))
      (is (= {:x :e :y :f} (-> f :poses (nth 2) :mapping))))))

(deftest multi-pair-root-layer-flows-test
  (testing "M4: root layer flows to every panel via resolve-tree"
    (let [f (-> iris pj/pose pj/lay-point (pj/pose [[:a :b] [:c :d]]))]
      (is (= 2 (count (:poses f))))
      (is (= 1 (count (:layers f))))
      (is (= :point (-> f :layers (nth 0) :layer-type))))))

(deftest multi-pair-root-aesthetic-splom-pattern-test
  (testing "M5: full SPLOM pattern -- root aesthetic + root layer + N panels"
    (let [f (-> iris
                (pj/pose {:color :species})
                pj/lay-point
                (pj/pose [[:a :b] [:c :d]]))]
      (is (= {:color :species} (:mapping f)))
      (is (= 2 (count (:poses f))))
      (is (= 1 (count (:layers f))))
      (is (= {:x :a :y :b} (-> f :poses (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f :poses (nth 1) :mapping))))))

(deftest multi-pair-cross-utility-test
  (testing "M6: (pj/pose data (pj/cross cols cols)) builds a SPLOM grid"
    ;; (pj/cross cols cols) is the canonical SPLOM input -- an MxM
    ;; Cartesian rectangle. See the G1-G5 tests below for the full
    ;; grid-shape contract; here we pin that it is NOT the flat
    ;; composite (which was the slice-1 behaviour).
    (let [cols [:a :b]
          f (pj/pose iris (pj/cross cols cols))]
      (is (= 2 (count (:poses f))) "2 rows, not 4 flat sub-poses")
      (is (= :vertical (get-in f [:layout :direction])))
      (is (= #{:x :y} (get-in f [:opts :share-scales]))))))

(deftest multi-pair-iteration-equivalence-test
  (testing "M7: (pj/pose fr vec-of-pairs) -- non-rectangular pair list threads per-pair"
    ;; 3 pairs that do NOT form a Cartesian rectangle -- keeps flat
    ;; behaviour; equivalent to threading pj/pose three times.
    (let [expected (-> iris
                       (pj/pose :a :b)
                       (pj/pose :c :d)
                       (pj/pose :e :f))
          actual (pj/pose iris [[:a :b] [:c :d] [:e :f]])]
      (is (= expected actual)))))

;; ============================================================
;; Multi-pair pj/pose -- rectangular grid reshape (G1-G5)
;; ============================================================
;;
;; When multi-pair pj/pose receives pairs that form an M x N
;; Cartesian rectangle (every combination of unique first-elements
;; with unique second-elements, in cross-order), the result is a
;; nested composite: outer :vertical of rows, one per distinct y;
;; each row is :horizontal of cells, one per distinct x.
;; :share-scales is stamped as #{:x :y} so axes align across rows and
;; columns -- the canonical SPLOM shape.
;;
;; A cell's x is named by the column it sits in and its y by the row,
;; which is how a scatterplot matrix is read and what the tick
;; suppression in grid-composite assumes (x ticks on the bottom row,
;; y ticks in the leftmost column). The assertions below pin that
;; orientation: written the other way round, every row but the last
;; loses its x axis and every column but the first its y axis, and a
;; reader takes a neighbour's numbers for its own.
;;
;; Pair lists that are not rectangular keep the flat-reduce
;; behaviour asserted in M7.

(deftest multi-pair-grid-splom-shape-test
  (testing "G1: (pj/cross cols cols) produces M x N nested composite"
    (let [f (pj/pose iris (pj/cross [:a :b] [:c :d]))]
      (is (= :vertical (get-in f [:layout :direction])))
      (is (= #{:x :y} (get-in f [:opts :share-scales])))
      (is (= 2 (count (:poses f))) "2 rows")
      (let [row-0 (first (:poses f))
            row-1 (second (:poses f))]
        (is (= :horizontal (get-in row-0 [:layout :direction])))
        (is (= 2 (count (:poses row-0))) "2 cells per row")
        (is (= 2 (count (:poses row-1))))
        ;; row 0: y = :c, x varies [:a :b] across the columns
        (is (= {:x :a :y :c} (:mapping (first (:poses row-0)))))
        (is (= {:x :b :y :c} (:mapping (second (:poses row-0)))))
        ;; row 1: y = :d, x varies the same way
        (is (= {:x :a :y :d} (:mapping (first (:poses row-1)))))
        (is (= {:x :b :y :d} (:mapping (second (:poses row-1)))))))))

(deftest multi-pair-grid-3x3-test
  (testing "G2: 3 x 3 SPLOM via pj/cross"
    (let [cols [:a :b :c]
          f (pj/pose iris (pj/cross cols cols))]
      (is (= 3 (count (:poses f))) "3 rows")
      (is (every? #(= 3 (count (:poses %))) (:poses f)) "3 cells each"))))

(deftest multi-pair-grid-root-aesthetic-test
  (testing "G3: SPLOM pattern -- root aesthetic + root layer + grid"
    (let [f (-> iris
                (pj/pose {:color :species})
                pj/lay-point
                (pj/pose (pj/cross [:a :b] [:c :d])))]
      (is (= {:color :species} (:mapping f))
          "root aesthetic preserved")
      (is (= 1 (count (:layers f)))
          "root layer preserved")
      (is (= :point (-> f :layers first :layer-type)))
      (is (= 2 (count (:poses f))) "2 rows")
      (is (every? #(= 2 (count (:poses %))) (:poses f)) "2 cells each"))))

(deftest multi-pair-grid-with-opts-arity-test
  (testing "G3b: 3-arity (pj/pose data multi-pair opts-map) folds the
            two-call SPLOM idiom into one call -- aesthetic at the root,
            grid below."
    (let [a (-> iris
                (pj/pose {:color :species})
                (pj/pose (pj/cross [:a :b] [:c :d])))
          b (-> iris
                (pj/pose (pj/cross [:a :b] [:c :d]) {:color :species}))]
      (is (= a b) "two-call and one-call forms are pose-equal")
      (is (= {:color :species} (:mapping b))
          "root aesthetic mapping lives at the composite root")
      (is (= 2 (count (:poses b))) "2 rows")
      (is (every? #(= 2 (count (:poses %))) (:poses b)) "2 cells each"))))

(deftest multi-pair-grid-non-rectangular-falls-through-test
  (testing "G4: non-rectangular pair list keeps flat composite"
    ;; 2 pairs: [:a :b] [:c :d] -- would need [:a :d] [:c :b] for a 2x2
    ;; rectangle, so falls through to flat reduce.
    (let [f (pj/pose iris [[:a :b] [:c :d]])]
      (is (= 2 (count (:poses f))))
      ;; Flat composite -- each sub-pose is a leaf with mapping, no
      ;; nested :poses
      (is (every? #(not (contains? % :poses)) (:poses f))))))

(deftest multi-pair-grid-1xN-falls-through-test
  (testing "G5: a 1xN shape does not grid-reshape"
    ;; (pj/cross [:a] [:b :c :d]) gives 3 pairs but only 1 unique x --
    ;; 1 row. We require at least 2 rows and 2 cols to reshape.
    (let [f (pj/pose iris (pj/cross [:a] [:b :c :d]))]
      ;; Falls through to flat -- 3 sub-poses at root
      (is (= 3 (count (:poses f))))
      (is (every? #(not (contains? % :poses)) (:poses f)))
      (is (not (contains? (:opts f) :share-scales))))))

;; ============================================================
;; Integration with pj/lay-* and resolve-tree (I1-I3)
;; ============================================================

(deftest integration-lay-resolve-test
  (testing "I1: root-origin layer reaches both panels through resolve-tree"
    (let [f (-> iris (pj/pose :a :b) pj/lay-point (pj/pose :c :d))
          leaves (pose/resolve-tree f)]
      (is (= 2 (count leaves)))
      (is (every? (fn [leaf] (= 1 (count (:layers leaf)))) leaves)
          "each resolved leaf carries the root layer")))

  (testing "I2: panel-specific layer does not leak across panels"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/lay-point :a :b)
                (pj/pose :c :d))
          leaves (pose/resolve-tree f)
          layers-per-leaf (mapv #(count (:layers %)) leaves)]
      (is (= [1 0] layers-per-leaf)
          "leaf 1 has the layer; leaf 2 does not")))

  (testing "I3: root aesthetic merges into both resolved leaves' mappings"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/pose {:color :species})
                (pj/pose :c :d))
          leaves (pose/resolve-tree f)]
      (is (= 2 (count leaves)))
      (is (every? (fn [leaf] (= :species (get-in leaf [:mapping :color])))
                  leaves)))))

;; ============================================================
;; Property-style checks
;; ============================================================

(deftest no-empty-map-output-property-test
  (testing "No pose or layer in the resulting tree carries {} for mapping or opts"
    (is (no-empty-map-output? (pj/pose)))
    (is (no-empty-map-output? (pj/pose iris)))
    (is (no-empty-map-output? (pj/pose iris :a :b)))
    (is (no-empty-map-output? (-> iris (pj/pose :a :b) (pj/pose :c :d))))
    (is (no-empty-map-output? (-> iris (pj/pose :a :b) pj/lay-point)))
    (is (no-empty-map-output?
         (-> iris (pj/pose :a :b) pj/lay-point (pj/pose :c :d))))))

(deftest poses-preserve-call-order-test
  (testing "Sub-poses appear in left-to-right pj/pose invocation order"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/pose :c :d)
                (pj/pose :e :f))]
      (is (= [{:x :a :y :b} {:x :c :y :d} {:x :e :y :f}]
             (mapv :mapping (:poses f)))))))

(deftest promotion-shape-idempotence-test
  (testing "Threading pj/pose three times equals threading twice + once"
    (let [three-in-a-row (-> iris
                             (pj/pose :a :b)
                             (pj/pose :c :d)
                             (pj/pose :e :f))
          built-in-two   (let [two (-> iris
                                       (pj/pose :a :b)
                                       (pj/pose :c :d))]
                           (pj/pose two :e :f))]
      (is (= three-in-a-row built-in-two)))))

;; ============================================================
;; Layer position vs pose position -- overlay semantics
;; ============================================================
;;
;; When a leaf already carries position and `pj/lay-*` is called with
;; a non-matching position, the leaf is promoted into a 2-panel
;; composite. The original leaf's layers stay with panel-1; the new
;; sub-pose carries the new position and the new layer. This is the
;; lay-* analog of Pose Rule LP3 (which already handled the composite
;; case the same way): distinct positional aesthetics mean distinct
;; poses, applied symmetrically across leaf and composite inputs.

(deftest leaf-layer-non-matching-position-promotes-test
  (testing "leaf with position, lay-* with non-matching position -- promotes"
    (let [fr (-> iris
                 (pj/pose :a :b)
                 (pj/lay-point :c :d))]
      (is (= 2 (count (:poses fr))))
      (is (= {:x :a :y :b} (:mapping (first (:poses fr)))))
      (is (= {:x :c :y :d} (:mapping (second (:poses fr)))))))
  (testing "promotion keeps the original layer on panel-1 (not flowing root)"
    (let [fr (-> iris
                 (pj/lay-point :a :b)
                 (pj/lay-point :c :d))]
      (is (nil? (:layers fr)))
      (is (= 1 (count (:layers (first (:poses fr))))))
      (is (= 1 (count (:layers (second (:poses fr))))))))
  (testing "matching position passes -- redundant :mapping on the layer is fine"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/lay-point :a :b))]
      (is (= {:x :a :y :b} (:mapping f)))
      (is (= 1 (count (:layers f))))))
  (testing "string vs keyword: distinct column references, so promotes"
    ;; Layer-level :data with string-named columns makes the safety
    ;; check pass: the new sub-pose has columns "a" and "b" to map
    ;; to. Without it, the safety check correctly notes that strings
    ;; "a"/"b" do not match the iris dataset's keyword columns.
    (let [fr (-> iris
                 (pj/pose :a :b)
                 (pj/lay-point "a" "b"
                               {:data (tc/dataset {"a" [1 2] "b" [1 2]})}))]
      (is (= 2 (count (:poses fr))))
      (is (= {:x :a :y :b} (:mapping (first (:poses fr)))))
      (is (= {:x "a" :y "b"} (:mapping (second (:poses fr))))))))

(deftest promotion-keeps-a-layers-own-mapping-test
  ;; Promotion stamps the leaf's position onto a layer that names
  ;; none, so promote-leaf keeps that layer with panel-1. The stamp
  ;; merges into the layer's mapping rather than replacing it: a
  ;; layer's aesthetics and its layer-type options share that slot,
  ;; and replacing it dropped every one of them in silence.
  (testing "an aesthetic written on the layer survives the split"
    (let [layer (-> iris
                    (pj/lay-point :a :b {:color "#377eb8"})
                    (pj/lay-point :c :d)
                    :poses
                    first
                    :layers
                    first)]
      (is (= {:x :a :y :b :color "#377eb8"} (:mapping layer)))))
  (testing "and reaches the drawn marks"
    (let [colors (->> (-> iris
                          (pj/lay-point :a :b {:color "#377eb8"})
                          (pj/lay-point :c :d)
                          pj/svg-summary
                          :colors)
                      (remove #{"none"})
                      set)]
      ;; #377eb8 is rgb(55,126,184); the default point grey is
      ;; rgb(51,51,51), which is what the panel-2 layer draws.
      (is (contains? colors "rgb(55,126,184)"))))
  (testing "a layer-type option written on the layer survives too"
    (let [layer (-> iris
                    (pj/lay-histogram :a {:bins 3})
                    (pj/lay-histogram :c)
                    :poses
                    first
                    :layers
                    first)]
      (is (= 3 (:bins (:mapping layer))))))
  (testing "a layer that names its own position is left alone"
    (let [layer (-> iris
                    (pj/pose :a :b)
                    (pj/lay-point :a :b {:color "#377eb8"})
                    (pj/lay-point :c :d)
                    :poses
                    first
                    :layers
                    first)]
      (is (= {:x :a :y :b :color "#377eb8"} (:mapping layer))))))

;; ============================================================
;; Layer structural keys -- :stat, :position, :mark (Decision 1)
;; ============================================================
;;
;; :layer-type, :stat, :position, and explicit :mark are first-class
;; sibling keys on the layer map. :mapping is reserved for
;; column-to-aesthetic bindings only.

(deftest layer-structural-keys-test
  (testing ":stat is a sibling key, not inside :mapping"
    (let [layer (-> (pj/pose iris :a :b)
                    (pj/lay-smooth {:stat :linear-model})
                    :layers
                    first)]
      (is (= :linear-model (:stat layer)))
      (is (not (contains? (or (:mapping layer) {}) :stat)))))

  (testing ":position is a sibling key, not inside :mapping"
    (let [layer (-> (pj/pose iris :a :b)
                    (pj/lay-bar {:position :dodge})
                    :layers
                    first)]
      (is (= :dodge (:position layer)))
      (is (not (contains? (or (:mapping layer) {}) :position)))))

  (testing ":mark (user override) is a sibling key, not inside :mapping"
    (let [layer (-> (pj/pose iris :a :b)
                    (pj/lay-point {:mark :line})
                    :layers
                    first)]
      (is (= :line (:mark layer)))
      (is (not (contains? (or (:mapping layer) {}) :mark)))))

  (testing "aesthetic keys stay in :mapping"
    (let [layer (-> (pj/pose iris :a :b)
                    (pj/lay-point {:color :species :alpha 0.5})
                    :layers
                    first)]
      (is (= {:color :species :alpha 0.5} (:mapping layer)))
      (is (not (contains? layer :color)))
      (is (not (contains? layer :alpha))))))
