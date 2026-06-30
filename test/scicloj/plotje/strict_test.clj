(ns scicloj.plotje.strict-test
  "Tests for the :strict config flag controlling option-key validation
   (warn-and-strip vs throw)."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]))

(def tiny {:x [1.0 2.0 3.0] :y [4.0 5.0 6.0]})

(deftest strict-default-warns-and-strips
  (testing "default :strict false: lay-* with unknown key warns and proceeds"
    (let [out (java.io.StringWriter.)
          fr (binding [*out* out]
               (-> tiny (pj/lay-point :x :y {:colour :a})))]
      (is (pj/pose? fr))
      (is (re-find #"does not recognize option" (str out)))
      (is (not (contains? (-> fr :layers first :mapping) :colour))
          ":colour is stripped, not stored on the layer"))))

(deftest strict-mode-throws
  (testing ":strict true (via with-config) throws on unknown lay-* key"
    (pj/with-config {:strict true}
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"does not recognize option"
           (-> tiny (pj/lay-point :x :y {:colour :a}))))))

  (testing ":strict true throws on unknown pj/options key"
    (pj/with-config {:strict true}
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"does not recognize option"
           (-> tiny (pj/lay-point :x :y) (pj/options {:scale-x :log}))))))

  (testing ":strict true throws on unknown pj/pose aesthetic key"
    (pj/with-config {:strict true}
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"does not recognize option"
           (-> tiny (pj/pose {:colour :a}))))))

  (testing ":strict true throws when a categorical :break names no category"
    (pj/with-config {:strict true}
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"not among the axis categories"
           (-> {:x ["a" "b" "c"] :y [1 2 3]}
               (pj/lay-point :x :y)
               (pj/scale :x {:breaks ["a" "zzz"]})
               pj/plan))))))

(deftest strict-mode-allows-known-keys
  (testing ":strict true does not interfere with valid options"
    (pj/with-config {:strict true}
      (let [fr (-> tiny (pj/lay-point :x :y {:color "red" :alpha 0.5}))]
        (is (pj/pose? fr))
        (is (= "red" (-> fr :layers first :mapping :color)))))))

(deftest strict-flag-via-set-config
  (testing "set-config! :strict true is honored"
    (try
      (defaults/set-config! {:strict true})
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"does not recognize option"
           (-> tiny (pj/lay-point :x :y {:colour :a}))))
      (finally (defaults/set-config! nil)))))

(deftest strict-error-suggests-toggle
  (testing "strict-mode error message points at how to disable"
    (pj/with-config {:strict true}
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #":strict false"
           (-> tiny (pj/lay-point :x :y {:colour :a})))))))
