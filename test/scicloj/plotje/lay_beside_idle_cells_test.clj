(ns scicloj.plotje.lay-beside-idle-cells-test
  "A `lay-*` naming columns no cell of a composite draws is drawn on a
   cell of its own. Cells that name no column and hold no layer -- a
   split written as filters -- then draw nothing, and a Note says so."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:u [1 2 3 4] :v [2 3 1 4] :g ["a" "b" "a" "b"]})
(def cells [{:where {:g "a"}} {:where {:g "b"}}])

(deftest a-layer-beside-cells-that-draw-nothing-is-noted-test
  (testing "the Note names the cells and the spelling that reaches them"
    (let [printed (with-out-str (-> d (pj/arrange cells) (pj/lay-point :u :v)))]
      (is (re-find #"2 cells, which name no column and hold no layer, draw nothing" printed))
      (is (re-find #"\(-> pose \(pj/pose :u :v\) pj/lay-point\)" printed))))
  (testing "the spelling it names draws the layer in every cell"
    (is (= (pj/plot (-> d (pj/arrange cells) (pj/pose :u :v) pj/lay-point))
           (pj/plot (-> d (pj/lay-point :u :v) (pj/facet :g))))))
  (testing "no Note where every cell names its columns"
    (is (= "" (with-out-str (-> d (pj/arrange [{:x :u :y :v}]) (pj/lay-point :v :u)))))))
