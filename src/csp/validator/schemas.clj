(ns csp.validator.schemas
  "Malli schemas for validating SKILL.md and SYSTEM.md files."
  (:require [malli.core :as m]
            [malli.error :as me]))

;; ============================================================================
;; SKILL.md Schemas
;; ============================================================================

(def kebab-case-pattern
  "Kebab-case: lowercase letters, digits, hyphens. No leading/trailing/consecutive hyphens."
  #"^[a-z0-9]+(-[a-z0-9]+)*$")

(def no-angle-brackets-pattern
  "Pattern that fails if string contains angle brackets."
  #"^[^<>]*$")

(def skill-name-schema
  "Name must be kebab-case, 1-64 characters."
  [:and
   [:string {:min 1 :max 64}]
   [:re {:error/message "must be kebab-case (lowercase, hyphens only, no leading/trailing/consecutive hyphens)"}
    kebab-case-pattern]])

(def skill-description-schema
  "Description must be 1-1024 characters, no angle brackets."
  [:and
   [:string {:min 1 :max 1024}]
   [:re {:error/message "cannot contain angle brackets (< or >)"}
    no-angle-brackets-pattern]])

(def skill-compatibility-schema
  "Compatibility string, max 500 characters."
  [:string {:max 500 :optional true}])

(def skill-frontmatter-schema
  "SKILL.md frontmatter schema per Anthropic spec."
  [:map
   [:name skill-name-schema]
   [:description skill-description-schema]
   [:license {:optional true} :string]
   [:allowed-tools {:optional true} [:vector :string]]
   [:metadata {:optional true} :map]
   [:compatibility {:optional true} skill-compatibility-schema]])

;; ============================================================================
;; SYSTEM.md Schemas
;; ============================================================================

(def version-pattern
  "Semantic version pattern: v1.2.3"
  #"^v\d+\.\d+\.\d+$")

(def priority-values
  "Valid priority attribute values."
  #{"critical" "high" "medium" "low"})

(def system-version-schema
  "Version must match semantic versioning: v1.2.3"
  [:re {:error/message "must match pattern v1.2.3"}
   version-pattern])

(def system-tag-schema
  "Basic schema for system prompt tags."
  [:map
   [:tag-name :string]
   [:priority {:optional true}
    [:enum {:error/message "must be critical, high, medium, or low"}
     "critical" "high" "medium" "low"]]
   [:content :string]])

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(defn validate-with-humanized-errors
  "Validate data against schema and return humanized errors.
   
   Returns:
   {:valid? boolean
    :errors nil-or-map
    :data original-data}"
  [schema data]
  (if (m/validate schema data)
    {:valid? true
     :errors nil
     :data data}
    (let [explanation (m/explain schema data)]
      {:valid? false
       :errors (me/humanize explanation)
       :data data})))

(defn skill-frontmatter-validator
  "Create compiled validator for skill frontmatter."
  []
  (m/validator skill-frontmatter-schema))

(defn validate-skill-frontmatter
  "Validate skill frontmatter and return detailed errors."
  [frontmatter]
  (validate-with-humanized-errors skill-frontmatter-schema frontmatter))
