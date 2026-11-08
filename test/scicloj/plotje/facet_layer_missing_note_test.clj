(ns scicloj.plotje.facet-layer-missing-note-test
  "Under a facet over columns, a layer naming one of the facet's columns
   is drawn in that column's panel only, and the other panels draw their
   other layers. Nothing is printed for it, as on a composite pose: the
   layer's columns say where it is drawn."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:t [1 2 3 4 5 6] :a [1 3 2 5 4 6] :b [10 30 20 50 40 60]})

(defn- printed [pose]
  (with-out-str (pj/plan pose)))

(defn- marks [pose]
  (mapv #(mapv :mark (-> % :plan :panels first :layers))
        (:sub-plots (pj/plan pose))))

(deftest a-layer-left-out-of-a-drawn-panel-test
  (testing "a scatter beside a smooth naming only :x"
    (let [pose (-> d
                   (pj/lay-point :t :a)
                   (pj/lay-smooth {:x :t})
                   (pj/facet {:y [:a :b]}))]
      (is (= [[:point :line] [:line]] (marks pose)))
      (is (= "" (printed pose)))))
  (testing "every panel draws the layer's type"
    (is (= "" (printed (-> d
                           (pj/lay-point :t :a)
                           (pj/lay-point :t :b)
                           (pj/facet {:y [:a :b]})))))
    (is (= "" (printed (-> d
                           (pj/pose {:x :t})
                           pj/lay-point
                           (pj/lay-smooth {:x :t})
                           (pj/facet {:y [:a :b]})))))))
