(ns webapi.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:use [ring.middleware.params :only [wrap-params]])
  (:require [compojure.route :as route])
  (:require [cheshire.core :refer :all])
  (:gen-class))

(defroutes app
  (GET "/status/:id" [id]
       (try
         (let [int-id (Long/valueOf id)]
           {:status 200
            :headers {"Content-Type" "application/json"}
            :body (generate-string {:id int-id :is-int (integer? int-id)})})
         (catch java.lang.NumberFormatException e
           {:status 400
            :headers {"Content-Type" "text/html"}
            :body "<h1>'id' must be digits</h1>"})))
  (POST "/new" [url]
        (if (nil? url)
          {:status 400
           :headers {"Content-Type" "text/html"}
           :body "<h1>must post a url that you want to download</h1>"}
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (generate-string {:url url})}))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  "Will start the server"
  (prn "start running under port 8080")
  (run-server (wrap-params app) {:port 8080}))
