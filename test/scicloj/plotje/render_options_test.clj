(ns scicloj.plotje.render-options-test
  "A plot option that changes the picture has to change it everywhere.

   Three paths reach the membrane stage, and each used to filter what it
   passed on: `pj/plot` handed over the whole opts map, `pj/save` went
   through `plan->plot`, which rebuilt them from a `select-keys`
   whitelist, and a composite recursed into each cell with `{}`. So an
   option could move a leaf on screen, do nothing in the saved file, and
   do nothing at all once the same pose was arranged -- silently, in all
   three cases.

   These tests compare the paths against each other rather than against
   a fixed expectation, so they keep holding as the defaults move."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.impl.defaults :as defaults]
            [scicloj.plotje.render.svg :as svg]))

(def data {:height [1 2 3] :weight [1 2 3] :n [1000 25000 100000]})
(def other {:height [1 2 3] :weight [2 3 4] :n [1000 25000 100000]})

(defn- leaf [extra]
  (-> data
      (pj/lay-point :height :weight {:size :n})
      (pj/lay-rule-h {:y-intercept 2})
      (pj/options (merge {:title "T"} extra))))

(defn- composite [extra]
  (-> (pj/arrange [(-> data (pj/lay-point :height :weight {:size :n})
                       (pj/lay-rule-h {:y-intercept 2}))
                   (-> other (pj/lay-point :height :weight {:size :n})
                       (pj/lay-rule-h {:y-intercept 3}))])
      (pj/options (merge {:title "T"} extra))))

(defn- svg-signature
  "What a rendering option could move, read out of a serialized SVG:
   the drawn text, the font sizes, the stroke colors and their widths."
  [s]
  {:text    (mapv second (re-seq #"<text[^>]*>([^<]*)</text>" s))
   :fonts   (vec (sort (distinct (map second (re-seq #"font-size=\"([^\"]*)\"" s)))))
   :strokes (vec (sort (distinct (map second (re-seq #"[^-]stroke=\"([^\"]*)\"" s)))))
   :widths  (vec (sort (distinct (map second (re-seq #"stroke-width=\"([^\"]*)\"" s)))))})

(defn- saved-signature [pose]
  (let [f (java.io.File/createTempFile "plotje-render-options" ".svg")]
    (try
      (pj/save pose (.getPath f))
      (svg-signature (slurp f))
      (finally (.delete f)))))

(defn- plotted-signature
  "pj/plot answers hiccup; pj/save writes that same hiccup out through
   `hiccup->svg-str`. Serializing here with the same function is what
   makes the two paths comparable -- what differs between them is the
   tree, which is the thing under test."
  [pose]
  (svg-signature (svg/hiccup->svg-str (pj/plot pose))))

(def moving-options
  "One option per membrane-stage reader, each with a value far enough
   from the default to show up."
  {:thousands-separator ","
   :title-font-size 30
   :label-font-size 22
   :grid-stroke-width 4
   :annotation-stroke "#0000ff"})

(deftest an-option-that-moves-a-plot-moves-the-saved-file-too
  (testing "pj/save honors what pj/plot honors"
    (doseq [[k v] moving-options]
      (let [base  (saved-signature (leaf {}))
            moved (saved-signature (leaf {k v}))]
        (is (not= base moved)
            (str k " changed pj/plot's picture but not the saved SVG -- "
                 "plan->plot used to rebuild render opts from a whitelist"))))))

(deftest the-two-paths-draw-the-same-picture
  (testing "plot and save agree, option by option"
    (doseq [[k v] (assoc moving-options :label-offset 90)]
      (is (= (plotted-signature (leaf {k v}))
             (saved-signature (leaf {k v})))
          (str k " renders differently through pj/plot and pj/save")))))

(deftest a-composite-cell-reads-the-composite-s-options
  (testing "an option that moves a leaf still moves it once arranged"
    (doseq [[k v] moving-options]
      (let [base  (plotted-signature (composite {}))
            moved (plotted-signature (composite {k v}))]
        (is (not= base moved)
            (str k " does nothing on a composite -- the composite "
                 "plan->membrane used to recurse with {}")))))
  (testing "and the saved composite agrees with the plotted one"
    (doseq [[k v] moving-options]
      (is (= (plotted-signature (composite {k v}))
             (saved-signature (composite {k v})))
          (str k " renders differently through the two composite paths")))))

;; ---- Coverage that maintains itself ----
;;
;; The five options above were chosen by hand, one per membrane-stage
;; reader. That pins the fix and nothing after it: a configuration key
;; added later would go unprobed until someone remembered this file.
;; Below, the probe set is built from the configuration registry, so a
;; new key is covered the moment it is documented -- or, when no value
;; can be derived for it, named in a failure until someone chooses one.

(defn- derived-probe
  "A value unlike `k`'s default, for a key whose default is a number or
   a boolean. nil when none can be derived."
  [k]
  (let [v (get defaults/defaults k)]
    (cond
      (boolean? v) (not v)
      (number? v)  (+ (* 2 v) 3)
      :else        nil)))

(def hand-probed
  "Keys whose default is absent, or is a string or a vector, so no
   value follows from it."
  {:thousands-separator ","
   :decimal-separator   "."
   :annotation-stroke   "#0000ff"
   :default-color       "#0000ff"
   :point-stroke        "#00ff00"
   :annotation-dash     [8 2]
   :x-tick-angle        45
   :x-tick-label-pad    8})

(def unprobed
  "Configuration keys this test does not exercise, and why. Pinned so
   the set cannot grow in silence: a key added to the registry lands
   here as a failure until it is either probed or listed with a reason."
  #{;; not read on the leaf render path at all
    :bin-method :domain-padding :legend-width :margin-multi
    :panel-size :validate
    ;; interaction, tested in interactivity_test
    :tooltip :brush
    ;; a map, not a scalar
    :theme
    ;; naming a palette or gradient, tested in the color tests
    :palette :color-scale :color-midpoint
    ;; chooses which render path runs, so it cannot compare two of them
    :format
    ;; where the legend sits, tested in legend_position_test
    :legend-position
    ;; turns a warning into a throw; tested in strict_test
    :strict})

(deftest every-configuration-key-is-probed-or-accounted-for
  (let [probed  (into (set (keys hand-probed))
                      ;; `some?`, not truthiness -- a boolean default of
                      ;; true derives the probe `false`, which a plain
                      ;; filter would drop as if no probe existed.
                      (filter #(some? (derived-probe %)))
                      (keys defaults/config-key-docs))
        missing (remove (into probed unprobed)
                        (keys defaults/config-key-docs))]
    (is (empty? missing)
        (str "configuration key(s) with no probe value and no entry in "
             "`unprobed`: " (vec (sort-by str missing))
             ". Give each a value in `hand-probed`, or list it in "
             "`unprobed` with the reason it is not exercised here."))))

(deftest plot-and-save-agree-on-every-probed-configuration-key
  (testing "the two paths draw the same picture, key by key"
    (doseq [k (sort-by str (keys defaults/config-key-docs))
            :when (not (unprobed k))
            :let  [v (get hand-probed k (derived-probe k))]
            :when (some? v)]
      (let [[plotted saved]
            (try [(plotted-signature (leaf {k v})) (saved-signature (leaf {k v}))]
                 (catch Exception e [::threw (ex-message e)]))]
        (is (= plotted saved)
            (str k " " (pr-str v)
                 " renders differently through pj/plot and pj/save"))))))

(deftest a-tooltip-stays-on-the-cell-that-asked-for-it
  (testing "passing the composite's options down does not spread its tooltip"
    ;; The cells' plan->membrane call carries the composite's opts now,
    ;; and :tooltip is decided per cell rather than on the composite, so
    ;; it is overwritten from each sub-plot's own plan.
    (let [one-interactive
          (-> (pj/arrange [(-> data (pj/lay-point :height :weight)
                               (pj/options {:tooltip true}))
                           (-> other (pj/lay-point :height :weight))])
              pj/plot
              pr-str)]
      (is (str/includes? one-interactive "data-tooltip")
          "the cell that asked for tooltips has them")
      (is (= 3 (count (re-seq #"data-tooltip" one-interactive)))
          "and only that cell's three points, not all six"))))
