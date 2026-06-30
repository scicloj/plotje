;; # Timelines
;;
;; Where
;; [Change Over Time](./plotje_book.change_over_time.html) covers
;; marks that trace a numeric quantity evolving along a
;; sequence (line, step, area, smooth), this chapter focuses on
;; placing **discrete things** on a time axis: events,
;; intervals, schedules. The y-axis here is a lane or a
;; baseline rather than an evolving value -- temporal
;; information is *where* something sits, not *how a value
;; moves*.
;;
;; The chapter introduces `pj/lay-interval-h` for horizontal
;; interval bars (Gantt-style) and shows how existing primitives
;; combine to build calendar-aware visualizations.
;;
;; Pipeline reminder: every example threads data through one or
;; more `pj/lay-*` calls, then through `pj/options` for titles
;; and axis labels.
;; Plotje detects temporal columns automatically and picks
;; calendar-aware tick labels for the date axis.

(ns plotje-book.timelines
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; rdatasets -- bundled R datasets (ggplot2-presidential, etc.)
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Historical event timeline
;;
;; Five milestones in computing history. Each event is a point on
;; a single horizontal line, with a text label nudged above. The
;; vertical position is fixed (`y = 1`) because the y-axis here
;; carries no meaning -- it provides a baseline.
;;
;; Sources for the milestones:
;;
;; - [Turing machine](https://en.wikipedia.org/wiki/Turing_machine)
;;   (Alan Turing's 1936 paper "On Computable Numbers")
;; - [Transistor](https://en.wikipedia.org/wiki/History_of_the_transistor)
;;   (Bardeen, Brattain, and Shockley at Bell Labs, December 1947)
;; - [ARPANET first link](https://en.wikipedia.org/wiki/ARPANET)
;;   (UCLA-SRI message, October 1969)
;; - [World Wide Web](https://en.wikipedia.org/wiki/World_Wide_Web)
;;   (Tim Berners-Lee's proposal, March 1989)
;; - [iPhone](https://en.wikipedia.org/wiki/IPhone_(1st_generation))
;;   (first release, June 2007)

(def computing-milestones
  {:date  [#inst "1936-01-01" #inst "1947-12-23" #inst "1969-10-29"
           #inst "1989-03-12" #inst "2007-06-29"]
   :y     [1 1 1 1 1]
   :event ["Turing machine"
           "Transistor"
           "ARPANET first link"
           "World Wide Web"
           "iPhone"]})

(-> computing-milestones
    (pj/lay-point :date :y {:size 6 :color "#2c3e50"})
    (pj/lay-text  :date :y {:text :event :nudge-y 0.3 :color "#2c3e50"})
    (pj/options {:title "Five milestones in computing"
                 :height 220
                 :y-label ""
                 :x-label "year"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 5 (:points s))
                                (every? (set (:texts s))
                                        ["Turing machine" "Transistor"
                                         "ARPANET first link"
                                         "World Wide Web" "iPhone"]))))])

;; The text labels read horizontally, which keeps them legible.
;; When several events fall close together on the time axis, giving
;; each event a different y spreads the labels apart vertically.
;; Here we apply that technique to the same five milestones to
;; demonstrate the layout pattern.

(def with-staggered-y
  (assoc computing-milestones :y [2 1 1.5 2 1]))

(-> with-staggered-y
    (pj/lay-point :date :y {:size 6 :color "#2c3e50"})
    (pj/lay-text  :date :y {:text :event :nudge-y 0.18 :color "#2c3e50"})
    (pj/options {:title "Same milestones, staggered y for label clarity"
                 :height 260
                 :y-label ""
                 :x-label "year"}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 5 (:points s))
                                (= 1 (:panels s)))))])

;; ## Annotated time series
;;
;; A regular line chart gains context when key dates appear as
;; vertical reference lines. Each rule's position is passed as a
;; constant to `lay-rule-v` -- not drawn from a data column -- so it
;; is treated as an annotation.
;;
;; The data is from
;; [`ggplot2::economics`](https://github.com/tidyverse/ggplot2/blob/main/data-raw/economics.R),
;; loaded here via the
;; [Rdatasets](https://github.com/scicloj/metamorph.ml#rdatasets)
;; bundle.

(def unemployment
  (-> (rdatasets/ggplot2-economics)
      (tc/select-rows #(let [d (:date %)]
                         (and (>= (.getYear d) 2000)
                              (<= (.getYear d) 2014))))))

(-> unemployment
    (pj/lay-line :date :unemploy {:color "#34495e"})
    (pj/lay-rule-v {:x-intercept (java.time.LocalDate/parse "2008-09-15")
                    :color "#c0392b"})
    (pj/lay-rule-v {:x-intercept (java.time.LocalDate/parse "2001-03-01")
                    :color "#7f8c8d"})
    (pj/options {:title "US unemployment with recession markers"
                 :y-label "thousands unemployed"
                 :x-label "date"
                 :height 320}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                ;; one line for unemployment plus two
                                ;; vertical recession-marker rules.
                                (= 3 (:lines s)))))])

;; The two rules mark the start of the
;; [dot-com recession](https://en.wikipedia.org/wiki/Early_2000s_recession)
;; (March 2001) and the
;; [Lehman Brothers collapse](https://en.wikipedia.org/wiki/Bankruptcy_of_Lehman_Brothers)
;; (September 2008). The line shape relative to the rules tells
;; the recession story without any prose.

;; ## Gantt chart with `lay-interval-h`
;;
;; The
;; [Gantt chart](https://en.wikipedia.org/wiki/Gantt_chart) (Henry
;; Gantt, ~1910) is the canonical "tasks-and-dates" picture: each
;; row is a task, each bar's left edge is the start, the right
;; edge is the end.
;;
;; `pj/lay-interval-h` draws one horizontal bar per row, from
;; `x` to `:x-end`, sitting at the lane named by the categorical
;; `y` column. The classic project Gantt:

(def project
  {:start  [#inst "2024-01-01" #inst "2024-02-15" #inst "2024-04-01"
            #inst "2024-05-10" #inst "2024-06-20"]
   :end    [#inst "2024-03-15" #inst "2024-04-20" #inst "2024-06-30"
            #inst "2024-07-10" #inst "2024-08-30"]
   :task   ["Design" "Build" "Test" "Deploy" "Document"]
   :team   ["UX" "Eng" "QA" "Eng" "UX"]})

(-> project
    (pj/lay-interval-h :start :task {:x-end :end :color :team})
    (pj/options {:title "Project plan -- bars colored by team"
                 :y-label "task"
                 :x-label ""
                 :height 320}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:polygons s)))))])

;; A real-world Gantt: every US president's term since 1953,
;; from
;; [`ggplot2::presidential`](https://github.com/tidyverse/ggplot2/blob/main/data-raw/presidential.R).
;; Color encodes party.

(-> (rdatasets/ggplot2-presidential)
    (pj/lay-interval-h :start :name {:x-end :end :color :party})
    (pj/options {:title "US presidential terms since 1953"
                 :y-label ""
                 :x-label "year"
                 :height 420
                 :palette ["#3498db" "#e74c3c"]}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 12 (:polygons s)))))])

;; The blue-and-red palette mirrors the conventional US party
;; colors (Democratic / Republican). Each bar's length is the
;; literal duration of the term -- tight bars (Kennedy, Ford)
;; stand out next to long ones (Reagan, Obama).

;; ## Adjusting bar thickness
;;
;; The `:interval-thickness` option controls how much of each
;; row's band the bar fills. The default is `0.7`; smaller values
;; leave more whitespace between rows, larger values approach
;; overlap.

(-> project
    (pj/lay-interval-h :start :task
                       {:x-end :end :color :team :interval-thickness 0.4})
    (pj/options {:title "interval-thickness = 0.4 -- thin bars"
                 :y-label "task"
                 :x-label ""
                 :height 320}))

(kind/test-last
 [(fn [_]
    (let [default-style (-> (pj/lay-interval-h project :start :task
                                               {:x-end :end})
                            pj/plan
                            :panels first :layers first :style)
          custom-style (-> (pj/lay-interval-h project :start :task
                                              {:x-end :end :interval-thickness 0.4})
                           pj/plan
                           :panels first :layers first :style)]
      (and (== 0.7 (:interval-thickness default-style))
           (== 0.4 (:interval-thickness custom-style)))))])

;; ## Numeric color
;;
;; Pass a numeric column to `:color` and Plotje grades each bar
;; along a continuous gradient instead of a categorical palette.
;; A continuous-color legend appears on the side, showing the
;; mapped data range. The gradient uses the configured color
;; scale (default linear; `(pj/scale :color :log)` on the pose
;; for log-spaced ticks).

(-> {:start [#inst "2024-01-01" #inst "2024-02-15" #inst "2024-04-01"
             #inst "2024-05-10" #inst "2024-06-20"]
     :end   [#inst "2024-03-15" #inst "2024-04-20" #inst "2024-06-30"
             #inst "2024-07-10" #inst "2024-08-30"]
     :task  ["Design" "Build" "Test" "Deploy" "Document"]
     :cost  [10 35 22 8 18]}
    (pj/lay-interval-h :start :task {:x-end :end :color :cost})
    (pj/options {:title "Project plan -- bars colored by cost"
                 :y-label "task"
                 :x-label ""
                 :height 320}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:polygons s)))))])

;; ## Vertical bars via `coord :flip`
;;
;; `lay-interval-h` always binds the lane to the y data axis.
;; To render visually vertical bars (lanes on the horizontal
;; axis, time running upward), apply `pj/coord :flip` to the
;; pose -- the renderer detects which axis carries the band and
;; lays out the rectangles accordingly.

(-> project
    (pj/lay-interval-h :start :task {:x-end :end :color :team})
    (pj/coord :flip)
    (pj/options {:title "Same project, vertical via coord :flip"
                 :height 360}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 5 (:polygons s)))))])

;; ## Marey train schedule
;;
;; [Etienne-Jules Marey](https://en.wikipedia.org/wiki/%C3%89tienne-Jules_Marey)'s
;; classic 1885 diagram of Paris-Lyon trains
;; ([context and image](https://en.wikipedia.org/wiki/Marey_chart),
;; popularized in
;; [Edward Tufte's *Visual Display of Quantitative Information*](https://en.wikipedia.org/wiki/The_Visual_Display_of_Quantitative_Information))
;; has a categorical y-axis (stations, in physical order) and a
;; temporal x-axis. Each train is a polyline; segments between
;; stations slope down because time moves forward as the train
;; moves south.
;;
;; Plotje builds this with `pj/lay-line` plus the existing
;; `:y-type :categorical` override, since the station order is
;; the physical north-to-south sequence (not alphabetical).
;;
;; The y-axis order follows the order each station first appears
;; in the data. With every train starting in Paris, Paris ends up
;; at the bottom and Marseille at the top -- which happens to
;; give the geographic top-to-bottom Paris->Marseille reading. If
;; your data has a different first-station-encountered order, the
;; lanes will reorder accordingly; pre-sorting the rows is the
;; reliable way to guarantee a chosen lane order.

(def trains
  ;; Four trains, each visiting five stations in order. Express trains
  ;; (A, C) take ~7 hours total; locals (B, D) ~9 hours -- visible as a
  ;; shallower slope on the plot.
  (let [stations ["Paris" "Dijon" "Lyon" "Avignon" "Marseille"]
        express  [6.0 8.0  9.5 11.5 13.0]
        local    [7.0 9.5 11.5 14.0 16.0]
        train-shifts [["Express A" 0.0  express]
                      ["Local B"   1.0  local]
                      ["Express C" 2.5  express]
                      ["Local D"   4.0  local]]]
    (vec
     (for [[name shift schedule] train-shifts
           [station hour] (map vector stations schedule)
           :let [h (+ hour shift)
                 hh (int h)
                 mm (int (* 60 (- h hh)))]]
       {:station station
        :time (java.time.LocalDateTime/of 2024 6 1 hh mm)
        :train name}))))

(-> trains
    (pj/lay-line  :time :station {:color :train :y-type :categorical :size 1.5})
    (pj/lay-point :time :station {:color :train :y-type :categorical :size 5})
    (pj/options {:title "Marey schedule -- Paris to Marseille"
                 :y-label ""
                 :x-label "time of day"
                 :height 320}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 4 (:lines s))
                                (= 20 (:points s)))))])

;; The slope of each line carries information: steeper means
;; slower (more time between two stations). The point overlay
;; marks the actual stops; remove `lay-point` and the schedule
;; reads as continuous travel.

;; ## Multi-track activity timeline
;;
;; Two ways to look at "what happened when" across multiple
;; days. The data shape is identical -- start, end, day, activity
;; kind -- only the interpretation of the time axis differs. The
;; right choice depends on what you want the reader to compare.

;; ### Absolute time
;;
;; The literal approach: `:start` and `:end` are real
;; datetimes, each bar covers its actual interval. Useful when
;; absolute time matters (logs, scheduling tools) or when bars
;; may cross day boundaries.

(def activity-datetime
  ;; A developer's week: meetings and deep-work blocks, with
  ;; absolute timestamps.
  {:start [#inst "2024-06-03T09:00" #inst "2024-06-03T10:30" #inst "2024-06-03T13:00"
           #inst "2024-06-04T09:00" #inst "2024-06-04T11:00" #inst "2024-06-04T14:30"
           #inst "2024-06-05T09:30" #inst "2024-06-05T13:00" #inst "2024-06-05T15:00"
           #inst "2024-06-06T09:00" #inst "2024-06-06T10:00" #inst "2024-06-06T13:30"
           #inst "2024-06-07T09:00" #inst "2024-06-07T11:00" #inst "2024-06-07T15:00"]
   :end   [#inst "2024-06-03T10:30" #inst "2024-06-03T12:00" #inst "2024-06-03T17:00"
           #inst "2024-06-04T11:00" #inst "2024-06-04T12:30" #inst "2024-06-04T17:00"
           #inst "2024-06-05T13:00" #inst "2024-06-05T15:00" #inst "2024-06-05T17:00"
           #inst "2024-06-06T10:00" #inst "2024-06-06T12:30" #inst "2024-06-06T17:00"
           #inst "2024-06-07T11:00" #inst "2024-06-07T15:00" #inst "2024-06-07T17:00"]
   :day   ["Mon" "Mon" "Mon" "Tue" "Tue" "Tue" "Wed" "Wed" "Wed"
           "Thu" "Thu" "Thu" "Fri" "Fri" "Fri"]
   :kind  ["meeting" "deep work" "deep work"
           "deep work" "meeting" "deep work"
           "deep work" "meeting" "deep work"
           "meeting" "meeting" "deep work"
           "deep work" "meeting" "deep work"]})

(-> activity-datetime
    (pj/lay-interval-h :start :day {:x-end :end :color :kind})
    (pj/options {:title "A week of activity, absolute time"
                 :y-label ""
                 :x-label "datetime"
                 :height 320}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 15 (:polygons s)))))])

;; The bars are accurate but visually narrow: each block is
;; only a few hours within a five-day axis, and the overnight
;; gaps consume most of the width. Comparing Monday's morning
;; to Wednesday's afternoon takes mental effort.

;; ### Hour-of-day numeric
;;
;; Re-index the x-axis to "hours since midnight" and the
;; within-day shape is what stands out. The data structure
;; is the same -- still `(start, end, day, kind)` -- but
;; `start` and `end` are now hours-from-midnight numbers, and
;; the x-axis spans the workday rather than the whole week.

(def activity
  ;; Same week as above, with hours-since-midnight in place of
  ;; full datetimes.
  {:start [9.0 10.5 13.0      ;; Mon
           9.0 11.0 14.5      ;; Tue
           9.5 13.0 15.0      ;; Wed
           9.0 10.0 13.5      ;; Thu
           9.0 11.0 15.0]     ;; Fri
   :end   [10.5 12.0 17.0     ;; Mon
           11.0 12.5 17.0     ;; Tue
           13.0 15.0 17.0     ;; Wed
           10.0 12.5 17.0     ;; Thu
           11.0 15.0 17.0]    ;; Fri
   :day   ["Mon" "Mon" "Mon"
           "Tue" "Tue" "Tue"
           "Wed" "Wed" "Wed"
           "Thu" "Thu" "Thu"
           "Fri" "Fri" "Fri"]
   :kind  ["meeting" "deep work" "deep work"
           "deep work" "meeting" "deep work"
           "deep work" "meeting" "deep work"
           "meeting" "meeting" "deep work"
           "deep work" "meeting" "deep work"]})

(-> activity
    (pj/lay-interval-h :start :day {:x-end :end :color :kind})
    (pj/options {:title "Same week, hour-by-hour"
                 :y-label ""
                 :x-label "hour of day"
                 :height 320}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 1 (:panels s))
                                (= 15 (:polygons s)))))])

;; Wednesday's afternoon block is the longest
;; uninterrupted deep-work stretch; Mondays start with a
;; meeting, Fridays with deep work. The trade-off is that you
;; lose absolute time -- "Monday 10am" no longer reads as a
;; specific point on a calendar.

;; ## Faceting by category
;;
;; Splitting one chart into one-panel-per-category makes
;; overlapping intervals easier to compare. Faceting the activity
;; timeline by `:kind` produces two stacked tracks -- meetings on
;; one panel, deep work on the other.

(-> activity
    (pj/lay-interval-h :start :day {:x-end :end :color :kind})
    (pj/facet :kind)
    (pj/options {:title "Same week, faceted by activity kind"
                 :x-label "hour of day"
                 :y-label ""
                 :height 360}))

(kind/test-last [(fn [v] (let [s (pj/svg-summary v)]
                           (and (= 2 (:panels s))
                                (= 15 (:polygons s)))))])

;; ## Interactive timelines
;;
;; Pass `:tooltip true` to add hover-text on each interval, or
;; `:brush true` for drag-to-select highlighting (the same
;; built-in mechanisms that work for points). For more
;; interaction patterns -- cross-panel linking, save-as-PNG --
;; see the
;; [Interactivity](./plotje_book.interactivity.html) chapter.

(-> (rdatasets/ggplot2-presidential)
    (pj/lay-interval-h :start :name {:x-end :end :color :party})
    (pj/options {:title "Hover for term details"
                 :tooltip true
                 :height 420
                 :palette ["#3498db" "#e74c3c"]}))

(kind/test-last [(fn [pose] (let [s (str (pj/plot pose))]
                              (and (re-find #":data-tooltip" s)
                                   (re-find #" → " s))))])

;; ## See Also
;;
;; - [**Core Concepts**](./plotje_book.core_concepts.html) -- mappings, aesthetics, and the inference rules behind temporal axes

;; ## What's Next
;;
;; - [**Interactivity**](./plotje_book.interactivity.html) -- tooltip, brush, cross-panel linking, save-as-PNG
;; - [**Change Over Time**](./plotje_book.change_over_time.html) -- line, step, area on a date axis
;; - [**Faceting**](./plotje_book.faceting.html) -- splitting one chart into many panels
;; - [**Customization**](./plotje_book.customization.html) -- titles, palettes, size, scales
