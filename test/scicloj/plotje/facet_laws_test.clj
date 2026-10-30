(ns scicloj.plotje.facet-laws-test
  "Laws of the facet and of `pj/pose`, checked over several datasets:
   a facet draws what its hand-written cells draw; expanding a facet and
   collapsing it again gives the pose back; a verb written before or
   after `pj/facet` gives an equal pose; `pj/pose` is idempotent."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.api :as tc]))

(def cases
  "A pose to facet, and the category column to facet it by."
  [["iris by species" (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width) :species]
   ["mpg by drv" (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy) :drv]
   ["mpg by cyl, a number" (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy) :cyl]
   ["mtcars by gear, histogram" (pj/lay-histogram (rdatasets/datasets-mtcars) :mpg) :gear]
   ["small, coloured" (pj/lay-point {:x [1 2 3 4 5 6] :y [2 1 4 3 6 5]
                                     :g ["a" "b" "a" "c" "b" "c"]}
                                    :x :y {:color :g})
    :g]])

(defn- cells-of
  "The facet written out by hand: one cell per value, in data order."
  [base col]
  (let [d (:data base)
        ds (if (tc/dataset? d) d (tc/dataset d))
        values (distinct (ds col))]
    (assoc base
           :layout {:direction :horizontal}
           :poses (mapv (fn [v] {:where {col v}}) values))))

(deftest a-facet-draws-what-its-cells-draw-test
  (doseq [[label base col] cases]
    (testing label
      (is (= (pj/plot (pj/facet base col))
             (pj/plot (cells-of base col)))))))

(deftest expanding-and-collapsing-gives-the-pose-back-test
  (doseq [[label base col] cases
          :let [faceted (pj/facet base col)
                expanded (pose/expand-facets faceted)]]
    (testing label
      (is (pose/composite? expanded) "the facet expands to cells")
      (is (= (:mapping faceted) (:mapping (pose/collapse-regular-split expanded))))
      (is (= (pj/plot faceted) (pj/plot expanded))))))

(deftest a-verb-before-or-after-the-facet-test
  (doseq [[label base col] cases
          :when (not= :histogram (:layer-type (first (:layers base))))]
    (testing label
      (is (= (-> base (pj/facet col) (pj/options {:title "t"}))
             (-> base (pj/options {:title "t"}) (pj/facet col))))
      (is (= (pj/plot (-> base (pj/facet col) pj/lay-smooth))
             (pj/plot (-> base pj/lay-smooth (pj/facet col)))))))
  (testing "a facet over columns"
    (let [mpg (rdatasets/ggplot2-mpg)]
      (is (= (-> mpg (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]}))
             (-> mpg (pj/pose {:x :displ}) (pj/facet {:y [:hwy :cty]}) pj/lay-point))))))

(deftest a-facet-keeps-the-legend-of-the-unfaceted-plot-test
  (let [d {:x [1 2 3 4 5 6] :y [1 2 3 4 5 6]
           :c ["b" "a" "c" "b" "c" "b"]
           :k [:kb :ka :kc :kb :kc :kb]
           :g ["p2" "p1" "p2" "p1" "p2" "p2"]}
        legend (fn [pose] (mapv :label (:entries (:legend (pj/plan pose)))))
        colors (fn [pose] (mapv :color (:entries (:legend (pj/plan pose)))))]
    (testing "the colour legend is in the order of the data's rows, not panel by panel"
      (let [plain (pj/lay-point d :x :y {:color :c})]
        (is (= ["b" "a" "c"] (legend plain) (legend (pj/facet plain :g))))
        (is (= (colors plain) (colors (pj/facet plain :g))))
        (is (= ["b" "a" "c"] (legend (pj/facet plain {:col nil :row :g}))))))
    (testing "the shape legend too"
      (is (= [["b" :circle] ["a" :square] ["c" :triangle]]
             (->> (-> d (pj/lay-point :x :y {:shape :c}) (pj/facet :g))
                  pj/plan :shape-legend :entries
                  (mapv (juxt :label :shape))))))
    (testing "counted bars"
      (is (= ["b" "a" "c"] (legend (-> d (pj/lay-bar :g {:color :c}) (pj/facet :g))))))
    (testing "a categorical axis of a keyword column"
      (is (= ["kb" "ka" "kc"]
             (-> d (pj/lay-point :k :y) (pj/facet :g) pj/plan :panels first :x-domain vec))))
    (testing "a specified :domain decides"
      (is (= ["c" "a" "b"]
             (legend (-> d
                         (pj/lay-point :x :y {:color :c})
                         (pj/facet :g)
                         (pj/scale :color {:domain ["c" "a" "b"]}))))))))

(deftest options-written-in-the-facet-call-test
  (let [mpg (rdatasets/ggplot2-mpg)
        p (pj/lay-point mpg :displ :hwy)
        share {:share-scales #{:x}}
        y-domains (fn [pose] (count (distinct (map :y-domain (:panels (pj/plan pose))))))]
    (testing "a trailing map is what pj/options takes"
      (is (= (pj/facet p :drv share) (-> p (pj/facet :drv) (pj/options share))))
      (is (= (pj/facet p {:row :drv} share) (-> p (pj/facet {:row :drv}) (pj/options share))))
      (is (= (pj/facet p {:col :drv :row :cyl} share) (-> p (pj/facet {:col :drv :row :cyl}) (pj/options share)))))
    (testing "and it is read"
      (is (= 1 (y-domains (pj/facet p :drv))))
      (is (= 3 (y-domains (pj/facet p :drv share)))))
    (testing "a keyword after the column that is neither :col nor :row reports an error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"was given :sideways after the column.*\(pj/facet pose \{:row :drv\}\)"
                            (pj/facet p :drv :sideways))))))

(deftest the-direction-argument-is-retired-test
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
        out (fn [f] (with-out-str (f)))]
    (testing "it draws as the map does, and warns naming the map"
      (let [printed (out #(is (= (pj/plot (pj/facet p :drv :row))
                                 (pj/plot (pj/facet p {:row :drv})))))]
        (is (re-find #"direction argument is retired\. Instead of \(pj/facet pose :drv :row\), write \(pj/facet pose \{:row :drv\}\)"
                     printed)))
      (is (re-find #"write \(pj/facet pose :drv\)" (out #(pj/facet p :drv :col))))
      (is (re-find #"retired" (out #(pj/facet p :drv :row {:share-scales #{:x}})))))
    (testing "the shorthand and the map print nothing"
      (is (= "" (out #(pj/facet p :drv))))
      (is (= "" (out #(pj/facet p {:row :drv}))))
      (is (= "" (out #(pj/facet p :drv {:share-scales #{:x}}))))
      (is (= "" (out #(pj/facet p {:col :drv :row :cyl})))))
    (testing "under :strict it reports an error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"direction argument is retired"
                            (pj/with-config {:strict true} (pj/facet p :drv :row)))))))

(deftest a-key-the-facet-map-does-not-read-test
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
        msg (fn [node] (try (pj/facet p node) nil
                            (catch clojure.lang.ExceptionInfo e (ex-message e))))]
    (testing "a plot option, with where to write it"
      (is (re-find #"unexpected key\(s\): \[:share-scales\].*It takes \[:col :row\].*in a map of its own after this one"
                   (msg {:col :drv :share-scales #{:x}})))
      (is (re-find #"It takes \[:x :y\]" (msg {:y [:cty :hwy] :share-scales #{}}))))
    (testing "columns beside a grid, a mapping, and a key of no kind"
      (is (re-find #"the columns of :y go inside :col or :row" (msg {:col :drv :y [:cty :hwy]})))
      (is (re-find #":color is a mapping" (msg {:col :drv :color :class})))
      (is (re-find #"unexpected key\(s\): \[:cols\]" (msg {:col :drv :cols 2}))))
    (testing "inside a part of a grid"
      (is (re-find #"its :col part has unexpected key\(s\): \[:foo\]"
                   (msg {:col {:x [:displ :cyl] :foo 1} :row {:y [:hwy]}}))))
    (testing "a map of none of the four keys keeps the column message"
      (is (re-find #"takes a column of the data" (msg {:value :g}))))
    (testing "the maps the facet reads are unchanged"
      (is (= {:col :drv} (select-keys (:mapping (pj/facet p {:col :drv})) [:col :row])))
      (is (= {:col :drv :row :cyl}
             (select-keys (:mapping (pj/facet p {:col :drv :row :cyl})) [:col :row]))))))

(deftest nil-cancels-the-facet-from-above-test
  (let [mpg (rdatasets/ggplot2-mpg)
        cell (fn [f] (f (pj/lay-point (pj/pose mpg :displ :hwy))))
        panels (fn [f] (->> (-> (pj/arrange [(cell f) (cell identity)]) (pj/facet :drv))
                            pj/plan :sub-plots
                            (mapv #(count (:panels (:plan %))))))]
    (is (= [3 3] (panels identity)))
    (is (= [1 3] (panels #(pj/facet % nil))))
    (is (= [1 3] (panels #(pj/facet % {:col nil}))) "the map says the same")
    (testing "on the pose that wrote the facet there is nothing above to cancel"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"nil cancels the facet of a pose above.*leave out the earlier pj/facet call"
                            (-> (pj/lay-point mpg :displ :hwy) (pj/facet :drv) (pj/facet nil)))))))

(deftest too-many-facet-panels-names-the-facet-test
  (let [mpg (rdatasets/ggplot2-mpg)
        p (pj/lay-point mpg :displ :hwy)
        report (fn [pose] (try (pj/plan pose) nil (catch clojure.lang.ExceptionInfo e e)))]
    (testing "across and down"
      (let [e (report (pj/facet p :model))]
        (is (re-find #"a facet by :model, which holds 38 values and draws them in one row"
                     (ex-message e)))
        (is (re-find #"does not wrap its panels.*\(pj/arrange poses \{:cols 4 :height 1200\}\)" (ex-message e)))
        (is (= {:facet :model :values 38} (select-keys (ex-data e) [:facet :values]))))
      (is (re-find #"in one column" (ex-message (report (pj/facet p {:row :model}))))))
    (testing "a plot too small with no facet keeps its message"
      (is (not (re-find #"facet" (ex-message (report (pj/options p {:width 30})))))))
    (testing "the suggested form draws"
      (is (= 38 (:panels (pj/svg-summary
                          (pj/arrange (mapv (fn [m] (pj/lay-point (tc/select-rows mpg #(= m (:model %)))
                                                                  :displ :hwy))
                                            (distinct (:model mpg)))
                                      {:cols 4 :height 1200}))))))))

(deftest pose-is-idempotent-test
  (doseq [[label base _] cases]
    (testing label
      (is (= base (pj/pose base)))
      (is (= base (pj/pose base {})))
      (let [m {:alpha 0.5}]
        (is (= (pj/pose base m) (pj/pose (pj/pose base m) m)))))))

(deftest a-layer-of-one-facet-column-stays-in-its-panel-test
  (let [mpg (rdatasets/ggplot2-mpg)
        y-domains (fn [p] (mapv (comp :y-domain first :panels pj/plan)
                                (:poses (pose/expand-facets p))))]
    (testing "a layer naming :hwy is not drawn in the :cty panel"
      (let [p (-> mpg (pj/pose {:x :displ}) (pj/lay-point {:y :hwy}) (pj/facet {:y [:hwy :cty]}))
            summary (pj/svg-summary p)]
        (is (= 2 (:panels summary)))
        (is (some #{"cty"} (:texts summary)))
        (is (= (y-domains p)
               (y-domains (-> mpg (pj/pose {:x :displ}) pj/lay-point (pj/facet {:y [:hwy :cty]})))))))))

(deftest a-layer-goes-to-the-cells-whose-columns-it-draws-test
  (let [iris (rdatasets/datasets-iris)
        cols [:sepal-length :sepal-width]
        matrix (-> iris (pj/pose {:color :species}) (pj/facet {:col {:x cols} :row {:y cols}}))
        diagonal-marks (fn [p] (->> (:sub-plots (pj/plan p))
                                    (keep (fn [{:keys [path plan]}]
                                            (when (apply = path)
                                              (-> plan :panels first :layers first :mark))))
                                    vec))]
    (testing "a point layer on a matrix leaves the diagonal to inference"
      (is (= [:bar :bar] (diagonal-marks (pj/lay-point matrix))))
      (is (= (pj/svg-summary matrix) (pj/svg-summary (pj/lay-point matrix)))))
    (testing "a density goes to the diagonal, the points elsewhere"
      (is (= [:area :area] (diagonal-marks (-> matrix pj/lay-point pj/lay-density)))))
    (testing "a layer that fits no cell is drawn in every cell"
      (let [s (pj/svg-summary (-> iris (pj/pose {:x :sepal-length :y :sepal-width})
                                  pj/lay-histogram (pj/facet :species)))]
        (is (= 3 (:panels s)))
        (is (zero? (:points s)))
        (is (pos? (:polygons s)))))))

(deftest a-facet-over-columns-names-its-columns-for-inference
  ;; Data of three columns infers :x, :y and :color by column order, and
  ;; a pose naming :x and :y infers none. A facet over columns names
  ;; them too: before, the matrix below coloured every panel by :c and
  ;; printed three warnings about a numeric :color on the densities.
  (let [d3 {:a [1 2 3 4 5] :b [2 1 4 3 5] :c [10 20 30 40 50]}
        warnings (fn [pose] (count (re-seq #"Warning" (with-out-str (pj/plan pose)))))
        legends (fn [pose] (keep #(-> % :plan :legend) (:sub-plots (pj/plan pose))))]
    (testing "a matrix of two of three columns has no colour"
      (let [matrix (-> (pj/pose d3)
                       (pj/facet {:col {:x [:a :b]} :row {:y [:a :b]}})
                       pj/lay-point
                       pj/lay-density)]
        (is (zero? (warnings matrix)))
        (is (empty? (legends matrix)))
        ;; Its marks are those of the matrix of the same two columns;
        ;; the tick labels on the left differ (decision 3.2).
        (is (= (select-keys (pj/svg-summary matrix) [:points :polygons :colors :panels])
               (select-keys (pj/svg-summary (-> (pj/facet d3 {:col {:x [:a :b]} :row {:y [:a :b]}})
                                                pj/lay-point
                                                pj/lay-density))
                            [:points :polygons :colors :panels])))))
    (testing "the same pose with no facet still colours by the third column"
      (is (= :c (-> (pj/pose d3) pj/lay-point pj/plan :legend :title))))
    (testing "a facet over :y alone takes its :x from a column it does not name"
      ;; As `(pj/lay-point data {:y :a})` draws :a against a column
      ;; nothing names.
      (is (= [[:c :a] [:c :b]]
             (mapv #(-> % :draft first ((juxt :x :y)))
                   (:sub-drafts (pj/draft (-> (pj/pose d3) (pj/facet {:y [:a :b]}) pj/lay-point)))))))))

(deftest a-category-facet-and-a-facet-over-columns-compose-test
  ;; Composed, the two drew a facet over columns nested in each
  ;; category's panel, with the strips repeated. Every spelling of the
  ;; grid gives one pose -- the categories in the mapping, the columns
  ;; under `:facet` -- and drawing crosses the two.
  (let [base (-> (rdatasets/datasets-iris) (pj/pose {:x :sepal-length}) pj/lay-point)
        ys [:sepal-width :petal-width]
        one (pj/facet base {:col :species :row {:y ys}})]
    (is (= {:x :sepal-length :col :species} (:mapping one)))
    (is (= {:y ys} (:facet one)))
    (doseq [two [(-> base (pj/facet {:y ys}) (pj/facet :species))
                 (-> base (pj/facet :species) (pj/facet {:y ys}))
                 (-> base (pj/facet {:row {:y ys}}) (pj/facet :species))
                 (-> base (pj/facet :species) (pj/facet {:row {:y ys}}))
                 (-> base (pj/facet {:y ys}) (pj/pose {:col :species}))
                 (-> base (pj/pose {:col :species}) (pj/facet {:y ys}))]]
      (is (= one two)))
    (is (= 6 (:panels (pj/svg-summary one))))
    (testing "categories down put the columns across"
      (let [down (-> base (pj/facet {:row :species}) (pj/facet {:y ys}))]
        (is (= {:col {:y ys}} (:facet down)))
        (is (= down (pj/facet base {:row :species :col {:y ys}})))))
    (testing "pairs of columns compose the same way"
      (let [pairs {:x [:sepal-length :petal-length] :y ys}
            p (pj/lay-point (rdatasets/datasets-iris) :sepal-length :sepal-width)]
        (is (= (pj/facet p {:col :species :row pairs})
               (-> p (pj/facet pairs) (pj/facet :species))))))
    (testing "a facet written on an arrangement crosses a cell's facet over columns"
      (let [cell (pj/facet base {:y ys})]
        (is (= (pj/plot (pj/facet (pj/arrange [cell cell]) :species))
               (pj/plot (pj/arrange [(pj/facet cell :species) (pj/facet cell :species)]))))))
    (testing "a third facet has no direction left"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"both directions"
                            (pj/facet one {:row :species})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"both directions"
                            (-> base (pj/facet {:col :species :row :species}) (pj/facet {:y ys})))))))

(deftest retired-facet-grid-warns-and-writes-the-map-test
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
    (testing "pj/facet-grid warns, naming the facet to write, and returns its pose"
      (let [printed (with-out-str
                      (is (= (pj/facet p {:col :drv :row :cyl})
                             (pj/facet-grid p :drv :cyl)))
                      (is (= (pj/facet p :drv)
                             (pj/facet-grid p :drv nil))))]
        (is (re-find #"pj/facet-grid is retired\. Write \(pj/facet pose \{:col :drv :row :cyl\}\)" printed))
        (is (re-find #"Write \(pj/facet pose :drv\)" printed))))
    (testing "under :strict it reports an error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"pj/facet-grid is retired"
                            (pj/with-config {:strict true} (pj/facet-grid p :drv :cyl)))))))

(deftest retired-names-report-in-one-shape-test
  ;; Under :strict each retired name throws a message that stays true
  ;; when it throws (nothing about what is drawn meanwhile), and
  ;; ex-data naming what is retired and the form to write instead.
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
        thrown (fn [f] (try (pj/with-config {:strict true} (f)) nil
                            (catch clojure.lang.ExceptionInfo e
                              [(ex-message e) (select-keys (ex-data e) [:retired :to])])))]
    (doseq [[f retired to] [[#(pj/facet-grid p :drv :cyl) :facet-grid {:col :drv :row :cyl}]
                            [#(pj/facet p :drv :row) :direction {:row :drv}]
                            [#(pj/overlay p) :overlay nil]
                            [#(pj/options p {:scales :free-y}) :scales {:share-scales #{:x}}]]]
      (let [[msg data] (thrown f)]
        (is (= {:retired retired :to to} data))
        (is (not (re-find #"ignored|for now|as before|is read as|dropped" msg)) msg)))))

(deftest a-coloured-scatter-plot-matrix-draws-one-legend-test
  ;; The diagonal draws a histogram the coloured layer does not reach;
  ;; it maps nothing to :color, and the matrix keeps one legend.
  (let [cols [:sepal-length :sepal-width :petal-length]
        texts (fn [v] (:texts (pj/svg-summary v)))
        on-layer (-> (rdatasets/datasets-iris)
                     (pj/facet {:col {:x cols} :row {:y cols}})
                     (pj/lay-point {:color :species}))]
    (is (= 1 (count (filter #{"species"} (texts on-layer)))))
    (testing "an arrangement keeps a legend beside its coloured cell"
      (let [mpg (rdatasets/ggplot2-mpg)]
        (is (= 1 (count (filter #{"drv"} (texts (pj/arrange [(pj/lay-point mpg :displ :hwy {:color :drv})
                                                             (pj/lay-point mpg :displ :cty)]))))))))))

(deftest facet-messages-name-what-can-be-written-test
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
    (testing "nil options are no options"
      (is (= (pj/facet p :drv) (pj/facet p :drv nil))))
    (testing "the other direction is offered only when it is free"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"write the other direction, \(pj/facet pose \{:row :cyl\}\)"
                            (-> p (pj/facet :drv) (pj/facet :cyl))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #":row is taken by :cyl: map :year to an aesthetic"
                            (-> p (pj/facet {:col :drv :row :cyl}) (pj/facet :year)))))
    (testing "one column inside a grid names the vector form"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"names its columns as a vector"
                            (pj/facet (rdatasets/ggplot2-mpg) {:col {:x :displ} :row {:y :hwy}}))))))

(deftest columns-by-columns-as-two-calls-test
  (let [p (pj/lay-point (pj/pose (rdatasets/ggplot2-mpg)))
        one (pj/facet p {:col {:x [:displ :cty]} :row {:y [:hwy :cty]}})]
    (testing "a second facet over columns naming the other direction crosses the first"
      (is (= one
             (-> p (pj/facet {:col {:x [:displ :cty]}}) (pj/facet {:row {:y [:hwy :cty]}}))
             (-> p (pj/facet {:x [:displ :cty]}) (pj/facet {:row {:y [:hwy :cty]}}))
             (-> p (pj/facet {:row {:y [:hwy :cty]}}) (pj/facet {:col {:x [:displ :cty]}})))))
    (testing "a bare second map would read as pairs, and the error names the direction to write"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"splits its columns once.*\(pj/facet pose \{:row \{:y \[:hwy :cty\]\}\}\)"
                            (-> p (pj/facet {:x [:displ :cty]}) (pj/facet {:y [:hwy :cty]})))))))

(deftest a-facet-over-columns-meets-what-the-pose-already-holds-test
  (let [p (pj/lay-point (pj/pose (rdatasets/ggplot2-mpg)))
        matrix {:col {:x [:displ :hwy]} :row {:y [:displ :hwy]}}]
    (testing "an order for :col written before a facet over columns on :col is reported"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"drawn in the order the columns are written: remove the scale"
                            (-> p (pj/scale :col {:domain [:hwy :displ]}) (pj/facet matrix))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"drawn in the order the columns are written: remove the scale"
                            (-> p (pj/facet matrix) (pj/scale :col {:domain [:hwy :displ]})))))
    (testing "categories written by pj/pose after a matrix are reported at the call"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"both directions"
                            (-> p (pj/facet matrix) (pj/pose {:col :drv})))))
    (testing "pj/pose {:row ..} after a facet over :y gives the pose pj/facet gives"
      (is (= (-> p (pj/pose {:x :displ}) (pj/facet {:y [:hwy :cty]}) (pj/facet {:row :drv}))
             (-> p (pj/pose {:x :displ}) (pj/facet {:y [:hwy :cty]}) (pj/pose {:row :drv})))))))

(deftest an-empty-map-changes-nothing-test
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)
        arr (pj/arrange [p p])]
    (is (= p (pj/options p {})))
    (is (= arr (pj/pose arr {})))
    (is (= (pj/facet arr :drv) (pj/pose arr {:col :drv})))))

(deftest pj-marginal-reads-only-size-test
  (let [p (pj/lay-point (rdatasets/ggplot2-mpg) :displ :hwy)]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"takes only :size.*no mapping"
                          (pj/marginal p :top :density {:color :drv})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"takes only :size.*pj/options"
                          (pj/marginal p :top :density {:title "T"})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"between 0 and 1"
                          (pj/marginal p :top :density {:size 2})))
    (is (pj/pose? (pj/marginal p :top :density {:size 0.3})))))

(deftest strips-of-margins-and-nested-grids-test
  (let [mpg (rdatasets/ggplot2-mpg)
        texts (fn [v] (set (:texts (pj/svg-summary v))))]
    (testing "a matrix with a nil margin names its rows"
      (is (every? (texts (-> (pj/pose mpg) pj/lay-point
                             (pj/facet {:col {:x [:displ :hwy nil]} :row {:y [:displ :hwy nil]}})))
                  ["displ" "hwy"]))
      (is (= 2 (count (filter #{"hwy"} (:texts (pj/svg-summary
                                                (-> (pj/pose mpg) pj/lay-point
                                                    (pj/facet {:col {:x [:displ :hwy nil]}
                                                               :row {:y [:displ :hwy nil]}})))))))))
    (testing "a grid placed as a cell draws its strips, and a nested caption is drawn once"
      (let [cell (-> (pj/pose mpg {:x :displ}) pj/lay-point
                     (pj/facet {:col :drv :row {:y [:hwy :cty]}})
                     (pj/options {:caption "inner cap"}))
            v (pj/arrange [cell (pj/lay-point mpg :displ :hwy)])
            ts (:texts (pj/svg-summary v))]
        (is (every? (set ts) ["f" "4" "r" "hwy" "cty"]))
        (is (= 1 (count (filter #{"inner cap"} ts))))))))

(deftest a-grid-placed-as-a-cell-is-drawn-as-alone-test
  (let [mpg (rdatasets/ggplot2-mpg)
        grid (-> (pj/pose mpg {:x :displ}) pj/lay-point
                 (pj/facet {:col :drv :row {:y [:hwy :cty]}}))
        count-of (fn [v t] (count (filter #{t} (:texts (pj/svg-summary v)))))]
    ;; alone and as a cell, :displ is titled once; beside it the scatter
    ;; titles its own
    (is (= 1 (count-of grid "displ")))
    (is (= 2 (count-of (pj/arrange [grid (pj/lay-point mpg :displ :hwy)]) "displ")))))

(deftest strip-labels-name-the-column-test
  (let [mpg (rdatasets/ggplot2-mpg)
        texts (fn [v] (set (:texts (pj/svg-summary v))))]
    (testing "a facet over categories, a compound key and a mixed grid"
      (is (every? (texts (-> mpg (pj/lay-point :displ :hwy)
                             (pj/facet {:col :drv :row :year} {:strip-labels :both})))
                  ["drv: f" "year: 1999"]))
      (is ((texts (-> mpg (pj/lay-point :displ :hwy)
                      (pj/facet [:drv :year] {:strip-labels :both})))
           "drv / year: f / 1999"))
      (let [ts (texts (-> (pj/pose mpg {:x :displ}) pj/lay-point
                          (pj/facet {:col :drv :row {:y [:hwy :cty]}})
                          (pj/options {:strip-labels :both})))]
        ;; the categories name their column; the strips naming columns do not change
        (is (every? ts ["drv: 4" "hwy" "cty"]))))
    (testing "the default prints the value alone, and another value is an error"
      (is ((texts (-> mpg (pj/lay-point :displ :hwy) (pj/facet :drv))) "f"))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #":strip-labels must be one of"
                            (pj/options (pj/pose mpg) {:strip-labels :name}))))))

(deftest a-compound-key-of-measurements-suggests-a-facet-over-columns-test
  (let [iris (rdatasets/datasets-iris)
        mpg (rdatasets/ggplot2-mpg)
        message (fn [v] (try (pj/plan v) nil
                             (catch clojure.lang.ExceptionInfo e (ex-message e))))
        hint #"For a panel per column, write \(pj/facet pose \{:x \[:petal-length :petal-width\]\}\)"]
    (testing "numerical columns in a compound key name the facet over columns"
      (is (re-find hint (message (-> iris (pj/lay-point :sepal-length :sepal-width)
                                     (pj/facet [:petal-length :petal-width]))))))
    (testing "a single numerical column, or a key holding a category, does not"
      (is (not (re-find #"panel per column"
                        (message (-> iris (pj/lay-point :sepal-length :sepal-width)
                                     (pj/facet :petal-length))))))
      (is (not (re-find #"panel per column"
                        (message (-> mpg (pj/lay-point :displ :hwy)
                                     (pj/facet [:manufacturer :cty])))))))))

(deftest nil-places-in-both-directions-title-each-axis-once-test
  ;; With `nil` across and down, the grid's x holds two `displ` cells
  ;; and a count, so it is not titled once beside the grid; each cell
  ;; whose ticks are dropped for the cell below (x) or at its left (y)
  ;; drops its title too where that cell draws the same column.
  (let [mpg (rdatasets/ggplot2-mpg)
        titles (fn [p] (frequencies (filter #{"displ" "hwy"} (:texts (pj/svg-summary p)))))]
    (is (= {"displ" 1 "hwy" 1}
           (titles (-> mpg (pj/facet {:col {:x [:displ nil]} :row {:y [nil :hwy]}}) pj/lay-point))))
    (testing "as with nil in one direction"
      (is (= {"displ" 1 "hwy" 1}
             (titles (-> mpg (pj/facet {:col {:x [:displ]} :row {:y [nil :hwy]}}) pj/lay-point)))))))
