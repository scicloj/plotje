;; # Pose Rules
;;
;; Poses gave the mental picture; this chapter proves it. Each of
;; the 29 rules below carries a rendered pose and a tested
;; assertion, with the printed structure shown where the shape is
;; the point, so the model claims are verified on every run.
;;
;; The rules are organized into seven sections (Construction, Layer
;; Placement, Leaf Identity, Scope, Options, Assembly, Layout) and
;; cover every call shape of `pj/pose`, `pj/lay-*`, `pj/arrange`,
;; `pj/options`, `pj/scale`, `pj/coord`, `pj/facet`, and `pj/cross`.
;;
;; Read [Poses](./plotje_book.pose_model.html) first -- this
;; chapter is the proof layer, not a teaching chapter.

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

;; ## Overview
;;
;; A **pose** is a plain Clojure map with a documented set of keys:
;;
;; | Key | On | Purpose |
;; |:----|:---|:--------|
;; | `:data` | leaf or any ancestor | dataset (tablecloth) |
;; | `:mapping` | pose or layer | column-to-aesthetic bindings |
;; | `:layers` | pose | per-scope layers |
;; | `:poses` | composite only | sub-poses |
;; | `:layout` | composite | direction + weights |
;; | `:opts` | root | plot-level options (incl. composite-level keys like `:share-scales`) |
;;
;; A **leaf pose** has `:data`, `:mapping`, `:layers`; no `:poses`.
;; A **composite pose** has `:poses`; sub-poses can be leaves or
;; further composites. A **layer** is a map with `:layer-type` and an
;; optional `:mapping`, plus sibling keys `:stat`, `:position`,
;; `:mark` when the user provides them.
;;
;; The rules below assume some familiarity with these shapes. If this
;; is new, [Poses](./plotje_book.pose_model.html) shows
;; them in use before we formalize them here.

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

;; ### Rule C3: `pj/pose` with position on a positioned leaf promotes to a composite
;;
;; When `pj/pose` is called with position (`:x`/`:y`) on a leaf
;; that already has position, the leaf becomes sub-pose 1 of a new
;; composite and the call becomes sub-pose 2. If the leaf carried
;; aesthetic alongside position, the aesthetic moves to the new
;; composite's **root** `:mapping` and flows to every sub-pose; the
;; position stays with sub-pose 1.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width))

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (:poses pose)))
         (= {:x :sepal-length :y :sepal-width}
            (:mapping (first (:poses pose))))
         (= {:x :petal-length :y :petal-width}
            (:mapping (second (:poses pose))))))])

;; When the leaf carried aesthetic + position, promotion splits
;; them -- aesthetic goes to root (flows to both panels), position
;; stays with sub-pose 1:

(-> iris
    (pj/pose :sepal-length :sepal-width {:color :species})
    (pj/pose :petal-length :petal-width))

(-> iris
    (pj/pose :sepal-length :sepal-width {:color :species})
    (pj/pose :petal-length :petal-width)
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (= {:x :sepal-length :y :sepal-width}
            (:mapping (first (:poses pose))))
         (= {:x :petal-length :y :petal-width}
            (:mapping (second (:poses pose))))))])

;; **Property P-C3 -- plot-level options stay at root on promotion.**
;; A `:title` set via `pj/options` before promotion does not demote
;; into sub-pose 1; it lives on the composite root's `:opts`.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/options {:title "Iris"})
    (pj/pose :petal-length :petal-width))

;; The printed structure shows `:opts {:title "Iris"}` at root and no
;; `:opts` on sub-pose 1:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/options {:title "Iris"})
    (pj/pose :petal-length :petal-width)
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= "Iris" (get-in pose [:opts :title]))
         (not (contains? (first (:poses pose)) :opts))))])

;; ### Rule C4: aesthetic-only `pj/pose` on a positioned leaf promotes without adding a panel
;;
;; An aesthetic-only call (no `:x`/`:y`) on a positioned leaf
;; wraps the leaf as sub-pose 1 of a new composite and routes the
;; aesthetic to the composite's root `:mapping`. The composite ends
;; up with exactly **one** sub-pose. The purpose is to position the
;; aesthetic at plot scope ahead of any subsequent panel.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= 1 (count (:poses pose)))
         (= {:color :species} (:mapping pose))
         (= {:x :sepal-length :y :sepal-width}
            (:mapping (first (:poses pose))))))])

;; **Property P-C4 -- aesthetic-then-panel equivalence.**
;; Aesthetic-only promotion followed by a position call equals
;; bundling the aesthetic on the initial leaf and then promoting
;; (C3's mapping-split path). Users can switch between the two
;; forms without changing the result.

(= (-> iris
       (pj/pose :sepal-length :sepal-width)
       (pj/pose {:color :species})
       (pj/pose :petal-length :petal-width))
   (-> iris
       (pj/pose :sepal-length :sepal-width {:color :species})
       (pj/pose :petal-length :petal-width)))

(kind/test-last [true?])

;; ### Rule C5: layer partitioning at promotion splits layers by position presence
;;
;; When a positioned leaf is promoted (via C3 or C4), each layer
;; is partitioned by a single test: a layer whose own `:mapping`
;; contains `:x` or `:y` is **panel-origin** and stays with
;; sub-pose 1; otherwise it is **root-origin** and moves to the
;; composite's root `:layers`, flowing to every sub-pose at plan
;; time. No whitelist; the layer's own mapping is self-describing.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/pose :petal-length :petal-width))

(kind/test-last
 [(fn [pose]
    (and (= 1 (count (:layers pose)))
         (= :point (:layer-type (first (:layers pose))))
         (= 2 (count (:poses pose)))
         (= [] (:layers (first (:poses pose))))
         (= [] (:layers (second (:poses pose))))))])

;; The bare `pj/lay-point` call is root-origin: at render time it
;; reaches both panels. Had we passed position --
;; `(pj/lay-point :sepal-length :sepal-width)` -- the layer would
;; stay with sub-pose 1.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width))

(kind/test-last
 [(fn [pose]
    (and (or (not (contains? pose :layers))
             (= [] (:layers pose)))
         (= 1 (count (:layers (first (:poses pose)))))
         (= [] (:layers (second (:poses pose))))))])

;; ### Rule C6: `pj/pose` on a composite dispatches by call shape
;;
;; Already a composite? The dispatch is:
;;
;; - **Position-carrying call** -- append a new sub-pose to `:poses`.
;; - **Aesthetic-only call** -- merge the aesthetic into the root
;;   `:mapping` (no new sub-pose).
;; - **Empty call** -- no-op (see C7).

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/pose :sepal-length :petal-length))

(kind/test-last
 [(fn [pose]
    (and (= 3 (count (:poses pose)))
         (= [{:x :sepal-length :y :sepal-width}
             {:x :petal-length :y :petal-width}
             {:x :sepal-length :y :petal-length}]
            (mapv :mapping (:poses pose)))))])

;; An aesthetic-only call on a composite merges into root, leaving
;; sub-poses alone:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/pose {:color :species}))

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/pose {:color :species})
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (:poses pose)))
         (= {:color :species} (:mapping pose))
         (= {:x :sepal-length :y :sepal-width}
            (:mapping (first (:poses pose))))))])

;; ### Rule C7: empty `pj/pose` on an existing pose is a no-op
;;
;; `(pj/pose pose)` where `pose` is a leaf or a composite returns `pose`
;; unchanged. This makes the 1-arity `pj/pose` safe as a syntactic
;; nullity.

;; A leaf pose. First the rendered plot, then its structure:

(def leaf-pose (-> iris (pj/pose :sepal-length :sepal-width)))

leaf-pose

(pose-summary leaf-pose)

;; Wrapping it with 1-arity `pj/pose` returns the same value:

(= leaf-pose (pj/pose leaf-pose))

(kind/test-last [true?])

;; A composite pose. The rendered plot, then its structure:

(def composite-pose
  (-> iris
      (pj/pose :sepal-length :sepal-width)
      (pj/pose :petal-length :petal-width)))

composite-pose

(pose-summary composite-pose)

;; And again, 1-arity `pj/pose` is a no-op:

(= composite-pose (pj/pose composite-pose))

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

;; Opts (`:cols`, `:title`, `:width`, `:height`, `:share-scales`)
;; route into the composite's `:opts` / `:layout`:

(pj/arrange
 [(pj/pose iris :sepal-length :sepal-width)
  (pj/pose iris :petal-length :petal-width)]
 {:title "Arranged"
  :share-scales #{:y}})

(kind/test-last
 [(fn [pose]
    (and (= "Arranged" (get-in pose [:opts :title]))
         (= #{:y} (get-in pose [:opts :share-scales]))))])

;; ### Rule C9: 3-arity `pj/pose` with a pair sequence and opts folds aesthetic-attach into multi-pair
;;
;; When `pj/pose` receives three arguments where the second is a
;; pair sequence (a vector of `[x y]` pairs, typically the output
;; of `pj/cross`) and the third is an opts map, the call is
;; equivalent to two chained calls: the opts mapping is attached to
;; the base first (Rule C2 or C4), then the pair sequence is
;; processed on top (Rule L5 for rectangular grids, otherwise a
;; flat one-panel-per-pair composite). The aesthetic lands on the
;; composite root and flows to every cell.

(-> iris
    (pj/pose (pj/cross [:sepal-length :sepal-width]
                       [:petal-length :petal-width])
             {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= {:color :species} (:mapping pose))
         (= 2 (count (:poses pose)))
         (every? #(= 2 (count (:poses %))) (:poses pose))))])

;; **Property P-C9 -- multi-pair fold.** The 3-arity form is
;; structurally equal to the two-call form.

(let [a (-> iris
            (pj/pose {:color :species})
            (pj/pose (pj/cross [:sepal-length :sepal-width]
                               [:petal-length :petal-width])))
      b (-> iris
            (pj/pose (pj/cross [:sepal-length :sepal-width]
                               [:petal-length :petal-width])
                     {:color :species}))]
  (= a b))

(kind/test-last [true?])

;; ---
;; ## Layer Placement
;;
;; Where `lay-*` calls place the layer in the pose tree.
;; Four rules covering the bare-vs-position and leaf-vs-composite
;; combinations, plus the raw-data convenience case.
;;
;; **Position storage (ratified 2026-04-23):** when a `lay-*` call
;; carries position, the position lives on the **layer's own
;; `:mapping`**. The leaf being attached to (or created for) also
;; carries position in its own `:mapping` where appropriate -- both
;; resolve to the same effective `:x`/`:y` via scope merge. The
;; layer's own `:mapping` is the authoritative record of what the
;; user typed and is what C5 inspects at promotion.

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
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/lay-point :sepal-length :sepal-width))

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (:poses pose)))
         (= 1 (count (:layers (first (:poses pose)))))
         (= 0 (count (:layers (second (:poses pose)))))
         (= :point
            (:layer-type (first (:layers (first (:poses pose))))))))])

;; **Note on leaf-input with non-matching position.**
;; A leaf that already carries position, called with a `lay-*` that
;; carries a **different** position, is **promoted** into a 2-panel
;; composite. The original leaf's layers stay with panel-1; the new
;; sub-pose carries the call's position and the new layer. This is
;; the symmetric counterpart of LP3 below: distinct positional
;; aesthetics mean distinct poses, whether the receiver is a leaf
;; or a composite.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :petal-length :petal-width))

(kind/test-last
 [(fn [fr]
    (and (= 2 (count (:poses fr)))
         (= {:x :sepal-length :y :sepal-width}
            (:mapping (first (:poses fr))))
         (= {:x :petal-length :y :petal-width}
            (:mapping (second (:poses fr))))))])

;; ### Rule LP3: on a composite, position-carrying `lay-*` misses append a new leaf at root
;;
;; When `lay-*` carries `:x`/`:y` and **no** descendant leaf has
;; matching effective `:x`/`:y`, a new leaf is appended at the
;; composite's root `:poses`. Its `:mapping` carries the call's
;; position; a single layer with matching position attaches to it.
;; The same rule applies to leaf input (see LP2 above) -- a leaf
;; with non-matching position is promoted to a composite first.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/lay-point :sepal-length :petal-length))

(kind/test-last
 [(fn [pose]
    (and (= 3 (count (:poses pose)))
         (= {:x :sepal-length :y :petal-length}
            (:mapping (nth (:poses pose) 2)))
         (= 1 (count (:layers (nth (:poses pose) 2))))))])

;; **Property: LP2 and LP3 produce the same panel structure.**
;; Promoting via `lay-*` (LP2) and building the composite explicitly
;; via two `pj/pose` calls then attaching layers (LP3) produce the
;; same sub-pose positions in the same order. The layer-attachment
;; details differ (the LP2 path stamps the original leaf's position
;; on its pre-existing layers so they stay with panel-1), but the
;; panel-level structure is the same.

(let [via-lay  (-> iris
                   (pj/lay-point :sepal-length :sepal-width)
                   (pj/lay-point :petal-length :petal-width))
      via-pose (-> iris
                   (pj/pose :sepal-length :sepal-width)
                   (pj/pose :petal-length :petal-width)
                   (pj/lay-point :sepal-length :sepal-width)
                   (pj/lay-point :petal-length :petal-width))]
  {:via-lay-mappings  (mapv :mapping (:poses via-lay))
   :via-pose-mappings (mapv :mapping (:poses via-pose))})

(kind/test-last
 [(fn [m] (= (:via-lay-mappings m) (:via-pose-mappings m)))])

;; **Property: column-existence safety check on the new sub-pose.**
;; When LP2 or LP3 would create a new sub-pose for a non-matching
;; position, `lay-*` first checks that the new position columns
;; exist in the data the sub-pose would use -- the layer's own
;; `:data` if present, otherwise the inherited data. A missing
;; column is almost always a typo or a column name mismatch; the
;; check raises a focused error at the call site rather than
;; deferring to a generic "column not found" at plan time.

(try
  (-> iris
      (pj/pose :sepal-length :sepal-width)
      (pj/lay-point :nope :nada))
  (catch clojure.lang.ExceptionInfo e
    (ex-message e)))

(kind/test-last
 [(fn [msg]
    (and (string? msg)
         (re-find #"doesn't exist in the data" msg)
         (re-find #"new sub-pose" msg)))])

;; Supplying `:data` on the lay-* call satisfies the safety check
;; -- the new sub-pose has its own data with the new columns.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/lay-point :foo :bar
                  {:data (tc/dataset {:foo [1 2 3] :bar [4 5 6]})}))

(kind/test-last
 [(fn [fr]
    (and (= 2 (count (:poses fr)))
         (= {:x :foo :y :bar}
            (:mapping (second (:poses fr))))))])

;; ### Rule LP4: `lay-*` on raw data coerces the data into a leaf pose
;;
;; `lay-*` called with a dataset as its first argument coerces the
;; data into a leaf pose first, then applies the layer. The result
;; is a leaf pose equivalent to
;; `(-> data (pj/pose :x :y) pj/lay-point)`. This keeps the
;; convenience one-liner `(-> data (pj/lay-point :x :y))` working as
;; the shortest path from data to plot.

(def tiny
  {:a [1 2 3 4 5]
   :b [2 4 3 5 4]})

(-> tiny
    (pj/lay-point :a :b))

(kind/test-last [(fn [v] (= 5 (:points (pj/svg-summary v))))])

;; The explicit two-step form produces the same leaf pose:

(-> tiny
    (pj/pose :a :b)
    pj/lay-point
    pose-summary)

(kind/test-last
 [(fn [pose]
    (and (= {:x :a :y :b} (:mapping pose))
         (= 1 (count (:layers pose)))
         (not (contains? pose :poses))))])

;; ---
;; ## Leaf Identity
;;
;; How columns identify a leaf. One rule, about inference when the
;; user omits column names. (How column references are compared is
;; covered by Rule LP2.)

;; ### Rule LI1: few-column datasets auto-infer columns by position
;;
;; When `lay-*` or `pj/pose` is called on a dataset without
;; explicit column arguments, columns are inferred:
;;
;; | Columns | Inferred mapping |
;; |:--------|:-----------------|
;; | 1 | `{:x col0}` |
;; | 2 | `{:x col0 :y col1}` |
;; | 3 | `{:x col0 :y col1 :color col2}` |
;; | 4+ | error (pass explicit x and y) |

(-> {:height [1 2 3] :weight [4 5 6] :species ["a" "b" "a"]}
    pj/lay-point)

(kind/test-last
 [(fn [v] (= 3 (:points (pj/svg-summary v))))])

;; Four or more columns without explicit arguments throws:

(try
  (-> {:a [1 2] :b [3 4] :c [5 6] :d [7 8]}
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
;; even on the same leaf -- do not see it. This is the terminal
;; case of S1: the layer's mapping is innermost in the merge.

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

;; ### Rule O2: `pj/scale` and `pj/coord` are plot-level options
;;
;; `pj/scale` writes into `:opts` under one of `:x-scale`,
;; `:y-scale`, `:size-scale`, `:alpha-scale`, `:fill-scale`, or
;; `:color-scale` -- one key per channel. Axis channels (`:x`,
;; `:y`) accept `:linear`, `:log`, `:categorical`; visual channels
;; (`:size`, `:alpha`, `:fill`, `:color`) accept `:linear` and
;; `:log`. `pj/coord` writes `:coord`. They apply to every leaf in
;; the tree uniformly. (Per-panel scale variation is an open
;; design question; today all are plot-wide.)

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/scale :x :log)
    (pj/coord :flip))

(kind/test-last
 [(fn [pose]
    (and (= {:type :log} (get-in pose [:opts :x-scale]))
         (= :flip (get-in pose [:opts :coord]))))])

;; A visual channel routes to its own opts key:

(-> iris
    (pj/pose :sepal-length :sepal-width {:size :petal-length})
    pj/lay-point
    (pj/scale :size :log))

(kind/test-last
 [(fn [pose]
    (= {:type :log} (get-in pose [:opts :size-scale])))])

;; ### Rule O3: `pj/facet` writes the faceting column to `:opts`
;;
;; `pj/facet` and `pj/facet-grid` store facet columns in `:opts`
;; as `:facet-col` (and `:facet-row` for a grid). The layout
;; effect -- splitting each leaf's panel into a group of panels --
;; happens at render time.

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/facet :species))

(kind/test-last
 [(fn [pose] (= :species (get-in pose [:opts :facet-col])))])

;; A 2D grid uses both keys:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    pj/lay-point
    (pj/facet-grid :species :species))

(kind/test-last
 [(fn [pose]
    (and (= :species (get-in pose [:opts :facet-col]))
         (= :species (get-in pose [:opts :facet-row]))))])

;; ### Rule O4: `pj/lay-rule-*` and `pj/lay-band-*` are layers (annotations)
;;
;; `pj/lay-rule-h`, `pj/lay-rule-v`, `pj/lay-band-h`, `pj/lay-band-v`
;; produce layers and scope like any other `lay-*`: bare call
;; attaches at root (flows to every panel); 4-arity with column
;; refs attaches to a matching leaf. Position rides as layer-type
;; keys (`:y-intercept`, `:x-intercept`, `:y-min`/`:y-max`,
;; `:x-min`/`:x-max`), not column refs.

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

;; A pose-scope annotation via the 4-arity attaches to a matching
;; leaf, not every panel:

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    (pj/lay-rule-h :sepal-length :sepal-width {:y-intercept 3.0}))

(kind/test-last
 [(fn [pose]
    (and (= 2 (count (:poses pose)))
         (= 1 (count (:layers (first (:poses pose)))))
         (= 0 (count (:layers (second (:poses pose)))))
         (= :rule-h (:layer-type
                     (first (:layers (first (:poses pose))))))))])

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
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
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
;; covering single panels, overlays, faceting, composite grids with
;; shared scales, and the SPLOM reshape of multi-pair poses.

;; ### Rule L1: each leaf produces a panel block
;;
;; Each leaf produces one **panel block** in the rendered plot.
;; Without faceting, the block contains one panel. With `pj/facet`
;; or `pj/facet-grid`, the block contains one panel per facet value
;; (or per (row, col) pair).

(-> iris
    (pj/pose :sepal-length :sepal-width)
    (pj/pose :petal-length :petal-width)
    pj/lay-point)

(kind/test-last
 [(fn [pose]
    (let [plan (pj/plan pose)]
      (and (:composite? plan)
           (= 2 (count (:sub-plots plan))))))])

;; ### Rule L2: layers within one leaf overlay within that leaf's panel block
;;
;; All layers applicable to a leaf (the leaf's own plus all
;; ancestor root-origin layers) draw on the same axis pair -- they
;; overlay within each panel of that leaf's block, not on separate
;; panels.

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
;; `:share-scales` (subset of `#{:x :y}`) enables column-bucketed
;; shared-scale resolution across sub-poses.
;;
;; **Column-bucketing**: when `:x` is shared, sub-poses whose
;; effective `:x` column is the same share that scale's domain;
;; sub-poses with different `:x` columns get independent
;; x-domains. Same for `:y`. This is what enables SPLOM (aligning
;; columns down, rows across) and marginal plots (x shared between
;; scatter and top density; right density has its own y).

(def l4-shared
  (pj/arrange
   [(-> iris (pj/pose :sepal-length :sepal-width) pj/lay-point)
    (-> iris (pj/pose :sepal-length :petal-width) pj/lay-point)]
   {:share-scales #{:x}}))

l4-shared

(kind/test-last
 [(fn [pose]
    (let [sub-plots (:sub-plots (pj/plan pose))
          domains (mapv #(get-in % [:plan :panels 0 :x-scale :domain]) sub-plots)]
      ;; Both panels share :sepal-length as x -> same x-domain
      (and (= 2 (count domains))
           (= (first domains) (second domains)))))])

;; ### Rule L5: multi-pair `pj/pose` reshapes rectangular pairs into a 2D grid (SPLOM)
;;
;; When `pj/pose` receives a pair-sequence that forms a
;; rectangular M x N Cartesian product (like the output of
;; `pj/cross cols cols`), the result is a nested **rows-of-cols**
;; composite with `:share-scales #{:x :y}` -- the canonical SPLOM
;; layout. Each cell inherits the base's `:data`, root `:mapping`,
;; and root `:layers` when rendered. The compositor applies three
;; renderer flags on cells:
;;
;; - `:suppress-legend true` on every cell (one shared legend is
;;   drawn at composite level).
;; - `:suppress-x-label true` on every non-bottom row (x-axis label
;;   shows only on the bottom row).
;; - `:suppress-y-label true` on every non-leftmost column (y-axis
;;   label shows only on the leftmost column).
;;
;; Idiomatic SPLOM usage therefore omits `pj/lay-point` -- each
;; cell infers its own layer type: scatter off-diagonal, histogram
;; on the diagonal (where x = y). Pair lists that are not
;; rectangular fall through to the flat one-panel-per-pair behaviour
;; (see Rules C3 / C6).

(-> iris
    (pj/pose (pj/cross [:sepal-length :sepal-width]
                       [:petal-length :petal-width])
             {:color :species}))

(kind/test-last
 [(fn [pose]
    (and (= :vertical (get-in pose [:layout :direction]))
         (= #{:x :y} (get-in pose [:opts :share-scales]))
         (= 2 (count (:poses pose)))
         (every? #(= 2 (count (:poses %))) (:poses pose))
         (= {:color :species} (:mapping pose))))])

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
;;   registry of mark + stat + position combinations the rules
;;   above orchestrate
