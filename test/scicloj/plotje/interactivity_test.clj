(ns scicloj.plotje.interactivity-test
  "`:tooltip true` and `:brush true` put Scittle forms in the plot. Whether
   they reach the browser as code depends on how Clay renders them, and
   nothing in the pose or the plot value shows the difference: the forms
   look identical either way. So these tests render through Clay itself
   and read the emitted HTML.

   The failure this pins: the interactive plot used to be one
   `:kind/hiccup2` value with the scripts nested inside it. `:kind/hiccup2`
   escapes every string it renders -- which is what makes a category named
   R&D reach the page as text -- and it escaped the script bodies too, so
   `(.querySelector container \"svg\")` arrived as
   `(.querySelector container &quot;svg&quot;)`. A script element's content
   is raw text in HTML, so the browser does not decode those entities and
   Scittle never reads the form. Tooltips and brush were dead in the
   browser while every assertion on the plot value passed."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is]]
            [hiccup.core :as hiccup]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.plotje.api :as pj]))

(defn- ->html
  "The HTML Clay emits for `value`, the way a rendered page gets it."
  [value]
  (->> (prepare/prepare-or-pprint {:value value})
       (map (fn [item]
              (or (:html item)
                  (some-> (prepare/item->hiccup item {}) hiccup/html))))
       (map str)
       (str/join "\n")))

(defn- scatter [opts]
  (-> {:height [1 2 3] :weight [4 5 6] :species ["a" "b" "R&D"]}
      (pj/lay-point :height :weight {:color :species})
      (pj/options (merge {:width 300 :height 200} opts))))

(defn- scripts
  "The body of every Scittle script in `html`."
  [html]
  (->> (re-seq #"(?s)<script type=\"application/x-scittle\">(.*?)</script>" html)
       (map second)))

(deftest a-tooltip-script-reaches-the-page-as-code
  (testing "the script body keeps the quotes it was written with"
    (let [bodies (scripts (->html (pj/plot (scatter {:tooltip true}))))]
      (is (= 1 (count bodies)))
      (is (str/includes? (first bodies) "(.querySelector container \"svg\")"))
      (is (not (str/includes? (first bodies) "&quot;"))
          "an escaped quote inside a script element is never decoded back"))))

(deftest a-brush-script-reaches-the-page-as-code
  (testing "the same for the brush script"
    (let [bodies (scripts (->html (pj/plot (scatter {:brush true}))))]
      (is (= 1 (count bodies)))
      (is (str/includes? (first bodies) "\"[data-row-idx]\""))
      (is (not (str/includes? (first bodies) "&quot;"))))))

(deftest the-brush-marquee-is-measured-in-one-unit
  (testing "the drag and the rectangle that shows it agree under CSS scaling"
    ;; A drag is measured from `clientX` and `getBoundingClientRect`, both
    ;; of which answer in CSS pixels. An SVG rect's `x`/`y`/`width`/
    ;; `height` are user units, which equal CSS pixels only while the SVG
    ;; renders at exactly its viewBox size -- so writing the drag onto an
    ;; SVG rect drew the marquee at a fraction of the drag under a
    ;; `max-width: 100%`. The marquee is an HTML overlay instead, sized in
    ;; CSS pixels like everything else the script measures.
    (let [body (first (scripts (->html (pj/plot (scatter {:brush true})))))]
      (is (str/includes? body "(.createElement js/document \"div\")"))
      (is (not (str/includes? body "createElementNS"))
          "an SVG rect would read the CSS-pixel drag as user units")
      (is (not (str/includes? body ".setAttribute sel"))
          "nothing about the marquee goes through SVG geometry attributes"))))

(deftest the-brush-marquee-does-not-swallow-the-drag
  (testing "the overlay covers the plot, so it must not be hit-testable"
    ;; The marquee is a sibling of the SVG, not a child, so a mouse event
    ;; landing on it would not bubble to the listeners on the SVG and the
    ;; drag would freeze the moment the pointer entered its own selection.
    (let [html (->html (pj/plot (scatter {:brush true})))
          css (second (re-find #"\.nsk-brush-sel \{([^}]*)\}" html))]
      (is (some? css))
      (is (str/includes? css "pointer-events:none"))
      (is (str/includes? css "position:absolute")))))

(deftest both-interactions-give-two-scripts
  (testing "tooltip and brush each contribute their own script"
    (let [bodies (scripts (->html (pj/plot (scatter {:tooltip true :brush true}))))]
      (is (= 2 (count bodies)))
      (is (every? #(not (str/includes? % "&quot;")) bodies)))))

(deftest the-plot-itself-still-escapes-its-text
  (testing "moving the scripts out did not cost the plot its escaping"
    (let [html (->html (pj/plot (scatter {:tooltip true})))]
      ;; The legend entry for the R&D category, escaped as markup must be.
      (is (str/includes? html "R&amp;D"))
      (is (not (re-find #"R&D" html))))))

(deftest the-script-finds-the-plot-it-belongs-to
  (testing "the scripts are siblings of the plot, so they address it by id"
    (let [html (->html (pj/plot (scatter {:tooltip true :brush true})))
          div-id (second (re-find #"<div id=\"(nsk-[^\"]+)\"" html))]
      (is (some? div-id))
      (is (every? #(str/includes? % div-id) (scripts html))))))

(deftest a-plain-plot-carries-no-script
  (testing "nothing is injected unless an interaction was asked for"
    (let [html (->html (pj/plot (scatter {})))]
      (is (empty? (scripts html)))
      (is (not (str/includes? html "nsk-tooltip"))))))
