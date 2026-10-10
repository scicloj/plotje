(ns scicloj.plotje.shared-panel-test
  "The layers of a pose share one panel (decision 3): what that draws,
   what is reported where the layers disagree about an axis, the
   retired `pj/overlay`, and the titles a series draws beside the same
   columns drawn as layers."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def cars
  {:displ [1.8 2.0 2.8 3.1 4.2]
   :hwy [29 31 26 27 20]
   :cty [18 21 16 18 14]
   :class ["compact" "compact" "midsize" "suv" "suv"]})

(defn- y-domains [plan]
  (mapv (fn [sp] (mapv :y-domain (:panels (:plan sp)))) (:sub-plots plan)))

(deftest layers-with-different-columns-share-one-panel-test
  (let [p (-> cars
              (pj/pose {:x :displ})
              (pj/lay-point {:y :hwy})
              (pj/lay-point {:y :cty}))
        texts (set (:texts (pj/svg-summary p)))]
    (is (= {:panels 1 :points 10} (select-keys (pj/svg-summary p) [:panels :points])))
    ;; The axis is titled by both columns, and the legend names each.
    (is (contains? texts "hwy, cty"))
    (is (every? texts ["hwy" "cty"]))))

(deftest overlaid-cell-shares-the-extent-of-every-column-test
  ;; An overlaid cell sharing y with another cell was stamped the extent
  ;; of its first column only, so the second column drew off the panel.
  (let [d {:x [1 2 3 4] :a [1 2 3 4] :b [100 200 250 300]}
        both (-> d (pj/lay-point :x :a) (pj/lay-point :x :b))
        doms (y-domains (pj/plan (pj/arrange [both (pj/lay-point d :x :a)]
                                             {:share-scales #{:y}})))]
    (is (apply = (map first doms)))
    (is (<= (ffirst (first doms)) 1))
    (is (>= (second (ffirst doms)) 300))))

(deftest layers-disagreeing-about-an-axis-are-reported-test
  (testing "a category beside a number on one axis is an error naming both"
    (let [msg (try (pj/plan (-> cars (pj/lay-point :class :hwy) (pj/lay-point :displ :hwy)))
                   nil
                   (catch clojure.lang.ExceptionInfo e (ex-message e)))]
      (is (re-find #"on :x they draw columns of different kinds" msg))
      (is (re-find #":class \(categorical\)" msg))
      (is (re-find #":displ \(numerical\)" msg))
      (is (re-find #"pj/arrange" msg))
      (is (re-find #"\(pj/facet pose \{:x \[:class :displ\]\}\)" msg))))
  (testing "the split the error names draws"
    (is (= 2 (:panels (pj/svg-summary
                       (pj/facet (-> cars (pj/lay-point :class :hwy) (pj/lay-point :displ :hwy))
                                 {:x [:class :displ]}))))))
  (testing "a count beside a column on :y is a Note"
    (let [out (with-out-str (pj/plan (-> cars (pj/lay-histogram :hwy) (pj/lay-point :hwy :cty))))]
      (is (re-find #"Note: the layers of this pose share one panel, and on :y pj/lay-histogram draws a count, pj/lay-point draws :cty" out))))
  (testing "layers agreeing about both axes say nothing"
    (is (= "" (with-out-str (pj/plan (-> cars (pj/lay-point :displ :hwy) (pj/lay-smooth :displ :hwy))))))))

(deftest overlay-is-retired-test
  (let [p (-> cars (pj/lay-point :displ :hwy) (pj/lay-point :displ :cty))]
    (testing "asking for one panel warns and changes nothing"
      (is (re-find #"Warning: pj/overlay -- :overlay is retired and ignored"
                   (with-out-str (pj/overlay p))))
      (is (= p (binding [*out* (java.io.StringWriter.)] (pj/overlay p))))
      (is (re-find #"Warning: pj/lay-point -- :overlay is retired"
                   (with-out-str (pj/lay-point cars :displ :hwy {:overlay true})))))
    (testing "asking for separate panels is an error naming the split"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"pj/overlay -- :overlay is retired.*pj/facet"
                            (pj/overlay p false)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"pj/lay-point -- :overlay is retired"
                            (pj/lay-point p :displ :cty {:overlay false}))))))

(deftest a-series-titles-the-columns-it-reads-test
  (let [layers (-> cars (pj/pose {:x :displ}) (pj/lay-point {:y :hwy}) (pj/lay-point {:y :cty}))
        series (-> cars (pj/pose {:x :displ :y [:hwy :cty]}) pj/lay-point)
        titles (fn [p] (let [plan (pj/plan p)]
                         {:x (:x-label plan) :y (:y-label plan)}))]
    ;; The pivot's invented columns title nothing: the same picture as
    ;; the same columns drawn as layers.
    (is (= (pj/svg-summary layers) (pj/svg-summary series)))
    (is (= {:x "displ" :y "hwy, cty"} (titles series)))
    (testing "a key column named with :as titles its legend"
      (is (some #{"measure"} (:texts (pj/svg-summary (pj/lay-line cars :displ {:series [:hwy :cty] :as :measure}))))))
    (testing "series in pairs title each axis by its own columns"
      (is (= {:x "displ, hwy" :y "cty, hwy"}
             (titles (pj/lay-point cars [:displ :hwy] [:cty :hwy])))))
    (testing "a title written in the options wins"
      (is (= "mpg" (:y (titles (pj/options series {:y-label "mpg"}))))))
    (testing "a label written on the scale wins"
      (is (= "mpg" (:y (titles (pj/lay-point cars :displ {:series [:hwy :cty] :scale {:label "mpg"}}))))))))

(deftest a-written-type-is-the-type-the-axis-reads-test
  (let [d {:n [4 6 8] :k ["a" "b" "c"] :v [1 2 3]}]
    (is (= 6 (:points (pj/svg-summary (-> d
                                          (pj/lay-point :n :v {:x-type :categorical})
                                          (pj/lay-point :k :v))))))))

(deftest dates-beside-numbers-name-their-kinds-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"cannot place dates and numbers together"
                        (pj/plan (-> {:t [(java.time.LocalDate/of 2020 1 1) (java.time.LocalDate/of 2020 2 1)]
                                      :n [1 2] :v [3 4]}
                                     (pj/lay-point :t :v)
                                     (pj/lay-point :n :v))))))

(deftest columns-of-different-size-note-test
  (let [d {:t [1 2 3 4]
           :small [12.0 20.0 31.0 44.0]
           :big [1999.0 2002.0 2005.0 2008.0]
           :near [10.0 25.0 30.0 40.0]}
        printed (fn [pose] (let [out (java.io.StringWriter.)]
                             (binding [*out* out] (pj/plan pose))
                             (str out)))
        layers (fn [a b] (-> d (pj/lay-point :t a) (pj/lay-point :t b)))]
    (testing "two layers whose columns differ greatly in size print one Note naming both"
      (let [out (printed (layers :small :big))]
        (is (= 1 (count (re-seq #"Note:" out))))
        (is (re-find #"on :y one axis holds :small \(12 to 44\), :big \(1999 to 2008\)" out))
        (is (re-find #"together span 12 to 2008" out))
        (is (re-find #"\(pj/facet pose \{:y \[:small :big\]\}\)" out))))
    (testing "a series of the same columns prints it too, on the pose or on a layer"
      (is (= 1 (count (re-seq #"Note: on :y" (printed (-> d (pj/pose {:x :t :y [:small :big]}) pj/lay-point))))))
      (is (= 1 (count (re-seq #"Note: on :y" (printed (pj/lay-point d :t [:small :big])))))))
    (testing "the :x axis is read the same way"
      (is (re-find #"Note: on :x" (printed (-> d (pj/lay-point :small :t) (pj/lay-point :big :t))))))
    (testing "columns of comparable size print nothing"
      (is (= "" (printed (layers :small :near)))))
    (testing "with a :domain specified, or a log scale, no extent is fitted on a linear axis and nothing is printed"
      (is (= "" (printed (pj/scale (layers :small :big) :y {:domain [0 2100]}))))
      (is (= "" (printed (pj/scale (layers :small :big) :y :log)))))
    (testing "a column holding one value is not counted as drawn flat"
      (is (= "" (printed (-> (assoc d :limit [30.0 30.0 30.0 30.0])
                             (pj/lay-point :t :small)
                             (pj/lay-line :t :limit))))))
    (testing "a layer beside a series is counted with the series' columns, named as written"
      (let [out (printed (-> (assoc d :mid [900.0 950.0 1000.0 1100.0])
                             (pj/lay-line :t [:big :mid])
                             (pj/lay-point :t :small)))]
        (is (= 1 (count (re-seq #"Note:" out))))
        (is (re-find #":big \(1999 to 2008\), :mid \(900 to 1100\), :small \(12 to 44\)" out))
        (is (not (re-find #":value" out)))))
    (testing "a layer placed in the drawing area is not a column of the axis"
      (is (= "" (printed (-> d
                             (pj/lay-point :t :small)
                             (pj/lay-point {:in :drawing-area
                                            :data {:px [300.0] :py [900.0]}
                                            :x :px :y :py}))))))
    (testing "a panel per column prints nothing"
      (is (= "" (printed (-> d (pj/pose {:x :t}) pj/lay-point (pj/facet {:y [:small :big]}))))))))
