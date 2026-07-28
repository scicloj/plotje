(ns scicloj.plotje.density-test
  "A density curve used to be estimated on a grid running half the data
   span past the values on either side, and that grid was both drawn and
   reported as the axis range -- so the plot came out far wider than the
   data it described, and a rug drawn alongside covered only the middle
   of the axis. The curve is now estimated across the observed values and
   reports them, so it ends where the data ends. Regression guard for
   https://github.com/scicloj/plotje/issues/23.

   Two things need asserting and only one of them is the domain. An
   interim fix clamped the reported domain while still estimating past
   the data and clipping the surplus at the panel edge; every
   domain assertion passed, and the drawn curve still overran the last
   observation by the width of the axis padding. The curve-extent tests
   below are the ones that pin what a reader actually sees."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

;; Values spread over 1..19, well away from the origin, so an estimate
;; running past the data would be easy to tell apart from one that does
;; not.
(def spread-data
  {:v (vec (mapcat (fn [i] (repeat 10 (double i))) (range 1 20)))})

(def grouped-data
  (assoc spread-data
         :g (vec (mapcat (fn [i] (repeat 10 (if (< i 10) "a" "b")))
                         (range 1 20)))))

(defn x-domain [pose]
  (-> pose pj/plan :panels first :x-domain))

(defn curve-extent
  "Lowest and highest x the density curve is actually drawn at, across
   every group -- as distinct from the axis range the panel reports."
  [pose]
  (let [xs (mapcat :xs (-> pose pj/plan :panels first :layers first :groups))]
    [(apply min xs) (apply max xs)]))

(defn per-group-extents
  "Each group's own drawn extent, in group order."
  [pose]
  (mapv (fn [g] [(apply min (:xs g)) (apply max (:xs g))])
        (-> pose pj/plan :panels first :layers first :groups)))

(defn violin-extent
  "Lowest and highest position a violin or ridgeline body is drawn at,
   across every category. The two marks keep their bodies in different
   plan slots -- `:violins` and `:ridges`."
  [pose]
  (let [layer (-> pose pj/plan :panels first :layers first)
        ys (mapcat :ys (or (:violins layer) (:ridges layer)))]
    [(apply min ys) (apply max ys)]))

(defn round4
  "Round to four decimals so an expected value can be written exactly
   without tripping over floating-point representation."
  [x]
  (/ (Math/round (* 1.0e4 (double x))) 1.0e4))

;; Data 1..19, plus the standard 5% :domain-padding on each side of the
;; span.
(def expected-data-domain [0.1 19.9])

(deftest density-domain-follows-the-data
  (testing "the reported range covers the data"
    (is (= expected-data-domain
           (mapv round4 (x-domain (-> spread-data (pj/lay-density :v)))))))

  (testing "a rug and a density agree on the axis, exactly"
    (is (= (x-domain (-> spread-data (pj/lay-rug :v)))
           (x-domain (-> spread-data (pj/lay-density :v))))))

  (testing "grouped curves report the range of all the data"
    (is (= expected-data-domain
           (mapv round4 (x-domain (-> grouped-data (pj/lay-density :v {:color :g}))))))))

(deftest curve-is-drawn-only-across-the-data
  ;; The axis carries 5% padding the curve must NOT fill: estimating past
  ;; the data and clipping the surplus made the fill reach the panel edge,
  ;; so the plot read as a distribution continuing out of view. Measured
  ;; on rendered output before this was fixed, the fill ran 25px past the
  ;; outermost rug tick on each side of a 600px-wide plot.
  (testing "the curve spans exactly the observed values"
    (is (= [1.0 19.0] (mapv round4 (curve-extent (-> spread-data (pj/lay-density :v)))))))

  (testing "the curve stops short of the axis on both sides"
    (let [[a-lo a-hi] (x-domain (-> spread-data (pj/lay-density :v)))
          [c-lo c-hi] (curve-extent (-> spread-data (pj/lay-density :v)))]
      (is (> c-lo a-lo))
      (is (< c-hi a-hi))))

  (testing "a rug and a density start and end together"
    (is (= (mapv round4 (x-domain (-> spread-data (pj/lay-rug :v))))
           (mapv round4 (x-domain (-> spread-data (pj/lay-density :v))))))
    (is (= [1.0 19.0]
           (mapv round4 (curve-extent (-> spread-data (pj/lay-density :v))))))))

(deftest each-group-is-estimated-over-its-own-values
  ;; This is ggplot2's `geom_density(trim = TRUE)`, NOT its default --
  ;; ggplot2 estimates every group across the whole scale, so all its
  ;; curves span the full range. Exposing that choice is a backlog item
  ;; (`trim`); this test pins which side of it we are currently on, so
  ;; adding the option cannot change the behaviour by accident.
  (testing "each group's curve spans only that group's values"
    (is (= [[1.0 9.0] [10.0 19.0]]
           (mapv #(mapv round4 %)
                 (per-group-extents (-> grouped-data (pj/lay-density :v {:color :g}))))))))

(deftest violins-are-trimmed-to-their-category
  ;; lay-violin and lay-ridgeline estimate through the same KDE. Before
  ;; trimming, each violin grew a long needle tail past its category's
  ;; values. ggplot2 trims violins by default (geom_violin(trim = TRUE)).
  (testing "violin bodies span only the observed values"
    (is (= [1.0 19.0]
           (mapv round4 (violin-extent (-> grouped-data (pj/lay-violin :g :v)))))))

  (testing "ridgeline follows the same rule"
    (is (= [1.0 19.0]
           (mapv round4 (violin-extent (-> grouped-data (pj/lay-ridgeline :g :v))))))))

(deftest density-still-renders
  (testing "bounding the estimate does not drop the curve"
    (let [s (pj/svg-summary (-> spread-data (pj/lay-density :v)))]
      (is (= 1 (:panels s)))
      (is (= 1 (:polygons s)))
      (is (= 1 (:clips s))))))

(deftest degenerate-columns-keep-their-domain
  (testing "a constant column falls back to a unit window around the value"
    (is (= [4.0 6.0]
           (mapv round4 (x-domain (-> {:v (vec (repeat 20 5.0))}
                                      (pj/lay-density :v))))))))
