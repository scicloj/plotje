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
