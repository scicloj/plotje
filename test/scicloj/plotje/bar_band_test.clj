(ns scicloj.plotje.bar-band-test
  "A bar fills a fraction of its category band. Naming a colour column
   divided the band by the number of colour categories even where
   nothing asked for a dodge, which narrowed every bar and pushed it to
   one side. Reported on #plotje 2026-09-02, reproduced on 0.10.1."
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
