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

(deftest a-set-valued-filter-labels-its-strip-by-its-members-test
  ;; The strip of a cell filtered by a set printed the set, `#{"b" "a"}`.
  (let [strips (fn [cells]
                 (-> (pj/lay-point d :x :y)
                     (assoc :layout {:direction :horizontal} :poses cells)
                     pj/draft
                     (get-in [:chrome-spec :col-labels])))]
    (testing "its members in the order the data holds them, joined by a comma"
      (is (= ["a, b" "c"] (strips [{:where {:g #{"b" "a"}}} {:where {:g "c"}}])))
      (is (= ["b, c" "a"] (strips [{:where {:g #{"c" "b"}}} {:where {:g "a"}}]))))
    (testing "numbers are formatted as a single value is"
      (is (= ["1.0, 4.0" "2.0"] (strips [{:where {:x #{4.0 1.0}}} {:where {:x 2.0}}]))))
    (testing "a single value is labelled as before"
      (is (= ["b" "a" "c"] (strips [{:where {:g "b"}} {:where {:g "a"}} {:where {:g "c"}}]))))))

(deftest a-cell-of-pj-arrange-may-hold-a-filter-test
  (let [base (pj/lay-point d :x :y)
        cells [{:where {:g "a"}} {:where {:g "b"}} {:where {:g "c"}}]]
    (testing "cells holding only a filter draw what pj/facet draws"
      (is (= (pj/plot (pj/facet base :g)) (pj/plot (pj/arrange base cells))))
      (is (= (pj/plot (pj/facet base :g))
             (pj/plot (-> d (pj/pose {:x :x :y :y}) (pj/arrange cells) pj/lay-point)))))
    (testing "a filter beside a mapping"
      (is (= {:panels 2 :points 3}
             (select-keys (pj/svg-summary (-> d
                                              (pj/arrange [{:x :x :y :y :where {:g "a"}}
                                                           {:x :y :y :x :where {:g "b"}}])
                                              pj/lay-point))
                          [:panels :points]))))
    (testing "the filter is checked against the data"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"A row filter names \[:nope\]"
                            (pj/plan (pj/arrange base [{:where {:nope 1}}])))))
    (testing "a map holding a filter and a key no mapping takes is still not a cell"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"is a map but not a pose"
                            (pj/arrange base [{:where {:g "a"} :bogus 1}]))))))
