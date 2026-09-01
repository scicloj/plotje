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
 v6_l124
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :species, :scale true}})))


(deftest
 t7_l127
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v6_l124)))


(def
 v9_l143
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :shade, :scale false}, :size 9})))


(deftest
 t10_l147
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
   v9_l143)))


(def
 v12_l165
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "#0077BB", :scale false}, :size 9})))


(deftest
 t13_l169
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (= 1 (count (-> p :panels first :layers first :groups))))))
   v12_l165)))


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
 v18_l202
 (->
  plants
  (pj/pose :height :weight)
  (pj/lay-point {:color {:column :species, :scale true}, :size 9})
  (pj/lay-line {:color {:value "ivy", :scale true}})))


(deftest
 t19_l207
 (is
  ((fn
    [fr]
    (let
     [p
      (pj/plan fr)
      ivy
      (->>
       p
       :legend
       :entries
       (filter (fn [e] (= "ivy" (:label e))))
       first
       :color)]
     (and
      (= ["fern" "moss" "ivy"] (mapv :label (:entries (:legend p))))
      (=
       ivy
       (-> p :panels first :layers second :groups first :color)))))
   v18_l202)))


(def
 v21_l221
 (->
  plants
  (pj/pose :height :weight)
  (pj/lay-point {:color {:column :species, :scale true}, :size 9})
  (pj/lay-line {:color {:value "predicted", :scale true}})))


(deftest
 t22_l226
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy" "predicted"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v21_l221)))


(def
 v24_l251
 (-> plants (pj/lay-point :height :weight {:color :weight})))


(deftest
 t25_l254
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v24_l251)))


(def
 v27_l292
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t28_l295
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v27_l292)))


(def
 v30_l306
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t31_l309
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= 9 (:radius (:style (-> p :panels first :layers first))))
      (nil? (:legend p)))))
   v30_l306)))


(def
 v33_l321
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 6.0, :text "target"})))


(deftest
 t34_l325
 (is
  ((fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))
   v33_l321)))


(def
 v36_l353
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
 v37_l362
 {:species-short (drawn-colors {:color :species}),
  :species-full
  (drawn-colors {:color {:column :species, :scale true}}),
  :shade-short (drawn-colors {:color :shade}),
  :shade-full (drawn-colors {:color {:column :shade, :scale true}}),
  :written-short (drawn-colors {:color "#0077BB"}),
  :written-full
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t38_l369
 (is
  ((fn
    [m]
    (and
     (= (:species-short m) (:species-full m))
     (= (:shade-short m) (:shade-full m))
     (= (:written-short m) (:written-full m))
     (= 3 (count (:species-short m)))
     (= #{"rgb(0,119,187)"} (:written-short m))))
   v37_l362)))


(def
 v40_l386
 {:column-alone (drawn-colors {:color {:column :shade}}),
  :column-scaled (drawn-colors {:color {:column :shade, :scale true}}),
  :value-alone (drawn-colors {:color {:value "#0077BB"}}),
  :value-drawn
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t41_l391
 (is
  ((fn
    [m]
    (and
     (= (:column-alone m) (:column-scaled m))
     (= (:value-alone m) (:value-drawn m))))
   v40_l386)))


(def
 v43_l405
 (-> plants (pj/lay-point :height :weight {:color {:from :shade}})))


(deftest
 t44_l408
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (->
      plants
      (pj/lay-point :height :weight {:color :shade})
      pj/svg-summary)))
   v43_l405)))


(def
 v46_l419
 (->
  plants
  (pj/lay-point :height :weight {:color {:from :shade, :scale false}})))


(deftest
 t47_l422
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
   v46_l419)))


(def
 v49_l435
 (->
  plants
  (pj/lay-point :height :weight {:color {:from :red}, :size 9})))


(deftest
 t50_l438
 (is
  ((fn
    [fr]
    (and
     (=
      [[1.0 0.0 0.0 1.0]]
      (->>
       fr
       pj/plan
       :panels
       first
       :layers
       first
       :groups
       (mapv :color)))
     (re-find
      #"Column :red \(from :color\) not found"
      (try
       (->
        plants
        (pj/lay-point :height :weight {:color {:column :red}})
        pj/plan)
       "no error"
       (catch clojure.lang.ExceptionInfo e (ex-message e))))))
   v49_l435)))


(def
 v52_l466
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t53_l472
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v52_l466)))


(def
 v55_l496
 (def
  named-after-a-color
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v56_l501 named-after-a-color)


(def
 v58_l505
 (->
  named-after-a-color
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t59_l508
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
   v58_l505)))


(def
 v61_l518
 (->
  named-after-a-color
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t62_l521
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v61_l518)))


(def v64_l532 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v65_l536 integer-named)


(def v67_l542 (-> integer-named (pj/lay-point {:x 0, :y 1})))


(deftest
 t68_l545
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v67_l542)))


(def v70_l553 (-> plants (pj/lay-point {:x 0, :y :weight})))


(deftest
 t71_l556
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v70_l553)))


(def v73_l564 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t74_l567
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v73_l564)))


(def v76_l573 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t77_l576
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v76_l573)))


(def
 v79_l587
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale :log}})))


(deftest
 t80_l590
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v79_l587)))


(def
 v82_l598
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale {:range [4 16]}}})))


(deftest
 t83_l602
 (is
  ((fn
    [fr]
    (let
     [refusal
      (fn
       [build]
       (try
        (pj/plan (build))
        "no error"
        (catch clojure.lang.ExceptionInfo e (ex-message e))))]
     (and
      (=
       (->> fr pj/plan :size-legend :entries (mapv :magnitude))
       (->>
        (-> plants (pj/lay-point :height :weight {:size :weight}))
        pj/plan
        :size-legend
        :entries
        (mapv (fn* [p1__84529#] (* 2 (:magnitude p1__84529#))))))
      (every?
       (fn*
        [p1__84530#]
        (re-find #":values does not recognize" (refusal p1__84530#)))
       [(fn*
         []
         (->
          plants
          (pj/lay-point
           :height
           :weight
           {:shape {:column :species, :scale {:values [:blob]}}})))
        (fn*
         []
         (->
          plants
          (pj/lay-point :height :weight {:shape :species})
          (pj/scale :shape {:values [:blob]})))]))))
   v82_l598)))


(def
 v85_l654
 (->
  plants
  (pj/lay-point
   {:x {:column :height, :scale {:type :log}}, :y :weight})))


(deftest
 t86_l657
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v85_l654)))


(def
 v88_l665
 (try
  (->
   plants
   (pj/pose :height :weight)
   (pj/lay-point {:x {:column :height, :scale :log}})
   (pj/lay-line {:x {:column :height, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t89_l674
 (is
  ((fn [m] (re-find #"Layers name different scales for the :x axis" m))
   v88_l665)))


(def
 v91_l686
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t92_l691
 (is ((fn [m] (re-find #":text has no scale to set" m)) v91_l686)))
