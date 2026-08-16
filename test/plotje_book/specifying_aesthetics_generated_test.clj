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
 v6_l117
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :species, :scale true}})))


(deftest
 t7_l120
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v6_l117)))


(def
 v9_l136
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :shade, :scale false}, :size 9})))


(deftest
 t10_l140
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
   v9_l136)))


(def
 v12_l158
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "#0077BB", :scale false}, :size 9})))


(deftest
 t13_l162
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (= 1 (count (-> p :panels first :layers first :groups))))))
   v12_l158)))


(def
 v15_l181
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "Model A", :scale true}, :size 9})))


(deftest
 t16_l185
 (is
  ((fn
    [fr]
    (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v15_l181)))


(def
 v18_l236
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t19_l239
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v18_l236)))


(def
 v21_l260
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t22_l263
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= 9 (:radius (:style (-> p :panels first :layers first))))
      (nil? (:legend p)))))
   v21_l260)))


(def
 v24_l275
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 6.0, :text "target"})))


(deftest
 t25_l279
 (is
  ((fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))
   v24_l275)))


(def
 v27_l307
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
 v28_l316
 {:species-short (drawn-colors {:color :species}),
  :species-full
  (drawn-colors {:color {:column :species, :scale true}}),
  :shade-short (drawn-colors {:color :shade}),
  :shade-full (drawn-colors {:color {:column :shade, :scale true}}),
  :written-short (drawn-colors {:color "#0077BB"}),
  :written-full
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t29_l323
 (is
  ((fn
    [m]
    (and
     (= (:species-short m) (:species-full m))
     (= (:shade-short m) (:shade-full m))
     (= (:written-short m) (:written-full m))
     (= 3 (count (:species-short m)))
     (= #{"rgb(0,119,187)"} (:written-short m))))
   v28_l316)))


(def
 v31_l340
 {:column-alone (drawn-colors {:color {:column :shade}}),
  :column-scaled (drawn-colors {:color {:column :shade, :scale true}}),
  :value-alone (drawn-colors {:color {:value "#0077BB"}}),
  :value-drawn
  (drawn-colors {:color {:value "#0077BB", :scale false}})})


(deftest
 t32_l345
 (is
  ((fn
    [m]
    (and
     (= (:column-alone m) (:column-scaled m))
     (= (:value-alone m) (:value-drawn m))))
   v31_l340)))


(def
 v34_l361
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t35_l367
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v34_l361)))


(def
 v37_l391
 (def
  named-after-a-colour
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v38_l396 named-after-a-colour)


(def
 v40_l400
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t41_l403
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
   v40_l400)))


(def
 v43_l413
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t44_l416
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v43_l413)))


(def v46_l427 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v47_l431 integer-named)


(def v49_l437 (-> integer-named (pj/lay-point {:x 0, :y 1})))


(deftest
 t50_l440
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v49_l437)))


(def v52_l448 (-> plants (pj/lay-point {:x 0, :y :weight})))


(deftest
 t53_l451
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v52_l448)))


(def v55_l459 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t56_l462
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v55_l459)))


(def v58_l468 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t59_l471
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v58_l468)))


(def
 v61_l481
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale :log}})))


(deftest
 t62_l484
 (is
  ((fn [fr] (= :log (-> fr pj/plan :size-legend :scale-type)))
   v61_l481)))


(def
 v64_l492
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:size {:column :weight, :scale {:range [4 16]}}})))


(deftest
 t65_l496
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
      (mapv (fn* [p1__74869#] (* 2 (:magnitude p1__74869#)))))))
   v64_l492)))


(def
 v67_l528
 (try
  (->
   plants
   (pj/lay-point :height :weight {:x {:column :height, :scale :log}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t68_l535
 (is
  ((fn [m] (re-find #"an axis takes its scale from the pose" m))
   v67_l528)))


(def
 v70_l547
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t71_l552
 (is ((fn [m] (re-find #":text has no scale to set" m)) v70_l547)))
