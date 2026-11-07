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
       (fn* [p1__85919#] (not= "r" p1__85919#))
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
 v15_l116
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet {:row :species})))


(deftest
 t16_l120
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v15_l116)))


(def
 v18_l128
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t19_l132
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v18_l128)))


(def
 v21_l141
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex} {:strip-labels :both})))


(deftest
 t22_l145
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (texts "smoker: No") (texts "sex: Female"))))
   v21_l141)))


(def
 v24_l157
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t25_l161
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
   v24_l157)))


(def
 v27_l174
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  (pj/lay-smooth {:color "black"})
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t28_l179
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
   v27_l174)))


(def
 v30_l197
 (with-out-str
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-smooth :displ :hwy {:color "black"})
   (pj/facet {:y [:hwy :cty]})
   pj/plan)))


(deftest
 t31_l203
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
   v30_l197)))


(def
 v33_l237
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})))


(deftest
 t34_l242
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
   v33_l237)))


(def
 v36_l277
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width :petal-length]},
    :row {:y [:sepal-length :sepal-width :petal-length]}})
  pj/lay-point))


(deftest
 t37_l283
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
           (fn* [p1__85920#] (first (:panels (:plan p1__85920#))))))
         (:sub-plots (pj/plan v)))]
       (every?
        (fn
         [r]
         (=
          (:y-domain (panels [r r]))
          (:y-domain (panels [r (mod (inc r) 3)]))))
        (range 3))))))
   v36_l277)))


(def
 v39_l299
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/matrix [:sepal-length :sepal-width :petal-length])
  pj/lay-point))


(deftest
 t40_l304
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
   v39_l299)))


(def
 v42_l328
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t43_l333
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v42_l328)))


(def
 v45_l339
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v46_l343
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t47_l348
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v46_l343)))


(def
 v49_l354
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet {:col :drv, :row :cyl})
  kind/pprint))


(deftest
 t50_l359
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v49_l354)))


(def
 v52_l366
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t53_l370
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
   v52_l366)))


(def
 v55_l387
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t56_l393
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v55_l387)))


(deftest
 t58_l405
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
   v55_l387)))


(def
 v60_l420
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t61_l426
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__85921#] (count (:panels (:plan p1__85921#))))
       (:sub-plots (pj/plan fr))))))
   v60_l420)))


(def
 v63_l439
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t64_l445
 (is ((fn [fr] (= 3 (:panels (pj/svg-summary fr)))) v63_l439)))


(def
 v66_l454
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
 t67_l462
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
   v66_l454)))


(def
 v69_l488
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t70_l494
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
   v69_l488)))


(def v72_l513 (pj/compound-key-aesthetics))


(deftest t73_l515 (is ((fn [s] (= #{:group :col :row} s)) v72_l513)))


(def
 v75_l520
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v76_l526 measures)


(def
 v78_l531
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t79_l537
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v78_l531)))


(def
 v81_l543
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t82_l547
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v81_l543)))


(def
 v84_l556
 (->
  measures
  (pj/lay-point :t :v)
  (pj/facet {:col :part, :row :dimension})))


(deftest
 t85_l560
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v84_l556)))


(def
 v87_l582
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t88_l587
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
   v87_l582)))


(def
 v90_l602
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:x}})))


(deftest
 t91_l607
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
   v90_l602)))


(def
 v93_l634
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species {:share-scales #{:x}})))


(deftest
 t94_l638
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
   v93_l634)))


(def
 v96_l649
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
 t97_l655
 (is
  ((fn [{:keys [not-split each-panel]}] (= [not-split] each-panel))
   v96_l649)))


(def
 v99_l680
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v100_l686
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t101_l690
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
   v100_l686)))


(def
 v103_l711
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :class})
  (pj/facet :drv)))


(deftest
 t104_l715
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
             (fn* [p1__85922#] (= drv (:drv p1__85922#))))))
          (distinct (:drv mpg)))))))))
   v103_l711)))


(def
 v106_l731
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t107_l736
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v106_l731)))


(def
 v109_l753
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t110_l759
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"panel aesthetic" m)
     (re-find #"read from a pose's mapping" m)
     (re-find #"pj/facet pose" m)
     (re-find #"in the pose's mapping" m)))
   v109_l753)))


(def
 v112_l775
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t113_l782
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v112_l775)))


(def
 v115_l790
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet {:col :species, :share-scales #{:x}}))
  (catch Exception e (ex-message e))))


(deftest
 t116_l796
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"unexpected key\(s\): \[:share-scales\]" m)
     (re-find #"in a map of its own after this one" m)))
   v115_l790)))


(def
 v118_l808
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/facet :model)
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t119_l815
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"a facet by :model" m)
     (re-find #"does not wrap its panels" m)
     (re-find #"pj/arrange poses" m)))
   v118_l808)))


(def
 v121_l836
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t122_l841
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      plan
      (pj/plan v)
      x-domains
      (for
       [sp (:sub-plots plan) panel (:panels (:plan sp))]
       (:x-domain panel))]
     (and
      (= 2 (:panels s))
      (= 468 (:points s))
      (apply = x-domains)
      (= 1 (count (filter #{"displ"} (:texts s)))))))
   v121_l836)))


(def
 v124_l857
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} {:x :displ, :y :cty}] {:cols 1})
  pj/lay-point))


(deftest
 t125_l861
 (is
  ((fn
    [v]
    (let
     [s
      (pj/svg-summary v)
      domains
      (fn
       [pose]
       (vec
        (for
         [sp (:sub-plots (pj/plan pose)) panel (:panels (:plan sp))]
         [(:x-domain panel) (:y-domain panel)])))
      faceted
      (->
       (rdatasets/ggplot2-mpg)
       (pj/pose {:x :displ})
       pj/lay-point
       (pj/facet {:y [:hwy :cty]}))]
     (and
      (= 2 (:panels s))
      (= 468 (:points s))
      (= (domains faceted) (domains v))
      (= 1 (count (filter #{"displ"} (:texts s)))))))
   v124_l857)))


(def
 v127_l887
 (pj/arrange
  (vec
   (for
    [drive ["4" "f" "r"]]
    (->
     (rdatasets/ggplot2-mpg)
     (tc/select-rows (fn* [p1__85923#] (= drive (:drv p1__85923#))))
     (pj/lay-point :displ :hwy)
     (pj/options {:title drive}))))))


(deftest
 t128_l893
 (is
  ((fn
    [v]
    (let
     [y-domains
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       (:y-domain panel))]
     (and (= 3 (count y-domains)) (= 3 (count (distinct y-domains))))))
   v127_l887)))


(def
 v130_l904
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)))


(deftest
 t131_l908
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 3 (count panels))
      (apply = (map :y-domain panels))
      (apply = (map :x-domain panels)))))
   v130_l904)))


(def
 v133_l920
 (pj/arrange
  (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
  [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]))


(deftest
 t134_l923
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
   v133_l920)))


(deftest
 t136_l939
 (is
  ((fn
    [v]
    (let
     [faceted
      (->
       (rdatasets/ggplot2-mpg)
       (pj/lay-point :displ :hwy)
       (pj/facet :drv))]
     (and
      (contains? v :poses)
      (not (contains? faceted :poses))
      (=
       3
       (:panels (pj/svg-summary (pj/lay-point faceted :displ :cty))))
      (= 4 (:panels (pj/svg-summary (pj/lay-point v :displ :cty)))))))
   v133_l920)))


(deftest
 t138_l953
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
           (fn* [p1__85924#] (= drive (:drv p1__85924#))))
          (pj/lay-point :displ :hwy))))
       {:share-scales #{:y}})]
     (apply
      =
      (for
       [sp (:sub-plots (pj/plan arranged)) panel (:panels (:plan sp))]
       (:y-domain panel)))))
   v133_l920)))


(def
 v140_l981
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :y {:domain [0 50]})
  pj/lay-smooth))


(def
 v141_l987
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  pj/lay-smooth
  (pj/scale :y {:domain [0 50]})
  (pj/facet :drv)))


(deftest
 t142_l993
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
   v141_l987)))


(deftest
 t144_l1009
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
   v141_l987)))


(def
 v146_l1043
 (let
  [mpg (rdatasets/ggplot2-mpg)]
  (->
   (tc/concat mpg (tc/add-column mpg :drv "(all)"))
   (pj/lay-point :displ :hwy)
   (pj/facet :drv))))


(deftest
 t147_l1048
 (is
  ((fn
    [v]
    (let
     [mpg (rdatasets/ggplot2-mpg) s (pj/svg-summary v)]
     (and
      (= (inc (count (distinct (mpg :drv)))) (:panels s))
      (= (* 2 (tc/row-count mpg)) (:points s))
      (some #{"(all)"} (:texts s)))))
   v146_l1043)))


(def
 v149_l1064
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t150_l1068
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v149_l1064)))


(def
 v152_l1076
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t153_l1082
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v152_l1076)))


(def
 v155_l1093
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)
  (pj/options {:width 800})))


(deftest
 t156_l1098
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v155_l1093)))


(def
 v158_l1106
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t159_l1112
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
   v158_l1106)))
