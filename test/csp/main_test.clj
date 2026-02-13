(ns csp.main-test
  (:require [clojure.test :refer [deftest testing is]]
            [csp.cli :as cli]))

(deftest cli-config-test
  (testing "CLI config is valid"
    (is (map? cli/cli-config))
    (is (= "csp" (:app-name cli/cli-config)))
    (is (seq (:subcommands cli/cli-config)))))
