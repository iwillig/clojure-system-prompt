(ns clojure-system-prompt.evals.error-handling
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private error-handling-judge
  (evals/create-judge
   "ErrorHandlingJudge"
   (fn [{:keys [output metadata]}]
     (let [has-required (or (not (:requiredSnippet metadata))
                            (str/includes? output (:requiredSnippet metadata)))
           has-forbidden (and (:forbiddenSnippet metadata)
                              (str/includes? output (:forbiddenSnippet metadata)))]
       {:score    (if (and has-required (not has-forbidden)) 1 0)
        :metadata {:requiredSnippet  (:requiredSnippet metadata)
                   :forbiddenSnippet (:forbiddenSnippet metadata)
                   :hasRequired      has-required
                   :hasForbidden     has-forbidden}}))))

(def ^:private test-cases
  [{:name            "catches specific exceptions not generic"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.specific-catch with a "
                          "namespace docstring and one public function. "
                          "The function reads a file and catches only "
                          "FileNotFoundException, not generic Exception.")
    :requiredSnippet "FileNotFoundException"
    :forbiddenSnippet "(catch Exception"}
   {:name             "uses ex-info with structured data"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.ex-info with a "
                           "namespace docstring and one public function. "
                           "The function validates a user map and throws "
                           "with ex-info including an error map.")
    :requiredSnippet "ex-info"
    :forbiddenSnippet "Exception."}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt error handling"
 {:harness         llama-server-harness
  :judges          [error-handling-judge]
  :judge-threshold 1}
 (fn [it]
   ((js-invoke it "for" (clj->js test-cases)) "$name"
    (evals/as-fixture
     (fn [^js tc ^js ctx]
       (-> (.run ctx (.-input tc) #js {:metadata (clj->js tc)})
           (.then
            (fn [^js result]
              (let [output (.-output result)]
                (.toContain (expect output) "(ns demo.")
                (.. (expect output) -not (toContain "```"))
                (when (.-requiredSnippet tc)
                  (.toContain (expect output) (.-requiredSnippet tc)))
                (when (.-forbiddenSnippet tc)
                  (.. (expect output)
                      -not
                      (toContain (.-forbiddenSnippet tc)))))))))))))
