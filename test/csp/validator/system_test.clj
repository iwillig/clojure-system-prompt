(ns csp.validator.system-test
  (:require [clojure.test :refer [deftest testing is]]
            [csp.validator.system :as system]))

(deftest validate-real-system-test
  (testing "validate actual SYSTEM.md"
    (let [result (system/validate-system "SYSTEM.md")]
      (is (:valid? result))
      (is (empty? (:errors result)))
      (is (= "v1.5.0" (:version result)))
      (is (contains? (:tags result) :system-prompt))
      (is (contains? (:tags result) :identity))
      (is (contains? (:tags result) :summary))
      (is (pos? (:code-blocks result))))))

(deftest validate-nonexistent-system-test
  (testing "nonexistent system file fails validation"
    (let [result (system/validate-system "nonexistent-SYSTEM.md")]
      (is (not (:valid? result)))
      (is (seq (:errors result))))))

(deftest extract-tags-test
  (testing "extract XML tags from content"
    (let [content "<system-prompt>\ntest\n</system-prompt>\n<identity>test</identity>"
          tags (system/extract-xml-tags content)]
      (is (set? tags))
      (is (contains? tags :system-prompt))
      (is (contains? tags :identity)))))

(deftest extract-version-test
  (testing "extract version from prompt-version tag"
    (is (= "v1.2.3" (system/extract-version "<prompt-version>v1.2.3</prompt-version>")))
    (is (nil? (system/extract-version "no version here")))))

(deftest extract-priorities-test
  (testing "extract priority attributes"
    (let [content "<core-mandate priority=\"critical\">test</core-mandate>"
          priorities (system/extract-priorities content)]
      (is (map? priorities))
      (is (= "critical" (:core-mandate priorities))))))
