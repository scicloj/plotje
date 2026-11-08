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
       (fn* [p1__83472#] (not= "r" p1__83472#))
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
 (with-out-str
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-smooth :displ :hwy {:color "black"})
   (pj/facet {:y [:hwy :cty]})
   pj/plan)))


(deftest
 t34_l235
 (is
  ((fn
    [out]
    (and
     (re-find #"pj/lay-smooth names :hwy on :y" out)
     (re-find #"\{:y :cty\} has no layer" out)
     (re-find #"leave :y off the layer and name :x on the pose" out)
     (=
      ""
      (with-out-str
       (->
        (rdatasets/ggplot2-mpg)
        (pj/lay-point :displ :hwy)
        (pj/facet {:y [:hwy :cty]})
        pj/plan)))))
   v33_l229)))


(def
 v36_l269
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})))


(deftest
 t37_l274
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
   v36_l269)))


(def
 v39_l313
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width :petal-length]},
    :row {:y [:sepal-length :sepal-width :petal-length]}})
  pj/lay-point))


(deftest
 t40_l319
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
           (fn* [p1__83473#] (first (:panels (:plan p1__83473#))))))
         (:sub-plots (pj/plan v)))]
       (every?
        (fn
         [r]
         (=
          (:y-domain (panels [r r]))
          (:y-domain (panels [r (mod (inc r) 3)]))))
        (range 3))))))
   v39_l313)))


(def
 v42_l335
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix [:sepal-length :sepal-width :petal-length])
  pj/lay-point))


(deftest
 t43_l340
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
   v42_l335)))


(def
 v45_l367
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t46_l372
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v45_l367)))


(def
 v48_l378
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v49_l382
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t50_l387
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v49_l382)))


(def
 v52_l393
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet {:col :drv, :row :cyl})
  kind/pprint))


(deftest
 t53_l398
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v52_l393)))


(def
 v55_l405
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t56_l409
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
   v55_l405)))


(def
 v58_l426
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t59_l432
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v58_l426)))


(deftest
 t61_l444
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
   v58_l426)))


(def
 v63_l459
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t64_l465
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__83474#] (count (:panels (:plan p1__83474#))))
       (:sub-plots (pj/plan fr))))))
   v63_l459)))


(def
 v66_l478
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t67_l484
 (is ((fn [fr] (= 3 (:panels (pj/svg-summary fr)))) v66_l478)))


(def
 v69_l493
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
 t70_l501
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
   v69_l493)))


(def
 v72_l527
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t73_l533
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
   v72_l527)))


(def v75_l553 (pj/compound-key-aesthetics))


(deftest t76_l555 (is ((fn [s] (= #{:group :col :row} s)) v75_l553)))


(def
 v78_l560
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v79_l566 measures)


(def
 v81_l571
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t82_l577
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v81_l571)))


(def
 v84_l583
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t85_l587
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v84_l583)))


(def
 v87_l596
 (->
  measures
  (pj/lay-point :t :v)
  (pj/facet {:col :part, :row :dimension})))


(deftest
 t88_l600
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v87_l596)))


(def
 v90_l622
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t91_l627
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
   v90_l622)))


(def
 v93_l642
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:x}})))


(deftest
 t94_l647
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
   v93_l642)))


(def
 v96_l674
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species {:share-scales #{:x}})))


(deftest
 t97_l678
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
   v96_l674)))


(def
 v99_l689
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
 t100_l695
 (is
  ((fn [{:keys [not-split each-panel]}] (= [not-split] each-panel))
   v99_l689)))


(def
 v102_l720
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v103_l726
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t104_l730
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
   v103_l726)))


(def
 v106_l754
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :class})
  (pj/facet :drv)))


(deftest
 t107_l758
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
             (fn* [p1__83475#] (= drv (:drv p1__83475#))))))
          (distinct (:drv mpg)))))))))
   v106_l754)))


(def
 v109_l774
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t110_l779
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v109_l774)))


(def
 v112_l796
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t113_l802
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"panel aesthetic" m)
     (re-find #"read from a pose's mapping" m)
     (re-find #"pj/facet pose" m)
     (re-find #"in the pose's mapping" m)))
   v112_l796)))


(def
 v115_l818
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t116_l825
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v115_l818)))


(def
 v118_l833
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet {:col :species, :share-scales #{:x}}))
  (catch Exception e (ex-message e))))


(deftest
 t119_l839
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"unexpected key\(s\): \[:share-scales\]" m)
     (re-find #"in a map of its own after this one" m)))
   v118_l833)))


(def
 v121_l850
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/facet :model)
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t122_l857
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"a facet by :model" m)
     (re-find #"holds 38 values" m)
     (re-find #"\(pj/facet pose :model \{:cols 4 :height 1200\}\)" m)))
   v121_l850)))


(def
 v124_l878
 (pj/arrange
  (vec
   (for
    [drive ["4" "f" "r"]]
    (->
     (rdatasets/ggplot2-mpg)
     (tc/select-rows (fn* [p1__83476#] (= drive (:drv p1__83476#))))
     (pj/lay-point :displ :hwy)
     (pj/options {:title drive}))))))


(deftest
 t125_l884
 (is
  ((fn
    [v]
    (let
     [y-domains
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       (:y-domain panel))]
     (and (= 3 (count y-domains)) (= 3 (count (distinct y-domains))))))
   v124_l878)))


(deftest
 t127_l896
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
           (fn* [p1__83477#] (= drive (:drv p1__83477#))))
          (pj/lay-point :displ :hwy))))
       {:share-scales #{:y}})]
     (apply
      =
      (for
       [sp (:sub-plots (pj/plan arranged)) panel (:panels (:plan sp))]
       (:y-domain panel)))))
   v124_l878)))


(def
 v129_l910
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)))


(deftest
 t130_l914
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 3 (count panels))
      (apply = (map :y-domain panels))
      (apply = (map :x-domain panels)))))
   v129_l910)))


(def
 v132_l926
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/arrange
   [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}])))


(deftest
 t133_l930
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
   v132_l926)))


(def
 v135_l946
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/lay-point :displ :cty)))


(deftest
 t136_l951
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
   v135_l946)))


(def
 v138_l964
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/arrange
   [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}])
  (pj/lay-point :displ :cty)))


(deftest
 t139_l969
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
   v138_l964)))


(def
 v141_l982
 (pj/arrange (rdatasets/ggplot2-mpg) [{:x :displ, :y :hwy} :hwy]))


(deftest
 t142_l984
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
   v141_l982)))


(def
 v144_l998
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/arrange
   [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]
   {:cols 2})))


(deftest
 t145_l1003
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
   v144_l998)))


(def
 v147_l1046
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :y {:domain [0 50]})
  pj/lay-smooth))


(def
 v148_l1052
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  pj/lay-smooth
  (pj/scale :y {:domain [0 50]})
  (pj/facet :drv)))


(deftest
 t149_l1058
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
   v148_l1052)))


(deftest
 t151_l1082
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
   v148_l1052)))


(def
 v153_l1127
 (let
  [mpg (rdatasets/ggplot2-mpg)]
  (->
   (tc/concat mpg (tc/add-column mpg :drv "(all)"))
   (pj/lay-point :displ :hwy)
   (pj/facet :drv))))


(deftest
 t154_l1132
 (is
  ((fn
    [v]
    (let
     [mpg (rdatasets/ggplot2-mpg) s (pj/svg-summary v)]
     (and
      (= (inc (count (distinct (mpg :drv)))) (:panels s))
      (= (* 2 (tc/row-count mpg)) (:points s))
      (some #{"(all)"} (:texts s)))))
   v153_l1127)))


(def
 v156_l1148
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t157_l1152
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v156_l1148)))


(def
 v159_l1160
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t160_l1166
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v159_l1160)))


(def
 v162_l1177
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)
  (pj/options {:width 800})))


(deftest
 t163_l1182
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v162_l1177)))


(def
 v165_l1190
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t166_l1196
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
   v165_l1190)))
