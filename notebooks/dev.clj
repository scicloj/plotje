(ns dev
  (:require [scicloj.clay.v2.api :as clay]))

(def ^:private read-chapters
  (fn []
    (-> "notebooks/chapters.edn" slurp clojure.edn/read-string)))

(def ^:private chapters->source-paths
  (fn [chapters]
    (into [] (mapcat (fn [[_part names]]
                       (map #(format "plotje_book/%s.clj" %) names)))
          chapters)))

(def ^:private chapters->parts
  (fn [chapters]
    (mapv (fn [[part names]]
            {:part part
             :chapters (mapv #(format "plotje_book/%s.clj" %) names)})
          chapters)))


(defn make-readme!
  "Render readme.clj as GitHub-flavored Markdown to the repo root.
   Produces README.md and readme_files/ with SVG images."
  []
  (clay/make! {:format [:gfm]
               :base-source-path "notebooks"
               :source-path ["readme.clj"]
               :base-target-path "."
               :show false})
  ;; Rename to conventional uppercase README.md
  (let [src (java.io.File. "readme.md")
        dst (java.io.File. "README.md")]
    (when (.exists src)
      (.delete dst)
      (.renameTo src dst))))

(defn make-book!
  "Render book HTML through Quarto.
  Use `:docs true` to render to the `docs` directory for publishing."
  [{:keys [docs]}]
  (make-readme!)
  (clay/make! (merge {:format [:quarto :html]
                      :base-source-path "notebooks"
                      :source-path (into ["index.clj"] (chapters->parts (read-chapters)))
                      :book {:title "Plotje"}
                      :clean-up-target-dir true
                      :base-target-path (if docs
                                          "docs"
                                          ".clay-temp-docs")})))

(defn make-gfm!
  "Render all (or specified) notebooks as GitHub-flavored Markdown."
  [& paths]
  (clay/make! {:format [:gfm]
               :base-source-path "notebooks"
               :source-path (or (seq paths)
                                (into ["index.clj"]
                                      (chapters->source-paths (read-chapters))))
               :base-target-path "gfm"
               :show false}))


(comment
  (make-readme!)
  (make-book! {:docs true})
  (make-book! {:docs false})
  (make-gfm!)
  (make-gfm! "wide_and_long.clj")
  (make-gfm! "plotje_book/quickstart.clj"))
