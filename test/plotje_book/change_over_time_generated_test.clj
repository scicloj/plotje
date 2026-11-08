(ns
 plotje-book.change-over-time-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l18
 (def
  wave
  {:x (range 30),
   :y
   (map (fn* [p1__80237#] (Math/sin (* p1__80237# 0.3))) (range 30))}))


(def v4_l21 (-> wave (pj/lay-line :x :y)))


(deftest
 t5_l24
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v4_l21)))


(def
 v7_l38
 (def
  waves-wide
  (tc/dataset
   {:x (range 30),
    :sin
    (map (fn* [p1__80238#] (Math/sin (* p1__80238# 0.3))) (range 30)),
    :cos
    (map
     (fn* [p1__80239#] (Math/cos (* p1__80239# 0.3)))
     (range 30))})))


(def
 v8_l44
 (def
  waves
  (tc/pivot->longer
   waves-wide
   [:sin :cos]
   {:target-columns :function, :value-column-name :y})))


(def v9_l49 (-> waves (pj/lay-line :x :y {:color :function})))


(deftest
 t10_l52
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v9_l49)))


(def v12_l61 (-> waves-wide (pj/lay-line :x [:sin :cos])))


(deftest
 t13_l64
 (is
  ((fn
    [v]
    (and
     (=
      {:panels 1, :lines 2}
      (select-keys (pj/svg-summary v) [:panels :lines]))
     (= ["sin" "cos"] (mapv :label (:entries (:legend (pj/plan v)))))))
   v12_l61)))


(def v15_l73 (-> wave (pj/lay-line :x :y {:size 4})))


(deftest
 t16_l76
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:lines s)))))
   v15_l73)))


(def
 v18_l84
 (def
  growth
  {:day [1 2 3 4 5 1 2 3 4 5],
   :value [10 15 13 18 22 8 12 11 16 19],
   :group [:a :a :a :a :a :b :b :b :b :b]}))


(def
 v19_l89
 (->
  growth
  (pj/pose :day :value {:color :group})
  pj/lay-line
  pj/lay-point))


(deftest
 t20_l94
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 10 (:points s)) (= 2 (:lines s)))))
   v19_l89)))


(def
 v22_l102
 (-> {:x [1 2 3 4 5], :y [2 4 1 5 3]} (pj/lay-step :x :y) pj/lay-point))


(deftest
 t23_l107
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 5 (:points s)) (= 1 (:lines s)))))
   v22_l102)))


(def
 v25_l115
 (->
  growth
  (pj/pose :day :value {:color :group})
  pj/lay-step
  pj/lay-point))


(deftest
 t26_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 10 (:points s)) (= 2 (:lines s)))))
   v25_l115)))


(def
 v28_l129
 (->
  {:x (concat (range 5) (range 5) (range 5)),
   :y (concat [1 2 3 4 5] [2 2 2 2 2] [3 1 2 1 2]),
   :group (concat (repeat 5 "A") (repeat 5 "B") (repeat 5 "C"))}
  (pj/lay-step :x :y {:position :stack, :color :group})))


(deftest
 t29_l134
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      groups
      (-> v pj/plan :panels first :layers first :groups)]
     (and
      (= 1 (:panels s))
      (= 3 (:lines s))
      (= ["A" "B" "C"] (mapv :label groups))
      (every? zero? (:y0s (last groups)))
      (= (vec (:ys (second groups))) (vec (:y0s (first groups))))
      (= (vec (:ys (last groups))) (vec (:y0s (second groups)))))))
   v28_l129)))


(def
 v31_l158
 (->
  {:x (range 30),
   :y
   (map (fn* [p1__80240#] (Math/sin (* p1__80240# 0.3))) (range 30))}
  (pj/lay-area :x :y)))


(deftest
 t32_l162
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v31_l158)))


(def
 v34_l171
 (->
  {:x (concat (range 10) (range 10) (range 10)),
   :y
   (concat
    [1 2 3 4 5 4 3 2 1 0]
    [2 2 2 3 3 3 2 2 2 2]
    [1 1 1 1 2 2 2 1 1 1]),
   :group (concat (repeat 10 "A") (repeat 10 "B") (repeat 10 "C"))}
  (pj/lay-area :x :y {:position :stack, :color :group})))


(deftest
 t35_l178
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 3 (:polygons s)))))
   v34_l171)))


(def
 v37_l190
 (def
  temp-pose
  (->
   {:date
    [#inst "2024-01-01T00:00:00.000-00:00"
     #inst "2024-02-01T00:00:00.000-00:00"
     #inst "2024-03-01T00:00:00.000-00:00"
     #inst "2024-04-01T00:00:00.000-00:00"
     #inst "2024-05-01T00:00:00.000-00:00"
     #inst "2024-06-01T00:00:00.000-00:00"],
    :temperature [3 5 9 14 19 23]}
   (pj/lay-line :date :temperature)
   pj/lay-point)))


(def v38_l197 temp-pose)


(deftest
 t39_l199
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panel
      (first (:panels (pj/plan temp-pose)))
      tick-labels
      (:labels (:x-ticks panel))]
     (and
      (= 6 (:points s))
      (= 1 (:lines s))
      (some
       (fn* [p1__80241#] (re-find #"[A-Z][a-z]{2}" p1__80241#))
       tick-labels))))
   v38_l197)))


(def
 v41_l216
 (def
  months
  [#inst "2024-01-01T00:00:00.000-00:00"
   #inst "2024-02-01T00:00:00.000-00:00"
   #inst "2024-03-01T00:00:00.000-00:00"
   #inst "2024-04-01T00:00:00.000-00:00"
   #inst "2024-05-01T00:00:00.000-00:00"
   #inst "2024-06-01T00:00:00.000-00:00"]))


(def
 v42_l220
 (->
  {:date (concat months months),
   :temperature [3 5 9 14 19 23 15 17 19 22 25 28],
   :city (concat (repeat 6 "Zurich") (repeat 6 "Athens"))}
  (pj/lay-line :date :temperature {:color :city})
  pj/lay-point))


(deftest
 t43_l228
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 12 (:points s))
      (= 2 (:lines s))
      (let
       [paths
        (fn
         [p]
         (->>
          (pj/plot p)
          (tree-seq coll? seq)
          (filter map?)
          (keep
           (fn*
            [p1__80242#]
            (or (:points p1__80242#) (:d p1__80242#))))
          set))]
       (=
        (paths v)
        (paths
         (pj/with-data
          v
          (tc/order-by (:data v) [:temperature] :desc))))))))
   v42_l220)))


(def
 v45_l248
 (->
  {:date
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"],
   :sales [10 25 30 22 35 40]}
  (pj/lay-area :date :sales)))


(deftest
 t46_l253
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 1 (:polygons s)))))
   v45_l248)))


(def
 v48_l267
 (def
  zurich
  {:date
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"],
   :temperature [3 5 9 14 19 23]}))


(def
 v49_l272
 (def
  athens
  {:date
   [#inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"
    #inst "2024-07-01T00:00:00.000-00:00"
    #inst "2024-08-01T00:00:00.000-00:00"
    #inst "2024-09-01T00:00:00.000-00:00"
    #inst "2024-10-01T00:00:00.000-00:00"],
   :temperature [25 28 31 32 27 22]}))


(def
 v50_l277
 (def
  cities
  [(-> zurich (pj/lay-line :date :temperature) pj/lay-point)
   (-> athens (pj/lay-line :date :temperature) pj/lay-point)]))


(def v51_l285 (pj/arrange cities {:cols 1}))


(deftest
 t52_l287
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__80243#] (-> p1__80243# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= 12 (:points (pj/svg-summary v)))
      (apply not= (mapv :x-domain panels))
      (apply
       not=
       (mapv
        (fn* [p1__80244#] (:labels (:x-ticks p1__80244#)))
        panels)))))
   v51_l285)))


(def v54_l301 (pj/arrange cities {:cols 1, :share-scales #{:x}}))


(deftest
 t55_l303
 (is
  ((fn
    [v]
    (let
     [panels
      (mapv
       (fn* [p1__80245#] (-> p1__80245# :plan :panels first))
       (:sub-plots (pj/plan v)))
      widths
      (->>
       (tree-seq vector? seq (pj/plot v))
       (filter
        (fn*
         [p1__80246#]
         (and
          (vector? p1__80246#)
          (= :rect (first p1__80246#))
          (= "rgb(232,232,232)" (:fill (second p1__80246#))))))
       (mapv
        (fn* [p1__80247#] (double (:width (second p1__80247#))))))]
     (and
      (= 2 (:panels (pj/svg-summary v)))
      (= 12 (:points (pj/svg-summary v)))
      (apply = (mapv :x-domain panels))
      (empty? (:labels (:x-ticks (first panels))))
      (seq (:labels (:x-ticks (second panels))))
      (apply = widths))))
   v54_l301)))


(def
 v57_l341
 (def
  sightings
  {:date
   [#inst "2021-03-14T00:00:00.000-00:00"
    #inst "2021-07-02T00:00:00.000-00:00"
    #inst "2021-11-28T00:00:00.000-00:00"
    #inst "2022-02-09T00:00:00.000-00:00"
    #inst "2022-05-30T00:00:00.000-00:00"
    #inst "2022-06-11T00:00:00.000-00:00"
    #inst "2022-06-25T00:00:00.000-00:00"
    #inst "2022-07-08T00:00:00.000-00:00"
    #inst "2022-09-17T00:00:00.000-00:00"
    #inst "2023-01-22T00:00:00.000-00:00"
    #inst "2023-08-05T00:00:00.000-00:00"
    #inst "2024-02-19T00:00:00.000-00:00"],
   :count [2 5 3 8 6 11 9 14 12 7 4 2]}))


(def
 v58_l348
 (->
  sightings
  (pj/lay-point :date :count)
  (pj/marginal :top :histogram)))


(deftest
 t59_l352
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (mapv
       (fn* [p1__80248#] (-> p1__80248# :plan :panels first))
       (:sub-plots (pj/plan v)))]
     (and
      (= 2 (:panels s))
      (= 12 (:points s))
      (= 5 (:polygons s))
      (apply = (mapv :x-domain panels))
      (= [] (:values (:x-ticks (first panels)))))))
   v58_l348)))


(def
 v61_l373
 (->
  {:date
   [#inst "2024-01-01T00:00:00.000-00:00"
    #inst "2024-02-01T00:00:00.000-00:00"
    #inst "2024-03-01T00:00:00.000-00:00"
    #inst "2024-04-01T00:00:00.000-00:00"
    #inst "2024-05-01T00:00:00.000-00:00"
    #inst "2024-06-01T00:00:00.000-00:00"
    #inst "2024-07-01T00:00:00.000-00:00"
    #inst "2024-08-01T00:00:00.000-00:00"
    #inst "2024-09-01T00:00:00.000-00:00"
    #inst "2024-10-01T00:00:00.000-00:00"
    #inst "2024-11-01T00:00:00.000-00:00"
    #inst "2024-12-01T00:00:00.000-00:00"],
   :sales [10 14 12 18 22 19 25 28 24 30 27 33]}
  (pj/pose :date :sales)
  pj/lay-line
  pj/lay-smooth))


(deftest
 t62_l382
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 2 (:lines s)))))
   v61_l373)))


(def
 v64_l397
 (->
  {:t (range 12), :delta [-3 -1 -2 0 2 4 -1 3 5 -2 1 4]}
  (pj/lay-line :t :delta)
  pj/lay-point
  (pj/lay-rule-h {:y-intercept 0, :color "#888"})))


(deftest
 t65_l403
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 12 (:points s)) (= 2 (:lines s)))))
   v64_l397)))
