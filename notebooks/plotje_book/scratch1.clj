(ns plotje-book.scratch1
  (:require [scicloj.plotje.api :as pj]
            [scicloj.kindly.v4.kind :as kind]))

(def pose1
  (pj/pose nil {:x :A
                :y :B
                :size 5}))

pose1

;; => {:mapping {:x :A, :y :B, :size 5}, :layers []}


(pj/lay-point pose1
              {:A [-3 -2 -1 0 1 2 3]
               :B [3 2 1 0 1 2 3]})

