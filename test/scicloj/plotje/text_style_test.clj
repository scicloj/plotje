(ns scicloj.plotje.text-style-test
  "Font weight and slant on the :text mark (`:font-weight`,
   `:font-style`). The membrane stage carries them on the Font record;
   the SVG backend realizes them as font-weight/font-style attributes and
   the Java2D backend as java.awt.Font BOLD/ITALIC styles.
   Regression guard for
   https://github.com/scicloj/plotje/issues/21 (bold and italic text)."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.walk :as walk]
            [scicloj.plotje.api :as pj])
  (:import [java.awt Color]
           [java.awt.image BufferedImage]))

(def text-data {:x [1 2 3] :y [1 2 3] :t ["a" "bb" "ccc"]})

;; ---- helpers ----

(defn- texts
  "All :text hiccup elements in an SVG tree."
  [svg]
  (let [acc (atom [])]
    (walk/postwalk
     (fn [x]
       (when (and (vector? x) (= :text (first x)) (map? (second x)))
         (swap! acc conj x))
       x)
     svg)
    @acc))

(defn- data-text-attrs
  "Attribute maps of the texts whose content is one of the data labels --
   excludes axis titles, tick labels, and other chrome."
  [svg]
  (let [labels #{"a" "bb" "ccc"}]
    (->> (texts svg)
         (filter #(labels (last %)))
         (mapv second))))

(defn- text-style
  "The :style map of the single text layer in a plan."
  [layer-fn mark opts]
  (->> (-> text-data (layer-fn :x :y (merge {:text :t} opts)))
       pj/plan :panels first :layers
       (filter #(= mark (:mark %)))
       first :style))

;; ---- plan-level: options flow into the layer style ----

(deftest font-weight-and-style-default-to-normal
  (testing "plain text carries :normal for both, preserving the pre-existing look"
    (is (= {:font-weight :normal :font-style :normal}
           (select-keys (text-style pj/lay-text :text {}) [:font-weight :font-style])))
    (is (= {:font-weight :normal :font-style :normal}
           (select-keys (text-style pj/lay-label :text {}) [:font-weight :font-style])))))

(deftest font-weight-and-style-flow-to-the-plan
  (testing "both entry points accept both options"
    (is (= :bold (:font-weight (text-style pj/lay-text :text {:font-weight :bold}))))
    (is (= :italic (:font-style (text-style pj/lay-text :text {:font-style :italic}))))
    (is (= :bold (:font-weight (text-style pj/lay-label :text {:font-weight :bold}))))
    (is (= :italic (:font-style (text-style pj/lay-label :text {:font-style :italic})))))
  (testing "the two compose -- bold italic"
    (is (= {:font-weight :bold :font-style :italic}
           (select-keys (text-style pj/lay-text :text {:font-weight :bold :font-style :italic})
                        [:font-weight :font-style])))))

(deftest unrecognized-values-are-rejected
  (testing "a numeric CSS weight is rejected -- Java2D has bold or nothing"
    (is (thrown? Exception (text-style pj/lay-text :text {:font-weight 700}))))
  (testing "an unknown weight names the accepted values"
    (is (thrown-with-msg? Exception #":font-weight must be one of"
                          (text-style pj/lay-text :text {:font-weight :heavy}))))
  (testing ":oblique is rejected -- SVG would slant it but Java2D draws it upright"
    (is (thrown-with-msg? Exception #":font-style must be one of"
                          (text-style pj/lay-text :text {:font-style :oblique})))))

;; ---- SVG backend ----

(deftest svg-emits-font-weight-and-font-style
  (testing "plain text carries neither attribute"
    (let [attrs (data-text-attrs (pj/plot (-> text-data (pj/lay-text :x :y {:text :t}))))]
      (is (= 3 (count attrs)))
      (is (every? #(nil? (:font-weight %)) attrs))
      (is (every? #(nil? (:font-style %)) attrs))))
  (testing "bold text carries font-weight only"
    (let [attrs (data-text-attrs (pj/plot (-> text-data (pj/lay-text :x :y {:text :t :font-weight :bold}))))]
      (is (every? #(= "bold" (:font-weight %)) attrs))
      (is (every? #(nil? (:font-style %)) attrs))))
  (testing "italic text carries font-style only"
    (let [attrs (data-text-attrs (pj/plot (-> text-data (pj/lay-text :x :y {:text :t :font-style :italic}))))]
      (is (every? #(= "italic" (:font-style %)) attrs))
      (is (every? #(nil? (:font-weight %)) attrs))))
  (testing "a boxed label's text is styled the same way as bare text"
    (let [attrs (data-text-attrs (pj/plot (-> text-data (pj/lay-label :x :y {:text :t
                                                                             :font-weight :bold
                                                                             :font-style :italic}))))]
      (is (every? #(= "bold" (:font-weight %)) attrs))
      (is (every? #(= "italic" (:font-style %)) attrs)))))

(deftest svg-summary-counts-styled-texts
  (testing "counts are zero for plain text and one per styled label"
    (let [s (fn [opts] (pj/svg-summary (-> text-data (pj/lay-text :x :y (merge {:text :t} opts)))))]
      (is (= [0 0] ((juxt :bold-texts :italic-texts) (s {}))))
      (is (= [3 0] ((juxt :bold-texts :italic-texts) (s {:font-weight :bold}))))
      (is (= [0 3] ((juxt :bold-texts :italic-texts) (s {:font-style :italic}))))
      (is (= [3 3] ((juxt :bold-texts :italic-texts)
                    (s {:font-weight :bold :font-style :italic}))))))
  (testing "axis and tick chrome stays unstyled, so the counts report data text only"
    (is (= 0 (:bold-texts (pj/svg-summary (-> text-data (pj/lay-point :x :y))))))))

;; ---- Java2D backend ----
;;
;; Attribute counting cannot see this path at all: bufimg draws through
;; membrane.java2d, which reads :weight/:slant off the Font record. These
;; check the raster itself.

(defn- dark-pixels [^BufferedImage img]
  (let [w (.getWidth img) h (.getHeight img)]
    (count (for [x (range w) y (range h)
                 :let [c (Color. (.getRGB img x y))]
                 :when (and (< (.getRed c) 100) (< (.getGreen c) 100) (< (.getBlue c) 100))]
             true))))

(defn- raster-hash [^BufferedImage img]
  (hash (for [x (range (.getWidth img)) y (range (.getHeight img))] (.getRGB img x y))))

(defn- big-text-png [opts]
  (pj/plot (-> {:x [2] :y [2] :t ["HHHHHHHH"]}
               (pj/lay-text :x :y (merge {:text :t :font-size 40} opts)))
           {:format :bufimg}))

(deftest java2d-draws-bold-and-italic
  (let [plain (big-text-png {})
        bold (big-text-png {:font-weight :bold})
        italic (big-text-png {:font-style :italic})]
    (testing "bold lays down more ink than plain at the same size"
      (is (> (dark-pixels bold) (* 1.2 (dark-pixels plain)))))
    (testing "italic slants the glyphs -- a different raster at a similar weight"
      (is (not= (raster-hash italic) (raster-hash plain)))
      (is (< (Math/abs (- (dark-pixels italic) (dark-pixels plain)))
             (* 0.2 (dark-pixels plain)))))))
