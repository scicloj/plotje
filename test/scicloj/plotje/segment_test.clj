(ns scicloj.plotje.segment-test
  "`pj/lay-segment` draws a straight line per row from (x, y) to
   (x-end, y-end). Each end is a column or a written value, and an end
   left out keeps the start's value -- which is what makes a stem plot
   one call (#50) and an annotation arrow one call (#17)."
  (:require [clojure.test :refer [deftest is testing]]
            [java-time.api :as jt]
            [scicloj.plotje.api :as pj]))

(def pts {:x0 [1 2 4] :y0 [1 3 2] :x1 [3 3 5] :y1 [2 1 4]})

(defn- segments
  "The drawn segments' endpoints, per plan group, as the extract leaves
   them."
  [pose]
  (->> (pj/plan pose) :panels first :layers
       (filter #(= :segment (:mark %)))
       (mapcat :groups)
       (mapcat (fn [{:keys [xs ys x-ends y-ends]}]
                 (map (fn [a b c d] [a b c d]) xs ys x-ends y-ends)))
       (mapv #(mapv (fn [v] (if (number? v) (double v) v)) %))))

(deftest a-segment-per-row-test
  (testing "both ends from columns"
    (is (= [[1.0 1.0 3.0 2.0] [2.0 3.0 3.0 1.0] [4.0 2.0 5.0 4.0]]
           (segments (pj/lay-segment pts :x0 :y0 {:x-end :x1 :y-end :y1}))))
    (is (= 3 (:lines (pj/svg-summary (pj/lay-segment pts :x0 :y0 {:x-end :x1 :y-end :y1}))))))

  (testing "an end left out keeps the start's value"
    (is (= [[1.0 1.0 1.0 0.0] [2.0 3.0 2.0 0.0] [4.0 2.0 4.0 0.0]]
           (segments (pj/lay-segment pts :x0 :y0 {:y-end 0})))
        "{:y-end 0} draws vertical stems to the zero line")
    (is (= [[1.0 1.0 3.0 1.0] [2.0 3.0 3.0 3.0] [4.0 2.0 5.0 2.0]]
           (segments (pj/lay-segment pts :x0 :y0 {:x-end :x1})))
        "{:x-end :x1} draws horizontal segments")))

(deftest the-domain-reaches-the-ends-test
  ;; A mark is on the panel only where its stat reports the extent it
  ;; covers; an end outside the starts' extent would otherwise be
  ;; clipped away.
  (let [plan (pj/plan (pj/lay-segment {:i [1 2 3] :r [2 3 4]} :i :r {:y-end 0}))
        [lo hi] (:y-domain (first (:panels plan)))]
    (is (<= lo 0 4 hi)))
  (let [[lo hi] (:x-domain (first (:panels (pj/plan (pj/lay-segment pts :x0 :y0 {:x-end :x1})))))]
    (is (<= lo 1 5 hi))))

(deftest a-stem-plot-over-a-numeric-index-test
  ;; The shape #50 asks for: no dot, a numeric x.
  (let [s (pj/svg-summary (pj/lay-segment {:index (range 1 151)
                                           :d (map #(mod (* 7 %) 11) (range 1 151))}
                                          :index :d {:y-end 0}))]
    (is (= 150 (:lines s)))
    (is (zero? (:points s)))))

(deftest arrow-heads-test
  (let [heads (fn [arrow] (:polygons (pj/svg-summary
                                      (pj/lay-segment pts :x0 :y0 {:x-end :x1 :y-end :y1
                                                                   :arrow arrow}))))]
    (is (= 0 (heads false)))
    (is (= 3 (heads :end)))
    (is (= 3 (heads true)))
    (is (= 3 (heads :start)))
    (is (= 6 (heads :both))))
  (testing "an arrow value it does not read is reported"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":arrow :tip is not one of"
                          (pj/plot (pj/lay-segment pts :x0 :y0 {:y-end 0 :arrow :tip}))))))

(deftest written-ends-on-a-categorical-axis-test
  ;; The #17 annotation: a number on a categorical axis is a place
  ;; counted from one, and a category may be written as a value.
  (let [bars (-> {:k ["Meter" "Over" "Parking" "Bus"] :v [462 181 92 30]}
                 (pj/lay-bar :k :v)
                 (pj/coord :flip))
        by-place (pj/plot (pj/lay-segment bars {:x 4 :y 300 :x-end 3 :y-end 150 :arrow :end}))
        by-name (pj/plot (pj/lay-segment bars {:x {:value "Bus"} :y 300
                                                :x-end {:value "Parking"} :y-end 150
                                                :arrow :end}))]
    (is (= 5 (:polygons (pj/svg-summary by-place))) "four bars and one head")
    (is (= by-place by-name) "places 4 and 3 are the categories Bus and Parking")))

(deftest an-end-shares-its-axis-type-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #":y-end column :lab has type :categorical"
                        (pj/plot (pj/lay-segment {:x [1 2] :y [1 2] :lab ["a" "b"]}
                                                 :x :y {:y-end :lab}))))
  (testing "a temporal end on a temporal axis"
    (is (= 2 (:lines (pj/svg-summary
                      (pj/lay-segment {:start [(jt/local-date 2024 1 1) (jt/local-date 2024 3 1)]
                                       :stop [(jt/local-date 2024 2 1) (jt/local-date 2024 6 1)]
                                       :lane [1 2]}
                                      :start :lane {:x-end :stop})))))))

(deftest a-segment-takes-colour-and-width-test
  (let [p (pj/lay-segment {:x [1 2] :y [1 2] :g ["a" "b"]} :x :y {:y-end 0 :color :g :size 3})
        s (pj/svg-summary p)]
    (is (= 2 (count (disj (:colors s) "none" "rgb(245,245,245)" "rgb(232,232,232)"))))
    (is (= 3.0 (double (get-in (pj/plan p) [:panels 0 :layers 0 :style :stroke-width])))))
  (testing "a position adjustment is refused, as there is no band to divide"
    (is (re-find #"position"
                 (with-out-str (pj/lay-segment pts :x0 :y0 {:y-end 0 :position :dodge}))))))
