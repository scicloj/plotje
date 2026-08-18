(ns scicloj.plotje.impl.pose-schema-test
  "Tests for the Pose Malli schema."
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.impl.pose-schema :as fs]))

(deftest leaf-pose-validity-test
  (testing "leaf with layers but no data/mapping is valid"
    (is (fs/valid? {:layers [{:layer-type :point}]})))

  (testing "leaf with full context is valid"
    (is (fs/valid? {:data {:x [1 2 3] :y [4 5 6]}
                    :mapping {:x :x :y :y}
                    :layers [{:layer-type :point
                              :mapping {:color :species}}]
                    :opts {:title "t"}})))

  (testing "layer with :mark / :stat overrides is valid"
    (is (fs/valid? {:layers [{:layer-type :smooth
                              :stat :linear-model
                              :mark :line}]}))))

(deftest composite-pose-validity-test
  (testing "composite with poses and layout is valid"
    (is (fs/valid? {:poses [{:layers [{:layer-type :point}]}
                            {:layers [{:layer-type :line}]}]
                    :layout {:direction :horizontal :weights [1 1]}})))

  (testing "nested composites are valid"
    (is (fs/valid? {:poses [{:poses [{:layers [{:layer-type :point}]}]}]})))

  (testing "composite with :share-scales is valid"
    (is (fs/valid? {:share-scales #{:x :y}
                    :poses [{:layers [{:layer-type :point}]}
                            {:layers [{:layer-type :point}]}]}))))

(deftest rejection-test
  (testing "non-map rejected"
    (is (not (fs/valid? "string"))))

  (testing "bad :layout :direction rejected"
    (is (not (fs/valid? {:poses []
                         :layout {:direction :diagonal}}))))

  (testing "non-positive :weights rejected"
    (is (not (fs/valid? {:poses []
                         :layout {:weights [1 -1]}}))))

  (testing ":share-scales with unknown axis rejected"
    (is (not (fs/valid? {:share-scales #{:z}
                         :poses []}))))

  (testing ":mapping keys must be keywords"
    (is (not (fs/valid? {:layers [{:mapping {"x" :foo}}]}))))

  (testing ":layers must be a vector of maps, :layer-type must be a keyword"
    (is (not (fs/valid? {:layers "not a vector"})))
    (is (not (fs/valid? {:layers [{:layer-type "string"}]})))))

(deftest extras-pass-through-test
  (testing "non-structural keys are allowed (for facet/mosaic metadata)"
    (is (fs/valid? {:layers [{:layer-type :point}]
                    :panel-label "row=a, col=b"
                    :facet-row :a
                    :facet-col :b}))))

;; ---- Mapping value grammar ----

(deftest every-aesthetic-has-a-reading-test
  (testing "the value grammar covers exactly the aesthetics that can name a column"
    ;; A new aesthetic added to the registry fails here until someone
    ;; writes down what its values may be.
    (is (= defaults/column-keys
           (set (keys fs/aesthetic-value-schemas))))))

(defn mapping-valid?
  "Validate a mapping on its own, without the surrounding pose."
  [m]
  (m/validate fs/Mapping m))

(deftest mapping-value-grammar-test
  (testing "a column reference is a keyword or a string, for every aesthetic"
    (doseq [k (keys fs/aesthetic-value-schemas)]
      (is (mapping-valid? {k :some-column}) (str k " accepts a keyword"))
      (is (mapping-valid? {k "some-column"}) (str k " accepts a string"))))

  (testing "nil is legal everywhere -- it cancels an inherited mapping"
    (doseq [k (keys fs/aesthetic-value-schemas)]
      (is (mapping-valid? {k nil}) (str k " accepts nil"))))

  (testing ":x and :y also take a literal value in data space"
    (is (mapping-valid? {:x 6.5 :y 3}))
    (is (mapping-valid? {:x (java.time.LocalDate/of 2020 1 1)}))
    (is (mapping-valid? {:x-end 9 :y-min 2 :y-max 4})))

  (testing ":size and :alpha take a literal value in their own range"
    (is (mapping-valid? {:size 4}))
    (is (mapping-valid? {:alpha 0.3}))
    (is (not (mapping-valid? {:size 0})) ":size is a radius, so it is positive")
    (is (not (mapping-valid? {:alpha 4})) ":alpha is an opacity within 0 and 1"))

  (testing "an appearance aesthetic other than :size and :alpha takes no number"
    ;; Each fails differently today, which is what the aesthetics
    ;; workplan is about: :color reaches the plan schema, :shape is
    ;; ignored, :group empties the layer.
    (doseq [k [:color :shape :fill :text]]
      (is (not (mapping-valid? {k 4})) (str k " takes no number"))))

  (testing ":group takes several columns as well as one"
    (is (mapping-valid? {:group [:a :b]}))
    (is (not (mapping-valid? {:group [4]}))))

  (testing "the column type overrides are the three classifications"
    (is (mapping-valid? {:x-type :categorical :y-type :numerical
                         :color-type :temporal}))
    (is (not (mapping-valid? {:x-type :continuous}))))

  (testing "a mapping is open, because layer options ride in it too"
    (is (mapping-valid? {:jitter 0.2 :in :drawing-area :nudge-x 3
                         :font-size 12 :bandwidth 0.3}))))

(deftest poses-built-through-the-api-are-valid-test
  (testing "every public constructor produces a pose the schema admits"
    ;; The schema went months without admitting a matrix layout, which
    ;; `pj/pose` stamps whenever a threaded `:x` or `:y` promotes a
    ;; leaf. Nothing caught it because nothing validated a real pose.
    (let [data {:num [1.0 2.0 3.0 4.0]
                :num2 [2.0 4.0 3.0 5.0]
                :cat ["p" "q" "p" "q"]}]
      (doseq [[label pose]
              {"leaf"        (pj/lay-point data :num :num2)
               "aesthetics"  (pj/lay-point data :num :num2 {:color :cat :size 4})
               "overlay"     (-> (pj/lay-point data :num :num2)
                                 (pj/lay-smooth)
                                 (pj/lay-text {:x 3 :y 3 :text "note"}))
               "facet"       (-> (pj/lay-point data :num :num2) (pj/facet :cat))
               "facet-grid"  (-> (pj/lay-point data :num :num2)
                                 (pj/facet-grid :cat :cat))
               "arrange"     (pj/arrange [(pj/lay-point data :num :num2)
                                          (pj/lay-line data :num :num2)])
               "share"       (pj/arrange [(pj/lay-point data :num :num2)
                                          (pj/lay-point data :num :num2)]
                                         {:share-scales #{:x :y}})
               "multi-pair"  (pj/pose data [[:num :num2] [:num2 :num]])
               "scale"       (-> (pj/lay-point data :num :num2) (pj/scale :x :log))
               "coord"       (-> (pj/lay-point data :num :num2) (pj/coord :flip))
               "options"     (-> (pj/lay-point data :num :num2)
                                 (pj/options {:title "t"}))
               "template"    (-> (pj/pose nil {:x :num :y :num2})
                                 (pj/lay-point)
                                 (pj/with-data data))}]
        (is (fs/valid? pose) (str label ": " (pr-str (fs/explain pose))))))))
