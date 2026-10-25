(ns
 plotje-book.faceting-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l33
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t4_l37
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
   v3_l33)))


(def
 v6_l66
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :col {:domain ["r" "f" "4"]})))


(deftest
 t7_l71
 (is
  ((fn
    [v]
    (=
     ["r" "f" "4"]
     (filterv
      #{"4" "f" "r"}
      (drop-while
       (fn* [p1__11196#] (not= "r" p1__11196#))
       (:texts (pj/svg-summary v))))))
   v6_l66)))


(def
 v9_l81
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :drv})
  (pj/facet :drv)
  (pj/scale :col {:domain ["r" "f" "4"]})
  (pj/scale :color {:domain ["r" "f" "4"]})))


(deftest
 t10_l87
 (is
  ((fn
    [v]
    (let
     [plan (pj/plan v)]
     (=
      ["r" "f" "4"]
      (mapv :col-label (:panels plan))
      (mapv :label (:entries (:legend plan))))))
   v9_l81)))


(def
 v12_l97
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :col {:domain ["r" "f" "4" "x"]})))


(deftest
 t13_l102
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
   v12_l97)))


(def
 v15_l117
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet {:row :species})))


(deftest
 t16_l121
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v15_l117)))


(def
 v18_l127
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t19_l131
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v18_l127)))


(def
 v21_l145
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t22_l149
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
   v21_l145)))


(def
 v24_l162
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  (pj/lay-smooth {:color "black"})
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t25_l167
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
   v24_l162)))


(def
 v27_l182
 (with-out-str
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-smooth :displ :hwy {:color "black"})
   (pj/facet {:y [:hwy :cty]})
   pj/plan)))


(deftest
 t28_l188
 (is
  ((fn
    [out]
    (and
     (re-find #"pj/lay-smooth names :hwy on :y" out)
     (re-find #"\{:y :cty\} has no layer" out)
     (re-find #"leave :y off the layer and name :x on the pose" out)))
   v27_l182)))


(def
 v30_l211
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t31_l216
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v30_l211)))


(def
 v33_l222
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v34_l226
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t35_l231
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v34_l226)))


(def
 v37_l237
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet-grid :drv :cyl)
  kind/pprint))


(deftest
 t38_l242
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v37_l237)))


(def
 v40_l249
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t41_l253
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
   v40_l249)))


(def
 v43_l270
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t44_l276
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v43_l270)))


(deftest
 t46_l288
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
   v43_l270)))


(def
 v48_l303
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t49_l309
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__11197#] (count (:panels (:plan p1__11197#))))
       (:sub-plots (pj/plan fr))))))
   v48_l303)))


(def
 v51_l322
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t52_l328
 (is ((fn [fr] (= 3 (:panels (pj/svg-summary fr)))) v51_l322)))


(def
 v54_l335
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
 t55_l343
 (is
  ((fn
    [fr]
    (=
     {:panels 6, :points 300}
     (select-keys (pj/svg-summary fr) [:panels :points])))
   v54_l335)))


(def
 v57_l355
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t58_l361
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
   v57_l355)))


(def v60_l379 (pj/compound-key-aesthetics))


(deftest t61_l381 (is ((fn [s] (= #{:group :col :row} s)) v60_l379)))


(def
 v63_l386
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v64_l392 measures)


(def
 v66_l397
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t67_l403
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v66_l397)))


(def
 v69_l409
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t70_l413
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v69_l409)))


(def
 v72_l422
 (-> measures (pj/lay-point :t :v) (pj/facet-grid :part :dimension)))


(deftest
 t73_l426
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v72_l422)))


(def
 v75_l440
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t76_l445
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
   v75_l440)))


(def
 v78_l460
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:x}})))


(deftest
 t79_l465
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
   v78_l460)))


(def
 v81_l490
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species {:share-scales #{:x}})))


(deftest
 t82_l494
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
   v81_l490)))


(def
 v84_l505
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
 t85_l511
 (is
  ((fn [{:keys [not-split each-panel]}] (= [not-split] each-panel))
   v84_l505)))


(def
 v87_l536
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v88_l542
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t89_l546
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
   v88_l542)))


(def
 v91_l567
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :class})
  (pj/facet :drv)))


(deftest
 t92_l571
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
             (fn* [p1__11198#] (= drv (:drv p1__11198#))))))
          (distinct (:drv mpg)))))))))
   v91_l567)))


(def
 v94_l587
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t95_l592
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v94_l587)))


(def
 v97_l609
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t98_l615
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"panel aesthetic" m)
     (re-find #"read from a pose's mapping" m)
     (re-find #"pj/facet pose" m)
     (re-find #"pj/facet-grid" m)
     (re-find #"in the pose's mapping" m)))
   v97_l609)))


(def
 v100_l632
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t101_l639
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v100_l632)))


(def
 v103_l647
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet {:col :species, :share-scales #{:x}}))
  (catch Exception e (ex-message e))))


(deftest
 t104_l653
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"unexpected key\(s\): \[:share-scales\]" m)
     (re-find #"in a map of its own after this one" m)))
   v103_l647)))


(def
 v106_l665
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/facet :model)
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t107_l672
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"a facet by :model" m)
     (re-find #"does not wrap its panels" m)
     (re-find #"pj/arrange poses" m)))
   v106_l665)))


(def
 v109_l693
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t110_l698
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
   v109_l693)))


(def
 v112_l713
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [[:displ :hwy] [:displ :cty]] {:cols 1})
  pj/lay-point))


(deftest
 t113_l717
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
   v112_l713)))


(def
 v115_l743
 (pj/arrange
  (vec
   (for
    [drive ["4" "f" "r"]]
    (->
     (rdatasets/ggplot2-mpg)
     (tc/select-rows (fn* [p1__11199#] (= drive (:drv p1__11199#))))
     (pj/lay-point :displ :hwy)
     (pj/options {:title drive}))))))


(deftest
 t116_l749
 (is
  ((fn
    [v]
    (let
     [y-domains
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       (:y-domain panel))]
     (and (= 3 (count y-domains)) (= 3 (count (distinct y-domains))))))
   v115_l743)))


(def
 v118_l760
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)))


(deftest
 t119_l764
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 3 (count panels))
      (apply = (map :y-domain panels))
      (apply = (map :x-domain panels)))))
   v118_l760)))


(def
 v121_l776
 (pj/arrange
  (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
  [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]))


(deftest
 t122_l779
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
   v121_l776)))


(deftest
 t124_l793
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
           (fn* [p1__11200#] (= drive (:drv p1__11200#))))
          (pj/lay-point :displ :hwy))))
       {:share-scales #{:y}})]
     (apply
      =
      (for
       [sp (:sub-plots (pj/plan arranged)) panel (:panels (:plan sp))]
       (:y-domain panel)))))
   v121_l776)))


(def
 v126_l821
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :y {:domain [0 50]})
  pj/lay-smooth))


(def
 v127_l827
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  pj/lay-smooth
  (pj/scale :y {:domain [0 50]})
  (pj/facet :drv)))


(deftest
 t128_l833
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
   v127_l827)))


(deftest
 t130_l849
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
   v127_l827)))


(def
 v132_l860
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t133_l864
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v132_l860)))


(def
 v135_l872
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet-grid :smoker :sex)))


(deftest
 t136_l878
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v135_l872)))


(def
 v138_l889
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)))


(deftest
 t139_l893
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v138_l889)))


(def
 v141_l901
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t142_l907
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
   v141_l901)))
