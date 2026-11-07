(ns scicloj.plotje.facet-cols-test
  "`:cols` written after a facet. A facet by one category across is
   drawn in rows of that many panels, as the arrangement of its cells
   with the same `:cols` is; on any other facet the key is named by the
   call it was written in, and dropped."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def d {:u [1 2 3 4 5] :v [1 2 3 4 5] :g ["a" "b" "c" "d" "e"] :h ["x" "x" "y" "y" "y"]})
(def p (pj/lay-point d :u :v))

;; Two rows per category, so a line has something to join in each panel.
(def d2 {:u [1 2 3 4 5 2 3 4 5 6] :v [1 2 3 4 5 2 3 4 5 6]
         :g ["a" "b" "c" "d" "e" "a" "b" "c" "d" "e"]})

(def cells (mapv (fn [g] {:where {:g g}}) ["a" "b" "c" "d" "e"]))

(deftest a-facet-across-wraps-test
  (testing "the facet draws what the arrangement of its cells draws, with the same :cols"
    (is (= (pj/plot (pj/facet p :g {:cols 2}))
           (pj/plot (pj/arrange p cells {:cols 2}))))
    (is (= (pj/plot (pj/facet p :g {:cols 4}))
           (pj/plot (pj/arrange p cells)))))
  (testing "as many columns as categories, or more, is the facet in one row"
    (is (= (pj/plot (pj/facet p :g {:cols 5})) (pj/plot (pj/facet p :g))))
    (is (= (pj/plot (pj/facet p :g {:cols 9})) (pj/plot (pj/facet p :g)))))
  (testing "the pose records :cols on its layout, and stays a leaf"
    (let [v (pj/facet p :g {:cols 2})]
      (is (= {:cols 2} (:layout v)))
      (is (nil? (:poses v)))))
  (testing "a layer added afterwards is drawn in every panel"
    (is (= 5 (:lines (pj/svg-summary (-> (pj/lay-point d2 :u :v)
                                         (pj/facet :g {:cols 2})
                                         (pj/lay-line :u :v)))))))
  (testing "data given afterwards"
    (is (= 5 (:panels (pj/svg-summary (-> (pj/pose {:x :u :y :v})
                                          pj/lay-point
                                          (pj/facet :g {:cols 2})
                                          (pj/with-data d)))))))
  (testing "the other plot options are still taken"
    (is (= "T" (get-in (pj/facet p :g {:cols 2 :title "T"}) [:opts :title]))))
  (testing ":cols must be a positive whole number"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"pj/facet takes :cols as a positive whole number"
                          (pj/facet p :g {:cols 0})))))

(deftest cols-on-another-facet-test
  (testing "a facet down names :cols, the facet across, and pj/arrange"
    (let [printed (with-out-str (is (= 5 (:panels (pj/svg-summary (pj/facet p {:row :g} {:cols 2}))))))]
      (is (re-find #"pj/facet does not recognize \[:cols\]\. A facet over :row draws its panels in one column" printed))
      (is (re-find #"pj/arrange" printed))))
  (testing "pj/facet given :col and :row is described as a grid"
    (is (re-find #"pj/facet does not recognize \[:cols\]\. A facet over :col and :row draws a grid"
                 (with-out-str (pj/facet p {:col :g :row :h} {:cols 2}))))
    (is (re-find #"A facet over :col and :row draws a grid"
                 (with-out-str (pj/facet p {:col {:x [:u :v]} :row {:y [:u :v]}} {:cols 2})))))
  (testing "a facet down written after the wrapped one makes a grid, and :cols is not used"
    (let [v (-> p
                (pj/facet :g {:cols 2})
                (pj/facet {:row :h}))
          printed (with-out-str (is (= 10 (:panels (pj/svg-summary v)))))]
      (is (re-find #":cols is not used" printed))))
  (testing "under :strict it reports an error"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"pj/facet does not recognize \[:cols\]"
                          (pj/with-config {:strict true} (pj/facet p {:row :g} {:cols 2}))))))
