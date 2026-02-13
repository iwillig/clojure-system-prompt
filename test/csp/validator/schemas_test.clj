(ns csp.validator.schemas-test
  (:require [clojure.test :refer [deftest testing is]]
            [csp.validator.schemas :as schemas]))

(deftest skill-name-validation-test
  (testing "valid kebab-case names"
    (is (:valid? (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "valid-skill-name")))
    (is (:valid? (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "skill123")))
    (is (:valid? (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "a"))))
  
  (testing "invalid names with underscores"
    (let [result (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "Invalid_Name")]
      (is (not (:valid? result)))
      (is (seq (:errors result)))))
  
  (testing "invalid names with uppercase"
    (let [result (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "InvalidName")]
      (is (not (:valid? result)))))
  
  (testing "invalid names with leading hyphen"
    (let [result (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "-invalid")]
      (is (not (:valid? result)))))
  
  (testing "invalid names with trailing hyphen"
    (let [result (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "invalid-")]
      (is (not (:valid? result)))))
  
  (testing "invalid names with consecutive hyphens"
    (let [result (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  "invalid--name")]
      (is (not (:valid? result)))))
  
  (testing "name too long"
    (let [long-name (apply str (repeat 65 "a"))
          result (schemas/validate-with-humanized-errors
                  schemas/skill-name-schema
                  long-name)]
      (is (not (:valid? result))))))

(deftest skill-description-validation-test
  (testing "valid descriptions"
    (is (:valid? (schemas/validate-with-humanized-errors
                  schemas/skill-description-schema
                  "A valid description")))
    (is (:valid? (schemas/validate-with-humanized-errors
                  schemas/skill-description-schema
                  "Description with numbers 123 and symbols !@#"))))
  
  (testing "invalid descriptions with angle brackets"
    (let [result (schemas/validate-with-humanized-errors
                  schemas/skill-description-schema
                  "Invalid <description>")]
      (is (not (:valid? result)))
      (is (seq (:errors result)))))
  
  (testing "description too long"
    (let [long-desc (apply str (repeat 1025 "a"))
          result (schemas/validate-with-humanized-errors
                  schemas/skill-description-schema
                  long-desc)]
      (is (not (:valid? result))))))

(deftest skill-frontmatter-validation-test
  (testing "valid frontmatter with required fields only"
    (let [result (schemas/validate-skill-frontmatter
                  {:name "valid-skill"
                   :description "A valid description"})]
      (is (:valid? result))
      (is (nil? (:errors result)))))
  
  (testing "valid frontmatter with all optional fields"
    (let [result (schemas/validate-skill-frontmatter
                  {:name "valid-skill"
                   :description "A valid description"
                   :license "MIT"
                   :allowed-tools ["tool1" "tool2"]
                   :metadata {:key "value"}
                   :compatibility "Compatible with version 2.0"})]
      (is (:valid? result))))
  
  (testing "missing required name field"
    (let [result (schemas/validate-skill-frontmatter
                  {:description "A description"})]
      (is (not (:valid? result)))
      (is (contains? (:errors result) :name))))
  
  (testing "missing required description field"
    (let [result (schemas/validate-skill-frontmatter
                  {:name "valid-skill"})]
      (is (not (:valid? result)))
      (is (contains? (:errors result) :description))))
  
  (testing "invalid name format"
    (let [result (schemas/validate-skill-frontmatter
                  {:name "Invalid_Name"
                   :description "Description"})]
      (is (not (:valid? result)))
      (is (contains? (:errors result) :name))))
  
  (testing "invalid description with angle brackets"
    (let [result (schemas/validate-skill-frontmatter
                  {:name "valid-name"
                   :description "Desc with <brackets>"})]
      (is (not (:valid? result)))
      (is (contains? (:errors result) :description)))))

(deftest system-version-validation-test
  (testing "valid semantic versions"
    (is (re-matches schemas/version-pattern "v1.0.0"))
    (is (re-matches schemas/version-pattern "v1.5.0"))
    (is (re-matches schemas/version-pattern "v2.10.15")))
  
  (testing "invalid version formats"
    (is (not (re-matches schemas/version-pattern "1.0.0")))
    (is (not (re-matches schemas/version-pattern "v1.0")))
    (is (not (re-matches schemas/version-pattern "v1.0.0.0")))))
