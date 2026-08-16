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
 v34_l362
 (-> plants (pj/lay-point :height :weight {:color {:from :shade}})))


(deftest
 t35_l365
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (->
      plants
      (pj/lay-point :height :weight {:color :shade})
      pj/svg-summary)))
   v34_l362)))


(def
 v37_l376
 (->
  plants
  (pj/lay-point :height :weight {:color {:from :shade, :scale false}})))


(deftest
 t38_l379
 (is
  ((fn
    [fr]
    (=
     [[0.8 0.2 (/ 17.0 255) 1.0]
      [0.0 (/ 119.0 255) (/ 187.0 255) 1.0]
      [0.0 0.6 (/ 136.0 255) 1.0]]
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      first
      :groups
      (mapv :color))))
   v37_l376)))


(def
 v40_l396
 (->
  plants
  (pj/pose :height :weight {:color :weight})
  (pj/scale :color :log)
  :mapping))


(deftest
 t41_l401
 (is
  ((fn
    [m]
    (=
     {:x :height,
      :y :weight,
      :color {:from :weight, :scale {:type :log}}}
     m))
   v40_l396)))


(def
 v43_l422
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t44_l428
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v43_l422)))


(def
 v46_l452
 (def
  named-after-a-colour
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v47_l457 named-after-a-colour)


(def
 v49_l461
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t50_l464
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
   v49_l461)))


(def
 v52_l474
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t53_l477
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v52_l474)))


(def v55_l488 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v56_l492 integer-named)


(def v58_l498 (-> integer-named (pj/lay-point {:x 0, :y 1})))


(deftest
 t59_l501
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v58_l498)))


(def v61_l509 (-> plants (pj/lay-point {:x 0, :y :weight})))


(deftest
 t62_l512
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v61_l509)))


(def v64_l520 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t65_l523
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v64_l520)))


(def v67_l529 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t68_l532
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v67_l529)))


(def
 v70_l542
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale :log}})))


(deftest
 t71_l545
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v70_l542)))


(def
 v73_l553
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale {:range [4 16]}}})))


(deftest
 t74_l557
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
      (mapv (fn* [p1__80812#] (* 2 (:magnitude p1__80812#)))))))
   v73_l553)))


(def
 v76_l588
 (->
  plants
  (pj/lay-point
   {:x {:column :height, :scale {:type :log}}, :y :weight})))


(deftest
 t77_l591
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v76_l588)))


(def
 v79_l599
 (try
  (->
   plants
   (pj/pose :height :weight)
   (pj/lay-point {:x {:column :height, :scale :log}})
   (pj/lay-line {:x {:column :height, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t80_l608
 (is
  ((fn [m] (re-find #"Layers name different scales for the :x axis" m))
   v79_l599)))


(def
 v82_l620
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t83_l625
 (is ((fn [m] (re-find #":text has no scale to set" m)) v82_l620)))
