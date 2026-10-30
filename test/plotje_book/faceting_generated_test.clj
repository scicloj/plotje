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
       (fn* [p1__80647#] (not= "r" p1__80647#))
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
 v18_l129
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t19_l133
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)))))
   v18_l129)))


(def
 v21_l142
 (->
  (rdatasets/reshape2-tips)
  (pj/lay-point :total-bill :tip {:color :sex})
  (pj/facet {:col :smoker, :row :sex} {:strip-labels :both})))


(deftest
 t22_l146
 (is
  ((fn
    [v]
    (let
     [texts (set (:texts (pj/svg-summary v)))]
     (and (texts "smoker: No") (texts "sex: Female"))))
   v21_l142)))


(def
 v24_l158
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet {:x [:sepal-length :sepal-width :petal-length]})))


(deftest
 t25_l162
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
   v24_l158)))


(def
 v27_l175
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  (pj/lay-smooth {:color "black"})
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t28_l180
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
   v27_l175)))


(def
 v30_l195
 (with-out-str
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-smooth :displ :hwy {:color "black"})
   (pj/facet {:y [:hwy :cty]})
   pj/plan)))


(deftest
 t31_l201
 (is
  ((fn
    [out]
    (and
     (re-find #"pj/lay-smooth names :hwy on :y" out)
     (re-find #"\{:y :cty\} has no layer" out)
     (re-find #"leave :y off the layer and name :x on the pose" out)))
   v30_l195)))


(def
 v33_l228
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:x :sepal-length})
  pj/lay-point
  (pj/facet {:col :species, :row {:y [:sepal-width :petal-width]}})))


(deftest
 t34_l233
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
   v33_l228)))


(def
 v36_l268
 (->
  (rdatasets/datasets-iris)
  (pj/pose {:color :species})
  (pj/facet
   {:col {:x [:sepal-length :sepal-width :petal-length]},
    :row {:y [:sepal-length :sepal-width :petal-length]}})
  pj/lay-point))


(deftest
 t37_l274
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
           (fn* [p1__80648#] (first (:panels (:plan p1__80648#))))))
         (:sub-plots (pj/plan v)))]
       (every?
        (fn
         [r]
         (=
          (:y-domain (panels [r r]))
          (:y-domain (panels [r (mod (inc r) 3)]))))
        (range 3))))))
   v36_l268)))


(def
 v39_l303
 (kind/table
  {:column-names [:aesthetic :what-it-reads],
   :row-vectors
   (mapv
    (fn [[k doc]] [k doc])
    (sort-by (comp str key) pj/panel-aesthetic-docs))}))


(deftest
 t40_l308
 (is
  ((fn [_] (= #{:col :row} (set (keys pj/panel-aesthetic-docs))))
   v39_l303)))


(def
 v42_l314
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)))


(def
 v43_l318
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  kind/pprint))


(deftest
 t44_l323
 (is ((fn [fr] (= :species (get-in fr [:mapping :col]))) v43_l318)))


(def
 v46_l329
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet {:col :drv, :row :cyl})
  kind/pprint))


(deftest
 t47_l334
 (is
  ((fn
    [fr]
    (and
     (= :drv (get-in fr [:mapping :col]))
     (= :cyl (get-in fr [:mapping :row]))))
   v46_l329)))


(def
 v49_l341
 (->
  (rdatasets/datasets-iris)
  (pj/pose
   {:x :sepal-length, :y :sepal-width, :col {:column :species}})
  (pj/lay-point)))


(deftest
 t50_l345
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
   v49_l341)))


(def
 v52_l362
 (->
  (pj/arrange
   [[(pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/facet :species)
  (pj/options {:width 700, :height 560})))


(deftest
 t53_l368
 (is ((fn [fr] (= 6 (:panels (pj/svg-summary fr)))) v52_l362)))


(deftest
 t55_l380
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
   v52_l362)))


(def
 v57_l395
 (->
  (pj/arrange
   [[(->
      (pj/pose (rdatasets/datasets-iris) :sepal-length :sepal-width)
      (pj/facet :species))]
    [(pj/pose (rdatasets/datasets-iris) :petal-length :petal-width)]])
  (pj/lay-point)
  (pj/options {:width 700, :height 560})))


(deftest
 t58_l401
 (is
  ((fn
    [fr]
    (and
     (= 4 (:panels (pj/svg-summary fr)))
     (=
      [3 1]
      (mapv
       (fn* [p1__80649#] (count (:panels (:plan p1__80649#))))
       (:sub-plots (pj/plan fr))))))
   v57_l395)))


(def
 v60_l414
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/lay-point :petal-length :petal-width)
  (pj/facet :species)
  (pj/options {:width 900, :height 480})))


(deftest
 t61_l420
 (is ((fn [fr] (= 3 (:panels (pj/svg-summary fr)))) v60_l414)))


(def
 v63_l429
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
 t64_l437
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
   v63_l429)))


(def
 v66_l463
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width)
  (pj/facet :species)
  (pj/marginal :top)
  (pj/options {:width 900, :height 480})))


(deftest
 t67_l469
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
   v66_l463)))


(def v69_l487 (pj/compound-key-aesthetics))


(deftest t70_l489 (is ((fn [s] (= #{:group :col :row} s)) v69_l487)))


(def
 v72_l494
 (def
  measures
  (tc/dataset
   {:part ["sepal" "sepal" "sepal" "sepal" "petal" "petal"],
    :dimension ["length" "length" "width" "width" "length" "length"],
    :t [1 2 1 2 1 2],
    :v [1.0 2.0 1.5 2.5 2.0 3.0]})))


(def v73_l500 measures)


(def
 v75_l505
 (try
  (-> measures (pj/lay-point :t :v {:color [:part :dimension]}))
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t76_l511
 (is
  ((fn [msg] (re-find #":color was given several columns" msg))
   v75_l505)))


(def
 v78_l517
 (-> measures (pj/lay-point :t :v) (pj/facet [:part :dimension])))


(deftest
 t79_l521
 (is
  ((fn
    [fr]
    (and
     (= 3 (:panels (pj/svg-summary fr)))
     (=
      ["sepal / length" "sepal / width" "petal / length"]
      (mapv :col-label (:panels (pj/plan fr))))))
   v78_l517)))


(def
 v81_l530
 (->
  measures
  (pj/lay-point :t :v)
  (pj/facet {:col :part, :row :dimension})))


(deftest
 t82_l534
 (is ((fn [fr] (= 4 (:panels (pj/svg-summary fr)))) v81_l530)))


(def
 v84_l556
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:y :x}})))


(deftest
 t85_l561
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
   v84_l556)))


(def
 v87_l576
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options {:share-scales #{:x}})))


(deftest
 t88_l581
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
   v87_l576)))


(def
 v90_l608
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species {:share-scales #{:x}})))


(deftest
 t91_l612
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
   v90_l608)))


(def
 v93_l623
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
 t94_l629
 (is
  ((fn [{:keys [not-split each-panel]}] (= [not-split] each-panel))
   v93_l623)))


(def
 v96_l654
 (def
  per-panel
  {:g ["L" "L" "L" "R" "R" "R"],
   :x [1 2 3 1 2 3],
   :y [1 1 1 1 1 1],
   :n [1 2 3 4 7 10]}))


(def
 v97_l660
 (-> per-panel (pj/lay-point :x :y {:size :n}) (pj/facet :g)))


(deftest
 t98_l664
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
   v97_l660)))


(def
 v100_l685
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy {:color :class})
  (pj/facet :drv)))


(deftest
 t101_l689
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
             (fn* [p1__80650#] (= drv (:drv p1__80650#))))))
          (distinct (:drv mpg)))))))))
   v100_l685)))


(def
 v103_l705
 (->
  per-panel
  (pj/lay-point :x :y {:size :n})
  (pj/facet :g)
  (pj/scale :size {:domain [0 20]})))


(deftest
 t104_l710
 (is
  ((fn
    [v]
    (let
     [radii (sort (:sizes (pj/svg-summary v)))]
     (and
      (= 6 (count radii))
      (> (first radii) 2.0)
      (< (last radii) 8.0))))
   v103_l705)))


(def
 v106_l727
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:col :species})
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t107_l733
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"panel aesthetic" m)
     (re-find #"read from a pose's mapping" m)
     (re-find #"pj/facet pose" m)
     (re-find #"in the pose's mapping" m)))
   v106_l727)))


(def
 v109_l749
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet :species)
   (pj/facet :sepal-length))
  (catch Exception e (ex-message e))))


(deftest
 t110_l756
 (is
  ((fn [m] (and (string? m) (re-find #"already facets by" m)))
   v109_l749)))


(def
 v112_l764
 (try
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width)
   (pj/facet {:col :species, :share-scales #{:x}}))
  (catch Exception e (ex-message e))))


(deftest
 t113_l770
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"unexpected key\(s\): \[:share-scales\]" m)
     (re-find #"in a map of its own after this one" m)))
   v112_l764)))


(def
 v115_l782
 (try
  (->
   (rdatasets/ggplot2-mpg)
   (pj/lay-point :displ :hwy)
   (pj/facet :model)
   pj/plan)
  (catch Exception e (ex-message e))))


(deftest
 t116_l789
 (is
  ((fn
    [m]
    (and
     (string? m)
     (re-find #"a facet by :model" m)
     (re-find #"does not wrap its panels" m)
     (re-find #"pj/arrange poses" m)))
   v115_l782)))


(def
 v118_l810
 (->
  (rdatasets/ggplot2-mpg)
  (pj/pose {:x :displ})
  pj/lay-point
  (pj/facet {:y [:hwy :cty]})))


(deftest
 t119_l815
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
   v118_l810)))


(def
 v121_l831
 (->
  (rdatasets/ggplot2-mpg)
  (pj/arrange [{:x :displ, :y :hwy} {:x :displ, :y :cty}] {:cols 1})
  pj/lay-point))


(deftest
 t122_l835
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
   v121_l831)))


(def
 v124_l861
 (pj/arrange
  (vec
   (for
    [drive ["4" "f" "r"]]
    (->
     (rdatasets/ggplot2-mpg)
     (tc/select-rows (fn* [p1__80651#] (= drive (:drv p1__80651#))))
     (pj/lay-point :displ :hwy)
     (pj/options {:title drive}))))))


(deftest
 t125_l867
 (is
  ((fn
    [v]
    (let
     [y-domains
      (for
       [sp (:sub-plots (pj/plan v)) panel (:panels (:plan sp))]
       (:y-domain panel))]
     (and (= 3 (count y-domains)) (= 3 (count (distinct y-domains))))))
   v124_l861)))


(def
 v127_l878
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)))


(deftest
 t128_l882
 (is
  ((fn
    [v]
    (let
     [panels (:panels (pj/plan v))]
     (and
      (= 3 (count panels))
      (apply = (map :y-domain panels))
      (apply = (map :x-domain panels)))))
   v127_l878)))


(def
 v130_l894
 (pj/arrange
  (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
  [{:where {:drv "f"}} {:where {:drv "4"}} {:where {:drv "r"}}]))


(deftest
 t131_l897
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
   v130_l894)))


(deftest
 t133_l911
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
           (fn* [p1__80652#] (= drive (:drv p1__80652#))))
          (pj/lay-point :displ :hwy))))
       {:share-scales #{:y}})]
     (apply
      =
      (for
       [sp (:sub-plots (pj/plan arranged)) panel (:panels (:plan sp))]
       (:y-domain panel)))))
   v130_l894)))


(def
 v135_l939
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  (pj/facet :drv)
  (pj/scale :y {:domain [0 50]})
  pj/lay-smooth))


(def
 v136_l945
 (->
  (rdatasets/ggplot2-mpg)
  (pj/lay-point :displ :hwy)
  pj/lay-smooth
  (pj/scale :y {:domain [0 50]})
  (pj/facet :drv)))


(deftest
 t137_l951
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
   v136_l945)))


(deftest
 t139_l967
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
   v136_l945)))


(def
 v141_l999
 (let
  [mpg (rdatasets/ggplot2-mpg)]
  (->
   (tc/concat mpg (tc/add-column mpg :drv "(all)"))
   (pj/lay-point :displ :hwy)
   (pj/facet :drv))))


(deftest
 t142_l1004
 (is
  ((fn
    [v]
    (let
     [mpg (rdatasets/ggplot2-mpg) s (pj/svg-summary v)]
     (and
      (= (inc (count (distinct (mpg :drv)))) (:panels s))
      (= (* 2 (tc/row-count mpg)) (:points s))
      (some #{"(all)"} (:texts s)))))
   v141_l999)))


(def
 v144_l1020
 (->
  (rdatasets/datasets-iris)
  (pj/lay-histogram :sepal-length {:color :species})
  (pj/facet :species)))


(deftest
 t145_l1024
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (pos? (:polygons s)))))
   v144_l1020)))


(def
 v147_l1032
 (->
  (rdatasets/reshape2-tips)
  (pj/pose :total-bill :tip {:color :sex})
  pj/lay-point
  (pj/lay-smooth {:stat :linear-model})
  (pj/facet {:col :smoker, :row :sex})))


(deftest
 t148_l1038
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 4 (:panels s)) (= 244 (:points s)) (= 4 (:lines s)))))
   v147_l1032)))


(def
 v150_l1049
 (->
  (rdatasets/palmerpenguins-penguins)
  (pj/lay-bar :species {:color :species})
  (pj/facet :island)
  (pj/options {:width 800})))


(deftest
 t151_l1054
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 3 (:panels s)) (= 5 (:polygons s)))))
   v150_l1049)))


(def
 v153_l1062
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})
  (pj/facet :species)
  (pj/options
   {:title "Iris by Species",
    :x-label "Sepal Length (cm)",
    :y-label "Sepal Width (cm)"})))


(deftest
 t154_l1068
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
   v153_l1062)))
