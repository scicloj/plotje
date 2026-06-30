(ns scicloj.plotje.render.bufimg
  "Render membrane drawable trees to java.awt.image.BufferedImage via
   membrane's Java2D backend. Faster than SVG for large plots and
   produces raster output that Clay renders automatically."
  (:require [membrane.java2d :as java2d]
            [membrane.ui :as ui]
            [scicloj.plotje.render.membrane :as membrane]
            [scicloj.plotje.impl.render :as render])
  (:import [javax.imageio ImageIO]
           [java.io File]))

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
