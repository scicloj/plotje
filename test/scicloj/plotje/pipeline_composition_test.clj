(ns scicloj.plotje.pipeline-composition-test
  "Tests that nail down the compositional principle: pj/draft, pj/plan,
   and pj/plot are literal compositions of the atomic public arrow
   functions. Equivalence tests prove the composition holds; with-redefs
   tests prove the user-facing functions actually call through to the
   atomic steps (not via an internal bypass)."
  (:require [clojure.test :refer [deftest is testing]]
            [membrane.ui]
            [tablecloth.api :as tc]
            [scicloj.plotje.api :as pj]
            [scicloj.plotje.render.membrane :as membrane]))

(def tiny (tc/dataset {:x [1 2 3] :y [4 5 6]}))

;; ============================================================
;; ->pose: polymorphic and idempotent
;; ============================================================

(deftest ->pose-accepts-data-and-pose
  (testing "raw data becomes a leaf pose with :data set"
    (let [p (pj/->pose tiny)]
      (is (pj/pose? p))
      (is (some? (:data p)))
      (is (empty? (:layers p)))))
  (testing "an existing pose passes through"
    (let [pose (pj/lay-point tiny :x :y)
          lifted (pj/->pose pose)]
      (is (pj/pose? lifted))
      (is (= (:layers pose) (:layers lifted))))))

(deftest ->pose-idempotent
  (testing "lifting twice equals lifting once"
    (let [once (pj/->pose tiny)
          twice (pj/->pose once)]
      (is (= (:data once) (:data twice)))
      (is (= (:layers once) (:layers twice))))))

;; ============================================================
;; pose->draft: shape dispatch
;; ============================================================

(deftest pose->draft-leaf-returns-LeafDraft
  (let [pose (pj/lay-point tiny :x :y)
        draft (pj/pose->draft pose)]
    (is (pj/leaf-draft? draft))
    (is (vector? (:layers draft)))
    (is (map? (:opts draft)))))

(deftest pose->draft-composite-returns-CompositeDraft
  (let [composite (pj/arrange [(pj/lay-point tiny :x :y)
                               (pj/lay-line tiny :x :y)])
        draft (pj/pose->draft composite)]
    (is (pj/composite-draft? draft))))

(deftest pose->draft-rejects-non-pose
  (is (thrown-with-msg? Exception #"expects a pose"
                        (pj/pose->draft "not a pose"))))

;; ============================================================
;; LeafDraft carries pose-level opts
;; ============================================================

(deftest leaf-draft-carries-opts
  (let [pose (-> (pj/lay-point tiny :x :y)
                 (pj/options {:title "T" :x-label "X" :width 700}))
        draft (pj/pose->draft pose)]
    (is (pj/leaf-draft? draft))
    (is (= "T" (:title (:opts draft))))
    (is (= "X" (:x-label (:opts draft))))
    (is (= 700 (:width (:opts draft))))))

;; ============================================================
;; pj/plan = ->pose ; pose->draft ; draft->plan
;; ============================================================

(deftest plan-equals-arrow-composition-no-opts
  (testing "no opts: pj/plan agrees with the literal arrow chain"
    (let [pose (pj/lay-point tiny :x :y)
          via-public (pj/plan pose)
          via-arrows (-> pose pj/->pose pj/pose->draft pj/draft->plan)]
      (is (= (:total-width via-public) (:total-width via-arrows)))
      (is (= (:total-height via-public) (:total-height via-arrows)))
      (is (= (count (:panels via-public)) (count (:panels via-arrows)))))))

(deftest plan-equals-arrow-composition-with-pose-opts
  (testing "opts on the pose: pj/plan agrees with the literal arrow chain"
    (let [pose (-> (pj/lay-point tiny :x :y)
                   (pj/options {:title "T" :x-label "Xax" :width 700}))
          via-public (pj/plan pose)
          via-arrows (-> pose pj/->pose pj/pose->draft pj/draft->plan)]
      (is (= "T" (:title via-public)) "title flowed through public path")
      (is (= "T" (:title via-arrows)) "title flowed through arrows path")
      (is (= "Xax" (:x-label via-public)))
      (is (= "Xax" (:x-label via-arrows)))
      (is (= 700 (:width via-public)))
      (is (= 700 (:width via-arrows))))))

(deftest plan-2arity-equals-arrow-composition
  (testing "pj/plan with opts arg: equals threading through pj/options"
    (let [pose (pj/lay-point tiny :x :y)
          opts {:title "T2" :width 800}
          via-public (pj/plan pose opts)
          via-arrows (-> pose pj/->pose (pj/options opts) pj/pose->draft pj/draft->plan)]
      (is (= (:title via-public) (:title via-arrows)))
      (is (= (:width via-public) (:width via-arrows))))))

;; ============================================================
;; pj/draft = ->pose ; pose->draft (with optional pj/options)
;; ============================================================

(deftest draft-equals-arrow-composition
  (testing "1-arity"
    (let [pose (pj/lay-point tiny :x :y)
          via-public (pj/draft pose)
          via-arrows (-> pose pj/->pose pj/pose->draft)]
      (is (pj/leaf-draft? via-public))
      (is (pj/leaf-draft? via-arrows))
      (is (= (:layers via-public) (:layers via-arrows)))
      (is (= (:opts via-public) (:opts via-arrows)))))
  (testing "2-arity threads through pj/options"
    (let [pose (pj/lay-point tiny :x :y)
          opts {:title "Hi"}
          via-public (pj/draft pose opts)
          via-arrows (-> pose pj/->pose (pj/options opts) pj/pose->draft)]
      (is (= "Hi" (:title (:opts via-public))))
      (is (= "Hi" (:title (:opts via-arrows)))))))

;; ============================================================
;; with-redefs: pj/plan calls the public arrow functions
;; ============================================================
;;
;; This proves the compositional structure holds at runtime, not just
;; in the source. If pj/plan ever bypasses the public arrow functions
;; via a private shortcut, these tests fail.

(deftest plan-calls-public-pose->draft
  (let [calls (atom 0)
        original pj/pose->draft]
    (with-redefs [pj/pose->draft (fn [pose] (swap! calls inc) (original pose))]
      (pj/plan (pj/lay-point tiny :x :y))
      (is (= 1 @calls)
          "pj/plan calls pj/pose->draft once for a leaf pose"))))

(deftest plan-calls-public-draft->plan
  (let [calls (atom 0)
        original pj/draft->plan]
    (with-redefs [pj/draft->plan (fn [d] (swap! calls inc) (original d))]
      (pj/plan (pj/lay-point tiny :x :y))
      (is (= 1 @calls)
          "pj/plan calls pj/draft->plan once for a leaf pose"))))

(deftest draft-calls-public-pose->draft
  (let [calls (atom 0)
        original pj/pose->draft]
    (with-redefs [pj/pose->draft (fn [pose] (swap! calls inc) (original pose))]
      (pj/draft (pj/lay-point tiny :x :y))
      (is (= 1 @calls)
          "pj/draft calls pj/pose->draft once"))))

(deftest membrane-equals-arrow-composition
  (testing "pj/membrane = (-> pose ->pose pose->draft draft->plan (plan->membrane opts))"
    (let [pose (-> (pj/lay-point tiny :x :y)
                   (pj/options {:title "T" :width 700}))
          via-public (pj/membrane pose)
          via-arrows (-> pose
                         pj/->pose
                         pj/pose->draft
                         pj/draft->plan
                         (pj/plan->membrane (:opts pose {})))]
      (is (pj/membrane? via-public))
      (is (pj/membrane? via-arrows))
      (is (= (membrane.ui/width via-public)
             (membrane.ui/width via-arrows))
          "plan-derived dimensions agree on both paths")
      (is (= "T" (:plotje/title via-public))
          "title rides as :plotje/title from the pose's :opts"))))

(deftest membrane-calls-the-pipeline
  (testing "pj/membrane literally calls the public atomic steps up through plan->membrane"
    (let [pose-draft-calls (atom 0)
          draft-plan-calls (atom 0)
          plan-membrane-calls (atom 0)
          orig-pose->draft pj/pose->draft
          orig-draft->plan pj/draft->plan
          orig-plan->membrane pj/plan->membrane]
      (with-redefs [pj/pose->draft   (fn [p] (swap! pose-draft-calls inc) (orig-pose->draft p))
                    pj/draft->plan   (fn [d] (swap! draft-plan-calls inc) (orig-draft->plan d))
                    pj/plan->membrane (fn
                                        ([p] (swap! plan-membrane-calls inc) (orig-plan->membrane p))
                                        ([p o] (swap! plan-membrane-calls inc) (orig-plan->membrane p o)))]
        (pj/membrane (pj/lay-point tiny :x :y))
        (is (= 1 @pose-draft-calls) "pj/membrane calls pj/pose->draft once")
        (is (= 1 @draft-plan-calls) "pj/membrane calls pj/draft->plan once")
        (is (= 1 @plan-membrane-calls) "pj/membrane calls pj/plan->membrane once")))))

(deftest plot-calls-all-five-atomic-steps
  (testing "pj/plot literally calls each of the five public atomic steps"
    (let [pose-draft-calls (atom 0)
          draft-plan-calls (atom 0)
          plan-membrane-calls (atom 0)
          membrane-plot-calls (atom 0)
          orig-pose->draft pj/pose->draft
          orig-draft->plan pj/draft->plan
          orig-plan->membrane pj/plan->membrane
          orig-membrane->plot pj/membrane->plot]
      (with-redefs [pj/pose->draft   (fn [p] (swap! pose-draft-calls inc) (orig-pose->draft p))
                    pj/draft->plan   (fn [d] (swap! draft-plan-calls inc) (orig-draft->plan d))
                    pj/plan->membrane (fn
                                        ([p] (swap! plan-membrane-calls inc) (orig-plan->membrane p))
                                        ([p o] (swap! plan-membrane-calls inc) (orig-plan->membrane p o)))
                    pj/membrane->plot (fn [m fmt o] (swap! membrane-plot-calls inc) (orig-membrane->plot m fmt o))]
        (pj/plot (pj/lay-point tiny :x :y))
        (is (= 1 @pose-draft-calls) "pj/plot calls pj/pose->draft once")
        (is (= 1 @draft-plan-calls) "pj/plot calls pj/draft->plan once")
        (is (= 1 @plan-membrane-calls) "pj/plot calls pj/plan->membrane once")
        (is (= 1 @membrane-plot-calls) "pj/plot calls pj/membrane->plot once")))))

;; ============================================================
;; Cross-stage misuse guards
;; ============================================================
;;
;; Drafts and membrane vectors are user-observable but are not
;; poses. The public shortcuts must reject them with clean
;; "expects a pose, not a ..." messages -- not silently corrupt
;; (the LeafDraft case before this fix) or produce degenerate
;; output (the membrane case).

(deftest draft-rejects-draft-input
  (let [d (pj/draft (pj/lay-point tiny :x :y))]
    (is (thrown-with-msg? Exception #"expects a pose, not a draft"
                          (pj/draft d)))))

(deftest plan-rejects-draft-input
  (let [d (pj/draft (pj/lay-point tiny :x :y))]
    (is (thrown-with-msg? Exception #"expects a pose, not a draft"
                          (pj/plan d)))))

(deftest membrane-rejects-membrane-input
  (let [m (pj/membrane (pj/lay-point tiny :x :y))]
    (is (thrown-with-msg? Exception #"Membrane drawable tree"
                          (pj/membrane m)))))

(deftest plot-rejects-membrane-input
  (let [m (pj/membrane (pj/lay-point tiny :x :y))]
    (is (thrown-with-msg? Exception #"Membrane drawable tree"
                          (pj/plot m)))))

(deftest plan-rejects-membrane-input
  (let [m (pj/membrane (pj/lay-point tiny :x :y))]
    (is (thrown-with-msg? Exception #"Membrane drawable tree"
                          (pj/plan m)))))

;; ============================================================
;; Composite equivalence: shortcut equals literal chain
;; ============================================================
;;
;; Composite poses go through `composite-pose->draft`, which does
;; cross-leaf work (shared-scale domain injection, suppress-* flags,
;; chrome geometry) before per-leaf draft->plan runs. The shortcut
;; must call the public arrow steps for composites just as it does
;; for leaves -- otherwise the boundaries the architecture chapter
;; teaches would not hold across pose shapes.

(def composite-pose
  (pj/arrange [(pj/lay-point tiny :x :y)
               (pj/lay-line  tiny :x :y)]))

(deftest composite-plan-equals-arrow-composition
  (testing "pj/plan on a composite pose agrees with the literal arrow chain"
    (let [via-public (pj/plan composite-pose)
          via-arrows (-> composite-pose pj/->pose pj/pose->draft pj/draft->plan)]
      (is (pj/composite-plan? via-public))
      (is (pj/composite-plan? via-arrows))
      (is (= (count (:sub-plots via-public))
             (count (:sub-plots via-arrows))))
      (is (= (:total-width via-public)  (:total-width via-arrows)))
      (is (= (:total-height via-public) (:total-height via-arrows))))))

(deftest composite-shared-scale-plan-equivalence
  (testing "shared-scale composite agrees through both routes"
    (let [shared-pose (pj/arrange [(pj/lay-point tiny :x :y)
                                   (pj/lay-point tiny :x :y)]
                                  {:share-scales #{:x :y}})
          via-public (pj/plan shared-pose)
          via-arrows (-> shared-pose pj/->pose pj/pose->draft pj/draft->plan)]
      (is (= (count (:sub-plots via-public))
             (count (:sub-plots via-arrows))))
      (let [domains-public (mapv (fn [sp] (-> sp :sub-plot :panels first
                                              (select-keys [:x-domain :y-domain])))
                                 (:sub-plots via-public))
            domains-arrows (mapv (fn [sp] (-> sp :sub-plot :panels first
                                              (select-keys [:x-domain :y-domain])))
                                 (:sub-plots via-arrows))]
        (is (= domains-public domains-arrows)
            "shared-scale domains agree on both paths")))))

(deftest composite-arrange-opts-flow-through
  (testing "arrange opts (title, width, height) reach the composite plan via both paths"
    (let [titled-pose (pj/arrange [(pj/lay-point tiny :x :y)
                                   (pj/lay-line  tiny :x :y)]
                                  {:title "Two panels" :width 800 :height 500})
          via-public (pj/plan titled-pose)
          via-arrows (-> titled-pose pj/->pose pj/pose->draft pj/draft->plan)]
      (is (= "Two panels" (-> via-public :chrome :title)))
      (is (= "Two panels" (-> via-arrows :chrome :title)))
      (is (= 800 (:total-width via-public)))
      (is (= 800 (:total-width via-arrows)))
      (is (= 500 (:total-height via-public)))
      (is (= 500 (:total-height via-arrows))))))

(deftest composite-plan-calls-public-arrows
  (testing "pj/plan on a composite calls each public arrow exactly once"
    (let [pose-calls (atom 0)
          draft-calls (atom 0)
          plan-calls (atom 0)
          orig-pose pj/->pose
          orig-pose->draft pj/pose->draft
          orig-draft->plan pj/draft->plan]
      (with-redefs [pj/->pose (fn
                                ([x] (swap! pose-calls inc) (orig-pose x))
                                ([x caller] (swap! pose-calls inc) (orig-pose x caller)))
                    pj/pose->draft (fn [p] (swap! draft-calls inc) (orig-pose->draft p))
                    pj/draft->plan (fn [d] (swap! plan-calls inc) (orig-draft->plan d))]
        (pj/plan composite-pose)
        (is (= 1 @pose-calls)  "pj/plan calls pj/->pose once for a composite")
        (is (= 1 @draft-calls) "pj/plan calls pj/pose->draft once for a composite")
        (is (= 1 @plan-calls)  "pj/plan calls pj/draft->plan once for a composite")))))

;; ============================================================
;; Faceted leaf: shortcut equals literal chain
;; ============================================================
;;
;; Facet expands a leaf into multiple panels at draft->plan time.
;; The shortcut must call the public arrow steps the same way for
;; faceted as for non-faceted leaves.

(deftest faceted-plan-equals-arrow-composition
  (testing "pj/plan on a faceted leaf agrees with the literal arrow chain"
    (let [data (tc/dataset {:x [1 2 3 4 5 6]
                            :y [1 2 3 4 5 6]
                            :g [:a :a :b :b :c :c]})
          pose (-> data
                   (pj/lay-point :x :y)
                   (pj/facet :g))
          via-public (pj/plan pose)
          via-arrows (-> pose pj/->pose pj/pose->draft pj/draft->plan)]
      (is (= (count (:panels via-public))
             (count (:panels via-arrows))))
      (is (= (:total-width via-public)  (:total-width via-arrows)))
      (is (= (:total-height via-public) (:total-height via-arrows))))))

;; ============================================================
;; ->pose lift is genuinely called by every shortcut
;; ============================================================
;;
;; Earlier with-redefs tests cover pose->draft/draft->plan/etc. but
;; not ->pose itself. If a shortcut bypassed the lift step, the
;; idempotent + dataset-promotion guarantees would be invisible to
;; users who rebind pj/->pose for testing.

(deftest every-shortcut-calls-public-->pose
  (let [shortcuts {pj/draft "pj/draft"
                   pj/plan "pj/plan"
                   pj/membrane "pj/membrane"
                   pj/plot "pj/plot"}]
    (doseq [[shortcut nm] shortcuts]
      (testing (str nm " calls pj/->pose")
        (let [calls (atom 0)
              orig pj/->pose]
          (with-redefs [pj/->pose (fn
                                    ([x] (swap! calls inc) (orig x))
                                    ([x caller] (swap! calls inc) (orig x caller)))]
            (shortcut (pj/lay-point tiny :x :y))
            (is (pos? @calls)
                (str nm " calls pj/->pose at least once"))))))))

;; ============================================================
;; pj/plot SVG bytes equal the let-form chain
;; ============================================================
;;
;; The end-to-end check: structural summary of the rendered SVG via
;; the shortcut equals the summary via the explicit let-form chain.
;; If pj/plot's body ever drifts from the chapter pseudocode, the
;; output bytes drift too.

(deftest plot-leaf-svg-equals-let-chain
  (testing "pj/plot on a leaf agrees with the let-form chain at the SVG-summary level"
    (let [pose (-> (pj/lay-point tiny :x :y)
                   (pj/options {:title "T" :width 700}))
          via-public (pj/plot pose)
          via-arrows (let [p (pj/->pose pose)
                           opts (:opts p {})
                           fmt (or (:format opts) :svg)]
                       (-> p
                           pj/pose->draft
                           pj/draft->plan
                           (pj/plan->membrane opts)
                           (pj/membrane->plot fmt opts)))
          s-public (pj/svg-summary via-public)
          s-arrows (pj/svg-summary via-arrows)]
      (is (= (:width s-public)  (:width s-arrows)))
      (is (= (:height s-public) (:height s-arrows)))
      (is (= (:panels s-public) (:panels s-arrows)))
      (is (= (:points s-public) (:points s-arrows))))))

(deftest plot-composite-svg-equals-let-chain
  (testing "pj/plot on a composite agrees with the let-form chain at the SVG-summary level"
    (let [via-public (pj/plot composite-pose)
          via-arrows (let [p (pj/->pose composite-pose)
                           opts (:opts p {})
                           fmt (or (:format opts) :svg)]
                       (-> p
                           pj/pose->draft
                           pj/draft->plan
                           (pj/plan->membrane opts)
                           (pj/membrane->plot fmt opts)))
          s-public (pj/svg-summary via-public)
          s-arrows (pj/svg-summary via-arrows)]
      (is (= (:width s-public)  (:width s-arrows)))
      (is (= (:height s-public) (:height s-arrows)))
      (is (= (:panels s-public) (:panels s-arrows))))))

;; ============================================================
;; Multimethod dispatch invariants
;; ============================================================
;;
;; `plan->membrane` must dispatch on a boolean derived from the
;; plan, not on the plan's class. Class dispatch leaks defrecord
;; classes across :reload of impl/resolve.clj (see the defmulti
;; docstring at render/membrane.clj for the full story). This test
;; catches a regression at CI time even if a contributor edits the
;; defmulti without reading the warning.

(deftest plan->membrane-uses-boolean-dispatch
  (testing "plan->membrane methods are keyed by booleans only"
    (let [ks (set (keys (methods membrane/plan->membrane)))]
      (is (every? boolean? ks)
          (str "plan->membrane multimethod must dispatch on booleans. "
               "Class-based dispatch leaks defrecord classes across "
               ":reload of impl/resolve.clj. See render/membrane.clj "
               "defmulti docstring. Got non-boolean keys: "
               (pr-str (remove boolean? ks))))
      (is (= #{false true} ks)
          "expected exactly two methods: leaf (false) and composite (true)"))))

;; ============================================================
;; Raw-data inference at user-facing terminal ops
;; ============================================================
;;
;; The terminal ops (plot/save/draft/plan/membrane) lift raw data
;; via `->pose` and then run the shared semantic-inference seam, so a
;; bare dataset renders a sensible default instead of a blank plot.
;; `->pose` itself stays minimal -- it produces a mapping-less leaf.

(def raw-1d (tc/dataset {:v [1 2 2 3 3 3 4 4 5]}))

(deftest raw-data-renders-through-terminal-ops
  (testing "->pose stays minimal: no mapping inferred"
    (is (empty? (:mapping (pj/->pose raw-1d)))))
  (testing "plot on raw data renders the inferred default (one panel)"
    (is (= 1 (:panels (pj/svg-summary (pj/plot raw-1d)))))
    (is (= (pj/svg-summary (pj/plot raw-1d))
           (pj/svg-summary (pj/plot (pj/pose raw-1d))))
        "raw data equals the pj/pose-wrapped path"))
  (testing "draft/plan/membrane on raw data produce a mapped result"
    (is (pos? (count (pj/draft raw-1d))))
    (is (pos? (count (:panels (pj/plan raw-1d)))))
    (is (some? (pj/membrane raw-1d))))
  (testing "inference is idempotent: a mapped pose is untouched"
    (let [mapped (pj/lay-point tiny :x :y)]
      (is (= (pj/svg-summary (pj/plot mapped))
             (pj/svg-summary (pj/plot (pj/pose mapped))))))))
