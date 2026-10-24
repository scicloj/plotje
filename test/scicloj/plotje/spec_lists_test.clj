(ns scicloj.plotje.spec-lists-test
  "A scale spec's sequences may be written as dtype-next readers, Java
   lists or arrays, which `sequential?` answers no to: each is read as a
   vector, in `pj/scale` and in a mapping's `:scale` alike."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [tablecloth.column.api :as tcc]
            [wadogo.scale :as ws]))

(defn- leverage-breaks
  "Breaks as `metamorph.ml` writes them for a leverage axis: wadogo's
   ticks, divided column-wise -- a dtype reader that is not
   `sequential?`."
  []
  (let [ticks (ws/ticks (ws/scale :linear {:domain [0.1 0.5]}) 5)]
    (tcc// ticks (tcc/- 1 ticks))))

(def d {:x [1.0 2.0 3.0 4.0] :y [1.0 4.0 9.0 16.0]})

(defn- x-ticks [pose] (-> pose pj/plan :panels first :x-ticks))

(deftest a-reader-is-read-as-a-vector-test
  (let [expected (x-ticks (-> d (pj/lay-point :x :y) (pj/scale :x {:breaks [1.0 2.0 3.0]
                                                                     :tick-labels ["a" "b" "c"]})))]
    (testing "a column expression, as `metamorph.ml` writes one"
      (is (not (sequential? (leverage-breaks))))
      (let [b (leverage-breaks)]
        (is (= (x-ticks (-> d (pj/lay-point :x :y) (pj/scale :x {:breaks (vec b)})))
               (x-ticks (-> d (pj/lay-point :x :y) (pj/scale :x {:breaks b})))))))
    (testing "a Java array"
      (is (= expected
             (x-ticks (-> d (pj/lay-point :x :y)
                          (pj/scale :x {:breaks (double-array [1.0 2.0 3.0])
                                        :tick-labels ["a" "b" "c"]}))))))
    (testing "in a mapping's :scale"
      (is (= expected
             (x-ticks (pj/lay-point d {:x {:column :x :scale {:breaks (double-array [1.0 2.0 3.0])
                                                               :tick-labels ["a" "b" "c"]}}
                                       :y :y}))))))
  (testing "a set is still refused"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"is not a sequence of tick values"
                          (pj/scale (pj/lay-point d :x :y) :x {:breaks #{1.0 2.0}})))))
