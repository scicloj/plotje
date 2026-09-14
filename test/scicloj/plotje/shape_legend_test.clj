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
            [malli.core :as m]
            [scicloj.plotje.impl.pose-schema :as pose-schema]
            [scicloj.plotje.render.mark :as mark]
            [scicloj.plotje.impl.defaults :as defaults]
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
  ;; An unrecognized :values symbol has to be refused here, at the pose
  ;; boundary, while the writer can still see which symbol they wrote.
  ;; draw-shape reports one it cannot draw rather than substituting a
  ;; circle, but that report arrives from the renderer, naming no layer.
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"does not recognize \[:nonsense\]"
       (-> tiers
           (pj/lay-point :model :score {:shape :tier})
           (pj/scale :shape {:values [:nonsense :square :cross]}))))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"unexpected key\(s\): \[:values\]"
       (-> tiers
           (pj/lay-point :model :score {:size :score})
           (pj/scale :size {:values [:circle]})))
      "a magnitude is measured along a range, not chosen from a list")
  ;; :color reads :values too -- the colours a categorical column is
  ;; drawn in -- and the symbol check must not police those.
  (is (= [(/ 228.0 255) (/ 26.0 255) (/ 28.0 255) 1.0]
         (->> (-> tiers
                  (pj/lay-point :model :score {:color :tier})
                  (pj/scale :color {:values ["#e41a1c"]})
                  pj/plan :legend :entries)
              (mapv :color)
              first))))

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
  ;; One category past the end of the palette repeats the first symbol, so
  ;; two categories become indistinguishable -- say so rather than draw a lie.
  ;; Driven off pj/shape-palette so growing it does not break the test. The
  ;; palette, not pj/shape-symbols: what runs out is the list categories are
  ;; assigned from, and a symbol a caller has to name by hand -- :circle-open --
  ;; is never assigned, so it does not raise this ceiling.
  (let [available (count (pj/shape-palette))
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

;; ---- An unfilled symbol ----

(defn- ring-and-disc-counts
  "How many circular marks the plot draws as an outline, and how many as
   a solid disc. A circle is a rounded rect whose radius is half its
   side, so both are `:rect` with an `:rx`; what tells them apart is
   whether the fill or the stroke carries the colour."
  [pose]
  (let [rects (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
                   (filter #(and (vector? %) (= :rect (first %))
                                 (map? (second %)) (:rx (second %))))
                   (map second))]
    {:rings (count (filter #(= "none" (:fill %)) rects))
     :discs (count (remove #(= "none" (:fill %)) rects))}))

(deftest circle-open-draws-a-ring-test
  ;; Requested on the issue tracker (#46): overlapping points are easier
  ;; to count as rings than as discs, which merge into one blob.
  (testing "a layer given :circle-open draws outlines, not discs"
    (let [pose (-> {:x [1 2 3] :y [1 2 3]}
                   (pj/lay-point :x :y {:shape :circle-open}))]
      (is (= {:rings 3 :discs 0} (ring-and-disc-counts pose)))))

  (testing "the legend draws the same symbol the marks do"
    ;; `draw-shape` is shared with the legend renderer, so this is really
    ;; a check that nothing routes around it.
    (let [pose (-> {:x [1 2 3 4] :y [1 2 3 4] :g ["a" "b" "a" "b"]}
                   (pj/lay-point :x :y {:shape :g})
                   (pj/scale :shape {:values [:circle-open :circle]}))]
      (is (= {:rings 3 :discs 3} (ring-and-disc-counts pose))
          "two marks and one legend key of each")))

  (testing "a ring covers the same box as the disc it replaces"
    ;; A stroke straddles its path, so a ring drawn at radius r would
    ;; reach r plus half the stroke and read as the larger symbol.
    (let [side (fn [shape]
                 (->> (tree-seq vector? seq
                                (pj/plot (-> {:x [1] :y [1]}
                                             (pj/lay-point :x :y (cond-> {:size 8}
                                                                   shape (assoc :shape shape))))
                                         {:format :svg}))
                      (filter #(and (vector? %) (= :rect (first %))
                                    (map? (second %)) (:rx (second %))))
                      (map second)
                      (map (fn [a] (+ (double (:width a))
                                      (double (or (:stroke-width a) 0)))))
                      first))]
      (is (== (side nil) (side :circle-open)))))

  (testing "the palette is unchanged, so no existing plot moves"
    (is (= [:circle :square :triangle :diamond :triangle-down :plus :cross]
           (pj/shape-palette)))
    (is (= :circle-open (last (pj/shape-symbols))))
    (is (= (pj/shape-palette) (vec (butlast (pj/shape-symbols)))))))

(deftest what-a-shape-is-is-answered-once
  ;; The set of symbols is meant to grow, so nothing may answer "is
  ;; this a shape" from a copy taken when a namespace loaded, and
  ;; nothing may answer it a second way.
  (testing "the schema reads the set when it validates, not at load"
    ;; An `[:enum ...]` built here would be built once, and a symbol
    ;; added afterwards would be refused however the rest answered.
    (is (every? #(m/validate pose-schema/Shape %) (pj/shape-symbols)))
    (is (not (m/validate pose-schema/Shape :banana)))
    (is (= (set (pj/shape-symbols))
           (set (filter #(m/validate pose-schema/Shape %) (pj/shape-symbols))))))

  (testing "an unnamed symbol draws a circle and an unknown one is reported"
    ;; Both callers inside the library substitute :circle for nil
    ;; before calling, so the old catch-all branch was reachable only
    ;; from outside -- and it handed a caller a circle where the pose
    ;; boundary would have refused the same symbol by name.
    (is (some? (mark/draw-shape nil 3.0)))
    (is (some? (mark/draw-shape :circle 3.0)))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Cannot draw the shape :banana"
                          (mark/draw-shape :banana 3.0))))

  (testing "the plan assigns from the same palette the accessor publishes"
    ;; Two readers of one list: the assignment in `plan.clj` and
    ;; `pj/shape-palette`. They go through one accessor so they cannot
    ;; drift, which is what a `:shape-values` configuration key would
    ;; hook into.
    (let [entries (-> {:x [1.0 2.0 3.0] :y [1.0 2.0 3.0] :g ["a" "b" "c"]}
                      (pj/lay-point :x :y {:shape :g})
                      pj/plan :shape-legend :entries)]
      (is (= (vec (take 3 (pj/shape-palette)))
             (mapv :shape entries))))))

(deftest the-palette-and-the-drawable-set-cannot-disagree
  ;; `drawable-shape-syms` was a value computed from `shape-syms` when
  ;; the namespace loaded, while `shape-palette` was a function reading
  ;; the same var at call time. Changing the palette moved one answer
  ;; and not the other: the plan handed a category the new symbol and
  ;; the schema then refused it. `drawable-shapes` recomputes now.
  (testing "before any change, every assigned symbol is a writable one"
    (is (every? (set (pj/shape-symbols)) (pj/shape-palette))))
  (testing "and after one, still"
    (with-redefs [defaults/shape-syms (conj defaults/shape-syms :moon)]
      (is (contains? (set (pj/shape-palette)) :moon)
          "the palette follows the change")
      (is (contains? (set (pj/shape-symbols)) :moon)
          "and so does the set that validates what may be written")
      (is (every? (set (pj/shape-symbols)) (pj/shape-palette))
          "which is the invariant: a symbol handed out can be written")))
  (testing "the change is not sticky"
    (is (not (contains? (set (pj/shape-symbols)) :moon)))))

(deftest the-refusal-does-not-list-the-symbol-it-refuses
  ;; The sentence enumerates what may be written, not what `draw-shape`
  ;; can draw. The two agree for every shipped symbol, so the only way
  ;; to see the difference is to add one -- and then the message said
  ;; Plotje draws the very symbol it was refusing.
  (testing "a plain unknown symbol is reported with the drawable list"
    (let [msg (try (mark/draw-shape :nonsense 5)
                   (catch clojure.lang.ExceptionInfo e (.getMessage e)))]
      (is (re-find #"Cannot draw the shape :nonsense" msg))
      (is (not (re-find #":nonsense.*:nonsense" msg))
          "named once, as the refused symbol, and not again as a drawable one")
      (is (re-find #":circle" msg) "the drawable symbols are still listed")))
  (testing "a symbol that is writable but undrawable is left out of its own list"
    (with-redefs [defaults/shape-syms (conj defaults/shape-syms :moon)]
      (let [msg (try (mark/draw-shape :moon 5)
                     (catch clojure.lang.ExceptionInfo e (.getMessage e)))]
        (is (re-find #"Cannot draw the shape :moon" msg))
        (is (not (re-find #"Plotje draws.*:moon" msg))
            "the list of what Plotje draws must not contain the refusal's subject")))))
