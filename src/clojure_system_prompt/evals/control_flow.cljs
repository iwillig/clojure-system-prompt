(ns clojure-system-prompt.evals.control-flow
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private control-flow-judge
  (evals/create-judge
   "ControlFlowJudge"
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
  [{:name            "uses when for single-branch side effects"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.when-test with a "
                          "namespace docstring and one public function. "
                          "The function takes a message and, only when it is "
                          "truthy, prints it and returns it. When falsy, "
                          "returns nil. Use single-branch style.")
    :requiredSnippet "when"}
   {:name             "uses cond instead of nested if"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.cond-test with a "
                           "namespace docstring and one public function. "
                           "The function takes a number and returns "
                           ":negative, :zero, or :positive. Use flat "
                           "multi-condition style, not nested ifs.")
    :requiredSnippet "cond"
    :forbiddenSnippet "(if ("}
   {:name            "uses case for constant dispatch"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.case-test with a "
                          "namespace docstring and one public function. "
                          "The function takes a keyword operation and two "
                          "numbers, dispatching to + or - based on the "
                          "keyword. Use constant-dispatch style.")
    :requiredSnippet "case"
    :forbiddenSnippet "(if (="}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt control flow"
 {:harness         llama-server-harness
  :judges          [control-flow-judge]
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
