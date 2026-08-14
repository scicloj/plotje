(ns scicloj.plotje.impl.aesthetic-registry-test
  "Tests that `defaults/aesthetic-registry` stays the single table the
   per-aesthetic sets come from.

   Before it was consolidated there were six lists of aesthetics in
   five namespaces, and they disagreed: one carried a `:y-end` that no
   mark read, another was missing the band bounds the glossary counts
   as positional. The point of these tests is that adding an aesthetic
   half-described, or growing a second list beside this one, fails the
   suite."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.layer-type :as layer-type]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.resolve :as resolve]))

(def required-properties
  "Every entry answers all of these. A new aesthetic that leaves one
   out fails here rather than falling through a derived set in
   silence."
  [:category :column? :value? :numeric? :categorical-column? :scale-default])

(deftest every-entry-is-fully-described-test
  (testing "each aesthetic answers every property"
    (doseq [[k entry] defaults/aesthetic-registry
            p required-properties]
      (is (contains? entry p)
          (str k " does not say " p))))

  (testing "the properties take the values the derived sets read"
    (doseq [[k {:keys [category scale-default]}] defaults/aesthetic-registry]
      (is (contains? #{:positional :appearance :grouping} category)
          (str k " has an unknown category"))
      (is (contains? #{:always :by-source :by-value :never nil} scale-default)
          (str k " has an unknown scale default"))))

  (testing "every aesthetic accepts a column, a value, or both"
    ;; An entry that accepts neither could never be written down.
    (doseq [[k {:keys [column? value?]}] defaults/aesthetic-registry]
      (is (or column? value?)
          (str k " accepts neither a column nor a value"))))

  (testing "an aesthetic with no scale takes no scale default"
    ;; `:group` splits the data and draws nothing, so `pj/scale` refuses
    ;; it -- there is no scale for a default to describe.
    (doseq [[k {:keys [scale-key scale-default]}] defaults/aesthetic-registry]
      (when (nil? scale-default)
        (is (nil? scale-key)
            (str k " has a scale but no default for it"))))))

(deftest derived-sets-test
  (testing "the sets other namespaces read are the ones they expect"
    (is (= #{:x :y :x-end :y-min :y-max :color :size :alpha :fill :shape :text :group}
           defaults/column-keys))
    (is (= [:x :x-end :y]
           (vec (sort-by str resolve/positional-aesthetics)))
        "only these three have a literal turned into a column")
    (is (= [:alpha :fill :size] defaults/continuous-column-aesthetics))
    (is (= [:fill :group :shape] defaults/column-only-aesthetics))
    (is (= #{:color :size :alpha :shape} defaults/legend-bearing-aesthetics))
    (is (= {:x :x-scale :y :y-scale :size :size-scale :alpha :alpha-scale
            :fill :fill-scale :color :color-scale :shape :shape-scale}
           defaults/channel->scale-key))))

(deftest the-narrower-sets-are-subsets-test
  (testing "an aesthetic whose literal becomes a column is positional"
    (is (every? (set defaults/positional-aesthetics)
                defaults/literal-to-column-aesthetics)))

  (testing "an aesthetic with a scale, a legend, or a column reading is one Plotje knows"
    (doseq [k (concat (keys defaults/channel->scale-key)
                      defaults/legend-bearing-aesthetics
                      defaults/column-keys)]
      (is (contains? defaults/aesthetic-registry k)
          (str k " is used as an aesthetic but has no registry entry")))))

(deftest every-aesthetic-is-documented-as-a-layer-option-test
  (testing "the user-facing option docs cover every aesthetic"
    ;; `layer-type/layer-option-docs` is what the book renders. An
    ;; aesthetic missing from it is an aesthetic no reader can look up.
    (doseq [k (keys defaults/aesthetic-registry)]
      (is (contains? layer-type/layer-option-docs k)
          (str k " has no entry in layer-option-docs")))))
