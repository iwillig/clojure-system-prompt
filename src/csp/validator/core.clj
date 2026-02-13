(ns csp.validator.core
  "Core validator API - orchestrates skill and system validation."
  (:require [csp.validator.skill :as skill]
            [csp.validator.system :as system]))

(defn validate-skill
  "Validate a SKILL.md file in the given directory.
   
   Args:
     skill-path - Path to skill directory containing SKILL.md
   
   Returns:
     {:valid? boolean
      :errors [string...]
      :warnings [string...]
      :skill-path string}"
  [skill-path]
  (skill/validate-skill skill-path))

(defn validate-system
  "Validate a SYSTEM.md file.
   
   Args:
     system-path - Path to SYSTEM.md file
   
   Returns:
     {:valid? boolean
      :errors [string...]
      :warnings [string...]
      :system-path string
      :version string}"
  [system-path]
  (system/validate-system system-path))

(defn validate-all
  "Validate both skill and system files.
   
   Args:
     opts - Map with:
       :skill-path - Path to skill directory
       :system-path - Path to SYSTEM.md file
   
   Returns:
     {:skill {...}
      :system {...}
      :valid? boolean}"
  [{:keys [skill-path system-path]}]
  (let [skill-result (when skill-path (validate-skill skill-path))
        system-result (when system-path (validate-system system-path))
        all-valid? (and (or (nil? skill-result) (:valid? skill-result))
                       (or (nil? system-result) (:valid? system-result)))]
    (cond-> {:valid? all-valid?}
      skill-result (assoc :skill skill-result)
      system-result (assoc :system system-result))))
