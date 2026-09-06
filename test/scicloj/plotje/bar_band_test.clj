(ns scicloj.plotje.bar-band-test
  "A bar fills a fraction of its category band. Naming a colour column
   divided the band by the number of colour categories even where
   nothing asked for a dodge, which narrowed every bar and pushed it to
   one side. Reported on #plotje 2026-09-02, reproduced on 0.10.1.

   The first fix compared the colour column against `:x` alone, so a
   horizontal bar -- categories on `:y` -- stayed narrowed through
   0.11.0. A boxplot orients the same way and was narrowed with it.
   Found 2026-09-06 while answering the report."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def string-rows [{:g "a" :v 3} {:g "b" :v 5} {:g "c" :v 4}])

(def ^:private panel-background "rgb(232,232,232)")

(defn- svg-elements [pose tag]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= tag (first %)) (map? (second %))))
       (map second)))

(defn- band-fraction
  "How much of its category band the first bar fills. Taken as a
   fraction because a legend narrows the panel, so the drawn width
   alone does not compare across plots."
  [pose]
  (let [panel (->> (svg-elements pose :rect)
                   (filter #(= panel-background (:fill %)))
                   first)
        bar   (->> (svg-elements pose :polygon)
                   (map :points)
                   (map (fn [pts]
                          (let [xs (->> (str/split pts #"\s+")
                                        (map #(parse-double (first (str/split % #",")))))]
                            (- (apply max xs) (apply min xs)))))
                   first)]
    (/ bar (/ (double (:width panel)) 3.0))))

(deftest undodged-bars-fill-their-band-test
  (testing "a bar with no colour fills 0.8 of its band"
    (is (< 0.79 (band-fraction (pj/lay-bar string-rows :g :v)) 0.81)))
  (testing "naming a colour column does not narrow it"
    ;; Nothing asked for a dodge here, so the band is not subdivided.
    ;; The legend makes the panel narrower; the fraction is what holds.
    (is (< 0.79 (band-fraction (pj/lay-bar string-rows :g :v {:color :g})) 0.81)))
  (testing "an actual dodge still subdivides the band"
    (let [rows (mapv #(assoc % :h (str (:g %) "x")) string-rows)]
      (is (< 0.26 (band-fraction (pj/lay-bar rows :g :v {:color :h :position :dodge}))
             0.27)))))

(defn- band-fraction-h
  "How much of its category band the first mark fills, for a mark whose
   categories run down `:y`. The band is a share of the panel height."
  [pose]
  (let [panel (->> (svg-elements pose :rect)
                   (filter #(= panel-background (:fill %)))
                   first)
        mark  (->> (svg-elements pose :polygon)
                   (map :points)
                   (map (fn [pts]
                          (let [ys (->> (str/split pts #"\s+")
                                        (map #(parse-double (second (str/split % #",")))))]
                            (- (apply max ys) (apply min ys)))))
                   (apply max))]
    (/ mark (/ (double (:height panel)) 3.0))))

(deftest horizontal-bars-fill-their-band-test
  ;; `pj/lay-bar` orients by which axis is categorical, so the same
  ;; chart written with the categories on :y has to answer the same way.
  (testing "a horizontal bar with no colour fills 0.8 of its band"
    (is (< 0.79 (band-fraction-h (pj/lay-bar string-rows :v :g)) 0.81)))
  (testing "naming the category column as the colour does not narrow it"
    (is (< 0.79 (band-fraction-h (pj/lay-bar string-rows :v :g {:color :g})) 0.81)))
  (testing "an actual dodge still subdivides the band"
    (let [rows (mapv #(assoc % :h (str (:g %) "x")) string-rows)]
      (is (< 0.26 (band-fraction-h (pj/lay-bar rows :v :g {:color :h :position :dodge}))
             0.27)))))

(def box-rows
  (mapcat (fn [g vs] (map (fn [v] {:g g :v v}) vs))
          ["a" "b" "c"]
          [[1 2 3] [3 4 5] [5 6 7]]))

(deftest horizontal-boxplot-keeps-its-width-test
  ;; A boxplot reads the same default position, so it was narrowed by
  ;; the same comparison. Its own default width is 0.6 of the band.
  (testing "a horizontal box with no colour fills 0.6 of its band"
    (is (< 0.59 (band-fraction-h (pj/lay-boxplot box-rows :v :g)) 0.61)))
  (testing "naming the category column as the colour does not narrow it"
    (is (< 0.59 (band-fraction-h (pj/lay-boxplot box-rows :v :g {:color :g})) 0.61))))
