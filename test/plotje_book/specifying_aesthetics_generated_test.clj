(ns
 plotje-book.specifying-aesthetics-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l65
 (def
  plants
  {:height [12 25 18 31],
   :weight [1.4 3.9 2.2 4.6],
   :species ["fern" "moss" "fern" "ivy"],
   :shade ["#CC3311" "#0077BB" "#CC3311" "#009988"]}))


(def v4_l71 plants)


(def
 v6_l120
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :species, :scale true}})))


(deftest
 t7_l123
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v6_l120)))


(def
 v9_l139
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :shade, :scale false}, :size 9})))


(deftest
 t10_l143
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (=
       [(/ 204.0 255) (/ 51.0 255) (/ 17.0 255) 1.0]
       (-> p :panels first :layers first :groups first :color)))))
   v9_l139)))


(def
 v12_l161
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "#0077BB", :scale false}, :size 9})))


(deftest
 t13_l165
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (= 1 (count (-> p :panels first :layers first :groups))))))
   v12_l161)))


(def
 v15_l184
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "Model A", :scale true}, :size 9})))


(deftest
 t16_l188
 (is
  ((fn
    [fr]
    (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v15_l184)))


(def
 v18_l239
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t19_l242
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v18_l239)))


(def
 v21_l263
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t22_l266
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= 9 (:radius (:style (-> p :panels first :layers first))))
      (nil? (:legend p)))))
   v21_l263)))


(def
 v24_l278
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 6.0, :text "target"})))


(deftest
 t25_l282
 (is
  ((fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))
   v24_l278)))


(def
 v27_l310
 (defn
  drawn-colors
  "The colors a `:color` mapping draws on the plants scatter."
  [mapping]
  (->
   plants
   (pj/lay-point :height :weight mapping)
   pj/svg-summary
   :colors
   (disj "none"))))


(def
 v28_l319
 {:species-short (drawn-colors {:color :species}),
  :species-full
  (drawn-colors {:color {:column :species, :scale true}}),
  :shade-short (drawn-colors {:color :shade}),
  :shade-full (drawn-colors {:color {:column :shade, :scale true}}),
  :written-short (drawn-colors {:color "#0077BB"}),
  :written-full
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t29_l326
 (is
  ((fn
    [m]
    (and
     (= (:species-short m) (:species-full m))
     (= (:shade-short m) (:shade-full m))
     (= (:written-short m) (:written-full m))
     (= 3 (count (:species-short m)))
     (= #{"rgb(0,119,187)"} (:written-short m))))
   v28_l319)))


(def
 v31_l343
 {:column-alone (drawn-colors {:color {:column :shade}}),
  :column-scaled (drawn-colors {:color {:column :shade, :scale true}}),
  :value-alone (drawn-colors {:color {:value "#0077BB"}}),
  :value-drawn
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t32_l348
 (is
  ((fn
    [m]
    (and
     (= (:column-alone m) (:column-scaled m))
     (= (:value-alone m) (:value-drawn m))))
   v31_l343)))


(def
 v34_l361
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t35_l364
 (is ((fn [fr] (= 4 (:points (pj/svg-summary fr)))) v34_l361)))


(def
 v36_l367
 (-> plants (pj/lay-point :height :weight {:color {:from :shade}})))


(deftest
 t37_l370
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (->
      plants
      (pj/lay-point :height :weight {:color :shade})
      pj/svg-summary)))
   v36_l367)))


(def
 v39_l386
 (->
  plants
  (pj/pose
   :height
   :weight
   {:size {:column :weight, :scale {:range [3 16]}}})
  (pj/lay-point {:size :weight})
  pj/plan
  :panels
  first
  :layers
  first
  :size-scale))


(deftest t40_l392 (is ((fn [spec] (= {:range [3 16]} spec)) v39_l386)))


(def
 v42_l406
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t43_l412
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v42_l406)))


(def
 v45_l436
 (def
  named-after-a-colour
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v46_l441 named-after-a-colour)


(def
 v48_l445
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t49_l448
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (=
       [[0.0 0.0 1.0 1.0]]
       (->> p :panels first :layers first :groups (mapv :color))))))
   v48_l445)))


(def
 v51_l458
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t52_l461
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v51_l458)))


(def v54_l472 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v55_l476 integer-named)


(def v57_l482 (-> integer-named (pj/lay-point {:x 0, :y 1})))


(deftest
 t58_l485
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v57_l482)))


(def v60_l493 (-> plants (pj/lay-point {:x 0, :y :weight})))


(deftest
 t61_l496
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v60_l493)))


(def v63_l504 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t64_l507
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v63_l504)))


(def v66_l513 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t67_l516
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v66_l513)))


(def
 v69_l526
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale :log}})))


(deftest
 t70_l529
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v69_l526)))


(def
 v72_l537
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale {:range [4 16]}}})))


(deftest
 t73_l541
 (is
  ((fn
    [fr]
    (=
     (->> fr pj/plan :size-legend :entries (mapv :magnitude))
     (->>
      (-> plants (pj/lay-point :height :weight {:size :weight}))
      pj/plan
      :size-legend
      :entries
      (mapv (fn* [p1__80460#] (* 2 (:magnitude p1__80460#)))))))
   v72_l537)))


(def
 v75_l572
 (->
  plants
  (pj/lay-point
   {:x {:column :height, :scale {:type :log}}, :y :weight})))


(deftest
 t76_l575
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v75_l572)))


(def
 v78_l583
 (try
  (->
   plants
   (pj/pose :height :weight)
   (pj/lay-point {:x {:column :height, :scale :log}})
   (pj/lay-line {:x {:column :height, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t79_l592
 (is
  ((fn [m] (re-find #"Layers name different scales for the :x axis" m))
   v78_l583)))


(def
 v81_l604
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t82_l609
 (is ((fn [m] (re-find #":text has no scale to set" m)) v81_l604)))
