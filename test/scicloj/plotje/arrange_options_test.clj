(ns scicloj.plotje.arrange-options-test
  "The options map of `pj/arrange`: its own options, the plot options
   `pj/options` takes, and a report for a key that is neither."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def tiny {:u [1.0 2.0 3.0] :v [4.0 5.0 6.0] :w [7.0 9.0 8.0] :g ["a" "b" "a"]})

(def a (pj/lay-point tiny :u :v))
(def b (pj/lay-point tiny :u :w))

(defn- printed [f]
  (let [sw (java.io.StringWriter.)]
    (binding [*out* sw] (f))
    (str sw)))

(deftest arrange-reports-a-key-it-does-not-read-test
  (doseq [opts [{:share-scale #{:x}} {:weights [3 1]} {:rows 1} {:col :g} {:nosuch 1}]]
    (testing (pr-str opts)
      (let [out (printed #(pj/arrange [a b] opts))]
        (is (re-find #"pj/arrange does not recognize \[" out))
        (is (re-find (re-pattern (str (first (keys opts)))) out)))
      (testing "and the arrangement is the one drawn without the key"
        (is (= (:opts (pj/arrange [a b]))
               (binding [*out* (java.io.StringWriter.)]
                 (:opts (pj/arrange [a b] opts))))))))
  (testing "a misspelt key is told the key it is near"
    (is (re-find #"Did you mean :share-scales for :share_scales"
                 (printed #(pj/arrange [a b] {:share_scales #{:x}})))))
  (testing "under :strict the key is an error"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"pj/arrange does not recognize \[:rows\]"
                          (pj/with-config {:strict true}
                            (pj/arrange [a b] {:rows 1})))))
  (testing "the options pj/arrange reads print nothing"
    (is (= "" (printed #(pj/arrange [a b] {:cols 1 :title "T" :subtitle "S" :caption "C"
                                           :width 500 :height 300 :share-scales #{:x}
                                           :align-panels true}))))))

(deftest arrange-writes-a-plot-option-on-the-arrangement-test
  (testing "a plot option in the options map is what pj/options writes"
    (doseq [opts [{:x-label "X"} {:theme {:bg "#FFEEEE"}} {:point-radius 8}
                  {:legend-position :bottom}]]
      (is (= "" (printed #(pj/arrange [a b] opts))))
      (is (= (:opts (pj/options (pj/arrange [a b]) opts))
             (:opts (pj/arrange [a b] opts))))))
  (testing "and it reaches every cell"
    (let [texts (:texts (pj/svg-summary (pj/plot (pj/arrange [a b] {:x-label "The X"}))))]
      (is (= 2 (count (filter #{"The X"} texts))))))
  (testing "beside the options pj/arrange reads itself"
    (let [arr (pj/arrange [a b] {:cols 1 :title "T" :y-label "Y"})]
      (is (= {:title "T" :y-label "Y"} (:opts arr)))
      (is (= :vertical (get-in arr [:layout :direction])))
      (is (= 2 (count (:poses arr))))))
  (testing "a value pj/options reports is reported here"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #":legend-position must be one of"
                          (pj/arrange [a b] {:legend-position :left})))))
