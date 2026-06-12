(ns clojure-system-prompt.evals.ns-conventions
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private ns-convention-judge
  (evals/create-judge
   "NamespaceConventionJudge"
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
  [{:name            "includes a namespace docstring"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.docstring with one "
                          "trivial public function. "
                          "Include a namespace docstring.")
    :requiredSnippet "(ns demo.docstring\n  \""}
   {:name             "avoids bang suffix on side-effecting function names"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.naming with a "
                           "namespace docstring and a side-effecting function "
                           "that saves a user. "
                           "Do not use a ! suffix.")
    :requiredSnippet  "(defn save-user"
    :forbiddenSnippet "(defn save-user!"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt namespace conventions"
 {:harness         llama-server-harness
  :judges          [ns-convention-judge]
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
