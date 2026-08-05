(ns scicloj.plotje.impl.frames
  "Drawing-space geometry of a plan: where each panel's frames sit on the
   canvas, and how a data position maps into them and back.

   A frame is a rectangle measured in drawing units. Three are named:

   - `:canvas` -- the whole output image
   - `:panel-box` -- one panel including its axis margin
   - `:drawing-area` -- the panel background inside that margin, where
     data marks are clipped

   The renderer builds the same panel origins and the same scales from
   the same plan values. That computation lives here so the public
   mapping (`pj/frames`) and the drawn output cannot drift apart:
   `render/membrane.clj` calls `panel-origin`, and `render/panel.clj`
   builds its scales the way `panel-scales` does."
  (:require [scicloj.plotje.impl.coord :as coord]
            [scicloj.plotje.impl.scale :as scale]))

(defn panel-origin
  "Top-left corner of `panel`'s panel box, relative to the plan's own
   canvas. `layout` pads push the grid right and down; `:row` and `:col`
   step through it."
  [plan panel]
  (let [{:keys [panel-width panel-height layout legend-position]} plan
        {:keys [y-label-pad title-pad legend-h]} layout
        strip-h (or (:strip-h layout) 0)
        ;; A :top legend takes a band between the title and the panels,
        ;; so the panels start below it. Mirrors plan->membrane.
        content-top (+ (or title-pad 0)
                       (if (= (or legend-position :right) :top)
                         (or legend-h 0)
                         0))]
    [(+ (or y-label-pad 0) (* (or (:col panel) 0) (double panel-width)))
     (+ content-top strip-h (* (or (:row panel) 0) (double panel-height)))]))

(defn panel-frames
  "The three frames of `panel`, each `[x y width height]` in canvas
   coordinates. `offset` shifts the panel box, for a panel that belongs
   to a composite cell rather than to the canvas directly."
  [plan panel [ox oy]]
  (let [{:keys [panel-width panel-height margin total-width total-height]} plan
        [px py] (panel-origin plan panel)
        x0 (+ (double ox) px)
        y0 (+ (double oy) py)
        m (double margin)]
    (array-map
     :canvas [0.0 0.0 (double total-width) (double total-height)]
     :panel-box [x0 y0 (double panel-width) (double panel-height)]
     :drawing-area [(+ x0 m) (+ y0 m)
                    (- (double panel-width) m m)
                    (- (double panel-height) m m)])))

(defn panel-scales
  "The wadogo scales `panel` is drawn with, built from the plan's domains
   and pixel dimensions exactly as `render/panel.clj` builds them."
  [plan panel]
  (let [{:keys [panel-width panel-height margin]} plan
        {:keys [x-domain y-domain x-scale y-scale]} panel
        m (double margin)]
    {:sx (scale/make-scale x-domain [m (- (double panel-width) m)] x-scale)
     :sy (scale/make-scale y-domain [(- (double panel-height) m) m] y-scale)}))

(defn panel-geometry
  "One panel's entry: its frames and everything needed to map into and out
   of them. Plain data -- the mappings are `to-drawing` and `to-data`
   below, which read this rather than being stored inside it."
  [plan panel offset]
  ;; An array-map, so the entry prints in a reading order -- where the
  ;; panel is, what it spans, then its rectangles. A map literal of this
  ;; many keys would be a hash map and would print in hash order.
  (array-map
   :row (or (:row panel) 0)
   :col (or (:col panel) 0)
   :coord (or (:coord panel) :cartesian)
   :x-domain (:x-domain panel)
   :y-domain (:y-domain panel)
   :x-scale (:x-scale panel)
   :y-scale (:y-scale panel)
   :invertible? (coord/invertible? (or (:coord panel) :cartesian))
   :frames (panel-frames plan panel offset)))

;; ---- Mapping between the spaces ----

(defn- panel-shape
  "The numbers a panel entry's own frames imply: where its panel box
   starts, how big it is, and how wide its margin is. Rebuilt from the
   entry rather than carried alongside it, so the entry contains no
   functions."
  [panel]
  (let [[bx by bw bh] (-> panel :frames :panel-box)
        [dx _ _ _] (-> panel :frames :drawing-area)]
    {:x0 (double bx) :y0 (double by)
     :pw (double bw) :ph (double bh)
     :m (- (double dx) (double bx))}))

(defn- panel-scale-pair
  "The two wadogo scales for a panel entry."
  [panel]
  (let [{:keys [pw ph m]} (panel-shape panel)]
    {:sx (scale/make-scale (:x-domain panel) [m (- pw m)] (:x-scale panel))
     :sy (scale/make-scale (:y-domain panel) [(- ph m) m] (:y-scale panel))}))

(defn to-drawing
  "Map data positions into canvas coordinates for `panel`."
  ([panel x y] (first (to-drawing panel [[x y]])))
  ([panel points]
   (let [{:keys [x0 y0 pw ph m]} (panel-shape panel)
         {:keys [sx sy]} (panel-scale-pair panel)
         coord-fn (coord/make-coord (:coord panel) sx sy pw ph m)]
     (mapv (fn [[x y]]
             (let [[px py] (coord-fn x y)]
               [(+ x0 (double px)) (+ y0 (double py))]))
           points))))

(defn to-data
  "Map canvas coordinates back to data positions for `panel`."
  ([panel cx cy] (first (to-data panel [[cx cy]])))
  ([panel points]
   (let [{:keys [x0 y0 pw ph m]} (panel-shape panel)
         {:keys [sx sy]} (panel-scale-pair panel)
         inverse-fn (coord/make-inverse (:coord panel) sx sy pw ph m)]
     (when-not inverse-fn
       (throw (ex-info (str "Coordinate system " (:coord panel) " has no inverse: "
                            "it folds x and y together, so a canvas position "
                            "does not name one pair of data values. Check "
                            ":invertible? on the panel before asking.")
                       {:coord (:coord panel)})))
     (mapv (fn [[cx cy]]
             (inverse-fn (- (double cx) x0) (- (double cy) y0)))
           points))))

(defn plan-frames
  "Geometry for every panel of `plan`, with `offset` added to each panel
   origin. A composite plan carries its cells in `:sub-plots`, each a leaf
   plan and the rectangle it is drawn into, so the cells recur with their
   own origin as the offset -- a composite is one coordinate system, not a
   nest of them."
  [plan offset]
  (if (:composite? plan)
    (vec (mapcat (fn [{:keys [plan rect]}]
                   (let [[x y] rect]
                     (plan-frames plan [(+ (double (first offset)) (double x))
                                        (+ (double (second offset)) (double y))])))
                 (:sub-plots plan)))
    (mapv #(panel-geometry plan % offset) (:panels plan))))
