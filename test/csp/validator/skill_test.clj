(ns csp.validator.skill-test
  (:require [clojure.test :refer [deftest testing is]]
            [csp.validator.skill :as skill]))

(deftest validate-real-skill-test
  (testing "validate actual clojure-repl-dev skill"
    (let [result (skill/validate-skill "clojure-repl-dev")]
      (is (:valid? result))
      (is (empty? (:errors result)))
      (is (= "clojure-repl-dev" (get-in result [:frontmatter :name]))))))

(deftest validate-nonexistent-skill-test
  (testing "nonexistent skill directory fails validation"
    (let [result (skill/validate-skill "nonexistent-skill-dir")]
      (is (not (:valid? result)))
      (is (seq (:errors result)))
      (is (some #(re-find #"SKILL.md not found" %) (:errors result))))))

(deftest validate-invalid-skill-test
  (testing "skill with invalid frontmatter fails validation"
    (let [result (skill/validate-skill "test/fixtures/invalid-skill")]
      (is (not (:valid? result)))
      (is (seq (:errors result)))
      ;; Should have errors for both name and description
      (is (some #(re-find #"name:" %) (:errors result)))
      (is (some #(re-find #"description:" %) (:errors result))))))
