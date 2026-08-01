(ns scicloj.plotje.data-labels-test
  "Labelling bars with their own values.

   Two halves of one feature. A label layer grouped the way a bar layer is
   dodged now dodges with it, so each label sits over the bar it names rather
   than piling up on the band centre (issue #13). And a counting stat now
   also reports its counts as points, so `(pj/lay-label {:stat :count})`
   labels each bar with its height without the caller pre-aggregating
   (issue #14).

   The assertions read positions out of the rendered SVG, because that is
   where the placement lives: the bug in #13 was invisible in the pose and in
   the draft, and showed in the plan only as a missing dodge context."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]
            [tablecloth.api :as tc]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ---- Reading positions out of rendered SVG ----

(def panel-fill "rgb(232,232,232)")

(defn- placed
  "Every element of an SVG hiccup tree, each carrying the absolute position of
   the group it is drawn in. Plotje nests a translate per drawing region, so
   an element's own coordinates are relative to its ancestors'."
  [node dx dy]
  (when (vector? node)
    (let [[tag attrs] node
          attrs (when (map? attrs) attrs)
          [dx dy] (if-let [[_ x y] (some->> (:transform attrs)
                                            (re-matches #"translate\(([-\d.]+),([-\d.]+)\)"))]
                    [(+ dx (parse-double x)) (+ dy (parse-double y))]
                    [dx dy])]
      (cons (assoc (or attrs {}) :tag tag :text (last node) :x dx :y dy)
            (mapcat #(placed % dx dy) (rest node))))))

(defn- elements [pose]
  (placed (pj/plot pose) 0.0 0.0))

(defn- panel-box
  "The panel background rectangle, as {:x0 :y0 :x1 :y1}."
  [els]
  (let [{:keys [x y width height]} (first (filter #(and (= :rect (:tag %))
                                                        (= panel-fill (:fill %)))
                                                  els))]
    {:x0 x :y0 y :x1 (+ x (parse-double (str width))) :y1 (+ y (parse-double (str height)))}))

(defn- bars
  "Bar polygons, as {:x0 :x1 :top} in absolute coordinates. A bar is drawn as
   a closed path, so its points give the span and the height directly."
  [els]
  (->> els
       (filter #(= :polygon (:tag %)))
       (mapv (fn [{:keys [points x y]}]
               (let [pts (mapv (fn [p] (mapv parse-double (str/split p #",")))
                               (str/split (str points) #" "))
                     xs (map first pts)
                     ys (map second pts)]
                 {:x0 (+ x (apply min xs))
                  :x1 (+ x (apply max xs))
                  :top (+ y (apply min ys))})))))

(defn- data-labels
  "Texts drawn inside the panel -- the labels on the data. Tick labels, axis
   titles and legend entries all sit outside it."
  [els]
  (let [{:keys [x0 y0 x1 y1]} (panel-box els)]
    (->> els
         (filter #(and (= :text (:tag %))
                       (<= x0 (:x %) x1)
                       (<= y0 (:y %) y1)))
         (mapv #(select-keys % [:text :x :y])))))

;; ---- Dodged labels (#13) ----

(def dodged-bars
  "The reporter's shape: a value bar per (category, group), each labelled with
   its own value."
  (-> (tc/dataset {:sex ["male" "male" "female" "female"]
                   :species ["cat" "dog" "cat" "dog"]
                   :pct [21 17 9 14]})
      (pj/pose :sex :pct)
      (pj/lay-bar {:color :species})
      (pj/lay-label {:text :pct :group :species})))

(deftest a-label-layer-joins-the-dodge-of-the-bars-it-names
  (testing "the text layer carries the bar layer's dodge context and indices"
    ;; The two layers have to agree on both, or the labels dodge to positions
    ;; the bars are not at. They agree by construction: position/apply-positions
    ;; derives one context for the whole cohort, and the text layer is now in it.
    (let [layers (-> dodged-bars pj/plan :panels first :layers)
          by-mark (into {} (map (juxt :mark identity)) layers)]
      (is (= #{:rect :text} (set (keys by-mark))))
      (is (= (:dodge-ctx (:rect by-mark)) (:dodge-ctx (:text by-mark))))
      (is (= {:n-groups 2} (:dodge-ctx (:text by-mark))))
      (is (= (mapv (juxt :label :dodge-idx) (:groups (:rect by-mark)))
             (mapv (juxt :label :dodge-idx) (:groups (:text by-mark))))))))

(deftest each-label-sits-over-its-own-bar
  (testing "a label's position falls within the bar it names, not the band centre"
    ;; Reported as https://github.com/scicloj/plotje/issues/13: every label in
    ;; a band was drawn at the band centre, so both landed on the boundary
    ;; between two dodged bars instead of over their own.
    (let [els (elements dodged-bars)
          bs (bars els)
          ls (data-labels els)]
      (is (= 4 (count bs)) "one bar per (category, group)")
      (is (= 4 (count ls)) "one label per bar")
      (doseq [{:keys [text x y]} ls]
        (let [owner (first (filter #(<= (:x0 %) x (:x1 %)) bs))]
          (is (some? owner) (str "label " text " sits over a bar"))
          ;; The label is placed at the bar's top, which is what makes it read
          ;; as that bar's value.
          (is (< (abs (- y (:top owner))) 12.0)
              (str "label " text " sits at its bar's top"))))
      (is (= 4 (count (distinct (map :x ls))))
          "no two labels share a position"))))

(deftest an-ungrouped-label-layer-is-left-alone
  (testing "with nothing to dodge against, labels stay on their categories"
    (let [pose (-> (tc/dataset {:sex ["male" "female"] :pct [21 9]})
                   (pj/pose :sex :pct)
                   (pj/lay-bar)
                   (pj/lay-label {:text :pct}))
          layers (-> pose pj/plan :panels first :layers)
          text-layer (first (filter #(= :text (:mark %)) layers))
          els (elements pose)]
      (is (nil? (:dodge-ctx text-layer)))
      (is (= 2 (count (data-labels els)))))))

;; ---- Counted labels (#14) ----

(deftest a-counting-stat-can-be-read-as-label-text
  (testing "lay-label with :stat :count labels each bar with its count"
    ;; Requested as https://github.com/scicloj/plotje/issues/14. Before, the
    ;; count stat reported only bars, so a text layer over it extracted no
    ;; groups at all and drew nothing -- silently.
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-bar :species)
                   (pj/lay-label {:stat :count}))
          ls (data-labels (elements pose))]
      (is (= 3 (count ls)))
      (is (= ["50"] (distinct (map :text ls))) "iris has 50 rows per species"))))

(deftest counted-labels-dodge-with-their-counted-bars
  (testing "the two halves compose: a count label per dodged bar"
    (let [pose (-> (tc/dataset {:drv (concat (repeat 25 "4") (repeat 40 "f") (repeat 12 "r")
                                             (repeat 18 "4") (repeat 33 "f") (repeat 7 "r"))
                                :era (concat (repeat 77 "early") (repeat 58 "late"))})
                   (pj/lay-bar :drv {:color :era})
                   (pj/lay-label {:stat :count :group :era}))
          els (elements pose)
          bs (bars els)
          ls (data-labels els)]
      (is (= 6 (count bs)))
      (is (= #{"25" "40" "12" "18" "33" "7"} (set (map :text ls))))
      (doseq [{:keys [text x]} ls]
        (let [owner (first (filter #(<= (:x0 %) x (:x1 %)) bs))]
          (is (some? owner) (str "count " text " sits over a bar")))))))

(deftest a-count-label-reports-the-same-number-as-its-bar
  (testing "the labels are derived from the bars, so they cannot disagree"
    (let [pose (-> (tc/dataset {:grade (concat (repeat 4 "a") (repeat 9 "b") (repeat 2 "c"))})
                   (pj/lay-bar :grade)
                   (pj/lay-label {:stat :count}))
          layers (-> pose pj/plan :panels first :layers)
          rect (first (filter #(= :rect (:mark %)) layers))
          text (first (filter #(= :text (:mark %)) layers))
          counts (mapv :count (:counts (first (:groups rect))))]
      (is (= 3 (count counts)))
      (is (= (mapv str counts) (:labels (first (:groups text))))))))
