(ns scicloj.plotje.text-fit-test
  "Text marks are sized in pixels, so a data label sitting at the largest
   value is drawn past the edge of the drawing area and cut off there --
   the top bar of a horizontal bar chart labelled 4623 instead of 462300
   (issue #18). Relaxing the clip cannot fix it: layout gives the panel
   every pixel no legend takes, so there is nothing on the far side of
   that edge. The axis has to carry a little more data range instead.

   Two guards live here. The first is the fit itself, which widens a
   numeric domain until every text mark fits. The second is the x-tick
   label clamp: a tick near the end of the axis centres its label half
   outside the panel, and widening a domain puts a tick there more often.

   The assertions read the rendered SVG, because the pose, the draft and
   svg-summary were all correct while the picture was wrong."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj])
  (:import [java.awt.image BufferedImage]))

;; ---- Reading the rendered SVG ----

(defn- walk
  "Every element of an SVG hiccup tree, each carrying its absolute
   position. Plotje nests a translate per drawing region, so an
   element's own coordinates are relative to its ancestors'."
  [node dx dy]
  (when (vector? node)
    (let [[tag attrs] node
          attrs (when (map? attrs) attrs)
          [dx dy] (if-let [[_ x y] (some->> (:transform attrs)
                                            (re-matches #"translate\(([-\d.]+),([-\d.]+)\)"))]
                    [(+ dx (parse-double x)) (+ dy (parse-double y))]
                    [dx dy])]
      (cons (assoc (or attrs {}) :tag tag :text (last node) :abs-x dx :abs-y dy)
            (mapcat #(walk % dx dy) (rest node))))))

(defn- elements [pose]
  (walk (pj/plot pose) 0.0 0.0))

(defn- px [v] (when v (parse-double (str v))))

(defn- drawing-area
  "Left and right edges of the grey panel background, which is drawn at
   exactly the rectangle the panel clips its data marks to."
  [els]
  (let [bg (->> els
                (filter #(and (= :rect (:tag %)) (= "rgb(232,232,232)" (:fill %))))
                (sort-by #(- (px (:width %))))
                first)]
    [(:abs-x bg) (+ (:abs-x bg) (px (:width bg)))]))

(defn- label-boxes
  "Left and right edges of every label box. A label box is the only
   rounded rect that is wider than it is tall; a point marker is always
   drawn on a square bounding box. Each box is drawn twice, once filled
   white and once stroked as its border, so the pairs are deduplicated."
  [els]
  (distinct
   (for [{:keys [tag rx width height abs-x]} els
         :when (and (= :rect tag) rx width height
                    (> (px width) (px height)))]
     [abs-x (+ abs-x (px width))])))

;; The chart from issue #18: the largest bar's label is the one that runs
;; past the axis. Short category names keep the y-label pad small, so the
;; panel takes nearly the whole width and there is no slack to absorb it.
(def parking
  {:violation ["Meter Expired" "Over Time Limit" "Other"]
   :tickets [462300 181444 122934]})

(defn- labelled-bars [opts]
  (-> parking
      (pj/pose :tickets :violation)
      (pj/lay-bar)
      (pj/lay-label {:text :tickets})
      (pj/options (merge {:width 681 :height 408} opts))))

(defn- x-domain [pose]
  (-> pose pj/plan :panels first :x-domain))

(deftest a-data-label-at-the-largest-value-is-drawn-in-full
  (testing "every label box lies inside the region the panel clips to"
    (let [els (elements (labelled-bars {}))
          [area-left area-right] (drawing-area els)
          boxes (label-boxes els)]
      (is (= 3 (count boxes)))
      (is (every? (fn [[l r]] (and (>= l area-left) (<= r area-right))) boxes)
          (str "label boxes " (vec boxes) " must fit in " [area-left area-right]))))
  (testing "and the widening is what puts it there"
    (let [els (elements (labelled-bars {:fit-text-domain false}))
          [_ area-right] (drawing-area els)]
      (is (some (fn [[_ r]] (> r area-right)) (label-boxes els))
          "without the fit the largest label still overruns the drawing area"))))

(deftest the-fit-widens-only-as-far-as-the-text-needs
  (let [fitted (second (x-domain (labelled-bars {})))
        unfitted (second (x-domain (labelled-bars {:fit-text-domain false})))]
    (is (> fitted unfitted))
    (testing "a label is worth a few percent of the axis, not a doubling"
      (is (< (/ (- fitted unfitted) unfitted) 0.1)))
    (testing "the low end is untouched -- nothing overflows there"
      (is (= (first (x-domain (labelled-bars {})))
             (first (x-domain (labelled-bars {:fit-text-domain false}))))))))

(deftest the-fit-leaves-a-pinned-domain-alone
  (testing "asking for a domain means that domain, cut labels and all"
    (is (= [0 500000]
           (-> parking
               (pj/pose :tickets :violation)
               (pj/lay-bar)
               (pj/lay-label {:text :tickets})
               (pj/scale :x {:domain [0 500000]})
               (pj/options {:width 681 :height 408})
               x-domain)))))

(deftest a-plot-without-text-marks-keeps-its-domain
  (let [bars (-> parking (pj/pose :tickets :violation) (pj/lay-bar)
                 (pj/options {:width 681 :height 408}))]
    (is (= (x-domain bars)
           (x-domain (-> parking (pj/pose :tickets :violation) (pj/lay-bar)
                         (pj/options {:width 681 :height 408
                                      :fit-text-domain false})))))))

(deftest the-fit-follows-the-value-axis-under-coord-flip
  (testing ":coord :flip puts the value axis on screen x, and the labels with it"
    (let [flipped (fn [opts]
                    (-> parking
                        (pj/pose :violation :tickets)
                        (pj/lay-bar)
                        (pj/lay-label {:text :tickets})
                        (pj/coord :flip)
                        (pj/options (merge {:width 681 :height 408} opts))
                        x-domain))]
      (is (> (second (flipped {}))
             (second (flipped {:fit-text-domain false})))))))

(deftest the-fit-widens-the-value-axis-above-a-bar
  (testing "a label anchored above the tallest bar needs headroom on y"
    (let [tall (fn [opts]
                 (-> {:cat ["a" "b"] :n [100 20]}
                     (pj/pose :cat :n)
                     (pj/lay-bar)
                     (pj/lay-label {:text :n :align-y :bottom})
                     (pj/options (merge {:width 400 :height 300} opts))
                     pj/plan :panels first :y-domain))]
      (is (> (second (tall {})) (second (tall {:fit-text-domain false})))))))

(deftest a-categorical-domain-is-never-widened
  (testing "widening means arithmetic on the domain, which categories have none of"
    (is (= ["Meter Expired" "Over Time Limit" "Other"]
           (-> parking
               (pj/pose :violation :tickets)
               (pj/lay-bar)
               (pj/lay-label {:text :tickets})
               (pj/options {:width 681 :height 408})
               pj/plan :panels first :x-domain)))))

;; ---- The x-tick label clamp ----

(defn- rgb [^BufferedImage img x y]
  (let [v (.getRGB img (int x) (int y))]
    [(bit-and (bit-shift-right v 16) 0xff)
     (bit-and (bit-shift-right v 8) 0xff)
     (bit-and v 0xff)]))

(defn- ink?
  "Tick labels are dark grey; the page behind them is white."
  [img x y]
  (< (/ (reduce + (rgb img x y)) 3.0) 180))

;; A domain whose top end is a round number puts a tick at the very end of
;; the axis, where a centered label hangs half outside the panel -- and the
;; panel's right edge is the image's right edge.
(def tick-at-the-axis-end
  (-> parking
      (pj/pose :tickets :violation)
      (pj/lay-bar)
      (pj/scale :x {:domain [0 500000]})
      (pj/options {:width 681 :height 408 :fit-text-domain false})))

(deftest the-last-x-tick-label-stays-inside-the-panel
  (testing "no tick-label ink reaches the right edge of the image"
    (let [^BufferedImage img (pj/plot tick-at-the-axis-end {:format :bufimg})
          w (.getWidth img)
          h (.getHeight img)]
      (is (empty? (for [x (range (- w 2) w)
                        y (range h)
                        :when (ink? img x y)]
                    [x y]))))))
