(ns clj-ci.server
  (:require [org.httpkit.server :as server]
            [bidi.ring :as bring]
            [ring.middleware.params :refer [wrap-params]]

            [cheshire.core :as json]

            [clj-ci.storage :as store]))

(defn -api-response
  [body & {:keys [status] :or {status 200}}]
  {:status status
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (str (json/encode body) \newline)})

(def routes
  ["" [["" (bring/redirect "/")]
       ["/" (fn [req] (-api-response "ok"))]
       ["/api/"
        {["project/" :id "/"]
         {""
          #(-api-response
            {:project (get-in % [:params :id])
             :history
             (->> (get-in % [:params :id])
                  store/results-of
                  (map (fn [r] (dissoc r :logs))))})

          "status.svg"
          #(-api-response
            {:todo "result widget"
             :project (get-in % [:params :id])})

          "results/"
          {""
           #(-api-response
             {:project (get-in % [:params :id])
              :result (store/latest-result (get-in % [:params :id]))})

           ["at/" :timestamp]
           #(-api-response
             {:todo "result history"
              :project (get-in % [:params :id])
              :time (get-in % [:params :timestamp])})}}}]
       ["/" (bring/->ResourcesMaybe {:prefix "public/"})]]])

(defonce +server+ (atom nil))

(defn start!
  [port]
  (reset!
   +server+
   (server/run-server
    (-> routes
        bring/make-handler
        wrap-params)
    {:port port
     :max-body 8388608}))
  (println "Listening on" port "..."))

(defn stop!
  []
  (when-let [s @+server+]
    (s :timeout 100)
    (reset! +server+ nil)
    (println "Server stopped...")))

(defn restart!
  [port]
  (stop!)
  (start! port))
