(ns scicloj.plotje.impl.text
  "Estimated pixel extent of drawn text.

   Text is the one mark whose size is fixed in pixels rather than
   derived from the data, so two stages need the same answer to how
   much room a string takes: the renderer, which places the glyphs and
   the box behind them, and the plan, which widens a numeric domain so
   a label near an edge is drawn in full. Both read the estimate from
   here, so the two cannot drift apart.

   The estimate is a per-character advance of 0.6 times the font size.
   Real glyph metrics are backend-specific -- the SVG target has none
   at all -- and a slight over-estimate is the safe direction for both
   callers: the renderer draws a box a shade wide, and the plan leaves
   a shade more room than the glyphs need.

   A third caller cannot use an average at all. Where a label is pushed
   away from an edge to keep it whole, reading low by a pixel is what
   cuts the glyph the move was meant to save, so that caller measures
   with `max-text-width` instead.")

(def char-advance
  "Width of one character as a fraction of the font size."
  0.6)

(def max-char-advance
  "Upper bound on the width of one character as a fraction of the font
   size. Which font a backend draws with is not known here: a browser
   renders the SVG with whatever it resolves the generic sans family
   to, and Java2D resolves its own logical font through the operating
   system. Digits are the widest thing a tick label is usually made of
   and run to 0.636 of the font size in DejaVu Sans and Verdana, well
   past `char-advance`, so a bound has to sit above that."
  0.7)

(def fit-pad
  "Gap in drawing units left between a text mark and the edge of the
   panel when the domain is widened to fit it. Without it the domain is
   fitted exactly and the last glyph touches the panel edge."
  4)

(def box-pad-x
  "Horizontal gap between a text mark and the edge of its box, in pixels."
  3)

(def box-pad-y
  "Vertical gap between a text mark and the edge of its box, in pixels."
  2)

(defn text-width
  "Estimated pixel width of the string `s` drawn at `font-size`."
  [font-size s]
  (* (count s) (double font-size) char-advance))

(defn max-text-width
  "Upper bound on the pixel width of the string `s` drawn at `font-size`,
   for a caller that must not read low whatever font the backend picks."
  [font-size s]
  (* (count s) (double font-size) max-char-advance))

(defn anchor-offset
  "Pixel offset to add to a text origin (top-left) so the anchored part of
   the text lands on the data point. `text-w`/`text-h` are estimated glyph
   box dimensions. `align-x` (default :left) is :left/:center/:right;
   `align-y` (default :center) is :top/:center/:bottom, data-oriented so
   :top puts the text's top edge at the point (text extends downward)."
  [align-x align-y text-w text-h]
  [(case (or align-x :left)
     :left   0.0
     :center (- (/ (double text-w) 2.0))
     :right  (- (double text-w)))
   (case (or align-y :center)
     :top    0.0
     :center (- (/ (double text-h) 2.0))
     :bottom (- (double text-h)))])

(defn extent
  "Pixel offsets from a text mark's anchor point to the four edges of what
   the mark draws, as [left right top bottom] in screen direction: left
   and top are at most zero, right and bottom at least zero. `style` is a
   plan layer's style map, so the box padding counts only when the mark
   carries a box."
  [{:keys [font-size align-x align-y box]} label]
  (let [fsize (double (or font-size 10))
        w (text-width fsize label)
        [dx dy] (anchor-offset align-x align-y w fsize)
        px (if box box-pad-x 0)
        py (if box box-pad-y 0)]
    [(- dx px) (+ dx w px) (- dy py) (+ dy fsize py)]))
