(ns clj-ci.storage
  (:require [clojure.java.io :as io]))

(def +clj-ci+ (str (System/getProperty "user.home") "/.clj-ci/"))
(def +repos+ (str +clj-ci+ "repos/"))
(def +results+ (str +clj-ci+ "results/"))

(defn now! []
  (quot (System/currentTimeMillis) 1000))

;; (defn record!
;;   [project success? ])
