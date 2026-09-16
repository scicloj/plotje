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


(def
 v21_l117
 (->
  numerical
  (pj/lay-point :k :v)
  (pj/lay-text {:x 2, :y 1.5, :text "a value of 1.5"})))


(deftest
 t22_l121
 (is
  ((fn
    [v]
    (let
     [with
      (-> v pj/plan :panels first :y-domain)
      without
      (->
       numerical
       (pj/lay-point :k :v)
       pj/plan
       :panels
       first
       :y-domain)]
     (and (< (first with) (first without)) (< (first with) 1.5))))
   v21_l117)))


(def
 v24_l134
 (->
  categorical
  (pj/lay-point :k :v)
  (pj/lay-text {:x 1.5, :y 25, :text "between a and b"})))


(deftest
 t25_l138
 (is
  ((fn
    [v]
    (let
     [frame
      (-> v pj/frames :panels first)
      at
      (fn [c] (first (pj/to-drawing frame c 25)))]
     (and
      (= ["a" "b" "c" "d"] (-> v pj/plan :panels first :x-domain))
      (< (abs (- (at 1.5) (/ (+ (at "a") (at "b")) 2.0))) 1.0E-9))))
   v24_l134)))


(def v27_l152 (-> temporal (pj/lay-point :k :v)))


(deftest
 t28_l155
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
   v27_l152)))


(def v30_l166 (-> categorical (pj/lay-point :v :v {:color :k})))


(deftest
 t31_l169
 (is
  ((fn [v] (= 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v30_l166)))


(def v33_l176 (-> numerical (pj/lay-point :v :v {:color :k})))


(deftest
 t34_l179
 (is
  ((fn [v] (< 4 (count (disj (:colors (pj/svg-summary v)) "none"))))
   v33_l176)))


(def
 v36_l189
 (->
  {:k ["a" "b" "c" "d"], :v [10 20 30 40], :warmth [7 30 12 21]}
  (pj/lay-bar :k :v {:color :warmth})))


(deftest
 t37_l192
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
   v36_l189)))


(def
 v39_l226
 (defn
  inferred-mark
  "The mark a pose is drawn with when no layer type is named."
  [pose]
  (-> pose pj/plan :panels first :layers first :mark)))


(def
 v40_l231
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
 t41_l240
 (is
  ((fn
    [ds]
    (=
     [:bar :bar :rect :line :boxplot :boxplot :point]
     (vec (:mark ds))))
   v40_l231)))


(def v43_l251 (pj/pose temporal :k :v))


(deftest
 t44_l253
 (is ((fn [v] (= 1 (:lines (pj/svg-summary v)))) v43_l251)))


(def
 v46_l261
 (def
  readings
  {:batch ["a" "a" "a" "a" "b" "b" "b" "c" "c"],
   :reading [3 5 4 6 8 9 7 2 6]}))


(def v47_l265 (pj/pose readings :batch :reading))


(deftest
 t48_l267
 (is ((fn [v] (= :boxplot (inferred-mark v))) v47_l265)))


(def v50_l273 (pj/pose readings :batch))


(deftest
 t51_l275
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
   v50_l273)))


(def
 v53_l294
 (try
  (-> numerical (pj/lay-boxplot :k :v) pj/plot)
  (catch Exception e (ex-message e))))


(deftest
 t54_l300
 (is ((fn [m] (re-find #"requires a categorical column" m)) v53_l294)))


(def v56_l307 (-> readings (pj/lay-boxplot :batch :reading)))


(deftest
 t57_l310
 (is ((fn [v] (= 3 (:polygons (pj/svg-summary v)))) v56_l307)))


(def
 v59_l324
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue {:x-type :categorical})))


(deftest
 t60_l327
 (is
  ((fn
    [v]
    (let
     [ticks (-> v pj/plan :panels first :x-ticks)]
     (and
      (true? (:categorical? ticks))
      (= ["2020" "2021" "2022" "2023"] (vec (:labels ticks))))))
   v59_l324)))


(def
 v62_l336
 (->
  {:year [2020 2021 2022 2023], :revenue [10 20 30 40]}
  (pj/lay-bar :year :revenue)))


(deftest
 t63_l339
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
   v62_l336)))


(def
 v65_l367
 (try
  (->
   {:species ["setosa" "versicolor"], :count [50 50]}
   (pj/lay-point :species :count {:x-type :numerical})
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t66_l374
 (is ((fn [m] (re-find #"which holds categorical values" m)) v65_l367)))


(def v68_l400 (-> numerical (pj/lay-point :k :v) (pj/scale :x :log)))


(deftest
 t69_l404
 (is
  ((fn [v] (= :log (-> v pj/plan :panels first :x-scale :type)))
   v68_l400)))


(def
 v71_l410
 (try
  (->
   numerical
   (pj/lay-point :k :v)
   (pj/scale :x :categorical)
   pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t72_l418
 (is
  ((fn [m] (re-find #"set :x-type or :y-type to :categorical" m))
   v71_l410)))


(def
 v74_l424
 (try
  (-> categorical (pj/lay-point :k :v) (pj/scale :x :log) pj/plan)
  (catch clojure.lang.ExceptionInfo e (ex-message e))))


(deftest
 t75_l432
 (is ((fn [m] (re-find #"requires numeric data" m)) v74_l424)))


(def
 v77_l440
 (-> categorical (pj/lay-point :k :v) (pj/scale :x :linear)))


(deftest
 t78_l444
 (is
  ((fn
    [v]
    (=
     (pj/svg-summary v)
     (pj/svg-summary (-> categorical (pj/lay-point :k :v)))))
   v77_l440)))
