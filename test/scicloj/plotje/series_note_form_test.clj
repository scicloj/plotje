(ns scicloj.plotje.series-note-form-test
  "The Note about columns of different size on one axis suggests a facet
   that draws: for a series, the facet is written in place of the series."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]))

(def rows
  {:quarter ["q1" "q2" "q3" "q4"] :revenue [10 12 14 13] :cost [7 8 9 9]
   :units [100 120 90 95]})

(deftest the-note-for-a-series-says-the-facet-replaces-it
  (let [note (with-out-str (pj/plan (pj/lay-line rows :quarter [:revenue :cost :units])))]
    (is (str/includes? note "Note: the :y axis holds :revenue"))
    (is (str/includes? note "name the columns in a facet in place of the series -- (pj/facet pose {:y [:revenue :cost :units]}), where no layer of the pose names a column on :y."))
    (testing "the form the Note describes"
      (is (= 3 (:panels (pj/svg-summary (-> rows
                                            (pj/pose {:x :quarter})
                                            pj/lay-line
                                            (pj/facet {:y [:revenue :cost :units]})))))))))

(deftest the-note-for-layers-naming-columns-keeps-the-plain-form
  (let [pose (-> rows (pj/lay-line :quarter :revenue) (pj/lay-line :quarter :units))
        note (with-out-str (pj/plan pose))]
    (is (str/includes? note "draw a panel per column: (pj/facet pose {:y [:revenue :units]})."))
    (testing "the form, added to that pose"
      (is (= 2 (:panels (binding [*out* (java.io.StringWriter.)]
                          (pj/svg-summary (pj/facet pose {:y [:revenue :units]})))))))))
