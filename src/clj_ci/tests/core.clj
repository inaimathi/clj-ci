(ns clj-ci.tests.core
  (:require [clojure.java.io :as io]
            [me.raynes.conch :as sh]))

(defn path->test-strategy
  [project-path]
  (let [files (->> project-path io/file file-seq)
        fname-set (->> files (map #(.getName %)) set)]
    (cond
      (contains? fname-set "project.clj")
      :clojure

      (contains? fname-set "run-test.lisp")
      :common-lisp-custom

      (contains? fname-set "run-tests.sh")
      :shell-custom

      :else nil)))

(defn -shell-opts [path & {:keys [out timeout] :or {out *out* timeout 20000}}]
  {:out out :err out :dir path :throw false :timeout timeout :verbose true})

(defmulti test-project!
  (fn [project-path log-path]
    (path->test-strategy project-path)))

(defmethod test-project! :default [project-path log-path] nil)
(defmethod test-project! :clojure
  [project-path log-path]
  (sh/let-programs [lein "lein"]
    (let [res (lein "test" (-shell-opts project-path :out (io/file log-path)))]
      {:success? (and (= 0 @(:exit-code res)) (->> res :proc :err empty?))})))

(defmethod test-project! :common-lisp-sbcl-prove
  [project-path log-path]
  (sh/let-programs [sbcl "sbcl"]
    (let [res (sbcl "--eval" "(ql:quickload :prove)" "--eval" "(ql:quickload (list :trivial-sat-solver :trivial-sat-solver-test))" "--eval" "(and (or (prove:run :trivial-sat-solver-test) (uiop:quit -1)) (uiop:quit 0))" (-shell-opts "/home/inaimathi/quicklisp/local-projects/trivial-sat-solver" :out (io/file log-path)))]
      {:success? (and (= 0 @(:exit-code res)) (->> res :proc :err empty?))})))
