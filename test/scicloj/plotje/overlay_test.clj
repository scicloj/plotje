(ns scicloj.plotje.overlay-test
  "`pj/overlay` and `{:overlay true}`: a layer joins the panel it is
   added to instead of starting one of its own."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def wealth
  [{:cohort "a" :growth 1.0 :tax 0.3}
   {:cohort "b" :growth 2.0 :tax 0.9}])

(def weather
  {:day [1 2 3] :reading [10 12 11] :humidity [40 55 50] :other [7 8 9]})

(defn- panels [pose]
  (:panels (pj/svg-summary pose)))

(deftest overlay-on-a-leaf-test
  (testing "without it, a second layer naming another x is a second panel"
    (is (= 2 (panels (-> wealth
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort))))))
  (testing "pj/overlay joins every layer added after it"
    (is (= 1 (panels (-> wealth
                         pj/overlay
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort))))))
  (testing "{:overlay true} joins one layer"
    (is (= 1 (panels (-> wealth
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort {:overlay true}))))))
  (testing "a layer opts out of a pose-level pj/overlay"
    (is (= 2 (panels (-> wealth
                         pj/overlay
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort {:overlay false}))))))
  (testing "(pj/overlay pose false) turns it off from there on"
    (is (= 2 (panels (-> wealth
                         pj/overlay
                         (pj/lay-bar :growth :cohort)
                         (pj/overlay false)
                         (pj/lay-bar :tax :cohort)))))))

(deftest overlay-keeps-the-layers-columns-test
  (testing "the joined layer carries its own columns, and both are drawn"
    (let [pose (-> wealth
                   pj/overlay
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
                                            pj/overlay
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
                     (pj/lay-point :quarter :cost)
                     pj/overlay)]

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
                    (pj/lay-point :quarter :cost {:color "#e6550d"})
                    pj/overlay)]
        (is (= "revenue" (:y-label (pj/plan own))))))

    (testing "layers drawing one place are untouched"
      (let [same (-> d (pj/lay-point :quarter :revenue)
                     (pj/lay-line :quarter :revenue) pj/overlay)]
        (is (nil? (:legend (pj/plan same))))
        (is (= "revenue" (:y-label (pj/plan same))))))

    (testing "an annotation placed in drawing space does not rename the axis"
      (let [annotated (-> d (pj/lay-point :quarter :revenue)
                          (pj/lay-text {:x 10 :y 10 :text "n" :in :drawing-area}))]
        (is (= "quarter" (:x-label (pj/plan annotated))))))

    (testing "the same layers in panels of their own keep one axis name"
      ;; Each panel's strip names the column it draws, so listing them
      ;; on the shared axis as well says the same thing twice. Only
      ;; layers sharing a panel put more than one name on an axis.
      (let [split (-> d (pj/lay-point :quarter :revenue)
                      (pj/lay-point :quarter :cost))]
        (is (= 2 (:panels (pj/svg-summary split))))
        (is (= "revenue" (:y-label (pj/plan split))))))))

(deftest overlay-is-read-at-draft-time-test
  ;; `:overlay` used to be read where the layer was added, and the pose
  ;; structure recorded where it landed. That made `pj/overlay` the one
  ;; step in the pipeline whose placement in a thread changed the result.
  ;; It is carried now and read at draft time, beside every other mapping.
  (testing "a pose-level flag stays on the pose"
    (is (true? (:overlay (-> wealth
                             pj/overlay
                             (pj/lay-bar :growth :cohort)
                             (pj/lay-bar :tax :cohort))))))

  (testing "a per-layer flag is kept on the layer, for draft to read"
    (let [layers (:layers (-> wealth
                              (pj/lay-bar :growth :cohort)
                              (pj/lay-bar :tax :cohort {:overlay true})))]
      (is (not (contains? (first layers) :overlay)))
      (is (true? (:overlay (second layers))))))

  (testing "pj/overlay says the same thing wherever in a thread it is written"
    (let [before (-> wealth
                     pj/overlay
                     (pj/lay-bar :growth :cohort)
                     (pj/lay-bar :tax :cohort))
          after  (-> wealth
                     (pj/lay-bar :growth :cohort)
                     (pj/lay-bar :tax :cohort)
                     pj/overlay)]
      (is (= 1 (panels before)))
      (is (= 1 (panels after)))
      ;; The same picture, not merely the same panel count.
      (is (= (pj/plot before) (pj/plot after)))))

  (testing "a mapping written after a layer already applied to it, and now :overlay does too"
    (is (= (pj/plot (-> weather
                        pj/overlay
                        (pj/lay-line :day :reading)
                        (pj/lay-line :day :humidity)))
           (pj/plot (-> weather
                        (pj/lay-line :day :reading)
                        (pj/lay-line :day :humidity)
                        pj/overlay))))))

(deftest overlay-on-a-composite-test
  (testing "a miss appends a panel without it, and joins the last leaf with it"
    (let [base (-> weather
                   (pj/pose :day :reading)
                   (pj/pose :day :humidity))]
      (is (= 3 (panels (pj/lay-point base :day :other))))
      (is (= 2 (panels (-> base pj/overlay (pj/lay-point :day :other)))))))
  (testing "a hit still lands on the matching leaf, overlay or not"
    (let [base (-> weather
                   (pj/pose :day :reading)
                   (pj/pose :day :humidity))]
      (is (= 2 (panels (-> base pj/overlay (pj/lay-point :day :humidity))))))))
