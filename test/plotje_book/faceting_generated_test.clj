(ns
 plotje-book.faceting-generated-test
 (:require
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l31
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)))


(deftest
 t4_l35
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
   v3_l31)))


(def
 v6_l62
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet {:row :species})))


(deftest
 t7_l66
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 150 (:points s)))))
   v6_l62)))


(def
 v9_l72
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet-grid :smoker :sex)))


(deftest
 t10_l76
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v9_l72)))


(def
 v12_l90
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t13_l94
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
   v12_l90)))


(def
 v15_l107
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  (pj/lay-smooth {:color "black"})
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t16_l112
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
   v15_l107)))


(def
 v18_l127
 (with-out-str
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-smooth :displ :hwy {:color "black"})
   (pj/facet {:y [:hwy :cty]})
   pj/plan)))


(deftest
 t19_l133
 (is
  ((fn
    [out]
    (and
     (re-find #"pj/lay-smooth names :hwy on :y" out)
     (re-find #"\{:y :cty\} has no layer" out)
     (re-find #"leave :y off the layer and name :x on the pose" out)))
   v18_l127)))


(def
 v21_l155
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t22_l160
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v21_l155)))


(def
 v24_l166
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v25_l170
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t26_l175
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v25_l170)))


(def
 v28_l181
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet-grid :drv :cyl)
  kind/pprint))


(deftest
 t29_l186
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v28_l181)))


(def
 v31_l193
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t32_l197
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
   v31_l193)))


(def
 v34_l214
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t35_l220
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v34_l214)))


(def
 v37_l233
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t38_l239
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__11193#] (count (:panels (:plan p1__11193#))))
       (:sub-plots (pj/plan fr))))))
   v37_l233)))


(def
 v40_l252
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t41_l258
 (is ((fn [fr] (= 3 (:panels (pj/svg-summary fr)))) v40_l252)))


(def
 v43_l265
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
 t44_l273
 (is
  ((fn
    [fr]
    (=
     {:panels 6, :points 300}
     (select-keys (pj/svg-summary fr) [:panels :points])))
   v43_l265)))


(def
 v46_l285
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t47_l291
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
   v46_l285)))


(def v49_l309 (pj/compound-key-aesthetics))


(deftest t50_l311 (is ((fn [s] (= #{:group :col :row} s)) v49_l309)))


(def
 v52_l316
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v53_l322 measures)


(def
 v55_l327
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t56_l333
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v55_l327)))


(def
 v58_l339
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t59_l343
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v58_l339)))


(def
 v61_l352
 (-> measures (pj/lay-point :t :v) (pj/facet-grid :part :dimension)))


(deftest
 t62_l356
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v61_l352)))


(def
 v64_l368
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t65_l373
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
   v64_l368)))


(def
 v67_l388
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:x}})))


(deftest
 t68_l393
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
   v67_l388)))


(def
 v70_l418
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species {:share-scales #{:x}})))


(deftest
 t71_l422
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
   v70_l418)))


(def
 v73_l433
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
 t74_l439
 (is
  ((fn [{:keys [not-split each-panel]}] (= [not-split] each-panel))
   v73_l433)))


(def
 v76_l464
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v77_l470
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t78_l474
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
   v77_l470)))


(def
 v80_l495
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :class})
  (pj/facet :drv)))


(deftest
 t81_l499
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
             (fn* [p1__11194#] (= drv (:drv p1__11194#))))))
          (distinct (:drv mpg)))))))))
   v80_l495)))


(def
 v83_l515
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t84_l520
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v83_l515)))


(def
 v86_l537
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t87_l543
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
   v86_l537)))


(def
 v89_l560
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t90_l567
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v89_l560)))


(def
 v92_l575
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet {:col :species, :share-scales #{:x}}))
  (catch Exception e (ex-message e))))


(deftest
 t93_l581
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"unexpected key\(s\): \[:share-scales\]" m)
     (re-find #"in a map of its own after this one" m)))
   v92_l575)))


(def
 v95_l592
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/facet :model)
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t96_l599
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"a facet by :model" m)
     (re-find #"does not wrap its panels" m)
     (re-find #"pj/arrange poses" m)))
   v95_l592)))


(def
 v98_l619
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t99_l624
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
   v98_l619)))


(def
 v101_l639
 (pj/arrange
  [(pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
   (pj/lay-point (rdatasets/ggplot2-mpg) :displ :cty)]
  {:cols 1}))


(deftest
 t102_l643
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
      (= 2 (count (filter #{"displ"} (:texts s)))))))
   v101_l639)))


(def
 v104_l669
 (pj/arrange
  (vec
   (for
    [drive ["4" "f" "r"]]
    (->
     (rdatasets/ggplot2-mpg)
     (tc/select-rows (fn* [p1__11195#] (= drive (:drv p1__11195#))))
     (pj/lay-point :displ :hwy)
     (pj/options {:title drive}))))))


(deftest
 t105_l675
 (is
  ((fn
    [v]
    (let
     [y-domains
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       (:y-domain panel))]
     (and (= 3 (count y-domains)) (= 3 (count (distinct y-domains))))))
   v104_l669)))


(def
 v107_l686
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)))


(deftest
 t108_l690
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 3 (count panels))
      (apply = (map :y-domain panels))
      (apply = (map :x-domain panels)))))
   v107_l686)))


(deftest
 t110_l701
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
           (fn* [p1__11196#] (= drive (:drv p1__11196#))))
          (pj/lay-point :displ :hwy))))
       {:share-scales #{:y}})]
     (apply
      =
      (for
       [sp (:sub-plots (pj/plan arranged)) panel (:panels (:plan sp))]
       (:y-domain panel)))))
   v107_l686)))


(def
 v112_l727
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t113_l731
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v112_l727)))


(def
 v115_l739
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet-grid :smoker :sex)))


(deftest
 t116_l745
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v115_l739)))


(def
 v118_l756
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)))


(deftest
 t119_l760
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v118_l756)))


(def
 v121_l768
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t122_l774
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
   v121_l768)))
