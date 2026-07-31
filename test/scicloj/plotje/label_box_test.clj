(ns scicloj.plotje.label-box-test
  "The background box a text mark can be drawn on: the `:box` option, the
   `pj/lay-label` entry point that switches it on, rounded corners, and a
   border that strokes rather than fills. `:text` and `:label` are two
   layer types drawn by one `:text` mark: `:label` is `:text` with
   `{:box true}` preset in the registry, so a boxed layer reports its mark
   as `:text` too. Part of
   https://github.com/scicloj/plotje/issues/19 (nicer labels)."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.walk :as walk]
            [scicloj.plotje.api :as pj]))

(def label-data {:x [1 2 3] :y [1 2 3] :t ["a" "bb" "ccc"]})

;; ---- helpers ----

(defn- box-rects
  "Attribute maps of the background rects -- those carrying an rx that are
   not square (a point marker is always drawn on a square box)."
  [svg]
  (let [acc (atom [])]
    (walk/postwalk
     (fn [x]
       (when (and (vector? x) (= :rect (first x)) (map? (second x)))
         (swap! acc conj (second x)))
       x)
     svg)
    (filterv #(and (number? (:rx %))
                   (not= (double (:width %)) (double (:height %))))
             @acc)))

(defn- text-style
  "The :style map of the single text layer in a plan."
  ([opts] (text-style pj/lay-label opts))
  ([layer-fn opts]
   (->> (-> label-data (layer-fn :x :y (merge {:text :t} opts)))
        pj/plan :panels first :layers
        (filter #(= :text (:mark %)))
        first :style)))

;; ---- two layer types, one mark ----

(deftest label-is-the-text-layer-type-with-a-preset-box
  (testing ":label stays a registered layer type, drawn by the :text mark"
    (let [lt (pj/layer-type-lookup :label)]
      (is (some? lt))
      (is (= :text (:mark lt)))
      (is (= {:box true} (:defaults lt)))))
  (testing "so a boxed layer reports the :text mark, like any other text layer"
    (is (= [:text] (->> (-> label-data (pj/lay-label :x :y {:text :t}))
                        pj/plan :panels first :layers
                        (mapv :mark)))))
  (testing "the preset is the only difference from pj/lay-text"
    (is (= (text-style pj/lay-label {})
           (text-style pj/lay-text {:box true})))))

;; ---- the option ----

(deftest box-is-off-for-text-and-on-for-label
  (testing "a bare text layer carries no box"
    (is (nil? (:box (text-style pj/lay-text {}))))
    (is (empty? (box-rects (pj/plot (-> label-data (pj/lay-text :x :y {:text :t})))))))
  (testing "pj/lay-label switches it on, with the default radius"
    (is (= {:corner-radius 3.0} (:box (text-style pj/lay-label {})))))
  (testing "an explicit :box on pj/lay-label wins over the default"
    (is (nil? (:box (text-style pj/lay-label {:box false}))))
    (is (= {:corner-radius 8.0} (:box (text-style pj/lay-label {:box {:corner-radius 8}})))))
  (testing "true is shorthand for the default box"
    (is (= (text-style pj/lay-text {:box true})
           (text-style pj/lay-text {:box {}})))))

(deftest corner-radius-reaches-the-rendered-rects
  (testing "the default radius appears as rx and ry on both box rects"
    (let [rs (box-rects (pj/plot (-> label-data (pj/lay-label :x :y {:text :t}))))]
      ;; two rects per label: the filled box and the stroked border
      (is (= 6 (count rs)))
      (is (every? #(= 3.0 (double (:rx %))) rs))
      (is (every? #(= 3.0 (double (:ry %))) rs))))
  (testing "zero gives square corners"
    (is (every? #(zero? (double (:rx %)))
                (box-rects (pj/plot (-> label-data
                                        (pj/lay-label :x :y {:text :t :box {:corner-radius 0}})))))))
  (testing "a larger radius flows through"
    (is (every? #(= 8.0 (double (:rx %)))
                (box-rects (pj/plot (-> label-data
                                        (pj/lay-label :x :y {:text :t :box {:corner-radius 8}}))))))))

(deftest box-options-are-validated
  (testing "a negative or non-numeric radius is rejected"
    (is (thrown-with-msg? Exception #":box :corner-radius must be a non-negative number"
                          (text-style {:box {:corner-radius -1}})))
    (is (thrown-with-msg? Exception #":box :corner-radius must be a non-negative number"
                          (text-style {:box {:corner-radius :round}}))))
  (testing "a misspelled box property is rejected rather than silently ignored"
    (is (thrown-with-msg? Exception #":box accepts only"
                          (text-style {:box {:radius 8}}))))
  (testing ":box itself must be a boolean or a map"
    (is (thrown-with-msg? Exception #":box must be true, false, or a map"
                          (text-style {:box :yes})))
    (is (thrown-with-msg? Exception #":box must be true, false, or a map"
                          (text-style {:box 8})))))

;; ---- the border ----

(deftest border-strokes-instead-of-filling
  (testing "each label draws a white fill and a stroked border over it"
    (let [rs (box-rects (pj/plot (-> label-data (pj/lay-label :x :y {:text :t}))))
          fills (filter #(not= "none" (:fill %)) rs)
          borders (filter #(= "none" (:fill %)) rs)]
      (is (= 3 (count fills)))
      (is (= 3 (count borders)))
      (is (every? #(= "rgb(255,255,255)" (:fill %)) fills))
      (testing "the border is a stroke -- it used to be a grey fill painted
                over the white box, so the box read flat grey"
        (is (every? #(and (= "none" (:fill %))
                          (some? (:stroke %))
                          (not= "none" (:stroke %))
                          (number? (:stroke-width %)))
                    borders))))))

;; ---- svg-summary keeps boxes out of the data counts ----

(deftest label-boxes-are-counted-separately
  (testing ":label-boxes reports one box per label"
    (is (= 3 (:label-boxes (pj/svg-summary (-> label-data (pj/lay-label :x :y {:text :t}))))))
    (is (= 0 (:label-boxes (pj/svg-summary (-> label-data (pj/lay-text :x :y {:text :t}))))))
    (is (= 0 (:label-boxes (pj/svg-summary (-> label-data (pj/lay-point :x :y)))))))
  (testing "a label box is neither a data point nor a heatmap tile"
    (let [s (pj/svg-summary (-> label-data (pj/lay-label :x :y {:text :t})))]
      (is (= 0 (:points s)))
      ;; Regression: the square boxes carried no rx and so landed in :tiles,
      ;; reporting 6 phantom tiles (two rects per label) for a plot with none.
      (is (= 0 (:tiles s)))
      (is (= #{} (:sizes s)))))
  (testing "points and labels on one panel stay distinguishable"
    (let [s (pj/svg-summary (-> label-data
                                (pj/lay-point :x :y)
                                (pj/lay-label :x :y {:text :t})))]
      (is (= 3 (:points s)))
      (is (= 3 (:label-boxes s)))
      (is (= 0 (:tiles s)))))
  (testing "a heatmap's tiles are still counted as tiles"
    (let [s (pj/svg-summary (-> {:x [0 0 1 1] :y [0 1 0 1] :v [1 2 3 4]}
                                (pj/lay-tile :x :y {:fill :v})))]
      (is (pos? (:tiles s)))
      (is (= 0 (:label-boxes s))))))

;; ---- Java2D renders it ----

(deftest java2d-renders-a-rounded-label
  (testing "a rounded label box rasterizes without error"
    (let [img (pj/plot (-> label-data (pj/lay-label :x :y {:text :t :box {:corner-radius 6}}))
                       {:format :bufimg})]
      (is (pos? (.getWidth img))))))
