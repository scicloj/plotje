;; # Pose Rules
;;
;; Each rule below states one behaviour and shows it on a rendered
;; pose, with the printed structure where the shape is the point.
;;
;; The rules are organized into seven sections (Construction, Layer
;; Placement, Leaf Identity, Scope, Options, Assembly, Layout) and
;; cover the call shapes of `pj/pose`, `pj/lay-*`, `pj/arrange`,
;; `pj/options`, `pj/scale`, `pj/coord`, `pj/facet`, and
;; `pj/facet-matrix`.
;;
;; Read [Poses](./plotje_book.pose_model.html) first. On a first read
;; through the book you can skim or skip this chapter, and return when
;; you want the precise rule behind a behavior.

(ns plotje-book.pose-rules
  (:require
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset operations
   [tablecloth.api :as tc]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Setup

(def iris (rdatasets/datasets-iris))

;; A helper to inspect pose structure without `:data` -- the dataset
;; is heavy and not what we are checking. We strip `:data` from the
;; pose and every nested sub-pose and layer.

(defn strip-data [pose]
  (cond-> (dissoc pose :data)
    (:layers pose) (update :layers (partial mapv #(dissoc % :data)))
    (:poses pose) (update :poses (partial mapv strip-data))))

(defn pose-summary
  "Print pose structure without :data (for readability)."
  [pose]
  (kind/pprint (strip-data pose)))

;; The cells of a composite -- its leaves, in reading order. `pj/arrange`
;; puts its cells in rows, so they are not its `:poses` directly.

(defn cells [pose]
  (filterv #(not (contains? % :poses)) (rest (tree-seq :poses :poses pose))))

;; ## Overview
;;
;; A **pose** is a plain Clojure map with a documented set of keys:
;;
;; | Key | On | Purpose |
;; |:----|:---|:--------|
;; | `:data` | leaf or any ancestor | dataset (Tablecloth) |
;; | `:mapping` | pose or layer | column-to-aesthetic bindings |
;; | `:layers` | pose | per-scope layers |
;; | `:poses` | composite only | sub-poses |
;; | `:layout` | composite | direction + weights |
;; | `:facet` | leaf | a facet over columns, expanded into cells when the pose is drawn (Rule O3) |
;; | `:opts` | root | plot-level options (incl. composite-level keys like `:share-scales`) |
;; | `:share-scales` | composite | the axes the cells share; `pj/arrange` writes it under `:opts`, and `pj/marginal` writes it on the composite itself |
;; | `:where` | any pose | a map from a column to a value or a set of values; the pose draws only the rows that match |
;;
;; A **leaf pose** has `:data`, `:mapping`, `:layers`; no `:poses`.
;; A **composite pose** has `:poses`, and a sub-pose may itself be a
;; composite: `pj/arrange` wraps its cells in a row per line of the
;; grid, so the composite it returns holds rows and each row holds
;; cells. A composite the caller supplies is accepted the same way:
;; `pj/arrange` given one among its inputs nests it (Rule C8),
;; `pj/facet` called on one divides every cell of it (Rule O3), and a
;; nested map written out and handed to `pj/pose` is read as written.
;; So a grid inside a grid can be written directly. A **layer**
;; is a map with `:layer-type` and an optional `:mapping`, plus sibling
;; keys `:stat`, `:position`, `:mark` when the user provides them.

;; ---
;; ## Construction
;;
;; How poses and composites come into existence. Nine rules
;; covering every `pj/pose` call shape plus `pj/arrange`.

;; ### Rule C1: `pj/pose` on raw data creates a leaf
;;
;; Called with a dataset as first argument, `pj/pose` returns a
;; leaf pose. The arity determines what's in `:mapping`: a keyword
;; is `:x`; two keywords are `:x` and `:y`; a final map is the
;; mapping and contributes aesthetic keys.

(-> iris
    (pj/pose :sepal-length :sepal-width))

(kind/test-last [(fn [v] (= 150 (:points (pj/svg-summary v))))])

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:x :sepal-length :y :sepal-width} (:mapping pose))
         (= [] (:layers pose))
         (not (contains? pose :poses))))])

;; With only `:color` in the mapping, the pose is a leaf that names no
;; `:x` and no `:y`. On data of one to three columns, the columns are
;; read by default when the pose is drawn (Rule LI1). Iris has more
;; columns, so a layer added to this pose has to name its columns, or a
;; later `pj/pose` call has to name them.

(-> iris
    (pj/pose {:color :species})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (not (contains? pose :poses))))])

;; ### Rule C2: `pj/pose` on a leaf merges into its mapping
;;
;; `pj/pose` called on a leaf merges the new mapping into the leaf's
;; own mapping, whatever the leaf or its layers already name. The leaf
;; remains a leaf, and no composite is created.

(-> iris
    pj/pose
    (pj/pose :sepal-length :sepal-width))

(kind/test-last
 [(fn [pose]
    (and (= {:x :sepal-length :y :sepal-width} (:mapping pose))
         (not (contains? pose :poses))))])

;; And an aesthetic-on-aesthetic extension merges with later-wins:

(-> iris
    (pj/pose {:color :species})
    (pj/pose :sepal-length :sepal-width))

(kind/test-last
 [(fn [pose]
    (= {:x :sepal-length :y :sepal-width :color :species} (:mapping pose)))])

;; **Property P-C2 -- construction commutativity.** A mapping built
;; over several `pj/pose` calls is equal to the same mapping written in
;; one call.

(= (-> iris
       pj/pose
       (pj/pose {:color :species})
       (pj/pose :sepal-length :sepal-width))
   (pj/pose iris :sepal-length :sepal-width {:color :species}))

(kind/test-last [true?])

;; ### Rule C3: `pj/pose` naming other columns replaces the leaf's `:x` and `:y`
;;
;; `pj/pose` writes one mapping and does not add a panel. Called with
;; an `:x` or `:y` other than the one the leaf's mapping has, it
;; replaces that column, and the layers that name no columns of their own read
;; the new columns. So a pose returned by a function can be pointed at
;; other columns:

(defn trend-plot [data]
  (-> data
      (pj/pose :sepal-length :sepal-width)
      pj/lay-point
      pj/lay-smooth))

(-> (trend-plot iris)
    (pj/pose :petal-length :petal-width))

(-> (trend-plot iris)
    (pj/pose :petal-length :petal-width)
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (not (contains? pose :poses))
         (= {:x :petal-length :y :petal-width} (:mapping pose))
         (= [{:layer-type :point} {:layer-type :smooth}] (:layers pose))))])

;; A layer that names its own columns keeps them. The pose's new `:x`
;; and `:y` are then drawn by no layer, and drawing the pose prints a
;; warning naming them:

(-> iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width))

(kind/test-last
 [(fn [pose]
    (let [printed (with-out-str (pj/plan pose))]
      (and (= {:x :petal-length :y :petal-width} (:mapping pose))
           (= [{:x :sepal-length :y :sepal-width}] (mapv :mapping (:layers pose)))
           (re-find #"a pose maps :x to :petal-length, and every layer drawn on it names its own :x" printed)
           (re-find #"a pose maps :y to :petal-width" printed))))])

;; For panels side by side, `pj/arrange` takes the pairs of columns:

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    pj/lay-point)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; **Property P-C3 -- writing the same columns again changes
;; nothing.** A `pj/pose` call naming the leaf's own `:x` and `:y`
;; returns the same pose.

(= (trend-plot iris)
   (pj/pose (trend-plot iris) :sepal-length :sepal-width))

(kind/test-last [true?])

;; ### Rule C4: `pj/pose` with no `:x` or `:y` merges into the leaf's mapping
;;
;; A `pj/pose` call naming no `:x` and no `:y`, on a leaf whose mapping
;; names them, merges the aesthetic into the leaf's `:mapping`. The
;; leaf stays a leaf.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose {:color :species}))

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose {:color :species})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (not (contains? pose :poses))
         (= {:x :sepal-length :y :sepal-width :color :species}
            (:mapping pose))))])

;; **Property P-C4 -- the aesthetic may come in a second call.** Adding
;; the aesthetic in a second call equals writing it in the first.

(= (-> iris
       (pj/pose :sepal-length :sepal-width)
       (pj/pose {:color :species}))
   (pj/pose iris :sepal-length :sepal-width {:color :species}))

(kind/test-last [true?])

;; ### Rule C5: `pj/pose` leaves the layers where they are
;;
;; `pj/pose` changes only the mapping. The layers stay on the pose
;; they were added to, and a layer that names no columns of its own
;; reads the merged mapping.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/pose {:color :species}))

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/pose {:color :species})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= [{:layer-type :point}] (:layers pose))
         (= {:x :sepal-length :y :sepal-width :color :species}
            (:mapping pose))))])

;; ### Rule C6: `pj/pose` on a composite merges into the root mapping
;;
;; On a composite, `pj/pose` merges into the root `:mapping`, which
;; reaches every cell that does not map the same aesthetic itself. So
;; an aesthetic that no cell maps itself reaches every cell:

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/pose {:color :species})
    pj/lay-point)

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/pose {:color :species})
    pj/lay-point
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (= [{:x :sepal-length :y :sepal-width}
             {:x :petal-length :y :petal-width}]
            (mapv :mapping (cells pose)))))])

;; An `:x` or a `:y` written on a composite whose cells each name their
;; own `:x` and `:y` reaches none of the cells. The cells keep their
;; columns, and drawing the pose prints a warning for each axis, saying
;; that the column written on the composite is not drawn:

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/pose :sepal-length :petal-length)
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (= 2 (count (re-seq #"not drawn" (with-out-str (pj/plan pose))))))])

;; ### Rule C7: `pj/pose` with nothing new has no effect
;;
;; `(pj/pose pose)` and `(pj/pose pose {})`, where `pose` is a leaf or
;; a composite, return `pose` unchanged, and writing a mapping twice
;; gives the pose that writing it once gives. So `pj/pose` is safe to
;; use as a pipeline step that does nothing.

;; A leaf pose. First the rendered plot, then its structure:

(def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width)))

leaf-pose

(pose-summary leaf-pose)

;; Wrapping it with `pj/pose` returns the same value:

(= leaf-pose
   (pj/pose leaf-pose)
   (pj/pose leaf-pose {}))

(kind/test-last [true?])

(= (pj/pose leaf-pose {:color :species})
   (pj/pose (pj/pose leaf-pose {:color :species}) {:color :species}))

(kind/test-last [true?])

;; A composite pose. The rendered plot, then its structure:

(def composite-pose
  (-> iris
      (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
      pj/lay-point))

composite-pose

(pose-summary composite-pose)

;; And again, `pj/pose` has no effect:

(= composite-pose
   (pj/pose composite-pose)
   (pj/pose composite-pose {}))

(kind/test-last [true?])

;; ### Rule C8: `pj/arrange` composes poses into a composite
;;
;; `pj/arrange` returns a composite. It takes a vector of cells, or
;; data or a pose first and then the cells. A cell is a pose, a
;; composite, a column (a panel of that column), a pair of columns (a
;; panel of the two columns), a mapping, or `nil` for a cell left
;; empty. The cells are placed in rows: the composite's `:poses` are
;; the rows, and each row's `:poses` are the cells of that row.

(pj/arrange
 [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
  (-> iris (pj/pose :petal-length :petal-width) pj/lay-point)])

(kind/test-last
 [(fn [pose]
    (and (contains? pose :poses)
         (= :vertical (get-in pose [:layout :direction]))
         ;; arrange wraps in rows-of-columns: outer :poses is
         ;; rows, each row's :poses is cells
         (= 1 (count (:poses pose)))
         (= 2 (count (:poses (first (:poses pose)))))))])

;; A cell may itself be a composite, and nests rather than being
;; flattened -- which is the shape `pj/arrange` builds for itself, one
;; row per line of the grid:

(-> (pj/arrange [(pj/arrange [(pj/pose iris :sepal-length :sepal-width)
                              (pj/pose iris :petal-length :petal-width)])
                 (pj/pose iris :sepal-length :petal-length)])
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (and (= 3 (:panels (pj/svg-summary pose)))
         ;; The supplied composite is still a composite where it
         ;; landed: root -> row -> the cell it was given as.
         (seq (:poses (first (:poses (first (:poses pose))))))))])

;; `pj/arrange` writes the options it is given into the composite's
;; `:opts`: `:title`, `:width`, `:height`, `:share-scales`, and any plot
;; option `pj/options` takes. `:cols` is not stored: it decides how many
;; cells each row holds.

(pj/arrange
 [(pj/pose iris :sepal-length :sepal-width)
  (pj/pose iris :petal-length :sepal-width)]
 {:title "Arranged"
  :share-scales #{:y}})

(kind/test-last
 [(fn [pose]
    (let [one-per-row (pj/arrange (vec (mapcat :poses (:poses pose)))
                                  {:cols 1 :x-label "X"})]
      (and (= "Arranged" (get-in pose [:opts :title]))
           (= #{:y} (get-in pose [:opts :share-scales]))
           ;; A plot option is written into :opts too.
           (= "X" (get-in one-per-row [:opts :x-label]))
           ;; :cols is read and not stored: one cell per row.
           (not (contains? (:opts one-per-row) :cols))
           (= [1 1] (mapv #(count (:poses %)) (:poses one-per-row))))))])

;; ### Rule C9: `pj/facet-matrix` writes a facet map, and a mapping on the pose
;;
;; `pj/facet-matrix` given columns writes the facet map that crosses
;; them, `{:col {:x cols} :row {:y cols}}`, on a leaf pose -- no `:poses` --
;; which the facet splits into a panel for every pair. Given a mapping
;; as well, it writes that mapping on the pose, so it reaches every
;; panel.

(-> iris
    (pj/facet-matrix [:sepal-length :sepal-width]
                     {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (= {:col {:x [:sepal-length :sepal-width]}
             :row {:y [:sepal-length :sepal-width]}}
            (:facet pose))
         (nil? (:poses pose))
         (= 4 (:panels (pj/svg-summary pose)))))])

;; **Property P-C9 -- the mapping can come first or last.** A mapping
;; written with `pj/pose` before `pj/facet-matrix` gives an equal pose.

(let [a (-> iris
            (pj/pose {:color :species})
            (pj/facet-matrix [:sepal-length :sepal-width]))
      b (-> iris
            (pj/facet-matrix [:sepal-length :sepal-width]
                             {:color :species}))]
  (= a b))

(kind/test-last [true?])

;; ---
;; ## Layer Placement
;;
;; Where `lay-*` calls place the layer in the pose tree. The rules
;; cover whether the call names `:x` and `:y`, whether the receiver is
;; a leaf or a composite, the raw-data convenience case, the panel the
;; layers of a leaf share, and the columns a layer reads from that
;; panel.
;;
;; **Where the columns are written:** the columns a `lay-*` call names
;; are written on the **layer's own `:mapping`**, and only there; the
;; pose's `:mapping` is what `pj/pose` writes. The two meet by scope
;; merge when the pose is drawn: a layer that names a column overrides
;; the pose's, and a layer that names none reads the pose's.

;; ### Rule LP1: bare `lay-*` attaches at the current pose's root
;;
;; A `lay-*` call naming no columns attaches the layer to
;; the current pose's top-level `:layers`. On a leaf, that is the
;; leaf's own `:layers`. On a composite, it is the root `:layers`,
;; and the layer flows into every descendant leaf when rendered.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (and (= 1 (count (:layers pose)))
         (= :point (:layer-type (first (:layers pose))))
         (empty? (or (:mapping (first (:layers pose))) {}))))])

;; On a composite, the same call attaches at root and reaches every
;; panel when rendered:

(-> (pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    pj/lay-point)

(-> (pj/arrange
     [(pj/pose iris :sepal-length :sepal-width)
      (pj/pose iris :petal-length :petal-width)])
    pj/lay-point
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (contains? pose :poses)
         (= 1 (count (:layers pose)))
         (= :point (:layer-type (first (:layers pose))))))])

;; **Property P-LP1 -- bare layers flow downward.** After adding one
;; bare layer to a composite, the composite's root `:layers` holds that
;; single entry. Each cell whose number of columns the layer type draws
;; from draws the root's layer first, underneath the cell's own layers,
;; and a cell that draws the root's layer has no layer inferred for it.

(let [before (pj/arrange
              [(pj/pose iris :sepal-length :sepal-width)
               (pj/pose iris :petal-length :petal-width)])
      after  (-> (pj/arrange
                  [(pj/pose iris :sepal-length :sepal-width)
                   (pj/pose iris :petal-length :petal-width)])
                 pj/lay-point)]
  [(count (or (:layers before) []))
   (count (or (:layers after)  []))])

(kind/test-last
 [(fn [counts]
    (let [marks (fn [pose]
                  (mapv #(mapv :mark (-> % :plan :panels first :layers))
                        (:sub-plots (pj/plan pose))))]
      (and (= [0 1] counts)
           ;; The root's point layer is drawn first, under the cell's
           ;; own smooth; the cell with no layer of its own draws the
           ;; root's layer and has none inferred.
           (= [[:point :line] [:point]]
              (marks (-> (pj/arrange
                          [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-smooth)
                           (pj/pose iris :petal-length :petal-width)])
                         pj/lay-point)))
           (= [[:line] [:line]]
              (marks (-> (pj/arrange
                          [(pj/pose iris :sepal-length :sepal-width)
                           (pj/pose iris :petal-length :petal-width)])
                         pj/lay-smooth)))
           ;; A cell drawing one column on both axes keeps its
           ;; histogram: the point layer draws from two columns.
           (= [[:bar] [:point] [:point] [:bar]]
              (marks (-> iris
                         (pj/facet-matrix [:sepal-length :sepal-width])
                         pj/lay-point))))))])

;; ### Rule LP2: on a composite, a `lay-*` naming columns attaches to the last matching leaf
;;
;; When a `lay-*` call names `:x` and `:y` on a composite, and the
;; panel of at least one leaf draws those two columns, the layer
;; attaches to the **last such leaf in left-to-right depth-first
;; order**. The columns of a leaf's panel are the `:x` and `:y` of the
;; leaf's mapping after the mappings above the leaf are merged in;
;; where that mapping names no column on an axis, the panel's column on
;; that axis is the column of the leaf's first layer that names a
;; column there. The layer's own `:mapping` carries the columns the
;; call named. This is the
;; same "most recent matching leaf" rule the teaching chapters
;; describe, formalized for composite poses where reading-order
;; depth-first traversal gives the precise answer. Matching is by
;; strict equality: a column reference is the keyword or string the
;; user typed, and the two forms are not interchangeable.

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point :sepal-length :sepal-width))

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (cells pose)))
         (= 1 (count (:layers (first (cells pose)))))
         (= 0 (count (:layers (second (cells pose)))))
         (= :point
            (:layer-type (first (:layers (first (cells pose))))))))])

;; A cell built by `lay-*` names its columns on its layer and not in
;; its own mapping. The panel's columns are the same either way, so the
;; layer attaches to that cell too:

(-> (pj/arrange [(pj/lay-point iris :sepal-length :sepal-width)
                 (pj/lay-point iris :petal-length :petal-width)])
    (pj/lay-smooth :petal-length :petal-width))

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (cells pose)))
         (= [[:point] [:point :smooth]]
            (mapv #(mapv :layer-type (:layers %)) (cells pose)))
         (= {:panels 2 :points 300 :lines 1}
            (select-keys (pj/svg-summary pose) [:panels :points :lines]))))])

;; **Note on leaf-input with other columns.**
;; A leaf that already carries columns, called with a `lay-*` that
;; names **different** columns, keeps the layer with the columns the
;; call named written on it. The pose stays a leaf, and the layer is
;; drawn on the leaf's one panel -- see Rule LP5. The pose's own columns
;; are drawn only by layers that name none, so here only the petal
;; columns are drawn, and drawing the pose prints a warning that the
;; pose's `:x` and `:y` are drawn by no layer:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))

(kind/test-last
 [(fn [fr]
    (and (nil? (:poses fr))
         (= {:x :sepal-length :y :sepal-width} (:mapping fr))
         (= [{:x :petal-length :y :petal-width}] (mapv :mapping (:layers fr)))
         (= {:panels 1 :points 150}
            (select-keys (pj/svg-summary fr) [:panels :points]))
         (re-find #"a pose maps :x to :sepal-length, and every layer drawn on it names its own :x, so :sepal-length is not drawn"
                  (with-out-str (pj/plan fr)))))])

;; **Where the columns are written does not change the rule.** A layer
;; names its columns either in the call's argument slots or in its
;; mapping, and identity reads both alike:
;; `(pj/lay-line pose :sepal-length :petal-width)` and
;; `(pj/lay-line pose {:x :sepal-length :y :petal-width})` land on the
;; same leaf. A written value is not a column -- it places a mark at
;; that value -- and the layer's data decides which of the two a value
;; is.
;;
;; Two lines over one `:x`, each naming its own `:y` in its mapping,
;; therefore draw on one panel, the y axis covering both columns:

(-> {:X [1 2 3 4 5] :Y [1 2 3 4 5] :Z [1 4 9 16 25]}
    (pj/pose :X)
    (pj/lay-line {:y :Y})
    (pj/lay-line {:y :Z}))

(kind/test-last
 [(fn [pose]
    (and (nil? (:poses pose))
         (= [{:y :Y} {:y :Z}] (mapv :mapping (:layers pose)))
         (= 1 (:panels (pj/svg-summary pose)))
         (some #{"Y, Z"} (:texts (pj/svg-summary pose)))))])

;; A written value in the same slot places a label. Here `:x` and `:y`
;; are numbers on a dataset that has no column named 2, so the label is
;; drawn at 2 and 4:

(-> {:X [1 2 3 4 5] :Y [1 2 3 4 5] :Z [1 4 9 16 25]}
    (pj/lay-point :X :Y)
    (pj/lay-text {:x 2 :y 4 :text "a note"}))

(kind/test-last
 [(fn [pose]
    (and (nil? (:poses pose))
         (= 2 (count (:layers pose)))))])

;; ### Rule LP3: on a composite, a `lay-*` naming columns no leaf draws appends a new leaf at the root
;;
;; When a `lay-*` call names `:x` and `:y` and the panel of **no** leaf
;; draws those two columns, a new leaf is appended at the composite's
;; root `:poses`. It holds the one layer, and the layer's
;; own `:mapping` carries the columns the call named.
;; A leaf given other columns stays a leaf instead, and its layers share
;; its panel -- see LP2 above.

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point :sepal-length :petal-length))

(kind/test-last
 [(fn [pose]
    (and (= 3 (count (cells pose)))
         (nil? (:mapping (nth (cells pose) 2)))
         (= [{:x :sepal-length :y :petal-length}]
            (mapv :mapping (:layers (nth (cells pose) 2))))))])

;; The new leaf reads the data of the composite's root. Where the cells
;; carry their own data and the root has none, the new leaf has no data
;; to read: it draws a panel reading "no data", and the call prints a
;; Note. The Note names the columns the cells draw, for a layer meant
;; for one of the cells, and shows the call with `{:data data}`, for a
;; layer meant for a cell of its own.

(def cells-with-their-own-data
  (pj/arrange [(pj/lay-point iris :sepal-length :sepal-width)
               (pj/lay-point iris :petal-length :petal-width)]))

(with-out-str
  (pj/lay-smooth cells-with-their-own-data :sepal-length :petal-length))

(kind/test-last
 [(fn [note]
    (and (re-find #"Note: pj/lay-smooth names :sepal-length and :petal-length" note)
         (re-find #"That cell has no data" note)
         (re-find #"the cells draw :sepal-length / :sepal-width, :petal-length / :petal-width" note)
         (re-find #"\(pj/lay-smooth pose :sepal-length :petal-length \{:data data\}\)" note)))])

;; Passing the data on the call gives the new leaf its own:

(pj/lay-smooth cells-with-their-own-data :sepal-length :petal-length
               {:data iris})

(kind/test-last
 [(fn [pose]
    (and (= 3 (count (cells pose)))
         (= {:panels 3 :points 300 :lines 1}
            (select-keys (pj/svg-summary pose) [:panels :points :lines]))
         (not (some #{"no data"} (:texts (pj/svg-summary pose))))))])

;; **Property: a split and an arrangement draw the same panels.**
;; Two layers on different columns, split over those columns with
;; `pj/facet`, and the same layers attached to a composite built with
;; `pj/arrange` (LP2 sends each to its matching cell) draw the same
;; panels with the same marks. The pose values differ: the facet keeps
;; one leaf and expands it into cells when it is drawn, while
;; `pj/arrange` builds a composite.

(let [via-facet (-> iris
                    (pj/lay-point :sepal-length :sepal-width)
                    (pj/lay-point :petal-length :petal-width)
                    (pj/facet {:x [:sepal-length :petal-length]
                               :y [:sepal-width :petal-width]}))
      via-arrange (-> iris
                      (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
                      (pj/lay-point :sepal-length :sepal-width)
                      (pj/lay-point :petal-length :petal-width))
      drawn (fn [pose] (select-keys (pj/svg-summary pose) [:panels :points]))]
  {:via-facet-drawn (drawn via-facet)
   :via-arrange-drawn (drawn via-arrange)
   :via-facet-shape (if (:poses via-facet) :composite :leaf)
   :via-arrange-shape (if (:poses via-arrange) :composite :leaf)})

(kind/test-last
 [(fn [m] (and (= (:via-facet-drawn m) (:via-arrange-drawn m))
               (= :leaf (:via-facet-shape m))
               (= :composite (:via-arrange-shape m))))])

;; **Property: column-existence safety check.**
;; When a `lay-*` call names a column on an axis where the pose already
;; draws another column, it first checks that those columns exist in the data the layer would draw
;; from -- the layer's own `:data` if present, otherwise the inherited
;; data. A missing column is almost always a typing mistake, so the
;; call reports it at once, with the columns the data has. A first
;; `lay-*` call on raw data is not checked at the call; a missing column
;; there is reported when the pose is drawn.
;;
;; The check at the call covers the columns written in the argument
;; slots. A name written in the layer's mapping may be a column or a
;; value, which the data decides when the pose is drawn, so a name there
;; that matches no column is reported when the pose is drawn.

(try
  (-> iris
      (pj/pose :sepal-length :sepal-width)
      (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg]
    (let [in-mapping (-> iris
                         (pj/pose :sepal-length :sepal-width)
                         (pj/lay-point {:y :nope}))]
      (and (string? msg)
           (re-find #"doesn't exist in the data" msg)
           ;; Written in the mapping, the name passes the call and is
           ;; reported when the pose is drawn.
           (pj/pose? in-mapping)
           (re-find #"Column :nope \(from :y\) not found"
                    (try (pj/plan in-mapping) ""
                         (catch Exception e (ex-message e)))))))])

;; Supplying `:data` on the lay-* call satisfies the safety check: the
;; layer draws its own data, on the leaf's panel.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :foo :bar
                  {:data (tc/dataset {:foo [1 2 3] :bar [4 5 6]})}))

(kind/test-last
 [(fn [fr]
    (and (nil? (:poses fr))
         (= [{:x :foo :y :bar}] (mapv :mapping (:layers fr)))
         (= {:panels 1 :points 3}
            (select-keys (pj/svg-summary fr) [:panels :points]))))])

;; ### Rule LP4: `lay-*` on raw data coerces the data into a leaf pose
;;
;; `lay-*` called with a dataset as its first argument coerces the
;; data into a leaf pose first, then applies the layer. The result
;; draws what `(-> data (pj/pose :x :y) pj/lay-point)` draws. This
;; keeps the convenience one-liner `(-> data (pj/lay-point :x :y))`
;; working as the shortest path from data to plot.

(def tiny
  {:a [1 2 3 4 5]
   :b [2 4 3 5 4]})

(-> tiny
    (pj/lay-point :a :b))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; The explicit two-step form draws the same plot. The two poses differ
;; in where the columns are written: the `lay-*` call above keeps them
;; on its layer, and here `pj/pose` writes them on the pose, where every
;; layer added afterwards reads them:

(-> tiny
    (pj/pose :a :b)
    pj/lay-point
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:x :a :y :b} (:mapping pose))
         (= 1 (count (:layers pose)))
         (not (contains? pose :poses))))])

;; ### Rule LP5: the layers of a leaf share its panel
;;
;; A leaf draws one panel, and every layer on it draws there, whatever
;; columns it names. Its `:x` and `:y` stay on the layer's own
;; `:mapping`, and the leaf's `:mapping` is left as it was.
;;
;; Each axis is titled by every column drawn on it. A layer with no
;; `:color` specified, on the pose or on the layer, also takes a colour
;; and a legend entry naming its columns by default, unless a layer of
;; the panel is coloured by a column. Each layer below specifies its
;; colour, so no colour and no legend entry is added.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
    (pj/lay-point :petal-length :petal-width {:color "#e6550d"}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)
                axis-titles (filter #{"sepal length" "sepal width"
                                      "petal length" "petal width"}
                                    (:texts s))]
            (and (= 1 (:panels s))
                 (= 300 (:points s))
                 ;; Both layers drew, each in its own colour.
                 (contains? (:colors s) "rgb(55,126,184)")
                 (contains? (:colors s) "rgb(230,85,13)")
                 ;; Each axis is titled by both columns drawn on it,
                 ;; and no legend entry names a column on its own.
                 (empty? axis-titles)
                 (some #{"sepal length, petal length"} (:texts s))
                 (some #{"sepal width, petal width"} (:texts s)))))])

;; One panel with the blue sepal points and the orange petal points on
;; it, each axis titled by both of its columns, and their domains
;; stretched to hold the petal values too. Printed, the leaf's
;; `:mapping` is untouched and the petal columns sit on the second
;; layer:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
    (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (not (contains? pose :poses))
         (= {:x :sepal-length :y :sepal-width} (:mapping pose))
         (= [{:color "#377eb8" :x :sepal-length :y :sepal-width}
             {:color "#e6550d" :x :petal-length :y :petal-width}]
            (mapv :mapping (:layers pose)))))])

;; On a composite, LP3 still applies: a layer naming columns no cell
;; draws is a cell of its own.

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-point :sepal-length :sepal-width {:color "#377eb8"})
    (pj/lay-point :petal-length :petal-width {:color "#e6550d"})
    (pj/lay-point :sepal-width :petal-width {:color "#4daf4a"}))

(kind/test-last
 [(fn [v] (= {:panels 3 :points 450}
             (select-keys (pj/svg-summary v) [:panels :points])))])

;; Separate panels for the layers of one leaf come from a split,
;; `(pj/facet pose {:x [...] :y [...]})`, or from `pj/arrange`. Asking
;; for them with `{:overlay false}` or `(pj/overlay pose false)` reports
;; an error naming both:

(try
  (-> iris
      (pj/lay-point :sepal-length :sepal-width)
      (pj/lay-point :petal-length :petal-width {:overlay false}))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (and (re-find #":overlay is retired" msg)
                 (re-find #"pj/facet" msg)
                 (re-find #"pj/arrange" msg)))])

;; ### Rule LP6: a layer reads the panel's column where it needs one
;;
;; The panel's column on an axis is the column the pose names there,
;; and where the pose names none, the column of the first layer that
;; names one there. A layer reads the panel's column on an axis where
;; it names none and its layer type needs one: both columns for a layer
;; naming none, and the other column for a layer naming one whose layer
;; type draws from two.

(-> iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-line {:y :petal-width})
    pj/lay-smooth)

(kind/test-last
 [(fn [v] (= [[:sepal-length :sepal-width]
              [:sepal-length :petal-width]
              [:sepal-length :sepal-width]]
             (mapv (juxt :x :y) (:layers (pj/draft v)))))])

;; The columns stay on the layers. The pose's own mapping is not
;; written, so a layer type that can draw from one column reads no
;; second column from a neighbouring layer:

(-> iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-rug {:x :petal-length})
    pj/draft
    :layers
    second
    (select-keys [:mark :x :y]))

(kind/test-last [(fn [m] (= {:mark :rug :x :petal-length} m))])

;; ---
;; ## Leaf Identity
;;
;; How columns identify a leaf. One rule, about inference when the
;; user omits column names. (How column references are compared is
;; covered by Rule LP2.)

;; ### Rule LI1: few-column datasets infer columns from column order
;;
;; When `lay-*` or `pj/pose` is called on a dataset without
;; explicit column arguments, the mapping is read off the order of
;; the dataset's columns when the pose is drawn:
;;
;; | Columns | Inferred mapping |
;; |:--------|:-----------------|
;; | 1 | `{:x col0}` |
;; | 2 | `{:x col0 :y col1}` |
;; | 3 | `{:x col0 :y col1 :color col2}` |
;; | 4+ | none (name the columns) |
;;
;; The example is a dataset rather than a map literal, because the rule
;; reads an order and a Clojure map does not carry one.

(-> (tc/dataset {:height [1 2 3] :weight [4 5 6] :species ["a" "b" "a"]})
    pj/lay-point)

(kind/test-last
 [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; With four or more columns no column is read by default. The
;; `pj/lay-point` call returns a pose, and a later step may name the
;; columns:

(-> (tc/dataset {:a [1 2] :b [3 4] :c [5 6] :d [7 8]})
    pj/lay-point
    (pj/pose {:x :a :y :b}))

(kind/test-last
 [(fn [v] (= 2 (:points (pj/svg-summary v))))])

;; Drawn with no column named, the pose reports an error naming the
;; layer and the columns of the data:

(try
  (-> (tc/dataset {:a [1 2] :b [3 4] :c [5 6] :d [7 8]})
      pj/lay-point
      pj/plan)
  (catch Exception e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (and (re-find #"this layer has no column on :x or :y" msg)
                 (re-find #"Available columns: \[:a :b :c :d\]" msg)))])

;; ---
;; ## Scope
;;
;; How mappings and data flow through the pose tree. Four rules
;; covering the root, then composite, then leaf, then layer chain
;; at arbitrary depth.

;; ### Rule S1: mapping scope is a tree-walk merge; narrower wins
;;
;; The effective `:mapping` for a rendered layer is computed by
;; merging, in order: root's `:mapping`, then each ancestor
;; composite's `:mapping`, then the leaf's own `:mapping`, then the
;; layer's own `:mapping`. Inner keys override outer. Any depth of
;; composite nesting works the same way.

;; Root-level aesthetic flows to every leaf. Using a two-panel
;; composite with `:color` declared at root:

(def s1-composite
  (pj/pose
   {:mapping {:color :species}
    :poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :petal-length :y :petal-width}
             :layers [{:layer-type :point}]}]
    :data iris}))

s1-composite

(kind/test-last
 [(fn [pose]
    (let [plan (pj/plan pose)
          panels (mapv (comp :panels :plan) (:sub-plots plan))]
      ;; Both sub-plots render colored-by-species -- 3 groups per panel
      (every? (fn [pp]
                (= 3 (count (:groups (first (:layers (first pp)))))))
              panels)))])

;; **Property P-S1 -- sibling independence.** A sub-pose's own
;; mapping does not leak into its siblings.

(def s1-siblings
  (pj/pose
   {:poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :petal-length :y :petal-width :color :species}
             :layers [{:layer-type :point}]}]
    :data iris}))

s1-siblings

(kind/test-last
 [(fn [pose]
    (let [sub-plots (:sub-plots (pj/plan pose))
          panel-groups (mapv (fn [sp]
                               (count (:groups (first (:layers
                                                       (first (-> sp :plan :panels)))))))
                             sub-plots)]
      ;; Sub-plot 0: no color -> 1 group. Sub-plot 1: :color :species -> 3.
      (= [1 3] panel-groups)))])

;; ### Rule S2: data scope -- nearest-ancestor-non-nil wins
;;
;; The effective `:data` for a rendered layer is the nearest
;; non-nil `:data` walking from the layer up through each ancestor
;; to the root. Layer `:data` > leaf `:data` > nearest ancestor
;; composite `:data` > root `:data`. Unlike mappings, data does not
;; merge -- it is picked, wholesale.

(def s2-tree
  (pj/pose
   {:poses [{:mapping {:x :sepal-length :y :sepal-width}
             :layers [{:layer-type :point}]}
            {:mapping {:x :a :y :b}
             :layers [{:layer-type :point}]
             :data (tc/dataset {:a [1 2 3] :b [3 5 4]})}]
    :data iris}))

s2-tree

(kind/test-last
 [(fn [pose]
    (let [sub-plots (:sub-plots (pj/plan pose))
          counts (mapv (fn [sp]
                         (-> sp :plan :panels first :layers first
                             :groups first :xs count))
                       sub-plots)]
      ;; Sub-plot 0 inherits root's iris (150 rows). Sub-plot 1 uses its own (3).
      (= [150 3] counts)))])

;; ### Rule S3: `nil` in a mapping cancels an inherited value
;;
;; Assigning `nil` to a mapping key at an inner scope cancels the
;; value inherited from outer scopes. The rendering path treats a
;; nil mapping value as equivalent to "no mapping for that
;; aesthetic."

(-> iris
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:color nil :stat :linear-model}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            ;; lm produces one overall line (not three), because its
            ;; :color was canceled
            (and (= 150 (:points s))
                 (= 1 (:lines s)))))])

;; ### Rule S4: layer `:mapping` is the narrowest scope
;;
;; A mapping written in a layer's own `:mapping` (aesthetic mappings
;; passed to `lay-*`) scopes to that layer only. Other layers --
;; even on the same leaf -- do not read that mapping. This is the
;; terminal case of S1: the layer's mapping is innermost in the merge.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species})
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            ;; Point layer sees :species (colored points); smooth
            ;; layer does not (one overall regression line).
            (and (= 150 (:points s))
                 (= 1 (:lines s)))))])

;; ---
;; ## Options
;;
;; Plot-level options and modifiers. Unlike mappings, layers, and
;; data (which live in the scope hierarchy), options configure the
;; whole rendered plot and attach to the root's `:opts`.

;; ### Rule O1: `pj/options` writes to the root's `:opts`
;;
;; `pj/options` merges its argument into the current pose's
;; `:opts`. On a leaf, that is the leaf's `:opts`. On a composite,
;; the root's. Options do not flow down like mappings -- they are
;; plot-level, not layer-level.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/options {:title "Iris"}))

(kind/test-last
 [(fn [pose] (= "Iris" (get-in pose [:opts :title])))])

;; Repeated calls merge, later-wins on collisions:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/options {:title "One"})
    (pj/options {:title "Two" :subtitle "Sub"}))

(kind/test-last
 [(fn [pose]
    (and (= "Two" (get-in pose [:opts :title]))
         (= "Sub" (get-in pose [:opts :subtitle]))))])

;; ### Rule O2: `pj/scale` writes the mapping, `pj/coord` writes options
;;
;; A scale belongs to the aesthetic it reads, so `pj/scale` writes it
;; into the pose's `:mapping`, under that aesthetic's `:scale`. It is
;; the same key a mapping written out in full uses, so the two are one
;; setting written two ways. `pj/coord` writes `:coord` into `:opts`,
;; which has no aesthetic to belong to.
;;
;; Axis aesthetics (`:x`, `:y`) accept `:linear`, `:log`,
;; `:categorical`; the visual ones (`:size`, `:alpha`, `:fill`,
;; `:color`) accept `:linear` and `:log`; `:shape` accepts
;; `:categorical`. Both `pj/scale` and `pj/coord` flow down to every
;; leaf beneath the pose they are called on.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/scale :x :log)
    (pj/coord :flip))

(kind/test-last
 [(fn [pose]
    (and (= {:type :log} (get-in pose [:mapping :x :scale]))
         (= :flip (get-in pose [:opts :coord]))))])

;; Where the aesthetic is already mapped, the scale joins that mapping.
;; A mapping written plainly has no room for one, so it is rewritten in
;; the full form under `:from` -- the plain reading spelled out, which
;; leaves what the mapping names unchanged:

(-> iris
    (pj/pose :sepal-length :sepal-width {:size :petal-length})
    pj/lay-point
    (pj/scale :size :log))

(kind/test-last
 [(fn [pose]
    (= {:from :petal-length :scale {:type :log}}
       (get-in pose [:mapping :size])))])

;; Where it is not mapped at that pose, the scale is written on its
;; own and applies to whatever names the aesthetic below:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:size :petal-length})
    (pj/scale :size :log))

(kind/test-last
 [(fn [pose]
    (and (= {:scale {:type :log}} (get-in pose [:mapping :size]))
         (= {:type :log} (-> pose pj/plan :panels first :layers first
                             :size-scale))))])

;; ### Rule O3: `pj/facet` writes a faceting column to the mapping, and a facet over columns to `:facet`
;;
;; `pj/facet` and `pj/facet-grid` write the faceting column into the
;; pose's `:mapping` under `:col` (and `:row` for a grid). These are
;; aesthetics, so they follow the scope rules every other mapping
;; follows: written on a pose they reach every layer and every
;; sub-pose below, and a sub-pose that writes its own overrides them.
;; The layout effect -- splitting each leaf's panel into a group of
;; panels -- happens at render time.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/facet :species))

(kind/test-last
 [(fn [pose] (= :species (get-in pose [:mapping :col])))])

;; A 2D grid uses both keys:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/facet-grid :species :species))

(kind/test-last
 [(fn [pose]
    (and (= :species (get-in pose [:mapping :col]))
         (= :species (get-in pose [:mapping :row]))))])

;; Because the faceting column is a mapping, a facet written on a
;; composite reaches every cell of it, and a cell that writes its own
;; overrides what it inherits:

(-> (pj/arrange [(pj/pose iris :sepal-length :sepal-width)
                 (pj/pose iris :petal-length :petal-width)])
    pj/lay-point
    (pj/facet :species))

(kind/test-last
 [(fn [pose] (= 6 (:panels (pj/svg-summary pose))))])

(-> (pj/arrange [(-> (pj/pose iris :sepal-length :sepal-width)
                     (pj/facet :species))
                 (pj/pose iris :petal-length :petal-width)])
    pj/lay-point)

(kind/test-last
 ;; Three panels in the cell that facets, one in the cell that does
 ;; not -- the inherited :col reaches only where none is written.
 [(fn [pose] (= 4 (:panels (pj/svg-summary pose))))])

;; A facet over columns is not a mapping. `pj/facet` given a map naming
;; columns, such as `{:y [:sepal-width :petal-width]}`, keeps it under
;; the pose's `:facet` key, and the pose is expanded into a cell per
;; column when it is drawn:

(-> iris
    (pj/pose {:x :sepal-length})
    pj/lay-point
    (pj/facet {:y [:sepal-width :petal-width]})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:y [:sepal-width :petal-width]} (:facet pose))
         (nil? (get-in pose [:mapping :col]))
         (= 2 (:panels (pj/svg-summary pose)))))])

;; ### Rule O4: `pj/lay-rule-*` and `pj/lay-band-*` are layers
;;
;; `pj/lay-rule-h`, `pj/lay-rule-v`, `pj/lay-band-h` and `pj/lay-band-v`
;; produce layers, and the layers are placed as any other `lay-*` layer
;; is: a call naming no columns attaches at the root and is drawn in
;; every panel, and on a composite a call naming two columns attaches
;; to the leaf that draws them. On a pose with one panel, columns
;; written on a rule or a band that the panel does not draw are not
;; read: the rule is drawn on that panel, and a warning names the
;; columns. Where the line or the band sits
;; is written as layer-type keys (`:y-intercept`, `:x-intercept`,
;; `:y-min`/`:y-max`, `:x-min`/`:x-max`), not as columns.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point {:color :species})
    (pj/lay-rule-h {:y-intercept 3.0}))

(kind/test-last
 [(fn [pose]
    (let [layers (:layers pose)
          rule (some #(when (= :rule-h (:layer-type %)) %) layers)]
      (and (some? rule)
           (= 3.0 (get-in rule [:mapping :y-intercept])))))])

;; A rule naming two columns, on a composite, attaches to the leaf that
;; draws them and is drawn in that leaf's panel only:

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0}))

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (cells pose)))
         (= 1 (count (:layers (first (cells pose)))))
         (= 0 (count (:layers (second (cells pose)))))
         (= :rule-h (:layer-type
                     (first (:layers (first (cells pose))))))))])

;; The same call on a pose with one panel draws the rule on that panel.
;; The petal columns are not read, so the axes are those of the
;; points:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/lay-rule-h :petal-length :petal-width {:y-intercept 3.0}))

(kind/test-last
 [(fn [pose]
    (let [y-domain #(-> % pj/plan :panels first :y-domain)
          points (-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)]
      (and (= 1 (:panels (pj/svg-summary pose)))
           (= (y-domain points) (y-domain pose))
           (re-find #"pj/lay-rule-h names :petal-length on :x and :petal-width on :y, columns the panel does not draw, so they are not read"
                    (with-out-str (pj/plan pose))))))])

;; ---
;; ## Assembly
;;
;; How the rules above combine to produce rendered layers. Each
;; rendered layer corresponds to one (leaf, applicable-layer)
;; pair, with all ancestor scope merged in.

;; ### Rule A1: one rendered layer per applicable (leaf, layer) pair
;;
;; For each leaf, the rendered layers are the leaf's own, plus each
;; layer written above it whose layer type draws from as many columns
;; as the leaf's panel (Property P-LP1). A leaf drawing none of these
;; has a layer inferred for it.

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    pj/lay-point                            ;; written on the root; reaches both
    (pj/lay-smooth :sepal-length :sepal-width
                   {:stat :linear-model}))  ;; written on sub-pose 1 only

(kind/test-last
 [(fn [pose]
    (let [plan (pj/plan pose)
          panel-layer-counts (mapv (fn [sp]
                                     (count (:layers (first (-> sp :plan :panels)))))
                                   (:sub-plots plan))]
      ;; sub-plot 0: point + smooth (2); sub-plot 1: point only (1)
      (= [2 1] panel-layer-counts)))])

;; ### Rule A2: each rendered layer carries fully merged scope
;;
;; Every layer rendered in the final plot reflects the full scope
;; merge: the dataset, every mapping that flows into it, `:x` and `:y`
;; among them, and the layer-type bundle (plus any
;; `:stat`, `:position`, `:mark` overrides). No scope level is
;; dropped; no key is unresolved.

(-> iris
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point)

(kind/test-last
 [(fn [_]
    (let [draft (-> iris
                    (pj/pose :sepal-length :sepal-width {:color :species})
                    pj/lay-point
                    pj/draft)
          layers (:layers draft)]
      (and (= 1 (count layers))
           (let [d (first layers)]
             (and (= :sepal-length (:x d))
                  (= :sepal-width (:y d))
                  (= :species (:color d))
                  (= :point (:mark d))
                  (= 150 (tc/row-count (:data d))))))))])

;; ---
;; ## Layout
;;
;; How leaves become panels in the rendered plot. Five rules
;; covering single panels, layers sharing a panel, faceting, composite
;; grids with shared scales, and the matrix `pj/facet-matrix` writes.

;; ### Rule L1: each leaf produces a panel block
;;
;; Each leaf produces one **panel block** in the rendered plot. Unfaceted,
;; the block is one panel, which all of the leaf's layers share (Rule
;; LP5); `pj/facet` or `pj/facet-grid` multiplies it by the facet values
;; (or by the (row, col) pairs).
;;
;; A facet over columns expands the leaf into one cell per column when
;; the pose is drawn, so the plan of such a leaf holds `:sub-plots`, as
;; the plan of a composite does.

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (let [plan (pj/plan pose)
          by-columns (pj/plan (-> iris
                                  (pj/pose {:x :sepal-length})
                                  pj/lay-point
                                  (pj/facet {:y [:sepal-width :petal-width]})))]
      (and (:composite? plan)
           (= 2 (count (:sub-plots plan)))
           ;; A leaf faceted over columns plans as a composite too.
           (:composite? by-columns)
           (= 2 (count (:sub-plots by-columns)))
           (empty? (:panels by-columns)))))])

;; One leaf whose layers name two pairs of columns is one panel:

(-> iris
    (pj/lay-point :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))

(kind/test-last
 [(fn [pose]
    (and (nil? (:poses pose))
         (= 1 (:panels (pj/svg-summary pose)))))])

;; ### Rule L2: layers of a leaf overlay within its panel
;;
;; Layers applicable to a leaf (the leaf's own plus the layers written
;; on the root that it draws) draw on the same axis pair -- they overlay
;; within the leaf's panel, not on separate panels (Rule LP5).

(-> iris
    (pj/pose :sepal-length :sepal-width {:color :species})
    pj/lay-point
    (pj/lay-smooth {:stat :linear-model}))

(kind/test-last
 [(fn [pose]
    (let [plan (pj/plan pose)
          panel (first (:panels plan))]
      (and (= 1 (count (:panels plan)))
           (= 2 (count (:layers panel))))))])

;; ### Rule L3: faceting splits each leaf into panels, by category or by column
;;
;; `(pj/facet pose :species)` draws one panel per distinct value of
;; `:species`. On the mpg data, `(pj/facet-grid pose :drv :cyl)` draws
;; one panel per pair of values, the values of `:drv` across and the
;; values of `:cyl` down. `(pj/facet pose {:y [:hwy :cty]})` draws one
;; panel per column (Rule O3). Each of the three takes a map of plot
;; options as its last argument, which is the map `pj/options` takes.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/facet :species))

(kind/test-last
 [(fn [pose]
    (let [mpg-pose (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
      (and (= 3 (count (:panels (pj/plan pose))))
           ;; The first column runs across (:col), the second down (:row).
           (= {:col :drv :row :cyl}
              (select-keys (:mapping (pj/facet-grid mpg-pose :drv :cyl))
                           [:col :row]))
           (= "Faceted"
              (get-in (pj/facet pose :species {:title "Faceted"})
                      [:opts :title]))
           (= "Faceted"
              (get-in (pj/facet-grid mpg-pose :drv :cyl {:title "Faceted"})
                      [:opts :title]))
           (= "Faceted"
              (get-in (pj/facet mpg-pose {:y [:hwy :cty]} {:title "Faceted"})
                      [:opts :title])))))])

;; ### Rule L4: composite layout is controlled by `:layout` and optional `:share-scales`
;;
;; A composite pose carries a `:layout` map, which holds the direction
;; of its rows and, in each row, the direction of the cells.
;; `pj/arrange` writes `:title`, `:width`, `:height` and `:share-scales`
;; into the composite's `:opts`, and `:cols` is not stored (Rule C8).
;; An optional
;; `:share-scales` (a subset of `#{:x :y}`, or a map from an axis to
;; `:same-column`, `:all`, or groups of columns) enables shared-scale
;; resolution across sub-poses. Under a set, sharing is column-bucketed.
;;
;; **Column-bucketing**: when `:x` is shared, sub-poses whose
;; effective `:x` column is the same share that scale's domain;
;; sub-poses with different `:x` columns get independent
;; x-domains. Same for `:y`. Marginal plots rest on this: a top strip draws the scatter's `:x` column on the
;; horizontal axis and shares `:x` with the scatter. A right strip is
;; drawn with `:coord :flip`, so the scatter's `:y` column runs up the
;; strip's vertical axis, and the strip and the scatter share `:y`. In
;; `:share-scales`, `:x` is the horizontal axis and `:y` the vertical
;; axis, also for a panel drawn with `:coord :flip`. A count or density
;; axis is shared among the cells that compute the same thing from the
;; same column.

(def l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}}))

l4-shared

(kind/test-last
 [(fn [pose]
    (let [x-domains (fn [p]
                      (mapv #(get-in % [:plan :panels 0 :x-domain])
                            (:sub-plots (pj/plan p))))
          domains (x-domains pose)
          ;; Both cells above hold the whole dataset, so their x
          ;; domains would agree with or without the setting -- the
          ;; example shows the shape, and this shows the effect. Two
          ;; cells over different species have different extents of
          ;; sepal-length, and only the shared pose pins them to one.
          cells (fn [share]
                  (pj/arrange
                   [(-> iris
                        (tc/select-rows #(= "setosa" (:species %)))
                        (pj/pose :sepal-length :sepal-width)
                        pj/lay-point)
                    (-> iris
                        (tc/select-rows #(= "virginica" (:species %)))
                        (pj/pose :sepal-length :petal-width)
                        pj/lay-point)]
                   (if share {:share-scales #{:x}} {})))]
      ;; Read from the panel rather than from its scale spec: a shared
      ;; extent is not written as a `:domain`, so asking the scale for
      ;; one answers nil for every cell and `nil = nil` passes whatever
      ;; the panels were drawn against.
      (and (= 2 (count domains))
           (every? some? domains)
           (apply = domains)
           (apply = (x-domains (cells true)))
           (apply not= (x-domains (cells false)))
           ;; A right marginal is drawn on its side and shares the
           ;; vertical axis with the scatter.
           (apply = (mapv #(get-in % [:plan :panels 0 :y-domain])
                          (:sub-plots (pj/plan (-> iris
                                                   (pj/lay-point :sepal-length :sepal-width)
                                                   (pj/marginal :right))))))
           ;; Two histograms of one column share their count axis.
           (apply = (mapv #(get-in % [:plan :panels 0 :y-domain])
                          (:sub-plots
                           (pj/plan
                            (pj/arrange
                             [(-> iris
                                  (tc/select-rows #(= "setosa" (:species %)))
                                  (pj/lay-histogram :sepal-length))
                              (-> iris
                                  (tc/select-rows #(= "virginica" (:species %)))
                                  (pj/lay-histogram :sepal-length))]
                             {:share-scales #{:y}}))))))))])

;; ### Rule L5: `pj/facet-matrix` is a facet with one panel per pair of columns
;;
;; `(pj/facet-matrix data xs ys)` returns a leaf pose -- no `:poses` -- that
;; carries the facet map `{:col {:x xs} :row {:y ys}}`: a column of the
;; grid for each column of `xs`, a row for each column of `ys`, and a
;; panel where they cross. It draws what `pj/facet` given that map
;; draws (Rule L3), so its axes, tick labels and strips are a facet's.
;;
;; A panel with one column on both axes draws a histogram of that
;; column, and every other panel draws the layer type inferred for its
;; two columns -- a scatter for two numerical columns. A layer added
;; with `pj/lay-*` is drawn in the panels whose number of columns its
;; layer type draws from, so `pj/lay-point` after `pj/facet-matrix`
;; leaves the histograms on the diagonal (Property P-LP1). `pj/pose`
;; given pairs of columns reports an error that suggests `pj/arrange`
;; and `pj/facet-matrix`.

(-> iris
    (pj/facet-matrix [:sepal-length :sepal-width]
                     [:petal-length :petal-width]
                     {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= {:col {:x [:sepal-length :sepal-width]}
             :row {:y [:petal-length :petal-width]}}
            (:facet pose))
         (= {:color :species} (:mapping pose))
         ;; The same picture as the facet map written out.
         (= (pj/plot pose)
            (pj/plot (-> iris
                         (pj/pose {:color :species})
                         (pj/facet {:col {:x [:sepal-length :sepal-width]}
                                    :row {:y [:petal-length :petal-width]}}))))
         ;; pj/pose given pairs points at the two functions.
         (let [msg (try (pj/pose iris (pj/cross [:sepal-length] [:petal-length :petal-width]))
                        ""
                        (catch Exception e (ex-message e)))]
           (and (re-find #"pj/pose was given pairs of columns" msg)
                (re-find #"pj/arrange" msg)
                (re-find #"pj/facet-matrix" msg)))))])

;; ---
;; ## A Note on `pj/cross`
;;
;; `pj/cross` is not a rule. It returns the pairs
;; `(for [x xs y ys] [x y])` and draws nothing. `pj/arrange` takes the
;; pairs as cells, one panel per pair, and `pj/facet-matrix` writes the
;; facet that crosses the two lists of columns (Rule L5).
;; `pj/pose` given the pairs reports an error.

(pj/cross [:a :b] [:c :d])

(kind/test-last
 [(fn [pairs]
    (and (= [[:a :c] [:a :d] [:b :c] [:b :d]] pairs)
         (= 4 (:panels (pj/svg-summary
                        (-> iris
                            (pj/arrange (pj/cross [:sepal-length :sepal-width]
                                                  [:petal-length :petal-width]))
                            pj/lay-point))))))])

;; ## What's Next
;;
;; - [**Inference Rules**](./plotje_book.inference_rules.html) --
;;   how Plotje fills in defaults (column types, marks, stats,
;;   scales) when you do not specify them
;; - [**Layer Types**](./plotje_book.layer_types.html) -- the
;;   registry of mark + stat + position combinations
