(ns scicloj.plotje.missing-group-test
  "A row with a missing value in a grouping column -- a categorical
   `:color`, or `:group` -- is removed and reported by every layer type,
   whatever its stat."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]))

(def rows
  {:v [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16]
   :w [1 4 9 16 25 36 49 64 81 100 121 144 169 196 225 256]
   :c ["p" "q" "p" "q" "p" "q" "p" "q" "p" "q" "p" "q" "p" "q" "p" "q"]
   :g ["a" "a" nil "b" "b" nil "a" "b" "a" "b" nil "b" "a" nil "a" "b"]})

(defn- drawn
  "The colours drawn and the text printed while planning and rendering."
  [pose]
  (let [summary (atom nil)
        printed (with-out-str (reset! summary (pj/svg-summary pose)))]
    {:colors (:colors (deref summary)) :printed printed}))

(def default-colour "rgb(51,51,51)")

(deftest a-missing-colour-is-removed-by-every-stat
  (doseq [[nm pose] [["bin" (pj/lay-histogram rows :v {:color :g})]
                     ["density" (pj/lay-density rows :v {:color :g})]
                     ["loess" (pj/lay-smooth rows :v :w {:color :g})]
                     ["linear-model" (pj/lay-smooth rows :v :w {:color :g :stat :linear-model})]
                     ["summary" (pj/lay-summary rows :c :w {:color :g})]
                     ["count" (pj/lay-bar rows :c {:color :g})]
                     ["identity" (pj/lay-point rows :v :w {:color :g})]]]
    (testing nm
      (let [{:keys [colors printed]} (drawn pose)]
        (is (not (contains? (set colors) default-colour))
            "no group is drawn in the default colour")
        (is (str/includes? printed "Removed 4 rows with a missing category in :g"))))))

(deftest a-missing-group-is-removed-by-every-stat
  (doseq [[nm pose] [["bin" (pj/lay-histogram rows :v {:group :g})]
                     ["density" (pj/lay-density rows :v {:group :g})]
                     ["count" (pj/lay-bar rows :c {:group :g})]
                     ["identity" (pj/lay-line rows :v :w {:group :g})]]]
    (testing nm
      (is (str/includes? (:printed (drawn pose))
                         "Removed 4 rows with a missing category in :g"))))
  (testing "a line per group that has a value, and no line for the rows without"
    (is (= 2 (:lines (pj/svg-summary (pj/lay-line rows :v :w {:group :g})))))))
