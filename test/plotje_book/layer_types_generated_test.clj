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
 v6_l63
 (kind/table
  {:column-names ["Layer type" "Mark" "Stat" "Position"],
   :row-maps
   (for
    [k layer-type/layer-type-order :let [m (layer-type/lookup k)]]
    {"Layer type" (kind/code (pr-str k)),
     "Mark" (kind/code (pr-str (:mark m))),
     "Stat" (kind/code (pr-str (:stat m))),
     "Position" (kind/code (pr-str (or (:position m) :identity)))})}))


(deftest t7_l73 (is ((fn [t] (= 25 (count (:row-maps t)))) v6_l63)))


(def
 v9_l94
 (kind/table
  {:column-names ["Mark" "Shape" "Used by"],
   :row-maps
   (for
    [mk (distinct-in-order :mark)]
    {"Mark" (kind/code (pr-str mk)),
     "Shape" (pj/mark-doc mk),
     "Used by" (used-by :mark mk)})}))


(deftest t10_l102 (is ((fn [t] (= 22 (count (:row-maps t)))) v9_l94)))


(def
 v12_l112
 (kind/table
  {:column-names ["Stat" "What it computes" "Used by"],
   :row-maps
   (for
    [st (distinct-in-order :stat)]
    {"Stat" (kind/code (pr-str st)),
     "What it computes" (pj/stat-doc st),
     "Used by" (used-by :stat st)})}))


(deftest t13_l120 (is ((fn [t] (pos? (count (:row-maps t)))) v12_l112)))


(def
 v15_l129
 (kind/table
  {:column-names ["Position" "What it does" "Used by"],
   :row-maps
   (for
    [pos [:identity :dodge :stack :fill]]
    {"Position" (kind/code (pr-str pos)),
     "What it does" (pj/position-doc pos),
     "Used by" (used-by :position pos)})}))


(deftest t16_l137 (is ((fn [t] (pos? (count (:row-maps t)))) v15_l129)))


(def
 v18_l155
 (kind/table
  {:column-names ["Option" "Description"],
   :row-maps
   (for
    [k layer-type/universal-layer-options]
    {"Option" (kind/code (pr-str k)),
     "Description" (get layer-type/layer-option-docs k)})}))


(deftest t19_l162 (is ((fn [t] (pos? (count (:row-maps t)))) v18_l155)))


(def
 v21_l170
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


(deftest t22_l180 (is ((fn [t] (pos? (count (:row-maps t)))) v21_l170)))


(def
 v24_l185
 (kind/table
  {:column-names ["Option" "Description"],
   :row-maps
   (for
    [[k desc] (sort-by key layer-type/layer-option-docs)]
    {"Option" (kind/code (pr-str k)), "Description" desc})}))


(deftest t25_l192 (is ((fn [t] (pos? (count (:row-maps t)))) v24_l185)))
