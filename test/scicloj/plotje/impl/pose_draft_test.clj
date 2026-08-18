(ns scicloj.plotje.impl.pose-draft-test
  "Specification for pose/leaf->draft. Pins the shape that plan.clj
   and the compositor depend on: one draft entry per applicable layer,
   merged mapping (pose < layer-type-info < layer), layer-level
   :stat / :position / :mark as sibling keys, plot-level
   :x-scale / :y-scale / :coord stamped from :opts, and -- when
   :opts carry :facet-col / :facet-row -- one entry per facet variant
   with filtered :data and string facet labels.

   Also pins edge case B (leaf has no :x/:y in its own :mapping but
   layers carry :x/:y) -- the pose-native path emits one entry per
   layer with :x/:y sourced from the layer, which is the behavior a
   user would expect of the pose model."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.impl.pose :as pose]))

(def iris
  (tc/dataset {:a [1.0 2.0 3.0]
               :b [0.5 1.0 1.5]
               :species ["setosa" "setosa" "versicolor"]}))

;; ============================================================
;; Leaf with position + layer shapes
;; ============================================================

(deftest leaf-with-position-single-layer-test
  (testing "leaf :x/:y + one bare layer -- one draft, mark from the layer type"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :mapping {:x :a :y :b}
                  :layers [{:layer-type :point :mapping {}}]})]
      (is (= 1 (count draft)))
      (is (= :a (:x (first draft))))
      (is (= :b (:y (first draft))))
      (is (= :point (:mark (first draft)))))))

(deftest pose-aesthetic-flows-into-layer-test
  (testing "pose-level :color flows into the draft entry"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :mapping {:x :a :y :b :color :species}
                  :layers [{:layer-type :point :mapping {}}]})]
      (is (= :species (:color (first draft)))))))

(deftest layer-overrides-pose-mapping-test
  (testing "layer mapping wins on shared keys"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :mapping {:x :a :y :b :color :species}
                  :layers [{:layer-type :point :mapping {:color :a}}]})]
      (is (= :a (:color (first draft)))))))

(deftest multiple-layers-yield-multiple-drafts-test
  (testing "each layer becomes one draft entry"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :mapping {:x :a :y :b}
                  :layers [{:layer-type :point :mapping {}}
                           {:layer-type :line  :mapping {}}]})]
      (is (= 2 (count draft)))
      (is (= #{:point :line} (into #{} (map :mark draft)))))))

(deftest structural-keys-carry-through-test
  (testing ":stat / :position / :mark siblings on a layer carry through"
    (is (= :linear-model
           (:stat (first (pose/leaf->draft
                          {:data iris
                           :mapping {:x :a :y :b}
                           :layers [{:layer-type :smooth :mapping {} :stat :linear-model}]})))))
    (is (= :dodge
           (:position (first (pose/leaf->draft
                              {:data iris
                               :mapping {:x :a :y :b}
                               :layers [{:layer-type :bar :mapping {} :position :dodge}]})))))))

(deftest empty-layers-yield-infer-placeholder-test
  (testing "no layers -> one {:mark :infer ...} draft entry"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :mapping {:x :a :y :b}
                  :layers []})]
      (is (= 1 (count draft))))))

(deftest no-mapping-no-layers-yields-empty-draft-test
  (testing "a bare leaf with data only produces no draft"
    (is (empty? (pose/leaf->draft {:data iris :layers []}))
        "nothing to infer from -- let plan produce a minimal placeholder.")))

(deftest plot-level-opts-stamped-on-every-entry-test
  (testing "the mapping's scales and the leaf's :coord stamp on each draft entry"
    ;; A scale lives with the mapping it reads -- `pj/scale` writes one
    ;; there too -- while `:coord` is still a leaf option.
    (let [[e] (pose/leaf->draft
               {:data iris
                :mapping {:x {:from :a :scale {:type :log}}
                          :y {:from :b :scale {:type :linear}}}
                :layers [{:layer-type :point :mapping {}}]
                :opts   {:coord :flip}})]
      (is (= {:type :log} (:x-scale e)))
      (is (= {:type :linear} (:y-scale e)))
      (is (= :flip (:coord e))))))

;; ============================================================
;; Edge case B: layers carry position, pose :mapping does not
;; ============================================================

(deftest edge-case-b-layer-carries-position-test
  (testing "leaf has no :x/:y; layer has :x/:y -- one draft entry per layer"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :layers [{:layer-type :point :mapping {:x :a :y :b}}]})]
      (is (= 1 (count draft)))
      (is (= :a (:x (first draft))))
      (is (= :b (:y (first draft))))
      (is (= :point (:mark (first draft)))))))

(deftest edge-case-b-multiple-layers-test
  (testing "two layers both carrying position -- two draft entries"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :layers [{:layer-type :point :mapping {:x :a :y :b}}
                           {:layer-type :line  :mapping {:x :a :y :b}}]})]
      (is (= 2 (count draft)))
      (is (every? #(= :a (:x %)) draft))
      (is (every? #(= :b (:y %)) draft)))))

;; ============================================================
;; Facet expansion
;; ============================================================

(deftest facet-col-expands-draft-test
  (testing ":opts {:facet-col col} multiplies the draft by distinct values"
    (let [draft (pose/leaf->draft
                 {:data iris
                  :mapping {:x :a :y :b}
                  :layers [{:layer-type :point :mapping {}}]
                  :opts   {:facet-col :species}})]
      (is (= 2 (count draft)) "two distinct species in the test dataset")
      (is (every? :facet-col draft))
      (is (every? #(string? (:facet-col %)) draft))
      (is (apply distinct? (map :__panel-idx draft))
          "each variant gets its own :__panel-idx so plan groups them as separate panels"))))

(deftest facet-row-and-col-expands-product-test
  (testing ":opts {:facet-col c :facet-row r} multiplies by the combined values"
    (let [ds (tc/dataset {:x [1 2 3 4]
                          :y [1 2 3 4]
                          :c ["A" "B" "A" "B"]
                          :r ["X" "X" "Y" "Y"]})
          draft (pose/leaf->draft
                 {:data ds
                  :mapping {:x :x :y :y}
                  :layers [{:layer-type :point :mapping {}}]
                  :opts   {:facet-col :c :facet-row :r}})]
      (is (= 4 (count draft)))
      (is (every? :facet-col draft))
      (is (every? :facet-row draft)))))
