(ns
 plotje-book.interactivity-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l41
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Hover over a point for column values",
    :tooltip true,
    :height 320})))


(deftest
 t4_l47
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #"nsk-tooltip" s))))
   v3_l41)))


(def
 v6_l64
 (def
  sales
  (tc/dataset
   {:month ["Jan" "Feb" "Mar" "Apr"],
    :revenue [1653346 2410880 987654 3120500],
    :margin [0.184 0.223 0.161 0.207]})))


(def v7_l69 sales)


(def
 v9_l74
 (def
  sales-labelled
  (tc/add-column
   sales
   :hover
   (fn*
    [p1__78731#]
    (map
     (fn
      [month revenue margin]
      (str
       month
       "\n"
       (format "%.1fM" (/ (double revenue) 1000000.0))
       " at "
       (format "%.1f%%" (* 100.0 margin))))
     (:month p1__78731#)
     (:revenue p1__78731#)
     (:margin p1__78731#))))))


(def v10_l82 sales-labelled)


(def
 v12_l86
 (->
  sales-labelled
  (pj/lay-point :margin :revenue {:tooltip :hover})
  (pj/options
   {:title "Hover for the month, revenue and margin", :height 320})))


(deftest
 t13_l91
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and
      (re-find #"1.7M at 18.4%" s)
      (true? (:tooltip (pj/plan pose)))
      (re-find #"nsk-tooltip" s))))
   v12_l86)))


(def
 v15_l121
 (def
  sales-rich
  (tc/add-column
   sales
   :hover
   (fn*
    [p1__78732#]
    (map
     (fn
      [month revenue margin]
      [:div
       [:b month]
       [:br]
       "revenue "
       [:code (format "%.1fM" (/ (double revenue) 1000000.0))]
       [:br]
       "margin "
       [:code (format "%.1f%%" (* 100.0 margin))]])
     (:month p1__78732#)
     (:revenue p1__78732#)
     (:margin p1__78732#))))))


(def v16_l132 sales-rich)


(def
 v18_l136
 (->
  sales-rich
  (pj/lay-point :margin :revenue {:tooltip :hover})
  (pj/options {:title "Hover for a formatted label", :height 320})))


(deftest
 t19_l141
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"<b>Jan</b>" s) (re-find #"<code>1.7M</code>" s))))
   v18_l136)))


(def
 v21_l150
 (->
  sales
  (pj/lay-point :margin :revenue {:tooltip "<b>not bold</b>"})
  (pj/options {:height 240})))


(deftest
 t22_l154
 (is
  ((fn
    [pose]
    (let
     [attrs
      (->>
       (tree-seq vector? seq (pj/plot pose))
       (filter
        (fn*
         [p1__78733#]
         (and (vector? p1__78733#) (map? (second p1__78733#)))))
       (map second))]
     (and
      (some :data-tooltip attrs)
      (not-any? :data-tooltip-html attrs))))
   v21_l150)))


(def
 v24_l175
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Drag a rectangle to highlight a region",
    :brush true,
    :height 320})))


(deftest
 t25_l181
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and
      (re-find #"nsk-brush-sel" s)
      (re-find #"\"0\.15\"|0\.15\b" s)
      (re-find #"\(<\s*bw\s+3\)" s))))
   v24_l175)))


(def
 v27_l199
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/options
   {:title "Brush on one panel, see linked points in the others",
    :brush true,
    :tooltip true,
    :height 320})))


(deftest
 t28_l207
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-row-idx" s) (re-find #"nsk-brush-sel" s))))
   v27_l199)))


(def
 v30_l219
 (->
  {:start
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-15T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-10T00:00:00.000-00:00"
    #inst "2024-06-20T00:00:00.000-00:00"],
   :end
   [#inst "2024-03-15T00:00:00.000-00:00"
    #inst "2024-04-20T00:00:00.000-00:00"
    #inst "2024-06-30T00:00:00.000-00:00"
    #inst "2024-07-10T00:00:00.000-00:00"
    #inst "2024-08-30T00:00:00.000-00:00"],
   :task ["Design" "Build" "Test" "Deploy" "Document"],
   :team ["UX" "Eng" "QA" "Eng" "UX"]}
  (pj/lay-interval-h :start :task {:x-end :end, :color :team})
  (pj/options
   {:title "Hover for task: start -> end, team",
    :tooltip true,
    :height 320})))


(deftest
 t31_l230
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #" → " s))))
   v30_l219)))


(def
 v33_l241
 (let
  [plot-svg
   (pj/plot
    (->
     (rdatasets/datasets-iris)
     (pj/lay-point :sepal-length :sepal-width {:color :species})
     (pj/options
      {:title "Click 'Save PNG' to download the rendering",
       :height 320})))
   attrs
   (second plot-svg)
   body
   (drop 2 plot-svg)
   plot-id
   (str "pj-png-" (System/nanoTime))
   btn-id
   (str plot-id "-save")
   script
   (str
    "document.getElementById('"
    btn-id
    "').addEventListener('click',function(){"
    "var svg=document.getElementById('"
    plot-id
    "');"
    "var w=svg.clientWidth||"
    (or (:width attrs) 600)
    ","
    "h=svg.clientHeight||"
    (or (:height attrs) 400)
    ";"
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
    [:button
     {:id btn-id, :style "margin-bottom:6px; padding:4px 12px;"}
     "Save PNG"]
    (into [:svg (assoc attrs :id plot-id)] body)
    [:script script]])))
