;; # Glossary
;;
;; Key terms used throughout Plotje, with brief definitions
;; and code examples. This is a reference chapter -- skim or skip it
;; on a first read through the book, and come back to look up a term
;; when you meet it.

(ns plotje-book.glossary
  (:require
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]
   ;; clojure2d -- color palettes and gradients
   [clojure2d.color :as c2d]))

;; ## Pose
;;
;; A **pose** is the composable value in Plotje. A leaf pose
;; describes one plot panel; a composite pose contains other poses
;; arranged together. Every pose-shaping function in the API
;; (`pj/pose`, `pj/lay-*`, `pj/facet`, `pj/arrange`, `pj/options`,
;; `pj/scale`, `pj/coord`) takes a pose and returns a pose.
;; `pj/->pose` is the polymorphic lift (a function that wraps a
;; value into a richer type) -- it accepts raw data or a pose and
;; returns a pose, so any of the output functions can start from a
;; dataset directly.
;; Poses auto-render in
;; [Kindly](https://scicloj.github.io/kindly-noted/)-compatible
;; tools like [Clay](https://scicloj.github.io/clay/).

(def my-pose
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/options {:title "Iris"})))

my-pose

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; A pose is a plain Clojure value -- printed, the same value reveals
;; its underlying map shape with `:data`, `:layers`, and `:opts`. The
;; columns are on the layer, because the `pj/lay-point` call named them;
;; a mapping written with `pj/pose` sits on the pose's own `:mapping`:

(kind/pprint my-pose)

(kind/test-last
 [(fn [pose]
    (and (some? (:data pose))
         (nil? (:mapping pose))
         (= :sepal-length (get-in pose [:layers 0 :mapping :x]))
         (= :sepal-width (get-in pose [:layers 0 :mapping :y]))
         (= :species (get-in pose [:layers 0 :mapping :color]))
         (= "Iris" (get-in pose [:opts :title]))))])

;; ## Leaf Pose
;;
;; A **leaf pose** is a pose with no sub-poses. It draws one panel,
;; which all of its layers share, whatever columns the layers name. A
;; facet multiplies the panel: by the categories of a column, or by the
;; columns `pj/facet` is given. A leaf pose carries `:data`, a
;; `:mapping` from columns to aesthetics, and `:layers` -- the
;; chart-type layers attached to it. Created by `pj/pose` or
;; `pj/lay-*`.

;; ## Composite Pose
;;
;; A **composite pose** is a pose that contains other poses under
;; `:poses` plus an optional `:layout`. Created by `pj/arrange` and
;; `pj/marginal`. Its leaves render independently
;; and are tiled into the final plot.
;;
;; A composite may hold a composite: a cell that is itself a composite
;; draws its own grid. Unequal cell weights are the one layout feature
;; `pj/arrange` does not expose -- build the composite as a literal map
;; with `:weights` and pass the map to `pj/pose`.

;; ## Arrange
;;
;; `pj/arrange` builds a composite pose from cells. A cell is a pose, a
;; composite, a column, a pair of columns, a mapping, or `nil` for a
;; cell left empty. Data or a pose written first reaches every cell.
;; The composite holds one sub-pose per row, and each row holds the
;; cells of that row. `pj/arrange` accepts `:cols`, `:align-panels`,
;; `:share-scales`, and every plot option `pj/options` takes, such as
;; `:title`, `:width` and `:height`. A cell that is a composite draws
;; its own grid. For unequal cell weights, which `pj/arrange` does not
;; expose, pass a literal map to `pj/pose`.

;; ## Layer Type
;;
;; A **layer type** is the bundle of mark + stat + position that
;; determines how data becomes a visual element. It is a context-free
;; recipe; placing it on a pose produces a *layer* (next entry).
;; See the [Layer Types](./plotje_book.layer_types.html#layer-types) chapter for
;; detailed tables of all built-in layer types, marks, stats, and
;; positions.

;; ## Layer
;;
;; A **layer** is a layer type placed on a pose, optionally with
;; scoped mappings. Created by `pj/lay-*`.
;;
;; Layers attach to poses in three ways, depending on what you
;; pass to `pj/lay-*`:
;;
;; - **Bare** -- `pj/lay-*` without columns attaches a layer that is
;;   drawn on every panel of the pose, reading that panel's columns --
;;   the ones `pj/pose` wrote, or the ones the first `pj/lay-*` call
;;   naming columns named. A layer naming one column reads the panel's
;;   other column where its layer type draws from two.
;; - **Matching columns** -- `pj/lay-*` with columns that match the
;;   most recent matching leaf reuses that leaf, so the new layer
;;   joins the existing panel.
;; - **Non-matching columns** -- on a leaf, the layer keeps the
;;   columns it names and draws on the leaf's panel; on a composite, a
;;   new leaf is appended with the layer attached.
;;
;; In each case the columns a `pj/lay-*` call names are written on the
;; layer's own mapping, never on the pose's.

(-> my-pose :layers first :layer-type)

(kind/test-last [(fn [k] (= :point k))])

;; ## Panel's Columns
;;
;; The **panel's column** on an axis is the column the pose names
;; there, and where the pose names none, the column of the first layer
;; that names a column there. A layer naming no column reads the
;; panel's column on both axes. A layer naming one column reads the
;; panel's column on the other axis where its layer type draws from two
;; columns. The
;; [Layer Types](./plotje_book.layer_types.html#columns-a-layer-type-draws-from)
;; chapter lists how many columns each layer type draws from.

;; ## Mark
;;
;; The **mark** is the visual shape shown for each data point or
;; group. Several layer types may share the same mark -- for
;; instance, `:line` and `:smooth` both produce lines, and `:area`
;; and `:density` both produce filled regions.
;; See the [Layer Types](./plotje_book.layer_types.html#marks) chapter for
;; a table of all built-in marks.

;; ## Stat
;;
;; A **stat** (statistical transform) processes raw data before
;; rendering. Each stat takes data-space inputs and produces the
;; geometry that its mark will show.
;; See the [Layer Types](./plotje_book.layer_types.html#stats) chapter for
;; a table of all built-in stats.

;; ## Position
;;
;; A **position** adjustment determines how overlapping marks are
;; placed: kept at their data values (`:identity`), dodged
;; side-by-side along a categorical band (`:dodge`), stacked
;; end-to-end so bar tops sit on the previous bar's top (`:stack`),
;; or normalized to fill `[0, 1]` proportions (`:fill`).
;; Position runs between stat computation and rendering. `:stack`
;; and `:fill` rewrite values in **data space**, before the scales
;; read them; `:dodge` leaves values untouched and records which slot
;; each mark takes in its band, which becomes an offset in **drawing
;; space** when the mark is drawn. You can override the default
;; position by writing `:position` in the layer's mapping. When multiple
;; layers share `:position :dodge`, they are coordinated together --
;; error bars automatically align with bars.
;;
;; A position adjustment is one of the [roles](#role) a distinction
;; can be given -- a slot within a band, or a place in a pile -- and
;; the only one written as a [layer setting](#layer-settings) rather
;; than as an aesthetic.
;; See the [Layer Types](./plotje_book.layer_types.html#positions) chapter for
;; a table of all built-in positions.

(def tips {:day ["Mon" "Mon" "Tue" "Tue"]
           :count [30 20 45 15]
           :meal ["lunch" "dinner" "lunch" "dinner"]})

(-> tips
    (pj/lay-bar :day :count {:color :meal :position :stack}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          groups (-> tips
                     (pj/lay-bar :day :count
                                 {:color :meal :position :stack})
                     pj/plan
                     (get-in [:panels 0 :layers 0 :groups]))
          lunch-bar (first groups)
          dinner-bar (second groups)]
      (and (= 4 (:polygons s))
           ;; The last group is laid down first, so dinner sits on the
           ;; baseline and lunch is lifted onto it -- the order the
           ;; legend lists the two in, reading down from its top.
           (every? zero? (:y0s dinner-bar))
           (every? pos? (:y0s lunch-bar)))))])

;; ## Draft
;;
;; A **draft** is the record produced by `pj/draft`. For a leaf
;; pose, it is a `LeafDraft` carrying `:layers` (a vector of flat
;; maps, one per applicable layer with merged pose-and-layer scope)
;; and `:opts` (the pose-level options that flow into the plan
;; stage). For a composite pose, it is a `CompositeDraft` carrying
;; per-leaf drafts plus composite layout geometry. Draft layers carry all the
;; information the plan stage needs: data, columns, mark, stat,
;; color, grouping.
;;
;; `pj/draft` is useful for inspecting exactly what the plan stage
;; will consume before any domains, ticks, or coordinate math are
;; computed.
;;
;; Keys prefixed with double underscores (e.g. `:__panel-idx`) are
;; internal markers used by later stages and follow the Clojure
;; convention "do not consume." Ignore them when reading a draft.

(-> my-pose pj/draft kind/pprint)

(kind/test-last [(fn [d] (and (pj/leaf-draft? d)
                              (= 1 (count (:layers d)))
                              (= :point (:mark (first (:layers d))))))])

;; ## Draft Layer
;;
;; A **draft layer** is one entry of a draft's `:layers` vector --
;; a single map that bundles the layer type, the merged mappings
;; (pose + layer scopes), and the effective dataset for one (leaf,
;; applicable-layer) pair. It is the specification of what the
;; renderer will draw for that layer, before any geometry, domains,
;; or ticks are computed. The plan layer (entry below) is the same
;; idea after geometry has been resolved.

(-> my-pose pj/draft :layers first kind/pprint)

(kind/test-last
 [(fn [d]
    (and (some? (:data d))
         (= :sepal-length (:x d))
         (= :sepal-width (:y d))
         (= :species (:color d))
         (= :point (:mark d))))])

;; ## Mapping
;;
;; A **mapping** maps a column (or a written value) to an
;; aesthetic. Each aesthetic has a [role](#role), and there are four:
;;
;; - **Positional aesthetics** (`:x`, `:y`, plus `:x-end`, `:y-end`,
;;   `:x-min`, `:x-max`, `:y-min`, `:y-max` for marks that need them)
;;   place each mark.
;; - **Appearance aesthetics** (`:color`, `:size`, `:alpha`, `:shape`,
;;   `:text`, `:tooltip`, `:fill`) shape how each mark looks.
;; - **Grouping aesthetic** (`:group`) splits the data and draws
;;   nothing of its own.
;; - **Panel aesthetics** (`:col`, `:row`) give each value a panel of
;;   its own. `pj/facet` and `pj/facet-grid` write them.
;;
;; A mapping can be written in full, saying which of its two readings
;; -- the column or the value -- it means, and which side of the scale
;; to read it through. `{:column :species}` names the column even where
;; a value of that name could be drawn, `{:value "red"}` names the
;; value even where the data carries a column called red, and a
;; `:scale false` beside either draws it as it stands.
;;
;; Mappings live on a pose -- where they flow into every layer
;; attached to it -- or on a single layer, where they scope to that
;; layer alone. Lower scope wins on conflict; an explicit `nil`
;; cancels a mapping inherited from above.
;;
;; **The mapping** of a pose or of a layer is also the name of the map
;; these are written in: the final argument of `pj/pose` and of every
;; `pj/lay-*` function, stored under `:mapping`. A pose's mapping takes
;; aesthetics. A layer's mapping takes aesthetics and that layer's
;; [layer settings](#layer-settings).

;; ## Aesthetic
;;
;; An **aesthetic** is a property of a mark that can be mapped to a
;; data column or fixed to a written value. Each one has a
;; [role](#role) -- what a distinction given to it is put to work as
;; -- and there are four:
;;
;; **Positional aesthetics** -- where the mark sits:
;;
;; | Key | Controls | Column type |
;; |:----|:---------|:------------|
;; | `:x` | Where the mark sits horizontally | Numerical, temporal, or categorical |
;; | `:y` | Where the mark sits vertically | Numerical, temporal, or categorical |
;; | `:x-end`, `:y-end` | The far end of a segment; `:x-end` is also the right edge of an interval bar | Same type as `:x` or `:y`, or a written value |
;; | `:x-min`, `:x-max` | Edges of a vertical band | No column -- a written value only |
;; | `:y-min`, `:y-max` | An errorbar's bounds, or the edges of a horizontal band | Same type as `:y`, or a written value |
;;
;; **Appearance aesthetics** -- how the mark looks:
;;
;; | Key | Controls | Column type |
;; |:----|:---------|:------------|
;; | `:color` | Fill/stroke color | Categorical or numerical |
;; | `:size` | How large a mark is drawn -- a point's radius | Numerical |
;; | `:alpha` | Opacity | Numerical |
;; | `:shape` | Point shape | Categorical |
;; | `:text` | Label content | Any |
;; | `:fill` | Tile gradient color | Numerical |
;;
;; **Grouping aesthetic** -- which rows are drawn together:
;;
;; | Key | Controls | Column type |
;; |:----|:---------|:------------|
;; | `:group` | Splits the layer into one drawn group per value | Categorical |
;;
;; **Panel aesthetics** -- which panel a row is drawn in:
;;
;; | Key | Controls | Column type |
;; |:----|:---------|:------------|
;; | `:col` | A panel per value, laid out across | Categorical, or a vector of columns united into one key |
;; | `:row` | A panel per value, laid out down | The same |
;;
;; The layer's data decides: a value naming one of its columns is a
;; column reference, and anything else is the value itself --
;; `"#E74C3C"`, `"red"` or `:red` on `:color`, `0.5` on `:alpha` --
;; setting that aesthetic for every mark. It has to be a value the
;; aesthetic can draw: `0.5` on `:text` names no column and is no
;; label either, so it is reported rather than drawn. Two aesthetics
;; have no reading for a value at all and say so -- `:fill` and
;; `:group` take a column and nothing else.
;;
;; To say which you mean where both readings fit, write the mapping in
;; full: `{:column "red"}` or `{:value "red"}`.
;;
;; A single layer can mix the readings: positional column refs (`:x`,
;; `:y`), an appearance column ref (`:color :species`,
;; `:size :petal-length`), and a written appearance value (`:alpha 0.7`,
;; the same opacity for every point):

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width
                  {:color :species :size :petal-length :alpha 0.7}))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= 150 (:points s))
           ;; :alpha 0.7 is a written value -- every point gets the
           ;; same opacity, so the rendered set has a single alpha.
           (= #{0.7} (:alphas s)))))])

;; ## Role
;;
;; A **distinction** is a set of things to tell apart, and it is made
;; of columns -- one column's values, or several columns united into
;; one key. A distinction's **role** is what it is put to work as: it
;; answers where the separated marks go, and how a reader tells them
;; apart. Every aesthetic carries one, and `pj/aesthetic-roles` says
;; which:

(kind/table
 {:column-names [:aesthetic :role]
  :row-vectors (->> (pj/aesthetic-roles)
                    (sort-by (comp str key))
                    (mapv (fn [[k role]] [k role])))})

(kind/test-last
 [(fn [_] (= #{:positional :appearance :grouping :panel}
             (set (vals (pj/aesthetic-roles)))))])

;; | Role | Where the marks go | How they are told apart |
;; |:--|:--|:--|
;; | `:positional` | at the value, along an axis | by where they sit |
;; | `:appearance` | in one place | by a colour, a size, a shape, under a legend |
;; | `:grouping` | in one place | not at all -- the split is in the data, not in the picture |
;; | `:panel` | a panel each | by a strip label |
;;
;; A role is not a category: a **category** is a value a categorical
;; column holds, which is the thing a role is given.
;;
;; Three ways of telling marks apart are not written as aesthetics:
;;
;; - A [position](#position) adjustment -- `:dodge`, `:stack`,
;;   `:fill` -- gives a distinction a slot within a band, or a place
;;   in a pile. It is written as a layer setting, and it tells the
;;   marks apart only by whatever aesthetic is also mapped.
;; - [`pj/arrange`](#arrange) gives each of several poses a plot of
;;   its own. It is written as a call, and what it separates is poses
;;   rather than a column's values.
;; - A facet over columns, `(pj/facet pose {:y [:hwy :cty]})`, gives
;;   each column a panel of its own. It is kept under the pose's
;;   `:facet` key, and what it separates is columns rather than the
;;   values of one column.
;;
;; So the same question -- what becomes of a distinction -- is
;; answered by an aesthetic for four roles, by a layer setting for one,
;; by a call for another, and by the pose's `:facet` key for the
;; last.

;; ## Group
;;
;; A **group** is a subset of data that is processed and rendered
;; together. Mapping `:color` to a categorical column automatically
;; creates groups -- one per unique value. You can also create groups
;; without color using the `:group` key.

(-> (rdatasets/datasets-iris)
    (pj/lay-line :sepal-length :sepal-width {:group :species}))

(kind/test-last
 [(fn [v]
    (let [groups (-> (rdatasets/datasets-iris)
                     (pj/lay-line :sepal-length :sepal-width
                                  {:group :species})
                     pj/plan
                     (get-in [:panels 0 :layers 0 :groups]))]
      (and (= 3 (:lines (pj/svg-summary v)))
           ;; One group per :species value: the plan layer's
           ;; :groups vector has 3 entries.
           (= 3 (count groups))
           (= ["setosa" "versicolor" "virginica"]
              (mapv :label groups)))))])

;; ## Data Space

;; **Data space** is values in their original units -- centimeters,
;; dollars, dates, species names. A pose's mappings, a stat's inputs
;; and outputs, and a plan's domains and ticks are all in data space.
;; Every stage up to and including the plan keeps its geometry there,
;; which is why a plan is readable: the numbers in it are the numbers
;; from the dataset.

;; ## Drawing Space

;; **Drawing space** is where the rendered output lives: positions
;; measured in **drawing units** from the top left of the canvas, x
;; rightward and y downward. A **scale** maps one axis from data
;; space into drawing space and a **coord** decides how the two axes
;; land there, so the crossing happens at the membrane stage. The
;; plan holds its geometry in data space, alongside the layout
;; dimensions -- panel size, margins, label padding -- which are
;; already in drawing units.
;;
;; A drawing unit is one unit of the plot's `:width` and `:height`.
;; In SVG output it is one user unit; in PNG output it is one Java2D
;; unit. How much of a screen that covers depends on how the output
;; is displayed, which is why this book does not call it a pixel.
;; A browser scales the SVG to whatever box the page gives it, so a
;; drawing unit becomes one CSS pixel only at the plot's natural
;; size -- and a CSS pixel is not a screen pixel either, since a
;; high-resolution display draws two or more screen pixels for each
;; one, and zoom moves that ratio again.
;;
;; The one place this book does say pixel is the browser interaction
;; layer, because that is the unit a mouse event reports: the drag
;; threshold in the
;; [Interactivity](./plotje_book.interactivity.html#brush-selection) chapter is
;; three CSS pixels, not three drawing units.
;;
;; A position in drawing space is measured from the corner of some
;; rectangle, and this book names three of them: the **canvas**, the
;; **panel box** and the **drawing area**, each with its own entry
;; below. `pj/frames` reports the panel box and the drawing area for
;; every panel of a plot, and the canvas once -- the canvas belongs to
;; the plot rather than to any panel.

;; ## Canvas

;; The **canvas** is the whole output image: the rectangle the plot's
;; `:width` and `:height` describe, with its top left corner at the
;; origin of drawing space. Everything else a plot draws -- panels,
;; axis labels, title, legend -- sits inside it.
;;
;; It is the outermost of the three frames, and the only one shared by
;; every panel. In a composite, each cell's panels report their
;; rectangles against this same canvas, so all of them can be compared
;; directly.

;; ## Panel Box

;; The **panel box** is one panel including the axis margin around it:
;; the rectangle a panel is given in the grid, before anything is
;; drawn inside it. In a faceted plot the panel boxes tile the grid
;; edge to edge, one per facet variant, each offset from the last by a
;; panel's width or height.
;;
;; A mark that draws in the margin clips to this box rather than to the
;; drawing area. A rug tick is one: it sits outside the panel
;; background. See **Clip**.

;; ## Drawing Area

;; The **drawing area** is the panel background inside the axis
;; margin: the panel box less that margin on all four sides. Data marks
;; are drawn here and clipped to it.
;;
;; A `:in :drawing-area` layer measures its positions from this
;; rectangle's top left corner, so a note at drawing-space `[12 12]`
;; lands 12 units right and 12 units down from that corner.

;; ## Place
;;
;; A **place** is where a mark is drawn: a location in data space that
;; an axis can name. What a place is made of depends on what the axis
;; holds.
;;
;; On a numerical or temporal axis a place is a data value -- a sepal
;; length of 6.5, a year of 2007 -- and the axis widens to reach one
;; that falls outside the data. A number naming no location the axis
;; could reach is not a place: an infinity is a number, and a row
;; carrying one is dropped before anything is drawn.
;;
;; On a categorical axis the categories carry no numbers of their own,
;; so a place is counted among them from one: `1` is the first
;; category, `2` the second, and `1.5` the point halfway between the
;; two. Such an axis carries bands rather than a range of values, so it
;; cannot widen to reach a number the way a numerical axis widens to
;; reach a value. It runs from `0.5` to half a place past the last
;; category, and a number outside that is reported, naming the value,
;; the categories and where the axis ends.
;;
;; Every number written for a categorical axis is read as a place: a
;; value in an `:x` or `:y` slot, a rule's `:x-intercept`, a band's
;; `:x-min` and `:x-max`, the amount a [Shift](#shift) moves a mark by,
;; and the coordinate `pj/to-drawing` is asked about. To name a
;; category rather than count places to it, write the value in full as
;; `{:value ...}` -- which matters where the categories are themselves
;; numbers, since `{:value "2021"}` is a band and `2021` a place far
;; past the last one.
;;
;; A place adds no category to the axis. The note below sits between
;; the first band and the second, and the axis still carries three:

(-> {:team ["red" "green" "blue"] :score [3 5 4]}
    (pj/lay-bar :team :score {:color "#a6cee3"})
    (pj/lay-text {:x 1.5 :y 4.5 :align-x :center :text "between two"}))

(kind/test-last
 [(fn [v]
    (let [panel (-> v pj/frames :panels first)
          at (fn [c] (first (pj/to-drawing panel c 4.5)))]
      (and (= ["red" "green" "blue"] (-> v pj/plan :panels first :x-domain))
           ;; Counted from one, so a place and the category it counts
           ;; to meet, and 1.5 is the midpoint of the two it falls
           ;; between.
           (= (at 1) (at "red"))
           (< (abs (- (at 1.5) (/ (+ (at "red") (at "green")) 2.0))) 1e-9)
           ;; Three categories, so the axis runs from 0.5 to 3.5.
           (every? number? [(at 0.5) (at 3.5)])
           (every? (fn [bad]
                     (try (at bad)
                          false
                          (catch Exception e
                            (boolean (re-find #"past the ends of this axis"
                                              (ex-message e))))))
                   [0.4 3.6])
           ;; And on a numerical axis a place is a data value, while an
           ;; infinity names no location the axis could reach -- the row
           ;; carrying one is dropped before anything is drawn.
           (= 3 (:points (pj/svg-summary
                          (pj/plot (-> {:h [1 2 ##Inf 4] :w [1 2 3 4]}
                                       (pj/lay-point :h :w)))))))))])

;; A place is a location in data space, so it is not a drawing unit.
;; [Drawing Space](#drawing-space) measures the page, and the scales map
;; a place onto drawing units: `pj/to-drawing` is that mapping, and
;; `pj/to-data` reads it back.
;;
;; Reading back does not always return what went in. `pj/to-data`
;; answers the category whose band holds a coordinate, so a place on a
;; categorical axis comes back as one of the two categories it fell
;; between rather than as the number that produced it.
;; [Placing Marks](./plotje_book.placing_marks.html#mapping-a-categorical-axis)
;; covers both directions.

;; ## Shift
;;
;; A **shift** moves a mark by a constant amount measured in the axis's
;; own units. Written as `:dx` and `:dy` in a layer's options. The name
;; says a change in `:x` without naming a unit, because the unit is
;; whatever `:x` is measured in. A shift is independent of
;; `:position`: a mark shifts by the same amount within a dodge as it
;; does at identity.
;;
;; What that unit is depends on the axis. On a numerical or temporal
;; axis it is a data value, added to the coordinate before the scales
;; run. On a categorical axis the categories carry no numbers of their
;; own, so the unit is one band: `{:dx 0.5}` moves a mark half a band
;; along, which is halfway to the next category. The shift is applied
;; there once each category has been placed among the others.
;;
;; `:offset-x` and `:offset-y` are the other way of moving a mark: a
;; distance on the page in drawing units, applied after the scales, so
;; the same number means the same thing on every axis.
;;
;; Either way the axis domain is computed without the shift, so a `:dx`
;; large enough to carry a mark past the domain leaves it clipped at the
;; panel edge. Widen the domain with `pj/scale` when that happens. A
;; shift is therefore a move rather than a claim about where the datum
;; belongs; ggplot2's `nudge_x` and `nudge_y` differ here, expanding the
;; axis range to keep the shifted mark in view.

(-> {:x [1 2 3] :y [4 5 6]}
    (pj/lay-point :x :y {:dx 0.5}))

(kind/test-last
 [(fn [v]
    (let [xs (-> {:x [1 2 3] :y [4 5 6]}
                 (pj/lay-point :x :y {:dx 0.5})
                 pj/plan
                 (get-in [:panels 0 :layers 0 :groups 0 :xs]))]
      (and (= 3 (:points (pj/svg-summary v)))
           ;; The original xs were [1 2 3]; :dx 0.5 shifts each
           ;; by 0.5 before the scale is applied.
           (= [1.5 2.5 3.5] xs))))])

;; On a categorical axis the same `0.5` is half a band. The label below
;; is shifted from the first category and lands halfway to the second,
;; over the gap between the two bars. The light fill keeps it readable
;; where it crosses one:

(-> {:team ["red" "green" "blue"] :score [3 5 4]}
    (pj/lay-bar :team :score {:color "#a6cee3"})
    (pj/lay-text {:x {:value "red"} :y 3 :align-x :center
                  :dx 0.5 :offset-y -10 :text "half a band"}))

(kind/test-last
 [(fn [v]
    (let [panel (-> v pj/frames :panels first)
          at (fn [c] (first (pj/to-drawing panel c 4.5)))]
      (and
       ;; Half a band from the first category is the place halfway to
       ;; the second, so the shift draws what writing that place draws.
       (= (pj/plot v)
          (pj/plot (-> {:team ["red" "green" "blue"] :score [3 5 4]}
                       (pj/lay-bar :team :score {:color "#a6cee3"})
                       (pj/lay-text {:x 1.5 :y 3 :align-x :center
                                     :offset-y -10 :text "half a band"}))))
       ;; And that place is the midpoint of the two band centres.
       (< (abs (- (at 1.5) (/ (+ (at "red") (at "green")) 2.0))) 1e-9)
       ;; The shift is not a category, so the axis still carries three.
       (= ["red" "green" "blue"] (-> v pj/plan :panels first :x-domain)))))])

;; ## Jitter
;;
;; **Jitter** adds random offsets in drawing units to reduce
;; overplotting. Unlike `:position` and a `:dx` shift, jitter operates after
;; scaling (not in data space) and is deterministic -- seeded by a
;; hash of the group's color so repeated renders produce identical
;; output.
;;
;; On categorical x-axes, jitter is applied along the band axis only.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :species :sepal-length {:jitter true}))

(kind/test-last [(fn [v] (and (pj/pose? v)
                              (pos? (:points (pj/svg-summary v)))))])

;; ## Inference
;;
;; **Inference** is the automatic selection of a layer type
;; (mark + stat + position) when you bypass `pj/lay-*` and pass
;; columns to `pj/pose`. Plotje picks a layer type based on
;; column types: numerical x and y defaults to `:point`, categorical
;; x with numerical y to `:boxplot`, a single numerical column to
;; `:histogram`, and so on. Use `:x-type` / `:y-type` on a pose or
;; layer to override the detected type.

(-> (rdatasets/datasets-iris)
    (pj/pose :sepal-length :sepal-width))

(kind/test-last [(fn [v] (pos? (:points (pj/svg-summary v))))])

;; ## Plan
;;
;; A **plan** is the fully resolved description of the plot --
;; a plain Clojure map containing everything needed to render a
;; plot: data-space geometry, domains, tick info, legend, layout
;; dimensions. No membrane types, no datasets, no scale objects.
;;
;; Created with `pj/plan`. Numeric arrays (`:xs`, `:ys`, etc.) are
;; [dtype-next](https://github.com/cnuernber/dtype-next) buffers for
;; efficiency -- they print with their length and a small preview
;; rather than every element.

(def my-plan (pj/plan my-pose))

(kind/pprint my-plan)

(kind/test-last
 [(fn [plan]
    (and (vector? (:panels plan))
         (= 1 (count (:panels plan)))
         (= 600 (:width plan))
         (= 400 (:height plan))
         (some? (:legend plan))))])

;; ## Panel
;;
;; A **panel** is a single plotting area within a plan. It contains
;; x/y domains, scale specs, tick info, coordinate type, and layers.
;; A simple plot has one panel; `pj/facet` and `pj/facet-grid`
;; produce multiple.

(kind/pprint (first (:panels my-plan)))

(kind/test-last
 [(fn [p]
    (and (= :cartesian (:coord p))
         (= [4.12 8.08] (:x-domain p))
         (= 1 (count (:layers p)))))])

;; ## Plan Layer
;;
;; A **plan layer** is the resolved descriptor inside a plan panel:
;; resolved mark type, style, and groups of data-space geometry.
;; The user-level layer becomes the plan layer through `pj/plan`.

(kind/pprint (get-in my-plan [:panels 0 :layers 0]))

(kind/test-last
 [(fn [layer]
    (and (= :point (:mark layer))
         (= 3 (count (:groups layer)))
         (every? :xs (:groups layer))))])

;; ## Dataset
;;
;; A **dataset** is the tabular data backing a plot. Plotje uses
;; [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset)
;; datasets internally -- column-oriented, dtype-next-backed tables.
;; The most convenient way to create and manipulate them is the
;; [Tablecloth](https://scicloj.github.io/tablecloth/) API, which is
;; a Clojure-idiomatic wrapper over `tech.ml.dataset`.
;;
;; Raw input (a map of `{column-name [values]}`, a sequence of
;; row-maps, or a CSV/URL string) is coerced via
;; `tablecloth.api/dataset` at construction time. The dataset lives
;; on a pose under `:data`; layers can override with their own
;; `:data`, and inside a composite each pose in `:poses` can carry
;; its own `:data` too.

;; ## Pipeline
;;
;; The **pipeline** is the five-stage flow from user code to
;; rendered output: `pose -> draft -> plan -> membrane -> plot`.
;; A pose is what you compose; `pj/draft` flattens it into a draft
;; (a `LeafDraft` or `CompositeDraft` record); `pj/plan` resolves
;; geometry and layout; the membrane stage turns the plan into
;; drawable primitives; the plot is the terminal SVG hiccup or PNG
;; output. See the [Architecture](./plotje_book.architecture.html#the-single-step-transitions)
;; chapter for the per-stage details.

;; ## Sub-plot
;;
;; A **sub-plot** is the plan of one cell: one leaf of a composite
;; pose, or the panel of one column in a facet over columns. Where the
;; plan of a leaf pose carries `:panels` (one per category of a facet),
;; the plan of a composite pose, and of a leaf with a facet over
;; columns, carries `:sub-plots`, one per cell, each with its own
;; nested `:plan` map. The compositor reads `:sub-plots` and tiles
;; their rendered membranes into the final canvas.

;; ## Resolve Tree
;;
;; The **resolve tree** is the scope-merge walk that propagates a
;; root pose's `:mapping`, `:opts`, and root-attached `:layers`
;; downward into every descendant leaf during plan construction.
;; Lower (narrower) scopes override higher ones; root-attached
;; layers reach every applicable leaf in the tree.

;; ## Domain
;;
;; A **domain** is the extent of data values a scale reads -- along an
;; axis, or on an appearance aesthetic such as `:size`. It is the data side of
;; a scale; the visible side is its range.
;;
;; - Numerical: `[min max]`, where `min`/`max` are the raw data
;;   extent extended by 5% on each side so points do not sit on
;;   the panel edge. For `my-pose` above, sepal-length runs from
;;   `4.3` to `7.9` in the data; the x-domain becomes
;;   `[4.12 8.08]`.
;; - Temporal: same `[min max]` form, but the values are
;;   epoch-milliseconds (Plotje converts `LocalDate`,
;;   `LocalDateTime`, `Instant`, and `java.util.Date` automatically).
;;   Tick labels stay calendar-aware.
;; - Categorical: a vector of distinct values **in the order they
;;   first appear in the data**, not alphabetical (e.g., iris
;;   gives `["setosa" "versicolor" "virginica"]`).
;;
;; Each panel carries its own `:x-domain` and `:y-domain`. The panels
;; of a facet share one domain pair by default; `:share-scales` names
;; the axes they share, and a panel computes its own domain on an axis
;; left out, so under `{:share-scales #{}}` each panel computes both.

(let [p (first (:panels my-plan))]
  {:x-domain (:x-domain p)
   :y-domain (:y-domain p)})

(kind/test-last
 [(fn [m]
    (and (= [4.12 8.08] (:x-domain m))
         (= 2 (count (:y-domain m)))
         (number? (first (:y-domain m)))))])

;; ## Clip
;;
;; **Clipping** bounds a panel's marks to the panel, so geometry that
;; runs past the axis domain -- a line beyond a narrowed scale, a
;; point at the very edge -- is masked at the panel boundary rather
;; than painting outside it (and, in a multi-panel layout, into a
;; neighbour). It is the visual counterpart of the **Domain**: the
;; domain names the window, the clip enforces it. A narrowed domain
;; therefore acts as a view window -- the data is kept, only the view
;; is bounded.
;;
;; Data marks clip to the drawing area (the grey panel background);
;; rug ticks, which sit in the axis margin by design, clip to the
;; wider panel box -- the panel rectangle including that margin. The
;; clip is realized in the **Membrane** stage as a scissor (a
;; `membrane.ui/scissor-view`, a Membrane primitive that masks its
;; contents to a rectangle), so every backend that honours it clips
;; identically. `pj/svg-summary` reports the clip regions as `:clips`
;; -- one per panel for the data marks, plus one per panel that also
;; carries a margin mark such as rug.

(-> my-pose pj/plot pj/svg-summary :clips)

(kind/test-last [(fn [n] (= 1 n))])

;; ## Tick
;;
;; A **tick** is an axis mark with a label at a domain value. Ticks
;; are chosen at layout time to fit the axis length in drawing
;; units -- label widths, the spacing asked for, and calendar boundaries
;; (for temporal axes) all feed into the selection. Each panel in
;; the plan carries its own `:x-ticks` and `:y-ticks` maps with
;; parallel `:values` and `:labels` vectors.

(-> my-plan :panels first :x-ticks)

(kind/test-last
 [(fn [m]
    (and (vector? (:values m))
         (vector? (:labels m))
         (= (count (:values m)) (count (:labels m)))
         (false? (:categorical? m))))])

;; ## Scale
;;
;; A **scale** turns data into something visible. The axes turn data
;; values into places in drawing units; `:color`, `:size`, `:alpha`,
;; `:fill` and `:shape` turn them into a color, a radius, an opacity or
;; a symbol. A scale is built from a domain and an output range using
;; [wadogo](https://github.com/scicloj/wadogo), so it depends on the
;; whole column it is given and not on any one row.
;;
;; Whether a mapping is read through its scale is asked one mapping at
;; a time, with the `:scale` key:
;;
;; - `{:color {:column :hex :scale false}}` draws the column's values
;;   as they stand. They inform no domain and get no legend, since a
;;   legend explains a scale.
;; - `{:color {:value "Model A" :scale true}}` sends a written value
;;   through the scale as though it were a column of one distinct
;;   value, which is how a whole layer is labelled as a named series.
;;
;; Omitting `:scale` leaves the choice to the conventions: a column is
;; read through its scale, and a written value is drawn as it stands on
;; the appearance aesthetics and read through the scale on `:x` and
;; `:y`.
;;
;; The same key says *which* scale, as a type or a whole spec:
;; `{:size {:column :weight :scale :log}}` reads that one mapping
;; logarithmically, whatever `pj/scale` says on the pose around it.
;; `pj/scale` and a mapping's `:scale` take the same spec. On `:x` and
;; `:y` a panel carries one scale per axis, so two layers naming
;; different ones are refused.
;;
;; Two aesthetics have no scale at all -- `:text`, which draws a label
;; as it stands, and `:group`, which draws nothing of its own -- so
;; both report a `:scale` rather than accepting one.
;;
;; | Type | Use |
;; |:-----|:----|
;; | `:linear` | Numerical data (default) |
;; | `:log` | Orders-of-magnitude data |
;; | `:categorical` | Distinct categories (band scale) |
;;
;; Scales are created at render time, not stored in the plan. The plan
;; stores scale *specs*: `:type` and `:domain`, which every scale has,
;; and the keys each aesthetic reads beside them. `pj/aesthetic-scales`
;; publishes which those are, aesthetic by aesthetic, and
;; [Scales](./plotje_book.scales.html#what-each-aesthetics-scale-takes)
;; prints the table.
;;
;; **Temporal columns** (`LocalDate`, `LocalDateTime`, `Instant`,
;; `java.util.Date`) are detected automatically and converted to
;; epoch-milliseconds -- one number per value -- before any scale is
;; built. The axis is then an ordinary `:linear` scale over those
;; numbers, and the domain the plan carries is a pair of
;; epoch-millisecond numbers. That is all "treated as numerical"
;; means: it describes the scaling, not the display. The tick labels
;; are written as dates or times, in a format that follows the span
;; the axis covers. See
;; [Inference Rules](./plotje_book.inference_rules.html#temporal-columns)
;; for the conversion and the tick formats, worked through.

;; ## Coord
;;
;; A **coord** (coordinate system) defines how data-space maps to
;; drawing units.
;;
;; | Type | Behavior |
;; |:-----|:---------|
;; | `:cartesian` | Standard: x rightward, y upward |
;; | `:flip` | Swap x and y axes |
;; | `:polar` | Radial: x as angle, y as radius |
;; | `:fixed` | Equal aspect ratio: 1 data unit = 1 data unit |

(-> (rdatasets/datasets-iris)
    (pj/lay-bar :species)
    (pj/coord :flip))

(kind/test-last
 [(fn [v]
    (and (= 3 (:polygons (pj/svg-summary v)))
         (= :flip
            (-> (rdatasets/datasets-iris)
                (pj/lay-bar :species)
                (pj/coord :flip)
                pj/plan
                (get-in [:panels 0 :coord])))))])

;; ## Facet
;;
;; A **facet** draws one pose as several panels. A facet over the
;; categories of a column draws a panel per category, each showing the
;; rows of that category. A facet over columns, written as a map such
;; as `{:y [:hwy :cty]}`, draws a panel per column, each showing every
;; row.
;;
;; - `pj/facet` given a column creates a panel per category of that
;;   column; given a map of columns, a panel per column or per pair of
;;   columns
;; - `pj/facet-grid` creates a grid from two categorical columns, the
;;   categories of the first across and those of the second down
;;
;; By default all panels share the same x and y domains, which cover
;; what the panels draw. `:share-scales` names the axes the panels
;; share, in `pj/options` or in the map of plot options that `pj/facet`
;; takes as its last argument: `{:share-scales #{:x}}` lets each panel use
;; its own `:y` range, and `{:share-scales #{}}` its own range on both
;; axes.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species))

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)
          n-panels (count
                    (:panels (pj/plan
                              (-> (rdatasets/datasets-iris)
                                  (pj/lay-point :sepal-length :sepal-width)
                                  (pj/facet :species)))))]
      (and (= 3 (:panels s))
           (= 3 n-panels)
           ;; A facet over columns: every row in each panel.
           (= {:panels 2 :points 300}
              (select-keys (pj/svg-summary
                            (-> (rdatasets/datasets-iris)
                                (pj/pose {:x :sepal-length})
                                pj/lay-point
                                (pj/facet {:y [:sepal-width :petal-width]})))
                           [:panels :points])))))])

;; ## Overlay
;;
;; To **overlay** is to draw a layer on a panel that another layer
;; already draws on. The layers of a pose overlay one another on its
;; panel, each keeping its own columns, and each axis covers every
;; column drawn on it. Separate panels come from `pj/facet` over the
;; columns or from `pj/arrange`.

(-> {:cohort [:a :b :c] :growth [12 19 15] :tax [3 5 4]}
    (pj/lay-bar :growth :cohort {:color "#377eb8"})
    (pj/lay-bar :tax :cohort {:bar-width 0.4 :color "#e6550d"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 1 (:panels s))
                 (= 6 (:polygons s)))))])

;; ## Marginal
;;
;; A **marginal** is a distribution of one of a panel's columns, drawn
;; in a thin panel against one edge of that panel and sharing its axis
;; for the column: the `:x` column above, the `:y` column to the right.
;; `pj/marginal` builds one -- a composite of two cells with
;; `:share-scales` pinning the shared axis, the marginal's duplicate
;; ticks and axis title dropped, and the two drawing areas aligned so a
;; value sits at the same place in each.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/marginal :top))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s))
                 (= 150 (:points s)))))])

;; The same scatter with its `:y` column described instead. The strip
;; is drawn under `pj/coord :flip`, so the distribution runs up the
;; panel and its baseline stands against the scatter.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/marginal :right))

(kind/test-last
 [(fn [v] (let [panels (mapv #(-> % :plan :panels first)
                             (:sub-plots (pj/plan v)))]
            (and (= 2 (:panels (pj/svg-summary v)))
                 (= (:y-domain (first panels))
                    (:y-domain (second panels))))))])

;; ## Annotation
;;
;; An **annotation** is a mark layered on a plot to explain it rather
;; than to show data: a reference line, a shaded region, a note, a
;; leader line, a caption. Where each one sits is given as a written
;; value or measured on the panel, not read from a column for every
;; row.
;;
;; Two ways to place one, and every one of the constructors below
;; accepts both:
;;
;; - **In data space**, so the annotation moves with the axis, and the
;;   axis reaches it: a rule written outside everything the data covers
;;   widens the axis rather than being clipped away.
;; - **On the panel**, with `:in :drawing-area`, so the annotation is
;;   placed in drawing units from the corner of the panel background
;;   and the axis domains are unaffected. See **Drawing Area**.
;;
;; Annotations are regular layers, so they attach under the same
;; three cases as any `lay-*`: bare call sits on the pose, and a call
;; naming columns joins the leaf's panel -- on a composite, the most
;; recent matching leaf, or a new leaf where none matches.
;;
;; | Constructor | What |
;; |:------------|:-----|
;; | `pj/lay-rule-v` | Vertical line at x = x-intercept |
;; | `pj/lay-rule-h` | Horizontal line at y = y-intercept |
;; | `pj/lay-band-v` | Vertical shaded region from x = x-min to x = x-max |
;; | `pj/lay-band-h` | Horizontal shaded region from y = y-min to y = y-max |
;; | `pj/lay-text`, `pj/lay-label` | A note, at a value or on the panel |
;;
;; Each of the four rule and band constructors names where it draws in
;; the layer's `:mapping` slot (`:y-intercept` or `:x-intercept` for
;; rules; `:y-min`/`:y-max` or `:x-min`/`:x-max` for bands), as a
;; written value rather than a column reference, and draws at exactly
;; one place.
;; `{:value 1.5}` says the same thing at more length; a `{:column ...}`
;; there is reported, since these read no column.
;; Column-mapped intercepts, producing one mark per row like ggplot2's
;; `geom_hline(aes(yintercept = ...))`, are planned but not yet
;; implemented.
;;
;; Annotation names a use rather than a kind of thing. Every one of
;; those constructors builds an ordinary layer, and a panel carries them
;; among its `:layers` in the order they were written -- so a rule
;; written before a scatter is drawn under its points, and one written
;; after is drawn over them. See the
;; [Extensibility](./plotje_book.extensibility.html#a-mark-placed-at-a-written-value) chapter.

(def annotated
  (-> (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width)
      (pj/lay-rule-h {:y-intercept 3.0})))

annotated

(kind/pprint (nth (:layers annotated) 1))

(kind/test-last
 [(fn [layer]
    (and (= :rule-h (:layer-type layer))
         (= 3.0 (get-in layer [:mapping :y-intercept]))))])

;; ## Legend
;;
;; A **legend** is the key drawn beside the plot that explains a
;; scale: which color stands for which category, which radius for
;; which number. It is generated automatically, and what it explains
;; is the scale rather than the mapping -- so a legend appears exactly
;; where a scale was applied.
;;
;; A column read through its scale gets a legend, and a written value
;; read through its scale gets a one-entry legend naming the value. A
;; mapping given `{:scale false}` gets no legend, since its values
;; were drawn as they stand and nothing was decided that a reader
;; would need explaining. Neither does an aesthetic the mark cannot
;; vary from row to row: `:size` on `pj/lay-line` draws one width for
;; the whole layer, so a legend pairing values with radii would
;; explain an encoding the panel does not carry.
;;
;; A legend appears in the plan under the key named for its aesthetic
;; -- `:legend` for color, holding entries with labels and colors, and
;; `:size-legend`, `:alpha-legend`, `:shape-legend` for the others. One
;; column driving both color and shape produces a single merged legend
;; under `:legend`, whose entries carry a `:shape` too. Where it sits
;; is controlled via `{:legend-position :bottom}` in options.

(kind/pprint (:legend my-plan))

(kind/test-last
 [(fn [leg]
    (and (map? leg)
         (= :species (:title leg))
         (= 3 (count (:entries leg)))
         (= ["setosa" "versicolor" "virginica"]
            (mapv :label (:entries leg)))))])

;; ## Theme
;;
;; A **theme** controls the visual appearance of non-data elements.
;; It is a nested map under `:theme` with three keys:
;;
;; | Key | Controls |
;; |:----|:---------|
;; | `:bg` | Panel background color |
;; | `:grid` | Gridline color |
;; | `:font-size` | Font size of the tick labels, in drawing units |
;;
;; Passed as `{:theme {...}}` via `pj/options`, `pj/with-config`, or
;; `pj/set-config!`. Other visual settings (margins, legend width, tick
;; spacing) are top-level configuration keys, not theme entries --
;; see `pj/config-key-docs`.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:theme {:bg "#2d2d2d" :grid "#444444" :font-size 10}}))

(kind/test-last
 [(fn [v]
    (let [small (pj/options v {:theme {:font-size 8}})
          sizes (frequencies
                 (keep #(get-in % [1 :font-size])
                       (filter #(and (vector? %) (= :text (first %)))
                               (tree-seq vector? seq (pj/plot small)))))
          panel (first (:panels (pj/plan small)))
          n-tick-labels (+ (count (:labels (:x-ticks panel)))
                           (count (:labels (:y-ticks panel))))]
      (and (= 150 (:points (pj/svg-summary v)))
           ;; `:font-size` sizes the tick labels and no other text.
           (= n-tick-labels (get sizes 8)))))])

;; ## Membrane
;;
;; A **membrane** is a `PlotjeMembrane` -- a record carrying a tree
;; of layout and drawing primitives (`Translate`, `WithColor`,
;; `RoundedRectangle`, `Label`, etc.) sized to a complete plot
;; canvas. The record itself implements the
;; [Membrane](https://github.com/phronmophobic/membrane) library's
;; UI protocols (`IOrigin`, `IBounds`, `IChildren`), so it composes
;; with other Membrane elements and any Membrane consumer can use
;; it without special handling.
;;
;; Plotje produces a membrane via `pj/plan->membrane` (single-step
;; transition from a plan) or `pj/membrane` (composition shortcut
;; from a pose). The record carries `:width` and `:height` as
;; fields (read via `(membrane.ui/width m)`/`(membrane.ui/height m)`),
;; and the title as `:plotje/title`. Direct renderers (e.g., Plotly)
;; skip the membrane entirely. The
;; [Membranes](./plotje_book.membranes.html) chapter walks the stage
;; in depth.

(def my-membrane (pj/plan->membrane my-plan))

;; A complete membrane is large -- one drawable per data point on
;; top of axes, gridlines, ticks, and labels. The record's children
;; (the underlying drawable tree) for `my-plan`:

(kind/pprint my-membrane)

(kind/test-last
 [(fn [m]
    (let [walk-text (fn walk [d]
                      (cond
                        (string? (:text d)) (:text d)
                        (:drawable d) (walk (:drawable d))
                        (:drawables d) (some walk (:drawables d))))
          drawables (membrane.ui/children m)
          texts (mapv walk-text drawables)]
      (and (pj/membrane? m)
           (= 9 (count drawables))
           ;; The first four top-level entries carry the title,
           ;; axis labels, and legend title.
           (= ["Iris" "sepal width" "sepal length" "species"]
              (vec (take 4 texts))))))])

;; ## Plot
;;
;; A **plot** is the final rendered output -- the result of rendering
;; a plan to a specific format. For SVG, the plot is hiccup markup
;; wrapped in `kind/hiccup`.
;;
;; Created by `pj/plot` or by auto-rendering a pose.

(def my-plot (pj/plan->plot my-plan :svg {}))

;; The plot is hiccup -- a vector starting with `:svg` followed by
;; an attribute map and the plot's drawable elements. Wrapped in
;; `kind/hiccup`, it renders as the same picture we saw at the
;; top of this chapter:

(kind/hiccup my-plot)

(kind/test-last
 [(fn [v]
    (let [s (pj/svg-summary v)]
      (and (= :svg (first my-plot))
           (= 150 (:points s))
           (= 600.0 (double (:width s))))))])

;; ## Palette
;;
;; A **palette** is an ordered set of colors used for categorical
;; aesthetics. When `:color` maps to a categorical column, colors
;; are assigned from the active palette in order.
;;
;; Plotje uses [clojure2d](https://github.com/Clojure2D/clojure2d)
;; for palettes. Set with `:values` in a `:color` scale spec, or with
;; the `:color-values` option one scope further out:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/scale :color {:values :set2}))

(kind/test-last
 [(fn [v] (= 150 (:points (pj/svg-summary v))))])

;; clojure2d provides thousands of named palettes -- the count below
;; is the size of the catalogue:

(count (c2d/find-palette #".*"))

(kind/test-last [(fn [n] (<= 5000 n))])

;; ## Gradient
;;
;; A **gradient** (or color scale) maps a continuous numeric range
;; to a smooth color ramp. Used when `:color` maps to a numerical
;; column.
;;
;; Common gradients: `:viridis`, `:inferno`, `:plasma`,
;; `:magma`. Diverging gradients center on a `:midpoint` value.
;; Set with `:range` in a `:color` scale spec, or with the
;; `:color-range` option one scope further out:

(-> {:x (range 50) :y (range 50) :c (range 50)}
    (pj/lay-point :x :y {:color :c})
    (pj/scale :color {:range :inferno}))

(kind/test-last
 [(fn [v]
    (and (= 50 (:points (pj/svg-summary v)))
         (= :inferno
            (:color-range
             (:legend (pj/plan
                       (-> {:x (range 50) :y (range 50) :c (range 50)}
                           (pj/lay-point :x :y {:color :c})
                           (pj/scale :color {:range :inferno}))))))))])

;; ## Configuration
;;
;; **Configuration** controls rendering behavior -- dimensions, theme,
;; palette, color scale, margins, and more. It is one of three option
;; kinds of value a plot is built from -- the others are
;; [plot options](#plot-options) and [mappings](#mapping).
;; Configuration follows a
;; precedence chain:
;;
;; plot options > `pj/with-config` > `pj/set-config!` > `plotje.edn` > library defaults
;;
;; `plotje.edn` is an optional file in your project root that provides
;; project-level defaults (e.g., a consistent palette or theme across all plots).
;;
;; See the Configuration chapter for details. The active configuration
;; is itself a Clojure map -- `pj/config` returns a snapshot:

(select-keys (pj/config) [:width :height :theme :color-values :color-range])

(kind/test-last
 [(fn [m]
    (and (number? (:width m))
         (number? (:height m))
         (map? (:theme m))))])

;; ## Plot Options
;;
;; **Plot options** are per-plot settings passed to `pj/options`,
;; `pj/plan`, or `pj/plot`. They include text content (title,
;; subtitle, caption, axis labels) and a nested `:config` override.
;; Unlike configuration keys, plot options are inherently per-plot --
;; a title does not make sense as a global default.
;;
;; See `pj/plot-option-docs` for the full list, or the
;; [Configuration](./plotje_book.configuration.html#using-plot-options) chapter for usage examples.

(sort (keys pj/plot-option-docs))

(kind/test-last
 [(fn [ks]
    (and (= 14 (count ks))
         (some #{:title :subtitle :caption :x-label :y-label} ks)))])

;; ## Layer Settings
;;
;; A **layer setting** is a key of a layer's [mapping](#mapping) that
;; is not an aesthetic. A layer setting says how the layer is computed
;; or drawn, where an aesthetic says what a column or a value becomes.
;; The layer settings are the parts of the layer type (`:mark`,
;; `:stat`, `:position`), the layer's own `:data`, the space its `:x`
;; and `:y` are in (`:in`), and the parameters of one layer type
;; (`:bins`, `:bandwidth`, `:confidence-band`, `:normalize`, and
;; others).
;;
;; A layer setting is written in the mapping of a layer function
;; (`pj/lay-point`, `pj/lay-histogram`, and the rest), beside the
;; layer's aesthetics. Some keys are accepted by every layer type, and
;; each layer type may accept more. The
;; [Layer Types](./plotje_book.layer_types.html#layer-type-specific-keys) chapter lists the
;; keys each layer type accepts. `pj/layer-mapping-docs` describes every
;; key of a layer's mapping, aesthetics and layer settings alike, and
;; `pj/layer-type-lookup` shows one layer type.

(sort (keys pj/layer-mapping-docs))

(kind/test-last
 [(fn [ks]
    (and (pos? (count ks))
         (some #{:color :size :alpha :group :position} ks)))])

;; ## Tooltip and Brush
;;
;; A **tooltip** shows data values on hover. A **brush** enables
;; click-and-drag selection that highlights a rectangular region.
;; Both are JavaScript-based interactions added to the SVG output.
;;
;; Enabled via `{:tooltip true}` and `{:brush true}` in options:

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:tooltip true :brush true}))

(kind/test-last
 [(fn [pose]
    (let [s (str (pj/plot pose))]
      (and (re-find #"data-tooltip" s)
           (re-find #"nsk-brush-sel" s))))])

;; ## Summary Table
;;
;; | Term | What | Key functions |
;; |:-----|:-----|:-------------|
;; | Pose | Composable value: data + mapping + layers (+ inner poses) | All `pj/` functions return poses |
;; | Leaf pose | Pose with no sub-poses; it draws one panel, which a facet multiplies | `pj/pose`, `pj/lay-*` |
;; | Composite pose | Pose containing other poses (in `:poses`) and a layout | `pj/arrange`, `pj/marginal` |
;; | Mapping | Column-to-aesthetic association on a pose or layer | `pj/pose` mapping, `pj/lay-*` mapping |
;; | Layer | Layer type attached to a pose, optionally with scoped mappings | `pj/lay-*` |
;; | Dataset | Tabular data backing a plot -- a `tech.ml.dataset`, conveniently built and manipulated via the Tablecloth API | `:data` slot, `pj/with-data` |
;; | Pipeline | Five-stage flow `pose -> draft -> plan -> membrane -> plot` | Architecture chapter |
;; | Sub-plot | The plan of one cell of a composite, or of one column of a facet over columns | `:sub-plots` in plan |
;; | Resolve tree | Scope-merge walk: root mappings propagate to every leaf | Internal to `pj/plan` |
;; | Draft | `LeafDraft` or `CompositeDraft` record carrying merged layer maps and pose-level options | `pj/draft`, automatic during `pj/plan` |
;; | Draft layer | One element of a draft: layer type + merged mappings + data | Element of `pj/draft` output |
;; | Layer type | Mark + stat + position bundle | `pj/layer-type-lookup`, `pj/lay-*` |
;; | Mark | Visual shape: point, line, bar, area, ... | Key in layer-type map |
;; | Stat | Data transform: identity, bin, count, linear-model, density, ... | Key in layer-type map |
;; | Position | How overlapping marks are placed: identity, dodge, stack, fill | Key in layer-type map |
;; | Inference | Auto-choosing mark/stat from column types | When `pj/lay-*` is omitted |
;; | Aesthetic | Mark property bindable to a column, each carrying a role | Key in mapping or layer |
;; | Role | What a distinction given to an aesthetic is put to work as: positional, appearance, grouping, panel | `pj/aesthetic-roles` |
;; | Group | Subset of data rendered together | From `:color` or `:group` |
;; | Plan | Fully resolved plot description | `pj/plan` |
;; | Panel | One plotting area (domain, ticks, layers) | One or more per plan |
;; | Plan layer | Resolved geometry + style for one mark | Inside plan panels |
;; | Domain | Data range on an axis | Part of panel |
;; | Place | Where a mark is drawn, in data space -- a value on a numerical axis, a count among the categories on a categorical one | Any number written for an axis; `pj/to-drawing` |
;; | Tick | Axis mark with label at a domain value | Part of panel |
;; | Data space | Values in their original units -- what mappings, stats, domains, and ticks hold | Every stage up to the plan |
;; | Drawing space | Positions in drawing units on the output canvas | Membrane and plot stages |
;; | Canvas | The whole output image, and the origin of drawing space | `:width` / `:height`; `pj/frames` |
;; | Panel box | One panel including its axis margin | `pj/frames` |
;; | Drawing area | The panel background inside that margin, where data marks clip | `pj/frames`; `:in :drawing-area` |
;; | Scale | Data-to-drawing-units mapping (linear, log, categorical) | `pj/scale`, or `:scale` in a mapping |
;; | Coord | Coordinate system (cartesian, flip, polar, fixed) | `pj/coord` |
;; | Facet | Split one pose into panels, by the categories of a column or by columns | `pj/facet`, `pj/facet-grid` |
;; | Arrange | Compose poses or columns into a grid of cells | `pj/arrange` |
;; | Overlay | Draw a layer on a panel another layer already draws on, whatever columns it names | every layer of a pose |
;; | Marginal | A distribution of a panel's `:x` or `:y` column, drawn above it or beside it on a shared axis | `pj/marginal` |
;; | Share scales | Make the panels of a facet, or the sibling poses of a composite, share data ranges across named axes | `:share-scales` in `:opts` |
;; | Annotation | Reference marks (rules, bands); where they draw is a written value in `:mapping` today, data-driven planned | `pj/lay-rule-*`, `pj/lay-band-*` |
;; | Legend | Color/size/alpha key from aesthetic mappings | Automatic in plan |
;; | Plot options | Title, subtitle, caption, labels, dimensions | `pj/options` |
;; | Layer settings | The keys of a layer's mapping that are not aesthetics: `:stat`, `:position`, `:bins`, `:data` | `pj/lay-*` mapping |
;; | Theme | Visual styling: background, grid, fonts | `:theme` in `pj/options` |
;; | Palette | Ordered color set for categorical aesthetics | `:values` in a `:color` scale spec |
;; | Gradient | Continuous color ramp for numerical mappings | `:range` in a `:color` scale spec |
;; | Configuration | Global rendering defaults | `pj/config`, `pj/set-config!`, `pj/with-config` |
;; | Membrane | `PlotjeMembrane` record -- a Membrane UI component carrying the drawable tree, plan-derived dimensions, and `:plotje/title` | `pj/membrane`, `pj/plan->membrane` |
;; | Plot | Final output (SVG hiccup) | `pj/plot`, `pj/save` |
;; | Tooltip / Brush | JavaScript hover and selection interactions | `{:tooltip true}` in options |
