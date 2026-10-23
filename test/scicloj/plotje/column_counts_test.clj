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

(deftest an-x-is-filled-in-only-for-a-layer-that-needs-two-columns-test
  (let [animals {:animal ["cat" "dog" "bird" "fish"] :count [12 8 15 5]}]
    (testing "no layer: the column on its side, on data of two columns too"
      (let [plan (pj/plan (pj/pose animals {:y :animal}))]
        (is (= "animal" (:y-label plan)))
        (is (= :rect (-> plan :panels first :layers first :mark)))))
    (testing "a scatter takes the first other column as its x"
      (let [plan (pj/plan (pj/lay-point animals {:y :count}))]
        (is (= ["animal" "count"] [(:x-label plan) (:y-label plan)]))))))

(deftest a-column-on-y-alone-and-a-specified-coord-test
  ;; Drawn on its side is the one drawing such a pose has. `:flip`
  ;; specified says what is inferred; any other coordinate system
  ;; reports an error, where it failed with a NullPointerException.
  (let [side (fn [coord]
               (cond-> (-> {:v [1.0 2.0 2.0 3.0 5.0]} (pj/pose {:y :v}) pj/lay-histogram)
                 coord (pj/coord coord)))]
    (is (= (pj/plot (side nil)) (pj/plot (side :flip))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":coord :cartesian has no drawing"
                          (pj/plan (side :cartesian))))))

(deftest the-type-of-a-column-on-y-alone-moves-with-the-column-test
  ;; A counting bar whose numerical column is named on `:y` read its
  ;; type from `:x-type`: `{:y :cyl :y-type :categorical}` reported that
  ;; `:cyl` is numerical, and the message named `:x`.
  (let [d {:cyl [4 4 6 8 8 8]}
        domains (fn [pose] (let [p (first (:panels (pj/plan pose)))]
                             [(:x-domain p) (:y-domain p)]))
        typed-on-x (domains (pj/lay-bar d {:y :cyl :x-type :categorical}))]
    (testing "`:y-type` beside a column on `:y` is that column's type"
      (is (= ["4" "6" "8"] (second (domains (pj/lay-bar d {:y :cyl :y-type :categorical})))))
      (is (= typed-on-x (domains (pj/lay-bar d {:y :cyl :y-type :categorical}))))
      (is (= typed-on-x (domains (-> d (pj/pose {:y :cyl :y-type :categorical}) pj/lay-bar)))))
    (testing "the same bars as the column on `:x`, flipped"
      (is (= (reverse typed-on-x)
             (domains (pj/lay-bar d {:x :cyl :x-type :categorical})))))
    (testing "with no type, the message names both spellings and no axis"
      (let [msg (try (pj/plan (pj/lay-bar d {:y :cyl})) nil
                     (catch clojure.lang.ExceptionInfo e (ex-message e)))]
        (is (re-find #"requires a categorical column, but :cyl is numerical" msg))
        (is (re-find #"\{:y-type :categorical\} for a column on :y" msg))
        (is (not (re-find #"column for :x" msg)))))))
