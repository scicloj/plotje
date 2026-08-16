(ns plotje-book.scratch
  (:require [scicloj.plotje.api :as pj]
            [tablecloth.api :as tc]))


(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["olive" "tomato" "tomato" "plum"]}
    (pj/pose {:x {:column :A
                  :scale true}
              :y {:column :B
                  :scale true}
              :color {:column :C
                      :scale false}
              :size {:value 10
                     :scale false}}))



(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["olive" "tomato" "tomato" "plum"]}
    (pj/pose {:x {:column :A}
              :y {:column :B}
              :color {:column :C
                      :scale false}
              :size {:value 10
                     :scale false}}))



(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["olive" "tomato" "tomato" "plum"]}
    (pj/pose {:x :A
              :y :B
              :color {:column :C
                      :scale false}
              :size {:value 10
                     :scale false}}))




(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["olive" "tomato" "tomato" "plum"]}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size {:value 10
                     :scale false}}))



(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["olive" "tomato" "tomato" "plum"]}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size 10}))



(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["olive" "tomatoooo" "tomato" "plum"]}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size 10}))






























(-> {:A (range 4)
     :B (reverse (range 4))
     :C ["rep" "dem" "dem" "indep"]}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size 10}))






(-> {:A (range 4)
     :B (reverse (range 4))
     :C (reverse ["rep" "dem" "dem" "indep"])}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size 10}))



#_
(-> {:A (range 4)
     :B (reverse (range 4))
     :C (reverse ["rep" "dem" "dem" "indep"])}
    (pj/pose {:x :A
              :y :B
              :color {:column :C
                      :scale {"rep" "red"
                              "dem" "blue"
                              "indep" "green"}}
              :size 10}))






(-> {:A (range 4)
     :B (reverse (range 4))
     :C (reverse ["rep" "dem" "dem" "indep"])
     :D [10 20 10 20]}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size :D}))


(-> {:A (range 4)
     :B (reverse (range 4))
     :C (reverse ["rep" "dem" "dem" "indep"])
     :D [10 20 10 20]}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size {:column :D
                     :scale (pj/linear-scale ???
                             {:factor 5})}}))


[10 20 10 20]

;; 10 -> 1
;; 20 -> 5

;; 10 -> 1
;; 20 -> 2

;; 10 -> 1
;; 20 -> sqrt(2)
;; (then the area proportion is 2)
























(-> {:A (range 4)
     :B (reverse (range 4))
     :C (reverse ["rep" "dem" "dem" "indep"])}
    (pj/pose {:x :A
              :y :B
              :color :C
              :size 10})
    pj/lay-point
    (pj/lay-label {:x :A
                   :y :B
                   :text :C
                   :offset-x 50}))



(-> {:A (range 4)
     :B (reverse (range 4))
     :C (reverse ["rep" "dem" "dem" "indep"])}
    tc/dataset
    (tc/map-columns :O
                    :A
                    (fn [A]
                      (if (= A 3)
                        -10
                        10))))










