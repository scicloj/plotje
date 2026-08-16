(ns scicloj.plotje.layer-type
  "Layer-type registry — keyword → layer-type map (mark + stat + position).
   Layer types are plain data maps. The registry makes them discoverable
   and extensible. Use `lookup` to get a layer type by keyword, `registered`
   to enumerate all layer types, and `register!` to add new ones."
  (:require [scicloj.plotje.impl.resolve :as resolve]))

;; ---- Registry ----

(def universal-layer-options
  "Layer options accepted by all layer types. :x and :y are included so a
   layer can override the pose's position mapping (an overlay-like
   pattern); the four-level merge in pose/leaf->draft already honors
   layer-level x/y, this list keeps build-layer's unknown-option
   warning consistent with that behavior. :x-type/:y-type/:color-type
   override the inferred column types (e.g. :x-type :categorical
   treats a numeric x as categorical). :mark and :stat let any layer
   override the visual mark or statistical transform supplied by its
   layer-type entry; unknown keywords raise a clear error at build time."
  [:x :y :color :color-type :alpha :group :position :data
   :x-type :y-type :mark :stat :offset-x :offset-y :in])

(def spaces
  "The coordinate systems a layer's `:x` and `:y` can be given in, named
   by `:in`. `:data` is the default and is what every layer has always
   used. `:drawing-area` measures in drawing units from the top left of
   the panel background, inside the axis margin.

   Not to be confused with `:position`, which is the dodge/stack/fill
   adjustment a layer type carries."
  #{:data :drawing-area})

(def layer-option-docs
  "Documentation for layer option keys. Maps key to description string."
  {:x "Column keyword or string naming the column drawn along the x axis, or a value to draw at that x. A value beside a column :y broadcasts over the layer's data; values for both :x and :y draw one mark"
   :y "Column keyword or string naming the column drawn along the y axis, or a value to draw at that y. The same two shapes as :x"
   :data "Dataset or plain data for this layer alone, overriding the pose's"
   :mark "Override the mark the layer type draws with — the shape on the panel"
   :stat "Override the statistic the layer type computes — e.g. {:stat :count} on a text layer labels counted bars"
   :color "A column of the layer's data, or a color -- a hex string with its #, a CSS name, or a keyword naming one. The data decides: a column called blue wins over the color of that name, and {:value \"blue\"} or {:column \"blue\"} insists. A column always passes through the color scale; {:color {:column :hex :scale false}} draws the colors it holds"
   :color-type "Override inferred color type — :categorical or :numerical. Use :categorical to treat numeric IDs as groups."
   :x-type "Override inferred x-column type — :categorical, :numerical, or :temporal. Use :categorical on numeric x (hours, years, IDs) when a categorical-axis mark (bar, boxplot) is needed."
   :y-type "Override inferred y-column type — :categorical, :numerical, or :temporal. Mirror of :x-type, used for horizontal layouts."
   :alpha "A column of the layer's data (per-row opacity), or a number within 0 and 1 for the whole layer. A column is scaled and a written number is drawn; {:alpha {:column :r :scale false}} reads the column as opacities, {:alpha {:value 0.3 :scale true}} sends the number through the scale"
   :group "A column of the layer's data, or a vector of them, grouping without color"
   :position "Position adjustment keyword — how overlapping groups are arranged (see pj/position-doc)"
   :nudge-x "Shift all x-coordinates by this data-space amount"
   :nudge-y "Shift all y-coordinates by this data-space amount"
   :in "The space this layer's :x and :y are in — :data (default, values mapped through the scales) or :drawing-area (drawing units from the top left of the panel background). It does not widen to the other aesthetics; to take one axis off its scale on its own, write {:y {:column :b :scale false}}. An unscaled layer is placed on the panel rather than in the data, so it does not move the axis domains"
   :offset-x "Shift the whole layer right by this many drawing units, after the scales. Unlike :nudge-x this is not a data value, so it works on a categorical axis and does not move the axis domain — use it to clear a label of the mark it labels"
   :offset-y "Shift the whole layer down by this many drawing units, after the scales. See :offset-x"
   :align-x "Horizontal text anchor — :left, :center, or :right (default :left); which part of the label sits at the x position"
   :align-y "Vertical text anchor — :top, :center, or :bottom (default :center); which part of the label sits at the y position. Data-oriented: :top puts the label's top edge at the point"
   :size "A column of the layer's data, or a positive number — point radius or stroke width. A column is scaled and a written number is drawn; {:size {:column :r :scale false}} reads the column as radii, {:size {:value 7 :scale true}} sends the number through the scale"
   :shape "A column of the layer's data, one symbol per category; or one symbol for the whole layer, from the list pj/shape-symbols gives"
   :jitter "true or an amount in drawing units — random offset to reduce overplotting"
   :text "A column of the layer's data, or a string, which labels every row with itself"
   :font-size "Text height in drawing units for a text or label mark (default 10)"
   :font-weight "Draws the text bold — :normal (default) or :bold"
   :font-style "Draws the text italic — :normal (default) or :italic"
   :box "Background box behind text — true for the default box, false or absent for none, or a map of box properties: {:corner-radius n} in drawing units (default 3, 0 for square corners). pj/lay-label is pj/lay-text with the box on"
   :confidence-band "true to show a standard-error confidence ribbon around the fitted line"
   :bootstrap-resamples "Number of bootstrap resamples for a LOESS confidence ribbon (default 200)"
   :bandwidth "Smoothing bandwidth for density and LOESS methods"
   :trim "Whether a density curve is estimated only over its own group's values. Density defaults to false (every group spans the whole layer's range); violin and ridgeline default to true (each body ends at its category's values)"
   :normalize "Histogram normalization — :density (area integrates to 1) or nil"
   :levels "Number of contour iso-levels (default 5)"
   :fill "A column of the layer's data holding tile fill values (pre-computed heatmap)"
   :y-min "A column of the layer's data, for an errorbar's lower bound; or a number, for a horizontal band's lower edge. Which one the layer type decides, and {:column :lo} or {:value 12} says the same thing at more length"
   :y-max "A column of the layer's data, for an errorbar's upper bound; or a number, for a horizontal band's upper edge. The same two shapes as :y-min"
   :side "Rug tick position — :x (default), :y, or :both"
   :density-2d-grid "2D density grid resolution — number of bins per axis (default 25)"
   :y-intercept "Numeric or temporal y-axis position for a horizontal reference line. Read straight from the mapping, so it names no column; {:value 2.0} says the same thing at more length"
   :x-intercept "Numeric or temporal x-axis position for a vertical reference line. The same two shapes as :y-intercept"
   :x-min "Lower x bound of a vertical shaded band, as a number or a date. Only lay-band-v reads it, and it reads the value straight from the mapping, so this names no column; {:value 1.5} says the same thing at more length"
   :x-max "Upper x bound of a vertical shaded band. The same two shapes as :x-min"
   :x-end "A column of the layer's data, or a number, for the right edge of a horizontal interval bar. A written value places every bar's right edge alike"
   :interval-thickness "Fraction (0.0–1.0) of the categorical band that an interval bar fills (default 0.7)"
   :bar-width "Width of a bar on a numeric or temporal x axis, in data units. Defaults to 0.9 of the smallest gap between adjacent x positions."
   :bins "Number of histogram bins, overriding the :bin-method estimate"
   :binwidth "Width of one histogram bin in data units, an alternative to :bins"
   :box-width "Fraction (0.0-1.0) of the categorical band that a box fills (default 0.6)"
   :cap-width "Width of an errorbar's end caps in drawing units"
   :length "Length of a rug tick in drawing units"
   :level "Confidence level of a smooth's ribbon (default 0.95)"
   :stroke "Outline color for an area or density curve — the fill still comes from :color"
   :stroke-width "Width of that outline in drawing units"
   :stroke-dash "Dash pattern for a line, step, smooth, reference line, or area outline — :dashed, :dotted, :solid, or a raw [dash gap ...] pattern in drawing units"})

(def quantities
  "The quantities a mark can draw an appearance channel as, and what
   each one implies. A mark names one of these per channel it varies
   from row to row, under `:varies`.

   - `:ink-exponent` — how the ink a mark covers grows with the
     quantity. A circle's radius and a square's side both square it; a
     stroke's width does not. It is what makes `{:by :area}` mean the
     same thing on every mark: area is the quantity raised to this
     power, so the scale can spread ink evenly without knowing which
     shape draws it.
   - `:swatch` — what the legend draws to explain the channel. A width
     encoding explained by graduated circles has the reader comparing
     diameters while the panel shows thicknesses.

   `:opacity` is the odd one: it is not a geometry, so nothing about
   area applies to it, and its exponent is 1 only in the sense that it
   has no shape to grow. `:geometry?` records that difference, which is
   what lets `register!` refuse a channel drawn as a quantity of the
   wrong kind -- an opacity declared as a radius is not inert, it
   squares the opacity curve."
  {:radius  {:ink-exponent 2 :swatch :circle  :geometry? true}
   :side    {:ink-exponent 2 :swatch :square  :geometry? true}
   :width   {:ink-exponent 1 :swatch :segment :geometry? true}
   :opacity {:ink-exponent 1 :swatch :square  :geometry? false}})

(def default-quantities
  "What a channel is taken to be drawn as where the mark declares
   nothing. Reached only for a mark that draws the channel without
   saying so -- which the plan warns about -- and set to what the
   built-in marks do.

   Its keys are also the channels a mark may declare it varies: these
   are the two the plan and the scale ask about, so a `:varies` naming
   any other channel would be read by nothing."
  {:size :radius :alpha :opacity})

(def ^:private registry*
  "Atom holding keyword → layer-type entry map."
  (atom {}))

(defn register!
  "Register a layer type. `k` is a keyword, `entry` is a map with
   :mark, :stat, and optionally :position and :doc.
   Position defaults to nil (identity) — only :dodge, :stack, :fill are explicit.

   :defaults is an optional map of layer-option values the layer type
   presets — how one layer type differs from another that shares its mark
   by the options it starts with rather than by what it draws. `:label` is
   `:text` with `{:box true}`. Options passed at the call site win over
   these; see resolve-layer-type-info in impl/pose.clj.

   :varies is an optional map from appearance channel to the quantity
   the mark draws it as, from `quantities` — `:point` declares
   `{:size :radius :alpha :opacity}`. A channel absent from the map is
   one the mark draws once for the whole layer, so a column mapped to
   it varies nothing: `:line` takes one stroke width whatever the
   column holds.

   Two things read the declaration, and both were closed tables before
   it existed. The plan asks it whether a column mapped to a channel is
   drawn at all — a mark it had never heard of answered no, so an
   extension that varied size per row was warned about and denied its
   legend while drawing correctly. The scale asks it which quantity to
   compute, so `{:by :area}` lands as an area whether the mark draws a
   radius or a width."
  [k entry]
  (when-let [v (:varies entry)]
    (when-not (map? v)
      (throw (ex-info (str "Layer type " k " declares :varies " (pr-str v)
                           ", which is not a map. It maps each appearance"
                           " channel the mark varies from row to row to the"
                           " quantity it draws it as, as "
                           (pr-str {:size :radius}) ".")
                      {:layer-type k :varies v}))))
  (doseq [[channel quantity] (:varies entry)]
    (when-not (contains? default-quantities channel)
      (throw (ex-info (str "Layer type " k " declares :varies for " channel
                           ", which is not a channel Plotje varies from row"
                           " to row. Supported: "
                           (vec (sort (keys default-quantities)))
                           ". A declaration for any other channel is read by"
                           " nothing.")
                      {:layer-type k :channel channel
                       :supported (vec (sort (keys default-quantities)))})))
    (when-not (quantities quantity)
      (throw (ex-info (str "Layer type " k " declares :varies " channel " "
                           quantity ", which is not a quantity Plotje draws."
                           " Supported: " (vec (sort (keys quantities))) ".")
                      {:layer-type k :channel channel :quantity quantity
                       :supported (vec (sort (keys quantities)))})))
    ;; A geometry and an opacity are not interchangeable: declaring
    ;; `{:alpha :radius}` is not inert, it squares the opacity curve,
    ;; because the exponent that spreads ink is applied to whatever the
    ;; channel is drawn as.
    (let [wanted (get-in quantities [(default-quantities channel) :geometry?])
          got (get-in quantities [quantity :geometry?])]
      (when (not= wanted got)
        (throw (ex-info (str "Layer type " k " declares :varies " channel " "
                             quantity ", and " channel " is "
                             (if wanted "a geometry" "not a geometry")
                             " while " quantity " is "
                             (if got "a geometry" "not one")
                             ". Supported for " channel ": "
                             (vec (sort (keep (fn [[q info]]
                                                (when (= wanted (:geometry? info)) q))
                                              quantities)))
                             ".")
                        {:layer-type k :channel channel :quantity quantity}))))
    ;; Two layer types sharing a mark describe the same drawing, so
    ;; they cannot disagree about what it varies. Caught here because
    ;; the lookup below answers per mark: left to run, a disagreement
    ;; would be settled by whichever entry the map happened to yield.
    ;; The entry being replaced is not a second opinion about its own
    ;; mark, so re-registering a layer type to correct its own :varies
    ;; is not a conflict with itself.
    (doseq [[other-k other] @registry*
            :when (and (not= other-k k)
                       (= (:mark other) (:mark entry))
                       (get-in other [:varies channel])
                       (not= (get-in other [:varies channel]) quantity))]
      (throw (ex-info (str "Layer type " k " declares :varies " channel " "
                           quantity ", and " other-k " draws the same mark ("
                           (:mark entry) ") as "
                           (get-in other [:varies channel])
                           ". A mark draws one quantity per channel.")
                      {:layer-type k :other other-k :mark (:mark entry)
                       :channel channel :quantity quantity}))))
  (swap! registry* assoc k (resolve/map->LayerType entry))
  k)

(defn lookup
  "Look up a registered layer type by keyword. Returns the layer-type map
   (with :mark, :stat, :position, :doc), or nil if not found."
  [k]
  (get @registry* k))

(defn registered
  "Return all registered layer types as a map of keyword → entry."
  []
  @registry*)

(defn mark-varies
  "The quantity `mark` draws `channel` as from row to row, or nil where
   it draws one value for the whole layer.

   Asked of the mark rather than of the layer type, because a layer can
   name its own `:mark` and because two layer types sharing a mark draw
   the same thing — `:label` is `:text` with a box preset on. Derived by
   looking through the registry rather than stored beside it, so one
   `register!` call is still the whole of the extension contract.
   `register!` refuses a disagreement, so the first entry found answers
   for the mark."
  [mark channel]
  (some (fn [[_ entry]]
          (when (= mark (:mark entry))
            (get-in entry [:varies channel])))
        @registry*))

(defn marks-varying
  "Every mark that varies `channel` from row to row, as a set. What the
   plan's messages name when a column is mapped to a channel no mark on
   the plot draws."
  [channel]
  (into #{} (for [[_ entry] @registry*
                  :when (get-in entry [:varies channel])]
              (:mark entry))))

(defn ink-exponent
  "How the ink a mark covers grows with the quantity it draws `channel`
   as. What lets a scale's `:by` mean one thing across marks that draw
   different shapes."
  [mark channel]
  (-> (or (mark-varies mark channel) (default-quantities channel))
      quantities
      :ink-exponent))

(def layer-type-order
  "Canonical display order for built-in layer types."
  [:point :line :step :area
   :histogram :bar
   :smooth :density
   :tile :density-2d :contour
   :boxplot :violin :ridgeline
   :summary :errorbar :lollipop
   :text :label :rug
   :interval-h
   :rule-h :rule-v :band-h :band-v])

;; ---- Built-in layer types ----

;; `:text` is not on `:point`'s accept-list: the point mark draws no
;; label, so `{:text "note"}` there was taken and dropped in silence.
;; Off the list, the universal warning names `lay-text` and `lay-label`
;; as the layer types that draw one.
;; The only built-in mark that varies an appearance channel from row to
;; row. Every other mark draws one value for the whole layer -- `:line`
;; takes one stroke width, `:boxplot` one opacity -- so a column mapped
;; there varies nothing.
(register! :point {:mark :point :stat :identity :accepts [:size :shape :jitter :nudge-x :nudge-y] :varies {:size :radius :alpha :opacity} :doc "Scatter — individual data points."})
(register! :line {:mark :line :stat :identity :accepts [:size :stroke-dash :nudge-x :nudge-y] :doc "Line — connects data points in order."})
(register! :step {:mark :step :stat :identity :accepts [:size :stroke-dash] :doc "Step — horizontal-then-vertical connected points."})
(register! :area {:mark :area :stat :identity :accepts [:stroke :stroke-width :stroke-dash] :doc "Area — filled region under a line."})
(register! :histogram {:mark :bar :stat :bin :x-only true :accepts [:normalize :bins :binwidth] :doc "Histogram — bins numerical data into bars."})
(register! :bar {:mark :rect :accepts [:bar-width] :doc "Bar — counts categories (x only), or uses y as the bar height when a y column is given."})
(register! :smooth {:mark :line :stat :loess :accepts [:confidence-band :level :bootstrap-resamples :bandwidth :size :stroke-dash :nudge-x :nudge-y] :doc "Smoothed trend line — defaults to LOESS; pass {:stat :linear-model} for OLS."})
(register! :density {:mark :area :stat :density :x-only true :accepts [:bandwidth :trim :stroke :stroke-width :stroke-dash] :doc "Density — KDE (kernel density estimation) as filled area."})
(register! :tile {:mark :tile :stat :bin2d :accepts [:fill :density-2d-grid] :doc "Tile/heatmap — 2D grid binning."})
(register! :density-2d {:mark :tile :stat :density-2d :accepts [:density-2d-grid] :doc "2D density — kernel density estimation (KDE) smoothed heatmap."})
(register! :contour {:mark :contour :stat :density-2d :accepts [:levels :size] :doc "Contour — iso-density contour lines."})
(register! :boxplot {:mark :boxplot :stat :boxplot :accepts [:size :box-width] :doc "Boxplot — median, quartiles, whiskers, outliers."})
(register! :violin {:mark :violin :stat :violin :accepts [:bandwidth :trim :size] :doc "Violin — mirrored density curve per category."})
(register! :ridgeline {:mark :ridgeline :stat :violin :accepts [:bandwidth :trim] :doc "Ridgeline — stacked density curves per category."})
(register! :summary {:mark :pointrange :stat :summary :accepts [:size] :doc "Summary — mean ± standard error per category."})
(register! :errorbar {:mark :errorbar :stat :identity :accepts [:y-min :y-max :size :cap-width :nudge-x :nudge-y] :doc "Errorbar — vertical error bars."})
(register! :lollipop {:mark :lollipop :stat :identity :accepts [:size] :doc "Lollipop — stem with dot."})
(register! :text {:mark :text :stat :identity :accepts [:text :font-size :font-weight :font-style :box :nudge-x :nudge-y :align-x :align-y] :doc "Text — data-driven labels, optionally on a background box."})
(register! :label {:mark :text :stat :identity :defaults {:box true} :accepts [:text :font-size :font-weight :font-style :box :nudge-x :nudge-y :align-x :align-y] :doc "Label — text on a background box. The :text mark with :box preset on."})
(register! :rug {:mark :rug :stat :identity :x-only true :accepts [:side :length] :doc "Rug — axis-margin tick marks."})
(register! :interval-h {:mark :interval-h :stat :identity :accepts [:x-end :interval-thickness]
                        ;; Dodge/stack/fill don't compose with interval-h yet -- a Gantt
                        ;; row may have multiple non-overlapping or overlapping intervals
                        ;; per category and we draw each row's rect at the band centre.
                        :rejects [:position]
                        :doc "Interval — horizontal bars from x to x-end at categorical y. For Gantt-style timelines."})
;; Rule and band layer types reject the universal options that have no
;; meaning for a single rule/band: there are no groups to dodge or
;; stack, no shape/jitter to vary across an aggregated mark, and the
;; column-type overrides only matter for stat-based marks.
;;
;; `:in` is rejected for a different reason: these four marks are
;; carried on a panel's `:annotations` slot rather than among its
;; `:layers`, and that path places them from data values only.
;; `:offset-x`/`:offset-y` do apply -- the annotation renderer shifts
;; each drawable by them, as the layer renderer does.
(def ^:private annotation-rejects
  [:position :group :x-type :y-type :color-type :in])

(register! :rule-h {:mark :rule-h :stat :identity :accepts [:y-intercept :stroke-dash] :rejects annotation-rejects :doc "Horizontal reference line at y = y-intercept."})
(register! :rule-v {:mark :rule-v :stat :identity :accepts [:x-intercept :stroke-dash] :rejects annotation-rejects :doc "Vertical reference line at x = x-intercept."})
(register! :band-h {:mark :band-h :stat :identity :accepts [:y-min :y-max] :rejects annotation-rejects :doc "Horizontal shaded band between y = y-min and y = y-max."})
(register! :band-v {:mark :band-v :stat :identity :accepts [:x-min :x-max] :rejects annotation-rejects :doc "Vertical shaded band between x = x-min and x = x-max."})

