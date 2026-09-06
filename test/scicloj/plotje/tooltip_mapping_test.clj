(ns scicloj.plotje.tooltip-mapping-test
  "The `:tooltip` mapping -- what a mark says on hover, written by the
   writer rather than assembled by the renderer."
  (:require [clojure.test :refer [deftest is testing]]
            [tablecloth.api :as tc]
            [scicloj.plotje.render.svg :as svg]
            [scicloj.plotje.api :as pj]))

(def sales
  (tc/dataset {:month   ["Jan" "Feb" "Mar"]
               :revenue [1653346 2410880 987654]
               :margin  [0.184 0.223 0.161]}))

(defn- tooltips
  "Every `data-tooltip` the rendered SVG carries, in draw order."
  [pose]
  (->> (tree-seq vector? seq (pj/plot pose))
       (filter #(and (vector? %) (map? (second %)) (:data-tooltip (second %))))
       (mapv #(:data-tooltip (second %)))))

(defn- attr
  "Every value of one attribute the rendered SVG carries, in draw order."
  [pose k]
  (->> (tree-seq vector? seq (pj/plot pose))
       (filter #(and (vector? %) (map? (second %)) (get (second %) k)))
       (mapv #(get (second %) k))))

(defn- page-has-tooltip-script? [pose]
  (let [strs (atom [])]
    (clojure.walk/postwalk (fn [x] (when (string? x) (swap! strs conj x)) x)
                           (pj/plot pose))
    (boolean (some #(re-find #"nsk-tooltip" %) @strs))))

(def labelled
  (tc/add-column sales :hover
                 #(map (fn [m r p]
                         (str m "\n" (format "%.1fM" (/ (double r) 1e6))
                              " at " (format "%.1f%%" (* 100.0 p))))
                       (:month %) (:revenue %) (:margin %))))

(deftest tooltip-column-is-shown-as-it-stands-test
  (testing "a mapped column is what the mark says, formatted by the writer"
    ;; The whole point: 1653346 reads as 1.7M because the writer said
    ;; so in the data language, not because a plotting option offered a
    ;; number format.
    (is (= ["Jan\n1.7M at 18.4%"
            "Feb\n2.4M at 22.3%"
            "Mar\n1.0M at 16.1%"]
           (tooltips (-> labelled (pj/lay-point :margin :revenue
                                                {:tooltip :hover}))))))
  (testing "a written string is what every mark of the layer says"
    (is (= ["one note" "one note" "one note"]
           (tooltips (-> sales (pj/lay-point :margin :revenue
                                             {:tooltip "one note"}))))))
  (testing "and a column the layer does not draw is reachable"
    ;; :month is on neither axis, and the tooltip names it anyway.
    (is (every? #(re-find #"Jan|Feb|Mar" %)
                (tooltips (-> labelled (pj/lay-point :margin :revenue
                                                     {:tooltip :hover})))))))

(deftest tooltip-mapping-turns-tooltips-on-test
  (testing "writing the mapping is the request; the option is not also needed"
    (let [pose (-> labelled (pj/lay-point :margin :revenue {:tooltip :hover}))]
      (is (true? (:tooltip (pj/plan pose))))
      (is (= 3 (count (tooltips pose))))
      ;; The attribute is useless without the script that reads it.
      (is (page-has-tooltip-script? pose))))
  (testing "and a plot that asks for neither carries none"
    (let [pose (-> sales (pj/lay-point :margin :revenue))]
      (is (empty? (tooltips pose)))
      (is (not (page-has-tooltip-script? pose))))))

(deftest tooltip-mapping-scopes-like-any-mapping-test
  (testing "written on the pose, every layer reads it"
    (is (= 3 (count (tooltips (-> labelled
                                  (pj/pose {:x :margin :y :revenue
                                            :tooltip :hover})
                                  pj/lay-point))))))
  (testing "written on a layer, the nearer one wins"
    (is (= ["one note" "one note" "one note"]
           (tooltips (-> labelled
                         (pj/pose {:x :margin :y :revenue :tooltip :hover})
                         (pj/lay-point {:tooltip "one note"})))))))

(deftest tooltip-falls-back-to-the-built-in-text-test
  (testing "with no mapping, a mark still describes itself"
    (let [ts (tooltips (-> sales (pj/lay-point :margin :revenue)
                           (pj/options {:tooltip true})))]
      (is (= 3 (count ts)))
      (is (every? #(re-find #"margin: " %) ts))))
  (testing "and an interval bar reads a mapped column too"
    (let [gantt (tc/dataset {:start [#inst "2024-01-01" #inst "2024-03-01"]
                             :end   [#inst "2024-02-01" #inst "2024-04-01"]
                             :task  ["Design" "Build"]
                             :note  ["design phase" "build phase"]})]
      (is (= ["design phase" "build phase"]
             (tooltips (-> gantt (pj/lay-interval-h :start :task
                                                    {:x-end :end
                                                     :tooltip :note}))))))))

(deftest tooltip-text-may-hold-newlines-test
  (testing "the page renders a newline rather than collapsing it"
    ;; `white-space: pre-line` is what makes a multi-line tooltip
    ;; readable; `nowrap` ran the lines together.
    (let [strs (atom [])]
      (clojure.walk/postwalk
       (fn [x] (when (string? x) (swap! strs conj x)) x)
       (pj/plot (-> labelled (pj/lay-point :margin :revenue {:tooltip :hover}))))
      (is (some #(re-find #"white-space:pre-line" %) @strs)))))

(deftest hiccup-tooltip-test
  (let [rich (tc/add-column sales :hover
                            #(map (fn [month revenue]
                                    [:div [:b month] [:br]
                                     "revenue " [:code (str revenue)]])
                                  (:month %) (:revenue %)))]
    (testing "a hiccup column is serialized to markup, under its own attribute"
      (is (= ["<div><b>Jan</b><br>revenue <code>1653346</code></div>"
              "<div><b>Feb</b><br>revenue <code>2410880</code></div>"
              "<div><b>Mar</b><br>revenue <code>987654</code></div>"]
             (attr (-> rich (pj/lay-point :margin :revenue {:tooltip :hover}))
                   :data-tooltip-html)))
      (is (empty? (attr (-> rich (pj/lay-point :margin :revenue {:tooltip :hover}))
                        :data-tooltip))))
    (testing "and a string stays text, however it is spelled"
      ;; The two travel under different attributes, so a string that
      ;; spells out a tag is shown as that text rather than rendered.
      (let [pose (-> sales (pj/lay-point :margin :revenue
                                         {:tooltip "<b>not bold</b>"}))]
        (is (= ["<b>not bold</b>"] (distinct (attr pose :data-tooltip))))
        (is (empty? (attr pose :data-tooltip-html)))))
    (testing "a written hiccup value covers the whole layer"
      (is (= ["<b>one note</b>" "<b>one note</b>" "<b>one note</b>"]
             (attr (-> sales (pj/lay-point :margin :revenue
                                           {:tooltip [:b "one note"]}))
                   :data-tooltip-html))))))

(deftest hiccup-serializer-escapes-content-not-structure-test
  ;; The boundary that matters: the tags are the writer's code, the
  ;; strings are the data. A column holding markup is shown, not run.
  (testing "text content is escaped"
    (is (= "<div>escaped: &lt;script&gt;alert(1)&lt;/script&gt;</div>"
           (svg/hiccup->html [:div "escaped: " "<script>alert(1)</script>"]))))
  (testing "attribute values are escaped"
    (is (= "<span title=\"a &quot;quoted&quot; &amp; thing\">x</span>"
           (svg/hiccup->html [:span {:title "a \"quoted\" & thing"} "x"]))))
  (testing "nesting, seqs and nil children"
    (is (= "<div>a<em>b</em><i>c</i>d</div>"
           (svg/hiccup->html [:div "a" [:em "b"] nil (list [:i "c"] "d")]))))
  (testing "a void element closes itself"
    (is (= "<div>one<br>two</div>"
           (svg/hiccup->html [:div "one" [:br] "two"]))))
  (testing "numbers are rendered as their text"
    (is (= "<b>42</b>" (svg/hiccup->html [:b 42])))))

(defn- warning-text
  "Whatever the plot printed while it was built."
  [pose]
  (let [w (java.io.StringWriter.)]
    (binding [*out* w] (pj/plot pose))
    (str w)))

(deftest tooltip-on-a-mark-that-draws-none-warns-test
  ;; Shipped silent in 0.11.0: the mapping was accepted, the stylesheet
  ;; and the browser script were injected, and nothing appeared.
  (testing "a bar is told, and the message names the marks that do draw one"
    (let [w (warning-text (pj/lay-bar labelled :month :revenue {:tooltip :hover}))]
      (is (re-find #"no mark on this plot draws hover text" w))
      (is (re-find #":interval-h, :point" w))))
  (testing "a written string on such a mark is told too"
    (is (re-find #"draws hover text"
                 (warning-text (pj/lay-line labelled :month :revenue
                                            {:tooltip "one for the layer"})))))
  (testing "a point is not told"
    (is (= "" (warning-text (pj/lay-point labelled :month :revenue
                                          {:tooltip :hover})))))
  (testing "a pose-level mapping is not told where any layer draws it"
    ;; The case the aesthetic exists for: the line ignores the column
    ;; and the point beside it draws the hover text.
    (is (= "" (warning-text (-> labelled
                                (pj/pose {:x :month :y :revenue :tooltip :hover})
                                (pj/lay-line)
                                (pj/lay-point))))))
  (testing "a plot with no tooltip mapping is not told"
    (is (= "" (warning-text (pj/lay-bar labelled :month :revenue))))))
