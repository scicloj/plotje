(ns
 plotje-book.timelines-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l54
 (def
  computing-milestones
  {:date
   [#inst "1936-01-01T00:00:00.000-00:00"
    #inst "1947-12-23T00:00:00.000-00:00"
    #inst "1969-10-29T00:00:00.000-00:00"
    #inst "1989-03-12T00:00:00.000-00:00"
    #inst "2007-06-29T00:00:00.000-00:00"],
   :y [1 1 1 1 1],
   :event
   ["Turing machine"
    "Transistor"
    "ARPANET first link"
    "World Wide Web"
    "iPhone"]}))


(def
 v4_l64
 (->
  computing-milestones
  (pj/lay-point :date :y {:size 6, :color "#2c3e50"})
  (pj/lay-text :date :y {:text :event, :nudge-y 0.3, :color "#2c3e50"})
  (pj/options
   {:title "Five milestones in computing",
    :height 220,
    :y-label "",
    :x-label "year"})))


(deftest
 t5_l72
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 5 (:points s))
      (every?
       (set (:texts s))
       ["Turing machine"
        "Transistor"
        "ARPANET first link"
        "World Wide Web"
        "iPhone"]))))
   v4_l64)))


(def
 v7_l85
 (def with-staggered-y (assoc computing-milestones :y [2 1 1.5 2 1])))


(def
 v8_l88
 (->
  with-staggered-y
  (pj/lay-point :date :y {:size 6, :color "#2c3e50"})
  (pj/lay-text
   :date
   :y
   {:text :event, :nudge-y 0.18, :color "#2c3e50"})
  (pj/options
   {:title "Same milestones, staggered y for label clarity",
    :height 260,
    :y-label "",
    :x-label "year"})))


(deftest
 t9_l96
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:panels s)))))
   v8_l88)))


(def
 v11_l113
 (def
  unemployment
  (->
   (rdatasets/ggplot2-economics)
   (tc/select-rows
    (fn*
     [p1__91694#]
     (let
      [d (:date p1__91694#)]
      (and (>= (.getYear d) 2000) (<= (.getYear d) 2014))))))))


(def
 v12_l119
 (->
  unemployment
  (pj/lay-line :date :unemploy {:color "#34495e"})
  (pj/lay-rule-v
   {:x-intercept (java.time.LocalDate/parse "2008-09-15"),
    :color "#c0392b"})
  (pj/lay-rule-v
   {:x-intercept (java.time.LocalDate/parse "2001-03-01"),
    :color "#7f8c8d"})
  (pj/options
   {:title "US unemployment with recession markers",
    :y-label "thousands unemployed",
    :x-label "date",
    :height 320})))


(deftest
 t13_l130
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:lines s)))))
   v12_l119)))


(def
 v15_l155
 (def
  project
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
   :team ["UX" "Eng" "QA" "Eng" "UX"]}))


(def
 v16_l163
 (->
  project
  (pj/lay-interval-h :start :task {:x-end :end, :color :team})
  (pj/options
   {:title "Project plan -- bars colored by team",
    :y-label "task",
    :x-label "",
    :height 320})))


(deftest
 t17_l170
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v16_l163)))


(def
 v19_l179
 (->
  (rdatasets/ggplot2-presidential)
  (pj/lay-interval-h :start :name {:x-end :end, :color :party})
  (pj/options
   {:title "US presidential terms since 1953",
    :y-label "",
    :x-label "year",
    :height 420,
    :palette ["#3498db" "#e74c3c"]})))


(deftest
 t20_l187
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 12 (:polygons s)))))
   v19_l179)))


(def
 v22_l203
 (->
  project
  (pj/lay-interval-h
   :start
   :task
   {:x-end :end, :color :team, :interval-thickness 0.4})
  (pj/options
   {:title "interval-thickness = 0.4 -- thin bars",
    :y-label "task",
    :x-label "",
    :height 320})))


(deftest
 t23_l211
 (is
  ((fn
    [_]
    (let
     [default-style
      (->
       (pj/lay-interval-h project :start :task {:x-end :end})
       pj/plan
       :panels
       first
       :layers
       first
       :style)
      custom-style
      (->
       (pj/lay-interval-h
        project
        :start
        :task
        {:x-end :end, :interval-thickness 0.4})
       pj/plan
       :panels
       first
       :layers
       first
       :style)]
     (and
      (== 0.7 (:interval-thickness default-style))
      (== 0.4 (:interval-thickness custom-style)))))
   v22_l203)))


(def
 v25_l233
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
   :cost [10 35 22 8 18]}
  (pj/lay-interval-h :start :task {:x-end :end, :color :cost})
  (pj/options
   {:title "Project plan -- bars colored by cost",
    :y-label "task",
    :x-label "",
    :height 320})))


(deftest
 t26_l245
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v25_l233)))


(def
 v28_l257
 (->
  project
  (pj/lay-interval-h :start :task {:x-end :end, :color :team})
  (pj/coord :flip)
  (pj/options
   {:title "Same project, vertical via coord :flip", :height 360})))


(deftest
 t29_l263
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 5 (:polygons s)))))
   v28_l257)))


(def
 v31_l291
 (def
  trains
  (let
   [stations
    ["Paris" "Dijon" "Lyon" "Avignon" "Marseille"]
    express
    [6.0 8.0 9.5 11.5 13.0]
    local
    [7.0 9.5 11.5 14.0 16.0]
    train-shifts
    [["Express A" 0.0 express]
     ["Local B" 1.0 local]
     ["Express C" 2.5 express]
     ["Local D" 4.0 local]]]
   (vec
    (for
     [[name shift schedule]
      train-shifts
      [station hour]
      (map vector stations schedule)
      :let
      [h (+ hour shift) hh (int h) mm (int (* 60 (- h hh)))]]
     {:station station,
      :time (java.time.LocalDateTime/of 2024 6 1 hh mm),
      :train name})))))


(def
 v32_l312
 (->
  trains
  (pj/lay-line
   :time
   :station
   {:color :train, :y-type :categorical, :size 1.5})
  (pj/lay-point
   :time
   :station
   {:color :train, :y-type :categorical, :size 5})
  (pj/options
   {:title "Marey schedule -- Paris to Marseille",
    :y-label "",
    :x-label "time of day",
    :height 320})))


(deftest
 t33_l320
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 4 (:lines s)) (= 20 (:points s)))))
   v32_l312)))


(def
 v35_l344
 (def
  activity-datetime
  {:start
   [#inst "2024-06-03T09:00:00.000-00:00"
    #inst "2024-06-03T10:30:00.000-00:00"
    #inst "2024-06-03T13:00:00.000-00:00"
    #inst "2024-06-04T09:00:00.000-00:00"
    #inst "2024-06-04T11:00:00.000-00:00"
    #inst "2024-06-04T14:30:00.000-00:00"
    #inst "2024-06-05T09:30:00.000-00:00"
    #inst "2024-06-05T13:00:00.000-00:00"
    #inst "2024-06-05T15:00:00.000-00:00"
    #inst "2024-06-06T09:00:00.000-00:00"
    #inst "2024-06-06T10:00:00.000-00:00"
    #inst "2024-06-06T13:30:00.000-00:00"
    #inst "2024-06-07T09:00:00.000-00:00"
    #inst "2024-06-07T11:00:00.000-00:00"
    #inst "2024-06-07T15:00:00.000-00:00"],
   :end
   [#inst "2024-06-03T10:30:00.000-00:00"
    #inst "2024-06-03T12:00:00.000-00:00"
    #inst "2024-06-03T17:00:00.000-00:00"
    #inst "2024-06-04T11:00:00.000-00:00"
    #inst "2024-06-04T12:30:00.000-00:00"
    #inst "2024-06-04T17:00:00.000-00:00"
    #inst "2024-06-05T13:00:00.000-00:00"
    #inst "2024-06-05T15:00:00.000-00:00"
    #inst "2024-06-05T17:00:00.000-00:00"
    #inst "2024-06-06T10:00:00.000-00:00"
    #inst "2024-06-06T12:30:00.000-00:00"
    #inst "2024-06-06T17:00:00.000-00:00"
    #inst "2024-06-07T11:00:00.000-00:00"
    #inst "2024-06-07T15:00:00.000-00:00"
    #inst "2024-06-07T17:00:00.000-00:00"],
   :day
   ["Mon"
    "Mon"
    "Mon"
    "Tue"
    "Tue"
    "Tue"
    "Wed"
    "Wed"
    "Wed"
    "Thu"
    "Thu"
    "Thu"
    "Fri"
    "Fri"
    "Fri"],
   :kind
   ["meeting"
    "deep work"
    "deep work"
    "deep work"
    "meeting"
    "deep work"
    "deep work"
    "meeting"
    "deep work"
    "meeting"
    "meeting"
    "deep work"
    "deep work"
    "meeting"
    "deep work"]}))


(def
 v36_l365
 (->
  activity-datetime
  (pj/lay-interval-h :start :day {:x-end :end, :color :kind})
  (pj/options
   {:title "A week of activity, absolute time",
    :y-label "",
    :x-label "datetime",
    :height 320})))


(deftest
 t37_l372
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 15 (:polygons s)))))
   v36_l365)))


(def
 v39_l389
 (def
  activity
  {:start
   [9.0
    10.5
    13.0
    9.0
    11.0
    14.5
    9.5
    13.0
    15.0
    9.0
    10.0
    13.5
    9.0
    11.0
    15.0],
   :end
   [10.5
    12.0
    17.0
    11.0
    12.5
    17.0
    13.0
    15.0
    17.0
    10.0
    12.5
    17.0
    11.0
    15.0
    17.0],
   :day
   ["Mon"
    "Mon"
    "Mon"
    "Tue"
    "Tue"
    "Tue"
    "Wed"
    "Wed"
    "Wed"
    "Thu"
    "Thu"
    "Thu"
    "Fri"
    "Fri"
    "Fri"],
   :kind
   ["meeting"
    "deep work"
    "deep work"
    "deep work"
    "meeting"
    "deep work"
    "deep work"
    "meeting"
    "deep work"
    "meeting"
    "meeting"
    "deep work"
    "deep work"
    "meeting"
    "deep work"]}))


(def
 v40_l413
 (->
  activity
  (pj/lay-interval-h :start :day {:x-end :end, :color :kind})
  (pj/options
   {:title "Same week, hour-by-hour",
    :y-label "",
    :x-label "hour of day",
    :height 320})))


(deftest
 t41_l420
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 15 (:polygons s)))))
   v40_l413)))


(def
 v43_l437
 (->
  activity
  (pj/lay-interval-h :start :day {:x-end :end, :color :kind})
  (pj/facet :kind)
  (pj/options
   {:title "Same week, faceted by activity kind",
    :x-label "hour of day",
    :y-label "",
    :height 360})))


(deftest
 t44_l445
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 2 (:panels s)) (= 15 (:polygons s)))))
   v43_l437)))


(def
 v46_l458
 (->
  (rdatasets/ggplot2-presidential)
  (pj/lay-interval-h :start :name {:x-end :end, :color :party})
  (pj/options
   {:title "Hover for term details",
    :tooltip true,
    :height 420,
    :palette ["#3498db" "#e74c3c"]})))


(deftest
 t47_l465
 (is
  ((fn
    [pose]
    (let
     [s (str (pj/plot pose))]
     (and (re-find #":data-tooltip" s) (re-find #" → " s))))
   v46_l458)))
