(ns csp.cli
  "CLI interface for validator using cli-matic."
  (:require [cli-matic.core :as cli]
            [csp.validator.core :as validator]
            [clojure.string :as str]))

(defn format-errors
  "Format errors for display."
  [errors]
  (if (empty? errors)
    ""
    (str "\nErrors:\n"
         (str/join "\n" (map #(str "  - " %) errors)))))

(defn format-warnings
  "Format warnings for display."
  [warnings]
  (if (empty? warnings)
    ""
    (str "\nWarnings:\n"
         (str/join "\n" (map #(str "  - " %) warnings)))))

(defn format-result
  "Format validation result for display."
  [result]
  (let [{:keys [valid? errors warnings]} result]
    (str (if valid? "VALID" "INVALID")
         (format-errors errors)
         (format-warnings warnings))))

(defn validate-skill-cmd
  "Validate a SKILL.md file."
  [{:keys [path]}]
  (let [result (validator/validate-skill path)]
    (println (str "Validating skill: " path))
    (println (format-result result))
    (if (:valid? result) 0 1)))

(defn validate-system-cmd
  "Validate a SYSTEM.md file."
  [{:keys [path]}]
  (let [result (validator/validate-system path)]
    (println (str "Validating system prompt: " path))
    (println (format-result result))
    (when (:version result)
      (println (str "\nVersion: " (:version result))))
    (if (:valid? result) 0 1)))

(defn validate-all-cmd
  "Validate both skill and system files."
  [{:keys [skill-path system-path]}]
  (let [result (validator/validate-all {:skill-path skill-path
                                        :system-path system-path})]
    (when skill-path
      (println "=== Validating Skill ===")
      (println (format-result (:skill result)))
      (println))
    
    (when system-path
      (println "=== Validating System Prompt ===")
      (println (format-result (:system result)))
      (when (get-in result [:system :version])
        (println (str "\nVersion: " (get-in result [:system :version]))))
      (println))
    
    (println (str "\n=== Overall Result: " (if (:valid? result) "VALID" "INVALID") " ==="))
    (if (:valid? result) 0 1)))

(def cli-config
  {:app-name "csp"
   :version "1.0.0"
   :description "Clojure System Prompt Validator"
   :subcommands
   [{:command "validate"
     :description "Validate skill and system prompt files"
     :subcommands
     [{:command "skill"
       :description "Validate a SKILL.md file"
       :opts [{:as "Path to skill directory"
               :option "path"
               :short "p"
               :type :string
               :default "clojure-repl-dev"}]
       :runs validate-skill-cmd}
      
      {:command "system"
       :description "Validate a SYSTEM.md file"
       :opts [{:as "Path to SYSTEM.md file"
               :option "path"
               :short "p"
               :type :string
               :default "SYSTEM.md"}]
       :runs validate-system-cmd}
      
      {:command "all"
       :description "Validate both skill and system files"
       :opts [{:as "Path to skill directory"
               :option "skill-path"
               :short "s"
               :type :string
               :default "clojure-repl-dev"}
              {:as "Path to SYSTEM.md file"
               :option "system-path"
               :short "p"
               :type :string
               :default "SYSTEM.md"}]
       :runs validate-all-cmd}]}]})

(defn -main [& args]
  (cli/run-cmd args cli-config))
