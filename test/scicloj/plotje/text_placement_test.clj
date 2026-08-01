(ns scicloj.plotje.text-placement-test
  "Both backends must put a label in the same place. The SVG backend is the
   reference: it emits dominant-baseline=\"hanging\" plus the label's
   :text-anchor, and it turns a Rotate into an SVG rotate(). Membrane's
   Java2D backend read none of those, so every PNG drew its text leftwards
   from the origin, a line-height too low, and unrotated -- y tick labels ran
   into the panel and over the data, x tick labels sat half a label too far
   right, and :x-tick-angle did nothing at all. render.bufimg teaches Java2D
   the same contract; these are its guards.

   The assertions are pixel measurements because that is the only place the
   defect showed: the plan, the membrane tree and svg-summary were all
   correct while the picture was wrong."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj])
  (:import [java.awt.image BufferedImage]))

(def panel-bg [232 232 232])
(def grid-line [238 238 238])

(defn- rgb [^BufferedImage img x y]
  (let [v (.getRGB img (int x) (int y))]
    [(bit-and (bit-shift-right v 16) 0xff)
     (bit-and (bit-shift-right v 8) 0xff)
     (bit-and v 0xff)]))

(defn- ink?
  "Text and data marks are dark; the page, the panel and the grid lines are
   all far lighter, so one threshold separates ink from everything else."
  [img x y]
  (< (/ (reduce + (rgb img x y)) 3.0) 180))

(defn- panel-box
  "Bounding box of the panel background. Anti-aliased text throws off single
   background-coloured pixels, so a column or row counts only when it carries
   a long run of them."
  [^BufferedImage img]
  (let [w (.getWidth img)
        h (.getHeight img)
        long-run? (fn [coords pixel] (> (count (filter pixel coords)) 50))
        xs (filter #(long-run? (range h) (fn [y] (= panel-bg (rgb img % y)))) (range w))
        ys (filter #(long-run? (range w) (fn [x] (= panel-bg (rgb img x %)))) (range h))]
    {:left (first xs) :right (last xs) :top (first ys) :bottom (last ys)}))

(defn- runs
  "Group a sorted sequence of coordinates into contiguous runs, tolerating a
   `gap`-pixel break inside one run."
  [gap coords]
  (reduce (fn [acc c]
            (if (and (seq acc) (<= (- c (peek (peek acc))) gap))
              (conj (pop acc) (conj (peek acc) c))
              (conj acc [c])))
          [] coords))

(defn- centers [rs]
  (mapv #(/ (+ (double (first %)) (peek %)) 2.0) rs))

;; The x domain is far wider than the data, so the left fifth of the panel
;; holds no data mark and any ink there is a mispositioned tick label.
(def points-on-the-right
  (-> {:x [8 9 10] :y [1000000 2000000 3000000]}
      (pj/lay-point :x :y)
      (pj/scale :x {:domain [0 10]})))

(deftest y-tick-labels-stay-out-of-the-panel
  (testing "text-anchor \"end\" right-aligns them against the axis"
    (let [^BufferedImage img (pj/plot points-on-the-right {:format :bufimg})
          {:keys [left top bottom]} (panel-box img)]
      (is (seq (for [x (range left) y (range (.getHeight img))
                     :when (ink? img x y)]
                 x))
          "the labels are drawn in the axis margin")
      ;; Drawn from the left instead, every one of them started at the axis
      ;; and ran rightwards over the data.
      (is (empty? (for [x (range (inc left) (+ left 60))
                        y (range top bottom)
                        :when (ink? img x y)]
                    x))
          "no label ink may sit inside the panel"))))

(deftest y-tick-labels-center-on-their-gridlines
  (testing "the hanging baseline puts a label in the band reserved for it"
    (let [^BufferedImage img (pj/plot points-on-the-right {:format :bufimg})
          {:keys [left top bottom]} (panel-box img)
          grid-rows (filter #(= grid-line (rgb img (+ left 20) %)) (range top bottom))
          ;; Only the columns next to the axis, and only the rows beside the
          ;; panel: the y-axis title sits further left again, and the leftmost
          ;; x tick label reaches into these columns below the panel.
          label-rows (filter (fn [y] (some #(ink? img % y) (range (- left 40) left)))
                             (range top (inc bottom)))
          label-centers (centers (runs 2 label-rows))]
      (is (= 11 (count grid-rows)) "one gridline per y tick")
      (is (= (count grid-rows) (count label-centers)) "one label per gridline")
      ;; Java2D put the baseline a full line-height below the origin, which
      ;; left every label about four pixels below its own gridline.
      (is (every? #(<= (Math/abs (double %)) 1.0)
                  (map - label-centers grid-rows))
          "each label is centered on its gridline"))))

;; Every x tick label here is the same width, so the band of tick-label ink is
;; symmetric about the panel and its center reads the anchor directly.
(def evenly-labelled-points
  (-> {:x [1 2 3] :y [1000000 2000000 3000000]}
      (pj/lay-point :x :y)))

(deftest x-tick-labels-center-on-their-ticks
  (testing "text-anchor \"middle\" centers them"
    (let [^BufferedImage img (pj/plot evenly-labelled-points {:format :bufimg})
          {:keys [left right bottom]} (panel-box img)
          band-xs (filter (fn [x] (some #(ink? img x %)
                                        (range (inc bottom) (+ bottom 20))))
                          (range (.getWidth img)))]
      (is (seq band-xs) "the tick labels are drawn below the panel")
      ;; Drawn from the left instead, the whole band sat half a label -- about
      ;; nine pixels here -- to the right of the panel.
      (is (<= (Math/abs (- (/ (+ (double (first band-xs)) (last band-xs)) 2.0)
                           (/ (+ (double left) right) 2.0)))
              2.0)
          "the band of tick labels is centered on the panel"))))

(def category-bars
  (-> {:cat ["alpha" "beta" "gamma"] :v [3 5 2]}
      (pj/lay-bar :cat :v)))

(defn- tick-label-band-height
  "Rows of ink in the first band below the panel -- the tick labels, above
   the gap that separates them from the axis title."
  [pose]
  (let [^BufferedImage img (pj/plot pose {:format :bufimg})
        {:keys [left right bottom]} (panel-box img)]
    (->> (range (inc bottom) (.getHeight img))
         (filter (fn [y] (some #(ink? img % y) (range left right))))
         (runs 2)
         first
         count)))

(deftest x-tick-angle-rotates-the-labels
  (testing "a slanted label occupies a taller band than a horizontal one"
    ;; membrane.ui.Rotate carries children, so Java2D's default drawable impl
    ;; walked straight through it and drew them upright: :x-tick-angle changed
    ;; the SVG and did nothing whatever to the PNG.
    (let [upright (tick-label-band-height category-bars)
          slanted (tick-label-band-height
                   (pj/options category-bars {:x-tick-angle -45}))]
      (is (< upright 15) "an upright label is about one font-size tall")
      (is (> slanted (* 2 upright)) "a 45-degree label is far taller"))))
