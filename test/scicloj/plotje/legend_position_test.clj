(ns scicloj.plotje.legend-position-test
  "`:legend-position :top` drew its legend above the top of the image.
   Layout reserves a band of `legend-h` between the title and the panels
   and shrinks the panels to pay for it, but the renderer went on placing
   the panels at the top of the canvas and put the legend a band's height
   *above* them -- y = -70 on a 400-pixel image. So the plot lost the
   space, the band opened up at the bottom instead, and the legend was
   drawn where nothing is visible.

   Every legend kind was affected, and the book's own worked example
   (customization.clj, 'Legend on top:') shipped without a legend. The
   assertion there was a count of data points, which passes whatever
   happens to the legend -- so the guard here reads absolute positions
   out of the rendered SVG instead."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

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

(defn- elements [pose] (walk (pj/plot pose) 0.0 0.0))

(defn- px [v] (when v (parse-double (str v))))

(defn- texts-reading
  "Absolute y of every text element whose content is one of `wanted`."
  [els wanted]
  (for [{:keys [tag text abs-y]} els
        :when (and (= :text tag) (contains? wanted text))]
    abs-y))

(defn- panel-top
  "Top edge of the widest panel background -- where the plotting area
   starts."
  [els]
  (->> els
       (filter #(and (= :rect (:tag %)) (= "rgb(232,232,232)" (:fill %))))
       (sort-by #(- (px (:width %))))
       first
       :abs-y))

(def canvas-height 400)

(defn- scatter
  "A three-category scatter, so every legend kind has something to draw."
  [mapping opts]
  (-> {:sepal-length [4.4 5.1 5.9 6.2 6.8 7.1]
       :sepal-width  [2.9 3.5 3.0 2.8 3.1 3.0]
       :species      ["setosa" "setosa" "versicolor" "versicolor"
                      "virginica" "virginica"]}
      (pj/lay-point :sepal-length :sepal-width mapping)
      (pj/options (merge {:width 640 :height canvas-height} opts))))

(def category-texts #{"setosa" "versicolor" "virginica"})

;; ---- The legend is inside the image, wherever it is put ----

(deftest legend-is-drawn-inside-the-canvas
  (doseq [channel [{:color :species} {:shape :species} {:color :species :shape :species}]
          position [:right :top :bottom]]
    (testing (str channel " at " position)
      (let [ys (texts-reading (elements (scatter channel {:legend-position position}))
                              category-texts)]
        (is (seq ys)
            "the legend keys reach the SVG at all")
        (is (every? #(<= 0.0 % canvas-height) ys)
            (str "every legend key is inside the " canvas-height "-pixel canvas, got " (vec ys)))))))

(deftest top-legend-sits-above-the-panel
  (testing "the band a :top legend reserves is the band it draws in"
    (let [els (elements (scatter {:color :species} {:legend-position :top}))
          ys (texts-reading els category-texts)
          top (panel-top els)]
      (is (every? #(< 0.0 % top) ys)
          (str "legend keys at " (vec ys) " fall between the top of the image and the panel at " top)))))

(deftest bottom-legend-sits-below-the-panel
  (testing "the mirror case, which was already correct"
    (let [els (elements (scatter {:color :species} {:legend-position :bottom}))
          ys (texts-reading els category-texts)
          top (panel-top els)]
      (is (every? #(< top % canvas-height) ys)
          (str "legend keys at " (vec ys) " fall between the panel at " top " and the bottom of the image")))))

(deftest a-title-pushes-the-top-legend-down
  (testing "the legend band starts below the title, not on top of it"
    (let [plain (texts-reading (elements (scatter {:color :species} {:legend-position :top}))
                               category-texts)
          titled (texts-reading (elements (scatter {:color :species}
                                                   {:legend-position :top :title "A title"}))
                                category-texts)]
      (is (every? #(< 0.0 % canvas-height) titled))
      (is (< (apply min plain) (apply min titled))
          "a title moves the legend down rather than leaving it where it was"))))

(deftest legend-position-none-draws-no-legend
  (doseq [channel [{:color :species} {:shape :species}]]
    (testing (str channel)
      (is (empty? (texts-reading (elements (scatter channel {:legend-position :none}))
                                 category-texts))))))
