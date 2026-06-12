(ns clojure-system-prompt.evals.java-imports
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private java-import-judge
  (evals/create-judge
   "JavaImportJudge"
   (fn [{:keys [output metadata]}]
     (let [has-expected (or (not (:expectedImport metadata))
                            (str/includes? output
                                           (:expectedImport metadata)))
           has-forbidden (and (:forbiddenSnippet metadata)
                              (str/includes? output
                                             (:forbiddenSnippet metadata)))]
       {:score    (if (and has-expected (not has-forbidden)) 1 0)
        :metadata {:expectedImport   (:expectedImport metadata)
                   :forbiddenSnippet (:forbiddenSnippet metadata)
                   :hasExpected      has-expected
                   :hasForbidden     has-forbidden}}))))

(def ^:private test-cases
  [{:name             "imports Instant for unqualified static member usage"
    :input            (str "Return only Clojure code. "
                           "Write a namespace declaration and one expression "
                           "that uses Instant/now unqualified. "
                           "The namespace must be named demo.instant.")
    :expectedImport   "(java.time Instant)"
    :forbiddenSnippet "java.time.Instant/now"}
   {:name             "imports UUID for unqualified static member usage"
    :input            (str "Return only Clojure code. "
                           "Write a namespace declaration and one expression "
                           "that uses UUID/randomUUID unqualified. "
                           "The namespace must be named demo.uuid.")
    :expectedImport   "(java.util UUID)"
    :forbiddenSnippet "java.util.UUID/randomUUID"}
   {:name             "allows fully qualified exception in one catch"
    :input            (str "Return only Clojure code. "
                           "Write a small function that catches "
                           "java.io.FileNotFoundException exactly once. "
                           "The namespace must be named demo.catch.")
    :forbiddenSnippet "(java.io FileNotFoundException)"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt Java imports"
 {:harness         llama-server-harness
  :judges          [java-import-judge]
  :judge-threshold 1}
 (fn [it]
   ((js-invoke it "for" (clj->js test-cases)) "$name"
    (evals/as-fixture
     (fn [^js tc ^js ctx]
       (-> (.run ctx (.-input tc) #js {:metadata tc})
           (.then
            (fn [^js result]
              (let [output (.-output result)]
                (.toContain (expect output) "(ns demo.")
                (.. (expect output) -not (toContain "```"))
                (when (.-expectedImport tc)
                  (.toContain (expect output) (.-expectedImport tc)))
                (when (.-forbiddenSnippet tc)
                  (.. (expect output)
                      -not
                      (toContain (.-forbiddenSnippet tc))))
                (when (.includes (.-input tc) "FileNotFoundException")
                  (.toContain (expect output)
                              "java.io.FileNotFoundException")))))))))))
