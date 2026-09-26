(ns scicloj.plotje.drawn-colour-test
  "Colour and legend changes checked on what is drawn -- the Java2D
   raster or the SVG -- rather than on counts: a two-colour gradient, a
   band's configured colour, arrow heads drawn without snapping, a
   gradient bar with no seams, a constant colour labelled once, a
   missing tile left as a gap, and a colour column on a rule."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj])
  (:import [java.awt Color]
           [java.awt.image BufferedImage]))

(defn- pixels
  "Every pixel of `img` as [x y r g b]."
  [^BufferedImage img]
  (for [y (range (.getHeight img))
        x (range (.getWidth img))
        :let [c (Color. (.getRGB img x y))]]
    [x y (.getRed c) (.getGreen c) (.getBlue c)]))

(defn- raster [pose] (pj/plot pose {:format :bufimg}))

(deftest two-colour-gradient-passes-through-purple-test
  ;; `{:low "blue" :high "red"}` ran through a light grey middle; red to
  ;; blue directly passes through purple -- red and blue, next to no
  ;; green -- which a path through near-white never draws.
  (let [img (raster (-> {:x (range 11) :y (range 11) :c (range 11)}
                        (pj/lay-point :x :y {:color :c :size 6})
                        (pj/options {:color-range {:low "blue" :high "red"}})))]
    (is (some (fn [[_ _ r g b]] (and (> r 90) (> b 90) (< g 40))) (pixels img)))))

(deftest band-color-configuration-paints-the-band-test
  (let [pose (pj/lay-band-h {:x [1 2] :y [1 2]} {:y-min 1.2 :y-max 1.8})
        red? (fn [img] (some (fn [[_ _ r g b]] (and (> r 200) (< g 120) (< b 120))) (pixels img)))]
    (is (not (red? (raster pose))) "the default band is grey")
    (is (red? (raster (pj/options pose {:config {:band-color "red" :band-opacity 1.0}}))))))

(deftest arrow-heads-are-not-snapped-test
  ;; `crispEdges` snapped a small diagonal triangle to whole pixels and
  ;; drew it as a jagged block beside its smooth line.
  (let [svg (pr-str (pj/plot (pj/lay-segment {:x [1] :y [1] :x1 [2] :y1 [2]} :x :y
                                             {:x-end :x1 :y-end :y1 :arrow :end})))
        heads (re-seq #"\[:polygon \{[^}]*\}" svg)]
    (is (= 1 (count heads)))
    (is (not-any? #(str/includes? % "crispEdges") heads)))
  (testing "a bar, a rectangle, is still snapped"
    (is (str/includes? (pr-str (pj/plot (pj/lay-bar {:q ["a" "b"] :v [1 2]} :q :v)))
                       "crispEdges"))))

(deftest gradient-bar-has-no-seams-test
  ;; Cells at fractional places left an anti-aliased hairline of the
  ;; background between them. Along the bar every pixel lies between its
  ;; neighbours' shades; a seam is a pixel lighter than both.
  (doseq [where [:right :bottom]]
    (testing (str where)
      (let [img (raster (-> {:x [1 2 3] :y [1 2 3] :c [1.0 5.0 9.0]}
                            (pj/lay-point :x :y {:color :c})
                            (pj/options {:legend-position where})))
            ^BufferedImage img img
            blue? (fn [x y] (let [c (Color. (.getRGB img x y))]
                              (> (.getBlue c) (+ 25 (.getRed c)))))
            ;; the bar is the longest straight run of bluish pixels,
            ;; along a row for :bottom and along a column for :right
            runs (if (= where :bottom)
                   (for [y (range (.getHeight img))]
                     (filterv #(blue? % y) (range (.getWidth img))))
                   (for [x (range (.getWidth img))]
                     (filterv #(blue? x %) (range (.getHeight img)))))
            [line run] (apply max-key (comp count second) (map-indexed vector runs))
            shade (fn [i] (let [c (Color. (if (= where :bottom)
                                            (.getRGB img (int i) (int line))
                                            (.getRGB img (int line) (int i))))]
                            (+ (.getRed c) (.getGreen c) (.getBlue c))))
            seams (for [[a b c] (partition 3 1 run)
                        :when (and (= (inc a) b) (= (inc b) c)
                                   (> (shade b) (+ 20 (max (shade a) (shade c)))))]
                    b)]
        (is (> (count run) 80) "the bar was found")
        (is (empty? seams))))))

(deftest constant-colour-is-labelled-once-test
  (let [svg (pr-str (pj/plot (pj/lay-point {:x [1 2] :y [1 2] :c [5 5]} :x :y {:color :c})))]
    (is (= 1 (count (re-seq #"\"5\"\]" svg))))))

(deftest a-missing-tile-is-a-gap-test
  ;; The step was measured after the row was dropped, so the two tiles
  ;; left grew to meet where the missing one was.
  (is (= [[0.5 1.5] [2.5 3.5]]
         (->> (pj/plan (pj/lay-tile {:x [1 2 3] :y [1 1 1] :f [1 nil 3]} :x :y {:fill :f}))
              :panels first :layers first :tiles
              (mapv (juxt :x-lo :x-hi))))))

(deftest a-colour-column-on-a-rule-test
  (let [d {:x [1 2 3] :y [1 2 3] :g ["a" "b" "a"]}]
    (testing "written on the rule: warned, and no legend of unused colours"
      (let [out (java.io.StringWriter.)
            plan (binding [*out* out] (pj/plan (pj/lay-rule-h d {:y-intercept 2 :color :g})))]
        (is (nil? (:legend plan)))
        (is (str/includes? (str out) "a rule or a band is drawn once"))))
    (testing "inherited from the pose: the points keep their legend, no message"
      (let [out (with-out-str
                  (is (= ["a" "b"]
                         (->> (-> d (pj/pose {:x :x :y :y :color :g}) pj/lay-point
                                  (pj/lay-rule-h {:y-intercept 2}) pj/plan)
                              :legend :entries (mapv :label)))))]
        (is (not (str/includes? out "rule or a band")))))))

(deftest categorical-colour-scale-names-color-type-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #":color-type :categorical"
                        (pj/plan (pj/scale (pj/lay-point {:x [1 2] :y [1 2] :n [4 6]} :x :y {:color :n})
                                           :color :categorical)))))
