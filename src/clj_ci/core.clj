(ns clj-ci.core
  (:require [clj-ci.tests.core :as tests]
            [clj-ci.pulls.core :as pulls]
            [clj-ci.server :as server]))

(defn setup! [] nil)

(defn -main [args]
  (server/start! 8080))
