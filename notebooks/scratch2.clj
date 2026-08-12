(ns scratch2
  (:require [scicloj.plotje.api :as pj]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.api :as tc]))


(-> {:x [1 2 3 4]
     :y [1 2 1 2]
     :c [:a-b :a_b :a-b :a_b]}
    (pj/lay-bar :x
                :y
                {:color :c}))

