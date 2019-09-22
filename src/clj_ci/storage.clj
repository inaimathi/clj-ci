(ns clj-ci.storage
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]

            [clj-ci.sources.core :as srcs]
            [clj-ci.tests.core :as tests]))

(def ROOT (str (System/getProperty "user.home") "/.clj-ci/"))

(def PATH
  {:project-list (str ROOT "repos.edn")
   :repos (str ROOT "repos/")
   :results (str ROOT "results/")})

(def REPOS
  (atom []))

(defn now! []
  (quot (System/currentTimeMillis) 1000))

(defn setup!
  []
  (io/make-parents (:project-list PATH))
  (when (.exists (io/as-file (:project-list PATH)))
    (let [r (java.io.PushbackReader. (io/reader (:project-list PATH)))]
      (reset!
       REPOS
       (vec (take-while identity (repeatedly #(edn/read {:eof nil} r))))))))

(defn get-project
  [project-name]
  (first (filter #(= (:name %) project-name) @REPOS)))

(defn add-project!
  [project-name & {:keys [path uri]}]
  (assert (or path uri) "A project needs either a path or a URL")
  (when (not (get-project project-name))
    (let [project
          {:name project-name
           :path (or path (str (:repos PATH) project-name "/"))
           :uri uri}]
      (spit (:project-list PATH) project :append true)
      (spit (:project-list PATH) \newline :append true)
      (swap! REPOS #(conj % project))
      project)))

(defn run-test!
  [project-name]
  :TODO--run-a-test-of-the-given-project-and-store
  (if-let [project (get-project project-name)]
    (let [now (now!)
          res-path (str (:results PATH) project-name "/" now "/")
          log-path (str res-path "results.log")]
      (io/make-parents log-path)
      (let [res (tests/test-project! (:path project) log-path)]
        (spit (str res-path "results.edn")
              (assoc
               res
               :id (srcs/path->source-id (:path project))
               :timestamp now))))))

(defn file-pair->result
  [file-pair]
  (let [[results logs] (sort-by #(.getName %) file-pair)]
    (assoc
     (edn/read-string (slurp results))
     :logs logs)))

(defn results-of
  [project-name]
  (if-let [project (get-project project-name)]
    (let [path (str (:results PATH) project-name "/")]
      (if (.exists (io/as-file path))
        (->>  (io/file path)
              file-seq
              (filter #(.isFile %))
              (partition-by #(edn/read-string (.getName (io/file (.getParent %)))))
              (map file-pair->result))
        (do
          (io/make-parents path)
          [])))))

(defn result-at
  [project timestamp]
  (->> (results-of project)
       (sort-by :timestamp >)
       (drop-while #(> (:timestamp %) timestamp))
       first))

(defn latest-result
  [project]
  (->> (results-of project)
       (sort-by :timestamp >)
       first))
