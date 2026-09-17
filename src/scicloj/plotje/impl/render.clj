(ns scicloj.plotje.impl.render
  (:require [clojure.string :as str]))

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

(def interactive-formats
  "The formats whose output can carry the `:tooltip` and `:brush`
   aesthetics. Both are drawn by a browser reading the figure, so the
   SVG renderer is the only one that answers them today. A renderer
   that draws interaction adds its format here."
  #{:svg})

(def ^:dynamic *format-asked*
  "The format name the caller wrote, where it differs from the name the
   renderer dispatches on. `pj/save` writes a file, so its vocabulary
   names the file format: a `.png` path reaches the `:bufimg` renderer,
   and a message naming `:bufimg` sends the reader looking for a word
   they never wrote. Bound by `pj/save`; nil everywhere else, where the
   two vocabularies agree.

   Which formats draw interaction is still read from
   `interactive-formats` alone -- this only changes the name the
   message prints."
  nil)

(defn warn-interaction-ignored!
  "Warn when a plot asks for hover text or a brush and the format it is
   rendered to draws neither.

   `plan/warn-unread-tooltip-mappings` reports the same fact reached
   the other way -- a `:tooltip` that reaches no mark that draws one.
   This is the half that said nothing: a raster render returned an
   image, drew no hover text and no brush, and printed nothing at all.

   Called by each `plan->plot` method and by `pj/plot`, which are the
   calls that know the format. Passing every format and letting
   `interactive-formats` decide means a renderer added later is covered
   without a second list naming it."
  [plan format opts]
  (when-not (contains? interactive-formats format)
    (let [asked (cond-> []
                  (or (:tooltip opts) (:tooltip plan)) (conj ":tooltip")
                  (:brush opts) (conj ":brush"))]
      (when (seq asked)
        (println (str "Warning: " (str/join " and " asked) " asked for, and the "
                      (or *format-asked* format)
                      " format draws no interaction. The formats that do: "
                      (str/join ", " (sort interactive-formats))
                      ". The request is accepted and draws nothing."))))))

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
