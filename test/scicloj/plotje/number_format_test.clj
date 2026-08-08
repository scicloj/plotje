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
  (is (= "462,389" (defaults/fmt-value-label 462389 {:thousands ","})))
  (is (= "462389" (defaults/fmt-value-label 462389 nil)))
  (testing "a string that looks numeric is still a category name"
    (is (= "462389" (defaults/fmt-value-label "462389" {:thousands ","}))))
  (testing "keywords and nil are unaffected"
    (is (= "setosa" (defaults/fmt-value-label :setosa {:thousands ","})))
    (is (= "" (defaults/fmt-value-label nil {:thousands ","})))))

(deftest fmt-number-writes-both-separators
  (testing "the decimal point becomes whatever :decimal names"
    (is (= "1234,5" (defaults/fmt-number "1234.5" {:decimal ","})))
    (is (= "0,5" (defaults/fmt-number "0.5" {:decimal ","})))
    (is (= "-1234,56" (defaults/fmt-number "-1234.56" {:decimal ","}))))
  (testing "the two are chosen independently, so European reads as European"
    (is (= "1.234,5" (defaults/fmt-number "1234.5" {:thousands "." :decimal ","})))
    (is (= "1 234,5" (defaults/fmt-number "1234.5" {:thousands " " :decimal ","})))
    (is (= "1,234.5" (defaults/fmt-number "1234.5" {:thousands "," :decimal "."}))))
  (testing "an exponent keeps its own point out of it"
    (is (= "1,0E7" (defaults/fmt-number "1.0E7" {:decimal ","}))))
  (testing "a whole number has no point to replace"
    (is (= "1.234" (defaults/fmt-number "1234" {:thousands "." :decimal ","}))))
  (testing "neither separator leaves the string exactly as it was"
    (is (= "1234.5" (defaults/fmt-number "1234.5" {})))
    (is (= "1234.5" (defaults/fmt-number "1234.5" {:thousands nil :decimal ""}))))
  (testing "a non-numeric string passes through either way"
    (is (= "Meter Expired"
           (defaults/fmt-number "Meter Expired" {:thousands "." :decimal ","})))))

(deftest a-decimal-separator-reaches-the-axis
  (testing "the option a German reader would set, end to end"
    (let [labels (-> {:height [1.0 1.5 2.0] :weight [1000.5 1500.25 2000.75]}
                     (pj/lay-point :height :weight)
                     (pj/options {:thousands-separator "." :decimal-separator ","})
                     pj/plan :panels first :x-ticks :labels)]
      (is (every? #(not (re-find #"\." %)) labels)
          "no point survives on an axis that asked for a comma")
      (is (some #(re-find #"," %) labels)
          "and the comma is what it reads instead"))))

;; ---- what a plot draws does not depend on the JVM it draws on ----

(deftest tick-labels-do-not-follow-the-default-locale
  (testing "a comma-decimal locale does not leak into the label text"
    ;; clojure.core/format reads Locale/getDefault; every formatting site
    ;; on the label path is pinned to Locale/ROOT instead, so the glyph a
    ;; plot draws for the point is chosen with :decimal-separator rather
    ;; than inherited from the machine.
    (let [ticks (fn [] (-> {:height [1.0 1.5 2.0 2.5 3.0] :weight [1 2 3 4 5]}
                           (pj/lay-point :height :weight)
                           pj/plan :panels first :x-ticks :labels))
          before (java.util.Locale/getDefault)]
      (try
        (java.util.Locale/setDefault (java.util.Locale/forLanguageTag "de-DE"))
        (let [german (ticks)]
          (java.util.Locale/setDefault (java.util.Locale/forLanguageTag "en-US"))
          (is (= (ticks) german)
              "the same data reads the same on a de-DE and an en-US JVM")
          (is (every? #(not (re-find #"," %)) german)
              "and reads with a point, which is what ROOT formats with"))
        (finally (java.util.Locale/setDefault before))))))

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
