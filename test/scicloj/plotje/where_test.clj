(ns scicloj.plotje.where-test
  "A written row filter on a pose, `:where` -- the provisional spelling
   of the filter a cell generated from a category carries."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:x [1.0 2.0 3.0 4.0] :y [1.0 2.0 3.0 4.0] :g ["a" "b" "a" "c"]})

(deftest a-leaf-filter-keeps-the-matching-rows-test
  (is (= 2 (:points (pj/svg-summary (-> d (pj/lay-point :x :y) (assoc :where {:g "a"}))))))
  (testing "a set is any of its values"
    (is (= 3 (:points (pj/svg-summary (-> d (pj/lay-point :x :y) (assoc :where {:g #{"a" "b"}})))))))
  (testing "several keys must all match"
    (is (= 1 (:points (pj/svg-summary (-> d (pj/lay-point :x :y) (assoc :where {:g "a" :x 3.0}))))))))

(deftest listed-filtered-cells-draw-the-facet-marks-test
  (let [base (pj/lay-point d :x :y)
        listed (assoc base :layout {:direction :horizontal}
                      :poses [{:where {:g "a"}} {:where {:g "b"}} {:where {:g "c"}}])
        faceted (pj/facet base :g)]
    (is (= (select-keys (pj/svg-summary faceted) [:panels :points])
           (select-keys (pj/svg-summary listed) [:panels :points])))))

(deftest a-filter-keeping-no-rows-is-an-empty-cell-test
  (let [p (assoc (pj/lay-point d :x :y) :layout {:direction :horizontal}
                 :poses [{:where {:g "a"}} {:where {:g "none"}}])]
    (is (= {:panels 1 :points 2} (select-keys (pj/svg-summary p) [:panels :points])))))

(deftest a-filter-on-a-missing-column-is-reported-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"A row filter names \[:nope\]"
                        (pj/plan (assoc (pj/lay-point d :x :y) :where {:nope 1})))))

(deftest numbers-compare-by-value-test
  ;; 3 selects a row holding 3.0, as it would in a comparison.
  (is (= 1 (:points (pj/svg-summary (-> d (pj/lay-point :x :y) (assoc :where {:x 3}))))))
  (is (= 2 (:points (pj/svg-summary (-> d (pj/lay-point :x :y) (assoc :where {:x #{1 4}})))))))

(deftest a-value-the-data-does-not-hold-is-reported-test
  ;; A misspelled category selects nothing; the warning names it.
  (let [out (with-out-str
              (pj/plan (-> d (pj/lay-point :x :y) (assoc :where {:g #{"a" "zz"}}))))]
    (is (re-find #"Warning: A row filter names \"zz\" on :g -- a value the data does not hold" out)))
  (testing "a cell generated from the data names none"
    (is (= "" (with-out-str (pj/plan (pj/facet (pj/lay-point d :x :y) :g)))))))

(deftest an-empty-set-warns-test
  (let [out (with-out-str
              (is (zero? (:points (pj/svg-summary (-> d (pj/lay-point :x :y) (assoc :where {:g #{}})))))))]
    (is (re-find #"an empty set on :g" out))))

(deftest listed-cells-match-numbers-by-value-test
  (let [base (pj/lay-point {:x [1 2 3 4] :y [1 2 3 4] :n [1 2 1 2]} :x :y)]
    (is (= (pj/plot (pj/facet base :n))
           (pj/plot (assoc base :layout {:direction :horizontal}
                           :poses [{:where {:n 1.0}} {:where {:n 2.0}}]))))))
