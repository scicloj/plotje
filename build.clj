(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'org.scicloj/plotje)
(def version "0.11.1")
(def snapshot (str version "-SNAPSHOT"))
(def class-dir "target/classes")

(defn- get-version [opts]
  (if (:snapshot opts)
    snapshot
    version))

(defn- jar-opts [opts]
  (let [version (get-version opts)]
    (assoc opts
           :lib lib
           :version version
           :jar-file (format "target/%s-%s.jar" lib version)
           :basis (b/create-basis {})
           :class-dir class-dir
           :target "target"
           :src-dirs ["src"]
           :resource-dirs ["resources"])))

(defn run-tests
  "Run tests via cognitect test runner. Throws if any test fails so
   `ci` does not silently proceed to JAR/pom on a red suite."
  [opts]
  (let [{:keys [exit]} (b/process {:command-args ["clojure" "-M:test" "-m" "cognitect.test-runner"]})]
    (when-not (zero? exit)
      (throw (ex-info (str "Tests failed (exit code " exit "); aborting build.")
                      {:exit exit}))))
  opts)

(defn- pom-template [version]
  [[:description "composable plotting in Clojure"]
   [:url "https://github.com/scicloj/plotje"]
   [:licenses
    [:license
     [:name "MIT License"]
     [:url "https://opensource.org/licenses/MIT"]]]
   [:developers
    [:developer
     [:name "scicloj"]]]
   [:scm
    [:url "https://github.com/scicloj/plotje"]
    [:connection "scm:git:https://github.com/scicloj/plotje.git"]
    [:developerConnection "scm:git:ssh:git@github.com:scicloj/plotje.git"]
    [:tag (str "v" version)]]])

(defn ci
  "Run the CI pipeline (test, clean, build JAR)"
  [opts]
  (run-tests opts)
  (b/delete {:path "target"})
  (let [opts (jar-opts opts)
        version (get-version opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom (assoc opts :pom-data (pom-template version)))
    (println "\nCopying source and resources...")
    (b/copy-dir {:src-dirs ["src" "resources"] :target-dir class-dir})
    (println "\nBuilding JAR..." (:jar-file opts))
    (b/jar opts))
  opts)

(defn deploy
  "Deploy to Clojars"
  [opts]
  (let [opts (jar-opts opts)]
    (dd/deploy (merge {:installer :remote
                       :artifact (:jar-file opts)
                       :pom-file (b/pom-path opts)}
                      opts)))
  opts)
