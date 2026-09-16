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

(deftest a-retired-configuration-key-names-its-replacement
  ;; :annotation-stroke became :rule-color. `api/renamed-options` gave
  ;; the pointer on the options path only, so the four configuration
  ;; entry points -- the key's own home -- reported a bare "does not
  ;; recognize" and read as a typo. The map lives in `defaults` now, so
  ;; `validate-config-keys!` reads it too.
  (let [said (fn [f]
               (let [out (java.io.StringWriter.)]
                 (binding [*out* out] (f))
                 (str out)))]
    (testing "the configuration path names the new key"
      (doseq [[what f] [["pj/with-config" #(pj/with-config {:annotation-stroke "red"}
                                             (constantly nil))]
                        ["the :config option" #(pj/plot (-> tiny (pj/lay-point :x :y)
                                                            (pj/options {:config {:annotation-stroke "red"}})))]]]
        (let [msg (said f)]
          (is (re-find #"does not recognize configuration key" msg)
              (str what " still reports the key"))
          (is (re-find #":annotation-stroke was renamed to :rule-color" msg)
              (str what " names what replaced it")))))
    (testing "the options path still does, in its own wording"
      (is (re-find #"Renamed to :rule-color"
                   (said #(pj/plot (-> tiny (pj/lay-point :x :y)
                                       (pj/options {:annotation-stroke "red"})))))))
    (testing "a key that was never renamed gets no pointer"
      (let [msg (said #(pj/with-config {:not-a-key 1} (constantly nil)))]
        (is (re-find #"does not recognize configuration key" msg))
        (is (not (re-find #"renamed to" msg)))))
    (testing "the replacement itself is accepted"
      (is (empty? (said #(pj/with-config {:rule-color "red"} (constantly nil))))))))

(deftest a-retired-layer-option-is-read-rather-than-dropped
  ;; :nudge-x became :dx. Reporting-and-dropping it the way
  ;; :annotation-stroke is dropped would move a label back onto the
  ;; mark it was written to clear -- a wrong picture behind a println.
  ;; So this rename is :accept: the old name is read as the new one and
  ;; the writer is told what to edit.
  (let [said (fn [f] (let [out (java.io.StringWriter.)]
                       (binding [*out* out] (f))
                       [(str out)]))
        bars {:team ["red" "green" "blue"] :score [3 5 4]}
        draw (fn [opts] (-> bars
                            (pj/lay-bar :team :score)
                            (pj/lay-text (merge {:x {:value "red"} :y 3.0
                                                 :text "hi"}
                                                opts))
                            pj/plot))]
    (testing "the old name draws exactly what the new one draws"
      (let [out (java.io.StringWriter.)
            [old new] (binding [*out* out]
                        [(draw {:nudge-x 0.5}) (draw {:dx 0.5})])]
        (is (= new old))
        (is (not= new (draw {})) "and 0.5 is a shift the picture shows")
        (is (re-find #":nudge-x was renamed to :dx" (str out)))
        (is (re-find #"Write :dx" (str out))
            "the message names the one-word edit")))
    (testing "the old name reaches the plan under the new one"
      (is (= [nil 0.5]
             (->> (-> bars
                      (pj/lay-bar :team :score)
                      (pj/lay-text {:x {:value "red"} :y 3.0 :text "hi"
                                    :nudge-x 0.5})
                      pj/plan)
                  :panels first :layers (mapv :dx)))))
    (testing "the new name draws with nothing said"
      (is (= [""] (said #(draw {:dx 0.5})))))
    (testing "under :strict the old name is an error rather than a warning"
      (pj/with-config {:strict true}
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #":nudge-x was renamed to :dx"
                              (draw {:nudge-x 0.5})))))
    (testing "the new name wins where a map carries both"
      (let [out (java.io.StringWriter.)
            both (binding [*out* out] (draw {:nudge-x 0.1 :dx 0.5}))]
        (is (= (draw {:dx 0.5}) both)
            "adding the old name cannot change what the new one drew")))))

(deftest a-dropped-rename-and-an-accepted-one-are-told-apart
  (testing "each entry carries where the name went and what happens to it"
    (is (= {:to :rule-color :mode :drop}
           (defaults/renamed-options :annotation-stroke)))
    (is (= {:to :dx :mode :accept} (defaults/renamed-options :nudge-x)))
    (is (= {:to :dy :mode :accept} (defaults/renamed-options :nudge-y))))
  (testing "one reader answers what replaced a name, for both paths"
    (is (= :rule-color (defaults/renamed-to :annotation-stroke)))
    (is (= :dx (defaults/renamed-to :nudge-x)))
    (is (nil? (defaults/renamed-to :not-a-key))))
  (testing "only an :accept rename is read"
    (is (defaults/rename-accepted? :nudge-x))
    (is (not (defaults/rename-accepted? :annotation-stroke)))
    (is (not (defaults/rename-accepted? :not-a-key)))))
