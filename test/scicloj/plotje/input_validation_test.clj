(ns scicloj.plotje.input-validation-test
  "Tests for the four input-validation guards added to close silent-
   failure gaps: nil data, non-collection data, empty-pose save, and
   non-map last arg in pj/pose's 4-arity."
  (:require [clojure.test :refer [deftest testing is]]
            [tablecloth.api :as tc]
            [java-time.api :as jt]
            [scicloj.plotje.api :as pj]))

(def tiny {:x [1.0 2.0 3.0] :y [4.0 5.0 6.0]})

(deftest nil-data-throws
  (testing "(pj/pose nil) throws with helpful message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data, but got nil"
         (pj/pose nil))))

  (testing "(pj/lay-point nil :x :y) throws (via ->pose)"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data, but got nil"
         (pj/lay-point nil :x :y))))

  (testing "(pj/options nil ...) throws (via ->pose)"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data, but got nil"
         (pj/options nil {:title "x"}))))

  (testing "the empty-pose 0-arity is unaffected"
    (is (pj/pose? (pj/pose)))))

(deftest non-collection-data-throws
  (testing "(pj/pose 42) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data"
         (pj/pose 42))))

  (testing "(pj/pose \"hello\") throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data"
         (pj/pose "hello"))))

  (testing "(pj/pose :a-keyword) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data"
         (pj/pose :a-keyword))))

  (testing "(pj/pose 42 :x :y) also throws (multi-arity rejects scalar)"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"requires data"
         (pj/pose 42 :x :y))))

  (testing "valid map data still works"
    (is (pj/pose? (pj/pose tiny))))

  (testing "valid sequence-of-maps data still works"
    (is (pj/pose? (pj/pose [{:a 1 :b 2} {:a 3 :b 4}])))))

(deftest pose-validation-is-published
  (testing "a pose built by the constructors conforms"
    ;; pj/valid-plan? and pj/valid-membrane? were published for the two
    ;; stages users are taught not to touch, while the pose -- the one
    ;; the book teaches them to build and assoc-in -- had none.
    (is (true? (pj/valid-pose? (pj/lay-point tiny :x :y))))
    (is (nil? (pj/explain-pose (pj/lay-point tiny :x :y))))
    (is (true? (pj/valid-pose? (pj/arrange [(pj/lay-point tiny :x :y)
                                            (pj/lay-line tiny :x :y)]))))
    (is (true? (pj/valid-pose? (-> {:x [1.0 2.0] :y [3.0 4.0] :g ["a" "b"]}
                                   (pj/lay-point :x :y)
                                   (pj/facet :g))))))

  (testing "a pose reached into and broken does not, and says why"
    (let [broken (assoc (pj/lay-point tiny :x :y) :layers {})]
      (is (false? (pj/valid-pose? broken)))
      (is (some? (pj/explain-pose broken)))))

  (testing "a value that is no pose at all"
    (is (false? (pj/valid-pose? 42)))
    (is (false? (pj/valid-pose? nil)))))

(deftest rows-given-as-vectors-are-data-not-a-membrane
  (testing "a vector of row vectors plots, and does not report a membrane"
    ;; `membrane.ui` extends IOrigin to vectors, so the rejection
    ;; diagnostic read [[1 2] [3 4]] as a hand-built drawable tree and
    ;; answered with a message about PlotjeMembrane. Timothy Pratley's
    ;; #32 thread began from data written this way.
    (is (pj/pose? (pj/lay-point [[1 2] [3 4]])))
    (is (= {:x 0 :y 1} (:mapping (pj/infer-mapping (pj/lay-point [[1 2] [3 4]]))))
        "the integer column names the coercion gives are inferred as usual")
    (is (= 3 (:points (pj/svg-summary (pj/plot (pj/lay-point [[1 2] [3 4] [5 6]]))))))
    (is (pj/pose? (pj/pose [[1 2] [3 4]] 0 1))
        "pj/pose already accepted this shape, and still does"))

  (testing "a real Membrane tree is still reported"
    (let [m (pj/membrane (pj/lay-point tiny :x :y))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Membrane drawable tree"
           (pj/lay-point m)))))

  (testing "a rendered hiccup vector is still reported as hiccup"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"rendered hiccup vector"
         (pj/lay-point (pj/plot (pj/lay-point tiny :x :y)))))))

(deftest template-idiom-still-works
  (testing "(pj/pose nil {:x :x :y :y}) supported as template"
    (let [tmpl (pj/pose nil {:x :x :y :y})]
      (is (pj/pose? tmpl))
      (is (nil? (:data tmpl)))
      (is (= {:x :x :y :y} (:mapping tmpl)))))

  (testing "(pj/pose nil :x :y) supported as template"
    (is (pj/pose? (pj/pose nil :x :y))))

  (testing "(pj/pose nil :x :y {:color :c}) supported as template"
    (let [tmpl (pj/pose nil :x :y {:color :c})]
      (is (pj/pose? tmpl))
      (is (= :c (-> tmpl :mapping :color))))))

(deftest empty-pose-save-throws
  (testing "(pj/save (pj/pose) ...) throws"
    (let [path "/tmp/_plotje_input_validation_test.svg"]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"empty pose -- nothing to render"
           (pj/save (pj/pose) path)))
      (is (not (.exists (java.io.File. path)))
          "no file written")))

  (testing "(pj/save (pj/pose) \"x.png\") throws"
    (let [path "/tmp/_plotje_input_validation_test.png"]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"empty pose -- nothing to render"
           (pj/save (pj/pose) path)))))

  (testing "save still works on a non-empty pose"
    (let [path "/tmp/_plotje_input_validation_test_ok.svg"]
      (pj/save (pj/lay-point tiny :x :y) path)
      (is (.exists (java.io.File. path)))
      (.delete (java.io.File. path)))))

(deftest non-map-opts-throws
  (testing "(pj/pose data x y :not-a-map) throws with helpful message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the last argument"
         (pj/pose [{:a 1 :b 2}] :a :b :c))))

  (testing "(pj/pose data x y nil) is accepted as no-opts"
    (is (pj/pose? (pj/pose [{:a 1 :b 2}] :a :b nil))))

  (testing "(pj/pose data x y {}) is accepted"
    (is (pj/pose? (pj/pose [{:a 1 :b 2}] :a :b {})))))

(deftest options-non-map-throws
  (testing "(pj/options pose <vector>) throws with helpful message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the second argument"
         (pj/options (pj/lay-point tiny :x :y) [:not :a :map]))))

  (testing "(pj/options pose 42) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the second argument"
         (pj/options (pj/lay-point tiny :x :y) 42))))

  (testing "(pj/options pose nil) is accepted (no-op)"
    (is (pj/pose? (pj/options (pj/lay-point tiny :x :y) nil))))

  (testing "(pj/options pose {}) is accepted"
    (is (pj/pose? (pj/options (pj/lay-point tiny :x :y) {})))))

(deftest lay-star-non-map-opts-throws
  (testing "(pj/lay-point ds :x :y <vector>) throws with helpful message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the last argument"
         (pj/lay-point tiny :x :y [:not :a :map]))))

  (testing "(pj/lay-point ds :x :y 42) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the last argument"
         (pj/lay-point tiny :x :y 42))))

  (testing "(pj/lay-line ds :x :y :not-a-map) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the last argument"
         (pj/lay-line tiny :x :y :foo))))

  (testing "(pj/lay-point ds :x :y nil) is accepted as no-opts"
    (is (pj/pose? (pj/lay-point tiny :x :y nil))))

  (testing "(pj/lay-point ds :x :y {}) is accepted"
    (is (pj/pose? (pj/lay-point tiny :x :y {})))))

(deftest error-messages-name-the-public-caller
  (testing "error from pj/lay-point on nil names pj/lay-point, not the private helper"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"^pj/lay-point requires data"
         (pj/lay-point nil :x :y))))

  (testing "error from pj/options on nil names pj/options"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"^pj/options requires data"
         (pj/options nil {:title "x"}))))

  (testing "error from pj/save on nil names pj/save"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"^pj/save requires data"
         (pj/save nil "/tmp/x.svg"))))

  (testing "error from pj/draft on nil names pj/draft"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"^pj/draft requires data"
         (pj/draft nil)))))

(deftest save-rejects-nonexistent-parent-dir
  (testing "pj/save into a nonexistent directory throws guidance, not raw IOException"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"parent directory .* does not exist"
         (pj/save (pj/lay-point tiny :x :y)
                  "/tmp/_plotje_no_such_dir_at_all/x.svg")))))

(deftest strict-config-rejects-non-boolean
  (testing "non-boolean :strict value throws at first read with explanation"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":strict config value must be true or false"
         (pj/with-config {:strict :yes}
           (pj/options (pj/lay-point tiny :x :y)
                       {:nonexistent-key 1}))))))

(deftest plot-on-plan-throws
  (testing "(pj/plot (pj/plan pose)) throws with helpful message"
    (let [pl (pj/plan (pj/lay-point tiny :x :y))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/plot expects a pose, not a plan"
           (pj/plot pl)))))

  (testing "the helpful error mentions pj/plan->plot as alternative"
    (let [pl (pj/plan (pj/lay-point tiny :x :y))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/plan->plot"
           (pj/plot pl))))))

(deftest draft-on-plan-throws
  (testing "(pj/draft (pj/plan pose)) throws with helpful message"
    (let [pl (pj/plan (pj/lay-point tiny :x :y))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/draft expects a pose, not a plan"
           (pj/draft pl)))))

  (testing "(pj/draft (pj/plan composite)) throws on composite plans too"
    (let [pl (pj/plan (pj/arrange [(pj/lay-point tiny :x :y)
                                   (pj/lay-line  tiny :x :y)]))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/draft expects a pose, not a plan"
           (pj/draft pl))))))

(deftest plot-on-draft-throws
  (testing "(pj/plot (pj/draft pose)) throws with helpful message"
    (let [d (pj/draft (pj/lay-point tiny :x :y))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/plot expects a pose, not a draft"
           (pj/plot d)))))

  (testing "(pj/plot (pj/draft composite)) throws on composite drafts too"
    (let [d (pj/draft (pj/arrange [(pj/lay-point tiny :x :y)
                                   (pj/lay-line  tiny :x :y)]))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/plot expects a pose, not a draft"
           (pj/plot d))))))

(deftest alpha-constant-out-of-range-throws
  (testing ":alpha > 1 throws at build-layer with helpful message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":alpha must be in \[0, 1\]"
         (pj/lay-point tiny :x :y {:alpha 1.5}))))

  (testing ":alpha < 0 throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":alpha must be in \[0, 1\]"
         (pj/lay-point tiny :x :y {:alpha -0.1}))))

  (testing ":alpha 0 and 1 are accepted (closed interval)"
    (is (pj/pose? (pj/lay-point tiny :x :y {:alpha 0})))
    (is (pj/pose? (pj/lay-point tiny :x :y {:alpha 1}))))

  (testing ":alpha as a column reference passes through"
    (is (pj/pose? (pj/lay-point tiny :x :y {:alpha :y})))
    (is (pj/pose? (pj/lay-point tiny :x :y {:alpha "y"})))))

(deftest size-constant-non-positive-throws
  (testing ":size 0 throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":size must be positive"
         (pj/lay-point tiny :x :y {:size 0}))))

  (testing ":size negative throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":size must be positive"
         (pj/lay-point tiny :x :y {:size -3}))))

  (testing "positive :size constant is accepted"
    (is (pj/pose? (pj/lay-point tiny :x :y {:size 5}))))

  (testing ":size as a column reference passes through"
    (is (pj/pose? (pj/lay-point tiny :x :y {:size :y})))))

(deftest mapping-value-symbol-throws
  (testing "(pj/pose data {:x 'col}) -- symbol value rejects with did-you-mean"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":x is a symbol .* did you mean :x\?"
         (pj/pose tiny {:x 'x :y :y}))))

  (testing "(pj/pose data {:color 'species}) -- symbol on color"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":color is a symbol .* did you mean :species\?"
         (pj/pose tiny {:x :x :y :y :color 'species}))))

  (testing "(pj/lay-point ... {:color 'species}) at lay-* path"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":color is a symbol"
         (pj/lay-point tiny :x :y {:color 'species})))))

(deftest mapping-value-nil-passes-through
  (testing "{:color nil} on a layer cancels an inherited color (intentional)"
    (is (some? (-> (pj/pose tiny {:x :x :y :y :color :y})
                   (pj/lay-point {:color nil})
                   pj/plan
                   :panels)))))

(deftest legend-position-enum-validates
  (testing "non-enum :legend-position throws with accepted-set guidance"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":legend-position must be one of"
         (pj/options (pj/lay-point tiny :x :y) {:legend-position :weird}))))

  (testing "string is rejected -- must be a keyword from the enum"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":legend-position must be one of"
         (pj/options (pj/lay-point tiny :x :y) {:legend-position "right"}))))

  (testing "all enum values pass: :right :bottom :top :none"
    (doseq [v [:right :bottom :top :none]]
      (is (pj/pose? (pj/options (pj/lay-point tiny :x :y)
                                {:legend-position v}))
          (str "legend-position " v " should be accepted"))))

  (testing "options without :legend-position is unaffected"
    (is (pj/pose? (pj/options (pj/lay-point tiny :x :y) {:title "ok"})))))

(deftest scales-enum-validates
  (testing "a value the retired :scales never took throws, naming :share-scales"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":scales is retired, and :weird is not one of the values it took.*Write :share-scales"
         (pj/options (pj/lay-point tiny :x :y) {:scales :weird}))))

  (testing "string is rejected -- must be a keyword from the enum"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":scales is retired"
         (pj/options (pj/lay-point tiny :x :y) {:scales "free"}))))

  (testing "the values it took are read, with a warning: :shared :free :free-x :free-y"
    (doseq [v [:shared :free :free-x :free-y]]
      (let [out (with-out-str
                  (is (pj/pose? (pj/options (pj/lay-point tiny :x :y)
                                            {:scales v}))
                      (str "scales " v " should be accepted")))]
        (is (re-find #":scales is retired" out)))))

  (testing "options without :scales is unaffected"
    (is (pj/pose? (pj/options (pj/lay-point tiny :x :y) {:title "ok"})))))

(deftest arrange-rejection-branches-on-type
  (testing "(pj/arrange [nil]) -- every cell empty -- says there is nothing to draw"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"given 1 cell, and every one is nil, a cell left empty"
         (pj/arrange [nil]))))

  (testing "(pj/arrange [non-pose-map]) names the missing :layers/:poses"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"is a map but not a pose"
         (pj/arrange [{:not-a :pose}]))))

  (testing "(pj/arrange [hiccup-vector]) keeps hiccup-specific guidance"
    (let [pre-rendered (pj/plot (pj/lay-point tiny :x :y))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"looks like rendered hiccup"
           (pj/arrange [pre-rendered pre-rendered])))))

  (testing "(pj/arrange [scalar]) names the actual type"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"must be a pose\. Got: java.lang.Long"
         (pj/arrange [42]))))

  (testing "plain vector inside an explicit grid hits the plain-vector branch"
    (let [p (pj/lay-point tiny :x :y)]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"is a plain vector"
           (pj/arrange [[p [1 2 3]]]))))))

(deftest arrange-share-scales-validation
  (testing "non-#{:x :y} keys are rejected with the accepted set named"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":share-scales must be a subset of"
         (pj/arrange [(pj/lay-point tiny :x :y)] {:share-scales #{:x :z}}))))

  (testing ":share-scales accepts both #{:x} and #{:y} subsets"
    (doseq [v [#{} #{:x} #{:y} #{:x :y} [:x] [:y]]]
      (is (some? (pj/arrange [(pj/lay-point tiny :x :y)] {:share-scales v}))
          (str "share-scales " v " should be accepted")))))

(deftest single-leaf-arrange-overrides-width-warns
  (testing "single-leaf arrange with conflicting :width prints a warning"
    (let [pose (-> tiny
                   (pj/lay-point :x :y)
                   (pj/options {:width 800 :height 400}))
          out (with-out-str (pj/arrange [pose]))]
      (is (re-find #"pj/arrange wraps a single leaf" out)
          "warns about the single-leaf override")
      (is (re-find #"800x400" out)
          "names the leaf's overridden dimensions")))

  (testing "matching :width does not warn"
    (let [pose (-> tiny
                   (pj/lay-point :x :y)
                   (pj/options {:width 600 :height 400}))
          out (with-out-str (pj/arrange [pose]))]
      (is (= "" out)
          "silent when leaf dims match composite dims")))

  (testing "no width set on the leaf does not warn"
    (let [pose (pj/lay-point tiny :x :y)
          out (with-out-str (pj/arrange [pose]))]
      (is (= "" out))))

  (testing "multi-leaf arrange does not warn even with :width on a leaf"
    (let [a (-> tiny (pj/lay-point :x :y) (pj/options {:width 800}))
          b (pj/lay-point tiny :x :y)
          out (with-out-str (pj/arrange [a b]))]
      (is (= "" out)))))

(deftest pose-2-arity-extracts-data-from-opts
  (testing "(pj/pose nil {:data X :x ... :y ...}) attaches X as data, mapping omits :data"
    (let [data {:a [1 2 3] :b [4 5 6]}
          p    (pj/pose nil {:data data :x :a :y :b})]
      (is (pj/pose? p))
      (is (= data (:data p)))
      (is (= {:x :a :y :b} (:mapping p)))))

  (testing "(pj/pose data {:data new-data ...}) opts :data overrides positional data"
    (let [orig {:x [1 2] :y [3 4]}
          new  {:a [10 20] :b [30 40]}
          p    (pj/pose orig {:data new :x :a :y :b})]
      (is (= new (:data p)))
      (is (= {:x :a :y :b} (:mapping p)))))

  (testing "(pj/pose data {:x ... :y ...}) without :data uses positional data"
    (let [data {:a [1 2 3] :b [4 5 6]}
          p    (pj/pose data {:x :a :y :b})]
      (is (= data (:data p)))
      (is (= {:x :a :y :b} (:mapping p)))))

  (testing "extending an existing pose: opts :data replaces the dataset"
    (let [base (pj/pose {:a [1 2] :b [3 4]} {:x :a :y :b})
          new  {:a [10 20 30] :b [40 50 60]}
          ext  (pj/pose base {:data new :color :a})]
      (is (= (tc/dataset new) (:data ext))
          "opts :data replaces base's data on extend (consistent across arities)"))))

(deftest a-number-position-is-resolved-by-the-data-test
  ;; A number in an `:x` or `:y` mapping used to be refused everywhere
  ;; except a layer's options map, on the reasoning that the check runs
  ;; before the data is available and cannot tell a column name from a
  ;; place. The source question is answered at draft time for every
  ;; other mapping, so it is answered there for these too, and the rule
  ;; is now one rule wherever the mapping is written.
  (let [ints {0 [1 2 3] 1 [4 5 6]}
        x-dom #(-> % pj/plan :panels first :x-domain)
        y-dom #(-> % pj/plan :panels first :y-domain)]

    (testing "an integer column name reads that column, in every arity"
      (doseq [[label p] [["pose map"        (-> (pj/pose ints {:x 0 :y 1}) pj/lay-point)]
                         ["pose 3-arity"    (-> (pj/pose ints 0 1) pj/lay-point)]
                         ["lay-* positional" (pj/lay-point ints 0 1)]
                         ["lay-* opts map"  (pj/lay-point ints {:x 0 :y 1})]
                         ["lay-* 4-arity"   (pj/lay-point ints 0 1 {})]
                         ["x-only arity"    (pj/lay-histogram ints 0)]]]
        (is (= [0.9 3.1] (x-dom p)) label)))

    (testing "and the axis is labelled with the column's name"
      (is (= ["0" "1"] ((juxt :x-label :y-label)
                        (pj/plan (-> (pj/pose ints {:x 0 :y 1}) pj/lay-point))))))

    (testing "a number the data carries no column for is a datum on the axis"
      (is (= [5.5 7.5] (x-dom (-> (pj/pose tiny {:x 6.5 :y :y}) pj/lay-point))))
      (is (= [5.5 7.5] (y-dom (-> (pj/pose tiny {:x :x :y 6.5}) pj/lay-point)))))

    (testing "the explicit form still says which reading is meant"
      (is (= [0.9 3.1] (x-dom (pj/lay-point ints {:x {:column 0} :y 1}))))
      (is (= [-1.0 1.0] (x-dom (pj/lay-point ints {:x {:value 0} :y 1})))))

    (testing "a template resolves against the data it is later given"
      (is (= [0.9 3.1] (x-dom (-> (pj/pose) (pj/pose {:x 0 :y 1})
                                  pj/lay-point (pj/with-data ints))))))

    (testing "valid column refs still work in every arity"
      (is (pj/pose? (pj/lay-point tiny :x :y)))
      (is (pj/pose? (pj/lay-point tiny :x :y {})))
      (is (pj/pose? (pj/lay-point tiny "x" "y")))
      (is (pj/pose? (pj/lay-point tiny {:x :x :y :y})))
      (is (pj/pose? (pj/lay-point tiny)))
      (is (pj/pose? (pj/pose tiny {:x :x :y :y})))
      (is (pj/pose? (pj/pose tiny {:x "x" :y "y"}))))))

(deftest a-position-that-is-neither-is-refused-test
  ;; What is left of the gate: a value that names no column and places
  ;; no mark either.
  (doseq [v [[1 2] true #{1}]]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":x must be a column reference or a value to place a mark at"
         (pj/pose tiny {:x v :y :y}))
        (pr-str v)))

  (testing "a number on an appearance aesthetic is a constant, not a column"
    (is (= #{1.0} (:sizes (pj/svg-summary (pj/lay-point tiny :x :y {:size 1})))))
    (is (= #{9.0} (:sizes (pj/svg-summary (pj/lay-point tiny :x :y {:size 9})))))))

(deftest hiccup-vector-input-throws-test
  ;; user-report-3 issue 3: saving the hiccup output of pj/plot
  ;; previously failed with a deep "Tensors must be 2 dimensional" error
  ;; from tc/dataset. Detect a hiccup-shaped vector at the input gate.
  (let [hiccup [:svg {:width 100} [:rect {:x 1 :y 1 :width 50 :height 50}]]]
    (testing "(pj/pose hiccup) throws with rendered-hiccup guidance"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"rendered hiccup vector"
           (pj/pose hiccup))))

    (testing "(pj/lay-point hiccup ...) throws with rendered-hiccup guidance"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"rendered hiccup vector"
           (pj/lay-point hiccup :x :y))))

    (testing "(pj/save hiccup \"x.png\") throws with rendered-hiccup guidance"
      (let [path "/tmp/_plotje_hiccup_input_test.png"]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"rendered hiccup vector"
             (pj/save hiccup path)))
        (is (not (.exists (java.io.File. path)))
            "no file written"))))

  (testing "vectors of row-maps are not flagged as hiccup"
    (is (pj/pose? (pj/pose [{:a 1 :b 2} {:a 3 :b 4}])))))

(deftest bare-scalar-column-input
  ;; A bare list of scalars becomes a single :value column (D4). The
  ;; hiccup guard is narrowed so an all-keyword vector reads as data,
  ;; not as rendered hiccup.
  (testing "vector of numbers -> single :value column"
    (is (= [:value] (tc/column-names (:data (pj/pose [1 4 1 5 6]))))))

  (testing "vector of strings -> single :value column"
    (is (= [:value] (tc/column-names (:data (pj/pose ["a" "b" "a"]))))))

  (testing "vector of keywords -> single :value column (not hiccup)"
    (is (= [:value] (tc/column-names (:data (pj/pose [:a :b :c :a]))))))

  (testing "keyword-headed vector with non-keyword elements is still hiccup"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"rendered hiccup vector"
         (pj/pose [:svg {:width 100} [:g]])))))

(deftest empty-collection-data-throws
  (testing "(pj/pose []) throws with empty-collection guidance"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"empty collection"
         (pj/pose []))))

  (testing "(pj/pose {}) throws with empty-collection guidance"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"empty collection"
         (pj/pose {}))))

  (testing "(pj/lay-point [] :x :y) also throws (via ->pose)"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"empty collection"
         (pj/lay-point [] :x :y))))

  (testing "the empty-pose 0-arity is unaffected"
    (is (pj/pose? (pj/pose))))

  (testing "the guidance points at (pj/pose) for the template"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"empty pose template"
         (pj/pose [])))))

(deftest zero-row-cell-renders-no-data-placeholder
  (testing "zero-row pose with explicit layer renders 'no data' placeholder"
    (let [empty-data (tc/dataset {:x [] :y []})
          pose (-> empty-data
                   (pj/pose {:x :x :y :y})
                   (pj/lay :point))
          texts (:texts (pj/svg-summary (pj/plot pose)))]
      (is (some #(= % "no data") texts)
          "rendered SVG includes the 'no data' placeholder text")))

  (testing "populated panel does NOT show the placeholder"
    (let [pose (pj/lay-point tiny :x :y)
          texts (:texts (pj/svg-summary (pj/plot pose)))]
      (is (not (some #(= % "no data") texts)))))

  (testing "zero-row cell inside arrange composite shows placeholder only on that cell"
    (let [a (-> tiny (pj/lay-point :x :y))
          b (-> (tc/dataset {:x [] :y []})
                (pj/pose {:x :x :y :y})
                (pj/lay :point))
          texts (:texts (pj/svg-summary (pj/plot (pj/arrange [a b]))))
          n-no-data (count (filter #(= % "no data") texts))]
      (is (= 1 n-no-data)
          "the empty cell renders 'no data'; the populated cell does not")))

  (testing "a panel whose only layer is a rule does not get the placeholder"
    ;; The rule has to be the only layer for this to test anything. With
    ;; a point layer beside it the point draws marks on its own, so the
    ;; assertion passes whether or not the rule counts -- which is what
    ;; it did after the rule stopped being an annotation and the
    ;; `(empty? annotations)` clause it guarded left `render/panel.clj`.
    (let [empty-ds (tc/dataset {:x [] :y []})
          rule-only (-> empty-ds
                        (pj/pose {:x :x :y :y})
                        (pj/lay-rule-h {:y-intercept 2}))
          nothing (-> empty-ds
                      (pj/pose {:x :x :y :y})
                      (pj/lay :point))
          says-no-data? (fn [pose]
                          (boolean (some #(= % "no data")
                                         (:texts (pj/svg-summary (pj/plot pose))))))]
      (is (not (says-no-data? rule-only))
          "a rule draws a mark, so the panel is not empty")
      (is (says-no-data? nothing)
          "the control: a panel that really draws nothing still says so"))))

(deftest options-width-height-non-number-throws
  (testing "(pj/options pose {:width \"800\"}) throws with type message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":width must be a number"
         (pj/options (pj/lay-point tiny :x :y) {:width "800"}))))

  (testing "(pj/options pose {:height :keyword}) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":height must be a number"
         (pj/options (pj/lay-point tiny :x :y) {:height :keyword}))))

  (testing "(pj/options pose {:width 800.5}) still rounds correctly"
    (let [p (pj/options (pj/lay-point tiny :x :y) {:width 800.5})]
      (is (= 801 (-> p :opts :width)))))

  (testing "(pj/options pose {:width 0.4}) throws round-to-zero error"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"must round to a positive integer"
         (pj/options (pj/lay-point tiny :x :y) {:width 0.4})))))

(deftest cross-argument-validation
  (testing "(pj/cross :scalar :scalar) throws"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"two sequentials of column"
         (pj/cross :a :b))))

  (testing "(pj/cross [:a] :scalar) throws naming the bad argument"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"two sequentials of column"
         (pj/cross [:a] :b))))

  (testing "(pj/cross [] [:c :d]) throws on empty xs"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"empty sequence"
         (pj/cross [] [:c :d]))))

  (testing "(pj/cross [:a :b] []) throws on empty ys"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"empty sequence"
         (pj/cross [:a :b] []))))

  (testing "(pj/cross [:a :b] [:c :d]) returns the 2x2 cross product"
    (is (= [[:a :c] [:a :d] [:b :c] [:b :d]]
           (pj/cross [:a :b] [:c :d])))))

(deftest pj-lay-eager-layer-type-validation
  (let [pose (pj/pose tiny :x :y)]
    (testing "unknown keyword throws at pj/lay, not deferred to plan"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Unknown layer type: :stackedbar.*Registered layer types"
           (pj/lay pose :stackedbar))))

    (testing "registered keyword passes through"
      (is (some? (pj/lay pose :point))))

    (testing ":infer sentinel passes through"
      (is (some? (pj/lay pose :infer))))

    (testing "extension map (entry shape) passes through"
      (is (some? (pj/lay pose {:mark :point}))))))

(deftest pj-facet-bad-direction-throws-ex-info
  (let [pose (pj/lay-point tiny :x :y)]
    (testing "(pj/facet pose col :diagonal) throws ex-info naming the accepted directions"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/facet direction must be :col or :row, got: :diagonal"
           (pj/facet pose :y :diagonal))))

    (testing ":col and :row pass through"
      (is (some? (pj/facet pose :y :col)))
      (is (some? (pj/facet pose :y :row))))))

(deftest plan-warns-on-out-of-range-breaks
  (let [iris (tc/dataset {:sepal-length [4.0 5.0 6.0]
                          :sepal-width  [2.0 3.0 4.0]})]
    (testing "out-of-range breaks warn naming the offending values"
      (let [out (with-out-str
                  (-> iris
                      (pj/lay-point :sepal-length :sepal-width)
                      (pj/scale :y {:type :linear :breaks [0 100 200]})
                      pj/plan))]
        (is (re-find #":breaks" out))
        (is (re-find #"\[0 100 200\]" out))
        (is (re-find #"outside the data domain" out))
        (is (re-find #":domain" out))))

    (testing "in-range breaks silent"
      (let [out (with-out-str
                  (-> iris
                      (pj/lay-point :sepal-length :sepal-width)
                      (pj/scale :y {:type :linear :breaks [2.0 3.0 4.0]})
                      pj/plan))]
        (is (= "" out))))

    (testing "partially out-of-range names only the offending values"
      (let [out (with-out-str
                  (-> iris
                      (pj/lay-point :sepal-length :sepal-width)
                      (pj/scale :y {:type :linear :breaks [3.0 100]})
                      pj/plan))]
        (is (re-find #"\[100\]" out))))

    (testing "strict mode upgrades to throw"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"outside the data domain"
           (pj/with-config {:strict true}
             (-> iris
                 (pj/lay-point :sepal-length :sepal-width)
                 (pj/scale :y {:type :linear :breaks [0 100 200]})
                 pj/plan)))))))

(deftest scale-labels-attach-to-breaks
  (let [data (tc/dataset {:day  [1 2 3 4 5 6 7]
                          :hour [0 6 12 18 0 6 12]
                          :v    [0.1 0.5 0.9 0.4 0.2 0.7 0.3]})
        days ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]
        plan (-> data
                 (pj/lay-tile :day :hour {:fill :v})
                 (pj/scale :x {:type :linear
                               :breaks [1 2 3 4 5 6 7]
                               :tick-labels days})
                 pj/plan)
        x-labels (-> plan :panels first :x-ticks :labels)
        x-values (-> plan :panels first :x-ticks :values)]
    (testing "user-supplied labels appear at user-supplied break positions"
      (is (= days x-labels))
      (is (= [1 2 3 4 5 6 7] (mapv long x-values))))))

(deftest scale-labels-validation
  (let [data (tc/dataset {:x [1 2 3] :y [10 20 30]})]
    (testing ":tick-labels without :breaks throws"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #":tick-labels requires :breaks"
           (-> data
               (pj/lay-point :x :y)
               (pj/scale :x {:type :linear :tick-labels ["a" "b" "c"]})))))

    (testing ":breaks and :tick-labels with mismatched counts throws"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"same count"
           (-> data
               (pj/lay-point :x :y)
               (pj/scale :x {:type :linear
                             :breaks [1 2 3]
                             :tick-labels ["a" "b"]})))))

    ;; The axis title and the tick text are one character apart, so a
    ;; text where a vector belongs is refused by name rather than
    ;; counted as one label per character.
    (testing ":tick-labels given a single string is refused, and says why"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #":tick-labels \"Axis title\" is not a sequence"
           (-> data
               (pj/lay-point :x :y)
               (pj/scale :x {:breaks [1 2] :tick-labels "Axis title"})))))))

(deftest plan-warns-on-unused-pose-mapping-keys
  (let [data (tc/dataset {:x [1 2 3] :y [10 20 30]
                          :ymin [5 15 25] :ymax [15 25 35]})]
    (testing ":y-min/:y-max at pose level with only lay-point warns"
      (let [out (with-out-str
                  (-> data
                      (pj/pose {:x :x :y :y :y-min :ymin :y-max :ymax})
                      pj/lay-point
                      pj/plan))]
        (is (re-find #":y-min" out))
        (is (re-find #":y-max" out))
        (is (re-find #"no descendant layer accepts" out))))

    (testing "silent when consuming layer is present"
      (let [out (with-out-str
                  (-> data
                      (pj/pose {:x :x :y :y :y-min :ymin :y-max :ymax})
                      pj/lay-errorbar
                      pj/plan))]
        (is (= "" out))))

    (testing "silent when no explicit layer (inference path)"
      (let [out (with-out-str (pj/plan (pj/pose data :x :y)))]
        (is (= "" out))))

    (testing "silent when only universals at pose level"
      (let [out (with-out-str
                  (-> data
                      (pj/pose {:x :x :y :y :color :y :alpha :y})
                      pj/lay-point
                      pj/plan))]
        (is (= "" out))))

    (testing "strict mode upgrades to throw"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"no descendant layer accepts"
           (pj/with-config {:strict true}
             (-> data
                 (pj/pose {:x :x :y :y :y-min :ymin})
                 pj/lay-point
                 pj/plan)))))))

(deftest errorbar-range-bounds-are-columns
  (let [data (tc/dataset {:x [1 2 3] :y [10.0 20.0 30.0]
                          :lo [5.0 15.0 25.0] :hi [15.0 25.0 35.0]})]
    (testing "two columns draw"
      (is (some? (pj/plan (pj/lay-errorbar data :x :y {:y-min :lo :y-max :hi})))))

    (testing "a number is reported, naming the mark, the key and what was written"
      (doseq [[opts key] [[{:y-min 5.0 :y-max :hi} ":y-min"]
                          [{:y-min :lo :y-max 35.0} ":y-max"]]]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             (re-pattern (str "lay-errorbar " key " [0-9.]+ is not a column"))
             (pj/plan (pj/lay-errorbar data :x :y opts)))
            "it died on a NullPointerException naming neither")))

    (testing "and where the whole mark is written rather than read from columns"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"lay-errorbar :y-min .* is not a column"
           (pj/plan (-> data
                        (pj/lay-point :x :y)
                        (pj/lay-errorbar {:x 2 :y 3.0 :y-min 2.0 :y-max 4.0}))))))

    (testing "the same two keys still take a written value on a band, which is the other reading"
      (is (some? (pj/plan (-> data
                              (pj/lay-point :x :y)
                              (pj/lay-band-h {:y-min 12.0 :y-max 18.0}))))))

    (testing "neither given is still reported as before"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"requires :y-min and :y-max columns"
           (pj/plan (pj/lay-errorbar data :x :y)))))))

(deftest plan-on-bare-template-throws-clear-error
  (testing "(pj/plan (pj/pose nil :x :y)) throws clear error instead of cryptic 'Unknown mark: nil'"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/pose->draft: got a pose with no data and no layers"
         (pj/plan (pj/pose nil :x :y))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Add a layer with pj/lay-\*"
         (pj/plan (pj/pose nil {:x :a :y :b})))))

  (testing "every shortcut that threads through pose->draft surfaces the same error"
    (doseq [shortcut [pj/draft pj/plan pj/membrane pj/plot pj/pose->draft]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/pose->draft: got a pose with no data and no layers"
           (shortcut (pj/pose nil :x :y))))))

  (testing "zero-arity (pj/pose) is not a bare template -- still plannable"
    (is (some? (pj/plan (pj/pose)))))

  (testing "mapping + data + no layers is plannable (data-driven inference)"
    (is (some? (pj/plan (pj/pose tiny :x :y)))))

  (testing "composite with data at root + mapping-only sub-poses is plannable"
    (is (some? (pj/plan (-> tiny
                            (pj/arrange [[:x :y] [:y :x]])
                            pj/lay-point))))))

(deftest lay-2arity-opts-map-auto-infers-from-raw-data
  (let [small (tc/dataset {:x [1.0 2.0] :y [10.0 20.0]})
        big (tc/dataset {:a [1] :b [2] :c [3] :d [4] :e [5]})]
    ;; The mapping is inferred at draft time; `pj/infer-mapping` is the
    ;; step the draft entry points run.
    (testing "(lay-rule-h raw-data {:y-intercept ...}) auto-infers x/y mapping"
      (let [pose (pj/lay-rule-h small {:y-intercept 5})]
        (is (= {:x :x :y :y} (:mapping (pj/infer-mapping pose))))
        (is (some? (:data pose)))))

    (testing "(lay-point raw-data {:color ...}) auto-infers x/y mapping"
      (is (= {:x :x :y :y}
             (:mapping (pj/infer-mapping (pj/lay-point small {:color :x}))))))

    (testing "(lay-line raw-data {:color ...}) auto-infers"
      (is (= {:x :x :y :y}
             (:mapping (pj/infer-mapping (pj/lay-line small {:color :x}))))))

    (testing "raw data with 4+ columns: drawn, the layer reports its missing columns"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"no column on :x or :y. It names :color :a.*The data has 5 columns, so none is read by default"
           (pj/plan (pj/lay-point big {:color :a})))))

    (testing "pose input is unaffected -- no auto-infer triggers on a pose"
      (let [bare-pose (pj/pose)
            pose (pj/lay-point bare-pose {:color :x})]
        (is (nil? (:mapping pose)))))))

(deftest a-layer-with-no-column-says-what-its-type-draws-from
  ;; Reported at the draft: the call builds a pose, and a later step may
  ;; still name the columns.
  (let [big (tc/dataset {:a [1] :b [2] :c [3] :d [4] :e [5]})]
    (testing "the call does not throw"
      (doseq [lay [pj/lay-point pj/lay-bar pj/lay-histogram pj/lay-density pj/lay-rug]]
        (is (= 1 (count (:layers (lay big)))))))

    (testing "a layer type drawing from two columns suggests both"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/lay-point draws from two columns.*\(pj/lay-point pose :x-column :y-column\)"
           (pj/plan (pj/lay-point big)))))

    (testing "a layer type drawing from one column suggests :x only"
      (doseq [[lay nm] [[pj/lay-histogram "histogram"]
                        [pj/lay-density "density"]
                        [pj/lay-rug "rug"]]]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             (re-pattern (str "pj/lay-" nm " draws from one column, on :x.*"
                              "\\(pj/lay-" nm " pose :x-column\\)"))
             (pj/plan (lay big))))))

    (testing "a layer type drawing from one column or two says so"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"pj/lay-bar draws from one column on :x, or two on :x and :y"
           (pj/plan (pj/lay-bar big)))))))

(deftest lay-star-docstrings-list-accepted-options
  (let [layer-type-keys (keys (scicloj.plotje.layer-type/registered))]
    (testing "every registered layer type has a corresponding pj/lay-K with an Accepted-options block"
      (doseq [k layer-type-keys]
        (let [v (resolve (symbol "scicloj.plotje.api" (str "lay-" (name k))))
              doc (:doc (meta v))]
          (is (some? v) (str "missing pj/lay-" (name k)))
          (is (re-find #"Accepted options:" doc)
              (str "pj/lay-" (name k) " docstring lacks Accepted options block")))))

    (testing "annotation rejects (:position :group :x-type :y-type :color-type) are filtered out for lay-rule-h"
      (let [doc (:doc (meta #'pj/lay-rule-h))]
        (is (not (re-find #":position" doc))
            "lay-rule-h docstring should not list :position")
        (is (not (re-find #":x-type" doc))
            "lay-rule-h docstring should not list :x-type")))

    (testing "lay-histogram's accepted-options block lists its layer-type-specific :bins"
      ;; Against the generated block alone, not the whole docstring: the
      ;; prose above it names `:bins` too, so matching the docstring
      ;; would pass even if the block dropped the option. The closing
      ;; backtick is what keeps `:bins` from matching `:binwidth`.
      (let [block (second (re-find #"(?s)(Accepted options:.*)\z"
                                   (:doc (meta #'pj/lay-histogram))))]
        (is (some? block) "lay-histogram docstring lacks an Accepted options block")
        (is (re-find #"`:bins`" block))))))

(deftest the-anchor-options-belong-to-text-alone
  (testing "a non-text layer warns, names where they belong, and ignores them"
    ;; The Placing Marks chapter says these two are the only options in it
    ;; that are text-specific. Nothing else pinned that.
    (let [out (with-out-str
                (pj/lay-point {:x [1 2] :y [3 4]} :x :y {:align-x :right}))]
      (is (re-find #"lay-point does not recognize option\(s\): \[:align-x\]" out))
      (is (re-find #"\[:label :text\]" out)))
    (is (nil? (->> (pj/plan (pj/lay-point {:x [1 2] :y [3 4]} :x :y
                                          {:align-x :right}))
                   :panels first :layers first :style :align-x))
        "the option is stripped rather than carried into the plan")))

(def four-columns
  "Four columns, so no column is read by default."
  {:t [1 2 3] :a [1.0 2.0 3.0] :b [3.0 2.0 1.0] :g ["p" "q" "p"]})

(defn- axis-labels [pose]
  ((juxt :x-label :y-label) (pj/plan pose)))

(deftest an-options-map-carrying-the-mapping-skips-inference
  (testing "a mapping given in the options map is honored on 4+ columns"
    ;; The guard used to run before the options map was applied, so it
    ;; refused the mapping it was asking for, on data too wide to guess.
    (is (= ["t" "a"] (axis-labels (pj/lay-point four-columns {:x :t :y :a}))))
    (is (= ["a" nil] (axis-labels (pj/lay-histogram four-columns {:x :a})))
        "an x-only layer type needs only :x to count as mapped"))

  (testing "an options map naming no column leaves the layer without one"
    (doseq [opts [{} {:size 5}]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"The data has 4 columns, so none is read by default"
           (pj/plan (pj/lay-point four-columns opts)))
          (str "options " opts " name no column on :x or :y"))))

  (testing "a partial mapping still infers, so the other axis is filled in"
    ;; Three columns, where inference succeeds: {:y :a} keeps the inferred
    ;; :x and overrides :y. Unchanged behavior, pinned because the fix
    ;; could have skipped inference here too.
    (is (= ["t" "a"]
           (axis-labels (pj/lay-point {:t [1 2 3] :a [1.0 2.0 3.0] :b [3.0 2.0 1.0]}
                                      {:y :a}))))))

;; ---- Aesthetics that used to fail in silence or by cast error ----

(def mixed
  {:num [1.0 2.0 3.0 4.0] :num2 [2.0 3.0 4.0 5.0] :cat ["p" "q" "p" "q"]})

(deftest a-categorical-column-in-a-continuous-aesthetic-reports-itself
  (testing ":size, :alpha and :fill name the aesthetic and the column"
    ;; These used to reach the encoder and die on
    ;; "class java.lang.String cannot be cast to class java.lang.Number",
    ;; naming neither.
    (doseq [[aesthetic pose] {:size  (pj/lay-point mixed :num :num2 {:size :cat})
                              :alpha (pj/lay-point mixed :num :num2 {:alpha :cat})
                              :fill  (pj/lay-tile mixed :num :num2 {:fill :cat})}]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"needs a numeric column, but :cat holds categories"
           (pj/plan pose))
          (str aesthetic " reports the categorical column"))))

  (testing "a numeric column and a literal still work, and :color takes categories"
    (is (pj/plan (pj/lay-point mixed :num :num2 {:size :num})))
    (is (pj/plan (pj/lay-point mixed :num :num2 {:size 4})))
    (is (pj/plan (pj/lay-point mixed :num :num2 {:color :cat})))))

(deftest a-value-in-a-column-only-aesthetic-is-refused
  (testing ":group used to empty the layer in silence"
    ;; Zero groups, so the layer drew nothing at all and nothing said why.
    (doseq [v [4 true]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #":group takes one column, or a vector of columns"
           (pj/plan (pj/lay-line mixed :num :num2 {:group v}))))))

  (testing ":fill used to be ignored"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":fill takes one column"
         (pj/plan (pj/lay-tile mixed :num :num2 {:fill 4})))))

  (testing ":shape takes a symbol now, and still refuses what is neither"
    ;; It gained a written-value reading, so the message that named its
    ;; one accepted shape gave way to one naming both readings.
    (is (= :square (-> (pj/lay-point mixed :num :num2 {:shape :square})
                       pj/plan :panels first :layers first :style :shape)))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"not found in dataset.*not a symbol either"
         (pj/plan (pj/lay-point mixed :num :num2 {:shape 4}))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"not found in dataset.*not a symbol either"
         (pj/plan (pj/lay-point mixed :num :num2 {:shape :sphere})))))

  (testing "columns, several grouping columns, and nil all still pass"
    (is (pj/plan (pj/lay-line mixed :num :num2 {:group :cat})))
    (is (pj/plan (pj/lay-line mixed :num :num2 {:group [:cat]})))
    (is (pj/plan (pj/lay-point mixed :num :num2 {:shape :cat})))
    (is (pj/plan (-> (pj/pose mixed {:x :num :y :num2 :group :cat})
                     (pj/lay-line {:group nil})))
        "nil cancels a mapping inherited from the pose")))

(deftest scale-has-no-group-channel
  (testing "pj/scale :group is refused rather than accepted and ignored"
    ;; It used to write a :group-scale that nothing stamped and nothing
    ;; read, so the plot was identical with and without it.
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"pj/scale has no :group aesthetic"
         (pj/scale (pj/lay-line mixed :num :num2 {:group :cat})
                   :group {:domain ["q" "p"]}))))

  (testing "the channels that draw something are unaffected"
    (is (pj/plan (pj/scale (pj/lay-point mixed :num :num2 {:shape :cat})
                           :shape {:domain ["q" "p"]})))
    (is (pj/plan (pj/scale (pj/lay-point mixed :num :num2) :x :log)))))

(def strings {:s ["p" "q" "r"] :n [1.0 2.0 3.0]})
(def numbers {:s [1.0 2.0 3.0] :n [1.0 2.0 3.0]})
(def dates {:s [(jt/local-date 2020 1 1) (jt/local-date 2020 6 1)] :n [1.0 2.0]})

(defn- msg
  "The message a plan reports, or nil where it plans."
  [pose]
  (try (pj/plan pose) nil
       (catch clojure.lang.ExceptionInfo e (ex-message e))
       (catch Exception e (ex-message e))))

(deftest only-categorical-can-be-asked-of-another-type-test
  ;; Every value can name a group, so :categorical is always available.
  ;; The other two have nothing to read on a column holding something
  ;; else, and used to reach the cast -- which reported a String or a
  ;; LocalDate and named neither the column nor the option.
  (testing ":categorical is accepted on every kind of column"
    (is (nil? (msg (pj/lay-point numbers :s :n {:x-type :categorical}))))
    (is (nil? (msg (pj/lay-point strings :s :n {:x-type :categorical}))))
    (is (nil? (msg (pj/lay-point dates :s :n {:x-type :categorical})))))

  (testing ":numerical and :temporal are accepted where they name the column's own type"
    (is (nil? (msg (pj/lay-point numbers :s :n {:x-type :numerical}))))
    (is (nil? (msg (pj/lay-point dates :s :n {:x-type :temporal})))))

  (testing "and report the column and what it holds where they do not"
    (doseq [[what pose] [[:numerical-on-strings (pj/lay-point strings :s :n {:x-type :numerical})]
                         [:temporal-on-strings (pj/lay-point strings :s :n {:x-type :temporal})]
                         [:temporal-on-numbers (pj/lay-point numbers :s :n {:x-type :temporal})]
                         [:numerical-on-dates (pj/lay-point dates :s :n {:x-type :numerical})]]]
      (let [m (msg pose)]
        (is (some? m) (str what " should report"))
        (is (re-find #":x-type" m) (str what " names the option"))
        (is (re-find #":s" m) (str what " names the column"))
        (is (re-find #"Write :categorical" m) (str what " names the way out")))))

  (testing "on :y-type and :color-type by the same rule"
    (is (re-find #":y-type" (msg (pj/lay-point strings :n :s {:y-type :numerical}))))
    (is (re-find #":color-type"
                 (msg (pj/lay-point {:a [1.0 2.0] :b [1.0 2.0] :c ["p" "q"]}
                                    :a :b {:color :c :color-type :numerical})))))

  (testing "a value that is not a column type is reported too"
    (let [m (msg (pj/lay-point numbers :s :n {:x-type :nonsense}))]
      (is (re-find #"is not a column type" m))
      (is (re-find #":categorical :numerical :temporal" m)))))

(def ^:private banded
  {:cat ["a" "b" "c"] :num [1.0 2.0 3.0] :val [4.0 5.0 6.0] :w [0.2 0.5 0.9]})

(deftest band-width-options-take-a-number-test
  ;; A column here failed three ways: a raw ClassCastException naming
  ;; Keyword and Number on a numeric axis, and "Plan does not conform to
  ;; schema" -- which names no option -- on a categorical one and for a
  ;; string. One width is drawn for the whole layer, so a column has
  ;; nothing to mean.
  (testing ":bar-width on a categorical axis"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":bar-width must be a number"
         (pj/plot (pj/lay-bar banded :cat :val {:bar-width :w}) {:format :svg}))))
  (testing ":bar-width on a numeric axis"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":bar-width must be a number"
         (pj/plot (pj/lay-bar banded :num :val {:bar-width :w}) {:format :svg}))))
  (testing ":bar-width given a string"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":bar-width must be a number"
         (pj/plot (pj/lay-bar banded :cat :val {:bar-width "0.4"}) {:format :svg}))))
  (testing ":box-width"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":box-width must be a number"
         (pj/plot (pj/lay-boxplot banded :cat :val {:box-width :val}) {:format :svg}))))
  (testing ":interval-thickness"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #":interval-thickness must be a number"
         (pj/plot (pj/lay-interval-h banded {:y :cat :x 0 :x-end :val
                                             :interval-thickness :w})
                  {:format :svg}))))
  (testing "and a number still draws"
    (is (some? (pj/plot (pj/lay-bar banded :cat :val {:bar-width 0.4})
                        {:format :svg})))))

(deftest a-rule-or-band-goes-on-an-x-only-pose
  ;; The x-only guard exists so `prepare-points` cannot fabricate y=0
  ;; for a mark that reads rows. These four write their value and read
  ;; no rows, so it never applied to them -- but once they became
  ;; ordinary layers they started reaching it, and a threshold line on
  ;; a histogram threw. `resolve/written-position-marks` is exempted.
  (let [counts (mapv (fn [i] {:v (rem (* i 7) 20) :g (if (even? i) "a" "b")})
                     (range 40))
        drawn (fn [pose] (:lines (pj/svg-summary pose)))]
    (testing "each x-only base takes a rule"
      (is (= 1 (drawn (-> counts (pj/lay-histogram :v) (pj/lay-rule-h {:y-intercept 3})))))
      (is (= 1 (drawn (-> counts (pj/lay-histogram :v) (pj/lay-rule-v {:x-intercept 10})))))
      (is (= 1 (drawn (-> counts (pj/lay-density :v) (pj/lay-rule-v {:x-intercept 10})))))
      (is (= 1 (drawn (-> counts (pj/lay-bar :g) (pj/lay-rule-h {:y-intercept 3})))))
      (is (= (inc (drawn (-> counts (pj/lay-rug :v))))
             (drawn (-> counts (pj/lay-rug :v) (pj/lay-rule-v {:x-intercept 10}))))
          "the rug draws a line per row, so the rule is one line more than the base"))
    (testing "a band too, which draws a rect rather than a line"
      (let [pose (-> counts (pj/lay-histogram :v) (pj/lay-band-h {:y-min 1 :y-max 3}))]
        (is (pos? (:visible-tiles (pj/svg-summary pose))))))
    (testing "the guard still fires for a mark that reads its rows"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"pj/lay-point draws from two columns.*no column on :y\. It names :x :v"
           (pj/plan (-> counts (pj/lay-point :v)))))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"pj/lay-line draws from two columns.*no column on :y\. It names :x :v"
           (pj/plan (-> counts (pj/lay-line :v))))))))

(deftest a-band-with-equal-bounds-is-reported
  ;; `lo > hi` was refused and `lo = hi` was not, so a band covering
  ;; nothing drew a rect of zero thickness -- in the SVG, invisible on
  ;; the plot, reported by nothing.
  (let [base (pj/lay-point tiny :x :y)]
    (testing "equal bounds name the rule that draws a line at one value"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"requires :y-min < :y-max.*lay-rule-h"
           (pj/lay-band-h base {:y-min 3 :y-max 3})))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"requires :x-min < :x-max.*lay-rule-v"
           (pj/lay-band-v base {:x-min 2 :x-max 2}))))
    (testing "the neighbouring guards are unchanged"
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"requires :y-min <= :y-max"
           (pj/lay-band-h base {:y-min 5 :y-max 3}))
          "a swapped pair still reports the swap, not the equality")
      (is (some? (pj/lay-band-h base {:y-min 3 :y-max 5}))
          "an ordinary band is accepted"))))

(deftest a-map-holding-a-map-is-not-data-test
  ;; A pose written by hand without :layers, :poses or a :mapping map
  ;; was read as a map of columns, and its keys were drawn as data.
  (testing "a hand-written pose map is reported, naming the map-valued keys"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"holds a map under \[:opts\].*Its keys: \[:opts :panels\]"
                          (pj/pose {:opts {:title "x"} :panels [1 2]})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"holds a map under \[:data\]"
                          (pj/plot {:data {:a [1 2] :b [3 4]} :layer [{:layer-type :point}]}))))
  (testing "a map of columns still draws, with a scalar broadcast"
    (is (= 2 (:points (pj/svg-summary (pj/pose {:a [1 2] :b 3})))))))

(deftest a-plan-or-a-draft-is-not-a-pose-test
  ;; Given where a pose goes, a plan died on a protocol error from
  ;; inside tech.v3.datatype.
  (let [p (pj/lay-point {:a [1 2] :b [3 4]} :a :b)]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"^pj/pose expects a pose or data, not a plan"
                          (pj/pose (pj/plan p))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"^pj/lay-point expects a pose or data, not a plan"
                          (pj/lay-point (pj/plan p))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"^pj/pose expects a pose or data, not a draft"
                          (pj/pose (pj/draft p))))))

(def cars
  {:drv   ["f" "f" "4" "r" "4" "f"]
   :class ["compact" "suv" "suv" "2seater" "compact" "compact"]
   :hwy   [29 20 18 26 25 31]
   :displ [1.8 3.0 4.0 5.7 2.0 1.6]})

(defn- panel-marks
  "The marks each panel of a plan draws, cell by cell for a composite."
  [plan]
  (if-let [subs (:sub-plots plan)]
    (vec (mapcat #(panel-marks (:plan %)) subs))
    (mapv #(mapv :mark (:layers %)) (:panels plan))))

(deftest a-categorical-column-on-both-axes-is-counted-test
  ;; The plan was built and the renderer then failed with a
  ;; NullPointerException: the pose wrote the inferred layer down as
  ;; `:bar`, and the bar's stat was chosen again as `:identity`, which
  ;; reads a length from a categorical column.
  (testing "one categorical column on both axes draws the count of its categories"
    (doseq [pose [(pj/pose cars :drv :drv)
                  (-> cars (pj/pose :drv :drv) pj/lay-bar)
                  (pj/lay-bar cars :drv :drv)
                  (-> cars (pj/pose :drv :drv) (pj/coord :flip))]]
      (is (= [[:rect]] (panel-marks (pj/plan pose))))
      (is (= 3 (:polygons (pj/svg-summary pose))))))
  (testing "the same picture as the column named once"
    (is (= (:polygons (pj/svg-summary (pj/pose cars :drv)))
           (:polygons (pj/svg-summary (pj/pose cars :drv :drv))))))
  (testing "the diagonal of a matrix holding a categorical column"
    (is (= [[:rect] [:boxplot] [:boxplot] [:bar]]
           (panel-marks (pj/plan (pj/cross-matrix cars [:drv :hwy])))))
    (is (pos? (:polygons (pj/svg-summary (pj/cross-matrix cars [:drv :hwy])))))
    (is (pos? (:polygons (pj/svg-summary (pj/arrange cars [[:drv :drv] [:drv :hwy]])))))
    (is (pos? (:polygons (pj/svg-summary (-> cars
                                             (pj/pose {:y :drv})
                                             (pj/facet {:x [:drv :hwy]})))))))
  (testing "a bar of a numerical column named on both axes counts too, and says what a count needs"
    ;; It was read as two columns and drew the column against itself.
    (doseq [pose [(pj/lay-bar cars :displ :displ)
                  (-> cars (pj/pose :displ :displ) pj/lay-bar)
                  (-> cars (pj/cross-matrix [:displ :hwy]) pj/lay-bar)
                  (-> cars
                      (pj/facet {:col {:x [:displ :hwy]} :row {:y [:displ :hwy]}})
                      pj/lay-bar)]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"lay-bar \(counting\) requires a categorical column for :x, but :displ is numerical"
                            (pj/plan pose))))
    (testing "as the column named once does"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"lay-bar \(counting\) requires a categorical column for :x, but :displ is numerical"
                            (pj/plan (pj/lay-bar cars :displ))))))
  (testing "layer types that draw only from two columns still draw the column against itself"
    (is (= (:points (pj/svg-summary (pj/lay-point cars :displ :hwy)))
           (:points (pj/svg-summary (pj/lay-point cars :displ :displ)))))
    (is (pos? (:points (pj/svg-summary (pj/lay-point cars :displ :displ))))))
  (testing "a rug on the diagonal of a matrix leaves its row's axis as the column's"
    (let [y-domains (fn [pose] (mapv #(-> % :plan :panels first :y-domain) (:sub-plots (pj/plan pose))))
          [rug-cell point-cell] (y-domains (-> cars (pj/cross-matrix [:displ :hwy]) pj/lay-point pj/lay-rug))]
      (is (= rug-cell point-cell))
      (is (pos? (first rug-cell)))))
  (testing "a bar of two different categorical columns has no length to read"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"lay-bar reads the length of a bar from a numerical column, but :drv and :class are both categorical"
                          (pj/plan (pj/lay-bar cars :drv :class)))))
  (testing "a boxplot of two categorical columns reports the column that is not numeric"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Stat :boxplot requires a numeric column for :y, but :class is categorical"
                          (pj/plan (pj/lay-boxplot cars :drv :class))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Stat :boxplot requires a numeric column for :y, but :drv is categorical"
                          (pj/plan (pj/lay-boxplot cars :drv :drv))))
    (is (= 3 (:polygons (pj/svg-summary (pj/lay-boxplot cars :drv :hwy)))))
    (is (= 3 (:polygons (pj/svg-summary (pj/lay-boxplot cars :hwy :drv))))))
  (testing "a bar of a category and a number, and points of two categories, are as before"
    (is (= 6 (:polygons (pj/svg-summary (pj/lay-bar cars :drv :hwy)))))
    (is (= 6 (:points (pj/svg-summary (pj/lay-point cars :drv :class)))))))

(deftest lay-reports-a-map-that-is-not-a-layer-type-test
  ;; A mapping written where the layer type goes was stored as the
  ;; layer type and drew a layer of the type the data chose, untitled,
  ;; with no message.
  (let [pose (pj/pose cars :displ :hwy)]
    (testing "a map that reads as a mapping is told where the layer type goes"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay got the map \{:y :hwy\} as its layer type.*\(pj/lay pose :point \{:y :hwy\}\)"
                            (pj/lay pose {:y :hwy}))))
    (testing "a map with no :mark is not a layer type"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"a layer type written as a map names its :mark"
                            (pj/lay pose {})))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"a layer type written as a map names its :mark"
                            (pj/lay pose {:stat :bin}))))
    (testing "a mark that is not registered is reported at the call"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay got :mark :nosuch, which is not a registered mark"
                            (pj/lay pose {:mark :nosuch}))))
    (testing "neither a keyword nor a map"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay got \"point\" as its layer type"
                            (pj/lay pose "point")))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay got nil as its layer type.*not registered"
                            (pj/lay pose nil))))
    (testing "a layer type written as a map still draws, with options after it"
      (is (= 6 (:points (pj/svg-summary (pj/lay pose {:mark :point :stat :identity})))))
      (is (= 6 (:points (pj/svg-summary (pj/lay pose (pj/layer-type-lookup :point))))))
      (is (= ["f" "4" "r"]
             (mapv :label (:entries (:legend (pj/plan (pj/lay pose {:mark :point} {:color :drv}))))))))))

(deftest save-asks-the-plan-whether-a-pose-is-empty-test
  ;; The check tested the pose's shape -- data on the pose, with a
  ;; layer or a mapping -- and so refused poses that draw.
  (let [path (str (System/getProperty "java.io.tmpdir") "/plotje-save-" (System/nanoTime) ".svg")
        saved? (fn [pose]
                 (pj/save pose path)
                 (let [f (java.io.File. path)
                       ok (pos? (.length f))]
                   (.delete f)
                   ok))]
    (testing "a pose that draws is saved, whatever names its columns"
      (is (saved? (pj/facet cars {:col {:x [:displ :hwy]} :row {:y [:displ :hwy]}})))
      (is (saved? (pj/facet cars {:x [:displ :hwy]})))
      (is (saved? (-> (pj/pose) (pj/lay-point :a :b {:data {:a [1 2] :b [3 4]}}))))
      (is (saved? (pj/pose cars :displ :hwy))))
    (testing "a pose that draws nothing is reported"
      (doseq [pose [(pj/pose) (pj/pose cars) (pj/arrange [(pj/pose) (pj/pose)])]]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"empty pose -- nothing to render"
                              (pj/save pose path)))))))

(def wide
  {:drv   ["f" "f" "4" "r" "4" "f"]
   :class ["compact" "suv" "suv" "2seater" "compact" "compact"]
   :hwy   [29 20 18 26 25 31]
   :cty   [21 15 14 17 19 24]
   :displ [1.8 3.0 4.0 5.7 2.0 1.6]})

(deftest a-layer-left-without-a-column-says-which-test
  ;; These failed with a NullPointerException, "Plan does not conform
  ;; to schema", or a message naming neither the layer nor the columns.
  (testing "a layer naming no column, on data too wide to read by default"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"pj/lay-point draws from two columns, one on :x and one on :y, and this layer has no column on :x or :y\. It names no column\..*Available columns: \[:class :cty :displ :drv :hwy\]"
                          (pj/plan (-> wide pj/pose pj/lay-point))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"pj/lay-histogram draws from one column, on :x, and this layer has no column on :x"
                          (pj/plan (-> wide pj/pose pj/lay-histogram)))))
  (testing "a layer naming one column of the two its type draws from"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"pj/lay-point draws from two columns.*no column on :x\. It names :y :hwy\. Name the :x column on the layer -- \(pj/lay-point pose :x-column :hwy\)"
                          (pj/plan (-> wide pj/pose (pj/lay-point {:y :hwy})))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"pj/lay-point draws from two columns.*no column on :x\. It names :y :hwy"
                          (pj/plan (-> wide
                                       (pj/arrange [[:displ :hwy] [:displ :cty]])
                                       (pj/lay-point {:y :hwy}))))))
  (testing "a bar naming :y alone beside points is not given the panel's :x"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"pj/lay-bar draws from one column on :x, or two on :x and :y, and this layer has no column on :x\. It names :y :hwy\. Name the :x column on the layer"
                          (pj/plan (-> wide (pj/lay-point :drv :cty) (pj/lay-bar {:y :hwy}))))))
  (testing "a value on :x with nothing on :y"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"no column on :y\. It names :x 3"
                          (pj/plan (-> wide pj/pose (pj/lay-point {:x {:value 3}}))))))
  (testing "layers that name no column and need none are drawn"
    (let [base (pj/lay-point wide :displ :hwy)]
      (is (= 1 (:lines (pj/svg-summary (pj/lay-rule-h base {:y-intercept 25})))))
      (is (some #{"n"} (:texts (pj/svg-summary (pj/lay-text base {:x 3 :y 25 :text "n"}))))))))

(deftest a-column-on-y-alone-in-a-layer-is-drawn-on-its-side-test
  ;; `(pj/pose data {:y :a})` with `pj/lay-histogram` drew on its side,
  ;; and `(pj/lay-histogram pose {:y :a})` failed with a
  ;; NullPointerException. One request, two spellings.
  (let [summary #(select-keys (pj/svg-summary %) [:polygons :texts])
        on-pose (-> wide (pj/pose {:y :hwy}) pj/lay-histogram)
        on-layer (-> wide pj/pose (pj/lay-histogram {:y :hwy}))]
    (testing "the layer spelling draws what the pose spelling draws"
      (is (= (summary on-pose) (summary on-layer)))
      (is (= "hwy" (:y-label (pj/plan on-layer)))))
    (testing "a second layer naming no column reads the same column"
      (is (= [[:bar :area]]
             (mapv #(mapv :mark (:layers %))
                   (:panels (pj/plan (-> wide pj/pose (pj/lay-histogram {:y :hwy}) pj/lay-density)))))))
    (testing "a bar counts the categories of a column named on :y"
      (is (= 3 (:polygons (pj/svg-summary (-> wide pj/pose (pj/lay-bar {:y :drv})))))))
    (testing "a coordinate system other than :flip has no drawing for it"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"names :hwy on :y alone.*:coord :polar has no drawing for it"
                            (pj/plan (pj/coord on-layer :polar)))))
    (testing "the same where the pose above specifies it: a facet over columns on its side"
      (let [faceted (-> wide pj/pose pj/lay-histogram (pj/facet {:y [:hwy :cty]}))
            coords (fn [pose] (mapv #(:coord (first (:panels (:plan %)))) (:sub-plots (pj/plan pose))))]
        (is (= [:flip :flip] (coords faceted) (coords (pj/coord faceted :flip))))
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"on :y alone.*:coord :polar has no drawing for it"
                              (pj/plan (pj/coord faceted :polar))))
        (is (= [:polar :polar]
               (coords (-> wide pj/pose pj/lay-histogram (pj/facet {:x [:hwy :cty]}) (pj/coord :polar))))
            "a facet not drawn on its side takes the coordinate system")))
    (testing "beside a layer that draws from two columns, the message names that layer"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay-histogram draws from one column.*It names :y :cty\..*and pj/lay-point does not"
                            (pj/plan (-> wide (pj/lay-point :displ :hwy) (pj/lay-histogram {:y :cty}))))))))

(deftest a-layer-naming-a-column-the-facet-lacks-test
  ;; The layer was drawn in every panel and the facet's columns in
  ;; none, with no message.
  (let [marks (fn [pose] (mapv (fn [sp] (mapv :mark (:layers (first (:panels (:plan sp))))))
                               (:sub-plots (pj/plan pose))))]
    (testing "an error where a panel is left with no layer drawing its own column"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay-point names :cty on :y, and no panel draws it: the facet over columns has :hwy and :displ on :y\. List :cty among the facet's :y columns"
                            (pj/plan (-> wide (pj/lay-point :drv :cty) (pj/facet {:y [:hwy :displ]})))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"pj/lay-point names :cty on :y, and no panel draws it"
                            (pj/plan (-> wide
                                         (pj/lay-point :drv :cty)
                                         (pj/lay-point :drv :hwy)
                                         (pj/facet {:y [:hwy :displ]}))))))
    (testing "a layer with data of its own is told its columns are not the facet's"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"This layer draws data of its own"
                            (pj/plan (-> wide
                                         (pj/lay-point :a :b {:data {:a [1 2] :b [3 4]}})
                                         (pj/facet {:y [:hwy :displ]}))))))
    (testing "beside layers that fit every panel, it is drawn in every panel"
      (is (= [[:point :point] [:point :point]]
             (marks (-> wide
                        (pj/pose {:x :drv})
                        (pj/facet {:y [:hwy :displ]})
                        pj/lay-point
                        (pj/lay-point {:y :cty}))))))
    (testing "a layer placed by written values is drawn in every panel"
      (is (= [[:point :text] [:point :text]]
             (marks (-> wide
                        (pj/pose {:x :displ})
                        (pj/facet {:y [:hwy :cty]})
                        pj/lay-point
                        (pj/lay-text {:x 3 :y 20 :text "n"}))))))))
