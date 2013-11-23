(ns webapi.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:use [ring.middleware.params :only [wrap-params]])
  (:require [compojure.route :as route])
  (:require [cheshire.core :refer :all])
  (:import (cloudDownload Utils TaskDispatcher Db Config))
  (:import (java.io FileInputStream))
  (:gen-class))

(def serverAddress "http://localhost:8080")

(declare taskDispatcher)

(defroutes app
  (GET "/status/:id" [id]
       (try
         (let [int-id (Long/valueOf id)
               task (Db/getTask int-id)]
           (if (nil? task)
             {:status 404
              :headers {"Content-Type" "text/html"}
              :body (str "<h1>" id " not existed</h1>")}
             (let [result {:status 200 :headers {"Content-Type" "text/html"}}
                   body {:id (.id task) :status (.state task)}]
               (cond (= (.state task) "succeeded")
                     (assoc result
                            :body (generate-string
                                    (assoc body
                                           :retrieveUrl (str serverAddress
                                                             "/retrieve/"
                                                             (.retrieveURL task)))))

                     (= (.state task) "failed")
                     (assoc result
                            :body (generate-string
                                    (assoc body
                                           :reason (.reason task))))

                     (or
                       (= (.state task) "downloading")
                       (= (.state task) "pending"))
                     (assoc result
                            :body (generate-string
                                    (assoc body
                                           :finished (str (.progress task) "%"))))

                     :else
                     {:status 500
                      :body (str "unknow state of task '" (.state task) "'")}
               ))))
         (catch java.lang.NumberFormatException e
           {:status 400
            :headers {"Content-Type" "text/html"}
            :body "<h1>'id' must be digits</h1>"})
         (catch Exception e
           {:status 404
            :headers {"Content-Type" "text/html"}
            :body (str "<h1>" id " not existed</h1>")})))

  (POST "/new" [url]
        (if (nil? url)
          {:status 400
           :headers {"Content-Type" "text/html"}
           :body "<h1>must post a url that you want to download</h1>"}
          (let [id (.newDownload taskDispatcher url)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (generate-string {:id id})})))

  (GET "/retrieve/:url" [url]
        (try
          (let [f (FileInputStream.
                    (str (Config/fileContainer) "/" (Db/retrieve url)))]
            {:status 200
             :body f})
          (catch java.io.FileNotFoundException e
            {:status 404
             :header {"Content-Type" "text/html"}
             :body (str "<h1>" url " is not found in server, maybe you should redownload it</h1>")})))

  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  "Will start the server"
  (prn "start running under port 8080")
  (Utils/initSystem)
  (def taskDispatcher (TaskDispatcher. 3))
  (run-server (wrap-params app) {:port 8080}))
