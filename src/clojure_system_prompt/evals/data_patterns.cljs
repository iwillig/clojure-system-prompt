(ns clojure-system-prompt.evals.data-patterns
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private data-patterns-judge
  (evals/create-judge
   "DataPatternsJudge"
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
  [{:name            "uses keyword keys instead of string keys in maps"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.keyword-keys with a "
                          "namespace docstring and one public function that "
                          "creates a user record with name, email, and role "
                          "fields. Use keyword keys, not string keys.")
    :requiredSnippet ":name"
    :forbiddenSnippet "\"name\""}
   {:name             "uses destructuring in function args"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.destructure with a "
                           "namespace docstring and one public function that "
                           "takes a user map and formats the name as "
                           "\"last, first\". Use destructuring to extract "
                           "first-name and last-name from the map arg.")
    :requiredSnippet ":keys"}
   {:name             "uses into for collection transformation"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.into-test with a "
                           "namespace docstring and one public function that "
                           "takes a collection and returns a vector of "
                           "doubled even numbers. Use into for the "
                           "transformation.")
    :requiredSnippet "into"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt data patterns"
 {:harness         llama-server-harness
  :judges          [data-patterns-judge]
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
