(ns csp.validator.system
  "Validate SYSTEM.md system prompt files."
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [csp.validator.schemas :as schemas]))

(defn extract-xml-tags
  "Extract XML-like tag names from system prompt.
   Returns set of tag keywords found."
  [content]
  (let [pattern #"<([a-z-]+)(?:\s+[^>]*)?>"
        matches (re-seq pattern content)]
    (into #{}
          (map (fn [[_ tag-name]]
                 (keyword tag-name))
               matches))))

(defn extract-version
  "Extract version from <prompt-version> tag."
  [content]
  (when-let [match (re-find #"<prompt-version>(.*?)</prompt-version>" content)]
    (second match)))

(defn extract-priorities
  "Extract priority attributes from tags."
  [content]
  (let [pattern #"<([a-z-]+)\s+priority=\"([^\"]+)\""
        matches (re-seq pattern content)]
    (into {}
          (map (fn [[_ tag-name priority]]
                 [(keyword tag-name) priority])
               matches))))

(defn validate-system-file-exists
  "Check that SYSTEM.md file exists."
  [system-path]
  (let [system-file (io/file system-path)]
    (if (.exists system-file)
      {:valid? true
       :path (.getPath system-file)}
      {:valid? false
       :errors [(str "SYSTEM.md not found at: " system-path)]})))

(defn validate-required-tags
  "Check that required tags are present."
  [tags]
  (let [required #{:system-prompt :identity :summary}
        missing (set/difference required tags)]
    (if (empty? missing)
      {:valid? true}
      {:valid? false
       :errors [(str "Missing required tags: "
                     (str/join ", " (map name missing)))]})))

(defn validate-version-format
  "Check that version matches semantic versioning."
  [version]
  (if version
    (if (re-matches schemas/version-pattern version)
      {:valid? true
       :version version}
      {:valid? false
       :errors [(str "Version '" version "' does not match pattern v1.2.3")]})
    {:valid? false
     :errors ["Missing <prompt-version> tag"]}))

(defn validate-priority-values
  "Check that priority attributes have valid values."
  [priorities]
  (let [invalid (remove (fn [[_ priority]]
                          (contains? schemas/priority-values priority))
                        priorities)]
    (if (empty? invalid)
      {:valid? true}
      {:valid? false
       :errors (map (fn [[tag priority]]
                      (str "Tag <" (name tag) "> has invalid priority '"
                           priority "'. Must be: critical, high, medium, or low"))
                    invalid)})))

(defn validate-code-examples
  "Check that code examples are present and well-formed.
   This is a basic check - could be enhanced with actual Clojure parsing."
  [content]
  (let [clojure-blocks (re-seq #"(?s)```clojure\n(.*?)```" content)]
    (if (empty? clojure-blocks)
      {:valid? true
       :warnings ["No Clojure code examples found"]}
      {:valid? true
       :code-blocks (count clojure-blocks)})))

(defn validate-system
  "Validate a SYSTEM.md file.
   
   Args:
     system-path - Path to SYSTEM.md file
   
   Returns:
     {:valid? boolean
      :errors [string...]
      :warnings [string...]
      :system-path string
      :version string
      :tags map}"
  [system-path]
  (let [;; Check file exists
        file-check (validate-system-file-exists system-path)]
    
    (if-not (:valid? file-check)
      (assoc file-check
             :system-path system-path
             :warnings [])
      
      ;; File exists, proceed with validation
      (let [content (slurp (:path file-check))
            
            ;; Extract structure
            tags (extract-xml-tags content)
            version (extract-version content)
            priorities (extract-priorities content)
            
            ;; Run validations
            required-tags (validate-required-tags tags)
            version-check (validate-version-format version)
            priority-check (validate-priority-values priorities)
            code-check (validate-code-examples content)
            
            ;; Collect errors and warnings
            all-errors (concat
                        (:errors required-tags)
                        (:errors version-check)
                        (:errors priority-check))
            all-warnings (concat
                          (:warnings code-check))
            
            valid? (empty? all-errors)]
        
        {:valid? valid?
         :errors (vec all-errors)
         :warnings (vec all-warnings)
         :system-path system-path
         :version version
         :tags tags
         :code-blocks (:code-blocks code-check)}))))
