(ns scicloj.plotje.reversed-domain-breaks-test
  "A domain written high to low draws the axis reversed, and a break
   inside it is not reported as outside it."
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]))

(def data {:a [0 1 2 3] :b [1 2 3 4]})

(deftest a-break-inside-a-reversed-domain-is-not-reported
  (is (str/blank? (with-out-str
                    (pj/plan (-> data
                                 (pj/lay-point :a :b)
                                 (pj/scale :x {:domain [4 -1] :breaks [0 1 2 3]})))))))

(deftest a-break-outside-a-reversed-domain-is-reported
  (is (re-find #"include value\(s\) \[9\] outside"
               (with-out-str
                 (pj/plan (-> data
                              (pj/lay-point :a :b)
                              (pj/scale :x {:domain [4 -1] :breaks [0 9]})))))))
