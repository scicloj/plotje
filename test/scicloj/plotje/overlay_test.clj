(ns scicloj.plotje.overlay-test
  "`pj/overlay` and `{:overlay true}`: a layer joins the panel it is
   added to instead of starting one of its own."
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.plotje.api :as pj]))

(def wealth
  [{:cohort "a" :growth 1.0 :tax 0.3}
   {:cohort "b" :growth 2.0 :tax 0.9}])

(def weather
  {:day [1 2 3] :reading [10 12 11] :humidity [40 55 50] :other [7 8 9]})

(defn- panels [pose]
  (:panels (pj/svg-summary pose)))

(deftest overlay-on-a-leaf-test
  (testing "without it, a second layer naming another x is a second panel"
    (is (= 2 (panels (-> wealth
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort))))))
  (testing "pj/overlay joins every layer added after it"
    (is (= 1 (panels (-> wealth
                         pj/overlay
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort))))))
  (testing "{:overlay true} joins one layer"
    (is (= 1 (panels (-> wealth
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort {:overlay true}))))))
  (testing "a layer opts out of a pose-level pj/overlay"
    (is (= 2 (panels (-> wealth
                         pj/overlay
                         (pj/lay-bar :growth :cohort)
                         (pj/lay-bar :tax :cohort {:overlay false}))))))
  (testing "(pj/overlay pose false) turns it off from there on"
    (is (= 2 (panels (-> wealth
                         pj/overlay
                         (pj/lay-bar :growth :cohort)
                         (pj/overlay false)
                         (pj/lay-bar :tax :cohort)))))))

(deftest overlay-keeps-the-layers-columns-test
  (testing "the joined layer carries its own columns, and both are drawn"
    (let [pose (-> wealth
                   pj/overlay
                   (pj/lay-bar :growth :cohort)
                   (pj/lay-bar :tax :cohort))]
      (is (= 1 (panels pose)))
      ;; Two bars per cohort: one from each layer, on one pair of axes.
      (is (= 4 (:polygons (pj/svg-summary pose))))
      (is (= {:x :tax :y :cohort}
             (:mapping (second (:layers pose)))))))
  (testing "the axis covers every column drawn on it"
    ;; reading spans 10-12 and humidity 40-55; the shared axis is ticked
    ;; over both, and named for the column the panel already had.
    (let [texts (->> (tree-seq vector? seq
                               (pj/plot (-> weather
                                            pj/overlay
                                            (pj/lay-line :day :reading)
                                            (pj/lay-line :day :humidity))
                                        {:format :svg}))
                     (filter #(and (vector? %) (= :text (first %))))
                     (map last)
                     (filter string?)
                     set)]
      (is (contains? texts "reading"))
      (is (contains? texts "55"))
      (is (not (contains? texts "humidity"))))))

(deftest overlay-is-not-carried-onto-the-layer-test
  (testing ":overlay says where the layer goes and is not kept on it"
    (let [pose (-> wealth
                   pj/overlay
                   (pj/lay-bar :growth :cohort)
                   (pj/lay-bar :tax :cohort))]
      (is (every? #(not (contains? % :overlay)) (:layers pose)))
      (is (true? (:overlay pose)))))
  (testing "a per-layer flag leaves no trace either"
    (is (every? #(not (contains? % :overlay))
                (:layers (-> wealth
                             (pj/lay-bar :growth :cohort)
                             (pj/lay-bar :tax :cohort {:overlay true})))))))

(deftest overlay-on-a-composite-test
  (testing "a miss appends a panel without it, and joins the last leaf with it"
    (let [base (-> weather
                   (pj/pose :day :reading)
                   (pj/pose :day :humidity))]
      (is (= 3 (panels (pj/lay-point base :day :other))))
      (is (= 2 (panels (-> base pj/overlay (pj/lay-point :day :other)))))))
  (testing "a hit still lands on the matching leaf, overlay or not"
    (let [base (-> weather
                   (pj/pose :day :reading)
                   (pj/pose :day :humidity))]
      (is (= 2 (panels (-> base pj/overlay (pj/lay-point :day :humidity))))))))
