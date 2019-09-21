(ns clj-ci.tests.core
  (:require [clojure.java.io :as io]
            [me.raynes.conch :as sh]
            [me.raynes.conch.low-level :as low]))

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

(defn -shell-opts [path]
  {:out *out* :err *out* :dir path :throw false :timeout 20000 :verbose true})

(defmulti test-project! path->test-strategy)

(defmethod test-project! :default [project-path] nil)
(defmethod test-project! :clojure
  [project-path]
  (sh/let-programs [lein "lein"]
    (let [res (lein "test" (-shell-opts project-path))]
      {:success? (and (= 0 @(:exit-code res)) (->> res :proc :err empty?))
       :out (->> res :proc :out)})))

(defmethod test-project! :common-lisp-sbcl-prove
  [project-path]
  (sh/let-programs [sbcl "sbcl"]
    (sbcl "--eval" "(ql:quickload :prove)" "--eval" "(ql:quickload (list :trivial-sat-solver :trivial-sat-solver-test))" "--eval" "(and (or (prove:run :trivial-sat-solver-test) (uiop:quit -1)) (uiop:quit 0))" (-shell-opts "/home/inaimathi/quicklisp/local-projects/trivial-sat-solver"))))
