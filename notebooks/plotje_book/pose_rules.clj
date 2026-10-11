;; # Pose Rules
;;
;; Each rule below states one behaviour and shows it on a rendered
;; pose, with the printed structure where the shape is the point.
;;
;; The rules are organized into seven sections (Construction, Layer
;; Placement, Leaf Identity, Scope, Options, Assembly, Layout) and
;; cover every call shape of `pj/pose`, `pj/lay-*`, `pj/arrange`,
;; `pj/options`, `pj/scale`, `pj/coord`, `pj/facet`, and `pj/cross`.
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
;; is `:x`; two keywords are `:x` and `:y`; an options map
;; contributes aesthetic keys.

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

;; With only an aesthetic mapping, position is omitted -- the pose
;; is a leaf with no position yet. Inference at render time will
;; handle picking an axis if a layer is added without position.

(-> iris
    (pj/pose {:color :species})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (not (contains? pose :poses))))])

;; ### Rule C2: `pj/pose` on an unpositioned leaf extends its mapping
;;
;; A leaf is **unpositioned** if neither its own `:mapping` nor any
;; of its layers' mappings carries `:x` or `:y`. Calling `pj/pose`
;; again on such a leaf merges the new mapping into the leaf's own;
;; the leaf remains a leaf. No composite is created.

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

;; **Property P-C2 -- construction commutativity.** A chained
;; unpositioned extension yields a pose structurally equal to the
;; same content expressed as one call.

(= (-> iris
       pj/pose
       (pj/pose {:color :species})
       (pj/pose :sepal-length :sepal-width))
   (pj/pose iris :sepal-length :sepal-width {:color :species}))

(kind/test-last [true?])

;; ### Rule C3: `pj/pose` with a different position on a positioned leaf replaces it
;;
;; `pj/pose` writes one mapping and does not add a panel. Called with
;; an `:x` or `:y` other than the one a positioned leaf has, it
;; replaces it, and the layers that name no columns of their own read
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

;; For panels side by side, `pj/arrange` takes the pairs of columns:

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    pj/lay-point)

(kind/test-last [(fn [v] (= 2 (:panels (pj/svg-summary v))))])

;; **Property P-C3 -- writing the same position again changes
;; nothing.** A position call naming the leaf's own `:x` and `:y`
;; returns the same pose.

(= (trend-plot iris)
   (pj/pose (trend-plot iris) :sepal-length :sepal-width))

(kind/test-last [true?])

;; ### Rule C4: aesthetic-only `pj/pose` on a positioned leaf merges into its mapping
;;
;; An aesthetic-only call (no `:x`/`:y`) on a positioned leaf merges
;; the aesthetic into the leaf's `:mapping`. The leaf stays a leaf.

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

;; **Property P-C4 -- aesthetic-then-position equivalence.** Adding
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
;; reaches every cell that does not map the same aesthetic itself. An
;; aesthetic usually reaches them all:

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

;; A position written on a composite whose cells each map their own
;; `:x` and `:y` reaches none of them. The cells keep their columns, and
;; drawing the pose warns that the root's position is not drawn:

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    (pj/pose :sepal-length :petal-length)
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (re-find #"not drawn" (with-out-str (pj/plan pose))))])

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
;; `pj/arrange` takes a sequence of poses (leaves in alpha) plus
;; optional layout options and returns a composite. The inputs become
;; the composite's `:poses`, wrapped in a 2-level row-and-column
;; layout.

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

;; Opts (`:cols`, `:title`, `:width`, `:height`, `:share-scales`)
;; route into the composite's `:opts` / `:layout`:

(pj/arrange
 [(pj/pose iris :sepal-length :sepal-width)
  (pj/pose iris :petal-length :sepal-width)]
 {:title "Arranged"
  :share-scales #{:y}})

(kind/test-last
 [(fn [pose]
    (and (= "Arranged" (get-in pose [:opts :title]))
         (= #{:y} (get-in pose [:opts :share-scales]))))])

;; ### Rule C9: `pj/cross-matrix` writes a mapping on the composite's root
;;
;; `pj/cross-matrix` given columns draws one cell for every pair of
;; them. Given a mapping as well, it writes that mapping on the
;; composite's root, so it reaches every cell.

(-> iris
    (pj/cross-matrix [:sepal-length :sepal-width]
                     {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (= 2 (count (:poses pose)))
         (every? #(= 2 (count (:poses %))) (:poses pose))))])

;; **Property P-C9 -- the mapping can come first or last.** A mapping
;; written with `pj/pose` before `pj/cross-matrix` gives an equal pose.

(let [a (-> iris
            (pj/pose {:color :species})
            (pj/cross-matrix [:sepal-length :sepal-width]))
      b (-> iris
            (pj/cross-matrix [:sepal-length :sepal-width]
                             {:color :species}))]
  (= a b))

(kind/test-last [true?])

;; ---
;; ## Layer Placement
;;
;; Where `lay-*` calls place the layer in the pose tree.
;; Five rules covering whether the call names `:x` and `:y`, whether
;; the receiver is a leaf or a composite, the raw-data convenience
;; case, and the panel the layers of a leaf share.
;;
;; **Where the columns are written:** the columns a `lay-*` call names
;; are written on the **layer's own `:mapping`**, and only there; the
;; pose's `:mapping` is what `pj/pose` writes. The two meet by scope
;; merge when the pose is drawn: a layer that names a column overrides
;; the pose's, and a layer that names none reads the pose's.

;; ### Rule LP1: bare `lay-*` attaches at the current pose's root
;;
;; A `lay-*` call without position arguments attaches the layer to
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
;; bare layer to a composite, the composite's root `:layers` holds
;; that single entry; each leaf inherits it (prepended), so every
;; sub-plot renders the layer on top of its inferred or explicit
;; leaf layers.

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
 [(fn [counts] (= [0 1] counts))])

;; ### Rule LP2: position-carrying `lay-*` attaches to the DFS-last matching leaf
;;
;; When `lay-*` carries `:x`/`:y` and at least one leaf has matching
;; effective `:x`/`:y` (after ancestor merge), the layer attaches to
;; the **last such leaf in left-to-right depth-first order**. The
;; layer's own `:mapping` carries the call's position. This is the
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

;; **Note on leaf-input with other columns.**
;; A leaf that already carries columns, called with a `lay-*` that
;; names **different** columns, keeps the layer with the columns the
;; call named written on it. The pose stays a leaf, and the layer is
;; drawn on the leaf's one panel -- see Rule LP5. The pose's own columns
;; are drawn only by layers that name none, so here only the petal
;; columns are drawn:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))

(kind/test-last
 [(fn [fr]
    (and (nil? (:poses fr))
         (= {:x :sepal-length :y :sepal-width} (:mapping fr))
         (= [{:x :petal-length :y :petal-width}] (mapv :mapping (:layers fr)))
         (= {:panels 1 :points 150}
            (select-keys (pj/svg-summary fr) [:panels :points]))))])

;; **Where the columns are written does not change the rule.** A layer
;; names its columns either in the call's argument slots or in its
;; options map, and identity reads both alike:
;; `(pj/lay-line pose :sepal-length :petal-width)` and
;; `(pj/lay-line pose {:x :sepal-length :y :petal-width})` land on the
;; same leaf. A written value is not a column -- it places a mark at
;; that value -- and the layer's data decides which of the two a value
;; is.
;;
;; Two lines over one `:x`, each naming its own `:y` in an options map,
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

;; ### Rule LP3: on a composite, position-carrying `lay-*` misses append a new leaf at root
;;
;; When `lay-*` carries `:x`/`:y` and **no** descendant leaf has
;; matching effective `:x`/`:y`, a new leaf is appended at the
;; composite's root `:poses`. It holds the one layer, and the layer's
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
;; When a `lay-*` call names columns the pose does not draw, it first
;; checks that those columns exist in the data the layer would draw
;; from -- the layer's own `:data` if present, otherwise the inherited
;; data. A missing column is almost always a typo or a column name
;; mismatch; the check raises a focused error at the call site rather
;; than deferring to a generic "column not found" at plan time.

(try
  (-> iris
      (pj/pose :sepal-length :sepal-width)
      (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg]
    (and (string? msg)
         (re-find #"doesn't exist in the data" msg)))])

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
;; | 4+ | error (pass explicit x and y) |
;;
;; The example is a dataset rather than a map literal, because the rule
;; reads an order and a Clojure map does not carry one.

(-> (tc/dataset {:height [1 2 3] :weight [4 5 6] :species ["a" "b" "a"]})
    pj/lay-point)

(kind/test-last
 [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Four or more columns without explicit arguments throws:

(try
  (-> (tc/dataset {:a [1 2] :b [3 4] :c [5 6] :d [7 8]})
      pj/lay-point)
  (catch Exception e
    (ex-message e)))

(kind/test-last
 [(fn [msg] (re-find #"Cannot auto-infer columns" msg))])

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

;; ### Rule O3: `pj/facet` writes the faceting column to the mapping
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
;; `pj/lay-rule-h`, `pj/lay-rule-v`, `pj/lay-band-h`, `pj/lay-band-v`
;; produce layers and scope like any other `lay-*`: bare call
;; attaches at root (flows to every panel); 4-arity with column
;; refs attaches to a matching leaf. Where the line or the band sits
;; is written as layer-type keys (`:y-intercept`, `:x-intercept`,
;; `:y-min`/`:y-max`, `:x-min`/`:x-max`), not as column references.

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

;; A pose-scope rule via the 4-arity attaches to a matching leaf, not
;; every panel:

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

;; ---
;; ## Assembly
;;
;; How the rules above combine to produce rendered layers. Each
;; rendered layer corresponds to one (leaf, applicable-layer)
;; pair, with all ancestor scope merged in.

;; ### Rule A1: one rendered layer per applicable (leaf, layer) pair
;;
;; For each leaf, the number of rendered layers equals the number
;; of layers applicable to that leaf -- the leaf's own plus all
;; ancestor root-origin layers.

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    pj/lay-point                            ;; root-origin; reaches both
    (pj/lay-smooth :sepal-length :sepal-width
                   {:stat :linear-model}))  ;; panel-origin; sub-pose 1 only

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
;; merge: the dataset, every mapping (positions and aesthetics)
;; that flows into it, and the layer-type bundle (plus any
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
;; grids with shared scales, and the SPLOM reshape of multi-pair poses.

;; ### Rule L1: each leaf produces a panel block
;;
;; Each leaf produces one **panel block** in the rendered plot. Unfaceted,
;; the block is one panel, which all of the leaf's layers share (Rule
;; LP5); `pj/facet` or `pj/facet-grid` multiplies it by the facet values
;; (or by the (row, col) pairs).

(-> iris
    (pj/arrange [[:sepal-length :sepal-width] [:petal-length :petal-width]])
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (let [plan (pj/plan pose)]
      (and (:composite? plan)
           (= 2 (count (:sub-plots plan))))))])

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
;; Layers applicable to a leaf (the leaf's own plus all ancestor
;; root-origin layers) draw on the same axis pair -- they overlay
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

;; ### Rule L3: faceting splits each leaf into panels by category
;;
;; `pj/facet :col` produces one panel per unique value of `:col`;
;; `pj/facet-grid :row-col :col-col` produces one panel per (row,
;; col) pair.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/facet :species))

(kind/test-last
 [(fn [pose] (= 3 (count (:panels (pj/plan pose)))))])

;; ### Rule L4: composite layout is controlled by `:layout` and optional `:share-scales`
;;
;; A composite pose carries a `:layout` map (set by `pj/arrange`
;; options: `:cols`, `:width`, `:height`, `:title`) controlling
;; the grid of its sub-poses' panel blocks. An optional
;; `:share-scales` (a subset of `#{:x :y}`, or a map from an axis to
;; `:same-column`, `:all`, or groups of columns) enables shared-scale
;; resolution across sub-poses. Under a set, sharing is column-bucketed.
;;
;; **Column-bucketing**: when `:x` is shared, sub-poses whose
;; effective `:x` column is the same share that scale's domain;
;; sub-poses with different `:x` columns get independent
;; x-domains. Same for `:y`. This is what enables SPLOM (one x down
;; each column, one y across each row) and marginal plots: a top strip
;; names the scatter's `:x` and falls in the same bucket, and a right
;; strip names the scatter's `:y` column on its own `:x`, so it falls
;; in a bucket of its own and is pinned to that column's extent there.

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
           (apply not= (x-domains (cells false))))))])

;; ### Rule L5: multi-pair `pj/pose` reshapes rectangular pairs into a 2D grid (SPLOM)
;;
;; When `pj/pose` receives a pair-sequence that forms a
;; rectangular M x N Cartesian product (like the output of
;; `pj/cross cols cols`), the result is a nested **rows-of-cols**
;; composite with `:share-scales #{:x :y}` -- the canonical SPLOM
;; layout. A cell's place follows its columns: its `:x` decides which
;; column of the grid it sits in, its `:y` which row. Each cell
;; inherits the base's `:data`, root `:mapping`, and root `:layers`
;; when rendered. The compositor applies these renderer flags on
;; cells:
;;
;; - `:suppress-legend`, `:suppress-x-label` and `:suppress-y-label`
;;   on every cell. One legend is drawn at composite level, and the
;;   strip labels carry the axis-variable names.
;; - `:suppress-x-ticks` on every cell above the bottom row, and
;;   `:suppress-y-ticks` on every cell right of the leftmost column.
;;   Every cell in a grid column carries the same `:x` and every cell
;;   in a row the same `:y`, so the bottom row's numbers describe the
;;   whole of their column and the leftmost column's the whole of
;;   their row.
;;
;; In practice, SPLOM usage therefore omits `pj/lay-point` -- each
;; cell infers its own layer type: scatter off-diagonal, histogram
;; on the diagonal (where x = y). Pair lists that are not
;; rectangular fall through to the flat one-panel-per-pair behaviour
;; (see Rules C3 / C6).

(-> iris
    (pj/cross-matrix [:sepal-length :sepal-width]
                     [:petal-length :petal-width]
                     {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= :vertical (get-in pose [:layout :direction]))
         (= #{:x :y} (get-in pose [:opts :share-scales]))
         (= 2 (count (:poses pose)))
         (every? #(= 2 (count (:poses %))) (:poses pose))
         (= {:color :species} (:mapping pose))
         ;; The row fixes y and the cells across it vary x, which is
         ;; what makes the bottom row's tick numbers true of the
         ;; column above them.
         (= [[{:x :sepal-length :y :petal-length}
              {:x :sepal-width :y :petal-length}]
             [{:x :sepal-length :y :petal-width}
              {:x :sepal-width :y :petal-width}]]
            (mapv (fn [row] (mapv :mapping (:poses row)))
                  (:poses pose)))))])

;; ---
;; ## A Note on `pj/cross`
;;
;; `pj/cross` is not a rule. It is a pure pair-generator --
;; `(for [x xs y ys] [x y])` -- returning `[x-col y-col]` pairs. It
;; has no plot-level behavior on its own; the multi-pair arity of
;; `pj/pose` (and `pj/arrange` for independent plots) is what turns
;; the generated sequence into panels, and those cases are already
;; covered by the rules above. `pj/cross` is shown as a
;; SPLOM-construction ingredient in the chart-type and how-to chapters
;; (Scatter, Faceting, Customization), not as a rule.

(pj/cross [:a :b] [:c :d])

(kind/test-last
 [(fn [pairs] (= [[:a :c] [:a :d] [:b :c] [:b :d]] pairs))])

;; ## What's Next
;;
;; - [**Inference Rules**](./plotje_book.inference_rules.html) --
;;   how Plotje fills in defaults (column types, marks, stats,
;;   scales) when you do not specify them
;; - [**Layer Types**](./plotje_book.layer_types.html) -- the
;;   registry of mark + stat + position combinations
