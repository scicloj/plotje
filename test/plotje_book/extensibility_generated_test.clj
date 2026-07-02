(ns
 plotje-book.extensibility-generated-test
 (:require
  [scicloj.metamorph.ml.rdatasets :as rdatasets]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [scicloj.plotje.impl.stat :as stat]
  [scicloj.plotje.impl.extract :as extract]
  [scicloj.plotje.render.mark :as mark]
  [scicloj.plotje.render.svg :as svg]
  [scicloj.plotje.impl.render :as render]
  [membrane.ui]
  [clojure.test :refer [deftest is]]))


(def
 v3_l39
 (kind/mermaid
  "\ngraph LR\n  B[\"Pose\"] -->|pj/pose->draft| D[\"Draft\"]\n  D -->|pj/draft->plan| P[\"Plan\"]\n  P -->|pj/plan->membrane| M[\"Membrane\"]\n  M -->|pj/membrane->plot| F[\"Plot\"]\n  P -.->|pj/plan->plot| F\n  style B fill:#d1c4e9\n  style D fill:#e8f5e9\n  style P fill:#fff3e0\n  style M fill:#e3f2fd\n  style F fill:#fce4ec\n"))


(def
 v5_l73
 (kind/table
  {:column-names ["Dispatch value" "What it does"],
   :row-maps
   (->>
    (methods stat/compute-stat)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "What it does" (pj/stat-doc k)})))}))


(deftest t6_l84 (is ((fn [t] (= 11 (count (:row-maps t)))) v5_l73)))


(def v8_l90 (layer-type/lookup :histogram))


(deftest t9_l92 (is ((fn [m] (= :bin (:stat m))) v8_l90)))


(def v11_l98 (layer-type/lookup :bar))


(deftest t12_l100 (is ((fn [m] (nil? (:stat m))) v11_l98)))


(def v14_l104 (layer-type/lookup :point))


(deftest t15_l106 (is ((fn [m] (= :identity (:stat m))) v14_l104)))


(def
 v17_l139
 (kind/table
  {:column-names ["Dispatch value" "Output"],
   :row-maps
   (->>
    (methods extract/extract-layer)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Output" (pj/mark-doc k)})))}))


(deftest t18_l150 (is ((fn [t] (= 18 (count (:row-maps t)))) v17_l139)))


(def
 v20_l155
 (->
  (rdatasets/datasets-iris)
  (pj/lay-point :sepal-length :sepal-width {:color :species})))


(deftest
 t21_l158
 (is ((fn [v] (= 150 (:points (pj/svg-summary v)))) v20_l155)))


(def
 v23_l162
 (let
  [s
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})
    pj/plan)
   layer
   (first (:layers (first (:panels s))))]
  layer))


(deftest
 t24_l168
 (is
  ((fn
    [m]
    (and (= :point (:mark m)) (number? (get-in m [:style :opacity]))))
   v23_l162)))


(def
 v26_l179
 (kind/table
  {:column-names ["Dispatch value" "Membrane output"],
   :row-maps
   (->>
    (methods mark/layer->membrane)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Membrane output" (pj/membrane-mark-doc k)})))}))


(deftest t27_l190 (is ((fn [t] (= 18 (count (:row-maps t)))) v26_l179)))


(def v29_l274 (mark/mark-clip-region :point))


(deftest
 t30_l276
 (is ((fn* [p1__91480#] (= :drawing-area p1__91480#)) v29_l274)))


(def v31_l278 (mark/mark-clip-region :rug))


(deftest
 t32_l280
 (is ((fn* [p1__91481#] (= :panel-box p1__91481#)) v31_l278)))


(def
 v34_l290
 (defmethod mark/mark-clip-region :margin-glyph [_] :panel-box))


(def v35_l292 (mark/mark-clip-region :margin-glyph))


(deftest
 t36_l294
 (is ((fn* [p1__91482#] (= :panel-box p1__91482#)) v35_l292)))


(def v38_l298 (remove-method mark/mark-clip-region :margin-glyph))


(def v39_l300 (contains? (methods mark/mark-clip-region) :margin-glyph))


(deftest t40_l302 (is (false? v39_l300)))


(def
 v42_l326
 (def
  my-plan
  (->
   (rdatasets/datasets-iris)
   (pj/lay-point :sepal-length :sepal-width {:color :species})
   pj/plan)))


(def v43_l331 (first (pj/plan->plot my-plan :svg {})))


(deftest t44_l333 (is ((fn [v] (= :svg v)) v43_l331)))


(def v46_l337 (def my-figure (pj/plan->plot my-plan :svg {})))


(def v47_l339 (vector? my-figure))


(deftest t48_l341 (is ((fn [v] (true? v)) v47_l339)))


(def v50_l391 (def my-membrane (pj/plan->membrane my-plan)))


(def v51_l393 (pj/membrane? my-membrane))


(deftest t52_l395 (is ((fn [v] (true? v)) v51_l393)))


(def v53_l397 (membrane.ui/width my-membrane))


(deftest t54_l399 (is ((fn [v] (number? v)) v53_l397)))


(def v55_l401 (first (pj/membrane->plot my-membrane :svg {})))


(deftest t56_l403 (is ((fn [v] (= :svg v)) v55_l401)))


(def
 v58_l409
 (def
  shortcut-membrane
  (pj/membrane
   (->
    (rdatasets/datasets-iris)
    (pj/lay-point :sepal-length :sepal-width {:color :species})))))


(def v59_l414 (pj/membrane? shortcut-membrane))


(deftest t60_l416 (is ((fn [v] (true? v)) v59_l414)))


(def
 v62_l455
 (kind/table
  {:column-names ["Dispatch value" "Scale type"],
   :row-maps
   (->>
    (methods scicloj.plotje.impl.scale/make-scale)
    keys
    (filter keyword?)
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Scale type" (pj/scale-doc k)})))}))


(deftest t63_l465 (is ((fn [t] (= 3 (count (:row-maps t)))) v62_l455)))


(def
 v65_l476
 (kind/table
  {:column-names ["Dispatch value" "Behavior"],
   :row-maps
   (->>
    (methods scicloj.plotje.impl.coord/make-coord)
    keys
    (filter keyword?)
    (remove #{:default})
    sort
    (mapv
     (fn
      [k]
      {"Dispatch value" (kind/code (pr-str k)),
       "Behavior" (pj/coord-doc k)})))}))


(deftest t66_l487 (is ((fn [t] (= 4 (count (:row-maps t)))) v65_l476)))


(def
 v68_l494
 (-> (rdatasets/datasets-iris) (pj/lay-bar :species) (pj/coord :flip)))


(deftest
 t69_l498
 (is
  ((fn
    [v]
    (let
     [s (pj/svg-summary v)]
     (and (= 1 (:panels s)) (pos? (:polygons s)))))
   v68_l494)))


(def
 v71_l514
 (defmethod
  stat/compute-stat
  :quantile
  [draft-layer]
  {:points [], :x-domain [0 1], :y-domain [0 1]}))


(def
 v72_l517
 (defmethod
  stat/compute-stat
  [:quantile :doc]
  [_]
  "Quantile regression bands"))


(def v74_l522 (pj/stat-doc :quantile))


(deftest
 t75_l524
 (is ((fn [v] (= "Quantile regression bands" v)) v74_l522)))


(def v77_l532 (remove-method stat/compute-stat [:quantile :doc]))


(def v78_l534 (pj/stat-doc :quantile))


(deftest t79_l536 (is ((fn [v] (= "(no description)" v)) v78_l534)))


(def v81_l542 (remove-method stat/compute-stat :quantile))


(def
 v82_l544
 (count
  (remove
   #{:default}
   (filter keyword? (keys (methods stat/compute-stat))))))


(deftest t83_l546 (is ((fn [v] (= 11 v)) v82_l544)))
