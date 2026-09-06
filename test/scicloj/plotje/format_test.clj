(ns scicloj.plotje.format-test
  "Tests for pj/plot honoring :format from a pose's :opts on both
   leaf and composite paths. Before this fix, the leaf path
   hardcoded :svg and the composite branch of pj/plot 1-arity also
   dropped format; only the Kindly auto-render of composites
   (render-composite) read :format. Now pj/plot is the single
   honoring point and render-composite delegates to it."
  (:require [clojure.test :refer [deftest testing is]]
            [scicloj.plotje.api :as pj]))

(def tiny {:x [1.0 2.0 3.0] :y [4.0 5.0 6.0]})

(deftest leaf-pose-default-format-is-svg
  (testing "leaf with no :format returns SVG hiccup"
    (let [out (pj/plot (pj/lay-point tiny :x :y))]
      (is (vector? out))
      (is (= :svg (first out))))))

(deftest leaf-pose-bufimg-format
  (testing "leaf with {:format :bufimg} in opts returns a BufferedImage"
    (let [pose (-> tiny
                   (pj/lay-point :x :y)
                   (pj/options {:format :bufimg}))]
      (is (instance? java.awt.image.BufferedImage (pj/plot pose))))))

(deftest composite-pose-default-format-is-svg
  (testing "composite with no :format returns SVG hiccup"
    (let [out (pj/plot (pj/arrange [(pj/lay-point tiny :x :y)
                                    (pj/lay-point tiny :x :y)]))]
      (is (vector? out) "SVG hiccup is a vector")
      (is (= :svg (first out))
          "SVG hiccup starts with :svg, not a wrapper tag"))))

(deftest composite-pose-bufimg-format
  (testing "composite with {:format :bufimg} returns a BufferedImage"
    (let [comp (-> (pj/arrange [(pj/lay-point tiny :x :y)
                                (pj/lay-point tiny :x :y)])
                   (pj/options {:format :bufimg}))]
      (is (instance? java.awt.image.BufferedImage (pj/plot comp))))))

(deftest format-via-with-config
  (testing "with-config {:format :bufimg} also flows through"
    (let [pose (pj/lay-point tiny :x :y)]
      (pj/with-config {:format :bufimg}
        ;; with-config sets a thread-local; pj/plot reads (:format
        ;; (:opts fr)) -- so for this to work, the format needs to
        ;; be on the pose's :opts. Demonstrate the per-pose path
        ;; is the current contract:
        (is (vector? (pj/plot pose))
            "with-config on its own does NOT inject :format into pose opts")))))

(deftest unknown-format-throws
  (testing "unknown format raises a clear ex-info from plan->plot dispatch"
    (let [pose (-> tiny
                   (pj/lay-point :x :y)
                   (pj/options {:format :nonexistent}))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Unknown render format"
           (pj/plot pose))))))

(deftest unknown-format-message-lists-lazily-loaded-formats
  ;; The list was built from the defmethods registered so far, so the
  ;; first call in a JVM named :svg alone and hid :bufimg and :png,
  ;; whose namespaces load on demand. A caller was told a format that
  ;; works does not exist.
  (testing "the message names every format a caller can ask for"
    (let [pose (-> tiny (pj/lay-point :x :y) (pj/options {:format :nonexistent}))
          msg (try (pj/plot pose) (catch clojure.lang.ExceptionInfo e (ex-message e)))]
      (is (re-find #":bufimg" msg))
      (is (re-find #":png" msg))
      (is (re-find #":svg" msg))))
  (testing "and says so before anything has rendered a bufimg"
    ;; Same question asked of the data the message is built from, so the
    ;; assertion holds whatever this JVM has already rendered.
    (let [pose (-> tiny (pj/lay-point :x :y) (pj/options {:format :nonexistent}))
          supported (:supported (try (pj/plot pose)
                                     (catch clojure.lang.ExceptionInfo e (ex-data e))))]
      (is (= [:bufimg :png :svg] (vec (sort supported)))))))

;; ---- pj/save format resolution (B2: opts > extension > :svg default) ----

(defn- read-magic [path]
  (let [bs (with-open [in (java.io.FileInputStream. ^String path)]
             (let [buf (byte-array 8)
                   n (.read in buf)]
               (vec (take n buf))))]
    bs))

(defn- svg? [path]
  ;; SVG starts with "<?xml" => bytes 0x3C 0x3F 0x78 0x6D 0x6C
  (let [bs (read-magic path)]
    (= [0x3C 0x3F 0x78 0x6D 0x6C] (mapv #(bit-and ^int % 0xFF) (take 5 bs)))))

(defn- png? [path]
  ;; PNG magic: 0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A
  (let [bs (read-magic path)]
    (= [0x89 0x50 0x4E 0x47] (mapv #(bit-and ^int % 0xFF) (take 4 bs)))))

(deftest save-svg-extension-default
  (testing "(pj/save pose \"x.svg\") writes SVG"
    (let [path "/tmp/_plotje_save_format_a.svg"
          pose (pj/lay-point tiny :x :y)]
      (pj/save pose path)
      (is (svg? path))
      (.delete (java.io.File. path)))))

(deftest save-png-extension-writes-png
  (testing "(pj/save pose \"x.png\") infers :png from extension and writes PNG"
    (let [path "/tmp/_plotje_save_format_b.png"
          pose (pj/lay-point tiny :x :y)]
      (pj/save pose path)
      (is (png? path))
      (.delete (java.io.File. path)))))

(deftest save-opts-format-overrides-extension
  (testing "(pj/save pose \"x.png\" {:format :svg}) writes SVG (opts wins, warns)"
    (let [path "/tmp/_plotje_save_format_c.png"
          pose (pj/lay-point tiny :x :y)]
      (pj/save pose path {:format :svg})
      (is (svg? path))
      (.delete (java.io.File. path))))

  (testing "(pj/save pose \"x.svg\" {:format :png}) writes PNG (opts wins, warns)"
    (let [path "/tmp/_plotje_save_format_d.svg"
          pose (pj/lay-point tiny :x :y)]
      (pj/save pose path {:format :png})
      (is (png? path))
      (.delete (java.io.File. path)))))

(deftest save-opts-format-on-pose-png
  (testing ":format :png set via pj/options on the pose flows through pj/save"
    (let [path "/tmp/_plotje_save_format_e.png"
          pose (-> tiny
                   (pj/lay-point :x :y)
                   (pj/options {:format :png}))]
      (pj/save pose path)
      (is (png? path))
      (.delete (java.io.File. path)))))

;; ---- :x-tick-angle flows through the save / plan->plot render paths ----
;; pj/plot passes the full opts to plan->membrane, but pj/save goes through
;; plan->plot :svg / :bufimg, which rebuild the membrane tree from a
;; select-keys whitelist. :x-tick-angle / :x-tick-label-pad must be in that
;; whitelist or saved files silently lose the rotation the notebook shows.

(def bars {:cat ["a" "b" "c" "d"] :n [1 2 3 4]})

(deftest save-svg-carries-x-tick-angle
  (testing "pj/save SVG includes the x-tick rotation transform"
    (let [path "/tmp/_plotje_xtick_angle.svg"
          pose (-> bars
                   (pj/lay-bar :cat :n)
                   (pj/options {:x-tick-angle -45}))]
      (pj/save pose path)
      (is (.contains ^String (slurp path) "rotate(-45"))
      (.delete (java.io.File. path)))))

(deftest save-svg-no-rotation-without-angle
  (testing "without :x-tick-angle, no x-tick rotation transform is emitted"
    (let [path "/tmp/_plotje_xtick_none.svg"
          pose (pj/lay-bar bars :cat :n)]
      (pj/save pose path)
      (is (not (.contains ^String (slurp path) "rotate(-45")))
      (.delete (java.io.File. path)))))

(deftest save-png-with-x-tick-angle-renders
  (testing "pj/save PNG with :x-tick-angle renders without error (bufimg whitelist)"
    (let [path "/tmp/_plotje_xtick_angle.png"
          pose (-> bars
                   (pj/lay-bar :cat :n)
                   (pj/options {:x-tick-angle -45}))]
      (pj/save pose path)
      (is (png? path))
      (.delete (java.io.File. path)))))

(deftest save-translates-bufimg-from-pose-opts
  (testing "pj/save translates legacy :bufimg from pose opts to :png"
    (let [path "/tmp/_plotje_save_format_bufimg_alias.png"
          pose (-> tiny
                   (pj/lay-point :x :y)
                   (pj/options {:format :bufimg}))]
      (pj/save pose path)
      (is (png? path))
      (.delete (java.io.File. path)))))

(deftest save-rejects-bufimg-in-opts-arg
  (testing "pj/save :format :bufimg in the explicit opts arg throws -- save vocabulary is :svg/:png"
    (let [path "/tmp/_plotje_save_format_bufimg_reject.png"
          pose (pj/lay-point tiny :x :y)]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #":format must be :svg or :png"
           (pj/save pose path {:format :bufimg}))))))

(deftest save-default-fallback-svg
  (testing "(pj/save pose \"x.unknownext\") falls back to :svg"
    (let [path "/tmp/_plotje_save_format_g.unknownext"
          pose (pj/lay-point tiny :x :y)]
      (pj/save pose path)
      (is (svg? path))
      (.delete (java.io.File. path)))))

(deftest save-non-map-opts-throws
  (testing "(pj/save pose path <vector>) throws with helpful message"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"opts map as the third"
         (pj/save (pj/lay-point tiny :x :y) "/tmp/_x.svg" [:not :a :map])))))
