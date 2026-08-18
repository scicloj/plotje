(ns scicloj.plotje.impl.defaults-test
  "Tests for the color vocabulary check.

   `names-a-color?` decides whether a value was *meant* as a color.
   Everything the aesthetics work does with a `:by-value` scale default
   rests on it: a written value that names a color is drawn, and a
   column is drawn only when every one of its values does. So the
   question it has to answer well is not \"can this be converted\" --
   `hex->rgba` answers that, and answers it too generously -- but
   \"would a reasonable person have written this meaning a color\"."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.impl.defaults :as defaults]))

(deftest a-short-string-is-not-a-hex-code-test
  ;; clojure2d reads a bare `abc` as the hex `#aabbcc`, so asking
  ;; `hex->rgba` whether a value is a color answers yes for several
  ;; plausible column-name typos -- which would draw a color where the
  ;; user meant a column and mistyped it.
  (testing "hex-shaped short strings are not colors"
    (doseq [s ["a" "ab" "abc" "fff" "beef" "face"]]
      (is (not (defaults/names-a-color? s))
          (str s " is a likelier column typo than a shade"))))

  (testing "hex->rgba is the more generous of the two, deliberately"
    (is (= [1.0 1.0 1.0 1.0] (defaults/hex->rgba "fff")))
    (is (not (defaults/names-a-color? "fff"))))

  (testing "colors still are"
    (doseq [s ["red" "steelblue" "rebeccapurple" "#abc" "#FF0000"]]
      (is (defaults/names-a-color? s) s))
    (doseq [k [:red :steelblue :rebeccapurple]]
      (is (defaults/names-a-color? k) (str k))))

  (testing "a malformed hex is not a color, and does not throw"
    (doseq [s ["#zzz" "#"]]
      (is (not (defaults/names-a-color? s)) s)))

  (testing "a non-color value is simply not one"
    (doseq [v [5 nil true :species :setosa]]
      (is (not (defaults/names-a-color? v)) (pr-str v)))))

(deftest which-category-values-read-as-colors-test
  ;; The residual risk in deciding a column by its contents: a
  ;; categorical column all of whose values happen to name colors stops
  ;; being categorical. These are the cases that decide how large that
  ;; surface is, so they are pinned rather than reasoned about.
  (testing "category values that are not colors -- the common case"
    (doseq [s ["setosa" "virginica" "tuna" "cod" "hazel" "cat" "dog" "fox"]]
      (is (not (defaults/names-a-color? s)) s)))

  (testing "category values that are colors -- the residual"
    ;; A produce column of variety names, or a favourite-colour column.
    (doseq [s ["olive" "plum" "tomato" "salmon" "brown" "purple"]]
      (is (defaults/names-a-color? s) s))))
