;; # Customization
;;
;; How to adjust the look of a plot: dimensions, labels, scales,
;; mark styling, palettes, themes, and legend placement.
;;
;; Other appearance topics live in their natural homes:
;; column-to-aesthetic mapping in
;; [Core Concepts](./plotje_book.core_concepts.html), reference
;; lines and bands in
;; [Core Concepts](./plotje_book.core_concepts.html) (constant
;; positions) and
;; [Timelines](./plotje_book.timelines.html) (temporal intercepts),
;; and tooltips/brushing in
;; [Interactivity](./plotje_book.interactivity.html).

(ns plotje-book.customization
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Clojure2d -- palette and gradient discovery
   [clojure2d.color :as c2d]))

;; ## Dimensions

;; A wide, short plot.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:width 800 :height 250}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (>= (:width s) 800))))])

;; A tall, narrow plot.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:width 300 :height 500}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (>= (:width s) 300))))])

;; ## Titles and Labels

;; Override axis labels and add a title.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Iris Sepal Measurements"
                 :x-label "Length (cm)"
                 :y-label "Width (cm)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Iris Sepal Measurements"} (:texts s)))))])

;; Add a subtitle and caption for context.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Iris Measurements"
                 :subtitle "Sepal dimensions across three species"
                 :caption "Source: Fisher's Iris dataset (1936)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Iris Measurements"} (:texts s))
                                (some (fn [t] (.contains ^String t "Sepal dimensions")) (:texts s)))))])

;; Legend titles default to the column name. Override with
;; `:color-label`, `:size-label`, or `:alpha-label`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:color-label "Species (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Species (override)"} (:texts s)))))])

;; The size legend title comes from `:size-label`:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:size :petal-length})
    (pj/options {:size-label "Petal length (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Petal length (override)"} (:texts s)))))])

;; And `:alpha-label` overrides the alpha legend title:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:alpha :petal-length})
    (pj/options {:alpha-label "Petal length (override)"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (some #{"Petal length (override)"} (:texts s)))))])

;; ### Color and fill
;;
;; Most marks expose `:color` as the encoding channel -- scatter
;; dots, lines, bar interiors, area fills, violins, lollipops -- all
;; styled with `:color` and named via `:color-label` in the legend.
;; The separate `:fill` channel is currently reserved for the heatmap
;; family: `lay-tile` (and the `:bin2d` output beneath
;; `lay-density-2d`) reads the encoded value as a continuous fill,
;; with its own legend title override `:fill-label`:

(-> {:x [1 2 3 1 2 3] :y [1 1 1 2 2 2] :z [10 20 30 40 50 60]}
    (pj/lay-tile :x :y {:fill :z})
    (pj/options {:fill-label "Score"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (some #{"Score"} (:texts s))
                                (pos? (:visible-tiles s)))))])

;; **Coming from ggplot2.** ggplot's `colour=` (stroke) and `fill=`
;; (interior) split is partial in Plotje today. On filled marks like
;; `lay-bar`, `lay-area`, and `lay-violin`, the `:color` aesthetic
;; paints the interior; there is no separate stroke channel, and
;; `:fill` is not accepted. A `lay-bar` styled with `{:color :species}`
;; produces one filled polygon per category:

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species {:color :species}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               fills (disj (:colors s) "none")]
                           (and (= 3 (:polygons s))
                                ;; three distinct interior colors
                                (= 3 (count fills)))))])

;; ## Rotating tick labels

;; When a categorical x-axis has many categories, or long category
;; names, the tick labels run into each other and become hard to
;; read. Rotate them with `:x-tick-angle`, given in degrees. A value
;; of -45 is a common diagonal that keeps the text legible while
;; saving horizontal room.

(-> {:product (map #(str "Product " %) (range 12))
     :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
    (pj/lay-bar :product :revenue)
    (pj/options {:x-tick-angle -45}))

(kind/test-last [(fn [v] (and (= 12 (:polygons (pj/svg-summary v)))
                              (.contains ^String (pr-str (pj/plot v)) "rotate(-45")))])

;; Plotje reserves extra vertical space below the panel for the
;; angled labels, scaled by the angle. When that automatic estimate
;; reserves too much or too little, set `:x-tick-label-pad` (in
;; pixels) to control the reserved height directly:

(-> {:product (map #(str "Product " %) (range 12))
     :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
    (pj/lay-bar :product :revenue)
    (pj/options {:x-tick-angle -45
                 :x-tick-label-pad 90}))

(kind/test-last [(fn [v] (= 12 (:polygons (pj/svg-summary v))))])

;; A label rotated this way extends down and to the left of its
;; tick. Very long names can run past the left edge of the plotting
;; area; see
;; [Known Limitations](./plotje_book.known_limitations.html).

;; ## Scales

;; Use a log scale for data spanning orders of magnitude.

(def exponential-data
  {:x (range 1 50)
   :y (map #(* 2 (Math/pow 1.1 %)) (range 1 50))})

;; Linear scale -- hard to see the structure.

(-> exponential-data
    (pj/lay-point :x :y)
    (pj/options {:title "Linear Scale"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 49 (:points s)))))])

;; Log y-scale -- reveals the exponential trend.

(-> exponential-data
    (pj/lay-point :x :y)
    (pj/scale :y :log)
    (pj/options {:title "Log Y Scale"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 49 (:points s)))))])

;; Lock the y-axis to a specific range.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :domain [0 6]})
    (pj/options {:title "Fixed Y Domain [0, 6]"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s)))))])

;; A domain narrower than the data turns the axis into a view window:
;; marks falling outside it are clipped to the panel, and the
;; underlying data is kept. This matches ggplot2's `coord_cartesian`,
;; which zooms the view, rather than scale limits, which drop rows.
;; Here the sepal-width domain is tightened to [3.0, 3.5], so points
;; above and below that band are clipped at the panel edge. All 150
;; observations are still rendered -- the marks sit behind a clip
;; region, one per panel.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :domain [3.0 3.5]})
    (pj/options {:title "Tight Y Domain [3.0, 3.5]"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (= 1 (:clips s)))))])

;; Pin exact tick locations with `:breaks` (ggplot2's
;; `scale_*_continuous(breaks=...)`).

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :y {:type :linear :breaks [2.0 3.0 4.0]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (every? (set (:texts s)) ["2" "3" "4"]))))])

;; Pair `:breaks` with `:labels` to render numeric positions with
;; custom tick text. The two vectors must match in count -- each
;; label is shown at its corresponding break. This is the path for
;; cases like a tile heatmap where the axis is numerically indexed
;; (1-7) but the natural labels are categorical (days of the week).

(-> (for [day (range 1 8) hour (range 0 24)]
      {:day day :hour hour :load (+ (* 0.3 (Math/sin (* 0.5 hour)))
                                    (* 0.2 (mod day 3)))})
    (pj/lay-tile :day :hour {:fill :load})
    (pj/scale :x {:type :linear
                  :breaks [1 2 3 4 5 6 7]
                  :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})
    (pj/options {:title "Weekly Load by Hour"}))

(kind/test-last
 [(fn [v] (let [texts (set (:texts (pj/svg-summary v)))]
            (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))])

;; Order a categorical axis explicitly with `:type :categorical`
;; and a `:domain` vector. Without this, categories appear in their
;; order of first occurrence in the data.

(-> {:size ["medium" "small" "large"]
     :count [12 30 7]}
    (pj/lay-bar :size :count)
    (pj/scale :x {:type :categorical :domain ["large" "medium" "small"]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)
                               labels (filter #{"large" "medium" "small"} (:texts s))]
                           (= ["large" "medium" "small"] (vec labels))))])

;; ### Log scale on visual channels
;;
;; `pj/scale` works on continuous visual channels too -- `:size`,
;; `:alpha`, `:fill`, and `:color`. When the encoded column spans
;; many orders of magnitude, a log scale spaces the legend ticks
;; logarithmically and maps the visual property (radius, alpha,
;; gradient color) in log-space, so each tick step represents the
;; same multiplicative ratio. `:categorical` does not apply to a
;; continuous encoding -- visual channels accept `:linear` (the
;; default) and `:log` only.

;; Point sizes from a column whose values jump by factors of ten.
;; Without `:scale :size :log`, the default linear mapping puts the
;; n=10 and n=100 points at nearly the same radius -- only n=1000
;; stands out. Linear scaling reflects absolute distance, which is
;; dominated by the largest value:

(-> {:user [:a :b :c] :n [10 100 1000]}
    (pj/lay-point :user :n {:size :n :x-type :categorical}))

(kind/test-last
 [(fn [v]
    (let [sizes (sort (:sizes (pj/svg-summary v)))]
      ;; Linear scaling: smallest two radii are within 50% of each
      ;; other; the largest radius is at least 3x the smallest.
      (and (= 3 (count sizes))
           (< (/ (second sizes) (first sizes)) 1.5)
           (> (/ (last sizes) (first sizes)) 3.0))))])

;; With `pj/scale :size :log`, each factor-of-10 step reflects the
;; same proportional jump in radius, so the n=10 and n=100 points
;; are now visibly distinct:

(-> {:user [:a :b :c] :n [10 100 1000]}
    (pj/lay-point :user :n {:size :n :x-type :categorical})
    (pj/scale :size :log))

(kind/test-last [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; The size legend's tick values are the original numbers (10,
;; 100, 1000), but the dot radii grow in log-space -- each step
;; reflects the same factor, matching what you see at the same data
;; values in the plot.

;; Tile heatmap with log-scaled fill:

(-> (for [r (range 5) c (range 5)]
      {:r r :c c :v (Math/pow 10.0 (/ (+ r c) 2.0))})
    (pj/lay-tile :r :c {:fill :v})
    (pj/scale :fill :log))

(kind/test-last [(fn [v] (>= (:visible-tiles (pj/svg-summary v)) 25))])

;; The continuous fill legend draws log-spaced tick labels along
;; the gradient bar so a tile's color reads as its log-space
;; position between the data minimum and maximum.
;;
;; To override the inferred type of a column (e.g. force a numeric
;; `:hour` column to render as categorical bands), see
;; [Inference Rules](./plotje_book.inference_rules.html).

;; ## Mark Styling

;; Pass `:alpha` and `:size` directly to layer functions.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species :alpha 0.5 :size 5}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 150 (:points s))
                                (contains? (:alphas s) 0.5)
                                (contains? (:sizes s) 5.0))))])

;; `:size` controls line thickness on line-based marks:

(-> {:x [1 2 3 4 5] :y [2 4 3 5 4]}
    (pj/lay-line :x :y {:size 3}))

(kind/test-last [(fn [v] (= 1 (:lines (pj/svg-summary v))))])

;; Alpha works on bars and polygons too.

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species {:alpha 0.4}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 3 (:polygons s))
                                (contains? (:alphas s) 0.4))))])

;; ## Text and Label Placement
;;
;; `pj/lay-text` and `pj/lay-label` place a label at a data point. By
;; default the label starts at the point and reads to the right,
;; centered on the point vertically. Two options move the label relative
;; to the point by choosing which part of the text the point pins:
;;
;; - `:align-x` -- `:left`, `:center`, or `:right` (default `:left`)
;; - `:align-y` -- `:top`, `:center`, or `:bottom` (default `:center`)
;;
;; The value names the part of the text placed at the point. `:align-x
;; :right` pins the right edge, so the label extends leftward; `:align-x
;; :center` straddles the point. `:align-y` reads in data orientation:
;; `:top` pins the top edge, so the text reads downward from the point,
;; and `:bottom` pins the bottom edge, so the text floats above it.
;;
;; Three labels on a column of points at the same x, one per horizontal
;; anchor -- the text fans right of, across, and left of the point:

(-> {:x [2 2 2] :y [3 2 1]}
    (pj/lay-point :x :y {:size 6 :color "#888888"})
    (pj/lay-text :x :y {:text :tag :align-x :left
                        :data {:x [2] :y [3] :tag ["align-x :left"]}})
    (pj/lay-text :x :y {:text :tag :align-x :center
                        :data {:x [2] :y [2] :tag ["align-x :center"]}})
    (pj/lay-text :x :y {:text :tag :align-x :right
                        :data {:x [2] :y [1] :tag ["align-x :right"]}}))

(kind/test-last
 [(fn [fr]
    (= [:left :center :right]
       (->> fr pj/plan :panels first :layers
            (filter #(= :text (:mark %)))
            (mapv #(-> % :style :align-x)))))])

;; A practical use of `:align-y`: float a value label just above each
;; bar by pinning the label's bottom edge to the bar top, centered over
;; the bar.

(-> {:species ["setosa" "versicolor" "virginica"]
     :pct     [33.3 33.3 33.3]}
    (pj/lay-bar :species :pct {:color "#a6cee3"})
    (pj/lay-text :species :pct {:text :pct :align-x :center :align-y :bottom}))

(kind/test-last
 [(fn [fr]
    (let [text-style (->> fr pj/plan :panels first :layers
                          (filter #(= :text (:mark %)))
                          first :style)]
      (and (= :center (:align-x text-style))
           (= :bottom (:align-y text-style)))))])

;; Every anchor value flows through for both `pj/lay-text` and
;; `pj/lay-label`; the defaults reproduce the original placement, and an
;; unrecognized value is rejected at plan time.

(kind/test-last
 [(fn [_]
    (let [text-style
          (fn [layer-fn mark opts]
            (->> (-> {:x [1] :y [1] :t ["a"]}
                     (layer-fn :x :y (merge {:text :t} opts)))
                 pj/plan :panels first :layers
                 (filter #(= mark (:mark %)))
                 first :style
                 (#(select-keys % [:align-x :align-y]))))]
      (and
       (= {:align-x :left :align-y :center} (text-style pj/lay-text :text {}))
       (= :left   (:align-x (text-style pj/lay-text :text {:align-x :left})))
       (= :center (:align-x (text-style pj/lay-text :text {:align-x :center})))
       (= :right  (:align-x (text-style pj/lay-text :text {:align-x :right})))
       (= :top    (:align-y (text-style pj/lay-text :text {:align-y :top})))
       (= :center (:align-y (text-style pj/lay-text :text {:align-y :center})))
       (= :bottom (:align-y (text-style pj/lay-text :text {:align-y :bottom})))
       (= {:align-x :right :align-y :top}
          (text-style pj/lay-label :label {:align-x :right :align-y :top}))
       (try (text-style pj/lay-text :text {:align-x :middle}) false
            (catch Exception _ true)))))])

;; ## Annotation Appearance
;;
;; Reference lines and bands are introduced in
;; [Core Concepts](./plotje_book.core_concepts.html); on temporal
;; axes, intercepts can be `LocalDate` / `Instant` values -- see
;; [Timelines](./plotje_book.timelines.html). This section covers
;; the appearance defaults you can override.
;;
;; Shaded bands draw at a default opacity of 0.15:

(:band-opacity (pj/config))

(kind/test-last [(fn [v] (= 0.15 v))])

;; Pass `{:alpha ...}` on a band layer to override:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/lay-band-v {:x-min 5.5 :x-max 6.5})
    (pj/lay-band-h {:y-min 3.0 :y-max 3.5 :alpha 0.3}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; Note: intercept and band-edge positions must be literal values
;; (numbers, or temporal values on a time axis) in this release. A
;; faceted plot with a different reference value per panel
;; (column-mapped intercept, ggplot2's
;; `geom_hline(aes(yintercept=...))`) is on the post-alpha roadmap.
;; Today, an annotation added once with the same intercept appears
;; on every panel of the faceted pose.

;; ## Palettes
;;
;; Pass `:palette` to override the default color cycle. It accepts a
;; vector of hex strings, a map from category to hex, or a keyword
;; naming one of the built-in palettes (`:set1`, `:set2`, `:dark2`,
;; `:tableau-10`, `:category10`, `:pastel1`, `:accent`, `:paired`, and
;; many more).
;;
;; For the full list of forms, the project-level / thread-local /
;; plot-level precedence chain, and the key table, see the
;; [Configuration](./plotje_book.configuration.html) chapter.

;; Custom vector:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette ["#E74C3C" "#3498DB" "#2ECC71"]}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; Named preset -- here `:dark2` for a high-contrast qualitative palette:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette :dark2}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Discovering Palettes and Gradients
;;
;; Plotje delegates color to the
;; [clojure2d](https://github.com/Clojure2D/clojure2d) library, which
;; bundles thousands of named palettes and gradients.  Use
;; `clojure2d.color/find-palette` and `clojure2d.color/find-gradient`
;; to search by regex pattern.

;; Find palettes whose name contains "budapest".

(c2d/find-palette #"budapest")

(kind/test-last [(fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))])

;; Find palettes whose name contains "set".

(c2d/find-palette #"^:set")

(kind/test-last [(fn [v] (and (sequential? v) (some #{:set1} v)))])

;; Find gradients related to "viridis".

(c2d/find-gradient #"viridis")

(kind/test-last [(fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))])

;; `c2d/palette` returns the colors for a given name.
;; Each color is a clojure2d `Vec4` (RGBA, 0-255 range).

(c2d/palette :grand-budapest-1)

(kind/test-last [(fn [v] (and (sequential? v) (pos? (count v))))])

;; ### Colorblind-friendly palettes
;;
;; For presentations and publications, consider palettes designed for
;; colorblind readers. Several good options are built in:
;;
;; - `:set2` -- muted qualitative, 8 colors
;; - `:dark2` -- dark qualitative, 8 colors
;; - `:khroma/okabeito` -- designed specifically for color vision deficiency
;; - `:tableau-10` -- Tableau default, high contrast

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:palette :khroma/okabeito}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; ## Theme
;;
;; Customize background color, grid color, and font size.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "White Theme"
                 :theme {:bg "#FFFFFF" :grid "#EEEEEE" :font-size 10}}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (= 150 (:points s))))])

;; ## Legend Position
;;
;; Control where the legend appears: `:right` (default), `:bottom`,
;; `:top`, or `:none`.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:legend-position :bottom}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 150 (:points s))
                                (< (:width s) 700))))])

;; Legend on top:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:legend-position :top}))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; No legend at all -- useful when the color encoding is documented
;; in the title or caption rather than a separate legend. The panel
;; takes the full width since no legend strip is reserved:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:legend-position :none}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          plan (pj/plan (-> (rdatasets/datasets-iris)
                            (pj/lay-point :sepal-length :sepal-width {:color :species})
                            (pj/options {:legend-position :none})))]
      (and (= 150 (:points s))
           (zero? (get-in plan [:layout :legend-w])))))])

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- the mapping and aesthetic vocabulary used throughout this chapter
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) -- where layer options, plot options, and configuration live
;; - [**Interactivity**](./plotje_book.interactivity.html) -- tooltips and brush selection

;; ## What's Next
;;
;; - [**Faceting**](./plotje_book.faceting.html) -- split any chart into panels by one or two variables
;; - [**API Reference**](./plotje_book.api_reference.html) -- complete function listing with docstrings
