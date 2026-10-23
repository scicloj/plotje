(ns scicloj.plotje.arrange-too-small-test
  "An arrangement whose cells are too small to draw says so in terms of
   the arrangement: its size, the rows or columns dividing it, and where
   the size is written."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def tiny {:u [1.0 2.0 3.0] :v [4.0 5.0 6.0] :g ["a" "b" "c"]})

(defn- cells [n] (vec (repeat n (pj/lay-point tiny :u :v))))

(defn- message [pose]
  (try (pj/plan pose) nil
       (catch clojure.lang.ExceptionInfo e (ex-message e))))

(deftest too-many-rows-names-the-arrangement-test
  (let [msg (message (pj/arrange (cells 38) {:cols 4}))]
    (is (re-find #"A cell of this arrangement is 40 high" msg))
    (is (re-find #"the arrangement's height, 400, is divided among 10 rows of cells" msg))
    (is (re-find #"\(pj/arrange cells \{:height \.\.\.\}\)" msg))
    (is (not (re-find #"The plot's height, 40" msg))
        "the cell's share is not called the plot's height")
    (is (not (re-find #":min-panel-size" msg))))
  (testing "the form the message suggests draws"
    (is (= 38 (count (:sub-plots (pj/plan (pj/arrange (cells 38) {:cols 4 :height 1200}))))))))

(deftest too-many-columns-names-the-arrangement-test
  (let [msg (message (pj/arrange (cells 15) {:cols 15}))]
    (is (re-find #"A cell of this arrangement is 40 wide" msg))
    (is (re-find #"the arrangement's width, 600, is divided among 15 columns of cells" msg))
    (is (re-find #"a smaller :cols" msg)))
  (testing "a row of cells splitting one dataset, written as a pose by hand"
    (let [msg (message (pj/pose {:data tiny :mapping {:x :u :y :v}
                                 :layers [{:layer-type :point}]
                                 :layout {:direction :horizontal}
                                 :poses (vec (repeat 15 {}))}))]
      (is (re-find #"is divided among 15 columns of cells" msg))
      (is (not (re-find #"The plot's width" msg))))))

(deftest a-facet-that-does-not-fit-suggests-an-arrangement-that-does-test
  (let [data {:u (vec (range 38)) :v (vec (range 38))
              :g (mapv #(str "g" %) (range 38))}
        msg (message (-> data (pj/lay-point :u :v) (pj/facet :g)))
        height (some->> msg (re-find #"\{:cols 4 :height (\d+)\}") second parse-long)]
    (is (= 1200 height))
    (testing "and the suggested form draws"
      (let [poses (mapv (fn [g] (pj/lay-point {:u [1.0 2.0] :v [1.0 2.0]} :u :v)) (:g data))]
        (is (= 38 (count (:sub-plots (pj/plan (pj/arrange poses {:cols 4 :height height}))))))))))

(deftest a-cell-pinning-its-own-panel-keeps-its-own-message-test
  (is (re-find #"Each panel would be"
               (str (message (pj/arrange [(pj/options (pj/facet (pj/lay-point tiny :u :v) :g)
                                                      {:panel-width 5})
                                          (pj/lay-point tiny :u :v)]))))))
