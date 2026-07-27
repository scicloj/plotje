(ns
 plotje-book.customization-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [clojure2d.color :as c2d]
  [clojure.test :refer [deftest is]]))


(def
 v3_l31
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 800, :height 250})))


(deftest
 t4_l35
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:width s) 800))))
   v3_l31)))


(def
 v6_l41
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:width 300, :height 500})))


(deftest
 t7_l45
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (>= (:width s) 300))))
   v6_l41)))


(def
 v9_l53
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Sepal Measurements",
    :x-label "Length (cm)",
    :y-label "Width (cm)"})))


(deftest
 t10_l59
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Sepal Measurements"} (:texts s)))))
   v9_l53)))


(def
 v12_l65
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "Iris Measurements",
    :subtitle "Sepal dimensions across three species",
    :caption "Source: Fisher's Iris dataset (1936)"})))


(deftest
 t13_l71
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Iris Measurements"} (:texts s))
      (some (fn [t] (.contains t "Sepal dimensions")) (:texts s)))))
   v12_l65)))


(def
 v15_l79
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:color-label "Species (override)"})))


(deftest
 t16_l83
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Species (override)"} (:texts s)))))
   v15_l79)))


(def
 v18_l89
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:size :petal-length})
  (pj/options {:size-label "Petal length (override)"})))


(deftest
 t19_l93
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v18_l89)))


(def
 v21_l99
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:alpha :petal-length})
  (pj/options {:alpha-label "Petal length (override)"})))


(deftest
 t22_l103
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (some #{"Petal length (override)"} (:texts s)))))
   v21_l99)))


(def
 v24_l117
 (->
  {:x [1 2 3 1 2 3], :y [1 1 1 2 2 2], :z [10 20 30 40 50 60]}
  (pj/lay-tile :x :y {:fill :z})
  (pj/options {:fill-label "Score"})))


(deftest
 t25_l121
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (some #{"Score"} (:texts s)) (pos? (:visible-tiles s)))))
   v24_l117)))


(def
 v27_l132
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:color :species})))


(deftest
 t28_l135
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v) fills (disj (:colors s) "none")]
     (and (= 3 (:polygons s)) (= 3 (count fills)))))
   v27_l132)))


(def
 v30_l149
 (->
  {:product
   (map (fn* [p1__93476#] (str "Product " p1__93476#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45})))


(deftest
 t31_l154
 (is
  ((fn
    [v]
    (and
     (= 12 (:polygons (pj/svg-summary v)))
     (.contains (pr-str (pj/plot v)) "rotate(-45")))
   v30_l149)))


(def
 v33_l162
 (->
  {:product
   (map (fn* [p1__93477#] (str "Product " p1__93477#)) (range 12)),
   :revenue [120 95 140 60 175 80 110 150 90 130 70 160]}
  (pj/lay-bar :product :revenue)
  (pj/options {:x-tick-angle -45, :x-tick-label-pad 90})))


(deftest
 t34_l168
 (is ((fn [v] (= 12 (:polygons (pj/svg-summary v)))) v33_l162)))


(def
 v36_l179
 (def
  exponential-data
  {:x (range 1 50),
   :y
   (map
    (fn* [p1__93478#] (* 2 (Math/pow 1.1 p1__93478#)))
    (range 1 50))}))


(def
 v38_l185
 (->
  exponential-data
  (pj/lay-point :x :y)
  (pj/options {:title "Linear Scale"})))


(deftest
 t39_l189
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 49 (:points s)))))
   v38_l185)))


(def
 v41_l195
 (->
  exponential-data
  (pj/lay-point :x :y)
  (pj/scale :y :log)
  (pj/options {:title "Log Y Scale"})))


(deftest
 t42_l200
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 49 (:points s)))))
   v41_l195)))


(def
 v44_l206
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :domain [0 6]})
  (pj/options {:title "Fixed Y Domain [0, 6]"})))


(deftest
 t45_l211
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v44_l206)))


(def
 v47_l224
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :domain [3.0 3.5]})
  (pj/options {:title "Tight Y Domain [3.0, 3.5]"})))


(deftest
 t48_l229
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v47_l224)))


(def
 v50_l236
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :breaks [2.0 3.0 4.0]})))


(deftest
 t51_l240
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (every? (set (:texts s)) ["2" "3" "4"]))))
   v50_l236)))


(def
 v53_l250
 (->
  (for
   [day (range 1 8) hour (range 0 24)]
   {:day day,
    :hour hour,
    :load (+ (* 0.3 (Math/sin (* 0.5 hour))) (* 0.2 (mod day 3)))})
  (pj/lay-tile :day :hour {:fill :load})
  (pj/scale
   :x
   {:type :linear,
    :breaks [1 2 3 4 5 6 7],
    :labels ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]})
  (pj/options {:title "Weekly Load by Hour"})))


(deftest
 t54_l259
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v53_l250)))


(def
 v56_l267
 (->
  {:size ["medium" "small" "large"], :count [12 30 7]}
  (pj/lay-bar :size :count)
  (pj/scale
   :x
   {:type :categorical, :domain ["large" "medium" "small"]})))


(deftest
 t57_l272
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      labels
      (filter #{"small" "medium" "large"} (:texts s))]
     (= ["large" "medium" "small"] (vec labels))))
   v56_l267)))


(def
 v59_l283
 (->
  {:quarter ["Q1" "Q2" "Q3" "Q4"], :revenue [120 150 90 200]}
  (pj/lay-bar :quarter :revenue)
  (pj/scale :x {:breaks ["Q1" "Q4"], :labels ["First" "Fourth"]})))


(deftest
 t60_l288
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and
      (contains? texts "First")
      (contains? texts "Fourth")
      (not (contains? texts "Q2")))))
   v59_l283)))


(def
 v62_l301
 (->
  {:bin (map (fn* [p1__93479#] (str "bin-" p1__93479#)) (range 40)),
   :count (range 40)}
  (pj/lay-bar :bin :count)
  (pj/scale :x {:n-ticks 8})))


(deftest
 t63_l306
 (is
  ((fn
    [v]
    (let
     [labels
      (filter
       (fn* [p1__93480#] (.startsWith p1__93480# "bin-"))
       (:texts (pj/svg-summary v)))]
     (= 8 (count labels))))
   v62_l301)))


(def
 v65_l327
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t66_l330
 (is
  ((fn
    [v]
    (let
     [sizes (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (count sizes))
      (< (/ (second sizes) (first sizes)) 1.5)
      (> (/ (last sizes) (first sizes)) 3.0))))
   v65_l327)))


(def
 v68_l343
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t69_l347
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v68_l343)))


(def
 v71_l356
 (->
  (for
   [r (range 5) c (range 5)]
   {:r r, :c c, :v (Math/pow 10.0 (/ (+ r c) 2.0))})
  (pj/lay-tile :r :c {:fill :v})
  (pj/scale :fill :log)))


(deftest
 t72_l361
 (is ((fn [v] (>= (:visible-tiles (pj/svg-summary v)) 25)) v71_l356)))


(def
 v74_l375
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t75_l378
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:panels s))
      (= 150 (:points s))
      (contains? (:alphas s) 0.5)
      (contains? (:sizes s) 5.0))))
   v74_l375)))


(def
 v77_l386
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t78_l389
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v77_l386)))


(def
 v80_l398
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dashed})))


(deftest
 t81_l401
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v80_l398)))


(def
 v83_l407
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :dotted})))


(deftest
 t84_l410
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "1.00 3.00"))))
   v83_l407)))


(def
 v86_l416
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash :solid})))


(deftest
 t87_l419
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:lines s)) (= 0 (:dashed-lines s)))))
   v86_l416)))


(def
 v89_l426
 (->
  {:x [1 2 3 4 5], :y [2 4 3 5 4]}
  (pj/lay-line :x :y {:stroke-dash [12 4]})))


(deftest
 t90_l429
 (is
  ((fn
    [v]
    (contains? (:dash-patterns (pj/svg-summary v)) "12.00 4.00"))
   v89_l426)))


(def
 v92_l433
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t93_l436
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v92_l433)))


(def
 v95_l459
 (->
  {:x [2 2 2], :y [3 2 1]}
  (pj/lay-point :x :y {:size 6, :color "#888888"})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :left,
    :data {:x [2], :y [3], :tag ["align-x :left"]}})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :center,
    :data {:x [2], :y [2], :tag ["align-x :center"]}})
  (pj/lay-text
   :x
   :y
   {:text :tag,
    :align-x :right,
    :data {:x [2], :y [1], :tag ["align-x :right"]}})))


(deftest
 t96_l468
 (is
  ((fn
    [fr]
    (=
     [:left :center :right]
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      (filter (fn* [p1__93481#] (= :text (:mark p1__93481#))))
      (mapv (fn* [p1__93482#] (-> p1__93482# :style :align-x))))))
   v95_l459)))


(def
 v98_l479
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :align-y :bottom})))


(deftest
 t99_l484
 (is
  ((fn
    [fr]
    (let
     [text-style
      (->>
       fr
       pj/plan
       :panels
       first
       :layers
       (filter (fn* [p1__93483#] (= :text (:mark p1__93483#))))
       first
       :style)]
     (and
      (= :center (:align-x text-style))
      (= :bottom (:align-y text-style)))))
   v98_l479)))


(deftest
 t101_l496
 (is
  ((fn
    [_]
    (let
     [text-style
      (fn
       [layer-fn mark opts]
       (->>
        (->
         {:x [1], :y [1], :t ["a"]}
         (layer-fn :x :y (merge {:text :t} opts)))
        pj/plan
        :panels
        first
        :layers
        (filter (fn* [p1__93484#] (= mark (:mark p1__93484#))))
        first
        :style
        ((fn*
          [p1__93485#]
          (select-keys p1__93485# [:align-x :align-y])))))]
     (and
      (=
       {:align-x :left, :align-y :center}
       (text-style pj/lay-text :text {}))
      (=
       :left
       (:align-x (text-style pj/lay-text :text {:align-x :left})))
      (=
       :center
       (:align-x (text-style pj/lay-text :text {:align-x :center})))
      (=
       :right
       (:align-x (text-style pj/lay-text :text {:align-x :right})))
      (=
       :top
       (:align-y (text-style pj/lay-text :text {:align-y :top})))
      (=
       :center
       (:align-y (text-style pj/lay-text :text {:align-y :center})))
      (=
       :bottom
       (:align-y (text-style pj/lay-text :text {:align-y :bottom})))
      (=
       {:align-x :right, :align-y :top}
       (text-style
        pj/lay-label
        :label
        {:align-x :right, :align-y :top}))
      (try
       (text-style pj/lay-text :text {:align-x :middle})
       false
       (catch Exception _ true)))))
   v98_l479)))


(def v103_l529 (:band-opacity (pj/config)))


(deftest t104_l531 (is ((fn [v] (= 0.15 v)) v103_l529)))


(def
 v106_l535
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t107_l540
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v106_l535)))


(def
 v109_l554
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-rule-v
   {:x-intercept 6.0, :color "gray", :stroke-dash :dashed})))


(deftest
 t110_l558
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (= 1 (:dashed-lines s))
      (contains? (:dash-patterns s) "6.00 4.00"))))
   v109_l554)))


(def
 v112_l577
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t113_l581
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v112_l577)))


(def
 v115_l585
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :dark2})))


(deftest
 t116_l589
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v115_l585)))


(def v118_l601 (c2d/find-palette #"budapest"))


(deftest
 t119_l603
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v118_l601)))


(def v121_l607 (c2d/find-palette #"^:set"))


(deftest
 t122_l609
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v121_l607)))


(def v124_l613 (c2d/find-gradient #"viridis"))


(deftest
 t125_l615
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v124_l613)))


(def v127_l620 (c2d/palette :grand-budapest-1))


(deftest
 t128_l622
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v127_l620)))


(def
 v130_l634
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :khroma/okabeito})))


(deftest
 t131_l638
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v130_l634)))


(def
 v133_l644
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t134_l649
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v133_l644)))


(def
 v136_l657
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t137_l661
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v136_l657)))


(def
 v139_l667
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t140_l671
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v139_l667)))


(def
 v142_l677
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t143_l681
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan
       (->
        (rdatasets/datasets-iris)
        (pj/lay-point :sepal-length :sepal-width {:color :species})
        (pj/options {:legend-position :none})))]
     (and
      (= 150 (:points s))
      (zero? (get-in plan [:layout :legend-w])))))
   v142_l677)))
