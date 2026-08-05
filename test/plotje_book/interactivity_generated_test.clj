(ns
 plotje-book.interactivity-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l35
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Hover over a point for column values",
    :tooltip true,
    :height 320})))


(deftest
 t4_l41
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #"nsk-tooltip" s))))
   v3_l35)))


(def
 v6_l56
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Drag a rectangle to highlight a region",
    :brush true,
    :height 320})))


(deftest
 t7_l62
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and
      (re-find #"nsk-brush-sel" s)
      (re-find #"\"0\.15\"|0\.15\b" s)
      (re-find #"\(<\s*bw\s+3\)" s))))
   v6_l56)))


(def
 v9_l80
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
 t10_l88
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-row-idx" s) (re-find #"nsk-brush-sel" s))))
   v9_l80)))


(def
 v12_l100
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
 t13_l111
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #" → " s))))
   v12_l100)))


(def
 v15_l122
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
