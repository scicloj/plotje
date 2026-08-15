(ns scicloj.plotje.position-space-test
  "A position is a quantity in a space.

   Two things follow, and this namespace pins both. First, a position may
   be a literal value, as `:color`, `:size` and `:alpha` have always
   allowed -- `{:color \"red\"}` needed no column, and now neither does
   `{:x 2.0}`. (`:shape` and `:group` still do not: see the backlog.)
   Second, `:in` names the space those numbers are in: `:data` by default,
   or `:drawing-area` for a mark placed on the panel rather than in the
   data.

   The distinction that has to hold: a data-space position is a datum, so
   it trains the axis; a drawing-space position is a page measurement, so
   it must not. A drawing-space note that stretched the axis to 160 would
   still draw, and would still look plausible, so the domain assertions
   below matter as much as the ink ones."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
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

(defn- n-marks
  "How many marks the last layer of `pose` draws."
  [pose]
  (->> (pj/plan pose) :panels first :layers last :groups
       (map (comp count :xs)) (reduce +)))

(deftest a-value-beside-a-column-broadcasts
  (testing "one x for every row, which is how a fixed-x label is written"
    (let [labelled (pj/lay-text scatter {:x 2.0 :y :weight :text :height})]
      (is (= 3 (n-marks labelled)))
      (is (= [2.0] (->> (pj/plan labelled) :panels first :layers last
                        :groups first :xs distinct vec))
          "the value repeats; the column varies")))
  (testing "the coordinate left out is inherited from the pose and broadcasts too"
    (is (= 3 (n-marks (pj/lay-text scatter {:x 2.0 :text :height})))))
  (testing "a string :text naming no column labels every row with it"
    ;; It used to be an error with data present -- a string was a column
    ;; reference and nothing else there, so the one reading it could
    ;; have had was unreachable. The data decides now: a string naming a
    ;; column reads it, and one naming none is the label itself.
    (let [labelled (pj/lay-text scatter {:x 2.0 :y :weight :text "n"})]
      (is (= 3 (n-marks labelled)))
      (is (= ["n"] (->> (pj/plan labelled) :panels first :layers last
                        :groups first :labels distinct vec))
          "one label, repeated over the layer's rows")))
  (testing "and a string that does name a column still reads it"
    (let [from-col (pj/lay-text scatter {:x 2.0 :y :weight :text :height})]
      (is (< 1 (count (->> (pj/plan from-col) :panels first :layers last
                           :groups first :labels distinct))))))
  (testing "and it broadcasts over the layer's own data when it brings some"
    (is (= 2 (n-marks (pj/lay-text scatter {:x 2.0 :y :w
                                            :text :t
                                            :data {:t ["a" "b"] :w [3.0 4.0]}}))))))

(deftest a-layer-of-values-alone-draws-one-mark
  (testing "neither :x nor :y reads the data, so the data's length is not its length"
    ;; The distinction that earns the two shapes: with a column for
    ;; neither, the layer is an annotation, and three identical marks
    ;; stacked on one point is what broadcasting there would mean.
    (is (= 1 (n-marks (pj/lay-text scatter {:x 2.0 :y 5.0 :text "n"}))))
    (is (= [:point :text] (marks (pj/lay-text scatter {:x 2.0 :y 5.0 :text "n"}))))))

(deftest an-integer-column-name-is-still-a-column
  (testing "a number that names a column reads the column, not a value to draw at"
    ;; A dataset built without column names is given integer ones, so the
    ;; two readings collide. The data decides.
    ;; `pj/infer-mapping` gives raw data its default mapping, so `{:x 0}`
    ;; reaches the merged mapping without passing the API's own check.
    (let [plan (pj/plan (tc/dataset [[1 2] [3 4] [5 7]]))]
      (is (= ["0" "1"] ((juxt :x-label :y-label) plan)))
      (is (= 3 (->> plan :panels first :layers last :groups first :xs count))))))

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

;; ---- A panel where nothing informs a domain ----

(deftest a-panel-with-no-data-space-x-still-has-an-x-domain
  ;; `y-dom` has carried a `[0 1]` fallback all along and `x-dom` had
  ;; none, so a panel whose every layer is on the panel rather than in
  ;; the data planned `:x-domain nil`. A nil domain is not a domain:
  ;; the marks that read it through a band scale died on
  ;; `Number.doubleValue() because "x" is null`, and the rest drew no x
  ;; ticks. Reached through `:in :drawing-area`, which is released, as
  ;; well as through a per-axis `:scale false`.
  (testing "through the whole-layer form"
    (is (= [0 1] (-> (pj/lay-bar {:a [1 2] :b [1 2]} :a :b {:in :drawing-area})
                     pj/plan :panels first :x-domain))))
  (testing "through a per-axis :scale false"
    (let [panel (-> {:a [1 2 3] :b [1 2 3]}
                    (pj/lay-point :a :b {:x {:column :a :scale false}
                                         :y {:column :b :scale false}})
                    pj/plan :panels first)]
      (is (= [0 1] (:x-domain panel)))
      (is (= [0 1] (:y-domain panel)))))
  (testing "and a bar mark on such a panel plans and draws"
    (is (instance? BufferedImage
                   (pj/plot (pj/lay-bar {:a [1 2] :b [1 2]} :a :b {:in :drawing-area})
                            {:format :bufimg})))))

;; ---- Annotation colors ----

(deftest an-annotation-takes-a-color-by-either-spelling
  ;; The pose gate reads a keyword naming a color as that color now,
  ;; but the annotation path kept `:color` only when it was a string --
  ;; so `{:color :red}` was dropped in silence while
  ;; `{:color :notacolour}` was still reported. The gate and the draw
  ;; path have to agree about the same value.
  (let [color-of (fn [v] (-> scatter
                             (pj/lay-rule-h {:y-intercept 2 :color v})
                             pj/plan :panels first :annotations first :color))]
    (is (= (color-of "red") (color-of :red)))
    (is (some? (color-of :red))))

  (testing "and a value naming no color is still reported"
    (is (thrown-with-msg?
         Exception #"not found in dataset"
         (pj/plan (pj/lay-rule-h scatter {:y-intercept 2 :color :notacolour}))))))
