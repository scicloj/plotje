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
  {:column-names
   ["Layer type" "Mark" "Stat" "Position" "Columns" "Presets"],
   :row-maps
   (for
    [k layer-type/layer-type-order :let [m (layer-type/lookup k)]]
    {"Layer type" (kind/code (pr-str k)),
     "Mark" (kind/code (pr-str (:mark m))),
     "Stat" (kind/code (pr-str (:stat m))),
     "Position" (kind/code (pr-str (or (:position m) :identity))),
     "Columns" (kind/code (pr-str (layer-type/column-counts k))),
     "Presets" (if-let [d (:defaults m)] (kind/code (pr-str d)) "")})}))


(deftest t7_l79 (is ((fn [t] (= 26 (count (:row-maps t)))) v6_l67)))


(def v9_l92 (layer-type/column-counts :bar))


(deftest
 t10_l94
 (is
  ((fn
    [cc]
    (let
     [columns
      (fn [pose] (mapv (juxt :x :y) (:layers (pj/draft pose))))
      data
      {:a [1 2 3], :b [4 5 6], :c [7 8 9]}]
     (and
      (= #{1 2} cc)
      (=
       [[:a :b] [:a :c]]
       (columns (-> data (pj/lay-point :a :b) (pj/lay-line {:y :c}))))
      (=
       [[:a :b] [:c nil]]
       (columns
        (-> data (pj/lay-point :a :b) (pj/lay-rug {:x :c})))))))
   v9_l92)))


(def v12_l134 (:varies (layer-type/lookup :point)))


(deftest
 t13_l136
 (is ((fn [m] (= {:size :radius, :alpha :opacity} m)) v12_l134)))


(def
 v15_l149
 (mapv
  (fn*
   [p1__78231#]
   (select-keys
    (layer-type/lookup p1__78231#)
    [:mark :stat :defaults]))
  [:text :label]))


(deftest
 t16_l152
 (is
  ((fn
    [rows]
    (=
     [{:mark :text, :stat :identity}
      {:mark :text, :stat :identity, :defaults {:box true}}]
     rows))
   v15_l149)))


(def
 v18_l175
 (kind/table
  {:column-names ["Mark" "Shape" "Used by"],
   :row-maps
   (for
    [mk (distinct-in-order :mark)]
    {"Mark" (kind/code (pr-str mk)),
     "Shape" (pj/mark-doc mk),
     "Used by" (used-by :mark mk)})}))


(deftest
 t19_l183
 (is
  ((fn
    [t]
    (and
     (= 22 (count (:row-maps t)))
     (=
      :tile
      (:mark (layer-type/lookup :tile))
      (:mark (layer-type/lookup :density-2d)))
     (=
      :line
      (:mark (layer-type/lookup :line))
      (:mark (layer-type/lookup :smooth)))))
   v18_l175)))


(def
 v21_l196
 (kind/table
  {:column-names ["Stat" "What it computes" "Used by"],
   :row-maps
   (for
    [st (distinct-in-order :stat)]
    {"Stat" (kind/code (pr-str st)),
     "What it computes" (pj/stat-doc st),
     "Used by" (used-by :stat st)})}))


(deftest t22_l204 (is ((fn [t] (pos? (count (:row-maps t)))) v21_l196)))


(def
 v24_l213
 (kind/table
  {:column-names ["Position" "What it does" "Used by"],
   :row-maps
   (for
    [pos [:identity :dodge :stack :fill]]
    {"Position" (kind/code (pr-str pos)),
     "What it does" (pj/position-doc pos),
     "Used by" (used-by :position pos)})}))


(deftest t25_l221 (is ((fn [t] (pos? (count (:row-maps t)))) v24_l213)))


(def
 v27_l238
 (kind/table
  {:column-names ["Key" "Description"],
   :row-maps
   (for
    [k layer-type/universal-mapping-keys]
    {"Key" (kind/code (pr-str k)),
     "Description" (get layer-type/layer-mapping-docs k)})}))


(deftest t28_l245 (is ((fn [t] (pos? (count (:row-maps t)))) v27_l238)))


(def
 v30_l254
 (kind/table
  {:column-names ["Layer type" "Additional keys"],
   :row-maps
   (for
    [k
     layer-type/layer-type-order
     :let
     [m (layer-type/lookup k) accepts (:accepts m)]
     :when
     (seq accepts)]
    {"Layer type" (kind/code (pr-str k)), "Additional keys" accepts})}))


(deftest t31_l264 (is ((fn [t] (pos? (count (:row-maps t)))) v30_l254)))


(def
 v33_l269
 (kind/table
  {:column-names ["Key" "Description"],
   :row-maps
   (for
    [[k desc] (sort-by key layer-type/layer-mapping-docs)]
    {"Key" (kind/code (pr-str k)), "Description" desc})}))


(deftest
 t34_l276
 (is
  ((fn
    [t]
    (let
     [documented
      (set (keys layer-type/layer-mapping-docs))
      in-use
      (into
       (set layer-type/universal-mapping-keys)
       (mapcat :accepts (vals (layer-type/registered))))]
     (and
      (= (count documented) (count (:row-maps t)))
      (empty? (remove documented in-use))
      (empty? (remove in-use documented)))))
   v33_l269)))
