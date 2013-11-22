(defproject webapi "0.1.0-SNAPSHOT"
            :description "web interface for cloudDownload"
            :url "http://example.com/FIXME"
            :main webapi.core
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :resource-paths ["resources/cloudDownload.jar" "resources/mysql-connector-java-5.1.26-bin.jar"]
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [http-kit "2.1.13"]
                           [compojure "1.1.5"]
                           [cheshire "5.2.0"]])
