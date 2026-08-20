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
  (:require [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as dfn]
            [scicloj.plotje.impl.coord :as coord]
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
  "The two frames a panel owns, each `[x y width height]` in canvas
   coordinates. `offset` shifts them, for a panel that belongs to a
   composite cell rather than to the canvas directly.

   The canvas is the third frame and is not among them: it belongs to
   the plot, not to a panel, and every panel would repeat it. It was
   reported here once, and was wrong for a composite -- built from the
   cell's own dimensions, so a 700-wide image with two cells told every
   panel its canvas was 350 wide."
  [plan panel [ox oy]]
  (let [{:keys [panel-width panel-height margin]} plan
        [px py] (panel-origin plan panel)
        x0 (+ (double ox) px)
        y0 (+ (double oy) py)
        m (double margin)]
    (array-map
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
  "The two wadogo scales for a panel entry, given the shape already read
   off it. Building a scale is the expensive part of a mapping call --
   several microseconds against a few nanoseconds for applying one -- so
   both directions build the pair once and reuse it across every point."
  [panel {:keys [pw ph m]}]
  {:sx (scale/make-scale (:x-domain panel) [m (- pw m)] (:x-scale panel))
   :sy (scale/make-scale (:y-domain panel) [(- ph m) m] (:y-scale panel))})

(defn- coordinate-columns
  "The `:x` and `:y` columns of `data`, which may be a dataset or anything
   `tc/dataset` coerces -- `{:x [1 2] :y [3 4]}`, or a vector of row maps.

   A dataset rather than two loose sequences, because the two coordinates
   of a point share one index: a dataset says that, and a pair of
   sequences only promises it."
  [caller data]
  (when (and (sequential? data) (number? (first data)))
    (throw (ex-info (str caller " takes either two coordinates or a dataset "
                         "of them, but got a collection of numbers: "
                         (pr-str (vec (take 4 data)))
                         ". For one point, pass the coordinates as two "
                         "arguments; for several, pass "
                         "`{:x [...] :y [...]}`.")
                    {:data data})))
  (when (and (sequential? data) (sequential? (first data)))
    (throw (ex-info (str caller " takes a dataset of coordinates, not a "
                         "collection of pairs. Pass `{:x [...] :y [...]}`, "
                         "whose two columns share one index, rather than "
                         (pr-str (vec (take 2 data))) ".")
                    {:data data})))
  (let [ds (if (tc/dataset? data) data (tc/dataset data))
        columns (set (tc/column-names ds))]
    (when-not (and (contains? columns :x) (contains? columns :y))
      (throw (ex-info (str caller " needs :x and :y columns, but its dataset "
                           "has " (vec (sort-by str (tc/column-names ds)))
                           ". Rename them, e.g. "
                           "`(tc/rename-columns ds {:lon :x :lat :y})`.")
                      {:columns (vec (tc/column-names ds))})))
    [(ds :x) (ds :y)]))

(defn- data-axis
  "The domain and scale spec a *data* axis is drawn through, as
   `[domain scale-spec]`.

   Under `:coord :flip` the data x is drawn up the vertical axis and the
   data y along the horizontal one, and `resolve-panel-domains` has
   already swapped `:x-domain` and `:y-domain` so that they describe the
   drawn axes. `to-drawing` and `to-data` speak in data order either
   way, so under a flip a question about data x is answered by the
   panel's y entries."
  [panel axis]
  (if (= (= :flip (:coord panel)) (= axis :x))
    [(:y-domain panel) (:y-scale panel)]
    [(:x-domain panel) (:x-scale panel)]))

(defn- check-categories
  "Refuse a value a categorical axis has no position for.

   A band scale answers with a position for each of its categories and
   with nothing between them, so a value it does not name -- a typo, a
   category filtered out of the data, or a fractional place such as 2.5
   -- has no position at all. Left to the scale this surfaced as a
   NullPointerException naming neither the value nor the axis, and the
   two arities disagreed about it: the scalar one threw while the
   dataset one wrote nil into the column."
  [caller panel axis values]
  (let [[domain spec] (data-axis panel axis)]
    (when (= :categorical (scale/scale-kind domain spec))
      ;; The seq is what says whether there was an offender; the offender
      ;; itself may be nil, which is one of the values a categorical axis
      ;; has no position for. Testing it for truth would let exactly that
      ;; one through.
      (when-let [offenders (seq (remove (set domain) values))]
        (let [bad (first offenders)]
          (throw (ex-info (str caller " got " (pr-str bad) " for " axis ", which is not a "
                               "category on this axis. Categories: " (vec domain) ". A "
                               "categorical axis is a band scale: it has a position for "
                               "each category and none between them. To place a mark clear "
                               "of another, use :offset-x / :offset-y, which shift by a "
                               "distance on the page and work on any axis.")
                          {:caller caller :axis axis :value bad
                           :categories (vec domain)})))))))

(defn- check-panel
  "Refuse anything but a panel entry from `pj/frames`.

   A panel entry is one element of `(:panels (pj/frames plot))`, and it
   is recognised by its `:frames`. Given a pose, a plan or the frames map
   itself, `panel-shape` read nil out of `:frames` and the arithmetic
   below it died on a NullPointerException naming neither the argument
   nor the function -- the same shape `check-categories` was written to
   remove."
  [caller panel]
  (when-not (and (map? panel)
                 (-> panel :frames :panel-box)
                 (-> panel :frames :drawing-area))
    ;; One cond, so what the argument is and what to do about it cannot
    ;; disagree, and so no branch reads a key out of a non-map.
    (let [[got fix]
          (cond
            (nil? panel)
            ["nil"
             "A panel entry comes from `pj/frames`: `(-> pose pj/frames :panels first)`."]

            (not (map? panel))
            [(str "a " (.getName (class panel)))
             "A panel entry comes from `pj/frames`: `(-> pose pj/frames :panels first)`."]

            (and (contains? panel :canvas) (contains? panel :panels))
            ["the whole frames map"
             "Pick a panel from it: `(-> frames :panels first)`."]

            (or (contains? panel :panels) (contains? panel :sub-plots))
            ["a plan"
             "Ask for its frames first: `(-> plan pj/frames :panels first)`."]

            (or (contains? panel :layers) (contains? panel :poses))
            ["a pose"
             "Ask for its frames first: `(-> pose pj/frames :panels first)`."]

            :else
            [(str "a map with keys " (vec (sort-by str (keys panel))))
             "A panel entry comes from `pj/frames`: `(-> pose pj/frames :panels first)`."])]
      (throw (ex-info (str caller " takes one panel entry -- an element of "
                           "`(:panels (pj/frames plot))` -- but got "
                           got ". " fix)
                      {:caller caller :panel panel})))))

(defn- axis-answer-type
  "The datatype a data axis answers in when read backwards. A continuous
   scale inverts to a number; a band scale inverts to the category whose
   band holds the position, or nil outside every band. Reading a
   continuous axis back through an object column would box every value
   and roughly double the cost of building the dataset from it."
  [panel axis]
  (let [[domain spec] (data-axis panel axis)]
    (if (= :categorical (scale/scale-kind domain spec)) :object :float64)))

(defn- to-drawing-fn
  "A function from one data pair to one canvas pair, with the panel's
   shape, scales and projection resolved once."
  [panel]
  (let [{:keys [x0 y0 pw ph m] :as shape} (panel-shape panel)
        {:keys [sx sy]} (panel-scale-pair panel shape)
        coord-fn (coord/make-coord (:coord panel) sx sy pw ph m)]
    (fn [x y]
      (let [[px py] (coord-fn x y)]
        [(+ x0 (double px)) (+ y0 (double py))]))))

(defn- to-data-fn
  "The inverse of `to-drawing-fn`, or a throw if this projection has none."
  [panel]
  (let [{:keys [x0 y0 pw ph m] :as shape} (panel-shape panel)
        {:keys [sx sy]} (panel-scale-pair panel shape)
        inverse-fn (coord/make-inverse (:coord panel) sx sy pw ph m)]
    (when-not inverse-fn
      (throw (ex-info (str "Coordinate system " (:coord panel) " has no inverse: "
                           "it folds x and y together, so a canvas position "
                           "does not name one pair of data values. Check "
                           ":invertible? on the panel before asking.")
                      {:coord (:coord panel)})))
    (fn [cx cy]
      (inverse-fn (- (double cx) x0) (- (double cy) y0)))))

(defn to-drawing
  "Map data positions into canvas coordinates for `panel`. Two coordinates
   give one point back as `[x y]`; a dataset of them gives a dataset back."
  ([panel x y]
   (check-panel "pj/to-drawing" panel)
   (check-categories "pj/to-drawing" panel :x [x])
   (check-categories "pj/to-drawing" panel :y [y])
   ((to-drawing-fn panel) x y))
  ([panel data]
   (check-panel "pj/to-drawing" panel)
   (let [[xs ys] (coordinate-columns "pj/to-drawing" data)
         _ (check-categories "pj/to-drawing" panel :x xs)
         _ (check-categories "pj/to-drawing" panel :y ys)
         {:keys [x0 y0 pw ph m] :as shape} (panel-shape panel)
         {:keys [sx sy]} (panel-scale-pair panel shape)
         [pxs pys] ((coord/make-coord-columns (:coord panel) sx sy pw ph m)
                    xs ys)]
     ;; Realized once, at the end: a returned column is read more than
     ;; once, and a lazy noncaching reader would run the scale again on
     ;; every read.
     (tc/dataset {:x (dtype/clone (dfn/+ pxs x0))
                  :y (dtype/clone (dfn/+ pys y0))}))))

(defn to-data
  "Map canvas coordinates back to data positions for `panel`. Two
   coordinates give one point back as `[x y]`; a dataset of them gives a
   dataset back."
  ([panel cx cy]
   (check-panel "pj/to-data" panel)
   ((to-data-fn panel) cx cy))
  ([panel data]
   (check-panel "pj/to-data" panel)
   (let [[cxs cys] (coordinate-columns "pj/to-data" data)
         {:keys [x0 y0 pw ph m] :as shape} (panel-shape panel)
         {:keys [sx sy]} (panel-scale-pair panel shape)
         inverse-columns (coord/make-inverse-columns
                          (:coord panel) sx sy pw ph m
                          (axis-answer-type panel :x)
                          (axis-answer-type panel :y))]
     (when-not inverse-columns
       (throw (ex-info (str "Coordinate system " (:coord panel) " has no inverse: "
                            "it folds x and y together, so a canvas position "
                            "does not name one pair of data values. Check "
                            ":invertible? on the panel before asking.")
                       {:coord (:coord panel)})))
     (let [[xs ys] (inverse-columns (dtype/clone (dfn/- cxs x0))
                                    (dtype/clone (dfn/- cys y0)))]
       (tc/dataset {:x (dtype/clone xs) :y (dtype/clone ys)})))))

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
