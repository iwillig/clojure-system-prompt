(ns clojure-system-prompt.evals.harness
  (:require
   ["node:fs/promises" :as fs]
   ["vitest-evals" :refer [createHarness]]
   [clojure.string :as str]))

(defn- env [k]
  (aget (.-env js/process) k))

(defn- default-base-url []
  (or (env "LLAMA_SERVER_BASE_URL")
      "http://127.0.0.1:8080/v1"))

(defn- default-model []
  (or (env "LLAMA_SERVER_MODEL")
      "unsloth/Qwen3.6-35B-A3B-MTP-GGUF:UD-Q8_K_XL"))

(defn- default-api-key []
  (env "LLAMA_SERVER_API_KEY"))

(defn- load-system-prompt [path]
  (.readFile fs path "utf8"))

(defn- chat-url [base-url]
  (str (str/replace base-url #"/+$" "") "/chat/completions"))

(defn- strip-fence [s]
  (let [m (.match s (js/RegExp. "^```[\\w-]*\\n([\\s\\S]*?)\\n```$"))]
    (if (and m (aget m 1))
      (.trim (aget m 1))
      s)))

(defn- extract-content [^js raw]
  (let [choices (.-choices raw)]
    (when choices
      (some-> (aget choices 0) .-message .-content))))

(defn- parse-response [resp text model]
  (let [raw    (js/JSON.parse text)
        status (.-status resp)]
    (when-not (.-ok resp)
      (throw (js/Error.
              (str "llama-server error (" status "): " text))))
    (let [content (extract-content raw)
          usage   (.-usage raw)]
      {:output        (some-> content .trim strip-fence)
       :model         (or (.-model raw) model)
       :status        status
       :input-tokens  (some-> usage .-prompt_tokens)
       :output-tokens (some-> usage .-completion_tokens)
       :total-tokens  (some-> usage .-total_tokens)})))

(defn- run-request [{:keys [input system-prompt base-url model api-key]}]
  (let [url     (chat-url (or base-url (default-base-url)))
        model   (or model (default-model))
        key     (or api-key (default-api-key))
        headers (cond-> {"Content-Type" "application/json"}
                  key (assoc "Authorization" (str "Bearer " key)))
        body    (js/JSON.stringify
                 #js {:model       model
                      :temperature 0
                      :messages    #js [#js {:role "system"
                                             :content system-prompt}
                                       #js {:role "user"
                                            :content input}]})]
    (.then
     (js/fetch url #js {:method  "POST"
                        :headers (clj->js headers)
                        :body    body})
     (fn [^js resp]
       (.then
        (.text resp)
        (fn [text]
          (parse-response resp text model)))))))

(defn- build-result [^js ctx {:keys [output model status input-tokens
                                     output-tokens total-tokens]}]
  (.setArtifact ctx "llama-server" #js {:status status :model model})
  #js {:output output
       :usage  #js {:provider     "llama-server"
                    :model        model
                    :inputTokens  input-tokens
                    :outputTokens output-tokens
                    :totalTokens  total-tokens}})

(def llama-server-harness
  (createHarness
   #js {:name "llama-server"
        :run  (fn [^js ctx]
                (let [meta           (js->clj (.-metadata ctx)
                                             :keywordize-keys true)
                      prompt-path    (or (:systemPromptPath meta) "SYSTEM.md")
                      prompt-promise (if-let [prompt (:systemPrompt meta)]
                                       (.resolve js/Promise prompt)
                                       (load-system-prompt prompt-path))]
                  (.then
                   prompt-promise
                   (fn [system-prompt]
                     (.then
                      (run-request {:input         (.-input ctx)
                                    :system-prompt system-prompt
                                    :base-url      (:baseUrl meta)
                                    :model         (:model meta)
                                    :api-key       (:apiKey meta)})
                      (fn [result]
                        (build-result ctx result)))))))}))
