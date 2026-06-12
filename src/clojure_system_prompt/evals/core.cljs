(ns clojure-system-prompt.evals.core
  (:require
   ["vitest-evals" :refer [createJudge describeEval]]))

(defn create-judge [judge-name assess-fn]
  (createJudge
   judge-name
   (fn [^js ctx]
     (let [result (assess-fn {:output   (.-output ctx)
                              :input    (.-input ctx)
                              :metadata (js->clj
                                         (.-metadata ctx)
                                         :keywordize-keys true)})]
       #js {:score    (:score result)
            :metadata (clj->js (or (:metadata result) {}))}))))

(defn as-fixture
  "Override toString so vitest's fixture parser sees the { run } pattern.
   The actual fn still receives (tc, ctx) where ctx.run is the harness runner."
  [f]
  (set! (.-toString f) (fn [] "async function(tc, { run }) {}"))
  f)

(defn describe-eval [suite-name {:keys [harness judges judge-threshold]} test-fn]
  (describeEval
   suite-name
   #js {:harness        harness
        :judges         (into-array judges)
        :judgeThreshold judge-threshold}
   test-fn))
