(ns scicloj.plotje.domain-warning-test
  "The warning for a :domain that omits a category names what the
   aesthetic orders -- an axis, a legend or the panels -- and no legend
   for an axis."
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]))

(def rows {:kind ["a" "b" "c" "a"] :size [1 2 3 4]})

(deftest a-domain-omitting-a-category-on-an-axis-names-no-legend
  (let [out (with-out-str (pj/plan (-> rows
                                       (pj/lay-bar :kind)
                                       (pj/scale :x {:domain ["c"]}))))]
    (is (str/includes? out "pj/scale :x :domain omits [\"a\" \"b\"]"))
    (is (str/includes? out "List every category to control the whole axis order."))
    (is (not (str/includes? out "legend")))))
