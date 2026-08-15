(ns
 plotje-book.specifying-aesthetics-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [clojure.test :refer [deftest is]]))


(def
 v3_l31
 (def
  plants
  {:height [12 25 18 31],
   :weight [1.4 3.9 2.2 4.6],
   :species ["fern" "moss" "fern" "ivy"],
   :shade ["#CC3311" "#0077BB" "#CC3311" "#009988"]}))


(def v4_l37 plants)


(def
 v6_l52
 (def
  named-after-a-colour
  {:height [12 25], :weight [1.4 3.9], "blue" ["p" "q"]}))


(def
 v8_l60
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:value "blue"}})))


(deftest
 t9_l63
 (is
  ((fn
    [fr]
    (=
     [[0.0 0.0 1.0 1.0]]
     (->>
      (pj/plan fr)
      :panels
      first
      :layers
      first
      :groups
      (mapv :color))))
   v8_l60)))


(def
 v11_l71
 (->
  named-after-a-colour
  (pj/lay-point :height :weight {:color {:column "blue"}})))


(deftest
 t12_l74
 (is
  ((fn
    [fr]
    (= ["p" "q"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v11_l71)))


(def v14_l90 (-> plants (pj/lay-point :height :weight {:color :shade})))


(deftest
 t15_l93
 (is
  ((fn [fr] (= 3 (count (:entries (:legend (pj/plan fr)))))) v14_l90)))


(def
 v17_l110
 (->
  plants
  (pj/lay-point :height :weight {:color "steelblue", :size 9})))


(deftest
 t18_l113
 (is
  ((fn
    [fr]
    (let
     [layer (-> fr pj/plan :panels first :layers first)]
     (and
      (= 9 (:radius (:style layer)))
      (nil? (:legend (pj/plan fr))))))
   v17_l110)))


(def
 v20_l122
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text {:x 20, :y 5.0, :text "target"})))


(deftest
 t21_l126
 (is
  ((fn [fr] (< 5.0 (second (-> fr pj/plan :panels first :y-domain))))
   v20_l122)))


(def
 v23_l147
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:column :shade, :scale false}, :size 9})))


(deftest
 t24_l151
 (is
  ((fn
    [fr]
    (and
     (nil? (:legend (pj/plan fr)))
     (=
      [(/ 204.0 255) (/ 51.0 255) (/ 17.0 255) 1.0]
      (->
       fr
       pj/plan
       :panels
       first
       :layers
       first
       :groups
       first
       :color))))
   v23_l147)))


(def
 v26_l161
 (->
  (assoc plants :r [4 8 12 16])
  (pj/lay-point :height :weight {:size {:column :r, :scale false}})))


(deftest
 t27_l164
 (is
  ((fn
    [fr]
    (and
     (true? (-> fr pj/plan :panels first :layers first :size-drawn?))
     (nil? (:size-legend (pj/plan fr)))))
   v26_l161)))


(def
 v29_l174
 (->
  plants
  (pj/lay-point
   :height
   :weight
   {:color {:value "Model A", :scale true}, :size 9})))


(deftest
 t30_l178
 (is
  ((fn
    [fr]
    (= ["Model A"] (mapv :label (:entries (:legend (pj/plan fr))))))
   v29_l174)))


(def
 v32_l184
 (try
  (-> plants (pj/lay-point :height :weight {:color "Model A"}) pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t33_l191
 (is
  ((fn
    [m]
    (and
     (re-find #"not found in dataset" m)
     (re-find #"not a color either" m)))
   v32_l184)))


(def
 v35_l203
 (->
  plants
  (pj/lay-point :height :weight)
  (pj/lay-text
   {:x 20, :y {:value 14, :scale false}, :text "fixed to the panel"})))


(deftest
 t36_l209
 (is
  ((fn
    [fr]
    (let
     [panel (-> fr pj/plan :panels first)]
     (and
      (true? (-> panel :layers last :y-drawn?))
      (< (second (:y-domain panel)) 14))))
   v35_l203)))


(def v38_l225 (def integer-named {0 [1 2 3], 1 [4 5 6]}))


(def v39_l229 integer-named)


(def v41_l233 (-> integer-named (pj/lay-point {:x {:column 0}, :y 1})))


(deftest
 t42_l236
 (is
  ((fn [fr] (= [0.9 3.1] (-> fr pj/plan :panels first :x-domain)))
   v41_l233)))


(def v44_l241 (-> integer-named (pj/lay-point {:x {:value 0}, :y 1})))


(deftest
 t45_l244
 (is
  ((fn [fr] (= [-1.0 1.0] (-> fr pj/plan :panels first :x-domain)))
   v44_l241)))


(def
 v47_l253
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
 t48_l260
 (is
  ((fn [m] (re-find #"A mapping's :scale is true or false" m))
   v47_l253)))
