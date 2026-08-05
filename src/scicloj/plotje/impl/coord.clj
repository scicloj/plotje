(ns scicloj.plotje.impl.coord
  (:require [scicloj.plotje.impl.scale :as scale]))

(defn- polar-project
  "Project pixel-space (px, py) to polar coordinates.
   Shared core for make-coord :polar and make-coord-px :polar."
  [cx cy r-max x-lo x-span y-lo y-span px py]
  (let [t-angle (/ (- px x-lo) (max 1.0 x-span))
        t-radius (/ (- (+ y-lo y-span) py) (max 1.0 y-span))
        angle (* 2.0 Math/PI t-angle)
        radius (* r-max t-radius)]
    [(+ cx (* radius (Math/cos (- angle (/ Math/PI 2.0)))))
     (+ cy (* radius (Math/sin (- angle (/ Math/PI 2.0)))))]))

(defmulti make-coord
  "Build a coordinate function: (coord data-x data-y) -> [pixel-x pixel-y]."
  (fn [coord-type sx sy pw ph m] coord-type))

(defmethod make-coord :cartesian [_ sx sy pw ph m]
  (fn [dx dy] [(sx dx) (sy dy)]))

(defmethod make-coord :fixed [_ sx sy pw ph m]
  (fn [dx dy] [(sx dx) (sy dy)]))

(defmethod make-coord :flip [_ sx sy pw ph m]
  (fn [dx dy] [(sx dy) (sy dx)]))

(defmethod make-coord :polar [_ sx sy pw ph m]
  (let [cx (/ pw 2.0) cy (/ ph 2.0)
        r-max (max 1.0 (- (min cx cy) m))
        x-lo (double m) x-span (double (- pw m m))
        y-lo (double m) y-span (double (- ph m m))]
    (fn [dx dy]
      (polar-project cx cy r-max x-lo x-span y-lo y-span (sx dx) (sy dy)))))

(defmethod make-coord [:cartesian :doc] [_ _ _ _ _ _] "Standard x-right, y-up mapping")
(defmethod make-coord [:fixed :doc] [_ _ _ _ _ _] "Fixed aspect ratio (1 data unit = 1 data unit)")
(defmethod make-coord [:flip :doc] [_ _ _ _ _ _] "Swap x and y axes")
(defmethod make-coord [:polar :doc] [_ _ _ _ _ _] "Radial mapping: x→angle, y→radius")

(defmethod make-coord :default [coord-type _ _ _ _ _]
  (throw (ex-info (str "Unknown coord type: " (pr-str coord-type)
                       ". Supported: " (vec (sort (filter keyword?
                                                          (remove #(or (vector? %) (= :default %))
                                                                  (keys (methods make-coord)))))))
                  {:coord-type coord-type})))

;; ---- Pixel-space reprojection (for arc interpolation) ----

(defmulti make-coord-px
  "Build a pixel-space reprojection function for coordinate systems that need
   arc interpolation (e.g. polar). Returns nil for systems where bars can be
   drawn as simple rectangles."
  (fn [coord-type sx sy pw ph m] coord-type))

(defmethod make-coord-px :default [_ _ _ _ _ _] nil)

(defmethod make-coord-px :polar [_ sx sy pw ph m]
  (let [cx (/ pw 2.0) cy (/ ph 2.0)
        r-max (max 1.0 (- (min cx cy) m))
        x-lo (double m) x-span (double (- pw m m))
        y-lo (double m) y-span (double (- ph m m))]
    (fn [px py]
      (polar-project cx cy r-max x-lo x-span y-lo y-span px py))))

;; ---- Inverse: drawing space back to data space ----

(defmulti make-inverse
  "Build the inverse of `make-coord`: (inverse panel-x panel-y) -> [data-x
   data-y], where the pixel arguments are relative to the panel box's own
   origin. Returns nil for a coordinate system with no coordinate-by-
   coordinate inverse.

   Interaction reads this direction: a brush reporting a data range, a
   crosshair reading an axis, a hit test."
  (fn [coord-type sx sy pw ph m] coord-type))

(defmethod make-inverse :default [_ _ _ _ _ _] nil)

(defmethod make-inverse :cartesian [_ sx sy _ _ _]
  (fn [px py] [(scale/invert sx px) (scale/invert sy py)]))

(defmethod make-inverse :fixed [_ sx sy _ _ _]
  (fn [px py] [(scale/invert sx px) (scale/invert sy py)]))

(defmethod make-inverse :flip [_ sx sy _ _ _]
  ;; :flip draws data x through sy and data y through sx, so the inverse
  ;; reads them back the same way round.
  (fn [px py] [(scale/invert sy py) (scale/invert sx px)]))

(defn invertible?
  "Whether `coord-type` has an inverse, i.e. a method of its own rather
   than the nil-returning default."
  [coord-type]
  (not= (get-method make-inverse coord-type)
        (get-method make-inverse :default)))

;; :polar has no method on purpose. Its projection folds x and y together
;; through an angle and a radius; inverting it would answer with a point
;; that is only sometimes the one asked about.

;; ---- Tick visibility ----

(defmulti show-ticks?
  "Whether to show tick labels for this coordinate system."
  (fn [coord-type] coord-type))

(defmethod show-ticks? :default [_] true)

(defmethod show-ticks? :fixed [_] true)
(defmethod show-ticks? :polar [_] false)
