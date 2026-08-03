(ns scicloj.plotje.shape-legend-test
  "Legends for the :shape aesthetic.

   Mapping a column to :shape drew different markers but never said what they
   meant: with :shape alone there was no legend at all, and with :color and
   :shape on one column the color legend's keys were all circles while the
   panel held circles, squares and triangles (issue #4).

   The fix moves the category-to-symbol assignment from render time to plan
   time, so one decision feeds both the marks and the legend. The assertions
   below read the rendered SVG, because a legend that disagrees with its marks
   is wrong in the picture while every plan value looks fine."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

;; ---- Reading the rendered SVG ----

(defn- walk
  "Every element of an SVG hiccup tree, each carrying its absolute position and
   whether it is drawn inside a legend. Plotje nests a translate per drawing
   region, so an element's own coordinates are relative to its ancestors';
   legend chrome sits under <g data-legend=\"true\">."
  [node dx dy in-legend?]
  (when (vector? node)
    (let [[tag attrs] node
          attrs (when (map? attrs) attrs)
          in-legend? (or in-legend? (= "true" (:data-legend attrs)))
          [dx dy] (if-let [[_ x y] (some->> (:transform attrs)
                                            (re-matches #"translate\(([-\d.]+),([-\d.]+)\)"))]
                    [(+ dx (parse-double x)) (+ dy (parse-double y))]
                    [dx dy])]
      (cons (assoc (or attrs {}) :tag tag :text (last node)
                   :legend? in-legend? :abs-x dx :abs-y dy)
            (mapcat #(walk % dx dy in-legend?) (rest node))))))

(defn- elements [pose]
  (walk (pj/plot pose) 0.0 0.0 false))

(defn- symbol-kind
  "Which shape symbol an element draws, or nil when it is not a marker. A
   marker is either a rounded rect on a square bounding box (circle when the
   radius is positive, square when it is zero) or a closed polygon whose
   vertex count names the symbol. The two triangles share a count, as do the
   plus and the cross."
  [{:keys [tag rx width height points]}]
  (case tag
    :rect (when (and rx width height
                     (== (parse-double (str width)) (parse-double (str height))))
            (if (pos? (parse-double (str rx))) :circle :square))
    :polygon (case (count (str/split (str points) #" "))
               4 :triangle-ish
               5 :diamond
               13 :plus-or-cross
               nil)
    nil))

(defn- capturing
  "Run `f`, returning [printed-output value] so a test can assert on both a
   warning and the value produced alongside it."
  [f]
  (let [value (volatile! nil)
        out (with-out-str (vreset! value (f)))]
    [out @value]))

(defn- markers
  "Marker elements, split into the ones inside the legend and the ones in the
   panel, each reduced to its symbol kind."
  [els]
  (let [kinds (keep (fn [el]
                      (when-let [k (symbol-kind el)]
                        {:kind k :legend? (:legend? el) :fill (:fill el)}))
                    els)]
    {:legend (filterv :legend? kinds)
     :marks (filterv (complement :legend?) kinds)}))

(defn- texts [els]
  (mapv :text (filter #(= :text (:tag %)) els)))

(defn- legend-texts [els]
  (mapv :text (filter #(and (= :text (:tag %)) (:legend? %)) els)))

;; ---- The reported bug: a shape mapping produces a legend ----

(deftest shape-alone-renders-a-legend
  ;; Issue #4 verified as open on 0.6.0 with exactly this check: the plot's
  ;; texts were axis and tick labels only -- no legend title, no categories.
  (let [els (elements (-> (rdatasets/datasets-iris)
                          (pj/lay-point :sepal-length :sepal-width
                                        {:shape :species})))
        ts (set (texts els))]
    (testing "the legend names the column and every category"
      (is (contains? ts "species"))
      (is (every? ts ["setosa" "versicolor" "virginica"])))
    (testing "the plan carries a shape legend"
      (let [legend (:shape-legend (pj/plan (-> (rdatasets/datasets-iris)
                                               (pj/lay-point :sepal-length :sepal-width
                                                             {:shape :species}))))]
        (is (= :shape (:type legend)))
        (is (= [["setosa" :circle] ["versicolor" :square] ["virginica" :triangle]]
               (mapv (juxt :label :shape) (:entries legend))))))))

(deftest legend-keys-draw-the-symbols-the-marks-draw
  ;; The heart of #4: the legend drew three circles over a panel of circles,
  ;; squares and triangles. Reconciling the counts ties the two together
  ;; without needing to know which point belongs to which category -- iris has
  ;; 50 rows per species, so each symbol must appear 50 times in the panel and
  ;; exactly once in the legend.
  (let [{:keys [legend marks]}
        (markers (elements (-> (rdatasets/datasets-iris)
                               (pj/lay-point :sepal-length :sepal-width
                                             {:shape :species}))))
        by-kind (fn [ms] (frequencies (map :kind ms)))]
    (is (= {:circle 50 :square 50 :triangle-ish 50} (by-kind marks))
        "the panel draws one symbol per species, 50 points each")
    (is (= {:circle 1 :square 1 :triangle-ish 1} (by-kind legend))
        "the legend draws each of those symbols once")))

;; ---- Color and shape on one column merge into one legend ----

(def color-and-shape
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species :shape :species})))

(deftest one-column-driving-color-and-shape-gives-one-legend
  (let [plan (pj/plan color-and-shape)
        els (elements color-and-shape)]
    (testing "the shape folds into the color legend rather than repeating it"
      (is (nil? (:shape-legend plan)))
      (is (= [:circle :square :triangle]
             (mapv :shape (:entries (:legend plan))))))
    (testing "each category is named once, not twice"
      (is (= 1 (count (filter #{"species"} (texts els)))))
      (is (= ["setosa" "versicolor" "virginica"]
             (remove #{"species"} (legend-texts els)))))
    (testing "the legend keys are the marks' symbols in the marks' colors"
      (let [{:keys [legend]} (markers els)]
        (is (= [:circle :square :triangle-ish] (mapv :kind legend)))
        (is (= 3 (count (distinct (map :fill legend))))
            "each key keeps its own category color")))))

(deftest different-columns-keep-two-legends
  ;; ggplot2 merges guides only when they match; two columns stay apart.
  (let [els (elements (-> (rdatasets/datasets-mtcars)
                          (pj/lay-point :wt :mpg {:color :cyl :shape :gear
                                                  :color-type :categorical})))
        ts (set (texts els))]
    (is (contains? ts "cyl"))
    (is (contains? ts "gear"))))

(deftest a-renamed-legend-is-not-merged
  ;; :shape-label asks for a legend of its own; merging would discard the name.
  (let [plan (pj/plan (-> (rdatasets/datasets-iris)
                          (pj/lay-point :sepal-length :sepal-width
                                        {:color :species :shape :species})
                          (pj/options {:shape-label "marker"})))]
    (is (some? (:shape-legend plan)))
    (is (= "marker" (:title (:shape-legend plan))))
    (is (not-any? :shape (:entries (:legend plan))))))

;; ---- The symbol assignment is plot-wide, not per layer ----

(deftest a-category-keeps-its-symbol-across-panels
  ;; The assignment used to be a zipmap built at render time from one layer's
  ;; own groups. Each facet panel is its own layer, so a category that is
  ;; absent from the first panel shifted every later panel's symbols: "b" drew
  ;; as a square beside "a" and as a circle beside "c".
  (let [data (concat (for [i (range 3)] {:x i :y i :g "a" :panel "left"})
                     (for [i (range 3)] {:x i :y i :g "b" :panel "left"})
                     (for [i (range 3)] {:x i :y i :g "b" :panel "right"})
                     (for [i (range 3)] {:x i :y i :g "c" :panel "right"}))
        plan (-> data
                 (pj/lay-point :x :y {:shape :g})
                 (pj/facet :panel)
                 pj/plan)
        maps (mapv :shape-map (mapcat :layers (:panels plan)))]
    (is (= 2 (count (:panels plan))))
    (is (apply = maps)
        "every panel resolves a category to the same symbol")
    (is (= {"a" :circle "b" :square "c" :triangle} (first maps)))))

;; ---- The shape legend keeps its place in the layout ----

(defn- category-label-y
  "Vertical position of the legend entry naming `label`."
  [els label]
  (:abs-y (first (filter #(and (= :text (:tag %)) (= label (:text %))) els))))

(deftest a-bottom-legend-reserves-the-same-room-for-either-channel
  ;; The height reserved for a top/bottom legend was counted from the color
  ;; legend's entries alone, so a shape-only plot reserved one row for three
  ;; and drew the legend down over the x-axis label.
  (let [iris (rdatasets/datasets-iris)
        bottom (fn [mapping]
                 (elements (-> iris
                               (pj/lay-point :sepal-length :sepal-width mapping)
                               (pj/options {:legend-position :bottom}))))
        by-shape (bottom {:shape :species})
        by-color (bottom {:color :species})]
    (is (= (category-label-y by-color "setosa")
           (category-label-y by-shape "setosa"))
        "three categories reserve the same height whichever channel names them")
    (is (< (category-label-y by-shape "setosa")
           (category-label-y by-shape "sepal length"))
        "the legend stays above the axis label")))

(deftest a-composite-draws-one-shared-shape-legend-inside-its-canvas
  ;; Composite legends sit on a fixed ladder, one rung per channel. Giving
  ;; shape a rung of its own put a shape-only composite's legend 400 pixels
  ;; below the bottom of the image.
  (let [iris (rdatasets/datasets-iris)
        els (elements (pj/arrange
                       [(-> iris (pj/lay-point :sepal-length :sepal-width {:shape :species}))
                        (-> iris (pj/lay-point :petal-length :petal-width {:shape :species}))]))
        height (parse-double (str (:height (first els))))]
    (is (= 1 (count (filter #(= "setosa" (:text %)) els)))
        "one shared legend, not one per cell")
    (is (every? #(< 0 (category-label-y els %) height)
                ["setosa" "versicolor" "virginica"])
        "every entry is drawn within the image")))

;; ---- pj/scale :shape ----

(def tiers
  {:model ["a" "b" "c" "d"] :score [3 1 4 2]
   :tier ["gold" "silver" "bronze" "gold"]})

(defn- legend-pairs [pose]
  (mapv (juxt :label :shape) (:entries (:shape-legend (pj/plan pose)))))

(deftest shape-scale-sets-category-order
  ;; :domain was documented on pj/scale as "shape legend order" and read
  ;; nowhere; the categories came out in data order regardless.
  (is (= [["gold" :circle] ["silver" :square] ["bronze" :triangle]]
         (legend-pairs (-> tiers
                           (pj/lay-point :model :score {:shape :tier})
                           (pj/scale :shape {:domain ["gold" "silver" "bronze"]}))))))

(deftest shape-scale-sets-the-symbols
  (is (= [["gold" :diamond] ["silver" :cross] ["bronze" :plus]]
         (legend-pairs (-> tiers
                           (pj/lay-point :model :score {:shape :tier})
                           (pj/scale :shape {:domain ["gold" "silver" "bronze"]
                                             :values [:diamond :cross :plus]}))))))

(deftest chosen-symbols-reach-the-rendered-marks
  ;; A :values that only changed the legend would leave the picture lying.
  (let [{:keys [legend marks]}
        (markers (elements (-> {:x [1 2 3 4] :y [1 2 3 4] :g ["a" "a" "b" "b"]}
                               (pj/lay-point :x :y {:shape :g})
                               (pj/scale :shape {:values [:plus :diamond]}))))]
    (is (= {:plus-or-cross 2 :diamond 2} (frequencies (map :kind marks))))
    (is (= {:plus-or-cross 1 :diamond 1} (frequencies (map :kind legend))))))

(deftest an-unknown-symbol-is-rejected
  ;; draw-shape falls back to a circle for anything it does not know, so an
  ;; unrecognized :values symbol would draw a circle while the legend named the
  ;; symbol -- the very disagreement between legend and marks this feature
  ;; exists to remove.
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"does not recognize \[:nonsense\]"
       (-> tiers
           (pj/lay-point :model :score {:shape :tier})
           (pj/scale :shape {:values [:nonsense :square :cross]}))))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #":values applies to :shape only"
       (-> tiers
           (pj/lay-point :model :score {:size :score})
           (pj/scale :size {:values [:circle]})))
      "another channel has no symbols to choose")
  ;; Saying only what :values is not for leaves a caller who wanted to
  ;; choose colors with nowhere to go, so the message names the palette.
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #":palette"
       (-> tiers
           (pj/lay-point :model :score {:color :tier})
           (pj/scale :color {:values ["#e41a1c"]})))))

(deftest a-domain-that-omits-a-category-warns-and-still-assigns-it
  (let [[out pairs] (capturing
                     #(legend-pairs (-> {:x [1 2 3] :y [1 2 3] :g ["a" "b" "c"]}
                                        (pj/lay-point :x :y {:shape :g})
                                        (pj/scale :shape {:domain ["c" "a"]}))))]
    (is (str/includes? out "omits [\"b\"]"))
    (is (= [["c" :circle] ["a" :square] ["b" :triangle]] pairs)
        "the listed categories lead; the omitted one follows, still drawn")))

;; ---- More categories than symbols ----

(deftest symbols-run-out-loudly
  ;; One category past the end of the symbol list repeats the first symbol, so
  ;; two categories become indistinguishable -- say so rather than draw a lie.
  ;; Driven off pj/shape-symbols so growing the list does not break the test.
  (let [available (count pj/shape-symbols)
        syms (fn [n]
               (capturing
                #(mapv :shape
                       (:entries (:shape-legend
                                  (pj/plan (-> {:x (range n) :y (range n)
                                                :g (mapv (fn [i] (str (char (+ 97 i))))
                                                         (range n))}
                                               (pj/lay-point :x :y {:shape :g}))))))))
        [full-out full] (syms available)
        [over-out over] (syms (inc available))]
    (is (= available (count (distinct full)))
        "every category up to the end of the list gets a symbol of its own")
    (is (not (str/includes? full-out "shape categories exceeds")))
    (is (str/includes? over-out (str (inc available) " shape categories exceeds the "
                                     available " available")))
    (is (= (first over) (last over))
        "one category past the end reuses the first symbol")))
