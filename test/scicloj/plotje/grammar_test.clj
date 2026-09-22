(ns scicloj.plotje.grammar-test
  "Tests for the :grammar plot option -- the three answers to what a
   bare vector of column names under an aesthetic means.

   :layered (the default) reads a bare vector on :x or :y as a series,
   pivoted and drawn as layers on one panel, and one on :group, :col or
   :row as a compound key. :paneled reads a series as a panel per
   column, which pj/overlay puts back on one panel. :written-out
   refuses a bare vector under any aesthetic and asks for the
   distinction written out.

   Two pathways that were surveyed and set aside in
   dev-notes/combination-spellings-2026-09-21.md as options G and I,
   built here so each can be run rather than argued about."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.pose :as pose]
            [tablecloth.api :as tc]))

(def ds
  (tc/dataset {:t    [1 2 3 4]
               :a    [10 20 30 40]
               :b    [5 15 25 35]
               :part ["l" "l" "r" "r"]
               :dim  ["w" "h" "w" "h"]}))

(defn- panels [pose] (count (:panels (pj/plan pose))))

(defn- legend [pose]
  (let [l (:legend (pj/plan pose))]
    [(:title l) (mapv :label (:entries l))]))

(defn- strip-labels
  "The facet strip text a plot draws, read off the rendered SVG. A
   strip label is not on the plan's panels -- it is drawn from the
   facet variant -- so the picture is what says whether the panels are
   labelled by column."
  [pose]
  (let [svg (pj/plot pose)]
    (->> (tree-seq coll? seq svg)
         (filter string?)
         set)))

;; ---- :layered, the default ----

(deftest layered-is-the-default
  (testing "a bare vector on :y is a series drawn as layers on one panel"
    (is (= 1 (panels (-> ds (pj/lay-line :t [:a :b])))))
    (is (= [:series ["a" "b"]] (legend (-> ds (pj/lay-line :t [:a :b]))))))
  (testing "writing the default out changes nothing"
    (is (= 1 (panels (-> ds
                         (pj/options {:grammar :layered})
                         (pj/lay-line :t [:a :b])))))
    (is (= [:series ["a" "b"]]
           (legend (-> ds
                       (pj/options {:grammar :layered})
                       (pj/lay-line :t [:a :b]))))))
  (testing "a bare vector on :group is a compound key"
    (is (= 1 (panels (-> ds (pj/lay-line :t :a {:group [:part :dim]})))))))

;; ---- :paneled -- option G ----

(deftest paneled-draws-a-panel-per-column
  (testing "a series written in a lay-* call"
    (is (= 2 (panels (-> ds
                         (pj/options {:grammar :paneled})
                         (pj/lay-line :t [:a :b]))))))
  (testing "a series written in the pose's own mapping"
    (is (= 2 (panels (-> ds
                         (pj/options {:grammar :paneled})
                         (pj/pose {:x :t :y [:a :b]})
                         pj/lay-line)))))
  (testing "the panels are labelled by the column each draws"
    (let [texts (strip-labels (-> ds
                                  (pj/options {:grammar :paneled})
                                  (pj/lay-line :t [:a :b])))]
      (is (contains? texts "a"))
      (is (contains? texts "b"))))
  (testing "the value axis is the column the pivot invents, shared by both panels"
    (is (= "value" (:y-label (pj/plan (-> ds
                                          (pj/options {:grammar :paneled})
                                          (pj/lay-line :t [:a :b]))))))))

(deftest paneled-crosses-with-a-facet-in-the-other-direction
  (is (= 4 (panels (-> ds
                       (pj/options {:grammar :paneled})
                       (pj/lay-line :t [:a :b])
                       (pj/facet :part :row))))))

(deftest paneled-reports-a-facet-in-the-same-direction
  ;; A pose divides its panels once per direction, and under :paneled a
  ;; series is already asking for the across direction. Silently keeping
  ;; one of the two is the defect this reports.
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #"already facets across"
       (-> ds
           (pj/options {:grammar :paneled})
           (pj/facet :part)
           (pj/lay-line :t [:a :b])))))

(deftest overlay-collapses-a-paneled-series
  (testing "pj/overlay puts the columns back on one panel, coloured"
    (is (= 1 (panels (-> ds
                         (pj/options {:grammar :paneled})
                         pj/overlay
                         (pj/lay-line :t [:a :b])))))
    (is (= [:series ["a" "b"]]
           (legend (-> ds
                       (pj/options {:grammar :paneled})
                       pj/overlay
                       (pj/lay-line :t [:a :b]))))))
  (testing "and says the same thing written after the layer as before it"
    ;; :overlay is read where the panels are decided, not where the
    ;; layer is added. Deciding the destination inside the lay-* call
    ;; would have made the two orders draw different pictures with
    ;; nothing said.
    (let [before (-> ds (pj/options {:grammar :paneled}) pj/overlay
                     (pj/lay-line :t [:a :b]))
          after  (-> ds (pj/options {:grammar :paneled})
                     (pj/lay-line :t [:a :b]) pj/overlay)]
      (is (= (panels before) (panels after) 1))
      (is (= (legend before) (legend after))))))

(deftest overlay-does-not-collapse-a-facet-the-writer-asked-for
  ;; The collapse is keyed on the column the series wrote, so a
  ;; pj/facet on an ordinary column keeps its panels under pj/overlay.
  (is (= 2 (panels (-> ds
                       (pj/options {:grammar :paneled})
                       pj/overlay
                       (pj/lay-point :t :a)
                       (pj/facet :part))))))

;; ---- :written-out -- option I ----

(deftest written-out-refuses-a-bare-vector
  (testing "on :y, and names the series form"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"\{:series \[:a :b\]\}"
         (-> ds (pj/options {:grammar :written-out})
             (pj/lay-line :t [:a :b])))))
  (testing "in the pose's own mapping"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"bare vector of column names"
         (-> ds (pj/options {:grammar :written-out})
             (pj/pose {:x :t :y [:a :b]})))))
  (testing "on :group, and names the column form"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"\{:column \[:part :dim\]\}"
         (-> ds (pj/options {:grammar :written-out})
             (pj/lay-line :t :a {:group [:part :dim]})))))
  (testing "in pj/facet"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"\{:column \[:part :dim\]\}"
         (-> ds (pj/options {:grammar :written-out})
             (pj/lay-point :t :a)
             (pj/facet [:part :dim])))))
  (testing "in pj/facet-grid"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"bare vector of column names"
         (-> ds (pj/options {:grammar :written-out})
             (pj/lay-point :t :a)
             (pj/facet-grid [:part :dim] :dim))))))

(deftest written-out-accepts-the-written-out-form
  (testing "a series"
    (is (= 1 (panels (-> ds (pj/options {:grammar :written-out})
                         (pj/lay-line :t {:series [:a :b]})))))
    (is (= [:series ["a" "b"]]
           (legend (-> ds (pj/options {:grammar :written-out})
                       (pj/lay-line :t {:series [:a :b]}))))))
  (testing "a compound key on :group"
    (is (= 1 (panels (-> ds (pj/options {:grammar :written-out})
                         (pj/lay-line :t :a {:group {:column [:part :dim]}}))))))
  (testing "a compound key in pj/facet -- one panel per combination the data holds"
    (is (= 4 (panels (-> ds (pj/options {:grammar :written-out})
                         (pj/lay-point :t :a)
                         (pj/facet {:column [:part :dim]})))))))

(deftest written-out-leaves-everything-else-alone
  (testing "a single column names one distinction, as it always did"
    (is (= [:part ["l" "r"]]
           (legend (-> ds (pj/options {:grammar :written-out})
                       (pj/lay-line :t :a {:color :part}))))))
  (testing "a tooltip vector is markup, not columns"
    (is (= 1 (panels (-> ds (pj/options {:grammar :written-out})
                         (pj/lay-point :t :a {:tooltip [:div "hi"]})))))))

;; ---- The option itself ----

(deftest grammar-value-is-checked
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #":grammar must be one of \[:layered :paneled :written-out\]"
       (-> ds (pj/options {:grammar :nonsense})))))

(deftest a-grammar-written-after-the-call-it-would-change-is-reported
  ;; A series is pivoted where the lay-* call is written, so a grammar
  ;; written after it cannot reach it. Half a pipeline under one
  ;; grammar and half under another is the silent case this closes.
  (testing "after a series read under another grammar"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"already read a series under :layered"
         (-> ds (pj/lay-line :t [:a :b])
             (pj/options {:grammar :paneled})))))
  (testing ":written-out after a bare vector already written"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"already names \[:part :dim\] on :group as a bare vector"
         (-> ds (pj/lay-line :t :a {:group [:part :dim]})
             (pj/options {:grammar :written-out})))))
  (testing "the same grammar written again is not a disagreement"
    (is (= 1 (panels (-> ds (pj/lay-line :t [:a :b])
                         (pj/options {:grammar :layered}))))))
  (testing "options carrying no grammar are untouched"
    (is (= 1 (panels (-> ds (pj/lay-line :t [:a :b])
                         (pj/options {:title "t"}))))))
  (testing "a grammar after a layer that read no combination is fine"
    (is (= 1 (panels (-> ds (pj/lay-point :t :a)
                         (pj/options {:grammar :paneled})))))))

(deftest the-grammar-is-published-with-the-other-plot-options
  (is (contains? pj/plot-option-docs :grammar)))

;; ---- Regressions, each against a defect a review round found ----

(defn- groups
  "The group labels each layer of each panel draws, which is what says
   whether a distinction survived. A count alone does not: the collapse
   that lost two of four groups drew the same number of panels and the
   same legend as the one that kept them."
  [pose]
  (mapv (fn [p] (mapv (fn [l] (mapv :label (:groups l))) (:layers p)))
        (:panels (pj/plan pose))))

(deftest written-out-does-not-invent-a-remedy-the-library-refuses
  ;; The guard fired on all fourteen column-bearing aesthetics and told
  ;; nine of them to write `{:column [...]}` -- which those nine then
  ;; refused, so following the advice produced a second error. Worse,
  ;; it replaced a correct message with a wrong one. `written-out-form`
  ;; now answers only for the five aesthetics that read a vector.
  (testing "an aesthetic that draws one thing keeps its own message"
    (let [msg (try (-> ds (pj/options {:grammar :written-out})
                       (pj/lay-point :t :a {:color [:part :dim]}))
                   nil
                   (catch clojure.lang.ExceptionInfo e (ex-message e)))]
      (is (some? msg))
      (is (re-find #"belongs on :x or :y" msg))
      (is (not (re-find #"\{:grammar :written-out\}" msg)))))
  (testing "and the message is the one the default grammar gives"
    (let [under (fn [pose-fn]
                  (try (pose-fn) nil
                       (catch clojure.lang.ExceptionInfo e (ex-message e))))]
      (is (= (under #(-> ds (pj/lay-point :t :a {:color [:part :dim]})))
             (under #(-> ds (pj/options {:grammar :written-out})
                         (pj/lay-point :t :a {:color [:part :dim]})))))))
  (testing "the advice that is given is accepted"
    (is (= 4 (panels (-> ds (pj/options {:grammar :written-out})
                         (pj/lay-point :t :a)
                         (pj/facet {:column [:part :dim]})))))))

(deftest a-collapsed-series-keeps-a-layers-own-colour
  ;; The collapse worked the destination out again from the leaf alone,
  ;; which missed a `:color` the layer mapped itself: four groups became
  ;; two, under an identical legend, so each line joined the :a value to
  ;; the :b value with nothing said. The decision is made once, when the
  ;; pivot happens, and carried.
  (is (= (groups (-> ds (pj/lay-line :t [:a :b] {:color :part})))
         (groups (-> ds (pj/options {:grammar :paneled}) pj/overlay
                     (pj/lay-line :t [:a :b] {:color :part}))))))

(deftest a-layers-own-overlay-reaches-a-paneled-series
  ;; `pj/overlay`'s docstring promises `{:overlay true}` joins that one
  ;; layer. The collapse read the leaf's flag alone, so the layer-level
  ;; form was inert against a paneled series in both directions.
  (testing "{:overlay true} on the layer collapses the panels"
    (is (= 1 (panels (-> ds (pj/options {:grammar :paneled})
                         (pj/lay-line :t [:a :b] {:overlay true})))))
    (is (= [:series ["a" "b"]]
           (legend (-> ds (pj/options {:grammar :paneled})
                       (pj/lay-line :t [:a :b] {:overlay true}))))))
  (testing "{:overlay false} on the layer opts out of a pose-level overlay"
    (is (= 2 (panels (-> ds (pj/options {:grammar :paneled}) pj/overlay
                         (pj/lay-line :t [:a :b] {:overlay false})))))))

(deftest a-col-naming-the-key-column-agrees-rather-than-conflicts
  ;; `{:y {:series [...] :as :measure} :col :measure}` -- the CHANGELOG's
  ;; own example -- asks by hand for exactly what :paneled supplies. The
  ;; guard compared nothing and refused it.
  (is (= 2 (panels (-> ds (pj/options {:grammar :paneled})
                       (pj/pose {:x :t :y {:series [:a :b] :as :measure}
                                 :col :measure})
                       pj/lay-line)))))

(deftest facet-grid-reports-a-second-facet
  ;; `pj/facet` reported a second facet in the same direction; the grid
  ;; form called no such guard and replaced whatever was there. Under
  ;; :paneled that threw away the series and drew one unlabelled group
  ;; per panel, but the hole was there in every grammar.
  (testing "after a paneled series"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/facet-grid was given .* which this pose already facets by :series"
         (-> ds (pj/options {:grammar :paneled})
             (pj/lay-line :t [:a :b])
             (pj/facet-grid :part :dim)))))
  (testing "after an ordinary pj/facet, in either direction"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/facet-grid was given"
         (-> ds (pj/lay-point :t :a) (pj/facet :part) (pj/facet-grid :dim :part))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/facet-grid was given"
         (-> ds (pj/lay-point :t :a) (pj/facet :part :row) (pj/facet-grid :part :dim)))))
  (testing "on a pose that facets by nothing yet"
    (is (= 4 (panels (-> ds (pj/lay-point :t :a) (pj/facet-grid :part :dim)))))))

(deftest the-ordering-report-reaches-a-cell
  ;; A series is pivoted in a cell -- `expand-series` refuses a
  ;; composite outright -- so reading the root alone made the report
  ;; blind to the one shape that needs it.
  (testing "a cell that read a series under another grammar"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"already read a series under :layered"
         (-> (pj/arrange [(-> ds (pj/lay-line :t [:a :b]))
                          (-> ds (pj/lay-point :t :a))])
             (pj/options {:grammar :paneled})))))
  (testing "a cell already naming a bare vector"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"a cell of this pose already names"
         (-> (pj/arrange [(-> ds (pj/lay-point :t :a {:group [:part :dim]}))])
             (pj/options {:grammar :written-out})))))
  (testing "and a cell of a cell"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"a cell of this pose already names"
         (-> (pj/arrange [(pj/arrange [(-> ds (pj/lay-point :t :a
                                                            {:group [:part :dim]}))])])
             (pj/options {:grammar :written-out})))))
  (testing "a composite with neither is untouched"
    (is (:composite? (pj/plan (-> (pj/arrange [(-> ds (pj/lay-point :t :a))])
                                  (pj/options {:grammar :written-out})))))))

(deftest a-pose-built-by-hand-is-held-to-the-grammar
  ;; A literal map reaches no pj/options call, so both checks had to
  ;; run at the hand-built entry point too -- the same pose threw from
  ;; a typed arity and drew from a literal map.
  (testing "the value is checked"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"not one of \[:layered :paneled :written-out\]"
         (pj/plan {:data ds :mapping {:x :t :y :a}
                   :opts {:grammar :nonsense}}))))
  (testing "a bare vector in its mapping is reported"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"bare vector of column names"
         (pj/pose {:data ds
                   :mapping {:x :t :y :a :group [:part :dim]}
                   :opts {:grammar :written-out}}))))
  (testing "a bare vector on one of its layers is reported"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"bare vector of column names"
         (pj/pose {:data ds
                   :mapping {:x :t :y :a}
                   :opts {:grammar :written-out}
                   :layers [{:layer-type :point :mapping {:group [:part :dim]}}]}))))
  (testing "written out, it draws"
    (is (= 1 (panels (pj/pose {:data ds
                               :mapping {:x :t :y :a
                                         :group {:column [:part :dim]}}
                               :opts {:grammar :written-out}}))))))

(deftest the-default-path-writes-nothing-onto-a-pose
  ;; Which grammar a pivot was read under is provenance, not a setting,
  ;; and `:opts` is the map a writer wrote with pj/options -- two
  ;; published chapters say so. It moved to metadata, so a series pose
  ;; prints as it did before the option existed.
  (testing "a series pose carries no :opts of its own"
    (is (nil? (:opts (-> ds (pj/lay-line :t [:a :b]))))))
  (testing "nor does its draft"
    (is (= {} (:opts (-> ds (pj/lay-line :t [:a :b]) pj/draft)))))
  (testing "a paneled series carries one key, and it is namespaced"
    (let [opts (:opts (-> ds (pj/options {:grammar :paneled})
                          (pj/lay-line :t [:a :b])))]
      (is (= :paneled (:grammar opts)))
      (is (contains? opts pose/series-panels-key))
      (is (every? #(or (= :grammar %) (qualified-keyword? %)) (keys opts))))))
