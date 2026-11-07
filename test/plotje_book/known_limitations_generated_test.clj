(ns
 plotje-book.known-limitations-generated-test
 (:require
  [scicloj.plotje.api :as pj]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l121 (select-keys (pj/config) [:width :height :domain-padding]))


(deftest
 t4_l123
 (is
  ((fn [m] (= {:width 600, :height 400, :domain-padding 0.05} m))
   v3_l121)))
