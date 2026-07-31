(ns
 plotje-book.layer-types-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [scicloj.plotje.layer-type :as layer-type]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def
 v3_l40
 (defn
  used-by
  "Sorted comma-separated layer-type names whose `field` equals `value`."
  [field value]
  (->>
   (layer-type/registered)
   (filter (fn [[_ m]] (= value (or (get m field) :identity))))
   (map (comp name key))
   sort
   (str/join ", "))))


(def
 v4_l49
 (defn
  distinct-in-order
  "Distinct values of `field` across layer types, in first-seen order."
  [field]
  (let
   [seen (volatile! #{})]
   (reduce
    (fn
     [acc k]
     (let
      [v (get (layer-type/lookup k) field)]
      (if (@seen v) acc (do (vswap! seen conj v) (conj acc v)))))
    []
    layer-type/layer-type-order))))


(def
 v6_l67
 (kind/table
  {:column-names ["Layer type" "Mark" "Stat" "Position" "Presets"],
   :row-maps
   (for
    [k layer-type/layer-type-order :let [m (layer-type/lookup k)]]
    {"Layer type" (kind/code (pr-str k)),
     "Mark" (kind/code (pr-str (:mark m))),
     "Stat" (kind/code (pr-str (:stat m))),
     "Position" (kind/code (pr-str (or (:position m) :identity))),
     "Presets" (if-let [d (:defaults m)] (kind/code (pr-str d)) "")})}))


(deftest t7_l78 (is ((fn [t] (= 25 (count (:row-maps t)))) v6_l67)))


(deftest
 t9_l87
 (is
  ((fn
    [_]
    (let
     [row
      (fn
       [k]
       (select-keys (layer-type/lookup k) [:mark :stat :defaults]))]
     (and
      (= {:mark :text, :stat :identity} (row :text))
      (=
       {:mark :text, :stat :identity, :defaults {:box true}}
       (row :label)))))
   v6_l67)))


(def
 v11_l110
 (kind/table
  {:column-names ["Mark" "Shape" "Used by"],
   :row-maps
   (for
    [mk (distinct-in-order :mark)]
    {"Mark" (kind/code (pr-str mk)),
     "Shape" (pj/mark-doc mk),
     "Used by" (used-by :mark mk)})}))


(deftest t12_l118 (is ((fn [t] (= 21 (count (:row-maps t)))) v11_l110)))


(def
 v14_l128
 (kind/table
  {:column-names ["Stat" "What it computes" "Used by"],
   :row-maps
   (for
    [st (distinct-in-order :stat)]
    {"Stat" (kind/code (pr-str st)),
     "What it computes" (pj/stat-doc st),
     "Used by" (used-by :stat st)})}))


(deftest t15_l136 (is ((fn [t] (pos? (count (:row-maps t)))) v14_l128)))


(def
 v17_l145
 (kind/table
  {:column-names ["Position" "What it does" "Used by"],
   :row-maps
   (for
    [pos [:identity :dodge :stack :fill]]
    {"Position" (kind/code (pr-str pos)),
     "What it does" (pj/position-doc pos),
     "Used by" (used-by :position pos)})}))


(deftest t18_l153 (is ((fn [t] (pos? (count (:row-maps t)))) v17_l145)))


(def
 v20_l171
 (kind/table
  {:column-names ["Option" "Description"],
   :row-maps
   (for
    [k layer-type/universal-layer-options]
    {"Option" (kind/code (pr-str k)),
     "Description" (get layer-type/layer-option-docs k)})}))


(deftest t21_l178 (is ((fn [t] (pos? (count (:row-maps t)))) v20_l171)))


(def
 v23_l186
 (kind/table
  {:column-names ["Layer type" "Additional options"],
   :row-maps
   (for
    [k
     layer-type/layer-type-order
     :let
     [m (layer-type/lookup k) accepts (:accepts m)]
     :when
     (seq accepts)]
    {"Layer type" (kind/code (pr-str k)),
     "Additional options" accepts})}))


(deftest t24_l196 (is ((fn [t] (pos? (count (:row-maps t)))) v23_l186)))


(def
 v26_l201
 (kind/table
  {:column-names ["Option" "Description"],
   :row-maps
   (for
    [[k desc] (sort-by key layer-type/layer-option-docs)]
    {"Option" (kind/code (pr-str k)), "Description" desc})}))


(deftest t27_l208 (is ((fn [t] (pos? (count (:row-maps t)))) v26_l201)))
