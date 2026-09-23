(ns scicloj.plotje.bare-vector-test
  "What a vector of column names under an aesthetic means.

   On :x or :y a bare vector is a series, pivoted and drawn as layers on
   one panel; on :group, :col or :row it is a compound key. The same two
   readings have written-out forms, {:series [...]} and {:column [...]},
   which draw the same plots."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [tablecloth.api :as tc]))

(def ds
  (tc/dataset {:t    [1 2 3 4]
               :a    [10 20 30 40]
               :b    [5 15 25 35]
               :part ["l" "l" "r" "r"]
               :dim  ["w" "h" "w" "h"]}))

(defn- panels [pose] (count (:panels (pj/plan pose))))

(defn- legend [pose]
  (let [l (:legend (pj/plan pose))]
    [(:title l) (mapv :label (:entries l))]))

(deftest a-bare-vector-by-aesthetic
  (testing "on :y it is a series drawn as layers on one panel"
    (is (= 1 (panels (-> ds (pj/lay-line :t [:a :b])))))
    (is (= [:series ["a" "b"]] (legend (-> ds (pj/lay-line :t [:a :b]))))))
  (testing "on :group it is a compound key"
    (is (= 1 (panels (-> ds (pj/lay-line :t :a {:group [:part :dim]})))))))

(deftest the-written-out-forms-draw-the-same
  (testing "a series"
    (is (= 1 (panels (-> ds (pj/lay-line :t {:series [:a :b]})))))
    (is (= [:series ["a" "b"]]
           (legend (-> ds (pj/lay-line :t {:series [:a :b]}))))))
  (testing "a compound key on :group"
    (is (= 1 (panels (-> ds (pj/lay-line :t :a {:group {:column [:part :dim]}}))))))
  (testing "a compound key in pj/facet -- one panel per combination the data holds"
    (is (= 4 (panels (-> ds
                         (pj/lay-point :t :a)
                         (pj/facet {:column [:part :dim]})))))))

(deftest a-tooltip-vector-is-markup
  (is (= 1 (panels (-> ds (pj/lay-point :t :a {:tooltip [:div "hi"]}))))))

(deftest facet-grid-reports-a-second-facet
  ;; `pj/facet` reported a second facet in the same direction; the grid
  ;; form called no such guard and replaced whatever was there.
  (testing "after an ordinary pj/facet, in either direction"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/facet-grid was given"
         (-> ds (pj/lay-point :t :a) (pj/facet :part) (pj/facet-grid :dim :part))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/facet-grid was given"
         (-> ds (pj/lay-point :t :a) (pj/facet :part :row) (pj/facet-grid :part :dim)))))
  (testing "on a pose that facets by nothing yet"
    (is (= 4 (panels (-> ds (pj/lay-point :t :a) (pj/facet-grid :part :dim)))))))

(deftest a-series-writes-nothing-onto-the-options
  ;; `:opts` is the map a writer wrote with pj/options, so a series
  ;; leaves it alone.
  (testing "a series pose carries no :opts of its own"
    (is (nil? (:opts (-> ds (pj/lay-line :t [:a :b]))))))
  (testing "nor does its draft"
    (is (= {} (:opts (-> ds (pj/lay-line :t [:a :b]) pj/draft))))))
