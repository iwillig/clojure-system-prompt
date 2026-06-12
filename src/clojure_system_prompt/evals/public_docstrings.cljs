(ns clojure-system-prompt.evals.public-docstrings
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(def ^:private docstrings-judge
  (evals/create-judge
   "DocstringsJudge"
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
  [{:name            "public function includes docstring"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.pub-doc with a "
                          "namespace docstring and one public function that "
                          "adds two numbers. The function must have a docstring.")
    :requiredSnippet "(defn add-two-numbers"}
   {:name             "public function docstring includes example"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.doc-example with a "
                           "namespace docstring and one public function that "
                           "calculates the square of a number. Include an "
                           "example in the docstring.")
    :requiredSnippet "Example:"}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt public docstrings"
 {:harness         llama-server-harness
  :judges          [docstrings-judge]
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
