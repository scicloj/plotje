(ns scicloj.plotje.impl.coord
  (:require [tech.v3.datatype :as dtype]
            [scicloj.plotje.impl.scale :as scale]))

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

;; ---- Column-wise projection ----
;;
;; The same mappings over whole columns rather than one point at a time.
;; They live beside the scalar ones so a coordinate system states its
;; projection once: a vectorized version written anywhere else would be a
;; second copy to keep in step.

(defmulti make-coord-columns
  "Build a column-wise coordinate function: (coord xs ys) -> [pxs pys],
   over readers rather than one point at a time. The results are lazy
   readers, so nothing is allocated until they are read or realized."
  (fn [coord-type sx sy pw ph m] coord-type))

(defmethod make-coord-columns :default [coord-type sx sy pw ph m]
  ;; Anything with no column-wise method of its own still works, one
  ;; point at a time. :polar takes this path on purpose: its projection
  ;; folds x and y together through `polar-project`, and restating that
  ;; arithmetic in column form would be a second copy of it.
  (let [f (make-coord coord-type sx sy pw ph m)]
    (fn [xs ys]
      (let [n (count xs)
            pxs (double-array n)
            pys (double-array n)]
        (dotimes [i n]
          (let [[px py] (f (xs i) (ys i))]
            (aset pxs i (double px))
            (aset pys i (double py))))
        [pxs pys]))))

(defmethod make-coord-columns :cartesian [_ sx sy _ _ _]
  (fn [xs ys] [(dtype/emap sx :float64 xs) (dtype/emap sy :float64 ys)]))

(defmethod make-coord-columns :fixed [_ sx sy _ _ _]
  (fn [xs ys] [(dtype/emap sx :float64 xs) (dtype/emap sy :float64 ys)]))

(defmethod make-coord-columns :flip [_ sx sy _ _ _]
  (fn [xs ys] [(dtype/emap sx :float64 ys) (dtype/emap sy :float64 xs)]))

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

(defmulti make-inverse-columns
  "The column-wise inverse: (inverse pxs pys) -> [xs ys]. Returns nil for
   a coordinate system with no coordinate-by-coordinate inverse, matching
   `make-inverse`.

   `x-type` and `y-type` are the datatypes the two axes answer in, which
   the caller knows from their scales: `:float64` for a continuous axis,
   `:object` for a band scale, whose inverse is the category holding the
   position, or nil outside every band. Reading a continuous axis back
   through an object column would box every value."
  (fn [coord-type sx sy pw ph m x-type y-type] coord-type))

(defmethod make-inverse-columns :default [coord-type sx sy pw ph m _ _]
  (when-let [f (make-inverse coord-type sx sy pw ph m)]
    (fn [pxs pys]
      (let [n (count pxs)
            xs (object-array n)
            ys (object-array n)]
        (dotimes [i n]
          (let [[x y] (f (pxs i) (pys i))]
            (aset xs i x)
            (aset ys i y)))
        [xs ys]))))

(defn- invert-column
  "One drawing-space column read back through `sc`, into a buffer of
   `datatype`. A loop rather than `dtype/emap`: `wadogo`'s inverse is a
   scalar call that dominates either way, and measured over 100k values
   the loop runs in 0.25 ms against 0.57 for the mapped reader."
  [sc datatype column]
  (let [n (count column)]
    (if (identical? :float64 datatype)
      (let [out (double-array n)]
        (dotimes [i n] (aset out i (double (scale/invert sc (column i)))))
        out)
      (let [out (object-array n)]
        (dotimes [i n] (aset out i (scale/invert sc (column i))))
        out))))

(defmethod make-inverse-columns :cartesian [_ sx sy _ _ _ x-type y-type]
  (fn [pxs pys] [(invert-column sx x-type pxs) (invert-column sy y-type pys)]))

(defmethod make-inverse-columns :fixed [_ sx sy _ _ _ x-type y-type]
  (fn [pxs pys] [(invert-column sx x-type pxs) (invert-column sy y-type pys)]))

(defmethod make-inverse-columns :flip [_ sx sy _ _ _ x-type y-type]
  ;; :flip reads data x back off the y scale, so the datatypes cross too.
  (fn [pxs pys] [(invert-column sy x-type pys) (invert-column sx y-type pxs)]))

;; ---- Tick visibility ----

(defmulti show-ticks?
  "Whether to show tick labels for this coordinate system."
  (fn [coord-type] coord-type))

(defmethod show-ticks? :default [_] true)

(defmethod show-ticks? :fixed [_] true)
(defmethod show-ticks? :polar [_] false)
