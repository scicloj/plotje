(ns
 plotje-book.specifying-aesthetics-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l61
 (def
  plants
  {:height [12 25 18 31],
   :weight [1.4 3.9 2.2 4.6],
   :species ["fern" "moss" "fern" "ivy"],
   :shade ["#CC3311" "#0077BB" "#CC3311" "#009988"]}))


(def v4_l67 plants)


(def
 v6_l113
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :species, :scale true}})))


(deftest
 t7_l116
 (is
  ((fn
    [fr]
    (=
     ["fern" "moss" "ivy"]
     (mapv :label (:entries (:legend (pj/plan fr))))))
   v6_l113)))


(def
 v9_l132
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :shade, :scale false}, :size 9})))


(deftest
 t10_l136
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
   v9_l132)))


(def
 v12_l154
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "#0077BB", :scale false}, :size 9})))


(deftest
 t13_l158
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (nil? (:legend p))
      (= 1 (count (-> p :panels first :layers first :groups))))))
   v12_l154)))


(def
 v15_l177
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "Model A", :scale true}, :size 9})))


(deftest
 t16_l181
 (is
  ((fn
    [fr]
    (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v15_l177)))


(def
 v18_l231
 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t19_l234
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v18_l231)))


(def
 v21_l255
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t22_l258
 (is
  ((fn
    [fr]
    (let
     [p (pj/plan fr)]
     (and
      (= 9 (:radius (:style (-> p :panels first :layers first))))
      (nil? (:legend p)))))
   v21_l255)))


(def
 v24_l270
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 6.0, :text "target"})))


(deftest
 t25_l274
 (is
  ((fn [fr] (<= 6.0 (second (-> fr pj/plan :panels first :y-domain))))
   v24_l270)))


(def
 v27_l297
 (let
  [colors
   (fn
    [m]
    (->>
     (pj/lay-point plants :height :weight m)
     pj/plan
     :panels
     first
     :layers
     first
     :groups
     (mapv :color)))]
  {:species
   (=
    (colors {:color :species})
    (colors {:color {:column :species, :scale true}})),
   :shade
   (=
    (colors {:color :shade})
    (colors {:color {:column :shade, :scale true}})),
   :written
   (=
    (colors {:color "#0077BB"})
    (colors {:color {:value "#0077BB", :scale false}}))}))


(deftest
 t28_l307
 (is
  ((fn [m] (= {:species true, :shade true, :written true} m))
   v27_l297)))


(def
 v30_l315
 (let
  [colors
   (fn
    [m]
    (->>
     (pj/lay-point plants :height :weight m)
     pj/plan
     :panels
     first
     :layers
     first
     :groups
     (mapv :color)))]
  {:column-without-scale
   (=
    (colors {:color {:column :shade}})
    (colors {:color {:column :shade, :scale true}})),
   :value-without-scale
   (=
    (colors {:color {:value "#0077BB"}})
    (colors {:color {:value "#0077BB", :scale false}}))}))


(deftest
 t31_l323
 (is
  ((fn
    [m]
    (= {:column-without-scale true, :value-without-scale true} m))
   v30_l315)))


(def
 v33_l338
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t34_l344
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (:y-drawn? (last (:layers panel))))
      (< (second (:y-domain panel)) 14))))
   v33_l338)))


(def
 v36_l368
 (def
  named-after-a-colour
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def v37_l373 named-after-a-colour)


(def
 v39_l377
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:value "blue"}, :size 9})))


(deftest
 t40_l380
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
   v39_l377)))


(def
 v42_l390
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:column "blue"}, :size 9})))


(deftest
 t43_l393
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v42_l390)))


(def v45_l402 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v46_l406 integer-named)


(def v48_l411 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t49_l414
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v48_l411)))


(def v51_l420 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t52_l423
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v51_l420)))


(def
 v54_l431
 (try
  (pj/pose integer-named {:x 0, :y 1})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t55_l436
 (is
  ((fn
    [m]
    (and
     (re-find #"must be a column reference" m)
     (re-find #"If 0 is a column name" m)))
   v54_l431)))


(def
 v57_l449
 (try
  (->
   plants
   (pj/lay-point
    :height
    :weight
    {:size {:column :weight, :scale :log}})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t58_l456
 (is
  ((fn [m] (re-find #"A mapping's :scale is true or false" m))
   v57_l449)))


(def
 v60_l468
 (try
  (pj/lay-text
   plants
   :height
   :weight
   {:text {:column :species, :scale false}})
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t61_l473
 (is ((fn [m] (re-find #":text has no scale to set" m)) v60_l468)))
