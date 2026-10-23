(ns scicloj.plotje.min-panel-size-test
  "`:min-panel-size`, the option the too-small-panel message names, is
   accepted where the message is read: `pj/options` and `pj/with-config`."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def data {:u (vec (range 30)) :v (vec (range 30))
           :g (mapv #(str "g" %) (range 30))})

(def faceted (-> data (pj/lay-point :u :v) (pj/facet :g)))

(defn- printed [f]
  (let [sw (java.io.StringWriter.)]
    (binding [*out* sw] (f))
    (str sw)))

(deftest the-option-the-message-names-is-accepted-test
  (testing "the message names it"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"lower :min-panel-size"
                          (pj/plan faceted))))
  (testing "pj/options takes it, with no warning, and the plot draws"
    (is (= "" (printed #(pj/options faceted {:min-panel-size 5}))))
    (is (= 30 (count (:panels (pj/plan (pj/options faceted {:min-panel-size 5})))))))
  (testing "pj/with-config takes it"
    (is (= "" (printed #(pj/with-config {:min-panel-size 5} (pj/plan faceted)))))
    (is (= 30 (count (:panels (pj/with-config {:min-panel-size 5} (pj/plan faceted)))))))
  (testing "it is documented with the other configuration keys"
    (is (contains? pj/config-key-docs :min-panel-size))))
