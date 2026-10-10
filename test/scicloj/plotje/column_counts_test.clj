(ns scicloj.plotje.column-counts-test
  "The numbers of columns a layer type draws from: declared on the
   registry, read back with `layer-type/column-counts`, and what a pose
   naming one column on `:y` draws."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.layer-type :as layer-type]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]))

(deftest column-counts-of-the-built-in-layer-types-test
  (is (= #{1} (layer-type/column-counts :histogram)))
  (is (= #{1} (layer-type/column-counts :density)))
  (is (= #{1} (layer-type/column-counts :rug)))
  (is (= #{1 2} (layer-type/column-counts :bar)))
  (is (= #{2} (layer-type/column-counts :point)))
  (is (= #{1 2} (layer-type/column-counts :infer)))
  (is (= #{1} (layer-type/column-counts {:mark :bar :stat :bin :x-only true}))))

(deftest register-checks-column-counts-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"not a set of the numbers of columns"
                        (layer-type/register! ::bad {:mark :point :column-counts #{3}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #":x-only true means #\{1\}"
                        (layer-type/register! ::bad {:mark :point :x-only true :column-counts #{2}})))
  (testing "an x-only entry reads as #{1}, and #{1} reads as x-only"
    (try
      (layer-type/register! ::one {:mark ::one :stat :bin :column-counts #{1}})
      (is (true? (:x-only (layer-type/lookup ::one))))
      (is (= #{1} (layer-type/column-counts ::one)))
      (finally
        (swap! @(resolve 'scicloj.plotje.layer-type/registry*) dissoc ::one)))))

(deftest a-pose-naming-only-y-draws-on-its-side-test
  (let [mpg (rdatasets/ggplot2-mpg)]
    (testing "a numeric column: the inferred histogram, as lay-histogram draws it"
      (is (= (pj/svg-summary (-> mpg (pj/pose {:y :cty}) pj/lay-histogram))
             (pj/svg-summary (pj/pose mpg {:y :cty})))))
    (testing "a categorical column: a bar of counts, with or without lay-bar"
      (let [s (pj/svg-summary (pj/pose mpg {:y :class}))]
        (is (= 7 (:polygons s)))
        (is (= s (pj/svg-summary (-> mpg (pj/pose {:y :class}) pj/lay-bar))))))))
