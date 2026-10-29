(ns scicloj.plotje.invariants-gen
  "A generator of poses and the checks of eight invariants, used by
   `scicloj.plotje.invariants-test`.

   A recipe is data: a dataset key and a vector of steps, each step a
   `pj` function symbol and its arguments after the threaded value. The
   same recipe builds the pose (`build`) and prints its form (`form-of`),
   so a violation is reported as a form that can be pasted into a REPL
   (with `mpg`, `iris`, `sales`, `na-data`, `empty-col` and `grouped-mpg`
   bound as below).

   The grammar and the order in which it draws random numbers are those
   of the research harness `dev-notes/invariants-2026-10-14/harness.clj`,
   so a seed generates the same poses in both. Changing the grammar
   changes every pose after the change."
  (:require [scicloj.plotje.api :as pj]
            [scicloj.metamorph.ml.rdatasets :as rd]
            [tablecloth.api :as tc]
            [clojure.string :as str]))

;; ---------------------------------------------------------------- data

(def sales
  (tc/dataset {:quarter ["Q1" "Q2" "Q3" "Q4" "Q1" "Q2" "Q3" "Q4"]
               :region ["North" "North" "North" "North" "South" "South" "South" "South"]
               :week [1 2 3 4 5 6 7 8]
               :revenue [10.0 12.5 9.0 14.0 7.0 8.5 11.0 9.5]
               :cost [6.0 7.0 6.5 8.0 5.0 5.5 6.0 7.5]
               :units [100 120 90 140 70 85 110 95]}))

(def na-data
  (tc/dataset {:temp (vec (range 1 21))
               :reading [1.0 2.0 nil 4.0 5.5 nil 7.0 8.0 9.5 10.0
                         11.0 nil 13.0 14.5 15.0 16.0 nil 18.0 19.0 20.5]
               :site ["a" "b" nil "a" "b" "a" nil "b" "a" "b"
                      "a" "b" "a" nil "b" "a" "b" "a" "b" "a"]}))

(def empty-col
  (tc/dataset {:temp [1 2 3 4 5 6 7 8 9 10]
               :reading [2.0 3.0 1.0 5.0 4.0 6.0 8.0 7.0 9.0 10.0]
               :site ["a" "b" "a" "b" "a" "b" "a" "b" "a" "b"]
               :blank (vec (repeat 10 nil))}))

(def mpg (rd/ggplot2-mpg))
(def iris (rd/datasets-iris))
(def grouped-mpg (tc/group-by mpg :drv))

(def datasets
  {:mpg {:sym 'mpg :data mpg :num [:displ :hwy :cty] :cat [:drv :class] :series [:hwy :cty] :w 35}
   :iris {:sym 'iris :data iris :num [:sepal-length :sepal-width :petal-length] :cat [:species]
          :series [:sepal-length :petal-length] :w 15}
   :sales {:sym 'sales :data sales :num [:week :revenue :cost :units] :cat [:quarter :region]
           :series [:revenue :cost] :w 20}
   :na {:sym 'na-data :data na-data :num [:temp :reading] :cat [:site] :series [:temp :reading] :w 15}
   :empty {:sym 'empty-col :data empty-col :num [:temp :reading :blank] :cat [:site]
           :series [:temp :reading] :w 10}
   :grouped {:sym 'grouped-mpg :data grouped-mpg :num [:displ :hwy :cty] :cat [:drv :class]
             :series [:hwy :cty] :w 5}})

;; ---------------------------------------------------------------- random

(def ^:dynamic *rng* nil)
(defn- rnd [] (.nextDouble ^java.util.Random *rng*))
(defn- chance [p] (< (rnd) p))
(defn- pick [coll] (let [v (vec coll)] (v (.nextInt ^java.util.Random *rng* (count v)))))
(defn- pick-w
  "Pick from a map of value to weight."
  [m]
  (let [tot (reduce + (vals m))
        x (* tot (rnd))]
    (loop [[[k w] & more] (seq m) acc 0.0]
      (if (or (nil? more) (< x (+ acc w))) k (recur more (+ acc w))))))
(defn- shuffle-r [coll]
  (let [al (java.util.ArrayList. ^java.util.Collection (vec coll))]
    (java.util.Collections/shuffle al *rng*)
    (vec al)))

;; ---------------------------------------------------------------- grammar

(def layer-types
  "Layer type to [function symbol, the column kinds it reads]."
  {:point ['pj/lay-point [:num :num]]
   :line ['pj/lay-line [:num :num]]
   :smooth ['pj/lay-smooth [:num :num]]
   :area ['pj/lay-area [:num :num]]
   :step ['pj/lay-step [:num :num]]
   :histogram ['pj/lay-histogram [:num]]
   :density ['pj/lay-density [:num]]
   :rug ['pj/lay-rug [:num]]
   :bar ['pj/lay-bar [:cat]]
   :bar2 ['pj/lay-bar [:cat :num]]
   :boxplot ['pj/lay-boxplot [:cat :num]]
   :violin ['pj/lay-violin [:cat :num]]
   :summary ['pj/lay-summary [:cat :num]]
   :lollipop ['pj/lay-lollipop [:cat :num]]})

(defn- col-of [ds kind] (pick (get ds kind)))

(def ^:private num-types [:point :line :smooth :area :step :histogram :density :rug])
(def ^:private cat-types [:bar :bar2 :boxplot :violin :summary :lollipop])

(def ^:private ^:dynamic *picked* nil)

(defn- pick-columns [ds xkind]
  (let [x (col-of ds xkind)
        y (pick (remove #{x} (:num ds)))]
    {:xkind xkind :x x :y y
     :series (vec (remove #{x} (:series ds)))}))

(defn- gen-layer [ds]
  (let [{:keys [xkind x y series]} *picked*
        coherent? (chance 0.85)
        lt (if coherent? (pick (if (= xkind :cat) cat-types num-types)) (pick (keys layer-types)))
        [fsym kinds] (layer-types lt)
        n (if (= 1 (count kinds)) (pick-w {0 35 1 55 2 10}) (pick-w {0 30 1 15 2 55}))
        xcol (if (and coherent? (chance 0.75)) x (col-of ds (first kinds)))
        ycol (cond (and (>= (count series) 2) (chance 0.1)) series
                   (and coherent? (chance 0.7)) y
                   :else (col-of ds (or (second kinds) (pick [:num :cat]))))
        cols (case n 0 [] 1 [xcol] 2 [xcol ycol])
        m (cond-> {}
            (and (= n 0) (chance 0.1)) (assoc :y (col-of ds :num))
            (chance 0.22) (assoc :color (col-of ds :cat))
            (chance 0.05) (assoc :color (col-of ds :num))
            (chance 0.08) (assoc :color "black")
            (chance 0.03) (assoc :nosuch-layer-key 1))]
    (into [fsym] (cond-> cols (seq m) (conj m)))))

(defn- gen-pose-step [ds]
  (let [{:keys [x y]} *picked*]
    (pick-w {nil 35
             ['pj/pose] 7
             ['pj/pose x] 15
             ['pj/pose x y] 30
             ['pj/pose {:x x :y y}] 6
             ['pj/pose {:x x :y y :color (col-of ds :cat)}] 7})))

(defn- gen-share []
  (pick [#{:x} #{:y} #{} #{:x :y} {:y :all}]))

(defn- gen-leaf-steps [ds]
  (binding [*picked* (pick-columns ds (if (chance 0.6) :num :cat))]
    (let [ps (gen-pose-step ds)
          nl (pick-w {0 4 1 48 2 34 3 14})]
      ;; a pose with no layer still needs a pose step to be a pose
      (vec (concat (cond ps [ps] (zero? nl) [['pj/pose]]) (repeatedly nl #(gen-layer ds)))))))

(defn- gen-recipe [i]
  (let [dk (pick-w (into {} (map (fn [[k v]] [k (:w v)]) datasets)))
        ds (datasets dk)
        wrapper (pick-w {:none 40 :facet-cat 15 :facet-cols 10 :arrange-cols 10
                         :facet-matrix 5 :arrange-poses 12 :arrange-one 8})
        share (when (chance 0.3) (gen-share))
        flip? (chance 0.1)
        bogus? (chance 0.04)
        tail (cond-> []
               (and share (chance 0.5)) (conj ['pj/options {:share-scales share}])
               bogus? (conj ['pj/options {:nosuch-option 1}])
               flip? (conj ['pj/coord :flip]))
        share-in-tail? (some #(= % ['pj/options {:share-scales share}]) tail)
        trailing-opts (when (and share (not share-in-tail?)) {:share-scales share})
        trailing-opts (if (chance 0.06) (assoc trailing-opts :nosuch-option 1) trailing-opts)]
    (merge
     {:id i :ds dk :wrapper wrapper}
     (binding [*picked* (pick-columns ds :num)]
       (case wrapper
         :none {:steps (into (gen-leaf-steps ds) tail)}
         :facet-cat (let [leaf (gen-leaf-steps ds)
                          f (cond-> ['pj/facet (col-of ds :cat)] trailing-opts (conj trailing-opts))]
                      {:steps (-> leaf (conj f) (into tail))})
         :facet-cols (let [[a b] (shuffle-r (:num ds))
                           axis (pick [:y :x])
                           f (cond-> ['pj/facet {axis [a b]}] trailing-opts (conj trailing-opts))
                           before? (chance 0.5)
                           pose-step (pick [nil ['pj/pose {(if (= axis :y) :x :y) (col-of ds :num)}]])
                           layers (vec (repeatedly (pick-w {1 70 2 30}) #(gen-layer ds)))]
                       {:steps (-> (vec (when pose-step [pose-step]))
                                   (into (if before? (conj layers f) (into [f] layers)))
                                   (into tail))})
         :arrange-cols (let [[a b c] (shuffle-r (:num ds))
                             cells (pick (remove nil? [[[a b] [a (or c b)]]
                                                       [a b]
                                                       [[(col-of ds :cat) a] [(col-of ds :cat) b]]]))
                             ar (cond-> ['pj/arrange cells] trailing-opts (conj trailing-opts))
                             layers (vec (repeatedly (pick-w {1 70 2 30}) #(gen-layer ds)))]
                         {:steps (-> [ar] (into layers) (into tail))})
         :facet-matrix (let [cols (vec (take 2 (shuffle-r (:num ds))))
                             layers (vec (repeatedly (pick-w {0 30 1 70}) #(let [l (gen-layer ds)] [(first l)])))]
                         {:steps (-> [['pj/facet {:col {:x cols} :row {:y cols}}]]
                                     (into layers)
                                     (cond-> trailing-opts (conj ['pj/options trailing-opts]))
                                     (into tail))})
         :arrange-poses (let [n (pick-w {2 75 3 25})
                              cells (vec (repeatedly n (fn [] (let [dk2 (if (chance 0.7) dk (pick (keys datasets)))]
                                                                {:ds dk2 :steps (gen-leaf-steps (datasets dk2))}))))]
                          {:cells cells :arrange-opts trailing-opts :steps tail})
         :arrange-one {:cells [{:ds dk :steps (gen-leaf-steps ds)}] :arrange-opts trailing-opts :steps tail})))))

(defn recipes
  "`n` recipes from `seed`."
  [seed n]
  (binding [*rng* (java.util.Random. (long seed))]
    (mapv gen-recipe (range n))))

;; ---------------------------------------------------------------- build and print

(defn- fn-of [sym] (deref (ns-resolve 'scicloj.plotje.api (symbol (name sym)))))

(defn- apply-steps [init steps]
  (reduce (fn [acc [f & args]] (apply (fn-of f) acc args)) init steps))

(defn build
  "The pose a recipe describes."
  [rec]
  (if (:cells rec)
    (let [cells (mapv build (:cells rec))
          a (if (:arrange-opts rec) (pj/arrange cells (:arrange-opts rec)) (pj/arrange cells))]
      (apply-steps a (:steps rec)))
    (apply-steps (:data (datasets (:ds rec))) (:steps rec))))

(defn- step-form [[f & args]] (apply list f args))

(defn form-of
  "The form of a recipe, as it would be written."
  [rec]
  (if (:cells rec)
    (let [a (apply list (cond-> (list 'pj/arrange (mapv form-of (:cells rec)))
                          (:arrange-opts rec) (concat [(:arrange-opts rec)])))]
      (if (seq (:steps rec)) (apply list '-> a (map step-form (:steps rec))) a))
    (apply list '-> (:sym (datasets (:ds rec))) (map step-form (:steps rec)))))

;; ---------------------------------------------------------------- outcome

(defn- safe-walk
  "A postwalk that descends only into maps, vectors, lists and sets;
   anything else (a dtype Column, an array) is a leaf."
  [f x]
  (f (cond
       (record? x) (into {} (map (fn [[k v]] [k (safe-walk f v)]) x))
       (map? x) (into {} (map (fn [[k v]] [k (safe-walk f v)]) x))
       (vector? x) (mapv #(safe-walk f %) x)
       (set? x) (set (map #(safe-walk f %) x))
       (list? x) (apply list (map #(safe-walk f %) x))
       (seq? x) (doall (map #(safe-walk f %) x))
       :else x)))

(defn- round-n [x] (if (number? x) (/ (Math/round (* 100.0 (double x))) 100.0) x))
(defn- rdom [d] (when d (mapv round-n d)))
(defn- rcol [c] (when (and (sequential? c) (= 4 (count c)) (every? number? c))
                  (mapv #(/ (Math/round (* 1000.0 (double %))) 1000.0) c)))

(def ^:private ignored-in-geometry
  #{:color :colors :label :group-key :dodge-idx :style :alpha-scale :size-scale
    :fixed-tooltip :shape-map :size-extent :alpha-extent :row-indices
    :alpha-drawn? :size-drawn? :x-domain :y-domain :dodge-ctx :color-categories :color-category})

(defn- numeric-seq? [v]
  (and (or (sequential? v) (and (some? v) (.isArray (class v))) (instance? java.util.List v))
       (not (string? v))))

(defn- geom-count
  "The number of elements in the largest numeric collection of a plan
   layer, summed over its groups."
  [layer]
  (let [m (into {} layer)
        from-map (fn [g] (reduce max 0 (for [[k v] g :when (and (not (ignored-in-geometry k)) (numeric-seq? v))]
                                         (count v))))]
    (+ (reduce + 0 (map #(if (map? %) (from-map %) 0) (:groups m)))
       (from-map (dissoc m :groups)))))

(defn- panel-summary [p]
  {:xd (rdom (:x-domain p)) :yd (rdom (:y-domain p))
   :n-layers (count (:layers p))
   :marks (mapv geom-count (:layers p))})

(defn- leaf-summary [pl path]
  {:path path
   :x-label (:x-label pl) :y-label (:y-label pl)
   :legend-labels (mapv (comp str :label) (get-in pl [:legend :entries]))
   :panels (mapv panel-summary (:panels pl))})

(defn- leaves [pl path]
  (if (:composite? pl)
    (vec (mapcat (fn [sp] (leaves (:plan sp) (conj path (:path sp)))) (:sub-plots pl)))
    [(leaf-summary pl path)]))

(defn- composite-chromes [pl]
  (if (:composite? pl)
    (cons (:chrome pl) (mapcat #(composite-chromes (:plan %)) (:sub-plots pl)))
    []))

(defn- summarize-plan [pl]
  (let [shared-legend (some #(let [sl (:shared-legend %)] (or (:legend sl) sl)) (composite-chromes pl))]
    {:composite? (boolean (:composite? pl))
     :leaves (leaves pl [])
     :shared-legend-labels (mapv (comp str :label) (:entries shared-legend))}))

(defn- ex-summary [^Throwable e]
  {:class (.getName (class e))
   :ex-info? (instance? clojure.lang.ExceptionInfo e)
   :msg (let [m (or (.getMessage e) "")] (subs m 0 (min 300 (count m))))
   :origin (->> (.getStackTrace e)
                (map #(.getClassName ^StackTraceElement %))
                (remove #(or (str/starts-with? % "clojure.")
                             (str/starts-with? % "java.")
                             (str/starts-with? % "jdk.")))
                first)})

(def ^:private computing-stats #{:bin :density :count})

(defn- cell-identities
  "For a composite pose: sub-draft path to {:x #{identity} :y #{identity}},
   where an identity is a column, or [:computed stat column] for an axis a
   stat computes."
  [p]
  (into {}
        (for [sd (:sub-drafts (pj/draft p))
              :let [ls (let [x (:draft sd)] (if (map? x) (:layers x) x))]]
          [(:path sd)
           {:x (set (map (fn [l] (if (and (:x l) (not (keyword? (:x l)))) [:other (:x l)] (:x l))) ls))
            :y (set (map (fn [l] (if (computing-stats (:stat l)) [:computed (:stat l) (:x l)] (:y l))) ls))}])))

(defn attempt
  "Build, plan and render a recipe, and summarize the outcome."
  [rec]
  (let [res (atom nil)
        out (with-out-str
              (reset! res
                      (try
                        (let [p (build rec)
                              pl (pj/plan p)
                              svg (pj/svg-summary p)]
                          {:status :ok
                           :plan (summarize-plan pl)
                           :cells (when (:composite? pl) (cell-identities p))
                           :leaf-layers (when-not (:composite? pl)
                                          (mapv #(select-keys % [:x :y :stat :layer-type]) (:layers (pj/draft p))))
                           :marks (+ (:points svg 0) (:lines svg 0) (:polygons svg 0) (:visible-tiles svg 0))
                           :no-data (count (filter #(= "no data" %) (:texts svg)))})
                        (catch Throwable e {:status :error :ex (ex-summary e)}))))]
    (assoc (deref res) :printed (str/trim out))))

(defn- attempt-pose
  "Plan a pose made by `build-fn`, for the invariants that compare two poses."
  [build-fn]
  (let [res (atom nil)
        out (with-out-str
              (reset! res (try {:status :ok :plan (summarize-plan (pj/plan (build-fn)))}
                               (catch Throwable e {:status :error :ex (ex-summary e)}))))]
    (assoc (deref res) :printed (str/trim out))))

;; ---------------------------------------------------------------- recipe facts

(defn- all-steps [rec] (concat (:steps rec) (mapcat all-steps (:cells rec))))

(defn- lay-step? [[f]] (str/starts-with? (name f) "lay-"))

(defn- share-of [rec]
  (or (:share-scales (:arrange-opts rec))
      (some (fn [[f & args]] (some #(when (map? %) (:share-scales %))
                                   (when (#{"options" "facet" "arrange"} (name f)) args)))
            (all-steps rec))))

(defn- flip? [rec] (some #(= % ['pj/coord :flip]) (all-steps rec)))

(defn- side-on?
  "A recipe with a layer naming only :y, which is drawn on its side (decided)."
  [rec]
  (some (fn [st] (and (lay-step? st)
                      (let [args (rest st) m (some #(when (map? %) %) args)]
                        (and (:y m) (not (:x m)) (not-any? #(or (keyword? %) (vector? %)) args)))))
        (all-steps rec)))

(defn- norm [s] (-> (str s) str/lower-case (str/replace #"[-_]" " ")))

(defn- panel-cmp [l]
  (mapv (fn [p] [(:xd p) (:yd p) (reduce + (:marks p))]) (:panels l)))

;; ---------------------------------------------------------------- invariants
;; Each takes a recipe and its outcome, and returns nil or a violation map.

(defn no-raw-exception
  "Every error is an ex-info thrown from a Plotje namespace."
  [rec o]
  (when (= :error (:status o))
    (let [{:keys [ex-info? origin class msg]} (:ex o)]
      (cond
        (not ex-info?) {:kind :raw-exception :class class :msg msg :origin origin}
        (and origin (not (str/starts-with? origin "scicloj.plotje")))
        {:kind :foreign-ex-info :origin origin :msg msg}))))

(defn no-silent-nothing
  "A pose that draws draws marks, or a \"no data\" panel, or prints something."
  [rec o]
  (when (and (= :ok (:status o)) (zero? (:marks o)) (zero? (:no-data o)) (str/blank? (:printed o)))
    {:kind :blank-figure}))

(defn shared-axes-agree
  "Where an axis is shared, panels drawing the same column on it have the
   same domain; under `{:y :all}` values share with values, counts with
   counts and densities with densities."
  [rec o]
  (when (and (= :ok (:status o)) (not (flip? rec)) (not (side-on? rec)))
    (let [share (share-of rec)
          lvs (get-in o [:plan :leaves])
          one-data? (#{:facet-cat :facet-cols :arrange-cols :facet-matrix} (:wrapper rec))
          axes (or (cond (set? share) share (map? share) (set (keys share)))
                   (when one-data? #{:x :y}))
          all? (fn [axis] (and (map? share) (= :all (get share axis))))
          dk {:x :xd :y :yd}
          lk {:x :x-label :y :y-label}
          cells (:cells o)
          problems
          (vec
           (for [axis (or axes #{})
                 :let [entries
                       (if cells
                         (for [l lvs
                               :let [ids (get-in cells [(first (:path l)) axis])
                                     p (first (:panels l))]
                               :when (and (= 1 (count ids)) (some? (first ids)) (= 1 (count (:panels l)))
                                          (pos? (:n-layers p)))]
                           {:label (let [id (first ids)
                                         kind (if (vector? id) (second id) :values)]
                                     (if (all? axis) [::all kind] id))
                            :dom (get p (dk axis))})
                         (for [p (:panels (first lvs))
                               :when (pos? (:n-layers p))]
                           {:label (get (first lvs) (lk axis)) :dom (get p (dk axis))}))]
                 [lab es] (group-by :label entries)
                 :let [doms (set (map :dom es))]
                 :when (> (count doms) 1)]
             {:axis axis :id lab :domains (vec doms)}))]
      (when (seq problems) {:kind :shared-axis-differs :share share :problems problems}))))

(defn unread-key-reported
  "An unknown key in a layer's mapping, in `pj/options`, in a facet's map
   or in the options of `pj/arrange` is named in a message."
  [rec o]
  (let [bogus (concat (filter #(str/includes? (pr-str %) "nosuch") (all-steps rec))
                      (when (str/includes? (pr-str (:arrange-opts rec)) "nosuch") [:arrange]))]
    (when (and (seq bogus) (= :ok (:status o))
               (not (str/includes? (str (:printed o)) "nosuch")))
      {:kind :unread-key-silent :steps (vec bogus)})))

(defn facet-equals-cells
  "`(pj/facet leaf :c)` draws what the leaf listed with one `{:where {:c v}}`
   cell per value draws, panel by panel."
  [rec o]
  (let [steps (:steps rec)
        fi (first (keep-indexed (fn [i [f & args]] (when (and (= "facet" (name f)) (keyword? (first args))) i)) steps))]
    (when (and fi (= :facet-cat (:wrapper rec)) (= :ok (:status o)) (not= :grouped (:ds rec)))
      (let [[_ c fopts] (steps fi)
            pre (subvec steps 0 fi)
            post (subvec steps (inc fi))
            data (:data (datasets (:ds rec)))
            vals (vec (distinct (get data c)))
            listed (fn []
                     (let [leaf (apply-steps (pj/pose data) pre)
                           comp (cond-> (assoc leaf :poses (mapv (fn [v] {:where {c v}}) vals)
                                               :layout {:direction :horizontal})
                                  fopts (update :opts merge fopts))]
                       (apply-steps comp post)))
            a (attempt-pose listed)]
        (if (not= :ok (:status a))
          {:kind :listed-form-fails :ex (:ex a) :values vals}
          (let [fp (panel-cmp (first (get-in o [:plan :leaves])))
                lp (vec (mapcat panel-cmp (get-in a [:plan :leaves])))]
            (when (not= fp lp)
              {:kind :facet-differs-from-cells :facet fp :cells lp :values vals})))))))

(defn arrange-one-cell
  "`(pj/arrange [p])` draws what `p` draws: domains, marks, legend labels."
  [rec o]
  (when (and (= :none (:wrapper rec)) (= :ok (:status o)))
    (let [a (attempt-pose #(pj/arrange [(build rec)]))]
      (if (not= :ok (:status a))
        {:kind :one-cell-arrangement-fails :ex (:ex a)}
        (let [p (mapv panel-cmp (get-in o [:plan :leaves]))
              q (mapv panel-cmp (get-in a [:plan :leaves]))
              lp (set (mapcat :legend-labels (get-in o [:plan :leaves])))
              lq (set (concat (get-in a [:plan :shared-legend-labels])
                              (mapcat :legend-labels (get-in a [:plan :leaves]))))]
          (cond (not= p q) {:kind :one-cell-differs :pose p :arranged q}
                (not= lp lq) {:kind :one-cell-legend-differs :pose lp :arranged lq}))))))

(defn flip-swaps-domains
  "`(pj/coord p :flip)` swaps the `:x` and `:y` domains and keeps the marks."
  [rec o]
  (when (and (= :none (:wrapper rec)) (= :ok (:status o)) (not (flip? rec)) (not (side-on? rec))
             ;; a pose naming only :y is drawn on its side, and :flip leaves it so (decided)
             (not (some (fn [l] (and (:y l) (nil? (:x l)))) (:leaf-layers o)))
             (not (some (fn [l] (and (keyword? (:y l)) (computing-stats (:stat l)) (= (:x l) (:y l)))) (:leaf-layers o))))
    (let [a (attempt-pose #(pj/coord (build rec) :flip))]
      (if (not= :ok (:status a))
        {:kind :flip-fails :ex (:ex a)}
        (let [p (mapv (fn [l] (mapv (fn [[x y n]] [y x n]) (panel-cmp l))) (get-in o [:plan :leaves]))
              q (mapv panel-cmp (get-in a [:plan :leaves]))]
          (when (not= p q) {:kind :flip-differs :pose p :flipped q}))))))

(defn- data-column? [rec lab]
  (let [dk (or (:ds rec) (:ds (first (:cells rec))))
        data (if (= :grouped dk) mpg (:data (datasets dk)))]
    (and (string? lab)
         (boolean (some #(= (norm (name %)) (norm lab)) (tc/column-names data))))))

(defn computed-axis-title
  "A leaf whose every layer computes its `:y` (a count or a density) does
   not title `:y` with a column of the data."
  [rec o]
  (let [ls (:leaf-layers o)
        lab (get-in o [:plan :leaves 0 :y-label])]
    (when (and (= :ok (:status o)) (seq ls) (not (flip? rec)) (not (side-on? rec))
               (every? #(computing-stats (:stat %)) ls)
               (not-any? #(and (:y %) (nil? (:x %))) ls)
               (not-any? #(= (:x %) (:y %)) ls)
               (data-column? rec lab))
      {:kind :count-axis-titled-by-a-column :y-label lab :layers ls})))

(def invariants
  "The eight invariants firm enough to assert, by name."
  [[:no-raw-exception no-raw-exception]
   [:no-silent-nothing no-silent-nothing]
   [:shared-axes-agree shared-axes-agree]
   [:unread-key-reported unread-key-reported]
   [:facet-equals-cells facet-equals-cells]
   [:arrange-one-cell arrange-one-cell]
   [:flip-swaps-domains flip-swaps-domains]
   [:computed-axis-title computed-axis-title]])

(defn run
  "Generate `n` recipes from `seed`, attempt each, and check every
   invariant. Returns one map per recipe."
  [seed n]
  (vec (for [rec (recipes seed n)
             :let [o (attempt rec)]]
         {:rec rec :form (form-of rec) :outcome o
          :violations (into {}
                            (for [[k f] invariants
                                  :let [v (try (f rec o)
                                               (catch Throwable e {:kind :check-failed :msg (.getMessage e)}))]
                                  :when v]
                              [k v]))})))
