;; # Draft: layer geometry is not clipped to the panel
;;
;; Motivation. Issue #16 reports that `pj/lay-line` does not respect the
;; `:domain` of a `pj/scale`. A collaborator draws Cook's-distance contour
;; lines on a residual-vs-leverage plot and constrains the y axis with
;; `pj/scale :y {:domain ...}`. In a single plot the result looks right;
;; once several such plots are stacked with `pj/arrange`, the contour
;; lines from one panel run across the panels above it.
;;
;; This draft reduces that to a minimal case and shows that the domain is
;; in fact respected -- the real gap was that layer geometry extending
;; past the domain was never clipped to the panel rectangle.
;;
;; RESOLVED. The fix wraps each panel's marks in a Membrane scissor
;; (`render/panel.clj`) and teaches the SVG backend to realize it as a
;; `<clipPath>` (`render/svg.clj`); the Java2D backend already masks
;; natively. The "geometry escapes the panel" section below still holds
;; at the coordinate level -- clipping masks, it does not move geometry,
;; so the SVG polyline still carries out-of-panel points -- but the
;; rendered output is now bounded to the panel. The published examples
;; live in customization.clj (domain as a view window), composition.clj
;; (panels clip their own content), and membranes.clj (the scissor
;; node); the regression guard is scicloj.plotje.clip-test.
;;
;; This notebook is exploratory -- it is not in chapters.edn and is not
;; part of the published book.

(ns draft-panel-clipping
  (:require [clojure.walk :as walk]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [scicloj.plotje.api :as pj]))

;; ## The data
;;
;; Five points with y between 10 and 25, plus one line whose data runs
;; far outside that range: from y = -100 up to y = 200. The narrow domain
;; stands in for the collaborator's `[cooksd-min cooksd-max]`, and the
;; out-of-range line stands in for a steep Cook's-distance contour.

(def points-data
  {:x [1 2 3 4 5]
   :y [10 20 15 25 18]})

(def line-data
  {:x [1 5]
   :y [-100 200]})

;; ## The pose
;;
;; A point layer, a line layer reaching well beyond the data, and a y
;; scale pinned to a domain narrower than the line.

(def one-panel
  (-> points-data
      (pj/lay-point :x :y)
      (pj/lay-line {:data line-data})
      (pj/scale :y {:domain [0 30]})))

;; ## A single plot looks correct
;;
;; The axis stops at 30 and the line appears to stop with it -- the
;; overflow falls outside the plotting canvas, where the SVG viewport
;; crops it from view.

one-panel

;; ## Arranged, the line bleeds across panels
;;
;; Stacking two copies exposes the overflow. The line that should stay
;; inside its own panel runs up through the panel above it.

(pj/arrange [one-panel one-panel] {:cols 1})

;; ## The domain itself is respected
;;
;; The reported symptom points at `:domain`, but the computed panel
;; domain is exactly the requested `[0 30]` -- in the single plot and in
;; each arranged sub-plot. The scale is doing its job.

(-> one-panel
    pj/plan
    :panels
    first
    :y-domain)

(kind/test-last [= [0 30]])

(->> (pj/arrange [one-panel one-panel] {:cols 1})
     pj/plan
     :sub-plots
     (map (fn [sp] (-> sp :plan :panels first :y-domain))))

(kind/test-last [= [[0 30] [0 30]]])

;; ## The geometry escapes the panel rectangle
;;
;; What actually goes wrong is visible in the rendered SVG. The panel's
;; drawing area spans a known band of y pixels; the line's rendered
;; endpoints sit far outside that band, above and below it. Nothing clips
;; them back to the panel.

(def svg (pj/plot one-panel {:format :svg}))

(defn collect
  "All attribute maps for elements with the given tag in an SVG hiccup tree."
  [hiccup tag]
  (let [out (atom [])]
    (walk/postwalk
     (fn [node]
       (when (and (vector? node) (= tag (first node)))
         (swap! out conj (second node)))
       node)
     hiccup)
    @out))

;; The panel rectangle -- its top and bottom in pixel space.

(def panel-rect
  (->> (collect svg :rect)
       (filter (fn [a] (and (:width a) (:height a) (not (:rx a)))))
       first))

(def panel-y-range
  [(double (:y panel-rect))
   (+ (double (:y panel-rect)) (double (:height panel-rect)))])

panel-y-range

;; Our line is the two-point polyline with the largest vertical span. Its
;; y pixel coordinates land outside the panel band on both ends.

(defn polyline->nums [points]
  (mapv #(Double/parseDouble %) (clojure.string/split points #"[ ,]+")))

(def out-of-range-line
  (->> (collect svg :polyline)
       (map :points)
       (filter #(re-matches #"[-0-9. ,]+" %))
       (map polyline->nums)
       (filter #(= 4 (count %)))
       (apply max-key (fn [[_ y1 _ y2]] (Math/abs (- y2 y1))))))

out-of-range-line

;; Both endpoints sit outside the panel band -- one below the bottom, one
;; above the top.

(let [[_ y1 _ y2] out-of-range-line
      [top bottom] panel-y-range]
  {:line-y [y1 y2]
   :panel-y-range panel-y-range
   :below-bottom (> (max y1 y2) bottom)
   :above-top (< (min y1 y2) top)})

(kind/test-last [#(and (:below-bottom %) (:above-top %))])

;; ## Where the clip was missing
;;
;; Before the fix, only the :tile layer-type clamped its rectangles to
;; the drawing area (the `*-clip` bounds in scicloj.plotje.render.mark).
;; The :line method -- and :point, :area, :smooth, and the rest -- emitted
;; raw `coord-fn` coordinates with no bounds check, so any geometry past
;; the domain was drawn at its projected pixel position. A single panel
;; hid this: the surrounding SVG viewport cropped the overflow. A
;; composite did not: each panel is translated into one tall canvas, so
;; one panel's overflow landed inside its neighbours.

;; ## The fix
;;
;; `panel->membrane` now wraps the panel's data marks in a
;; `membrane.ui/scissor-view` at the drawing area (the grey panel
;; background), and the SVG backend realizes that scissor as a
;; `<clipPath>`. The clip is a node in the membrane tree, so every
;; backend honours it uniformly. The polyline coordinates above are
;; unchanged -- a scissor masks, it does not move geometry -- but the
;; rendered SVG now carries a clip region per panel:

(:clips (pj/svg-summary svg))

(kind/test-last [#(= 1 %)])

;; ## Stress tests: diverse layer types and edge cases
;;
;; The minimal repro uses one line. A clip that masks every mark type
;; uniformly should hold across filled marks, statistical layers,
;; coordinate systems, facets, annotations, and log scales -- and it
;; should leave alone the marks that legitimately sit outside the
;; drawing area. The cases below were each rendered to PNG and
;; inspected visually; here they carry `kind/test-last` assertions on
;; the clip count (one clip region per panel) and, where relevant, on
;; data retention. The unifying check is `:clips` equals the number of
;; panels.

;; ### Filled marks: bars and area clipped flat at the window top
;;
;; A value bar taller than the y-domain is cut flat at the panel edge
;; rather than overflowing above it.

(-> {:cat ["a" "b" "c" "d"] :v [10 60 35 90]}
    (pj/lay-bar :cat :v)
    (pj/scale :y {:type :linear :domain [0 40]})
    (pj/options {:title "bars clipped at y=40"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; A filled area beyond the window is clipped to the window top, with
;; the fill polygon bounded to the panel.

(-> {:x (range 0 20) :y (map #(* % %) (range 0 20))}
    (pj/lay-area :x :y)
    (pj/scale :y {:type :linear :domain [0 100]})
    (pj/options {:title "area clipped at y=100"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; ### Margin marks survive: rug
;;
;; Data marks clip to the drawing area, but rug ticks sit in the bottom
;; margin on purpose. They get a separate scissor at the wider panel
;; box, so they survive the clip. A panel with both data marks and a
;; rug therefore reports two clip regions -- one for the data, one for
;; the rug.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rug :sepal-length {:length 12})
    (pj/options {:title "rug survives the clip"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            ;; data clip + rug clip, and the rug ticks are still present
            (and (= 2 (:clips s))
                 (pos? (:lines s)))))])

;; ### Annotations clipped to the window
;;
;; A `rule-h` inside the window draws; one outside it is masked away. A
;; `band-h` straddling the window edge is cut at the panel top.

(-> {:x [1 2 3 4 5] :y [10 20 15 25 18]}
    (pj/lay-point :x :y)
    (pj/lay-rule-h {:y-intercept 22 :color "green"})
    (pj/lay-rule-h {:y-intercept 999 :color "purple"})
    (pj/lay-band-h {:y-min 25 :y-max 200 :color "orange"})
    (pj/scale :y {:type :linear :domain [0 30]})
    (pj/options {:title "rule/band clipped to the window"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; ### Coordinate systems: flip and polar
;;
;; Under `:coord :flip` the clip still bounds an out-of-domain line.

(-> {:x [1 2 3 4 5] :y [10 20 15 25 18]}
    (pj/lay-point :x :y)
    (pj/lay-line {:color "red" :data {:x [1 5] :y [-200 300]}})
    (pj/scale :y {:type :linear :domain [0 30]})
    (pj/coord :flip)
    (pj/options {:title "flip + clipped line"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; Polar still renders -- the panel-box clip does not distort it.

(-> {:cat ["a" "b" "c" "d" "e"] :v [3 7 5 9 4]}
    (pj/lay-bar :cat :v)
    (pj/coord :polar)
    (pj/options {:title "polar still renders"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; ### Statistical layers: smooth and boxplot
;;
;; A smooth curve and its points clip to a tightened window.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-smooth :sepal-length :sepal-width {:color "red"})
    (pj/scale :y {:type :linear :domain [2.8 3.2]})
    (pj/options {:title "smooth clipped to [2.8, 3.2]"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; Boxplot whiskers and boxes are cut at the window edges.

(-> (rdatasets/datasets-iris)
    (pj/lay-boxplot :species :sepal-width)
    (pj/scale :y {:type :linear :domain [2.8 3.2]})
    (pj/options {:title "boxplot clipped to [2.8, 3.2]"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; ### Facets: one clip region per cell
;;
;; A tightened domain clips the points in every cell, and each cell
;; reports its own clip region.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :domain [2.8 3.3]})
    (pj/facet :species)
    (pj/options {:title "facet: points clipped per cell"}))

(kind/test-last [#(= 3 (:clips (pj/svg-summary %)))])

;; ### Log scale
;;
;; On a log y-scale, points above the window are clipped and the line
;; is cut at the panel top -- the clip operates on resolved pixel
;; positions, so it is the same mechanism whatever the scale type.

(-> {:x [1 2 3 4 5] :y [2 20 200 2000 20000]}
    (pj/lay-point :x :y)
    (pj/lay-line {:color "red"})
    (pj/scale :y {:type :log :domain [1 1000]})
    (pj/options {:title "log y [1, 1000]"}))

(kind/test-last [#(= 1 (:clips (pj/svg-summary %)))])

;; ### Boundary points clip at the drawing-area edge
;;
;; A point sitting exactly on the domain edge is cut at the grey
;; drawing-area boundary -- half inside the window, half outside --
;; matching ggplot's default panel clip. The marks are not dropped:
;; all five points are still emitted, the edge ones simply rendered
;; behind the clip.

(-> {:x [1 2 3 4 5] :y [0 10 20 25 30]}
    (pj/lay-point :x :y {:size 14})
    (pj/scale :y {:type :linear :domain [0 30]})
    (pj/options {:title "boundary points (y=0, y=30) clip at the grey edge"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 5 (:points s))
                 (= 1 (:clips s)))))])

;; ### The original issue: stacked steep lines
;;
;; Several steep contour lines (the shape from issue #16), stacked with
;; `pj/arrange`. Each panel confines its lines; none bleed into the
;; panels above. Three panels, three clip regions.

(def cooks
  (-> {:x [0.2 0.3 0.4 0.5 0.6] :y [0.05 0.1 0.08 0.15 0.4]}
      (pj/lay-point :x :y)
      (pj/lay-line {:color "grey" :data {:x [-0.1 0.8] :y [0 0.4]}})
      (pj/lay-line {:color "grey" :data {:x [-0.1 0.8] :y [0 0.8]}})
      (pj/lay-line {:color "grey" :data {:x [-0.1 0.8] :y [0 1.6]}})
      (pj/lay-line {:color "grey" :data {:x [-0.1 0.8] :y [0 3.2]}})
      (pj/scale :y {:type :linear :domain [0 0.45]})))

(pj/arrange [cooks cooks cooks] {:cols 1 :height 900})

(kind/test-last [#(= 3 (:clips (pj/svg-summary %)))])

;; ## Notes
;;
;; - The fix lives at the panel boundary, not in each mark. Clamping a
;;   line's two endpoints independently (the way :tile clamps a
;;   rectangle) would bend the line's slope. A per-panel scissor clips
;;   every mark type uniformly without per-mark geometry math.
;;
;; - Data marks clip to the drawing area (the grey background), so
;;   nothing spills into the axis margin -- the same boundary ggplot
;;   clips to by default. Rug ticks draw in the margin on purpose, so
;;   they get a second scissor at the wider panel box.
;;
;; - A consequence of the drawing-area clip: a mark sitting exactly on
;;   the domain edge (a point at the y-max, say) is cut at that edge,
;;   as in ggplot. Geometry is masked, never dropped.
;;
;; - With the clip in place, the single-panel and composite cases behave
;;   identically, which is the property we wanted.
