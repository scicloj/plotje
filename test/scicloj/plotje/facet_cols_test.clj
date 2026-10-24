(ns scicloj.plotje.facet-cols-test
  "`:cols` written after a facet: a facet does not wrap its panels, so
   the key is named by the call it was written in, and dropped."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:u [1 2 3 4] :v [1 2 3 4] :g ["a" "b" "c" "d"] :h ["x" "x" "y" "y"]})
(def p (pj/lay-point d :u :v))

(deftest cols-after-a-facet-test
  (testing "pj/facet warns under its own name, names pj/arrange, and draws"
    (let [printed (with-out-str (is (= 4 (:panels (pj/svg-summary (pj/facet p :g {:cols 2}))))))]
      (is (re-find #"pj/facet does not recognize \[:cols\]\. A facet draws its panels in one row or one column" printed))
      (is (re-find #"pj/arrange" printed))))
  (testing "pj/facet-grid says what its columns are"
    (is (re-find #"pj/facet-grid does not recognize \[:cols\]\. A grid has a column of panels for each value of its first column"
                 (with-out-str (pj/facet-grid p :g :h {:cols 2})))))
  (testing "the other plot options are still taken"
    (is (= "T" (get-in (binding [*out* (java.io.StringWriter.)] (pj/facet p :g {:cols 2 :title "T"}))
                       [:opts :title]))))
  (testing "under :strict it reports an error"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"pj/facet does not recognize \[:cols\]"
                          (pj/with-config {:strict true} (pj/facet p :g {:cols 2}))))))
