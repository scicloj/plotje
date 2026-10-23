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
        (is (= ["b" "a" "c"] (legend (pj/facet-grid plain nil :g))))))
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
      (is (= (pj/facet p :drv :row share) (-> p (pj/facet :drv :row) (pj/options share))))
      (is (= (pj/facet p {:row :drv} share) (-> p (pj/facet {:row :drv}) (pj/options share))))
      (is (= (pj/facet-grid p :drv :cyl share) (-> p (pj/facet-grid :drv :cyl) (pj/options share)))))
    (testing "and it is read"
      (is (= 1 (y-domains (pj/facet p :drv))))
      (is (= 3 (y-domains (pj/facet p :drv share)))))
    (testing "a direction that is neither :col nor :row still reports an error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"direction must be :col or :row"
                            (pj/facet p :drv :sideways))))))

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
      (is (re-find #"in one column" (ex-message (report (pj/facet p :model :row))))))
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
               (select-keys (pj/svg-summary (-> (pj/cross-matrix d3 [:a :b])
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
