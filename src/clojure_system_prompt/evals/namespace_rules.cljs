(ns clojure-system-prompt.evals.namespace-rules
  (:require
   ["vitest" :refer [expect]]
   [clojure.string :as str]
   [clojure-system-prompt.evals.core :as evals]
   [clojure-system-prompt.evals.harness :refer [llama-server-harness]]))

(defn- namespace-block [output]
  (let [defn-idx (.indexOf output "(defn")
        def-idx (.indexOf output "(def ")
        split-idx (cond
                    (and (>= defn-idx 0) (>= def-idx 0))
                    (min defn-idx def-idx)

                    (>= defn-idx 0)
                    defn-idx

                    (>= def-idx 0)
                    def-idx

                    :else
                    -1)]
    (if (neg? split-idx)
      output
      (.substring output 0 split-idx))))

(def ^:private namespace-rules-judge
  (evals/create-judge
   "NamespaceRulesJudge"
   (fn [{:keys [output metadata]}]
     (let [ns-output (namespace-block output)
           has-required (or (not (:requiredSnippet metadata))
                            (str/includes? ns-output
                                           (:requiredSnippet metadata)))
           has-forbidden (and (:forbiddenSnippet metadata)
                              (str/includes? ns-output
                                             (:forbiddenSnippet metadata)))
           require-idx (.indexOf ns-output "(:require")
           import-idx (.indexOf ns-output "(:import")
           ordered? (or (not (:requireBeforeImport metadata))
                        (= -1 import-idx)
                        (and (not= -1 require-idx)
                             (< require-idx import-idx)))]
       {:score    (if (and has-required
                           (not has-forbidden)
                           ordered?)
                    1
                    0)
        :metadata {:requiredSnippet     (:requiredSnippet metadata)
                   :forbiddenSnippet    (:forbiddenSnippet metadata)
                   :requireBeforeImport (:requireBeforeImport metadata)
                   :hasRequired         has-required
                   :hasForbidden        has-forbidden
                   :ordered             ordered?}}))))

(def ^:private test-cases
  [{:name            "rejects :use in namespace declarations"
    :input           (str "Return only Clojure code. "
                          "Write a namespace named demo.no-use with a "
                          "namespace docstring and a trivial function that "
                          "joins strings using clojure.string. "
                          "Use an alias for clojure.string.")
    :requiredSnippet "[clojure.string :as str]"
    :forbiddenSnippet ":use"}
   {:name             "rejects broad refers for clojure.string"
    :input            (str "Return only Clojure code. "
                           "Write a namespace named demo.no-refer-all with a "
                           "namespace docstring and a trivial function that "
                           "trims a string using clojure.string. "
                           "Use an alias for clojure.string and do not use "
                           "broad refers.")
    :requiredSnippet  "[clojure.string :as str]"
    :forbiddenSnippet ":refer :all"}
   {:name                "puts :require before :import"
    :input               (str "Return only Clojure code. "
                              "Write a namespace named demo.order with a "
                              "namespace docstring, a clojure.string alias, "
                              "and one expression that uses Instant/now "
                              "unqualified.")
    :requiredSnippet     "(:require"
    :requireBeforeImport true}])

(defn ^:export register [] nil)

(evals/describe-eval
 "pi system prompt namespace rules"
 {:harness         llama-server-harness
  :judges          [namespace-rules-judge]
  :judge-threshold 1}
 (fn [it]
   ((js-invoke it "for" (clj->js test-cases)) "$name"
    (evals/as-fixture
     (fn [^js tc ^js ctx]
       (-> (.run ctx (.-input tc) #js {:metadata tc})
           (.then
            (fn [^js result]
              (let [output (.-output result)
                    ns-output (namespace-block output)
                    require-idx (.indexOf ns-output "(:require")
                    import-idx (.indexOf ns-output "(:import")]
                (.toContain (expect output) "(ns demo.")
                (.. (expect output) -not (toContain "```"))
                (when (.-requiredSnippet tc)
                  (.toContain (expect ns-output) (.-requiredSnippet tc)))
                (when (.-forbiddenSnippet tc)
                  (.. (expect ns-output)
                      -not
                      (toContain (.-forbiddenSnippet tc))))
                (when (.-requireBeforeImport tc)
                  (.toBeGreaterThanOrEqual (expect require-idx) 0)
                  (.toBeGreaterThan (expect import-idx) require-idx)))))))))))
