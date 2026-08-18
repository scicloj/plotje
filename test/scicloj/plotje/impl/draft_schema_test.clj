(ns scicloj.plotje.impl.draft-schema-test
  "Tests for the draft Malli schemas -- in particular that a draft
   built through the public API conforms to the shape the schema
   claims for it."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.draft-schema :as ds]))

(def data
  {:num [1.0 2.0 3.0 4.0 5.0 6.0]
   :num2 [2.0 4.0 3.0 5.0 4.0 6.0]
   :cat ["p" "q" "p" "q" "p" "q"]
   :lab ["a" "b" "c" "d" "e" "f"]})

(deftest drafts-built-through-the-api-are-valid-test
  (testing "every draft shape the pipeline emits conforms"
    (doseq [[label pose]
            {"leaf"         (pj/lay-point data :num :num2)
             "aesthetics"   (pj/lay-point data :num :num2 {:color :cat :size 4})
             "no stat"      (pj/lay-bar data :cat)
             "x-only"       (pj/lay-histogram data :num)
             "annotation"   (-> (pj/lay-point data :num :num2)
                                (pj/lay-text {:x 3 :y 3 :text "note"}))
             "data-less"    (-> (pj/pose) (pj/lay-band-h {:y-min 2 :y-max 4}))
             "inferred"     {:num [1.0 2.0] :num2 [3.0 4.0]}
             "facet"        (-> (pj/lay-point data :num :num2) (pj/facet :cat))
             "scaled"       (-> (pj/lay-point data :num :num2)
                                (pj/scale :x :log)
                                (pj/coord :flip))
             "gradient"     (-> (pj/lay-point data :num :num2 {:color :num2})
                                (pj/options {:color-range :inferno}))
             "composite"    (pj/arrange [(pj/lay-point data :num :num2)
                                         (pj/lay-line data :num :num2)])}]
      (let [draft (pj/draft pose)]
        (is (ds/valid? draft) (str label ": " (pr-str (ds/explain draft))))))))

(deftest positional-aesthetics-name-columns-test
  (testing "a literal :x becomes a column, so the draft names columns only"
    ;; `resolve-positional-values` broadcasts a literal value over the
    ;; layer's data, or builds a one-row dataset when neither `:x` nor
    ;; `:y` names a column. Either way the draft carries one shape.
    (let [layers (-> (pj/lay-point data :num :num2)
                     (pj/lay-text {:x 3 :y 3 :text "note"})
                     pj/draft
                     :layers)]
      (is (every? ds/layer-valid? layers))
      (is (= {:x :x :y :y :text :text}
             (-> layers second (select-keys [:x :y :text]))))))

  (testing "an integer column name counts as naming a column"
    (let [layers (-> (pj/lay-point {0 [1.0 2.0] 1 [3.0 4.0]} {:x 1 :y 0})
                     pj/draft
                     :layers)]
      (is (every? ds/layer-valid? layers))
      (is (= {:x 1 :y 0} (-> layers first (select-keys [:x :y]))))))

  (testing "with no data anywhere a literal value stays one"
    ;; The one exception to the invariant: there is no dataset to add a
    ;; constant column to, so `:y` keeps its number and resolves to
    ;; nothing downstream, as the column reference beside it does.
    (let [layer (-> (pj/pose) (pj/lay-point {:x :num :y 5}) pj/draft :layers first)]
      (is (nil? (:data layer)))
      (is (= 5 (:y layer)))
      (is (ds/layer-valid? layer))))

  (testing "a literal value where the layer has data is rejected"
    (is (not (ds/layer-valid? {:data (tc/dataset data) :__panel-idx 0 :x 5})))))

(deftest layer-structural-nils-test
  (testing "a layer type registered without a stat drafts an explicit nil"
    ;; A registry entry is a `LayerType` record, so `:stat` exists as a
    ;; field whether or not the entry set it.
    (let [layer (-> (pj/lay-bar data :cat) pj/draft :layers first)]
      (is (contains? layer :stat))
      (is (nil? (:stat layer)))
      (is (ds/layer-valid? layer)))))

(deftest the-color-scale-spec-has-a-key-of-its-own-test
  ;; The spec and the gradient shared one key until 0.9.0, and
  ;; whichever was written second silently discarded the other -- so a
  ;; viridis gradient and a log scale could not be asked for together.
  ;; The gradient is now the colour scale's own `:range`.
  (testing "the spec travels on the layer, under its own key"
    (is (= {:type :log}
           (-> (pj/lay-point data :num :num2 {:color :num2})
               (pj/scale :color :log)
               pj/draft :layers first :color-scale-spec))))

  (testing "a gradient written as a plot option does not travel on the layer"
    ;; It is the outer scope of the same setting, resolved from the
    ;; configuration where the marks and the legend read it.
    (let [layer (-> (pj/lay-point data :num :num2 {:color :num2})
                    (pj/options {:color-range :inferno})
                    pj/draft :layers first)]
      (is (nil? (:color-scale-spec layer)))
      (is (ds/layer-valid? layer))))

  (testing "and written as a spec key it travels with the rest of the spec"
    (let [layer (-> (pj/lay-point data :num :num2 {:color :num2})
                    (pj/scale :color {:type :log :range :inferno})
                    pj/draft :layers first)]
      (is (= {:type :log :range :inferno} (:color-scale-spec layer)))
      (is (ds/layer-valid? layer))))

  (testing "so both can be asked for at once, in either order"
    (let [stops (fn [pose] (->> pose pj/plan :legend :stops (mapv :color)))
          colors (fn [pose] (-> pose pj/plan :panels first :layers first
                                :groups first :colors vec))
          gradient-then-scale (-> (pj/lay-point data :num :num2 {:color :num2})
                                  (pj/options {:color-range :inferno})
                                  (pj/scale :color :log))
          scale-then-gradient (-> (pj/lay-point data :num :num2 {:color :num2})
                                  (pj/scale :color :log)
                                  (pj/options {:color-range :inferno}))
          plain (-> (pj/lay-point data :num :num2 {:color :num2})
                    (pj/options {:color-range :inferno}))]
      (is (= (stops gradient-then-scale) (stops scale-then-gradient))
          "the order does not decide which survives")
      (is (= (stops plain) (stops gradient-then-scale))
          "the gradient is the one asked for")
      (is (not= (colors plain) (colors gradient-then-scale))
          "and the log scale still spaces the marks"))))
