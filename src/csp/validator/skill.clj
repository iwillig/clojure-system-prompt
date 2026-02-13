(ns csp.validator.skill
  "Validate SKILL.md files per Anthropic skill specification."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [csp.validator.markdown :as md]
            [csp.validator.schemas :as schemas]))

(defn file-exists?
  "Check if file exists at path."
  [path]
  (.exists (io/file path)))

(defn validate-skill-file-exists
  "Check that SKILL.md exists in skill directory."
  [skill-path]
  (let [skill-md (io/file skill-path "SKILL.md")]
    (if (.exists skill-md)
      {:valid? true
       :path (.getPath skill-md)}
      {:valid? false
       :errors ["SKILL.md not found in directory"]})))

(defn validate-frontmatter-format
  "Check that markdown starts with YAML frontmatter."
  [content]
  (if (str/starts-with? content "---")
    (if-let [match (re-find #"(?s)^---\n(.*?)\n---" content)]
      {:valid? true
       :frontmatter-text (second match)}
      {:valid? false
       :errors ["Invalid frontmatter format - must be surrounded by --- delimiters"]})
    {:valid? false
     :errors ["No YAML frontmatter found - must start with ---"]}))

(defn validate-no-unexpected-keys
  "Check for keys not in allowed set."
  [frontmatter]
  (let [allowed #{:name :description :license :allowed-tools :metadata :compatibility}
        unexpected (remove allowed (keys frontmatter))]
    (if (empty? unexpected)
      {:valid? true}
      {:valid? false
       :errors [(str "Unexpected key(s) in frontmatter: "
                     (str/join ", " (map name unexpected))
                     ". Allowed properties are: "
                     (str/join ", " (map name (sort allowed))))]})))

(defn validate-reference-files
  "Check that referenced files exist (references/*.md)."
  [skill-path content]
  (let [;; Extract relative paths from markdown links
        ref-pattern #"\[.*?\]\((references/[^\)]+\.md)\)"
        refs (map second (re-seq ref-pattern content))
        skill-dir (io/file skill-path)
        missing (remove (fn [ref]
                          (.exists (io/file skill-dir ref)))
                        refs)]
    (if (empty? missing)
      {:valid? true}
      {:valid? false
       :errors (map #(str "Referenced file not found: " %) missing)})))

(defn validate-skill
  "Validate a skill directory.

   Args:
     skill-path - Path to skill directory (containing SKILL.md)

   Returns:
     {:valid? boolean
      :errors [string...]
      :warnings [string...]
      :skill-path string}"
  [skill-path]
  (let [;; Check file exists
        file-check (validate-skill-file-exists skill-path)]

    (if-not (:valid? file-check)
      (assoc file-check
             :skill-path skill-path
             :warnings [])

      ;; File exists, proceed with validation
      (let [content (slurp (:path file-check))

            ;; Validate frontmatter format
            fm-format (validate-frontmatter-format content)]

        (if-not (:valid? fm-format)
          (assoc fm-format
                 :skill-path skill-path
                 :warnings [])

          ;; Parse frontmatter
          (let [parsed (md/parse-frontmatter content)
                frontmatter (:frontmatter parsed)

                ;; Run validations
                unexpected-keys (validate-no-unexpected-keys frontmatter)
                schema-validation (schemas/validate-skill-frontmatter frontmatter)
                ref-files (validate-reference-files skill-path content)

                ;; Collect errors
                all-errors (concat
                            (:errors unexpected-keys)
                            (when-let [errs (:errors schema-validation)]
                              (for [[k msgs] errs
                                    msg msgs]
                                (str (name k) ": " msg)))
                            (:errors ref-files))

                valid? (empty? all-errors)]

            {:valid? valid?
             :errors (vec all-errors)
             :warnings []
             :skill-path skill-path
             :frontmatter frontmatter}))))))
