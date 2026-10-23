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
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
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

(defn- panels
  "The leaf poses of `fr` in layout order. pj/arrange nests its cells
  in rows, so the panel list is not `(:poses fr)`."
  [fr]
  (if (seq (:poses fr))
    (vec (mapcat panels (:poses fr)))
    [fr]))

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
;; A mapping writer -- position on a leaf that has one (P1-P4)
;; ============================================================
;;
;; `pj/pose` merges into the pose's mapping and never adds a panel. A
;; new `:x` or `:y` replaces the old one, so a pose returned by a
;; function can be pointed at other columns.

(deftest mapping-writer-position-test
  (testing "P1: the same position again gives the same pose"
    (let [p (pj/pose iris :a :b)]
      (is (= p (pj/pose p :a :b)))))

  (testing "P2: a different position replaces the old one"
    (let [f (-> iris (pj/pose :a :b) (pj/pose :c :d))]
      (is (= {:x :c :y :d} (:mapping f)))
      (is (not (contains? f :poses)))))

  (testing "P3: the aesthetic stays"
    (is (= {:x :c :y :d :color :species}
           (:mapping (-> iris (pj/pose :a :b {:color :species}) (pj/pose :c :d))))))

  (testing "P4: a pose returned by a function, pointed at other columns"
    (let [trend (fn [data] (-> data
                               (pj/pose :sepal-length :sepal-width)
                               pj/lay-point
                               pj/lay-smooth))
          real-iris (rdatasets/datasets-iris)
          moved (pj/pose (trend real-iris) :petal-length :petal-width)]
      (is (= {:x :petal-length :y :petal-width} (:mapping moved)))
      (is (= [{:layer-type :point} {:layer-type :smooth}] (:layers moved)))
      (is (= (pj/plot moved)
             (pj/plot (-> real-iris
                          (pj/pose :petal-length :petal-width)
                          pj/lay-point
                          pj/lay-smooth)))))))

;; ============================================================
;; A mapping writer -- aesthetic on a leaf with position (A1-A2)
;; ============================================================

(deftest mapping-writer-aesthetic-test
  (testing "A1: an aesthetic merges into the leaf's mapping"
    (let [f (-> iris (pj/pose :a :b) (pj/pose {:color :species}))]
      (is (= {:x :a :y :b :color :species} (:mapping f)))
      (is (not (contains? f :poses)))))

  (testing "A2: the layers stay on the leaf"
    (let [f (-> iris (pj/pose :a :b) pj/lay-point (pj/pose {:color :species}))]
      (is (= [{:layer-type :point}] (:layers f)))
      (is (= {:x :a :y :b :color :species} (:mapping f))))))

;; ============================================================
;; Laws of a mapping writer (W1-W3)
;; ============================================================

(deftest mapping-writer-laws-test
  (let [p (-> iris (pj/pose :a :b {:color :species}) pj/lay-point)]
    (testing "W1: (pj/pose p) is p"
      (is (= p (pj/pose p))))
    (testing "W2: (pj/pose p {}) is p"
      (is (= p (pj/pose p {}))))
    (testing "W3: writing a mapping twice is writing it once"
      (is (= (pj/pose p {:size :c}) (pj/pose (pj/pose p {:size :c}) {:size :c}))))
    (testing "W4: on a composite, an aesthetic merges into the root"
      (let [c (pj/arrange iris [[:a :b] [:c :d]])]
        (is (= {:color :species} (:mapping (pj/pose c {:color :species}))))
        (is (= c (pj/pose c {})))))))

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
    (let [composite (pj/arrange iris [[:a :b] [:c :d]])
          again (pj/pose composite)]
      (is (= composite again)))))

;; ============================================================
;; A position on a composite (K1-K3)
;; ============================================================

(deftest composite-append-test
  (testing "K1: a position on a composite goes to the root; cells that map
            their own override it, and the draft warns that it is not drawn"
    (let [real-iris (rdatasets/datasets-iris)
          c (-> real-iris
                (pj/arrange [[:sepal-length :sepal-width]])
                (pj/pose :petal-length :petal-width)
                pj/lay-point)
          out (with-out-str (pj/plan c))]
      (is (= {:x :petal-length :y :petal-width} (:mapping c)))
      (is (re-find #"not drawn" out))
      (is (= (pj/plot c)
             (pj/plot (-> real-iris (pj/arrange [[:sepal-length :sepal-width]]) pj/lay-point))))))
  (testing "K2: no warning where a cell reads the root's position"
    (let [real-iris (rdatasets/datasets-iris)]
      (is (= "" (with-out-str
                  (pj/plan (-> (pj/arrange [(pj/pose real-iris {:y :sepal-width})])
                               (pj/pose {:x :sepal-length})
                               pj/lay-point)))))))
  (testing "K1: the same where the cells name their columns on their layers"
    (let [real-iris (rdatasets/datasets-iris)
          cells [(pj/lay-point real-iris :sepal-length :sepal-width)
                 (pj/lay-point real-iris :sepal-length :petal-width)]
          out (with-out-str (pj/plan (-> (pj/arrange cells) (pj/pose :petal-length :petal-width))))]
      (is (re-find #"maps :x to :petal-length, and every cell below it names its own :x" out))
      (is (re-find #"maps :y to :petal-width" out))
      (is (= "" (with-out-str (pj/plan (pj/arrange cells)))))))
  (testing "K3: cells are appended with pj/arrange"
    (let [f (-> iris (pj/arrange [[:a :b]]) (pj/arrange [[:c :d] [:e :f]]))]
      (is (= [{:x :a :y :b} {:x :c :y :d} {:x :e :y :f}]
             (mapv :mapping (panels f))))))
  (testing "K3: a column and a pair are each read the same wherever they stand"
    (let [base (pj/arrange iris [[:a :b]])
          cells (fn [f] (mapv :mapping (rest (panels f))))]
      (is (= [{:x :c :y :d} {:x :e}] (cells (pj/arrange base [[:c :d] :e]))))
      (is (= [{:x :e} {:x :c :y :d}] (cells (pj/arrange base [:e [:c :d]]))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"appends columns and pairs of columns.*\[:c :d :e\] is neither"
                            (pj/arrange base [[:c :d :e]]))))))

;; ============================================================
;; Multi-pair pj/arrange -- N panels in one call (M1-M7)
;; ============================================================
;;
;; (pj/arrange fr [[:a :b] [:c :d]]) and (pj/arrange fr [:a :b :c])
;; make one panel per pair or column. pj/arrange lays the panels out
;; as a grid: a vertical composite of horizontal rows, so the panels
;; are the leaves of that tree, not (:poses f). The `panels` helper
;; collects them in layout order. The canonical use is a row of
;; panels that share a root aesthetic and layer:
;;
;;   (-> data (pj/pose {:color :species})
;;            pj/lay-point
;;            (pj/arrange [[:a :b] [:c :d]]))
;;
;; For the full SPLOM grid, see pj/facet-matrix (G1-G5 below).

(deftest multi-pair-raw-data-bivariate-test
  (testing "M1: (pj/arrange data [[:a :b] [:c :d]]) -- vector of pairs"
    (let [f (pj/arrange iris [[:a :b] [:c :d]])]
      (is (= 2 (count (panels f))))
      (is (= {:x :a :y :b} (-> f panels (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f panels (nth 1) :mapping))))))

(deftest multi-pair-raw-data-univariate-test
  (testing "M2: (pj/arrange data [:a :b :c]) -- vector of columns"
    (let [f (pj/arrange iris [:a :b :c])]
      (is (= 3 (count (panels f))))
      (is (= {:x :a} (-> f panels (nth 0) :mapping)))
      (is (= {:x :b} (-> f panels (nth 1) :mapping)))
      (is (= {:x :c} (-> f panels (nth 2) :mapping))))))

(deftest multi-pair-extend-existing-test
  (testing "M3: arranged from a pose with a position -- the position stays
            at the root and the pairs become the panels"
    (let [f (-> iris (pj/pose :a :b) (pj/arrange [[:c :d] [:e :f]]))]
      (is (= {:x :a :y :b} (:mapping f)))
      (is (= 2 (count (panels f))))
      (is (= {:x :c :y :d} (-> f panels (nth 0) :mapping)))
      (is (= {:x :e :y :f} (-> f panels (nth 1) :mapping))))))

(deftest multi-pair-root-layer-flows-test
  (testing "M4: root layer flows to every panel via resolve-tree"
    (let [f (-> iris pj/pose pj/lay-point (pj/arrange [[:a :b] [:c :d]]))]
      (is (= 2 (count (panels f))))
      (is (= 1 (count (:layers f))))
      (is (= :point (-> f :layers (nth 0) :layer-type))))))

(deftest multi-pair-root-aesthetic-splom-pattern-test
  (testing "M5: full SPLOM pattern -- root aesthetic + root layer + N panels"
    (let [f (-> iris
                (pj/pose {:color :species})
                pj/lay-point
                (pj/arrange [[:a :b] [:c :d]]))]
      (is (= {:color :species} (:mapping f)))
      (is (= 2 (count (panels f))))
      (is (= 1 (count (:layers f))))
      (is (= {:x :a :y :b} (-> f panels (nth 0) :mapping)))
      (is (= {:x :c :y :d} (-> f panels (nth 1) :mapping))))))

(deftest multi-pair-cross-utility-test
  (testing "M6: (pj/facet-matrix data cols) writes the facet map crossing cols"
    (let [cols [:a :b]
          f (pj/facet-matrix iris cols)]
      (is (= {:col {:x cols} :row {:y cols}} (:facet f)))
      (is (nil? (:poses f)) "a pose with one panel, split when drawn"))))

(deftest multi-pair-iteration-equivalence-test
  (testing "M7: (pj/arrange data pairs) -- one panel per pair, in order"
    (let [f (pj/arrange iris [[:a :b] [:c :d] [:e :f]])]
      (is (= [{:x :a :y :b} {:x :c :y :d} {:x :e :y :f}]
             (mapv :mapping (panels f)))))))

;; ============================================================
;; pj/facet-matrix -- the facet map crossing two lists (G1-G5)
;; ============================================================
;;
;; pj/facet-matrix writes `{:col {:x xs} :row {:y ys}}` on a pose with
;; one panel. When it is drawn the facet expands into a grid: a row per
;; `:y` column and, across it, a cell per `:x` column. A cell's x is
;; named by the column it sits in and its y by the row, which is how a
;; scatterplot matrix is read; the assertions below pin that
;; orientation.

(defn- cell-mappings
  "The mappings of the cells a facet over columns expands into, row by
   row."
  [f]
  (mapv (fn [row] (mapv :mapping (:poses row)))
        (:poses (pose/expand-facet-nodes f))))

(deftest multi-pair-grid-splom-shape-test
  (testing "G1: two lists of columns cross into a grid, x across and y down"
    (let [f (pj/facet-matrix iris [:a :b] [:c :d])]
      (is (= {:col {:x [:a :b]} :row {:y [:c :d]}} (:facet f)))
      (is (= [[{:x :a :y :c} {:x :b :y :c}]
              [{:x :a :y :d} {:x :b :y :d}]]
             (cell-mappings f)))
      (is (= 4 (:panels (pj/svg-summary f)))))))

(deftest multi-pair-grid-3x3-test
  (testing "G2: 3 x 3 SPLOM"
    (let [f (pj/facet-matrix iris [:a :b :c])]
      (is (= 9 (:panels (pj/svg-summary f)))))))

(deftest multi-pair-grid-root-aesthetic-test
  (testing "G3: SPLOM pattern -- aesthetic and layer on the pose, facet below"
    (let [f (-> iris
                (pj/pose {:color :species})
                pj/lay-point
                (pj/facet-matrix [:a :b]))]
      (is (= {:color :species} (:mapping f)) "aesthetic preserved")
      (is (= 1 (count (:layers f))) "layer preserved")
      (is (= :point (-> f :layers first :layer-type)))
      (is (= {:col {:x [:a :b]} :row {:y [:a :b]}} (:facet f))))))

(deftest multi-pair-grid-with-opts-arity-test
  (testing "G3b: (pj/facet-matrix data xs ys mapping) folds the two-call
            idiom into one call"
    (let [a (-> iris
                (pj/pose {:color :species})
                (pj/facet-matrix [:a :b] [:c :d]))
          b (-> iris
                (pj/facet-matrix [:a :b] [:c :d] {:color :species}))]
      (is (= a b) "two-call and one-call forms are pose-equal")
      (is (= {:color :species} (:mapping b))))))

(deftest multi-pair-grid-non-rectangular-falls-through-test
  (testing "G4: a non-rectangular pair list goes through pj/arrange --
            one leaf panel per pair, no grid reshape"
    (let [f (pj/arrange iris [[:a :b] [:c :d]])
          ps (panels f)]
      (is (= 2 (count ps)))
      (is (every? #(not (contains? % :poses)) ps)))))

(deftest multi-pair-grid-1xN-test
  (testing "G5: one :x column against three :y columns is a column of three panels"
    (let [f (pj/facet-matrix iris [:a] [:b :c :d])]
      (is (= [[{:x :a :y :b}] [{:x :a :y :c}] [{:x :a :y :d}]]
             (cell-mappings f))))))

;; ============================================================
;; Integration with pj/lay-* and resolve-tree (I1-I3)
;; ============================================================

(deftest integration-lay-resolve-test
  (testing "I1: root-origin layer reaches both panels through resolve-tree"
    (let [f (-> iris (pj/arrange [[:a :b] [:c :d]]) pj/lay-point)
          leaves (pose/resolve-tree f)]
      (is (= 2 (count leaves)))
      (is (every? (fn [leaf] (= 1 (count (:layers leaf)))) leaves)
          "each resolved leaf carries the root layer")))

  (testing "I2: panel-specific layer does not leak across panels"
    (let [f (-> iris
                (pj/arrange [[:a :b] [:c :d]])
                (pj/lay-point :a :b))
          leaves (pose/resolve-tree f)
          layers-per-leaf (mapv #(count (:layers %)) leaves)]
      (is (= [1 0] layers-per-leaf)
          "leaf 1 has the layer; leaf 2 does not")))

  (testing "I3: root aesthetic merges into both resolved leaves' mappings"
    (let [f (-> iris
                (pj/arrange [[:a :b] [:c :d]])
                (pj/pose {:color :species}))
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
    (is (no-empty-map-output? (pj/arrange iris [[:a :b] [:c :d]])))
    (is (no-empty-map-output? (-> iris (pj/pose :a :b) pj/lay-point)))
    (is (no-empty-map-output?
         (-> iris (pj/arrange [[:a :b] [:c :d]]) pj/lay-point)))))

(deftest poses-preserve-call-order-test
  (testing "Cells appear in the order pj/arrange is given them"
    (let [f (pj/arrange iris [[:a :b] [:c :d] [:e :f]])]
      (is (= [{:x :a :y :b} {:x :c :y :d} {:x :e :y :f}]
             (mapv :mapping (panels f)))))))

;; ============================================================
;; Layer columns vs pose columns
;; ============================================================
;;
;; When a leaf already carries columns and `pj/lay-*` is called with
;; others, the layer joins the leaf carrying its own columns, and the
;; layers of the leaf share its one panel. `lay-*` does not restructure
;; the pose.

(deftest leaf-layer-non-matching-columns-share-the-panel-test
  (testing "leaf with columns, lay-* with other columns -- one panel"
    (let [fr (-> iris
                 (pj/pose :a :b)
                 (pj/lay-point :c :d))]
      (is (nil? (:poses fr)) "lay-* does not restructure the pose")
      (is (= {:x :a :y :b} (:mapping fr)))
      (is (= [{:x :c :y :d}] (mapv :mapping (:layers fr))))
      (is (= 1 (:panels (pj/svg-summary fr))))))
  (testing "each layer keeps its own columns, on one panel"
    (let [fr (-> iris
                 (pj/lay-point :a :b)
                 (pj/lay-point :c :d))]
      (is (nil? (:poses fr)))
      (is (= [{:x :a :y :b} {:x :c :y :d}] (mapv :mapping (:layers fr))))
      (is (= 1 (:panels (pj/svg-summary fr))))))
  (testing "matching position passes -- redundant :mapping on the layer is fine"
    (let [f (-> iris
                (pj/pose :a :b)
                (pj/lay-point :a :b))]
      (is (= {:x :a :y :b} (:mapping f)))
      (is (= 1 (count (:layers f))))
      (is (= 1 (:panels (pj/svg-summary f))))))
  (testing "string vs keyword: distinct column references, both drawn"
    ;; Layer-level :data with string-named columns makes the safety
    ;; check pass: the new panel has columns "a" and "b" to map to.
    ;; Without it, the safety check correctly notes that strings
    ;; "a"/"b" do not match the iris dataset's keyword columns.
    (let [fr (-> iris
                 (pj/pose :a :b)
                 (pj/lay-point "a" "b"
                               {:data (tc/dataset {"a" [1 2] "b" [1 2]})}))]
      (is (nil? (:poses fr)))
      (is (= {:x :a :y :b} (:mapping fr)))
      (is (= [{:x "a" :y "b"}] (mapv :mapping (:layers fr))))
      (is (= 1 (:panels (pj/svg-summary fr)))))))

(deftest a-split-keeps-a-layers-own-mapping-test
  ;; A layer that names no place is stamped with the leaf's when the
  ;; first disagreement arrives, so it stays on the panel it was written
  ;; for. The stamp merges into the layer's mapping rather than replacing
  ;; it: a layer's aesthetics and its layer-type options share that slot,
  ;; and replacing it dropped every one of them in silence.
  (testing "an aesthetic written on the layer survives the split"
    (let [layer (-> iris
                    (pj/lay-point :a :b {:color "#377eb8"})
                    (pj/lay-point :c :d)
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
                    :layers
                    first)]
      (is (= 3 (:bins (:mapping layer))))))
  (testing "a layer that names its own position is left alone"
    (let [layer (-> iris
                    (pj/pose :a :b)
                    (pj/lay-point :a :b {:color "#377eb8"})
                    (pj/lay-point :c :d)
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
