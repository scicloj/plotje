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
   :shade ["#EE7733" "#AA3377" "#EE7733" "#000000"]}))


(def v4_l74 plants)


(def
 v6_l123
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :species, :scale true}})))


(deftest
 t7_l126
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v6_l123)))


(def
 v9_l142
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :shade, :scale false}, :size 9})))


(deftest
 t10_l146
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (=
       [(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
       (-> p :panels first :layers first :groups first :color)))))
   v9_l142)))


(def
 v12_l164
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "#0077BB", :scale false}, :size 9})))


(deftest
 t13_l168
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (= 1 (count (-> p :panels first :layers first :groups))))))
   v12_l164)))


(def
 v15_l187
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "Model A", :scale true}, :size 9})))


(deftest
 t16_l191
 (is
  ((fn
    [fr]
    (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v15_l187)))


(def
 v18_l242
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t19_l245
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v18_l242)))


(def
 v21_l266
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t22_l269
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= 9 (:radius (:style (-> p :panels first :layers first))))
      (nil? (:legend p)))))
   v21_l266)))


(def
 v24_l281
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 6.0, :text "target"})))


(deftest
 t25_l285
 (is
  ((fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))
   v24_l281)))


(def
 v27_l313
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
 v28_l322
 {:species-short (drawn-colors {:color :species}),
  :species-full
  (drawn-colors {:color {:column :species, :scale true}}),
  :shade-short (drawn-colors {:color :shade}),
  :shade-full (drawn-colors {:color {:column :shade, :scale true}}),
  :written-short (drawn-colors {:color "#0077BB"}),
  :written-full
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t29_l329
 (is
  ((fn
    [m]
    (and
     (= (:species-short m) (:species-full m))
     (= (:shade-short m) (:shade-full m))
     (= (:written-short m) (:written-full m))
     (= 3 (count (:species-short m)))
     (= #{"rgb(0,119,187)"} (:written-short m))))
   v28_l322)))


(def
 v31_l346
 {:column-alone (drawn-colors {:color {:column :shade}}),
  :column-scaled (drawn-colors {:color {:column :shade, :scale true}}),
  :value-alone (drawn-colors {:color {:value "#0077BB"}}),
  :value-drawn
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t32_l351
 (is
  ((fn
    [m]
    (and
     (= (:column-alone m) (:column-scaled m))
     (= (:value-alone m) (:value-drawn m))))
   v31_l346)))


(def
 v34_l365
 (-> plants (pj/lay-point :height :weight {:color {:from :shade}})))


(deftest
 t35_l368
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (->
      plants
      (pj/lay-point :height :weight {:color :shade})
      pj/svg-summary)))
   v34_l365)))


(def
 v37_l379
 (->
  plants
  (pj/lay-point :height :weight {:color {:from :shade, :scale false}})))


(deftest
 t38_l382
 (is
  ((fn
    [fr]
    (=
     [[(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
      [(/ 170.0 255) (/ 51.0 255) (/ 119.0 255) 1.0]
      [0.0 0.0 0.0 1.0]]
     (->>
      fr
      pj/plan
      :panels
      first
      :layers
      first
      :groups
      (mapv :color))))
   v37_l379)))


(def
 v40_l399
 (->
  plants
  (pj/pose :height :weight {:color :weight})
  (pj/scale :color :log)
  :mapping))


(deftest
 t41_l404
 (is
  ((fn
    [m]
    (=
     {:x :height,
      :y :weight,
      :color {:from :weight, :scale {:type :log}}}
     m))
   v40_l399)))


(def
 v43_l425
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t44_l431
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v43_l425)))


(def
 v46_l455
 (def
  named-after-a-colour
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v47_l460 named-after-a-colour)


(def
 v49_l464
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t50_l467
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
   v49_l464)))


(def
 v52_l477
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t53_l480
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v52_l477)))


(def v55_l491 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v56_l495 integer-named)


(def v58_l501 (-> integer-named (pj/lay-point {:x 0, :y 1})))


(deftest
 t59_l504
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v58_l501)))


(def v61_l512 (-> plants (pj/lay-point {:x 0, :y :weight})))


(deftest
 t62_l515
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v61_l512)))


(def v64_l523 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t65_l526
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v64_l523)))


(def v67_l532 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t68_l535
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v67_l532)))


(def
 v70_l546
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale :log}})))


(deftest
 t71_l549
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v70_l546)))


(def
 v73_l556
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale {:range [4 16]}}})))


(deftest
 t74_l560
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
      (mapv (fn* [p1__72362#] (* 2 (:magnitude p1__72362#)))))))
   v73_l556)))


(def
 v76_l592
 (->
  plants
  (pj/lay-point
   {:x {:column :height, :scale {:type :log}}, :y :weight})))


(deftest
 t77_l595
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v76_l592)))


(def
 v79_l603
 (try
  (->
   plants
   (pj/pose :height :weight)
   (pj/lay-point {:x {:column :height, :scale :log}})
   (pj/lay-line {:x {:column :height, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t80_l612
 (is
  ((fn [m] (re-find #"Layers name different scales for the :x axis" m))
   v79_l603)))


(def
 v82_l624
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t83_l629
 (is ((fn [m] (re-find #":text has no scale to set" m)) v82_l624)))
