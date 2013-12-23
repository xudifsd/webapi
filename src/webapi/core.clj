(ns webapi.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:use [ring.middleware.params :only [wrap-params]])
  (:require [compojure.route :as route])
  (:require [cheshire.core :refer :all])
  (:import (cloudDownload Utils TaskDispatcher Db Config))
  (:import (java.io FileInputStream File))
  (:gen-class))

(declare serverAddress)
(declare serverPort)

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
                                                             ":"
                                                             serverPort
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

                     (= (.state task) "removed")
                     (assoc result
                            :body (generate-string
                                    body))

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
  (when (.exists (File. "cloudDownload.conf"))
    (let [{fileContainer :fileContainer
           initUrl :initUrl
           dbName :dbName
           dbUsername :dbUsername
           dbPassword :dbPassword
           serverAddress-conf :serverAddress
           serverPort-conf :serverPort}
          (read-string (slurp "cloudDownload.conf"))]
      (when fileContainer
        (println (str "Using " fileContainer " as fileContainer"))
        (set! Config/fileContainer fileContainer))
      (when initUrl
        (println (str "Using " initUrl " as initUrl"))
        (set! Config/initUrl initUrl))
      (when dbName
        (println (str "Using " dbName " as dbName"))
        (set! Config/dbName dbName))
      (when dbUsername
        (println (str "Using " dbUsername " as dbUsername"))
        (set! Config/user dbUsername))
      (when dbPassword
        (println (str "Using " dbPassword " as dbPassword"))
        (set! Config/password dbPassword))
      (if serverAddress-conf
        (do
          (println (str "Using " serverAddress-conf " as serverAddress"))
          (def serverAddress serverAddress-conf))
        (def serverAddress "http://localhost"))
      (if serverPort-conf
        (do
          (println (str "Using " serverPort-conf " as serverPort"))
          (def serverPort serverPort-conf))
        (def serverPort "8080"))))
  (Utils/initSystem)
  (def taskDispatcher (TaskDispatcher. 3))
  (prn (str "start running under port " serverPort))
  (run-server (wrap-params app) {:port (Integer/valueOf serverPort)}))
