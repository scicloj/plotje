(ns scicloj.plotje.top-bottom-legend-test
  "A shape, size or alpha legend at `:legend-position :top` or
   `:bottom` is drawn, each in a row of its own under the colour
   legend, below both rows of a gradient. Only the colour or shape
   legend was drawn there, and nothing said the others were left out."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def data {:a [1 2 3 4] :b [4 3 2 1] :s [10 20 30 40] :w [0.1 0.4 0.7 1.0]
           :g ["p" "q" "p" "q"] :k ["u" "u" "v" "v"]})

(defn- texts [legend-position mapping]
  (set (:texts (pj/svg-summary
                (-> data
                    (pj/lay-point :a :b mapping)
                    (pj/options {:legend-position legend-position}))))))

(deftest size-and-alpha-legends-at-top-and-bottom-test
  (doseq [pos [:top :bottom :right]]
    (testing (str pos)
      (let [t (texts pos {:color :g :size :s :alpha :w})]
        (is (contains? t "g"))
        (is (contains? t "s"))
        (is (contains? t "w")))
      (testing "a shape legend beside a colour legend"
        (let [t (texts pos {:color :g :shape :k :size :s})]
          (is (every? t ["g" "p" "k" "u" "v" "s"]))))
      (testing "a shape legend beside a colour gradient"
        (let [t (texts pos {:color :s :shape :k})]
          (is (every? t ["s" "k" "u" "v"]))))
      (testing "without a colour legend"
        (let [t (texts pos {:size :s})]
          (is (contains? t "s")))))))
