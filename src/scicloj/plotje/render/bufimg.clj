(ns scicloj.plotje.render.bufimg
  "Render membrane drawable trees to java.awt.image.BufferedImage via
   membrane's Java2D backend. Faster than SVG for large plots and
   produces raster output that Clay renders automatically."
  (:require [membrane.java2d :as java2d]
            [membrane.ui :as ui]
            [scicloj.plotje.render.membrane :as membrane]
            [scicloj.plotje.render.dash]
            [scicloj.plotje.impl.render :as render])
  (:import [javax.imageio ImageIO]
           [java.io File]
           [java.awt Graphics2D]
           [scicloj.plotje.render.dash WithStrokeDash]))

;; Java2D parity for stroke dashes: membrane has no dash primitive, so we
;; teach its Java2D backend how to draw our WithStrokeDash wrapper. The
;; wrapper sits outside with-stroke-width, so the inner width merge keeps
;; the dash array we set here (merge-stroke preserves the current dash).
(extend-type WithStrokeDash
  java2d/IDraw
  (draw [this]
    (java2d/push-stroke
     (.setStroke ^Graphics2D java2d/*g*
                 (java2d/merge-stroke (.getStroke ^Graphics2D java2d/*g*)
                                      {:dash (float-array (:dash this))}))
     (doseq [d (:drawables this)]
       (java2d/draw d)))))

(defmethod render/membrane->plot :bufimg [membrane-tree _ opts]
  (let [w (int (or (ui/width membrane-tree) (:total-width opts) 600))
        h (int (or (ui/height membrane-tree) (:total-height opts) 400))]
    (java2d/draw-to-image membrane-tree [w h])))

(defmethod render/plan->plot :bufimg [plan _ opts]
  (let [render-opts (select-keys opts [:width :height :theme :palette
                                       :color-scale :color-midpoint
                                       :x-tick-angle :x-tick-label-pad])
        membrane-tree (membrane/plan->membrane plan render-opts)]
    (render/membrane->plot membrane-tree :bufimg
                           (assoc opts
                                  :total-width (:total-width plan)
                                  :total-height (:total-height plan)))))

(defn save-png
  "Save a BufferedImage to a PNG file. Returns the path."
  [^java.awt.image.BufferedImage img path]
  (ImageIO/write img "png" (File. (str path)))
  (str path))
