(ns scicloj.plotje.contour-interpolation-test
  "A contour crosses a cell edge where the density passes the level,
   interpolated along the edge whether the density rises or falls
   there. On a grid with one peak, the contour at half the peak's
   height runs halfway between the peak and its neighbours on every
   side."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.extract :as extract]))

(def segments @#'extract/marching-squares-segments)

;; A 3x3 grid, zero but for 1.0 at the centre. With x-lo and y-lo at 0
;; and steps of 1, the grid values sit at 0.5, 1.5 and 2.5 on each axis.
(def peak (double-array [0 0 0
                         0 1 0
                         0 0 0]))

(deftest a-contour-crosses-midway-on-rising-and-falling-edges-test
  (let [pts (for [seg (segments peak 3 0.5 0.0 0.0 1.0 1.0) pt seg] pt)
        xs (map first pts)
        ys (map second pts)]
    (testing "the crossings lie halfway between the peak and its neighbours"
      (is (= 1.0 (apply min xs)))
      (is (= 2.0 (apply max xs)))
      (is (= 1.0 (apply min ys)))
      (is (= 2.0 (apply max ys))))
    (testing "no crossing lands on a grid value"
      (is (not-any? #(or (= 0.5 %) (= 2.5 %)) (concat xs ys))))))

(deftest contour-lines-of-a-density-are-drawn-test
  (let [r (java.util.Random. 7)
        d {:u (vec (repeatedly 300 #(.nextGaussian r)))
           :v (vec (repeatedly 300 #(.nextGaussian r)))}]
    (is (pos? (:lines (pj/svg-summary (pj/lay-contour d :u :v)))))))
