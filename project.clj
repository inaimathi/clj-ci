(defproject clj-ci "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]

                 [me.raynes/conch "0.8.0"]
                 [codax "1.3.1"]
                 [bidi "2.1.3"]
                 [http-kit "2.3.0"]

                 [cheshire "5.8.1"]
                 [hiccup "1.0.5"]]

  :main clj-ci.core
  :aot [clj-ci.core])
