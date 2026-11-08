(ns
 plotje-book.faceting-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l32
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t4_l36
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      panels
      (:panels
       (pj/plan
        (->
         (rdatasets/datasets-iris)
         (pj/lay-point :sepal-length :sepal-width {:color :species})
         (pj/facet :species))))
      x-doms
      (mapv :x-domain panels)
      y-doms
      (mapv :y-domain panels)]
     (and
      (= 3 (:panels s))
      (= 150 (:points s))
      (= #{0} (set (map :row panels)))
      (= [0 1 2] (mapv :col panels))
      (apply = x-doms)
      (apply = y-doms))))
   v3_l32)))


(def
 v6_l65
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :col {:domain ["r" "f" "4"]})))


(deftest
 t7_l70
 (is
  ((fn
    [v]
    (=
     ["r" "f" "4"]
     (filterv
      #{"4" "f" "r"}
      (drop-while
       (fn* [p1__85712#] (not= "r" p1__85712#))
       (:texts (pj/svg-summary v))))))
   v6_l65)))


(def
 v9_l80
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :drv})
  (pj/facet :drv)
  (pj/scale :col {:domain ["r" "f" "4"]})
  (pj/scale :color {:domain ["r" "f" "4"]})))


(deftest
 t10_l86
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (=
      ["r" "f" "4"]
      (mapv :col-label (:panels plan))
      (mapv :label (:entries (:legend plan))))))
   v9_l80)))


(def
 v12_l96
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :col {:domain ["r" "f" "4" "x"]})))


(deftest
 t13_l101
 (is
  ((fn
    [v]
    (let
     [printed
      (with-out-str (pj/plan v))
      omitting
      (->
       (rdatasets/ggplot2-mpg)
       (pj/lay-point :displ :hwy)
       (pj/facet :drv)
       (pj/scale :col {:domain ["r" "f"]}))]
     (and
      (= 3 (:panels (pj/svg-summary v)))
      (re-find
       #":domain names \[.x.\], which the data does not hold"
       printed)
      (= 3 (:panels (pj/svg-summary omitting)))
      (re-find
       #":domain omits \[.4.\]"
       (with-out-str (pj/plan omitting))))))
   v12_l96)))


(def
 v15_l119
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :class {:cols 4})))


(deftest
 t16_l123
 (is
  ((fn
    [v]
    (let
     [boxes
      (map (comp :panel-box :frames) (:panels (pj/frames v)))
      texts
      (set (:texts (pj/svg-summary v)))
      panels
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       panel)]
     (and
      (= 7 (:panels (pj/svg-summary v)))
      (apply = (map :x-domain panels))
      (apply = (map :y-domain panels))
      (= 4 (count (distinct (map first boxes))))
      (= 2 (count (distinct (map second boxes))))
      (every?
       texts
       ["compact"
        "midsize"
        "suv"
        "2seater"
        "minivan"
        "pickup"
        "subcompact"]))))
   v15_l119)))


(def
 v18_l148
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet {:row :species})))


(deftest
 t19_l152
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v18_l148)))


(def
 v21_l160
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t22_l164
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v21_l160)))


(def
 v24_l173
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex} {:strip-labels :both})))


(deftest
 t25_l177
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (texts "smoker: No") (texts "sex: Female"))))
   v24_l173)))


(def
 v27_l189
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t28_l193
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      boxes
      (map (comp :panel-box :frames) (:panels (pj/frames v)))]
     (and
      (= 3 (:panels s))
      (pos? (:polygons s))
      (= 3 (count (distinct (map first boxes))))
      (= 1 (count (distinct (map second boxes)))))))
   v27_l189)))


(def
 v30_l206
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  (pj/lay-smooth {:color "black"})
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t31_l211
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      boxes
      (map (comp :panel-box :frames) (:panels (pj/frames v)))]
     (and
      (= 2 (:panels s))
      (= 2 (:lines s))
      (zero? (:points s))
      (= 1 (count (distinct (map first boxes))))
      (= 2 (count (distinct (map second boxes)))))))
   v30_l206)))


(def
 v33_l229
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-smooth :displ :hwy {:color "black"})
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t34_l233
 (is
  ((fn
    [v]
    (and
     (=
      [[:line] [:point]]
      (mapv
       (fn*
        [p1__85713#]
        (mapv :mark (-> p1__85713# :plan :panels first :layers)))
       (-> v pj/plan :sub-plots)))
     (= "" (with-out-str (pj/plan v)))))
   v33_l229)))


(def
 v36_l268
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})))


(deftest
 t37_l273
 (is
  ((fn
    [v]
    (let
     [base
      (->
       (rdatasets/datasets-iris)
       (pj/pose {:x :sepal-length})
       pj/lay-point)
      ys
      [:sepal-width :petal-width]]
     (and
      (=
       {:panels 6, :points 300}
       (select-keys (pj/svg-summary v) [:panels :points]))
      (= v (-> base (pj/facet :species) (pj/facet {:y ys})))
      (= v (-> base (pj/facet {:y ys}) (pj/facet :species)))
      (let
       [m
        (try
         (pj/facet v {:row :species})
         nil
         (catch clojure.lang.ExceptionInfo e (ex-message e)))]
       (and
        (re-find #"both directions" m)
        (re-find #":color" m)
        (re-find #"pj/arrange" m))))))
   v36_l268)))


(def
 v39_l312
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width :petal-length]},
    :row {:y [:sepal-length :sepal-width :petal-length]}})
  pj/lay-point))


(deftest
 t40_l318
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 9 (:panels s))
      (= (* 6 150) (:points s))
      (pos? (:polygons s))
      (let
       [panels
        (into
         {}
         (map
          (juxt
           :path
           (fn* [p1__85714#] (first (:panels (:plan p1__85714#))))))
         (:sub-plots (pj/plan v)))]
       (every?
        (fn
         [r]
         (=
          (:y-domain (panels [r r]))
          (:y-domain (panels [r (mod (inc r) 3)]))))
        (range 3))))))
   v39_l312)))


(def
 v42_l334
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix [:sepal-length :sepal-width :petal-length])
  pj/lay-point))


(deftest
 t43_l339
 (is
  ((fn
    [v]
    (=
     v
     (->
      (rdatasets/datasets-iris)
      (pj/pose {:color :species})
      (pj/facet
       {:col {:x [:sepal-length :sepal-width :petal-length]},
        :row {:y [:sepal-length :sepal-width :petal-length]}})
      pj/lay-point)))
   v42_l334)))


(def
 v45_l366
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t46_l371
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v45_l366)))


(def
 v48_l377
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v49_l381
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t50_l386
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v49_l381)))


(def
 v52_l392
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet {:col :drv, :row :cyl})
  kind/pprint))


(deftest
 t53_l397
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v52_l392)))


(def
 v55_l404
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t56_l408
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (pj/svg-summary
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width)
       (pj/facet :species)))))
   v55_l404)))


(def
 v58_l425
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t59_l431
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v58_l425)))


(deftest
 t61_l443
 (is
  ((fn
    [_]
    (try
     (pj/facet
      (pj/arrange
       [(pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
        (pj/lay-point (rdatasets/ggplot2-mpg) :displ :cty)])
      {:y [:hwy :cty]})
     false
     (catch
      clojure.lang.ExceptionInfo
      e
      (boolean (re-find #"on a composite pose" (ex-message e))))))
   v58_l425)))


(def
 v63_l458
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t64_l464
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__85715#] (count (:panels (:plan p1__85715#))))
       (:sub-plots (pj/plan fr))))))
   v63_l458)))


(def
 v66_l477
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t67_l483
 (is ((fn [fr] (= 3 (:panels (pj/svg-summary fr)))) v66_l477)))


(def
 v69_l492
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet
   {:col :species,
    :row
    {:x [:sepal-length :petal-length],
     :y [:sepal-width :petal-width]}})
  (pj/options {:width 900, :height 480})))


(deftest
 t70_l500
 (is
  ((fn
    [fr]
    (and
     (=
      {:panels 6, :points 300}
      (select-keys (pj/svg-summary fr) [:panels :points]))
     (=
      fr
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width)
       (pj/lay-point :petal-length :petal-width)
       (pj/facet
        {:x [:sepal-length :petal-length],
         :y [:sepal-width :petal-width]})
       (pj/facet :species)
       (pj/options {:width 900, :height 480})))
     (=
      fr
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width)
       (pj/lay-point :petal-length :petal-width)
       (pj/facet :species)
       (pj/facet
        {:x [:sepal-length :petal-length],
         :y [:sepal-width :petal-width]})
       (pj/options {:width 900, :height 480})))))
   v69_l492)))


(def
 v72_l526
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t73_l532
 (is
  ((fn
    [fr]
    (let
     [s
      (pj/svg-summary fr)
      labelled
      (->>
       (pj/plan fr)
       :sub-plots
       (mapcat (comp :panels :plan))
       (keep :col-label))]
     (and
      (= 6 (:panels s))
      (= 150 (:points s))
      (= ["setosa" "versicolor" "virginica"] (vec labelled)))))
   v72_l526)))


(def v75_l552 (pj/compound-key-aesthetics))


(deftest t76_l554 (is ((fn [s] (= #{:group :col :row} s)) v75_l552)))


(def
 v78_l559
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v79_l565 measures)


(def
 v81_l570
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t82_l576
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v81_l570)))


(def
 v84_l582
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t85_l586
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v84_l582)))


(def
 v87_l595
 (->
  measures
  (pj/lay-point :t :v)
  (pj/facet {:col :part, :row :dimension})))


(deftest
 t88_l599
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v87_l595)))


(def
 v90_l621
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t91_l626
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      default
      (->
       (rdatasets/datasets-iris)
       (pj/lay-point :sepal-length :sepal-width {:color :species})
       (pj/facet :species))
      doms
      (mapv (juxt :x-domain :y-domain) (:panels (pj/plan v)))]
     (and
      (= 3 (:panels s))
      (= 150 (:points s))
      (apply = doms)
      (= (pj/plot v) (pj/plot default)))))
   v90_l621)))


(def
 v93_l641
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:x}})))


(deftest
 t94_l646
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      ranges
      (fn
       [axes]
       (let
        [panels
         (:panels
          (pj/plan
           (->
            (rdatasets/datasets-iris)
            (pj/lay-point :sepal-length :sepal-width)
            (pj/facet :species)
            (pj/options {:share-scales axes}))))]
        [(count (distinct (map :x-domain panels)))
         (count (distinct (map :y-domain panels)))]))]
     (and
      (= 3 (:panels s))
      (= [1 3] (ranges #{:x}))
      (= [3 1] (ranges #{:y}))
      (= [3 3] (ranges #{})))))
   v93_l641)))


(def
 v96_l673
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species {:share-scales #{:x}})))


(deftest
 t97_l677
 (is
  ((fn
    [v]
    (=
     v
     (->
      (rdatasets/datasets-iris)
      (pj/lay-point :sepal-length :sepal-width {:color :species})
      (pj/facet :species)
      (pj/options {:share-scales #{:x}}))))
   v96_l673)))


(def
 v99_l688
 (let
  [whole
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width))
   domains
   (fn
    [pose]
    (mapv (juxt :x-domain :y-domain) (:panels (pj/plan pose))))]
  {:not-split (first (domains whole)),
   :each-panel (distinct (domains (pj/facet whole :species)))}))


(deftest
 t100_l694
 (is
  ((fn [{:keys [not-split each-panel]}] (= [not-split] each-panel))
   v99_l688)))


(def
 v102_l719
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v103_l725
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t104_l729
 (is
  ((fn
    [v]
    (let
     [layers (->> v pj/plan :panels (mapcat :layers))]
     (and
      (= 2 (count layers))
      (= [[1 10] [1 10]] (mapv :size-extent layers))
      (=
       [[1 2 3] [4 7 10]]
       (mapv (fn [l] (vec (mapcat :sizes (:groups l)))) layers)))))
   v103_l725)))


(def
 v106_l753
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :class})
  (pj/facet :drv)))


(deftest
 t107_l757
 (is
  ((fn
    [v]
    (let
     [labels
      (fn [pose] (mapv :label (:entries (:legend (pj/plan pose)))))
      mpg
      (rdatasets/ggplot2-mpg)]
     (and
      (=
       (labels v)
       (labels (pj/lay-point mpg :displ :hwy {:color :class}))
       (vec (distinct (:class mpg))))
      (not=
       (vec (distinct (:class mpg)))
       (vec
        (distinct
         (mapcat
          (fn
           [drv]
           (:class
            (tc/select-rows
             mpg
             (fn* [p1__85716#] (= drv (:drv p1__85716#))))))
          (distinct (:drv mpg)))))))))
   v106_l753)))


(def
 v109_l773
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t110_l778
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v109_l773)))


(def
 v112_l795
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t113_l801
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"panel aesthetic" m)
     (re-find #"read from a pose's mapping" m)
     (re-find #"pj/facet pose" m)
     (re-find #"in the pose's mapping" m)))
   v112_l795)))


(def
 v115_l817
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t116_l824
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v115_l817)))


(def
 v118_l832
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet {:col :species, :share-scales #{:x}}))
  (catch Exception e (ex-message e))))


(deftest
 t119_l838
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"unexpected key\(s\): \[:share-scales\]" m)
     (re-find #"in a map of its own after this one" m)))
   v118_l832)))


(def
 v121_l849
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/facet :model)
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t122_l856
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"a facet by :model" m)
     (re-find #"holds 38 values" m)
     (re-find #"\(pj/facet pose :model \{:cols 4 :height 1200\}\)" m)))
   v121_l849)))


(def
 v124_l877
 (pj/arrange
  (vec
   (for
    [drive ["4" "f" "r"]]
    (->
     (rdatasets/ggplot2-mpg)
     (tc/select-rows (fn* [p1__85717#] (= drive (:drv p1__85717#))))
     (pj/lay-point :displ :hwy)
     (pj/options {:title drive}))))))


(deftest
 t125_l883
 (is
  ((fn
    [v]
    (let
     [y-domains
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       (:y-domain panel))]
     (and (= 3 (count y-domains)) (= 3 (count (distinct y-domains))))))
   v124_l877)))


(deftest
 t127_l895
 (is
  ((fn
    [_]
    (let
     [arranged
      (pj/arrange
       (vec
        (for
         [drive ["4" "f" "r"]]
         (->
          (rdatasets/ggplot2-mpg)
          (tc/select-rows
           (fn* [p1__85718#] (= drive (:drv p1__85718#))))
          (pj/lay-point :displ :hwy))))
       {:share-scales #{:y}})]
     (apply
      =
      (for
       [sp (:sub-plots (pj/plan arranged)) panel (:panels (:plan sp))]
       (:y-domain panel)))))
   v124_l877)))


(def
 v129_l909
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)))


(deftest
 t130_l913
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 3 (count panels))
      (apply = (map :y-domain panels))
      (apply = (map :x-domain panels)))))
   v129_l909)))


(def
 v132_l925
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/arrange
   [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}])))


(deftest
 t133_l929
 (is
  ((fn
    [v]
    (=
     (pj/plot v)
     (pj/plot
      (->
       (rdatasets/ggplot2-mpg)
       (pj/lay-point :displ :hwy)
       (pj/facet :drv)))))
   v132_l925)))


(def
 v135_l945
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/lay-point :displ :cty)))


(deftest
 t136_l950
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (not (contains? v :poses))
      (= 3 (:panels s))
      (= 468 (:points s))
      (=
       ["hwy" "cty"]
       (mapv :label (:entries (:legend (pj/plan v))))))))
   v135_l945)))


(def
 v138_l963
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/arrange
   [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}])
  (pj/lay-point :displ :cty)))


(deftest
 t139_l968
 (is
  ((fn
    [v]
    (and
     (contains? v :poses)
     (=
      (pj/plot v)
      (pj/plot
       (->
        (rdatasets/ggplot2-mpg)
        (pj/lay-point :displ :hwy)
        (pj/facet :drv)
        (pj/lay-point :displ :cty))))))
   v138_l963)))


(def
 v141_l981
 (pj/arrange (rdatasets/ggplot2-mpg) [{:x :displ, :y :hwy} :hwy]))


(deftest
 t142_l983
 (is
  ((fn
    [v]
    (and
     (= 2 (:panels (pj/svg-summary v)))
     (=
      [:point :bar]
      (for
       [sp
        (:sub-plots (pj/plan v))
        panel
        (:panels (:plan sp))
        layer
        (:layers panel)]
       (:mark layer)))))
   v141_l981)))


(def
 v144_l997
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/arrange
   [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]
   {:cols 2})))


(deftest
 t145_l1002
 (is
  ((fn
    [v]
    (let
     [boxes
      (map (comp :panel-box :frames) (:panels (pj/frames v)))
      panels
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       panel)
      texts
      (set (:texts (pj/svg-summary v)))]
     (and
      (= 3 (count panels))
      (= 2 (count (distinct (map first boxes))))
      (= 2 (count (distinct (map second boxes))))
      (every? texts ["f" "4" "r"])
      (apply = (map :x-domain panels))
      (apply = (map :y-domain panels))
      (=
       (pj/plot v)
       (pj/plot
        (->
         (rdatasets/ggplot2-mpg)
         (pj/lay-point :displ :hwy)
         (pj/facet :drv {:cols 2})))))))
   v144_l997)))


(def
 v147_l1045
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :y {:domain [0 50]})
  pj/lay-smooth))


(def
 v148_l1051
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  pj/lay-smooth
  (pj/scale :y {:domain [0 50]})
  (pj/facet :drv)))


(deftest
 t149_l1057
 (is
  ((fn
    [v]
    (let
     [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
     (and
      (=
       (pj/plot v)
       (pj/plot
        (->
         p
         (pj/facet :drv)
         (pj/scale :y {:domain [0 50]})
         pj/lay-smooth)))
      (=
       (-> p (pj/facet :drv) (pj/options {:title "T"}))
       (-> p (pj/options {:title "T"}) (pj/facet :drv)))
      (=
       (-> p (pj/facet :drv) (pj/coord :flip))
       (-> p (pj/coord :flip) (pj/facet :drv))))))
   v148_l1051)))


(deftest
 t151_l1081
 (is
  ((fn
    [_]
    (=
     (->
      (rdatasets/ggplot2-mpg)
      (pj/pose {:x :displ})
      pj/lay-point
      (pj/facet {:y [:hwy :cty]}))
     (->
      (rdatasets/ggplot2-mpg)
      (pj/pose {:x :displ})
      (pj/facet {:y [:hwy :cty]})
      pj/lay-point)))
   v148_l1051)))


(def
 v153_l1126
 (let
  [mpg (rdatasets/ggplot2-mpg)]
  (->
   (tc/concat mpg (tc/add-column mpg :drv "(all)"))
   (pj/lay-point :displ :hwy)
   (pj/facet :drv))))


(deftest
 t154_l1131
 (is
  ((fn
    [v]
    (let
     [mpg (rdatasets/ggplot2-mpg) s (pj/svg-summary v)]
     (and
      (= (inc (count (distinct (mpg :drv)))) (:panels s))
      (= (* 2 (tc/row-count mpg)) (:points s))
      (some #{"(all)"} (:texts s)))))
   v153_l1126)))


(def
 v156_l1147
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t157_l1151
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v156_l1147)))


(def
 v159_l1159
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t160_l1165
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v159_l1159)))


(def
 v162_l1176
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)
  (pj/options {:width 800})))


(deftest
 t163_l1181
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v162_l1176)))


(def
 v165_l1189
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t166_l1195
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and
      (= 3 (:panels s))
      (= 150 (:points s))
      (some #{"Iris by Species"} (:texts s))
      (some #{"Sepal Length (cm)"} (:texts s)))))
   v165_l1189)))
