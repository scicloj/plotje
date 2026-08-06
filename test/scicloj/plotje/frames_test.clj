(ns scicloj.plotje.frames-test
  "`pj/frames` publishes where a plot's panels sit on the canvas and how to
   get between data space and drawing space.

   The claim that matters is that it agrees with the renderer. A mapping
   that is internally consistent but half a margin away from where the ink
   lands would pass every structural assertion and be useless for the one
   thing it exists for, so the central test here renders a point to a
   raster and looks for ink where `:to-drawing` said it would be."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [scicloj.plotje.api :as pj])
  (:import [java.awt.image BufferedImage]))

(defn- one-panel [pose]
  (-> pose pj/frames :panels first))

(defn- rgb [^BufferedImage img x y]
  (let [v (.getRGB img (int x) (int y))]
    [(bit-and (bit-shift-right v 16) 0xff)
     (bit-and (bit-shift-right v 8) 0xff)
     (bit-and v 0xff)]))

(defn- ink?
  "A drawn mark is dark; the panel behind it is pale."
  [img x y]
  (< (/ (reduce + (rgb img x y)) 3.0) 170))

(defn- ink-near?
  "Any ink within `r` of [cx cy], staying inside the image."
  [^BufferedImage img [cx cy] r]
  (let [w (.getWidth img) h (.getHeight img)]
    (boolean
     (some (fn [[x y]] (and (< -1 x w) (< -1 y h) (ink? img x y)))
           (for [x (range (- (int cx) r) (+ (int cx) r 1))
                 y (range (- (int cy) r) (+ (int cy) r 1))]
             [x y])))))

;; ---- The mapping agrees with the ink ----

(def lone-point
  "One point, at a position with nothing else near it. The domain is
   pinned so the test does not depend on padding."
  (-> {:height [2.0] :weight [7.0]}
      (pj/lay-point :height :weight)
      (pj/scale :x {:domain [0 10]})
      (pj/scale :y {:domain [0 10]})
      (pj/options {:width 400 :height 300})))

(deftest to-drawing-lands-where-the-renderer-draws
  (testing "the published mapping points at the actual mark"
    (let [at (pj/to-drawing (one-panel lone-point) 2.0 7.0)
          img (pj/plot lone-point {:format :bufimg})]
      (is (ink-near? img at 3)
          (str "no ink within 3 units of " (vec at))))))

(deftest the-mapping-is-not-merely-self-consistent
  (testing "a position the mark is NOT at has no mark on it"
    (let [elsewhere (pj/to-drawing (one-panel lone-point) 8.0 2.0)
          img (pj/plot lone-point {:format :bufimg})]
      (is (not (ink-near? img elsewhere 3))))))

;; ---- Both directions ----

(deftest to-data-inverts-to-drawing
  (testing "a data position survives the round trip"
    (let [p (one-panel lone-point)
          [cx cy] (pj/to-drawing p 3.5 6.25)
          [dx dy] (pj/to-data p cx cy)]
      (is (< (abs (- 3.5 dx)) 1e-9))
      (is (< (abs (- 6.25 dy)) 1e-9)))))

(deftest polar-offers-no-inverse
  (testing "a projection that folds x and y together does not pretend to invert"
    (let [p (one-panel (-> {:g ["a" "b" "c"] :v [1 2 3]}
                           (pj/lay-bar :g :v)
                           (pj/coord :polar)))]
      (is (false? (:invertible? p)))
      ;; x is a category here, so the forward direction is asked in its terms
      (is (some? (pj/to-drawing p "a" 2)))
      (is (thrown-with-msg? Exception #"has no inverse" (pj/to-data p 100.0 100.0))))))

(deftest the-panel-entries-are-data
  (testing "no functions inside, so a frames result prints and round-trips"
    (let [f (pj/frames lone-point)]
      (is (= f (read-string (pr-str f))))
      (is (true? (-> f :panels first :invertible?))))))

(deftest the-dataset-arity-agrees-with-the-scalar-one
  (testing "a column of positions maps the same as one point at a time"
    (let [p    (one-panel lone-point)
          data {:x [1.0 3.5 9.0] :y [2.0 6.25 9.0]}
          rows (fn [ds] (mapv (juxt :x :y) (tc/rows ds :as-maps)))]
      (is (= (mapv (fn [x y] (pj/to-drawing p x y)) (:x data) (:y data))
             (rows (pj/to-drawing p data))))
      (is (= (mapv (fn [x y] (pj/to-data p x y)) (:x data) (:y data))
             (rows (pj/to-data p data)))))))

(deftest the-dataset-arity-answers-with-a-dataset
  (testing "columns keep their names, and numbers stay numbers"
    (let [p   (one-panel lone-point)
          out (pj/to-drawing p {:x [1.0 3.5] :y [2.0 6.25]})]
      (is (tc/dataset? out))
      (is (= [:x :y] (vec (tc/column-names out))))
      (is (= 2 (tc/row-count out)))
      (is (= :float64 (dtype/elemwise-datatype (out :x))))
      (is (= :float64 (dtype/elemwise-datatype ((pj/to-data p out) :x)))
          "a continuous axis reads back as numbers, not as boxed objects"))))

(deftest a-dataset-round-trips-through-both-directions
  (testing "column in, column out, and back to where it started"
    (let [p    (one-panel lone-point)
          data (tc/dataset {:x [1.0 3.5 9.0] :y [2.0 6.25 9.0]})
          back (pj/to-data p (pj/to-drawing p data))]
      (is (every? #(< (abs %) 1e-9) (dfn/- (back :x) (data :x))))
      (is (every? #(< (abs %) 1e-9) (dfn/- (back :y) (data :y)))))))

(deftest flip-swaps-the-axes-in-both-directions
  (testing "under :flip the data x runs down the drawing y"
    (let [flipped (-> {:height [2.0] :weight [7.0]}
                      (pj/lay-point :height :weight)
                      (pj/scale :x {:domain [0 10]})
                      (pj/scale :y {:domain [0 10]})
                      (pj/coord :flip)
                      (pj/options {:width 400 :height 300}))
          p (one-panel flipped)
          [dx dy] (apply pj/to-data p (pj/to-drawing p 2.0 7.0))]
      (is (< (abs (- 2.0 dx)) 1e-9))
      (is (< (abs (- 7.0 dy)) 1e-9)))))

;; ---- The shape of the arguments ----

(deftest one-point-passed-as-a-pair-is-refused
  (testing "the natural mistake gets an error that names it"
    ;; (to-drawing panel [2 5]) reads as one point and means two, each of
    ;; them a bare number. Left to the mapping it failed with "nth not
    ;; supported on this type: Long", which names neither the argument nor
    ;; the call.
    (let [p (one-panel lone-point)]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"takes either two coordinates or a dataset of them"
           (pj/to-drawing p [2 5])))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"not a collection of pairs"
           (pj/to-drawing p [[2 5] [3 6]]))
          "the older pair-collection shape names the dataset that replaced it")
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"takes either two coordinates or a dataset of them"
           (pj/to-data p [100.0 100.0])))
      (testing "while the two shapes that mean something are accepted"
        (is (= 1 (tc/row-count (pj/to-drawing p {:x [2] :y [5]}))))
        (is (= 2 (count (pj/to-drawing p 2 5))))))))

;; ---- Frames ----

(defn- inside?
  "Rectangle `a` sits within rectangle `b`."
  [[ax ay aw ah] [bx by bw bh]]
  (and (>= ax bx) (>= ay by)
       (<= (+ ax aw) (+ bx bw))
       (<= (+ ay ah) (+ by bh))))

(deftest the-frames-nest
  (testing "drawing area inside panel box inside canvas"
    ;; The canvas comes from the top of the result, not from the panel.
    ;; A panel used to repeat it, and the copy was the cell's dimensions
    ;; on a composite -- so this assertion and the composite one below
    ;; could each pass while disagreeing about what the canvas was.
    (let [{:keys [canvas panels]} (pj/frames lone-point)
          {:keys [frames]} (first panels)]
      (is (inside? (:drawing-area frames) (:panel-box frames)))
      (is (inside? (:panel-box frames) canvas))
      (is (nil? (:canvas frames))
          "a panel reports the canvas, which belongs to the plot"))))

(deftest the-drawing-area-is-the-panel-box-less-its-margin
  (testing "the margin is what separates them, on all four sides"
    (let [plan (pj/plan lone-point)
          m (:margin plan)
          {:keys [frames]} (one-panel lone-point)
          [px py pw ph] (:panel-box frames)
          [dx dy dw dh] (:drawing-area frames)]
      (is (== (+ px m) dx))
      (is (== (+ py m) dy))
      (is (== (- pw m m) dw))
      (is (== (- ph m m) dh)))))

(deftest a-mark-lands-inside-its-own-drawing-area
  (testing "a data position maps into the rectangle data marks are clipped to"
    (let [p (one-panel lone-point)
          [cx cy] (pj/to-drawing p 2.0 7.0)]
      (is (inside? [cx cy 0 0] (-> p :frames :drawing-area))))))

;; ---- More than one panel ----

(def faceted
  (-> {:height [1 2 3 4 5 6] :weight [2 4 6 8 10 12]
       :species ["a" "a" "b" "b" "c" "c"]}
      (pj/lay-point :height :weight)
      (pj/facet :species)
      (pj/options {:width 600 :height 300})))

(deftest every-panel-gets-its-own-frame
  (testing "a facet grid reports one entry per panel, at distinct origins"
    (let [panels (:panels (pj/frames faceted))]
      (is (= 3 (count panels)))
      (is (= 3 (count (distinct (map #(-> % :frames :panel-box) panels))))))))

(deftest facet-panels-do-not-overlap
  (testing "no two drawing areas intersect"
    (let [areas (map #(-> % :frames :drawing-area) (:panels (pj/frames faceted)))]
      (doseq [[a b] (for [x areas y areas :when (not= x y)] [x y])]
        (let [[ax ay aw ah] a
              [bx by _ _] b]
          (is (or (>= bx (+ ax aw)) (>= by (+ ay ah))
                  (<= bx ax) (<= by ay))
              (str a " overlaps " b)))))))

(deftest each-facet-panel-maps-into-itself
  (testing "a data position maps into the drawing area of its own panel"
    (doseq [p (:panels (pj/frames faceted))]
      (let [[cx cy] (pj/to-drawing p 3 6)]
        (is (inside? [cx cy 0 0] (-> p :frames :drawing-area)))))))

;; ---- Composites ----

(def composed
  (pj/arrange [(-> {:height [1 2] :weight [3 4]} (pj/lay-point :height :weight))
               (-> {:height [1 2] :weight [3 4]} (pj/lay-line :height :weight))]
              {:width 700 :height 300}))

(deftest every-frame-of-a-composite-fits-the-whole-image
  (testing "each cell's rectangles nest inside the image, not inside its cell"
    ;; The failure this replaces: a panel's own :canvas was built from
    ;; its sub-plan's dimensions, so on a 700-wide composite every panel
    ;; called the canvas 350 wide, and the second cell's panel box at
    ;; x = 392.5 fell outside the rectangle it was nominally inside.
    (let [{:keys [canvas panels]} (pj/frames composed)
          [_ _ cw _] canvas]
      (is (= [0.0 0.0 700.0 300.0] canvas))
      (is (> (apply max (map #(let [[x _ w _] (-> % :frames :panel-box)] (+ x w))
                             panels))
             (/ cw 2.0))
          "no panel reaches past the midpoint, so nothing tests the far cell")
      (doseq [p panels]
        (is (inside? (-> p :frames :panel-box) canvas))
        (is (inside? (-> p :frames :drawing-area) (-> p :frames :panel-box)))))))

(deftest a-composite-reports-canvas-coordinates
  (testing "cells are offset into one coordinate system, not nested ones"
    (let [{:keys [canvas panels]} (pj/frames composed)]
      (is (= 2 (count panels)))
      (is (every? #(inside? (-> % :frames :panel-box) canvas) panels))
      ;; The second cell sits to the right of the first.
      (let [[x1] (-> panels first :frames :panel-box)
            [x2] (-> panels second :frames :panel-box)]
        (is (> x2 x1))))))

;; ---- Entry points ----

(deftest a-plan-and-its-pose-agree
  (testing "frames takes either, and answers the same"
    (let [from-pose (pj/frames lone-point)
          from-plan (pj/frames (pj/plan lone-point))]
      (is (= (:canvas from-pose) (:canvas from-plan)))
      (is (= (map :frames (:panels from-pose))
             (map :frames (:panels from-plan)))))))

;; ---- The margin the mapping reads back ----

(defn- margin-gaps
  "The distance from a panel box's edge to its drawing area's, on each
   of the four sides: left, top, right, bottom."
  [panel]
  (let [[bx by bw bh] (-> panel :frames :panel-box)
        [dx dy dw dh] (-> panel :frames :drawing-area)]
    [(- dx bx) (- dy by)
     (- (+ bx bw) (+ dx dw))
     (- (+ by bh) (+ dy dh))]))

(deftest the-margin-is-the-same-on-all-four-sides
  (testing "every plot shape leaves an equal gap around its drawing area"
    ;; `pj/to-drawing` reads the margin back off these two rectangles --
    ;; it takes the left gap and builds both scale ranges from it, the
    ;; way the renderer builds them from the plan's single :margin. That
    ;; is exact only while the four gaps agree. If the layout ever gives
    ;; a panel an asymmetric margin, this fails and names the assumption
    ;; rather than leaving every position on the right and bottom wrong.
    (doseq [[label pose]
            [["plain" lone-point]
             ["facet" faceted]
             ["composite" composed]
             ["rotated ticks" (-> {:g ["a longish name" "another one"] :v [1 2]}
                                  (pj/lay-bar :g :v)
                                  (pj/options {:x-tick-angle -45}))]
             ["pinned panel size" (-> {:x [1 2] :y [3 4]}
                                      (pj/lay-point :x :y)
                                      (pj/options {:panel-width 300
                                                   :panel-height 200}))]]]
      (doseq [p (:panels (pj/frames pose))]
        (let [gaps (margin-gaps p)]
          (is (apply == gaps)
              (str label ": gaps differ around the drawing area: " gaps)))))))

;; ---- What the two mapping directions cost ----

(deftest a-mapping-call-builds-its-scales-once
  (testing "the collection arity does not rebuild them per point"
    ;; Building a wadogo scale takes microseconds; applying one takes
    ;; nanoseconds. A per-point rebuild would not change a single
    ;; answer, so nothing else here would catch it -- but mapping ten
    ;; thousand points would cost a scale build ten thousand times.
    ;; Ten thousand points against one, per point, with a wide margin
    ;; for a loaded machine. A ratio, so a slow machine slows both sides.
    (let [panel (one-panel lone-point)
          data  (tc/dataset {:x (double-array (map #(* 0.001 %) (range 10000)))
                             :y (double-array (map #(* 0.0005 %) (range 10000)))})
          time-of (fn [f]
                    (dotimes [_ 20] (f))
                    (let [t0 (System/nanoTime)]
                      (dotimes [_ 20] (f))
                      (/ (- (System/nanoTime) t0) 20.0)))
          per-point (/ (time-of #(pj/to-drawing panel data)) 10000.0)
          one-call  (time-of #(pj/to-drawing panel 2.0 7.0))]
      (is (< per-point (/ one-call 10.0))
          (str "a point inside a dataset costs " (long per-point)
               " ns against " (long one-call)
               " ns for a call of its own; the scales look rebuilt per point")))))
