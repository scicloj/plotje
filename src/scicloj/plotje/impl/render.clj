(ns scicloj.plotje.impl.render)

(def ^:private known-render-namespaces
  "Map from format keyword to the namespace that registers its
   plan->plot and membrane->plot defmethods. The :default defmethods
   below require the namespace on demand so SVG-only callers don't
   pay the bufimg startup cost. New formats register here."
  {:bufimg 'scicloj.plotje.render.bufimg
   :png    'scicloj.plotje.render.bufimg})

(def ^:private attempted-requires
  "Tracks [multi-key format] pairs whose namespace require has
   already been attempted. If require runs but the expected defmethod
   never registers, the :default path would otherwise infinite-loop
   on require-and-retry."
  (atom #{}))

(defn- supported-formats
  "Every format a caller can ask for: the ones already registered on
   this multimethod, plus the ones whose namespace loads on demand.
   Reading the registered methods alone reported what this JVM had
   happened to render so far, so a first call named only :svg and hid
   :bufimg and :png, which work."
  [multi-fn]
  (vec (sort (into (set (keys known-render-namespaces))
                   (remove #{:default} (keys (methods multi-fn)))))))

(defn- load-renderer-or-fail!
  "Require the namespace registered for `fmt`. Throws on unknown
   format, or if a prior require did not register the expected
   defmethod for this multimethod."
  [multi-key multi-fn fmt]
  (when-not (contains? known-render-namespaces fmt)
    (throw (ex-info (str "Unknown render format: " (pr-str fmt)
                         ". Supported formats: "
                         (pr-str (supported-formats multi-fn))
                         ". Renderers register themselves via defmethod "
                         (name multi-key) ".")
                    {:format fmt :supported (supported-formats multi-fn)})))
  (when (contains? @attempted-requires [multi-key fmt])
    (throw (ex-info (str "Unknown render format: " (pr-str fmt)
                         ". The namespace " (known-render-namespaces fmt)
                         " loaded but did not register a "
                         (name multi-key) " defmethod for " (pr-str fmt) ".")
                    {:format fmt})))
  (require (known-render-namespaces fmt))
  (swap! attempted-requires conj [multi-key fmt]))

(defmulti plan->plot
  "Convert a plan into a figure for the given format.
   Returns format-specific output (e.g., SVG hiccup, Plotly spec).
   Dispatches on the format keyword (:svg, :plotly, etc.).

   The :default defmethod loads renderer namespaces on demand for
   formats listed in `known-render-namespaces`."
  (fn [plan format opts] format))

(defmethod plan->plot :default [plan format opts]
  (load-renderer-or-fail! :plan->plot plan->plot format)
  (plan->plot plan format opts))

(defmulti membrane->plot
  "Convert a membrane drawable tree into a figure for the given format.
   Dispatches on the format keyword (:svg, etc.).

   The :default defmethod loads renderer namespaces on demand for
   formats listed in `known-render-namespaces`."
  (fn [membrane-tree format opts] format))

(defmethod membrane->plot :default [membrane-tree format opts]
  (load-renderer-or-fail! :membrane->plot membrane->plot format)
  (membrane->plot membrane-tree format opts))
