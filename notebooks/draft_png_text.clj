;; # Draft: PNG text sits where the SVG text sits
;;
;; Motivation. A plot has two rendering paths -- the SVG backend
;; (`render/svg.clj`) and the Java2D backend behind `:format :bufimg` and
;; `(pj/save pose "plot.png")` (`render/bufimg.clj`). Both consume the same
;; Membrane drawable tree, so the same plan should give the same picture. For
;; text it did not. The SVG backend emits `dominant-baseline="hanging"` plus
;; the label's `:text-anchor`, and turns a `membrane.ui.Rotate` into an SVG
;; `rotate()`. Membrane's Java2D backend read none of the three, so every
;; saved PNG placed all of its chrome text differently from the SVG of the
;; same pose:
;;
;; - y tick labels started at the axis and ran rightwards into the panel,
;;   over the data
;; - x tick labels, axis titles, legend text and facet strips sat half a
;;   label too far right
;; - every string sat about four pixels too low
;; - `:x-tick-angle` did nothing at all
;;
;; RESOLVED. `render/bufimg.clj` teaches the Java2D backend the same
;; contract, as `IDraw` extensions on `membrane.ui.Label` and
;; `membrane.ui.Rotate`. The regression guard is
;; `scicloj.plotje.text-placement-test`, which measures pixels -- the pose,
;; the draft, the plan and `pj/svg-summary` were all correct while the
;; picture was wrong, which is why this survived so long.
;;
;; This notebook is the visual check. Every section renders one pose twice,
;; SVG first and PNG second; the two should be indistinguishable apart from
;; the font, since Java2D draws with its own default rather than the
;; browser's.
;;
;; This notebook is exploratory -- it is not in chapters.edn and is not
;; part of the published book. Rendering it with
;; `(dev/make-gfm! "draft_png_text.clj")` does write
;; `test/draft_png_text_generated_test.clj` alongside the markdown, the same
;; way a chapter does; that file is a by-product rather than part of the
;; suite, since the same ground is covered by
;; `scicloj.plotje.text-placement-test`. Delete it after rendering, or leave
;; it and it will run with everything else.

(ns draft-png-text
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [scicloj.plotje.api :as pj])
  (:import [java.awt.image BufferedImage]))

;; ## Rendering a pose both ways
;;
;; Evaluating a pose renders it through the SVG path. Pinning
;; `:format :bufimg` renders the same pose through Java2D, which is the path
;; `pj/save` to a `.png` file takes.

(defn as-png
  "The same pose, rendered through the Java2D path instead of the SVG one."
  [pose]
  (pj/options pose {:format :bufimg}))

(defn raster
  "The pose rendered to a BufferedImage, for measuring."
  ^BufferedImage [pose]
  (pj/plot pose {:format :bufimg}))

;; ## Measuring a rendering
;;
;; The pictures below are the point of this notebook, but a picture cannot
;; say whether a label is one pixel or nine pixels out of place. These three
;; helpers read positions back out of a rendered raster, so each section can
;; state a number and check it.
;;
;; Text and data marks are dark; the page, the panel background and the grid
;; lines are all far lighter, so a single threshold separates ink from
;; everything else.

(def panel-background [232 232 232])

(def gridline [238 238 238])

(defn pixel
  "The red, green and blue components at one position."
  [^BufferedImage img x y]
  (let [v (.getRGB img (int x) (int y))]
    [(bit-and (bit-shift-right v 16) 0xff)
     (bit-and (bit-shift-right v 8) 0xff)
     (bit-and v 0xff)]))

(defn ink?
  "Whether the position carries drawn ink rather than background."
  [img x y]
  (< (/ (reduce + (pixel img x y)) 3.0) 180))

(defn panel-box
  "The panel background's bounding box, as {:left :right :top :bottom}.
   Anti-aliased text throws off single background-coloured pixels, so a
   column or row counts only when it carries a long run of them."
  [^BufferedImage img]
  (let [w (.getWidth img)
        h (.getHeight img)
        long-run? (fn [coords carries?] (> (count (filter carries? coords)) 50))
        xs (filter #(long-run? (range h) (fn [y] (= panel-background (pixel img % y))))
                   (range w))
        ys (filter #(long-run? (range w) (fn [x] (= panel-background (pixel img x %))))
                   (range h))]
    {:left (first xs) :right (last xs) :top (first ys) :bottom (last ys)}))

(defn ink-box
  "The bounding box of the ink inside a [x0 y0 x1 y1] region, or nil when
   the region is blank."
  [img [x0 y0 x1 y1]]
  (let [lit (for [x (range x0 x1) y (range y0 y1) :when (ink? img x y)] [x y])]
    (when (seq lit)
      {:left (reduce min (map first lit))
       :right (reduce max (map first lit))
       :top (reduce min (map second lit))
       :bottom (reduce max (map second lit))})))

;; ## Right-aligned text: the y tick labels
;;
;; A y tick label is emitted with `text-anchor="end"` so that its right edge
;; lands against the axis and the text reads back towards the margin. Drawn
;; from the left instead, it starts where it should end and covers the data.
;;
;; Large y values make the effect obvious, since the labels are wide. The x
;; domain here is far wider than the data on purpose, so the left of the
;; panel holds no data mark and any ink there is a misplaced label.

(def big-y-values
  (-> {:x [26 27 28] :y [1000000 2000000 3000000]}
      (pj/lay-point :x :y)
      (pj/scale :x {:domain [20 30]})
      (pj/options {:width 480 :height 320 :title "y tick labels"})))

big-y-values

(as-png big-y-values)

;; The labels end at the axis, and the strip of panel immediately inside the
;; axis is empty. Before the fix that strip held the labels themselves.

(let [img (raster big-y-values)
      {:keys [left top bottom]} (panel-box img)]
  {:panel-starts-at left
   :labels-end-at (:right (ink-box img [0 top left bottom]))
   :ink-inside-the-panel-edge (count (for [x (range (inc left) (+ left 60))
                                           y (range top bottom)
                                           :when (ink? img x y)]
                                       x))})

(kind/test-last
 [(fn [m] (and (<= (:labels-end-at m) (:panel-starts-at m))
               (zero? (:ink-inside-the-panel-edge m))))])

;; ## Centred text: the x tick labels
;;
;; An x tick label is emitted with `text-anchor="middle"` so that it centres
;; on its tick. Drawn from the left, every one of them shifts right by half
;; its own width -- a small enough error to look like a rounding artefact
;; rather than a missing feature, which is part of why it went unnoticed.
;;
;; The x domain above was chosen so that every tick label is two digits wide.
;; Equal widths make the band of tick-label ink symmetric about the panel, so
;; its centre reads the anchoring directly: centred labels put the band's
;; centre on the panel's, and labels drawn from the left shift the whole band
;; right by half a label.

(let [img (raster big-y-values)
      {:keys [left right bottom]} (panel-box img)
      band (ink-box img [0 (inc bottom) (.getWidth img) (+ bottom 18)])]
  {:panel-centre (/ (+ (double left) right) 2.0)
   :tick-label-band-centre (/ (+ (double (:left band)) (:right band)) 2.0)})

(kind/test-last
 [(fn [m] (<= (abs (- (:tick-label-band-centre m) (:panel-centre m))) 2.0))])

;; ## The hanging baseline
;;
;; Membrane's Java2D backend draws a string one line-height below its origin;
;; the SVG backend's hanging baseline instead centres the font's
;; ascent-to-descent box on a band of the font's own size, starting at the
;; origin. That band is what the layout reserves -- a y tick label is placed
;; half a font-size above its tick so that the label straddles it -- so the
;; check is that each label comes out centred on its own gridline.
;;
;; The differences below are in pixels, one per tick.

(let [img (raster big-y-values)
      {:keys [left top bottom]} (panel-box img)
      centre (fn [[a b]] (/ (+ (double a) b) 2.0))
      runs (fn [coords]
             (reduce (fn [acc c]
                       (if (and (seq acc) (<= (- c (peek (peek acc))) 2))
                         (conj (pop acc) (conj (peek acc) c))
                         (conj acc [c])))
                     [] coords))
      grid-rows (filter #(= gridline (pixel img (+ left 20) %)) (range top bottom))
      label-rows (filter (fn [y] (some #(ink? img % y) (range (- left 40) left)))
                         (range top (inc bottom)))]
  (mapv (fn [rows row] (- (centre [(first rows) (peek rows)]) row))
        (runs label-rows)
        grid-rows))

(kind/test-last [(fn [diffs] (every? #(<= (abs (double %)) 1.0) diffs))])

;; ## Rotation
;;
;; `:x-tick-angle` turns each tick label about its own origin, which is how a
;; chart with long category names stays readable. `membrane.ui.Rotate` carries
;; children, so Membrane's default drawable implementation walked straight
;; through it and drew them upright: the option changed the SVG and did
;; nothing whatever to the PNG.

(def slanted-ticks
  (-> {:category ["alpha" "beta" "gamma" "delta"] :value [3 5 2 4]}
      (pj/lay-bar :category :value)
      (pj/options {:width 480 :height 320 :x-tick-angle -45
                   :title "x-tick-angle -45"})))

slanted-ticks

(as-png slanted-ticks)

;; A slanted label occupies a much taller band below the panel than an
;; upright one. Both numbers are rows of ink in the PNG.

(defn tick-label-band-height
  "Rows of ink in the first band below the panel -- the tick labels, above
   the gap that separates them from the axis title."
  [pose]
  (let [img (raster pose)
        {:keys [left right bottom]} (panel-box img)
        rows (filter (fn [y] (some #(ink? img % y) (range left right)))
                     (range (inc bottom) (.getHeight img)))]
    (->> rows
         (reduce (fn [acc y]
                   (if (and (seq acc) (<= (- y (peek (peek acc))) 2))
                     (conj (pop acc) (conj (peek acc) y))
                     (conj acc [y])))
                 [])
         first
         count)))

{:upright (tick-label-band-height (pj/options slanted-ticks {:x-tick-angle 0}))
 :slanted (tick-label-band-height slanted-ticks)}

(kind/test-last
 [(fn [m] (> (:slanted m) (* 2 (:upright m))))])

;; ## The Known Limitation that was not one
;;
;; The book carried an entry saying that saving to PNG "truncates the rotated
;; y-axis label after ~6 characters", with the root cause given as Membrane's
;; Java2D backend clipping the rotated-text bounding box, and the resolution
;; as an upstream fix in Membrane. It was none of that. The axis title is
;; drawn inside a `Rotate`, so the Java2D backend drew it horizontally; laid
;; out flat it ran straight out of the narrow margin reserved for it, and the
;; rest was cut off. The rotation fix above resolves it, and the entry is
;; gone from `known_limitations.clj`.

(def long-axis-title
  (-> {:x [1 2 3] :y [4 5 6]}
      (pj/lay-point :x :y)
      (pj/options {:width 480 :height 320
                   :y-label "measured concentration (mg/L)"
                   :x-label "elapsed time (minutes)"})))

long-axis-title

(as-png long-axis-title)

;; The title is drawn turned through a quarter turn, so it is tall rather
;; than wide -- and it is the whole string, not the first six characters of
;; it. Ink in the left-most columns spans most of the panel's height.

(let [img (raster long-axis-title)
      {:keys [top bottom]} (panel-box img)
      title (ink-box img [0 0 20 (.getHeight img)])]
  {:title-ink-height (- (:bottom title) (:top title))
   :panel-height (- bottom top)})

(kind/test-last
 [(fn [m] (> (:title-ink-height m) (* 0.5 (:panel-height m))))])

;; ## Text marks
;;
;; The same contract governs the text a `pj/lay-text` or `pj/lay-label` mark
;; takes from a column. Those marks position themselves -- they translate by
;; their own `:align-x` and `:align-y` rather than setting a `:text-anchor`
;; -- so the anchoring change does not touch them, but the baseline one does.
;; A label's box is sized from the font, so text drawn a line-height too low
;; hung its descenders below the border. Deliberately descender-heavy text at
;; a large font size makes that easy to see: look at where the tails of the
;; g, j, p, q and y fall relative to the rounded border.

(def boxed-labels
  (-> {:x [1 2 3] :y [2 5 3] :tag ["gp" "yj" "pq"]}
      (pj/lay-point :x :y {:size 5})
      (pj/lay-label {:text :tag :font-size 20 :align-x :center :nudge-y 0.4})
      (pj/scale :y {:domain [1.5 6.0]})
      (pj/options {:width 480 :height 320 :title "descenders and the box border"})))

boxed-labels

(as-png boxed-labels)

;; ## A whole plot
;;
;; Every kind of chrome text at once -- title, facet strips, legend title and
;; entries, both axis titles, both sets of tick labels.

(def everything
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/facet :species)
      (pj/options {:width 700 :height 300 :title "Iris"})))

everything

(as-png everything)

;; ## Notes
;;
;; - The vertical rule is not "put the top of the text at the origin". Chrome
;;   was measured: at font-size 11 its hanging baseline sits 10 pixels below
;;   the origin and the digits' ink is centred on the origin plus 5.5, which
;;   is the ascent-to-descent box centred on a band of the font's own size.
;;   Deriving that from the font metrics rather than from a fixed fraction
;;   keeps it right at other sizes.
;;
;; - The two extensions in `render/bufimg.clj` replace Membrane's own
;;   implementations for `Label` and `Rotate` rather than wrapping a Plotje
;;   record the way `WithStrokeDash` does. Keeping the Membrane record means
;;   its `IBounds` still drives layout, and the roughly twenty call sites
;;   that already attach `:text-anchor` needed no change.
;;
;; - The fonts differ between the two paths and are meant to: the SVG names a
;;   system font stack the browser resolves, while Java2D draws with its own
;;   default. Glyph widths differ by a few percent, so the pictures above are
;;   the same layout in two typefaces, not two copies of one image.
