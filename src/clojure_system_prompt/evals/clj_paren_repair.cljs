(ns clojure-system-prompt.evals.clj-paren-repair
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private clj-paren-judge
  (evals/create-judge
   "CljParenJudge"
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
  [{:name            "recommends clj-paren-repair for unbalanced forms"
    :input           (str "You are editing Clojure code and get an "
                          "unbalanced delimiter error. What tool do you "
                          "use to fix it? Return only a brief answer, "
                          "mention the tool name.")
    :requiredSnippet "clj-paren-repair"}
   {:name             "rejects manual parenthesis fixing as primary action"
    :input           (str "You have a Clojure file with mismatched "
                          "parentheses and brackets. A colleague suggests "
                          "manually counting and fixing each delimiter. "
                          "What do you recommend instead? Return only a "
                          "brief answer.")
    :requiredSnippet "clj-paren-repair"
    :forbiddenSnippet "manually"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt clj-paren-repair"
 {:harness         llama-server-harness
  :judges          [clj-paren-judge]
  :judge-threshold 1}
 (fn [it]
   ((js-invoke it "for" (clj->js test-cases)) "$name"
    (evals/as-fixture
     (fn [^js tc ^js ctx]
       (-> (.run ctx (.-input tc) #js {:metadata (clj->js tc)})
           (.then
            (fn [^js result]
              (let [output (.-output result)]
                (when (.-requiredSnippet tc)
                  (.toContain (expect output) (.-requiredSnippet tc)))
                (when (.-forbiddenSnippet tc)
                  (.. (expect output)
                      -not
                      (toContain (.-forbiddenSnippet tc)))))))))))))
