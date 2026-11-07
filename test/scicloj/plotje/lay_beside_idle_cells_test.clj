(ns scicloj.plotje.lay-beside-idle-cells-test
  "A `lay-*` naming columns, added to cells that name no column and hold
   no layer -- a split written as filters -- is drawn in every cell, as
   it is drawn in every panel of the facet those cells split."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:u [1 2 3 4] :v [2 3 1 4] :g ["a" "b" "a" "b"]})
(def cells [{:where {:g "a"}} {:where {:g "b"}}])

(deftest a-layer-on-cells-that-name-no-column-is-drawn-in-each-test
  (testing "drawn in every cell, as the facet draws it, with no Note"
    (let [printed (with-out-str
                    (is (= (pj/plot (-> d (pj/arrange cells) (pj/lay-point :u :v)))
                           (pj/plot (-> d (pj/lay-point :u :v) (pj/facet :g))))))]
      (is (= "" printed))))
  (testing "no cell is added"
    (is (= 2 (:panels (pj/svg-summary (-> d (pj/arrange cells) (pj/lay-point :u :v)))))))
  (testing "no Note where every cell names its columns"
    (is (= "" (with-out-str (-> d (pj/arrange [{:x :u :y :v}]) (pj/lay-point :v :u)))))))
