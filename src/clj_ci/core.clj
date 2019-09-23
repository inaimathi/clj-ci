(ns clj-ci.core
  (:require [clj-ci.tests.core :as tests]
            [clj-ci.sources.core :as srcs]
            [clj-ci.server :as server]
            [clj-ci.storage :as store]))

(defn -main [args]
  (store/setup!)
  (server/start! 8080))
