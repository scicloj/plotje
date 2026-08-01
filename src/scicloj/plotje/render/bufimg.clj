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
           [membrane.ui Label Rotate]
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

;; ---- Text placement parity ----
;;
;; The two backends disagreed about where a membrane Label sits relative to
;; its origin, so every PNG placed its text differently from the SVG of the
;; same plan. The SVG backend is the reference: it emits
;; dominant-baseline="hanging" plus the label's :text-anchor, which puts the
;; text in a band of the font's own size starting at the origin and shifts it
;; horizontally to meet the anchor. Membrane's Java2D backend reads neither --
;; it draws every string leftwards from the origin, one line-height below it.
;; The two extensions below give Java2D the same contract. They replace
;; membrane's own impls for these types, which is why they say why.

(defn- anchor-dx
  "Horizontal shift that turns a left-drawn string of advance width `w` into
   an SVG text-anchor placement."
  [text-anchor w]
  (case text-anchor
    "middle" (- (/ (double w) 2.0))
    "end" (- (double w))
    0.0))

(defn- hanging-dy
  "Vertical shift from membrane's text origin to the SVG one. Membrane draws
   the first line's baseline one line-height below the origin; SVG's hanging
   baseline instead centers the font's ascent-to-descent box on a band of the
   font's own size, which is the box the layout code reserves for a label."
  [font]
  (let [size (double (or (:size font) 14))
        {:keys [ascent descent]} (java2d/font-metrics font)
        baseline (+ (/ size 2.0) (/ (- ascent descent) 2.0))]
    (- baseline (dec (java2d/font-line-height font)))))

(extend-type Label
  java2d/IDraw
  (draw [this]
    (let [font (:font this)
          [w _] (java2d/text-bounds (java2d/get-java-font font) (:text this))]
      (java2d/push-transform
       (.translate ^Graphics2D java2d/*g*
                   (double (anchor-dx (:text-anchor this) w))
                   (double (hanging-dy font)))
       ;; LabelRaw is membrane's own unpositioned drawing path, so the glyphs
       ;; themselves still come from membrane.
       (java2d/draw (java2d/->LabelRaw (:text this) font))))))

;; Rotate carries children, so membrane's default drawable impl walks straight
;; through it and draws them unrotated -- silently dropping :x-tick-angle from
;; every PNG. SVG emits rotate(degrees) about the origin; positive degrees turn
;; clockwise in both, so the angle passes through unchanged.
(extend-type Rotate
  java2d/IDraw
  (draw [this]
    (java2d/push-transform
     (.rotate ^Graphics2D java2d/*g* (Math/toRadians (:degrees this)))
     (java2d/draw (:drawable this)))))

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
