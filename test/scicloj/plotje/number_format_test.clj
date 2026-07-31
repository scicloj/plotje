(ns scicloj.plotje.number-format-test
  "Digit grouping for large numbers via the `:thousands-separator`
   configuration key: numeric tick labels and the text a `:text`/`:label`
   mark takes from a column. Off by default -- grouping is wrong for a
   number that identifies rather than measures (a year would read 2,026).
   Part of https://github.com/scicloj/plotje/issues/19 (nicer labels)."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]))

(def ticket-data
  {:violation ["Meter Expired" "Over Time Limit" "Stop Prohibited"]
   :tickets [462389 181444 163294]})

(defn- plot-texts [opts]
  (set (:texts (pj/svg-summary (-> ticket-data
                                   (pj/lay-bar :tickets :violation)
                                   (pj/lay-label :tickets :violation {:text :tickets})
                                   (pj/options opts))))))

(defn- y-tick-labels [opts]
  (-> {:x [1 2 3] :y [1000000 2000000 3000000]}
      (pj/lay-point :x :y)
      (pj/options opts)
      pj/plan :panels first :y-ticks :labels))

;; ---- the pure helper ----

(deftest group-digits-handles-the-shapes-a-formatted-number-takes
  (testing "three-digit groups, counted from the right"
    (is (= "462,389" (defaults/group-digits "462389" ",")))
    (is (= "1,000,000" (defaults/group-digits "1000000" ",")))
    (is (= "999" (defaults/group-digits "999" ",")))
    (is (= "1,000" (defaults/group-digits "1000" ","))))
  (testing "a sign stays outside the grouping"
    (is (= "-1,234" (defaults/group-digits "-1234" ","))))
  (testing "only the integer part is grouped"
    (is (= "-1,234.56" (defaults/group-digits "-1234.56" ",")))
    (is (= "0.5" (defaults/group-digits "0.5" ","))))
  (testing "an exponent is left alone"
    (is (= "1.0E7" (defaults/group-digits "1.0E7" ","))))
  (testing "nil and the empty string mean no grouping"
    (is (= "1000" (defaults/group-digits "1000" nil)))
    (is (= "1000" (defaults/group-digits "1000" ""))))
  (testing "a space is a real separator, not an absent one"
    (is (= "462 389" (defaults/group-digits "462389" " "))))
  (testing "any string works, so other conventions do too"
    (is (= "462.389" (defaults/group-digits "462389" ".")))
    (is (= "462'389" (defaults/group-digits "462389" "'"))))
  (testing "a non-numeric string passes through"
    (is (= "Meter Expired" (defaults/group-digits "Meter Expired" ",")))))

(deftest fmt-value-label-groups-only-numbers
  (is (= "462,389" (defaults/fmt-value-label 462389 ",")))
  (is (= "462389" (defaults/fmt-value-label 462389 nil)))
  (testing "a string that looks numeric is still a category name"
    (is (= "462389" (defaults/fmt-value-label "462389" ","))))
  (testing "keywords and nil are unaffected"
    (is (= "setosa" (defaults/fmt-value-label :setosa ",")))
    (is (= "" (defaults/fmt-value-label nil ",")))))

;; ---- off by default ----

(deftest ungrouped-by-default
  (testing "numbers render as plain digit runs unless asked"
    (let [texts (plot-texts {})]
      (is (contains? texts "462389"))
      (is (not (contains? texts "462,389")))))
  (testing "tick labels too"
    (is (= ["1000000" "1200000" "1400000"] (take 3 (y-tick-labels {}))))))

;; ---- the option ----

(deftest separator-reaches-ticks-and-data-labels
  (testing "data label text is grouped"
    (is (contains? (plot-texts {:thousands-separator ","}) "462,389")))
  (testing "axis tick labels are grouped"
    (is (contains? (plot-texts {:thousands-separator ","}) "100,000"))
    (is (= ["1,000,000" "1,200,000" "1,400,000"]
           (take 3 (y-tick-labels {:thousands-separator ","})))))
  (testing "other conventions work"
    (is (contains? (plot-texts {:thousands-separator "."}) "462.389"))
    (is (contains? (plot-texts {:thousands-separator " "}) "462 389")))
  (testing "an empty separator is the same as none"
    (is (contains? (plot-texts {:thousands-separator ""}) "462389"))))

(deftest separator-is-settable-as-configuration
  (testing "the key is a documented configuration key, not only a plot option"
    (is (contains? (set (keys pj/config-key-docs)) :thousands-separator))
    (is (nil? (:thousands-separator (pj/config)))))
  (testing "pj/with-config sets it for every plot in scope"
    (is (contains? (pj/with-config {:thousands-separator ","}
                     (plot-texts {}))
                   "462,389"))))

;; ---- layout reserves room for the wider labels ----

(deftest grouped-labels-do-not-eat-into-the-panel
  (testing "the panel narrows by the width the separators add"
    (let [width (fn [opts] (-> {:x [1 2 3] :y [1000000 2000000 3000000]}
                               (pj/lay-point :x :y)
                               (pj/options opts)
                               pj/plan :panel-width))
          plain (width {})
          grouped (width {:thousands-separator ","})]
      (is (< grouped plain))
      ;; Two separators per label at 0.5 * the 11px tick font = 11px.
      (is (= 11.0 (- plain grouped))))))

;; ---- what it deliberately leaves alone ----

(deftest categories-and-legends-are-not-grouped
  (testing "a numeric category axis keeps its values whole -- these name
            things rather than measure them, and a year would read 2,026"
    (let [texts (set (:texts (pj/svg-summary
                              (-> {:year [2024 2025 2026] :n [1 2 3]}
                                  (pj/lay-bar :year :n {:x-type :categorical})
                                  (pj/options {:thousands-separator ","})))))]
      (is (contains? texts "2024"))
      (is (not (contains? texts "2,024"))))))
