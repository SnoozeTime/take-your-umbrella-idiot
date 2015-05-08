(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]))

(def counter (atom 0))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (pr-str [(str "Hello " @counter) :from 'Heroku])})

(defroutes app
  (GET "/" []
       (splash))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))


(def poller-run? (atom true))

(defn database-poller []
  (while @poller-run?
      (println "hey")
      (swap! counter inc)
      (Thread/sleep 2000)))

(defn -main [& [port]]
  (do
    (.start (Thread. database-poller))
    (let [port (Integer. (or port (env :port) 5000))]
      (jetty/run-jetty (site #'app) {:port port :join? false}))))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
(defn stop-all-services []
  (reset! poller-run? false))
