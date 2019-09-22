(ns clj-ci.core
  (:require [clj-ci.tests.core :as tests]
            [clj-ci.sources.core :as srcs]
            [clj-ci.server :as server]
            [clj-ci.storage :as store]))

(defn setup! [] nil)

(defn -main [args]
  (server/start! 8080))
