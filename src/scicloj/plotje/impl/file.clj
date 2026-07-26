(ns scicloj.plotje.impl.file)

(defn imeta-file
  "Returns a proxy of an io/file that can have metadata attached"
  ([pathname] (imeta-file pathname {}))
  ([pathname metadata]
   (let [m (atom metadata)]
     (proxy [java.io.File clojure.lang.IObj] [^String pathname]
       ;; 1. Core Clojure Metadata Interface
       (meta [] @m)
       (withMeta [new-meta] (imeta-file pathname new-meta))

       ;; 2. Ensure Equality works correctly based on path
       (equals [other]
         (and (instance? java.io.File other)
              (= (.getAbsolutePath this) (.getAbsolutePath other))))

       (hashCode []
         (.hashCode (.getAbsolutePath this)))

       ;; 3. Make REPL printing cleaner
       (toString []
         (.getPath this))))))
