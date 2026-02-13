(ns csp.main
  (:require [csp.cli :as cli])
  (:gen-class))

(defn -main [& args]
  (apply cli/-main args))
