(ns scicloj.plotje.impl.membrane
  "Defines `PlotjeMembrane` -- the value type returned by the
   membrane stage of the pipeline.

   A `PlotjeMembrane` is a Membrane UI component implementing
   `IOrigin`, `IBounds`, and `IChildren`, so it composes naturally
   with other Membrane components and renders through any Membrane
   backend without special-casing.

   Fields:

   - `:drawables`     a vector of `membrane.ui` drawing primitives
   - `:width`         canvas width in drawing units
   - `:height`        canvas height in drawing units

   Plus optional namespaced attributes carried as map entries:

   - `:plotje/title`  the plot title, or absent when unset

   Future per-membrane attributes (subtitle, caption, ...) are added
   as `:plotje/*` keys via `assoc`, not as new record fields, so the
   record arity stays stable.

   This namespace requires `[membrane.ui]`. Other `impl/` namespaces
   keep that dependency at arm's length, but the membrane stage's
   value type cannot avoid it -- a `PlotjeMembrane` is, by
   definition, a Membrane component."
  (:require [malli.core :as m]
            [membrane.ui :as ui]))

(defrecord PlotjeMembrane [drawables width height]
  ui/IOrigin
  (-origin [_] [0 0])
  ui/IBounds
  (-bounds [_] [width height])
  ui/IChildren
  (-children [_] drawables))

(defn membrane?
  "True if x is a `PlotjeMembrane` -- the canonical value returned by
   `pj/plan->membrane` and `pj/membrane`."
  [x]
  (instance? PlotjeMembrane x))

(defn- drawable-element?
  "True if `x` is a Membrane element in its own right.

   `membrane.ui` extends `IOrigin` to vectors and to nil, treating a
   vector as a group of drawables, so those two answer true whatever
   they hold and cannot settle the question on their own."
  [x]
  (and (some? x)
       (not (vector? x))
       (satisfies? ui/IOrigin x)))

(defn membrane-tree?
  "True if x looks like a Membrane drawable tree -- either a
   `PlotjeMembrane` record (the canonical Plotje shape) or a non-empty
   vector that bottoms out in a Membrane element (a hand-built drawable
   tree, whose groups are vectors).

   The leftmost branch is followed down to its first non-vector, because
   asking `IOrigin` of the vector itself answers true for `[[1 2] [3 4]]`
   -- a pair of data rows, which `pj/pose` reads as a dataset.

   Used by Plotje's input-validation gate to detect when a user has
   accidentally piped a rendered membrane back into a function that
   expects a pose, so the error can advise calling `pj/membrane->plot`
   instead. Rejection diagnostic only -- not used to accept input, so
   where it cannot tell, answering false leaves the value to the checks
   that follow."
  [x]
  (or (membrane? x)
      (boolean
       (and (vector? x)
            (seq x)
            (loop [head (first x)]
              (if (vector? head)
                (if (seq head)
                  (recur (first head))
                  false)
                (drawable-element? head)))))))

(def PlotjeMembraneSchema
  "Malli schema for the `PlotjeMembrane` record, validated as a map.

   Backend authors writing their own `membrane->plot` defmethod read
   width/height via `(membrane.ui/width m)`/`(membrane.ui/height m)`
   (derived from `IBounds`) and title via `(:plotje/title m)`."
  [:map
   [:drawables [:sequential any?]]
   [:width pos-int?]
   [:height pos-int?]
   [:plotje/title {:optional true} [:maybe string?]]])

(defn valid?
  "True if x conforms to `PlotjeMembraneSchema`."
  [x]
  (m/validate PlotjeMembraneSchema x))

(defn explain
  "Explain why x does not conform to `PlotjeMembraneSchema`. Returns
   nil if valid, or a Malli explanation map if invalid."
  [x]
  (m/explain PlotjeMembraneSchema x))
