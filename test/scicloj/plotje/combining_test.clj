(ns scicloj.plotje.combining-test
  "Where two values for one key meet -- a later call on the same pose, a
   layer under its pose, a cell under its composite -- one combiner
   answers at every level. A key holds either one value, replaced by the
   later or inner one, or a map of independent settings, combined key by
   key: a scale spec, a row filter `:where`, a `:theme`."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]))

(def tiny {:u [1.0 2.0 3.0] :v [4.0 5.0 6.0] :w [7.0 9.0 8.0]
           :g ["a" "b" "a"] :h ["p" "p" "q"]})

(defn- svg-text [fr] (str (pj/plot fr)))

(def ^:private rgb {"#0000ff" "rgb(0,0,255)" "#ff0000" "rgb(255,0,0)"})

(defn- shows?
  "Whether the drawn SVG uses `colour` -- written as given or as the
   `rgb(...)` the renderer prints."
  [fr colour]
  (let [svg (svg-text fr)]
    (or (str/includes? svg colour) (str/includes? svg (rgb colour)))))

;; ---- Plot options ----

(deftest a-theme-combines-key-by-key-at-every-level-test
  (testing "pj/options twice"
    (let [fr (-> tiny
                 (pj/lay-point :u :v)
                 (pj/options {:theme {:bg "#0000ff"}})
                 (pj/options {:theme {:grid "#ff0000"}}))]
      (is (= {:bg "#0000ff" :grid "#ff0000"} (get-in fr [:opts :theme])))))
  (testing "a composite's theme reaches a cell that writes its own"
    (let [cell (-> (pj/pose nil {:x :u :y :v})
                   (pj/options {:theme {:grid "#ff0000"}}))
          fr (-> tiny
                 (pj/arrange [cell])
                 (pj/options {:theme {:bg "#0000ff"}})
                 pj/lay-point)]
      (is (shows? fr "#0000ff"))
      (is (shows? fr "#ff0000"))))
  (testing "a pose arranged into cells keeps its theme under a cell's"
    (let [cell (-> (pj/pose nil {:x :u :y :v})
                   (pj/options {:theme {:grid "#ff0000"}}))
          fr (-> tiny
                 (pj/pose {})
                 (pj/options {:theme {:bg "#0000ff"}})
                 (pj/arrange [cell])
                 pj/lay-point)]
      (is (shows? fr "#0000ff"))
      (is (shows? fr "#ff0000")))))

(deftest share-scales-is-one-value-test
  (testing "a later :share-scales replaces the earlier, in either spelling"
    (is (= {:y :all}
           (-> tiny
               (pj/lay-point :u :v)
               (pj/options {:share-scales {:x :all}})
               (pj/options {:share-scales {:y :all}})
               :opts :share-scales)))
    (is (= #{:y}
           (-> tiny
               (pj/lay-point :u :v)
               (pj/options {:share-scales #{:x}})
               (pj/options {:share-scales #{:y}})
               :opts :share-scales)))))

(deftest a-plain-option-is-replaced-test
  (is (= "b" (-> tiny
                 (pj/lay-point :u :v)
                 (pj/options {:title "a"})
                 (pj/options {:title "b"})
                 :opts :title))))

;; ---- Row filters ----

(def other {:u [1.0 2.0 3.0 4.0] :v [4.0 5.0 6.0 7.0] :g ["a" "a" "a" "b"]})

(defn- points [fr] (:points (pj/svg-summary fr)))

(deftest a-row-filter-combines-column-by-column-at-every-level-test
  (testing "pj/pose twice: the same column is replaced"
    (is (= {:g "b"} (:where (-> tiny
                                (pj/pose :u :v {:where {:g "a"}})
                                (pj/pose {:where {:g "b"}}))))))
  (testing "a cell's value for a column replaces its composite's"
    (is (= 1 (-> tiny
                 (pj/pose {:x :u :y :v :where {:g "a"}})
                 (pj/arrange [{:where {:g "b"}}])
                 pj/lay-point
                 points))))
  (testing "filters on different columns both apply"
    (is (= 1 (-> tiny
                 (pj/pose {:x :u :y :v :where {:g "a"}})
                 (pj/arrange [{:where {:h "q"}}])
                 pj/lay-point
                 points))))
  (testing "a cell and (pj/pose cell-filter) on the composite read alike"
    (is (= (-> tiny
               (pj/pose {:x :u :y :v :where {:g "a"}})
               (pj/pose {:where {:g "b"}})
               pj/lay-point
               points)
           (-> tiny
               (pj/pose {:x :u :y :v :where {:g "a"}})
               (pj/arrange [{:where {:g "b"}}])
               pj/lay-point
               points))))
  (testing "through a nested arrangement"
    (is (= 1 (-> tiny
                 (pj/arrange [(pj/arrange [(pj/pose nil {:x :u :y :v :where {:g "b"}})])])
                 (pj/pose {:where {:g "a"}})
                 pj/lay-point
                 points)))))

(deftest a-composite-filter-reaches-a-cell-with-its-own-data-test
  (testing "as the composite's mapping does"
    (is (= 5 (-> tiny
                 (pj/pose {:x :u :y :v :where {:g "a"}})
                 (pj/arrange [{} (pj/pose other {:x :u :y :v})])
                 pj/lay-point
                 points))))
  (testing "a column the cell's data lacks is reported"
    (is (thrown-with-msg? Exception #"names \[:h\], which the data does not have"
                          (-> tiny
                              (pj/pose {:x :u :y :v :where {:h "p"}})
                              (pj/arrange [{} (pj/pose other {:x :u :y :v})])
                              pj/lay-point
                              pj/plan)))))

(deftest a-misspelled-composite-filter-warns-once-test
  (is (= 1 (count (re-seq #"Warning: A row filter names \"z\""
                          (with-out-str
                            (-> tiny
                                (pj/pose {:x :u :y :v :where {:g "z"}})
                                (pj/arrange [{} {:y :u} {:x :v}])
                                pj/lay-point
                                pj/plan)))))))

(deftest the-configuration-chain-combines-the-same-way-test
  (testing "a theme set for a scope keeps the default keys it does not name"
    (let [theme (pj/with-config {:theme {:bg "#000000"}} (:theme (pj/config)))]
      (is (= "#000000" (:bg theme)))
      (is (some? (:grid theme)))))
  (testing "a gradient is one value, replaced whole"
    (pj/with-config {:color-range {:low "#ff0000" :high "#0000ff"}}
      (is (= {:low "#ff0000" :high "#0000ff"}
             (:color-range (pj/config)))))))
