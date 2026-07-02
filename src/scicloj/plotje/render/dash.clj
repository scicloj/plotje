(ns scicloj.plotje.render.dash
  "A membrane drawable wrapper that applies a stroke dash pattern to the
   strokes of its children. Membrane has no built-in dash primitive, so
   this record carries a `[dash gap ...]` pixel pattern that the SVG and
   Java2D backends each read (see render.svg and render.bufimg).

   Mirrors membrane.ui.WithStrokeWidth so the compositor can measure and
   walk it like any other drawable."
  (:require [membrane.ui :as ui]))

(defrecord WithStrokeDash [dash drawables]
  ui/IOrigin
  (-origin [_] [0 0])

  ui/IBounds
  (-bounds [_] (ui/bounds drawables))

  ui/IChildren
  (-children [_] drawables)

  ui/IMakeNode
  (make-node [_ childs] (WithStrokeDash. dash childs)))

(defn with-stroke-dash
  "Wrap drawables so their strokes render with the given `[dash gap ...]`
   pixel pattern. Place OUTSIDE with-stroke-width so the Java2D backend
   sets the dash first and the inner width merge preserves it."
  [dash & drawables]
  (WithStrokeDash. dash (vec drawables)))
