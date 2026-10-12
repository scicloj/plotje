;; # Panels
;;
;; A plot can be one set of axes or a grid of them, and which you get is
;; never chosen directly. A leaf pose draws one panel, however many
;; layers it holds and whatever columns they name. More panels come from
;; three places:
;;
;; - **`pj/facet` over the categories of a column** -- a panel per
;;   category.
;; - **`pj/facet` over columns** -- a panel per column, or per pair of
;;   columns.
;; - **`pj/arrange`** -- a panel per pose given to it.
;;
;; This chapter shows each, and how they combine.
;;
;; Read [Poses](./plotje_book.pose_model.html) first for what a leaf is.

(ns plotje-book.panels
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

(def measurements
  {:height  [1 2 3 4]
   :weight  [1 2 3 4]
   :depth   [2 3 4 5]
   :species ["a" "a" "b" "b"]})

;; ## One leaf is one panel

;; A pose built from one dataset with one mapping is a single leaf, and
;; draws a single panel:

(-> measurements
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; Adding a layer does not add a panel. The layers of a pose share its
;; axes and paint over one another, which is the point of them:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-line :height :weight))

(kind/test-last [(fn [v] (= 1 (:panels (pj/svg-summary v))))])

;; That holds for a layer naming other columns too. Its columns are drawn
;; against the axes the panel already has, each axis covers every column
;; drawn on it and is titled by all of them, and each layer takes a
;; colour and a legend entry naming its columns:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (some #{"height, depth"} (:texts s)))))])

;; The colour per column is a default, and it is decided layer by
;; layer: a layer with no `:color` specified takes the colour and the
;; legend entry of its column. A layer that specifies a colour is drawn
;; in that colour and has no legend entry. Here a black line is added,
;; and the two point layers keep the default colours:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight)
    (pj/lay-line :height :weight {:color "black"}))

(kind/test-last [(fn [v] (let [plan (pj/plan v)]
                           (and (= ["height" "depth"]
                                   (mapv :label (:entries (:legend plan))))
                                (contains? (:colors (pj/svg-summary v)) "rgb(0,0,0)"))))])

;; A `:color` on the pose reaches every layer, so every layer has a
;; colour specified and none takes the default. The layers are coloured
;; by the pose's `:color`, and the axis is still titled by every column:

(-> measurements
    (pj/pose {:color :species})
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (some #{"height, depth"} (:texts s))
                                (some #{"species"} (:texts s))
                                ;; no entry naming a column
                                (not-any? #{"height" "depth"} (:texts s)))))])

;; The default is also left out of a panel where one layer is coloured
;; by a column. The legend of that panel lists the values of the column,
;; and a column name among them would read as one more value. The
;; second layer below has no `:color` specified and is drawn in the
;; default colour:

(-> measurements
    (pj/lay-point :height :weight {:color :species})
    (pj/lay-point :depth :weight))

(kind/test-last [(fn [v] (= ["a" "b"]
                            (mapv :label (:entries (:legend (pj/plan v))))))])

;; To name the second layer in that legend, specify its colour as a
;; value read through the color scale: `{:color {:value "depth" :scale
;; true}}`.

;; ### The panel's columns

;; A `pj/lay-*` call keeps its columns on its own layer, so a pose built
;; by such calls has no mapping of its own. The panel still has columns:
;; on each axis, the column the pose names, and where the pose names
;; none there, the column of the first layer that names one. A layer
;; that names no columns reads both of the panel's columns:

(-> measurements
    (pj/lay-point :height :weight)
    pj/lay-smooth)

(kind/test-last [(fn [v] (= [:height :weight]
                            ((juxt :x :y) (second (:layers (pj/draft v))))))])

;; The layer that names no columns may be written first. A `pj/lay-*`
;; call does not read the data's columns when it is called; the layer
;; is given its columns when the pose is drawn, so a later call may
;; name them:

(-> measurements
    pj/lay-smooth
    (pj/lay-point :height :weight))

(kind/test-last [(fn [v] (= [[:height :weight] [:height :weight]]
                            (mapv (juxt :x :y) (:layers (pj/draft v)))))])

;; A layer that names one column reads the panel's column on the other
;; axis, where its layer type draws from two columns. The line below
;; names `:depth` on `:y` and is drawn against the panel's `:height`:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-line {:y :depth}))

(kind/test-last [(fn [v] (and (= [:height :depth]
                                 ((juxt :x :y) (second (:layers (pj/draft v)))))
                              (some #{"weight, depth"} (:texts (pj/svg-summary v)))))])

;; A layer type that can draw from one column is not given a second
;; column. A rug draws from one column, so the rug below marks `:depth`
;; along the `:x` axis and reads no `:y`:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-rug {:x :depth}))

(kind/test-last [(fn [v] (let [rug (second (:layers (pj/draft v)))]
                           (and (= :depth (:x rug))
                                (nil? (:y rug)))))])

;; ## A panel per column

;; To draw the two columns apart, split the pose over them with
;; `pj/facet`. Each layer naming one of the columns is drawn only in
;; that column's panel:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight)
    (pj/facet {:x [:height :depth]}))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; The cells of a facet over columns are made from the panel's columns,
;; and the facet replaces the column on the axis it names. Below, the
;; point layer names `:weight`, so it is drawn in the `:weight` panel.
;; The `:depth` panel keeps the panel's `:height` on `:x`. No layer
;; names `:depth`, so that panel draws the layer the data chooses for
;; `:height` and `:depth`, which is a scatter too:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/facet {:y [:weight :depth]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= {:panels 2 :points 8}
                                   (select-keys s [:panels :points]))
                                ;; each panel titled by its own column on :y,
                                ;; and the shared :x titled once
                                (= ["weight" "depth" "height"]
                                   (filterv #{"weight" "depth" "height"} (:texts s))))))])

;; Where one layer is meant for every column, name the columns in the
;; facet alone. The panels share the axis they agree about, `:weight`,
;; and title it once:

(-> measurements
    (pj/pose {:y :weight})
    pj/lay-point
    (pj/facet {:x [:height :depth]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 1 (count (filter #{"weight"} (:texts s)))))))])

;; ## Arranging poses side by side

;; `pj/arrange` takes finished poses and makes each one a cell of a
;; single composite:

(pj/arrange [(pj/lay-point measurements :height :weight)
             (pj/lay-point measurements :depth :weight)])

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; A composite pose can also be written directly, which
;; [Composition](./plotje_book.composition.html#explicit-composite-poses)
;; covers. It is the same structure `pj/arrange` builds.

;; ## Faceting multiplies the panels

;; Faceting takes the leaf and cuts it into one panel per category of
;; the faceting column. Two species give two panels:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/facet :species))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; The count follows the data, not the call. The same call on a column
;; with four categories gives four panels:

(-> {:height [1 2 3 4] :weight [1 2 3 4] :site ["p" "q" "r" "s"]}
    (pj/lay-point :height :weight)
    (pj/facet :site))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; Every panel of a facet over categories shares one axis range, so the
;; panels can be read against each other. That is what makes faceting different from
;; arranging two poses that happen to hold the same columns.

;; ## Combining them

;; A facet over categories cuts each panel a pose draws into one per
;; category. A pose whose layers name different columns draws one panel,
;; so faceting it by `:species` draws two:

(-> measurements
    (pj/lay-point :height :weight)
    (pj/lay-point :depth :weight)
    (pj/facet :species))

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; Columns and categories cross in one `pj/facet` call, the categories
;; across and the columns down:

(-> measurements
    (pj/pose {:y :weight})
    pj/lay-point
    (pj/facet {:col :species :row {:x [:height :depth]}}))

(kind/test-last [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; `:col` and `:row` are aesthetics, so a facet written on an arranged
;; pose reaches every cell:

(-> (pj/arrange [(pj/pose measurements :height :weight)
                 (pj/pose measurements :depth :weight)])
    (pj/lay-point)
    (pj/facet :species))

(kind/test-last
 [(fn [v] (= 4 (:panels (pj/svg-summary v))))])

;; ## See Also
;;
;; - [Poses](./plotje_book.pose_model.html) -- leaf and composite poses
;; - [Composition](./plotje_book.composition.html) -- arranging poses,
;;   shared scales, and the patterns built from them
;; - [Faceting](./plotje_book.faceting.html) -- faceting in practice,
;;   including grids of two columns
;; - [Pose Rules](./plotje_book.pose_rules.html) -- the rules that
;;   decide where a `pj/lay-*` call's layer goes
