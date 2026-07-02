(ns scicloj.plotje.stroke-test
  "Stroke family: an opt-in outline on filled area/density marks
   (`:stroke`, `:stroke-width`) and a dash pattern on stroked paths
   (`:stroke-dash`). The membrane stage expresses the dash as a
   WithStrokeDash wrapper; the SVG backend realizes it as
   stroke-dasharray and the Java2D backend as a dashed BasicStroke.
   Regression guard for
   https://github.com/scicloj/plotje/issues/11 (fill + outline) and
   https://github.com/scicloj/plotje/issues/12 (dashed lines)."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.walk :as walk]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.extract :as extract])
  (:import [java.awt Color]))

(def iris-like
  {:v [4.3 4.6 5.0 5.4 5.8 6.1 6.5 6.9 7.2 7.6
       4.4 4.8 5.1 5.5 5.9 6.2 6.6 7.0 7.3 7.9]
   :grp (into (vec (repeat 10 "a")) (repeat 10 "b"))})

(def line-data {:x [1 2 3 4 5] :y [10 20 15 25 18]})

;; ---- helpers ----

(defn- elems
  "All hiccup element vectors in an SVG tree matching tag + attr predicate."
  [svg tag pred]
  (let [acc (atom [])]
    (walk/postwalk
     (fn [x]
       (when (and (vector? x) (= tag (first x)) (map? (second x)) (pred (second x)))
         (swap! acc conj x))
       x)
     svg)
    @acc))

(defn- dashed-polylines [svg]
  (elems svg :polyline #(:stroke-dasharray %)))

(defn- outline-polylines [svg css-color]
  ;; Area outlines are stroked polylines (fill none) in the outline color;
  ;; excludes the light grey grid lines.
  (elems svg :polyline #(and (= "none" (:fill %)) (= css-color (:stroke %)))))

;; ---- resolve-dash unit ----

(deftest resolve-dash-presets-and-vectors
  (testing "named presets resolve to [dash gap]"
    (is (= [6 4] (extract/resolve-dash :dashed)))
    (is (= [1 3] (extract/resolve-dash :dotted))))
  (testing ":solid and nil are no-dash"
    (is (nil? (extract/resolve-dash :solid)))
    (is (nil? (extract/resolve-dash nil))))
  (testing "an unknown preset is dropped (no dash)"
    (is (nil? (extract/resolve-dash :zigzag))))
  (testing "a raw [dash gap ...] vector passes through"
    (is (= [8 3] (extract/resolve-dash [8 3])))
    (is (= [5 2 1 2] (extract/resolve-dash [5 2 1 2])))))

;; ---- #12 dashed lines ----

(deftest dashed-line-emits-stroke-dasharray
  (testing "a :dashed line emits one dashed polyline"
    (let [ps (dashed-polylines (pj/plot (-> line-data (pj/lay-line :x :y {:stroke-dash :dashed}))))]
      (is (= 1 (count ps)))
      (is (= "6.00 4.00" (:stroke-dasharray (second (first ps)))))))
  (testing "a raw [dash gap] vector is honored verbatim"
    (let [ps (dashed-polylines (pj/plot (-> line-data (pj/lay-line :x :y {:stroke-dash [8 3]}))))]
      (is (= "8.00 3.00" (:stroke-dasharray (second (first ps)))))))
  (testing ":solid draws no dash"
    (is (empty? (dashed-polylines (pj/plot (-> line-data (pj/lay-line :x :y {:stroke-dash :solid}))))))))

(deftest plain-line-has-no-dash
  (testing "an ordinary line carries no stroke-dasharray"
    (is (empty? (dashed-polylines (pj/plot (-> line-data (pj/lay-line :x :y))))))))

(deftest svg-summary-counts-dashed-lines
  (testing ":dashed-lines separates a dashed line from a solid one"
    (is (= 0 (:dashed-lines (pj/svg-summary (-> line-data (pj/lay-line :x :y))))))
    (is (= 1 (:dashed-lines (pj/svg-summary (-> line-data (pj/lay-line :x :y {:stroke-dash :dashed})))))))
  (testing ":dash-patterns reports the distinct patterns; presets differ"
    (is (= #{} (:dash-patterns (pj/svg-summary (-> line-data (pj/lay-line :x :y))))))
    (is (contains? (:dash-patterns (pj/svg-summary (-> line-data (pj/lay-line :x :y {:stroke-dash :dashed})))) "6.00 4.00"))
    (is (contains? (:dash-patterns (pj/svg-summary (-> line-data (pj/lay-line :x :y {:stroke-dash :dotted})))) "1.00 3.00"))
    (is (contains? (:dash-patterns (pj/svg-summary (-> line-data (pj/lay-line :x :y {:stroke-dash [12 4]})))) "12.00 4.00"))))

(deftest dashed-reference-line
  (testing "a rule (reference line) honors :stroke-dash"
    (let [svg (pj/plot (-> line-data
                           (pj/lay-point :x :y)
                           (pj/lay-rule-v {:x-intercept 3 :color "red" :stroke-dash :dashed})))
          ps (outline-polylines svg "rgb(255,0,0)")]
      (is (= 1 (count ps)))
      (is (= "6.00 4.00" (:stroke-dasharray (second (first ps))))))))

;; ---- #11 fill + outline ----

(deftest density-outline-is-opt-in
  (testing "a plain density has a fill polygon and no outline polyline"
    (let [svg (pj/plot (-> iris-like (pj/lay-density :v)))]
      (is (= 1 (count (elems svg :polygon #(not= "none" (:fill %))))))
      (is (empty? (outline-polylines svg "rgb(255,0,0)")))))
  (testing "grouped density still draws no outline unless asked"
    (let [svg (pj/plot (-> iris-like (pj/lay-density :v {:color :grp})))]
      (is (empty? (outline-polylines svg "rgb(255,0,0)"))))))

(deftest grouped-density-outlines-each-group
  (testing "one red outline per group when :stroke is set"
    (let [svg (pj/plot (-> iris-like (pj/lay-density :v {:color :grp :stroke "red"})))]
      (is (= 2 (count (outline-polylines svg "rgb(255,0,0)")))))))

(deftest density-with-stroke-draws-an-outline
  (testing "a light fill plus a red outline -- the issue #11 look"
    (let [svg (pj/plot (-> iris-like (pj/lay-density :v {:color "lightblue" :stroke "red"})))]
      (is (= 1 (count (elems svg :polygon #(= "rgb(173,216,230)" (:fill %))))))
      (is (= 1 (count (outline-polylines svg "rgb(255,0,0)"))))))
  (testing ":stroke-width sets the outline width"
    (let [svg (pj/plot (-> iris-like (pj/lay-density :v {:stroke "red" :stroke-width 1})))
          o (first (outline-polylines svg "rgb(255,0,0)"))]
      (is (= 1 (:stroke-width (second o))))))
  (testing "a dashed outline on an area combines fill + dashed stroke"
    (let [svg (pj/plot (-> iris-like (pj/lay-density :v {:stroke "red" :stroke-dash [8 4]})))
          o (first (outline-polylines svg "rgb(255,0,0)"))]
      (is (= "8.00 4.00" (:stroke-dasharray (second o)))))))

;; ---- Java2D parity (renders + stroke color present) ----

(defn- has-color? [^java.awt.image.BufferedImage img r-lo g-hi b-hi]
  (let [w (.getWidth img) h (.getHeight img)]
    (boolean
     (some (fn [y]
             (some (fn [x]
                     (let [c (Color. (.getRGB img x y))]
                       (and (> (.getRed c) r-lo) (< (.getGreen c) g-hi) (< (.getBlue c) b-hi))))
                   (range 0 w 3)))
           (range 0 h 3)))))

(deftest java2d-renders-dash-and-outline
  (testing "a dashed line renders to a raster without error"
    (let [img (pj/plot (-> line-data (pj/lay-line :x :y {:stroke-dash :dashed})) {:format :bufimg})]
      (is (pos? (.getWidth img)))))
  (testing "a red-outlined density paints red pixels in the raster"
    (let [img (pj/plot (-> iris-like (pj/lay-density :v {:color "lightblue" :stroke "red"}))
                       {:format :bufimg})]
      (is (has-color? img 150 100 100)))))
