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
