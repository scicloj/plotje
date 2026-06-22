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
 v30_l145
 (def
  exponential-data
  {:x (range 1 50),
   :y
   (map
    (fn* [p1__85819#] (* 2 (Math/pow 1.1 p1__85819#)))
    (range 1 50))}))


(def
 v32_l151
 (->
  exponential-data
  (pj/lay-point :x :y)
  (pj/options {:title "Linear Scale"})))


(deftest
 t33_l155
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 49 (:points s)))))
   v32_l151)))


(def
 v35_l161
 (->
  exponential-data
  (pj/lay-point :x :y)
  (pj/scale :y :log)
  (pj/options {:title "Log Y Scale"})))


(deftest
 t36_l166
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 49 (:points s)))))
   v35_l161)))


(def
 v38_l172
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :domain [0 6]})
  (pj/options {:title "Fixed Y Domain [0, 6]"})))


(deftest
 t39_l177
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (= 150 (:points s)))))
   v38_l172)))


(def
 v41_l190
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :domain [3.0 3.5]})
  (pj/options {:title "Tight Y Domain [3.0, 3.5]"})))


(deftest
 t42_l195
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (= 1 (:clips s)))))
   v41_l190)))


(def
 v44_l202
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/scale :y {:type :linear, :breaks [2.0 3.0 4.0]})))


(deftest
 t45_l206
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 150 (:points s))
      (every? (set (:texts s)) ["2" "3" "4"]))))
   v44_l202)))


(def
 v47_l216
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
 t48_l225
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (every? texts ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])))
   v47_l216)))


(def
 v50_l233
 (->
  {:size ["medium" "small" "large"], :count [12 30 7]}
  (pj/lay-value-bar :size :count)
  (pj/scale
   :x
   {:type :categorical, :domain ["large" "medium" "small"]})))


(deftest
 t51_l238
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      labels
      (filter #{"small" "medium" "large"} (:texts s))]
     (= ["large" "medium" "small"] (vec labels))))
   v50_l233)))


(def
 v53_l259
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})))


(deftest
 t54_l262
 (is
  ((fn
    [v]
    (let
     [sizes (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 3 (count sizes))
      (< (/ (second sizes) (first sizes)) 1.5)
      (> (/ (last sizes) (first sizes)) 3.0))))
   v53_l259)))


(def
 v56_l275
 (->
  {:user [:a :b :c], :n [10 100 1000]}
  (pj/lay-point :user :n {:size :n, :x-type :categorical})
  (pj/scale :size :log)))


(deftest
 t57_l279
 (is ((fn [v] (= 3 (:points (pj/svg-summary v)))) v56_l275)))


(def
 v59_l288
 (->
  (for
   [r (range 5) c (range 5)]
   {:r r, :c c, :v (Math/pow 10.0 (/ (+ r c) 2.0))})
  (pj/lay-tile :r :c {:fill :v})
  (pj/scale :fill :log)))


(deftest
 t60_l293
 (is ((fn [v] (>= (:visible-tiles (pj/svg-summary v)) 25)) v59_l288)))


(def
 v62_l307
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point
   :sepal-length
   :sepal-width
   {:color :species, :alpha 0.5, :size 5})))


(deftest
 t63_l310
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
   v62_l307)))


(def
 v65_l318
 (-> {:x [1 2 3 4 5], :y [2 4 3 5 4]} (pj/lay-line :x :y {:size 3})))


(deftest
 t66_l321
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v65_l318)))


(def
 v68_l325
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species {:alpha 0.4})))


(deftest
 t69_l328
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:polygons s)) (contains? (:alphas s) 0.4))))
   v68_l325)))


(def
 v71_l351
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
 t72_l360
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
      (filter (fn* [p1__85820#] (= :text (:mark p1__85820#))))
      (mapv (fn* [p1__85821#] (-> p1__85821# :style :align-x))))))
   v71_l351)))


(def
 v74_l371
 (->
  {:species ["setosa" "versicolor" "virginica"], :pct [33.3 33.3 33.3]}
  (pj/lay-value-bar :species :pct {:color "#a6cee3"})
  (pj/lay-text
   :species
   :pct
   {:text :pct, :align-x :center, :align-y :bottom})))


(deftest
 t75_l376
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
       (filter (fn* [p1__85822#] (= :text (:mark p1__85822#))))
       first
       :style)]
     (and
      (= :center (:align-x text-style))
      (= :bottom (:align-y text-style)))))
   v74_l371)))


(deftest
 t77_l388
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
        (filter (fn* [p1__85823#] (= mark (:mark p1__85823#))))
        first
        :style
        ((fn*
          [p1__85824#]
          (select-keys p1__85824# [:align-x :align-y])))))]
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
   v74_l371)))


(def v79_l421 (:band-opacity (pj/config)))


(deftest t80_l423 (is ((fn [v] (= 0.15 v)) v79_l421)))


(def
 v82_l427
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/lay-band-v {:x-min 5.5, :x-max 6.5})
  (pj/lay-band-h {:y-min 3.0, :y-max 3.5, :alpha 0.3})))


(deftest
 t83_l432
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s)))) v82_l427)))


(def
 v85_l457
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette ["#E74C3C" "#3498DB" "#2ECC71"]})))


(deftest
 t86_l461
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v85_l457)))


(def
 v88_l465
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :dark2})))


(deftest
 t89_l469
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v88_l465)))


(def v91_l481 (c2d/find-palette #"budapest"))


(deftest
 t92_l483
 (is
  ((fn [v] (and (sequential? v) (some #{:grand-budapest-1} v)))
   v91_l481)))


(def v94_l487 (c2d/find-palette #"^:set"))


(deftest
 t95_l489
 (is ((fn [v] (and (sequential? v) (some #{:set1} v))) v94_l487)))


(def v97_l493 (c2d/find-gradient #"viridis"))


(deftest
 t98_l495
 (is
  ((fn [v] (and (sequential? v) (some #{:viridis/viridis} v)))
   v97_l493)))


(def v100_l500 (c2d/palette :grand-budapest-1))


(deftest
 t101_l502
 (is ((fn [v] (and (sequential? v) (pos? (count v)))) v100_l500)))


(def
 v103_l514
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:palette :khroma/okabeito})))


(deftest
 t104_l518
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v103_l514)))


(def
 v106_l524
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options
   {:title "White Theme",
    :theme {:bg "#FFFFFF", :grid "#EEEEEE", :font-size 10}})))


(deftest
 t107_l529
 (is
  ((fn [v] (let [s (pj/svg-summary v)] (= 150 (:points s))))
   v106_l524)))


(def
 v109_l537
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :bottom})))


(deftest
 t110_l541
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 150 (:points s)) (< (:width s) 700))))
   v109_l537)))


(def
 v112_l547
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :top})))


(deftest
 t113_l551
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v112_l547)))


(def
 v115_l557
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/options {:legend-position :none})))


(deftest
 t116_l561
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
   v115_l557)))
