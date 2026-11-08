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
      (= 1 (count (-> p :panels first :layers first :groups)))
      (=
       [(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
       (->
        p
        :panels
        first
        :layers
        first
        :groups
        first
        :colors
        first)))))
   v9_l143)))


(def
 v12_l184
 (->
  {:cohort ["a" "b" "c"],
   :growth [10 20 30],
   :shade ["#EE7733" "#AA3377" "#000000"]}
  (pj/lay-bar :cohort :growth {:color {:column :shade, :scale false}})))


(deftest
 t13_l188
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 1 (count (:groups layer)))
      (= 3 (count (:colors (first (:groups layer))))))))
   v12_l184)))


(def
 v15_l208
 (try
  (->
   {:day [1 2 3],
    :visits [4 5 6],
    :shade ["#EE7733" "#AA3377" "#000000"]}
   (pj/lay-line :day :visits {:color {:column :shade, :scale false}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t16_l215
 (is
  ((fn
    [m]
    (and
     (re-find #"A :scale false on :color" m)
     (re-find
      #"For one color over the layer, write the value itself"
      m)
     (re-find #"drop the :scale" m)))
   v15_l208)))


(def
 v18_l232
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "#0077BB", :scale false}, :size 9})))


(deftest
 t19_l236
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (= 1 (count (-> p :panels first :layers first :groups))))))
   v18_l232)))


(def
 v21_l259
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "Model A", :scale true}, :size 9})))


(deftest
 t22_l263
 (is
  ((fn
    [fr]
    (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v21_l259)))


(def
 v24_l274
 (->
  plants
  (pj/pose :height :weight)
  (pj/lay-point {:color {:column :species, :scale true}, :size 9})
  (pj/lay-line {:color {:value "ivy", :scale true}})))


(deftest
 t25_l279
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
   v24_l274)))


(def
 v27_l303
 (->
  plants
  (pj/pose :height :weight)
  (pj/lay-point {:color {:column :species, :scale true}, :size 9})
  (pj/lay-line {:color {:value "predicted", :scale true}})))


(deftest
 t28_l308
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy" "predicted"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v27_l303)))


(def
 v30_l334
 (-> plants (pj/lay-point :height :weight {:color :weight})))


(deftest
 t31_l337
 (is
  ((fn [fr] (= :continuous (-> fr pj/plan :legend :type))) v30_l334)))


(def
 v33_l380
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t34_l383
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v33_l380)))


(def
 v36_l394
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t37_l397
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= 9 (:radius (:style (-> p :panels first :layers first))))
      (nil? (:legend p)))))
   v36_l394)))


(def
 v39_l414
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 6.0, :text "target"})))


(deftest
 t40_l418
 (is
  ((fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))
   v39_l414)))


(def
 v42_l451
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
 v43_l460
 {:species-short (drawn-colors {:color :species}),
  :species-full
  (drawn-colors {:color {:column :species, :scale true}}),
  :shade-short (drawn-colors {:color :shade}),
  :shade-full (drawn-colors {:color {:column :shade, :scale true}}),
  :written-short (drawn-colors {:color "#0077BB"}),
  :written-full
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t44_l467
 (is
  ((fn
    [m]
    (and
     (= (:species-short m) (:species-full m))
     (= (:shade-short m) (:shade-full m))
     (= (:written-short m) (:written-full m))
     (= 3 (count (:species-short m)))
     (= #{"rgb(0,119,187)"} (:written-short m))))
   v43_l460)))


(def
 v46_l484
 {:column-alone (drawn-colors {:color {:column :shade}}),
  :column-scaled (drawn-colors {:color {:column :shade, :scale true}}),
  :value-alone (drawn-colors {:color {:value "#0077BB"}}),
  :value-drawn
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t47_l489
 (is
  ((fn
    [m]
    (and
     (= (:column-alone m) (:column-scaled m))
     (= (:value-alone m) (:value-drawn m))))
   v46_l484)))


(def
 v49_l503
 (-> plants (pj/lay-point :height :weight {:color {:from :shade}})))


(deftest
 t50_l506
 (is
  ((fn
    [fr]
    (=
     (pj/svg-summary fr)
     (->
      plants
      (pj/lay-point :height :weight {:color :shade})
      pj/svg-summary)))
   v49_l503)))


(def
 v52_l517
 (->
  plants
  (pj/lay-point :height :weight {:color {:from :shade, :scale false}})))


(deftest
 t53_l520
 (is
  ((fn
    [fr]
    (=
     [[(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
      [(/ 170.0 255) (/ 51.0 255) (/ 119.0 255) 1.0]
      [(/ 238.0 255) (/ 119.0 255) (/ 51.0 255) 1.0]
      [0.0 0.0 0.0 1.0]]
     (->
      fr
      pj/plan
      :panels
      first
      :layers
      first
      :groups
      first
      :colors)))
   v52_l517)))


(def
 v55_l545
 (->
  plants
  (pj/lay-point :height :weight {:color {:from :red}, :size 9})))


(deftest
 t56_l548
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
   v55_l545)))


(def
 v58_l583
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t59_l589
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v58_l583)))


(def
 v61_l617
 (def
  named-after-a-color
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v62_l622 named-after-a-color)


(def
 v64_l626
 (->
  named-after-a-color
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t65_l629
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
   v64_l626)))


(def
 v67_l645
 (->
  named-after-a-color
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t68_l648
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v67_l645)))


(def v70_l659 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v71_l663 integer-named)


(def v73_l669 (-> integer-named (pj/lay-point {:x 0, :y 1})))


(deftest
 t74_l672
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v73_l669)))


(def v76_l684 (-> plants (pj/lay-point {:x 0, :y :weight})))


(deftest
 t77_l687
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v76_l684)))


(def v79_l699 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t80_l702
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v79_l699)))


(def v82_l712 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t83_l715
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v82_l712)))


(def
 v85_l730
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale :log}})))


(deftest
 t86_l733
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v85_l730)))


(def
 v88_l744
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale {:range [4 16]}}})))


(deftest
 t89_l748
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
        (mapv (fn* [p1__78820#] (* 2 (:magnitude p1__78820#))))))
      (every?
       (fn*
        [p1__78821#]
        (re-find #":values does not recognize" (refusal p1__78821#)))
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
   v88_l744)))


(def
 v91_l806
 (->
  plants
  (pj/lay-point
   {:x {:column :height, :scale {:type :log}}, :y :weight})))


(deftest
 t92_l809
 (is
  ((fn [fr] (= :log (-> fr pj/plan :panels first :x-scale :type)))
   v91_l806)))


(def
 v94_l822
 (try
  (->
   plants
   (pj/pose :height :weight)
   (pj/lay-point {:x {:column :height, :scale :log}})
   (pj/lay-line {:x {:column :height, :scale :linear}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t95_l831
 (is
  ((fn [m] (re-find #"Layers name different scales for the :x axis" m))
   v94_l822)))


(def
 v97_l843
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t98_l848
 (is ((fn [m] (re-find #":text has no scale to set" m)) v97_l843)))


(def
 v100_l864
 (kind/table
  {:column-names [:aesthetic :role],
   :row-vectors
   (->>
    (pj/aesthetic-roles)
    (sort-by (comp str key))
    (mapv (fn [[k role]] [k role])))}))


(deftest
 t101_l870
 (is
  ((fn
    [_]
    (=
     #{:grouping :panel :positional :appearance}
     (set (vals (pj/aesthetic-roles)))))
   v100_l864)))
