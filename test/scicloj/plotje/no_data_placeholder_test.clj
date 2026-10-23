(ns scicloj.plotje.no-data-placeholder-test
  "The 'no data' placeholder is drawn over panels whose layers
   render no geometry. A rule or a band draws geometry like any other
   mark, so a panel carrying one is not empty. The check
   is driven by the rendered output, so it works for built-in
   marks that store geometry in different slots (:groups, :boxes,
   :violins, :ridges, :tiles, :levels) and for any extension mark
   that stores geometry under a custom slot."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.extract :as extract]
            [scicloj.plotje.impl.stat :as stat]
            [scicloj.plotje.layer-type :as layer-type]
            [scicloj.plotje.render.mark :as mark]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [membrane.ui :as ui]))

(defn- has-no-data-text? [pose]
  (boolean (some #{"no data"} (:texts (pj/svg-summary pose)))))

(deftest valid-boxplot-does-not-show-no-data
  (testing "boxplot with valid :boxes is not flagged as empty"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-boxplot :species :sepal-width))]
      (is (not (has-no-data-text? pose))))))

(deftest valid-violin-does-not-show-no-data
  (testing "violin with valid :violins is not flagged as empty"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-violin :species :sepal-width))]
      (is (not (has-no-data-text? pose))))))

(deftest valid-ridgeline-does-not-show-no-data
  (testing "ridgeline with valid :ridges is not flagged as empty"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-ridgeline :species :sepal-width))]
      (is (not (has-no-data-text? pose))))))

(deftest valid-tile-does-not-show-no-data
  (testing "tile with valid :tiles is not flagged as empty"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-tile :sepal-length :sepal-width))]
      (is (not (has-no-data-text? pose))))))

(deftest valid-density-2d-does-not-show-no-data
  (testing "density-2d with valid :tiles is not flagged as empty"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-density-2d :sepal-length :sepal-width))]
      (is (not (has-no-data-text? pose))))))

(deftest valid-contour-does-not-show-no-data
  (testing "contour with valid :levels is not flagged as empty"
    (let [pose (-> (rdatasets/datasets-iris)
                   (pj/lay-contour :sepal-length :sepal-width))]
      (is (not (has-no-data-text? pose))))))

(deftest dashboard-with-boxplot-cell-does-not-show-no-data
  (testing "the composition.clj dashboard example renders without 'no data'"
    (let [iris (rdatasets/datasets-iris)
          dashboard (pj/arrange
                     [[(-> iris (pj/lay-histogram :sepal-length))
                       (-> iris (pj/lay-boxplot :species :sepal-width
                                                {:color :species}))]
                      [(-> iris (pj/lay-point :petal-length :petal-width
                                              {:color :species}))
                       (-> iris (pj/lay-density :petal-length
                                                {:color :species}))]])]
      (is (not (has-no-data-text? dashboard))))))

(deftest empty-data-still-shows-no-data
  (testing "the placeholder still fires when layers truly have no geometry"
    (let [pose (-> {:x [1.0 2.0 3.0] :y [##NaN ##NaN ##NaN]}
                   (pj/lay-point :x :y))]
      (is (has-no-data-text? pose)
          "all-NaN y collapses :groups to empty -> 'no data' should appear"))))

(deftest a-layer-with-no-data-shows-no-data-whatever-its-type
  ;; A stat that reads its dataset used to fail on a pose with no data,
  ;; with a protocol error naming nil, while a layer whose stat passes
  ;; the rows through drew the placeholder.
  (doseq [layer-type [:area :bar :boxplot :contour :density :density-2d
                      :errorbar :histogram :label :line :lollipop :point
                      :ridgeline :rug :smooth :step :summary :text :tile
                      :violin]]
    (testing (str layer-type " on a pose with no data")
      (is (has-no-data-text? (pj/pose {:mapping {:x :a :y :b}
                                       :layers [{:layer-type layer-type}]})))))
  (testing "a template draws once it has its data"
    (is (= 1 (:lines (pj/svg-summary
                      (-> (pj/pose)
                          (pj/lay-smooth :a :b)
                          (pj/with-data {:a [1 2 3 4 5 6] :b [2 3 1 5 4 6]}))))))))

(deftest a-new-cell-with-no-data-prints-a-note
  (let [d {:a [1 2 3 4 5 6] :b [2 3 1 5 4 6] :c [3 1 2 6 5 4]}
        arranged (pj/arrange [(pj/lay-point d :a :b) (pj/lay-point d :a :c)])]
    (testing "the cells carry their own data, and the layer names columns no cell draws"
      (let [note (with-out-str (pj/lay-smooth arranged :b :c))]
        (is (re-find #"Note: pj/lay-smooth names :b and :c" note))
        (is (re-find #"That cell has no data" note))
        (is (re-find #"the cells draw :a / :b, :a / :c" note))
        (is (re-find #"\(pj/lay-smooth pose :b :c \{:data data\}\)" note)))
      (is (has-no-data-text? (binding [*out* (java.io.StringWriter.)]
                               (pj/lay-smooth arranged :b :c)))))
    (testing "no Note where the layer has data, joins a cell, or the root has data"
      (is (= "" (with-out-str (pj/lay-smooth arranged :b :c {:data d}))))
      (is (= "" (with-out-str (pj/lay-smooth arranged :a :c))))
      (is (= "" (with-out-str (-> d
                                  (pj/arrange [[:a :b] [:a :c]])
                                  pj/lay-point
                                  (pj/lay-smooth :b :c))))))))

(deftest extension-with-custom-slot-does-not-show-no-data
  (testing "an extension that stores geometry in a custom slot
            (here :bars, mirroring waterfall_extension.clj) is
            recognized by the rendered-marks check"
    (try
      (defmethod stat/compute-stat ::custom [{:keys [data x y]}]
        {:bars (vec (for [row (range (count (data x)))]
                      {:cat (nth (data x) row)
                       :val (double (nth (data y) row))}))
         :x-domain (vec (distinct (data x)))
         :y-domain [0.0 (apply max (data y))]})
      (defmethod extract/extract-layer ::custom [_ stat _ _]
        {:mark ::custom :style {} :bars (:bars stat)})
      (defmethod mark/layer->membrane ::custom [layer ctx]
        (let [{:keys [sx sy]} ctx]
          (vec (for [{:keys [cat val]} (:bars layer)
                     :let [band (sx cat true)
                           x0 (:rstart band) x1 (:rend band)
                           y0 (sy 0.0) y1 (sy val)]]
                 (ui/with-color [0.3 0.5 0.8 1.0]
                   (ui/path [x0 y0] [x1 y0] [x1 y1] [x0 y1]))))))
      (layer-type/register! ::custom {:mark ::custom :stat ::custom})
      (let [pose (-> {:c ["A" "B" "C"] :v [1.0 2.0 3.0]}
                     (pj/pose :c :v)
                     (pj/lay (layer-type/lookup ::custom)))]
        (is (not (has-no-data-text? pose))))
      (finally
        (remove-method stat/compute-stat ::custom)
        (remove-method extract/extract-layer ::custom)
        (remove-method mark/layer->membrane ::custom)
        (swap! @(resolve 'scicloj.plotje.layer-type/registry*)
               dissoc ::custom)))))

(deftest smooth-groups-with-too-few-rows-are-warned-about-test
  ;; A smooth draws nothing for a group with fewer rows than its fit
  ;; needs -- 4 for loess, 3 for a straight line. With every group
  ;; dropped the panel reads "no data" beside a legend of four entries;
  ;; the warning names the groups, once per plan.
  (let [sales {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"]
               :week [1 2 3 4 5 6 7 8]
               :units [100 120 90 140 70 85 110 95]}
        printed (fn [pose] (with-out-str (pj/plan pose)))
        loess (printed (pj/lay-smooth sales :week :units {:color :quarter}))
        lm (printed (pj/lay-smooth sales :week :units {:color :quarter :stat :linear-model}))]
    (is (= (str "Warning: a smooth with :stat :loess fits a curve from at least 4 rows"
                " per group, so it draws nothing for the groups Q1, Q2, Q3 and Q4 of"
                " :quarter, which have 2 rows each.\n")
           loess))
    (is (re-find #":stat :linear-model fits a line from at least 3 rows per group" lm))
    (is (= "Warning: a smooth with :stat :loess fits a curve from at least 4 rows, and the layer has 3 rows, so it draws nothing.\n"
           (printed (pj/lay-smooth {:week [1 2 3] :units [4 6 5]} :week :units))))
    ;; Enough rows: nothing printed.
    (is (= "" (printed (pj/lay-smooth sales :week :units))))))
