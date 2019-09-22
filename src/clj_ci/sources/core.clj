(ns clj-ci.sources.core
  (:require [clojure.string :as str]
            [me.raynes.conch :as sh]))

(defn uri

  [uri]

  :git)

(defn path->source-id
  [path]
  (sh/let-programs [git "git"]
    (str/trim (git "rev-parse" "HEAD" {:dir path}))))
