(ns scicloj.plotje.rule-default-colour-test
  "A rule or a band beside layers that draw different columns takes no
   default colour, so nothing is said about a :color it was not given."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]))

(def rows {:a [1 2 3 4] :b [2 4 6 8] :c [1 3 2 5]})

(defn- printed [pose]
  (with-out-str (pj/plan pose)))

(deftest a-rule-beside-two-columns-prints-nothing-about-colour
  (let [two (-> rows (pj/lay-point :a :b) (pj/lay-point :a :c))]
    (testing "a rule"
      (is (not (str/includes? (printed (pj/lay-rule-h two {:y-intercept 3}))
                              "was given the column"))))
    (testing "a band"
      (is (not (str/includes? (printed (pj/lay-band-h two {:y-min 2 :y-max 3}))
                              "was given the column"))))
    (testing "the legend names the two columns and not the rule"
      (is (= ["b" "c"]
             (mapv :label (get-in (pj/plan (pj/lay-rule-h two {:y-intercept 3}))
                                  [:legend :entries])))))
    (testing "a colour column written on the rule is still reported"
      (is (str/includes? (printed (pj/lay-rule-h (assoc rows :g ["p" "q" "p" "q"])
                                                 {:y-intercept 3 :color :g}))
                         "lay-rule-h was given the column :g as :color")))))
