(ns scicloj.plotje.shared-panel-test
  "The layers of a pose share one panel (decision 3): what that draws,
   what is reported where the layers disagree about an axis, the
   retired `pj/overlay`, and the titles a series draws beside the same
   columns drawn as layers."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]))

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

(deftest a-layer-beside-a-series-draws-its-own-rows-once-test
  ;; A layer naming a column of its own on the axis a series is on
  ;; draws none of the series. It read the pivoted rows, which hold
  ;; each row of the data once per series column, so every mark was
  ;; drawn twice -- once in each series' colour where the series was
  ;; on the pose. With no :color specified it takes the colour and the
  ;; legend entry of its column, beside the series' entries.
  (let [groups (fn [pose]
                 (vec (for [p (:panels (pj/plan pose)) l (:layers p)]
                        (mapv (fn [g] [(:label g) (count (:xs g))]) (:groups l)))))]
    (testing "a series on the pose"
      (is (= [[["hwy" 5] ["cty" 5]] [["displ" 5]]]
             (groups (-> cars
                         (pj/pose {:x :displ :y [:hwy :cty]})
                         pj/lay-point
                         (pj/lay-point {:y :displ}))))))
    (testing "a series on a layer"
      (is (= [[["hwy" 5] ["cty" 5]] [["displ" 5]]]
             (groups (-> cars
                         (pj/lay-point :displ [:hwy :cty])
                         (pj/lay-point :displ :displ))))))
    (testing "a literal :color on the layer is drawn as it stands, with no entry"
      (is (= [[["hwy" 5] ["cty" 5]] [[nil 5]]]
             (groups (-> cars
                         (pj/pose {:x :displ :y [:hwy :cty]})
                         pj/lay-point
                         (pj/lay-point {:y :displ :color "black"}))))))
    (testing "the layer keeps a :color of its own"
      (is (= [[["hwy" 5] ["cty" 5]] [["compact" 2] ["midsize" 1] ["suv" 2]]]
             (groups (-> cars
                         (pj/pose {:x :displ :y [:hwy :cty]})
                         pj/lay-point
                         (pj/lay-point {:y :displ :color :class}))))))))

(deftest kinds-error-names-the-type-option-test
  (let [message (fn [pose] (try (pj/plan pose) nil (catch clojure.lang.ExceptionInfo e (ex-message e))))]
    (testing "categories beside numbers: the column can be read as categories"
      (is (re-find #"set :x-type :categorical on its layer"
                   (message (-> cars (pj/lay-point :class :hwy) (pj/lay-point :cty :hwy))))))
    (testing "dates beside numbers: no such reading"
      (is (not (re-find #"-type"
                        (message (-> {:d [#inst "2024-01-01" #inst "2024-02-01"] :n [1 2] :v [1 2]}
                                     (pj/lay-point :d :v)
                                     (pj/lay-point :n :v)))))))))

(defn- panel-marks
  "The marks each panel of a plan draws, cell by cell for a composite."
  [plan]
  (if-let [subs (:sub-plots plan)]
    (vec (mapcat #(panel-marks (:plan %)) subs))
    (mapv #(mapv :mark (:layers %)) (:panels plan))))

(defn- draft-columns [pose]
  (mapv (juxt :mark :x :y)
        (filter #(and (map? %) (contains? % :__panel-idx))
                (tree-seq coll? seq (pj/draft pose)))))

(deftest a-layer-reads-the-panels-column-where-it-needs-one-test
  ;; The panel's column on an axis is the pose's, else that of the first
  ;; layer naming one there. A layer naming one column of two failed
  ;; with "pj/plan got 1.6 for :x, which is past the ends of this axis".
  (testing "a layer naming one column reads the other from the first layer"
    (is (= [[:point :displ :cty] [:point :displ :hwy]]
           (draft-columns (-> cars (pj/lay-point :displ :cty) (pj/lay-point {:y :hwy})))))
    (is (= [[:point :displ :cty] [:point :hwy :cty]]
           (draft-columns (-> cars (pj/lay-point :displ :cty) (pj/lay-point {:x :hwy})))))
    (is (= {:panels 1 :points 10}
           (select-keys (pj/svg-summary (-> cars
                                            (pj/lay-point :displ :cty)
                                            (pj/lay-point {:y :hwy})))
                        [:panels :points]))))
  (testing "axis by axis: the layer naming :y first reads the :x a later layer names"
    (is (= [[:point :displ :hwy] [:point :displ :cty]]
           (draft-columns (-> cars pj/pose (pj/lay-point {:y :hwy}) (pj/lay-point :displ :cty))))))
  (testing "a layer naming no column reads both, as before"
    (is (= [[:point :displ :hwy] [:line :displ :hwy]]
           (draft-columns (-> cars (pj/lay-point :displ :hwy) pj/lay-smooth)))))
  (testing "a layer type that can draw from one column is given no second column"
    ;; Written into the pose, the first layer's :y would reach the bar
    ;; by scope and turn its count into a bar per row.
    (let [p (-> cars (pj/lay-point :class :hwy) (pj/lay-bar :class))]
      (is (= [[:point :class :hwy] [:rect :class nil]]
             (binding [*out* (java.io.StringWriter.)] (draft-columns p))))
      (is (= 3 (:polygons (binding [*out* (java.io.StringWriter.)] (pj/svg-summary p))))))
    (is (= [[:point :displ :hwy] [:rug :cty nil]]
           (draft-columns (-> cars (pj/lay-point :displ :hwy) (pj/lay-rug {:x :cty}))))))
  (testing "the pose's own column reaches a layer by scope and is not written into it"
    (let [e (pose/elaborate (-> cars (pj/pose {:x :displ}) (pj/lay-point {:y :hwy}) pj/lay-smooth))]
      (is (= {:x :displ} (:mapping e)))
      (is (= [{:y :hwy} {:y :hwy}] (mapv :mapping (:layers e))))))
  (testing "a scale specified beside the first layer's column comes with the column"
    (is (= {:x {:column :displ :scale {:type :log}} :y :hwy}
           (-> cars
               (pj/lay-point {:x {:column :displ :scale {:type :log}} :y :cty})
               (pj/lay-point {:y :hwy})
               pose/elaborate
               :layers second :mapping)))))

(deftest the-columns-a-layer-reads-are-written-into-the-elaborated-pose-test
  (let [p (-> cars (pj/lay-point :displ :cty) (pj/lay-line {:y :hwy}) pj/lay-smooth)
        e (pose/elaborate p)]
    (testing "into the layers, and not into the pose's mapping"
      (is (nil? (:mapping e)))
      (is (= [{:x :displ :y :cty} {:x :displ :y :hwy} {:x :displ :y :cty}]
             (mapv :mapping (:layers e)))))
    (testing "elaboration is idempotent, and the elaborated pose draws the same"
      (is (= e (pose/elaborate e)))
      (is (= (pj/svg-summary p) (pj/svg-summary e))))
    (testing "a layer placed in the drawing area reads drawing units, not columns"
      (is (= {:x 0.5 :y 0.5 :text "n" :in :drawing-area}
             (-> cars
                 (pj/lay-point :displ :hwy)
                 (pj/lay-text {:x 0.5 :y 0.5 :text "n" :in :drawing-area})
                 pose/elaborate
                 :layers second :mapping))))))

(deftest a-facet-over-columns-makes-its-cells-from-the-panels-columns-test
  ;; A layer naming both columns drew its own panel and a histogram on
  ;; its side for the other column; with a smooth it failed with a
  ;; NullPointerException.
  (testing "the panel no layer names is inferred from the panel's column and the facet's"
    (doseq [[facet cols] [[{:y [:hwy :cty]} [[:point :displ :hwy] [:point :displ :cty]]]
                          [{:x [:displ :cty]} [[:point :displ :hwy] [:point :cty :hwy]]]]]
      (let [p (-> cars (pj/lay-point :displ :hwy) (pj/facet facet))]
        (is (= cols (draft-columns p)))
        (is (= [[:point] [:point]] (panel-marks (pj/plan p)))))))
  (testing "a layer naming no column is drawn in every panel"
    (let [p (-> cars (pj/lay-point :displ :hwy) pj/lay-smooth (pj/facet {:y [:hwy :cty]}))]
      (is (= [[:point :line] [:line]] (panel-marks (pj/plan p))))
      (is (= [[:point :displ :hwy] [:line :displ :hwy] [:line :displ :cty]]
             (draft-columns p)))))
  (testing "the facet written first, then the layer"
    (is (= [[:point] [:point]]
           (panel-marks (pj/plan (-> cars (pj/facet {:y [:hwy :cty]}) (pj/lay-point :displ :hwy)))))))
  (testing "two layers each naming both columns take a panel each"
    (is (= [[:point] [:line]]
           (panel-marks (pj/plan (-> cars
                                     (pj/lay-point :displ :hwy)
                                     (pj/lay-line :displ :cty)
                                     (pj/facet {:y [:hwy :cty]}))))))))
