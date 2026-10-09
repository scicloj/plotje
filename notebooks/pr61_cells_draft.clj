;; # Cells Draft

(ns pr61-cells-draft
  (:require
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]
   ;; Rdatasets -- standard datasets
   [scicloj.metamorph.ml.rdatasets :as rdatasets]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Plotje -- composable plotting
   [scicloj.plotje.api :as pj]))

;; ## Two paths to the same panels, today

(def scatter
  (-> (rdatasets/datasets-iris)
      (pj/pose :sepal-length :sepal-width {:color :species})
      pj/lay-point))

;; ### Categories: `pj/facet`

(def by-facet
  (pj/facet scatter :species))

by-facet

;; ### Columns: `pj/arrange`

(def by-columns
  (pj/arrange scatter [[:sepal-length :sepal-width]
                       [:petal-length :petal-width]]))

by-columns

;; ### Categories listed by hand

(def by-hand
  (pj/arrange
   (vec (for [s ["setosa" "versicolor" "virginica"]]
          (-> (rdatasets/datasets-iris)
              (tc/select-rows #(= s (:species %)))
              (pj/pose :sepal-length :sepal-width {:color :species})
              pj/lay-point
              (pj/options {:title s}))))))

by-hand

;; ### What differs

(kind/table
 (for [[label v] [["pj/facet" by-facet] ["listed by hand" by-hand] ["columns" by-columns]]]
   (let [s (pj/svg-summary v)]
     {:route label
      :panels (:panels s)
      :points (:points s)
      :composite? (contains? v :poses)})))

(kind/test-last
 [(fn [_] (let [f (pj/svg-summary by-facet)
                h (pj/svg-summary by-hand)]
            (and (= 3 (:panels f) (:panels h))
                 (= 150 (:points f) (:points h))
                 ;; One axis title against one per cell.
                 (= 1 (count (filter #{"sepal length"} (:texts f))))
                 (= 3 (count (filter #{"sepal length"} (:texts h))))
                 (not (contains? by-facet :poses))
                 (contains? by-hand :poses))))])

;; ## One expansion for categories and columns

(defn expand
  [pose variation]
  (if (map? variation)
    (let [[k cols] (first variation)]
      (vec (for [c cols]
             (-> pose
                 (pj/pose {k c})))))
    (vec (for [v (distinct ((:data pose) variation))]
           (-> pose
               (update :data tc/select-rows #(= v (get % variation)))
               (pj/options {:title (str v)}))))))

(pj/arrange (expand scatter :species))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:panels s)) (= 150 (:points s)))))])

(pj/arrange (expand scatter {:x [:sepal-length :petal-length :petal-width]}))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:panels s)) (= 450 (:points s)))))])

;; ## What the listed pose would hold

;; Pseudocode:

;; ```clojure
;; {:data iris
;;  :mapping {:x :sepal-length :y :sepal-width :color :species}
;;  :layers [{:layer-type :point}]
;;  :poses [{:where {:species "setosa"}     :label "setosa"}
;;          {:where {:species "versicolor"} :label "versicolor"}
;;          {:where {:species "virginica"}  :label "virginica"}]}
;; ```

;; Pseudocode:

;; ```clojure
;; {:data iris
;;  :mapping {:y :sepal-width :color :species}
;;  :layers [{:layer-type :point}]
;;  :poses [{:mapping {:x :sepal-length} :label "sepal length"}
;;          {:mapping {:x :petal-length} :label "petal length"}
;;          {:mapping {:x :petal-width}  :label "petal width"}]}
;; ```

;; Pseudocode:

;; ```clojure
;; (pj/facet scatter :species)                 ; category cells
;; (pj/facet scatter {:x [:sepal-length :petal-length :petal-width]})
;;                                             ; column cells
;; (pj/facet scatter {:col :species :row :sex}) ; a grid, two variations
;; ```
