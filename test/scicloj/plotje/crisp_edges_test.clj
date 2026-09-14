(ns scicloj.plotje.crisp-edges-test
  "A filled shape thinner than a device pixel is drawn as nothing.

   Every filled polygon carried `shape-rendering=\"crispEdges\"`,
   unconditionally, since the March 2026 bar-chart work that used it to
   remove the hairline seam anti-aliasing leaves between two bars sharing
   an edge. `crispEdges` also snaps every edge to a whole device pixel,
   so a bar narrower than one pixel has both its edges snapped to the
   same place and disappears.

   Reported by @carstenbehring on #plotje 2026-09-07, while drawing a
   needle plot with `:bar-width`: \"due to rounding errors it sometimes
   will go to width=0, instead of width=1\". The geometry was never
   wrong -- the SVG carried the right coordinates the whole time -- so
   nothing that inspects the plan or counts shapes could see it.

   Measured in headless Chromium 2026-09-09. Twelve bars sweeping 0.3 to
   1.4 drawing units wide: with `crispEdges` two vanished and the ten
   that drew were all one pixel at full opacity, so width carried no
   information at all; without it all twelve drew and their ink rose in
   step with their widths. On his own plot, ten needles came back and the
   distinct ink levels went from 4 to 56.

   The rule is by extent, not by mark: a shape at least one drawing unit
   in both directions keeps `crispEdges`, and anything thinner is left to
   anti-alias. The floor is one because a sweep of 32 sub-pixel offsets
   never erased a shape a whole drawing unit wide; an earlier two cost
   the seam removal on every shape between one and two units."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(defn- polygons
  "The attribute maps of every filled polygon the pose draws."
  [pose]
  (->> (tree-seq vector? seq (pj/plot pose {:format :svg}))
       (filter #(and (vector? %) (= :polygon (first %)) (map? (second %))))
       (map second)))

(defn- crisp-fraction
  "How many of the pose's filled polygons are snapped to whole pixels."
  [pose]
  (let [ps (polygons pose)]
    [(count (filter :shape-rendering ps)) (count ps)]))

(deftest a-thin-bar-is-left-to-anti-alias
  (testing "the reported case: 100 bars 0.2 data units wide on a numeric axis"
    (let [pose (-> {:x (range 100)
                    :y (repeat 100 1.0)}
                   (pj/lay-bar :x :y {:bar-width 0.2}))
          [crisp total] (crisp-fraction pose)]
      (is (= 100 total) "every bar is drawn")
      (is (zero? crisp)
          "no bar under one drawing unit wide is snapped to whole pixels"))))

(deftest an-ordinary-bar-is-still-snapped
  (testing "the seam-removal crispEdges exists for is kept"
    (doseq [[what pose]
            [["a plain bar chart"
              (-> {:v [1 2 3] :g [:x :y :z]} (pj/lay-bar :g :v))]
             ["stacked bars, which share an edge"
              (-> {:v [1 2 3 4 5 6]
                   :g [:x :y :z :x :y :z]
                   :s ["one" "one" "one" "two" "two" "two"]}
                  (pj/lay-bar :g :v {:color :s :position :stack}))]
             ["a histogram, whose bins share an edge"
              (-> {:v (map #(/ % 100.0) (range 300))}
                  (pj/pose :v)
                  (pj/lay-histogram {:bins 20}))]]]
      (let [[crisp total] (crisp-fraction pose)]
        (is (pos? total) (str what " draws filled shapes"))
        (is (= crisp total)
            (str what " keeps crispEdges on every one of them"))))))

(deftest the-threshold-is-on-the-smaller-extent
  (testing "a bar is thin in one direction and tall in the other, so the
            rule has to read the smaller of the two"
    (let [wide (-> {:x [1 2 3] :y [10 20 30]}
                   (pj/lay-bar :x :y {:bar-width 0.6}))
          thin (-> {:x [1 2 3] :y [10 20 30]}
                   (pj/lay-bar :x :y {:bar-width 0.002}))]
      (is (= (apply = (crisp-fraction wide)) true)
          "a bar well over one unit wide is snapped")
      (is (zero? (first (crisp-fraction thin)))
          "the same bars, drawn far narrower, are not"))))

(deftest the-geometry-is-unchanged
  (testing "this is a rendering hint only -- the coordinates were always
            right, which is why nothing counting shapes could see the bug"
    (let [pose (-> {:x (range 100) :y (repeat 100 1.0)}
                   (pj/lay-bar :x :y {:bar-width 0.2}))
          widths (->> (polygons pose)
                      (map :points)
                      (map (fn [pts]
                             (let [xs (->> (clojure.string/split pts #"\s+")
                                           (map #(parse-double
                                                  (first (clojure.string/split % #",")))))]
                               (- (apply max xs) (apply min xs))))))]
      (is (every? pos? widths)
          "every bar has a positive drawn width in the SVG")
      ;; Not exactly equal: a bar sits at a fractional place on the axis,
      ;; so two bars of one data width round to 0.98 and 0.99 drawing
      ;; units. The claim is that the data width reaches the SVG intact,
      ;; which a spread of a hundredth of a unit says and an `=` does not.
      (is (< (- (apply max widths) (apply min widths)) 0.02)
          "and they are one width, as the data says, to within rounding"))))

(deftest the-threshold-sits-at-one-drawing-unit
  ;; The tests above use extremes -- 0.002 data units against 0.6 -- so
  ;; they pass at any threshold between them. These two bracket the
  ;; boundary itself, and the wide half fails if the floor goes back up
  ;; to two: 120 bars in a 230-unit plot are 1.15 drawing units each,
  ;; which is exactly the band an earlier threshold of 2.0 gave up.
  (let [bars (fn [width]
               (-> {:c (mapv #(str "c" %) (range 120))
                    :v (vec (repeatedly 120 (constantly 1.0)))}
                   (pj/lay-bar :c :v)
                   (pj/options {:width width :height 200})))
        bar-units (fn [pose]
                    (let [xs (->> (clojure.string/split (:points (first (polygons pose))) #"\s+")
                                  (map #(parse-double
                                         (first (clojure.string/split % #",")))))]
                      (- (apply max xs) (apply min xs))))]
    (testing "just over one drawing unit is snapped"
      (let [pose (bars 230)
            [crisp total] (crisp-fraction pose)]
        (is (< 1.0 (bar-units pose) 2.0)
            "the bars sit between the old floor and the new one")
        (is (= crisp total 120)
            "every bar keeps crispEdges, so touching bars show no seam")))
    (testing "just under one drawing unit is left to anti-alias"
      (let [pose (bars 200)
            [crisp total] (crisp-fraction pose)]
        (is (< (bar-units pose) 1.0))
        (is (= 120 total) "every bar is drawn")
        (is (zero? crisp)
            "narrower than a device pixel at natural size, so snapping would erase it")))))
