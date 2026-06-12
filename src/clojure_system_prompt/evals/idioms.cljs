(ns clojure-system-prompt.evals.idioms
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private idioms-judge
  (evals/create-judge
   "IdiomsJudge"
   (fn [{:keys [output metadata]}]
     (let [has-required (or (not (:requiredSnippet metadata))
                            (str/includes? output
                                           (:requiredSnippet metadata)))
           has-forbidden (and (:forbiddenSnippet metadata)
                              (str/includes? output
                                             (:forbiddenSnippet metadata)))]
       {:score    (if (and has-required (not has-forbidden)) 1 0)
        :metadata {:requiredSnippet  (:requiredSnippet metadata)
                   :forbiddenSnippet (:forbiddenSnippet metadata)
                   :hasRequired      has-required
                   :hasForbidden     has-forbidden}}))))

(def ^:private test-cases
  [{:name            "uses some-> for nil-safe nested access"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.some-arrow with a "
                          "namespace docstring and one public function. "
                          "The function should take a user map and return the "
                          "first 5 characters of the nested postal code when "
                          "present, otherwise nil. Use nil-safe style.")
    :requiredSnippet "some->"}
   {:name            "uses ->> for sequence pipelines"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.thread-last with a "
                          "namespace docstring and one public function. "
                          "The function should take users, keep active ones, "
                          "extract emails, remove nils, and join them with ", ". "
                          "Use sequence-pipeline style.")
    :requiredSnippet "->>"}
   {:name            "uses cond-> for conditional map transforms"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.cond-arrow with a "
                          "namespace docstring and one public function. "
                          "The function should take a request map plus two "
                          "flags for authenticated and admin, then conditionally "
                          "add user and permissions fields. Use conditional "
                          "transformation style.")
    :requiredSnippet "cond->"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt idioms"
 {:harness         llama-server-harness
  :judges          [idioms-judge]
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
                (when (.-requiredSnippet tc)
                  (.toContain (expect output) (.-requiredSnippet tc)))
                (when (.-forbiddenSnippet tc)
                  (.. (expect output)
                      -not
                      (toContain (.-forbiddenSnippet tc)))))))))))))
