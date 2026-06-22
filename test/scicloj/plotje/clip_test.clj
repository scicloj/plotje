(ns scicloj.plotje.clip-test
  "Panel marks are clipped to the panel box so geometry extending past
   the domain -- a line beyond a narrowed scale, say -- cannot paint
   outside its panel into a neighbour. The membrane stage expresses the
   clip as a Membrane scissor-view; the SVG backend realizes it as a
   <clipPath>, and the Java2D backend masks natively. Regression guard
   for https://github.com/scicloj/plotje/issues/16."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [membrane.ui :as ui])
  (:import [java.awt Color]))

(def points-data {:x [1 2 3 4 5] :y [10 20 15 25 18]})

;; A line whose data runs far past a tightened y-domain.
(defn out-of-domain-pose [line-color]
  (-> points-data
      (pj/lay-point :x :y {:color "blue"})
      (pj/lay-line {:color line-color :data {:x [1 5] :y [-200 300]}})
      (pj/scale :y {:type :linear :domain [0 30]})))

(deftest membrane-carries-a-scissor
  (testing "the panel's data marks are wrapped in a scissor-view at the drawing area"
    (let [m (pj/membrane (out-of-domain-pose "red"))
          scissor (->> (tree-seq coll? seq (ui/children m))
                       (filter #(instance? membrane.ui.ScissorView %))
                       first)]
      (is (some? scissor))
      ;; offset is the panel margin (drawing-area top-left), not [0 0]
      (is (= 2 (count (:offset scissor))))
      (is (every? pos? (:offset scissor)))
      (is (= 2 (count (:bounds scissor))))
      (is (every? pos? (:bounds scissor))))))

(deftest svg-emits-one-clip-per-panel
  (testing "single panel reports one clip region"
    (is (= 1 (:clips (pj/svg-summary (pj/plot (out-of-domain-pose "red")))))))
  (testing "stacked arrangement reports one clip region per panel"
    (let [arr (pj/arrange [(out-of-domain-pose "red")
                           (out-of-domain-pose "red")]
                          {:cols 1})
          s (pj/svg-summary (pj/plot arr))]
      (is (= 2 (:panels s)))
      (is (= 2 (:clips s))))))

(deftest clip-does-not-drop-data
  (testing "all 150 points render under a domain narrower than the data"
    ;; The clip masks; it does not filter rows. Every mark is still
    ;; emitted, just bounded to the panel.
    (let [s (pj/svg-summary
             (pj/plot (-> points-data
                          (pj/lay-point :x :y)
                          (pj/scale :y {:type :linear :domain [12 18]}))))]
      (is (= 5 (:points s))))))

(defn- red-pixel? [^java.awt.image.BufferedImage img x y]
  (let [c (Color. (.getRGB img x y))]
    (and (> (.getRed c) 150) (< (.getGreen c) 100) (< (.getBlue c) 100))))

(deftest line-does-not-bleed-into-sibling-panel
  (testing "a red out-of-domain line in the bottom panel stays out of the top panel"
    ;; Top panel: blue points only. Bottom panel: a red line whose data
    ;; runs far past a narrow domain. Before clipping, the bottom line's
    ;; geometry projected into the top panel's region; after clipping,
    ;; every red pixel must sit in the bottom half of the canvas.
    (let [top (-> points-data (pj/lay-point :x :y {:color "blue"}))
          bottom (out-of-domain-pose "red")
          img (pj/plot (pj/arrange [top bottom] {:cols 1}) {:format :bufimg})
          h (.getHeight img)
          w (.getWidth img)
          half (quot h 2)
          red-ys (for [y (range h) x (range w)
                       :when (red-pixel? img x y)]
                   y)]
      (is (seq red-ys) "the red line should be visible in the bottom panel")
      (is (every? #(>= % half) red-ys)
          "no red pixel may appear in the top panel's region"))))
