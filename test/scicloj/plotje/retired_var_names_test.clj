(ns scicloj.plotje.retired-var-names-test
  "Names a release replaced, kept for one release beside the new name."
  (:require [clojure.test :refer [deftest is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.layer-type :as layer-type]))

(deftest the-old-names-hold-the-same-value-test
  (is (= pj/layer-mapping-docs pj/layer-option-docs))
  (is (= layer-type/layer-mapping-docs layer-type/layer-option-docs))
  (is (= layer-type/universal-mapping-keys layer-type/universal-layer-options))
  (is (every? #(:deprecated (meta %)) [#'pj/layer-option-docs
                                       #'layer-type/layer-option-docs
                                       #'layer-type/universal-layer-options])))
