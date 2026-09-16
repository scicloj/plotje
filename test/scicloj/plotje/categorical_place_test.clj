(ns scicloj.plotje.categorical-place-test
  "A number written for a categorical axis is a place among its
   categories, counted from one.

   `1` is the first category, `1.5` sits halfway to the second, and the
   axis reaches half a unit past each end. The same number has to mean
   the same thing however it is written and on whichever axis it is
   written for, so these tests hold the two spellings together -- a bare
   number, and a nudge from a named category -- and hold `:x` against
   `:y`. Those were three separate answers before: `:x` reported that
   numeric and categorical domains could not be merged, `:y` added a
   fourth category ticked `1.5`, and `pj/to-drawing` refused the value
   on either axis."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]
            [clojure.string :as str]))

(def x-categorical
  "Three bars, categories on x."
  (-> {:x ["A" "B" "C"] :y [2.2 4.0 3.5]}
      (pj/lay-bar :x :y)))

(def y-categorical
  "The same three bars flipped, categories on y."
  (-> {:v [1.0 2.0 3.0] :cat ["x" "y" "z"]}
      (pj/lay-bar :v :cat)))

(defn- panel [pose]
  (first (:panels (pj/plan pose))))

(defn- frame [pose]
  (first (:panels (pj/frames pose))))

(defn- polygon-spans
  "The horizontal extent of each polygon drawn, in the panel's own
   coordinates. A bar is a polygon, so this is what says where one was
   drawn and how wide. Polygons only -- the grid and the axes are
   polylines, and they carry `:points` too."
  [pose]
  (->> (re-seq #":polygon \{[^}]*?:points \"([^\"]+)\"" (pr-str (pj/plot pose)))
       (mapv (fn [[_ pts]]
               (let [xs (mapv #(Double/parseDouble (first (str/split % #",")))
                              (str/split pts #" "))]
                 [(reduce min xs) (reduce max xs)])))))

;; ---- One number, one meaning ----

(deftest the-two-spellings-draw-the-same-picture
  (testing "a bare place and a nudge from the category it counts from"
    ;; Byte-identical drawings, not merely equal positions: the plans
    ;; differ -- one carries :xs [1.5], the other :xs ["A"] and a
    ;; :nudge-x -- and the whole point is that the difference stops
    ;; there.
    (is (= (pj/plot (-> x-categorical (pj/lay-label {:x 1.5 :y 3.0 :text "note"})))
           (pj/plot (-> x-categorical (pj/lay-label {:x {:value "A"} :nudge-x 0.5
                                                     :y 3.0 :text "note"}))))
        "on x")
    (is (= (pj/plot (-> x-categorical (pj/lay-point {:x 1.5 :y 3.0})))
           (pj/plot (-> x-categorical (pj/lay-point {:x {:value "A"} :nudge-x 0.5 :y 3.0}))))
        "and not only for text")
    (is (= (pj/plot (-> y-categorical (pj/lay-label {:y 1.5 :x 2.0 :text "note"})))
           (pj/plot (-> y-categorical (pj/lay-label {:y {:value "x"} :nudge-y 0.5
                                                     :x 2.0 :text "note"}))))
        "on y, which used to add a fourth category for the bare number")))

(deftest a-place-adds-no-category
  (testing "the axis keeps the categories the data gave it"
    (let [p (panel (-> x-categorical (pj/lay-label {:x 1.5 :y 3.0 :text "note"})))]
      (is (= ["A" "B" "C"] (:x-domain p)))
      (is (= ["A" "B" "C"] (:values (:x-ticks p)))))
    (let [p (panel (-> y-categorical (pj/lay-label {:y 1.5 :x 2.0 :text "note"})))]
      (is (= ["x" "y" "z"] (:y-domain p))
          "y had its own copy of the domain parse and grew a 1.5 category")
      (is (= ["x" "y" "z"] (:values (:y-ticks p)))))))

(deftest a-place-is-counted-from-one-and-interpolates
  (testing "1 is the first category and 1.5 is halfway to the second"
    (let [p (frame x-categorical)
          at (fn [v] (first (pj/to-drawing p v 3.0)))]
      (is (= (at "A") (at 1)))
      (is (= (at "B") (at 2)))
      (is (< (abs (- (at 1.5) (/ (+ (at "A") (at "B")) 2.0))) 1e-9)))))

(deftest a-place-counts-in-the-order-the-axis-draws
  (testing "a domain the writer set decides which category is first"
    (let [p (frame (-> x-categorical (pj/scale :x {:domain ["C" "B" "A"]})))
          at (fn [v] (first (pj/to-drawing p v 3.0)))]
      (is (= (at "C") (at 1)) "not the order the data happened to arrive in")
      (is (= (at "A") (at 3))))))

;; ---- Where the axis ends ----

(deftest the-ends-of-the-axis-are-places-and-past-them-is-refused
  (testing "three categories reach from 0.5 to 3.5"
    (let [p (frame x-categorical)]
      (is (number? (first (pj/to-drawing p 0.5 3.0))))
      (is (number? (first (pj/to-drawing p 3.5 3.0))))
      (doseq [bad [3.6 99 -50]]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"past the ends of this axis"
                              (pj/to-drawing p bad 3.0))
            (str bad " has no place on a three-category axis")))))
  (testing "the refusal names the value, the categories and the range"
    (let [p (frame x-categorical)
          e (try (pj/to-drawing p 99 3.0) (catch clojure.lang.ExceptionInfo e e))]
      (is (re-find #"got 99 for :x" (ex-message e)))
      (is (re-find #"\[\"A\" \"B\" \"C\"\]" (ex-message e)))
      (is (= [0.5 3.5] (:place-range (ex-data e))))))
  (testing "a written layer is refused on the same rule, on either axis"
    (doseq [[label pose] [["x" (-> x-categorical (pj/lay-label {:x 99 :y 3.0 :text "note"}))]
                          ["y" (-> y-categorical (pj/lay-label {:y 99 :x 2.0 :text "note"}))]]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"past the ends of this axis"
                            (pj/plan pose))
          (str "on " label " -- a place off the axis was scaled and clipped away silently")))
    (is (some? (pj/plan (-> x-categorical (pj/lay-label {:x 3.5 :y 3.0 :text "note"}))))
        "and the end itself still draws")))

(deftest a-category-that-is-a-number-is-still-named-not-counted
  (testing "an axis built from numbers reads a place, and says so"
    (let [p (frame (-> {:cohort [2020 2021 2022] :n [3 5 4]}
                       (pj/lay-bar :cohort :n {:x-type :categorical})))
          at (fn [v] (first (pj/to-drawing p v 3.0)))]
      (is (= (at "2021") (at 2))
          "the middle band is named by its label and counted as place 2")
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"past the ends of this axis"
                            (at 2021))
          "and the value itself is a place far off the axis, not the band"))))

;; ---- The marks that draw at a place ----

(deftest a-bar-written-at-a-place-is-drawn-there
  (testing "the bin one unit wide around 1.5 spans the first two band centres"
    (let [base (polygon-spans x-categorical)
          with (polygon-spans (-> x-categorical (pj/lay-bar {:x 1.5 :y 3.0})))
          centre (fn [[lo hi]] (/ (+ lo hi) 2.0))]
      (is (= 3 (count base)))
      (is (= 4 (count with))
          "the bar used to be dropped, under a warning naming a log scale")
      ;; The tolerance is the SVG's own: coordinates are written to two
      ;; decimals, so two places that agree exactly still differ by up
      ;; to half a unit in the last one.
      (let [[lo hi] (last with)]
        (is (< (abs (- lo (centre (nth base 0)))) 0.01))
        (is (< (abs (- hi (centre (nth base 1)))) 0.01))))))

(defn- vertical-line-x
  "The x of the one line drawn straight down the panel. The grid and the
   axis run across it, so a line whose two ends share an x is the rule."
  [pose]
  (->> (re-seq #":points \"([0-9.-]+),[0-9.-]+ ([0-9.-]+),[0-9.-]+\"" (pr-str (pj/plot pose)))
       (keep (fn [[_ a b]] (when (= a b) (Double/parseDouble a))))
       first))

(defn- band-width
  "The width of the widest filled rectangle a band draws. The panel
   background is a rectangle too, and it is the wider of the two only
   when the band spans less than the whole drawing area."
  [pose]
  (->> (re-seq #"rect \{:fill[^}]*:width ([0-9.]+)," (pr-str (pj/plot pose)))
       (mapv (comp #(Double/parseDouble %) second))
       (reduce min)))

(deftest a-rule-and-a-band-are-drawn-at-their-place
  (testing "the marks that draw at a value written on the layer read a place too"
    ;; Each of these put its shape on the panel's left edge whatever
    ;; intercept it was given, because the band scale answered nil for a
    ;; number and the nil became zero; `pj/lay-band-v` with numeric
    ;; bounds died on a NullPointerException naming neither.
    (let [centre (fn [[lo hi]] (/ (+ lo hi) 2.0))
          bars (polygon-spans x-categorical)
          [a b c] (mapv centre bars)]
      (is (< (abs (- (vertical-line-x (-> x-categorical (pj/lay-rule-v {:x-intercept 1}))) a)) 0.01))
      (is (< (abs (- (vertical-line-x (-> x-categorical (pj/lay-rule-v {:x-intercept 2}))) b)) 0.01))
      (is (< (abs (- (vertical-line-x (-> x-categorical (pj/lay-rule-v {:x-intercept 1.5})))
                     (/ (+ a b) 2.0)))
             0.01)
          "and a place between two categories, as every other mark reads it")
      (is (< (abs (- (band-width (-> x-categorical (pj/lay-band-v {:x-min 1 :x-max 3})))
                     (- c a)))
             0.01)
          "a band spans from the first category's centre to the third's"))))

;; ---- What Known Limitations records ----
;;
;; `known_limitations.clj` carries no runnable examples -- the chapter is
;; prose -- so the claims it makes about places are held here, where they
;; fail if the behaviour moves and the page goes stale unnoticed.

(deftest a-rule-past-the-ends-draws-where-a-label-is-refused
  (testing "the gap the Known Limitations page records"
    (is (some? (pj/svg-summary
                (pj/plot (-> x-categorical (pj/lay-rule-v {:x-intercept 99})))))
        "the rule draws, far off the panel, with nothing said")
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"past the ends of this axis"
                          (pj/plan (-> x-categorical
                                       (pj/lay-label {:x 99 :y 3.0 :text "note"}))))
        "where a label written at the same value is refused by name")))

(deftest a-mark-that-occupies-a-band-asks-for-a-category-column
  (testing "and says so, rather than drawing at the place or dropping it"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires a categorical column"
                          (pj/plot (pj/lay-boxplot x-categorical {:x 1.5 :y 3.0}))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires a categorical column"
                          (pj/plot (pj/lay-violin x-categorical {:x 1.5 :y 3.0}))))))
