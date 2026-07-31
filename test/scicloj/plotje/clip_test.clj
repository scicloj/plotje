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

;; A filled area blends with the panel behind it, so the fill lands well
;; short of pure red. Match a reddish pixel instead of a saturated one.
(defn- reddish-pixel? [^java.awt.image.BufferedImage img x y]
  (let [c (Color. (.getRGB img x y))]
    (and (> (.getRed c) 200) (< (.getGreen c) 180) (< (.getBlue c) 180))))

(defn- panel-bg-pixel? [^java.awt.image.BufferedImage img x y]
  (= (Color. 232 232 232) (Color. (.getRGB img x y))))

(deftest density-under-a-narrow-domain-stays-in-the-panel
  (testing "a density fill does not paint over the y-axis tick labels"
    ;; Reported as https://github.com/scicloj/plotje/issues/24: with a
    ;; user :domain the density curve, which is estimated well past the
    ;; data, ran outside the panel and over the axis labels beside it.
    ;; The curve must still be drawn (it is only masked, not filtered),
    ;; but no part of it may sit left of the panel.
    (let [pose (-> {:v (vec (mapcat (fn [i] (repeat 10 (double i))) (range 1 20)))}
                   (pj/lay-density :v {:color "red"})
                   (pj/scale :x {:domain [8 12]}))
          ^java.awt.image.BufferedImage img (pj/plot pose {:format :bufimg})
          w (.getWidth img)
          h (.getHeight img)
          red-xs (for [x (range w) y (range h)
                       :when (reddish-pixel? img x y)]
                   x)
          panel-left (first (for [x (range w) y (range h)
                                  :when (panel-bg-pixel? img x y)]
                              x))]
      (is (seq red-xs) "the density fill should be visible")
      (is (some? panel-left) "the panel background should be visible")
      ;; Both edges are anti-aliased, so the first pixel of pure panel
      ;; colour sits a column inside the true edge. The regression this
      ;; guards spilled tens of pixels into the margin, so a one-pixel
      ;; allowance costs nothing.
      (is (>= (apply min red-xs) (dec (long panel-left)))
          "no fill pixel may appear left of the panel"))))

;; ---- clip-path ids ----

(defn- clip-ids [pose]
  (vec (re-seq #"plotje-clip-[a-z0-9]+" (pr-str (pj/plot pose)))))

(def clipped-pose
  (-> {:x [1 2 3] :y [1 2 3]}
      (pj/lay-point :x :y)
      (pj/scale :x {:domain [1.5 2.5]})))

(deftest clip-ids-are-derived-from-the-region-not-a-counter
  (testing "the same pose renders the same ids every time"
    ;; They came from a process-global counter, so a file's ids depended on
    ;; how many plots the JVM had drawn first: re-rendering the readme
    ;; rewrote every SVG with no visual change, hiding real diffs.
    (is (= (clip-ids clipped-pose) (clip-ids clipped-pose)))
    (is (seq (clip-ids clipped-pose))))
  (testing "rendering other plots first does not shift them"
    (let [before (clip-ids clipped-pose)]
      (dotimes [_ 3]
        (pj/plot (-> {:x [1 2] :y [3 4]} (pj/lay-line :x :y))))
      (is (= before (clip-ids clipped-pose)))))
  (testing "a different clip region gets a different id, so two plots on one
            page cannot borrow each other's clipPath"
    (is (not= (set (clip-ids clipped-pose))
              (set (clip-ids (-> clipped-pose (pj/options {:width 700 :height 300}))))))) 
  (testing "every id is referenced by a clip-path attribute"
    (let [svg (pr-str (pj/plot clipped-pose))]
      (doseq [id (distinct (clip-ids clipped-pose))]
        (is (.contains ^String svg (str "url(#" id ")")))))))
