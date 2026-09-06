(ns
 plotje-book.column-types-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [tablecloth.column.api :as tcc]
  [clojure.test :refer [deftest is]]))


(def v3_l42 (def numerical {:k [1 2 3 4], :v [10 20 30 40]}))


(def v4_l45 (def categorical {:k ["a" "b" "c" "d"], :v [10 20 30 40]}))


(def
 v5_l48
 (def
  temporal
  {:k
   [(java.time.LocalDate/parse "2026-01-15")
    (java.time.LocalDate/parse "2026-02-15")
    (java.time.LocalDate/parse "2026-03-15")
    (java.time.LocalDate/parse "2026-04-15")],
   :v [10 20 30 40]}))


(def
 v7_l58
 (def ratios {:k [(/ 1 3) (/ 2 3) (/ 4 3) (/ 5 3)], :v [10 20 30 40]}))


(def
 v9_l64
 (tc/dataset
  (for
   [[label d]
    [["numerical" numerical]
     ["categorical" categorical]
     ["temporal" temporal]
     ["ratios" ratios]]]
   {:dataset label, :datatype (tcc/typeof (:k (tc/dataset d)))})))


(deftest
 t10_l72
 (is
  ((fn
    [ds]
    (=
     [:int64 :string :packed-local-date :object]
     (vec (:datatype ds))))
   v9_l64)))


(def v12_l80 (-> ratios (pj/lay-point :k :v)))


(deftest
 t13_l83
 (is
  ((fn
    [v]
    (false? (:categorical? (-> v pj/plan :panels first :x-ticks))))
   v12_l80)))


(def v15_l92 (-> numerical (pj/lay-point :k :v)))


(deftest
 t16_l95
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (false? (:categorical? ticks))
      (contains? (set (:labels ticks)) "2"))))
   v15_l92)))


(def v18_l104 (-> categorical (pj/lay-point :k :v)))


(deftest
 t19_l107
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["a" "b" "c" "d"] (vec (:labels ticks))))))
   v18_l104)))


(def v21_l116 (-> temporal (pj/lay-point :k :v)))


(deftest
 t22_l119
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (false? (:categorical? ticks))
      (some
       (fn [l] (re-find #"^[A-Z][a-z]{2}-\d\d$" l))
       (:labels ticks)))))
   v21_l116)))


(def v24_l130 (-> categorical (pj/lay-point :v :v {:color :k})))


(deftest
 t25_l133
 (is
  ((fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v24_l130)))


(def v27_l140 (-> numerical (pj/lay-point :v :v {:color :k})))


(deftest
 t28_l143
 (is
  ((fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v27_l140)))


(def
 v30_l153
 (->
  {:k ["a" "b" "c" "d"], :v [10 20 30 40], :warmth [7 30 12 21]}
  (pj/lay-bar :k :v {:color :warmth})))


(deftest
 t31_l156
 (is
  ((fn
    [v]
    (let
     [colors
      (-> v pj/plan :panels first :layers first :groups first :colors)
      lightness
      (fn [c] (reduce + (take 3 c)))]
     (and
      (= 4 (:polygons (pj/svg-summary v)))
      (= 4 (count (distinct colors)))
      (> (lightness (nth colors 1)) (lightness (nth colors 3))))))
   v30_l153)))


(def
 v33_l190
 (defn
  inferred-mark
  "The mark a pose is drawn with when no layer type is named."
  [pose]
  (-> pose pj/plan :panels first :layers first :mark)))


(def
 v34_l195
 (tc/dataset
  [{:x-column "numerical",
    :y-column "none",
    :mark (inferred-mark (pj/pose numerical :k))}
   {:x-column "temporal",
    :y-column "none",
    :mark (inferred-mark (pj/pose temporal :k))}
   {:x-column "categorical",
    :y-column "none",
    :mark (inferred-mark (pj/pose categorical :k))}
   {:x-column "temporal",
    :y-column "numerical",
    :mark (inferred-mark (pj/pose temporal :k :v))}
   {:x-column "categorical",
    :y-column "numerical",
    :mark (inferred-mark (pj/pose categorical :k :v))}
   {:x-column "numerical",
    :y-column "categorical",
    :mark (inferred-mark (pj/pose categorical :v :k))}
   {:x-column "numerical",
    :y-column "numerical",
    :mark (inferred-mark (pj/pose numerical :k :v))}]))


(deftest
 t35_l204
 (is
  ((fn
    [ds]
    (=
     [:bar :bar :rect :line :boxplot :boxplot :point]
     (vec (:mark ds))))
   v34_l195)))


(def v37_l215 (pj/pose temporal :k :v))


(deftest
 t38_l217
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v37_l215)))


(def
 v40_l225
 (def
  readings
  {:batch ["a" "a" "a" "a" "b" "b" "b" "c" "c"],
   :reading [3 5 4 6 8 9 7 2 6]}))


(def v41_l229 (pj/pose readings :batch :reading))


(deftest
 t42_l231
 (is ((fn [v] (= :boxplot (inferred-mark v))) v41_l229)))


(def v44_l237 (pj/pose readings :batch))


(deftest
 t45_l239
 (is
  ((fn
    [v]
    (and
     (= :rect (inferred-mark v))
     (= 3 (:polygons (pj/svg-summary v)))
     (=
      [4 3 2]
      (->>
       (pj/plan v)
       :panels
       first
       :layers
       first
       :groups
       first
       :counts
       (mapv :count)))))
   v44_l237)))


(def
 v47_l258
 (try
  (-> numerical (pj/lay-boxplot :k :v) pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t48_l264
 (is ((fn [m] (re-find #"requires a categorical column" m)) v47_l258)))


(def v50_l271 (-> readings (pj/lay-boxplot :batch :reading)))


(deftest
 t51_l274
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v50_l271)))


(def
 v53_l288
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue {:x-type :categorical})))


(deftest
 t54_l291
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))
   v53_l288)))


(def
 v56_l300
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue)))


(deftest
 t57_l303
 (is
  ((fn
    [v]
    (let
     [ticks
      (-> v pj/plan :panels first :x-ticks)
      groups
      (fn
       [mapping]
       (->
        {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
        (pj/lay-point :year :revenue mapping)
        pj/plan
        :panels
        first
        :layers
        first
        :groups
        count))]
     (and
      (false? (:categorical? ticks))
      (contains? (set (:labels ticks)) "2020.5")
      (= 1 (groups {:color :year}))
      (= 4 (groups {:color :year, :color-type :categorical})))))
   v56_l300)))


(def
 v59_l331
 (try
  (->
   {:species ["setosa" "versicolor"], :count [50 50]}
   (pj/lay-point :species :count {:x-type :numerical})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t60_l338
 (is ((fn [m] (re-find #"which holds categorical values" m)) v59_l331)))


(def v62_l364 (-> numerical (pj/lay-point :k :v) (pj/scale :x :log)))


(deftest
 t63_l368
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :x-scale :type)))
   v62_l364)))


(def
 v65_l374
 (try
  (->
   numerical
   (pj/lay-point :k :v)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t66_l382
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v65_l374)))


(def
 v68_l388
 (try
  (-> categorical (pj/lay-point :k :v) (pj/scale :x :log) pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t69_l396
 (is ((fn [m] (re-find #"requires numeric data" m)) v68_l388)))


(def
 v71_l404
 (-> categorical (pj/lay-point :k :v) (pj/scale :x :linear)))


(deftest
 t72_l408
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary (-> categorical (pj/lay-point :k :v)))))
   v71_l404)))
