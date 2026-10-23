(ns scicloj.plotje.invariants-test
  "Eight invariants over a thousand generated poses, with a fixed seed.

   The poses come from `scicloj.plotje.invariants-gen`: six datasets
   (among them one with missing values, one with an all-nil column and a
   grouped dataset), leaves of zero to three layers, facets by category
   and over columns, arrangements, a cross matrix, `:share-scales`,
   `:coord :flip`, and unknown keys. Each test checks one invariant on
   every pose and reports the shortest failing forms, which can be pasted
   into a REPL with the datasets of `invariants-gen` in scope.

   A violation that a known defect explains -- one waiting for a
   decision -- is listed in `known-defects` and printed, not failed. When
   that defect is fixed its count drops to zero; remove its entry then."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [scicloj.plotje.invariants-gen :as gen]))

(def seed
  "Seed 1 generates the poses of the research runs `base-s1` and
   `patched-s1` (their first thousand)."
  1)

(def pose-count 1000)

(def results (delay (gen/run seed pose-count)))

;; ---------------------------------------------------------------- known defects

(defn- all-steps [rec] (concat (:steps rec) (mapcat all-steps (:cells rec))))

(defn- grouped-dataset? [{:keys [rec]}]
  (or (= :grouped (:ds rec)) (some #(= :grouped (:ds %)) (:cells rec))))

(defn- no-layer-anywhere? [{:keys [rec]}]
  (not-any? (fn [[f]] (str/starts-with? (name f) "lay-")) (all-steps rec)))

(def known-defects
  "Invariant to a list of known defects, each a name and a predicate on a
   result. A violation matching one is printed and does not fail."
  {:no-raw-exception
   [{:defect "a grouped dataset fails inside Tablecloth (defects-stats-and-data 11, question 6)"
     :match? grouped-dataset?}]
   :no-silent-nothing
   [{:defect "a grouped dataset under a facet draws a blank figure (defects-stats-and-data 11, question 6)"
     :match? grouped-dataset?}
    {:defect "a pose with no layer on data of four or more columns draws a blank figure (defects-layers-and-columns 8)"
     :match? no-layer-anywhere?}]})

;; ---------------------------------------------------------------- reporting

(defn- explained-by [k result]
  (some #(when ((:match? %) result) (:defect %)) (get known-defects k)))

(defn- shortest-forms
  "The `n` shortest distinct forms among `results`, each with its violation."
  [k results n]
  (->> results
       (map (fn [r] [(pr-str (:form r)) (get-in r [:violations k])]))
       (sort-by (comp count first))
       (map first)
       distinct
       (take n)
       (map (fn [form] (str "  " form "\n    " (pr-str (some #(when (= form (pr-str (:form %)))
                                                                (get-in % [:violations k]))
                                                             results)))))
       (str/join "\n")))

(defn- check-invariant [k]
  (let [violating (filter #(get-in % [:violations k]) (deref results))
        {known true unexplained false} (group-by #(boolean (explained-by k %)) violating)]
    (doseq [[defect rs] (group-by #(explained-by k %) known)]
      (println (format "invariants-test %s: %d poses, known defect: %s" (name k) (count rs) defect)))
    ;; A count, so that a failure prints the forms below and not the
    ;; outcomes of every violating pose.
    (is (= 0 (count unexplained))
        (str (count unexplained) " poses violate " (name k) "; the shortest:\n"
             (shortest-forms k unexplained 5)))))

;; ---------------------------------------------------------------- tests

(deftest no-raw-exception-test
  (testing "every error is an ex-info thrown from a Plotje namespace"
    (check-invariant :no-raw-exception)))

(deftest no-silent-nothing-test
  (testing "a pose that draws draws marks, a \"no data\" panel, or prints a message"
    (check-invariant :no-silent-nothing)))

(deftest shared-axes-agree-test
  (testing "panels sharing an axis and drawing one column on it have one domain"
    (check-invariant :shared-axes-agree)))

(deftest unread-key-reported-test
  (testing "an unknown key in a mapping, in pj/options, a facet or pj/arrange is named"
    (check-invariant :unread-key-reported)))

(deftest facet-equals-cells-test
  (testing "a facet by category draws what its cells listed with :where draw"
    (check-invariant :facet-equals-cells)))

(deftest arrange-one-cell-test
  (testing "an arrangement of one cell draws what the cell draws"
    (check-invariant :arrange-one-cell)))

(deftest flip-swaps-domains-test
  (testing ":coord :flip swaps the domains and keeps the marks"
    (check-invariant :flip-swaps-domains)))

(deftest computed-axis-title-test
  (testing "a count or density axis is not titled by a column of the data"
    (check-invariant :computed-axis-title)))

(deftest the-generator-reaches-every-invariant-test
  (testing "the seed generates poses that draw, poses that report an error, and each wrapper"
    (let [rs (deref results)]
      (is (< 500 (count (filter #(= :ok (get-in % [:outcome :status])) rs))))
      (is (< 100 (count (filter #(= :error (get-in % [:outcome :status])) rs))))
      (is (= #{:none :facet-cat :facet-cols :arrange-cols :facet-matrix :arrange-poses :arrange-one}
             (set (map (comp :wrapper :rec) rs)))))))
