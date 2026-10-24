(ns scicloj.plotje.facet-layer-missing-note-test
  "Under a facet over columns, a layer naming one of the facet's columns
   is drawn in that column's panel only. Where another panel is drawn by
   other layers, none of the first layer's type, a Note says so: the
   panel showed a smooth without its points, or a rule alone, with no
   message."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:t [1 2 3 4 5 6] :a [1 3 2 5 4 6] :b [10 30 20 50 40 60]})

(defn- printed [pose]
  (with-out-str (pj/plan pose)))

(deftest a-layer-left-out-of-a-drawn-panel-test
  (testing "a scatter beside a smooth naming only :x"
    (let [out (printed (-> d
                           (pj/lay-point :t :a)
                           (pj/lay-smooth {:x :t})
                           (pj/facet {:y [:a :b]})))]
      (is (re-find #"Note: pj/lay-point names :a on :y, so it is drawn in the panel of that column only" out))
      (is (re-find #"The panel \{:y :b\} draws only pj/lay-smooth" out))
      (is (re-find #"leave :y off the layer and name :x on the pose" out))
      (is (= 1 (count (re-seq #"Note:" out))))))
  (testing "no Note where every panel draws the layer's type"
    (is (= "" (printed (-> d
                           (pj/lay-point :t :a)
                           (pj/lay-point :t :b)
                           (pj/facet {:y [:a :b]})))))
    (is (= "" (printed (-> d
                           (pj/pose {:x :t})
                           pj/lay-point
                           (pj/lay-smooth {:x :t})
                           (pj/facet {:y [:a :b]})))))
    (is (= "" (printed (-> d
                           (pj/lay-point :t :a)
                           (pj/facet {:y [:a :b]})))))))
