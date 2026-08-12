;; # Draft: bars painting over their own text labels
;;
;; Motivation. The R `explore` package's `explore_all()` renders each
;; categorical variable as a horizontal percentage bar with a numeric
;; label sitting on the bar (for example "33.3" on each Species bar).
;; A collaborator is reproducing that style and observes that the bars
;; cover the text and hide it. This draft reproduces the behavior in
;; Plotje and traces where the labels end up underneath the bars.
;;
;; This notebook is exploratory -- it is not in chapters.edn and is not
;; part of the published book.

(ns draft-label-occlusion
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.plan :as plan]))

;; ## The data
;;
;; Three species, each at 33.3 percent -- the same shape as the Species
;; panel in explore_all(iris).

(def species-data
  {:species ["setosa" "versicolor" "virginica"]
   :pct [33.3 33.3 33.3]})

;; ## Reproduction
;;
;; A horizontal percentage bar with a text label at each bar's value.
;; The label is added to the pose AFTER the bar, so the natural reading
;; is "draw the bar, then draw the text on top of it".

(-> species-data
    (pj/lay-bar :species :pct)
    (pj/lay-text :species :pct {:text :pct})
    (pj/coord :flip))

;; With the default bar color (dark gray) and the default text color
;; (dark gray), the "33.3" labels are nearly swallowed by the bars.
;; That alone looks like a contrast problem, so make the bars fully
;; opaque to separate the two hypotheses.

(-> species-data
    (pj/lay-bar :species :pct {:alpha 1.0})
    (pj/lay-text :species :pct {:text :pct})
    (pj/coord :flip))

;; The labels survive here only because they sit at the bar's right end
;; and spill onto the panel background. A light bar fill confirms the
;; text is genuinely under the bar, not merely low-contrast: the part of
;; the label overlapping the fill reads through the bar's translucency,
;; and would vanish entirely at full opacity.

(-> species-data
    (pj/lay-bar :species :pct {:color "#a6cee3"})
    (pj/lay-text :species :pct {:text :pct})
    (pj/coord :flip))

;; ## The pose layer order does not matter
;;
;; Adding the bar first or the text first produces the same plan. The
;; paint order is decided downstream, not by the order of lay-* calls.

(def bar-then-text
  (-> species-data
      (pj/lay-bar :species :pct)
      (pj/lay-text :species :pct {:text :pct})
      (pj/coord :flip)))

(def text-then-bar
  (-> species-data
      (pj/lay-text :species :pct {:text :pct})
      (pj/lay-bar :species :pct)
      (pj/coord :flip)))

;; In both poses the plan's panel layers come out in the same order --
;; the text layer first, the bar (rect) layer second. SVG paints in list
;; order, so the bar is always painted last, on top of the text.

(defn panel-layer-marks [pose]
  (->> (pj/plan pose)
       :panels
       first
       :layers
       (mapv (juxt :mark :position))))

(panel-layer-marks bar-then-text)

(panel-layer-marks text-then-bar)

;; ## Root cause
;;
;; The reordering happens in scicloj.plotje.impl.position/apply-positions.
;; It groups a panel's layers by their position type and re-emits them in
;; a fixed canonical order:

plan/draft->plan ;; (just to anchor the namespace; see position.clj)

(deref #'scicloj.plotje.impl.position/position-order)

;; The bar layer carries position :dodge; the text layer has no
;; explicit position, which apply-positions treats as :identity. Because
;; :identity precedes :dodge in the canonical order, the text is emitted
;; before the bar regardless of how the pose was built -- and the
;; later-painted bar covers it.
;;
;; In other words: data-driven text and label marks (position :identity)
;; are always painted before bar marks (position :dodge or :stack), so a
;; bar occludes a label that lands inside it. The plot author cannot
;; reorder them from the pose, because apply-positions discards pose
;; order in favor of the position-type order.

;; ## Can opacity rescue the label?
;;
;; Because the text is painted first and the bar second, a translucent
;; bar lets the underlying text bleed through. To test this honestly the
;; label must land inside the fill, not at the bar's end where it spills
;; onto the background. Give the label layer its own data with the SAME
;; column names (so it stays in one panel) but a mid-bar value.

(def label-data
  {:species ["setosa" "versicolor" "virginica"]
   :pct [16.0 16.0 16.0]
   :lbl ["33.3" "33.3" "33.3"]})

;; Fully opaque: the mid-bar label is completely gone.

(-> species-data
    (pj/lay-bar :species :pct {:color "#3366aa" :alpha 1.0})
    (pj/lay-text :species :pct {:text :lbl :data label-data})
    (pj/coord :flip))

;; At alpha 0.25 the label reads clearly through the pale fill.

(-> species-data
    (pj/lay-bar :species :pct {:color "#3366aa" :alpha 0.25})
    (pj/lay-text :species :pct {:text :lbl :data label-data})
    (pj/coord :flip))

;; Findings from sweeping alpha (mid-blue fill #3366aa, default dark-gray
;; text): legible at 0.25-0.6, fading by 0.7, gone at the 0.85 default
;; and at 1.0. So opacity is a real but poor remedy:
;;
;; - It only works by washing the fill out, which defeats a saturated
;;   bar look -- the thing the explore style relies on.
;; - It depends on bar-color/text-color contrast. A dark fill, or light
;;   text, breaks it at any alpha.
;; - It cannot help an intentionally opaque bar.
;; - The blend is bar-over-text-over-background, so the label color is
;;   muddied by both the fill and whatever is behind the bar.

;; ## Notes for discussion
;;
;; - The canonical position order exists to make stacking and dodging
;;   deterministic across hash-map iteration. It was not meant to set
;;   visual paint order, but it does, as a side effect.
;; - The explore_all() style also right-aligns the label inside the bar
;;   end. Plotje's :text layer-type accepts only [:text :font-size
;;   :nudge-x :nudge-y] -- there is no horizontal anchor, so a label
;;   cannot be pinned inside the bar end the way explore does.

;; ## Agreed plan
;;
;; Direction 1 -- paint order. apply-positions will preserve the pose's
;; layer order in its output, grouping by position type only to compute
;; stack/dodge geometry. The author's lay-* order becomes the paint
;; order, so "bar then text" paints the text on top.
;;
;; Direction 2 -- text anchoring. Add a horizontal and vertical anchor to
;; the :text (and :label) layer types, named to mirror :nudge-x/:nudge-y:
;;
;; - :align-x -- one of :left, :center, :right
;; - :align-y -- one of :bottom, :center, :top
;;
;; The value names the part of the text placed at the data point, so
;; :align-x :right tucks the label inside the bar end (text extends
;; leftward). Defaults :align-x :left and :align-y :center reproduce the
;; current placement.
;;
;; Separate follow-up -- :nudge-x throws on a categorical axis because
;; apply-nudge adds the offset to the raw category values (strings)
;; rather than to drawing-unit positions. Tracked apart from the anchor
;; work.
