(ns scicloj.plotje.offset-test
  "`:offset-x` and `:offset-y` shift a layer by a number of drawing units,
   after the scales have run.

   This is what `:nudge-x` and `:nudge-y` cannot be. A label has to clear
   the mark it labels by roughly the mark's radius, which is a length on
   the page: no data value is right for it across two scales, and on a
   categorical axis a data-space shift has nothing to add itself to --
   `:nudge-x` throws there.

   The tests read the raster, because an offset that reaches the plan and
   then fails to move any ink would pass every structural assertion."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj])
  (:import [java.awt.image BufferedImage]))

(defn- rgb [^BufferedImage img x y]
  (let [v (.getRGB img (int x) (int y))]
    [(bit-and (bit-shift-right v 16) 0xff)
     (bit-and (bit-shift-right v 8) 0xff)
     (bit-and v 0xff)]))

(defn- ink? [img x y]
  (< (/ (reduce + (rgb img x y)) 3.0) 170))

(defn- ink-near?
  [^BufferedImage img [cx cy] r]
  (let [w (.getWidth img) h (.getHeight img)]
    (boolean
     (some (fn [[x y]] (and (< -1 x w) (< -1 y h) (ink? img x y)))
           (for [x (range (- (int cx) r) (+ (int cx) r 1))
                 y (range (- (int cy) r) (+ (int cy) r 1))]
             [x y])))))

(defn- lone-point [opts]
  (-> {:height [2.0] :weight [7.0]}
      (pj/lay-point :height :weight opts)
      (pj/scale :x {:domain [0 10]})
      (pj/scale :y {:domain [0 10]})
      (pj/options {:width 400 :height 300})))

(defn- where [pose]
  (pj/to-drawing (-> pose pj/frames :panels first) 2.0 7.0))

;; ---- The offset moves the ink, by that much ----

(deftest an-offset-shifts-the-mark-on-the-page
  (testing "ink appears one offset to the right of where the datum is"
    (let [[cx cy] (where (lone-point {}))
          img (pj/plot (lone-point {:offset-x 40}) {:format :bufimg})]
      (is (ink-near? img [(+ cx 40) cy] 3)
          "no ink at the offset position")
      (is (not (ink-near? img [cx cy] 3))
          "ink still at the unoffset position"))))

(deftest offsets-work-on-both-axes
  (testing "y offsets shift down the page, as drawing coordinates run"
    (let [[cx cy] (where (lone-point {}))
          img (pj/plot (lone-point {:offset-x 30 :offset-y 25}) {:format :bufimg})]
      (is (ink-near? img [(+ cx 30) (+ cy 25)] 3)))))

(deftest an-offset-does-not-move-the-axis
  (testing "a drawing-space shift is not a data value, so no domain changes"
    (is (= (-> (lone-point {}) pj/plan :panels first
               ((juxt :x-domain :y-domain)))
           (-> (lone-point {:offset-x 40 :offset-y 25}) pj/plan :panels first
               ((juxt :x-domain :y-domain)))))))

;; ---- Where nudge cannot go ----

(def categorical-labels
  ;; The label text is words, not the numbers, so the assertion below
  ;; cannot be satisfied by the y-axis tick labels instead.
  (-> {:team ["red" "green" "blue"] :score [3 5 4] :tag ["low" "high" "mid"]}
      (pj/lay-bar :team :score)
      (pj/lay-text {:text :tag :offset-y -8 :align-x :center})
      (pj/options {:width 400 :height 300})))

(defn- layer-offsets
  "The :offset-x each of a plan's first panel's layers carries."
  [pose k]
  (mapv k (:layers (first (:panels (pj/plan pose))))))

(deftest a-data-space-nudge-refuses-a-categorical-axis
  (testing "the gap :offset-* exists to fill"
    (is (thrown-with-msg?
         Exception #"does not apply to a categorical"
         (pj/plan (pj/lay-point {:team ["red" "green"] :score [3 5]}
                                :team :score {:nudge-x 0.2}))))))

(deftest an-offset-applies-on-a-categorical-axis
  (testing "a label is lifted clear of its bar, on an axis with no data units"
    (is (= [nil -8] (layer-offsets categorical-labels :offset-y)))
    (let [texts (:texts (pj/svg-summary (pj/plot categorical-labels)))]
      (is (= 3 (count (filter #{"low" "high" "mid"} texts)))))))

;; ---- It is not a text feature ----

(deftest every-layer-type-takes-an-offset
  (testing ":offset-x is a universal layer option, not one mark's parameter"
    ;; Behaviourally, not by reading a list: each layer type is built with
    ;; an offset and the plan layer has to come back carrying it.
    (doseq [t [:point :line :step :area :text :label]]
      (is (= [7]
             (layer-offsets (pj/lay {:height [1 2 3] :weight [2 4 6]
                                     :tag ["a" "b" "c"]}
                                    t
                                    {:x :height :y :weight :text :tag
                                     :offset-x 7})
                            :offset-x))
          (str "layer type " t " lost its offset")))))

(deftest the-offset-options-are-documented
  (testing "both appear in the public option docs"
    (is (contains? pj/layer-option-docs :offset-x))
    (is (contains? pj/layer-option-docs :offset-y))))

(deftest a-line-layer-shifts-too
  (testing "the offset is applied where every mark passes, not inside one"
    (let [line (fn [opts] (-> {:height [1 2 3] :weight [2 4 6]}
                              (pj/lay-line :height :weight opts)
                              (pj/scale :x {:domain [0 4]})
                              (pj/scale :y {:domain [0 8]})
                              (pj/options {:width 400 :height 300})))
          [cx cy] (pj/to-drawing (-> (line {}) pj/frames :panels first) 2 4)
          img (pj/plot (line {:offset-y 40}) {:format :bufimg})]
      (is (ink-near? img [cx (+ cy 40)] 3))
      (is (not (ink-near? img [cx cy] 3))))))

;; ---- The offset and the room reserved for text ----

(def labelled-at-the-edge
  "The rightmost label is the one at risk: it is anchored at the largest
   x and then pushed further right still."
  (fn [opts]
    (-> {:height [1 2 3] :weight [2 4 6] :tag ["one" "two" "three"]}
        (pj/lay-point :height :weight {:size 8})
        (pj/lay-text :height :weight (merge {:text :tag} opts))
        (pj/options {:width 600 :height 400}))))

(deftest an-offset-label-still-gets-room-made-for-it
  (testing "the domain widens by the offset as well as by the text"
    ;; Found by looking at the picture: the domain was fitted to where the
    ;; text was anchored, not to where the offset then put it, so the last
    ;; label ran off the panel edge while every assertion passed.
    (let [[_ plain-hi] (-> (labelled-at-the-edge {}) pj/plan
                           :panels first :x-domain)
          [_ offset-hi] (-> (labelled-at-the-edge {:offset-x 10}) pj/plan
                            :panels first :x-domain)]
      (is (> offset-hi plain-hi)
          "an offset label asks for no more room than an unoffset one"))))

(deftest an-offset-label-is-drawn-in-full
  (testing "no label ink reaches the right edge of the image"
    (let [^BufferedImage img (pj/plot (labelled-at-the-edge {:offset-x 10})
                                      {:format :bufimg})
          w (.getWidth img)
          h (.getHeight img)]
      (is (empty? (for [x (range (- w 2) w)
                        y (range h)
                        :when (ink? img x y)]
                    [x y]))))))

;; ---- Refusals ----

(deftest an-offset-is-a-number-not-a-column
  (testing "one value shifts the whole layer, so a column has nothing to mean"
    (is (thrown-with-msg?
         Exception #"must be a number of drawing units"
         (-> {:height [1] :weight [2]}
             (pj/lay-point :height :weight {:offset-x :height})
             pj/plan)))))

(deftest the-refusal-points-at-nudge
  (testing "the error names the data-space option, since that is the other half"
    (is (thrown-with-msg?
         Exception #":nudge-y"
         (-> {:height [1] :weight [2]}
             (pj/lay-point :height :weight {:offset-y "8"})
             pj/plan)))))
