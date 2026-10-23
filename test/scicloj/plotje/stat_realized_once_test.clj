(ns scicloj.plotje.stat-realized-once-test
  "A fitted curve is evaluated once per grid point, however many times
   the plan and the renderer read it."
  (:require [clojure.test :refer [deftest testing is]]
            [fastmath.kernel :as kernel]
            [fastmath.interpolation.acm :as interp]
            [fastmath.ml.regression :as regr]
            [scicloj.plotje.api :as pj]))

(def rows
  {:v (mapv #(Math/sin (* 0.7 %)) (range 60))
   :w (mapv #(+ (* 0.1 %) (Math/cos (* 0.3 %))) (range 60))
   :c (mapv #(if (even? %) "p" "q") (range 60))})

(defn- counting
  "Calls `f` with the three fitting functions wrapped to count each
   evaluation of the function they return. Returns the counts."
  [f]
  (let [n (atom {:kde 0 :loess 0 :lm 0})
        kernel-density kernel/kernel-density
        loess interp/loess
        predict regr/predict]
    (with-redefs [kernel/kernel-density
                  (fn [& args]
                    (update (apply kernel-density args) :kde
                            (fn [kd] (fn [x] (swap! n update :kde inc) (kd x)))))
                  interp/loess
                  (fn [& args]
                    (let [g (apply loess args)]
                      (fn [x] (swap! n update :loess inc) (g x))))
                  regr/predict
                  (fn [& args] (swap! n update :lm inc) (apply predict args))]
      (binding [*out* (java.io.StringWriter.)]
        (f)))
    (deref n)))

(deftest a-curve-is-evaluated-once-per-grid-point
  (testing "a density curve of 512 points"
    (is (= 512 (:kde (counting #(pj/plot (pj/lay-density rows :v)))))))
  (testing "two violins of 80 points"
    (is (= 160 (:kde (counting #(pj/plot (pj/lay-violin rows :c :w)))))))
  (testing "a loess curve of 80 points"
    (is (= 80 (:loess (counting #(pj/plot (pj/lay-smooth rows :v :w)))))))
  (testing "a loess curve and 20 resampled curves, 80 points each"
    (is (= (* 21 80)
           (:loess (counting #(pj/plot (pj/lay-smooth rows :v :w {:confidence-band true
                                                                  :bootstrap-resamples 20})))))))
  (testing "a linear model with a band: 80 points and the two ends of the line"
    (is (= 82 (:lm (counting #(pj/plot (pj/lay-smooth rows :v :w {:stat :linear-model
                                                                  :confidence-band true}))))))))
