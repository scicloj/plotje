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

    (testing "layers that specify a colour take no default colour, and the axis still names every column"
      (let [own (-> d
                    (pj/lay-point :quarter :revenue {:color "#377eb8"})
                    (pj/lay-point :quarter :cost {:color "#e6550d"}))]
        (is (= "revenue, cost" (:y-label (pj/plan own))))
        (is (nil? (:legend (pj/plan own))))
        (is (= #{"rgb(55,126,184)" "rgb(230,85,13)"}
               (disj (:colors (pj/svg-summary own)) "none")))))

    (testing "the default is decided layer by layer: a literal colour on one layer leaves the others theirs"
      (let [with-line (-> d
                          (pj/pose {:x :quarter})
                          (pj/lay-point {:y :revenue})
                          (pj/lay-point {:y :cost})
                          (pj/lay-line {:y :revenue :color "black"}))
            plan (pj/plan with-line)]
        (is (= ["revenue" "cost"] (mapv :label (:entries (:legend plan)))))
        (is (= #{"rgb(0,0,0)" "rgb(228,26,28)" "rgb(55,126,184)"}
               (disj (:colors (pj/svg-summary with-line)) "none")))))

    (testing "the default specified on one layer leaves the other layer its default"
      (let [one (-> d
                    (pj/lay-point :quarter :revenue {:color {:value "revenue" :scale {:label ""}}})
                    (pj/lay-point :quarter :cost))]
        (is (= (:colors (pj/svg-summary overlaid)) (:colors (pj/svg-summary one))))
        (is (= ["revenue" "cost"] (mapv :label (:entries (:legend (pj/plan one))))))))

    (testing "no layer takes the default on a panel where a layer is coloured by a column"
      (let [d2 (assoc d :region ["n" "s" "n" "s"])
            plan (pj/plan (-> d2
                              (pj/lay-point :quarter :revenue {:color :region})
                              (pj/lay-point :quarter :cost)))]
        (is (= :region (:title (:legend plan))))
        (is (= ["n" "s"] (mapv :label (:entries (:legend plan)))))))

    (testing "a scale set on the pose leaves a layer's literal colour drawn as it stands"
      (let [d2 (assoc d :region ["n" "s" "n" "s"])
            p (-> d2
                  (pj/pose {:x :quarter :y :revenue :color :region})
                  (pj/scale :color {:label "Region"})
                  pj/lay-point
                  (pj/lay-line {:color "black"}))]
        (is (= ["n" "s"] (mapv :label (:entries (:legend (pj/plan p))))))
        (is (contains? (:colors (pj/svg-summary p)) "rgb(0,0,0)"))))

    (testing "a :color on the pose reaches the layers, so they take no default colour"
      (let [d2 (assoc d :region ["n" "s" "n" "s"])
            by-pose (-> d2
                        (pj/pose {:x :quarter :color :region})
                        (pj/lay-point {:y :revenue})
                        (pj/lay-point {:y :cost}))
            plan (pj/plan by-pose)]
        (is (= :region (:title (:legend plan))))
        (is (= ["n" "s"] (mapv :label (:entries (:legend plan)))))
        (is (= "revenue, cost" (:y-label plan)))))

    (testing "so does a constant colour, and a colour cancelled with nil"
      (doseq [c ["#377eb8" nil]]
        (let [p (-> d
                    (pj/pose {:x :quarter :color c})
                    (pj/lay-point {:y :revenue})
                    (pj/lay-point {:y :cost}))]
          (is (nil? (:legend (pj/plan p))))
          (is (= "revenue, cost" (:y-label (pj/plan p)))))))

    (testing "a layer placed in the drawing area names no column of an axis"
      (let [noted (-> d
                      (pj/lay-point :quarter :revenue)
                      (pj/lay-point {:in :drawing-area
                                     :data {:px [50.0] :py [50.0]}
                                     :x :px :y :py}))
            plan (pj/plan noted)]
        (is (= "revenue" (:y-label plan)))
        (is (= "quarter" (:x-label plan)))
        (is (nil? (:legend plan)))))

    (testing "another aesthetic on the pose leaves :color unspecified, so the default colours are given"
      (let [d2 (assoc d :region ["n" "s" "n" "s"])
            shaped (-> d2
                       (pj/pose {:x :quarter :shape :region})
                       (pj/lay-point {:y :revenue})
                       (pj/lay-point {:y :cost}))
            entries (fn [pose] (set (:texts (pj/svg-summary pose))))]
        (is (every? (entries shaped) ["revenue" "cost" "n" "s"]))))

    (testing "a fitted line grouped by one column keeps the colour of its own column"
      (let [iris-like {:a (vec (range 12))
                       :b (mapv #(* 2.0 %) (range 12))
                       :c (mapv #(+ 5.0 (* 0.5 %)) (range 12))
                       :g (vec (take 12 (cycle ["u" "v"])))}
            grouped (-> iris-like
                        (pj/pose {:x :a :group :g})
                        (pj/lay-smooth {:y :b :stat :linear-model})
                        (pj/lay-smooth {:y :c :stat :linear-model}))
            colours (fn [layer] (set (map :color (:groups layer))))
            [l1 l2] (:layers (first (:panels (pj/plan grouped))))]
        (is (= 2 (count (:groups l1))))
        (is (= 1 (count (colours l1))))
        (is (= 1 (count (colours l2))))
        (is (not= (colours l1) (colours l2)))))

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

(deftest a-column-on-both-axes-titles-both-test
  ;; One column drawn against itself is a column drawn on :y, and the
  ;; layers beside it keep their :y columns in the title.
  (let [y-label (fn [pose] (:y-label (pj/plan pose)))]
    (testing "a layer drawing one column on both axes"
      (is (= "reading" (y-label (pj/lay-point weather :reading :reading)))))
    (testing "beside a layer drawing another column on :y, in either order"
      (is (= "reading, humidity"
             (y-label (-> weather
                          (pj/lay-point :reading :reading)
                          (pj/lay-point :reading :humidity)))))
      (is (= "humidity, reading"
             (y-label (-> weather
                          (pj/lay-point :reading :humidity)
                          (pj/lay-point :reading :reading))))))))
