(ns scicloj.plotje.impl.defaults
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure2d.color :as c]
            [scicloj.kindly.v4.api :as kindly]))

;; ---- Palette and Theme ----

(defn c2d->rgba
  "Convert a clojure2d color (Vec4, 0-255 channels) to [r g b a] in 0-1 range."
  [color]
  (let [cc (c/to-color color)]
    [(/ (double (c/red cc)) 255.0)
     (/ (double (c/green cc)) 255.0)
     (/ (double (c/blue cc)) 255.0)
     (/ (double (c/alpha cc)) 255.0)]))

(def default-palette-name
  "Default categorical palette name (clojure2d palette keyword)."
  :set1)

(def ^:private palette-aliases
  "Short aliases for clojure2d palette names that differ from our old naming."
  {:tableau10 :tableau-10})

(defn resolve-palette
  "Resolve a keyword to a clojure2d palette, trying aliases.
   Returns a non-empty palette vector, falling back to the default palette."
  [k]
  (let [pal (c/palette (get palette-aliases k k))]
    (if (seq pal) pal (c/palette default-palette-name))))

(def theme
  "Default theme: background color, grid color, and font size."
  {:bg "#E8E8E8" :grid "#F5F5F5" :font-size 11})

;; ---- Visual Defaults ----

(def defaults
  "In-code fallback for keys not present in the EDN resource. The EDN
   file (`resources/plotje-defaults.edn`) is the canonical source;
   this map is consulted only when the EDN load fails or as a secondary
   lookup in `(or cfg defaults)` patterns in stat/layout code."
  {;; Layout — must match EDN
   :width 600 :height 400
   :margin 10 :margin-multi 10 :panel-size 200 :legend-width 100
   ;; Ticks
   :x-tick-spacing 60 :y-tick-spacing 40
   ;; Points
   :point-radius 3.0 :point-opacity 0.75
   :point-stroke "none" :point-stroke-width 0
   ;; Bars and lines
   :bar-opacity 0.85 :line-width 2.5 :grid-stroke-width 0.6
   ;; Annotations
   :annotation-stroke "#333" :annotation-dash [4 3] :band-opacity 0.15
   ;; Statistics
   :bin-method :sturges
   :domain-padding 0.05
   ;; Labels and titles
   :label-font-size 13 :title-font-size 15
   :label-offset 38 :title-offset 18
   :fit-text-domain true
   ;; Facet strips
   :strip-font-size 11 :strip-height 16
   ;; Number separators
   :thousands-separator nil :decimal-separator nil
   ;; Fallback
   :default-color "#333"})

;; ---- Aesthetic Registry ----

(def aesthetic-registry
  "Per-aesthetic properties, and the single table the rest of the
   library derives its per-aesthetic sets from. One entry per
   aesthetic, each carrying everything any consumer needs to know about
   it, so a new aesthetic is added here and nowhere else.

   - `:category` -- `:positional` places a mark, `:appearance` decides
     how it looks, `:grouping` splits the data and draws nothing of its
     own. The glossary's Aesthetic entry teaches the same three, under
     those names.
   - `:column?` -- whether the aesthetic can name a dataset column at
     all. `:x-min` and `:x-max` cannot: only `lay-band-v` reads them,
     and it reads a value straight from the mapping. `:y-min` and
     `:y-max` can, because `lay-errorbar` reads them as columns -- see
     the note on layer-dependent readings at the end.
   - `:numeric?` -- whether the column it names, when it names one,
     holds numbers. Those are the keys eligible for finite-value
     filtering at plan time.
   - `:value?` -- whether a written value is accepted beside a column
     reference. `:fill` and `:group` take a column and nothing else
     today. For `:fill` that is a gap, and `:scale-default` below says
     what a value there would mean once it is closed; for `:group` it
     is not, since it splits the data and draws nothing of its own, so
     there is nothing a value could mean.
   - `:scale-default` -- which side of the scale a **written value**
     falls on when the mapping does not say. It has nothing to say
     about a column: a column passes through the aesthetic's scale
     wherever there is one, uniformly, and nothing reads its contents.
     `:by-source` draws it, and the aesthetic's vocabulary is what
     the gate refuses it against: `\"red\"` is a color and is drawn,
     `\"notacolor\"` is neither a column nor a color and is reported.
     `:always` scales it instead -- `{:x 6.5}` is a datum on the axis,
     not 6.5 drawing units. `:never` means no scale to pass through;
     `nil` means no scale at all. A `:scale` in the mapping overrides
     it in either direction, and is the only way to move a column off
     its scale or to read a written value as data.

     **The split is by category, not by aesthetic.** Every appearance
     aesthetic is `:by-source` and every positional one is `:always`,
     which is the same distinction the glossary draws and the
     `:category` entry above records. The reason the two differ is
     that position already has a second vocabulary for page geometry
     and appearance does not: `{:in :drawing-area}` says \"these
     coordinates are drawing units\", so a bare `{:x 6.5}` is free to
     mean the data value, while nothing but `{:size 7}` can say a
     radius of seven. Both readings stay reachable either way --
     `{:x {:value 6.5 :scale false}}` and
     `{:size {:value 7 :scale true}}`.
   - `:drawn-column?` -- whether a column of this aesthetic can be
     drawn as it stands, which is what `{:scale false}` on a column
     asks for. It is the only route to an identity scale now that a
     column never leaves its scale by convention. False where the
     reading is not written: `:shape` would draw a column of symbols
     and does not, and `:fill` derives its color through a gradient
     with no branch for a value that already is one. Kept as a flag
     rather than a silence for the same reason `:value?` is: **one
     entry says what a reading would mean and another says whether it
     exists, and a gate needs both.** See `dev-notes/backlog.md`.
   - `:categorical-column?` -- whether the column it names may hold
     categories. False on the three column-bearing aesthetics that
     encode a magnitude and have no categorical counterpart --
     `:alpha`, `:fill`, `:size`, which is `continuous-column-aesthetics`
     -- and on `:x-min` / `:x-max`, which name no column at all.
   - `:scale-key` -- the key a draft layer carries this channel's
     resolved scale spec under, for the aesthetics that have a scale.
     `pj/scale` writes the mapping rather than `:opts`, so this names
     where the spec arrives, not where it is written.
   - `:legend?` -- whether the compositor may hoist this aesthetic's
     legend to composite level when every leaf agrees on it. It is not
     which aesthetics draw a legend: `:fill` draws a continuous one and
     is not hoisted, so it carries no `:legend?`.

   - `:literal->column?` -- whether `impl.pose/resolve-positional-values`
     turns a literal value here into a constant column before anything
     else reads the mapping. True of the three the stat and the extract
     read; the band bounds are read straight from the mapping by their
     own marks instead, so normalizing them would take the value away
     from the only code that wants it.

   `:scale-default` says what a value means once the aesthetic accepts
   one, so `:fill` carries `:by-source` while `:value?` is still false.
   Setting `:value?` before the reading exists would make a value pass
   every check and then draw nothing, which is the defect this table is
   here to prevent.

   One pair records a reading that depends on the layer rather than on
   the value, which is a wart the table makes visible rather than
   hides: `:y-min` / `:y-max` are a column on `:errorbar` and a value
   on `:band-h`. `:text` is not such a pair -- a written label
   broadcasts over the layer's rows whether or not the layer has
   data."
  {:x     {:category :positional :column? true  :value? true  :numeric? true  :categorical-column? true  :scale-default :always :drawn-column? true    :literal->column? true :scale-key :x-scale}
   :y     {:category :positional :column? true  :value? true  :numeric? true  :categorical-column? true  :scale-default :always :drawn-column? true    :literal->column? true :scale-key :y-scale}
   :x-end {:category :positional :column? true  :value? true  :numeric? true  :categorical-column? true  :scale-default :always :drawn-column? true    :literal->column? true}
   :y-min {:category :positional :column? true  :value? true  :numeric? true  :categorical-column? true  :scale-default :always :drawn-column? true}
   :y-max {:category :positional :column? true  :value? true  :numeric? true  :categorical-column? true  :scale-default :always :drawn-column? true}
   :x-min {:category :positional :column? false :value? true  :numeric? true  :categorical-column? false :scale-default :always :drawn-column? true}
   :x-max {:category :positional :column? false :value? true  :numeric? true  :categorical-column? false :scale-default :always :drawn-column? true}
   :color {:category :appearance :column? true  :value? true  :numeric? true  :categorical-column? true  :scale-default :by-source :drawn-column? true  :scale-key :color-scale :legend? true}
   :size  {:category :appearance :column? true  :value? true  :numeric? true  :categorical-column? false :scale-default :by-source :drawn-column? true :scale-key :size-scale  :legend? true}
   :alpha {:category :appearance :column? true  :value? true  :numeric? true  :categorical-column? false :scale-default :by-source :drawn-column? true :scale-key :alpha-scale :legend? true}
   :fill  {:category :appearance :column? true  :value? false :numeric? true  :categorical-column? false :scale-default :by-source :drawn-column? false  :scale-key :fill-scale}
   :shape {:category :appearance :column? true  :value? true  :numeric? false :categorical-column? true  :scale-default :by-source :drawn-column? false  :scale-key :shape-scale :legend? true}
   :text  {:category :appearance :column? true  :value? true  :numeric? false :categorical-column? true  :scale-default :never :drawn-column? true}
   :group {:category :grouping   :column? true  :value? false :numeric? false :categorical-column? true  :scale-default nil :drawn-column? false}})

(defn aesthetics-where
  "The aesthetics whose registry entry satisfies `pred`, in a stable
   order. Every per-aesthetic set in the library comes from here."
  [pred]
  (into [] (comp (filter (comp pred val)) (map key)) (sort aesthetic-registry)))

(def column-keys
  "Set of keywords that can reference dataset columns in mappings."
  (set (aesthetics-where :column?)))

(def numeric-aesthetic-keys
  "Aesthetics whose column values are numeric -- subject to finite-value
   filtering. Derived from aesthetic-registry."
  (aesthetics-where #(and (:numeric? %) (:column? %))))

(def positional-aesthetics
  "The aesthetics that place a mark, and so may be given as a value.
   Named for the glossary's sake: `:position` there is the dodge /
   stack / fill adjustment, which these have nothing to do with."
  (aesthetics-where #(= :positional (:category %))))

(def literal-to-column-aesthetics
  "The aesthetics whose literal value becomes a constant column in the
   draft. A subset of `positional-aesthetics` -- see `:literal->column?`."
  (aesthetics-where :literal->column?))

(def column-only-aesthetics
  "Aesthetics that read a column and have no reading for a value."
  (aesthetics-where #(and (:column? %) (not (:value? %)))))

(def continuous-column-aesthetics
  "Aesthetics whose column must hold numbers, because what they encode
   is a magnitude. `:color` is not among them: a categorical color
   column is a palette, which is the reading these three lack."
  (aesthetics-where #(and (:column? %) (not (:categorical-column? %)))))

(def channel->scale-key
  "Channel keyword to the key a draft layer holds its scale spec under."
  (into {} (for [[k {:keys [scale-key]}] aesthetic-registry
                 :when scale-key]
             [k scale-key])))

(def channel-scale-types
  "The scale types each channel accepts.

   `:categorical` places a value by which category it is, which the two
   channels drawn as an axis can do and `:shape` can only do. The
   continuous visual channels have no categorical reading: a size or an
   opacity is a magnitude, and a symbol is not."
  {:x     #{:linear :log :categorical}
   :y     #{:linear :log :categorical}
   :color #{:linear :log}
   :fill  #{:linear :log}
   :size  #{:linear :log}
   :alpha #{:linear :log}
   :shape #{:categorical}})

(defn default-scale-type
  "The scale type an aesthetic falls back on when no scope named one.

   Derived from `channel-scale-types` rather than listed again: an
   aesthetic with no continuous reading is placed by category, and
   every other one is linear.

   A spec that names no type is not an opinion that the scale is
   linear, so the fallback is applied once, after every scope has
   accumulated, rather than where a scale is written."
  [aesthetic]
  (let [accepted (get channel-scale-types aesthetic)]
    (if (and accepted (not (contains? accepted :linear)))
      :categorical
      :linear)))

(def channel-scale-options
  "The scale-spec keys each channel reads, beside `:type` and
   `:domain`, which every scale that has a type has.

   A key a channel does not read is refused where it is written rather
   than dropped in silence.

   `:range` and `:values` are the two shapes the output set takes.
   `:range` is spanned between two ends: an interval of radii for
   `:size`, an interval of opacities for `:alpha`, a gradient for the
   two colour channels. `:values` is enumerated and assigned by index:
   the marker symbols `:shape` draws, the colours a categorical
   `:color` column is drawn in. `:color` reads both, because a colour
   column may be of either type, and its type decides which is read;
   `:fill` takes only numbers, so it reads `:range` alone. An axis
   reads neither -- the panel decides what it spans.

   `:by` asks how a value spreads across a range in the quantity the
   mark draws it as, correcting for the way ink grows with that
   quantity. An opacity has no shape and so no area to correct for,
   which leaves the three methods one function: `:size` reads `:by`
   and `:alpha` does not.

   `:from-zero` asks a different question -- whether the drawn quantity
   is proportional to the value -- and an opacity can answer it, since
   zero opacity is absence the way zero area is. A colour cannot: the
   low end of a gradient is a colour rather than an absence, so there
   is no quantity there to be proportional to anything. `:midpoint` is
   what the two colour channels anchor with instead. It names the value
   the middle of the range is drawn at, which is what centres a
   diverging gradient on zero rather than halfway along the data.

   `:breaks`, `:tick-labels`, `:n-ticks` and `:tick-spacing` place and
   word tick marks, so all four belong to the two channels drawn as an
   axis. `:n-ticks` asks for about that many ticks and `:tick-spacing`
   for about that much room each; a numeric axis reads whichever is
   named, and a categorical one is ticked at its categories, which
   `:n-ticks` thins.

   `:label` titles what explains the scale to a reader: the axis for
   `:x` and `:y`, the legend for the rest. Every channel with a scale
   reads it. (`:label` is that title; `:tick-labels` is the text drawn
   at the ticks of an axis.)"
  {:x     #{:breaks :tick-labels :n-ticks :tick-spacing :label}
   :y     #{:breaks :tick-labels :n-ticks :tick-spacing :label}
   :color #{:range :values :midpoint :label}
   :fill  #{:range :midpoint :label}
   :shape #{:values :label}
   :size  #{:range :by :from-zero :label}
   :alpha #{:range :from-zero :label}})

;; ---- What a scale takes, as one ordered table ----
;;
;; `channel-scale-types` and `channel-scale-options` are sets, so
;; neither carries an order to show a reader. These three vectors
;; supply one, and `aesthetic-scales` below joins the three tables into
;; the shape a reference table wants. The book builds its own table
;; from it rather than restating it by hand, which is how the two stay
;; agreed.

(def scale-type-order
  "Display order for scale types: the continuous readings, then the
   categorical one."
  [:linear :log :categorical])

(def scale-spec-key-order
  "Display order for the per-aesthetic scale spec keys: the output set
   an aesthetic is drawn from, then how a value spreads across it and
   where it is anchored, then the keys that place and word tick marks,
   then the axis title."
  [:range :values :by :from-zero :midpoint
   :breaks :tick-labels :n-ticks :tick-spacing :label])

(def scale-bearing-aesthetic-order
  "Display order for the aesthetics that have a scale: the two drawn as
   an axis first, then the appearance aesthetics. Aesthetics sharing a
   set of capabilities are adjacent, so a table can group them."
  [:x :y :size :alpha :color :fill :shape])

(defn- in-canonical-order
  "Sort `items` by their place in `order`. Anything `order` does not
   list follows, by name, so a newly added entry shows up at the end
   rather than disappearing."
  [order items]
  (let [rank (zipmap order (range))]
    (vec (sort-by (juxt #(get rank % (count order)) str) items))))

(def aesthetic-scales
  "What each aesthetic's scale accepts, one entry per aesthetic that
   has a scale, in display order.

   Each entry carries `:aesthetic`, the `:types` it can be read
   through, and the spec `:keys` it reads beside `:type` and `:domain`,
   which belong to every scale. Both are ordered vectors.

   Derived from `channel-scale-types` and `channel-scale-options` so
   that documentation cannot drift from what the validators enforce."
  (vec (for [a (in-canonical-order scale-bearing-aesthetic-order
                                   (keys channel-scale-types))]
         {:aesthetic a
          :types (in-canonical-order scale-type-order (get channel-scale-types a))
          :keys  (in-canonical-order scale-spec-key-order
                                     (get channel-scale-options a #{}))})))

(defn scale-option-key
  "The plot option that is the outer scope of scale spec key `k` on
   `aesthetic`: the two names joined by a hyphen. `:label` on `:x` is
   `:x-label`, `:values` on `:color` is `:color-values`, `:midpoint` on
   `:fill` is `:fill-midpoint`.

   Computed rather than listed, so an option and the spec key it is the
   outer scope of cannot drift apart, and so a new spec key gets its
   option spelling by being named."
  [aesthetic k]
  (keyword (str (name aesthetic) "-" (name k))))

(defn scale-setting
  "The effective value of scale setting `k` for `aesthetic`: what the
   scale spec says, and failing that what the plot option of the same
   name says.

   A setting belonging to one aesthetic is written in two places: in a
   scale spec, and as the plot option `scale-option-key` names. The
   spec is written further in, so it wins, and it wins by naming the
   key at all -- a spec giving the key the value nil means nil, and
   does not fall back to the option.

   `spec` is the resolved scale spec, nil where none was written.
   `options` is the map the plot option is looked up in: the resolved
   configuration for a setting that has a configuration level, and a
   pose's own options for one that is per-plot, such as a title."
  [aesthetic k spec options]
  (if (contains? spec k)
    (get spec k)
    (get options (scale-option-key aesthetic k))))

(def drawn-range-options
  "The scale-spec keys that describe how a value spreads across what a
   mark draws it as. Only some channels read them; see
   `channel-scale-options`."
  #{:range :by :from-zero})

(def channel-ranges
  "What a continuous appearance channel spans when its scale names no
   `:range`, in the quantity the mark draws it as: `:size` in drawing
   units of radius, `:alpha` in opacity.

   Stated here because the marks and the legend both need it and each
   held its own copy -- the same defect the shared mapper exists to
   prevent, one level up. The alpha range is ggplot2's."
  {:size  [2.0 8.0]
   :alpha [0.1 1.0]})

(def legend-bearing-aesthetics
  "Aesthetics that produce a legend at render time."
  (set (aesthetics-where :legend?)))

;; ---- Shape Symbols ----

(def shape-syms
  "Shape symbols assigned to categorical shape values, in assignment
   order. A plot with more categories than symbols reuses them from the
   start of the list, which makes two categories indistinguishable --
   plan time warns before that happens."
  [:circle :square :triangle :diamond :triangle-down :plus :cross])

(def legend-swatch-size
  "Side length of the colored key drawn beside a legend entry (square)."
  8)

;; ---- Color Helpers ----

(defn hex->rgba
  "Convert any color representation to [r g b a] in 0-1 range.
   Accepts hex strings (#RGB, #RRGGBB, #RRGGBBAA, or without #),
   named color strings (\"red\", \"steelblue\"), keywords (:red, :darkblue),
   or any value that clojure2d.color/to-color understands.

   Converting is not deciding. `names-a-color?` answers the narrower
   question of whether a value was *meant* as a color, and refuses the
   bare hex this function accepts, because a three-letter string is a
   mistyped column name more often than it is a shade."
  [color]
  (if (and (string? color) (not (.startsWith ^String color "#")))
    ;; Non-# string: try as hex first, then as named color keyword
    (let [cc (try (c/to-color color)
                  (catch NumberFormatException _ nil))]
      (if cc
        (c2d->rgba cc)
        (let [cc (c/to-color (keyword color))]
          (if cc
            (c2d->rgba cc)
            (throw (ex-info (str "Unknown color: \"" color
                                 "\". Use a hex string like \"#FF0000\" or a CSS color name like \"red\".")
                            {:color color}))))))
    (c2d->rgba color)))

(defn- spread-idx
  "Map category index `i` of `n` categories into a palette index across
   `p` palette entries, spreading evenly so that n categories against
   a p-entry palette pick values stretched across the whole range
   instead of the first n entries.

   Currently unused by `color-for` — the canonical path for continuous
   color is a colour scale's `:range`, not its `:values` with a
   gradient name. This
   helper stays as a dormant utility in case we later expose a
   `:palette-sampling :spread` escape hatch."
  [^long i ^long n ^long p]
  (cond
    (<= p 0) 0
    (<= n 1) (mod i p)
    (>= n p) (mod i p)
    :else (mod (long (Math/round (* (double i) (/ (double (dec p)) (double (dec n))))))
               p)))

(defn color-for
  "Look up the color for a categorical value from the palette.
   Returns [r g b a] in 0-1 range.
   palette can be: nil (default), a keyword (any clojure2d palette name),
   a vector of hex strings, or a map of {category-value color}.

   Map lookup is tolerant of string/keyword mismatch: {:setosa \"#F00\"}
   matches both :setosa and \"setosa\" data values.

   For keyword/vector palettes, the i-th category gets the i-th
   palette entry (wrapping modulo the palette size). This preserves
   the authorial ordering of designed-categorical palettes like
   :set1, :dark2, :tableau-10. If you want a continuous color ramp
   (viridis, inferno, etc.) for a numeric column, use the colour
   scale's `:range` instead -- that's the canonical gradient path and
   interpolates the full color range smoothly."
  ([categories val]
   (color-for categories val nil))
  ([categories val palette]
   (let [raw-idx (if categories (.indexOf ^java.util.List categories val) -1)
         idx (if (neg? raw-idx) 0 raw-idx)]
     (if (map? palette)
       ;; Explicit mapping: look up value, try alternate string/keyword form,
       ;; fall back to index in default palette
       (let [cv (or (get palette val)
                    (cond
                      (keyword? val) (get palette (name val))
                      (string? val) (get palette (keyword val))
                      :else nil))]
         (if cv
           (hex->rgba cv)
           (let [pal (resolve-palette default-palette-name)]
             (c2d->rgba (nth pal (mod idx (count pal)))))))
       ;; Index-based: keyword → c/palette, vector → use directly, nil → default
       (cond
         (keyword? palette)
         (let [pal (resolve-palette palette)]
           (c2d->rgba (nth pal (mod idx (count pal)))))
         (sequential? palette)
         (hex->rgba (nth palette (mod idx (count palette))))
         :else
         (let [pal (resolve-palette default-palette-name)]
           (c2d->rgba (nth pal (mod idx (count pal))))))))))

;; ---- Continuous Color ----

(defn- wrap-gradient
  "Wrap a clojure2d gradient function to return [r g b a] in 0-1 range."
  [g]
  (fn [t] (c2d->rgba (g t))))

(def gradient-color
  "Default gradient function (dark blue → light blue, matching ggplot2).
   Takes t in [0,1], returns [r g b a] 0-1."
  (wrap-gradient (c/gradient [(c/to-color "#132B43") (c/to-color "#56B1F7")])))

(def diverging-color
  "Diverging gradient function (RdBu). Takes t in [0,1], returns [r g b a] 0-1."
  (wrap-gradient (c/gradient :grDevices/RdBu)))

(def ^:private gradient-aliases
  "Short aliases for common clojure2d gradient names."
  {:viridis :viridis/viridis :inferno :viridis/inferno
   :plasma :viridis/plasma :magma :viridis/magma
   :cividis :viridis/cividis :turbo :viridis/turbo
   :rocket :viridis/rocket :mako :viridis/mako
   :RdBu :grDevices/RdBu :RdYlBu :grDevices/RdYlBu
   :BrBG :grDevices/BrBG :coolwarm :pals/coolwarm})

(def gradient-palette-keywords
  "Set of keywords that resolve to a continuous gradient rather than a
   categorical palette. Users who pass one as a colour scale's
   `:values` almost always meant its `:range` with a numeric color
   column. `plan/warn-palette-wrap!` fires a warning when it sees one
   of these among the values."
  (set (keys gradient-aliases)))

(defn- resolve-gradient-name
  "Resolve a keyword to a clojure2d gradient, trying aliases then direct lookup."
  [k]
  (or (c/gradient (get gradient-aliases k k))
      (c/gradient k)))

(defn names-a-color?
  "True of a value that unmistakably names a color: a `#`-prefixed hex
   string, or a CSS color name as a string or a keyword.

   Deliberately narrower than what `hex->rgba` will convert, which also
   reads a bare `abc` as the hex `#aabbcc`. That extra latitude is fine
   once something has decided the value is a color, and wrong while
   deciding: `abc` and `fff` are far likelier to be mistyped column
   names than colors, and reading them as colors is exactly the silent
   failure asking the vocabulary was meant to prevent."
  [v]
  (boolean
   (cond
     (keyword? v) (some? (c/to-color v))
     (string? v) (if (.startsWith ^String v "#")
                   (try (some? (c/to-color v)) (catch Throwable _ false))
                   (some? (c/to-color (keyword v))))
     :else false)))

(defn gradient-map?
  "True of a map that describes a custom gradient -- one naming at least
   one of `:low`, `:mid` and `:high`.

   A gradient reaches `resolve-gradient-fn` as the `:range` of a colour
   scale, which holds a gradient and nothing else. The check remains
   because a map is also how a whole scale spec is written, and a spec
   read as a gradient would resolve to three default stops: a plot
   asking for a log colour scale would silently change its colours as
   well as its spacing."
  [m]
  (and (map? m)
       (boolean (some #(contains? m %) [:low :mid :high]))))

(defn resolve-gradient-fn
  "Resolve a colour scale's `:range` to a gradient function
   t→[r g b a] (0-1 range).
   nil or :sequential → dark blue to light blue (ggplot2 default).
   :diverging → RdBu.
   keyword → clojure2d gradient name (:inferno, :viridis/plasma, etc.).
   map {:low hex :mid hex :high hex} → custom 3-stop gradient.
   function → used directly.
   Throws on an unrecognized keyword, and on a map naming none of the
   three stops -- that map is a whole scale spec written where a
   gradient belongs, and drawing it as three default stops would change
   a plot's colours without saying so."
  [gradient]
  (cond
    (nil? gradient) gradient-color
    (= :sequential gradient) gradient-color
    (= :diverging gradient) diverging-color
    (fn? gradient) gradient
    (keyword? gradient)
    (if-let [g (resolve-gradient-name gradient)]
      (wrap-gradient g)
      (throw (ex-info (str "Unknown colour :range " gradient
                           ". Use a clojure2d gradient name (e.g. :inferno, :viridis, :plasma)"
                           " or :sequential / :diverging.")
                      {:range gradient})))
    (gradient-map? gradient)
    (let [{:keys [low mid high]
           :or {low "#B2182B" mid "#F7F7F7" high "#2166AC"}} gradient
          g (c/gradient [(c/to-color low) (c/to-color mid) (c/to-color high)])]
      (wrap-gradient g))
    (map? gradient)
    (throw (ex-info (str "A colour :range map names at least one of :low,"
                         " :mid and :high, and " (pr-str gradient)
                         " names none. A whole scale spec goes under"
                         " :scale, not under its own :range.")
                    {:range gradient}))
    :else (throw (ex-info (str "Invalid colour :range: " (pr-str gradient)
                               ". Expected nil, a keyword, a {:low :mid :high}"
                               " map, or a function.")
                          {:range gradient}))))

(defn scale-gradient-fn
  "The gradient function `aesthetic` reads a numeric column through.

   One resolution, used by the marks and by the legend alike, so the
   bar a reader matches a colour against is drawn from the same
   function the marks were. Takes the `:range` the scale spec names,
   and failing that the `:color-range` or `:fill-range` plot option."
  [aesthetic spec cfg]
  (resolve-gradient-fn (scale-setting aesthetic :range spec cfg)))

(defn normalize-midpoint
  "Remap a value v from [vmin, vmax] to [0,1] with optional midpoint.
   Without midpoint: linear (v-vmin)/(vmax-vmin).
   With midpoint: values below midpoint → [0, 0.5], above → [0.5, 1.0]."
  [v vmin vmax midpoint]
  (if midpoint
    (let [v (double v) vmin (double vmin) vmax (double vmax) mid (double midpoint)]
      (cond
        (<= v vmin) 0.0
        (>= v vmax) 1.0
        (<= v mid) (if (<= mid vmin) 0.5 (* 0.5 (/ (- v vmin) (- mid vmin))))
        :else (if (>= mid vmax) 0.5 (+ 0.5 (* 0.5 (/ (- v mid) (- vmax mid)))))))
    (let [span (- (double vmax) (double vmin))]
      (if (<= span 0) 0.5 (/ (- (double v) (double vmin)) span)))))

(defn normalize-continuous
  "Remap a value v from [vmin, vmax] to [0,1] using a scale-type aware
   transform. :linear (default) uses normalize-midpoint with the optional
   midpoint. :log uses log10 endpoints; midpoint is ignored under :log.

   The result is clamped to [0,1], which is the whole gradient. Every
   caller reads it as a place along one, and the endpoints used to come
   from the data itself, so nothing could fall outside. A `:domain`
   narrower than the data can, and a value beyond it is drawn at the
   nearer end of the gradient -- the same answer `:size` and `:alpha`
   give a value outside their domain."
  [scale-type v vmin vmax midpoint]
  (-> (if (= scale-type :log)
        (let [vl   (Math/log10 (max 1e-300 (double v)))
              minl (Math/log10 (max 1e-300 (double vmin)))
              maxl (Math/log10 (max 1e-300 (double vmax)))
              span (- maxl minl)]
          (if (<= span 0) 0.5 (/ (- vl minl) span)))
        (normalize-midpoint v vmin vmax midpoint))
      (max 0.0)
      (min 1.0)))

;; ---- Name Formatting ----

(defn fmt-name
  "Format a column name for display: :sepal-length becomes sepal length.
   Hyphens and underscores turn into spaces in a keyword or a symbol,
   neither of which can hold a space, so a separator in one stands in for
   a word break. A string is left as written -- a string can hold a space,
   so a hyphen in one was chosen rather than substituted, as in
   Cost-Benefit Ratio. Any other name -- a dataset built without column
   names gets integer ones -- formats as its printed form, so
   auto-labelling does not crash on it."
  [k]
  (if (instance? clojure.lang.Named k)
    (str/replace (name k) #"[-_]" " ")
    (str k)))

(defn fmt-category-label
  "Format a category value (keyword, string, number, etc.) for display.
   Used for axis tick labels, legend entries, facet strip labels, and any
   other user-visible category text.

   Formats by the same rule as `fmt-name`, so a keyword category reads as
   words: :not-applicable becomes not applicable. Hyphenated keyword
   categories are common and are nearly always meant as words, which is
   what makes the rule worth applying to data as well as to names.

   Two keyword categories differing only by separator -- :a-b and :a_b --
   therefore format alike. On a categorical axis that combines them, since
   `impl/stat.clj` maps the column through this function before grouping;
   `format-category-column` there warns when it happens. Convert such a
   column to strings to keep the values apart."
  [v]
  (fmt-name v))

(defn fmt-root
  "`format` pinned to `Locale/ROOT`.

   `clojure.core/format` reads the JVM's default locale, so a tick that
   read 1.2 here read 1,2 on a German JVM -- and beside a grouped axis
   the same comma then meant a decimal point on one axis and a thousands
   separator on the other. `render/svg.clj` pinned its coordinates in
   0.2.2 (PR #3); the label text is pinned for the same reason. Which
   glyph a plot draws for the point is chosen with `:decimal-separator`,
   not inherited from the machine."
  [fmt v]
  (String/format java.util.Locale/ROOT fmt (to-array [v])))

(defn- absent?
  "Only nil and the empty string count as no separator: a single space is
   a valid one, the convention in much of Europe."
  [separator]
  (or (nil? separator) (= "" (str separator))))

(defn- in-threes
  "Digits of an integer part, joined in groups of three from the right."
  [digits separator]
  (->> digits
       reverse
       (partition-all 3)
       (map (comp str/join reverse))
       reverse
       (str/join separator)))

;; A formatted number, split into the parts the two separators act on:
;; sign, integer digits, an optional fractional part, and whatever
;; follows (an exponent, or nothing). A string that does not start with
;; digits does not match at all, which is what lets a category name pass
;; through these functions untouched.
(def ^:private number-parts-re #"(-?)(\d+)(?:\.(\d+))?(.*)")

(defn number-separators
  "The two separators a plot writes its numbers with, read off a resolved
   config as `{:thousands ... :decimal ...}`. Threaded as one value
   rather than as two scalars, because it passes through six layers of
   tick layout on its way to the formatter."
  [cfg]
  {:thousands (:thousands-separator cfg)
   :decimal (:decimal-separator cfg)})

(defn fmt-number
  "Rewrite an already-formatted number string with a plot's separators:
   `:thousands` between each group of three digits in the integer part,
   `:decimal` in place of the point.

   Every number reaching here was formatted under `Locale/ROOT`, so its
   decimal point is always a `.` and the two separators can be chosen
   independently: `{:thousands \".\" :decimal \",\"}` turns 1234.5 into
   1.234,5. Either one absent leaves that part as it was, so the default
   of neither leaves the string alone.

   Returns `s` unchanged when it does not begin with digits, so a
   category name passes through."
  [s {:keys [thousands decimal]}]
  (if (and (absent? thousands) (absent? decimal))
    s
    (if-let [[_ sign digits frac tail] (re-matches number-parts-re (str s))]
      (str sign
           (if (absent? thousands) digits (in-threes digits thousands))
           (when frac (str (if (absent? decimal) "." decimal) frac))
           tail)
      s)))

(defn group-digits
  "Insert `separator` between three-digit groups in the integer part of an
   already-formatted number string: 462389 with a comma gives 462,389.
   A leading sign, a fractional part, and any exponent are left alone.

   The thousands half of `fmt-number`, for a caller that has only that
   separator to hand."
  [s separator]
  (fmt-number s {:thousands separator}))

(defn fmt-value-label
  "Format a data value for display as text on a plot, writing a number
   with the plot's `separators`. Non-numeric values format as
   `fmt-category-label` does."
  [v separators]
  (cond-> (fmt-category-label v)
    (number? v) (fmt-number separators)))

(defn fmt-legend-number
  "Format a legend's numeric value: an integral value loses its trailing
   .0, and the digits are grouped per `:thousands-separator`, so a legend
   reads the same way as the axis ticks beside it.

   Here rather than beside the renderer because the layout has to
   measure the same string the renderer will draw. A size legend's
   entries carry a value and no label, so measuring the label read
   nothing and the column was sized as if the numbers were not there."
  [v cfg]
  (let [d (double v)
        s (if (== d (Math/floor d)) (str (long d)) (str v))]
    (fmt-number s (number-separators cfg))))

;; ---- Configuration Precedence Chain ----
;;
;; Resolved with precedence (highest to lowest):
;;   1. plot options (passed to pj/options, pj/plot, pj/plan, etc.)
;;   2. binding *config* (thread-local override)
;;   3. set-config! (global mutable state)
;;   4. plotje.edn (project root or classpath)
;;   5. library defaults (plotje-defaults.edn)

(def ^:private library-defaults
  "Library defaults loaded from plotje-defaults.edn on classpath.
   Falls back to the static `defaults` and `theme` maps if the resource is missing."
  (delay
    (if-let [r (io/resource "plotje-defaults.edn")]
      (edn/read-string (slurp r))
      (merge defaults {:theme theme}))))

(def ^:dynamic *config*
  "Dynamic var for thread-local config overrides.
   Bind to a map to override any config keys for the current thread.
   (binding [defaults/*config* {:theme {:bg \"#FFF\"}}] ...)"
  nil)

(defonce ^:private config-atom
  (atom nil))

;; Defined below, once `config-key-docs` -- the set it validates
;; against -- exists. Only ever called at runtime, so the forward
;; reference resolves.
(declare validate-config-keys!)

(defn set-config!
  "Set global config overrides. Persists across calls until reset.
   (set-config! {:color-values :dark2 :theme {:bg \"#FFFFFF\"}})
   (set-config! nil)  — reset to defaults"
  [m]
  (validate-config-keys! "pj/set-config!" m)
  (reset! config-atom m))

(def ^:private edn-cache
  "TTL cache for plotje.edn (1 second)."
  (atom {:value nil :timestamp 0}))

;; The last plotje.edn content checked, so a file naming a key Plotje
;; does not read is reported when it changes rather than once a second
;; forever. `defonce` takes no docstring, hence the comment.
(defonce ^:private edn-validated
  (atom ::unread))

(defn- read-plotje-edn
  "Read plotje.edn from classpath or the current working directory.
   Returns nil if the file does not exist. Cached with 1-second TTL."
  []
  (let [{:keys [value timestamp]} @edn-cache
        now (System/currentTimeMillis)]
    (if (< (- now timestamp) 1000)
      value
      (let [from-cp (io/resource "plotje.edn")
            from-cwd (let [f (io/file "plotje.edn")]
                       (when (.exists f) f))
            source (or from-cp from-cwd)
            v (when source (edn/read-string (slurp source)))]
        (reset! edn-cache {:value v :timestamp now})
        (when (not= v @edn-validated)
          (reset! edn-validated v)
          (validate-config-keys! "plotje.edn" v))
        v))))

(def config-key-docs
  "Documentation metadata for configuration keys.
   Each entry maps a key to [category description]."
  {:width ["Layout" "Plot width"]
   :height ["Layout" "Plot height"]
   :margin ["Layout" "Margin around single-panel plots"]
   :margin-multi ["Layout" "Margin around multi-panel plots"]
   :panel-size ["Layout" "Default panel size for faceted/multi-variable grids"]
   :legend-width ["Layout" "Width reserved for the legend column"]
   :legend-position ["Layout" "Legend placement — :right, :bottom, :top, or :none"]
   :theme ["Theme" "Nested map {:bg :grid :font-size} — visual identity"]
   :label-font-size ["Typography" "Font size for axis labels"]
   :title-font-size ["Typography" "Font size for the plot title"]
   :strip-font-size ["Typography" "Font size for facet strip labels"]
   :point-radius ["Points" "Default point radius"]
   :point-opacity ["Points" "Default point opacity (0.0–1.0)"]
   :point-stroke ["Points" "Point border stroke color (\"none\" to disable)"]
   :point-stroke-width ["Points" "Point border stroke width"]
   :bar-opacity ["Bars & Lines" "Default bar fill opacity"]
   :line-width ["Bars & Lines" "Default line stroke width"]
   :grid-stroke-width ["Bars & Lines" "Grid line stroke width"]
   :annotation-stroke ["Annotations" "Stroke color for annotation marks"]
   :annotation-dash ["Annotations" "Dash pattern [dash gap] for annotation lines"]
   :band-opacity ["Annotations" "Opacity for confidence bands"]
   :x-tick-spacing ["Ticks" "Target spacing, in drawing units, between ticks on the x axis"]
   :y-tick-spacing ["Ticks" "Target spacing, in drawing units, between ticks on the y axis"]
   :x-tick-angle ["Ticks" "Rotation angle for x-axis tick labels in degrees (0 = horizontal, -45 = common diagonal)"]
   :x-tick-label-pad ["Ticks" "Extra vertical space, in drawing units, reserved below panels for angled x-tick labels, added on top of :label-offset. When nil, auto-computed from :x-tick-angle. When 0, no extra space is reserved and rotated labels may be clipped by the SVG boundary."]
   :bin-method ["Statistics" "Histogram bin count method (:sturges, :sqrt, :rice, :fd)"]
   :domain-padding ["Statistics" "Fractional padding added to numeric domains"]
   :label-offset ["Labels" "Pixel offset for axis labels from the axis"]
   :fit-text-domain ["Labels" "When true (the default), a numeric domain is widened so that text and label marks near its edge are drawn in full instead of being cut off at the panel boundary. A domain pinned with pj/scale is never widened"]
   :decimal-separator ["Labels" "String drawn in place of the decimal point of a number that measures, in the same text :thousands-separator groups (a comma, with a point for the thousands, gives the European 1.234,5). Numbers are formatted under Locale/ROOT, so the two separators are chosen here rather than inherited from the JVM's locale. nil (the default) leaves the point as a point"]
   :thousands-separator ["Labels" "String inserted between three-digit groups of a number that measures (a comma gives 462,389): numeric axis tick labels, text/label mark content, and the values a size or alpha legend prints. A number that names is left alone -- category names, colour and shape legend labels, facet strips, and a categorical axis whose categories happen to be numbers. nil (the default) leaves everything ungrouped"]
   :title-offset ["Labels" "Pixel offset for the title from the top"]
   :strip-height ["Labels" "Height of facet strip label bars"]
   :validate ["Behavior" "When true, validate plans against Malli schema"]
   :strict ["Behavior" "When true, throw on unknown option keys instead of warning and stripping"]
   :default-color ["Behavior" "Fallback color when no color mapping is set"]
   :color-values ["Color" "The colours a categorical :color column is drawn in, as a palette name, a vector of colours, or a map from category to colour. The outermost scope of :values in a :color scale spec, so a spec written on a mapping or a layer wins over it"]
   :color-range ["Color" "The gradient a numeric :color column is read through — :sequential, :diverging, a gradient name, a {:low :mid :high} map, or a function. The outermost scope of :range in a :color scale spec, so a spec written on a mapping or a layer wins over it"]
   :color-midpoint ["Color" "The value the middle of the :color gradient is drawn at, which centres a diverging gradient there rather than halfway along the data. The outermost scope of :midpoint in a :color scale spec"]
   :fill-range ["Color" "The gradient a numeric :fill column is read through, in the same forms as :color-range. The outermost scope of :range in a :fill scale spec"]
   :fill-midpoint ["Color" "The value the middle of the :fill gradient is drawn at. The outermost scope of :midpoint in a :fill scale spec"]
   :tooltip ["Interaction" "Enable hover tooltips (truthy value)"]
   :brush ["Interaction" "Enable drag-to-select brush (truthy value)"]
   :format ["Output" "Render format — :svg (default)"]})

(def plot-option-docs
  "Documentation for plot-level option keys.
   These are accepted by pj/options, pj/plan, and pj/plot but are
   inherently per-plot (text content or nested config override).
   Each entry maps a key to [category description]."
  {:title ["Content" "Plot title string"]
   :subtitle ["Content" "Plot subtitle string"]
   :caption ["Content" "Plot caption string (bottom)"]
   :x-label ["Content" "X-axis title, overriding the inferred column name. The outermost scope of the same setting `:label` names in an `:x` scale spec, so a spec written on a mapping or a layer wins over it"]
   :y-label ["Content" "Y-axis title, overriding the inferred column name. The outermost scope of the same setting `:label` names in a `:y` scale spec, so a spec written on a mapping or a layer wins over it"]
   :color-label ["Content" "Color legend title, overriding the inferred column name. The outermost scope of the same setting `:label` names in a `:color` scale spec, so a spec written on a mapping or a layer wins over it"]
   :fill-label ["Content" "Fill legend title, overriding the inferred column name; used by tile fills, density-2d and bin2d. The outermost scope of the same setting `:label` names in a `:fill` scale spec, so a spec written on a mapping or a layer wins over it"]
   :size-label ["Content" "Size legend title, overriding the inferred column name. The outermost scope of the same setting `:label` names in a `:size` scale spec, so a spec written on a mapping or a layer wins over it"]
   :alpha-label ["Content" "Alpha legend title, overriding the inferred column name. The outermost scope of the same setting `:label` names in an `:alpha` scale spec, so a spec written on a mapping or a layer wins over it"]
   :shape-label ["Content" "Shape legend title, overriding the inferred column name. The outermost scope of the same setting `:label` names in a `:shape` scale spec, so a spec written on a mapping or a layer wins over it"]
   :panel-width ["Layout" "Pin panel width (escape hatch; :width becomes derived total)"]
   :panel-height ["Layout" "Pin panel height (escape hatch; :height becomes derived total)"]
   :scales ["Layout" "Facet scale coordination — :shared (default), :free, :free-x, :free-y"]
   :share-scales ["Layout" "Composite scale coordination — set of axes (#{:x}, #{:y}, or #{:x :y}) shared across composite cells"]
   :config ["Config" "Nested config map merged into resolved config"]})

(defn config
  "Return the effective resolved configuration as a map.
   Deep-merges: library defaults < plotje.edn < set-config! < *config*.
   Nested maps (e.g. :theme) are merged recursively.
   Useful for inspecting which values are in effect."
  []
  (let [base @library-defaults
        from-edn (read-plotje-edn)
        from-atom @config-atom
        from-binding *config*]
    (cond-> base
      from-edn (kindly/deep-merge from-edn)
      from-atom (kindly/deep-merge from-atom)
      from-binding (kindly/deep-merge from-binding))))

(defn- backfill-nil-theme-values
  "Replace any nil entries in the resolved theme with the library defaults.
   A user passing `{:theme {:bg nil}}` through `with-config`/plot-opts
   would otherwise see their nil merged on top of the default and reach
   renderers that call `hex->rgba`, causing NPEs. Backfilling keeps the
   baseline visible while still honoring non-nil overrides."
  [cfg]
  (let [default-theme (:theme @library-defaults)]
    (update cfg :theme
            (fn [t]
              (reduce-kv (fn [m k v] (if (nil? v) (assoc m k (get default-theme k)) m))
                         t
                         t)))))

(defn- config-strict?
  "Whether an unreadable configuration key throws rather than warns.

   Resolved without calling `config`, because `plotje.edn` is checked
   while `config` is building its answer and would recur."
  [m]
  (cond
    (contains? m :strict) (:strict m)
    (contains? *config* :strict) (:strict *config*)
    (contains? @config-atom :strict) (:strict @config-atom)
    :else (:strict @library-defaults)))

(defn validate-config-keys!
  "Report configuration keys Plotje does not read, naming `where` they
   were written. Warns, or throws under `:strict`.

   `pj/options` has always refused a key it does not read, and the
   configuration path did not. A key a release removed -- `:palette`,
   `:color-scale`, `:tick-spacing-x` -- written in `plotje.edn` or
   passed to `set-config!`, `with-config` or the `:config` option was
   merged into the resolved configuration, read by nothing, and drew
   the default without a word. Upgrading a project reverted its palette
   in silence.

   The key is left in the map rather than stripped: nothing reads it
   either way, and the resolved configuration is what `pj/config`
   reports, so removing entries there would hide what was written.

   Returns `m`, so it can sit in a threading position."
  [where m]
  (when (map? m)
    (let [accepted (set (keys config-key-docs))
          unknown (remove accepted (keys m))]
      (when (seq unknown)
        (let [msg (str where " does not recognize configuration key(s): "
                       (vec (sort unknown)) "."
                       "\n  Accepted: " (vec (sort accepted)))]
          (if (config-strict? m)
            (throw (ex-info msg {:caller where
                                 :unknown (vec (sort unknown))
                                 :accepted accepted}))
            (println (str "Warning: " msg)))))))
  m)

(def ^:private flat-config-keys
  "Config keys that are forwarded as flat scalars from plot-opts to cfg.
   :theme is excluded (deep-merged separately) and :config is excluded
   (it's the nested escape hatch, not a config key itself)."
  (disj (set (keys config-key-docs)) :theme :config))

(defn resolve-config
  "Resolve config with plot options deep-merged on top of the precedence chain.
   Plot options have the highest priority. Any key from `config-key-docs`
   passed directly in plot-opts is forwarded to the resolved cfg. Nested
   maps (:theme) are merged recursively. The `:config` key is a deep-merge
   escape hatch for arbitrary overrides."
  [plot-opts]
  (let [cfg (config)]
    (when (:panel-size plot-opts)
      (println "Warning: :panel-size is a legacy option and no longer used by the layout pipeline. Use :panel-width and/or :panel-height, or :width/:height for total SVG size."))
    (-> (if (seq plot-opts)
          (let [;; Deep-merge the nested :config escape hatch first (lowest priority
                ;; among plot-opts, highest priority over the cfg chain).
                cfg (if-let [nested (:config plot-opts)]
                      (kindly/deep-merge
                       cfg (validate-config-keys! "The :config option" nested))
                      cfg)
                ;; Deep-merge :theme (map value, not scalar)
                cfg (if-let [th (:theme plot-opts)]
                      (update cfg :theme kindly/deep-merge th)
                      cfg)]
            ;; Forward every flat config key the user passed.
            (reduce-kv (fn [m k v]
                         (if (and (contains? flat-config-keys k)
                                  (some? v))
                           (assoc m k v)
                           m))
                       cfg
                       plot-opts))
          cfg)
        backfill-nil-theme-values)))
