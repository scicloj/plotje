(ns
 plotje-book.interactivity-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l46
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Hover over a point for column values",
    :tooltip true,
    :height 320})))


(deftest
 t4_l52
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #"nsk-tooltip" s))))
   v3_l46)))


(def
 v6_l69
 (def
  sales
  (tc/dataset
   {:month ["Jan" "Feb" "Mar" "Apr"],
    :revenue [1653346 2410880 987654 3120500],
    :margin [0.184 0.223 0.161 0.207]})))


(def v7_l74 sales)


(def
 v9_l79
 (def
  sales-labelled
  (tc/add-column
   sales
   :hover
   (fn*
    [p1__75803#]
    (map
     (fn
      [month revenue margin]
      (str
       month
       "\n"
       (format "%.1fM" (/ (double revenue) 1000000.0))
       " at "
       (format "%.1f%%" (* 100.0 margin))))
     (:month p1__75803#)
     (:revenue p1__75803#)
     (:margin p1__75803#))))))


(def v10_l87 sales-labelled)


(def
 v12_l91
 (->
  sales-labelled
  (pj/lay-point :margin :revenue {:tooltip :hover})
  (pj/options
   {:title "Hover for the month, revenue and margin", :height 320})))


(deftest
 t13_l96
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and
      (re-find #"1.7M at 18.4%" s)
      (true? (:tooltip (pj/plan pose)))
      (re-find #"nsk-tooltip" s))))
   v12_l91)))


(def
 v15_l128
 (def
  sales-rich
  (tc/add-column
   sales
   :hover
   (fn*
    [p1__75804#]
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
     (:month p1__75804#)
     (:revenue p1__75804#)
     (:margin p1__75804#))))))


(def v16_l139 sales-rich)


(def
 v18_l143
 (->
  sales-rich
  (pj/lay-point :margin :revenue {:tooltip :hover})
  (pj/options {:title "Hover for a formatted label", :height 320})))


(deftest
 t19_l148
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #"<b>Jan</b>" s) (re-find #"<code>1.7M</code>" s))))
   v18_l143)))


(def
 v21_l157
 (->
  sales
  (pj/lay-point
   :margin
   :revenue
   {:tooltip [:b "one reading per point"]})
  (pj/options {:height 240})))


(deftest
 t22_l161
 (is
  ((fn
    [pose]
    (let
     [attrs
      (->>
       (tree-seq vector? seq (pj/plot pose))
       (filter
        (fn*
         [p1__75805#]
         (and (vector? p1__75805#) (map? (second p1__75805#)))))
       (map second))]
     (=
      ["<b>one reading per point</b>"]
      (distinct (keep :data-tooltip-html attrs)))))
   v21_l157)))


(def
 v24_l172
 (->
  sales
  (pj/lay-point :margin :revenue {:tooltip "<b>not bold</b>"})
  (pj/options {:height 240})))


(deftest
 t25_l176
 (is
  ((fn
    [pose]
    (let
     [attrs
      (->>
       (tree-seq vector? seq (pj/plot pose))
       (filter
        (fn*
         [p1__75806#]
         (and (vector? p1__75806#) (map? (second p1__75806#)))))
       (map second))]
     (and
      (some :data-tooltip attrs)
      (not-any? :data-tooltip-html attrs))))
   v24_l172)))


(def
 v27_l197
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Drag a rectangle to highlight a region",
    :brush true,
    :height 320})))


(deftest
 t28_l203
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and
      (re-find #"nsk-brush-sel" s)
      (re-find #"\"0\.15\"|0\.15\b" s)
      (re-find #"\(<\s*bw\s+3\)" s))))
   v27_l197)))


(def
 v30_l221
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
 t31_l229
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-row-idx" s) (re-find #"nsk-brush-sel" s))))
   v30_l221)))


(def
 v33_l241
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
 t34_l252
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #" → " s))))
   v33_l241)))


(def
 v36_l263
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


(def
 v38_l307
 (with-out-str
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   (pj/options {:tooltip true, :brush true})
   (pj/plot {:format :bufimg}))))


(deftest
 t39_l313
 (is
  ((fn
    [out]
    (and
     (re-find #":tooltip and :brush asked for" out)
     (re-find #":bufimg format draws no interaction" out)
     (re-find #"The formats that do: :svg" out)
     (= 1 (count (re-seq #"draws no interaction" out)))
     (let
      [drawn (atom nil)]
      (with-out-str
       (reset!
        drawn
        (->
         (rdatasets/datasets-iris)
         (pj/lay-point :sepal-length :sepal-width)
         (pj/options {:tooltip true})
         (pj/plot {:format :bufimg}))))
      (instance? java.awt.image.BufferedImage (deref drawn)))))
   v38_l307)))
