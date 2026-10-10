(ns scicloj.plotje.overlay-test
  "The layers of a pose share one panel: a layer naming columns the
   panel does not draw is drawn on it, against the axes it has. (The
   `pj/overlay` that asked for this is retired -- see
   `shared-panel-test`.)"
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def wealth
  [{:cohort "a" :growth 1.0 :tax 0.3}
   {:cohort "b" :growth 2.0 :tax 0.9}])

(def weather
  {:day [1 2 3] :reading [10 12 11] :humidity [40 55 50] :other [7 8 9]})

(defn- panels [pose]
  (:panels (pj/svg-summary pose)))

(deftest layers-share-a-leaf-panel-test
  (testing "a second layer naming another x joins the panel"
    (is (= 1 (panels (-> wealth
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort)))))))

(deftest overlay-keeps-the-layers-columns-test
  (testing "the joined layer carries its own columns, and both are drawn"
    (let [pose (-> wealth
                   (pj/lay-bar :growth :cohort)
                   (pj/lay-bar :tax :cohort))]
      (is (= 1 (panels pose)))
      ;; Two bars per cohort: one from each layer, on one pair of axes.
      (is (= 4 (:polygons (pj/svg-summary pose))))
      (is (= {:x :tax :y :cohort}
             (:mapping (second (:layers pose)))))))
  (testing "the axis covers every column drawn on it, and names them all"
    ;; reading spans 10-12 and humidity 40-55; the shared axis is
    ;; ticked over both. It used to be named for the column the panel
    ;; already had, so humidity was drawn and never named anywhere --
    ;; in the axis title, in a legend, or in a colour of its own.
    (let [texts (->> (tree-seq vector? seq
                               (pj/plot (-> weather
                                            (pj/lay-line :day :reading)
                                            (pj/lay-line :day :humidity))
                                        {:format :svg}))
                     (filter #(and (vector? %) (= :text (first %))))
                     (map last)
                     (filter string?)
                     set)]
      (is (contains? texts "55"))
      (is (contains? texts "reading, humidity")
          "the axis names both columns it draws")
      (is (contains? texts "humidity")
          "and the legend names the layer drawing it"))))

(deftest an-overlay-says-how-its-marks-are-told-apart-test
  ;; Overlaid layers that disagree about a column used to be drawn in
  ;; one colour, with nothing naming the second: the axis was titled
  ;; after whichever layer came first, there was no legend, and the
  ;; picture showed less than was asked for without saying so. The
  ;; ceremony was attached to the better outcome -- a series drew the
  ;; readable picture and an overlay the unreadable one.
  (let [d {:quarter [1 2 3 4] :revenue [10 12 14 13] :cost [7 8 9 8]}
        texts (fn [fr] (->> (pj/plot fr) (tree-seq vector? seq)
                            (filter string?) set))
        overlaid (-> d
                     (pj/lay-point :quarter :revenue)
                     (pj/lay-point :quarter :cost))]

    (testing "each layer earns a palette colour and a legend entry"
      (let [t (texts overlaid)]
        (is (contains? t "revenue"))
        (is (contains? t "cost")))
      (is (= 2 (count (filter #(re-find #"^rgb" %)
                              (:colors (pj/svg-summary overlaid)))))))

    (testing "the axis names every column it draws"
      (is (= "revenue, cost" (:y-label (pj/plan overlaid)))))

    (testing "the legend takes no title of its own, the entries naming the columns"
      (is (= "" (:title (:legend (pj/plan overlaid))))))

    (testing "a title the writer sets is left alone"
      (is (= "Measure" (:title (:legend (pj/plan (pj/scale overlaid :color
                                                           {:label "Measure"})))))))

    (testing "a writer who has coloured the layers is left alone"
      ;; Nothing is synthesized, and the axis keeps the single name.
      (let [own (-> d
                    (pj/lay-point :quarter :revenue {:color "#377eb8"})
                    (pj/lay-point :quarter :cost {:color "#e6550d"}))]
        (is (= "revenue" (:y-label (pj/plan own))))))

    (testing "layers drawing one place are untouched"
      (let [same (-> d (pj/lay-point :quarter :revenue)
                     (pj/lay-line :quarter :revenue))]
        (is (nil? (:legend (pj/plan same))))
        (is (= "revenue" (:y-label (pj/plan same))))))

    (testing "an annotation placed in drawing space does not rename the axis"
      (let [annotated (-> d (pj/lay-point :quarter :revenue)
                          (pj/lay-text {:x 10 :y 10 :text "n" :in :drawing-area}))]
        (is (= "quarter" (:x-label (pj/plan annotated))))))))

(deftest a-layer-on-a-composite-test
  ;; A composite's cells are separate poses; decision 3c (a layer goes
  ;; to every cell naming its columns) is not built.
  (testing "a miss appends a panel"
    (let [base (pj/arrange weather [[:day :reading] [:day :humidity]])]
      (is (= 3 (panels (pj/lay-point base :day :other))))))
  (testing "a hit lands on the matching leaf"
    (let [base (pj/arrange weather [[:day :reading] [:day :humidity]])]
      (is (= 2 (panels (pj/lay-point base :day :humidity)))))))

(deftest layers-are-told-apart-however-they-were-added-test
  (let [d {:t [1 2 3 4] :a [1 3 2 4] :b [2 2 5 1]}
        seen (fn [p] (let [s (pj/svg-summary p)]
                       [(:panels s) (:lines s) (set (:texts s))
                        (->> (pj/plot p) (tree-seq coll? seq) (filter map?)
                             (keep :stroke) (filter #(re-find #"^rgb" %)) set)]))
        both (seen (-> d (pj/lay-line :t :a) (pj/lay-line :t :b)))]
    (is (= both (seen (-> d (pj/pose :t :a) pj/lay-line (pj/lay-line :t :b)))))
    (is (contains? (nth both 2) "a, b"))))
