(ns scicloj.plotje.side-on-message-test
  "The message for a one-column layer on a pose naming a column on `:y`
   alone, beside a layer that keeps the pose from being drawn on its
   side: what it suggests has to draw."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def cars
  {:displ [1.8 2.0 2.8 3.1 4.2 5.3]
   :hwy [29 31 26 27 20 19]
   :cty [18 21 16 18 14 13]
   :class ["compact" "compact" "midsize" "suv" "suv" "suv"]})

(defn- message [pose]
  (try (binding [*out* (java.io.StringWriter.)] (pj/plan pose))
       nil
       (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest the-suggestions-of-the-side-on-message-draw-test
  ;; The message suggested (pj/pose data {:x :hwy}) with (pj/coord :flip).
  ;; Following it, the point layer reported that it has no column on :y.
  (let [msg (message (-> cars (pj/pose {:y :hwy}) pj/lay-histogram pj/lay-point))]
    (testing "the message says why the pose is not drawn on its side"
      (is (re-find #"pj/lay-histogram draws from one column" msg))
      (is (re-find #"pj/lay-point does not, so this pose is not drawn on its side" msg)))
    (testing "it suggests the two forms below, and no longer the one that fails"
      (is (re-find #"\(pj/lay-histogram pose :hwy\)" msg))
      (is (re-find #"\(pj/lay-histogram data \{:y :hwy\}\)" msg))
      (is (not (re-find #"pj/coord :flip" msg))))
    (testing "each suggested form draws"
      (is (nil? (message (-> cars (pj/pose {:y :hwy}) (pj/lay-histogram :hwy) pj/lay-point))))
      (is (nil? (message (pj/lay-histogram cars {:y :hwy})))))
    (testing "the form the message used to suggest reports an error"
      (is (re-find #"pj/lay-point draws from two columns"
                   (message (-> cars
                                (pj/pose {:x :hwy})
                                pj/lay-histogram
                                pj/lay-point
                                (pj/coord :flip))))))))
