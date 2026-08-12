(ns scratch3
  (:require [scicloj.plotje.api :as pj]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.api :as tc]
            [clojure.string :as str]))

(def stations
  (-> "/workspace/Downloads/קווי אוטובוסים בתחנות שונות.csv"
      (tc/dataset {:key-fn keyword})
      (tc/map-columns :associated_routes
                      :associated_routes
                      (fn [strings]
                        (-> strings
                            (some-> (str/split #","))
                            (->> (map #(Integer/parseInt %)))
                            set)))))


;; (def savidor-routes
;;   (-> stations
;;       (tc/select-rows #(-> % :stop_code (= 20349)))
;;       :associated_routes
;;       first))

;; (def savidor-routes
;;   (-> stations
;;       (tc/select-rows #(-> % :stop_code (= 20349)))
;;       :associated_routes
;;       first))

;; (-> {:route savidor-routes}
;;     tc/dataset
;;     (tc/map-columns "number of stations"
;;                     :route
;;                     (fn [route]
;;                       (-> stations
;;                           (tc/select-rows (fn [row]
;;                                             ((:associated_routes row) route)))
;;                           tc/row-count)))
;;     (tc/group-by ["number of stations"])
;;     (tc/aggregate {"number of savidor routes" tc/row-count})
;;     (tc/order-by ["number of stations"]))

;; (count savidor-routes)


;; (-> {:route savidor-routes}
;;     tc/dataset
;;     (tc/map-columns :n-stations
;;                     :route
;;                     (fn [route]
;;                       (-> stations
;;                           (tc/select-rows (fn [row]
;;                                             ((:associated_routes row) route)))
;;                           tc/row-count))))


;; (-> stations
;;     (tc/select-rows
;;      (fn [row]
;;        ((:associated_routes row) 72))))


;; (-> stations
;;     (tc/select-rows
;;      (fn [row]
;;        ((:associated_routes row) 159))))


;; (-> {:route savidor-routes}
;;     tc/dataset
;;     (tc/map-columns :stops
;;                     :route
;;                     (fn [route]
;;                       (-> stations
;;                           (tc/select-rows (fn [row]
;;                                             ((:associated_routes row) route)))
;;                           :stop_code
;;                           sort)))
;;     (tc/map-columns :n-stops
;;                     :stops
;;                     count)
;;     (tc/order-by :n-stops :desc)
;;     (tech.v3.dataset.print/print-range :all))


;; (-> {:route savidor-routes}
;;     tc/dataset
;;     (tc/map-columns :stops
;;                     :route
;;                     (fn [route]
;;                       (-> stations
;;                           (tc/select-rows (fn [row]
;;                                             ((:associated_routes row) route)))
;;                           :stop_code
;;                           sort)))
;;     (tc/map-columns :n-stops
;;                     :stops
;;                     count)
;;     (tc/order-by :n-stops :desc)
;;     (tech.v3.dataset.print/print-range :all)
;;     (tc/write-csv! "/workspace/Downloads/Savidor-routes.csv"))



(for [main-stop [20349
                 20013
                 2321]]
  [main-stop
   (-> {:route (-> stations
                   (tc/select-rows #(-> % :stop_code (= main-stop)))
                   :associated_routes
                   first)}
       tc/dataset
       (tc/map-columns :stops
                       :route
                       (fn [route]
                         (-> stations
                             (tc/select-rows (fn [row]
                                               ((:associated_routes row) route)))
                             :stop_code
                             sort)))
       (tc/select-rows #(-> % :stops count (> 1)))
       :route
       vec
       sort)])

