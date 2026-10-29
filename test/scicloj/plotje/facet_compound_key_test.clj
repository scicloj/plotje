(ns scicloj.plotje.facet-compound-key-test
  "A compound key written in full, {:column [:a :b]}, inside a facet
   over columns draws what the bare vector [:a :b] draws there."
  (:require [clojure.test :refer [deftest is]]
            [scicloj.plotje.api :as pj]))

(def rows
  {:size [1 2 3 4 5 6 7 8]
   :height [2 4 6 8 1 3 5 7]
   :weight [5 6 7 8 1 2 3 4]
   :kind ["a" "a" "b" "b" "a" "a" "b" "b"]
   :year [1 2 1 2 1 2 1 2]})

(defn- panels [facet]
  (-> rows
      (pj/pose {:x :size})
      pj/lay-point
      (pj/facet facet)
      pj/svg-summary
      :panels))

(deftest a-compound-key-in-full-inside-a-facet-over-columns
  (is (= 8 (panels {:col [:kind :year] :row {:y [:height :weight]}})))
  (is (= 8 (panels {:col {:column [:kind :year]} :row {:y [:height :weight]}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Column :nope \(from :col\) not found"
                        (panels {:col {:column [:kind :nope]} :row {:y [:height :weight]}}))))
