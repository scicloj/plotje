(ns scicloj.plotje.panel-sizes-test
  "Panels of unequal size: `:col-weights` and `:row-weights` share out an
   arrangement's columns and rows, `:panel-width` and `:panel-height`
   pin them; and `pj/matrix`, the facet crossing columns with columns."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def cars {:displ [1.8 2.0 2.8 3.1 4.2 5.3 2.5 3.6]
           :hwy [29 31 26 27 23 17 30 25]
           :cty [18 21 16 18 15 12 20 17]})

(defn- message [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-message e))))

(defn- sizes
  "Each cell's panel width and height, by path."
  [v]
  (into {} (map (juxt :path (fn [s] [(get-in s [:plan :panel-width])
                                     (get-in s [:plan :panel-height])])))
        (:sub-plots (pj/plan v))))

(defn- near? [a b] (<= (Math/abs (- (double a) (double b))) 1.0))

(deftest weights-share-out-the-panels-test
  (testing "a row of two cells, [1 2]: the second panel twice as wide"
    (let [v (pj/arrange cars [{:x :displ :y :hwy} :hwy] {:col-weights [1 2]})
          [[a _] [b _]] (map (sizes v) [[0 0] [0 1]])]
      (is (near? (* 2 a) b))
      (is (= [0.0 0.0 600.0 400.0] (:canvas (pj/frames v))))))
  (testing "a column of two cells, [3 1]"
    (let [v (pj/arrange cars [{:x :displ :y :hwy} :displ] {:cols 1 :row-weights [3 1]})
          [[_ a] [_ b]] (map (sizes v) [[0 0] [1 0]])]
      (is (<= (Math/abs (- a (* 3 b))) 3.0))))
  (testing "equal weights draw what no weights draw"
    (is (= (pj/plot (pj/arrange cars [:hwy :cty]))
           (pj/plot (pj/arrange cars [:hwy :cty] {:col-weights [1 1]})))))
  (testing "no cell reads them"
    (is (not-any? #(some (fn [k] (contains? (:opts %) k)) [:col-weights :row-weights])
                  (:sub-plots (pj/plan (pj/arrange cars [:hwy :cty] {:col-weights [1 2]}))))))
  (testing "pj/options writes them on an arrangement as pj/arrange does"
    (is (= (pj/plot (pj/arrange cars [:hwy :cty] {:col-weights [1 2]}))
           (pj/plot (pj/options (pj/arrange cars [:hwy :cty]) {:col-weights [1 2]}))))))

(deftest pinned-panels-size-the-plot-test
  ;; The plot's size is a whole number of drawing units, and the room
  ;; its labels take need not be, so a pinned panel is within one unit.
  (testing "a number pins every column's panels, and the plot's width follows"
    (let [v (pj/arrange cars [:hwy :cty] {:panel-width 150})]
      (is (every? #(near? 150 (first %)) (vals (sizes v))))
      (is (< (nth (:canvas (pj/frames v)) 2) 600.0))))
  (testing "a vector pins each column's"
    (let [s (sizes (pj/arrange cars [{:x :displ :y :hwy} :hwy]
                               {:panel-width [300 150] :panel-height 200}))]
      (is (every? true? (map near? (s [0 0]) [300 200])))
      (is (every? true? (map near? (s [0 1]) [150 200])))))
  (testing "a scatter plot matrix, within one drawing unit"
    (is (every? (fn [[w h]] (and (near? w 120) (near? h 120)))
                (vals (sizes (pj/matrix cars [:displ :hwy :cty]
                                        {:panel-width 120 :panel-height 120})))))))

(deftest sizes-written-wrongly-test
  (is (re-find #":col-weights \[1 2 3\] gives 3 values, and the panels stand in 2 columns"
               (message #(pj/plan (pj/arrange cars [:hwy :cty] {:col-weights [1 2 3]})))))
  (is (re-find #"takes a vector of positive numbers"
               (message #(pj/plan (pj/arrange cars [:hwy :cty] {:col-weights [1 -2]})))))
  (is (re-find #":panel-width pins the panels in drawing units and :col-weights shares them"
               (message #(pj/plan (pj/arrange cars [:hwy :cty] {:panel-width 150 :col-weights [1 2]})))))
  (is (re-find #"rows of this arrangement hold different numbers of cells: 2, 1"
               (message #(pj/plan (pj/arrange cars [[:hwy :cty] [:displ]] {:col-weights [1 2]})))))
  (testing "a plot planned as one takes neither weights nor a vector of sizes"
    (let [faceted (-> cars (pj/lay-point :displ :hwy) (pj/facet :hwy))]
      (is (re-find #":col-weights shares out the columns and rows of panels of an arrangement"
                   (message #(pj/plan (pj/options faceted {:col-weights [1 2]})))))
      (is (re-find #":panel-width given a vector pins the panels of each column"
                   (message #(pj/plan (pj/options faceted {:panel-width [100 200]}))))))))

(deftest matrix-is-the-facet-crossing-columns-test
  (let [cols [:displ :hwy :cty]]
    (is (= (pj/matrix cars cols)
           (pj/facet cars {:col {:x cols} :row {:y cols}})))
    (is (= (pj/matrix cars [:displ] [:hwy :cty] {:share-scales #{}})
           (pj/facet cars {:col {:x [:displ]} :row {:y [:hwy :cty]}} {:share-scales #{}})))
    (is (= 9 (:panels (pj/svg-summary (pj/matrix cars cols)))))
    (testing "on a pose, with layers added afterwards"
      (is (= (-> cars (pj/pose {:color :hwy}) (pj/matrix cols) pj/lay-point)
             (-> cars (pj/pose {:color :hwy}) (pj/facet {:col {:x cols} :row {:y cols}}) pj/lay-point))))
    (testing "nil gives the marginals, and the corner prints nothing"
      (let [v (pj/matrix cars [:displ nil] [nil :hwy] {:col-weights [3 1] :row-weights [1 3]})
            s (sizes v)]
        (is (= 3 (:panels (pj/svg-summary v))))
        (is (= "" (with-out-str (pj/plot v))))
        (is (<= (Math/abs (- (first (s [1 0])) (* 3 (first (s [1 1]))))) 3.0))
        (is (<= (Math/abs (- (second (s [1 0])) (* 3 (second (s [0 0]))))) 3.0))))
    (testing "a column where a vector of columns goes"
      (is (re-find #"pj/matrix takes its columns across as a vector of columns"
                   (message #(pj/matrix cars :displ)))))
    (testing "a mapping where the options go warns under pj/matrix's name"
      (is (re-find #"pj/matrix does not recognize \[:color\]"
                   (with-out-str (pj/matrix cars cols {:color :hwy})))))))
