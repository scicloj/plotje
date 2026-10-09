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
   [scicloj.plotje.api :as pj]
   ;; The provisional expansion, internal while its publishing is open
   [scicloj.plotje.impl.pose :as pose]))

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

(def expand pose/expand-variation)

(expand scatter :species)

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:panels s)) (= 150 (:points s)))))])

(-> (expand scatter :species)
    (update :poses #(mapv (fn [c] (dissoc c :opts)) %))
    (dissoc :data :layers)
    kind/pprint)

(expand scatter {:x [:sepal-length :petal-length :petal-width]})

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 3 (:panels s)) (= 450 (:points s)))))])

;; ### A grid of two categories

(def mpg-points (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy))

(pj/facet-grid mpg-points :drv :cyl)

(expand mpg-points {:col :drv :row :cyl})

(kind/test-last
 [(fn [v] (= (:points (pj/svg-summary v))
             (:points (pj/svg-summary (pj/facet-grid mpg-points :drv :cyl)))))])

;; ## Category cells as written filters

(def listed-with-filters
  (-> scatter
      (assoc :layout {:direction :horizontal}
             :share-scales #{:x :y}
             :poses [{:where {:species "setosa"}}
                     {:where {:species "versicolor"}}
                     {:where {:species "virginica"}}])))

listed-with-filters

(kind/test-last
 [(fn [v] (let [titles (fn [p] (frequencies (filter #{"sepal length" "sepal width"}
                                                   (:texts (pj/svg-summary p)))))]
            (and (= (select-keys (pj/svg-summary v) [:panels :points])
                    (select-keys (pj/svg-summary by-facet) [:panels :points]))
                 (= (titles by-facet) (titles v)))))])

;; ### A filter that keeps no rows is an empty cell

(-> scatter
    (assoc :layout {:direction :horizontal}
           :poses [{:where {:species "setosa"}}
                   {:where {:species "no such species"}}
                   {:where {:species #{"versicolor" "virginica"}}}]))

(kind/test-last
 [(fn [v] (let [s (pj/svg-summary v)]
            (and (= 2 (:panels s)) (= 150 (:points s)))))])

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
