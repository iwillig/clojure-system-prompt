(ns clojure-system-prompt.evals.naming-conventions
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private naming-judge
  (evals/create-judge
   "NamingJudge"
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
  [{:name            "predicates end with question mark"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.pred-fn with a "
                          "namespace docstring and one public function "
                          "that checks if a user is active. Name it "
                          "using predicate convention.")
    :requiredSnippet "active?"}
   {:name             "uses kebab-case for function names"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.kebab-case with a "
                           "namespace docstring and one public function "
                           "that calculates the total price including tax. "
                           "Use kebab-case naming.")
    :requiredSnippet "total-price"
    :forbiddenSnippet "totalPrice"}
   {:name             "never uses bang suffix on side-effecting functions"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.no-bang with a "
                           "namespace docstring and one public side-effecting "
                           "function that saves a user. Do not use bang suffix.")
    :requiredSnippet "(defn save-user"
    :forbiddenSnippet "(defn save-user!"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt naming conventions"
 {:harness         llama-server-harness
  :judges          [naming-judge]
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
