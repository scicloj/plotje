(ns scicloj.plotje.position-space-test
  "A position is a quantity in a space.

   Two things follow, and this namespace pins both. First, a position may
   be a literal, as every appearance aesthetic has always allowed --
   `{:color \"red\"}` needed no column, and now neither does `{:x 2.0}`.
   Second, `:in` names the space those numbers are in: `:data` by default,
   or `:drawing-area` for a mark placed on the panel rather than in the
   data.

   The distinction that has to hold: a data-space position is a datum, so
   it trains the axis; a drawing-space position is a page measurement, so
   it must not. A drawing-space note that stretched the axis to 160 would
   still draw, and would still look plausible, so the domain assertions
   below matter as much as the ink ones."
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

(defn- ink-in?
  "Any ink inside the box [x y w h]."
  [^BufferedImage img [bx by bw bh]]
  (let [w (.getWidth img) h (.getHeight img)]
    (boolean
     (some (fn [[x y]] (and (< -1 x w) (< -1 y h) (ink? img x y)))
           (for [x (range (int bx) (int (+ bx bw)))
                 y (range (int by) (int (+ by bh)))]
             [x y])))))

(def scatter
  (-> {:height [1 2 3] :weight [2 4 6]}
      (pj/lay-point :height :weight)
      (pj/options {:width 400 :height 300})))

(defn- x-domain [pose]
  (:x-domain (first (:panels (pj/plan pose)))))

(defn- marks [pose]
  (mapv :mark (:layers (first (:panels (pj/plan pose))))))

;; ---- Literal positions, in data space ----

(deftest a-position-may-be-a-literal
  (testing "no invented dataset needed to place a note"
    (let [noted (pj/lay-text scatter {:x 2.0 :y 5.0 :text "note"})]
      (is (= [:point :text] (marks noted)))
      (is (some #{"note"} (:texts (pj/svg-summary (pj/plot noted))))))))

(deftest a-literal-position-is-a-datum-and-trains-the-axis
  (testing "an annotation beyond the data widens the axis to hold it"
    (is (= [0.9 3.1] (x-domain scatter)))
    (let [[lo hi] (x-domain (pj/lay-text scatter {:x 9.0 :y 5.0 :text "far"}))]
      (is (< lo 1.0))
      (is (> hi 9.0)))))

(deftest a-literal-text-is-the-text
  (testing "a string names a column everywhere else, but there is no column here"
    (let [noted (pj/lay-text scatter {:x 2.0 :y 5.0 :text "R&D"})]
      (is (some #{"R&D"} (:texts (pj/svg-summary (pj/plot noted))))))))

(deftest a-literal-x-requires-a-literal-y
  (testing "one coordinate given as a value needs the other given the same way"
    ;; A literal value draws one mark, and the layer gets a one-row
    ;; dataset holding it. A column beside it would be read against that
    ;; row; a coordinate left out is inherited from the pose and read
    ;; there too. Both used to fail downstream against the synthesized
    ;; dataset, reporting a column missing from something the caller
    ;; never wrote.
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"literal value for :x and a column for :y"
         (pj/lay-text scatter {:x 2.0 :y :weight :text "n"})))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"literal value for :x but no :y"
         (pj/lay-text scatter {:x 2.0 :text "n"}))))
  (testing "and a literal value does not combine with the layer's own data"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"carrying its own :data"
         (pj/lay-text scatter {:x 2.0 :y 5.0 :text :t :data {:t ["n"]}}))))
  (testing "while both as values still works"
    (is (= [:point :text] (marks (pj/lay-text scatter {:x 2.0 :y 5.0 :text "n"}))))))

(deftest a-literal-still-rejects-what-is-neither
  (testing "the helpful error survives for values that are no kind of position"
    (is (thrown-with-msg?
         Exception #"must be a column reference"
         (pj/lay-text scatter {:x [:a :b] :y 1 :text "x"})))))

;; ---- Drawing space ----

(def noted
  (pj/lay-text scatter {:in :drawing-area :x 160 :y 30 :text "peak season"}))

(deftest a-drawing-space-layer-is-still-drawn
  (testing "leaving the domain computation does not mean leaving the panel"
    (is (= [:point :text] (marks noted)))
    (is (= [nil :drawing-area]
           (mapv :in (:layers (first (:panels (pj/plan noted)))))))
    (is (some #{"peak season"} (:texts (pj/svg-summary (pj/plot noted)))))))

(deftest a-drawing-space-position-does-not-train-the-axis
  (testing "160 drawing units is not a data value of 160"
    (is (= (x-domain scatter) (x-domain noted)))))

(deftest a-drawing-space-text-does-not-reserve-data-room
  (testing ":fit-text-domain leaves it alone too"
    ;; A text mark near the top of the data normally widens the domain to
    ;; be drawn in full. A drawing-space one cannot be helped that way --
    ;; widening the axis would not move it -- so it must not ask.
    (is (= (x-domain scatter)
           (x-domain (pj/lay-text scatter {:in :drawing-area
                                           :x 380 :y 30 :text "wide label here"}))))))

(deftest a-drawing-space-mark-lands-in-its-drawing-area
  (testing "the position is measured from the drawing area's top left"
    (let [area (-> noted pj/frames :panels first :frames :drawing-area)
          [ax ay] area
          img (pj/plot noted {:format :bufimg})]
      ;; The text is anchored at the drawing area's origin plus [160 30],
      ;; and extends right and down from there.
      (is (ink-in? img [(+ ax 160) (+ ay 22) 90 20])
          "no ink where the drawing-space position points")
      (is (not (ink-in? img [(+ ax 160) (+ ay 90) 90 20]))
          "ink well below where it was placed"))))

(deftest the-same-numbers-mean-different-places-in-the-two-spaces
  (testing "the space is what decides, not the numbers"
    (let [in-data (pj/lay-text scatter {:x 2.0 :y 5.0 :text "aaa"})
          in-drawing (pj/lay-text scatter {:in :drawing-area :x 2.0 :y 5.0 :text "aaa"})
          at-data (pj/to-drawing (-> in-data pj/frames :panels first) 2.0 5.0)
          [ax ay] (-> in-drawing pj/frames :panels first :frames :drawing-area)]
      ;; Data space puts it in the middle of the panel; drawing space puts
      ;; it two units from the panel background's corner.
      (is (> (first at-data) (+ ax 100)))
      (is (ink-in? (pj/plot in-drawing {:format :bufimg})
                   [(+ ax 2) (+ ay 0) 40 20])))))

;; ---- Refusals ----

(deftest an-unknown-space-is-refused-by-name
  (testing "the error lists what there is"
    (is (thrown-with-msg?
         Exception #":in must be one of \[:data :drawing-area\]"
         (pj/lay-text scatter {:in :moon :x 1 :y 2 :text "no"})))))

(deftest the-space-is-documented
  (testing ":in appears in the public layer option docs"
    (is (contains? pj/layer-option-docs :in))))
