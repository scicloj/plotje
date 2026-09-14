(ns scicloj.plotje.draw-order-test
  "A rule is drawn where its layer sits, not always last.

   `:rule-h`, `:rule-v`, `:band-h` and `:band-v` used to be carried on
   a panel's `:annotations` slot and composed after every data mark, so
   a rule written first was still painted over the points -- issue #48.
   They are ordinary layers now, so draw order is layer order.

   The tests read the raster. A layer list says which order the plan
   holds and nothing about which shape covers which, and the whole
   defect was a correct order drawn in the wrong sequence."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj])
  (:import [java.awt.image BufferedImage]))

(defn- rgb [^BufferedImage img x y]
  (let [v (.getRGB img (int x) (int y))]
    [(bit-and (bit-shift-right v 16) 0xff)
     (bit-and (bit-shift-right v 8) 0xff)
     (bit-and v 0xff)]))

(defn- white? [[r g b]] (and (> r 200) (> g 200) (> b 200)))
(defn- black? [[r g b]] (and (< r 60) (< g 60) (< b 60)))

(def ^:private datum
  "The one row every pose here draws, in the middle of a pinned domain
   so the rules cross it."
  {:height [5.0] :weight [5.0]})

(def ^:private dot
  "One large opaque white point, so the pixel at the datum says which
   of the two shapes was drawn second."
  {:size 30 :color "#ffffff" :alpha 1.0})

(defn- base
  "A pose with both domains pinned, so the datum sits at a place the
   scales put in the same spot whatever is layered on it."
  []
  (-> datum
      (pj/scale :x {:domain [0 10]})
      (pj/scale :y {:domain [0 10]})
      (pj/options {:width 400 :height 300})))

(defn- datum-pixel
  "The colour where the datum is drawn. `pj/to-drawing` reports that
   place through the same scales the renderer draws with, which the
   image's own centre does not: the panel is not centred on the canvas
   once the axis margin and the legend column are taken out."
  [pose]
  (let [[cx cy] (pj/to-drawing (-> pose pj/frames :panels first) 5.0 5.0)
        ^BufferedImage img (pj/plot pose {:format :bufimg})]
    (rgb img cx cy)))

(deftest a-band-is-drawn-in-the-order-it-was-written
  (let [band {:y-min 0.0 :y-max 10.0 :color "#000000" :alpha 1.0}]
    (testing "a band written after the mark covers it"
      (is (black? (datum-pixel (-> (base)
                                   (pj/lay-point :height :weight dot)
                                   (pj/lay-band-h band))))))

    (testing "and a band written before it is covered by the mark"
      ;; This is the half that could not be asked for at all: the
      ;; annotations slot composed after every layer whatever the pose
      ;; said.
      (is (white? (datum-pixel (-> (base)
                                   (pj/lay-band-h band)
                                   (pj/lay-point :height :weight dot))))))))

(deftest issue-48-two-rules-written-before-the-points
  ;; The shape @carstenbehring reported: a horizontal rule, a vertical
  ;; rule, then the points -- "so I would expect that points are on top
  ;; of lines, but I see the opposite". Both rules cross at the datum,
  ;; so the pixel there is the point's white if the points were drawn
  ;; last and the rules' black if they were not. The rules are drawn
  ;; wide so a pixel at the crossing is unambiguously theirs; the
  ;; defect was about order, not about width.
  (let [rules (fn [pose]
                (-> pose
                    (pj/lay-rule-h {:y-intercept 5.0 :color "#000000" :size 12})
                    (pj/lay-rule-v {:x-intercept 5.0 :color "#000000" :size 12})))]
    (testing "rules first, points second: the points win"
      (is (white? (datum-pixel (-> (base)
                                   rules
                                   (pj/lay-point :height :weight dot))))))

    (testing "points first, rules second: the rules win"
      (is (black? (datum-pixel (-> (base)
                                   (pj/lay-point :height :weight dot)
                                   rules)))))))
