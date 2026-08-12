(ns scratch
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.plotje.api :as pj]
            [tablecloth.api :as tc]))

(-> (tc/dataset {:fitted [1 2 3] :residual [1 2 3]})
    (pj/lay-point :fitted :residual)
    (pj/lay-text :x :y 
                 {:text :text
                  :data (tc/dataset {:x [1 2 3]
                                     :y [1 2 3]
                                     :text [:a :b :c]})}))


(-> (tc/dataset {:fitted [1 2 3] :residual [1 2 3]})
    (pj/lay-point :fitted :residual)
    (pj/lay-text {:text :text
                  :data (-> (tc/dataset {:x [1 2 3]
                                         :y [1 2 3]
                                         :text [:a :b :c]})
                            (tc/rename-columns
                             {:x :fitted
                              :y :residual}))}))





(-> (tc/dataset {:fitted [1 2 3] :residual [1 2 3]})
    (pj/lay-point :fitted :residual)
    (pj/lay-text {:data (tc/dataset {:x [1 2 3]
                                     :y [1 2 3]
                                     :text [:a :b :c]})}))



{:layers [{:layer-type :point} {:layer-type :text, :data _unnamed [3 3]:

                                | :x | :y | :text |
                                |---:|---:|-------|
                                |  1 |  1 |    :a |
                                |  2 |  2 |    :b |
                                |  3 |  3 |    :c |
                                }], :data _unnamed [3 2]:

 | :fitted | :residual |
 |--------:|----------:|
 |       1 |         1 |
 |       2 |         2 |
 |       3 |         3 |
 , :mapping {:x :fitted, :y :residual}}
