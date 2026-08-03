(ns scicloj.plotje.markup-in-text-test
  "Text a plot takes from its data or its options can be made of the same
   characters as markup: a category named R&D, a title reading
   Q1 <profit> & loss. Both have to reach the reader as those characters
   rather than as markup, on either path out of `pj/plot`.

   The two paths escape in different places. A saved file is serialized
   here, and `hiccup->svg-str` runs every string and every attribute
   value through `escape-xml`. The notebook path serializes nothing of
   its own: it hands the hiccup to Clay under a Kindly kind, and the kind
   is what decides whether Clay escapes. `:kind/hiccup` did not, so R&D
   reached the page as markup; `:kind/hiccup2` is documented as
   defaulting to string escaping, and is what `pj/plot` now asks for
   (PR #33). Nothing else pinned that choice, so a revert to the kind
   that does not escape would pass the rest of the suite."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is]]
            [clojure.walk :as walk]
            [scicloj.plotje.api :as pj]))

(def teams
  {:team ["R&D" "Sales <EU>"]
   :headcount [3 5]})

(defn- bars [opts]
  (-> teams
      (pj/pose :team :headcount)
      (pj/lay-bar)
      (pj/options (merge {:title "Q1 <profit> & loss" :width 400 :height 300}
                         opts))))

(defn- markup-strings
  "Every distinct string in `tree` holding a character markup is made of."
  [tree]
  (let [found (atom [])]
    (walk/postwalk (fn [x]
                     (when (and (string? x) (re-find #"[&<>]" x))
                       (swap! found conj x))
                     x)
                   tree)
    (distinct @found)))

(deftest the-notebook-path-asks-for-a-kind-that-escapes
  (testing "pj/plot hands Clay the kind that escapes the strings it renders"
    (is (= :kind/hiccup2 (:kindly/kind (meta (pj/plot (bars {}))))))))

(deftest an-interactive-plot-asks-for-the-same-kind
  (testing "the tooltip wrapper carries the kind, not only the bare svg"
    (is (= :kind/hiccup2
           (:kindly/kind (meta (pj/plot (bars {:tooltip true}))))))))

(deftest the-hiccup-carries-the-text-as-written
  (testing "escaping belongs to whoever renders, so the tree holds the original"
    (is (= #{"Q1 <profit> & loss" "R&D" "Sales <EU>"}
           (set (markup-strings (pj/plot (bars {}))))))))

(deftest a-saved-svg-escapes-the-text
  (testing "this path writes the file itself, so it escapes for itself"
    (let [path "/tmp/_plotje_markup_in_text.svg"
          svg (do (pj/save (bars {}) path)
                  (slurp path))]
      (is (str/includes? svg "R&amp;D"))
      (is (str/includes? svg "&lt;EU&gt;"))
      (is (not (str/includes? svg "R&D")))
      (is (not (str/includes? svg "Sales <EU>"))))))
