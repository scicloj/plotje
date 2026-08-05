;; # Interactivity
;;
;; Plotje produces SVG hiccup. Two layers of interaction are
;; available:
;;
;; - **Built-in**: pass `:tooltip true` or `:brush true` in pose
;;   options. Plotje injects `data-tooltip` / `data-row-idx`
;;   attributes on rendered shapes and includes the matching
;;   browser-side script automatically.
;; - **Custom wrappers**: wrap the SVG output with `kind/hiccup`
;;   plus a small `[:script ...]` form for behaviours not built in
;;   (e.g. save-as-PNG).
;;
;; The static GFM render of this notebook shows the SVGs as
;; flat images. Open the HTML rendering to see the interactions
;; live.

(ns plotje-book.interactivity
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; rdatasets -- bundled R datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Tooltips
;;
;; Pass `:tooltip true` to `pj/options` and every data shape gets
;; a `data-tooltip` attribute holding its column values. A small
;; embedded script renders the tooltip on hover -- no extra setup.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Hover over a point for column values"
                 :tooltip true
                 :height 320}))

(kind/test-last [(fn [pose]
                   (let [s (str (pj/plot pose))]
                     (and (re-find #":data-tooltip" s)
                          (re-find #"nsk-tooltip" s))))])

;; ## Brush selection
;;
;; `:brush true` enables drag-to-select. While dragging, a shaded
;; rectangle follows the cursor; on release, points inside keep
;; full opacity and points outside dim to 0.15. A short drag
;; (less than 3 CSS pixels each side -- the unit a mouse event
;; reports, not a drawing unit) clears the selection. Selection
;; is keyed by row index, so it tracks the same rows across every
;; panel in the pose.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    (pj/options {:title "Drag a rectangle to highlight a region"
                 :brush true
                 :height 320}))

(kind/test-last
 [(fn [pose]
    (let [s (str (pj/plot pose))]
      (and (re-find #"nsk-brush-sel" s)
           ;; Outside-selection points dim to 0.15 opacity.
           (re-find #"\"0\.15\"|0\.15\b" s)
           ;; Drags shorter than 3 pixels each side clear the
           ;; selection.
           (re-find #"\(<\s*bw\s+3\)" s))))])

;; ## Cross-panel linked highlighting
;;
;; Because brush selection is keyed by `data-row-idx` (a stable
;; integer attached to each rendered shape at extract time), the
;; same selection lights up matching rows in every panel of a
;; faceted pose. Drag in one species panel; the corresponding
;; rows in the other two species panels respond immediately.

(-> (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width)
    (pj/facet :species)
    (pj/options {:title "Brush on one panel, see linked points in the others"
                 :brush true
                 :tooltip true
                 :height 320}))

(kind/test-last [(fn [pose]
                   (let [s (str (pj/plot pose))]
                     (and (re-find #":data-row-idx" s)
                          (re-find #"nsk-brush-sel" s))))])

;; ## Interval (Gantt) tooltips
;;
;; `lay-interval-h` participates in the same tooltip/brush system.
;; Each rectangle's tooltip names the lane, the start, the end,
;; and the color label; on temporal axes the start and end are
;; formatted as date strings rather than raw epoch-milliseconds.

(-> {:start [#inst "2024-01-01" #inst "2024-02-15" #inst "2024-04-01"
             #inst "2024-05-10" #inst "2024-06-20"]
     :end   [#inst "2024-03-15" #inst "2024-04-20" #inst "2024-06-30"
             #inst "2024-07-10" #inst "2024-08-30"]
     :task  ["Design" "Build" "Test" "Deploy" "Document"]
     :team  ["UX" "Eng" "QA" "Eng" "UX"]}
    (pj/lay-interval-h :start :task {:x-end :end :color :team})
    (pj/options {:title "Hover for task: start -> end, team"
                 :tooltip true
                 :height 320}))

(kind/test-last [(fn [pose]
                   (let [s (str (pj/plot pose))]
                     (and (re-find #":data-tooltip" s)
                          (re-find #" → " s))))])

;; ## Custom wrapper: save as PNG
;;
;; Browsers can serialize an SVG element to a PNG via a `<canvas>`
;; round-trip. The wrapper below adds a "Save PNG" button that
;; renders the current SVG into a canvas and triggers a download.

(let [plot-svg (pj/plot
                (-> (rdatasets/datasets-iris)
                    (pj/lay-point :sepal-length :sepal-width {:color :species})
                    (pj/options {:title "Click 'Save PNG' to download the rendering"
                                 :height 320})))
      attrs (second plot-svg)
      body (drop 2 plot-svg)
      plot-id (str "pj-png-" (System/nanoTime))
      btn-id (str plot-id "-save")
      script (str "document.getElementById('" btn-id "').addEventListener('click',function(){"
                  "var svg=document.getElementById('" plot-id "');"
                  "var w=svg.clientWidth||" (or (:width attrs) 600) ","
                  "h=svg.clientHeight||" (or (:height attrs) 400) ";"
                  "var data=new XMLSerializer().serializeToString(svg);"
                  "var img=new Image();"
                  "img.onload=function(){"
                  "var c=document.createElement('canvas');c.width=w;c.height=h;"
                  "c.getContext('2d').drawImage(img,0,0,w,h);"
                  "var a=document.createElement('a');"
                  "a.href=c.toDataURL('image/png');a.download='plotje.png';"
                  "document.body.appendChild(a);a.click();a.remove();};"
                  "img.src='data:image/svg+xml;base64,'+btoa(unescape(encodeURIComponent(data)));"
                  "});")]
  (kind/hiccup
   [:div
    [:button {:id btn-id
              :style "margin-bottom:6px; padding:4px 12px;"}
     "Save PNG"]
    (into [:svg (assoc attrs :id plot-id)] body)
    [:script script]]))

;; ## See Also
;;
;; - [**Options and Scopes**](./plotje_book.options_and_scopes.html) -- where `:tooltip`, `:brush`, and other plot options live in the pose

;; ## What's Next
;;
;; - [**Timelines**](./plotje_book.timelines.html) -- where most of the example data came from
;; - [**Customization**](./plotje_book.customization.html) -- titles, palettes, scales
;; - [**Architecture**](./plotje_book.architecture.html) -- how the pipeline produces the SVG
